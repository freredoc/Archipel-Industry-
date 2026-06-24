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
- **État au dernier passage : `GAME_BUILD = 106`, `GAME_VERSION = 'Alpha 10.81'`.** Changement
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
