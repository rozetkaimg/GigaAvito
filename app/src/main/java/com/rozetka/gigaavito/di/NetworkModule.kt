package com.rozetka.gigaavito.di

import android.annotation.SuppressLint
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

val networkModule = module {
    single {
        HttpClient(Android) {
            install(SSE)
            engine {
                sslManager = { conn ->
                    val tm = arrayOf<X509TrustManager>(@SuppressLint("CustomX509TrustManager")
                    object : X509TrustManager {
                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkClientTrusted(p0: Array<out X509Certificate>?, p1: String?) {}
                        @SuppressLint("TrustAllX509TrustManager")
                        override fun checkServerTrusted(p0: Array<out X509Certificate>?, p1: String?) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                    })
                    val sslContext = SSLContext.getInstance("TLS")
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
}