package com.rozetka.gigaavito.screens.images.componets

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.rozetka.gigaavito.utils.getGigaImageFile

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
fun ImageCard(
    imageUrl: String,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: (ByteArray?) -> Unit,
    onLongClick: () -> Unit
) {
    var isLoaded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val localFile = remember(imageUrl) { getGigaImageFile(context, imageUrl) }

    with(sharedTransitionScope) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier
                .fillMaxWidth()
                .sharedElement(
                    rememberSharedContentState(key = "img_$imageUrl"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
                .combinedClickable(
                    onClick = {
                        val bytes = if (localFile.exists()) localFile.readBytes() else null
                        onClick(bytes)
                    },
                    onLongClick = onLongClick
                )
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(localFile)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onSuccess = { isLoaded = true },
                modifier = Modifier.fillMaxWidth().wrapContentHeight().animateContentSize()
            )
            if (!isLoaded) {
                Box(Modifier.fillMaxWidth().height(180.dp).background(MaterialTheme.colorScheme.surfaceVariant))
            }
        }
    }
}