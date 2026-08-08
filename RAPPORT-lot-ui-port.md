# RAPPORT — Lot « UI & Port »

## Versions produites

| | Base | Livré |
|---|---|---|
| `GAME_BUILD` | 371 | **372** |
| `GAME_VERSION` | Alpha 14.88 | **Alpha 14.89** |
| `SAVE_VERSION` | 31 | **31 (INCHANGÉ)** |
| Taille | 3 289 188 o | **3 303 182 o** |

Base de référence **EXACTE** au brief (build 371 / 14.88 / 3 289 188 o) : aucune ancre n'a eu à
être ré-adaptée.

### Delta d'octets

| Poste | Octets |
|---|---|
| 27 blocs des 7 chantiers + bump | **+13 259** |
| `GAME_NOTES` + note « cargo » (mesurée, cf. §1) | +735 |
| **Total** | **+13 994** |

Les 2 sprites PNG inlinés pèsent ~1 100 o de ce total ; le reste est très majoritairement du
commentaire de décision (conventions du projet).

### SHA-256 des 7 blocs `<script>`, RE-EXTRAITS du fichier patché

```
blk1  a50c1c4e7f4a304c650c0cfa7e06c4ffdfb0d87510d83738114d4dd5a1641628      413 o
blk2  8fbb22187703339c146b2f82badd8701d199128cd3d9dd8673d213c8616ca541    4 341 o
blk3  d949f1c3687aedadcedac85261865f29b17cd273997e7f6b2bfc53b2f9d4c4dd   10 751 o
blk4  35f4f974f4b2bcd44da73963347f8952e341f83909e4498227d4e26b98f66f0d  131 835 o
blk5  8f111a1380cf98fca4e48d1fc2aa448199b93cab0be15072f68e7175ba98e426  1 111 021 o
blk6  8582f11695ef290087b8af9803ba12d1509e1087229d5b0cfbd63f616f6790d2  225 580 o
blk7  0087ece38ea42ca59f82b0586d9607bbc64c644ecdd2434772502a8e87ed7fc3  1 540 762 o
```

## Contrôles de méthode

- **29 paires ancre/remplacement**, toutes vérifiées **`count == 1` AVANT écriture**. Aucune ancre
  n'a été retapée : toutes extraites du fichier par script (le fichier mêle UTF-8 littéral et
  échappements `\xNN`).
- **Round-trip : 29/29 blocs retrouvés VERBATIM** (`count == 1`) dans le fichier compilé.
- **`node --check` : 7 blocs, 7 OK**, sur l'édition PUBLIQUE **et** sur l'édition DEV
  (`sed 's/^const DEV_BUILD = false;$/const DEV_BUILD = true;/'`).
- Taille mesurée par `os.path.getsize`, jamais `len(text)`.
- **4 ancres du brief avaient une indentation différente du fichier réel** (`port: 'tile_port_terre',`,
  `const sk = buildingSpriteKey(...)`, `drawAnimFrame(ctx, 'tile_port_mer', ...)`,
  `const consumers = energyConsumerList(...)`) → ré-extraites du fichier, `count == 1` ensuite.
  `buildingSpriteKey(id, upg, drillDir)` apparaît **2 fois** (le 2ᵉ site est le fantôme de
  construction souterraine) : c'est l'indentation à 6 espaces qui désambiguïse.

## Résultats des tests

Harnais Chromium (`/opt/pw-browsers/chromium-1194`), serveur lancé **depuis le dépôt**,
viewport 420×900 sauf mention. **Build effectif : 372 / Alpha 14.89.**

**46 assertions, 0 KO, rejouées 2 fois sans flottement.** Console : **0 erreur**
(seul bruit filtré : le 404 favicon/`version.json` PRÉEXISTANT du serveur de test).

### Chantier 1 — Port cassé (10/10)

| # | Résultat | Mesure |
|---|---|---|
| 1.1 | **PASS** | île 1, nœud 2 non confirmé → `tile_port_terre_casse` **et** `tile_port_mer_casse` dessinés, les sprites normaux absents |
| 1.2 | **PASS** | nœud 2 confirmé → retour aux sprites normaux **sans changement d'île** (`portCasse` false, `currentIsland` 1) |
| 1.3 | **PASS** | île 6 → `portCasse` **false** quel que soit l'état des nœuds (`ISLAND_ACCESS_NODE[7]` undefined) ; île 5 → cassée tant que le nœud 28 n'est pas confirmé |
| 1.4 | **PASS** | port cassé + 5 routes posées autour du port → réseau **`connected`** : aucun effet fonctionnel |
| 1.5 | **PASS** | `tile_port_terre_casse` retiré de `SPRITE_DATA` → repli sur `tile_port_terre`, **aucune tuile vide** |
| 1.6 | **PASS** | capture jointe, cf. §« lisibilité » |
| 1.7 | **PASS** | data-URL re-décodées **depuis le fichier patché** : SHA-256 **identiques** (`db138ef7…` 441 o, `2782bcf9…` 298 o) |
| 1.8 | **PASS** | `tile_port_mer_casse` : colortype **3 (palette)** + chunk **`tRNS` présent (1 entrée)** → transparence PRÉSERVÉE, aucun carré opaque |

