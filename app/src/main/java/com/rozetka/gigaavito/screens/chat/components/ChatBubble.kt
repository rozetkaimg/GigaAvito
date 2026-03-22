package com.rozetka.gigaavito.screens.chat.components

import android.content.ClipData
import android.content.Intent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.rozetka.gigaavito.R
import com.rozetka.gigaavito.screens.chat.ChatViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ChatBubble(
    text: String,
    isUser: Boolean,
    timestamp: Long,
    fileId: String? = null,
    isGenerating: Boolean = false,
    viewModel: ChatViewModel? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    onImageClick: ((ByteArray?) -> Unit)? = null
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }
    var imageBytes by remember(fileId) { mutableStateOf<ByteArray?>(null) }

    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 4.dp, bottomEnd = 24.dp)
    }

    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    val isImageGenerating = isGenerating && text.contains("<img")
    val displayText = if (isImageGenerating) text.substringBefore("<img").trim() else text.trim()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val skeletonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    fun shareContent() {
        val intent = Intent(Intent.ACTION_SEND)
        var isImageAttached = false

        if (imageBytes != null) {
            try {
                val file = File(context.cacheDir, "shared_image_${System.currentTimeMillis()}.jpg")
                file.writeBytes(imageBytes!!)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                intent.type = "image/jpeg"
                intent.putExtra(Intent.EXTRA_STREAM, uri)
                intent.clipData = ClipData.newRawUri("", uri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                isImageAttached = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (!isImageAttached) {
            intent.type = "text/plain"
        }

        if (displayText.isNotBlank()) {
            intent.putExtra(Intent.EXTRA_TEXT, displayText)
        }

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.action_share)))
    }

    if (showMenu) {
        Dialog(onDismissRequest = { showMenu = false }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                modifier = Modifier.width(320.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.dialog_actions),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(displayText))
                            showMenu = false
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ContentCopy, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Text(stringResource(R.string.action_copy_text), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        onClick = {
                            shareContent()
                            showMenu = false
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = if (imageBytes != null) stringResource(R.string.action_share_all) else stringResource(R.string.action_share_text),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box {
            Column(
                modifier = Modifier.widthIn(max = 310.dp),
                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
            ) {
                Surface(
                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = bubbleShape,
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                if (!isGenerating && (displayText.isNotEmpty() || imageBytes != null)) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showMenu = true
                                }
                            }
                        )
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .animateContentSize(
                                animationSpec = spring(
                                    dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                                    stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                                )
                            )
                    ) {
                        if (displayText.isEmpty() && isGenerating && !isImageGenerating) {
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.status_processing), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.alpha(0.7f))
                            }
                        } else if (displayText.isNotEmpty()) {
                            Text(
                                text = displayText,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.alpha(if (isGenerating) 0.8f else 1f)
                            )
                        }

                        val hasFileId = fileId != null && viewModel != null

                        if (isImageGenerating || hasFileId) {
                            LaunchedEffect(fileId) {
                                if (hasFileId) {
                                    imageBytes = viewModel?.downloadImage(fileId!!, context)
                                }
                            }

                            val showSkeleton = isImageGenerating || (hasFileId && imageBytes == null)

                            if (showSkeleton) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = if (displayText.isNotEmpty()) 8.dp else 0.dp)
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = skeletonAlpha)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp).alpha(skeletonAlpha),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else if (imageBytes != null) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = if (displayText.isNotEmpty()) 8.dp else 0.dp)
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .then(
                                            if (sharedTransitionScope != null && animatedVisibilityScope != null && fileId != null) {
                                                with(sharedTransitionScope) {
                                                    Modifier.sharedBounds(
                                                        sharedContentState = rememberSharedContentState(key = "chat_img_$fileId"),
                                                        animatedVisibilityScope = animatedVisibilityScope,
                                                        boundsTransform = { _, _ -> tween(300, easing = FastOutSlowInEasing) },
                                                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                                                        clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(16.dp))
                                                    )
                                                }
                                            } else Modifier
                                        )
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { onImageClick?.invoke(imageBytes) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(imageBytes)
                                            .memoryCacheKey(fileId)
                                            .placeholderMemoryCacheKey(fileId)
                                            .crossfade(false)
                                            .build(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }

                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 4.dp)
                                .alpha(0.7f)
                        )
                    }
                }
            }
        }
    }
}