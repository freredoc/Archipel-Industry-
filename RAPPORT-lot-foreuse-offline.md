# RAPPORT — Lot « Foreuse Nord + rattrapage hors-ligne »

**Brief** : `BRIEFlotforeuseoffline.md`
**Livré** : `GAME_BUILD = 376` · `GAME_VERSION = 'Alpha 14.93'` · **`SAVE_VERSION` INCHANGÉ (31)**
**Base** : Alpha 14.92 / build 375 / 3 319 817 o — **exacte au byte près**, conforme au brief.

---

## 0 — ⚠ CHANTIER 1 NON LIVRÉ : les deux PNG ne sont pas joints

Le brief déclare deux « fichiers joints » (`bat_foreuse_n.png`, `anim_bat_foreuse_n.png`).
**Ils ne sont arrivés nulle part** : seul le `.md` du brief a été transmis à la session.

Recherche exhaustive :

| Emplacement | Résultat |
|---|---|
| Pièces jointes de la session | seul `BRIEFlotforeuseoffline.md` |
| Les 7 zips de sprites du dépôt | `bat_foreuse_n.png` présent dans **2** d'entre eux, aucun ne concorde |

| Fichier trouvé | SHA-256 | Taille |
|---|---|---|
| `archipel_textures_v3.2.zip` → `sprites/batiments/bat_foreuse_n.png` | `e14fea1a…` | 274 o |
| `Archipel_sprites_ile6_v2.8.zip` → `sprites/bat_foreuse_n.png` | `0c3db872…` | 356 o |
| **attendu par le brief** | **`61fa71ae…`** | **543 o** |

Le premier (`e14fea1a…`) est **exactement l'art actuellement en jeu** — c'est-à-dire l'art
défectueux, pas le corrigé.

**Aucun art de substitution n'a été fabriqué.** Le test 1.5 exige la concordance SHA-256 avec le
tableau du brief : un art régénéré ne pourrait jamais la produire, et livrer un sprite « qui
ressemble » sous couvert de correction serait inventer un livrable. C'est l'arbitrage déjà retenu
aux lots 14.64 et 14.65, où le pack manquait de la même façon.

### Le défaut est néanmoins caractérisé au pixel (utile à l'auteur de l'art)

Sprite `bat_foreuse_n` re-décodé depuis la data-URL du fichier **en jeu** :

| ligne | attendu (§ du brief) | mesuré |
|---|---|---|
| y20 | bordure sombre continue + socle `#4E525C` en x11..19 | bordure `#161A22` + **pixels de mât** `B8C1CE`/`C2CBD6`/`8A95A4`/`5E6978` en x11..19 |
| y21 | `#FF9628` continu de x5 à x26 | x5..**x9**, puis **trou**, puis x21..**x26** |
| y22 | `#C46618` continu de x5 à x26 | idem |

⚠ **Correction factuelle au brief** : il annonce l'interruption « entre x=11 et x=19 » (9 px). La
mesure donne **x10..x20, soit 11 px**. Le reste du §1 est exact — notamment les **706 pixels
opaques** du test 1.6, retrouvés à l'identique.

Le sprite `bat_foreuse_s` confirme la composition de référence : ses lignes 18-22 sont un corps
plein continu, le foret sortant par un socle en y23-24.

### Ce qui est prêt

Le script `patch_sprite.py` est écrit et validé syntaxiquement. Il attend les deux fichiers et
**refuse d'écrire** si l'un des contrôles échoue :

