# RAPPORT — LOT L2 · amorçage de l'île 7 (id interne 8)

**Brief** : `BRIEFL2ile7amorcage.md` · **Base** : `main` après la fusion de la PR #416 (build 438 / Alpha 20.5)
**Livré** : **build 439 / Alpha 20.6** · `SAVE_VERSION` **INCHANGÉ (31)**
**Branche** : `claude/file-7-a52mbd` — **PR ouverte, non fusionnée** (le merge appartient à Ethan).

> **Convention de nommage** — dans tout ce rapport, « île 8 » désigne l'**id interne**. Le joueur, lui,
> lit **« Île 7 »** : `islandLabel(8)` renvoie `'Île 7'` (l'id 7 est le souterrain, affiché « Île 6 S »,
> donc aucune « Île 7 » n'a jamais été montrée). Le libellé « Île 8 » n'apparaît nulle part à l'écran.

> Le joueur débarque, coupe du bois, pose une carrière, relie le tout au port. **Rien d'autre.**
> Aucune recette de transformation n'a été écrite : ni scierie, ni charbonnière, ni ferme, ni
> culture, ni péremption, ni port sortant.

---

## 1. Versions produites

| | valeur |
|---|---|
| `GAME_BUILD` | **439** (438 → 439) |
| `GAME_VERSION` | **`Alpha 20.6`** |
| `SAVE_VERSION` | **31, inchangé** — aucun champ de partie créé (voir §5) |
| `GAME_NOTES` | « Sur la septieme ile : une bucheronneuse abat la foret, une carriere rustique se paie en bois, et le tout se relie au port. La clairiere devient constructible. » (ASCII pur, aucun `"`) |

**Contrôle de collision de numéro** (le build 400 avait été consommé deux fois entre sessions) :
`GAME_BUILD` relevé sur **toutes les branches distantes**, pas seulement `main` — max = 438, donc 439 libre ;
re-vérifié juste avant le push.

Une ligne a été **ajoutée** au bloc de commentaire cumulatif au-dessus de `GAME_BUILD` ; aucune ligne
antérieure n'a été effacée.

---

## 2. Architecture : le monolithe n'a pas été édité

Tout passe par la chaîne build-S :

```
src/index.src.html   (26 ancres)  ─┐
src/sprites-inline.js (5 sprites) ─┴─> node tools/build.js ─> Archipel_industry_alpha-7.html
```

`.build-stamp` re-tamponné : `d55b7653d124da81e076f38f87e35602645f6f098fef65d2971b7a33f0a8cfb9`.

### Deltas d'octets — la somme tombe juste

| fichier | base 438 | build 439 | delta |
|---|---:|---:|---:|
| `src/index.src.html` | 2 450 192 | 2 480 934 | **+30 742** |
| `src/sprites-inline.js` | 1 417 235 | 1 423 316 | **+6 081** |
| **`Archipel_industry_alpha-7.html`** | 3 867 476 | 3 904 299 | **+36 823** |

`30 742 + 6 081 = 36 823` **exactement** : la génération est une concaténation fidèle, elle n'a rien
ajouté ni perdu en route.

### SHA-256 des 7 blocs `<script>` — 4 blocs sur 7 sont intacts à l'octet

| bloc | base 438 | build 439 | Δ car. |
|---|---|---|---:|
| 1 | `a50c1c4e…` | `a50c1c4e…` | 0 |
| 2 | `69ca1cfd…` | `69ca1cfd…` | 0 |
| 3 | `d949f1c3…` | `d949f1c3…` | 0 |
| 4 | `35f4f974…` | `35f4f974…` | 0 |
| 5 (sprites) | `9b0ac8ea…` | `ed1e8d8f…` | +6 071 |
| 6 (LOCALES) | `ca240cfd…` | `abdbe3c6…` | +4 859 |
| 7 (jeu) | `b8a8042d…` | `456e905a…` | +24 981 |

Somme des deltas caractères : **+35 911**, pour **+36 823 octets** → **912 octets** de différence, qui
sont les caractères UTF-8 multi-octets des commentaires et libellés ajoutés. Cohérent.

---

## 3. Les 26 ancres, et leur compte vérifié

Patcheur idempotent, `count == 1` **exigé avant chaque écriture**. Après application, chaque
remplacement est présent **exactement une fois** dans `src/index.src.html` : **26/26** (contrôle rejoué
juste avant le commit).

| # | ancre | ce qu'elle pose |
|---|---|---|
| 1 | `T1a RES_SHORT` | `bois: 'bois'` |
| 2 | `T1b RES_TIER` | `bois: 't0'`, **en fin de bloc t0** |
| 3 | `T1c CARRIER_BY_RES` | `bois: 'road'` |
| 4 | `T4a FELL_TIME` | `FELL_TIME = 60`, `FELL_RATE = 1` |
| 5 | `T2/T3 defs` | les 2 bâtiments |
| 6 | `T2b buildingSpriteKey` | `DIR_ART_IDS` (généralisation) |
| 7 | `T5a TOOLBAR_GROUPS` | les 2 vignettes dans « Extraction » |
| 8 | `T5b nœud 44 unlocks` | `buildings: [bucheronneuse, carriere_rustique]` |
| 9 | `T4b fellList decl` | la file de coupe du tick |
| 10 | `T4c effOutputs` | la sortie DYNAMIQUE `{ bois: FELL_RATE }` |
| 11 | `T4d fellList push` | **après** le `continue` de déconnexion |
| 12 | `T4e avancement` | la boucle de progression + la clairière |
| 13 | `T4f fellNotify` | toast + SFX + `scheduleSave` dans `frame` |
| 14 | `T4g serialize` | `pl.fl` / `pl.fd` |
| 15 | `T4h loadSave` | restauration |
| 16 | `T4i tryFell` | la garde de réservation + le sens |
| 17 | `T4j handleTap` | la branche « taper une forêt » |
| 18-19 | `T4k` / `T4l` | câblage de la prop `onFell` |
| 20 | `T2c fiche feller` | le pavé directionnel de la fiche |
| 21-23 | `T2d` / `T2e` / `T2f` | débord d'une demi-case, flèche, barre de travaux |
| 24 | `T5c i18n L2` | l'IIFE d'augmentation 4 langues |
| 25-26 | bump · `GAME_NOTES` | version |

### Les 5 sprites, comparés **octet à octet** au pack

Le zip livré contient **15 PNG + 1 MANIFEST**. Cinq seulement sont dans le périmètre ; les dix autres
(scierie, charbonnière, ferme, pont, usines…) **n'ont pas été injectés** — ce serait du poids mort.

| clé | dimensions | octets | sha256 (16) | identique au pack |
|---|---|---:|---|---|
| `bat_bucheronneuse_n` | 32×48 | 801 | `8913fed44c13723a` | ✅ |
| `bat_bucheronneuse_s` | 32×48 | 820 | `c4010755bdd7ba64` | ✅ |
| `bat_bucheronneuse_o` | 48×32 | 729 | `410d32fa546b2c5d` | ✅ |
| `bat_bucheronneuse_e` | 48×32 | 712 | `674be73494ee17a2` | ✅ |
| `bat_carriere_rustique` | 32×32 | 636 | `af5deaf49f0703f7` | ✅ |

Chaque clé apparaît **une seule fois** dans `src/sprites-inline.js` (aucun doublon d'assignation).
Les 32×48 / 48×32 sont voulus : la bûcheronneuse **déborde d'une demi-case** dans le sens où elle
abat, exactement comme la foreuse (mécanisme 14.94, ici généralisé).

---

## 4. Compilation et gardes de la CI

`node --check` sur les **7 blocs** des **3 variantes réellement dérivées par `android.yml`** :

| variante | blocs | résultat |
|---|---:|---|
| `game-public.html` | 7 | **7/7** |
| `game-dev.html` (`DEV_BUILD = true`) | 7 | **7/7** |
| `game-store.html` (les 2 `sed` du workflow) | 7 | **7/7** |

⚠ **Le compte de blocs est fait AVANT la boucle et la boucle refuse de conclure s'il ne vaut pas 7**
(piège 19.7 : une extraction interrompue annonce « 7/7 » sur un dossier qui n'en contient qu'un).

Gardes de contenu du workflow, rejouées avec **les commandes littérales d'`android.yml`** :

| garde | attendu | mesuré |
|---|---|---|
| `grep -c 'ko-fi' game-public.html` | 1 | **1** |
| `grep -c 'ko-fi' game-store.html` | 0 | **0** |
| `grep -c 'const SELF_UPDATE = true;' game-store.html` | 0 | **0** |
| `grep -q "^const SELF_UPDATE = true;$"` (entrée) | présent | **présent** |
| `grep -q "^const SUPPORT_URL = 'https://ko-fi.com/freredoc';$"` (entrée) | présent | **présent** |
| `grep -oP 'const GAME_NOTES = "\K[^"]*'` | non vide, sans `"` | **OK** |
| emoji 🏭 | 5 | **5** |

⚠ **ERREUR À MOI, SIGNALÉE PARCE QU'ELLE S'EST PRODUITE DEUX FOIS** : ma première simulation du `sed`
magasin, écrite « de mémoire », rendait `ko-fi = 1` côté magasin — c'est-à-dire un **faux rouge** sur
une garde bloquante de la CI. Le motif réel du workflow est
`s|^const SUPPORT_URL = 'https://ko-fi.com/freredoc';$|const SUPPORT_URL = '';|` ; le mien portait un
suffixe `; // DEV_BUILD` inexistant. **Règle : rejouer les commandes du workflow À LA LETTRE, jamais
une paraphrase.** (Le même piège avait déjà coûté un faux positif au lot L1.)

---

## 5. Ce qui a été construit, et pourquoi ainsi

### 5.1 La ressource `bois`

Trois tables, trois raisons distinctes :

* **`RES_SHORT`** — le libellé. Traduit dans les 4 langues (`bois` / `wood` / `madera` / `Holz`).
* **`RES_TIER`** — `t0`, **et sa place dans la table EST son rang d'affichage** (`RES_ORDER_RANK` =
  ordre de déclaration). Posée **en fin de bloc t0**, après `uranium`, et non à côté de `pierre` :
  l'ordre interne d'un tier suit la progression du jeu, or le bois se rencontre au bout du
  Collisionneur. En tête de tier, il passerait pour une ressource de départ. Mesuré : rang bois = 8,
  uranium = 7, `lingot_fer` (premier t1) = 9.
* **`CARRIER_BY_RES`** — `road`. `carrierOf` retombe déjà sur `'road'` par défaut, **mais c'est
  l'entrée explicite qui inscrit la ressource dans `TRADE_RESOURCES`** (construit par
  `Object.keys(CARRIER_BY_RES)`). Sans elle, le bois serait hors du commerce maritime **à jamais**,
  y compris le jour où l'île 8 recevra une liaison. Aucun effet visible aujourd'hui : l'île 8 n'a pas
  de liaison et la liste du Port filtre en plus sur `unlockedResourceSet`.

