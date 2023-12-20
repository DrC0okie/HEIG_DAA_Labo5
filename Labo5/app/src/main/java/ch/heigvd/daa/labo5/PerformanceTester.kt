package ch.heigvd.daa.labo5

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.URL
import java.util.concurrent.Executors

object PerformanceTester {

    val dispatcherPairs = listOf(
        "IO" to Dispatchers.IO,
        "2 Threads" to Executors.newFixedThreadPool(2).asCoroutineDispatcher(),
        "4 Threads" to Executors.newFixedThreadPool(4).asCoroutineDispatcher(),
        "8 Threads" to Executors.newFixedThreadPool(8).asCoroutineDispatcher(),
        "16 Threads" to Executors.newFixedThreadPool(16).asCoroutineDispatcher()
    )

    suspend fun testDownloadPerformance(
        items: List<URL>,
        testScope: CoroutineScope,
        uiScope: CoroutineScope,
        updateUI: (String) -> Unit,
        updateProgress: (Int) -> Unit
    ): List<TestResult> {
        val results = mutableListOf<TestResult>()

        testScope.async {
            dispatcherPairs.forEachIndexed { index, (name, dispatcher) ->
                uiScope.launch { updateUI("Testing $name dispatcher...") }.join()
                val duration = testDownloadWithDispatcher(items, name, dispatcher, testScope)
                results.add(TestResult(name, duration))
                uiScope.launch { updateUI("Testing complete!") }.join()
                uiScope.launch { updateProgress(index + 1) }.join() // Update progress
            }
        }.await()

        return results
    }

    private suspend fun testDownloadWithDispatcher(
        items: List<URL>,
        dispatcherName: String,
        dispatcher: CoroutineDispatcher,
        scope: CoroutineScope
    ): Long {
        val startTime = System.currentTimeMillis()
        scope.launch(dispatcher) {
            items.map { url ->
                launch {
                    downloadImage(url)
                }
            }.joinAll()
        }.join() // Ensure the coroutine completes
        return System.currentTimeMillis() - startTime
    }

    private suspend fun downloadImage(url: URL): Bitmap? {
        return try {
            val downloader = ImageDownloader()
            val bytes = downloader.downloadImage(url) // Download the image
            downloader.decodeImage(bytes!!) // Decode and return the Bitmap
        } catch (e: Exception) {
            Log.e("ImageDownload", "Error downloading image from $url", e)
            null
        }
    }
}