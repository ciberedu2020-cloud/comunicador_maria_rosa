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
internal class Words(
    val skip: String, val back: String, val next: String, val finish: String,
    val welcomeTitle: String, val welcomeBody: String,
    val botTitle: String, val botSteps: String, val tokenLabel: String, val alreadyBot: String,
    val paste: String, val checking: String, val tokenBad: String,
    val contactTitle: String, val contactSteps: String, val botRef: String, val nameLabel: String,
    val articleLabel: String, val numberLabel: String, val wrote: String, val useIt: String,
    val save: String, val saved: String,
    val fixTitle: String, val fixIntro: String, val btnHome: String, val btnPin: String,
    val levelsShow: String, val levelsHide: String, val fixLevels: String,
    val wifiTitle: String, val wifiBody: String, val wifiOn: String, val wifiOff: String, val wifiBtn: String,
    val pinTitle: String, val pinBody: String, val pinLabel: String, val pinNote: String,
    val doneTitle: String, val doneBody: String,
)

internal val Ca = Words(
    skip = "Salta la guia", back = "Enrere", next = "Següent", finish = "Acaba",
    welcomeTitle = "Preparem l'aparell",
    welcomeBody = "Aquesta aplicació està pensada per a persones que tenen dificultats per fer servir un mòbil, " +
        "i permet comunicar-se per veu amb uns pocs usuaris.\n\n" +
        "Aquesta guia és opcional: serveix per deixar l'app a punt amb només aquest mòbil, sense ordinador. " +
        "Hi ha tres coses a fer: connectar un bot de Telegram, afegir la persona amb qui es parlarà " +
        "i, si vols, fixar l'app a la pantalla. Pots saltar-la ara i tornar-hi des de «Programació».",
    botTitle = "2. El bot de Telegram",
    botSteps = "Cada aparell té el seu propi bot, que crees tu amb el nom que vulguis.\n\n" +
        "A Telegram, busca @BotFather (el que té el segell blau de verificat).\n" +
        "Envia-li /newbot, tria un nom i un usuari que acabi en «bot».\n" +
        "Et donarà un token (una cadena llarga amb dos punts). Copia'l i enganxa'l aquí sota. No el mostris a ningú.",
    tokenLabel = "Token del bot",
    alreadyBot = "Aquest aparell ja té un bot. Si no en vols canviar, deixa la casella buida i toca «Següent».",
    paste = "Enganxa", checking = "Comprovant…",
    tokenBad = "No funciona: comprova que sigui el token sencer i que hi hagi internet.",
    contactTitle = "3. Qui hi parlarà",
    contactSteps = "Aquesta persona ha d'obrir Telegram, buscar %s, tocar Start i escriure «hola». " +
        "Quan ho faci, el seu nom i número apareixeran aquí sols. També pots escriure'ls a mà.",
    botRef = "el bot que has creat",
    nameLabel = "Nom (surt al botó)", articleLabel = "Article davant del nom", numberLabel = "Número de Telegram",
    wrote = "Ha escrit: %s (%s)", useIt = "Fes-lo servir", save = "Desa aquest contacte", saved = "Contacte desat ✔",
    fixTitle = "5. Fixar l'app",
    fixIntro = "Per facilitar l'ús de l'app a persones a qui costa fer servir els mòbils, proposem fer servir " +
        "aquest mòbil exclusivament per a aquesta aplicació. Per això hi ha tres nivells perquè l'app quedi fixada a la pantalla, " +
        "de menys a més segur. El primer es pot fer ara amb els botons; els altres els pots llegir més avall. Tot és opcional.",
    btnHome = "Que el botó d'inici torni a l'app", btnPin = "Fixa l'app a la pantalla",
    levelsShow = "Veure els tres nivells", levelsHide = "Amaga els nivells",
    fixLevels = "1. Fixar l'app (els botons de dalt). Es pot sortir mantenint premuts Enrere i Recents (el gest pot variar segons el mòbil) " +
        "i s'ha de tornar a fixar després d'un reinici. Va bé contra tocs sense voler, no contra algú que ho sàpiga.\n\n" +
        "2. Fixar l'app i posar un bloqueig de pantalla amb PIN; als ajustos d'Android, activar que demani el PIN abans de deixar de fixar " +
        "(el nom de l'opció varia). Compte: amb bloqueig de pantalla, en despertar el mòbil pot demanar el PIN abans de mostrar l'app. " +
        "No s'ha provat amb aquesta app.\n\n" +
        "3. Mode quiosc (l'app com a administradora del mòbil): no se'n pot sortir sense l'adult i sobreviu als reinicis. " +
        "Cal un mòbil restaurat de fàbrica, sense cap compte, i fer-ho un cop des d'un ordinador. Aquesta versió de l'app encara no ho porta incorporat.\n\n" +
        "Per sortir d'una fixació: «Programació» → «Deixa de fixar».",
    wifiTitle = "1. Wi-Fi",
    wifiBody = "Aquest mòbil funciona només amb Wi-Fi: no té número de telèfon ni dades mòbils. Ha d'estar connectat al Wi-Fi de la casa on es farà servir; sense això no rebrà ni enviarà missatges. Comprova que hi estigui, o connecta'l ara.",
    wifiOn = "Connectat al Wi-Fi ✔", wifiOff = "Encara no està connectat a cap Wi-Fi", wifiBtn = "Obre els ajustos de Wi-Fi",
    pinTitle = "4. Protegir Programació",
    pinBody = "«Programació» és la pantalla on es canvia la configuració de l'app (contactes, so…). Només ho ha de fer un adult. Si vols que ningú més hi pugui entrar (per exemple, els nets), posa-hi un PIN de 4 xifres: el demanarà cada cop que algú intenti obrir Programació. No és el codi per desbloquejar el mòbil. És opcional.",
    pinLabel = "PIN (4 xifres; buit = sense PIN)",
    pinNote = "Apunta'l en un lloc segur: si s'oblida, l'única manera de treure'l és reinstal·lar l'app, i es perd la configuració.",
    doneTitle = "Tot a punt", doneBody = "Ja pots tancar la guia. Tot es pot canviar més endavant des de «Programació».",
)

