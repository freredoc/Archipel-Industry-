# RAPPORT — LOT L4 : la transformation de l'Île 7 (id interne 8)

> **Convention de nommage, à lire en premier.** L'île porte l'**id interne 8** dans tout le code
> (`ISLAND_TERRAINS`, `game.islands[8]`, `game.port[8]`) et s'affiche **« Île 7 »** au joueur, via
> `islandLabel(8)`. L'id 7 est le **souterrain** de l'île 6, affiché « Île 6 S ». Ce rapport dit
> « île 8 » quand il parle du code et « Île 7 » quand il parle de l'écran. **Aucun texte visible par
> le joueur n'écrit « île 8 »** — vérifié : les 12 occurrences ajoutées sont toutes dans des
> commentaires `//`.

---

## 1. Versions produites

| | avant | après |
|---|---|---|
| `GAME_BUILD` | 439 | **440** |
| `GAME_VERSION` | `Alpha 20.6` | **`Alpha 20.7`** |
| `SAVE_VERSION` | 31 | **31 — INCHANGÉ** |

**Le numéro n'a pas été proposé par le brief** (règle §0) : il a été **relevé sur les 27 branches
distantes**, pas seulement sur `main`. Maximum constaté = **439** (`origin/main` et
`origin/claude/file-7-a52mbd`), la suivante étant 437. **440 est donc libre**, et l'étiquette
`Alpha 20.7` n'est portée par aucune branche.

`SAVE_VERSION` reste à 31 : le lot n'ajoute **aucun champ de partie**. Deux ressources, quatre
bâtiments et deux nœuds — tous dérivés des tables, tous reconstruits au chargement.

`GAME_NOTES` est réécrit (la CI en fait le champ `notes` de `version.json`).

---

## 2. Architecture build-S — ce qui a été édité, ce qui a été régénéré

Le monolithe **n'a pas été édité**. Deux sources, un générateur :

| fichier | rôle | delta |
|---|---|---|
| `src/index.src.html` | source du jeu (13 ancres) | 2 480 934 → 2 497 062 o (**+16 128**) |
| `src/sprites-inline.js` | 3 sprites du pack | +13 lignes |
| `Archipel_industry_alpha-7.html` | **généré** par `node tools/build.js` | 3 904 299 → 3 924 096 o (**+19 797**) |
| `.build-stamp` | sha256 du généré, re-tamponné par le build | `d55b7653…` → `7efa0ddc…` |

Sortie du générateur : `3924096 o, 34692 lignes · 1545 lignes injectées depuis sprites-inline.js ·
blocs <script> détectés (^<script) : 7 — OK`.

### SHA-256 par bloc, **re-extraits du fichier généré**

| bloc | car. (439) | car. (440) | delta | sha256 (440) | modifié ? |
|---|---|---|---|---|---|
| 1 | 413 | 413 | 0 | `a50c1c4e7f4a304c` | non |
| 2 | 3 275 | 3 275 | 0 | `69ca1cfded2eaa08` | non |
| 3 | 10 751 | 10 751 | 0 | `d949f1c3687aedad` | non |
| 4 | 131 835 | 131 835 | 0 | `35f4f974f4b2bcd4` | non |
| 5 | 1 422 914 | 1 426 573 | **+3 659** | `5237c2546b0befb2` | oui (i18n) |
| 6 | 436 317 | 439 224 | **+2 907** | `53d8159e88b00bd8` | oui (sprites) |
| 7 | 1 585 399 | 1 598 427 | **+13 028** | `ace2028724795876` | oui (jeu) |

sha256 du fichier complet : `7efa0ddc1cce8e217ebc0a10371f18de7175e60ac8f1fa09051001d1e7071e20`
(= le contenu de `.build-stamp`).

---

## 3. Les 13 ancres, et leur compte

Toutes **extraites du fichier**, jamais retapées, et toutes à **`count == 1`** avant écriture. Le
patcheur (`patch_L4.py`) sort en erreur et n'écrit rien si un compte diffère.

| ancre | ce qu'elle fait | count |
|---|---|---|
| `C3-RES_SHORT` | noms courts `planche` / `fil_carbone` | 1 |
| `C1-RES_TIER-t1` | `planche: 't1'` en FIN de bloc t1 | 1 |
| `C1-RES_TIER-t2` | `fil_carbone: 't2'` en FIN de bloc t2 | 1 |
| `C4-CARRIER` | `planche`/`fil_carbone` → `'road'` | 1 |
| `C-SPRITE-OVERRIDE` | repli de sprite du foyer | 1 |
| `C6-BUILDINGS` | les 4 définitions de bâtiment | 1 |
| `C9-TOOLBAR-energy` | `foyer_charbon` dans « Énergie » | 1 |
| `C9-TOOLBAR-groupe` | nouveau groupe « Bois et carbone » | 1 |
| `C8-TECH_NODES` | nœuds 45 et 46 | 1 |
| `C-I18N` | bloc d'augmentation 4 langues | 1 |
| `VER-BUILD` | commentaire cumulatif + `GAME_BUILD` | 1 |
| `VER-LABEL` | `GAME_VERSION` | 1 |
| `VER-NOTES` | `GAME_NOTES` | 1 |

