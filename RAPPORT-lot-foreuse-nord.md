# RAPPORT — Lot « Foreuse Nord » (chantier 1)

**Brief** : `ADDENDUMforeusenord1.md` (remplace intégralement le chantier 1 de `BRIEFlotforeuseoffline.md`)
**Livré** : `GAME_BUILD = 377` · `GAME_VERSION = 'Alpha 14.94'` · **`SAVE_VERSION` INCHANGÉ (31)**
**Base** : Alpha 14.93 / build 376 — le chantier 2 a été livré entre-temps, la base a donc avancé
de 375 à 376. Les 2 ancres re-vérifiées, **`count == 1`** chacune, aucune adaptation.

**Aucune ligne de JS modifiée** : remplacement de données d'image pur.

---

## 0 — ⚠ Le zip joint contient l'ANCIEN art, pas le corrigé

Deux pièces sont arrivées : l'addendum (avec les images **inlinées en base64**) et
`foreusenord.zip`. **Les deux ne disent pas la même chose.**

| Source | `bat_foreuse_n` | feuille |
|---|---|---|
| base64 de l'addendum | `61fa71ae…` 543 o | `a63da1ac…` 751 o |
| `foreusenord.zip` | **`e14fea1a…` 274 o** | **`db3839d5…` 370 o** |
| art alors EN JEU (build 376) | `e14fea1a…` | `db3839d5…` |

Le zip livre donc **exactement l'art défectueux déjà en place**. Son `LISEZ-MOI.md` le dit
lui-même : *« bat_foreuse_n — **extrait de la base**. Base : Alpha 14.88 · GAME_BUILD = 371 »*.
C'est l'export de diagnostic qui a servi à **produire** le correctif, pas le correctif.

Appliquer le zip aurait réécrit le défaut sur lui-même, sans que rien ne le signale — le contrôle
SHA-256 du §1.1 l'attrape (`e14fea1a` ≠ `61fa71ae`). **Ce sont les base64 de l'addendum qui font
foi**, et l'addendum l'annonce (« Fichiers joints : AUCUN — brief autosuffisant »).

---

## 1 — Ce qui a été posé

| Ancre | `count` avant | Cible |
|---|---|---|
| `window.__SPRITE_DATA__["bat_foreuse_n"]` | **1** | sprite statique 32×48 |
| `window.__ANIM_DATA__["bat_foreuse_n"]` | **1** | feuille 128×48, 4 frames |

Les octets décodés du base64 sont écrits tels quels — **aucune ré-ouverture / ré-écriture PNG**,
qui aurait changé le SHA-256 et pu aplatir la transparence en palette.

`"bat_foreuse_n":` (à `count == 2`) n'a **pas** été touché : ce sont les déclarations `ANIM_META`,
correctes par construction (la dernière écrase, `{fw:32, fh:48, frames:4, fps:8}`).

Le script de pose **refuse d'écrire** si l'un des contrôles échoue ; les 6 ont passé avant écriture.

---

## 2 — Tests

| # | Attendu | Mesuré | |
|---|---|---|---|
| 1.1 | SHA-256 des base64 décodés | `61fa71ae…` 543 o (32×48) · `a63da1ac…` 751 o (128×48) — **conformes** | **PASS** |
| 1.2 | SHA-256 re-décodés **depuis le fichier patché** | **identiques** — aucune corruption au passage | **PASS** |
| 1.3 | y21/y22 orange continu x5..x26, 0 pixel étranger | vérifié sur le statique **et les 4 frames** : `x5..x26`, **0 trou** | **PASS** |
| 1.4 | 706 pixels opaques | **706** | **PASS** |
| 1.5 | frame 0 == sprite statique | **0 px d'écart** | **PASS** |
| 1.6 | 0 écart d'alpha ancien / nouveau | **0 sur 1536** | **PASS** |
| 1.7 | Île 7, foreuse Nord à l'arrêt : bande continue | **4 images réellement dessinées contrôlées au pixel** : `x5..x26`, **0 trou** | **PASS** |
| 1.8 | En creusement : bande continue sur les 4 frames | feuille `bat_foreuse_n` utilisée, offsets source 0/32/64/96 parcourus | **PASS** |
| 1.9 | S, O, E inchangées | statique **et** anim **byte-identiques** à la base, et chacune dessine sa clé propre | **PASS** |
| **1.10** | **Rejouer 1.3 sur la base → doit échouer** | **trou x10..x20 = 11 px** sur y21 et y22 | **PASS** (échoue bien) |

Le **1.10** confirme la mesure corrigée de l'addendum : **11 px (x10..x20)**, et non les 9 px
(x11..x19) du brief d'origine. L'addendum en donne la raison — x11..x19 est l'étendue du **foret**,
le trou dans l'orange y ajoute les deux pixels de bordure `#161A22` en x10 et x20.

### ⚠ 1.7 : une foreuse à l'arrêt affiche l'ANIMATION, pas le sprite statique

Ma première formulation asserait « c'est le sprite statique qui est dessiné » — **faux KO**.
Sondé : une foreuse posée a `active === undefined` (elle n'est jamais tickée inactive), donc le
rendu passe par `drawAnimFrame` et dessine la **feuille**. Comportement **préexistant**, sans
rapport avec ce lot.

Le critère qui compte n'est pas « quelle clé », c'est « **la bande orange est-elle continue sur ce
qui est réellement dessiné** ». Le test re-décode donc chaque image effectivement passée à
`drawImage` et lit ses pixels : **4 images, `x5..x26`, 0 trou**. C'est une assertion de bout en
bout qu'on ne peut pas satisfaire par accident.

---

## 3 — Contrôles d'intégrité

| | |
|---|---|
| Ancres | **2**, à `count == 1` avant écriture |
| `node --check` | **7 blocs / 7 OK**, éditions publique **et** dev |
| Taille (`os.path.getsize`) | 3 321 797 → **3 322 621 o**, delta **+824 o** |
| `SAVE_VERSION` | **31**, inchangé |
| JS modifié | **aucune ligne** — données d'image seules |

**Non-régression** : suites des lots 14.89 / 14.90 rejouées sur ce build — **58 PASS / 0 KO**.
Boot des deux éditions : canvas **100 %** peint (2 802 / 2 802 et 2 672 / 2 672), **0 `tickError`**,
**0 erreur console**, `build 377 · Alpha 14.94`.

---

## 4 — Écarts au brief

| # | Écart | Justification |
|---|---|---|
| 1 | Base **376**, pas 375 | Le chantier 2 a été livré entre-temps. Les 2 ancres re-vérifiées à `count == 1`, aucune adaptation. |
| 2 | **Le zip joint est ignoré** | Il contient l'art de la base (extrait du build 371), pas le correctif — son propre `LISEZ-MOI` le dit. Les base64 de l'addendum font foi. |
| 3 | 1.7 assertionne les **pixels dessinés**, pas la clé | Une foreuse au repos a `active === undefined` et affiche la feuille d'animation, pas le statique (préexistant). Asserter sur la clé donnait un faux KO ; asserter sur les pixels prouve le critère visuel réel. |
