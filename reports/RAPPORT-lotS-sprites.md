# RAPPORT — Lot S : extraction des sprites hors du monolithe (modèle A)

**Livré : `GAME_BUILD = 437` · `GAME_VERSION = 'Alpha 20.4'` · `SAVE_VERSION = 31` (INCHANGÉ).**
Base : build 436 / Alpha 20.3, `main` et la branche au même commit `d629aeb`.
Aucune mécanique de jeu, aucun champ de sauvegarde, **aucune ligne de CI** touchée.

---

## 1. Ce qui a été fait

Les **1 292 lignes d'assignation pure de data-URL** (1,29 Mo) sortent du monolithe vers
`src/sprites-inline.js`. Le monolithe **reste commité à la racine** (modèle A) et devient un
fichier **généré** par `node tools/build.js`, qui concatène :

```
src/index.src.html   (résiduel, marqueur @@SPRITES_INLINE@@ seul sur sa ligne, bloc <script> n°5)
        +
src/sprites-inline.js   (1 292 lignes de données + les 102 lignes de commentaire qui les documentent)
        =
Archipel_industry_alpha-7.html   (+ bannière « FICHIER GENERE … NE PAS EDITER »)
```

Arborescence livrée, conforme au §3 du brief :

| fichier | rôle | taille |
|---|---|---|
| `Archipel_industry_alpha-7.html` | **GÉNÉRÉ**, commité, ne pas éditer | 3 786 131 o · 33 555 l. |
| `.build-stamp` | sha256 du fichier généré (référence du garde-fou) | 65 o |
| `src/index.src.html` | résiduel + marqueur | 2 429 398 o · 32 154 l. |
| `src/sprites-inline.js` | les data-URL | 1 356 684 o · 1 401 l. |
| `tools/build.js` | reconstruction (Node, zéro dépendance) | 4 638 o |
| `tools/split-once.js` | splitter à usage unique, commité pour traçabilité | 7 255 o |

⚠ **`GAME_BUILD` / `GAME_VERSION` / `GAME_NOTES` se bumpent dans `src/index.src.html`**, jamais
dans le fichier généré. Le commentaire cumulatif du build 437 le dit sur place.

---

## 2. Baseline (mesurée AVANT le split, sur le monolithe 436)

Boot HTTP réel, `localStorage` purgé avant navigation. **Ce sont ces valeurs qui font seuil, pas
des valeurs devinées.**

| compteur | baseline (436) | après (437, généré) | verdict |
|---|---|---|---|
| `Object.keys(SPRITE_DATA).length` | **1 500** | **1 500** | = |
| `Object.keys(ANIM_DATA).length` | **240** | **240** | = |
| `Object.keys(ANIM_META).length` | **220** | **220** | = |
| `Object.keys(ANIM_BY_SK).length` | **220** | **220** | = et **> 0** |
| `SPRITES_ENABLED` | `true` | `true` | = |
| empreinte `SPRITE_DATA` (djb2 sur `clé:longueur` trié) | `7f006fe5/1500` | `7f006fe5/1500` | = |
| empreinte `ANIM_DATA` | `e8715008/240` | `e8715008/240` | = |
| canvas peint | 100 % | 100 % | = |
| erreurs console | 1 (`/favicon.ico` 404) | 1 (`/favicon.ico` 404) | = (bruit préexistant) |

Les deux **empreintes** vont plus loin que les quatre compteurs demandés : elles hachent la
longueur de la valeur finale de **chacune des 1 740 clés**. Deux objets de même taille mais dont
une seule clé aurait changé d'override donneraient deux empreintes différentes.

⚠ Le 404 est `/favicon.ico`, demandé par le navigateur, absent du serveur de test.
**Contre-épreuve exécutée : identique sur le monolithe 436 non modifié** → ce n'est pas une
régression de ce lot.

---

## 3. Lignes déplacées

Règle appliquée telle quelle (§4) : `^\s*window\.__(SPRITE|ANIM)_DATA__(\[|=\{)` **ET**
`data:image/`.

