package cat.mariarosa.app

import android.content.ClipboardManager
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/** The words of the first-time guide in one language. %s marks where a name or number goes. */
private class Words(
    val skip: String, val back: String, val next: String, val finish: String,
    val welcomeTitle: String, val welcomeBody: String,
    val botTitle: String, val botSteps: String, val tokenLabel: String, val alreadyBot: String,
    val paste: String, val checking: String, val tokenOk: String, val tokenBad: String,
    val contactTitle: String, val contactSteps: String, val nameLabel: String,
    val articleLabel: String, val numberLabel: String, val wrote: String, val useIt: String,
    val save: String, val saved: String,
    val fixTitle: String, val fixBody: String, val btnHome: String, val btnPin: String, val fixNote: String,
    val doneTitle: String, val doneBody: String,
)

private val Ca = Words(
    skip = "Salta la guia", back = "Enrere", next = "Següent", finish = "Acaba",
    welcomeTitle = "Preparem l'aparell",
    welcomeBody = "Aquesta guia és opcional: serveix per deixar l'app a punt amb només aquest mòbil, " +
        "sense ordinador. Tens tres coses a fer: connectar un bot de Telegram, afegir la persona amb qui es parlarà " +
        "i, si vols, fixar l'app a la pantalla. Pots saltar-la ara i tornar-hi des de «Programació».",
    botTitle = "1. El bot de Telegram",
    botSteps = "A Telegram, busca @BotFather (el que té el segell blau de verificat).\n" +
        "Envia-li /newbot, tria un nom i un usuari que acabi en «bot».\n" +
        "Et donarà un token (una cadena llarga amb dos punts). Copia'l i enganxa'l aquí sota. No el mostris a ningú.",
    tokenLabel = "Token del bot", alreadyBot = "Aquest aparell ja té un bot. Si no en vols canviar, deixa la casella buida i toca «Següent».",
    paste = "Enganxa", checking = "Comprovant…", tokenOk = "Bot connectat: @%s", tokenBad = "No funciona: comprova que sigui el token sencer i que hi hagi internet.",
    contactTitle = "2. Qui hi parlarà",
    contactSteps = "Aquesta persona ha d'obrir Telegram, buscar el bot @%s, tocar Start i escriure «hola». " +
        "Quan ho faci, el seu nom i número apareixeran aquí sols. També pots escriure'ls a mà.",
    nameLabel = "Nom (surt al botó)", articleLabel = "Article davant del nom", numberLabel = "Número de Telegram",
    wrote = "Ha escrit: %s (%s)", useIt = "Fes-lo servir", save = "Desa aquest contacte", saved = "Contacte desat ✔",
    fixTitle = "3. Fixar l'app",
    fixBody = "Perquè l'app quedi sempre a la pantalla i no es tregui fàcilment. És opcional. " +
        "No és un bloqueig total (això requereix un mode especial), però dificulta que algú surti per error.",
    btnHome = "Que el botó d'inici torni a l'app", btnPin = "Fixa l'app a la pantalla",
    fixNote = "Per sortir-ne: a «Programació» hi ha «Deixa de fixar». Després d'un reinici s'ha de tornar a fixar.",
    doneTitle = "Tot a punt", doneBody = "Ja pots tancar la guia. Tot es pot canviar més endavant des de «Programació».",
)

private val Es = Words(
    skip = "Saltar la guía", back = "Atrás", next = "Siguiente", finish = "Terminar",
    welcomeTitle = "Preparamos el aparato",
    welcomeBody = "Esta guía es opcional: sirve para dejar la app lista solo con este móvil, sin ordenador. " +
        "Hay tres cosas: conectar un bot de Telegram, añadir a la persona con quien se hablará y, si quieres, " +
        "fijar la app en la pantalla. Puedes saltarla ahora y volver desde «Programación».",
    botTitle = "1. El bot de Telegram",
    botSteps = "En Telegram, busca @BotFather (el que tiene el sello azul de verificado).\n" +
        "Envíale /newbot, elige un nombre y un usuario que acabe en «bot».\n" +
        "Te dará un token (una cadena larga con dos puntos). Cópialo y pégalo aquí abajo. No se lo enseñes a nadie.",
    tokenLabel = "Token del bot", alreadyBot = "Este aparato ya tiene un bot. Si no quieres cambiarlo, deja la casilla vacía y toca «Siguiente».",
    paste = "Pegar", checking = "Comprobando…", tokenOk = "Bot conectado: @%s", tokenBad = "No funciona: comprueba que sea el token entero y que haya internet.",
    contactTitle = "2. Quién hablará",
    contactSteps = "Esa persona debe abrir Telegram, buscar el bot @%s, tocar Start y escribir «hola». " +
        "Cuando lo haga, su nombre y número aparecerán aquí solos. También puedes escribirlos a mano.",
    nameLabel = "Nombre (sale en el botón)", articleLabel = "Artículo delante del nombre", numberLabel = "Número de Telegram",
    wrote = "Ha escrito: %s (%s)", useIt = "Usar", save = "Guardar este contacto", saved = "Contacto guardado ✔",
    fixTitle = "3. Fijar la app",
    fixBody = "Para que la app quede siempre en la pantalla y no se salga fácilmente. Es opcional. " +
        "No es un bloqueo total (eso requiere un modo especial), pero dificulta salir por error.",
    btnHome = "Que el botón de inicio vuelva a la app", btnPin = "Fijar la app en la pantalla",
    fixNote = "Para salir: en «Programación» está «Dejar de fijar». Tras un reinicio hay que volver a fijarla.",
    doneTitle = "Todo listo", doneBody = "Ya puedes cerrar la guía. Todo se puede cambiar después desde «Programación».",
)

