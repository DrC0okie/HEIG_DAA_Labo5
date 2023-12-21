package ch.heigvd.daa.labo5.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * Singleton responsible for downloading and decoding images.
 *
 * Provides utility functions to download image data from a URL and to decode data into a Bitmap.
 * This object abstracts the network and image processing operations
 * @author Timothée Van Hove, Léo Zmoos
 */
object ImageDownloader {

    /**
     * Downloads image data as a ByteArray from the specified URL.
     * @param url The URL from which to download the image data.
     * @return A ByteArray containing the downloaded image data, or null if the download fails.
     */
    suspend fun download(url: URL): ByteArray? = withContext(Dispatchers.IO) {
        try {
            url.readBytes()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decodes a ByteArray into a Bitmap.
     * @param bytes The ByteArray containing the image data to decode.
     * @return A Bitmap representing the decoded image, or null if the decoding fails.
     */
    suspend fun decode(bytes: ByteArray): Bitmap? = withContext(Dispatchers.Default) {
        try {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (e: Exception) {
            null
        }
    }
}