package idv.fan.iisigroup.android.test.network

import io.mockk.every
import io.mockk.firstArg
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class FlightJsRedirectInterceptorTest {

    private lateinit var chain: Interceptor.Chain
    private lateinit var interceptor: FlightJsRedirectInterceptor

    private val baseUrl = "https://kia.gov.tw"

    @Before
    fun setUp() {
        chain = mockk()
        interceptor = FlightJsRedirectInterceptor()
    }

    private fun buildRequest(path: String, queryParams: Map<String, String> = emptyMap()): Request {
        val urlBuilder = "$baseUrl$path".toHttpUrl().newBuilder()
        queryParams.forEach { (key, value) -> urlBuilder.addQueryParameter(key, value) }
        return Request.Builder().url(urlBuilder.build()).build()
    }

    private fun buildResponse(
        request: Request,
        contentType: String,
        body: String,
    ): Response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("OK")
        .header("Content-Type", contentType)
        .body(body.toResponseBody(contentType.toMediaType()))
        .build()

    // -------------------------------------------------------------------------
    // 1. non-HTML response is returned without second request
    // -------------------------------------------------------------------------
    @Test
    fun `non-HTML response is returned without second request`() {
        val request = buildRequest("/AirInfo/InstantSchedule.ashx", mapOf("AirFlyLine" to "2", "AirFlyIO" to "2"))
        val jsonResponse = buildResponse(request, "application/json", """[{"key":"value"}]""")

        every { chain.request() } returns request
        every { chain.proceed(request) } returns jsonResponse

        val result = interceptor.intercept(chain)

        assertNotNull(result)
        // proceed should only be called once (no redirect)
        verify(exactly = 1) { chain.proceed(any()) }
    }

    // -------------------------------------------------------------------------
    // 2. HTML with matching JS redirect makes second request to correct URL
    // -------------------------------------------------------------------------
    @Test
    fun `HTML with matching JS redirect makes second request to correct URL`() {
        val request = buildRequest(
            "/AirInfo/InstantSchedule.ashx",
            mapOf("AirFlyLine" to "2", "AirFlyIO" to "2"),
        )

        val html = """
            <html>
            <script>
            var line = "2"; var io = "2";
            if (line === "2" && io === "2") target = "InstantSchedule_DOMARR.json";
            window.location.href = "/AirInfo/" + target;
            </script>
            </html>
        """.trimIndent()

        val htmlResponse = buildResponse(request, "text/html; charset=utf-8", html)
        val redirectResponse: Response = mockk(relaxed = true)

        val capturedRequests = mutableListOf<Request>()

        every { chain.request() } returns request
        every { chain.proceed(any()) } answers {
            val req = firstArg<Request>()
            capturedRequests.add(req)
            if (req == request) htmlResponse else redirectResponse
        }

        interceptor.intercept(chain)

        // Verify second request was made
        verify(exactly = 2) { chain.proceed(any()) }
        assertEquals(2, capturedRequests.size)

        // Verify the captured redirect URL is correct
        val redirectUrl = capturedRequests[1].url
        assertEquals("/AirInfo/InstantSchedule_DOMARR.json", redirectUrl.encodedPath)
        // Query params should be removed
        assertEquals(null, redirectUrl.queryParameter("AirFlyLine"))
        assertEquals(null, redirectUrl.queryParameter("AirFlyIO"))
    }

    // -------------------------------------------------------------------------
    // 3. HTML without matching conditions returns original response without second request
    // -------------------------------------------------------------------------
    @Test
    fun `HTML without matching conditions returns original response without second request`() {
        val request = buildRequest(
            "/AirInfo/InstantSchedule.ashx",
            mapOf("AirFlyLine" to "2", "AirFlyIO" to "2"),
        )

        // HTML with mismatched line/io values
        val html = """
            <html>
            <script>
            var line = "1"; var io = "1";
            if (line === "1" && io === "1") target = "InstantSchedule_OTHER.json";
            window.location.href = "/AirInfo/" + target;
            </script>
            </html>
        """.trimIndent()

        val htmlResponse = buildResponse(request, "text/html; charset=utf-8", html)

        every { chain.request() } returns request
        every { chain.proceed(request) } returns htmlResponse

        val result = interceptor.intercept(chain)

        assertNotNull(result)
        // Only the initial proceed should have been called, no redirect
        verify(exactly = 1) { chain.proceed(any()) }
    }
}
