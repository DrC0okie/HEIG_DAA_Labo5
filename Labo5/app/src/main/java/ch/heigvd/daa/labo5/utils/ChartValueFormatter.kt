package ch.heigvd.daa.labo5.utils

import com.github.mikephil.charting.formatter.ValueFormatter


/**
 * A custom formatter for chart values in the application.
 *
 * This class extends [ValueFormatter] and is used to format the floating-point values
 * in chart specifically for displaying the values with 'ms' (milliseconds) as the unit.
 * @author Timothée Van Hove, Léo Zmoos
 */
class ChartValueFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        return "${value.toInt()} ms"
    }
}