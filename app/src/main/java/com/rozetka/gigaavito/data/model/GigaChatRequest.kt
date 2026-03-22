package com.rozetka.gigaavito.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GigaChatRequest(
    val model: String,
    val messages: List<Message>,
    val stream: Boolean = false,
    val function_call: String
) {
    @Serializable
    data class Message(
        val role: String,
        val content: String,
        val attachments: List<String>? = null
    )
}