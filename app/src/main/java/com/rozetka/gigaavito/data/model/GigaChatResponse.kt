package com.rozetka.gigaavito.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GigaChatResponse(val choices: List<Choice>) {
    @Serializable data class Choice(val message: GigaChatRequest.Message)
}