private val Es = Words(
    skip = "Saltar la guía", back = "Atrás", next = "Siguiente", finish = "Terminar",
    welcomeTitle = "Preparamos el aparato",
    welcomeBody = "Esta aplicación está pensada para personas con dificultades para usar un móvil, " +
        "y permite comunicarse por voz con unos pocos usuarios.\n\n" +
        "Esta guía es opcional: sirve para dejar la app lista solo con este móvil, sin ordenador. " +
        "Hay tres cosas: conectar un bot de Telegram, añadir a la persona con quien se hablará y, si quieres, " +
        "fijar la app en la pantalla. Puedes saltarla ahora y volver desde «Programación».",
    botTitle = "2. El bot de Telegram",
    botSteps = "Cada aparato tiene su propio bot, que creas tú con el nombre que quieras.\n\n" +
        "En Telegram, busca @BotFather (el que tiene el sello azul de verificado).\n" +
        "Envíale /newbot, elige un nombre y un usuario que acabe en «bot».\n" +
        "Te dará un token (una cadena larga con dos puntos). Cópialo y pégalo aquí abajo. No se lo enseñes a nadie.",
    tokenLabel = "Token del bot",
    alreadyBot = "Este aparato ya tiene un bot. Si no quieres cambiarlo, deja la casilla vacía y toca «Siguiente».",
    paste = "Pegar", checking = "Comprobando…",
    tokenBad = "No funciona: comprueba que sea el token entero y que haya internet.",
    contactTitle = "3. Quién hablará",
    contactSteps = "Esa persona debe abrir Telegram, buscar %s, tocar Start y escribir «hola». " +
        "Cuando lo haga, su nombre y número aparecerán aquí solos. También puedes escribirlos a mano.",
    botRef = "el bot que has creado",
    nameLabel = "Nombre (sale en el botón)", articleLabel = "Artículo delante del nombre", numberLabel = "Número de Telegram",
    wrote = "Ha escrito: %s (%s)", useIt = "Usar", save = "Guardar este contacto", saved = "Contacto guardado ✔",
    fixTitle = "5. Fijar la app",
    fixIntro = "Para facilitar el uso de la app a personas a quienes les cuesta usar el móvil, proponemos usar " +
        "este móvil exclusivamente para esta aplicación. Por eso hay tres niveles para que la app quede fijada en la pantalla, " +
        "de menos a más seguro. El primero se puede hacer ahora con los botones; los otros puedes leerlos más abajo. Todo es opcional.",
    btnHome = "Que el botón de inicio vuelva a la app", btnPin = "Fijar la app en la pantalla",
    levelsShow = "Ver los tres niveles", levelsHide = "Ocultar los niveles",
    fixLevels = "1. Fijar la app (los botones de arriba). Se puede salir manteniendo pulsados Atrás y Recientes (el gesto puede variar según el móvil) " +
        "y hay que volver a fijarla tras un reinicio. Sirve contra toques sin querer, no contra alguien que lo sepa.\n\n" +
        "2. Fijar la app y poner un bloqueo de pantalla con PIN; en los ajustes de Android, activar que pida el PIN antes de dejar de fijar " +
        "(el nombre de la opción varía). Ojo: con bloqueo de pantalla, al despertar el móvil puede pedir el PIN antes de mostrar la app. " +
        "No se ha probado con esta app.\n\n" +
        "3. Modo quiosco (la app como administradora del móvil): no se puede salir sin el adulto y sobrevive a los reinicios. " +
        "Requiere un móvil restaurado de fábrica, sin ninguna cuenta, y hacerlo una vez desde un ordenador. Esta versión de la app aún no lo incluye.\n\n" +
        "Para salir de una fijación: «Programación» → «Dejar de fijar».",
    wifiTitle = "1. Wi-Fi",
    wifiBody = "Este móvil funciona solo con Wi-Fi: no tiene número de teléfono ni datos móviles. Debe estar conectado al Wi-Fi de la casa donde se usará; sin eso no recibirá ni enviará mensajes. Comprueba que lo esté, o conéctalo ahora.",
    wifiOn = "Conectado al Wi-Fi ✔", wifiOff = "Aún no está conectado a ningún Wi-Fi", wifiBtn = "Abrir los ajustes de Wi-Fi",
    pinTitle = "4. Proteger Programación",
    pinBody = "«Programación» es la pantalla donde se cambia la configuración de la app (contactos, sonido…). Solo debe hacerlo un adulto. Si quieres que nadie más pueda entrar (por ejemplo, los nietos), pon un PIN de 4 cifras: lo pedirá cada vez que alguien intente abrir Programación. No es el código para desbloquear el móvil. Es opcional.",
    pinLabel = "PIN (4 cifras; vacío = sin PIN)",
    pinNote = "Anótalo en un lugar seguro: si se olvida, la única forma de quitarlo es reinstalar la app, y se pierde la configuración.",
    doneTitle = "Todo listo", doneBody = "Ya puedes cerrar la guía. Todo se puede cambiar después desde «Programación».",
)