**Non-régression du TUTORIEL (3/3)** — le test qui protège le §1.3 : partie neuve, tutoriel actif,
port de l'île 1 **cassé**, pose d'une mine + route → mine **reliée** (`tutCountConnected` = 1) et le
tutoriel **progresse jusqu'à « Tuto 3/10 »**. 0 erreur de tick.

### Chantier 2 — « Demander au port » (3/3)

| # | Résultat | Mesure |
|---|---|---|
| 2.1 | **PASS** | île 2 verrouillée + déficit → bouton **ABSENT** |
| 2.2 | **PASS** | île 2 débloquée + déficit → bouton **PRÉSENT** |
| 2.3 | **PASS** | île 2 débloquée, port plein → bouton absent (règle `askNeeded` d'origine préservée) |

`askNeeded` n'est lu **nulle part ailleurs** dans les deux composants (vérifié : 1 déclaration +
1 usage chacun) → neutralisation en amont, point de décision unique, comme recommandé.

### Chantier 3 — Carré de couleur → sprite (5/5)

| # | Résultat | Mesure |
|---|---|---|
| 3.1 | **PASS** | « amélioration immédiate » décochée, outil Améliorer, **tap canvas RÉEL** → en-tête = `<img class="ip-sprite">`, plus de `.ip-swatch`, image non cassée |
| 3.2 | **PASS** | 37 sprites de porte AND retirés → repli **`.ip-swatch` propre**, **aucune image cassée** |
| 3.3 | **PASS** | Élévateur non réparé → `tile_i6_elevateur_casse` ; réparé → `tile_i6_elevateur` |
| 3.4 | **PASS** | fiche d'une porte AND → vignette `logic_porte_and` |
| 3.5 | **PASS** (statique) | les 3 nouveaux sites sont gatés sur `SPRITES_ENABLED` **et** testent `SPRITE_DATA[...]` avant substitution, avec repli `.ip-swatch` |

⚠ **Test 3.5 non exécutable au runtime** : `SPRITES_ENABLED` est une **`const` de module**, non
réassignable (piège déjà documenté en 14.86). Le contrôle est donc fait **sur la source**, sur les
3 sites, plutôt qu'en jeu.

### Chantier 4 — Lignes à 0 kW (5/5 + contre-épreuve)

| # | Résultat | Mesure |
|---|---|---|
| 4.1 | **PASS** | 4 consommateurs dont 1 à 0 kW **au milieu** → **3 lignes**, la 4ᵉ absente |
| 4.2 | **PASS** | rangs affichés **1, 3, 4** = ordre RÉEL de `energyPriority`, et non 1,2,3 renumérotés |
| 4.3 | **PASS** | 1ʳᵉ ligne visible → Monter grisé ; dernière ligne visible → Descendre grisé |
| 4.4 | **PASS** | ordre `[A, Z=0, B, C]`, « Descendre » sur A → `[Z, B, A, C]` : **A et B échangés**, l'entrée masquée SAUTÉE, déplacement **visible** |
| 4.5 | **PASS** | tous à 0 kW → 0 ligne + « Aucun consommateur », **aucun crash** |
| 4.6 | **PASS** | **contre-épreuve sur la BASE 371** : 4.1, 4.2, 4.4 et 4.5 **ÉCHOUENT** — les tests sont falsifiables |

La contre-épreuve reproduit exactement le piège n°3 du brief : sur la base, « Descendre » sur A
échange A avec l'entrée **masquée** (`[A,Z,…]` → `[Z,A,…]`) → **rien ne bouge à l'écran**.

