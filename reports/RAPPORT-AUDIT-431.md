# Rapport d'audit du code — base build 431 (Alpha 19.8)

Date : 2026-08-18 · Branche auditée : `main` (tête = build 431, lot Discord) via `claude/code-audit-qbbdio`.
Aucune modification de code : ce lot ne livre QUE ce rapport.

## Méthode

- Contrôles directs : extraction des 7 blocs `<script>` (compte vérifié = 7 AVANT de conclure) + `node --check`,
  équilibre des accolades CSS, cohérence des versions (jeu / `version.json` / `sw.js` / `index.html`),
  simulation locale des gardes CI, relevé `GAME_BUILD` sur TOUTES les branches distantes,
  **boot réel en Chromium headless** (viewport 420 px, DPR 3, locale fr).
- 3 passes d'audit approfondies en parallèle : coquille Android (`android/`), CI (`android.yml` + fichiers
  publiés), code mort / incohérences du mono-fichier.

## Verdict global

**Le projet est en bon état.** Aucun constat CRITIQUE. Les gardes CI sont toutes falsifiables et vertes sur la
base 431, les versions sont parfaitement synchronisées, le jeu boote sans erreur, et la coquille Android est
saine (commentaires conformes au code, réglages WebView sûrs par défaut). Les points à traiter sont des
durcissements (regex CI non ancrées, receiver Android pré-33) et un lot de nettoyage de code mort qui
s'accumule depuis plusieurs versions.

---

## 1. Intégrité vérifiée (RAS)

| Contrôle | Résultat |
|---|---|
| `node --check` sur les 7 blocs `<script>` | **7/7 OK** (extracteur séquentiel, purge + compte avant boucle) |
| Accolades CSS du bloc `<style>` | 955 / 955, équilibré |
| `GAME_BUILD` 431 · `GAME_VERSION` 'Alpha 19.8' · `SAVE_VERSION` 31 | cohérents avec `version.json` (431 / Alpha 19.8, notes == `GAME_NOTES` au caractère près) et `sw.js` (`archipel-431`) |
| `index.html` vs `Archipel_industry_alpha-7.html` | **byte-identiques** (normal : édition publique == source, `DEV_BUILD = false`) |
| Boot réel Chromium (jeu servi en HTTP, 8 s de jeu) | splash retiré, canvas peint, mode normal, `tickErrors` vide, **0 `pageerror`**, seule « erreur » console = le 404 `favicon.ico` du serveur de test (préexistant, hors jeu) |
| Collision de numéro de build (règle du mémo) | max distant = **431 sur `main`** ; aucune branche au-dessus, 432 libre |
| Gardes CI simulées sur la base réelle | `ko-fi` = 1, `^const SELF_UPDATE = true;$` = 1, `^const DEV_BUILD = false;$` = 1, `TESTER_BUILD` = 2 commentaires / 0 code — toutes vertes ; le lot 19.8 n'a laissé AUCUN motif surveillé en texte libre |
| Lot Discord (431) | `DISCORD_URL` déclaré 1 fois (l. 10175), rendu gardé par `DISCORD_URL ? … : null`, présent dans les 3 paquets (décision documentée l. 10076-10080 : les politiques stores visent les paiements, pas les liens de communauté). Aucun garde CI ne matche « discord ». |
| `manifest.json` / `version.json` | JSON valides ; PRECACHE de `sw.js` cohérent avec le manifeste |
| Doublons de clés (`RES_TIER` 46 / `RES_SHORT` 48 / `CARRIER_BY_RES` 48) | **0 doublon** (pas de récidive du `gaz_fossiles` de 14.16) ; l'écart de 2 clés (`energie_kw`, `gaz_echappement`) est voulu |
| `TODO` / `FIXME` / `console.log` résiduels | **0** (les 6 `console.error` sont les gardes documentées + UMD React) |
| Branches distantes non mergées | 1 seul commit orphelin (doc, `claude/playstore-preparation-g0w8vb`) — rien de perdu |

