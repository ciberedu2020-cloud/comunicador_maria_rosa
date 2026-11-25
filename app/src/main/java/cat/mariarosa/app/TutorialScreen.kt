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
import androidx.compose.ui.Alignment
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

/** The words of the first-time guide in one language. %s marks where a name or number goes. */
internal class Words(
    val skip: String, val back: String, val next: String, val finish: String,
    val welcomeTitle: String, val welcomeBody: String,
    val wifiTitle: String, val wifiBody: String, val wifiOn: String, val wifiOff: String, val wifiBtn: String,
    val botTitle: String, val botSteps: String, val tokenLabel: String, val alreadyBot: String,
    val paste: String, val checking: String, val tokenBad: String,
    val contactTitle: String, val contactSteps: String, val botRef: String, val haveContacts: String, val nameLabel: String,
    val articleLabel: String, val numberLabel: String, val wrote: String, val useIt: String,
    val save: String, val saved: String,
    val pinTitle: String, val pinBody: String, val pinLabel: String, val pinNote: String,
    val fixTitle: String, val fixIntro: String, val btnPin: String, val fixExit: String,
    val doneTitle: String, val doneBody: String,
)

internal val Ca = Words(
    skip = "Salta la guia", back = "Enrere", next = "Següent", finish = "Acaba",
    welcomeTitle = "Preparem l'aparell",
    welcomeBody = "Aquesta app és per a persones a qui costa fer servir el mòbil: permet parlar per veu amb unes poques persones.\n\nAquesta guia ajuda un adult a deixar-ho tot a punt, només amb aquest mòbil. Els passos són: internet, el bot de Telegram, qui hi parlarà, un PIN opcional i fixar l'app. Pots saltar-la i tornar-hi des de «Programació».",
    wifiTitle = "1. Internet",
    wifiBody = "Aquest mòbil necessita internet per rebre i enviar missatges. El més senzill és el Wi-Fi de casa; si no n'hi ha, també va amb una targeta SIM amb dades (no cal per trucar). Sense internet no funciona. Comprova que estigui connectat o connecta'l ara.",
    wifiOn = "Connectat a internet ✔", wifiOff = "Encara no està connectat a internet", wifiBtn = "Obre els ajustos de Wi-Fi",
    botTitle = "2. El bot de Telegram",
    botSteps = "Necessites tenir instal·lada l'aplicació Telegram en algun mòbil per poder preparar això. " +
        "Cada aparell té el seu propi bot, que crees tu.\n\n" +
        "1. A Telegram, busca @BotFather (el que té el segell blau de verificat) i obre'l.\n" +
        "2. Envia-li un missatge amb aquest text: /newbot\n" +
        "3. Et demanarà un nom per al bot: escriu el que vulguis (per exemple, «Casa de la Maria Rosa»).\n" +
        "4. Després et demanarà un nom d'usuari, que ha d'acabar en «bot» (per exemple, casamariarosa_bot). " +
        "Si algú ja l'ha triat abans, et dirà que està ocupat: n'hauràs de pensar un altre que no tingui ningú.\n" +
        "5. Quan acabi, et donarà una sèrie llarga de números i lletres. Copia-la (mantén el dit a sobre) i enganxa-la aquí sota.",
    tokenLabel = "Codi que t'ha donat el BotFather",
    alreadyBot = "Aquest aparell ja té un bot. Si no en vols canviar, deixa la casella buida i toca «Següent».",
    paste = "Enganxa", checking = "Comprovant…",
    tokenBad = "No funciona: comprova que hagis enganxat el codi sencer i que hi hagi internet.",
    contactTitle = "3. Qui hi parlarà",
    contactSteps = "Aquesta persona ha d'obrir Telegram, buscar %s, tocar Start i escriure qualsevol missatge. Quan ho faci, aquí sortirà un requadre groc: toca «Fes-lo servir» i desa el contacte.",
    botRef = "el bot que has creat",
    haveContacts = "En aquest mòbil ja hi tens aquests contactes: %s. Si no en vols afegir cap més, no posis res i ves al pas següent.",
    nameLabel = "Nom (surt al botó)", articleLabel = "Article davant del nom", numberLabel = "Número de Telegram",
    wrote = "Ha escrit: %s (%s)", useIt = "Fes-lo servir", save = "Desa aquest contacte", saved = "Contacte desat ✔",
    pinTitle = "4. Protegir Programació",
    pinBody = "«Programació» és on es canvia la configuració. Només ho ha de fer un adult. Si vols que ningú més hi entri (per exemple, els nets), posa-hi un PIN de 4 xifres. No és el codi per desbloquejar el mòbil. És opcional.",
    pinLabel = "PIN (4 xifres; buit = sense PIN)",
    pinNote = "Apunta'l: si s'oblida, només es pot treure reinstal·lant l'app.",
    fixTitle = "5. Fixar l'app",
    fixIntro = "Fixar l'app fa que el mòbil només serveixi per a aquesta app, perquè no se'n pugui sortir per error. Android et demanarà que ho confirmis. És opcional.",
    btnPin = "Fixa l'app a la pantalla",
    fixExit = "Per sortir-ne: «Deixa de fixar» o mantén premuts Enrere i Recents. Després d'un reinici cal tornar-la a fixar.",
    doneTitle = "Tot a punt", doneBody = "Ja pots tancar la guia. Tot es pot canviar més endavant des de «Programació».",
)