⚠ **Aucun bâtiment du jeu n'a une puissance nominale nulle** au niveau 0 (vérifié sur les 69
consommateurs). Le cas 0 kW a donc été construit en enveloppant `energyConsumerList` (déclaration de
fonction d'un script classique → réassignable via `window`), ce qui exerce **le vrai chemin de rendu
ET `moveEnergyPriority`**.

### Chantier 5 — Options (3/3 + contre-épreuve)

Mesuré sur les 3 lignes à `<select>` (Langue, Grands nombres, Seuil de bascule) :

| Viewport | Titre | Description | Largeur desc. | `<select>` | Chevauchement | Débordement |
|---|---|---|---|---|---|---|
| 360 px | 1 ligne | ≤ 3 lignes | 276 px | 128-132 px | aucun | aucun |
| 320 px | 1 ligne | ≤ 3 lignes | 239 px | 128-132 px | aucun | aucun |
| 900 px | 1 ligne | ≤ 2 lignes | 278-418 px | 128-132 px | aucun | aucun |

**Contre-épreuve BASE 371** — le défaut est reproduit et chiffré :
- @360 : « Grands nombres » titre sur **2 lignes**, description sur **12 lignes** pour une largeur
  de **0 px** (= un mot par ligne), `<select>` à **268 px** ; « Seuil de bascule » titre 3 lignes,
  description 9 lignes.
- @320 : le `<select>` de « Grands nombres » **DÉBORDE** du panneau.
- @900 : « Grands nombres » titre encore sur 2 lignes.

### Chantier 6 — Colonnes du Port (2/2 + contre-épreuve)

Panneau Port de l'île 6, onglet « Transit île », **40 lignes** :

| Viewport | Écart max des colonnes | Boutons hors panneau | Noms rognés |
|---|---|---|---|
| **patché** @360 | **0 px** | **0 / 80** | 0 |
| **patché** @420 | **0 px** | **0 / 80** | 0 |
| base 371 @360 | 73 px | **26 / 80** | 0 |
| base 371 @420 | 17 px | **1 / 80** | 0 |

Test 6.4 (le plus discriminant) couvert : le nom le plus long est **« mot.quantique »**, et c'est
bien sa 2ᵉ flèche qui sortait du panneau sur la base (1 bouton hors panneau @420).

**Mesure de la dernière colonne** (comme demandé avant de choisir la largeur) : icône `.ui-ico`
16 px + padding 2×3 px + bordure 2×1 px = **24 px par bouton**, + `gap:2px` → **50 px** pour deux
boutons. La colonne était à **44 px** → élargie à **52 px**. Première colonne passée en
`minmax(0,1fr)` + `.pp-c-res{min-width:0;overflow-wrap:anywhere;}`.

### Chantier 7 — Démarrage automatique (11/11)

| # | Résultat | Mesure |
|---|---|---|
| 7.1 | **PASS** | palier 2 → bouton **ABSENT** ; palier 3 mais nœud 43 non confirmé → **ABSENT** |
| 7.2 | **PASS** | palier 3 + nœud 43 confirmé → bouton **PRÉSENT** (« ○ Démarrage automatique de la séquence ») |
| 7.3 | **PASS** | bascule active (activée par un **vrai clic**), `state='ready'` → `state === 'running'` et `launched` **sans clic** |
| 7.4 | **PASS** | réseau logique retiré → la machine **NE démarre PAS**, `colliderLaunchBlock === 'logic'` |
| 7.5 | **PASS** | **5 cycles** ready→running → 5 lancements et **0 notification** (lancement silencieux) |
| 7.6 | **PASS** | `autoLaunch: true` **sérialisé** dans la save, **toujours actif** après rechargement réel, `SAVE_VERSION` toujours 31 |
| 7.7 | **PASS** | save **privée du champ** `autoLaunch` (réinjectée par `addInitScript` pour survivre au flush `pagehide`) → `false`, **0 erreur de tick** |

Le test 7.4 prouve que **`launchCollider()` a bien été réutilisé** et non contourné.
Le lancement automatique appelle `launchCollider(true)` : `silent` supprime toast **et** SFX, au
lancement comme au refus.

⚠ **Emplacement du crochet** : la vérification est placée **dans la boucle de tick**
(`while (g.tickAcc >= 1 …)`, juste après `onTick`), et non dans la frame rAF — `co.state` vient
d'être recalculé par `processCollider`, donc la transition est vue au tick où elle se produit.

### Boot des 2 éditions

| Édition | `DEV_BUILD` | Canvas peint | Ticks / 6 s | Erreurs tick | Console |
|---|---|---|---|---|---|
| PUBLIQUE | false | **100 %** | 6 | 0 | **aucune** |
| DEV | true | **100 %** | 6 | 0 | **aucune** |

Aucune page blanche.

---

## Écarts au brief, et pourquoi

### 1. ⚠ CARGO — la prémisse du brief est FAUSSE à partir de l'île 2 (mesuré, non corrigé)

Le §1.4 pose : « quand le port de l'île courante est cassé, aucune liaison n'est active, donc
`dockProximity` ne devrait rien produire », et demande de corriger si un cargo s'anime.

**Mesuré :**

| Île | Port cassé | Liaison active | Cargo |
|---|---|---|---|
| 1 (partie neuve) | oui | **non** (`linkActive('1-2')` false) | aucun — prémisse VRAIE |
| 2 (nœud 2 confirmé, nœud 8 non) | **oui** | **OUI** | **un cargo s'anime** |

La prémisse ne tient que pour l'île 1, parce que c'est le **même** nœud 2 qui casse son port et qui
active la liaison 1-2. Dès l'île 2, le port est « cassé » tant que le **nœud 8** n'est pas confirmé,
alors que la liaison avec l'île 1 fonctionne déjà.

**Le cargo n'a PAS été masqué**, et c'est délibéré : le §1.3 pose que l'état cassé est **purement
visuel** et que le port reste fonctionnel. L'île 2 commerce réellement avec l'île 1 ; masquer le
cargo supprimerait une information **vraie** et contredirait la contrainte de design du brief
lui-même. Le constat est consigné **en commentaire dans `drawPortExtras`** pour qu'une session
future ne le re-dérive pas. **À arbitrer par Ethan si le rendu choque.**

### 2. ⚠ Test 3.3 — la fiche « Élévateur cassé » n'est PAS le site patché

Le panneau que le §3b désigne (`ip-swatch` orange `#FF9628`) n'est rendu **que si
`elevatorRepaired` est VRAI** (`if (info.mode === 'elevator' && game.elevatorRepaired)`). Non
réparé, le tap tombe sur le **panneau de RÉPARATION**, qui affiche déjà `tile_i6_elevateur_casse`
depuis 14.08.

Conséquence : le patch 3b corrige bien le carré orange (fiche de l'élévateur **réparé**), et sa
branche `casse` est **actuellement inatteignable**. Elle est conservée — le brief demande
explicitement de « choisir selon `game.elevatorRepaired`, comme le fait déjà le rendu de la tuile »
— mais il faut savoir qu'elle est défensive. Les deux moitiés du test 3.3 passent, l'une par le
code préexistant, l'autre par le patch.

### 3. ⚠ Chantier 4 — bornes des boutons : index FILTRÉ, pas index d'origine

Le brief demande d'« utiliser l'index d'origine pour **les bornes et le rang** ». Appliqué à la
lettre, cela **contredit son propre test 4.3** : avec un ordre `[A>0, B>0, Z=0]`, B a l'index
d'origine 1 ≠ 2, donc son bouton Descendre ne serait pas grisé alors que c'est la **dernière ligne
visible** — 4.3 échouerait. Symétriquement, avec `[Z=0, A>0, …]`, A aurait un bouton Monter actif
qui ne ferait **rien** (`movePriority` saute les entrées masquées).

Livré : **rang = index d'ORIGINE** (test 4.2), **bornes = position dans la liste VISIBLE**
(test 4.3). Les deux tests passent.

### 4. `SAVE_VERSION` — conforme, et le drapeau du `LISEZ-MOI` a été écarté

Le `LISEZ-MOI` du pack d'art conclut qu'il faut « ajouter un drapeau de partie sur le modèle de
`elevatorRepaired`, plus sa sérialisation ». Le brief l'interdit, et **aucun obstacle n'est apparu** :
l'état du port est **entièrement dérivé** de `ISLAND_ACCESS_NODE` + `isNodeConfirmed`. Aucun champ
ajouté, aucune migration, aucune désynchronisation possible. Les saves existantes affichent le bon
état dès le premier rendu.

Le seul champ ajouté de tout le lot est `collider.autoLaunch` (chantier 7), **additif** comme
`stops`/`enabled`/`launched` : absent d'une save antérieure = `false`.

### 5. Écart d'art confirmé au pixel (§1.1 du brief) — `tile_port_mer_casse` est une RECOLORATION

Mesuré sur les sprites re-décodés du fichier patché :

| Sprite | Pixels opaques | Luminance | Couleurs | Silhouette |
|---|---|---|---|---|
| `tile_port_terre` | 1024 | 130,7 | 30 | — |
| `tile_port_terre_casse` | 1024 | **82,7** (−37 %) | **17** | 1024/1024 identique |
| `tile_port_mer` | **271** | 73,9 | 9 | — |
| `tile_port_mer_casse` | **271** | **49,5** (−33 %) | 11 | **1024/1024 identique** |

Le brief a **raison contre le `LISEZ-MOI`** : la grue est debout, le ponton intact, **271 pixels
opaques avant comme après**. « La flèche de grue est au sol » n'est pas ce que contient le fichier.

**Test 1.6 — lisibilité en jeu (captures jointes, tuile 26 px, zoom par défaut) :** la ruine **se
lit**. La tuile terre perd tous ses accents vifs (conteneurs rouge/bleu/blanc, liseré de route
jaune) et vire au rouille sombre ; la tuile mer suit en luminance. Côté mer **seule**, la lecture
serait ambiguë — c'est la paire qui fait l'effet. Si Ethan veut une vraie destruction côté mer,
c'est **un remplacement d'art, pas un correctif de code** ; le PNG n'a pas été retouché.

### 6. Le liseré de route jaune du quai n'a PAS été restauré

Conforme au §1.1 : passage au rouille assumé, sans effet fonctionnel (les routes réelles sont
dessinées dans la passe réseau).

---

## Points restants

- **Lot « Gisements par exclusivité d'île » (6 overlays `overlay_resource_iN`) NON intégré** : le
  `LISEZ-MOI` du pack impose « un brief d'intégration par lot, jamais groupés », et seul le brief
  du lot Port a été fourni. Les 6 PNG sont dans le pack, en attente de leur brief.
- **Cargo devant un port en ruine (île 2 à 5)** : mesuré, documenté en commentaire, **non modifié**
  — arbitrage de design (cf. écart n°1).
- **Branche `casse` de la fiche Élévateur** : inatteignable en l'état (cf. écart n°2).
- **Hors périmètre, non touché** : conversion recherche → livraison (29 nœuds), halos d'antenne
  disparus après déficit, `BLD_SPRITE_OVERRIDE`, `buildingSpriteKey`, `energyConsumerList`
  elle-même, `moveEnergyPriority` hors saut des masquées, `SAVE_VERSION`.

## Pièges de harnais rencontrés (à ne pas redécouvrir)

1. **`window.__ui()` n'expose PAS `centerOnTile`/`setInfo`/`setPortOpen`** (seulement `tryPlace`,
   `canPlace`, `switchIsland`, `askPortFor`, `setLogicConfig`, `buyResearch`). Un
   `if (ui.centerOnTile) …` passe donc **silencieusement** et l'on croit centrer la caméra sans
   rien faire. Recaler `cam.x`/`cam.y` à la main (inverse exact de `pointerToTile`).
2. **L'INVENTAIRE ouvert se pose en SUPERPOSITION** sur le haut du canvas : `elementFromPoint`
   renvoie `inventory open` et tout tap y atterrit. Le replier avant tout tap canvas.
3. **Tout panneau ouvert avale le tap suivant** — y compris le panneau Options resté ouvert d'un
   test précédent, qui recouvrait le bouton PORT. Fermer les panneaux entre deux sections, ou
   isoler les sections dans des fichiers de test distincts.
4. **`addInitScript` REJOUE à chaque navigation, RELOAD COMPRIS** : un `localStorage.clear()` nu y
   fait repartir tout test de rechargement sur une partie NEUVE (7.6 a échoué ainsi). Le garder
   derrière un drapeau `sessionStorage`.
5. **Une save forgée dans `localStorage` est écrasée par le flush `pagehide`** à la navigation : la
   **réinjecter** dans un `addInitScript` (test 7.7).
6. **`useGhostGuard` avale le 1ᵉʳ clic** d'un panneau : amorcer par un `pointerdown` dispatché
   **dans** le panneau, puis réessayer jusqu'à effet (le toggle 7.x a été flaky sans cela).
7. **`tile_port_terre` est ANIMÉ** (sheet `tile_port_terre`, 14.08) : un espion `drawImage` voit
   `ANIM:tile_port_terre` et **pas** la clé statique. Asserter sur les deux.
8. **La pastille ⚡ du HUD est MASQUÉE tant que l'île 2 est verrouillée** (`islandTradeUnlocked`) :
   sans `islandUnlocked[2] = true`, le panneau Énergie est inatteignable.
9. **L'EnergyPanel porte les classes `research-panel port-panel`**, comme le PortPanel.
10. **Le panneau d'amélioration n'apparaît que si `g.ui.fastUpgrade` est FAUX** (« amélioration
    immédiate » décochée) ; sinon le tap améliore directement.
11. **`co.powered` non booléen ⇒ tick BLANC** (`processCollider` sort sans toucher `state`/`timer`).
    Poser un **getter** `powered → undefined` rend l'état `'ready'` déterministe pour tester le
    démarrage automatique, sans monter tout un réseau électrique + hélium 3.
