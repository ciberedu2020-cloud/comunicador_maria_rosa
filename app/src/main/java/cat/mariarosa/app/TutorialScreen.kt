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
    val fixTitle: String, val fixIntro: String, val btnHome: String, val btnPin: String, val fixExit: String,
    val levelsShow: String, val levelsHide: String, val fixLevels: String,
    val doneTitle: String, val doneBody: String,
)

internal val Ca = Words(
    skip = "Salta la guia", back = "Enrere", next = "Següent", finish = "Acaba",
    welcomeTitle = "Preparem l'aparell",
    welcomeBody = "Aquesta aplicació està pensada per a persones que tenen dificultats per fer servir un mòbil, " +
        "i permet comunicar-se per veu amb uns pocs usuaris.\n\n" +
        "Aquesta guia és opcional: serveix per deixar l'app a punt amb només aquest mòbil, sense ordinador. " +
        "Els passos són: el Wi-Fi, el bot de Telegram, qui hi parlarà, un PIN opcional i, si vols, fixar l'app a la pantalla. " +
        "Pots saltar-la ara i tornar-hi des de «Programació».",
    wifiTitle = "1. Wi-Fi",
    wifiBody = "Per començar, necessites l'ajuda d'algú per deixar-ho tot a punt. Un cop fetes aquestes pautes, després ja anirà tot sol.\n\n" +
        "Aquest mòbil només funcionarà a casa, amb el Wi-Fi de la casa: no té número de telèfon ni dades mòbils, " +
        "i fora de casa no rebrà ni enviarà missatges. Comprova que estigui connectat al Wi-Fi, o connecta'l ara.",
    wifiOn = "Connectat al Wi-Fi ✔", wifiOff = "Encara no està connectat a cap Wi-Fi", wifiBtn = "Obre els ajustos de Wi-Fi",
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
    contactSteps = "Aquesta persona ha d'obrir Telegram, buscar %s, tocar Start i escriure «hola». " +
        "Quan ho faci, el seu nom i número apareixeran aquí sols. També pots escriure'ls a mà.",
    botRef = "el bot que has creat",
    haveContacts = "En aquest mòbil ja hi tens aquests contactes: %s. Si no en vols afegir cap més, no posis res i ves al pas següent.",
    nameLabel = "Nom (surt al botó)", articleLabel = "Article davant del nom", numberLabel = "Número de Telegram",
    wrote = "Ha escrit: %s (%s)", useIt = "Fes-lo servir", save = "Desa aquest contacte", saved = "Contacte desat ✔",
    pinTitle = "4. Protegir Programació",
    pinBody = "«Programació» és la pantalla on es canvia la configuració de l'app (contactes, so…). Només ho ha de fer un adult. Si vols que ningú més hi pugui entrar (per exemple, els nets), posa-hi un PIN de 4 xifres: el demanarà cada cop que algú intenti obrir Programació. No és el codi per desbloquejar el mòbil. És opcional.",
    pinLabel = "PIN (4 xifres; buit = sense PIN)",
    pinNote = "Apunta'l en un lloc segur: si s'oblida, l'única manera de treure'l és reinstal·lar l'app, i es perd la configuració.",
    fixTitle = "5. Fixar l'app",
    fixIntro = "Per facilitar l'ús de l'app a persones a qui costa fer servir els mòbils, proposem fer servir aquest mòbil exclusivament per a aquesta aplicació. " +
        "Amb aquests dos botons ho pots fer de dues maneres: fixar l'app a la pantalla, i fer que el botó d'inici torni sempre a l'app. " +
        "A sota te'n proposem dues més, més segures, que has de fer tu mateix canviant paràmetres del mòbil. Tot és opcional.",
    btnHome = "Que el botó d'inici torni a l'app", btnPin = "Fixa l'app a la pantalla",
    fixExit = "Per sortir d'una fixació: «Programació» → «Deixa de fixar». També se'n pot sortir mantenint premuts Enrere i Recents " +
        "(el gest pot variar segons el mòbil). Després d'un reinici s'ha de tornar a fixar. Va bé contra tocs sense voler, no contra algú que ho sàpiga.",
    levelsShow = "Dues maneres més segures (avançat)", levelsHide = "Amaga-ho",
    fixLevels = "A. Bloqueig de pantalla amb PIN. Fixa l'app, posa un bloqueig de pantalla amb PIN i, als ajustos d'Android, " +
        "activa que demani el PIN abans de deixar de fixar (el nom de l'opció varia). Compte: amb bloqueig de pantalla, en despertar el mòbil " +
        "pot demanar el PIN abans de mostrar l'app. No s'ha provat amb aquesta app.\n\n" +
        "B. Mode quiosc (l'app com a administradora del mòbil). No se'n pot sortir sense l'adult i sobreviu als reinicis. " +
        "Cal un mòbil restaurat de fàbrica, sense cap compte, i fer-ho un cop des d'un ordinador. Aquesta versió de l'app encara no ho porta incorporat.",
    doneTitle = "Tot a punt", doneBody = "Ja pots tancar la guia. Tot es pot canviar més endavant des de «Programació».",
)

