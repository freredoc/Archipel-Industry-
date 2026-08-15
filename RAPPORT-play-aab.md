# RAPPORT — Paquet Play Store : montée de chaîne API 36 (P1), puis `.aab` magasin (P2)

Brief : `BRIEFplayaab.md`. **Brief NON pré-compilé** (pas de SDK/Gradle côté rédaction) : ni SHA, ni
sortie de compilation à comparer, et ses versions d'outils étaient explicitement à re-vérifier. Le
banc d'essai est donc le `workflow_dispatch` **depuis la branche**, qui construit et téléverse **sans
rien publier** (les étapes à effet de bord sont gatées sur `refs/heads/main`).

## Version produite

| | |
|---|---|
| `GAME_BUILD` | **423** (422 → 423) |
| `GAME_VERSION` | **Alpha 19.0** (Alpha 18.9 → Alpha 19.0) |
| `SAVE_VERSION` | **31, INCHANGÉ** — aucun changement de jeu, le HTML ne bouge que par son bump |

Numéro relevé sur **les 64 branches distantes**, pas seulement `main` : maximum = 422 → 423 libre.

⚠ **Le bump est dans le commit P1**, pas P2 : le brief prévoit que P1 puisse être mergé seul, et un
merge sans bump republierait un APK différent sous le build 422 — exactement la collision de numéro
documentée au build 298 (pas de notification de mise à jour, cache SW inchangé).

`GAME_NOTES` (UTF-8 littéral, aucun guillemet droit) :

> Mise a jour technique : le jeu passe sur la chaine de compilation Android 16, une etape
> obligatoire pour deposer l'application sur les magasins. Rien ne change dans la partie.

## Base

`main` au moment de l'exécution : **build 422 / Alpha 18.9** (le brief décrivait l'état du 14/08 ;
quatre livraisons d'autres sessions sont passées depuis). La PR précédente étant mergée, la branche a
été **repartie de `main`**. Vérifié avant de commencer : `android/` et `.github/workflows/android.yml`
**intacts depuis le lot 18.4** (`git diff` vide), et le lot 18.4 lui-même en place
(`SELF_UPDATE = true` et `SUPPORT_URL` Ko-fi, 1 occurrence chacun).

---

# P1 — Montée de chaîne API 36

## Versions RETENUES, et la source consultée

Aucune version n'a été recopiée du brief (qui donnait « ≥ 8.9 **de mémoire** »).

| | Avant | **Retenu** | Source |
|---|---|---|---|
| AGP | 8.5.2 | **8.13.0** | notes de version AGP 8.13 : *« The maximum API level that Android Gradle Plugin 8.13 supports is API level 36.1 »*, **Gradle minimum 8.13**, SDK Build Tools min 35.0.0, **JDK 17** |
| Wrapper Gradle | 8.7 | **8.13** | idem (minimum exigé par AGP 8.13) |
| Plateforme SDK (CI) | `android-34` | **`android-36`** | « Set up the Android 16 SDK » |
| Build Tools (CI) | `34.0.0` | **`36.0.0`** | idem |
| `compileSdk` / `targetSdk` | 34 / 34 | **36 / 36** | « Set up the Android 16 SDK » : plancher **AGP 8.9.0-rc01** pour `compileSdk 36` |
| `minSdk` | 26 | **26 inchangé** | — |
| JDK | 17 | **17 inchangé** | AGP 8.13 exige 17 |

⚠ **AGP 8.13 = dernier de la ligne 8.x, choix délibéré.** Il couvre l'API 36 sans imposer la migration
de MAJEURE vers AGP 9.x, qui exigerait **Gradle 9.5** et ses ruptures de DSL. AGP 9.3 (juillet 2026)
monte à l'API 37 — inutile ici, et c'est du risque pur pour un module WebView sans dépendances.

⚠ **`buildToolsVersion '36.0.0'` ÉPINGLÉ** (ajout hors liste du brief) : sans cette ligne, AGP 8.13
prend **sa** valeur par défaut, 35.0.0, alors que la CI n'installe que 36.0.0 → téléchargement
implicite. Les deux lignes doivent désormais bouger ensemble, c'est écrit dans les deux fichiers.
Un contrôle `ls .../build-tools/36.0.0/aapt2` est ajouté tôt dans la CI, parce que l'étape
« Assert appIds » **échoue volontairement** si elle ne trouve pas d'aapt2.

## Les deux comportements d'Android 16 — traités, alors que le brief les classait en risques

