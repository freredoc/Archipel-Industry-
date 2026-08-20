# RAPPORT — Lot DEBITS (build 435 / Alpha 20.2)

Brief `BRIEFlotdebits.md`, patcher `patch_fmtrate.py` **pré-compilé**, test `test_fmtrate.js`
fourni. Base : `main` @ `3fcd2fb` (build 434 / Alpha 20.1). Branche : `claude/code-audit-qbbdio`.

---

## 1. Version produite

| | |
|---|---|
| `GAME_BUILD` | **434 → 435** |
| `GAME_VERSION` | **Alpha 20.1 → Alpha 20.2** |
| `SAVE_VERSION` | **31, inchangé** |

Numéro relevé sur **toutes** les branches distantes avant le bump : max = **434** → **435 libre**.
`GAME_NOTES` est ici franchement joueur, le changement étant visible à l'écran.

## 2. Ancre

**Une seule ancre**, extraite du fichier, `count == 1` vérifié **avant écriture** : le commentaire
du 14.6x **et** le corps de `fmtRate`. `function fmtRate(` reste à **1** après application, et la
fonction n'a toujours qu'**un seul appelant** (`fmtRateSci`, qui sert 26 sites) — c'est ce qui fait
qu'un correctif d'une ligne porte partout.

Patcher **idempotent** (2ᵉ passage : « DEJA APPLIQUE — aucune modification »).

L'ancre inclut le commentaire **à dessein** : le remplacer par le seul code aurait laissé en place
une description promettant un séparateur que la moitié du code ne posait pas. Le commentaire
d'origine est **conservé** et rectifié en dessous, pas réécrit.

## 3. Empreintes — ré-extraites du fichier patché

| | Avant patch | Après patch, **avant bump** | Après bump |
|---|---|---|---|
| Fichier | `bfea9498…39fbc13b` · 3 781 458 o | **`5e5af202778d1097136f3aba7d2adeb6357ae590261f3f727c45f60efb24df5b`** · 3 782 259 o | `87c595a9…a0000209` · 3 784 461 o |
| **Bloc 7** | — | **`34d349b4f04f5cb7ea0521bba2c56ccb2b1104accd7582420bd14db8741c8108`** | `6673d61d…caaabf682` |

**Les deux empreintes annoncées par le brief — fichier ET bloc 7 — sont retrouvées au caractère
près** : le patch appliqué ici est byte-identique à celui du rédacteur.

## 4. Delta d'octets

**+801 o EXACT** (valeur du brief au byte près) pour le patch seul ; +2 202 o de plus pour le bump
et le bloc de commentaire cumulatif. **Aucune ligne de CSS ni de Java n'est touchée.**

## 5. Suite de validation — montages réellement exécutés

| # | Résultat | Montage |
|---|---|---|
| **T1** | **PASS — 19 lignes, 0 KO** | `node test_fmtrate.js`, sortie `TOUS LES CAS PASSENT`. ⚠ Le brief annonce « 18/18 » ; le compte réel des lignes assertées est **19** (12 `chk` + 7 non-régressions) — écart de comptage du brief, aucune conséquence. Le test **extrait `fmtSep` et `fmtRate` du fichier patché** (`indexOf` + `new Function`) au lieu d'en recopier le corps : il mesure bien le code livré. |
| **T2** | **PASS — le défaut est reproduit** | Colonne « avant » du test, sur l'ancienne implémentation réimplémentée : `12345 → 12 345` **mais** `12345.6 → 12345,6`, `8504.32 → 8504,32`, `9830.4 → 9830,4`, `-5114.88 → -5114,88`. Après patch : `12 345,6`, `8 504,32`, `9 830,4`, `-5 114,88`. **C'est ce test qui établit qu'il y avait bien un défaut** — T1 seul prouverait que la nouvelle fonction fait ce qu'elle fait. |
| **T3** | **PASS** | 7 valeurs sous 1000 (`0`, `1`, `0.5`, `-0.25`, `999`, `999.99`, `12.34`) **strictement identiques** à l'ancienne implémentation. Le patch ne déborde pas de son périmètre. |
| **T4** | **PASS** | Négatifs : `-5114.88 → -5 114,88`, `-30300 → -30 300`. Signe conservé, groupement posé. |
| **T5** | **PASS** | `node --check` **7/7 sur les 3 variantes CI** (`game-public` / `game-dev` / `game-store`, reproduites par les `sed` exacts du workflow ; compte de blocs vérifié **avant** de boucler). Accolades `<style>` : **961 / 961**, inchangé. Gardes CI rejouées **après** rédaction des commentaires : `ko-fi` **1** publique / **0** magasin, `const SELF_UPDATE = true;` **0** en magasin, `^const DEV_BUILD = …$` 1/1, `SUPPORT_URL` ancré 1, garde M4 passante, extractions ancrées → `GAME_BUILD` 435 / `Alpha 20.2`. |
| **T6** | **PASS** | Banc headless Chromium, **360 px / dpr 3 / locale fr**, panneau Production ouvert, 5 lignes forgées en réassignant `islandFlowAgg` sur `window`. **Zéro `pageerror`.** |

