package com.rozetka.gigaavito.utils


import androidx.compose.ui.graphics.Color

fun generateColorFromHash(str: String): Color {
    val hash = str.hashCode()
    val r = (hash shr 16 and 0xFF)
    val g = (hash shr 8 and 0xFF)
    val b = (hash and 0xFF)
    return Color(r, g, b)
}

fun getInitials(name: String): String {
    return name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercase() }
        .joinToString("")
}
