package com.rozetka.gigaavito.di

import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { FirebaseAuth.getInstance() }
    
    single {
        ImageLoader.Builder(androidContext())
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(androidContext(), 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(androidContext().cacheDir.resolve("image_cache"))
                    .build()
            }
            .components {
                add(OkHttpNetworkFetcherFactory())
            }
            .build()
    }
}
