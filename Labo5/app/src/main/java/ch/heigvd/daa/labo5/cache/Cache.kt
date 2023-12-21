package ch.heigvd.daa.labo5.cache

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.nio.file.Files

/**
 * Singleton object that provides caching functionality for Bitmap objects.
 * @author Timothée Van Hove, Léo Zmoos
 */
object Cache {
    private lateinit var cacheDir: File
    private const val EXP_DELAY = 60 * 5000L

    /**
     * Sets the directory used for caching Bitmap objects.
     * @param cacheDir The file directory to be used for caching.
     */
    fun setDir(cacheDir: File) {
        Cache.cacheDir = cacheDir
        createCacheDir()
    }

    /**
     * Retrieves a Bitmap from the cache.
     * @param name The name of the file to be retrieved.
     * @return A Bitmap if it exists in the cache and hasn't expired, null otherwise.
     */
    fun get(name: String): Bitmap? {
        val file = File(cacheDir, name)
        if (file.exists() && file.canRead() && file.length() != 0L && !isExpired(file)) {
            return BitmapFactory.decodeFile(file.path);
        }
        return null
    }

    /**
     * Caches a Bitmap object.
     * @param name The name of the file to be saved.
     * @param image The Bitmap object to be saved.
     */
    fun set(name: String, image: Bitmap) = File(cacheDir, name).writeBitmap(image)

    /**
     * Clears the cache directory.
     */
    fun clear() = cacheDir.deleteRecursively().also { createCacheDir() }

    /**
     * Creates a cache directory if it doesn't exist.
     */
    private fun createCacheDir() {
        if (!cacheDir.exists())
            Files.createDirectory(cacheDir.toPath())
    }

    /**
     * Checks if a file in the cache has expired.
     * @param file The file to check for expiration.
     * @return True if the file has expired, false otherwise.
     */
    private fun isExpired(file: File) = file.lastModified() < System.currentTimeMillis() - EXP_DELAY

    /**
     * Writes a Bitmap object to the file system.
     * @receiver File The file to write the Bitmap to.
     * @param bitmap The Bitmap object to write.
     * @param quality The quality of the compressed image.
     */
    private fun File.writeBitmap(bitmap: Bitmap, quality: Int = 100) {
        // Check if the parent directory exists, create it if it doesn't
        parentFile?.let {if (!it.exists()) it.mkdirs()}

        outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it)
            it.flush()
        }
    }
}