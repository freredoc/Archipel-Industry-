# RAPPORT — LOT ICON-1 : icônes PWA et logo de chargement

Brief : `BRIEFloticonessplash.md` · patcheur `patch_icones.py` · banc `bench_icons.js` · pack `archipelloticones.zip`

---

## 1. Version livrée

| | |
|---|---|
| `GAME_BUILD` | **428** |
| `GAME_VERSION` | **'Alpha 19.5'** |
| `SAVE_VERSION` | **31, INCHANGÉ** |
| `GAME_NOTES` | `Nouvelle icone du jeu, reprise de l'illustration de l'ile, et ecran de chargement assorti.` |

**427 est le maximum relevé sur les 64 branches distantes**, pas seulement sur `main` — 428 était
libre. Ligne ajoutée au bloc cumulatif au-dessus de `const GAME_BUILD` (23 lignes), aucune ligne
antérieure effacée.

Le bump est **fonctionnel** ici et pas seulement conventionnel : la CI réécrit
`var CACHE = 'archipel-<GAME_BUILD>'` dans `sw.js`. Sans lui, les joueurs déjà installés
conserveraient l'ancien cache — donc les anciennes icônes et l'ancien splash. Vérifié en simulant le
`sed` de la CI : `BUILD` extrait = **428**, cache réécrit en `archipel-428`.

---

## 2. ⚠ ÉCART DE BASE — le brief vise 426, la base réelle est 427

Le brief prévenait d'une session concurrente et demandait un `git pull` frais. C'était justifié :
entre sa rédaction et son exécution, **deux lots ont été fusionnés** (PWA-1 → 426, puis SHOT-1 → 427).

| | brief | réel |
|---|---|---|
| base | 426 · `6eeaad9072172b80…` | **427 · `12a58875dd3c8659…` · 3 729 003 o** |
| patché (avant bump) | `e4610759e8ccbe2e…` · 3 729 604 o | **`d2376795b8697eec…` · 3 732 945 o** |
| delta du patcheur | +3 942 o | **+3 942 o — identique** |

