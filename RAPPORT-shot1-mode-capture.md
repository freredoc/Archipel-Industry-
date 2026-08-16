# RAPPORT — LOT SHOT-1 : mode capture (build dev)

Brief : `BRIEFshot1modecapture.md` · patcheur `patcher_shotmode.py` (fourni pré-exécuté)

---

## 1. Version livrée

| | |
|---|---|
| `GAME_BUILD` | **427** |
| `GAME_VERSION` | **'Alpha 19.4'** |
| `SAVE_VERSION` | **31, INCHANGÉ** |
| `GAME_NOTES` | `Outil interne de capture d'ecran, reserve a la version de developpement. Rien ne change dans la partie.` |

**426 est le maximum relevé sur les 64 branches distantes**, pas seulement sur `main` — 427 était
donc libre. Une ligne a été ajoutée au bloc de commentaire cumulatif au-dessus de `const GAME_BUILD`
(24 lignes), aucune ligne antérieure effacée.

`SAVE_VERSION` n'est pas concerné : le mode capture n'écrit aucun champ, ni dans `g.ui`, ni ailleurs.

---

## 2. ⚠ ÉCART DE BASE — le brief vise 425, la base réelle est 426

Le brief déclare pour base le build **425 / Alpha 19.2** (`054a5c1a…`, 3 713 985 o). Entre sa
rédaction et son exécution, **le lot PWA-1 a été fusionné** : `main` porte désormais le build **426 /
Alpha 19.3**.

| | brief | réel |
|---|---|---|
| base | 425 / 19.2 · `054a5c1a…` · 3 713 985 o | **426 / 19.3 · `6eeaad90…` · 3 725 662 o** |
| patché (avant bump) | `64679432…` · 3 715 025 o | **`b41773dc…` · 3 726 702 o** |
| delta du patcheur | +1 040 o | **+1 040 o — identique** |

**Les deux SHA-256 du brief ne peuvent donc pas correspondre, et c'est attendu.** Ce qui compte, et
qui est vérifié : **le delta est EXACTEMENT celui annoncé (+1 040 o)**, et les **12 ancres sortent
toutes à `count == 1` sur la base réelle** — le lot PWA-1 n'en a perturbé aucune (contrôlé par un
passage à blanc comptant chaque ancre AVANT d'écrire quoi que ce soit).

Un commit d'Ethan (`37890a6`, `sprites_hud_compact.zip`) est présent sur `main` par-dessus le merge :
il ne touche pas le HTML et n'entre pas dans ce lot.

### Empreintes livrées

```
base    3 725 662 o   sha256 6eeaad9072172b800fc39e7343d79207ca0035d31dc09b92ac498d0fe9ae97b0
patché  3 726 702 o   sha256 b41773dcc7c9db2c9a27832b9b1371102aabd34547632b8444d45ea668756f4c   (patcheur seul, +1 040 o)
livré   3 729 003 o   sha256 12a58875dd3c86596d32f364c64a608892a0119c22223316788f3ad7775fa754   (+ bump + commentaire)
```

**Aller-retour** : patcheur rejoué sur la base propre → `cmp` **identique octet pour octet**.
**Idempotence** : rejeu sur le fichier patché → `DEJA PATCHE - aucune modification`, taille inchangée.

---

## 3. Les 12 ancres

Le patcheur échoue bruyamment si une ancre n'est pas à `count == 1`. Passage à blanc préalable sur la
base réelle **et** exécution : les 12 sont sorties à 1, dans l'ordre.

| Ancre | Cible | count |
|---|---|---|
| A1 | déclaration du state, après `const [dev, setDev]` | 1 |
| A2 | `toggleShotMode()` avant `function toggleLevels()` | 1 |
| A3 | signature du Toolbar (`+ shotMode`) | 1 |
| A4 | garde du chrono → `DEV_BUILD && !shotMode` | 1 |
| A5 | props du Toolbar → `shotMode: shotMode` | 1 |
| A6 | bandeau `.status` → `!shotMode &&` | 1 |
| A7 | `.dev-banner` → `dev && !shotMode &&` | 1 |
| A8 | `logiclayer-btn` → `!invOpen && !shotMode &&` | 1 |
| A9 | `underground-btn` → `+ !shotMode &&` | 1 |
| A10 | signature de `OptionsModal` | 1 |
| A11 | ligne d'option, après « Mode développeur » | 1 |
| A12 | props de `OptionsModal` depuis App | 1 |

Après application : **16 occurrences de `shotMode`** dans le fichier ; **0 sur la base non patchée**
(contrôle du brief, retrouvé).

---

## 4. `node --check` — 7/7

7 blocs `<script>` (comptés via `(?m)^<script`), **7 OK**, rejoué **après le bump**.

