package ch.heigvd.daa.labo5.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.*
import ch.heigvd.daa.labo5.cache.Cache
import ch.heigvd.daa.labo5.cache.ClearCacheWorker
import ch.heigvd.daa.labo5.adapter.ImageRecyclerAdapter
import ch.heigvd.daa.labo5.adapter.OnItemClickListener
import ch.heigvd.daa.labo5.R
import ch.heigvd.daa.labo5.databinding.ActivityMainBinding
import ch.heigvd.daa.labo5.utils.Dialogs.showNoConnectionDialog
import ch.heigvd.daa.labo5.utils.Network.isNetworkAvailable
import kotlinx.coroutines.cancelChildren
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * The main activity for the image gallery application.
 * @author Timothée Van Hove, Léo Zmoos
 */
class MainActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ImageRecyclerAdapter

    companion object {
        const val INTERVAL = 15L
        val items = List(10000) { URL("https://daa.iict.ch/images/${it + 1}.jpg") }
        const val uniqueWorkName = "ClearCachePeriodicWorkLabo5"
        val WM_POLICY = ExistingPeriodicWorkPolicy.KEEP
        val UNIT = TimeUnit.MINUTES
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Init the cache object default path
        Cache.setDir(cacheDir)

        initRecyclerView()
        tryLoadImages()
        setPeriodicCacheClear()
        setupSwipeRefresh()
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_actions_refresh -> {
                if (adapter.itemCount > 0) {
                    manualClearCache() // Clear cache and reload if items are already there
                } else {
                    tryLoadImages() // Just load images if the RecyclerView is empty
                }
                true
            }

            R.id.menu_actions_test -> {
                this.startActivity(Intent(this, TestActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Perform any final cleanup before an activity is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            // This ensures that coroutines are cancelled only when the activity is truly finishing
            lifecycleScope.coroutineContext.cancelChildren()
        }
    }

    /**
     * Callback method to be invoked when an item in this view has been clicked.
     * @param position The position of the view in the adapter.
     * @param items The list of URLs that the adapter is currently managing.
     */
    override fun onItemClick(position: Int, items: List<URL>) {
        val imageUrl = items[position].toString()
        val intent = Intent(this, FullScreenImageActivity::class.java)
        intent.putExtra("IMAGE_URL", imageUrl)
        startActivity(intent)
    }

    /**
     * Triggers a manual cache clear operation and attempts to reload images.
     */
    private fun manualClearCache() {
        val clearCacheWork = OneTimeWorkRequestBuilder<ClearCacheWorker>().build()
        WorkManager.getInstance(this).enqueue(clearCacheWork)

        // After clearing the cache, reload the images
        if (tryLoadImages())
            Toast.makeText(this, "Cache cleared and images reloaded", Toast.LENGTH_LONG).show()
    }

    /**
     * Initializes the RecyclerView with an adapter and layout manager.
     */
    private fun initRecyclerView() {
        adapter = ImageRecyclerAdapter(this, emptyList(), lifecycleScope, this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)
    }

    /**
     * Attempts to load images if network is available. Displays a dialog if not.
     * @return Boolean True if images are loaded, false otherwise.
     */
    private fun tryLoadImages(): Boolean {
        return if (isNetworkAvailable(this)) {
            adapter.updateItems(items)
            true
        } else {
            showNoConnectionDialog(this, layoutInflater)
            false
        }
    }

    /**
     * Sets up a periodic cache clearing task using WorkManager.
     */
    private fun setPeriodicCacheClear() {
        // Bind the periodic cache clear
        val clearCacheRequest = PeriodicWorkRequestBuilder<ClearCacheWorker>(INTERVAL, UNIT).build()
        val wm = WorkManager.getInstance(this)
        wm.enqueueUniquePeriodicWork(uniqueWorkName, WM_POLICY, clearCacheRequest)
    }

    /**
     * Sets up the swipe-to-refresh layout's behavior.
     */
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            manualClearCache()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}