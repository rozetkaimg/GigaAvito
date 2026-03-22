package com.rozetka.gigaavito.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Forward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Precision
import coil3.size.Size


@Composable
fun ImagePage(
    path: Any,
    zoomState: ZoomState,
    rootState: DismissRootState,
    screenHeightPx: Float,
    dismissDistancePx: Float,
    dismissVelocityThreshold: Float,
    onDismiss: () -> Unit,
    showControls: Boolean,
    onToggleControls: () -> Unit
) {
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { offset ->
                        val currentScale = zoomState.scale.value
                        val targetScale = if (currentScale > 1.1f) 1f else 3f
                        zoomState.onDoubleTap(scope, offset, targetScale, size)
                    },
                    onTap = { onToggleControls() }
                )
            }
            .pointerInput(Unit) {
                detectZoomAndDismissGestures(
                    zoomState = zoomState,
                    rootState = rootState,
                    screenHeightPx = screenHeightPx,
                    dismissThreshold = dismissDistancePx,
                    dismissVelocityThreshold = dismissVelocityThreshold,
                    onDismiss = onDismiss,
                    scope = scope
                )
            }
    ) {
        ZoomableImage(
            data = path,
            zoomState = zoomState
        )
    }
}

@Composable
fun ImageOverlay(
    showControls: Boolean,
    rootState: DismissRootState,
    mediaData: Any,
    caption: String?,
    onDismiss: () -> Unit,
    showSettingsMenu: Boolean,
    onToggleSettings: () -> Unit,
    onForward: (Any) -> Unit,
    onDelete: ((Any) -> Unit)?,
    onCopyLink: ((Any) -> Unit)?,
    onCopyText: ((Any) -> Unit)?
) {
    AnimatedVisibility(
        visible = showControls && rootState.offsetY.value == 0f,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(Modifier.fillMaxSize()) {
            ViewerTopBar(
                onBack = onDismiss,
                onActionClick = onToggleSettings,
                isActionActive = showSettingsMenu,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            if (!caption.isNullOrBlank()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ViewerCaption(caption = caption)
                }
            }
        }
    }

    AnimatedVisibility(
        visible = showSettingsMenu && showControls,
        enter = fadeIn(tween(150)) + scaleIn(
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
            initialScale = 0.8f,
            transformOrigin = TransformOrigin(1f, 0f)
        ),
        exit = fadeOut(tween(150)) + scaleOut(
            animationSpec = tween(150),
            targetScale = 0.9f,
            transformOrigin = TransformOrigin(1f, 0f)
        ),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onToggleSettings()
                }
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 56.dp, end = 16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            ImageSettingsMenu(
                onCopyLink = if (onCopyLink != null) {
                    {
                        onCopyLink.invoke(mediaData)
                        onToggleSettings()
                    }
                } else null,
                onCopyText = if (!caption.isNullOrBlank() && onCopyText != null) {
                    {
                        onCopyText.invoke(mediaData)
                        onToggleSettings()
                    }
                } else null,
                onForward = {
                    onForward(mediaData)
                    onToggleSettings()
                },
                onDelete = if (onDelete != null) {
                    {
                        onDelete(mediaData)
                        onToggleSettings()
                    }
                } else null
            )
        }
    }
}

@Composable
fun ZoomableImage(
    data: Any,
    zoomState: ZoomState
) {
    val context = LocalContext.current
    var isHighResLoading by remember(data) { mutableStateOf(true) }

    val thumbnailRequest = remember(data) {
        ImageRequest.Builder(context)
            .data(data)
            .size(100, 100)
            .crossfade(true)
            .build()
    }

    val fullRequest = remember(data) {
        ImageRequest.Builder(context)
            .data(data)
            .size(Size.ORIGINAL)
            // Исправленный путь к EXACT
            .precision(Precision.EXACT)
            .crossfade(true)
            .build()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AsyncImage(
            model = thumbnailRequest,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = zoomState.offsetX.value
                    translationY = zoomState.offsetY.value
                    scaleX = zoomState.scale.value
                    scaleY = zoomState.scale.value
                }
        )

        AsyncImage(
            model = fullRequest,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = zoomState.offsetX.value
                    translationY = zoomState.offsetY.value
                    scaleX = zoomState.scale.value
                    scaleY = zoomState.scale.value
                },
            onState = { state ->
                isHighResLoading = state is AsyncImagePainter.State.Loading
            }
        )

        if (isHighResLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color.White,
                strokeWidth = 3.dp
            )
        }
    }
}


@Composable
fun ViewerTopBar(
    onBack: () -> Unit,
    onActionClick: () -> Unit,
    isActionActive: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                )
            )
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
        }
        IconButton(onClick = onActionClick) {
            Icon(Icons.Default.MoreVert, contentDescription = "Меню", tint = Color.White)
        }
    }
}

@Composable
fun ViewerCaption(caption: String) {
    Text(
        text = caption,
        color = Color.White,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
fun ViewerSettingsDropdown(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 8.dp,
        modifier = Modifier.width(200.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            content()
        }
    }
}

@Composable
fun MenuOptionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, color = textColor, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun ImageSettingsMenu(
    onCopyLink: (() -> Unit)?,
    onCopyText: (() -> Unit)? = null,
    onForward: () -> Unit,
    onDelete: (() -> Unit)?
) {
    ViewerSettingsDropdown {
        if (onCopyText != null) {
            MenuOptionRow(
                icon = Icons.Rounded.ContentCopy,
                title = "Копировать текст",
                onClick = onCopyText
            )
        }
        if (onCopyLink != null) {
            MenuOptionRow(
                icon = Icons.Rounded.Link,
                title = "Копировать ссылку",
                onClick = onCopyLink
            )
        }
        MenuOptionRow(
            icon = Icons.AutoMirrored.Rounded.Forward,
            title = "Отправить",
            onClick = onForward
        )
        if (onDelete != null) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            MenuOptionRow(
                icon = Icons.Rounded.Delete,
                title = "Удалить",
                onClick = onDelete,
                iconTint = MaterialTheme.colorScheme.error,
                textColor = MaterialTheme.colorScheme.error
            )
        }
    }
}