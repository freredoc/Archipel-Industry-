# RAPPORT — Lot « Gisements par exclusivité d'île »

## Versions produites

| | Base | Livré |
|---|---|---|
| `GAME_BUILD` | 372 | **373** |
| `GAME_VERSION` | Alpha 14.89 | **Alpha 14.90** |
| `SAVE_VERSION` | 31 | **31 (INCHANGÉ)** |
| Taille | 3 303 182 o | **3 310 733 o** |

Base de référence **EXACTE** au brief (372 / 14.89 / 3 303 182 o).

### Delta d'octets

| Poste | Octets |
|---|---|
| 2 blocs (6 sprites + site de rendu) + bump | **+7 718** |
| `GAME_NOTES` | −167 (le nouveau texte est plus court) |
| **Total** | **+7 551** |

Les 6 PNG pèsent ~4 500 o de ce total ; le reste est le commentaire de décision.

### SHA-256 des 7 blocs `<script>`, RE-EXTRAITS du fichier patché

```
blk1  a50c1c4e7f4a304c650c0cfa7e06c4ffdfb0d87510d83738114d4dd5a1641628      413 o
blk2  8fbb22187703339c146b2f82badd8701d199128cd3d9dd8673d213c8616ca541    4 341 o
blk3  d949f1c3687aedadcedac85261865f29b17cd273997e7f6b2bfc53b2f9d4c4dd   10 751 o
blk4  35f4f974f4b2bcd44da73963347f8952e341f83909e4498227d4e26b98f66f0d  131 835 o
blk5  8f111a1380cf98fca4e48d1fc2aa448199b93cab0be15072f68e7175ba98e426  1 111 021 o
blk6  8582f11695ef290087b8af9803ba12d1509e1087229d5b0cfbd63f616f6790d2  225 580 o
blk7  218cd2ec042d375c79b8868ab341ad69f9b5981fe3f708eb571605332b9a3a0b  1 548 283 o
```

Seul **blk7** change (les 6 autres sont bit-à-bit identiques au build 372).

## Ancres appliquées

| Ancre | `count` avant | Rôle |
|---|---|---|
| `// --- spritesheets (frame 0 == sprite statique, verifie pixel a pixel) ---` | **1** | insertion des 6 clés |
| `        const ov = COAST_FEATURE_OVERLAY[t.terrain];`<br>`        if (ov) drawSprite(ctx, ov, x, y, tile, tile);` | **1** | site de rendu (les 2 lignes sont consécutives → **une seule ancre**, ce qui évite deux remplacements dépendants) |
| `const GAME_BUILD = 372;` | **1** | bump |
| `const GAME_VERSION = 'Alpha 14.89';` | **1** | bump |

`const COAST_FEATURE_OVERLAY = {` : vérifiée `count == 1` mais **NON modifiée** — la table reste le repli générique, conformément au brief.

- **4/4 ancres à `count == 1` AVANT écriture**, extraites du fichier par script (jamais retapées).
- **Round-trip : 4/4 blocs retrouvés VERBATIM** (`count == 1`) dans le fichier compilé.
- **`node --check` : 7 blocs, 7 OK**, sur l'édition PUBLIQUE **et** DEV.
- Blocs extraits du fichier **non filtré** (le filtre `awk 'length($0)<300'` n'a servi qu'au grep).
- Taille par `os.path.getsize`.

## Sprites — §1 et test 6.10

SHA-256 vérifiés **avant** encodage (contre le pack) **et** re-décodés **depuis les data-URL du
fichier patché** :

