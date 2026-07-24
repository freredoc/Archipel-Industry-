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
- **État au dernier passage : `GAME_BUILD = 275`, `GAME_VERSION = 'Alpha 13.93'`, `SAVE_VERSION = 27`.**
  Changement 13.93 : **PUZZLE COLLISIONNEUR — Phase A (fondations : multi-source par face + 4 portes ;
  brief `briefcouchelogiqueile6`).** PÉRIMÈTRE = les briques moteur non-régressives et testables ; le
  runtime du puzzle (émetteur/vanne/Collisionneur/Data Center/nœuds 39-41/tutos) est REPORTÉ. `SAVE_VERSION`
  INCHANGÉ (aucun champ persisté ajouté : portes = ids additifs ; `emitBits` lu au runtime, aucun bâtiment
  ne le pose encore). (1) **§1 Multi-source par face** (`computeLogic`) : nouveau flag def `logicMultiSource`
  + branche de collecte `emitters` (parallèle à sensors/gates/sinks) + boucle qui écrit **UNE valeur par
  direction** dans `base` via `dirNet(r,c,d)` (mapping FIXE non orientable : dir 0→α, 1→β, 2→γ, 3→face
  INERTE toujours 0). Les bits proviennent de `bld.emitBits` (posé par le parent, §3.2 non implémenté ;
  absent → tout 0). N'écrit QUE des 1 (OR par réseau) → **non-régression** : un `logicSource` classique
  continue de mettre à 1 TOUS ses réseaux adjacents (code inchangé). (2) **§3.1 Quatre portes** `porte_nand`/
  `porte_nor`/`porte_xor`/`porte_xnor` (clones de `porte_not`, t5, `logicGate` = `nand/nor/xor/xnor`, coût
  identique) + évaluation étendue dans la boucle de propagation (NAND=NON(ET), NOR=NON(OU), **XOR/XNOR =
  PARITÉ** du nb d'entrées à 1 — seule généralisation cohérente pour 3 faces d'entrée). Sprites orientés
  déjà intégrés (build 271, pack v2.5). Gating PROVISOIRE au nœud #33 (avec les 3 portes de base ; le split
  définitif nœuds 39/40 = paliers P2/P3 viendra avec le runtime). (3) **§9** : les 8 clés `fil_logique_v1_*`
  dites manquantes (07_NES_on, 09_NO(_on), 12_SO(_on), 13_NSO(_on), 15_NESO_on) sont **déjà présentes**
  (intégrées au build 271) — vérifié, rien à faire. Validé : `node --check` (7 blocs) + tests unitaires
  des tables de vérité (AND2/OR2/NAND2/NOR2/XOR2/XNOR2, XOR3/XNOR3 parité, NOT, XNOR = égalité 2 bits) +
  Chromium boot 0 erreur JS + sprites de portes résolus. **⚠ REPORTÉ (runtime interlocké, prochain
  passage) :** §3.2 bloc émetteur enfant/parent (cycle de vie + émission de saveurs aléatoires),
  §3.3 bloc vanne + compteur `colliderConfirms` + pénalité (→ `SAVE_VERSION` 28), §3.4 jonction logique +
  split de câble, §4 Collisionneur (démarrage 10 min, sigmoïde 5 min montante/descendante, paliers
  P1/P2/P3 1→32→512→8192 MW), §5 débit Data Center (0,0625/tick ×2/niveau, bascule 4/s), §7 nœuds tech
  39/40/41, §8 tutos. Build 274→275.
  Changement 13.92 : **REMBLAI SOUTERRAIN (gaté par la foreuse) + icône tuile terrain.** `SAVE_VERSION`
  INCHANGÉ (terrain persisté via `terrainMods` existant). (1) **Remblai île 7** : `tryExtend` +
  branche `handleTap` acceptent désormais l'île 7. La roche (`water`) adjacente à un tunnel (`land`/
  `coast`) est remblayable → devient du **SOL DE TUNNEL (`land`)** (en surface : `coast`, inchangé),
  MAIS exige une **FOREUSE adjacente** (4-dir, comme le forage — « il faut la foreuse ») : garde dans
  `handleTap` (pas de panneau sans foreuse) ET dans `tryExtend` (toast « Foreuse requise à côté »).
  Payé depuis le port île 6 (`portPool(7)`→`port[6]`, résolution île courante). (2) **Icône** : le
  bouton « Remblayer » de l'`InfoPanel` (mode `extend`) affiche un **sprite de tuile terrain** (île 7 →
  `tile_i7_land`, surface → `tile_i<N>_coast`) au lieu de l'emoji 🏗️ (`iconSprite` → `<img.ui-ico>`).
  i18n toast en/es/de. Validé : `node --check` (7 blocs) + sprites présents + Chromium boot 0 erreur JS.
  Build 273→274.
  Changement 13.91 : **COUCHE LOGIQUE togglable + swap de barre d'outils.** `SAVE_VERSION` INCHANGÉ
  (`uiPrefs.logicLayer` = champ additif rétro-compatible ; absent = false). (1) **Bouton bascule** en
  HAUT À GAUCHE, sous la barre d'inventaire (`.logiclayer-btn` `left:8px;top:150px`, miroir du bouton
  souterrain ; sprite `logic_porte_and`, liseré vert `#00E5A0` + halo quand actif). État
  `game.ui.logicLayer` (persisté serialize/loadSave/newGame ; state React `logicLayer` + handler
  `toggleLogicLayer` qui ferme les panneaux + désélectionne). (2) **Affichage** (`draw`) : les blocs/câbles
  logiques (`bdef.logic||logicSource||logicSink||logicGate`) ne sont dessinés QUE quand la couche est ON ;
  en couche ON, les bâtiments NON logiques sont estompés (`ctx.globalAlpha=0.32`, reset en tête de chaque
  itération + après la passe) → la logique ressort « par-dessus l'île ». Gate PUREMENT visuel (la sim
  logique tourne toujours). (3) **Barre d'outils** (Toolbar prop `logicLayer`) : en couche ON, le menu
  Bâtiment bascule sur `LOGIC_BLOCK_GROUPS` (groupe `logic` : capteur/actionneur/portes) → onglet
  **« Bloc logique »**, le menu Réseau sur `LOGIC_WIRE_GROUPS` (`logic_wire`) → onglet **« Câble logique »**,
  l'onglet **Améliorer est verrouillé** (`tabAllowed`), Copier/Démolir restent actifs. En couche OFF, les
  éléments logiques sont RETIRÉS des menus classiques (`BUILD_GROUPS_NORMAL` sans `logic`,
  `NETWORK_GROUPS_NORMAL` sans `logic_wire`) → ils n'existent QUE via la couche. Labels i18n en/es/de
  (Logic block/Logic cable · Bloque/Cable lógico · Logikblock/Logikkabel). Validé : `node --check` (7 blocs)
  + Chromium E2E (boot 0 erreur JS ; bouton présent ; clic → `ui.logicLayer` bascule true/false ; en ON :
  onglets « Logic block »/« Logic cable », Améliorer verrouillé, bouton actif ; draw sans erreur ON et OFF).
  Build 272→273.
  Changement 13.90b : **CORRECTIF retours screenshots.** `SAVE_VERSION` INCHANGÉ (assets + CSS). (1) **Bouton
  souterrain trop haut** → `.underground-btn` `top:92px` → **`top:150px`** (clair sous la barre INVENTAIRE/
  Production). (2) **Tuiles de bordure du tunnel « plantées dans le nommage »** : 12 tuiles `i7_bord_*`
  (`coin`/`ext`/`chenal`) étaient l'ART PRÉ-v2.5 (coin/ext inversés, chenal en U 3 côtés — cf. §« Correction
  v2.5 » du README pack) car déjà présentes en jeu → non ré-injectées par le passage 13.90 (« missing » only).
  Comparaison byte-à-byte pack v2.5 ↔ jeu : **18 sprites STALE** (les 12 `i7_bord_*` + `bat_collisionneur_p1/p2`
  + `logic_porte_not_{e,n,o,s}`) → ré-inlinés en **override d'assignation** en fin de bloc `__SPRITE_DATA__`
  (dernière assignation gagne au runtime, au-dessus de l'objet littéral). Vérifié : la dernière valeur de chaque
  clé == pack. (Les `tile_i7_land/water/coast` sont IDENTIQUES pack↔jeu — le brun du tunnel est l'art voulu,
  seul le bord était faux.) Validé : `node --check` (7 blocs). Build 271→272.
  Changement 13.90 : **PATCH île 6 — pack sprites v2.5 + onglets tungstène/quantique + bouton souterrain déplacé.**
  `SAVE_VERSION` INCHANGÉ (aucun champ persisté ; assets + UI). (1) **Intégration pack `ile6 v2.5`** :
  **70 sprites** manquants inlinés dans `window.__SPRITE_DATA__[…]` (couche logique complète — portes
  AND/OR/NOT/NAND/NOR/XOR/XNOR/BUF ×orientations, senseurs plein/vide/contient/déficit/saveur, émetteurs
  α/β/γ + `_on`, vanne + `_penalite`, jonction logique NS/EO, sortie collisionneur, entrée data center ;
  data_center v1-v4 ; collisionneur `p1/p2/p3` `_boot`/`_actif` ; **flèches d'élévateur**
  `tile_elevateur_fleche_bas`/`_haut`) + **7 sheets d'anim** (`mine_tungstene_v1/v2/v3`,
  `bat_collisionneur_p{1,2,3}_boot` 256×96=4×64×96, `logic_vanne_penalite` 96×32=3×32×32) inlinées dans
  `window.__ANIM_DATA__[…]` sous la CLÉ STATIQUE + `Object.assign(ANIM_META, …)` pour les 4 nouvelles metas
  (mine_tungstene déjà présent). Frame 0 == statique (invariant du pack). **Après ce build, plus AUCUN
  sprite du pack v2.5 manquant.** (2) **Onglets `Tungstène` + `Quantique`** (demande) : les bâtiments île 6/7
  sortent de extraction/energy/steel/electronics/chemistry et forment 2 groupes `TOOLBAR_GROUPS` dédiés —
  Tungstène (`mine_tungstene`, `four_arc_tungstene`, `machine_outil`, `presse_uhp`), Quantique (`foreuse`,
  `extracteur_souterrain`, `geothermie`, `centrale_gaz`, `separateur_cryogenique`, `fab_ordi_quantique`,
  `data_center`). Labels i18n en/es/de (Tungsten/Tungsteno/Wolfram · Quantum/Cuántica/Quanten). (3) **Bouton
  souterrain déplacé** en HAUT À DROITE, sous les alertes (`.underground-btn` : `top:92px;right:8px`, fini
  `top:50%`) et rendu avec les **sprites flèche** (`tile_elevateur_fleche_bas` en surface → descendre,
  `_haut` au souterrain → remonter) au lieu des emojis ⛏️/↑ (`img.ug-arrow`). Validé : `node --check`
  (7 blocs) + 87 ids toolbar sans doublon/perte + sprites/i18n résolus. Build 270→271.
  ⚠ **Non traité dans ce passage (reportés, à confirmer)** : couche logique togglable + swap de la barre
  d'outils (bloc/câble logique), remblai souterrain gaté par la foreuse, icône tuile terrain pour le remblai,
  reset perçu de la taille des badges (chemin `uiPrefs.badgeScale` audité : serialize→loadSave→boot sync
  cohérent, aucun bug de persistance trouvé dans le code).
  Changement 13.89 : **PATCH souterrain — 3 retours (centrales, construction étalée, priorité élévateur).**
  `SAVE_VERSION` INCHANGÉ (`pl.cb` = champ additif rétro-compatible). (1) **Aucune centrale au souterrain
  hors Géothermie + Accumulateur** : `forbiddenIslands` des éoliennes / centrales charbon-diesel passe de
  `[6]` à **`[6, 7]`**, et la centrale nucléaire reçoit `[7]` → seuls la Géothermie (producteur) et
  l'Accumulateur (batterie) sont posables sur l'île 7 (gardes `canPlace`/`tryPlace` + masquage `visibleOn`).
  (2) **Construction souterraine ÉTALÉE dans le temps** (nouveau) : poser un bâtiment sur l'île 7 paie le
  coût au port de l'île 6 (réservé) puis le marque **en construction** (`bld.construction {rem, tot, rate}`,
  fantôme INACTIF) ; la matière (somme des unités du coût, `costUnits`) DESCEND par l'élévateur à
  `rem`/s → à 0 le bâtiment s'ACTIVE. Rendu : **sprite fantôme translucide clignotant + barre de
  progression + décompte** (s restantes au débit courant, ⏸ si rien ne descend) ; fiche = « en construction ·
  X% · ~Ys ». Persisté (`pl.cb`, reprend au chargement). Îles 1-6 = construction INSTANTANÉE (inchangé).
  (3) **Priorité de l'élévateur RÉORDONNÉE par CATÉGORIE** (au lieu de la priorité de flux haute/normale/
  basse) : **1) construction, 2) sortants non immédiatement consommés (dépôts au port île 6), 3) intrants**.
  Le budget `elevatorRateAt` sert la construction d'abord, puis les sortants (`elevOutFac`), puis les
  intrants (`elevInFac`). `game.elevatorFlow` ventile `{construction, out, in}` ; le panneau Élévateur
  affiche la ligne « Priorité : constr. → sortants → intrants ». Souterrain non relié (`undergroundBlocked`)
  → budget 0 (rien ne descend, construction en attente). Validé : `node --check` (7 blocs) + Chromium E2E :
  éolienne/charbon/diesel/nucléaire refusés sur l'île 7, géo/accu acceptés ; géothermie (1350 unités) posée
  → fantôme, descente linéaire 16 u/s (niv 0), finalisée à ~85 ticks, puis active ; construction préempte
  le débit (constr. 16/16) ; île 1 = pas de fantôme ; round-trip `pl.cb` ; 0 erreur console + captures
  (fantôme « 64s », sol de tunnel). Build 269→270.
  Changement 13.88 : **PATCH rendu souterrain (retour #1 « sprite mal agencé », screenshot reçu).**
  `SAVE_VERSION` INCHANGÉ (dessin seul). Le souterrain (île 7) réutilisait le système d'auto-tiling
  du LITTORAL (conçu pour terre↔mer) → l'élévateur « flottait » sur du vide et les tuiles mélangeaient
  sol/côte/triangles de transition (aspect « boîtes mal alignées »). Corrigés (branche draw île 7
  UNIQUEMENT, îles 1-6 pixel-identiques) : (1) **Sol de tunnel UNIFORME** — l'île 7 dessine toujours
  `tile_i7_land` (fini `tile_i7_coast` + `coastTransitionTri`, artefacts de rivage incohérents sous
  terre) ; les murs viennent seulement de `tunnelBorderPieces` (côté roche). (2) **Sol sous la cage** —
  l'art d'élévateur (réutilisé de l'île 6) ne remplit pas la tuile → on pose `tile_i7_land` DESSOUS avant
  la cage (fini la cage flottante). (3) Les stubs de raccordement réseau↔élévateur (13.87) sont désormais
  visibles sur ce sol continu. Validé : `node --check` (7 blocs) + Chromium E2E (île 7 : cage posée sur
  sol de tunnel, sol uniforme, stubs route/tuyau/conduit rendus ; îles 1-6 inchangées ; 0 erreur console)
  + capture. ⚠ Reste : la grille île 7 n'a que **12 tuiles de tunnel** (exiguë) — agrandissement de la
  grille / art d'élévateur souterrain dédié = piste séparée si le rendu ne suffit pas. Build 268→269.
  Changement 13.87 : **PATCH souterrain — 3 retours (inventaire île 6, connexions élévateur ; #1 signalé).**
  `SAVE_VERSION` INCHANGÉ (affichage seul ; aucune donnée persistée touchée). (1) **Construction
  souterraine = inventaire de l'île 6.** Le moteur payait DÉJÀ depuis le port de l'île 6 (`portPool(7)` →
  `game.port[6]`) et les 12 tuiles de tunnel (3 `land` + 9 `coast`, coast auto-ajouté aux bâtiments `land`)
  sont constructibles — MAIS l'UI affichait `game.port[7]` (inexistant → **inventaire VIDE**, pastilles de
  coût toutes rouges) → la construction SEMBLAIT impossible. Fix : le HUD (inventaire), la barre Bâtiment
  (affordabilité), l'`InfoPanel` (coût d'amélioration/densification) et le `NetworkPanel` (coût réseau)
  lisent désormais **`portPool(game, currentIsland)`** → sur l'île 7 ils montrent/évaluent l'inventaire de
  l'île 6. Vérifié E2E : inventaire île 7 = 36 ressources de l'île 6 ; pose d'une géothermie sur une tuile
  `coast` débitée du port île 6. (2) **Connexions VISUELLES élévateur ↔ réseaux.** L'élévateur est un
  TERRAIN (pas un bâtiment) → les tuiles route/tuyau/conduit ne dessinaient aucune branche vers lui (retour
  « pas de connexions »). Nouveau helper `elevatorEdgeMask` : une tuile route/tuyau/conduit adjacente à
  l'élévateur ajoute la branche vers lui ; et la tuile élévateur dessine des **stubs** vers chaque réseau
  voisin (route/tuyau/conduit + jonctions, PAS le câble — l'électricité ne transite pas). Raccord visuel
  continu surface ↔ souterrain (la mécanique fonctionnait déjà : `net.connected`/`elevatorSurfaceLinked`
  vérifiés). (3) **⚠ « Sprite souterrain mal agencé » SIGNALÉ, non corrigé** (screenshots non reçus) :
  l'île 7 est une grille 5×9 avec seulement **12 tuiles de tunnel** groupées autour de l'élévateur (le reste
  = roche), l'élévateur réutilise l'art `tile_i6_elevateur` de l'île 6, et `tunnelBorderPieces` est un arbre
  « best-effort ». Diagnostic à confirmer avec le nouveau screenshot : agrandir la grille île 7 (plus de
  tunnels), refaire les sprites `i7_bord_*`/`tile_i7_*`, et/ou un art d'élévateur souterrain dédié. Validé :
  `node --check` (7 blocs) + Chromium E2E (inventaire île 6 sur l'île 7 ; pose sur tunnel coast payée depuis
  port 6 ; sprites de stub présents ; 0 erreur console). Build 267→268.
  Changement 13.86 : **PATCH île 6 — 11 retours (énergie, chaleur souterraine, élévateur, UX).**
  `SAVE_VERSION` INCHANGÉ (aucun champ persisté requis ; migration additive de l'élévateur ; chaleur
  transitoire). (1) **Île 6 sans éoliennes ni centrales charbon/diesel** : nouveau flag def
  `forbiddenIslands: [N]` posé sur `eolienne`/`eolienne_offshore`/`centrale_charbon(_v2)`/
  `centrale_diesel(_v2)` = `[6]` (elle n'a que la Centrale à Gaz / Géothermie) — gardes dans `canPlace`/
  `tryPlace` + masquage dans le menu (`visibleOn`). La **tour aéroréfrigérante** = `[7]` (pas de tour au
  souterrain, cf. #7). (2) **Animations de sprite** : correctif d'un `ANIM_META` MALFORMÉ (les 3
  `mine_tungstene_v{1..3}` étaient IMBRIQUÉES dans `tile_i5_coast_tri_sw` → jamais enregistrées, mine
  figée) + **10 sheets 4 frames GÉNÉRÉS** (Pillow, frame 0 == statique vérifié 0 px, balayage lumineux
  subtil) pour `bat_machine_outil`/`bat_geothermie`/`bat_presse_uhp`/`bat_centrale_gaz`/
  `bat_fab_ordi_quantique`/`bat_data_center`/`bat_separateur_cryogenique`/`four_arc_tungstene`/
  `bat_foreuse`/`bat_extracteur` (clé anim = clé statique → `ANIM_BY_SK` résout, `bat_extracteur` couvre
  l'override `extracteur_souterrain`). (3) **`ISLAND_KICKSTART_6` élargi** : ≈ 3/4 des ressources de BASE
  pour monter le refroidissement dès l'arrivée (1 centrale nucléaire + 2 tours + pompe V2 Nv.12 + 4
  conduits) → acier 6000, béton armé 12000, proc 600, pièce méca 6000, lingot fer 3000, lingot cuivre
  3000, ciment 1500, polymère 150 (+ irradiés existants). (4) **Ressources île 6/7 en catégorie T5** :
  ajout `RES_TIER` `t5` (tungstène → quantique → hélium) + `RES_TIER_RANK.t5` + `RES_TIER_LABEL.t5` → fini
  le 2e « T0 » fourre-tout en bas de l'inventaire. (5) **Conduit de chaleur bloqué en sprite V3** (comme
  route/tuyau/câble) : `net.unlimited ? 4 : min(lvl, 3)` aux 3 spots de dessin (draw conduit, stub sous
  bâtiment, vignette du NetworkPanel) — V4 réservé à l'ILLIMITÉ (débit infini, prévu plus tard ; le
  conduit n'a pas encore de matériau irradié → `unlimited` toujours faux). (6) **Machine-Outil ET Presse
  UHP génèrent de la chaleur EN FONCTION DE LEUR CONSO** (comme l'usine moteur nuc) : `heatCap: 10` +
  `heatEmit = HEAT_PER_MW × (power × regime)/1000` → trip après 60 s de chaleur cumulée (heatCapOf commun).
  Vérifié moteur : Machine-Outil pleine charge = 0,128 MJ/s exact. (7) **L'élévateur fait TRANSITER la
  chaleur** (île 6 ↔ 7) : la Presse UHP souterraine n'a pas de tour sur place → sa chaleur remonte par la
  cage. `rebuildNetworks` marque `net.elevatorLinked` sur un conduit touchant la tuile élévateur (île 6 ou 7) ;
  `processHeat` ajoute un tampon partagé `game.elevatorHeat` (MJ, borné par `elevatorRateAt`) : île 7 = les
  conduits élévateur-liés y DÉPOSENT (tour virtuelle), île 6 = les tours élévateur-liées le REFROIDISSENT
  (source virtuelle). Sans tour de surface → le tampon sature → le souterrain surchauffe. Vérifié E2E :
  chaleur Presse → tampon → tour de surface (drain 1,024 MJ/tick à V1). (8) **Bâtiment en PAUSE évacue
  encore sa chaleur** : `processHeat` garde les SOURCES en pause/logicOff dans la liste (émission déjà 0,
  mais la chaleur accumulée continue de se vider vers les tours ; seules les TOURS en pause sont sautées).
  Vérifié : 4 → 2,976 MJ après un tick. (9) **Nœud #32 (Câble Supra) instantané** : déjà `mode:'auto'`
  `reqs:[]` (aucune demande de 100 câble supra) — état confirmé. (10) **Réparation élévateur automatique** :
  la RECHERCHE #31 (livraison) EST le paiement → `applyUnlocks` pose `elevatorRepaired`/`islandUnlocked[7]`
  et reconstruit les réseaux 6/7 dès la confirmation (fini le 2e paiement sur la tuile) ; migration `loadSave`
  (nœud #31 confirmé mais pas encore réparé → réparé au chargement). (11) **Bouton flottant surface ↔
  souterrain** (`.underground-btn`, côté droit, visible sur l'île 6/7 quand l'île 7 est débloquée) : bascule
  6 ⇄ 7 (« ⛏️ Souterrain » / « ↑ Surface ») — plus besoin de chercher l'onglet île 7. Validé : `node --check`
  (7 blocs) + Chromium E2E (données ci-dessus ; transit de chaleur réel via `__gameRef` ; pause qui refroidit ;
  bouton souterrain rendu ; 0 erreur console). Build 266→267.
  Changement 13.85 : **PATCH — pack sprites île 6 v2.2 + 5 retours.** `SAVE_VERSION` INCHANGÉ (terrain
  reconstruit depuis la def ; le reste = data/affichage additifs). (1) **Sprites OFFICIELS** (pack
  `Archipel_sprites_ile6_v2.2`) : `ile_6`/`ile_6_gris` (64×64) remplacent les icônes générées ; le
  **Collisionneur cassé** utilise `bat_collisionneur_ruine` (64×96 TOURNÉ en paysage 96×64) → bloc terrain
  **3 larges × 2 hauts** (au-dessus de l'élévateur), sprite découpé en 6 tuiles `tile_i6_collisionneur_0..5`
  (sous-index = nord*3 + ouest) ; **réseau logique** enfin en sprites : câble `fil_logique_v1_<NN>_<LETTRES>` (+`_on`
  si 1, 32 sprites, repli vectoriel) et dispositifs `logic_porte_{and,or,not}_{n,e,s,o}` /
  `logic_senseur_{plein,vide,contient}_{n,e,s,o}` (mode capteur full→plein/empty→vide/active·inactive→contient)
  / `logic_sortie_collisionneur_{n,e,s,o}` (actionneur) — orientation encodée dans le sprite (DIRS4 [N,S,O,E]
  → suffixe), la pastille d'état 0/1 reste, la flèche vectorielle ne s'affiche qu'en repli. (2) **Menu
  Bâtiment : bouton « tout replier / tout déplier »** (`.bp-collapse-all`, dans la barre de recherche) —
  bascule l'état `collapsed` de TOUS les groupes de `BUILD_GROUPS` d'un coup (⊟ replier / ⊞ déplier). (3)
  **Coût transit 5↔6** : `PORT_BASE_COST[5]` béton irradié 10000 → **1000** (÷10 ; pièce méca 10000 inchangé).
  (4) **Déblocage île 6** (nœud #28 delivery) : ancien barème (10000 élém. moteur + 100000 acier/câble irr.)
  → **ressources de base de l'île 5** (= ISLAND_KICKSTART_5 inliné) **+ 500 de chaque irradié** (acier/béton/
  câble). (5) **Port île 6 non améliorable** : l'île 6 n'a pas de `PORT_BASE_COST` (dernière du chemin
  maritime, l'île 7 est souterraine) → la section « Amélioration du transit » du Port est masquée pour une
  île sans barème + garde dans `upgradePort` (fini l'amélioration « gratuite » à vide). (6) **Mine Tungstène :
  `power` 2048 → 512 kW** au Nv.1. Validé : `node --check` (7 blocs) + Chromium headless (0 erreur) : rendu
  île 6 = collisionneur officiel 2×3 au-dessus de l'élévateur + icône d'onglet ; réseau logique forgé
  (capteur/AND/actionneur + câbles) rendu en sprites ; bouton tout replier/déplier bascule les groupes ;
  Port île 6 sans section d'amélioration. Build 265→266.
  Changement 13.84 : **PATCH testeur — 10 retours (île 6, antenne, inventaire, décompte).** `SAVE_VERSION`
  INCHANGÉ (aucun champ persisté ajouté ; terrain reconstruit depuis la def ; déblocage additif). (1)
  **Inventaire ouvert = SUPERPOSITION** (ne pousse plus la scène vers le bas) : le HUD + la barre
  d'inventaire sont enveloppés dans un `.hud-stack` (`position:relative`) et `.inventory.open` passe en
  `position:absolute; top:100%` (au-dessus de la carte, sans occuper de hauteur de layout ; z-index 40,
  max-height 65vh, scroll). (2) **Antenne — aperçu d'amélioration BOOST corrigé** : la ligne « Boost » de
  l'aperçu affichait le facteur de ZONE brut (×4 → ×8) au lieu de l'effet EFFECTIF `antSpeedMul` → désormais
  « ×1,2 → ×1,4 » (`fxDec(antSpeedMul(2^(upg+1)))` etc.), cohérent avec la ligne « Effet ». (3) **Icône île 6** :
  sprites `ile_6`/`ile_6_gris` GÉNÉRÉS (recolorés depuis `ile_5` vers le bleu-gris de l'île 6 ; `_gris` =
  silhouette grise réutilisée) → l'onglet île 6 affiche une icône (fini le « 6 » de repli). (4) **Décompte
  inventaire = flux RÉEL** : le popover ressource (clic sur une ressource) lit désormais `islandFlowAgg`
  (prod/conso réelles du dernier tick via `game.netFlow`) au lieu du recalcul statique `resourceRates` (repli
  si flux nul) → la production des **fours à ARC** (sortie effective hors `outputs` statiques) ET le **boost
  d'ANTENNE** (×antSpeedMul) apparaissent enfin. (5) **Ordinateur quantique masqué** : `fab_ordi_quantique`
  ajouté aux `unlocks.buildings` du nœud **#37** (« Ordinateur Quantique ») → `ordinateur_quantique` n'est
  plus « débloqué » d'emblée (n'apparaît plus dans l'inventaire avant #37). (6) **Coût amélioration PORT 5**
  → `beton_arme_irradie: 10000 + piece_meca: 10000` (remplace acier/béton armé/câble 10000). (7) **Île 6 —
  REMAP terrain** : puits de pétrole (P) → accidenté (obstacle) ; accidenté (O) → mine (`resource`) ; les
  **6 mines du haut** → **Collisionneur cassé** (nouveau terrain `collider`, char `K`, `TERRAIN_COLORS.collider`,
  non constructible/non circulable). Sprite = tranche 3×2 d'un anneau métallique endommagé
  (`tile_i6_collisionneur_0..5`, généré) ; branche de rendu dédiée (sous-index = balayage voisins dr/dc,
  indépendant de la position). (8) **Falaises île 6** : sprites `i6_falaise_*` (11) GÉNÉRÉS (recolorés depuis
  l'île 5 vers un rocher ardoise pourpre sombre contrastant avec l'eau bleue ; écume cyan préservée) →
  l'île 6 (et l'extension de terrain water→coast, retour #6) affiche enfin des falaises. Validé : `node
  --check` (7 blocs) + Chromium headless (boot 0 erreur hors fetch offline ; terrain île 6 remappé vérifié
  via `__gameRef` — collider/resource/obstacle aux bonnes tuiles ; rendu île 6 : anneau collisionneur cassé
  continu + falaises + icône d'onglet ; inventaire ouvert `position:absolute` ne pousse plus la scène ;
  19 sprites générés présents/décodés). Build 264→265.
  Changement 13.83 : **RÉSEAU LOGIQUE — Phase 5A (brief `BRIEF_ILE6_PHASE5`).** (0) **2 correctifs phase 4** :
  (a) Séparateur Cryogénique `ordinateur_quantique:10` → **`processeur:100`** (lève la dépendance circulaire
  dure) ; (b) **type `reqs` `resourceTile`** (compte les tuiles `resource` d'une île, défaut 7) → **#36
  « Trouver de l'Hélium »** exige désormais **≥1 tuile forée sur l'île 7** (fini le `reqs:[]` auto-validant ;
  un #36 déjà confirmé en save ne régresse pas, les nœuds confirmés ne se réévaluent pas). (1) **5ᵉ réseau
  `logic_wire`** (kind infra, flag `logic:true`, patron du conduit) : booléen 0/1 par réseau connexe, **OU
  câblé** (1 si ≥1 source à 1), **instantané, sans mémoire** (recalculé chaque tick par `processLogic`,
  appelé en tête de `tickIsland`). NON améliorable (`networkUnitCost` vide), **local à l'île** (jamais
  port/élévateur/bateaux — vérifié), pas de jonction. Débloqué par **#32** (`unlocks.buildings +=
  logic_wire/capteur/actionneur`). Dessin vectoriel vert **vif (#00E5A0) si 1 / terne (#0d5a45) si 0**.
  (2) **Capteur** (`logicSource`) : observe **UN** voisin choisi au clic (`sensorDir`, §5.1 — option
  « un seul voisin explicite » retenue) ; conditions **full/empty/active/inactive** (`sensorMode`).
  ⚠ `full` = 1ʳᵉ ressource de sortie du voisin ≥ sa **cible d'export** (`stockCible`) au port (le moteur
  ne modélise pas de plafond de stock → la cible = le « réservoir » ; documenté). **Actionneur**
  (`logicSink`) : lit le réseau adjacent (OU) → pose **`bld.logicOff`** (jamais `bld.active`, §5.2) sur le
  voisin ciblé (`actDir`), **polarité inversable** (`actInvert`). (3) **`bld.logicOff`** traité EXACTEMENT
  comme une pause dans la boucle bâtiment (active=false, regime=0, heatEmit=0, **`discReason:'logic'`**) —
  l'actionneur n'écrit JAMAIS `active`. **Sites `active===false`/`paused` doublés par `logicOff`** :
  boucle bâtiment (nouveau bloc, mirroir de `paused`), pré-pass antenne, `processHeat` (skip tour + liste
  towers), `islandNuclearCoolingOk`. Les sites LECTURE aval (`active===false` : boucle énergie, draw,
  InfoPanel) héritent automatiquement (le bloc pose `active=false`). (4) **Portes AND/OR/NOT**
  (`logicGate`) : **face de sortie orientée** (`gateDir`, pivot au clic — modèle validé avec l'utilisateur,
  réutilise l'esprit des jonctions) ; entrées = faces NON-sortie adjacentes à un réseau logique ; sortie =
  face orientée. **Évaluation itérative** jusqu'à stabilisation, plafond **`LOGIC_MAX_ITER=16`** (coupe les
  oscillations, garde la dernière valeur). NOT = NON(OU des entrées). (5) **Tech** : **#33** validé par
  compteur **`game.techTree.logicTriggered`** (type `reqs` `logicTrig`, incrémenté à la 1ʳᵉ extinction par
  actionneur) → débloque les 3 portes ; **#34** = ≥1 porte construite (type `reqs` `buildAny` — ⚠
  approximation : ne vérifie pas que le montage passe PAR la porte, signalé) → unlocks vide (phase 6).
  (6) **UI** : panneau d'info par dispositif (capteur : 4 conditions + cible + signal émis ; actionneur :
  cible + polarité + reçu→action ; porte : face de sortie + sortie) via `setLogicConfig` (patch/cycle
  d'orientation) ; flèche d'orientation + pastille d'état sur la carte. **Migration `SAVE_VERSION` 26→27**
  (+27 whitelist) : `logicTriggered` (techTree) + réglages d'instance (`sm/sd/ad/ai/gd`) sérialisés/
  restaurés (`logicOff` NON persisté, recalculé au 1er tick). **HORS périmètre (reportés)** : saveurs
  d'information quantique + tri (phase 5B), capteurs avancés déficit/saveur (5B), Collisionneur (6).
  ⚠ **Rétroaction** (capteur observant le bâtiment que l'actionneur éteint) : oscille chaque tick — voulu,
  AUCUNE détection de boucle, vérifié ne plante/diverge pas. Validé : `node --check` (7 blocs) + Chromium
  E2E (~40 assertions) : correctifs (sep coût, resourceTile #36) ; unlocks #32/#33/#34 ; **capteur→câble→
  actionneur éteint la cible** (logicOff/active=false/disc 'logic') + rallume ; polarité inversée ;
  **étanchéité inter-îles** ; **tables de vérité AND[0001]/OR[0111]/NOT[10] exactes** + cascade NOT(NOT(x))=x
  + plafond d'itérations ; **rétroaction 200 ticks sans crash/divergence** ; non-régression grilles 1-7
  phase4↔5 ; saves v22→v27 + round-trip des réglages logiques ; boot 2 modes 0 erreur. Build 263→264.
  Changement 13.82 : **ÎLE 6 / SOUTERRAIN — Phase 4 (chaîne He3 + quantique ; brief `BRIEF_ILE6_PHASE4`).**
  (1) **Azote stockable au port, PAS transitable** : `PORT_PIPE_RES += azote` (ancre D1, 1 ligne) → l'azote
  (carrier `pipe`) est stocké au PORT (chemin `pipePort`) donc traverse l'élévateur, MAIS reste hors
  `TRADE_LIQUIDS`/`TRADE_RESOURCE_SET` → **jamais transité par bateau** (décision testeur maintenue,
  `rawShippable(g,6,5,'azote')===0` vérifié). Migration D8 (pools tuyau → port) déjà générique sur toute
  clé `PORT_PIPE_RES` → couvre l'azote (commentaire MAJ). Oxygène NON touché (reste pool local île 6).
  (2) **5 bâtiments** (t5, style `separateur_air`/`presse_uhp`/`geothermie`) : `extracteur_souterrain`
  (île 7, sur poche He3 `resource` ; acide 8 + eau 16 → gaz_fossiles 1 ; power 0), `separateur_cryogenique`
  (île 7 ; multi-sortie sur ratios 1 gaz_fossiles + azote 16 → helium3 0,01 + helium4 0,1 + methane 0,89 ;
  sigmoïde 128→1024), `centrale_gaz` (**île 6 SURFACE** — arbitrage concepteur ; methane 8 + oxygene 64 →
  512 kW ; producteur power:0), `fab_ordi_quantique` (île 6 ; câble supra 16 + proc 8 + lingot_or 64 →
  ordinateur_quantique 0,01 ; sigmoïde 1024→8096), `data_center` (île 6 ; proc 1 + azote 1024 + helium4 8 →
  information_quantique 1 ; **power:0 = énergie absente de l'Excel, non inventée**). Toolbar ×5 ;
  `BLD_SPRITE_OVERRIDE.extracteur_souterrain = 'bat_extracteur'` (le fichier livré n'a pas le suffixe
  `_souterrain`) ; les 4 autres résolvent `bat_<id>`. (3) **Tech tree renuméroté sur l'Excel v2** :
  l'ancien **#32 « Forage Profond → foreuse »** est remplacé par **#32 Câble Supraconducteur** (case vide) ;
  ajout **#33/#34 Circuit Logique 1/2** (cases vides PHASE 5), **#35 Réparation du Collisionneur** (livraison
  10000 béton irr. + 10000 alliage + 10000 câble supra → **foreuse**), **#36 Trouver de l'Hélium** →
  extracteur + séparateur cryo, **#37 Ordinateur Quantique** (produire 100 helium4) → centrale gaz +
  data center, **#38 Data Center** (produire 1 ordinateur_quantique, unlocks vide PHASE 6). LOCALES `tech`
  ×4 langues (32→38). `BUILDING_NODE` auto-dérivé → foreuse=35/extracteur=36/gaz=37 sans câblage manuel.
  Les cases vides (`reqs:[]`, mode auto) atteignent `condition_ok` immédiatement (1 clic, ne bloquent pas
  la chaîne #31→#38). (4) **Migration `SAVE_VERSION` 25→26** (+26 whitelist) : si l'ancien #32 (Forage)
  était confirmé, on marque **#32/#33/#34/#35 confirmés** → la **foreuse reste débloquée** (livraison #35
  NON re-exigée) ; #36-#38 restent `locked` (promus normalement). ⚠ **BLOCAGES SIGNALÉS (Excel prime,
  implémenté tel quel, arbitrage playtest)** : (a) **dépendance circulaire DURE** — le Séparateur Cryo
  coûte **10 ordinateurs quantiques**, or l'Ordi Quantique (Fab) est débloqué par #37 qui exige « produire
  100 helium4 » = avoir déjà un Séparateur → **le 1er Séparateur est inconstructible** (à trancher) ;
  (b) **#35 = 10000 câble supra** (seule source : Presse UHP à 1 supra/s partagé avec le débit élévateur)
  → ~10000 s de jeu MINIMUM (bien plus avec le partage) = atteignabilité très longue ; (c) **incohérence
  foreuse/collisionneur** — #35 (Collisionneur) débloque la foreuse qui sert à forer l'He3 qui alimente le
  Collisionneur ; (d) **helium = helium4** (interprétation §3/§6) ; (e) **Data Center sans énergie**
  (colonne Excel vide → power:0, non inventé) ; (f) **saveurs d'information quantique** et **peaker de la
  Centrale à Gaz** REPORTÉS (phase 5). ⚠ **#36 « trouver une tuile resource île 7 »** : AUCUN type de
  `reqs` existant (produce/build/node/port/energy/imported/accu) ne l'exprime → **fallback `reqs:[]`**
  (auto-valide dès #35, ne bloque pas ; proposition : type `resourceTile` en attente d'arbitrage §6).
  Validé : `node --check` (7 blocs) + Chromium E2E (2 suites, ~55 assertions) : boot 2 modes + 5 defs/
  sprites ; azote au port + `rawShippable===0` + oxygène local ; migration azote v25 pool→port ; chaîne
  souterraine réelle (extracteur → gaz 1/s & acide 8/s ; séparateur ratios **1:10:89 EXACTS**, accumulation
  0,01/s exacte, azote 16/s) ; chaîne surface (centrale gaz **512 kW** + méthane 8/oxygène 64 ; fab ordi
  0,01/s ; data center info 1/s + azote 1024/helium4 8) ; **débit élévateur** (demande 18 > cap 16 →
  flux bridé PROPORTIONNELLEMENT à 16/18, PAS de blocage dur) ; tech chaîne #31→#38 (cases vides
  condition_ok, #35→foreuse, #37→gaz/data) ; migration tech v25 (foreuse conservée, 38 nœuds) ;
  non-régression grilles îles 1-7 IDENTIQUES phase 3↔4 ; saves v22→v26 round-trip ; 0 erreur console.
  Build 262→263.
  Changement 13.81 : **ÎLE 6 / SOUTERRAIN — Phase 3 (l'élévateur : transferts au port île 6, débit
  borné ; brief `BRIEF_ILE6_PHASE3`).** Le souterrain (île 7) devient une extension de l'île 6 reliée
  par un « tuyau » de capacité finie. (1) **portPool(7)** renvoie désormais `game.port[6]` quand
  l'élévateur est réparé (sinon `{}` jetable) → le tick de l'île 7 puise/dépose dans le port de l'île 6
  (aucun `game.port[7]` jamais créé). L'île 6 étant tickée AVANT l'île 7 (`for def of ISLAND_TERRAINS`),
  l'île 7 lit l'état À JOUR du port (vérifié : consommation même tick). (2) **Connexion physique** :
  `rebuildNetworks` — cible de connexion = le PORT (îles 1-6) / la TUILE ÉLÉVATEUR de l'île 7 (si réparé) ;
  un réseau route/tuyau ADJACENT à la cible OU PASSANT PAR elle (infra sur la tuile élévateur — le cas
  « sur la tuile » ne se produit jamais sur un port → îles 1-6 intactes) devient `connected` → réutilise
  la bascule pipePort/road existante. (3) **§4.2 option (a) retenue** : le souterrain n'est alimenté que
  si la tuile élévateur de l'ÎLE 6 est reliée par route/tuyau au port en surface (`elevatorSurfaceLinked`)
  — donne un rôle à la tuile élévateur de surface (tracé port→élévateur à construire). Gating dans le
  tick (`undergroundBlocked`). (4) **Débit borné** : `ELEVATOR_BASE_RATE=16` (×2/niveau, `elevatorRateAt`)
  = enveloppe PARTAGÉE bornant la somme de TOUT le port I/O (road + pipePort, intrants + sortants) × régime,
  tous sens confondus. Allocation par priorité de flux (haute→normale→basse, même schéma que la saturation
  réseau) → `elevFacOf(pri)` multiplie le flush port de l'île 7 (= 1 ailleurs, non-régression). `game.elevatorFlow`
  {demand, cap, used} pour l'UI. (5) **Amélioration** : `game.elevatorLevel` (défaut 0), `ELEVATOR_BASE_COST`
  (×2/niveau, payé depuis le port île 6) ; action `tryUpgradeElevator` ; panneau élévateur DÉDIÉ (au clic
  sur la tuile réparée) = niveau, débit consommé/max en temps réel, débit suivant, coût, bouton améliorer
  (`handleTap` ouvre le panneau réparé OU cassé). (6) **Motif de déconnexion DÉDIÉ `'elevator'`** (remplace
  l'approximation 'road' de la phase 2) : élévateur non réparé / non relié en surface / bâtiment non relié
  à la tuile élévateur ; `DISC_LABELS.elevator`. (7) **Électricité NE PASSE PAS** (vérifié) : `energie_kw`
  est `wire`, le tick élec. est par île, jamais dans road/pipePort ; couper la géothermie île 7 arrête les
  bâtiments souterrains même si l'île 6 est excédentaire. Les `NON_STORABLE` ne transitent jamais (déjà
  exclus de pipePort). (8) **Migration `SAVE_VERSION` 24→25** : `elevatorLevel` sérialisé/restauré (défaut
  0) ; whitelist +25. **HORS périmètre (dette assumée)** : construction ralentie par le débit (§7 — le jeu
  n'a aucune notion de construction étalée dans le temps ; système à part entière, phase dédiée). **Valeurs
  à playtester** : débit de base 16/s (une presse tourne à ~94 % au niveau 0 — plafond ressenti d'emblée,
  voulu) ; coûts d'amélioration élévateur ; coûts réparation/forage hérités phase 2. Validé : `node --check`
  (7 blocs) + Chromium E2E `https://localhost/` (~40 assertions, 2 suites) : non-régression grilles îles 1-6
  (2 modes) ; ordre de tick (consommation port[6] MÊME tick) ; chaîne réelle presse UHP (route surface
  port→élévateur + presses reliées à la tuile élévateur i7 + géothermies → consomme câble irradié de port[6],
  y dépose supra) ; **enveloppe plafonnée EXACTEMENT** (2 presses/34 demandé → 16/s au niveau 0, 32/s au
  niveau 1) ; **priorité respectée** (haute servie, basse coupée) ; étanchéité électrique (géothermie coupée
  → presse s'arrête) ; élévateur non réparé → isolement total, `port[7]` absent, 0 exception ; amélioration
  VIA CLIC UI (niveau +1, coût 250/500/2500 débité port 6) ; flux transit 5→6 et consommation four_fer île 1
  IDENTIQUES phase 2 vs phase 3 ; migrations v22/v23/v24/v25 sans perte (`elevatorLevel` persisté) ; 0 erreur
  console. Build 261→262.
  Changement 13.80 : **ÎLE 6 / SOUTERRAIN — Phase 2 (logistique 5↔6, réparation élévateur, foreuse He3 ;
  brief `BRIEF_ILE6_PHASE2`).** (1) **Logistique 5↔6** : `SHIP_LINKS += '5-6'` (activation auto via
  `linkActive` au déblocage île 6, aucun gating ajouté) ; `transitForwardBudget` — bornes de chaîne
  linéaire `5 → 6` (2 spots : `nextI > 6`, `while i <= 6`) → l'île 6 reçoit le transit relais ;
  `defaultShips` crée `ships['5-6']` (migration auto) ; kickstart île 6 RÉDUIT (acier/béton/câble irr.
  500, pièce méca 250 ; `element_moteur_nuc` retiré — transite désormais). L'acide (déjà dans
  `TRADE_LIQUIDS`) transite par mer 5→6 et alimente les mines via la bascule pipe→pipePort existante —
  **le blocage acide de la phase 1 est LEVÉ.** (2) **Réparation élévateur** : état de PARTIE
  `game.elevatorRepaired` (bool, pas de terrain distinct) ; **nœud #31** « Réparation de l'Élévateur »
  (`mode:'delivery'` — structure `delivery:{piece_precision:2000, beton_arme_irradie:20000}` alignée sur
  #21/#28, PAS un `reqs:[{t:'deliver'}]` inexistant ; `unlocks.elevatorRepair` + geothermie + presse_uhp)
  + LOCALES ×4 ; `isElevatorRepairUnlocked` (flag générique `isTechFlagConfirmed`) ; action
  `tryRepairElevator` (coût FIXE 500 p.précision / 1000 alliage / 5000 câble irr. depuis le port île 6,
  bypass dev, une seule fois → `elevatorRepaired=true` + `islandUnlocked[7]=true`) ; rendu élévateur
  CONDITIONNEL (`tile_i6_elevateur` réparé / `_casse` sinon). (3) **Ouverture île 7 au joueur** : onglet
  `IslandSelector` + `switchIsland` gatés sur `islandUnlocked[7]` (posé à la réparation) au lieu du flag
  dev (bypass dev conservé) ; 7 onglets tiennent à 390px (flex+min-width:0, vérifié, pas d'overflow).
  (4) **Foreuse + He3** : bâtiment `foreuse` (t5, exclusiveIsland 7, power 512, sans I/O — sprite
  `bat_foreuse`) + toolbar ; **nœud #32** « Forage Profond » (`prereq 31`, produce **`piece_precision`
  500** — ⚠ le brief proposait `cable_supraconducteur` INATTEIGNABLE en phase 2, option (a) retenue) +
  LOCALES ×4 ; `game.he3Deposits` = 3 tuiles `land` île 7 (hors élévateur, coords paddées) générées UNE
  fois par `generateHe3Deposits` (Fisher-Yates dans `ensureIslandDefaults`) puis PERSISTÉES ;
  `game.drillsCount[7]` ; `drillCost(n)` = ×4/cran (100 p.précision + 500 câble irr.) ; action `tryDrill`
  (foreuse 4-adjacente requise, révèle un gisement → terrain `resource`/`tile_i7_resource`, sinon rien ;
  coût payé & compteur incrémenté dans les 2 cas ; `t.drilled` interdit le re-forage). (5) **Port île 7
  ABSENT préservé** : `portPool` + la passe chaleur renvoient un tampon JETABLE pour l'île 7 (jamais
  `game.port[7]`) → le tick des bâtiments souterrains tourne sans créer de port (aucun dépôt utile en
  phase 2). (6) **UI terrain** : `InfoPanel` (branche répare/remblai) GÉNÉRALISÉE aux modes `elevator`
  et `drill` (cost/count/unlocked/onAct mode-aware, ligne compteur masquée pour l'élévateur) ;
  `handleTap` ouvre le panneau élévateur (tuile `elevator` non réparée) et forage (île 7, `land` vierge,
  foreuse adjacente) ; props `onRepairElevator`/`onDrill`. (7) **Migration `SAVE_VERSION` 23→24** :
  `elevatorRepaired`/`he3Deposits`/`drillsCount` sérialisés + restaurés AVANT `ensureIslandDefaults` (pas
  de régénération des gisements) ; tuiles `drilled` persistées par île (comme terrainMods) ; whitelist
  +24. **Décisions à arbitrer (rapport)** : (a) **forage payé depuis le PORT DE L'ÎLE 6** — l'île 7 n'a
  pas de port, le brief n'a pas fixé la source ; `missingFor`/`pay`/`refund` reçoivent un param île
  optionnel ; (b) **presse UHP sur l'île 7 → `discReason='road'`** (pas `'input'` comme prévu au brief) :
  sans port souterrain, sa sortie route ne peut être déposée → déconnexion route AVANT le contrôle
  d'intrant ; les deux = « logistique manquante » (état voulu, résolu phase 3) ; (c) nœud #32 prereq (cf.
  supra) ; (d) coûts réparation/forage = premières estimations non playtestées. Validé : `node --check`
  (7 blocs) + Chromium E2E `https://localhost/` (~50 assertions, 2 suites) : non-régression grilles îles
  1-6 identiques (2 modes) ; boot 2 modes + tick île 7 sans port (port[7] absent) ; données (SHIP_LINKS,
  nœuds 31/32, foreuse, geothermie gatée par #31, kickstart réduit, drillCost ×4) ; transit réel acide+
  acier 5→6 (`tickShips`) ; chaîne mine tungstène alimentée en acide → produit (plus de blocage) ;
  réparation élévateur VIA CLIC UI (coût débité port 6, île 7 débloquée + onglet, sprite réparé) ; forage
  VIA CLIC UI (gisement→resource, coût port 6, `drilled` marqué, compteur) ; migration v22/v23 sans perte
  (îles 6/7, ship 5-6, port[7] absent) ; round-trip v24 (gisements/drillsCount/drilled/elevatorRepaired
  restaurés exact) ; presse UHP non fonctionnelle ; 7 onglets à 390px sans overflow ; 0 erreur console.
  Build 260→261.
  Changement 13.79 : **ÎLE 6 (surface) + SOUTERRAIN (île 7) — Phase 1 (fondations : terrain, ressources,
  5 bâtiments, tech, sprites, migration ; brief `BRIEF_ILE6_PHASE1`).** PÉRIMÈTRE = poser le terrain,
  déclarer le contenu et intégrer les sprites AVANT le système d'élévateur/transfert (phase 2). (1)
  **Terrain** : nouveau code `E` = terrain `'elevator'` (`charToTerrain`) — circulable par les RÉSEAUX
  (boucle post-`BUILDINGS` ajoutant `'elevator'` aux `terrains` des infra/jonctions), JAMAIS
  constructible (aucun bâtiment ne le liste) ; `TERRAIN_COLORS.elevator`. La promotion `land→coast` de
  `buildIslandTiles` ne touche pas l'élévateur (elle ne traite que `land`). (2) **Grilles** : île 6
  (16×16, port maritime `X` r13c5, gisement tungstène 6 `M`, élévateur `E`, 2 `P` réservés au
  Collisionneur phase 6) + île 7 (souterrain, 5×9, 12 tunnels + 1 élévateur, PAS de port) ajoutées à
  `NORMAL_ISLANDS` ET `ISLAND_TERRAINS_BASE` ; `PORTS_BASE[6]` ; garde `if (base)` dans `applyGameMode`
  (île 7 sans port → `base` undefined, `portPosFor` renvoie déjà `null`, 2 sites d'appel sûrs). (3)
  **11 ressources** déclarées (`CARRIER_BY_RES` + `RES_SHORT`) : tungstène/alliage/pièce précision/câble
  supra (produites en phase 1) + ordi & info quantiques, matière exotique, gaz fossiles, He3/He4, méthane
  (phases futures). (4) **5 bâtiments** (`tier: 't5'` → pas de surcoût `TIER_COST_MULT`) : `mine_tungstene`
  (surface, exclusiveIsland 6, multi-sortie tungstène+pierre, conso acide), `four_arc_tungstene`,
  `machine_outil` (surface), `geothermie` (souterrain, PRODUCTEUR — `power:0`+`outputs.energie_kw:512`,
  convention producteurs) et `presse_uhp` (souterrain, sigmoïde 128→1024 = `base:128,amp:896`) +
  `TOOLBAR_GROUPS`, `TIER_ACCENT.t5`. (5) **Tech** : nœud 28 « Navire Futuriste » (doublon île 5 inerte)
  repointé → **« Accès Île 6 »** (`unlocks.islands:[6]`+`mine_tungstene`, LOCALES tech 28 réécrit ×4
  langues) ; nœuds **29** (Four à Arc Tungstène, produire 100 tungstène) et **30** (Machine-Outil, 100
  alliage) ajoutés (+ LOCALES) ; `ISLAND_ACCESS_NODE[6]=28` ; kickstart île 6 (SANS acide, cf. blocage).
  (6) **Rendu** : branche `elevator` du draw = `tile_i6_elevateur_casse` (phase 1 : cassé) ; helper DÉDIÉ
  `tunnelBorderPieces` (miroir `coastFoamPieces`, prédicat roche=water, pièces `i7_bord_*` sur la tuile de
  sol) routé quand `isl===7` ; écume/falaise maritimes désactivées sur l'île 7 (roche). `coastCliffPieces`
  INTACT (îles 1–6 pixel-identiques). (7) **Onglets d'île** : île 7 filtrée (souterrain, accès par
  l'élévateur en phase 2) ; **accès DEV** temporaire (île 7 visible + cliquable sous `game.ui.dev`,
  contournement dans `switchIsland`). (8) **Sprites** : 69 fichiers `sprites/` + 3 sheets d'anim
  `mine_tungstene_*` (frame 0 == statique vérifié 0 px) inlinés ; `ANIM_META` ×3 ; résolution auto
  `tile_iN_*` / `item_*` / `bat_*`. (9) **Migration `SAVE_VERSION` 22→23** : `ensureIslandDefaults(g)`
  (idempotent, appelé en newGame/chooseMode/loadSave) garantit les structures par île 6/7 (islands via
  `buildIslandTiles`, `port[6]={}` SANS `port[7]`, islandUnlocked/repairs/extensions 6-7 par défaut) ;
  whitelist `loadSave` +23 ; serialize (spreads par île) persiste 6/7 automatiquement. Validé :
  `node --check` (7 blocs) + Chromium E2E `https://localhost/` (~35 assertions) : non-régression grilles
  îles 1-5 identiques (2 modes) ; terrain 6/7 (dims paddées 32×32 / 21×25, 6 resource + 2 oil + 1 elevator
  île 6 non promu en coast, 12 land + 1 elevator île 7, `PORTS[6]` défini, `PORTS[7]` undefined sans throw,
  2 modes) ; 5 sprites + 11 ressources + nœuds 28/29/30 + geothermie producteur + presse sigmoïde ; save
  v22 forgée → migre sans perte (bâtiment île 1 + stock préservés, îles 6/7 créées, `port[7]` absent) ;
  `tunnelBorderPieces` → 12 clés `i7_bord_*` toutes présentes dans `__SPRITE_DATA__`, aucune sur la roche ;
  0 erreur console (hors fetch offline). **⚠ BLOCAGES/TENSIONS SIGNALÉS (non tranchés, cf. brief) :**
  (a) **acide non transitable** — la Mine Tungstène consomme 16 acide/s mais l'acide est un fluide `pipe`
  hors `PORT_PIPE_RES` → ni transitable ni dans le kickstart → la mine est posable mais NON alimentée ;
  (b) **pas de liaison maritime 5↔6** (`SHIP_LINKS` inchangé) → l'île 6 n'importe rien en phase 1 (amorcée
  par kickstart seul) ; (c) **chaleur Machine-Outil/Presse** non branchée (ratio Excel « mj selon ratio
  elec » non déterminé → pas de `heatCap`, option offerte par le brief) ; (d) **géothermie/presse sans
  nœud tech** → toujours « unlocked » mais gatées par `exclusiveIsland:7` + île 7 dev-only (invisibles au
  joueur) ; (e) nouvelles ressources sans `RES_TIER` → groupées sous T0 dans l'inventaire (cosmétique).
  Build 259→260.
  Changement 13.78 : **bouton booster réaffiché (demande utilisateur).** `BOOSTER_UI_ENABLED` repassé
  `false → true` → le 6e bouton (booster) revient dans la barre du bas (à nouveau **6 boutons**), avec
  le layout 2 lignes du 13.77 (sprite `ui_booster` en haut, « ×N » + charge mm:ss en dessous) ;
  l'astuce `boost` (gatée sur le même flag) se redéclenche à l'île 2. Aucune autre modif. Validé :
  `node --check` (7 blocs) + Chromium E2E (île 2 forgée → 6 boutons, `.tab-boost` présent rendant
  `img.ui-ico` `ui_booster` « ×2 29:59 », clic actif `boost-on` ; 0 erreur console) + capture.
  Build 258→259.
  Changement 13.77 : **boutons du bas sur 2 lignes (sprite en haut, texte en bas, centré) + bouton
  booster retiré (code conservé).** (1) **Layout 2 lignes** : `.tabs-row .tab-btn .tb-top` passe en
  `flex-direction:column` (icône au-dessus du libellé, centré) + icône agrandie (13→16 px, emoji
  .82→1rem) → fini le texte comprimé/tronqué (« Démol », « CoPier ») quand icône et texte se
  partageaient une seule ligne. (2) **Bouton booster retiré** : nouveau flag module
  `const BOOSTER_UI_ENABLED = false;` (près de `BOOSTER_MAX`) gate le 6e `tab('boost',…)` (via
  `BOOSTER_UI_ENABLED && tab(...)`) → **barre à 5 boutons**. TOUTE la mécanique booster reste en place
  (état `boosterCharge`/`boosterOn`, recharge frame+offline, `toggleBooster`, sprite `ui_booster`,
  scène) — repasser le flag à `true` réaffiche le bouton sans autre changement. L'astuce `boost` est
  aussi gatée sur le flag (`when: g => BOOSTER_UI_ENABLED && …`) → pas de tuto pour un bouton absent.
  `SAVE_VERSION` inchangé. Validé : `node --check` (7 blocs) + Chromium E2E (île 2 forgée → 5 boutons,
  `.tab-boost` absent, `tb-top` flex-direction column, libellés complets ; 0 erreur console) + capture
  barre du bas (icône centrée sur ligne 1, libellé ligne 2). Build 257→258.
  Changement 13.76 : **sprite propre du booster + astuce booster débloquée à l'île 2.** (1) **Sprite
  `ui_booster`** (fusée pixel-art 16×16 générée Pillow : nez rouge, hublot cyan, corps clair, ailerons
  rouges, flamme) inliné dans `__SPRITE_DATA__` après `ui_batterie` → le 6e bouton du bas
  (`tab('boost',…,'booster',…)`) rend automatiquement le sprite via `uiIcon('booster')` (clé `ui_booster`)
  au lieu de l'emoji 🚀. (2) **Astuce `boost`** (GAME_TIPS, insérée après `transport`, `when = île 2
  débloquée` — même condition que `transport`, donc s'affiche à la « réparation » de l'île 2) : explique
  l'accélération du temps (aucun débit modifié), le barème ×2→×10, le cumul avec le mode rapide, la charge
  30 min / recharge 24 h. Repli fr (pas de trad, précédent 13.32). (3) **Scène d'illustration `TIP_SCENES.boost`**
  (île 2 : mine→route→four avec overlays `fx_boost`, sprite `ui_booster`, badges « x2 » / « + vitesse »)
  → l'astuce a une vignette dans le popup ET l'Aide. `SAVE_VERSION` inchangé. Validé : `node --check`
  (7 blocs) + Chromium E2E fr (île 2 forgée : bouton booster rend `img.ui-ico` `ui_booster` ; Aide →
  carte « 🚀 Le booster de vitesse » dépliable → canvas 768×512 rempli 100 % ; 0 erreur console) +
  sprite décodé 16×16 + capture barre du bas (fusée verte « ×2 » à droite des 6 boutons). Build 256→257.
  Changement 13.75 : **ajustements booster/UI (retours utilisateur sur 13.74).** (1) **Textes des
  boutons du HAUT restaurés** : la compaction CSS §7 du 13.74 (masquage `.rlabel` + sous-textes
  options/aide) est ANNULÉE — les libellés Port/Recherche/Options/Aide réapparaissent. (2) **Boutons
  du BAS compactés à la place** : le helper `tab()` ne rend plus `.tb-sub` quand le sous-texte est
  `null` ; les 5 boutons d'action passent en libellé court SANS sous-texte — Bâtiment/Réseau → **« Ouvrir »
  / « Fermer »** (nom du bâtiment si un outil est sélectionné), Copier/Démolir/Améliorer gardent leur
  verbe (sous-textes « choisir »/« remb. 100% »/« niveau +1 » supprimés). (3) **Booster = 6e bouton
  de la barre du bas** (au lieu du bouton flottant `.booster-btn`, retiré) : rendu via `tab('boost',…)`,
  icône 🚀, nom **« ×N »** (ou « Booster » grisé si indispo île 1), sous-texte = charge mm:ss ; classes
  `tab-boost` + `boost-on` (vert, actif) / `boost-empty` (charge < 1 s) / `boost-locked` (île 1, grisé).
  Toujours présent → **6 boutons** en permanence (grisé tant que seule l'île 1). (4) **Booster limité à
  30 min** (`BOOSTER_MAX = 1800`) avec **recharge complète en 24 h réelles** (`BOOSTER_RECHARGE_PER_SEC
  = 1800/86400`) — remplace l'ancien plafond 1 h / recharge 150 s/h. Appliqué en jeu (`frame`) ET
  hors-ligne (`runCatchUp`) ; `loadSave` clampe à `BOOSTER_MAX` (une save 13.74 > 30 min redescend).
  `SAVE_VERSION` inchangé. Validé : `node --check` (7 blocs) + Chromium E2E fr (haut restauré : `.rlabel`
  « Port » + « Options » visibles ; bas = 6 boutons sans sous-texte ; booster grisé « Booster » île 1 →
  « ×2 » + « 30:00 » île 2 ; clic → `boost-on` + `boosterOn=true` ; 0 erreur console). Build 255→256.
  Changement 13.74 : **booster de vitesse rechargeable (brief `briefboostervitesse`) + antenne T5
  renommée + export de sauvegarde encodé.** (1) **Booster de vitesse** — accélère le NOMBRE DE TICKS/s
  (même mécanique que le mode rapide `timeScale`, aucun débit modifié). Multiplicateur selon la PLUS
  HAUTE île débloquée : `BOOSTER_MUL_BY_ISLAND {1:1,2:2,3:4,4:6,5:8,6:10}` (île 1 seule = indispo ;
  île 6 supportée nativement même absente du code). Helpers module `highestUnlockedIsland`/
  `boosterMulAvailable`/`fmtBoosterTime` (après `antElecBoost`). État `g.boosterCharge` (0→3600 s,
  plafond 1 h) + `g.boosterOn` (jamais restauré actif au chargement). Recharge **150 s/heure réelle**
  quand OFF, décharge 1:1 quand ON — EN JEU (boucle `frame`, avant `_ts`) ET HORS-LIGNE (`runCatchUp`,
  sur `elapsedSec`, pas `ticks`). `_ts = (timeScale||1) × boosterMul` (cumul multiplicatif) ; coupure
  auto à charge épuisée ; `_maxTicks` borné à **200** (garde-fou pire cumul ×100). `toggleBooster`
  (App). Bouton flottant `.booster-btn` (bas-droite, position fixe, hors `.hud`) : visible si
  `boosterMul>1`, affiche 🚀 ×N + charge mm:ss, classe `on` (vert, actif) / `empty` (grisé, charge < 1 s
  → clic inerte). Props Toolbar `boosterMul`/`boosterCharge`/`boosterOn`/`onToggleBooster`, re-render
  via l'intervalle `bumpClock` 1 s. `SAVE_VERSION` inchangé (`boosterCharge` additif dans serialize/
  newGame/loadSave ; `boosterOn` non sauvegardé). (2) **§7 brief** : boutons HUD haut compactés (CSS)
  — `.research-btn/.options-btn/.save-btn` font-size .62rem, `.rlabel` + sous-textes options/aide
  masqués (`display:none`), titres conservés. (3) **Antenne T5 → « Antenne Amplificatrice »** (patch
  utilisateur) : rename aux 10 emplacements (inline `BUILDINGS.antenne.name` + TECH_NODES 27 + LOCALES
  bld.antenne & tech 27 × 4 langues ; fr/de « Antenne Amplificatrice », en « Amplifier Antenna », es
  « Antena Amplificadora »). ⚠ `I18N.applyToData` réécrit depuis LOCALES → éditer AUSSI les entrées
  LOCALES (fait). (4) **Export de sauvegarde ENCODÉ** (patch utilisateur : empêcher l'édition + c/c
  plus digeste) : nouveaux helpers module (près de `lsDel`) `encodeSave`/`decodeSave` = LZW sur octets
  UTF-8 (dict initial 256, codes 16 bits gelés à 65535) → base64, préfixe `ARCHv1:`. L'export
  (`doExport`) et le `.txt` produisent ce jeton compact + OPAQUE (save réelle → ratio ~0,34 = plus
  digeste, impossible à trafiquer à la main). `slotImport` = `JSON.parse(decodeSave(text))`,
  **rétro-compatible** (accepte aussi le JSON brut des anciens exports). Validé : `node --check`
  (7 blocs) + tests unitaires node (LZW round-trip petit/gros + save réelle sérialisée + passthrough
  JSON ; barème booster ×2/4/8/10 ; recharge 1 h → 150 s exact ; fmtBoosterTime) + Chromium E2E fr
  (boot 0 erreur ; booster invisible île 1 seule → visible ×2 « 🚀×2 05:00 » après déblocage île 2 ;
  clic → classe `on` + `boosterOn=true` ; charge < 1 s → grisé « 00:00 » ; `.rlabel` display:none ;
  export réel île 2 forgée → jeton `ARCHv1:` round-trip exact, `boosterCharge` sérialisé ; 0 erreur
  console). Build 254→255.
  Changement 13.72 : **port — amélioration gatée sur la PROCHAINE île + transit ÷10 + coûts ÷10 +
  migration douce des saves (SAVE_VERSION 22).** 3 demandes utilisateur. (1) **Gate « prochaine
  île »** : améliorer le port de l'île N exige que l'île N+1 soit débloquée (l'île 5, dernière,
  n'a pas de suivante → règle `hasLink` existante seule). 2 spots : handler `upgradePort` (toast
  orange, filet) + `PortPanel` (message `.pp-port-locked` « 🔒 Débloquez l'île N… » quand une
  liaison existe mais N+1 verrouillée, bouton Améliorer `disabled` via `nextLocked` ; le message
  historique « réparez l'île voisine » couvre le cas 0 liaison). i18n : 2 clés composables
  en/es/de. (2) **Transit ÷10 + coûts ÷10, ratio coût/débit INCHANGÉ** : `SHIP_BATCH` 600→**60**
  (débit de base Normal 10→1 u/s) et `PORT_BASE_COST` ÷10 sur les 5 îles (î1 10k ciment + 10k
  ling.fer, etc. — le commentaire « = coût de livraison du déblocage » n'est plus vrai).
  ⚠ **Mode Difficile** : lot de base 6 → 0,1 u/s au niveau 0, or `transitPerSec` FLOORAIT → transit
  MORT. Nouveau helper **`portRateAt(lvl)`** (source unique tick + panneau Port) : entier dès
  1 u/s (comportement historique), **fractionnaire en dessous** — le moteur (`transferLink`)
  déplace des quantités flottantes sans problème. (3) **Migration saves < 22** (`loadSave`, après
  la restauration de `portSpeed`) : **+3 niveaux sur TOUTES les îles** (×8 ≈ ×10) → débit ET
  prochains coûts ≈ 80 % de l'ancien barème, ratio coût/débit préservé, niveaux payés non
  dévalués, save homogène (un port jamais amélioré passait sinon de 10 à 1 u/s). `SAVE_VERSION`
  21→**22** (+22 whitelist), pas d'autre migration. Rappel mécanique : le niveau d'une LIAISON =
  `max(portSpeed[src], portSpeed[dest])`. `__heat` étendu (PORT_BASE_COST/portUpgradeCost/
  portRateAt/transitPerSec/ISLAND_ACCESS_NODE/linkActive). Validé : `node --check` (7 blocs) +
  Chromium E2E fr-FR 31 assertions (coûts/débits/ratio exacts ; Difficile 0,1/0,8/1 u/s via
  `applyGameMode` aller-retour ; save v21 forgée depuis une VRAIE save (skip tuto + flush
  `pagehide`) → +3 partout, liaison 1-2 32 u/s = 80 % de l'ancien ; UI : verrou île 2→3 + bouton
  disabled, île 1 sans verrou, « Débit max 32 u/s · lots ×32 » ; round-trip v22 sans double
  migration — save en attente garantie par le toggle Cible⇒Réserve avant le flush ; 0 erreur
  console) + non-régression suite 13.71 (36 assertions). ⚠ Piège harnais : le bouton PORT du HUD
  se sélectionne par `button[title="Configuration du port (commerce)"]` (2 `.research-btn`).
  Build 252→253.
  Changement 13.71 : **patch 5 demandes utilisateur — conduit polymère ÷10 + réseaux ×8/niveau +
  nœud 28 pure réparation + centrale ×2 (ratio 1024 kW : 128 kJ) + menu Bâtiment rabattable/recherche.**
  (1) **Conduit : polymère ÷10** (cuivre INCHANGÉ) aux 3 spots : pose (def `BUILDINGS.conduit.cost`
  100→10/tuile), table `NETWORK_UPGRADE_COST.conduit` (1000→100, 10000→1000) et formule
  `networkUnitCost` conduit (100×10^lvl → 10×10^lvl) — table et formule restent égales. (2) **Montée
  des AUTRES réseaux (route/tuyau/câble) plus pénalisante** : niveaux 3+ passent de ×4 à **×8 par
  niveau** (`Math.pow(8, level-3)`, aligné sur le débit ×8/palier → le coût par unité de débit ne
  baisse plus ; 3→4 = 800 inchangé, 4→5 = 6400, 5→6 = 51200…). Le rattrapage à la pose (`tbl[3]`)
  reste cohérent (= cran 3→4 de la formule). (3) **Nœud 28 (Navire Futuriste) = pure RÉPARATION** :
  les 3 reqs `produce` (10000 EMN + 100000 acier irr. + 100000 câble irr.) DUPLIQUAIENT la livraison
  → le joueur « payait » deux fois (production cumulée PUIS stock à livrer). `reqs: []` désormais
  (comme les accès d'île 2/8/14/21) : prêt à livrer dès le nœud 25 confirmé, la livraison (inchangée)
  est le seul coût. Saves existantes : `nodeCond` sur reqs vides = true → condition_ok au 1er
  evaluateTechTree, aucune migration. (4) **Centrale nucléaire ×2 + nouveau ratio chaleur** :
  `NUC_POWER` 8192→**16384** (2 spots : tick + fiche) et **`HEAT_PER_MW` 0,25→0,125** (= 1024 kW →
  128 kJ/s demandé) → la chaleur à pleine puissance V1 reste 2,048 MJ/s (mêmes tours/conduits).
  Les 2 `0.25` codés en dur de la fiche centrale (lignes Sortie/Prod. théorique) passent par
  `HEAT_PER_MW`. ⚠ Effet de bord assumé : la chaleur d'antenne en mode prod (même constante) ÷2.
  (5) **Menu Bâtiment** : chaque catégorie a une **tête cliquable** (`.tool-group-head`, chevron
  ▸/▾, SFX click) qui la rabat (`collapsed[gk]`, état de SESSION dans la Toolbar — survit à
  l'ouverture/fermeture comme le scroll 13.37, non persisté) + pastille `notif-dot` sur une tête
  repliée cachant un bâtiment « nouveau » ; **champ de recherche sticky** (`.bp-search`, menu
  Bâtiment seulement) filtrant par nom localisé, insensible aux accents (`normSearch` =
  lowercase + NFD), la recherche IGNORE le rabattage, message `.bp-empty` si 0 résultat, effacée à
  la fermeture du panneau (useEffect sur buildOpen) ; le menu Réseau est rabattable aussi (pas de
  recherche). `renderGroups(groups, gate, opts {collapsible, query})`. i18n : nouvelle IIFE (4 clés
  en/es/de). `__heat` étendu (networkUnitCost/NETWORK_UPGRADE_COST/TECH_NODES/TECH_BY_ID/
  HEAT_PER_MW). `SAVE_VERSION` inchangé. Validé : `node --check` (7 blocs) + Chromium E2E fr-FR
  36 assertions (données exactes ×3 réseaux + conduit ; moteur réel : centrale 2×2 forgée in-vivo
  (ancre + tuiles `occupied`) → `heatEmit/nucCur` = 0,000125 EXACT au tick ; menu : replier/déplier
  par tête, « carriere » trouve « Carrière V1 » dans une catégorie REPLIÉE, .bp-empty, ✕, recherche
  vidée à la réouverture, Réseau sans champ ; `fmtHeat(16384×0,125/1000)` = « 2,05 MJ » ; 0 erreur
  console) + smoke i18n en (4 clés résolues). ⚠ Rappel harnais : `window.__gameRef` est le REF
  (`.current`) ; le 1er enfant de `.build-panel` est désormais `.bp-search` (les sélecteurs
  `:first-child` sur les groupes ne matchent plus). Build 251→252.
  Changement 13.70 : **boost de VITESSE et conso élec. boostée de l'antenne ÷10 au Nv.1** (demande
  utilisateur : aligner sur la productivité 13.67). 2 nouveaux helpers module à côté d'`antProdEffect`
  (sources de vérité UNIQUES tick + fiches + bornes énergie + badge carte) : **`antSpeedMul(f)`**
  = 1 + min(1, 5 %×f) (Nv.1 : ×1,1, Nv.2 : ×1,2…, plafonné ×2 = ancien effet Nv.1) et
  **`antElecBoost(f)`** = min(2, 10 %×f) (conso sigmoïde ×1→×1,2 au Nv.1, ×1→×1,4 au Nv.2…,
  plafonnée ×1→×3 = ancien effet) — `f` = facteur de zone brut 2^(upg+1), TOUJOURS stocké tel quel
  dans `buffSet`/`bld.antennaBuff` (les helpers dérivent). Mécanique du mode PRODUCTIVITÉ intacte
  (déjà ÷10 en 13.67) ; sa conso élec. boostée passe par le MÊME `antElecBoost`. Effet de bord
  assumé : la chaleur d'antenne en mode prod (0,25 MJ × MW consommés EN PLUS) baisse d'autant.
  9 spots : tick (power boosté + outMul/inMul via helpers), bornes `demandMin/Max` de la boucle
  énergie (le fix 13.69 passe par `antElecBoost`), fiche bâtiment boosté (« Boost antenne ×1,1 »,
  Élec. « boosté ×1→×1,2 » — nouveau formateur local `fxDec`, virgule fr), ligne « Effet » de la
  fiche antenne, tooltip du bouton Vitesse du toggle (texte neuf + i18n en/es/de, nouvelle IIFE
  d'augmentation), badge carte « ×N » (affiche le multiplicateur EFFECTIF ×1,1 — plus le facteur
  brut ×2), astuce `antenne` (⚠ les entrées **LOCALES tips ×4 langues** ÉCRASENT l'inline via
  `applyToData` → les 4 réécrites ; elles dataient d'avant 11.31 : « double la production »,
  « 512 kW ») + astuce `antenne_modes` (inline fr, phrase Vitesse chiffrée). `SAVE_VERSION`
  inchangé. `__heat` étendu (antProdEffect/antSpeedMul/antElecBoost). Validé : `node --check`
  (7 blocs) + Chromium E2E fr-FR 18 assertions (helpers unitaires + plafonds ; moteur réel forgé :
  bornes 1152/1177,6 kW exactes, panneau « 1,15 MW → 1,18 MW » + « Amplitude 26 kW » ; mine_fer
  boostée → netFlow = base ×1,1 EXACT ; fiches mine + antenne « ×1,1 »/« ×1→×1,2 » par tap réel ;
  0 erreur console) + smoke i18n en (tooltip + tips résolus). Build 250→251.
  Changement 13.69 : **fix « le boost élec. de l'antenne n'est pas pris en compte dans l'onglet
  Énergie ».** Retour testeur (suite du 13.68). Au tick, la conso d'un bâtiment boosté par
  l'antenne oscille du NOMINAL ×1 à ×(1+facteur) (`power = nomP × (1 + sig × fac)`, identique en
  vitesse et en productivité), mais les bornes `demandMin`/`demandMax` de la boucle énergie
  (affichées dans « Consommation min → max »/« Amplitude » du panneau Énergie ET dans « Demande
  min→max » du panneau Câble) sommaient `minPower`/`nominalPower` SANS le facteur → max
  sous-estimé (jusqu'à ÷3 au Nv.1) et min faux (le plancher sigmoïde du bâtiment n'est jamais
  atteint une fois boosté). Fix dans la boucle des consommateurs par composante : si
  `antFac = max(bld.antennaBuff, bld.antennaProd) > 1` → `demMin += nomP`,
  `demMax += nomP × (1 + antFac)`, `hasVarCons = true` (les drapeaux sont posés chaque tick par la
  boucle bâtiment, AVANT la boucle énergie). Affichage seul (`cc.power`/« Demande totale » étaient
  déjà justes), `SAVE_VERSION` inchangé, aucune clé i18n nouvelle. Validé : `node --check`
  (7 blocs) + Chromium E2E moteur réel (aciérie 128 kW + antenne 1024 kW mode VITESSE + câbles +
  route→port forgés via `__gameRef`/`__heat.rebuildNetworks` : `antennaBuff = 2`,
  `demandMin/Max = 1152/1408 kW` EXACTS (avant fix : 1152/1152), demande instantanée dans la
  plage, panneau Énergie « 1,15 MW → 1,41 MW » + « Amplitude 256 kW » ; 0 erreur console).
  Build 249→250.
  Changement 13.68 : **7 retours testeur — popup densification verrouillée + 2 badges Port (import
  consommé / aller-retour) + tuto traverser refait + astuce traverser_tuyau + flux nucléaires dans
  l'onglet Production + conso min/max « Amplitude » (panneau Énergie).** (1) **Popup « Recherche
  requise » (densification)** : le bouton 🔒 Densifier (fiche bâtiment `InfoPanel` ET `UpgradePanel`)
  n'est PLUS `disabled` quand la recherche manque — le clic ouvre **`DensifyLockPopup`** (nouveau
  composant après `ResearchDonePopup`, classes `rd-popup` réutilisées) : nom du nœud requis, texte
  explicatif, bouton **« Voir la recherche »** (ferme fiche/panneau + ouvre le ResearchPanel) ;
  state App `densLock`, prop `onDensifyLocked(densId)` sur les 2 panneaux ; CSS `.locked` passe
  `cursor:pointer` + hover jaune. (2) **Badge Port « import consommé »** (`.pp-state.drain`, orange,
  glyphe `←!`) : dans `stockStateFor`, si import > 0 ET stock < cible ET `local + imp ≤ 0` (la conso
  locale absorbe tout ce qui arrive) → le joueur voit POURQUOI son stock ne monte pas (retour : « je
  veux 10k charbon, 10/s arrivent, l'île en consomme 20/s, le stock ne bouge pas »). (3) **Badge
  « aller-retour »** (`.pp-state.ping`, rouge, `↔`) : nouvelle ventilation des flux PAR île voisine
  (`_tfExpBy`/`_tfImpBy` dans PortPanel) — si la même ressource est exportée ET importée avec la
  MÊME île → badge + tooltip (« réglez cible/réserve ou interdisez un sens ») ; le cas chaîne
  (reçu d'une île, réexpédié vers une autre) garde le badge `⇄ transit`. (4) **Astuce `traverser`
  REFAITE** (brief `FIX_traverser.md` : l'illustration montrait un câble traversant `four_fer_v1`,
  four à CHARBON qui ne se raccorde pas au câble — situation impossible) : scène TIP_SCENES →
  éolienne → câble → **four_arc_fer** → câble → aciérie + badges « fait pont »/« alimentee » ;
  body réécrit (consommateur du porteur = pont, FUSION des réseaux, la route ne traverse jamais).
  (5) **Nouvelle astuce `traverser_tuyau`** (brief `AJOUT_traverser_tuyau.md`) : scène puits →
  tuyau → raffinerie (pont) → usine polymère, `when` = raffinerie débloquée, insérée APRÈS
  traverser (TIP_SCENES 32→33). ⚠ Les 2 astuces restent en repli fr (pas de trad, précédent 13.32).
  (6) **Flux nucléaires dans l'onglet Production** (retours « matériaux irradiés introuvables » et
  « U235 pas consommé ») : le bloc centrale du tick lit/écrit le port EN DIRECT (hors
  inByType/outByType) → invisibles dans `netFlow`/`islandFlowAgg`. Ajout de 5 `addFlow(roadNid,…)` :
  cons `combustible_u235`, cons matériau de base, prod irradié/plutonium. (7) **Panneau Énergie** :
  la ligne « Conso sigmoïdes (min→max) » devient **« Consommation min → max »** et s'affiche
  TOUJOURS dès qu'il y a des consommateurs (plus gatée sur `demandVar` — demande : voir la conso
  min et max) ; « Écart sigmoïdes » renommé **« Amplitude (sigmoïdes) »** (affiché seulement si ça
  oscille). i18n : nouveau bloc d'augmentation (après le bloc 13.67) — 10 clés en/es/de. Aucune
  mécanique/sauvegarde touchée hors affichage (`SAVE_VERSION` inchangé ; les addFlow sont du
  reporting). Validé : `node --check` (7 blocs) + Chromium E2E fr-FR 45 assertions (statics scènes ;
  EnergyPanel forgé : plage 100→400 kW + Amplitude 300 kW, conso fixe → plage sans Amplitude ;
  Port forgé : badges drain+ping + tooltips ; four_fer u9 forgé in-vivo + tap réel → bouton 🔒
  cliquable → popup → « Voir la recherche » ; Aide : 2 astuces dépliées, textes neufs, canvas 100 % ;
  **moteur réel** : centrale forgée (BFS route→port + câble via `__gameRef`/`__heat.rebuildNetworks`)
  → `netFlow` U235/acier/acier irradié ≈1/s exacts + lignes visibles dans l'onglet Production ;
  0 erreur console) + smoke i18n en/es/de (10 clés résolues). ⚠ Piège harnais : `pointerToTile` est
  relatif au CANVAS (`getBoundingClientRect`) — convertir les coords tuile→page en ajoutant
  `rect.left/top` ; `g.catchingUp = true` gèle tick ET draw (données forgées stables, mais plus de
  redraw) ; une astuce peut s'ouvrir juste avant le gel → purger `.tip-ok` avant les panneaux.
  Build 248→249.
  Changement 13.67 : **effet du mode PRODUCTIVITÉ ÷10 au Nv.1 (retour testeur : « trop efficace »)
  + l'effet MONTE désormais avec le niveau de l'antenne.** Avant : rendement +100 % / vitesse −50 %
  PLAT quel que soit le niveau. Désormais **nouveau helper module `antProdEffect(f)`** (avant
  `tickIsland`, source de vérité UNIQUE tick + fiches) : à partir du facteur de zone `f = 2^(upg+1)`
  (le même que le boost VITESSE, déjà stocké dans `debuffSet`/`bld.antennaProd`) → **bonus de
  rendement = 5 % × f** (Nv.1 : +10 %, Nv.2 : +20 %…, plafonné à +100 % = ancien effet) et **malus
  de vitesse = 2,5 % × f** (Nv.1 : −5 %, plafonné à −50 %) ; SORTIE ×(1−malus), INTRANTS
  ×(1−malus)/(1+bonus). Améliorer l'antenne sert donc AUSSI en mode prod (avant : seul l'élec
  changeait). Conso élec. boostée ×1→×(1+f) et chaleur INCHANGÉES. 5 spots : tick (outMul/inMul),
  fiche bâtiment boosté (antInMul/antOutMul + ligne « Productivité » DYNAMIQUE « rendement +X % ·
  vitesse −Y % »), tooltip toggle antenne, ligne « Effet » de la fiche antenne (valeurs du niveau
  courant), astuce `antenne_modes`. IIFE i18n 13.66 REMPLACÉE (clés obsolètes) par les nouvelles
  clés en/es/de (dont « rendement »/« vitesse » composables). `SAVE_VERSION` inchangé. Validé :
  `node --check` (7 blocs) + Chromium E2E moteur réel (four_fer + route port + câble + antenne
  forgés, mode prod : Nv.1 → lingot 0,95/s & minerai/lingot = 8÷1,1 exacts ; antenne montée Nv.2
  → 0,90/s & 8÷1,2 ; 0 erreur console). Build 247→248.
  Changement 13.66 : **reformulation du mode PRODUCTIVITÉ de l'antenne (retour testeur : « intrants
  ÷2 · sortie ×0,5 » incompréhensible).** Le concept est désormais présenté partout comme
  **« rendement ×2 · vitesse ×0,5 »** (= 2× moins de matières par unité produite, machine 2× plus
  lente ; mécanique INCHANGÉE — intrants ×0,25 / sortie ×0,5). 4 textes réécrits : (1) ligne
  « Productivité » de la fiche d'un bâtiment boosté (valeur + tooltip) ; (2) tooltip du bouton
  Productivité du toggle de la fiche antenne ; (3) ligne « Effet » mode-aware de la fiche antenne ;
  (4) body de l'astuce `antenne_modes` (« Productivité = rendement ×2 : … »). + 2e IIFE
  d'augmentation i18n (après le bloc ADD, ~ligne 2210) : 4 clés en/es/de (les anciennes clés
  n'avaient AUCUNE traduction — déjà en repli fr). Aucune mécanique/sauvegarde touchée. Validé :
  `node --check` (7 blocs) + Chromium E2E (boot 0 erreur ; clés résolues : en « yield ×2 · speed
  ×0.5 », de « Ausbeute ×2 · Tempo ×0,5 » — ⚠ le navigateur de test est locale EN → `I18N.t`
  renvoie l'anglais, normal). Build 246→247.
  Changement 13.65 : **2 retours testeur — icônes des panneaux Réparer/Remblayer + gaz NON
  transitables.** (1) **Coût en pastilles sprites** : la ligne « Coût » des panneaux terrain
  (`InfoPanel`, branche `mode 'repair'/'extend'`) passait par `formatCost` (texte brut, pas de
  notation port) → pastilles `.dr-res` (sprite `itemSpriteKey` + `RES_SHORT` + `fmtPort`), classe
  `.ipc-ci.miss` (rouge) quand le stock du port ne couvre pas (sauf mode dev). (2) **Import/export
  d'azote et d'oxygène INTERDITS** (demande : gaz = production locale seulement) :
  `oxygene`/`azote` RETIRÉS de `TRADE_LIQUIDS` (posés en 13.59) → hors `TRADE_RESOURCES`, plus de
  ligne dans la config Transit du Port ; ils restent stockés au port (tuyau relié) et visibles à
  l'inventaire. Protection vieilles saves : (a) `tradePriorityFor` FILTRE désormais les entrées
  devenues non transitables (en plus d'ajouter les manquantes) ; (b) **garde central dans
  `rawShippable`** (`TRADE_RESOURCE_SET`, nouveau Set module) → 0 pour toute ressource hors
  commerce, quel que soit le chemin (transferLink, pré-pass `transitDestPriority` — qui aurait
  sinon expédié un gaz d'une save avec ordre explicite). `SAVE_VERSION` inchangé. Validé :
  `node --check` (7 blocs) + Chromium E2E (pire cas forgé : 50k gaz + cible île 2 + gaz dans
  tradePriority ET transitDestPriority → 4 s de ticks : flux 1→2 = acier seul (10/s), 0 gaz au
  port 2, liste purgée/réconciliée ; InfoPanel standalone repair+extend : 2/3 pastilles avec
  sprite, fmtPort, `.miss` rouge ; 0 erreur console). ⚠ Rendu standalone d'InfoPanel : opacity
  reste 0 sans `stageRef` réel (layout effect) — asserter le DOM, pas les pixels. Build 245→246.
  Changement 13.64 : **chaleur de l'usine moteur nucléaire = 1024 kJ/s au Nv.1, ×2 par niveau.**
  Demande utilisateur. `bld.heatEmit` de `usine_moteur_nuc` (boucle bâtiment de `tickIsland`) passe
  de `1 × regime` (plat, quel que soit le niveau) à **`1.024 × mult × regime`** (`mult` = 2^upgrade
  déjà en scope) → Nv.1 = 1,024 MJ/s (1024 kJ), Nv.2 = 2,048, Nv.3 = 4,096… Échelle binaire alignée
  conduit/tour (au Nv.1 : 1 tuile de conduit V1 + 1 tour V1 suffisent pile). Le plafond de trip suit
  automatiquement (`heatCapOf` = 60 s d'émission, dynamique) ; la fiche « Bilan chaleur » lit
  `heatEmit` en live → rien d'autre à toucher. `SAVE_VERSION` inchangé. Validé : `node --check`
  (7 blocs) + Chromium E2E moteur réel via `__gameRef` (2 usines forgées u0/u1 + route connectée au
  port + câble + intrants — ⚠ l'usine consomme du PLUTONIUM (recette 13.0), pas de l'U235 — :
  `heatEmit/regime` = 1,024 et 2,048 exacts ; 0 erreur console). Build 244→245.
  Changement 13.63 : **barre de CALIBRATION en sprite au-dessus de la centrale nucléaire.** Demande
  utilisateur. (1) **9 sprites `ui_jauge_calib_000..100`** GÉNÉRÉS (les `ui_jauge_mj_*` du pack ROTÉS à
  l'horizontale 32×8 + recoloration violette #7E57C2 — teinte HLS des pixels saturés, gris du cadre
  conservés ; ~3 Ko), inlinés après `ui_jauge_mj_100`. (2) **Draw** (après la jauge de chaleur, avant le
  post-effet endommagé) : si `bdef.nuclear && nucState === 'starting'` (calibrage/recalibrage) →
  barre horizontale (largeur 1 tuile, h = tuile/4) CENTRÉE en haut de l'emprise 2×2, cran =
  `round(nucTimer/300 × 8)` (300 = NUC_START, constante LOCALE au tick — dupliquée en littéral
  commenté) ; remplissage gauche→droite ; disparaît en `running`/`stopping`/`off` ; ne chevauche pas
  la jauge de chaleur (verticale, bord gauche, centrée verticalement) ; `_animPlayed` posé (redraw
  ~10 FPS pendant la rampe) ; repli barre segmentée violette si sprite absent. (3) **Nouveau hook de
  test `window.__gameRef`** (dans App, comme `__heat`) : accès à la partie en cours pour les E2E
  (forge de bâtiments in-vivo, fini les saves forgées pour les cas simples). ⚠ Piège E2E découvert :
  une centrale forgée sans câble/combustible est re-basculée `stopping` par le TICK suivant (la barre
  « disparaît » en ~1 s) → forger avec `paused: true` (le tick saute le bâtiment, l'état nucléaire
  reste gelé — et la barre reste visible sur une centrale en pause mi-calibrage, edge assumé).
  `SAVE_VERSION` inchangé (nucState/nucTimer déjà transitoires). Validé : `node --check` (7 blocs) +
  Chromium E2E partie réelle (9 sprites décodés ; centrale forgée au centre de la vue : barre à
  7 %/50 %/95 % conforme aux captures zoomées, disparition en running ; 0 erreur console).
  Build 243→244.
  Changement 13.62 : **4 fixes testeur (retours 13.59).** (1) **Icône azote** : sprite `item_azote`
  GÉNÉRÉ (recoloration verte de `item_oxygene`, bouteille de gaz, 16×16, 206 o) et inliné juste après
  lui — `itemSpriteKey('azote')` le prend automatiquement (inventaire/recettes/fiches/Port ; le pack
  officiel ne livre toujours pas d'art azote — remplacer la clé si un art officiel arrive). (2) **Icône
  pompe V3 manquante (menu Bâtiment + carte)** : `pompe_eau_v3` n'a AUCUNE clé sprite (ni `bat_`, ni id,
  ni `_v1`) → alias `BLD_SPRITE_OVERRIDE.pompe_eau_v3 = 'pompe_eau_v2'` (réutilise l'art V2 ; l'anim V2
  suit via `ANIM_BY_SK`). (3) **Import des matériaux irradiés impossible** : la liste Transit du Port
  (`visiblePriority`) filtre par `unlockedResourceSet` = ressources dans les `outputs` STATIQUES des
  bâtiments débloqués — or la centrale produit les irradiés DYNAMIQUEMENT (`nucMix`/`nucOutKey`, pas
  d'`outputs`) → sur une île sans stock, impossible de fixer une cible d'import. Fix dans
  `unlockedResourceSet` : si `centrale_nucleaire` débloquée → ajoute `nucOutKey(k)` pour chaque
  `NUC_MAT_KEYS` (acier/béton/câble irradiés + plutonium). Effet de bord assumé : ils apparaissent
  aussi à 0 dans l'inventaire HUD et le Calculateur dès la centrale recherchée. La mécanique de transit
  était SAINE (carrier road → déjà dans `TRADE_RESOURCES`). (4) **Conduit 1,024 MJ au lieu de 1 MJ
  rond** (demande : « 1024 kJ par conduit ») : `conduitDebit` base 1 → **1,024** (V1=1,024, V2=8,192,
  V3=65,536 MJ/s/tuile, ×8/palier conservé) — échelle BINAIRE alignée sur les kW : 1 tuile V1 =
  exactement l'absorption d'une tour V1 (1,024 MJ/s), centrale 8192 kW = 2,048 MJ/s = 2 tuiles.
  Affichage « 1,02 MJ » via `fmtHeat` (dynamique, rien d'autre à toucher). + hook de test `__heat`
  étendu (buildingSpriteKey/itemSpriteKey/unlockedResourceSet/isBuildingUnlocked/fmtHeat).
  `SAVE_VERSION` inchangé (aucune donnée de save touchée). Validé : `node --check` (7 blocs) +
  Chromium E2E (item_azote décodé 16×16 ; `itemSpriteKey('azote')='item_azote'` ;
  `buildingSpriteKey('pompe_eau_v3')='pompe_eau_v2'` présent ; `unlockedResourceSet` avec/sans
  centrale → 4 irradiés présents/absents ; conduitDebit 1,024/8,192/65,536 → « 1,02/8,19/65,5 MJ » ;
  0 erreur console). Build 242→243.
  Changement 13.61 : **GUIDE DYNAMIQUE post-tutoriel (brief `BRIEF_B_GUIDE_DYNAMIQUE`)** — la couche
  « quoi faire ensuite » après le tuto : objectifs pilotés par l'ÉTAT (pas un compteur), correctifs
  récurrents AVANT découvertes one-shot (K1). (1) **`GUIDE_OBJECTIVES`** (module, avant GAME_TIPS,
  8 entrées) : fix_deconnecte / fix_deficit / fix_sature / go_recherche (fix, K2) / go_eolienne /
  go_wire / go_ile2 / go_liaison ; `activeGuideObjective` = 1er `when` vrai non accompli (try/catch),
  `guideHasTradeCfg`. ⚠ 4 écarts au brief (tous justifiés/testés) : (a) go_recherche `when` =
  **`hasPendingResearch`** (pas « ≥1 condition_ok » : un nœud delivery est condition_ok IMMÉDIATEMENT
  sans être payable → l'objectif serait affiché en permanence) ; (b) fix_deficit sur **`e.balance <
  -1e-6`** (bilan honnête 13.8 — `demand > produced` sous-détecte, produced inclut la décharge
  batterie) ; (c) `guideHasTradeCfg` = stockCible>0 (toute île) OU seuilExport>0 **île 1 seulement**
  (le kickstart 13.31 pose des réserves sur l'île débloquée → le critère du brief était vrai sans
  action du joueur) ; (d) fix_deconnecte **s'efface devant go_wire** (1re éolienne sans aucun câble :
  « Relie-le au port » est trompeur, la leçon câble EST le correctif). (2) **`checkGuide()`** (App,
  appelé avant checkTips dans la boucle rAF) : sélection d'objectif **throttlée ~4 Hz** (les when/done
  scannent les tuiles de toutes les îles), avancement de cible PAR frame ; refs jumelles
  `guideIdRef/guideObjRef/guideTargetRef` (lisibles depuis draw) + states guideId/guideTarget ;
  discover accompli → `g.guide.done[id]` DÉFINITIF (K3) ; tip `why` ouvert UNE fois (marqué tipsSeen,
  K7) ; tuto actif → guide inerte. (3) **Bannière** : `TutorialBanner` gagne le mode `total: 0` →
  badge « Objectif » sans compteur, goal via **`I18N.t(o.goal)`** (couche ui, PAS applyToData) ;
  montée après la bannière tuto (`tutorialStep < 0 && guideId`), halo DOM idem (masqué si popup) ;
  le relayout du bandeau dépend désormais de `[tutorialStep >= 0, !!guideId]`. « Passer » →
  `skipGuide` = guide ENTIER off (K4), toggle **« Guide »** dans les Options (`toggleGuide`,
  pattern tipsEnabled). **PAS de gate d'onglets** (K5 — le tuto seul est bloquant). (4)
  **`drawTutorialHalo` étendu** : gardes refondues (`tutorial actif → île 1 seulement` ; sinon lit
  `guideObjRef/guideTargetRef` → **toutes les îles**, §11 du brief) + mots-clés `@disconnected`
  (b.disc, île courante) et `@saturated` (tuiles des `netSaturated[isl]`). (5) **8e `data-tut`** :
  `island-<id>` sur les onglets d'île (IslandSelector). (6) **`SAVE_VERSION` 20→21** (+21 whitelist) :
  `guide {enabled, done, seenIsland2}` newGame/serialize/loadSave (save antérieure → défauts, le
  joueur en cours bénéficie des objectifs pertinents — voulu). (7) **i18n** : 2e bloc d'augmentation
  (après le bloc TUT 13.60) — 8 goals + Objectif/libellés/toasts en en/es/de. Validé : `node --check`
  (7 blocs) + Chromium E2E 4 suites (~75 assertions au total) : guide partie 1 (statics — ordre K1,
  7 why existants, 0 collision d'id —, tuto passé → 0 gate (K5), mine sans route posée en réel →
  bannière Objectif + halo canvas sonde pixel + tip 1 fois, route L jusqu'au port → objectif résolu) ;
  guide partie 2 saves forgées (go_eolienne → carte → outil ; go_wire ; go_ile2 → clic île 2 →
  go_liaison + done persisté ; tradeConfig → résolu sans retour (K3) ; Passer + réactivation par le
  toggle Options ; save v20 sans champ → défauts ; **halo canvas pulsé sur l'ÎLE 2** — test critique
  §11) ; non-régression des 2 suites tuto 13.60 (fin de tuto : la bannière GUIDE peut enchaîner —
  assertion adaptée) ; 0 erreur console partout. ⚠ Rappels harnais : la bannière guide partage la
  classe `.tuto-banner` (tester le CONTENU, pas la présence) ; `disc` = « pas relié AU PORT » (une
  tuile de route adjacente ne suffit pas — tracer le L complet) ; OptionsModal = `.slot-panel`/
  `.slot-close`. Build 241→242.
  Changement 13.60 : **TUTORIEL V2 (île 1) — 8 étapes guidées, halo pulsé, popups « pourquoi »,
  tuto bloquant** (brief `BRIEF_TUTO_V2`, décisions D1-D5 actées). (1) **`TUTORIAL_STEPS` refondu
  (7 → 8 étapes, la ROUTE remonte en étape 2** pour boucler mine→route→port immédiatement) : chaque
  étape = `{reveal, goal, why, targets[], done, afterToast}` ; nouvel ordre mine_fer / road /
  carriere / améliorer / mine_charbon / four_fer / cimenterie / recherche (done étape 8 = n'importe
  quel nœud ≠ 1 confirmé). (2) **Machine à cibles du halo** : `targets[]` = séquence `{sel|tiles,
  when(g, ui)}` ; `checkTutorial` s'arrête sur la PREMIÈRE cible dont `when` est VRAI et pilote
  `g.tutorial.targetIdx` (persisté). ⚠ 2 écarts au brief (sinon cibles inatteignables — la machine
  s'arrête à la 1re vraie) : étape 4 cible tiles `when: !ui.upOpen` (pas `()=>true`) pour atteindre
  le bouton Monter, étape 8 cible recherche `when: !ui.researchOpen` pour atteindre Livrer/Confirmer.
  `ui` = **`panelsRef`** (miroir render-body des useState `buildOpen/netOpen/upOpen(=upgrade)/
  researchOpen` — lisible depuis la boucle rAF). (3) **7 attributs `data-tut`** (6 du brief + 1) :
  ToolButton (=id), Port, Recherche, « Monter » InfoPanel, bouton « ⬆ Améliorer » de l'UpgradePanel
  (ajout : le flux réel de l'étape 4 passe par l'outil Améliorer → UpgradePanel, pas l'InfoPanel),
  « Confirmer » ET « Livrer » du ResearchPanel (même ancre `confirm` : le 1er nœud validable — Accès
  Île 2 — est en mode delivery). (4) **`TutorialHalo`** (composant DOM, `pointer-events:none`,
  repositionné par rAF, pulsation CSS `@keyframes tut-pulse`) monté sous la bannière, masqué si un
  popup est ouvert ; **`drawTutorialHalo(ctx, ox, oy, tile, r0, r1, c0, c1)`** (halo canvas, bornes
  visibles passées en params) : tuiles posables via `canPlace`, `@upgradable` = mine/carrière Nv.1,
  `'link'` = mine + port + polyligne pointillée en L (pas de pathfinding, J1) ; pose `_animPlayed =
  true` → pulse au canal ~10 FPS d'ambiance. (5) **Tuto bloquant (D3/D4)** : prop `tutStep` de la
  Toolbar → `tabAllowed(key)` (classe `.tab-locked` + disabled) ; **Démolir TOUJOURS actif (D4)**,
  Copier jamais pendant le tuto (J2) ; cartes du menu déjà filtrées par `tutorialRevealed` (intact).
  (6) **8 popups « pourquoi »** `tut_*` dans GAME_TIPS (`when: ()=>false`, ouverts par
  `showTip(st.why)` au franchissement + marqués `tipsSeen` → visibles dans l'Aide) ; textes fr
  inline + **bloc d'augmentation i18n** (après le bloc ADD, ~ligne 2183) : tips en/es/de,
  **`L.tutorial` REMPLACÉ en bloc** (8 goals ×4 langues — l'ordre a changé, fusionner aurait gardé
  les vieux goals), toasts `afterToast` en/es/de. ⚠ Le fallback body d'un tip vient du locale FR
  (`tip()` mappe sur `fb`) → les entrées fr DOIVENT être dans LOCALES.fr.tips sinon les body
  en/es/de ne s'appliquent pas. (7) **`checkTips` réécrit** : tutoriel actif = canal popup RÉSERVÉ
  (bienvenue → tut_mine à l'étape 0 → popups d'étape) ; les astuces contextuelles (recherche/port,
  vraies dès le boot car le nœud 2 est condition_ok immédiatement) sont **différées sans être
  marquées vues** → reprennent à la fin/skip du tuto. (8) **FIX course préexistante « bienvenue
  par-dessus la ModeModal »** : `needModeRef` était synchronisé par `useEffect([needMode])` qui, au
  montage initial, tournait APRÈS l'effet de boot avec `needMode` encore false → écrasait le true ;
  la 1re frame rAF battait le re-render React (flaky, reproduit ~1/4). Désormais synchro dans le
  CORPS du render + pose synchrone au boot et dans `chooseMode`. (9) **`SAVE_VERSION` 19→20**
  (+20 whitelist) : `tutorial.targetIdx` sérialisé/restauré (vieille save sans champ → 0, halo
  recalé sur la 1re cible, pas de crash — vérifié). Validé : `node --check` (7 blocs) + Chromium E2E
  3 suites (~55 assertions) : partie neuve réelle fr (statics, séquence bienvenue→tut_mine, halo
  .tab-build→carte→gisements→pose mine→étape 2→route→liaison réelle au port→étape 3, gate D3/D4,
  save v20) ; saves forgées (étape 4 : .tab-upg→@upgradable→bouton panneau→2 améliorations→étape 5 ;
  étape 8 : Recherche→Livrer→fin du tuto+gate levé ; v19 sans targetIdx ; « Passer ») ; smoke DE
  (goal + tips traduits) ; pulse vérifié par sonde pixel (canal vert oscille) ; 0 erreur console.
  ⚠ Pièges harnais E2E : (a) le flush `pagehide` ré-écrit la save du jeu à la navigation → geler
  `Storage.prototype.setItem` avant de forger une save en localStorage ; (b) réplique caméra =
  `clientWidth/Height` + `MIN_TILE=26` ; au boot d'une save mi-tuto la bannière apparaît APRÈS
  `centerCam()` (relayout sans recentrage) → tap auto-calibrant ±1 tuile ; (c) attendre la
  SUPPRESSION de `#splash` (pas juste `__splashGone`) avant les taps canvas. Build 240→241.
  Changement 13.59 : **oxygène/azote + séparateur d'air + 6 nouveaux paliers V2/V3 + puits V2 +
  1 centrale nucléaire PAR ÎLE** (brief utilisateur + pack `Archipel_sprites_OFFICIEL.zip`,
  dossier `_nouveau_v2/`). (1) **2 nouvelles ressources** `oxygene`/`azote` (RES_TIER t2, carrier
  **pipe** — gaz —, ajoutées à `TRADE_LIQUIDS` → transitables ; sprite `item_oxygene` livré, azote
  en repli code). (2) **`separateur_air`** (nouveau bâtiment, nœud 16 avec le raffineur Si) :
  1024 kW → 512 O₂ + 1024 N₂/s, coût 500 circuit + 1000 béton armé + 1000 câble — ⚠ nouveau flag
  **`noTierMult: true`** (exemption explicite du `TIER_COST_MULT` ×8 t3 : coût du brief = coût payé) ;
  **`separateur_air_v2`** (palier u10, forfait 50 proc + 1000 béton irr + 1000 câble irr, sigmoïde
  288→2304). (3) **Règle sigmoïde des V2** : base V1 ×1,125 → plancher ÷4, plafond ×2 (ex. 1024 →
  1152 → 288/2304). Nouveaux paliers (TIER_NEXT cap 9 + TIER_STEP entry 10) : **usine_polymere_v2**
  (4 pétrole/2 eau/0,5 Si/0,125 acide → 1 poly ; forfait 1000 béton + 500 pièce + 10 proc ; sigmoïde
  27/189), **distillerie_v2** (4 pétrole/2 eau/2 O₂ → 1 acide ; 5 EMN + 200 câble irr + 100 béton
  irr ; 18/126), **raffinerie_v2** (I/O idem V1 ; 10 proc + 500 câble + 500 béton + 500 poly ; 9/63),
  **centrale_diesel_v2** (1,5 diesel + 2 O₂ → 512 kW, soit 1536/2048 → 524288 au Nv.11 ; 10 proc +
  1000 pièce), **circuit_v2** (2 câble + 8 poly + 4 azote → 1 circuit — les « 8196/4196 » du brief
  lus comme 8192/4096 ; forfait 5000 béton irr + 2500 acier irr + 100 EMN ; sigmoïde V1 conservée),
  **pompe_eau_v3** (palier de pompe_eau_v2 cap 19, entry 20 ; 10 EMN + 500 béton irr + 500 acier
  irr ; sigmoïde 0,03515625/0,24609375). (4) **`puits_petrole_v2`** = bâtiment À PART (pas de
  densification) : 8 acide + sigmoïde 36→288 kW → 256 pétrole au Nv.1, coût 10 EMN + 200 béton irr +
  100 câble irr, île 3 comme le V1. (5) **Recettes** : raffineur_silicium +32 O₂/s, fab_processeur
  +256 azote/s ; **fours à arc** : nouvel intrant secondaire fixe `extraIn` dans ARC_DEF (fer :
  O₂ 0,015625 base = 16384/s au Nv.21 ; cuivre : acide 0,00048828125 = 512/s) — `arcEffective`
  fusionne extraIn dans inputs, statiques REPRÉSENTATIFS alignés ; **forfait des arcs** → 50 EMN +
  1000 câble irr + 500 acier irr + 500 béton irr. (6) **1 centrale nucléaire PAR ÎLE** : nouveau
  helper `countBuildingsOnIsland` — le garde `maxPerIsland` de `tryPlace` comptait via
  `countBuildings` (TOUTES les îles = 1/partie, bug latent, le toast disait déjà « par île ») ;
  vaut aussi pour l'antenne ; **`exclusiveIsland: 5` RETIRÉ de la centrale** (posable partout, état
  nucléaire déjà par île). (7) **Déblocages** : nœud 16 += separateur_air ; nœud 20 (plateforme) +=
  usine_polymere_v2/raffinerie_v2/centrale_diesel_v2 ; nœud 26 (mines V3) += distillerie_v2/
  puits_petrole_v2/separateur_air_v2/circuit_v2/pompe_eau_v3 ; toolbar MAJ (9 ids). (8) **Assets** :
  `assets_data.js` du zip collé après l'ancre `__ANIM_DATA__["tour_aerorefrigerante"]` (36 sprites +
  18 sheets, dont les V2 « île 6 » encore inertes : fab_processeur_v2, fonderie_or_v2,
  raffineur_silicium_v2, broyeur_uranium_v2, centrale_enrichissement_v2, centrale_nucleaire_v2 —
  PAS de defs BUILDINGS, volontaire) + 17 entrées `ANIM_META`. `SAVE_VERSION` inchangé (ids/
  ressources additifs). Validé : `node --check` (7 blocs) + Chromium E2E 40 assertions (defs/
  forfaits/nœuds/toolbar exacts ; coût séparateur NON multiplié ; sprites décodés 32×32 + sheets
  128×32 + `ANIM_BY_SK` ; moteur réel île 1 : séparateur +512 O₂/+1024 N₂/s pile, distillerie_v2
  u10 +1024 acide/−2048 O₂/s, arc fer u20 −16384 O₂/s et minerai/lingot 4194304/1048576 (4:1) ;
  `countBuildingsOnIsland` 1/île ; 0 erreur console). ⚠ Piège harnais : pour mesurer les débits
  nominaux d'un réseau forgé, passer les réseaux en `unlimited` (sinon on mesure le PLAFOND de débit
  V1 : tuyau 64/s, route 128/s) ; `netIds` n'existe que sur les JONCTIONS (tuile d'infra simple =
  `t.networkId`). Build 239→240.
  Changement 13.58 : **refonte chaleur — conduits FLUX pur (×8/palier, teinte au % de flux) + stock
  de chaleur DANS le bâtiment (1 min d'émission) + alerte d'accumulation.** 5 demandes utilisateur.
  (1) **`conduitDebit` ×2 → ×8 par palier** : V1=1, V2=8, V3=64 MJ/s/tuile. (2) **Le conduit ne
  stocke PLUS de chaleur** : `processHeat` étape 3 réécrite — plus de tampon `net.heatStore` (purgé
  par `delete` à chaque tick ; le report du tampon dans `rebuildNetworks` — scission 13.33 + fusion
  traversée — est retiré) ; transfert DIRECT sources → tours chaque tick, borné par le débit total
  (tuiles × débit/tuile), l'absorption des tours (eau) et la chaleur dispo des sources. `conduitFlow`
  = MJ/s réellement évacués (inchangé). (3) **Teinte du conduit = % de FLUX** (`conduitLoad =
  flux/débit total` au lieu de `stock/cap`) — les sprites `_chauffe1/2/3` (≥25/50/80 %) et le stub
  sous bâtiment en héritent sans changement du draw. (4) **Plafond de chaleur des bâtiments = 1 MINUTE
  d'émission** (`HEAT_CAP_SECONDS = 60`) : `heatCapOf(bld)` devient DYNAMIQUE = `max(heatEmit,
  heatEmitPk) × 60` où `heatEmitPk` = pic d'émission récent (décroissance ×0,995/tick, posé à
  l'étape 1) — stable pour les émissions oscillantes (antenne) ; repli : émission nulle avec chaleur
  gelée → cap = chaleur courante (jauge pleine, pas de trip). `b.heatCap` (def, 20/10/10) ne sert
  plus que de FLAG « bâtiment à chaleur ». **Trip seulement si la chaleur MONTE** (`heatEmit >
  heatCool`) — baisser la puissance avec de la chaleur stockée ne trippe plus. Fiche bâtiment + jauge
  de tuile passées sur `heatCapOf` (+ tooltip « plafond = 1 min d'émission »). (5) **Alerte
  d'accumulation** (`game.heatWarn`, à transition, ré-armée < 5 %) : dès 10 % du plafond en MONTÉE →
  toast orange « accumule de la chaleur — surchauffe dans ~Xs » + SFX `powerAlert` throttlé 8 s ;
  drapeau `bld.heatWarned` (transient), remis à zéro au trip et dans `tryHeatRepair` (qui reset aussi
  `heatEmitPk`). NetworkPanel conduit : ligne « Stockage » RETIRÉE, « Flux évacué » = `X / cap /s`
  (rouge si ≥99 % = saturé), titres MAJ ; astuce nucléaire réécrite ; i18n en/es/de des 3 nouvelles
  clés (bloc ADD ligne ~2115). `SAVE_VERSION` inchangé (`pl.h` inchangé ; `heatEmitPk`/`heatWarned`
  transients). Validé : `node --check` (7 blocs) + Chromium E2E 25 assertions (débits 1/8/64/512 ;
  usine forgée + 3 conduits + tour + eau : flux 1 MJ/s, load = 1/3, heatStore undefined ; saturation
  émission 5 > débit 3 → flux 3, load 1 ; heatWarn à 10 % avec secs=135 exact ; trip au tick 150 pile
  (= cap 300 MJ à +2 net/tick), chaleur figée à 300 ; chaleur en baisse → PAS de trip ; conduit V2 →
  débit 24, flux = absorption tour 4,096, load 0,171) + boot partie réelle 8 s (0 erreur console).
  Build 238→239.
  Changement 13.57 : **fix « la centrale redémarre à 0 après une mise à jour ».** Bug testeur.
  Diagnostic (E2E save forgée) : le RELOAD d'une centrale `running` est SAIN (reprise immédiate
  pleine puissance) — les vraies causes : (1) la machine à états n'avait AUCUNE récupération depuis
  `stopping` — un manque de combustible d'UN tick (stock U235 flottant près de 0 au moment de la
  save, hoquet au chargement, rattrapage hors-ligne) → arrêt complet PUIS recalibrage 5 min depuis
  0 ; (2) `nucFrom` (départ de rampe) n'était pas persisté → recharger PENDANT un calibrage
  repartait de 0. Fix : (a) branche `stopping` du tick : si `wireOk && fuelOK && targetFrac > 0` →
  repart en `starting` DEPUIS `nucCur` (rampe 5 min, cohérent avec le recalibrage existant) ;
  (b) `pl.nf = nucFrom` sérialisé/restauré (champ additif, `SAVE_VERSION` inchangé) ; vieille save
  sans `nf` en pleine rampe → nouvelle rampe DEPUIS `nucCur` (timer remis à 0). L'art V2 alternatif
  du zip uploadé reste IGNORÉ (décision utilisateur). Validé : `node --check` (7 blocs) + Chromium
  E2E (save forgée centrale+route+câble+U235 : reload running → +8,19 MW immédiat ; stopping tardif
  + fuel revenu → `starting` depuis nc (nf=100) ; reload mi-calibrage → rampe continue (+4,42 MW) ;
  vieille save sans nf → rampe depuis 4096, pas 0 ; sans fuel → s'arrête toujours ; 0 erreur
  console). ⚠ Piège E2E découvert : une centrale forgée SANS tour surchauffe (trip à 20 MJ) en
  ~10 ticks — un « +0 kW » au reload peut être la surchauffe, pas le bug. Build 237→238.
  Changement 13.56 : **jauge de chaleur = SPRITES du pack (`ui_jauge_mj_*`).** L'utilisateur a uploadé
  `Archipel_sprites_COMPLET.zip` (commit « Add files via upload ») contenant **9 sprites de jauge**
  `ui_jauge_mj_000..013..100` (8×32 VERTICAL, thermomètre orange, crans par HUITIÈMES, cadre rouge à
  100 %) → inlinés dans le bloc d'assignations `__SPRITE_DATA__`. Le draw de la jauge (13.55) choisit
  le sprite au cran le plus proche (`round(hf×8)`, min 1 dès qu'il y a de la chaleur) et le dessine au
  bord GAUCHE de la tuile (l=tile/4, h=×4, centré verticalement sur l'emprise) ; ≥ 80 % → pulsation
  d'ALPHA (au lieu du clignotement de couleur) + `_animPlayed`. La barre segmentée du 13.55 reste en
  REPLI si le sprite manque. ⚠ Le zip uploadé contient AUSSI un art V2 DIFFÉRENT (cimenterie_v2,
  centrale_charbon_v2, pompe_eau_v2, betonniere_v2 + sheets — style entièrement redessiné, vérifié au
  pixel ~700 px/sprite de diff) : **PAS intégré** (l'utilisateur n'a parlé que des jauges — à lui de
  confirmer quel art V2 fait foi). Validé : `node --check` (7 blocs) + Chromium E2E (2 usines forgées
  h=5/h=9 → capture : thermomètre mi-plein / presque plein + pulsation ; 0 erreur console du jeu).
  Build 236→237.
  Changement 13.55 : **tour aéroréfrigérante 1,024 MJ/s + joules à l'échelle des watts + jauge de
  chaleur « sprite ».** Demandes utilisateur. (1) **Rééquilibrage** : absorption de la tour
  0,768 → **1,024 MJ/s** (V1, ×2^upgrade) — 2 spots (`processHeat` absorbCap + `capA` fiche) →
  la centrale 8192 kW (2,048 MJ/s) = **exactement 2 tours V1**, l'usine moteur (1 MJ/s) = 1 tour.
  (2) **`fmtHeat` aligné sur `fmtPower`** : nouvelle marche **kJ** (entier) sous 1 MJ, puis MJ/GJ
  (mantisse fmtSig) — « 512 kJ », « 1,02 MJ », « 2,05 GJ ». Appliqué AUSSI à la fiche (les mini
  formateurs locaux `fmtH`/`fH`/`fmtN` remplacés par `fmtHeat`) : ligne Chaleur `X / cap · %`,
  Bilan chaleur, Refroidissement de la tour (` MJ/s évacués` → clé `/s évacués`, i18n en/es/de
  ajoutée), lignes 🔥 réel/théorique de la centrale. (3) **Jauge de chaleur sur tuile en
  « sprite »** (draw, remplace la barre pleine sans cadre) : icône **`ui_chaleur`** (drawSprite) à
  gauche + cadre pixel 1 px (#565b66) + fond sombre + **barre SEGMENTÉE 8 cellules** (≥1 allumée
  dès qu'il y a de la chaleur) ; couleurs vert/orange/rouge + clignotement ≥80 % conservés.
  Validé : `node --check` (7 blocs) + Chromium E2E (fmtHeat 6 cas exacts ; partie réelle avec 2
  usines moteur forgées h=5 et h=9 via addInitScript sur des tuiles de terre calculées par
  `buildIslandTiles` → capture : icône + cadre + segments, orange 4/8 et rouge plein, lisibles ;
  0 erreur console du jeu — les warnings « passive event listener » viennent des wheel synthétiques
  du test). Build 235→236.
  Changement 13.54 : **bouton de sortie de mode = croix seule dans la bannière d'état (retour
  testeur sur le 13.53).** Le gros bouton `.tool-quit` du 13.53 (au-dessus des ACTIONS) est RETIRÉ
  (render Toolbar + prop onQuit + câblage App + CSS). Il existait en fait DÉJÀ un « ✕ Quitter » dans
  la bannière `.status` (« <outil> — touchez une tuile… ») mais il était INVISIBLE sur mobile : le
  bandeau entier portait `white-space:nowrap + overflow:hidden` → dès que le hint était long, le
  bouton était rogné hors du bandeau. Fix : le hint est enveloppé dans un `<span.status-hint>`
  (min-width:0 + ellipsis) — la croix (désormais **« ✕ » seule**, padding réduit) reste TOUJOURS
  visible à droite du texte ; + SFX `deselect` au clic (manquait). Les clés i18n « Quitter »/« Quitter
  le mode en cours » du 13.53 restent (inertes). Validé : `node --check` (7 blocs) + Chromium E2E
  viewport 390px (mode Démolir : hint ellipsé, croix entière dans le viewport à droite du texte,
  `elementFromPoint` = le bouton, clic → mode quitté + croix disparue ; `.tool-quit` absent ;
  0 erreur console). Build 234→235.
  Changement 13.53 : **refonte nœuds 25→28 (+1 nœud, SAVE_VERSION 19) + tuto mix irradiés + alerte
  centrale sans tour + anim tour aéroréfrigérante + bouton Quitter + centrale 8192 kW.** Demandes
  utilisateur. (1) **Tech tree** : l'ancien nœud 25 (Usine+Mines V3+Arcs) est SCINDÉ —
  **25 « Usine Moteur Nucléaire »** (auto, produire 1000 acier irr. + 1000 béton irr. + 1000 câble
  irr., débloque `usine_moteur_nuc` seule ; le « 1000 câble irradié » en double du message utilisateur
  interprété comme les 3 irradiés), **26 « Mines V3 + Fours à Arc »** (auto, produire 100
  élém.moteur, débloque les 6 mines V3 + 2 arcs), **27 « Antenne T5 »** (prereq 25, inchangé sinon),
  **28 « Navire Futuriste »** (prereq 25, delivery inchangée). ⚠ Renommer/renuméroter = éditer AUSSI
  les 4 entrées LOCALES `tech` (fait, fr/en/es/de + entrée 28 ajoutée). **`SAVE_VERSION` 18→19**
  (+19 whitelist) : migration dans `loadSave` (< 19, sur `savedStatus` AVANT le map) — ancien 25
  confirmé ⇒ nouveaux 25 ET 26 confirmés (aucun déblocage perdu), statuts intermédiaires rétrogradés
  `available` (les conditions ont changé), ancien 26→27 et 27→28 copiés tels quels. (2) **Tuto
  `nuc_mix`** (GAME_TIPS, après l'astuce réseaux illimités, `when` = centrale débloquée) : sélecteur
  Une seule/Mix/Auto de la centrale, plutonium 4e option + plafond, matériau consommé seulement si
  livré au port. Sans scène d'illustration (TipIllustration → null) et non traduit (repli fr).
  (3) **Alerte démarrage sans refroidissement** : helper module `islandNuclearCoolingOk(game, isl)`
  (au moins une tour non pausée/non endommagée, alimentée en eau au dernier tick — `regime` null =
  jamais tickée = OK —, sur un conduit touchant l'emprise 2×2 d'une centrale) ; dans
  `setNuclearPower`, toute MONTÉE de puissance sans refroidissement OK → toast rouge + SFX
  `powerAlert` throttlé. (4) **Anim tour aéroréfrigérante** : sheet 128×32 du pack inlinée
  (`__ANIM_DATA__` + `ANIM_META` fps 4, frame 0 == statique vérifié 0 px) ; ⚠ la tour a SES DEUX
  clés statiques dans SPRITE_DATA (`tour_…` ET `bat_tour_…`) or `ANIM_BY_SK` retient la 1re
  candidate alors que `buildingSpriteKey` préfère `bat_…` → **alias explicite** ajouté après l'IIFE.
  Anime seulement si active (= alimentée en eau). (5) **Bouton « ✕ Quitter »** (`.tool-quit`, rouge,
  au-dessus de la barre ACTIONS) dès qu'un outil/mode est actif (Copier/Démolir/Améliorer/pose —
  affiche le nom du mode) : `onQuit` → SFX deselect + `deselectAll()` ; avant il fallait re-cliquer
  le même onglet. (6) **`NUC_POWER` 6144 → 8192 kW** (2 occurrences tick + fiche) → chaleur pleine
  puissance V1 = 2,048 MJ/s (3 tours V1 nécessaires au lieu de 2, conséquence assumée). i18n en/es/de
  (Quitter, toast alerte). Validé : `node --check` (7 blocs) + Chromium E2E (28 nœuds, defs/LOCALES
  exacts ; helper refroidissement 6 cas unitaires ; partie réelle : Démolir → bouton « ✕ Exit
  Demolish » visible, clic = mode quitté ; migration réelle v18 forgée via addInitScript →
  25/26 confirmés, 27 PRÊT, 28 disponible ; sheet 128×32 décodée + alias `ANIM_BY_SK` ; 0 erreur
  console hors fetch version.json offline). Build 233→234.
  Changement 13.52 : **notation scientifique complétée + nœud 24 simplifié + badge d'état de stock
  (Port).** Demandes utilisateur (4 captures). (1) **Notation scientifique** : la ligne « Débit max »
  du Port (`fmtInt` → `fmtPort` ×2) et le popover ressource (Production/Consommation/Bilan net :
  `fmtRate` → `fmtRateSci` ; Export/Import l'étaient déjà) passent en scientifique dès 1e5
  (« 163 840 u/s » → « 1,64e5 u/s », « +1048576 /s » → « +1,05e6 /s »). (2) **Nœud tech 24 (Centrale
  Nucléaire + Tour)** : mode `delivery` → **`auto`**, condition unique **produire 64 combustible_u235**
  (avant : 5 U235 + livraison acier 1000/béton 1500/proc 100/pièce 1000, supprimée). ⚠ Une save où le
  nœud est déjà `condition_ok` le reste (pas de rétrogradation) — validable d'un clic. (3) **Badge
  d'état du stock** (onglet « Transit île » du Port, sous « X en stock ») : petit badge coloré par
  ressource — `→ export` (orange), `← import` (bleu), `⇄ transit` (violet, reçu ET réexpédié),
  `▲ remplissage` (vert, prod locale nette > 0 et stock < cible), `⧖ en attente` (gris, cible non
  atteinte et rien n'arrive — la voisine n'a pas de surplus), `✓ cible atteinte` (vert atténué) ;
  rien si aucune cible et aucun flux. Données : `game.transitFlow` (flux réels sommés par île, calcul
  UNE fois par rendu dans `PortPanel` : `_tfExp`/`_tfImp`), `islandFlowAgg` (prod/conso locales),
  helper `stockStateFor(res, cfg)` ; CSS `.pp-state.{exp,imp,transit,fill,wait,done}` ; tooltips
  détaillés ; i18n en/es/de (bloc ADD). Affichage seul, `SAVE_VERSION` inchangé. Validé : `node
  --check` (7 blocs) + Chromium E2E (PortPanel rendu standalone avec état forgé : les 6 badges
  exacts + tooltips ; nœud 24 auto/64/sans delivery ; `fmtPort(163840)`=1,64e5,
  `fmtRateSci(1048576)`=1,05e6 ; 0 erreur console). Build 232→233.
  Changement 13.51 : **nombres électriques à 3 chiffres significatifs max.** Demande utilisateur.
  `fmtSig` (la mantisse partagée par `fmtPower`/`fmtEnergy`/`fmtEnergyPair`/`fmtHeat`) passe de
  « jusqu'à 2 décimales » (→ « 131,07 MW », 5 chiffres) à **3 chiffres significatifs** : 0 décimale
  ≥ 100, 1 décimale ≥ 10, 2 décimales en dessous, zéros de fin retirés → « 131 MW », « 65,5 MW »,
  « 1,02 MW », « 2,1 GW » (chaleur/batterie alignées : « 20,5 MWh », « 1,54 MJ »). Les branches
  < 1000 (kW/kWh entiers) sont déjà ≤ 3 chiffres. Edge assumé : ~999 950 kW s'affiche « 1000 MW »
  (frontière d'unité, transitoire). Affichage seul. Validé : `node --check` (7 blocs) + Chromium
  (boot 0 erreur ; `fmtPower/fmtEnergy/fmtHeat/fmtEnergyPair` vérifiés en jeu). Build 231→232.
  Changement 13.50 : **équilibrage (brief arc/softcap/broyeur V2) + 4 fixes UI** (brief
  `BRIEF_equilibrage_arc_softcap_broyeurv2` + zip `Archipel_sprite_broyeur_v2` + retours testeur).
  (1) **Fours à arc** : forfait d'entrée → **`{ element_moteur_nuc: 10 }`** (remplace béton/pièce/proc) ;
  déblocage déplacé du **nœud 19 → nœud 25** (celui de l'usine moteur nuc + mines V3). Nœud 19 renommé
  « **Densification Avancée** » (unlocks `betonniere_v2` + `broyeur_v2`), nœud 25 renommé « Moteur
  Nucléaire + Mines V3 + Fours à Arc ». ⚠ Les noms de nœuds sont RÉÉCRITS par `I18N.applyToData`
  (LOCALES `tech` par id, y compris fr) → renommer un nœud = éditer AUSSI les 4 entrées LOCALES.
  Effets assumés (brief §5) : partie ayant confirmé le 19 mais pas le 25 → ne peut plus POSER de
  nouveaux arcs (ceux posés continuent) ; démolition d'un arc → rembourse le NOUVEAU forfait.
  (2) **Bridage économique `COST_SOFTCAP_X2`** (atelier_meca, cablerie, acierie — PAS de cap dur) :
  au-delà du Nv. affiché 10 le facteur de coût DOUBLE à chaque cran (ratios 2,7 → 5,4 → 10,8 → 21,6 →
  43,2…) ; branche dans `upgradeCostFactor`, courbes puits/éolienne intactes. (3) **Puits de pétrole** :
  courbe `3,0 × 1,1^k` → **`3,0 × 1,2^k`** (ratios 3,00/3,60/4,32/5,18/6,22). (4) **Nouveau bâtiment
  `broyeur_v2`** (palier V2 du broyeur, `TIER_NEXT.broyeur cap 9`, forfait **5 processeur + 1000
  câble**, entrée u10) : recette **8 pierre + 8 eau → 1 silicium** (l'eau est AJOUTÉE au palier V2),
  `power: 96` PLAT (identique V1, pas de sigmoïde — décision D1 du brief), `cost: {}`, **PAS dans
  `TIER_NEXT`** → améliorable à l'infini (×2,7/cran, voulu) ; toolbar Électronique après le V1 ;
  **sprite + anim livrés** (zip) : `bat_broyeur_v2` 32×32 + sheet 128×32 4f fps8 inlinés (frame 0 ==
  statique vérifié 0 px). (5) **Fiche bâtiment — Élec. en AMPLITUDE** : la ligne des conso variables
  (sigmoïde/aléatoire) n'affiche plus la valeur instantanée qui sautait à chaque tick, mais l'amplitude
  fixe « min→max (amplitude) » (i18n en/es/de). (6) **Tiers ressources RETRIÉS** (15 réaffectations :
  pétrole/min.or/uranium→T0, polymère/diesel/silicium/acide/ling.or/yellowcake→T1, câble/acier/
  si.raffiné/comb.U235→T2, béton armé/pièce méca→T3) + **place FIXE dans l'inventaire** : l'ordre de
  déclaration de `RES_TIER` fait foi (`RES_ORDER_RANK`/`resOrderRank`, tri de l'inventaire du HUD) —
  fini l'alphabétique mouvant. Affichage seul, aucune save touchée. (7) **Fix tap-through (« clic
  fantôme »)** : le tap canvas qui OUVRE un panneau cliquait immédiatement un bouton rendu sous le
  doigt (« Baisser » de la fiche, toggle du Port…). Nouveau hook **`useGhostGuard(openKey)`** (module,
  après la destructuration React) : un clic légitime est toujours précédé d'un pointerdown DANS le
  panneau → tant qu'aucun pointerdown interne depuis l'ouverture (openKey = objet info/net/up recréé
  à chaque tap), le click est avalé en phase CAPTURE (indépendant du timing, jamais bloquant pour une
  vraie interaction). Appliqué à **InfoPanel** (2 racines : bâtiment + réparation/remblai),
  **UpgradePanel**, **NetworkPanel**, **PortPanel** (panneau ET backdrop — le fantôme pouvait fermer
  le panneau). (8) **Fix oscillation des confirmations** : `.ip-up.armed,.ip-down.armed` utilisait
  `animation:notifpulse` (= `transform:scale(1.35)`, prévu pour la pastille de notification 8 px) →
  le bouton pleine largeur « Confirmer ? » gonflait/dégonflait en boucle. Nouveau
  `@keyframes armedpulse` (pulsation de `filter:brightness` seulement, aucune géométrie) ;
  `notifpulse` reste réservé à `.notif-dot`. `SAVE_VERSION` inchangé (18 — `broyeur_v2` = id additif,
  tiers/UI = affichage). Validé : `node --check` (7 blocs) + Chromium E2E 37 assertions (données du
  brief exactes — ratios softcap/puits, coûts atelier 2 287 679/12 353 468/133 417 454/2 881 816 998 ;
  `cumulativeInvested('broyeur_v2', 10)` = chaîne V1 + forfait ; sprite décodé/mappé `ANIM_BY_SK` ;
  nœuds 19/25 + i18n ; Port réel : clic fantôme AVALÉ, vrai clic accepté, garde désarmé ensuite ;
  0 erreur console). Build 230→231.
  Changement 13.46 : **nouveaux coûts de forfait V2 + Bétonnière V2 (nouveau bâtiment) + animations
  sprites des V2.** Demande utilisateur (zip `Archipel_sprites_COMPLET`). (1) **Forfaits de densification
  revus** (`TIER_STEP`) : `four_fer_v2` ET `four_cuivre_v2` (« Four v2 ») → `{ piece_meca: 500,
  beton_arme: 500, circuit: 10 }` (avant circuit 50 + piece 2000 + béton 500) ; `cimenterie_v2` →
  `{ beton_arme: 500, acier: 500, circuit: 10 }` ; `centrale_charbon_v2` → `{ beton_arme: 500,
  piece_meca: 500, circuit: 10 }`. (2) **Nouveau bâtiment `betonniere_v2`** (palier V2 de `betonniere`,
  `TIER_NEXT.betonniere = {next, cap: 9}`, `TIER_STEP` forfait **50 processeur**, entrée u10 = Nv.11).
  Def : `tier t2`, `cost: {}`, `power: 0` + **`sigmoid {base:16, amp:112, period:60}`** (conso Nv.11 =
  **16384→131072 kW**), intrants **pierre 64 / minerai_fer 32 / eau 8** (base ; ×1024 à Nv.11 =
  65536/32768/8192 /s), sortie **beton_arme 1** (base → **1024/s** à Nv.11, valeur confirmée par
  l'utilisateur). Exempt du
  `TIER_COST_MULT` via le suffixe `/_v2$/`. **Débloqué avec les fours à arcs** (ajouté aux `unlocks.buildings`
  du **nœud tech 19**) ; ajouté au groupe toolbar « Ciment & béton ». Densification gatée par la recherche
  (13.26). (3) **Animations sprites** : le zip a livré les sheets 128×32 (4 frames) manquantes →
  `betonniere_v2` (static + sheet), `cimenterie_v2`, `centrale_charbon_v2`, `pompe_eau_v2` (sheets ; leurs
  statiques existaient déjà) inlinés dans le bloc d'assignations `window.__(SPRITE|ANIM)_DATA__[…]` +
  4 entrées `ANIM_META` → ces bâtiments s'animent désormais quand actifs (frame 0 == statique vérifié
  byte-à-byte). `SAVE_VERSION` inchangé (`betonniere_v2` = nouvel id additif, aucune migration). Validé :
  `node --check` (7 blocs) + Chromium E2E (boot 0 erreur ; def/sigmoïde/forfaits/TIER_NEXT-STEP-PREV/
  node 19/toolbar exacts ; sprite `betonniere_v2` 32×32, 4 sheets 128×32 décodées et mappées par
  `ANIM_BY_SK` ; `cumulativeInvested('betonniere_v2', 10)` OK). Build 226→227.
  Changement 13.45 : **fours à arc — intrants minerai ÷2 (fix erreur de calcul).** Demande utilisateur :
  les fours à arc consommaient 2× trop de minerai. Les DEUX débits d'entrée passent 8 → **4 /s** (base,
  ×2^upgrade ensuite) : `ARC_DEF.four_arc_fer.inRate` et `ARC_DEF.four_arc_cuivre.inRate` (source de
  vérité de la sim via `arcEffective`) + les recettes STATIQUES représentatives des blocs `four_arc_*`
  de BUILDINGS (`minerai_fer`/`minerai_cuivre` 8 → 4, repli du code lisant les champs statiques —
  cohérence). Conversion minerai→lingot : 8:1 → **4:1** (l'entrée est fixe quel que soit le mode ; les
  sorties, la conso sigmoïde et les ratios acier/pièce/câble par MINERAI restent inchangés → par unité
  PRODUITE tout coûte 2× moins de minerai). Les fours V1 (à charbon, 8 minerai) et V2 (4 minerai) sont
  INTACTS. Aucune sauvegarde touchée (`SAVE_VERSION` inchangé). Validé : `node --check` (7 blocs) + 16
  assertions unitaires (inRate/statiques = 4, sorties/sigmoïde/nominal/minPower INCHANGÉS, fours V1-V2
  non touchés) + Chromium E2E moteur (arc u20 réel : minerai consommé / lingots produits = **4,0000
  exactement**, demande 262144→2097152 lisse ratio 8.00, 0 erreur console). Build 225→226.
  ⚠ Piège harnais de test : un `eval()` direct fait FUIR les déclarations `function` dans le scope
  englobant (sloppy mode) → collision avec un destructuring `const` du même nom dans le module de test
  (« Identifier already declared ») ; nommer différemment les variables du test.
  Changement 13.44 : **panneau Aide en ACCORDÉON (titres seuls + dépliage avec illustration).** Demande
  utilisateur : le bouton Aide n'affiche plus les astuces complètes mais **uniquement leurs titres** ;
  un clic sur un titre déplie l'astuce complète — **illustration sprite** (`TipIllustration`, le même
  canvas `TIP_SCENES` que la popup d'astuce) + **texte riche** (`dangerouslySetInnerHTML`, fini le
  `stripHtml` — le gras est conservé comme dans la popup). Implémentation : `HelpPanel` gagne un état
  `openTip` (accordéon : UNE astuce dépliée à la fois, re-clic = repli) ; la tête de carte
  `.help-card-head` devient un `<button>` (reset CSS : `background:none;border:none;font:inherit`…,
  hover titre jaune) avec **chevron** `.help-card-chev` ▸/▾ ; `TipIllustration` + `.help-card-body`
  rendus SEULEMENT si dépliée (le canvas ne se dessine que là) ; SFX `click`/`panelClose` au
  dépliage/repli ; classe `.help-card.open` (liseré jaune), `.help-card .tip-illu` margin ajusté.
  Le filtre « astuces débloquées seulement » (13.41) et la section tutoriel sont INCHANGÉS. i18n
  en/es/de (« Voir l'astuce complète », « Replier »). Aucune mécanique/sauvegarde touchée
  (`SAVE_VERSION` inchangé). Validé : `node --check` (7 blocs) + Chromium E2E (partie neuve : 5 cartes
  titres seuls, 0 corps/0 canvas ; clic titre 1 → corps avec `<b>` + canvas 768×512 rempli 100 % +
  chevron ▾ ; clic titre 2 → accordéon (seule la 2e ouverte) ; re-clic → tout replié ; 0 erreur
  console). Build 224→225.
  Changement 13.43 : **refonte sigmoïdes + batterie + fours à arc (brief « Refonte sigmoïdes, batterie
  & fours à arc »).** (1) **Batterie** : capacité de l'accumulateur 20480 → **8192** (le repli `|| 512`
  d'`accCapacity` — socle V1 d'upgrade — est INTACT, voulu). (2) **Toutes les sigmoïdes en period 60** :
  `circuit` passait 150 → 60 (fab_processeur et enrichissement y étaient déjà). (3) **Ratio 1→8 partout**
  (pic conservé, plancher recalé à pic/8) : `fab_processeur` 16/1008 → **128/896** (128→1024), `circuit`
  64/192 → **32/224** (32→256), `centrale_enrichissement` 64/192 → **32/224** (32→256). (4) **Fours à
  arc : conso aléatoire → SIGMOÏDE** : `ARC_DEF` passe de `powMin`/`powMax` à **`powBase`/`powAmp`**
  (lingot 0,25/1,75 → 262144→2097152 à niv 21 ; acier 1,25/8,75 ; pièce 0,75/5,25 ; câble 0,5/3,5) ;
  `arcEffective` renvoie `sigmoid {base, amp, period: 60}` (un mix = combinaison linéaire des base/amp →
  le ratio 8 est conservé ; mix 50/50 acier+pièce = base 1/amp 7) ; **tick** : `effSigmoid` (arc OU
  `b.sigmoid`) prime, `effRandomP = null` pour un arc ; **`nominalPower`/`minPower`** routés via
  `arcEffective(bld, null)` (bornes exactes en single, indicatives en mix/auto — assumé) ; **InfoPanel** :
  `bSigmoid = arcIO ? arcIO.sigmoid : b.sigmoid` (la ligne Élec. d'un arc affiche la plage sigmoïde).
  **Option A retenue** : les `randomP {0.5, 1.5}` STATIQUES des blocs `four_arc_*` de BUILDINGS sont
  CONSERVÉS (repli du code lisant les champs statiques + drapeau « conso variable » des détecteurs
  réseau `hasVarCons`/`isEnergyConsumer`/`usesWireUI` — jamais tirés au sort pour un arc). (5) **Mines V3
  (les 6) + usine moteur nucléaire : randomP → sigmoid** : mines 0,0625-0,1875 → **{0.03125, 0.21875, 60}**
  (à niv 21 : 32768→262144, pic doublé assumé) ; `usine_moteur_nuc` {64, 512} → **{64, 448, 60}** (64→512,
  moyenne 288 inchangée, `heatCap: 10` intact). Ces bâtiments passent par la branche `b.sigmoid` des
  helpers — aucun câblage supplémentaire. (6) **Tuto accumulateur réécrit** (GAME_TIPS) : règle de
  dimensionnement chiffrée (production ≈ **5/8 du pic**, capacité ≈ **8× le pic**, exemple 1024 kW →
  640 kW + 8192 kWh) ; pas de nouvelle scène d'illustration requise. (7) **`SAVE_VERSION` 17→18** (+18 à
  la whitelist `loadSave` ; aucune migration nécessaire — `sigmoidT`/`randomPower` sont transitoires).
  Validé : `node --check` (7 blocs) + 67 assertions unitaires (arcEffective single/mix 50/50,
  nominal/minPower arc niv 21 = 2097152/262144, les 6 mines V3, usine, batterie, période 60 partout) +
  Chromium E2E (save forgée : arc réel — migré u20 par `migratePlacement`, confirmé — + éolienne u18 +
  route/câble illimités → demande élec. = **sinusoïde LISSE tick par tick, pic exact 2 097 152 kW**,
  fini les paliers aléatoires ; conversion 8:1 exacte (1e9 minerai → 1,25e8 lingots) ; save v18
  rechargée ; 0 erreur console). Build 223→224.
  Changement 13.42 : **fix sprites de connexion près d'une jonction (règle d'axe 13.18 appliquée au
  DESSIN).** Bug testeur (capture) : une mine V2 avec une jonction route/câble à l'Est (câble E-O à
  travers, route N-S) affichait un stub ROUTE côté Est alors que le raccord réel est du câble.
  Cause : le 13.18 a restreint la MÉCANIQUE (chaque porteur d'une jonction ne transmet que le long
  de SON axe) mais pas le DESSIN — (1) la boucle des **stubs sous les bâtiments** comptait une
  jonction voisine pour un porteur même si son axe ne fait pas face au bâtiment → le masque route
  gagnait le bit Est, puis la résolution de conflit 10.40 retirait ce bit au câble (stub route sur
  un raccord câble) ; (2) **`netConnectMask`** (sprites des tuiles d'infra) dessinait une branche
  vers une jonction non raccordée (même défaut). Fix : (1) le stub n'accepte une jonction voisine
  que si `junctionDirOk(..., -dr, -dc)` (elle transmet VERS le bâtiment) ; (2) `netConnectMask`
  gagne un 5e param opt-in `junctionAxis` (même garde), activé UNIQUEMENT à l'appel des sprites
  d'infra — PAS dans `junctionAxisH` (l'y activer créerait une récursion entre jonctions
  adjacentes). Dessin seul, aucune mécanique/sauvegarde touchée. Validé : `node --check` (7 blocs) +
  Chromium E2E avant/après sur save forgée (mine_fer_v2 u11 + route N + câble S + jonction E +
  câbles) : AVANT = morceau de route entre mine et câble à l'Est + fausse branche du câble sud de
  jonction ; APRÈS = câble direct, branche disparue ; unit `junctionAxisH`/`junctionDirOk` (câble
  horizontal, route verticale, route ne transmet pas vers l'Ouest) ; 0 erreur console. NB : la mine
  V1 (`power: 0`) ne se raccorde PAS au câble — le cas testeur est une mine V2+. Build 222→223.
  Changement 13.41 : **3 retours testeur UI + audit throttle.** (1) **Aide = astuces DÉBLOQUÉES
  seulement** : `HelpPanel` reçoit `game` et filtre `GAME_TIPS` (`tipsSeen[t.id] || t.when(game)`,
  try/catch — couvre astuces désactivées et « Revoir les astuces » qui vide tipsSeen) ; section
  renommée « Astuces débloquées » + note « 🔒 D'autres astuces se débloqueront en progressant. »
  si certaines sont masquées ; title du bouton Aide adapté. (2) **Bouton « Y aller » dans les
  alertes** : chaque ligne de l'`AlertsPanel` (stock ET énergie) gagne un bouton `.alert-go`
  (`onGoIsland` : `switchIsland` + fermeture, SANS ouvrir de panneau — voir la carte) ; le clic
  sur la LIGNE garde son comportement (île + Port/Énergie). ⚠ Les lignes passent de `<button>` à
  `<div>` (bouton imbriqué = HTML invalide) ; grid 4 colonnes. (3) **Indicateur ⚡/🔋 remonté dans
  la barre du HAUT, à droite de RECHERCHE** (`hud-side` : [Port][Recherche][⚡/🔋]) — annule
  l'emplacement barre d'inventaire du 13.40 ; même JSX (clic → panneau Énergie), classe
  `stocks stocks-inv` conservée (ligne + compact). (4) **Audit throttle (demande « Vérifier
  throttle »)** : RAS — `SFX.playThrottled` (cooldown par nom), alertes stock/énergie à TRANSITION
  (bip à l'entrée en alerte seulement, réarmé à la sortie) + throttle 6-8 s, toasts nucléaire/
  surchauffe throttlés 8/4 s, `ANIM_REDRAW_MS` (~10 FPS ambiance, interactions à 60 Hz),
  `scheduleSave` 500 ms + flush arrière-plan : tous corrects, aucun correctif. i18n en/es/de
  (Y aller, Aller à cette île, Astuces débloquées…). Validé : `node --check` (7 blocs) + Chromium
  E2E (stocks après RECHERCHE + absents de l'inventaire, clic ⚡ → Énergie ; aide nouvelle partie =
  5 cartes/32 + note 🔒 ; save forgée stockAlerts → ligne DIV + « Y aller » ferme sans panneau,
  clic ligne → Port ; 0 erreur console). Build 221→222.
  Changement 13.40 : **2 retours testeur UI.** (1) **Indicateur d'énergie déplacé** : le bloc
  `.stocks` (pastille ⚡ bilan kW + 🔋 batterie, clic → panneau Énergie, JSX inchangé) quitte le
  haut du HUD (`hud-side`, qui garde PORT/RECHERCHE) pour la barre d'inventaire, À DROITE du bouton
  Production — ordre : [INVENTAIRE][Production][⚡/🔋][réparation][alerte], états replié ET ouvert
  (`stocksBlk` défini dans l'IIFE du HUD). CSS `.stocks-inv` : disposition en LIGNE (la colonne du
  11.06 ne vaut que pour l'ancien emplacement) + pastilles compactées. (2) **Confirmations de la
  fiche bâtiment re-demandées à la réouverture** : l'effet de reset des états armés (Monter/
  Baisser/Pause/Démolir) dépend de l'OBJET `info` (recréé à CHAQUE tap) et plus de `[info.r,
  info.c]` — quitter la fiche puis re-toucher le MÊME bâtiment ré-exige le clic « Confirmer »
  (avant : l'état armé survivait au re-tap du même bâtiment). Les clics ENCHAÎNÉS sur « Monter »
  fiche ouverte restent sans re-confirmation (design 13.20). Validé : `node --check` (7 blocs,
  dev + testeur) + Chromium E2E (save testeur : stocks absents du haut, ordre inventaire OK, clic
  ⚡ → panneau Énergie ; aciérie : armé → monte → monte enchaîné, fermeture + re-tap → 1er clic
  N'A PAS monté + libellé « Confirm »). Build 220→221.
  Changement 13.39 : **badge « nouveau » sur les bâtiments fraîchement débloqués + bouton INVENTAIRE
  sans nombre.** (1) **Badge nouveau** : pastille jaune « nouveau » (`.tb-new`, i18n en/es/de) sur
  chaque bâtiment du menu Bâtiment/Réseau débloqué mais JAMAIS sélectionné ; `notif-dot` sur les
  onglets Bâtiment/Réseau tant qu'il en reste un VISIBLE. État `game.seenBuildings` (champ additif
  rétro-compatible, `SAVE_VERSION` inchangé) : sérialisé, restauré (`loadSave`), et si ABSENT (partie
  neuve ou vieille save) le boot initialise « tout ce qui est débloqué = vu » (pas de spam). Marquage
  « vu » à la première sélection (`selectTool` + scheduleSave). ToolButton reçoit `isNew`, Toolbar
  `seenBuildings` ; helper `hasNewIn(groups, gate)` pour les pastilles d'onglet. CSS : `.tool-btn`/
  `.tab-btn` passent `position:relative`. (2) **Bouton INVENTAIRE** : le nombre (`.inv-count`) est
  retiré de l'état replié (le libellé seul reste). Validé : `node --check` (7 blocs, dev + testeur)
  + Chromium E2E (save testeur : init sans badge ; « déblocage » simulé → 1 badge « new » + dot ;
  sélection → vu, persisté dans la save, badge et dot disparus ; INVENTORY sans chiffre).
  Build 219→220.
  Changement 13.38 : **capacité du réseau TUYAU ÷2.** Demande testeur. Nouvelle constante
  `PIPE_CAP_DIV = 2` appliquée dans `networkThroughput` (comme `WIRE_CAP_MULT` pour le câble) :
  tuyau V1=64, V2=512, V3=4096 /s (÷2 de plus en Difficile) ; route et câble inchangés. Tous les
  appelants passent déjà le type depuis 13.13 → panneau réseau, saturation, bannières et pools en
  héritent. Validé : `node --check` (7 blocs, dev + testeur) + Chromium moteur (partie Normal :
  pipe 64/512/4096, road 128/1024/8192, wire 512/4096/32768). Build 218→219.
  Changement 13.37 : **le menu Bâtiment (et Réseau) garde sa position de défilement.** Demande
  testeur : les panneaux `.build-panel` sont DÉMONTÉS à la fermeture → chaque réouverture repartait
  en haut de la liste (pénible pour reprendre un bâtiment du bas). Fix dans la Toolbar (toujours
  montée) : refs `buildScrollRef`/`netScrollRef` + helper `keepScroll(posRef)` = `{ref}` (callback
  qui restaure `el.scrollTop` au montage — clampé par le navigateur si la liste a raccourci) +
  `{onScroll}` (mémorise la position). Positions INDÉPENDANTES pour les deux menus. Aucune
  mécanique/persistance touchée (position non sauvegardée — session seulement, voulu). Validé :
  `node --check` (7 blocs, dev + testeur) + Chromium E2E (save testeur : menu Bâtiment scrollable
  790 px → scroll 250 → fermer/rouvrir → 250 conservé ; menu Réseau ouvre à 0, indépendant).
  Build 217→218.
  Changement 13.36 : **flush de sauvegarde au passage en arrière-plan (fix « Taille des badges
  réinitialisée au lancement »).** TOUTE la chaîne badgeScale (serialize `uiPrefs` → `loadSave` →
  sync React boot/Options → draw) est CORRECTE (vérifiée E2E avec la save du testeur). La vraie
  faille : les sauvegardes sont UNIQUEMENT débouncées (`scheduleSave`, 500 ms) et il n'y avait
  AUCUN flush quand l'app part en arrière-plan — Android gèle les timers JS d'une WebView cachée
  puis peut tuer le process → un réglage fait juste avant de quitter (geste typique pour une
  option) était perdu et « réinitialisé » au lancement suivant. Fix : listener `onHide`
  (`visibilitychange`→hidden + `pagehide`, dans l'effet canvas à côté d'`onResume`, retiré au
  cleanup) qui appelle `flushSave()` **seulement si `gameRef.current.saveTimer` est en attente**
  (une partie neuve jamais touchée n'écrit pas de save parasite — le ModeModal reste affiché au
  boot suivant). Protège au passage les 500 dernières ms de TOUTE action avant de quitter l'app.
  Validé : `node --check` (7 blocs, dev + testeur) + Chromium E2E (save testeur : slider 1.6→0.8,
  save en attente, localStorage ENCORE à 1.6 → dispatch hidden → localStorage à 0.8 → reload →
  0.8 restauré ; boot/reload/Options déjà vérifiés OK par ailleurs). Build 216→217.
  Changement 13.35 : **alerte électrique PAR RÉSEAU (composante câble).** Retour testeur : aucune
  alerte alors qu'un réseau était en déficit — `activeEnergyAlerts` comparait production/demande de
  l'ÎLE entière, or l'électricité circule par composante câble : un surplus sur un câble masquait le
  déficit d'un autre (HUD « +0 kW » sans alerte, bâtiment 0 % ⚡). Désormais l'alerte se déclenche
  PAR COMPOSANTE (via `game.wireInfo[isl]`, objets dédupliqués par identité — une composante est
  partagée par plusieurs nids) : demande non SERVIE (`served + 0.5 < demand`, couvre production
  insuffisante ET câble saturé) + batterie de la composante vide (`accStored`). L'`AlertsPanel`
  affiche « ⚡ Réseau en déficit · 0% batterie » (ou « N réseaux en déficit ») + livré/demandé de la
  pire composante ; repli île entière conservé (avant le 1er tick, sans wireInfo). Le badge ⚠ du HUD
  et le SFX `checkEnergyAlerts` en héritent (mêmes données). i18n en/es/de. Validé : `node --check`
  (7 blocs, dev + testeur) + Chromium E2E sur la save du testeur (île 2 : bilan global +1024 kW MAIS
  composante 11,78/12,03 MW → alerte `{isl:2, nets:1}`, bouton ⚠ présent, panneau « Grid in
  deficit · 0% battery 11,78 MW / 12,03 MW » ; 0 erreur console). Build 215→216.
  Changement 13.34 : **3 correctifs UX (retours testeur).** (1) **Toast « manque » sans décimales** :
  `missingFor` arrondit à l'entier SUPÉRIEUR (`Math.ceil`) + notation port (`fmtPort`) — fini
  « manque 352.49999999999994 ». (2) **Fiche bâtiment — cause élec. LISIBLE** : quand un bâtiment
  manque d'électricité, la ligne Vitesse précise le bilan du réseau électrique DU bâtiment
  (`firstWireNid` → `game.wireInfo`) : « — ce réseau produit X / Y demandés » (production
  insuffisante SUR CE CÂBLE) ou « — câble saturé : débit X / Y demandés » — car le HUD peut
  afficher un surplus qui est sur un AUTRE réseau (confusion réelle du testeur). i18n en/es/de.
  (3) **Bouton « ⏸ Mettre en pause » à 2 temps** (état `armedPause`, classe `.ip-pause.armed`
  jaune, libellé « Confirmer ? ») — même bug de tap-through que « Baisser » 13.17 : la fiche
  s'ouvre sous le doigt et le bouton 1-clic se déclenchait tout seul (« le bâtiment reconstruit
  était déjà en pause » : vérifié moteur — démolir un bâtiment en pause + reposer donne un
  bâtiment NEUF non pausé ; c'était le tap accidentel). La REPRISE (▶) reste à 1 clic.
  Validé : `node --check` (7 blocs, dev + testeur) + Chromium E2E sur la save du testeur (fiche
  par tap canvas réel : reprise 1 clic, 1er clic pause = armé SANS pauser, 2e clic = pause ;
  centrale coupée → fiche aciérie « this grid produces 7,68 MW / 12,03 MW requested » ; toast
  manque = entiers arrondis sup). Build 214→215.
  Changement 13.33 : **fix scission de réseau — plus de retour au niveau 1.** Démolir une tuile
  d'infra qui COUPE un réseau en deux réinitialisait la moitié « non première » au niveau 1 (et
  perdait son statut illimité) : dans le flood-fill de `rebuildNetworks`, `oldToNew[oldId]` ne
  mappait l'ancien réseau que vers le PREMIER fragment rencontré — seul lui héritait niveau/pool/
  heatStore. Fix : pendant le flood-fill, CHAQUE fragment hérite directement du `level` (max) et de
  `unlimited` de l'ancien réseau de ses tuiles ; le pool et le tampon thermique restent au premier
  fragment (pas de duplication de matière). Au passage : la passe de FUSION « traversée bâtiment »
  (10.59) reporte désormais aussi `unlimited` et `heatStore` (avant : perdus à la fusion — `unlimited`
  n'était reporté NULLE PART dans un rebuild ; il ne survivait que par la save). Aucun changement de
  coût (l'amélioration réseau se paie PAR TUILE → conserver le niveau des deux moitiés n'est pas un
  exploit ; la re-jonction reprend le max, et la pose d'une tuile sur un réseau haut paie toujours le
  rattrapage). Validé : `node --check` (7 blocs, dev + testeur) + Chromium moteur réel (route 7 tuiles
  V4 + câble V3 illimité : rebuild no-op → tout conservé ; démolition du milieu → les DEUX fragments
  V4 / V3+illimité, pool sur un seul fragment ; re-pose → un seul réseau V4, pool intact, illimité
  conservé). Build 213→214.
  Changement 13.32 : **illustrations d'astuces (canvas, zéro octet d'image) + refonte/extension de
  `GAME_TIPS` (17 → 32 astuces)** (brief BRIEF tip scenes). (1) **Moteur `TipScenes`** (IIFE module,
  inséré juste avant `function TipPopup`) : autotiling du littoral porté du jeu (écume/falaises/
  triangles/overlays), cache d'images propre (recrée des `Image` depuis les data-URL de
  `__SPRITE_DATA__` — aucun octet ajouté), `draw(canvas, scene, 4)` = terrain + sprites en NEAREST +
  repères vectoriels (badges/flèches/jauges/cadenas/interdit) ; **`TIP_SCENES`** = 32 specs
  déclaratives `{island, grid 4×6, ops[]}` (clé = id d'astuce, 1:1 avec `GAME_TIPS`) ; composant
  **`TipIllustration`** (1er rendu immédiat + `preload().then(redraw)`), inséré dans `TipPopup` entre
  `tip-head` et `tip-body`, renvoie `null` sans scène. (2) **CSS** `.tip-illu`/`.tip-illu-canvas` —
  ⚠ piège : la règle GLOBALE `canvas{position:absolute;inset:0}` (canvas de jeu) s'applique aussi ici
  → `.tip-illu-canvas` doit poser `position:static` (sinon le canvas recouvre toute la popup et
  intercepte les clics du bouton « Compris »). (3) **Astuces** : `jonctions` refondue (niveaux
  indépendants + pose directe sur réseau) ; `upgrade_vs_v2` (périmé depuis la densification 13.20-23,
  encore présent contrairement au brief) **remplacé** par `traverser` + `densifier` ; **14 ajouts** :
  `port`, `eolienne`, `centrale_charbon`, `priorite`, `centrale_diesel`, `four_arc_fer`,
  `four_arc_cuivre`, `puits_piege`, `construire_mer`, `liaisons_port`, `reserves`, `copier`,
  `plutonium`, `antenne_modes` — insérés dans l'ordre de progression recommandé du brief. `when` :
  ids vérifiés contre `BUILDINGS` (fours à arc = ids unifiés 13.22, sans repli anciens ids) ;
  **`port` déclenché par `tipResearchActionable(g)`** (pas le `g => true` du brief, qui l'aurait
  affiché en même temps que `bienvenue` — choix par défaut À VALIDER par Ethan, comme le maintien des
  9 propositions, toutes gardées). ⚠ Nouvelles astuces **non traduites** (repli fr en en/es/de —
  i18n à faire si souhaité). Aucune mécanique/sauvegarde touchée, `SAVE_VERSION` inchangé. Validé :
  `node --check` (7 blocs) + Chromium E2E (32 scènes dessinées, 0 vide, toutes les clés sprites +
  terrain autotilé présentes dans `__SPRITE_DATA__` ; nouvelle partie : popups bienvenue → recherche
  → port → priorite → copier avec canvas 768×512 rempli 100 %, clics OK ; captures jonctions V2×V3 et
  densifier Nv9→V2 fidèles ; 0 erreur console hors fetch `version.json` offline). Build 212→213.
  Changement 13.31 : **kickstart d'île protégé de l'export + lien « Cible ⇒ Réserve » unidirectionnel.**
  (1) **Stock de départ bloqué** : au déblocage d'une île (2-5), `applyUnlocks` pose désormais, pour
  chaque ressource du `ISLAND_KICKSTART`, la **réserve** (`tradeCfgFor(...).seuilExport`) au montant
  déposé (`Math.max` — ne rabaisse jamais une réserve préexistante) → le transit ne siphonne plus le
  coup de pouce vers les îles voisines dès le premier tick ; le joueur peut baisser la réserve pour
  ré-exporter. Ne s'applique qu'aux déblocages FUTURS (saves existantes : îles déjà ouvertes
  inchangées). (2) **Lien cible/réserve à sens unique** (le « Cible = Réserve » du 13.16 devient
  **« Cible ⇒ Réserve »**) : dans `setTradeCfg`, éditer la **cible** aligne la réserve dessus ; éditer
  la **réserve** ne touche plus la cible. L'activation du toggle aligne toujours réserve=cible pour
  les ressources déjà configurées (inchangé). Libellé + infobulle du bouton `.pp-link-reserve` et
  i18n en/es/de mis à jour (clés gettext renommées). `SAVE_VERSION` inchangé. Validé : `node --check`
  (7 blocs, dev + testeur après sed) + Chromium E2E (applyUnlocks île 3 forgée : 7 réserves = montants
  exacts, réserve préexistante 9999 conservée ; Port réel : lien ON, cible 5000 → réserve 5000,
  réserve 250 → cible INCHANGÉE à 5000 ; 0 erreur console). Build 211→212.
  Changement 13.30 : **édition TESTEUR — 2 APK construits depuis le MÊME fichier de jeu.** Pas de
  branche git séparée (une mise à jour = les DEUX APK d'un coup). (1) **Flag `const TESTER_BUILD =
  false;`** (juste au-dessus de `VERSION_URL`) : la CI le bascule à `true` par `sed` (ligne exacte
  `^const TESTER_BUILD = false;$` — NE PAS reformater cette ligne) pour produire la variante testeur,
  avec garde-fou grep qui fait échouer le build si la bascule rate. En testeur : **pas de mode dev**
  (toggle masqué dans les Options + garde `toggleDev`) ni de **mode rapide** (toggle « Mode rapide »
  masqué dans la ModeModal, garde `toggleFastMode` — couvre le chrono ET la création de partie —,
  span `N×` du playclock masqué, title adapté) ; ligne Version des Options suffixée « · test » /
  « · dev ». (2) **CI (`android.yml`)** : 2 builds gradle — DEV (`fr.archipel.industry`, libellé
  **« Archipel Ind. Dev »**, asset `ArchipelIndustryDev.apk`, remplace l'ancien `ArchipelIndustry.apk`
  et met à jour l'app déjà installée) et TESTEUR (`-PappId=fr.archipel.industry.tester`, libellé
  « Archipel Industry », asset `ArchipelIndustryTester.apk`, installable À CÔTÉ) ; les 2 APK publiés
  dans la release `apk-latest` + artifact ; vérif certificat sur les 2. (3) **`version.json`** : champ
  `apk` → APK dev (les installs existantes, qui lisent ce champ, migrent vers l'édition dev), nouveau
  champ **`apkTester`** ; le jeu choisit `TESTER_BUILD ? apkTester||apk : apk` aux 2 points de fetch
  (boot + « Vérifier les mises à jour »). `SAVE_VERSION` inchangé, aucune mécanique touchée. Validé :
  `node --check` (7 blocs, éditions dev ET testeur après sed) + Chromium E2E des 2 variantes (dev :
  toggle rapide + toggle dev + `N×` présents, clic chrono → 10× ; testeur : tout absent, clic chrono
  inerte, `TESTER_BUILD===true`, version « · test » ; 0 erreur console). Build 210→211.
  Changement 13.29 : **pause d'un bâtiment + 2 ajustements HUD.** (1) **Pause joueur** : nouveau bouton
  « ⏸ Mettre en pause » / « ▶ Reprendre la production » (`.ip-pause`, orange/vert, au-dessus de
  Démolir) dans la fiche bâtiment (kind `build`, non-fixe). En pause : `bld.paused` → **skip en tête
  de la boucle bâtiment** de `tickIsland` (AVANT les branches accu/nucléaire → vaut pour tous :
  active=false, discReason='paused', regime=0, heatEmit=0 → ne consomme/produit RIEN, ni élec., ni
  chaleur) ; **pré-pass antenne** : antenne en pause = aucune zone d'influence ; **`processHeat`** :
  tour en pause n'évacue plus (heatAbsorb=0), chaleur résiduelle gelée. Carte : icône `etat_arret`
  (repli `statusSpriteKey` — inactive + discReason inconnu). Fiche : « Vitesse 0% · en pause (par le
  joueur) ». Handler App `togglePauseBuilding` (SFX toggleOff/On + toast). **Persistance** : `pl.pz = 1`
  (champ additionnel rétro-compatible, `SAVE_VERSION` inchangé), restauré dans `loadSave`. (2) **Boutons
  INVENTAIRE/Production −10 %** (`.inv-label-btn` .78→.70rem pad 12→10, `.inv-prod-btn` .62→.56rem
  icône 12→11px). (3) **Bouton réparation d'île déplacé À DROITE de Production** (états replié ET
  ouvert : [INVENTAIRE][Production][réparation][alerte]). i18n en/es/de. Validé : `node --check`
  (7 blocs) + Chromium E2E (fiche mine : ⏸ → « Speed 0% · paused (by you) » + bouton vert ▶, save
  `pz:1`, reload → toujours en pause, reprise → `pz` retiré ; ordre HUD et tailles vérifiés).
  Build 209→210.
  Changement 13.28 : **réseaux — béton armé et acier RETIRÉS des coûts possibles.** Un seul matériau
  d'amélioration par type de réseau : route = ciment, tuyau = lingot de fer, câble = câble. (1)
  **`NETWORK_HI_MATS`** : `premium: null` pour road ET pipe (plus de bascule auto cheap→premium du
  13.13 ; `networkUnitCost` garde sa branche premium, inerte). (2) **`NETWORK_UPGRADE_COST`** (table du
  rattrapage de niveau à la pose, `tbl[3]` = cran 3→4) : road `beton_arme: 100` → **`ciment: 800`**,
  pipe `acier: 100` → **`lingot_fer: 800`** (aligné sur la vraie formule cheap 800×4^k — le rattrapage
  ne coûte plus de matériau premium ET ne sous-facture plus). (3) **NetworkPanel** : le bloc de
  bascule auto cheap→premium (13.13) supprimé (code mort) ; `hiMats` conservé (bouton illimité
  irradié, INCHANGÉ : `beton_arme_irradie`/`acier_irradie` restent le forfait « illimité »).
  Validé : `node --check` (7 blocs) + Chromium E2E (route 12 tuiles posée en jeu, montée V1→V4 via le
  panneau : 120 / 1200 / 9600 ciment payés — jamais de béton armé ; port fourni en béton armé/acier
  pour contre-épreuve). Build 208→209.
  Changement 13.27 : **pose directe V2/V3 au niveau d'entrée + remboursement symétrique + élec.
  mines V2 ÷8.** (1) **Pose directe d'un bâtiment de palier** (`tryPlace`, après `t.building = {…}`) :
  `if (TIER_STEP[id]) t.building.upgrade = tierEntry(id)` → un V2 posé depuis la barre d'outils démarre
  à **u=10 (Nv.11)**, un V3/arc à u=20 — cohérent avec le cumul payé (avant : posé Nv.1 pour le prix du
  cumul, et `cumulativeInvested(id, 0)` ne remboursait même pas le forfait à la démolition → perte
  sèche). (2) **Migration TOUTES versions** (tête de `migratePlacement`, AVANT le garde `fromV >= 16`) :
  un bâtiment de palier avec `p.u < tierEntry` est remonté à l'entrée (répare les placements Nv.1 des
  builds 202-207 ; ne touche pas les V2 ≥ entrée). `SAVE_VERSION` inchangé. (3) **Rééquilibrage** : les
  4 mines V2 (`mine_fer_v2`/`mine_charbon_v2`/`mine_cuivre_v2`/`carriere_v2`) passent de `power: 1` à
  **`0.125`** (÷8 ; à l'entrée u10 : 128 kW au lieu de 1024 kW ; fours V2 et `pompe_eau_v2` — déjà à
  0,125 — inchangés). Validé : `node --check` (7 blocs) + Chromium E2E (save forgée : v2 u3 → fiche
  Nv.11 après load (migration) ; fiche v2 u10 = Nv.11 / **128 kW** ; démolition → remboursement =
  pierre 242 214 + forfait (acier 500, câble 500, ciment 1000) ; re-pose barre d'outils sur tuile
  ressource → u10 et **prix payé == remboursement** au près). Build 207→208.
  Changement 13.26 : **2 fixes UI — bouton Alerte compacté + densification visible/verrouillée dans la
  fiche bâtiment.** (1) **Bouton Alerte** (`.inv-alert-btn`) réduit (~35 % : font .66→.58rem, padding
  2×8→1×4, gap 5→3, icône 12→10 px, badge .55rem) → tient sur la MÊME ligne que INVENTAIRE/Production
  (il passait à la ligne sur mobile ; ≈38 px). (2) **Fiche bâtiment (InfoPanel) au cap de palier** : le
  bouton « Monter » (qui proposait Nv.11 et échouait en silence — `tryUpgrade` retourne false au cap)
  devient **« ✦ Densifier »** (2 temps comme Monter : 1er clic = aperçu `.ip-up-preview` « Densification
  → <nom> » + pastilles du forfait, 2e = `tryDensify` ; fiche FERMÉE à la réussite car elle capturait
  l'ancien bâtiment). **Recherche manquante** → bouton VISIBLE mais grisé 🔒 (`.densify-btn.locked`,
  disabled) avec le **nom de la recherche requise** en sous-libellé (pédagogie). Calculs : `tierLink`/
  `atCap`/`densUnlocked` (via `isBuildingUnlocked`)/`densCost`/`canDens`/`densNode` (props `onDensify`
  câblée sur l'instance). (3) **Même verrou dans l'UpgradePanel** (outil Améliorer) : bouton grisé 🔒 +
  ligne de coût « 🔒 Recherche requise : <nœud> » si non débloqué. (4) **`tryDensify` gate la recherche**
  (filet : toast rouge « 🔒 Recherche requise : … » + SFX invalid) — avant, on pouvait densifier vers un
  bâtiment PAS ENCORE débloqué par la recherche. CSS : `.ip-up.densify-btn` (violet) + `.locked` (gris).
  i18n en/es/de (Densifier/Densification/Recherche requise). Validé : `node --check` (7 blocs) +
  Chromium E2E (save forgée mine_fer u9 : fiche SANS recherche → bouton locked/disabled, sub = nom du
  nœud, plus de « Monter » ; nœud 7 confirmé → « ✦ Densifier », armé = aperçu forfait, confirmation →
  toast « ✦ Mine Fer V2 » + fiche fermée ; bouton alerte synthétique ≈38 px). Build 206→207.
  Changement 13.25 : **option « Production hors-ligne » (désactivable).** Nouveau toggle dans les
  Options (au-dessus de « Calcul hors-ligne simplifié ») : préférence `ui.offlineEnabled` (défaut
  **true** = comportement historique ; pattern `uiPrefs` complet : newGame, serialize, loadSave avec
  rétro-compat champ absent = activée, state React, sync au load + à l'ouverture des Options,
  `SAVE_VERSION` inchangé). Désactivée → **garde en tête de `runCatchUp`** : AUCUN rattrapage (ni
  production, ni `playTicks`, ni overlay/récap) ; les horloges `lastSave`/`lastActiveTs` sont
  **réarmées** (dans la garde ET dans `toggleOffline` à la désactivation) pour que le temps d'absence
  ne soit jamais rattrapé rétroactivement (réactivation de l'option, retour d'arrière-plan) — couvre
  les DEUX chemins d'appel (boot `applyOfflineProgress` + resume visibilitychange). i18n en/es/de.
  Validé : `node --check` (7 blocs) + Chromium E2E (toggle ON par défaut ; OFF persisté dans
  `uiPrefs` ; save antidatée de 2 h + option OFF → reload sans overlay/récap, chrono inchangé ;
  contre-épreuve option ON → 2 h créditées, chrono 02:00:10 + récap). Build 205→206.
  Changement 13.24 : **Mode Rapide intégré + chronomètre cliquable** (brief `BRIEF_MODE_RAPIDE_INTEGRE`).
  On accélère le TEMPS (plus de ticks/s), jamais les débits — aucun équilibre modifié. (1) **État**
  (init App, à côté de `tickAcc`) : `playTicks` (temps de jeu simulé en ticks, PERSISTÉ), `timeScale`
  (1|10, NON persisté — repart à 1 au boot), `_sfxPrev` (mémo audio avant coupure auto). (2) **Boucle
  `frame`** : accumulation `+= min(dt×_ts, _ts)`, plafond `_maxTicks = _ts>1 ? _ts*5 : 5`,
  `g.playTicks++` par tick simulé. (3) **Offline** : `runCatchUp` crédite `playTicks += ticks` UNE fois
  en tête (la boucle frame ne tique pas pendant le rattrapage ; l'extrapolation simplifiée représente
  quand même ce temps — pas de double comptage). (4) **Persistance** : `SAVE_VERSION 16→17` (+17 à la
  whitelist), `playTicks` dans `serialize` ; au load : défaut 0 (< 17), `timeScale = 1`, `_sfxPrev =
  null`. ⚠ Le bloc `audio.enabled` de `serialize` persiste désormais le CHOIX joueur (`_sfxPrev`) quand
  `timeScale > 1` (le mute auto du mode rapide n'écrase plus le réglage sauvegardé). (5) **UI** : helper
  module `fmtPlaytime(ticks)` (HH:MM:SS, après `fmtHeat`) ; bouton `.playclock` (1er enfant du
  `toolbar-wrap`, au-dessus de la barre de bâtiments ; props `playTicks`/`timeScale`/`onToggleFast` de
  la Toolbar, re-rendu via le `setInterval(bumpClock, 1000)` existant) affichant temps + `N×`
  (`.playclock-fast` liseré jaune en 10×) ; handler App `toggleFastMode` (10× → mute auto avec mémo
  `_sfxPrev` ; 1× → restaure SANS écraser un mute volontaire ; `toggleAudio` met à jour `_sfxPrev` si
  réglé manuellement PENDANT le rapide). (6) **Création de partie** : toggle « Mode rapide » (défaut
  OFF, `.mode-fast-row`) dans la **ModeModal** (l'écran « Choisis ton mode de jeu » — PAS le SlotPanel :
  `slotCreate` recharge la page avant de créer l'état, la ModeModal est l'écran de création effectif) →
  `chooseMode(mode, fast)` appelle `toggleFastMode()` si coché. i18n en/es/de (« Mode rapide », tooltip
  chrono, desc toggle). Validé : `node --check` (7 blocs) + Chromium E2E (fmtPlaytime 0/3661/86399
  exacts ; 1× ≈ 1 tick/s, clic → 10× = 40 ticks/4 s + classe fast, retour 1× OK ; création avec toggle
  → 10× immédiat ; reload → `playTicks` restauré de la save v17, `timeScale = 1`, `audio.enabled = true`
  malgré le mute auto actif à la sauvegarde ; save v16 forgée sans `playTicks` → charge sans erreur,
  chrono repart à 0). Build 204→205.
  Changement 13.23 : **Phase 4 (finale) densification — migration des sauvegardes + `SAVE_VERSION`
  15→16** (brief `BRIEF_PHASE4_MIGRATION` ; la refonte paliers/densification est COMPLÈTE). (1)
  **Version** : `SAVE_VERSION = 16` ; 16 ajouté à la whitelist de `loadSave` (liste unique). (2)
  **Migration < 16** (module, après `arcDefaultState`) : `ARC_MODE_FROM_OLD` (four_arc_acier →
  four_arc_fer/acier, four_arc_piece → four_arc_fer/piece_meca, four_arc_cable →
  four_arc_cuivre/cable) + `migratePlacement(p, fromV)` (mutation en place : anciens arcs → arc
  unifié mode single équivalent + poids par défaut ; bâtiment de palier (`TIER_STEP`) → `p.u =
  tierEntry` ; V1 au-delà du nouveau cap (`TIER_NEXT`) → `p.u = cap` ; **pas de remboursement**).
  Appelée en TÊTE de la boucle de placements de `loadSave`, AVANT le garde `!BUILDINGS[p.b]` → les
  anciens arcs sont convertis au lieu d'être droppés ; tout autre id inconnu reste ignoré sans crash.
  Les changements de recettes (centrale charbon, raffinerie, polymère, coût pompe) ne nécessitent
  AUCUNE migration (la save ne stocke que id+niveau). (3) **Sérialisation/restauration `pl.arc`** :
  déjà en place depuis 13.22 (rétro-compatible), inchangée. Validé : `node --check` (7 blocs) +
  Chromium E2E (save v15 forgée : chargement 0 erreur ; les 3 anciens arcs → arcs unifiés u20/mode
  correspondant, 0 id résiduel ; four_fer_v2 u17→10, mine_fer_v3 u3→20, mine_or/uranium_v3 →10 ;
  four_fer u14→9 (cap), four_fer u5 et acierie u12 INTACTS, cimenterie u11→9 ; re-save = version 16 ;
  2e reload+resave v16 : modes d'arc et niveaux CONSERVÉS — la migration ne retouche pas les v16).
  Build 203→204.
  Changement 13.22 : **Phase 3 densification — four à arc UNIFIÉ + sélecteur multi-sortie par bâtiment**
  (brief `BRIEF_PHASE3_ARC_UNIFIE`). (1) **2 nouveaux bâtiments** `four_arc_fer`/`four_arc_cuivre`
  (flag `arc: true`, `cost: {}`, t3 ; I/O statiques = REPRÉSENTATIFS du mode lingot par défaut, repli
  pour le code lisant les champs statiques). (2) **Données module** (après `tierForfait`) : `ARC_DEF`
  (input fixe `inRate` 8, `order`, par sortie {out, powMin, powMax} — fer : lingot 1 (0,5-1,5 kW) /
  acier 0,125 (2,5-7,5) / pièce 0,125 (1,5-4,5) ; cuivre : lingot 1 / câble 1/12 (1-3)) ; `arcWeights`
  (single/mix/auto, mirror `nucAutoWeights` — auto = stock port le plus bas favorisé) ; `arcEffective`
  (I/O + fourchette conso pondérés, base avant ×2^upgrade ; **l'entrée reste FIXE**, poids nuls →
  repli 1re sortie) ; `arcDefaultState` (single/lingot, poids 1). (3) **Paliers** : `TIER_NEXT` +=
  four_fer_v2/four_cuivre_v2 → arcs (**cap 19**, fini l'« améliorable à l'infini » de la phase 1) ;
  `TIER_STEP` += les 2 arcs (entry 20, forfait 2000 béton armé + 1000 pièce + 100 processeur). (4)
  **Init `bld.arc`** : `tryDensify` (après transform) + `tryPlace` (après pose). (5) **`tickIsland`**
  (tête de boucle bâtiment, après `mult`) : `arcEff = ARC_DEF[bld.id] ? arcEffective(bld, workPort) :
  null` → `effInputs`/`effOutputs`/`effRandomP` substitués à `b.inputs`/`b.outputs`/`b.randomP` dans
  TOUTE la boucle (basePower, `eligible`, `inByType`/`outByType`, energyOut ; le dépôt aval passe déjà
  par `outByType`). (6) **Suppression des 3 anciens arcs** (`four_arc_acier`/`_cable`/`_piece`) :
  BUILDINGS, `BLD_SPRITE_OVERRIDE` (ligne `four_arc_meca`), toolbar steel/copper (remplacés par les 2
  unifiés), nœud tech 19 → `['four_arc_fer','four_arc_cuivre']` (name/reqs/prereq intacts). Restes
  inertes voulus : sprites/anims/i18n data. ⚠ Saves avec anciens arcs : tuiles **droppées** au
  chargement (garde `!BUILDINGS[p.b] → continue` préexistante) — conversion en phase 5. (7)
  **Persistance** (rétro-compatible, `SAVE_VERSION` inchangé) : `serialize` émet `pl.arc =
  {mode,sel,w}` ; `loadSave` restaure avec défauts/validation (`arcDefaultState` merge). (8) **UI** :
  `InfoPanel` — les lignes Entrées/Réel/Sortie/Élec./aperçu d'amélioration passent par
  `arcIO = arcEffective(...)` (`bIn`/`bOut`/`bRandomP`) → la fiche montre le mode courant, pas le
  statique ; **bloc sélecteur** (classes `ip-nuc-*` réutilisées, SANS curseur de puissance) : ligne
  I/O effective « − entrée /s (fixe) → + sorties », 3 boutons de mode, boutons par sortie (single),
  sliders 0-100 pas 5 + % normalisé (mix), barres lecture seule (auto) ; setters `setArcMode`/
  `setArcSel`/`setArcWeight` (état PAR bâtiment `t.building.arc`) + props `onSetArc*`. Hors scope :
  migration/conversion des saves (phase 5). Validé : `node --check` (7 blocs) + Chromium (boot 0
  erreur build 203 ; `arcEffective` : single lingot/acier, mix 50/50 (sortie ET conso pondérées,
  entrée fixe), auto (stock bas favorisé), câble 1/12, poids nuls → repli ; cumul
  `cumulativeInvested('four_arc_fer',20)` = four_fer 0..9 + v2 10..19 + arc 20 ; toolbar/nœud 19/
  override/sprites 32×32 OK ; grep anciens ids = data inerte seulement ; E2E A : four_fer_v2 u19 →
  « Densifier → Four à Arc Fer » → arc u20 + `pl.arc` single/lingot + forfait débité exact ; E2E B :
  arc réel posé (route→port, éolienne+câble) → +1 lingot/s à −8 minerai/s régime 100 %, bascule
  « acier » via le sélecteur de la fiche → +0,125 acier/s, lingot FIGÉ, minerai toujours −8/s,
  `pl.arc.sel='acier'` persisté). Build 202→203.
  Changement 13.21 : **Phase 2 densification — contenu V2 (bâtiments, recettes, nœuds, rééquilibrage,
  assets)** (brief `BRIEF_PHASE2_CONTENU_V2` + `archipel_new_assets.js`). (1) **3 nouveaux bâtiments**
  (après `four_cuivre_v2`, `cost: {}` car paliers via `TIER_STEP`, exempts `TIER_COST_MULT` via
  `/_v2$/`) : `cimenterie_v2` (4 pierre + 0,5 fer + 0,5 eau → 1 ciment, 0 kW — intrants fractionnaires
  VOULUS, n'existe qu'à upgrade ≥10), `pompe_eau_v2` (1 eau, 0,125 kW, côte), `centrale_charbon_v2`
  (8 charbon + 2 eau → 128 kW, posable land/resource/coast). (2) **Paliers branchés** : `TIER_NEXT`
  += cimenterie/pompe_eau/centrale_charbon (cap 9, pas de V3 → V2 améliorable à l'infini) ; `TIER_STEP`
  += les 3 forfaits (circuit 10+béton 1000 / béton 500+polymère 100 / béton 1000+pièce 500+circuit 100).
  (3) **Recettes** : `centrale_charbon` 64→128 kW ; `raffinerie` diesel 1→3, power 16→32 ;
  `usine_polymere` pétrole 16→8, eau 4→2, +pierre 4 (sortie/power inchangés) ; coût `pompe_eau` V1
  {pierre:10,lingot_fer:10}→{ciment:50,lingot_fer:50}. (4) **Rééquilibrage socle ×2^upgrade** (inputs/
  outputs/power SEULS, costs intacts) : fours V2 = 4 minerai→1 lingot (SANS charbon), power 1 ; mines V2
  = output 1, power 1 ; les 6 mines V3 = output 1, `randomP {min:0.0625, max:0.1875}` (moyenne 0,125 kW
  → ~128 MW à niv 21) — l'`element_moteur_nuc` reste uniquement dans le coût/forfait (jamais un intrant).
  (5) **Toolbar** : les 3 V2 ajoutés aux groupes extraction/cement/energy ; **coût de pose affiché =
  cumul** pour les bâtiments de palier (`ToolButton` : `cost = TIER_STEP[id] ? cumulativeInvested(id,
  tierEntry(id)) : b.cost`, sous-libellé inclus ; même substitution dans `BuildingDetailModal`).
  (6) **Tech tree — échange nœuds 7↔13** (reqs/prereq INCHANGÉS) : nœud 7 « Upgrades V2 — Extraction »
  débloque les 4 mines V2 (plus tôt, acier/cuivre) ; nœud 13 « Upgrades V2 — Transformation » débloque
  four_fer_v2/four_cuivre_v2 + les 3 nouveaux V2 (circuit). (7) **Assets** : 5 sprites inlinés dans le
  bloc d'assignations `__SPRITE_DATA__` (cimenterie_v2, pompe_eau_v2, centrale_charbon_v2 + four_arc_fer/
  four_arc_cuivre pour la phase 3) ; méthode SFX `densify()` (arpège + power-up + clunk) inlinée après
  `downgrade` ; `tryDensify` joue `densify` (fini le repli `upgrade`). Hors scope : four à arc unifié
  (phase 3), migration saves/`SAVE_VERSION` (phase 5), bétonnière V2/nucléaire/pétrole (plus tard).
  Validé : `node --check` (7 blocs) + Chromium (boot 0 erreur build 202 ; toutes les defs/paliers/
  recettes/toolbar/nœuds vérifiés par assertions ; 5 sprites décodés 32×32 ; `SFX.play('densify')` sans
  throw ; cumul pose pompe_eau_v2 exact ; E2E : cimenterie u9 → « Densifier → Cimenterie V2 » → clic =
  cimenterie_v2 u10 + forfait débité exactement ; ToolButton cimenterie_v2 = 4 pastilles de cumul,
  pas « gratuit »). Build 201→202.
  Changement 13.20 : **Phase 1 densification — moteur de paliers (V1→V2→V3) + bouton « Densifier » +
  courbe éolienne accélérée** (brief `BRIEF_PHASE1_DENSIFICATION`). (1) **Données module** (après
  `UPGRADE_SCALE`) : `TIER_NEXT` (id → {next, cap} ; mines+fours cap 9, mines V2 cap 19, or/uranium
  sautent V2), `TIER_STEP` (id de palier → {entry 10|20, forfait plat}), `TIER_PREV` (auto-dérivé),
  helpers `tierEntry`/`tierForfait`. (2) **Coût** : `upgradeCostFactor` gagne la branche `eolienne`
  (niveau ≥10 → même courbe accélérée que le puits, ×2,7⁹ puis ×3,0×1,1^k ; puits inchangé) ;
  `upgradeCost` branche sur `TIER_STEP` (coût d'un cran = forfait × 2,7^(level−entry), le cran d'entrée
  = forfait plat) sinon chemin historique ; nouvelle `cumulativeInvested(id, upgrade)` (remonte la
  chaîne V1→…→id, somme pose+montées+forfaits ; `cumulativeUpgradeCost` conservée pour les jonctions).
  (3) **Actions** : nouvelle `tryDensify(r,c)` (au cap → paie le forfait, transforme sur place :
  `building.id = next`, `upgrade = entry` ; SFX 'upgrade' en hook, TODO son dédié) ; `tryUpgrade`
  bloqué au cap (`lvl >= link.cap`) ; `tryDowngrade` bloqué au 1er niveau d'un palier
  (`lvl <= tierEntry`) ; `tryPlace` : pose directe d'un bâtiment de palier = `cumulativeInvested(id,
  tierEntry(id))` ; `tryDemolish` : remboursement = `cumulativeInvested` (jonctions inchangées).
  (4) **UI** : `UpgradePanel` reçoit `onDensify` ; au cap, la ligne de coût devient « Densification »
  (forfait) et le bouton devient « ✦ Densifier → <nom> » (`.up-btn.densify-btn` violet, CSS ajouté) ;
  câblage `onDensify: tryDensify` sur l'instance. **Hors scope (phases 2+)** : valeurs de base des
  V2/V3, nouveaux bâtiments (cimenterie_v2…), four à arc unifié, swap tech, migration saves.
  Validé : `node --check` (7 blocs) + Chromium (boot 0 erreur ; moteur de coût : forfait plat,
  ×2,7/cran, chaîne `cumulativeInvested` V1+V2+V3 exacte, éolienne ×3 au niveau 10, puits/hors-palier
  inchangés ; rendu `UpgradePanel` 2 branches ; E2E : four_fer u9 → « Densifier → Four Fer V2 » →
  clic = four_fer_v2 u10 + port débité du forfait exact ; démolition d'un four_fer_v2 u12 →
  remboursement = `cumulativeInvested` au près). Build 200→201.
  Changement 13.19 : **plafonnement du redessin d'ambiance à ~10 FPS (« Levier 1 » — chauffe/batterie).**
  Le canvas était redessiné à ~60 FPS en continu : le dirty-checking (`g.dirty`) ne servait jamais car
  chaque frame animée re-marquait `g.dirty` via `_animPlayed` (eau/écume/machines toujours animées →
  redraw permanent → CPU/GPU saturés sur mobile). Fix chirurgical (2 modifs) : (1) nouvelle constante
  module `ANIM_REDRAW_MS = 100` (après `_animPlayed`, ~L2329). (2) Dans `frame`, le re-déclenchement
  d'animation `if (_animPlayed) g.dirty = true;` devient un **garde temporel** : `if (_animPlayed && now
  - (g.lastAnimTs || 0) >= ANIM_REDRAW_MS) { g.dirty = true; g.lastAnimTs = now; }` → l'ambiance ne
  redessine qu'à ~10 FPS. **Canal interaction inchangé** : `markDirty` (pan/hover/zoom/placement/
  sélection), le tick horloge et le cargo (`boatActiveNearPort` garde son `g.dirty = true`
  inconditionnel) restent servis immédiatement au prochain rAF (60 Hz maintenu). `g.lastAnimTs` géré via
  `|| 0` (pas d'init dans le constructeur). Aucune logique de simulation touchée ; UI React (useReducer/
  useState, hors rAF) non concernée. `node --check` (7 blocs) OK. Build 199→200.
  Changement 13.18 : **jonction = CROISEMENT strict (pas de diffusion perpendiculaire).** Demande
  utilisateur (anti-abus). Une jonction porte deux réseaux qui se croisent ; jusqu'ici chaque porteur
  se connectait sur les **4 côtés** → un porteur pouvait « diffuser » son réseau perpendiculairement à
  sa traversée (ex. câble N-S qui traverse ET route qui diffuse N-S par la même jonction). Désormais
  **chaque porteur ne transmet QUE le long de SON axe** (2 côtés opposés), jamais sur les côtés
  perpendiculaires, et les deux porteurs sont **forcément perpendiculaires**. (1) Helpers module
  `junctionAxisH(tiles,r,c,def)` (le 1er porteur est-il horizontal ? — priorité au porteur qui
  « traverse » réellement un axe = 2 côtés opposés présents, l'autre prend le perpendiculaire ; repli 1
  côté puis défaut A=horizontal/B=vertical) et `junctionDirOk(...,carrier,dr,dc)` (le porteur transmet-il
  dans cette direction ?). (2) `rebuildNetworks` (flood-fill) : depuis/vers une jonction, on ne propage
  un porteur que si `junctionDirOk` (côté sur l'axe) — port road/pipe inclus. (3) `adjacentNetworks` et
  `adjacentNetworksFootprint` : un voisin jonction ne compte pour un porteur que si le côté est sur son
  axe (bâtiments/bridging respectent la règle). (4) **Sprite** : l'orientation vient désormais de
  `junctionAxisH` (visuel = mécanique). `node --check` (7 blocs) + Chromium (croisement propre : 2
  porteurs connectés ; abus : côté perpendiculaire NON diffusé/réseau séparé ; porteurs distincts ;
  boot+ticks 0 erreur, build 199) OK. Build 198→199.
  Changement 13.17 : **fix « baisse de niveau accidentelle » à la réouverture de la fiche bâtiment.**
  Bug utilisateur : la fiche bâtiment (`InfoPanel`) s'ouvre flottante à l'endroit du tap (`info.x/info.y`)
  et, pour un bâtiment bas à l'écran, est repoussée vers le haut → le **bouton « Baisser » se retrouve
  sous le doigt**. Or « Baisser » baissait le niveau en **un seul clic** (sans confirmation, contrairement
  à « Monter » et « Démolir ») → le tap d'ouverture « traversait » sur le bouton et baissait le niveau
  (souvent juste après une amélioration). Fix : **« Baisser » passe en confirmation à 2 temps** (état
  `armedDown`, réinitialisé sur `[info.r, info.c]`, classe `.ip-down.armed`) — 1er clic = « Confirmer ? »
  (rien n'est baissé), 2e clic = baisse effective. Cohérent avec Monter/Démolir/NetworkPanel. `node
  --check` (7 blocs) + Chromium (boot 0 erreur, build 198) OK. Build 197→198.
  Changement 13.16 : **bascule « Cible = Réserve » par île (haut de l'onglet Transit).** Demande
  utilisateur. Nouveau bouton **`.pp-link-reserve`** en tête de la liste de transit du Port (au-dessus de
  l'en-tête du tableau) affichant **OUI/NON** : quand activé, la **réserve** (`seuilExport`) suit toujours
  la **cible** (`stockCible`) et **inversement** pour toutes les ressources de l'île. (1) Flag par île
  `game.tradeLinkReserve[isl]` (persisté newGame/serialize/loadSave ; rétro-compat : absent = off,
  `SAVE_VERSION` inchangé). (2) Handler `toggleTradeLinkReserve` : à l'activation, aligne `seuilExport =
  stockCible` pour toutes les ressources déjà configurées. (3) `setTradeCfg` : si le flag est actif,
  éditer `stockCible` OU `seuilExport` fixe **les deux** à la même valeur. (4) i18n en/es/de
  (« Cible = Réserve », OUI/NON, infobulle). `node --check` (7 blocs) + Chromium (toggle OUI/NON, cible
  5000→réserve 5000, réserve 250→cible 250, 0 erreur, build 197) OK. Build 196→197.
  Changement 13.15 : **fix transit bloqué par la réserve d'une île intermédiaire + revert de la pose
  jonction-sur-réseau du 13.14.** Demande utilisateur. (1) **Transit débloqué (réacheminement)** : une
  île intermédiaire dont la **réserve** (`seuilExport`) = sa **cible** (`stockCible`) ne réexportait
  JAMAIS vers l'aval → tout le transit en aval était silencieusement bloqué (ex. île 1 produit du
  charbon, île 2 cible 10000 + réserve 10000 → l'île 3 n'était jamais servie). Contre-intuitif. Nouveau
  helper `transitForwardBudget(game, src, dest, res)` : la **cible d'import EFFECTIVE** de la destination
  = sa cible propre **+** un budget de transit = somme des déficits des îles **au-delà** (chaîne linéaire
  1-2-3-4-5), **borné par le débit de la liaison aval**. `rawShippable` l'utilise → l'amont « sur-remplit »
  légèrement l'île intermédiaire, qui réexpédie cet excédent vers l'aval **sans jamais descendre sous sa
  réserve** et **sans accumuler** (borne = 1 débit aval/tick). Respecte `interdit`/blocages/liaisons
  inactives. Simulation Chromium : charbon 1→2→3 (et 1→2→3→4) ; l'île intermédiaire reste EXACTEMENT à
  sa réserve (min=max=10000) ; sans demande aval, réserve inchangée (remplit puis garde) ; `interdit`
  respecté (pas de réacheminement). (2) **Revert 13.14** : la **pose de jonction sur n'importe quel
  réseau** (non-couplé) est **annulée** (demande utilisateur : la pose sur un porteur couplé existait
  déjà et suffit, ne change pas les réseaux). `canPlace`/`tryPlaceJunction`/refund/texte d'aide
  reviennent à l'état pré-13.14. **Le sprite de réparation d'île permanent du 13.14 est CONSERVÉ.**
  `node --check` (7 blocs) + Chromium (transit OK, 0 erreur, build 196) OK. Build 195→196.
  Changement 13.14 : **sprite de réparation d'île permanent avec notification.** Demande utilisateur.
  L'ancien bouton texte « Réparer Île N » (visible seulement une fois la recherche d'accès atteinte) est
  remplacé par un **sprite seul** (`uiIcon('reparation')`, classe `.inv-repair-ico`) **affiché en
  permanence** dès qu'une île suivante existe. États : **atténué/désactivé** (`.locked`) tant que la
  recherche d'accès n'est pas atteinte ; **actif** (cliquable → `RepairModal`) ensuite ; **pastille de
  notification** (`.notif-dot` + pulse `.ready`) quand la réparation devient **possible** (ressources
  livrées au port = `canRepair`). (NB : la « pose de jonction sur n'importe quel réseau » initialement
  livrée en 13.14 a été **retirée en 13.15** à la demande de l'utilisateur.) Build 194→195.
  Changement 13.13 : **amélioration réseau = matériau AUTOMATIQUE (cheap → premium) + câble ×4.**
  Demande utilisateur. (1) **Matériau d'amélioration auto (paliers V3+)** : le bouton « Monter » du
  `NetworkPanel` ne propose plus de **sélecteur manuel** cheap/premium ; il paie **par défaut en
  « cheap »** (route → ciment, tuyau → lingot de fer, câble → câble) et **bascule automatiquement** sur
  le **« premium »** (route → béton armé, tuyau → acier) si le stock du port ne suffit pas pour le cheap.
  Le câble n'a pas de premium → reste sur son unique matériau. Implémenté dans le panneau : `effPay`
  calculé via deux appels `networkLevelChange(+1,'cheap')` / `(+1,'premium')` + test d'`affordCost` sur
  `game.port`. State `payMat`/`setPayMat` et bloc UI `.ip-fluxpri` du sélecteur **supprimés** ; le coût
  réel (matériau choisi) reste visible dans le sous-label du bouton. (2) **Capacité câble ×4** :
  `networkThroughput(level, type)` prend désormais le **type** de réseau et multiplie par
  `WIRE_CAP_MULT = 4` quand `type === 'wire'` → le câble transporte 4× plus de puissance à niveau égal
  (route/tuyau inchangés). Les 4 appelants passent le type (`net.type`/`no.type`/`netObj.type`) ; la
  composante électrique (`poolCap`) et le panneau (`wi.cap`) en héritent. `node --check` (7 blocs) +
  Chromium (ratio câble/route = 4 ; auto-fallback : ciment riche→ciment, sans ciment + béton→béton armé,
  ni l'un ni l'autre→bloqué ; 0 erreur, build 194) OK. Build 193→194.
  Changement 13.12 : **jonctions = MÉLANGE de versions de réseaux (chaque porteur garde son niveau).**
  Demande utilisateur : une jonction peut désormais relier deux réseaux de **niveaux différents** (ex.
  route V1 × câble V2) et chaque porteur s'améliore **indépendamment** (améliorer le câble n'améliore que
  le câble). (1) **Mécanique : déjà supportée au niveau données** — `coupledNetworkIds` renvoyait déjà le
  seul réseau courant (10.99), `rebuildNetworks` traverse chaque porteur séparément (un `networkId` par
  carrier dans `t.netIds`) et ne fusionne que les réseaux d'un MÊME porteur. Aucune règle de pose/upgrade
  ne forçait l'égalité des niveaux (`tryPlaceJunction` sans contrôle de niveau). (2) **Sprites refondus**
  : le pack a livré **96 sprites mixtes** `jonction_<H>_v<n>_<V>_v<m>` (6 orientations × 4 × 4) encodant le
  niveau de CHAQUE porteur. Les **24 anciens** sprites mono-version (`jonction_<H>_<V>_v<n>`) retirés de
  `__SPRITE_DATA__` (582→654 clés). (3) **Draw** (`drawBuilding`, branche jonction) : le niveau de sprite
  est calculé **par porteur** via `carLvl(car)` (= niveau de `g.networks[isl][t.netIds[car]]`, 4 si
  illimité, sinon 1..3) au lieu du `max` des deux → clé `jonction_<first>_v<carLvl(first)>_<second>_v<carLvl(second)>`.
  (4) **Câblage** : `BLD_SPRITE_OVERRIDE` (icônes menu Réseau des 3 jonctions) → `jonction_<a>_v1_<b>_v1` ;
  texte d'aide « (mêmes niveaux) » → « (niveaux indépendants) ». `node --check` (7 blocs) + Chromium (boot
  0 erreur, 96 clés jonction, clé mixte `jonction_route_v1_cable_v3` présente, build 193) OK. Build 192→193.
  Changement 13.11 : **2 sons de baisse de niveau (downgrade).** Le module SFX gagne `downgrade`
  (arpège DESCENDANT mat, bâtiment) et `downgradeNetwork` (réseau) — inlinés après `upgrade`/
  `upgradeNetwork` (47 noms au total). Branchements : `tryDowngrade` → `downgrade` ; `changeNetworkLevel`
  (dir<0) → `downgradeNetwork` (les DEUX chemins : baisse de niveau normale ET retrait du statut illimité
  « débit limité »). `node --check` (7 blocs) + smoke jsdom (47 noms, `downgrade`/`downgradeNetwork`
  présents et joués sans throw, build 192) OK. Build 191→192.
  Changement 13.10 : **sons sur TOUS les boutons d'UI (suite du 13.9).** Le 13.9 ne sonnait pas sur les
  boutons du HAUT (sauf Options), les boutons du BAS, ni à l'ouverture/fermeture des fenêtres et de la
  fiche bâtiment. Câblage au niveau des handlers App (pas dans les composants) : (1) **barre du bas** :
  `selectTool` (`selectBuilding` à la sélection / `deselect` à la désélection — couvre Copier/Démolir/
  Améliorer + palette), `onToggleBuild`/`onToggleNet` (`panelOpen`/`panelClose`). (2) **barre du haut** :
  `onOpenPort`/`onEnergy`/`onShowRepair`/`onOpenResearch`/`onSave`/`onOpenHelp`/`onOpenProduction`/
  `onOpenCalc`/`onOpenAlerts` (`panelOpen`), `onToggleInv` (`panelOpen`/`panelClose` selon l'état),
  `onResClick` (`click`). (3) **fiche bâtiment** : tap d'un bâtiment → `click` (ouverture), tap d'un
  port → `panelOpen`, tap d'une infra → `panelOpen` (ouverture du `NetworkPanel`). (4) **fermeture des
  fenêtres** : `panelClose` ajouté à l'`onClose` (bouton ×) de Production/Calculateur/Alertes/InfoPanel/
  UpgradePanel/NetworkPanel/Aide/Port/Réparation/Énergie/Recherche/BuildingDetailModal. Les `setInfo(null)`
  collatéraux (switchIsland, selectTool) ne sonnent PAS (évite le doublon — ils ont déjà leur son).
  `node --check` (7 blocs) OK. Build 190→191.
  Changement 13.9 : **intégration du système audio (SFX, synthèse procédurale Web Audio).** Le module
  `sfx_module.js` (44 sons one-shot + `placeC` en réserve) est inliné au niveau module (const `SFX`,
  juste après `VERSION_URL`, hors React, une seule instance) — **single-file, offline, zéro fichier/CDN**
  (synthèse à la volée). (1) **Déblocage mobile** : `SFX.unlock()` en tête de `onPointerDown` (canvas) +
  à l'ouverture des Options (1er geste atteignable). (2) **Branchements** : `tryPlace` (place/placeHeavy
  selon footprint ; road/cable/pipe selon le carrier infra ; `invalid` sur chaque échec verbeux),
  `tryPlaceJunction` (`junction`/`invalid`), `tryDemolish` (`demolish`), `tryUpgrade`+`upgradeAllSameType`
  (`upgrade`), `changeNetworkLevel` (`upgradeNetwork` à la montée), tech tree (`nodeReady` à la transition
  « prêt », `unlock` sur `techConfirm`, `delivery` sur `techDeliver`), révélation tuto (`buildingUnlock`),
  `applyUnlocks` (`islandUnlock`, ou `endgameUnlock` pour l'île finale 5, une fois via la garde
  `wasUnlocked`), changement d'île (`islandTransition`), Options (`panelOpen`/`panelClose`), slots
  (`save`/`slotCreate`/`slotDelete`). (3) **Alertes anti-spam** (transition d'état + `playThrottled`) :
  `checkEnergyAlerts` (nouveau, dérivé de `activeEnergyAlerts` : `powerAlert` à l'entrée en déficit,
  `normalRestored` à la résolution), `checkStockAlerts` (`stockFull`), centrale en sécurité (`fuelLow`),
  surchauffe (`powerAlert`). (4) **Persistance** : `SAVE_VERSION 14→15` (+15 à la whitelist `loadSave`),
  défauts `audioEnabled:true`/`audioVolume:0.55` (newGame), bloc `audio:{enabled,volume}` dans
  `serialize`, restauration avec guards dans `loadSave` (save < v15 → défauts). (5) **UI Options** :
  toggle « Sons » + slider « Volume » (pattern `tipsEnabled`, miroir `gameRef` + état React, son témoin
  `place` throttlé au réglage). (6) **Robustesse** : `play()` enveloppe `ensure()` dans son try/catch →
  un environnement sans Web Audio (jsdom, vieille WebView) n'interrompt JAMAIS le jeu. `node --check`
  (7 blocs) + smoke jsdom (SFX=object/45 noms, play() sans throw, GAME_BUILD 190 ; save v15 sérialise le
  bloc audio ; rétro-compat save v14 → défauts ; cycle save audio off/vol 0,3 → restauré ; 0 erreur) OK.
  Changement 13.8 : **sprite de boost rouge/bleu + bilan électrique honnête (hors batterie).** (1)
  **Sprites de boost colorés** : le pack a livré `fx_boost` (BLEU) re-livré + **`fx_boost_productivite`
  (ROUGE)**. L'overlay d'influence d'antenne dessine `fx_boost` (bleu) sur la zone VITESSE et
  `fx_boost_productivite` (rouge) sur la zone PRODUCTIVITÉ (les badges `ui_mode_*` retirés — la couleur
  du glow suffit). (2) **Bilan électrique honnête** : le HUD (pastille ⚡), le `ProductionPanel` (⚡ Net)
  et l'`EnergyPanel` (« Bilan réel ») calculaient le bilan = **`produced` − demande**, or `produced`
  inclut la **décharge batterie** → bilan trompeur (positif alors que la batterie baisse, ou inversement).
  Désormais le bilan = **`gross` (génération RÉELLE des générateurs, hors batterie) − demande** : POSITIF
  = surplus → la batterie se charge ; NÉGATIF = déficit → la batterie se décharge. La contribution
  batterie reste affichée à part dans l'`EnergyPanel` (ligne « Batterie (décharge) »). `node --check`
  (7 blocs) + Chromium (fx_boost & fx_boost_productivite décodés 32×32, 0 erreur) OK.
  Changement 13.7 : **productivité antenne = vrais intrants ÷2 + sprites de boost distincts + équilibrage.**
  (1) **Productivité = intrants ÷2** : en mode productivité, un bâtiment boosté a sa **sortie ×0,5**
  (ralenti) MAIS ses **intrants ×0,25** (ralenti + ÷2) → **moitié moins de matières par unité produite**
  (le vrai intérêt du mode). Tick : `ioMul` scindé en `inMul` (×0,25 en prod) / `outMul` (×0,5). Fiche :
  `antInMul`/`antOutMul`, ligne « Productivité » = « intrants ÷2 · sortie ×0,5 ». (2) **Sprites de boost
  distincts** : la zone d'influence affiche désormais l'effet **dans les DEUX modes** (`game.antennaProdZone`
  exposé par le tick) avec un **badge de mode** (`ui_mode_vitesse` en vitesse, `ui_mode_productivite` en
  productivité, coin haut-droit) + lueur `fx_boost`. (3) **Équilibrage** : mines V2 (fer/charbon/cuivre/
  carrière) coût **−30 %** (100/80/40 → 70/56/28) ; fours V2 fer & cuivre coût **−50 %** (cable/acier 200→100,
  ciment 100→50) ; éolienne offshore coût **−20 %** (150/100/80 → 120/80/64) ; **conso élec. plateforme
  pétrolière ×8** (4 → 32). (4) **Vérif** : l'accélération du coût d'amélioration du **puits de pétrole**
  est correcte (special-case `upgradeCostFactor` : facteur 53→159→526→1910… au-delà du niveau 5 → ramp très
  fort, inchangé). `node --check` (7 blocs) + smoke chaleur + Chromium (four_fer prod : intrants ÷4 base /
  sortie ÷2 ; speed ×2 ; 0 erreur) OK.
  Changement 13.6 : **fiche d'un bâtiment boosté = mode productivité visible.** La fiche d'un bâtiment
  dans la zone d'une antenne ne montrait l'effet QUE en mode vitesse (ligne « Boost antenne ×N »,
  intrants/sorties ×N) ; en **productivité** elle restait sur les valeurs normales (×1). Le tick repose
  désormais un drapeau d'affichage `bld.antennaProd` (= facteur de la zone prod, 1 sinon ; **mécanique
  inchangée**). L'`InfoPanel` calcule `antIoMul` (×N en vitesse, **×0,5 en productivité**, ×1 sinon) et
  `antElecFac` (= max(vitesse, prod) → boost élec ×1→×(1+fac) dans les DEUX modes) : les lignes
  **Entrées/Sortie/Réel** utilisent `antIoMul`, la ligne **« Boost antenne ×N »** devient **« Productivité
  ×0,5 (ralenti) »** en mode prod, et **Élec.** affiche « boosté ×1→×(1+fac) » aussi en prod. `node
  --check` (7 blocs) + Chromium (four_fer : aucun→8/1 · vitesse→16/2 « Boost ×2 » · prod→4/0,5
  « Productivité ×0,5 », élec boosté dans les 2 ; 0 erreur) OK.
  Changement 13.5 : **plafond plutonium (mode auto) + fiche antenne mode-aware.** (1) **Plafond
  plutonium** : `game.nuclearMix[isl].plutoCap` (0 = illimité, persisté newGame/serialize/loadSave).
  `nucMix` renvoie `plutoCap` ; `nucAutoWeights(port, plutoCap)` met le poids plutonium à **0** dès que
  le stock port ≥ plutoCap → l'équilibrage auto se reporte sur les irradiés. Champ **NumField** dans la
  section auto de la fiche centrale (`.ip-nuc-cap`, hint « stock / cap »), handler `setNucPlutoCap`
  (prop `onSetNucPlutoCap`) ; `setNucMode`/`setNucMixWeight` préservent `plutoCap`. (2) **Fiche antenne**
  : la ligne **« Effet »** était figée sur « ×2 intrants & production » même en mode productivité.
  Elle est désormais **mode-aware** (lit `game.antennaMode[isl]`) : VITESSE → « ×N intrants &
  production… » ; PRODUCTIVITÉ → « Productivité : vitesse −50 % & intrants ÷2 · émet de la chaleur… ».
  **Mécanique de l'antenne inchangée** (demande utilisateur). `node --check` (7 blocs) + Chromium (cap :
  plutonium 100 %→0 % au-delà du plafond ; fiche antenne speed≠prod ; 0 erreur) OK.
  Changement 13.4 : **sprite de la tour aéroréfrigérante mis à jour.** Le pack a re-livré
  `tour_aerorefrigerante.png` (474 o, nouvelle art). L'inlining était périmé : `bat_tour_aerorefrigerante`
  ET `tour_aerorefrigerante` portaient l'ancien art (md5 02cb…). Les 2 clés ré-inlinées avec la nouvelle
  art (md5 b8e5…) — le jeu rend `bat_tour_aerorefrigerante` (`buildingSpriteKey`). `node --check` (7 blocs)
  + boot Chromium (sprite décodé 32×32, 0 erreur) OK.
  Changement 13.3 : **antenne — retrait du liseré cyan.** L'overlay d'influence d'antenne ne dessine
  plus le `strokeRect` cyan autour des cases boostées ; seul l'effet **`fx_boost`** (alpha pulsé
  0,30→0,80) reste sur chaque case influencée. `node --check` (7 blocs) OK.
  Changement 13.2 : **câblage des sprites livrés en 13.1 (5 retours visuels).** (1) **Conduits qui
  « rentrent » dans les bâtiments** : nouvelle branche de `draw()` (avant `drawBuilding`) qui dessine un
  **stub conduit** SOUS tout bâtiment à chaleur (`heatCap`) ou tour (`tour`) — masque vers les tuiles
  conduit adjacentes, sprite `conduit_v{slvl}_{masque}` (variante `_chauffe{1,2,3}` selon le
  remplissage `conduitLoad` du réseau voisin) → le conduit ne s'arrête plus au bord, il pénètre la tuile
  (miroir des stubs route/tuyau/câble). (2) **Plus de teinte rouge canvas sur le V4** : le repli
  vectoriel cuivre ne pose plus la teinte rouge proportionnelle au flux quand `slvl > 3` (réservée à
  l'art « chauffe » des niveaux ≤ 3). (3) **Effet boost antenne** : l'overlay d'influence d'antenne
  dessine désormais le sprite **`fx_boost`** (alpha pulsé 0,30→0,80) sur chaque case influencée, en plus
  du liseré cyan. (4) **Boutons d'île = sprites** : `IslandSelector` rend `ile_N` (déverrouillée) /
  `ile_N_gris` (verrouillée) au lieu du chiffre/cadenas (repli chiffre/🔒 si sprite absent ; CSS
  `.island-tab-ico`). (5) **Sprites d'île dans les onglets** : helper `islandIcon(id)` (sprite `ile_N`,
  CSS `.island-mini-ico` 16px) ajouté aux **onglets du panneau Production** (Île 1-5) et aux **en-têtes
  de l'onglet « Transit archipel »** du Port (Île src → Île dest). `node --check` (7 blocs) + smoke
  chaleur + boot Chromium (5 onglets d'île en sprite, 0 erreur) OK.
  Changement 13.1 : **intégration des nouveaux sprites du pack (re-livré).** 157 sprites inlinés dans
  `__SPRITE_DATA__` : **`item_plutonium`** (icône plutonium, auto-câblée via `itemSpriteKey` →
  inventaire/recettes/fiches) ; **144 sprites de conduit « chauffe »** (`conduit_v{1..3}_{masque}_chauffe{1,2,3}`)
  = art de chaleur par **niveau de remplissage du tampon** : la branche conduit du `drawBuilding` choisit
  `_chauffe1` (f≥0,25), `_chauffe2` (f≥0,5), `_chauffe3` (f≥0,8) sinon le sprite de base (repli vectoriel +
  teinte canvas pour V4 sans variante) → plus de teinte rouge canvas quand le sprite existe ; + `fx_boost`,
  `ile_1..5`/`ile_N_gris` (sélecteur d'île), `tile_i3_petrole` (inlinés, dispo — câblage UI à suivre).
  `Archipel_sprites_COMPLET.zip` du repo mis à jour. `node --check` (7 blocs) + boot jsdom (sprites
  présents/décodés, 0 erreur) + smoke chaleur OK.
  Changement 13.0 : **plutonium + chaleur en MJ/GJ.** (1) **Plutonium** (nouvelle ressource t4, road,
  pas encore de sprite → repli code) : la **centrale** peut le produire comme **4e option** de
  `NUC_MAT_KEYS` (`['acier','beton_arme','cable','plutonium']`), **même ratio** que les irradiés
  (1/s à V1·100 %), mais **sans matériau de base** (helpers `nucBaseKey`=null/`nucOutKey`='plutonium' ;
  le réacteur le « breed »). Les 3 modes (single/mix/auto) et l'auto-équilibrage gèrent les 4 clés ;
  sélecteur de fiche à 4 boutons. (2) **Moteur nucléaire** : recette `combustible_u235`→**`plutonium`**
  (0,1/s). (3) **Affichage chaleur** : nouveau `fmtHeat(mj)` (MJ < 1000, **GJ** au-delà, 2 décimales fr
  comme l'électricité) appliqué au **panneau conduit** (Débit total / Stockage / Flux évacué) ; ligne
  « Transit : aucun » masquée pour le conduit. Persistance : poids `plutonium` ajouté à `nuclearMix`
  (migration : absent < 181 → 1) ; `SAVE_VERSION` inchangé (rétro-compat). **Reste (déféré)** : glitch
  esthétique des **jonctions** quand un réseau traverse un bâtiment (besoin d'un repro précis).
  `node --check` (7 blocs) + boot jsdom + smoke chaleur OK.
  Changement 12.9 : **conduit = TAMPON thermique + flux affiché + coûts réduits.** (1) **Modèle conduit
  refondu** (demande utilisateur) : un réseau conduit de **N tuiles** stocke **N×débit MJ** et a un **débit
  TOTAL** (entrée comme sortie) de **N×débit MJ/s** (V1 : 10 tuiles → 10 MJ stockés, 10 MJ/s). Plus de
  bottleneck « par tuile d'interface » / obligation de splitter. `processHeat` : `net.heatStore` (MJ
  tamponnés, conservé au rebuild via oldToNew), les sources poussent leur chaleur dans le tampon (≤ débit
  total, ≤ espace libre → `heatCool`), les tours l'évacuent (≤ débit total, ≤ stock, ≤ absorption →
  `heatAbsorb`). La **teinte** du conduit suit le **remplissage du tampon** (`store/cap`, rouge si proche
  plein). `game.conduitFlow[isl][nid]` = MJ/s réellement évacués. (2) **Panneau réseau conduit** (clic) :
  lignes **« Débit total N MJ/s »**, **« Stockage X / N MJ »**, **« 🔥 Flux évacué Y MJ/s »**. (3) **Coûts**
  : polymère du conduit **÷5** (base 500→100/tuile ; upgrade ×10/palier inchangé sur la base réduite),
  **tour ÷4** (béton 1000→250, lingot fer 500→125, avant ×8 du palier T3). (4) **Calibrage centrale** : la
  chaleur EST émise pendant la rampe sigmoïde (`heatEmit = nucCur×0,25/1000`) ; le « Bilan chaleur » de la
  fiche (12.8) la rend visible. `node --check` (7 blocs) + smoke (flux 1,536 MJ/s) + test tour OK.
  Changement 12.8 : **plafonds de chaleur relevés + DIAGNOSTICS de refroidissement dans les fiches.**
  (1) **Plafonds** : centrale `heatCap` 10→**20**, usine 6→**10** (demande utilisateur, plus de marge).
  (2) **`processHeat` trace** désormais, par bâtiment : `bld.heatCool` (MJ/s réellement évacués sur une
  source), `bld.onConduit` (la tour touche-t-elle un conduit ?) et `bld.heatAbsorb` (MJ/s qu'une tour
  évacue effectivement, au prorata sur son réseau). (3) **Fiches** : la source (centrale/usine/antenne)
  affiche une ligne **« Bilan chaleur : +X émis · −Y évacué /s »** (vert si évacué≥émis, orange sinon) →
  on voit immédiatement si le refroidissement suit ; la **tour** affiche **« Refroidissement : a/cap MJ/s
  évacués »** (ou **« ⚠ pas relié à un conduit »** en orange si `!onConduit`) + **« Eau : N% »**. Le
  mécanisme lui-même est CORRECT (vérifié jsdom : centrale + conduit + 2 tours alimentées en eau sur le
  MÊME réseau conduit → chaleur stabilisée, heatCool=1,536=émission) ; le problème de l'utilisateur venait
  d'un raccordement (tour pas sur le conduit, ou eau insuffisante) — désormais visible dans la fiche.
  `node --check` (7 blocs) + smoke chaleur + test tour OK.
  Changement 12.7 : **équilibrage usine moteur nucléaire (trop fragile).** Elle émettait 2 MJ/s pour un
  plafond de 2 MJ → trip en ~1-2 s ; pire, une tuile de conduit V1 ne porte que 1 MJ/s, donc même bien
  branchée elle ne pouvait PAS évacuer ses 2 MJ/s sans splitter. Désormais **émission 2→1 MJ/s** et
  **plafond `heatCap` 2→6 MJ** : évacuable par **UNE ligne de conduit V1 + 2 tours** (1 MJ/s shed = émission),
  et ~6 s de marge avant trip si non refroidie. (Rappel : sans **conduit** reliant l'usine aux tours, la
  chaleur n'a nulle part où aller → trip ; relier les tours à l'eau ne suffit pas.) `node --check` (7 blocs)
  + smoke chaleur OK.
  Changement 12.6 : **fix « ancien sprite de centrale pendant 1 s ».** En 12.2 seul le sprite STATIQUE
  `bat_centrale_nucleaire` (`__SPRITE_DATA__`) avait été rafraîchi, PAS la sheet d'animation
  `__ANIM_DATA__['centrale_nucleaire']` (256×64) → la centrale en marche (animée) montrait l'ancien art,
  le statique le nouveau. Re-sync des 3 sheets d'anim qui différaient du pack : `centrale_nucleaire`
  (+ `tile_i1_land_breeze`, `tile_i1_water_breeze`). Vérifié au pixel (Pillow) : **frame 0 de la nouvelle
  sheet == sprite statique** → plus de saut/flicker. `node --check` (7 blocs) + boot jsdom OK.
  Changement 12.5 : **fix icône électrique trompeuse sur la tour aéroréfrigérante.** La tour (qui ne
  consomme QUE de l'eau) affichait l'icône de déficit `etat_courant` (⚡) car `processHeat` posait
  `bld.regime`/`active` mais PAS `bld.inFac`/`bld.pwrAvg` → `drawBuilding` repliait la cause de déficit
  sur `'power'`. Fix : la tour reçoit désormais `inFac=wf` (fraction d'eau), `pwrAvg=1` (jamais limitée par
  l'élec.), `discReason = wf>0 ? null : 'input'` → l'icône affichée est « intrants/eau » (`etat_intrant`),
  plus jamais le sprite électrique. `node --check` (7 blocs) + smoke chaleur OK.
  Changement 12.4 : **fix bande vide en haut de la fiche bâtiment.** L'en-tête `.ip-head` (sticky,
  `margin-top:-11px` censé absorber le `padding-top` du panneau) ne remontait PAS sous le cadre 9-slice
  (`border:8px`+`border-image`) → bande vide d'~19 px (≈38 px en ×2) au-dessus du titre. Fix : `.info-panel`
  `padding-top:11px→0` et `.ip-head` `margin-top:-11px→0` (padding interne 11→9) → l'en-tête est collé au
  bord supérieur (juste sous le cadre), plus de bande. Diagnostiqué/validé au pixel via Chromium
  (`head.top` 19→8 px). `node --check` (7 blocs) + boot jsdom OK.
  Changement 12.3 : **3 correctifs de sprites (retours visuels).** (1) **Conduit relié aux bâtiments** :
  le masque de connexion du conduit (`drawBuilding`/branche conduit) ajoute désormais une branche vers
  tout bâtiment **à chaleur** (`heatCap` : centrale/usine/antenne) ou **tour** (`tour`) adjacent → le
  sprite `conduit_v{niv}_{masque}` pointe vers eux (fini les extrémités « ouvertes »). (2) **Plus de barre
  noire sur la centrale** : la jauge de chaleur sur la tuile ne dessine plus son fond sombre quand la
  chaleur est nulle (seuil `hf>0.005`) — elle n'apparaît qu'en présence de chaleur (signal d'alerte).
  (3) **Sprite de réseau dans le panneau d'amélioration** : l'en-tête du `NetworkPanel` (clic sur une
  route/câble/tuyau/conduit) affiche le **sprite représentatif** (`{prefixe}_v{niv}_15_NESO`) au lieu d'un
  carré de couleur (repli swatch si sprite absent). Validé : `node --check` (7 blocs) + boot jsdom 0 erreur.
  Changement 12.2 : **mise à jour de l'ensemble des sprites (pack `Archipel_sprites_COMPLET`).** Le pack
  livre enfin l'art des nouveautés 12.0. Re-sync de `__SPRITE_DATA__` (objet principal) depuis le pack :
  354 clés existantes rafraîchies/conservées + **69 ajoutées** — les **64 tuiles de conduit**
  (`conduit_v1..v4_00..15_*`, auto-tiling par bitmask), `bat_tour_aerorefrigerante` + `tour_aerorefrigerante`,
  `ui_chaleur`, `ui_mode_vitesse`, `ui_mode_productivite`. `ui_reparation`/`ui_sauvegarde` (hors pack)
  préservés. (Le bloc d'assignations `__SPRITE_DATA__["…"]=` route/tuyau/câble/côte reste intact → 531
  clés au total au runtime.) **Câblage** : (1) le **conduit** se rend désormais via le sprite
  connection-aware `conduit_v{niv}_{masque}` (repli vectoriel cuivre si absent) + teinte de chaleur
  rouge proportionnelle au flux ; (2) **tour** rendue via `bat_tour_aerorefrigerante` ; (3) icône du menu
  Réseau du conduit (`BLD_SPRITE_OVERRIDE.conduit = conduit_v1_15_NESO`) ; (4) le toggle d'antenne affiche
  les icônes `ui_mode_vitesse`/`ui_mode_productivite` ; (5) jauge/fiche de chaleur utilisent `ui_chaleur`.
  Anim du `tour_aerorefrigerante_sheet` NON intégrée (aucune entrée dans `animations_manifest.csv` → rendu
  statique, voulu). Validé : `node --check` (7 blocs) + boot jsdom (sprites présents/décodés, 0 erreur) +
  smoke chaleur + migration v13 OK.
  Changement 12.1 : **2 ajustements UI.** (1) **Bouton « Passer » pendant le calcul hors-ligne** :
  l'overlay `.catchup-overlay` reçoit un bouton `.catchup-skip` (drapeau `catchUpSkipRef`) qui interrompt
  la boucle `step()` de `runCatchUp` et entre dans le jeu immédiatement (production restante hors-ligne
  abandonnée, sans extrapolation). (2) **Bouton Alerte réduit ~20 %** (`.inv-alert-btn` .82→.66rem,
  padding 3×10→2×8, gap 6→5, icône 12px). Validé : `node --check` (7 blocs) + boot jsdom sans erreur.
  Changement 12.0 : **refonte complète du refroidissement nucléaire — la chaleur est un STOCK interne
  par bâtiment (MJ), plus un flux.** (1) **Modèle** : helper module `processHeat(game, isl)` appelé chaque
  tick (après l'énergie). Chaque source a `bld.heat` (MJ) + `bld.heatEmit` (émission/tick) ; elle monte
  quand émission > absorption, descend sinon, bornée à 0 ; au plafond → **trip**. `bld.heatEmit` calculé :
  centrale `nucCur×0,25/1000` (6144 kW → 1,536 MJ/s), usine moteur `2×regime`, antenne (prod) `0,25×MW
  consommés EN PLUS par les voisins`. Plafonds (`heatCap`) : centrale 10, usine 2, antenne 10. (2) **Centrale**
  : `NUC_POWER=6144`, **eau froide SUPPRIMÉE** (intrants = 1 comb.U235 + 1 matériau/s ; sorties = 6144 MW +
  1 matériau irradié + 1,536 MJ/s). Plus de mise en sécurité par manque d'eau (refroidissement = chaleur).
  (3) **Bâtiment `refroidisseur` SUPPRIMÉ** (BUILDINGS/TOOLBAR/TECH ; nœud 23 = enrichissement seul, nœud 24
  sans condition build refroidisseur). `eau_froide` n'est plus produite/consommée (defs laissées, inertes).
  (4) **Nouveau bâtiment `tour_aerorefrigerante`** (1×1, absorbe 0,768 MJ/s, consomme 256 eau/s via tuyau,
  coût 1000 béton armé + 500 lingot fer, upgradable ×2 absorption / ×2 eau / ×2,7 coût, nœud 24). 2 tours V1
  = 1 centrale. (5) **Nouveau réseau infra `conduit`** (carrier 'heat') : transporte la chaleur, débit
  `conduitDebit(lvl)` = 1/2/4 MJ/s/tuile (×2/palier), coût base 1000 cuivre + 500 polymère/tuile, upgrade
  ×10/palier (`networkUnitCost` cas spécial). **Ramifiable** (flood-fill), **NON traversable** (aucune
  jonction conduit ; pose interdite sur/par un autre réseau). Le débit par tuile force à **splitter** vers
  2 tours (`condTilesForNet` = nb de tuiles conduit face à la source). Rendu canvas vectoriel cuivre +
  **teinte de chaleur dynamique** (cuivre→rouge selon `game.conduitLoad[isl][nid]`). (6) **Usine moteur**
  émet 2 MJ/s (cap 2), raccordable au conduit. (7) **Antenne 3 états** (`game.antennaMode[isl]`) : VITESSE
  (défaut, ×2 I/O, conso ×3 sigmoïde, 0 chaleur), PRODUCTIVITÉ (vitesse −50 % & intrants ÷2, conso voisins
  identique, émet chaleur), SURCHAUFFE (subi : arrêt, chaleur gelée, debuff voisins persiste 5 min). Toggle
  dans la fiche (`onSetAntMode`). (8) **Trip commun** (`§7`) : au plafond → arrêt + état endommagé ≥5 min ;
  **redémarrage = payer 20 % du coût TOTAL cumulé** (`buildingTotalCost` = construction + upgrades ; calculé
  à la volée, pas de champ stocké). Handler `tryHeatRepair`. Post-effet canvas (teinte rouge clignotante,
  pas de sprite). (9) **UI** : jauge de chaleur sur la tuile (barre %, vert<50/orange<80/rouge clignotant) +
  dans la fiche (`b.heatCap`), toasts au trip et à la réparation possible (`game.heatTrip`/`heatRepairReady`),
  `NetworkPanel` conduit (débit MJ/s/tuile). (10) **Persistance** : `SAVE_VERSION 13→14`, sérialise
  `pl.h`/`pl.dmg`/`pl.dt` + `antennaMode` ; migration : refroidisseur posé = **droppé** au chargement
  (BUILDINGS sans entrée → `continue`), bâtiments existants `heat=0`. Validé : `node --check` (7 blocs) +
  smoke jsdom (boot sans erreur ; centrale + 2 tours + conduit → chaleur bornée/stabilisée ; coupure eau →
  trip à 10 MJ tick 6 ; 20 % = acier 1600… ; chaleur gelée endommagée) + migration v13-avec-refroidisseur OK.
  Changement
  11.45 : **correction taille boutons HUD : c'est PRODUCTION qui rétrécit, pas Alerte.** Le 11.44 avait
  réduit le bouton Alerte par erreur. Remis l'alerte à sa taille d'origine (.82rem) et réduit le
  **bouton Production de ~20 %** (`.inv-prod-btn` .78→.62rem, icône 15→12px, padding/gap réduits).
  Validé : `node --check` (7 blocs) + CSS équilibré. Changement
  11.44 : **5 ajustements UI/nucléaire.** (1) **Mode AUTO des matériaux irradiés** : `nuclearMix[isl]`
  passe d'un booléen `on` à un `mode` ('single'|'mix'|'auto', rétro-compat `on`→'mix'). Le tick unifie
  les 3 modes par un jeu de POIDS normalisés (`single`=1 sur matKey, `mix`=poids manuels, `auto`=
  `nucAutoWeights` = `(stockMax − stock_irr)+1` → produit davantage du matériau irradié le moins en
  stock). Handler `setNucMode` (remplace `setNucMixOn`). (2) **Sélecteur nucléaire à 3 boutons à
  suivre** : `.ip-nuc-mats` passe en 3 colonnes (mode Une seule/Mix/Auto ET matériaux Acier/Béton/
  Câble, fini le 2+1). Mode auto = affichage lecture seule (barre + %·débit). (3) **Bouton ALERTE
  ~20 % plus petit** que Production (`.inv-alert-btn` .82→.62rem, icône 12px, padding réduit). (4)
  **Croix de fermeture TOUJOURS visible** : `.ip-head` de la fiche bâtiment devient `position:sticky;
  top:0` → la × reste en haut à droite même sur une fiche longue (centrale). (5) **Pas de récap de
  rattrapage hors-ligne < 5 min** : seuil `finishCatchUp` 60→300 s. Validé : `node --check` (7 blocs)
  + CSS équilibré + Chromium (save réelle : 3 modes, auto 50/0/50 selon stocks, × visible après scroll,
  0 erreur). Changement
  11.43 : **popover ressource — le « Bilan net » inclut désormais le transit.** Dans le popover
  (clic sur une ressource de l'inventaire), `net` valait `prod − conso` (transit ignoré). Désormais
  `net = prod − conso − export + import` (export/import lus depuis `game.transitFlow`) → le bilan
  reflète la vraie variation de stock. Ex. acide : 32 − 136 − 0 + 512 = **+408/s** (au lieu de −104).
  Validé : `node --check` (7 blocs). Changement
  11.42 : **badge antenne « ×N » à la MÊME échelle que le badge niveau.** Le badge cyan ×N
  (`drawBuilding`) utilisait `tile*0.34` (police `bh*0.72`) alors que le niveau/déficit
  (`drawInfoBadges`) utilise `tile*0.16` → ×N visiblement plus gros. Le badge ×N reprend désormais
  EXACTEMENT la même formule (police `tile*0.16*bsc` "DM Mono", pad `*0.038`, segH `fs+pad*2`, rayon
  `*0.045`, ancrage haut-gauche, seuil `tile>=16`) → ×N et niveau identiques. Validé : `node --check`
  (7 blocs) + Chromium (0 erreur). Changement
  11.41 : **centrale diesel — intrant diesel 4/s → 3/s.** `centrale_diesel` (diesel → energie_kw 512) :
  `diesel` passe de **4 à 3** (sortie élec. inchangée). Validé : `node --check` (7 blocs). Changement
  11.40 : **centrale nucléaire — puissance 4 MW → 6 MW.** `NUC_POWER` passe de **4096 à 6144 kW**
  (V1 niveau 1 / 100 %, ×2^upgrade ensuite) aux 2 endroits (tick + fiche). Intrants/sorties à
  l'échelle inchangés. Validé : `node --check` (7 blocs). Changement
  11.39 : **broyeur d'uranium — intrant acide ÷4.** `broyeur_uranium` (uranium 128 + acide → yellow_cake 1) :
  `acide` passe de **16 → 4** (uranium et sortie inchangés). Validé : `node --check` (7 blocs). Changement
  11.38 : **centrale nucléaire — répartition des matériaux irradiés (mode « une seule » ou « mix »).**
  Nouveau réglage par île `game.nuclearMix[isl] = {on, acier, beton_arme, cable}` (poids 0–100,
  persisté newGame/serialize/loadSave ; helper module `nucMix(game,isl)` + const `NUC_MAT_KEYS`).
  **Tick** (bloc nucléaire de `tickIsland`) : si `mix.on`, la quantité irradiable `irrAmt` est
  **répartie entre acier/béton armé/câble au prorata des poids normalisés** (chaque part produite
  seulement si payable au port) ; sinon mode « une seule » inchangé (`nuclearConfig`). **Fiche
  centrale** (`InfoPanel`) : toggle **« Une seule » / « Mix réparti »** ; en mix, **3 sliders** (un
  par matériau) avec **% normalisé + débit /s** (`ip-nuc-mix*`). Handlers `setNucMixOn` (init parts
  ≈⅓ à la 1re activation) / `setNucMixWeight` ; props `onSetNucMixOn`/`onSetNucMixWeight`. Validé :
  `node --check` (7 blocs) + CSS équilibré + Chromium (save réelle : split 20/60/20 exact, fiche =
  toggle + 3 sliders « 33% · 0,53/s », 0 erreur). Changement
  11.37 : **5 sprites UI manquants intégrés + emojis remplacés.** Le pack `Archipel_sprites_COMPLET.zip`
  (re-livré par l'utilisateur) contenait enfin `ui_production`, `ui_energie`, `ui_alerte`,
  `ui_calculateur`, `ui_astuce` (16×16). Les 5 inlinés dans `__SPRITE_DATA__` (avant `ui_configurer`).
  Câblage : **📊 Production** (déjà `uiIcon('production')`, sprite désormais présent), **⚡ pastille
  énergie HUD** (`uiIcon('energie')`), **⚠ bouton alerte HUD** (`uiIcon('alerte')`), **🧮 bouton +
  titre Calculateur** (`uiIcon('calculateur')`), **💡 « Revoir les astuces »** (`uiIcon('astuce')`).
  Le `Archipel_sprites_COMPLET.zip` du repo est mis à jour. Restent en emoji (texte) : ⚡/⚠ en
  préfixe de chaîne (recettes/fiches/bannières), toasts, astuces/i18n, typo. Validé : `node --check`
  (7 blocs) + CSS équilibré + Chromium (5 sprites décodés 16×16, pastille énergie en sprite, 0 erreur).
  Changement
  11.36 : **slider « Taille des badges » (3 d'un coup) + 2 emojis UI → sprites + audit emoji.** (1)
  **Slider** : option `ui.badgeScale` (0,5→2, défaut 1, persistée comme les autres uiPrefs) dans
  l'`OptionsModal` (helper `sliderRow`) → multiplie d'un coup la taille des 3 badges carte : **%
  déficit, niveau** (`drawInfoBadges`, ×sc sur fs/pad/gap/rayon) et **boost antenne ×N** (`drawBuilding`,
  ×sc sur bh). Handler `setBadgeScaleVal` (clampé 0,5–2). (2) **Emojis→sprites câblés** (sprite
  existant) : bouton **Options** `⚙`→`uiIcon('configurer')`, bouton **Aide** `?`→`uiIcon('info')`.
  (3) **Audit emoji** : le reste des emojis UI sans sprite correspondant (📊 Production, ⚡ énergie,
  ⚠ alerte, 🧮 calculateur, 💡 astuces) → **sprites `ui_*` à créer** ; les emojis dans les toasts/
  astuces/i18n et la typo (→ ← ⬆ ⬇ ✓) restent en emoji (texte). Validé : `node --check` (7 blocs) +
  CSS équilibré + Chromium (slider présent, ⚙ en sprite, 0 erreur). Changement
  11.35 : **freeze « Copier » VRAIE cause trouvée (boucle de rendu qui meurt) + boost antenne visible
  dans la fiche + badges carte agrandis.** (1) **FIX FREEZE (cause racine)** : la fonction `frame`
  (boucle rAF) n'avait PAS de try/catch → une exception dans `draw()` (ou le tick) empêchait
  `requestAnimationFrame(frame)` d'être atteint → **la boucle de rendu MOURAIT** ; seule la **vue de
  l'île** gelait (le reste du thread/CSS continuait) jusqu'à ce que le « beat » la relance après
  2500 ms + 2000 ms ≈ **~4 s** (symptôme exact rapporté : « seule la vue de l'île freeze »). `frame`
  est désormais enveloppé `try { … } catch(log) { } finally { reprogramme TOUJOURS la frame }` → la
  boucle survit à toute exception (la vue saute 1 frame au lieu de geler 4 s). Garde « beat » abaissé
  (seuil 2500→1200 ms, vérif 2000→600 ms) pour relancer vite les arrêts hors-exception (perte de
  contexte GPU…). Reproduction : la save de l'utilisateur (43 Ko, 550 bât.) + 645 bâtiments synthé.
  → copie = ~27 ms en Chromium desktop (donc throw env.-spécifique WebView ; le try/catch corrige le
  symptôme quoi qu'il arrive). (2) **Boost antenne dans la fiche** : l'`InfoPanel` multipliait Sortie/
  Entrées par `upgradeMult` seul → une mine boostée affichait la MÊME production (256/s) qu'une non
  boostée. Ajout `antBoost = bld.antennaBuff>1 ? … : 1` → Entrées/Sortie/Réel ×antBoost, ligne **Élec.**
  « boosté ×1→×(1+N) », + **ligne « Boost antenne ×N »** (cyan). (3) **Badges carte** (% déficit +
  niveau) : taille intermédiaire (`tile*0.16`, entre l'origine 0.22 et le trop-petit 0.11 de 11.33).
  Validé : `node --check` (7 blocs) + CSS équilibré + Chromium (save réelle : 59 fps, splash OK,
  0 erreur). Changement
  11.34 : **3 correctifs UX : hors-ligne lent par défaut + splash de chargement + « Copier » ne
  sauvegarde plus inutilement.** (1) **Calcul hors-ligne LENT par défaut** : `simplifyOffline`
  bascule par défaut à `false` (sémantique `=== true` partout : newGame, useState, serialize en
  `!!`, loadSave, `runCatchUp`, sync UI). Le rattrapage > 1 h fait désormais la **simulation
  complète** (avec barre de progression) au lieu de l'extrapolation rapide ; le mode rapide reste
  disponible via l'option « Calcul hors-ligne simplifié ». (2) **Splash de chargement** : `<div
  id="splash">` HTML STATIQUE (logo 🏭 + titre + spinner + « Chargement… ») affiché **immédiatement**
  (avant React/JS) → fini l'écran noir « est-ce que ça a planté ? » pendant le décodage + le
  rattrapage. CSS `#splash` (z-index 250, sous le catchup-overlay 300) ; masqué au **1er `draw()`**
  réussi (`window.__splashGone`, classe `.hide` + retrait) ; filet de sécurité 12 s dans un `<script>`
  du body (7 blocs script désormais). (3) **« Copier » ne gèle plus** : `onPointerUp` ne planifie une
  sauvegarde QUE pour une action modifiant l'état (pose/amélioration/démolition, captées via
  `wasMode`) ; une simple sélection (Copier / inspection) ne re-sérialise plus les 5 îles +
  localStorage pour rien (cause la plus probable du gel de plusieurs secondes au Copier sur une
  grosse partie ; non reproductible en environnement headless — à confirmer côté appareil). Validé :
  `node --check` (7 blocs) + CSS équilibré + Chromium (splash présent puis retiré au 1er rendu,
  0 erreur). Changement
  11.33 : **badges carte réduits (% déficit + niveau, ~−75 % surface).** Dans `drawInfoBadges`
  (pastilles bas-gauche d'une case), les multiplicateurs de taille sont ~divisés par 2 (font
  `tile*0.22`→`*0.11`, pad `*0.05`→`*0.025`, gap `*0.06`→`*0.03`, rayon `*0.06`→`*0.03`, plancher
  6→5 px) → pastilles **% d'efficacité (déficit)** et **numéro de niveau** ~2× plus petites
  (≈ −75 % de surface). L'icône de panne (haut-droite) inchangée. Validé : `node --check` (6 blocs)
  + CSS équilibré + Chromium (0 erreur). Changement
  11.32 : **antenne — intrants AUSSI boostés + conso élec. sigmoïde ×1→×3.** Ajustement du 11.31 :
  un bâtiment boosté a désormais ses **intrants ×facteur ET ses sorties ×facteur** (il « tourne plus
  vite » au lieu de produire gratuitement), et sa **conso électrique OSCILLE en sigmoïde** jusqu'à
  ×(1+facteur) du nominal — soit **×1→×3 au niveau de base** (réintroduction de `bld.antennaSigT` /
  période 60). `ioMul = mult × buffFac` appliqué aux entrées comme aux sorties. Fiche « Effet » MAJ
  (`×N intrants & production · conso ×1→×(1+N) sigmoïde`) + tip. Validé : `node --check` (6 blocs) +
  CSS équilibré + Chromium (0 erreur). Changement
  11.31 : **antenne refondue : boost effectif, améliorable, conso 1024 kW, retours visuels.** (1)
  **Boost effectif** : le pré-pass d'antenne (`tickIsland`) stocke un FACTEUR par tuile influencée
  (`buffSet[r-c] = 2^(upgrade+1)`, mémorisé dans `game.antennaBuff[isl]`) au lieu d'un booléen. Un
  bâtiment de production (`b.outputs && !antenna && kind build`) sur une tuile influencée a ses
  **sorties (matières + élec.) ×facteur** et sa **conso élec. ×facteur** ; intrants matières
  inchangés (production « offerte », payée en courant). L'ancienne conso oscillante ×1→×3
  (`antennaSigT`/`nominalPower`) est supprimée. (2) **Antenne améliorable** : `isUpgradable` n'exclut
  plus `antenna` → ×2 boost + ×2 conso par niveau, **coût ×2,7/niveau** (UPGRADE_SCALE standard). (3)
  **Conso de base 512 → 1024 kW**. (4) **Retours visuels** : pulsation cyan (`#26C6DA`) sur les 8 cases
  influencées (overlay dans `draw()`, lit `game.antennaBuff[isl]`, clignote via `performance.now()`,
  force `_animPlayed`), + **badge « ×N »** cyan (coin haut-gauche) sur chaque bâtiment boosté
  (`t.building.antennaBuff > 1`). (5) Fiche bâtiment : ligne « Effet » dynamique (`×2^(upg+1)
  production… · conso ×…`) + ligne « Boost » dans l'aperçu d'amélioration ; astuce + tip MAJ. Validé :
  `node --check` (6 blocs) + CSS équilibré + Chromium (`isUpgradable('antenne')=true`, coût ×2,7,
  boost 2→4, 0 erreur). Changement
  11.30 : **transit — switch destination à position fixe + boutons réduits.** Onglet « Transit île »
  du Port : les boutons de priorité de destination ne changent plus de place (tri croissant fixe : île
  N-1 à GAUCHE, N+1 à DROITE) ; seule la case prioritaire est remplie en orange (défaut N+1). Boutons
  réduits (font .68rem, padding 2×9, `white-space:nowrap`). Validé : `node --check` (6 blocs) + CSS
  équilibré + Chromium (0 erreur). Changement
  11.29 : **les couleurs transit du build 152 (11.27) annulent et remplacent celles du build 150
  (11.25).** Retrait du **coloriage du build 150** : le **soulignement orange/bleu des ressources de
  l'inventaire** (`.inv-export`/`.inv-import` + prop `transitDir` du HUD) et la **teinte du nom de
  ressource dans le Port** (`.pp-res-name.pp-export`/`.pp-import`). Conservé : le coloriage du build
  152 — flux des **Liaisons** (`.pp-cargo-out` orange / `.pp-cargo-in` bleu) et lignes **Export/Import**
  du popover ressource. `islandTransitDir` devient inutilisé (laissé). Validé : `node --check` (6 blocs)
  + CSS équilibré + Chromium (0 erreur). Changement
  11.28 : **priorité de destination — défaut « île N+1 », switch agrandi.** (1) **Défaut N+1** : la
  passe normale de `tickShips` sert désormais les destinations de chaque source dans l'ordre
  DÉCROISSANT (île voisine la plus haute = N+1 d'abord) au lieu de l'ordre des SHIP_LINKS ; idem
  `transitDestOrder` (tri `b-a`). Donc par défaut une ressource part vers N+1 avant N-1 ; la priorité
  explicite par ressource (pré-pass) override toujours. (2) **UI switch** : les puces `Île N` sont
  regroupées dans un **switch segmenté** `.pp-dest-sw` (boutons accolés, l'île prioritaire remplie en
  orange), **agrandies** (font .82rem, padding 5×14) ; label « Envoyer d'abord : ». Validé :
  `node --check` (6 blocs) + CSS équilibré + Chromium (défaut→île 5 d'abord, override [3,5]→île 3,
  0 erreur). Changement
  11.27 : **couleurs export/import dans les Liaisons du Port + lignes Export/Import dans le popover
  ressource.** (1) **Liaisons** (onglet Transit) : les flux sortants `pp-cargo-out` sont **orange**
  (export), les entrants `pp-cargo-in` passent de vert à **bleu** (import) — mêmes couleurs que
  l'inventaire/Port. (2) **Popover ressource** (clic sur une ressource de l'inventaire) : deux lignes
  ajoutées sous Production/Consommation → **« Export → X/s »** (orange) et **« Import ← Y/s »** (bleu),
  calculées depuis `game.transitFlow` pour l'île courante (`fmtRateSci`). Bilan net inchangé. CSS
  `.res-pop-row.export/.import`. Validé : `node --check` (6 blocs) + CSS équilibré + Chromium (popover =
  Production/Conso/Export/Import/Bilan, 0 erreur). Changement
  11.26 : **priorité de destination du transit par ressource + sprite ressource dans le Port.** (1)
  **Priorité de destination** : nouvelle structure `game.transitDestPriority[src][res] = [dest,…]`
  (persistée newGame/serialize/loadSave). `tickShips` réécrit : remet `transitFlow` à zéro une fois,
  puis **pré-pass** qui expédie chaque ressource ayant un ordre explicite vers les îles dans cet ordre
  (consomme surplus + budget `used` par sens), puis **passe normale** `transferLink(…, used)` (budget
  réduit, flux accumulé). Défaut (aucun ordre) = comportement inchangé. Helpers `transitNeighbors`,
  `transitDestOrder`, `setTransitDestFirst`. UI Port (« Transit île ») : sous chaque ressource, quand
  l'île a ≥2 voisins, des **puces `Î<n>`** (`.pp-dest-chip`, 1re = orange) — toucher une île la met en
  tête (handler App `setDestPriority`). Ex. processeur → île 5 puis île 3. (2) **Sprite ressource**
  ajouté devant le nom dans chaque ligne du Port (`.pp-res-head`/`.pp-res-ico`). Validé : `node --check`
  (6 blocs) + CSS équilibré + Chromium (surplus rare : défaut→île 3 d'abord, priorité [5,3]→île 5
  d'abord ; Port affiche 5 sprites, 0 erreur). Changement
  11.25 : **câble : débit/flux en kW·MW·GW + couleurs transit (export/import) inventaire & Port.** (1)
  Dans le `NetworkPanel` d'un **câble**, « Débit max » et « Flux demandé » sont de la PUISSANCE → passent
  de `fmtPort(x)+' /s'` à **`fmtPower(x)`** (kW/MW/GW) quand `isWire` (route/tuyau gardent `… /s`). (2)
  Nouveau helper `islandTransitDir(game, isl)` (net `transitFlow` → `'export'`/`'import'` par ressource).
  L'**inventaire** (HUD, prop `transitDir`) souligne chaque ressource en **orange si exportée**, **bleu
  si importée** (`.inv-export`/`.inv-import`, `box-shadow inset`). Le **Port** colore le nom de ressource
  des mêmes couleurs (`.pp-res-name.pp-export`/`.pp-import`). Validé : `node --check` (6 blocs) + CSS
  équilibré + Chromium (`fmtPower(65536)=65,54 MW`, dir import/export OK, 0 erreur). Changement
  11.24 : **alerte « déficit électrique + 0% batterie » + vérif tech tree pompe/puits.** (1) Nouvelle
  fonction `activeEnergyAlerts(game)` : une île DÉBLOQUÉE en **déficit** (`demand > produced+0.5`) ET
  **batterie vide** (`accStored <= 0` : 0% ou aucun accumulateur) génère une alerte. Comptée dans
  `alertCount` du HUD (bouton ⚠ à côté de Production) et listée dans `AlertsPanel` (ligne rouge
  `.alert-energy` « ⚡ Déficit · 0% batterie », clic → va à l'île + ouvre le panneau Énergie via
  `onGoEnergy`). Le panneau s'intitule désormais « ⚠️ Alertes » (élec. + stock). (2) **Vérif tech
  tree** : `puits_petrole` est débloqué par le **nœud 8 « Accès Île 3 »**, `pompe_eau` par le **nœud 9
  « Usine Polymère »** (prérequis nœud 8) → bien au stade **île 3, pas avant** (les Excel étaient
  périmés ; le fix tuyau 11.23 reste cohérent). Validé : `node --check` (6 blocs) + CSS équilibré +
  Chromium (alerte déclenchée si déficit+0%, pas si batterie chargée ni surplus, 0 erreur). Changement
  11.23 : **tuyaux disponibles sur les îles 1 et 2 (fix déblocage).** `networkUnlocked` gatait le
  tuyau par `currentIsland >= 3` → impossible de poser un tuyau sur les îles 1-2 même après avoir
  débloqué un bâtiment à liquide (incohérent avec la pompe à eau « dispo partout » depuis 10.79).
  Désormais `pipeOk = has('pompe_eau') || has('puits_petrole')` → le réseau tuyau (et les jonctions
  route/tuyau, câble/tuyau) apparaît dès qu'un bâtiment à liquide est débloqué par la recherche, sur
  N'IMPORTE quelle île. Validé : `node --check` (6 blocs) + Chromium (pipe i1 sans déblocage=false,
  avec pompe_eau=true, puits_petrole i2=true, 0 erreur). Changement
  11.22 : **notation scientifique étendue aux coûts & recettes de la fiche bâtiment.** Les gros
  nombres restaient en notation normale (ex. coût d'amélioration « Pièce méca 669 463 »). Désormais
  scientifique (≥1e5) : (1) **coût d'amélioration** (`InfoPanel` `ip-cost-chips`, `fmtInt`→`fmtPort`) ;
  (2) **coût de construction** du menu Bâtiment (`ToolButton` `tb-cost`, `fmtInt`→`fmtPort`) ; (3) **coût
  de réparation** (`RepairModal`, `fmtInt`→`fmtPort` sur stock & coût) ; (4) **recettes** Entrées/Sortie :
  `recipeChips` (fiche, tap) + `formatRecipe` (aperçu d'amélioration) + `resChips` (détail appui long)
  passent `fmtRate`→`fmtRateSci` (et coût `resChips` `fmtInt`→`fmtPort`). `fmtPower` (élec.) inchangé.
  Validé : `node --check` (6 blocs) + Chromium (669463→6,69e5, 512→512, 0 erreur). Changement
  11.21 : **catégorie « Ciment & béton » + fours V2 rééquilibrés.** (1) **Menu Bâtiment** : nouvelle
  catégorie `cement` « Ciment & béton » (ids `cimenterie`+`betonniere`) insérée **au-dessus** de
  `steel` « Fer-acier » dans `TOOLBAR_GROUPS` ; ces 2 bâtiments sont retirés de Fer-acier. Label i18n
  ajouté (en/es/de). (2) **Fours V2** (`four_fer_v2`, `four_cuivre_v2`) : **intrants & extrants ×8**
  (minerai 4→32, lingot 1→8) mais **consommation électrique ×2** (power 16→32). Validé : `node --check`
  (6 blocs) + Chromium (ordre cement→steel, recettes ×8/power ×2, 0 erreur). Changement
  11.20 : **« V1 » solo retiré des noms + calculateur réorganisé (ressources d'abord, bâtiments
  optionnels avec amélioration).** (1) **Noms** : passe `stripSoloV1()` après `I18N.applyToData` →
  retire « V1 » du nom des bâtiments SANS déclinaison (ex. « Aciérie V1 »→« Aciérie », « Pompe Eau »,
  « Usine Moteur Nucléaire ») ; les familles avec versions (mine_fer/_v2/_v3, four_fer/_v2…) gardent
  « V1 » pour les distinguer. Cross-langue (le jeton V1 est conservé tel quel par l'i18n).
  (2) **Calculateur** : `computeProductionChain` renvoie aussi `resourceRates` (débit /s de chaque
  ressource de la chaîne, hors item cible). Le panneau affiche par DÉFAUT **« Ressources nécessaires
  /s »** (+ ⚡ conso) ; les **bâtiments** sont masqués derrière un bouton **« ▸/▾ Bâtiments
  nécessaires »** (state `showBlds`). Quand ils sont affichés, un sélecteur **« Avec amélioration −
  Nv. X + »** (state `upg`, 0→12) divise les comptes par `2^upg` (et suffixe « Nv.X » au nom). CSS
  `.calc-toggle`/`.calc-upg*`. Validé : `node --check` (6 blocs) + CSS équilibré + Chromium (noms OK,
  ressources par défaut, toggle bâtiments, Nv.1 ÷2, 0 erreur). Changement
  11.19 : **calculateur — items limités aux ressources débloquées.** `CalculatorPanel` reçoit `game`
  et filtre `itemsList` via `unlockedResourceSet(game)` (ressources produites par un bâtiment débloqué
  par la recherche) → la grille « Produire » ne montre QUE les ressources débloquées (cohérent avec
  inventaire/port). Ex. début de partie = 5 items au lieu des 27 produisibles. Prop `game` passée au
  rendu. Validé : `node --check` (6 blocs) + Chromium (5 items en début de partie, 0 erreur). Changement
  11.18 : **calculateur de production intégré.** Nouveau bouton **« 🧮 Calculateur »** sous l'inventaire
  ouvert (HUD) → ouvre `CalculatorPanel`. L'utilisateur choisit un **item** (grille de sprites, 27 items
  produisibles) + un **débit cible /s** (`NumField`) ; le jeu déroule **toute la chaîne** : helper
  module `computeProductionChain(item, rate)` (récursif, profondeur max 80) via `PRODUCER_OF` (producteur
  canonique par ressource = bâtiment de BASE préféré, `energie_kw` exclue). Affiche : **bâtiments
  nécessaires** (charge réelle `×N` + nombre à poser `→ ceil`, triés par tier via `calcBtierRank`),
  **ressources de base à fournir** (feuilles sans producteur, rare), et **consommation électrique
  totale** (`calcDefPower` : sigmoïde base+amp / aléatoire max / power, sommée × count → `fmtPower`).
  State App `calcOpen`, prop `onOpenCalc`, rendu près de `ProductionPanel`. CSS `.inv-calc-btn` +
  `.calc-*`. Lecture pure (n'affecte pas la partie). Libellés en `I18N.t` (repli fr hors-fr). Validé :
  `node --check` (6 blocs) + CSS équilibré + Chromium (chaîne 4 acier/s = 4 acierie/32 four_fer/256
  mine_fer/40 mine_charbon, 512 kW ; UI OK, 0 erreur). Changement
  11.17 : **centrale d'enrichissement U235 — temps de fabrication ×4 (4× plus lente).** Comme pour
  l'usine moteur nucléaire (11.14), `centrale_enrichissement` : **intrants ET sortants ÷4**
  (yellow_cake 8→2, acier 1→0,25 ; combustible_u235 1→0,25) → fabrication 4× plus lente. **Conso
  électrique INCHANGÉE** (`power: 0` + `sigmoid {base:64, amp:192, period:60}` conservés). Coût de
  construction inchangé. Validé : `node --check` (6 blocs) + Chromium (recette ÷4, sigmoid intact,
  0 erreur). Changement
  11.16 : **panneau Port — mention de l'île reliée par le transit.** Dans la section « Amélioration du
  transit » (onglet « Transit île »), une ligne **« ↔ Transit avec l'île N »** (ou « les îles N, M »
  si plusieurs liaisons, ex. île 3 ↔ 2 et 4) s'affiche sous le titre quand ≥1 liaison est active
  (`links` déjà calculé : SHIP_LINKS impliquant l'île courante + `linkActive`). Lève la confusion
  « pourquoi puis-je améliorer le port 5 ? » → il transite avec l'île 4 (chaîne 1-2-3-4-5, pas d'île 6).
  CSS `.pp-port-linked` (atténué). Affichage seul, aucune logique touchée. Validé : `node --check`
  (6 blocs) + CSS équilibré + Chromium (0 erreur). Changement
  11.15 : **coût d'amélioration réseau ×4/niveau + élément moteur nuc. en T4.** (1) **Réseaux
  (route/câble/tuyau) plus chers** : la montée des paliers élevés (niveau 3+, `networkUnitCost`)
  passe de `×2` à **`×4` par niveau** (`base * Math.pow(4, level-3)`) → ex. route cheap L3→4 = 800,
  L4→5 = 3200, L5→6 = 12800 (au lieu de 800/1600/3200). Niveaux 1-2 (tables fixes) inchangés. (2)
  **`element_moteur_nuc`** passe `RES_TIER` t3 → **t4** (affiché sous le séparateur T4 de l'inventaire,
  avec les matériaux irradiés). Validé : `node --check` (6 blocs) + Chromium (tier t4, coûts ×4, 0 erreur).
  NB : la **pose/amélioration de port** est déjà bloquée sans liaison active (`hasLink`/`links.length`) ;
  le port de l'île 5 transite avec l'île 4 (chaîne 1-2-3-4-5, pas d'île 6) → son amélioration est
  légitime. Le **bouton alerte** (HUD, à droite de Production) n'apparaît que s'il y a ≥1 alerte active
  (seuil « ⚠️ Alerte si stock < » réglé dans le Port + stock sous le seuil). Changement
  11.14 : **usine moteur nucléaire — durée de production ×10 (correction du 11.13, sens inversé).**
  L'utilisateur voulait l'inverse du 11.13 : production **10× plus LENTE**. `usine_moteur_nuc` :
  **intrants ET sortants ÷10 par rapport à l'original** (combustible_u235 1→0,1, piece_meca 50→5,
  processeur 10→1, polymere 50→5 ; element_moteur_nuc 1→0,1). **Conso électrique INCHANGÉE**
  (`power: 0` + `randomP {min:64, max:512}`). Coût de construction inchangé. Validé : `node --check`
  (6 blocs) + Chromium (recette ÷10, randomP intact, 0 erreur). Changement
  11.13 : **usine moteur nucléaire — durée de production ÷10.** `usine_moteur_nuc` : **intrants ET
  sortants ×10** (combustible_u235 1→10, piece_meca 50→500, processeur 10→100, polymere 50→500 ;
  element_moteur_nuc 1→10) → elle produit 10× plus vite (durée ÷10). **Conso électrique INCHANGÉE**
  (`power: 0` + `randomP {min:64, max:512}` conservés) comme demandé. Coût de construction inchangé.
  Validé : `node --check` (6 blocs) + Chromium (recette ×10, randomP intact, 0 erreur). Changement
  11.12 : **puissance affichée en kW / MW / GW selon l'ampleur.** Nouveaux helpers `fmtPower(kw)`
  (kW < 1000, MW < 1e6, GW au-delà ; signe conservé, mantisse `fmtSig` = entier si rond sinon 2
  décimales, virgule fr) et `fmtEnergy(kwh)` / `fmtEnergyPair(charge, cap)` (kWh/MWh/GWh ; la paire
  partage l'unité de la capacité → « 0,51 / 20,48 MWh »). Appliqués à TOUS les affichages d'électricité :
  **HUD** (pastille ⚡), **ProductionPanel** (⚡ Prod/Conso/Net), **EnergyPanel** (prod/batterie/demande/
  non servie/bilan + accumulateurs), **NetworkPanel** câble (demande min→max, Production, Livrée, réserve
  accu), **InfoPanel** (conso/prod élec., centrale nucléaire ⚡, ligne conso « prévu min→max », stockage
  batterie, aperçu d'amélioration), **BuildingDetailModal** (conso/prod élec.). Les `… kW` / `… kWh`
  codés en dur sont retirés (l'unité vient désormais du helper). Affichage seul, aucune mécanique
  touchée (les valeurs internes restent en kW). Validé : `node --check` (6 blocs) + rendu Chromium
  (0 erreur ; `fmtPower(1024)`=`1,02 MW`, `fmtPower(4e6)`=`4 GW`, `fmtPower(512)`=`512 kW`). Changement
  11.11 : **notation scientifique dans les panneaux Production et Réseaux.** Nouveau helper
  `fmtRateSci(v)` (= notation scientifique `1,5e5`/`2,43e6` dès 1e5, décimales fines en dessous, via
  `fmtInt(v, 1e5)`). (1) **ProductionPanel** : bilan énergie (`fmtInt`→`fmtPort` pour ⚡ Prod/Conso/Net
  kW) ; colonnes Prod/Conso/Net /s et débits Transit (`fmtRate`→`fmtRateSci`). (2) **NetworkPanel**
  (clic sur route/câble/tuyau) : Débit max, Flux demandé, Demande min→max, Production kW, Réserve
  accumulateur (charge/cap), Livrée, Tuiles (`fmtInt`→`fmtPort`) ; lignes Production /s & Consommation /s
  (`fmtFlow` : `fmtRate`→`fmtRateSci`). La citerne tuyau (`fmtPool`) utilisait déjà `fmtPort`. Aucune
  logique de jeu touchée (affichage seul ; le `fmtInt(cap)` de la fiche BATTERIE, ligne ~8220, non
  concerné). Validé : `node --check` (6 blocs) + rendu Chromium (0 erreur ; `fmtRateSci(150000)`=`1,5e5`,
  `fmtRateSci(123.45)`=`123.45`). Changement
  11.10 : **sprites de jonction RÉELLEMENT mis à jour (les 24 ré-inlinés).** En 11.08 j'avais comparé
  les sprites jonction à une **copie périmée** du pack restée dans le working tree → conclusion erronée
  « déjà à jour ». Le `Archipel_sprites_COMPLET.zip` sur `main` (617292 o) contenait en fait une
  **nouvelle version** des 24 PNG `jonction_<H>_<V>_v1..v4`. Comparaison **exhaustive** des 350 sprites
  statiques inlinés vs le zip du repo : **24 STALE (toutes les jonctions)**, le reste conforme. Les 24
  ont été **ré-inlinés byte-à-byte** depuis le pack courant (MATCH 348/350 ; les 2 restants
  `ui_reparation`/`ui_sauvegarde` viennent d'une autre source, hors pack, intacts). Convention
  d'orientation re-vérifiée sur le nouvel art (premier token = porteur horizontal) → logique de draw
  inchangée et correcte. Validé : `node --check` (6 blocs) + rendu Chromium (0 erreur). Changement
  11.09 : **export/transit du DIESEL possible.** Le diesel était **exclu du transit inter-îles** :
  `TRADE_RESOURCES` ne retient que les ressources portées par la **route** OU listées dans
  `TRADE_LIQUIDS` (`['petrole','acide']`). Or `CARRIER_BY_RES.diesel === 'pipe'` (depuis 10.34) et le
  diesel n'était pas dans `TRADE_LIQUIDS` → jamais expédiable, alors même qu'il est **stocké au port**
  (`isPortPipe`, 10.38). Fix : **ajout de `'diesel'` à `TRADE_LIQUIDS`** → il entre dans
  `TRADE_RESOURCES` et le transit/commerce (qui lit/écrit directement le port pour toute ressource)
  le gère sans autre changement. De plus, `tradePriorityFor` **réconcilie** désormais la liste de
  priorité en mémoire (ajoute les ressources transitables apparues après sa création) pour que le
  diesel soit aussi transité en mode Priorité dans les **parties déjà en cours** (le `loadSave`
  réconciliait déjà au chargement, l.10813-10814). Diesel apparaît donc dans la config Transit du Port
  (seuil/cible/interdit) et les flux. Validé : `node --check` (6 blocs) + rendu Chromium (diesel ∈
  TRADE_RESOURCES, isPortPipe, 0 erreur). Changement
  11.08 : **pose de jonction LIBRE (fin des refus géométriques) + vérif sprites jonction.** (1) **Bug :
  « impossible de poser une jonction ici ».** `tryPlaceJunction` refusait la pose sur une tuile vide qui
  ne **touchait aucun réseau infra adjacent** (toast « ❌ Doit toucher un réseau ») ou quand le porteur
  croisé ne pouvait pas être auto-posé à côté (toast « ❌ Pas de place pour le réseau croisé à côté »).
  Or une jonction porte les DEUX réseaux et se relie dès qu'un tracé la touche (rebuildNetworks). Désormais
  la pose est **libre** (cohérent avec la pose sans route des bâtiments, 10.34) : ces 2 refus sont retirés ;
  l'auto-pose du porteur manquant ne se fait QUE si **exactement un** des deux porteurs est présent et reste
  **best-effort** (pas de place à côté → la jonction est posée seule, le croisement se complète quand le
  joueur étend l'autre réseau). Restent bloquants : tuile occupée, croisement d'une infra non-couplée,
  terrain interdit, limite Difficile (1/type/île), coût. (2) **Sprites jonction VÉRIFIÉS à jour** : les 24
  PNG inlinés (`jonction_<H>_<V>_v1..v4`, 6 paires orientées) **correspondent byte-à-byte** au pack
  `Archipel_sprites_COMPLET` ; la logique d'orientation (`first` = porteur horizontal, `second` = vertical,
  via les masques de connexion) est correcte → aucun sprite obsolète, rien à ré-inliner. Validé : `node
  --check` (6 blocs) + rendu Chromium (0 erreur console). Changement
  11.07 : **inventaire ouvert : tout affiché + 1 tier par ligne.** (1) `.inventory.open` perd
  `max-height:140px`/`overflow-y:auto` → **tout l'inventaire est visible** (plus de scroll). (2) Un
  **saut de ligne forcé** (`<span class="inv-break">`, `flex-basis:100%`) est inséré avant CHAQUE
  séparateur de tier dans le rendu (boucle `inv`) → chaque tier (T0/T1/T2/T3/T4) **recommence sur sa
  propre ligne**. CSS + 1 élément de rendu. Validé : `node --check` + rendu Chromium (T0 ligne 1, T1
  ligne 2, 0 erreur). Changement
  11.06 : **pastille batterie sous l'électricité (HUD).** Le conteneur `.stocks` (qui contient
  exactement les pastilles ⚡ kW et 🔋 %) passe de `flex-direction:row` à **`column`** (gap 3px,
  `align-items:stretch`, `flex:0 0 auto`) → la batterie s'empile **sous** l'électricité au lieu d'être
  à côté ; PORT/RECHERCHE restent à droite (siblings dans `.hud-side`). CSS only (1 ligne). Changement
  11.05 : **accumulateur ×10 + estimation charge/décharge + boutons haut/bas en sprite (thème bleu).**
  (1) **Capacité accumulateur** `2048 → 20480` (×10 ; ×2^upgrade conservé). (2) **Estimation temps**
  dans la fiche (tap) : nouvelle ligne « Charge/Déch. » → « ⬆ plein dans `mm min ss s` » (charge) ou
  « ⬇ vide dans … » (décharge) ou « stable ». La boucle énergie stocke `bld.accDelta` (kWh/tick signé,
  + charge / − décharge) par accumulateur ; la fiche calcule `(cap−stock)/delta` ou `stock/−delta`.
  Lecture live (recalculé chaque tick). (3) **Boutons du haut (Options/?/PORT/RECHERCHE/INVENTAIRE/
  Production) et du bas (onglets d'action)** reçoivent le **sprite bouton 9-slice `--btn-*`** en bordure
  RÉDUITE (6px) sous `body:not(.theme-inox)` → « plus petit », comme les outils du menu bâtiment (avant
  ils étaient plats en thème bleu ; l'inox les avait déjà). + clés i18n (en/es/de). Validé : `node
  --check` (6 blocs) + CSS équilibré + rendu Chromium (boutons biseautés, 0 erreur, DE traduit). Changement
  11.04 : **texture « plaque métal bleue » affinée (v3).** Nouveau `ui_tex_bleu_brillant.png` (150×150,
  1166 o, navy quasi uni + une bande diagonale très douce) ré-uploadé dans le pack → `--tex-bleu`
  ré-inliné. Plus subtil/lisible encore que la v2 (11.02). Pas de voile (déjà retiré). CSS only.
  Validé : CSS équilibré + rendu Chromium (thème bleu très lisible, 0 erreur). Changement
  11.03 : **panneau Port (onglets Transit île/archipel) + mode import + centrale théorique + fix
  save→options.** (1) **Onglets dans le Port** : « Transit île » (contenu actuel, défaut) et « Transit
  archipel » (nouvel onglet listant TOUS les flux inter-îles, groupés src→dest, avec sprite ressource
  + débit /s) ; helper module `allTransitFlows(game)` (lit `game.transitFlow`). `PortPanel` : state
  `tab`, barre `.pp-tabs`/`.pp-tab`, rp-list rendu en ternaire ; vue archipel `.pp-arch-*`. (2) **Mode
  Priorité/Proportionnel = IMPORT** : `transferLink` utilise désormais le mode ET l'ordre de priorité
  de la **destination** (`tradeModeFor/tradePriorityFor(game, dest)` au lieu de `src`) → sur le panneau
  d'une île, ces réglages gouvernent ses imports. (3) **Centrale : prod/conso THÉORIQUES** : 2 lignes
  ajoutées à la fiche (`.ip-theo`, atténué/italique) montrant la cible (`frac`) en plus du réel
  (`realFrac`). (4) **Fix save→Options** : fermer le panneau Sauvegarde (ou « Sauvegarder ») rouvre les
  Options (d'où il a été ouvert) — `onClose`/`onSaveNow` font `setOptionsOpen(true)`. + clés i18n des
  nouveaux libellés (en/es/de). Validé : `node --check` (6 blocs) + smoke Chromium fr/de (Port 2 onglets,
  0 erreur, clés DE traduites). Changement
  11.02 : **nouvelle texture « plaque métal bleue » (douce) + retrait du voile.** Le pack a livré un
  nouveau `ui_tex_bleu_brillant.png` (**150×150**, « bandes brillantes diagonales espacées et douces »,
  navy foncé) remplaçant l'ancien 64×64 trop contrasté. `--tex-bleu` ré-inliné avec ce PNG ; le **voile
  sombre** (`linear-gradient(rgba(11,12,32,.74))`) ajouté en 10.99 pour rattraper l'ancienne texture est
  **retiré** des 2 règles de panneaux du thème bleu (la nouvelle texture est lisible telle quelle, sous
  l'ombre de texte conservée). Scopé `body:not(.theme-inox)`. CSS only. Validé : `node --check` + CSS
  équilibré + rendu Chromium (thème bleu lisible, sheen doux, 0 erreur). Changement
  11.01 : **i18n — Phase 2 / CHECKPOINT 2 (couche UI câblée).** Les libellés d'interface en dur sont
  désormais enveloppés dans `I18N.t('texte fr')` (modèle gettext, clé = texte français). Câblage fait
  par **transform automatisé conservateur** (script Node, 2 passes : littéraux UTF-8 puis formes
  `\xNN` de Babel), restreint au `<script>` du jeu, positions sûres uniquement (exclut
  className/key/id/label/name/type/color/comparaisons/clés d'objet ; tokens < 3 ou sans lettre
  ignorés) → ~440 enveloppes sur ~351 clés distinctes (sur 403). UI traduite : barre du haut,
  onglets d'action (Gebäude/Netz/Kopieren/Abreißen/Verbessern…), titres de panneaux, options, aides.
  **Reliquat** (reste en français hors-fr) : toasts en **littéraux-gabarits** (backticks `` ` ``, non
  matchés par le wrap par guillemets) + quelques libellés ajoutés après build 108 absents du TSV
  (ex. « Fond des panneaux »). Repli fr automatique partout. Validé : `node --check` (6 blocs, 0 échec)
  + smoke Chromium de/en/fr (UI traduite, FR intact, **0 erreur console**) + rendu jeu DE. Voir
  `PASSATION_I18N.md`. Changement
  11.00 : **i18n — Phase 1 / CHECKPOINT 1 (couche CONTENU + langue système + sélecteur).** Kit
  `archipel_i18n.js` (4 langues fr/en/es/de, API `t/get/set/applyToData/available/names`) **inliné**
  dans un `<script>` avant le script du jeu (fichier unique hors-ligne ; aucun `</script>` littéral).
  + **bloc d'augmentation** ajoutant aux 4 langues les 9 labels de catégories du menu Bâtiment +
  `Langue`. **`I18N.applyToData({BUILDINGS,RES_SHORT,TECH_NODES,TUTORIAL_STEPS,GAME_TIPS})`** appelé
  après `GAME_TIPS` → réécrit en place noms ressources/bâtiments/recherches + tuto + astuces (repli
  fr). **`TOOLBAR_GROUPS`** : `key` stable par groupe (infra/junction/extraction/energy/steel/copper/
  electronics/chemistry/nuclear), filtres `NETWORK_GROUPS`/`BUILD_GROUPS` sur `g.key`, label rendu via
  `I18N.t(g.label)`. **Sélecteur de langue** dans Options (reload au changement). Langue par défaut =
  système. `SAVE_VERSION` inchangé, aucune mécanique modifiée. Validé : `node --check` (6 blocs) + CSS
  équilibré + smoke Chromium (de→Eisenmine V1/Schließen, en→Iron Mine V1/Close, 0 erreur) + rendu menu
  DE (noms+ressources+catégories traduits). **RESTE : Phase 2 (couche UI, ~404 libellés `I18N.t`)** —
  le chrome UI est encore en français. Voir `PASSATION_I18N.md`. Changement
  10.99 : **fix amélioration réseau avec jonctions + thème « plaque métal bleue » (défaut).** (1)
  **Bug : impossible d'améliorer un réseau portant des jonctions** (route+câble+tuyau) — `coupledNetworkIds`
  couplait, via `junctionLinks`, les réseaux des DEUX porteurs DIFFÉRENTS d'une jonction (route↔câble) ;
  `networkLevelChange` exigeant un même niveau → bloqué « Réseaux couplés à des niveaux différents ». Or
  les réseaux d'un MÊME porteur traversant une jonction sont déjà fusionnés en un seul id par
  `rebuildNetworks` (union-find 10.59). `coupledNetworkIds` renvoie désormais `{networkId}` seul → chaque
  porteur s'améliore INDÉPENDAMMENT. (2) **Nouveau UI bleu** (pack `ui_tex_bleu_brillant`) : le thème
  par défaut (bleu) reçoit un **fond métal bleu brossé** sur tous les panneaux (`.hud`/`.research-panel`/
  `.slot-panel`/`.toolbar`/`.tip-popup`/`.mode-modal`/`.info-panel`/`.build-panel`), comme la tôle larmée
  de l'inox : `--tex-bleu` inliné + cadres rivets/sobre SANS `fill`. La texture brute (oblique très
  contrastée) étant illisible, elle est posée **sous un voile sombre** (`linear-gradient(rgba(11,12,32,.74))`)
  + ombre de texte → reflet métallique subtil et lisible. Scopé `body:not(.theme-inox)` (inox inchangé).
  Validé : `node --check` + CSS équilibré + rendu Chromium (réseau améliorable, thème bleu lisible). Changement
  10.98 : **fix freeze pose + calibrage réel + bouton alerte + fonderie or → électronique.** (1)
  **Freeze ~3 s à la pose (surtout via Copier) corrigé** : `onPointerUp` (et fin de pinch) appelait
  `flushSave()` **synchrone** → `serialize()` des 5 îles + `JSON.stringify` + `localStorage` à CHAQUE
  tap (≈ plusieurs secondes sur une grosse partie), **redondant** avec le `scheduleSave()` déjà planifié
  par `tryPlace`/`tryDemolish`. Remplacé par `scheduleSave()` (débouncé 500 ms). Garde `if(!b) return`
  dans `tryPlace` (évite un crash `'kind'` si l'outil est invalide). (2) **Calibrage centrale = production
  RÉELLE** : la fiche affiche les Entrées/Sortie (U235, eau froide, kW, matériau irradié) selon la
  fraction **réelle en cours** (`bld.nucCur / maxPower`, rampe sigmoïde) au lieu de la cible — 0 à
  l'arrêt/sécurité, valeur qui monte pendant le calibrage ; le curseur affiche toujours la cible. (3)
  **Bouton ALERTE** (HUD, à droite de Production) : helper `activeStockAlerts(game)` (stock port < seuil,
  toutes îles) ; bouton orange pulsant `.inv-alert-btn` (visible si ≥1 alerte, badge = nombre) ouvrant
  `AlertsPanel` (liste Île/ressource/stock·seuil, clic → va à l'île + ouvre le Port). (4) **Catégorie
  bâtiment** : `fonderie_or` déplacée de « Or » (supprimée) vers **« Électronique »**. Validé :
  `node --check` + CSS équilibré + chargement Chromium sans erreur. Changement
  10.97 : **4 ajustements UI/jeu.** (1) **Lisibilité inox renforcée** : `--ink-dim`/`--ink-faint`
  encore éclaircis (`#d6dae2`/`#bcc1cb`) + **ombre portée** (`text-shadow:0 1px 2px rgba(0,0,0,.55)`)
  sur le texte de tous les panneaux/barres inox (`.hud`/`.research-panel`/…/`.build-panel`) → le texte
  se détache de la tôle larmée. (2) **Bouton de fermeture = petite CROIX dessinée** (sprite-like) :
  `.rp-close`/`.slot-close`/`.ip-close` deviennent un carré 24px (fond `--panel-2`, cadre `--line`)
  avec une croix en pseudo-éléments (`::before`/`::after` barres rotées 45°/-45°, glyphe « × » masqué
  via `font-size:0`) ; survol rouge. Cohérent thème bleu ET inox (via variables). (3) **Centrale posée
  démarre à 0 %** : à la pose d'un bâtiment `nuclear`, `game.nuclearPower[currentIsland]` est forcé à
  **0** (dans `tryPlace`) → la centrale reste à l'arrêt (ne tente pas de démarrer faute de
  refroidissement, plus de mise en sécurité immédiate) ; le joueur branche puis monte la jauge. (4)
  **Nouvelle catégorie « Électronique »** dans le menu Bâtiment : `broyeur`/`raffineur_silicium`/
  `circuit`/`fab_processeur` sortent de « Cuivre » (qui garde four_cuivre/_v2 + câblerie + four_arc_cable) ;
  `fonderie_or` reste dans « Or ». CSS + data + 1 ligne de logique. Validé : `node --check` + rendu
  Chromium (croix visible, texte lisible). Changement
  10.96 : **ressources irradiées en T4 (inventaire) + sous-catégories « Traitement ».** (1) Les 4
  matériaux **irradiés** (`acier_irradie`/`beton_arme_irradie`/`cable_irradie`/`ciment_irradie`) passent
  de `RES_TIER` t3 → **t4** ; ajout de `t4:4` à `RES_TIER_RANK` et `t4:'T4'` à `RES_TIER_LABEL` → ils
  s'affichent sous un séparateur **T4** dans l'inventaire (le tri `resTierRank` les place après t3).
  (2) L'ancienne catégorie unique **« Traitement »** du menu Bâtiment (`TOOLBAR_GROUPS`) est scindée en
  **4 catégories** par filière : **Fer-acier** (four_fer/_v2, acierie, four_arc_acier, cimenterie,
  betonniere, atelier_meca, four_arc_piece), **Cuivre** (four_cuivre/_v2, cablerie, four_arc_cable,
  circuit, broyeur, raffineur_silicium, fab_processeur), **Plastique et chimie** (usine_polymere,
  raffinerie, distillerie), **Or** (fonderie_or). Aucune logique de jeu ; les filtres NETWORK/BUILD
  ne testent qu'Infrastructure/Jonctions (intacts). Validé : `node --check` + rendu Chromium (catégories
  visibles, ex. « Fer-acier »). Changement
  10.95 : **centrale 4 MW + jauge de puissance + lisibilité inox renforcée.** (1) **`NUC_POWER`
  16384 → 4096** : la centrale V1 (niveau 1 / 100 %) produit **4 MW** (et ×2^upgrade : Nv.1 = 8 MW…).
  MAJ dans le tick ET la fiche. (2) **Curseur de puissance = jauge graduée** : les 3 boutons (−/%/+)
  sont remplacés par une **barre graduée en %** (remplissage violet `linear-gradient`, graduations
  tous les 10 %, % centré) encadrée de boutons **−** et **+** (classes `.ip-nuc-pow`/`.ip-nuc-pm`/
  `.ip-nuc-gauge*`). (3) **Lisibilité inox** : `--ink-dim`/`--ink-faint` encore éclaircis et neutralisés
  (`#cbd0d9`/`#aeb3bd`, moins de teinte bleue froide) → les sous-textes du panneau Port (« X en stock »
  `.pp-res-stock`, en-têtes `.pp-cfg-head`, labels `.pp-section-label`) et autres textes dim deviennent
  bien lisibles sur la plaque larmée. CSS+constante, logique inchangée. Validé : `node --check` + rendu
  Chromium (Port lisible, jauge OK). Changement
  10.94 : **recettes nucléaires + UI (lisibilité, boutons du haut).** (1) **Centrale enrichissement** :
  +`acier: 1` aux intrants (yellow_cake 8 + **acier 1** → U235 1). (2) **Centrale nucléaire — matériau
  irradié OPTIONNEL** : la centrale ne dépend plus du matériau (acier/béton/câble) pour tourner
  (`fuelOK` = route + U235 seulement) ; le matériau n'est consommé (et l'irradié produit) QUE s'il est
  livré au port (`if port[matKey] >= irrAmt`) — sinon la centrale fournit l'électricité seule. La fiche
  affiche désormais Entrées = U235 + eau froide, Sortie = kW, et une **sous-fenêtre encadrée séparée**
  « Matériau irradié (optionnel) » (conso→prod + note + sélecteur acier/béton/câble), classes
  `.ip-nuc-irr*`. (3) **Boutons du haut** (Options/?/PORT/RECHERCHE/INVENTAIRE/Production) = **sprite
  bouton inox 9-slice** (`--inox-btn-*`, bordure 6px) comme le menu du bas — les pastilles de valeur
  (kW/batterie/chips) restent plates. (4) **Lisibilité thème inox** : `--ink-dim`/`--ink-faint`
  éclaircis (`#aab1bf`/`#878e9c`) → sous-textes (« X en stock », en-têtes, valeurs réseau, sous-libellés
  MONTER…) lisibles sur la plaque larmée. CSS+recettes, logique nucléaire inchangée sauf l'optionalité
  du matériau. Validé : `node --check` + rendu Chromium. Changement
  10.93 : **refonte tech tree nucléaire + centrale réglable/améliorable (LOGIQUE).** (1) **Tech tree :**
  les mines v3 ne sont PLUS débloquées par le nœud 24 (Centrale, « trop vite ») mais par le **nœud 25
  (Usine Moteur Nucléaire)** — donc après le moteur. Nouveau bâtiment **`mine_uranium_v3`** (coût 100
  béton armé + 100 acier + 100 câble + 1 `element_moteur_nuc` ; `randomP` 8-64 ; sortie uranium 32/s)
  ajouté aux unlocks du nœud 25, à `TOOLBAR_GROUPS` Extraction. **Coût de TOUTES les mines v3** :
  `processeur:10` → **`element_moteur_nuc:1`** (il faut donc avoir un moteur). **Antenne (nœud 26)** :
  `produce element_moteur_nuc` 10 → **1000**. (2) **Centrale nucléaire (réécriture du bloc tick) :**
  **puissance ×2** (`NUC_POWER` 8192→16384) ; **curseur de puissance 0→100 % par paliers de 10 %**
  (`game.nuclearPower[isl]`, défaut 100 %) qui met à l'échelle **intrants ET sorties** (U235, matériau,
  eau froide, matériau irradié, kW) ; **recalibrage sigmoïde de 5 min à CHAQUE changement** de puissance
  (ou d'amélioration) via une rampe `nucFrom→nucTo` ; **mise en SÉCURITÉ** si l'eau froide manque (état
  `safety`, 5 min sans rien consommer/produire, **redémarrage auto** + **notification** via `game.nucNotify`
  → toast rouge) ; **plus de ciment irradié** (`ciment` retiré de `NUC_MATS` partout) ; **centrale
  AMÉLIORABLE** comme les autres (`isUpgradable` n'exclut plus `nuclear` ; ×2^niveau sur puissance +
  intrants + sorties). Fiche centrale réécrite (curseur −/%/+, états Calibrage/Sécurité, IO à l'échelle).
  Persistance : `game.nuclearPower` (newGame/serialize/loadSave) + `nucCur` par centrale ; `ciment` retiré
  des whitelists `nuclearConfig`. Validé : `node --check` + simulation machine à états (calibrage→16384 kW,
  sécurité+notif+redémarrage, recalibrage 50 %→8192, upgrade Nv.1→32768, 0 %→arrêt) + chargement Chromium
  sans erreur. Changement
  10.92 : **tôle larmée inox sur TOUTE l'UI (barre du haut + tous les panneaux), comme le menu
  bâtiment.** Jusqu'en 10.91 seuls `.build-panel` (+ `.inventory` depuis 10.90) avaient la tôle larmée ;
  la barre du haut `.hud` et tous les modaux (`.research-panel`/`.slot-panel`/`.tip-popup`/`.mode-modal`/
  `.info-panel`/`.toolbar`) gardaient un fond gunmetal PLEIN (`--inox-panneau` avec `fill`). Désormais
  ils reçoivent TOUS le **MÊME** fond que le menu bâtiment : `background:var(--tex-inox-leger) repeat`
  + cadre `--inox-panneau` **SANS `fill`** → la **plaque larmée inox** est visible derrière Recherche,
  Port/transit, fiche bâtiment, Options, Production, Aide, barre du haut et barre d'actions ; les
  cartes/boutons internes restent gris gunmetal par-dessus (« fond plaque inox, bouton gris »). CSS
  only. Changement
  10.91 : **palette bleue → gunmetal pour TOUT le thème inox (fin des fonds bleus dans les panneaux).**
  En 10.88-10.90 seuls les CADRES + la barre du haut passaient inox ; les **cartes/encarts/champs/
  boutons internes** des panneaux restaient bleus (Recherche `.rp-node`, Options, liaisons Port
  `.pp-link`, encart Énergie `.ep-stats`, cartes Aide `.help-card`, champs `NumField`, priorité
  Haute/Normale/Basse `.ip-fluxpri-btn`…) car ils utilisent `var(--panel)`/`var(--panel-2)`/`var(--line)`.
  Fix : **une seule règle `body.theme-inox{--panel:#262931;--panel-2:#2f323b;--line:#474c57;}`** rebascule
  toute la palette → tous ces éléments deviennent **gris gunmetal** d'un coup (« fond plaque inox, bouton
  gris »). Les accents de SÉLECTION codés en dur (jaune Priorité/PRÊT, bleu `#0277BD` Normale, violet
  `#7E57C2` matériau, vert toggles) restent pour distinguer l'état actif. CSS only, aucune logique.
  Validé : `node --check` + CSS équilibré + rendu Chromium (Options/Recherche → cartes grises, 0 bleu). Changement
  10.90 : **barre du haut en PLAQUE INOX (thème inox).** En 10.88 le cadre `.hud` passait inox mais
  les éléments INTERNES restaient bleus : la bande **inventaire** (`.inventory`, `var(--panel-2)`) et
  toutes les **pastilles** (`.options-btn`/`.research-btn` PORT·RECHERCHE/`.stock` kW·batterie/
  `.inv-label-btn` INVENTAIRE/`.inv-prod-btn` Production/`.inv-item`/`.inv-count`, en `var(--panel)`/
  `--panel-2`). Désormais (sous `body.theme-inox`) : l'inventaire passe sur **tôle larmée légère**
  (`--tex-inox-leger` repeat, bordures `#3a3d47`) et toutes les pastilles passent en **gunmetal**
  (`#2c2f38`, bordure `#4a4f5a`, texte `#ebeef5`) + survol `#363a44`. Les accents (vert REPARER,
  vert/rouge kW·batterie via règles plus spécifiques, indice or des onglets/tiers) sont conservés.
  CSS only, aucune logique touchée. Validé : `node --check` + CSS équilibré + rendu Chromium (plus
  aucun fond bleu en haut). Changement
  10.89 : **débit max /s du transit affiché dans le panneau Port (section « Amélioration du
  transit »).** La ligne montrait « Taille des lots ×N (X u) » → « ×N+1 » (formulation héritée du
  système par paquets, alors que le transit est CONTINU depuis 10.48) : on ne voyait pas ce que
  l'amélioration du port augmente réellement. Désormais elle affiche **« Débit max <X> u/s · lots ×N »**
  (à gauche) et **« → <Y> u/s »** (prochain niveau, à droite), où le débit = `floor(shipBatchBase() ×
  2^niveau / TRANSIT_DIV)` = le plafond `transitPerSec` par liaison et par seconde (partagé entre les
  ressources expédiées). `TRANSIT_DIV` (=60) déjà accessible ; `title` explicatif + classe `.pp-port-mult`
  (lots ×N atténué). Aucune logique de jeu touchée (affichage seul). Changement
  10.88 : **thème « Fond inox » rendu PLEINEMENT cohérent (PROMPT_UI_INOX).** Le 10.86 mélangeait
  les styles (cartes restées bleu foncé sur fond tôle larmée brillante). Désormais un seul thème
  métal gris appliqué PARTOUT, via **8 nouveaux sprites `theme_inox`** inlinés en variables CSS
  `:root` (~2,8 Ko) : `--inox-panneau` (cadre 9-slice gunmetal `#24262e`), `--inox-btn-normal/-hover/
  -active/-off` (4 états de bouton), `--inox-onglet-actif/-inactif` (onglets, accent or), `--tex-inox-leger`
  (tôle larmée ATTÉNUÉE 32×32 tileable). **Application (CSS pur, scopée `body.theme-inox`)** : (1)
  **barre du haut (`.hud`) + panneaux/cartes/tooltips** (`.research-panel/.slot-panel/.tip-popup/
  .mode-modal/.info-panel/.toolbar`) → cadre inox PLEIN (`fill`) + texte clair `#ebeef5` ; (2) **menu
  construction (`.build-panel`)** → fond `--tex-inox-leger` repeat + cadre inox SANS `fill` (texture
  visible) ; labels catégorie (`.tool-group-label`) en `#c8ccd2` ; (3) **cartes de bâtiment**
  (`.build-panel .tool-btn`) = boutons inox 4 états (override du kit bleu `--btn-*`) ; (4) **boutons
  d'action** (`.tab-btn` : Bâtiment/Réseau/Copier/Démolir/Améliorer) = boutons inox 4 états (le nom
  garde son indice rouge/vert) ; (5) **onglets** (`.island-tab`/`.prod-tab` : Inventaire/Production +
  îles 1-5) = sprites onglet inox (bordure 5px pour les onglets d'île compacts). Couleurs de valeurs
  (vert/orange/rouge des ressources) conservées via les règles plus spécifiques existantes. Le thème
  par défaut (bleu) est inchangé (toutes les règles sont sous `body.theme-inox`). `--cadre-inox`
  (10.86) devient inutilisé (laissé). Aucune logique de jeu touchée. Validé : `node --check` OK +
  rendu Chromium (0 erreur console, plus aucun panneau bleu). Changement
  10.87 : **2 corrections de bugs (chasse aux bugs).** (1) **Conversion croisement→jonction sans
  remboursement** (`tryPlaceJunction`) : poser une jonction PAR-DESSUS un réseau infra existant
  (`tileCarrier`) écrasait `t.building` (le réseau croisé) **sans rembourser** ses matériaux ni ses
  améliorations → perte sèche (ex. câble V3 absorbé), aggravée en Difficile où la jonction est gratuite.
  La démolition d'une jonction ne restitue que le coût de la jonction → asymétrie. Désormais, quand on
  convertit un croisement, l'infra écrasée est **remboursée** (`refund(ob.cost)` + `cumulativeUpgradeCost`
  si améliorée), hors mode dev → conservation de la matière (place/démolit redevient neutre). (2)
  **Extrapolation hors-ligne d'une île débloquée pendant l'échauffon** (`runCatchUp`, mode simplifié
  > 1 h) : une île dont l'accès se confirme PENDANT les 300 ticks d'échantillon reçoit un **kickstart
  ponctuel** ; son port étant absent du `baseSnap`, le débit mesuré (`kickstart/sampleTicks`) était
  extrapolé sur les milliers de ticks restants → stocks démesurés. Garde `if (!base[isl]) continue;`
  → les îles apparues après le snapshot ne sont plus extrapolées. Changement
  10.86 : **option « Fond bleu / Fond inox » (ambiance des panneaux).** Nouvelle préférence `ui.theme`
  (`'bleu'` défaut | `'inox'`), persistée dans `uiPrefs` (pattern complet : newGame, serialize,
  loadSave défaut+restore, state React `theme`, sync au load + à l'ouverture des options). Sélecteur
  2 boutons (Bleu/Inox) en haut de l'`OptionsModal` (`opt-theme-sel`, prop `theme`/`onSetTheme=chooseTheme`).
  Un `useEffect` pose la classe **`body.theme-inox`**. CSS : par défaut tout reste bleu (sobre/rivets) ;
  sous `body.theme-inox`, `.research-panel/.slot-panel/.tip-popup/.mode-modal/.info-panel` reçoivent le
  **cadre inox** (fill), et `.build-panel/.toolbar` la **tôle larmée** (`--tex-inox` en background) +
  **cadre métal** sans fill. Remplace le cadre métal inconditionnel du 10.85 (build-panel redevient
  bleu par défaut). CSS + uiPrefs only, aucune logique de jeu. Changement
  10.85 : **menu construction en 3 colonnes + ambiance métal.** (1) `.build-panel .tool-row` passe de
  `repeat(4,1fr)` à **`repeat(3,1fr)`** — avec le cadre 9-slice des boutons (10.84), 4 colonnes
  tronquaient les noms/coûts (« Carriè », « Centra Charbo »…) ; 3 colonnes laissent la place. (2) Le
  menu construction (`.build-panel`) reçoit le **cadre MÉTAL** (`--cadre-metal`, `8 fill / 8px stretch`)
  au lieu du sobre (10.83) → accent industriel sur la surface déjà thémée. CSS pur. Les autres sprites
  du kit (onglets, boutons HUD, inox/texture) restent inlinés non câblés (densité mobile / sémantique
  couleur — à activer selon retour). Changement
  10.84 : **kit UI complet inliné (PROMPT_UI_COMPLET) + boutons 9-slice 4 états sur le menu
  construction.** (1) **10 nouveaux sprites UI** inlinés en variables CSS `:root` (~3,8 Ko) :
  `--btn-normal/-hover/-active/-off` (4 états de bouton `ui_bouton_*_9slice` 17×17), `--onglet-actif/
  -inactif` (onglets), `--cadre-metal/-inox/-bouton` (cadres), `--tex-inox` (tôle larmée 32×32 tileable).
  S'ajoutent à `--cadre-rivets/-sobre` (10.54). (2) **Application** : seuls les boutons du **menu
  construction** (`.build-panel .tool-btn`, zone défilable) reçoivent le kit 4 états (normal→survol→
  enfoncé/sélectionné via `border-image var(--btn-*) 8 fill / 8px stretch`) ; le nom garde sa couleur
  d'accent. Volontairement **pas** appliqué aux boutons du HUD (hauteur fixe → un cadre 8px les ferait
  déborder) ni aux boutons sémantiques colorés (vert confirmer / jaune primaire) ni aux onglets
  compacts (densité mobile), ni l'ambiance metal/inox (sobre reste par défaut) — ces sprites sont
  inlinés et **disponibles** mais non câblés (à activer selon préférence/retour visuel). CSS pur. Changement
  10.83 : **extension du cadre 9-slice aux menus/panneaux/tooltips/barre d'outils** (PROMPT_MENU_9SLICE
  / PROMPT_INTEGRATION_UI1). Le cadre n'était posé que sur `.research-panel`/`.slot-panel` (et tous les
  modaux réutilisant `research-panel`). Désormais : (1) la règle existante reçoit **`fill`** (peint le
  fond `#15152a` = `var(--panel)`) + `image-rendering:pixelated` ; (2) **cadre SOBRE** (`--cadre-sobre`,
  `8 fill / 8px stretch`) sur `.tip-popup`, `.mode-modal`, `.info-panel` (feuille bâtiment), `.build-panel`
  (menu construction) ; (3) **cadre RIVETS** sur `.toolbar` (barre d'outils). Pur CSS, aucune logique
  touchée. ⚠️ Les **sprites UI** (`ui_*`/`item_*`/`etat_*`, 52) restent déjà 100% inlinés+câblés (10.54+) :
  rien de neuf côté icônes. NB : `box-sizing:border-box` global → le cadre 8px n'élargit pas les
  conteneurs. Changement
  10.82 : **stockage tuyau relié au port = stocké AU PORT (comme une route).** Un réseau TUYAU
  adjacent au port (`net.connected`) ne garde plus ses liquides (pétrole/eau/acide) dans son pool
  invisible : ils sont désormais stockés dans le **port** (visibles dans l'inventaire, partagés,
  transitables). (1) **Boucle bâtiment** (`tickIsland`) : si un réseau tuyau adjacent est `connected`,
  les intrants/extrants tuyau **stockables** du bâtiment basculent du bucket `pipe` (pool) vers
  `pipePort` (port) — même chemin que le diesel ; `NON_STORABLE` (eau_froide du nucléaire) reste dans
  le pool (tampon transitoire). (2) **Purge fin de tick** : les pools des réseaux tuyau `connected`
  sont **vidés dans le port** chaque tick (migration des réserves existantes incluse). (3)
  **`tradeAvail`/`Draw`/`Deposit`** simplifiés : lisent/écrivent directement le port pour TOUTES les
  ressources (les liquides reliés au port y sont désormais) → transit pétrole/acide via le port.
  `portPipePools` devient du code mort (laissé). Les réseaux tuyau **non reliés** au port gardent leur
  pool local (boucle isolée inchangée). Changement
  10.81 : **rééquilibrage production (mines hautes / offshore / fours à arc).** (1) **Mines v2 ×8**
  (`outputs` 2→16) : `mine_fer_v2`, `mine_charbon_v2`, `mine_cuivre_v2`, `carriere_v2`. (2) **Mines v3
  ×8** (4→32) : `mine_fer_v3`, `mine_cuivre_v3`, `carriere_v3`, `mine_charbon_v3`. (3) **Offshore ×8
  output** : `eolienne_offshore` energie_kw 16→128, `plateforme_petroliere` petrole 1→8. (4) **Fours à
  arc ×4 intrants & extrants** : `four_arc_acier` (minerai_fer 24→96, acier 1→4), `four_arc_cable`
  (minerai_cuivre 36→144, cable 1→4), `four_arc_piece` (minerai_fer 24→96, piece_meca 1→4). Changement
  10.80 : **coût d'amélioration en pastilles dans la fiche bâtiment** — la ligne « Coût » de la
  prévisualisation d'amélioration (`InfoPanel`, `ip-up-preview`) n'était qu'un texte `formatCost`
  coloré en entier (jaune si payable, rouge sinon). Désormais elle rend une **pastille par ressource**
  (`.ip-cost-chips`/`.ipc-ci`) ; seules les ressources **indisponibles** (stock port `currentIsland` <
  coût) passent en **rouge** (`.miss`), comme le menu construction (10.78). `port` déjà en scope. Changement
  10.79 : **3 ajustements + vérif pack UI.** (1) **`eolienne` ×2 plus chère** (`cost` ciment 60→120,
  lingot_cuivre 60→120). (2) **`eolienne_offshore` ×2 efficace** (`outputs.energie_kw` 8→16). (3)
  **`pompe_eau` disponible partout** : `exclusiveIsland: 3` retiré (reste `terrains:['coast']` → toute
  île une fois la recherche débloquée). ⚠️ **Pack `Archipel_sprites_COMPLET` (UI) déjà 100% intégré**
  par les builds antérieurs : les 52 sprites `ui_*`/`item_*`/`etat_*` + 2 cadres 9-slice sont tous dans
  `__SPRITE_DATA__`, et le câblage existe déjà (`uiIcon` pour onglets/boutons, `itemSpriteKey` pour
  HUD/recettes/coûts, `statusSpriteKey`+`drawDeficitIcon` pour les overlays d'état au Canvas, cadres
  `--cadre-rivets` sur `.research-panel`/`.slot-panel` et tous les modaux qui réutilisent la classe).
  L'`info-panel` est une feuille basse (border-top, pas une carte) → pas de cadre, voulu. Rien de
  nouveau à inliner. Changement
  10.78 : **4 demandes UI.** (1) **Coût build en pastilles** : `ToolButton` affiche le coût en
  pastilles `.tb-cost`/`.tb-ci` ; seules les ressources **indisponibles** (stock port < coût) passent
  en rouge (`.miss`). `port` (= `game.port[currentIsland]`) propagé App→`Toolbar`→`ToolButton`. Infra
  (tracé continu) / jonctions à coût croissant gardent leur libellé texte. (2) **Coûts transit
  rééquilibrés** (`PORT_BASE_COST`) : î1 100k ciment+100k lingot_fer ; î2 100k ciment+15k acier ; î3
  15k béton_armé+50k acier ; î4 50k béton_armé+100k piece_meca ; î5 100k acier+100k béton_armé+100k
  cable. (3) **Sélecteur d'île = 5 boutons** côte à côte (`IslandSelector` réécrit, fini le menu
  déroulant) ; CSS `.island-tabs`/`.island-tab` flex + `min-width:0` + media-queries (≤560/≤380px) →
  ne déborde jamais. (4) **Panneau Production** : bouton `📊 Production` à droite d'INVENTAIRE (`Hud`
  prop `onOpenProduction`, state App `prodOpen`). `ProductionPanel` + helper `islandFlowAgg` (agrège
  `game.netFlow[isl]` sur tous les réseaux) : onglets Toutes/î1-5, tableau **Prod/Conso/Net /s** par
  ressource, bilan énergie (kW), et liste **Transit** inter-îles (depuis `game.transitFlow`). Lecture
  live. Changement
  10.77 : **réserve d'énergie (accumulateurs) dans le `NetworkPanel` câble** — pendant de la ligne
  « Réserve » du tuyau (10.74). La boucle énergie (`wireInfo`) accumule désormais `accStored`/`accCap`/
  `accCount` par composante électrique (somme `acc.stored`/`acc.capacity` des accus de la composante).
  Le `NetworkPanel` câble affiche une ligne **« Réserve » `🔋 <charge> / <capacité> kWh · X%`**
  (`fmtInt`) quand ≥1 accumulateur est branché (`showWireAcc = wi.accCount>0 && wi.accCap>0`), insérée
  juste après la ligne « Production ». Lecture live (l'info est recalculée chaque tick). Changement
  10.76 : **animation du littoral (écume + falaises + triangles)** — passe additive sur le rendu
  statique du 10.75 (`PROMPT_INTEGRATION_ANIM.md`). (1) **100 spritesheets** `anim/` (128×32 = 4
  frames de 32) inlinées dans `__ANIM_DATA__` (~198 Ko), **clé = clé de sprite STATIQUE** (`coast_*`
  25, `iN_falaise_*` 55, `tile_iN_coast_tri_*` 20) → `ANIM_BY_SK` les mappe automatiquement (frame 0 ≈
  statique). Entrées `ANIM_META` ajoutées (fps **4** = `floor(now/250)%4`, le compteur global voulu par
  la spec). (2) **Draw** : les 3 couches côtières routées via `drawAnimFrame` (frame GLOBALE, **sans**
  déphasage `r+c` → écume continue entre tuiles, ≠ brise diagonale des tuiles de base) avec repli
  `drawSprite` tant que la sheet décode. Base terre/côte/eau = déjà animée (brise, 10.66/10.51) ;
  overlays obstacle/ressource/pétrole = **statiques** (non animés, voulu). Réutilise le mécanisme
  `_animPlayed`→redraw existant (la brise force déjà le redraw continu → aucun surcoût nouveau). NB :
  pas de toggle `animationsEnabled` ajouté (cohérence avec le jeu qui anime déjà bâtiments+brise sans
  option ; à faire en option globale séparée si besoin). Validé : `node --check` OK + 100 clés présentes
  data+méta (128×32) et toutes mappées à un sprite statique réel. Changement
  10.75 : **rendu du LITTORAL (écume + falaises + triangles de transition + overlays) — auto-tiling
  statique**. Intégration du pack `Archipel_sprites_COMPLET` (sprites côtiers, jusque-là NON intégrés).
  (1) **103 sprites statiques** inlinés per-key dans `__SPRITE_DATA__` (~46 Ko) : `coast_*` (25 écume
  génériques), `iN_falaise_*` (55 = 11×5 îles), `tile_iN_coast_tri_*` (20 triangles), `overlay_*` (3 :
  obstacle/resource/petrole). (2) **Helpers d'auto-tiling** (module, ~après `drawTileAnim`) :
  `coastIsWater/IsLand/IsCoast`, `coastFoamPieces` (0..2 clés `coast_*` selon les 8 voisins terre),
  `coastCliffPieces` (0..2 `falaise_*`), `coastTransitionTri` (`nw/ne/sw/se`), `COAST_FEATURE_OVERLAY`.
  Règle clé : **l'écume ET les falaises se dessinent sur les tuiles d'EAU**, d'après les tuiles de
  TERRE voisines. (3) **Boucle terrain de `draw()` réécrite** en 3 branches : tuile EAU = `tile_iN_water`
  + écume (overlay) + falaise (overlay par-dessus) ; tuile TERRE = base `tile_iN_coast` (si elle touche
  l'eau, recalculé via `coastIsCoast`) sinon `tile_iN_land` + triangle de transition + overlay de feature
  (les overlays **remplacent** les anciennes tuiles cuites `tile_iN_obstacle/resource/oil`, gardées en
  repli) ; branche `else` = ancien rendu si `SPRITES_ENABLED` faux. AUCUNE modif de la logique de jeu
  (éco/tick/save). Validé : `node --check` OK + simulation (île carrée + lac + chenal) → 0 clé manquante,
  écume/falaise/triangles cohérents. ⚠️ **Animations côtières NON intégrées** (les sheets `anim/coast_*`,
  `anim/iN_falaise_*`, `anim/..._breeze` du pack, ~143 Ko) : feature de suivi (frame 0 = sprite statique
  actuel, donc base correcte). Changement
  10.74 : **réserve du pool tuyau + plage de demande élec. dans le `NetworkPanel`**. (1) **Réserve
  tuyau** : le pool d'un réseau TUYAU stocke les liquides (pétrole/eau/acide…) **sans plafond** (seul
  `eau_froide` ∈ `NON_STORABLE` est purgé chaque tick) — invisible dans l'inventaire du port. Le
  `NetworkPanel` affiche désormais une ligne **« Réserve »** (`poolEntries` = `netObj.pool` filtré
  >1e-6, trié, `fmtPool`/`fmtPort`) pour un tuyau ; la ligne « Transit : aucun » n'apparaît plus si
  une réserve existe. Explique « ça marche malgré le déficit » = la citerne tampon. (2) **Plage
  min→max élec.** : nouveau helper `minPower(bld)` (plancher sigmoïde `s.base`, borne basse aléatoire
  `randomP.min`, conso fixe sinon). La boucle énergie (`wireInfo`) accumule `demandMin`/`demandMax`/
  `variable` par composante câble (somme `minPower`/`nominalPower` des consommateurs ; `variable` si
  ≥1 sigmoïde/aléatoire). Le `NetworkPanel` câble montre une ligne **« Demande min→max »**
  (`fmtInt(demandMin)→fmtInt(demandMax) kW`) quand `wi.variable` et l'écart > 0,5 → dimensionner la
  prod pour le pire cas. (La fiche bâtiment montrait déjà le min→max par bâtiment, lignes ~7191/7195.)
  Changement
  10.73 : **démolition CLIC PAR CLIC (fin du balayage)** — en mode Démolir, le geste démolissait
  dès qu'il DÉMARRAIT sur un bâtiment puis **balayait** (`drag.demoStart` + `tryDemolish` dans
  `onPointerMove`) → rasait une rangée entière au moindre glissé = suppressions accidentelles.
  Désormais la démolition se comporte comme la pose/amélioration : `onPointerDown` (branche
  `DEMOLISH`) ne fait que `drag.mode='demolish'`/`panned=false` (plus de démolition immédiate ni de
  `demoStart`) ; un **glissé déplace la carte** (branche pan fusionnée `place|upgrade|demolish` dans
  `onPointerMove`, seuil `TAP_THRESHOLD`) ; la démolition ne se déclenche qu'au **tap franc** dans
  `onPointerUp` (`!drag.panned && drag.dist < TAP_THRESHOLD` → `tryDemolish(r,c,true)`), **un
  bâtiment par clic**. Texte d'aide MAJ. Changement
  10.72 : **sprites de connexion des réseaux (route/câble/tuyau) intégrés** — le pack
  `Archipel_sprites_COMPLET` contenait les **180 tuiles de connexion** `route|cable|tuyau_v{1..4}_{01..15}_*`
  (auto-tiling N/E/S/O). Le code de dessin les RÉFÉRENÇAIT déjà (`NET_PREFIX`+`NET_MASK_SUFFIX`,
  `drawSprite(...)→continue`, sinon repli vecteur) mais elles n'étaient PAS dans `__SPRITE_DATA__` →
  réseaux dessinés en rectangles. Désormais inlinées (per-key, ~68 Ko) → routes/câbles/tuyaux rendus
  en **vraies tuiles texturées connectées** (V4 = illimité). NB : le pack contient aussi des sprites de
  **côte/falaise/overlay** (`coast_*`, `iN_falaise_*`, `tile_iN_coast_tri_*`, `overlay_*`) NON intégrés :
  ils nécessitent une **nouvelle logique d'auto-tiling du littoral** (le jeu dessine la côte en tuile
  unique `tile_iN_coast`) — feature séparée à faire. Changement
  10.71 : **rattrapage hors-ligne non bloquant (barre de progression) + calcul simplifié**. (1)
  `runCatchUp(elapsedSec, onDone)` réécrit : simulation par **tranches de ~80 ms** via `setTimeout`
  (rend la main à l'UI entre chaque) → **overlay `.catchup-overlay`** plein écran avec spinner +
  **pourcentage** + barre (`catchUp` state {pct, approx}). Le `frame` saute le tick tant que
  `g.catchingUp` (pas de double-tick). Absence ≤ 5 min = chemin synchrone sans overlay (imperceptible).
  (2) **Calcul simplifié** au-delà d'1 h (`ticks > 3600`) si l'option est active : on simule un
  **échauffon** de 900 ticks réels, on mesure le débit moyen sur les 300 derniers, puis on
  **extrapole** le reste (`port[k] += rate × restant`, clampé ≥ 0) → rattrapage quasi instantané.
  (3) **Option `simplifyOffline`** (défaut **oui**, persistée dans `uiPrefs`) + toggle dans
  `OptionsModal` (« Calcul hors-ligne simplifié »). Changement
  10.70 : (1) **processeur retiré du coût** de `eolienne_offshore` (10→0) et `plateforme_petroliere`
  (30→0). (2) **stockage batterie en temps réel** dans la fiche bâtiment (tap) : la ligne « Stockage »
  affiche désormais `🔋 <charge> / <capacité> kWh · X%` (lecture live de `bld.stored`, mis à jour
  chaque tick via `energyChanged`→`resChanged`→`bumpHud`) au lieu de la seule capacité. La ligne
  « Élec. » (0 kW trompeur) est masquée pour les accumulateurs (`!b.accumulator`). Changement
  10.69 : **intrants en déficit (orange) + ligne « intrants réels » dans la fiche bâtiment**. (1) Le
  tick stocke désormais `bld.inAvail` = ratio dispo/demande PAR intrant (calculé dans la boucle
  bâtiment depuis `workPort`/pools, route + tuyau + pipePort). (2) `recipeChips(rec, mult, avail)`
  accepte ce map : un intrant dont `inAvail[k] < 0.995` passe en **orange** (`.dr-res.dr-short`,
  titre « déficit X% dispo ») dans la ligne **Entrées** de l'`InfoPanel`. (3) Nouvelle ligne **« Réel »
  entrées** (quand le bâtiment tourne au ralenti, `0 < speedPct < 100`) : consommation réelle
  `inputs × speedPct/100` (miroir de la ligne « Réel » de sortie). Changement
  10.68 : 4 demandes. (1) **fix transit incohérent** : le flux SORTANT du panneau Port lisait
  `shippableQty` (intention) → pouvait afficher « aucun flux sortant » alors que l'île voisine recevait
  bien la ressource. Désormais `outFlow` lit `game.transitFlow[courante+'_'+other]` (flux réel du
  dernier tick), comme le flux entrant → les deux îles affichent le MÊME débit. (2) **capacité batterie
  512 → 2048** (`accumulateur.capacity`, ×2^upgrade conservé). (3) **production élec. en SORTIE** :
  la ligne « Sortie » de l'InfoPanel affiche `⚡ X kW` pour les bâtiments qui produisent de l'énergie
  (`outputs.energie_kw` : éolienne/charbon/diesel) — avant, non affichée (la ligne Élec. ne montrait
  que la conso, soit 0). (4) **noms + sprites dans la fiche bâtiment (tap)** : `recipeChips` affiche
  désormais `<sprite> <nom court> <débit>` (le nom RES_SHORT était masqué quand un sprite existait).
  Changement
  10.67 : **fix batterie — charge/décharge bornées par le débit du câble**. Vérif du fonctionnement
  de l'accumulateur (boucle énergie de `tickIsland`) : charge rendement 0.8 / décharge 1.0, regroupé
  par composante électrique (`poolAccs`/`ufRoot`), capacité ×2^upgrade (`accCapacity`), `stored`
  sérialisé (`pl.s`) — **OK**. **Bug trouvé & corrigé** : sur un réseau **saturé** (demande > débit),
  la batterie se déchargeait (ou chargeait) **au-delà** de ce que le câble peut transporter → l'énergie
  non livrée était gaspillée (ex. batterie pleine 512 vidée mais seulement 256 livrés). Désormais
  `surplus`/`need` sont bornés par `cap` : `surplus = min(prod,cap)−netDem`, `need = min(netDem,cap)−prod`
  (inchangé si câble illimité ou non saturé). La réserve restante est conservée. Changement
  10.66 : **tuiles « brise » terrain animées sur TOUTES les îles** (le pack `Archipel_sprites_COMPLET`
  livré contenait enfin les sheets manquantes). Avant : seuls `tile_i1_land/water` animaient. Désormais
  **16 sheets** `tile_i{1..5}_{land,water,coast}_breeze` + `tile_i3_petrole_breeze` inlinées dans
  `__ANIM_DATA__` (per-key), et `TILE_ANIM_BY_KEY` mappe chaque clé de tuile statique
  (`tile_iN_land/water/coast`, `tile_i3_oil`→`tile_i3_petrole_breeze`) → sa sheet (fps 3, phase par tuile
  `(t+r+c)%4`). Frame 0 = sprite statique EXACT (vérifié byte-à-byte → pas de saut) ; diff frames ~100-244 px
  = ondulation/brise bien visible (≠ tuiles portuaires subtiles). `tile_i4_coast_breeze` est statique (4
  frames identiques dans le pack, inoffensif). Les anims portuaires « balise » du 10.65 sont conservées.
  Changement
  10.65 : **animations tuiles portuaires rendues VISIBLES (balise pulsante)** — les anims `tile_port_mer`
  (grue) et `tile_port_terre` (panneau) du 10.58 *jouaient* déjà (via `drawPortExtras`/`drawBuilding`→
  `drawAnimFrame`) mais variaient de seulement 5–20 px sur 1024 → imperceptibles. Les 2 sheets
  `__ANIM_DATA__` sont régénérées : une **balise lumineuse pulsante** (off→0.6→1.0→0.6) est composée
  sur les frames 1-3 de la sheet existante (frame 0 = sprite statique EXACT inchangé → pas de saut),
  rouge pour la mer (haut-centre), cyan/vert pour la terre. Diff frames ~17-37 px = clignotement bien
  visible. NB : le système d'anim (`_animPlayed`→redraw, `spriteOnReady`) et le câblage étaient corrects ;
  seul le contenu des sheets était trop subtil. (Les tuiles « brise » terrain restent île 1 uniquement.)
  Changement
  10.64 : (1) **flux ENTRANT dans le panneau Port** — en plus du flux sortant (`outFlow`, shippableQty),
  chaque liaison montre désormais le flux **reçu** de l'autre île : `inFlow` lit
  `game.transitFlow[other+'_'+courante]` (débit réel /s du dernier tick), affiché « sprite <ressource>
  <débit>/s ← Île N » en vert (`.pp-cargo-in`). Corrige « je ne vois pas le transit de l'île 3 » (île
  importatrice : rien en sortie, tout en entrée). (2) **sprites ressources dans la fiche bâtiment au
  TAP** (`InfoPanel`) — les lignes Entrées/Sortie/Réel passent de `formatRecipe` (texte) à `recipeChips`
  (sprite + débit, comme `BuildingDetailModal`). (3) **rééquilibrage élec.** : `centrale_charbon`
  energie_kw 32→64 (×2), `centrale_diesel` 128→512 (×4), `centrale_nucleaire` `NUC_POWER` 2048→8192 (×4).
  Changement
  10.63 : **import/export des sauvegardes en fichier `.txt`** (en plus du presse-papier/texte du
  10.34). (1) **Côté jeu** (`SlotPanel`) : modale Export → bouton **« ⤓ Télécharger .txt »**
  (`saveTextFile` : pont natif `ArchipelNative.saveText` si présent, sinon **Blob + `<a download>`**
  navigateur) ; fichier nommé `archipel-<slot>.txt`. Modale Import → bouton **« 📂 Charger un fichier
  .txt »** (`<input type=file>` masqué + `FileReader` → remplit la zone de texte, puis « Importer »).
  Const `NATIVE_SAVE`. (2) **Coquille Android** (`MainActivity.java`) : `WebChromeClient.onShowFileChooser`
  (ouvre `ACTION_OPEN_DOCUMENT` text/json, renvoie l'URI via `onActivityResult` → le `<input file>`)
  ; `WebBridge.saveText(filename, content)` → `writeDownload` écrit dans **Téléchargements** (MediaStore
  Android 10+, sinon dossier app) + Toast. Aucune permission ajoutée (scoped storage). ⚠️ Les users
  Android doivent **mettre à jour l'APK** pour le sélecteur/écriture natifs (sinon repli Blob inopérant
  dans l'ancienne WebView). Changement
  10.62 : **débit /s du transit + champs cible/réserve en notation « port »**. (1) **Flux /s** :
  `transferLink` mémorise le débit réel par ressource du dernier tick dans `game.transitFlow[src_'_'dest]`
  (transitoire, non sauvegardé) ; le panneau Port affiche désormais `… <ressource> <débit>/s → Île N`
  (via `fmtPort`) au lieu du nom seul. (2) **Champs numériques** : `NumField` affiche la valeur en
  notation « port » (`fmtPort` : 1e5, 1,5e6…) hors édition (valeur brute en édition ; `parseNum`
  accepte scientifique + virgule) → la **cible** et la **réserve** du commerce, et le seuil d'alerte,
  ne s'affichent plus en entier. Changement
  10.61 : **sprite de la ressource transférée dans le panneau Port** — la liste « flux sortant »
  d'une liaison (`PortPanel`, `outFlow`) affichait le sprite générique du cargo (`cargoSprite`) +
  code court → on ne voyait pas QUEL item transitait. Désormais chaque ligne montre le **sprite de la
  ressource** (`itemSpriteKey`/`SPRITE_DATA`, classe CSS `.pp-cargo-ico` 16 px, repli `cargoSprite`
  si pas de sprite) + nom court + « → Île N ». Changement
  10.60 : **priorité de flux ÉTENDUE à l'électricité** — la priorité de flux par bâtiment (`fluxPri`
  haute/normale/basse, fiche bâtiment) arbitrait déjà les **intrants ET sorties matières** sur réseau
  route/tuyau saturé (via `addTier(routeIn/routeOut)` + `tierFactor`). Désormais elle arbitre AUSSI
  **l'électricité** : `cutToFit` est enveloppé par un découpage en paliers (`cutToFitMode` interne) →
  on sert d'abord TOUS les « haute », puis « normale », puis « basse » ; le mode énergie
  (priority/fair/proportional) ne départage qu'au sein d'un même palier. La fiche bâtiment montre le
  sélecteur de priorité pour tout bâtiment qui utilise un réseau **OU tire du courant** (`usesNet ||
  drawsPower`), libellé « Priorité de flux (intrants & élec.) ». Changement
  10.59 : **réseau « traversant » câble + tuyau (pont via bâtiment)** — nouvelle règle : un bâtiment
  qui se raccorde au CÂBLE (élec.) ou au TUYAU laisse le réseau le **traverser**. Implémenté dans
  `rebuildNetworks` : après le flood-fill, une passe union-find (`mUF`/`mFind`) **fusionne tous les
  réseaux d'un même porteur (`wire`/`pipe`) adjacents à l'emprise** d'un bâtiment qui
  `buildingConnectsCarrier(id, carrier)` (le port exclu — gestion diesel spéciale). Les réseaux non
  racine sont fusionnés dans la racine (pool `addInto`, level = max, connected = OR) et `t.networkId`/
  `t.netIds` réécrits. **Interdit pour la route** (jamais fusionnée). Ex. éolienne–câble–four arc–
  câble–aciérie → un seul réseau câble (l'éolienne alimente four + aciérie) ; idem tuyau (pool de
  ressources partagé). Tout l'aval (boucle énergie, pools tuyau, débit, NetworkPanel, sprites)
  utilise automatiquement les réseaux fusionnés (l'union-find électrique du tick devient
  redondante mais reste, inoffensive). Changement
  10.58 : **animations tuiles portuaires + format « port » des gros nombres**. (1) **Vérif sprites
  tuiles/anim** (pack `Archipel_sprites_COMPLET`) : les 28 tuiles statiques + 55 anims bâtiments + 2
  tuiles brise étaient déjà intégrées ; **manquaient les 2 anims de tuiles portuaires** → ajoutées :
  `tile_port_mer` (grue, fps 4) et `tile_port_terre` (panneau de contrôle, fps 3) inlinées dans
  `__ANIM_DATA__` + entrées `ANIM_META`. `tile_port_terre` s'anime automatiquement (le bâtiment
  `port` passe par `drawBuilding`→`drawAnimFrame`, clé statique = `tile_port_terre`) ; `tile_port_mer`
  câblé dans `drawPortExtras` (`drawAnimFrame` avec repli `drawSprite`). Frame 0 = sprite statique
  exact (vérifié byte-à-byte → pas de saut). (2) **Format « port »** : `fmtInt(n, thresh)` accepte un
  seuil ; nouveau `fmtPort = n => fmtInt(n, 1e5)` (notation scientifique dès 1e5 : `1,5e5`, `1e6`…),
  appliqué aux stocks du port → inventaire HUD + panneau Port (stock « en stock » et coûts
  d'amélioration du transit). Changement
  10.57 : **HUD compact + sprites ressources dans la fiche bâtiment**. (1) **HUD** : les libellés
  texte « Énergie » et « Batterie » des pastilles haut-droite sont retirés (on garde l'icône `⚡` /
  `🔋` + la valeur) → gain de place. (2) **`BuildingDetailModal`** (appui long) : les lignes Coût /
  Entrées / Sorties affichent désormais **sprite + quantité** par ressource (helper `resChips(obj,
  perSec)` → `itemSpriteKey`/`SPRITE_DATA`, repli code court si sprite absent) au lieu du texte
  `formatCost`/`formatRecipe`. CSS `.dr-res` (chip inline icône+nombre). Changement
  10.56 : **rééquilibrage coûts** — `accumulateur` (batterie) coût de base **÷2** (silicium_raffine
  150→75, processeur 10→5, cable 150→75) ; les **3 fours à arc** (`four_arc_acier`/`_cable`/`_piece`)
  coût de base **÷4** (acier 300→75, beton_arme/cable 150→38, cable/piece_meca 100→25, processeur
  40→10). NB : ces bâtiments sont t3, donc `TIER_COST_MULT` ×8 s'applique toujours par-dessus (le
  ratio ÷2 / ÷4 est respecté). Changement
  10.55 : **affichage déficit retravaillé + options + inventaire compact**. (1) **Bâtiments en
  déficit** : le sprite garde sa **pleine taille** (fini la réduction du 10.52/10.53) ; une **petite
  icône d'état** (`drawDeficitIcon`, sprite `etat_*` via `statusSpriteKey`) s'affiche en **haut à
  droite** (cause : `input` si intrants < élec., sinon `power`) ; **plus aucun assombrissement** de
  la case à l'arrêt (suppression des remplissages `rgba(13,13,26,.55)` + teinte rouge ; les anciens
  `drawLevelBadge`/`drawStatusBadge`/`drawBuildingStateOverlay` sont remplacés par `drawInfoBadges`
  + `drawDeficitIcon`). (2) **Badges bas-gauche** (`drawInfoBadges`) : `[efficacité %][niveau]` —
  le **% d'efficacité** (= `eff`, 0 à l'arrêt) à GAUCHE du **numéro de niveau** réduit (0.22 au lieu
  de 0.30) ; le % n'apparaît qu'en déficit. `drawBuilding` reçoit `inFac`+`pwrAvg` en plus de
  `regime`. Sprite **figé** (pas d'anim) en déficit comme à l'arrêt. (3) **3 nouvelles options**
  (persistées dans `uiPrefs`) : `showDeficitPct`, `showDeficitSprite` (à côté de `showLevels`) —
  toggles `toggleDeficitPct`/`toggleDeficitSprite`, lus par les fonctions de dessin. (4)
  **Inventaire HUD** : sprite + chiffre **uniquement** (plus de nom ; repli code court si sprite
  absent) ; affiche **chaque ressource débloquée même à 0** (`unlockedResourceSet(game)` passé en
  prop `unlockedRes` à `Hud`, + ressources en stock) ; les ressources **jamais débloquées** ne
  s'affichent plus. Changement
  10.54 : **nouveaux sprites UI (batterie + cadres menu)** — depuis `Archipel_sprites_COMPLET` :
  (1) **icône batterie UI** `ui_batterie` inlinée → le HUD utilise `uiIcon('batterie', "🔋")` (PNG
  si présent, sinon emoji 🔋) pour la pastille « Batterie ». (2) **cadres « menu » 9-slice** :
  sprites `ui_cadre_rivets_9slice` / `ui_cadre_sobre_9slice` (17×17) exposés en variables CSS
  `--cadre-rivets`/`--cadre-sobre` (`:root`), appliqués via `border-image: var(--cadre-rivets) 8 / 8px
  stretch` sur `.research-panel` et `.slot-panel` (encadrement orné des modales principales).
  Changement
  10.53 : **production bridée au prorata du déficit (régime réel)** — un bâtiment qui manque
  d'intrants ET/OU d'élec. ne s'arrête plus brutalement : il tourne à `regime = min(inFac, pwrF)`
  (`inFac` = fraction d'intrants dispo calculée dans la boucle bâtiment au lieu du tout-ou-rien ;
  `pwrF` = `bld.pwrAvg` du tick précédent). `regime` est stocké sur `bld` et bride conso+production
  au dépôt (`actives` : `fc *= rg`), la prod élec. (`energyOut*regime`), et la réservation `workPort`.
  Arrêt franc seulement si `inFac<=0` (PAS sur déficit élec. → le bâtiment reste dans
  `energyConsumers` pour que pwrAvg converge, sinon oscillation). Affichage : icône réduite par
  `regime` (drawBuilding reçoit `bld.regime`), et l'InfoPanel montre régime % + cause (intrants /
  élec. / réseau) + ligne « Réel » = sorties×régime. Changement
  10.52 : **déficit élec. = icône réduite + figée** — `drawBuilding` reçoit `pwrAvg` (duty-cycle lissé) ;
  si `powerDef = pwrAvg<0.995 && !disconnected`, on dessine le sprite STATIQUE réduit (`s=0.55+0.4·pwrAvg`,
  centré, plus petit = plus de déficit), SANS badge de panne ni animation → fin du clignotement actif/
  arrêt. La prod n'est PAS bridée par le déficit (le dépôt `actives` ligne ~3985 précède la coupure élec.
  ligne ~4323) → un bâtiment en déficit produit à plein régime, jamais à l'arrêt (vérifié). Changement
  10.51 : **tuiles « brise » animées (île 1 terre + eau)** — 2 sheets `tile_i1_land/water_breeze`
  ajoutées à `__ANIM_DATA__`. `TILE_ANIM_BY_KEY` mappe la clé de tuile statique (`tile_i1_land`,
  `tile_i1_water`) → sheet. `drawTileAnim(ctx,terrKey,x,y,w,r,c)` dessine la frame `(t+r+c)%4`
  (phase PAR TUILE = bourrasque diagonale, `t` au fps du manifest ~3), appelée AVANT `drawSprite`
  dans la boucle terrain. Bords 2px identiques → tiling sans couture. Autres îles/terrains = statiques
  (pas de sheet). Changement précédent
  10.50 : (1) **animations complétées (55 bâtiments)**
  10.50 : (1) **animations complétées (55 bâtiments)** — `__ANIM_DATA__`/`ANIM_META` regénérés depuis
  `Archipel_sprites_COMPLET` : ajout des 20 sheets éolienne+mines manquantes (tous les bâtiments animés
  maintenant ; tuiles « brise » fournies mais PAS encore intégrées). (2) **fix notif recherche livraison**
  — un nœud `mode:'delivery'` (accès/« réparation » d'île, ex. node 2 Accès Île 2 : `reqs:[]` + `delivery`
  10000+10000) passait `condition_ok` immédiatement et notifiait même sans les ressources. Nouveau
  `deliveryReady(game,def)` (port de l'île courante couvre `def.delivery`) gate `hasPendingResearch`
  (pastille) ET `evaluateTechTree` (toast `researchReady`, via flag `node.notified` réarmable). Init de
  `node.notified` en newGame (false) et loadSave (true si confirmé ou prêt-ET-livrable). Changement
  10.49 : **animations de bâtiments (spritesheets 4 frames)** — 35 sheets du pack `animations_pack_complet`
  inlinées dans `window.__ANIM_DATA__` (clé = `cle` du manifest), méta dans `ANIM_META`. `ANIM_BY_SK`
  réindexe par CLÉ STATIQUE (`[cle,'bat_'+cle,cle+'_v1']` présente dans SPRITE_DATA) → frame 0 = sprite
  statique exact. `drawAnimFrame(ctx,sk,…)` dessine la frame `floor(now/1000*fps)%frames` du sheet
  (sub-rect `i*fw`), appelée dans `drawBuilding` **seulement si le bâtiment est actif** (à l'arrêt =
  sprite statique). `_animPlayed` force le redraw continu tant qu'une anim est visible. centrale_nucleaire
  = 256×64 (frames 64×64, 2×2). Cargo = statique (pas d'anim, retirée du pack). ⚠️ Les 20 sheets
  éolienne+mines (« préexistantes ») ne sont PAS dans le zip fourni → ces bâtiments restent statiques
  (fallback) tant qu'on n'a pas leurs sheets. Changement
  10.48 : **transit lissé + anti aller-retour + cargo découplé**. (1) **Transfert continu** : `tickShips`
  ne déplace plus de gros lots — `transferLink(src,dest)` transfère chaque seconde, dans les DEUX sens,
  `lot_de_base/TRANSIT_DIV` (TRANSIT_DIV=60 = ancien aller-retour) → débit identique mais flux « 1/s ».
  (2) **Anti aller-retour** : `shippableQty` renvoie 0 si la destination peut nous fournir la ressource
  (`rawShippable` inverse > 0) → on n'exporte pas ce qu'on importe. (3) **Cargo décoratif** : le sprite
  `bateau_cargo` fait un aller-retour VISUEL de 20 s (`SHIP_TRAVEL_TICKS=10`, découplé du transfert,
  cale toujours vide ; `BOAT_PROX_THRESH=0` → visible tout le trajet). (4) **PortPanel** : `🚢` remplacé
  par `cargoSprite()` (img), la cale remplacée par le **flux sortant continu** (`shippableQty`), libellé
  « ↔ transit continu ». `loadCargo` devient du code mort (laissé). Changement
  10.47 : **bâtiments « récompense » (V2/V3) exemptés du surcoût de palier** — le multiplicateur
  `TIER_COST_MULT` (T1×2/T2×4/T3×8) n'est plus appliqué aux versions améliorées (suffixe `_v2`/`_v3`,
  via `isRewardBuilding = /_v\d+$/`, ex. mine_fer_v2/v3, four_fer_v2, carriere_v2/v3) : elles
  reprennent leur coût de base. ⚠️ NB : *tous* les bâtiments à palier (t1-t3) sont débloqués par la
  recherche, donc exempter « tout ce qui vient de la recherche » annulerait le surcoût entièrement —
  on cible donc les seuls suffixes `_vN`. Les autres bâtiments du palier gardent le surcoût. Changement
  10.46 : **recherches terminées reléguées en fin de liste + compactées** — `techNodesOrdered(game)`
  trie les nœuds de `ResearchPanel` pour mettre les `confirmed` à la FIN (ordre d'id sinon) ; les
  nœuds confirmés s'affichent en compact (CSS `.rp-node.st-confirmed` : padding réduit, `.rp-st`
  masqué, `.rp-name` plus petit) et n'affichent plus la ligne « → débloque … ». Changement
  10.45 : **détail des ressources transitant sur un réseau**
  10.45 : **détail des ressources transitant sur un réseau** — `tickIsland` accumule `netFlow[nid] =
  {prod, cons}` par ressource (déposée / puisée /s) pour route+tuyau (+pipePort) en parallèle de
  `netDemand`, stocké dans `game.netFlow[isl]`. Le `NetworkPanel` (clic sur une route/tuyau) affiche
  deux nouvelles lignes **Production /s** et **Consommation /s** (liste `res` triée, via `fmtRate` +
  `RES_SHORT`), « Transit : aucun » si vide. Le câble garde son bilan élec. en kW. Changement
  10.44 : **fiche détaillée d'un bâtiment par appui long**
  10.44 : **fiche détaillée d'un bâtiment par appui long** — dans l'onglet Bâtiment / Réseau, un
  appui long (~450 ms) sur une tuile ouvre `BuildingDetailModal` (coût, taille, terrain, entrées/
  sorties /s, conso/prod élec., stockage, exclusivité). `ToolButton` gère le long-press (pointer
  events + timer, annulé si glissement >10 px pour ne pas gêner le défilement ; appui court =
  sélection inchangée, `lp.current.fired` supprime le clic). `Toolbar` détient l'état `detailId`.
  Changement 10.43 : **stocks de démarrage différenciés par île** — `ISLAND_KICKSTART` n'utilise plus la même
  base pour les îles 2-5. Nouveaux objets `ISLAND_KICKSTART_3/4/5` (île 2 inchangée). Île 3 = base
  + lingot_cuivre 500 / acier 250 / cable 250 ; île 4 = ces ressources toutes à 1000 + beton_arme /
  polymere / piece_meca 500 ; île 5 = celles de l'île 4 toutes à 1000. Déposé au port à la 1re
  ouverture de l'île (n'affecte pas une île déjà débloquée dans une save existante). Changement
  10.42 : **mise à jour in-app (quasi-auto, 1 tap)** — la coquille Android expose un pont JS
  `window.ArchipelNative` (`MainActivity.WebBridge`, `addJavascriptInterface`) avec `update(url)` :
  télécharge l'APK (`HttpURLConnection`, suivi de redirection GitHub→CDN, cache `update.apk`) puis
  l'installe via **`PackageInstaller`** (session MODE_FULL_INSTALL → `BroadcastReceiver` sur
  `INSTALL_ACTION` qui lance l'écran de confirmation système). Pas de FileProvider (projet
  `useAndroidX=false`). Permission **`REQUEST_INSTALL_PACKAGES`** + vérif `canRequestPackageInstalls()`
  (sinon ouverture de `ACTION_MANAGE_UNKNOWN_APP_SOURCES`). Avancement renvoyé au JS via
  `window.__archipelUpdate(state, pct)`. Côté jeu : const `NATIVE_UPDATER`, l'`OptionsModal` (état
  `available`) affiche un bouton **« Mettre à jour maintenant »** (progress %) si le pont existe,
  sinon le lien de téléchargement classique. ⚠️ Android interdit l'install 100 % silencieuse pour une
  app sideloadée : 1 tap « Installer » reste requis. Changement
  10.41 : **persistance des modifications de terrain** — la réparation (accidenté→terre/côte) et le
  remblai (eau→côte) n'étaient PAS sauvegardés : `buildIslandTiles` reconstruit les tuiles depuis la
  def à chaque chargement, et seuls les *compteurs* `repairsCount`/`extensionsCount` étaient persistés
  → terrain réparé perdu au reload/MAJ. Fix : `buildIslandTiles` mémorise `tiles[r][c].baseTerrain` ;
  `serialize` émet un tableau `terrainMods` (tuiles où `terrain !== baseTerrain`) par île ; `loadSave`
  réapplique ces overrides (avec `padShift`) AVANT de poser les bâtiments. `SAVE_VERSION` **12 → 13**
  (ajouté à la whitelist de `slotLoad`). Changement
  10.40 : **fix stub réseau « deux câbles »** — quand plusieurs porteurs (câble + tuyau) se
  raccordent à un bâtiment via la MÊME jonction (même direction), leurs stubs se superposaient et
  le câble (dessiné en dernier) masquait le tuyau (ex. centrale diesel → « deux câbles »). Le draw
  des stubs (vers ligne 10090) calcule désormais `stubMask`/`juncMask` par porteur, puis attribue
  chaque **direction de jonction contestée** à un seul porteur (le moins raccordé ailleurs, via
  `popc`) sans jamais vider le masque d'un porteur → câble et tuyau s'affichent distinctement.
  Changement 10.39 : **électricité « puiser sur tous les câbles adjacents »** — un bâtiment (producteur /
  consommateur / accu) qui touche plusieurs réseaux câble les fusionne électriquement (union-find
  `wireUF`/`ufRoot`/`ufUnion` + `wireRepFor` dans `tickIsland`). La boucle énergie itère désormais par
  **composante fusionnée** (pools `poolProd`/`poolCons`/`poolAccs`), avec **débit de composante =
  somme des débits des câbles** qui la composent. Corrige la fragmentation en mini-réseaux d'une
  tuile (des bâtiments restaient sous-alimentés alors que l'île produisait assez). Le `NetworkPanel`
  câble et `game.wireInfo[isl]` affichent le bilan de la composante (reporté sur chaque câble). Seuls
  les bâtiments vraiment isolés (1 seul câble sous-dimensionné, ou aucun câble) restent coupés.
  Changements 10.38 : (1) **diesel stocké au port** — nouveau concept `PORT_PIPE_RES`/`isPortPipe` : une ressource
  peut être transportée par TUYAU mais stockée au PORT (au lieu du pool tuyau). Le diesel est routé
  via un nouveau bucket `inByType.pipePort`/`outByType.pipePort` (dépôt/conso au port, demande reportée
  sur le réseau tuyau pour la saturation). `tradeAvail/Draw/Deposit` excluent `PORT_PIPE_RES` du
  chemin pool→port. Migration au chargement : le diesel resté dans les pools tuyau est rapatrié au
  port. (2) **fix sprite de connexion bâtiment↔jonction** — le stub réseau sous un bâtiment lisait
  le niveau via `nt.networkId` (null pour une jonction) → toujours V1 ; on lit désormais
  `nt.netIds[carrier]`. Changements
  10.37 : (1) **fix régime 0 %** — le panneau bâtiment montrait 0 % pour un consommateur élec. à
  demande variable (`sigmoid`/`randomP`, dont `b.power===0`) coupé à l'instant T ; on teste désormais
  `nominalPower(bld) > 0` (et non `b.power>0`) pour activer le régime lissé `pwrAvg` → affiche le vrai
  duty-cycle (~50-100 %). (2) **bannière de saturation câble** — la saturation électrique d'un câble
  (demande élec. > débit) est maintenant ajoutée à `game.netSaturated[isl]` dans la boucle énergie
  (la liste était figée avant, route/tuyau seulement) → la bannière haut-droite « ⚠ Câble VN saturé »
  s'affiche comme pour la route.
- Changement 10.36 : **détecteur de mise à jour automatique** — au lancement, un `useEffect` fetch `version.json`
  (`VERSION_URL`) et compare `build > GAME_BUILD` ; si une version plus récente existe → state App
  `updateInfo` → **pastille `notif-dot` + classe `has-update` sur le bouton Options** (Hud), **toast**
  « Mise à jour disponible », et l'`OptionsModal` s'ouvre déjà sur l'état `available` (lien de
  téléchargement APK via `updateInfo.apk`). Échoue en silence hors-ligne. La vérif manuelle
  (`checkUpdate`) reste dispo. Changement
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