⚠ **ÉCART ASSUMÉ ET DÉLIBÉRÉ.** Le brief listait le travail P1 en cinq points (AGP, wrapper, CI, SDK,
ne pas toucher `useAndroidX`) et rangeait l'edge-to-edge et l'orientation parmi les « risques à
valider sur appareil ». **Livrer P1 sans les traiter aurait fait échouer V3 par construction** : le
critère d'acceptation de V3 est précisément « la barre d'outils du bas reste entièrement cliquable ».

**(a) Edge-to-edge imposé.** Vérifié sur la page officielle des changements de comportement d'Android
16 : *« For apps targeting Android 16 (API level 36), `R.attr#windowOptOutEdgeToEdgeEnforcement` is
deprecated and disabled, and your app can't opt-out of going edge-to-edge. »* La WebView dessine donc
sous les barres système, et la réserve d'espace que le framework faisait disparaît → **la barre
d'outils du bas passerait derrière les trois boutons de navigation**.

Nouveau `MainActivity.applyInsetPadding()` : rembourrage par l'**union `systemBars() | displayCutout()`**
puis **consommation** des insets. On reproduit ainsi exactement la réserve d'espace d'avant
targetSdk 36, sans dépendre du mode d'encoche retenu par la version d'Android.

- ⚠ **Conséquence à connaître** : la WebView ne débordant plus sous les barres, `env(safe-area-inset-*)`
  y vaut **0** — le rembourrage CSS du lot A devient **inerte dans l'APK**, et reste actif en web/PWA
  où c'est le navigateur qui gère la zone sûre. Les deux chemins ne se cumulent donc **jamais**.
- ⚠ **Auto-correcteur sur Android ≤ 15** : le framework y applique encore les insets lui-même, le
  gestionnaire reçoit des valeurs nulles et ne rembourre rien → **pas de double marge**.
- ⚠ **Réversible en un bloc** si Ethan préfère éprouver d'abord le chemin CSS pur : supprimer
  `applyInsetPadding()` et son appel suffit.

`hideSystemBars()` passe par **`WindowInsetsController`** sur API 30+ (`setSystemUiVisibility` est
déprécié et n'est plus fiable une fois l'edge-to-edge imposé). Masquer la barre de statut met son
inset à 0 → le rembourrage du haut retombe à l'encoche seule, comme aujourd'hui.

**(b) Verrou d'orientation levé.** *« For apps targeting Android 16, orientation, resizability and
aspect ratio restrictions no longer apply on displays with smallest width >= 600dp »* — **mais** la
même page liste les exceptions, dont : *« Games (based on `android:appCategory`) »*. Fermé par
**`android:appCategory="game"`**, une ligne, et factuellement exacte.

⚠ **C'est l'exception PÉRENNE.** L'autre voie documentée,
`PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY`, est explicitement **temporaire** : *« The opt-out is
temporary and won't apply when targeting API level 37 »*. Elle n'a donc pas été retenue.

---

# P2 — Variante magasin et `.aab`

**Une bascule `-PstoreBuild=true`, pas de *flavor*, pas de second `MainActivity`.** Les tâches
existantes (`assembleDebug` ×2) ne changent ni de nom ni de comportement.

- **`AndroidManifest-store.xml`** : identique au manifeste hors magasin, **sans
  `REQUEST_INSTALL_PACKAGES` ni `INTERNET`**. Les deux fichiers ne diffèrent que par ces lignes.
- **`sourceSets`** sélectionne le fichier ; **`buildFeatures { buildConfig true }`** (désactivé par
  défaut en AGP 8) + **`buildConfigField SELF_UPDATE`**.
- **`MainActivity`** : sortie immédiate en tête de `WebBridge.update(...)` et `registerInstallReceiver()`
  non appelé. **`saveText`, le sélecteur de sauvegarde et l'ouverture des liens externes : intacts.**

⚠ **Ce que le binaire magasin contient encore de l'updater, en une ligne** : tout le code de
`downloadAndInstall` / `installApk` / `PackageInstaller` **reste présent, simplement inatteignable** —
verrou plutôt qu'ablation, un seul chemin de code ; ce que Play évalue réellement est la **permission
déclarée**, et elle disparaît.

⚠ **Retrait d'`INTERNET`** justifié sur `game-store.html` : les deux `fetch(VERSION_URL…)` sont
derrière `SELF_UPDATE` (à `false` dans cette variante) et les `fetch` du service worker relèvent du
chemin PWA, inerte en WebView. **À confirmer par un boot réel — c'est l'objet de T4, non exécuté.**

## Écarts au brief, avec leurs raisons

1. **`storeBuild` lit la VALEUR de la propriété**, pas seulement sa présence. Le brief proposait
   `project.hasProperty('storeBuild')`, vrai **même pour `-PstoreBuild=false`** : on produirait une
   variante magasin en croyant l'avoir désactivée.
