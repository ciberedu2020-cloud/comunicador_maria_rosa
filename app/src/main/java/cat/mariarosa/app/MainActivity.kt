package cat.mariarosa.app

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.media.AudioManager
import android.os.Bundle
import android.os.SystemClock
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
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

internal val Atkinson = FontFamily(
    Font(R.font.atkinson_hyperlegible_regular),
    Font(R.font.atkinson_hyperlegible_bold, FontWeight.Bold),
)

private enum class Screen { Home, Settings, Tutorial }

class MainActivity : ComponentActivity() {
    private var config by mutableStateOf(AppConfig.Fallback)
    private var screen by mutableStateOf(Screen.Home)
    private var askPin by mutableStateOf(false)
    private var pinned = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The screen is a mailbox: it stays on, and the volume is high so the
        // chime and the voice are heard whatever the volume was left at.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        (getSystemService(AUDIO_SERVICE) as AudioManager).apply {
            setStreamVolume(AudioManager.STREAM_MUSIC, getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)
            setStreamVolume(AudioManager.STREAM_ALARM, getStreamMaxVolume(AudioManager.STREAM_ALARM), 0)
        }
        Lang.init(this)
        config = AppConfig.load(this)
        val prefs = Prefs(this)
        // The very first time after installing: the guide, which can be skipped
        // (and then is not offered again by itself). Without it nobody would know how to set the app up.
        if (!prefs.tutorialDone) screen = Screen.Tutorial
        // Shown over the lock screen and able to light the screen: it is a
        // mailbox that sits on a table, not a phone in a pocket.
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        // Permissions are asked once, the first time they are needed; the
        // battery question follows, so two system windows never overlap.
        val askPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            askBatteryExemption()
        }
        val askHome = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
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

        val applyConfig = {
            config = AppConfig.load(this)
            messenger.reload(config)
        }
        val chooseHome = {
            // Makes the home button come back to this app.
            try {
                val rm = getSystemService(RoleManager::class.java)
                if (rm.isRoleAvailable(RoleManager.ROLE_HOME) && !rm.isRoleHeld(RoleManager.ROLE_HOME)) {
                    askHome.launch(rm.createRequestRoleIntent(RoleManager.ROLE_HOME))
                }
            } catch (_: Exception) {
                try { startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) } catch (_: Exception) {}
            }
        }
        setContent {
            val state by messenger.state.collectAsState()
            when (screen) {
                Screen.Tutorial -> androidx.compose.material3.ProvideTextStyle(
                    androidx.compose.ui.text.TextStyle(fontFamily = Atkinson),
                ) {
                    TutorialScreen(
                        context = this,
                        onApply = applyConfig,
                        onHome = chooseHome,
                        onPin = ::pinScreen,
                        onWifi = ::openWifi,
                    ) { screen = Screen.Home }
                }
                Screen.Settings -> SettingsScreen(
                    context = this,
                    initial = AppConfig.load(this).contacts,
                    onWifi = ::openWifi,
                    onPin = ::pinScreen,
                    onUnpin = ::unpinScreen,
                    onHome = chooseHome,
                    onTutorial = { screen = Screen.Tutorial },
                    onBattery = { askBatteryExemption(force = true) },
                ) {
                    applyConfig()
                    screen = Screen.Home
                }
                Screen.Home -> Home(
                    config, state, messenger::onTap, messenger::onReplyTap, messenger::onCancelSend,
                    messenger::onSendNow,
                ) {
                    if (prefs.pin.isEmpty()) openSettings(messenger) else askPin = true
                }
            }
            if (askPin) PinDialog(prefs.pin, onClose = { askPin = false }) {
                askPin = false
                openSettings(messenger)
            }
        }
    }

    private var leftAt = 0L

    override fun onStop() {
        super.onStop()
        leftAt = SystemClock.elapsedRealtime()
        askPin = false
    }

    /**
     * Leaving the app (recents, screen off) must not leave the programming
     * screen open for whoever comes next. With a PIN set, it closes at once;
     * the guide gets ten minutes, because filling it in means going to Telegram and back.
     */
    override fun onStart() {
        super.onStart()
        if (leftAt == 0L || Prefs(this).pin.isEmpty()) return
        when (screen) {
            Screen.Settings -> screen = Screen.Home
            Screen.Tutorial -> if (SystemClock.elapsedRealtime() - leftAt > 10 * 60_000L) screen = Screen.Home
            Screen.Home -> Unit
        }
    }

    /** Keeps this app on the screen; Android asks the person to confirm. Home and recents stop working. */
    private fun pinScreen() {
        try { startLockTask(); pinned = true } catch (_: Exception) {}
    }

    private fun unpinScreen() {
        try { stopLockTask() } catch (_: Exception) {}
        pinned = false
    }

    /** Pinned apps cannot open other screens, so the Wi-Fi settings need the pin lifted first. */
    private fun openWifi() {
        unpinScreen()
        try { startActivity(Intent(Settings.ACTION_WIFI_SETTINGS)) } catch (_: Exception) {}
    }

    /** A recording waiting to be sent is thrown away: the adult is here to program, not to talk. */
    private fun openSettings(messenger: Messenger) {
        messenger.onCancelSend(keepDoor = false)
        screen = Screen.Settings
    }

    /** Asks Android not to put the app to sleep to save battery. Once, at install; again only on request. */
    private fun askBatteryExemption(force: Boolean = false) {
        val prefs = Prefs(this)
        if (!force && prefs.batteryAsked) return
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        if (pm.isIgnoringBatteryOptimizations(packageName)) { prefs.batteryAsked = true; return }
        prefs.batteryAsked = true
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
    onSendNow: () -> Unit,
    onSettings: () -> Unit,
) {
    val contacts = config.activeContacts
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (state.countdown != null || state.doorOpen) {
            // Only while a reply waits to be sent (and 3 s after it is thrown away): a small door for the adult, out of the way.
            Text(
                S.programmingDoor,
                color = Color.LightGray,
                fontFamily = Atkinson,
                fontSize = 18.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().clickable { onSettings() }.padding(vertical = 4.dp),
            )
        }
        if (contacts.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(S.noContacts, color = Color.White, fontFamily = Atkinson, fontSize = 32.sp)
            }
        }
        contacts.forEach { contact ->
            val waiting = state.pending[contact.id] ?: 0
            val playing = state.playingContact == contact.id
            val (colour, title, subtitle) = when {
                playing -> Triple(Blue, S.listening, S.tapToStop)
                waiting > 0 -> Triple(Green, S.newMessageFrom(contact), S.tapToListen)
                contacts.size == 1 -> Triple(Yellow, S.hello, S.noNewMessage)
                else -> Triple(Yellow, if (Lang.current == "ca") contact.fullCapital else contact.name, S.noNewMessage)
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
                // Recording, counting and results belong to ONE contact: only her buttons change.
                val mine = state.replyContact == contact.id
                val recording = mine && state.recording
                val notice = if (mine) state.notice else null
                val (rColour, rTitle, rSubtitle) = when {
                    recording -> Triple(
                        Red,
                        S.recording("${state.recordSeconds / 60}:${"%02d".format(state.recordSeconds % 60)}"),
                        S.tapToSend,
                    )
                    notice == Notice.Sending -> Triple(Blue, S.sending, "")
                    notice == Notice.Sent -> Triple(Green, S.sent, S.willReceive(contact))
                    notice == Notice.Failed -> Triple(Orange, S.sendFailed, S.tryAgain)
                    notice == Notice.Discarded -> Triple(Light, S.discarded, S.notSent)
                    else -> Triple(Light, S.replyTo(contact), S.tapAndSpeak)
                }
                val countdown = if (mine) state.countdown else null
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
                                .background(Orange)
                                .clickable { onSendNow() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                                Text(
                                    S.sendingIn(countdown),
                                    color = Color.Black,
                                    fontFamily = Atkinson,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    S.sendNowHint,
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
                S.noInternet,
                color = Color.White,
                fontFamily = Atkinson,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Asks for the PIN before the programming screen. */
@Composable
private fun PinDialog(pin: String, onClose: () -> Unit, onOk: () -> Unit) {
    var typed by androidx.compose.runtime.remember { mutableStateOf("") }
    var wrong by androidx.compose.runtime.remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(S.pin) },
        text = {
            Column {
                OutlinedTextField(
                    value = typed,
                    onValueChange = { typed = it.filter(Char::isDigit); wrong = false },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                )
                if (wrong) {
                    Spacer(Modifier.height(8.dp))
                    Text(S.pinWrong, color = Color.Red)
                }
            }
        },
        confirmButton = { TextButton(onClick = { if (typed == pin) onOk() else wrong = true }) { Text(S.enter) } },
        dismissButton = { TextButton(onClick = onClose) { Text(S.cancel) } },
    )
}
