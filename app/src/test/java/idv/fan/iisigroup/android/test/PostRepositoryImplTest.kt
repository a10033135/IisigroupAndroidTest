package idv.fan.iisigroup.android.test

import idv.fan.iisigroup.android.test.data.remote.api.PostApiService
import idv.fan.iisigroup.android.test.data.remote.model.PostResponse
import idv.fan.iisigroup.android.test.data.repository.PostRepositoryImpl
import idv.fan.iisigroup.android.test.network.ApiResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PostRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockApiService = mockk<PostApiService>()
    private lateinit var repository: PostRepositoryImpl

    @Before
    fun setup() {
        repository = PostRepositoryImpl(mockApiService)
    }

    @Test
    fun `getPosts returns Success with mapped domain models`() = runTest {
        val responses = listOf(
            PostResponse(1, 1, "Title 1", "Body 1"),
            PostResponse(2, 1, "Title 2", "Body 2"),
        )
        coEvery { mockApiService.getPosts() } returns responses

        val result = repository.getPosts()

        assertTrue(result is ApiResult.Success)
        val posts = (result as ApiResult.Success).data
        assertEquals(2, posts.size)
        assertEquals(1, posts[0].id)
        assertEquals("Title 1", posts[0].title)
        assertEquals("Body 2", posts[1].body)
    }

    @Test
    fun `getPosts returns Error when API throws exception`() = runTest {
        coEvery { mockApiService.getPosts() } throws RuntimeException("Network error")

        val result = repository.getPosts()

        assertTrue(result is ApiResult.Error)
        assertEquals("Network error", (result as ApiResult.Error).message)
    }

    @Test
    fun `getPostById returns Success with correct domain model`() = runTest {
        val response = PostResponse(42, 3, "My Title", "My Body")
        coEvery { mockApiService.getPostById(42) } returns response

        val result = repository.getPostById(42)

        assertTrue(result is ApiResult.Success)
        val post = (result as ApiResult.Success).data
        assertEquals(42, post.id)
        assertEquals(3, post.userId)
        assertEquals("My Title", post.title)
    }

    @Test
    fun `getPostById returns Error when API throws exception`() = runTest {
        coEvery { mockApiService.getPostById(99) } throws RuntimeException("Not found")

        val result = repository.getPostById(99)

        assertTrue(result is ApiResult.Error)
        assertEquals("Not found", (result as ApiResult.Error).message)
    }

    @Test
    fun `getPosts Error includes cause throwable`() = runTest {
        val cause = RuntimeException("timeout")
        coEvery { mockApiService.getPosts() } throws cause

        val result = repository.getPosts()

        assertTrue(result is ApiResult.Error)
        assertEquals(cause, (result as ApiResult.Error).cause)
    }
}
