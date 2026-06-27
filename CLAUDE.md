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
- **État au dernier passage : `GAME_BUILD = 178`, `GAME_VERSION = 'Alpha 12.7'`, `SAVE_VERSION = 14`.**
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
