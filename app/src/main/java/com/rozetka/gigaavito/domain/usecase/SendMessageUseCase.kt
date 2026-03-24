package com.rozetka.gigaavito.domain.usecase

import com.rozetka.gigaavito.data.local.ChatDao
import com.rozetka.gigaavito.data.local.ChatEntity
import com.rozetka.gigaavito.data.local.MessageEntity
import com.rozetka.gigaavito.domain.ChatGenerationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class SendMessageUseCase(
    private val chatDao: ChatDao,
    private val chatGenerationManager: ChatGenerationManager
) {
    suspend operator fun invoke(
        chatId: String,
        text: String,
        imageBytes: ByteArray?,
        model: String,
        onNewChatCreated: (String) -> Unit,
        onError: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        var workingId = chatId
        
        if (workingId == "new") {
            workingId = UUID.randomUUID().toString()
            val newChat = ChatEntity(
                id = workingId,
                title = text.take(30).ifBlank { "New Chat" },
                createdAt = System.currentTimeMillis()
            )
            chatDao.insertChat(newChat)
            onNewChatCreated(workingId)
        }

        chatDao.upsertMessage(
            MessageEntity(UUID.randomUUID().toString(), workingId, text, true, System.currentTimeMillis())
        )

        chatGenerationManager.startGeneration(
            chatId = workingId,
            text = text,
            imageBytes = imageBytes,
            model = model,
            onError = onError,
            onComplete = {}
        )
    }
}