---

## 2. CI (`.github/workflows/android.yml`) — 4 constats MOYENS

**M1 — `cancel-in-progress: true` peut détruire la release entre deux runs `main`** (l. 34-41).
Le groupe par ref protège `main` d'une annulation par une branche, mais **deux merges rapprochés sur `main`**
partagent le même groupe : le second run annule le premier, potentiellement entre `gh release delete` et
`gh release create` → release `apk-latest` absente et `version.json` en ligne pointant des assets 404 jusqu'à
la fin du second run. Piste : `cancel-in-progress: ${{ github.ref != 'refs/heads/main' }}`.

**M2 — Regex `GAME_BUILD`/`GAME_VERSION`/`GAME_NOTES` non ancrées en début de ligne** (l. 131-132, 149-150,
175-176, 217, 310, 369-376). `grep -oP "const GAME_BUILD = \K[0-9]+" | head -1` prend la PREMIÈRE occurrence,
commentaires compris — or le bloc de commentaire cumulatif au-dessus de `GAME_BUILD` grossit à chaque lot. Le
jour où un commentaire écrit littéralement `const GAME_BUILD = NNN` avant la vraie ligne, la CI publie un
mauvais numéro **en silence**. Sain aujourd'hui (1 seule occurrence de chaque motif), mais c'est la classe de
piège déjà payée deux fois (run 561, lot ICON-1). Correctif : ancrer `^const …` (le mémo le prescrit déjà pour
les recherches manuelles).

**M3 — Compteurs `grep -c` non ancrés** (l. 103-111, 182-207). `ko-fi` et `const SELF_UPDATE = true;` matchent
n'importe où : la protection actuelle repose sur la discipline rédactionnelle des commentaires du jeu, pas sur
la CI. Piste : compter des motifs ancrés (`^const SUPPORT_URL = 'https://ko-fi`, `^const SELF_UPDATE = true;$`)
en garde primaire.

**M4 — `GAME_NOTES` : troncature silencieuse sur guillemet droit** (l. 376-377). `[^\"]*` s'arrête au premier
`"` sans erreur — la règle « jamais de guillemet droit dans GAME_NOTES » n'est vérifiée par rien. Un garde
`grep -q 'const GAME_NOTES = "[^"]*";$'` fermerait le point.

Mineurs CI : commentaire périmé « build-tools;34.0.0 » (l. 246-247, le réel est 36.0.0) · pattern
`grep && { exit 1; }` du contrôle « non debuggable » qui laisserait `$?=1` s'il devenait la dernière ligne du
step (l. 234-235) · `game-store.html` absent de l'exclusion Pages de `_config.yml` (l. 33-35) et diagramme
`_config.yml` périmé (pipeline à 2 éditions, sans store/.aab) · `\\K` vs `\K` incohérents (équivalents en
bash, mais invitent un mauvais « alignement ») · la décision « Discord reste dans la variante magasin » n'est
figée par aucune assertion CI (contrairement à SUPPORT_URL, gardé entrée ET sortie).

Vérifié conforme : gardes non tautologiques et falsifiables (contre-mesures systématiques, assertions sur
l'asset réellement embarqué et sur l'extrait du bundle), Publish AVANT Sync version.json, exactement 2 étapes
gatées `refs/heads/main`, `[skip ci]` sur le commit de synchro, `jq --arg` (pas d'injection JSON),
`android.yml.patched` bien supprimé.

---

## 3. Coquille Android (`android/`) — 6 constats MOYENS

**A1 — Repli d'`openExternally` : une URL externe peut se charger DANS la WebView, pont exposé**
(`MainActivity.java:175-185`). Si `startActivity(ACTION_VIEW)` lève (appareil sans navigateur), le `catch`
retourne `false` → la WebView charge l'URL externe, qui accède à `window.ArchipelNative` (`saveText`,
`update`). Contredit le commentaire l. 111-112. Correctif d'une ligne : retourner `true` inconditionnellement
pour http/https. **C'est le point Android n°1**, d'autant que le lot 431 ajoute précisément un lien externe
cliquable (Discord).

