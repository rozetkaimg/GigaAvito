package com.rozetka.gigaavito.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.rozetka.gigaavito.R
import java.io.File

fun shareImage(context: Context, imageUrl: String) {
    val file = getGigaImageFile(context, imageUrl)
    if (!file.exists()) return

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.action_share)))
}

fun Context.shareMedia(text: String? = null, imageBytes: ByteArray? = null) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        var isImageAttached = false

        if (imageBytes != null) {
            runCatching {
                val file = File(cacheDir, "shared_gen_${System.currentTimeMillis()}.jpg")
                file.writeBytes(imageBytes)
                val uri = FileProvider.getUriForFile(this@shareMedia, "$packageName.provider", file)

                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newRawUri("", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                isImageAttached = true
            }
        }

        if (!isImageAttached) {
            type = "text/plain"
        }

        if (!text.isNullOrBlank()) {
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }

    startActivity(Intent.createChooser(intent, getString(R.string.action_share)))
}