**`DIR_ART_IDS` n'a pas été touché** (marqueur de non-régression du brief, C10) : vérifié au diff.

### Idempotence — un piège payé, puis fermé

Le premier patcheur testait l'idempotence sur *l'absence de l'ancre*. Or **trois ancres sont un
préfixe ou un suffixe de leur remplacement** (insertion en tête/queue de bloc : `BLD_SPRITE_OVERRIDE`,
le groupe de toolbar, l'IIFE i18n) : l'ancre survit à l'écriture, donc le patcheur se rejouait.
**Mesuré : +3 705 octets au second passage.** Le test porte désormais sur un **marqueur** propre au
patch. Second passage : **13/13 « déjà patché », delta +0 octet.**

---

## 4. Ce que le lot ajoute

### 4.1 Deux ressources

| id | tier | place dans `RES_TIER` | `RES_SHORT` | porteur | fr / en / es / de |
|---|---|---|---|---|---|
| `planche` | **t1** | **fin** du bloc t1, derrière `yellow_cake` | `planche` | `road` | planche / plank / tabla / Brett |
| `fil_carbone` | **t2** | **fin** du bloc t2, derrière `combustible_u235` | `fil carbone` | `road` | fil carbone / carbon fiber / fibra carbono / Kohlefaser |

**La place dans la table EST le rang d'affichage** (`RES_ORDER_RANK` = ordre de déclaration), et
l'ordre interne d'un tier suit la **progression** : les deux se rencontrent après l'île 5, donc en
fin de bloc, jamais à côté de leurs voisines de famille. **Mesuré** (X11) :
`yellow_cake(17) < planche(18) < acier(19)` et `combustible_u235(26) < fil_carbone(27) < beton_arme(28)`.

L'entrée `CARRIER_BY_RES` **n'est pas décorative** : `TRADE_RESOURCES` est construit par
`Object.keys(CARRIER_BY_RES)`. Sans elle, les deux ressources seraient à jamais hors du commerce
maritime, y compris le jour où l'île recevra sa liaison. Aucune n'entre dans `INITIAL_RESOURCES`.

### 4.2 Quatre bâtiments — et le coût **réellement payé**

C'est le point du §4 du brief (« vérifier le coût EFFECTIVEMENT payé, pas celui écrit »).
`TIER_COST_MULT` multiplie par palier : t1 ×2, t2 ×4. Écrits tels quels, la scierie coûterait
**400 bois** et la filerie **600 planches**.

**Écart au brief, assumé et documenté** : plutôt que de rétrograder les deux en t0, ils gardent leur
tier et portent **`noTierMult: true`** — exemption explicite qui existe déjà (précédent
`separateur_air`, dont le commentaire dit « coûts du brief = coûts payés »). Raison : le tier ne
gouverne pas que le coût, il gouverne aussi **l'accent du menu** (`TIER_ACCENT`) et **le rang du
Calculateur** (`CALC_BTIER`) — une scierie qui produit une ressource t1 doit se lire t1. Le brief
autorise explicitement l'ajustement (« les tiers ci-dessus sont indicatifs : ajuster pour que le coût
payé soit celui du tableau »), c'est le coût payé qui est opposable, et il l'est.

| id | tier | terrains | coût **payé** (mesuré) | intrants | sortants | `power` |
|---|---|---|---|---|---|---|
| `charbonniere` | t0 | `land`, `coast` | 200 bois · 100 pierre | 2 bois | 1 charbon | **0** |
| `foyer_charbon` | t0 | `coast` | 200 pierre · 100 bois | 8 charbon | 128 `energie_kw` | **0** |
| `scierie` | t1 + `noTierMult` | `land`, `coast` | 200 bois · 200 pierre | 2 bois | 1 planche | 64 |
| `filerie_carbone` | t2 + `noTierMult` | `land`, `coast` | 150 planche · 200 pierre | 4 charbon | 1 fil de carbone | 256 |

Tous : `exclusiveIsland: 8`, `size: [1, 1]`, `needRoad: true`.

**Le test X1 mesure le coût à l'unité près, dans les deux sens** : port garni du coût **moins 1** sur
une ressource → pose **refusée** ; port garni du coût **exact** → pose **acceptée** et port **vidé à
zéro**. 4/4 aux quatre bâtiments.

**Trois contraintes, et aucune n'est décorative :**

1. **`power: 0` sur la charbonnière et le foyer.** L'île n'importe rien : si le premier maillon de la
   chaîne énergétique demandait du courant, il faudrait du courant pour faire du courant — boucle
   morte, l'île ne démarrerait jamais. Le foyer est de plus un **producteur**, et tous les producteurs
   du jeu sont à `power: 0` avec `outputs.energie_kw`. **Mesuré** (X2) : la demande de l'île 8 reste à
   **0 kW** avec une charbonnière qui tourne.
