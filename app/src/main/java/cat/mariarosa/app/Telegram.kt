package cat.mariarosa.app

import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * The few calls to Telegram's bot service that the app needs. Plain
 * HttpURLConnection, so there is no library to keep up to date.
 * Every method blocks: call them off the main thread.
 */
class TelegramClient(private val token: String) {

    private fun call(method: String, params: Map<String, String>, readTimeoutMs: Int): JSONObject {
        val body = params.entries.joinToString("&") {
            "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}"
        }
        val conn = URL("https://api.telegram.org/bot$token/$method").openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 10_000
            conn.readTimeout = readTimeoutMs
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.outputStream.use { it.write(body.toByteArray()) }
            val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
            val json = JSONObject(stream.bufferedReader().use { it.readText() })
            if (!json.optBoolean("ok")) throw IOException("Telegram: ${json.optString("description")}")
            return json
        } finally {
            conn.disconnect()
        }
    }

    /** Waits up to [timeoutSec] seconds for new messages, so it is cheap to call in a loop. */
    fun getUpdates(offset: Long, timeoutSec: Int): JSONArray =
        call(
            "getUpdates",
            mapOf("offset" to offset.toString(), "timeout" to timeoutSec.toString(), "allowed_updates" to "[\"message\"]"),
            (timeoutSec + 15) * 1000,
        ).getJSONArray("result")

    /** The bot's own user name, so the programming screen can say who to write to. */
    fun botUsername(): String =
        call("getMe", emptyMap(), 15_000).getJSONObject("result").optString("username")

    fun sendMessage(chatId: Long, text: String) {
        call("sendMessage", mapOf("chat_id" to chatId.toString(), "text" to text), 15_000)
    }

    /** Sends [file] (Ogg/Opus) as a voice message, so it shows with a play button in Telegram. */
    fun sendVoice(chatId: Long, file: File, durationSec: Int) {
        val boundary = "----mariarosa${System.currentTimeMillis()}"
        val conn = URL("https://api.telegram.org/bot$token/sendVoice").openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.connectTimeout = 10_000
            conn.readTimeout = 60_000
            conn.setChunkedStreamingMode(0)
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            conn.outputStream.use { out ->
                fun field(name: String, value: String) {
                    out.write("--$boundary\r\nContent-Disposition: form-data; name=\"$name\"\r\n\r\n$value\r\n".toByteArray())
                }
                field("chat_id", chatId.toString())
                field("duration", durationSec.toString())
                out.write(
                    ("--$boundary\r\nContent-Disposition: form-data; name=\"voice\"; filename=\"voice.ogg\"\r\n" +
                        "Content-Type: audio/ogg\r\n\r\n").toByteArray(),
                )
                file.inputStream().use { it.copyTo(out) }
                out.write("\r\n--$boundary--\r\n".toByteArray())
            }
            val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
            val json = JSONObject(stream.bufferedReader().use { it.readText() })
            if (!json.optBoolean("ok")) throw IOException("Telegram: ${json.optString("description")}")
        } finally {
            conn.disconnect()
        }
    }

    fun download(fileId: String, dest: File) {
        val path = call("getFile", mapOf("file_id" to fileId), 15_000)
            .getJSONObject("result").getString("file_path")
        val conn = URL("https://api.telegram.org/file/bot$token/$path").openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 10_000
            conn.readTimeout = 30_000
            if (conn.responseCode !in 200..299) throw IOException("Telegram: download failed")
            conn.inputStream.use { input -> dest.outputStream().use { input.copyTo(it) } }
        } finally {
            conn.disconnect()
        }
    }
}
