# RAPPORT — Lot MENAGE (build 436 / Alpha 20.3)

Brief `BRIEFlotmenage.md`, deux patchers **pré-compilés** et indépendants (`patch_menage_jeu.py`,
`patch_menage_depot.py`). Base annoncée : `main` @ `3fcd2fb` (build 434). **Base réelle
d'exécution : `main` @ `45842e6` (build 435 / Alpha 20.2)** — le lot DEBITS a été mergé
entre-temps ; conséquences au §6. Branche : `claude/code-audit-qbbdio`.

---

## 1. Version produite

| | |
|---|---|
| `GAME_BUILD` | **435 → 436** |
| `GAME_VERSION` | **Alpha 20.2 → Alpha 20.3** |
| `SAVE_VERSION` | **31, inchangé** |

Numéro relevé sur **toutes** les branches distantes avant le bump : max = **435** → **436 libre**.

## 2. Ancres et disparitions

**Les 6 ancres du volet JEU sont sorties à `count == 1` sur la base RÉELLE (435)**, vérifiées au
passage à blanc **avant** toute écriture — le lot DEBITS n'en a perturbé aucune :

| Ancre | `count` |
|---|---|
| `J1_boot` (fonction `makeIcon` + bloc d'injection blob) | 1 |
| `J1_comment` (le paragraphe menteur du lot ICON-1) | 1 |
| `J2_drawSpriteTinted` (+ `_tintCache`) | 1 |
| `J2_portPipePools` | 1 |
| `J2_loadCargo` | 1 |
| `J2_islandTransitDir` | 1 |

**T4 — les 7 motifs de disparition, tous à 0** : `function makeIcon(size)`,
`var icon192 = makeIcon(`, `function portPipePools(`, `function loadCargo(`,
`function islandTransitDir(`, `function drawSpriteTinted(`, `_tintCache`.

Les deux patchers sont **idempotents** (2ᵉ passage : « DEJA APPLIQUE », « deja a jour », delta 0).

## 3. Empreintes — ré-extraites du fichier patché

### Volet JEU

| | Base réelle (435) | Après patch, **avant bump** | Après bump |
|---|---|---|---|
| Fichier | `87c595a9…a0000209` · 3 784 461 o | `840d2174…95efd20cf` · 3 779 250 o | `8869c105…19a0e9f44e` · 3 782 275 o |
| **Bloc 1 (BOOT)** | — | **`a50c1c4e7f4a304c650c0cfa7e06c4ffdfb0d87510d83738114d4dd5a1641628`** | identique |
| **Bloc 5** | `1be53ce44e7be14fb81bd92e6a338cba274304f38c6077061fd3e33232cc2651` | **identique** | identique |
| **Bloc 6** | — | **`c8d52d2d9dc19b5dbded5e6960cc30d80c0e27ea1a3a2f8740456001190ea265`** | identique |
| Bloc 7 | — | `23341e4a…24ba5005d` | `21fed9aa…5adb81f0` |

**Blocs 1, 5 et 6 : conformes au brief au caractère près.** Le bloc 7 diverge — **et c'est
attendu, pas suspect** : il porte le correctif `fmtRate` du build 435 que la base du brief (434)
n'avait pas.

> **Ce n'est pas une supposition.** Le patcher a été **rejoué sur la base 434 du brief** dans un
> répertoire séparé : il y rend **exactement** le SHA de fichier annoncé
> (`6bb4c96e9730876c7a0259dac5610e93fcfbd01487097581ddb3256a33b3152f`), la taille annoncée
> (**3 776 247 o**) et **les quatre SHA de bloc, bloc 7 compris**
> (`8a6217d82df4f8bcaacc96374497f3c5fcbbf5654cf161bc5ea15d3e5379d50d`). Le patch appliqué ici est
> donc **byte-identique** à celui du rédacteur ; tout l'écart vient de la base.

### Volet DÉPÔT — les 3 SHA conformes au brief

| Fichier | SHA-256 obtenu | Conforme |
|---|---|---|
| `_config.yml` | `cb47edc19bc6ee977b202ea2d50f37321ae5b7343a3740d8c1e45360f828c993` | ✅ |
| `android/README.md` | `038614f5f0aab4498a79cb54e104764c5539d08c31d862524cda8e6649146d2c` | ✅ |
| `.gitignore` | `208443549a0c40feb5bd01e9ef7120bfe84944cfbaf208dd9c8ce54a860fdbfa` | ✅ |

## 4. Delta d'octets

**−5 211 o EXACT** pour le patch du volet jeu (valeur du brief au byte près), mesuré par le
patcher **et** confirmé par `wc -c` (3 784 461 → 3 779 250). Le bump et son bloc de commentaire
cumulatif reprennent +3 025 o. Le volet dépôt ne touche aucun fichier de jeu.

## 5. Suite de validation — montages réellement exécutés

| # | Résultat | Montage |
|---|---|---|
| **T1** | **PASS** | `node --check` **7/7 sur les 3 variantes CI** (`game-public` / `game-dev` / `game-store`, reproduites par les `sed` exacts du workflow ; compte de blocs vérifié **avant** de boucler, refus si ≠ 7). Accolades `<style>` : **961 / 961**, inchangé. |
| **T2** | **PASS** | **Boot RÉEL en HTTP** (`http://127.0.0.1:8931`, jamais `file://`), viewport 360 / dpr 3 / locale fr, `localStorage` purgé via `addInitScript`, attente 8 s. **Avant et après strictement identiques.** |
| **T3** | **PASS** | Contre-épreuve J1 — voir le tableau ci-dessous. |
| **T4** | **PASS** | Les 7 motifs de déclaration/appel à **0**. |
| **T5** | **PASS** | **Bloc 5 identique au SHA** entre la base 435 et le fichier patché (`1be53ce4…2cc2651`) → le patch n'a pas débordé. |
| **T6** | **PASS** | `yaml.safe_load('_config.yml')` → `exclude` = **7 entrées**, dont **`game-store.html`**. |
| **T7** | **PASS** | `git check-ignore -v` sur `index.html`, `Archipel_industry_alpha-7.html`, `version.json`, `manifest.json`, `privacy.html` (+ `sw.js` et `_config.yml`, ajoutés) → **aucun n'est ignoré**. |
| **T8** | **PASS** | `git status --short` : 3 modifications + 1 ajout, **aucun fichier suivi ne disparaît** ; `git ls-files node_modules` = **171 fichiers, toujours indexés**. |
| **T9** | voir §5 ter | Compilation Android des 3 paquets via `workflow_dispatch` sur la branche. |

### T3 — la contre-épreuve, avec ses deux colonnes

Instrumentation de `HTMLCanvasElement.prototype.toDataURL` et de `URL.createObjectURL` **avant
navigation** (`addInitScript`), comptage au boot :

| | `toDataURL` | tailles | blobs de manifeste |
|---|---|---|---|
| **Base 435** | **2** | `192x192`, `512x512` | **1** |
| **Patché** | **0** | — | **0** |

**C'est ce test qui établit qu'il y avait un travail inutile à chaque démarrage et qu'il a cessé.**
T2 seul passerait aussi si le patch n'avait rien retiré.

### T2 — les deux relevés, côte à côte

| Mesure | Avant (435) | Après | Après bump |
|---|---|---|---|
| `document.body.children` | 8 | 8 | 8 |
| Canvas | 1080×1464 | 1080×1464 | 1080×1464 |
| `.hud` · `.toolbar` · `.inv-prod-btn` · `.tip-ok` | présents | présents | présents |
| `link[rel=manifest]` | 1 → `./manifest.json` | 1 → `./manifest.json` | 1 → `./manifest.json` |
| `pageerror` | 0 | 0 | 0 |
| `console.error` | 1 (404 préexistant du serveur de test) | 1, **le même** | 1 |

Le boot a été rejoué **après le bump** : c'est le seul contrôle qui attrape une page blanche, que
`node --check` ne voit pas.

### 5 ter. T9 — run CI

`workflow_dispatch` sur `claude/code-audit-qbbdio`. Résultat consigné en §8.

**Contrôle ajouté, non demandé** : le `.gitignore` ne peut pas perturber la CI, car son étape de
synchronisation stage par **chemins explicites** (`git add version.json index.html sw.js`, l. 407)
— aucun `git add -A`, et aucun de ces trois fichiers n'est couvert par un motif (T7).

## 6. Écarts au brief, et raisons

1. ⚠ **Écart de base, mesuré et compensé.** Le brief vise `main` @ `3fcd2fb` (build 434) ; la base
   réelle était **435** (lot DEBITS mergé entre-temps). Ses SHA de **fichier** et de **bloc 7** ne
   pouvaient donc pas correspondre. Vérifié à la place, et c'est plus fort : **les blocs 1, 5 et 6
   concordent avec le brief sur la base réelle**, et **le patcher rejoué sur la base 434 rend
   EXACTEMENT le SHA de fichier et les 4 SHA de bloc du brief**.
2. **Contrôles T7 étendus** à `sw.js` et `_config.yml` (le brief en listait 5), et **vérification
   des lignes `git add` du workflow** — c'est le seul chemin par lequel un `.gitignore` pourrait
   casser la CI.
3. **Aucune modification des patchers** : les 3 SHA du volet dépôt et le delta du volet jeu sont
   conformes au byte près.

## 7. Points ouverts

- ⚠ **`node_modules/` reste SUIVI** (171 fichiers, ~18 Mo). Le `.gitignore` ne le désindexe pas et
  ne le peut pas : il n'agit que sur les fichiers non indexés. **Aucun `git rm --cached` n'a été
  lancé** — c'est une décision d'Ethan, conformément au brief.
- **Trois tests appareil restent dus, et ce lot ne les remplace pas** : T7 du lot 20.0 (repli
  `openExternally` sans navigateur système), T6 et T7 du lot 20.1 (mise à jour in-app complète,
  broadcast forgé). Les trois tiennent en une session.
- **A3** (`update(url)` sans liste d'hôtes autorisés) et **A5** (APK publiés en `assembleDebug`) :
  arbitrages produit, pas des correctifs mécaniques. Le README `android/` signale désormais A5
  explicitement comme non tranché.
- Restants de l'audit 431, non traités ici : `starting: 'Calibrage…'`, la whitelist de saves
  `[3..31]`, la **locale `'fr-FR'` en dur** (point ouvert du lot DEBITS), les 12 champs
  écrits-jamais-lus, les 4 commentaires mensongers restants.
- ⚠ **Play Store** : le volet jeu change le `.aab` — **à ne pas soumettre** pendant la fenêtre de
  14 jours du test fermé.

## 8. Run CI de validation

`workflow_dispatch` sur `claude/code-audit-qbbdio`, commit `b9c8de4` → **run 571, toutes les
étapes en succès**.

| Étape | Résultat |
|---|---|
| Prepare game files (public + dev + magasin) | success |
| Build **PUBLIC** APK · **DEV** APK · **STORE** bundle `.aab` + APK de contrôle | success |
| Assert store package (paquet, `targetSdk`, permissions, debuggable) | success |
| Assert appIds (2 éditions installables côte à côte) | success |
| Show signing certificate (clé stable) | success |
| Upload APK / store artifacts | success |
| Sync PWA (`index.html` + cache `sw.js`) | success — libre par conception |
| **Publish to `apk-latest` release** | **SKIPPED** |
| **Sync `version.json`** | **SKIPPED** |

**T9 PASS** : les 3 paquets sortent et les deux étapes à effet de bord sont sautées — le gate
`refs/heads/main` tient, rien n'est publié. Le nouveau `.gitignore` n'a perturbé aucune étape,
ce qui confirme en conditions réelles le contrôle du §5 ter.