2. **`coast` dans les quatre listes.** L'île n'a **aucune** tuile `land` au départ (mesuré : water
   1992 · coast 166 · forest 146 · land 0). Aucun des quatre n'entre dans `NO_COAST`, qui *retire*
   `coast`.
3. **Le coût écrit est le coût payé** (ci-dessus).

**`needRoad: true` est purement déclaratif** : plus aucun code ne le lit depuis 10.34 (le garde-fou
`hasAdjacentRoad` a été retiré de `canPlace`/`tryPlace`) — vérifié, les 31 occurrences du fichier sont
toutes des déclarations. Le vrai gating est `roadReachesPort` dans le tick. On le conserve pour
l'homogénéité avec les trente autres bâtiments qui le portent.

### 4.3 Sprites

| clé | source | dims | ct | octets | sha256 **re-extrait du fichier généré** | == pack |
|---|---|---|---|---|---|---|
| `bat_charbonniere` | pack | 32×32 | 6 | 596 | `c220855b7f3dc7dd` | oui |
| `bat_scierie` | pack | 32×32 | 6 | 639 | `00069e8b7d49a5a9` | oui |
| `bat_filerie_carbone` | pack | 32×32 | 6 | 726 | `98cae52c8283e964` | oui |

**Le foyer n'a pas d'art dans le pack.** `BLD_SPRITE_OVERRIDE` lui donne une **liste de candidats**
`['bat_foyer_charbon', 'bat_centrale_charbon']` : la clé définitive est citée en tête, le repli
derrière. Le jour où le PNG est déposé sous ce nom, il est pris **automatiquement, sans retoucher au
code**. **Repli assumé** : en attendant, le foyer emprunte l'art de la centrale à charbon.

**7 des 15 PNG du pack restent hors périmètre** (ferme, pont, presse à lamelle, raffinerie antique,
élastomère, fibre de carbone, polymère premium) — les injecter serait du poids mort. Avec les 5 du
lot L2, **8 sur 15 sont désormais injectés**.

### 4.4 Deux nœuds de recherche

| id | nom | prereq | mode | île | livraison | débloque |
|---|---|---|---|---|---|---|
| 45 | Feu et Charbon | 44 | `delivery` | 8 | **500 bois** | `charbonniere`, `foyer_charbon` |
| 46 | Scierie et Filerie | 45 | `delivery` | 8 | **300 charbon** | `scierie`, `filerie_carbone` |

Calqués sur le **nœud 28** (`mode: 'delivery'`, `reqs: []`) : la livraison est le seul coût, rien à
« produire » en plus. `RESEARCH_DELIVERY_FACTOR` vaut 1 → les quantités écrites sont les quantités
exigées.

**Où la livraison lit le stock — vérifié au fichier, puis par test.** `deliveryIsland(game, def)`
renvoie `def.island` quand il existe, donc **8** ; `techDeliver` débite `portPool(game, 8)`, qui est
`game.port[8]` (`portPool` ne détourne que l'île 7 vers le port de l'île 6). `techDeliver` **et**
`deliveryReady` exigent en outre `def.island === game.currentIsland` → **la livraison se fait sur
place**, et la pastille de notification ne s'allume pas depuis une autre île.

**L'entrepôt de l'île 8 est `portCasse`** — cas *explicite* dans la fonction (`if (+isl === 8) return
true`, posé au lot L1). Mais cet état est **purement visuel** : aucun code mécanique ne le lit. Le
dépôt et le débit passent normalement. **Prouvé par X6**, pas supposé : 499 bois → refus ; 500 bois →
nœud confirmé, port débité **à zéro**, les deux bâtiments débloqués.

### 4.5 Menu

`BUILDING_NODE` est dérivé de `unlocks.buildings`, mais **l'inscription dans `TOOLBAR_GROUPS` est
indispensable en plus** : le menu énumère les **groupes**, pas `BUILDINGS`. Un bâtiment absent de la
table n'est pas « masqué », il est **inposable**.