private val En = Words(
    skip = "Skip the guide", back = "Back", next = "Next", finish = "Finish",
    welcomeTitle = "Let's set up the phone",
    welcomeBody = "This app is meant for people who have difficulty using a phone, " +
        "and lets them communicate by voice with a few people.\n\n" +
        "This guide is optional: it gets the app ready using only this phone, no computer. " +
        "There are three things: connect a Telegram bot, add the person who will be talking, and, if you wish, " +
        "pin the app to the screen. You can skip it now and come back from \"Programming\".",
    botTitle = "2. The Telegram bot",
    botSteps = "Each phone has its own bot, which you create with whatever name you like.\n\n" +
        "In Telegram, search for @BotFather (the one with the blue verified badge).\n" +
        "Send it /newbot, choose a name and a username ending in \"bot\".\n" +
        "It will give you a token (a long string with a colon). Copy it and paste it below. Do not show it to anyone.",
    tokenLabel = "Bot token",
    alreadyBot = "This phone already has a bot. To keep it, leave the box empty and tap \"Next\".",
    paste = "Paste", checking = "Checking…",
    tokenBad = "It does not work: check it is the whole token and that there is internet.",
    contactTitle = "3. Who will talk",
    contactSteps = "That person must open Telegram, search for %s, tap Start and write \"hello\". " +
        "Their name and number will then appear here by themselves. You can also type them in.",
    botRef = "the bot you created",
    nameLabel = "Name (shown on the button)", articleLabel = "Article before the name", numberLabel = "Telegram number",
    wrote = "Wrote: %s (%s)", useIt = "Use it", save = "Save this contact", saved = "Contact saved ✔",
    fixTitle = "5. Pin the app",
    fixIntro = "To make the app easier for people who find phones hard to use, we suggest using this phone " +
        "only for this app. That is why there are three levels for keeping the app pinned to the screen, " +
        "from less to more secure. The first can be done now with the buttons; you can read the others below. All of it is optional.",
    btnHome = "Make the home button return to the app", btnPin = "Pin the app to the screen",
    levelsShow = "See the three levels", levelsHide = "Hide the levels",
    fixLevels = "1. Pin the app (the buttons above). It can be left by holding Back and Recents (the gesture may differ by phone), " +
        "and it must be pinned again after a restart. Good against accidental touches, not against someone who knows how.\n\n" +
        "2. Pin the app and set a screen lock with a PIN; in Android's settings, turn on asking for the PIN before unpinning " +
        "(the option's name varies). Careful: with a screen lock, waking the phone may ask for the PIN before showing the app. " +
        "Not tested with this app.\n\n" +
        "3. Kiosk mode (the app as the phone's administrator): it cannot be left without the adult and survives restarts. " +
        "It needs a factory-reset phone with no accounts, and doing it once from a computer. This version of the app does not include it yet.\n\n" +
        "To leave a pinned screen: \"Programming\" → \"Stop pinning\".",
    wifiTitle = "1. Wi-Fi",
    wifiBody = "This phone works only on Wi-Fi: it has no phone number or mobile data. It must be connected to the Wi-Fi of the house where it will be used; without that it will neither receive nor send messages. Check that it is, or connect it now.",
    wifiOn = "Connected to Wi-Fi ✔", wifiOff = "Not connected to any Wi-Fi yet", wifiBtn = "Open Wi-Fi settings",
    pinTitle = "4. Protect Programming",
    pinBody = "\"Programming\" is the screen where the app's settings are changed (contacts, sound…). Only an adult should do it. If you want nobody else to get in (for example, the grandchildren), set a 4-digit PIN: it will be asked every time someone tries to open Programming. It is not the code that unlocks the phone. It is optional.",
    pinLabel = "PIN (4 digits; empty = no PIN)",
    pinNote = "Write it down somewhere safe: if forgotten, the only way to remove it is to reinstall the app, and the settings are lost.",
    doneTitle = "All set", doneBody = "You can close the guide. Everything can be changed later from \"Programming\".",
)

