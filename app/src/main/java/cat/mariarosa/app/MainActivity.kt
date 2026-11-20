package cat.mariarosa.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.media.AudioManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Atkinson = FontFamily(
    Font(R.font.atkinson_hyperlegible_regular),
    Font(R.font.atkinson_hyperlegible_bold, FontWeight.Bold),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The screen is a mailbox: it stays on, and the volume is high so the
        // chime and the voice are heard whatever the volume was left at.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        (getSystemService(AUDIO_SERVICE) as AudioManager).apply {
            setStreamVolume(AudioManager.STREAM_MUSIC, getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)
            setStreamVolume(AudioManager.STREAM_ALARM, getStreamMaxVolume(AudioManager.STREAM_ALARM), 0)
        }
        val config = AppConfig.load(this)
        // Shown over the lock screen and able to light the screen: it is a
        // mailbox that sits on a table, not a phone in a pocket.
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        // Permissions are asked once, the first time they are needed; the
        // battery question follows, so two system windows never overlap.
        val askPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            askBatteryExemption()
        }
        val wanted = buildList {
            if (config.features.reply &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
            ) add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (wanted.isNotEmpty()) askPermissions.launch(wanted.toTypedArray()) else askBatteryExemption()
        // Keeps listening when the screen goes off or this window is covered.
        startForegroundService(Intent(this, MessengerService::class.java))
        val messenger = Messenger.get(this)
        messenger.start()
        setContent {
            val state by messenger.state.collectAsState()
            Home(config, state, messenger::onTap, messenger::onReplyTap, messenger::onCancelSend)
        }
    }

    /** Asks Android not to put the app to sleep to save battery. Asked until it is granted. */
    private fun askBatteryExemption() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (pm.isIgnoringBatteryOptimizations(packageName)) return
        try {
            startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName")),
            )
        } catch (_: Exception) {
        }
    }
}

private val Yellow = Color(0xFFFFD54F)
private val Green = Color(0xFF66BB6A)
private val Blue = Color(0xFF64B5F6)
private val Red = Color(0xFFEF5350)
private val Orange = Color(0xFFFFB74D)
private val Light = Color(0xFFECEFF1)

/** One big button per active contact. Its colour and words say what is happening. */
@Composable
private fun Home(
    config: AppConfig,
    state: UiState,
    onTap: (String) -> Unit,
    onReply: (String) -> Unit,
    onCancel: () -> Unit,
) {
    val contacts = config.activeContacts
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (contacts.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Sense contactes", color = Color.White, fontFamily = Atkinson, fontSize = 32.sp)
            }
        }
        contacts.forEach { contact ->
            val waiting = state.pending[contact.id] ?: 0
            val playing = state.playingContact == contact.id
            val (colour, title, subtitle) = when {
                playing -> Triple(Blue, "Escoltant…", "Toca per parar")
                waiting > 0 -> Triple(Green, "Missatge nou de ${contact.name}", "Toca aquí per escoltar")
                else -> Triple(Yellow, contact.name, "Cap missatge nou")
            }
            Box(
                modifier = Modifier
                    .weight(if (config.features.reply) 1.3f else 1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(colour)
                    .clickable { onTap(contact.id) },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Text(
                        title,
                        color = Color.Black,
                        fontFamily = Atkinson,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp,
                        textAlign = TextAlign.Center,
                    )
                    Text(subtitle, color = Color.Black, fontFamily = Atkinson, fontSize = 28.sp, textAlign = TextAlign.Center)
                }
            }
            if (config.features.reply) {
                val (rColour, rTitle, rSubtitle) = when {
                    state.recording -> Triple(
                        Red,
                        "Gravant… ${state.recordSeconds / 60}:${"%02d".format(state.recordSeconds % 60)}",
                        "Toca per enviar",
                    )
                    state.notice == Notice.Sending -> Triple(Blue, "Enviant…", "")
                    state.notice == Notice.Sent -> Triple(Green, "Enviat ✔", "${contact.name} ho rebrà")
                    state.notice == Notice.Failed -> Triple(Orange, "No s'ha pogut enviar", "Torna-ho a provar")
                    state.notice == Notice.Discarded -> Triple(Light, "Esborrat", "No s'ha enviat")
                    else -> Triple(Light, "Respon a ${contact.name}", "Toca i parla")
                }
                val countdown = state.countdown
                if (countdown != null) {
                    // A recording is waiting: it goes by itself, or the X throws it away.
                    Row(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Orange),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "S'enviarà en $countdown",
                                    color = Color.Black,
                                    fontFamily = Atkinson,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    "Toca la X per esborrar-lo",
                                    color = Color.Black,
                                    fontFamily = Atkinson,
                                    fontSize = 24.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp))
                                .background(Red)
                                .clickable { onCancel() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("✕", color = Color.Black, fontFamily = Atkinson, fontWeight = FontWeight.Bold, fontSize = 90.sp)
                        }
                    }
                } else
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(rColour)
                        .clickable { onReply(contact.id) },
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                        Text(
                            rTitle,
                            color = Color.Black,
                            fontFamily = Atkinson,
                            fontWeight = FontWeight.Bold,
                            fontSize = 40.sp,
                            textAlign = TextAlign.Center,
                        )
                        if (rSubtitle.isNotEmpty()) {
                            Text(rSubtitle, color = Color.Black, fontFamily = Atkinson, fontSize = 26.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
        if (!state.online) {
            Text(
                "Sense connexió a internet",
                color = Color.White,
                fontFamily = Atkinson,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