private val En = Words(
    skip = "Skip the guide", back = "Back", next = "Next", finish = "Finish",
    welcomeTitle = "Let's set up the phone",
    welcomeBody = "This guide is optional: it gets the app ready using only this phone, no computer. " +
        "There are three things: connect a Telegram bot, add the person who will be talking, and, if you wish, " +
        "pin the app to the screen. You can skip it now and come back from \"Programming\".",
    botTitle = "1. The Telegram bot",
    botSteps = "In Telegram, search for @BotFather (the one with the blue verified badge).\n" +
        "Send it /newbot, choose a name and a username ending in \"bot\".\n" +
        "It will give you a token (a long string with a colon). Copy it and paste it below. Do not show it to anyone.",
    tokenLabel = "Bot token", alreadyBot = "This phone already has a bot. To keep it, leave the box empty and tap \"Next\".",
    paste = "Paste", checking = "Checking…", tokenOk = "Bot connected: @%s", tokenBad = "It does not work: check it is the whole token and that there is internet.",
    contactTitle = "2. Who will talk",
    contactSteps = "That person must open Telegram, search for the bot @%s, tap Start and write \"hello\". " +
        "Their name and number will then appear here by themselves. You can also type them in.",
    nameLabel = "Name (shown on the button)", articleLabel = "Article before the name", numberLabel = "Telegram number",
    wrote = "Wrote: %s (%s)", useIt = "Use it", save = "Save this contact", saved = "Contact saved ✔",
    fixTitle = "3. Pin the app",
    fixBody = "So the app always stays on the screen and cannot easily be left. It is optional. " +
        "It is not a full lock (that needs a special mode), but it makes leaving by mistake harder.",
    btnHome = "Make the home button return to the app", btnPin = "Pin the app to the screen",
    fixNote = "To leave: \"Programming\" has \"Stop pinning\". After a restart it must be pinned again.",
    doneTitle = "All set", doneBody = "You can close the guide. Everything can be changed later from \"Programming\".",
)

private val Articles = listOf("–" to "", "la" to "la", "el" to "el", "l'" to "l'", "en" to "en")

private fun wordsFor(lang: String) = when (lang) { "es" -> Es; "en" -> En; else -> Ca }

private fun systemLang(): String = when (Locale.getDefault().language) { "es" -> "es"; "en" -> "en"; else -> "ca" }

/**
 * The first-time guide, optional and skippable at any step. It does what the
 * programming screen does, but explained step by step, so a phone can be
 * prepared with no computer.
 */
