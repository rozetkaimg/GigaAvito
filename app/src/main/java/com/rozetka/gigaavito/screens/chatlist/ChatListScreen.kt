package com.rozetka.gigaavito.screens.chatlist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.rozetka.gigaavito.R
import com.rozetka.gigaavito.screens.chatlist.component.ChatListItem
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    viewModel: ChatListViewModel = koinViewModel(),
    onOpenDrawer: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    isSearchActiveFromExternal: Boolean = false,
    onExternalSearchConsumed: () -> Unit = {}
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val typingChats by viewModel.typingChats.collectAsState()
    val isSystemBusy = typingChats.isNotEmpty()
    val pagedChats = viewModel.chats.collectAsLazyPagingItems()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }

    val newChatDefaultTitle = stringResource(R.string.new_chat_title)
    val noMessagesPlaceholder = stringResource(R.string.no_messages)

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isSearchActiveFromExternal) {
        if (isSearchActiveFromExternal) {
            isSearchActive = true
            onExternalSearchConsumed()
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        searchText = ""
        viewModel.updateSearchQuery("")
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AnimatedContent(
                targetState = isSearchActive,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                },
                label = "top_bar_animation"
            ) { active ->
                if (active) {
                    TopAppBar(
                        title = {
                            TextField(
                                value = searchText,
                                onValueChange = {
                                    searchText = it
                                    viewModel.updateSearchQuery(it) // Живой поиск
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester),
                                placeholder = { Text(stringResource(R.string.search_chats_placeholder)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        keyboardController?.hide()
                                    }
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                isSearchActive = false
                                searchText = ""
                                viewModel.updateSearchQuery("")
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            }
                        },
                        actions = {
                            if (searchText.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchText = ""
                                    viewModel.updateSearchQuery("")
                                }) {
                                    Icon(Icons.Default.Close, null)
                                }
                            }
                        }
                    )
                } else {
                    MediumTopAppBar(
                        title = { Text(stringResource(R.string.chats_title), fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = onOpenDrawer) {
                                Icon(Icons.Default.Menu, null)
                            }
                        },
                        actions = {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Default.Search, null)
                            }
                        },
                        scrollBehavior = scrollBehavior
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!isSystemBusy) {
                        viewModel.createNewChat(newChatDefaultTitle) {
                            onNavigateToChat("new")
                        }
                    }
                },
                containerColor = if (isSystemBusy) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                contentColor = if (isSystemBusy) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp)
            ) {
                if (isSystemBusy) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Edit, null)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(pagedChats.itemCount) { index ->
                val item = pagedChats[index]
                item?.let { chatInfo ->
                    val isTyping = typingChats.contains(chatInfo.chat.id)

                    ChatListItem(
                        title = chatInfo.chat.title,
                        lastMessage = chatInfo.lastMessage ?: noMessagesPlaceholder,
                        timestamp = chatInfo.lastMessageTimestamp,
                        isPinned = chatInfo.chat.isPinned,
                        isTyping = isTyping,
                        onClick = { onNavigateToChat(chatInfo.chat.id) },
                        onLongClick = { viewModel.togglePinChat(chatInfo.chat.id, chatInfo.chat.isPinned) }
                    )
                }
            }

            if (pagedChats.loadState.refresh is LoadState.Loading || pagedChats.loadState.append is LoadState.Loading) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}