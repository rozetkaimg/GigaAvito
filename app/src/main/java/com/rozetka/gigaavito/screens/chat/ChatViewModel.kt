package com.rozetka.gigaavito.screens.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.gigaavito.data.local.*
import com.rozetka.gigaavito.data.remote.GigaChatRepository
import com.rozetka.gigaavito.domain.ChatGenerationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class ChatState(
    val models: List<String> = listOf("GigaChat"),
    val selectedModel: String = "GigaChat",
    val attachedImageUri: Uri? = null
)

class ChatViewModel(
    private val initialChatId: String,
    private val chatDao: ChatDao,
    private val gigaChatRepository: GigaChatRepository,
    private val chatGenerationManager: ChatGenerationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatState())
    val uiState = _uiState.asStateFlow()

    private val _currentChatId = MutableStateFlow(initialChatId)
    val currentChatId = _currentChatId.asStateFlow()

    private val imageCache = ConcurrentHashMap<String, ByteArray>()

    val isGenerating: StateFlow<Boolean> = chatGenerationManager.isAnyChatGenerating
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: Flow<List<MessageEntity>> = _currentChatId.flatMapLatest { id ->
        chatDao.getMessages(id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val chatInfo: Flow<ChatEntity?> = _currentChatId.flatMapLatest { id ->
        chatDao.getChatById(id)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val generatingMessage: StateFlow<String?> = _currentChatId.flatMapLatest { id ->
        chatGenerationManager.getGeneratingFlow(id)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _errorEvent = MutableSharedFlow<Pair<String, Uri?>>()
    val errorEvent = _errorEvent.asSharedFlow()

    init {
        loadModels()
    }

    private fun loadModels() {
        viewModelScope.launch {
            val availableModels = gigaChatRepository.getModels()
            if (availableModels.isNotEmpty()) {
                _uiState.update { it.copy(
                    models = availableModels,
                    selectedModel = availableModels.first()
                )}
            }
        }
    }

    fun selectModel(modelId: String) {
        _uiState.update { it.copy(selectedModel = modelId) }
    }

    fun attachImage(uri: Uri?) {
        _uiState.update { it.copy(attachedImageUri = uri) }
    }

    fun sendMessage(text: String, imageBytes: ByteArray? = null) {
        if (text.isBlank() && imageBytes == null) return
        if (chatGenerationManager.isAnyChatGenerating.value) return

        val model = _uiState.value.selectedModel
        val attachedUri = _uiState.value.attachedImageUri
        _uiState.update { it.copy(attachedImageUri = null) }

        viewModelScope.launch(Dispatchers.IO) {
            if (_currentChatId.value == "new") {
                val newId = UUID.randomUUID().toString()
                val newChat = ChatEntity(
                    id = newId,
                    title = text.take(30).ifBlank { "New Chat" },
                    createdAt = System.currentTimeMillis()
                )
                chatDao.insertChat(newChat)
                _currentChatId.value = newId
            }

            val workingId = _currentChatId.value
            chatDao.upsertMessage(
                MessageEntity(UUID.randomUUID().toString(), workingId, text, true, System.currentTimeMillis())
            )

            chatGenerationManager.startGeneration(
                chatId = workingId,
                text = text,
                imageBytes = imageBytes,
                model = model,
                onError = { errText ->
                    viewModelScope.launch(Dispatchers.IO) {
                        chatDao.upsertMessage(
                            MessageEntity(
                                id = UUID.randomUUID().toString(),
                                chatId = workingId,
                                text = "⚠️ Error: $errText",
                                isUser = false,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        _errorEvent.emit(Pair(text, attachedUri))
                    }
                },
                onComplete = { }
            )
        }
    }

    suspend fun downloadImage(fileId: String, context: Context): ByteArray? {
        val file = File(context.filesDir, "giga_$fileId.jpg")
        if (file.exists()) return file.readBytes()

        return gigaChatRepository.downloadImage(fileId)?.also {
            file.writeBytes(it)
            imageCache[fileId] = it
        }
    }

    fun renameChat(newTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.renameChat(_currentChatId.value, newTitle)
        }
    }
}