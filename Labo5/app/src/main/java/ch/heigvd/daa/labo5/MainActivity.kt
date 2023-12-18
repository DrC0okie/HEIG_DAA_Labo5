package ch.heigvd.daa.labo5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import java.util.concurrent.TimeUnit
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.WorkRequest
import ch.heigvd.daa.labo5.databinding.ActivityMainBinding
import kotlinx.coroutines.cancelChildren
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var clearCacheRequest: WorkRequest
    private lateinit var adapter: ImageRecyclerAdapter

    companion object {
        const val CLEAR_CACHE_INTERVAL = 15L
        const val PICTURES_NB = 10000
        const val ENDPONT = "https://daa.iict.ch/images/"
        const val FILE_EXT = ".jpg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Init the cache object default path
        Cache.setDir(cacheDir)

        // Generate list of URLs
        val items = List(PICTURES_NB) { URL("$ENDPONT${it + 1}$FILE_EXT") }

        adapter = ImageRecyclerAdapter(items, lifecycleScope)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)

        // Bind the periodic cache clear
        clearCacheRequest =
            PeriodicWorkRequestBuilder<ClearCacheWorker>(
                CLEAR_CACHE_INTERVAL,
                TimeUnit.MINUTES
            ).build()

        WorkManager.getInstance(applicationContext).enqueue(clearCacheRequest)

        // Test button click listener
        val testButton = findViewById<Button>(R.id.startTestButton)
        testButton.setOnClickListener {
            val nbItems = 32
            val results = PerformanceTester(items.take(nbItems), lifecycleScope).testDownloadPerformance()
            showResultsDialog(results, nbItems)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_actions_refresh -> {
                launchClearCache()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            // This ensures that coroutines are cancelled only when the activity is truly finishing
            lifecycleScope.coroutineContext.cancelChildren()
        }
    }

    private fun launchClearCache() {
        val clearCacheRequest = OneTimeWorkRequest.Builder(ClearCacheWorker::class.java).build()
        WorkManager.getInstance(applicationContext).enqueue(clearCacheRequest)
        adapter.notifyDataSetChanged()
    }

    private fun showResultsDialog(results: String, nbItem: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Test Results for $nbItem parallel downloads")
        builder.setMessage(results)
        builder.setPositiveButton("OK", null)
        val dialog = builder.create()
        dialog.show()
    }
}