2. **Parenthèses obligatoires autour du ternaire de `manifest.srcFile`.** `manifest.srcFile a ? b : c`
   est une *command expression* sans parenthèses suivie d'un ternaire : Groovy la lit
   `srcFile(a) ? b : c`.
3. **Un APK magasin est construit EN PLUS du bundle** (`assembleRelease`, mêmes propriétés).
   `aapt2 dump badging` **ne lit pas un `.aab`** ; sur l'APK il donne le paquet, le `targetSdk`, les
   permissions et l'état debuggable. **T1 devient donc automatisé en CI, sans dépendre de
   `bundletool`** — et c'est aussi l'APK qu'Ethan installe pour T4, que le brief réclamait de toute
   façon. La signature du bundle, elle, se lit avec **`jarsigner`** (signature JAR), pas `apksigner`.
4. **Assertion `minSdk` corrigée après un premier run.** Voir « Ce qui a cassé », ci-dessous.
5. **Traitement des deux comportements d'Android 16 dans P1** (cf. supra).

## Ce qui a cassé, et pourquoi c'était le contrôle et non le paquet

**Run 549** : toute la chaîne a construit sans erreur (`BUILD SUCCESSFUL in 23s`, `bundleRelease`,
`assembleRelease`, `signReleaseBundle`), les assertions d'asset et leur contre-mesure sont passées,
et le badging a donné `package: name='fr.archipel.industry.store' versionCode='423' versionName='Alpha 19.0'
compileSdkVersion='36'` + `targetSdkVersion:'36'`.

La seule étape en échec était **mon assertion `sdkVersion:'26'`** : l'`aapt2` de **build-tools 36
n'émet plus cette ligne sous ce nom**. `minSdk` ne fait pas partie des critères T1 du brief —
l'assertion était un ajout de ma part. Corrigée pour accepter `sdkVersion:` **ou** `minSdkVersion:`,
**en échouant si aucune n'est trouvée** (un garde qui se désarme tout seul ne garde rien), et le dump
de l'en-tête badging est conservé dans la CI pour le prochain qui doute.

---

## Validation CI — run **550** (`workflow_dispatch` sur la branche, commit `1d06936`) : **succès**

Les 18 étapes ont tourné ; les deux étapes à effet de bord ont bien été **sautées** (gate de branche).

| Test | Verdict | Montage effectivement exécuté |
|---|---|---|
| **V1** — les deux APK se construisent | **PASS** | `Build PUBLIC APK` 40 s, `Build DEV APK` 12 s, `Assert appIds` OK, `Show signing certificate` retrouve l'empreinte stable **`a259f777…3962a3`** sur les 3 APK (publique, dev **et** magasin) |
| **V2** — `aapt2 dump badging` | **PASS** | `package: name='fr.archipel.industry.store' versionCode='423' versionName='Alpha 19.0' compileSdkVersion='36'` + `targetSdkVersion:'36'`. ⚠ `minSdk` : voir « Ce qui a cassé » — l'étiquette `sdkVersion:` a disparu avec build-tools 36, l'assertion accepte désormais les deux noms |
| **T1** — manifeste du paquet | **PASS** | Étape `Assert store package`, **sur l'APK construit avec le MÊME manifeste et les MÊMES propriétés** (`aapt2` ne lit pas un `.aab`) : appId `fr.archipel.industry.store`, `versionCode` = `GAME_BUILD` (423), `targetSdkVersion` 36, **0 `uses-permission`**, **pas de `application-debuggable`**. Chaque contrôle est un `exit 1` → l'étape verte vaut assertion. **Contre-mesure jouée** : l'APK **publique** doit en déclarer exactement **2** — sinon c'est le compteur qui est faux, pas le paquet qui est propre |
| **T2** — asset embarqué | **PASS** | Asset **extrait du bundle** (`unzip -p ArchipelIndustry.aab base/assets/index.html`, et non le fichier intermédiaire) : `const SELF_UPDATE = false;` présent, `ko-fi` à **0**, `const DEV_BUILD = false;` présent. **CONTRE-MESURE EXÉCUTÉE** : les mêmes greps sur l'asset de l'**APK publique** (`unzip -p ArchipelIndustry.apk assets/index.html`) donnent l'**inverse** — `SELF_UPDATE = true` et **1** occurrence de `ko-fi`. Sans elle, un extracteur rendant une chaîne vide passerait au vert. Sortie CI : *« asset magasin et contre-mesure publique : conformes »* |
| **T3** — signature | **PASS** | `jarsigner -verify` sur le **`.aab`** (`apksigner` ne lit pas un bundle) : **`jar verified.`**, `Signed by "CN=Archipel Industry, OU=Game, O=Archipel, L=Paris, ST=IDF, C=FR"`, digest SHA-256 → **clé stable, pas la clé debug**. L'APK magasin porte la même empreinte que les deux autres |
| **V3** — appareil réel | **NON EXÉCUTÉ** | Exige un appareil. En attente d'Ethan (cf. « Points laissés en suspens ») |
| **T4** — boot sans permission | **NON EXÉCUTÉ** | Idem. `ArchipelIndustryStore.apk` est fourni dans l'artefact pour ça |

