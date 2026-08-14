# RAPPORT — lot I7a : moteur `short` (no-op strict) + les 3 queues de gabarits

**Base** : build **412 / Alpha 17.9** (`main` après merge de la PR #389), SHA-256
`ed0618fad78ef1f62f6b640ff79a026a15ac0bc31def36f46c2f0c4f2de78c51`, 3 680 992 o.
**Livré** : build **413 / Alpha 18.0**, SHA-256
`6e8f99edf184dec28a337a887fc7d696ed82b77452036ebea5da5014d974ff2d`, 3 683 264 o —
**delta +2 272 o**. `SAVE_VERSION` **inchangé** (31).

Blocs `<script>` : **7**, dont **2 modifiés**.

| bloc | SHA-256 (12) | octets | état |
|---|---|---|---|
| 1 | `a50c1c4e7f4a` | 418 | identique |
| 2 | `8fbb22187703` | 4 397 | identique |
| 3 | `d949f1c3687a` | 10 751 | identique |
| 4 | `35f4f974f4b2` | 131 835 | identique |
| 5 | `1be53ce44e7b` | 1 113 969 | identique |
| 6 | `05ecb9a73a75` | 432 443 | **modifié** (base `333f75ef028b`, 430 824 o) |
| 7 | `6c7e4caed14e` | 1 728 943 | **modifié** (base `5d31df09f159`, 1 728 290 o) |

## 1. Le défaut, re-vérifié au runtime (pas recopié de l'inventaire)

Relevé sur la base 412, dans les 4 langues :

- **52 astuces, 52 `short`** — le popup ne rend donc **jamais** `body` ;
- **0 / 156** `short` diffèrent du français en en+es+de → le champ n'est traduit **nulle part** ;
- `I18N.tip()` ne rendait que `["title","body"]` ;
- longueurs fr **min 54 · max 139 · moyenne 91** (conformes à l'inventaire) ;
- `collider_cmp1` en allemand : titre **« Zwei Bits vergleichen »**, phrase **française**.

## 2. Ce qui a été fait

### A — moteur (2 ancres, `count == 1` chacune)

1. `I18N.tip()` rend `short` : `(a && nonEmpty(a.short) && a.short) || (b && b.short) || ''`.
   Il suit la logique de `title` (chaîne, repli sur la source), **pas** celle de `body`
   (tableau fusionné par index, qui exigeait un corps français non vide pour amorcer la boucle) —
   c'est pourquoi I7b **n'aura pas besoin d'entrée `fr`**, contrairement à I4.
2. `applyToData` écrit `if (nonEmpty(tr.short)) t.short = tr.short;`.

⚠ **La garde est le sous-lot.** Le motif voisin `label` (`if (lb != null)`) écrit la chaîne vide
et **vide le champ** (memo 14.95) ; le reproduire ici afficherait un popup **vide** dans les
4 langues, sans la moindre erreur en console. Les deux ajouts portent un commentaire qui le dit.

### B — les 3 queues de gabarits (4 sites)

| site | fichier | ce qui restait en français |
|---|---|---|
| `tryPlace` (emprise multi-tuiles) | L27592 | `" : besoin de "` |
| `selectTool` (exclusivité d'île) | L28591 | `" ne se construit que sur "` |
| `handleTap`, voie Copier | L31207 | `" ne se construit que sur "` |
| toast de surchauffe | L31573 | `" s si rien ne change"` |

Enveloppement méthode I3 : structure et espaces préservés, **aucune concaténation fusionnée**.
Les 3 clés correspondantes sont fournies en en/es/de par une IIFE de fusion en fin de bloc 6
(`if(!L.ui[k])`, non-ASCII en `\uXXXX`), calées sur la **phrase assemblée** au site d'appel :
l'ordre des mots allemand impose « ist nur baubar auf » et non « kann nur gebaut werden auf ».

⚠ **Écart d'inventaire, mesuré** : le brief annonce **3 occurrences** de
`" ne se construit que sur "` ; il y en a **2** en code. La 3ᵉ (L19422) est un **commentaire**
qui décrit le comportement de `selectTool`. Scan AST indépendant (`TemplateElement` français)
sur les **7 blocs** : **4 fragments / 3 textes distincts** avant, **0 après**.

## 3. Tests

**T1 — no-op prouvé (le test central).** Base 412 ↔ patché 413, **4 langues** :

| langue | `short` identiques | `title` | `body` | `short` vides après patch | pageerrors |
|---|---|---|---|---|---|
| fr | **52/52** | 52/52 | 52/52 | 0 | 0 |
| en | **52/52** | 52/52 | 52/52 | 0 | 0 |
| es | **52/52** | 52/52 | 52/52 | 0 | 0 |
| de | **52/52** | 52/52 | 52/52 | 0 | 0 |

+ `I18N.tip()` rend désormais `["title","short","body"]`.

**Contre-épreuve de la garde** (ce qui rend le test falsifiable) : sur le build 413,
`I18N.tip(id).short` vaut `''` pour **52/52** astuces dans **chacune** des 4 langues, faute de
table. Avec le motif fautif `!= null`, ce sont donc **52 champs × 4 langues** qui seraient
écrasés par la chaîne vide. La garde en préserve 52/52.

**T4 — les 3 queues, jugées sur la phrase RENDUE.** Espion `MutationObserver` sur les toasts,
purge des astuces (leur `.research-backdrop` avale les taps) et attente de disparition du toast
précédent avant chaque déclenchement — sans cette isolation on capte le toast du test précédent
et l'on conclut à un faux PASS (c'est arrivé à la 1ʳᵉ passe).

| site | mode d'atteinte | rendu allemand |
|---|---|---|
| surchauffe | **état forgé** : une entrée poussée dans `g.heatWarn`, dépilée par la vraie boucle `frame` | « Ultrahochdruckpresse (Insel 6) sammelt Wärme an — Überhitzung in ~42 s, wenn sich nichts ändert » |
| emprise 2×2 | **réel** : `__ui().tryPlace(..., verbose)` sur une ancre acceptée dont le carré déborde sur l'eau | « Kernkraftwerk V1: benötigt 2×2 freie Felder (gleiches Gelände) » |
| `selectTool` | **filet défensif**, appel direct de la prop React `onSelect` | « Wolframmine V1 ist nur baubar auf Insel 6 » |
| `handleTap` Copier | **filet défensif**, tuile forgée puis **tap canvas réel** | « Wolframmine V1 ist nur baubar auf Insel 6 » |

**en : 4 PASS · es : 4 PASS · de : 4 PASS.**

⚠ **Deux de ces gardes ne sont pas atteignables par un geste de joueur, et c'est de source** :
`selectTool` n'a **qu'un seul appelant** dans tout le fichier (`onSelect` de la `Toolbar`), et
`ToolButton` **ne l'appelle pas** pour un bâtiment d'une autre île — depuis 14.31 le tap ouvre sa
**fiche** (`if (offIsland) { onDetail(id); return; }`). De même, un bâtiment exclusif à une autre
île ne peut pas se trouver sur l'île courante, donc la voie Copier de `handleTap` ne peut pas s'y
heurter. Ce sont des **filets**, comme le dit le commentaire de `ToolButton` — leur traduction
reste nécessaire (un lot futur peut rouvrir le chemin), leur test est déclaré comme forgé.

**Français : aucune régression.** La même suite T4 jouée en `fr` sur la base **et** sur le patché
rend les **4 phrases identiques au caractère près** (diff vide).

**T5 — non-régression.** 52 `title` et 52 `body` inchangés dans les 4 langues · **parité des
paragraphes 52 × 3 langues : 0 écart** (piège 14.53 : `applyToData` fusionne les `body` par index) ·
`node --check` **7/7** · boot 4 langues **0 `pageerror`** · 7 balises `^<script` avant comme après.

## 4. Points en suspens

- **I7b** fournira les 52 `short` en en/es/de ; jusque-là l'écran est **identique** dans les
  4 langues, aux 3 queues près — c'est exactement ce que T1 mesure.
- **3 icônes d'astuce** restent des emoji bruts (🎓 `tut_fin`, 📈 `tut_debit`, 🔗 `liaisons_port`) :
  identique sur la base, art écarté en séance (memo 14.74). Hors périmètre.
- Le brief place ce lot **avant I6** : les 3 clés `ui` et les futures entrées `short` ne doivent
  pas être rencontrées comme orphelines par la purge.
