package ch.heigvd.daa.labo5

import com.github.mikephil.charting.formatter.ValueFormatter

class ChartValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return "${value.toInt()} ms"
    }
}