private val Es = Words(
    skip = "Saltar la guía", back = "Atrás", next = "Siguiente", finish = "Terminar",
    welcomeTitle = "Preparamos el aparato",
    welcomeBody = "Esta aplicación está pensada para personas con dificultades para usar un móvil, " +
        "y permite comunicarse por voz con unos pocos usuarios.\n\n" +
        "Esta guía es opcional: sirve para dejar la app lista solo con este móvil, sin ordenador. " +
        "Los pasos son: el Wi-Fi, el bot de Telegram, quién hablará, un PIN opcional y, si quieres, fijar la app en la pantalla. " +
        "Puedes saltarla ahora y volver desde «Programación».",
    wifiTitle = "1. Wi-Fi",
    wifiBody = "Para empezar, necesitas la ayuda de alguien para dejarlo todo listo. Una vez hechas estas pautas, después todo irá solo.\n\n" +
        "Este móvil solo funcionará en casa, con el Wi-Fi de la casa: no tiene número de teléfono ni datos móviles, " +
        "y fuera de casa no recibirá ni enviará mensajes. Comprueba que esté conectado al Wi-Fi, o conéctalo ahora.",
    wifiOn = "Conectado al Wi-Fi ✔", wifiOff = "Aún no está conectado a ningún Wi-Fi", wifiBtn = "Abrir los ajustes de Wi-Fi",
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
    contactSteps = "Esa persona debe abrir Telegram, buscar %s, tocar Start y escribir «hola». " +
        "Cuando lo haga, su nombre y número aparecerán aquí solos. También puedes escribirlos a mano.",
    botRef = "el bot que has creado",
    haveContacts = "En este móvil ya tienes estos contactos: %s. Si no quieres añadir ninguno más, no pongas nada y ve al paso siguiente.",
    nameLabel = "Nombre (sale en el botón)", articleLabel = "Artículo delante del nombre", numberLabel = "Número de Telegram",
    wrote = "Ha escrito: %s (%s)", useIt = "Usar", save = "Guardar este contacto", saved = "Contacto guardado ✔",
    pinTitle = "4. Proteger Programación",
    pinBody = "«Programación» es la pantalla donde se cambia la configuración de la app (contactos, sonido…). Solo debe hacerlo un adulto. Si quieres que nadie más pueda entrar (por ejemplo, los nietos), pon un PIN de 4 cifras: lo pedirá cada vez que alguien intente abrir Programación. No es el código para desbloquear el móvil. Es opcional.",
    pinLabel = "PIN (4 cifras; vacío = sin PIN)",
    pinNote = "Anótalo en un lugar seguro: si se olvida, la única forma de quitarlo es reinstalar la app, y se pierde la configuración.",
    fixTitle = "5. Fijar la app",
    fixIntro = "Para facilitar el uso de la app a personas a quienes les cuesta usar el móvil, proponemos usar este móvil exclusivamente para esta aplicación. " +
        "Con estos dos botones puedes hacerlo de dos maneras: fijar la app en la pantalla, y hacer que el botón de inicio vuelva siempre a la app. " +
        "Más abajo te proponemos dos más, más seguras, que debes hacer tú mismo cambiando parámetros del móvil. Todo es opcional.",
    btnHome = "Que el botón de inicio vuelva a la app", btnPin = "Fijar la app en la pantalla",
    fixExit = "Para salir de una fijación: «Programación» → «Dejar de fijar». También se puede salir manteniendo pulsados Atrás y Recientes " +
        "(el gesto puede variar según el móvil). Tras un reinicio hay que volver a fijarla. Sirve contra toques sin querer, no contra alguien que lo sepa.",
    levelsShow = "Dos maneras más seguras (avanzado)", levelsHide = "Ocultar",
    fixLevels = "A. Bloqueo de pantalla con PIN. Fija la app, pon un bloqueo de pantalla con PIN y, en los ajustes de Android, " +
        "activa que pida el PIN antes de dejar de fijar (el nombre de la opción varía). Ojo: con bloqueo de pantalla, al despertar el móvil " +
        "puede pedir el PIN antes de mostrar la app. No se ha probado con esta app.\n\n" +
        "B. Modo quiosco (la app como administradora del móvil). No se puede salir sin el adulto y sobrevive a los reinicios. " +
        "Requiere un móvil restaurado de fábrica, sin ninguna cuenta, y hacerlo una vez desde un ordenador. Esta versión de la app aún no lo incluye.",
    doneTitle = "Todo listo", doneBody = "Ya puedes cerrar la guía. Todo se puede cambiar después desde «Programación».",
)

