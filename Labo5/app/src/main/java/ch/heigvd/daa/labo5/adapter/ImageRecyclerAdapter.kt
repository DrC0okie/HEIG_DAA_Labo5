package ch.heigvd.daa.labo5.adapter

import android.content.Context
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
import ch.heigvd.daa.labo5.R
import ch.heigvd.daa.labo5.cache.Cache
import ch.heigvd.daa.labo5.utils.ImageDownloader.decode
import ch.heigvd.daa.labo5.utils.ImageDownloader.download
import ch.heigvd.daa.labo5.utils.Network.isNetworkAvailable
import java.net.URL

/**
 * Adapter for a RecyclerView to display images from URLs using coroutines.
 * @param urls List of image URLs to be displayed in the RecyclerView.
 * @param scope LifecycleCoroutineScope for managing coroutine lifecycle.
 * @author Timothée Van Hove, Léo Zmoos
 */
class ImageRecyclerAdapter(
    private val context: Context,
    urls: List<URL> = listOf(),
    private val scope: LifecycleCoroutineScope,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<ImageRecyclerAdapter.ViewHolder>() {

    private var items: List<URL>

    init {
        items = urls
    }

    override fun getItemCount() = items.size

    /**
     * ViewHolder for the RecyclerView items.
     * Manages the loading and display of images from URLs.
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        init {
            view.setOnClickListener {
                // Scale animation for visual feedback
                view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(200).withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
                }
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(position, items)
                }
            }
        }

        private val image = view.findViewById<ImageView>(R.id.image_view_item)
        private val progressBar = view.findViewById<ProgressBar>(R.id.progressbar_item)
        private var currentUrl: String? = null
        private var downloadJob: Job? = null

        /**
         * Binds a URL to this ViewHolder. If the URL is different from the current one,
         * it cancels any existing download and starts a new one.
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
         * @param bitmap The downloaded bitmap to display.
         */
        private suspend fun updateImageView(bitmap: Bitmap?) = withContext(Dispatchers.Main) {
            if (bitmap != null) {
                image.setImageBitmap(bitmap)
                image.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            } else {
                // Handle the case where the bitmap is null (display a placeholder)
                image.setImageResource(R.drawable.ic_launcher_foreground)
                image.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
        }

        /**
         * Retrieves the bitmap from cache or downloads it if not cached.
         * @param filename The filename used for caching the bitmap.
         * @param url The URL of the image to be downloaded.
         * @return Bitmap The bitmap either from cache or downloaded.
         */
        private suspend fun getBitmap(filename: String, url: URL): Bitmap? {
            var cachedBitmap = Cache.get(filename)
            if (cachedBitmap != null) {
                return cachedBitmap
            }

            if (!isNetworkAvailable(context)) {
                // Log a message or handle no network scenario
                Log.e("ImageRecyclerAdapter", "No network available to download image")
                return null
            }

            try {
                val bytes = download(url)
                if (bytes != null) {
                    cachedBitmap = decode(bytes)
                    Cache.set(filename, cachedBitmap!!)
                }
            } catch (e: Exception) {
                Log.e("ImageRecyclerAdapter", "Error downloading image from $url", e)
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

    fun updateItems(newItems: List<URL>) {
        items = newItems
        notifyDataSetChanged()
    }
}