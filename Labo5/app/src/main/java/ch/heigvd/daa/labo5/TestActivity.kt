package ch.heigvd.daa.labo5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import ch.heigvd.daa.labo5.databinding.ActivityMainBinding
import ch.heigvd.daa.labo5.databinding.ActivityTestBinding
import java.net.URL

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding

    companion object{
        const val PICTURES_NB = 16
        const val ENDPOINT = "https://daa.iict.ch/images/"
        const val FILE_EXT = ".jpg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityTestBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            editTextNbImages.setText(PICTURES_NB)
            buttonBack.setOnClickListener {
                finish()
            }
            buttonStartTest.setOnClickListener {
                val nbDownloads = editTextNbImages.text.toString().toInt()
                if (nbDownloads < 0 || nbDownloads > 32)
                    editTextNbImages.error = "The quantity of images must be > 0 and < 32"
                else
                    editTextNbImages.error = null

                val items = List(nbDownloads) { URL("${ENDPOINT}${it + 1}${FILE_EXT}") }
                val results = PerformanceTester.testDownloadPerformance(
                    items.take(nbDownloads),
                    lifecycleScope
                )
                PerformanceTester.showResultsDialog(results, nbDownloads, this@TestActivity)
            }
        }
    }
}