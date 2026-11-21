package cat.mariarosa.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File

/** What the reply button reports after a recording: shown for a few seconds. */
enum class Notice { Sending, Sent, Failed, Discarded }

data class UiState(
    val pending: Map<String, Int> = emptyMap(),
    val playingContact: String? = null,
    val online: Boolean = true,
    val recording: Boolean = false,
    val recordSeconds: Int = 0,
    val notice: Notice? = null,
    /** Seconds left to cancel a recorded reply; null when nothing is waiting to be sent. */
    val countdown: Int? = null,
    /** The programming door stays open a few seconds after a recording is thrown away. */
    val doorOpen: Boolean = false,
)

/**
 * Everything that happens behind the screen: asks Telegram for new messages,
 * downloads the voice ones, sounds a soft chime once for each new one, plays
 * them, records and sends her replies, and tells the sender when something has
 * been heard.
 *
 * One instance for the whole process, so a rotated or recreated screen never
 * starts a second poller (Telegram refuses two at once).
 */
class Messenger private constructor(context: Context, @Volatile private var config: AppConfig) {
    private val app = context.applicationContext
    private val prefs = Prefs(app)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val client = TelegramClient(BuildConfig.TELEGRAM_TOKEN)
    private val inbox = Inbox(app)
    private val dir = File(app.filesDir, "voice").apply { mkdirs() }

    private val _state = MutableStateFlow(UiState(pending = inbox.counts()))
    val state: StateFlow<UiState> = _state

    private var player: MediaPlayer? = null
    private var lastTap = 0L
    private var lastReplyTap = 0L
    private var heardNew = false
    private var started = false

    private var recorder: MediaRecorder? = null
    private var recordFile: File? = null
    private var recordStartedAt = 0L
    private var recordJob: Job? = null
    private var noticeJob: Job? = null
    private var countdownJob: Job? = null
    private var pendingFile: File? = null
    private var pendingSend: Triple<Long, File, Int>? = null
    private var countdownStartedAt = 0L
    private var doorJob: Job? = null

    // The chime is our own sound, not the phone's notification sound: on some
    // phones (this Xiaomi) that setting is empty and the "chime" is silent.
    private var chimePlayer: MediaPlayer? = null

    private fun playChime() {
        // Never ring over her own recording.
        if (_state.value.recording || !prefs.soundOn) return
        try {
            stopChime()
            val mp = MediaPlayer()
            app.resources.openRawResourceFd(R.raw.chime).use {
                mp.setDataSource(it.fileDescriptor, it.startOffset, it.length)
            }
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            mp.setOnCompletionListener {
                it.release()
                if (chimePlayer === it) chimePlayer = null
            }
            mp.prepare()
            mp.start()
            chimePlayer = mp
        } catch (_: Exception) {
            // No sound is better than a crash: the button still turns green.
        }
    }