private val Es = Words(
    skip = "Saltar la guía", back = "Atrás", next = "Siguiente", finish = "Terminar",
    welcomeTitle = "Preparamos el aparato",
    welcomeBody = "Esta app es para personas a quienes les cuesta usar el móvil: permite hablar por voz con unas pocas personas.\n\nEsta guía ayuda a un adulto a dejarlo todo listo, solo con este móvil. Los pasos son: internet, el bot de Telegram, quién hablará, un PIN opcional y fijar la app. Puedes saltarla y volver desde «Programación».",
    wifiTitle = "1. Internet",
    wifiBody = "Este móvil necesita internet para recibir y enviar mensajes. Lo más sencillo es el Wi-Fi de casa; si no hay, también funciona con una tarjeta SIM con datos (no hace falta para llamar). Sin internet no funciona. Comprueba que esté conectado o conéctalo ahora.",
    wifiOn = "Conectado a internet ✔", wifiOff = "Aún no está conectado a internet", wifiBtn = "Abrir los ajustes de Wi-Fi",
    botTitle = "2. El bot de Telegram",
    botSteps = "Necesitas tener instalada la aplicación Telegram en algún móvil para poder preparar esto. " +
        "Cada aparato tiene su propio bot, que creas tú.\n\n" +
        "1. En Telegram, busca @BotFather (el que tiene el sello azul de verificado) y ábrelo.\n" +
        "2. Envíale un mensaje con este texto: /newbot\n" +
        "3. Te pedirá un nombre para el bot: escribe el que quieras (por ejemplo, «Casa de María Rosa»).\n" +
        "4. Después te pedirá un nombre de usuario, que debe acabar en «bot» (por ejemplo, casamariarosa_bot). " +
        "Si alguien ya lo ha elegido antes, te dirá que está ocupado: tendrás que pensar otro que no tenga nadie.\n" +
        "5. Al terminar, te dará una serie larga de números y letras. Cópiala (mantén el dedo encima) y pégala aquí abajo.",
    tokenLabel = "Código que te ha dado BotFather",
    alreadyBot = "Este aparato ya tiene un bot. Si no quieres cambiarlo, deja la casilla vacía y toca «Siguiente».",
    paste = "Pegar", checking = "Comprobando…",
    tokenBad = "No funciona: comprueba que hayas pegado el código entero y que haya internet.",
    contactTitle = "3. Quién hablará",
    contactSteps = "Esa persona debe abrir Telegram, buscar %s, tocar Start y escribir cualquier mensaje. Cuando lo haga, aquí saldrá un recuadro amarillo: toca «Usar» y guarda el contacto.",
    botRef = "el bot que has creado",
    haveContacts = "En este móvil ya tienes estos contactos: %s. Si no quieres añadir ninguno más, no pongas nada y ve al paso siguiente.",
    nameLabel = "Nombre (sale en el botón)", articleLabel = "Artículo delante del nombre", numberLabel = "Número de Telegram",
    wrote = "Ha escrito: %s (%s)", useIt = "Usar", save = "Guardar este contacto", saved = "Contacto guardado ✔",
    pinTitle = "4. Proteger Programación",
    pinBody = "«Programación» es donde se cambia la configuración. Solo debe hacerlo un adulto. Si quieres que nadie más entre (por ejemplo, los nietos), pon un PIN de 4 cifras. No es el código para desbloquear el móvil. Es opcional.",
    pinLabel = "PIN (4 cifras; vacío = sin PIN)",
    pinNote = "Anótalo: si se olvida, solo se puede quitar reinstalando la app.",
    fixTitle = "5. Fijar la app",
    fixIntro = "Fijar la app hace que el móvil solo sirva para esta app, para que no se pueda salir por error. Android te pedirá que lo confirmes. Es opcional.",
    btnPin = "Fijar la app en la pantalla",
    fixExit = "Para salir: «Dejar de fijar» o mantén pulsados Atrás y Recientes. Tras un reinicio hay que volver a fijarla.",
    doneTitle = "Todo listo", doneBody = "Ya puedes cerrar la guía. Todo se puede cambiar después desde «Programación».",
)

