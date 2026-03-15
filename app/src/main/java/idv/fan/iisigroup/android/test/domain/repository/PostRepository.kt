package idv.fan.iisigroup.android.test.domain.repository

import idv.fan.iisigroup.android.test.domain.model.Post
import idv.fan.iisigroup.android.test.network.ApiResult

interface PostRepository {
    suspend fun getPosts(): ApiResult<List<Post>>
    suspend fun getPostById(id: Int): ApiResult<Post>
}
