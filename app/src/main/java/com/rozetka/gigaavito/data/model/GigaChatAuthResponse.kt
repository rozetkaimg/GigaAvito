package com.rozetka.gigaavito.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GigaChatAuthResponse(
    val access_token: String,
    val expires_at: Long
)