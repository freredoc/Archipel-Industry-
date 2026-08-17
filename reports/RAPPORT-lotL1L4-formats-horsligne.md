# RAPPORT — Lot L1 (formatage des nombres) + L4 (rattrapage hors-ligne)

Brief : `BRIEFlotL1L4formatshorsligne` · patcheur `patch_L1L4.py` (pré-compilé, fourni)
Branche : `claude/temps-souterrain-display-uoonrz` — **PR ouverte, NON mergée** (le merge appartient à Ethan).

## Version produite

| | |
|---|---|
| `GAME_BUILD` | 386 → **387** |
| `GAME_VERSION` | Alpha 15.3 → **Alpha 15.4** |
| `SAVE_VERSION` | **31, INCHANGÉ** (retrait de champ, précédent `information_quantique`) |
| Taille | 3 366 638 → **3 371 093** o (**+4 455**) |
| SHA-256 livré | `7f615b11868c0320641f5ca20216adc4c6a94506b1126dcd1e9f40450dfc787c` |

Le delta est supérieur aux +79 o du brief : s'y ajoutent le bump, `GAME_NOTES`, le bloc de
commentaire cumulatif (obligatoire par convention projet) et les 4 sites de la demande joueur.

## Sortie du patcheur

Base vérifiée `3d9ce553…` = build 386 exactement (aucun avertissement).

```
OK - 41 ancres appliquees
SHA-256 fichier patche : bd38e7b7bbf10f4e37b33654f758b6e5184c3c73f5eb37b79599f315141e2b66
```

**Conforme au caractère près** à l'attendu du brief (`bd38e7b7…`) : les 41 ancres étaient toutes à
`count == 1`, aucune adaptation.

## SHA-256 des 7 blocs `<script>` (ré-extraits du fichier livré)

| bloc | octets | sha256 | vs brief |
|---|---|---|---|
| 1 | 418 | `a50c1c4e7f4a304c650c0cfa7e06c4ffdfb0d87510d83738114d4dd5a1641628` | identique |
| 2 | 4 397 | `8fbb22187703339c146b2f82badd8701d199128cd3d9dd8673d213c8616ca541` | identique |
| 3 | 10 751 | `d949f1c3687aedadcedac85261865f29b17cd273997e7f6b2bfc53b2f9d4c4dd` | identique |
| 4 | 131 835 | `35f4f974f4b2bcd44da73963347f8952e341f83909e4498227d4e26b98f66f0d` | identique |
| 5 | 1 112 066 | `6066e8c1aeb44929ec5e92d946a936e70451d0c76d7f1375fae084200c308720` | identique |
| 6 | 239 836 | `f6cdea55af87528fdb0faa6d73d1cf2367219509802364d6d0226252832c73d9` | identique |
| 7 | 1 618 423 | `1d17e9d33773864e1f28260de6366414d4eee3554f7f39fc91da2ac0bfbe3e01` | **écart attendu** |

**Blocs 1 à 6 byte-identiques au brief** — preuve directe que le patch ET les ajouts de séance ne
touchent que le bloc 7. L'écart du bloc 7 (`f20d7091…` au brief) est **assumé et explicable** : il
porte `GAME_BUILD` / `GAME_VERSION` / `GAME_NOTES` / le commentaire cumulatif — que le brief laisse
explicitement au moment de l'exécution — plus les 4 sites de la demande joueur ci-dessous.

`node --check` : **7 blocs, 7 OK**.

## Demande joueur traitée dans la même séance

> « pour le temps souterrain afficher 4min55 plutôt que 4min »

`fmtDur` (créée par le lot) tronquait à la minute entre 1 min et 1 h — le brief le signalait comme
choix de place assumé. Les **secondes sont ajoutées**, et l'écriture passe **compacte, sans espaces**,
sur les 4 rangs : `45s` / `4min55` / `4h50` / `3j08h`.