- **`foyer_charbon` → groupe « Énergie »** (c'est une centrale ; c'est là que le joueur cherche de
  quoi s'alimenter, à côté de `centrale_charbon`).
- **`charbonniere`, `scierie`, `filerie_carbone` → nouveau groupe « Bois et carbone »**, posé après
  « Quantique ». Les groupes du menu suivent les **familles de matière**, et chaque île récente a la
  sienne (« Tungstène », « Quantique »). Le `label` est une **clé i18n gettext** (le menu rend
  `I18N.t(g.label)`), traduite dans le bloc d'augmentation ; la `key` (`'bois'`) est stable et sert
  aux filtres `NETWORK_GROUPS`/`BUILD_GROUPS`.

### 4.6 i18n

Un bloc d'augmentation, **quatre couches**, **quatre langues, `fr` comprise** pour `res`/`bld`/`tech`
(`applyToData` réécrit depuis la table dès qu'une valeur est non vide : une entrée `fr` manquante
ferait diverger ces lignes de toutes leurs voisines). `ui` n'a **pas** de français — le modèle est
gettext, la clé EST le texte français. Fusion non destructive (`if(!L.x[k])`), donc rejouable.

---

## 5. Contrôles de compilation

### `node --check` — 7 blocs × 3 variantes CI

Le compte de blocs est vérifié **AVANT** la boucle (une extraction interrompue laisserait un dossier
partiel et un « 7/7 » mensonger sur un seul fichier), puis le nombre de fichiers écrits est
re-vérifié après écriture.

| variante | blocs détectés | `node --check` |
|---|---|---|
| `game-public.html` | 7 | **7/7 OK** |
| `game-dev.html` | 7 | **7/7 OK** |
| `game-store.html` | 7 | **7/7 OK** |

**21/21, 0 KO.**

### Gardes CI — rejouées avec les **commandes littérales** d'`android.yml`

⚠ **Le piège a coûté un faux rouge aux lots L1 et L2** : une paraphrase du `sed` de la variante
magasin donnait `ko-fi = 1` sur une garde bloquante. Les commandes ont donc été **copiées-collées
depuis le workflow**, sans reformulation, et **rejouées APRÈS l'écriture du bloc de commentaires**
(leçon du build 429 : un commentaire suffit à faire échouer un `grep -c` non ancré).

| garde `android.yml` | résultat |
|---|---|
| `grep -q "^const DEV_BUILD = true;$" game-dev.html` | ✔ |
| `grep -q "^const DEV_BUILD = false;$" game-public.html` | ✔ |
| `grep -q "^const SELF_UPDATE = true;$" game-store.html` (entrée) | ✔ |
| `grep -q "^const SUPPORT_URL = 'https://ko-fi.com/freredoc';$" game-store.html` (entrée) | ✔ |
| `[ "$(grep -c 'ko-fi' game-store.html)" = "0" ]` après les 2 `sed` | **0** ✔ |
| `[ "$(grep -c 'const SELF_UPDATE = true;' game-store.html)" = "0" ]` | **0** ✔ |
| `grep -q "^const SELF_UPDATE = true;$" game-public.html` | ✔ |
| `[ "$(grep -c 'ko-fi' game-public.html)" = "1" ]` | **1** ✔ |
| `grep -qP '^const GAME_NOTES = "[^"]*";$'` | ✔ (aucun `"` dans les notes) |
| `grep -oP '^const GAME_BUILD = \K[0-9]+'` / `GAME_VERSION` | **440** / **Alpha 20.7** |
| `sed` du cache `sw.js` puis `grep -q "^var CACHE = 'archipel-$BUILD';$"` | ✔ `archipel-440` |

**11/11 PASS.** Le lot n'écrit aucun nom de service de soutien en commentaire, et aucun motif
surveillé par un `grep -c` non ancré n'apparaît en texte libre.

---

## 6. Validation X1 → X13

Bancs : `playwright-core` + Chromium 1194 (`/opt/pw-browsers/chromium-1194/chrome-linux/chrome` ;
`playwright install` proscrit), servi en **HTTP** sur 127.0.0.1, **jamais** `file://`.

### Résumé

| test | ce qu'il prouve | verdict |
|---|---|---|
| **X1** | le coût **payé** de chacun des 4 = le tableau, à l'unité près (coût−1 refusé, coût exact accepté et port vidé) | 4/4 PASS |
| **X2** | charbonnière : 2 bois/s → 1 charbon/s, et **0 kW débité** | 2/2 PASS |
| **X3** | le foyer produit **128 kW sur l'île 8** — contre-preuve : `game.energy[1]` **inchangé** | 3/3 PASS |
| **X4** | scierie sans courant → arrêt motif `power` ; foyer alimenté → 1 planche/s | 2/2 PASS |
| **X5** | route coupée → la charbonnière s'arrête (motif `road`) ; rétablie → elle repart | 2/2 PASS |
| **X6** | 499 bois refusé, 500 bois → nœud 45 confirmé et port débité (**entrepôt en ruine**) ; livraison **sur place** exigée | 3/3 PASS |
| **X7** | les 4 au menu de l'île 8 + groupe « Bois et carbone » ; contre-preuve île 1 (grisés, clic n'arme rien, **identique aux 2 bâtiments L2**) | 5/5 PASS |
| **X8** | filerie : 4 charbon/s → 1 fil/s, charbon total = 4 + 3×8 | 2/2 PASS |
| **X9** | **jouabilité réelle, carte réelle, gestes réels** | 4/4 PASS |
| **X10** | sauvegarde / rechargement | 3/3 PASS |
| **X11** | inventaire, 4 langues, rang de déclaration | 5/5 PASS |
| **X12** | **non-régression `centrale_charbon` sur les îles 1-5** | 2/2 PASS |
| **X13** | non-régression souterrain (île 7) + coupe du lot L2 | 2/2 PASS |

