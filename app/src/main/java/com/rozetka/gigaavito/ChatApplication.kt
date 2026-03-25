package com.rozetka.gigaavito

import android.app.Application
import android.content.Context
import coil3.ImageLoader
import coil3.SingletonImageLoader
import com.google.firebase.FirebaseApp
import com.rozetka.gigaavito.di.*
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ChatApplication : Application(), SingletonImageLoader.Factory {
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

    override fun newImageLoader(context: Context): ImageLoader = get()
}
