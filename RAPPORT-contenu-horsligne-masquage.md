# RAPPORT — LOT CONTENU : annonce du hors ligne + masquage de l'arbre de recherche

## Version produite

| | |
|---|---|
| Base | build **429** / `Alpha 19.6` |
| Livré | build **430** / `Alpha 19.7` |
| `SAVE_VERSION` | **31, INCHANGÉ** — aucun champ de partie créé, aucune migration |
| Branche | `claude/petit-bug-fix-3h59fa`, repartie de `main` (`2df8729`) |

430 est libre : relevé fait sur **toutes** les branches distantes (`origin/main` et
`origin/claude/petit-bug-fix-3h59fa` sont à 429).

`GAME_NOTES` réécrit sans guillemet droit ni apostrophe droite, extrait proprement par le
`grep -oP` de la CI.

---

## Écart de base — et la conformité prouvée autrement

Le brief vise une base de **3 744 974 o**. La base réelle sur `main` en fait **3 745 929**, soit
**+955** : c'est exactement le correctif CI du build 429 (un commentaire, dans le même bloc 7).
**Les deux SHA de fichier du brief ne pouvaient donc pas correspondre.**

Ce qui a été vérifié **à la place**, et qui est plus fort qu'une simple concordance :

1. les **8 ancres sortent à `count == 1`** sur la base réelle ;
2. le **delta est celui annoncé au byte près : +8 432** ;
3. `b06` **concorde même sur la base réelle** (`8817d363…`, le SHA du brief) — le correctif CI ne
   touchait que `b07` ;
4. surtout : le patcher **rejoué sur la base d'avant le correctif** (commit `73197ba`, 3 744 974 o)
   rend **exactement les trois SHA du brief** — fichier complet `d6de36bb…`, `b06 8817d363…`,
   `b07 a9e66509…`, delta +8 432. **Le patch appliqué ici est donc byte-identique à celui du
   rédacteur** ; le seul écart est mon propre correctif.

---

## Ancres — 8/8 à `count == 1`

| # | Nom | Zone | Compte |
|---|---|---|---|
| 1 | `C1-astuce` | fin de `GAME_TIPS` | 1 |
| 2 | `C2-i18n` | fin du bloc 6, après la dernière IIFE i18n | 1 |
| 3 | `C3-capture` | `finishCatchUp`, avant `primeTipsSeen(g)` | 1 |
| 4 | `C4-report` | `finishCatchUp`, objet du récap | 1 |
| 5 | `C5-recap` | `OfflineModal`, après `.offline-sub` | 1 |
| 6 | `C6-flag` | `ResearchPanel`, après `portPool` | 1 |
| 7 | `C7-liste` | `ResearchPanel`, source du `.map` | 1 |
| 8 | `C8-carte` | `ResearchPanel`, fin de `.rp-list` | 1 |

---

## Blocs `<script>` — comptés explicitement AVANT tout `node --check`

**7 blocs** par `(?m)^<script`, et **7 fichiers réellement écrits**. L'extracteur **purge son
dossier de sortie** puis **refuse de rendre la main** si le nombre de fichiers écrits diffère de 7 ;
la boucle `node --check` **recompte les fichiers** et refuse de conclure si le compte n'est pas 7.
C'est la parade au faux vert décrit par le brief (un dossier partiel laissé par une extraction
interrompue faisait annoncer « 7/7 » sur un seul fichier).

### SHA-256 ré-extraits du fichier patché — **avant** bump

| Bloc | État | SHA-256 |
|---|---|---|
| b01–b05 | identiques à la base | inchangés |
| b06 | **MODIFIÉ** (tables i18n) | `8817d3637e1651f8649ab46148f6a82c564b04b89aa7265fe3a8e1c1befd7001` **= SHA du brief** |
| b07 | **MODIFIÉ** | `f6ff2c41da785a3f9036187d3bca1d723bb3dc84764fd732873fbe861d14a54f` (≠ brief : +955 o de correctif CI) |
| fichier complet | | `858ef0dded3c6f52cb397b2336cfd125ba75c14e2fbbb2851161043bec7d4613` |

### Après l'écart demandé + bump

| Bloc | SHA-256 |
|---|---|
| b06 | `8817d3637e1651f8649ab46148f6a82c564b04b89aa7265fe3a8e1c1befd7001` (inchangé) |
| b07 | `ea62f2cf0c48e0eead2000ed2b12143d031364e1d60d67d1de59c0b1b332e433` |
| fichier complet | `3e7da180f5f8a954483d4c31f6e9df5c2eabb634d142f6f1380cf7a7d590e13a` |

