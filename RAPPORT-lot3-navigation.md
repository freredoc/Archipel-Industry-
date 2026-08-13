# RAPPORT — LOT 3 : navigation depuis la carte

**Livré : `GAME_BUILD = 397`, `GAME_VERSION = 'Alpha 16.4'`, `SAVE_VERSION = 31` (INCHANGÉ, vérifié).**

Base d'exécution : build **396 / Alpha 16.3**, SHA-256
`227ccc5652555d56e96592c8e93812024927075d679742ba12cd64e2102b9c2f`, 3 441 933 o — la branche a été
repartie de `main` après le merge du lot 2 (PR #379).

⚠ **AUCUN BRIEF N'ACCOMPAGNAIT CE LOT**, contrairement aux lots 1 et 2. Pas de valeurs attendues, pas
d'empreinte du patcher, pas de liste de tests, pas de consigne de versionnage. **La suite de
validation ci-dessous a donc été conçue à partir du patch lui-même**, et le versionnage suit la
convention établie par les deux lots précédents. Les valeurs de référence ne sont comparées à rien :
elles sont mesurées.

Patcher reçu : `patch_lot3.py`, 4 928 o,
SHA-256 `b47fc5b609863db4d74d176bea46420d605409bf6c6dc8ad93cdd9410b6a2533` (aucune empreinte de
référence à confronter).

---

## 1. Vérifications faites AVANT d'appliquer

Sans brief pour les garantir, les trois hypothèses du patch ont été vérifiées à la source :

| hypothèse du patch | vérification |
|---|---|
| `off` (île verrouillée) est en portée dans la boucle des îles | **oui** — `const off = !ouverte(id);` juste au-dessus de l'ancre |
| `setMapOpen` est atteignable depuis `switchIsland` | **oui** — `const [mapOpen, setMapOpen]` est déclaré **avant** `function switchIsland` (offsets 3 061 699 / 3 218 796), et `switchIsland` est une **déclaration de fonction**, donc hissée : aucun risque de zone morte |
| `switchIsland` refuse déjà l'île courante et les îles verrouillées | **oui** — `if (id === g.currentIsland) return;` en première ligne, puis `if (!g.islandUnlocked[id] && !g.ui.dev)` → toast rouge + `SFX.invalid` |

La troisième est la plus utile : **le garde du lot est donc DOUBLE**, et c'est le bon sens — le rendu
évite de proposer un geste sans effet, le moteur reste la vérité.

---

## 2. Application

Sortie du patcher, **8 ancres sur 8** :

```
OK css  OK   OK ile   OK   OK mp_sig   OK   OK app     OK
OK sig  OK   OK sout  OK   OK mp_call  OK   OK switch  OK
delta octets : +1820
```

**Idempotence** : seconde passe → gardes déclenchées, **+0 octet**.
Taille finale après i18n et bump : **3 446 584 o** (+2 831 o).

---

## 3. Contrôles statiques

- **`node --check` : 7/7**, avant ET après le bump. Piège habituel reproduit : scanner naïf = **11**
  correspondances, scanner ancré `(?m)^<script` = **7** blocs réels.
- **Parenthèses** : `ArchipelMap` **67/67**, `MapPanel` **55/55** (inchangé depuis le lot 2, le patch
  n'y touche que la liste de props).
- **`SAVE_VERSION` toujours à 31** — aucun champ ajouté ni retiré.
- `GAME_NOTES` : **608 caractères** extraits par la regex de la CI, chaîne complète, accents
  littéraux, **0 séquence `\u`**, aucun guillemet droit.

### SHA-256 des 7 blocs, re-extraits APRÈS la toute dernière modification

Fichier entier : **`69b909a48c10b4aeb63bb348157fe6ab896f51ed058ddb76d7d5429260aaf025`** — 3 446 584 o.

| bloc | SHA-256 (16 car.) | taille |
|---|---|---|
| blk1 | `a50c1c4e7f4a304c` | 418 |
| blk2 | `8fbb22187703339c` | 4 397 |
| blk3 | `d949f1c3687aedad` | 10 751 |
| blk4 | `35f4f974f4b2bcd4` | 131 835 |
| blk5 | `1be53ce44e7be14f` | 1 113 969 |
| blk6 | `188ef1b636036af0` | 240 694 |
| blk7 | `b04117674bf6f2f6` | 1 687 897 |

---

## 4. Suite de validation exécutée

Chromium headless, page servie depuis la racine du dépôt, locale forcée. Astuces fermées **par clic
sur `.tip-ok`**, jamais `remove()`.

| test | verdict | montage et valeurs mesurées |
|---|---|---|
| **T1** qui est un bouton | **PASS** | 390×780, îles 2 et 3 déverrouillées. Île **courante** : `DIV`, `.cur`, pas de `.go`. Îles **ouvertes non courantes** : **2 boutons**, tous `BUTTON` avec un `title` « Aller à … ». Îles **verrouillées** : **2**, toutes `DIV` sans `.go` |
| **T2** naviguer | **PASS** | Îles 2-6 ouvertes, carte ouverte. **Clic souris réel** sur « Aller à Île 3 » → `currentIsland` **1 → 3**, panneau **fermé** et backdrop **retiré** |
| **T3** souterrain, aller-retour | **PASS** | `elevatorRepaired` + `islandUnlocked[7]`. Depuis l'île 1 : marqueur `BUTTON`, title **« Descendre à Île 6 S »** → clic réel → **île 7**, panneau fermé. Une fois au souterrain : marqueur devient `DIV` **`.cur`** sans `.go`, et **l'île 6 redevient un `BUTTON`** → clic « Aller à Île 6 » → **retour île 6** |
| **T4** île verrouillée | **PASS** | Partie neuve. Le nœud verrouillé est un `DIV`, sans `.go`, sans `onclick` ; un clic **ne change pas d'île** (1 → 1), **laisse le panneau ouvert** et **n'émet aucun toast** — il n'est même pas une cible, on ne passe donc jamais par le refus du moteur |
| **NR** non-régression du lot 2 | **PASS** | **5 liaisons**, toutes `DIV`, `pointerEvents` **`none`** — le lot 3 n'a pas ré-armé les traits. Révélation de l'île 6 : brume présente à la 1ʳᵉ ouverture avec `archiVu6` posé à `true`, **absente** à la 2ᵉ |

**0 `pageerror`** sur toute la suite. Seule erreur console : le **404 unique** du serveur de test,
bruit préexistant documenté depuis le build 14.47.

### ⚠ Piège de harnais, retombé dessus — et c'est un « KO » qui ne prouvait rien

T2 et T3 ont d'abord échoué : le bouton était bien un `BUTTON` avec le bon `title`, mais le clic ne
naviguait pas. **La cause est `useGhostGuard`**, que `MapPanel` installe sur son panneau
(`onClickCapture`) : il avale tout clic tant qu'aucun `pointerdown` **interne** n'a eu lieu depuis
l'ouverture. Un `el.click()` programmatique n'émet pas de `pointerdown` — le doigt du joueur, si.

Le défaut était donc **entièrement dans mon montage**. Corrigé en passant par un **vrai clic souris
Playwright** (`locator.click()`, qui envoie `pointerdown` + `pointerup` + `click`). Le piège est
documenté au mémo depuis le build 13.50 ; je le consigne à nouveau parce qu'il produit exactement le
faux verdict le plus coûteux : « la fonctionnalité ne marche pas » alors qu'elle marche.

---

## 5. i18n — tranché, avec un précédent direct

Les deux libellés du lot sont des `title` : `I18N.t("Aller à ") + islandLabel(id)` et
`I18N.t("Descendre à ") + islandLabel(7)`. Aucun des deux n'existait dans les tables (**0 entrée**).

**Décision : ajoutés aux 3 tables (en/es/de)** — l'inverse de ce que j'ai fait au lot 1 pour
« Faire défiler », et la raison n'est pas un changement d'avis mais un **précédent vérifié** :

| clé existante | entrées de table |
|---|---|
| « Aller à cette île » (panneau d'alertes, build 13.41) | **3** |
| « Y aller » (même panneau) | **3** |

C'est la **même affordance** — un tooltip qui emmène sur une île. Laisser celui de la carte en
français jurerait avec son propre jumeau à deux panneaux d'écart. « Faire défiler » n'a, lui, aucun
équivalent traduit dans le fichier.

Implémentation : IIFE d'augmentation, motif des blocs 14.32 / 14.54 / 16.3, garde de fusion
`if(!L.ui[k])` qui n'écrase jamais une entrée existante. Rendu vérifié **en jeu, 4 langues** :

```
fr | « Aller à Île 2 »      « Descendre à Île 6 S »
en | « Go to Island 2 »     « Go down to Island 6 U »
es | « Ir a Isla 2 »        « Bajar a Isla 6 S »
de | « Gehe zu Insel 2 »    « Hinunter zu Insel 6 U »
```

Les libellés d'île viennent d'`islandLabel`, donc l'id interne 7 s'affiche « Île 6 S » / « Island 6 U »
et **jamais « Île 7 »** — l'invariant du lot 1 tient dans les quatre langues.

---

## 6. Écarts et décisions

1. **Aucun brief** (cf. en-tête). Versionnage, plan de tests et rapport suivent la convention des
   lots 1-2. **Numéro choisi à l'exécution : 397 / Alpha 16.4**, le dépôt étant à 396 après le merge
   du lot 2.
2. **Branche `claude/carte-archipel-wmyxbs`** (consigne de session), repartie de `main`.
3. **Deux tests de ma suite ont d'abord donné un faux KO** (cf. §4) — harnais, pas produit.

---

## 7. Points signalés, non corrigés

- **`switchIsland` ferme désormais la carte pour TOUS ses appelants**, pas seulement le clic sur la
  carte : le sélecteur d'onglets du HUD, le bouton souterrain, les boutons « Y aller » des alertes,
  et les objectifs du guide referment eux aussi le panneau s'il est ouvert. **C'est voulu** — un
  changement d'île doit laisser le joueur devant l'île, quelle que soit l'origine du geste — mais
  c'est un effet qui dépasse le seul lot, et il mérite d'être su.
- **Le double garde rend le refus du moteur inatteignable depuis la carte** : une île verrouillée
  n'étant pas un bouton, le toast « Île verrouillée — débloquez-la via la Recherche » ne se déclenche
  jamais par ce chemin (mesuré en T4). Le joueur voit une case grisée sans explication. **Choix
  assumé** : la case est déjà visiblement grisée, et le toast reste accessible par les onglets du
  HUD. À rouvrir si la carte devient le chemin principal.
- **`I18N.t("Faire défiler")` du lot 1 reste non traduit** — décision inchangée et argumentée
  ci-dessus, mais c'est désormais le **seul** libellé français résiduel de la refonte Carte. Un mot à
  dire et je le rattrape.
- **Hors périmètre, non anticipé** : la **réparation unifiée (lot 4)**. Le bouton 🛠 et `RepairModal`
  sont **strictement intacts**.
- **Nom du rapport vérifié libre avant écriture** (leçon du build 15.1).
