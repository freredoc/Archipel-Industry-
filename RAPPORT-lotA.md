# RAPPORT — Lot A : retrait du halo permanent sur l'onglet « Améliorer »

Brief : `BRIEFlotAhalosature1.md` · ancre : `ancrelotA1.txt` · site unique, bloc `<script>` n° 7.

## 1. Versions

| | avant | après |
|---|---|---|
| `GAME_BUILD` | 378 | **379** |
| `GAME_VERSION` | Alpha 14.95 | **Alpha 14.96** |
| `SAVE_VERSION` | 31 | **31** (inchangé — aucun champ de sauvegarde touché) |

Le brief ne proposait aucun numéro (§ Versionnage) : 379 / Alpha 14.96 étaient les suivants
disponibles. `GAME_NOTES` réécrit pour ce lot.

## 2. Base d'exécution — conforme au brief

- SHA-256 du fichier de base : `975836e778af7c6ee275b324d4b9a90c3e157fdf5c56c6ee8b375d6c8ecc308e`
  → **identique** à celui annoncé au §« Base d'exécution ».
- `GAME_BUILD` relevé à l'exécution : **378** → pas d'écart, application directe autorisée.
- Blocs `<script>` : **7** (extraction séquentielle sur les balises dont le préfixe de ligne est
  vide). Le chiffre de 11 occurrences textuelles brutes est bien un artefact — non « corrigé ».

## 3. Comptage de l'ancre AVANT application

```
count = 1
```

(`len(ancre) = 190` caractères ; comptage identique avec ou sans le saut de ligne final.)

## 4. `node --check` des 7 blocs

**Édition PUBLIQUE — 7/7 OK :**

```
bloc 1 : OK   bloc 2 : OK   bloc 3 : OK   bloc 4 : OK
bloc 5 : OK   bloc 6 : OK   bloc 7 : OK
=== 7/7 OK
```

**Édition DEV** (`sed 's/^const DEV_BUILD = false;$/const DEV_BUILD = true;/'`) — **7/7 OK**.

## 5. SHA-256 du bloc 7, ré-extrait du fichier

⚠ **Le bloc 7 porte À LA FOIS le patch et le bump de version** (`GAME_BUILD` / `GAME_VERSION` /
`GAME_NOTES` y sont). Le hash attendu par le brief a été pré-compilé **avant** que le numéro de
version soit connu — il ne peut donc pas décrire le fichier livré. Les deux mesures sont données :

**(a) Patch SEUL, appliqué sur la base 378 non bumpée** — c'est ce qui prouve que mon remplacement
est byte-identique à celui du brief :

```
bloc 7 BASE   : 1 568 864 car., sha 938943be06fd3d6f9e48d1aa83bd9b4557a5d42599604bf835ffd9da113a8c16
bloc 7 PATCH  : 1 569 804 car., sha c5f8121ab5ca150b8aae798b196f6732577d2e0b76495067d88ca65de074294d
attendu brief : 1 569 804 car., sha c5f8121ab5ca150b8aae798b196f6732577d2e0b76495067d88ca65de074294d
```

→ **CONFORME au caractère et à l'octet près** (longueurs de base et patchée toutes deux exactes).

**(b) Fichier RÉELLEMENT livré** (patch + bump + `GAME_NOTES` + commentaire de version) :

```
bloc 7 LIVRÉ  : 1 569 796 car., sha 62ac660e3a6944a9883733e16e587a60f98fd0ecb12177ad3b63e620efda9242
```

L'écart de **−8 caractères** vs (a) s'explique entièrement : le nouveau `GAME_NOTES` est plus court
que celui du lot 14.95, ce qui compense les 4 lignes de commentaire ajoutées au-dessus de
`GAME_BUILD`. Delta du fichier entier : **+1 000 octets** (3 333 643 → 3 334 643).
SHA-256 du fichier livré : `db94def726272bc45d83a7e52850df51259149ad09481b6c449fcf7786975770`.

## 6. Contrôle `grep` du §4 de la vérification

```
grep -c "sel: '.tab-upg'"  →  2
  ligne 14310 -> TUTORIAL_STEPS   (étape index 4 = « Tuto 5/10 »)
  ligne 14329 -> TUTORIAL_STEPS   (étape index 5 = « Tuto 6/10 »)
tab-upg dans GUIDE_OBJECTIVES    = 0
@saturated dans GUIDE_OBJECTIVES = 1  (cible conservée, cf. §9)
```