**Taille : 3 745 929 → 3 759 910 o.** Le patch seul pèse **+8 432** (exactement l'attendu) ; le
reste est l'écart demandé et le commentaire cumulatif de version.

**Ordre respecté** : patcher → SHA → écart demandé → bump.

### `node --check`

**7/7 sur les 3 variantes CI** (`game-public`, `game-dev`, `game-store`), avec la garde de
complétude décrite ci-dessus.

### Gardes de comptage de la CI — rejouées **APRÈS** l'écriture des commentaires

C'est la leçon du build 429, qui avait cassé `main` pour cette raison exacte.

| Garde | Attendu | Mesuré |
|---|---|---|
| lien de soutien dans la variante magasin | 0 | **0** |
| lien de soutien dans la publique | 1 | **1** |
| `const SELF_UPDATE = true;` dans le magasin *(motif non ancré)* | 0 | **0** |
| `^const DEV_BUILD = true;$` (dev) / `false` (publique) | 1 / 1 | **1 / 1** |
| `^const SELF_UPDATE = true;$` (publique) / `false` (magasin) | 1 / 1 | **1 / 1** |

`GAME_BUILD` = 430 et `GAME_NOTES` extraits proprement.

---

## ÉCART DEMANDÉ PAR ETHAN — le nœud 28 est masqué lui aussi

> « nœud 28 caché et devient visible lorsqu'on termine le nœud 25 »

Le brief laissait le nœud 28 **lisible** (« c'est l'objectif qui porte le joueur jusque-là »). Or
il s'appelle **« Accès Île 6 »** : le montrer **annonce l'existence de l'île 6**, que la carte
traite justement comme la surprise du jeu (`ARCHI_CACHEE`). La demande est donc cohérente avec
l'intention même du volet B, et elle est appliquée.

Le masquage se fait désormais en **deux paliers** :

| État | Liste affichée | Carte repliée |
|---|---|---|
| 25 non confirmé | nœuds **1 à 27** | oui |
| 25 confirmé, 28 non | nœuds **1 à 28** | oui |
| 28 confirmé | **tout l'arbre (43)** | non |

**La sûreté est prouvée par le graphe pour les DEUX paliers, pas supposée.** Relevé dans
`TECH_NODES` : `prereq[28] = 25` (comme 26 et 27), et 29→43 forment une chaîne strictement
linéaire remontant à 28. Tant que 25 n'est pas confirmé, le nœud 28 est **nécessairement**
`locked` ; aucun palier ne peut donc masquer un nœud `available` ou `condition_ok`, ni laisser la
pastille de notification allumée sans cible. **Vérifié aussi au runtime** (T6bis).

**26 et 27 restent visibles** bien qu'ayant eux aussi `prereq: 25` : leurs noms (« Mines V3 +
Fours à Arc », « Antenne Amplificatrice ») ne dévoilent rien de l'île 6. On masque un **spoiler**,
pas un palier de progression.

---

## Suite de validation

**Setup réellement utilisé** — Chromium 1194 piloté par **playwright-core**, servi en HTTP sur
`127.0.0.1:8099` depuis la racine du dépôt, `localStorage` purgé par `addInitScript`, langue
forcée, attente de 6 s après `networkidle`, astuces fermées par `.tip-ok` (jamais `.remove()`).
`ResearchPanel` et `OfflineModal` montés directement dans un conteneur détaché (ce sont des
déclarations de fonction d'un script classique, donc des propriétés de `window`). **T8 mesuré par
le canal CDP `Runtime.exceptionThrown`**, en plus de `pageerror`.

**T1 → T8 et T-extra ont été rejoués deux fois, aux valeurs strictement identiques** (aucun
flottement) ; le contre-test T7, joué sur la base non patchée, l'a été **une fois**.

| Test | Verdict | Mesure |
|---|---|---|
| T1 — astuce présente, 3 § dans les 4 langues | **PASS** | `GAME_TIPS` = **53**, `body` = 3 (source et i18n), en fr/en/de |
| T2 — les 4 langues réellement servies | **PASS** | en → « The factory runs without you » ; de → « Die Fabrik läuft ohne dich », p1 traduit |
| T3 — `short` fr reste la source | **PASS** | `I18N.tip().short` = `''` en fr, `GAME_TIPS` conserve son `short` fr |
| T4 — phrase du récap, une fois et une seule | **PASS** | `explain:true` → **1** `.offline-note` ; `false` → **0** |
| T5 — la liste s'arrête au seuil | **PASS** *(valeurs modifiées par l'écart)* | voir tableau ci-dessous |
| T6 — dévoilement d'un bloc | **PASS** | 28 confirmé → **43** cartes, **0** masquée |
| T6bis — rien de validable n'est caché | **PASS** | partie neuve : statut 28 = `locked`, statuts 29→43 = `['locked']` |
| T7 — contre-test base 429 | **PASS** (échoue comme prévu) | voir ci-dessous |
| T8 — non-régression au boot | **PASS** | **0** `pageerror`, **0** exception CDP, en fr/en/de |
| T9 — sur appareil | **NON COUVERT** | voir points en suspens |
| T-extra — chemin joueur réel | **PASS** | ajouté hors brief, voir ci-dessous |

