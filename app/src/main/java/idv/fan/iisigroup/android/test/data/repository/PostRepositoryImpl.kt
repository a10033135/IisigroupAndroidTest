package idv.fan.iisigroup.android.test.data.repository

import idv.fan.iisigroup.android.test.data.remote.api.PostApiService
import idv.fan.iisigroup.android.test.data.remote.model.PostResponse
import idv.fan.iisigroup.android.test.domain.model.Post
import idv.fan.iisigroup.android.test.domain.repository.PostRepository
import idv.fan.iisigroup.android.test.network.ApiResult
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val apiService: PostApiService,
) : PostRepository {

    override suspend fun getPosts(): ApiResult<List<Post>> = runCatching {
        apiService.getPosts().map { it.toDomain() }
    }.fold(
        onSuccess = { ApiResult.Success(it) },
        onFailure = { ApiResult.Error(it.message ?: "Unknown error", it) },
    )

    override suspend fun getPostById(id: Int): ApiResult<Post> = runCatching {
        apiService.getPostById(id).toDomain()
    }.fold(
        onSuccess = { ApiResult.Success(it) },
        onFailure = { ApiResult.Error(it.message ?: "Unknown error", it) },
    )

    private fun PostResponse.toDomain() = Post(
        id = id,
        userId = userId,
        title = title,
        body = body,
    )
}
