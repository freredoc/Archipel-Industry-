# RAPPORT — LOT L1 · Île 7 (id interne 8), socle

**Brief** : `BRIEFL1ile7socle.md` · **Grille** : `cartesile7.json` · **Packs** : `spritesile7icones.zip` (3),
`spritesile7build434.zip` (74)

---

## 0. Résumé

L'île existe, elle est atteignable, on peut y aborder et y poser un réseau. **Elle est vide** : aucun
bâtiment, aucune ressource neuve, aucune culture, aucune péremption — le bois, la bûcheronneuse et le
défrichage restent le lot L2. Aucune entrée n'a été écrite dans `BUILDINGS` ni dans la table des
ressources (garde-fou du §0 du brief : `BUILDINGS` reste à **114** entrées, mesuré).

| | |
|---|---|
| `GAME_BUILD` produit | **438** (base 437) |
| `GAME_VERSION` produite | **Alpha 20.5** (base Alpha 20.4) |
| `SAVE_VERSION` | **31, INCHANGÉ** — aucun champ de partie ajouté |
| Ancres appliquées | **22**, toutes à `count == 1` **avant** écriture |
| `node --check` | **7/7 sur les 3 variantes CI** (publique / dev / magasin) |
| Suite de validation | **V1→V12 : 37 PASS / 0 KO**, rejouée 2 fois à l'identique |
| Contre-épreuve base 437 | **10/10 assertions échouent** → la suite est falsifiable |
| Non-régression base ↔ patch | **25 PASS / 0 KO** |

**Numéro de build vérifié libre juste avant le push**, sur *toutes* les branches distantes (piège du
mémo 16.9, collision survenue au build 400) : maximum relevé **437** (`main`,
`claude/nouvelle-architecture-8q4l45`, `claude/file-7-a52mbd`) → **438 libre**.

---

## 1. Écart de base et d'architecture — À LIRE EN PREMIER

Le brief est écrit contre le **build 434** et suppose que les sprites s'inlinent **dans le monolithe**.
Ni l'un ni l'autre n'est vrai ici :

- **base réelle = 437 / Alpha 20.4** (le lot « nouvelle architecture » a été fusionné entre-temps) →
  les SHA-256 de fichier du brief **ne peuvent pas correspondre**, c'est attendu ;
- depuis le build 437 le dépôt est en **architecture build-S** :
  `src/index.src.html` + `src/sprites-inline.js` → `tools/build.js` → `Archipel_industry_alpha-7.html`.
  Le monolithe porte le bandeau « FICHIER GENERE … NE PAS EDITER » et un garde `.build-stamp`
  (sha256). **Toute édition à la main y serait écrasée au prochain build.**

**Ce qui a été fait à la place** : les 19 blocs de code sont appliqués à `src/index.src.html`, les
sprites à `src/sprites-inline.js`, puis le monolithe est **régénéré** par `node tools/build.js` et son
empreinte réinscrite dans `.build-stamp`. Ce qui a été vérifié, ce sont **les comptes d'ancres sur la
base réelle** (22/22 à 1) et les **SHA-256 ré-extraits du fichier patché** (§3).

---

## 2. Ancres appliquées (22, toutes à `count == 1`)

Le patcheur refuse d'écrire si une seule ancre n'est pas unique, et il est **idempotent** (rejeu →
« déjà appliqué »).

### 2.1 — `src/index.src.html`, 19 blocs

