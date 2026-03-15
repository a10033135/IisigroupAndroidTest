package idv.fan.iisigroup.android.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import idv.fan.iisigroup.android.test.domain.model.Post
import idv.fan.iisigroup.android.test.domain.usecase.GetPostsUseCase
import idv.fan.iisigroup.android.test.network.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getPostsUseCase: GetPostsUseCase,
) : ViewModel() {

    private val _currentDestination = MutableStateFlow(AppDestinations.HOME)
    val currentDestination: StateFlow<AppDestinations> = _currentDestination.asStateFlow()

    private val _postsState = MutableStateFlow<ApiResult<List<Post>>?>(null)
    val postsState: StateFlow<ApiResult<List<Post>>?> = _postsState.asStateFlow()

    fun navigateTo(destination: AppDestinations) {
        _currentDestination.value = destination
    }

    fun fetchPosts() {
        viewModelScope.launch(Dispatchers.IO) {
            _postsState.value = getPostsUseCase()
        }
    }
}
