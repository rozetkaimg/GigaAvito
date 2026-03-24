package com.rozetka.gigaavito.screens.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozetka.gigaavito.data.local.*
import com.rozetka.gigaavito.domain.ChatGenerationManager
import com.rozetka.gigaavito.data.model.*
import com.rozetka.gigaavito.domain.usecase.DownloadImageUseCase
import com.rozetka.gigaavito.domain.usecase.GetModelsUseCase
import com.rozetka.gigaavito.domain.usecase.SendMessageUseCase
import com.rozetka.gigaavito.utils.getGigaImageFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class ChatViewState(
    val chatId: String = "",
    val chatInfo: ChatEntity? = null,
    val messages: List<MessageEntity> = emptyList(),
    val models: List<String> = listOf("GigaChat"),
    val selectedModel: String = "GigaChat",
    val attachedImageUri: Uri? = null,
    val generatingMessage: String? = null,
    val isGenerating: Boolean = false
)

class ChatViewModel(
    private val initialChatId: String,
    private val chatDao: ChatDao,
    private val chatGenerationManager: ChatGenerationManager,
    private val sendMessageUseCase: SendMessageUseCase,
    private val getModelsUseCase: GetModelsUseCase,
    private val downloadImageUseCase: DownloadImageUseCase
) : ViewModel() {

    private val _currentChatId = MutableStateFlow(initialChatId)
    private val _localUiState = MutableStateFlow(LocalUiState())

    private data class LocalUiState(
        val models: List<String> = listOf("GigaChat"),
        val selectedModel: String = "GigaChat",
        val attachedImageUri: Uri? = null
    )

    private val imageCache = ConcurrentHashMap<String, ByteArray>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val viewState: StateFlow<ChatViewState> = _currentChatId.flatMapLatest { id ->
        combine(
            chatDao.getMessages(id),
            chatDao.getChatById(id),
            chatGenerationManager.getGeneratingFlow(id),
            chatGenerationManager.isAnyChatGenerating,
            _localUiState
        ) { messages, info, generatingMsg, isGenerating, local ->
            ChatViewState(
                chatId = id,
                chatInfo = info,
                messages = messages,
                models = local.models,
                selectedModel = local.selectedModel,
                attachedImageUri = local.attachedImageUri,
                generatingMessage = generatingMsg,
                isGenerating = isGenerating
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChatViewState(chatId = initialChatId)
    )

    private val _errorEvent = MutableSharedFlow<Pair<String, Uri?>>()
    val errorEvent = _errorEvent.asSharedFlow()

    init {
        loadModels()
    }

    private fun loadModels() {
        viewModelScope.launch {
            when (val result = getModelsUseCase()) {
                is NetworkResult.Success -> {
                    val availableModels = result.data
                    if (availableModels.isNotEmpty()) {
                        _localUiState.update { it.copy(
                            models = availableModels,
                            selectedModel = availableModels.first()
                        )}
                    }
                }
                else -> {}
            }
        }
    }

    fun selectModel(modelId: String) {
        _localUiState.update { it.copy(selectedModel = modelId) }
    }

    fun attachImage(uri: Uri?) {
        _localUiState.update { it.copy(attachedImageUri = uri) }
    }

    fun sendMessageWithContext(text: String, uri: Uri?, context: Context) {
        val bytes = uri?.let { currentUri ->
            runCatching {
                context.contentResolver.openInputStream(currentUri)?.use { it.readBytes() }
            }.getOrNull()
        }
        sendMessage(text, bytes)
    }

    fun sendMessage(text: String, imageBytes: ByteArray? = null) {
        val currentState = viewState.value
        if ((text.isBlank() && imageBytes == null) || currentState.isGenerating) return

        val model = currentState.selectedModel
        val attachedUri = currentState.attachedImageUri
        _localUiState.update { it.copy(attachedImageUri = null) }

        viewModelScope.launch {
            sendMessageUseCase(
                chatId = _currentChatId.value,
                text = text,
                imageBytes = imageBytes,
                model = model,
                onNewChatCreated = { newId -> _currentChatId.value = newId },
                onError = { errText ->
                    viewModelScope.launch(Dispatchers.IO) {
                        chatDao.upsertMessage(
                            MessageEntity(
                                id = UUID.randomUUID().toString(),
                                chatId = _currentChatId.value,
                                text = "⚠️ Error: $errText",
                                isUser = false,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        _errorEvent.emit(Pair(text, attachedUri))
                    }
                }
            )
        }
    }

    suspend fun downloadImage(fileId: String, context: Context): ByteArray? {
        val file = getGigaImageFile(context, fileId)
        if (file.exists()) return file.readBytes()

        val result = downloadImageUseCase(fileId)
        return if (result is NetworkResult.Success) {
            val bytes = result.data
            file.writeBytes(bytes)
            imageCache[fileId] = bytes
            bytes
        } else {
            null
        }
    }

    fun renameChat(newTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatDao.renameChat(_currentChatId.value, newTitle)
        }
    }
}