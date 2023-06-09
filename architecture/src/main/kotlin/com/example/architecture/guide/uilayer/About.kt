package com.example.architecture.guide.uilayer.about

import android.os.Bundle
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

class Message

// [START android_arch_ui_about_state_classes]
data class NewsUiState(
    val isSignedIn: Boolean = false,
    val isPremium: Boolean = false,
    val newsItems: List<NewsItemUiState> = listOf(),
    val userMessages: List<Message> = listOf(),
    // [START_EXCLUDE]
    val isFetchingArticles: Boolean = false,
    // [END_EXCLUDE]
)

data class NewsItemUiState(
    val title: String,
    val body: String,
    val bookmarked: Boolean = false,
    // ...
)
// [END android_arch_ui_about_state_classes]

object StateExtension {
    // [START android_arch_ui_about_state_extension]
    data class NewsUiState(
        val isSignedIn: Boolean = false,
        val isPremium: Boolean = false,
        val newsItems: List<NewsItemUiState> = listOf()
    )

    val NewsUiState.canBookmarkNews: Boolean get() = isSignedIn && isPremium
    // [END android_arch_ui_about_state_extension]
}

object Loading {
    // [START android_arch_ui_about_boolean_loading]
    data class NewsUiState(
        val isFetchingArticles: Boolean = false,
        // ...
    )
    // [END android_arch_ui_about_boolean_loading]
}

object ErrorMessages {
    // [START android_arch_ui_about_compose_error_messages]
    data class Message(val id: Long, val message: String)

    data class NewsUiState(
        val userMessages: List<Message> = listOf(),
        // ...
    )
    // [END android_arch_ui_about_compose_error_messages]
}

object Compose {

    object VM1 {
        // [START android_arch_ui_about_compose_vm1]
        class NewsViewModel(/* ... */) : ViewModel() {
            val uiState: NewsUiState = TODO("Produce state")
        }
        // [END android_arch_ui_about_compose_vm1]
    }

    object VM2 {
        // [START android_arch_ui_about_compose_vm2]
        class NewsViewModel(/* ... */) : ViewModel() {
            var uiState by mutableStateOf(NewsUiState())
                private set
        }
        // [END android_arch_ui_about_compose_vm2]
    }

    object VM3 {
        object NewsRepository {
            fun newsItemsForCategory(category: String): List<NewsItemUiState> = TODO()
        }

        fun getMessagesFromThrowable(ioe: IOException): List<Message> = TODO()

        // [START android_arch_ui_about_compose_vm3]
        class NewsViewModel(
            private val repository: NewsRepository,
            // ...
        ) : ViewModel() {

            var uiState by mutableStateOf(NewsUiState())
                private set

            private var fetchJob: Job? = null

            fun fetchArticles(category: String) {
                fetchJob?.cancel()
                fetchJob = viewModelScope.launch {
                    try {
                        val newsItems = NewsRepository.newsItemsForCategory(category)
                        uiState = uiState.copy(newsItems = newsItems)
                    } catch (ioe: IOException) {
                        // Handle the error and notify the UI when appropriate.
                        val messages = getMessagesFromThrowable(ioe)
                        uiState = uiState.copy(userMessages = messages)
                    }
                }
            }
        }
        // [END android_arch_ui_about_compose_vm3]

        object Screen1 {
            // [START android_arch_ui_about_compose_screen1]
            @Composable
            fun LatestNewsScreen(
                viewModel: NewsViewModel = viewModel()
            ) {
                // Show UI elements based on the viewModel.uiState
            }
            // [END android_arch_ui_about_compose_screen1]
        }

        object Screen2 {
            // [START android_arch_ui_about_compose_screen2]
            @Composable
            fun LatestNewsScreen(
                modifier: Modifier = Modifier,
                viewModel: NewsViewModel = viewModel()
            ) {
                Box(modifier.fillMaxSize()) {

                    if (viewModel.uiState.isFetchingArticles) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }

                    // Add other UI elements. For example, the list.
                }
            }
            // [END android_arch_ui_about_compose_screen2]
        }
    }
}

object Views {

    object VM1 {
        // [START android_arch_ui_about_views_vm1]
        class NewsViewModel(/* ... */) : ViewModel() {
            val uiState: StateFlow<NewsUiState> = TODO("Produce state")
        }
        // [END android_arch_ui_about_views_vm1]
    }

    object VM2 {
        // [START android_arch_ui_about_views_vm2]
        class NewsViewModel(/* ... */) : ViewModel() {
            private val _uiState = MutableStateFlow(NewsUiState())
            val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()
        }
        // [END android_arch_ui_about_views_vm2]
    }

    object VM3 {
        object NewsRepository {
            fun newsItemsForCategory(category: String): List<NewsItemUiState> = TODO()
        }

        fun getMessagesFromThrowable(ioe: IOException): List<Message> = TODO()

        // [START android_arch_ui_about_views_vm3]
        class NewsViewModel(
            private val repository: NewsRepository,
            // ...
        ) : ViewModel() {

            private val _uiState = MutableStateFlow(NewsUiState())
            val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

            private var fetchJob: Job? = null

            fun fetchArticles(category: String) {
                fetchJob?.cancel()
                fetchJob = viewModelScope.launch {
                    try {
                        val newsItems = repository.newsItemsForCategory(category)
                        _uiState.update {
                            it.copy(newsItems = newsItems)
                        }
                    } catch (ioe: IOException) {
                        // Handle the error and notify the UI when appropriate.
                        _uiState.update {
                            val messages = getMessagesFromThrowable(ioe)
                            it.copy(userMessages = messages)
                        }
                    }
                }
            }
        }
        // [END android_arch_ui_about_views_vm3]

        object Screen1 {
            // [START android_arch_ui_about_views_screen1]
            class NewsActivity : AppCompatActivity() {

                private val viewModel: NewsViewModel by viewModels()

                override fun onCreate(savedInstanceState: Bundle?) {
                    // [START_EXCLUDE]
                    super.onCreate(savedInstanceState)
                    // [END_EXCLUDE]

                    lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.STARTED) {
                            viewModel.uiState.collect {
                                // Update UI elements
                            }
                        }
                    }
                }
            }
            // [END android_arch_ui_about_views_screen1]
        }

        object Screen2 {
            // [START android_arch_ui_about_views_screen2]
            class NewsActivity : AppCompatActivity() {
                // [START_EXCLUDE silent]
                lateinit var progressBar: ProgressBar
                // [END_EXCLUDE]

                private val viewModel: NewsViewModel by viewModels()

                override fun onCreate(savedInstanceState: Bundle?) {
                    // [START_EXCLUDE]
                    super.onCreate(savedInstanceState)
                    // [END_EXCLUDE]

                    // STOPSHIP remove this line
                    lifecycleScope.launch {
                        repeatOnLifecycle(Lifecycle.State.STARTED) {
                            // Bind the visibility of the progressBar to the state
                            // of isFetchingArticles.
                            viewModel.uiState
                                .map { it.isFetchingArticles }
                                .distinctUntilChanged()
                                .collect { progressBar.isVisible = it }
                        }
                    }
                }
            }
            // [END android_arch_ui_about_views_screen2]
        }
    }
}