private val En = Words(
    skip = "Skip the guide", back = "Back", next = "Next", finish = "Finish",
    welcomeTitle = "Let's set up the phone",
    welcomeBody = "This app is for people who find phones hard to use: it lets them talk by voice with a few people.\n\nThis guide helps an adult get everything ready, using only this phone. The steps are: internet, the Telegram bot, who will talk, an optional PIN and pinning the app. You can skip it and come back from \"Programming\".",
    wifiTitle = "1. Internet",
    wifiBody = "This phone needs internet to receive and send messages. The easiest way is home Wi-Fi; if there is none, a SIM card with mobile data also works (it does not need to be used for calls). Without internet it does not work. Check that it is connected, or connect it now.",
    wifiOn = "Connected to the internet ✔", wifiOff = "Not connected to the internet yet", wifiBtn = "Open Wi-Fi settings",
    botTitle = "2. The Telegram bot",
    botSteps = "You need the Telegram app installed on some phone to be able to set this up. " +
        "Each phone has its own bot, which you create.\n\n" +
        "1. In Telegram, search for @BotFather (the one with the blue verified badge) and open it.\n" +
        "2. Send it a message with this text: /newbot\n" +
        "3. It will ask for a name for the bot: type whatever you like (for example, \"Maria Rosa's home\").\n" +
        "4. Then it will ask for a username, which must end in \"bot\" (for example, casamariarosa_bot). " +
        "If someone has already taken it, it will say it is taken: you will need to think of another one that nobody has.\n" +
        "5. When it finishes, it will give you a long series of numbers and letters. Copy it (hold your finger on it) and paste it below.",
    tokenLabel = "Code BotFather gave you",
    alreadyBot = "This phone already has a bot. To keep it, leave the box empty and tap \"Next\".",
    paste = "Paste", checking = "Checking…",
    tokenBad = "It does not work: check that you pasted the whole code and that there is internet.",
    contactTitle = "3. Who will talk",
    contactSteps = "That person must open Telegram, search for %s, tap Start and write any message. When they do, a yellow box will appear here: tap \"Use it\" and save the contact.",
    botRef = "the bot you created",
    haveContacts = "This phone already has these contacts: %s. If you do not want to add any more, put nothing and go to the next step.",
    nameLabel = "Name (shown on the button)", articleLabel = "Article before the name", numberLabel = "Telegram number",
    wrote = "Wrote: %s (%s)", useIt = "Use it", save = "Save this contact", saved = "Contact saved ✔",
    pinTitle = "4. Protect Programming",
    pinBody = "\"Programming\" is where the settings are changed. Only an adult should do it. If you want nobody else to get in (for example, the grandchildren), set a 4-digit PIN. It is not the code that unlocks the phone. It is optional.",
    pinLabel = "PIN (4 digits; empty = no PIN)",
    pinNote = "Write it down: if forgotten, it can only be removed by reinstalling the app.",
    fixTitle = "5. Pin the app",
    fixIntro = "Pinning the app makes the phone usable only for this app, so nobody leaves it by mistake. Android will ask you to confirm. It is optional.",
    btnPin = "Pin the app to the screen",
    fixExit = "To leave: \"Stop pinning\", or hold Back and Recents. After a restart it must be pinned again.",
    doneTitle = "All set", doneBody = "You can close the guide. Everything can be changed later from \"Programming\".",
)

