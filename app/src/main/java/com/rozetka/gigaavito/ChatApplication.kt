package com.rozetka.gigaavito

import android.app.Application
import com.google.firebase.FirebaseApp
import com.rozetka.gigaavito.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        startKoin {
            androidContext(this@ChatApplication)
            modules(
                networkModule,
                databaseModule,
                repositoryModule,
                useCaseModule,
                viewModelModule,
                appModule
            )
        }
    }
}