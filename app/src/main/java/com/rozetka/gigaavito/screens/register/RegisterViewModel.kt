package com.rozetka.gigaavito.screens.register

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.rozetka.gigaavito.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RegisterState(
    val isLoading: Boolean = false,
    val errorResId: Int? = null,
    val rawErrorMessage: String? = null,
    val isSuccess: Boolean = false
)

class RegisterViewModel(private val auth: FirebaseAuth) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")

    fun register() {
        if (email.value.isBlank() || password.value.isBlank() || confirmPassword.value.isBlank()) {
            _state.value = _state.value.copy(errorResId = R.string.error_fill_fields)
            return
        }

        if (password.value != confirmPassword.value) {
            _state.value = _state.value.copy(errorResId = R.string.error_pass_mismatch)
            return
        }

        if (password.value.length < 6) {
            _state.value = _state.value.copy(errorResId = R.string.error_pass_short)
            return
        }

        _state.value = _state.value.copy(isLoading = true, errorResId = null, rawErrorMessage = null)

        auth.createUserWithEmailAndPassword(email.value.trim(), password.value)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        rawErrorMessage = task.exception?.localizedMessage
                    )
                }
            }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorResId = null, rawErrorMessage = null)
    }
}