### T5 / T6 — masquage mesuré, aux trois états

| État des nœuds | Cartes | `idMax` | Cartes `????` | Dernière carte |
|---|---|---|---|---|
| tous `locked` (25 non confirmé) | **28** | **27** | **1** | `???? — more research lies beyond` |
| 1→25 `confirmed` | **29** | **28** | **1** | `???? — more research lies beyond` |
| 1→28 `confirmed` | **43** | **43** | **0** | `28. Island 6 Access` |

*(Les valeurs de T5 diffèrent de celles du brief — 29 cartes / `idMax` 28 — précisément à cause de
l'écart demandé : le premier palier s'arrête maintenant à 27.)*

### T7 — CONTRE-TEST sur la base 429 non patchée

| État des nœuds | Cartes | `idMax` | Masquées | Dernière carte |
|---|---|---|---|---|
| tous `locked` | **43** | 43 | **0** | **`43. Collider P3`** |
| 1→25 `confirmed` | **43** | 43 | **0** | `25. Nuclear Engine Factory` |
| 1→28 `confirmed` | **43** | 43 | **0** | `28. Island 6 Access` |

Et `.offline-note` : **0** avec `explain:true` **comme** avec `explain:false`.

Le contre-test est falsifiant à deux titres : il montre que la phrase du récap vient bien du patch,
et que **43 cartes dans les trois configurations** — donc l'écart mesuré vient du **masquage**, pas
du tri de `techNodesOrdered`. Ce tri est d'ailleurs visible dans la ligne du milieu (les confirmés
coulent en bas, la dernière carte devient `25.`) : c'est exactement pourquoi le filtrage se fait
**par `def.id` et jamais par rang**.

### T-extra — le chemin joueur réel *(ajouté hors brief)*

Partie neuve, clic réel sur « Passer » du bandeau de tutoriel, puis file d'astuces déroulée une par
une :

- **patch 430** : astuces ouvertes = `["La recherche", "L'usine tourne sans vous"]`,
  `tipsSeen.hors_ligne` = **true** ;
- **base 429** : `["La recherche"]`, `tipsSeen.hors_ligne` = **false**.

L'astuce arrive donc bien au joueur par le vrai chemin — derrière la file d'astuces différées
pendant le tutoriel, ce qui est le comportement attendu — et le drapeau qu'elle pose est celui-là
même qui éteindra la phrase du récap.

---

## Écarts par rapport au brief

1. **Le nœud 28 est masqué lui aussi**, révélé à la confirmation du 25 — **demande explicite
   d'Ethan**, détaillée plus haut. Conséquence sur T5 : 28 cartes / `idMax` 27 au premier palier,
   là où le brief annonçait 29 / 28.
2. **Écart de base de +955 o** (mon correctif CI du build 429) : les SHA de fichier du brief ne
   pouvaient pas correspondre. Conformité établie autrement, par rejeu sur la base d'avant le
   correctif — les trois SHA du brief y sont reproduits exactement.
3. **Banc porté sur playwright-core** (`puppeteer-core` et `@sparticuz/chromium` absents de
   l'image ; `playwright install` proscrit). Pilote seul ; les montages et assertions sont ceux du
   brief, et le canal CDP exigé par T8 est bien utilisé.
4. **Un test ajouté** (T-extra), falsifiable, qui couvre le chemin joueur réel de bout en bout.

---

## Points restés en suspens

- **T9, sur appareil** (S25 FE, 3 boutons) — deux coups d'œil qu'aucun banc ne remplace : la carte
  repliée `????` en bas du panneau Recherche doit se lire comme une intention et non comme un bug ;
  la phrase du récap doit tenir dans `.offline-note` **en allemand**, la plus longue des quatre.
- **Une partie déjà en cours ne verra jamais le popup** de l'astuce : `primeTipsSeen` marque vues
  les astuces dont la condition est déjà remplie au chargement. C'est le comportement voulu (pas
  d'avalanche rétroactive) et c'est le cas de la partie d'Ethan — l'astuce reste consultable dans
  l'Aide, et la phrase du récap prend alors le relais à la première absence.
- **Hors périmètre, non touché** : `OFFLINE_MAX_TICKS` et le mécanisme de rattrapage, `ARCHI_CACHEE`
  et la carte, `TECH_NODES` (aucun `prereq` ni coût modifié), `primeTipsSeen`, `SAVE_VERSION`, la CI.
- **PR ouverte, non mergée** — le merge sur `main` appartient à Ethan : il déclenche `android.yml`,
  qui republie l'APK, `index.html` et `version.json`.
