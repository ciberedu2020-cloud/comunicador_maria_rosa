package cat.mariarosa.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** After a restart, starts listening again without anyone opening the app. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            context.startForegroundService(Intent(context, MessengerService::class.java))
        }
    }
}
