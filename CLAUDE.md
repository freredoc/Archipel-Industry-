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
- **État au dernier passage : `GAME_BUILD = 372`, `GAME_VERSION = 'Alpha 14.89'`, `SAVE_VERSION = 31`.**
  Changement 14.89 (brief `BRIEFlotuiport1`, **lot « UI & Port » — 7 chantiers, 27 blocs**) :
  **le PORT apparaît EN RUINE tant que la liaison maritime suivante n'est pas payée**, « Demander au
  port » disparaît avec une seule île, **3 en-têtes de fiche passent du carré de couleur au SPRITE**,
  le panneau Énergie **masque les lignes à 0 kW**, les Options et le Port cessent d'écraser leur
  contenu, et le Collisionneur peut **relancer sa séquence tout seul**. `SAVE_VERSION` INCHANGÉ ;
  seul champ ajouté `collider.autoLaunch` (**additif**, absent = `false`). Base EXACTE (371 / 14.88 /
  3 289 188 o) ; **29/29 ancres à `count == 1` AVANT application**, **round-trip 29/29 VERBATIM**,
  `node --check` 7/7 sur les éditions PUBLIQUE **et** DEV, **delta +13 994 o** (dont +13 259 pour les
  27 blocs, le reste = `GAME_NOTES` et la note « cargo »).
  (1) **§1 — PORT EN RUINE, état ENTIÈREMENT DÉRIVÉ, aucun drapeau.** ⚠ Le `LISEZ-MOI` du pack d'art
  conclut qu'il faut « ajouter un drapeau de partie sur le modèle de `elevatorRepaired`, plus sa
  sérialisation » : **ne pas le faire.** Le constat est juste (aucune mécanique de port cassé
  n'existait) mais l'arbre techno est DÉJÀ persisté → nouveau `portCasse(game, isl)` =
  `ISLAND_ACCESS_NODE[isl+1]` existe ET n'est pas confirmé. **Aucun champ, aucune migration, aucun
  état à maintenir cohérent** (un port ne peut pas se désynchroniser de sa liaison, il n'a pas d'état
  propre), et les saves existantes affichent le bon état dès le 1ᵉʳ rendu. **L'île 6 tombe juste SANS
  cas particulier** (`ISLAND_ACCESS_NODE[7]` undefined → jamais cassée).
  ⚠ **PUREMENT VISUEL** : le port reste raccordable route/tuyau et son stock utilisable — **le
  tutoriel de l'île 1 exige de relier des bâtiments au port, le casser fonctionnellement le rendrait
  INFAISABLE** (non-régression mesurée : tuto actif + port cassé → mine reliée, `tutCountConnected`
  = 1, progression jusqu'à « Tuto 3/10 »).
  ⚠ **La substitution ne peut PAS passer par `BLD_SPRITE_OVERRIDE`** : `buildingSpriteKey` est une
  fonction GLOBALE PURE sans accès à `game` (depuis 14.32 l'override accepte une liste de candidats,
  mais le choix s'y fait sur la PRÉSENCE du sprite, jamais sur un état de partie). Elle vit dans
  `drawBuilding` (tuile terre) et `drawPortExtras` (tuile mer), qui ont `gameRef`.
  ⚠ **`tile_port_mer` est ANIMÉ, `tile_port_mer_casse` est STATIQUE** → appeler `drawSprite`
  DIRECTEMENT pour l'état cassé, sans passer par `drawAnimFrame`. Idem `tile_port_terre` : un espion
  `drawImage` voit `ANIM:tile_port_terre` et **pas** la clé statique.
  ⚠ **CARGO — LA PRÉMISSE DU BRIEF EST FAUSSE DÈS L'ÎLE 2, MESURÉ, NON CORRIGÉ.** Le brief pose
  « port cassé ⇒ aucune liaison active ⇒ aucun cargo ». Vrai pour l'**île 1 seulement** (c'est le
  MÊME nœud 2 qui casse son port et active la liaison 1-2). Dès l'île 2 : port cassé tant que le
  nœud 8 n'est pas confirmé, alors que la liaison 1-2 est déjà active → **un cargo s'anime devant un
  port en ruine, et c'est CORRECT** (l'île 2 commerce réellement). Masquer le cargo retirerait une
  information VRAIE et contredirait « l'état cassé est purement visuel ». Constat consigné en
  commentaire dans `drawPortExtras`, **à arbitrer**.
  ⚠ **`tile_port_mer_casse` est une RECOLORATION, pas une destruction** (le brief a raison contre le
  `LISEZ-MOI`) : mesuré au pixel sur les sprites re-décodés du fichier patché, **271 px opaques avant
  ET après, silhouette identique sur 1024/1024**, luminance −33 %. La grue est debout. La tuile terre,
  elle, perd ses accents vifs (1024 px, luminance 130,7 → 82,7, couleurs 30 → 17). **La ruine se lit
  au zoom par défaut, mais c'est la PAIRE qui fait l'effet** — côté mer seul, ce serait ambigu. Une
  vraie destruction côté mer serait un **remplacement d'art, pas un correctif de code**.
  ⚠ `tile_port_mer_casse` porte sa transparence en **PALETTE** (colortype 3 + chunk `tRNS`) :
  encoder les OCTETS du PNG, jamais ré-encoder l'image — sinon la grue apparaîtrait sur un carré
  opaque au-dessus de l'eau (vérifié après insertion : `tRNS` présent, SHA-256 conformes).
  (2) **§2 — « Demander au port » masqué avec une seule île** (`!islandUnlocked[2]`), neutralisé **en
  amont au calcul de `askNeeded`** (point de décision unique, vérifié lu nulle part ailleurs dans les
  2 composants). ⚠ Les 2 blocs InfoPanel/UpgradePanel sont **identiques sur 389 caractères sauf la
  classe** (`ip-` vs `up-`).
  (3) **§3 — 3 en-têtes passent au SPRITE** (UpgradePanel, Élévateur, élément logique), sur le motif
  de référence de l'InfoPanel. ⚠ **Toujours tester `SPRITE_DATA[_sk]` avant substitution** :
  `buildingSpriteKey` peut rendre une clé ABSENTE (branches `return ov` de `BLD_SPRITE_OVERRIDE`) →
  `src=undefined`, image cassée au lieu du repli propre. ⚠ **Le test « SPRITES_ENABLED = false » n'est
  PAS exécutable au runtime** (`const` de module, non réassignable, cf. 14.86) → contrôle sur la
  SOURCE. ⚠ **La fiche « Élévateur cassé » n'est PAS le site patché** : le panneau au carré orange
  n'est rendu que si `elevatorRepaired` est VRAI ; non réparé, le tap tombe sur le panneau de
  RÉPARATION, qui affiche déjà `tile_i6_elevateur_casse` depuis 14.08 → **la branche `casse` du patch
  est actuellement inatteignable** (conservée, défensive).
  (4) **§4 — lignes à 0 kW masquées dans le panneau Énergie.** ⚠ **LE FILTRE VIT DANS LE RENDU,
  EXCLUSIVEMENT** : `energyConsumerList` a un SECOND appelant (`moveEnergyPriority`) — y filtrer
  retirerait les bâtiments de la GESTION de priorité, pas seulement de l'affichage. ⚠ **`i` portait
  TROIS sémantiques** (rang, borne Monter, borne Descendre) : livré **rang = index d'ORIGINE**,
  **bornes = position dans la liste VISIBLE**. ⚠ **ÉCART ASSUMÉ** : le brief demande l'index
  d'origine « pour les bornes ET le rang », ce qui **contredit son propre test 4.3** (la dernière
  ligne visible n'aurait pas son Descendre grisé). ⚠ `moveEnergyPriority` **saute les entrées
  masquées** (échange avec le prochain consommateur non nul) — sinon « Descendre » échangerait avec
  un invisible et **rien ne bougerait à l'écran** (reproduit sur la base 371). Seuil **0,0005 kW**,
  pas `=== 0`. ⚠ **Aucun bâtiment n'a une puissance nominale nulle au niveau 0** (69 consommateurs
  vérifiés) : le cas se construit en enveloppant `energyConsumerList` via `window`.
  (5) **§5 — Options.** `.opt-lbl` portait `min-width:0` SANS `flex` ni plancher et était le SEUL
  élément rétrécissable de la ligne (tous les contrôles ont `flex-shrink:0`) → il absorbait tout le
  débordement d'un `<select>` large. **Le coupable est la largeur du CONTRÔLE, pas la description.**
  Livré : plancher `flex:1 1 auto; min-width:148px`, `<select>` borné à 132 px, `flex-wrap` sur la
  ligne. Contre-épreuve base 371 @360 px : « Grands nombres » titre sur **2 lignes**, description sur
  **12 lignes pour 0 px de large** (un mot par ligne), `<select>` à 268 px qui **déborde** à 320 px.
  (6) **§6 — colonnes du Port.** `1fr` vaut `minmax(auto,1fr)` dont le minimum est la largeur du
  CONTENU : chaque ligne étant une grille INDÉPENDANTE, chacune débordait différemment. Livré
  `minmax(0,1fr)` + `.pp-c-res{min-width:0;overflow-wrap:anywhere}`, et **dernière colonne 44 → 52 px**
  (mesuré : 16 px d'icône + 6 de padding + 2 de bordure = 24 px/bouton, + 2 de gap = **50 px** pour
  deux). Contre-épreuve base 371 sur 40 lignes : écart de colonnes **73 px @360** / 17 px @420 et
  **26 / 80 boutons hors panneau @360** (dont la 2ᵉ flèche de « mot.quantique ») → **0 px, 0/80** après.
  (7) **§7 — démarrage automatique du Collisionneur** (`colliderAutoAvailable` : dernier palier ET
  dernier nœud puzzle confirmé ET seuil `COLLIDER_GOALS` atteint — composé depuis l'existant, rien en
  dur). ⚠ **Passe par `launchCollider(silent)`, JAMAIS par `co.launched = true`** : `launchCollider`
  contient l'unique point de décision `colliderLaunchBlock`, partagé avec l'état grisé du bouton ; le
  contourner ferait démarrer la machine sans réseau logique ou palier bloqué (test dédié : réseau
  logique retiré → `block === 'logic'`, pas de démarrage). ⚠ **Lancement SILENCIEUX** (ni toast ni
  SFX) : après une pause ou une pénalité `launched` retombe à faux et le cycle se rejoue — mesuré
  **5 cycles → 0 notification**. ⚠ **Le crochet est dans la BOUCLE DE TICK** (juste après `onTick`),
  pas dans la frame rAF : `co.state` vient d'être recalculé par `processCollider`.
  **Validé** : `node --check` (**7 blocs, 7 OK**, éditions publique ET dev) + Chromium **46 assertions
  du lot, 0 KO, rejouées 2 fois sans flottement**, dont **3 assertions de non-régression du TUTORIEL**
  et **4 contre-épreuves sur la BASE 371** (chantiers 4, 5 et 6 y échouent → les tests sont
  falsifiables). Boot des 2 éditions : canvas **100 %**, horloge qui avance, **0 `tickError`,
  0 erreur console**.
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) **`window.__ui()` n'expose PAS
  `centerOnTile`/`setInfo`/`setPortOpen`** (seulement `tryPlace`, `canPlace`, `switchIsland`,
  `askPortFor`, `setLogicConfig`, `buyResearch`) → un `if (ui.centerOnTile) …` passe en SILENCE et
  l'on croit centrer la caméra sans rien faire ; recaler `cam.x`/`cam.y` à la main (inverse de
  `pointerToTile`) ; (b) **l'INVENTAIRE ouvert se pose EN SUPERPOSITION** sur le haut du canvas
  (`elementFromPoint` → `inventory open`) : le replier avant tout tap ; (c) **tout panneau resté
  ouvert avale le tap suivant** — le panneau Options d'une section précédente recouvrait le bouton
  PORT : isoler les sections dans des fichiers de test distincts ; (d) **`addInitScript` REJOUE à
  chaque navigation, RELOAD COMPRIS** → un `localStorage.clear()` nu fait repartir tout test de
  rechargement sur une partie NEUVE (garder derrière un drapeau `sessionStorage`) ; (e) une save
  forgée est **écrasée par le flush `pagehide`** → la réinjecter dans un `addInitScript` ;
  (f) **`useGhostGuard` avale le 1ᵉʳ clic** d'un panneau : amorcer par un `pointerdown` dispatché
  DANS le panneau puis réessayer ; (g) **la pastille ⚡ est MASQUÉE tant que l'île 2 est verrouillée**
  (`islandTradeUnlocked`) → panneau Énergie inatteignable sans `islandUnlocked[2]` ; (h) **l'EnergyPanel
  porte les classes `research-panel port-panel`**, comme le PortPanel ; (i) le panneau d'amélioration
  n'apparaît que si **`g.ui.fastUpgrade` est FAUX** ; (j) **`co.powered` non booléen ⇒ tick BLANC**
  (`processCollider` sort sans toucher `state`/`timer`) → poser un **getter** `powered → undefined`
  rend l'état `'ready'` déterministe, sans monter tout un réseau électrique + hélium 3.
  ⚠ **HORS PÉRIMÈTRE, non touché** : le **lot « Gisements par exclusivité d'île »** (6 overlays
  `overlay_resource_iN` livrés dans le même pack — le `LISEZ-MOI` impose « un brief par lot, jamais
  groupés », et seul le brief du lot Port a été fourni) ; la conversion recherche → livraison
  (29 nœuds) ; les halos d'antenne disparus après déficit ; `BLD_SPRITE_OVERRIDE`,
  `buildingSpriteKey`, `energyConsumerList`, `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 371`, `GAME_VERSION = 'Alpha 14.88'`, `SAVE_VERSION = 31`.**
  Changement 14.88 (brief `BRIEFTUTOLOT7retours`, **LOT 7 — 8 sites A→H, quatre retours indépendants**) :
  **le toast d'échec nomme le bâtiment**, **le stock de départ passe de 20 à 30**, **l'étape 10 rend la
  main** (plus de halo, les astuces d'alerte se débloquent) et **le guide désigne le bouton Réparer**.
  `SAVE_VERSION` INCHANGÉ, aucun champ persisté. Base EXACTE (370 / 14.87) ; **8/8 ancres uniques,
  16/16 hachages conformes AVANT application**, `node --check` 7/7, **delta +2 878 o EXACT**.
  (1) **§A — le toast disait « ❌ Extension V1 : manque 20 pierre » pour un BÂTIMENT.** Le message
  d'échec de `tryPlace` était écrit pour les réseaux et servait à tout : poser une mine sans pierre
  annonçait une « extension » que le joueur n'avait pas demandée. Le libellé se scinde désormais sur
  `b.kind === 'infra'` : les réseaux gardent « Extension V\<niveau\> », les bâtiments prennent leur
  **nom** (`b.name || id`). Mesuré : « ❌ Mine Fer V1 : manque 20 pierre » ; contre-épreuve sur une
  route → « ❌ Extension V2 : manque 10 ciment » (inchangé).
  (2) **§B/C/D — stock de départ 20 → 30 pierre et minerai de fer.** ⚠ **Il est écrit à TROIS
  endroits** (`INITIAL_RESOURCES`, l'objet de partie neuve, et le repli `if (!g.port[1])` du
  chargement) : n'en changer qu'un fait dépendre le stock du **chemin d'entrée** dans la partie
  (partie neuve vs slot rechargé). Les 3 sont patchés et un commentaire d'avertissement est posé
  au-dessus d'`INITIAL_RESOURCES`. Mesuré : **3 sites à 30, 0 stock resté à 20**.
  ⚠ **PIÈGE DE MESURE (m'a donné 3 faux KO)** : le coût des fours **paraît changé** parce que
  `TIER_COST_MULT` (t1 ×2) s'applique **au chargement du module** sur `BUILDINGS[id].cost` → la
  source dit 20+20, le runtime rend **40+40**. Le contrôle du §3 (« coûts de bâtiment inchangés »)
  doit donc comparer à `20 × mult`, jamais au littéral. Les 4 littéraux `pierre: 20`/`minerai_fer: 20`
  restants sont bien des **coûts** (mine, carrière, four fer, four cuivre) et le 4ᵉ `pierre: 30` est
  le coût **préexistant** de la cimenterie (`pierre: 30, minerai_fer: 10`) — pas un stock oublié.
  (3) **§E/§F — l'étape 10 rend la main.** Elle était la dernière étape à plan… sans plan : elle
  gardait un halo (`targets`) qui ne désignait plus rien d'utile, et `checkTips` **coupait toutes les
  astuces contextuelles** tant que le tutoriel était actif. Désormais `targets: []` (aucun halo, DOM
  ni canvas) et `checkTips` laisse passer **`reseau_sature` puis `deficit`** dès l'étape 10 — ce sont
  exactement les deux pannes que la montée en cadence provoque. **La bannière et les 2 compteurs sont
  CONSERVÉS** (l'objectif reste lisible). Mesuré : 0 cible, 0 halo, bannière « Tuto 10/10 » intacte.
  ⚠ **Le `return` a dû être DÉPLACÉ dans le `if`** : laissé à sa place, il sortait de `checkTips`
  avant le nouveau bloc et l'étape 10 restait muette (le patch aurait été inerte).
  ⚠ **CONTRE-ÉPREUVE EXÉCUTÉE** : à l'étape 6, la saturation est bien **muette** ET **non marquée
  vue** → l'astuce reste disponible pour l'étape 10. `port` et `energie` restent différées (leurs
  conditions sont vraies dès le boot ; les rendre passantes ferait une rafale au franchissement).
  ⚠ **FAUSSE ALERTE QUE J'AI LEVÉE PUIS INSTRUITE** : j'ai d'abord cru que `reseau_sature` portait
  `silent: true` (mon grep captait le `silent` de l'astuce SUIVANTE) — donc que le lot 7 contredisait
  le lot 3C. Vérification exhaustive : les 7 astuces muettes sont `tut_copier`, `transport`,
  `priorite`, `batiment_deconnecte`, `liaisons_port`, `reserves`, `copier` — **ni `reseau_sature`,
  ni `deficit`**. Aucun conflit ; l'assertion est conservée comme **contrôle de cohérence permanent**
  (si un lot futur rend l'une des deux muette, le §E devient inerte en silence).
  (4) **§G/§H — le guide désigne enfin le bouton Réparer.** Le tutoriel fini, le joueur devait
  « réparer » l'île 2 sans que rien ne pointe le bouton : `go_recherche` l'envoyait à la Recherche,
  où le nœud d'accès est en `condition_ok` mais **se livre depuis le bouton Réparer du HUD**. Nouvel
  objectif **`go_reparer`**, inséré **AVANT** `go_recherche` (ordre = correctifs récurrents d'abord,
  règle K1 du lot Guide), avec `data-tut="repair"` posé sur `.inv-repair-ico`. Son `when` recalcule
  le nœud depuis `ISLAND_ACCESS_NODE` et teste `g.techTree.nodes[nd - 1].status === 'condition_ok'`.
  ⚠ **`nodes[id - 1]`, pas `nodes[id]`** : c'est la convention d'indexation du jeu ; un `nodes[nd]`
  aurait désigné le nœud suivant et l'objectif se serait armé au mauvais moment.
  Mesuré : muet tant que le nœud n'est pas `condition_ok`, bandeau + halo sur le bouton Réparer
  ensuite, **muet à nouveau** une fois l'île 2 ouverte.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **38 assertions du lot, 0 KO, rejouées
  2 fois sans flottement** — **+ 326 assertions en NON-RÉGRESSION des lots 3A (63), 3B (50), 3C (38),
  3D (42), 3E (12), 4 (46), 5 (35) et 6 (40) rejouées sur ce build, 0 KO, 0 assertion à adapter**
  (le lot 7 ne renverse aucune assertion antérieure : les lots 4-6 testent les étapes **à plan**,
  or l'étape 10 n'en a jamais eu). Page blanche DEV et PUBLIQUE console vide.
  ⚠ **PIÈGES DE HARNAIS** : (a) **le serveur de test doit être lancé depuis le DÉPÔT**, pas depuis le
  scratchpad — sinon la page rend 404 et l'on croit à une page blanche (`__gameRef` absent, splash
  jamais retiré) ; (b) un `boot()` qui remplit le port avant lecture **efface le stock de départ** :
  capturer la valeur AVANT de fournir quoi que ce soit ; (c) à l'étape 10 le popup capté par défaut
  est **`tut_debit`** (le `why` de l'étape, servi plus tôt dans `checkTips`) — purger avant d'asserter
  sur `reseau_sature`, sinon on mesure le mauvais popup.
  ⚠ **Taille : 3 286 158 → 3 289 188 o** (+2 878 les 8 blocs EXACT, +152 le nouveau `GAME_NOTES`).
  ⚠ **HORS PÉRIMÈTRE, non touché** : le plan de pose et le verrou (lot 4), la palette et les fantômes
  du halo (lot 6), les astuces `port`/`energie` (toujours différées pendant le tutoriel), les autres
  objectifs du guide, `tabAllowed`, le défaut de `roadReachesPortFootprint`, `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 370`, `GAME_VERSION = 'Alpha 14.87'`, `SAVE_VERSION = 31`.**
  Changement 14.87 (brief `BRIEFTUTOLOT6halo`, **LOT 6 — 5 sites A→E, tous dans le dessin du halo**) :
  **le halo de tuiles devient PERMANENT**, il reçoit **une palette dédiée** et **le sprite du bâtiment
  attendu en fantôme**. `SAVE_VERSION` INCHANGÉ, aucun champ persisté. Base EXACTE (369 / 14.86) ;
  **5/5 ancres uniques, 10/10 hachages conformes AVANT application**, `node --check` 7/7,
  **delta +2 252 o EXACT**.
  (1) **Le défaut fermé** : `drawTutorialHalo` lisait `targets[idx]`, la cible COURANTE de la
  séquence. Tant que l'index pointait `.tab-build`, il n'y avait pas de `tiles` et la fonction
  SORTAIT — **le joueur ne voyait où poser qu'APRÈS avoir ouvert le menu**, l'inverse du service
  rendu. Le halo prend désormais la cible `tiles` de l'étape quel que soit l'index. Mesuré à
  l'étape 1, **menu fermé et aucun outil armé** : la tuile de la mine est déjà haloée (7 rects
  `#FF6E40`) alors que la cible courante est bien `.tab-build`.
  ⚠ **Le halo DOM (boutons) n'est PAS touché** : il continue de suivre la séquence. Les deux halos
  coexistent — vérifié, `.tut-halo` présent sur le bouton pendant que les tuiles sont désignées.
  (2) **Palette `TUT_HALO_COLOR`, distincte de `BUILDINGS[id].color`** : les couleurs du jeu sont des
  tons de terre (brun `#8B4513`, beige `#D2B48C`, gris `#777`) qui se noient dans le vert de la carte.
  Les 6 nouvelles tranchent (`#FF6E40` fer, `#FFD54F` carrière, `#26C6DA` route, `#AB47BC` charbon,
  `#FF1744` four, `#ECEFF1` cimenterie). **Elle ne sert QU'au halo** et ne remplace la palette du jeu
  nulle part. Mesuré : 6/6 types couverts, 6 couleurs distinctes.
  (3) **Fantôme = sprite propre du bâtiment, à opacité FIXE 0,62** (seuls le fond et le cadre
  respirent — un sprite qui pulse clignote). Mesuré : 32 fantômes, **un seul alpha, 0,62**.
  ⚠ **La clé est RÉSOLUE par `buildingSpriteKey(bid, 0)`, jamais écrite en dur** — elle dépend de la
  table d'alias `BLD_SPRITE_OVERRIDE`, et un art renommé changerait une clé figée sans que rien ne le
  signale. **Précision factuelle mesurée** : `buildingSpriteKey('mine_fer', 3)` rend `mine_fer_v1` —
  **l'art ne dépend PAS de l'`upgrade`**, les paliers sont des bâtiments DISTINCTS (`mine_fer_v4` →
  `mine_fer_v4`). Le risque du test 4 du brief est donc réel mais porte sur l'id du palier, pas sur
  l'argument de niveau. Vérifié : le halo dessine `mine_fer_v1`/`carriere_v1`/`four_fer_v1`, jamais
  `mine_fer_v4`.
  (4) **Les réseaux n'ont PAS de fantôme** (`isNet` → `bid` nul) : leur art dépend des connexions
  voisines (`route_v1_02_E`, `route_v1_05_NS`…) et une tuile isolée afficherait un tronçon orienté au
  hasard. Ils restent des cases cyan, ce qui distingue d'emblée « ici un bâtiment » de « ici passe la
  route ». Mesuré : **0 fantôme de route**, les seules clés fantômes sont des bâtiments.
  (5) **Repli conservé** : `drawSprite` rend `false` si l'art manque ou n'est pas décodé → le halo
  retombe sur le libellé (FER, PIER, CHAR, FOUR, CIM). Mesuré en neutralisant `drawSprite` : les
  5 libellés reviennent et la case colorée reste.
  (6) **`@upgradable` / `@disconnected` / `@saturated` inchangés** : `col`/`lab`/`bid` facultatifs →
  halo vert d'origine, **0 libellé, 0 fantôme** (mesuré : 14 rects `rgba(76,175,80,…)`).
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **40 assertions du lot, 0 KO, rejouées
  2 fois sans flottement** — dont **60 fps sur 2 s avec les 14 tuiles haloées de l'étape 6**, et
  l'invariant du lot 4 (halo == verrou) toujours à **0 écart sur 49 152 combinaisons** avec en plus
  une pose réelle sur une tuile haloée **menu fermé** — **+ 286 assertions en NON-RÉGRESSION des lots
  3A (63), 3B (50), 3C (38), 3D (42), 3E (12), 4 (46) et 5 (35)**, page blanche DEV et PUBLIQUE
  console vide.
  ⚠ **3 assertions du lot 5 mises à jour, RENVERSEMENTS VOULUS** : la palette du halo passe de
  `BUILDINGS[id].color` à `TUT_HALO_COLOR`, et le libellé cède la place au fantôme (il ne subsiste
  qu'en repli).
  ⚠ **PIÈGES DE HARNAIS** : (a) un espion `drawImage` capte **TOUT le rendu**, y compris les routes
  réellement posées — ne juger les fantômes que sur `globalAlpha === 0.62`, sinon on croit à un
  fantôme de route ; (b) **`SPRITES_ENABLED` est une `const` de module**, non réassignable : pour
  tester le repli, neutraliser **`drawSprite`** (déclaration de fonction d'un script classique, donc
  propriété de `window` — même famille qu'`activeEnergyAlerts` en 14.67) ; (c) `@upgradable` ne
  désigne que des mines/carrières **de niveau 1** : sans bâtiment posé, le halo est vide et le test
  mesure du néant.
  ⚠ **Taille : 3 283 945 → 3 286 158 o** (+2 252 les 5 blocs EXACT, −39 le nouveau `GAME_NOTES`).
  ⚠ **HORS PÉRIMÈTRE, non touché** : le halo DOM, le plan de pose et le verrou (lot 4), les couleurs
  de `BUILDINGS`, `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 369`, `GAME_VERSION = 'Alpha 14.86'`, `SAVE_VERSION = 31`.**
  Changement 14.86 (brief `BRIEFTUTOLOT5etape4halo`, **LOT 5 — 9 sites A→E, dont 4 tables i18n**) :
  **l'étape 4 n'enseigne plus Copier** (on pose au menu Bâtiment, comme partout ailleurs), **elle
  perd son popup**, et **le halo prend la couleur et le nom du bâtiment attendu**. `SAVE_VERSION`
  INCHANGÉ. Base EXACTE (368 / 14.85) ; **9/9 ancres uniques, 18/18 hachages conformes AVANT
  application**, `node --check` 7/7, **delta +1 765 o EXACT**.
  (1) **Copier sort de la trame** : plus aucune étape 1..9 ne déclare `.tab-copy` (mesuré sur les 9),
  et `tabAllowed` n'en garde qu'**UNE occurrence** dans tout le fichier. Le bouton revient à l'étape
  10 par la règle du lot 4 (`si >= 9 → return true`) — **aucun code ajouté pour ça**.
  (2) ⚠ **ÉCART NÉCESSAIRE, MESURÉ — sans lui le §1 du brief était FAUX.** Le brief annonce que
  `tut_copier` « reste consultable dans l'Aide ». Or l'étape 4 perd son `why` : **plus rien n'ouvre
  cette astuce, donc plus rien ne la marque `tipsSeen`** — et l'Aide filtre sur
  `tipsSeen[id] || when(game)` (13.41) avec un `when: () => false`. Mesuré sur le patch verbatim :
  **`visibleDansAide: false`, à l'étape 10 COMME tutoriel terminé — l'astuce disparaissait purement
  et simplement.** Correctif d'une ligne : `when: g => !(g.tutorial && g.tutorial.active) ||
  g.tutorial.step >= 9` → elle se débloque exactement quand le bouton Copier est rendu au joueur.
  `silent: true` la garde **muette** (jamais ouverte d'elle-même). Vérifié dans le VRAI panneau Aide :
  absente avant l'étape 10, présente à l'étape 10 et dépliable en texte intégral (349 caractères).
  (3) **Les 4 tables i18n sont indispensables et le brief a raison de les traiter ensemble** :
  `applyToData` réécrit les objectifs **PAR INDEX**, donc ne changer que le français aurait laissé
  l'anglais, l'espagnol et l'allemand annoncer le bouton Copier **sans qu'aucune erreur n'apparaisse**.
  Mesuré au runtime dans les 4 langues : **0 objectif mentionnant Copier/Copy/Copiar/Kopieren**.
  (4) **Halo différencié** : `mark(r, c, col, lab)` — `col`/`lab` **facultatifs**, donc `@upgradable`,
  `@disconnected` et `@saturated` gardent le halo vert d'origine (vérifié : 14 rects
  `rgba(76,175,80,…)`, 0 libellé). Les 6 types du plan ont bien 6 couleurs distinctes et leurs
  libellés (`FER`, `PIER`, `CHAR`, `FOUR`, `CIM`) ; **la route a un `label` vide** → case colorée
  sans texte. Sous 18 px de tuile, le libellé disparaît et la case reste (mesuré à 14 px : 0 libellé).
  (5) **L'invariant du lot 4 tient** : le changement d'apparence n'a pas touché la source — halo et
  verrou restent identiques sur **49 152 combinaisons, 0 écart**.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **37 assertions du lot, 0 KO, rejouées
  2 fois sans flottement** — dont les **9 poses du plan de l'étape 4 par taps réels SANS Copier**
  (9/9 acceptées, étape validée) **+ 251 assertions en NON-RÉGRESSION des lots 3A (63), 3B (50),
  3C (38), 3D (42), 3E (12) et 4 (46)**, page blanche DEV et PUBLIQUE console vide.
  ⚠ **3 assertions renversées VOLONTAIREMENT** (lots 3D et 4) : à l'étape 4 le halo initial passe de
  `.tab-copy` à `.tab-build`, Copier y est désormais FERMÉ, et le test « pose par Copier soumise au
  verrou » n'a plus d'objet — **il ne reste aucune étape à plan où Copier soit posable**.
  ⚠ **PIÈGES DE HARNAIS** : (a) **armer un outil ne fait PAS descendre le halo sur les tuiles** à
  l'étape 4 — son `when` teste `!ui.buildOpen`, il faut **OUVRIR le menu Bâtiment** ; sinon on
  capture un halo vide et on croit le patch mort ; (b) franchir plusieurs étapes d'un coup laisse la
  **file d'astuces** du lot 3A pleine : purger la file avant d'affirmer qu'« aucun popup ne s'ouvre » ;
  (c) le backdrop d'un popup **avale le clic sur le bouton Aide** — purger avant d'ouvrir l'Aide.
  ⚠ **Taille : 3 281 591 → 3 283 945 o** (+1 765 les 9 blocs EXACT, +603 le correctif `when` et son
  commentaire, −14 le nouveau `GAME_NOTES`).
  ⚠ **POINT DE DESIGN LAISSÉ OUVERT (§2 du brief, à trancher par Ethan)** : sans le geste Copier, les
  étapes 4 et 6 énoncent des objectifs voisins — « 2 mines et 2 carrières de plus, reliées » (1+1 → 3+3),
  puis la même chose « et au niveau 2 » (3+3 → 5+5). Les textes ont été différenciés ; **la fusion des
  deux étapes n'est PAS tranchée ici**.
  ⚠ **HORS PÉRIMÈTRE, non touché** : le plan de pose et le verrou (lot 4), `tabAllowed`, les autres
  étapes, le défaut de `roadReachesPortFootprint`, `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 368`, `GAME_VERSION = 'Alpha 14.85'`, `SAVE_VERSION = 31`.**
  Changement 14.85 (brief `BRIEFTUTOLOT4forcage`, **LOT 4 — 5 sites A→E**) : **le tutoriel impose
  désormais OÙ poser.** Un plan de 32 poses **relevé sur une partie réelle** (journal du build 367),
  pas calculé : le halo désigne les tuiles exactes et le verrou n'accepte qu'elles. `SAVE_VERSION`
  INCHANGÉ, aucun champ persisté. Base EXACTE (367 / 14.84) ; **5/5 ancres uniques, 10/10 hachages
  conformes AVANT application**, `node --check` 7/7, **delta +4 445 o EXACT**.
  (1) ⚠ **LE PLAN A ÉTÉ RE-VÉRIFIÉ CONTRE LE TERRAIN AVANT D'ÊTRE APPLIQUÉ, et ce contrôle est
  OBLIGATOIRE à chaque retouche du plan** : Démolir étant désormais fermé (§4), un plan qui ne
  suffirait pas à valider son étape **enfermerait le joueur sans recours**. Rejoué sur la base 367 :
  **32/32 poses acceptées par `tryPlace`**, 6/6 mines sur `resource`, **19/19 routes en un seul blob
  touchant le port**, 0 bâtiment orphelin, 0 empreinte ≠ 1×1, et surtout **8/8 étapes à plan se
  valident avec les SEULES poses disponibles jusque-là** (le `done` évalué étape par étape).
  (2) **Halo et verrou partagent leur source** (`tutPlanRemaining`) : le joueur ne peut jamais se
  voir refuser une tuile qui clignote, ni réussir sur une tuile qui ne clignote pas. **Invariant
  vérifié EXHAUSTIVEMENT : 49 152 combinaisons (8 étapes × toute la grille × 6 bâtiments), 0 écart.**
  C'est le test central du lot ; s'il se rompt, le guidage ment.
  (3) ⚠ **D4 EST RENVERSÉ — Démolir se ferme, et ce renversement est CONDITIONNEL.** Démolir
  n'existait que comme porte de sortie d'une pose ratée ; le verrou refusant la pose **avant qu'elle
  existe**, il n'y a plus rien à défaire. **Rouvrir la pose libre sans rendre Démolir enfermerait le
  joueur sur sa première erreur** — les deux vont ensemble. Mesuré sur les 10 étapes : Démolir fermé
  **exactement** sur les 8 étapes à plan, ouvert aux étapes 5 et 10.
  (4) **L'ordre n'est pas imposé** (décision d'Ethan) : le joueur pose sur *une* des tuiles restantes
  de son étape, pas dans la séquence relevée. Mesuré à l'étape 4 : route → bâtiment → route, 0 refus.
  (5) **L'ancien halo `link` disparaît** : il dessinait une polyligne décorative sans désigner aucune
  tuile posable, et cherchait **toujours `mine_fer`** — y compris aux étapes de la carrière, du
  charbon, du four et de la cimenterie (six étapes sur sept montraient le mauvais bâtiment).
  (6) **CONTRÔLE AJOUTÉ HORS BRIEF, et il fallait le faire** : **l'étape 4 impose Copier** — si la
  capture armait un chemin de pose distinct, le forçage n'aurait pas couvert cette étape. Vérifié par
  le vrai geste (clic réel sur Copier → tap sur une mine → outil `mine_fer`) : la pose retombe bien
  dans la branche `place` verrouillée — **hors plan REFUSÉE avec le toast, sur le plan acceptée**.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **49 assertions, 0 KO, rejouées 2 fois
  sans flottement** + **7 assertions d'audit du plan** — dont le **tutoriel déroulé de bout en bout,
  étapes 1 à 9, par 32 TAPS CANVAS RÉELS sur les seules tuiles du halo : 32 posées, 0 refusée**
  (`tryPlace` direct contournerait le verrou, qui vit dans `onPointerUp` — un test de forçage DOIT
  passer par de vrais taps) **+ 205 assertions en NON-RÉGRESSION des lots 3A (63), 3B (50), 3C (38),
  3D (42) et 3E (12)**, page blanche DEV et PUBLIQUE console vide.
  ⚠ **2 assertions du lot 3D mises à jour, ce sont des RENVERSEMENTS VOULUS, pas des régressions** :
  les cibles `tiles: '<id>'` deviennent `tiles: '@plan'`, et l'étape 10 rend **toute la barre** (elle
  n'ouvrait que Améliorer + Démolir).
  ⚠ **PIÈGES DE HARNAIS (4 causes distinctes, 21 faux KO à la 1ʳᵉ passe — à ne pas redécouvrir)** :
  (a) **l'INVENTAIRE est déplié d'office depuis le lot 3B et se pose EN SUPERPOSITION** sur le haut
  du canvas → tout tap y atterrit (`span.iv`) : le replier ; (b) **purger les popups AVANT de toucher
  au HUD** — leur `.research-backdrop` avale le clic, donc replier l'inventaire d'abord ne marche
  pas ; (c) **`el.tagName === 'CANVAS'` NE SUFFIT PAS** pour vérifier qu'un point vise la carte : un
  popup d'astuce ouvert pose son propre `<canvas class="tip-illu-canvas">` par-dessus, et le tap part
  dedans **sans le moindre toast** — comparer à `document.querySelector('.stage canvas')` ;
  (d) **`clampPan` recale la caméra** : après centrage, REFAIRE le trajet inverse de `pointerToTile`
  et vérifier que le point désigne bien la tuile visée, sinon réessayer. Et (e) un espion de toasts
  **ne doit PAS dédoublonner** sur le dernier message : deux refus successifs du verrou émettent le
  même texte, et le second passerait pour muet.
  ⚠ **Bruit console PRÉEXISTANT à filtrer, pas une régression** : le survol en mode Copier émet
  `Archipel frame error: … reading 'kind'` (`canPlace('__copy')`, `BUILDINGS['__copy']` undefined,
  documenté en 14.46).
  ⚠ **Taille : 3 277 083 → 3 281 591 o** (+4 445 les 5 blocs EXACT, +63 le nouveau `GAME_NOTES`).
  ⚠ **HORS PÉRIMÈTRE, non touché** : les `goal`/`done`/`progress`/`why`/`afterToast` (donc **aucune
  table i18n** — l'alignement PAR INDEX d'`applyToData` est préservé), le défaut de
  `roadReachesPortFootprint` (un bâtiment posé contre le port n'est jamais reconnu comme relié),
  `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 367`, `GAME_VERSION = 'Alpha 14.84'`, `SAVE_VERSION = 31`.**
  Changement 14.84 (brief `BRIEFTUTOLOT3Ehaloetape6`, **LOT 3E — 1 SEUL site, 2 `when` allongés**) :
  **à l'étape 6, le halo désignait le RÉSEAU pendant toute la phase d'amélioration.** `SAVE_VERSION`
  INCHANGÉ. Base EXACTE (366 / 14.83) ; **1/1 ancre unique, 2/2 hachages conformes AVANT application**,
  `node --check` 7/7, **delta +556 o EXACT**.
  (1) ⚠ **C'EST L'ÉCART QUE J'AVAIS SIGNALÉ AU LOT 3D EN LE JUGEANT « SANS CONSÉQUENCE » — ET CE
  JUGEMENT ÉTAIT FAUX.** J'avais mesuré que le halo repointait Réseau quand l'outil route est
  **désarmé**, et conclu que le cas était marginal. Ce que je n'avais pas mesuré : **cliquer sur
  Améliorer désarme précisément la route**. Le halo désignait donc le Réseau **au moment exact où le
  joueur fait ce qu'on lui demande**, pendant toute la phase d'amélioration. Contre-épreuve exécutée
  sur la base 366 : outil route → `.tab-upg` ✅ · outil **Améliorer → `.tab-net`** ✗ · aucun outil →
  `.tab-net` ✗. **Leçon : un halo qui pointe l'onglet que le joueur vient de quitter n'est pas un
  détail cosmétique ; « l'étape se franchit quand même » ne suffit pas à classer un défaut de guidage.**
  (2) **Le correctif** : les 2 `when` réseau reçoivent `&& (tutCountConnected(g,1,'mine_fer') < 5 ||
  tutCountConnected(g,1,'carriere') < 5)` — ils testaient `tutCount` (**POSÉES**) là où il fallait
  `tutCountConnected` (**RELIÉES**). Comme `.tab-net` précède `.tab-upg` et que la machine à cibles
  s'arrête sur la PREMIÈRE condition vraie, il suffisait que ces `when` restent vrais pour que le
  Réseau reprenne la main. Mesuré après patch : les **3 états d'outil donnent `.tab-upg`**.
  (3) ⚠ **LA CONTRE-ÉPREUVE EST LE TEST QUI COMPTE** (test 2 du brief) : avec **3 mines reliées sur 5**
  et l'outil Améliorer armé, le halo doit **encore** désigner `.tab-net`. Sans elle, une condition trop
  large passerait le test 1 en éteignant DÉFINITIVEMENT la cible réseau — et l'étape redeviendrait
  infaisable, soit exactement le défaut que le lot 3D venait de fermer. Mesuré : `.tab-net`. ✅
  (4) **Le contrôle permanent du 3D est rejoué et reste vert** : ce lot restreint un `when`, jamais un
  `sel` — or `tabAllowed` ne lit QUE les `sel`. **0 étape infaisable** (constaté, pas supposé).
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **12 assertions du lot, 0 KO, rejouées
  2 fois sans flottement** (les 3 états d'outil, la contre-épreuve 3+3, 6/10 puis 8/10 améliorés →
  halo qui reste sur Améliorer, franchissement de bout en bout → bannière « Tuto 7/10 ») **+ 193
  assertions en NON-RÉGRESSION des lots 3A (63), 3B (50), 3C (38) et 3D (42) rejouées sur ce build**,
  page blanche DEV et PUBLIQUE console vide.
  ⚠ **PIÈGE DE MESURE (m'a donné un état de test inexploitable)** : à l'étape 6, **« 6/10 améliorés »
  est l'état de DÉPART**, pas une progression — les 3 mines + 3 carrières de l'étape 5 sont déjà au
  Nv.2. Une sonde qui « monte 6 bâtiments de plus » monte en fait les 4 restants et **franchit
  l'étape** au lieu de mesurer une progression intermédiaire. Pour un état distinct, monter **2** de
  plus (8/10).
  ⚠ **Taille : 3 276 520 → 3 277 083 o** (+556 le bloc EXACT, +7 le nouveau `GAME_NOTES`).
  ⚠ **HORS PÉRIMÈTRE, non touché** : les 9 autres étapes, `tabAllowed`, les `goal`/`done`/`progress`/
  `why` (donc aucune table i18n à toucher — l'alignement PAR INDEX d'`applyToData` est préservé),
  **Démolir toujours actif** (décision D4) et le défaut de `roadReachesPortFootprint`, `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 366`, `GAME_VERSION = 'Alpha 14.83'`, `SAVE_VERSION = 31`.**
  Changement 14.83 (brief `BRIEFTUTOLOT3Dreseau`, **LOT 3D — 1 SEUL site : `TUTORIAL_STEPS`**) :
  **le menu Réseau était VERROUILLÉ sur les six étapes qui exigent de relier un bâtiment au port**
  (3, 4, 6, 7, 8, 9). `SAVE_VERSION` INCHANGÉ, aucun champ ajouté. Base EXACTE (365 / 14.82) ;
  **1/1 ancre unique, 2/2 hachages conformes AVANT application**, `node --check` 7/7 du premier coup,
  **delta +2 451 o EXACT**.
  (1) **Le défaut** : `tabAllowed(key)` construit la liste des onglets ouverts à partir des SEULS
  `sel` des `targets` de l'étape courante (`sels.includes('.tab-net')`). Aucune de ces six étapes ne
  déclarait `.tab-net`. Le tutoriel restait jouable **tant que le joueur ne changeait pas d'outil**
  (l'outil route reste armé après l'étape 2) ; dès qu'il sélectionne un bâtiment, il perd la route et
  **ne peut plus la reprendre**. Constaté en jeu à l'étape 6 : quatre bâtiments posés à 0 %, compteur
  figé à 6/10, **aucune sortie**.
  ⚠ **POURQUOI 151 ASSERTIONS NE L'ONT PAS VU — la leçon de méthode du lot.** Tous les tests des lots
  3A/3B/3C vérifiaient qu'une condition de sortie, **une fois atteinte**, valide bien l'étape. Aucun ne
  vérifiait qu'elle est **ATTEIGNABLE avec les seuls boutons autorisés**. Ce sont deux propriétés
  distinctes, et seule la première était testée. **Contrôle désormais permanent (test 1) : pour chaque
  étape, croiser la liste des onglets ouverts par `tabAllowed` avec les prédicats appelés par son
  `done` — toute étape dont le `done` appelle `tutConnected`/`tutCountConnected` DOIT ouvrir
  `.tab-net`, toute étape qui appelle `tutAllUpgraded`/`tutFlow*` DOIT ouvrir `.tab-upg`.** Mesuré :
  **0 infaisable après patch**, et le même contrôle rejoué **sur la base 365 en rend 6** (étapes 3, 4,
  6, 7, 8, 9) — le test est donc falsifiable, ce n'est pas une assertion creuse.
  (2) **Les cibles de pose s'ÉTEIGNENT une fois la pose faite** (`when: g => tutCount(g, 1, 'x') < 1`)
  — sans quoi le halo resterait sur les tuiles de construction et **ne désignerait jamais le réseau**
  (la machine à cibles s'arrête sur la PREMIÈRE cible dont `when` est vrai). Séquence mesurée :
  étape 3 Bâtiment → tuiles → **Réseau** → liaison ; étape 4 Copier → **Réseau** → liaison ;
  étape 6 Bâtiment → **Réseau** → Améliorer ; étapes 7/8/9 Bâtiment → tuiles → **Réseau** → liaison.
  (3) ⚠ **ÉCART MESURÉ, LIVRÉ VERBATIM — FERMÉ DEPUIS PAR LE LOT 3E (14.84), ET MON « non bloquant »
  ÉTAIT FAUX : cliquer sur Améliorer DÉSARME la route, donc le halo pointait le Réseau pendant toute
  la phase d'amélioration. Description d'origine conservée ci-dessous.** Le `when` de `.tab-net` teste
  `tutCount` (POSÉES) et non `tutCountConnected` (RELIÉES)** : une fois les 10 reliées, si le joueur
  **désarme** l'outil route, le halo **repointe Réseau** alors qu'il n'y a plus rien à relier (mesuré
  aux 3 états : outil route armé → `.tab-upg` ✅ ; désarmé → `.tab-net` ; menu Réseau ouvert →
  `[data-tut="road"]`). **Sans conséquence** : Améliorer reste déverrouillé (`locked:false`) et l'étape
  se franchit (mesuré). Le cas n'existe QU'à l'étape 6 — partout ailleurs la liaison franchit l'étape
  immédiatement, il n'y a pas de « suite » après elle. Correctif d'une ligne si on veut le fermer :
  ajouter `&& (tutCountConnected(g,1,'mine_fer') < 5 || tutCountConnected(g,1,'carriere') < 5)` aux
  `when` de `.tab-net` et `[data-tut="road"]` de l'étape 6. **2 assertions posées pour le rendre
  falsifiable** si un lot ultérieur le corrige.
  (4) **Contrôle sémantique fort, à rejouer à chaque retouche de `TUTORIAL_STEPS`** : les 10 `goal`,
  `why`, `done`, `progress`, `afterToast` et `reveal` sont **IDENTIQUES à l'ancre, dans le même ordre**
  — seuls les `targets` changent. C'est ce qui garantit qu'`I18N.applyToData` (qui réécrit `s.goal`
  **PAR INDEX** depuis les tables LOCALES, piège du lot 2) reste aligné : **aucune table i18n à
  toucher**. Vérifié au runtime dans les 4 langues (10 étapes, 0 incomplète, goals justes en fr/en).
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **42 assertions, 0 KO, rejouées 2 fois
  sans flottement** (+ le test 1 et sa contre-épreuve) — atteignabilité des 10 étapes, parcours RÉEL
  de l'étape 6 qui bloquait (2 mines + 2 carrières posées **sans Copier**, clic RÉEL sur Réseau puis
  sur la route, liaison, compteur 6/10 → 10/10 vert, étape franchie), étape 4 reliée par le menu
  Réseau après Copier, halo qui suit la progression, verrous d'onglets (étape 3 Copier fermé, étape 4
  Bâtiment fermé, étape 6 Copier fermé, étape 10 **seul Améliorer** + Démolir), page blanche DEV et
  PUBLIQUE console vide **+ 151 assertions en NON-RÉGRESSION des lots 3A (63), 3B (50) et 3C (38)
  rejouées sur ce build**.
  ⚠ **PIÈGE DE HARNAIS (coûteux, 1 faux KO)** : **forcer `game.tutorial.step` ne suffit pas à changer
  l'étape vue par l'UI.** La `Toolbar` lit le **state REACT** `tutorialStep`, que `checkTutorial` ne
  met à jour que s'il a posé `changed` — or forcer le step sans rien d'autre ne déclenche ni
  franchissement ni changement de `targetIdx`. Résultat : `g.tutorial.step === 9` mais des onglets
  d'étape 1 (Bâtiment ouvert, Améliorer verrouillé), qui se lit comme un défaut du patch. Remède :
  **désaligner `targetIdx`** (une valeur qui ne sera pas recalculée, ex. 7) pour forcer la synchro,
  puis contrôler la bannière (« Tuto 10/10 ») avant d'asserter quoi que ce soit sur les onglets.
  ⚠ **Taille : 3 274 102 → 3 276 520 o** (+2 451 le bloc EXACT, −33 le nouveau `GAME_NOTES` plus court).
  ⚠ **HORS PÉRIMÈTRE (§3), non touché** : **Démolir reste actif** (décision D4) — Ethan veut le retirer,
  mais seulement une fois la tuile de pose verrouillée ET le défaut de `roadReachesPortFootprint` traité
  (**un bâtiment posé contre le port n'est jamais reconnu comme relié** ; sans Démolir le joueur n'aurait
  aucune issue). **Lot séparé, rien anticipé ici.** Également non touchés : `tabAllowed` lui-même, les
  `goal`/`done`/`progress`/`why`, les tables i18n, `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 365`, `GAME_VERSION = 'Alpha 14.82'`, `SAVE_VERSION = 31`.**
  Changement 14.82 (brief `BRIEFTUTOLOT3Castuces`, **LOT 3C — 6 sites A→F**) : **le catalogue
  d'astuces est resserré** — 39 `short` de plus (popup = UNE phrase, l'Aide garde le texte intégral),
  9 textes longs réécrits, 2 titres, **6 astuces rendues MUETTES** (`silent`), 7 conditions de
  déclenchement corrigées, et l'astuce `four_arc_cuivre` supprimée (fusion dans `four_arc_fer`).
  `SAVE_VERSION` INCHANGÉ. Base EXACTE (364 / 14.81) ; **6/6 ancres uniques, 12/12 hachages conformes
  AVANT application**, `node --check` 7/7, **delta des 6 blocs +4 160 o EXACT**.
  (1) **Le champ `silent` (§F), et pourquoi PAS `when: () => false`** : `HelpPanel` appelle
  `t.when(game)` pour décider si une entrée est débloquée → neutraliser `when` **effacerait l'astuce
  de l'Aide**, l'inverse de la consigne. `silent` n'est lu que par `nextPendingTip`. Mesuré : les 6
  (`transport`, `priorite`, `batiment_deconnecte`, `liaisons_port`, `reserves`, `copier`) ne s'ouvrent
  JAMAIS **alors que leurs 6 conditions sont vraies** (le test est donc falsifiable) et figurent
  toutes dans l'Aide, dépliables en texte intégral.
  (2) **§C/§D/§E — le guide ouvre les popups LUI-MÊME** (`checkGuide` → `showTip(o.why)`, sans
  consulter `when` ni `silent`) : trois objectifs pointaient vers des astuces devenues muettes
  (`go_ile2`→`transport`, `fix_deconnecte`→`batiment_deconnecte`, `go_liaison`→`liaisons_port`) et
  auraient continué de les ouvrir. Leur `why` est retiré ; le bandeau et le halo du guide sont
  intacts (mesuré). Contrôle permanent : **aucun `why` du guide ne doit pointer vers une astuce
  `silent`** — il en reste 4 (`deficit`, `reseau_sature`, `eolienne`, `energie`), tous sains.
  (3) ⚠ **`tipAnyAtLevel(g, 'puits_petrole', 4)` : `minUpg` est un INDICE** (0 = Nv.1), donc
  « niveau 5 » s'écrit **4** — même convention que `tutUpgradedCount`. Mesuré aux 3 crans : muet aux
  Nv.1 et Nv.4, actif au Nv.5. Un cran d'écart déclencherait l'astuce avant le piège qu'elle annonce.
  (4) ⚠ **ÉCART NÉCESSAIRE — `I18N.applyToData` RÉÉCRIT `title` ET `body` des astuces depuis les
  tables LOCALES** (même piège qu'au lot 2 avec les goals, et qu'en 14.50/14.53) : **23 des 50 astuces
  ont une entrée LOCALES**, donc leur texte inline est ignoré au runtime. Mesuré : 3 changements du
  lot étaient **invisibles en jeu** — le titre « L'antenne Amplificatrice » restait « L'antenne T5 »,
  et les body réécrits de `bienvenue` et `accumulateur` restaient à l'ancienne version. Corrigé :
  titre de l'antenne dans les 4 langues (aligné sur le nom du bâtiment, renommé en 14.74) + body fr
  de ces 2 astuces. Les 7 autres textes réécrits (`densifier`, `four_arc_fer`, `puits_piege`,
  `foreuse`, les 3 du Collisionneur) n'ont pas d'entrée LOCALES → ils passaient déjà.
  (5) ⚠ **CONSÉQUENCE DU (4), à connaître : `applyToData` fusionne les tableaux `body` PAR INDEX.**
  Une langue dont la traduction a MOINS de paragraphes que l'inline hérite des paragraphes
  surnuméraires **EN FRANÇAIS** — mesuré après le correctif (4) : « Trois réseaux relient tout » en
  4ᵉ position d'un texte anglais. Les 2 paragraphes ajoutés ont donc été **traduits en en/es/de**
  (+662 o). **Audit ajouté et à rejouer à chaque retouche de texte : pour chaque astuce des LOCALES,
  la traduction doit avoir AUTANT de paragraphes que le fr** — mesuré après correction : **0
  déséquilibre** sur les 23 × 3.
  (6) **`four_arc_cuivre`** : l'astuce disparaît, le **BÂTIMENT est intact** (12 occurrences hors
  `GAME_TIPS`, dont `ARC_MODE_FROM_OLD`). Une save ayant vu l'astuce conserve l'identifiant orphelin
  dans `tipsSeen` — `HelpPanel` itère sur `GAME_TIPS`, pas sur `tipsSeen` : vérifié par rechargement
  RÉEL d'une save créée sur la base 364, l'Aide s'ouvre sans erreur et l'entrée a simplement disparu.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **151 assertions, 0 KO, rejouées 2 fois
  sans flottement** — 38 pour le lot (6 muettes jamais ouvertes mais présentes dans l'Aide, bandeau
  du guide + halo sans popup, les 7 conditions une par une, `irradie` muet pendant le calibrage et
  actif en `running`, fusion des fours, save ancienne, textes du Collisionneur et de la foreuse,
  page blanche) **+ 113 en NON-RÉGRESSION des lots 3A et 3B rejouées sur ce build**.
  ⚠ **PIÈGES DE HARNAIS** : (a) une astuce « muette » se prouve par un espion qui note TOUT popup
  ouvert au fil du jeu (un simple coup d'œil à un instant donné ne prouve rien) ; (b) pour rejouer la
  suite du lot 3B, la base doit être le build **363** — sur une base 364 la ModeModal n'existe plus
  et la partie difficile du test 3 est incréable.
  ⚠ **Taille : 3 268 951 → 3 274 102 o** (+4 160 les 6 blocs EXACT, +300 le correctif LOCALES fr,
  +662 les traductions, le reste = bump et `GAME_NOTES`).
  ⚠ **HORS PÉRIMÈTRE, non touché** : le bâtiment `four_arc_cuivre` et `ARC_MODE_FROM_OLD`, les 2 ids
  orphelins des LOCALES (`upgrade_vs_v2`, `non_stockable`, inoffensifs), les messages d'erreur du
  Collisionneur (`collider_penalite`, `collider_arret`, inchangés), `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 364`, `GAME_VERSION = 'Alpha 14.81'`, `SAVE_VERSION = 31`.**
  Changement 14.81 (brief `BRIEFTUTOLOT3Bverrous`, **LOT 3B — 8 sites A→H**) : **une partie neuve
  DÉMARRE DIRECTEMENT** (plus d'écran de choix de mode), **inventaire déplié à la création**, et
  **Port / Recherche / Calculateur restent grisés tant qu'ils n'ont rien à montrer** (pastille
  d'énergie MASQUÉE, pas grisée). `SAVE_VERSION` INCHANGÉ, aucun champ ajouté (`invOpen` n'est pas
  persisté et ne le devient pas). Base EXACTE (363 / 14.80) ; **8/8 ancres uniques, 16/16 hachages
  conformes AVANT application**, `node --check` 7/7, **delta des 8 blocs +2 652 o EXACT**, contrôles
  sémantiques 4/4 (`setNeedMode(true)` 0, `chooseMode('normal', false)` 1, `disabled: !islandTrade…` 2,
  `disabled: !researchUnlocked` 1).
  (1) ⚠ **`ModeModal` N'A PLUS AUCUN POINT D'ENTRÉE — le mode DIFFICILE n'est plus créable.**
  Décision d'Ethan, assumée, à rouvrir par les Options plus tard : **ne pas « réparer » ce point.**
  Les parties existantes ne sont pas touchées (le mode vit dans la save et `loadSave` le rétablit
  lui-même, AVANT le `if (!loaded)`) — vérifié par le vrai chemin : partie difficile créée sur la
  base 363, rechargée en 364 → **îles restées 28×28** (contre 32×32 en normal), `mode: 'difficile'`,
  **aucun toast de mode** (donc `chooseMode` n'a pas tourné — preuve par les conséquences, un appel
  ici reconstruirait les îles et détruirait la partie).
  (2) ⚠ **ÉCART NÉCESSAIRE 1 — le prédicat de la Recherche.** Le brief impose `tipResearchActionable`,
  qui compte `condition_ok` : or le **nœud 2 (« Accès Île 2 », `delivery` à `reqs` vides) est
  `condition_ok` DÈS LE PREMIER TICK**, sans être payable (piège déjà documenté en 13.61 pour
  `go_recherche`). Mesuré au boot : `tipResearchActionable = true` → **le bouton n'aurait JAMAIS été
  grisé** et l'objectif n°4 du brief était manqué (Port/Calculateur/pastille, eux, étaient corrects).
  Nouveau helper `researchPanelUnlocked` = **`hasPendingResearch` (gaté par `deliveryReady` depuis
  13.68 → vraiment « à portée ») OU un nœud déjà confirmé** (pour l'irréversibilité exigée au test 7).
  Mesuré : grisé au boot, actif dès la livraison payable, **reste actif après validation même port
  VIDÉ**. Aligné sur le guide : `go_recherche` ne pointe le bouton que si `hasPendingResearch`, donc
  **le halo ne désigne jamais un bouton grisé** (idem `go_liaison`/Port avec `islandUnlocked[2]`).
  ⚠ Effet assumé AVANT la première validation : le bouton suit le stock (s'allume quand la livraison
  devient payable, se re-grise si le joueur dépense) — c'est la définition de « à portée ».
  (3) ⚠ **ÉCART NÉCESSAIRE 2 — l'inventaire déplié RECOUVRAIT le bandeau du tutoriel.** L'inventaire
  ouvert est en SUPERPOSITION (`position:absolute; top:100%` du `.hud-stack`, 11.36) : mesuré sur
  420/360/390 px, il se plaçait **exactement sur le bandeau** (inventaire y 108→238, bandeau y
  108→174) → objectif, **compteurs du lot 3A** et bouton « Passer » **invisibles et inatteignables au
  premier lancement**, c'est-à-dire au seul moment où ils comptent. **CONTRE-ÉPREUVE : défaut
  PRÉEXISTANT** (base 363, inventaire ouvert À LA MAIN pendant le tutoriel → recouvrement identique ;
  inventaire fermé → bandeau à y=144, cliquable) que le dépliage d'office rendait SYSTÉMATIQUE.
  Corrigé en deux temps, car **le z-index seul ne suffit pas** : le bandeau étant semi-transparent, il
  se superposait au texte de l'inventaire (« Tuto 1/10 » sur « INVENTAIRE ») — illisible, donc pire.
  (a) `.tuto-banner{position:relative;z-index:21}` — 21 suffit : `.hud-stack` porte
  `position:relative;z-index:20` et **ENFERME** le z-index 40 de l'inventaire dans son contexte
  d'empilement ; reste sous les boutons flottants (45) et les panneaux (120-131). (b) `TutorialBanner`
  publie sa **hauteur MESURÉE** dans `--tuto-h` (`useLayoutEffect` + `ResizeObserver`, remise à 0 au
  démontage) et `.inventory.open` s'ancre à `top:calc(100% + var(--tuto-h, 0px))` → les deux
  s'EMPILENT. Hauteur mesurée et non figée : le bandeau passe à deux lignes sur écran étroit.
  Re-mesuré : inventaire à y=174 sur les 3 viewports, **5 items sur 5 visibles**, `Passer`/goal/
  compteur tous atteignables. Corrige aussi le cas préexistant pour tous les joueurs.
  (4) **Conséquence cosmétique SIGNALÉE, non corrigée** : `chooseMode` affiche son toast
  « 🗺 Mode Normal — grandes îles » au premier lancement — un mode que le joueur n'a pas choisi est
  donc nommé à l'écran. Sans effet fonctionnel ; le retirer demanderait de toucher `chooseMode`,
  hors des 8 blocs.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **113 assertions, 0 KO, rejouées 2 fois
  sans flottement** — 50 pour le lot (démarrage direct, création de slot par le VRAI chemin
  Options → Sauvegarde → nouvel emplacement, rechargement d'une partie difficile, 3 verrous +
  clics sans effet, déverrouillages successifs, contre-épreuve île 2 re-verrouillée, page blanche
  DEV + PUBLIQUE) **+ 63 en NON-RÉGRESSION du lot 3A rejouées sur ce build** (déroulé 1→10,
  compteurs, file d'attente, marquage à la fermeture, 360 px, guide sans compteur).
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) **le `PortPanel` porte les classes
  `research-panel port-panel`** → un sélecteur `.research-panel` matche AUSSI le Port (utiliser
  `:not(.port-panel)`) ; (b) valider un nœud ouvre **`.rd-popup` (« Recherche terminée »)**, dont le
  backdrop avale le clic suivant — une purge qui ne ferme que `.tip-popup` ne suffit pas ; (c) fermer
  un panneau demande d'**amorcer `useGhostGuard`** (pointerdown INTERNE, 13.50) puis de boucler
  jusqu'à disparition réelle, sinon le panneau reste ouvert et recouvre la cible suivante ;
  (d) **valider le nœud 2 EST « Accès Île 2 »** → Port/Calculateur/pastille s'ouvrent du même coup :
  toute assertion « ils restent grisés » doit être posée AVANT la validation ; (e) une île difficile
  fait **28×28** (12 + 2×`SEA_PAD`) contre 32×32 en normal — pas de seuil « < 24 » ; (f) un
  `addInitScript` s'exécute **avant `document.documentElement`** → un `MutationObserver` posé là lève.
  ⚠ **Taille : 3 262 251 → 3 268 951 o** (+2 652 les 8 blocs EXACT, le reste = les 2 écarts,
  le bump et `GAME_NOTES`).
  ⚠ **HORS PÉRIMÈTRE, non touché** : `ModeModal` (laissée en place, inatteignable), l'accès au mode
  difficile par les Options, `chooseMode` et son toast, `tipResearchActionable` (conservée telle
  quelle — elle sert aux astuces), `SAVE_VERSION`, le tutoriel et ses compteurs (lot 3A).
- **État précédent : `GAME_BUILD = 363`, `GAME_VERSION = 'Alpha 14.80'`, `SAVE_VERSION = 31`.**
  Changement 14.80 (brief `BRIEFTUTOLOT3Acompteurs`, **LOT 3A — 9 sites A→H**) : **chaque étape du
  tutoriel affiche un COMPTEUR de progression (« Mines 2/3 », posé/relié, niveau, débit) et les popups
  d'astuce ne s'écrasent plus — ils se mettent en FILE, et une astuce n'est marquée « vue » qu'à sa
  FERMETURE effective.** `SAVE_VERSION` INCHANGÉ (`tipsSeen` existe déjà ; seul le moment où il est
  écrit change). Base EXACTE (362 / 14.79) ; **9/9 ancres uniques, 18/18 hachages conformes AVANT
  application**, `node --check` 7/7 du premier coup, **delta des 9 blocs +4 712 o EXACT**.
  (1) **La file (§A/§B)** : `showTip` empile quand un popup est déjà ouvert (dédoublonné) ; `closeTip`
  marque vu PUIS dépile. Le cas réel visé : tout monter au Nv.2 AVANT de relier la 3e carrière valide
  les étapes 4 et 5 **dans la même frame** — mesuré : popup `tut_ameliorer` affiché, `tut_marge` le
  REMPLACE à sa fermeture, aucun des deux marqué vu avant fermeture, les deux dans l'Aide ensuite,
  file vide après (pas de 3e, pas de doublon sur tout le déroulé).
  (2) **Les compteurs (§C→§H)** : champ `progress: g => [[label, actuel, max]]` par étape, rendus dans
  la bannière (`.tuto-count-item`, vert `done` quand actuel ≥ max, `Math.min` d'affichage), recalculés
  à chaque bump du HUD. Nouveaux helpers `tutUpgradedCount` (nombre au niveau voulu) et `tutFlowOf`
  (**`Math.floor`** du débit brut, MÊME source `islandFlowAgg` que l'étape → jamais « 10/10 » pendant
  que l'étape refuse ; mesuré à 9,9/s → « 9/10 » et refus). Compteur affiché même à max = 1 (décision
  d'Ethan) ; étapes charbon/four/cimenterie distinguent **« Posée 1/1 · Reliée 0/1 »** — c'est là que
  les joueurs se bloquaient.
  (3) ⚠ **ÉCART NÉCESSAIRE 1 — la branche étape-0 de `checkTips` généralisée à l'étape COURANTE**
  (et son marquage à l'ouverture retiré) : sans cela le test 3 du brief (« recharger sans fermer →
  il se réaffiche ») était INFAISABLE — `tut_mine` restait marqué à l'ouverture, et pour les étapes
  ≥ 1 le §B rendait l'explication **IRRÉCUPÉRABLE** au rechargement popup ouvert (le franchissement ne
  se rejoue jamais, et l'astuce non marquée + `when: () => false` disparaissait AUSSI de l'Aide).
  Étape courante SEULEMENT (re-proposer les étapes passées ferait une rafale au boot des saves
  antérieures au lot 2). Mesuré : reload popup ouvert → réaffiché (`tut_mine` ET `tut_route`) ;
  fermé → marqué, plus réaffiché ; save « à l'ancienne » (why marqué à l'ouverture) → aucun popup au
  boot, bannière 6/10.
  (4) ⚠ **ÉCART NÉCESSAIRE 2 — `scheduleSave()` ajouté au marquage de `closeTip`** : déplacé à la
  fermeture, le marquage n'était plus persisté NULLE PART (l'ancien monde le sauvait via le
  `scheduleSave` qui suivait l'ouverture) → un popup lu serait revenu à chaque lancement si le joueur
  quittait sans autre action.
  (5) ⚠ **ÉCART NÉCESSAIRE 3 — `.tuto-main{flex-wrap:wrap}` ajouté au bloc §H (≤ 480 px)** : deux
  compteurs larges (« Reliées 10/10 · Au niveau 2 6/10 ») **débordaient du viewport 360 px** (mesuré :
  bord droit à 369 px) — le compteur est `flex-shrink:0` par design (il doit survivre à l'ellipse du
  goal), donc rien ne pouvait céder ; le test 9 du brief était infaisable. Il passe désormais ENTIER
  sur sa propre ligne (re-mesuré : 34→261 px, rien de coupé).
  (6) **« 10/10 » double est TRANSITOIRE par construction** : dès que les deux conditions passent, le
  `while` de `checkTutorial` gagne la frame et la bannière disparaît. L'état « 10/10 vert » ne
  s'observe que si l'AUTRE compteur bloque — mesuré ainsi (« Ciments/s 10/10 » vert pendant
  « Lingots/s 9/10 »). Ne pas chercher à observer le double 10/10, c'est structurel.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **63 assertions (38 + 25), 0 KO,
  rejouées 2 fois sans flottement** : déroulé 1→10 avec compteurs à chaque étape, progression
  1/3 → 2/3 → 3/3-vert, posée-non-reliée n'incrémente JAMAIS, double franchissement + file,
  concordance panneau Production (10/9), floor 9,9 → 9/10, marquage à la fermeture par RECHARGEMENTS
  réels (avec contre-épreuve), 360 px, `.tip-dismiss` intact, astuces contextuelles reprennent au
  skip, bandeau du GUIDE sans compteur (`gcount = 0`), page blanche DEV + PUBLIQUE (console vide).
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) une tuile-sonde « posée mais PAS
  reliée » doit être choisie **SANS route adjacente** — voisine d'une route existante, elle est
  connectée DÈS la pose et l'étape franchit avant la lecture (3 faux KO d'un coup) ; (b)
  `islandFlowAgg` est une déclaration de fonction d'un script classique → **réassignable via
  `window`** pour forger un flux fractionnaire (même famille que `activeEnergyAlerts` en 14.67) —
  restaurer après ; (c) sur une partie neuve « skippée », AUCUN objectif du guide n'est actif → pas
  de bandeau du tout : poser une mine non reliée pour armer `fix_deconnecte` avant d'asserter.
  ⚠ **Taille : 3 256 281 → 3 262 251 o** (+4 712 blocs EXACT, +282 écart 2, +367 écart 3, +609
  écart 1 + bump/`GAME_NOTES`).
  ⚠ **HORS PÉRIMÈTRE, non touché** : `nextPendingTip` marque toujours à la SÉLECTION (astuces
  contextuelles — les toucher affecterait les 32 astuces hors tuto), les marquages à l'ouverture du
  guide (K7, 13.61) et du Collisionneur (14.58), `SAVE_VERSION`, l'enregistreur `REC`, `TIP_SCENES`.
- **État précédent : `GAME_BUILD = 362`, `GAME_VERSION = 'Alpha 14.79'`, `SAVE_VERSION = 31`.**
  Changement 14.79 (brief `BRIEFTUTOLOT2dixetapes`, **LOT 2 — 5 blocs A→E**) : **la trame du tutoriel
  passe de 8 à 10 étapes** — le bouton Copier entre dans la trame (étape 4), une étape de réserve
  précède les transformateurs (étape 6), et l'objectif final devient une CADENCE à atteindre (étape 10).
  Les popups du tutoriel deviennent COURTS (une phrase), le texte intégral reste dans l'Aide.
  `SAVE_VERSION` INCHANGÉ, aucun champ persisté (la save ne stocke que `tutorial.step`, un entier).
  Base EXACTE (361 / 14.78) ; **5/5 ancres uniques, 10/10 hachages conformes AVANT application**,
  `node --check` 7/7 du premier coup, **delta des 5 blocs +5 268 o EXACT** (+603 o d'écart i18n
  documenté ci-dessous, le reste étant le bump et `GAME_NOTES`).
  (1) **La trame** : mine → route → carrière → **Copier (2 mines + 2 carrières DE PLUS, reliées → 3
  et 3)** → tout au Nv.2 → **réserve (2 + 2 de plus, reliées ET Nv.2 → 5 et 5)** → mine de charbon →
  four à fer → cimenterie → **cadence 10 lingots/s ET 10 ciments/s**. Nouveaux prédicats
  `tutCountConnected(g, id, n)` (compte les bâtiments RELIÉS au port — une mine posée dans un coin
  sans route ne compte pas, mesuré) et `tutFlowAtLeast(g, res, n)` (lit `islandFlowAgg().prod` =
  production BRUTE ; mesuré : 10 lingots + 9 ciments → étape OUVERTE, 10 + 10 → franchie).
  (2) **§E — le bouton Copier obéit à la trame** : `tabAllowed('copy')` rend vrai quand la cible de
  l'étape contient `.tab-copy` (lecture de `st.targets[].sel`). ⚠ **À l'étape 4 les menus Bâtiment ET
  Réseau restent verrouillés — c'est voulu** : le joueur DOIT copier, **y compris la ROUTE** (la
  branche COPY de `handleTap` arme `t.building.id` quel qu'il soit ; mesuré : copie d'une route →
  `tool='road'`, c'est ainsi qu'on relie les bâtiments copiés). Copier re-grise à l'étape 5 (mesuré).
  (3) **§D — champ `short` des astuces** : `TipPopup` affiche `tip.short` (UNE phrase) quand il
  existe, sinon `body` intégral ; l'Aide affiche TOUJOURS `body`. Contrôles sémantiques du brief :
  **11 `short`, 0 au-delà de 20 mots (max 18), 0 `why` orphelin, `tut_recherche` conservé** (sa carte
  reste dans l'Aide bien que l'étape recherche ait quitté la trame). Une astuce SANS `short`
  (bienvenue) rend son body complet (mesuré : 3 paragraphes).
  (4) ⚠ **ÉCART NÉCESSAIRE AU BRIEF — les 4 tables i18n `tutorial` portées de 8 à 10 goals**
  (fr/en/es/de, +603 o) : `I18N.applyToData` réécrit `s.goal` **PAR INDEX** depuis les tables
  (`s.goal = _g('tutorial', String(i))`) → appliqué verbatim, la bannière aurait MENTI de l'étape 4 à
  l'étape 8 **dans les 5 langues** (les goals de l'ancienne trame décalés d'un cran ; mesuré à la
  sonde avant correction, corrigé et re-mesuré : les 10 goals sortent justes en fr/en/de).
  (5) ⚠ **CORRECTION D'UN DIAGNOSTIC DES LOTS 1 ET 1d — le « popup pourquoi qui ne s'ouvre pas »
  était un ARTEFACT DE HARNAIS, pas un préexistant du jeu.** La purge des popups cliquait
  `.tip-popup button` = le PREMIER bouton = **`.tip-dismiss` (« désactiver les astuces »)** →
  `tipsEnabled` passait à `false` dès la bienvenue et plus AUCUN popup ne s'ouvrait. En fermant par
  **`.tip-ok`**, les popups « pourquoi » s'ouvrent tous (mesuré : bienvenue + les 10 popups d'étape,
  chacun en 1 paragraphe = le `short`). La contre-épreuve « identique sur la base » du lot 1 était
  vraie mais mesurait le même harnais défectueux des deux côtés.
  (6) **T8 — save mi-tuto de la base 361 rechargée en 362** : `step: 5` conservé tel quel → bannière
  **« Tuto 6/10 »** avec l'objectif de la NOUVELLE étape 6, aucun plantage, le joueur continue sur la
  nouvelle trame (comportement assumé : le step est un simple entier, pas de re-mapping).
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **54 assertions, 0 KO, rejouées 2 fois
  sans flottement** : déroulé RÉEL 1→10 (progression stricte `[1..10]`, **jamais de recul** même en
  posant des bâtiments Nv.1 pendant l'étape 6), `fx tutstep` 1..10 (cohérent lot 1d), 4/5 carrières
  Nv.2 → étape 6 PAS validée (exige 5 et 5), panneau Production concordant (ling.fer 10 / ciment 9),
  fin de tuto `active:false`, goals 4 langues, page blanche DEV + PUBLIQUE (console vide).
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) **fermer un popup d'astuce par
  `.tip-ok`, JAMAIS par le premier bouton** (qui est `.tip-dismiss` — c'est l'artefact du point 5) ;
  (b) le panneau Production se parse **par cellules** (`.prod-row` → `.pc-res`/`.pc-p`), pas au
  `textContent` concaténé (« 10 » + « 0 » se lit « 100 ») — et la cellule du nom contient l'`<img>`
  du sprite, donc elle n'est JAMAIS un élément « feuille » ; comparer au nom court `RES_SHORT` lu au
  runtime ; (c) le survol en mode Copier émet le **frame error PRÉEXISTANT 14.46**
  (`canPlace('__copy')`) → le filtrer des assertions console, ce n'est pas une régression.
  ⚠ **Taille : 3 250 360 → 3 256 281 o** (+5 268 blocs, +603 i18n, +50 bump/`GAME_NOTES`).
  ⚠ **HORS PÉRIMÈTRE, non touché** : `SAVE_VERSION`, l'enregistreur `REC` (lot 1d), le guide
  dynamique (13.61), les scènes d'illustration `TIP_SCENES` (les nouvelles astuces réutilisent les
  scènes existantes ou n'en ont pas), le verrou d'onglets hors Copier, `checkTips`.
- **État précédent : `GAME_BUILD = 361`, `GAME_VERSION = 'Alpha 14.78'`, `SAVE_VERSION = 31`.**
  Changement 14.78 (brief `BRIEFTUTOLOT1djournaleffets`, **LOT 1d — 12 blocs, dont 7 dans le moteur**) :
  **le journal passe des GESTES aux EFFETS** — il note désormais si une pose a abouti ou été refusée,
  le motif du refus, l'étape de tutoriel en cours et les variations de stock. `SAVE_VERSION` INCHANGÉ,
  aucun champ persisté. Base EXACTE (360 / 14.77) ; **12/12 ancres uniques, 24/24 hachages conformes
  AVANT application**, `node --check` 7/7 du premier coup, **delta +4 139 o EXACT**.
  (1) **Ce que le premier journal réel a montré** (188 s, 142 lignes, exporté depuis l'APK) : 43 entrées
  `?` en doublon des `tap` (**31 % du fichier**), des étiquettes dépendantes de la langue (`txt:INVENTAIRE`)
  ou ambiguës (`txt:✕` porté par **cinq** boutons distincts), et surtout **aucun moyen de savoir si un
  geste a abouti**.
  (2) **Doublon canvas fermé** (§C) : un tap sur la carte produit AUSSI un clic DOM qui remonte à la
  capture racine, sur un `<canvas>` que `closest` ne reconnaît pas → entrée `?`. Écarté à la source par
  `t.closest('canvas')`. Mesuré après : **10 poses → 10 `tap`, 0 `?`**.
  (3) **`labelOf` v2** : le repli TEXTE passe en DERNIER recours, une **première classe** s'intercale
  avant lui. ⚠ **La liste blanche reste PRIORITAIRE** : `"tab-btn tab-build"` donnerait `tab-btn`,
  générique à tous les onglets. Mesuré : les 3 croix testées sortent **distinctes** (`cls:ip-close`,
  `cls:res-pop-close`, `cls:slot-close`), **0 `txt:✕`**, et `cls:inv-label-btn` au lieu de
  `txt:INVENTAIRE`.
  (4) **`REC.act(g, kind, r, c, id, ok)` RENVOIE `ok` INCHANGÉ** — c'est ce qui permet d'envelopper
  `tryPlace`/`tryUpgrade`/`tryDemolish` sans que l'appelant voie une différence. **C'est le vrai risque
  du lot** : 7 sites moteur. Vérifié par les 6 chemins réels (tap, panneau d'amélioration, amélioration
  rapide, fiche bâtiment × 2, démolition au tap).
  (5) **Motifs de refus par `showToast`** (§K) : point de passage UNIQUE, plutôt que les onze
  `return false` de `tryPlace`. Mesuré : `{"fx":"place","r":5,"c":9,"id":"mine_fer","ok":0}` suivi de
  `{"fx":"msg","m":"❌ Mine Fer V1 : terrain non autorisé"}`.
  (6) **Variations de stock** : le 1ᵉʳ effet porte l'inventaire complet, les suivants le seul delta,
  et **rien du tout** si rien n'a bougé. ⚠ **Changement d'île → `lastRes` est purgé** → inventaire
  complet de la nouvelle île, jamais un delta croisé (mesuré : `{"acier":7,"ciment":3}` après bascule).
  ⚠ Le tampon `lastRes` n'est PAS remis à zéro par `toggle()` — sans conséquence, `fx` sortant avant
  `resDelta` quand l'enregistreur est éteint : le 1ᵉʳ effet après un allumage porte bien l'inventaire
  complet tant qu'on n'a pas déjà enregistré dans la même page.
  (7) ⚠ **LE §E PATCHE UNE BRANCHE MORTE, et le test 5 du brief repose sur une prémisse fausse.**
  `drag.mode = 'paint'` n'apparaît **qu'UNE seule fois dans tout le fichier** — au site consommateur
  patché ; `onPointerDown` ne l'assigne **jamais** (branches réelles : `select`/`place`/`upgrade`/
  `demolish`, cf. 14.75). Un glissé outil route **déplace la carte**, il ne pose rien. Mesuré :
  5 tuiles glissées → **0 posée, 0 `fx place`** — le journal reflète donc EXACTEMENT ce que le jeu a
  fait. Le bloc est appliqué verbatim (inerte, sans risque) ; **si la pose au glissé doit revenir,
  c'est `onPointerDown` qu'il faut rouvrir**, pas ce site.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **61 assertions, 0 KO**, **rejouées 2 fois
  sans flottement**. **Test 8** : tutoriel déroulé de bout en bout → **`fx tutstep` step 1..8**, une
  entrée par franchissement, `tutorial.active` à `false` à la fin. **Test 9** : 5 100 clics → `count()`
  plafonne à **5 000**, plus ancienne conservée `i:102`. **Test 10** : éteint, 40 s de jeu + gestes →
  `count() === 0`, puis allumage + export **sans geste intermédiaire** → `n:1` (le clic d'export seul).
  **Test 11** : 3 min de jeu enregistrement actif → 0 `tickError`, canvas peint, 0 erreur console.
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) `tryUpgrade` refuse LÉGITIMEMENT quand
  le port est vide — un test d'amélioration doit **approvisionner le port** (`RES_SHORT` en boucle),
  sinon on mesure le manque de ressources et non la préservation du retour ; (b) un flood de routes
  BFS **sature l'île** (87 land + 36 coast, 120 routes posées → plus une seule tuile libre) : tracer
  des **chemins ciblés** port→bâtiment ; (c) une tuile **adjacente au port** donne un chemin BFS VIDE,
  donc aucune route posée, et `roadReachesPortFootprint` échoue alors que le bâtiment touche le port —
  imposer une distance ≥ 3 ; (d) `checkTutorial` est un **`while`** : si toutes les conditions sont
  vraies d'un coup, les 8 étapes défilent dans la même frame.
  ⚠ **Taille : 3 246 200 → 3 250 360 o.** Les 12 blocs pèsent **+4 139 o EXACT** ; le reste est le bump
  et le nouveau `GAME_NOTES`.
  ⚠ **HORS PÉRIMÈTRE, non touché** : le verrou du tuto (lot 2), l'ajout de `data-tut` (le repli
  « première classe » suffit), l'échantillonnage périodique des ressources (écarté au §2 du brief),
  `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 360`, `GAME_VERSION = 'Alpha 14.77'`, `SAVE_VERSION = 31`.**
  Changement 14.77 (brief `BRIEFTUTOLOT1cexportnatif`, **LOT 1c — 3 blocs, dont un DÉPLACEMENT de
  fonction entre portées**) : **le journal exporté était INTROUVABLE sur l'appareil** — l'export
  empruntait un `Blob` + `<a download>` là où la WebView de l'APK ne produit aucun fichier atteignable.
  `SAVE_VERSION` INCHANGÉ, aucun champ persisté. Base EXACTE (359 / 14.76) ; **3/3 ancres uniques,
  6/6 hachages conformes AVANT application**, `node --check` 7/7 du premier coup, **delta +832 o EXACT**.
  (1) **Le vrai mécanisme, c'est le PONT NATIF** : l'export de SAUVEGARDE appelle
  `window.ArchipelNative.saveText(name, text)` **AVANT** toute tentative de Blob (via `NATIVE_SAVE`,
  13.63). Le lot 1 avait lu le repli et pris le repli pour le mécanisme. `saveTextFile` **REMONTE** de
  `SlotsModal` au niveau module et devient l'**unique** implémentation, partagée par les deux exports.
  ⚠ **Contrôle sémantique à rejouer à chaque retouche** : **1** définition `saveTextFile = (name, text)`,
  **2** appels `saveTextFile(` — le déplacement met en risque l'appel historique de `SlotsModal`, qui se
  résout désormais vers la portée module.
  (2) **`.jsonl` → `.txt`** : Android n'associe **aucune application** à `.jsonl` → fichier ni ouvrable
  ni partageable. Le CONTENU reste du JSON-lines (une ligne = un objet). Après patch, `.jsonl` n'apparaît
  plus que dans les **3 occurrences de commentaire** qui expliquent l'abandon, **0 en code**.
  (3) ⚠ **ORDRE DE DÉCLARATION, même piège TDZ qu'au lot 1b — ne pas « ranger » le code.**
  `saveTextFile` est posé juste après `NATIVE_SAVE` qu'il lit, donc **APRÈS** le module `REC` qui
  l'appelle (offsets mesurés sur la base 359 : **`REC` à 2 109 119, `NATIVE_SAVE` à 2 185 848**). Sans
  risque : l'appel n'a lieu qu'au clic. Le remonter avant `NATIVE_SAVE` rouvrirait la zone morte
  temporelle → **page blanche**.
  (4) ⚠ **LE COMMENTAIRE DU LOT 1 ÉTAIT FAUX SUR SES TROIS AFFIRMATIONS** (« .jsonl », « fiable en APK »,
  « même motif que l'export de sauvegarde ») : l'ancre du §J **commence au commentaire**, pas à
  `exportNow`. Un commentaire mensonger qui survit à la correction qu'il décrit est pire que pas de
  commentaire — c'est lui qui m'avait fait livrer le défaut au lot 1.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **33 assertions, 0 KO**, **rejouées 2 fois
  sans flottement**. ⚠ **Le pont `ArchipelNative` est SIMULÉ** (injecté par `addInitScript` AVANT le
  script du jeu — `NATIVE_SAVE` est évalué au chargement du module) : c'est le seul moyen d'exercer en
  labo le chemin qui échouait sur APK. **Test 1** : `saveText` appelé **1 fois**, nom
  `archipel-journal-360.txt`, et **AUCUN Blob créé** — le pont court-circuite bien le repli.
  **CONTRE-ÉPREUVE EXÉCUTÉE sur la BASE 359 avec le MÊME pont présent** : `saveText` **n'est PAS
  appelé** (n=0) et un Blob est créé à la place → c'est exactement le défaut, et le test est falsifiable.
  **Test 3** : sans pont, `NATIVE_SAVE` est **`undefined`** (valeur de retour du `&&`, pas `false` —
  piège d'assertion) donc falsy → repli Blob, **même nom de fichier**. **Test 4** : presse-papier
  conservé et **strictement identique** au contenu du fichier (440 o des deux côtés). **Test 5**
  (le site mis en risque par le déplacement) : export de sauvegarde par de VRAIS clics
  Options → Sauvegarde → Exporter → « ⤓ Télécharger .txt » → passe par le pont, nom
  `archipel-Partie_1.txt`, aucun Blob, et le contenu **se ré-importe** (`decodeSave` + `JSON.parse` OK).
  **Test 6** : les 2 éditions démarrent, console vide, 0 `ReferenceError`.
  ⚠ **Taille : 3 245 389 → 3 246 200 o.** Les 3 blocs pèsent **+832 o EXACT** ; le reste est le bump et
  le nouveau `GAME_NOTES` (plus court).
  ⚠ **PIÈGE DE HARNAIS** : `play()` ne doit pas présumer qu'un panneau Options est ouvert — le fermer
  seulement s'il existe, sinon le scénario qui n'allume pas l'enregistreur s'effondre sur `.slot-close`.
  ⚠ **HORS PÉRIMÈTRE, non touché** : le verrou du tuto (lot 2), `copyText`, le format JSON-lines, le
  double-échappement de `notes` dans `version.json` (anomalie préexistante signalée au lot 1b),
  `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 359`, `GAME_VERSION = 'Alpha 14.76'`, `SAVE_VERSION = 31`.**
  Changement 14.76 (brief `BRIEFTUTOLOT1bdevbuild`, **LOT 1b — 3 blocs**) : **l'enregistreur d'actions
  du lot 1 devient EXCLUSIF à l'édition DEV** (décision d'Ethan). `SAVE_VERSION` INCHANGÉ, aucun champ
  persisté. Base EXACTE (358 / 14.75) ; **3/3 ancres uniques, 6/6 hachages conformes AVANT application**,
  `node --check` 7/7 du premier coup, **delta +723 o EXACT** (l'attendu au byte près).
  (1) ⚠ **LE LOT EST UN PIÈGE DE ZONE MORTE TEMPORELLE, ET LE BRIEF LE DÉSAMORCE — NE PAS
  « SIMPLIFIER ».** `const DEV_BUILD = false;` est déclaré **APRÈS** le module `REC` dans le fichier
  (offsets mesurés sur la base 358 : **`REC` à 2 109 119, `DEV_BUILD` à 2 145 019**). Porter la garde sur
  l'initialisation (`let on = DEV_BUILD && lsGet(…)`) lève une `ReferenceError` **au chargement** →
  **page blanche**. `DEV_BUILD` n'est donc consulté QUE dans les corps de `push` et de `toggle`, appelés
  au clic ou au tap, longtemps après. **Contre-épreuve EXÉCUTÉE** (le module `REC` extrait du fichier
  PATCHÉ, exécuté dans un `vm` où `const DEV_BUILD` est déclaré APRÈS lui, exactement l'ordre du
  fichier) : la variante livrée **démarre**, la variante naïve lève `ReferenceError: Cannot access
  'DEV_BUILD' before initialization`. Les deux côte à côte — sans quoi le montage ne prouverait rien.
  (2) **`isOn()` peut rendre `true` en édition PUBLIQUE et c'est VOULU** : un `archipel_rec` à `'1'`
  hérité d'une édition DEV sur la même origine survit. Sans effet — `push` refuse, `toggle` refuse, la
  ligne d'option n'est plus rendue. **Ne pas « corriger » cet état apparent : le neutraliser à l'init,
  c'est rouvrir la TDZ.** C'est toute la raison d'être de la garde dans `push` (§E) : sans elle, le
  journal tournerait **en silence, sans aucune UI pour l'éteindre ni l'exporter**. Test 4 exécuté :
  drapeau posé à la main + **2 min de jeu réel** → `REC.count() === 0`, `toggle()` rend `false`.
  (3) **La dette de traduction du lot 1 est ANNULÉE** : les 4 libellés restent en français et n'ont plus
  à entrer dans les tables i18n — l'édition publique ne les rend plus. Rien à faire, ni maintenant ni
  au lot 2. ⚠ **La capacité d'enregistrer sur APK est INTACTE** : l'édition DEV (`fr.archipel.industry`)
  est précisément celle installée sur le téléphone d'Ethan (14.55).
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **40 assertions, 0 KO**, **rejouées 2 fois
  sans flottement** + **3 assertions de contre-épreuve TDZ** sous Node. **Test 2 (édition DEV)** : suite
  du lot 1 rejouée en entier — étiquettes `["cls:tab-build","tut:mine_fer","cls:tab-net","tut:port"]`,
  **0 `cls:tab-btn`**, tap journalisé avec `tool:"mine_fer"`, **téléchargement RÉEL** de
  `archipel-journal-359.jsonl` (en-tête `build:359`, entrées `ui` ET `tap`), anneau plafonné à 2 000.
  **Test 3 (édition PUBLIQUE)** : ni ligne d'option ni bouton d'export ; **« Sons » suit immédiatement
  « Guide »**, le fragment retiré ne laisse pas de trou. **Test 5** : les 2 éditions démarrent, **console
  vide au chargement, 0 `ReferenceError`** — c'est LE risque du lot et il ne se voit que là.
  **Test 6** : les **5** assertions `DEV_BUILD` du workflow (lignes 73/75/103/119/191) rejouées en
  simulation locale, **5/5 vertes** — dont le contrôle bloquant 14.55 sur `index.html`.
  ⚠ **ÉCART DEV/PUBLIQUE ATTENDU, ne pas le lire comme un défaut** : l'édition DEV a **DEUX** lignes
  d'options de plus, pas une — l'enregistreur **et « Mode développeur »**, ce dernier gaté sur
  `DEV_BUILD` depuis 14.55. Vérifié : hors ces 2 lignes, les listes sont **identiques et dans le même
  ordre** (15 lignes), rien n'est perdu en publique. Une assertion qui n'excepte que l'enregistreur
  tombe en KO à tort (c'est arrivé à la 1ʳᵉ passe).
  ⚠ **Taille : 3 244 736 → 3 245 389 o.** Les 3 blocs pèsent **+723 o EXACT** (mesuré AVANT le bump :
  3 245 459) ; le total redescend parce que le nouveau `GAME_NOTES` est 70 octets plus court.
  ⚠ **ANOMALIE PRÉEXISTANTE SIGNALÉE, NON CORRIGÉE (hors périmètre)** : le champ `notes` de
  `version.json` est **doublement échappé** par l'étape CI « Sync version.json » (`\\u00ab` au lieu de
  `«`) → le bandeau « Mise à jour disponible » affiche littéralement `é` / `«` au lieu des
  accents et des guillemets. **Ce n'est PAS une régression** : le build 357 porte exactement le même
  défaut (`ic\\u00f4nes du pack`), et probablement tous les précédents. Les notes SONT rendues au joueur.
  Correctif d'une ligne dans le workflow, à faire dans un lot séparé.
  ⚠ **HORS PÉRIMÈTRE, non touché** : le verrou du tuto (lot 2), `TESTER_BUILD`, le mode rapide, les
  tables i18n, `SAVE_VERSION`, `exportNow` (non gaté — inatteignable en publique, son bouton n'existe pas).
- **État précédent : `GAME_BUILD = 358`, `GAME_VERSION = 'Alpha 14.75'`, `SAVE_VERSION = 31`.**
  Changement 14.75 (brief `BRIEFTUTOLOT1enregistreur`, **LOT 1 sur 2 — l'ENREGISTREUR D'ACTIONS**) :
  **une option de diagnostic journalise chaque bouton touché et chaque tuile tapée**, exportable en
  `.jsonl`. `SAVE_VERSION` INCHANGÉ, **aucun champ de sauvegarde** (le journal vit en MÉMOIRE, seul le
  drapeau ON/OFF persiste dans `localStorage`). Base EXACTE (357 / 14.74) ; **5/5 ancres uniques,
  10/10 hachages conformes AVANT application**, `node --check` 7/7 du premier coup, **delta des 5 blocs
  +4 661 o EXACT** (l'attendu au byte près).
  (1) **Objet** : le lot 2 (tuto forcé) se branchera sur la MÊME couche de capture. **Aucun verrou ici,
  aucun comportement de jeu modifié.**
  (2) **Anneau MÉMOIRE de 2 000 entrées** (`REC`, posé juste après `lsDel`) : les plus anciennes tombent.
  Zéro impact sauvegarde, zéro migration. Le drapeau seul persiste (`lsGet`/`lsSet`, clé `archipel_rec`),
  **défaut OFF**. Capture des clics en **phase CAPTURE sur la racine `.app`** (`onClickCapture`) : un seul
  point au lieu des ~178 `onClick`.
  (3) ⚠ **ÉCART AU BRIEF, DÉLIBÉRÉ ET NÉCESSAIRE — le point d'entrée du tap canvas est DÉPLACÉ.** Le §B
  posait `REC.tap` **dans `handleTap`**. Or `handleTap` n'a **qu'un seul appelant**, gardé par
  `drag.mode === 'select'` (`onPointerUp`) : il n'est atteint que quand **aucun outil n'est armé**. Pose,
  amélioration et démolition passent par trois AUTRES branches → **elles n'auraient JAMAIS été
  journalisées**, alors que ce sont exactement les gestes que le tutoriel enseigne. **Mesuré avant
  correctif : 0 entrée `tap` pour deux taps outil Mine de fer en main** (le test 4 du brief exige
  pourtant `tool:"mine_fer"` — son ancre et son critère d'acceptation se contredisent). L'appel est donc
  posé **dans `onPointerUp`, avant l'aiguillage**, sur le **tap franc** (`!drag.panned && drag.dist <
  TAP_THRESHOLD`) → une entrée par tap, tous modes confondus, avec l'outil réellement armé ; un glissé
  (pan) n'est pas retenu. `handleTap` est laissé intact. **Mesuré après : 2 entrées, coordonnées exactes,
  `tool:"mine_fer"`.** ⚠ `drag.mode === 'paint'` n'est plus jamais posé par `onPointerDown` (branche
  morte) → les 4 modes réels sont couverts.
  (4) ⚠ **LE TEST 1 DU BRIEF EST INEXACT (sans conséquence)** : il attend `n:0` après activation +
  export. **Le clic sur « Exporter le journal » se journalise LUI-MÊME** — la capture racine s'exécute
  **avant** le `onClick` du bouton — donc `n:1`, avec une entrée `cls:slot-new`. Mesuré. Le vrai critère
  (rien n'est enregistré tant que l'option est éteinte) est vérifié séparément : **`REC.count() === 0`
  après 5 clics et 5 taps OFF** — et il ne peut pas être prouvé par l'export, puisque `toggle()` **vide
  le tampon** à l'allumage.
  (5) **Étiquetage par stabilité décroissante** : `data-tut` > classe significative > texte tronqué.
  ⚠ **Le `(?!btn\b)` de la regex est LA pièce critique** : `className` vaut `"tab-btn tab-build …"` →
  sans le lookahead, tous les onglets deviendraient `cls:tab-btn`, **indiscernables**. Mesuré au test 3 :
  `["cls:tab-build","tut:mine_fer","cls:tab-net","tut:port"]`, **0 `cls:tab-btn`**.
  (6) **Export** : fichier `.jsonl` (Blob + `<a download>`, le motif de l'export de sauvegarde) **+**
  tentative de presse-papier. Le fichier est fiable en APK, le presse-papier ne l'est pas.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **49 assertions, 0 KO**, **rejouées 2 fois
  sans flottement**, viewport 420 px / DPR 3. **Test 1** : option éteinte, bouton d'export ABSENT,
  `archipel_rec` absent du `localStorage`, `REC.count() === 0` après 10 gestes ; puis allumage →
  **téléchargement RÉEL** de `archipel-journal-358.jsonl` avec en-tête `{k:"meta", build:358,
  ver:"Alpha 14.75"}`. **Test 2** : drapeau conservé au rechargement, et **la sauvegarde réelle ne
  contient ni `"rec"` ni `archipel_rec`**. **Test 4** : les 2 taps aux bonnes coordonnées, puis
  **couche logique ouverte par le VRAI bouton** → `lay:1`. **Test 5** : 2 100 clics → `count()` plafonne
  à **2 000**, la plus ancienne conservée est `i:101` (et non `i:1`), la numérotation continue à 2 100.
  **Test 6** : enregistrement ACTIF, pose + amélioration + démolition + Port + Recherche + couche logique
  + **20 s de jeu réel** → 0 `tickError`, canvas peint, **0 erreur console**. **Test 7** : `useGhostGuard`
  intact — le clic fantôme est bien AVALÉ (le journal, lui, peut le contenir : la capture racine passe
  avant le garde, **limite assumée, rien n'en dépend au lot 1**). **Test 8** : tuto intact (bannière
  « Tuto 1/8 » → « Tuto 2/8 », halo DOM ET canvas, menu filtré à 1 carte, 3 onglets `tab-locked`).
  ⚠ **CONTRE-ÉPREUVE EXÉCUTÉE** : le popup « pourquoi » de l'étape 2 ne s'ouvre pas — **comportement
  PRÉEXISTANT**, identique sur la base 357 non patchée (`tipsSeen === ["bienvenue"]` des deux côtés).
  Ce n'est PAS une régression de ce lot ; à instruire séparément si le tuto doit expliquer chaque étape.
  ⚠ **PIÈGE DE HARNAIS** : l'extraction des blocs `<script>` doit **conserver ce qui suit le `>`** sur la
  ligne d'ouverture — les deux UMD React commencent par `/**` **sur la ligne du tag** ; un extracteur qui
  jette la ligne entière rend 2 blocs sur 7 en `SyntaxError` et fait croire à une casse du patch.
  ⚠ **Taille : 3 239 471 → 3 244 736 o.** Les 5 blocs du brief pèsent **+4 661 o EXACT** ; le reste est
  le bump, le nouveau `GAME_NOTES` et le déplacement du point d'entrée du tap (commentaire de décision).
  ⚠ **DETTE ASSUMÉE** : les **4 libellés ne sont traduits dans aucune table** (français dans les
  5 langues) — acceptable pour un outil de diagnostic, à combler s'il reste en build public. Tout bouton
  tombant sur le repli `txt:` est un candidat à `data-tut` au lot 2 : **c'est précisément le but du
  premier journal réel**.
  ⚠ **HORS PÉRIMÈTRE, non touché** : tout verrouillage de bouton, la fusion tuto/guide, l'extension de
  `data-tut`, les chapitres île 2, `SAVE_VERSION`, `useGhostGuard`, `handleTap`.
- **État précédent : `GAME_BUILD = 357`, `GAME_VERSION = 'Alpha 14.74'`, `SAVE_VERSION = 31`.**
  Changement 14.74 (brief `BRIEF-neuf-icones`, pack `archipelicones finale`, **4 modifications de nature
  DIFFÉRENTE**) : **9 emoji de plus deviennent des icônes du pack** (bateau, pétrole, carte, montagne,
  île, mélange, dossier, boussole, colis). `SAVE_VERSION` INCHANGÉ, aucun champ persisté.
  Base annoncée 355 / 14.72 ; base RÉELLE d'exécution **356 / 14.73** — **les 4 ancres sont sorties à
  `count == 1` sans adaptation**, le lot 14.73 n'ayant touché que les boutons de priorité.
  ⚠ **Le lot n'est PAS « neuf insertions de données ».** Il en embarque quatre natures, et trois ne
  sont pas mécaniques : (A) 9 affectations `window.__SPRITE_DATA__[…]` (clés NEUVES → forme homogène,
  le piège du littéral d'objet ne concerne que les REMPLACEMENTS) ; (B) 9 entrées dans
  `UI_ICON_BY_EMOJI` (**49 → 58**) ; (C) **2 sites React que le mécanisme automatique ne couvre pas** ;
  (D) **1 règle CSS sans laquelle ces 2 sites deviennent illisibles**.
  (1) **§2C — L'EMOJI PASSÉ EN ARGUMENT POSITIONNEL ÉCHAPPE À `leadIconOf`.** Sur l'écran de choix de
  mode, `card(mode, icon, name, desc)` rend son 2ᵉ argument **tel quel** : aucune chaîne n'est lue,
  donc `leadIconOf` ne voit rien. Il faut poser `uiIcon()` **à la main** sur les 2 sites
  (`card('normal', uiIcon('carte', "🗺", 'ico-mode')` et l'équivalent montagne). Le repli emoji est
  conservé → le site ne peut pas casser si le sprite manque. **Parenthèses vérifiées PAR PARCOURS**
  (compteur de profondeur sautant chaînes et échappements) sur le fichier PATCHÉ, pas à l'œil : une
  parenthèse manquante ici donne une **page blanche**, pas une erreur visible.
  (2) ⚠ **CORRECTION FACTUELLE AU BRIEF (§2D), sans conséquence sur le patch** : il justifie la règle
  CSS par « `.ui-ico` fait 11×11 px ». **Faux** — la règle de BASE `.ui-ico` fait **16×16** (ligne 242) ;
  le 11×11 est scopé à `.inv-prod-btn` (ligne 102), et c'est cette ligne-là que l'ancre `.ui-ico{width:
  11px;height:11px;}` attrape (en sous-chaîne, `count == 1`). **La conclusion reste juste** : sans règle
  dédiée l'icône de mode tomberait de ~32 px (emoji à `font-size:2rem`) à **16 px**. La règle
  `.mode-card-icon .ui-ico.ico-mode{width:32px;…}` est donc bien nécessaire, et sa spécificité (0,3,0)
  bat `.ui-ico` (0,1,0) **quel que soit son emplacement** → posée à l'ancre du brief (ligne 103, au
  milieu des règles de la barre d'inventaire), ce qui est stylistiquement bizarre mais fonctionnellement
  identique. Mesuré en jeu : **32×32 px**, classe `ui-ico ico-mode`, sur les DEUX cartes.
  (3) ⚠ **LES CLÉS DE LA TABLE PORTENT L'EMOJI NU, SANS U+FE0F** — et c'est vérifié dans les deux sens.
  `leadIconOf` lit `String.fromCodePoint(msg.codePointAt(0))` **puis** consomme le sélecteur de variante
  séparément : une clé avec VS16 ne matcherait jamais. Or plusieurs de ces emoji sont suivis de U+FE0F
  dans les tables i18n. Mesuré sur les 9 : reconnus **NUS** ET **suivis de U+FE0F**, avec le reste du
  libellé intact. Audit : **0 clé porteuse de VS16** sur les 58.
  ⚠ **La valeur est le nom SANS le préfixe `ui_`** (`'carte'`, pas `'ui_carte'`) — c'est `uiIcon` qui
  préfixe. Vérifié sur les 58.
  (4) **NEUF, PAS DOUZE — 3 icônes ÉCARTÉES en séance d'art, à ne pas réintroduire** : `ui_diplome` 🎓
  (lit comme un casque après 4 tentatives), `ui_maillon` 🔗 (les anneaux lisent « lunettes », la version
  reliée « H »), `ui_retour` 🌙 (le croissant lit « C » ; la variante sablier **EST** `ui_attente` à
  l'identique). Elles sont dans `ecartees/` du pack pour arbitrage. **Ces 3 emoji restent des emoji en
  jeu — ce n'est PAS un défaut** (mesuré : `leadIconOf` rend `null`, aucun sprite `ui_diplome`/
  `ui_maillon`/`ui_retour` en base).
  ⚠ **5 pistes de la spec d'origine abandonnées pour COLLISION** avec des icônes déjà en base : 🛢
  « goutte » contre `ui_carburant`, 🗺 « quadrillage » contre `ui_densifier`, ⚗ « barres verticales »
  contre `ui_production`, 🎓 et 🧭 « losange » contre `ui_gemme`. Les formes livrées sont AUTRES —
  volontaire, ce ne sont pas des écarts à corriger.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + **delta +3 089 o EXACT** (`os.path.getsize`) +
  les **9 PNG re-décodés depuis le HTML PATCHÉ** : SHA-256 **9/9 conformes**, **16×16**, alpha
  **BINAIRE**, **5 couleurs**, masque bit à bit vs `ui_port` → **193 px opaques, 0 pixel d'écart sur
  les 9**, contour intégralement `#1E2128`. + Chromium **12 assertions, 0 KO** et **10 de
  non-régression**.
  **Test 12 (celui qui prouve que la règle CSS existe)** : écran de choix de mode capturé AVANT le
  choix → les 2 cartes rendent `ui_carte` / `ui_montagne` à **32×32**, plus aucun emoji résiduel.
  **Test 10 en canvas, à TAILLE RÉELLE (16 px, sans zoom)** : les 9 dessinées à côté de `ui_port` /
  `ui_monter` / `ui_ok` / `ui_arret` → bbox `{top:1, bottom:15, left:1, right:15}` et **193 opaques,
  identiques aux 4 références sur les 13 icônes**.
  **Test 13** : la 1ʳᵉ astuce du tutoriel rend `ui_ile` (et non 🏝). **Test 15** : les **100** icônes
  `ui_*` décodent (0 échec) ; diff des clés de sprite **1 502 → 1 511** = **exactement les 9 ajoutées,
  0 modifiée**. **Save créée par la BASE 356 rechargée en 357** → ordres de priorité énergie ET transit
  conservés, stocks intacts, `SAVE_VERSION` 31, **0 `tickError`, 20 s de jeu réel, 0 `pageerror`**.
  ⚠ **L'AUDIT DE LA TABLE (institué au lot 14.72) EST REJOUÉ ET DOIT L'ÊTRE ICI PLUS QUE JAMAIS** :
  ce lot fait passer la table de 49 à 58 entrées. Mesuré : **58 entrées, 55 noms de sprite distincts,
  0 manquant, 0 VS16**.
  ⚠ **PIÈGE DE HARNAIS** : `SPRITE_DATA` est une `const` de MODULE, pas une propriété de `window` →
  `page.waitForFunction(() => window.SPRITE_DATA)` **expire** ; attendre la disparition de `#splash`,
  puis référencer `SPRITE_DATA` **par son nom nu** dans `page.evaluate`. Et une capture de bandeau à
  `transform:scale(2)` est **rognée par le viewport de 420 px** : au-delà de ~11 icônes il faut passer
  sur deux lignes.
  ⚠ **ÉTIQUETTE PÉRIMÉE DANS LE PACK, sans effet** : `planches/finale_test.png` porte encore le titre
  « 12 nouvelles mélangées à 12 déjà en base » alors que 3 ont été écartées. C'est l'artefact d'art qui
  est daté, pas la livraison.
  ⚠ **Taille : 3 236 323 → 3 239 412 o (+3 089 o EXACT**, l'attendu au byte près) ; 3 239 471 après
  bump, le nouveau `GAME_NOTES` étant plus long.
  ⚠ **HORS PÉRIMÈTRE (§2), non touché** : toute autre icône, tout autre site d'appel (les 7 autres
  emoji du lot sont couverts d'office par `leadIconOf` / `iconLabel` / le champ `icon:` de `GAME_TIPS`
  — **aucun appel à poser à la main**), `uiIcon`, `iconLabel`, les tables i18n, `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 356`, `GAME_VERSION = 'Alpha 14.73'`, `SAVE_VERSION = 31`.**
  Changement 14.73 (brief `BRIEFCORRECTIFinversionenergie`, **1 bloc**) : **l'inversion Monter/Descendre
  du panneau Énergie est FERMÉE** — c'est l'anomalie signalée au lot 14.72, ici corrigée.
  `SAVE_VERSION` INCHANGÉ, **delta 0 OCTET** (le patch PERMUTE, il n'ajoute rien).
  Base EXACTE (355 / 14.72 / 3 236 325 o) ; **1/1 ancre unique, hachage conforme AVANT application**
  (7ᵉ brief pré-compilé d'affilée), `node --check` 7/7 du premier coup.
  (1) **Le défaut** : `moveEnergyPriority(key, dir)` fait `splice(i,1)` puis `splice(i+dir,0,key)` →
  `+1` déplace vers le BAS. Or le bouton qui appelait `+1` portait le titre « Monter » **et**, depuis
  14.72, le sprite `ui_monter`. L'en-tête annonce « Ordre de priorité (haut = servi en premier) » et
  `.ep-rank` numérote 1, 2, 3… de haut en bas : aucune ambiguïté sur ce que « monter » devrait faire.
  ⚠ **Le défaut est PRÉEXISTANT** (contre-épreuvé sur la base 354 au lot précédent) ; 14.72 ne l'a pas
  créé, il l'a rendu VOYANT — un `title=` ne se survole pas au doigt, une flèche se lit d'un coup d'œil.
  (2) **3 options écartées, la 4ᵉ retenue** : échanger les deux `title:` laisserait la flèche VERS LE
  HAUT sur le bouton qui descend (on corrige le mot, on garde le mensonge visuel — **pire qu'avant**) ;
  inverser le signe DANS `moveEnergyPriority` ferait diverger sa convention de celle de `movePriority`
  du Port (piège pour la suite). **Retenu : PERMUTER LES DEUX BOUTONS.** Le bloc qui appelle `-1` passe
  en premier avec « Monter » + `ui_monter` ; celui qui appelle `+1` passe en second avec « Descendre »
  + `ui_baisser`. Les `disabled` suivent leur bouton. **Les titres et les sprites NE BOUGENT PAS** — ce
  sont les comportements qui viennent se placer sous les bons libellés. Résultat : le panneau Énergie
  devient **strictement identique au Port** (même ordre, même convention de signe, mêmes états grisés).
  ⚠ **C'EST UN CHANGEMENT DE COMPORTEMENT**, assumé : un joueur qui avait appris la manipulation
  actuelle (cliquer la flèche du haut pour rétrograder) verra les deux boutons échangés. **Aucune
  migration possible et aucune nécessaire** — `energyPriority` stocke une LISTE ORDONNÉE, pas un sens
  de tri ; les ordres existants sont conservés tels quels, seuls les boutons changent de place.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + **delta 0 octet EXACT** (ancre et remplacement font
  **379 octets chacun**) + le bloc du **PORT vérifié INCHANGÉ** (`count == 1` avant ET après).
  **Chromium, 3 suites, 37 assertions, 0 KO** :
  **Test 6 (LE test du lot)** : `(10,13) (10,14) (10,15) (10,16)` → clic « Monter » sur la 2ᵉ ligne →
  `(10,14) (10,13) (10,15) (10,16)` — **elle passe 1ʳᵉ**. **Test 7** : « Descendre » sur la 2ᵉ → elle
  passe 3ᵉ. **Test 8** : ligne 1 = flèche du HAUT grisée (opacité .3, 16×16, visible), dernière ligne =
  l'inverse ; rangs 1,2,3,4 de haut en bas — **capture avant/après à l'appui, le symptôme est retourné**.
  **Test 10 en MOTEUR RÉEL** (et non sur une assertion creuse) : 1 éolienne Nv.16 (**262 144 kW**) +
  câble illimité + **4 refroidisseurs** (131 072 kW pièce, **aucun intrant, aucune sortie** → ni route
  ni tuyau nécessaires, c'est ce qui rend le montage possible) → demande 524 288 pour 262 144 produits,
  **exactement 2 servis** ; les 2 derniers coupés avec le motif **`power`** ; puis le DERNIER remonté en
  tête **par les vrais boutons « Monter »** → **il devient servi et celui qu'il a doublé est coupé**.
  **Test 11** : la suite complète du lot 14.72 rejouée → **16/16** (le Port n'a pas bougé ; l'assertion
  qui échouait alors affiche désormais « VERDICT : le bouton titré « Monter » MONTE »).
  **Tests 12/13** : ordre modifié conservé au rechargement, et **save créée par la BASE 355 rechargée en
  356** → ordre ÉNERGIE conservé tel quel, ordre TRANSIT conservé, stocks intacts, `SAVE_VERSION` 31,
  **0 `tickError`, 20 s de jeu réel, 0 `pageerror`**.
  ⚠ **PIÈGE DE HARNAIS (nouveau)** : l'île de départ est **trop exiguë** pour une bande 3×6 de terre
  libre → un montage électrique de test doit **combler des tuiles d'eau** (`baseTerrain 'water'` +
  `terrain 'coast'`, le remblai du jeu) pour dégager la place, sinon la recherche de zone rend `null`
  et le test s'effondre sur `build.keys`. Et pour un déficit mesurable il faut des consommateurs
  **SANS intrants ni sorties** (`refroidisseur` en mode `sec`) : tout le reste sort de
  `energyConsumers` faute de desserte, et on mesure alors une liste VIDE.
  ⚠ **HORS PÉRIMÈTRE, non touché** : `moveEnergyPriority` (sa convention de signe reste celle de
  `movePriority`), le panneau Port, `energyConsumerList`, `cutToFitMode`, les titres, les sprites,
  le CSS, `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 355`, `GAME_VERSION = 'Alpha 14.72'`, `SAVE_VERSION = 31`.**
  Changement 14.72 (brief `BRIEFLOTCOURTpriorite`, **2 blocs**) : **les 4 boutons de réordonnancement
  des listes de priorité passent du glyphe au SPRITE** (`▲`/`▼` → `ui_monter`/`ui_baisser`) — priorité
  de transit du **Port** et priorité d'alimentation de l'**Énergie**. `SAVE_VERSION` INCHANGÉ, aucun
  champ persisté, aucune règle de simulation touchée. Base EXACTE (354 / 14.71 / 3 236 301 o).
  ⚠ **BRIEF PRÉ-COMPILÉ, 6ᵉ fois de suite** : **2/2 ancres uniques**, **2/2 hachages conformes AVANT
  application**, `node --check` 7/7 du premier coup, **delta +74 o EXACT**.
  (1) **Les glyphes ne rejoignent PAS `UI_ICON_BY_EMOJI`** (décision du brief, à ne pas « simplifier »)
  : `▲`/`▼` servent AILLEURS de caractères de mise en page (chevrons de repli `▾`/`▸`) — les faire
  basculer globalement toucherait des sites qui doivent rester typographiques. On appelle donc
  `uiIcon()` **directement** sur les 4 sites, **avec le glyphe en repli**. Mesuré après patch :
  `"▲"` et `"▼"` restent à **2 occurrences chacun**, mais **toutes les 4 sont désormais des
  replis de `uiIcon` — 0 glyphe NU**.
  ⚠ **PIÈGE DE MESURE** : le fichier stocke ces glyphes en **séquences d'échappement littérales**
  (`▲`, 6 caractères), pas en UTF-8 → un `grep` du vrai caractère `▲` renvoie **0** et fait
  conclure à tort que le patch a tout retiré.
  (2) **AUCUNE règle CSS ajoutée** : `.pp-c-pri button:disabled{opacity:.3}` existait déjà → le sprite
  désactivé reste **VISIBLE en opacité réduite**, il ne disparaît pas (exigence §6.8, mesurée :
  `opacity 0.3`, `16×16 px`, `visible true`). Les boutons passent d'un glyphe à `.6rem` (~10 px) à un
  sprite de 16 px : la ligne du Port fait 30 px, les boutons restent **centrés sur la ligne à 0,5 px**
  et le sprite est **centré dans son bouton à 0,00 px**.
  ⚠ **ANOMALIE PRÉEXISTANTE CONFIRMÉE, NON CORRIGÉE ICI — FERMÉE AU LOT 14.73** (hors périmètre de ce
  lot-ci : le brief écrivait noir sur blanc « le patch conserve cette inversion telle quelle ») : dans
  le **panneau Énergie**, le bouton titré
  **« Monter » DESCEND l'entrée**, et « Descendre » la monte. Le brief anticipait l'inversion des
  signes (`Monter` → `onMovePriority(+1)`, l'inverse du Port) et demandait de **« vérifier que monter
  monte »** : **il ne monte pas.** `moveEnergyPriority(key, +1)` fait `splice(i,1)` puis
  `splice(i+1,0,key)` → l'entrée descend d'un rang, alors que l'en-tête annonce « haut = servi en
  premier » et que `.ep-rank` numérote 1, 2, 3… de haut en bas. Les `disabled` sont cohérents avec le
  MOUVEMENT (`+1` désactivé sur la DERNIÈRE ligne) et donc **incohérents avec les TITRES** : à l'écran,
  la 1ʳᵉ ligne a sa flèche BAS grisée et la dernière sa flèche HAUT grisée — l'inverse de ce qu'on
  attend. **Contre-épreuve exécutée sur la base 354 non patchée : comportement IDENTIQUE** (`(9,14)`
  passe de la position 2 à la 3 en cliquant « Monter »), donc **ce n'est PAS une régression de ce lot**.
  ⚠ **MAIS ce lot la rend BEAUCOUP plus visible** : un tooltip « Monter » ne se survole pas au doigt,
  une **flèche ▲ se lit d'un coup d'œil**. Correctif d'une ligne si on veut le fermer : échanger les
  deux `title:` OU les deux signes `+1`/`-1` **et** les deux conditions `disabled` du bloc Énergie.
  Le **Port est correct** (vérifié : « Monter » sur la 2ᵉ ligne → elle passe 1ʳᵉ).
  (3) **§5 — L'AUDIT DE LA TABLE DEVIENT PERMANENT** (le brief l'institutionnalise, il était né hors
  brief au lot 14.68) : **vérifier que chacune des 49 entrées de `UI_ICON_BY_EMOJI` pointe vers un
  sprite RÉELLEMENT présent dans `SPRITE_DATA`**. Mode de défaillance vicieux : nom absent → `uiIcon`
  retombe sur son repli, qui est la **chaîne VIDE** quand l'appelant est `leadIconOf`/`iconLabel` →
  **le libellé perd son emoji SANS gagner d'icône**, rien ne casse, personne ne le voit. **À REJOUER À
  CHAQUE AJOUT DANS LA TABLE et à chaque suppression de sprite.** Mesuré sur 355 : **49 entrées,
  46 noms de sprite distincts, 0 manquant.**
  ⚠ **`ui_deplacer` RESTE ORPHELIN, ET C'EST NORMAL** (correction d'un inventaire erroné des lots
  précédents) : il n'a aucun site parce que **la fonction qu'il illustre n'existe pas** — il n'y a pas
  de déplacement de bâtiment dans le jeu (`onMove` est `onMovePriority`, tout autre chose). Les 3
  autres « sprites dormants » que le mémo listait sont en fait **UTILISÉS** : `ui_pause_logique` par
  `drawSprite` **sur le CANVAS** (marqueur de bâtiment coupé par un actionneur), `ui_mode_vitesse` et
  `ui_mode_productivite` par le sélecteur de mode d'antenne. L'inventaire précédent ne regardait que
  les appels `uiIcon()` et la table — il ratait le canvas et les appels indirects.
  ⚠ **Le logo `🏭` du splash reste un emoji** (décision du brief) : il est en **HTML statique, avant
  React**, à `font-size:56px` ; `ui_usine` est un 16×16 qu'il faudrait agrandir 3,5× — chunky à côté du
  titre. À revoir seulement si un logo à sa taille est produit.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + **4 assertions d'audit** + Chromium **15 assertions
  sur 16** (le seul KO est l'anomalie préexistante ci-dessus, contre-épreuvée sur la base) + **10
  assertions de non-régression** : **save créée par la BASE 354 puis rechargée en 355** → ordre de
  priorité ÉNERGIE conservé, ordre de priorité TRANSIT conservé, stocks et déblocage d'île intacts,
  `SAVE_VERSION` toujours 31, **0 `tickError`, 20 s de jeu réel, 0 `pageerror`**.
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) **`.pp-c-pri` est AUSSI la classe de la
  cellule d'EN-TÊTE** (« Prio ») du tableau du Port → toujours filtrer sur la présence d'un `<button>`,
  sinon l'en-tête compte comme une ligne et le test d'ordre compare deux fois la même chaîne ;
  (b) **`useGhostGuard` frappe encore** — un `.click()` DOM sur un bouton du Port juste après son
  ouverture est **AVALÉ** : amorcer le garde par un `pointerdown` dispatché **DANS** `.port-panel`
  avant de cliquer (le panneau Énergie, lui, ne l'a pas avalé — ne pas en déduire que le garde est
  inactif) ; (c) la liste de priorité du panneau Énergie est **VIDE tant qu'aucun consommateur
  électrique n'existe sur l'île** (`energyConsumerList` balaie la GRILLE) → forger des bâtiments à
  `power > 0` avant d'ouvrir le panneau ; (d) mesurer l'alignement d'un sprite contre `.pp-res-name`
  donne **−3,84 px** et fait crier au bug : le nom est sur la 1ʳᵉ des DEUX lignes de la cellule
  ressource alors que les boutons sont centrés sur la ligne ENTIÈRE — mesurer contre le **bouton** et
  contre la **ligne**, pas contre le texte.
  ⚠ **Taille : 3 236 301 → 3 236 375 o (+74 o EXACT** pour les 2 blocs, l'attendu au byte près) ;
  3 236 325 après bump, le nouveau `GAME_NOTES` étant plus court.
  ⚠ **HORS PÉRIMÈTRE, non touché** : `UI_ICON_BY_EMOJI` (aucune entrée ajoutée), `leadIconOf`,
  `uiIcon`, `iconLabel`, le CSS, l'inversion Monter/Descendre du panneau Énergie, le logo du splash,
  `ui_deplacer`, `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 354`, `GAME_VERSION = 'Alpha 14.71'`, `SAVE_VERSION = 31`.**
  Changement 14.71 (brief `BRIEF-ETAPE3-icones-gabarit`, **ÉTAPE 3 sur 3, la dernière du pack
  `archipelsprites3etapes`**) : **7 icônes `ui_*` rejoignent le gabarit commun de 193 pixels opaques.**
  `SAVE_VERSION` INCHANGÉ, **aucun champ persisté, aucune clé changée, AUCUN câblage** — remplacement
  de données pur. Base EXACTE annoncée par le brief : 350 / 14.67 ; base RÉELLE d'exécution : **353 /
  14.70** (les étapes 2 et 1 étaient déjà livrées) — le brief prévoit ce cas (« si la base a avancé, ne
  pas s'arrêter »), et **les 7 ancres sont sorties à `count == 1` sans la moindre adaptation** :
  l'étape 3 est réellement indépendante des deux autres, dans les deux sens.
  ⚠ **LE PIÈGE DU BRIEF, CONFIRMÉ EN PRATIQUE : `SPRITE_DATA` A DEUX FORMES DE DÉCLARATION** — un
  littéral d'objet `"clé":"data:…"` ET ~1330 affectations `window.__SPRITE_DATA__["clé"]="data:…";`.
  Les 7 icônes sont **réparties sur les deux** : 6 dans le littéral (`ui_astuce`, `ui_calculateur`,
  `ui_chaleur`, `ui_energie`, `ui_mode_vitesse`, `ui_production`), **1 seule en affectation**
  (`ui_pause_logique`). Mesuré : `grep 'window.__SPRITE_DATA__["ui_astuce"]='` renvoie **0**. Un patch
  uniforme aurait donc échoué **6 fois sur 7**, en silence. (Le même piège avait déjà mordu au lot
  14.65 sur un contrôle aller-retour par regex — c'est la **2ᵉ fois** : toute manipulation de
  `SPRITE_DATA` doit gérer les DEUX formes, sans exception.)
  (1) **Le défaut** : les icônes du pack partagent un masque de disque de **193 pixels opaques** sur
  256, identique bit à bit. Sept exceptions subsistaient. Les 6 à 172 px partagent **EXACTEMENT** le
  même écart (−23 / +2) : c'est un **gabarit antérieur**, pas six erreurs indépendantes ; le disque
  était d'un cheveu plus petit et légèrement décalé. `ui_energie` et `ui_chaleur` sont parmi les icônes
  les plus affichées du jeu. Le tableau du §1 du brief a été **retrouvé à l'identique sur la base 353**
  (172 opaques × 6 avec −23/+2, `ui_pause_logique` à 185 avec −8/+0 ; couleurs 26/12/16/14/26/13/6).
  ⚠ **LES GLYPHES N'ONT PAS ÉTÉ REDESSINÉS** (décision de l'auteur du pack, à ne pas « améliorer ») :
  chaque glyphe a été EXTRAIT de l'icône actuelle et reposé sur un disque reconstruit au gabarit. Une
  tentative de redessin avait donné un résultat inférieur (« la flamme devenait une maison, la
  calculatrice perdait ses touches »). **Vérifié à la capture avant/après** : l'ampoule reste une
  ampoule, la calculatrice garde ses touches, la flamme reste une flamme, l'éclair reste un éclair.
  ⚠ **La chute du nombre de couleurs n'est PAS une perte** : l'antialiasing des 2 icônes à 26 couleurs
  portait sur le **DISQUE**, pas sur le glyphe → leur passage à 5 couleurs ne perd rien de l'identité
  visuelle. Compte final mesuré : **6 pour `ui_chaleur`, 5 pour les six autres** (conforme au §5.7).
  (2) **AUCUN câblage** : les 7 clés sont inchangées et déjà appelées — 4 par `uiIcon()` direct, 3 via
  `UI_ICON_BY_EMOJI` — et le restent. `UI_ICON_BY_EMOJI`, `leadIconOf`, `uiIcon`, `iconLabel` et les
  tables i18n ne sont pas touchés.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + **les 7 PNG re-décodés depuis le HTML PATCHÉ** :
  SHA-256 **7/7 conformes** au pack, **16×16**, alpha **BINAIRE** (`{0, 255}`, aucune valeur
  intermédiaire), masque comparé bit à bit à `ui_port` → **193 px opaques, 0 pixel d'écart sur les 7**,
  **contour intégralement `#1E2128` sur 7/7**. + Chromium **8 assertions, 0 KO**.
  **Test 10 (le seul qui compte selon le brief — « un écart de 21 pixels ne se juge pas agrandi »)** :
  les 7 dessinées **À TAILLE RÉELLE (16 px, `imageSmoothingEnabled = false`, sans zoom)** à côté de
  `ui_port` / `ui_monter` / `ui_ok` / `ui_arret`, puis le disque **mesuré au pixel dans le canvas** :
  bbox `{top:1, bottom:15, left:1, right:15}` et **193 opaques — identique aux 4 références sur les 11
  icônes**. Même diamètre, même alignement, prouvé au pixel et pas à l'œil.
  **Test 11** : `ui_energie` rend bien la pastille ⚡ du HUD (16×16) et `ui_chaleur` le bouton
  Surchauffes (`.inv-heat-btn`), toutes deux reconnaissables à la capture.
  **Test 12** : diff des **1 502 clés de sprite** entre `HEAD` et le fichier patché → **exactement les
  7 attendues ont changé**, aucune autre ; et les **91 icônes `ui_*` décodent** toujours (0 échec).
  ⚠ **Taille : 3 237 578 → 3 236 302 o (−1 276 o EXACT**, l'attendu au byte près — le fichier RÉTRÉCIT,
  les icônes sont plus simples) ; 3 236 301 après bump, le nouveau `GAME_NOTES` étant 1 octet plus court.
  ⚠ **HORS PÉRIMÈTRE (§3), non touché** : toutes les autres icônes, `UI_ICON_BY_EMOJI`, `leadIconOf`,
  `uiIcon()`, les tables i18n, et **tout code de rendu** (aucune des 3 étapes du pack n'en touche).
- **État précédent : `GAME_BUILD = 353`, `GAME_VERSION = 'Alpha 14.70'`, `SAVE_VERSION = 31`.**
  Changement 14.70 (brief `BRIEF-ETAPE1-animation-remblai`, **ÉTAPE 1 sur 3, à intégrer APRÈS la 2**) :
  **les remblais des îles 1 à 5 s'animent** (5 sheets 128×32 + 5 entrées de table). `SAVE_VERSION`
  INCHANGÉ, **aucun code de rendu touché** — la table est le SEUL point de branchement.
  (1) **Le défaut** : le rendu tente `drawTileAnim` puis retombe sur `drawSprite`. Une tuile de
  remblai n'avait pas de sheet → elle restait **figée au milieu d'un littoral qui scintille à 3 fps**.
  À cette cadence l'œil ne lit pas « de la pierre, donc immobile », il lit **une tuile non chargée**.
  (2) **Amplitude MOITIÉ de celle du littoral** (arbitrage du brief, à ne pas « corriger ») : écart
  moyen 4,7 contre 10,2 pour `tile_i1_coast_breeze`. Un enrochement bouge moins qu'une végétation ;
  l'objectif est de supprimer la lecture « non chargée », pas de faire onduler un ouvrage de génie
  civil. L'asymétrie du cycle (départ et retour doux vers la frame 0) est obtenue **par
  construction** — chaque pixel reçoit un profil temporel nul en frame 0 → l'invariant est mécanique,
  pas espéré. Les joints sombres sont exclus de la gigue.
  ⚠ **PIÈGE SÉMANTIQUE de `TILE_ANIM_BY_KEY`** : la **CLÉ** est celle du sprite **STATIQUE**, la
  propriété **`cle`** celle de la **SHEET**. L'entrée existante `tile_i3_oil → 'tile_i3_petrole_breeze'`
  le prouve. Les entrées ajoutées sont donc `tile_i1_remblai: { cle: 'tile_i1_remblai_breeze', … }`
  et **non** `tile_i1_remblai_breeze: {…}`. Vérifié après patch : 5 clés statiques, 5 `cle` de sheet.
  ⚠ **NE PAS délimiter la table par une chaîne sentinelle** : elle se termine par `\n};` et **non**
  `\n  };` — une sentinelle erronée renvoie EN SILENCE un bloc qui court jusqu'à la fin du fichier.
  Délimitation faite par **parcours d'accolades** depuis `const TILE_ANIM_BY_KEY = {`.
  ⚠ **L'ÎLE 6 N'A PAS DE SHEET, ET C'EST VOULU** : `tile_i6_land`/`tile_i6_coast` n'en ont pas non
  plus et l'île 6 est absente de la table. Un remblai statique y est entouré de voisins statiques —
  **cohérent, ne pas « compléter » par symétrie**. Mesuré en jeu : sur l'île 6 le remblai est dessiné
  par `drawSprite` (`tile_i6_remblai`), aucune sheet de remblai n'y est demandée.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + les 2 ancres à `count == 1`, **table 16 → 21
  entrées**, **5/5 SHA-256 re-extraits du HTML patché identiques au pack**, sheets **128×32 alpha
  uniformément 255**, **invariant frame 0 == sprite statique : 5/5, les DEUX décodés depuis le HTML
  PATCHÉ** (et non depuis les fichiers sources), **delta +11 912 octets EXACT**.
  **En jeu** (espion `drawImage` enregistrant l'OFFSET SOURCE, donc la frame réellement dessinée) :
  zone 4×4 comblée sur l'île 1 → la sheet est utilisée et **les 4 frames sont parcourues**
  (`sx = 0, 32, 64, 96`), le littoral voisin s'anime en même temps ; **au zoom minimum, même
  résultat** (aucune retombée sur le statique) ; sur l'île 6, **aucune sheet de remblai**.
  ⚠ **HORS PÉRIMÈTRE** : les 16 sheets existantes, `drawTileAnim`, `drawSprite`, l'ordre de tentative
  des deux, les sprites statiques de remblai, l'île 6, l'île 7.
- **État précédent : `GAME_BUILD = 352`, `GAME_VERSION = 'Alpha 14.69'`, `SAVE_VERSION = 31`.**
  Changement 14.69 (brief `BRIEF-ETAPE2-retirage-i2`, **1 bloc — ÉTAPE 2 sur 3, à intégrer EN
  PREMIER**) : **la tuile `tile_i2_remblai` est retirée.** `SAVE_VERSION` INCHANGÉ, **aucun code
  touché** (remplacement de données, clé inchangée → aucun site d'appel à modifier).
  ⚠ **L'ORDRE 2 → 1 → 3 EST OBLIGATOIRE et n'est PAS un caprice** : la sheet `tile_i2_remblai_breeze`
  de l'étape 1 a sa **frame 0 calée sur la tuile RETIRÉE**. Intégrer l'étape 1 d'abord casserait
  l'invariant « frame 0 == sprite statique » au moment du retirage. L'étape 3 est, elle, réellement
  indépendante.
  (1) **Le défaut, mesuré sur la tuile d'origine** : **colonne 0 et colonne 31 identiques à l'octet
  près** — un joint du Voronoï tombait pile sur la couture verticale, écart au raccord **0,00** pour
  un écart interne moyen de 37,2. Le pavage restait correct mais installait une **ligne verticale
  tous les 32 px** sur une grande zone comblée. La nouvelle tuile mesurée **en jeu** (re-décodée du
  HTML patché) : raccord vertical **0,97×** l'écart interne, horizontal **0,98×** — exactement les
  valeurs du §1. Plus aucun bord identique à son opposé.
  ⚠ **`SPRITE_DATA` a DEUX formes de déclaration** (littéral d'objet ET ~1330 affectations
  `window.__SPRITE_DATA__["clé"]=…`). `tile_i2_remblai` est de la **seconde** : une regex qui ne voit
  que le littéral ne la trouve pas. Piège déjà rencontré au lot 14.65, re-confirmé ici.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + ancre à `count == 1` avant / **0 après**,
  `grep tile_i2_remblai` inchangé à 1 (la clé existe toujours), **SHA-256 re-extrait du HTML patché
  identique au pack** (`d0bbe1c1…`), PNG **32×32, alpha uniformément 255, 13 couleurs**, **delta
  +60 octets EXACT**. En jeu : zone **4×4 comblée sur l'île 2** + zoom minimum, espion `drawImage` →
  **`tile_i2_remblai` est bien la clé dessinée**, et **aucune autre tuile de remblai** ne l'est.
  ⚠ **HORS PÉRIMÈTRE** : les 5 autres tuiles de remblai, `tile_i2_coast`, les triangles
  `coast_tri_*`, toute logique de rendu.
- **État précédent : `GAME_BUILD = 351`, `GAME_VERSION = 'Alpha 14.68'`, `SAVE_VERSION = 31`.**
  Changement 14.68 (brief `BRIEFLIBELLESvague2`, **20 blocs**) : **20 libellés JSX de plus passent
  de l'emoji au SPRITE** — bandeaux de déficit (les 3 branches), fiche de centrale, chantier
  souterrain, boutons Densifier et Pause/Reprise, colonne « interdit » du Port, accumulateurs du
  panneau câble, en-tête des Options, export de sauvegarde, messages de mise à jour.
  `SAVE_VERSION` INCHANGÉ, **aucun champ persisté touché**. Base EXACTE (350 / 14.67 / 3 225 392 o),
  sur la branche `claude/big-patch-fix-o2jyh9`.
  ⚠ **BRIEF PRÉ-COMPILÉ, 5ᵉ fois de suite** : **20/20 ancres uniques**, **20/20 hachages conformes
  AVANT application**, **`node --check` 7/7 du premier coup**, **delta d'octets EXACT (+220)**.
  ⚠ **CHANGEMENT DE MÉTHODE CÔTÉ BRIEF, à réclamer** : la vague 1 plaçait les parenthèses à vue et
  avait cassé le fichier deux fois. Cette vague-ci a été produite par un outil qui **CALCULE
  l'étendue de l'argument React** (compte les délimiteurs, saute chaînes et gabarits, s'arrête à la
  virgule de plus haut niveau). Les 20 sites sont sortis d'un coup, sans reprise. **Le point de
  fermeture ne se devine pas, il se calcule.**
  (1) **Tous les blocs sont de la forme `expr` → `iconLabel(expr)`**, aucun code nouveau : le helper
  et la table datent des lots 14.65/14.67. Les 20 ancres sont des expressions COMPLÈTES, donc chaque
  ancre est une sous-chaîne de son remplacement → **`old_count` reste à 1 partout** après patch.
  (2) **LE GISEMENT EST ÉPUISÉ.** L'outil a classé tous les littéraux à emoji de tête : 20
  convertibles (ce lot), 26 déjà enveloppés, 5 arguments de `showToast` (déjà traités au rendu),
  9 valeurs de PROPRIÉTÉ (`afterToast:`, `title:`… — un attribut ne prend qu'une chaîne), 23 lignes
  de tables i18n (données, pas rendu), la table elle-même, 1 contexte indéterminable.
  ⚠ Les 9 `afterToast:` sont des chaînes de `TUTORIAL_STEPS` qui **finissent dans `showToast`** :
  les convertir serait une DOUBLE conversion. **56 littéraux subsistent, aucun n'est un oubli.**
  (3) **§4 — trois cas voulus, à ne pas « corriger »** : (a) `cfg.interdit ? '⛔' : '○'` — l'interdit
  devient un sprite, le ○ de l'état autorisé reste un GLYPHE (l'interdiction se voit, l'autorisation
  reste discrète) ; (b) deux libellés réduits à l'emoji SEUL (`✦ `/`🔒 ` du bouton Densifier, `🔒`
  d'un bouton verrouillé) → reste vide, sprite suivi d'une espace, sans conséquence ; (c) la fiche
  de centrale porte un **SECOND emoji 🔥 en MILIEU de chaîne** : seul celui de TÊTE est converti,
  `leadIconOf` ne voit que le premier code point — comportement attendu, vérifié.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + **`iconLabel` ré-extrait du fichier patché et
  exercé sous Node avec un React minimal** (**10 assertions, 0 KO** — le tableau du §6 : `ui_energie`
  / `ui_alerte` / `ui_chantier` / `ui_densifier` / `ui_arret` / `ui_batterie` / `ui_ok` /
  `ui_configurer` / `ui_refus`, plus `○` → texte brut) + Chromium **3 suites, 37 assertions, 0 KO**,
  **rejouées 2 fois sans flottement**, et les **7 suites des lots 14.65 → 14.67 rejouées en
  non-régression** (95 assertions, 0 KO).
  **Test 7** (emoji en milieu de chaîne) : la ligne de puissance de la centrale rend le sprite
  énergie en tête, **le 🔥 du milieu reste un emoji**, et la paire « puissance · chaleur » est
  intacte. **Test 8** (valeurs interpolées) : « Travaux [sprite] Nv.2 · 42% · en attente de
  l'élévateur » — pourcentage ET état conservés. **Test 16** (découpe) : sur les 20 sites, aucun
  texte tronqué ni dupliqué ; les valeurs interpolées vérifiées une par une (nombre de réseaux,
  pourcentages, paire kWh « 8,39 / 16,8 GWh · 50% », libellé de version « Alpha 99.9 »).
  ⚠ **AUDIT AJOUTÉ (hors brief), 0 défaut** : les **49 entrées** de `UI_ICON_BY_EMOJI` pointent
  TOUTES vers un sprite existant. C'est le contrôle qui manquait — un nom absent de `SPRITE_DATA`
  ferait retomber `uiIcon` sur son repli (**la chaîne VIDE**), donc le libellé perdrait son emoji
  SANS gagner d'icône. À rejouer à chaque ajout dans la table.
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) **le site 🚧 est la ligne TRAVAUX**
  (amélioration en cours), pas la construction simple — il faut `construction.up` ; sans lui on tombe
  sur le libellé « en construction · N% » de la ligne Vitesse, **qui n'a pas d'emoji** et fait
  conclure à tort à un sprite manquant ; (b) **un Proxy qui rend un sprite pour TOUTE clé masque
  l'absence réelle** dans le test isolé — c'est ce qui m'a fait croire à un bug de `iconLabel` alors
  que le site testé n'était pas le bon ; (c) laisser une **centrale 2×2** posée fait retomber le tap
  suivant dessus (l'emprise déborde de la tuile visée) → nettoyer les 4 tuiles entre deux tests ;
  (d) le panneau **Sauvegarde s'ouvre DEPUIS les Options**, pas depuis le HUD ; (e) le presse-papier
  exige `grantPermissions(['clipboard-read','clipboard-write'])`, sinon `copied` reste faux et la
  branche `✓` n'est jamais rendue ; (f) le détecteur de MAJ se pilote proprement en **forgeant la
  réponse de `fetch`** (le vrai chemin `checkUpdate → setUpd → updStatus` est alors exercé).
  ⚠ **DEUX LIBELLÉS NON ATTEIGNABLES EN NAVIGATEUR** (couverts par le test isolé seulement) :
  `⬇ Mettre à jour maintenant` et `❌ Échec du téléchargement` sont gatés sur **`NATIVE_UPDATER`**,
  qui n'existe que dans la coquille Android.
  ⚠ **Taille : 3 225 392 → 3 225 608 o (+216 o)** — le CODE seul pèse **+220 o** (exactement
  l'attendu) ; le total est plus petit de 4 octets parce que le nouveau `GAME_NOTES` est plus court.
  ⚠ **HORS PÉRIMÈTRE (§3), non touché** : les 9 valeurs de propriété, les 23 lignes de tables i18n,
  les emoji en MILIEU de chaîne, et les 4 sprites dormants (`ui_deplacer`, `ui_mode_vitesse`,
  `ui_pause_logique`, `ui_mode_productivite` en icône de bouton).
- **État précédent : `GAME_BUILD = 350`, `GAME_VERSION = 'Alpha 14.67'`, `SAVE_VERSION = 31`.**
  Changement 14.67 (brief `BRIEFLIBELLESvague1`, **13 blocs**) : **11 libellés JSX passent de l'emoji
  au SPRITE** (bandeaux Alertes / Surchauffes / déficit électrique, boutons du Collisionneur, bouton
  de forage, en-tête Production, puissance de la chaîne du Calculateur, titre de densification,
  les 2 états de l'accumulateur). `SAVE_VERSION` INCHANGÉ, **aucun champ persisté touché**.
  Base du brief EXACTE (349 / 14.66 / 3 224 561 o), sur la branche `claude/big-patch-fix-o2jyh9`.
  ⚠ **BRIEF PRÉ-COMPILÉ, 4ᵉ fois de suite** : **13/13 ancres uniques**, **13/13 hachages conformes
  AVANT application**, **delta d'octets EXACT (+1 030 au byte près)**. Aucune adaptation.
  (1) **§L1 — nouveau helper `iconLabel(txt, cls)`**, posé juste avant `uiIcon`. Il réutilise la
  MÊME table que les toasts (`UI_ICON_BY_EMOJI` + `leadIconOf`, lot 14.65) : aucune clé i18n touchée,
  les ternaires sont couverts d'office (on lit la chaîne RÉSOLUE), et le **repli est intégral** —
  un emoji absent de la table renvoie le texte INCHANGÉ.
  ⚠ **Pourquoi site par site et pas un entonnoir** : les toasts ont un site de rendu UNIQUE ;
  les libellés n'en ont AUCUN. **55 libellés portent encore un emoji de tête sur la base 349**, sous
  55 formes différentes (concaténations, ternaires imbriqués, arguments React séparés). Chacun
  demande de lire l'étendue exacte de l'expression pour poser la parenthèse fermante — c'est
  exactement la faute qui avait donné une page blanche au lot A. **Ce lot en convertit 11 ; les 44
  restants sont une 2ᵉ vague délibérée.**
  ⚠ **UN CAS EST STRUCTURELLEMENT INCONVERTIBLE** : `placeholder: "🔍 " + I18N.t("Rechercher un
  bâtiment…")` — un attribut HTML ne prend qu'une chaîne, jamais un nœud React. Il gardera son emoji.
  (2) **§L2 — `.lbl-ico`**, jumelle de `.toast-ico` (`1em`, `vertical-align:-.12em`, pixelated).
  (3) **8 des 13 blocs ont une ancre qui est une SOUS-CHAÎNE de leur remplacement** (L1, L2, L3, L4,
  L6, L7, L9, L10) → leur `old_count` reste à **1** après patch. Ce n'est pas un échec ; seuls
  L5, L8, L11, L12, L13 tombent à 0. Vérifié explicitement à l'aller-retour.
  ⚠ **ÉCART DE LIBELLÉ DANS LE BRIEF, sans conséquence** : le §5 nomme L6 « puissance de recherche »
  et le test 14 « Panneau Recherche ». Le site réel est **`.calc-power` du CALCULATEUR**
  (`iconLabel("⚡ " + fmtPower(res.power))`, `res` = résultat de `computeProductionChain`). Testé là.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + **`iconLabel` EXTRAIT du fichier patché et exercé
  sous Node avec un React minimal** (`iconlabel.js` : 4 fragments, **11 assertions, 0 KO**) — les
  8 entrées du tableau du §6 (`ui_alerte`/`ui_chaleur`/`ui_energie`/`ui_reprendre`/`ui_pause`/
  `ui_verrou`/`ui_mine`/`ui_batterie`), les 2 replis, et la classe **`ui-ico lbl-ico`**.
  + Chromium **3 suites, 39 assertions, 0 KO**, **rejouées 2 fois sans flottement** (la suite du
  Collisionneur rejouée 3 fois après stabilisation), et les **5 suites des lots 14.65/14.66 rejouées
  en non-régression** (63 assertions, 0 KO).
  **Test 8** (concaténation) : « ⚡ » + `a.nets` + libellé → sprite énergie ET **le nombre 2 est
  conservé** (« 2 réseaux en déficit · 0% batterie »). **Test 10** (ternaire à 3 branches) : les
  **trois** branches du bouton « Lancer la séquence » rendent chacune LEUR sprite — `logic` →
  `ui_verrou`, `goal` → `ui_verrou`, prêt → `ui_reprendre` — et le bouton marche/arrêt rend
  `ui_reprendre` / `ui_pause` selon `colOn`. **Test 15** (repli) : 🏝️ dans l'Aide s'affiche
  toujours en emoji, **0 icône vide**. Aucun texte tronqué ni dupliqué nulle part.
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) **une astuce en file recouvre le
  CANVAS** — son `.tip-illu-canvas` intercepte le tap et `elementFromPoint` le confirme ; purger en
  boucle sur `.tip-popup button` (pas seulement `.tip-ok`) AVANT chaque tap ; (b) **recentrer la
  caméra puis viser immédiatement RATE la tuile** : `clampPan` recale `cam.x`/`cam.y` à la frame
  suivante → relire les coordonnées APRÈS l'attente, et réessayer ; (c) **le tick reconstruit
  `game.wireInfo` PUIS rend dans la même passe** — une valeur forgée en `setInterval` est
  systématiquement écrasée avant le rendu : pour tester l'alerte énergie, remplacer la fonction
  `activeEnergyAlerts` (accessible via `window`, déclaration de fonction d'un script classique),
  le chemin de RENDU testé reste le vrai ; (d) le **sol de tunnel de l'île 7 est `coast`, pas
  `land`** (`buildIslandTiles` promeut toute terre touchant de l'eau) ; (e) le bouton
  **Calculateur est SOUS l'inventaire OUVERT** ; (f) le panneau du Collisionneur ne se re-rend qu'au
  bump du HUD → relire en boucle courte (1 flottement observé sur 2 passes avant correction).
  ⚠ **Taille : 3 224 561 → 3 225 392 o (+831 o)** — le CODE seul pèse **+1 030 o** (exactement
  l'attendu, commentaires du brief inclus) ; le total est plus PETIT parce que le nouveau
  `GAME_NOTES` est plus court que celui de la 14.66.
  ⚠ **HORS PÉRIMÈTRE (§3), non touché** : les **44 libellés restants** (2ᵉ vague), le `placeholder`
  de recherche (inconvertible), les **9 emoji en MILIEU de chaîne** (`✓`, puces `●`/`○` — `leadIconOf`
  ne les voit pas), et les 4 sprites dormants (`ui_deplacer`, `ui_mode_vitesse`, `ui_pause_logique`,
  `ui_mode_productivite` en icône de bouton) qui demandent un `uiIcon()` posé à la main.
- **État précédent : `GAME_BUILD = 349`, `GAME_VERSION = 'Alpha 14.66'`, `SAVE_VERSION = 31`.**
  Changement 14.66 (brief `BRIEFFORMATSNUMERIQUES`, **15 blocs**) : **les grands nombres deviennent
  cohérents partout (séparateur de milliers sous le seuil, scientifique OU préfixe SI au-dessus,
  seuil réglable), et le panneau Énergie gagne une ligne « Dimensionnement » à trois états.**
  `SAVE_VERSION` INCHANGÉ, **les 2 champs ajoutés sont OPTIONNELS avec repli** (`numFormat`,
  `numThreshold` dans `uiPrefs`). Base du brief EXACTE (348 / 14.65 / 3 216 105 o).
  ⚠ **BRIEF PRÉ-COMPILÉ, 3ᵉ fois de suite, et ça se voit** : **15/15 ancres uniques du premier coup,
  15/15 hachages conformes AVANT application, delta d'octets EXACT (+8 402 au byte près, mesuré
  avant le bump)**. Aucune adaptation.
  (1) **§N1 — `fmtInt` devient le formateur PILOTABLE.** Sous le seuil : **toujours** le séparateur
  de milliers (`toLocaleString('fr-FR')` → espace fine insécable U+202F). Au-dessus : `sci`
  (`2,35e6`, défaut) ou `si` (`2,35 M`). Seuil réglable 1e3 → 1e9 ou **`Infinity` (« jamais »)**.
  ⚠ **`fmtInt` est HORS React** : il est appelé depuis des fonctions qui n'ont accès ni au state ni
  à `game` → la source de vérité est une paire de **variables de MODULE** (`NUM_FORMAT`,
  `NUM_THRESHOLD`) poussée par **`setNumPrefs`**. Le state React ne sert qu'à redessiner ; c'est
  pourquoi `chooseNumFormat` appelle `setNumPrefs` **ET** `setNumFormat`. Oublier l'un des deux
  donne soit un affichage figé, soit un `<select>` qui ne reflète plus la valeur.
  ⚠ **Pourquoi SI et pas k/m/b/t** : en français « b » = billion = 1e12, en anglais 1e9 — un stock
  de **2,57e12** (relevé sur une partie réelle) s'écrirait « 2,57 b » avec DEUX sens. Le jeu affiche
  déjà « 285 GW » et « 8,39 GWh » : le SI est la seule écriture cohérente avec l'existant.
  ⚠ **Le seuil EXPLICITE reste prioritaire** : `fmtInt(n, thresh)` garde son paramètre pour les
  appelants qui imposent un seuil ; seuls `fmtPort` et `fmtRateSci` passent désormais sans, et
  suivent donc le joueur.
  (2) **§N5 — `formatCost` ne formatait RIEN** : `` `${v} ${RES_SHORT[k]}` `` sortait la valeur
  BRUTE. C'était la source des nombres à rallonge du **menu Améliorer**, du bouton **« Aligner »**,
  des boutons **V+/V−** du panneau réseau et de **3 toasts** — **20 sites d'appel corrigés par une
  seule ligne** (passage par `fmtPort`). Mesuré : `{acier: 2345000}` → « 2,35e6 acier ».
  (3) **§N3 — `fmtRate` : deux défauts d'un coup.** Les milliers n'étaient PAS séparés (un débit de
  12 345/s s'écrivait « 12345 » **juste à côté** d'un stock « 12 345 », dans le même panneau) et la
  décimale était un **point** (« 12.34 ») là où tout le jeu met une virgule.
  (4) **§N6/N7 — `fmtPower`/`fmtEnergy` montent jusqu'au PW/PWh** : la chaîne s'arrêtait au
  gigawatt et imprimait ensuite une mantisse non séparée (2,57e12 kW → « 2570000 GW »). Latent,
  atteignable en fin de partie longue. Mesuré : `fmtPower(2.57e12)` = **2,57 PW**.
  (5) **§N8/N9 — persistance.** ⚠ **`Infinity` NE SURVIT PAS à `JSON.stringify`** (il devient
  `null`) → le seuil « jamais » est sérialisé en **chaîne `'inf'`** et reconverti à la lecture.
  ⚠ **`setNumPrefs` DOIT être appelé dans `loadSave`** : sans lui la save serait relue mais
  l'affichage garderait les défauts jusqu'au premier changement d'option (vérifié : après
  rechargement, `fmtPort(12345)` rend « 12,3 k » dès le 1ᵉʳ rendu).
  (6) **§N13/N14 — nouveau helper `selRow`** (ligne d'option à `<select>`, gabarit de la ligne
  « Langue ») + les 2 lignes **« Grands nombres »** et **« Seuil de bascule »**. Un `<select>` et
  non des boutons segmentés **parce que le seuil a SIX choix**, ce qui déborderait sur mobile.
  ⚠ **N14 doit être appliqué AVANT N13** (`selRow` doit exister avant d'être appelé).
  (7) **§N15 — ligne « Dimensionnement » à 3 états** : `gross < ideal.prod` → **sous-dimensionné**
  (rouge, les accus se vident) ; `ideal.prod ≤ gross ≤ demMax` → **optimal** (vert, les accus
  absorbent la bosse) ; `gross > demMax` → **au-dessus du pic** (ocre). Le vert/rouge d'avant ne
  montrait qu'UNE borne ; il en manquait la symétrique.
  ⚠ **FORMULATION VOLONTAIREMENT MESURÉE, à ne pas « simplifier »** : au-dessus du pic les
  accumulateurs ne sont **PAS inutiles**, ils restent le tampon des PANNES (centrale à court de
  combustible, réacteur qui trippe, zone d'antenne qui s'éteint sur le gate `pwrAvg`). L'infobulle
  dit donc « ne servent plus qu'**en cas de panne** » — écrire « inutiles » pousserait le joueur à
  démonter une sécurité. **Testé explicitement** : le mot « inutile » est ABSENT de l'infobulle.
  ⚠ La ligne est gatée sur **`showSpread`**, comme les 3 lignes « idéales » qui la suivent : sans
  oscillation un accumulateur ne sert à rien et les cibles vaudraient 0.
  ⚠ **HORS PÉRIMÈTRE (§4), non touché** : `fmtSig`, `fmtHeat`, `fmtR`, `fmtN` — ils affichent des
  GRANDEURS à 3 chiffres significatifs avec unité, pas des quantités, et ne suivent donc pas la
  préférence ; `fmtPower`/`fmtEnergy` gardent leurs préfixes d'unité (unités physiques, pas une
  convention d'affichage) — seule leur table est étendue.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + **chaîne de formatage EXTRAITE du fichier patché
  et exercée isolément sous Node** (`fmtchain.js` : 6 fragments, **22 assertions, 0 KO**) — le
  tableau du §7 est retrouvé aux 14 cases (`999`/`12 345`/`99 999`/`1e5`/`2,35e6`/`8,61e9`/`2,57e12`
  et `100 k`/`2,35 M`/`8,61 G`/`2,57 T`), plus seuil `Infinity` → `2 565 633 264 880`, seuil 1e3 en
  SI → `12,3 k`, `fmtRate` 3 cas, `fmtPower` 2 cas. + Chromium **3 suites, 40 assertions, 0 KO**,
  **rejouées 2 fois sans flottement**, et les **3 suites du lot 14.65 rejouées en non-régression**
  (35 assertions, 0 KO).
  **Test 6** (le défaut d'origine du lot) : `formatCost` ne sort plus AUCUNE valeur brute.
  **Test 10** : réglés → sauvegardés (`uiPrefs.numFormat: 'si'`, `numThreshold: 1000`) → rechargés
  → appliqués **dès le premier rendu** ; et le round-trip de `Infinity` passe bien par `'inf'`.
  **Test 15** : au-dessus du pic → ocre, infobulle « ne servent plus qu'en cas de panne », **mot
  « inutile » absent**. **Test 17** : save créée par la **BASE 348** rechargée en 349 → stock intact,
  0 `tickError`, 20 s de jeu réel ; **test 11** : cette même save (sans les 2 champs) retombe sur
  `sci` / 1e5, soit l'affichage d'avant.
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) **le panneau Énergie doit être
  OUVERT AVANT de forger `game.energy`** — une fois l'état forgé, la pastille ⚡ change de largeur au
  re-rendu et le vrai clic souris rate sa cible (`boundingBox` périmée) ; (b) le panneau ne se
  re-rend qu'au **bump du HUD** → relire en boucle courte jusqu'à ce que le rendu ait rattrapé l'état
  forgé, sinon on lit le rendu PRÉCÉDENT (3 faux KO d'affilée) ; (c) une **astuce en file peut
  refermer le panneau** au milieu du test → purger et ré-ouvrir avant chaque lecture ; (d) comparer
  des nombres formatés exige de normaliser **U+202F ET U+00A0 ET U+2009** (`toLocaleString('fr-FR')`
  rend l'espace FINE insécable, pas une espace ordinaire) ; (e) le sélecteur du panneau Énergie est
  **`.stock.energy`**, pas `.stocks`.
  ⚠ **Taille : 3 216 105 → 3 224 561 o (+8 456 o)** — dont **+8 402 pour les 15 blocs** (exactement
  l'attendu, les commentaires de décision étant déjà inclus dans les blocs du brief), le reste étant
  le bump et `GAME_NOTES`.
- **État précédent : `GAME_BUILD = 348`, `GAME_VERSION = 'Alpha 14.65'`, `SAVE_VERSION = 31`.**
  Changement 14.65 (brief `BRIEFINTEGRATIONBicones`, **INTÉGRATION B — 7 chantiers, tous livrés**) :
  **les emoji de tête des toasts, des astuces et de l'Aide deviennent des SPRITES (26 icônes neuves
  + 9 arts refaits), et le clamp de chaleur se ré-arme à chaque changement de plafond.**
  `SAVE_VERSION` INCHANGÉ, **aucun champ persisté ajouté** (`heatCapSeen` est transitoire).
  Base 347 / 14.64 / 3 204 873 o EXACTE ; **16/16 ancres uniques**, **14/14 SHA-256 de bloc
  conformes**, **35/35 SHA-256 de PNG conformes**.
  ⚠ **LE PACK EST ARRIVÉ EN 2 TEMPS** : la 1ʳᵉ session n'a reçu que le `.md` (le lot A avait consommé
  `archipel-sprites.zip` sans le commiter). **Livraison SUSPENDUE au chantier 0** plutôt que de
  générer un art de substitution — mesuré alors : 22 des 48 sprites de la table existaient déjà, les
  26 manquants étaient EXACTEMENT ceux de C1a, et le repli de C3 est la **chaîne VIDE** → un toast
  `❌ Manque acier` aurait perdu son emoji. **C'était le bon arbitrage** ; le zip fourni ensuite a
  débloqué C1a→C5 **sans la moindre adaptation**.
  (1) **§C0 — LE DRAPEAU DEVIENT UNE MÉMOIRE DU PLAFOND.** `heatCapAdj` était un booléen à **usage
  unique**, consommé au PREMIER `processHeat`. Or le gate `pwrAvg` de la 14.63 garantit que la zone
  d'antenne est **ÉTEINTE au tick 1** après un chargement → le plafond y est calculé **NON boosté**,
  donc le **plus GRAND** : le clamp ne trouve rien à raboter et **se dépense pour rien**. Au tick 2 la
  zone s'allume, le plafond **RÉTRÉCIT**, et la protection n'existe plus → **jauge bloquée à 135 %**.
  `bld.heatCapSeen = cap` → le clamp se ré-arme à **chaque changement** de plafond (allumage de zone,
  amélioration, densification, chargement) et ne fait rien quand il n'y a rien à raboter.
  ⚠ **Pourquoi le plafond RÉTRÉCIT à l'allumage** : depuis le lot A, `heatEmitMaxOf` part de
  `meanPower` pour un bâtiment boosté. Ratio mesuré = `meanPower/nominalPower × (1 + antElecBoost)`
  = **0,5625 × 1,2 = 0,675** pour une antenne Nv.1 sur une sigmoïde 1→8. **D'où le seuil de 67,5 %**
  du §8.17 : en dessous, le plafond ne rétrécit pas assez pour que le clamp ait à mordre.
  ⚠ **Deux effets de bord cherchés, aucun trouvé** (mesurés) : cycler le plafond **CONVERGE en UN
  cycle** — 22,1616 → 11,54736 au 1ᵉʳ rétrécissement puis **identique à 1e-12 sur 10 changements**
  (le clamp ne mord que si la chaleur DÉPASSE : pas de pompe à chaleur) ; et quand le plafond
  **GRANDIT** le clamp ne remonte **jamais** la chaleur.
  (2) **§C1a/C1b — 35 PNG.** 26 icônes NEUVES injectées en assignations
  `window.__SPRITE_DATA__["ui_…"]=…` (l'ancre est le commentaire des spritesheets) + **9 arts
  REFAITS** dans le grand littéral (`ui_alerte`, `ui_baisser`, `ui_batiment`, `ui_demolir`,
  `ui_mode_productivite`, `ui_monter`, `ui_port`, `ui_reparation`, `ui_route`) — ces 9-là étaient
  **déjà affichés** par la barre d'outils et le HUD, ils changent donc d'aspect immédiatement.
  ⚠ **Deux formes de déclaration cohabitent** dans `SPRITE_DATA` (grand littéral `"clé":"data:…"` ET
  assignation `window.__SPRITE_DATA__["clé"]="data:…"`) : un contrôle aller-retour par regex DOIT
  gérer les deux, sinon il conclut à tort que 26 clés sont absentes (m'est arrivé).
  (3) **§C2 — `UI_ICON_BY_EMOJI` (49 entrées) + `leadIconOf(msg)`**, posés juste avant `uiIcon`.
  `leadIconOf` lit le **premier code point** (`codePointAt`, donc les emoji hors BMP passent), saute
  un éventuel **sélecteur de variante U+FE0F** puis les espaces, et rend `{name, rest}` ou `null`.
  ⚠ **PIÈGE DU §6, vérifié** : la flèche de l'astuce `traverser` est **U+219D** (↝), pas U+21DD —
  `grep` confirme 0 occurrence de U+21DD dans le fichier. Une table écrite au jugé aurait raté
  cette entrée en silence.
  (4) **§C3/§C4a/§C4b/§C5 — trois sites de rendu.** Toast (`toast.msg`), icône du popup d'astuce et
  icône des cartes de l'Aide (`tip.icon`). **Le repli diffère et c'est voulu** : le toast retombe sur
  la chaîne d'origine (l'emoji est DANS le message), les astuces retombent sur `tip.icon` puis `💡`.
  **Aucune des 48 entrées de `GAME_TIPS` n'est retouchée** : leur champ `icon` reste un emoji, c'est
  la table qui traduit au rendu. CSS : `.toast-ico{width:1em;height:1em;vertical-align:-.12em}`.
  ⚠ **COUVERTURE MESURÉE : 42 astuces sur 48** (le brief annonçait 44/52 — `GAME_TIPS` en compte 48
  au runtime). Les **6 non couvertes** sont exactement celles dont le §3 déclare qu'aucun art n'est
  livré (🏝 🚢 🛢 🔗 ⚗) → **la couverture est complète au regard de l'art disponible**, et une astuce
  sans icône garde son emoji (vérifié en jeu : 🏝️ s'affiche tel quel dans l'Aide).
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **8 suites, 91 assertions, 0 KO**,
  **rejouées 2 fois sans flottement**. **§8.9** (le test qui valide le §3) : les **DEUX branches d'un
  gabarit ternaire** `⬆`/`⬇` rendent chacune LEUR sprite, et les deux data-URI sont **différentes** —
  c'est la preuve que traduire à l'AFFICHAGE (et non réécrire les 300 `showToast`) couvre les
  messages construits. **§8.13** : la suite ENTIÈRE rejouée en **locale EN** → le sprite apparaît sur
  la chaîne TRADUITE (« Missing acier »), le texte traduit est intact, aucune régression i18n.
  **§8.17 sur un RECHARGEMENT RÉEL** : zone éteinte au 1ᵉʳ tick (plafond 7,68), allumée au suivant
  (plafond 5,184 = **0,675×**), jauge qui **serait à 122 %** sans le clamp → **ne dépasse jamais
  100 %**, **aucun trip**. §8.18 contre-épreuve : `heatCapSeen` pré-positionné → le clamp ne mord pas,
  la chaleur RESTE à 135 % (et le bâtiment en marche **s'endommage**) — c'est bien le clamp qui
  protégeait. Contrôles du §7 : **16/16 ancres à `count == 1`**, **14/14 blocs re-extraits au SHA-256
  conforme**, **35/35 PNG re-décodés depuis le HTML patché identiques octet à octet au pack**
  (16×16, 193 px opaques, alpha binaire, 5 couleurs — 6 pour `ui_route`).
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) **un bâtiment en PAUSE est sauté EN
  TÊTE de la boucle bâtiment** → son `antennaBuff`/`antennaProd` n'est **jamais remis à jour** et son
  plafond reste FIGÉ : la zone semble ne jamais s'éteindre. **Idem pour un bâtiment AFFAMÉ.** Pour
  cycler une zone il faut donc que le bâtiment TOURNE — donc un vrai refroidissement ; (b) le
  **Refroidisseur en mode `sec`** (`bld.cool = {sel:'sec'}`) absorbe 0,5 MJ/s **sans aucun fluide** :
  c'est le seul refroidissement montable sans réseau de tuyau jusqu'au port ; (c) **la migration de
  palier (13.27) remonte au Nv.11 tout bâtiment de `TIER_STEP` au rechargement** → `centrale_
  enrichissement_v2` voit son plafond ×1024 et tous les seuils calculés avant la save deviennent
  faux. Prendre une source **hors palier** : `presse_uhp` (sigmoïde 128→1024, `heatCap`, ratio 0,675,
  émission faible) — vérifié `TIER_STEP['presse_uhp'] === undefined` ; (d) `usine_moteur_nuc` a une
  émission **PLATE** dans `heatEmitMaxOf` → son plafond **ne rétrécit PAS** (ratio 1) : inutilisable
  pour ce test ; (e) couper un câble **à l'intérieur d'un long `page.evaluate` asynchrone** ne prend
  pas — piloter les mutations depuis Node, avec les attentes entre deux `evaluate` ;
  (f) **`showToast` vit dans le scope d'App** et n'est PAS exposé par `window.__ui()` (comme
  `askPortFor` avant lui). Sonder les hooks de la fibre React est **DANGEREUX** (écrire dans le hook
  `info`/`upgrade` fait planter le rendu). La voie propre : **envelopper `React.useState` dans un
  `addInitScript`** — l'UMD fait `window.React = {}` PUIS remplit l'objet, donc il faut un accesseur
  sur `window.React` **ET** un accesseur sur `React.useState` ; on enregistre les paires
  `[état, setter]`, on déclenche **un vrai toast du jeu** (`tryPlace` sur de l'eau, verbeux), et le
  hook dont l'état vaut `{msg, color, key}` désigne `setToast`. Le jeu déstructure
  `const {useState} = React` **une seule fois** au chargement du module : l'enveloppe doit être posée
  avant ; (g) un toast s'efface tout seul au bout de 1 800 ms et un toast de jeu peut s'intercaler →
  relire en boucle courte, et **comparer les data-URI ENTIÈRES** (deux sprites 16×16 partagent leurs
  60 premiers caractères de base64 — un `slice(0,60)` donne un faux KO).
  ⚠ **Taille : 3 204 873 → 3 216 105 o (+11 232 o)** — dont **+11 173 pour le code** ; le brief
  annonçait +10 334, l'écart de **+839 o est le commentaire de décision du §C0** exigé par les
  conventions du projet (11 lignes), le reste étant le bump et `GAME_NOTES`.
  ⚠ **HORS PÉRIMÈTRE, non touché** : les 4 sprites dormants (`ui_deplacer`, `ui_mode_vitesse`,
  `ui_pause_logique`, `ui_mode_productivite` en icône de bouton), les emoji au MILIEU d'un libellé,
  les sheets `_breeze` des tuiles de remblai, `heatEmitMaxOf`, `HEAT_CAP_SECONDS`, `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 347`, `GAME_VERSION = 'Alpha 14.64'`, `SAVE_VERSION = 31`.**
  Changement 14.64 (brief `BRIEFINTEGRATIONAremblai`, **INTÉGRATION A — 2 chantiers indépendants**) :
  **(A) le REMBLAI devient VISIBLE (6 tuiles dédiées) ; (B) le plafond de chaleur se réaligne sur
  `meanPower`.** `SAVE_VERSION` INCHANGÉ, **aucun champ persisté ajouté** (`heatCapAdj` est
  transitoire — la sérialisation des placements est une liste blanche). Base du brief EXACTE
  (346 / 14.63 / 3 199 226 o).
  ⚠ **BRIEF PRÉ-COMPILÉ, ET ÇA S'EST VU (2ᵉ fois de suite)** : **5/5 ancres uniques du premier coup,
  4/4 hachages de remplacement conformes AVANT application, 6/6 SHA-256 des PNG conformes, et delta
  d'octets EXACT (+5 952 au byte près, mesuré avant le bump)**. Aucune adaptation. **C'est la méthode
  à réclamer.**
  ⚠ **LE PACK N'ÉTAIT PAS JOINT À LA SESSION** (seul le `.md` est arrivé) : les 6 PNG sont
  introuvables dans les 6 zips du dépôt. Livraison **suspendue et pack redemandé** plutôt que de
  générer un art de substitution — les SHA-256 du §6 n'auraient jamais concordé. Les 4 blocs de CODE
  ont été appliqués et validés pendant l'attente (ils dégradent proprement : A2 retombe sur
  `tile_i<N>_coast`, A3 sur l'emoji).
  (1) **§A — LE REMBLAI SE VOIT.** Une tuile comblée (`isFilledTile` : `baseTerrain 'water'` +
  `terrain 'coast'`) prend une **clé dédiée par île**, `tile_i<N>_remblai` (6 tuiles, îles 1 à 6).
  ⚠ **La clé est PRIORITAIRE sur le calcul d'adjacence, et c'est tout le point** : la clé normale sort
  de `coastIsCoast()`, **pas de `t.terrain`** → une tuile comblée que le joueur finit par entourer de
  terre serait redessinée en `land` et **le remblai disparaîtrait**. Mesuré : remblai encerclé de terre
  → toujours `tile_i1_remblai`, contre-épreuve même voisinage sans le drapeau → `tile_i1_land`.
  Une clé par île suffit donc, là où l'adjacence en aurait demandé 12.
  ⚠ **Île 7 exclue** : la branche `isTun` passe avant. Mesuré : tuile percée → `tile_i7_land`, les
  anneaux de coût (`drillLayer`) restent lisibles.
  ⚠ **LES TUILES DE REMBLAI SONT STATIQUES** (constat non prévu par le brief) : il n'existe pas de
  sheet `tile_i<N>_remblai_breeze`, alors que `tile_i{1..5}_coast` sont routées vers une animation de
  brise par `TILE_ANIM_BY_KEY`. Un remblai ne frissonne donc pas au milieu d'un littoral qui ondule.
  Défendable (un enrochement n'a pas de raison de miroiter) mais **c'est une différence visible** —
  à trancher si elle choque. C'est aussi ce qui explique qu'un espion `drawImage` mappé sur le seul
  `SPRITE_DATA` voie la clé du remblai mais **pas** celle de la côte des îles 1-5 (elle passe par
  `drawTileAnim`) : mapper AUSSI `__ANIM_DATA__`, sinon on croit à tort à un KO.
  ⚠ **Écume et falaises CONSERVÉES, mais pas là où le brief le dit** : le §3 annonce qu'elles « se
  posent par-dessus le remblai » ; en réalité (règle 10.75) **elles se dessinent sur les tuiles d'EAU**,
  d'après la terre voisine. Mesuré : les voisins d'eau d'un remblai calculent bien leur écume à partir
  de lui (`coast_ligne_w` / `coast_ligne_e`) → **le trait de rive reste continu**, et
  `coastTransitionTri` rend `null` sur un remblai (aucun triangle), exactement comme annoncé.
  (2) **§B — LE PLAFOND DE CHALEUR SUIT ENFIN L'ÉMISSION.** Le lot 2 (14.63) a fait passer la référence
  du boost d'antenne de `nominalPower` (PIC) à `meanPower` (MOYENNE) ; **`heatEmitMaxOf` n'avait pas
  suivi**. Sur un bâtiment à sigmoïde le plafond valait donc **1,7778× l'émission réelle** (= 256/144),
  contredisant l'invariant de `HEAT_CAP_SECONDS` (« 60 s d'émission de pointe »). Mesuré, ratio
  ancien/nouveau **exactement 1,7778** sur les 4 bâtiments à sigmoïde concernés (`presse_uhp`,
  `centrale_enrichissement_v2`, `usine_moteur_nuc_v2`, `usine_moteur_quantique`).
  ⚠ **Ce n'était PAS qu'une marge de sécurité** : `heatCapOf` est le dénominateur de `heat / cap`
  partout — art du conduit, fiche, et **capteur `surchauffe` réglé en POURCENTAGE (lot 1)**. Mesuré :
  un capteur réglé à **80 %** sur une presse boostée basculait en réalité à **142 % du chemin réel vers
  le trip**, c'est-à-dire **après** que le bâtiment aurait dû tripper. Il bascule désormais à 80 % pile.
  ⚠ **RIEN NE CHANGE pour une conso FIXE** (`meanPower == nominalPower`) : `machine_outil` mesurée à
  ratio **1,0000**, et les émissions PLATES (`usine_moteur_nuc`, `cryostat`, `data_center`) sortent
  avant par leur propre branche. Hors zone d'antenne : ratio 1,0000 aussi.
  ⚠ **LE CLAMP §B2 NE COUVRE PAS LE CAS RECHARGEMENT — ANOMALIE MESURÉE, LIVRÉE TELLE QUELLE.**
  `heatCapAdj` est un **one-shot** consommé au 1ᵉʳ `processHeat`. Or 14.63 documente qu'**une save
  rechargée a sa zone d'antenne ÉTEINTE au 1ᵉʳ tick** (`pwrAvg` absent → 0), rallumée au suivant. Donc
  au tick 1 le plafond est calculé **NON boosté** (le plus GRAND : 7,68 pour une presse), le clamp ne
  trouve rien à raboter **et se consomme** ; au tick 2 la zone s'allume, le plafond tombe à 5,184, et
  la protection est déjà dépensée. Mesuré sur bâtiment refroidi (trip impossible, on n'observe que le
  clamp) : zone active au tick 1 → heat 7,0 **rabotée à 5,132**, jauge 99 %, stable ; **save rechargée
  → heat reste à 7,0 pour un plafond de 5,184, soit une jauge FIGÉE à 135 %**, indéfiniment au-dessus
  du plafond. Conséquences : jauge > 100 %, capteur `surchauffe` en alerte permanente, et **si le
  bâtiment se remet à monter il tripe sans filet**. Le brief étant pré-compilé et exigeant la
  conformité SHA-256, **B2 est livré VERBATIM** ; le correctif (ré-armer `heatCapAdj` quand le plafond
  CHANGE, au lieu d'un one-shot) est une décision de design à arbitrer.
  ⚠ **Le clamp ne fait jamais tripper** (vérifié) : il s'exécute **avant** le test de trip.
  Contre-épreuve décisive : même état avec `heatCapAdj` pré-posé (= comportement SANS clamp) →
  `damaged = true` ; avec le clamp → `damaged = false`.
  ⚠ **`loadSave` clampe DÉJÀ `heat` au plafond au chargement** (filet 14.30) : c'est lui qui a ramené
  une chaleur de 8,0 à 7,68 dans le test de save réelle — ne pas l'attribuer au clamp B2.
  ⚠ **`meanPower` (13666) est déclarée APRÈS `heatEmitMaxOf` (10075)** : sans effet, ce sont des
  déclarations de fonction hoistées du même bloc `<script>` (le code appelait déjà `nominalPower` et
  `antElecBoost`, tous deux plus bas).
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium, **0 `pageerror`**, seul bruit console le
  **404 PRÉEXISTANT** du serveur de test. Contrôles du §7 : 5/5 ancres à `count == 1` avant et après ;
  **4/4 SHA-256 aller-retour** re-extraits du fichier patché ; **6/6 PNG re-décodés identiques
  octet-à-octet** au pack (32×32, palette, opaques, 11-14 couleurs) ; **delta +5 952 o EXACT** avant
  bump (3 199 226 → 3 205 178 ; 3 204 873 après bump, `GAME_NOTES` étant plus court que le précédent) ;
  `grep` = **6 déclarations + 2 usages**. **En jeu** : les 6 îles dessinent bien LEUR clé de remblai ;
  **remblai RÉEL** posé par le handler du bouton « Remblayer » → sauvegarde forcée → **rechargement** →
  `terrainMods` conservé et **rendu en `tile_i1_remblai`** ; annulation → retour à `tile_i1_water` ;
  bouton du panneau d'extension portant `tile_i1_remblai` ; **save créée par la BASE 346 rechargée en
  347** → remblai conservé, **aucun trip**, 0 `tickError`, 20 s de jeu réel, canvas peint ; lisibilité
  contrôlée à la capture au rendu **MIN_TILE (26 px)** — enrochement gris net, distinct du littoral
  turquoise et de la terre, sans moirage.
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) le panneau de remblai est **gaté par
  la recherche AU TAP** (`isTerrainExtendUnlocked`) → sans confirmer le nœud portant
  `unlocks.terrainExtend`, le tap sur une tuile de mer **n'ouvre rien** et on croit le rendu cassé ;
  (b) `tryExtend` n'est **PAS exposée** dans `window.__ui()` → passer par le **handler React du bouton
  via la fibre** (`__reactProps$…`), comme en 14.62 ; (c) `useGhostGuard` avale le 1ᵉʳ clic du panneau —
  et même amorcé, le vrai clic souris n'a pas suffi ici : la fibre est la voie fiable ; (d) le 1ᵉʳ
  `<img>` du panneau d'extension est le **swatch d'en-tête** (= terrain REMPLACÉ, donc `tile_i<N>_water`)
  — l'icône de A3 est sur le **bouton** (`class ui-ico`, parent `ip-demolish`) : viser le mauvais
  `<img>` fait conclure à tort à un échec ; (e) forger des tuiles « au centre de la vue » tombe sur la
  TERRE de l'île — pour un remblai il faut chercher de l'**eau adjacente à la terre** ; (f) un bâtiment
  à chaleur non alimenté a `heatEmit = 0` donc `rising` faux → **il ne tripe jamais** : un test de trip
  doit poser `heatEmit` explicitement (le tick le recalcule, piège 14.53).
  ⚠ **Taille : 3 199 226 → 3 204 873 o (+5 647 o)** — dont **+5 952 pour les 5 blocs** (exactement
  l'attendu), le reste étant le bump et un `GAME_NOTES` plus court.
  ⚠ **HORS PÉRIMÈTRE, non touché (§3)** : les 35 icônes du pack (**intégration B**), `tile_i7_remblai`
  (abandonné en séance d'art), les triangles `coast_tri_*` (vérifiés sans interaction), `coastFoamPieces`,
  `HEAT_CAP_SECONDS`, `HEAT_PER_MW`, `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 346`, `GAME_VERSION = 'Alpha 14.63'`, `SAVE_VERSION = 31`.**
  Changement 14.63 (brief `BRIEFLOT2antenne`, **LOT 2 — l'ANTENNE AMPLIFICATRICE, 7 chantiers sur un
  même sous-système**) : **3 exploits fermés, 2 rééquilibrages, 2 bugs d'affichage.** `SAVE_VERSION`
  INCHANGÉ, **aucun champ persisté ajouté** (`antPowered` est transitoire — la sérialisation des
  placements est une liste blanche). Base du brief EXACTE (345 / 14.62 / 3 192 664 o).
  ⚠ **BRIEF PRÉ-COMPILÉ, ET ÇA S'EST VU** : après les deux ratés du lot 1 (ancre mal échappée, patch
  à qui il manquait une parenthèse), l'auteur a appliqué ses 13 blocs sur une copie locale et publié
  les SHA-256 **RE-EXTRAITS du fichier patché**. Résultat ici : **13/13 ancres uniques du premier
  coup, 13/13 hachages conformes, delta d'octets EXACT (+6 447 au byte près)**. Aucune adaptation.
  **C'est la méthode à réclamer pour les prochains briefs.**
  (1) **§C1 — LA ZONE EXIGE UNE ANTENNE RÉELLEMENT ALIMENTÉE.** La pré-passe ne testait que
  l'ADJACENCE à un réseau câble : **une tuile de câble reliée à RIEN suffisait** à ouvrir la zone
  complète, et sans même cette ruse il suffisait de placer l'antenne **EN DERNIER dans l'ordre de
  priorité énergie** pour qu'elle reçoive 0 kW en boostant à 100 % (au Nv.10 : **524 MW de conso
  propre esquivés**). Nouveau gate sur `pwrAvg`, lu au tick PRÉCÉDENT (même décalage d'un tick que
  les capteurs logiques, assumé).
  ⚠ **HYSTÉRÉSIS 0,999 / 0,90, indispensable** : sur un réseau juste en limite, couper la zone baisse
  la demande, ce qui rallume la zone, ce qui la remonte → clignotement à chaque tick. Mesuré 60 s sur
  un réseau calé à **1 288 kW pour un pic de demande de 1 305,6** : **1 seule transition** (le
  allumage initial), zone stable ensuite.
  ⚠ **Save rechargée = zone ÉTEINTE au 1ᵉʳ tick** (`pwrAvg` absent → 0), rallumée au suivant. Mesuré.
  (2) **§C2 — LES PRODUCTEURS D'ÉLECTRICITÉ SORTENT DE LA ZONE.** Le prix du boost est la conso
  élec. MAJORÉE du voisin ; or un générateur a `power: 0`, donc sa référence vaut 0 **et le prix
  aussi** → une éolienne boostée produisait davantage pour **ZÉRO contrepartie**, et en mode
  productivité l'antenne n'émettait même **aucune chaleur** (celle-ci est indexée sur la conso EN PLUS
  des voisins). 8 bâtiments concernés ; le nucléaire était déjà hors du chemin générique.
  Mesuré : éolienne en zone = **même régime** que sa jumelle hors zone ; antenne en prod avec un seul
  producteur dans sa zone → **`heatEmit` = 0**, et contre-épreuve avec un consommateur → chaleur > 0.
  (3) **§C3 — `maxPerIsland` compte par FAMILLE DE PALIERS** (nouveau helper `tierFamily`, remonte
  puis redescend la chaîne `TIER_NEXT`). Il comptait **par id** : `antenne` et `antenne_v2` étant deux
  ids capés à 1 chacun, on pouvait poser **une V1 ET une V2** sur la même île — idem
  `centrale_nucleaire` V1/V2, ce qui cassait l'invariant écrit noir sur blanc dans le code (« une
  centrale par île, donc au plus une voix : pas de mixage »).
  ⚠ **Deux antennes cassaient en prime `antBld`**, qui ne retient que la DERNIÈRE balayée : l'autre
  n'émettait jamais de chaleur et n'avait **aucun plafond de trip**.
  ⚠ **Le cap ne joue qu'À LA POSE** : une save contenant déjà deux antennes n'est PAS migrée.
  Mesuré : `tierFamily('antenne')` = `[antenne, antenne_v2]`, `mine_fer_v2` → la chaîne V1→V4
  complète, `data_center` → lui seul ; pose V1 acceptée puis V2 **refusée**, et contre-épreuve
  2 aciéries acceptées (aucun cap).
  (4) **§C4 — LE MALUS DE VITESSE DU MODE PRODUCTIVITÉ EST SUPPRIMÉ.** Il était plafonné à −80 %,
  atteint au **Nv.5** : au-delà la sortie restait figée à ×0,20 **pour toujours** pendant que la conso
  continuait de doubler → l'électricité par unité produite doublait **à production CONSTANTE** (×261
  au Nv.10). **Le mode était injouable passé le Nv.4.**
  ⚠ **Le modèle Factorio est hors d'atteinte ici** (module productivité compensé par des diffuseurs
  de vitesse) : l'antenne est **À LA FOIS** le module et le diffuseur, et son mode est **unique par
  île** → les deux couches ne peuvent pas se superposer. Sans malus les deux modes deviennent
  symétriques : **ratio prix/gain = 1,00 à TOUS les niveaux, dans les deux modes** (vérifié Nv.1→10).
  ⚠ **`malus` est CONSERVÉ dans l'objet retourné** (deux fiches le lisent) et vaut désormais 0 ; les
  3 textes qui affichaient « vitesse −N % » sont réécrits, sinon les fiches annonçaient « vitesse −0 % ».
  Mesuré en moteur réel : sortie **identique** hors zone (1/s), intrants **÷ 1,1 exactement**
  (16 → 14,545 et 8 → 7,273).
  (5) **§C5 — LA RÉFÉRENCE DU BOOST DEVIENT LA PUISSANCE MOYENNE** (nouveau `meanPower`, miroir exact
  de `nominalPower` : mêmes branches arc/sigmoïde/random/fixe, **même traitement de `qStab`**).
  `nominalPower` rend le **PIC** : entrer dans une zone d'antenne faisait donc sauter le **PLANCHER**
  de conso de `base` à `base+amp` — **×8 sur une recette au rapport 1→8** — AVANT même le boost, soit
  un surcoût caché de **×1,778 en kWh par unité produite**, dans les DEUX modes.
  ⚠ **RIEN NE CHANGE pour un bâtiment à conso FIXE** (`meanPower == nominalPower`) : Aciérie, Data
  Center, Refroidisseur vérifiés identiques.
  ⚠ **CONSÉQUENCE ASSUMÉE** : la chaleur d'antenne suit la même référence → **×0,5625** sur un
  bâtiment à sigmoïde. C'est voulu (on ne facture plus une conso fantôme). Si le refroidissement
  devient trop peu contraignant, le levier est `HEAT_PER_MW`, **pas** un retour à la référence pic.
  Mesuré sur un `circuit` (sigmoïde 32→256, moyenne 144) boosté ×1,2 : bornes du réseau
  **1 200 → 1 452,8** au lieu de 1 312 → … avec l'ancienne référence.
  (6) **§C6 — la liste de priorité du panneau Énergie affiche la conso RÉELLE** : elle remplissait
  chaque ligne avec `nominalPower`, le pic **NON boosté**, ignorant complètement `antennaBuff`
  (sous-déclaration ×2,6 au pic pour une antenne Nv.4). **Les TOTAUX étaient déjà justes** (calculés
  dans le tick) — le bug était purement dans la ligne par bâtiment.
  (7) **§C7 — la fiche bâtiment passe par `meanPower`** au lieu de recalculer en local : le calcul
  local partait du pic **et surtout oubliait le facteur du Stabilisateur Quantique**, donc la fiche
  annonçait une plage que le moteur n'appliquait pas (**+10,8 % avec un stabilisateur Nv.2**).
  Mesuré en UI réelle : « boosté ×1→×1,2 · **144 kW→173 kW** », et avec stabilisateur Nv.2 la
  référence tombe à **129,96** (= 144 × 0,95²).
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **7 suites, 86 assertions, 0 KO**,
  **rejouées 2 fois sans flottement** — seul bruit console : le **404 PRÉEXISTANT** du serveur de
  test. **§7.19 sur une save RÉELLE créée par la BASE 345 puis rechargée en 346** : antenne Nv.4,
  mode productivité, réglages de transit et stocks tous conservés, 0 `tickError`, 20 s de jeu réel,
  canvas peint, 0 `pageerror`.
  ⚠ **ANOMALIE SIGNALÉE, NON CORRIGÉE (hors des 13 blocs)** : `heatEmitMaxOf` calcule le PLAFOND de
  chaleur d'un bâtiment boosté avec `nominalPower × (1 + antElecBoost)`, alors que son émission réelle
  part désormais de `meanPower`. Sur un bâtiment à sigmoïde le plafond est donc **~1,78× plus généreux**
  que l'émission — **marge de sécurité, jamais un trip imméritée** ; à réaligner sur `meanPower` si on
  veut que le plafond colle à nouveau à l'émission.
  ⚠ **INCOHÉRENCE COSMÉTIQUE CONNUE ET ACCEPTÉE (§4 du brief)** : `game.antennaBuff[isl]` reste
  l'ensemble des **TUILES**, non filtré par éligibilité → **une éolienne dans la zone affiche encore le
  liseré** alors qu'elle n'a plus aucun effet. La corriger demanderait de dupliquer le test
  d'éligibilité dans le rendu.
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) une rangée de câble posée en travers
  d'une île **COUPE le blob de routes** et tous les bâtiments au nord passent `disc: 'road'` — poser
  une **jonction route/câble** pour laisser passer la route (axes perpendiculaires, règle 13.18) ;
  (b) le **débit du câble V1 plafonne la composante à 512 kW** : sans passer les réseaux en
  `unlimited`, on mesure le plafond du réseau et l'antenne (1 024 kW) n'est **jamais servie** — donc
  `antPowered` reste faux et on croit à tort que le gate est cassé ; (c) **forger des bâtiments
  déclenche une astuce** dont le popup vole les clics du canvas → purger les astuces **APRÈS** la
  forge, pas seulement avant ; (d) fermer une fiche par `.click()` DOM est avalé par `useGhostGuard`
  → vrai clic souris **et** attendre la disparition du panneau, sinon le tap suivant relit la fiche
  encore ouverte ; (e) `tierFamily`, `meanPower`, `energyConsumerList`, `islandFlowAgg` sont des
  **déclarations de MODULE** → accessibles par leur nom nu dans `page.evaluate`, inutile de les
  exposer dans `__heat`.
  ⚠ **Taille : 3 192 664 → 3 199 226 o (+6 562 o)** — dont **+6 447 pour les 13 blocs** (exactement
  l'attendu), le reste étant `GAME_NOTES`.
  ⚠ **HORS PÉRIMÈTRE, non touché (§4)** : le rayon d'influence, les barèmes `antSpeedMul` /
  `antElecBoost`, le plafond de rendement (retiré en 14.03, il le reste), `HEAT_PER_MW`, le chemin
  nucléaire (`nucList`), `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 345`, `GAME_VERSION = 'Alpha 14.62'`, `SAVE_VERSION = 31`.**
  Changement 14.62 (brief `BRIEFLOT1capteurchenaltransitreserve`, **LOT 1 — quatre chantiers INDÉPENDANTS**) :
  **(A) le seuil du capteur `surchauffe` se règle en POURCENTAGE ENTIER ; (B) le chenal d'accès du port
  n'est plus remblayable ; (C) la section « Transit entre îles » quitte le panneau Production ;
  (D) « Demander au port » TIENT aussi la réserve d'export.** `SAVE_VERSION` INCHANGÉ, **les 2 champs
  ajoutés sont OPTIONNELS avec repli** (`sensorPct` côté surcouche logique, `askPrev` côté `tradeConfig`).
  Base du brief EXACTE (344 / 14.61 / 3 186 954 o).
  ⚠ **4 ANCRES DU BRIEF ÉTAIENT MAL ÉCHAPPÉES** (A2, B4, C2, D1a) : le fichier **mélange** les deux
  encodages — les blocs récents portent des caractères UTF-8 **littéraux** (`é`, `→`, `î`, `⚠`), les blocs
  passés par Babel des **`\xNN`**. Les ancres du brief supposaient `\xNN` partout → `count == 0`. Ré-extraites
  du fichier, puis `count == 1`. **Ne jamais présumer l'encodage d'une ancre : la sortir du fichier.**
  ⚠ **ERREUR DE PARENTHÉSAGE DANS LE BRIEF (§4.C2), corrigée** : le remplacement proposé `  }))));`
  compte **UN `)` DE MOINS** que nécessaire. Après retrait de la section transit il faut refermer
  **arrow → `resKeys.map` → `div.prod-table` → `div.prod-body` → `div.prod-panel` → Fragment**, soit
  `})))));` (5 parenthèses). Livré avec 5 ; `node --check` (7 blocs, 7 OK) est ce qui tranche.
  (1) **§A — le seuil de `surchauffe` passe en POURCENTAGE ENTIER** (champ **dédié `sensorPct`**, 1→100,
  défaut 80). Le bug : `NumField` valide en `Math.floor()` et affiche via `fmtPort` (qui **arrondit**) —
  une fraction 0→1 y était **illisible** (0,8 s'affichait « 1 ») **et inréglable** (toute saisie < 1 était
  plancherée à 0, donc retombait au défaut). **Seule la valeur 1 était atteignable.**
  ⚠ **`sensorSeuil` redevient la propriété EXCLUSIVE du mode `seuil`** (un STOCK) : les deux modes
  partageaient le même champ, donc un seuil de 1e7 acier devenait un seuil de chaleur clampé à 1 en
  changeant de mode. Mesuré (T11) : `sensorPct = 55` + `sensorSeuil = 1e7` → chaleur **0,55**, stock **1e7**,
  les deux intacts après un aller-retour de mode ET un rechargement.
  ⚠ **Plancher à 1 %, pas 0** : un seuil de 0 % basculerait le signal en permanence. Saisie 0 → 1,
  250 → 100, « 42,7 » → 42 (mesuré en UI réelle).
  ⚠ **PIÈGE — les surcouches logiques sont sérialisées par LISTE BLANCHE** (`t.logic` est recopié champ
  par champ vers des clés courtes `sm`/`sr`/`sq`/`sd`/`gd`…). Ajouter `sensorPct` à l'objet en mémoire
  **NE SUFFIT PAS** : sans les 3 sites (sérialisation `lp.sp` + **DEUX** chemins de restauration —
  migration `< v28` depuis `t.building`, et chargement `v28+` depuis `logicPlacements`), le réglage
  disparaît au rechargement. Les 3 sont patchés ; **save réelle écrite puis rechargée** : `sp:55` présent
  dans le fichier, `sensorPct` restauré, fraction toujours 0,55.
  (2) **§B — le CHENAL D'ACCÈS du port n'est plus remblayable** : le cargo navigue sur la **LIGNE du port,
  à gauche de celui-ci** (`drawPortExtras` pose le ponton en `portC-1` et fait naviguer le bateau depuis
  `portC-7`, tous sur `portR`). `tryExtend` ne testait que « eau + adjacent à la terre » → on pouvait
  remblayer là, et le cargo naviguait ensuite **sur la terre ferme**.
  ⚠ **La règle est volontairement LARGE — TOUTE la ligne à gauche du port**, pas les 7 tuiles de
  l'animation courante : elle doit rester vraie si l'animation change. Nouveau helper `isPortChannel`.
  ⚠ **Île 7 absente de `PORTS`** → `portPosFor` rend `null` → aucun chenal, et la garde est de toute
  façon placée **APRÈS** `if (isl === 7) return tryDrill(r, c);` → **la foreuse n'est pas touchée**.
  ⚠ **AUCUNE MIGRATION (décision assumée)** : une tuile déjà remblayée dans un chenal sur une save
  existante **reste remblayée** — aucun remboursement, aucun rollback.
  ⚠ **Défense en DEUX temps, les deux testées** : (a) UI — le bouton « Remblayer » reste **visible mais
  désactivé**, avec le motif en clair (`blockMsg`, rouge) ; refuser en silence donnerait l'impression d'un
  bouton cassé ; (b) MOTEUR — garde dans `tryExtend` (toast rouge + `SFX.invalid`), **avant** le gate de
  techno et le paiement : **rien n'est débité**. Le (b) a été exercé en appelant le **handler React du
  bouton** via la fibre (`__reactProps$…`), ce qui court-circuite `disabled` ET le ghost-guard.
  (3) **§C — la section « Transit entre îles » quitte le panneau Production** (calcul, rendu, CSS + les
  5 règles orphelines `.prod-trow`/`.pt-route`/`.pc-res`/`.pt-rate`/`.prod-transit`). `grep transits` = 0,
  `grep prod-sub|prod-transit|prod-trow|pt-route|pt-rate` = 0. **`allTransitFlows` et l'onglet « Transit
  archipel » du Port sont CONSERVÉS INTACTS** (grep inchangé à 3) — c'est là que l'information vit
  désormais, sous forme de **CARTE** (14.41 : on touche une liaison pour voir son transit, ce n'est plus
  une liste). `.prod-empty` est **conservée** (encore utilisée par le tableau de ressources vide).
  ⚠ **Les clés i18n `"Transit entre îles"` / `"Aucun transit en cours."` deviennent ORPHELINES dans les
  5 tables et sont LAISSÉES EN PLACE** (décision du brief) : ce sont des lignes géantes, les éditer est du
  risque pur pour zéro gain.
  (4) **§D — « Demander au port » TIENT la réserve d'export.** `askPortFor` relevait la cible d'import
  mais laissait `seuilExport` à 0 : on n'exporte que le surplus **au-dessus** de la réserve, donc la
  matière importée pour l'amélioration **repartait en aval dès son arrivée** et l'amélioration n'était
  jamais payable. Elle relève désormais les DEUX, mémorise l'ancienne réserve dans **`askPrev`**, et
  `pay` la **REND** via le nouveau `releaseAskHold`.
  ⚠ **RELÂCHE À LA DÉPENSE, PAS À L'ARRIVÉE** (décision) : relâcher quand la marchandise débarque rouvre
  exactement la fenêtre qu'on ferme — le bateau réexpédierait en aval ce qui vient d'arriver.
  ⚠ **LE COMMENTAIRE DE 14.42 DISAIT L'INVERSE ET A ÉTÉ RÉÉCRIT, PAS CONSERVÉ** : il justifiait de ne pas
  toucher `seuilExport` (« l'île cesserait de réexporter en aval »). C'est **CADUC** depuis
  `transitForwardBudget`, qui sur-remplit l'île intermédiaire pour qu'elle relaie en aval **sans jamais
  descendre sous sa réserve**. En revanche l'**écriture DIRECTE sur `cfg`** (et non via `setTradeCfg`) est
  **CONSERVÉE** : `setTradeCfg` applique le lien « Cible ⇒ Réserve » qui écraserait la réserve **sans
  mémoriser** l'ancienne valeur — on ne pourrait plus la rendre.
  ⚠ **`askPrev` n'est mémorisé QU'UNE FOIS** : une 2ᵉ demande sur la même ressource ne l'écrase pas
  (sinon on rendrait plus tard une réserve déjà gonflée par la 1ʳᵉ). Mesuré : demande 100 puis 1000 →
  réserve 106 puis 1051, **`askPrev` reste 0**. Et une réserve **déjà supérieure** n'est jamais baissée
  (99999 conservée, aucun `askPrev` posé).
  ⚠ **PIÈGE — `pay(cost, isl)` accepte `isl` ABSENT.** `portPool` fait le repli
  `islandId != null ? islandId : game.currentIsland` ; **`portIslandOf` NE LE FAIT PAS** et rend
  `undefined` → `g.tradeConfig[undefined]` **n'échoue pas, il ne fait rien**, et le hold ne se relâcherait
  **jamais, en silence**. `releaseAskHold` refait la résolution à l'identique.
  ⚠ **LECTURE SEULE sur `tradeConfig` dans `releaseAskHold`** : ne JAMAIS passer par `tradeCfgFor`, il
  **CRÉERAIT** une entrée pour chaque ressource de chaque coût payé dans la partie.
  ⚠ **Édition manuelle = abandon du hold** (`setTradeCfg` supprime `askPrev`) : sans ça, on écraserait
  plus tard le réglage que le joueur vient de saisir. Mesuré : réserve éditée à 321 → bordure ocre
  disparue, et la dépense ne la remet **pas** à 0.
  ⚠ **`askPrev` doit être ajouté à la LISTE BLANCHE de restauration de `tradeConfig`** — sans cette
  ligne le hold est perdu au rechargement et **la réserve gonflée resterait en place à vie**.
  ⚠ **Le hold est VISIBLE** (`.pp-c-num.held`, bordure + texte ocre `#e6b673`) : un `title` seul est
  inatteignable au doigt sur mobile. Mesuré : `rgb(230, 182, 115)` sur le champ Réserve **et lui seul**.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **6 suites, 80 assertions, 0 KO**, **rejouées
  2 fois sans flottement**, + une suite de **non-régression, 12 assertions** — seul bruit console : le
  **404 PRÉEXISTANT** du serveur de test. **§2.4 vérifié sur une save RÉELLE créée par la BASE 344 puis
  rechargée en 345** : réglages de transit conservés (900/400), **aucun `askPrev` nulle part**, stock
  intact, capteur à l'ancienne (`sensorSeuil = 0,35`) **toujours à 0,35** (repli), 0 `tickError`,
  20 s de jeu réel à ~1 tick/s, canvas peint, 0 `pageerror`.
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) **`page.click({force:true})` est AVALÉ**
  par l'UI — il faut un vrai `mouse.move` + `down` + pause + `up` ; (b) **`tryPlace(r, c, id, verbose)`
  prend l'id EN PARAMÈTRE**, il ne le lit PAS dans `g.ui.tool` — l'appeler à 2 arguments rend `false` en
  silence (m'a coûté 3 itérations) ; (c) **`askPortFor` ne relève QUE ce qui est en DÉFICIT** → remplir le
  port AVANT l'appel le rend inopérant : mettre le port à 0, appeler, PUIS livrer ; (d) le piège (g) de
  14.47 frappe encore — **confirmer tout l'arbre de recherche déclenche une FILE d'astuces** dont le
  `.research-backdrop` vole les clics du canvas : ne confirmer QUE le nœud portant le flag voulu
  (`unlocks.terrainExtend`), et couper `g.ui.tipsEnabled` AVANT ; (e) `game.transitFlow` est **remis à
  zéro à chaque tick** par `tickShips` → une valeur forgée ne survit pas, la **ré-affirmer** en
  `setInterval` (~25 ms), même patron que `conduitLoad` (14.51) ; (f) les champs `NumField` affichent en
  **notation port** (`1 051`, espace fine) → normaliser avant de comparer à `"1051"`.
  ⚠ **Taille : 3 186 954 → 3 192 664 o (+5 710 o)**, dominée par les commentaires de décision.
  ⚠ **HORS PÉRIMÈTRE, non touché (§2.2)** : **l'antenne amplificatrice (zone, boost, conso, chaleur,
  `maxPerIsland`) — c'est le LOT 2, séparé** ; le mode `seuil` du capteur et son champ `sensorSeuil` ;
  l'onglet « Transit archipel » et `allTransitFlows` ; `transitForwardBudget`, `game.tradePriority`,
  `cfg.interdit`, `tradeBlockDest` ; les tables i18n ; `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 344`, `GAME_VERSION = 'Alpha 14.61'` (étiquette INCHANGÉE, voir
  ci-dessous), `SAVE_VERSION = 31`.**
  Changement build 344 (brief `BRIEFDIVERSL4`, **LOT 4 « DIVERS » — deux changements INDÉPENDANTS**) :
  **(§A) le forfait des 7 mines V4 gagne 1 ordinateur quantique + 1 moteur quantique ; (§B) cavalier —
  `toggleCollider` purge aussi `co.powered`.** `SAVE_VERSION` INCHANGÉ, **aucun champ de sauvegarde touché**
  (`co.powered` est transitoire, absent de la liste blanche). Base du brief EXACTE (343 / 14.61 /
  3 184 653 o) : les **8 ancres M1→M8 sont sorties UNIQUES**, aucune re-dérivation.
  ⚠ **`GAME_VERSION` N'A PAS ÉTÉ BUMPÉE, À DESSEIN** : le brief l'interdit explicitement deux fois
  (§0 « Aucune étiquette `GAME_VERSION` » et §10 « Ne proposer aucune étiquette »). Seul `GAME_BUILD`
  passe 343 → 344 — c'est lui qui porte la notification de mise à jour. **Un build dont l'étiquette
  n'avance pas est donc NORMAL ici** ; les commentaires de ce lot sont datés « build 344 » et non
  « 14.6x ». Signalé au rapport comme écart assumé à la convention du projet (« bumper à CHAQUE modif »).
  (1) **§A — forfait des mines V4** : `{ alliage_tungstene: 100, piece_precision: 10 }` devient
  `{ …, ordinateur_quantique: 1, moteur_quantique: 1 }` sur les **7** lignes (fer, charbon, carrière,
  cuivre, or, uranium, tungstène). **AJOUT, pas remplacement** (L1) — un remplacement aurait donné une
  courbe à ressource unique, très granuleuse dans le bas (1 → 3 → 7).
  ⚠ **LE FORFAIT EST LA BASE D'UNE COURBE, PAS UN COÛT FIXE** (L3) : le coût d'un niveau `u` vaut
  `round(forfait × barème^(u − entry))` → les deux ressources quantiques apparaissent à **TOUS** les
  niveaux V4, en quantités croissantes. Mesuré (T2, recherche `upg` à 0 donc barème 2,7) : Nv.31 →
  `{alliage 270, précision 27, ordi 3, moteur 3}` · **Nv.35 → `{alliage 14 349, précision 1 435,
  ordi 143, moteur 143}`**. Au Nv.40 le moteur quantique se compte en dizaines de milliers. C'est voulu.
  ⚠ **LA MINE DE TUNGSTÈNE EST AVANTAGÉE, C'EST UNE DÉCISION (L2), PAS UN OUBLI** : les `entry`
  diffèrent (30 · 30 · 30 · 30 · 20 · 20 · **10**) pour un forfait IDENTIQUE → elle atteint son V4
  **vingt niveaux plus tôt** que le fer, au même prix. Mesuré (T3) : `upgradeCost(…, 10,
  'mine_tungstene_v4')` = exactement le forfait nu, comme le fer au Nv.30. **NE PAS « RÉÉQUILIBRER ».**
  ⚠ **`entry` NON TOUCHÉES** et **aucun autre forfait de `TIER_STEP` modifié** : diff complet publié —
  **41 entrées, 7 lignes de données modifiées, les 34 autres identiques à l'octet** (T5, 0 écart).
  Le commentaire `14.08` au-dessus du bloc annonçait « identiques : alliage de tungstène + pièces de
  précision » — devenu faux, il est **corrigé sur place** (le brief ne le mentionnait pas).
  (2) **§B — cavalier `co.powered`** : le lot 2 (14.59) avait couvert le cas `undefined` (premier tick
  d'une session). Restait le cas **PÉRIMÉ** : l'affectation de `co.powered` par la boucle énergie est
  gardée par `state !== 'off'`, donc **machine à l'arrêt = mesure GELÉE**. Après un arrêt carburant elle
  vaut `true` (un arrêt He3 suppose le courant présent, ordre courant-avant-carburant de 14.24) ; si le
  réseau s'est effondré entre-temps, le rallumage jugeait sur cette valeur morte et **prélevait un tick
  d'hélium 3 à vide**. `toggleCollider` purge désormais `co.powered = undefined` **au même endroit et
  dans les deux sens** que `halt`/`_haltPrev` (L4).
  ⚠ **`undefined` et NON `false` (L5)** : `false` provoquerait un arrêt courant IMMÉRITÉ au tick suivant ;
  `undefined` fait retomber la machine dans la garde du lot 2 (`typeof co.powered !== 'boolean'`) → tick
  sans jugement ni prélèvement. **L'invariant devient : `co.powered` ne fait foi que tant que la boucle
  énergie l'entretient.**
  ⚠ **La garde `state !== 'off'` de la boucle énergie est CORRECTE et n'a PAS été touchée (L6)** :
  entretenir `powered` sur une machine éteinte n'aurait aucun sens. C'est la **consommation** de la
  valeur périmée qui était fautive, pas sa non-mise-à-jour. La garde `typeof !== 'boolean'` du lot 2 est
  elle aussi intacte — c'est elle qui recueille le cas créé ici.
  **CONTRE-ÉPREUVE EXÉCUTÉE, même scénario sur les deux fichiers** : base 14.61 → `powered` reste `true`
  au rallumage et **l'He3 passe de 1000 à 999** (1 prélevé à vide) ; build 344 → `powered` devient
  `undefined` et **l'He3 reste à 1000**. C'est la preuve directe du correctif.
  ⚠ **ÉCART BRIEF/CODE SIGNALÉ, non corrigé** : T10 du brief annonce « Tick 1 : tick blanc. Tick 2 :
  démarrage ». En réalité la branche de **réamorçage** (`state === 'off' && !_haltPrev` → `starting` +
  `colliderBoot`) se trouve **AVANT** la garde du lot 2 dans `processCollider` → le réamorçage a lieu
  DÈS le tick 1, et le tick 2 est le premier qui juge et fait avancer le timer. Le résultat observable
  visé par le brief tient : **`colliderBoot` joué exactement UNE fois, 0 hélium prélevé au tick 1**
  (mesuré). Le commentaire du lot 2 (« tick BLANC : ne touche NI `state` … ») décrit donc la garde
  elle-même, pas la branche qui la précède.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **16 assertions, 0 KO**, suite **rejouée
  2 fois sans flottement**. T6 = densification RÉELLE en jeu (clic souris sur « ✦ Densifier » de la fiche,
  2 temps) : `mine_fer_v3` Nv.30 → `mine_fer_v4` u=30, **débité au port : alliage 100 · précision 10 ·
  ordi quantique 1 · moteur quantique 1**. T7 : `cumulativeInvested('mine_fer_v4', 32)` contient
  `ordi 11 / moteur 11` et la portion V1→V3 **aucun**. T8/T9 : rallumage réel après arrêt carburant →
  stock He3 **strictement inchangé**, 0 arrêt compté, puis arrêt `power` normal au tick suivant.
  T12 : lots 1-2 intacts (tick blanc W1, arrêt électrique V1, arrêt total He3 V4, 60 ticks de panne →
  0 boot rejoué, 1 seul arrêt). T13 : lots 3A/3B intacts (frontières de points [0,3,4,5], soft caps sous
  seuil, Σ séparateur = 1, remboursement R1 = 44 883 pierre/minerai à l'unité, soft caps et `sep`/`boot`
  à zéro, `island8Unlocked` false, 7 îles). T14 : **620 ticks réels** avec montage logique complet
  (émetteurs → XNOR → vanne) → 0 `tickError`, 0 arrêt, 0 pénalité, 19 confirmations.
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) **`colliderPalier` s'indexe sur
  `COLLIDER_REPAIR_NODES`, PAS sur `COLLIDER_PUZZLE_NODES`** (correctif 14.39) → un test qui confirme
  tout l'arbre puis « relocke les puzzles » reste **au palier 3** : il faut relocker les réparations II
  et III (en gardant la I, sinon `colliderRepaired` tombe). Symptômes trompeurs : He3 à 16/s au lieu de
  1/s, et un comparateur XNOR 1 bit qui prend une pénalité (il est juste au palier 1, faux au palier 3) ;
  (b) **`switchIsland(6)` REFUSE si l'île est verrouillée** — poser `g.islandUnlocked[6] = true` avant,
  sinon on tape le canvas de l'île 1 et « la fiche ne s'ouvre pas » ; (c) un bloc de test qui remplit un
  port à 1e12 et confirme tout l'arbre **pollue tous les blocs suivants** (palier, remboursements
  globaux) → juger les deltas de remboursement **par île**, pas globalement ; (d) le montage logique
  du Collisionneur exige de **purger `t.logic` avant de le reposer** et de remettre à plat
  `code`/`dcCode`/`dcAcc`/`roundDone`, sinon la toute première manche est jugée sur un état périmé
  (1 pénalité fantôme) ; (e) le bouton « ✦ Densifier » est à **2 temps** et la fiche défile →
  `scrollIntoViewIfNeeded` + re-localisation entre les deux clics.
  ⚠ **Taille : 3 184 653 → 3 186 954 o (+2 301 o)**, dominée par les commentaires de décision.
  ⚠ **HORS PÉRIMÈTRE, non touché (§3)** : les `entry` des mines V4 et tout autre forfait de `TIER_STEP`,
  les recettes/sorties/sigmoïdes/sprites des mines, la garde `state !== 'off'` de la boucle énergie,
  la garde `typeof co.powered !== 'boolean'` du lot 2, le reste du régime de déficit, la file FIFO,
  les recherches infinies, le format de sauvegarde.
- **État précédent : `GAME_BUILD = 343`, `GAME_VERSION = 'Alpha 14.61'`, `SAVE_VERSION = 31`.**
  Changement 14.61 (brief `BRIEFRECHERCHESINFINIESL3B`, **LOT 3B — clôture des recherches infinies**) :
  **ACHAT des recherches + REMBOURSEMENT RÉTROACTIF + INTERFACE (fiche du Collisionneur) + crochet
  île 8 inerte.** `SAVE_VERSION` INCHANGÉ, **aucun champ persisté ajouté** (l'achat écrit
  `techTree.research`, champ du lot 3A ; le remboursement crédite les ports existants). Base du brief
  EXACTE (342 / 14.60 / 3 175 388 o) : les **14 ancres K1→K14 sont sorties UNIQUES** (comptes publiés,
  aucune re-dérivation).
  ⚠ **CE LOT CONTIENT LA SEULE OPÉRATION DU PROJET QUI PEUT FABRIQUER DE LA MONNAIE SANS RIEN CASSER**
  (un remboursement trop généreux est invisible : aucun crash, aucun test rouge, juste une économie
  faussée). Les 4 garde-fous J1/J2/J3/J4 sont la substance du lot — ne jamais les « simplifier » :
  (1) **J1 — liste EXPLICITE `RESEARCH_REFUNDABLE = {upg, transit, remblai}`** : `boot` possède
  `base`/`step` comme les autres mais ce sont des SECONDES — dériver la liste de la présence de `base`
  rembourserait des ressources pour une durée de démarrage. `sep` (multiplicatif) n'a rien à rembourser.
  Mesuré (R9) : `sep` et `boot` achetés avec four Nv.10 + port Nv.5 + 3 remblais en stock → **0 port
  touché**.
  (2) **J2 — le remboursement part TOUJOURS du barème d'AVANT CE POINT** (`sBefore = base − step·lvl`,
  `sAfter = base − step·(lvl+1)`), jamais de 2,70 fixe. Mesuré (R2) : four monté au Nv.12 SOUS 2,69
  puis achat du 2ᵉ point → rendu **380 165** pierre ; un calcul depuis 2,70 aurait rendu **775 447**
  (le double — c'est l'exploit « monter pas cher, se faire rembourser cher » qui est bloqué).
  (3) **J3 — arrondi PAR CRAN puis soustraction des totaux entiers** (`Σround(ancien) − Σround(nouveau)`,
  jamais `round(ΣΔ)`) : le joueur a PAYÉ des crans arrondis un par un, le remboursement doit suivre le
  même chemin. Mesuré (R3, 40 bâtiments) : écart entre les deux méthodes
  `{pierre:+1, ciment:+2, acier:+6, lingot_fer:−6, silicium:−11}` — les deux DIFFÈRENT, c'est la
  version niveau-par-niveau qui est livrée.
  (4) **J4 — paramètre `scaleOv` OPTIONNEL en dernière position sur les 6 fonctions de coût**
  (`upgradeCostFactor`/`upgradeCost`/`cumulativeInvested`/`portUpgradeCost`/`elevatorUpgradeCost`/
  `extensionCost`) pour évaluer le même état aux deux barèmes — **JAMAIS d'écriture temporaire dans
  `*_SCALE_CUR` suivie d'une restauration** (une exception au milieu laisserait toute l'économie sur
  un mauvais barème). Grep publié : les seules écritures de `UPGRADE_SCALE_CUR`/`PORT_SCALE_CUR`/
  `EXT_SCALE_CUR`/`COLLIDER_START_CUR` sont les 4 `let` d'init + les 4 affectations DANS
  `refreshResearchEffects` (lignes 8944-8947 / 8952-8955). N1 prouve que les 6 signatures étendues
  rendent des résultats **identiques à l'octet à 14.60** sans l'argument (20 ids × 31 niveaux +
  facteurs + ports 5×21 + élévateur 16 + remblai 9, contre le VRAI code 14.60 via `_ref1460.html`).
  (5) **J6 — les soft caps remboursent zéro STRUCTURELLEMENT** (aucun filtre explicite) : ils ignorent
  `scaleOv` comme ils ignorent CUR (branche `soft` du lot 3A) → leur delta est nul par construction.
  Mesuré (R4) : `puits` Nv.8 + `eolienne` Nv.12 + les 5 `COST_SOFTCAP_X2` Nv.12 seuls sur une île →
  delta `{}`, **aucune entrée de port créée**, le niveau de recherche passe quand même à 1.
  (6) **J7 — achat ATOMIQUE** (`buyResearch` : vérifier → delta → créditer → incrémenter → refresh) :
  l'incrément de niveau vient APRÈS le crédit. Mesuré (A4) : un `refund` qui LÈVE (Proxy piégé sur
  `game.port[1]`) laisse le niveau à 0 ET le barème à 2,7 — aucun état à moitié écrit.
  (7) **J5 — chaque île reçoit SON delta ; l'île 7 crédite le PORT 6** (via `portPool`), l'élévateur
  aussi. Mesuré (R5) : gains `{1: 980, 3: 75, 6: 465}` où 465 = 86 (bâtiments île 6) + 379 (souterrain),
  **aucun `game.port[7]` créé**. Emprise multi-tuiles comptée UNE fois (R6 : centrale 2×2 → delta d'UN
  bâtiment, dédup par identité d'objet `t.building` dans un `Set`).
  (8) **UI (bloc C, ancre K11)** : section « Points de recherche » dans la fiche du Collisionneur,
  gatée sur le nœud 43 confirmé — `disponibles / gagnés` (`fmtPort`) + 5 boutons `Nv. N/10 · effet`
  (`2.70` → `2.60`, `×1.00` → `×2.59`, `600.00 s` → `300.00 s`, « max » au cap), infobulle « ⚠ Le point
  dépensé est DÉFINITIF et ne peut pas être récupéré » (J8 : AUCUNE confirmation à 2 temps — décision
  du brief, le tooltip porte l'avertissement). Achat par VRAI clic souris vérifié (U3 : « 3 / 3 » →
  « 2 / 3 », Nv. 1, barème 2,69, toast « 🔬 Recherche améliorée »).
  ⚠ **3 ADAPTATIONS AU BRIEF, assumées et signalées** : (a) le brief demandait un SFX à l'achat — il
  n'existe AUCUN son « research » dans le catalogue (67 sons) → **retiré**, à câbler si un son est
  livré un jour ; (b) le brief proposait `var(--violet)` pour la couleur du compteur — cette variable
  CSS **n'existe pas** → littéral `#B47CFF` ; (c) `bumpHud()` + `scheduleSave()` ajoutés en fin de
  `buyResearch` (pattern standard de TOUS les handlers — sans eux le HUD n'affiche le crédit qu'au
  tick suivant et une fermeture immédiate perdrait l'achat).
  ⚠ **`island8Unlocked(game)` rend `false` en dur (bloc E, J11)** : c'est le SEUL crochet du lot pour
  l'île 8 — aucune île supplémentaire nulle part (N5 : 7 îles). Le jour venu : brancher la condition
  réelle ici, et se souvenir que l'id 8 s'affichera via `islandLabel`.
  **Validé** : `node --check` (**7 blocs, 7 OK**) + Chromium **2 suites, 31 assertions (22 moteur +
  9 UI réelle), 0 KO** (seul bruit : le 404 PRÉEXISTANT du serveur de test), moteur rejoué **3 fois,
  UI 2 fois, sans flottement**. R1 vérifié à l'unité contre un calcul indépendant : 1 four Nv.10 →
  achat `upg` → **{pierre: 44 883, minerai_fer: 44 883}** exactement. R7 transit : île 1 +75 428,
  île 2 +1 715, élévateur → port 6 +1 638. R8 remblai : île 2 +935 370 pierre +93 537 ciment +9 354
  béton irradié = Σ des 4 crans re-cotés. A6 persistance réelle : save/reload → niveaux conservés,
  barèmes 2,68/1,97/570 corrects, **port STRICTEMENT identique** (aucun remboursement rejoué). N4 :
  600+ ticks réels avec montage logique COMPLET → 0 tickError, 0 arrêt, 0 pénalité, 20 confirmations.
  N2/N3 : sous-ensemble essentiel des lots 1/2/3A rejoué (frontières de points, soft caps sous seuil,
  somme séparateur = 1, tick blanc W1, arrêt électrique V1, arrêt total He3 V4) — les suites
  COMPLÈTES X/Y/Z/V/W ont été jouées sur cette même base aux livraisons 14.58-14.60, et N1 prouve
  l'identité à l'octet des 6 fonctions de coût.
  ⚠ **PIÈGES DE HARNAIS (nouveaux, coûteux)** : (a) **l'`innerText` des fiches est en MAJUSCULES**
  (CSS `text-transform`) → tout matcher de libellé doit être insensible à la casse (`NV. 0/10 ·
  600.00 S`) — m'a donné 5 faux KO d'un coup ; (b) **pollution d'état entre blocs de test** : 40 000
  confirmations + nœud 43 confirmé laissés par un bloc précédent déclenchent `colliderGoalLocked`
  (seuil atteint, puzzle non validé) → early-return AVANT le régime de déficit testé ensuite — purger
  `node43.status`/`colliderConfirms` entre blocs ; (c) le compteur « N / M » de la fiche a un JUMEAU
  (la ligne CONFIRMATIONS) → un `match(/\d+ \/ \d+/)` naïf attrape la mauvaise ligne ; (d) pour A4,
  un `new Proxy({}, { set() { throw } })` posé sur `game.port[1]` prouve l'atomicité sans toucher au
  code.
  ⚠ **Taille : 3 175 388 → 3 184 653 o (+9 265 o).**
  ⚠ **`__heat` étendu** : `researchRefundDelta`, `RESEARCH_REFUNDABLE`, `island8Unlocked`
  (`cumulativeInvested` y était déjà — ne pas dupliquer la clé) ; **`__ui` étendu** : `buyResearch`.
  ⚠ **HORS PÉRIMÈTRE, non touché (§3)** : le contenu des 5 barèmes (lot 3A), `researchPointsEarned`
  et ses frontières, le régime de déficit (lots 1-2), la file FIFO, toute génération d'île 8 (seul le
  crochet `island8Unlocked` existe, inerte), `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 342`, `GAME_VERSION = 'Alpha 14.60'`, `SAVE_VERSION = 31`.**
  Changement 14.60 (brief `BRIEFRECHERCHESINFINIESL3A`, **LOT 3A**) : **FONDATIONS DES RECHERCHES
  INFINIES — cinq barèmes pilotables, calcul des points, persistance, rafraîchissement. NI achat, NI
  remboursement, NI interface (lot 3B).** `SAVE_VERSION` INCHANGÉ ; seul champ ajouté
  `techTree.research` (5 entiers, optionnel, clampé [0,10] à l'écriture ET à la lecture). Base du
  brief EXACTE (341 / 14.59 / 3 164 819 o) : les **18 ancres sont sorties UNIQUES**.
  (1) **Cinq barèmes** (`RESEARCH_DEFS`) : `upg` 2,7 −0,01/niv (2,60 au cap), `transit` 2 −0,03
  (1,70 — port ET élévateur), `remblai` 10 −0,3 (7), `sep` ×1,1 multiplicatif sur He3 ET He4
  (méthane = RELIQUAT, somme exactement 1), `boot` 600 s −30 (300). Variables de MODULE
  (`UPGRADE_SCALE_CUR`/`PORT_SCALE_CUR`/`EXT_SCALE_CUR`/`COLLIDER_START_CUR`) rafraîchies par
  **`refreshResearchEffects(game)`**, appelée à **3 sites** : (1) tête de `onTick` (le FILET H4 — un
  site d'invalidation oublié coûte un tick, jamais une dérive), (2) fin de `loadSave` (avant le seul
  `return true`), (3) création de partie neuve (après `ensureIslandDefaults`).
  ⚠ **CONTRADICTION INTERNE DU BRIEF, tranchée en faveur de la DÉCISION (P5 adapté)** : H1 exige que
  les soft caps gardent 2,7 « **y compris sous leur seuil** », mais le patch P5 verbatim
  (`return Math.pow(UPGRADE_SCALE_CUR, level)`) aurait fait payer 2,6^k au puits (< Nv.5) et à
  l'éolienne (< Nv.10) alors que leurs branches au-dessus du seuil repartent d'un préfixe
  **2,7^4 / 2,7^9** → discontinuité au seuil + remboursements (`cumulativeInvested`) incohérents avec
  ce qui a été payé. La branche finale teste donc le buildingId (`soft ? BASE : CUR`). Mesuré (Y2) :
  avec `upg = 10`, puits Nv.3 = 2,7³ ET Nv.8, éolienne Nv.7 ET Nv.12, aciérie Nv.5 ET Nv.12 — tous
  STRICTEMENT identiques à recherche 0.
  ⚠ **`UPGRADE_SCALE` N'EXISTE PLUS** (séparé en `_BASE` soft caps / `_CUR` barème normal) :
  `grep 'UPGRADE_SCALE[^_]'` hors commentaires = **0**. Ne jamais réintroduire un nom unique.
  ⚠ **`COLLIDER_START` reste la PÉRIODE de la sinusoïde (600, JAMAIS réduite — H2)** ; seule la
  DURÉE de démarrage passe par `colliderStartOf()` (4 sites : fin de démarrage + clamp du timer,
  fiche « Démarrage restant », rampe du son). **G11 (l'oscillation) est resté INCHANGÉ** — vérifié au
  SHA. Mesuré (Y8) : avec `boot = 10` (démarrage 300 ticks, mesuré), les maxima de `co.want` sur
  1 200 ticks restent espacés de **600**, pas de 300.
  ⚠ **Séparateur : mutation GLOBALE de `BUILDINGS.separateur_cryogenique.outputs` depuis la base
  figée `CRYO_BASE_OUT`** (H5) — l'avertissement « ne jamais muter b.outputs » vise la mutation par
  INSTANCE ; recalculer depuis la valeur COURANTE composerait l'effet à chaque tick (l'hélium
  partirait à l'infini). Idempotence mesurée (Z7) : 20 appels → valeurs stables. Au cap : He3
  0,025937 · He4 0,259374 · méthane 0,714689, somme = 1 (±1e-12).
  ⚠ **Points DÉRIVÉS, jamais stockés (H7)** : `researchPointsEarned` = fonction pure, gatée sur le
  nœud 43 confirmé ; **boucle entière, PAS `Math.log2`** (H8). Frontières exactes mesurées (Z2) :
  [9 999, 10 000, 19 999, 20 000, 40 000, 80 000, 10 000·2²⁰] → **[0, 3, 3, 4, 5, 6, 23]**.
  `researchPointsAvailable` est clampée à 0 (un dépensé > gagné — save d'une version future — ne
  rend jamais un négatif).
  ⚠ **Coûts fractionnaires → `Math.round` sur le coût FINAL** de `portUpgradeCost` /
  `elevatorUpgradeCost` / `extensionCost` (H9) : à recherche 0, round sur `2^n` et `10^n` est
  l'identité — **vérifié à l'octet** (série X).
  **MÉTHODE DE NON-RÉGRESSION (série X)** : la table de référence est calculée par le VRAI code
  14.59 — copie jetable `_ref1459.html` du commit précédent avec **un export `upgradeCost` injecté
  dans `__heat`** (seule fonction manquante), jamais une réimplémentation du harnais. 20 bâtiments ×
  31 niveaux + 7 courbes de facteurs + ports 5×21 + élévateur 16 + remblai 9 : **identiques à
  l'octet** (JSON.stringify). X8 = partie réelle 600+ ticks avec **montage logique COMPLET**
  (émetteurs → XNOR → vanne) : 38 manches, 20 confirmations, **0 pénalité, 0 arrêt, 0 tickError**.
  Validé : `node --check` (**7 blocs, 7 OK**) + Chromium **2 suites, 27 assertions, 0 KO, 0
  `pageerror`**, rejouées **3 fois sans flottement** (X1→X8, Y1→Y9, Z1→Z8 tous PASS). Persistance
  réelle : save écrite/rechargée (Z4 : niveaux conservés, `UPGRADE_SCALE_CUR` 2,63 réaligné au
  chargement), save privée de `research` (Z5 : tout à 0 = 14.59), save trafiquée `{upg:47,
  transit:-3}` (Z6 : clampée 10/0), mode Difficile par la vraie ModeModal (Z8).
  ⚠ **PIÈGES DE HARNAIS (coûteux)** : (a) **le piège (h) frappe encore** — `localStorage.clear()`
  dans un `addInitScript` rejoue au reload et la course clear ↔ flush-`pagehide` donne des boots
  **INCOHÉRENTS d'un run à l'autre** (slot actif recréé, save perdue ou non) : symptôme vu ici,
  `research` rechargé correct mais barèmes restés à 2,7 dans un run, partie neuve dans l'autre —
  TOUJOURS garder le clear derrière un drapeau ; (b) **le montage logique du Collisionneur exige 3
  réseaux DISJOINTS** (α_col → XNOR ; α_DC → XNOR ; sortie → vanne) : le flood-fill des fils est
  4-dir, **une seule adjacence parasite court-circuite tout** (1 pénalité + machine éteinte au
  premier essai) — router en évitant les faces β/S, γ/O et VALIDE/E des émetteurs ET les tuiles
  voisines de la vanne ; un fil logique posé PAR-DESSUS le landmark (t.logic en surcouche) fonctionne ;
  (c) pour X8, l'état du **Data Center est ré-affirmé à chaque tick** (`active/inFac/pwrAvg/heat`) —
  son alimentation complète (azote + conduit + tour depuis 14.36) n'est pas l'objet du test ;
  (d) Z3 : donner assez de confirmations pour `earned > spent`, sinon le clamp à 0 d'`available`
  fait échouer l'assertion naïve `available === earned − spent`.
  ⚠ **Taille : 3 164 819 → 3 175 388 o (+10 569 o).**
  ⚠ **`__heat` étendu** : `colliderStartOf`, `RESEARCH_DEFS/KEYS/CAP/FIRST/FIRST_POINTS`,
  `CRYO_BASE_OUT`, `researchLvl`, `researchPointsEarned/Spent/Available`, `refreshResearchEffects`,
  `upgradeCost`, et **`researchScales()` en GETTER** (les barèmes sont des `let` de module — les
  figer dans l'objet rendrait les tests mensongers).
  ⚠ **HORS PÉRIMÈTRE, non touché (§3)** : l'achat, le remboursement, l'interface (**lot 3B** — aucun
  bouton, aucune écriture joueur de `techTree.research`), le régime de déficit (lots 1-2), la file
  FIFO, `COLLIDER_POWER`/`COLLIDER_RAMP`/`COLLIDER_HE3`/`COLLIDER_GOALS`, `colliderGoalLocked`,
  `colliderPalier`, `TIER_STEP`/`TIER_NEXT`, le CONTENU de `COST_SOFTCAP_X2`, `PORT_BASE_COST`,
  `ELEVATOR_BASE_COST`, la recette du Séparateur hors `outputs` (intrants/conso/sigmoïde), l'île 8
  (`researchPointsSpent` est le helper prévu pour son branchement, sans condition active).
- **État précédent : `GAME_BUILD = 341`, `GAME_VERSION = 'Alpha 14.59'`, `SAVE_VERSION = 31`.**
  Changement 14.59 (brief `BRIEFHOTFIXP13ETSOUTERRAINFIFOL2`, **LOT 2, deux sous-lots indépendants**) :
  **(§A) le Collisionneur ne peut plus être jugé au premier tick d'une session ; (§B) la construction
  SOUTERRAINE passe du proportionnel à une FILE STRICTE.** `SAVE_VERSION` INCHANGÉ ; seul champ ajouté
  `cb.seq`, **optionnel** avec réattribution au chargement. Base du brief EXACTE (340 / 14.58 /
  3 158 046 o) : les **10 ancres sont sorties UNIQUES**.
  (1) **§A — TICK BLANC** : `processCollider` tourne **AVANT** la boucle énergie, donc au tout premier
  tick `co.powered` vaut `undefined` (champ transitoire, jamais persisté) — et `undefined === false`
  étant FAUX, la machine était réputée **alimentée** et descendait droit au carburant. Deux dégâts,
  tous deux mesurés : (a) une partie rechargée sans He3 prenait un **ARRÊT TOTAL** (interrupteur qui
  tombe) là où le tick suivant aurait donné un arrêt `power` **récupérable seul** ; (b) `colliderDrawHe3`
  était appelé et **prélevait réellement l'hélium** sur une partie **sans câble relié** — exactement ce
  que le tout-ou-rien de 14.24 existe pour empêcher. Désormais, tant que `typeof co.powered !== 'boolean'`,
  on ne juge RIEN : `want`/`cur`/`he3Used`/`palier`/`goal` seulement, puis `return`.
  ⚠ **Ce n'est PAS une tolérance au sens de D1 (14.58)** : D1 porte sur un déficit **CONSTATÉ**, ici le
  capteur n'a pas encore parlé. **D1 reste intégralement en vigueur** (V1→V8 rejoués, tous PASS).
  ⚠ **Tick BLANC au sens strict** : ne touche NI `halt`, NI `_haltPrev`, NI `state`, NI `timer`, NI
  `stops`, NI `enabled` — sinon un simple rechargement compterait un **arrêt fantôme**.
  (2) **§B — FILE STRICTE (FIFO)** : l'enveloppe « construction » de l'élévateur était répartie
  **proportionnellement** → dix bâtiments posés ensemble descendaient tous au dixième de la vitesse et
  aucun n'aboutissait. Désormais **un chantier à la fois**, trié sur `cst.seq` (ordre de CRÉATION, pas
  la géométrie de la grille), **construction et amélioration dans la MÊME file** (vérifié).
  ⚠ **`consDem` = `rem` de la TÊTE SEULEMENT**, pas la somme de la file (décision E2) : sinon la
  catégorie réclamerait le débit de toute la file et l'**immobiliserait sans l'utiliser** — le FIFO
  gaspillerait l'élévateur au lieu de le sérialiser. Mesuré à débit 16 384 : demande totale **4**
  (= la tête), le reliquat reste disponible pour les sortants/intrants.
  ⚠ **AUCUN débordement (E1)** : les chantiers en attente reçoivent **zéro**, même s'il reste du débit.
  ⚠ **UN TICK DE LATENCE entre deux chantiers (E5)**, assumé : quand la tête se finalise, la suivante
  ne prend le relais qu'au tick suivant. Mesuré sur 3 chantiers de 32 u à 16 u/s : finalisations aux
  ticks 1 / 3 / 5, **0 chevauchement** (jamais deux `rate > 0` au même tick).
  ⚠ **`nextUgSeq` porte son compteur sur `game`, PAS au niveau module** : un compteur de module n'est
  jamais réinitialisé au changement de partie ni au chargement — c'est exactement le défaut corrigé en
  14.51 sur `_elevTileCache`. `game.ugSeq` est **DÉRIVÉ au chargement** (`max + 1`), jamais persisté.
  ⚠ **ÉCART AU BRIEF (P19)** : le §5 propose `gameRef.current` pour `restoreUgSeq` ; dans `loadSave` la
  variable en portée est **`g`** (l'objet de partie en cours de construction), `gameRef.current` ne
  pointe pas encore dessus. Corrigé, et la fonction est appelée **après la boucle des îles**.
  (3) **UI** : la fiche d'un chantier en attente affiche **« en attente · N chantier(s) devant »** au
  lieu d'un « en construction · 0 % » qui ne bouge pas (sans quoi le joueur croit l'élévateur en
  panne) ; le panneau Élévateur gagne **« Chantiers en file : N · un seul servi à la fois »**, masquée
  quand la file vaut 0 ou 1 (`consQueue` ajouté à `elevatorFlow`).
  Validé : `node --check` (**7 blocs, 7 OK**) + Chromium **2 suites, 21 assertions, 0 KO, 0
  `pageerror`** (seul bruit : le **404 PRÉEXISTANT** du serveur de test), suites **rejouées 2 fois sans
  flottement**. §A : W1 (tick 1 → `enabled` true, `he3Used` 0, **stock He3 strictement inchangé**,
  `stops` 0, état intact), W2 (tick 2 → `halt 'power'`, `stops` 1, jamais `'fuel'`), W3 (600 ticks
  nominaux → `stops` 0), W4 (**non-régression du lot 1** : V1/V2/V3/V4/V8 tous conformes). §B : W5
  (3 chantiers → `rate` 16/0/0, `queue` 0/1/2), W6+W15 (**0 chevauchement**, finalisations
  séquentielles), W7 (E2 : demande = tête seule), W8 (construction + amélioration dans UNE file), W9
  (fiche « en attente · 1 chantier(s) devant »), W10 (`consQueue` 3/1/0), W11 (**pose RÉELLE par
  `tryPlace` ×3** → `seq` 1<2<3, save réelle écrite avec `cb.seq`, rechargée → **ordre identique**,
  `ugSeq` 3), W12 (save **privée de `cb.seq`** → rangs réattribués dans l'ordre de balayage, 0
  `tickError`), W13 (1 seul chantier → **16 u/s, débit plein, aucune régression**), W14 (souterrain non
  relié → rien n'avance, 0 erreur).
  ⚠ **PIÈGES DE HARNAIS (coûteux)** : (a) **`localStorage.clear()` dans un `addInitScript` REJOUE À
  CHAQUE reload** → un test de rechargement repart sur une partie NEUVE (piège (h) déjà documenté en
  14.47, retombé dessus) : garder le nettoyage derrière un drapeau posé au 1ᵉʳ boot ; (b) sans **ROUTE
  port ↔ tuile élévateur EN SURFACE** (île 6), `undergroundBlocked` est vrai depuis 14.50 et
  l'élévateur ne descend RIEN — on mesure alors le blocage, pas la file (m'a donné 5 faux KO) : le
  harnais trace la route par un BFS sur l'île 6 ; (c) `tryPlace` échoue en silence si le **port de
  l'île 6** ne couvre pas le coût (la construction souterraine y est payée) → remplir **toutes** les
  clés de `RES_SHORT`, pas une liste choisie à la main ; (d) après un `reload`, attendre que
  **`window.__gameRef.current.islands[7]`** existe : `!document.getElementById('splash')` ne suffit
  pas, `gameRef.current` est encore `null` un instant.
  ⚠ **Taille : 3 158 046 → 3 164 819 o (+6 773 o).**
  ⚠ **HORS PÉRIMÈTRE, non touché (§3)** : tout le régime de déficit du lot 1 (`co.halt`, `co.stops`,
  `stLabel`, toasts, astuce), `elevatorAllocate`, `elevatorRateAt`, `ELEVATOR_BASE_RATE`, les 3 modes
  de répartition, l'ordre des catégories, le bridage `elevInFac`/`elevOutFac` (14.05), `underWorks` et
  le refus d'empiler deux travaux sur un même bâtiment, les foreuses, l'échappement, la chaleur de la
  cage, et **toute recherche infinie / réduction de coût / remboursement : LOT 3.**
  ⚠ **Aucun réordonnancement par le joueur (E6)** : la file est strictement FIFO, à rouvrir seulement
  si l'usage le réclame.
- **État précédent : `GAME_BUILD = 340`, `GAME_VERSION = 'Alpha 14.58'`, `SAVE_VERSION = 31`.**
  Changement 14.58 (brief `BRIEFCOLLISIONNEURDEFICITL1`, **LOT 1**) : **LE COLLISIONNEUR NE TOLÈRE
  PLUS AUCUN DÉFICIT — revirement assumé de 14.17.** `SAVE_VERSION` INCHANGÉ ; le seul champ ajouté
  (`collider.stops`) est **OPTIONNEL avec repli `0`**. Base du brief EXACTE (339 / 14.57 /
  3 150 064 o) : les **14 ancres sont sorties UNIQUES**, aucune re-dérivation.
  (1) **Régime punitif** (`processCollider`) : déficit **ÉLECTRIQUE** → **ARRÊT** (`state = 'off'`,
  `timer = 0`, `launched = false`) — les 10 min de démarrage sont **intégralement reperdues** et la
  séquence est à relancer à la main ; déficit **HÉLIUM 3** → **ARRÊT TOTAL** (`enabled = false`),
  **aucune reprise automatique**, le joueur doit rebasculer l'interrupteur. **Aucune tolérance, aucune
  hystérésis** : un seul tick suffit (décision D1).
  ⚠ **L'ordre courant-AVANT-carburant (14.24) est ce qui rend le lot vivable** : un déficit He3 ne
  peut survenir que **courant présent** — sans lui, chaque micro-coupure ferait tomber l'interrupteur.
  Mesuré : courant ET He3 absents simultanément → `halt === 'power'`, `enabled` reste `true`.
  ⚠ **LA BOUCLE DE REDÉMARRAGE PERPÉTUEL EST VOULUE (D2), CE N'EST PAS UN BUG.** Un réseau qui ne
  tient pas le **pic de la sinusoïde** (période `COLLIDER_START`) fait redémarrer la machine, remonter
  sa rampe, retomber en déficit au même point, indéfiniment — elle peut ne **JAMAIS** atteindre
  `ready`. Mesuré sur 3 000 ticks à 60 % du pic : 1 806 ticks `starting` / 1 194 `off`, **5 arrêts**,
  `ready` jamais atteint. **NE PAS « CORRIGER ».**
  ⚠ **`co.want` reste publié pendant l'arrêt** (règle 14.17 CONSERVÉE, D7) : à `want = 0` la boucle
  énergie conclurait « alimenté » au tick suivant et la machine repartirait à plein → battement
  marche/arrêt à chaque tick.
  (2) **GARDE DE RÉAMORÇAGE indispensable** : la branche `off → starting` s'évalue **AVANT** le
  déficit → sans `&& !co._haltPrev`, une panne prolongée rejouerait `colliderBoot` **1×/seconde**.
  Mesuré : 60 ticks de panne → **0 `colliderBoot`, 1 seul `colliderHalt`, `stops` +1** (et non 60).
  Conséquence assumée : le réamorçage est **décalé d'exactement 1 tick** après la fin du déficit
  (tick N : rien ; tick N+1 : `starting`, `timer = 1`, un seul boot).
  (3) **Compteur `co.stops` SÉPARÉ de `co.penalties` (D3)** : `penalties` désigne une **faute de
  câblage** (vanne ouverte sur codes différents), `stops` une **panne de réseau**. Les mélanger
  rendrait la fiche mensongère. Vérifié : sur V1→V9 `penalties` reste 0 ; une pénalité de vanne
  laisse `stops` inchangé. Nouvelle ligne **« Arrêts (déficit) »** dans la fiche (orange).
  (4) **`co.halt` N'EST PLUS EFFACÉ** par le retour anticipé `enabled === false` (D8) : sinon le tick
  suivant effacerait la cause et la fiche afficherait « éteint (par le joueur) » alors que le joueur
  n'a rien éteint. La purge se fait dans `toggleCollider`.
  ⚠ **ÉCART AU BRIEF — `toggleCollider` purge `halt` dans LES DEUX SENS**, pas au seul allumage
  (`if (on)` du P8) : sans cela, une extinction **manuelle** faite après un déficit électrique
  hériterait de « ARRÊTÉ — électricité insuffisante ».
  ⚠ **TROU DU BRIEF, comblé** : le §4 liste l'ancre **A13** (`const halted = repaired && colOn &&
  co.halt;`) mais **aucun patch ne l'utilise** — or après un arrêt total He3 la machine EST
  `enabled === false`, donc `halted` valait faux et le libellé retombait sur « éteint (par le
  joueur) », **exactement le contresens que D8 cherche à éviter** (V6 aurait échoué). `colOn` est
  retiré de la condition et `halted` **remonté devant `!colOn`** dans `stLabel` **et** `stColor`.
  Même raison pour la **ligne « Hélium 3 »** (que le §3.3 met en périmètre) : son gate `repaired &&
  colOn` la faisait disparaître au moment précis où elle est utile → élargi à `co.halt === 'fuel'`.
  ⚠ **ASTUCE AJOUTÉE (§3.5)** : le périmètre exige « toast + astuce à la première occurrence » mais
  **le patch P12 ne décrit que le toast**. Nouvelle astuce `collider_arret` (`when: () => false`,
  ouverte par le TICK via `colliderStopNotify`, calquée sur `collider_penalite`).
  ⚠ **COMMENTAIRE 14.17 RÉÉCRIT, PAS CONSERVÉ** (§1) : il annonçait « aucune pénalité n'est possible
  pendant une panne » — laisser ça en place alors que le code inflige un arrêt serait le pire des
  deux mondes. Le commentaire de `toggleCollider` est aussi corrigé (il prétendait depuis 14.07 que
  le compte à rebours est « gelé » et repris au rallumage : **faux depuis 14.08**).
  ⚠ **ANOMALIE SIGNALÉE, NON CORRIGÉE** : `processCollider` tourne **avant** la boucle énergie, donc
  au **tout premier tick d'une session** `co.powered` est `undefined` (champ transitoire, jamais
  sauvegardé) → la règle D5 ne peut pas s'appliquer et une partie rechargée **sans He3 ET sans câble**
  prend un arrêt **`fuel`** (interrupteur qui tombe) là où elle prendrait un arrêt `power` au tick
  suivant. Fenêtre d'un seul tick, comportement défendable (il n'y a effectivement pas d'He3) — à
  arbitrer si un joueur le signale.
  ⚠ **HORS PÉRIMÈTRE, non touché (§3)** : `colliderPenalty` et tout le circuit de pénalité,
  `colliderGoalLocked`, `colliderPalier`, `colliderDrawHe3` et son tout-ou-rien, `COLLIDER_START`,
  `COLLIDER_RAMP`, `COLLIDER_POWER`, `COLLIDER_HE3`, `COLLIDER_GOALS`, la garde `!!co.halt` de
  `colliderLoopFrame` (devenue redondante mais strictement neutre), `SAVE_VERSION`, et **toute
  recherche infinie / réduction de coût / remboursement / île 8 : LOTS ULTÉRIEURS.**
  Validé : `node --check` (**7 blocs, 7 OK**) + Chromium **2 suites, 31 assertions, 0 KO, 0
  `pageerror`** (le seul bruit console est le **404 PRÉEXISTANT** du serveur de test), suites
  **rejouées 2 fois sans flottement**. Moteur : V1→V16 du brief tous PASS, dont V9 (boucle
  perpétuelle observée = PASS attendu), V12 (**0 He3 prélevé** pendant un déficit électrique :
  stock 990 → 990), V13 (**save RÉELLE** écrite puis **rechargée** : SAVE 31, `stops` conservé,
  `enabled: false`), V14 (save **privée de `stops`** → 0, 0 `tickError`), V16 (la boucle audio
  s'arrête à l'arrêt et repart avec la rampe). **UI RÉELLE** (vrais clics souris, tap canvas réel sur
  le landmark) : toast rouge nommant la cause, **astuce `collider_arret` affichée**, fiche
  « ARRÊTÉ — électricité insuffisante, démarrage perdu » puis « ARRÊTÉ — hélium 3 manquant, relance
  manuelle » (**et non** « éteint (par le joueur) »), ligne « Arrêts (déficit) », ligne Électricité
  reformulée, **clic réel sur « ▶ Allumer le Collisionneur »** → `halt = null`, `enabled = true`,
  toast « relancé ».
  ⚠ **PIÈGES DE HARNAIS (nouveaux, coûteux)** : (a) **`co.powered` est réécrit par la boucle énergie
  à chaque tick** → un déficit électrique forgé ne survit pas : le **ré-affirmer en continu**
  (`setInterval` ~25 ms), même patron que `conduitLoad` (14.51) et `collider.state` (14.57) ;
  (b) piloter `processCollider` **directement** (exposé dans `window.__heat`) évite toute la boucle
  de jeu, mais il faut alors fournir l'He3 par un **vrai réseau tuyau adjacent** à `colliderBounds` —
  et **le réapprovisionner**, sinon les tests suivants héritent d'un pool vide et partent en arrêt
  `fuel` (m'a donné 2 faux KO) ; (c) une save forgée dans `localStorage` est **écrasée par le flush
  `pagehide`** à la navigation — geler `Storage.prototype.setItem` ne suffit pas, il faut la
  **réinjecter dans un `addInitScript`** (qui s'exécute avant le script du jeu) ; (d) tester le repli
  d'un champ absent exige que la machine soit **`enabled: false` dans la save forgée**, sinon le 1ᵉʳ
  tick réel la fait légitimement s'arrêter et `stops` repasse à 1 — on mesure alors le tick, pas le
  chargement ; (e) `switchIsland` n'est **pas globale** (portée React) → passer par `window.__ui()` ;
  (f) débloquer les îles ouvre l'astuce « Transport inter-îles » qui **occupe le canal popup** →
  l'astuce d'arrêt est différée (et NON marquée vue) : purger **avant** d'armer le déficit ;
  (g) les toasts s'effacent seuls → les capter par **`MutationObserver`**, pas par une lecture
  ponctuelle du DOM.
  ⚠ **Taille : 3 150 064 → 3 158 046 o (+7 982 o)**, dominée par les commentaires de décision.
- **État précédent : `GAME_BUILD = 339`, `GAME_VERSION = 'Alpha 14.57'`, `SAVE_VERSION = 31`.**
  Changement 14.57 (brief `BRIEFSFXBspatial`, **BRIEF B**, suite directe du A) : **LES BOUCLES SUIVENT
  LA CAMÉRA (volume + panoramique stéréo) ET LA CENTRALE NUCLÉAIRE REÇOIT SA VOIX.**
  `SAVE_VERSION` INCHANGÉ, aucun champ persisté ajouté (les 2 caches sont VOLATILS — vérifié sur une
  save réelle : ni `_colliderBox` ni `_nucVoice` n'y figurent, `serialize` construisant une liste
  blanche). Base du brief EXACTE (338 / 14.56) : les **5 ancres sont sorties UNIQUES**.
  (1) **LOT 11 — moteur de boucle MULTI-VOIX** : table `LOOP_SPECS` en tête du module (collider :
  `triangle` 55 + `sine` 110,7 Hz, filtre 90→520 ; nuclear : `sine` 118 + `triangle` 237,4 Hz, filtre
  200→1600), `loopStart` retombe sur `collider` pour un id inconnu et reste **IDEMPOTENT** (10 appels
  → 0 nœud créé). ⚠ **Le DÉSACCORD de la 2ᵉ voix (110,7 et non 110 ; 237,4 et non 237) est
  INTENTIONNEL** — c'est lui qui produit le battement lent de « machine ». NE PAS ARRONDIR.
  **`StereoPannerNode` inséré entre le filtre et le gain** (`f → p → g → worldBus`), avec repli
  `f → g` si `createStereoPanner` n'existe pas. `worldBus` reste EN DUR (commentaire d'avertissement
  conservé). ⚠ **`loopStop` a dû être amendé** : il déconnectait une liste FIXE de nœuds — sans y
  ajouter `v.p`, chaque cycle aurait laissé un panner accroché au graphe (fuite). Mesuré : 20 cycles
  × 2 voix → **80 oscillateurs et 40 panners créés PUIS relâchés**, 0 voix orpheline.
  (2) **LOT 12 — `spatialPresence(game, r, c, w, h)`**, helper **PUR** (aucun DOM, aucun effet de
  bord), rendant `{pres, pan}` ou **`null`** si la caméra n'est pas prête (premier rendu :
  `cssW`/`cssH`/`tile` à 0) → l'appelant COUPE sa boucle au lieu de calculer sur des zéros.
  **UNE SEULE implémentation**, partagée par les deux sources (vérifié par grep : 1 seul
  `function spatialPresence`, 1 seul `0.20 + 0.80 * prox`, 1 seul `cam.zoom - MIN_ZOOM`).
  ⚠ **`zf` se calcule depuis `cam.zoom`, JAMAIS depuis `cam.tile`** : `tile = max(MIN_TILE,
  round(fitTile × zoom))` avec `fitTile` déjà borné à [26, 64] → sur un grand écran `tile` dépasse
  MAX_TILE et le facteur saturerait à 1 sur presque toute la plage. `cam.zoom` va de 1 à 4 quelle que
  soit la taille d'écran. Mesuré : centré+zoom max → `pres` **1,00** ; centré+zoom min → **0,60** ;
  hors champ lointain → **0,20** (plancher) ; `pan` borné à **±0,8**.
  (3) **LOT 13 — Collisionneur spatialisé** : `gain = 0.10 × (0.2 + 0.8×f) × pres`, `pan` transmis.
  ⚠ **`f` (avancement du démarrage) n'est PAS touché** et ne doit JAMAIS être rebranché sur `co.cur`
  (avertissement 14.38 toujours valable). L'emprise vient de **`colliderBounds` RÉUTILISÉE** (elle
  faisait déjà ce balayage — pas de 2ᵉ implémentation), mise en cache sur **`game._colliderBox`**
  (jamais au niveau module : leçon du 14.51, un cache de module survivrait à un changement de partie
  ou de mode). Le terrain ne bouge jamais → aucune invalidation. Mesuré en jeu réel : **0,100**
  centré+zoom max, **0,020** loin+dézoomé, **rapport exactement 5×**, pan −0,8 / +0,8 aux extrêmes.
  (4) **LOT 14 — `nuclearLoopFrame(game)`**, inséré dans la frame ENTRE `colliderLoopFrame` et
  `reverbFrame`. `gain = 0.008 × f × pres`, `freq = 200 + 1400×f`.
  ⚠ **Gain 0,008 contre 0,100, et c'est VOULU** : à gain égal une fondamentale à 118 Hz est perçue
  **~2,8× plus forte** qu'à 55 Hz (pondération A) → le résultat est à ~22 % du Collisionneur en SONIE.
  Monter ce chiffre le rend immédiatement envahissant.
  ⚠ **AUCUN plancher sur `f`** (pas de `0.2 + 0.8×f` comme le Collisionneur) : à `nucCur = 0` le
  réacteur est **TOTALEMENT muet**. Un réacteur à l'arrêt qui ronronne serait un mensonge, et son
  silence est précisément l'information utile. Mesuré : `nucCur ≈ 0` → gain **1e-6** ; pleine
  puissance → **0,008** ; `stopping` → redescend en continuant de sonner ; `off`/`safety`/`damaged`
  → **`loopStop`, 0 `loopSet`**. `maxPower` reprend l'expression du moteur (`NUC_POWER_BASE × 2^upgrade`).
  **Localisation** : `maxPerIsland: 1` → au plus UNE voix, aucun mixage. Cache `game._nucVoice`
  rafraîchi **au plus toutes les 30 frames** (`NUC_LOOP_RESCAN`) ET immédiatement à tout changement
  d'île. Mesuré : démolition → voix coupée en **281-862 ms** ; construction → voix qui apparaît
  **sans rechargement**. ⚠ En navigateur HEADLESS la rAF est throttlée (~21 fps mesurés) → les
  30 frames y valent jusqu'à ~1,4 s ; à 60 fps réels c'est 0,5 s.
  ⚠ **CORRECTIF NON DEMANDÉ MAIS INDISPENSABLE** : `onHide` coupait `'collider'` seulement. La rAF ne
  tournant pas en arrière-plan, `nuclearLoopFrame` n'aurait JAMAIS été rappelée → **la voix nucléaire
  aurait tourné écran éteint**. `SFX.loopStop('nuclear')` ajouté au même handler.
  Validé : `node --check` (**7 blocs, 7 OK**) + Chromium **5 suites, 143 assertions, 1 KO** — le seul
  KO est le **404 PRÉEXISTANT** du brief A (contre-épreuve déjà faite sur la base 337 : identique),
  **0 `pageerror`**. Suites B rejouées 2 fois, suite A rejouée 5 fois, **sans flottement**. Les
  3 suites du brief A rejouées en NON-RÉGRESSION : réverbération du souterrain (0,294 sous terre →
  0,0005 en surface), 1 seul convolveur, catalogue toujours à 67 sons.
  ⚠ **PIÈGES DE HARNAIS (coûteux, nouveaux)** : (a) **`processCollider` réécrit `collider.state` en
  tête de CHAQUE tick** (sans carburant il retombe à `off`/`halt`) → une valeur forgée ne survit pas
  d'une frame à l'autre : la **ré-affirmer en continu** (`setInterval` ~25 ms), exactement comme
  `conduitLoad` en 14.51 ; **même piège pour `nucState`/`nucCur`** (le moteur nucléaire fait retomber
  la centrale en `stopping` sans carburant) ; (b) **ne JAMAIS lire `param.value` juste après
  `setValueAtTime`** : tant qu'aucun quantum de rendu n'est passé il renvoie la valeur PAR DÉFAUT
  (440 Hz, filtre 350 Hz) → faux KO ; espionner l'ÉCRITURE, c'est déterministe ; (c) le champ de
  version d'une save s'appelle **`version`**, pas `v` ; (d) pour mesurer les gains, espionner
  **`SFX.loopSet`** (ce qui est DEMANDÉ) et non le gain du nœud, qui est lissé sur 0,15 s ;
  (e) une réflexion précoce d'IR se vérifie par **détecteur de PIC** (`d[i] − (d[i−1]+d[i+1])/2`) et
  non par seuil sur la valeur brute : la queue est du bruit ALÉATOIRE et le test flotte (1 faux KO
  sur 5 passes avant correction).
  ⚠ **Taille : 3 142 916 → 3 150 064 o (+7 148 o)**, sous le plafond de 8 Ko du brief.
  ⚠ **HORS PÉRIMÈTRE (§6), non touché** : spatialisation d'autres bâtiments (data center, géothermie,
  four à arc), ambiances par densité, la réverbération du brief A, `f` du Collisionneur, nouveaux sons
  ponctuels, `SAVE_VERSION`, `BUILDINGS`, `TECH_NODES`, sprites. **Le point conditionnel du §6
  (déplacer la construction du convolveur vers le DÉBLOCAGE de l'île 7) n'a PAS été fait** : il est
  gaté sur un constat de gel en APK réel, et aucun gel n'a été constaté à ce jour.
- **État précédent : `GAME_BUILD = 338`, `GAME_VERSION = 'Alpha 14.56'`, `SAVE_VERSION = 31`.**
  Changement 14.56 (brief `BRIEFSFXAsouterrain`, **BRIEF A — le brief B n'est PAS anticipé**) :
  **GRAPHE AUDIO À DEUX BUS + RÉVERBÉRATION PAR CONVOLUTION À L'ÎLE 7 + 2 sons + 4 câblages.**
  `SAVE_VERSION` INCHANGÉ, **aucun champ persisté ajouté ni modifié** (vérifié sur une save réelle :
  ni `wetSend`, ni `conv`, ni `reverb`). Base du brief EXACTE (337 / 14.55 / 3 136 543 o) : les
  **12 ancres sont sorties UNIQUES** (dont `src.connect(f)…` à **2** occurrences, comme annoncé).
  (1) **DEUX BUS** : `worldBus ─┬─► master` / `└─► wetSend ─► conv ─► master`, et `uiBus ─► master`.
  Les 3 helpers (`tone`/`noise`/`woosh`) visent une variable **`bus`** que `play()` bascule avant
  d'invoquer le son → **les 67 définitions du catalogue sont intactes**. `DRY_UI` (10 sons
  d'interface : click, clickAlt, tabSwitch, tabHover, panelOpen, panelClose, invalid, notify, save,
  mapOpen) → `uiBus` ; tout le reste → `worldBus`.
  ⚠ **`bus` est remis à `worldBus` dans un `finally`**, pas après l'appel : sans lui, un son qui LÈVE
  laisserait `bus` sur `uiBus` et **TOUS les sons suivants deviendraient secs** — bug silencieux,
  invisible hors île 7. Testé en forçant une exception dans `createOscillator`.
  ⚠ **La voix de boucle vise `worldBus` EN DUR, pas `bus`** : elle démarre HORS de `play()`, `bus`
  contiendrait la valeur laissée par le dernier son joué (mesuré : après un `click`, elle partirait
  sur `uiBus`). **0 `g.connect(master)` restant** (grep) ; les 3 `connect(master)` subsistants sont
  légitimes : `worldBus→master`, `uiBus→master`, `conv→master`.
  (2) **RÉVERBÉRATION — IR générée à la volée, STÉRÉO, construction PARESSEUSE** à la première
  descente (`buildReverb` appelé par `reverbFrame` seulement si l'île 7 est visée). Mesuré :
  **0 convolveur** après 30 frames en surface ET sur une partie de surface entière ; **56 ms** de
  génération à la première descente ; **1 SEUL `ConvolverNode`** après 20 aller-retours.
  Recette (0,8 s + 14 ms de pré-délai, `normalize = false`) : queue diffuse `exp(-6.9·t)` filtrée
  par un passe-bas à un pôle `k = 0,85 − 0,70·t` ; **6 réflexions précoces** (13/21/31/43/59/77 ms,
  amplitudes ±0,60→0,16), positions **×1,07 sur le canal droit** ; normalisation en **ÉNERGIE**
  (`g = 3,2/√Σd²`).
  ⚠ **La décroissance DOIT être exponentielle** : contre-épreuve numérique exécutée — en `(1−t)⁴`
  les incréments de RMS partent à −3,4 dB puis plongent à **−26,3 dB** (ça traîne puis se coupe net,
  on entend une salve de bruit), contre **−4,4 à −5,4 dB réguliers** en exponentiel.
  ⚠ **Normalisation en ÉNERGIE et non en crête** : mesurée à `Σd² = 10,240` exactement sur les DEUX
  canaux → allonger la queue ne monte plus le niveau, le réglage de durée reste indépendant du volume.
  ⚠ **Les réflexions précoces sont le composant le plus important** (sans elles : un bruit ajouté,
  pas un volume). Vérifiées : impulsion positive à **13,0 ms** à gauche, la MÊME à **13,91 ms** à
  droite, corrélation L/R **≈ 0,02** (canaux bien décorrélés).
  (3) **`reverbFrame(g)`** inséré dans la frame entre `colliderLoopFrame(g)` et `checkTutorial()`
  (wrapper module-level avec `try/catch`, même patron que `colliderLoopFrame`) : cible
  `currentIsland === 7 ? 0,30 : 0`, fondu `setTargetAtTime(…, 0,4)` → l'acoustique s'installe
  **derrière** le son d'ascenseur. Mesuré en jeu réel : 0 → **0,291** à la descente, retour à
  **0,0002** à la remontée, avec une valeur INTERMÉDIAIRE (0,105) pendant le fondu — donc pas de
  claquement. **Le mute agit sur `master`, donc en aval : rien à câbler.**
  (4) **2 sons** (**catalogue 65 → 67**), nouvelle section `/* --- Découverte --- */` :
  **`reveal6`** (woosh lowpass 60→900 Hz sur 1,2 s + montée 392/523/659/784 Hz + shimmer 1568 Hz,
  ~1,7 s, calé sur les 1800 ms de l'animation) et **`locate`** (880 → 1318 Hz, bref, neutre).
  (5) **4 câblages** : `reveal6` dans l'effet de révélation de l'île 6 (**après** la pose de
  `archiVu6`, appel direct — on est dans un `useEffect`, pas dans le tick) ; onglet « Transit
  archipel » `tabSwitch` → **`mapOpen`** (l'onglet EST la carte depuis 14.41, le son dormait dans le
  fichier depuis l'origine ; l'onglet île garde `tabSwitch`) ; **`locate`** sur le « Y aller » du
  panneau de surchauffe (affordance jusque-là totalement muette) ; **branche `placeC` SUPPRIMÉE** de
  la pose.
  ⚠ **`placeC` était INATTEIGNABLE** : 111 bâtiments en 1×1, **2 en 2×2** (les centrales nucléaires),
  aucun au-delà → le seuil de 6 tuiles ne pouvait rien viser, et l'abaisser à 4 n'aurait fait que
  renommer le son des centrales en rendant `placeHeavy` inerte à son tour. `placeC` **reste au
  catalogue**, en réserve. Vérifié en moteur réel : 1×1 → `place`, 2×2 → `placeHeavy`, `placeC` jamais.
  Validé : `node --check` (**7 blocs, 7 OK**) + Chromium **3 suites, 80 assertions, 1 KO** — le seul
  KO est un **404 PRÉEXISTANT** (contre-épreuve exécutée sur la base 337 : **identique**, ressource
  absente du serveur de test), **0 `pageerror`**. Suite 1 **rejouée 6 fois sans flottement**.
  Instrumentation : espion sur `AudioNode.prototype.connect` + marquage des nœuds à la création →
  le graphe est mesuré, pas supposé (routage des 10 sons DRY_UI et de 9 sons de monde vérifié
  un par un). **UI RÉELLE** (vrais clics souris) : `reveal6` sonne à l'ouverture du Port avec l'île 6
  fraîchement débloquée puis **PLUS JAMAIS** après fermeture/réouverture ; onglet archipel → `mapOpen`
  seul, onglet île → `tabSwitch` seul ; bouton 🔥 → panneau → « Y aller » → `locate`. **Save réelle**
  écrite puis **rechargée** : SAVE 31, `archiVu6` restauré, 0 `tickError`, horloge qui avance.
  **Mute** sous terre → plus aucun son créé (donc plus rien n'alimente la queue), démute → retour
  immédiat. **Arrière-plan/retour** : 1 seul convolveur, acoustique restaurée.
  ⚠ **PIÈGES DE HARNAIS (coûteux, nouveaux)** : (a) le navigateur de test est en locale **EN** →
  `button[title="Configuration du port (commerce)"]` ne matche pas : forcer
  `localStorage['archipel_lang'] = 'fr'` **dans l'`addInitScript`** ; (b) **forger un bâtiment
  déclenche une astuce** dont la `.tip-popup` + son `.research-backdrop` recouvrent la barre
  d'inventaire → purger les astuces **APRÈS la forge**, pas avant (et par de VRAIS clics souris :
  un `.click()` DOM est avalé par `useGhostGuard`) ; (c) pour un test d'UI de surchauffe, forger un
  bâtiment **`damaged`** (état PERSISTANT) plutôt qu'une chaleur qui monte (`heatEmit` est recalculé
  à chaque tick, piège 14.53) ; (d) comparer un échantillon brut d'IR au bruit ALÉATOIRE de la queue
  donne un test **INSTABLE** (2 faux KO sur 6 passes) → asserter sur la valeur SIGNÉE aux positions
  attendues et sur la corrélation L/R, jamais sur un argmax ; (e) `mine_fer` exige un gisement
  `resource` — pour un test de pose 1×1 sur `land`, prendre `cimenterie`.
  ⚠ **Taille : 3 136 543 → 3 142 916 o (+6 373 o)**, sous le plafond de 10 Ko du brief.
  ⚠ **HORS PÉRIMÈTRE, non touché (§4)** : **tout le brief B** (présence spatiale, panoramique stéréo,
  voix nucléaire) — `loopStart`/`loopSet`/`colliderLoopFrame` ne sont modifiés qu'au point 8.3 ;
  réverbération sur une autre île que la 7 ; ambiances de bâtiment ; réseau de délais en remplacement
  de la convolution ; sonorisation de `gaz_echappement` et de l'inhibition des portes logiques ;
  `SAVE_VERSION`, `BUILDINGS`, `TECH_NODES`, les sprites.
- **État précédent : `GAME_BUILD = 337`, `GAME_VERSION = 'Alpha 14.55'`, `SAVE_VERSION = 31`.**
  Changement 14.55 (brief `BRIEFEDITIONSCI`) : **DEUX ÉDITIONS AU LIEU DE TROIS — `TESTER_BUILD` est
  SUPPRIMÉ, le MODE DÉVELOPPEUR passe sous `DEV_BUILD`, `index.html` (PWA) est bâti depuis l'édition
  PUBLIQUE, et les 2 étapes CI à effet de bord sont GATÉES sur `main`.** `SAVE_VERSION` INCHANGÉ.
  Les **16 ancres (H1→H5, W1→W10, G1) sont sorties telles quelles** (H2 à `count == 2` par conception).
  (1) **`TESTER_BUILD` SUPPRIMÉ du HTML.** Une fois le Mode développeur passé sous `DEV_BUILD`, il ne
  gatait plus AUCUNE différence de jeu — seulement le canal de MAJ et le suffixe d'étiquette : édition
  testeur et édition publique étaient devenues identiques. Il ne reste que **2 commentaires**, 0 code.
  (2) **Le Mode développeur (construction/améliorations gratuites) passe sous `DEV_BUILD`**, avec le
  mode rapide ×10. ⚠ **`DEV_BUILD` n'est PAS `g.ui.dev`** : `g.ui.dev` est l'INTERRUPTEUR (Options),
  `DEV_BUILD` est le BUILD qui a le droit de l'afficher. **`toggleDev` est l'UNIQUE écriture de
  `g.ui.dev` dans tout le fichier** (vérifié par grep : les 2 autres occurrences sont les `dev: false`
  d'init) → avec `if (!DEV_BUILD) return;` en tête, l'édition publique ne peut JAMAIS l'activer.
  ⚠ **C'est ce qui rend le non-bump de `SAVE_VERSION` correct** : `dev` est absent de `uiPrefs`
  (vérifié) et les 2 chemins d'init le forcent à `false`. Mesuré en moteur réel : publique → coût
  débité EXACTEMENT ; dev + mode dev ON → 0 débité ; dev + mode dev OFF → coût débité.
  ⚠ **Piège connu, sans conséquence ici** : `loadSave` fait `if (!g.ui) g.ui = {…dev:false…}` → charger
  une save dans une session où `g.ui` EXISTE DÉJÀ conserve `dev`. Inatteignable en publique (dev ne peut
  jamais y devenir vrai) ; en dev c'est le comportement voulu.
  (3) **Canal de MAJ à 2 branches** : `apk: (DEV_BUILD ? d.apkDev || d.apk : d.apk) || d.url || ''`
  (**2 sites** : boot + vérification manuelle). `version.json` : `build/version/url/apk/apkDev/notes`,
  **`apkTester` supprimé**. Le repli `|| d.apk` couvre un `version.json` ANTÉRIEUR sans `apkDev`
  (testé : jamais de lien vide, repli en cascade jusqu'à `d.url` puis `''`).
  (4) **CI — 2 APK.** **PUBLIQUE** `ArchipelIndustry.apk`, appId **`fr.archipel.industry.pub`**, libellé
  « Archipel Industry ». **DEV** `ArchipelIndustryDev.apk`, **appId d'ORIGINE `fr.archipel.industry`**
  (pas de `-PappId`), libellé « Archipel Ind. Dev ».
  ⚠ **L'appId d'origine reste sur la DEV À DESSEIN** : c'est le seul APK installé aujourd'hui, le garder
  préserve son dossier de données WebView **donc la sauvegarde d'Ethan**. La publique prend un appId neuf
  que personne n'a. Réversible à coût nul TANT QUE personne n'a installé la publique.
  ⚠ **La PUBLIQUE est construite AVANT la DEV** pour que l'assertion d'appId par défaut de la dev ne
  puisse pas être satisfaite par un reste de la publique.
  ⚠ **Nouvelle étape `Assert appIds`** (hors brief, ajoutée) : `aapt2 dump badging` sur les 2 APK, échec
  dur si l'un n'a pas l'appId attendu **ou si les 2 sont identiques** (installation côte à côte
  impossible) → le V14 du brief devient une assertion CI au lieu d'un contrôle manuel.
  (5) **`index.html` (PWA / navigateur — SEUL canal des utilisateurs Apple) est bâti depuis l'édition
  PUBLIQUE**, avec une **assertion BLOQUANTE `grep -q "^const DEV_BUILD = false;$" index.html`**.
  ⚠ **NE JAMAIS RETIRER CETTE LIGNE** : c'est elle qui empêche la version web de partir en édition dev
  (construction gratuite pour tout le monde). Sabotage testé localement → le job échoue bien.
  (6) **GATE DE BRANCHE `if: github.ref == 'refs/heads/main'` sur EXACTEMENT 2 étapes** : `Sync
  version.json` (elle **pousse sur main**) et `Publish` (elle **recrée la release**). Build, artefacts
  et `Sync PWA` restent LIBRES → on peut vérifier une branche sans rien publier.
  ⚠ **C'est le correctif d'un vrai danger** relevé au lot précédent : `git push origin HEAD:main`
  n'était gaté par RIEN — un dispatch depuis une branche publiait des APK issus de code non mergé ET
  annonçait la version aux joueurs. Le `|| echo` qui avalait l'échec du push devient un `::warning::`.
  ⚠ **ORDRE DES 2 ÉTAPES GATÉES INVERSÉ (revue adversariale)** : `Publish` tourne AVANT `Sync
  version.json` — on téléverse les assets AVANT de les annoncer, sinon chaque run `main` ouvrait une
  fenêtre où `version.json` pointait un nom d'asset absent de la release (404), aggravée par le
  RENOMMAGE de l'asset public de ce lot. Si le push d'annonce échoue ensuite, la release est bonne et
  les joueurs voient simplement encore l'ancienne version : dégradation sûre.
  ⚠ **`concurrency.group` passe de global à PAR REF** (`android-apk-${{ github.ref }}`) : avec un
  groupe global + `cancel-in-progress`, un dispatch de vérification depuis une branche — l'usage même
  que le gate encourage — ANNULAIT un run `main` en cours, potentiellement entre le `gh release
  delete` et le `create` de Publish → release détruite.
  ⚠ **Durcissements de la même revue** : l'édition embarquée est vérifiée SUR L'ASSET des 2 APK
  (`grep DEV_BUILD` sur `assets/index.html` avant chaque gradle — la publique DOIT être `false`, la
  dev `true`) ; `Assert appIds` ne se désarme plus en silence (repli `aapt2` → `aapt` → **échec dur**,
  plus de `::warning::` muet) ; le `BUILD` extrait pour le cache `sw.js` est validé « entier non
  nul » (la garde d'avant comparait le sed à la MÊME variable : tautologique, un repli `0` passait) ;
  `_config.yml` réécrit (il documentait le pipeline à 3 éditions et affirmait que la source était
  l'édition dev — désormais : source = PUBLIQUE, `game-public.html` ajouté à l'exclude) ;
  `android.yml.patched` (copie périmée du workflow, 0 référence) SUPPRIMÉ ; les 2 commentaires
  au-dessus des lignes `apk:` réécrits (ils décrivaient encore testeur/apkTester avec le MAUVAIS
  sens de repli).
  (7) **`build.gradle` : COMMENTAIRE UNIQUEMENT** (`applicationId` inchangé, vérifié : 0 ligne de diff
  non-commentaire).
  Validé : `node --check` (**7 blocs, 7 OK**) + lint YAML + Chromium **55 assertions, 0 KO** (4 langues,
  2 éditions, moteur réel) + **5 min de jeu par édition** (300 ticks, 0 `tickErrors`, 0 erreur console).
  **CI RÉELLE — run #460 dispatché SUR LA BRANCHE** : succès, artefact produit, et **les 2 étapes gatées
  prouvées SAUTÉES par leurs conséquences observables** — `main` n'a reçu AUCUN commit de synchro
  (toujours `build 336`, clé `apkTester` encore là) et la release décrit toujours les anciennes éditions.
  Le run ayant réussi, l'étape non gatée `Assert appIds` (échec dur) est passée → les 2 appId sont bons
  et distincts.
  ⚠ **PIÈGE DE HARNAIS (coûteux, nouveau)** : les panneaux `.slot-list` **DÉFILENT**. Un bouton en bas de
  liste (ex. le toggle Mode développeur, y≈1060 dans un viewport de 900) est **hors viewport** :
  `elementFromPoint` rend `null` et le clic souris part dans le vide **sans aucune erreur** — le test
  conclut à tort que le bouton ne marche pas. Le `realClick` du harnais fait désormais TOUJOURS un
  `scrollIntoView({block:'center'})` puis vérifie que la cible est bien dans le viewport ET au-dessus.
  ⚠ **Autre piège** : les INFRA (`road`/`pipe`/`wire`) ont **`cost: {}`** (prix via `networkUnitCost`) →
  un test « le coût est débité » posé sur une route est VACUEUX (0 attendu, 0 mesuré, vert à tort). Poser
  un vrai bâtiment à `cost` non vide.
  ⚠ **ÉTAPE MANUELLE UNIQUE POUR ETHAN** : son APK installé (build 336, `DEV_BUILD=false`) lit `d.apk`,
  qui pointe désormais sur l'édition **publique**, d'appId DIFFÉRENT → Android l'installerait À CÔTÉ.
  **Une seule fois : installer `ArchipelIndustryDev.apk` depuis la release** (même appId → mise à jour en
  place, sauvegarde conservée). Ensuite les MAJ suivront `apkDev` toutes seules.
  ⚠ **Corollaire non listé par le brief** : une installation **TESTEUR** encore déployée tourne sur
  l'ancien code (`TESTER_BUILD = true`) et lit `d.apkTester || d.apk` ; `apkTester` disparaissant, elle
  retombe sur `d.apk` = la publique, d'appId différent → installation à côté. C'est la migration
  attendue (les 2 éditions étant devenues identiques), mais il faut le savoir.
  ⚠ **Taille : 3 134 927 → 3 136 543 o (+1 616 o)** — commentaires de décision + note de version
  (dont les commentaires de canal réécrits après revue).
  ⚠ **HORS PÉRIMÈTRE, non touché** : `SUPPORT_URL` (toujours placeholder vide → section soutien masquée),
  toute mécanique de jeu, le contenu de `sw.js` hors bump de cache, la signature APK et le keystore.
- **État précédent : `GAME_BUILD = 336`, `GAME_VERSION = 'Alpha 14.54'`, `SAVE_VERSION = 31`.**
  Changement 14.54 (brief `BRIEFBOOSTERDEVSOUTIEN`, lots A/B/C) : **LE BOOSTER DE VITESSE EST
  SUPPRIMÉ, le MODE RAPIDE ×10 devient exclusif au build dev (`DEV_BUILD`), et les Options gagnent
  une section « Soutenir le projet » (`SUPPORT_URL`).** `SAVE_VERSION` INCHANGÉ — les champs
  `boosterCharge`/`boosterOn` des anciennes saves sont **tolérés et ignorés**, plus jamais écrits.
  Base du brief EXACTE (3 137 904 o, build 335) : les **26 ancres sont sorties UNIQUES**.
  (1) **LOT A — SUPPRESSION RÉELLE, pas une mise à `false` de `BOOSTER_UI_ENABLED`.** C'est la
  décision clé : ce flag ne masquait que le BOUTON — la mécanique continuait d'accumuler une réserve
  invisible (recharge en jeu ET pendant le rattrapage hors-ligne). Tout est parti : `BOOSTER_MUL_BY_ISLAND`,
  `BOOSTER_MAX`, `BOOSTER_RECHARGE_PER_SEC`, `BOOSTER_UI_ENABLED`, `boosterMulAvailable`,
  `fmtBoosterTime`, `highestUnlockedIsland`, le 6e bouton de la barre du bas, son CSS, `toggleBooster`,
  les 4 props de la `Toolbar`, l'init de partie neuve, le payload de sauvegarde, le bloc de rattrapage
  hors-ligne, le bloc de la boucle de frame, l'astuce `boost`, sa scène `TIP_SCENES` et le sprite
  `ui_booster`. **La barre du bas passe de 6 à 5 boutons.**
  ⚠ **`highestUnlockedIsland` n'avait qu'UN appelant** (`boosterMulAvailable`) — vérifié avant retrait ;
  si un futur lot en a besoin, il faut la réécrire.
  ⚠ **`_realDt` disparaît avec le bloc** (aucun autre usage) ; **`prev` et `elapsedSec` restent utilisés**
  (respectivement par `g.tickAcc += …` et par 4 autres sites de `runCatchUp`) — ne pas les retirer.
  ⚠ **Le multiplicateur maximal du jeu tombe de ×100 (rapide × booster) à ×10, et à ×1 dans le build
  PUBLIC.** C'est VOULU. Le garde-fou `_maxTicks = min(200, _ts*5)` ne mord donc plus jamais (pire cas
  50 ticks/frame) ; il est conservé comme filet.
  (2) **LOT B — nouvelle constante de build `DEV_BUILD` (défaut `false`)**, posée juste après
  `TESTER_BUILD`. Le mode rapide ×10 (bouton chronomètre + case « Mode rapide » de l'écran de création
  + `toggleFastMode`) est gaté dessus. ⚠ **Ce n'est NI `g.ui.dev`** (toggle Options accessible à
  n'importe quel joueur) **NI `TESTER_BUILD`** (qui garde son rôle INTACT : sélection d'APK + retrait du
  Mode développeur — hors périmètre, non touché, vérifié). Étiquette de version corrigée :
  `TESTER_BUILD ? ' · test' : DEV_BUILD ? ' · dev' : ''` — l'ancienne affichait « · dev » dans le build
  PUBLIC, ce qui devenait trompeur.
  ⚠ **AUCUN forçage à ajouter au chargement** : `loadSave` fait déjà `g.timeScale = 1` (« jamais restauré
  depuis la save ») → une save faite en ×10 dans le build dev rouvre en ×1 en public (vérifié V7).
  ⚠ **La ligne `^const TESTER_BUILD = false;$` reste intacte et sur sa propre ligne** — la CI la
  substitue par `sed` avec un `grep` de garde. Le motif `^const DEV_BUILD = false;$` est écrit pour être
  substituable de la même façon (vérifié au `sed` sur les 4 variantes de test).
  ⚠ **HORS PÉRIMÈTRE, à faire séparément** : la variante d'APK dev dans le workflow CI (ce lot
  n'introduit que la constante qui la rendra possible).
  (3) **LOT C — section « Soutenir le projet »** en bas des Options, gatée sur `SUPPORT_URL`
  (**PLACEHOLDER vide = section entièrement masquée** — à renseigner par Ethan avant publication).
  `<a>` `target="_blank"` `rel="noopener"`, gabarit `slot-new opt-fullbtn` dans un `.slot-row`, avec la
  phrase « Archipel Industry est gratuit et le restera. » VISIBLE (pas seulement en `title` — un tooltip
  est inatteignable au doigt). **AUCUNE règle CSS ajoutée** : seul un `style` inline remet
  `display:block/textAlign/textDecoration` sur le `<a>`. Aucune contrepartie en jeu.
  i18n en/es/de des 2 nouveaux libellés (nouveau bloc d'augmentation `/* 14.54 */`).
  ⚠ **LE NETTOYAGE i18n DU BRIEF EST UN NO-OP, vérifié** : ni « Booster » ni l'astuce booster
  n'existaient dans les 4 catalogues LOCALES (ils étaient en repli fr, comme les astuces depuis 13.32)
  → rien à retirer.
  ⚠ **`fx_boost` / `fx_boost_productivite` CONSERVÉS** (overlays d'influence d'antenne, aucun rapport
  avec le booster) : le bloc d'overlay et les 2 sheets sont **BYTE-IDENTIQUES à la base** (SHA-256
  `46a03f72…`, 1 659 o). Seules les **3 ops `"s": "fx_boost"` de la scène d'astuce supprimée** partent —
  le §V4 du brief (« count inchangé ») est donc littéralement infaisable, et c'est normal.
  Validé : `node --check` (**7 blocs, 7 OK**) + Chromium **2 suites, 77 assertions, 0 KO**,
  **2 passes identiques**, viewport 420 px / DPR 3, sur **4 variantes** (public / dev / testeur /
  SUPPORT_URL renseignée). **Save legacy RÉELLE** (créée par le vrai chemin puis réinjectée avec
  `boosterCharge: 1234.5` + `boosterOn: true`) → charge sans erreur, champs `undefined`, stock intact,
  horloge qui avance, 5 boutons. **Cadences MESURÉES sur 5 min** : public **300 ticks/300 s (×1)**, dev
  **3004 ticks/300 s (×10)**, 0 `tickErrors`, 0 `pageerror`, canvas vivant. **UI RÉELLE** (vrais clics
  souris) : chronomètre absent en public/testeur, présent en dev → clic → ×10 + `playclock-fast` + audio
  coupé + `_sfxPrev` mémorisé, re-clic → ×1 + audio restauré ; les 3 étiquettes de version ; les
  2 cartes de mode restent ALIGNÉES sans la ligne « Mode rapide » ; section soutien absente à
  `SUPPORT_URL = ''`, présente sinon (href/target/rel/pleine largeur/placée avant la ligne de version),
  traduite dans les 4 langues (**umlaut allemand correct, aucun `\xNN` visible**). Captures contrôlées.
  **Round-trip SHA-256 des 9 blocs réécrits : 9/9 à `count == 1`, 0 échappement parasite.**
  ⚠ **BRUIT DE CONSOLE — contre-épreuve BASE 335 vs PATCHÉ 336 faite** : les 2 seules « erreurs » sont
  un **404 `/favicon.ico`** (absent du serveur de test) et un **`ERR_CONNECTION_RESET` sur `VERSION_URL`**
  (fetch sortant vers `raw.githubusercontent.com`, bloqué en sandbox). **La base 335 les produit à
  l'identique** → ce ne sont PAS des régressions. **0 `pageerror` des deux côtés.**
  ⚠ **Taille : 3 137 904 → 3 134 927 o (−2 977 o).** Le CODE seul pèse **−3 653 o** (mesuré avant le
  bump) ; le retour à −2 977 vient du commentaire de version et de `GAME_NOTES`. Un delta POSITIF avant
  bump signifierait que la suppression a échoué.
  ⚠ **HORS PÉRIMÈTRE, non touché** : `TESTER_BUILD` et ses usages existants, le mode développeur
  `g.ui.dev`, le workflow CI, la vraie URL de don, `fx_boost`/overlays d'antenne, `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 335`, `GAME_VERSION = 'Alpha 14.53'`, `SAVE_VERSION = 31`.**
  Changement 14.53 (demande joueur : « à droite des alertes, lister tous les endroits où il y a une
  surchauffe ») : **bouton 🔥 SURCHAUFFES + panneau listant TOUS les points chauds, toutes îles
  confondues.** `SAVE_VERSION` INCHANGÉ — **affichage seul**, aucun champ persisté, aucune règle de
  simulation touchée.
  (1) **`activeHeatAlerts(game)`** (module, à côté d'`activeStockAlerts`/`activeEnergyAlerts`) réunit
  **4 cas** : **(a)** bâtiment **ENDOMMAGÉ** par surchauffe (+ délai avant réparation, via `dmgTimer`
  vs 300) ; **(b)** source qui **ACCUMULE** — jauge `heat / heatCapOf` **≥ `HEAT_ALERT_FRAC` (0,1)`
  ET en HAUSSE** (`heatEmit > heatCool`), avec le % et le délai estimé avant panne ; **(c)** réseau
  **CONDUIT dont la CRÊTE dépasse le débit** (`conduitPeak` vs `tuiles × conduitDebit`, indicateur
  14.31 : la panne est annoncée AVANT d'arriver) ; **(d)** **TAMPON DE LA CAGE** saturé
  (`elevatorHeat ≥ elevatorRateAt`). Tri : le pire d'abord (endommagé → cage → conduit → jauge
  décroissante) ; **rouge** dès `HEAT_HOT_FRAC` (0,8) ou endommagé, orange sinon.
  ⚠ **Une chaleur GELÉE ou qui RETOMBE n'est JAMAIS listée** (`rate > 1e-9` exigé) : elle ne mènera
  pas au trip, et l'y faire figurer aurait rempli la liste de faux positifs permanents. Même seuil
  (10 % + en hausse) que le toast d'alerte existant → aucune incohérence entre les deux signaux.
  ⚠ **Un conduit ILLIMITÉ n'est jamais en crête** (il ne sature pas) ; ses sources, si elles
  chauffent, sont déjà listées au cas (b) → pas de doublon.
  ⚠ **Emprise multi-tuile comptée UNE fois** : seule l'ancre porte `.building` (les autres ont
  `.occupied`) → le balayage de tuiles visite chaque bâtiment une seule fois (vérifié sur la centrale
  2×2). Les **îles verrouillées** sont ignorées.
  (2) **Bouton `.inv-heat-btn`** dans la barre d'inventaire, **À DROITE de `.inv-alert-btn`** (états
  replié ET ouvert), rouge pulsant, sprite **`ui_chaleur`** déjà présent, badge = nombre de points
  chauds. Visible **seulement s'il y en a ≥ 1**, comme le bouton d'alerte.
  (3) **`HeatPanel`** réutilise le CSS du panneau d'alertes (`alerts-*`, `alert-row`). ⚠ **Le clic sur
  une ligne (ou « Y aller ») bascule sur l'île ET CENTRE la caméra sur la tuile** — nouveau
  **`centerOnTile(r, c)`** (`centerCam` ne centre que la grille entière ; inverse de `pointerToTile` :
  `camX = baseX + (c + 0,5) × tile − cssW/2`). Sur une grande île, « il y a une surchauffe » sans le
  « où » ne sert à rien : c'est la raison d'être des coordonnées affichées.
  ⚠ **3 itérations de MISE EN PAGE, mesurées à la capture** (à ne pas refaire) : (a) une seule chaîne
  « nom (r,c) — état » débordait sur **4 lignes** en 420 px → scindée en 2 lignes (nom / sous-ligne) ;
  (b) `.alert-res` hérite de **`text-transform:capitalize`** → rendait « Panne Dans ~12 S » : il faut
  `text-transform:none` (comme `.alert-energy`) ; (c) la 4ᵉ colonne de MJ ne laissait que **~80 px** au
  nom (tronqué à « Ma… ») → grille ramenée à **3 colonnes** `[île][nom+état][Y aller]`, les MJ ne
  restant que pour le conduit et la cage (où le rapport crête/débit EST le diagnostic) ; les
  **coordonnées ouvrent la sous-ligne** (collées au nom, elles étaient les premières rognées) et la
  sous-ligne **passe à la ligne au lieu d'ellipser** (tronquer perdait « panne ~9 s », l'info la plus
  utile). Lignes finales : **46 px**.
  Validé : `node --check` (**7 blocs, 7 OK**) + Chromium **14 suites, 188 assertions, 0 KO, 0 erreur
  JS**, **2 passes identiques**. Helper : 15 assertions (les 4 cas, chaleur gelée/en baisse/sous le
  seuil NON listées, tri, emprise 2×2, île verrouillée, cage). **UI RÉELLE** : bouton absent sans
  surchauffe, présent avec badge « 2 », **position vérifiée à droite des alertes** (index 3 → 4), clic
  réel → panneau, les 2 bâtiments NOMMÉS avec leurs coordonnées, la ligne à 92 % **en rouge et en
  premier**, et **« Y aller » centre la caméra à 0,12 tuile près**. Captures d'écran contrôlées.
  ⚠ **PIÈGE DE HARNAIS** : `processHeat` **RECALCULE `heatEmit`/`heatCool` à chaque tick** → un
  bâtiment forgé non alimenté retombe à `heatEmit = 0`, donc « plus en hausse », donc absent de la
  liste (et sa chaleur finirait par TRIPPER). Pour un test d'UI il faut **ré-affirmer l'état forgé en
  continu** (`setInterval` ~40 ms), exactement comme pour `conduitLoad` (piège 14.52).
  ⚠ **HORS PÉRIMÈTRE, non touché** : `processHeat`, `heatCapOf`, `heatEmitMaxOf`, les seuils de trip,
  les toasts existants (`heatWarn`/`heatTrip`), `AlertsPanel`, `SAVE_VERSION`, et **aucune traduction**
  des ~10 nouveaux libellés (repli fr hors-fr, comme les astuces depuis 13.32).
- **État précédent : `GAME_BUILD = 334`, `GAME_VERSION = 'Alpha 14.52'`, `SAVE_VERSION = 31`.**
  Changement 14.52 (brief `BRIEFMOTEUR2DIAGNOSTICELEVATEUR`, **lot J** + **lot H confirmé sur la SAVE
  RÉELLE**) : **livraison du pack de 241 sprites** (l'inhibition des portes et les réseaux morts
  deviennent VISIBLES) et **clôture du diagnostic de l'élévateur**. `SAVE_VERSION` INCHANGÉ — ce lot
  n'ajoute que des sprites.
  (1) **LOT H — CONFIRMÉ sur la partie du joueur : branche B-D, IL N'Y A PAS DE BUG.** La save a été
  fournie après coup (`archipelPartie_1_3.txt`, export `ARCHv1:`) et chargée par le **VRAI chemin**
  (3 clés de slot). playTicks **408 875**, mode normal, `elevatorRepaired`, `elevatorLevel 10`.
  Mesuré sur **600 ticks** (instrumentation temporaire aux ancres H1/H2, **retirée**, 0 trace) :
  `elevatorTileOf(game,6)` = **{r:13,c:14}**, `elevatorSurfaceLinkedFor('road')` = **false**,
  `undergroundBlocked` = **true**, `hasPoolIO` = **true**, `inByType` =
  `{road:{cable_irradie:332,8 | 665,6}}`, `outByType` = `{road:{cable_supraconducteur:20,8 | 41,6}}`,
  les **3 Presses UHP — (9,8) (9,10) (11,10)** — `disc: true` / `discReason: 'elevator'` /
  `regime: undefined` / `active: false` à **CHAQUE** tick → `cable_supraconducteur` **delta = 0**
  (il DÉCROÎT même un peu : la surface en consomme). `elevatorFlow` `demand 0 / used 0`, 0 `tickError`.
  **Le stock de 550 291,9 est HISTORIQUE.** Contre-épreuve : avec une route posée, delta > 0.
  ⚠ **UNE AFFIRMATION DU BRIEF EST FAUSSE, mesurée sur sa save** : le §1 pose « il n'y a pas non plus
  de tuyau relié au port » et « (13,15) est un tuyau **isolé** d'une seule tuile ». En réalité
  **`elevatorSurfaceLinkedFor('pipe')` = TRUE** : le tuyau (13,13)…(15,13) est **PONTÉ** par le
  `separateur_air_v2` de **(16,13)** (un bâtiment à I/O tuyau fait pont — règle 10.59) et rejoint
  (17,13)…(20,13) puis le port (21,13) ; et (13,15) est lui-même ponté par le **Data Center** (14,15)
  et le **Refroidisseur** (12,15). Côté joueur : **ses LIQUIDES descendent déjà**, seuls les SOLIDES
  sont coupés. Le lot D/cas miroir du 14.50 n'est donc PAS neutre sur sa partie.
  ⚠ **L'ÉLÉVATEUR EST MURÉ** — le vrai problème du joueur, mesuré, **non corrigé** (hors périmètre §2) :
  ses 4 faces sont occupées ((12,14) et (14,14) **conduit**, (13,13) et (13,15) **tuyau**) ; la tuile
  élévateur (13,14) est LIBRE (une infra peut y être posée, terrain `elevator`). Son seul réseau route
  relié au port (nid 9 : (21,14) (20,14) (20,15) + 4 `jonction_route_cable` + (17,15) (15,15)…) culmine
  à **(15,15)**, et les 2 corridors qui l'en rapprochent coûtent cher — **les deux VÉRIFIÉS en moteur
  réel** (presses à `regime 1`, **+104 câble supra/s**, `elevatorFlow 1768/16384`) :
  (a) **colonne 16** (3 tuiles de câble sacrifiées) → **5 bâtiments de l'île 6 privés de courant**
  (refroidisseur, separateur_air_v2, mine_tungstene, antenne, four_arc_tungstene ; consommation
  **11,69 GW → 164 MW**) ; (b) **(14,15)** → c'est le **DATA CENTER** : production de l'île 6 à **ZÉRO**
  (il pontait le câble) **et** 2ᵉ émetteur du puzzle du Collisionneur perdu. ⚠ **Aucune jonction ne
  sauve la colonne 16** : la route et le câble y voudraient le **MÊME axe** (N-S), or une jonction
  n'unit que deux porteurs **perpendiculaires** (règle 13.18). À arbitrer par le joueur.
  (2) **LOT J — 241 sprites injectés** (bloc `__SPRITE_DATA__`, après l'ancre J2) : **144**
  `logic_porte_<op>_<sortie>_x<inhibée>_<état>` · **24** `logic_porte_not_<sortie>_i<entrée>_<état>` ·
  **16** `fil_logique_v1_<masque>_<lettres>_mort` · **9** `logic_jonction_NS_EO_<ns><eo>` · **48**
  `conduit_v4_<masque>_<lettres>_chauffe<1|2|3>`. **Aller-retour SHA-256 : 241/241 concordants**,
  0 doublon de clé, **tous 32×32**, et **241/241 DÉCODENT réellement en `Image`** (aucun PNG corrompu).
  **Validations enfin DYNAMIQUES** (espion `drawImage` + reverse-map sur TOUTES les clés) : porte à face
  inhibée → **`logic_porte_and_n_xs_0`** (le bouchon est bien sur la face S, opposée à la sortie N) ;
  réseaux morts → **`fil_logique_v1_01_N_mort`** et **`_02_E_mort`** ; jonction aux 2 axes morts →
  **`logic_jonction_NS_EO_mm`** ; conduit V4 chaud → **`conduit_v4_00_iso_chauffe3`** ; 0 exception
  console, aucune tuile logique sans dessin.
  ⚠ **ÉCART CONFIRMÉ CONTRE LE PACK RÉEL** : il livre **24** clés `logic_porte_not_*_i*_*` mais le
  moteur n'en adresse que **8** (`4 sorties × 2 états`), l'inhibition du NON étant **DÉRIVÉE**
  (décision §1-3 du brief 14.50) : son entrée est toujours la face opposée. Les 16 autres sont livrées
  et ne seront jamais demandées, sauf à rendre l'inhibition du NON réglable.
  ⚠ **PIÈGES DE HARNAIS** : (a) pour rejouer une save joueur, écrire **directement** les 3 clés de slot
  (`archipel_slot_<id>` / `archipel_slots` / `archipel_active`) — et **fermer en boucle** l'overlay de
  rattrapage (`.catchup-skip`), le récap hors-ligne et les astuces avant toute mesure ; (b) le décodeur
  d'export `ARCHv1:` (LZW 16 bits + base64) est désormais **COMMITÉ** à la racine
  (`decode_save.js`, `node decode_save.js <export.txt> <sortie.json>`) — le conteneur étant éphémère,
  la version « scratchpad » citée en 14.18 avait disparu ; indispensable pour lire une save hors
  navigateur ; (c) après un `git checkout -B` sur un `main` qui contient déjà
  ses propres commits, **re-jouer l'injection par script** plutôt que restaurer une copie : c'est
  déterministe et le SHA-256 le prouve.
  Validé : `node --check` (**7 blocs, 7 OK**) + Chromium **12 suites, 156 assertions, 0 KO, 0 erreur
  JS**, **2 passes identiques**. **Taille : 3 027 478 → 3 124 981 o (+97 503 o)**, quasi intégralement les
  241 PNG en data-URI.
  ⚠ **HORS PÉRIMÈTRE, non touché** : `rebuildNetworks`, la classification des porteurs,
  `undergroundBlocked`, `hasPoolIO`, `roadReachesPort`, `SAVE_VERSION`, et **la partie du joueur** (aucun
  correctif de terrain appliqué — le diagnostic est livré, l'arbitrage lui revient).
- **État précédent : `GAME_BUILD = 333`, `GAME_VERSION = 'Alpha 14.51'`, `SAVE_VERSION = 31`.**
  Changement 14.51 (brief `BRIEFMOTEUR2DIAGNOSTICELEVATEUR`, lots H/I/J) : **DIAGNOSTIC de la
  « fuite » de l'élévateur (aucun bug — branche B-D) + cache de tuile élévateur porté sur `game`.**
  `SAVE_VERSION` INCHANGÉ — aucun champ persisté touché (`game._elevTile` est transitoire, vérifié
  ABSENT de la sauvegarde). Base du brief EXACTE (3 025 253 o, MD5 `f1639846…` = la 14.50 livrée juste
  avant) : les **12 ancres sont sorties UNIQUES**.
  (1) **LOT H — CONCLUSION : branche B-D, IL N'Y A PAS DE BUG. Rien n'a été « corrigé ».** ⚠ **La save
  du joueur n'a PAS été fournie** avec ce brief : le protocole a été joué sur une **reproduction
  SYNTHÉTIQUE** de la géométrie du §1 (élévateur î6 (13,14), port (21,13), conduits en (12,14)/(14,14),
  tuyaux en (13,13)/(13,15) dont un réseau de 3 tuiles qui n'atteint jamais le port, **aucune route**
  près de l'élévateur, **aucune jonction**, 3 Presses UHP î7, `elevatorRepaired`, `elevatorLevel 10`,
  `port[6].cable_supraconducteur = 550 291,9`).
  **Mesuré sur 600 ticks, instrumentation temporaire aux ancres H1 et H2** (600 entrées H1,
  1800 entrées H2 = 3 presses × 600) : `elevatorTileOf(game,6)` = **{r:13,c:14}** (donc PAS B-A),
  `elevatorSurfaceLinkedFor('road')` = **false** (donc PAS B-B), `('pipe')` = false,
  `undergroundBlocked` = **true**, `hasPoolIO` = **true**, `inByType` =
  `{road:{cable_irradie:16}}`, `outByType` = `{road:{cable_supraconducteur:1}}`, les 3 presses
  `disc: true` / `discReason: 'elevator'` / `regime: undefined` / `active: false` à **chaque** tick
  (donc PAS B-C), `cable_supraconducteur` **550 291,9 → 550 291,9, delta = 0**, `elevatorFlow`
  `demand 0 / used 0`. **Le stock du joueur est HISTORIQUE** → le §5-11 du brief précédent est CLOS.
  ⚠ **CONTRE-ÉPREUVE INDISPENSABLE** (sans elle « delta 0 » ne prouve rien) : la MÊME mesure après
  pose d'une route port ↔ élévateur donne `linkRoad = true`, les presses passent de `elevator` à
  `power` et le delta devient **> 0** → le montage détecte bien une production quand elle existe.
  ⚠ **Le chemin est verrouillé PAR CONSTRUCTION** (lecture de code, en plus de la mesure) : la branche
  `if (!ok)` fait `continue` **AVANT** la boucle de production, et plus **aucune** ligne ne remet `ok`
  à `true` après l'ancre H3 — seuls des `ok = false` suivent.
  ⚠ **`regime` vaut `undefined`, pas 0**, sur un bâtiment déconnecté : il sort de la boucle avant
  l'écriture du régime (piège déjà documenté en 14.48 (f) — ne pas asserter sur `regime === 0`).
  (2) **LOT I — `_elevTileCache` n'est plus un objet de MODULE.** Il était écrit une fois par île et
  **JAMAIS invalidé** (ni au chargement d'une save, ni au changement de partie, ni au changement de
  mode), alors qu'`applyGameMode` reconstruit `ISLAND_TERRAINS` avec des grilles **différentes** selon
  `normal`/`difficile` → la tuile mémoïsée pour l'une pouvait être resservie à l'autre. Porté sur
  **`game._elevTile`** (initialisé paresseusement) → un nouvel objet de partie repart avec un cache
  vide, **aucun site d'invalidation à maintenir**. Mémoïsation **CONSERVÉE** (mesuré au Proxy :
  **862** lectures de grille au 1ᵉʳ appel, **0** sur les 50 suivants) — `elevatorTileOf` est appelée
  par tuile au rendu, la recalculer serait quadratique par image. Bug fermé **prouvé** : deux objets
  `game` de grilles différentes → tuiles **{r:13,c:14}** et **{r:5,c:5}**, sans contamination croisée.
  (3) ⚠ **LOT J NON LIVRABLE — le pack de 241 sprites n'a PAS été fourni** (ni dans le brief, ni en
  pièce jointe, ni dans les 6 zips du dépôt). Ce qui A été fait à la place : **contrôle de CARDINALITÉ
  des clés que le moteur DEMANDE**, qui doit correspondre au §7-7 → **porte 144/144 ✓**, **fil mort
  16/16 ✓**, **jonction 9/9 ✓**, **conduit V4 chaud 48/48 ✓**. Formats confirmés :
  `logic_porte_and_n_xs_0`, `fil_logique_v1_00_iso_mort`, `logic_jonction_NS_EO_mm`,
  `conduit_v4_00_iso_chauffe1`. ⚠ **ÉCART** : le brief annonce **24** clés `logic_porte_not_*_i*_*`,
  or seules **8** sont atteignables (`4 sorties × 2 états`) — l'inhibition du NON étant **DÉRIVÉE**
  (décision §1-3 du brief précédent), son entrée est toujours la face opposée. Les 16 autres seront
  livrées mais jamais demandées, sauf à rendre l'inhibition du NON réglable.
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) **`processHeat` remet `conduitLoad[isl]`
  à ZÉRO à chaque tick** → une valeur de charge forcée ne survit pas d'une frame à l'autre, il faut la
  ré-affirmer avant chaque redessin ; (b) le décodage des sprites est **PARESSEUX** → tant que l'art de
  test n'est pas décodé, `drawSprite` rend false et le draw tombe sur la clé de BASE : une boucle qui
  s'arrête au 1ᵉʳ sprite vu enregistre le REPLI et conclut à tort (flottement 4/6 → 8/8 après
  correction : attendre `spriteUsable`, puis ne sortir que sur la clé attendue) ; (c)
  `NET_MASK_SUFFIX[m]` contient **déjà** le numéro de masque (`'00_iso'`) — le préfixer une seconde
  fois fabrique une clé fantôme `conduit_v4_00_00_iso_*`.
  Validé : `node --check` (**7 blocs, 7 OK**) + Chromium **11 suites, 149 assertions, 0 KO, 0 erreur
  JS**, **2 passes identiques**. **Instrumentation retirée : 0 trace `__ELEVDIAG__`** (grep), fichier
  ramené au MD5 de base avant application du lot I. **Taille : 3 025 253 → 3 027 478 o (+2 225 o).**
  ⚠ **HORS PÉRIMÈTRE, non touché** (conforme au §2) : `rebuildNetworks`, la classification des
  porteurs, `hasPoolIO`, `undergroundBlocked`, `roadReachesPort`, `SAVE_VERSION`.
- **État précédent : `GAME_BUILD = 332`, `GAME_VERSION = 'Alpha 14.50'`, `SAVE_VERSION = 31`.**
  ⚠ Le mémo n'a pas été tenu pour la 14.49 (build 331, « plus aucun stock de port négatif ») : le bloc
  14.48 ci-dessous décrit l'état d'AVANT la 14.49.
  Changement 14.50 (brief `BRIEFMOTEURLOTGROUPE`, lots A→G) : **INHIBITION DES PORTES LOGIQUES
  (2 entrées max), RÉSEAUX LOGIQUES MORTS, transit du dernier fragment d'unité, élévateur (axe des
  jonctions + cas miroir), retour de l'acide sur les V1, capteur de SURCHAUFFE, retrait de la teinte
  canvas du conduit V4.** `SAVE_VERSION` INCHANGÉ — le seul champ ajouté (`gx` = `gateInhibit`) est
  **OPTIONNEL avec repli** sur un défaut DÉRIVÉ. Base du brief EXACTE (2 997 577 o, MD5
  `b27155df…`) : les **18 ancres sont sorties UNIQUES**, B6/B7 partageant leur première ligne
  (2 occurrences, comme annoncé).
  ⚠ **PRÉ-REQUIS NON LIVRÉ, à ne pas rechercher comme un défaut** : le pack de **241 sprites** du lot
  `BRIEF-SPRITES-LOGIQUE-INHIBITION` **n'est PAS dans le dépôt** (vérifié : 0 clé `_mort`, 0 clé
  `logic_porte_*_x*`, 0 `conduit_v4_*_chauffe*`, et aucun des 6 zips ne les contient). Le §0 du brief
  couvre ce cas : les chaînes de candidats placent la clé NOUVELLE en tête et gardent les clés
  HÉRITÉES en queue → **aucune casse**, mais l'inhibition, les réseaux morts et l'art `_chauffe` du V4
  restent **INVISIBLES** jusqu'à la livraison du pack. Vérifié par **injection d'art de test** +
  espion `drawImage` : les bonnes clés sont bien DEMANDÉES.
  (1) **LOT A — les portes passent à 2 ENTRÉES MAX.** Une face non-sortie est INHIBÉE (défaut :
  l'OPPOSÉE à la sortie, `gateDir ^ 1` — `DIRS4 = [N, S, O, E]` donc 0↔1 et 2↔3 sont bien les paires
  opposées) → une porte à 2 entrées garde ses deux LATÉRALES. ⚠ **La porte NON est la CONVENTION
  INVERSE, assumée** : UNE entrée sur la face opposée (traversée), ses deux latérales inhibées ; son
  inhibition est **DÉRIVÉE, non réglable** (un réglage y est ignoré, vérifié). L'exclusion se fait à la
  **construction de `gateIO`**, JAMAIS à l'évaluation : `inNets` est lu par l'UI, il ne doit pas
  compter une face morte. Nouveaux helpers `gateInhibitDefault` / `gateInhibitedDirs` / `gateLiveDirs`
  + miroirs transitoires `gateInhCur` / `gateLive` / `gateWiredInh` (jamais persistés).
  ⚠ **ROTATION** : `gateInhibitedDirs` filtre une inhibition égale à la face de sortie → elle
  **re-défaute** toute seule ; `setLogicConfig` **efface** en plus le réglage mort pour que la save ne
  le conserve pas. Balayage 7 ops × 4 dir × 4 inh = **112 cas, 0 état où sortie == inhibée**.
  ⚠ **LE POINT LE PLUS IMPORTANT À REMONTER — un ET/OU à 3 faces câblées N'EXISTE PLUS.** Un
  comparateur 3 bits doit **CHAÎNER deux portes** (mesuré en moteur réel : `AND(AND(x,y),z)` = **8/8
  conforme**, chaque porte à 2 entrées vives). Les montages existants qui s'appuyaient sur une 3ᵉ
  entrée **changent de résultat** → toast de migration au chargement (`gateInhibitWarn`, consommé UNE
  fois par chargement dans la boucle `frame`, après qu'un tick a rempli `gateWiredInh`) + ligne
  d'avertissement dans la fiche. **JAMAIS dans l'art de la tuile** (décision du brief).
  ⚠ **CHOIX NON PRÉVU PAR LE BRIEF, assumé** : la face inhibée est **choisissable** dans la fiche
  (3 boutons, l'opposée par défaut). Le brief pose `gateInhibit` comme un champ persisté optionnel
  sans dire qui l'écrit ; sans réglage, un joueur dont les 2 fils arrivent sur la face opposée + une
  latérale serait coincé. Le NON reste dérivé.
  (2) **LOT B — « MORT » est STRUCTUREL, pas dynamique** : un réseau logique est mort si **aucun
  pilote ne le vise** (aucune face de capteur `sensorDir`, aucune face d'émetteur, aucune sortie de
  porte). ⚠ **Une porte qui sort 0 rend son réseau VIVANT** — sans cette définition tout réseau à 0
  s'afficherait mort et l'indication **clignoterait** entre deux ticks. Calculé dans `processLogic`
  **AVANT** la propagation itérative (la vivacité ne dépend que du câblage) → `game.logicDead[isl]`.
  Suffixe de sprite `_mort` / `_on` / `''` aux **2** sites de dessin du fil (B6 = legacy `t.building`,
  B7 = surcouche `t.logic`, le vrai chemin depuis 13.96) ; jonction → `logic_jonction_NS_EO_<ns><eo>`
  avec chaque caractère ∈ `m 0 1`, replis sur les 4 clés héritées. **Stabilité mesurée : 3 ticks
  identiques, 0 clignotement.**
  (3) **LOT C — le `Math.floor` d'`exportable` est SUPPRIMÉ** : il rendait le dernier fragment d'unité
  **définitivement inexpédiable** (0,4 → 0 → rien ne partait JAMAIS). Mesuré : `rawShippable` = **0,4**
  là où l'ancien calcul rendait **0** (contre-épreuve incluse). Le transit déplace des flottants depuis
  10.48, aucun appelant aval ne suppose un entier. `askPortFor` prend une **marge** :
  `ceil(coût × 1,05) + 1` → **1051 pour un coût de 1000** (avant : 1000 pile, donc bloqué « à très peu
  près »).
  (4) **LOT D — élévateur, 2 trous fermés.** (a) `elevatorSurfaceLinkedFor` respecte enfin l'**AXE des
  jonctions** (`junctionDirOk`, comme `adjacentNetworks`) : il lisait `Object.values(nt.netIds)`, donc
  une jonction dont seul l'axe **PERPENDICULAIRE** rejoint le port validait le lien. Le paramètre
  `dr/dc` = pas de l'élévateur vers le voisin ; **seul l'axe compte** (`junctionDirOk` teste `!== 0`),
  pas le sens. Le cas « infra POSÉE sur la tuile élévateur » n'a pas d'axe d'approche → **inchangé**
  (vérifié). (b) **CAS MIROIR FERMÉ** : les SOLIDES exigent une **ROUTE** port ↔ élévateur, en miroir
  du TUYAU exigé depuis 14.26. ⚠ **Ceci RENVERSE une décision explicite de 14.26** — le commentaire
  est **réécrit sur place, pas supprimé**, pour dater le revirement. ⚠ **Rupture assumée** : un
  souterrain relié par un **TUYAU SEUL** s'arrête côté solides (motif `elevator`) jusqu'à la pose d'une
  route ; mesuré en moteur réel sur une Presse UHP (`elevator` → puis `wire` une fois la route posée).
  ⚠ **§5-11 NON EXÉCUTABLE : aucune save joueur n'a été fournie avec ce brief.** Les DEUX conditions
  sont reproduites **synthétiquement** et corrigées (axe de jonction ET cas miroir) ; laquelle
  débloquait réellement le souterrain du joueur reste **indéterminée** faute de sa partie.
  (5) **LOT E — l'acide REVIENT sur les V1 seulement** : `fonderie_or` **4** (calé sur
  `broyeur_uranium`), `raffineur_silicium` **8** (calé sur `extracteur_souterrain`). Les V2 restent
  SANS acide → elles redeviennent de vraies améliorations de **RECETTE**. Les **4 commentaires 14.19**
  sont réécrits (ils disaient « V1 comme V2 », devenu faux). ⚠ **EFFET DE BORD, l'inverse de 14.19** :
  la fonderie d'or retrouve un liquide → elle **refait PONT** entre deux tronçons de tuyau (règle
  10.59, `buildingConnectsCarrier` vérifié à `true`) ; sa V2, elle, ne fait toujours pas pont.
  (6) **LOT F — nouveau mode de capteur `surchauffe`** : bâtiment `heatCap` → `heat / heatCapOf(bld)` ;
  réseau **conduit** → **chaleur des SOURCES**. ⚠ **Il ne lit PAS `conduitLoad`** : celui-ci vaut le %
  de flux utilisé sur V1-V3 et la **pire source** sur V4 illimité — un mode qui change de SENS selon
  le niveau du réseau serait inutilisable (la saturation de flux a déjà son mode, `sature`). Mesuré :
  `conduitLoad` forcé à 0,99 avec des sources à 0,1 → **signal 0**. Le calcul `worst` est **EXTRAIT**
  en `conduitSourceHeatFrac(sources)` (2 consommateurs : teinte du V4 + capteur) et publié pour TOUS
  les niveaux dans `game.conduitSrcHeat[isl][nid]`. Seuil **0,8** par défaut (= palier `_chauffe3`),
  réglable via `sensorSeuil` (champ déjà persisté, **clampé à 1** — un reste de mode `seuil` à 1e7
  devient « 100 % », jamais un faux positif). Le mode **`seuil` est RETIRÉ du conduit** (aucun stock à
  y lire) et **conservé sur route/tuyau**. ⚠ `surchauffe` reste **NON-DÉFAUT** (conduit → `sature`,
  bâtiment → `elec`) → **aucun capteur existant ne change de comportement**.
  (7) **LOT G — la teinte canvas du conduit V4 est RETIRÉE** (2 sites : tuile de conduit + stub sous
  un bâtiment à chaleur) : le V4 rejoint les V1-V3 sur l'art `_chauffe1/2/3`. **0 appel
  `drawSpriteTinted` restant** (la fonction reste définie, primitive générique) ; `conduitUnlTint`
  reste utilisé par le repli VECTORIEL, il n'est donc pas orphelin.
  Validé : `node --check` (**7 blocs, 7 OK**) + Chromium **9 suites, 132 assertions, 0 KO, 0 erreur
  JS**, suites **rejouées 2 fois intégralement sans flottement**, en viewport 420 px / DPR 3.
  **§5-6 puzzle P3 : 48/48 verdicts conformes** (8 codes Collisionneur × 6 codes Data Center — il
  n'émet jamais de lepton ; leptons `000`/`111`), **0 pénalité imméritée** ; + XNOR natif à 2 entrées
  **4/4** et ET chaîné **8/8** en moteur réel → un comparateur correct reste constructible.
  **§5-16 aller-retour SHA-256 : 28/28 blocs retrouvés VERBATIM** (`count == 1`, sauf la ligne
  partagée B6/B7 à 2 par construction) + **0 anomalie d'échappement**.
  **UI RÉELLE** (vrais clics souris, tap canvas réel) : bouton de couche logique → fiche de porte
  affichant « Entrées 2 », la face inhibée, l'**avertissement ⚠** sur la face câblée-mais-inhibée et
  la ligne « Signal reçu » ; **clic réel** sur le bouton « O » → `gateInhibit` passe à `[2]`.
  **Round-trip de sauvegarde RÉEL** : SAVE **31**, `gx: [2]` sérialisé, rechargement → réglage
  conservé, **toast de migration affiché UNE seule fois** ; puis save **privée de `gx`** (simule
  l'avant-lot) → la porte prend le **DÉFAUT**, 0 perte, horloge qui avance.
  ⚠ **ÉCART AU BRIEF (anchor B5)** : le brief place la nouvelle clé de sprite de porte au seul site
  B5, qui est le chemin **LEGACY** (`t.building`). Depuis 13.96 la logique vit dans `t.logic` et c'est
  la **passe de surcouche** qui dessine réellement les portes → la clé y a été ajoutée AUSSI, sinon
  l'inhibition n'aurait été visible nulle part. Les deux sites portent la même chaîne de candidats.
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) pour mesurer QUELLE clé de sprite est
  demandée quand le pack manque, **injecter un art de test unique** (`canvas.toDataURL` 1×1 par clé)
  + espion `drawImage` avec reverse-map `dataURL → clé` — et **reverse-mapper AUSSI les clés DÉJÀ
  présentes**, sinon les `_on` du pack v2.2 ne sont jamais vus (m'a donné un faux KO) ; (b) le draw ne
  parcourt que les tuiles **VISIBLES** → forger dans un carré libre autour du **centre de l'écran**
  (`cCen = (largeur/2 − (baseX − camX)) / tile − 0,5`), pas n'importe où sur l'île ; (c) les ids de
  jonction sont **`jonction_route_cable` / `_route_tuyau` / `_cable_tuyau`** (pas `junction_*`) et
  `junctionDirOk` prend la **def du BÂTIMENT** jonction, pas la def d'île ; (d) pour piloter un bit
  logique à 0/1 sans émetteur, poser un capteur `elec` sur un **support forgé** et jouer sur
  `pwrAvg` (0,5 → 1 ; 1 → 0) ; (e) `askPortFor` et `setLogicConfig` vivent dans le scope d'**App**
  (hors de `window.__heat`, posé au niveau module) → exposés via `window.__ui()`.
  ⚠ **Taille : 2 997 577 → 3 024 511 o (+26 934 o).** Le CODE seul pesait **+22 656 o** (mesuré avant
  le bump) ; le reste est le commentaire de version et `GAME_NOTES`. Volume dominé par les
  commentaires (⚠ conventions du projet), le code effectif étant d'environ 200 lignes.
  ⚠ **HORS PÉRIMÈTRE, non touché** : les 241 sprites (non livrés), `colliderCompare`, `heatCapOf`,
  `conduitDebit`, `roadReachesPort`, `TRADE_LIQUIDS`, `PORT_PIPE_RES`, `SAVE_VERSION`, la définition de
  `drawSpriteTinted` (0 appelant), et **aucune traduction** des ~10 nouveaux libellés (repli fr
  hors-fr, comme les astuces depuis 13.32).
- **État précédent : `GAME_BUILD = 330`, `GAME_VERSION = 'Alpha 14.48'`, `SAVE_VERSION = 31`.**
  Changement 14.48 (brief `BRIEFB7echappementcollisionneur`, chantiers P1→P5) : **ÉCHAPPEMENT
  SOUTERRAIN, raccord visuel du COLLISIONNEUR, Centrale à Gaz 2 MW, Séparateur d'Air interdit île 7,
  et les `forbiddenIslands` passent de MASQUÉS à GRISÉS.** `SAVE_VERSION` INCHANGÉ — aucun champ de
  sauvegarde créé/modifié/supprimé ; `gaz_echappement` ne persiste JAMAIS (purgée du port à chaque tick).
  Base du brief EXACTE (2 983 827 o, SHA-256 `03e63676…`) : les **13 ancres sont sorties UNIQUES**, sans
  exception, et le **delta des 13 blocs vaut +4 921 o AU BYTE PRÈS** (mesuré avant le bump, la valeur du
  brief tombe juste). Les **13 blocs ont été extraits PAR SCRIPT du brief** (pas retapés) → les 13 SHA-256
  et longueurs du §7 sont retrouvés à l'identique, 0 anomalie.
  (1) **P1 — le Collisionneur est un TERRAIN, pas un bâtiment** : `netConnectMask` ne lit que
  `nt.building`, donc la tuile tuyau/câble voisine ne posait JAMAIS son bit vers le landmark — elle
  dessinait un cul-de-sac ET s'amincissait de ce côté (le « trou » signalé). Nouveau `colliderEdgeMask`,
  calqué sur `elevatorEdgeMask` (la cage est aussi un terrain). **TUYAU et CÂBLE seulement.** ⚠ **PUREMENT
  VISUEL** : `colliderDrawHe3`/`colliderWireNid` passaient déjà par `adjacentNetworksFootprint`.
  Mesuré à l'espion `drawImage` : tuyau au sud du bloc → `tuyau_v1_01_N`, câble au nord → `cable_v1_04_S`
  (branche bien VERS la machine), tandis que **route → `route_v1_00_iso` et conduit → `conduit_v1_00_iso`**
  (aucune branche — la non-régression exigée). Bande de tuyau sur les 3 tuiles de largeur → `06_ES`,
  `14_ESO`, `12_SO` : chacune porte sa branche S ET le tracé reste continu. Les 4 paliers suivent
  (`tuyau_v1/v2/v3/v4`). ⚠ **Le stub dessiné PAR la tuile collider (14.39) est BYTE-IDENTIQUE** entre la
  base 329 et le build patché (contre-épreuve exécutée sur les deux fichiers).
  (2) **P2 — Centrale à Gaz 512 → 2048 kW** (à 512 elle était strictement dominée : même sortie qu'une
  Diesel V1 t2 et qu'une Géothermie t5 SANS intrant). ⚠ **Ce n'est PAS un V2** : aucune entrée
  `TIER_NEXT`/`TIER_STEP` (dans ce jeu un V2 allège la RECETTE, cf. `centrale_diesel_v2`). Nouveau champ
  de def **`vent`**. Mesuré : 2048 kW au Nv.1, 4096 au Nv.2, intrants 8 méthane + 64 oxygène puis ×2.
  (3) **P3 — `gaz_echappement`, ressource FANTÔME** : dans `RES_SHORT` + `CARRIER_BY_RES` (pipe) mais dans
  **AUCUN `outputs` statique** — injectée dans `effOutputs` AU TICK et seulement si `isl === 7`. Vérifié en
  jeu : `PRODUCER_OF.gaz_echappement === null`, absente de `RES_TIER`, de `unlockedResourceSet` (inventaire
  HUD), de `TRADE_RESOURCES` (onglet Port + carte archipel). ⚠ **Copie DÉFENSIVE obligatoire**
  (`Object.assign({}, …)`) : `b.outputs` est l'objet de def PARTAGÉ — le muter contaminerait les centrales
  de surface. ⚠ **Elle passe par `pipePort`, PAS par `NON_STORABLE`** : `NON_STORABLE` reste confiné au pool
  (`pipeToPort` le saute) et ne touche JAMAIS l'élévateur, or c'est le transit qu'on veut facturer. Elle
  entre donc dans `outDem` (catégorie « sortants »), `elevOutFac` bride le régime, puis le nouveau
  `VENTED_RES` la PURGE du port en fin de tick (**après** le vidage des citernes, sinon le pool la
  réinjecterait). Mesuré en moteur réel : `netFlow` = `prod {gaz_echappement: 64}` / `cons {oxygene: 64,
  methane: 8}`, `elevatorFlow.out = 64`, et **0 clé `gaz_echappement` au port après 300 ticks**.
  ⚠ **DEUX ÉCARTS MESURÉS au modèle §5 du brief, même cause racine — LE MÉTHANE TRANSITE AUSSI.** Le brief
  pose « le méthane ne transite pas (Séparateur Cryogénique déjà `exclusiveIsland: 7`) ». **FAUX en
  pratique** : dès que le réseau tuyau souterrain est `connected` (il touche la tuile élévateur), `pipeToPort`
  bascule vers le port **TOUT** l'I/O tuyau stockable — méthane compris. Mesuré `elevatorFlow` : `in = 72`
  (64 O₂ + 8 méthane), pas 64. Corollaire non listé par le brief : un Séparateur Cryogénique local
  **DÉPOSERAIT lui aussi son méthane AU PORT** (donc en haut) avant que la centrale ne le redescende — un
  aller-retour dans la cage. Conséquences : (a) la charge totale est **136/s** et non 128/s ; (b) surtout,
  **le régime au Nv.0 n'est PAS ≈12,5 %** — voir ci-dessous.
  ⚠ **LE POINT LE PLUS IMPORTANT À REMONTER — en mode `priority` (LE DÉFAUT), la centrale souterraine est
  en TOUT-OU-RIEN, pas en rampe.** L'ordre strict `construction → sortants → intrants` fait que la première
  catégorie prend tout le débit et affame la seconde ; or `regime = min(elevInFac, elevOutFac)` → **0**.
  Mesuré, cage Nv.0 (16/s) : `out=16, in=0` → **régime 0, motif `elevbusy`** (le brief annonçait ≈12,5 %).
  Balayage complet : **Nv.0/1/2 → régime 0** ; **Nv.3 → 88,9 %** ; **Nv.4 → 100 %**. En `fair` → 11,1 %,
  en `proportional` → 11,8 % (là, la rampe progressive existe). Inverser l'ordre (`intrants` avant
  `sortants`) donne aussi 0 : c'est la STRICTE priorité qui est en cause, pas le sens. **Rien n'a été
  « corrigé »** (le brief l'interdit explicitement) — mais le joueur qui reste en mode par défaut verra sa
  centrale à 0 % jusqu'au Nv.3 de cage, sans palier intermédiaire. À arbitrer au playtest.
  (4) **P4 — `separateur_air` / `_v2` : `forbiddenIslands: [7]`.** ⚠ **GRANDFATHERING VÉRIFIÉ** : un
  séparateur DÉJÀ posé île 7 tourne toujours (mesuré régime 1, 512 O₂/s + 1024 N₂/s) et survit à un
  rechargement ; `forbiddenIslands` n'est lu que par `canPlace`/`tryPlace`. L'outil **Copier** le refuse
  bien (toast RÉEL « ❌ Séparateur Air V1 : non constructible sur cette île »). ⚠ **Piège de test** : viser
  une tuile OCCUPÉE fait sortir `tryPlace` sur `t.building || t.occupied` **AVANT** le garde d'île → aucun
  toast, faux négatif. Viser une tuile réellement libre.
  (5) **P5 — `offIslandOn` : les `forbiddenIslands` passent de MASQUÉS à GRISÉS.** `visibleOn` INCHANGÉE
  (ils restent non posables) ; le NON DÉBLOQUÉ reste MASQUÉ (vérifié : centrale nucléaire + séparateur
  interdits île 7 ET non débloqués → invisibles). ⚠ **Lire les compteurs du brief comme « grisés SANS
  tooltip »** : l'île 6 affiche **26 grisés au total = 20 préexistants (exclusivité, avec tooltip « Se
  construit sur Île N ») + exactement 6 nouveaux à `title: null`** (éolienne, éolienne offshore, charbon
  V1/V2, diesel V1/V2) ; l'île 7 en donne **11** (les 6 + tour aéroréfrigérante, nucléaire V1/V2,
  séparateur V1/V2). Le tap sur un grisé ouvre la FICHE sans armer l'outil ; l'interrupteur OFF les fait
  tous disparaître et persiste `showOffIsland`. ⚠ **Le libellé « Bâtiments des autres îles » devient
  partiellement inexact** (il couvre maintenant « interdit ici ») — SIGNALÉ, volontairement non renommé.
  Validé : `node --check` (**7 blocs, 7 OK**) + contrôle syntaxique ciblé de `colliderEdgeMask` + Chromium
  **11 suites, ~75 assertions**, suites **rejouées 2 fois sans flottement**, en viewport 420 px / DPR 3.
  **Contrôle d'intégrité §7 : 13/13 blocs `occ=1`, SHA-256 et longueurs conformes, 0 anomalie.**
  **Non-régressions par contre-épreuve BASE 329 vs PATCHÉ 330** : stub collider (14.39) byte-identique ;
  **menu du tutoriel + verrouillage des onglets IDENTIQUES sur les 8 étapes** (test 26 rejoué sur les deux
  fichiers). **Rechargement RÉEL d'une save créée sur le build 329** → SAVE 31, centrale gaz Nv.3 et
  séparateur île 7 intacts, stocks intacts, 0 `tickErrors`, horloge qui avance, 0 erreur JS.
  ⚠ **Test 15 TRANCHÉ — aucun sprite `item_gaz_echappement` n'est nécessaire** : `fmtFlow` du `NetworkPanel`
  rend du **TEXTE seul** (`fmtRateSci(v) + ' ' + matLabel(k)`), il n'y a aucun `<img>` de ressource dans ces
  lignes → un carré de repli est structurellement impossible. Panneau réel mesuré : « PRODUCTION /S 64 gaz
  échap. » et « CONSOMMATION /S 64 oxygène · 8 méthane », 0 image cassée.
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) **une `.tip-popup` (astuce de bienvenue)
  RECOUVRE le bouton « Passer » du tuto au boot** → `elementFromPoint` renvoie `tip-popup` et le tuto n'est
  JAMAIS passé (le menu reste filtré à 5 bâtiments, les onglets restent `tab-locked`) : fermer les astuces
  par `.tip-ok` AVANT, et en boucle, avec de VRAIS clics souris ; (b) la **bannière du GUIDE partage la
  classe `.tuto-banner`** → tester le CONTENU (`/Tuto \d+\/\d+/`), pas la présence ; (c) `page.reload()`
  **RECHARGE LA SAUVEGARDE AUTO** → un 2ᵉ scénario repart sur l'état du 1ᵉʳ : prendre un `boot()` NEUF par
  scénario ; (d) `switchIsland`, `pointerToTile` et `centerCam` **ne sont PAS globaux** (portée React) →
  piloter par de vrais clics sur `.island-tab` et recalculer la tuile via `px = rect.left + (baseX − camX)
  + (c + 0,5) × tile` ; (e) sans passer les réseaux en `unlimited`, le **plafond V1 du tuyau (64/s)** bride
  8 méthane + 64 oxygène = 72 → on mesure 7,11 et 56,89 (×8/9) au lieu de 8 et 64 ; (f) un bâtiment
  **déconnecté ne voit jamais son `regime` écrit** (il sort de la boucle avant) → il reste `undefined`, pas
  `0` : asserter sur `active`/`discReason` ; (g) forcer une sauvegarde exige d'armer `g.saveTimer` **ET** de
  redéfinir `document.visibilityState` sur `'hidden'` (`flushSave` est une closure).
  ⚠ **BUG PRÉEXISTANT RECONFIRMÉ, non corrigé (hors périmètre)** : outil **Copier** en main + survol →
  `drawHover` appelle `canPlace(r, c, '__copy')`, `BUILDINGS['__copy']` est `undefined` → `Archipel frame
  error: Cannot read properties of undefined (reading 'kind')` à chaque frame (avalé par le `try/catch` de
  14.13). Déjà documenté en 14.46, **ce n'est PAS une régression de ce lot**.
  ⚠ **Taille : 2 983 827 → 2 993 472 o (+9 645 o).** Les 13 blocs du brief seuls pèsent **+4 921 o**
  (exactement l'attendu) ; le reste est le commentaire de version et `GAME_NOTES`.
  ⚠ **HORS PÉRIMÈTRE, non touché** : le stub 14.39 de la tuile collider, le `title:` de la vignette (déjà
  gardé par `exclusiveIsland != null` → un bâtiment seulement interdit n'affiche AUCUN motif, c'est voulu),
  le libellé de l'interrupteur, `centrale_gaz_v2`, `TRADE_LIQUIDS`, `PORT_PIPE_RES`, `elevatorAllocate`,
  `SAVE_VERSION`, et **aucune traduction** du libellé « gaz échap. » (repli fr hors-fr).
- **État précédent : `GAME_BUILD = 329`, `GAME_VERSION = 'Alpha 14.47'`, `SAVE_VERSION = 31`.**
  Changement 14.47 (brief `BRIEFUIELECRECETTE`, éditions D1→D7) : **vignettes allégées, « Demander au
  port » gaté par l'armement, DIMENSIONNEMENT ÉLECTRIQUE IDÉAL, et retour du processeur dans la MOT2.**
  `SAVE_VERSION` INCHANGÉ — aucun champ de sauvegarde touché, aucune prop de composant ni état React
  nouveau, **aucune règle CSS ajoutée** (les 3 nouvelles lignes réutilisent `.ep-stat` et `.ip-row`).
  Les **11 ancres du brief sont sorties UNIQUES sur la base 14.46/328**, sans exception.
  (1) **D1+D2 — la ligne `tb-io` des vignettes est SUPPRIMÉE** (pas masquée par option) : elle avait
  été posée en 14.42 sur chaque vignette du menu Bâtiment/Réseau et la surchargeait. Le CSS
  (`.tb-io`/`.io-in`/`.io-out`/`.io-pw`) part avec le code, sinon il resterait du CSS mort. La
  pastille de coût et le libellé de repli sont **conservés**. ⚠ **Rien n'est perdu, seulement
  déplacé** : l'information reste dans la **fiche détaillée** (appui long 450 ms), vérifiée
  byte-identique — `BuildingDetailModal` n'est pas touché.
  (2) **D3 — « Demander au port » de la FICHE bâtiment exige désormais l'ARMEMENT** (`armed &&
  askNeeded`) : il n'apparaît qu'après un 1ᵉʳ clic sur « Monter »/« Densifier », au lieu d'être
  affiché en permanence. `armed` est l'état DÉJÀ partagé par Monter et Densifier, et il est remis à
  `false` par le `useEffect([info])` → fermer puis re-toucher le bâtiment remasque le bouton (voulu).
  ⚠ **Le `UpgradePanel` (outil ⬆) est laissé TEL QUEL** (décision du brief, vérifiée par test) : il
  n'a **aucun armement** — son bouton agit au 1ᵉʳ clic et est déjà grisé quand on ne peut pas payer.
  Y ajouter un armement imposerait 2 clics pour améliorer, ce qui n'est pas demandé.
  (3) **D4/D5/D6 — nouveau helper `idealGridSizing(demMin, demMax)`** + 3 lignes **« Production
  idéale / Stockage idéal / Ratio idéal »** dans le panneau **⚡ Énergie** (par ÎLE) ET dans la fiche
  d'un réseau **CÂBLE** (par COMPOSANTE). Modèle : toutes les sigmoïdes ont `period: 60` et 1 tick =
  1 s, donc 1 unité de stockage = 1 kW pendant 1 tick ; l'accumulateur absorbe la **BOSSE** au-dessus
  de la moyenne (`S·60/2π ≈ 9,55·S`), ce qui permet de dimensionner la production sur la **MOYENNE et
  non sur le PIC**, majorée de la perte de charge des accumulateurs **V1** (rendement 0,8 → +0,25·E).
  ⚠ **Cas V1 retenu VOLONTAIREMENT** (conservateur) : détecter des Accumulateurs V2 (`chargeLossless`)
  imposerait de propager leur type jusqu'au panneau — hors périmètre ; un joueur en V2 a ~6 % de marge
  en plus, ce qui est le bon sens de l'erreur. ⚠ **Le doublon île/composante est ASSUMÉ** : les
  accumulateurs servent une *composante câble*, pas une île — un ratio île-global mentirait dès qu'il
  y a deux grilles séparées (**vérifié en jeu** : 306 kW/4,28 MWh sur une grille, 612 kW/8,56 MWh sur
  l'autre, chacune cohérente avec SA plage). Affichage gaté par `showSpread`/`showWireRange` : sans
  oscillation un accumulateur ne sert à rien et les 3 lignes vaudraient 0.
  ⚠ **`statRow` gagne un 4ᵉ paramètre `title`** : ses **8 autres sites d'appel passent 2 ou 3
  arguments**, `title` y vaut `undefined` et React n'émet pas l'attribut → aucun site à modifier.
  (4) **D7 — `usine_moteur_nuc_v2` : `ordinateur_quantique 0,001` → `processeur 1`.** ⚠ **CONSÉQUENCE
  ASSUMÉE ET VOULUE** : au Nv.11 la V2 consomme **1 024 processeurs/s, exactement comme la V1** au même
  niveau → l'allègement de recette de la MOT2 ne porte plus que sur les **DEUX** postes « tonnage »
  (pièce méca → pièce de précision, polymère → câble supra). L'ordinateur quantique **sort entièrement
  de la recette** mais reste exigé au **FORFAIT de densification** (`TIER_STEP`, non modifié).
  ⚠ Le paragraphe 14.46 ci-dessous est donc **périmé sur ce point** (il cite « ordi quantique » comme
  3ᵉ substitution) — le commentaire de recette a été corrigé sur place.
  Validé : `node --check` (**7 blocs, 7 OK**) + Chromium **4 suites, 94 assertions, 0 KO, 0 erreur JS**,
  suites rejouées **2 fois intégralement sans flottement**, en viewport 420 px / DPR 3.
  **Contrôles du brief** : 5.1 **7/7 orphelins à count 0** (`tb-io`, `ioSegs`, `ioLine`, `io-in`,
  `io-out`, `io-pw`, `ordinateur_quantique: 0.001`) ; 5.2 **9/9 blocs conformes au SHA-256** ;
  5.3 **delta mesuré +2 816 o EXACTEMENT** (avant bump, `os.path.getsize` — la valeur du brief tombe
  juste) ; 5.4 **7/7 `node --check`**.
  ⚠ **DEUX ERREURS DU BRIEF au §5.2, à ne pas rechercher comme des défauts** : (a) il affirme que les
  blocs hashés « ne contiennent pas le marqueur `<VER>` » — **FAUX pour le bloc D2**, dont le hash a
  été calculé **avec le `<VER>` littéral** (qui fait 5 caractères, comme `14.47`, d'où la longueur
  exacte de 280 o) : hasher le bloc après substitution donne forcément un autre hash ; (b) le bloc D5a
  (189 o) s'arrête à `  },` **sans espace final** — le couper une virgule plus loin donne 190 o et un
  hash différent. Les deux « écarts » constatés au premier passage venaient de là, pas du code.
  **UI RÉELLE** (vrai navigateur, vrais clics souris) : menu Bâtiment et menu Réseau **sans aucune
  `.tb-io`**, pastilles de coût toujours là (dont 3 en rouge faute de stock), hauteurs de vignettes
  cohérentes ; **appui long réel** → la fiche détaillée liste toujours ENTRÉES/SORTIES/RÉSEAUX ;
  **fiche bâtiment ouverte par tap canvas réel** → bouton absent à l'ouverture, **apparaît** après le
  1ᵉʳ clic « Monter » (qui passe à « Confirmer »), **disparaît** après « Demander au port », **remasqué**
  après fermeture/réouverture, et **absent même armé** quand le stock suffit.
  **Cas de référence du brief (T11) retrouvé AU CARACTÈRE PRÈS** sur une Usine Moteur Nucléaire V1
  Nv.1 seule (sigmoïde 64 → 512 kW) : « Consommation min → max **64 kW → 512 kW** », « Production
  idéale **306 kW** », « Stockage idéal **4,28 MWh** », « Ratio idéal **14 kWh / kW** » — production
  idéale en **rouge** (8 kW produits < 306), stockage idéal en **orange**, puis **au vert** dès qu'un
  Accumulateur V1 (8 192 kWh) est branché, avec « · actuel N » qui apparaît. Les 3 infobulles
  s'affichent avec **accents et symboles corrects** (`≈`, `×`, `÷`, `→`, `·`), **aucun `\xNN` visible**.
  **Moteur RÉEL pour la MOT2** : 1 processeur/s, **0 ordinateur quantique**, 0,1 plutonium/s →
  0,1 `element_moteur_nuc`/s (ratio processeur/sortie = **10**), `heatEmit > 0` (la branche
  proportionnelle 14.46 fonctionne toujours), 0 `tickErrors`. **Round-trip de sauvegarde** : SAVE **31**,
  MOT2 sérialisée, rechargement réel → bâtiment intact, 0 `tickErrors`, horloge qui avance.
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) **la `ModeModal` (« Choisis ton mode »)
  est ENCORE ouverte au boot** et recouvre tout → `elementFromPoint` renvoie `.opt-toggle`/
  `.mode-fast-desc` et **tout hit-testing échoue** ; cliquer la VRAIE carte « Normal » (13.35), un
  `.click()` DOM sur le tuto ne suffit pas ; (b) **`BuildingDetailModal` se rend dans `.slot-panel`**,
  PAS `.research-panel` — chercher la mauvaise classe fait conclure à tort que l'appui long ne marche
  pas ; (c) **ne JAMAIS purger un overlay avec `.remove()`** : ça casse l'arbre React et le `<canvas>`
  disparaît ; fermer par un **vrai clic souris** (un `.click()` DOM est avalé par `useGhostGuard`, le
  panneau reste ouvert et son backdrop bloque ensuite le canvas) ; (d) **`PORTS` et `BUILDINGS` sont
  des `const` de MODULE, pas des propriétés de `window`** → dans `page.evaluate` il faut les nommer
  nus (`PORTS[isl]`), `window.PORTS` rend `undefined` ; (e) **`g.islands[isl]` EST le tableau 2D**
  (pas `.tiles`), et **`cam.x`/`cam.baseX` sont en PIXELS** : l'inverse exact de `pointerToTile` est
  `px = (baseX − camX) + (c + 0,5) × tile` ; (f) **un bâtiment forgé sans desserte route→port n'a pas
  ses intrants** → il sort d'`energyConsumers` et `demandMin/Max` restent à **0** : pour mesurer une
  demande il faut une nappe de route TOUCHANT le port ; (g) **débloquer tout l'arbre de recherche
  déclenche une FILE d'astuces** dont le `.research-backdrop` intercepte les clics du HUD — couper
  `g.ui.tipsEnabled` AVANT, ou ne pas débloquer (poser les bâtiments dans la grille suffit, le tick ne
  vérifie pas les déblocages) ; (h) `page.addInitScript` **rejoue à chaque navigation, reload compris**
  → vider `localStorage` là-dedans fait repartir un test de rechargement sur une partie NEUVE.
  ⚠ **NON-KO à ne pas rechercher** : les **5 bâtiments du début de partie ont tous `power: 0`** → leur
  fiche n'affiche **aucune ligne Élec.**, c'est correct ; la **`machine_outil` est à conso PLATE** →
  son panneau câble n'a ni « Demande min→max » ni lignes idéales, c'est le gate voulu ; et une MOT2
  forgée en dessous de son niveau d'entrée **ressort au Nv.11 après rechargement** (migration 13.27
  des bâtiments de palier, préexistante).
  ⚠ **Taille : 2 979 389 → 2 983 827 o (+4 438 o).** Les 9 éditions du brief seules pèsent **+2 816 o**
  (exactement l'attendu) ; le reste est le commentaire de version et `GAME_NOTES`.
  ⚠ **HORS PÉRIMÈTRE, non touché** : `BuildingDetailModal` et `UpgradePanel` (**vérifiés byte-identiques
  à la base**), `portAskNeeded`/`askPortFor`, le moteur électrique (`processHeat`, charge/décharge des
  accumulateurs), `fmtPower`/`fmtEnergy`/`fmtEnergyPair`/`fmtSig`, `TIER_STEP`, `SAVE_VERSION`, et
  **aucune traduction** des 6 nouveaux libellés (repli fr hors-fr, comme les astuces depuis 13.32).
- **État précédent : `GAME_BUILD = 328`, `GAME_VERSION = 'Alpha 14.46'`, `SAVE_VERSION = 31`.**
  Changement 14.46 (brief `BRIEFMOT2VERROUSILE` + pack `pack_usine_moteur_nuc_v2`) : **USINE MOTEUR
  NUCLÉAIRE V2 (nœud 41) + VERROUS D'ÎLE CONDITIONNELS.** `SAVE_VERSION` INCHANGÉ — c'est le cœur du
  lot B : l'état des verrous est **entièrement dérivé** de `techTree.nodes[].status`, déjà persisté.
  Les **25 ancres du brief sont sorties UNIQUES sur la base 14.45/327**, sans exception (la base du
  brief était exacte, cas rare).
  (1) **LOT A — MOT2** : palier V2 de `usine_moteur_nuc` (`TIER_NEXT` cap 9, `TIER_STEP` entrée u10,
  forfait alliage 1600 + p.précision 800 + câble supra 800 + ordi quantique 10). **Sortie, sigmoïde
  élec (64/448/60) et exclusivité île 5 IDENTIQUES à la V1** : c'est un **allègement de recette**, pas
  un gain de débit. Les 3 intrants « tonnage » montent d'un cran, calibrés pour que leur débit **au
  Nv.11 égale celui de la V1 au Nv.1** (mesuré : 5,12 · 5,12 · 1,024 contre 5 120 · 5 120 · 1 024) ;
  le **plutonium reste à 0,1** (identité nucléaire).
  ⚠ **LE PIÈGE DU LOT, confirmé en moteur réel** : la V1 est un cas de chaleur **PLAT** codé en dur sur
  son id, à DEUX endroits. La V2 passe en **PROPORTIONNEL** → sans l'ajout à la branche `HEAT_PER_MW ×
  conso` du tick, la chaîne de ternaires retombe sur `: 0` et **la V2 n'émettrait AUCUNE chaleur**,
  héritant par accident de l'identité `noHeat` de la Centrale Nucléaire V2. Mesuré après correctif :
  émission **variable 0,027 → 0,064 MJ/s** (19 valeurs distinctes sur 30 ticks, elle suit bien la
  sigmoïde) et **trip au tick 58** sur un plafond `heatCapOf` de 3,84 MJ. `heatCapOf` n'a bien reçu
  **aucune modification** : sans cas particulier sur son id, elle retombe d'elle-même sur
  `HEAT_PER_MW × conso nominale`.
  ⚠ **CONSÉQUENCE D'ÉQUILIBRAGE À SIGNALER** (non demandée, mesurée) : au Nv.11 la V1 émet **1 048,6
  MJ/s** (plat, 1,024 × 1024) contre **65,5 MJ/s** pour la V2 → densifier **divise la chaleur par 16**.
  C'est la conséquence directe de la décision « proportionnel » du brief, à arbitrer au playtest.
  (2) **`fab_ordi_quantique` : sortie 0,01 → 0,0625/s**, **intrants INCHANGÉS** (gain d'efficacité
  ×6,25 assumé — à 0,01 la livraison du nœud 40, 1 000 ordinateurs, demandait ~28 h au Nv.1).
  (3) **LOT B — `exclusiveUntilNode: N`** : un bâtiment cesse d'être exclusif dès que le nœud N est
  **confirmé**. `four_arc_tungstene` → 39, `machine_outil` → 41, `centrale_gaz` → 37. Deux helpers
  calqués sur `coolerModesAvailable`/`coolerModesFor` : **`exclusiveIslandWith(id, freedSet)`** (pour la
  `Toolbar`, qui n'a qu'un ensemble dérivé) et **`exclusiveIslandFor(game, id)`** (pour les 3 sites à
  état de partie : `selectTool`, changement d'île, mode Copier) ; tous deux rendent **`null`** quand le
  bâtiment est libéré — exactement la valeur que les appelants comparaient déjà.
  ⚠ **`ToolButton` n'a demandé AUCUNE modification** (vérifié) : sa ligne `title:` est gardée par
  `offIsland &&`, calculé par le parent → une fois libéré, `offIsland` devient faux et le `title`
  retombe sur `undefined` tout seul.
  ⚠ **LOGISTIQUE ASSUMÉE** : l'`oxygene` de la centrale à gaz est porté par le TUYAU et **absent de
  `TRADE_LIQUIDS`** → non expédiable. Toute île d'accueil devra produire le sien sur place (Séparateur
  d'Air local), comme le Cryostat en 14.37. **Ce n'est pas un bug**, `TRADE_LIQUIDS` n'est pas touché.
  (4) **SPRITE** ⚠ **ÉCART NÉCESSAIRE au §A7 du brief** : le brief pré-câblait le candidat
  `bat_usine_moteur_nucleaire_v2`, or le pack livré nomme sa clé **`bat_usine_moteur_nuc_v2`** (elle
  COLLE à l'id, contrairement à la V1). Appliqué verbatim, le candidat n'aurait jamais existé, la V2
  aurait emprunté l'art de la V1 **et son animation aurait été morte** (`ANIM_BY_SK` indexe, lui, la
  vraie clé). Liste corrigée en `['bat_usine_moteur_nuc_v2', 'bat_usine_moteur_nucleaire']` (repli V1
  conservé). Mesuré à l'espion `drawImage` : c'est bien **le sprite dédié qui est dessiné, jamais le
  repli**. Frame 0 == statique **au pixel près** (0 px d'écart) → aucun saut au démarrage de l'anim.
  ⚠ **HORS PÉRIMÈTRE, À TRANCHER (le point le plus important du rapport)** : la MOT2 est le **SEUL**
  des 16 bâtiments de `TIER_STEP` **absent de `TOOLBAR_GROUPS`** → elle ne s'obtient QUE par
  densification, jamais par pose directe. Le brief ne demandait pas d'entrée de menu ; une ligne dans le
  groupe `nuclear` suffirait (le coût de pose serait `cumulativeInvested`, mécanisme en place depuis
  13.27). Conséquence annexe : sa fiche `BuildingDetailModal` (appui long au MENU) est **inatteignable**,
  donc la ligne « Exclusif » du §B6 n'est pas observable sur elle.
  Validé : `node --check` (7 blocs) + Chromium **4 suites, 48 assertions, 0 KO, 0 erreur JS**, suites
  rejouées **2 fois intégralement sans flottement**, en viewport 420 px / DPR 3. **UI RÉELLE** :
  densification par tap canvas sur l'outil Améliorer → `usine_moteur_nuc_v2` Nv.11 et **forfait débité
  au près** (1600/800/800/10) ; fiche « 65,5 MW→524 MW », intrants du palier, sortie 102,4/s ; les 3
  verrous vus **dans le menu Bâtiment depuis l'île 1** (grisés avant, non grisés après), fiche annonçant
  « Exclusif · Île 6 — libéré par « Data Center » » **puis plus de ligne du tout** après le nœud 37 ;
  bascule d'île outil en main (désélectionné avant 41, **conservé** après) ; mode Copier ; et
  **non-régression** : `mine_tungstene`/`fab_ordi_quantique`/`data_center`/`usine_moteur_quantique`
  restent île 6 même après le nœud 43, tout le bloc île 7 reste île 7. **Round-trip de save** : SAVE 31,
  et une save créée sur le **build 327 (avant patch)** rechargée en 328 → 4 bâtiments intacts,
  0 `tickErrors`, horloge qui avance, **verrous recalculés seuls** depuis `techTree`.
  ⚠ **CONTRÔLE SHA-256 (§6.1 du brief)** : les **26 blocs livrés sont VERBATIM** dans le HTML
  (hash brief == hash ré-extrait, **0 divergence**) et **0 anomalie d'échappement**. ⚠ Corollaire
  assumé : les commentaires du brief portent le littéral **« 14.4x »** (non substitué, sinon les hashes
  divergeaient) — à remplacer par 14.46 d'un coup si on le souhaite.
  ⚠ **BUG PRÉEXISTANT TROUVÉ EN PASSANT, NON CORRIGÉ** (hors périmètre) : outil **Copier** en main +
  survol d'une tuile → `drawHover` appelle `canPlace(r, c, '__copy')`, or `BUILDINGS['__copy']` est
  `undefined` → **`Archipel frame error` à CHAQUE frame** (le `try/catch` de 14.13 l'avale, la frame est
  perdue). **Contre-épreuve faite sur le build 327 d'origine : 35 erreurs, contre 36 sur le build patché
  → ce n'est PAS une régression.** Correctif d'une ligne : une branche `tool === COPY` dans `drawHover`.
  ⚠ **PIÈGES DE HARNAIS** : (a) le tuto est bloquant et son gate passe par un **state REACT** → écrire
  `game.tutorial` ne suffit PAS, il faut cliquer le VRAI bouton « Passer » ; (b) `BuildingDetailModal`
  se rend dans **`.slot-panel`** (mêmes classes que le panneau de sauvegarde) et son backdrop
  **intercepte tous les clics** — le purger avant chaque interaction ; (c) une vignette **GRISÉE**
  ouvre la FICHE et **n'appelle PAS `selectTool`** (14.31) → y attendre un toast de refus est un faux
  KO ; (d) l'onglet **Améliorer est un TOGGLE** : le re-cliquer désélectionne l'outil et le tap ouvre
  alors l'InfoPanel (2 clics armés) au lieu de l'UpgradePanel (1 clic) ; (e) `useGhostGuard` avale
  toujours le 1ᵉʳ clic d'un panneau.
  ⚠ **Taille : 2 969 462 → 2 979 389 o (+9 927 o).** Les 26 blocs du brief seuls pèsent **+6 941 o** ;
  le reste = les 2 PNG en data-URL (~1 148 o), leur commentaire, la note de version, `GAME_NOTES` et
  4 clés ajoutées à `__heat` (`exclusiveIslandFor`/`With`, `islandFreeSet`, `TIER_PREV`).
  ⚠ **HORS PÉRIMÈTRE, non touché** : la V1, `heatCapOf`, `TRADE_LIQUIDS`, `canPlace`/`tryPlace` (qui ne
  lisent pas `exclusiveIsland`), les autres bâtiments exclusifs, `SAVE_VERSION`, et **aucune traduction**
  des 2 nouveaux libellés (« — libéré par « … » » → repli fr hors-fr, comme les astuces depuis 13.32).
- **État précédent : `GAME_BUILD = 324`, `GAME_VERSION = 'Alpha 14.42'`, `SAVE_VERSION = 31`.**
  ⚠ Le mémo n'a pas été tenu pour les versions 14.43 → 14.45 (builds 325-327) : ce bloc décrit la 14.42.
  Changement 14.42 (**LOT B6** du brief `B6_brief`) : **RÉVÉLATION ANIMÉE DE L'ÎLE 6** sur la carte
  de l'archipel. À la **PREMIÈRE** ouverture de la carte après le déblocage : un banc de brume se
  lève (0 → 1,4 s), l'île émerge (0,5 → 1,5 s), la liaison 5-6 se trace (0,9 → 1,6 s). Une seule
  fois dans la partie. `SAVE_VERSION` INCHANGÉ (le drapeau est un champ OPTIONNEL).
  (1) **9 modifications** : `carte_brume` (sheet 768×128 = 6 frames de 128, dithering Bayer donc
  alpha strictement 0/255 — langage pixel du jeu, PNG en palette) ; `reveal_css.txt` ; la prop
  `reveal` ajoutée à `ArchipelMap` ; la classe `reveal` sur le trait menant à l'île révélée ; le
  bloc `const iles` remplacé (classe `reveal` + calque de brume) ; l'état `archReveal` dans
  `PortPanel` ; la prop passée à la carte ; et **2 lignes de persistance** (sérialisation +
  chargement).
  ⚠ **`clip-path` et NON une largeur ou une transform animée** pour le tracé du trait : le bouton
  porte déjà `translateY(-50%) rotate(Xdeg)`, animer sa `transform` le **décrocherait de son île**
  (le défaut que B5 mesurait à 2 px près). Mesuré : le **rectangle du trait est IDENTIQUE pendant
  et après** l'animation (écart 0,00 px) et sa `transform` inline est inchangée.
  (2) **PERSISTANCE — `SAVE_VERSION` ne bouge pas** : `archiVu6` est un champ additif. ⚠ **Le repli
  du chargement est le point critique** : `g.archiVu6 = data.archiVu6 != null ? … : !!(data.
  islandUnlocked && data.islandUnlocked[6])` — sans lui, **TOUTES les parties en cours rejoueraient
  la révélation d'une île qu'elles possèdent depuis longtemps** à la mise à jour.
  (3) **`prefers-reduced-motion: reduce`** coupe les 3 animations et masque la brume (`display:none`)
  → l'île et le trait sont directement en place.
  Validé : `node --check` (7 blocs) + Chromium **7 suites, 28 assertions, 0 KO, 0 erreur JS**, suite
  rejouée 3 fois sans flottement. Cycle COMPLET vérifié : partie sans île 6 → rien ; île 6 débloquée
  → brume + `arch-node.reveal` + `arch-link.reveal` + drapeau posé au montage ; fermer/rouvrir → plus
  d'animation ; **sauvegarde forcée puis RECHARGEMENT** → `archiVu6` bien sérialisé, toujours pas
  d'animation ; et **le cas le plus facile à casser** : save dont on a RETIRÉ `archiVu6` (simule une
  save pré-B6) avec l'île 6 débloquée → le repli déduit `true`, **aucune animation**. Contexte
  `reducedMotion: 'reduce'` : 0 animation, brume masquée, île à `opacity 1`, trait non clippé.
  ⚠ **PIÈGE DE MESURE (m'a donné un faux KO)** : mesurer la distance trait ↔ centre de l'île
  **PENDANT** la séquence ne teste PAS le `clip-path` — l'île 6 est elle-même en train d'émerger
  (`scale(.84)→1`, `translateY(-44%)→(-50%)`), donc **SON centre bouge de 6,66 px** (mesuré :
  87,4 px de large et centre y=493,15 pendant, 104 px et y=486,49 après). Le bon test est la
  **stabilité du rectangle du trait** entre l'état animé et l'état final, plus l'alignement APRÈS
  la séquence (mesuré 1,99 / 1,63 px, dans la tolérance B5 de 2 px).
  ⚠ **CONTRÔLE ANTI-ÉCHAPPEMENT (nouveau, suite au `\xEE` de B5)** : après application, vérifier que
  **chaque fichier livré se retrouve VERBATIM** dans le HTML (`html.count(contenu) == 1`) et qu'aucun
  `I18N.t("…\\…")` ne contient de double antislash. Fait : les 4 fichiers verbatim, 0 anomalie.
  `node --check` ne voit PAS ce défaut — seul ce contrôle (ou le rendu) l'attrape.
  ⚠ **Taille : 2 947 552 → 2 955 424 o (+7 872 o).** Le brief annonçait +7 489 pour les 9
  modifications seules ; mesuré **+7 481** avant le bump (8 octets d'écart, un détail de saut de
  ligne dans un bloc remplacé), le reste étant le commentaire de version.
  ⚠ **HORS PÉRIMÈTRE** : aucune traduction des nouveaux libellés (repli fr), aucune autre île n'a de
  révélation (`ARCHI_CACHEE` ne contient que la 6), pas de son.
- **État précédent : `GAME_BUILD = 323`, `GAME_VERSION = 'Alpha 14.41'`, `SAVE_VERSION = 31`.**
  Changement 14.41 (**LOT B5** du brief `B5_brief`) : **CARTE DE L'ARCHIPEL** — l'onglet « Transit
  archipel » du Port ne liste plus les flux par direction, il **dessine l'archipel**. Une liaison =
  un TRAIT qu'on touche pour ouvrir son détail (les 2 sens réunis). `SAVE_VERSION` INCHANGÉ.
  ⚠ **LE BRIEF PROPOSAIT « 14.39 » — DÉJÀ PRIS** par le build 321 (palier Collisionneur, raccords du
  landmark, Refroidisseur ×128). Livré en **14.41 / build 323**. Le brief visait la 319 ; les
  **5 ancres ont été re-vérifiées sur la 322 et sont TOUTES restées uniques** (le panneau Port n'a
  été touché ni par 14.39 ni par 14.40) — c'est pourquoi le delta annoncé tombe juste.
  (1) **5 modifications** : `patch_carte.js` (7 sprites `carte_*`) avant l'ancre `ANIM_BY_SK` ;
  `carte_css.txt` avant le commentaire CSS « Vue Transit archipel » ; le composant `ArchipelMap`
  avant `function PortPanel({` ; l'ancre A4 (1 043 car., l'ancien bloc de rendu de l'onglet)
  REMPLACÉE par `carte_onglet.js` ; et l'état `archSel` ajouté après `const [tab, setTab]`.
  **Aucune purge** (les 7 clés `carte_*` sont neuves) et **aucun CSS existant touché** (le détail
  d'une liaison réutilise `pp-arch-link` / `-head` / `-row` / `-rate`).
  (2) ⚠ **BUG DANS LE FICHIER LIVRÉ, corrigé** : `carte_onglet.js` contenait
  `I18N.t("Flux entre \\xEEles")` — **double antislash**, là où l'ancre d'origine a `\xEE`. Livré
  tel quel, le titre s'affichait littéralement **« Flux entre \xEEles »** ET la clé i18n ne
  correspondait plus à aucune traduction (double casse : affichage + i18n). Restauré à `\xEE`
  (vérifié après coup : le titre rend « Flows between islands » en locale EN).
  (3) **Règles d'affichage actées** : îles **2-5 verrouillées → GRISÉES** en CSS
  (`filter: grayscale(1) brightness(.6)`, aucun sprite `_gris` supplémentaire) ; **l'île 6 n'est PAS
  dessinée du tout** tant qu'elle est verrouillée (`ARCHI_CACHEE = {6:true}` — c'est la surprise du
  jeu, une silhouette grisée la vendrait) ; **l'île 7 est absente** de la carte (elle transite par
  l'élévateur, pas par bateau) — elle n'est ni dans `ARCHI_POS` ni dans `SHIP_LINKS`, et le
  composant est null-safe si un lien pointait vers une île sans position.
  ⚠ **`.arch-map` fixe `aspect-ratio: 100/132` et c'est STRUCTUREL** : c'est ce ratio qui permet de
  calculer les angles des liaisons **sans mesurer le DOM** (`ARCHI_RATIO = 1.32` convertit un écart
  vertical en % de HAUTEUR vers des % de LARGEUR). Changer l'un sans l'autre fait diverger les
  traits de leurs îles. Pour ajouter une île : une entrée `ARCHI_POS`, un sprite `carte_ile_N`, une
  entrée `SHIP_LINKS` — rien d'autre.
  Validé : `node --check` (7 blocs) + Chromium **6 suites, 22 assertions, 0 KO, 0 erreur JS**, suite
  rejouée 3 fois sans flottement, en viewport **420 px / DPR 3** (mobile réel). **Le test du ratio,
  qui est le point sensible** : mesuré au DOM, chaque trait **part du centre de son île A et arrive
  au centre de son île B**, écart max **2,09 px** — et le cadre mesure bien 1,320. Cible tactile
  **26 px CSS = 78 px physiques** en DPR 3 (au-dessus des ~70 px des onglets du jeu). Partie NEUVE :
  5 îles dessinées, 4 grisées, **0 trace de l'île 6 et 0 trait 5-6** ; après déblocage : 6 îles,
  0 grisée, les 5 liaisons de `SHIP_LINKS`. Sélection : clic → contour cyan + détail, reclic →
  refermé et l'invite revient. Non-régression : les 2 onglets, la carte **seulement** dans l'onglet
  archipel, « Transit île » intact.
  ⚠ **PIÈGE DE HARNAIS** : `useGhostGuard` (13.50) **avale le 1ᵉʳ clic** du panneau tant qu'aucun
  `pointerdown` INTERNE n'a eu lieu depuis son ouverture → un test qui clique `.pp-tab` juste après
  avoir ouvert le Port ne déclenche RIEN (symptôme : « la carte n'est pas rendue »). Amorcer le
  garde par un `pointerdown` sur `.port-panel` avant chaque clic.
  ⚠ **Taille : 2 934 736 → 2 947 552 o (+12 816 o).** Le brief annonçait **+12 252** et c'est
  EXACTEMENT le delta des 5 modifications (mesuré avant le bump) ; les 564 octets restants sont le
  commentaire de version. **Les tailles du brief B5 sont en octets et concordent.**
  ⚠ **HORS PÉRIMÈTRE** : aucune animation d'apparition de l'île 6 (à trancher), l'onglet « Transit
  île » et le panneau Production inchangés. ⚠ Les 2 nouveaux libellés (« Touchez une liaison pour
  voir son transit. ») **ne sont pas traduits** → repli fr hors-fr, comme les astuces depuis 13.32.
- **État précédent : `GAME_BUILD = 322`, `GAME_VERSION = 'Alpha 14.40'`, `SAVE_VERSION = 31`.**
  Changement 14.40 (brief `BRIEFSFX14.38`) : **EXTENSION DU MODULE AUDIO — 17 sons, file de sons
  simulation → UI, rebranchement de la dette, et BOUCLE audio du Collisionneur.** `SAVE_VERSION`
  INCHANGÉ (aucun champ persisté ; `sfxQueue` et `co._haltPrev` sont transitoires).
  ⚠ **LE BRIEF VISAIT « 14.38 / build 320 » SUR UNE BASE 319 — CES VERSIONS EXISTENT DÉJÀ** (14.38 =
  lot B4 arts dédiés, 14.39 = palier Collisionneur). Contenu livré en **14.40 / build 322** ; les
  **26 ancres ont été re-vérifiées sur le build 321 — toutes UNIQUES**, y compris les 6 multi-lignes.
  Les 3 avertissements du brief sont CONFIRMÉS : `SFX.play('click')` existe **12 fois** (4.11 exige
  donc son ancre multi-lignes) et `SFX.playThrottled('powerAlert', 4000|8000)` **3 fois chacune**
  (4.6/4.13/4.14 passent obligatoirement par la ligne `showToast` unique qui les précède).
  (1) **LOT 1 — 17 sons** (catalogue **48 → 65**) en 4 sections avant `/* --- Système & slots --- */` :
  Collisionneur (boot/ready/launch/round/match/stop/halt), Souterrain (elevator/drillStart/drillDone/
  pocketFound), Logique (logicGate/logicRotate/logicToggle), Chaleur (heatWarn/heatTrip/nucSafety).
  Uniquement les helpers existants `tone`/`noise`/`woosh`. Les 5 nouveaux sons d'alerte sont ajoutés
  à `ALERTS` (métadonnée exportée, **non lue par `play()`** — vérifié, purement documentaire).
  (2) **LOT 2 — RÈGLE D'OR : aucun `SFX.play()` depuis le tick.** `onTick` est appelé DIRECTEMENT
  par `runCatchUp` → un son dans `processCollider`/`processLogic` produirait une avalanche au retour
  du joueur. Nouveau **`qSfx(game, name)`** (plafond DUR 32, pas de `shift()` → aucun coût O(n) au
  tick) + **`drainSfxQueue(game)`** appelé **UNE fois par frame** entre `g.animClock++` et
  `checkTutorial()` — **hors** de la boucle `while (g.tickAcc >= 1 …)`, qui peut tourner **200 fois**
  par frame en mode rapide × booster. File > 8 ⇒ **purge sans jouer** ; sinon dédoublonnage et
  **2 sons maximum**. ⚠ **Vérifié par GREP, pas à l'œil** (point 4 du brief) : **0 appel réel** à
  `SFX.play`/`playThrottled` dans `processCollider`, `tickIsland`, `onTick`, `processLogic`,
  `processHeat`, `colliderPenalty`, `tickShips`, `runCatchUp` (les commentaires citant « SFX.play »
  sont exclus du contrôle).
  (3) **LOT 3 — dette** : 5 sons muets rebranchés (`tabSwitch` sur les 2 onglets du Port,
  `boatUnlock` à l'amélioration du transit, `load` à la fermeture du récap hors-ligne — **seul moment
  où il est audible**, le contexte audio n'existant pas avant le 1er geste).
  ⚠ **ÉCART SIGNALÉ — `placeC` reste INERTE.** Le brief fixe le critère de landmark à `w*h >= 6`,
  mais **aucun bâtiment posable n'atteint 6 tuiles** : le plus gros est **2×2** (les centrales, 4
  tuiles), et le Collisionneur 3×2 est un **landmark de TERRAIN**, jamais posé par `tryPlace` (il n'y
  a par ailleurs **aucun champ `landmark`** dans `BUILDINGS` — les 17 occurrences du mot sont des
  commentaires). La branche est écrite avec une constante `LANDMARK_TILES = 6` : **la passer à 4**
  la viserait aux centrales. Valeur du brief conservée, décision laissée au joueur.
  (4) **LOT 4 — accroches** : `colliderBoot` (off→starting), `colliderReady` (**seulement** si l'état
  devient `ready` : s'il enchaîne sur `running`, `colliderLaunch` a déjà sonné), `colliderRound`,
  `colliderMatch`, `colliderStop` (remplace `powerAlert` sur la pénalité), `colliderLaunch` (remplace
  `unlock`) ; `elevator` pour tout trajet impliquant l'île 7 ; `drillStart` **remplace** le `place`
  générique de `tryDrill` (les deux ensemble se chevaucheraient) ; `drillDone`/`pocketFound` dans un
  drain **jusque-là totalement muet** ; `logicRotate`/`logicToggle` selon `patch.__cycle` ;
  `logicGate`/`junction`/`cable` à la pose ; `heatTrip`/`heatWarn`/`nucSafety` ; `click` ajouté à
  `setArcMode` (il était le seul à ne pas sonner, contrairement à `setArcSel`/`setCoolSel`).
  ⚠ **`colliderHalt` sur TRANSITION uniquement** (`co._haltPrev`) : sans cette garde il partirait à
  **chaque tick** de panne. `_haltPrev` n'est pas persisté — la sérialisation du collisionneur est
  une **liste blanche** (`state/timer/cur/penalties/enabled/launched`), vérifiée.
  (5) **LOT 5 — BOUCLE** (`loopStart`/`loopSet`/`loopStop`, voix = 2 osc + filtre + gain) :
  **triangle 55 Hz + sine 110,7 Hz** (désaccord → battement de « machine »), lowpass Q 0,8. Toutes
  les transitions en `setTargetAtTime(…, 0.15)` (une affectation directe produit un zipper noise).
  ⚠ **NE PAS piloter par `co.cur`** : c'est une sinusoïde PERMANENTE de période `COLLIDER_START`
  (600 ticks) → la boucle respirerait toutes les 10 min À VIE. Elle suit l'**avancement du démarrage**
  (monotone) : gain 0,020 → 0,100 et coupure 90 → 520 Hz, **maximum atteint pile quand `colliderReady`
  sonne**, puis TENU. ⚠ **`setEnabled(false)` coupe toutes les voix** ; démuter ne relance rien (c'est
  la frame qui redemandera `loopStart`).
  ⚠ **CORRECTIF NON DEMANDÉ MAIS INDISPENSABLE** : la boucle rAF **ne tourne plus en arrière-plan**,
  donc `colliderLoopFrame` n'aurait **jamais** été rappelée pour couper le bourdon → la voix aurait
  tourné écran éteint. Un `SFX.loopStop('collider')` est ajouté au handler **`onHide`** existant (le
  `flushSave` et sa garde `saveTimer` sont intacts).
  Validé : `node --check` (7 blocs) + Chromium **5 suites, 49 assertions, 0 KO, 0 erreur JS**, suites
  rejouées **2 fois intégralement sans flottement**. Web Audio RÉEL : les 17 sons joués sans
  exception. Moteur RÉEL (Collisionneur alimenté par un tuyau contre le landmark + citerne d'He3 —
  **sans carburant il reste `halt='fuel'` à vie et rien n'avance**) : boot, `colliderHalt` **une seule
  fois** sur 5 ticks de coupure puis **resonne** après rétablissement, `colliderReady` seulement si
  non lancé. **5000 ticks hors-ligne → file plafonnée à 32, drain joue 0 son.** Boucle : **20 cycles
  start/stop = 40 oscillateurs** (aucune fuite), `loopStart` idempotent (10 appels → 2 osc), `co.cur`
  ×8192 ne change ni gain ni coupure. **UI RÉELLE** : clics réels sur les 7 onglets d'île (1→2 =
  `islandTransition`, 6⇄7 = `elevator`), les 2 onglets du Port (`tabSwitch` ×2), l'amélioration du
  transit (`boatUnlock`), et **tap canvas réel** posant un `porte_and` → `logicGate`. Drains de frame
  réels : `heatTrip`/`heatWarn`/`nucSafety`/`colliderStop`/`drillDone`/`pocketFound`. **Round-trip de
  save** : SAVE 31, `sfxQueue` et `_haltPrev` **absents du fichier**, données intactes, 0 erreur.
  ⚠ **PIÈGES DE HARNAIS (coûteux)** : (a) `clearOverlays` clique `.research-backdrop`, qui est **aussi
  le fond du panneau Port** → l'appeler avant un clic d'onglet FERME le panneau (prévoir une variante
  sans nettoyage) ; (b) **`useGhostGuard` (13.50)** avale le 1ᵉʳ click d'un panneau tant qu'aucun
  `pointerdown` INTERNE n'a eu lieu → dispatcher un `pointerdown` dans le panneau avant de cliquer ;
  (c) le **TUTORIEL est bloquant** (13.60) : il verrouille les onglets et filtre le menu Bâtiment aux
  seuls bâtiments révélés → **le menu logique reste VIDE** tant qu'on n'a pas cliqué « Passer » ;
  (d) l'onglet Bâtiment est un **TOGGLE** → n'ouvrir que si `.build-panel` est absent.
  ⚠ **HORS PÉRIMÈTRE (§7), non touché** : ambiances par bâtiment, boucles data center/géothermie/four
  à arc, bips α/β/γ (exigeraient `play(name, arg)`), `gasHiss`, variation de pitch, et les 9 sons
  laissés en réserve (`titleSting`, `mapOpen`, `clickAlt`, `errorGeneric`, `importBlocked`, `noInput`,
  `networkSplit`, `notify`, `prodMilestone`).
  ⚠ **Taille : 2 921 480 → 2 934 736 o (+13 256 o).** Le brief plafonnait à 12 Ko : **dépassement
  assumé de 968 o** — le CODE seul pèse ~7,7 Ko (17 sons + API de boucle + file), le reste est la
  documentation des 5 pièges ci-dessus. Trois passes de condensation ont déjà retiré ~2,5 Ko ; couper
  davantage supprimerait des ⚠ (règle d'or, piège `co.cur`, timbre triangle, `placeC` inerte, `onHide`).
- **État précédent : `GAME_BUILD = 321`, `GAME_VERSION = 'Alpha 14.39'`, `SAVE_VERSION = 31`.**
  Changement 14.39 (brief `brief14.38` + 1 bug joueur) : **palier du Collisionneur indexé sur les
  RÉPARATIONS, raccords du landmark refaits, Refroidisseur ×128, hélium liquide gaté par le Cryostat,
  et la Centrale Nucléaire V2 n'affiche plus de chaleur.** `SAVE_VERSION` INCHANGÉ.
  ⚠ **LE BRIEF VISAIT « 14.38 / build 320 » — CETTE VERSION EXISTE DÉJÀ** (lot B4, arts dédiés, mergé
  entre-temps). Le contenu est donc livré en **14.39 / build 321** ; les 26 ancres du brief ont été
  re-vérifiées sur le build 320 — **25 uniques, 1 tronquée** (E4 : le brief écrivait
  `I18N.t("Posez un câble logique…")`, la vraie chaîne va jusqu'à « …du Collisionneur »).
  (1) **LOT A — `colliderPalier` lisait `COLLIDER_PUZZLE_NODES` (39/41) au lieu de
  `COLLIDER_REPAIR_NODES` (38/40/42).** Effet joueur mesuré : valider le nœud 41 (1 000 confirmations)
  faisait basculer en **P3 — 6 saveurs, 3 bits, leptons 000/111** alors que la Réparation III (nœud 42,
  qui livre XOR/XNOR) n'était pas faite → comparateur 3 bits + détecteur de motif à construire **sans
  les deux portes prévues pour ça**. Le commentaire d'en-tête disait déjà « 40 → P2, 41 → P3 »,
  incohérent avec lui-même. `colliderGoalLocked` exige désormais le puzzle **ET** la réparation
  suivante (sinon le garde-fou anti-sur-farming de 14.24 se désamorçait : palier constant, on accumule
  vers le seuil de N+1 avec les portes de N) ; au palier 3 il n'y a pas de réparation suivante → seul
  le nœud 43 libère. **ARRÊT COMPLET au seuil** (et plus seulement blocage de relance) : `processCollider`
  sort en `state='off'`, 0 kW, 0 He3, séquence annulée — une machine DÉJÀ EN MARCHE continuait sinon
  d'empiler. ⚠ **`co.enabled` n'est PAS touché** → la machine repart SEULE dès la réparation confirmée
  (vérifié), en repayant les 10 min. ⚠ **Dépassement résiduel d'UNE manche assumé** : `processCollider`
  tourne avant `processLogic`, la manche du tick où le seuil tombe est déjà jugée.
  ⚠ **ÉCART — le brief affirmait que `goalLocked` est déclaré AVANT `stLabel` dans le panneau. FAUX** :
  il l'était **après** `launchBlock` → le lire depuis `stLabel` aurait levé une `ReferenceError` (zone
  morte temporelle d'un `const`) et **tué le panneau au premier rendu**. La déclaration a été REMONTÉE.
  `co.palier`/`co.goal` sont posés AVANT tout early-return (le miroir restait figé machine éteinte) et
  le panneau lit désormais `colliderPalier(game)` directement.
  (2) **LOT B — raccords du landmark refaits** : l'ancien bloc cumulait le masque des 4 côtés et
  dessinait **un seul sprite APRÈS l'art** → deux réseaux opposés donnaient `tuyau_vX_10_EO`, soit un
  **tuyau peint EN TRAVERS de la machine**. Désormais **1 sprite par DIRECTION**, dessiné **SOUS l'art**
  (convention des stubs de bâtiment), et les **JONCTIONS sont reconnues** (`junctionDirOk`, règle d'axe
  13.18 ; le niveau vient de `netIds[carrier]`, `networkId` étant null sur une jonction — sinon repli V1
  systématique). Mesuré à l'espion `drawImage` : `tuyau_v3_08_O`, `cable_v1_04_S`, `tuyau_v1_02_E` —
  **0 masque combiné**, 69 raccords tous dessinés avant l'art de leur tuile.
  ⚠ **B.3 — CONTRÔLE VISUEL FAIT, aucun raccord avalé** : diff au pixel entre rendu avec et sans réseau,
  tuile par tuile → 43 px (bas-gauche), 21 px (haut-droite), 17 px (bas-droite) modifiés, **0 px sur les
  3 tuiles non raccordées**. L'art laisse bien passer le raccord (36 % de transparence).
  (3) **LOT C — Refroidisseur ×128** : élec. **1024 → 131 072 kW**, absorptions et eau/azote ×128,
  hélium gazeux ×8, hélium liquide ×4 → `sec 65,536 · eau 262,144 (32 768 eau/s) · azote 524,288
  (131 072 azote/s) · hélium 1 048,576 (8 He4/s) · hélium liquide 2 097,152 MJ/s (0,25/s)`.
  **Coût de construction INCHANGÉ** (arbitrage : le coût par MJ est déjà divisé par 128). Rapports
  eau/MJ (125) et azote/MJ (250) **inchangés** — c'est l'ÉCHELLE qui bouge. Calages : 131 072 azote/s =
  1 Séparateur d'Air au Nv.8 pile ; 0,25 He liquide/s = 1 Cryostat V1 pile (vérifiés).
  ⚠ **`coolerEffective` rend les valeurs de BASE (V1)** : le ×2^niveau est appliqué par les APPELANTS
  (boucle bâtiment pour les intrants, `processHeat` pour l'absorption) — piège de test, mesurer en
  moteur réel.
  (4) **LOT D — hélium liquide INVISIBLE sans Cryostat** (il n'est produit que par lui) : helpers
  `coolerModesAvailable(bool)` / `coolerModesFor(game)`, branchés au sélecteur de l'InfoPanel, à la
  ligne « Fluides » de la fiche (`BuildingDetailModal` reçoit `cryoOk`), + **garde dans `setCoolSel`**
  et **repli « sec » au chargement** d'une save 14.36→14.38 qui l'aurait sélectionné sans Cryostat.
  (5) **BUG JOUEUR — « on voit encore la chaleur émise de la nuke v2 »** : la V2 a `noHeat` et son bloc
  de chaleur est bien gaté par `b.heatCap` (absent) — MAIS les lignes **« Sortie »** et **« Prod.
  théorique »** de la fiche calculaient `NUC_POWER × upMult × frac × HEAT_PER_MW` **en dur, sans tester
  `noHeat`** → « ⚡ 16,4 MW · 🔥 2,05 MJ/s » sur une centrale qui n'émet rien. Le 🔥 est retiré pour une
  V2 (⚡ conservé), la V1 est intacte (vérifié par tap canvas réel sur les deux fiches).
  ⚠ **2ᵉ correctif, PAS demandé explicitement mais du même défaut** : `islandNuclearCoolingOk` comptait
  la V2 comme « centrale à refroidir » → monter sa puissance déclenchait le toast rouge « aucune tour
  aéroréfrigérante reliée par conduit — la centrale va surchauffer », **alarme fausse et impossible à
  satisfaire** (aucun `heatCap` → aucun trip possible). Une V1 sur la même île déclenche toujours
  l'alerte (contre-épreuve). ⚠ **Le joueur avait écrit « toast pas de conduit pour nuke v2, normal »** :
  si l'intention était de GARDER ce toast, c'est la seule ligne à annuler (`b.nuclear && !b.noHeat`).
  Validé : `node --check` (7 blocs) + Chromium **8 suites, 119 assertions, 0 KO, 0 erreur JS**, suite
  rejouée **3 fois sans flottement**. Moteur RÉEL : les 5 fluides mesurés un par un (fluide prélevé ET
  MJ réellement retirés d'une source saturée), Nv.2 exactement ×2, **tour non régressée** (256 eau/s,
  1,024 MJ/s) ; arrêt au seuil vérifié tick par tick (200 ticks → 0 confirmation de plus) puis reprise
  seule ; **UI RÉELLE par tap canvas** (fiche refroidisseur « ENTRÉES eau 32768/s · ÉLEC. 131 MW ·
  − 32768 eau/s → 262 MJ/s », 4 boutons de fluide → 5 dès le nœud 41 confirmé SANS rechargement ;
  panneau Collisionneur « ARRÊTÉ — palier atteint, réparation à livrer » en orange, P1 · 2 saveurs ·
  1 bit) ; **round-trip de sauvegarde** (SAVE 31, hélium liquide → « sec » sans Cryostat, CONSERVÉ avec) ;
  détecteur d'atteignabilité de l'arbre (14.27) rejoué → 0 nœud inatteignable ; **pas d'interblocage**
  (les 3 réparations n'exigent aucune sortie du Collisionneur : 38 alliage/supra/élém.moteur, 40 ordi
  quantique/pièce précision, 42 moteur quantique).
  ⚠ **PIÈGES DE HARNAIS** : (a) **l'eau n'est PAS dans `PORT_PIPE_RES`** → elle vit dans la CITERNE du
  réseau tuyau, pas au port (l'azote, lui, est au port) — mesurer la somme des deux ; (b) le **niveau
  d'un réseau** est porté par `g.networks[isl][nid].level`, PAS par `upgrade` du bâtiment d'infra ;
  (c) les **onglets d'île sont des SPRITES sans texte** → cibler par INDEX (0..6 = îles 1..7) ;
  (d) un espion `drawImage` capte TOUT l'écran → filtrer sur les positions où l'art du landmark est
  dessiné, sinon les tuiles d'infra ordinaires polluent la mesure ; (e) une fiche déjà ouverte fait
  croire au succès d'un tap → attendre la DISPARITION de `.info-panel` avant de retaper.
  ⚠ **HORS PÉRIMÈTRE, non touché** (conforme au §2 du brief) : `COLLIDER_HE3`, `COLLIDER_POWER`,
  `COLLIDER_GOALS`, l'art du landmark, le coût du Refroidisseur, `SAVE_VERSION`.
  ⚠ **Taille : 2 912 076 → 2 921 480 o (+9 404 o).**
- **État précédent : `GAME_BUILD = 320`, `GAME_VERSION = 'Alpha 14.38'`, `SAVE_VERSION = 31`.**
  Changement 14.38 (**LOT B4** du brief `B4_brief` — pack `ile6 v3.3`, `patch_lot7.js`) : **trois arts
  DÉDIÉS remplacent des emprunts.** ASSETS + 2 overrides — **aucune modification de rendu**.
  `SAVE_VERSION` INCHANGÉ.
  (1) **AUCUNE PURGE** (contrairement à B1/B2) : `carriere_v4` et `pompe_eau_v3` sont des clés
  **NEUVES** (elles n'existaient ni en sprite ni en sheet — seulement comme entrées
  `BLD_SPRITE_OVERRIDE`/`BUILDINGS`/`TIER_STEP`, vérifié) ; `item_helium_liquide` ne vit que dans le
  **grand littéral** (~ligne 1674) et n'est donc pas supprimable ligne à ligne — c'est la
  **réaffectation tardive** du patch qui le couvre (la dernière affectation gagne au runtime).
  ⚠ **Vérification faite avant d'insérer** : aucune des 3 clés n'est (ré)assignée APRÈS le point
  d'insertion — sinon le nouvel art aurait été écrasé silencieusement.
  (2) **INSERTION** de `patch_lot7.js` avant l'ancre `// Indexé par CLÉ DE SPRITE STATIQUE` :
  3 sprites + 2 sheets + 2 `ANIM_META`.
  (3) **LES 2 OVERRIDES PASSENT EN LISTES DE CANDIDATS** (mécanisme 14.32, premier présent gagne) :
  `carriere_v4: ['carriere_v4', 'mine_pierre_v4']` et `pompe_eau_v3: ['pompe_eau_v3', 'pompe_eau_v2']`
  → l'art dédié est pris, le repli reste derrière par sûreté. Les commentaires devenus FAUX
  (« Pas d'art dédié livré pour la pompe V3… ») sont remplacés.
  ⚠ **Ce que ça corrige côté joueur** : `pompe_eau_v3` affichait l'art de la V2 → **améliorer sa
  pompe ne changeait rien à l'écran** ; `carriere_v4` affichait `mine_pierre_v4` → **une carrière
  ressemblait à une mine**.
  Validé : `node --check` (7 blocs) + Chromium **5 suites, 27 assertions, 0 KO, 0 erreur JS**, suite
  rejouée 3 fois sans flottement. Résolution EN JEU : `buildingSpriteKey` rend bien `carriere_v4` et
  `pompe_eau_v3` (replis conservés en 2ᵉ position) ; **rendu RÉEL** (bâtiment posé + espion sur
  `drawImage`) → c'est bien le nouvel art qui est dessiné, **jamais l'emprunt**, et les 2 animations
  tournent. Les arts sont mesurés RÉELLEMENT différents de leur emprunt (**753 px** sur 1024 pour la
  carrière, **116 px** pour la pompe — la V2 est volontairement conservée au pixel près sous les
  ajouts, le joueur reconnaît sa machine). **`frame 0` == statique au pixel près (0 px d'écart)** sur
  les 2 sheets → aucun saut quand l'animation démarre. Famille hélium : `item_helium_liquide` a la
  **silhouette identique au pixel près** à `item_helium3` (**89 px opaques**, comme He3 ET He4) mais
  **61 px de coloris différents** (ménisque + liquide bleu) → homogène de forme, distinct d'un coup
  d'œil. Non-régression : les **113 bâtiments** résolvent vers un sprite présent.
  ⚠ **Taille : 2 906 078 → 2 912 076 o (+5 998 o).** Le brief annonçait +5 913 : l'écart de 85 octets
  vient uniquement du commentaire de version (4 lignes ici).
  ⚠ **HORS PÉRIMÈTRE** : `refroidisseur_v2`/`cryostat_v2` (les bâtiments s'appellent « V1 »),
  `item_helium4` à qui il manque les 2 px de reflet de `helium3`/`helium_liquide` (à faire seulement
  si l'écart se voit en jeu), et `logic_emetteur` qui résout `null` (préexistant, inerte).
- **État précédent : `GAME_BUILD = 319`, `GAME_VERSION = 'Alpha 14.37'`, `SAVE_VERSION = 31`.**
  Changement 14.37 (brief `brief1437correctifstransit`) : **correctifs I/O dynamiques du Refroidisseur,
  refonte de la liste des liquides expédiables, Cryostat posable partout, absorptions en échelle
  binaire, hélium en t4, mix réparti en barres (nucléaire ET fours à arc), sprite hélium liquide.**
  `SAVE_VERSION` INCHANGÉ (aucun champ persisté ne bouge ; tout le reste est de la data relue).
  Les **12 ancres du brief ont été trouvées UNIQUES** sur le build 318, sans exception.
  (1) **LOT A — les 4 lecteurs de `b.inputs` restants.** Le Refroidisseur n'a **aucun `inputs`
  statique** (son fluide est choisi au runtime via `coolerEffective`), or 4 sites lisaient encore la
  def. **A.1 `resourceRates`** est le seul défaut réellement fonctionnel : sa conso de fluide y valait
  **0** (contre-épreuve exécutée sur le build 318 : 0 pour les 4 fluides ; après patch : 256 / 1024 /
  1 / 0,0625 exacts). ⚠ **LE BRIEF SURESTIME LA PORTÉE** : il annonce « la conso reste à zéro dans le
  panneau Réseau **et** dans les stats par ressource ». **FAUX pour le panneau Réseau** — `netFlow` et
  `islandFlowAgg` étaient DÉJÀ corrects en 318 (mesuré : 256/s), le 14.36 ayant volontairement laissé
  le refroidisseur dans la boucle bâtiment. `resourceRates` n'est que le **repli** (lu quand prod ET
  conso valent 0, cf. 14.24) — le correctif est juste, son périmètre est plus étroit qu'annoncé.
  **A.2** (`usedRoad`/`usedPipe`) n'est **pas** l'arbitrage de saturation que le brief décrit : c'est
  l'**InfoPanel**. Variable d'instance = **`bld`**, mais on réutilise **`coolIO`** (déjà calculé
  ligne ~14618 par le 14.36) au lieu de rappeler `coolerEffective` — strictement équivalent.
  Effet réel : le refroidisseur est enfin vu comme usager du TUYAU → sélecteur de priorité de flux et
  vitesse % reflétant la saturation. ⚠ **Le MOTEUR n'est pas touché** : mesuré à l'identique au bit
  près sur 318 et 319 (netDemand 512, netFactor 0,125, 64 eau/s prélevés). **A.3** = `BuildingDetailModal`
  (appui long), qui travaille sur la DÉF sans instance → on liste les fluides possibles.
  **A.4** = `UpgradePanel`, variable d'instance **`bld`** (déclarée ligne ~15774), le brief est exact.
  (2) **LOT B — `TRADE_LIQUIDS`** devient `[petrole, acide, diesel, gaz_fossiles, helium4, methane]`.
  ⚠ **Vérifié avant d'écrire, la thèse du brief est JUSTE** : `PORT_PIPE_RES` n'est qu'un
  **pré-classement** (bucket initial de `isPortPipe`) ; la bascule pool → port se fait au tick dès
  qu'un réseau tuyau adjacent est `connected` et n'exclut que **`NON_STORABLE`** (lu ligne ~9199).
  `gaz_fossiles` et `methane` n'y figurent donc PAS et s'expédient quand même (mesuré par
  `transferLink` réel). `PORT_PIPE_RES` **non touché**.
  (3) **LOT C — Cryostat posable partout** (`exclusiveIsland: 6` retiré). Vérifié en moteur réel sur
  l'**île 1** : 0,25 He4 + 1024 azote → 0,25 He liquide, chaleur plate 2,048 MJ/s.
  ⚠ `terrains` vaut `['land','resource','coast']` au RUNTIME (le `coast` est ajouté par la passe
  13.79) — c'est le cas AUSSI sur le build 318, ce n'est pas un effet de bord du lot.
  (4) **LOT D — absorptions binaires** 0,512 / 2,048 / 4,096 / 8,192 / 16,384 MJ/s (× la Tour V1 =
  1,024). Débits de fluide INCHANGÉS. Mesuré sur une source SATURÉE (piège 14.36 (a) : `heatAbsorb`
  est ce qui a été absorbé, pas la capacité), et doublé exactement au Nv.2.
  (5) **LOT E — `helium4` t5 → t4** ; `helium3` et `helium_liquide` restent t5.
  (6) **LOT F — mix réparti en BARRES**, nucléaire ET fours à arc, structure
  `[label] [−] [barre + % en surimpression] [+] [débit /s]`. ⚠ **ARBITRAGE** : le brief dit à la fois
  « donner au mix le rendu du mode auto » (barre FINE `ip-nuc-mix-auto-bar`, 7 px) et « barre avec %
  en surimpression / patron de la jauge de la Centrale d'Enrichissement V2 ». Les deux sont
  incompatibles — une barre de 7 px ne porte pas de texte et paraît cassée entre deux boutons de
  26 px. On a retenu **la jauge `ip-nuc-gauge`** (24 px), conforme à la structure cible explicite et
  à la référence esthétique nommée. Le mode **auto reste INCHANGÉ** (barre fine, aucun bouton), comme
  l'exige le test 11. ⚠ **Les deux mécaniques NE sont PAS harmonisées** (demande explicite) : le
  nucléaire garde sa **somme verrouillée à 100** (`onSetNucMixDelta ±1`), le four à arc ses **poids
  libres normalisés** (`onSetArcWeight`, `clamp(cur ± 5, 0, 100)`, défaut 1) — vérifié par round-trip
  de save (poids 35/20/5 conservés). `ARC_DEF` ne contient que `four_arc_fer` et `four_arc_cuivre`,
  et le bloc de rendu est générique (`def.order.map`) → **un seul patch couvre les deux fours** ;
  le four à arc tungstène n'a pas de mix et n'est pas concerné.
  ⚠ **UNE SEULE règle CSS ajoutée** (`.ip-nuc-mix-row.pm`) : la jauge réutilise `ip-nuc-gauge` /
  `-fill` / `-lbl` telles quelles. **Colonnes de largeur FIXE `104px 28px 1fr 28px 60px`** et non
  `auto` : `display:grid` porte sur la LIGNE, donc chaque ligne est sa propre grille et des colonnes
  `auto` se dimensionnent ligne par ligne → les boutons ± se décalaient visiblement d'une ligne à
  l'autre selon la longueur du libellé (constaté à la capture, corrigé). 104 px parce que
  « Béton armé » mesure **93 px** (mesuré au navigateur, pas estimé) ; ellipse pour les autres langues.
  (7) **LOT G — sprite `item_helium_liquide`** inliné en tête de `__SPRITE_DATA__` (16×16 décodé).
  ⚠ `item_helium3` et `item_helium4` **n'existent toujours pas** (le brief suppose le contraire en
  parlant d'une « forme reprise de `item_helium3` ») : hélium liquide est le seul hélium à avoir une
  icône. Aucun code à modifier, `itemSpriteKey` résout automatiquement.
  Validé : `node --check` (7 blocs) + Chromium **6 suites, 120 assertions, 0 KO**, suite rejouée
  **2 fois sans flottement**. Moteur RÉEL : les 4 fluides du refroidisseur mesurés un par un par les
  3 chemins d'affichage (port, `netFlow`, `resourceRates`), ×2 au Nv.2, **0 conso en mode sec** ;
  absorptions mesurées sur source saturée ; **tour non régressée** (1,024 MJ/s, 256 eau/s, `port`) ;
  transit **6 liquides expédiés / 5 refusés** par `rawShippable` ET `transferLink` réels ; Cryostat
  produisant sur l'île 1 ; **UI RÉELLE par tap canvas** (les 2 fiches, 5 colonnes, jauges 24 px,
  somme 100 après 7 clics « + », auto intact, +5 de poids sur l'arc) ; **save build 318 rechargée en
  319** : refroidisseur (fluide `azote`, Nv.3), four à arc (mode + poids), cryostat île 6, stocks et
  mix nucléaire tous conservés, 0 `tickErrors`, horloge qui avance.
  ⚠ **Test 6 (élévateur) = CONSTAT, rien corrigé** : `elevatorSurfaceLinkedFor(game, carrier)` filtre
  par **PORTEUR**, jamais par ressource → azote et hélium liquide traversent toujours l'élévateur
  6 ↔ 7, `TRADE_LIQUIDS` ne l'affecte pas.
  ⚠ **PIÈGES DE HARNAIS (coûteux, nouveaux)** : (a) le playwright du dépôt attend la révision **1228**
  et l'image n'a que la **1194** → lancer avec `executablePath:
  '/opt/pw-browsers/chromium-1194/chrome-linux/chrome'`, ne JAMAIS tenter `playwright install` ;
  (b) **`unlimited` SURVIT à un `rebuildNetworks`** (héritage 13.33) → un test de saturation exige une
  **page neuve**, sinon on mesure un réseau resté illimité par un bloc précédent ; (c) ne brider QUE le
  tuyau : brider aussi le câble coupe le refroidisseur (1024 kW) et on ne mesure plus rien ;
  (d) **`flushSave`/`scheduleSave` sont des CLOSURES**, pas des globales — pour forcer une sauvegarde
  il faut armer `g.saveTimer` **ET** redéfinir `document.visibilityState` sur `'hidden'` (le handler
  `onHide` teste la propriété, dispatcher l'événement ne suffit pas) ; (e) `page.mouse.click()` rate
  souvent le tap canvas → `move` + `down` + pause + `up`, **avec réessai jusqu'à voir la fiche
  attendue** ; (f) `islandFlowAgg` renvoie `{prod:{}, cons:{}}` et **`nucMix()` une COPIE dérivée**
  (structure persistée = à plat dans `game.nuclearMix[isl]`) — deux faux « bugs » qui m'ont coûté du
  temps ; (g) le **404 console unique** est PRÉEXISTANT (identique sur 318, ressource PWA absente du
  serveur de test), ce n'est pas une régression.
  ⚠ **HORS PÉRIMÈTRE, non traité (conforme au §9 du brief)** : `PORT_PIPE_RES` intact, élévateur non
  modifié, arbre des besoins (`for (const k in b.inputs …)` du planificateur) laissé tel quel, les
  deux mécaniques de mix non unifiées, four à arc tungstène non touché, `SAVE_VERSION` non incrémenté,
  aucun rééquilibrage spontané.
  ⚠ **Taille : 2 900 283 → 2 906 078 o (+5 795 o).**
- **État précédent : `GAME_BUILD = 318`, `GAME_VERSION = 'Alpha 14.36'`, `SAVE_VERSION = 31`.**
  Changement 14.36 (brief `brief1430refroidissement`) : **CHAÎNE DE REFROIDISSEMENT — nouvelle ressource
  hélium liquide, bâtiments Refroidisseur et Cryostat, Data Center allégé mais chauffant, Usine Moteur
  Quantique à l'hélium liquide, soft cap de la Tour.** `SAVE_VERSION` INCHANGÉ (`pl.cool` = champ
  additif ; tout le reste est de la data relue depuis `BUILDINGS`).
  ⚠ **LE BRIEF ANNONÇAIT « 14.30 / build 312 » SUR UNE BASE 311 — cette version EXISTE DÉJÀ** (lot A
  chaleur nucléaire). Le contenu est donc livré en **14.36 / build 318** ; toutes les ancres du brief ont
  été re-vérifiées sur le build 317 (24 ancres, **23 uniques, 1 introuvable** — cf. écart (5)).
  (1) **Nouvelle ressource `helium_liquide`** (t5, carrier `pipe`, `PORT_PIPE_RES`), déclarée juste
  après `helium4` aux 3 tables pour garder sa place dans l'inventaire (l'ordre de déclaration fait
  RANG). **`helium4` passe aussi au port** (il entre dans le COÛT du Cryostat, débité du port).
  ⚠ **ÉCART NÉCESSAIRE — `PORT_PIPE_RES` NE SUFFIT PAS AU TRANSIT MARITIME** : c'est exactement le cas
  de l'azote depuis 13.82 (stocké au port, volontairement NON transitable). Le brief supposait le
  contraire et son test 10 exige pourtant l'expédition 6→5. Les deux hélium sont donc aussi ajoutés à
  **`TRADE_LIQUIDS`** (sans quoi le Refroidisseur à l'hélium serait inutilisable hors île 6 et le
  Cryostat, exclusif à l'île 6, ne servirait à rien). **L'azote reste non transitable** (contre-épreuve
  au test).
  (2) **Soft cap de la Tour aéroréfrigérante** : `tour_aerorefrigerante` rejoint `COST_SOFTCAP_X2` →
  au-delà du Nv. affiché 10, le facteur de coût DOUBLE à chaque cran (crans 5,4 / 10,8 / 21,6, courbe
  **identique à l'aciérie**, vérifié). Bridage ÉCONOMIQUE seul : absorption et eau doublent toujours.
  Nv. 1→9 **strictement inchangés**.
  (3) **REFROIDISSEUR** (nœud 37, groupe **Tungstène**, 1024 kW, `tour: true` + nouveau flag `cooler`).
  Successeur de la Tour, **posable île 7 comprise** (la tour a `forbiddenIslands: [7]`) — c'est son
  intérêt principal : le souterrain traite enfin sa chaleur sur place au lieu de la faire remonter par
  l'élévateur. **`COOLER_DEF`** (patron d'`ARC_DEF`, posé juste après `arcDefaultState`) : sélecteur de
  fluide à 5 modes, **sec 0,5 · eau 2 · azote 4 · hélium 8 · hélium liquide 16 MJ/s** absorbés à V1
  (×2 par niveau), intrants respectifs **aucun · 256 eau · 1024 azote · 1 He4 · 0,0625 He liquide**.
  ⚠ **ARCHITECTURE — il reste dans la BOUCLE BÂTIMENT**, il n'emprunte PAS le chemin hors-boucle de la
  tour : consommateur pur avec `power > 0`, donc jamais sauté par la garde `!effOutputs && power <= 0`
  (précédent exact : le Data Center). Conséquence voulue : sa conso de fluide apparaît **nativement**
  dans les panneaux Réseau et Production (le défaut de lisibilité 14.17 de la tour ne se reproduit pas),
  et déficit/`inFac`/`pwrAvg`/motifs sont gérés par le moteur existant. Dans `processHeat` étape 2, la
  branche `cooler` **ne prélève RIEN et n'écrase AUCUN champ d'état** (`active`, `disc`, `inFac`,
  `pwrAvg`, `regime`, `waterFrom/Avail/Need/Drawn` restent ceux de la boucle bâtiment) : elle ne fait que
  lire `bld.regime` (à jour — `processHeat` tourne APRÈS la boucle et après le lissage de `pwrAvg`).
  ⚠ `to.absorb` et `to.condIds` sont posés **AVANT le `continue`** : l'inscription dans `condNets` se
  fait dans une boucle ULTÉRIEURE qui les relit. Régime PROPORTIONNEL (pas d'`allOrNothing`) : 50 %
  d'eau → 50 % d'absorption.
  ⚠ **ÉCART (a) — `buildingConnectsCarrier(refroidisseur, 'pipe')` forcé à vrai** : son fluide étant
  choisi au runtime, il n'a AUCUN `inputs` statique → sans ce cas il ne dessinerait aucun raccord tuyau
  et ne ferait pas **PONT**, alors que la tour qu'il remplace le fait (elle a `eau` en dur). Même esprit
  que la foreuse et sa route (14.07). Volontairement **indépendant du mode COURANT** : changer de fluide
  ne doit jamais couper en deux un tuyau qui traversait le bâtiment.
  ⚠ **ÉCART (b) — fiche** : le brief affirmait « ses intrants sont déjà affichés par le rendu standard ».
  **FAUX** — l'`InfoPanel` lit `b.inputs`, absent. Ajout de `coolIO` à côté d'`arcIO`
  (`bIn = arcIO ? … : coolIO ? coolIO.inputs : b.inputs`), sinon la ligne « Entrées » resterait vide.
  Le bloc diagnostic `b.tour` est partagé : `capA` lit désormais l'absorption du FLUIDE et la ligne
  « Eau X % · N eau/s » est **masquée** pour un refroidisseur (elle lit `waterNeed`/`waterDrawn`, jamais
  renseignés pour lui).
  État `bld.cool = { sel }` : posé à la pose / densification / réparation, sérialisé en **`pl.cool`**
  (la chaîne seule), restauré avec repli **`sec`** (save antérieure ou valeur inconnue).
  (4) **CRYOSTAT** (nœud 41, groupe **Quantique** juste après le Séparateur, île 6, 8192 kW **FIXES**
  sans sigmoïde) : liquéfacteur d'hélium, cycle de Claude réel — compression + **pré-refroidissement à
  l'azote** — donc **1024 azote/s** en plus des 0,25 He4/s, et conversion **1:1** (changement d'état,
  pas une transmutation) → 0,25 He liquide/s. Coût = 1000 hélium (la charge initiale de la boucle
  fermée) + alliage/supra/polymère/ordi quantique. **Émetteur de chaleur PLAT 2,048 MJ/s** au Nv.1.
  ⚠ `label: 'LIQ'` (le brief proposait `CRYO2` avec repli `LIQ` ; `CRYO` est pris par le Séparateur).
  (5) **DATA CENTER** : recette réduite à **1024 azote/s** (processeur et hélium RETIRÉS) et il devient
  **émetteur de chaleur PLAT 1,024 MJ/s**. `allOrNothing` et `maxPerIsland: 1` CONSERVÉS (seule la
  justification du commentaire change). ⚠ **Effet de bord ASSUMÉ, sans garde-fou ni migration (demande
  explicite du brief)** : sur une save existante, un Data Center sans conduit accumule et passe en
  **endommagé au bout de 60 s** (réparation 20 %) — le joueur devra le raccorder. ⚠ **Second effet de
  bord, non listé par le brief** : n'ayant plus d'intrant porté par la ROUTE, il **ne s'y raccorde
  plus** (`buildingConnectsCarrier`) — il lui faut désormais tuyau + câble + conduit.
  ⚠ **ANCRE INTROUVABLE (la seule)** : le brief §4.5 donnait
  `const effInputs = arcEff ? arcEff.inputs : b.inputs;` — le 14.32 a intercalé `enrEff`. La chaîne
  réelle devient `arcEff → enrEff → coolEff → b.inputs`.
  (6) **USINE MOTEUR QUANTIQUE** : `azote: 8192` → **`helium_liquide: 0.5`** (équivalence du
  Refroidisseur, 1024 azote ↔ 0,0625 He liquide). Aucune migration (la save ne stocke qu'id + niveau).
  ⚠ **À SIGNALER, non corrigé (arbitrage d'équilibrage)** : 1 Usine V1 = 0,5 He liq/s → **2 Cryostats**
  → 0,5 He4/s → **5 Séparateurs Cryogéniques** → 5 Extracteurs, plus 2048 azote/s. Chaîne amont lourde,
  assumée pour de l'endgame, à rééquilibrer après playtest.
  (7) ⚠ **ÉCART (c) — `heatEmitMaxOf` ÉTENDU** (le brief est antérieur au 14.30 lot A, qui a rendu le
  plafond de chaleur FIXE) : sans y déclarer l'émission PLATE du Cryostat et du Data Center, `heatCapOf`
  serait retombé sur `HEAT_PER_MW × conso nominale` → plafond **deux fois trop bas** pour le Cryostat
  (1,024 au lieu de 2,048) et il tripperait en 30 s au lieu de 60.
  Validé : `node --check` (7 blocs) + Chromium **5 suites, 120 assertions, 0 KO, 0 erreur JS**.
  Moteur RÉEL : les 4 fluides mesurés un par un (conso EXACTE sur le réseau **et** MJ/s réellement
  retirés d'une source saturée), mode **sec sans aucun tuyau** (régime 1, absorption 0,5, aucun motif
  « pipe »), fluide à moitié → **régime 0,5 et absorption 2** sans écraser `pwrAvg`, Nv.2 exactement
  doublé, chaleur réellement évacuée d'une source du conduit, **tour aéroréfrigérante non régressée**
  (256 eau/s hors boucle, `waterFrom = 'port'`, 1,024 MJ/s) ; Cryostat 1024 azote + 0,25 He4 → 0,25 He
  liquide, **8192 kW constants dès le 1ᵉʳ tick** (`min = max`, pas de sigmoïde), 2,048 MJ/s, plafond
  2,048 × 60, **surchauffe au tick 60 sans conduit** ; Data Center 1024 azote, **0 hélium 0 processeur**,
  1,024 MJ/s ; Usine MQ sans He liquide → régime 0, puis 0,5 He liq/s → 0,1 moteur/s ; transit **6→5
  des deux hélium**, azote toujours refusé. UI RÉELLE (tap canvas) : 5 boutons de fluide, mode courant
  surligné, **clic réel « Azote » → `cool.sel` + absorption qui passe de 2 à 4 MJ/s**, ligne « Eau … »
  bien masquée, « Entrées eau 256/s » affichée. **Rechargement RÉEL** : fluide conservé, fluide inconnu
  → « sec ». **Save du build 317 rechargée en 318** : 0 perte (niveaux, stocks, conduit), 0 `tickErrors`,
  0 endommagé, horloge qui avance. **Détecteur d'atteignabilité de l'arbre** (14.27) rejoué : 0 nœud
  inatteignable.
  ⚠ **PIÈGES DE HARNAIS (coûteux)** : (a) `bld.heatAbsorb` est ce qui a été **réellement** absorbé, pas
  la capacité — sans source chaude sur le conduit il vaut 0 ; mesurer la capacité par la **baisse de
  `heat` d'une source saturée**, conduit passé en `unlimited` ; (b) l'électricité circule PAR COMPOSANTE
  câble : deux tuiles de câble non contiguës = deux réseaux, et un bâtiment qui se raccorde au câble les
  **ponte** — un refroidisseur retiré coupe donc l'alimentation de son voisin ; (c) sur l'**île 6**,
  `processCollider` réécrit `collider.state` en tête de `tickIsland` → un Data Center y reste
  **`dcIdle`** (régime 0) quoi qu'on force : le tester sur l'île 1 ; (d) sans `unlimited`, le tuyau V1
  (64/s) bride les 1024 azote/s et on mesure le plafond du réseau ; (e) le tap canvas est **avalé tant
  qu'une astuce est ouverte** (`.tip-popup` + `.research-backdrop`) → les fermer en boucle avant ;
  (f) `innerText` des fiches est en MAJUSCULES (CSS) → tester les libellés sans casse.
  ⚠ **HORS PÉRIMÈTRE, non traité (demandé par le brief §8)** : aucun sprite touché — `bat_refroidisseur`
  et `bat_cryostat` **dormaient déjà depuis B1** et sont donc actifs immédiatement ; l'icône
  `item_helium_liquide` **n'existe pas** (repli null-safe, à livrer). `SAVE_VERSION` non incrémenté,
  Refroidisseur volontairement hors de `COST_SOFTCAP_X2` et hors du groupe `nuclear`.
  ⚠ **Taille : 2 885 563 → 2 900 283 o (+14 720 o).**
- **État précédent : `GAME_BUILD = 317`, `GAME_VERSION = 'Alpha 14.35'`, `SAVE_VERSION = 31`.**
  Changement 14.35 (**LOT B3** du brief `B3_brief`) : **landmark du Collisionneur ANIMÉ + anneaux de
  coût de la foreuse.** **AUCUN asset ajouté** — tout l'art dormait depuis B1, ce lot le réveille.
  `SAVE_VERSION` INCHANGÉ.
  (1) **`drawAnimFrame` gagne un 7ᵉ paramètre OPTIONNEL `sub` = `{x, y, w, h}`** : prélève une
  SOUS-RÉGION de la frame courante (repère de la FRAME, le décalage `i * a.fw` est ajouté par la
  fonction). C'est **le seul point du lot qui touche une primitive partagée** (~40 appels) — la
  branche `else` est identique à l'existant, donc les appels sans `sub` sont strictement inchangés.
  ⚠ **À annuler EN PREMIER si quoi que ce soit régresse ailleurs dans le rendu.**
  ⚠ **NE PAS CONFONDRE avec `drawTileAnim`** (juste en dessous) : son `ctx.drawImage` est presque
  identique mais finit par **`x, y, w, w`** (et non `w, h`) — il ne doit pas être touché (vérifié
  intact après coup).
  (2) **LANDMARK ANIMÉ** : les sheets `bat_collisionneur_pN_boot|_actif` couvrent le bloc ENTIER
  (96×64) alors que le rendu du terrain avance **tuile par tuile** → chaque tuile prélève sa tranche
  32×32 via `{x: dc * 32, y: dr * 32, w: 32, h: 32}` (`dr`/`dc` = nb de tuiles collider contiguës au
  N/O, déjà bornés à 1 et 2 juste au-dessus → la sous-région reste toujours dans les 96×64).
  États : `starting` → `_boot` (seul le petit anneau tourne), `ready`/`running` → `_actif` (les deux
  anneaux), `off` ou non réparé → **aucune animation**, tranche statique du palier.
  ⚠ **Le repli en cascade est VOULU, ne pas le « corriger »** : tant que la sheet (384×64, plus lourde
  que les tranches 32×32) n'est pas décodée, `drawAnimFrame` rend `false` et la tranche statique du
  bon palier s'affiche ; la frame 0 de chaque sheet étant identique au statique correspondant, la
  bascule vers l'animé ne produit **aucun saut visible**.
  (3) **ANNEAUX DE COÛT (île 7)** : le coût de pose et la conso d'une foreuse **doublent à chaque
  cercle** autour de l'élévateur (`drillLayer` = distance de Tchebychev → anneaux CARRÉS). Le sol
  alterne désormais `tile_i7_land` (cercle PAIR) / `tile_i7_land_clair` (cercle IMPAIR) → le palier
  de coût se **lit sur la grille**. Le cercle 0 (tuile élévateur) est normal, le 1 clair, le 2 — la
  référence de coût ×1 — normal. `elevatorTileOf` est mémoïsé (`_elevTileCache`) → appel gratuit par
  tuile. Repli : sprite absent → sol normal.
  Validé : `node --check` (7 blocs) + Chromium **4 suites, 21 assertions, 0 KO, 0 erreur JS**, suite
  rejouée **3 fois sans flottement**. Méthode : espion sur `drawImage` enregistrant **la SOURCE en
  plus de la destination** (c'est la source qui prouve la découpe). **Non-régression (le point
  sensible du lot)** : sur **27 sheets et 1 758 appels** capturés (écume côtière, falaises, brise,
  bâtiments animés), **100 % des appels SANS `sub` découpent toujours la frame ENTIÈRE**
  (`sy=0, sw=fw, sh=fh`, `sx` multiple de `fw`) — 0 anomalie. Collisionneur : les 5 états parcourus
  (non réparé → ruine statique ; `starting` → `_boot` ; `ready` ET `running` → `_actif` ; `off` →
  retour aux tranches **du palier**, pas la ruine) ; les **6 tranches sont distinctes et exactement
  (0,0) (0,32) (32,0) (32,32) (64,0) (64,32)** = 3 colonnes × 2 lignes dans 96×64, toutes en 32×32,
  et les 6 tuiles utilisent **la MÊME frame** (pas de déphasage, contrairement aux tuiles de brise).
  Île 7 : les deux teintes sont dessinées (8 claires / 5 normales), élévateur au cercle 0.
  Contrôle visuel : landmark rendu comme **un bloc cohérent sans couture** entre tuiles ; anneaux
  carrés bien visibles et alternés autour de l'élévateur.
  ⚠ **PIÈGE DE HARNAIS MAJEUR (m'a coûté un faux « bug »)** : la partie de démarrage est en mode
  **« difficile »** et le modal de choix est ENCORE ouvert → le terrain `collider` n'existe pas
  (0 tuile). **Ne PAS appeler `applyGameMode('normal')` à la main** : ça repeuple les defs de TOUTES
  les îles sans reconstruire les grilles déjà créées → `tutCount` lit une ligne inexistante et **la
  boucle de rendu lève à chaque frame** (`Archipel frame error: Cannot read properties of undefined
  (reading 'building')`, canvas à 0 %). Passer par le VRAI chemin : **cliquer la carte « Normal » du
  `.mode-modal`** (`chooseMode`), qui reconstruit tout de façon cohérente (vérifié : mode normal,
  6 tuiles collider, 0 erreur).
  ⚠ Autre piège : après des dizaines de redraws forcés, un échantillon `getImageData` peut tomber
  entre le `clearRect` et le repaint → **laisser reposer le rendu et réessayer** avant de conclure
  qu'un canvas est vide (le contrôle visuel donnait 100 % au même instant).
  ⚠ **Taille : 2 883 349 → 2 885 563 o (+2 214 o).** Le brief annonçait +2 217 : l'écart de 3 octets
  vient uniquement du libellé du commentaire de version, rédigé différemment. **Les tailles du brief
  B3 sont enfin en OCTETS** (`os.path.getsize`) et concordent avec le dépôt — celles des briefs
  précédents étaient comptées en caractères, d'où l'écart relevé en B2.
  ⚠ **HORS PÉRIMÈTRE, non traité** : `refroidisseur`/`cryostat` dans `BUILDINGS` (specs attendues),
  `logic_emetteur` qui résout `null` (préexistant, inerte), et la pointe du foret masquée par un
  bâtiment au sud/est (limite documentée en B2).
- **État précédent : `GAME_BUILD = 316`, `GAME_VERSION = 'Alpha 14.34'`, `SAVE_VERSION = 31`.**
  Changement 14.34 (**LOT B2** du brief `B2_brief` — pack `archipel_textures_v3.2`, complément) :
  **foreuse « 1 case ½ » + sélecteur d'émetteur par COMBINAISON de bits.** `SAVE_VERSION` INCHANGÉ
  (aucun champ persisté ne bouge ; `drillDir` = `pl.dd` existe depuis 14.03).
  (1) **PURGE de 12 lignes** (les 5 clés de `keys_to_purge_b2.txt`) : `bat_foreuse` était définie
  **2× dans `SPRITE_DATA` ET 2× dans `ANIM_DATA`** (overrides empilés), les 4 orientations 1× chacune.
  ⚠ **L'ORDRE COMPTE** : purger d'abord, insérer ensuite. Puis **INSERTION** de `patch_foreuse.js`
  avant l'ancre `// Indexé par CLÉ DE SPRITE STATIQUE` : 5 sprites + 4 sheets + 4 `ANIM_META`.
  ⚠ **`bat_foreuse` perd son animation et n'en récupère pas** : ce n'est plus qu'une **icône de menu
  statique 32×32** (c'est voulu — le menu ne peut pas afficher un sprite débordant). Son entrée
  `ANIM_META` résiduelle devient **inerte** : `ANIM_BY_SK` n'indexe que ce qui existe dans
  `ANIM_DATA` (vérifié en lisant la boucle : `for (const cle in ANIM_DATA)`), donc pas de plantage.
  ⚠ Les `ANIM_META` des 4 orientations ont des **`fw`/`fh` NON CARRÉS** (32×48 en n/s, 48×32 en o/e) —
  `drawAnimFrame` le gère déjà (il découpe sur `a.fw`/`a.fh` et étire dans le rectangle destination).
  (2) **FOREUSE « 1 case ½ »** (`drawBuilding`) : seul le **rectangle de DESSIN** est étendu d'une
  DEMI-case dans le sens de forage (`fx/fy/fw/fh`), **l'emprise logique reste 1×1**. Les appels
  `drawDeficitIcon` / `drawInfoBadges` qui suivent restent sur `(x, y, W, H)` — sinon l'icône de
  déficit irait se coller au bout du foret. Repli automatique : si les clés `bat_foreuse_<dir>`
  manquent, `buildingSpriteKey` retombe sur `bat_foreuse` (32×32) qui n'entre dans aucun des 4 cas.
  ⚠ **LIMITE CONNUE, VOLONTAIREMENT NON CORRIGÉE** : le terrain est peint en passe complète AVANT les
  bâtiments (le foret passe donc proprement sur le sol voisin), mais un **bâtiment** situé au SUD ou à
  l'EST est dessiné APRÈS et **recouvrira la pointe**. En pratique la foreuse creuse vers de la roche.
  Le correctif propre serait une passe dédiée après les bâtiments — hors périmètre.
  (3) **ÉMETTEUR : un sprite par COMBINAISON de bits.** Avant, seul le signal du palier COURANT était
  lisible (à P3 le joueur voyait γ mais ni α ni β, alors que le code émis dépend des trois). Désormais
  `codeE` = les `palE` premiers bits de `bitsE` (= `[α, β, γ, VALIDE]`, ordre des faces N, S, O) →
  clé `logic_emetteur[_dc]_p<palE>_<bits>`. Variante **`_dc`** (liseré violet) quand le porteur est le
  **Data Center** (`tiles[r][c].building.id === 'data_center'`) — l'émetteur du Collisionneur est sur
  du terrain `collider`, donc sans bâtiment. Les 28 clés ont été **injectées en B1** et dormaient.
  ⚠ `sig`/`lit` restent déclarés au-dessus : ils ne servent plus qu'au REPLI, ne pas les supprimer.
  ⚠ Le comportement **muet est inchangé** : face VALIDE à 0 → `logic_emetteur_inactif`, en amont du
  nouveau code. Mapping des faces inchangé (**α nord, β sud, γ ouest, VALIDE est**).
  Validé : `node --check` (7 blocs) + Chromium **3 suites, 26 assertions, 0 KO, 0 erreur JS**, suite
  rejouée **5 fois de suite sans flottement**. Méthode : **espion sur `CanvasRenderingContext2D.
  prototype.drawImage`** + reverse-map `dataURL → clé` → on mesure le rectangle de dessin RÉEL du vrai
  `draw()`. Mesures : tuile 26 px → **NORD/SUD 26×39, OUEST/EST 39×26** (26 + 13 = 1 case ½) ; NORD
  démarre 13 px plus HAUT que SUD et OUEST 13 px plus à GAUCHE qu'EST (le débord part du bon côté) ;
  SUD et EST partent du coin de la case ; le carter reste cadré sur la case dans l'axe non forant ;
  **icône de déficit ancrée sur la CASE** (y=175 ≥ haut de case 174), pas sur la pointe ; animation
  qui tourne en marche (sheet) et **figée à l'arrêt** (statique) ; **les 8 combinaisons P3 donnent
  chacune LEUR sprite** (table complète 000→111), variante `_dc` sur le Data Center, P1 sur l'île 6 ;
  les 111 bâtiments résolvent vers un sprite présent.
  ⚠ **PIÈGES DE HARNAIS (coûteux, à ne pas redécouvrir)** : (a) les sprites se décodent
  **PARESSEUSEMENT** → tant qu'une clé n'est pas décodée `drawSprite` rend `false` et le draw tombe
  sur le **candidat suivant** : on mesure alors le mauvais sprite (symptôme observé : `p3_100` demandé,
  `p3_000` dessiné). Préchauffer via `__heat.spriteImg` + `spriteUsable` AVANT toute mesure ;
  (b) la boucle rAF ne redessine QUE si `g.dirty` → une fenêtre de capture d'UNE frame tombe parfois
  sur un rendu déjà consommé : **réessayer jusqu'à observer un dessin** ; (c) sur l'**île 6**,
  `syncColliderChildren` **SUPPRIME tout enfant d'émetteur hors position attendue** (même passe de
  nettoyage qu'en 14.07 pour la vanne mal placée) → une forge d'émetteur y est effacée entre deux
  frames, il faut la **rejouer avant chaque tentative** ; (d) `processLogic` réécrit `emitBits` à
  chaque tick → le poser en **getter** (`Object.defineProperty`) pour qu'il survive à la mesure ;
  (e) la taille de tuile est **`g.cam.tile`** (il n'existe pas de `g.tile`).
  ⚠ **HORS PÉRIMÈTRE, reporté en B3** : animation du landmark collisionneur (les sheets `_boot`/
  `_actif` 96×64 supposent d'étendre `drawSprite`/`drawAnimFrame` avec une découpe de SOURCE + un
  sélecteur d'état lisant `game.collider` — ça touche des primitives partagées par tout le rendu),
  anneaux de coût de la foreuse (`tile_i7_land_clair`, même passe terrain), `refroidisseur`/`cryostat`
  dans `BUILDINGS` (en attente de specs), et `logic_emetteur` qui résout `null` via `buildingSpriteKey`
  (préexistant et inerte — une ligne `logic_emetteur: ['logic_emetteur_inactif']` le fermerait).
  ⚠ **Taille : 2 886 069 → 2 883 349 o (−2 720 o = −2,7 Ko)**, exactement le delta annoncé par le brief.
  (Les tailles ABSOLUES du brief — 2 860 849 → 2 858 083 — ne correspondent pas au dépôt réel : c'est
  le DELTA qui fait foi.)
- **État précédent : `GAME_BUILD = 315`, `GAME_VERSION = 'Alpha 14.33'`, `SAVE_VERSION = 31`.**
  Changement 14.33 (**LOT B1** du brief `B1_brief` — pack `archipel_textures_v3.2`) : **injection du
  pack sprites « île6 v3.2 ». ASSETS SEULS — aucune ligne de logique de rendu touchée.**
  `SAVE_VERSION` INCHANGÉ (aucune structure de sauvegarde n'est concernée).
  (1) **PURGE de 98 lignes** : les 101 clés de `keys_to_purge.txt` (toutes occurrences dans
  `__SPRITE_DATA__` ET `__ANIM_DATA__`) **plus 24 lignes orphelines** `logic_bloc_alpha[_beta]
  [_gamma]_<dir>_<0|1>` — **zéro référence dans le code** (vérifié : `grep -c 'logic_bloc'` sur le
  fichier filtré → 0) ; elles faisaient doublon avec `logic_emetteur_*`, ce que le rendu utilise
  réellement. ⚠ **L'ORDRE COMPTE** : purger d'abord, insérer ensuite (sinon la purge effacerait les
  lignes qu'on vient d'ajouter). 74 lignes pour 61 clés car 4 clés étaient **déjà définies
  plusieurs fois** (overrides empilés par des packs successifs : `bat_collisionneur_p1`/`_p2` ×3,
  `bat_collisionneur` ×2, `bat_fab_ordi_quantique` ×2 côté `ANIM_DATA`) → la purge élimine toutes
  les occurrences, le patch redéfinit une seule fois. `bat_refroidisseur` vit dans le **grand
  littéral** (~ligne 1657) et non en affectation individuelle : il n'est pas purgé, la
  **réaffectation tardive** du patch le couvre (la dernière affectation gagne au runtime).
  (2) **INSERTION** de `patch_assets.js` juste AVANT l'ancre `// Indexé par CLÉ DE SPRITE STATIQUE`
  (donc après le dernier `Object.assign(ANIM_META, …)` existant et avant `const ANIM_BY_SK`) :
  **96 sprites + 15 spritesheets + 15 entrées `ANIM_META`**. ⚠ Vérifié que `ANIM_META` est bien
  **déclaré avant** (ligne ~3250) — sinon l'`Object.assign` du patch lèverait un `ReferenceError`.
  Les 3 `bat_collisionneur_pN_boot` **passent de `fw:64,fh:96` à `fw:96,fh:64`** (l'art portrait
  tourné devient nativement PAYSAGE) et les 3 `_actif` sont ajoutés : c'est une **réécriture**, pas
  un doublon — l'ancien `Object.assign` reste en place et le patch, qui s'exécute après, écrase.
  (3) ⚠ **LE §3 DU BRIEF ÉTAIT DÉJÀ FAIT, EN MIEUX — ne pas le rejouer.** Le brief (rédigé sur la
  base 311 / 14.29) demandait de supprimer `centrale_enrichissement_v2: 'bat_broyeur_uranium_v2'`
  de `BLD_SPRITE_OVERRIDE`, faute de quoi le nouvel art « ne s'afficherait jamais ». **Cette ligne
  n'existe plus depuis le 14.32** : elle est devenue une **LISTE de candidats**
  `['bat_centrale_enrichissement_v2', 'bat_broyeur_uranium_v2']` (le premier PRÉSENT gagne). Le
  patch livre précisément `bat_centrale_enrichissement_v2` → il est pris **automatiquement**, et le
  repli reste derrière par sûreté. **Aucune retouche de `BLD_SPRITE_OVERRIDE`** (vérifié en jeu :
  `buildingSpriteKey('centrale_enrichissement_v2')` → `bat_centrale_enrichissement_v2`).
  (4) **HORS PÉRIMÈTRE, volontairement non traité** (arrive en B2) : la **FOREUSE** — ses sprites
  32×48 / 48×32 sont **absents du patch**, le rendu dessinant en `tile × tile` ils seraient écrasés
  dans 32×32 ; les clés `bat_foreuse*` existantes sont **intactes** (vérifié). Sont injectées mais
  **DORMANTES** (aucun code ne les lit aujourd'hui, c'est voulu) : le sélecteur d'émetteur par
  combinaison de bits (`logic_emetteur_p1_*`/`p2_*`/`p3_*` et `logic_emetteur_dc_*` — le cumul par
  palier continue de passer par les clés HISTORIQUES `logic_emetteur_<sig>[_on]`), les sheets
  `_boot`/`_actif` du landmark (le rendu du terrain `collider` n'appelle que `drawSprite` sur les
  tranches), et `tile_i7_land_clair` (anneaux de coût de la foreuse). Ni `refroidisseur` ni
  `cryostat` ne sont créés dans `BUILDINGS` : leur art est injecté, les bâtiments n'existent pas.
  ⚠ **`logic_emetteur` (le bâtiment) résout sur `null` via `buildingSpriteKey` — c'est PRÉEXISTANT
  et INERTE**, pas une régression : contre-épreuve exécutée sur le build 314 d'origine → même
  résultat exact (1 seul id sans sprite, le même). Il est `childOnly` (jamais posable, jamais au
  menu) et se dessine par sa PROPRE chaîne de candidats (`logic_emetteur_<sig>[_on]` →
  `logic_emetteur_inactif` → `logic_emetteur_alpha`), toutes présentes → aucun carré de repli.
  Validé : `node --check` (7 blocs) + Chromium **6 suites, 32 assertions, 0 KO, 0 erreur JS** — les
  **101 clés purgées sont TOUTES redéfinies** (0 manquante, comparaison ensembliste purge ↔ patch :
  0 clé orpheline des deux côtés) et **décodent réellement** en `Image` (0 échec) ; 0 clé
  `logic_bloc_*` résiduelle ; **aucune clé du pack définie 2× dans le MÊME conteneur** (⚠ ne PAS
  tester les doublons globalement : le fichier en contient une cinquantaine d'origine
  — `logic_porte_*`, `i7_bord_*`, `bat_foreuse`… — qui sont des overrides INTENTIONNELS de packs
  successifs, et une clé peut légitimement exister à la fois en statique et en sheet) ;
  `bat_collisionneur_p1` statique mesuré **96×64 PAYSAGE**, sheet 384×64, les 6 entrées `ANIM_META`
  du collisionneur en **96×64** (boot @6 fps, actif @10) ; **balayage des 111 bâtiments** → tous
  résolvent vers un sprite PRÉSENT (hors le `logic_emetteur` préexistant ci-dessus) ; les 7 cibles
  nommées du brief pointent bien sur l'art dédié v3.2 ; boot réel (canvas peint 100 %, splash
  retiré, 0 erreur console). **Contrôle visuel** (planche contact rendue) : collisionneur paysage
  halls en haut à gauche / grand anneau à droite / petit anneau en bas à gauche, et l'aspect change
  bien à chaque palier (P0 rouillé → P1 → P2 → P3 argenté) ; émetteur en **rose des vents**
  (α nord, β sud, γ ouest, VALIDE est — le quartier correspondant s'allume en `_on`) ; actionneur
  **sans flèche**, voyant carré centré, identique dans les 4 sens ; fours V2 / centrifugeuse
  (rotor + trémie) / refroidisseur / cryostat ; famille quantique + antenne V2 en argenté V4 ;
  icône `item_moteur_quantique` 16×16.
  ⚠ **Taille du fichier : 2 934 056 → 2 885 788 o (−47,1 Ko).** Un delta POSITIF signifierait que
  la purge a échoué. (Le brief annonçait −47,6 Ko, mesurés sur la base 311 : l'écart vient de la
  base différente, la purge ayant retiré exactement les 98 lignes attendues.)
  ⚠ **`index.html` / `sw.js` / `version.json` NE SONT PAS à éditer à la main** : `index.html` est
  l'**édition TESTEUR** (copie du jeu avec `TESTER_BUILD = true`, seule ligne qui diffère) et la CI
  les régénère tous les trois depuis `Archipel_industry_alpha-7.html` après un build sur `main`
  (étapes « Sync PWA » et « Sync version.json »).
- **État précédent : `GAME_BUILD = 314`, `GAME_VERSION = 'Alpha 14.32'`, `SAVE_VERSION = 31`.**
  Changement 14.32 (**LOT C** du brief `BRIEF_LOT_C_enrichissement`) : **renommage Centrale
  Enrichissement V2 (+ son art enfin branché), mélange uranium/plutonium réglable, et mix irradié
  en ± 1 % à somme verrouillée.** `SAVE_VERSION` INCHANGÉ (`pl.enr` = champ additif ; les poids du
  mix sont renormalisés au chargement, sans changement de format).
  (1) **C1 — « Centrifugeuse Uranium V2 » redevient « Centrale Enrichissement V2 »** (graphie
  alignée sur le V1, `Centrale Enrichissement V1`). Seul le NOM change : l'id
  `centrale_enrichissement_v2`, la recette, `TIER_NEXT`/`TIER_STEP` sont intacts, et la migration
  d'ids **`broyeur_uranium_v2` / `centrifugeuse_uranium` → `broyeur_uranium` est CONSERVÉE** (ce
  sont des builds PUBLIÉS 308/310 — vérifié par rechargement réel). Il ne reste du mot
  « centrifugeuse » que 3 commentaires d'historique et **la chaîne de l'id dans la migration**.
  ⚠ **DÉCOUVERTE — l'art dédié existait déjà.** `BLD_SPRITE_OVERRIDE` accepte désormais une **LISTE
  de candidats** (1er présent gagne, même esprit que `cands` dans `buildingSpriteKey`) : on a mis
  `['bat_centrale_enrichissement_v2', 'bat_broyeur_uranium_v2']` pour qu'Ethan n'ait qu'à déposer
  son PNG… **et le sprite est DÉJÀ dans le pack depuis 13.59** (il y avait été inliné comme sprite
  « V2 île 6 » inerte et jamais branché ; le 14.29 pointait le bâtiment sur l'art du Broyeur V2).
  Il est donc actif immédiatement, **avec sa sheet d'animation 4 frames**, et il est bien DISTINCT
  du repli. Un nouvel art se substituera sous la MÊME clé, sans retoucher au code.
  (2) **C2 — MÉLANGE URANIUM / PLUTONIUM réglable PAR BÂTIMENT** (mécanique sœur du four à arc :
  `ENR_TUNABLE` / `enrDefaultState` / `enrEffective` / `enrClampU`, posés à côté d'`arcDefaultState`).
  Curseur `u` entier de 10 à 90, défaut 50 ; `s = (u−50)/40`, **uranium ×4^s**, **plutonium ×4^(−s)**,
  acier et sortie INCHANGÉS. ⚠ **`u` n'est PAS une fraction massique** : c'est un curseur de
  COMPROMIS (le tooltip le dit explicitement) — plus d'uranium = moins de plutonium, à production
  de combustible CONSTANTE. Invariants vérifiés : **uranium × plutonium constant** (1,074e8 au
  Nv.13, écart max 1,4e-16 sur tout le balayage) et rapport U/Pu = **10 240 × 16^s**. Seuls les
  `inputs` passent par `enrEffective` ; `effOutputs` et la sigmoïde ne sont pas touchés.
  Sérialisation : **`pl.enr` = un ENTIER** (pas un objet) ; absent → 50, clampé à [10, 90] — une
  save antérieure reproduit donc EXACTEMENT la recette d'origine (4^0 = 1). UI : boutons ± 1 %
  (`.ip-nuc-pm`), jauge `u / 100−u`, **`NumField`** pour la saisie directe (80 appuis seraient
  intenables au doigt) et les débits réels au niveau courant.
  ⚠ **Piège de mesure** : à un stock de port de 1e14 l'ULP du float64 vaut 0,015625 → les deltas de
  plutonium (0,00625/s) sont purement absorbés par l'arrondi. Mesurer sur un stock modéré.
  (3) **C3 — le mix irradié passe en VRAIS POURCENTAGES, somme verrouillée à 100.** C'étaient des
  **poids libres** normalisés par `wsum` à l'usage (la save du joueur portait 105 et 120 de somme),
  réglés par un slider au pas de 5 — imprécis au doigt et sans signification lisible.
  `setNucMixWeight` → **`setNucMixDelta(islandId, mat, ±1)`** : **+1 % PREND 1 % au plus gros des
  autres**, −1 % le lui REND ; `+` désactivé quand tous les autres sont à 0, `−` à 0 ; égalité
  départagée par l'ordre de `NUC_MAT_KEYS` (déterministe). La prop est renommée partout
  (`grep -c onSetNucMixWeight` → **0**). **Migration au chargement** : renormalisation des 4 poids à
  une somme de 100 par la **méthode du plus grand reste** (un simple arrondi donnerait 99 ou 101),
  somme nulle → 25/25/25/25, appliquée **quel que soit le mode** (les poids sont stockés même en
  single/auto). Sur la save du joueur : île 3 `{55,0,50,0}` → **`{52,0,48,0}`**, île 5
  `{0,100,0,20}` → **`{0,83,0,17}`**, île 4 `{25,25,25,0}` → `{34,33,33,0}` — toutes à 100.
  ⚠ **Le FOUR À ARC partage la classe CSS `ip-nuc-mix`** et garde ses sliders : le seul site touché
  est celui qui appelait `onSetNucMixWeight` (vérifié — les 2 `input[type=range]` restants sont
  l'arc et le slider générique des Options).
  ⚠ **`__heat` étendu** (`ENR_TUNABLE`, `enrEffective`, `enrDefaultState`, `enrClampU`, bornes,
  `nucMix`, `NUC_MAT_KEYS`, `BLD_SPRITE_OVERRIDE`, `upgradeMult`).
  i18n en/es/de des 9 nouveaux libellés (bloc d'augmentation `/* 14.32 */`).
  Validé : `node --check` (7 blocs) + Chromium **3 suites, 40 assertions, 0 KO, 0 erreur JS** — les
  6 valeurs du tableau de référence retrouvées aux 3 points (2,62e5/409,6 · 1,05e6/102,4 ·
  4,19e6/25,6, rapports 640/10 240/163 840) ; **moteur RÉEL** : 2 Centrales d'Enrichissement V2 sur
  la MÊME île réglées à 10 et 90 → uranium 1088/s et plutonium 0,10625/s = somme exacte des deux
  recettes, **acier et combustible identiques** (0,5/s chacun) ; somme du mix exactement 100 après
  50 appuis mélangés ; **rechargements RÉELS** : les 2 ids historiques toujours rabattus, réglages
  relus `[50, 77, 90]` (absent → 50, 999 → 90) et bien sérialisés ; save du joueur : 7 îles, aucun
  NaN, 0 endommagé après 70 s (lots A/B intacts).
- **État précédent : `GAME_BUILD = 313`, `GAME_VERSION = 'Alpha 14.31'`, `SAVE_VERSION = 31`.**
  Changement 14.31 (**LOT B** du brief `BRIEF_LOT_B_lisibilite_ui`, **B6 tranché sur l'OPTION 2**) :
  **lisibilité — émission de chaleur max en fiche, charge crête du conduit, réseaux nécessaires,
  bâtiments des autres îles en gris, annonce de densification, et le Collisionneur exige enfin un
  vrai câble.** `SAVE_VERSION` INCHANGÉ (`uiPrefs.showOffIsland` = champ additif, absent = défaut).
  (1) **B1 — ligne « Émission max » en fiche** (`fmtHeat(heatEmitMaxOf(bld))`, source de vérité du
  lot A, RIEN n'est recalculé) : sur un bâtiment dont la conso OSCILLE, l'émission instantanée ne
  dit rien du dimensionnement des tours. Affichée si `sigmoid || randomP || antenna || nuclear`.
  ⚠ **L'exemple du brief est FAUX** : l'Usine Moteur Nucléaire n'est PAS « à conso plate » — elle a
  une sigmoïde `{64, 448, 60}` depuis 13.43 (seule son ÉMISSION est plate, 1,024 × niveau) → la
  ligne s'y affiche. Le SEUL bâtiment à chaleur réellement à conso plate est **`machine_outil`**,
  et lui est bien exclu — c'est exactement l'intention. Tooltip du plafond corrigé (« plafond FIXE
  = 1 min d'émission MAXIMALE », il ne dépend plus du régime courant depuis 14.30).
  (2) **B2 — « Charge crête » du réseau conduit** : nouveau registre `game.conduitPeak[isl][nid]`
  posé par `processHeat` (somme des `heatEmitMaxOf` des sources raccordées), affiché à côté du flux
  et **ROUGE dès que la crête dépasse le débit** — le signal arrive AVANT la panne, alors que le
  flux instantané peut rester bas tant que les sigmoïdes ne sont pas en phase. ⚠ Vérifié comme le
  demandait le brief : `cn.sources` ne contient QUE des `bld` réels (le pseudo-élément
  `{ elevator: true }` est ajouté à `srcs`, la copie) → aucune garde nécessaire.
  (3) **B3 — ligne « Réseaux » en fiche** (`buildingConnectsCarrier` sur road/pipe/wire/conduit,
  complète depuis 14.30). ⚠ **Aucune table de libellés créée** : les noms viennent des defs d'infra
  elles-mêmes (`BUILDINGS.road/pipe/wire/conduit.name`), déjà traduites par `applyToData`.
  **Libellés d'île** : `'île ' + b.exclusiveIsland` → **`islandLabel(...)`** (« Île 6 S » pour l'id 7).
  Balayage exhaustif : **UNE SEULE** composition manuelle existait (la ligne `Exclusif`) ; les 2
  autres occurrences sont `islandLabel` elle-même et la phrase « pic des sigmoïdes de l'île » (pas
  une étiquette). Le toast de refus de pose utilisait déjà `islandLabel`.
  (4) **B4 — bâtiments d'une AUTRE île affichés en GRIS** (défaut ACTIVÉ, découvrabilité) : fonction
  SŒUR **`offIslandOn(id)`** — `visibleOn` reste booléenne, **aucun de ses appelants n'est touché**.
  ⚠ Seule l'EXCLUSIVITÉ change de traitement : `forbiddenIslands` et le non-débloqué restent
  MASQUÉS. Classe `.tool-btn.off-island` (opacité + grayscale, **PAS de `pointer-events:none`**) ;
  un tap ouvre la **FICHE** et `selectTool` refuse la sélection avec le message EXISTANT du mode
  Copier (aucun second texte). Les grisés participent à la RECHERCHE (« tungst » depuis l'île 2
  remonte la Mine Tungstène). Interrupteur en bas du panneau Bâtiment réutilisant le style
  d'interrupteur du jeu (**`.opt-toggle`** des Options — aucun composant neuf), masqué en couche
  logique et absent de l'onglet Réseau. Persisté dans `uiPrefs.showOffIsland`.
  (5) **B5 — ligne « Densification »** (`TIER_NEXT` + `TIER_STEP[next].entry`) : rien n'annonçait
  qu'un bâtiment allait devenir autre chose avant d'atteindre le palier. C'est aussi la réponse au
  « où est l'Accumulateur V2 ? » — comme **18 autres cibles**, il ne se pose pas, il s'obtient en
  améliorant. ⚠ **Le brief annonce « au Nv.10 », c'est un décalage d'UN** : `entry` est l'index
  d'amélioration 0-based et le jeu affiche partout `upgrade + 1` (cf. 13.27, « u=10 (Nv.11) ») →
  on affiche **Nv.11**. `TIER_STEP.entry` est présent pour TOUTES les cibles : le repli `cap + 1`
  n'est jamais utilisé (vérifié).
  (6) **B6 — OPTION 2 RETENUE (arbitrage joueur) : le Collisionneur EXIGE un réseau CÂBLE adjacent.**
  Avant, sa demande était prélevée sur le bilan de l'ÎLE 6 entière : n'importe quelle source de
  l'île l'alimentait, câblée ou non → dessiner un raccord aurait été un **mensonge visuel**. Nouveau
  **`colliderWireNid(game, isl)`** (adjacence à `colliderBounds`, comme `colliderDrawHe3` le fait
  pour le tuyau) ; sans câble → `co.powered = false`, plus aucune alimentation. Avec câble, il puise
  sur **SA composante** (`wireInfo[nid].deliver − served`, donc borné par le DÉBIT du câble) et sa
  demande est publiée sur cette composante (`demand`/`served`/`netDemand`) → le panneau du câble
  cesse de sous-estimer la plus grosse charge de la partie. **Stubs de raccord** dessinés sur le
  landmark pour le **câble ET le tuyau** (l'He3 y était déjà réellement puisé depuis 14.17) → le
  visuel devient sincère dans les deux sens. Nouvelle ligne « Câble : relié / non relié » dans sa
  fiche. ⚠ **RUPTURE D'ÉQUILIBRAGE ASSUMÉE** : un Collisionneur qu'aucun câble ne touche perd son
  alimentation (son démarrage recule) tant que le joueur ne l'a pas raccordé. **Aucune migration de
  save n'est nécessaire** (aucun champ persisté ne change ; `wireNid`/`wireOk` sont transitoires) —
  sur la save du joueur le Collisionneur est `off`, donc rien ne casse immédiatement.
  ⚠ **`__heat` étendu** (`islandLabel`, `colliderWireNid`, `colliderBounds`, `TIER_NEXT`, `TIER_STEP`).
  i18n en/es/de des 15 nouveaux libellés (bloc d'augmentation `/* 14.31 */`).
  Validé : `node --check` (7 blocs) + Chromium **3 suites, 45 assertions, 0 KO, 0 erreur JS** —
  émission max × 60 == plafond sur TOUS les bâtiments à chaleur ; charge crête = somme exacte des
  2 sources, **rouge à flux instantané NUL** quand la crête dépasse le débit, 0 (pas de NaN) sans
  source, jamais rouge en illimité ; Presse UHP = route · câble · conduit, éolienne = câble seul,
  `islandLabel(7)` = « Île 6 S » ; **UI RÉELLE** depuis l'île 2 : 26 bâtiments grisés sur 89, clic →
  fiche sans sélection d'outil, recherche « tungst » → 3 résultats grisés, switch éteint → ils
  disparaissent ET le réglage est écrit dans `uiPrefs`, non-régression (non débloqué = masqué) ;
  **save RÉELLE du joueur** (7 îles, 58 types de bâtiments) : aucun NaN/undefined dans les données
  des fiches et des panneaux, 0 endommagé après 70 s, centrale île 5 toujours `regime = 1`, les
  2 centrales en pause toujours en arrêt franc.
  ⚠ **Pièges de harnais (nouveaux)** : la partie de démarrage est en mode **« difficile »** → le
  terrain `collider` n'existe pas (appeler `applyGameMode('normal')` + reconstruire l'île 6) ; le
  navigateur de test est en **locale EN** (forcer `localStorage['archipel_lang'] = 'fr'`) ; sur une
  save ancienne l'**overlay de rattrapage hors-ligne PUIS le récap** interceptent les clics (les
  fermer avant toute interaction) ; le **canvas plein écran fait échouer le hit-testing de
  Playwright** → piloter l'UI en JS (`el.click()`, setter natif + événement `input`) ; l'onglet
  Bâtiment est un **TOGGLE** (re-cliquer le ferme → helper idempotent).
  ⚠ **HORS lot B, non traité** : tout le lot C (renommage Centrifugeuse → Centrale d'Enrichissement,
  ratio uranium/plutonium, boutons ± du mix irradié).
- **État précédent : `GAME_BUILD = 312`, `GAME_VERSION = 'Alpha 14.30'`, `SAVE_VERSION = 31`.**
  Changement 14.30 (**LOT A** du brief `BRIEF_LOT_A_chaleur_nucleaire`) : **plafond de chaleur FIXE
  (fin des trips fantômes) + antenne qui ne chauffe plus à l'arrêt + le conduit traverse les
  bâtiments + la centrale publie ses champs de rendu et s'arrête franchement.** `SAVE_VERSION`
  INCHANGÉ (aucun champ ni format ne change ; le clamp de chargement est purement défensif).
  (1) **A1 — LE PLAFOND DE CHALEUR DEVIENT STRUCTUREL.** C'était la cause du retour joueur « je pose
  des bâtiments, je reviens plus tard, ils sont cassés alors qu'il n'y avait aucun intrant ou qu'ils
  étaient en pause ». `heatCapOf` valait `max(heatEmit, heatEmitPk) × 60` : à l'arrêt `heatEmit` tombe
  à 0 et `heatEmitPk` **décroissait ×0,995/tick (~2 min)** → le plafond passait **SOUS la chaleur déjà
  stockée**. Nouveau **`heatEmitMaxOf(bld)`** = émission MAXIMALE (donc indépendante de l'état
  courant) : centrale → `NUC_POWER_BASE × mult × HEAT_PER_MW / 1000` (V2 `noHeat` → 0), antenne →
  `bld.heatEmitMax` posé par le tick (cf. A2), usine moteur nuc → `1,024 × mult` (plat), tous les
  autres → `HEAT_PER_MW × nominalPower(bld) / 1000` (qStab inclus, ×(1 + antElecBoost) si voisin
  d'antenne). `heatCapOf` = `heatEmitMaxOf × HEAT_CAP_SECONDS`. **`heatEmitPk` SUPPRIMÉ** (3 sites).
  ⚠ **Le vrai déclencheur du trip n'était PAS la reprise à plein régime** (le plafond y revient d'un
  coup) mais la **reprise à régime PARTIEL** (intrants qui reviennent doucement, ou rechargement où
  `heatEmitPk` n'est pas persisté → repli `cap = bld.heat` = jauge pleine d'office) : à 50 % de régime
  le plafond ne valait plus que la moitié → `rising` + `heat ≥ cap` → **trip au premier tick**.
  Contre-épreuve automatisée incluse (l'ancien modèle trippe, le nouveau non).
  ⚠ **Nouvelle constante module `NUC_POWER_BASE = 16384`** : elle DOIT rester alignée sur les deux
  constantes LOCALES `NUC_POWER = 16384` (tick + fiche), qui restent en place. **Clamp au chargement**
  (`loadSave`) : `heat` est borné au nouveau plafond, sinon une save d'avant 14.30 tripe au 1ᵉʳ tick.
  (2) **A2 — l'antenne en PAUSE / éteinte par la LOGIQUE n'émet plus de chaleur.** La ligne qui pose
  `antBld.heatEmit` tourne **APRÈS** la boucle bâtiment (qui avait déjà mis 0) et ne testait que
  `damaged` → elle **écrasait** le 0. Tests `!paused && !logicOff` ajoutés. Nouvel accumulateur
  **`antExtraKwMax`** (parallèle à `antExtraKw`, `nomP × antElecBoost(fac)` = sin_term à 1) →
  `antBld.heatEmitMax`, le plafond fixe de l'antenne (il dépend du VOISINAGE boosté, pas de sa propre
  def). ⚠ `heatEmitMax` est **conservé même à l'arrêt** (sinon la jauge de la fiche deviendrait
  illisible) ; tant qu'il est absent, `heatCapOf` rend 0 → **aucun trip possible**, repli voulu.
  (3) **A3 — LE CONDUIT DE CHALEUR TRAVERSE LES BÂTIMENTS**, comme le câble et le tuyau. Poser une
  source au milieu d'une ligne de conduit la coupait en DEUX réseaux. Trois maillons, dont un
  **manquant** qui rendait les deux autres inopérants : **`buildingConnectsCarrier` n'avait AUCUNE
  branche `conduit`** (aucune ressource ne porte la chaleur → elle répondait toujours `false`) →
  ajout `res = !!b.heatCap || !!b.tour` ; puis la passe de pontage de `rebuildNetworks` passe de
  `['wire','pipe']` à `['wire','pipe','conduit']`. ⚠ **La ROUTE reste exclue** (vérifié par
  contre-épreuve).
  ⚠ **A3.3 du brief (ajouter `'conduit'` à `carriers3`) NON APPLIQUÉ, volontairement** : un bloc de
  stub conduit **DÉDIÉ existe déjà** depuis 13.2 (juste après `carriers3`), sur exactement le même
  ensemble (`bdef.heatCap || bdef.tour`) et **en mieux** — il gère les variantes `_chauffe1/2/3` et la
  teinte selon `conduitLoad`. L'ajouter à `carriers3` n'aurait fait que dessiner un second stub NON
  teinté dessous. Aucun cas manquant : la passe générique n'apporterait que les JONCTIONS, or **il
  n'existe aucune jonction conduit** (`junction` = road/wire, road/pipe, wire/pipe uniquement).
  Les 208 sprites `conduit_*` sont tous présents (masques + variantes de chauffe).
  (4) **A4 — `nucList` publie enfin `regime` / `inFac` / `pwrAvg` / `discReason`.** Elle ne posait que
  `active` et `disc` → `drawBuilding` lisait des valeurs **figées d'un tick antérieur** : une centrale
  mise en pause UNE FOIS gardait `regime = 0` **à vie** → badge déficit permanent **et « 0 % »** sur
  une centrale qui tourne (retour joueur, save île 5), avec en prime `inFac`/`pwrAvg` `undefined` →
  cause repliée sur `'input'`, donc fausse. ⚠ **`regime` vaut 1 dès que la centrale marche OU calibre,
  quel que soit le CURSEUR** : une centrale volontairement réglée à 60 % n'est pas en déficit.
  (5) **A5 — la PAUSE d'une centrale devient un ARRÊT FRANC.** Elle faisait `continue` **avant**
  `nucList.push` → `nucState`/`nucTimer`/`nucCur` **GELÉS** (la save du joueur contenait bien deux
  centrales `pz:1` figées à `running` 524 MW et `stopping`). Désormais les branches `paused` et
  `logicOff` **poussent quand même** dans `nucList` avec un motif (`halt: 'paused' | 'logic'`), honoré
  par 4 conditions de la machine à états → rampe `stopping` de 30 s puis `off`. ⚠ La **RÉCUPÉRATION**
  déjà présente dans `stopping` fait repartir la rampe **depuis `nucCur`** : une pause brève n'est pas
  punie de 5 min (vérifié : reprise depuis 15 445 kW, pas 0). `bld.active = !halt && (…)`, mais la
  rampe **continue de délivrer son courant décroissant** → pas de blackout brutal ; `heatEmit` reste
  indexé sur `nucCur` (un réacteur qui décélère est encore chaud), décision assumée.
  ⚠ **Ordre A4 → A5** : A5.3 amende la ligne `discReason` posée par A4.
  ⚠ **Pièges de harnais** : `processHeat` est appelé **DEPUIS `tickIsland`** → une centrale de labo
  sans tour ni conduit **tripe en ~170 s** et masque toute la machine à états (neutraliser `bld.heat`
  dans la boucle de test) ; **`logicOff` est RECALCULÉ par `processLogic` à chaque tick** → le poser à
  la main est effacé, il faut un **vrai actionneur** dans `t.logic` (sans fil logique → signal 0 →
  bâtiment éteint depuis 14.08) ; forger un `game` à la main casse (`accumulators`…), **cloner
  `__gameRef.current`** ; injecter une save de test exige d'écrire **directement** les 3 clés de slot
  (`archipel_slot_<id>` / `archipel_slots` / `archipel_active`), le chemin hérité `archipel_save_v1`
  passant par `lsSet` que le gel de `setItem` bloque.
  Validé : `node --check` (7 blocs) + Chromium **4 suites, 71 assertions, 0 KO, 0 erreur JS** —
  plafond stable dans 4 états et invariant sur **tous** les bâtiments à chaleur ; coupure d'intrants
  et pause de 180 s puis reprise (à 5/25/50/100 % de régime) → **aucun trip** ; **contre-épreuve** :
  l'ancien modèle trippe sur le même scénario ; le trip reste fonctionnel sans refroidissement
  (tick 59 ≈ `HEAT_CAP_SECONDS`) ; antenne prod en pause ET par actionneur réel → `heatEmit = 0` ;
  conduit traversant une source = **UN SEUL** réseau de 4 tuiles, tour de l'autre côté qui refroidit
  (flux 1,024 MJ/s), **route toujours coupée**, câble toujours traversant ; **save RÉELLE du joueur**
  (801 bâtiments, 7 îles) : 0 endommagé au chargement et après 70 s, centrale île 5 r16/c19 Nv.13 →
  `regime = 1`, `pwrAvg = 1`, `discReason = null` (fini le badge déficit et le « 0 % »), et les
  **2 centrales en pause figées passent bien en arrêt franc** avec le motif `paused` ; motifs `wire` /
  `input` / `logic` corrects ; centrale V2 `noHeat` toujours sans chaleur ni trip.
  ⚠ **`__heat` étendu** (`heatCapOf`, `heatEmitMaxOf`, `HEAT_CAP_SECONDS`, `NUC_POWER_BASE`).
  ⚠ **HORS lot A, non traités (lots B et C du brief)** : affichage du max de chaleur en fiche et du
  max à évacuer sur le conduit, grisage des bâtiments exclusifs, sprites de raccord du Collisionneur,
  renommage Centrifugeuse → Centrale d'Enrichissement, ratio uranium/plutonium, boutons ± du mix
  irradié.
- **État précédent : `GAME_BUILD = 311`, `GAME_VERSION = 'Alpha 14.29'`, `SAVE_VERSION = 31`.**
  Changement 14.29 : **le Broyeur Uranium n'a PLUS de V2 (bridage économique au Nv.10) et la
  Centrifugeuse — qui est le palier V2 de la Centrale Enrichissement — remplace le yellowcake par
  de l'URANIUM et génère de la chaleur.** `SAVE_VERSION` INCHANGÉ (`migratePlacement`).
  ⚠ **DEUX MALENTENDUS À NE PAS REFAIRE.** (a) En 14.25 j'ai lu « Centrifugeuse uranium (niveau 11) »
  comme un bâtiment NEUF, palier du broyeur → j'ai créé `centrifugeuse_uranium` en gardant le broyeur
  hard-cappé, d'où « j'ai toujours des vieux broyeurs ». (b) En 14.28 j'ai sur-corrigé en FUSIONNANT
  tout (broyeur supprimé, enrichissement V1 à l'uranium, yellowcake purgé, nœuds 22/23/24 restructurés)
  — **entièrement annulé par `git revert`**. La demande réelle, tenir les TROIS phrases ensemble :
  « centrifugeuse v1 et broyeur uranium v1 comme avant », « broyeur v2 ne doit pas exister »,
  « centrifugeuse v2 remplace le yellowcake par de l'uranium en plus du plutonium ».
  (1) **INCHANGÉS** : `broyeur_uranium` (uranium 128 + acide 4 → yellowcake 1), `centrale_enrichissement`
  V1 (yellowcake 2 + acier → comb. U235), la ressource `yellow_cake`, et les nœuds **22/23/24**. La
  chaîne yellowcake reste la voie NORMALE.
  (2) **`broyeur_uranium` n'a plus AUCUN palier** : entrée retirée de `TIER_NEXT`, et le bâtiment
  `centrifugeuse_uranium` du 14.25 est SUPPRIMÉ (def, `TIER_STEP`, barre d'outils, nœud 41).
  (3) **« Hard cap au Nv.10 » = bridage ÉCONOMIQUE `COST_SOFTCAP_X2`** (choix du joueur : « fais comme
  l'aciérie/câblerie »), et non un cap dur. ⚠ **Piège évité** : mettre `TIER_NEXT = { next: null }`
  aurait cassé l'UI (bouton « 🔒 Densifier » vers un id nul, `tierEntry(null)`), et un flag `hardCap`
  neuf aurait dupliqué un mécanisme existant. Ratios au-delà du Nv.10 : **5,4 → 10,8 → 21,6**.
  (4) **`centrale_enrichissement_v2` → « Centrifugeuse Uranium V2 »** : `yellow_cake 1.5` remplacé par
  **`uranium 256`**, plutonium CONSERVÉ mais aux chiffres du joueur (**0,025**, soit 25,6 au Nv.11 —
  c'était 0,1), acier 0,25 inchangé → comb. U235 0,25. **`heatCap: 10` AJOUTÉ** (« génère de la
  chaleur ») + `centrale_enrichissement_v2` inscrit dans la liste `HEAT_PER_MW × MW consommés` de
  `tickIsland`. **Élec. ×4 du 14.26 conservée** (288/2016 → 294,9 MW → 2,36 GW au Nv.11).
  ⚠ Son art devient celui du Broyeur V2 supprimé (`BLD_SPRITE_OVERRIDE`).
  ⚠ Le nom est INLINE seulement : `centrale_enrichissement_v2` n'a pas d'entrée `bld` dans les LOCALES
  (comme tous les V2), contrairement à `centrale_enrichissement` qui en a une — **`applyToData`
  réécrit les noms depuis les LOCALES `bld` ET `tech`**, ces sections EXISTENT (j'avais conclu
  l'inverse en 14.25 : faux, la recherche était mal faite).
  (5) **MIGRATION — aucun bâtiment perdu** : `broyeur_uranium_v2` (saves ≤ 14.24) **ET**
  `centrifugeuse_uranium` (builds **308 et 310, tous deux PUBLIÉS**) → **`broyeur_uranium`**, niveau
  CONSERVÉ (le broyeur n'a plus de cap dur, seulement un coût qui explose). Sans ce renommage,
  `!BUILDINGS[p.b] → continue` sauterait la tuile et le joueur perdrait bâtiment + investissement.
  Validé : `node --check` (7 blocs) + Chromium **4 suites, 62 assertions, 0 KO, 0 erreur JS** —
  broyeur/enrichissement V1/yellowcake/nœuds 22-24 vérifiés INCHANGÉS ; broyeur sans palier + courbe
  de coût IDENTIQUE à l'aciérie (ratios 5,4/10,8/21,6) ; **moteur réel** : centrifugeuse V2 Nv.11 →
  2,62e5 uranium + 25,6 plutonium + 256 acier consommés, 256 comb. U235 produits, **0 yellowcake**,
  2,36 GW, chaleur émise ; broyeur V1 → 128 uranium/s → 1 yellowcake/s ; **migration par rechargement
  RÉEL** des 2 ids historiques (niveaux conservés) ; détecteur de blocage 14.27 rejoué ;
  non-régression 14.24→14.27 + boot réel.
  ⚠ **Pièges de harnais** : le broyeur consomme de l'ACIDE → il lui faut un **TUYAU** relié au port,
  une route ne suffit pas (sinon `discReason: 'pipe'` et il ne consomme rien) ; les assertions de NOM
  doivent viser la valeur APRÈS `applyToData` (« Broyeur Uranium », pas l'inline « Broyeur Uranium V1 »).
- **État précédent : `GAME_BUILD = 310`, `GAME_VERSION = 'Alpha 14.27'`, `SAVE_VERSION = 31`.**
  Changement 14.27 : **DÉBLOCAGE DE L'ENDGAME — l'Usine de Moteur Quantique passe du nœud 43 au
  nœud 41.** `SAVE_VERSION` INCHANGÉ (les nœuds sont reconstruits depuis `TECH_NODES` au chargement ;
  un joueur ayant déjà confirmé le 41 gagne simplement le déblocage au prochain `evaluateTechTree`).
  **LE BLOCAGE** (trouvé en répondant à « où est l'usine de moteur quantique ? ») :
  `usine_moteur_quantique` est l'**UNIQUE** productrice de `moteur_quantique` ; elle était débloquée
  par le nœud **43** (Collisionneur P3), dont le prérequis est le **42** (Réparation III), dont la
  LIVRAISON exige **1 000 moteur_quantique**. Dépendance **circulaire** → l'usine n'apparaissait
  JAMAIS dans la barre d'outils, et tout l'endgame (P3, stabilisateur quantique, antenne V2, mines
  V4) était définitivement hors d'atteinte. **CORRECTIF** : elle est déplacée dans les `unlocks` du
  nœud **41** (Collisionneur P2). Vérifié : tous ses coûts ET ses intrants (alliage de tungstène,
  pièce de précision, câble supraconducteur, ordi quantique, élém. moteur nuc., azote) proviennent de
  bâtiments débloqués BIEN AVANT le 41 → elle est réellement constructible à ce stade. Les autres
  récompenses des nœuds 41 et 43 sont inchangées.
  ⚠ **DÉTECTEUR AJOUTÉ À LA SUITE DE TESTS** (le piège s'est produit DEUX fois : 13.82 avec le
  Séparateur Cryogénique, puis ici) : une fermeture avant de l'arbre confirme les nœuds un à un et
  signale ceux qu'on ne peut jamais atteindre — un nœud n'est atteignable que si son prérequis l'est
  ET si toutes les ressources qu'il exige (`delivery` + reqs `produce`) sont produites par un
  bâtiment débloqué AVANT lui. ⚠ La centrale nucléaire produisant ses irradiés/plutonium
  DYNAMIQUEMENT (hors `outputs`), ils sont ajoutés à la main au détecteur, sinon il crie au loup.
  **Contre-épreuve incluse** : sur l'arbre d'AVANT le correctif, le détecteur signale bien les
  nœuds **42 et 43**. À rejouer à chaque livraison qui exige une ressource de fin de chaîne.
  Validé : `node --check` (7 blocs) + Chromium **17 assertions, 0 KO, 0 erreur JS** (déplacement,
  récompenses intactes, détecteur + contre-épreuve, non-régression 14.24→14.26, boot réel).
- **État précédent : `GAME_BUILD = 309`, `GAME_VERSION = 'Alpha 14.26'`, `SAVE_VERSION = 31`.**
  Changement 14.26 : **2 EXPLOITS FERMÉS + élec ×4 de la Centrale Enrichissement V2 + barre d'outils
  réorganisée.** `SAVE_VERSION` INCHANGÉ (`pl.du` = champ additif avec repli).
  (1) **EXPLOIT « baisser avant de réparer »** (signalé joueur). La facture de réparation après
  surchauffe = **20 % du coût TOTAL cumulé** (`buildingTotalCost`), calculée sur le niveau COURANT.
  Or « Baisser » **REMBOURSE** le coût du palier (`tryDowngrade` → `refund(upgradeCost(...))`) : le
  joueur baissait son bâtiment endommagé, réparait sur une base réduite, puis remontait avec l'argent
  rendu → il empochait **20 % de l'écart à chaque surchauffe**. **CORRECTIF** : nouveau champ
  **`bld.dmgUp`**, figé au moment du TRIP (dans `processHeat`), et `buildingTotalCost` prend le
  niveau le PLUS HAUT entre le courant et `dmgUp` **tant que le bâtiment est `damaged`**. Baisser
  reste permis, ça ne rapporte plus rien. ⚠ **Persisté** (`pl.du`) : sans ça un simple rechargement
  rouvrait l'exploit. ⚠ Correction placée dans `buildingTotalCost` et non dans `tryHeatRepair` :
  l'APERÇU de la fiche et le PAIEMENT passent tous deux par là, ils ne peuvent plus diverger.
  Repli save antérieure (pas de `dmgUp`) → niveau courant = comportement d'avant.
  (2) **EXPLOIT « la route fait transiter les liquides »** (signalé joueur) : une simple ROUTE entre
  le port et la tuile élévateur suffisait à faire descendre AUSSI les liquides — les machines du
  souterrain puisaient l'acide/l'eau directement au port sans qu'aucun tuyau ne relie port et
  élévateur en surface. Cause : `elevatorSurfaceLinked` acceptait **route OU tuyau**, sans distinguer
  le porteur, et la bascule `pipe → pipePort` ne testait que le réseau LOCAL de l'île 7. **CORRECTIF**
  : nouveau **`elevatorSurfaceLinkedFor(game, carrier)`** ; la bascule vers le PORT exige désormais
  un **réseau TUYAU** port ↔ élévateur (`underLiquidBlocked`). Sans lui les liquides restent dans la
  CITERNE locale et le bâtiment affiche le motif **`elevator`**.
  ⚠ **RUPTURE ASSUMÉE pour les parties en cours** (choix joueur : « appliquer sans toast ») : une
  partie qui n'a qu'une route verra ses machines souterraines à liquides s'arrêter jusqu'à la pose
  d'un tuyau. Le seul indice est le motif « élévateur » dans la fiche.
  ⚠ **Le cas MIROIR n'est PAS traité** (décision joueur) : un tuyau seul continue de débloquer le
  souterrain côté solides — `roadReachesPort` exige bien une route côté ÎLE 7, mais pas côté surface.
  (3) **Centrale Enrichissement V2 : conso élec. ×4** (demande, capture à l'appui) — sigmoïde
  **72/504 → 288/2016**, soit **294,9 MW → 2,36 GW au Nv.11** (contre 73,7 → 590 MW). **Recette
  INCHANGÉE** (yellowcake 1536 + acier 256 + plutonium 102,4 → comb.U235 256 au Nv.11).
  (4) **Barre d'outils réorganisée** (demande) : `centrale_gaz` + `geothermie` → **Énergie** (ce sont
  des producteurs d'électricité), `foreuse` → **Tungstène**, `presse_uhp` → **Quantique**. Vérifié :
  0 doublon, 0 id orphelin, aucun bâtiment perdu.
  ⚠ **BLOCAGE DUR TROUVÉ, NON CORRIGÉ (décision de game design)** — réponse à « où est l'usine de
  moteur quantique ? » : elle est bien dans le groupe **Quantique**, mais elle est **INATTEIGNABLE**.
  `usine_moteur_quantique` est l'**UNIQUE** producteur de `moteur_quantique` ; elle est débloquée par
  le nœud **43** (Collisionneur P3), dont le prérequis est le nœud **42** (Réparation III), dont la
  LIVRAISON exige **1 000 moteur_quantique**. Dépendance **circulaire** : 42 exige une ressource que
  seul 43 permet de produire, et 43 exige 42. Tout l'endgame (P3, stabilisateur quantique, antenne
  V2, mines V4) est donc hors d'atteinte. **Correctif d'une ligne proposé** : déplacer
  `usine_moteur_quantique` des `unlocks` du nœud 43 vers ceux du nœud **41** (Collisionneur P2) — à
  arbitrer, ça change le rythme de l'endgame. (Le même schéma existait déjà en 13.82 avec le
  Séparateur Cryogénique : à surveiller à chaque nouvelle livraison qui exige une ressource de fin.)
  Validé : `node --check` (7 blocs) + Chromium **4 suites, 45 assertions, 0 KO, 0 erreur JS** —
  élec ×4 et recette inchangée ; barre d'outils (4 déplacements, 0 doublon/orphelin) ; **exploit 1**
  par TRIP RÉEL (`processHeat` appelé jusqu'à la surchauffe → `dmgUp` figé à 8, facture inchangée
  après une baisse, **round-trip de sauvegarde** confirmant que `dmgUp` survit) ; **exploit 2** en
  MOTEUR RÉEL (route seule port↔élévateur + mine de tungstène souterraine reliée route ET tuyau
  local → **0 acide prélevé au port**, motif `elevator` ; pose d'un tuyau de surface → **l'acide
  redescend**) ; non-régression 14.24/14.25 + boot réel.
  ⚠ **Pièges de harnais** : pour provoquer un trip il faut appeler **`processHeat` directement** —
  la boucle bâtiment recalcule `heatEmit` à 0 pour un bâtiment non alimenté, et le trip n'arrive
  jamais ; côté île 7, le réseau tuyau doit **TOUCHER la tuile élévateur** pour être `connected`
  (sinon on teste une citerne isolée et le motif est `input`, pas `elevator`).
- **État précédent : `GAME_BUILD = 308`, `GAME_VERSION = 'Alpha 14.25'`, `SAVE_VERSION = 31`.**
  Changement 14.25 : **FIX du double comptage du panneau Production (régression 14.24) + Broyeur
  Uranium hard cap Nv.10 et remplacement du V2 par la CENTRIFUGEUSE URANIUM.** `SAVE_VERSION`
  INCHANGÉ (le renommage d'id se fait dans `migratePlacement`, qui tourne pour TOUTES les versions).
  (1) **RÉGRESSION 14.24 CORRIGÉE — l'onglet « Toutes » comptait l'île 6 DEUX FOIS.** Retour joueur
  « la conso d'He3 du Collisionneur est invisible dans l'onglet production ». **Reproduit dans le VRAI
  panneau, moteur et rAF réels** : la ligne était bien PRÉSENTE, mais à **2 /s pour une conso réelle
  de 1**. Cause : depuis le §1ter du 14.24, `islandFlowAgg(6)` ET `islandFlowAgg(7)` renvoient tous
  deux le total FUSIONNÉ ; or `ProductionPanel` somme sur toutes les îles débloquées → tout ce qui
  vit sur l'île 6 et le souterrain était additionné deux fois. **CORRECTIF** : l'onglet « Toutes » ne
  garde qu'UN représentant par groupe d'îles à port commun (`portSharingIslands`). ⚠ **Le bilan
  ÉLECTRIQUE, lui, continue d'itérer sur les îles NON dédupliquées** (`scopeRaw`) : l'électricité ne
  traverse PAS l'élévateur (règle 13.81 §7), chaque grille garde son bilan — dédupliquer là aurait
  fait disparaître toute la production et la demande du souterrain. Les onglets d'une île donnée
  restent le total fusionné (c'est l'objet du §1ter).
  ⚠ **Sur le build 307 la conso d'He3 s'affiche bien** (vérifié) : si le joueur ne la voit pas, c'est
  soit qu'il est resté sur le 306, soit que son Collisionneur est en PAUSE (`co.halt`) — depuis 14.24
  une machine en pause ne brûle plus d'He3, donc elle n'affiche rien, ce qui est correct. La fiche du
  Collisionneur nomme la cause (« Hélium 3 · MANQUANT (tuyau ?) » / « Électricité insuffisante »).
  (2) **BROYEUR URANIUM HARD CAP AU Nv.10** (demande) : `TIER_NEXT.broyeur_uranium.cap = 9` — le cap
  était DÉJÀ à 9 (upgrade 0-indexé → Nv.10 affiché), la vraie demande était le changement de cible de
  densification. Il densifie désormais vers `centrifugeuse_uranium`.
  (3) **`broyeur_uranium_v2` SUPPRIMÉ**, remplacé par **`centrifugeuse_uranium`** (même palier :
  entrée upgrade 10 = **Nv.11**, même forfait `alliage_tungstene: 400 + element_moteur_nuc: 50`).
  ⚠ **MIGRATION OBLIGATOIRE, sinon perte de bâtiment** : sans renommage, la boucle de chargement
  saute la tuile (`!BUILDINGS[p.b] → continue`) et le joueur PERD le bâtiment ET son investissement.
  `migratePlacement` renomme l'id en TÊTE, pour toutes les versions de save (testé par round-trip
  réel : bâtiment conservé, niveau 12 intact).
  (4) **RECETTE (valeurs demandées AU Nv.11, donc ÷1024 dans la def)** : intrants **uranium 2,62e5 +
  plutonium 25,6 + acier 256** → sortie **combustible_u235 256**. ⚠ **Ce n'est PLUS un broyeur** :
  elle court-circuite la chaîne yellowcake → enrichissement et fabrique DIRECTEMENT le combustible.
  ⚠ **Le plutonium devient un INTRANT** (2ᵉ débouché après l'usine moteur nuc.) : il vient de la
  centrale nucléaire, qui consomme du combustible U235 → **boucle douce**, amorçable uniquement par
  la chaîne V1 (broyeur Nv.≤10 → enrichissement). Garder quelques broyeurs au cap est donc NÉCESSAIRE.
  ⚠ **Effet de bord vérifié (même classe qu'en 14.19 pour la fonderie d'or)** : la centrifugeuse n'a
  plus AUCUN liquide (le V2 consommait de l'acide) → `buildingConnectsCarrier(…, 'pipe')` devient
  **FAUX**, elle ne fait plus **PONT** entre deux tronçons de tuyau (règle 10.59) : un tuyau qui
  traversait un Broyeur V2 sera **COUPÉ EN DEUX** après densification/migration. À garder en tête si
  un joueur signale un tuyau coupé près d'une centrifugeuse.
  (5) **CONSO ÉLECTRIQUE : sigmoïde `{base: 72, amp: 432}`** → **73 728 → 516 096 kW au Nv.11**
  (73,7 MW → 516 MW). ⚠ **`amp` est l'AMPLITUDE, pas le plafond : max = base + amp.** Un ×4 littéral
  des deux champs du V2 (18/126) donnerait 72/504, soit un plafond de **590 MW, pas 516**. On a calé
  sur les DEUX valeurs explicitement confirmées par le joueur (73,7 → 516), ce qui vaut ×4 sur le
  PLANCHER et `amp = 504 − 72 = 432`. **Piège à ne pas refaire** : ne jamais annoncer un plafond
  égal à `amp`.
  (6) **CHALEUR** : `heatCap: 10` (flag) + `centrifugeuse_uranium` ajouté à la liste
  `HEAT_PER_MW × MW consommés` de `tickIsland` (règle habituelle, comme machine_outil / presse UHP /
  usine moteur quantique). Le plafond de trip reste dynamique (60 s d'émission, `heatCapOf`).
  (7) **Art** : aucun sprite de centrifugeuse livré → `BLD_SPRITE_OVERRIDE.centrifugeuse_uranium =
  'bat_broyeur_uranium_v2'` (statique ET animation, `ANIM_BY_SK` résolvant depuis la clé de sprite).
  Le nom reste en français dans les 4 langues : **les LOCALES n'ont AUCUNE section `bld`**, les noms
  de bâtiments ne sont traduits nulle part (état existant, le V2 l'était aussi).
  Validé : `node --check` (7 blocs) + Chromium **6 suites, 58 assertions, 0 KO, 0 erreur JS** —
  def/paliers/forfait/sprite exacts, 0 id orphelin (déblocages ET barre d'outils) ; **VRAI panneau
  Production** (moteur + rAF réels, Collisionneur alimenté par une éolienne câblée) : ligne
  « Hélium 3 » présente et à **1 /s** (et non 2) ; **moteur réel** : centrifugeuse Nv.11 → 2,62e5
  uranium, 25,6 plutonium, 256 acier consommés et **256 combustible U235 produits** en un tick,
  516 096 kW nominaux, chaleur émise, plus de raccord tuyau ; migration d'une save contenant un
  Broyeur V2 par **rechargement réel** ; non-régression 14.24 + boot réel.
  ⚠ **Pièges de harnais** : l'électricité circule **PAR COMPOSANTE CÂBLE** — un générateur et sa
  machine doivent toucher LE MÊME réseau, sinon `discReason: 'power'` (deux tuiles de câble
  séparées ne suffisent pas) ; un générateur **sans câble adjacent** est coupé (`discReason: 'wire'`)
  même si l'ampleur du bilan d'île suffirait ; forger un id INCONNU sur une tuile vivante fait lever
  `drawBuilding` → geler le dessin avec `catchingUp` avant la forge.
- **État précédent : `GAME_BUILD = 307`, `GAME_VERSION = 'Alpha 14.24'`, `SAVE_VERSION = 31`.**
  Changement 14.24 : **PATCH 7 retours — He3 du Collisionneur enfin compté, ordre de l'élévateur au
  choix, totaux île 6 / île 6 S réconciliés, He3 ×4/palier, « Île 7 » → « Île 6 S », 2 garde-fous de
  lancement, Data Center en veille.** `SAVE_VERSION` INCHANGÉ (`elevatorPriorityOrder` est un champ
  additif avec repli ; `co.he3Used`/`he3Net`, `bld.dcIdle` sont transitoires).
  (1) **CONSO D'HÉLIUM 3 DU COLLISIONNEUR INVISIBLE** — le Collisionneur est un landmark de TERRAIN,
  pas un bâtiment : son prélèvement passe par `colliderDrawHe3` (déduction directe au port / à la
  citerne) et n'apparaissait dans AUCUN `b.inputs`. ⚠ **Le correctif du brief (`resourceRates` seul)
  aurait été INOPÉRANT en pratique** : le panneau Production et le popover lisent `islandFlowAgg`
  (flux RÉELS du dernier tick) et ne retombent sur `resourceRates` que si prod ET conso valent 0 —
  or un Séparateur Cryogénique qui produit de l'He3 rend prod > 0. Le flux est donc désormais inscrit
  dans **`netFlow`** au tick (`addFlow(co.he3Net, 'cons', 'helium3', co.he3Used)`), sur le réseau
  tuyau où la machine a réellement puisé ; `resourceRates` le gagne aussi (repli avant le 1ᵉʳ tick).
  On publie la quantité **MESURÉE** (`co.he3Used`) et non le barème du palier : la machine boit aussi
  pendant son démarrage et son état « prêt », rien du tout en pause.
  ⚠ **BUG ANNEXE TROUVÉ ET CORRIGÉ** : `colliderDrawHe3` était appelé AVANT le calcul de `co.halt` →
  sur une panne de COURANT l'He3 était prélevé PUIS la machine mise en pause dans le même tick :
  elle brûlait son carburant pendant toute la panne, exactement ce que le « tout ou rien » de 14.17
  voulait éviter. L'ordre est inversé (le manque de courant court-circuite le plein).
  (2) **ORDRE DE L'ÉLÉVATEUR AU CHOIX DU JOUEUR** (mode `priority`) : l'ordre des 3 catégories était
  câblé en dur (`[consDem, outDem, inDem]`). Nouveau `game.elevatorPriorityOrder` (persisté, défaut =
  ordre historique) + `elevatorPriorityOrderOf`/`isElevatorPriorityOrder` ; le tick permute les
  demandes avant `elevatorAllocate` puis redistribue par catégorie (`elevatorAllocate` INCHANGÉ).
  Setter `promoteElevatorCategory(cat)` = **remonte d'un rang** (permutation de 2 éléments → un ordre
  invalide est impossible par construction). UI : liste classée **1./2./3.** avec le débit servi et un
  bouton **▲** par ligne (la 1ʳᵉ n'en a pas) — pattern retenu contre le drag (fragile au doigt) et
  les 3 dropdowns (peuvent produire des ordres invalides). ⚠ Le bloc n'est affiché **qu'en mode
  Prioritaire** : en équitable/proportionnel l'ordre n'a aucun effet, l'afficher promettrait du faux.
  (3) **TOTAUX ÎLE 6 ≠ ÎLE 6 S POUR UN MÊME STOCK** : `game.netFlow` est indexé par GRILLE, or l'île 6
  et le souterrain partagent UN SEUL port (`portPool(7)` → `port[6]`) → chaque panneau n'affichait que
  la moitié des flux. Nouveau helper **`portSharingIslands`** (générique via `portIslandOf` : le jour
  où une vraie île 7 autonome existera, la fusion s'arrêtera d'elle-même), utilisé par
  **`islandFlowAgg` ET `resourceRates`** → les deux panneaux affichent désormais EXACTEMENT le même
  total.
  (4) **`COLLIDER_HE3` ×2 → ×4 par palier** (demande) : **P1 1 /s · P2 4 /s · P3 16 /s**. ⚠ À
  RE-PLAYTESTER : le Séparateur Cryogénique sort 0,01 He3/s de base (×2/niveau) → ~Nv.7 pour P1,
  ~Nv.9 pour P2, ~Nv.11 pour P3. C'est le genre de raideur que le 14.18 avait justement corrigé en
  passant de ×8 à ×2.
  (5) **« Île 7 » → « Île 6 S » côté joueur** : nouveau **`islandLabel(id)`** (à côté d'`islandIcon`),
  **16 sites** d'affichage migrés (les 13 du brief + 3 trouvés en plus : toast « Erreur de simulation
  (Île N) », toast « ne se construit que sur l'île N » — `exclusiveIsland` vaut 7 pour 4 bâtiments —,
  et le bouton surface/souterrain). Le titre du panneau Port et le sélecteur d'île y passent aussi
  (par robustesse : `currentIsland` y est déjà résolu par `portIslandOf`, donc jamais 7).
  ⚠ `islStr` de l'alerte de stock est bien une **clé de for-in (string)** → `islandLabel(+islStr)`,
  sinon le `id === 7` strict ne matcherait jamais. Usages internes (`isl === 7`, clés d'état,
  commentaires) volontairement INCHANGÉS. Garde-fou documenté dans la fonction : ne jamais réutiliser
  l'id 7 pour autre chose que le souterrain sans repasser par `islandLabel`.
  (6) **2 GARDE-FOUS DE LANCEMENT** (fusionnés dans `launchCollider`, via un point de décision UNIQUE
  **`colliderLaunchBlock(game)`** partagé avec l'état grisé du bouton) : **§5** le Collisionneur ne
  peut plus être lancé s'il n'est relié à AUCUN réseau logique (`colliderLogicLinked` : l'émetteur
  bas-gauche ET la vanne haut-droite doivent chacun toucher un conducteur — nouveau helper
  module-scope **`hasAdjacentLogicConductor`**, car `adjLogicNets` est une closure interne à
  `processLogic`, inaccessible, et résoudrait tout le graphe pour rien) ; **§4** une fois
  `COLLIDER_GOALS[palier]` atteint, la RELANCE est bloquée tant que le nœud de palier (39/41/43) n'est
  pas confirmé (`colliderGoalLocked`) → fini le sur-farming qui pré-validait le palier suivant.
  ⚠ Aucune machine en marche n'est coupée : `co.confirms` peut dépasser légèrement le seuil, c'est
  attendu. Le bouton est **grisé avec la cause nommée** (2 nouvelles lignes dans la fiche), au lieu de
  laisser cliquer dans le vide.
  (7) **DATA CENTER EN VEILLE tant que la séquence n'est pas lancée** : il n'a aucune sortie (14.16),
  son seul rôle est d'être le 2ᵉ émetteur du puzzle — or `processCollider` ne tire de manche qu'en
  `state === 'running'`. Nouveau flag transitoire `bld.dcIdle` → régime 0 **et retrait de
  `energyConsumers`** (donc **0 azote, 0 hélium 4, 0 processeur, 0 kW**). ⚠ **PAS le même cas que le
  `allOrNothing` de 14.17** (qui DOIT rester inscrit) : ici la condition de sortie de veille est
  EXTERNE (l'état du Collisionneur), elle ne dépend ni de `pwrAvg` ni du régime → aucun interblocage
  possible, et le retirer GÈLE son `pwrAvg` au lieu de l'effondrer. Nouvel état `dataCenterState`
  **`idle`** (+ libellé) : « en veille » n'est pas un déficit, il ne manque rien au joueur.
  Validé : `node --check` (7 blocs) + Chromium **7 suites, 151 assertions, 0 KO, 0 erreur JS** —
  helpers et barèmes exacts ; i18n en/es/de des 22 nouveaux libellés (`islandLabel(7)` → « Island 6 U »
  / « Isla 6 S » / « Insel 6 U ») ; **moteur RÉEL** : Data Center posé + relié (route/tuyau/câble) →
  **0 consommé et demande d'île à 0 kW** avant la séquence, puis recette EXACTE (ratios azote/hélium
  **512** et azote/processeur **1024**), retour en veille, **reprise (anti-interblocage)** ; He3
  prélevé au port au barème du palier (1 puis 4 /s), **visible dans `islandFlowAgg`**, **0 brûlé en
  pause de courant** ; élévateur en PÉNURIE réelle (presse UHP alimentée + chantier, débit 16/s) →
  l'ordre change VRAIMENT qui est servi, et ne change rien en fair/proportional ; **UI réelle** (vrai
  `InfoPanel`) : clics RÉELS sur ▲ → ordre permuté, bloc masqué en mode équitable ; bouton « Lancer »
  grisé sans réseau logique puis ACTIF une fois câblé (clic réel → lancement), grisé au palier atteint
  puis réactivé dès la recherche validée ; round-trip save/reload (SAVE_VERSION 31, champ additif) ;
  boot réel (horloge qui avance, canvas peint, 0 `tickErrors`).
  ⚠ **Pièges de harnais (re)confirmés** : sans passer les réseaux en `unlimited`, le débit V1 (tuyau
  64/s, **câble 2048 kW**) bride tout et on mesure le plafond du réseau, pas la recette ; le terrain
  `collider` n'existe QUE sur les grandes îles du **mode Normal** (une partie « Difficile » n'a pas de
  Collisionneur) ; `co.palier` est un CACHE posé par `processCollider` (la fiche affiche l'ancien
  palier tant qu'un tick n'a pas eu lieu) ; les popups d'astuce et `.research-backdrop` volent les
  clics d'un harnais UI.
- **État précédent : `GAME_BUILD = 306`, `GAME_VERSION = 'Alpha 14.23'`, `SAVE_VERSION = 31`.**
  Changement 14.23 : **hélium du Data Center ÷4** (demande). `SAVE_VERSION` INCHANGÉ (la save ne stocke
  qu'id + niveau, jamais les recettes). `data_center.inputs.helium4` **8 → 2**. Azote (1024), processeur
  (1), conso (1024 kW), `allOrNothing` et `maxPerIsland: 1` **inchangés** ; il n'a toujours AUCUNE sortie
  (rôle d'émetteur du puzzle du Collisionneur depuis 14.16). Contexte : le Séparateur Cryogénique ne sort
  que **0,1 He4/s de base** (×2/niveau) → à 8 He4/s le Data Center exigeait un séparateur très amélioré
  rien que pour démarrer, alors qu'il ne produit rien. ⚠ Aucun autre consommateur d'`helium4` dans le jeu
  (seule autre mention : le déblocage du nœud **#37**, « produire 100 helium4 », non touché).
  Validé : `node --check` (7 blocs) + Chromium **11 assertions, 0 KO, 0 erreur JS** — def exacte
  (2/1024/1, pas d'`outputs`, 1024 kW) + **moteur réel** : Data Center posé au coin du port de l'île 6
  (route + tuyau reliés au port, câble + accumulateur chargé pour le courant), 20 ticks →
  **40 hélium consommés = 2 /s exactement**, ratios azote/hélium **512** et hélium/processeur **2**
  (c'était 8) ; + non-régression complète 14.22 (élévateur-pont, pavé directionnel, méthane t3).
  ⚠ **Piège de harnais** : sans passer les réseaux en `unlimited`, le débit V1 du tuyau (64/s) bride
  azote+hélium et on mesure le PLAFOND du réseau, pas la recette.
- **État précédent : `GAME_BUILD = 305`, `GAME_VERSION = 'Alpha 14.22'`, `SAVE_VERSION = 31`.**
  Changement 14.22 : **méthane en T3 · plancher de 15 min pour l'extrapolation hors-ligne · L'ÉLÉVATEUR
  FAIT PONT (le vrai « tuyau coupé » du joueur) · sens N/E/S/O en PAVÉ DIRECTIONNEL.**
  `SAVE_VERSION` INCHANGÉ (aucun champ persisté ajouté ; `_catchUpStats` est transitoire).
  (1) **`methane` t5 → t3** (demande). ⚠ L'ordre de DÉCLARATION de `RES_TIER` vaut **place fixe** dans
  l'inventaire (`RES_ORDER_RANK`) → la ligne a été **DÉPLACÉE** dans le bloc t3 (après `eau_froide`),
  pas seulement ré-étiquetée, sinon elle aurait gardé son rang de fin de liste (même piège qu'en 14.16).
  (2) **HORS-LIGNE : au moins 15 min SIMULÉES avant la multiplication** (« Production approximatif hors
  ligne : simuler un minimum 15 min avant de faire la multiplication »). Le mode simplifié CHOISI le
  faisait déjà (`WARM = min(ticks, 900)`), mais la **bascule AUTOMATIQUE** (filet anti-gel du 14.13,
  budget 90 s quand le joueur a demandé le calcul complet) coupait où elle voulait et n'ajoutait que
  `SAMPLE` : mesuré, un rattrapage de 8 h était extrapolé à partir de **301 ticks (5 min)**, sur des
  machines encore en régime TRANSITOIRE (stocks qui se remplissent, chantiers en cours) — puis multiplié
  par 28 500 ticks. Nouveau **`MIN_WARM = 900`** appliqué aux DEUX chemins. Le débit reste mesuré sur les
  **300 derniers** ticks de l'échauffon (fenêtre de régime ÉTABLI, après les transitoires).
  ⚠ **Bug annexe corrigé** : le diviseur du débit était figé à `SAMPLE` alors que la fenêtre réelle
  pouvait être plus courte (bascule en cours d'échauffon) → **débit sous-estimé jusqu'à ×3**. On mémorise
  désormais `sampleFrom` (tick du snapshot) et on divise par `WARM - sampleFrom`, la fenêtre RÉELLE.
  Nouveau diagnostic transitoire **`g._catchUpStats`** `{ticks, warm, simulated, sampleFrom, approx,
  skipped}` — indispensable pour instruire un futur retour joueur sur le hors-ligne.
  (3) **« LES TUYAUX SONT CENSÉS TRAVERSER MAIS ILS SONT COUPÉS » — ce n'était PAS une jonction.**
  Capture décodée tuile par tuile (identification des sprites contre `__SPRITE_DATA__`) : le croisement
  est la **TUILE ÉLÉVATEUR** de l'île 6 — tuyau V4 au nord ET au sud, conduit V4 à l'ouest, route à l'est.
  Ce que le joueur voit n'est pas un réseau continu, ce sont les **STUBS que la cage dessine** vers chacun
  de ses voisins depuis 13.87 (« la cage aspire les réseaux route/tuyau/conduit »). Or l'élévateur est un
  **TERRAIN, pas un bâtiment** → il échappait à la règle de traversée 10.59 (qui ne teste que
  `t.building`) : les deux tronçons de tuyau restaient **DEUX réseaux distincts**, chacun avec sa citerne
  et son niveau, sans que rien ne le signale — le dessin promettait une continuité que la mécanique
  n'avait pas. **CORRECTIF** : `rebuildNetworks` fusionne les réseaux d'un même porteur adjacents à la
  tuile élévateur, exactement comme autour d'un bâtiment qui fait pont — pour **route, tuyau ET conduit**
  (les trois porteurs vers lesquels la cage dessine déjà un raccord). ⚠ **Le CÂBLE en est EXCLU** :
  l'électricité ne transite PAS par l'élévateur (règle 13.81 §7 — couper la géothermie de l'île 7 doit
  arrêter le souterrain même si l'île 6 est excédentaire) ; **vérifié par contre-épreuve**. ⚠ Gaté sur
  `elevatorRepaired` : une cage en ruine ne relie rien. `elevatorLinked` (conduit ↔ tampon de chaleur
  6 ↔ 7) est désormais reporté à la fusion.
  (4) **SENS N/E/S/O = PAVÉ DIRECTIONNEL** (demande + croquis) : les 4 boutons alignés « à suivre »
  deviennent une **CROIX 3×3** (N en haut, O à gauche, E à droite, S en bas) — la POSITION du bouton
  donne le sens, on ne lit plus une lettre pour savoir où l'on vise. Nouveau helper module **`dirPad(cur,
  onPick, cellOf)`** + CSS `.ip-dpad*`, branché aux **2** endroits qui proposent une direction : porte &
  capteur de la **couche logique** et **foreuse** (qui garde ses faces grisées « pas de mur de ce côté »).
  ⚠ L'index moteur `DIRS4` est `[N, S, O, E]` : la grille remappe (N→0, O→2, E→3, S→1). ⚠ La case
  CENTRALE reste **VIDE** : y répéter la lettre de la face courante la faisait lire comme un 5ᵉ bouton
  aligné avec O et E ; la face choisie se voit à son bouton surligné. Aucune clé i18n nouvelle (les
  libellés « Face de sortie » / « Signal sortant » / « Direction du creusement » sont inchangés).
  Validé : `node --check` (7 blocs) + Chromium **6 suites, 49 assertions, 0 KO, 0 erreur JS** — méthane
  t3 et RANG déplacé dans le bloc t3 (0 doublon sur 45 entrées) ; hors-ligne 8 h forgé en localStorage
  (`savedAt` antidaté, `Storage.prototype.setItem` gelé) → **900 ticks simulés / fenêtre 300** en mode
  simplifié ET sur la bascule automatique (horloge `performance.now` accélérée ×200 pour forcer le filet)
  avec **contre-épreuve : sans le correctif, 301 ticks** ; élévateur → tuyau/route/conduit fusionnés,
  **câble NON fusionné**, cage en ruine → toujours coupé ; pavé directionnel **rendu par le VRAI
  `InfoPanel`** (grille 3×3, N au-dessus de S, O à gauche de E, N et O centrés sur la croix) avec
  **clics réels** E·S·O·N → `[3,1,2,0]` exacts et `gateDir=2` sur OUEST ; non-régression **13.18** (les
  deux porteurs traversent la jonction et restent distincts), **10.59** (la raffinerie fait pont tuyau, le
  four à charbon coupe) et **13.81** (routes adjacentes à la cage toujours `connected` sur l'île 7) ;
  boot réel (horloge qui avance, canvas 100 %, 0 `tickErrors`).
  ⚠ **Piège de test rencontré** : `useGhostGuard` (13.50) avale le 1ᵉʳ click d'un panneau tant qu'aucun
  `pointerdown` interne n'a eu lieu → un test qui clique un bouton de fiche doit d'abord dispatcher un
  `pointerdown` dans le panneau, sinon le premier clic est perdu (constaté, non un bug).
- **État précédent : `GAME_BUILD = 304`, `GAME_VERSION = 'Alpha 14.21'`, `SAVE_VERSION = 31`.**
  Changement 14.21 : **VRAI BUG DE RÉSEAU TROUVÉ — l'axe d'une jonction était déduit des BÂTIMENTS
  voisins.** `SAVE_VERSION` INCHANGÉ (les réseaux sont reconstruits à chaque chargement).
  (1) **Texte de la tour aéroréfrigérante RETIRÉ** (demande) : les lignes « Puise dans » (14.18) et
  l'avertissement sur sa soif (14.17) sont supprimés — ils accusaient la tour, qui n'était pour rien
  dans le blocage. La fiche revient à « Eau X% · N eau/s ». (La publication de sa conso sur le réseau
  tuyau, elle, est conservée : c'est une donnée juste et utile.)
  (2) **LE BUG (retour : « si on met un bâtiment sur le tuyau… ça bloque le raffineur silicium ; si je
  mets un séparateur d'air, même problème ; on a l'impression que les réseaux sont séparés »).**
  Reproduit par **BALAYAGE EXHAUSTIF** (256 configurations autour d'une jonction) : **45 configurations
  sur 128 COUPAIENT** un tuyau qui traverse VISUELLEMENT la jonction. Symptôme exact du joueur (capture) :
  un réseau tuyau à **« TUILES 1 · FLUX 0 · TRANSIT aucun »** et le raffineur à 0 %.
  **CAUSE RACINE** : `junctionAxisH` déduit l'axe d'une jonction (quel porteur va horizontalement,
  lequel verticalement) à partir de `netConnectMask` — **qui compte AUSSI les BÂTIMENTS raccordés au
  porteur** (c'est voulu pour les SPRITES : on veut dessiner une branche vers un bâtiment desservi).
  Résultat : poser une pompe / une tour / un séparateur d'air / un raffineur Si à l'EST d'une jonction
  faisait croire que le tuyau courait EST-OUEST → **l'axe basculait** → le tracé NORD-SUD qui la
  traversait était **coupé en deux**, sans que rien ne le signale. Un bâtiment est un CONSOMMATEUR EN
  BOUT DE LIGNE : il ne dit rien du sens dans lequel le réseau court.
  **CORRECTIF** : `netConnectMask` gagne un 6ᵉ paramètre **`infraOnly`** (opt-in) ; `junctionAxisH`
  l'active → l'axe se déduit des **TRACÉS RÉELS uniquement** (infra + jonctions). Partout ailleurs
  (sprites, stubs sous bâtiments) les bâtiments comptent toujours. **45 coupures → 2.**
  ⚠ **Les 2 restantes sont INHÉRENTES au design** (règle 13.18, « une jonction est un CROISEMENT
  strict ») : avec du tuyau sur les **QUATRE** côtés, un seul axe peut passer — il faut une tuile de
  tuyau ordinaire, pas une jonction. Non corrigé, c'est la règle.
  ⚠ **La save du joueur n'en portait plus la trace** (il avait démoli le bâtiment avant d'exporter) :
  le bug a été trouvé par balayage, pas par lecture de sa partie. Le scan « pont manqué » sur ses
  7 îles est propre, et ses réseaux ne changent pas avec le correctif.
  Validé : `node --check` (7 blocs) + Chromium **11 suites, 159 assertions, 0 KO, 0 erreur JS** — le
  cas EXACT du joueur (tuyau N-S + séparateur d'air / raffineur Si / pompe / tour à l'E comme à l'O)
  ; non-régression du croisement (les deux porteurs traversent et restent des réseaux DISTINCTS), de
  la règle 13.18 (pas de diffusion perpendiculaire) et du raccordement d'un bâtiment en bout de ligne
  à travers la jonction ; + tout 14.17 → 14.20 et la save du joueur.
- **État précédent : `GAME_BUILD = 303`, `GAME_VERSION = 'Alpha 14.20'`, `SAVE_VERSION = 31`.**
  Changement 14.20 : **l'illimité devient une TECHNIQUE D'ÎLE + écran noir du petit hors-ligne.**
  `SAVE_VERSION` INCHANGÉ (`netInfPaid` et `unlimited` existent déjà).
  (1) **PLUS DE 2ᵉ BOUTON — « c'est forcément toute l'île »** (demande). Le bouton « ∞ Toute l'île »
  du 14.17 est SUPPRIMÉ : l'illimité n'est plus un attribut qu'on pose tracé par tracé, c'est une
  **technique acquise pour un TYPE de réseau sur une ÎLE**. `networkUnlimitedInfo` a désormais pour
  portée `networksOfType(île, type)` (au lieu de `coupledNetworkIds`) → un seul achat bascule TOUS
  les tracés. **Le forfait devient FORFAITAIRE** (`NETWORK_UNLIMITED_COST` une fois, plus ×nb de
  réseaux couplés) : découper son réseau en dix morceaux ne coûte plus dix fois le prix.
  (2) **Un tracé posé APRÈS l'achat naît ILLIMITÉ** : `rebuildNetworks` marque `net.unlimited` dès la
  création si `netInfPaidFor(île, type)`. Sans ça, le joueur payait « toute l'île » puis voyait chaque
  nouveau tronçon repartir en V1 limité — la demande vaut aussi pour la suite. ⚠ Le drapeau reste **par
  île ET par type** (payer la route ne donne pas le câble — vérifié par contre-épreuve).
  (3) **GATE V3 JUGÉ À L'ÉCHELLE DE L'ÎLE** (`islandHasUnlimitableNet`) : l'illimité s'achetant pour
  l'île, gater sur le niveau du seul tracé CLIQUÉ était arbitraire. C'était la cause du retour
  « impossible de faire les conduits de chaleur illimité » : sur la save du joueur, **3 conduits sur
  11 étaient V2** et n'affichaient donc AUCUN bouton, même à côté d'un V4 sur la même île. Désormais
  il suffit qu'UN réseau de ce type ait atteint V3. ⚠ **L'AUTRE moitié du blocage est le COÛT** :
  10 000 câbles supraconducteurs, produits **1/s par la seule Presse UHP** (≈ 2 h 45 de production
  dédiée), alors que le joueur en a **0 à 200 par île**. Le chiffre est celui qu'il a demandé en
  14.17 — signalé, non modifié, à trancher.
  (4) **ÉCRAN NOIR SUR UN PETIT HORS-LIGNE — cause trouvée et corrigée.** Le raccourci « absence
  ≤ 5 min → simuler d'un bloc, sans overlay » (14.71) simulait jusqu'à **300 ticks EN BLOQUANT le
  thread**. Mesuré sur la save du joueur : **4 min d'absence = 21 SECONDES de calcul**, pendant
  lesquelles la boucle rAF ne tourne pas et rien n'est peint — alors que le canvas vient d'être
  effacé par le `layout()` du retour d'arrière-plan. D'où « des fois dans les cas où il y a peu de
  hors ligne c'est un écran noir ». Le raccourci est **SUPPRIMÉ** : on passe toujours par le chemin
  DÉCOUPÉ (tranches de 80 ms), et c'est l'**OVERLAY qui devient différé** (`OVERLAY_AFTER_MS = 180`)
  → toujours pas de clignotement quand c'est rapide, mais jamais d'écran noir figé.
  Validé : `node --check` (7 blocs) + Chromium **10 suites, 146 assertions, 0 KO, 0 erreur JS** —
  portée « toute l'île » et forfait unique, gate V3 par île (un seul tracé V4 ouvre le bouton pour
  toute l'île), tracé posé après l'achat né illimité + contre-épreuve tuyau non payé, bouton unique
  dans le panneau **cliqué pour de vrai** ; et sur la **save RÉELLE** : rattrapage de 4 min →
  **blocage max 103 ms** (au lieu de 21 s), overlay affiché, canvas peint à l'arrivée ; les 11
  conduits du joueur passent tous au bouton ∞ sauf l'île 7 (aucun V3 dessus). i18n en/es/de.
- **État précédent : `GAME_BUILD = 302`, `GAME_VERSION = 'Alpha 14.19'`, `SAVE_VERSION = 31`.**
  Changement 14.19 : **3 ajustements de recettes (acide).** `SAVE_VERSION` INCHANGÉ (la save ne
  stocke qu'id + niveau, jamais les recettes). (1) **Mine Tungstène : acide 16 → 8** (÷2, demande).
  Appliqué au **V1 ET au V4** : c'est la MÊME recette, le V4 n'ayant jamais fait que la reprendre
  telle quelle (14.09). Sortie inchangée (1 tungstène + 8 pierre). (2) **Fonderie Or : acide RETIRÉ**
  (V1 et V2) → il ne lui reste que `minerai_or: 16`. (3) **Raffineur Silicium : acide RETIRÉ**
  (V1 et V2) → `silicium: 8 + oxygene: 32`. ⚠ **EFFET DE BORD IDENTIFIÉ ET TESTÉ** : l'acide était le
  SEUL liquide de la **Fonderie Or** → `buildingConnectsCarrier('fonderie_or','pipe')` devient
  **FAUX**. Conséquences : (a) plus de stub de tuyau dessiné sous elle ; (b) surtout, elle **ne fait
  plus PONT** entre deux tronçons de tuyau (règle de traversée 10.59) → un tuyau qui la traversait
  se retrouve **COUPÉ EN DEUX**. Vérifié par sonde sur la save du joueur : ses **2 fonderies d'or ne
  touchent qu'UN seul côté tuyau** → aucun réseau cassé chez lui. **Le Raffineur Si, lui, garde
  l'oxygène** (liquide) → il continue de faire pont, aucun changement. À garder en tête si un joueur
  signale un tuyau coupé près d'une fonderie d'or. ⚠ **L'acide garde 8 consommateurs**
  (puits_petrole_v2, usine_polymere_v2, four_arc_cuivre, broyeur_uranium(_v2), mine_tungstene(_v4),
  extracteur_souterrain) → il ne devient pas une ressource morte. Validé : `node --check` (7 blocs)
  + Chromium **8 suites, 127 assertions, 0 KO, 0 erreur JS** (dont les 6 recettes exactes, le
  raccordement tuyau des 3 bâtiments, la **coupure de tuyau reproduite** pour la fonderie d'or et la
  **contre-épreuve** « le raffineur Si laisse toujours passer », + non-régression 14.17/14.18 et
  rejeu de la save du joueur).
- **État précédent : `GAME_BUILD = 301`, `GAME_VERSION = 'Alpha 14.18'`, `SAVE_VERSION = 31`.**
  Changement 14.18 : **He3 ×2 par palier + « la tour bloque le tuyau » DIAGNOSTIQUÉ SUR LA SAVE DU
  JOUEUR.** `SAVE_VERSION` INCHANGÉ (`bld.waterFrom`/`waterAvail` transitoires).
  (1) **`COLLIDER_HE3` ×8 → ×2 par palier** (demande) : **P1 1 /s · P2 2 /s · P3 4 /s** (était
  1/8/64). En ×8, le palier 3 demandait 64 He3/s alors que le Séparateur Cryogénique n'en sort que
  0,01/s de base (×2/niveau) → il fallait le monter ~6 niveaux de plus rien que pour suivre.
  (2) **« LA TOUR AÉRORÉFRIGÉRANTE BLOQUE LE TUYAU » — RÉFUTÉ, sur la save du joueur elle-même.**
  Sonde sur les **23 tours** de ses 7 îles : **chaque tour a TOUS ses côtés tuyau sur LE MÊME
  réseau** (0 tour non fusionnée), et il n'existe dans TOUTE la partie **AUCUN endroit** où deux
  réseaux tuyau différents se font face de part et d'autre d'une tuile. La tour ne coupe rien : la
  passe de fusion « traversée » (10.59) la traverse bien, elle consomme de l'eau donc
  `buildingConnectsCarrier(tour,'pipe')` est vrai. **LA VRAIE CAUSE, mesurée** : une seule tour est
  à sec dans sa partie — **île 4, tour Nv.1 en (14,14)** — et elle est branchée sur le tuyau **RELIÉ
  AU PORT**, donc elle lit le **stock du PORT**, qui est à **0** ; pendant ce temps **948 952 221
  d'eau** dorment dans les CITERNES des tuyaux ISOLÉS de la MÊME île (île 5 : **10,6 milliards**
  piégés, port à 0 ; île 2 : **83 milliards** piégés). Les deux sources sont DISTINCTES par design
  (10.82 / 13.82) : un tuyau relié au port stocke AU PORT, un tuyau isolé garde sa citerne — mais
  RIEN ne le disait, d'où « la tour bloque le tuyau ». (3) **Correctif de DIAGNOSTIC** : nouvelle
  ligne **« Puise dans »** dans la fiche de la tour → **« le PORT · <stock> »** ou **« la citerne du
  tuyau · <stock> »**, en ROUGE si le stock est vide. Le joueur voit immédiatement que c'est le PORT
  qui est à sec, pas le tuyau qui est coupé. `bld.waterFrom` / `bld.waterAvail` posés par
  `processHeat`. ⚠ **Aucun changement de mécanique** : c'est de l'affichage.
  ⚠ **PISTE OUVERTE (non traitée, à arbitrer)** : l'eau (et tout liquide) s'accumule **sans plafond
  et sans usage** dans la citerne d'un tuyau isolé, invisible depuis l'inventaire. C'est ce qui rend
  le diagnostic si difficile. Options possibles : plafonner la citerne d'un réseau isolé, l'afficher
  dans une alerte, ou permettre un déversement vers le port. Décision de game design, non prise ici.
  Validé : `node --check` (7 blocs) + Chromium **7 suites, 112 assertions, 0 KO, 0 erreur JS**, dont
  la **save RÉELLE du joueur rejouée** (barème 1/2/4, les 23 tours savent d'où elles puisent, la
  tour à sec est bien identifiée « PORT · 0 », inventaire de l'eau piégée par île) ; i18n en/es/de.
  ⚠ **Outil réutilisable créé** : `scratchpad/decode.js` décode un export `ARCHv1:` (LZW+base64) en
  JSON, et `loadsave.js` le rejoue par le VRAI chemin de chargement — indispensable pour diagnostiquer
  un retour joueur sur sa partie.
- **État précédent : `GAME_BUILD = 300`, `GAME_VERSION = 'Alpha 14.17'`, `SAVE_VERSION = 31`.**
  Changement 14.17 : **PATCH 9 retours joueur — He3 du Collisionneur, cadence du Data Center, pause
  sur déficit, illimité par île, hors-ligne, conduit illimité, tour/tuyau, port au souterrain.**
  `SAVE_VERSION` INCHANGÉ (aucun champ persisté ajouté : `co.halt`/`co.want`/`co.he3Need`,
  `bld.waterNeed`/`waterDrawn` sont transitoires ; `unlimited` du conduit passe par le champ `unl`
  déjà générique de la sérialisation des réseaux).
  (1) **LE COLLISIONNEUR CONSOMME ENFIN DE L'HÉLIUM 3** (« le collisionneur ne consomme pas d'hélium
  3 »). C'était le chaînon manquant du souterrain, exactement le même défaut qu'`information_quantique`
  au 14.16 : la foreuse révèle les poches d'He3, le Séparateur Cryogénique le raffine… et **personne
  ne le consommait**. Nouveau barème **`COLLIDER_HE3` = P1 1 /s · P2 8 /s · P3 64 /s** (échelle
  binaire ×8 par palier ; le plafond de PUISSANCE, lui, fait ×16). Approvisionnement par **TUYAU sur
  le landmark**, même chemin que la tour aéroréfrigérante (`colliderDrawHe3` + `colliderBounds`) :
  réseau relié au port → lecture du PORT (les liquides d'un tuyau relié y sont flushés chaque tick),
  réseau isolé → lecture de sa CITERNE. **TOUT OU RIEN** : soit la manche est payée entièrement, soit
  **rien n'est prélevé** (sinon l'He3 brûlerait pendant que la machine est en pause). ⚠ **Rupture
  assumée pour les parties en cours** : un Collisionneur sans tuyau d'He3 se met en pause tant que le
  joueur ne l'a pas raccordé (c'est le but de la demande). ⚠ **Ordre de grandeur à surveiller** : le
  Séparateur Cryogénique sort 0,01 He3/s de base (×2^niveau) → il faut ~Nv.8 pour couvrir le palier 1,
  ~Nv.14 pour le palier 3. À rééquilibrer si c'est trop raide au playtest.
  (2) **TOUT DÉFICIT MET EN PAUSE** (« si il y a un quelconque déficit dans le Colisionneur ou le data
  center ça se met en pause »). **Collisionneur** : nouveau `co.halt` (`'power'` | `'fuel'` | null).
  Avant, un manque de courant faisait **RECULER** le démarrage (jusqu'à repartir de zéro) sans que rien
  ne le dise. Désormais le compte à rebours est **GELÉ**, la machine tombe à 0 kW, n'émet aucun code et
  ne tire aucune manche → **aucune pénalité n'est possible pendant une panne**, et elle reprend
  EXACTEMENT où elle en était. ⚠ **Piège évité, à ne pas défaire** : la DEMANDE électrique reste
  publiée en pause via un champ séparé **`co.want`** (la boucle énergie compare `want` au disponible
  pour poser `co.powered`, mais ne consomme que `co.cur` = 0). Si on mettait la demande à zéro, le tick
  suivant conclurait « alimenté » (0 kW suffisent toujours) et la machine battrait marche/arrêt à chaque
  tick. **Data Center** : nouveau flag de def **`allOrNothing`** lu dans la boucle bâtiment → au moindre
  déficit (intrants OU courant) son régime tombe à **0** au lieu de tourner au ralenti. Il n'a AUCUNE
  sortie : tourner à 40 % ne produisait rien tout en brûlant azote et hélium. ⚠ **Piège évité, à ne pas
  défaire** : il reste inscrit dans `energyConsumers` et `bld.active` reste `true` — c'est
  indispensable pour que la boucle énergie le serve et que `pwrAvg` remonte, sinon il resterait à 0 % à
  vie une fois coupé (**interblocage**, testé). L'affichage « 0 % + cause » vient de `regime`/`inFac`/
  `pwrAvg`, pas d'`active`. `dataCenterState` gagne l'état **`deficit`**.
  (3) **DATA CENTER : VALIDATIONS PAR SECONDE** (« le data center doit indiquer combien de validation
  par seconde »). Sa fiche gagne **Validations** (= `rate × reward`, ce qu'un comparateur correct
  rapporte réellement), **Cadence** (manches/s, ou « 1 manche toutes les N s ») et **Récompense** (×N).
  On voit enfin ce que rapporte une amélioration. (4) **UN SEUL DATA CENTER PAR ÎLE**
  (`maxPerIsland: 1`) : le moteur n'en a de toute façon jamais utilisé qu'un (`findDataCenter` renvoie
  le PREMIER trouvé) — en poser un second coûtait très cher pour rien.
  (5) **RÉSEAUX ILLIMITÉS SUR TOUTE L'ÎLE** (« j'ai amélioré plusieurs réseaux en illimité mais il y en
  a pas sur toute l'île »). **Ce n'était PAS un bug** (vérifié : l'héritage d'`unlimited` à la scission
  et à la fusion est correct depuis 13.33) : `unlimited` est un attribut de RÉSEAU et une île en porte
  facilement une dizaine du même type — il fallait tous les retrouver un par un. Nouveaux helpers
  `networksOfType` / `networkUnlimitedAllInfo` + bouton **« ∞ Toute l'île (N) »** dans le panneau
  réseau + handler `makeAllNetworksUnlimited`. **Ne coûte pas plus cher** : le forfait est déjà acquis
  une fois par île ET par type (`netInfPaid`, 14.05), donc les suivants sont gratuits.
  (6) **CONDUIT DE CHALEUR ILLIMITÉ : 10 000 CÂBLES SUPRACONDUCTEURS** (demande). Il lui manquait
  l'état illimité que les 3 autres réseaux ont depuis longtemps — le sprite V4 lui était **déjà
  réservé** depuis 13.86 mais aucun matériau n'était défini, donc `unlimited` restait faux à vie.
  Entrée `conduit` ajoutée à `NETWORK_HI_MATS` (`irradie: 'cable_supraconducteur'`, `cheap`/`premium`
  nuls → le coût des PALIERS garde sa formule dédiée ×10). `processHeat` : débit `Infinity` quand
  illimité → le conduit n'est plus jamais le facteur limitant (restent l'émission des sources et
  l'absorption des tours) ; teinte « chauffe » jamais saturée.
  (7) **CALCUL HORS-LIGNE : L'OPTION EST ENFIN RESPECTÉE** (« des fois le calcul hors ligne est quasi
  instantané malgré l'option calcul rapide désactivée »). **Cause trouvée** : le filet anti-gel du
  14.13 (bascule automatique en extrapolation) avait un budget de **8 s**, vite dépassé sur une grosse
  partie → l'option « simplifié » désactivée n'était de fait presque jamais respectée, **en silence**.
  Désormais le budget vaut **90 s quand le joueur a demandé le calcul complet** (8 s sinon) — le filet
  ne sert plus que d'anti-plantage — et quand il se déclenche quand même, **le récap le DIT**
  (`report.approx` → ligne « ≈ Production APPROXIMÉE… », et « ⏭ Rattrapage interrompu » après un
  « Passer »). (8) **RÉCAP HORS-LIGNE DÉFILABLE** (« possibilité de scroller les ressources eues
  pendant le temps hors ligne ») : la liste n'est plus **coupée à 12 entrées** (elle l'était en dur) et
  `.offline-gains` défile (`max-height:46vh; overflow-y:auto`).
  (9) **TOUR AÉRORÉFRIGÉRANTE / TUYAU : elle NE bloque PAS** (« la tour semble bloquer le tuyau, alors
  qu'aucune règle ne l'interdit »). **Vérifié par sonde** : tuyau · tuyau · TOUR · tuyau · tuyau donne
  **UN SEUL réseau** (la tour consomme de l'eau → `buildingConnectsCarrier(tour,'pipe')` vrai → la
  passe de fusion 10.59 la traverse), et un four à charbon coupe bien en contre-épreuve. **La vraie
  cause du ressenti** : la tour puise son eau dans `processHeat`, HORS de la boucle bâtiment → sa
  consommation n'apparaissait **ni** dans « Consommation /s » du panneau réseau, **ni** dans la demande
  du tuyau. Or elle est énorme (**256 eau/s × 2^niveau**, soit 131 072 /s au Nv.10) et se sert sur le
  stock commun → les autres machines à eau tombaient à 0 % sans que rien ne désigne le coupable.
  Correctif de **LISIBILITÉ** : sa demande et son flux sont publiés sur le réseau tuyau qui la dessert,
  sa fiche affiche « pris / besoin » et un avertissement sur le doublement par niveau. ⚠ **Affichage
  SEUL** : `netFactor`/`netTierFactor` sont déjà figés quand `processHeat` tourne → **aucun changement
  d'équilibrage**. (10) **BOUTON PORT AU SOUTERRAIN** (« quand on est dans le souterrain et qu'on
  clique sur le bouton port cela doit afficher le port de l'île 6 ») : le panneau s'ouvrait **VIDE**
  (l'île 7 n'a pas de port, `game.port[7]` n'existe jamais). Nouveau helper **`portIslandOf`** (7 → 6,
  identité ailleurs) passé au `PortPanel` → il affiche « Port — Île 6 », et tous ses handlers reçoivent
  déjà l'île en paramètre donc agissent sur la bonne. Aligne le panneau sur ce que faisaient déjà le
  moteur (`portPool`), le HUD et les coûts depuis 13.87.
  Validé : `node --check` (7 blocs) + Chromium **5 suites, 107 assertions, 0 KO, 0 erreur JS** —
  barèmes He3 et tout-ou-rien du plein ; pause sur manque d'He3 ET de courant avec **timer gelé** et
  **demande toujours publiée** ; 200 ticks en pause → **0 manche, 0 pénalité** ; reprise exacte au
  retour du carburant ; Data Center **par le moteur réel** (régime 100 % → déficit partiel → régime 0
  avec **0 azote et 0 hélium brûlés** → reprise) ; **non-régression anti-interblocage** (production
  coupée 40 ticks → `pwrAvg` effondré → courant rétabli → **régime 100 % retrouvé**) ; **cadence des
  manches inchangée** (20 manches sur 320 ticks au Nv.1) et 320 He3 consommés ; conduit V1 borné à
  1,024 MJ/s → **illimité = seule l'absorption de la tour limite** ; conso d'eau de la tour visible
  dans `netDemand`/`netFlow` ; récap hors-ligne **30 ressources listées + scroll effectif** + mention
  d'approximation ; bouton « ∞ Toute l'île (3) » **cliqué pour de vrai** (armement puis confirmation) ;
  fiche Data Center « 4 /s » au Nv.7 ; fiche Collisionneur « EN PAUSE » + ligne Hélium 3 ; **boot réel**
  (horloge qui avance, canvas peint, 0 `tickErrors`) ; i18n en/es/de des 23 nouveaux libellés.
  ⚠ **Le sprite du conduit V4 existait déjà** — rien à générer.
  Changement 14.16 : **suppression d'`information_quantique` + 3 ressources retriées.** `SAVE_VERSION`
  INCHANGÉ (purge idempotente au chargement, aucun champ ajouté). (1) **`information_quantique`
  SUPPRIMÉE** (demande utilisateur, suite au signalement du 14.15 : elle n'avait **aucun consommateur**
  dans tout le jeu et s'accumulait sans usage — 325 au port du testeur). Retirée des **4** points de
  déclaration : sprite `item_information_quantique`, `RES_SHORT`, `RES_TIER`, `CARRIER_BY_RES` — et
  la clé `outputs` du **Data Center**, qui **n'a donc plus AUCUNE sortie**. ⚠ **Le Data Center garde
  tout son rôle** : il est le **2ᵉ émetteur du puzzle du Collisionneur** (il publie `collider.dcCode`),
  et `processCollider` ne teste que son EXISTENCE (`findDataCenter`), jamais sa production → **le
  puzzle est intact** (vérifié). Ses intrants (processeur 1 + azote 1024 + He4 8) et ses 1024 kW sont
  **conservés** : ils deviennent le coût de fonctionnement du puzzle (les retirer serait un choix
  d'équilibrage non demandé). **2 conséquences vérifiées, assumées** : (a) sans `outputs`, il sort
  d'`eligible` → **l'antenne ne le booste plus** (elle n'aurait rien à booster) ; (b) il reste
  **raccordé à la ROUTE** (via l'intrant `processeur`, carrier road) **et au TUYAU** (azote/He4), et
  **continue de consommer** — le garde `if (!effOutputs && power <= 0)` ne le saute pas puisque
  `power = 1024 > 0`. (2) **`REMOVED_RESOURCES`** (nouvelle constante module, à côté de `RES_TIER`) +
  **purge au chargement** dans `loadSave` : sans elle, le stock hérité restait dans `game.port` et
  **l'inventaire du HUD l'affichait cassé** — la boucle `for (const k in resources) if (… > 0.5)
  invSet.add(k)` ajoute TOUTE ressource en stock, or la clé n'a plus ni sprite, ni nom court, ni tier
  (elle serait tombée en T5 avec un libellé `undefined`). Purge idempotente, réutilisable pour toute
  future suppression. (3) **3 ressources retriées** (demande utilisateur) : **minerai de tungstène
  `tungstene` t5 → t2**, **`gaz_fossiles` t5 → t2**, **`alliage_tungstene` t5 → t4**. ⚠ L'ordre de
  DÉCLARATION de `RES_TIER` vaut **place fixe** dans l'inventaire (`RES_ORDER_RANK`) → les 3 lignes ont
  été **DÉPLACÉES** dans leur nouveau bloc (et pas seulement ré-étiquetées), sinon elles auraient gardé
  un rang de fin de liste. ⚠ Piège rencontré : `gaz_fossiles` s'est retrouvé **en double** (t2 + t5
  résiduel) — en JS **la dernière clé gagne**, il serait resté en t5 ; doublon retiré, contrôle
  automatique ajouté au test (46 entrées, 0 doublon). Validé : `node --check` (7 blocs) + Chromium
  **6 assertions, 0 erreur JS** sur la **save RÉELLE du testeur** — `information_quantique` purgée du
  port (325 → absente), **aucune entrée « info.quant. » ni « undefined » dans l'inventaire**, Data
  Center sans sortie mais toujours raccordé route+tuyau, **Data Center émettant toujours son code**,
  0 `tickErrors`, horloge qui avance ; + **dump de l'inventaire RÉEL rendu** confirmant les groupes :
  T2 = `steel, cable, ref. Si, tungstène, oxygène, azote, gaz foss., U235 fuel`, T4 = `… plutonium,
  all.tungst.`, T5 = plus aucune trace des 4 ressources déplacées/supprimées.
  (4) **MANCHES SYNCHRONISÉES** (demande joueur : « le Data Center n'a pas la même vitesse… il
  faudrait qu'il envoie autant de données en même temps que le Data Center et qui suivent son
  amélioration »). Avant, le Collisionneur retirait une saveur à **CHAQUE tick** pendant que le Data
  Center gardait la sienne **4 à 16 ticks** → émetteurs DÉSYNCHRONISÉS, aucune « manche » observable
  (un côté clignotait, l'autre était figé). Désormais **une MANCHE = un tirage SIMULTANÉ des deux
  émetteurs**, à la **cadence du Data Center** (`co.round` incrémenté à chaque tirage). Sans Data
  Center, **aucune manche n'est tirée** et les DEUX émetteurs restent muets (face VALIDE à 0) → le
  joueur voit tout de suite ce qui manque. Nouvelle ligne **« Cadence des manches »** dans la fiche
  du Collisionneur (`1 toutes les N s · Data Center Nv.X`). (5) **UN SEUL verdict par MANCHE**
  (`co.roundDone`) : les codes étant maintenant TENUS plusieurs ticks, une vanne laissée ouverte
  était jugée à chaque tick → une manche gagnante rapportait jusqu'à **16 confirmations** et une
  manche perdue coûtait jusqu'à **16 pénalités** (constaté sur la save du joueur : 2 pénalités
  d'affilée pour un seul code du Data Center). (6) **`DC_RATE_CAP` 4 → 1** : un tick ne peut porter
  qu'UNE manche, donc au-delà de 1 manche/tick le débit était **purement perdu** — les niveaux 5, 6
  et 7 du Data Center donnaient EXACTEMENT la même cadence (1/s) pour un coût croissant, soit
  **2 améliorations mortes**. Le surplus devient le **multiplicateur de RÉCOMPENSE** déjà prévu par
  l'intention d'origine (Nv.6 ×2, Nv.7 ×4, Nv.8 ×8) → chaque niveau compte. ⚠ **Conséquence de
  rythme assumée** : au Nv.1 le puzzle avance à 1 manche/16 s (avant : le Collisionneur spammait
  chaque tick, ~2 confirmations/s) → **P1 (100 confirmations) demande d'améliorer le Data Center**,
  ce qui est exactement le but recherché. Validé : `node --check` (7 blocs) + Chromium **7 + 11
  assertions, 0 erreur JS** — cadence exacte par niveau (16 s / 4 s / 1 s, récompense ×1/×2/×4/×8),
  **aucun code ne change hors d'une manche**, comparateur sur α → **0 pénalité sur 4000 ticks** vs
  comparateur sur γ → pénalités (le défaut de câblage reste puni), vanne forcée ouverte 160 ticks →
  **10 verdicts pour 10 manches** (avant : 160) ; + non-régression complète 14.15/14.16 et boot de
  la save du joueur (0 `tickErrors`, canvas peint). ⚠ **CAUSE RÉELLE de SES pénalités, re-diagnostiquée
  sur sa 2ᵉ save** (`penalties: 5`, `confirms: 3`, Data Center **Nv.3**) : il a corrigé l'émetteur du
  **Data Center** (face **N = α**) mais celui du **Collisionneur est TOUJOURS sur OUEST = γ**
  (constant 0 au palier 1) → son XNOR calcule `XNOR(0, dc) = NON(dc)`, la vanne s'ouvre donc
  exactement quand le Data Center émet 0, et le verdict est alors un pur tirage à pile ou face sur
  le code du Collisionneur. La désynchronisation n'était PAS la cause de ses pénalités (un
  comparateur correct n'en prenait aucune, même désynchronisé) — mais elle rendait le puzzle
  illisible, et la correction est bonne en soi.
  (7) **LE DATA CENTER DOIT ÊTRE EN SERVICE POUR CALCULER** (question du joueur : « est-ce que
  mettre en pause le data center fait quelque chose ? » — réponse : **non, et c'était un bug**).
  `processCollider` ne testait que l'**EXISTENCE** du bâtiment (`findDataCenter`) : le mettre en
  PAUSE ne changeait RIEN au puzzle alors qu'il cessait de consommer ses intrants ET ses 1024 kW →
  **le puzzle tournait gratuitement**. Nouveau helper **`dataCenterState(tile)`** →
  `absent` / `paused` / `logic` (actionneur) / `damaged` / `starved` (intrants ou courant) / `on` ;
  seul `on` tire des manches. ⚠ `active` est posé par la boucle bâtiment, donc lu avec **un tick de
  retard** (`processCollider` tourne avant) — imperceptible, et `undefined` (jamais tické) vaut
  « en service ». Corollaire **voulu** : un Data Center à l'arrêt rend les DEUX émetteurs muets →
  **aucune pénalité n'est possible** (vérifié : vanne forcée ouverte 500 ticks → 0 pénalité), et la
  fiche du Collisionneur **nomme la cause** (nouvelle ligne « Data Center » + `DC_STATE_LABEL`).
  Validé : `node --check` (7 blocs) + Chromium **8 + 12 assertions, 0 erreur JS** — pause / coupure
  logique / manque d'intrants → 0 manche et émetteurs muets, retour en service → les manches
  repartent ; + non-régression 14.15/14.16 (comparateur correct : **0 pénalité sur 3000 ticks**) et
  boot de la 4ᵉ save du joueur. ⚠ **Sa 4ᵉ save re-confirme le diagnostic** : Data Center dé-pausé,
  circuit logique **INCHANGÉ**, émetteur du Collisionneur **toujours sur OUEST = γ** → 6ᵉ pénalité.
  (8) **LE CÂBLE LOGIQUE NE SE RACCORDE PLUS À UNE FACE MUETTE** (demande joueur : « le câble
  logique ne doit pas connecter tant qu'on n'a pas débloqué le signal β et γ, je pense aux sprites
  aussi »). C'est le correctif qui rend l'erreur **IMPOSSIBLE** au lieu de seulement lisible : au
  palier 1, β (SUD) et γ (OUEST) sont constants à 0 ; un comparateur câblé dessus compare 0 avec 0,
  répond « égal » en permanence et fait pénaliser — c'est exactement ce que le joueur a reproduit
  **3 saves d'affilée**. Deux helpers module : **`emitterFaceCarries(game, isl, dir)`** (dir 3 =
  VALIDE toujours exploitable, sinon `dir < colliderBits(palier)`) et **`emitterFaceFromStep(dr, dc)`**
  (le pas va du CÂBLE vers l'émetteur → la face est la direction OPPOSÉE, index `DIRS4 = [N,S,O,E]`).
  Le masque de connexion du câble logique les consulte : une face muette n'est plus raccordée → le
  fil apparaît **visiblement détaché**. ⚠ Purement VISUEL côté mécanique (l'émetteur n'écrivait déjà
  que des 1, donc une face muette ne pilotait rien) : aucune régression de simulation, et le
  raccordement **réapparaît tout seul** quand le palier débloque β puis γ. (9) **Sprites de
  l'émetteur** : le pack fournit un écran par SIGNAL (`logic_emetteur_alpha/beta/gamma` + `_on`) et
  un écran VIDE (`logic_emetteur_inactif`) — jusqu'ici le draw affichait `..._alpha` EN PERMANENCE,
  même émetteur à l'arrêt. Désormais : **écran éteint quand l'émetteur est muet** (face VALIDE à 0 :
  pas de Data Center, en pause, sans intrants…), sinon **le signal du palier courant** (P1 → α,
  P2 → β, P3 → γ) **allumé quand son bit vaut 1** → le joueur voit le code changer sur la carte.
  Validé : `node --check` (7 blocs) + Chromium **10 + 6 assertions, 0 erreur JS** — mapping des
  4 pas exact (N↔S, O↔E), faces porteuses par palier (P1 `α+VALIDE`, P2 `+β`, P3 `+γ`), masque de
  raccordement **`SO` en P1 → `NSO` en P2 → `NESO` en P3** (la face γ, celle du fil mal branché du
  joueur, est REFUSÉE en P1 et acceptée en P3), 7 variantes de sprite présentes ; + non-régression
  14.15/14.16 (comparateur correct : **0 pénalité sur 3000 ticks**) et boot de la save.
  (10) **BUILD 298 → 299 : deux APK DIFFÉRENTS avaient été publiés sous le MÊME numéro de build.**
  Le 14.16 a été mergé en **DEUX PR** (#287 = suppression d'`information_quantique` + retri des
  tiers ; #288 = manches synchronisées + pause du Data Center + sprites/faces), chacune déclenchant
  un build. Or `GAME_BUILD` n'avait été bumpé qu'UNE fois (297 → 298) : les **deux** APK se sont
  publiés en « build 298 », le second écrasant le premier dans la release `apk-latest`. Conséquence
  exacte remontée par le testeur (« j'ai pas les dernières modifications avec les sprites ») : ayant
  installé l'APK de la PR #287, son jeu comparait `version.json` (298) à son `GAME_BUILD` (298) →
  `build > GAME_BUILD` FAUX → **aucune notification de mise à jour**, alors que l'APK en ligne
  contenait bien tout (vérifié en téléchargeant la release et en grepant l'HTML embarqué :
  `emitterFaceCarries`, `logic_emetteur_inactif`, `dataCenterState`, `REMOVED_RESOURCES`,
  `co.roundDone` tous PRÉSENTS). Même piège pour le **cache du service worker** (`archipel-$BUILD`,
  identique → pas de re-fetch). ⚠ **RÈGLE À RETENIR** : le bump de `GAME_BUILD` doit être fait
  **par LIVRAISON (par merge sur `main`)**, pas par « lot de travail » — si un même lot part en
  plusieurs PR, il faut **re-bumper à chaque PR**, sinon les installations restent bloquées sur la
  première. Contournement immédiat pour un joueur déjà coincé : réinstaller l'APK à la main depuis
  la release (le lien fonctionne, c'est seulement la NOTIFICATION qui ne part pas).
  Changement 14.15 : **COMPARATEUR DU COLLISIONNEUR — « que des erreurs avec un comparateur normal ».**
  `SAVE_VERSION` INCHANGÉ (aucun champ persisté ajouté ; `lastVerdict` transitoire). Le testeur a
  fourni sa save : **palier 1, 3 pénalités, 0 confirmation**. **CAUSE RACINE (diagnostiquée sur sa
  save)** : son comparateur XNOR est **PARFAITEMENT JUSTE** (2 NOT + 2 AND + 1 OR, exactement le
  montage du tuto `collider_cmp1`) — mais ses DEUX émetteurs ne sont câblés que sur la face
  **OUEST**, or le mapping des faces est **FIXE et non orientable** : `DIRS4 = [N, S, O, E]` →
  **dir 0 = NORD = α**, 1 = SUD = β, **2 = OUEST = γ**. Au palier 1 **seul α porte la donnée** ; γ est
  **constant à 0**. Son XNOR comparait donc **0 avec 0** → il répondait « égal » **à chaque tick** →
  la vanne s'ouvrait en permanence → pénalité à chaque tirage où les vrais codes différaient (~50 %).
  **Et RIEN à l'écran ne disait quelle face est α** (ni la fiche, ni le tuto). D'où « on est dans le
  flou ». **5 défauts corrigés.** (1) **La vanne comparait les CHAÎNES** (`a === b2`) alors que le
  joueur ne voit que les **BITS émis** : au P3, Collisionneur `'100'` vs Data Center niveau 0 `'1'`
  donne **les mêmes faces `1000`** mais des chaînes différentes → **pénalité imméritée**. Nouveau
  helper **`colliderCompare(a, b, palier)`** : comparaison **sur les bits, sur la largeur du palier**.
  (2) **`dcCode` absent → PÉNALITÉ GARANTIE.** Sans Data Center (ou avant son 1er calcul) `dcCode`
  vaut `null`, l'émetteur sort `0000` = exactement une saveur `'0'` → un comparateur CORRECT conclut
  « égal », envoie 1, et `b2 != null` était faux → **pénalité**. Désormais `colliderCompare` renvoie
  **`null` = manche INVALIDE** : **ni confirmation ni pénalité**. (3) **Le Data Center restait muet
  ~16 ticks** (0,0625 tirage/tick au niveau 0) pendant que le Collisionneur émettait déjà à chaque
  tick → c'est **là** que tombaient les pénalités. **1er calcul désormais IMMÉDIAT.** (4) **4ᵉ face =
  STROBE « VALIDE »** : elle était « réservée, toujours 0 » ; elle vaut maintenant **1 dès que
  l'émetteur publie un code réel** → une saveur `'0'` et un émetteur **muet** sont enfin
  **DISTINGUABLES** (ils sortaient tous deux `0000`). (5) **Une porte sans AUCUNE entrée câblée sort
  désormais 0** : seul le `AND` avait ce garde (`ins.length > 0`) — un **NOT/NAND/NOR/XNOR flottant
  sortait 1**, donc un montage incomplet **ouvrait la vanne** (l'action destructrice). Une porte
  câblée dont l'entrée vaut 0 est **inchangée** (`ins = [0]` → NOT sort bien 1). **UX (le vrai
  correctif « flou »)** : fiche de l'**ÉMETTEUR** = **plan des faces** (`N α / S β / O γ / E VALIDE`)
  avec le **bit courant de chacune**, les faces **inutiles au palier grisées** + avertissement
  explicite ; fiche de la **VANNE** = signal reçu, codes comparés, verdict ; fiche du
  **COLLISIONNEUR** = codes **et faces émises** + **verdict de la dernière manche**. ⚠ **L'émetteur
  et la vanne n'étaient PAS inspectables** (le tap en couche logique n'acceptait que
  porte/capteur/actionneur) → le plan des faces aurait été inatteignable : condition élargie à
  `logicMultiSource`/`logicValve`, et la **rotation retirée** pour eux (faces fixes / OU des 4 faces —
  la proposer était mensonger). Tutos `collider_cmp1` et `collider_penalite` réécrits (mapping des
  faces énoncé noir sur blanc). Validé : `node --check` (7 blocs) + Chromium **25 assertions ×3 runs,
  0 échec, 0 erreur JS** — les 3 cas qui pénalisaient à tort ; strobe sur les 3 états ; **save RÉELLE
  du testeur** rejouée (diagnostic automatique : « face NORD câblée sur AUCUN des 2 émetteurs, le
  comparateur est sur OUEST=γ ») ; **un comparateur correctement câblé → 0 pénalité sur 4000 tirages
  en P1 ET P3**, détecteur de leptons **+68 % de confirmations** (le puzzle garde son intérêt) ;
  non-régression : vanne forcée sur tirages aléatoires → pénalités ET confirmations ; + **rendu réel
  des 2 nouveaux panneaux** par tap canvas (gel du tick via `catchingUp`) ; + boot de la save du
  testeur (0 `tickErrors`, horloge qui avance, canvas peint). ⚠ **SIGNALÉ, non traité** :
  `information_quantique` (sortie du Data Center) **n'a AUCUN consommateur** dans tout le jeu — elle
  s'accumule sans usage (325 au port du testeur). C'est le « pas de stockage d'informations
  quantique » remonté : la ressource existe et EST bien stockée, mais elle ne sert à rien. Lui donner
  un débouché (alimenter les manches du Collisionneur ? recette d'endgame ?) est une **décision de
  game design** à trancher, pas un bug — non inventée ici.
  Changement 14.14 : **LA VRAIE CAUSE DU GEL — une FAUTE DE FRAPPE de mon code du 14.08 tuait le tick
  de toute île possédant une CENTRALE NUCLÉAIRE.** `SAVE_VERSION` INCHANGÉ (1 ligne de correctif).
  Le testeur a fourni son EXPORT de sauvegarde : chargé tel quel, il donne l'exception en 9 secondes —
  `ReferenceError: b is not defined` dans `tickIsland`, **sur les îles 2, 3, 4, 5 ET 6**. La ligne
  fautive est celle que j'ai écrite au 14.08 pour le flag `noHeat` de la Centrale V2 :
  `bld.heatEmit = b.noHeat ? …` — or dans la boucle `for (const nu of nucList)` la définition
  s'appelle **`nb`** (`const nb = BUILDINGS[bld.id]`), **`b` n'existe pas dans ce scope**. Corrigé en
  `nb && nb.noHeat`. **Pourquoi je ne l'ai pas trouvé en 4 passages** : la ligne n'est atteinte QUE
  s'il y a une centrale sur l'île → **une partie neuve ne plante jamais**, et toutes mes reproductions
  (îles peuplées à la main, balayage 7 îles, parcours de MAJ) posaient des bâtiments 1×1 — la centrale
  est **2×2** et mes forgeurs la SAUTAIENT (`if (b.size && …) continue;`). Le testeur me l'avait dit
  dès le départ : « ma save depuis l'update ne fonctionne plus », partie neuve OK. **Mécanique du
  gel** : `onTick` avorte à la 1re île qui a une centrale → l'exception remonte à `frame`, dont le
  `try/catch` la mange → **`draw()` n'est JAMAIS atteint**, à chaque frame. D'où, exactement : écran
  noir (le canvas est effacé au 1er `layout()` et plus rien ne le repeint), **chronomètre figé à
  43:28:36 sur toutes les captures**, et « je ne peux plus zoomer, toucher les bâtiments ». ⚠ **Ma
  théorie du 14.13 (rattrapage hors-ligne bloqué) N'ÉTAIT PAS son cas** : sa save a
  **`offlineEnabled: false`** — le rattrapage ne tourne même pas chez lui. Les filets du 14.13
  (soupape rAF, bascule en extrapolation) restent utiles mais ne visaient pas la bonne panne ; en
  revanche l'**isolation par île du 14.13 fait sa preuve ici** : avec elle, la même faute ne gèle plus
  le jeu, elle arrête UNE île et lève un toast rouge. **Durcissement ajouté** : `step()` de
  `runCatchUp` enveloppe `onTick`/`tickShips` dans un `try/catch` — une exception ne peut plus tuer la
  chaîne `setTimeout` (ce qui laisserait `catchingUp` vrai à vie = gel définitif, y compris après
  relance). Validé : `node --check` (7 blocs) + **la sauvegarde RÉELLE du testeur** (711 bâtiments,
  7 îles, 156 516 ticks) rejouée par le vrai chemin de chargement : **0 erreur, 0 `tickErrors`,
  canvas 100 %, horloge qui avance, zoom qui répond, les 7 onglets d'île en 2 tours tous dessinés, et
  les ports qui bougent** (22 assertions) ; + **suite dédiée** (8 assertions) qui pose une centrale V1
  et une V2 et vérifie 0 exception, V1 émettant 2,04 MJ/s et **V2 exactement 0** — suite dont j'ai
  **vérifié qu'elle ÉCHOUE (4 KO) si je remets la ligne fautive** ; + non-régression 5 suites
  (22 + 46 + 11 + 10 assertions). Build 295→296.
  Changement 14.13 : **LE GEL — le rattrapage hors-ligne pouvait bloquer le jeu DÉFINITIVEMENT.**
  `SAVE_VERSION` INCHANGÉ. Le vrai symptôme n'était ni le noir ni les sprites : « je ne peux plus
  zoomer, toucher les bâtiments… on dirait que c'est freeze ». **L'indice était sous mes yeux depuis
  la 1re capture** : le chronomètre affiche **43:28:36 sur TOUTES les captures**, sur plusieurs jours
  — `playTicks` n'avançait plus. Je l'avais mis sur le compte du hasard ; c'était la preuve du gel.
  **Cause** : `runCatchUp` simule l'absence par TRANCHES de 80 ms enchaînées en **`setTimeout(step, 0)`**,
  avec `g.catchingUp = true` pendant toute l'opération. Or la boucle `frame` fait `if (g.catchingUp)
  { … return; }` → **ni tick, ni dessin, et `markDirty()` reste sans effet** (les taps ne produisent
  plus rien). Si Android **gèle ou tue ses timers** au milieu du rattrapage (l'app part en
  arrière-plan), la chaîne ne repart JAMAIS : `catchingUp` reste vrai à vie, et **chaque `onResume`
  est gaté par `!catchingUp`** → aucune sortie possible, même en relançant l'app (le boot refait un
  rattrapage qui se re-gèle). Aggravé par le défaut du 13.34 (`simplifyOffline` = **false**) : une
  absence de 8 h = **28 800 ticks SIMULÉS** — sur une grosse partie c'est plusieurs MINUTES d'écran
  figé, indistinguables d'un plantage. **Deux correctifs.** (1) **SOUPAPE dans la boucle rAF** (seule
  horloge fiable : le rAF ne tourne que quand l'app est visible) — chaque tranche horodate
  `g._catchUpTs` ; si plus rien n'a progressé depuis **4 s**, le rattrapage est déclaré mort, on
  appelle `finishCatchUp` et **on rend la main au joueur**. (2) **Bascule AUTOMATIQUE en
  extrapolation** : on mesure le débit réel (`ms/tick`) et, si finir la simulation complète
  dépasserait un **budget de gel de 8 s**, on repasse au mode « échauffon + extrapolation »
  (`simplify`) — le joueur retrouve la main en ~1 s au lieu de plusieurs minutes. L'option
  « Calcul hors-ligne simplifié » n'est PAS touchée : c'est un filet, pas un changement de défaut.
  **(3) ISOLATION PAR ÎLE — un gel ne peut plus être SILENCIEUX.** Retour affiné du testeur :
  « ça part en couilles **1 seconde après le chargement initial** » = au TOUT PREMIER tick. Une
  exception dans `tickIsland` remontait jusqu'à la boucle `frame` : `playTicks` n'était plus
  incrémenté (d'où l'horloge figée **sur la valeur sauvegardée**), `draw()` n'était jamais atteint,
  et `markDirty()` restait sans effet → écran noir + jeu qui ne répond plus, **sans le moindre
  message**. Désormais `onTick` enveloppe CHAQUE île dans un try/catch : l'île fautive est isolée
  (`game.tickErrors[isl]`), le reste du jeu continue de tourner et de se dessiner, et un **toast
  rouge nomme l'île et l'erreur** (+ `console.error('Archipel tick error (île N)')`). ⚠ Ce n'est PAS
  la cause racine — elle dépend de la sauvegarde du testeur, non reproduite en laboratoire (partie
  synthétique « fin de jeu », 7 îles peuplées, changements d'île par les VRAIS onglets, 2 tours
  complets : 0 erreur) — mais le jeu redevient jouable et le défaut devient DIAGNOSTICABLE.
  Validé : `node --check` (7 blocs) + Chromium **5 suites, 97 assertions, 0 erreur JS** dont une
  suite dédiée qui **REPRODUIT le gel** (les `setTimeout` sont avalés après 2 tranches, comme une
  WebView qui passe en arrière-plan) : `catchingUp` constaté à `true` avec le jeu figé, puis la
  soupape le clôt, **l'horloge repart, le canvas est repeint et le zoom répond de nouveau** ; +
  un rattrapage de 8 h qui se termine en **185 ms** au lieu de plusieurs minutes ; + une **île
  sabotée** (tuile qui lève à la lecture) : l'exception est capturée et attribuée, **l'horloge
  continue d'avancer et le canvas continue d'être peint**. Build 294→295.
  Changement 14.12 : **le chien de garde du 14.10 provoquait LUI-MÊME le clignotement.** `SAVE_VERSION`
  INCHANGÉ (rendu seul). Retour testeur sur le build 292 : « **il y a animation et ça repart au noir
  après 1 seconde** » — ma détection de canvas vide battait en boucle. Deux défauts, tous deux dans
  mon code du 14.10 : (1) son test « **tous les pixels identiques** » jugeait VIDE une vue
  parfaitement dessinée mais **UNIFORME** — une vue 100 % océan, ou un terrain rendu en couleur de
  repli quand les bitmaps sont morts (cas du 14.11 !) → il recréait le canvas, **ce qui efface
  l'écran** → image, noir, image, noir jusqu'au plafond de 8 recréations. Le test porte désormais sur
  l'**ALPHA** : `draw()` commence par un `clearRect` (alpha 0), donc si tous les points échantillonnés
  ont ENCORE alpha 0, aucune primitive n'a abouti ; une vue uniforme opaque a alpha 255 et n'est plus
  jugée vide. (2) Le 1er palier appelait **`layout()`**, qui réassigne `canvas.width` et **EFFACE le
  canvas** → il provoquait exactement le noir qu'il devait corriger. Retiré (les dimensions n'ont pas
  changé) : on ne fait plus que purger le cache d'images + redemander un dessin. ⚠ Les deux défauts se
  RENFORÇAIENT avec le bug 14.11 : bitmaps morts → terrain en couleur de repli → vue uniforme → jugée
  vide → recréation → écran effacé → re-décodage partiel → uniforme → … Validé : `node --check`
  (7 blocs) + Chromium **4 suites, 87 assertions, 0 erreur JS**, dont l'assertion dédiée : un écran
  peint d'UNE seule couleur opaque (cas « 100 % océan ») traverse **3 cycles du chien de garde sans
  aucune recréation de canvas**. Build 293→294.
  Changement 14.11 : **LA VRAIE CAUSE du « noir / bloqué » — les BITMAPS DÉCODÉS sont jetés par la
  WebView au retour d'arrière-plan.** `SAVE_VERSION` INCHANGÉ (rendu seul). Le 14.10 visait la perte
  de SURFACE ; deux nouvelles captures ont montré autre chose : rendu **PARTIEL** — le contour de
  côte (écume) dessiné, quelques machines dessinées, mais **le terrain absent (fond noir)** et la
  plupart des bâtiments réduits à leur **carré de couleur** (leur repli vectoriel). Précision
  décisive du testeur : « **quand je reviens sur l'application** ». Ce n'est donc pas le contexte
  canvas, c'est le **cache d'images**. **Deux défauts trouvés.** (1) **`spriteImg` renvoyait une
  image INUTILISABLE** : `return cached && cached.complete ? cached : cached || null` — une Image en
  cours de décodage (ou dont le bitmap a été jeté) était renvoyée telle quelle, `drawSprite` faisait
  `drawImage` (qui ne peint RIEN sans lever d'erreur) puis renvoyait **`true`** → l'appelant sautait
  son repli (`TERRAIN_COLORS`, vectoriel) → **tuile INVISIBLE**, c'est-à-dire du NOIR. C'est pour ça
  que mes sondes n'avaient rien vu : en laboratoire les images sont déjà décodées. Nouveau garde
  **`spriteUsable(img)` = `complete && naturalWidth > 0`** (le `naturalWidth` retombe à 0 quand
  Android jette le bitmap, alors que `complete` reste `true` — d'où l'échec silencieux) ; `spriteImg`
  ET `animImg` renvoient désormais **null** tant que l'image n'est pas dessinable → le repli COLORÉ
  s'affiche et `spriteOnReady` redessine au décodage. (2) **Aucune re-décodage au retour
  d'arrière-plan** : les entrées du cache restaient des Image mortes, définitivement. Nouveau
  **`resetSpriteCaches()`** (vide `spriteImgCache` + `animImgCache` → tout est re-décodé depuis les
  data-URL) appelé (a) **au retour sur l'application** (`onResume`, avant `layout()`/`draw()`),
  (b) au 1er constat du chien de garde 14.10 et (c) sur `contextrestored`. Le cache étant peuplé
  PARESSEUSEMENT (sprite par sprite au fil des dessins), la purge ne coûte que le re-décodage de ce
  qui est réellement visible (~32 sprites sur une vue d'île), pas des ~900 inlinés. Validé :
  `node --check` (7 blocs) + Chromium **4 suites, 86 assertions, 0 erreur JS** dont une suite dédiée
  qui REPRODUIT le symptôme (`naturalWidth` forcé à 0 sur le prototype `Image` → toutes les images du
  cache deviennent mortes) : rendu dégradé constaté, **mais le terrain garde son repli coloré au lieu
  de devenir invisible** (fix n°1), puis `visibilitychange`/`pageshow` → **la vue redevient riche
  sans aucune interaction du joueur** (fix n°2) ; + `spriteUsable` sur les 3 cas (bitmap jeté / pas
  encore décodée / décodée). Build 292→293.
  Changement 14.10 : **FIX « l'affichage des îles est soit noir soit bloqué » — le canvas se répare
  tout seul.** `SAVE_VERSION` INCHANGÉ (rendu seul). Retour testeur avec 2 captures : HUD, horloge,
  bilan élec. et barre d'actions parfaitement rendus (DOM), mais la **zone de l'île entièrement
  NOIRE** ; une autre capture de la même partie montre l'île normalement. **Non reproduit en
  laboratoire** malgré : parcours de MAJ réel (partie build 289 → save v30 → rechargement en v31 :
  0 erreur, canvas à 98,8 %), balayage **7 îles × couche logique ON/OFF** avec ~55 bâtiments par île
  + les 13 éléments logiques + foreuse en opération + Collisionneur réparé (14 relevés, 0 erreur
  console, `Archipel frame error` jamais émis). **Diagnostic** : les deux branches de terrain ont
  déjà un repli `TERRAIN_COLORS` → un sprite non décodé donne une tuile COLORÉE, jamais du noir ; et
  `clampPan` empêche de sortir de la grille. Reste la cause classique en WebView Android : la
  **surface de rendu du canvas est perdue** (pression mémoire, retour d'arrière-plan, reset GPU).
  Deux pièges qui la rendaient DÉFINITIVE : (a) `contextlost`/`isContextLost()` **n'existent pas sur
  un contexte 2D avant Chromium 105** → `ctxValid()` répond « valide », le jeu dessine dans le vide
  et la vue reste noire À VIE (la boucle et le tick continuent → l'horloge et le bilan élec. bougent,
  exactement ce que montrent les captures) ; (b) même quand l'événement existe il n'est pas toujours
  émis. **Correctif : on ne teste plus l'API, on teste le RÉSULTAT.** Nouveau `canvasLooksBlank()`
  (~400 points échantillonnés via un seul `getImageData` ; un canvas dont TOUS les pixels sont
  identiques est vide — une vue d'île, même 100 % océan, a de la texture) + `checkBlankCanvas()`
  appelé (1) par le chien de garde **une fois sur 5 (~3 s)** et (2) **immédiatement au retour
  d'arrière-plan** (cause n°1). Escalade en 2 temps : 1er constat → `layout()` + `dirty` + `start()`
  (simple frame ratée) ; 2e constat consécutif → **l'élément canvas est RECRÉÉ** (`setCanvasKey`,
  seul remède quand la surface est morte), plafonné à 8 recréations par session. Gardes anti-faux
  positif : on ne juge JAMAIS pendant le splash, le rattrapage hors-ligne (`catchingUp`) ou le choix
  de mode, ni si la grille n'est pas prête, ni si `getImageData` échoue. Ajout aussi de
  **`contextrestored`** (→ `layout()` + redessin) et retrait propre des deux listeners au cleanup ;
  `contextlost` gardait déjà son `preventDefault()` (sans lui le contexte n'est jamais restauré).
  Validé : `node --check` (7 blocs) + Chromium **3 suites, 76 assertions, 0 erreur JS** dont une
  suite dédiée : surface morte SIMULÉE (`drawImage`/`fillRect`/`fill`/`stroke`/`fillText`
  neutralisés + effacement) → canvas vide constaté, **la boucle continue de tourner**, puis retour
  des primitives → **la vue revient SANS aucune interaction du joueur** ; `contextlost` intercepté
  puis `contextrestored` → vue dessinée ; **non-régression : aucun remontage parasite** (1 seul
  canvas dans le DOM après 7 s de jeu normal). ⚠ Deux assertions de la suite 14.07 devenues FAUSSES
  ont été corrigées (elles vérifiaient l'ancienne règle « la pause GÈLE le démarrage du
  Collisionneur », remplacée en 14.08 par « la pause le remet à ZÉRO »). Build 291→292.
  Changement 14.09 : **Mine Tungstène — densification DIRECTE en V4 au Nv.11 (sprite du pack v2.8).**
  `SAVE_VERSION` INCHANGÉ (nouvel id additif). Retour utilisateur juste après le 14.08 : la chaîne
  tungstène **saute V2 ET V3** — la Mine Tungstène V1 densifie directement en **`mine_tungstene_v4`**
  (`TIER_NEXT.mine_tungstene` cap 9, `TIER_STEP` entrée **u10 = Nv.11**, même forfait que les autres
  mines V4 : **100 alliage de tungstène + 10 pièce de précision**), exactement comme l'or et l'uranium
  sautent le V2. Elle prend le sprite `mine_tungstene_v4` (+ son anim) du pack v2.8, déjà inliné et
  jusque-là inerte. **Recette CONSERVÉE** (16 acide → 1 tungstène + 8 pierre) ; la conso **PLATE de
  512 kW du V1 devient une SIGMOÏDE 144 → 1152** selon la règle des paliers (pic V1 ×1,125 = 576 →
  plancher 576/4 = 144, plafond 576×2 = 1152, ratio 1→8) — à l'entrée u10 le pic vaut donc ×1024, dans
  la continuité du niveau 9 du V1. Débloquée avec les autres mines V4 au **nœud 43** (puzzle P3) et
  ajoutée au groupe **Tungstène** de la barre d'outils. ⚠ Les sprites `mine_tungstene_v2/v3` du pack
  restent donc **inutilisés** : il n'y a AUCUN palier intermédiaire (choix explicite de l'utilisateur).
  Validé : `node --check` (7 blocs) + Chromium **46 assertions, 0 erreur JS** (les 5 nouvelles portent
  sur le tungstène : V1 → V4 direct, entrée u10, forfait, sprite du pack, recette conservée, sigmoïde
  144 → 1152, coût cumulé de la chaîne). Build 290→291.

  Changement 14.08 : **PATCH 9 retours + 15 NOUVEAUX BÂTIMENTS (mines V4, chaîne or/silicium/processeur V2,
  chaîne nucléaire V2, quantique) selon `Classeur1.xlsx` + pack sprites v2.8.** `SAVE_VERSION` **30→31**
  (une seule migration : la polarité par défaut de l'ACTIONNEUR s'inverse → on retourne `actInvert` des
  actionneurs existants pour que les montages en cours fassent EXACTEMENT la même chose).
  (1) **CHALEUR île 6 « rien n'est évacué en haut »** : le mécanisme est **CORRECT** (probe : chaleur locale
  5 → 0,904 MJ à 1,024 MJ/s, **tampon de la cage 10 → 4,88 MJ** avec une tour alimentée en eau sur un conduit
  élévateur-lié). C'est un problème de **DIAGNOSTIC** : rien ne disait si le conduit de SURFACE touche la tuile
  élévateur. Deux indicateurs ajoutés : panneau **Conduit** → ligne « Élévateur : relié à la cage / ⚠ non relié
  à la cage » (+ « Tampon de la cage X / cap » quand il est relié) ; panneau **Élévateur** → ligne
  « 🔥 Tampon de chaleur X / cap » (rouge à saturation = le souterrain surchauffe). (2) **CÂBLES LOGIQUES non
  connectés aux jonctions ni à la vanne** : le masque de connexion du câble logique n'acceptait que
  capteur/actionneur/porte/émetteur → il ignorait **`logicJunction` ET `logicValve`** (mécaniquement tout allait
  bien : la jonction est un CONDUCTEUR, la vanne lit l'OU de ses faces). Le masque accepte désormais **tout
  élément logique** (`isLogicId`). Au passage : la **jonction** affiche enfin son état RÉEL par axe (variantes
  d'art `_ns` / `_eo` / `_both`) et la **vanne** son art dédié `logic_vanne_collisionneur_<dir>_<0|1>` (v2.8,
  repli sur `logic_vanne`). (3) **COLLISIONNEUR — conso OSCILLANTE + séquence manuelle** : la puissance n'est
  plus fixe, c'est une **sinusoïde de période 600 s** (`COLLIDER_START`) entre le plancher 1 MW et le plafond du
  palier (même forme que les autres sigmoïdes) ; pendant les 10 min de démarrage l'oscillation est écrasée sur
  le plancher puis prend son amplitude. Nouvel état **`ready`** : à la fin du démarrage la machine est PRÊTE mais
  **n'émet AUCUN code** tant que le joueur n'a pas cliqué **« ▶ Lancer la séquence »** (nouveau bouton du
  panneau, drapeau persisté `launched`). ⚠ **Changement de règle du 14.07** : une **PAUSE remet le démarrage à
  ZÉRO** (et annule la séquence) — ce n'est plus un gel ; idem pour une **pénalité**. Un simple manque de courant,
  lui, ne perd PAS le lancement (il fait juste reculer le démarrage). (4) **ÉLÉVATEUR — mode de répartition au
  choix** : `game.elevatorMode` (persisté, défaut `priority`) + helper `elevatorAllocate(dems, budget, mode)` →
  **prioritaire** (ordre strict constr. → sortants → intrants, comportement d'avant), **équitable** (parts égales
  entre les catégories qui demandent, résidu redistribué en 3 passes) ou **proportionnel** (prorata). Switch
  3 boutons dans le panneau Élévateur. (5) **ACTIONNEUR** : le **sens disparaît** de son panneau (la mécanique
  n'a JAMAIS utilisé `actDir` — il lit déjà l'OU de ses 4 faces) et la **polarité par défaut s'inverse** :
  **signal 0 → bâtiment DÉSACTIVÉ** (il tourne quand le signal vaut 1). Libellés du sélecteur devenus explicites
  (« 0 → désactivé » / « 1 → désactivé »). Migration < 31 : `actInvert` retourné sur tous les actionneurs
  existants (les deux chemins de chargement). (6) **ANNULER UN REMBLAI** : helper `isFilledTile` (tuile dont
  `baseTerrain === 'water'` comblée par le joueur) ; toucher une telle tuile VIDE ouvre le panneau d'extension en
  mode **annulation** → bouton « ↩ Annuler le remblai » (surface : la tuile redevient de la mer, le compteur
  d'extensions est décrémenté et le coût de la DERNIÈRE extension rendu au port) / « ↩ Reboucher le tunnel »
  (île 7 : aucun remboursement, la foreuse a été consommée). Refus si un bâtiment, un réseau ou un élément
  logique s'y trouve encore. (7) **CARRÉS EMOJI → SPRITES** : les carrés de couleur `.ip-swatch` des en-têtes
  (indiscernables d'un glyphe manquant) sont remplacés par de vrais sprites — **tuile de mer** pour l'extension,
  terrain accidenté pour la réparation, cage pour l'élévateur, **sprite du Collisionneur** (ruine ou palier
  réparé) pour son panneau. (8) **`he4` → « Hélium », `he3` → « Hélium 3 »** (`RES_SHORT` ; les LOCALES ne
  contiennent pas ces clés → aucune réécriture par `applyToData`). (9) **« La distillerie bloque les tuyaux ? »**
  → **NON, vérifié** : probe sur une vraie île — tuyau · tuyau · **distillerie** · tuyau · tuyau donne **UN SEUL
  réseau** (`networkId` identique de part en part, règle de traversée 10.59, idem raffinerie), alors qu'un four à
  charbon (aucune E/S tuyau) coupe bien le réseau. Aucun correctif : ce qui peut ressembler à un blocage, c'est
  (a) la **fusion** qui additionne la demande des deux côtés sur UN débit (saturation plus rapide, visible dans
  le panneau réseau) et (b) la distillerie **V2** qui exige de l'**oxygène**, gaz produit seulement par le
  Séparateur d'Air et **non transitable** entre îles. (10) **15 NOUVEAUX BÂTIMENTS** (coûts/effets = `Classeur1.xlsx`,
  art = pack v2.8 + les 6 sprites V2 « île 6 » livrés dès 13.59 et restés inertes) : **6 mines V4**
  (`mine_fer_v4`/`mine_charbon_v4`/`carriere_v4`/`mine_cuivre_v4`/`mine_or_v4`/`mine_uranium_v4` — paliers des V3,
  sigmoïde ×2 du V3 `0,0625 → 0,5`, forfait **100 alliage tungstène + 10 pièce de précision**, entrée **u30**
  sauf or/uranium **u20** puisqu'ils sautent le V2 ; `carriere_v4` → art `mine_pierre_v4` via
  `BLD_SPRITE_OVERRIDE`) ; **chaîne or/silicium/processeur V2** (`fonderie_or_v2` 18→144 kW,
  `raffineur_silicium_v2` 36→288, `fab_processeur_v2` 288→2304 — **règle des V2 13.59** : pic V1 ×1,125 puis
  plancher = pic/4 et plafond = pic×2, ratio 1→8) ; **chaîne nucléaire V2** (`broyeur_uranium_v2` 18→144,
  **`centrale_enrichissement_v2`** 72→576 avec **−25 % de yellowcake (2 → 1,5) + 0,1 plutonium/s incorporé**,
  **`centrale_nucleaire_v2`** — même moteur mais nouveau flag **`noHeat`** → **elle n'émet plus AUCUNE chaleur**,
  ni tour ni conduit, et aucun trip possible faute de `heatCap`) ; **quantique** —
  **`stabilisateur_quantique`** (nouveau flag `quantumStab`, 1/île, 8192 kW à plat au Nv.1 ; helper
  `quantumStabFactor` = **0,95^(niveau+1)**, MULTIPLICATIF, appliqué au profil sigmoïde de TOUS les bâtiments de
  son île → Nv.1 −5 %, Nv.2 −9,75 %, Nv.3 −14,3 % ; `bld.qStab` est relu par `nominalPower`/`minPower` donc les
  plages « min → max » affichées suivent aussi ; un stabilisateur en pause / éteint par la logique / non câblé ne
  stabilise rien, et le `min` sur plusieurs unités empêche tout empilement jusqu'à zéro), **`antenne_v2`**
  (palier de l'antenne ; nouveau flag **`antRadius: 2`** → **24 cases influencées (5×5)** au lieu de 8, modes et
  courbes d'amélioration INCHANGÉS ; la boucle d'antenne construit désormais ses offsets depuis le rayon) et
  **`usine_moteur_quantique`** (île 6 ; sigmoïde **4096 → 32768 kW** = pic 32 MW, chaleur « règle habituelle »
  `HEAT_PER_MW × MW consommés`, recette 1 ordi quantique + 1 élém. moteur + 10 pièce précision + 15 câble supra
  + **8192 azote** → **0,1 `moteur_quantique`/s**). **Nouvelle ressource `moteur_quantique`** (t5, porteur route,
  icône générée par recoloration d'`item_ordinateur_quantique` — le pack v2.8 n'en livre pas). (11)
  **BRANCHEMENTS DES PUZZLES ENFIN RÉELS** : les ids « préparés » du 14.01 pointaient dans le vide → nœud **39**
  (P1, 100 confirmations) = chaîne or/silicium/processeur V2 (l'`mine_or_v2` prévu n'existe pas : l'or saute le
  V2 par design), nœud **41** (P2) = chaîne nucléaire V2, nœud **43** (P3) = stabilisateur + antenne V2 +
  **usine moteur quantique** (l'`usine_moteur_nuc_v2` prévu est remplacé : l'Excel demande le moteur QUANTIQUE)
  + les 6 mines V4. **Plus aucun id de déblocage orphelin** (assertion E2E). Coûts de réparation du Collisionneur
  repris de l'Excel : **P1** 20 000 alliage + 20 000 supra + 10 000 élém. moteur, **P2** 1 000 ordi quantique +
  30 000 pièce précision, **P3** 1 000 moteur quantique. (12) **3 valeurs de l'Excel corrigées sur l'existant** :
  **Extracteur Souterrain 0 → 512 kW**, **Séparateur Cryogénique pic 1024 → 2048**, **Data Center 0 → 1024 kW**.
  ⚠ **NON traité, signalé** : les sprites `mine_tungstene_v2/v3/v4` du pack restent inutilisés (la Mine Tungstène
  n'a qu'un V1 ; créer sa chaîne complète n'était pas demandé) ; l'Excel écrit « 8196 kW », implémenté **8192**
  (échelle binaire du jeu, comme la foreuse en 14.06). Validé : `node --check` (7 blocs) + Chromium **41
  assertions, 0 erreur JS** — les 15 defs + leurs sprites, barèmes exacts (mines V4, forfaits, recettes,
  puissances de l'Excel), 0 id orphelin ; **stabilisateur par le moteur réel** (facteurs 0,95 / 0,9025, pic
  1024 → 972,8 kW, sans effet en pause) ; **antenne 8 → 24 cases** ; **Collisionneur** (1 200 ticks : état
  `ready` sans code émis, oscillation plancher↔plafond sur une période complète, lancement → codes, pause ET
  pénalité → démarrage à zéro + séquence annulée) ; les **3 modes d'élévateur** (10·5·0 / 5·5·5 / 5·5·5 +
  redistribution du résidu 2·6,5·6,5) ; **actionneur** (signal 0 → désactivé, polarité inversée → ne coupe plus) ;
  détection de tuile remblayée. Build 289→290.
  Changement 14.07 : **PATCH 5 retours — raccord route de la foreuse, barème de creusement ré-ancré sur
  la DISTANCE à l'élévateur, Collisionneur (vanne déplacée + sprite réparé + interrupteur), double badge
  de pause.** `SAVE_VERSION` INCHANGÉ (`collider.enabled` = champ additif ; absent = allumé).
  (1) **FOREUSE — la route ne se raccordait pas VISUELLEMENT** (retour + screenshot) : sa sortie (pierre)
  est injectée AU TICK et n'est pas déclarée dans la def → `buildingConnectsCarrier('foreuse','road')`
  répondait faux, donc ni branche de route vers elle ni stub sous elle. La mécanique, elle, marchait
  (elle évacuait bien). Cas explicite ajouté pour `driller` + `road`. (2) **Barème RÉ-ANCRÉ sur la
  distance de Tchebychev à l'élévateur** (la spec disait « le cercle de 4096 commence à deux cases
  autour de l'élévateur, l'élévateur serait à 1024 ») : `drillLayer` renvoie désormais la DISTANCE BRUTE
  (avant : distance+1). `DRILL_MAX_POWER` devient **1024 kW sur la tuile élévateur**, ×2 par cercle →
  **cercle 2 = 4096 kW, 3 = 8192, 4 = 16384** (valeurs inchangées, seul l'ancrage bouge d'un cran) ;
  coût de la foreuse ×1 / ×2 / ×4 sur ces mêmes cercles. (3) **Évacuation indexée sur la puissance**
  (`DRILL_KW_PER_STONE = 64`, helper `drillStoneAt`) : **64 pierre/s au cercle 2**, ×2 par cercle (128,
  256) — le débit fixe de 8/s du 14.06 est remplacé. ⚠ **Conséquence de jeu VÉRIFIÉE et voulue** :
  l'élévateur de base (16 u/s) ne peut évacuer que le quart d'une foreuse de cercle 2 → elle creuse à
  25 % (20 min au lieu de 5) tant qu'on ne l'améliore pas. C'est exactement la règle « si l'élévateur est
  bridé, la foreuse est ralentie ». (4) **COLLISIONNEUR — vanne d'entrée en HAUT À DROITE**
  (`syncColliderChildren` : `put(minR, maxC)` ; l'émetteur reste en bas à gauche). Une partie en cours a
  sa vanne à l'ancienne place → la passe de nettoyage retire toute vanne **mal placée** (sinon le
  landmark en porterait deux). (5) **Sprite du Collisionneur à la 1re réparation** : 18 nouvelles
  tranches `tile_i6_collisionneur_p{1,2,3}_{0..5}` générées depuis les sprites officiels
  `bat_collisionneur_p1/p2/p3` (64×96 → ROTATE_270 → 96×64 → 3×2, **exactement la découpe de la ruine**,
  vérifiée au pixel sur `tile_i6_collisionneur_0..5`). Le draw choisit ruine / P1 / P2 / P3 selon
  `colliderRepaired` + `colliderPalier` (repli sur la ruine si une tranche manque). (6) **Interrupteur
  marche/arrêt** (`collider.enabled`, bouton dans le panneau du landmark) : éteint, il ne tire plus un
  seul kW (`state='off'` → sa demande n'est plus ajoutée à l'île) et son **compte à rebours de démarrage
  est GELÉ** — rallumer reprend où on en était (≠ pénalité des codes faux, qui remet les 10 min à zéro).
  (7) **DOUBLE BADGE DE PAUSE** (screenshot : une pastille bleue ET une rouge sur la même tuile) : un
  bâtiment éteint par un actionneur porte `ui_pause_logique` (bleu, coin haut-gauche) ET recevait
  l'icône d'état `etat_arret` (rouge, haut-droite) via `statusSpriteKey('logic')`. Désormais **UNE SEULE
  pastille** : `drawDeficitIcon` sort si le motif est `logic`, et le badge bleu ne s'affiche que si la
  logique est la cause ACTIVE (`discReason === 'logic'`) — une pause MANUELLE par-dessus un actionneur
  ne montre donc que le badge d'arrêt. (8) **AUDIT EMOJI** (demande « lister les carrés ») : les
  **4 familles CSS** (`ArchipelPixel`, `Bebas Neue`, `Barlow Condensed`, `DM Mono`) pointent toutes vers
  le **MÊME pixel-font de 116 glyphes** (ASCII + accents FR + `— … → ─ ═ ▴ ▾ ◆ ○ ♻ ⚠ ⛔ ✓ ❌ ⬆ ⬇`).
  **Tout le reste tombe en police de secours** : 115 caractères distincts / 1310 occurrences, dont
  ~356 dans les panneaux, 46 dans les toasts, 59 dans les astuces et 845 dans les LOCALES. Les plus
  exposés : `§ − ÷ ≥ ≤ ≈ ≠ ∞ • α β γ ₂ ᵉ ʳ` (aucune police emoji ne les couvre), les accents ES/DE
  (`ó í á ñ ß Ö Ü ¡ ¿`) des traductions, et les emoji récents `🪨 🪫 🛗` (Android < 12). Inventaire
  complet livré dans la réponse ; AUCUN remplacement effectué (le patch demandait de lister).
  Validé : `node --check` (7 blocs) + Chromium **5 suites, 92 assertions, 0 erreur JS** — raccord
  route/câble/tuyau de la foreuse ; barème (4096/8192/16384/32768 kW, coûts ×1·×2·×4·×8, 64·128·256
  pierre/s, débit = puissance/64) ; **parcours réel** (fiche → « Percer le mur » → pierre au port île 6
  au débit du cercle → 1 s/tick élévateur non limitant → chantier concurrent qui fige → coupure de
  courant → mur percé + foreuse disparue) ; Collisionneur **par la vraie UI** (vanne haut-droite, ancienne
  retirée, émetteur inchangé, 18 tranches présentes et distinctes de la ruine, tap → panneau → clic réel
  sur « Éteindre » → 0 kW + timer gelé → rallumage qui reprend) ; badge unique dans les deux cas ;
  round-trips de sauvegarde. Build 288→289.
  Changement 14.06 : **FOREUSE refaite selon la SPEC D'ORIGINE (fournie après coup par l'utilisateur) —
  machine à usage unique qui creuse un mur en 5 min, extrait 8 pierre/s par l'élévateur, monte de 0 à
  8192 kW, s'arrête 30 s quand le courant manque, et DISPARAÎT en laissant une tuile constructible.**
  `SAVE_VERSION` INCHANGÉ (`pl.dg` étendu d'un champ `st` optionnel ; `dig` retiré — il valait toujours
  vrai). ⚠ **Remplace intégralement la foreuse du 14.05** (30 s, coût d'extension, prospection de sol).
  (1) **Usage unique** : au bout des 5 min (`DRILL_TIME = 300`), le mur visé devient du **sol de tunnel
  constructible** et la **foreuse est consommée** — sa tuile est libérée. C'est « l'extension de terrain
  en mer, en plus sophistiqué » : **on ne paie PLUS de coût d'extension** (`extensionCost`/
  `extensionsCount` ne concernent plus l'île 7), le coût EST la foreuse. `tryDrill` ne débite donc RIEN
  au démarrage, et `tryExtend` sur l'île 7 délègue entièrement à la foreuse (sans gate sur la techno de
  remblai : la foreuse EST l'outil). (2) **8 pierre/s pendant l'opération**, injectés dynamiquement comme
  sortie route (`effOutputs` passe en `let`) → ils remontent par l'ÉLÉVATEUR comme n'importe quel sortant,
  donc **élévateur bridé = foreuse ralentie** (le fix de régime du 14.05 s'applique tel quel). Corollaire
  voulu : sans route jusqu'à l'élévateur, elle ne creuse pas. (3) **Conso 0 → max en 5 min**,
  **proportionnelle à l'AVANCEMENT** (pas au temps) : au ralenti « elle monte très doucement » au lieu de
  s'arrêter, exactement comme demandé. (4) **Manque de courant → arrêt de 30 s** (`DRILL_STALL`) puis
  reprise **au même point** : la coupure est détectée via `active === false && discReason === 'power'`
  (posé par `cutBld`), le compteur `drillStall` met la conso à 0 (le réseau peut respirer) et gèle
  l'avancement. ⚠ Deux pièges corrigés au passage : (a) le régime de la foreuse **n'utilise plus
  `pwrAvg`** (le duty-cycle lissé l'aurait fait creuser au ralenti ~20 s après chaque reprise — le manque
  de courant est en TOUT OU RIEN) ; (b) le garde `if (!effOutputs && power <= 0) continue;` sautait la
  foreuse en pause (ni sortie ni conso) → **son compte à rebours ne s'écoulait jamais** : exception
  ajoutée pour une foreuse en opération. (5) **COUCHES autour de l'élévateur** (`drillLayer`, distance de
  Tchebychev, 1 = la tuile élévateur) : coût de la foreuse **×2 par couche** (`drillCostMult` : 2e cercle
  ×1, 3e ×2, 4e ×4) et puissance max **×2 par couche** (`drillPowerAt` : **4096 / 8192 / 16384 kW**). Le
  coût est appliqué à la POSE (`drillPlaceCost` dans `tryPlace`), puisque la foreuse est consommée.
  (6) **Poche d'He3 = 10 % par mur percé** (`DRILL_POCKET_CHANCE`) — la **prospection d'une tuile de sol
  a disparu** (plus de `mode: 'drill'` au tap, plus de `drillCost`/`drillsCount` en jeu ; les champs
  restent lus par les vieilles saves). (7) **UI** : plus de compteur de murs percés — la fiche affiche la
  **couche** (« cercle N · coût ×N · X kW »), la ligne « 5 min · 8 pierre/s · 0 → max », l'avancement
  (ou « reprise dans Ns » pendant un arrêt) et le rappel que la foreuse disparaît. La ligne « Élec. »
  générique est masquée pour la foreuse (sa conso dépend de la couche). Tuto réécrit. Validé :
  `node --check` (7 blocs) + Chromium **4 suites, 69 assertions, 0 erreur JS** — barème exact
  (300 s / 8 pierre/s / 30 s / 10 % ; puissances 4096·8192·16384·32768 ; coûts ×1·×2·×4·×8) ; **parcours
  réel par l'UI** : foreuse posée (coût = base × couche), **0 kW au repos**, tap → fiche (couche, durée,
  cible « mur de roche », plus de compteur) → clic « Percer le mur » → **rien débité**, conso qui monte
  depuis ~0, **8 pierre/s exactement au port de l'île 6**, avancement 1 s/tick ; **chantier concurrent →
  la foreuse n'avance plus** (élévateur préempté) puis repart ; **coupure de courant → arrêt de 30 s,
  0 kW, avancement gelé, compte à rebours qui s'écoule** ; fin d'opération → **mur percé + foreuse
  disparue + 0 kW + notification** ; round-trip de sauvegarde par rechargement réel (creusement, mur
  visé, pause de courant, drapeau ∞). Build 287→288.
  Changement 14.05 : **PATCH 6 retours — élévateur qui bride VRAIMENT les machines, batteries (badge
  fantôme + charge homogène), foreuse (sens visible, forage de mur, 0 kW au repos), réseau infini
  remboursé puis gratuit.** `SAVE_VERSION` INCHANGÉ (3 champs additifs optionnels : `pl.dg`
  = forage en cours, `netInfPaid` = forfait ∞ acquis par île/type ; `elevFac` transitoire).
  (1) **ÉLÉVATEUR — le bridage entre enfin dans le RÉGIME** (retour « quand on construit, mes machines
  fonctionnent alors que cela ne devrait pas »). `elevInFac`/`elevOutFac` ne multipliaient QUE les
  écritures au port : une machine privée d'élévateur — cas typique d'une **CONSTRUCTION, qui préempte
  100 % du débit** (priorité n°1 depuis 13.89) — continuait de tourner à plein régime (animée, tirant
  toute son électricité) et pouvait même **produire dans un pool tuyau LOCAL (non bridé) sans rien
  prélever au port** = matière créée du néant. La passe élévateur replie désormais le facteur dans
  `a.regime` (+ `bld.regime`, `bld.elevFac`) PUIS remet `elevInFac`/`elevOutFac` à 1 pour ne pas
  l'appliquer deux fois — la demande étant proportionnelle au régime, **les totaux déplacés sont
  identiques**, seule la vitesse des machines change. Régime à 0 → arrêt FRANC : `discReason`
  **`elevbusy`** (nouveau libellé « élévateur saturé (construction et travaux servis en premier) »,
  distinct de `elevator` = non relié) et **retrait de `energyConsumers`** (même convention qu'un
  bâtiment totalement privé d'intrants : il ne tire plus de courant). Bridage partiel → la fiche
  nomme la cause (« élévateur saturé »). `actives` transporte désormais `bld`. Au passage,
  `drawBuilding` : quand ni les intrants ni l'élec. ne sont en cause (réseau saturé, élévateur), la
  petite icône de déficit est celle des **intrants** et non celle du courant. (2) **BATTERIE — badge
  d'arrêt FANTÔME** (retour « batterie avec badge pause ? », screenshot) : la branche `accumulator` de
  `tickIsland` faisait `continue` **sans jamais remettre `bld.active` à true** → une batterie passée
  par la construction souterraine (qui pose `active=false`, motif `building`) gardait le badge
  `etat_arret` À VIE une fois construite. L'état est maintenant réinitialisé dans la branche : une
  batterie n'a ni intrant ni sortant, elle est **opérationnelle dès qu'elle touche un câble** ; sinon
  **déconnectée avec le motif `wire`** (ce qui explique enfin une batterie figée à 0 %). `pwrAvg = 1`
  → plus d'icône ⚡ trompeuse. (3) **BATTERIES — charge/décharge HOMOGÈNES sur l'île** (retour « une
  à 100 %, l'autre à 0 % ») : la boucle était **séquentielle** (la 1re se remplissait entièrement
  avant que la 2e reçoive quoi que ce soit). Charge répartie **au prorata de la place libre**,
  décharge **au prorata du stock**, avec 4 passes de résidu pour redistribuer ce qu'une batterie
  saturée n'a pas pu prendre → toutes convergent vers le même % et y restent. **Totaux et rendements
  inchangés** (0,8 en V1, 1 en V2 `chargeLossless`, bornes de débit du câble conservées) ; une
  batterie seule se comporte exactement comme avant. (4) **FOREUSE — 3 retours.** (a) **Le SENS est
  visible sur la carte** : flèche vectorielle vers la face visée (`drillDir`), **ambre au repos,
  jaune vif pendant un forage**. (b) **On peut creuser un MUR** : la cible peut être de la roche
  (`water`) → percée en **sol de tunnel**, exactement le résultat du remblai et **avec la même
  économie** (`extensionCost` + `extensionsCount[7]`) ; la fiche annonce « mur de roche — à percer »
  et le bouton devient « ⛏ Percer le mur ». `tryExtend` sur l'île 7 **délègue à `tryDrill`** → un
  seul chemin (le remblai souterrain exigeait déjà une foreuse adjacente). (c) **0 kW tant qu'on n'a
  pas démarré** : le forage devient une **opération TIMÉE** (`DRILL_TIME = 30 s`, état persisté
  `bld.drilling {r,c,rem,tot,rate,dig}`) ; `basePower` est **forcé à 0 hors opération** (la foreuse
  n'entre même plus dans `energyConsumers`) et ses **512 kW ne sont tirés que pendant le forage**, qui
  **avance au régime électrique** (pas de courant → barre figée, « sans courant » dans la fiche).
  Le résultat (gisement révélé / mur percé) est appliqué par le tick, qui empile `game.drillNotify` :
  la boucle `frame` fait le `rebuildNetworks` + le toast (on ne reconstruit JAMAIS les réseaux au
  milieu d'un tick, les `routeIn`/`routeOut` déjà résolus pointeraient dans le vide). Barre de
  progression jaune sur la tuile (helper `drawWorkBar` réutilisé) + lignes « Forage en cours » et
  « Durée · élec. » dans la fiche. Tuto `foreuse` réécrit. (5) **RÉSEAU INFINI — remboursement puis
  gratuité** : passer un réseau en ILLIMITÉ **rembourse l'intégralité des ressources investies dans
  ses paliers** (nouveau `networkInvested` = Σ coût unitaire des crans V1→niveau × tuiles) et
  **ramène son niveau à 1** — les paliers ne servent plus à rien à débit infini, et le niveau 1 rend
  le remboursement **non rejouable** (re-limiter puis re-passer en ∞ ne rend plus rien). Une fois le
  forfait payé, nouveau drapeau persisté **`game.netInfPaid[île][type]`** → **tous les ∞ suivants de
  ce type sur cette île sont GRATUITS** (bouton ∞ proposé **dès le niveau 1** dans ce cas, inutile de
  refaire monter les paliers pour se les faire rembourser) et **étendre un réseau ∞ ne coûte plus
  aucun rattrapage**. ⚠ Drapeau par île **ET par type** : payer l'infini pour la route ne donne pas le
  câble (un drapeau global offrirait 3 réseaux pour le prix d'un). Sous-libellé du bouton : « gratuit »
  et « ↩ <remboursement> ». Validé : `node --check` (7 blocs) + Chromium E2E **3 suites, 56 assertions,
  0 erreur JS** — batteries (3 accus : charge ET décharge au même niveau au 1e-3 près, batterie câblée
  sans badge, non câblée → motif `wire`) ; réseau ∞ **par la vraie UI** (tap canvas → panneau câble →
  bouton ∞ armé/confirmé : illimité, niveau ramené à 1, 10 000 câble irr. débités, **paliers rendus au
  port**, drapeau posé ; ∞ suivant gratuit y compris sur un réseau neuf ; route de l'île 1 et câble de
  l'île 2 NON offerts) ; souterrain réel (presse UHP alimentée à **régime 0,9375 = 15/16 du débit**,
  puis chantier → **flux 16/16 en construction, machine ARRÊTÉE avec le motif `elevbusy`**, chantier
  fini → elle repart) ; foreuse **par la vraie UI** (fiche au tap, 4 boutons N/E/S/O, cible « mur de
  roche — à percer », clic réel sur « Percer le mur » → forage démarré, coût d'extension débité au port
  île 6, **0 kW au repos → 512 kW en forage**, mur percé au bout de 30 s, notification empilée, retour
  à 0 kW) ; **round-trip de sauvegarde par RECHARGEMENT réel** (forage en cours + direction + drapeau ∞
  restaurés) ; non-régression batterie seule (charge = surplus × 0,8 exact, capacité 8192).
  Build 286→287.
  Changement 14.04 : **FIX — le Collisionneur puisait l'électricité de l'île 6 SANS avoir été réparé.**
  `SAVE_VERSION` **29→30** (correctif de save obligatoire). **Cause** : la migration 14.01 (< 29) faisait
  `set(38, keep(old[33]))` — pour ne pas retirer au joueur ses 3 portes de base, elle confirmait le
  nœud **38 « Réparation du Collisionneur I »** dès que l'ANCIEN nœud 33 « Circuit Logique » (précoce et
  bon marché) l'était. Or `colliderRepaired()` ne teste QUE le nœud 38 → le Collisionneur démarrait tout
  seul et ajoutait sa demande géante à l'île 6 (1 MW au 1er tick, sigmoïde jusqu'à **32 MW** au palier P1)
  alors que la livraison de réparation (10 000 × 3) n'avait jamais été faite. **Partie NEUVE non touchée**
  (tous les nœuds `locked` → conso 0, vérifié). **Correctif** : (1) nouveau champ persisté
  **`techTree.grantedBuildings`** (liste de bâtiments débloqués HORS nœud) + helper `isBuildingGranted`,
  lus par `unlockedBuildingSet` et `isBuildingUnlocked` → une migration peut conserver un déblocage acquis
  **sans** confirmer le nœud qui le donne aujourd'hui ; (2) la migration < 29 n'écrit plus les nœuds de
  RÉPARATION (38/40/42) : elle pousse les portes correspondantes dans `grantedBuildings` (ancien 33 ou 40
  → and/or/not, ancien 41 → nand/nor, ancien 42 → xor/xnor) ; les 3 PUZZLES (39/41/43) gardent leur
  héritage inchangé ; (3) **correctif < 30** pour les saves DÉJÀ migrées par 14.01 : tout nœud de
  `COLLIDER_REPAIR_NODES` confirmé alors que son `prereq` ne l'est pas est **impossible en jeu** → on
  retire la confirmation (le Collisionneur redevient en ruine) et on bascule ses portes dans
  `grantedBuildings`. Idempotent, et la chaîne LÉGITIME (37 confirmé puis 38) n'est jamais touchée.
  Validé : `node --check` (7 blocs) + Chromium : partie neuve → collisionneur `off`, demande île 6 = 0 ;
  **repro du bug** (38 confirmé, 37 `locked`) → `starting`, +1040 kW dès le 1er tick ; après correctif,
  les 3 saves forgées donnent — v29 corrompue → 38 `locked`, 3 portes conservées, `repaired false`,
  demande 0 ; v28 « Circuit Logique » → idem ; v29 légitime (37+38) → `repaired true`, collisionneur qui
  démarre (aucune régression) ; round-trip v30 (`grantedBuildings` sérialisé/restauré) ; 0 erreur JS.
  Build 285→286.
  Changement 14.03 : **PATCH 5 retours — améliorations souterraines étalées, foreuse (non améliorable,
  direction + démarrage manuel, tuto), antenne productivité déplafonnée.** `SAVE_VERSION` INCHANGÉ
  (3 champs additifs optionnels : `pl.cb.up`, `pl.cb.to`, `pl.dd`). (1) **Améliorations SOUTERRAINES
  étalées** : améliorer/densifier sur l'île 7 était **instantané** alors que la CONSTRUCTION descend par
  l'élévateur depuis 13.89. Le champ `bld.construction` gagne **`up`** (niveau visé) et **`to`** (id du
  palier pour une densification) ; `scheduleUnderWork(t, cost, up, toId)` (île 7, `kind build`, hors dev)
  pose `{rem = costUnits(coût), tot, rate, up}` au lieu d'appliquer le niveau. ⚠ **Différence VOULUE avec
  la construction** : une amélioration en cours **n'arrête PAS le bâtiment** (il tourne à son niveau
  ACTUEL, le nouveau s'applique quand la matière arrive) — la garde du tick et le fantôme du draw ne
  s'appliquent donc QUE si `up == null`. Rendu : le bâtiment est dessiné normalement + **barre de travaux
  VIOLETTE** (helper `drawWorkBar` extrait du fantôme, bleu pour la construction) ; fiche : nouvelle ligne
  **« Travaux 🚧 <cible> · X% · ~Ys »**. Priorité élévateur n°1 inchangée (construction ET travaux).
  `tryUpgrade`/`tryDensify`/**`upgradeAllSameType`** (le groupé aussi) passent par le même chemin ;
  `underWorks()` refuse d'empiler deux chantiers. **FIX au passage** : `UpgradePanel` lisait
  `game.port[currentIsland]` au lieu de **`portPool`** → au souterrain l'inventaire vu était VIDE et le
  bouton « Améliorer » **toujours grisé** (13.87 avait corrigé HUD/barre/InfoPanel/NetworkPanel, pas ce
  panneau) ; même correctif sur le panneau terrain (réparer/remblayer). (2) **Foreuse NON améliorable** :
  nouveau flag def **`noUpgrade`** (lu par `isUpgradable`) — elle n'a ni intrant ni sortant, améliorer ne
  faisait que **doubler ses 512 kW pour zéro effet** (piège à ressources). (3) **Foreuse pilotée depuis sa
  fiche** (nouveau flag def **`driller`**) : **4 boutons N · E · S · O** (même ordre d'affichage que la
  couche logique — `DIRS4` = [N, S, O, E] → 0, 3, 1, 2), ligne **Cible** (« sol de tunnel — forable » /
  « déjà foré » / « occupée » / « pas du sol de tunnel » / « hors du tunnel »), **Coût** en pastilles
  (port île 6), **Forages effectués**, et bouton **« ⛏ Démarrer le forage »** (grisé si la cible n'est pas
  forable ou si le port ne couvre pas). Réglage `bld.drillDir` persisté (`pl.dd`) ; handler `setDrillDir`.
  Le forage par tap sur la tuile voisine reste disponible (aucune régression). (4) **Tuto `foreuse`**
  (`GAME_TIPS`, `when` = foreuse débloquée) : où la poser, viser puis déclencher, coût ×4 par forage payé
  au port de l'île 6, **3 poches d'He3 cachées**, l'Extracteur se pose sur le gisement révélé, et elle
  n'est pas améliorable. Sans scène d'illustration (précédent `nuc_mix`). (5) **Antenne — mode
  PRODUCTIVITÉ déplafonné + le PRIX suit** : `antProdEffect` rabotait le **rendement** à +100 % (Nv.5,
  même symptôme que la vitesse en 14.02) → plafond **retiré** (Nv.1 +10 % · Nv.2 +20 % · Nv.3 +40 % ·
  Nv.4 +80 % · **Nv.5 +160 %** · Nv.6 +320 %…). Le **malus de vitesse suit sa propre courbe** : 2,5 %×f
  (Nv.1 −5 % · Nv.2 −10 % · Nv.3 −20 % · Nv.4 −40 % · **Nv.5 −80 %**) et **hard cap à −80 %**, atteint
  pile au Nv.5 — sans borne il passerait −160 % au Nv.6 (production négative). Et le plafond
  d'**`antElecBoost`** (+200 %, signalé en 14.02) **saute aussi** → la conso boostée reste le double du
  gain à tous les niveaux (Nv.5 ×1→×4,2) et la **chaleur** d'antenne en mode prod (HEAT_PER_MW × kW
  consommés en plus) **suit automatiquement**. Textes MAJ (astuce `antenne` inline + 4 LOCALES, astuce
  `antenne_modes`, commentaires moteur). i18n en/es/de des 15 nouveaux libellés. `__heat` étendu
  (`isUpgradable`, `costUnits`). Validé : `node --check` (7 blocs) + Chromium E2E **par la vraie UI** :
  courbes exactes (rendement `[10, 20, 40, 80, 160, 320]` %, malus `[5, 10, 20, 40, 80, 80]` %, élec.
  `[0,2 … 6,4]`, vitesse `[1,1 … 4,2]`) ; `isUpgradable('foreuse') === false` ; parcours réel île 6 →
  bouton souterrain → île 7 → outil Améliorer → tap sur la géothermie → **bouton Améliorer ENFIN actif** →
  `construction {rem: 3645, up: 1}`, niveau TOUJOURS 0, matière descendue à 1024 u/s, **niveau appliqué
  au bout de ~4 s** ; fiche foreuse RÉELLE (4 boutons dir → `drillDir` 0/3/1/2 exacts, cible recalculée,
  aucun bouton « Monter ») → clic RÉEL sur « ⛏ Démarrer le forage » → **compteur 0→1, tuile `drilled`,
  gisement révélé** ; tuto `foreuse` bien ouvert dans la file d'astuces ; round-trip de sauvegarde
  (`cb.up`/`cb.to`/`dd` restaurés à l'identique) ; 0 erreur JS. Build 284→285.
  Changement 14.02 : **FIX hardcap de l'antenne au Nv.5 + capteur « stock ≥ seuil » sur route/tuyau.**
  `SAVE_VERSION` INCHANGÉ (2 champs additifs optionnels dans `logicPlacements`). (1) **Antenne — plafond
  de VITESSE retiré** : `antSpeedMul(f) = 1 + min(1, 0,05×f)` rabotait le bonus à +100 % dès que
  `0,05×f > 1`, c'est-à-dire **exactement au Nv.5** (f = 2^(upg+1) = 32 → +160 % ramenés à +100 %) →
  améliorer l'antenne au-delà du Nv.4 ne servait plus à rien côté vitesse. Le `Math.min(1, …)` est
  **supprimé** → Nv.1 ×1,1 · Nv.2 ×1,2 · Nv.3 ×1,4 · Nv.4 ×1,8 · **Nv.5 ×2,6 (+160 %)** · Nv.6 ×4,2…
  (les niveaux 1-4 sont **inchangés**, seul le rabot disparaît). Les 8 sites d'affichage passent déjà
  par le helper (fiche antenne « Effet », aperçu d'amélioration, fiche du bâtiment boosté, badge carte
  « ×N ») → ils suivent automatiquement. Textes MAJ : tooltip du bouton **Vitesse** (clé i18n ET les
  3 traductions en/es/de), astuces **`antenne`** (inline + les **4 entrées LOCALES** — `applyToData`
  écrase l'inline) et **`antenne_modes`**, + 4 commentaires moteur. ⚠ **SIGNALÉ, non touché** :
  `antElecBoost` garde SON plafond (+200 %, atteint lui aussi au Nv.5) — la conso boostée cesse donc de
  monter alors que le gain de vitesse continue ; l'antenne devient strictement plus rentable à chaque
  niveau au-delà du Nv.5. À arbitrer (le patch demandé ne portait que sur la vitesse).
  (2) **Capteur `seuil` sur route/tuyau** (« un capteur sur une route/tuyaux peut lire un intrant et un
  seuil, ex. 1e7 acier = 1 ») : `sensorModesFor` renvoie désormais **`[sature, seuil]`** pour un support
  route/tuyau (câble inchangé = `elec`, le défaut reste `sature`). Deux réglages par capteur :
  **`sensorRes`** (ressource lue) et **`sensorSeuil`** (seuil) ; `evalSensor` sort **1 dès que le stock
  ≥ seuil**, **0 sans ressource choisie** ; **seuil 0 = « il y en a »** (stock strictement positif, pas
  de nombre magique). Nouveau helper **`logicNetStock(game, isl, net, res)`** : un réseau **relié au
  port** lit le PORT (les liquides d'un tuyau relié y sont flushés chaque tick depuis 10.82), un réseau
  isolé lit sa **citerne** (`net.pool`). Nouveau **`sensorSeuilResources(game, carrier)`** : ressources
  du bon porteur (`CARRIER_BY_RES`), débloquées par la recherche (ou en stock), triées comme
  l'inventaire. **Panneau de config** (mode `seuil` sélectionné) : grille **« Ressource lue »** en
  SPRITES (3 colonnes), champ **« Seuil »** (`NumField` → accepte `1e4`, virgule fr) et ligne
  **« Stock lu »** en direct. Persistance `logicPlacements` : **`sr`** / **`sq`** (sérialisés +
  restaurés dans les DEUX chemins de chargement) — champs additifs, aucune migration. i18n en/es/de
  des 7 nouveaux libellés. `__heat` étendu (`sensorSeuilResources`, `logicNetStock`). Validé :
  `node --check` (7 blocs) + Chromium E2E : courbe d'antenne exacte `[1,1 · 1,2 · 1,4 · 1,8 · 2,6 ·
  4,2]` + badges `×2,6` au Nv.5 ; capteur forgé sur une VRAIE route reliée au port → **1e7 acier = 1**,
  9 999 999 = 0, seuil 0 + stock 0 = 0 / stock 1 = 1, sans ressource = 0 ; **tap RÉEL** sur le capteur
  en couche logique → panneau avec les 2 modes, 5 ressources en sprite, champ seuil et « Stock lu » ;
  **clic RÉEL** sur « ling.fer » → `sensorRes` posé, saisie `1e4` → `sensorSeuil = 10000`, stock 12 345
  → **signal 1** ; round-trip de sauvegarde (`sm/sr/sq/sd` restaurés à l'identique après reload) ;
  0 erreur JS. Build 283→284.
  Changement 14.01 : **RESTRUCTURATION de l'arbre de recherche (nœuds 33-43) + 4 boutons de direction
  N/E/S/O dans le panneau logique.** `SAVE_VERSION` **28→29** (renumérotation → migration obligatoire).
  (1) **Panneau logique** : le bouton de rotation qui faisait tourner à l'aveugle est remplacé par
  **4 boutons À SUIVRE sur la largeur, ordre NORD · EST · SUD · OUEST** (grid 4 colonnes ; l'index
  moteur `DIRS4` étant `[N, S, O, E]`, l'ordre d'AFFICHAGE remappe vers 0, 3, 1, 2). La face courante
  est surlignée. (2) **Arbre restructuré (43 nœuds contigus)** : **33 = Batterie V2** (ex-39) ;
  **34 = Forage Profond** → la FOREUSE (elle venait de l'ancienne « Réparation du Collisionneur » — or
  la foreuse sert à trouver l'He3 qui ALIMENTE le Collisionneur : l'incohérence signalée depuis la
  phase 4 est enfin levée) ; la chaîne He3/Quantique/Data Center glisse de −1 (**35** Hélium, **36**
  Ordi Quantique, **37** Data Center) ; puis **3 RÉPARATIONS (livraison) alternées avec 3 PUZZLES
  (confirmations)** : **38** Réparation I → `porte_and/or/not`, **39** P1 (100) → *ensemble production
  or v2*, **40** Réparation II → `porte_nand/nor`, **41** P2 (1 000) → *ensemble prod nucléaire v2*,
  **42** Réparation III → `porte_xor/xnor` (toutes les portes), **43** P3 (10 000) → *stabilisateur
  quantique + antenne v2 + usine moteur nuc v2 + mines v4*. Les nœuds « Circuit Logique » et « Poser une
  porte logique » sont **SUPPRIMÉS** (les portes sont désormais la récompense des réparations).
  ⚠ **BRANCHEMENTS PRÉPARÉS** : les récompenses des 3 puzzles référencent des ids de bâtiments qui
  **N'EXISTENT PAS** (aucune def créée — demande explicite « préparer les branchements, ne pas créer »).
  C'est SANS RISQUE : `applyUnlocks` ne touche pas `unlocks.buildings`, `unlockedBuildingSet` ne fait
  qu'ajouter des chaînes, et les 2 sites d'affichage utilisent `BUILDINGS[b] && BUILDINGS[b].name || b`
  (repli null-safe → l'id brut s'affiche). (3) **Constantes nommées** `COLLIDER_REPAIR_NODES [38,40,42]`
  / `COLLIDER_PUZZLE_NODES [39,41,43]` + helpers `isNodeConfirmed`/`colliderRepaired` : les 3 sites qui
  codaient `nodes[34]` en dur (et `colliderPalier`) passent par ces constantes — ces ids avaient DÉJÀ
  dérivé une fois. Palier = 1 + nb de PUZZLES résolus. (4) **Migration < 29** (remappage par SENS, pas
  par numéro) : 39→33 (Batterie V2), 35→34 (Forage, MÊME récompense = foreuse), 36→35, 37→36, 38→37,
  **ancien 33 « Circuit Logique » → 38 Réparation I** (le joueur NE PERD PAS ses 3 portes de base),
  40→39, 41→41, 42→43. LOCALES `tech` 33-43 réécrites ×4 langues. Validé : `node --check` (7 blocs) +
  Chromium E2E : arbre **43 nœuds contigus**, paliers 1→2→3 pilotés par les puzzles, Réparation I
  confirmée → le Collisionneur démarre ; **4 boutons N/E/S/O en 4 colonnes**, clic RÉEL sur chacun →
  `gateDir` 3/1/2/0 exacts + surlignage ; **save v28 forgée (ancien arbre) → migration** : foreuse,
  Batterie V2 et portes de base CONSERVÉES, palier 2, Réparation II en `condition_ok` ; 0 erreur JS.
  Build 282→283.
  Changement 14.00 : **PUZZLE COLLISIONNEUR — §8 tutos + panneau d'état (le brief est COMPLET).**
  `SAVE_VERSION` INCHANGÉ (tips = `tipsSeen` existant ; panneau = lecture seule). (1) **§8 — 4 tutos**
  ajoutés à `GAME_TIPS` : **`collider_cmp1`** « Comparer deux bits » (déblocage des 3 portes de base :
  explique le tirage aléatoire des saveurs, la vanne, et que le comparateur 1 bit coûte **5 portes**
  = 2 NOT + 2 AND + 1 OR) ; **`collider_cmp2`** « NAND & NOR : le comparateur en 3 portes » (déblocage
  NAND/NOR au nœud 40 : **`NOR(a,b) OU AND(a,b)`** = XNOR, 2 portes de moins) ; **`collider_cmp3`**
  « XNOR natif — et les leptons » (déblocage XOR/XNOR au 41 : 1 porte par bit, MAIS le détecteur
  000/111 reste nécessaire — NOR des 3 bits / AND des 3 bits + OU final) ; **`collider_penalite`**
  « Le Collisionneur s'est éteint » (`when: () => false`, ouvert par le TICK). (2) **Notification de
  pénalité** : le flag `colliderPenaltyNotify` posé par `colliderPenalty` est consommé dans la boucle
  `frame` (à côté de `nucNotify`/`heatTrip`) → **toast rouge** systématique + **popup la PREMIÈRE fois**
  (garde `tipsSeen` + `!activeTipRef.current` → ne vole jamais le canal à une astuce déjà ouverte).
  C'est le tuto le plus important du brief : sans lui le joueur croit à un bug. (3) **Panneau d'état du
  Collisionneur** (`InfoPanel`, `info.mode === 'collider'`, ouvert en touchant le landmark — `handleTap`
  accepte désormais `t.terrain === 'collider'`) : **État** (en ruine / démarrage / en service / arrêt),
  **Palier** (P1-P3 · nb de saveurs · nb de bits), **Démarrage restant** (mm:ss), **Puissance**
  (courante / plafond du palier), **Électricité insuffisante** si `powered === false`, **Confirmations**
  (courant / objectif du palier), **Pénalités**, et les **codes émis** (Collisionneur / Data Center) en
  service. i18n en/es/de des 20 nouveaux libellés. Validé : `node --check` (7 blocs) + Chromium E2E :
  **tap réel sur le landmark → panneau** (« P3 · 6 flavors · 3 bits », « Startup remaining 09:59 »,
  « 21,3 MW / 8,39 GW », « insufficient — startup is falling back », « 0 / 10 000 ») ; les **3 tutos de
  portes s'affichent** au déblocage ; **pénalité réelle → popup « Le Collisionneur s'est éteint »**
  marquée vue (une seule fois) ; 0 erreur JS. Build 281→282.
  Changement 13.99 : **PUZZLE COLLISIONNEUR — runtime complet (§2/§3.2/§3.3/§3.4/§4/§5/§7 du brief).**
  `SAVE_VERSION` INCHANGÉ (champs additifs : `collider`, `techTree.colliderConfirms`, éléments enfants
  dans `t.logic` déjà couvert par `logicPlacements`). (1) **§2 Encodage PRÉFIXE des saveurs**
  (`COLLIDER_FLAVORS`) : 2 → 4 → 6 saveurs (P1 `up/down` 1 bit, P2 +`top/bottom` 2 bits, P3
  +`charm/strange` 3 bits) — un code appris ne change JAMAIS (invariant testé). **Leptons**
  `COLLIDER_EXCEPTIONS` 000/111 émis par le **Collisionneur SEUL** (jamais le Data Center → aucun cas
  ambigu) → le joueur doit envoyer 1 quelle que soit la comparaison, ce qui impose un détecteur de
  motif EN PLUS du comparateur. (2) **§3.2 Bloc ÉMETTEUR** `logic_emetteur` (`logicMultiSource`,
  `childOnly`) : **enfant** créé/détruit avec son porteur par `syncColliderChildren` (Collisionneur :
  bas-gauche du landmark 3×2 ; Data Center : sa tuile), **jamais posable** ; réseaux logiques
  reconstruits à chaque apparition/disparition. Publie les bits de SON support (`co.code` / `co.dcCode`)
  via `emitBits` (mapping fixe dir 0→α, 1→β, 2→γ, 3→INERTE). (3) **§3.3 Bloc VANNE** `logic_vanne`
  (`logicValve`, childOnly, haut-gauche) : lit le **OU de ses faces** ; 1 + codes égaux **ou lepton** →
  `techTree.colliderConfirms += max(1, dcReward)` ; 1 + codes différents → **`colliderPenalty`**
  (extinction, `state='off'`, timer remis à 0 → **les 10 min sont à refaire**, `colliderPenaltyNotify`) ;
  0 → rien. (4) **§3.4 JONCTION logique** `logic_jonction` : `rebuildLogicNetworks` réécrit en parcours
  de **NŒUDS « r,c,axe »** — une jonction porte **2 réseaux indépendants** (`netNS`/`netEO`), traverser
  ne change jamais d'axe → deux fils se croisent sans se connecter ; `dirNet` entre par l'axe du
  déplacement. Le **SPLIT en T marche déjà** (le flood-fill est un broadcast). (5) **§4 Collisionneur** :
  `processCollider` — machine `off→starting→running`, **démarrage 600 s** (`COLLIDER_START`), **sigmoïde
  300 s** (`COLLIDER_RAMP`) **montante ET descendante** (≠ nucléaire), puissance `COLLIDER_POWER`
  P1 32 MW / P2 512 MW / P3 8192 MW (plancher 1 MW), palier = 1 + nb de nœuds 40/41 confirmés
  (`colliderPalier`), **consommateur géant** ajouté à la demande de l'île 6 (pas assez d'élec. →
  `co.powered=false` → le démarrage RECULE et la puissance redescend). (6) **§5 Data Center** :
  `DC_BASE_RATE` 0,0625/tick ×2/niveau, **borné à 4/s** (`DC_RATE_CAP`) — au-delà le surplus devient un
  **multiplicateur de RÉCOMPENSE** (`dcEffective` : u7 → 4/s ×2, u8 → 4/s ×4). (7) **§7 nœuds tech
  40/41/42** « Collisionneur P1/P2/P3 » (⚠ le brief disait 39/40/41 mais **39 = Batterie V2** → décalés)
  + **nouveau type de `reqs` `colliderConfirm`** (100 / 1 000 / 10 000). Le nœud **33 rend les 3 portes
  de base** ; **NAND/NOR au nœud 40**, **XOR/XNOR au 41** (progression du brief) ; LOCALES ×4 pour 39-42.
  Validé : `node --check` (7 blocs) + **tests unitaires** (comparateur 3 bits sur les **64 combinaisons**
  `sortie === (a===b)` ; XNOR en 3 portes `NOR ou AND` == XNOR natif ; détecteurs 000/111 ; XOR/XNOR à
  2 ET 3 entrées = parité ; invariant PRÉFIXE des saveurs) + Chromium E2E : enfants auto-créés
  (1 émetteur + 1 vanne), P3 = **8 388 608 kW exact**, **codes égaux → confirmations**, **codes
  différents → pénalité (state running→starting, timer 500→1, compteur, notify)**, 300 tirages P3
  (leptons présents côté Collisionneur, **0 côté Data Center**, 0 code invalide), débits DC exacts,
  paliers 1/2/3 selon les nœuds ; 0 erreur JS. **⚠ REPORTÉ : §8 tutos** (4 popups de déblocage) et
  l'affichage UI dédié du Collisionneur (état/compteur dans un panneau). Build 280→281.
  Changement 13.98 : **4 retours — gating du bouton couche logique, boutons flottants masqués par
  l'inventaire, FIX démolition qui rasait la couche physique, FIX nœud 34 insatisfiable.**
  `SAVE_VERSION` INCHANGÉ. (1) **Bouton couche logique gaté sur la RECHERCHE** : rendu seulement si
  `isBuildingUnlocked(game,'logic_wire')` (le nœud #32 qui donne câble/capteur/actionneur) — avant il
  s'affichait dès le début sur une couche vide. **Filet de save** : une save des builds 273-279 peut
  avoir `uiPrefs.logicLayer = true` SANS le déblocage → le boot force la couche OFF (sinon joueur
  coincé en couche logique, bouton masqué, aucune sortie). (2) **Boutons flottants masqués quand
  l'inventaire est ouvert** (`!invOpen &&` sur `.logiclayer-btn` ET `.underground-btn`) — ils
  chevauchaient le panneau d'inventaire ouvert. (3) **FIX démolition en couche logique** : le garde
  ne traitait la surcouche QUE si `t.logic` existait, sinon il tombait dans le chemin normal et
  **détruisait le bâtiment/réseau du dessous**. Désormais, couche logique active ⇒ la démolition est
  **exclusivement** cantonnée à `t.logic` (tuile sans élément logique → toast « Rien à démolir dans la
  couche logique », le bâtiment est INTACT). (4) **FIX nœud 34 « ne fonctionne pas »** : `countBuildings`/
  `countBuildingsOnIsland` ne scrutaient que `t.building` — or depuis 13.96 les portes vivent dans
  `t.logic` → le `reqs` `buildAny` du nœud 34 était **INSATISFIABLE**. Les deux compteurs scrutent
  désormais **LES DEUX slots**. Le nœud est aussi **clarifié** : renommé « Poser une porte logique »
  (l'ancien « Circuit Logique 2 » ne disait pas quoi faire — ⚠ renommé AUSSI dans les 4 entrées
  LOCALES `tech`, sinon `applyToData` réécrit l'ancien nom) et ses `ids` couvrent **les 7 portes**
  (avant : seules les 3 de base → poser un XOR ne validait rien). i18n en/es/de du nouveau toast.
  Validé : `node --check` (7 blocs) + Chromium E2E par les VRAIS chemins de code : bouton absent en
  partie neuve / présent après déblocage ; les 2 boutons disparaissent inventaire ouvert ; **tap de
  démolition réel en couche logique → le bâtiment SURVIT** ; **nœud 34 `available` → `condition_ok`
  dès qu'une porte est posée dans la couche** ; 0 erreur JS. Build 279→280.
  Changement 13.97 : **Sprite Batterie V2 (pack v2.7) + panneau de config de la couche logique (§6 modes
  par support) + copier en couche logique + booster retiré COMPLÈTEMENT en dev.** `SAVE_VERSION` INCHANGÉ.
  (1) **Sprite `bat_accumulateur_v2`** (pack `ile6 v2.7` — seule ADDITION du pack, les 307 autres sprites
  byte-identiques à v2.6) inliné ; l'override `accumulateur_v2 → bat_accumulateur` est **RETIRÉ** → la
  résolution naturelle `bat_<id>` prend le nouvel art (vérifié distinct du V1). ⚠ Aucun autre pack du repo
  (COMPLET/OFFICIEL, y compris `_nouveau_v2/`) ne contenait d'art de batterie V2 — d'où l'attente de v2.7.
  (2) **`sensorModesFor(support)`** (module, source de vérité UNIQUE panneau + `processLogic`) : modes
  DISPONIBLES selon le support, le 1er étant le **défaut sensé** → bâtiment `[elec, intrant]`, batterie
  `[batt_empty, batt_full]`, câble `[elec]`, route/tuyau `[sature]`. `processLogic` utilise
  `sensorDefaultMode(sup)` quand `sensorMode` est absent (fini le défaut `elec` inadapté à une batterie).
  (3) **Panneau de config de la surcouche** (`InfoPanel`, `info.mode === 'logic'`) : ligne **Support**,
  bouton de **ROTATION LIBRE** (4 dir — « Face de sortie / Signal sortant / Face lue »), **sélecteur de
  condition** (modes du support), **polarité** de l'actionneur, état du signal. Le tap sur un dispositif
  en couche logique OUVRE ce panneau (au lieu de tourner à l'aveugle) — la rotation reste à 1 clic dedans.
  L'ancien bloc logique de l'InfoPanel (sur `t.building`) devient inerte (la logique vit dans `t.logic`).
  (4) **FIX réel** : `evalSensor` lisait `networkId`/`netIds` sur `t.building` alors qu'ils sont portés par
  la **TUILE** → nouveau `supportTileAt` (ancre résolue) ; les modes câble/route/tuyau fonctionnent
  vraiment. (5) **Copier en couche logique** : en mode Copier, toucher un élément de `t.logic` le capture
  comme outil actif. (6) **Booster retiré COMPLÈTEMENT en dev** : l'onglet était déjà gaté `!dev` ; son
  **astuce** l'est désormais aussi (`when` += `!(g.ui && g.ui.dev)`) → ni bouton ni tuto en dev.
  i18n en/es/de des 15 nouveaux libellés. `__heat` étendu (sensorModesFor/sensorDefaultMode/isLogicId/
  rebuildLogicNetworks). Validé : `node --check` (7 blocs) + Chromium E2E (sprite V2 présent ET distinct du
  V1 ; chaîne capteur(batterie, SANS mode explicite)→câble→actionneur met la mine en pause via le DÉFAUT
  `batt_empty` ; modes par support exacts `[elec,intrant]` / `[batt_empty,batt_full]` ; onglet booster
  absent en dev ; 0 erreur JS). Build 278→279.
  Changement 13.96 : **COUCHE LOGIQUE EN SURCOUCHE ABSTRAITE (`t.logic`) — pose n'importe où + lien
  monde réel (§2 + §6).** `SAVE_VERSION` **27→28**. (1) **Slot `t.logic` PARALLÈLE à `t.building`** :
  un élément logique (capteur/actionneur/porte/câble logique/émetteur — `isLogicId`) vit dans `t.logic`,
  posable **N'IMPORTE OÙ** (mer/sol, **par-dessus** bâtiments et réseaux) — vraie couche abstraite.
  `tryPlaceLogic` (intercepté en tête de `tryPlace`) + `canPlace` (logique → `!t.logic`). Chaque câble
  logique coûte **1 câble supraconducteur**. Démolition/rotation gérées dans la couche. (2) **Réseau
  logique SÉPARÉ** : `rebuildLogicNetworks` (flood-fill 4-dir des `logic_wire` de `t.logic` → `t.logic.netId`,
  `game.logicNets[isl]`), appelé en tête de `processLogic`. Aucun niveau/jonction/port (broadcast). (3)
  **`processLogic` réécrit sur `t.logic`** : capteurs/portes/actionneurs/émetteurs lus depuis `t.logic` ;
  **le SUPPORT = le bâtiment SOUS l'élément (même tuile, emprise résolue)** → un **capteur sur un bâtiment
  le lit**, un **actionneur sur un bâtiment le met en PAUSE** (`logicOff`). Capteur = sortie DIRIGÉE
  (`sensorDir`, la flèche = direction du signal) ; porte inchangée ; actionneur lit l'OU des faces.
  (4) **§6 modes de capteur PAR SUPPORT** (`sensorMode`) : bâtiment → **déficit élec.** (`pwrAvg<1`) ou
  **déficit d'intrant** (`inFac<1`) ; batterie → **0 %** / **100 %** ; câble → déficit élec. du réseau ;
  route/tuyau → réseau saturé. (5) **Rotation LIBRE** (`setLogicConfig` sur `t.logic`, cycle 4 dir) ;
  **tap en couche logique** (mode Sélection) fait TOURNER le dispositif. (6) **Rendu** : passe overlay
  DÉDIÉE sur `t.logic` (par-dessus l'île estompée, couche ON) — câbles (`fil_logique` connection-aware) +
  dispositifs (sprites v2.6 `_<dir>_<0|1>`) ; le rendu logique est retiré de la boucle `t.building`
  (désormais toujours estompée en couche ON). (7) **Persistance** : `logicPlacements` par île
  (serialize/loadSave) ; **migration <28** : les éléments logiques d'un ancien `t.building` (placements)
  sont routés vers `t.logic`. Validé : `node --check` (7 blocs) + Chromium E2E : **capteur(batterie 0%)
  →câble→actionneur(mine)** met la mine en pause ; batterie 100 % → relâche ; round-trip save v28
  (porte+câble restaurés) ; boot 0 erreur JS. **⚠ REPORTÉ :** panneau de choix du MODE de capteur
  (défaut sensé par support) ; copier en couche logique ; jonction/split de câble logique ; saveurs.
  Changement 13.95 : **SPRITES LOGIQUE v2.6 (état 0/1 + flèches DANS le sprite) + animations île 6 +
  badge pause logique.** `SAVE_VERSION` INCHANGÉ (assets + dessin). (1) **Intégration pack `ile6 v2.6`** :
  **160 sprites** inlinés/rafraîchis (`window.__SPRITE_DATA__[…]`, override d'assignation, dernière gagne)
  — **106 nouveaux** (senseur unifié `logic_senseur_<dir>_<0|1>`, actionneur `logic_actionneur_<dir>_<0|1>`,
  7 portes `logic_porte_<op>_<dir>_<0|1>`, blocs α/αβ/αβγ, `ui_pause_logique` 16×16…) + **54 rafraîchis**
  (bordures i7, collisionneur, bases logiques). **9 sheets d'anim île 6** (`centrale_gaz`, `extracteur`,
  `fab_ordi_quantique`, `foreuse`, `four_arc_tungstene`, `geothermie`, `machine_outil`, `presse_uhp`,
  `separateur_cryogenique`) rafraîchies (art dédié v2.6) sous leurs clés existantes (`bat_<id>` /
  `four_arc_tungstene`) → ANIM_META déjà présent, `ANIM_BY_SK` résout. (2) **Rendu des dispositifs
  logiques réécrit** (`draw`) : sprite UNIFIÉ v2.6 avec l'ÉTAT 0/1 (bandeau) ET la flèche de direction
  DESSINÉS dans le sprite (`_<dir>_<0|1>`, chaîne de repli vers `_<dir>` puis base, puis packs
  antérieurs). Le **senseur** a UN seul visuel (plus de sprite par mode ; la condition est un réglage du
  panneau). Quand le sprite v2.6 est utilisé (`logicStateSprite`), la **pastille verte 0/1 ET la flèche
  vectorielle sont RETIRÉES** (déjà encodées) ; repli conservé pour les anciens sprites. (3) **Badge
  pause logique** : tout bâtiment mis en pause PAR un actionneur (`bld.logicOff`) porte l'icône
  `ui_pause_logique` (coin haut-gauche), distincte de la pause manuelle. (4) **Overrides menu** : `capteur`
  →`logic_senseur_n_1`, `actionneur`→`logic_actionneur_n_1` (fini le sprite « sortie collisionneur »).
  Validé : `node --check` (7 blocs) + Chromium (boot 0 erreur JS ; 5 sprites v2.6 présents ; anim
  `bat_centrale_gaz` == pack v2.6 ; badge/pastille non-régressifs). **⚠ REPORTÉ (refonte behavior,
  prochain passage) :** §2 pose de logique EN SURCOUCHE (slot `t.logic` parallèle, n'importe où y compris
  par-dessus bâtiments/réseaux) + §6 modes de senseur PAR PORTEUR (bâtiment : déficit élec./intrant ;
  route/tuyau : état/seuil/déficit de stock ; câble : déficit élec. ; batterie : 0%/100%) + actionneur =
  mise en pause du bâtiment support. Build 276→277.
  Changement 13.94 : **PATCH (5 demandes) — Batterie V2 + recherche « 33 bis », sprites logique dans le
  menu, split Bloc/Porte logique, portes non améliorables, booster masqué en dev.** `SAVE_VERSION`
  INCHANGÉ (accumulateur_v2 = id additif ; nœud 39 additif, `techTree.nodes` reconstruit depuis
  `TECH_NODES` par id au chargement → save ancienne : nœud 39 `locked`, aucun crash ; le cap `TIER_NEXT`
  ne s'applique qu'à `fromV<16` → batteries actuelles NON rétro-cappées). (1) **Batterie V2** :
  nouveau bâtiment `accumulateur_v2` (`chargeLossless:true` → **rendement de charge 1** au lieu de 0,8
  dans la boucle énergie) ; densification `accumulateur`→`accumulateur_v2` (`TIER_NEXT` cap 9 ;
  `TIER_STEP` forfait **100 alliage_tungstene + 50 cable_supraconducteur**) ; nouveau nœud tech **id 39
  « Batterie V2 »** (prereq 32, produire 1000 câble supraconducteur → débloque `accumulateur_v2`).
  ⚠ Les futurs nœuds Collisionneur prendront 40/41/42 (le brief disait 39/40/41). (2) **Sprites logique
  au menu** (`BLD_SPRITE_OVERRIDE`) : `capteur`→`logic_senseur_plein`, `actionneur`→
  `logic_sortie_collisionneur`, 7 portes→`logic_porte_<op>`, `logic_wire`→`fil_logique_v1_15_NESO`,
  `accumulateur_v2`→`bat_accumulateur` (fini les carrés de couleur). (3) **Split menu couche logique** :
  le groupe `logic` devient DEUX groupes — **`logicblock`** (capteur/actionneur) et **`logicgate`**
  (7 portes) ; `LOGIC_BLOCK_GROUPS`/`BUILD_GROUPS_NORMAL` mis à jour ; i18n « Porte logique » en/es/de.
  (4) **Portes non améliorables** : `isUpgradable` exclut `logic/logicSource/logicSink/logicGate/
  logicMultiSource` (conso élec. déjà 0). (5) **Booster masqué en dev** : le 6e onglet est gaté sur
  `!dev` (prop `dev` de la Toolbar). Validé : `node --check` (7 blocs) + Chromium E2E (boot 0 erreur JS ;
  onglet booster absent en dev ; couche logique → 2 groupes « Logic block »/« Logic gate », 9 boutons
  logiques rendus en SPRITE (0 carré de couleur)). **⚠ REPORTÉ (refonte interlockée, prochain passage) :**
  §2 pose de logique EN SURCOUCHE (n'importe où, y compris par-dessus bâtiments/réseaux — couche
  abstraite, slot `t.logic` parallèle) ; §6 modes de senseur par porteur (bâtiment : déficit élec./
  intrant ; route/tuyau : état/seuil/déficit de stock ; câble : déficit élec. ; batterie : 0%/100%) +
  actionneur = pause ; §4/§8 rotation de tous les outputs + flèche de direction PAR-DESSUS le sprite ;
  §10 indication 0/1 sur le sprite + badge pause logique + animations bâtiments île 6. Build 275→276.
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
