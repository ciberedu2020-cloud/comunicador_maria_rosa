package cat.mariarosa.app

import android.content.Context
import org.json.JSONArray

/**
 * What an adult sets on the phone itself, in the programming screen, so the
 * app can be prepared without a computer: the contacts, the sound, the PIN.
 * It lives apart from config.json, which only holds what ships with the code.
 */
class Prefs(context: Context) {
    private val p = context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var soundOn: Boolean
        get() = p.getBoolean("sound", true)
        set(v) = p.edit().putBoolean("sound", v).apply()

    /** A bot token typed in the assistant; empty = use the one built into the app. */
    var token: String
        get() = p.getString("token", "") ?: ""
        set(v) = p.edit().putString("token", v.trim()).apply()

    fun effectiveToken(): String = token.ifBlank { BuildConfig.TELEGRAM_TOKEN }

    /** True once the first-time guide was finished or skipped: it is not offered again by itself. */
    var tutorialDone: Boolean
        get() = p.getBoolean("tutorialDone", false)
        set(v) = p.edit().putBoolean("tutorialDone", v).apply()

    /** The battery question is asked once, when the app is first installed. */
    var batteryAsked: Boolean
        get() = p.getBoolean("batteryAsked", false)
        set(v) = p.edit().putBoolean("batteryAsked", v).apply()

    /** "ca", "es", "en" or "fr" for the guide; empty = follow the phone's language. */
    var lang: String
        get() = p.getString("lang", "") ?: ""
        set(v) = p.edit().putString("lang", v).apply()

    /** Empty = no PIN: the programming screen opens with a tap. */
    var pin: String
        get() = p.getString("pin", "") ?: ""
        set(v) = p.edit().putString("pin", v).apply()

    /** The contacts saved on the phone, or null if none were ever saved (the files apply). */
    fun contacts(): List<Contact>? = try {
        p.getString("contacts", null)?.let { s ->
            val arr = JSONArray(s)
            (0 until arr.length()).map { Contact.fromJson(arr.getJSONObject(it)) }
        }
    } catch (e: Exception) {
        null
    }

    fun saveContacts(list: List<Contact>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        p.edit().putString("contacts", arr.toString()).apply()
    }

    /** The last person the bot did not know who wrote to it: offered in the programming screen. */
    data class Stranger(val chatId: Long, val name: String)

    var stranger: Stranger?
        get() = p.getString("stranger", null)?.let {
            val i = it.indexOf('|')
            if (i < 0) null else it.substring(0, i).toLongOrNull()?.let { id -> Stranger(id, it.substring(i + 1)) }
        }
        set(v) = p.edit().apply { if (v == null) remove("stranger") else putString("stranger", "${v.chatId}|${v.name}") }.apply()
}
