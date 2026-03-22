package com.rozetka.gigaavito.data.model

import com.rozetka.gigaavito.data.remote.*
import kotlinx.serialization.Serializable

@Serializable
data class GigaChatModelsResponse(val data: List<GigaChatModel>)