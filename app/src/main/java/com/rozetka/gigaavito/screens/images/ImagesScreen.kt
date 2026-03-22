package com.rozetka.gigaavito.screens.images

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rozetka.gigaavito.R
import com.rozetka.gigaavito.screens.images.componets.ImageCard
import com.rozetka.gigaavito.utils.MediaViewer
import com.rozetka.gigaavito.utils.shareImage
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ImagesScreen(
    viewModel: ImagesViewModel = koinViewModel(),
    onMenuClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val pullToRefreshState = rememberPullToRefreshState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    SharedTransitionLayout {
        AnimatedContent(
            targetState = state.selectedImage,
            label = "MediaTransition"
        ) { targetPair ->
            if (targetPair != null) {
                MediaViewer(
                    mediaData = targetPair.second ?: targetPair.first.url,
                    onDismiss = { viewModel.onCloseViewer() },
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "img_${targetPair.first.url}"),
                        animatedVisibilityScope = this@AnimatedContent
                    )
                )
            } else {
                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.menu_images), fontWeight = FontWeight.Bold) },
                            navigationIcon = {
                                IconButton(onClick = onMenuClick) {
                                    Icon(Icons.Default.Menu, contentDescription = null)
                                }
                            },
                            scrollBehavior = scrollBehavior
                        )
                    }
                ) { padding ->
                    PullToRefreshBox(
                        isRefreshing = state.isLoading,
                        onRefresh = { viewModel.refreshImages() },
                        state = pullToRefreshState,
                        modifier = Modifier.fillMaxSize().padding(padding)
                    ) {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            verticalItemSpacing = 12.dp,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.images, key = { it.url }) { item ->
                                ImageCard(
                                    imageUrl = item.url,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this@AnimatedContent,
                                    onClick = { bytes ->
                                        viewModel.onImageClick(item, bytes)
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        shareImage(context, item.url)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}