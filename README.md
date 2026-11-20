# Comunicador Maria Rosa — bústia de veu per a un mòbil dedicat

App d'Android amb **una sola pantalla i dos botons grans**, pensada perquè una persona gran
que no sap fer anar un mòbil pugui **escoltar i respondre notes de veu** d'una amiga o familiar.
No té número de telèfon, ni WhatsApp, ni navegador: només això.

Funciona a través d'un **bot de Telegram**. La persona que escriu (un familiar o una amiga)
fa servir el Telegram de sempre; l'aparell de l'altra banda mostra la nota com un botó que
s'escolta amb un toc.

> Estat: fases 1 i 2 funcionen i s'han provat en un Xiaomi Redmi Note 15 (Android 16).
> Encara **no** hi ha mode quiosc ni ús continuat en un entorn real. Vegeu `docs/FASES.md`.

---

## Què fa

- **Missatge nou:** arriba una nota de veu → la pantalla s'encén (encara que estigués apagada),
  sona un avís suau de tres notes **una sola vegada**, i el botó de dalt es posa **verd**.
- **Escoltar:** un toc al botó verd reprodueix la nota. Un altre toc la para. Si no n'hi ha de nova,
  un toc repeteix l'última. Qui ha enviat rep al Telegram «✔ Ho ha escoltat».
- **Respondre:** botó de baix. Un toc comença a gravar (vermell, amb el temps), un altre toc acaba.
  Després hi ha **5 segons per canviar d'idea**: una **X vermella** esborra la nota; si no es toca, s'envia sola.
- **Només veu**, als dos sentits. Si algú escriu text, el bot respon que de moment només rep notes de veu.
- Si no hi ha internet, ho diu a la pantalla: «Sense connexió a internet».
- Es manté en marxa amb la pantalla apagada (servei en primer pla) i **arrenca sola** quan es reinicia el mòbil.

## Peces

| Fitxer | Què fa |
|---|---|
| `app/src/main/assets/config.json` | Contactes (nom, identificador de Telegram) i interruptors de cada fase |
| `app/.../Messenger.kt` | Tot el que passa darrere la pantalla: rebre, reproduir, gravar, compte enrere, avisos |
| `app/.../Telegram.kt` | Les crides al bot (llegir missatges, descarregar, enviar veu) |
| `app/.../MainActivity.kt` | La pantalla i els permisos |
| `app/.../MessengerService.kt` | El servei que manté l'app viva |
| `telegram.properties` | **El token del bot. Secret.** No es puja a cap repositori |
| `docs/FASES.md` | El pla per fases i el registre del que ha funcionat i el que no |

## `config.json`

```json
{
  "sendDelaySeconds": 5,
  "features": { "listen": true, "reply": true, "photos": false, "text": false },
  "contacts": [
    { "id": "familiar", "name": "Familiar", "active": true, "telegramId": 123456789 }
  ]
}
```

- `sendDelaySeconds`: segons per cancel·lar una nota abans que surti. `0` = s'envia a l'acte.
- `features.reply`: `false` amaga el botó de respondre. És la manera de **tornar enrere una fase**
  si és massa per a la persona: no cal tocar el codi, només canviar-ho i tornar a instal·lar.
- `contacts`: només es mostren els `active: true`. El `telegramId` és el número de xat de la persona.
  El **nom és el que surt al botó**. Sense `telegramId`, l'aparell respon a qui li escrigui amb el seu número, perquè l'hi puguis posar.
- `config.local.json` (mateixa carpeta, **no es puja a git**) té prioritat sobre `config.json`: aquí hi va la teva configuració real.

## Posar en marxa el bot (una sola vegada)

1. A Telegram, parla amb **@BotFather** (el verificat, amb el segell blau) i envia `/newbot`.
2. Tria un nom i un usuari que acabi en `bot`. Et dona un **token**.
3. Desa'l **sense escriure'l enlloc més** a `telegram.properties` (hi ha un `telegram.properties.example`), una línia: `token=EL_TOKEN`.
   Amb el token copiat al porta-retalls:
   ```bash
   printf 'token=%s\n' "$(pbpaste)" > telegram.properties && chmod 600 telegram.properties && pbcopy < /dev/null
   ```
