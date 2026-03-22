package com.rozetka.gigaavito.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)
private val LightColorScheme = lightColorScheme(primary = Purple40, secondary = PurpleGrey40, tertiary = Pink40)

@Composable
fun GigaAvitoTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean = true,
    animate: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val targetScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val duration = if (animate) 600 else 0
    val spec = tween<androidx.compose.ui.graphics.Color>(durationMillis = duration)

    val colorScheme = targetScheme.copy(
        primary = animateColorAsState(targetScheme.primary, spec).value,
        onPrimary = animateColorAsState(targetScheme.onPrimary, spec).value,
        primaryContainer = animateColorAsState(targetScheme.primaryContainer, spec).value,
        onPrimaryContainer = animateColorAsState(targetScheme.onPrimaryContainer, spec).value,
        secondary = animateColorAsState(targetScheme.secondary, spec).value,
        onSecondary = animateColorAsState(targetScheme.onSecondary, spec).value,
        secondaryContainer = animateColorAsState(targetScheme.secondaryContainer, spec).value,
        onSecondaryContainer = animateColorAsState(targetScheme.onSecondaryContainer, spec).value,
        tertiary = animateColorAsState(targetScheme.tertiary, spec).value,
        onTertiary = animateColorAsState(targetScheme.onTertiary, spec).value,
        tertiaryContainer = animateColorAsState(targetScheme.tertiaryContainer, spec).value,
        onTertiaryContainer = animateColorAsState(targetScheme.onTertiaryContainer, spec).value,
        background = animateColorAsState(targetScheme.background, spec).value,
        onBackground = animateColorAsState(targetScheme.onBackground, spec).value,
        surface = animateColorAsState(targetScheme.surface, spec).value,
        onSurface = animateColorAsState(targetScheme.onSurface, spec).value,
        surfaceVariant = animateColorAsState(targetScheme.surfaceVariant, spec).value,
        onSurfaceVariant = animateColorAsState(targetScheme.onSurfaceVariant, spec).value,
        error = animateColorAsState(targetScheme.error, spec).value,
        onError = animateColorAsState(targetScheme.onError, spec).value,
        errorContainer = animateColorAsState(targetScheme.errorContainer, spec).value,
        onErrorContainer = animateColorAsState(targetScheme.onErrorContainer, spec).value,
        outline = animateColorAsState(targetScheme.outline, spec).value,
        outlineVariant = animateColorAsState(targetScheme.outlineVariant, spec).value,
        scrim = animateColorAsState(targetScheme.scrim, spec).value,
        inverseSurface = animateColorAsState(targetScheme.inverseSurface, spec).value,
        inverseOnSurface = animateColorAsState(targetScheme.inverseOnSurface, spec).value,
        inversePrimary = animateColorAsState(targetScheme.inversePrimary, spec).value,
        surfaceTint = animateColorAsState(targetScheme.surfaceTint, spec).value
    )

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}