### 5.2 La bûcheronneuse

`feller: true`, `maxPerIsland: 1`, `noUpgrade: true`, `cost: {}`, `power: 0`, `exclusiveIsland: 8`,
`terrains: ['land', 'coast']`.

* **Gratuite (D1)** — c'est la première machine d'une île sans économie. Mesuré (W3) : le port n'est
  débité de **rien** (les 4 lignes du port de l'île 8 sont identiques avant et après la pose).
* **Une par île (D2)** — le garde `maxPerIsland` de `tryPlace` couvre **aussi la voie Copier**, qui
  ne fait qu'armer l'outil et repasse par `tryPlace`. Rien à ajouter : W4 mesure les deux chemins.
* **Elle ne disparaît PAS (D3)** — à l'inverse de la foreuse, consommée par son opération. L'île 8
  compte **146 tuiles de forêt** (mesuré) : la faire disparaître à chaque arbre imposerait 146 reposes.
* **`noUpgrade`** — elle n'a ni intrant ni consommation ; améliorer ne ferait que doubler zéro.

### 5.3 La carrière rustique

`cost: { bois: 150 }`, `outputs: { pierre: 1 }`, `power: 0`, `tier: 't0'`, `exclusiveIsland: 8`,
`terrains: ['land', 'resource', 'coast']`.

⚠ **PIÈGE ÉVITÉ, ET IL AURAIT ÉTÉ FATAL** : la carrière de surface est dans `NO_COAST` (qui lui
retire `coast`). Recopier ce réglage « par symétrie de nom » aurait rendu la carrière rustique
**INPOSABLE PARTOUT** : l'île 8 vierge ne contient **aucune tuile `land`** — recensement mesuré :
`water 1992 · coast 166 · forest 146`. Les seules tuiles `land` de l'île 8 sont les **clairières**
que la bûcheronneuse crée. Elle n'a donc **pas** été ajoutée à `NO_COAST`, et `coast` est dans ses
terrains.

`tier: 't0'` : elle échappe au `TIER_COST_MULT`. Mesuré (W12) : `cost.bois === 150` **exactement**,
refusée à 149, acceptée à 150, port ramené à 0.

### 5.4 La coupe

`FELL_TIME = 60` (1 min par tuile), `FELL_RATE = 1` (1 bois/s → **60 bois par arbre**).
Calibré sur le seul débouché du lot : la carrière à 150 bois = **2,5 arbres**, soit ~2 min 30 pour la
première carrière. Les 146 tuiles portent 8 760 bois au total, très loin d'être limitant.

**Le bois est gaté par le réseau, exactement comme n'importe quelle production.** La sortie est
**dynamique** (`if (b.feller && bld.felling) effOutputs = { bois: FELL_RATE };`) et l'inscription dans
la file de coupe (`fellList.push`) est placée **APRÈS** le `continue` de déconnexion. Conséquence
mesurée (W11) : route coupée → **0 bois au port ET la coupe n'avance même pas** (`rem` reste à 60,
motif `road`) ; réseau rétabli → le bois arrive. C'est le comportement voulu, et il est falsifiable.

