package cat.mariarosa.app

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** The article choices; the label is what an adult sees, the value is what is stored. */
private val Articles = listOf("cap" to "", "la" to "la", "el" to "el", "l'" to "l'", "en" to "en")

private class Draft(
    val id: String,
    name: String,
    article: String,
    telegramId: String,
    active: Boolean,
) {
    var name by mutableStateOf(name)
    var article by mutableStateOf(article)
    var telegramId by mutableStateOf(telegramId)
    var active by mutableStateOf(active)

    fun toContact() = Contact(id, name.trim(), active, telegramId.trim().toLongOrNull(), article)
}

private val Grey = Color(0xFF424242)

@Composable
private fun Heading(text: String) = Text(text, fontSize = 26.sp, color = Color.Black)

@Composable
private fun Body(text: String, color: Color = Grey) = Text(text, fontSize = 19.sp, color = color)

/** A big, full-width button: easy to hit and to read. */
@Composable
private fun Big(text: String, primary: Boolean = false, enabled: Boolean = true, onClick: () -> Unit) {
    val modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
    if (primary) Button(onClick = onClick, enabled = enabled, modifier = modifier) { Text(text, fontSize = 20.sp) }
    else OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier) { Text(text, fontSize = 18.sp) }
}

@Composable
private fun Card(background: Color = Color(0xFFF1F3F4), spacing: Dp = 10.dp, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(background).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) { content() }
}

/**
 * The programming screen: for the adult who sets the phone up, never for
 * Maria Rosa. Everything here is done on the phone, with no computer.
 * Read top to bottom: first how things stand (Wi-Fi, bot), then the people,
 * then the protections.
 */
