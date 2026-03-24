package com.rozetka.gigaavito.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.rozetka.gigaavito.R
import com.rozetka.gigaavito.screens.chat.components.ChatBubble
import com.rozetka.gigaavito.screens.chat.components.ChatInputBar
import com.rozetka.gigaavito.screens.chat.components.ChatSettingsBottomSheet
import com.rozetka.gigaavito.utils.MediaViewer
import com.rozetka.gigaavito.utils.extractGigaImageId
import com.rozetka.gigaavito.utils.removeGigaImageTags
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ChatScreen(
    chatId: String,
    viewModel: ChatViewModel = koinViewModel(parameters = { parametersOf(chatId) }),
    onNavigateBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var fullScreenMedia by remember { mutableStateOf<Pair<String, ByteArray>?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    val state by viewModel.viewState.collectAsState()

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    val errorMsg = stringResource(R.string.error_generation)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> viewModel.attachImage(uri) }

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collect { (failedText, failedImageUri) ->
            messageText = failedText
            if (failedImageUri != null) {
                viewModel.attachImage(failedImageUri)
            }
            snackbarHostState.showSnackbar(
                message = errorMsg,
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(state.messages.size, state.generatingMessage) {
        if (state.messages.isNotEmpty() || state.generatingMessage != null) {
            listState.animateScrollToItem(0)
        }
    }

    if (showRenameDialog) {
        var newTitle by remember { mutableStateOf(state.chatInfo?.title ?: "") }
        val onRenameConfirm = {
            if (newTitle.isNotBlank()) viewModel.renameChat(newTitle)
            showRenameDialog = false
        }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text(stringResource(R.string.dialog_rename_title)) },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = onRenameConfirm) { Text(stringResource(R.string.btn_save)) }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }

    if (showBottomSheet) {
        ChatSettingsBottomSheet(
            selectedModel = state.selectedModel,
            models = state.models,
            onDismiss = { showBottomSheet = false },
            onRenameClick = {
                showBottomSheet = false
                showRenameDialog = true
            },
            onModelSelect = { model ->
                viewModel.selectModel(model)
                showBottomSheet = false
            }
        )
    }

    SharedTransitionLayout {
        AnimatedContent(
            targetState = fullScreenMedia,
            label = "ChatMediaTransition"
        ) { targetMedia ->
            if (targetMedia != null) {
                MediaViewer(
                    mediaData = targetMedia.second,
                    onDismiss = { fullScreenMedia = null },
                    modifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "chat_img_${targetMedia.first}"),
                        animatedVisibilityScope = this@AnimatedContent,
                        boundsTransform = { _, _ -> tween(300, easing = FastOutSlowInEasing) },
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    )
                )
            } else {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        CenterAlignedTopAppBar(
                            scrollBehavior = scrollBehavior,
                            title = {
                                Text(state.chatInfo?.title ?: stringResource(R.string.chat_default_title), style = MaterialTheme.typography.titleMedium)
                            },
                            navigationIcon = {
                                IconButton(onClick = onNavigateBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                                }
                            },
                            actions = {
                                IconButton(onClick = { showBottomSheet = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.menu_settings))
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )
                    },
                    bottomBar = {
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .navigationBarsPadding()
                                .imePadding()
                                .padding(bottom = 8.dp)
                        ) {
                            if (state.attachedImageUri != null) {
                                Box(modifier = Modifier.padding(start = 16.dp, bottom = 8.dp, top = 8.dp)) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(state.attachedImageUri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { viewModel.attachImage(null) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .offset(x = 8.dp, y = (-8).dp)
                                            .size(24.dp)
                                            .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }

                            ChatInputBar(
                                messageText = messageText,
                                onValueChange = { messageText = it },
                                onAttachClick = { photoPickerLauncher.launch("image/*") },
                                onSend = {
                                    if (!state.isGenerating && (messageText.isNotBlank() || state.attachedImageUri != null)) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.sendMessageWithContext(messageText, state.attachedImageUri, context)
                                        messageText = ""
                                    }
                                },
                                isLoading = state.isGenerating,
                                hasAttachment = state.attachedImageUri != null
                            )
                        }
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .consumeWindowInsets(paddingValues)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            reverseLayout = true
                        ) {
                            if (state.generatingMessage != null) {
                                item {
                                    ChatBubble(
                                        text = state.generatingMessage ?: "",
                                        isUser = false,
                                        timestamp = System.currentTimeMillis(),
                                        isGenerating = true,
                                        viewModel = viewModel,
                                        sharedTransitionScope = this@SharedTransitionLayout,
                                        animatedVisibilityScope = this@AnimatedContent
                                    )
                                }
                            }

                            items(state.messages, key = { it.id }) { message ->
                                val fileId = message.text.extractGigaImageId()
                                val cleanText = message.text.removeGigaImageTags()

                                ChatBubble(
                                    text = cleanText,
                                    isUser = message.isUser,
                                    timestamp = message.timestamp,
                                    fileId = fileId,
                                    viewModel = viewModel,
                                    sharedTransitionScope = this@SharedTransitionLayout,
                                    animatedVisibilityScope = this@AnimatedContent,
                                    onImageClick = { bytes ->
                                        if (fileId != null && bytes != null) {
                                            fullScreenMedia = fileId to bytes
                                        }
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