| # | Ancre | Objet |
|---|---|---|
| T1a | `charToTerrain` | `F: 'forest'` |
| T1b | `TERRAIN_COLORS` | `forest: '#1E3A18'` (repli couleur) |
| T1c | `chTerr` (moteur d'illustration des astuces) | `F: 'forest'`, par cohérence |
| T2a | `NORMAL_ISLANDS` | entrée id 8 (32×32, portR 13, portC 5) **verbatim du JSON** |
| T2b | branche `else` d'`applyGameMode` | repli `difficile` → grille `normal`, **dérivé de la donnée** |
| T3 | `islandLabel` | `id === 8` → « Île 7 » |
| — | `ARCHI_CACHEE` | `{ 6: true, 8: true }` — onglet masqué tant que verrouillée |
| R1 | après `tunnelBorderPieces` | nouvelle `forestBorderPieces` |
| R2 | `baseKey` du dessin | branche `t.terrain === 'forest'` |
| R3 | `const tri = …` | triangle de transition **supprimé** sur la forêt |
| R4 | site de dessin | pose des liserés de lisière, gardée par présence de clé |
| T5a | à côté de `COLLIDER_GOALS` | `const ISLAND8_CONFIRMS = 1280000;` |
| T5b | fin de `TECH_NODES` | nœud 44 « Accès Île 7 » |
| T5c | `island8Unlocked` | `return isNodeConfirmed(game, 44);` |
| T5d | fiche du Collisionneur | ligne « Accès Île 7 — conf / 1 280 000 » |
| T6 | après `ISLAND_KICKSTART_5` | `ISLAND_KICKSTART_8` |
| T6b | table `ISLAND_KICKSTART` | entrée `8:` |
| T7 | `portCasse` | cas explicite `+isl === 8` → toujours en ruine |
| T8 | `freedScopeLabel` | **borne laissée à 7** + commentaire du motif (§8) |

### 2.2 — `src/index.src.html`, 3 blocs (i18n + version)

| # | Ancre | Objet |
|---|---|---|
| i18n | avant la fermeture du bloc 6 | IIFE d'augmentation : `ui` « Île 7 » / « Accès Île 7 » en en/es/de ; `tech` clé `"44"` dans **les 4 langues** |
| bump | `const GAME_BUILD` / `GAME_VERSION` | 437 → **438**, Alpha 20.4 → **Alpha 20.5**, + bloc de commentaire cumulatif (les précédents sont **conservés**) |
| notes | `GAME_NOTES` | note de version du lot |

> ⚠ **`tech` en 4 langues, fr compris, et c'est la convention du fichier** : `I18N.applyToData`
> réécrit `TECH_NODES[].name` depuis `LOCALES.<lang>.tech[String(id)]` quand la valeur est non vide.
> Les nœuds 42 et 43 ont chacun **4** entrées ; le 44 en a donc 4 aussi (mesuré). Sans entrée `fr`, le
> nom inline aurait suffi — mais la table aurait divergé de ses voisines, exactement le piège dénoncé
> au lot A′ (14.97).

### 2.3 — `src/sprites-inline.js`

Un seul bloc appendé, marqué `// ===== L1 — ILE 7 (id interne 8) =====`, placé **après** les
assignations `i5_falaise_*` (dont dépendent les alias) et **avant** `const SPRITE_DATA = …`.

---

## 3. Empreintes et delta d'octets

**Fichier complet** : `3 786 131 → 3 867 476 o`, soit **+81 345 o**.
SHA-256 du monolithe patché : `98c61c1bf64b4474ac2c232362f60d9a5f702e1728a41fe0f8ad6ae51d0edae0`
(= contenu de `.build-stamp`, garde de `tools/build.js`).

Sources : `src/index.src.html` **+20 794 o** · `src/sprites-inline.js` **+60 551 o**.

**Les 7 blocs `<script>`, ré-extraits du fichier patché** (extraction séquentielle, balise en début de
ligne — le compteur refuse de conclure si le compte ≠ 7, piège du 19.7) :

| bloc | base 437 | patch 438 | état |
|---|---|---|---|
| 1 | 418 o · `a50c1c4e…` | 418 o · `a50c1c4e…` | identique |
| 2 | 3 316 o · `69ca1cfd…` | 3 316 o · `69ca1cfd…` | identique |
| 3 | 10 751 o · `d949f1c3…` | 10 751 o · `d949f1c3…` | identique |
| 4 | 131 835 o · `35f4f974…` | 131 835 o · `35f4f974…` | identique |
| **5** | 1 356 685 o · `94f64ae3…` | **1 417 236 o** · `9b0ac8ea288321c2ebb97d9008e9792d3dcb5df8c6ed3aaa3645ff76834023f0` | **+60 551** (sprites) |
| **6** | 437 335 o · `c8d52d2d…` | **438 517 o** · `ca240cfd2048e475e4673e7049cc7ced2fa9c462b28b356217ac1c3ece560b57` | **+1 182** (i18n) |
| **7** | 1 569 626 o · `68de732f…` | **1 589 238 o** · `b8a8042dd8b4dd07ca498ef7e288d401c61fa744b06cb4b718c692169ccd555f` | **+19 612** (code + commentaires) |

---

## 4. T1 — sites énumérant les terrains, et le choix retenu pour chacun

Le terrain est nommé **`forest`, en anglais**, comme ses sept voisins (`water`, `land`, `coast`,
`resource`, `obstacle`, `oil`, `elevator`, `collider`) : la clé de sprite d'un terrain est **dérivée du
nom** (`'tile_i' + isl + '_' + t.terrain`), un terrain français isolé serait une bizarrerie permanente.
Le PNG livré `tile_i8_foret.png` est donc **renommé `tile_i8_forest` à l'injection**.

| Site | Ligne | Décision | Motif |
|---|---|---|---|
| `charToTerrain` | 2654 | **`F: 'forest'` ajouté** | c'est la table de la grille |
| `chTerr` (moteur `TipScenes`) | 23510 | **`F: 'forest'` ajouté** | table jumelle, duplicat historique ; aucune scène d'astuce ne l'utilise en L1, ajouté par cohérence (une future scène forestière n'aurait sinon rendu que de l'eau) |
| `TERRAIN_COLORS` | 3869 | **`forest: '#1E3A18'` ajouté** | repli couleur du canvas si le sprite manque ou n'est pas décodé |
| `COAST_FEATURE_OVERLAY` | 3871 | **INCHANGÉ** | il liste des *overlays posés sur une base* (`obstacle`/`resource`/`oil`). La forêt est une **tuile de base**, pas un overlay |
| `coastIsWater` / `coastIsLand` / `coastIsCoast` | — | **INCHANGÉS** | voir l'avertissement ci-dessous |
| `baseKey` (dessin) | 29897 | **branche `forest` ajoutée** | voir l'avertissement ci-dessous |
| `const tri = …` (dessin) | 29910 | **forêt exclue** | le triangle de transition littorale est un dégradé terre → **sable** : il barrerait la canopée |
| `tryRepair` + voie « accidenté » du tap | — | **INCHANGÉS** | la forêt vierge n'est **pas réparable** : c'est le **défrichage du lot L2** qui la convertira |
| `terrains: [...]` des 114 bâtiments | — | **INCHANGÉS** | aucun ne liste `forest` → la forêt est **inconstructible**, exactement comme `obstacle` |
| Passe `NO_COAST` (`coast` ajouté à tout bâtiment qui liste `land`) | — | **INCHANGÉE** | c'est elle qui rend les 166 côtes de l'île constructibles |

> ⚠ **LE PIÈGE EST PLUS PROFOND QUE LE NOM DU FICHIER — c'est le point le plus important du lot.**
> La clé dérivée du terrain (`tk`) n'est qu'un **REPLI**. La clé réellement dessinée (`baseKey`) sort
> de `coastIsCoast()`, et **`coastIsLand` signifie « pas de l'eau »** — une tuile de forêt y compte
> donc comme de la **TERRE**. `tile_i8_coast` et `tile_i8_land` résolvant tous les deux, `tk` n'aurait
> **JAMAIS** été atteint : la forêt se serait affichée **en herbe, sans la moindre erreur**, ni au
> `node --check` ni en console. D'où la branche explicite dans `baseKey`, et le test V7bis qui
> **espionne `drawImage`** au lieu de faire confiance à la clé calculée.

---

## 5. T4 — sprites : ce qui a été injecté, et pourquoi 11 tuiles sont des alias

**69 statiques + 19 bandes d'animation + 11 alias**, tous dans `src/sprites-inline.js` :

| Famille | n | État |
|---|---|---|
| terrain `tile_i8_{water,coast,land,forest}` | 4 | **actif** (`tile_i8_foret` → clé `tile_i8_forest`) |
| lisière `i8_bord_*` | 16 | **actif** (posée sur la tuile de CLAIRIÈRE) |
| icônes `ile_8`, `ile_8_gris`, `carte_ile_8` | 3 | 2 actives ; **`carte_ile_8` DORMANTE** — l'île 8 n'a pas d'entrée `ARCHI_POS` (aucune liaison maritime en L1), la carte de l'archipel ne l'énumère donc jamais |
| `champ_*` | 16 | **DORMANTS — lot L2** |
| cultures statiques `overlay_cult_*` | 19 | **DORMANTES — lot L2** |
| bandes `overlay_cult_*_breeze` | 19 | **DORMANTES — lot L2** |
| falaises `i8_falaise_*` | 11 | **ALIAS vers l'île 5** (ci-dessous) |

Gardes posées à l'injection : classement exhaustif des 74 fichiers (échec si une clé n'est pas
classée), comptes attendus par famille, et **dimensions vérifiées** (32×32 pour les statiques,
128×32 pour les bandes).

