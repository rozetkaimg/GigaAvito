package com.rozetka.gigaavito.ui.theme

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val _isDarkTheme = MutableStateFlow(prefs.getBoolean("is_dark_theme", false))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    private val _animateNextTransition = MutableStateFlow(false)
    val animateNextTransition: StateFlow<Boolean> = _animateNextTransition

    fun setTheme(isDark: Boolean) {
        _animateNextTransition.value = true
        _isDarkTheme.value = isDark
        prefs.edit().putBoolean("is_dark_theme", isDark).apply()
    }

    fun onAnimationFinished() {
        _animateNextTransition.value = false
    }
}