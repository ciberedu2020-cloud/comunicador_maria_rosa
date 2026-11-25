package cat.mariarosa.app

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * The language of the whole app: the one chosen in the guide or in Programació,
 * or else the phone's own (Catalan, Spanish, English or French; Catalan if it is none of them).
 * It is Compose state, so changing it redraws every screen at once.
 */
object Lang {
    val all = listOf("ca" to "Català", "es" to "Español", "en" to "English", "fr" to "Français")

    var current by mutableStateOf("ca")
        private set

    fun init(context: Context) {
        val saved = Prefs(context).lang
        current = if (saved.isNotEmpty()) saved else when (Locale.getDefault().language) {
            "es" -> "es"; "en" -> "en"; "fr" -> "fr"; else -> "ca"
        }
    }

    fun set(context: Context, code: String) {
        Prefs(context).lang = code
        current = code
    }
}

private fun tr(ca: String, es: String, en: String, fr: String): String = when (Lang.current) {
    "es" -> es; "en" -> en; "fr" -> fr; else -> ca
}

/** French "de"/"d'" before a name. */
private fun frDe(name: String) =
    if (name.firstOrNull()?.lowercaseChar()?.let { it in "aeiouhàâéèêîôû" } == true) "d'$name" else "de $name"

/**
 * Every word the app shows or sends, in the four languages side by side so a
 * translation can be checked next to the original. The Spanish, English and
 * French are machine translations and have not been reviewed by a person.
 * Each is a getter, so it follows the current language every time it is read.
 */
object S {
    // ---- Maria Rosa's screen ----
    val noContacts get() = tr("Sense contactes", "Sin contactos", "No contacts", "Aucun contact")
    val listening get() = tr("Escoltant…", "Escuchando…", "Listening…", "Écoute…")
    val tapToStop get() = tr("Toca per parar", "Toca para parar", "Tap to stop", "Touchez pour arrêter")
    fun newMessageFrom(c: Contact) = when (Lang.current) {
        "es" -> "Mensaje nuevo de ${c.name}"
        "en" -> "New message from ${c.name}"
        "fr" -> "Nouveau message ${frDe(c.name)}"
        else -> "Missatge nou ${c.ofFull}"
    }
    val tapToListen get() = tr("Toca aquí per escoltar", "Toca aquí para escuchar", "Tap here to listen", "Touchez ici pour écouter")
    val hello get() = tr("Hola!", "¡Hola!", "Hello!", "Bonjour !")
    val noNewMessage get() = tr("Cap missatge nou", "Ningún mensaje nuevo", "No new messages", "Aucun nouveau message")
    fun recording(t: String) = tr("Gravant… $t", "Grabando… $t", "Recording… $t", "Enregistrement… $t")
    val tapToSend get() = tr("Toca per enviar", "Toca para enviar", "Tap to send", "Touchez pour envoyer")
    val sending get() = tr("Enviant…", "Enviando…", "Sending…", "Envoi…")
    val sent get() = tr("Enviat ✔", "Enviado ✔", "Sent ✔", "Envoyé ✔")
    fun willReceive(c: Contact) = when (Lang.current) {
        "es" -> "${c.name} lo recibirá"
        "en" -> "${c.name} will get it"
        "fr" -> "${c.name} le recevra"
        else -> "${c.fullCapital} ho rebrà"
    }
    val sendFailed get() = tr("No s'ha pogut enviar", "No se ha podido enviar", "Could not send", "Échec de l'envoi")
    val tryAgain get() = tr("Torna-ho a provar", "Inténtalo de nuevo", "Try again", "Réessayez")
    val discarded get() = tr("Esborrat", "Borrado", "Deleted", "Supprimé")
    val notSent get() = tr("No s'ha enviat", "No se ha enviado", "It was not sent", "Non envoyé")
    fun replyTo(c: Contact) = when (Lang.current) {
        "es" -> "Responde a ${c.name}"
        "en" -> "Reply to ${c.name}"
        "fr" -> "Répondre à ${c.name}"
        else -> "Respon a ${c.full}"
    }
    val tapAndSpeak get() = tr("Toca i parla", "Toca y habla", "Tap and speak", "Touchez et parlez")
    fun sendingIn(n: Int) = tr("S'enviarà en $n", "Se enviará en $n", "Sending in $n", "Envoi dans $n")
    val sendNowHint get() = tr(
        "Toca aquí per enviar ara · X per esborrar",
        "Toca aquí para enviar ahora · X para borrar",
        "Tap here to send now · X to delete",
        "Touchez ici pour envoyer maintenant · X pour supprimer",
    )
    val programmingDoor get() = tr("⚙ Programació", "⚙ Programación", "⚙ Programming", "⚙ Programmation")
    val noInternet get() = tr("Sense connexió a internet", "Sin conexión a internet", "No internet connection", "Pas de connexion internet")
    val pin get() = "PIN"
    val pinWrong get() = tr("PIN incorrecte", "PIN incorrecto", "Wrong PIN", "PIN incorrect")
    val enter get() = tr("Entra", "Entrar", "Enter", "Entrer")
    val cancel get() = tr("Cancel·la", "Cancelar", "Cancel", "Annuler")

