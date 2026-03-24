package com.rozetka.gigaavito.screens.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rozetka.gigaavito.R
import com.rozetka.gigaavito.screens.login.LoginScreen
import com.rozetka.gigaavito.screens.register.RegisterScreen
import com.rozetka.gigaavito.screens.main.MainDrawerScaffold

@Composable
fun AppNavigation(
    startDestination: Any,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val animDuration = context.resources.getInteger(R.integer.nav_anim_duration)

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(animDuration))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(animDuration))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(animDuration))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(animDuration))
        }
    ) {
        composable<Screen.Login> {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.MainFlow) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register) }
            )
        }

        composable<Screen.Register> {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.MainFlow) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.MainFlow> {
            MainDrawerScaffold(
                onLogout = {
                    onLogout()
                    navController.navigate(Screen.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}