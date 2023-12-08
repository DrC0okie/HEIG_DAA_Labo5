package ch.heigvd.daa.labo5

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.nio.file.Files

object Cache {
    private lateinit var cacheDir: File
    private var expirationDelay = 60 * 5000L // Files expire after 5 minutes

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

    fun set(name: String, image: Bitmap) {
        val file = File(cacheDir, name)
        file.writeBitmap(image)
    }

    fun clear() {
        cacheDir.deleteRecursively()
        createCacheDir()
    }

    private fun createCacheDir() {
        if (!cacheDir.exists()) {
            Files.createDirectory(cacheDir.toPath())
        }
    }

    private fun isExpired(file: File): Boolean {
        return file.lastModified() < System.currentTimeMillis() - expirationDelay
    }

    private fun File.writeBitmap(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        quality: Int = 100
    ) {
        outputStream().use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }
    }
}