> ⚠ **Les bandes `_breeze` entrent dans `ANIM_DATA` SANS entrée `ANIM_META`, volontairement** :
> `ANIM_BY_SK` fait `if (!meta) continue`, donc une bande sans méta est purement **ignorée** — elle ne
> peut pas se retrouver associée par erreur à une autre clé statique. C'est L2 qui les câblera (via
> `TILE_ANIM_BY_KEY`, comme les tuiles « brise », et non via `ANIM_META`).

### L'aliasage de falaises : ce qui était possible, et ce qui ne l'était pas

Le pack annonce que les falaises, `overlay_obstacle` et les `coast_tri` sont « byte-identiques sur les
six îles » et qu'on peut donc les aliaser sans perte. **Mesuré au pixel, c'est faux.** (PIL est absent
de cet environnement : les PNG ont été décodés par un décodeur écrit en bibliothèque standard, validé
par un contrôle de déterminisme et une contre-épreuve sur `tile_iN_land`.)

- les **silhouettes** coïncident (0 écart d'alpha) ;
- les **couleurs** sont recolorisées par île : 17 teintes pour i1, 17 pour i5, 15 pour i6 ;
  `falaise_s` i1 vs i5 = **320 px sur 1024 différents** ; `overlay_obstacle` et `coast_tri` :
  **1024/1024 px différents**.

**Décision** : les 11 falaises de l'île 8 sont des **alias vers celles de l'île 5** — c'est un
**REPLI assumé, pas une déduplication**. L'île 5 est la référence la plus proche disponible (le
terrain de l'île 8 est lui-même une recolorisation dans cette famille), mais **ce n'est pas un
équivalent exact**. Zéro octet de base64 ajouté (la même chaîne est partagée) et **aucun mécanisme
neuf** : déposer un jour de vrais `i8_falaise_*` **après** ce bloc les remplace, sans toucher au code.
`overlay_obstacle` et les `coast_tri` ne sont **pas** aliasés — la grille de l'île 8 ne contient ni
`obstacle` ni `oil`, et le triangle est explicitement supprimé sur la forêt.

---

## 6. T8 — la borne : décision et motif

**La borne de `freedScopeLabel` est LAISSÉE à `i <= 7`.**

Le brief la présente comme une borne de portée à généraliser. Ce n'en est pas une : cette fonction ne
borne **pas le transit**, elle compose la phrase « … devient constructible sur les îles N à M » de la
**carte du nœud 43** (les bâtiments libérés par `exclusiveUntilNode`).

- **Aucun effet mécanique** : `canPlace` et `tryPlace` lisent `forbiddenIslands` **en direct** et
  laissent **déjà** passer l'île 8. La constructibilité y est acquise sans toucher à cette ligne
  (V8 le vérifie sur le terrain).
- **Effet si on l'étendait** : la phrase s'affiche sur la carte du nœud **43**, donc **avant** que le
  nœud 44 ne soit confirmé → on annoncerait au joueur une portée qu'il ne peut pas encore atteindre.

Le motif est écrit **en commentaire à côté de la ligne**, pour que le lot qui ouvrira la chaîne
maritime de l'île 8 sache que la borne est un choix et pas un oubli.

---

## 7. Résultats des tests — le montage réellement exécuté

Banc : Chromium 1194 (`/opt/pw-browsers/chromium-1194/chrome-linux/chrome`, jamais
`playwright install`), pilote `playwright-core`, serveur HTTP lancé **depuis la racine du dépôt**.
**Suite rejouée 2 fois, résultats identiques, sans flottement.**

### 7.1 — V1 → V12 : **37 PASS / 0 KO**

| Test | Verdict | Montage réellement exécuté |
|---|---|---|
| **V1** | PASS | Partie neuve, boot réel : canvas peint, **0 exception dure** |
| **V2** | PASS ×3 | `colliderConfirms` forgé à **1 279 999** (un de moins que le seuil) → nœud 44 `available`, `island8Unlocked` **faux**, sélecteur à **5 onglets** (1-5 ; 6/7/8 masquées), onglet île 8 **absent** |
| **V3** | PASS ×4 | Passage à **1 280 000** → nœud 44 `condition_ok`, **confirmé par un vrai clic** dans la fiche Recherche → `island8Unlocked` vrai, `islandUnlocked[8]`, sélecteur à **6 onglets**, onglet `title="Île 7"` **non verrouillé**, `switchIsland(8)` → `currentIsland = 8` |
| **V4(pre)** | PASS | **AVANT** déblocage : `game.port[8]` **existe mais est VIDE** (`{}`) — écart au brief, §8 |
| **V4** | PASS | **APRÈS** déblocage : `port[8]` = `{piece_precision: 250, cable_supraconducteur: 150, processeur: 50, piece_meca: 250}` — **exactement** |
| **V5** | PASS | Sauvegarde réelle + **rechargement réel** → `port[8]` **inchangé** (idempotence de la garde `!wasUnlocked`) |
| **V6** | PASS | **100 `piece_precision` dépensés** puis rechargement → **150**, pas 250 (le kickstart ne se recrédite pas). Ressource corrigée par rapport au brief, §8 |
| **V7** | PASS ×2 | Terrains comptés sur la grille servie : **712 eau · 166 côte · 146 forêt** (brut 32×32) ; **1992 eau** une fois padée (`SEA_PAD = 8`) ; **exactement 1 tuile de port**, posée sur une **côte** |
| **V7bis** | PASS | Espion sur `drawImage` + reverse-map data-URL → clé : `tile_i8_forest` **et** `i8_bord_n` **réellement dessinés** sur l'île 8 |
| **V8** | PASS ×2 | **Pose réelle** d'une passerelle : **REFUSÉE** sur une tuile `forest` (`canPlace` faux **et** `tryPlace` faux) ; **contre-épreuve** : **ACCEPTÉE** sur une tuile `coast` voisine |
| **V9** | PASS ×12 | Les **4 langues** : `islandLabel(8)` rend « Île 7 » / « Island 7 » / « Isla 7 » / « Insel 7 » (et l'île 7 interne rend toujours « Île 6 S » / « Island 6 U » / « Isla 6 S » / « Insel 6 U ») ; nom du nœud 44 traduit ; **aucun « île 8 » dans le DOM rendu** |
| **V10** | PASS ×2 | Espion `drawImage` sur l'île 8 : **sprite de port EN RUINE** dessiné, sprite intact absent ; **contre-épreuve île 1 au même instant** : port **INTACT** |
| **V11** | PASS ×3 | **Save d'avant le lot** (sans île 8) chargée par le **vrai chemin** : 0 exception, île 8 **recréée mais VERROUILLÉE**, nœud 44 `locked`, onglet absent |
| **V12** | PASS ×3 | Mode **`difficile`** forcé dans le slot `localStorage` puis **rechargement par `loadSave`** : contre-épreuve — les îles 1-7 sont bien **compactes** (île 1 : 32×32 en normal → **28×28** en difficile) ; l'île 8 sert sa grille **`normal`** en repli (48×48 padée, mêmes comptes) ; port défini, île atteignable, canvas peint, 0 exception |

### 7.2 — Contre-épreuve sur la base 437 : **10/10 assertions échouent**

La même suite jouée contre le build 437 non patché échoue partout où elle doit : `islandLabel(8)` y
rend **« Île 8 »**, le nœud 44 n'existe pas, l'île 8 n'existe pas. **La suite mesure donc bien
quelque chose.**

### 7.3 — Non-régression base ↔ patch : **25 PASS / 0 KO**

```
PASS NR iles 1..7 : grille IDENTIQUE (32x32, 29x36, 32x32, 32x32, 34x34, 32x32, 21x25)
PASS NR PORTS[1..7] identique · PORTS[8] : base=null patch=21,13
PASS NR portCasse iles 1..7 identique — {"1":true,"2":true,"3":true,"4":true,"5":true,"6":false,"7":false}
PASS NR ISLAND_ACCESS_NODE INTACT (ni 8: ni 9:) — {"2":2,"3":8,"4":14,"5":21,"6":28}
PASS NR SHIP_LINKS INTACT · nb de batiments 114 -> 114 · TECH_NODES 43 -> 44
PASS NR terrains : seul F change · sprites +69 / -0 · anims +19 / -0 · 0 exception
```

### 7.4 — Contrôles de chaîne

- `node --check` : **7/7 sur `game-public`, `game-dev` et `game-store`**, **après** le bump et
  **après** avoir écrit les commentaires.
- **Gardes de comptage de la CI rejouées avec le `sed` RÉEL d'`android.yml`** (leçon du build 429,
  qui a cassé le run 561 parce qu'un commentaire de validation écrivait le nom du service de soutien
  en clair) : les **4 gardes de contenu** d'`android.yml` passent — `ko-fi` = **1** en publique /
  **0** en magasin, `SELF_UPDATE` basculé et absent en magasin, `DEV_BUILD` aux deux valeurs.
  ⚠ Une première simulation *maison* du `sed` a donné un faux positif (`ko-fi` = 1 en magasin) parce
  que **mon motif** portait un `// DEV_BUILD` que la ligne réelle n'a pas. **C'était ma simulation
  qui était fausse, pas le fichier** : rejouée avec le `sed` du workflow, la garde passe.
- `GAME_NOTES` conforme au `grep -P '^const GAME_NOTES = "[^"]*";$'` de la CI ; extraction de
  `GAME_BUILD` / `GAME_VERSION` / `GAME_NOTES` simulée → `438` / `Alpha 20.5` / la note complète.
- **Nomenclature** : 35 occurrences de « île 8 » dans le fichier, dont **29 en commentaire** et **6
  qui sont des IDENTIFIANTS** (`island8Unlocked` — nom **préexistant depuis le build 14.61** — et
  `ISLAND8_CONFIRMS`). **Zéro occurrence visible par le joueur**, ce que V9 prouve directement sur le
  DOM rendu dans les 4 langues.

---

## 8. Écarts au brief, avec la raison

1. **Base et architecture** (§1) — brief écrit contre le 434 + sprites dans le monolithe ; base réelle
   **437** en **architecture build-S**. Édition dans `src/`, monolithe **régénéré**. Vérifié à la
   place : **22/22 ancres à `count == 1`** sur la base réelle, et les SHA-256 **ré-extraits du fichier
   patché**.

2. **V4, « `port[8]` doit être nul avant déblocage »** — **faux par construction** :
   `ensureIslandDefaults(g)` crée `g.port[isl] = {}` pour **toutes** les îles de `ISLAND_TERRAINS`, île
   8 comprise, dès le chargement. L'assertion est devenue « **existe mais est VIDE** », ce qui est la
   preuve **plus forte** : elle montre que *lire* l'île ne fabrique pas le kickstart, alors que
   « n'existe pas » aurait seulement montré que la structure n'est pas encore créée.

3. **V6, « dépenser 100 processeur → 150 et non 250 »** — le brief se contredit : sa **propre table de
   kickstart** donne `processeur: 50`. La ressource à **250** est `piece_precision`. Le test a été
   déplacé sur `piece_precision` (250 → 150 après dépense et rechargement) ; l'invariant testé (le
   kickstart ne se recrédite pas) est **identique**.