private val Fr = Words(
    skip = "Passer le guide", back = "Retour", next = "Suivant", finish = "Terminer",
    welcomeTitle = "Préparons l'appareil",
    welcomeBody = "Cette application est conçue pour les personnes qui ont du mal à utiliser un téléphone portable, " +
        "et leur permet de communiquer par la voix avec quelques personnes.\n\n" +
        "Ce guide est facultatif : il permet de préparer l'application avec ce seul téléphone, sans ordinateur. " +
        "Il y a trois choses à faire : connecter un bot Telegram, ajouter la personne avec qui l'on parlera " +
        "et, si vous le souhaitez, épingler l'application à l'écran. Vous pouvez le passer maintenant et y revenir depuis « Programmation ».",
    botTitle = "2. Le bot Telegram",
    botSteps = "Chaque appareil a son propre bot, que vous créez avec le nom que vous voulez.\n\n" +
        "Dans Telegram, cherchez @BotFather (celui qui a le badge bleu de vérification).\n" +
        "Envoyez-lui /newbot, choisissez un nom et un identifiant se terminant par « bot ».\n" +
        "Il vous donnera un jeton (une longue chaîne avec deux-points). Copiez-le et collez-le ci-dessous. Ne le montrez à personne.",
    tokenLabel = "Jeton du bot",
    alreadyBot = "Cet appareil a déjà un bot. Pour le garder, laissez la case vide et touchez « Suivant ».",
    paste = "Coller", checking = "Vérification…",
    tokenBad = "Cela ne fonctionne pas : vérifiez que c'est le jeton complet et qu'il y a internet.",
    contactTitle = "3. Qui parlera",
    contactSteps = "Cette personne doit ouvrir Telegram, chercher %s, toucher Start et écrire « bonjour ». " +
        "Dès qu'elle l'aura fait, son nom et son numéro apparaîtront ici tout seuls. Vous pouvez aussi les saisir à la main.",
    botRef = "le bot que vous avez créé",
    nameLabel = "Nom (affiché sur le bouton)", articleLabel = "Article devant le nom", numberLabel = "Numéro Telegram",
    wrote = "A écrit : %s (%s)", useIt = "Utiliser", save = "Enregistrer ce contact", saved = "Contact enregistré ✔",
    fixTitle = "5. Épingler l'application",
    fixIntro = "Pour faciliter l'usage de l'application aux personnes qui ont du mal avec les téléphones, nous proposons d'utiliser " +
        "ce téléphone uniquement pour cette application. Il y a donc trois niveaux pour que l'application reste épinglée à l'écran, " +
        "du moins sûr au plus sûr. Le premier peut se faire maintenant avec les boutons ; vous pouvez lire les autres plus bas. Tout est facultatif.",
    btnHome = "Que le bouton d'accueil revienne à l'application", btnPin = "Épingler l'application à l'écran",
    levelsShow = "Voir les trois niveaux", levelsHide = "Masquer les niveaux",
    fixLevels = "1. Épingler l'application (les boutons ci-dessus). On peut en sortir en maintenant Retour et Récents (le geste peut varier selon le téléphone), " +
        "et il faut la réépingler après un redémarrage. Utile contre les touches involontaires, pas contre quelqu'un qui sait faire.\n\n" +
        "2. Épingler l'application et mettre un verrouillage d'écran avec code PIN ; dans les réglages d'Android, activer la demande du PIN avant de désépingler " +
        "(le nom de l'option varie). Attention : avec un verrouillage d'écran, le réveil du téléphone peut demander le PIN avant d'afficher l'application. " +
        "Non testé avec cette application.\n\n" +
        "3. Mode kiosque (l'application comme administratrice du téléphone) : on ne peut pas en sortir sans l'adulte et il survit aux redémarrages. " +
        "Il faut un téléphone réinitialisé, sans aucun compte, et le faire une fois depuis un ordinateur. Cette version de l'application ne l'inclut pas encore.\n\n" +
        "Pour quitter un écran épinglé : « Programmation » → « Ne plus épingler ».",
    wifiTitle = "1. Wi-Fi",
    wifiBody = "Ce téléphone fonctionne uniquement en Wi-Fi : il n'a ni numéro de téléphone ni données mobiles. Il doit être connecté au Wi-Fi de la maison où il sera utilisé ; sinon il ne recevra ni n'enverra de messages. Vérifiez qu'il l'est, ou connectez-le maintenant.",
    wifiOn = "Connecté au Wi-Fi ✔", wifiOff = "Pas encore connecté à un Wi-Fi", wifiBtn = "Ouvrir les réglages Wi-Fi",
    pinTitle = "4. Protéger Programmation",
    pinBody = "« Programmation » est l'écran où l'on modifie les réglages de l'application (contacts, son…). Seul un adulte doit le faire. Si vous voulez que personne d'autre n'y accède (par exemple les petits-enfants), mettez un code PIN à 4 chiffres : il sera demandé chaque fois que quelqu'un essaiera d'ouvrir Programmation. Ce n'est pas le code de déverrouillage du téléphone. C'est facultatif.",
    pinLabel = "PIN (4 chiffres ; vide = sans PIN)",
    pinNote = "Notez-le en lieu sûr : en cas d'oubli, le seul moyen de l'enlever est de réinstaller l'application, et les réglages sont perdus.",
    doneTitle = "Tout est prêt", doneBody = "Vous pouvez fermer le guide. Tout peut être modifié plus tard depuis « Programmation ».",
)

private val Articles = listOf("–" to "", "la" to "la", "el" to "el", "l'" to "l'", "en" to "en")

private fun wordsFor(lang: String) = when (lang) { "es" -> Es; "en" -> En; "fr" -> Fr; else -> Ca }

private fun systemLang(): String = when (Locale.getDefault().language) { "es" -> "es"; "en" -> "en"; "fr" -> "fr"; else -> "ca" }

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
    onWifi: () -> Unit,
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
    var showLevels by remember { mutableStateOf(false) }
    var wifi by remember { mutableStateOf(wifiConnected(context)) }
    var pin by remember { mutableStateOf(prefs.pin) }
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
            wifi = wifiConnected(context)
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
                listOf("ca" to "Català", "es" to "Español", "en" to "English", "fr" to "Français").forEach { (code, label) ->
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
                    OutlinedButton(onClick = onHome) { Text(w.btnHome) }
                    OutlinedButton(onClick = onPin) { Text(w.btnPin) }
                    TextButton(onClick = { showLevels = !showLevels }) { Text(if (showLevels) w.levelsHide else w.levelsShow) }
                    if (showLevels) Text(w.fixLevels, fontSize = 17.sp, color = Color.DarkGray)
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