### Le détail, test par test

**X1 — le coût payé est celui du tableau.** C'est la demande centrale du §4 du brief : les tiers y
sont dits « indicatifs », c'est le coût **effectivement débité** qui fait foi. Le banc vide le port,
y met le coût **moins une unité** (pose refusée), puis le coût **exact** (pose acceptée, port
retombé à zéro sur chaque clé). Les quatre passent :

| bâtiment | tier | coût débité | port après |
|---|---|---|---|
| `charbonniere` | `t0` | 200 bois · 100 pierre | 0 · 0 |
| `foyer_charbon` | `t0` | 200 pierre · 100 bois | 0 · 0 |
| `scierie` | `t1` + `noTierMult` | 200 bois · 200 pierre | 0 · 0 |
| `filerie_carbone` | `t2` + `noTierMult` | 150 planche · 200 pierre | 0 · 0 |

⚠ **`noTierMult` est indispensable, pas décoratif.** `TIER_COST_MULT` multiplie le coût de `t1` par
2 et celui de `t2` par 4 **au chargement du module**, une fois pour toutes. Sans l'exemption, la
scierie coûterait 400 bois + 400 pierre et la filerie 600 planches + 800 pierre — le double et le
quadruple du tableau. Le précédent est `separateur_air` (13.59), qui porte le même drapeau pour la
même raison. `t0` n'a pas d'entrée dans la table : les deux premiers n'ont rien à exempter.

**X2 — charbonnière : 2 bois/s → 1 charbon/s, et 0 kW.** Débits mesurés `2.0000` et `1.0000` par
seconde, et **demande électrique de l'île = 0 kW**. Le brief en fait un impératif : une machine à
`power > 0` posée avant le foyer serait inerte et le joueur ne saurait pas pourquoi.

**X3 — le foyer produit 128 kW, sur l'île 8 et nulle part ailleurs.** `gross = 128 kW`,
`balance = +128 kW`, `8 charbon/s` consommés. Contre-preuve : `game.energy[1]` **strictement
inchangé** — l'électricité est déjà par île, il n'y avait aucune garde à écrire, mais le brief
demande de le **prouver**, pas de le croire.

**X4 — la scierie dépend vraiment du foyer.** Foyer à sec → scierie `active = false`,
`discReason = 'power'`, **croissance nulle** sur 12 ticks. Foyer réalimenté → `1.0000 planche/s`.

**X5 — la route compte.** Une seule tuile de route retirée **au contact du port** suffit : la
charbonnière passe de +10 charbon / 10 ticks à **+0 sur 15 ticks**, motif `road`. Route rétablie,
elle repart à +10 / 10 ticks.

**X6 — la livraison du nœud 45.** 499 bois : `techDeliver` refuse, statut `condition_ok`.
500 bois : nœud **confirmé**, port **débité à zéro**, bâtiments débloqués. Et la livraison **exige
d'être sur place** : depuis l'île 1 elle est refusée, depuis l'île 8 elle passe. ⚠ L'entrepôt de
l'île 8 est en **ruine** (`portCasse` a un cas explicite pour elle) : le test prouve **par l'effet**
que cet état est purement visuel et n'empêche ni le dépôt ni le débit.

**X7 — les quatre sont posables, et seulement là.** Les quatre vignettes sont au menu de l'île 8, le
groupe **« Bois et carbone »** existe. Contre-preuve sur l'île 1 : les quatre sont **grisés**
(`.tool-btn.off-island`) et **cliquer leur vignette n'arme aucun outil** — et les deux bâtiments du
lot L2 se comportent **à l'identique**, donc L4 n'introduit rien de nouveau sur ce chemin.

**X8 — la filerie.** `4 charbon/s → 1.0000 fil de carbone/s`, `gross = 384 kW` (trois foyers),
consommation totale de charbon **28,000 /s** = 4 (filerie) + 3 × 8 (foyers), à 1e-6 près.

**X9 — le seul test qui dise si le lot est jouable.** Carte réelle, aucun défrichage forcé, gestes
réels. Topologie relevée au départ : **1992 eau, 166 côte, 146 forêt, 0 terre**, et un couloir de
**5 tuiles** posables reliées au port (⚠ le brief en annonce 6 — la mesure en donne 5), avec
5 lisières de forêt. Le banc joue ensuite la vraie boucle : poser la bûcheronneuse, taper une tuile
de forêt, attendre les 60 s de coupe, **déplacer la machine** quand la clairière s'éloigne.
Résultat : **24 coupes, 17 déplacements, 1440 bois au port**, puis la carrière rustique payée en
bois (1440 → 1290), puis **220 ticks** de production de pierre (220), puis la **livraison des
500 bois** du nœud 45 et la pose d'une charbonnière — le tout par le jeu, sans forge d'état.