private val En = Words(
    skip = "Skip the guide", back = "Back", next = "Next", finish = "Finish",
    welcomeTitle = "Let's set up the phone",
    welcomeBody = "This app is meant for people who have difficulty using a phone, " +
        "and lets them communicate by voice with a few people.\n\n" +
        "This guide is optional: it gets the app ready using only this phone, no computer. " +
        "The steps are: Wi-Fi, the Telegram bot, who will be talking, an optional PIN and, if you wish, pinning the app to the screen. " +
        "You can skip it now and come back from \"Programming\".",
    wifiTitle = "1. Wi-Fi",
    wifiBody = "To begin with, you need someone's help to get everything ready. Once these steps are done, everything will run by itself.\n\n" +
        "This phone will only work at home, on the home Wi-Fi: it has no phone number or mobile data, " +
        "and away from home it will neither receive nor send messages. Check that it is connected to the Wi-Fi, or connect it now.",
    wifiOn = "Connected to Wi-Fi ✔", wifiOff = "Not connected to any Wi-Fi yet", wifiBtn = "Open Wi-Fi settings",
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
    contactSteps = "That person must open Telegram, search for %s, tap Start and write \"hello\". " +
        "Their name and number will then appear here by themselves. You can also type them in.",
    botRef = "the bot you created",
    haveContacts = "This phone already has these contacts: %s. If you do not want to add any more, put nothing and go to the next step.",
    nameLabel = "Name (shown on the button)", articleLabel = "Article before the name", numberLabel = "Telegram number",
    wrote = "Wrote: %s (%s)", useIt = "Use it", save = "Save this contact", saved = "Contact saved ✔",
    pinTitle = "4. Protect Programming",
    pinBody = "\"Programming\" is the screen where the app's settings are changed (contacts, sound…). Only an adult should do it. If you want nobody else to get in (for example, the grandchildren), set a 4-digit PIN: it will be asked every time someone tries to open Programming. It is not the code that unlocks the phone. It is optional.",
    pinLabel = "PIN (4 digits; empty = no PIN)",
    pinNote = "Write it down somewhere safe: if forgotten, the only way to remove it is to reinstall the app, and the settings are lost.",
    fixTitle = "5. Pin the app",
    fixIntro = "To make the app easier for people who find phones hard to use, we suggest using this phone only for this app. " +
        "With these two buttons you can do it in two ways: pin the app to the screen, and make the home button always return to the app. " +
        "Below we suggest two more, more secure, that you have to do yourself by changing the phone's settings. All of it is optional.",
    btnHome = "Make the home button return to the app", btnPin = "Pin the app to the screen",
    fixExit = "To leave a pinned screen: \"Programming\" → \"Stop pinning\". It can also be left by holding Back and Recents " +
        "(the gesture may differ by phone). After a restart it must be pinned again. Good against accidental touches, not against someone who knows how.",
    levelsShow = "Two more secure ways (advanced)", levelsHide = "Hide",
    fixLevels = "A. Screen lock with a PIN. Pin the app, set a screen lock with a PIN and, in Android's settings, " +
        "turn on asking for the PIN before unpinning (the option's name varies). Careful: with a screen lock, waking the phone " +
        "may ask for the PIN before showing the app. Not tested with this app.\n\n" +
        "B. Kiosk mode (the app as the phone's administrator). It cannot be left without the adult and survives restarts. " +
        "It needs a factory-reset phone with no accounts, and doing it once from a computer. This version of the app does not include it yet.",
    doneTitle = "All set", doneBody = "You can close the guide. Everything can be changed later from \"Programming\".",
)