| Clé | SHA-256 | Taille | Format | Transparence |
|---|---|---|---|---|
| `overlay_resource_i1` | `08bdcc51…5539f8843` | 554 o | 32×32 colortype 3 | `tRNS` présent |
| `overlay_resource_i2` | `f05e90f9…a08e7f5e13` | 554 o | 32×32 colortype 3 | `tRNS` présent |
| `overlay_resource_i4` | `bcc8a62b…6823361187` | 554 o | 32×32 colortype 3 | `tRNS` présent |
| `overlay_resource_i5` | `66a1d2e7…711555d9b5a` | 554 o | 32×32 colortype 3 | `tRNS` présent |
| `overlay_resource_i6` | `dff02e77…f418c0133eb6436` | 554 o | 32×32 colortype 3 | `tRNS` présent |
| `overlay_resource_i7` | `892b9cb9…656a378017c9890` | 554 o | 32×32 colortype 3 | `tRNS` présent |

**6/6 conformes**, aucune corruption au passage base64, transparence de palette préservée.

**Silhouette — claim du pack vérifiée au pixel**, sur les sprites re-décodés du fichier patché :

| Sprite | Px opaques | Écart d'alpha vs base | Px recolorés | Couleurs |
|---|---|---|---|---|
| `overlay_resource` (base) | **487** | — | — | — |
| les 6 variantes | **487** chacune | **0 / 1024** | 94 | 39 |

Alpha strictement binaire `{0, 255}`. La silhouette du massif est **bit-à-bit identique** ; seuls
les 94 pixels des 6 nodules changent de couleur. Aucun décalage visuel possible d'une île à l'autre.

## Résultats des tests

Harnais Chromium, serveur lancé **depuis le dépôt**. **Build effectif : 373 / Alpha 14.90.**

**12 assertions du lot, 0 KO**, + **6 contre-épreuves** sur la base 372, + **39 assertions de
non-régression** du lot Port rejouées sur ce build. Console : **0 erreur**.

⚠ **Le comptage se fait sur des POSITIONS DISTINCTES (`clé@x,y`), pas sur des appels `drawImage`** :
la capture court sur plusieurs frames (~13 redraws en 700 ms) et un comptage d'appels rendait des
multiples (208 au lieu de 16). C'est le piège qui a fait échouer la première passe.

⚠ **Viewport 1250×1150 pour les comptages** : au zoom 1 le rendu ajuste la grille au canvas
(`fitTile` borné à [26, 64]), donc sur un écran de 420 px l'île entière n'est pas visible et le test
ne compterait que les tuiles à l'écran — il passerait en mesurant moins que la vérité.

| # | Résultat | Mesure |
|---|---|---|
| 6.1 | **PASS** | Île 1 : **16** tuiles `resource` → **16 × `overlay_resource_i1`**, 0 générique, 0 autre variante |
| 6.2 | **PASS** | Île 2 : **19** → **19 × `overlay_resource_i2`** |
| 6.3 | **PASS** | Îles 4 / 5 / 6 : **10 / 12 / 4** → autant de `_i4` / `_i5` / `_i6` |
| 6.4 | **PASS** | Île 3 : **5** tuiles `resource` → **5 × `overlay_resource` générique**, **0 variante** |
| 6.5 | **PASS** | Île 3 : **6** tuiles `oil` → **6 × `overlay_petrole`**, inchangé |
| 6.6 | **PASS** | Île 7 : **0** tuile `resource` à la génération, **3 poches FORGÉES** → **3 × `overlay_resource_i7`** |
| 6.7 | **PASS** | `obstacle` : 4 / 4 / 4 / 4 / 4 / 2 par île → `overlay_obstacle` inchangé partout |
| 6.8 | **PASS** | `overlay_resource_i2` retiré de `SPRITE_DATA` → **19 × générique**, 0 variante, **aucune tuile sans overlay** |
| 6.9 | **PASS** (statique) | `SPRITES_ENABLED` est une **`const` de module** (non réassignable) ; la branche `else` n'est pas touchée par le patch |
| 6.10 | **PASS** | 6 SHA-256 re-décodés du fichier patché : identiques au § 1 |
| 6.11 | **PASS** | overlay dessiné sur un fond magenta : **537 px de fond restent visibles**, 487 couverts — le terrain reste visible, aucun carré opaque |
| 6.12 | **PASS** | **contre-épreuve base 372** : les îles 1/2/4/5/6/7 rendent **toutes** le générique, **0 variante** → 6 tests échouent |

