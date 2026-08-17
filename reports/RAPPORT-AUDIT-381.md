# RAPPORT D'AUDIT — base 381 / Alpha 14.98

## 0. Métadonnées

| | |
|---|---|
| Source auditée | `https://raw.githubusercontent.com/freredoc/Archipel-Industry-/main/Archipel_industry_alpha-7.html` (re-téléchargée le jour de l'audit ; **identique octet à octet** au fichier du dépôt local) |
| SHA-256 | `7fda80ee2455658a29f086bdb3616a8a4fd4c00ff51d1bf5c1bc3d031dfda4ec` |
| Date | 2026-08-08 |
| Modèle | Claude Fable 5 (le brief recommandait Opus 5 ; la session tournait sur Fable 5 — signalé, sans incidence sur la méthode) |
| Durée | ~1 h 30 de session effective |
| Nature | **Lecture seule.** Aucun octet du fichier source modifié, aucun bump, aucune PR. |
| Méthode principale | Extraction des registres **AU RUNTIME** (boot Chromium headless + `page.evaluate` sur les bindings lexicaux globaux) plutôt que parsing textuel — immunise contre les pièges §2 du brief (apostrophes de commentaires, deux formes de `SPRITE_DATA`, `ISLAND_TERRAINS` vide, sentinelles). Le parsing textuel n'a servi qu'aux greps de sites de code (chantiers E/G/H), sur le fichier NON filtré. `awk length<300` utilisé UNIQUEMENT en exploration, jamais en extraction. |

## 1. Vérifications de base

| Fait | Attendu (brief) | Mesuré | Verdict |
|---|---|---|---|
| Taille | 3 349 788 o | 3 349 788 o | ✓ |
| Lignes | 30 698 | 30 698 (`wc -l`) | ✓ |
| Lignes ≥ 300 car. | 1 552 | 1 552 | ✓ |
| `GAME_VERSION` / `GAME_BUILD` / `SAVE_VERSION` | Alpha 14.98 / 381 / 31 | idem (l.8515/8516/8211) | ✓ |
| Blocs `<script>` | 7/7 (ancrés `^<script` / `^</script>`) | 7/7 | ✓ |
| `node --check` | 7/7 | **7/7 OK** (blocs de 412 / 4 340 / 10 750 / 131 834 / 1 111 781 / 232 099 / 1 578 960 caractères) | ✓ |
| **Boot réel** (Chromium headless 1194, serveur lancé depuis le dépôt, viewport 420×900 DPR 3, langue fr) | démarre | **OK** : `#splash` retiré, canvas peint à **100 %** (échantillonnage alpha), horloge qui avance (`playTicks` 3 → 6 sur 2,5 s), `tickErrors` null, **0 `pageerror`** | ✓ |
| Console au boot | — | 1 **404 préexistant** du serveur de test (ressource absente, connu depuis 14.54) + 1 **warning** `Canvas2D … willReadFrequently` (voir S4-H2) | à noter |

## 2. Synthèse

**0 × S1 · 0 × S2 · 4 × S3 · 6 × S4.** Aucun bloquant, aucune mécanique fausse trouvée : la base 381 est saine sur les huit chantiers. Les intégrités référentielle (A), du graphe de production (B), de l'arbre techno (C), de la sauvegarde (F) et de la chaîne chaleur/énergie (G) sont **vérifiées conformes au runtime**. Les 5 trouvailles les plus notables :

1. **S3-G1** — la fiche détaillée (appui long) filtre sur `b.power > 0` : les **36 bâtiments** à consommation sigmoïde/aléatoire n'affichent **aucune ligne « Conso. élec. »** (le correctif 14.95-C1 n'a pas été porté sur ce panneau).
2. **S3-H1** — le tableau du panneau **Production** a une largeur minimale de ~357 px : la colonne « Net /s » sort du panneau à 360 px **et** à 390 px de viewport (mesuré au pixel).
3. **S3-D1** — **321 des 858** libellés `I18N.t(...)` du code n'ont d'entrée dans aucune table en/es/de → repli français silencieux hors-fr.
4. **S4-E1** — **12 champs d'état écrits à chaque tick et lus nulle part** (`bld.waterNeed/waterDrawn/waterFrom/waterAvail`, `bld.gateInhCur/gateLive`, `co.confirms/goal/from/dcState/dcRate/wireNid`), dont **2 accompagnés de commentaires FAUX** qui affirment que la fiche les lit.
5. **S4-B1** — `ciment_irradie` est une ressource **morte** (0 producteur, 0 consommateur, 0 coût depuis 12.0) toujours déclarée partout et **absente de `REMOVED_RESOURCES`**.