    val setupTitle get() = tr("Falta configurar l'aplicació", "Falta configurar la aplicación", "The app still needs to be set up", "L'application doit encore être configurée")
    val setupHint get() = tr(
        "Toca «Programació» a dalt a la dreta.",
        "Toca «Programación» arriba a la derecha.",
        "Tap \"Programming\" at the top right.",
        "Touchez « Programmation » en haut à droite.",
    )
    val micTitle get() = tr("Permís del micròfon", "Permiso del micrófono", "Microphone permission", "Autorisation du microphone")
    val micBody get() = tr(
        "Aquesta app fa servir el micròfon només quan toques el botó de respondre, per gravar la teva veu. Cada gravació s'envia com a nota de veu, a través de Telegram, a la persona que has triat. L'app no la guarda un cop enviada i no la comparteix amb ningú més. Si no ho permets, no podràs respondre per veu.",
        "Esta app usa el micrófono solo cuando tocas el botón de responder, para grabar tu voz. Cada grabación se envía como nota de voz, a través de Telegram, a la persona que has elegido. La app no la guarda una vez enviada y no la comparte con nadie más. Si no lo permites, no podrás responder por voz.",
        "This app uses the microphone only when you tap the reply button, to record your voice. Each recording is sent as a voice message, through Telegram, to the person you chose. The app does not keep it once sent and does not share it with anyone else. If you do not allow it, you will not be able to reply by voice.",
        "Cette application utilise le microphone uniquement lorsque vous touchez le bouton de réponse, pour enregistrer votre voix. Chaque enregistrement est envoyé comme message vocal, via Telegram, à la personne que vous avez choisie. L'application ne le conserve pas une fois envoyé et ne le partage avec personne d'autre. Si vous ne l'autorisez pas, vous ne pourrez pas répondre par la voix.",
    )
    val micContinue get() = tr("Continua", "Continuar", "Continue", "Continuer")
    val micNotNow get() = tr("Ara no", "Ahora no", "Not now", "Pas maintenant")
    val micBtn get() = tr("Permís del micròfon", "Permiso del micrófono", "Microphone permission", "Autorisation du microphone")
    val waiting get() = tr("Esperant missatges", "Esperando mensajes", "Waiting for messages", "En attente de messages")

    // ---- What the bot says to the other person on Telegram ----
    val heard get() = tr("✔ Ho ha escoltat", "✔ Lo ha escuchado", "✔ Heard", "✔ Écouté")
    fun unknown(id: Long) = tr(
        "Aquest aparell encara no et coneix. El teu identificador és $id.",
        "Este aparato aún no te conoce. Tu identificador es $id.",
        "This device does not know you yet. Your identifier is $id.",
        "Cet appareil ne vous connaît pas encore. Votre identifiant est $id.",
    )
    val voiceOnly get() = tr(
        "De moment aquest aparell només rep notes de veu. Mantén premut el micròfon per enviar-ne una.",
        "De momento este aparato solo recibe notas de voz. Mantén pulsado el micrófono para enviar una.",
        "For now this device only receives voice messages. Hold the microphone to send one.",
        "Pour l'instant cet appareil ne reçoit que des messages vocaux. Maintenez le micro pour en envoyer un.",
    )

