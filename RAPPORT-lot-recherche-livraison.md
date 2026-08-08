# RAPPORT — Lot « Recherche par livraison »

**Brief** : `BRIEFlotrecherchelivraison.md`
**Livré** : `GAME_BUILD = 374` · `GAME_VERSION = 'Alpha 14.91'` · **`SAVE_VERSION` INCHANGÉ (31)**

---

## 0 — Base effective : 373, pas 372

Le brief déclare une base « Alpha 14.89 / GAME_BUILD 372 / 3 303 182 octets ». **La branche était à 373**
(Alpha 14.90, lot « Gisements », 3 310 733 o) au moment de l'exécution — mergé entre la rédaction du
brief et son exécution.

Toutes les ancres ont donc été **re-vérifiées sur la base réelle 373**. Les comptages du brief y sont
retrouvés **à l'identique** (43 nœuds, `{start:1, delivery:10, auto:31, manual:1}`, `manual` = nœud 5,
`const pool = game.port[game.currentIsland] || {};` à `count == 2`) : le lot Gisements n'a touché ni
`TECH_NODES` ni le moteur de recherche. **Aucune adaptation nécessaire.**

| | base 373 | patché 374 | delta |
|---|---|---|---|
| Taille (`os.path.getsize`) | 3 310 733 o | 3 316 626 o | **+5 893 o** |

---

## 1 — Ancres appliquées (toutes vérifiées `count == 1` AVANT écriture)

### 1a — Bloc `TECH_NODES` : transformation **programmatique**, pas d'ancre textuelle

Conformément au §6, les 29 nœuds n'ont **pas** été patchés par 29 ancres (`mode: 'auto',` apparaît
31 fois — aucune n'est unique). Procédé :

1. Ancre de délimitation `const TECH_NODES = [{` — **`count == 1`**.
2. Bloc délimité par **comptage de crochets**, sur le fichier **non filtré**.
3. Découpe en **43 spans** d'objets de profondeur 1 ; chirurgie **à l'intérieur du span** de chaque nœud.
4. Assertions **avant** réécriture (toutes vertes) :
   - exactement **29** nœuds ont changé de mode ;
   - les 14 nœuds intouchés (1, 2, 8, 14, 21, 28, 31, 34, 38, 39, 40, 41, 42, 43) sont **identiques
     octet à octet** ;
   - total **43** ; tout `mode: 'delivery'` a un `delivery` non vide ; toute clé de `delivery` existe
     dans `RES_TIER`.

⚠ **Piège de scanner rencontré et fermé.** Un scanner de crochets conscient des seules *chaînes*
échoue sur ce fichier : les commentaires français contiennent des apostrophes (`n'est`, `d'accès`)
qu'il prend pour des ouvertures de chaîne. Première tentative → bloc de **78 879** caractères et
**41** objets (le scan avait couru jusqu'à `TRADE_LIQUIDS`). Le scanner a été rendu **conscient des
commentaires** `//` et `/* */` → **15 018** caractères, **43** objets, bloc terminé par `];`.
Le filtrage `awk 'length($0)<300'` n'a servi qu'à l'exploration, jamais à l'extraction.

### 1b — Les 6 ancres textuelles

| # | Ancre | `count` avant | Round-trip |
|---|---|---|---|
| A | `function techDeliver(game, id) {` … `const pool = game.port[…]` | 1 | verbatim ✓ |
| B | `function deliveryReady(game, def) {` … `const pool = game.port[…]` | 1 | verbatim ✓ |
| C | `ResearchPanel` — `const port = game.port[currentIsland] || {};` | 1 | verbatim ✓ |
| D | `ResearchPanel` — bloc `isDelivery` / `isManual` / `canDeliver` | 1 | verbatim ✓ |
| E | `ResearchPanel` — rendu de la ligne de livraison | 1 | verbatim ✓ |
| F | CSS `.rp-ci.miss{…}` (ajout de `.rp-island-note`) | 1 | verbatim ✓ |

Plus : `RESEARCH_DELIVERY_FACTOR` (avant `const TECH_NODES = [{`), la boucle d'application du facteur
(ancre `}];\nconst TECH_BY_ID = {};`, `count == 1`), un bloc d'augmentation i18n (ancre `/* 14.54 */`,
`count == 1`), et le bump.