## 3. Trouvailles par chantier

### A — Intégrité référentielle des données

Registres extraits au runtime : `BUILDINGS` 115 · `TECH_NODES` 43 · `TIER_NEXT`/`TIER_STEP` 42/42 · `TOOLBAR_GROUPS` 14 groupes · `BLD_SPRITE_OVERRIDE` 28 · `UI_ICON_BY_EMOJI` 58 · `TILE_ANIM_BY_KEY` 21 · `SPRITE_DATA` 1 495 clés (littéral + affectations, les deux formes couvertes par construction puisque lues au runtime) · `ANIM_DATA` 241 · `ANIM_META` 221 · ressources 48.

**Résultat : 0 référence cassée.** En détail, tout vérifié conforme :
- Toutes les clés et cibles de `TIER_NEXT`/`TIER_STEP` existent dans `BUILDINGS` ; chaque cible de palier a son entrée `TIER_STEP` et réciproquement.
- 0 doublon et 0 id inconnu dans `TOOLBAR_GROUPS` ; `logic_wire`/`logic_jonction` sont servis par `LOGIC_WIRE_GROUPS` (l.7526), donc posables.
- Les 28 entrées de `BLD_SPRITE_OVERRIDE` ont ≥ 1 candidat présent ; les 115 bâtiments résolvent un sprite via `buildingSpriteKey`, **sauf `logic_emetteur`** (S4-A2, préexistant inerte).
- Les 58 valeurs de `UI_ICON_BY_EMOJI` pointent vers un sprite `ui_*` présent ; **0 clé porteuse de U+FE0F**.
- Les 21 sheets « breeze » sans `ANIM_META` sont **toutes** couvertes par `TILE_ANIM_BY_KEY` (qui porte sa propre méta) — pas une anomalie.
- Toutes les ressources citées (inputs/outputs/cost/forfaits/livraisons/reqs) existent dans `RES_SHORT` ; parité `RES_SHORT`/`CARRIER_BY_RES` 48/48 ; `RES_TIER` omet volontairement `energie_kw` et `gaz_echappement` (documenté 14.48).
- Les 3 ressources sans sprite `item_<res>` direct (`eau_froide`, `ciment_irradie`, `element_moteur_nuc`) sont couvertes par **`ITEM_SPRITE_OVERRIDE`** (l.4417-4421), cibles toutes présentes — pas de trouvaille.
- `exclusiveUntilNode` et `ISLAND_ACCESS_NODE` ({2:2, 3:8, 4:14, 5:21, 6:28}) ne citent que des nœuds existants.