    /**
     * Turns the screen on for a few seconds when something arrives, so a
     * message is seen even if the screen had gone off. The deprecated flags
     * are the only way for an app to do this without being in front.
     */
    @Suppress("DEPRECATION")
    private fun wakeScreen() {
        try {
            val pm = app.getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "mariarosa:newMessage",
            ).acquire(10_000L)
        } catch (_: Exception) {
            // The chime and the green button still tell her.
        }
    }

    private fun stopChime() {
        chimePlayer?.runCatching { stop(); release() }
        chimePlayer = null
    }

    /** The programming screen changed the contacts: use them from now on. */
    fun reload(newConfig: AppConfig) {
        config = newConfig
    }

    fun start() {
        if (started) return
        started = true
        scope.launch(Dispatchers.IO) { pollLoop() }
    }

    // ---- listening: what the screen calls ----

    fun onTap(contactId: String) {
        if (_state.value.recording) return
        val now = SystemClock.elapsedRealtime()
        // Ignore a second touch right after the first: shaky fingers, double taps.
        if (now - lastTap < 700) return
        lastTap = now
        if (_state.value.playingContact != null) {
            stopPlayback(contactId)
            return
        }
        val path = inbox.next(contactId)
        if (path != null) {
            heardNew = true
            refreshCounts()
            play(contactId, path)
        } else {
            inbox.last(contactId)?.let { play(contactId, it) }
        }
    }

    // ---- playback ----

    private fun play(contactId: String, path: String) {
        stopChime()
        try {
            val mp = MediaPlayer()
            mp.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build(),
            )
            mp.setDataSource(path)
            mp.prepare()
            mp.setOnCompletionListener { onFinished(contactId) }
            mp.start()
            player = mp
            _state.value = _state.value.copy(playingContact = contactId)
        } catch (e: Exception) {
            // A damaged file must not leave her stuck: move on to the next one.
            releasePlayer()
            onFinished(contactId)
        }
    }

    private fun onFinished(contactId: String) {
        releasePlayer()
        val more = inbox.next(contactId)
        if (more != null) {
            refreshCounts()
            play(contactId, more)
            return
        }
        _state.value = _state.value.copy(playingContact = null)
        sendHeardReceipt(contactId)
    }

    private fun stopPlayback(contactId: String) {
        releasePlayer()
        _state.value = _state.value.copy(playingContact = null)
        // Cut short on purpose: the sender is still told it was heard.
        if (!inbox.hasPending(contactId)) sendHeardReceipt(contactId)
    }

    private fun sendHeardReceipt(contactId: String) {
        if (!heardNew) return
        heardNew = false
        val chatId = config.contacts.firstOrNull { it.id == contactId }?.telegramId ?: return
        scope.launch(Dispatchers.IO) {
            try { client.sendMessage(chatId, "✔ Ho ha escoltat") } catch (_: Exception) {}
        }
    }

    private fun releasePlayer() {
        player?.runCatching { stop(); release() }
        player = null
    }

    private fun refreshCounts() {
        _state.value = _state.value.copy(pending = inbox.counts())
    }

    // ---- replying: tap once to start recording, tap again to send ----

    fun onReplyTap(contactId: String) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastReplyTap < 700) return
        lastReplyTap = now
        when {
            _state.value.recording -> finishRecording(contactId)
            _state.value.notice == Notice.Sending || _state.value.countdown != null -> Unit
            else -> startRecording()
        }
    }

    private fun startRecording() {
        if (app.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            showNotice(Notice.Failed)
            return
        }
        releasePlayer()
        stopChime()
        val file = File(app.cacheDir, "reply_${System.currentTimeMillis()}.ogg")
        try {
            val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(app) else @Suppress("DEPRECATION") MediaRecorder()
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.OGG) // Ogg/Opus: what Telegram plays as a voice message
            r.setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
            r.setAudioSamplingRate(16_000)
            r.setAudioEncodingBitRate(32_000)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            recordFile = file
            recordStartedAt = SystemClock.elapsedRealtime()
            noticeJob?.cancel()
            _state.value = _state.value.copy(
                playingContact = null, recording = true, recordSeconds = 0, notice = null,
            )
            recordJob = scope.launch {
                while (true) {
                    delay(500)
                    val secs = ((SystemClock.elapsedRealtime() - recordStartedAt) / 1000).toInt()
                    _state.value = _state.value.copy(recordSeconds = secs)
                    // Two minutes is plenty; stops her leaving it recording by accident.
                    if (secs >= MAX_RECORD_SECONDS) {
                        config.activeContacts.firstOrNull()?.let { finishRecording(it.id) }
                        break
                    }
                }
            }
        } catch (e: Exception) {
            recorder?.runCatching { release() }
            recorder = null
            file.delete()
            showNotice(Notice.Failed)
        }
    }

    private fun finishRecording(contactId: String) {
        recordJob?.cancel()
        val r = recorder
        val file = recordFile
        val secs = ((SystemClock.elapsedRealtime() - recordStartedAt) / 1000).toInt()
        recorder = null
        recordFile = null
        _state.value = _state.value.copy(recording = false, recordSeconds = 0)
        var ok = true
        try { r?.stop() } catch (e: RuntimeException) { ok = false } // too short to record anything
        r?.runCatching { release() }
        if (!ok || file == null || secs < 1) {
            file?.delete()
            return
        }
        val chatId = config.contacts.firstOrNull { it.id == contactId }?.telegramId
        if (chatId == null) {
            file.delete()
            showNotice(Notice.Failed)
            return
        }
        queueSend(chatId, file, secs)
    }

    /**
     * Holds the recording for a few seconds before it is sent, so a wrong or
     * accidental one (a grandchild playing with it, say) can be thrown away.
     * Nothing leaves the phone until the count reaches zero.
     */
    private fun queueSend(chatId: Long, file: File, secs: Int) {
        val wait = config.sendDelaySeconds
        if (wait <= 0) {
            upload(chatId, file, secs)
            return
        }
        pendingFile = file
        pendingSend = Triple(chatId, file, secs)
        countdownStartedAt = SystemClock.elapsedRealtime()
        countdownJob?.cancel()
        countdownJob = scope.launch {
            for (n in wait downTo 1) {
                _state.value = _state.value.copy(countdown = n)
                delay(1_000)
            }
            _state.value = _state.value.copy(countdown = null)
            pendingFile = null
            pendingSend = null
            upload(chatId, file, secs)
        }
    }

    /**
     * Tapping the count itself: send now, no more waiting. Ignored for the
     * first moment, so the double tap that ended the recording cannot also send it.
     */
    fun onSendNow() {
        val send = pendingSend ?: return
        if (_state.value.countdown == null) return
        if (SystemClock.elapsedRealtime() - countdownStartedAt < 1_500) return
        countdownJob?.cancel()
        countdownJob = null
        pendingFile = null
        pendingSend = null
        _state.value = _state.value.copy(countdown = null)
        upload(send.first, send.second, send.third)
    }

    /** The X: throws the recording away instead of sending it. */
    fun onCancelSend(keepDoor: Boolean = true) {
        if (_state.value.countdown == null) return
        countdownJob?.cancel()
        countdownJob = null
        pendingFile?.delete()
        pendingFile = null
        pendingSend = null
        _state.value = _state.value.copy(countdown = null)
        showNotice(Notice.Discarded)
        if (keepDoor) {
            // The adult who threw the note away still has 3 seconds to reach the programming door.
            doorJob?.cancel()
            _state.value = _state.value.copy(doorOpen = true)
            doorJob = scope.launch {
                delay(3_000)
                _state.value = _state.value.copy(doorOpen = false)
            }
        }
    }

    private fun upload(chatId: Long, file: File, secs: Int) {
        _state.value = _state.value.copy(notice = Notice.Sending)
        scope.launch(Dispatchers.IO) {
            val result = try {
                client.sendVoice(chatId, file, secs)
                Notice.Sent
            } catch (e: Exception) {
                Notice.Failed
            } finally {
                file.delete()
            }
            scope.launch { showNotice(result) }
        }
    }

    /** Shows the result on the reply button for a few seconds, then back to normal. */
    private fun showNotice(notice: Notice) {
        noticeJob?.cancel()
        _state.value = _state.value.copy(notice = notice)
        noticeJob = scope.launch {
            delay(5_000)
            _state.value = _state.value.copy(notice = null)
        }
    }

    // ---- Telegram ----

    private suspend fun pollLoop() {
        var failures = 0
        while (true) {
            try {
                val updates = client.getUpdates(inbox.offset(), 25)
                for (i in 0 until updates.length()) {
                    val u = updates.getJSONObject(i)
                    handle(u)
                    inbox.setOffset(u.getLong("update_id") + 1)
                }
                failures = 0
                if (!_state.value.online) _state.value = _state.value.copy(online = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                failures++
                if (failures >= 3 && _state.value.online) _state.value = _state.value.copy(online = false)
                delay(5_000)
            }
        }
    }

    private fun handle(update: JSONObject) {
        val msg = update.optJSONObject("message") ?: return
        val chatId = msg.getJSONObject("chat").getLong("id")
        val contact = config.activeContacts.firstOrNull { it.telegramId == chatId }
        if (contact == null) {
            // Someone not approved: tell them their number, and keep it so the
            // programming screen can offer to add them with one tap.
            val from = msg.optJSONObject("from")?.optString("first_name", "").orEmpty()
            prefs.stranger = Prefs.Stranger(chatId, from.ifBlank { "Contacte" })
            runCatching {
                client.sendMessage(chatId, "Aquest aparell encara no et coneix. El teu identificador és $chatId.")
            }
            return
        }
        val voice = msg.optJSONObject("voice") ?: msg.optJSONObject("audio")
        if (voice == null) {
            if (!config.features.text) {
                runCatching {
                    client.sendMessage(
                        chatId,
                        "De moment aquest aparell només rep notes de veu. Mantén premut el micròfon per enviar-ne una.",
                    )
                }
            }
            return
        }
        val file = File(dir, "${msg.getLong("message_id")}_${System.currentTimeMillis()}.ogg")
        client.download(voice.getString("file_id"), file) // throws on failure: the update is retried
        inbox.add(contact.id, file.absolutePath)
        scope.launch {
            refreshCounts()
            wakeScreen()
            playChime()
        }
    }

    companion object {
        private const val MAX_RECORD_SECONDS = 120

        @Volatile private var instance: Messenger? = null

        fun get(context: Context): Messenger =
            instance ?: synchronized(this) {
                instance ?: Messenger(context, AppConfig.load(context)).also { instance = it }
            }
    }
}