### Méthode employée pour le test 6.6 (exigée par le brief)

**FORGE** de `tiles[r][c].terrain = 'resource'` sur 3 tuiles `land` de l'île 7, puis redessin.
Le creusement réel n'a **pas** été employé : il dépend de `Math.random() < DRILL_POCKET_CHANCE`,
donc non déterministe. Le test mesure d'abord **0 tuile `resource` à la génération** — ce qui rend
explicite le fait qu'un test naïf « ouvrir l'île 7 et regarder » aurait passé **à vide**.

### Boot des 2 éditions

| Édition | `DEV_BUILD` | Canvas peint | Ticks / 6 s | Erreurs tick | Console |
|---|---|---|---|---|---|
| PUBLIQUE | false | **100 %** | 6 | 0 | **aucune** |
| DEV | true | **100 %** | 6 | 0 | **aucune** |

### Non-régression du lot Port (build 372) rejouée sur 373

| Suite | Assertions | Résultat |
|---|---|---|
| Port en ruine | 10 | **0 KO** |
| « Demander au port » + en-tête UpgradePanel | 6 | **0 KO** |
| Panneau Énergie (0 kW) | 7 | **0 KO** |
| En-têtes sprite + Options | 7 | **0 KO** |
| Colonnes du Port | 2 | **0 KO** |
| Collisionneur (auto-launch) | 11 | **0 KO** |
| Tutoriel avec port en ruine | 3 | **0 KO** |
| **Total** | **46** | **0 KO** |

---

## Écarts au brief, et pourquoi

### 1. ⚠ Le tableau de comptage du § 4 décrit un mode que le joueur ne peut plus créer

Le brief annonce (comptage « obtenu par évaluation de `ISLAND_TERRAINS_BASE` ») :

| Île | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|---|---|
| brief (`ISLAND_TERRAINS_BASE`) | 8 | 9 | 4 | 8 | 7 | 6 | 0 |

**Ce tableau est exact** — vérifié : `ISLAND_TERRAINS_BASE` donne bien 8 / 9 / 4 / 8 / 7 / 6 / 0.
Mais `ISLAND_TERRAINS_BASE` est la base du **mode DIFFICILE**. Le mode **NORMAL** part de
`NORMAL_ISLANDS`, et **depuis le lot 3B (14.81) une partie neuve démarre DIRECTEMENT en mode
Normal** : la `ModeModal` n'a plus aucun point d'entrée (vérifié en jeu — `0 .mode-card`, mode
`normal`). Les comptes qu'un joueur voit réellement sont donc :

| Île | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|---|---|---|---|---|---|---|---|
| **NORMAL (ce que voit le joueur)** | **16** | **19** | **5** | **10** | **12** | **4** | **0** |
| `oil` en NORMAL | 0 | 0 | **6** | 0 | 0 | **0** | 0 |

Deux conséquences à connaître : l'île 3 a **5** tuiles `resource` en Normal (et non 4), et l'île 6
n'a **aucune** tuile `oil` en Normal (elle en a 2 en Difficile).

**Les tests ont donc été formulés contre la vérité de la partie en cours** (comptage des tuiles dans
`game.islands`, puis exigence « autant de nodules que de tuiles `resource`, et zéro autre clé »),
et non contre une constante recopiée du brief. Le tableau du brief est vérifié **séparément**, comme
donnée. Les deux assertions passent.

### 2. Île 3 — arbitrage **A** livré, comme demandé

Ses 5 tuiles `resource` gardent l'overlay générique. Le mécanisme étant un **repli par présence de
clé**, déposer un `overlay_resource_i3` dans `SPRITE_DATA` suffirait à basculer en B **sans
retoucher une ligne de code** — c'est consigné en commentaire au site de rendu.

L'incohérence assumée reste réelle et un peu plus large qu'annoncé : `puits_petrole` et
`puits_petrole_v2` ont bien `terrains: ['resource', 'oil']`, donc **5** tuiles (pas 4) sont
concernées en mode Normal.