Zones délimitées par **comptage de crochets** conscient des chaînes ET des commentaires (piège
14.91 : les apostrophes françaises des commentaires trompent un scanner naïf). Étapes 4 et 5 :
**exactement** celles annoncées par le brief.

## 7. Boot navigateur — confirmé

Chromium 1194, viewport 420 px / DPR 3, les deux éditions :

```
PUBLIQUE 379 : canvas 100% · ticks 0->6 · tickErrors {} · erreurs 0
DEV      379 : canvas 100% · ticks 0->6 · tickErrors {} · erreurs 0
```

Carte affichée, horloge qui avance, aucune zone morte temporelle, console propre.

## 8. Test falsifiable et contre-test

**Montage réel** (île 1, tutoriel passé, nappe de 110 routes depuis le port) : **5 mines de fer
Nv.5 + 4 carrières Nv.5** (`upgrade: 4` → `upgradeMult = 2^4 = 16`), toutes reliées au **même
réseau de route laissé au Nv.1**.

**Demande réellement relevée : `144,00 u/s`** — exactement la valeur du brief (5×16 + 4×16).
Cap route Nv.1 = 128 u/s → `netFactor = 0,889` (= 128/144, sous le seuil 0,999) → `netSaturated[1]`
non vide → `fix_sature` armé.

| Assertion | Patché 379 | Base 378 |
|---|---|---|
| demande > 128 u/s | OK 144,00 | OK 144,00 |
| `netSaturated` non vide | OK 1 entrée | OK 1 entrée |
| bannière « Un réseau est saturé : il ne transporte plus tout. Améliore son niveau. » | OK | OK |
| **aucun halo DOM à l'écran** | **OK 0** | **KO 1** |
| **aucun halo sur l'onglet Améliorer** | **OK** | **KO** |
| **aucun halo après ouverture d'un panneau** | **OK 0** | **KO 1** |
| Nv.2 (cap 1024) → `netSaturated` vide | OK | OK |
| Nv.2 → bannière disparue | OK | OK |
| contre-test Nv.1 → saturation revenue | OK 1 entrée | OK 1 entrée |
| contre-test → bannière revenue | OK | OK |
| **contre-test → toujours aucun halo** | **OK 0** | **KO 1** |
| canvas peint / console propre | OK 100 % / 0 | OK 100 % / 0 |
| | **15 OK, 0 KO** | **11 OK, 4 KO** |

**Le test est falsifiable** : les 4 assertions de halo ÉCHOUENT sur la base 378 non patchée (le
halo y est bien présent ET positionné sur `.tab-upg`, écart < 30 px) et passent après patch.
Les deux suites ont été **rejouées 2 fois, résultats identiques, aucun flottement**.

⚠ **Piège de harnais coûteux, à ne pas redécouvrir.** Le guide OUVRE lui-même le tip `reseau_sature`
(champ `why`) au moment où `fix_sature` s'arme, et le halo DOM est masqué tant qu'un popup est
ouvert (`tutorialStep < 0 && guideId && !activeTip`). Sans purge des astuces **APRÈS la forge**,
l'assertion « aucun halo » passe AUSSI sur la base non patchée → **test creux**. C'est arrivé à la
1ʳᵉ passe : 15 OK des deux côtés. Purger avant la forge ne suffit pas.

⚠ **Second piège** : le montage initial remplaçait des tuiles de route **adjacentes au port** par
des bâtiments → la nappe perdait son adjacence au port (`connected: false`), les 9 bâtiments
sortaient `disc: true / discReason: 'road'`, `netDemand` restait vide et c'est `fix_deconnecte` qui
s'armait. La pose se fait désormais en **vérifiant-et-annulant** (après chaque pose, tous les
bâtiments déjà posés doivent rester servis par un réseau `connected`, sinon la route est remise).

## 9. Non-régression — le halo TUTORIEL sur `.tab-upg`

Le lot ne retire la cible que du **guide** ; les deux occurrences de `TUTORIAL_STEPS` doivent
continuer de désigner l'onglet Améliorer. Suite dédiée (état forgé : 5 mines + 5 carrières reliées
au Nv.1, non améliorées) :

