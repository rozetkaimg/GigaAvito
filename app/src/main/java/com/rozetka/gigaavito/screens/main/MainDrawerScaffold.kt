package com.rozetka.gigaavito.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rozetka.gigaavito.R
import com.rozetka.gigaavito.screens.chat.ChatScreen
import com.rozetka.gigaavito.screens.chatlist.ChatListScreen
import com.rozetka.gigaavito.screens.images.ImagesScreen
import com.rozetka.gigaavito.screens.navigation.Screen
import com.rozetka.gigaavito.screens.profile.ProfileScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDrawerScaffold(onLogout: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val mainNavController = rememberNavController()
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var triggerSearchOnMain by remember { mutableStateOf(false) }
    var menuSearchText by remember { mutableStateOf("") }
    val onSearchTrigger = {
        if (currentRoute != Screen.ChatList.route) {
            mainNavController.navigate(Screen.ChatList.route) {
                popUpTo(Screen.ChatList.route) { inclusive = true }
            }
        }
        scope.launch {
            drawerState.close()
            triggerSearchOnMain = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(320.dp),
                drawerShape = MaterialTheme.shapes.extraLarge,
                drawerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = menuSearchText,
                    onValueChange = { menuSearchText = it },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { onSearchTrigger() }, // Клик по всей области поля
                    placeholder = { Text(stringResource(R.string.menu_search_hint)) },
                    leadingIcon = {
                        IconButton(onClick = { onSearchTrigger() }) {
                            Icon(Icons.Default.Search, null)
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.close() }
                            mainNavController.navigate(Screen.Chat.createRoute("new")) {
                                popUpTo(Screen.ChatList.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Outlined.Message, null)
                        }
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        disabledBorderColor = Color.Transparent
                    ),
                    enabled = true
                )

                Spacer(Modifier.height(16.dp))

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_new_chat), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Add, null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        mainNavController.navigate(Screen.Chat.createRoute("new")) {
                            popUpTo(Screen.ChatList.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_images), fontWeight = FontWeight.Bold) },
                    icon = { Icon(if (currentRoute == Screen.Images.route) Icons.Filled.Image else Icons.Outlined.Image, null) },
                    selected = currentRoute == Screen.Images.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        mainNavController.navigate(Screen.Images.route) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp))

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_home), fontWeight = FontWeight.Bold) },
                    icon = { Icon(if (currentRoute == Screen.ChatList.route) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline, null) },
                    selected = currentRoute == Screen.ChatList.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        mainNavController.navigate(Screen.ChatList.route) {
                            popUpTo(Screen.ChatList.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_profile), fontWeight = FontWeight.Bold) },
                    icon = { Icon(if (currentRoute == Screen.Profile.route) Icons.Filled.Person else Icons.Outlined.PersonOutline, null) },
                    selected = currentRoute == Screen.Profile.route,
                    onClick = {
                        scope.launch { drawerState.close() }
                        mainNavController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(Modifier.weight(1f))

                NavigationDrawerItem(
                    label = { Text(stringResource(R.string.menu_logout), fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
                    selected = false,
                    onClick = onLogout,
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedTextColor = MaterialTheme.colorScheme.error,
                        unselectedIconColor = MaterialTheme.colorScheme.error
                    )
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    ) {
        NavHost(navController = mainNavController, startDestination = Screen.ChatList.route) {
            composable(Screen.ChatList.route) {
                ChatListScreen(
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onNavigateToChat = { chatId -> mainNavController.navigate(Screen.Chat.createRoute(chatId)) },
                    isSearchActiveFromExternal = triggerSearchOnMain,
                    onExternalSearchConsumed = { triggerSearchOnMain = false }
                )
            }
            composable(
                route = Screen.Chat.route,
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                key(chatId) {
                    ChatScreen(
                        chatId = chatId,
                        onNavigateBack = { mainNavController.popBackStack() }
                    )
                }
            }
            composable(Screen.Profile.route) {
                ProfileScreen(onOpenDrawer = { scope.launch { drawerState.open() } }, onLogout = onLogout)
            }
            composable(Screen.Images.route) {
                ImagesScreen(
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
        }
    }
}