**Aucun `rebuildNetworks` à la fin d'une coupe** : la clairière est du terrain, elle ne crée ni ne
casse aucun réseau. Le tick n'est donc pas alourdi.

### 5.5 Le site de tap retenu, et son motif

`tryFell` est appelé depuis **`handleTap`**, dans la branche **mode Sélection uniquement** :

```js
if (!toolActive && t && t.terrain === 'forest') { tryFell(tr, tc); return; }
```

* **Pourquoi pas `tryExtend`** — le chemin du remblai/forage ne peut pas atteindre une tuile `forest` :
  il est gardé sur de l'eau (surface) ou de la roche (île 7). Le brancher là aurait demandé d'élargir
  une garde qui protège deux autres mécaniques.
* **Pourquoi `!toolActive`** — les modes `place` / `upgrade` / `demolish` gardent leur sens ; taper une
  forêt avec la pioche de démolition ne doit pas lancer une coupe. La branche couvre en revanche
  **Copier** (qui passe par le mode Sélection), sans code supplémentaire.

**Le sens de la machine est DÉDUIT, jamais réglé à l'aveugle** : `fellDir` vient du pas forêt → machine,
avec **la formule EXACTE de `tryDrill`** (`dr === -1 ? 1 : dr === 1 ? 0 : dc === -1 ? 3 : 2`, indices
`DIRS4 = [N, S, O, E]`). Mesuré aux 4 orientations (W5), et le sprite dessiné suit :
`bat_bucheronneuse_n / _s / _o / _e`, relevé par espion sur `drawImage` + reverse-map dataURL → clé.

