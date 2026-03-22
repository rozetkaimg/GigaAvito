package com.rozetka.gigaavito.data.local


import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import com.rozetka.gigaavito.data.model.MediaItem

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
    val isPinned: Boolean = false
)

@Fts4(contentEntity = ChatEntity::class)
@Entity(tableName = "chats_fts")
data class ChatFtsEntity(
    val title: String
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long
)

data class ChatWithLastMessage(
    @Embedded val chat: ChatEntity,
    val lastMessage: String?,
    val lastMessageTimestamp: Long?
)

@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey val id: String,
    val url: String,
    val timestamp: Long,
    val senderId: String
) {
    fun toMediaItem() = MediaItem(url)
}