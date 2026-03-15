package idv.fan.iisigroup.android.test.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExchangeRateResponse(
    @Json(name = "data") val data: Map<String, Double>,
)