Le pavé directionnel de la fiche **lance la coupe** au lieu de seulement orienter : sur une machine
qui n'a qu'une cible possible par face, un réglage séparé du déclenchement n'apporterait rien.

### 5.6 La persistance : zéro champ de partie, zéro migration

* **La clairière** est persistée par **`terrainMods`**, qui sérialise déjà toute tuile où
  `terrain !== baseTerrain`. Aucun drapeau parallèle. Mesuré (W9) sur un **rechargement RÉEL** :
  la clairière tient (`terrain = land`, `baseTerrain = forest`), la machine est toujours là, le bois
  aussi.
* **La coupe en cours** tient dans `pl.fl = {r, c, rem, tot}` et `pl.fd` (le sens) — deux champs
  **additifs** de la sérialisation des placements, comme `pl.dg`/`pl.dd` de la foreuse.
  `SAVE_VERSION` reste **31** : une sauvegarde antérieure se charge sans migration, et une sauvegarde
  439 ouverte par un build antérieur ignore simplement ces deux clés.
* Mesuré (W10) : coupe à mi-course (`rem = 35`), sauvegarde forcée, **rechargement réel** → elle
  **reprend où elle en était** (cible et sens conservés, `rem` restauré et non remis à 60), puis se
  termine.

### 5.7 `DIR_ART_IDS` — la généralisation, et sa preuve de non-nuisance