Et sur les **trois variantes que la CI dérive** (simulation locale des `sed`/`grep` d'`android.yml`) :
`game-public.html` **7/7** · `game-dev.html` **7/7** · `game-store.html` **7/7**.

Invariants CI préservés : `ko-fi` = **1** en publique / **0** en magasin, `SELF_UPDATE = true`
présent en publique et absent en magasin, gardes d'entrée et contre-gardes comprises.

### Vérification RÉELLE en CI — run 557

La simulation locale a été confirmée par un **`workflow_dispatch` sur la branche** (run **557**, sha
`a9a5408`) : **succès complet**, 1 min 54.

| Étape | Conclusion |
|---|---|
| 6 · `Prepare game files (public + dev + magasin)` | **success** — gardes d'entrée/sortie et compteurs `ko-fi` passent pour de vrai |
| 8-10 · builds PUBLIC / DEV / STORE (`.aab` + APK de contrôle) | success |
| 11 · `Assert store package` (paquet, targetSdk, permissions, debuggable) | success |
| 12 · `Assert appIds` · 13 · `Show signing certificate` | success |
| 17 · `Publish to "apk-latest" release` | **SKIPPED** |
| 18 · `Sync version.json from the game's GAME_BUILD` | **SKIPPED** |

**Les deux étapes à effet de bord ont bien été sautées** (gate `refs/heads/main`) : `main` est resté
sur `37890a6`, `GAME_BUILD = 426`, `version.json` en build 426 — **rien n'a été publié**, vérifié
après coup.

⚠ Ce run prouve que la chaîne construit, assemble et signe les trois paquets avec ce HTML. **Il ne
prouve rien du rendu à l'écran** — le mode capture n'est de toute façon atteignable que sous
`DEV_BUILD = true`, donc dans aucun des trois paquets produits par la CI.

⚠ `shotMode` apparaît 14 fois dans `game-store.html` : le code **existe** dans la variante magasin
mais y est **inatteignable** (`DEV_BUILD = false` → `toggleShotMode` sort tôt et la ligne d'option
n'est pas rendue). Même arbitrage que `SELF_UPDATE` : un verrou plutôt qu'une ablation, un seul
chemin de code.

---

## 5. `DEV_BUILD` reste à `false` dans le diff — vérifié

```
grep -c '^const DEV_BUILD = false;$' Archipel_industry_alpha-7.html   →  1
```

Les copies `DEV_BUILD = true` utilisées par le banc sont des fichiers **de `/tmp` uniquement**
(`shot_dev.html`, `base_dev.html`), jamais stagés. Le staging s'est fait **par chemins explicites**,
jamais `git add -A`.

---

## 6. Banc — 6 tests + contre-test, 7/7 PASS

Pilote : **playwright-core + le Chromium de l'image**. `puppeteer-core` et `@sparticuz/chromium` sont
absents et `playwright install` est proscrit sur ce projet — même écart de pilote qu'au lot PWA-1,
**pilote seul** : clics souris réels (`page.mouse.click` aux coordonnées du centre de l'élément),
jamais `el.click()`. Servi en HTTP sur `127.0.0.1:8741`, locale forcée `fr`.

| Test | Setup réellement exécuté | Mesure | Verdict |
|---|---|---|---|
| **T1** | boot → « Passer » (clic réel) → Options → interrupteur `.opt-toggle.shot` | `libelle=true idx=10 toast="Mode capture active" interrupteur_actif=true` | **PASS** |
| **T2** | idem + inventaire replié ; relevé avant/après | `avant playclock=1 → après playclock=0`, `hud=1`, `boutons_hud=2` | **PASS** |
| **T3** | mode dev ON, `islandUnlocked[6..7]`, `grantedBuildings += logic_wire`, `switchIsland(6)`, inventaire replié | **AVANT** `status=1 dev=1 logique=1 souterrain=1` → **APRÈS** `0 0 0 0` | **PASS** |
| **T4** | enchaîné sur T3 : mode capture OFF | `playclock=1 status=1 dev=1 logique=1 souterrain=1` | **PASS** |
| **T5** | mode ON → `saveTimer` armé + `visibilitychange`/`pagehide` → rechargement réel | `masqué_avant=true`, après reload `playclock=1` (mode OFF), `"shotMode"` **absent du stockage** | **PASS** |
| **T6** | `DEV_BUILD = false`, reboot | `ligne=false playclock=0` **`status=1`** | **PASS** |
| **CONTRE-TEST** | base **non patchée**, `DEV_BUILD = true` | `ligne=false idx=-1 playclock=1` | **PASS (échoue comme attendu)** |

**Suite rejouée 2 fois, 7/7 les deux fois, sans flottement.**

Falsifiabilité, comme l'exige le brief : T2 asserte **aussi** que `.hud` et ses deux boutons
survivent (un `!shotMode` mal placé masquerait la barre entière) ; T3 mesure les quatre éléments
**présents avant** de les mesurer absents (une assertion « absent » sur un élément jamais rendu ne
prouverait rien) ; T6 vérifie que `.status` **reste** en édition publique — c'est le test qui
attraperait une garde oubliée pénalisant les joueurs.

### ⚠ Deux pièges de banc payés, à ne pas redécouvrir

**(a) L'inventaire est déplié à la création (13.84) et se pose en surimpression.** Les deux pastilles
sont gardées par `!invOpen` : tant qu'il est ouvert, `.logiclayer-btn` et `.underground-btn` sont
**absentes du DOM pour une raison qui n'a rien à voir avec le mode capture**. Première passe : T3
mesurait `logique=0 souterrain=0` *avant* activation → la précondition n'était pas remplie et le test
ne prouvait rien. Remède : replier l'inventaire (`.inv-label-btn`) avant toute mesure.

**(b) « Cliquer deux fois » à cause de `useGhostGuard` est FAUX sur un interrupteur.** La règle
(13.50) veut qu'un premier clic soit avalé tant qu'aucun `pointerdown` interne n'a eu lieu depuis
l'ouverture du panneau. Appliquée littéralement à un *toggle*, elle le **bascule deux fois** et le
ramène à son état de départ — mesuré : toast « Mode capture desactive » et interrupteur inactif,
alors que le patch fonctionnait parfaitement. Remède : **amorcer** le garde par un `pointerdown`
dispatché dans le panneau, puis cliquer **une seule fois**, et **vérifier l'état atteint** (helper
`setToggle` qui réessaie jusqu'à trois fois). C'est ce qui a fait passer le banc de 2/7 à 7/7 sans
qu'une seule ligne du patch ne change.

---

## 7. Écarts au brief, et leurs motifs

1. **Base 426 au lieu de 425** (§2) — le lot PWA-1 a été fusionné entre-temps. Les 12 ancres ont été
   re-vérifiées et sortent toutes à `count == 1` ; le delta d'octets est identique à l'annonce. Seuls
   les deux SHA-256 du brief ne peuvent pas correspondre.
2. **Pilote de banc porté sur playwright-core** — `puppeteer-core` et `@sparticuz/chromium` absents
   de l'image, `playwright install` proscrit. Les montages et assertions sont ceux du brief.
3. **Le banc ne passe pas par une save injectée** mais par un boot réel, tutoriel passé au **clic
   réel** sur « Passer », puis forge des préconditions de T3 via `__gameRef` / `__ui()`
   (`islandUnlocked`, `grantedBuildings`, `switchIsland`). Motif : forger une save complète et valide
   pour n'observer que des visibilités DOM est du risque inutile ; le chemin de rendu testé est
   strictement le même. T5, lui, passe bien par une **sauvegarde réelle et un rechargement réel**.

Le brief a été suivi sur tout le reste : aucun miroir `g.ui`, aucun `scheduleSave`, garde composée
`DEV_BUILD && !shotMode`, **aucune clé i18n ajoutée** pour « Mode capture » (le français s'affiche
dans les quatre langues, ce qui est accepté puisque la ligne n'existe que sous `DEV_BUILD`), chaînes
insérées en **ASCII pur**.

---

## 8. Points restants

- **Aucune capture réellement produite.** Le lot livre l'outil ; composer les scènes et sortir les
  1080×1920 reste à faire sur appareil ou émulateur, avec un build `DEV_BUILD = true`.
- **Conséquence d'usage à ne pas oublier** : les pastilles Logique et Souterrain étant masquées, il
  faut **armer la couche logique et descendre au souterrain AVANT** d'activer le mode capture ; on en
  sort par les Options.
- Le calcul « 215 + 215 ≈ 430 px » du brief (barres système + bloc ACTIONS pour tomber de 2340 à
  1920) est **repris tel quel, non re-mesuré ici** — il vient d'une capture réelle du S25 FE.
- **T4 / T5 / T6 du lot P4** et **T4 du lot P2** restent à exécuter sur appareil ; ils sont antérieurs
  à ce lot et non rouverts par lui.

---

## 9. Livraison

- Branche `claude/playstore-preparation-g0w8vb`, **repartie de `origin/main`** (la PR #399 du lot
  PWA-1 étant fusionnée, l'historique déjà mergé n'a pas été empilé).
- **Nouvelle PR** — la #399 est close, elle ne peut pas porter ce lot.
- **PR ouverte, NON fusionnée** : le merge appartient à Ethan, c'est lui qui déclenche `android.yml`
  et donc la republication de l'APK, d'`index.html` et de `version.json`.
