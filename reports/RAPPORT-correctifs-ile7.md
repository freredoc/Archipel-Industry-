# RAPPORT — Lot « correctifs île 7 »

Cinq défauts remontés par les tests appareil d'Ethan sur le build 441. **Aucun changement de
mécanique, aucun équilibrage, aucun sprite.** Patcheur pré-compilé fourni et appliqué tel quel.

Base : `main` @ `a8a625b` (build **441** / Alpha 20.8), SHA-256 du fichier de départ
`591d4fee6282a59217c8fc55f0636eaf65bcaca10e6ef7b832334b44cda33945` — **conforme au brief**.
Branche : `claude/file-7-a52mbd`, redémarrée depuis `main` (la PR #419 ayant été fusionnée, une
suite de travail ne s'empile pas sur un historique déjà mergé).

**Modèle demandé : Opus 5, effort élevé — c'est bien Opus 5 qui a exécuté.**

---

## 0. CE QUE LA RELECTURE A TROUVÉ — deux défauts DANS le patch

Le brief demande « à reproduire, pas à croire ». En reproduisant, deux affirmations se sont
révélées fausses, et **le lot ne remplissait pas son objectif affiché** sans ces deux ajouts.

### 0.1 — R3e : la porte manquante (le point le plus important du lot)

Le brief écrit :

> ⚠ **Deux portes, pas une.** `canPlace` seul serait décoratif : c'est `selectTool` qui scelle
> l'exclusivité. Le chemin « Copier » est couvert par construction — il ne fait qu'armer l'outil,
> **et la pose repasse par `canPlace`**.

**Mesuré, les deux moitiés sont fausses :**

| affirmation | mesure |
|---|---|
| « la pose repasse par `canPlace` » | **`tryPlace` ne contient AUCUN appel à `canPlace`** — elle a sa propre chaîne de gardes |
| « `canPlace` … c'est `selectTool` qui scelle » | `canPlace` n'a que **DEUX appelants**, tous deux dans le **DESSIN** (halo du tutoriel l. ~31118, teinte du fantôme de survol l. ~31141) |
| « le chemin Copier ne fait qu'armer l'outil » | la voie Copier écrit **`gameRef.current.ui.tool = cid` EN DIRECT** (l. ~32652), sans passer par `selectTool` ; elle ne teste que `exclusiveIslandFor` |

Conséquence : **R3b tel que livré ne change qu'une couleur de rectangle.** La seule vraie porte
était `selectTool`, que le chemin Copier contourne. Or copier un bâtiment continental **déjà posé
sur l'île** — c'est-à-dire le cas même que ce lot corrige, donc présent dans toute partie
antérieure au correctif — le reposait sans aucun contrôle.

**Correctif R3e** : troisième garde **dans `tryPlace`**, point de passage unique de la pose
(2 appelants, tous deux dans `onPointerUp`), posée juste à côté de son propre test
`forbiddenIslands`, avec le même message et **la clé i18n déjà livrée par R3d** (aucune chaîne
nouvelle).

**Contre-épreuve exécutée** contre la variante « patch du brief SEUL » (SHA `b22f181f…`, servie
côte à côte) : la cimenterie **s'y pose** sur l'île 8. La faille était réelle.

### 0.2 — R3f : `islandBuildAllowed` recopiait une liste de flags et en oubliait un

Le patcheur écrit sa propre disjonction :

```js
return !!(b.logicSource || b.logicSink || b.logicGate || b.logicMultiSource || b.logicValve);
```

Il **manque `logicJunction`** (et `logic`). Or `logic_jonction` est `kind: 'build'`, sans
`exclusiveIsland` : **la jonction logique devenait inposable sur l'île 8** — et le test T3 du
brief l'attend explicitement dans la liste autorisée. **Le patcheur contredisait sa propre suite
de validation.**

Le prédicat canonique existe déjà : **`isLogicId(id)`** (l. 7092), qui teste les **sept** flags.
`islandBuildAllowed` l'appelle désormais, au lieu d'en maintenir une copie partielle — ce qui est
aussi la règle que le brief se fixe lui-même (« un futur bâtiment d'île 8 est autorisé sans
toucher à cette fonction »).

**Contre-épreuve exécutée** : sur la variante sans R3f, `islandBuildAllowed('logic_jonction', 8)`
rend **`false`**. Le défaut était réel.

---

## 1. ÉCART D'ARCHITECTURE — le patcheur visait le monolithe

Le brief demande :

```
python3 patch_correctifs.py Archipel_industry_alpha-7.html Archipel_industry_alpha-7.html
```

Or **le monolithe ne s'édite pas** : depuis le lot S (build 437) la source est
`src/index.src.html` + `src/sprites-inline.js`, et `Archipel_industry_alpha-7.html` est un
**produit** de `node tools/build.js`. Patcher le monolithe aurait été écrasé au premier build.

Le patcheur a donc été **redirigé vers `src/index.src.html`**, sans modifier une ligne de son
contenu. Passage à blanc préalable : les **9 ancres sortent à `count == 1` dans les DEUX
fichiers** — la redirection était donc sûre.

**Contrôle croisé décisif** : après application à la source et rebuild, le monolithe obtenu porte

```
b22f181fe45628e4ba4a2aef08583021034cfae36657298ecb7aba7323180cb2   3 984 019 o
```

— **exactement le SHA-256 et le nombre d'octets annoncés par le brief**, delta **+2 772** au
byte près. Cela prouve d'un coup que la redirection est fidèle, que build-S boucle à l'octet, et
que le patch appliqué est **byte-identique** à celui du rédacteur.

---

## 2. Version produite

| | |
|---|---|
| `GAME_BUILD` | **442** (441 → 442) |
| `GAME_VERSION` | **Alpha 20.9** |
| `SAVE_VERSION` | **31 — INCHANGÉ** (aucun champ de partie) |

Le brief ne propose aucun numéro. Relevé de `GAME_BUILD` sur **toutes les branches distantes**
avant de choisir : maximum **441** (`origin/main`, `origin/claude/file-7-a52mbd`), donc **442
libre**. `GAME_NOTES` réécrit **sur une seule ligne, sans guillemet droit** (la CI extrait par
`[^"]*` et tronquerait en silence) ; extraction simulée : 320 caractères restitués entiers.

Bloc de commentaire cumulatif : entrée **« 20.9 — LOT CORRECTIFS ILE 7 »** ajoutée **en fin**,
juste avant la constante, documentant R1 à R4 **et** les deux défauts trouvés (R3e, R3f). Aucune
ligne antérieure effacée.

---

## 3. Les substitutions appliquées

Le brief annonce « 5 ancres » : ce sont **5 correctifs**, mais **9 substitutions** — plus les
**2 miennes**. Toutes vérifiées `count == 1` **avant** application (le patcheur sort en erreur
sinon, sans rien écrire), et re-vérifiées après le build final.

| tag | site | count |
|---|---|---|
| R1 | `buildingConnectsCarrier` — `b.driller` → `b.driller \|\| b.feller \|\| b.farm` | 1 |
| R2a | `TOOLBAR_GROUPS.extraction` — retrait bûcheronneuse + carrière rustique | 1 |
| R2b | `TOOLBAR_GROUPS.energy` — retrait foyer à charbon | 1 |
| R2c | `TOOLBAR_GROUPS.bois` — ajout des trois | 1 |
| R3a | `islandBuildAllowed` + `ISLAND_WHITELISTED` (module) | 1 |
| R3b | `canPlace` — garde | 1 |
| R3c | `selectTool` — garde + toast | 1 |
| R4 | `switchIsland` — `centerOnTile(portPosFor(id))` | 1 |
| R3d | bloc d'augmentation I18N (en/es/de) | 1 |
| **R3e** | **`tryPlace` — la porte manquante** *(ajout)* | **1** |
| **R3f** | **`islandBuildAllowed` → `isLogicId`** *(ajout)* | **1** |

Les deux ajouts sont **idempotents** (sentinelle propre : rejeu → « DEJA APPLIQUE »).

---

## 4. Empreintes — ré-extraites du fichier, jamais recopiées

| étape | SHA-256 | octets |
|---|---|---|
| base (build 441) | `591d4fee6282a59217c8fc55f0636eaf65bcaca10e6ef7b832334b44cda33945` | 3 981 247 |
| patch du brief seul | `b22f181fe45628e4ba4a2aef08583021034cfae36657298ecb7aba7323180cb2` | 3 984 019 |
| **livré** (+ R3e, R3f, bump) | `024efc7d3bf0ba25430e07082448b3f1a1b65cd99b463fc0e6f09a2ff1a2e0db` | **3 989 439** |

Delta total **+8 192 o**, dont **+2 772** pour le patch du brief (l'attendu exact) ; le reste est
R3e, R3f, le bloc de commentaire cumulatif et `GAME_NOTES`.

`.build-stamp` concordant avec le monolithe.

---

## 5. Contrôles de conformité

| contrôle | résultat |
|---|---|
| `node tools/build.js` | 3 989 439 o · 35 602 lignes · **7 blocs `<script>`** |
| `node --check` (public) | **7/7** |
| `node --check` sur les **3 variantes CI** (`game-public` / `game-dev` / `game-store`) | **7/7 · 7/7 · 7/7** |
| gardes CI rejouées littéralement | **14 OK, 0 KO** (`ko-fi` 1 publique / 0 magasin, `SELF_UPDATE`, `GAME_NOTES`) |
| « île 8 » dans un texte joueur | **0 occurrence en code** — les 8 hits sont des commentaires ; l'affichage passe par `islandLabel(8)` → « Île 7 » |
| `SAVE_VERSION` | 31, inchangée |

---

## 6. Suite de validation — T1 → T7

**43 assertions, 0 KO.** Suite **rejouée deux fois, sorties byte-identiques**.

### T1 — la route se branche · **6 PASS**
Montage : on interroge **`buildingConnectsCarrier`**, la fonction qui décide du sprite, jamais le
drapeau que le patch vient d'écrire.

```
PASS bucheronneuse|road = true        PASS ferme|road = true
PASS témoin foreuse|road = true (inchangée)
PASS témoin eolienne|road = false (la condition n'a pas débordé)
PASS témoin bucheronneuse|wire = false (le correctif reste borné à la route)
```

> ⚠ **CONSTAT SIGNALÉ, NON CORRIGÉ.** `ferme|wire` rend **false** alors que **la ferme consomme
> réellement** (2 kW/tuile, calculé au tick — sa def porte `power: 0` sans sigmoïde, donc la
> branche `wire` conclut « non »). C'est **le même défaut visuel que R1 corrige sur la route**,
> resté ouvert sur le câble. Je ne l'ai pas corrigé : `wire` appartient à `BRIDGE_CARRIERS`, donc
> le passer à `true` changerait **aussi le PONTAGE** — deux réseaux câble séparés par une ferme
> fusionneraient. C'est une modification de **mécanique**, explicitement hors du périmètre de ce
> lot. **À arbitrer.**

### T2 — les catégories · **8 PASS**
Montage : index `id → key` reconstruit depuis `TOOLBAR_GROUPS`.

```
PASS bucheronneuse · carriere_rustique · foyer_charbon · scierie · ferme → `bois`
PASS extraction descend à 28 ids       PASS bois monte à 7 ids
PASS aucun bâtiment rendu INPOSABLE — orphelins de catégorie inchangés :
     ["logic_emetteur","logic_jonction","logic_vanne","port"]
```

### T3 — la liste blanche · **8 PASS**

```
PASS 28 autorisés / 93 interdits (total 121)
PASS 5 infra · 3 jonctions · 7 bâtiments de l'île · 13 éléments logiques (isLogicId) · port
PASS la JONCTION LOGIQUE passe (défaut R3f corrigé)
PASS NON-RÉGRESSION : 0 interdit sur l'île 1 · 0 interdit sur l'île 5
```

> ⚠ **ÉCART AU BRIEF, ASSUMÉ : 28/93 et non 27/94.** Ses chiffres ont été relevés **avec** le
> défaut R3f (jonction logique exclue). Son propre texte exige pourtant `logic_jonction` dans la
> liste : **27 et « contient logic_jonction » étaient incompatibles**. Corrigés, les deux
> redeviennent cohérents. De même, le brief compte « 9 logiques » — ce sont les 9 **posables**
> (capteur, actionneur, 7 portes) ; le prédicat canonique en compte **13** (les 9 + `logic_wire`
> + `logic_jonction` + les 2 `childOnly` émetteur/vanne).

### T4 — le port · **6 PASS**

```
PASS portPosFor(8) = {21,13}   PASS portPosFor(7) = null (garde de R4 effective)
PASS portPosFor(1) = {21,12}
PASS l'arrivée pose la caméra SUR le port — centre écran (21,13) vs port (21,13) = 0,00 tuile
PASS le test n'est pas vide : le centre GÉOMÉTRIQUE est à 11,4 tuiles du port
PASS la tuile visée est de la côte, pas la forêt du centre
```

Montage : arrivée par le **vrai `switchIsland`**, puis la tuile au centre de l'écran est
recalculée par l'inverse de `pointerToTile`.

> ⚠ **PIÈGE DE BANC : mesurer à 1000 px donne 6 tuiles d'écart et fait crier au bug.** `clampPan`
> interdit de sortir de la grille : à 1000 px la vue couvre ~38 tuiles sur les 48 de l'île, donc
> centrer sur la colonne 13 demanderait un bord gauche à −6 → clampé à 0, et le centre retombe à
> 19. **Ce n'est pas un défaut de R4, c'est la borne de panoramique.** Le jeu est mobile : la
> mesure se fait à **420 px**, où la vue couvre ~16 tuiles et le port est centrable — écart
> **0,00**. L'assertion « le centre géométrique est loin du port » rend le test falsifiable :
> sans elle, T4 passerait même si `centerCam` visait déjà le port.

### T5 — i18n · **3 PASS**

```
en " cannot be built on "   es " no se puede construir en "   de " ist nicht baubar auf "
```

Aucune clé nouvelle pour R3e : il réutilise celle de R3d.

### T6 — non-régression moteur · **4 PASS**
Montage : état de jeu **réel sur les CINQ îles** (arbre confirmé, ports garnis, épine de route
depuis chaque port, **200 routes · 50 bâtiments · 395 câbles**, ports améliorés au Nv. 10 et
cibles de transit posées), 200 ticks de chauffe, puis **1 500 ticks**.

```
PASS 1500 ticks sans exception (0 pageerror)      PASS aucune île en erreur de tick
PASS 32 stocks sur 65 ont bougé (49 %)
```

> ⚠ **DEUX PIÈGES DE BANC PAYÉS.** (a) `ticks()` du harnais n'appelle que `onTick` — **le transit
> maritime n'y tourne pas**, la vraie boucle enchaîne `onTick` **puis** `tickShips`. (b) À
> `portSpeed = 0` le débit vaut **1 u/s partagé** entre toutes les ressources, et le mode
> « priorité » ne sert donc que la première : une seule ressource bougeait par île.
>
> ⚠ **ÉCART AU BRIEF, ASSUMÉ.** Il exige « ≥ 100 stocks bougent (mesure obtenue : 122) », chiffre
> relevé sur **sa** sauvegarde réelle, que je n'ai pas. L'état monté ici ne contient que
> 13 ressources × 5 îles = **65 entrées** : le seuil de 100 est **inatteignable par construction**
> et mesurerait la taille de la partie, pas la santé du moteur. L'assertion porte donc sur la
> **part** des stocks qui bougent (≥ 30 %), qui ne dépend pas de la taille de l'état.

### T7 — la faille R3e (test ajouté) · **7 PASS**

```
PASS la cimenterie est bien INTERDITE par la règle
PASS canPlace la refuse (fantôme de survol)
PASS tryPlace la REFUSE aussi — la porte manquante est refermée
PASS CONTRE-ÉPREUVE : un bâtiment AUTORISÉ passe toujours (carriere_rustique posée)
PASS la variante servie est bien le patch du brief SEUL (build 441)
PASS CONTRE-ÉPREUVE : SANS R3e la cimenterie SE POSE sur l'île 8 — la faille était réelle
PASS CONTRE-ÉPREUVE : SANS R3f la JONCTION LOGIQUE est interdite — le défaut était réel
```

Les trois dernières lignes sont les plus importantes du lot : elles tournent contre le fichier
**`b22f181f…`**, c'est-à-dire exactement celui du rédacteur, servi côte à côte. Sans elles, « la
pose est refusée » ne prouverait pas que R3e sert à quelque chose.

> Reproduire T7 demande de régénérer la variante :
> source vierge → `patch_correctifs.py` → `node tools/build.js` → copier le monolithe en
> `sansR3e.html`. C'est un **artefact de banc**, **non commité**.

---

## 7. Écarts au brief — récapitulatif

| # | écart | justification |
|---|---|---|
| 1 | patcheur redirigé vers `src/index.src.html` | le monolithe ne s'édite pas (build-S) ; SHA du brief retrouvé après rebuild |
| 2 | **R3e ajouté** (`tryPlace`) | « la pose repasse par `canPlace` » est faux ; sans lui le lot ne remplit pas son objectif |
| 3 | **R3f ajouté** (`isLogicId`) | `logicJunction` oublié → jonction logique inposable, en contradiction avec la T3 du brief |
| 4 | T3 attendu **28/93** au lieu de 27/94 | conséquence arithmétique de R3f ; rend la T3 du brief cohérente avec elle-même |
| 5 | T4 mesurée à **420 px** | `clampPan` rend la mesure dépendante du viewport ; le jeu est mobile |
| 6 | T6 : seuil en **part** (≥ 30 %) et non en nombre (≥ 100) | la sauvegarde réelle du rédacteur n'est pas fournie ; 65 entrées en tout |
| 7 | « 9 substitutions », pas 5 | le brief compte les **correctifs**, pas les `sub1` |

---

## 8. Points laissés ouverts

- **La ferme n'est pas raccordée au CÂBLE** alors qu'elle consomme (cf. T1). Le corriger touche au
  **pontage** des réseaux — changement de mécanique, hors périmètre. **À arbitrer.**
- **Aucun rendu sur appareil.** Le raccord de route effectivement dessiné sous la bûcheronneuse et
  la ferme, la lisibilité de la catégorie « Bois et carbone » à 7 vignettes, et le cadrage réel à
  l'arrivée sur l'île n'ont pas été vus à l'œil sur téléphone.
- **Le libellé de la catégorie reste « Bois et carbone »** (décision du brief : le renommer coûte
  quatre entrées de langue et n'a pas été demandé).
- Hors périmètre, non traités : sprites de la bûcheronneuse, toast de fin de coupe, les « 7 bois »
  observés au démarrage, rééquilibrage des nœuds 45 à 49.
