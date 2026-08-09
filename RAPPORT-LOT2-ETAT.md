# RAPPORT — LOT 2 « NETTOYAGE D'ÉTAT »
## base 382 / Alpha 14.99 → **Alpha 15.0 / build 383**

## 0. Métadonnées

| | |
|---|---|
| Modèle réellement utilisé | **Claude Fable 5** (le brief demandait Opus 5 — la session tournait sur Fable 5 ; même situation qu'au lot 1, signalée au §4 : aucune décision « créative » en compensation, patch appliqué verbatim) |
| SHA-256 d'entrée | `8972492af09143fa29ef0d4cd1251ecb7a91a839b9cffbb980185e5229e85ef5` (= le lot 1 livré, re-vérifié avant patch ; contrôle accessoire `grep -c basePowerRange` = **5** ✓) |
| SHA-256 de sortie **avant bump** | `c7c2effb2d55a954fe38d4ca686a5c15114ecea44075536af75052e9aa773974` — **EXACTEMENT la valeur attendue à l'étape 2 du brief** |
| Numéros retenus | `GAME_BUILD = 383`, `GAME_VERSION = 'Alpha 15.0'` (premiers disponibles après 382/14.99 ; « 15.0 » suit le précédent 13.99 → 14.00, et c'est le numéro que les commentaires du patch du brief portent eux-mêmes) |
| SHA-256 du fichier livré (après bump) | `1c583204735db9fb06883e324232a8303630d84231a5d5252ab21a60696f67b2` · 3 351 944 octets |
| `SAVE_VERSION` | **31, inchangé** (aucune lecture conditionnée par la version ; la purge `ciment_irradie` suit le modèle 14.16, idempotente) |

## 1. Application du patch

Le patcher du §2 a été **extrait à l'octet près du fichier du brief** (bloc ```python découpé entre les clôtures — jamais retapé) et exécuté tel quel sur la base :

```
A-purge          count=1        C4-confirms      count=1
B-fmtPower       count=1        C4-goal-1        count=1
C1-waterFrom     count=1        C4-goal-2        count=1
C1-bestPool      count=1        C4-goal-3        count=1
C2-waterNeed     count=1        C4-goal-4        count=1
C3-liveDirs-decl count=1        C4-from-1        count=1
C3-liveDirs-push count=1        C4-from-2        count=1
C3-mirrors       count=1        C4-from-3        count=1
D-comment-tour   count=1        C4-dcState       count=1
D-comment-cooler count=1        C4-dcRate-1      count=1
                                C4-dcRate-2      count=1
                                C4-wireNid       count=1
patch applique, 3351281 octets
```

**22/22 ancres uniques · 3 351 281 octets = la valeur annoncée · SHA de sortie conforme au caractère près. Aucune divergence, aucune réparation manuelle.**

## 2. Vérifications

**Blocs `<script>` (avant bump)** — extraits du fichier **non filtré**, extracteur conservant le texte collé à la balise ouvrante (les blocs 3/4 commencent par `/**` collé au `<script>`, piège de l'étape 3) :

| Bloc | Caractères | SHA-256 | Verdict |
|---|---|---|---|
| 1 | 411 | `50efcead…` | conforme (inchangé) |
| 2 | 4 339 | `6820628a…` | conforme (inchangé) |
| 3 | 10 750 | `efe1e3ea…` | conforme (inchangé) |
| 4 | 131 834 | `58303967…` | conforme (inchangé) |
| 5 | 1 111 780 | `3b3948d4…` | conforme (inchangé) |
| 6 | 232 098 | `24521d5b…` | conforme (inchangé) |
| 7 | 1 580 373 | `aa685220…` | conforme (**le seul modifié**, comme annoncé) |

(Même convention d'extracteur qu'au lot 1 : le `\n` qui suit une balise « nue » est retiré avant hachage — les blocs 3/4 concordent « bruts ». **7/7 identiques aux empreintes du brief.**)

**`node --check`** : 7/7 OK **avant** bump ET 7/7 OK **après** bump.

**Contrôle de résidu (étape 4)** — grep `waterFrom|waterAvail|waterNeed|waterDrawn|gateInhCur|gateLive\b|co\.confirms|co\.goal|co\.from|co\.dcState|co\.dcRate|co\.wireNid` sur le fichier patché avant bump → **2 lignes, toutes deux des COMMENTAIRES produits verbatim par le patch du brief, 0 code** :
- l.11481 — le commentaire historique du bloc portes (« …ont été retirés en 15.0 : personne ne les lisait ») = celui que l'étape 4 attend ;
- l.20014 — le commentaire **D-comment-tour du brief lui-même** (« elle ne lit PAS `bld.waterNeed`/`waterDrawn`, retirés faute de lecteur ») : il nomme les champs, donc il matche la regex. L'étape 4 annonce « une seule ligne » — elle ne compte pas sa propre édition D ; écart purement documentaire, voir §4.

Sur le fichier **livré** (après bump), 2 lignes de plus : mon commentaire de version (l.8523-8524) nomme les champs retirés — même nature, documenté ici.

**Intacts, vérifiés** : `gateWiredInh` = **5** occurrences ✓ · `gateLiveDirs` = **4** ✓ · `basePowerRange` = **5** ✓ · `bld.regime`/`pwrAvg`/`nominalPower`/`minPower` non touchés (aucune de leurs lignes dans le diff).

**Bump** : commentaire de version (7 lignes, style du fichier) + `GAME_BUILD/GAME_VERSION` + `GAME_NOTES` réécrit. `GAME_NOTES` contrôlé : **0 guillemet droit interne** (l'extraction CI `[^"]*` rend la chaîne entière, non tronquée), **0 séquence `\u`** (UTF-8 littéral, guillemets français — contraintes 14.96).

**Boot réel (étape 5)** — Chromium headless, serveur lancé depuis le dépôt, fichier bumpé : splash retiré, **canvas peint à 100 %** (échantillonnage alpha), horloge qui avance (`playTicks` 2 → 3), **0 `pageerror`**.

## 3. Suite de validation — T1..T9

Tests par **gestes réels** (souris brute aux coordonnées, appuis longs, taps canvas, couche logique ouverte par son vrai bouton), sur le fichier livré (« APRÈS ») **et** sur la base 382 servie en parallèle (« AVANT », contrôles négatifs). **Suite rejouée 2 fois — résultats strictement identiques, zéro flottement.**

| # | Verdict | APRÈS (observé) | AVANT (contrôle négatif observé) |
|---|---|---|---|
| **T1** série `fmtPower` (0 · 0,03125 · 0,125 · 0,25 · −0,25 · 1 · 999 · 1000) | **PASS** | `0 kW · 31 W · 125 W · 250 W · -250 W · 1 kW · 999 kW · 1 MW` — **les 8 valeurs attendues au caractère près** (zéro exact resté en kW) | `0 kW` pour les cinq premiers ✓ |
| **T2** Four à Arc Fer, fiche détaillée | **PASS** | `250 W – 10 kW` | `0 kW – 10 kW` ✓ |
| **T3** Mine Fer V3, fiche détaillée | **PASS** | `31 W – 250 W` | `0 kW – 0 kW` ✓ — le défaut (b) du §1 du brief |
| **T4** Panneau Énergie, bilan net exactement nul | **PASS** | pastille `+0 kW`, ligne « Bilan réel » `+0 kW` — jamais « 0 W » | identique ✓ |
| **T5** Tour aéroréfrigérante alimentée puis à sec (branche CITERNE restructurée par C1) | **PASS** | ligne « Eau » : `100% · 256 eau/s` → à sec `0% · 256 eau/s` (orange) | **chaînes STRICTEMENT identiques** à l'APRÈS ✓ |
| **T6** Porte AND, face SUD câblée mais inhibée | **PASS** | avertissement affiché (`gateWiredInh` vivant, `[1]` = face S) ; faces vives correctes (O · E via `gateLiveDirs()`) ; `gateInhCur`/`gateLive` **absents du runtime** (écritures retirées) | avertissement **identique octet pour octet** ; `gateInhCur: [1]`, `gateLive: [2,3]` encore écrits ✓ |
| **T7** Collisionneur : `off → starting → running`, pénalité, DC en service puis en pause | **PASS** | fiche complète (palier P3 · 6 saveurs · 3 bits, confirmations 7/10 000, cadence DC, état DC « à l'arrêt », câble « non relié », après pénalité `state off · penalties 3 · timer 0`) — **les deux fiches ET l'état post-pénalité strictement identiques à l'AVANT** : tout est bien recalculé au rendu | identique ✓ (miroirs encore écrits, affichage inchangé) |
| **T8** Save forgée `ciment_irradie: 777` dans `g.port[1]`, chargée puis rechargée | **PASS** | clé **ABSENTE** après le 1ᵉʳ chargement, **idempotent** au 2ᵉ, **aucune autre clé de stock touchée** | clé **conservée** (777) aux deux chargements ✓ |
| **T9** Round-trip | **PASS** | les 22 paires ré-appliquées à une copie vierge de la base 382 (22/22 `count=1`) → **identité octet pour octet** avec le fichier livré avant bump (`cmp` silencieux, SHA `c7c2effb…` des deux côtés) | — |

**Le contrôle qui comptait le plus (T5–T7)** : les trois moteurs où des écritures ont disparu rendent un affichage **byte-identique** à la base — la comparaison a porté sur les chaînes entières des fiches, pas sur des extraits. Aucun lecteur caché : le relevé de l'audit 381 était complet.

`pageerrors` : **0 sur les deux fichiers, sur les deux passes.**

## 4. Écarts au brief et raisons

1. **Modèle** : exécuté sur Fable 5 et non Opus 5 (configuration de session). Patch appliqué verbatim, SHA vérifiés à chaque étape — aucune compensation créative.
2. **Résidu étape 4 : 2 lignes et non « une seule »** — la 2ᵉ est le **commentaire D-comment-tour introduit par le patch du brief lui-même** (il nomme `waterNeed`/`waterDrawn` pour dire qu'ils ne sont PAS lus, donc il matche la regex de contrôle). L'étape 4 ne comptait pas sa propre édition D. Zéro occurrence de code, zéro oubli.
3. **`GAME_NOTES` réécrit + commentaire de version ajouté** : non demandés par le brief, mais conventions du projet (le commentaire nomme les champs retirés → il ajoute 2 lignes au grep de résidu sur le fichier final, assumé et documenté au §2).
4. **SHA de blocs** : concordance via la même normalisation du `\n` de tête qu'au lot 1 (convention d'extracteur ; le fichier entier concordait sans normalisation).
5. **Harnais uniquement** (aucun octet du jeu concerné) : astuces + guide désactivés, tutoriel passé par le vrai bouton, tour + tuyau forgés sur l'île 1 (citerne pilotée), porte + fil forgés dans `t.logic`, Collisionneur piloté par `processCollider` réel sous état forgé (ré-affirmation continue, patron 14.58), save T8 injectée par les 3 clés de slot derrière un drapeau `sessionStorage` (patron 14.98).
6. Rien d'autre : aucune ancre retapée, `SAVE_VERSION` inchangé, déclaration de `ciment_irradie` conservée dans les tables (RES_SHORT/RES_TIER/carrier/i18n — la décision est la purge des saves, pas le démontage du registre), pas de `.nojekyll`, pas de PR.

**Constat signalé, hors périmètre, NON corrigé** (interdits §5 : lecture seule hors patch) : la ligne d'avertissement du bloc portes (l.19065) fait `iconLabel(I18N.t("⚠ Face(s) ")) + gwired.map(…)` — **concaténation d'un élément React avec une chaîne** → elle s'affiche « **[object Object]S câblée(s) mais INHIBÉE(S)…** » au lieu de « ⚠ Face(s) S câblée(s)… ». **Préexistant** (mesuré identique sur la base 382 non patchée — c'est même ce qui a validé T6 : l'affichage, défectueux ou pas, n'a pas bougé d'un octet). Cause : `iconLabel` renvoie un nœud React dès que l'emoji de tête a un sprite (`⚠` → `ui_alerte`), il ne doit jamais être opérande d'un `+`. Correctif d'une ligne pour un lot ultérieur : passer les trois morceaux en enfants séparés du `createElement` au lieu de les concaténer.

## 5. Hypothèses [NON VÉRIFIÉ]

- **[NON VÉRIFIÉ]** T5 exerce la branche CITERNE (réseau tuyau isolé) ; la branche PORT du même code restructuré (réseau relié au port) suit le même chemin `wf` mais n'a pas été montée séparément.
- **[NON VÉRIFIÉ]** T8 purge une save forgée minimale ; aucune save réelle de joueur portant du `ciment_irradie` ancien n'a été rejouée (le mécanisme `REMOVED_RESOURCES` est celui de 14.16, déjà éprouvé sur `information_quantique`).
- **[NON VÉRIFIÉ]** Les 31 sites d'appel de `fmtPower` n'ont pas tous été ouverts un à un : T1 couvre la fonction, T2-T4 couvrent 4 lecteurs réels (fiche détaillée ×2, panneau d'amélioration via lot 1, pastille + panneau Énergie) ; les autres passent par le même helper.
- **[NON VÉRIFIÉ]** Le cycle Collisionneur de T7 est piloté sous état forgé (câble/He3 simulés) ; un cycle entièrement « légitime » (vrai réseau électrique + vraie citerne He3) n'a pas été remonté — le chemin de code exercé (fiche + `processCollider` + pénalité) est le même.
