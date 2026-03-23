package com.rozetka.gigaavito.screens.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.rozetka.gigaavito.R
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

    private val _tokensCount =
        MutableStateFlow(application.resources.getInteger(R.integer.default_tokens))
    val tokensCount: StateFlow<Int> = _tokensCount

    fun logout(onSuccess: () -> Unit) {
        auth.signOut()
        onSuccess()
    }

    fun updateProfile(newName: String, newPhotoUri: Uri? = null) {
        val user = auth.currentUser
        var finalUri = newPhotoUri
        val contentScheme = getApplication<Application>().getString(R.string.uri_scheme_content)

        if (newPhotoUri != null && newPhotoUri.toString().contains(contentScheme)) {
            finalUri = saveImageToCache(newPhotoUri)
        }

        val profileUpdates = UserProfileChangeRequest.Builder().apply {
            displayName = newName
            finalUri?.let { photoUri = it }
        }.build()

        user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userName.value = newName
                finalUri?.let { userPhotoUrl.value = it }
            }
        }
    }

    private fun saveImageToCache(uri: Uri): Uri? {
        return runCatching {
            val context = getApplication<Application>()
            val prefix = context.getString(R.string.avatar_prefix)
            val extension = context.getString(R.string.image_extension)
            val file = File(context.cacheDir, "$prefix${System.currentTimeMillis()}$extension")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file)
        }.getOrNull()
    }
}