**Artefacts produits** (téléversés, **rien publié**) : `ArchipelIndustry-APK` (2 APK, 3 281 499 o) et
`ArchipelIndustry-STORE` (`ArchipelIndustry.aab` + `ArchipelIndustryStore.apk` + `game-store.html`,
4 365 633 o).

⚠ **Run 549, la passe précédente, est instructif et reste au dossier** : la chaîne **construisait déjà
intégralement** (`BUILD SUCCESSFUL in 23s`, `bundleRelease`, `assembleRelease`, `signReleaseBundle`) —
AGP 8.13 + Gradle 8.13 + API 36 + `sourceSets` + `buildConfig` étaient bons du premier coup. Seule
mon assertion `minSdk` échouait.

## Les étapes existantes n'ont pas changé de comportement

- `Prepare game files` : les gardes d'entrée/sortie de la variante magasin (lot 18.4) et la
  **contre-garde** sur la publique passent toujours.
- `Build PUBLIC APK` / `Build DEV APK` : inchangées, assertions d'asset vertes.
- **`Assert appIds` : NON TOUCHÉE** — elle compare toujours **deux** APK ; le bundle a ses assertions
  propres, dans une étape dédiée (`aapt2 dump badging` ne lit pas un `.aab`).
- `Show signing certificate` : étendue au `.aab` (jarsigner) et à l'APK magasin — additif.
- `Sync PWA` : `PWA build=423`, `sw.js -> var CACHE = 'archipel-423'`, assertion `DEV_BUILD = false`
  sur `index.html` verte.
- `Publish to "apk-latest"` et `Sync version.json` : **sautées**, gate `refs/heads/main` respecté —
  le dispatch depuis la branche n'a **rien publié**, comme prévu.

## Points laissés en suspens

- **V3 et T4 : NON EXÉCUTÉS, en attente d'Ethan.** Ils exigent un appareil réel et ne peuvent pas
  être portés au vert autrement. Le RAPPORT devra mentionner **le modèle d'appareil et le mode de
  navigation (gestes ou 3 boutons)** : un appareil sans encoche à navigation gestuelle a des insets
  quasi nuls et validerait à vide.
  - **V3** — installer l'APK **dev**, vérifier en portrait **puis en paysage** que le HUD n'est masqué
    ni par l'encoche ni par la barre de navigation, que la barre d'outils du bas reste entièrement
    cliquable, et que la scène ne saute pas au changement d'orientation.
  - **T4** — installer `ArchipelIndustryStore.apk` (artefact `ArchipelIndustry-STORE`), lancer :
    le jeu démarre, une partie se charge, **asserter d'abord que le panneau Options est OUVERT**,
    puis constater que la section « Soutenir le projet » **et** le bouton « Vérifier les mises à
    jour » sont **absents**. Vérifier l'absence de `SecurityException` liée à `INTERNET` dans
    `logcat`. ⚠ La consigne « `useGhostGuard` avale le premier clic » **ne vaut pas** pour le bouton
    Options : il ouvre au premier clic et le second le referme.
- **P1 ne se merge pas avant V3** (consigne du brief).
- **Dépôt Play** : hors périmètre. Le dépôt, le choix de Play App Signing et la piste de test fermé
  appartiennent à Ethan. ⚠ **Le `versionCode` d'un dépôt accepté n'est jamais réutilisable** ; comme
  il dérive de `GAME_BUILD`, **chaque dépôt consomme définitivement un numéro de build**, même retiré.
- **App Store** : risque de rejet 4.2 « fonctionnalité minimale » pour un jeu emballé en WebView, et
  persistance de `localStorage` en WKWebView (purge possible → sauvegardes perdues). À instruire à
  part, avant tout empaquetage iOS.
- **`version.json` / `index.html` / `sw.js`** non touchés : la PWA reste l'édition **publique**, hors
  magasin (avec lien de soutien et auto-updater). La CI les régénère après un build sur `main`.