```
OK  forge : 10 producteurs Nv.1 reliés
OK  Tuto 5/10 : bannière du tutoriel · halo DOM présent · halo bien SUR l'onglet Améliorer
OK  Tuto 6/10 : bannière du tutoriel · halo DOM présent · halo bien SUR l'onglet Améliorer
OK  console propre
-> 8 OK, 0 KO   (rejouée 2 fois, identique)
```

⚠ Sans la forge, l'étape 6 s'arrête légitimement sur sa **première** cible vraie (`.tab-build`,
tant que `tutCount < 5`) : y attendre le halo sur Améliorer est un **faux KO** (constaté).
⚠ Forcer `game.tutorial.step` ne suffit pas à changer l'étape vue par l'UI (la `Toolbar` lit le
state React `tutorialStep`) → `targetIdx` est désaligné pour forcer la resynchro (recette 13.83).

## 10. Écarts constatés entre le brief et le fichier réel

1. **Aucun écart sur l'ancre, le remplacement ni le comportement.** Ancre à `count == 1`, hash du
   bloc 7 patché conforme au caractère près, `grep` du §4 conforme (2 occurrences, étapes 4 et 5),
   demande mesurée = 144,00 u/s comme annoncé.

2. **Le hash du §2 de « Vérification après application » ne peut pas décrire le fichier livré** —
   le bloc 7 contient aussi `GAME_BUILD`/`GAME_VERSION`/`GAME_NOTES`, or le brief demande de
   bumper les deux « en choisissant le numéro disponible au moment de l'exécution », numéro
   inconnu à la pré-compilation. Le hash est donc vérifié sur la variante **patch seul** (§5a),
   qui est le seul contrôle qui ait un sens ici. À l'avenir : soit pré-compiler le hash sur un
   bloc qui ne porte pas la version, soit fixer le numéro dans le brief.

3. **Le bug préexistant du §« Note » est CONFIRMÉ, mesuré sur le fichier.** Les deux sites de
   `push` (lignes 13068 et 13885) empilent bien des objets `{ type, level }` dans
   `game.netSaturated[isl]`, alors que `drawTutorialHalo` (ligne ~27921) fait
   `new Set(sat).has(tiles[r][c].networkId)`. La cible `@saturated` n'a donc **jamais** rien
   marqué. Elle est **conservée telle quelle**, comme le demande le brief, pour que sa réparation
   reste un choix explicite. Conséquence assumée et vérifiée à l'écran : `fix_sature` ne produit
   désormais **plus aucun halo**, seulement sa bannière — c'est le comportement demandé.

4. **Hors périmètre, signalé** : la bannière du guide reste affichée en permanence tant que la
   saturation dure (elle porte l'information, c'est l'intention du lot). Seul le halo disparaît.

5. **EFFET DE BORD FAVORABLE, non demandé mais assumé — l'anomalie CI 14.76 se referme.**
   `GAME_NOTES` devait être réécrit pour ce lot ; il l'a été en **UTF-8 littéral** (comme les
   régions de code récentes) au lieu des séquences `\uXXXX` héritées de Babel. Or l'étape CI
   « Sync version.json » fait `grep -oP "const GAME_NOTES = \"\K[^\"]*"` puis `jq --arg notes` :
   avec l'ancien style, la variable shell contenait les caractères `\`,`u`,`0`,`0`,`e`,`9` et `jq`
   échappait le backslash → `\\u00e9` dans `version.json` → le joueur lisait `é` / `«` sous
   « Mise à jour disponible » (anomalie documentée au mémo 14.76, présente depuis plusieurs
   builds). Extraction simulée sur le fichier livré : la chaîne sort telle quelle et `jq` la rend
   correctement accentuée. La chaîne ne contient **aucun `"`** (guillemets français `«` `»`), donc
   le `[^\"]*` du grep ne la tronque pas. `GAME_NOTES` n'est lu par **aucun code du jeu** (le
   panneau Options affiche `version.json.notes`, récupéré par le réseau) : le seul consommateur
   est la CI.

## 11. Périmètre non touché

`tipAnyNetworkSaturated`, `drawTutorialHalo`, les deux sites de `push` de `netSaturated`, les
`TUTORIAL_STEPS`, les autres objectifs de `GUIDE_OBJECTIVES`, le tip `reseau_sature`,
`NETWORK_THROUGHPUT`, `SAVE_VERSION`.