⚠ Le brief signalait que `const pool = game.port[game.currentIsland] || {};` est à `count == 2` :
confirmé. Les deux sites ont été **ancrés sur leur signature de fonction englobante**, jamais sur la
ligne seule.

### 1c — SHA-256 des blocs `<script>`, **ré-extraits du fichier patché**

| Bloc | base (o) | patché (o) | SHA-256 (patché) | |
|---|---|---|---|---|
| 1 | 418 | 418 | `a50c1c4e7f4a304c650c0cfa7e06c4ffdfb0d87510d83738114d4dd5a1641628` | identique |
| 2 | 4 397 | 4 397 | `8fbb22187703339c146b2f82badd8701d199128cd3d9dd8673d213c8616ca541` | identique |
| 3 | 10 751 | 10 751 | `d949f1c3687aedadcedac85261865f29b17cd273997e7f6b2bfc53b2f9d4c4dd` | identique |
| 4 | 131 835 | 131 835 | `35f4f974f4b2bcd44da73963347f8952e341f83909e4498227d4e26b98f66f0d` | identique |
| 5 | 1 111 292 | 1 111 292 | `8f111a1380cf98fca4e48d1fc2aa448199b93cab0be15072f68e7175ba98e426` | identique |
| **6** | 233 205 | **233 641** | `8cf477e74d65a76965694c3fa39904b6f2b46260559d376ac9d30a8e5d9abb75` | **MODIFIÉ** (i18n) |
| **7** | 1 571 033 | **1 576 354** | `50f02f51b25c2d389cd8e36a89d1aee5276ddd3ad24cb849b4fc2130bf169fad` | **MODIFIÉ** (jeu) |

`node --check` : **7 blocs, 7 OK**, éditions **PUBLIQUE et DEV** (`sed` `DEV_BUILD=true` → 7/7).

---

## 2 — La table des 29 nœuds **telle qu'elle figure dans le fichier patché**

Ré-extraite par **évaluation du bloc sous Node** depuis `Archipel_industry_alpha-7.html` — non
recopiée du brief.

| Nœud | Île | Nom | Mode | Livraison | Source |
|---|---|---|---|---|---|
| 3 | 2 | Four Cuivre V1 | `delivery` | 100 `minerai_cuivre` | `reqs` |
| 4 | 2 | Éolienne + Câblerie | `delivery` | 100 `lingot_cuivre` | `reqs` |
| 5 | 2 | Centrale Charbon + Aciérie | `delivery` | 50 `cable` | `reqs` |
| 6 | 2 | Jonctions (3 types) | `delivery` | 100 `acier` | `reqs` |
| 7 | 2 | Upgrades V2 — Extraction | `delivery` | 10000 `acier` + 10000 `lingot_cuivre` | `reqs` |
| 9 | 3 | Usine Polymère | `delivery` | 100 `petrole` | `reqs` |
| 10 | 3 | Bétonnière + Atelier Méca. | `delivery` | 100 `polymere` | `reqs` |
| 11 | 3 | Centrale Diesel + Raffinerie | `delivery` | 100 `beton_arme` + 100 `piece_meca` | `reqs` |
| 12 | 3 | Circuit V1 | `delivery` | 500 `polymere` | **choisi** |
| 13 | 3 | Upgrades V2 — Transformation | `delivery` | 1000 `circuit` | `reqs` |
| 15 | 4 | Broyeur + Distillerie | `delivery` | 100 `minerai_or` | `reqs` |
| 16 | 4 | Fonderie Or + Raffineur Si | `delivery` | 200 `silicium` + 200 `acide` | `reqs` |
| 17 | 4 | Fab. Processeur | `delivery` | 300 `lingot_or` + 40 `silicium_raffine` | `reqs` |
| 18 | 4 | Accumulateur | `delivery` | 10 `processeur` | `reqs` |
| 19 | 4 | Densification Avancée | `delivery` | 50 `processeur` | **choisi** |
| 20 | 4 | Éolienne Offshore + Plateforme | `delivery` | 1000 `processeur` | `reqs` |
| 22 | 5 | Broyeur Uranium | `delivery` | 100 `uranium` | `reqs` |
| 23 | 5 | Centrale Enrichissement | `delivery` | 100 `yellow_cake` | `reqs` |
| 24 | 5 | Centrale Nucléaire + Tour aéroréfrigérante | `delivery` | 64 `combustible_u235` | `reqs` |
| 25 | 5 | Usine Moteur Nucléaire | `delivery` | 1000 `acier_irradie` + 1000 `beton_arme_irradie` + 1000 `cable_irradie` | `reqs` |
| 26 | 5 | Mines V3 + Fours à Arc | `delivery` | 100 `element_moteur_nuc` | `reqs` |
| 27 | 5 | Antenne Amplificatrice | `delivery` | 1000 `element_moteur_nuc` | `reqs` |
| 29 | 6 | Four à Arc Tungstène | `delivery` | 100 `tungstene` | `reqs` |
| 30 | 6 | Machine-Outil | `delivery` | 100 `alliage_tungstene` | `reqs` |
| 32 | 6 | Câble Supraconducteur | `delivery` | 500 `alliage_tungstene` | **choisi** |
| 33 | 6 | Batterie V2 | `delivery` | 1000 `cable_supraconducteur` | `reqs` |
| 35 | **7** | Trouver de l'Hélium | `delivery` | 500 `piece_precision` | **choisi** |
| 36 | 6 | Ordinateur Quantique | `delivery` | 100 `helium4` | `reqs` |
| 37 | 6 | Data Center | `delivery` | 1 `ordinateur_quantique` | `reqs` |

