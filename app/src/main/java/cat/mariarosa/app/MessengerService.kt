package cat.mariarosa.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder

/**
 * Keeps the app alive in the background so messages arrive even when the
 * screen is off or another app is in front. The poller itself lives in
 * [Messenger]; this service only holds the process up and shows the (quiet)
 * notification Android requires for that.
 */
class MessengerService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "Connexió", NotificationManager.IMPORTANCE_LOW),
        )
        val notification = Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Comunicador Maria Rosa")
            .setContentText("Esperant missatges")
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }
        Messenger.get(this).start()
        // If Android ever stops it, it asks to start again.
        return START_STICKY
    }

    private companion object {
        const val CHANNEL = "connexio"
    }
}