`buildingSpriteKey` savait déjà choisir `bat_foreuse_<dir>`, mais avec l'id de la foreuse **en dur**.
Une table `DIR_ART_IDS = { foreuse: 'bat_foreuse', bucheronneuse: 'bat_bucheronneuse' }` remplace le
cas particulier. **Les clés de la foreuse sont inchangées à l'octet** : mesuré des deux côtés,
`buildingSpriteKey('foreuse', 0, 1) === 'bat_foreuse_s'` sur la base **comme** sur le patch.

---

## 6. Le banc : W1 → W15, avec le montage réellement exécuté

Chromium 1194 (`/opt/pw-browsers/chromium-1194`, `playwright install` proscrit), servi en **HTTP sur
127.0.0.1**, jamais `file://`. `localStorage` purgé par `addInitScript` derrière un drapeau
`sessionStorage` (piège 14.59 : `addInitScript` rejoue à **chaque** navigation, rechargement compris —
sans le drapeau, tout test de rechargement repart sur une partie neuve).

**Résultat : 46 PASS / 0 KO, suite rejouée TROIS fois d'affilée, verdicts identiques (aucun flottement).**

### Le montage : mesuré, pas supposé

L'île 8 vierge a été **relevée** avant d'écrire le moindre test :

* recensement : **`water 1992 · coast 166 · forest 146`** — **aucune tuile `land`** ;
* port en **(21, 13)** ;
* **la composante route joignable au port ne fait que CINQ tuiles**, en file :
  `(20,13) → (19,13) → (18,13) → (17,13)`, plus `(21,14)` isolée à côté du port ;
* chacune de ces cinq tuiles ne touche **qu'UNE** tuile de forêt.

Deux conséquences qui ont réécrit le banc :

1. **La machine ne peut pas être collée au port.** `roadReachesPort` exige un *réseau route* qui touche
   le port ; un bâtiment posé contre lui n'est jamais reconnu comme relié (défaut préexistant, mémo
   14.78). Le montage impose donc **au moins une tuile de route** entre la machine et le port
   (`prof >= 1`). Ma première version cherchait la tuile *la plus proche* du port — elle tombait
   pile sur ce cas, posait **zéro route**, et **six tests échouaient pour cette unique raison**.
2. **W7 (« machine occupée ») ne peut pas se jouer sur ce montage** : aucune des cinq tuiles reliées
   n'a deux forêts voisines. Comme la garde de réservation de `tryFell` ne dépend pas du réseau, W7 a
   son montage propre, sur une tuile à deux forêts voisines, où qu'elle soit.

### Résultats

