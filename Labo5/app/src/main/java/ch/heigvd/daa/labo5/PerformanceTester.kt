package ch.heigvd.daa.labo5

import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher as CD
import kotlinx.coroutines.CoroutineScope as CS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.net.URL
import java.util.concurrent.Executors

object PerformanceTester {

    val dispatcherPairs = listOf(
        "IO" to Dispatchers.IO,
        "2 Threads" to Executors.newFixedThreadPool(2).asCoroutineDispatcher(),
        "8 Threads" to Executors.newFixedThreadPool(8).asCoroutineDispatcher(),
        "16 Threads" to Executors.newFixedThreadPool(16).asCoroutineDispatcher(),
        "32 Threads" to Executors.newFixedThreadPool(32).asCoroutineDispatcher()
    )

    suspend fun testDispatcherPerformance(
        items: List<URL>,
        testScope: CS,
        uiScope: CS,
        updateUI: (String) -> Unit,
        updateProgress: (Int) -> Unit
    ): List<TestResult> {
        val results = mutableListOf<TestResult>()

        testScope.async {
            dispatcherPairs.forEachIndexed { index, (name, dispatcher) ->
                uiScope.launch { updateUI("Testing $name dispatcher...") }.join()
                results.add(TestResult(name, getDuration(items, dispatcher, testScope)))
                uiScope.launch { updateUI("Testing complete!"); updateProgress(index + 1) }.join()
            }
        }.await()
        return results
    }

    private suspend fun getDuration(items: List<URL>, disp: CD, scope: CS): Long {
        val startTime = System.currentTimeMillis()
        scope.launch(disp) { items.map { url -> launch { downloadImage(url) } }.joinAll() }.join()
        return System.currentTimeMillis() - startTime
    }

    private suspend fun downloadImage(url: URL): Bitmap? {
        return try {
            with(ImageDownloader()) { decodeImage(downloadImage(url)!!) }
        } catch (e: Exception) {
            Log.e("ImageDownload", "Error downloading image from $url", e)
            null
        }
    }
}