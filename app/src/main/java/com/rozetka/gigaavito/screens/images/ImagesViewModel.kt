package com.rozetka.gigaavito.screens.images

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.gigaavito.data.local.AppDatabase
import com.rozetka.gigaavito.data.model.MediaItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ImagesState(
    val images: List<MediaItem> = emptyList(),
    val isLoading: Boolean = false,
    val selectedImage: Pair<MediaItem, ByteArray?>? = null
)

class ImagesViewModel(private val database: AppDatabase) : ViewModel() {

    private val _state = MutableStateFlow(ImagesState())
    val state: StateFlow<ImagesState> = _state.asStateFlow()

    init {
        loadImages()
    }

    private fun loadImages() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            database.imageDao().getAllImages().collectLatest { entities ->
                val mediaItems = entities.map { MediaItem(url = it.url) }
                _state.update { it.copy(images = mediaItems, isLoading = false) }
            }
        }
    }

    fun refreshImages() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(1000)
            loadImages()
        }
    }

    fun onImageClick(item: MediaItem, bytes: ByteArray?) {
        _state.update { it.copy(selectedImage = item to bytes) }
    }

    fun onCloseViewer() {
        _state.update { it.copy(selectedImage = null) }
    }
}