| test | verdict | mesure |
|---|---|---|
| W1 démarrage | PASS | canvas peint, **0 erreur dure** |
| W2 menu île 8 | PASS | 95 vignettes, les 2 posables |
| W2 contre-épreuve île 1 | PASS | **0 posable**, 2 grisées « autres îles » |
| W3 pose | PASS | machine (19,13), forêt (19,14), **1 route** |
| W3 gratuité | PASS | port **strictement identique** avant/après |
| W4 seconde machine (barre) | PASS | `tryPlace = false` |
| W4 seconde machine (Copier) | PASS | refusée, **1 seule** sur l'île |
| W5 ×4 sens + art | PASS | `fellDir` 1/0/3/2 et `bat_bucheronneuse_s/n/e/o` **dessinés** |
| W6 forêt non adjacente | PASS | refus + toast « Aucune bûcheronneuse adjacente », 0 coupe |
| W7 machine occupée | PASS | 2ᵉ tap refusé, **cible inchangée**, toast « déjà en train de couper » |
| W8 clairière | PASS | `forest → land` (base `forest`) |
| W8 bois | PASS | **0 → 60** en 62 ticks |
| W8 la machine reste (D3) | PASS | machine présente, `felling` effacé |
| W9 rechargement | PASS | clairière **tient**, machine et bois conservés |
| W10 coupe à mi-course | PASS | `rem 35 → 34`, cible (19,14) et sens 3 conservés, **jamais remis à 60** |
| W10 fin après rechargement | PASS | `terrain = land` |
| W11 route coupée | PASS | **0 bois**, motif `road`, `rem` figé à 60 |
| W11 contre-épreuve | PASS | route rétablie → **le bois arrive** (30 en 30 ticks) |
| W12 coût | PASS | `cost.bois === 150` **exactement** |
| W12 149 / 150 | PASS | refusée / acceptée, port ramené à 0 |
| W12 production | PASS | pierre **0 → 12** en 12 ticks, `regime = 1`, motif `null` |
| W13 démolition en pleine coupe | PASS | machine partie, **aucune réservation fantôme** |
| W13 tuile re-visable | PASS | repose + recoupe OK |
| W14 foreuse (×4) | PASS | voir §7 |
| W15 `bois` ×4 langues | PASS | `bois` / `wood` / `madera` / `Holz` |
| W15 rang d'affichage | PASS | bois = 8, uranium = 7, `lingot_fer` = 9 |
| W15 inventaire | PASS | l'entrée apparaît |
| W15 couverture i18n | PASS | **4/4** langues × (res + les 2 bâtiments), par `hasOwnProperty` |

### W-extra — le chemin du joueur, joué en entier

Le brief décrit un parcours ; il a été **joué**, sur une île 8 vierge, coupes par **taps réels** :

| étape | mesure |
|---|---|
| 1ʳᵉ coupe | **60 bois** — et la machine se retrouve avec **0 forêt voisine** |
| déplacement sur la clairière | repose acceptée (gratuite : D1), **3 forêts voisines** |
| 2 coupes de plus | **180 bois** → la carrière (150) devient payable, reste 30 |
| carrière reliée | **pierre 0 → 12** en 12 ticks |

**C'est le lot en une ligne, et il tient.** Au passage, ce test montre pourquoi D1 (machine gratuite)
et D3 (elle ne disparaît pas) sont les deux bonnes décisions : la première machine ne peut abattre
**qu'un seul arbre** avant d'être bloquée, et le joueur doit la **déplacer** sur sa propre clairière
pour continuer. Gratuite, ce déplacement est indolore ; payante, il serait punitif.

---

## 7. Non-régression de la foreuse — par comparaison, pas par affirmation

La foreuse est le voisin direct de tout ce que ce lot touche : `DIR_ART_IDS`, la file `drillList`,
la branche `effOutputs`. Le **même banc** a donc été rejoué sur la **base 438**, avec le même montage
(élévateur réparé, route port 6 → cage, route foreuse → cage) :

| mesure | base 438 | build 439 |
|---|---|---|
| foreuse posable | (9,10) | (9,10) |
| `buildingSpriteKey('foreuse', 0, 1)` | `bat_foreuse_s` | `bat_foreuse_s` |
| forage lancé, sens | `drilling`, dir **0** | `drilling`, dir **0** |
| `rem` au lancement | **300** = `DRILL_TIME` | **300** |
| `rem` après 20 ticks | **299,5** | **299,5** |
| `regime` | **0,5** | **0,5** |
| `drillStall` | *(vierge)* | *(vierge)* |
| pierre remontée / 20 ticks | **16** | **16** |

**Identiques, chiffre pour chiffre.**

