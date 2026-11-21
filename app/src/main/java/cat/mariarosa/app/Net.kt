package cat.mariarosa.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** True when the phone has an internet connection, whether by Wi-Fi or by a SIM card's data. */
fun internetConnected(context: Context): Boolean = try {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    cm.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
} catch (_: Exception) {
    false
}
