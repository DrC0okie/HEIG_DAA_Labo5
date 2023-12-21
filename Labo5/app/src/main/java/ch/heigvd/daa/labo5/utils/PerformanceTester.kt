package ch.heigvd.daa.labo5.utils

import android.graphics.Bitmap
import android.util.Log
import ch.heigvd.daa.labo5.utils.ImageDownloader.decode
import ch.heigvd.daa.labo5.utils.ImageDownloader.download
import kotlinx.coroutines.CoroutineDispatcher as CD
import kotlinx.coroutines.CoroutineScope as CS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.net.URL
import java.util.concurrent.Executors

/**
 * Object for testing the performance of various CoroutineDispatchers.
 *
 * Provides functionality to execute and measure the performance of downloading a list of images
 * using different CoroutineDispatchers. This is used to determine the most efficient dispatcher
 * for parallel image downloading tasks.
 * @author Timothée Van Hove, Léo Zmoos
 */
object PerformanceTester {

    /**
     * A list of pairs of dispatcher names and their corresponding CoroutineDispatcher.
     */
    val dispatcherPairs = listOf(
        "IO" to Dispatchers.IO,
        "2 Threads" to Executors.newFixedThreadPool(2).asCoroutineDispatcher(),
        "8 Threads" to Executors.newFixedThreadPool(8).asCoroutineDispatcher(),
        "16 Threads" to Executors.newFixedThreadPool(16).asCoroutineDispatcher(),
        "32 Threads" to Executors.newFixedThreadPool(32).asCoroutineDispatcher()
    )

    /**
     * Tests the performance of different dispatchers for image downloading tasks.
     * @param items A list of URLs for the images to be downloaded.
     * @param testScope The CoroutineScope in which the download tasks will be executed.
     * @param uiScope The CoroutineScope for updating the UI with the test progress.
     * @param updateUI A lambda function to update the UI with the current testing status.
     * @param updateProgress A lambda function to update the UI with the progress of the tests.
     * @return A list of [TestResult] containing the dispatcher names and their respective durations.
     */
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

    /**
     * Calculates the duration taken to download a list of images using a specified dispatcher.
     * @param items The list of URLs for the images to be downloaded.
     * @param disp The CoroutineDispatcher to be used for the download tasks.
     * @param scope The CoroutineScope in which the download tasks will be executed.
     * @return The duration (in milliseconds) taken to complete all download tasks.
     */
    private suspend fun getDuration(items: List<URL>, disp: CD, scope: CS): Long {
        val startTime = System.currentTimeMillis()
        scope.launch(disp) { items.map { url -> launch { downloadImage(url) } }.joinAll() }.join()
        return System.currentTimeMillis() - startTime
    }

    /**
     * Attempts to download and decode an image from a given URL.
     * @param url The URL of the image to be downloaded and decoded.
     * @return A Bitmap of the downloaded image, or null if the download or decoding fails.
     */
    private suspend fun downloadImage(url: URL): Bitmap? {
        return try {
            decode(download(url)!!)
        } catch (e: Exception) {
            Log.e("ImageDownload", "Error downloading image from $url", e)
            null
        }
    }
}