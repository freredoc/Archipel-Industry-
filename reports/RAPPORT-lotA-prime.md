# RAPPORT — Lot A′ : resynchronisation de la table i18n du tutoriel

Brief : `BRIEFlotAprimei18ntutoriel.md` · paires : `pairslotAprime.json` (4 triplets).

> ## ⚠ EN UN PARAGRAPHE
> **La prémisse du brief est FAUSSE : le défaut décrit n'est pas atteignable en jeu.** Le littéral
> à 7 entrées est **MORT** — une IIFE d'augmentation (13.60), dans le **même bloc 6**, fait
> `L.tutorial = m.tutorial` (remplacement en bloc, inconditionnel) avec les **10 entrées
> correctes** dans les 4 langues. Mesuré en navigateur sur la base **NON patchée** : l'étape 2
> affiche déjà « Relie la mine au port avec une route. » et les étapes 8/9/10 sont déjà traduites
> en en/es/de. **Appliquer le texte du brief tel quel aurait créé une SECONDE VÉRITÉ divergente**
> en 4 points. Le lot est donc livré **autrement** : le littéral mort est **recopié depuis la
> source vivante**, à l'octet. C'est un **no-op prouvé** pour le joueur (40/40 bannières
> identiques) dont la valeur est la **suppression du piège** + la **défense en profondeur**.

## 1. Versions

| | avant | après |
|---|---|---|
| `GAME_BUILD` | 379 | **380** |
| `GAME_VERSION` | Alpha 14.96 | **Alpha 14.97** |
| `SAVE_VERSION` | 31 | **31** (inchangé) |

## 2. Les 4 `count` relevés AVANT application

```
fr  count = 1     en  count = 1     es  count = 1     de  count = 1
```

Aucun échappement `\uXXXX` / `\xNN` / apostrophe échappée dans les 4 zones (vérifié) — conforme
au §« Modifications » du brief.

## 3. Pourquoi le brief se trompe — trois preuves indépendantes

### (a) Qui fait foi au runtime ? (bloc 6 tronqué avant l'IIFE, vs bloc 6 complet)

Sur la **base 379, NON patchée** :

```
(a) LITTÉRAL SEUL  : fr 7 entrées · [1] = "Posez une carrière sur une tuile."      · [7] = undefined
(b) BLOC 6 COMPLET : fr 10 entrées · [1] = "Relie la mine au port avec une route." · [7] = "Pose un four à fer…"
(c) VERDICT : le littéral fait-il foi ?  NON — l'IIFE écrase le littéral
```

Le site responsable, déjà présent en base :

```js
for(var code in TUT){ var L=I18N.locales[code]; if(!L) continue; var m=TUT[code];
  L.tutorial=m.tutorial; // REMPLACEMENT en bloc (l'ordre des étapes a changé)
```

### (b) Rejeu du VRAI `_g` (module i18n réel exécuté, pas une réimplémentation) — sur la base 379

```
fr : 10 entrées pour 10 étapes      en : 10 entrées pour 10 étapes
es : 10 entrées pour 10 étapes      de : 10 entrées pour 10 étapes
OK  aucune étape ne retombe sur le français inline — 0 null sur 40
OK  fr affiché == goal inline sur les 10 étapes — étapes divergentes : []
-> 9 OK, 0 KO
```

Le test « falsifiable » du brief **passe déjà sur la base** : il n'est donc pas falsifiable.

### (c) Contrôle EN JEU sur la base 379 — celui que le brief propose comme contre-test

Le brief affirme : « Avant le patch, l'étape 1 affiche « Posez une carrière » alors qu'elle attend
une route ». **Faux.** Bannière réellement lue, partie neuve, base non patchée :

```
[fr] étape 2 : Relie la mine au port avec une route.       ← et non « Posez une carrière »
[de] étape 2 : Verbinde die Mine mit einer Straße zum Hafen.
```

Et les étapes 7–10, que le brief dit « rester en français quelle que soit la langue » :

```
[en] 7..10 : Place a coal mine… / Place an iron furnace… / Place a cement plant… / Produce 10 iron ingots…
[es] 7..10 : Coloca una mina de carbón… / …un horno de hierro… / …una cementera… / Produce 10 lingotes…
[de] 7..10 : Platziere eine Kohlemine… / …einen Eisenofen… / …ein Zementwerk… / Produziere 10 Eisenbarren…
```

**Déjà entièrement traduites.**

## 4. Pourquoi je n'ai PAS appliqué le texte du brief

Appliqué verbatim, il crée une **divergence** avec la source vivante (mesurée) :

