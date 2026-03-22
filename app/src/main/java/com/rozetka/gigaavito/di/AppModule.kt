package com.rozetka.gigaavito.di

import android.annotation.SuppressLint
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.rozetka.gigaavito.data.local.AppDatabase
import com.rozetka.gigaavito.data.remote.GigaChatRepository
import com.rozetka.gigaavito.domain.ChatGenerationManager
import com.rozetka.gigaavito.screens.chat.ChatViewModel
import com.rozetka.gigaavito.screens.chatlist.ChatListViewModel
import com.rozetka.gigaavito.screens.images.ImagesViewModel
import com.rozetka.gigaavito.screens.login.LoginViewModel
import com.rozetka.gigaavito.screens.profile.ProfileViewModel
import com.rozetka.gigaavito.screens.register.RegisterViewModel
import com.rozetka.gigaavito.ui.theme.ThemeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

val appModule = module {
    single { FirebaseAuth.getInstance() }
    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "chat_database")
            .build()
    }
    single { get<AppDatabase>().imageDao() }
    single { get<AppDatabase>().chatDao() }

    single {
        HttpClient(Android) {
            install(SSE)
            engine {
                sslManager = { conn ->
                    val tm = arrayOf<X509TrustManager>(@SuppressLint("CustomX509TrustManager")
                    object : X509TrustManager {
                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkClientTrusted(
                            p0: Array<out X509Certificate>?,
                            p1: String?
                        ) {
                        }

                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkServerTrusted(
                            p0: Array<out X509Certificate>?,
                            p1: String?
                        ) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    })
                    val sslContext = SSLContext.getInstance("SSL")
                    sslContext.init(null, tm, SecureRandom())
                    conn.sslSocketFactory = sslContext.socketFactory
                    conn.hostnameVerifier = HostnameVerifier { _, _ -> true }
                }
            }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true })
            }
        }
    }

    single { GigaChatRepository(get()) }
    single { ChatGenerationManager(get(), get(), get()) }

    viewModelOf(::ProfileViewModel)
    single { ThemeViewModel(androidApplication()) }
    viewModelOf(::LoginViewModel)
    viewModelOf(::ChatViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ChatListViewModel)
    viewModelOf(::ImagesViewModel)
    viewModelOf(::ChatViewModel)

}