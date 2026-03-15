package idv.fan.iisigroup.android.test.network

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

/**
 * 動態解析 kia.gov.tw InstantSchedule.ashx 的 JS redirect。
 *
 * 該端點回傳含 JavaScript 的 HTML，透過 window.location.href 導向實際 JSON 檔案。
 * OkHttp 無法執行 JS，此 Interceptor 改由 regex 解析 HTML 中的條件邏輯，
 * 動態取得對應 URL，再自動重打一次 request。
 *
 * 解析目標 JS 結構：
 *   if (line === "X" && io === "Y") target = "some.json";
 *   window.location.href = "/some/path/" + target;
 */
class FlightJsRedirectInterceptor : Interceptor {

    // 比對每個 if/else if 的條件與 target 值
    // 例：line === "2" && io === "2" ... target = "InstantSchedule_DOMARR.json"
    private val conditionRegex = Regex(
        """line\s*===?\s*"(\d+)"[^}]*?io\s*===?\s*"(\d+)"[^}]*?target\s*=\s*"([^"]+)"""",
        RegexOption.DOT_MATCHES_ALL,
    )

    // 比對 window.location.href = "/path/" + target 中的 base path
    private val basePathRegex = Regex(
        """window\.location\.href\s*=\s*"([^"]+)"\s*\+""",
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val response = chain.proceed(originalRequest)

        val contentType = response.header("Content-Type") ?: return response
        if (!contentType.contains("text/html", ignoreCase = true)) return response

        val url = originalRequest.url
        val airFlyLine = url.queryParameter("AirFlyLine") ?: return response
        val airFlyIO = url.queryParameter("AirFlyIO") ?: return response

        val html = response.peekBody(Long.MAX_VALUE).string()

        val target = conditionRegex.findAll(html)
            .firstOrNull { it.groupValues[1] == airFlyLine && it.groupValues[2] == airFlyIO }
            ?.groupValues?.get(3)

        val basePath = basePathRegex.find(html)?.groupValues?.get(1)

        if (target == null || basePath == null) {
            Timber.w("JS redirect parse failed (line=$airFlyLine, io=$airFlyIO)")
            return response
        }

        Timber.d("JS redirect → $basePath$target")
        response.close()

        val redirectUrl = url.newBuilder()
            .encodedPath("$basePath$target")
            .removeAllQueryParameters("AirFlyLine")
            .removeAllQueryParameters("AirFlyIO")
            .build()

        val redirectRequest = originalRequest.newBuilder().url(redirectUrl).build()
        return chain.proceed(redirectRequest)
    }
}
