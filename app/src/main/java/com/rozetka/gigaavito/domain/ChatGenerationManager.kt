package com.rozetka.gigaavito.domain

import com.rozetka.gigaavito.data.local.ChatDao
import com.rozetka.gigaavito.data.local.ImageDao
import com.rozetka.gigaavito.data.local.ImageEntity
import com.rozetka.gigaavito.data.local.MessageEntity
import com.rozetka.gigaavito.data.remote.GigaChatRepository
import com.rozetka.gigaavito.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class ChatGenerationManager(
    private val gigaChatRepository: GigaChatRepository,
    private val chatDao: ChatDao,
    private val imageDao: ImageDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val activeGenerations = ConcurrentHashMap<String, MutableStateFlow<String?>>()
    private val activeJobs = ConcurrentHashMap<String, Job>()

    private val _typingChats = MutableStateFlow<Set<String>>(emptySet())
    val typingChats: StateFlow<Set<String>> = _typingChats.asStateFlow()

    val isAnyChatGenerating: StateFlow<Boolean> = _typingChats
        .map { it.isNotEmpty() }
        .stateIn(scope, SharingStarted.Eagerly, false)

    fun getGeneratingFlow(chatId: String): StateFlow<String?> {
        return activeGenerations.getOrPut(chatId) { MutableStateFlow(null) }.asStateFlow()
    }

    fun isGenerating(chatId: String): Boolean {
        return activeJobs.containsKey(chatId)
    }

    fun startGeneration(
        chatId: String,
        text: String,
        imageBytes: ByteArray?,
        model: String,
        onError: (String) -> Unit,
        onComplete: () -> Unit
    ) {
        if (isGenerating(chatId)) return

        val generatingState = activeGenerations.getOrPut(chatId) { MutableStateFlow("") }
        generatingState.value = ""

        _typingChats.update { it + chatId }

        val job = scope.launch {
            try {
                withTimeoutOrNull(120_000L) {
                    val history = chatDao.getMessages(chatId).first()
                        .take(20).reversed().map {
                            GigaChatRequest.Message(role = if (it.isUser) "user" else "assistant", content = it.text)
                        }.toMutableList()

                    var attachmentIds: List<String>? = null
                    if (imageBytes != null) {
                        val uploadedId = gigaChatRepository.uploadFile(imageBytes, "img.jpg")
                        if (uploadedId != null) attachmentIds = listOf(uploadedId)
                    }

                    history.add(GigaChatRequest.Message(
                        role = "user",
                        content = text.ifBlank { "Сгенерируй изображение" },
                        attachments = attachmentIds
                    ))

                    var fullResponse = ""
                    gigaChatRepository.generateResponseStream(history, model)
                        .cancellable()
                        .collect { chunk ->
                            fullResponse += chunk
                            generatingState.value = fullResponse
                        }

                    if (fullResponse.isNotBlank()) {
                        chatDao.upsertMessage(
                            MessageEntity(UUID.randomUUID().toString(), chatId, fullResponse, false, System.currentTimeMillis())
                        )

                        val imgRegex = "<img src=\"([^\"]+)\"".toRegex()
                        imgRegex.find(fullResponse)?.groups?.get(1)?.value?.let { fileId ->
                            imageDao.insertImage(
                                ImageEntity(id = fileId, url = fileId, timestamp = System.currentTimeMillis(), senderId = "ai")
                            )
                        }
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (activeJobs.containsKey(chatId)) {
                    onError(text)
                }
            } finally {
                generatingState.value = null
                activeJobs.remove(chatId)
                _typingChats.update { it - chatId }
                withContext(Dispatchers.Main) { onComplete() }
            }
        }

        activeJobs[chatId] = job
    }

    fun cancelGeneration(chatId: String) {
        activeJobs[chatId]?.cancel()
        activeJobs.remove(chatId)
        activeGenerations[chatId]?.value = null
        _typingChats.update { it - chatId }
    }
}