| mesure | attendu (brief) | mesuré | |
|---|---|---|---|
| lignes déplacées | 1 292 | **1 292** | ✔ exact |
| dont bloc 5 | 949 | **949** | ✔ |
| dont bloc 7 | 343 | **343** | ✔ |
| lignes > 300 caractères, résiduel | 1 573 → 424 | **1 573 → 424** | ✔ exact |

Contrôles de non-débordement de la règle, sur les 33 511 lignes :

- lignes qui matchent la regex **sans** `data:image/` : **0** ;
- lignes portant `data:image/` **et** citant `__SPRITE_DATA__`/`__ANIM_DATA__` hors regex : **0** ;
- lignes extraites avec une indentation (donc potentiellement imbriquées dans une fonction) :
  **0** — les 1 292 sont au niveau module ;
- clés distinctes portées par les 1 292 lignes : **1 740 = 1 500 + 240**, soit exactement le
  contenu runtime de `SPRITE_DATA` + `ANIM_DATA`. Aucune clé ne vient d'ailleurs, aucune ne se perd.

**Les 10 data-URL de la feuille de style et le `<img src="data:image/png…">` (11 Ko) ne bougent
pas** : hors périmètre, explicitement.

---

## 4. Commentaires : retenus, déplacés, renvois

**102 lignes de commentaire** accompagnent les données (règle §4 : un bloc contigu de lignes
blanches/commentaires précédant immédiatement une série de lignes déplacées voyage avec elles).

### Blocs RETENUS dans `src/index.src.html` (7)

| lignes (source 436) | bloc | raison |
|---|---|---|
| 3927-3929 | commentaire de `const ANIM_DATA` | documente une déclaration qui reste |
| 4085-4087 | ⚠ 14.95 — zone morte temporelle d'`ANIM_META` | n'a de sens qu'à côté du `Object.assign` |
| 4282 | `// --- spritesheets (frame 0 == sprite statique, verifie pixel a pixel) ---` | titre |
| 4303 | `// --- ANIM_META : les 3 bat_collisionneur_pN_boot passent de 64x96 a 96x64, + _actif ---` | titre |
| 4314 | `// --- spritesheets 4 frames (frame 0 == statique, verifie) ---` | titre |
| 4319 | `// --- ANIM_META : fw/fh NON carres (32x48 et 48x32) ---` | titre |
| 4328 | `// --- spritesheets (frame 0 == statique, verifie) ---` | titre |

### Lignes de renvoi ajoutées (5)

Texte inséré, à l'identique du brief :
`// (data-URL déplacées dans src/sprites-inline.js — cf. tools/build.js)`

| insérée après | pourquoi |
|---|---|
| `const ANIM_DATA = typeof window !== 'undefined' && …` (L3930) | les sheets décrites juste au-dessus sont parties |
| dernière ligne du ⚠ 14.95 (L4087) | le pack de sheets qui suivait est parti |
| `// --- spritesheets …` (L4282) | les spritesheets titrées ici sont parties |
| `// --- spritesheets 4 frames …` (L4314) | idem |
| `// --- spritesheets …` (L4328) | idem |

**Les deux titres `// --- ANIM_META : …` (L4303, L4319) NE reçoivent PAS de renvoi** : ce qu'ils
documentent, c'est l'`Object.assign(ANIM_META, …)` posé juste en dessous, et il n'a pas bougé.
Y mettre un renvoi aurait fabriqué le commentaire menteur que le §4 cherche à éviter, à l'envers.

### Ce qui ne bouge pas, vérifié

`const ANIM_META` (L4083) et ses **six** `Object.assign(ANIM_META, …)` (L4084, 4101, 4304, 4320,
4331, 4357) : **aucun ne porte de data-URL, aucun n'a bougé**. `const SPRITE_DATA`,
`const SPRITES_ENABLED`, `const ANIM_DATA`, `const ANIM_BY_SK` : intacts, à leur place.

---

## 5. Preuves d'extraction (au-delà des tests demandés)

Ces trois preuves ont été exécutées **avant le bump de version**, sur l'état où le seul écart
possible venait du découpage lui-même.

### P1 — ROUND-TRIP byte-identique

Le masque d'extraction est **re-dérivé indépendamment depuis l'original**, puis l'original est
**recomposé** en ré-entrelaçant `src/sprites-inline.js` (moins son en-tête) et
`src/index.src.html` (moins le marqueur et les 5 renvois) :

