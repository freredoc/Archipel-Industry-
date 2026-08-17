# RAPPORT — Lot L7 (énergie du souterrain : suppression Géothermie V2, soft cap, gaz libre, séparateur cryogénique)

Brief : `BRIEFlotL7energiesouterrain` · patcheur `patch_L7.py` (pré-compilé, 13 ancres)
Branche : `claude/temps-souterrain-display-uoonrz`, **repartie de `main`** (la PR #375 a été mergée).

## Version produite

| | |
|---|---|
| `GAME_BUILD` | 392 → **393** |
| `GAME_VERSION` | Alpha 15.9 → **Alpha 16.0** |
| `SAVE_VERSION` | **31, INCHANGÉ** — aucune migration de format |
| Taille | 3 418 208 → **3 421 291** o (**+3 083**) |
| SHA-256 livré | `cbebae793bb81fe97f67e18b7a4c49d18e2338bdfe5ba9153e6dd09943880f19` |

⚠ **Le patch seul pèse `−379` o, exactement l'attendu du brief** (mesuré avant le bump :
3 418 208 → 3 417 829). Le lot retire plus qu'il n'ajoute. Le `+3 083` final est le commentaire
cumulatif — qui porte les cinq avertissements d'architecture — et `GAME_NOTES`.

## Sortie du patcheur

Base vérifiée `d5807a6f…` = build 392, **aucun avertissement**.

```
OK - 13 ancres appliquees
SHA-256 fichier patche : 3df81ea75e9ea678cd859dc860a79d825734c2c8c27b7e74e09627485037a77c
```

**Conforme au caractère près.**

**Contrôle intermédiaire, avant le bump : les 7 blocs étaient TOUS identiques aux SHA du brief**, y
compris les **deux qui bougent** — bloc 5 `eb79498a…` / 1 111 572 o (les assets retirés) et bloc 7
`da8100b3…` / 1 665 870 o (le code) — et la taille tombait sur 3 417 829 o exactement.

## SHA-256 des 7 blocs, ré-extraits APRÈS la toute dernière modification du HTML

| bloc | octets | sha256 |
|---|---|---|
| 1 | 418 | `a50c1c4e7f4a304c650c0cfa7e06c4ffdfb0d87510d83738114d4dd5a1641628` |
| 2 | 4 397 | `8fbb22187703339c146b2f82badd8701d199128cd3d9dd8673d213c8616ca541` |
| 3 | 10 751 | `d949f1c3687aedadcedac85261865f29b17cd273997e7f6b2bfc53b2f9d4c4dd` |
| 4 | 131 835 | `35f4f974f4b2bcd44da73963347f8952e341f83909e4498227d4e26b98f66f0d` |
| **5** | **1 111 572** | `eb79498a11d3c700bca3e23a24b4e5312302e19340c2e9cf32ef0e94a70624e2` |
| 6 | 239 836 | `f6cdea55af87528fdb0faa6d73d1cf2367219509802364d6d0226252832c73d9` |
| **7** | **1 669 332** | `f3bb601095ed3345f5af73eb0e92a50df7bb9bfc837efe65df273402bd69ef8a` |

**Blocs 1 à 6 byte-identiques au brief, bloc 5 compris** — les assets sont bien retirés et rien
d'autre n'a bougé dans ce bloc. Seul le bloc 7 diffère (bump + commentaire + `GAME_NOTES`).
`node --check` : **7 blocs, 7 OK**. Empreintes prises **après** la dernière retouche du HTML.

## Vérifications de code faites avant les tests

Le brief désigne deux points comme critiques ; les deux sont **relus au fichier**, pas supposés.

1. **La ligne qui évite de perdre des bâtiments.** `migratePlacement` est appelée l. **25206**, et le
   garde `if (!BUILDINGS[p.b] || …) continue` est l. **25207** — juste après. Le renommage
   `geothermie_v2 → geothermie` est posé l. **10290**, donc **avant** le `if ((fromV||0) >= 16) return`
   de la l. 10293 : il s'applique bien à **toutes les versions**. Il est écrit juste sous celui du
   Broyeur Uranium, dont le commentaire porte le même avertissement — les deux se lisent ensemble.
2. **Les deux mécanismes d'île ne sont pas gardés au même endroit**, et le brief a raison :
   - `forbiddenIslands` → lu par **`canPlace`** (l. 26534) **et** `tryPlace` (l. 26661) ;
   - `exclusiveIsland` → lu par **`selectTool`** (l. 27669), `switchIsland` (l. 27761) et la voie
     Copier de `handleTap` (l. 30284) — **jamais** par `canPlace` ni `tryPlace`.

   Un test qui n'interroge que `canPlace` conclurait donc à tort. **La suite passe par `selectTool`.**