    // ---- Programació ----
    val settingsTitle get() = tr("Programació", "Programación", "Programming", "Programmation")
    val settingsIntro get() = tr(
        "Aquí es canvia la configuració de l'app. Només ho ha de fer un adult.",
        "Aquí se cambia la configuración de la app. Solo debe hacerlo un adulto.",
        "Here the app's settings are changed. Only an adult should do it.",
        "Ici on modifie les réglages de l'application. Seul un adulte doit le faire.",
    )
    val openGuide get() = tr("Obre la guia pas a pas", "Abrir la guía paso a paso", "Open the step-by-step guide", "Ouvrir le guide pas à pas")
    val nowTitle get() = tr("Com està ara", "Cómo está ahora", "How things stand", "État actuel")
    val wifiOk get() = tr("Internet: connectat ✔", "Internet: conectado ✔", "Internet: connected ✔", "Internet : connecté ✔")
    val wifiNo get() = tr(
        "Internet: no connectat. Sense internet no es poden rebre ni enviar missatges.",
        "Internet: no conectado. Sin internet no se pueden recibir ni enviar mensajes.",
        "Internet: not connected. Without internet no messages can be received or sent.",
        "Internet : non connecté. Sans internet, impossible de recevoir ou d'envoyer des messages.",
    )
    val connectWifi get() = tr("Connecta al Wi-Fi", "Conectar al Wi-Fi", "Connect to Wi-Fi", "Se connecter au Wi-Fi")
    val changeWifi get() = tr("Canvia de Wi-Fi", "Cambiar de Wi-Fi", "Change Wi-Fi", "Changer de Wi-Fi")
    fun botOk(n: String) = tr("Bot de Telegram: @$n ✔", "Bot de Telegram: @$n ✔", "Telegram bot: @$n ✔", "Bot Telegram : @$n ✔")
    val botPending get() = tr(
        "Bot de Telegram: pendent de configurar. Obre la guia pas a pas.",
        "Bot de Telegram: pendiente de configurar. Abre la guía paso a paso.",
        "Telegram bot: not set up yet. Open the step-by-step guide.",
        "Bot Telegram : à configurer. Ouvrez le guide pas à pas.",
    )
    val botChecking get() = tr("Bot de Telegram: comprovant…", "Bot de Telegram: comprobando…", "Telegram bot: checking…", "Bot Telegram : vérification…")
    val contactsTitle get() = tr("Contactes", "Contactos", "Contacts", "Contacts")
    val contactsHint get() = tr(
        "Per afegir algú, que escrigui qualsevol missatge al bot de Telegram. Quan ho faci, aquí sota sortirà un requadre groc amb el botó «Afegeix-la com a contacte».",
        "Para añadir a alguien, que escriba cualquier mensaje al bot de Telegram. Cuando lo haga, aquí abajo saldrá un recuadro amarillo con el botón «Añadir como contacto».",
        "To add someone, they just write any message to the Telegram bot. When they do, a yellow box with the button \"Add as a contact\" will appear below.",
        "Pour ajouter quelqu'un, il suffit qu'il écrive n'importe quel message au bot Telegram. Dès qu'il l'aura fait, un encadré jaune avec le bouton « Ajouter comme contact » apparaîtra ci-dessous.",
    )
    val fixSettingsIntro get() = tr(
        "Així el mòbil només serveix per a aquesta app. Android et demanarà que ho confirmis. Per sortir-ne, toca «Deixa de fixar» o, si no hi ets, mantén premuts Enrere i Recents.",
        "Así el móvil solo sirve para esta app. Android te pedirá que lo confirmes. Para salir, toca «Dejar de fijar» o, si no estás, mantén pulsados Atrás y Recientes.",
        "This way the phone is only used for this app. Android will ask you to confirm. To leave, tap \"Stop pinning\" or, if you are not here, hold Back and Recents.",
        "Ainsi le téléphone ne sert qu'à cette application. Android vous demandera de confirmer. Pour en sortir, touchez « Ne plus épingler » ou, si vous n'êtes pas là, maintenez Retour et Récents.",
    )
    val removeAsk get() = tr(
        "Si prems aquest botó, perdràs el contacte. N'estàs segur?",
        "Si pulsas este botón, perderás el contacto. ¿Estás seguro?",
        "If you press this button, you will lose the contact. Are you sure?",
        "Si vous appuyez sur ce bouton, vous perdrez le contact. Êtes-vous sûr ?",
    )
    val removeYes get() = tr("Sí, treu-lo", "Sí, quitarlo", "Yes, remove it", "Oui, le retirer")
    val removeNo get() = tr("No, deixa'l", "No, dejarlo", "No, keep it", "Non, le garder")
    fun wroteBot(name: String, id: Long) = tr(
        "Ha escrit al bot: $name ($id)", "Ha escrito al bot: $name ($id)", "Wrote to the bot: $name ($id)", "A écrit au bot : $name ($id)",
    )
    val addHer get() = tr("Afegeix-la com a contacte", "Añadir como contacto", "Add as a contact", "Ajouter comme contact")
    val nameLabel get() = tr("Nom (surt al botó)", "Nombre (sale en el botón)", "Name (shown on the button)", "Nom (affiché sur le bouton)")
    val articleLabel get() = tr("Article davant del nom", "Artículo delante del nombre", "Article before the name", "Article devant le nom")
    val none get() = tr("cap", "ninguno", "none", "aucun")
    fun willShow(c: Contact) = tr(
        "Es llegirà: «${replyTo(c)}»", "Se mostrará: «${replyTo(c)}»", "It will read: \"${replyTo(c)}\"", "Il affichera : « ${replyTo(c)} »",
    )
    val telegramNumber get() = tr("Número de Telegram", "Número de Telegram", "Telegram number", "Numéro Telegram")
    val visible get() = tr("Surt a la pantalla", "Sale en la pantalla", "Shown on the screen", "Affiché à l'écran")
    val hidden get() = tr("Amagat", "Oculto", "Hidden", "Masqué")
    val removeContact get() = tr("Treu aquest contacte", "Quitar este contacto", "Remove this contact", "Retirer ce contact")
    val maxContacts get() = tr(
        "Màxim 5 contactes: és el que cap a la pantalla amb la lletra llegible.",
        "Máximo 5 contactos: es lo que cabe en la pantalla con letra legible.",
        "Maximum 5 contacts: that is what fits on the screen with readable letters.",
        "5 contacts au maximum : c'est ce qui tient à l'écran avec des lettres lisibles.",
    )
    val addManual get() = tr("Afegeix un contacte a mà", "Añadir un contacto a mano", "Add a contact by hand", "Ajouter un contact à la main")
    val soundTitle get() = tr("So", "Sonido", "Sound", "Son")
    val soundOn get() = tr("Sona un avís quan arriba un missatge", "Suena un aviso cuando llega un mensaje", "A chime plays when a message arrives", "Un signal sonore retentit à l'arrivée d'un message")
    val soundOff get() = tr("Sense avís sonor", "Sin aviso sonoro", "No chime", "Pas de signal sonore")
    val protectTitle get() = tr("Protegir aquesta pantalla", "Proteger esta pantalla", "Protect this screen", "Protéger cet écran")
    val protectBody get() = tr(
        "Un PIN de 4 xifres evita que ningú més canviï res aquí (per exemple, els nets). No és el codi per desbloquejar el mòbil.",
        "Un PIN de 4 cifras evita que nadie más cambie nada aquí (por ejemplo, los nietos). No es el código para desbloquear el móvil.",
        "A 4-digit PIN stops anyone else from changing anything here (for example, the grandchildren). It is not the code that unlocks the phone.",
        "Un code PIN à 4 chiffres évite que quelqu'un d'autre change quoi que ce soit ici (par exemple les petits-enfants). Ce n'est pas le code de déverrouillage du téléphone.",
    )
    val pinField get() = tr("PIN (4 xifres; buit = sense PIN)", "PIN (4 cifras; vacío = sin PIN)", "PIN (4 digits; empty = no PIN)", "PIN (4 chiffres ; vide = sans PIN)")
    val pinMust4 get() = tr("El PIN ha de tenir 4 xifres.", "El PIN debe tener 4 cifras.", "The PIN must have 4 digits.", "Le PIN doit avoir 4 chiffres.")
    val pinNote get() = tr(
        "Apunta'l: si s'oblida, només es pot treure reinstal·lant l'app.",
        "Anótalo: si se olvida, solo se puede quitar reinstalando la app.",
        "Write it down: if forgotten, it can only be removed by reinstalling the app.",
        "Notez-le : en cas d'oubli, on ne peut l'enlever qu'en réinstallant l'application.",
    )
    val pinAppTitle get() = tr("Fixar l'app", "Fijar la app", "Pin the app", "Épingler l'application")
    val unpin get() = tr("Deixa de fixar", "Dejar de fijar", "Stop pinning", "Ne plus épingler")
    val batteryTitle get() = tr("Bateria", "Batería", "Battery", "Batterie")
    val batteryBody get() = tr(
        "Android pot aturar l'app per estalviar bateria, i llavors no arribarien els missatges. Cal permetre que funcioni sempre.",
        "Android puede parar la app para ahorrar batería, y entonces no llegarían los mensajes. Hay que permitir que funcione siempre.",
        "Android may stop the app to save battery, and then messages would not arrive. It must be allowed to run all the time.",
        "Android peut arrêter l'application pour économiser la batterie, et les messages n'arriveraient plus. Il faut l'autoriser à fonctionner en permanence.",
    )
    val batteryBtn get() = tr("Permet que funcioni sempre", "Permitir que funcione siempre", "Allow it to run all the time", "Autoriser le fonctionnement permanent")
    val saveBack get() = tr("Desa i torna", "Guardar y volver", "Save and go back", "Enregistrer et revenir")
    val backNoSave get() = tr("Torna sense desar", "Volver sin guardar", "Go back without saving", "Revenir sans enregistrer")
    val languageTitle get() = tr("Idioma", "Idioma", "Language", "Langue")
    /** Shown only in the languages that are machine translations. */
    val autoTranslation get() = tr(
        "",
        "Traducción automática: puede contener errores.",
        "Machine translation: it may contain errors.",
        "Traduction automatique : elle peut contenir des erreurs.",
    )
}
