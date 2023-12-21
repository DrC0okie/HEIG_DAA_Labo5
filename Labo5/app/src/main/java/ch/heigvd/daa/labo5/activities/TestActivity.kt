package ch.heigvd.daa.labo5.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import ch.heigvd.daa.labo5.utils.ChartValueFormatter
import ch.heigvd.daa.labo5.R
import ch.heigvd.daa.labo5.utils.TestResult
import ch.heigvd.daa.labo5.databinding.ActivityTestBinding
import ch.heigvd.daa.labo5.utils.Network.isNetworkAvailable
import ch.heigvd.daa.labo5.utils.PerformanceTester
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch
import java.net.URL

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding

    companion object {
        const val PICTURES_NB = 16
        const val MAX_DOWNLOAD = 128
        const val ENDPOINT = "https://daa.iict.ch/images/"
        const val FILE_EXT = ".jpg"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)

        with(binding) {
            setContentView(root)
            editTextNbImages.setText(PICTURES_NB.toString())
            buttonBack.setOnClickListener { finish() }
            buttonStartTest.setOnClickListener { launchTests() }
        }
    }

    private fun launchTests() {
        if (!isNetworkAvailable(this)){
            Toast.makeText(this, "No internet connection available", Toast.LENGTH_LONG).show()
            return
        }

        with(binding) {
            val nbDownloads = editTextNbImages.text.toString().toInt()

            // Validate the number of downloads
            if(! isInputValid(nbDownloads))
                return

            // Disable the test button to prevent multiple clicks during test execution
            buttonStartTest.isEnabled = false
            editTextNbImages.error = null

            // Prepare the list of URLs for the images to be downloaded
            val items = List(nbDownloads) { URL("$ENDPOINT${it + 1}$FILE_EXT") }

            // Initialize the progress bar
            progressBar.apply { max = PerformanceTester.dispatcherPairs.size; progress = 0 }

            // Start the test in a coroutine
            lifecycleScope.launch {
                val testResults = PerformanceTester.testDispatcherPerformance(
                    items,
                    this,
                    lifecycleScope,
                    updateUI = { status -> binding.textViewStatus.text = status },
                    updateProgress = { progress -> progressBar.progress = progress }
                )

                // Re-enable the test button and setup the bar chart
                buttonStartTest.isEnabled = true
                setupBarChart(testResults)
            }
        }
    }

    private fun setupBarChart(testResults: List<TestResult>) {
        val entries = ArrayList<BarEntry>() // List to hold bar entries
        val labels = ArrayList<String>() // List to hold axis labels

        testResults.forEachIndexed { index, result ->
            entries.add(BarEntry(index.toFloat(), result.duration.toFloat()))
            labels.add(result.dispatcherName)
        }

        // Create a dataset and Set the text size, the bars color and the formatter
        BarDataSet(entries, "Dispatcher Performance").apply {
            valueTextSize = 12f
            color = ContextCompat.getColor(this@TestActivity, R.color.main_theme)
            valueFormatter = ChartValueFormatter()
        }.also{binding.barChart.data = BarData(it)}

        with(binding) {
            // Configure the X-axis
            val xAxis = barChart.xAxis
            xAxis.granularity = 1f
            xAxis.labelCount = labels.size
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)

            // Other chart configurations
            barChart.axisRight.isEnabled = false
            barChart.legend.isEnabled = false
            barChart.description.text = ""
            barChart.invalidate() // refresh
        }
    }

    private fun isInputValid(nbDownloads: Int):Boolean{
        if (nbDownloads < 0 || nbDownloads > MAX_DOWNLOAD) {
            binding.editTextNbImages.error = "The image quantity must be > 0 and < $MAX_DOWNLOAD"
            return false
        }
        return true
    }
}