# Lot I6 — hygiène des tables i18n (dernier lot du chantier)

**Base** 414 / Alpha 18.1 · **Livré** `GAME_BUILD = 415`, `GAME_VERSION = 'Alpha 18.2'`
`SAVE_VERSION` **inchangé** · **delta −22 293 octets** (3 702 848 → 3 680 555)
SHA-256 `ace4fec02a51af27b64fcdd5bd470938e33aba98563f8084dcaa793cb788e857`
`node --check` **7/7**, 7 balises `<script>`.

Aucune ligne de logique de jeu n'est touchée : le lot retire des **traductions devenues
inatteignables**, corrige un commentaire faux et aligne 3 boucles de fusion sur la
convention du fichier.

---

## 1. Le critère : décidable, pas échantillonné

Le brief proposait de repérer les clés mortes par un balayage au runtime. Ce n'était pas
tenable — un balayage ne visite pas les branches qu'il ne déclenche pas, et c'est
exactement là que se cachent les clés qu'il ne faut PAS supprimer.

`I18N.t(k)` est une **recherche par clé EXACTE** (`L.ui[k] || F.ui[k] || k`). Une clé n'est
donc vivante que si la chaîne exacte est **réellement passée à `I18N.t`**, par l'une des
deux seules voies possibles :

1. **en littéral** — `I18N.t("…")` : relevé par AST sur le bloc 7 (les commentaires en sont
   exclus par construction ; un `grep` naïf s'y fait piéger, cf. §5) → **975 clés** ;
2. **par une voie dynamique** — le fichier ne compte que **11 appels à `I18N.t` dont
   l'argument n'est pas un littéral**, et **aucun n'est une concaténation** : la liste est
   donc **CLOSE**, et vérifiable une fois pour toutes.

Les 8 voies dynamiques, dépouillées **statiquement** (jamais au runtime) :

| voie | libellés | source |
|---|---|---|
| `sensorModesFor` | 13 | toutes les branches `return [...]` de la fonction |
| `TOOLBAR_GROUPS` | 14 | champ `label` |
| `COOLER_DEF` | 5 | champ `label` |
| `GUIDE_OBJECTIVES` | 9 | champ `goal` |
| `TUTORIAL_STEPS.afterToast` | 13 | champ `afterToast` |
| `TUTORIAL_STEPS.progress` | 23 | 1ᵉʳ élément de chaque triplet |
| `ELEVATOR_CAT_LABEL` | 3 | valeurs |
| `DC_STATE_LABEL` | 8 | valeurs |

**Résultat : 1 122 clés `ui` = 969 littérales + 68 routées + 85 MORTES.**

Les 85 sont un **sous-ensemble strict** des 90 candidates de l'inventaire. Les **5
conservées sont exactement les 5 pièges que l'inventaire signalait** — `Déficit d'intrant`,
`Batterie à 0 %`, `Batterie à 100 %`, `Réseau saturé`, `Stock ≥ seuil` — toutes atteintes
par `I18N.t(m.label)` depuis `sensorModesFor`, qu'aucun balayage runtime ne visite en
entier. Le critère statique les rattrape ; l'échantillonnage ne les aurait pas vues.

---

## 2. Ce qui est retiré

**85 clés `ui`** et **2 entrées `tips` orphelines** (`upgrade_vs_v2`, `non_stockable`) :

| | avant | après |
|---|---|---|
| `ui` fr | 404 | **362** (−42) |
| `ui` en / es / de | 1 122 | **1 037** (−85 chacune) |
| `tips` (4 langues) | 54 | **52** (−2) |

Soit **305 propriétés supprimées** : 43 clés présentes dans 3 tables + 42 dans les 4, plus
les 2 ids de `tips` × 4 langues. Les chiffres relevés **au runtime** après patch
recoupent exactement ce compte.

La suppression est faite **par NŒUD** (positions rendues par l'AST), jamais par
substitution de texte : une même chaîne peut être une CLÉ dans une table et une VALEUR dans
une autre, et un `replace` mordrait sur la valeur. Le script **échoue** si une clé annoncée
est absente — c'est ce qui prouve que la base n'a pas bougé sous lui (`manquantes: []`).

Échantillon de ce qui part : `Transit entre îles` et `Transit île` (section retirée du
panneau Production en 14.62), `Réparer Île ` (bouton texte remplacé par un sprite en 13.14),
`Quitter` / `✕ Quitter` / `Quitter le mode en cours` (bouton du 13.53, retiré au 13.54),
`Calcul hors-ligne simplifié`, `Rendre illimité`, `Matériau à irradier`, `Palier 1/2/3`,
`Puise dans`, les libellés de forage, `💡 Revoir les astuces` (dont l'emoji est devenu un
sprite en 11.37 — la chaîne rendue n'a plus l'emoji, l'ancienne clé ne matche plus).

---

## 3. Les trois autres corrections

**Commentaire menteur.** L'en-tête du bloc i18n du tutoriel annonçait « les 8 objectifs
(REMPLACENT les 7 anciens) […] les 8 popups ». La trame en compte **14** depuis le chapitre
île 2 (14.98). Réécrit, avec la précision qui manquait : `applyToData` réécrit les `goal`
**PAR INDEX**, donc les entrées absentes retombent sur le français sans rien décaler.

**3 boucles de fusion alignées.** Trois IIFE d'augmentation écrasaient les clés existantes
(`for (var k in ADD[lg]) L.ui[k] = ADD[lg][k];`) au lieu de la forme gardée
`if (!L.ui[k])` employée partout ailleurs. Elles sont aux lignes **3303 / 3306 / 3307**
(blocs 14.54, 14.91, 14.93) — **et non dans l'IIFE « GUIDE DYNAMIQUE » comme l'annonçait
l'inventaire**. L'alignement est **mesuré NEUTRE** : bloc 6 chargé avec et sans les gardes →
**0 écart sur les 3 tables `ui` complètes**. Ces IIFE ne redéfinissaient donc aucune clé
déjà posée ; la garde ne change rien aujourd'hui, elle empêche une régression demain.

**Rectification de l'inventaire** : `" ne se construit que sur "` a **2** occurrences dans
le code, pas 3.

---

## 4. Validation

**T1 — aucune clé VIVANTE supprimée.** On rejoue l'extraction AST des littéraux passés à
`I18N.t` et on vérifie que chacun est TOUJOURS en table dans les 3 langues. Parcourir la
liste des clés supprimées ne prouverait rien : c'est le sens inverse qui a une valeur.
→ **975 clés, 6 absentes**, les mêmes 6 qu'avant : `"Nv. "`, `"max"`, `" min · "`,
`" kWh / kW"`, `" → "`, `"∞ "` — des fragments typographiques jamais traduits,
**identiques sur la base 414** (contre-épreuve exécutée).

**Garde central.** Aucune des 85 clés purgées n'est atteignable, ni en littéral ni par une
voie dynamique : **0 sur 85**.

**T2 — les 8 voies dynamiques.** 88 libellés routés, **2 anomalies, toutes deux
PRÉEXISTANTES et identiques sur la base** :
- `Surchauffe` (mode de capteur) n'est traduit dans aucune des 3 langues — **signalé, non
  corrigé** : ajouter une traduction n'est pas de l'hygiène, c'est du contenu ;
- `mine_fer` ×2 est un **artefact de mon extracteur** (un tableau imbriqué
  `['mine_fer','carriere']` passé en argument, sous une propriété `progress`), pas une clé
  attendue.

**T3 — le rendu à l'écran, en allemand, comparé à la base 414 (jamais jugé à l'œil).**
13 surfaces capturées par `textContent` : HUD, barre d'actions, menu Bâtiment **déplié**
(93 vignettes, 7 446 car.), fiche détaillée (appui long), Port, Recherche, Énergie,
Production, Options, **et une fiche de capteur sur chacun des 4 supports que
`sensorModesFor` distingue** — accumulateur, conduit, route, bâtiment.
→ **13/13 identiques**, aux deux seules exceptions attendues :
- la ligne de version (`Alpha 18.1 · Build 414` → `18.2 · 415`) ;
- le **signal courant** du capteur sur accumulateur (`GESENDETES SIGNAL 0 ○ falsch` /
  `1 ● wahr`) — une valeur **vivante**, pas un libellé : la base elle-même bascule d'un run
  à l'autre (deux exécutions sur 8571 donnent les deux valeurs), et la seconde exécution de
  la base est **byte-identique** à celle du build patché. Les libellés eux-mêmes
  (`Batterie bei 0 %` / `bei 100 %`) sont inchangés — c'est précisément ce que le test vise.

**T4 — boot des 4 langues.** fr/en/es/de : **0 `pageerror`, 0 erreur console, 0
`tickError`**, horloge qui avance (1 → 5 ticks), canvas peint, **0 occurrence de
`undefined` / `[object` dans le texte de la page**, langue effectivement chargée.
⚠ Le 404 du service worker (`sw.js`, absent du serveur de test) est **PRÉEXISTANT** :
filtré explicitement et **contre-épreuvé sur la base 414**, jamais ignoré en aveugle.

**Round-trip.** Le bloc 6 réextrait du fichier livré est **byte-identique** à ce que l'outil
a écrit, une fois les deux retouches ultérieures appliquées : commentaire **+252**, 3 gardes
**+87** = **+339** caractères, intégralement expliqués.

**Numéro de build.** Relevé sur **toutes les branches distantes**, pas seulement `main` :
max = **414** (`main` et `claude/chantier-l18n-s04ef0`) → **415 libre**.

---

## 5. Pièges rencontrés, à ne pas redécouvrir

⚠ **Les offsets d'acorn sont en unités UTF-16, un index de chaîne Python compte des CODE
POINTS.** Le bloc 6 contient **162 caractères hors BMP** (emoji) : appliquer les positions
de l'AST côté Python décale la coupe jusqu'à 162 caractères et taille au milieu des
voisines. Symptôme observé — des entrées fusionnées, `"❔ Aide s pas (tutoriel)"`, et un
`SyntaxError: Unexpected identifier 'Tuto'`. **La découpe se fait donc en JS**, Python ne
faisant qu'un remplacement de bloc exact, sans arithmétique d'offset.

⚠ **Deux propriétés VOISINES toutes deux supprimées se disputent la même virgule.** Les
plages qui se recouvrent sont **fusionnées** (305 brutes → 212 plages), pas traitées comme
un défaut.

⚠ **Un audit qui compte les CLÉS de propriété comme preuve de vie ne trouve jamais rien** :
chaque clé se trouve elle-même. Il faut exclure `parent.type === 'Property' && parent.key
=== n` — et exclure **le bloc 6 lui-même** des preuves (les tables i18n font souvent
correspondre le français à lui-même).

⚠ **Un `grep` ne distingue pas un commentaire d'un rendu.** `I18N.t("Transit archipel")`
existe dans le fichier… **dans un commentaire** décrivant une ancre. Un contrôle par `grep`
aurait conclu que la clé est vivante ; l'AST voit juste. Inversement, le bloc 7 encode le
non-ASCII en `\uXXXX` : chercher `❔` en clair n'y trouve rien.

⚠ **L'EnergyPanel porte les classes `research-panel port-panel`**, comme le Port : un
sélecteur `:not(.port-panel)` le rate. **Options et la fiche détaillée vivent dans
`.slot-panel`**, pas `.research-panel`. Et **l'inventaire ouvert recouvre le haut du
canvas** — il masque les boutons flottants, dont celui de la couche logique : sans le
replier, aucun capteur n'est atteignable (symptôme : `elementFromPoint` rend un `DIV`).

---

## 6. Signalé, non corrigé — hors périmètre

Ces textes sont **rendus BRUTS**, jamais passés à `I18N.t` : leur traduction était déjà
inatteignable AVANT ce lot, la purge ne fait que retirer une entrée qui ne servait plus.
Les corriger, c'est **poser un `I18N.t` dans le bloc 7** — du contenu, pas de l'hygiène.

- **`kW soutenus`** — libellé d'un prérequis de recherche (`case 'energy'`). C'est le
  **SEUL** des 8 labels de `reqs` qui ne soit pas enveloppé : les 7 autres passent tous par
  `I18N.t`. Oubli isolé du lot I3, correctif d'une ligne.
- **`❔ Aide & astuces`** — titre du panneau (`.slot-title`), en dur.
- **`— vide —`** — inventaire vide.
- **`⚠ MODE DEV — construction gratuite`** — bandeau du mode développeur (édition dev seule).

Autres cas examinés un par un et **volontairement laissés** :
- **`Jonction logique`** était un **doublon** : le nom du bâtiment est traduit par la table
  `bld` (`Logic Junction` / `Unión Lógica` / `Logik-Kreuzung`), que `applyToData` applique.
- **`Partie 1`** et **`Import `** sont des **noms d'emplacement de sauvegarde** (données
  écrites dans l'index des slots), pas des libellés d'écran : ils doivent rester tels quels.
- **`/s`** est un suffixe d'unité concaténé, identique dans les 4 langues.
- **`Surchauffe`** (cf. T2) n'a aucune traduction — préexistant.

**Non touché** : `applyToData`, `I18N.t` et sa chaîne de repli, les tables `bld` / `res` /
`tech` / `tutorial` / `tips` (hors les 2 ids orphelins), les 5 libellés de `sensorModesFor`,
le bloc 7 hors versionnage, `SAVE_VERSION`.
