package com.gujju.thoughts

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

suspend fun shareImageFromUrl(
    context: Context,
    imageUrl: String,
    fileName: String
) {
    try {
        // Download the image on an IO thread
        val bitmap = withContext(Dispatchers.IO) {
            val url = URL(imageUrl)
            BitmapFactory.decodeStream(url.openConnection().getInputStream())
        }

        // Save the bitmap to a temporary file
        val file = withContext(Dispatchers.IO) {
            // Get the cache directory for sharing
            val cachePath = File(context.filesDir, "images")
            cachePath.mkdirs() // create the directory if it doesn't exist

            val tempFile = File(cachePath, fileName)
            tempFile.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                out.flush()
            }
            tempFile
        }

        // Get a shareable URI for the file using FileProvider
        val fileUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        // Create and configure the share intent
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Read Amazing Suvichar Quotes from this amazing app. Download it now: https://play.google.com/store/apps/details?id=" + context.packageName
        )
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Start the sharing activity chooser
        context.startActivity(Intent.createChooser(shareIntent, "Share image via"))
    } catch (e: Exception) {
        // Handle any exceptions during the process (e.g., network error, file creation error)
        e.printStackTrace()
        // You might want to show a toast or a snackbar here to inform the user.
    }
}

