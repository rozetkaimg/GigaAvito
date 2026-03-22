package com.rozetka.gigaavito.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query(
        """
        SELECT c.*, 
               m.text as lastMessage, 
               m.timestamp as lastMessageTimestamp
        FROM chats c
        LEFT JOIN (
            SELECT chatId, text, timestamp 
            FROM messages m1 
            WHERE timestamp = (SELECT MAX(timestamp) FROM messages m2 WHERE m2.chatId = m1.chatId)
        ) m ON c.id = m.chatId
        WHERE :query = '' 
           OR c.title LIKE '%' || :query || '%' 
           OR EXISTS (
               SELECT 1 FROM messages 
               WHERE messages.chatId = c.id 
               AND messages.text LIKE '%' || :query || '%'
           )
        ORDER BY c.isPinned DESC, COALESCE(m.timestamp, c.createdAt) DESC
        """
    )
    fun getPagedChats(query: String): PagingSource<Int, ChatWithLastMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)

    @Upsert
    suspend fun upsertMessage(message: MessageEntity)

    @Query("UPDATE chats SET isPinned = :isPinned WHERE id = :chatId")
    suspend fun updatePinStatus(chatId: String, isPinned: Boolean)

    @Query("UPDATE chats SET title = :newTitle WHERE id = :chatId")
    suspend fun renameChat(chatId: String, newTitle: String)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp DESC")
    fun getMessages(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM chats WHERE id = :chatId")
    fun getChatById(chatId: String): Flow<ChatEntity?>

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteMessagesByChatId(chatId: String)

    @Query("DELETE FROM chats WHERE id = :chatId")
    suspend fun deleteChatById(chatId: String)
}