**A-S3-A1 · S3 · chantier A · l.7461 (`TOOLBAR_GROUPS`)**
Extrait : `geothermie_v2` et `usine_moteur_nuc_v2` sont les **2 seuls** bâtiments de palier (sur 42) absents de toute barre d'outils (avec `port` et les 2 `childOnly`, légitimes).
Preuve : diff runtime `Object.keys(BUILDINGS)` − ids de `TOOLBAR_GROUPS` − `LOGIC_*_GROUPS` → `['geothermie_v2','logic_emetteur','logic_jonction','logic_vanne','port','usine_moteur_nuc_v2']` ; `logic_jonction` est dans `LOGIC_WIRE_GROUPS` (l.7526).
Impact joueur : la **fiche détaillée** (appui long au menu) de ces 2 paliers est inatteignable.
Piste : une ligne dans les groupes `energy`/`nuclear` (défaut déjà signalé aux mémos 14.46 et 14.95 — décision d'Ethan).

**A-S4-A2 · S4 · chantier A · résolution sprite**
`buildingSpriteKey('logic_emetteur')` → `null` (bâtiment `childOnly`, dessiné par sa propre chaîne `logic_emetteur_*`). Préexistant et inerte, documenté au mémo 14.33 ; une ligne `logic_emetteur: ['logic_emetteur_inactif']` dans l'override le fermerait.

**A-S4-A3 · S4 · chantier A · l.3655 (littéral `ANIM_META`)**
`ANIM_META["bat_foreuse"]` n'a plus de sheet dans `ANIM_DATA` (résiduel du remplacement 14.34, documenté « inerte » — `ANIM_BY_SK` n'indexe que ce qui existe dans `ANIM_DATA`). Preuve : diff runtime `ANIM_META` − `ANIM_KEYS` = `['bat_foreuse']`.

### B — Chaîne de production

Graphe construit depuis le dump runtime, **arcs dynamiques inclus** (centrale nucléaire : irradiés + plutonium ; fours à arc via `ARC_DEF` ; refroidisseur via `COOLER_DEF` ; tour → eau ; foreuse → pierre ; collisionneur → hélium 3 ; `centrale_gaz` île 7 → `gaz_echappement`), puits de coût inclus (coûts de pose, forfaits `TIER_STEP`, livraisons de recherche).

**Résultat : aucune ressource consommée sans producteur atteignable.** Contrôle d'atteignabilité PAR ÎLE (exclusivités/interdictions/déblocages) : les seuls « hits » sont les consommateurs d'**oxygène sur l'île 7** — faux positif explicitement anticipé par le brief (l'oxygène est un liquide tuyau stockable → flushé au port quand le réseau touche le port (règle 10.82) → descend par l'élévateur, port 6/7 partagé). `beton_arme_irradie` et `moteur_quantique` n'ont pas de consommateur de recette mais des puits de coût nombreux — normal (matériaux d'endgame).

**B-S4-B1 · S4 · chantier B · l.3273 (`RES_SHORT`), l.3364 (`REMOVED_RESOURCES`), l.4419 (`ITEM_SPRITE_OVERRIDE`)**
Extrait : `const REMOVED_RESOURCES = ['information_quantique'];`
`ciment_irradie` : **0 producteur** (retiré de `NUC_MATS` au 12.0), **0 consommateur, 0 puits de coût** — ressource morte, pourtant toujours déclarée dans `RES_SHORT`/`RES_TIER`(t4)/`CARRIER_BY_RES`/`ITEM_SPRITE_OVERRIDE`, et **absente de la purge `REMOVED_RESOURCES`**.
Preuve : graphe runtime (producteurs/consommateurs/puits vides tous les trois) ; `grep -n "ciment_irradie"` ne rend que des déclarations de registres.
Impact joueur : un stock hérité d'une save d'avant 12.0 reste affiché à l'inventaire à vie (avec l'icône du béton irradié), sans aucun usage possible.
Piste : ajouter `ciment_irradie` à `REMOVED_RESOURCES` (purge idempotente déjà en place, l.~24310) — ou l'assumer comme relique.

**B-S4-B2 · S4 · chantier B · l.4767 (`NON_STORABLE`)**
`eau_froide` : 0 producteur / 0 consommateur depuis 12.0 (« defs laissées, inertes » au mémo), mais reste dans `NON_STORABLE` (purge par tick), les registres et `ITEM_SPRITE_OVERRIDE`. Dette connue, sans effet joueur.

