package com.rozetka.gigaavito.di

import androidx.room.Room
import com.rozetka.gigaavito.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "chat_database")
            .build()
    }
    single { get<AppDatabase>().imageDao() }
    single { get<AppDatabase>().chatDao() }
}