**Références résiduelles à `geothermie_v2`** : 6 occurrences, dont **une seule ligne de CODE** — la
migration. Les cinq autres sont des commentaires (le journal du build 382, que le brief demande de
ne pas réécrire, et le nouveau commentaire 16.0). Aucune définition, aucun asset, aucune entrée de
`TOOLBAR_GROUPS`, aucun `unlocks`.

## Montage effectif

⚠ **Je n'ai pas la sauvegarde de fin de partie d'Ethan.** La partie de référence est donc
**reconstruite puis SAUVEGARDÉE PAR LA BASE 392** (où `geothermie_v2` existe encore), avant d'être
rechargée en 393 par le vrai chemin de `loadSave`. C'est le seul montage qui teste réellement la
migration — une forge in vivo en 393 ne le pourrait pas, le bâtiment n'y existant plus.

Contenu forgé : **23 géothermies au souterrain (22 V1 + 1 V2 au Nv.13)** et **11 séparateurs
cryogéniques**, nœuds 38 à 43 confirmés, `SAVE_VERSION 31`, `geothermie_v2` bien présent **1 fois**
dans le JSON sérialisé.

⚠ **L'île 6 souterrain n'ouvre que 12 tuiles de tunnel au départ** — impossible d'y poser 23
centrales sans forer. Le montage ouvre donc les tuiles de roche **hors socle** (`isBedrock`, lot L2) :
**360 tuiles**, ce qui recoupe exactement les « 361 tuiles jouables (cercles 0-9) » du mémo, l'élévateur
en moins.

Banc : Chromium 1194 headless, serveur HTTP depuis la racine du dépôt, copies `BANC_392.html` /
`BANC_393.html` exposant `switchIsland`, `canPlace`, `tryPlace`, `selectTool` et `flushSave` par un
**exposeur paresseux** `window.__H = () => ({…})`. **Les bancs ne partent pas dans la PR**, et leur
absence du livrable est asservie par un test. La save est réinjectée dans un `addInitScript` (elle
survit ainsi au flush `pagehide`, piège 14.59).

## Validation — 9 assertions, 9 PASS

| # | test | résultat | valeurs relevées |
|---|---|---|---|
| V1 | Boot | **PASS** | build **393** / Alpha 16.0 / SAVE **31**, canvas **100 %**, splash retiré, **0 `pageerror`**, 0 `tickError` |
| V2 | Suppression | **PASS** | `BUILDINGS.geothermie_v2` **undefined** · `BUILDINGS.geothermie` présent · `TIER_NEXT.geothermie` **undefined** · `TIER_STEP.geothermie_v2` **undefined** · `COST_SOFTCAP_X2.geothermie` **vrai** |
| V3 | Références résiduelles | **PASS** | absent de `TOOLBAR_GROUPS` et des 9 `unlocks` du nœud 43 ; `geothermie` **toujours** dans la barre |
| V4 | **MIGRATION** | **PASS** | **23 géothermies · 0 en V2 · niveau max 13 → 4 194 304 kW · 11 séparateurs · 0 bâtiment d'un autre id, 0 perdu** |
| V5 | Soft cap | **PASS** | Nv.13 : **4,150e8** contre **4,053e5** au barème normal = **×1 024** · Nv.9 : **7 625,6** des deux côtés · progression mesurée **×2 / ×8 / ×64 / ×1 024** aux Nv. 10/11/12/13 |
| V6 | Séparateur **avant** le nœud 43 | **PASS** | `exclusiveIslandFor` = **7** · outil armé au souterrain, **refusé sur l'île 6** · nœud restauré à `confirmed` |
| V7 | Séparateur **après** le nœud 43 | **PASS** | `exclusiveIslandFor` = **null** · `canPlace` **false sur les îles 1 à 5**, **true sur 6 et 7** · bascule d'île vérifiée sur les 7 |
| V8 | Centrale à gaz | **PASS** | posable sur **les 7 îles** avec le nœud 37 **non confirmé** · `exclusiveIslandFor` = **null** |
| V9 | Assets | **PASS** | `bat_geothermie_v2` **undefined** dans `SPRITE_DATA`, `ANIM_DATA` et `ANIM_META` ; la V1 est intacte |

**V4 est bien la preuve critique** : la V2 Nv.13 devient une V1 Nv.13 qui produit **rigoureusement
autant** (4 194 304 kW = 512 × 2¹³), et le compte de bâtiments est **strictement conservé** — 23
géothermies plus 11 séparateurs, aucun autre id, aucune tuile sautée.

### Contre-épreuve — la MÊME save, les mêmes gestes, sur la BASE 392 : 6/6

| | base 392 | build 393 |
|---|---|---|
| `geothermie_v2` (def, `TIER_NEXT`, `TIER_STEP`, toolbar, nœud 43) | **présent partout** | absent partout |
| la save rechargée | **22 géothermies + 1 V2 Nv.13** | **23 géothermies, 0 V2** |
| `upgradeCostFactor('geothermie', 13)` | **4,053e5** = barème normal (×1) | **4,150e8** (×1 024) |
| séparateur, nœud 43 confirmé | `exclusiveIslandFor` = **7**, non armable sur l'île 6 | **null**, armable sur 6 et 7 |
| centrale à gaz sur l'île 1 | `exclusiveIslandFor` = **6**, **non armable** | **null**, armable |
| assets `bat_geothermie_v2` | **présents** | absents |

