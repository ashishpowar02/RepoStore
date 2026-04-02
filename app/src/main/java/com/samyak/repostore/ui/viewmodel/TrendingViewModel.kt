package com.samyak.repostore.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.samyak.repostore.data.model.AppCategory
import com.samyak.repostore.data.model.AppItem
import com.samyak.repostore.data.repository.GitHubRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TrendingUiState {
    data object Loading : TrendingUiState()
    data object Empty : TrendingUiState()
    data class LoadingMore(val currentApps: List<AppItem>) : TrendingUiState()
    data class Success(val apps: List<AppItem>) : TrendingUiState()
    data class Error(val message: String) : TrendingUiState()
}

class TrendingViewModel(private val repository: GitHubRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<TrendingUiState>(TrendingUiState.Loading)
    val uiState: StateFlow<TrendingUiState> = _uiState.asStateFlow()

    private var currentPage = 1
    private val loadedApps = mutableListOf<AppItem>()
    private var loadJob: Job? = null
    private var isLoadingMore = false

    init {
        loadTrendingApps(refresh = true)
    }

    private fun loadTrendingApps(refresh: Boolean = false) {
        if (refresh) {
            currentPage = 1
            loadedApps.clear()
            isLoadingMore = false
        }

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = if (loadedApps.isEmpty()) {
                TrendingUiState.Loading
            } else {
                TrendingUiState.LoadingMore(loadedApps.toList())
            }

            val result = repository.getAppsByCategory(AppCategory.TRENDING, currentPage)
            result.fold(
                onSuccess = { apps ->
                    val sortedApps = apps.sortedByDescending { it.repo.stars }
                    
                    if (refresh || currentPage == 1) {
                        loadedApps.clear()
                    }
                    loadedApps.addAll(sortedApps)

                    _uiState.value = if (loadedApps.isEmpty()) {
                        TrendingUiState.Empty
                    } else {
                        TrendingUiState.Success(loadedApps.toList())
                    }
                    isLoadingMore = false
                },
                onFailure = { error ->
                    _uiState.value = if (loadedApps.isEmpty()) {
                        TrendingUiState.Error(error.message ?: "Failed to load apps")
                    } else {
                        TrendingUiState.Success(loadedApps.toList())
                    }
                    isLoadingMore = false
                }
            )
        }
    }

    fun loadMore() {
        if (isLoadingMore) return
        if (_uiState.value is TrendingUiState.Loading) return

        isLoadingMore = true
        currentPage++

        viewModelScope.launch {
            _uiState.value = TrendingUiState.LoadingMore(loadedApps.toList())

            val result = repository.getAppsByCategory(AppCategory.TRENDING, currentPage)

            result.fold(
                onSuccess = { apps ->
                    val sortedApps = apps.sortedByDescending { it.repo.stars }
                    val existingIds = loadedApps.map { it.repo.id }.toSet()
                    val newApps = sortedApps.filter { it.repo.id !in existingIds }
                    loadedApps.addAll(newApps)
                    _uiState.value = TrendingUiState.Success(loadedApps.toList())
                },
                onFailure = {
                    // Revert page increment on failure
                    currentPage--
                    _uiState.value = TrendingUiState.Success(loadedApps.toList())
                }
            )
            isLoadingMore = false
        }
    }

    fun refresh() {
        loadTrendingApps(refresh = true)
    }

    fun retry() {
        loadTrendingApps(refresh = true)
    }
}

class TrendingViewModelFactory(private val repository: GitHubRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrendingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrendingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
