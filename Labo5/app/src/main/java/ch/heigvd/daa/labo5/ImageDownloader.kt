package ch.heigvd.daa.labo5

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.concurrent.Executors

class ImageDownloader {

    // essaie d'un custom threads dispatcher pour améliorer les performances
    val myThreadDispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()
    companion object {
        private val TAG = ImageDownloader::class.qualifiedName
    }

    suspend fun downloadImage(url: URL): ByteArray? = withContext(myThreadDispatcher) {
        try {
            url.readBytes()
        } catch (e: Exception) {
            Log.w(TAG, "Error downloading image", e)
            null
        }
    }

    suspend fun decodeImage(bytes: ByteArray): Bitmap? = withContext(Dispatchers.Default) {
        try {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            Log.w(TAG, "Error decoding image", e)
            null
        }
    }
}