4. **V12, mode difficile** — le brief suppose qu'on peut créer une partie difficile depuis l'écran de
   choix. **Impossible depuis le lot 3B (build 364)** : la `ModeModal` n'a plus aucun point d'entrée,
   une partie neuve démarre directement en Normal. Le mode `difficile` est donc forcé **dans le slot
   `localStorage`** puis rechargé par le **vrai chemin `loadSave`**. ⚠ `applyGameMode` **n'est jamais
   appelée à la main** (piège 14.35 : elle repeuple les defs sans reconstruire les grilles déjà
   créées → la boucle de rendu lève à chaque frame).

5. **T8, borne laissée à 7** — décision et motif au §6.

6. **Falaises aliasées vers l'île 5** — la promesse « byte-identiques » du pack est **fausse, mesurée
   au pixel** ; l'alias est un repli assumé, pas une déduplication (§5).

7. **`ISLAND_ACCESS_NODE` non touchée** — le brief n'en parle pas, mais c'était la voie « naturelle »
   pour brancher un accès d'île. **Elle aurait cassé le souterrain** : cette table décrit la chaîne
   **maritime** et sert **aussi** à `portCasse(isl)` via `[isl + 1]`. Y poser `8: 44` mettrait le port
   du **souterrain** « en ruine » et ferait apparaître, depuis l'île 6 S, une **cible de réparation
   pour une île absente de la carte**. D'où le **cas explicite** dans `portCasse` (T7).