**X10 — sauvegarde et rechargement.** Les 4 bâtiments reviennent avec le bon id, les nœuds 45 et 46
restent `confirmed`, et `planche` / `fil_carbone` sont relus **à l'unité près**.

**X11 — l'inventaire.** Noms traduits dans les 4 langues (`planche` / `plank` / `tabla` / `Brett`,
`fil carbone` / `carbon fiber` / `fibra carbono` / `Kohlefaser`), tiers `t1` / `t2`, porteur
`road`, et **rang = ordre de déclaration** : `yellow_cake`(17) < `planche`(18) < `acier`(19), et
`combustible_u235`(26) < `fil_carbone`(27) < `beton_arme`(28).

**X12 — `centrale_charbon` n'a pas bougé.** Coût `{cable:80, ciment:100, acier:80}`, terrains
`['coast']`, `forbiddenIslands [6,7]`, recette identique — et elle reste **posable sur les cinq
premières îles** (une tuile trouvée sur chacune). C'est la non-régression que le §1 exige : le lot
ne réutilise pas la centrale, il ne devait pas non plus l'abîmer.

**X13 — le souterrain et le lot L2.** Île 7 interne : 12 tuiles de tunnel, `portPool(7)` rend bien
le **port de l'île 6**, `DRILL_TIME = 300`, libellés `« Île 6 S »` et `« Île 7 »`. Et la coupe du
lot L2 fonctionne toujours **au tap réel** (`felling = true`, `fellDir = 1`).

### Stabilité

La suite a été **rejouée trois fois**. Les 39 lignes d'assertion des trois passes sont
**identiques caractère pour caractère** deux à deux (`diff` vide sur les trois paires), valeurs
mesurées comprises : **aucun flottement**.

---

## 7. Contre-épreuve sur la base 439

Une suite séparée (`contre_L4.js`) rejoue les mêmes questions sur la **base non patchée**, servie
depuis une copie de `main`. Sans elle, un « tout est vert » ne prouverait pas que la suite mesure
quelque chose.

| | ce qui est vérifié sur 439 | résultat |
|---|---|---|
| **C1** | la base est bien `439` / `Alpha 20.6` | ✔ |
| **C2** | aucun des 4 bâtiments n'existe | `[false, false, false, false]` |
| **C3** | `planche` / `fil_carbone` absents des **3** tables (`RES_TIER`, `RES_SHORT`, `CARRIER_BY_RES`) | tous `null` |
| **C4** | les nœuds 45 et 46 n'existent pas | **44 nœuds**, 45 = false, 46 = false |
| **C5** | le groupe « Bois et carbone » n'existe pas | 14 groupes, sans lui |
| **C6** | les 3 sprites ne sont pas injectés | `[false, false, false]` |
| **C7** | aucun override de sprite pour le foyer | false |
| **C8** | `centrale_charbon` a **déjà** son coût d'origine | `{cable:80, ciment:100, acier:80}` |
| **C9** | poser l'un des 4 est **impossible** sur la base | les 4 refusés |

**9 PASS / 0 KO.** Chaque assertion verte du banc principal a donc un pendant rouge sur la base : la
suite est falsifiable.

---

## 8. Écarts au brief, et pourquoi

| § | ce que le brief dit | ce qui a été fait | raison |
|---|---|---|---|
| §4 | `scierie` t1, `filerie_carbone` t2 | tiers **conservés** + `noTierMult: true` | Le brief demande d'ajuster **pour que le coût payé soit celui du tableau** et déclare les tiers « indicatifs ». Rétrograder en t0 aurait aussi changé l'accent du menu (`TIER_ACCENT`) et le rang du Calculateur (`CALC_BTIER`). `noTierMult` est l'exemption prévue pour exactement ce cas (précédent `separateur_air`). Coût payé mesuré à l'unité près (X1). |
| §4 | `needRoad: true` | posé, mais **inerte** | Aucun code ne lit `needRoad` depuis 10.34 (les 31 occurrences du fichier sont des déclarations). Conservé pour l'homogénéité ; le vrai gating est `roadReachesPort` dans le tick. |
| §5 | « confirmer qu'un port `portCasse` accepte quand même le dépôt » | confirmé **par l'effet**, pas par appel direct | `portCasse` et `deliveryReady` ne sont pas exportés par `window.__heat` : X6 juge sur `techDeliver` + statut + débit du port, ce qui est plus fort qu'un appel isolé. |
| §7 X7 | « contre-preuve île 1 absent » | contre-preuve sur le **grisage + le clic** | Depuis 14.48, un bâtiment `exclusiveIsland` n'est plus masqué hors de son île : il est **grisé** (`.tool-btn.off-island`) et sa vignette ouvre la fiche sans armer l'outil. La contre-preuve porte donc sur la vraie porte. |
| §6 | patcheur idempotent | idempotence par **marqueur**, pas par absence d'ancre | Trois ancres sont un préfixe/suffixe de leur remplacement (insertion en tête ou queue de bloc) : tester leur absence faisait se rejouer le patcheur (+3 705 o mesurés au 2ᵉ passage). |
| §8 | — | groupe de menu **« Bois et carbone »** créé | Le brief demande d'inscrire les quatre dans `TOOLBAR_GROUPS` sans dire où. Les groupes suivent les **familles de matière** et chaque île récente a la sienne ; le foyer va en « Énergie » (c'est une centrale). |