| | littéral (brief) | augmentation (ce qui s'affiche) |
|---|---|---|
| `en[5]` | Two more mines and two more quarries… | **2** more mines and **2** more quarries… |
| `es[4]` | **Mejora** todos tus edificios al nivel 2. | **Sube** todos tus edificios al nivel 2. |
| `es[5]` | **Dos** minas y **dos** canteras más… | **2** minas y **2** canteras más… |
| `de[5]` | **Zwei** weitere Minen und **zwei** weitere Steinbrüche… | **2** weitere Minen und **2** weitere Steinbrüche… |

Invisible (l'augmentation gagne), mais le fichier porterait alors **deux copies crédibles qui se
contredisent** — un piège **pire** que celui qu'on retire, et exactement celui contre lequel le
§« Piège à ne pas reproduire » du brief met en garde.

**Livré à la place** : les 4 tables littérales sont **régénérées depuis les tables de
l'augmentation** (jamais retapées). Contrôle après application :

```
fr / en / es / de  littéral == augmentation ?  OUI  (10 entrées chacune)
VERDICT : UNE SEULE VÉRITÉ — aucune divergence possible
```

## 5. Ce que le lot apporte réellement

1. **Suppression du piège.** Le prochain lecteur de `LOCALES.fr.tutorial` ne lira plus l'inverse de
   la vérité — c'est précisément ce qui a induit l'auteur du brief en erreur.
2. **Défense en profondeur.** L'IIFE est **gardée** (`if(!window.I18N||!I18N.locales) return;`).
   Si un refactor cessait d'exposer `I18N.locales`, le jeu retomberait **en silence** sur le
   littéral. Effet mesuré de ce repli :

   | | base 379 | patché 380 |
   |---|---|---|
   | littéral seul, `fr[1]` | « Posez une carrière sur une tuile. » ✗ | « Relie la mine au port avec une route. » ✓ |
   | littéral seul, `fr[7]` | `undefined` ✗ | « Pose un four à fer et relie-le au port. » ✓ |

   Autrement dit : le bug du brief **deviendrait réel** sur 379 dans ce scénario, plus sur 380.

## 6. `node --check`

**PUBLIQUE 7/7 OK** · **DEV 7/7 OK** (`sed 's/^const DEV_BUILD = false;$/…true;/'`).

## 7. SHA-256 des blocs 6 et 7, ré-extraits du fichier

```
bloc 6 : 227 358 car. · bb270eedcfeba02408b8d266442bd533de93dcfd9a832f9993ae0bc3cd3fbdd2
bloc 7 : 1 571 233 car. · d17191849d55baf477ae6e2acb9ba1b42950d2d4c4c615ec2c02737c20950267
blocs 1–5 : INCHANGÉS (a50c1c4e… / 8fbb2218… / d949f1c3… / 35f4f974… / 6066e8c1…)
```

⚠ **Écart assumé avec les valeurs du brief**, deux causes :
- **bloc 6** : brief `0f7c59cf…` / 227 374 car. — j'obtiens 227 358 car. (**−16**), parce que le
  littéral reprend le texte de **l'augmentation** (« 2 more mines ») et non celui du brief
  (« Two more mines »). C'est la correction du §4, délibérée.
- **bloc 7** : le brief attend `c5f8121a…` / 1 569 804 car., soit le bloc **sans aucun bump de
  version**. Le bloc 7 porte `GAME_BUILD`/`GAME_VERSION`/`GAME_NOTES` + les commentaires de
  décision des lots A et A′ → il ne peut pas correspondre. (Le hash `c5f8121a…` a bien été
  vérifié conforme au caractère près lors du **lot A**, sur la variante patch-seul.)

## 8. Boot navigateur — confirmé

```
PUBLIQUE 380 : canvas 100% · ticks 0->6 · tickErrors {} · erreurs 0
DEV      380 : canvas 100% · ticks 0->6 · tickErrors {} · erreurs 0
```

## 9. Rejeu de `_g` (4 langues × 10 index) sur le fichier patché

```
fr/en/es/de : 10 entrées pour 10 étapes
OK  aucune étape ne retombe sur le français inline — 0 null sur 40
OK  fr affiché == goal inline sur les 10 étapes — étapes divergentes : []
OK  applyToData ne laisse aucun goal vide (4 langues)
OK  aucune étape restée en français (en/es/de)
-> 9 OK, 0 KO
```

## 10. Contrôle en jeu — étapes 1 et 7, et NO-OP prouvé

Bannière relevée **dans le navigateur**, 4 langues × 10 étapes, avant (379) et après (380) :

```
langues comparées : fr, en, es, de
étapes comparées  : 40
AUCUNE différence de bannière entre 379 et 380
→ le lot est un NO-OP pour le joueur (prouvé en jeu)
```

Étape 1 : « Pose une mine de fer sur un gisement de fer. » (identique avant/après).
Étape 7 : « Pose une mine de charbon et relie-la au port. » / « Place a coal mine… » /
« Coloca una mina de carbón… » / « Platziere eine Kohlemine… » (identique avant/après).

## 11. Non-régression du lot A (halo `fix_sature`)

Suite du lot A rejouée sur le build 380 : **15 OK, 0 KO** — demande 144,00 u/s, `netFactor` 0,889,
bannière de saturation présente, **0 halo** sur l'onglet Améliorer, contre-test Nv.2 → Nv.1 conforme.

## 12. Écarts constatés entre le brief et le fichier réel

1. **Le défaut décrit n'existe pas en jeu** (§3, trois preuves) — le littéral est mort depuis 13.60.
2. **Le test « falsifiable » du brief passe déjà sur la base** : il ne peut rien démontrer.
3. **Le contrôle en jeu et le contrôle multilingue proposés sont faux** : ce qu'ils annoncent comme
   « avant le patch » n'est pas ce que le jeu affiche.
4. **Le texte de remplacement diverge de la source vivante en 4 points** (§4) → non appliqué tel quel.
5. **Le hash de bloc 7 ne peut pas décrire un fichier bumpé** (même remarque qu'au lot A).
6. `GAME_NOTES` rédigé **sans surpromesse** : la note dit explicitement « sans effet visible en jeu ».

## 13. Périmètre non touché

L'IIFE d'augmentation (source vivante), `applyToData`, `_g`, `TUTORIAL_STEPS` et ses `goal` inline,
les sous-clés `res`/`bld`/`tech`/`tips`/`ui` des `LOCALES`, `SAVE_VERSION`.
