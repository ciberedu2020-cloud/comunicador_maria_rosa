package cat.mariarosa.app

import android.content.Context
import org.json.JSONObject

/** One person Maria Rosa can talk to. Inactive contacts are never shown. */
data class Contact(
    val id: String,
    val name: String,
    val active: Boolean,
    /** Telegram chat number of this person; null until they have been approved. */
    val telegramId: Long? = null,
)

/**
 * Each phase of the plan is a switch here. Going back a phase is turning a
 * switch off, not rebuilding the app.
 */
data class Features(
    val listen: Boolean,
    val reply: Boolean,
    val photos: Boolean,
    val text: Boolean,
)

data class AppConfig(
    val features: Features,
    val contacts: List<Contact>,
    /** Seconds she can still cancel a recorded reply before it is sent; 0 sends at once. */
    val sendDelaySeconds: Int = 5,
) {
    val activeContacts: List<Contact> get() = contacts.filter { it.active }

    companion object {
        /** Used if the file is missing or damaged: the smallest working setup. */
        val Fallback = AppConfig(
            Features(listen = true, reply = false, photos = false, text = false),
            emptyList(),
        )

        fun parse(json: String): AppConfig {
            val root = JSONObject(json)
            val f = root.getJSONObject("features")
            val features = Features(
                listen = f.optBoolean("listen", true),
                reply = f.optBoolean("reply", false),
                photos = f.optBoolean("photos", false),
                text = f.optBoolean("text", false),
            )
            val arr = root.getJSONArray("contacts")
            val contacts = (0 until arr.length()).map {
                val c = arr.getJSONObject(it)
                Contact(
                    c.getString("id"),
                    c.getString("name"),
                    c.optBoolean("active", false),
                    if (c.has("telegramId")) c.getLong("telegramId") else null,
                )
            }
            return AppConfig(features, contacts, root.optInt("sendDelaySeconds", 5))
        }

        /**
         * Reads config.local.json if it exists (a private copy, never published),
         * otherwise the config.json that ships with the code.
         */
        fun load(context: Context): AppConfig = try {
            val name = if (context.assets.list("")?.contains("config.local.json") == true) {
                "config.local.json"
            } else {
                "config.json"
            }
            parse(context.assets.open(name).bufferedReader().use { it.readText() })
        } catch (e: Exception) {
            Fallback
        }
    }
}
