package ch.heigvd.daa.labo5.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

object ImageDownloader {

    suspend fun download(url: URL): ByteArray? = withContext(Dispatchers.IO) {
        try {
            url.readBytes()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun decode(bytes: ByteArray): Bitmap? = withContext(Dispatchers.Default) {
        try {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }
}