---

## 9. Points ouverts — à arbitrer

### 9.1 L'équilibrage de la chaîne est TRÈS tendu, et c'est arithmétique

Les chiffres du brief donnent, en régime établi :

- la **filerie** demande 4 charbon/s **et 256 kW** ;
- 256 kW = **2 foyers**, chacun buvant 8 charbon/s → **16 charbon/s** rien que pour le courant ;
- total **20 charbon/s**, soit **20 charbonnières**, soit **40 bois/s** ;
- or la bûcheronneuse sort **1 bois/s** (`FELL_RATE`) et il n'y en a **qu'une par île**
  (`maxPerIsland: 1`, décision du lot L2).

**Facteur ~40 entre l'offre de bois et la demande d'une seule filerie.** Ce n'est pas un défaut du
lot — ce sont les nombres du brief, appliqués tels quels — mais c'est le point le plus visible à
arbitrer. Trois leviers possibles, dans l'ordre du moins invasif : (a) monter `FELL_RATE` ou lever
`maxPerIsland` sur la bûcheronneuse ; (b) baisser `power` de la filerie ; (c) monter la sortie du
foyer. **Aucun n'a été touché** — hors périmètre.

### 9.2 Le foyer n'accepte que la CÔTE, donc pas les clairières

`foyer_charbon` a `terrains: ['coast']` — le tableau du brief, et le calque exact de
`centrale_charbon`. Conséquence : **une clairière ouverte par la bûcheronneuse (terrain `land`) ne
peut pas l'accueillir**. L'île a 166 tuiles de côte, donc il y a de la place ; mais le joueur qui
défriche pour se faire de la place ne pourra pas y poser son foyer. Ajouter `land` à sa liste
suffirait — **c'est un choix de design, il n'a pas été fait**.

### 9.3 Le couloir de départ : 5 tuiles, pas 6

Le brief annonce « 6 tuiles routables en file d'une seule tuile (mesuré) ». **Mesure refaite ici** :
la composante joignable depuis le port compte **5 tuiles**, toutes en file d'une seule tuile, et
touche **5 lisières de forêt**. Topologie de l'île au départ : **water 1992 · coast 166 ·
forest 146 · land 0** (aucune tuile de terre : toute la place se gagne à la hache). L'écart d'une
tuile vient probablement de la façon de compter la tuile du port elle-même. Sans conséquence : X9
montre que le couloir suffit à amorcer la boucle.

### 9.4 Le foyer n'a pas d'art

Repli assumé sur `bat_centrale_charbon` (cf. §4.3). À remplacer dès qu'un PNG `bat_foyer_charbon`
est déposé dans le pack — **aucune retouche de code ne sera nécessaire**.

### 9.5 `exclusiveIsland` n'est pas lu par `tryPlace` — constat PRÉEXISTANT