@Composable
fun SettingsScreen(
    context: Context,
    initial: List<Contact>,
    onWifi: () -> Unit,
    onPin: () -> Unit,
    onUnpin: () -> Unit,
    onHome: () -> Unit,
    onTutorial: () -> Unit,
    onBattery: () -> Unit,
    onDone: () -> Unit,
) {
    val prefs = remember { Prefs(context) }
    val drafts = remember {
        mutableStateListOf<Draft>().apply {
            addAll(initial.map { Draft(it.id, it.name, it.article, it.telegramId?.toString() ?: "", it.active) })
        }
    }
    var sound by remember { mutableStateOf(prefs.soundOn) }
    var pin by remember { mutableStateOf(prefs.pin) }
    var botName by remember { mutableStateOf<String?>(null) }
    var botChecked by remember { mutableStateOf(false) }
    var stranger by remember { mutableStateOf(prefs.stranger) }
    var wifi by remember { mutableStateOf(internetConnected(context)) }
    var showLevels by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()
    // Opening the text must be visible: the screen scrolls down to show it.
    LaunchedEffect(showLevels) {
        if (showLevels) {
            delay(150)
            scroll.animateScrollTo(scroll.maxValue)
        }
    }

    LaunchedEffect(Unit) {
        botName = withContext(Dispatchers.IO) {
            runCatching { TelegramClient(prefs.effectiveToken()).botUsername() }.getOrNull()
        }
        botChecked = true
    }
    // Wi-Fi and newcomers are kept up to date while this screen is open.
    LaunchedEffect(Unit) {
        while (true) {
            wifi = internetConnected(context)
            stranger = prefs.stranger
            delay(2_000)
        }
    }

    val pinBad = pin.length in 1..3

    ProvideTextStyle(TextStyle(fontFamily = Atkinson)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scroll)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(S.settingsTitle, fontSize = 34.sp, color = Color.Black)
            Body(S.settingsIntro)
            Big(S.openGuide, primary = true, onClick = onTutorial)
            Heading(S.languageTitle)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Lang.all.forEach { (code, label) ->
                    FilterChip(selected = Lang.current == code, onClick = { Lang.set(context, code) }, label = { Text(label, fontSize = 16.sp) })
                }
            }
            if (S.autoTranslation.isNotEmpty()) Body(S.autoTranslation)

            // ---- How things stand ----
            Heading(S.nowTitle)
            Card {
                Body(
                    if (wifi) S.wifiOk else S.wifiNo,
                    color = if (wifi) Color(0xFF2E7D32) else Color(0xFFC62828),
                )
                if (!wifi) Big(S.connectWifi, onClick = onWifi) else Big(S.changeWifi, onClick = onWifi)
                Body(
                    when {
                        botName != null -> S.botOk(botName!!)
                        botChecked -> S.botPending
                        else -> S.botChecking
                    },
                    color = if (botName != null) Color(0xFF2E7D32) else Grey,
                )
            }

            // ---- The people ----
            Heading(S.contactsTitle)
            Body(S.contactsHint)
            stranger?.let { s ->
                if (drafts.none { it.telegramId.trim() == s.chatId.toString() }) {
                    Card(background = Color(0xFFFFF3CD)) {
                        Body(S.wroteBot(s.name, s.chatId), color = Color.Black)
                        Big(S.addHer, primary = true, enabled = drafts.size < MAX_CONTACTS) {
                            drafts.add(Draft("c${System.currentTimeMillis()}", s.name, "", s.chatId.toString(), true))
                            prefs.stranger = null
                            stranger = null
                        }
                    }
                }
            }
            drafts.forEachIndexed { i, d ->
                Card {
                    OutlinedTextField(
                        value = d.name, onValueChange = { d.name = it },
                        label = { Text(S.nameLabel) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    // The article is Catalan grammar ("la Maria"); other languages do not use it.
                    if (Lang.current == "ca") {
                        Body(S.articleLabel)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Articles.forEach { (label, value) ->
                                FilterChip(selected = d.article == value, onClick = { d.article = value }, label = { Text(if (value.isEmpty()) S.none else label, fontSize = 18.sp) })
                            }
                        }
                    }
                    Body(S.willShow(d.toContact()))
                    OutlinedTextField(
                        value = d.telegramId, onValueChange = { d.telegramId = it.filter(Char::isDigit) },
                        label = { Text(S.telegramNumber) }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Switch(checked = d.active, onCheckedChange = { d.active = it })
                        Body(if (d.active) S.visible else S.hidden, color = Color.Black)
                    }
                    Big(S.removeContact) { drafts.removeAt(i) }
                }
            }
            if (drafts.size < MAX_CONTACTS) {
                Big(S.addManual) {
                    drafts.add(Draft("c${System.currentTimeMillis()}", "", "", "", true))
                }
            } else {
                Body(S.maxContacts)
            }

            // ---- Sound ----
            Heading(S.soundTitle)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Switch(checked = sound, onCheckedChange = { sound = it; prefs.soundOn = it })
                Body(if (sound) S.soundOn else S.soundOff, color = Color.Black)
            }

            // ---- Protections ----
            Heading(S.protectTitle)
            Body(S.protectBody)
            OutlinedTextField(
                value = pin, onValueChange = { pin = it.filter(Char::isDigit).take(4) },
                label = { Text(S.pinField) }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.fillMaxWidth(),
            )
            if (pinBad) Body(S.pinMust4, color = Color(0xFFC62828))
            Body(S.pinNote)

            Heading(S.pinAppTitle)
            val w = wordsFor(Lang.current)
            Body(S.fixSettingsIntro)
            Big(w.btnPin, onClick = onPin)
            Big(S.unpin, onClick = onUnpin)
            Big(w.btnHome, onClick = onHome)
            Body(S.exitShort)
            Big(if (showLevels) "▲ ${w.levelsHide}" else "▼ ${w.levelsShow}") { showLevels = !showLevels }
            if (showLevels) Card(background = Color(0xFFE8EAF6)) { Body(w.fixLevels) }

            Heading(S.batteryTitle)
            Body(S.batteryBody)
            Big(S.batteryBtn, onClick = onBattery)

            Big(S.saveBack, primary = true, enabled = !pinBad) {
                prefs.saveContacts(drafts.map { it.toContact() }.filter { it.name.isNotEmpty() })
                prefs.pin = pin
                onDone()
            }
            Big(S.backNoSave, onClick = onDone)
        }
    }
}