```
sha256 ORIGINAL   : 8869c1059925229b2d25bc4ac2e0de14ab246f1d579f4f8518607519a0e9f44e
sha256 RECOMPOSÉ  : 8869c1059925229b2d25bc4ac2e0de14ab246f1d579f4f8518607519a0e9f44e
>>> IDENTIQUE — aucune ligne perdue, dupliquée ni réordonnée
```
1 394 lignes extraites et 32 118 lignes résiduelles, **toutes consommées** (aucun reliquat des
deux côtés).

### P2 — Multi-ensemble exhaustif des lignes, original 436 → généré 437

Comptage signé de **chaque ligne distincte** du fichier :

```
3 lignes RETIRÉES  : const GAME_BUILD = 436; / const GAME_VERSION = 'Alpha 20.3'; / l'ancien GAME_NOTES
44 lignes AJOUTÉES : 1 bannière + 7 d'en-tête de sprites-inline.js + 5 renvois
                     + 31 de commentaire cumulatif + les 3 nouvelles constantes de version
```
**Pas une seule ligne de jeu n'a disparu.** Les seules disparitions sont les trois constantes
qu'on vient de remplacer.

### P3 — Ordre des données préservé (c'est lui qui décide du dernier override)

La sous-suite des 1 292 lignes de data-URL, extraite du fichier **généré**, est **identique en
ordre et en contenu** à celle de l'original (même sha256 : `9fb99655131e98e2…`).

Ça compte : **248 clés portent plusieurs assignations**, dont **63 à cheval bloc 5 / bloc 7**
(ex. `i7_bord_coin_ne` : L2609, L3063 puis L4027). Le dernier gagne au runtime — c'est
exactement ce que T4 vérifie côté navigateur.

### P4 — Les blocs non concernés sont intacts à l'octet

| bloc | 436 (avant) | `src/index.src.html` | 437 (généré) |
|---|---|---|---|
| 1 | 0,4 Ko | 0,4 Ko | 0,4 Ko |
| 2 | 3,2 Ko | 3,2 Ko | 3,2 Ko |
| 3 | 10,5 Ko | 10,5 Ko | 10,5 Ko |
| 4 | 128,7 Ko | 128,7 Ko | 128,7 Ko |
| 5 | **1 087,9 Ko** | **0,027 Ko** | **1 324,9 Ko** |
| 6 | 427,1 Ko | 427,1 Ko | 427,1 Ko |
| 7 | **1 766,2 Ko** | **1 532,8 Ko** | **1 532,8 Ko** |
| total | 3 424,0 Ko | 2 102,8 Ko | 3 427,7 Ko |

Les blocs 1, 2, 3, 4 et 6 sont **inchangés à l'octet près** (delta 0) : le patch n'a pas débordé.
Dans le fichier généré, le bloc 5 grossit de 237 Ko exactement autant que le bloc 7 maigrit — les
343 lignes du bloc 7 et leurs commentaires y remontent.

⚠ **Pourquoi c'est sans risque** : `const SPRITE_DATA` (bloc 7) est une **référence partagée**
vers `window.__SPRITE_DATA__`, pas une copie. Assigner plus tôt ne change rien ; c'est même
strictement plus sûr que l'état d'avant, où **1 143 assignations sur 1 292 s'exécutaient APRÈS**
la liaison.

### P5 — La bannière avant `<!DOCTYPE>` ne change pas le rendu

Un commentaire avant le doctype est légal en HTML5, mais « légal » n'est pas « sans effet » : en
mode quirks la mise en page CSS changerait en silence. Relevé DOM comparé, même navigateur, même
viewport (420×900, DPR 3), original 436 vs généré 437 :

| relevé | 436 | 437 |
|---|---|---|
| `document.compatMode` | `CSS1Compat` | `CSS1Compat` |
| doctype analysé | `html` | `html` |
| enfants de `<body>` | 8 | 8 |
| tampon canvas | 1260×1764 | 1260×1764 |
| canvas CSS · `.app` · `.hud` · `.toolbar-wrap` | 420×588 · 420×900 · 420×118 · 420×79 | identiques |
| `document.characterSet` | UTF-8 | UTF-8 |

