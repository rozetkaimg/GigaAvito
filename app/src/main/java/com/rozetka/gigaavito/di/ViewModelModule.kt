package com.rozetka.gigaavito.di

import com.rozetka.gigaavito.screens.chat.ChatViewModel
import com.rozetka.gigaavito.screens.chatlist.ChatListViewModel
import com.rozetka.gigaavito.screens.images.ImagesViewModel
import com.rozetka.gigaavito.screens.login.LoginViewModel
import com.rozetka.gigaavito.screens.profile.ProfileViewModel
import com.rozetka.gigaavito.screens.register.RegisterViewModel
import com.rozetka.gigaavito.ui.theme.ThemeViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::ProfileViewModel)
    single { ThemeViewModel(androidApplication()) }
    viewModelOf(::LoginViewModel)
    viewModelOf(::ChatViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ChatListViewModel)
    viewModelOf(::ImagesViewModel)
}