**La forme compacte n'est pas un caprice typographique, elle tient à la place.** Le décompte est
centré sur UNE tuile (26 px au zoom mini). Mesuré au rendu réel (DM Mono), en largeurs de tuile :

| forme | exemple | largeur |
|---|---|---|
| compacte livrée, pire cas | `59min59` | **2,15** |
| forme du brief, pire cas | `3 j 08 h` | 2,46 |
| forme espacée avec secondes | `59 min 59` | 2,77 |
| compacte, rang jour | `3j08h` | **1,54** |
| avant le lot (secondes brutes) | `289290s` | 2,15 |

Le décompte **déborde donc moins qu'avec la forme du brief tout en portant les secondes**.

⚠ **Une valeur que j'avais d'abord écrite dans le commentaire du code était une estimation, pas une
mesure** (« 1,15 tuile ») ; elle a été remplacée par les chiffres relevés ci-dessus.

### 3 sites HORS BRIEF alignés (même défaut, non traité par le lot)

Les fiches imprimaient encore des **secondes brutes** — `~289290s` pour 3 j 08 h — dans un panneau où
la place n'est pourtant pas contrainte : chantier souterrain (`speedReason`), ligne « 🚧 Travaux »,
et opération de forage. Les trois passent par `fmtDur` : carte et fiche disent enfin la même chose.
`fmtDur` a désormais 4 appelants ; 0 secondes brutes restantes.

## Résultats de validation (tous rejoués, aucun repris du brief)

Banc : Chromium 1194 headless, serveur HTTP depuis la racine du dépôt, viewport 420×900.
Save forgée en séance (partie neuve + mine de fer reliée au port par route ⇒ flux réel de
**1 minerai_fer/tick**), `savedAt` antidaté, réinjectée par `addInitScript` derrière un drapeau
`sessionStorage` (piège 14.59). `_catchUpStats` lu via `window.__gameRef.current` — **aucune copie
de banc instrumentée n'a été nécessaire** : le fichier testé est celui de la PR.

| # | test | résultat | mesure |
|---|---|---|---|
| V1 | Boot | **PASS** | build 387 / Alpha 15.4 / SAVE 31, canvas peint **100 %**, horloge qui avance, **0 `pageerror`** |
| V2 | Absence courte non extrapolée (−12 min) | **PASS** | `{ticks:720, warm:720, simulated:720, approx:false, capped:false}` |
| V3 | Absence longue extrapolée (−24 h) | **PASS** | `{ticks:86401, warm:1000, simulated:1000, sampleFrom:0, approx:true, capped:false}` |
| V4 | Plafond (−40 h) | **PASS** | `{ticks:100000, capped:true}` |
| V5 | Récap du plafond | **PASS** | « ⏳ Absence plafonnée : 27 h 47 min créditées (maximum). Au-delà, la production n'est plus comptée. » |
| V6 | Compteurs de flux extrapolés | **PASS** | voir ci-dessous |
| V7 | Formateurs | **PASS** | `2,5 TJ` · `2,5 PJ` · `5,68 / 5,68 TWh` · `1 / 1 PWh` · `250 W` · `3j08h` · `4h50` |
| V8 | Option disparue | **PASS** | `grep -c simplifyOffline` = **0**, `toggleSimplifyOffline` = **0** |
| V9 | Non-régression d'échelle | **PASS** | `fmtHeat(2500)` = `2,5 GJ` (la branche GJ n'est pas avalée par TJ) |
| — | Demande joueur | **PASS** | `fmtDur(295)` = **`4min55`**, dessiné sur la carte (espion `fillText`) ET en fiche : « en construction · 51% · ~4min55 » |

Bornes de `fmtDur` vérifiées une à une : `45s · 59s · 1min00 · 59min59 · 1h00 · 23h59 · 1j00h`,
`fmtDur(0)` = `0s`, `fmtDur(-5)` = `0s`.