private val Fr = Words(
    skip = "Passer le guide", back = "Retour", next = "Suivant", finish = "Terminer",
    welcomeTitle = "Préparons l'appareil",
    welcomeBody = "Cette application est conçue pour les personnes qui ont du mal à utiliser un téléphone portable, " +
        "et leur permet de communiquer par la voix avec quelques personnes.\n\n" +
        "Ce guide est facultatif : il permet de préparer l'application avec ce seul téléphone, sans ordinateur. " +
        "Les étapes sont : le Wi-Fi, le bot Telegram, qui parlera, un PIN facultatif et, si vous le souhaitez, épingler l'application à l'écran. " +
        "Vous pouvez le passer maintenant et y revenir depuis « Programmation ».",
    wifiTitle = "1. Wi-Fi",
    wifiBody = "Pour commencer, vous avez besoin de l'aide de quelqu'un pour tout préparer. Une fois ces étapes faites, tout se fera ensuite tout seul.\n\n" +
        "Ce téléphone ne fonctionnera qu'à la maison, avec le Wi-Fi de la maison : il n'a ni numéro de téléphone ni données mobiles, " +
        "et hors de la maison il ne recevra ni n'enverra de messages. Vérifiez qu'il est connecté au Wi-Fi, ou connectez-le maintenant.",
    wifiOn = "Connecté au Wi-Fi ✔", wifiOff = "Pas encore connecté à un Wi-Fi", wifiBtn = "Ouvrir les réglages Wi-Fi",
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
    contactSteps = "Cette personne doit ouvrir Telegram, chercher %s, toucher Start et écrire « bonjour ». " +
        "Dès qu'elle l'aura fait, son nom et son numéro apparaîtront ici tout seuls. Vous pouvez aussi les saisir à la main.",
    botRef = "le bot que vous avez créé",
    haveContacts = "Ce téléphone a déjà ces contacts : %s. Si vous ne voulez pas en ajouter d'autres, ne saisissez rien et passez à l'étape suivante.",
    nameLabel = "Nom (affiché sur le bouton)", articleLabel = "Article devant le nom", numberLabel = "Numéro Telegram",
    wrote = "A écrit : %s (%s)", useIt = "Utiliser", save = "Enregistrer ce contact", saved = "Contact enregistré ✔",
    pinTitle = "4. Protéger Programmation",
    pinBody = "« Programmation » est l'écran où l'on modifie les réglages de l'application (contacts, son…). Seul un adulte doit le faire. Si vous voulez que personne d'autre n'y accède (par exemple les petits-enfants), mettez un code PIN à 4 chiffres : il sera demandé chaque fois que quelqu'un essaiera d'ouvrir Programmation. Ce n'est pas le code de déverrouillage du téléphone. C'est facultatif.",
    pinLabel = "PIN (4 chiffres ; vide = sans PIN)",
    pinNote = "Notez-le en lieu sûr : en cas d'oubli, le seul moyen de l'enlever est de réinstaller l'application, et les réglages sont perdus.",
    fixTitle = "5. Épingler l'application",
    fixIntro = "Pour faciliter l'usage de l'application aux personnes qui ont du mal avec les téléphones, nous proposons d'utiliser ce téléphone uniquement pour cette application. " +
        "Avec ces deux boutons, vous pouvez le faire de deux façons : épingler l'application à l'écran, et faire que le bouton d'accueil revienne toujours à l'application. " +
        "Plus bas, nous vous en proposons deux autres, plus sûres, que vous devez faire vous-même en modifiant des paramètres du téléphone. Tout est facultatif.",
    btnHome = "Que le bouton d'accueil revienne à l'application", btnPin = "Épingler l'application à l'écran",
    fixExit = "Pour quitter un écran épinglé : « Programmation » → « Ne plus épingler ». On peut aussi en sortir en maintenant Retour et Récents " +
        "(le geste peut varier selon le téléphone). Après un redémarrage, il faut l'épingler de nouveau. Utile contre les touches involontaires, pas contre quelqu'un qui sait faire.",
    levelsShow = "Deux façons plus sûres (avancé)", levelsHide = "Masquer",
    fixLevels = "A. Verrouillage d'écran avec PIN. Épinglez l'application, mettez un verrouillage d'écran avec PIN et, dans les réglages d'Android, " +
        "activez la demande du PIN avant de désépingler (le nom de l'option varie). Attention : avec un verrouillage d'écran, le réveil du téléphone " +
        "peut demander le PIN avant d'afficher l'application. Non testé avec cette application.\n\n" +
        "B. Mode kiosque (l'application comme administratrice du téléphone). On ne peut pas en sortir sans l'adulte et il survit aux redémarrages. " +
        "Il faut un téléphone réinitialisé, sans aucun compte, et le faire une fois depuis un ordinateur. Cette version de l'application ne l'inclut pas encore.",
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
    onHome: () -> Unit,
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
    var showLevels by remember { mutableStateOf(false) }
    var wifi by remember { mutableStateOf(wifiConnected(context)) }
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
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Lang.all.forEach { (code, label) ->
                FilterChip(selected = Lang.current == code, onClick = { Lang.set(context, code) }, label = { Text(label) })
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
                    OutlinedButton(onClick = onHome) { Text(w.btnHome) }
                    OutlinedButton(onClick = onPin) { Text(w.btnPin) }
                    Text(w.fixExit, fontSize = 17.sp, color = Color.DarkGray)
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