**A2 — Récepteur d'installation non protégé sur API 26-32** (`MainActivity.java:403-408`).
`RECEIVER_NOT_EXPORTED` n'existe qu'à partir d'API 33 ; en dessous, le broadcast est joignable par toute app,
et la branche `STATUS_PENDING_USER_ACTION` fait `startActivity()` sur un `EXTRA_INTENT` fourni par le
broadcast (intent redirection). Mitigation : filtrer sur un `EXTRA_SESSION_ID` connu.

**A3 — `update(url)` sans allowlist d'hôte ni contrôle d'intégrité** (`MainActivity.java:210-243, 297-349`).
Mitigations réelles en place (garde `SELF_UPDATE`, cleartext bloqué, PackageInstaller refuse une autre
signature, confirmation utilisateur), mais un appelant du pont peut faire télécharger/proposer un APK d'un
AUTRE package. À évaluer conjointement avec A1.

**A4 — `saveText` : path traversal sur la branche API < 29** (`MainActivity.java:266-269`). `filename` non
assaini (`../`). Portée limitée (stockage externe de l'app), la branche MediaStore est saine. Assainir le nom.

**A5 — Les APK publiés (publique + dev) sont des `assembleDebug`** → `debuggable=true` distribué (données
extractibles via `run-as`, WebView inspectable). Choix documenté au README ; le paquet magasin est bien un
`release` (assertion CI). Signalé comme surface acceptée, à re-trancher avant une distribution large.

**A6 — `android/README.md` entièrement périmé** (l. 21, 41-43, 60-62) : annonce API 34 / AGP 8.5.2 / 1
permission / 1 paquet, le réel est API 36 / AGP 8.13 / 2 permissions publiques + 0 store / 3 paquets. Toutes
les instructions locales sont fausses aujourd'hui.

Mineurs Android : flux non fermés sur exception (`saveText` MediaStore l. 259-262, `downloadAndInstall`
l. 323-342) · sessions PackageInstaller jamais `abandonSession()` + `update.apk` jamais purgé du cache
(l. 351-376) · I/O disque de `saveText` sur le thread UI (l. 200-207) · pas de `web.destroy()`/`onPause()`
(sans conséquence, l'activité EST le process) · `available()` du pont = code mort (le jeu teste la truthiness
de `window.ArchipelNative`) · `fullBackupContent` sans `dataExtractionRules` (ignoré sur Android 12+) ·
`configChanges` sans `uiMode` (un basculement clair/sombre recrée l'activité → rechargement, perte bornée au
débounce de 500 ms + flush `pagehide`) · `jsUpdate` concatène `state` sans échappement (sûr aujourd'hui,
valeurs internes constantes).

Vérifié conforme : surface `addJavascriptInterface` minimale (3 méthodes annotées, minSdk 26 → pas de
réflexion héritée), réglages `file://` par défaut sûrs, insets et barre d'état conformes au design P4 mot pour
mot, `storeBuild` lit bien la VALEUR, `buildToolsVersion` épinglé 36.0.0, manifests publics/store conformes
(2 permissions / 0), `appCategory="game"` en place, `.gitignore` android correct (keystore + asset généré).

---

## 4. Mono-fichier du jeu — code mort et 1 incohérence

**J1 — `makeIcon` : le commentaire du build 428 est INEXACT — le générateur S'EXÉCUTE à chaque boot.**
Déclaré l. 1700, appelé l. 1715 (`makeIcon(192)`, `makeIcon(512)`) **inconditionnellement** ; seul
l'`appendChild` du `<link>` blob est gardé (garde toujours fausse, le fichier porte un
`<link rel="manifest">` en ligne 7). Les deux canvas 192/512 sont donc dessinés à chaque chargement et le
résultat est jeté. Coût faible mais réel, et le commentaire cumulatif l. 9939-9942 (« ne s'exécutent
jamais ») est faux sur ce point. **Le lot de retrait annoncé au 428 reste à faire** — et devra rectifier le
commentaire.

