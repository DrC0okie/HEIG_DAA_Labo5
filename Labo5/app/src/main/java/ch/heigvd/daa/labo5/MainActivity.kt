package ch.heigvd.daa.labo5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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

    private lateinit var clearCachePeriodicRequest: WorkRequest

    private lateinit var adapter: ImageRecyclerAdapter

    companion object {
        const val CLEAR_CACHE_INTERVAL = 15L
        const val PICTURES_NB = 10000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Init the cache object default path
        Cache.setDir(cacheDir)

        // Generate list of URLs
        val items = List(PICTURES_NB) {
            val num = it + 1
            URL("https://daa.iict.ch/images/$num.jpg")
        }

        adapter = ImageRecyclerAdapter(items, lifecycleScope)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)

        // Bind the periodic cache clear
        clearCachePeriodicRequest =
            PeriodicWorkRequestBuilder<ClearCacheWorker>(
                CLEAR_CACHE_INTERVAL,
                TimeUnit.MINUTES
            ).build()

        WorkManager
            .getInstance(applicationContext)
            .enqueue(clearCachePeriodicRequest)
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

    override fun onStop() {
        super.onStop()
        lifecycleScope.coroutineContext.cancelChildren()
    }

    private fun launchClearCache() {
        val clearCacheRequest = OneTimeWorkRequest.Builder(ClearCacheWorker::class.java).build()
        WorkManager
            .getInstance(applicationContext)
            .enqueue(clearCacheRequest)
        adapter.notifyDataSetChanged()
    }
}