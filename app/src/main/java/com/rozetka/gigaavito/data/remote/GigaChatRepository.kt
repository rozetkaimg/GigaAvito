package com.rozetka.gigaavito.data.remote

import com.rozetka.gigaavito.BuildConfig
import com.rozetka.gigaavito.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.serialization.json.Json
import java.util.UUID

class GigaChatRepository(private val client: HttpClient) {

    private val authKey = BuildConfig.GIGACHAT_AUTH_KEY
    private val baseUrl = BuildConfig.GIGACHAT_BASE_URL
    private val authUrl = BuildConfig.GIGACHAT_AUTH_URL

    private var currentToken: String? = null
    private var tokenExpiresAt: Long = 0
    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    private suspend fun getAccessToken(): String {
        if (currentToken != null && System.currentTimeMillis() + 60000 < tokenExpiresAt) {
            return currentToken!!
        }

        val response: GigaChatAuthResponse = client.post(authUrl) {
            header(HttpHeaders.Authorization, "Basic $authKey")
            header("RqUID", UUID.randomUUID().toString())
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("scope=GIGACHAT_API_PERS")
        }.body()

        currentToken = response.access_token
        tokenExpiresAt = response.expires_at
        return response.access_token
    }

    suspend fun getModels(): List<String> {
        return try {
            val validToken = getAccessToken()
            val response: GigaChatModelsResponse = client.get("${baseUrl}models") {
                header(HttpHeaders.Authorization, "Bearer $validToken")
            }.body()
            response.data.map { it.id }
        } catch (e: Exception) {
            listOf("GigaChat")
        }
    }

    suspend fun uploadFile(fileBytes: ByteArray, fileName: String): String? {
        return try {
            val validToken = getAccessToken()
            val response: GigaChatFileUploadResponse = client.post("${baseUrl}files") {
                header(HttpHeaders.Authorization, "Bearer $validToken")
                setBody(
                    MultiPartFormDataContent(
                        formData {
                            append("purpose", "general")
                            append("file", fileBytes, Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            })
                        }
                    )
                )
            }.body()
            response.id
        } catch (e: Exception) {
            null
        }
    }

    fun generateResponseStream(
        messages: List<GigaChatRequest.Message>,
        model: String
    ): Flow<String> = flow {
        val validToken = getAccessToken()

        client.sse(
            urlString = "${baseUrl}chat/completions",
            request = {
                method = HttpMethod.Post
                header(HttpHeaders.Authorization, "Bearer $validToken")
                contentType(ContentType.Application.Json)
                setBody(GigaChatRequest(
                    model = model,
                    messages = messages,
                    stream = true,
                    function_call = "auto"
                ))
            }
        ) {
            incoming.takeWhile { it.data != "[DONE]" }.collect { event ->
                val data = event.data
                if (data != null) {
                    try {
                        val response = jsonParser.decodeFromString<GigaChatStreamResponse>(data)
                        response.choices.firstOrNull()?.delta?.content?.let { emit(it) }
                    } catch (e: Exception) {
                    }
                }
            }
        }
    }

    suspend fun downloadImage(fileId: String): ByteArray? {
        return try {
            val validToken = getAccessToken()
            client.get("${baseUrl}files/$fileId/content") {
                header(HttpHeaders.Authorization, "Bearer $validToken")
                header(HttpHeaders.Accept, "application/jpg")
            }.body<ByteArray>()
        } catch (e: Exception) {
            null
        }
    }
}