8. **`ARCHI_CACHEE` — ajout hors liste de travail du brief.** Sans lui, `IslandSelector` (qui énumère
   `ISLAND_TERRAINS`) afficherait un onglet « Île 7 » **verrouillé et inatteignable dès la partie
   neuve**, dans la bande que le lot 18.9 a justement dégagée en retirant celui de l'île 6 pour la
   même raison. Effet sur la carte de l'archipel : **nul** (pas d'entrée `ARCHI_POS`).

9. **Seuil hors de `COLLIDER_GOALS`** — le brief dit « 1 280 000 confirmations », sans dire où. Il est
   posé dans une constante **dédiée** (`ISLAND8_CONFIRMS`) et **pas** dans `COLLIDER_GOALS` : cette
   table est indexée par **palier** (1..3) et pilote `colliderGoalLocked` ; une 4ᵉ entrée nommerait un
   palier inexistant et, `COLLIDER_FLAVORS[4]` étant absent, **l'émission de codes casserait**.
   ⚠ **Seuil atteignable, prouvé sur le graphe** : au palier 3, nœud 43 confirmé,
   `colliderGoalLocked` sort `false` (`COLLIDER_REPAIR_NODES[3]` est `undefined`) et
   `colliderAutoAvailable` devient vrai → la machine tourne **sans plafond**.

