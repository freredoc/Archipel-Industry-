# RAPPORT — Correctif L6b (miroir d'état du calque logique)

Patcheur `patch_L6b.py` (pré-compilé, fourni) · **correctif d'un défaut que MON lot L6 a introduit**.
Branche : `claude/temps-souterrain-display-uoonrz`, **repartie de `main`** (la PR #373 a été mergée).

## Version produite

| | |
|---|---|
| `GAME_BUILD` | 390 → **391** |
| `GAME_VERSION` | Alpha 15.7 → **Alpha 15.8** |
| `SAVE_VERSION` | **31, INCHANGÉ** — aucun champ persisté touché |
| Taille | 3 399 561 → **3 401 709** o (**+2 148**) |
| SHA-256 livré | `60420f44eaf74b751b4f69f3f57fedd7ae52a644b7b2802e9052c6c14f414c01` |

L'ancre pèse une ligne ; le delta est presque entièrement le commentaire cumulatif (dont la leçon de
méthode) et `GAME_NOTES`.

## Sortie du patcheur

Base vérifiée `09437fa5…` = build 390, **aucun avertissement**.

```
OK - 1 ancre appliquee
SHA-256 fichier patche : 6d1c6d1719d1ae09a0d1c1e1e04ea9f04d10cc19cb4e5ab2eb2a4c02e2a80b04
```

**Conforme au caractère près** à l'attendu du brief (`6d1c6d17…`). Contrôle intermédiaire : **avant le
bump, le bloc 7 valait `78637890…`**, exactement l'empreinte annoncée — patch appliqué à l'identique.

## SHA-256 des 7 blocs, ré-extraits APRÈS la toute dernière modification du HTML

| bloc | octets | sha256 |
|---|---|---|
| 1 | 418 | `a50c1c4e7f4a304c650c0cfa7e06c4ffdfb0d87510d83738114d4dd5a1641628` |
| 2 | 4 397 | `8fbb22187703339c146b2f82badd8701d199128cd3d9dd8673d213c8616ca541` |
| 3 | 10 751 | `d949f1c3687aedadcedac85261865f29b17cd273997e7f6b2bfc53b2f9d4c4dd` |
| 4 | 131 835 | `35f4f974f4b2bcd44da73963347f8952e341f83909e4498227d4e26b98f66f0d` |
| 5 | 1 112 066 | `6066e8c1aeb44929ec5e92d946a936e70451d0c76d7f1375fae084200c308720` |
| 6 | 239 836 | `f6cdea55af87528fdb0faa6d73d1cf2367219509802364d6d0226252832c73d9` |
| 7 | 1 649 256 | `b1b03e91face50b0cf53fd5b02b7858b27753c5a48468a666adc5e6b6a316651` |

**Blocs 1 à 6 byte-identiques au brief** ; l'écart du bloc 7 porte le bump, le commentaire cumulatif
et `GAME_NOTES`. `node --check` : **7 blocs, 7 OK**. Empreintes prises **après** la dernière retouche
du HTML, fichiers de banc déjà supprimés.

## Le défaut, relu dans le code — l'analyse du brief est exacte

Le calque logique a **deux représentations** :

| | écrit par | lu par |
|---|---|---|
| `g.ui.logicLayer` (champ de partie) | `toggleLogicLayer`, sérialisation | **`tryDemolish`** (l. 26627, `if (gL.ui.logicLayer)`) |
| `logicLayer` (état React, `useState(false)` l. 24225) | `setLogicLayer` | **`Toolbar`** (prop l. 31016) |

`toggleLogicLayer` (l. 28139) tient **les deux**, plus `deselectAll()`, la fermeture des deux menus,
`markDirty()` et `scheduleSave()`. Mon handler `onGoTile` ne posait que le champ.

**La conséquence n'est donc pas cosmétique.** Après « Y aller » : la barre affiche le mode bâtiment
(menus normaux, onglet Améliorer actif) pendant que la démolition, elle, cible **exclusivement** la
surcouche `t.logic`. Le joueur clique une usine pour la raser et lit « Rien à démolir dans la couche
logique » — ou efface un élément logique en croyant toucher autre chose.

⚠ **L'appel reste sous condition** (`if (!gameRef.current.ui.logicLayer)`) : `toggleLogicLayer` est
une **bascule**, pas un setter. L'appeler inconditionnellement **éteindrait** le calque quand il est
déjà allumé — soit exactement le cas d'un joueur qui clique « Y aller » en étant déjà en mode logique.

## Validation — le test asserte sur le RENDU, jamais sur le champ écrit

