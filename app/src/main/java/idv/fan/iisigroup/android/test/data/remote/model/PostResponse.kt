package idv.fan.iisigroup.android.test.data.remote.model

import com.google.gson.annotations.SerializedName

data class PostResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String,
)
