package cat.mariarosa.app

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
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

/**
 * The programming screen: for the adult who sets the phone up, never for
 * Maria Rosa. Everything here is done on the phone, with no computer.
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
    var showLevels by remember { mutableStateOf(false) }
    var sound by remember { mutableStateOf(prefs.soundOn) }
    var pin by remember { mutableStateOf(prefs.pin) }
    var botName by remember { mutableStateOf<String?>(null) }
    var stranger by remember { mutableStateOf(prefs.stranger) }

    LaunchedEffect(Unit) {
        botName = withContext(Dispatchers.IO) {
            runCatching { TelegramClient(prefs.effectiveToken()).botUsername() }.getOrNull()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Programació", fontSize = 32.sp, color = Color.Black)
        OutlinedButton(onClick = onTutorial) { Text("Obre la guia de configuració") }
        Text(
            if (botName != null) "Bot de Telegram: @$botName. Qui hi vulgui parlar ha d'escriure «hola» a aquest bot i tocar Start."
            else "Bot de Telegram: pendent de configurar (o sense internet). Obre la guia de configuració.",
            fontSize = 18.sp,
            color = Color.DarkGray,
        )

        stranger?.let { s ->
            if (drafts.none { it.telegramId.trim() == s.chatId.toString() }) {
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF3CD)).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("Ha escrit al bot: ${s.name} (${s.chatId})", fontSize = 18.sp, color = Color.Black)
                    Button(onClick = {
                        drafts.add(Draft("c${System.currentTimeMillis()}", s.name, "", s.chatId.toString(), true))
                        prefs.stranger = null
                        stranger = null
                    }) { Text("Afegeix-la com a contacte") }
                }
            }
        }

        Text("Contactes", fontSize = 24.sp, color = Color.Black)
        drafts.forEachIndexed { i, d ->
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFF1F3F4)).padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = d.name, onValueChange = { d.name = it },
                    label = { Text("Nom (surt als botons)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
                Text("Article davant del nom", fontSize = 16.sp, color = Color.DarkGray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Articles.forEach { (label, value) ->
                        FilterChip(selected = d.article == value, onClick = { d.article = value }, label = { Text(label) })
                    }
                }
                Text("Es llegirà: «Respon a ${d.toContact().full}»", fontSize = 16.sp, color = Color.DarkGray)
                OutlinedTextField(
                    value = d.telegramId, onValueChange = { d.telegramId = it.filter(Char::isDigit) },
                    label = { Text("Número de Telegram") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Switch(checked = d.active, onCheckedChange = { d.active = it })
                    Text(if (d.active) "Visible" else "Amagat", fontSize = 18.sp, color = Color.Black)
                }
                OutlinedButton(onClick = { drafts.removeAt(i) }) { Text("Treu aquest contacte") }
            }
        }
        if (drafts.size < MAX_CONTACTS) {
            OutlinedButton(onClick = { drafts.add(Draft("c${System.currentTimeMillis()}", "", "", "", true)) }) {
                Text("Afegeix un contacte")
            }
        }

        Text("So", fontSize = 24.sp, color = Color.Black)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Switch(checked = sound, onCheckedChange = { sound = it; prefs.soundOn = it })
            Text(if (sound) "Sona l'avís quan arriba un missatge" else "Sense avís sonor", fontSize = 18.sp, color = Color.Black)
        }

        Text("PIN per entrar aquí", fontSize = 24.sp, color = Color.Black)
        OutlinedTextField(
            value = pin, onValueChange = { pin = it.filter(Char::isDigit).take(6) },
            label = { Text("Buit = sense PIN") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "Si l'oblides, només es pot esborrar reinstal·lant l'app (es perd aquesta programació).",
            fontSize = 16.sp, color = Color.DarkGray,
        )

        Text("Fixar l'app", fontSize = 24.sp, color = Color.Black)
        Text(Ca.fixIntro, fontSize = 16.sp, color = Color.DarkGray)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPin) { Text("Fixa l'app") }
            OutlinedButton(onClick = onUnpin) { Text("Deixa de fixar") }
        }
        OutlinedButton(onClick = { showLevels = !showLevels }) { Text(if (showLevels) Ca.levelsHide else Ca.levelsShow) }
        if (showLevels) Text(Ca.fixLevels, fontSize = 16.sp, color = Color.DarkGray)
        OutlinedButton(onClick = onHome) { Text("Que el botó d'inici torni a l'app") }
        OutlinedButton(onClick = onBattery) { Text("Torna a demanar l'exempció de bateria") }
        OutlinedButton(onClick = onWifi) { Text("Obre els ajustos de Wi-Fi") }

        Button(onClick = {
            prefs.saveContacts(drafts.map { it.toContact() }.filter { it.name.isNotEmpty() })
            prefs.pin = pin
            onDone()
        }, modifier = Modifier.fillMaxWidth()) { Text("Desa i torna", fontSize = 22.sp) }
        OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) { Text("Torna sense desar") }
    }
}

private const val MAX_CONTACTS = 4
