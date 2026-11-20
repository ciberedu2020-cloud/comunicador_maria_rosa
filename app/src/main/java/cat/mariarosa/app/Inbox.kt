package cat.mariarosa.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * What has arrived and not yet been listened to, plus the last message
 * listened to (so a tap with nothing new plays it again). Kept in
 * SharedPreferences so a restart loses nothing.
 */
class Inbox(context: Context) {
    private val prefs = context.getSharedPreferences("inbox", Context.MODE_PRIVATE)

    @Synchronized fun offset(): Long = prefs.getLong("offset", 0L)

    @Synchronized fun setOffset(value: Long) {
        prefs.edit().putLong("offset", value).apply()
    }

    private fun pending(): MutableList<Pair<String, String>> {
        val arr = JSONArray(prefs.getString("pending", "[]"))
        return (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            o.getString("c") to o.getString("p")
        }.toMutableList()
    }

    private fun savePending(list: List<Pair<String, String>>) {
        val arr = JSONArray()
        list.forEach { arr.put(JSONObject().put("c", it.first).put("p", it.second)) }
        prefs.edit().putString("pending", arr.toString()).apply()
    }

    @Synchronized fun add(contactId: String, path: String) {
        savePending(pending().apply { add(contactId to path) })
    }

    @Synchronized fun counts(): Map<String, Int> =
        pending().groupingBy { it.first }.eachCount()

    @Synchronized fun hasPending(contactId: String): Boolean =
        pending().any { it.first == contactId }

    /** Takes the oldest unheard message; it becomes the "last" one, replacing the previous. */
    @Synchronized fun next(contactId: String): String? {
        val list = pending()
        val item = list.firstOrNull { it.first == contactId } ?: return null
        list.remove(item)
        savePending(list)
        prefs.getString("last_$contactId", null)?.let { java.io.File(it).delete() }
        prefs.edit().putString("last_$contactId", item.second).apply()
        return item.second
    }

    @Synchronized fun last(contactId: String): String? =
        prefs.getString("last_$contactId", null)?.takeIf { java.io.File(it).exists() }
}
