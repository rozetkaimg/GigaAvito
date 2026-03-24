package com.rozetka.gigaavito.domain.usecase

import com.rozetka.gigaavito.data.model.NetworkResult
import com.rozetka.gigaavito.data.remote.GigaChatRepository

class GetModelsUseCase(private val repository: GigaChatRepository) {
    suspend operator fun invoke(): NetworkResult<List<String>> {
        return repository.getModels()
    }
}