package com.rozetka.gigaavito.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaViewer(
    mediaData: Any,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val rootState = rememberDismissRootState()
    val zoomState = rememberZoomState()
    var showControls by remember { mutableStateOf(true) }

    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val screenHeightPx = with(density) { config.screenHeightDp.dp.toPx() }

    LaunchedEffect(Unit) {
        launch { rootState.scale.animateTo(1f, spring(0.8f, Spring.StiffnessMedium)) }
        launch { rootState.backgroundAlpha.animateTo(1f, tween(150)) }
    }

    BackHandler { onDismiss() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = rootState.backgroundAlpha.value))
            .graphicsLayer {
                translationY = rootState.offsetY.value
                scaleX = rootState.scale.value
                scaleY = rootState.scale.value
            }
    ) {
        ImagePage(
            path = mediaData,
            zoomState = zoomState,
            rootState = rootState,
            screenHeightPx = screenHeightPx,
            dismissDistancePx = 160.dp.value,
            dismissVelocityThreshold = 1000.dp.value,
            onDismiss = onDismiss,
            showControls = showControls,
            onToggleControls = { showControls = !showControls }
        )

        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { shareImageFromCache(context, mediaData, scope) }) {
                        Icon(Icons.Default.Share, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    }
}

private fun shareImageFromCache(context: Context, data: Any, scope: CoroutineScope) {
    scope.launch {
        val request = ImageRequest.Builder(context)
            .data(data)
            .build()

        val result = context.imageLoader.execute(request)
        if (result is SuccessResult) {
            val bitmap = result.image.toBitmap()
            val uri = withContext(Dispatchers.IO) {
                try {
                    val cachePath = File(context.cacheDir, "images")
                    cachePath.mkdirs()
                    val file = File(cachePath, "shared_image.png")
                    val stream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.close()
                    FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                } catch (e: Exception) { null }
            }

            uri?.let {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, it)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Image"))
            }
        }
    }
}