package idv.fan.iisigroup.android.test

import idv.fan.iisigroup.android.test.domain.model.Post
import idv.fan.iisigroup.android.test.domain.usecase.GetPostsUseCase
import idv.fan.iisigroup.android.test.network.ApiResult
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockGetPostsUseCase = mockk<GetPostsUseCase>()
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        viewModel = MainViewModel(mockGetPostsUseCase)
    }

    // --- Navigation tests ---

    @Test
    fun `initial destination is HOME`() {
        assertEquals(AppDestinations.HOME, viewModel.currentDestination.value)
    }

    @Test
    fun `navigateTo FAVORITES changes destination to FAVORITES`() = runTest {
        viewModel.navigateTo(AppDestinations.FAVORITES)
        assertEquals(AppDestinations.FAVORITES, viewModel.currentDestination.value)
    }

    @Test
    fun `navigateTo PROFILE changes destination to PROFILE`() = runTest {
        viewModel.navigateTo(AppDestinations.PROFILE)
        assertEquals(AppDestinations.PROFILE, viewModel.currentDestination.value)
    }

    @Test
    fun `navigateTo HOME from FAVORITES returns to HOME`() = runTest {
        viewModel.navigateTo(AppDestinations.FAVORITES)
        viewModel.navigateTo(AppDestinations.HOME)
        assertEquals(AppDestinations.HOME, viewModel.currentDestination.value)
    }

    @Test
    fun `navigateTo same destination keeps current destination`() = runTest {
        viewModel.navigateTo(AppDestinations.HOME)
        assertEquals(AppDestinations.HOME, viewModel.currentDestination.value)
    }

    @Test
    fun `navigateTo is called and updates state via spyk`() = runTest {
        val spyViewModel = spyk(viewModel)
        spyViewModel.navigateTo(AppDestinations.PROFILE)
        verify { spyViewModel.navigateTo(AppDestinations.PROFILE) }
        assertEquals(AppDestinations.PROFILE, spyViewModel.currentDestination.value)
    }

    // --- Posts API tests ---

    @Test
    fun `initial postsState is null`() {
        assertEquals(null, viewModel.postsState.value)
    }

    @Test
    fun `fetchPosts sets postsState to Success on success`() = runTest {
        val posts = listOf(Post(1, 1, "Title", "Body"))
        coEvery { mockGetPostsUseCase() } returns ApiResult.Success(posts)

        viewModel.fetchPosts()
        advanceUntilIdle()

        assertTrue(viewModel.postsState.value is ApiResult.Success)
        assertEquals(posts, (viewModel.postsState.value as ApiResult.Success).data)
    }

    @Test
    fun `fetchPosts sets postsState to Error on failure`() = runTest {
        coEvery { mockGetPostsUseCase() } returns ApiResult.Error("Server error")

        viewModel.fetchPosts()
        advanceUntilIdle()

        assertTrue(viewModel.postsState.value is ApiResult.Error)
        assertEquals("Server error", (viewModel.postsState.value as ApiResult.Error).message)
    }
}