### 3. Île 7 — le cyan est conservé, avec la justification du brief (et non celle du pack)

Vérifié : la grille de base de l'île 7 ne contient **aucun** caractère `M` (0 tuile `resource` à la
génération), et ses tuiles `resource` naissent au runtime du creusement des foreuses. Sur l'île 7,
`terrain === 'resource'` **désigne donc exclusivement une poche d'Hélium-3** : le cyan est
sémantiquement exact. Le raisonnement du `LISEZ-MOI` (cyan comme pis-aller de lisibilité contre le
gris de l'extracteur) n'a pas été retenu — la raison du brief est meilleure, et c'est elle qui est
inscrite en commentaire.

### 4. Aucun écart d'implémentation

Le mécanisme est exactement celui prescrit : repli **par présence de clé**, jamais par liste d'îles
en dur ; table `COAST_FEATURE_OVERLAY` **inchangée** ; `obstacle` et `oil` non touchés ; branche
`else` de `SPRITES_ENABLED` non touchée. Le seul changement de forme est `const ov` → `let ov`,
imposé par la substitution.

## Lisibilité en jeu

Planche jointe : `docs/captures-lot-gisements/nodules-par-ile.png` — un gisement de chaque île,
**au rendu réel (tuile 26 px, zoom par défaut)**, capture magnifiée ×4 pour l'examen.

Les 6 teintes se distinguent nettement du brun du massif et les unes des autres. L'île 3 (massif
brun nu) contraste clairement avec les 5 autres. Les rochers gris d'`overlay_obstacle`, visibles sur
les vignettes des îles 2 et 6, ne sont pas confondables avec les nodules : silhouette entièrement
différente (rochers libres sur l'herbe contre nodules sertis dans un massif brun).

## Points restants

- **Île 3 / arbitrage B** : produire `overlay_resource_i3` au brun pétrole `#3E2723` si Ethan veut
  la série complète. Aucun code à toucher.
- **Hors périmètre, non touché** : conversion recherche → livraison (29 nœuds) ; halos d'antenne
  disparus après déficit ; arbitrage cargo hérité du lot Port ; `notes` doublement échappé dans
  `version.json` (anomalie CI préexistante, 14.76) ; `COAST_FEATURE_OVERLAY` ; `overlay_obstacle` ;
  `overlay_petrole` ; `SAVE_VERSION`.

## Pièges de harnais (nouveaux, à ne pas redécouvrir)

1. **Compter des positions distinctes, pas des appels `drawImage`** : l'espion accumule sur toutes
   les frames de la fenêtre de capture. Clé de dédup : `clé@x,y` (la caméra ne bouge pas pendant la
   mesure).
2. **Un comptage d'île exige un GRAND viewport** : au zoom 1, `fitTile` est borné à [26, 64], donc
   sur 420 px de large l'île de 32 tuiles ne tient pas et le test ne verrait qu'une partie des
   tuiles — il passerait en mesurant moins que la vérité.
3. **Il n'y a plus d'écran de choix de mode** (14.81) : un `chooseMode` par clic sur `.mode-card` ne
   trouve rien, et toute partie neuve est en **Normal**. Pour vérifier les grilles du mode Difficile,
   lire `ISLAND_TERRAINS_BASE` **comme donnée** — ⚠ ne **jamais** appeler `applyGameMode('difficile')`
   à la main : il repeuple les defs sans reconstruire les grilles déjà créées, et la boucle de rendu
   lève à chaque frame (piège 14.35).
4. **`ISLAND_TERRAINS` est un tableau VIDE à la déclaration** (peuplé au runtime selon le mode) :
   un comptage qui le lit sort **zéro sans erreur**.
5. **Le barycentre des tuiles `resource` tombe souvent sur de l'herbe vide** : pour cadrer une
   capture sur un gisement, viser la tuile qui a le plus de voisines `resource` dans un rayon de 2.
