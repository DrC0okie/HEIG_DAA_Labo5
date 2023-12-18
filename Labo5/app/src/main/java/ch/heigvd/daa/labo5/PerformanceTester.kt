package ch.heigvd.daa.labo5

import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.URL
import java.util.concurrent.Executors

class PerformanceTester(private val items: List<URL>, private val scope: CoroutineScope) {

    fun testDownloadPerformance(): String {
        val results = StringBuilder()
        val dispatcherPairs = listOf(
            "IO" to Dispatchers.IO,
            "2 Threads" to Executors.newFixedThreadPool(2).asCoroutineDispatcher(),
            "4 Threads" to Executors.newFixedThreadPool(4).asCoroutineDispatcher(),
            "8 Threads" to Executors.newFixedThreadPool(8).asCoroutineDispatcher(),
            "16 Threads" to Executors.newFixedThreadPool(16).asCoroutineDispatcher()
        )

        runBlocking {
            dispatcherPairs.forEach { (name, dispatcher) ->
                val result = testDownloadWithDispatcher(name, dispatcher)
                results.append("$result\n")
            }
        }
        return results.toString()
    }

    private suspend fun testDownloadWithDispatcher(dispatcherName: String, dispatcher: CoroutineDispatcher): String {
        val startTime = System.currentTimeMillis()
        scope.launch(dispatcher) {
            items.map { url ->
                launch {
                    downloadImage(url)
                }
            }.joinAll()
        }.join() // Ensure the coroutine completes
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        return "$dispatcherName: $duration ms"
    }


    private suspend fun downloadImage(url: URL): Bitmap? {
        return try {
            val downloader = ImageDownloader() // Assuming this is a class with downloading logic
            val bytes = downloader.downloadImage(url) // Download the image
            downloader.decodeImage(bytes!!) // Decode and return the Bitmap
        } catch (e: Exception) {
            Log.e("ImageDownload", "Error downloading image from $url", e)
            null
        }
    }
}