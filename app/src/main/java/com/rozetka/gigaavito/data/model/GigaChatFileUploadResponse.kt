package com.rozetka.gigaavito.data.model

import kotlinx.serialization.Serializable

@Serializable
data class GigaChatFileUploadResponse(
    val id: String,
    val object_type: String? = null,
    val bytes: Long? = null,
    val created_at: Long? = null,
    val filename: String? = null,
    val purpose: String? = null
)