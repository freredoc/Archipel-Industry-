# RAPPORT — lot I7b : les 52 `short` d'astuce en anglais, espagnol et allemand

**Base** : build **413 / Alpha 18.0** (sortie du lot I7a), SHA-256
`6e8f99edf184dec28a337a887fc7d696ed82b77452036ebea5da5014d974ff2d`, 3 683 264 o.
**Livré** : build **414 / Alpha 18.1**, SHA-256
`42fe678db2f6f712a4318c715af6800b3f3fb8a944d3dca55a9dd2c9b7628a92`, 3 702 848 o —
**delta +19 584 o**. `SAVE_VERSION` **inchangé** (31). Aucune ligne de rendu touchée.

| bloc | SHA-256 (12) | octets | vs base 412 |
|---|---|---|---|
| 1 à 5 | `a50c1c4e7f4a` · `8fbb22187703` · `d949f1c3687a` · `35f4f974f4b2` · `1be53ce44e7b` | — | **identiques** |
| 6 | `3da2b3294b6b` | 451 829 | modifié (IIFE des 52 `short`) |
| 7 | `84e143dfcc8c` | 1 729 141 | modifié (**bump + `GAME_NOTES` seulement**) |

## 1. Ce qui a été fait

Une IIFE de fusion en fin de bloc 6, **52 ids × 3 langues**, garde `if(!L.tips[t].short)`,
non-ASCII en `\uXXXX`. L'entrée d'une astuce absente d'une table est créée avec le seul champ
`short` : `title` et `body` continuent de retomber sur la table française, inchangés.

**Aucune entrée `fr`**, conformément au brief : `short` suit la logique de `title` (chaîne, repli
sur la source), et non celle de `body` (tableau fusionné **par index**, qui obligeait I4 à fournir
un corps français pour amorcer la boucle). Une copie française serait un no-op qui peut diverger.

**Appariement par index, ids issus du runtime.** Le fichier de traduction porte un champ `id`
qui sert de **contrôle**, pas de source : le patcher lit les 52 ids dans le dump de `GAME_TIPS`
(build 413) et s'arrête si un index ne concorde pas. **52/52 concordants.**

**Longueurs** (le popup est étroit — fr : 54 à 139 caractères) :

| langue | min | max | moyenne | ratio moyen / fr | ratio max |
|---|---|---|---|---|---|
| en | 61 | 121 | 86 | 0,94 | 1,15 |
| es | 60 | 126 | 89 | 0,98 | 1,18 |
| de | 60 | 141 | 92 | 1,01 | 1,32 |

Aucune traduction ne dépasse le contrôle de longueur du patcher (`1,35 × fr + 12`).

**Vocabulaire** aligné sur les **corps** traduits en I4 — même carte, même surface : `Collider`
(et non « Kollider »), `Ausbauen`, `Tempo`/`Ausbeute` pour les deux modes d'antenne, `Aufschüttung`,
`Hindernis-Felder`, `botón Subir`, `Land reclamation`.

## 2. Tests

**T2 — application effective.** La vraie porte est `applyToData` : on juge donc `GAME_TIPS`
**après** application, pas le contenu de `LOCALES`. Critère par astuce : la phrase rendue diffère
du français **et** vaut exactement la traduction fournie.

| langue | short traduits et conformes | pageerrors |
|---|---|---|
| en | **52/52** | 0 |
| es | **52/52** | 0 |
| de | **52/52** | 0 |
| fr | **52/52 identiques à la base 412** | 0 |

Témoin nommé par le brief — `collider_cmp1` en allemand :
titre « Zwei Bits vergleichen », short « Ein Draht trägt 0 oder 1. Ein Gatter liest Drähte und
gibt genau einen aus. Da fängt alles an. »

**T3 — rendu écran par le bon chemin.** C'est le **popup** qui est ouvert, pas le panneau Aide :
Aide rend `body` (traduit depuis I4) et passerait même si I7 échouait. `TipPopup` fait
`const body = tip.short ? [tip.short] : …` → le `.tip-body` du popup contient **exactement un
`<p>`**, et c'est lui qu'on lit.

**6 popups ouverts par leurs vrais chemins**, dont **3 `tut_*`** : `bienvenue` (au boot),
`tut_marge` · `tut_debit` · `tut_mine` (le `why` de l'étape courante, re-proposé par `checkTips`),
`collider_penalite` · `collider_arret` (drapeaux consommés par la boucle `frame`).
**9 PASS / 0 KO en de, en, es et fr** (en français l'attendu est la source : le lot doit y être
no-op — il l'est).

⚠ Fermeture par **vrais clics** (`useGhostGuard`, 13.50) et file vidée avant chaque cible
(`showTip` **met en file** depuis 13.80).

**Contre-épreuve, sur la base 412** : la même suite en allemand donne **3 PASS / 6 KO** — les
6 popups y affichent la phrase **française**. Le test est falsifiable.

**T4 — les 3 queues de gabarits** (livrées en I7a) rejouées sur le 414 : **4 PASS / 0 KO**.

**T5 — non-régression.** 52 `title` et 52 `body` inchangés dans les 4 langues · **parité des
paragraphes 52 × 3 : 0 écart** · `node --check` **7/7** · boot 4 langues **0 `pageerror`** ·
7 balises `^<script` avant comme après.

## 3. Points en suspens

- **Le chantier I7 est clos** : les 52 astuces sont désormais traduites **entièrement** — titre,
  phrase du popup (`short`) et texte long (`body`).
- **3 icônes d'astuce** restent des emoji bruts (🎓 `tut_fin`, 📈 `tut_debit`, 🔗 `liaisons_port`) :
  identique sur la base, art écarté en séance (memo 14.74). Hors périmètre.
- **I6** (purge des orphelins, commentaires, convention IIFE) doit être joué **après** ce lot :
  les 52 entrées `short` et les 3 clés `ui` d'I7a ne doivent pas y être prises pour des orphelines.
