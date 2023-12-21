package ch.heigvd.daa.labo5.utils

/**
 * Represents the result of a performance test for a specific dispatcher.
 *
 * This data class holds the name of the dispatcher and the duration it took to complete a set of tasks.
 * @property dispatcherName The name of the dispatcher used for the test (e.g., "IO", "4 Threads").
 * @property duration The time taken (in milliseconds) to complete the tasks using the specified dispatcher.
 * @author Timothée Van Hove, Léo Zmoos
 */
data class TestResult(val dispatcherName: String, val duration: Long)