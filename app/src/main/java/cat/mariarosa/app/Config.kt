package cat.mariarosa.app

import android.content.Context
import org.json.JSONObject

/** The most people that fit on the screen with letters still readable. */
const val MAX_CONTACTS = 5

/** One person Maria Rosa can talk to. Inactive contacts are never shown. */
data class Contact(
    val id: String,
    val name: String,
    val active: Boolean,
    /** Telegram chat number of this person; null until they have been approved. */
    val telegramId: Long? = null,
    /** "la", "el", "l'" or "en": goes before the name ("Respon a la Maria"). Empty for none. */
    val article: String = "",
) {
    /** The name as said after "a": "la Maria", "l'Eloi", "en Joan". */
    val full: String get() = when {
        article.isEmpty() -> name
        article.endsWith("'") -> article + name
        else -> "$article $name"
    }

    /** The name as said after "de": "de la Maria", "de l'Eloi", "d'en Joan". */
    val ofFull: String get() = when {
        article == "en" -> "d'en $name"
        article.isEmpty() -> if (name.firstOrNull()?.lowercaseChar()?.let { it in "aeiouàèéíòóú" } == true) "d'$name" else "de $name"
        else -> "de $full"
    }

    /** The name at the start of a sentence: "La Maria", "L'Eloi", "En Joan". */
    val fullCapital: String get() = full.replaceFirstChar { it.uppercase() }

    fun toJson(): JSONObject = JSONObject()
        .put("id", id).put("name", name).put("active", active).put("article", article)
        .also { if (telegramId != null) it.put("telegramId", telegramId) }

    companion object {
        fun fromJson(c: JSONObject) = Contact(
            c.getString("id"),
            c.getString("name"),
            c.optBoolean("active", false),
            if (c.has("telegramId")) c.getLong("telegramId") else null,
            c.optString("article", ""),
        )
    }
}

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
                Contact.fromJson(arr.getJSONObject(it))
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
            val base = parse(context.assets.open(name).bufferedReader().use { it.readText() })
            // What was set on the phone itself (the programming screen) wins over the files.
            val onPhone = Prefs(context).contacts()
            if (onPhone != null) base.copy(contacts = onPhone) else base
        } catch (e: Exception) {
            Fallback
        }
    }
}