**Un seul écart sur 11 relevés, et c'est celui qu'on a créé** : `document.childNodes[0]` est
désormais le nœud commentaire au lieu du doctype. **`CSS1Compat` des deux côtés : pas de quirks.**

### P6 — La CI passe (simulée localement, `android.yml` non modifié)

Toutes ses lectures sont ancrées sur des **motifs**, jamais sur des numéros de ligne : la
bannière en tête ne les gêne pas. Simulation des étapes réelles :

```
BUILD = 437   VERSION = Alpha 20.4   NOTES extraites correctement (aucun " dans la chaîne)
game-public : ^const DEV_BUILD = false;$ → 1     ko-fi → 1
game-dev    : ^const DEV_BUILD = true;$  → 1
game-store  : ^const SELF_UPDATE = false;$ → 1   ko-fi → 0   ^const DEV_BUILD = false;$ → 1
sw.js       : var CACHE = 'archipel-437';
node --check 7/7 sur game-public, game-dev ET game-store
```

---

## 6. Tests T1 → T9

Boot HTTP réel sur `http://127.0.0.1:<port>` (jamais `file://` — `PWA_ELIGIBLE` teste
`location.protocol`), `localStorage` purgé via `addInitScript` avant navigation, viewport 420×900
DPR 3.

| test | montage réellement exécuté | résultat |
|---|---|---|
| **T1** sprites préservés | boot du **fichier généré**, `Object.keys(SPRITE_DATA).length` + empreinte djb2 sur `clé:longueur` des 1 500 clés | **PASS** — 1 500, empreinte `7f006fe5/1500`, égales à la baseline |
| **T2** anims préservées | `Object.keys(ANIM_DATA).length` et `Object.keys(ANIM_META).length` + empreinte `ANIM_DATA` | **PASS** — 240 / 220, empreinte `e8715008/240`, égales à la baseline. Un `Object.assign(ANIM_META,…)` déplacé par erreur ferait chuter les 220 |
| **T3** liaison non cassée | `Object.keys(ANIM_BY_SK).length` comparé au **compte** de baseline, pas à la présence de la clé | **PASS** — **220**, `> 0`. C'est le filet contre la panne muette : si `window.__ANIM_DATA__` était absent à `const ANIM_DATA`, celle-ci créerait un objet neuf, `ANIM_BY_SK` sortirait **vide** et **aucune erreur JS** ne serait levée |
| **T4** override final identique | 5 clés à assignations **multiples ET à cheval bloc 5 / bloc 7** (`item_helium_liquide` L2093+L4327, `bat_refroidisseur` L2093+L4125, `bat_usine_moteur_nuc_v2` L2525+L4355, `i7_bord_coin_ne` L2609+L3063+L4027, `i7_bord_ext_sw` L2617+L3070+L4034) + `ANIM_DATA['refroidisseur']` (L2380+L4302) ; comparaison de `…[clé].length` avant/après | **PASS** — 242 / 450 / 514 / 254 / 442 / 602, **identiques**. Une inversion d'ordre serait invisible à T1 (mêmes comptes) et visible ici |
| **T5** le jeu démarre | boot complet, splash retiré, 2,5 s de jeu, capture d'écran | **PASS** — canvas peint **100 %**, horloge à 3 ticks, tutoriel « Tutorial 1/14 », astuce de bienvenue **avec son illustration entièrement dessinée** (mines, route, port + grue, rochers), HUD en sprites. 0 `pageerror`, 1 seul 404 (`/favicon.ico`, préexistant, contre-épreuvé) |
| **T6** garde-fou actif | build réussi → **1 octet modifié** dans le fichier généré (`… en jeu.` → `… en jeu!`) → `node tools/build.js` | **PASS** — code de sortie **1**, message explicite avec les deux sha256, **fichier INTACT** (sha inchangé, la modification est toujours là). Puis `--force` : le build passe et **restaure exactement** le sha d'origine |
| **T7** idempotence | 3 builds consécutifs sans toucher aux sources | **PASS** — `ee654fd7…` aux trois, `.build-stamp` identique |
| **T8** `node --check` | 7 blocs extraits **du fichier généré** par balayage séquentiel (jamais `awk length<300`), refus de conclure si ≠ 7 fichiers | **PASS** — **7/7**, et 7/7 aussi sur `game-public` / `game-dev` / `game-store` |
| **T9** comptage des blocs | `(?m)^<script` sur le fichier généré | **PASS** — **7**. (`<script[^>]*>` en donne 15 : la chaîne `"<script>"` du UMD React et des citations en commentaire — c'est le piège que le brief signale) |

`node --check` passe aussi sur `src/sprites-inline.js` seul : les blocs de commentaires déplacés
sont complets, aucun `/* … */` n'a été coupé en deux par l'extraction.

---

## 7. sha256 (ré-extraits des fichiers sur disque, jamais transcrits)

```
ee654fd7fb990230e889bef1461c6c5ed5a5128a8b34c207e0a6b05f5d5214f7  Archipel_industry_alpha-7.html
7110f121e6b8f1865fc63aaeb9061f8ddffa379b267f638cad5398eac1c44077  src/index.src.html
37048778ca039b8e7db885550050c8a9944b6706443b9446379fcd5b40e834b7  src/sprites-inline.js
8d5d56f8c4869f88f3f73898f4fd74660974cf9abb2a8e35a65abfa76c71f137  tools/build.js
0c6aeb675a706ec794b88570770e313b80220cfc7e597b8da93726d479eebf9f  tools/split-once.js

.build-stamp                                    → ee654fd7fb990230e889bef1461c6c5ed5a5128a8b34c207e0a6b05f5d5214f7
sha256 du monolithe 436 (base, pour mémoire)    → 8869c1059925229b2d25bc4ac2e0de14ab246f1d579f4f8518607519a0e9f44e
```

Delta d'octets du monolithe : **3 782 275 → 3 786 131 o (+3 856)**, soit la bannière, les 7 lignes
d'en-tête de `sprites-inline.js`, les 5 renvois et les 31 lignes de commentaire cumulatif du
build 437 — rien d'autre (cf. P2).

---

## 8. Écarts au brief, et pourquoi

1. **Harnais de test.** Le §6 demande `puppeteer-core` + `@sparticuz/chromium` : **absents de
   l'image**, et `playwright install` est proscrit (il exige une révision que l'image n'a pas).
   Porté sur **`playwright-core` + Chromium 1194** (`/opt/pw-browsers/chromium-1194/chrome-linux/chrome`).
   Toutes les assertions sont reprises à l'identique ; `addInitScript` remplace
   `evaluateOnNewDocument`, `newContext({viewport, deviceScaleFactor})` remplace `setViewport`.
   Le HTTP réel et la purge de `localStorage` avant navigation sont respectés.

2. **Les commentaires voyagent — donc le résiduel est plus court que le tableau du §2.**
   Le §3 (« les 1 292 lignes **+ leurs commentaires** ») et le §4 (règle de déplacement) disent que
   les blocs de commentaires suivent les données ; le tableau du §2, lui, annonce
   **32 220 lignes / 2,44 Mo** pour le résiduel et **2 Ko** pour le bloc 5, ce qui n'est arithmétiquement
   possible **que si les commentaires restent** (33 511 − 1 292 + 1). Les deux ne peuvent pas être vrais
   ensemble.
   **J'ai suivi la RÈGLE**, pas le tableau : laisser dans le bloc 5 une quarantaine de commentaires
   décrivant des packs de sprites au-dessus d'un endroit où il ne reste plus un seul sprite
   fabriquerait très exactement le **quatrième commentaire menteur** que le §4 cite en justification.
   Conséquence chiffrée : **102 lignes de commentaire partent**, le résiduel fait **32 154 lignes /
   2,32 Mo** (dont +31 lignes de commentaire cumulatif que j'ajoute), et le bloc 5 résiduel tombe à
   **27 octets** au lieu de 2 Ko.
   **Recoupement qui tranche** : la métrique « lignes de plus de 300 caractères : 1 573 → 424 » du
   brief est retrouvée **au chiffre près**, ce qui prouve que l'ensemble des lignes de **données**
   extraites est exactement celui que le rédacteur avait mesuré. L'écart ne porte que sur les
   commentaires.

3. **Tailles absolues par bloc.** Mes mesures diffèrent des siennes sur les valeurs absolues
   (bloc 7 : 1 766 Ko mesurés contre 1 738 Ko annoncés) alors que **les deltas concordent**
   (−233 Ko contre −231 Ko) : convention de mesure différente. Le brief prévient que les chiffres
   sont à re-vérifier ; le tableau du §5 ci-dessus est celui que j'ai mesuré.

4. **Marqueur compté ligne à ligne.** Le §5-2 demande « exactement une fois ». Implémenté comme
   « exactement **une ligne égale** au marqueur ». Motif concret : le commentaire de version du
   build 437 **cite** le marqueur, et le comptage sur la chaîne brute a fait **échouer le build** —
   c'est le garde-fou qui l'a attrapé, pas moi. Deux corrections plutôt qu'une : le comptage devient
   line-anchored (une citation ne peut ni faire échouer le build ni servir de point d'injection) **et**
   la citation a été raccourcie en `@@SPRITES_INLINE@@`.

5. **Premier build : `--force` obligatoire.** Le §5-3 décrit la comparaison au `.build-stamp` mais
   ne tranche pas le cas où le stamp **n'existe pas**. J'ai choisi de **refuser** (on ne peut pas
   vérifier qu'on n'écrase rien), avec un message qui nomme le remède. Le cas ne se présente qu'au
   bootstrap : `.build-stamp` est commité.

6. **`_config.yml` : `src/` et `tools/` ajoutés à l'`exclude` Pages.** Hors de la liste explicite
   du brief, mais `src/index.src.html` est une page de jeu **amputée de ses sprites** : la servir
   donnerait une seconde URL de jeu, cassée — exactement ce que ce fichier existe pour empêcher
   (il le dit lui-même en en-tête). `tools/` rejoint `android/` et `.github/` comme outillage.
   Aucun effet sur Git ni sur la CI.

7. **`GAME_NOTES` réécrit** pour ce build (texte joueur, sans guillemet droit, sans apostrophe —
   convention en place). La CI le relit correctement : vérifié par simulation du `grep -oP`.

---

## 9. Points ouverts

- **Rien ne garde le sens inverse.** `build.js` empêche d'écraser une correction faite dans le
  fichier généré ; **rien n'empêche de commiter `src/` sans avoir rebuildé**, auquel cas le
  monolithe commité serait périmé. Le correctif est d'une ligne — une étape CI
  `node tools/build.js && git diff --exit-code -- Archipel_industry_alpha-7.html` — mais **la CI
  est explicitement hors périmètre de ce lot** (« `android.yml` : aucune modification »). À
  arbitrer par Ethan ; en attendant, le rituel est : *éditer `src/`, lancer `node tools/build.js`,
  commiter les trois.*
- **`index.html` à la racine reste au build 436** jusqu'au prochain run CI sur `main` : c'est le
  fonctionnement habituel (il n'est jamais édité à la main), simplement rappelé ici parce qu'il
  recevra la bannière à ce moment-là.
- **Aucun test sur appareil.** La bannière avant `<!DOCTYPE` est prouvée inoffensive sur
  Chromium 1194 (`CSS1Compat`, mise en page identique au pixel) ; la WebView Android partage ce
  moteur mais n'a pas été testée.
- **Les 11 Ko de data-URL de la feuille de style** (`--tex-bleu`, `--btn-*`, `--cadre-*`,
  `--inox-panneau`, `--tex-inox`, `--onglet-inactif`, et le `<img src="data:image/png…">`) restent
  dans `src/index.src.html` : hors périmètre, comme demandé.
- **`ANIM_BY_SK` reste calculé dans le bloc 7**, après toutes les assignations : rien à faire, mais
  c'est la dépendance d'ordre qu'il ne faudra jamais casser (T3 la surveille).

## 10. Hors périmètre — non touché

`node_modules` (toujours suivi, 171 fichiers), A3 (liste d'hôtes autorisés sur `update(url)`),
A5 (APK en `assembleDebug`), les trois tests appareil dus (T7 SILENCIEUX, T6 PONT, T7 PONT), et
toute réorganisation du bloc 7 au-delà du retrait des lignes de data-URL.
