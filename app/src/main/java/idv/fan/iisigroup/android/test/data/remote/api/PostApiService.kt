package idv.fan.iisigroup.android.test.data.remote.api

import idv.fan.iisigroup.android.test.data.remote.model.PostResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface PostApiService {

    @GET("posts")
    suspend fun getPosts(): List<PostResponse>

    @GET("posts/{id}")
    suspend fun getPostById(@Path("id") id: Int): PostResponse
}
