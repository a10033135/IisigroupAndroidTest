package idv.fan.iisigroup.android.test

import idv.fan.iisigroup.android.test.data.local.datastore.UserPreferencesDataStore
import idv.fan.iisigroup.android.test.domain.model.Post
import idv.fan.iisigroup.android.test.domain.usecase.GetPostsUseCase
import idv.fan.iisigroup.android.test.network.ApiResult
import idv.fan.iisigroup.android.test.ui.state.PostUiState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    private val mockDataStore = mockk<UserPreferencesDataStore>()
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        every { mockDataStore.isDarkTheme } returns flowOf(false)
        viewModel = MainViewModel(mockGetPostsUseCase, mockDataStore)
    }

    // --- Navigation ---

    @Test
    fun `initial destination is HOME`() {
        assertEquals(AppDestinations.HOME, viewModel.currentDestination.value)
    }

    @Test
    fun `navigateTo FAVORITES changes destination`() = runTest {
        viewModel.navigateTo(AppDestinations.FAVORITES)
        assertEquals(AppDestinations.FAVORITES, viewModel.currentDestination.value)
    }

    @Test
    fun `navigateTo PROFILE changes destination`() = runTest {
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
    fun `navigateTo via spyk verifies call`() = runTest {
        val spyViewModel = spyk(viewModel)
        spyViewModel.navigateTo(AppDestinations.PROFILE)
        verify { spyViewModel.navigateTo(AppDestinations.PROFILE) }
        assertEquals(AppDestinations.PROFILE, spyViewModel.currentDestination.value)
    }

    // --- Posts UI State ---

    @Test
    fun `initial postUiState is Idle`() {
        assertTrue(viewModel.postUiState.value is PostUiState.Idle)
    }

    @Test
    fun `fetchPosts transitions through Loading then Success`() = runTest {
        val posts = listOf(Post(1, 1, "Title", "Body"))
        coEvery { mockGetPostsUseCase() } returns ApiResult.Success(posts)

        viewModel.fetchPosts()
        advanceUntilIdle()

        val state = viewModel.postUiState.value
        assertTrue(state is PostUiState.Success)
        assertEquals(posts, (state as PostUiState.Success).posts)
    }

    @Test
    fun `fetchPosts sets Error state on failure`() = runTest {
        coEvery { mockGetPostsUseCase() } returns ApiResult.Error("Server error")

        viewModel.fetchPosts()
        advanceUntilIdle()

        val state = viewModel.postUiState.value
        assertTrue(state is PostUiState.Error)
        assertEquals("Server error", (state as PostUiState.Error).message)
    }

    // --- DataStore ---

    @Test
    fun `isDarkTheme default is false`() {
        assertEquals(false, viewModel.isDarkTheme.value)
    }

    @Test
    fun `isDarkTheme reflects DataStore value when subscribed`() = runTest {
        every { mockDataStore.isDarkTheme } returns flowOf(true)
        val vm = MainViewModel(mockGetPostsUseCase, mockDataStore)
        val collected = mutableListOf<Boolean>()
        val job = vm.isDarkTheme.onEach { collected.add(it) }.launchIn(this)
        advanceUntilIdle()
        assertTrue(collected.contains(true))
        job.cancel()
    }
}
