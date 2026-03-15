package idv.fan.iisigroup.android.test.domain.usecase

import idv.fan.iisigroup.android.test.domain.model.Post
import idv.fan.iisigroup.android.test.domain.repository.PostRepository
import idv.fan.iisigroup.android.test.network.ApiResult
import javax.inject.Inject

class GetPostsUseCase @Inject constructor(
    private val repository: PostRepository,
) {
    suspend operator fun invoke(): ApiResult<List<Post>> = repository.getPosts()
}