Banc : Chromium 1194 headless, serveur depuis la racine du dépôt, viewport 420×900. Deux copies de
banc (`BANC_391.html`, `BANC_390.html`, plus `BASE390.html`) — **supprimées avant le commit**, et leur
absence du livrable est asservie par un test.

Scénario exact demandé : **île 7, calque éteint → ouvrir la liste d'alertes → cliquer « Y aller » →
vérifier la présence de `.tab-logic`.**

| | champ `g.ui.logicLayer` (lu par `tryDemolish`) | barre d'outils (`.tab-logic`) | accord |
|---|---|---|---|
| base 390 | `true` | **absent** | **non** |
| patch 391 | `true` | **présent** | **oui** |

Onglets relevés après le clic :

- base 390 : `tab-build · tab-net · tab-copy · tab-dem · tab-upg`
- patch 391 : `tab-build tab-logic · tab-net tab-logic · tab-copy · tab-dem · tab-upg tab-locked`

C'est **le seul point qui distingue les deux versions**, comme annoncé : le champ vaut `true` des deux
côtés. Un test qui l'aurait asserté aurait passé sur les deux fichiers.

| # | test | résultat | mesure |
|---|---|---|---|
| V1 | Boot du fichier livré | **PASS** | build **391** / Alpha 15.8 / SAVE 31, canvas **100 %**, 0 `pageerror` |
| V2 | Île 7, calque éteint avant le clic | **PASS** | `logicLayer` `false`, `.tab-logic` absent |
| V3 | Liste d'alertes ouverte, ligne logique présente | **PASS** | bouton « Aller à cette porte » |
| V4 | **`.tab-logic` présent après « Y aller »** | **PASS** | onglets Bloc logique + Câble logique rendus |
| V5 | Champ et écran d'accord | **PASS** | champ `true` **et** barre en mode logique |
| V6 | Bascule non inversée si déjà allumé | **PASS** | calque déjà ON → clic → **reste** ON, `.tab-logic` toujours présent |
| V7 | Améliorer verrouillé en mode logique | **PASS** | `tab-upg` porte `tab-locked` |
| V8 | Scénario destructeur | **PASS** | voir ci-dessous |
| V9 | Poignée de banc absente du livrable | **PASS** | `window.__H` **undefined** |

**9/9 PASS sur le patch 391. 7/9 sur la base 390** — V4 et V5 y échouent, et elles seules.

### V8 — le scénario destructeur, mesuré sur les deux versions

Un élément logique est posé, puis démoli après un « Y aller » :

| | ce que la barre annonce | ce que la démolition fait |
|---|---|---|
| base 390 | **`batiment`** | **efface l'élément logique** |
| patch 391 | **`logique`** | efface l'élément logique |

Le comportement de la démolition est **identique** des deux côtés — c'est bien l'**accord** qui est
restauré, pas la mécanique. La démonstration du danger décrit par Ethan est directe : sur la base, le
joueur détruit un élément logique pendant que l'écran lui annonce le mode bâtiment.

## Écarts par rapport au brief

**Aucun.** L'unique ancre est appliquée verbatim.

## Ma responsabilité, et la règle que j'en tire

Mon test du lot L6 assertait `g.ui.logicLayer === true` — **le champ que le patch venait d'écrire**.
Il ne pouvait donc que passer, sans rien prouver de ce que voit le joueur. Le piège est déjà au mémo
depuis le lot 14.83 (« ⚠ `E1e-tutStep` lit le STATE React `tutorialStep`, pas la ref — les onglets se
rendent depuis le state ») : je ne l'ai pas transposé.

**Règle ajoutée à la méthode :** quand un patch écrit un champ de `g.ui`, chercher systématiquement
s'il existe un `useState` qui le double, et **asserter sur le rendu** (ici la présence de
`.tab-logic`), jamais sur le champ écrit. Corollaire : faire **rendre à l'écran** tout composant
qu'un patch touche, au lieu de se contenter du boot.

## Points signalés, NON corrigés

- **Le champ et le miroir restent deux vérités.** `g.ui.logicLayer` et l'état React peuvent diverger
  partout ailleurs si un futur site écrit l'un sans l'autre. `toggleLogicLayer` est aujourd'hui le
  **seul** site qui tient les deux — vérifié par grep : les autres occurrences de `ui.logicLayer` sont
  des **lectures** (`tryDemolish`, `tryPlace`, sérialisation, boot). Unifier (rendre le champ dérivé
  du state, ou l'inverse) est un lot de refonte, pas un correctif d'une ligne.
- Les libellés du lot L6 restent non traduits (repli français) → lot i18n de l'audit 381.
- `activeLogicAlerts` balaie toujours toutes les îles à chaque calcul du HUD ; toujours aucune
  mémoïsation, toujours aucun profilage qui la justifie.
