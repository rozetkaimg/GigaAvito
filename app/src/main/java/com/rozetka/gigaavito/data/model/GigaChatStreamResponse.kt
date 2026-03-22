package com.rozetka.gigaavito.data.model

import kotlinx.serialization.Serializable


@Serializable
data class GigaChatStreamResponse(val choices: List<StreamChoice>) {
    @Serializable data class StreamChoice(val delta: Delta)
    @Serializable data class Delta(val content: String? = null)
}