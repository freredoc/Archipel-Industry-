# RAPPORT — Lot « mécanique ferme »

**Livré en `GAME_BUILD = 445` / `GAME_VERSION = 'Alpha 21.2'`.**
`SAVE_VERSION` **inchangé (31)** — aucune migration, conformément au brief.

Base : build 444 / Alpha 21.1, artefact `4ee2746c…`. **Exacte** : `src/index.src.html` mesuré à
2 570 416 o et l'artefact à 4 039 112 o, aux valeurs annoncées près. Numéro de build choisi après
relevé de `GAME_BUILD` sur **toutes** les branches distantes (max = 444, donc 445 libre).

---

## 1. Empreintes

| Étape | `src/index.src.html` | Artefact | SHA-256 de l'artefact |
|---|---|---|---|
| Base 444 | 2 570 416 | 4 039 112 | `4ee2746ccd422cf0b414bcb1875dda7b03453eab1d23bbfddb4d42a8905fb516` |
| **Patcheur seul** | **2 572 817 (+2 401)** | **4 041 513** | **`1d97638852dbd658b0420d2759e0853f3ed5c7cee35dadb54165deb708bacd41`** |
| Livré (patch + correctifs + bump) | 2 578 744 (+8 328) | 4 047 440 (+8 328) | `d5ee2f39a5689d039c36812aecd51699fe4f3a0c882a588e89a631d83352ec14` |

**Le patcheur seul reproduit le brief à l'octet et au SHA près** : +2 401 sur la source, 4 041 513 o
d'artefact, `1d976388…`. Le patch appliqué est donc exactement celui du rédacteur — c'est mesuré
avant tout bump, sur une reconstruction dédiée, pas déduit.

`src/sprites-inline.js` n'est pas touché : les deux deltas d'artefact et de source sont égaux.
`index.html`, `sw.js` et `version.json` ne sont pas édités à la main — la CI les régénère après
fusion. Simulation faite : `sed` du cache → `var CACHE = 'archipel-445';`.

## 2. Ancres

**17 ancres, toutes à `count == 1`**, comptées à blanc AVANT toute écriture.

| Réf | Origine | `count` | Objet |
|---|---|---|---|
| F0 | patcheur | 1 | chargeur `pl.zn`, `z.length >= 2` |
| F1 | patcheur | 1 | sérialisation du compteur par champ |
| F2 | patcheur | 1 | `farmTileBurst` extrait, `farmBurst` le somme |
| F3 | patcheur | 1 | le tick avance champ par champ |
| F4a/b/c | patcheur | 1 / 1 / 1 | `farmZoneReach`, refus de coupure, refus d'îlot |
| F5 | patcheur | 1 | `carriere_rustique` dans `NO_COAST` |
| F6 | patcheur | 1 | `FELL_YIELD` 60 → 100 |
| A-coupe | ajout | 1 | commentaire de `FELL_YIELD` rendu faux par F6 |
| B-def / B-perenne / B-coupee / B-appel | ajout | 1 ×4 | stade dessiné par CHAMP |
| C-i18n-en / es / de | ajout | 1 ×3 | les 2 toasts de F4 traduits |

## 3. Contrôles faits AVANT d'écrire

- **Les 14 sites qui lisent `bld.zone` ont été relevés un par un.** Aucun ne reconstruit les tuples
  en couples, sauf la sérialisation — qui est précisément l'ancre F1. Le tuple à 3 valeurs traverse
  donc `farmIndex`, le tick, les trois compteurs de zone et la fiche sans être rogné nulle part.
- **`NO_COAST` fait bien DEUX passes** (l. 7003-7012) : la première AJOUTE `coast` à tout bâtiment
  listant `land` **en sautant les entrées de `NO_COAST`**, la seconde le RETIRE des seules entrées
  de la table. La def de la carrière rustique porte littéralement `['land','resource','coast']` : la
  seule porte est bien la table, exactement comme l'annonce le brief.
- **Ordre F0 en premier** respecté par construction (le patcheur pose ses ancres dans l'ordre du
  fichier et échoue bruyamment sinon).

## 4. Suite de validation

**33 assertions, 0 KO, rejouées deux fois sans flottement.** Chaque test tourne sur le **témoin 444**
puis sur le **patch**, servis côte à côte en HTTP.

### T1 — carrière hors côte (3 PASS)

| | témoin 444 | patch 445 |
|---|---|---|
| `carriere_rustique.terrains` | `['land','resource','coast']` | **`['land','resource']`** |
| `carriere.terrains` (témoin) | `['land','resource']` | `['land','resource']` |
| `scierie.terrains` (témoin) | `['land','coast']` | `['land','coast']` |

La scierie **garde** son littoral : le test échouerait si la normalisation avait été cassée pour
tout le monde.

### T2 — la coupe (1 PASS)
`FELL_YIELD` : **60 → 100**.

### T3 — la salve est par tuile (3 PASS)
`typeof farmTileBurst` : **`undefined` → `'function'`**. Sur une ferme à 3 champs sans légumineuse
voisine : `farmBurst` = **540** = somme exacte des trois `farmTileBurst` (180 chacun). Le total est
identique témoin/patch — la refonte ne déplace pas la valeur, seulement le moment où elle tombe.