### T6 — valeurs mesurées, patch contre base

Même montage, même viewport, exécuté sur les **deux** fichiers :

| Ligne | **Base 434** | **Patch 435** |
|---|---|---|
| `si.raffiné` | `9830,4` · `4659,2` · `+5171,2` | **`9 830,4`** · **`4 659,2`** · **`+5 171,2`** |
| `mot.quantique` | `8504,32` · `13619,2` · `-5114,88` | **`8 504,32`** · **`13 619,2`** · **`-5 114,88`** |
| `béton irr.` | `1 024` · `0` · `+1 024` | `1 024` · `0` · `+1 024` (entier : déjà séparé, **inchangé**) |

**C'est la démonstration du défaut dans le vrai panneau** : sur la base, `1 024` (entier, séparé)
et `8504,32` (non entier, non séparé) cohabitent **dans la même colonne**.

**Géométrie du lot TABLE-PROD (19.9) : inchangée.** Le séparateur allonge les chaînes d'un
caractère, il fallait le vérifier et non le supposer :

| Mesure | Base 434 | Patch 435 |
|---|---|---|
| `grid-template-areas` | `"res res res" "prd cns net"` | **identique** |
| `grid-template-columns` | `96.7969px` ×3 | **identique** |
| Hauteur de ligne | 38 px | **identique** |
| Troncature (`scrollWidth > clientWidth`) | aucune | **aucune** — les 15 cellules chiffrées mesurent `scrollW == clientW == 97` |

## 6. Écarts au brief, et raisons

1. **T1 : 19 lignes assertées, pas 18.** Écart de comptage du brief (12 appels `chk` + 7
   non-régressions). Aucune conséquence : 0 KO, `TOUS LES CAS PASSENT`.
2. **Contre-épreuve T6 ajoutée sur la base non patchée**, non demandée explicitement. T6 tel que
   spécifié ne mesure que le patch ; la même mesure sur la base est ce qui rend le résultat
   falsifiable, et c'est elle qui montre `1 024` et `8504,32` côte à côte dans la même colonne.
3. **Aucune modification du patch** : SHA du fichier **et** du bloc 7 conformes, delta au byte près.

## 7. Points ouverts

- ⚠ **La locale `'fr-FR'` est EN DUR** dans `fmtRate` comme dans `fmtSep`, alors que le jeu a
  quatre langues : un joueur allemand lit un groupement français. Incohérence **préexistante** et
  bien plus large que ce lot — elle toucherait tous les nombres du jeu et demande de trancher si la
  locale suit `I18N` ou reste figée. **Rien n'a été changé ici**, conformément au §6 du brief.
- `fmtInt`, `fmtSiNum`, `fmtSig`, `NUM_THRESHOLD`, le réglage joueur `NUM_FORMAT` : intacts.
- Restant dû de l'audit 431 : **T6/T7 sur appareil** des lots 20.0 et 20.1 (repli `openExternally`,
  mise à jour in-app, broadcast forgé), **A3** (`update(url)` sans liste d'hôtes — décision
  produit), **A5** (APK en `assembleDebug`), **A6** (README `android/` périmé), **J1/J2** (code mort
  dont `makeIcon`), `_config.yml` périmé, `node_modules` commité.
- ⚠ **Play Store** : lot web, aucune coquille Android touchée — mais le `.aab` produit par la CI
  embarque le jeu, donc il **change** et ne doit **pas** être soumis pendant la fenêtre de 14 jours
  du test fermé.
