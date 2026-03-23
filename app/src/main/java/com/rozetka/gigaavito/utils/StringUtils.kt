package com.rozetka.gigaavito.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import com.rozetka.gigaavito.R
import java.io.File

const val GIGA_IMAGE_PREFIX = "giga_"
const val GIGA_IMAGE_EXT = ".jpg"
private val IMG_REGEX = "<img src=\"([^\"]+)\"[^>]*>".toRegex()

val String.colorHash: Color
    get() {
        val hash = hashCode()
        return Color(
            (hash shr 16) and 0xFF,
            (hash shr 8) and 0xFF,
            hash and 0xFF
        )
    }

fun String.extractGigaImageId(): String? {
    return IMG_REGEX.find(this)?.groups?.get(1)?.value
}

fun String.removeGigaImageTags(): String {
    return this.replace(IMG_REGEX, "").trim()
}

fun getGigaImageFile(context: Context, imageId: String): File {
    return File(context.filesDir, "$GIGA_IMAGE_PREFIX$imageId$GIGA_IMAGE_EXT")
}

