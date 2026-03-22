package com.rozetka.gigaavito.screens.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream

class ProfileViewModel(
    application: Application,
    private val auth: FirebaseAuth
) : AndroidViewModel(application) {

    val userName = MutableStateFlow(auth.currentUser?.displayName)
    val userEmail = MutableStateFlow(auth.currentUser?.email)
    val userPhone = MutableStateFlow(auth.currentUser?.phoneNumber)
    val userPhotoUrl = MutableStateFlow(auth.currentUser?.photoUrl)

    private val _tokensCount = MutableStateFlow(1250)
    val tokensCount: StateFlow<Int> = _tokensCount

    fun logout(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }

    fun updateProfile(newName: String, newPhotoUri: Uri? = null) {
        val user = auth.currentUser
        var finalUri = newPhotoUri

        if (newPhotoUri != null && newPhotoUri.toString().contains("content://")) {
            finalUri = saveImageToCache(newPhotoUri)
        }

        val profileUpdates = UserProfileChangeRequest.Builder().apply {
            setDisplayName(newName)
            finalUri?.let { setPhotoUri(it) }
        }.build()

        user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userName.value = newName
                finalUri?.let { userPhotoUrl.value = it }
            }
        }
    }

    private fun saveImageToCache(uri: Uri): Uri? {
        return try {
            val context = getApplication<Application>()
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }
}