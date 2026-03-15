package idv.fan.iisigroup.android.test.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExchangeRateResponse(
    @Json(name = "meta") val meta: Meta,
    @Json(name = "data") val data: Map<String, Double>,
) {
    @JsonClass(generateAdapter = true)
    data class Meta(
        @Json(name = "last_updated_at") val lastUpdatedAt: String,
    )
}
