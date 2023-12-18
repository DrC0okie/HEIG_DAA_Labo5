package ch.heigvd.daa.labo5

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import androidx.lifecycle.LifecycleCoroutineScope
import java.net.URL
import java.util.concurrent.Executors

/**
 * Adapter for a RecyclerView to display images from URLs using coroutines.
 * @author Timothée Van Hove, Léo Zmoos
 * @param urls List of image URLs to be displayed in the RecyclerView.
 * @param scope LifecycleCoroutineScope for managing coroutine lifecycle.
 */
class ImageRecyclerAdapter(urls: List<URL> = listOf(), private val scope: LifecycleCoroutineScope) :
    RecyclerView.Adapter<ImageRecyclerAdapter.ViewHolder>() {

    private var items = listOf<URL>()

    init {
        items = urls
    }

    override fun getItemCount() = items.size

    /**
     * ViewHolder for the RecyclerView items.
     * Manages the loading and display of images from URLs.
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val image = view.findViewById<ImageView>(R.id.image_view_item)
        private val progressBar = view.findViewById<ProgressBar>(R.id.progressbar_item)
        private var currentUrl: String? = null
        private var downloadJob: Job? = null


        /**
         * Binds a URL to this ViewHolder. If the URL is different from the current one,
         * it cancels any existing download and starts a new one.
         *
         * @param url The URL of the image to be loaded and displayed.
         */
        fun bind(url: URL) {
            // Check if the new URL is different from the current one
            if (currentUrl != url.toString()) {
                // Cancel any existing download job for the previous URL
                downloadJob?.cancel()

                // Reset the visibility of the ProgressBar and ImageView
                progressBar.visibility = View.VISIBLE
                image.visibility = View.INVISIBLE

                // Update the current URL
                currentUrl = url.toString()

                // Start a new coroutine for downloading and displaying the image
                downloadJob = scope.launch {
                    val cachedBitmap =
                        getBitmap(url.path.substring(url.path.lastIndexOf('/') + 1), url)
                    updateImageView(cachedBitmap)
                }
            }
        }

        /**
         * Unbinds the current ViewHolder, canceling any ongoing image download.
         * This method is called when the ViewHolder is being recycled.
         */
        fun unbind() {
            // Cancel any ongoing download job to prevent memory leaks and unnecessary work
            downloadJob?.cancel()

            // Reset the visibility of the ProgressBar and ImageView
            progressBar.visibility = View.VISIBLE
            image.visibility = View.INVISIBLE

            // Clear the current URL since the view is being recycled
            currentUrl = null
        }

        /**
         * Updates the ImageView with the downloaded bitmap on the main thread.
         *
         * @param bitmap The downloaded bitmap to display.
         */
        private suspend fun updateImageView(bitmap: Bitmap) = withContext(Dispatchers.Main) {
            image.setImageBitmap(bitmap)
            image.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }

        /**
         * Retrieves the bitmap from cache or downloads it if not cached.
         *
         * @param filename The filename used for caching the bitmap.
         * @param url The URL of the image to be downloaded.
         * @return Bitmap The bitmap either from cache or downloaded.
         */
        private suspend fun getBitmap(filename: String, url: URL): Bitmap {
            var cachedBitmap = Cache.get(filename)
            if (cachedBitmap == null) {
                val downloader = ImageDownloader()
                val bytes = downloader.downloadImage(url)
                cachedBitmap = downloader.decodeImage(bytes!!)
                Cache.set(filename, cachedBitmap!!)
            }
            return cachedBitmap
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_image, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        items[position].let { holder.bind(it) }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.unbind()
    }
}