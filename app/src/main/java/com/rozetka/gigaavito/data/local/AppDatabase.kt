package com.rozetka.gigaavito.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ChatEntity::class,
        ChatFtsEntity::class,
        MessageEntity::class,
        ImageEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun imageDao(): ImageDao
}