**Les deux SHA-256 du brief ne peuvent donc pas correspondre, et c'est attendu.** Ce qui a été
vérifié à la place : **les 2 ancres HTML sortent à `count == 1` sur la base réelle** (passage à blanc
avant d'écrire) et **le delta est exactement celui annoncé**.

### Les 4 PNG étaient déjà sur `main`

Ethan les a poussés lui-même (commit `0238ec2`, *Add files via upload*) pendant la préparation de ce
lot. Comparés au pack **octet pour octet** : **les 4 sont identiques**.

```
icon-180.png          c8c922af0ee49956…   2 812 o   IDENTIQUE
icon-192.png          65cbb16d19316c29…   2 892 o   IDENTIQUE
icon-512.png          82f9a414cd383a15…   4 903 o   IDENTIQUE
icon-512-maskable.png 299b0ef208043bc5…   3 399 o   IDENTIQUE
```

**L'étape 3 du brief (« copier les 4 PNG ») était donc déjà faite** — je n'ai rien écrasé. Restaient
`manifest.json`, `sw.js`, le HTML et le bump.

### Empreintes livrées

```
base    3 729 003 o   sha256 12a58875dd3c86596d32f364c64a608892a0119c22223316788f3ad7775fa754
patché  3 732 945 o   sha256 d2376795b8697eeca5159811d1ec2fd38a0ae3767cb0d8264906c09319091644   (patcheur seul, +3 942 o)
livré   3 735 145 o   sha256 38115757bafc82b00ae598c96a2edff83f07b859b2d70021bbe930d6ecf8f7c5   (+ bump + commentaire)
```

**Aller-retour** : patcheur rejoué sur la base propre → `cmp` **identique octet pour octet**.
**Idempotence** : rejeu sur le fichier patché → `DEJA PATCHE — aucune modification`.

---

## 3. Ancres appliquées

| Fichier | Ancre | count |
|---|---|---|
| HTML | A1 · CSS `#splash .sp-logo{font-size:56px…}` | **1** |
| HTML | A2 · `<div class="sp-logo">🏭</div>` | **1** |
| `sw.js` | `'./icon-512.png'\n];` → ajout de `'./icon-512-maskable.png'` | **1** |
| `manifest.json` | remplacé (ajout de l'entrée `maskable`) | — |

Après application : `sp-logo-img` **1** ; **5 emoji 🏭 restants** (les 5 autres usages — onglet
bâtiments, icônes de nœuds, en-têtes — sont conservés, exactement comme l'annonce le brief).

⚠ Mon propre bloc de commentaire cumulatif contenait initialement un 🏭, ce qui portait le compte à
**6** et rendait ce contrôle mensonger. Le mot a été substitué à l'emoji ; le compte est retombé à 5.

### Écart mineur, assumé : le saut de ligne final du manifeste

Le `manifest.json` du dépôt se termine par `\n`, celui du pack par `}`. J'ai **conservé la convention
du dépôt** (+1 octet, 698 o au lieu de 697) — cela évite un `\ No newline at end of file` parasite
dans le diff. Le contenu JSON est celui du pack, à l'octet près par ailleurs ; seule l'entrée
`maskable` s'ajoute aux deux existantes (diff vérifié).

---

## 4. Les PNG — les chiffres calculés du brief sont VÉRIFIÉS

Mesurés dans Chromium (PIL n'est pas disponible dans cet environnement, contrairement à celui de
rédaction) en décodant chaque PNG sur un canvas et en lisant les pixels.

⚠ **Deux mesures fausses avant la bonne, et la raison mérite d'être connue** : classer « la mer » par
la couleur du pixel (0,0) donne un rayon de contenu de 256 px, et prendre la palette de l'anneau
extérieur en donne autant — parce que **la mer porte une texture de vagues** (les aplats bleus plus
clairs visibles sur l'illustration) que ces deux critères comptent comme du contenu. Le critère juste
est **« bleu franchement dominant » = mer** (`B > R+18 && B > G+18`), l'île étant verte / grise /
rouge / brune / blanche.

| | brief | mesuré |
|---|---|---|
| rayon de l'île, `icon-512.png` | 200 px | **200,9 px** |
| rayon de l'île, `icon-512-maskable.png` | 149 px | **149,2 px** |
| zone sûre (disque de 66 % du côté) | 169 px | **149,2 ≤ 169 → DANS la zone sûre** |

Le facteur de réduction est donc bien **calculé, pas choisi** — l'affirmation du brief tient.

Chargement et décodage réels, servis en HTTP :

| Fichier | HTTP | type | décodée | taille |
|---|---|---|---|---|
| `icon-180.png` | 200 | `image/png` | oui | 180×180 |
| `icon-192.png` | 200 | `image/png` | oui | 192×192 |
| `icon-512.png` | 200 | `image/png` | oui | 512×512 |
| `icon-512-maskable.png` | 200 | `image/png` | oui | 512×512 |

Les **trois `src` déclarées par le manifeste répondent 200**. Le logo inline du splash décode en
**128×128**, 43 teintes, pour 3 276 caractères de base64.

### `oxipng` : non appliqué

Vérifié dans cet environnement : **`oxipng` est absent**, comme dans celui de rédaction. Les poids
restent ceux du pack (2 812 / 2 892 / 4 903 / 3 399 o). Les fichiers sont déjà en mode palette, le
gain attendu est faible — mais le passage `oxipng -o 6` reste souhaitable avant publication si
l'outil devient disponible.

---

## 5. `node --check` — 7/7

7 blocs `<script>` (comptés via `(?m)^<script`), **7 OK**, rejoué **après le bump**.
Sur les **trois variantes dérivées par la CI** : `game-public` **7/7** · `game-dev` **7/7** ·
`game-store` **7/7**. `sw.js` compile ; `manifest.json` est un JSON valide.

Invariants CI, rejoués en simulant les `sed`/`grep` d'`android.yml` :

| Contrôle | Attendu | Mesuré |
|---|---|---|
| `^const SELF_UPDATE = true;$` | 1 | 1 |
| `^const SUPPORT_URL = 'https://ko-fi.com/freredoc';$` | 1 | 1 |
| `^const DEV_BUILD = false;$` | 1 | 1 |
| `ko-fi` publique / magasin | 1 / 0 | 1 / 0 |
| `SELF_UPDATE = true` dans `game-store` | 0 | 0 |
| réécriture du cache `sw.js` | `archipel-428` | `archipel-428` |

---

## 6. Banc — `bench_icons.js`

Pilote **porté sur playwright-core** (`puppeteer-core` et `@sparticuz/chromium` absents de l'image,
`playwright install` proscrit) : **pilote seul**, les 7 montages et leurs assertions sont repris à
l'identique, y compris le sondage à 30 ms qui capture l'état du splash **avant sa disparition**.

| Test | Mesure | Verdict |
|---|---|---|
| I1 logo = `IMG`, plus un emoji | `{"tag":"IMG","w":128,"h":128,"complete":true}` | **PASS** |
| I2 source inline `data:image/` | `src commence par: data:image/` | **PASS** |
| I3 image **réellement décodée** | `complete=true 128x128` | **PASS** |
| I4 aucune requête en échec | `…/main/version.json` — **voir ci-dessous** | **ÉCHEC, préexistant** |
| I5 aucune `pageerror` | `0` | **PASS** |
| I6 non-régression PWA-1 | `PWA_ELIGIBLE=true` | **PASS** |
| I7 manifeste déclare une maskable | `{"n":3,"purposes":"any,any,maskable"}` | **PASS** |

**6/7 PASS.**

### ⚠ I4 : l'échec est du bruit réseau du bac à sable, prouvé et non supposé

La seule requête en échec est le `fetch` du détecteur de mise à jour vers
`raw.githubusercontent.com/.../version.json`, que l'environnement de test n'atteint pas. C'est un
**bruit préexistant déjà documenté** dans le mémo du projet.

**Contre-épreuve exécutée** : le même banc sur la **base non patchée** échoue sur **exactement la
même requête**. I4 ne mesure donc rien de ce lot.

Ce que I4 devait réellement couvrir ici — que les PNG se chargent — est vérifié séparément et
positivement au §4 : **4 PNG sur 4 en HTTP 200, décodés aux bonnes dimensions**, et les trois `src`
du manifeste en 200.

### Contre-test — 3/7 sur la base non patchée

```
I1 ECHEC  {"tag":"DIV","inline":""}          <- l'emoji, pas une image
I2 ECHEC  src commence par: (vide)
I3 ECHEC  complete=undefined undefinedxundefined
I4 ECHEC  version.json                        <- identique des deux cotes
I5/I6/I7 PASS
```

**Les trois assertions du lot (I1, I2, I3) basculent** : la suite mesure bien quelque chose. I6 et I7
passent des deux côtés, et **c'est voulu** — ce sont des gardes de non-régression (PWA-1 est déjà
fusionné ; le manifeste est livré à part, donc déjà en place quand le banc sert la base). Le brief
annonçait 4/7 ; on observe 3/7, l'unité d'écart étant I4, qui passait dans un environnement avec
accès réseau.

---

## 7. Écarts au brief, et leurs motifs

1. **Base 427 au lieu de 426** (§2) — deux lots fusionnés entre-temps. Ancres re-vérifiées à
   `count == 1`, delta identique à l'annonce ; seuls les SHA-256 diffèrent.
2. **Étape 3 déjà faite par Ethan** — les 4 PNG étaient sur `main`, byte-identiques au pack. Rien
   écrasé, comparaison consignée.
3. **Pilote de banc porté sur playwright-core** — mêmes motifs qu'aux lots PWA-1 et SHOT-1.
4. **Saut de ligne final du manifeste conservé** (convention du dépôt, +1 octet).
5. **Mesure des PNG faite dans Chromium** et non avec PIL, absent de cet environnement. Les valeurs
   du brief sont confirmées.
6. **I4 laissé en échec plutôt que filtré**, avec contre-épreuve — filtrer en silence aurait masqué
   un vrai 404 le jour où il s'en produirait un.

---

## 8. Points restants

- **Aucun rendu sur appareil.** Le recadrage réel de la maskable par One UI sur le S25 FE, et le
  rendu du splash, restent à valider. La mesure du §4 dit que l'île tient dans la zone sûre ; elle ne
  dit pas à quoi ressemble le squircle de Samsung.
- **`oxipng -o 6` non appliqué** — outil absent (§4).
- **Le rendu iOS n'est pas testé** : Safari ignore les entrées `maskable` et utilise
  `apple-touch-icon` → `icon-180.png` (`<link>` présent ligne 8, fichier livré), non vérifié sur
  appareil Apple.
- **`makeIcon` reste en place, code mort.** `index.html` porte déjà `<link rel="manifest">` (l. 7),
  or l'injection du manifeste blob est gardée par
  `if (!document.querySelector('link[rel="manifest"]'))` — garde **toujours fausse**, y compris dans
  l'APK. Ni `makeIcon` ni le manifeste blob ne s'exécutent jamais. **À retirer dans un lot dédié** :
  sans quoi le prochain qui régénère des icônes repartira de ce générateur. Un commentaire
  d'avertissement est posé dans le bloc cumulatif pour que le constat ne se reperde pas.
- **T4 / T5 / T6 du lot P4**, **T4 du lot P2** et la validation du bandeau PWA **en 3 boutons**
  restent à exécuter sur appareil — antérieurs, non rouverts par ce lot.

---

## 9. Livraison

- Branche `claude/playstore-preparation-g0w8vb`, **repartie de `origin/main`** (les PR #399 et #400
  étant fusionnées, l'historique déjà mergé n'a pas été empilé).
- **Nouvelle PR** — #399 et #400 sont closes, elles ne peuvent pas porter ce lot.
- **PR ouverte, NON fusionnée** : le merge appartient à Ethan, c'est lui qui déclenche `android.yml`
  et donc la republication de l'APK, d'`index.html` et de `version.json`.