**B-S4-B3 · S4 · chantier B · l.3283 (`RES_SHORT`)**
`matiere_exotique` : déclarée en 13.79 pour les « phases futures », jamais produite ni consommée ni coûtée. Dette volontaire, à garder en tête.

### C — Arbre technologique

- **Structure** : 43 nœuds d'ids contigus 1..43, tous les `prereq` strictement inférieurs à l'id → **0 cycle, 0 nœud inatteignable**. Modes : 39 `delivery`, 3 `auto` (39/41/43, confirmations gratuites post-réparation — attendu), 1 `start`.
- **Producibilité** : pour chacune des 39 livraisons, chaque ressource exigée a ≥ 1 bâtiment producteur débloqué par un **ancêtre strict** (ou disponible d'origine) — **39/39, 0 KO** (contrôle statique sur le dump ; recoupe le test dynamique 8.16 du lot 14.91).
- **Gardes** : `techDeliver` (l.16587) et `deliveryReady` (l.16615) portent la **même** condition d'île et passent tous deux par `portPool` (le nœud 35, île 7, débite bien le port 6) — vérifié en ouvrant les deux sites.
- **`ISLAND_ACCESS_NODE`** cohérent avec les 7 îles de `ISLAND_TERRAINS_BASE` ; `[7]` absent comme attendu (souterrain via nœud 31).

**Point chaud « produce cumulé vs stock instantané » — liste complète demandée.** `produce` (compteur global cumulé, sans dimension d'île) rend un nœud `condition_ok` ; la livraison exige le stock **au même instant** sur l'île du nœud. La pastille de notification est déjà gatée par `deliveryReady` (donc payable), le risque est le **coût réel en temps de stockage**, borne inférieure à 1 bâtiment producteur V1 non amélioré :

| Nœud | Livraison dominante | Débit V1 | Borne inf. |
|---|---|---|---|
| 25 | 3 × 1 000 irradiés **simultanés** (île 5) | 1/s (un matériau à la fois) | ~50 min cumulées |
| 27 | 1 000 `element_moteur_nuc` | 0,1/s | 2,8 h |
| 31 | 20 000 `beton_arme_irradie` | 1/s | 5,6 h |
| 33 | 1 000 `cable_supraconducteur` (île 6, presse île 7 **bridée par l'élévateur**) | 1/s | ≥ 17 min |
| 34 | 2 000 béton irr. + 2 000 alliage + 1 000 supra | 1/s | ~1 h |
| 38 | 20 000 supra + 20 000 alliage + **10 000 `element_moteur_nuc`** | 0,1/s | **27,8 h** |
| 40 | 1 000 `ordinateur_quantique` | 0,0625/s | 4,4 h |
| 42 | 1 000 `moteur_quantique` | 0,1/s | 2,8 h |

`RESEARCH_DELIVERY_FACTOR = 1` (l.14844) agit uniformément et ne corrige pas ce cas — c'est le levier prévu. **Aucun nœud invalidable** ; c'est un dossier d'ÉQUILIBRAGE (le nœud 38 est le plus exposé), pas un bug. Décision d'Ethan.

### D — Internationalisation

Tables dumpées au runtime via `I18N.locales` (4 langues) :
- **Parité de clés parfaite** sur `res` (32×4), `bld` (56×4), `tech` (43×4), `tutorial` (14×4, **0 goal null**), `tips` (26×4).
- **Paragraphes `body` des tips : 0 déséquilibre** sur 26 entrées × 3 langues (l'audit institué au 14.82 tient).
- `ui` : en/es/de ont **exactement le même jeu de 696 clés** (∆ = 0) ; les 404 clés de `fr.ui` sont toutes ⊂ en (les 292 clés en-seulement sont l'effet normal du modèle gettext, clé = texte fr).
- **0 clé emoji avec U+FE0F** dans `UI_ICON_BY_EMOJI` (58 clés).

**D-S3-D1 · S3 · chantier D · tout le fichier**
Extrait : sur **858** littéraux `I18N.t('…'/"…")` distincts extraits du code (décodage exact des `\xNN` via Node), **321 n'ont d'entrée dans aucune table** → rendus en français dans les 4 langues. Exemples : « Améliorer le débit », « Percer le mur (5 min) », « Demander au port », « Réparer l'élévateur », toutes les fiches logique/foreuse/élévateur/chaleur récentes.
Preuve : script reproductible (regex `I18N\.t\(\s*(['"])…` + `eval` du littéral + membership dans `en.ui`) ; liste complète produite en annexe de session (`i18n_missing.txt`).
Impact joueur : l'UI hors-fr est ~ 40 % française sur les systèmes récents (reliquat documenté depuis 11.01, jamais chiffré).
Piste : un lot i18n dédié, mécanique et pré-compilable (les 321 clés sont listées).

### E — État mort et état jamais assigné

Méthode : relevé regex des assignations (`=` hors comparaison, `+=`, `++`…) vs mentions, sur les alias réels (`game.`/`g.`/`gameRef.current.` ; `bld.`/`bl.`/`t.building.` ; `co.` ; `drag.`), puis **vérification individuelle au grep de chaque survivant** (les faux positifs d'alias — `gg.lastActiveTs`, `lg.emitCode`, minifiés React — ont été éliminés un à un).

**E-S4-E1 · S4 · chantier E · 12 champs écrits-jamais-lus, tous confirmés à 0 lecture dans tout le fichier :**

| Champ | Sites d'écriture | Note |
|---|---|---|
| `bld.waterFrom` | l.10865 | posé par `processHeat` à chaque tick |
| `bld.waterAvail` | l.10868, 10877 | idem |
| `bld.waterNeed` | l.10909 | **commentaire FAUX** : « fiche : « Eau X% · N eau/s » » |
| `bld.waterDrawn` | l.10910 | **commentaire FAUX** : « fiche : ce qu'elle a RÉELLEMENT pris ce tick » |
| `bld.gateInhCur` | l.11485 | `processLogic`, chaque tick |
| `bld.gateLive` | l.11486 | la fiche recalcule via `gateLiveDirs()` (l.19027) |
| `co.confirms` | l.11582 | miroir — la fiche lit `techTree.colliderConfirms` (l.18830 env.) |
| `co.goal` | l.11866, 11957, 12014, 12040 | la fiche recalcule `COLLIDER_GOALS[pal]` (l.18832) |
| `co.from` | l.11926, 12006, 12375 | vestige de rampe — l'oscillation actuelle passe par `co.sigT` |
| `co.dcState` | l.12057 | la fiche appelle `dataCenterState(dcT)` en direct |
| `co.dcRate` | l.12062, 12088 | **commentaire FAUX** : « affichée dans la fiche » — elle recalcule via `dcEffective` |
| `co.wireNid` | l.13959 | le champ vivant est `co.wireOk` (lu l.18874) |

Preuve : pour chacun, `grep -n "\.<champ>\b"` sur le fichier entier ne rend que les sites d'écriture (+ commentaires).
Impact joueur : aucun (dette pure) — MAIS les **4 commentaires mensongers** (l.10909-10910, l.12062, et « elle lit `bld.waterNeed`/`waterDrawn` » à l.19999-20001 : la fiche tour lit en réalité `wf = bld.regime`) sont exactement le piège n°6 du brief : ils dispensent le lecteur de vérifier.
Piste : lot de nettoyage — supprimer les écritures ou rebrancher les lecteurs, et corriger les commentaires.

Cas volontaires, non comptés : `game._catchUpStats` (l.~25273, diagnostic transitoire documenté 14.22) ; `drag.pid` (plomberie pointer-events) ; `co.powered` purgé à `undefined` (faux positif listé par le brief, confirmé conforme).

**Chantier PARTIEL** : la direction inverse (« lu-jamais-écrit ») n'est **pas exploitable par regex** sur ce code (écritures via littéraux d'objet de `newGame`, désérialisation par clés courtes `pl.*`, alias multiples) — les listes brutes étaient à > 90 % des faux positifs. Non traitée plutôt que rendue plausible (cf. §4).

### F — Sauvegarde et migration

- **Round-trip réel exécuté** (Chromium) : partie neuve → save forcée (armement `saveTimer` + `visibilityState` hidden, le vrai chemin `flushSave`) → **`version: 31`** dans le slot → reload → chargement par le vrai `loadSave`.
- **Diff de schéma** partie neuve vs partie rechargée : **0 champ perdu** (top-level, `ui`, `tutorial`, `guide`, `techTree`). Les seuls écarts sont des champs AJOUTÉS côté chargé, tous légitimes et à défaut sûr (`archiVu6`, `ugSeq`, `gateInhibitWarn`, `catchingUp`, `_catchUpStats`/`_catchUpTs`, `ui.numFormat`/`numThreshold` — posés paresseusement). `tickErrors` null après reload.
- **44 champs sérialisés** relevés dans le slot ; les ~46 champs transitoires non sérialisés (réseaux, flux, caches `_elevTile`, audio-runtime, `wireInfo`…) sont tous **reconstruits par tick ou re-dérivés au chargement** (échantillon vérifié : `networks` via `rebuildNetworks`, `energy`/`netFlow` par le tick).
- **Liste blanche de versions** : `[3..31]` contiguë (l.24271) ; migration `invertActs` pour < 31, `padShift` pour < 11 — conformes aux mémos. Une save v1/v2 est rejetée (retour `false`, partie neuve) — assumé historique.

**Aucune trouvaille S1/S2.** Le save réel `ARCHv1:` d'Ethan n'a pas été demandé (l'analyse statique + le round-trip suffisaient, conformément au brief).

### G — Énergie et chaleur

- **Liste fermée de la chaleur proportionnelle** (l.12996-13006) : relue au site — exactement `machine_outil`, `presse_uhp`, `usine_moteur_quantique`, `centrale_enrichissement_v2`, `usine_moteur_nuc_v2`, indexée sur `power × regime` (consommation **électrique**), sinon plat (`usine_moteur_nuc`, `cryostat`, `data_center`), antenne (l.13089) et nucléaire (l.13578) à part. Les **11 bâtiments à `heatCap`** ont chacun une branche d'émission ; `centrale_nucleaire_v2` est `noHeat` sans `heatCap` ; **`centrale_gaz` n'a ni l'un ni l'autre** (le contre-exemple type du brief est conforme : chaleur nulle).
- **`heatEmitMaxOf`** (l.10654-10682) relu intégralement : cohérent branche à branche avec la table d'émission du tick (nucléaire/antenne/plats/proportionnel, référence `meanPower` × boost d'antenne). Aucun bâtiment ni UI ne suppose une chaleur hors liste/`heatCap`.
- **`pwrAvg`** : coefficient 0,12 confirmé (l.13941), un seul site de lissage — cité, aucun correctif proposé (interdit par le brief).
- **Antenne (dossier documenté, non corrigé)** : le gate est aujourd'hui `antPowered`/`antTicks`/`antNeed` avec `ANT_POWER_TICKS = 3`, `ANT_POWER_SLOW = 45`, `ANT_POWER_HOLD = 20` (l.11769-11771) — l'hystérésis d'origine « 55 ticks » décrite par le brief a été remplacée par le double barème du 14.92 ; le service est jugé sur le **motif électrique** (`!(active === false && discReason === 'power')`, l.12523 env.). État actuel sain à la lecture ; champs transitoires hors save (confirmé au round-trip F).
- `tickIsland` : un seul site d'appel, dans la boucle sur toutes les îles — reconfirmé.

**G-S3-G1 · S3 · chantier G/H · l.18245 (`BuildingDetailModal`)**
Extrait : `if (b.power > 0) addRow(I18N.t('Conso. élec.'), fmtPower(b.power));`
La fiche **détaillée** (appui long sur une vignette du menu Bâtiment) filtre sur le champ statique `b.power` au lieu du prédicat `isEnergyConsumer` (l.14552) : les **36 bâtiments** dont la consommation est portée par `sigmoid`/`randomP` avec `power: 0` (dump : `betonniere_v2`, `centrale_enrichissement(_v2)`, `circuit(_v2)`, `fab_processeur(_v2)`, `fab_ordi_quantique`, les mines V3/V4, `presse_uhp`, `usine_moteur_nuc(_v2)`, `usine_moteur_quantique`, `separateur_cryogenique`, les 2 fours à arc, etc.) n'affichent **aucune ligne de consommation électrique**. Les deux autres panneaux (InfoPanel l.20237, UpgradePanel l.20728) utilisent bien le prédicat depuis 14.95 — ce site a été oublié.
Preuve : grep des 4 occurrences d'`isEnergyConsumer` (aucune dans `BuildingDetailModal`, qui commence l.18174) + comptage runtime des 36 bâtiments `power==0 && (sigmoid||randomP)`. Scénario falsifiable : appui long sur « Presse UHP » au menu → la fiche liste Coût/Entrées/Sorties mais aucune ligne élec., alors que sa sigmoïde tire 128→1024 kW.
Impact joueur : impossible d'anticiper la facture électrique d'un bâtiment variable avant de le poser.
Piste : même correctif que 14.95-C1 — `isEnergyConsumer(id)` + plage `minPower→nominalPower`.

### H — Rendu React et boot réel

1-2. `node --check` 7/7 et boot réel OK (cf. §1). L'équilibre des parenthèses des sites React est couvert par la validation syntaxique des 7 blocs (une parenthèse manquante casserait `node --check`) ; aucune vérification sémantique supplémentaire n'a été tentée.
3. **Pastille vs sprite** : les 8 usages restants de `.ip-swatch` ont été ouverts un à un — **tous sont des branches de REPLI** de ternaires `sprite ? <img> : <swatch>` (InfoPanel ×2, en-têtes terrain l.18754/18920/19176, UpgradePanel l.20701, NetworkPanel l.21894). Aucun panneau oublié.

**H-S3-H1 · S3 · chantier H · l.204 (CSS `.prod-row`)**
Extrait : `.prod-row{display:grid;grid-template-columns:1fr 88px 88px 92px;gap:4px;…}`
Les colonnes fixes (88+88+92 + 3 gaps + libellés) imposent une largeur minimale ≈ **357 px** au tableau du panneau **Production** ; `1fr` vaut `minmax(auto,1fr)` — exactement la famille de défauts corrigée pour le Port en 14.89 §6, non appliquée ici.
Preuve **mesurée au runtime** (vrai panneau ouvert par le vrai bouton, 3 viewports) : à **360 px**, panneau large de 298 px, bord droit à x=349, la cellule « Net /s » finit à **x=387** (38 px hors panneau, 27 px hors écran) ; à **390 px** : 388 vs 378 (coupée) ; à 420 px : 389 vs 407 (OK). `scrollWidth` 357 > `clientWidth` 298 sans défilement horizontal.
Impact joueur : sur mobile 360-390 px, la colonne « Net /s » du panneau Production est tronquée ou invisible.
Piste : `minmax(0,1fr)` + colonnes fixes réduites (ou `overflow-x:auto` sur le conteneur), comme au 14.89.

**H-S4-H2 · S4 · chantier H · boot**
Warning console au démarrage : `Canvas2D: Multiple readback operations using getImageData are faster with the willReadFrequently attribute set to true` — déclenché par les relectures répétées du chien de garde `canvasLooksBlank` (10.64) (et par la sonde d'audit elle-même). Cosmétique/performance ; piste : `getContext('2d', { willReadFrequently: true })` — à peser, ce contexte change le chemin GPU du canvas principal.

## 4. Chantiers partiels ou non traités

- **E (direction « lu-jamais-écrit »)** : abandonnée — les écritures passent par des littéraux d'objet (`newGame`), la désérialisation à clés courtes (`pl.*`) et de multiples alias ; un relevé regex produit > 90 % de faux positifs. Ce qu'il reste à faire : une passe outillée (AST) ou un relevé par famille de champs, hors de portée d'un audit en session.
- **H point 3 (parenthèses React)** : couvert par `node --check` seulement (garantie syntaxique, pas sémantique).
- **G (antenne)** : documentée à la lecture des sites (constantes, barèmes, motif électrique) ; **aucune mesure dynamique de rallumage** n'a été rejouée dans cette session (elle l'a été au lot 14.92 sur cette même base).
- **F** : le save réel `ARCHv1:` d'Ethan n'a pas été rejoué (non nécessaire, cf. chantier F ; le brief l'autorisait explicitement).

## 5. Hypothèses [NON VÉRIFIÉ]

- **[NON VÉRIFIÉ]** G-S3-G1 : le rendu effectif de la fiche détaillée n'a pas été exercé par un appui long réel ; la preuve est le chemin de code (l.18245 relue, seule ligne élec. du composant, gate `b.power > 0`) + les données runtime des 36 bâtiments. Le scénario donné est falsifiable en jeu.
- **[NON VÉRIFIÉ]** H-S3-H1 : la mesure porte sur la ligne d'EN-TÊTE (seule ligne rendue sur une partie neuve sans flux) ; les lignes de données partagent la même règle `grid-template-columns`, donc le même minimum — mais aucune ligne de données réelle n'a été mesurée.
- **[NON VÉRIFIÉ]** B : les arcs « dynamiques » du graphe de production reproduisent les règles documentées aux mémos (nucléaire, arcs, refroidisseur, foreuse, collisionneur) ; chaque règle a été confirmée à l'existence de son site (grep) mais les débits n'ont pas été re-mesurés en moteur dans cette session.
- **[NON VÉRIFIÉ]** D : les 321 littéraux non traduits incluent des fragments composables (préfixes/suffixes concaténés) dont certains sont peut-être volontairement non traduits (unités « /s », « /tuile ») — le compte exact de phrases pleines est un sous-ensemble à trier au moment du lot i18n.

## 6. Recommandation de séquencement

1. **Lot « UI panneaux » (S3, petit, même famille que 14.89/14.95)** : G-S3-G1 (fiche détaillée → `isEnergyConsumer` + plage min→max) + H-S3-H1 (`minmax(0,1fr)` sur `.prod-row`). Deux sites, patrons de correction déjà éprouvés dans le fichier, testables par les harnais existants (UI réelle + mesures au pixel).
2. **Lot « nettoyage d'état » (S4, zéro risque joueur)** : les 12 champs morts de E-S4-E1 + les 4 commentaires mensongers + `ciment_irradie` → `REMOVED_RESOURCES` (B-S4-B1) + l'entrée `ANIM_META.bat_foreuse` (A-S4-A3). À grouper parce que chaque champ mort et chaque commentaire faux coûte du temps à chaque futur audit/lot (piège n°6 du brief) ; aucun de ces retraits ne touche une save.
3. **Lot « i18n reliquat » (S3, gros mais mécanique)** : les 321 clés de D-S3-D1 — pré-compilable, la liste est produite ; à trier d'abord (phrases pleines vs fragments d'unités).
4. **Décisions d'Ethan (pas des patchs)** : barre d'outils pour `geothermie_v2`/`usine_moteur_nuc_v2` (A-S3-A1, déjà signalé deux fois) ; équilibrage des livraisons lourdes du chantier C (le nœud 38 à ~28 h V1 est le point dur) — le levier `RESEARCH_DELIVERY_FACTOR` existe.