private val Fr = Words(
    skip = "Passer le guide", back = "Retour", next = "Suivant", finish = "Terminer",
    welcomeTitle = "Préparons l'appareil",
    welcomeBody = "Cette application est pour les personnes qui ont du mal à utiliser un téléphone : elle permet de parler par la voix avec quelques personnes.\n\nCe guide aide un adulte à tout préparer, avec ce seul téléphone. Les étapes : internet, le bot Telegram, qui parlera, un code PIN facultatif et l'épinglage de l'application. Vous pouvez l'ignorer et y revenir depuis « Programmation ».",
    wifiTitle = "1. Internet",
    wifiBody = "Ce téléphone a besoin d'internet pour recevoir et envoyer des messages. Le plus simple est le Wi-Fi de la maison ; s'il n'y en a pas, une carte SIM avec données mobiles fonctionne aussi (inutile pour téléphoner). Sans internet, rien ne marche. Vérifiez qu'il est connecté, ou connectez-le maintenant.",
    wifiOn = "Connecté à internet ✔", wifiOff = "Pas encore connecté à internet", wifiBtn = "Ouvrir les réglages Wi-Fi",
    botTitle = "2. Le bot Telegram",
    botSteps = "Il vous faut l'application Telegram installée sur un téléphone pour pouvoir préparer cela. " +
        "Chaque appareil a son propre bot, que vous créez.\n\n" +
        "1. Dans Telegram, cherchez @BotFather (celui qui a le badge bleu de vérification) et ouvrez-le.\n" +
        "2. Envoyez-lui un message avec ce texte : /newbot\n" +
        "3. Il vous demandera un nom pour le bot : écrivez ce que vous voulez (par exemple « Maison de Marie-Rose »).\n" +
        "4. Ensuite il vous demandera un nom d'utilisateur, qui doit se terminer par « bot » (par exemple casamariarosa_bot). " +
        "Si quelqu'un l'a déjà choisi, il vous dira qu'il est pris : il faudra en trouver un autre que personne n'a.\n" +
        "5. À la fin, il vous donnera une longue suite de chiffres et de lettres. Copiez-la (maintenez le doigt dessus) et collez-la ci-dessous.",
    tokenLabel = "Code donné par BotFather",
    alreadyBot = "Cet appareil a déjà un bot. Pour le garder, laissez la case vide et touchez « Suivant ».",
    paste = "Coller", checking = "Vérification…",
    tokenBad = "Cela ne fonctionne pas : vérifiez que vous avez collé le code en entier et qu'il y a internet.",
    contactTitle = "3. Qui parlera",
    contactSteps = "Cette personne doit ouvrir Telegram, chercher %s, toucher Start et écrire n'importe quel message. Dès qu'elle l'aura fait, un encadré jaune apparaîtra ici : touchez « Utiliser » et enregistrez le contact.",
    botRef = "le bot que vous avez créé",
    haveContacts = "Ce téléphone a déjà ces contacts : %s. Si vous ne voulez pas en ajouter d'autres, ne saisissez rien et passez à l'étape suivante.",
    nameLabel = "Nom (affiché sur le bouton)", articleLabel = "Article devant le nom", numberLabel = "Numéro Telegram",
    wrote = "A écrit : %s (%s)", useIt = "Utiliser", save = "Enregistrer ce contact", saved = "Contact enregistré ✔",
    pinTitle = "4. Protéger Programmation",
    pinBody = "« Programmation » est l'endroit où l'on change les réglages. Seul un adulte doit le faire. Si vous voulez que personne d'autre n'y accède (par exemple les petits-enfants), mettez un code PIN à 4 chiffres. Ce n'est pas le code de déverrouillage du téléphone. C'est facultatif.",
    pinLabel = "PIN (4 chiffres ; vide = sans PIN)",
    pinNote = "Notez-le : en cas d'oubli, on ne peut l'enlever qu'en réinstallant l'application.",
    fixTitle = "5. Épingler l'application",
    fixIntro = "Épingler l'application fait que le téléphone ne sert qu'à elle, pour qu'on n'en sorte pas par erreur. Android vous demandera de confirmer. C'est facultatif.",
    btnPin = "Épingler l'application à l'écran",
    fixExit = "Pour en sortir : « Ne plus épingler », ou maintenez Retour et Récents. Après un redémarrage, il faut l'épingler de nouveau.",
    doneTitle = "Tout est prêt", doneBody = "Vous pouvez fermer le guide. Tout peut être modifié plus tard depuis « Programmation ».",
)

