package ch.heigvd.daa.labo5

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.work.*
import ch.heigvd.daa.labo5.databinding.ActivityMainBinding
import kotlinx.coroutines.cancelChildren
import java.net.URL
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ImageRecyclerAdapter

    companion object {
        const val INTERVAL = 15L
        const val PICTURES_NB = 10000
        const val ENDPOINT = "https://daa.iict.ch/images/"
        const val FILE_EXT = ".jpg"
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

        // Generate list of URLs
        val items = List(PICTURES_NB) { URL("$ENDPOINT${it + 1}$FILE_EXT") }

        adapter = ImageRecyclerAdapter(items, lifecycleScope, this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = GridLayoutManager(this, 3)

        // Bind the periodic cache clear
        val clearCacheRequest = PeriodicWorkRequestBuilder<ClearCacheWorker>(INTERVAL, UNIT).build()
        val wm = WorkManager.getInstance(this)
        wm.enqueueUniquePeriodicWork(uniqueWorkName, WM_POLICY, clearCacheRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_actions_refresh -> {
                manualClearCache()
                true
            }
            R.id.menu_actions_test -> {
                this.startActivity(Intent(this, TestActivity::class.java))
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

    override fun onItemClick(position: Int, items: List<URL>) {
        val imageUrl = items[position].toString()
        val intent = Intent(this, FullScreenImageActivity::class.java)
        intent.putExtra("IMAGE_URL", imageUrl)
        startActivity(intent)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun manualClearCache() {
        val clearCacheWork = OneTimeWorkRequestBuilder<ClearCacheWorker>().build()
        WorkManager.getInstance(this).enqueue(clearCacheWork)
        adapter.notifyDataSetChanged() // Refresh the gallery
        Toast.makeText(this, "Cache cleared", Toast.LENGTH_LONG).show()
    }
}