- SHA-256, taille et dimensions conformes au tableau du brief ;
- silhouette identique (0 écart d'alpha sur 1536 px, 706 opaques) ;
- frame 0 de la sheet == sprite statique, au pixel près ;
- bande orange **continue** sur y21 et y22 ;
- les 2 ancres `window.__SPRITE_DATA__["bat_foreuse_n"]` / `__ANIM_DATA__` à `count == 1` ;
- encodage des **octets du fichier**, jamais ré-encodage de l'image.

Déposer les deux PNG et lancer le script suffit à livrer le chantier 1.

---

## 1 — Chantier 2 : le rattrapage hors-ligne

### Le défaut (§2.3 du brief, confirmé par la mesure)

Le pourcentage se calculait sur **`ticks`** (la durée totale de l'absence) alors que la boucle ne
simule que **`WARM`** ticks avant d'extrapoler. En mode simplifié `WARM = min(ticks, 900)`.

Mesuré sur la base 375, absence de 8 h (28 800 ticks), option « calcul simplifié » active :
la barre **plafonne à 3 %** puis saute à 100 %. Le joueur voit une barre qui n'avance pas.

Afficher `done / ticks` en chiffres, comme le demande le §2.1 pris isolément, aurait **reproduit
le même mensonge en plus visible**.

### Le correctif

La progression se rapporte à ce qui est **réellement simulé**, et l'extrapolation est annoncée à
part :

```
885 / 900 ticks simulés · 27 900 extrapolés
```

En mode complet il n'y a rien à extrapoler, et la ligne se réduit à `1 692 / 1 800 ticks`.

⚠ **`WARM` et `simplify` sont relus à chaque appel de `showOverlay`**, jamais capturés au
démarrage : la bascule anti-gel (`CATCHUP_BUDGET_MS`) redéfinit les deux **pendant** l'échauffon.
Ce sont des `let` de la même fermeture, donc la lecture directe suffit — c'est ce qui garantit que
le total affiché suit la nouvelle valeur au lieu de sauter.

⚠ **Seuil de format imposé** : `fmtInt(n, Infinity)`. Sans argument, `fmtInt` suit la préférence
« Grands nombres » du joueur, qui descend jusqu'à 1 000 → **28 800 s'afficherait `2,88e4`**. Un
nombre de ticks est une **durée**, pas un stock : il garde toujours le séparateur de milliers.
Le brief signalait `formatCost` comme piège ; celui-ci ne l'était pas.

`OVERLAY_AFTER_MS = 180` est **conservé** : aucun overlay sur une absence courte.

### Sites patchés

| # | Ancre | `count` avant | Taille |
|---|---|---|---|
| A | `const showOverlay = pct => {` … `};` | 1 | 196 → 1 213 o |
| B | `showOverlay(skipped ? 100 : Math.min(99, …));` | 1 | 80 → 114 o |
| C | bloc JSX `.catchup-note` (extrait du fichier, **pas retapé**) | 1 | 202 → 530 o |
| D | `.catchup-note{` (CSS) | 1 | 14 → 122 o |
| E | bloc d'augmentation i18n (3 clés × en/es/de) | — | ajouté |

---

## 2 — Tests

| # | Montage | Attendu | Mesuré | |
|---|---|---|---|---|
| 2.1 | Absence courte (~72 s) | aucun overlay | **aucun overlay**, 78 ticks rattrapés — le différé de 180 ms est intact | **PASS** |
| 2.2 | 30 min, mode complet | ticks affichés, total réel, barre à 100 % | `1 692 / 1 800 ticks`, **6 échantillons tous à `/ 1 800`**, aucune mention d'extrapolation, max **94 %** | **PASS** |
| 2.2b | invariant | `pct == round(100 × faits / simulés)` | **0 écart / 6** | **PASS** |
| **2.3** | **8 h, mode simplifié** | progression rapportée à `WARM`, extrapolés annoncés, **barre qui ne stagne pas** | **`885 / 900 ticks simulés · 27 900 extrapolés`**, pct max **98 %** | **PASS** |
| 2.3b | invariant | `pct == round(100 × faits / simulés)` | **0 écart** | **PASS** |
| **2.4** | Bascule automatique | total suivant la nouvelle `WARM`, sans saut | **28 800 sans bascule / 900 avec**, pour la MÊME absence (voir ci-dessous) · invariants vérifiés sur **359 relevés** · 0 recul | **PASS** |
| 2.5 | Bouton « Passer » | comportement inchangé | overlay fermé, main rendue | **PASS** |
| 2.6 | 4 langues | libellé traduit, séparateurs corrects | fr `900 ticks simulés · 27 900 extrapolés` · en `ticks simulated · extrapolated` · es `simulados · extrapolados` · de `Ticks simuliert · extrapoliert` — **0 chaîne française résiduelle** | **PASS** |
| 2.7 | Comparaison de nombres formatés | U+202F / U+00A0 / U+2009 normalisés | normalisation appliquée dans le harnais | **PASS** |
| **2.8** | **Rejouer 2.3 sur la base 375** | doit **échouer** | **aucune ligne de ticks**, barre plafonnée à **3 %** | **PASS** (échoue bien) |

Le **2.8** est ce qui rend 2.3 falsifiable : même montage, même absence, même option — la base
plafonne à 3 % sans afficher un seul tick, le build 376 monte à 98 % en annonçant les 27 900 ticks
extrapolés.

### ⚠ 2.4 : la transition de `WARM` n'est pas observable à l'écran — et la comparaison le prouve mieux

La bascule anti-gel se déclenche quand la durée projetée dépasse `CATCHUP_BUDGET_MS`. Deux régimes,
et **aucun des deux ne montre la transition** :

- **horloge accélérée** : le budget est franchi dès la 1ʳᵉ tranche, donc **avant** les 180 ms de
  différé de l'overlay → l'écran n'affiche jamais la valeur d'avant bascule ;
- **temps réel** : la machine simule 28 800 ticks bien en deçà des 90 s du mode complet → la
  bascule **ne se déclenche jamais**.

La propriété à prouver n'est de toute façon pas « on voit le total changer », c'est « **le total
affiché est la valeur COURANTE de `WARM`** ». Deux runs à absence et mode identiques (8 h, complet),
ne différant que par l'horloge, la démontrent :

| run | bascule | total affiché |
|---|---|---|
| temps réel, 99 relevés | non | **28 800** |
| horloge ×120, 260 relevés | oui | **900** |

Une implémentation qui capturerait `WARM` au démarrage afficherait **28 800 dans les deux cas**.
S'y ajoute l'invariant `total affiché + extrapolés == durée de l'absence`, vrai sur **les
359 relevés** des deux runs.

⚠ **Piège de harnais, coûteux** : à 120 ms d'échantillonnage, le rattrapage de 8 h se termine
entre deux relevés → **1 seul échantillon** capté, et une assertion portant sur « le dernier
échantillon » mesurait un instantané au hasard (3 faux KO). Corrigé en échantillonnant à 25 ms et
en asserant sur un **invariant vrai à chaque relevé** (`pct == round(100 × faits / simulés)`)
plutôt que sur une valeur finale.

---

## 3 — Contrôles d'intégrité

| | |
|---|---|
| Ancres | **4** de code + 1 bloc i18n, toutes à `count == 1` avant écriture |
| `node --check` | **7 blocs / 7 OK**, éditions publique **et** dev |
| Taille (`os.path.getsize`) | 3 319 817 → **3 321 797 o**, delta **+1 980 o** |
| `SAVE_VERSION` | **31**, inchangé — aucun champ de partie |
| Hors périmètre | `OFFLINE_MAX_TICKS`, `MIN_WARM`, `SAMPLE`, `CATCHUP_BUDGET_MS`, `OVERLAY_AFTER_MS`, le mécanisme d'extrapolation : **intouchés** |

### Non-régression

| Lot | Suites | Résultat |
|---|---|---|
| 14.89 « UI & Port » | `t1_port` `t23` `t4` `t35` `t6` `t7` `ttuto` | **58 PASS / 0 KO** |
| 14.90 « Gisements » | `tg` `tg2` | (inclus ci-dessus) |
| 14.91 « Recherche par livraison » | `tstatic` `tA` `tB` `tD` | **64 PASS / 0 KO** |
| 14.92 « Halos d'antenne » | `t75_78` (base 375 ↔ build 376) | **3 PASS / 0 KO** — série de zone identique, redessins 595 → 597 sur 20 s |

**Boot des deux éditions** : canvas **100 %** peint (2 802 / 2 802 et 2 672 / 2 672), **0 `tickError`**,
**0 erreur console**, `build 376 · Alpha 14.93`.

---

## 4 — Écarts au brief

| # | Écart | Justification |
|---|---|---|
| 1 | **Chantier 1 non livré** | Les deux PNG ne sont joints nulle part. Aucun art de substitution : le test 1.5 impose la concordance SHA-256. Script de pose prêt. |
| 2 | Le trou de la bande orange fait **11 px (x10..x20)**, pas 9 (x11..x19) | Mesuré au pixel sur l'art en jeu. Sans effet sur le correctif attendu. |
| 3 | `fmtInt(n, Infinity)` au lieu de `fmtInt(n)` | Sans seuil imposé, la préférence « Grands nombres » du joueur écrirait 28 800 en `2,88e4`. |
| 4 | `showOverlay` ne prend plus de paramètre | Elle calcule le pourcentage elle-même, à partir de `WARM` **relu au moment de l'appel**. Le §2.2 laissait le choix (« lui passer aussi les ticks, ou lire `done`/`ticks` par fermeture ») ; la seconde voie est la seule qui garantisse la fraîcheur de `WARM` après bascule. |