---

## 9. Points restés ouverts

1. **Le nœud 44 est VISIBLE dans l'arbre dès que le nœud 28 est confirmé.** Le masquage de fin d'arbre
   (lot 19.7) replie tout ce qui dépasse en **une** carte tant que le nœud 25 puis le 28 ne sont pas
   confirmés ; passé le 28, l'arbre est **entièrement dévoilé** — le nœud 44 y compris. Le joueur
   apprend donc l'existence d'une 7ᵉ île avant d'avoir fini le Collisionneur. **Non traité** : le
   brief ne le demande pas, et étendre le masquage est un arbitrage de game design (le nom
   « Accès Île 7 » est en soi le spoiler, comme l'était « Accès Île 6 »). **À trancher par Ethan.**

2. **La borne de `freedScopeLabel`** (§6) — laissée à 7, motif écrit en commentaire, à rouvrir si le
   lot qui ouvre la chaîne maritime veut annoncer la portée.

3. **L'entrepôt de l'île 8 reste en ruine pendant tout le lot** (T7), et c'est **purement visuel** :
   le port y est fonctionnel côté réseaux. Son nœud de réparation arrive au **lot L5**.

4. **`carte_ile_8` est dormante** : l'île 8 n'a pas d'entrée `ARCHI_POS` (aucune liaison maritime en
   L1). Le lot qui ouvrira la liaison devra l'ajouter — et **repasser par `ARCHI_POS_5`/l'enveloppe
   20/80** du lot 19.6, qui étale les îles tant que l'île 6 n'est pas visible.

5. **Les falaises de l'île 8 empruntent les couleurs de l'île 5** (§5) — à remplacer par de vrais
   `i8_falaise_*` si l'écart se voit en jeu ; aucun code à toucher.

6. **Non couvert : le rendu sur appareil.** Lisibilité de la lisière de forêt à la taille de tuile
   réelle, contraste forêt/côte, tenue de l'onglet « Île 7 » à 6 onglets sur un écran étroit — rien de
   tout cela n'a été jugé ailleurs qu'en navigateur de bureau.

7. **Champ et cultures dormants** (16 + 19 + 19 bandes) : aucun code ne les lit en L1. Ils sont
   injectés pour que **L2 n'ait plus que du code à écrire**.

---

## 10. Livraison

Branche **`claude/file-7-a52mbd`**, **PR ouverte, NON fusionnée** — le merge sur `main` appartient à
Ethan seul (il déclenche `android.yml`, qui republie l'APK, `index.html` et `version.json`).