Vérifié : poser l'un des quatre sur l'île 1 **par appel direct à `tryPlace`** réussit. Ce n'est pas
introduit par ce lot : la règle est établie depuis le lot L7 (16.0) — la porte est `selectTool`, pas
`tryPlace`. **X7 vérifie la vraie porte** (vignette grisée, clic qui n'arme aucun outil) et **compare
aux deux bâtiments du lot L2, qui se comportent à l'identique**.

---

## 10. Hors périmètre — non touché

`centrale_charbon` (définition **inchangée**, vérifiée au diff **et** par X12), `DIR_ART_IDS`, la
bûcheronneuse et son `feller`, la foreuse et son `driller`, `NO_COAST`, `SAVE_VERSION`,
`INITIAL_RESOURCES`, `ISLAND_KICKSTART`, la CI, et **tout ce qui est agricole** : pas de ferme, pas de
culture, pas de zone, pas de péremption, pas de λ, pas de port sortant, pas d'élastomère.

**Le pack de sprites contient 15 PNG ; le lot en prend 3.** Cinq des douze autres
(`bat_bucheronneuse_{n,s,e,o}`, `bat_carriere_rustique`) étaient **déjà injectés par le lot L2** ;
les **sept derniers** (`bat_ferme`, `bat_pont`, `bat_presse_lamelle`, `bat_raffinerie_antique`,
`bat_usine_elastomere`, `bat_usine_fibre_carbone`, `bat_usine_polymere_premium`) **ne sont pas
injectés** — ils désignent des bâtiments qui n'existent pas, ce serait du poids mort dans un
fichier unique de 3,8 Mo.

---

## 11. Pièges de banc payés — pour le prochain

Aucun n'était un défaut du patch ; tous ont d'abord ressemblé à un rouge du lot.

1. **La file de popups « Recherche terminée » avale tous les taps.** `goto8()` confirme 43 nœuds
   d'un coup → une **file** de `.rd-popup`. Cliquer `.research-backdrop` **ne suffit pas** :
   Playwright vise le CENTRE de l'élément, or le popup couvre ce centre — le clic atterrit sur le
   popup. Il faut fermer par **son bouton `.rd-btn`**, et **boucler**. Diagnostiqué en lisant
   `elementFromPoint` au centre du canvas (`rd-popup`). Sans ça, X9 mesurait du néant pendant
   quatre passes.
2. **La boucle rAF du jeu tique pendant les `await`.** `ticks(page, 20)` force 20 ticks, le jeu en
   ajoute ~1/s : mesure sortie à **21/20 = ×1,05**. Remède : `game.catchingUp = true` (le seul gel
   propre — `frame` sort avant le tick ET avant le dessin), relâché après la mesure.
3. **Un générateur sans câble adjacent est COUPÉ** (motif `wire`, règle 13.39). Sans câblage, le
   foyer affichait `gross = 0` et paraissait inerte.
4. **`pwrAvg` est un duty-cycle lissé, et il ne rejoint 1 qu'ASYMPTOTIQUEMENT**
   (`pwrAvg += 0,12 × (x − pwrAvg)`). Mesurer trop tôt donnait 0,82 planche/s (défaut de recette
   supposé) ; **40 ticks laissaient encore 0,9977**, soit exactement l'erreur résiduelle
   `0,88^41` — un rouge à 2 × 10⁻³ sur une tolérance à 10⁻⁶, pour une recette pourtant juste.
   **120 ticks** ramènent l'erreur à ~10⁻⁷.
5. **Le premier tick tourne avant que la coupure électrique soit établie** : une scierie sans courant
   sort **1** planche puis s'arrête. On mesure la **croissance**, pas le stock absolu.
6. **Un blob de route mange toute la place** — surtout la CÔTE, dont le foyer a besoin. Le montage
   **pose les bâtiments d'abord** (sur les tuiles les plus ÉLOIGNÉES du port, ordre BFS inverse),
   **puis** trace une route fine par bâtiment. Poser sur les tuiles proches supprimait les amorces
   du tracé (« pas de chemin de route »).
7. **Une machine collée au port n'est jamais reconnue reliée** (`roadReachesPort` veut un *réseau*
   route qui touche le port — défaut préexistant, memo 14.78). Le banc s'est diagnostiqué tout seul :
   `machine(21,14) … actif=false motif=road`. Toute pose de machine impose donc **profondeur ≥ 1**.
8. **`portCasse`, `deliveryReady`, `tryFell` et `roadReachesPort` ne sont pas exportés** par
   `window.__heat` : on juge sur l'effet, et la coupe se lance par le **geste réel** (tap franc sur
   la tuile de forêt).
9. **Le patcheur n'est idempotent que sur un MARQUEUR** quand l'ancre est un préfixe/suffixe du
   remplacement (cf. §3).
10. **Les backticks dans un commentaire à l'intérieur d'un template literal** ferment le littéral :
    `node --check` du banc l'attrape, mais seulement si on le lance.
11. **L'électricité circule PAR COMPOSANTE CÂBLE, et les routes du montage DÉCOUPENT le blob de
    câble.** Symptôme : `gross = 384 kW` sur l'île et la filerie coupée `motif = power` quand même —
    le foyer était seul dans sa composante. Un simple « câbler toute tuile libre » ne répare rien :
    un câble ne traverse pas une route. Le remède est le mécanisme prévu par le jeu, la **jonction
    route/câble**, qui laisse le câble passer **perpendiculairement** à la route (règle 13.18) ;
    le banc n'en pose que ce qu'il faut (**1 à 2**), leur coût **doublant** à chaque exemplaire.
12. **Le tracé de route peut manger LES DEUX voisines libres d'un bâtiment**, qui ressort alors
    `motif = wire` sans aucun câble adjacent. Le montage **réserve la voisine du câble AVANT** de
    tracer, et la route **contourne** cette tuile (avec repli si aucun chemin ne subsiste).
13. **Le gel par `catchingUp` est ANNULÉ au bout de ~4 s** par la soupape anti-blocage du rAF
    (14.13). Pour une mesure longue ou un rechargement, il faut **fermer la page**, pas compter sur
    le gel.
14. **Le rechargement rejoue le RATTRAPAGE HORS-LIGNE** sur le temps écoulé depuis `lastActiveTs` :
    la chaîne tourne pendant ce rattrapage et fabrique une planche de plus (55 → 56). Ce n'est pas
    un défaut de persistance, c'est le jeu qui fait son travail — mais l'égalité stricte est
    intestable tant qu'un intrant reste au port. Le banc **met la chaîne à sec** avant de sauvegarder.
