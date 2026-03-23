package com.rozetka.gigaavito.screens.navigation


@JvmInline
value class Screen(val route: String) {
    companion object {
        val Login = Screen("login")
        val Register = Screen("register")
        val MainFlow = Screen("main_flow")
        val ChatList = Screen("chat_list")
        val Chat = Screen("chat/{chatId}")
        val Profile = Screen("profile")
        val Images = Screen("images")

        const val ARG_CHAT_ID = "chatId"

        fun createChatRoute(chatId: String) = Screen("chat/$chatId")
    }
}