Six verdicts opposés sur la **même sauvegarde**. Le lot est falsifiable de bout en bout, et la
migration est prouvée par différence et non par affirmation.

### Contrôles finaux — 5/5

| test | résultat | mesure |
|---|---|---|
| Boot du **fichier LIVRÉ** (pas le banc) avec la save réelle | **PASS** | build 393, canvas 100 %, 0 `pageerror`, 0 `tickError` |
| Migration effective dans le fichier livré | **PASS** | 23 géothermies, 0 V2 |
| Poignée de banc absente du livrable | **PASS** | `typeof window.__H` = `undefined` |
| Ligne « Exclusif » de la fiche détaillée | **PASS** | avant : **« Île 6 S — libéré par « Collisionneur P3 » »** · après : **disparaît** |
| Constat du brief sur les îles 1-5 | **PASS** | interdit (`forbiddenIslands`) mais `exclusiveIslandFor` = null → **aucun motif affiché** |

Le seul bruit console est le **404 préexistant** du serveur de test (ressource PWA absente).

## Écarts par rapport au brief, et leurs raisons

1. **Sauvegarde reconstruite au lieu de celle d'Ethan** (cf. Montage). Elle porte les mêmes
   grandeurs — 23 géothermies dont une V2 Nv.13, 11 séparateurs, nœud 43 confirmé — et elle est
   **écrite par la base 392**, ce qui rend le test de migration réel. Mais je ne peux pas affirmer
   avoir rejoué **sa** partie.
2. **Aucun autre écart.** Les 13 ancres sont appliquées verbatim.

## Points signalés, NON corrigés (conformément au brief)

- **Après le nœud 43, sur les îles 1 à 5, le séparateur est grisé SANS motif affiché** — vérifié :
  `forbiddenIslands` le refuse, mais le tooltip « Se construit sur X » est gardé par
  `exclusiveIsland != null`, qui vaut désormais `null`. C'est le comportement déjà en place pour
  onze bâtiments du souterrain, et le commentaire du code dit que c'est voulu. Constat pour un
  éventuel futur lot d'interface.
- **La ligne « Exclusif » se comporte toute seule**, comme annoncé : vérifiée, rien ajouté.
- **Le commentaire du build 382 mentionne encore `geothermie_v2` dans `TOOLBAR_GROUPS`** :
  **non réécrit**, c'est un journal exact pour son build. Seule la nouvelle ligne 16.0 dit que le
  palier est retiré.
- **Logistique de la Centrale à Gaz** : son oxygène est porté par le tuyau et absent de
  `TRADE_LIQUIDS` → **non expédiable par bateau**. Une île 1-5 d'accueil devra produire le sien sur
  place. L'élévateur n'étant pas un bateau, le souterrain continue de puiser dans le port de l'île 6.
  À dire au joueur le moment venu ; rien à corriger ici.
- **Équilibrage du soft cap non jugé** : la partie de référence conserve ses 23 géothermies et la
  Nv.13 garde ses 4,19 GW ; c'est le **cran suivant** qui devient prohibitif. Chiffres rapportés,
  constantes non ajustées.

## Pièges de banc payés en séance

- **`switchIsland` REFUSE une île verrouillée.** Sans débloquer les îles, la bascule n'a pas lieu,
  `curTiles()` reste sur la grille précédente, et `canPlace(r, c)` teste des coordonnées trouvées
  sur une **autre** île. J'ai eu un V8 rouge sur les seules îles 3 et 5 — un faux KO purement dû au
  hasard des coordonnées. **Remède durable : asserter que `g.currentIsland === isl` après chaque
  bascule**, ce que la suite fait désormais pour les 7 îles.
- **`isBedrock(game, r, c)` prend 3 arguments**, pas 4, et **n'est pas exposé par `__heat`** — il se
  référence par son nom nu (déclaration de module).
- **L'île 6 souterrain n'a que 12 tuiles ouvertes** au départ : tout montage qui y pose plus d'une
  dizaine de bâtiments doit d'abord ouvrir la roche hors socle.
- Reconfirmé : la save doit être réinjectée dans un `addInitScript` pour survivre au flush
  `pagehide` ; `flushSave` n'écrit que si un enregistrement est en attente (`g.saveTimer`).
- **Méthode de commit** : les fichiers d'extraction sont désormais écrits dans `/tmp`, jamais dans le
  dépôt, et le staging se fait par **chemins explicites** — la faute du lot L6b (3,1 Mo de `blk*.js`
  embarqués par un `git add -A`) ne peut plus se reproduire.
