package ch.heigvd.daa.labo5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import ch.heigvd.daa.labo5.databinding.ActivityMainBinding
import ch.heigvd.daa.labo5.databinding.ActivityTestBinding
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
            buttonStartTest.setOnClickListener { setTestClickListener() }
        }
    }

    private fun setTestClickListener() {
        with(binding) {

            val nbDownloads = editTextNbImages.text.toString().toInt()
            if (nbDownloads < 0 || nbDownloads > 64) {
                editTextNbImages.error = "The quantity of images must be > 0 and < 64"
                return
            }

            buttonStartTest.isEnabled = false
            editTextNbImages.error = null
            val items = List(nbDownloads) { URL("${ENDPOINT}${it + 1}${FILE_EXT}") }

            // Show the progress bar and reset the status text view
            progressBar.max = PerformanceTester.dispatcherPairs.size
            progressBar.progress = 0

            // Start the test in a coroutine
            lifecycleScope.launch {
                val testResults = PerformanceTester.testDownloadPerformance(
                    items,
                    this,
                    lifecycleScope,
                    updateUI = { status -> binding.textViewStatus.text = status },
                    updateProgress = { progress -> progressBar.progress = progress }
                )
                buttonStartTest.isEnabled = true
                setupBarChart(testResults)
            }

        }
    }

    private fun setupBarChart(testResults: List<TestResult>) {
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        testResults.forEachIndexed { index, result ->
            entries.add(BarEntry(index.toFloat(), result.duration.toFloat()))
            labels.add(result.dispatcherName)
        }

        val dataSet = BarDataSet(entries, "Dispatcher Performance")
        dataSet.color = resources.getColor(R.color.main_theme)

        // Increase the value text size
        dataSet.valueTextSize = 12f

        //set custom valueFormatter
        dataSet.valueFormatter = ChartValueFormatter()

        val data = BarData(dataSet)

        with(binding) {
            barChart.data = data

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
}