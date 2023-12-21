package ch.heigvd.daa.labo5.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Singleton to check network connectivity status.
 *
 * Provides utility function to determine whether there is an active network connection.
 * @author Timothée Van Hove, Léo Zmoos
 */
object Network {

    /**
     * Assesses the current network state of the device using the ConnectivityManager.
     * @param context The context used to access system services.
     * @return true if connected to WiFi, cellular, or Ethernet network, false otherwise.
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = manager.activeNetwork ?: return false
        val actNw = manager.getNetworkCapabilities(capabilities) ?: return false

        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}