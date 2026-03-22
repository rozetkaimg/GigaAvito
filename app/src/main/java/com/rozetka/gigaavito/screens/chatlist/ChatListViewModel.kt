package com.rozetka.gigaavito.screens.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.rozetka.gigaavito.data.local.ChatDao
import com.rozetka.gigaavito.data.local.ChatEntity
import com.rozetka.gigaavito.data.local.ChatWithLastMessage
import com.rozetka.gigaavito.domain.ChatGenerationManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.UUID

class ChatListViewModel(
    private val chatDao: ChatDao,
    private val chatGenerationManager: ChatGenerationManager
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    val typingChats = chatGenerationManager.typingChats

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val chats: Flow<PagingData<ChatWithLastMessage>> = _searchQuery
        .debounce(400)
        .flatMapLatest { query ->
            Pager(
                config = PagingConfig(pageSize = 20, enablePlaceholders = false),
                pagingSourceFactory = { chatDao.getPagedChats(query.trim()) }
            ).flow.cachedIn(viewModelScope)
        }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun createNewChat(title: String, onCreated: (String) -> Unit) {
        val newId = UUID.randomUUID().toString()
        viewModelScope.launch {
            chatDao.insertChat(
                ChatEntity(
                    id = newId,
                    title = title,
                    createdAt = System.currentTimeMillis(),
                    isPinned = false
                )
            )
            onCreated(newId)
        }
    }

    fun togglePinChat(chatId: String, currentPinStatus: Boolean) {
        viewModelScope.launch {
            chatDao.updatePinStatus(chatId, !currentPinStatus)
        }
    }
}