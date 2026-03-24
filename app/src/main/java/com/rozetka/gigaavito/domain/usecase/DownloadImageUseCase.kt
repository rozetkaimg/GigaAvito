package com.rozetka.gigaavito.domain.usecase

import com.rozetka.gigaavito.data.model.NetworkResult
import com.rozetka.gigaavito.data.remote.GigaChatRepository

class DownloadImageUseCase(private val repository: GigaChatRepository) {
    suspend operator fun invoke(fileId: String): NetworkResult<ByteArray> {
        return repository.downloadImage(fileId)
    }
}