### T4 — le chargeur (5 PASS) — **et il corrige le brief**

Sauvegarde forgée par le VRAI chemin (ferme réelle posée sur l'île 8, écriture forcée par
`saveTimer` + passage en arrière-plan), puis relue des deux côtés.

| Sauvegarde | témoin 444 | patch 445 |
|---|---|---|
| `zn: [[r,c,17],[r2,c2]]` (le tuple du brief) | zone = **`[[10,15]]`** — 1 couple | `[[10,14,17],[10,15]]`, 17 intact |
| `zn: [[r,c,17],[r2,c2,17]]` (ce que le patch ÉCRIT) | zone = **`null`** | `[[10,14,17],[10,15,17]]` |

⚠ **Le brief annonce qu'avec son tuple mixte le témoin rend une « zone vide ». C'est faux** : le
couple nu y **survit** (`z.length === 2` l'accepte), seul le triplet est jeté. La vraie
démonstration est la seconde ligne : dès qu'un tick a tourné, **tous** les tuples portent leur
compteur, et 444 rend alors une zone **réellement vide** — la ferme perd toute sa zone en silence.
C'est ce cas-là qui rend F0 indispensable, et il est plus grave que celui du brief.

### T5 — l'exploit (4 PASS) — **le brief le déclarait non automatisable**

| | témoin 444 | patch 445 |
|---|---|---|
| Salve après ajout de 3 champs à 55/60 | **720 bois** | **180 bois** |
| Restes des 3 champs neufs | — | `55` chacun (cycle plein entamé) |

⚠ **Le brief écrit : « une ferme forcée en mémoire n'entre jamais dans `farmList` ». Mesuré : elle y
entre dès qu'elle est RELIÉE AU PORT** (`regime = 1`, `disc = false`) — et depuis le lot progression
la ferme tire **0 kW**, donc la route suffit, sans électricité. Le montage est réel : chemin tracé
par parcours depuis le port, forêt **abattue** le long du tracé (`forest` → `land`, ce que fait la
bûcheronneuse), 12 tuiles de route, `rebuildNetworks`, puis **85 ticks de `onTick` réels**. T5 est
donc **mesuré, pas reporté sur appareil**.

### T6 — contiguïté (9 PASS) — **également automatisé**

`toggleFarmZone` et `farmZoneReach` sont internes au composant, invisibles depuis la page. Ils ont
été **extraits VERBATIM de l'artefact produit** (découpe par comptage d'accolades consciente des
chaînes ET des commentaires) et exercés sous Node avec des bouchons : le code exécuté est celui du
jeu, pas une réécriture.

| Cas | Attendu | Mesuré |
|---|---|---|
| (a) clairière non voisine | refus + toast | refus, « ❌ Les champs doivent se toucher et rejoindre la ferme » |
| (a-bis) première clairière contre la ferme | accepté | accepté, aucun toast |
| (a-ter) chaînage au bout de la zone | accepté | accepté |
| (b) ligne de 3, retrait du milieu | refus + toast | refus, « ❌ Retirer ce champ couperait la zone » |
| (c) retrait d'une extrémité | accepté | accepté |
| **(d) zone DÉJÀ en morceaux** | retrait légitime possible | fragment détaché retirable ; extrémité du fragment relié retirable ; **milieu du fragment relié toujours refusé** |
| (e) plafond de zone | prioritaire | « ❌ Zone au maximum » l'emporte sur le test de contiguïté |

Le cas (d) confirme la conception du brief : la règle **compare l'avant et l'après**, elle n'exige
pas une zone parfaite au départ.

### T7 — le stade dessiné (3 PASS) — voir §5

### T8 — non-régression (5 PASS)
Aucune `pageerror` ni erreur console au boot des deux builds, ni pendant le montage + 85 ticks.
`SAVE_VERSION` inchangé à 31. `node --check` **7/7 sur les trois variantes CI** (publique, dev,
magasin), 7 blocs `^<script` détectés par le build.

⚠ Le banc a d'abord rendu un KO de bruit (404 favicon) qui n'apparaissait **que sur le premier
contexte de l'origine** — Chromium met le favicon en cache par origine, donc il ne se voyait que sur
le témoin, qui tourne en premier. Ce n'était pas une différence de build. Corrigé à la source
(`favicon.ico` et `sw.js` déposés dans le dossier servi) plutôt qu'expliqué dans un filtre : le banc
est désormais **déterministe, 0 KO des deux côtés**.

## 5. Écart : un défaut d'affichage que F3 introduit, mesuré et corrigé

`farmStageKey(bld)` est appelé **par tuile** au dessin (l. 31765) avec la ferme propriétaire, et
lisait `bld.cycRem`. Après F3, ce champ ne vaut plus que « la prochaine récolte » — **le champ le
plus avancé**. Les quatre champs se dessinaient donc au stade du plus mûr.

Mesure sur trois champs aux restes 58 / 30 / 2 (ce que F3 produit réellement) :