4. Qui hagi d'escriure al bot l'ha de trobar pel nom d'usuari i tocar **Start**.

Si el token s'ha filtrat alguna vegada: a BotFather, `/revoke`, i desa el nou.

## Afegir una persona nova (fase 4)

1. Que escrigui «hola» al bot. L'aparell li respon amb **el seu identificador** (un número).
2. A `config.json`, afegeix-la amb `"active": true` i aquest número com a `telegramId`.
3. Torna a compilar i instal·lar. (Quan toqui, es pot fer a distància; ara no.)
4. Afegeix-ne **una a la vegada** i només quan la comunicació anterior estigui consolidada.

## Compilar i instal·lar

El Java és dins d'Android Studio, no al sistema:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug
```

L'APK queda a `app/build/outputs/apk/debug/app-debug.apk`.

**Posar-lo al mòbil.** En un Xiaomi no s'ha pogut activar «Instal·la a través de l'USB»; el camí que funciona és:

```bash
~/Library/Android/sdk/platform-tools/adb push app/build/outputs/apk/debug/app-debug.apk /sdcard/Download/MariaRosa.apk
```

i al mòbil: **Fitxers → Descàrregues → MariaRosa.apk → Instal·la**. Si surt un avís de seguretat, «Instal·la igualment».
Perquè `adb` el vegi: opcions de desenvolupador → **Depuració per USB**, i acceptar l'avís al mòbil.

## Configurar el mòbil de la persona (llista per no deixar-se res)

1. **Wi-Fi de casa de la persona** configurat abans de res. En mode quiosc no hi haurà Configuració.
2. Instal·lar l'APK i obrir-lo. Acceptar **micròfon**, **notificacions** i l'exempció de **bateria**.
3. Comprovar que el **volum** és alt (l'app el posa al màxim en obrir-se).
4. **Endollat sempre** i en un lloc fix.
5. **Xiaomi/HyperOS:** si la pantalla no s'encén sola en arribar un missatge, activar a l'app els permisos
   «Mostra a la pantalla de bloqueig», «Finestres emergents en segon pla» i «Inici automàtic».
   (A mòbils d'altres marques això costa menys. Per això no es recomana un Xiaomi per a l'aparell definitiu.)
6. Prova final: apagar la pantalla amb el botó lateral i enviar una nota des d'un altre aparell.
   Ha d'encendre's, sonar i posar-se verd.

## Si alguna cosa falla

| Què passa | Què mirar |
|---|---|
| No s'ha sentit l'avís | El so és propi de l'app (`res/raw/chime.wav`), no el del sistema; mirar el volum d'alarma |
| La pantalla no s'encén amb un missatge nou | Permisos de Xiaomi (punt 5), exempció de bateria, que sigui endollat |
| «Sense connexió a internet» | Wi-Fi de casa; el router pot haver canviat |
| «No s'ha pogut enviar» | Internet, o el permís del micròfon denegat |
| El bot respon «no et conec…» | Aquest número no és a `config.json`; afegir-lo (vegeu amunt) |
| No es pot instal·lar per cable | Fer-ho com està descrit: `adb push` i tocar el fitxer |

## Pendent

- **Mode quiosc:** app administradora del dispositiu, amb sortida oculta només per a adults (p. ex. cinc tocs i un PIN)
  per arribar als ajustos de Wi-Fi. Cal un aparell que es pugui restaurar de fàbrica, sense comptes.
- Pantalla que baixi la brillantor o s'apagui de nit (evita marcar-la i gastar bateria).
- Icona retocada, i fases 3–5 (fotos i text llegit en veu alta, més contactes, pregunta a la IA).

## Llicències

Codi sota **GNU GPL v3** (`LICENSE`). Tipografia **Atkinson Hyperlegible** (`OFL-AtkinsonHyperlegible.txt`).
