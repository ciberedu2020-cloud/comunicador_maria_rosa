package cat.mariarosa.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/** True when the phone is connected to a Wi-Fi network (the only connection this app is meant to use). */
fun wifiConnected(context: Context): Boolean = try {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    cm.getNetworkCapabilities(cm.activeNetwork)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
} catch (_: Exception) {
    false
}
