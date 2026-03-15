package idv.fan.iisigroup.android.test

import idv.fan.iisigroup.android.test.domain.model.Post
import idv.fan.iisigroup.android.test.domain.repository.PostRepository
import idv.fan.iisigroup.android.test.domain.usecase.GetPostsUseCase
import idv.fan.iisigroup.android.test.network.ApiResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetPostsUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockRepository = mockk<PostRepository>()
    private lateinit var useCase: GetPostsUseCase

    @Before
    fun setup() {
        useCase = GetPostsUseCase(mockRepository)
    }

    @Test
    fun `invoke delegates to repository getPosts`() = runTest {
        val posts = listOf(Post(1, 1, "Title", "Body"))
        coEvery { mockRepository.getPosts() } returns ApiResult.Success(posts)

        val result = useCase()

        assertTrue(result is ApiResult.Success)
        assertEquals(posts, (result as ApiResult.Success).data)
        coVerify(exactly = 1) { mockRepository.getPosts() }
    }

    @Test
    fun `invoke returns Error when repository fails`() = runTest {
        coEvery { mockRepository.getPosts() } returns ApiResult.Error("Network error")

        val result = useCase()

        assertTrue(result is ApiResult.Error)
        assertEquals("Network error", (result as ApiResult.Error).message)
    }

    @Test
    fun `invoke calls repository exactly once`() = runTest {
        coEvery { mockRepository.getPosts() } returns ApiResult.Success(emptyList())

        useCase()

        coVerify(exactly = 1) { mockRepository.getPosts() }
    }
}
