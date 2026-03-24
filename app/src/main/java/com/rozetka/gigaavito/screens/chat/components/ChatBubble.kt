package com.rozetka.gigaavito.screens.chat.components

import android.content.ClipData
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.rozetka.gigaavito.R
import com.rozetka.gigaavito.screens.chat.ChatViewModel
import com.rozetka.gigaavito.utils.shareMedia
import kotlinx.coroutines.launch
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
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboard.current
    val scope = rememberCoroutineScope()

    var showMenu by remember { mutableStateOf(false) }
    var imageBytes by remember(fileId) { mutableStateOf<ByteArray?>(null) }

    val timeStr = remember(timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    }

    val isImageGenerating = remember(text, isGenerating) { isGenerating && text.contains("<img") }
    val displayText = remember(text, isImageGenerating) {
        if (isImageGenerating) text.substringBefore("<img").trim() else text.trim()
    }

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

    if (showMenu) {
        MessageActionsDialog(
            isImage = imageBytes != null,
            onDismiss = { showMenu = false },
            onCopy = {
                scope.launch {
                    val clipData = ClipData.newPlainText("Copied Text", displayText)
                    clipboardManager.setClipEntry(clipData.toClipEntry())
                    showMenu = false
                }
            },
            onShare = {
                context.shareMedia(displayText, imageBytes)
                showMenu = false
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            shape = getBubbleShape(isUser),
            modifier = Modifier
                .widthIn(max = 310.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = {
                        if (!isGenerating && (displayText.isNotEmpty() || imageBytes != null)) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showMenu = true
                        }
                    })
                }
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .animateContentSize(spring(stiffness = Spring.StiffnessLow))
            ) {
                if (displayText.isEmpty() && isGenerating && !isImageGenerating) {
                    LoadingStatusIndicator()
                } else if (displayText.isNotEmpty()) {
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.alpha(if (isGenerating) 0.8f else 1f)
                    )
                }

                if (isImageGenerating || (fileId != null && viewModel != null)) {
                    LaunchedEffect(fileId) {
                        if (fileId != null && imageBytes == null) {
                            imageBytes = viewModel?.downloadImage(fileId, context)
                        }
                    }

                    ImageContent(
                        imageBytes = imageBytes,
                        fileId = fileId,
                        showSkeleton = isImageGenerating || (fileId != null && imageBytes == null),
                        skeletonAlpha = skeletonAlpha,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onImageClick = onImageClick
                    )
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

@Composable
private fun MessageActionsDialog(
    isImage: Boolean,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
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

                ActionItem(Icons.Default.ContentCopy, stringResource(R.string.action_copy_text), onCopy)
                Spacer(modifier = Modifier.height(8.dp))
                ActionItem(
                    icon = Icons.Default.Share,
                    label = if (isImage) stringResource(R.string.action_share_all) else stringResource(R.string.action_share_text),
                    onClick = onShare
                )
            }
        }
    }
}

@Composable
private fun ActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ImageContent(
    imageBytes: ByteArray?,
    fileId: String?,
    showSkeleton: Boolean,
    skeletonAlpha: Float,
    sharedTransitionScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    onImageClick: ((ByteArray?) -> Unit)?
) {
    val modifier = Modifier
        .padding(top = 8.dp)
        .fillMaxWidth()
        .height(220.dp)
        .clip(RoundedCornerShape(16.dp))

    if (showSkeleton) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = skeletonAlpha)),
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
            modifier = modifier
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
                .clickable { onImageClick?.invoke(imageBytes) },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageBytes)
                    .memoryCacheKey(fileId)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun LoadingStatusIndicator() {
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
        Text(
            text = stringResource(R.string.status_processing),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.alpha(0.7f)
        )
    }
}

private fun getBubbleShape(isUser: Boolean) = RoundedCornerShape(
    topStart = 24.dp,
    topEnd = 24.dp,
    bottomStart = if (isUser) 24.dp else 4.dp,
    bottomEnd = if (isUser) 4.dp else 24.dp
)