### V6 — la preuve la plus solide du lot, avec contre-épreuve

Deux mesures indépendantes, chacune contre la base 386 **non patchée**, même montage :

| grandeur | base 386 | build 387 |
|---|---|---|
| `techTree.produced.minerai_fer` après 24 h | 28 812 | **86 413** |
| ticks réellement crédités | 28 800 (8 h, **tronqué en silence**) | **86 401** |
| note affichée au joueur | *aucune* | ligne « Absence plafonnée » quand le plafond mord |
| `colliderConfirms` extrapolées (6 injectées pendant l'échauffon) | **0** (figé) | **+512** |

La base tronquait donc une absence de 24 h à 8 h **sans le dire** — exactement le symptôme que le lot
ferme. Le test est falsifiable dans les deux sens.

## Écarts par rapport au brief, et leurs raisons

1. **`fmtDur` : secondes ajoutées + écriture compacte sur les 4 rangs.** Demande explicite du joueur
   en séance. Justifié par mesure : plus d'information pour moins de largeur (2,15 contre 2,46).
2. **3 sites de fiches alignés sur `fmtDur`** (hors périmètre du brief). Même défaut d'affichage que
   celui que le lot corrige sur la carte, correction d'une expression chacun, risque nul, et
   incohérence carte/fiche évitée.
3. **Bloc 7 différent du SHA du brief** : attendu, il porte le bump et `GAME_NOTES` (le brief laisse
   le numéro au moment de l'exécution).
4. **Rien d'autre.** La colonne « Ressource » du panneau Production reste **intacte** (exclue par
   Ethan). `SAVE_VERSION` non bumpé.

## Points signalés, NON corrigés (conformément au brief)

- `labelOf` arrondit à la minute : le plafond s'annonce « 27 h 47 min » pour 100 000 s
  (27 h 46 min 40 s). Voulu.
- Trois chaînes nouvelles ne sont pas traduites (repli français dans les 4 autres langues) — elles
  rejoignent le lot i18n ouvert de l'audit 381.
- **`techTree.produced` extrapolé rend des nœuds de recherche confirmables pendant l'absence**, alors
  que `finishCatchUp` vide `researchReady`/`researchNotify` juste après. **Constat de séance** : sur
  la save de test, `produced.minerai_fer` passe de 11 à 86 413 pendant l'absence — un nœud dont la
  condition tomberait dans cet intervalle serait donc satisfait sans que la pastille soit armée au
  retour. À traiter au **lot L6** (« notification quand on peut dépenser un point de recherche »),
  comme prévu par le brief ; rien corrigé ici.
- Superposition des étiquettes de décompte quand deux chantiers souterrains sont adjacents : connu,
  rendu canvas, objet du lot L2.
- **Constat de banc, en faveur du diagnostic du brief** : sur la base 386, un rattrapage de 24 h
  bascule en `approx:true` avec `warm:900, sampleFrom:600` **alors que l'option « simplifié » était
  désactivée** — le filet anti-gel tombait bien tout seul, et l'ancienne fenêtre de mesure ne faisait
  que 300 ticks. C'est précisément ce que le régime unique supprime.

## Pièges de banc rencontrés

- Le tap canvas est **avalé** tant qu'une astuce est ouverte et que l'inventaire est déplié (il se
  pose en superposition sur le haut du canvas) : purger `.tip-ok`, replier l'inventaire, passer le
  tutoriel, puis re-tester `elementFromPoint === CANVAS` avant de cliquer.
- Après recentrage manuel de la caméra, `clampPan` recale : refaire le trajet inverse de
  `pointerToTile` et relire, sinon le tap rate la tuile.
- `grep -c TIER_LABEL` rend 4 après le patch : ce sont des **`RES_TIER_LABEL`** (séparateurs de tiers
  de l'inventaire, intacts) — faux positif, le `TIER_LABEL` de l'en-tête de fiche a bien disparu.