### Contre-épreuve : la suite est falsifiable

Rejouée sur la base 438 — **11 PASS / 0 KO**, chaque affirmation du lot y **tombe** :

| affirmation | sur la base 438 |
|---|---|
| les 2 bâtiments existent | **non** (`BUILDINGS` ne les connaît pas) |
| les 5 sprites existent | **0/5** |
| `bois` dans `RES_SHORT` / `RES_TIER` / `CARRIER_BY_RES` | **`undefined` × 3** |
| clé i18n `bois` | **0/4** langues |
| le nœud 44 débloque des bâtiments | **`unlocks.buildings` absent** |
| taper une forêt lance une coupe | **non** — terrain inchangé, 0 bois |
| l'île 8 et sa forêt existent | **oui** (146 tuiles — c'est le socle L1, déjà fusionné) |

La dernière ligne est celle qui compte : elle montre que la contre-épreuve mesure bien **le lot L2**,
et pas l'absence de l'île.

---

## 8. Ce que l'absence dans `TOOLBAR_GROUPS` produisait

Un bâtiment débloqué par la recherche mais **absent de `TOOLBAR_GROUPS` n'a aucune vignette** : il
n'existe que par densification, et sa fiche détaillée (appui long au menu) est inatteignable. Les deux
bâtiments de L2 n'ayant **aucun palier**, l'oubli les aurait rendus **strictement inaccessibles** —
débloqués, listés dans `unlocks`, et introuvables. C'est exactement l'anomalie signalée au mémo 14.46
puis fermée au lot 14.99 (les deux derniers bâtiments de `TIER_STEP` hors barre d'outils).

Ils sont donc dans le groupe **`extraction`** — la catégorie qui contient déjà mines et carrières.
Mesuré (W2) : **95 vignettes** sur l'île 8, les deux présentes et **non grisées** ; sur l'île 1,
**0 posable** et 2 grisées « bâtiments des autres îles » (comportement normal de tout
`exclusiveIsland`, pas une régression).

---

## 9. Écarts au brief, et leur raison

1. **Le montage du banc n'est pas celui que le brief imaginait.** Le brief suppose qu'on pose la
   machine « près du port ». La mesure interdit ce montage : la machine collée au port n'est jamais
   reconnue reliée (défaut préexistant). Le banc impose une route intermédiaire. *Sans cet écart, six
   tests échouaient — et pour la mauvaise raison.*
2. **W7 a un montage dédié**, parce qu'aucune tuile reliée au port n'a deux forêts voisines (mesuré).
   La garde testée ne dépend pas du réseau ; le montage est donc légitime.
3. **W14 ne prouve pas que la foreuse « creuse jusqu'au bout ».** Elle ne progresse qu'avec du
   **courant** souterrain (2,05 MW au 1er cercle) : monter une centrale sur l'île 7 sort du périmètre.
   Ce qui est prouvé est plus fort et plus honnête : **les mêmes nombres des deux côtés** (§7).
4. **Un W-extra a été ajouté** (le chemin du joueur de bout en bout). Non demandé, mais c'est la seule
   preuve directe de la phrase de périmètre du brief.
5. **`bois` n'a PAS été ajouté à `unlockedResourceSet`.** Voir §10.

---

## 10. Points ouverts, à trancher par Ethan

1. **L'île 8 est TRÈS étroite au départ, et c'est mesuré.** Cinq tuiles jouables reliées au port, en
   file d'une seule tuile de large. Poser la bûcheronneuse **coupe cette file en deux** (une route ne
   traverse jamais un bâtiment — règle 10.59, seuls le câble et le tuyau font pont). Conséquence :
   **un seul bâtiment peut être relié à la fois** tant que le joueur n'a pas défriché, et la première
   machine ne peut abattre **qu'un seul arbre** avant d'être bloquée. Le jeu s'en sort (W-extra le
   prouve : déplacer la machine sur sa clairière lui ouvre 3 forêts), mais **c'est serré**, et c'est
   un arbitrage de rythme, pas un défaut. À élargir en L3 si le ressenti est trop rude.