**J2 — Code mort confirmé (0 appelant, vérifié par comptage)** :
- `portPipePools` (l. 18627) — mort depuis 10.82 ;
- `loadCargo` (l. 18793) — mort depuis 10.48 ;
- `islandTransitDir` (l. 19660) — mort depuis 11.29, **absent de la liste du mémo** (ajout de cet audit) ;
- `drawSpriteTinted` (l. 4992) — 0 appel depuis 14.50 (conservée « primitive générique » ; `conduitUnlTint`,
  elle, est bien vivante) ;
- branche `manual` du tech tree (l. 18474, 23053) — 0 nœud utilisateur (conservée sciemment, 14.91) ;
- `placeC` (SFX, l. 10373) et `ui_deplacer` (sprite, littéral l. 2083) — réserves assumées.

Un **lot de nettoyage unique** couvrirait J1+J2 (make­Icon + 4 fonctions mortes ; la branche `manual`,
`placeC` et `ui_deplacer` peuvent rester si l'arbitrage « réserve » tient toujours).

Vérifié sain : purge du booster complète (0 résidu de `highestUnlockedIsland`/`toggleBooster`/etc.),
`shotMode` conforme au design 19.4 (16 occurrences, `useState` pur, gardes `DEV_BUILD` en place),
`cumulativeUpgradeCost` vivante (3 appels), whitelist de versions de save couvre 3→31.

---

## 5. Hygiène du dépôt

- **`node_modules/` est commité** (171 fichiers, ~18 Mo, `playwright-core`) et il n'y a **aucun `.gitignore`
  racine**. C'est vraisemblablement un choix d'outillage (conteneur éphémère → le banc Playwright survit aux
  sessions), mais il n'est documenté nulle part et gonfle le pack git (~19,5 Mo au total). À trancher : soit le
  documenter dans le mémo comme volontaire, soit le retirer et l'installer en début de session.
- **`CLAUDE.md` pèse ~998 Ko.** Le mémo cumulatif approche le mégaoctet ; il est chargé intégralement à chaque
  session. Un archivage des blocs anciens (< build ~400) vers `reports/` réduirait le coût sans perdre
  l'historique (les commentaires cumulatifs du fichier de jeu portent déjà le même journal).
- Whitelist de versions de save en tableau littéral `[3, 4, …, 31]` (l. 26702) : fonctionnelle mais à étendre à
  la main à chaque bump — une borne `data.version >= 3 && data.version <= SAVE_VERSION` serait équivalente et
  s'entretiendrait seule (MINEUR).
- Les artefacts de banc `blk*.js` ne sont plus suivis (leçon 391 appliquée) ; `reports/` et `sprites/` sont
  bien rangés.

---

## 6. Recommandations priorisées

1. **Android A1** — replier `openExternally` sur `return true` pour http/https (1 ligne ; ferme l'exposition
   du pont natif à une page externe).
2. **CI M1** — `cancel-in-progress` conditionnel hors `main` (protège la release `apk-latest`).
3. **CI M2/M3/M4** — ancrer les regex d'extraction et de garde (`^const …`), garde de fermeture sur
   `GAME_NOTES` (rend la CI robuste au piège des commentaires, payé deux fois).
4. **Lot de nettoyage jeu** — retirer `makeIcon` + les 4 fonctions mortes, rectifier le commentaire l. 9939.
5. **Android A2/A4** — `EXTRA_SESSION_ID` sur le receiver pré-33, assainir `filename` de `saveText`.
6. **Docs** — réécrire `android/README.md` et le diagramme de `_config.yml` (tous deux périmés).
7. **Dépôt** — trancher le sort de `node_modules/` commité + ajouter un `.gitignore` racine ; alléger le mémo.

Aucun de ces points ne bloque une livraison ; aucun ne touche `SAVE_VERSION`.
