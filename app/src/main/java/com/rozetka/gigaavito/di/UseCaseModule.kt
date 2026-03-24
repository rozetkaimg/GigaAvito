package com.rozetka.gigaavito.di

import com.rozetka.gigaavito.domain.usecase.DownloadImageUseCase
import com.rozetka.gigaavito.domain.usecase.GetModelsUseCase
import com.rozetka.gigaavito.domain.usecase.SendMessageUseCase
import org.koin.dsl.module

val useCaseModule = module {
    single { SendMessageUseCase(get(), get()) }
    single { GetModelsUseCase(get()) }
    single { DownloadImageUseCase(get()) }
}