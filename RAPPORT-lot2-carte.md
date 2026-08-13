# RAPPORT — LOT 2 : panneau Carte

**Livré : `GAME_BUILD = 396`, `GAME_VERSION = 'Alpha 16.3'`, `SAVE_VERSION = 31` (INCHANGÉ, vérifié).**

Base d'exécution : build **395 / Alpha 16.2**, SHA-256 `1d0d0cd864f800970547caec974e31a525170f5cba36872215c6f98773360177`,
3 436 454 o — **identique** à la base de pré-compilation annoncée par le brief. La branche a été
repartie de `main` après le merge du lot 1 (PR #378), donc aucune divergence.

---

## 1. Application du patch

`patch_lot2.py` haché avant usage : **`98f99a8b261a4598876a07d18be7477dc98ef276d25cbb3ff2b9f8c5d9702ade`**,
12 032 o — conforme au brief.

Sortie du patcher, **13 ancres sur 13** :

```
OK css       OK        OK pp_tabs   OK        OK hud_aide  OK
OK am_sig    OK        OK pp_else   OK        OK app_st    OK
OK am_link   OK        OK pp_state  OK        OK app_rnd   OK
OK am_click  OK        OK hud_prop  OK
OK am_ret    OK        OK hud_call  OK
delta octets : +1724
```

**Delta constaté : +1 724 o** — exactement la valeur annoncée.

**Le fichier patché avant bump porte `d22f546839b1e665b989c57dd8c34092e0e310614ced15af2497b7b3151261b8`
— identique au binaire près au repère du brief.** C'est la confirmation la plus forte possible que
le patch s'est appliqué exactement comme pré-compilé, sans dérive d'ancre.

**Idempotence vérifiée** : seconde passe → les 13 gardes se déclenchent, **+0 octet**.

Taille finale après i18n et bump : **3 441 933 o** (+3 755 o).

---

## 2. Contrôles statiques

- **`node --check` : 7/7**, avant ET après le bump. Piège du brief reproduit : le scanner naïf
  `<script\b[^>]*>` rend **11** correspondances contre **7** blocs réels.
- **`MapPanel` : 55 parenthèses ouvrantes / 55 fermantes** — la valeur du brief, retrouvée.
- **Résidus à zéro**, tous vérifiés :

  | motif | occurrences |
  |---|---|
  | `"pp-tabs"` | 0 |
  | `archSel` / `setArchSel` | 0 / 0 |
  | `setTab` | 0 |
  | `tab === 'island'` | 0 |
  | `archReveal` | 0 |

  ⚠ `onPick` rend **5** occurrences, mais **aucune dans `ArchipelMap`** : ce sont le pavé
  directionnel (`dirPad`, build 14.22) et le `selRow` des Options, sans rapport avec ce lot. La
  signature est bien `function ArchipelMap({ game, flows, reveal })` et le composant contient
  **0 `onPick` et 0 `sel ===`**.
- **`SAVE_VERSION` toujours à 31.**

### SHA-256 des 7 blocs, re-extraits APRÈS la toute dernière modification

Fichier entier : **`227ccc5652555d56e96592c8e93812024927075d679742ba12cd64e2102b9c2f`** — 3 441 933 o.

| bloc | SHA-256 (16 car.) | taille |
|---|---|---|
| blk1 | `a50c1c4e7f4a304c` | 418 |
| blk2 | `8fbb22187703339c` | 4 397 |
| blk3 | `d949f1c3687aedad` | 10 751 |
| blk4 | `35f4f974f4b2bcd4` | 131 835 |
| blk5 | `1be53ce44e7be14f` | 1 113 969 |
| blk6 | `766081c0b56bf01c` | 240 272 |
| blk7 | `f00e3fdd2ff43853` | 1 684 498 |

`GAME_NOTES` : **739 caractères extraits** par la regex de la CI (`[^"]*`), chaîne complète, accents
littéraux, **0 séquence `\u`**, aucun guillemet droit.

---

## 3. Suite de validation exécutée

Chromium headless, page servie depuis la racine du dépôt, locale forcée. **Astuces fermées par clic
sur `.tip-ok` uniquement — jamais `remove()`** (le brief prévient qu'une suppression hors React casse
la réconciliation et empêche ensuite tout panneau de s'ouvrir ; la consigne a été suivie et aucun
`NotFoundError` n'est apparu).

| test | verdict | montage et valeurs mesurées |
|---|---|---|
| **T1** pré-compilation | **PASS** | 7/7 `node --check` ; scanner naïf = 11 (piège reproduit) |
| **T2** le panneau s'ouvre | **PASS** | 390×780. Le **2ᵉ `.help-btn`** porte `title="Carte de l'archipel"` ; son clic fait apparaître `.research-panel.port-panel` contenant `.arch-map`. Titre du panneau « Carte de l'archipel », section « Flux entre îles » |
| **T3** liaisons non cliquables | **PASS** | Panneau ouvert, îles 2-6 déverrouillées. **5 liaisons**, toutes en **`DIV`**, `pointerEvents` **`none`**, **0 `onclick`**, et l'invite « Touchez une liaison… » a bien disparu |
| **T4** marqueur du souterrain | **PASS** | `elevatorRepaired=false` → marqueur **absent** ; repassé à `true` → **présent**, image **76 px**, libellé **« Île 6 S »** (issu d'`islandLabel`), `.arch-brume` **absent dans les deux cas**. **6 nœuds d'île** et le marqueur **n'en est pas un** (`classList.contains('arch-node')` faux) |
| **T5** Port sans onglets | **PASS** | Carte **fermée** d'abord (piège du brief). Sélecteur `[data-tut="port"]`. **1 seul panneau**, `.pp-tabs` = **0**, `.pp-tab` = **0**, `.arch-map` = **0**, titre **« Port — Île 1 »**, sections conservées : **« Amélioration du transit », « Liaisons », « Commerce (Île 1) »** |
| **T6** compactage d'Options | **PASS** | **479 px → 50 px** (libellé `display:none`), **480 px → 127 px** (`display:block`). Valeurs du brief retrouvées exactement |
| **T6 bis** pastille de MAJ | **PASS** | *voir §4 — le point que le brief n'avait pas pu provoquer* |
| **T7** son | **PASS** | Ouverture Carte → `["mapOpen","reveal6"]` ; ouverture Port → `["panelOpen"]`, **aucun `mapOpen`**. `SFX.play('mapOpen')` s'exécute sans exception. Vérifié aussi à la source : `mapOpen` est au catalogue (déclaration + liste `DRY_UI`) et **le seul appel `SFX.play('mapOpen')` du fichier est le nouveau bouton HUD** |
| **NR** révélation de l'île 6 | **PASS** | Ajouté hors brief. Le composant hôte change (`PortPanel` → `MapPanel`) : 1ʳᵉ ouverture → brume **présente** et `archiVu6` posé à **`true`** ; 2ᵉ ouverture → brume **absente**. La révélation joue toujours, et **toujours une seule fois** |

**0 `pageerror`** sur toute la suite. Seule erreur console : un **404 unique** du serveur de test,
bruit **préexistant** documenté depuis le build 14.47.

### Bande d'onglets, bouton Carte compris

| largeur | bande | onglets entiers | largeur d'Options |
|---|---|---|---|
| 320 | 98 | 3 | 50 |
| 390 | 166 | **6** | 50 |
| 479 | 166 | 6 | 50 |
| 480 | 166 | 6 | **127** |
| 560 | 166 | 6 | 127 |

**Table du brief reproduite à l'identique.** On ajoute un bouton et la bande respire davantage
qu'avant : **six îles entières dès 390 px**, contre cinq au build 395.

---

## 4. Le point que le brief n'avait pas pu provoquer : la pastille de mise à jour

Le brief demandait explicitement, à 479 px, de forcer l'état « mise à jour disponible » et de
vérifier que la pastille survit au compactage — en précisant : *« si tu ne parviens pas à le
provoquer, écris-le plutôt que de cocher le test »*. **Il a été provoqué et il passe.**

Montage : `window.fetch` intercepté dans un `addInitScript` pour que la requête `version.json`
renvoie `{build: 9999, version: 'Alpha 99.9'}`. Le **vrai chemin** du jeu est donc exercé
(`fetch` → `build > GAME_BUILD` → `setUpdateInfo`), pas un état forcé à la main.

Mesuré à **479 px** :

```
has-update=true · .notif-dot présent=true display=block largeur=9px
libellé display=none · bouton=50px
```

La pastille est **rendue et visible** alors que le libellé est masqué. La justification du brief est
donc confirmée par le DOM : le bouton Options a pour enfants `[span.gear, span(libellé),
span.notif-dot]` — `nth-child(2)` ne prend que le libellé, là où `last-child` aurait emporté la
pastille. **C'était le seul point où ce lot pouvait faire disparaître une information plutôt qu'un
mot ; il est fermé.**

---

## 5. Écarts par rapport au brief

1. **Branche `claude/carte-archipel-wmyxbs`** et non celle du brief : la consigne de session l'impose
   et interdit toute autre branche. Sa PR (lot 1) étant mergée, la branche a été **repartie de
   `main`** conformément à la règle de suivi, sans empiler sur de l'historique déjà mergé.
2. **Les deux libellés visibles sont traduits** (voir §6) — le brief laissait le choix, je l'ai
   tranché dans le sens de l'ajout.
3. **Trois montages de ma suite étaient fautifs à la première passe, aucun défaut produit** — je les
   consigne parce qu'un « KO » de harnais lu trop vite devient un défaut imaginaire :
   - **T4** : j'avais déverrouillé l'île 6 sans poser `archiVu6`, ce qui déclenche **légitimement**
     la révélation → `.arch-brume` présente. Ce n'est pas une brume attachée au marqueur, c'est la
     fonctionnalité attendue. Montage corrigé (`archiVu6 = true` pour isoler le marqueur), et la
     révélation testée **séparément** en non-régression.
   - **T5** : mon sélecteur `/port/i` sur les `title` matchait aussi **« Exporter »**. Remplacé par
     `[data-tut="port"]`, avec une attente que le HUD re-rende (le bouton est `disabled` tant
     qu'aucune deuxième île n'est accessible).
   - **T7** : `SFX.names` n'existe pas. Remplacé par un espion sur `SFX.play` **plus** un contrôle à
     la source des sites d'appel.

Aucun autre écart : les 13 ancres sont sorties uniques, aucune n'a dû être re-dérivée, et les deux
pièges d'ancre signalés par le brief (`pp_tabs` borné sur `I18N.t("Transit archipel"))), tab ===
'island' ? `, `pp_else` remplacé par `)` et non par le vide) étaient déjà refermés dans le patcher —
je ne les ai pas « simplifiés ».

---

## 6. i18n : tranché, et pourquoi

Le brief demandait de trancher et de l'écrire. **Audit d'abord**, plutôt que de supposer :

| libellé | entrées de table avant | décision |
|---|---|---|
| « Flux entre îles » | **3** (en/es/de) | déjà traduit, rien à faire — le brief le pensait, c'est vérifié |
| « Aucun flux de transit en cours. » | **3** | déjà traduit |
| « Carte de l'archipel » | **0** | **ajouté** |
| « Carte » | **0** | **ajouté** |

**Ajout retenu** parce que ce sont du **texte visible** — titre de panneau et libellé de bouton du
HUD — et non un `title` comme le « Faire défiler » du lot 1, que j'avais assumé en français. Le
brief souligne lui-même que l'arbitrage n'est pas le même ; laisser deux mots français en dur dans
le HUD d'un jeu qui tient quatre langues serait une régression visible, pour un coût de deux clés.

Implémentation : **IIFE d'augmentation** sur le motif des blocs 14.32 / 14.54, avec la garde de
fusion `if(!L.ui[k])` qui **n'écrase jamais** une entrée existante.
⚠ L'ancre a dû être re-choisie : le motif de fermeture de ces IIFE apparaît **3 fois** dans le
fichier — l'assertion `count == 1` l'a arrêté avant toute écriture, et l'ancre retenue est un
fragment unique du bloc 14.54.

Rendu vérifié **en jeu, dans les quatre langues** :

```
fr | bouton "Carte"  titre "Carte de l'archipel"      section "Flux entre îles"
en | bouton "Map"    titre "Archipelago map"          section "Flows between islands"
es | bouton "Mapa"   titre "Mapa del archipiélago"    section "Flujos entre islas"
de | bouton "Karte"  titre "Karte des Archipels"      section "Flüsse zwischen Inseln"
```

⚠ **`I18N.applyToData` fusionne les tableaux `body` par index** — sans objet ici : l'ajout porte sur
`L.ui` (clés plates), pas sur des données à tableaux. Aucun audit de cardinalité n'est requis.

---

## 7. Points en suspens

- **`I18N.t("Faire défiler")` du lot 1 reste non traduit** (les chevrons de la bande d'onglets).
  Décision inchangée : `title`/`aria-label` seuls, aucun texte à l'écran → **lot i18n de l'audit
  381**. Le contraste avec le présent lot est délibéré et documenté.
- **Hors périmètre, non anticipé** : la navigation par clic sur les îles (**lot 3**) et la
  réparation unifiée (**lot 4**). Le bouton 🛠 et `RepairModal` sont **laissés strictement intacts** —
  les fusionner maintenant rendrait la réparation inaccessible pendant deux lots.
- **Nom du rapport vérifié libre avant écriture** (leçon du build 15.1).
