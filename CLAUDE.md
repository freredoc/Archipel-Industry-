# Archipel Industry — Guide projet

Mémo pour les sessions Claude Code. À lire au début de chaque session.

## Le jeu
- Jeu mobile **mono-fichier** : `Archipel_industry_alpha-7.html` (~8700 lignes, React via
  `React.createElement` inline, **fonctionne hors-ligne**, aucune dépendance réseau).
- `index.html` = simple redirection vers ce fichier.
- Le commentaire « React via Babel standalone (JSX) » en tête est trompeur : le code est
  du `React.createElement` pur (pas de JSX). Pour valider la syntaxe : extraire le bloc
  `<script>` du jeu et `node --check`.

## Version (à bumper à CHAQUE modif livrée)
- En haut du fichier (~ligne 2820) :
  - `const GAME_BUILD = N;`  (entier — sert à comparer avec la version en ligne)
  - `const GAME_VERSION = 'Alpha X.Y';`  (étiquette affichée)
- ⚠️ **Si on ne bumpe pas `GAME_BUILD`, le jeu n'affiche pas de notification de mise à jour.**
- La CI régénère `version.json` (racine) depuis `GAME_BUILD`/`GAME_VERSION` après un build
  sur `main`.
- **État au dernier passage : `GAME_BUILD = 60`, `GAME_VERSION = 'Alpha 10.35'`.** Changement
  10.35 : intégration des **6 sprites d'état de panne** (`etat_route`, `etat_tuyau`, `etat_cable`,
  `etat_intrant`, `etat_courant`, `etat_arret`) inlinés en base64 dans `window.__SPRITE_DATA__`
  (350 sprites au total) → `drawStatusBadge` les affiche désormais réellement par-dessus les
  bâtiments à l'arrêt. Changements
  10.34 : (1) **pose sans route** — le garde-fou `needRoad`/`hasAdjacentRoad` est retiré de
  `canPlace`/`tryPlace` ; un bâtiment se pose même sans route adjacente (il s'affiche déconnecté
  via `discReason='road'` tant qu'aucune route ne le touche). (2) **Export/Import de sauvegarde
  (texte)** dans `SlotPanel` : bouton « Exporter » par emplacement (copie le JSON du slot dans le
  presse-papier + textarea), bouton « Importer » (colle un JSON → nouveau slot via `slotImport`,
  puis `slotSwitchTo`/reload). Choix texte (pas de fichier) car la WebView Android n'a ni
  DownloadListener ni file chooser. (3) **Diesel transporté par tuyau** : `CARRIER_BY_RES.diesel`
  passe `road`→`pipe`. (4) **Sprites d'état de panne** : `drawStatusBadge` dessine un sprite
  PAR-DESSUS un bâtiment à l'arrêt (au lieu du carré rouge/noir), mappé depuis `bld.discReason` via
  `STATUS_SPRITE` ; fallback teinte si le PNG manque. **Sprites à fournir (clés)** : `etat_route`,
  `etat_tuyau`, `etat_cable`, `etat_intrant`, `etat_courant`, `etat_arret`.
- Panneau (10.33) : le panneau
  Câble affiche le **bilan électrique par réseau** (Production / Demande / Livrée) via
  `game.wireInfo[isl][nid]` (rempli dans la boucle énergie de `tickIsland`) : si un câble est
  **saturé** (production ≥ demande mais livrée < demande à cause du débit) → message « câble saturé,
  améliorez le débit » ; sinon si production < demande → « production insuffisante ». Le motif `power`
  du panneau bâtiment renvoie vers le débit du câble. Jonctions :
  en **Normal** illimitées avec **coût croissant** (`JUNCTION_BASE_COST` ×2^(nb déjà posées du type) ;
  remboursement symétrique à la démolition) ; en **Difficile** limitées à 1/type/île (gratuites).
  Pose via `tryPlaceJunction` : autorisée **sur une tuile vide** OU **par-dessus un réseau infra**
  d'un des deux porteurs (croisement converti) ; si l'autre porteur manque, une infra V1 est
  **auto-posée perpendiculairement** sur les tuiles voisines libres (« route de l'autre côté »).
  ⚠️ Une jonction appartient aux **DEUX réseaux porteurs** : `rebuildNetworks` la traverse pour
  chaque porteur et stocke un `networkId` par porteur dans `t.netIds` (les deux carriers « passent »
  à travers, dans n'importe quelle orientation). `adjacentNetworks`/`…Footprint` lisent `t.netIds`.
  Le sprite de jonction est **choisi orienté** : il existe 2 sprites par type (`jonction_<H>_<V>` où
  H = porteur horizontal, V = vertical, ex. `jonction_route_cable` vs `jonction_cable_route`) ; le
  draw sélectionne le bon selon `netConnectMask` des deux porteurs (« rotation auto »). Le `SAVE_VERSION`
  est à **12** (rétro-compat gérée au chargement pour les versions 3→12).

## Systèmes du jeu (repères de code — rechercher les noms exacts)
Tout est dans le mono-fichier. Pour valider : extraire le `<script>` et `node --check`.
- **Modes de jeu** (au démarrage d'une nouvelle partie) : `Difficile` = îles compactes
  d'origine (`ISLAND_TERRAINS_BASE`), `Normal` = grandes îles éditeur (`NORMAL_ISLANDS`,
  codes terrain W/T/M/C/O/P/X). `applyGameMode(mode)` peuple `ISLAND_TERRAINS` + `PORTS`
  depuis les bases. `game.mode` sauvegardé ; modal `ModeModal` + handler `chooseMode`.
- **Extension des mers** : `SEA_PAD = 8` tuiles d'eau ajoutées sur chaque côté via
  `padIslandDef`. Décalage uniforme +8,+8 → migration des saves < v11 dans `loadSave`
  (placements, pools, niveaux, clés `energyPriority`). `centerCam()` recentre la vue.
- **Terrain pétrole** : type `oil` (`TERRAIN_COLORS.oil`, char `P`), où l'on pose
  `puits_petrole` (terrains `['resource','oil']`, `exclusiveIsland: 3`).
- **Réseaux (route/tuyau/câble)** : débit `networkThroughput(n) = 128×8^(n-1)` (infini, plus
  de plafond Infinity). `networkUnitCost(type, level, payMat)` : V1-V2 base, V3+ choix
  ciment/lingot_fer/câble (volume, base 800) **ou** béton armé/acier (premium, base 100),
  ×2/niveau. État `net.unlimited` (débit Infinity) via 10000 du matériau **irradié**
  (`beton_arme_irradie`/`acier_irradie`/`cable_irradie`). Sprite réseau bloqué à V3 (niv≥3),
  V4 = visuel illimité (`Math.min(lvl,3)`, ou 4 si `net.unlimited`).
- **Priorité de flux par bâtiment** : `bld.fluxPri` (`haute`/`normale`/`basse`), sert les
  prioritaires d'abord sur réseau saturé (`netTierDemand`/`tierFactor` dans le tick).
- **Énergie** : `game.energy[isl]` = {produced(=supply), consumed, demand, gross, accStored,
  accCap}. HUD : bilan réel (supply−demand), pastille 🔋 batterie. `EnergyPanel` : récap +
  « Demande non servie ». ⚡ **L'élec circule PAR réseau câble** : chaque réseau câble est un
  sous-réseau électrique distinct (prod/conso/accus rattachés via `firstWireNid` = 1er câble
  adjacent au footprint). Le **débit du câble** (`networkThroughput`/illimité) borne la puissance
  transmissible → `netDemand[wireNid]` = demande élec, le `NetworkPanel` câble affiche débit/
  saturation et l'amélioration sert vraiment. Tick : boucle par `wireProd`/`wireCons`/`wireAccs`
  dans `tickIsland` (modes priority/fair/proportional via `cutToFit`).
- **Transit** (taille des lots cargo) : `portSpeed` / `portSpeedMult = 2^level`,
  `PORT_MAX_LEVEL = Infinity` (infini, coût ×2/niveau). Base lot = `shipBatchBase()` (=`SHIP_BATCH`
  600, ÷10 en Difficile). Mode de répartition `game.tradeMode[isl]` : `priority` (défaut, remplit
  la cale du plus prioritaire) ou `proportional` (prorata des demandes) — `loadCargo`/`tradeModeFor`.
- **Mode de jeu & équilibrage** : `CURRENT_MODE` (var module, MAJ par `applyGameMode` + en tête
  de `onTick`). En **Difficile** : `networkThroughput` ÷2 (V1=64, V2=512, V3=4096) et `shipBatchBase` ÷10 (économie tendue
  d'origine). Coût de construction renchéri par palier via `TIER_COST_MULT` (T1×2, T2×4, T3×8 ;
  T0/infra inchangés) appliqué une fois sur `BUILDINGS[id].cost` au chargement du module.
- **Bateau** : `drawSpriteRot` (rotation), visible seulement ~10 s près du quai (départ/arrivée),
  hors écran le reste (seuil `BOAT_PROX_THRESH`).
- **Répare/remblai** : gâtés par recherche (`isTerrainRepairUnlocked`/`isTerrainExtendUnlocked`)
  AU TAP — on n'ouvre plus le panneau avant déblocage.

## Build APK Android (CI)
- Workflow : `.github/workflows/android.yml`. Déclencheurs : push sur `main` (chemins
  `Archipel_industry_alpha-7.html`, `android/**`, le workflow) **ou** `workflow_dispatch`.
- Construit l'APK (coquille WebView, dossier `android/`), le **signe**, et le publie dans la
  release **`apk-latest`** (asset `ArchipelIndustry.apk`).
- App Android : `android/app/src/main/java/fr/archipel/industry/MainActivity.java` (WebView).
  La barre de navigation Android (3 boutons) reste visible, son espace est réservé (pas de
  mode immersif).

## Signature — clé stable (CRUCIAL)
- Secret repo GitHub **`SIGNING_KEYSTORE_B64`** = le keystore en base64 (Repository secret,
  onglet *Actions*).
- `android/app/build.gradle` : alias **`archipel`**, store/key password **`archipelapp`** (valeurs
  par défaut). Le keystore généré doit correspondre à ces valeurs.
- Empreinte SHA-256 attendue :
  `A259F77798C2B99C567EA70D6B3E94490E32FE924C3FDCEFD83332619A3962A3`
- Le workflow affiche le certificat à chaque build (étape « Show signing certificate », via
  **`apksigner verify --print-certs`** — `keytool -printcert -jarfile` ne lit PAS le schéma v2/v3).
- ⚠️ Sans cette clé, l'APK est signé avec la **clé debug** (régénérée à chaque run CI) →
  signatures différentes → erreur Android **« Application non installée »** lors des MAJ.
- Après une bascule de clé, il faut **désinstaller une fois** ; ensuite les MAJ s'installent
  par-dessus sans désinstaller (même clé).

## Workflow de développement (préférences utilisateur)
- Développer sur la branche **`claude/resource-access-question-5mftqw`**.
- **Push direct sur `main` BLOQUÉ** par un garde-fou de l'environnement → toujours passer par
  une **Pull Request**.
- Préférence : **Claude crée la PR, lance le build, ET merge lui-même** (via l'API GitHub —
  le merge de PR est autorisé, contrairement au push direct sur `main`) une fois le build vert.
- ⚠️ **Avant de merger, vérifier que la tête de la PR contient bien le dernier commit/bump**
  (un merge sur une tête périmée laisse `version.json` en arrière — déjà arrivé).
- Après le merge sur `main`, le build `main` resynchronise `version.json`.
- Garder la branche de dev à jour avec `main` pour éviter la divergence.

## Environnement
- Conteneur **éphémère** (cloud) : tout ce qui n'est pas commité est perdu entre sessions.
- Outils GitHub via le serveur MCP (`mcp__github__*`), pas de `gh` CLI.
