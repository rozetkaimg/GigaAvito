package com.rozetka.gigaavito

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth
import com.rozetka.gigaavito.screens.navigation.AppNavigation
import com.rozetka.gigaavito.screens.navigation.Screen
import com.rozetka.gigaavito.ui.theme.GigaAvitoTheme
import com.rozetka.gigaavito.ui.theme.ThemeViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val auth: FirebaseAuth by inject()
    private val themeViewModel: ThemeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startDestination: Screen = if (auth.currentUser != null) Screen.MainFlow else Screen.Login

        setContent {
            val isDarkTheme by themeViewModel.isDarkTheme.collectAsState()
            val animate by themeViewModel.animateNextTransition.collectAsState()

            GigaAvitoTheme(darkTheme = isDarkTheme, animate = animate) {
                AppNavigation(
                    startDestination = startDestination,
                    onLogout = { auth.signOut() }
                )

                LaunchedEffect(isDarkTheme) {
                    if (animate) {
                        kotlinx.coroutines.delay(700)
                        themeViewModel.onAnimationFinished()
                    }
                }
            }
        }
    }
}