private val Articles = listOf("–" to "", "la" to "la", "el" to "el", "l'" to "l'", "en" to "en")

internal fun wordsFor(lang: String) = when (lang) { "es" -> Es; "en" -> En; "fr" -> Fr; else -> Ca }


/**
 * The first-time guide, optional and skippable at any step. It does what the
 * programming screen does, but explained step by step, so a phone can be
 * prepared with no computer.
 */
@Composable
fun TutorialScreen(
    context: Context,
    onApply: () -> Unit,
    onUnpin: () -> Unit,
    onPin: () -> Unit,
    onWifi: () -> Unit,
    onClose: () -> Unit,
) {
    val prefs = remember { Prefs(context) }
    val w = wordsFor(Lang.current)
    var step by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()
    // Each step starts at its top, not wherever the previous one was scrolled to.
    LaunchedEffect(step) { scroll.scrollTo(0) }

    var token by remember { mutableStateOf("") }
    var checking by remember { mutableStateOf(false) }
    var tokenError by remember { mutableStateOf(false) }
    var botName by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var article by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    var wifi by remember { mutableStateOf(internetConnected(context)) }
    var pin by remember { mutableStateOf(prefs.pin) }
    var stranger by remember { mutableStateOf(prefs.stranger) }
    var contactsNow by remember { mutableStateOf(AppConfig.load(context).contacts.filter { it.telegramId != null }) }

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
            wifi = internetConnected(context)
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
        Lang.all.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                pair.forEach { (code, label) ->
                    FilterChip(
                        selected = Lang.current == code,
                        onClick = { Lang.set(context, code) },
                        label = { Text(label, fontSize = 17.sp) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        if (S.autoTranslation.isNotEmpty()) Text(S.autoTranslation, fontSize = 14.sp, color = Color.DarkGray)
        TextButton(onClick = { close() }, modifier = Modifier.align(Alignment.End)) { Text(w.skip) }

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            when (step) {
                0 -> {
                    Text(w.welcomeTitle, fontSize = 30.sp, color = Color.Black)
                    Text(w.welcomeBody, fontSize = 19.sp, color = Color.DarkGray)
                }
                1 -> {
                    Text(w.wifiTitle, fontSize = 30.sp, color = Color.Black)
                    Text(w.wifiBody, fontSize = 19.sp, color = Color.DarkGray)
                    Text(
                        if (wifi) w.wifiOn else w.wifiOff,
                        fontSize = 20.sp,
                        color = if (wifi) Color(0xFF2E7D32) else Color(0xFFC62828),
                    )
                    OutlinedButton(onClick = onWifi) { Text(w.wifiBtn) }
                }
                2 -> {
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
                3 -> {
                    Text(w.contactTitle, fontSize = 30.sp, color = Color.Black)
                    Text(w.contactSteps.format(botName?.let { "@$it" } ?: w.botRef), fontSize = 19.sp, color = Color.DarkGray)
                    if (contactsNow.isNotEmpty()) {
                        Text(
                            w.haveContacts.format(contactsNow.joinToString(", ") { it.name }),
                            fontSize = 18.sp, color = Color(0xFF2E7D32),
                        )
                    }
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
                    if (Lang.current == "ca") {
                        Text(w.articleLabel, fontSize = 16.sp, color = Color.DarkGray)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Articles.forEach { (label, value) ->
                                FilterChip(selected = article == value, onClick = { article = value }, label = { Text(label) })
                            }
                        }
                    }
                    OutlinedTextField(
                        value = number, onValueChange = { number = it.filter(Char::isDigit); saved = false },
                        label = { Text(w.numberLabel) }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        enabled = name.isNotBlank() && number.isNotEmpty() && contactsNow.size < MAX_CONTACTS,
                        onClick = {
                            val id = number.toLong()
                            val kept = AppConfig.load(context).contacts.filter { it.telegramId != null && it.telegramId != id }
                            prefs.saveContacts(kept + Contact("c${System.currentTimeMillis()}", name.trim(), true, id, article))
                            prefs.stranger = null
                            onApply()
                            contactsNow = AppConfig.load(context).contacts.filter { it.telegramId != null }
                            saved = true
                        },
                    ) { Text(w.save) }
                    if (contactsNow.size >= MAX_CONTACTS) Text(S.maxContacts, fontSize = 17.sp, color = Color(0xFFC62828))
                    if (saved) Text(w.saved, fontSize = 18.sp, color = Color(0xFF2E7D32))
                }
                4 -> {
                    Text(w.pinTitle, fontSize = 30.sp, color = Color.Black)
                    Text(w.pinBody, fontSize = 19.sp, color = Color.DarkGray)
                    OutlinedTextField(
                        value = pin, onValueChange = { pin = it.filter(Char::isDigit).take(4) },
                        label = { Text(w.pinLabel) }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(w.pinNote, fontSize = 17.sp, color = Color.DarkGray)
                }
                5 -> {
                    Text(w.fixTitle, fontSize = 30.sp, color = Color.Black)
                    Text(w.fixIntro, fontSize = 19.sp, color = Color.DarkGray)
                    OutlinedButton(onClick = onPin, modifier = Modifier.fillMaxWidth()) { Text(w.btnPin) }
                    OutlinedButton(onClick = onUnpin, modifier = Modifier.fillMaxWidth()) { Text(S.unpin) }
                    Text(w.fixExit, fontSize = 17.sp, color = Color.DarkGray)
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
                enabled = !checking && !(step == 4 && pin.length in 1..3),
                modifier = Modifier.weight(1f),
                onClick = {
                    when {
                        step == 6 -> close()
                        step == 4 -> { prefs.pin = pin; step++ }
                        step == 2 && token.isNotEmpty() -> {
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
            ) { Text(if (step == 6) w.finish else w.next, fontSize = 20.sp) }
        }
    }
}