2. **`bois` n'apparaît pas dans la liste de transit du Port.** `unlockedResourceSet` ne lit que les
   `outputs` **statiques** ; la sortie de la bûcheronneuse est dynamique. Volontairement laissé tel
   quel : **l'île 8 n'a aucune liaison maritime**, la ligne serait présente et morte. Le jour où l'île
   reçoit sa liaison, il faudra ajouter `bois` à `unlockedResourceSet` (une ligne) — sinon la
   ressource restera invisible au Port. `CARRIER_BY_RES.bois` est déjà posé pour ça.
3. **La carrière rustique n'a pas de palier ni d'amélioration.** 1 pierre/s, point. C'est cohérent
   avec le périmètre (« pas de transformation »), mais ça deviendra le goulot dès L3.
4. **10 des 15 sprites du pack ne sont pas injectés** (scierie, charbonnière, ferme, pont, presse,
   filerie, raffinerie antique, usines élastomère / fibre / polymère premium). Ils attendent leur lot.
5. **Non couvert** : aucun rendu sur appareil. La lisibilité du débord d'une demi-case de la
   bûcheronneuse, la flèche de visée et la barre de travaux verte n'ont été vérifiées qu'en Chromium
   de bureau.
6. **Non relu par un locuteur** : les 3 traductions (`wood` / `madera` / `Holz`) et les libellés des
   deux bâtiments.

---

## 11. Pièges de banc payés, pour la prochaine session

* **`locator.click()` attend l'actionnabilité 30 secondes par défaut.** Un `.tip-ok` recouvert par un
  backdrop faisait **boucler la purge pendant sept minutes**, en silence (le `.catch` avalait le rejet).
  Tous les clics du banc sont désormais **bornés à 1,2 s**, avec repli sur un clic DOM en page.
* **Forger l'état ouvre de NOUVELLES astuces** dont le popup recouvre le canvas : la purge est faite
  **avant chaque essai de tap**, pas seulement au démarrage. Symptôme sinon : `elementFromPoint` rend
  `tip-dismiss` et le tap n'atteint jamais la carte.
* **La classe de l'onglet Démolir est `tab-dem`**, pas `tab-demolish` (`tab('dem', 'tab-dem', …)`).
* **`useGhostGuard` avale le premier clic d'un panneau** : amorcer par un `pointerdown` **dans** le
  panneau, cliquer **une** fois, puis **asserter l'état atteint** et réessayer. Un `el.click()` DOM
  nu ne suffit pas.
* **Le HUD ne se re-rend qu'au bump du tick** : après avoir forgé un stock, il faut faire tourner
  quelques ticks avant de lire l'inventaire, sinon on lit le rendu **précédent**.
* **Le bouton INVENTAIRE est un INTERRUPTEUR** et l'inventaire est **déplié à la création** : le
  basculer à l'aveugle le REFERME. (Les `.inv-item` sont dans le DOM plié comme déplié — mesuré.)
* **La construction souterraine est ÉTALÉE (13.89) et PRÉEMPTE le débit de l'élévateur** : les routes
  qu'on vient de poser sur l'île 7 affament la foreuse. Solder les chantiers avant de mesurer.
* **L'île 7 n'a que 3 tuiles `land` et 9 `coast`** : un balayage limité à `land`/`resource` n'y trouve
  **rien**, alors que `foreuse.terrains` accepte `coast`.
* **Ne jamais paraphraser un `sed` de la CI** — cf. §4.

---

## 12. État final

* `node --check` : **7/7 sur les 3 variantes** (`game-public`, `game-dev`, `game-store`), compte de
  blocs vérifié **avant** la boucle.
* Banc : **46 PASS / 0 KO**, **trois passes identiques**.
* Contre-épreuve base 438 : **11 PASS / 0 KO** — la suite est falsifiable.
* Foreuse : **mêmes nombres des deux côtés**.
* Gardes de contenu de la CI : **toutes vertes**, rejouées avec les commandes littérales du workflow.
* `SAVE_VERSION` **31, inchangé** ; **aucun champ de partie** créé.
* Périmètre respecté : **aucune recette de transformation n'a été écrite.**