| | clés de stade rendues |
|---|---|
| 444 + F3 seul | `foret_4`, `foret_4`, `foret_4` |
| Livré | `foret_souche`, `foret_2`, `foret_4` |

Un champ tout juste replanté (reste 58 sur 60) s'affichait **prêt à récolter** et ne rendait rien —
soit exactement la confusion que F3 existe pour supprimer. Amplitude : deux champs décalés de 30 s
donnent **10 relevés faux sur 20** sur un cycle complet.

Le brief énumère « trois helpers d'affichage » qui lisent encore `cycRem` et accepte la lecture
« prochaine récolte » **pour la fiche** — ce qui est juste. Mais `farmStageKey` n'est pas la fiche,
c'est l'art de la tuile, et là « le plus avancé » n'est pas une simplification : c'est faux.
Correctif : `farmTileRem(bld, r, c, def)` lit le compteur **de la tuile**.

⚠ **`r`/`c` sont facultatifs** : sans eux la fonction retombe sur `bld.cycRem`, donc tout appelant
qui ne les passe pas garde exactement le comportement d'avant (vérifié : T7 troisième assertion).
Il n'existe qu'un seul appelant, et il les passe.
⚠ **L'installation de l'hévéa (`hevInst`) reste PAR FERME** : c'est la plantation qui s'installe,
pas la tuile. ⚠ **La légumineuse garde aussi son compteur de ferme** : F3 ne touche pas sa branche
(elle sort avant, sur `!cu.P || !cu.res`).

## 6. Autres écarts au brief

- **Le commentaire de `FELL_YIELD` mentait après F6.** Il affirmait « le total par tuile est
  INCHANGE (60) » juste au-dessus de la constante que F6 porte à 100. Réécrit sur place plutôt que
  laissé : un commentaire mensonger qui survit à la correction qu'il décrit est pire que pas de
  commentaire. Le commentaire de journal du lot L2 (bloc de version, l. 9761) n'est PAS retouché —
  c'est un journal, exact pour son build.
- **Les deux toasts de F4 n'avaient aucune traduction.** Leurs **trois voisins de la même fonction**
  en ont trois chacune (en/es/de) : les laisser en repli français aurait fait diverger un même
  panneau avec lui-même selon la ligne touchée. Six entrées ajoutées, mesurées à 3 occurrences
  échappées chacune, comme leurs voisines.
- **T5 et T6 sont mesurés, pas reportés.** Le brief les classait « à faire sur appareil » ; les deux
  motifs avancés se sont révélés inexacts (cf. T5 et T6 ci-dessus).

## 7. Gardes de comptage de la CI

Rejouées **après** avoir écrit mes propres commentaires — c'est l'ordre qui compte (le build 429 a
cassé `main` parce qu'un contrôle juste avait été joué trop tôt) :

| Garde | Attendu | Mesuré |
|---|---|---|
| `grep -c 'ko-fi'` variante magasin | 0 | **0** |
| `grep -c 'const SELF_UPDATE = true;'` magasin | 0 | **0** |
| `grep -c 'ko-fi'` variante publique | 1 | **1** |
| `DEV_BUILD` basculé en dev / faux en publique | oui | **oui** |
| Auto-updater conservé en publique | oui | **oui** |

`GAME_NOTES` : **une seule ligne**, 306 caractères, **aucun guillemet droit**, ASCII pur.
Extraction CI simulée (`grep -oP` puis `jq --arg`) : la chaîne ressort intacte.

## 8. Points ouverts

- **Contrôle visuel sur appareil, non couvert ici.** Que les champs d'une même ferme affichent
  désormais des stades différents se mesure (T7) mais ne se juge qu'à l'œil : lisibilité des cinq
  stades côte à côte sur une parcelle contiguë, et rendu du halo de zone en cours d'édition.
- **Le panneau de la ferme reste au lot suivant** (pavé retiré, boutons de culture toujours
  affichés et estompés quand verrouillés, temps et quantité dans le libellé, halo de zone), ainsi
  que la fusion des nœuds 48/49. Le lot présent lui fournit l'information qui lui manquait : chaque
  champ porte maintenant son propre reste.
- **`bld.cycRem` reste une seconde lecture du même état.** Il vaut désormais le minimum des restes,
  entretenu par le tick ; la fiche et la sérialisation le lisent encore. Ce n'est plus une vérité
  parallèle (il est dérivé à chaque tick), mais un lot qui voudrait afficher « la récolte du champ
  X » devra passer par `farmTileRem`, pas par lui.
- **Coût du parcours au dessin** : `farmTileRem` balaie la zone de la ferme pour chaque tuile de
  champ visible. La zone est bornée par `farmZoneMax` (1 + niveau), donc quelques dizaines de
  comparaisons par image au pire — aucune mesure de dégradation n'a été faite, aucune n'a paru
  nécessaire à cette échelle.
- **L'exploit fermé par F3 avait un jumeau qui ne l'est pas** : rien n'empêche de retirer un champ
  juste avant la salve. Le reste du champ retiré est perdu avec lui (le tuple part de `bld.zone`),
  donc l'opération est purement défavorable au joueur — signalé pour mémoire, pas corrigé.
