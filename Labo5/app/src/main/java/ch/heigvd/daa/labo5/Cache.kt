package ch.heigvd.daa.labo5

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.nio.file.Files

object Cache {
    private lateinit var cacheDir: File
    private const val EXP_DELAY = 60 * 5000L

    fun setDir(cacheDir: File) {
        Cache.cacheDir = cacheDir
        createCacheDir()
    }

    fun get(name: String): Bitmap? {
        val file = File(cacheDir, name)
        if (file.exists() && file.canRead() && file.length() != 0L && !isExpired(file)) {
            return BitmapFactory.decodeFile(file.path);
        }
        return null
    }

    fun set(name: String, image: Bitmap) = File(cacheDir, name).writeBitmap(image)

    fun clear() = cacheDir.deleteRecursively().also { createCacheDir() }

    private fun createCacheDir() {
        if (!cacheDir.exists())
            Files.createDirectory(cacheDir.toPath())
    }

    private fun isExpired(file: File) = file.lastModified() < System.currentTimeMillis() - EXP_DELAY

    private fun File.writeBitmap(bitmap: Bitmap, quality: Int = 100) {
        outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it)
            it.flush()
        }
    }
}