**29 nœuds**, conformes au brief ligne par ligne.

### Comptage des modes

| Mode | base 373 | patché 374 | attendu |
|---|---|---|---|
| `start` | 1 | 1 | 1 ✓ |
| `delivery` | 10 | **39** | 39 ✓ |
| `auto` | 31 | **3** (39, 41, 43) | 3 ✓ |
| `manual` | **1** (nœud 5) | **0** | 0 ✓ |
| **Total** | **43** | **43** | 43 ✓ |

La branche `manual` du code est **conservée** (aucun nœud ne l'utilise, la porte reste ouverte).

---

## 3 — Le facteur global

```js
const RESEARCH_DELIVERY_FACTOR = 1;
```

posé **en tête de `TECH_NODES`**, appliqué **après** la construction du bloc :

```js
for (const _rdn of TECH_NODES) {
  if (_rdn.mode !== 'delivery' || _rdn.island == null || !_rdn.delivery) continue;
  for (const _rdk in _rdn.delivery) _rdn.delivery[_rdk] = Math.round(_rdn.delivery[_rdk] * RESEARCH_DELIVERY_FACTOR);
}
```

**La table reste écrite aux valeurs de base** — aucune quantité multipliée en dur. Passer le seul
chiffre à 2, 5 ou 10 met les 29 livraisons à l'échelle d'un coup.

⚠ **Décision à connaître : le facteur ne s'applique QU'AUX 29 nœuds du lot**, reconnus à la présence
de leur champ `island`. Les 10 livraisons d'accès/réparation d'île antérieures (2, 8, 14, 21, 28, 31,
34, 38, 40, 42) n'en portent pas et gardent leurs quantités d'origine. C'est ce qu'imposent le §1
(« ce qui est déjà une livraison ne change pas ») et le §9 (leur rééquilibrage est hors périmètre) ;
le §8.10 (« toutes les quantités ×10 ») est donc lu comme *toutes celles du lot*. Le champ `island`
sert de marqueur plutôt qu'un drapeau supplémentaire : pas d'état neuf à maintenir, et la propriété
« nœud de ce lot ⇔ nœud contraint à une île » reste vraie par construction.

`Math.round` protège d'un facteur fractionnaire ; à 1 c'est l'identité sur des entiers.

---

## 4 — Le bug bloquant du §3, et ce qu'il touchait vraiment

`techDeliver` et `deliveryReady` lisaient `game.port[game.currentIsland] || {}`. L'île 7 n'ayant
**jamais** de `game.port[7]`, toute livraison depuis le souterrain échouait **en silence**. Les deux
fonctions passent désormais par **`portPool(game, game.currentIsland)`**.

**`portPool` rend bien une RÉFÉRENCE VIVANTE** — vérifié au runtime, pas supposé (test dédié : une
sonde écrite dans l'objet rendu est relue dans `game.port[1]`, et `portPool(game, 7) === game.port[6]`
sans jamais créer `game.port[7]`). C'était le piège le plus grave du lot : une copie aurait rendu la
recherche **gratuite sans le moindre symptôme**. Le test 8.7 relit le stock par un chemin
**indépendant de `portPool`** (`game.port[6]` en direct) pour ne pas se mordre la queue.

⚠ **Le défaut n'était PAS introduit par ce lot : il était déjà là et déjà atteignable.** Contre-épreuve
exécutée sur la base 373 : le nœud **31** (« Réparation de l'Élévateur »), qui était **déjà** en mode
`delivery`, est **impossible à livrer depuis l'île 7** sur la base — `techDeliver` rend `false`,
le stock reste intact. Il était latent parce qu'on livre le 31 depuis l'île 6 (l'île 7 n'est pas encore
accessible), mais le correctif profite **aussi aux 10 livraisons antérieures**.

---

## 5 — Contrainte d'île

- Champ **`island` en dur** sur chacun des 29 nœuds (jamais dérivé de la chaîne de prérequis).
- `techDeliver` **et** `deliveryReady` refusent si `def.island != null && def.island !== game.currentIsland`.
  La même condition dans les deux : sinon la pastille de notification s'allumerait pour une recherche
  non validable ici.
- Les 10 livraisons antérieures n'ont pas de champ `island` → **aucune contrainte**, comportement
  strictement inchangé (vérifié : le nœud 2 se livre encore depuis n'importe quelle île).
- Île 7 : `island: 7` exige d'être **physiquement au souterrain**, alors que le stock puisé est celui
  de l'île 6. C'est voulu, et c'est mesuré dans les deux sens (8.8 réussit, 8.9 refuse).

### Affichage

Le panneau Recherche indique l'île attendue sur tout nœud dont `island` diffère de l'île courante :

- note **« À livrer depuis l'Île N »** (classe `.rp-island-note`, ocre) à côté des pastilles de coût ;
- même texte en `title` du bouton désactivé ;
- **les pastilles de coût montrent le stock de l'ÎLE ATTENDUE**, pas celui de l'île courante. Un
  « 60/100 » vu depuis l'île 5 parle de l'île 2 : le joueur voit sa progression *et* sait où aller.
  Afficher le stock local aurait été un chiffre sans signification. Le bouton, lui, suit exactement la
  règle de `techDeliver`.
- i18n en/es/de du nouveau libellé (bloc d'augmentation dédié).

`islandLabel` est utilisé pour le nom : l'île 7 s'affiche **« Île 6 S »**, conformément à la
convention de 14.24.

---

## 6 — Audit de produisibilité (fait AVANT d'écrire quoi que ce soit)

⚠ **Le §2 du brief affirme que la règle « livraison = condition `produce` » garantit la produisibilité
sur l'île « puisque la condition a été remplie sur place ». C'est FAUX au sens strict** : `techProduced`
est un compteur **GLOBAL** (`game.techTree.produced[res]`), pas par île. La propriété a donc été
**vérifiée pour de bon**, deux fois :

1. **Produisibilité sur le pool de l'île** — pour chaque ressource livrée, existe-t-il un producteur
   posable sur l'île visée (`exclusiveIsland` / `forbiddenIslands`), ou la ressource est-elle
   transitable (`TRADE_RESOURCES` = porteurs `road` + `TRADE_LIQUIDS`) ? → **0 KO sur 35 couples
   (nœud, ressource)**. L'île 7 est traitée comme le pool de l'île 6 dans les deux sens.
2. **Absence de circularité** — pour chaque nœud, au moins un producteur de sa ressource est-il
   débloqué par un nœud **strictement ancêtre** ? → **0 KO**, en excluant le nœud lui-même du calcul.
   L'île de livraison est elle aussi ouverte par un ancêtre (île → nœud : `{1:1, 2:2, 3:8, 4:14, 5:21,
   6:28, 7:31}`).

Vérifications ponctuelles du §5 confirmées : nœud 33 (`presse_uhp`, `exclusiveIsland: 7`) et nœud 36
(`separateur_cryogenique`, `exclusiveIsland: 7`) remontent au port de l'île 6 → livrables depuis
l'île 6 ✓. Nœud 35 (`machine_outil`, `exclusiveIsland: 6`) dépose au port 6, qui **est** le pool de
l'île 7 ✓. `NON_STORABLE` = `eau_froide` seul, `VENTED_RES` = `gaz_echappement` seul : **aucune
ressource du tableau n'est non stockable** ✓. `helium4` est bien dans `PORT_PIPE_RES` ✓.

---

## 7 — Tests

**Total : 79 assertions pour ce lot, 0 KO, suites rejouées 2 fois sans flottement**, plus
**56 assertions de non-régression** et 2 boots.

### 7.1 — Statique (15 assertions, `node`) — `tstatic.js`

| Test | Résultat |
|---|---|
| 8.1 total 43 · modes `{start:1, delivery:39, auto:3}` · `manual` = 0 · les 3 `auto` sont 39/41/43 | **PASS** ×4 |
| 8.2 `delivery == produce` ressource par ressource et quantité par quantité (25 nœuds) | **PASS** (25/25) |
| 8.2bis les 4 nœuds « choisis » (12/19/32/35) portent les valeurs du brief | **PASS** |
| 8.3 toutes les clés de `delivery` présentes dans `RES_TIER` | **PASS** (0 clé inconnue) |
| §4 champ `island` sur **exactement** les 29 convertis, aux valeurs du brief | **PASS** ×2 |
| 8.13 nœuds 39/41/43 : `mode: 'auto'`, **sans** `delivery` ni `island` | **PASS** |
| 8.14 les 14 nœuds intouchés **octet à octet** vs base 373 | **PASS** (14/14) |
| 8.14bis `delivery` des 10 antérieures identique en structure | **PASS** |
| **8.15 contre-épreuve** : aucun des 29 n'a de `delivery` sur la base ; la base a bien `{delivery:10, auto:31, manual:1}` | **PASS** ×2 |
| §6 `name` / `prereq` / `reqs` / `unlocks` des 29 **inchangés** | **PASS** (0 écart) |

### 7.2 — Moteur (23 assertions, Chromium) — `tA.js`

| Test | Résultat |
|---|---|
| Facteur lu = 1 · modes runtime · valeurs de base | **PASS** ×3 |
| **§3 `portPool` rend la référence VIVANTE** (sonde écrite/relue) | **PASS** |
| **§3 `portPool(7)` → le port de l'île 6, `port[7]` jamais créé** | **PASS** |
| 8.5 île 2, 100 `minerai_cuivre` → nœud 3 confirmé, port débité **de 100 exactement** (le reste du stock intact), déblocage appliqué | **PASS** ×3 |
| 8.4 port vide → refus, `deliveryReady` faux, `hasPendingResearch` faux | **PASS** ×2 |
| 8.6 depuis l'île 5 **avec le stock en local** → refusé, rien débité, `deliveryReady` faux | **PASS** ×3 |
| **8.7** nœud 33 (1000 `cable_supraconducteur`) île 6 → stock **réellement** débité, relu via `game.port[6]` **en direct** | **PASS** ×2 |
| **8.8** nœud 35 depuis le **souterrain** → **réussit**, débité du pool de l'île 6, aucun `port[7]` créé | **PASS** ×3 |
| **8.9** même nœud depuis l'île 6 → refusé, stock intact | **PASS** |
| §1 les 10 livraisons antérieures restent **sans** contrainte d'île | **PASS** |
| 8.13 nœud 39 confirmé d'un clic, **gratuitement** (port inchangé) | **PASS** |
| §1 `techConfirm` **refuse** un nœud converti | **PASS** |
| Console propre | **PASS** |

### 7.3 — UI réelle (17 assertions, vrais clics) — `tB.js`

| Test | Résultat |
|---|---|
| 8.4 bouton « Livrer » présent et **désactivé**, pastille rouge, **aucune** note d'île, **aucune** pastille de notification | **PASS** ×4 |
| 8.5 bouton **actif** avec le stock, pastille verte, **clic RÉEL** → nœud confirmé et stock débité | **PASS** ×3 |
| 8.6 depuis l'île 5 : bouton gris, note **« À livrer depuis l'Île 2 »**, même motif en `title`, pastille montrant **60/100 de l'île 2** (et non 100 de l'île 5) | **PASS** ×4 |
| §4 depuis l'île 6, nœud 35 → **« À livrer depuis l'Île 6 S »**, gris malgré une pastille verte (le pool est le même) | **PASS** ×2 |
| §4 depuis le souterrain, le même nœud est **actif** et sans note | **PASS** |
| 8.13 nœud 39 → bouton **« Confirmer »**, aucune pastille de coût | **PASS** |
| Console propre | **PASS** |

### 7.4 — Saves et facteur (16 assertions) — `tC.js`

Saves **réelles**, écrites par la **base 373** (fichier servi sur la même origine) puis rechargées sur
le build 374 par le vrai chemin de chargement.

| Test | Résultat |
|---|---|
| **7a** 6 nœuds confirmés sur la base → **restent confirmés**, **aucun coût rétroactif** (port intact au caractère près), déblocages conservés | **PASS** ×4 |
| **8.12 / 7b** un nœud `condition_ok` devient **payant** : `mode` passé à `delivery`, `techConfirm` refusé, livraison refusée sans stock puis acceptée avec, port débité exactement | **PASS** ×3 |
| **8.11 / 7c** save **tout confirmé** → **43/43** restent confirmés, aucun coût réclamé, aucune recherche en attente, **0 erreur de tick** | **PASS** ×3 |
| **8.10** facteur = 10 → les 29 livraisons ×10 (`n3` 1000, `n7` 100000, `n37` 10), **aucune** valeur restée à la base | **PASS** ×3 |
| 8.10 les 10 livraisons antérieures **non touchées** par le facteur | **PASS** |
| Console propre | **PASS** |

### 7.5 — 8.16, le test qui compte (5 assertions) — `tD.js`

Partie **neuve**, arbre parcouru de bout en bout. Les `reqs` sont forgées — ce sont la **livraison** et
la **contrainte d'île** qu'on éprouve, pas la capacité à produire. Chaque nœud est franchi **pour de
vrai** (`techDeliver` / `techConfirm`), sur un port **approvisionné exactement** du montant dû, en
basculant sur l'île attendue.

- **39 nœuds de livraison franchis**, dont le nœud 35 **depuis le souterrain** ;
- **3 nœuds `auto` (39/41/43)** confirmés d'un clic ;
- **43/43 confirmés — aucun nœud infranchissable** ;
- **aucune ligne `BLOQUÉ` ni `DÉBIT FAUX`** : chaque port est retombé à zéro après sa livraison.

### 7.6 — Contre-épreuves runtime sur la base 373 (3 assertions) — `tContre.js`

| Contre-épreuve | Résultat |
|---|---|
| **CE-1** sur la base, la livraison depuis l'île 7 **échoue** (nœud 31, déjà `delivery` avant ce lot) — le bug du §3 est démontré, pas supposé | **PASS** |
| **CE-2** sur la base, le nœud 3 se valide **gratuitement depuis l'île 5** — aucune contrainte d'île n'existait | **PASS** |
| **CE-3** sur la base, `RESEARCH_DELIVERY_FACTOR` n'existe pas | **PASS** |

### 7.7 — Non-régression (56 assertions, 0 KO)

Suites des lots précédents rejouées sur ce build :

| Suite | Assertions |
|---|---|
| Lot « UI & Port » (14.89) — port en ruine, « Demander au port », en-têtes sprite, panneau Énergie, Options, colonnes du Port, Collisionneur | 43 |
| **Tutoriel** (3 assertions, dont l'étape franchie par un tap réel) | 3 |
| Lot « Gisements » (14.90) — nodules par île, repli par présence de clé, overlay transparent | 12 |
| Boot **édition PUBLIQUE** : canvas **100 %**, horloge qui avance, 0 `tickError`, **0 erreur console** | 1 |
| Boot **édition DEV** (`DEV_BUILD=true`) : canvas **100 %**, 0 `tickError`, **0 erreur console** | 1 |

⚠ Le tutoriel est **intact** : son étape 8 pointe `data-tut="confirm"`, présent sur les boutons
« Livrer » **et** « Confirmer», et le premier nœud validable reste le nœud 2 (déjà `delivery` avant ce
lot). Aucune modification du tutoriel n'était nécessaire.

---

## 8 — Écarts au brief et constats

### 8.1 — Écarts assumés

| # | Écart | Justification |
|---|---|---|
| 1 | **Base 373 et non 372** | La branche avait avancé. Toutes les ancres re-vérifiées ; comptages du brief retrouvés à l'identique. |
| 2 | **Le facteur ne s'applique qu'aux 29 nœuds du lot** | Le §1 et le §9 excluent explicitement les 10 livraisons antérieures. Le champ `island` sert de marqueur, sans état supplémentaire. |
| 3 | **Les pastilles de coût montrent le stock de l'île ATTENDUE** | Le brief demande d'« indiquer l'île attendue » sans dire quoi afficher dans les pastilles. Montrer le stock local d'une île qui n'a rien à voir aurait été un chiffre trompeur. |
| 4 | **i18n en/es/de ajoutée** pour le nouveau libellé | Non demandé, mais c'est la pratique du projet depuis 13.32 pour tout libellé d'interface. |

### 8.2 — Constats **signalés, non corrigés** (hors périmètre)

**(a) La notification de recherche se ré-arme au changement d'île.** `evaluateTechTree` remet
`node.notified` à `false` quand `deliveryReady` redevient faux — donc quitter puis revenir sur l'île
attendue **re-déclenche** la notification et son SFX. Ce n'est **pas un comportement neuf** : il
existait déjà pour les 10 livraisons antérieures, dont `deliveryReady` dépendait déjà du port de
l'île courante. Ce lot l'étend simplement à 29 nœuds de plus. Si cela devient bruyant, le correctif
est d'une ligne (ne plus dé-armer `notified` une fois posé) — mais c'est un changement de
comportement, donc une décision, pas un correctif.

**(b) Le bouton Recherche peut être grisé avant la première confirmation.** `researchPanelUnlocked`
(14.81) = « une recherche est livrable **ici** » **ou** « un nœud ≠ 1 est déjà confirmé ». Avant la
toute première confirmation, si rien n'est livrable sur l'île courante, le bouton reste gris. **Ce
n'est pas une régression** : le premier nœud validable est le nœud 2, **déjà** une livraison avant ce
lot, donc la fenêtre existait déjà à l'identique. Dès le nœud 2 confirmé, le panneau est ouvrable en
permanence — vérifié. *(Ce point a d'abord fait échouer un test : l'état forcé était incohérent avec
les invariants du jeu — un nœud `condition_ok` dont le prérequis n'était pas confirmé. Le harnais
confirme désormais la chaîne d'ancêtres, comme le jeu le ferait.)*

**(c) Équilibrage non éprouvé, et c'est la raison d'être du facteur.** La condition `produce` compte
une production **cumulée depuis le début de la partie** ; la livraison exige d'en avoir autant **en
stock au même instant**. Pour un intermédiaire consommé aussi vite qu'il est produit, l'écart peut
être rude — le nœud 25 (1000 de **chacun** des trois irradiés simultanément) et le nœud 33
(1000 `cable_supraconducteur`, produits 1/s par la seule Presse UHP) sont les plus exposés.
`RESEARCH_DELIVERY_FACTOR` est là pour ça, et rien n'a été « rééquilibré » spontanément.

**(d) Le champ `island` du nœud et le champ `island` d'un `req`** (`resourceTile`, `port`) portent le
même nom dans deux portées différentes. Le brief impose ce nom ; aucune collision (objets distincts),
mais c'est à savoir en relisant le nœud 35, qui porte les deux.

### 8.3 — Hors périmètre, non touché

Halos d'antenne après déficit ; overlays du lot Gisements ; coûts d'accès aux îles (nœuds 2, 8, 14,
21, 28) ; la branche `manual` du code ; `techProduced` et les `reqs` ; `SAVE_VERSION` ; l'arbitrage
cargo du lot Port (toujours à trancher) ; le `notes` doublement échappé de `version.json` (anomalie CI
préexistante 14.76).

---

## 9 — Résumé

| | |
|---|---|
| Ancres | **6 textuelles à `count == 1`** + 1 transformation programmatique + 3 insertions ancrées, **round-trip verbatim** |
| `node --check` | **7 blocs / 7 OK**, éditions publique **et** dev |
| Nœuds | **43**, `{start:1, delivery:39, auto:3, manual:0}` |
| Assertions du lot | **79, 0 KO**, rejouées 2 fois sans flottement |
| Contre-épreuves sur la base | **5** (2 statiques + 3 runtime), toutes probantes |
| Non-régression | **56 assertions, 0 KO** + 2 boots à canvas 100 % |
| Delta | **+5 893 o** (3 310 733 → 3 316 626) |