@Composable
fun TutorialScreen(
    context: Context,
    onApply: () -> Unit,
    onHome: () -> Unit,
    onPin: () -> Unit,
    onClose: () -> Unit,
) {
    val prefs = remember { Prefs(context) }
    var lang by remember { mutableStateOf(prefs.lang.ifEmpty { systemLang() }) }
    val w = wordsFor(lang)
    var step by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    var token by remember { mutableStateOf("") }
    var checking by remember { mutableStateOf(false) }
    var tokenError by remember { mutableStateOf(false) }
    var botName by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var article by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    var stranger by remember { mutableStateOf(prefs.stranger) }

    // The name of the bot in use, to tell the person which one to write to.
    LaunchedEffect(Unit) {
        botName = withContext(Dispatchers.IO) {
            runCatching { TelegramClient(prefs.effectiveToken()).botUsername() }.getOrNull()
        }
    }
    // Someone writing to the bot shows up here without leaving the screen.
    LaunchedEffect(Unit) {
        while (true) {
            stranger = prefs.stranger
            delay(2_000)
        }
    }

    fun close() {
        prefs.tutorialDone = true
        onClose()
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("ca" to "Català", "es" to "Español", "en" to "English").forEach { (code, label) ->
                    FilterChip(selected = lang == code, onClick = { lang = code; prefs.lang = code }, label = { Text(label) })
                }
            }
            TextButton(onClick = { close() }) { Text(w.skip) }
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when (step) {
                0 -> {
                    Text(w.welcomeTitle, fontSize = 30.sp, color = Color.Black)
                    Text(w.welcomeBody, fontSize = 19.sp, color = Color.DarkGray)
                }
                1 -> {
                    Text(w.botTitle, fontSize = 30.sp, color = Color.Black)
                    Text(w.botSteps, fontSize = 19.sp, color = Color.DarkGray)
                    if (botName != null) Text(w.alreadyBot, fontSize = 17.sp, color = Color.DarkGray)
                    OutlinedTextField(
                        value = token, onValueChange = { token = it.trim(); tokenError = false },
                        label = { Text(w.tokenLabel) }, singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedButton(onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        token = cm.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text?.toString()?.trim().orEmpty()
                        tokenError = false
                    }) { Text(w.paste) }
                    if (checking) Text(w.checking, fontSize = 17.sp, color = Color.DarkGray)
                    if (tokenError) Text(w.tokenBad, fontSize = 17.sp, color = Color.Red)
                }
                2 -> {
                    Text(w.contactTitle, fontSize = 30.sp, color = Color.Black)
                    Text(w.contactSteps.format(botName ?: "…"), fontSize = 19.sp, color = Color.DarkGray)
                    stranger?.let { s ->
                        Column(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF3CD)).padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(w.wrote.format(s.name, s.chatId), fontSize = 18.sp, color = Color.Black)
                            Button(onClick = { name = s.name; number = s.chatId.toString(); saved = false }) { Text(w.useIt) }
                        }
                    }
                    OutlinedTextField(
                        value = name, onValueChange = { name = it; saved = false },
                        label = { Text(w.nameLabel) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    )
                    Text(w.articleLabel, fontSize = 16.sp, color = Color.DarkGray)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Articles.forEach { (label, value) ->
                            FilterChip(selected = article == value, onClick = { article = value }, label = { Text(label) })
                        }
                    }
                    OutlinedTextField(
                        value = number, onValueChange = { number = it.filter(Char::isDigit); saved = false },
                        label = { Text(w.numberLabel) }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        enabled = name.isNotBlank() && number.isNotEmpty(),
                        onClick = {
                            val id = number.toLong()
                            val kept = AppConfig.load(context).contacts.filter { it.telegramId != null && it.telegramId != id }
                            prefs.saveContacts(kept + Contact("c${System.currentTimeMillis()}", name.trim(), true, id, article))
                            prefs.stranger = null
                            onApply()
                            saved = true
                        },
                    ) { Text(w.save) }
                    if (saved) Text(w.saved, fontSize = 18.sp, color = Color(0xFF2E7D32))
                }
                3 -> {
                    Text(w.fixTitle, fontSize = 30.sp, color = Color.Black)
                    Text(w.fixBody, fontSize = 19.sp, color = Color.DarkGray)
                    OutlinedButton(onClick = onHome) { Text(w.btnHome) }
                    OutlinedButton(onClick = onPin) { Text(w.btnPin) }
                    Text(w.fixNote, fontSize = 17.sp, color = Color.DarkGray)
                }
                else -> {
                    Text(w.doneTitle, fontSize = 30.sp, color = Color.Black)
                    Text(w.doneBody, fontSize = 19.sp, color = Color.DarkGray)
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (step > 0) OutlinedButton(onClick = { step-- }) { Text(w.back) }
            Button(
                enabled = !checking,
                modifier = Modifier.weight(1f),
                onClick = {
                    when {
                        step == 4 -> close()
                        step == 1 && token.isNotEmpty() -> {
                            // A mistyped token would silently break everything: check it before keeping it.
                            checking = true
                            scope.launch {
                                val user = withContext(Dispatchers.IO) {
                                    runCatching { TelegramClient(token).botUsername() }.getOrNull()
                                }
                                checking = false
                                if (user != null) {
                                    prefs.token = token
                                    prefs.stranger = null
                                    botName = user
                                    token = ""
                                    onApply()
                                    step++
                                } else {
                                    tokenError = true
                                }
                            }
                        }
                        else -> step++
                    }
                },
            ) { Text(if (step == 4) w.finish else w.next, fontSize = 20.sp) }
        }
    }
}
