package com.rozetka.gigaavito.di

import com.rozetka.gigaavito.data.remote.GigaChatRepository
import com.rozetka.gigaavito.domain.ChatGenerationManager
import org.koin.dsl.module

val repositoryModule = module {
    single { GigaChatRepository(get()) }
    single { ChatGenerationManager(get(), get(), get()) }
}