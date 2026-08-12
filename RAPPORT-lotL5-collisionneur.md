# RAPPORT — Lot L5 (fiche Collisionneur à onglets · recherches expliquées · surcadençage P4/P5)

Brief : `BRIEFlotL5collisionneur` · patcheur `patch_L5.py` (pré-compilé, 12 ancres)
Branche : `claude/temps-souterrain-display-uoonrz`, **repartie de `main`** (la PR #374 a été mergée).

## Version produite

| | |
|---|---|
| `GAME_BUILD` | 391 → **392** |
| `GAME_VERSION` | Alpha 15.8 → **Alpha 15.9** |
| `SAVE_VERSION` | **31, INCHANGÉ** — `overclock` est un champ ADDITIF, aucune migration |
| Taille | 3 401 709 → **3 418 208** o (**+16 499**) |
| SHA-256 livré | `d5807a6f57913c1cbcb1d3f7eca77a3a6900666af52b775c1318409f68e3541f` |

Le brief annonçait +13 907 o pour le patch seul ; s'y ajoutent le commentaire cumulatif (24 lignes,
il porte les quatre avertissements d'architecture) et `GAME_NOTES`.

## Sortie du patcheur

Base vérifiée `60420f44…` = build 391, **aucun avertissement**.

```
OK - 12 ancres appliquees
SHA-256 fichier patche : cba50181c4728f754de2442d2e4a0dfdb2f348bac1f76b0c6eb7aaa4f218bf19
```

**Conforme au caractère près** à l'attendu du brief.

**Contrôle intermédiaire, avant le bump : les 7 blocs étaient TOUS identiques aux SHA du brief, bloc 7
compris** (`a27902161b436944f69b2f8f41d06247f37a18ed8a307d78de91369c8ec6cd7d`, 1 663 163 o) et la taille
tombait sur 3 415 616 o exactement — le patch est appliqué à l'identique, l'écart final vient du seul bump.

## SHA-256 des 7 blocs, ré-extraits APRÈS la toute dernière modification du HTML

| bloc | octets | sha256 |
|---|---|---|
| 1 | 418 | `a50c1c4e7f4a304c650c0cfa7e06c4ffdfb0d87510d83738114d4dd5a1641628` |
| 2 | 4 397 | `8fbb22187703339c146b2f82badd8701d199128cd3d9dd8673d213c8616ca541` |
| 3 | 10 751 | `d949f1c3687aedadcedac85261865f29b17cd273997e7f6b2bfc53b2f9d4c4dd` |
| 4 | 131 835 | `35f4f974f4b2bcd44da73963347f8952e341f83909e4498227d4e26b98f66f0d` |
| 5 | 1 112 066 | `6066e8c1aeb44929ec5e92d946a936e70451d0c76d7f1375fae084200c308720` |
| 6 | 239 836 | `f6cdea55af87528fdb0faa6d73d1cf2367219509802364d6d0226252832c73d9` |
| 7 | 1 665 755 | `6274527c0a35f7bac65e566e7165801e8ae1153cf7b8a9fecb737bb6cda33b53` |

**Blocs 1 à 6 byte-identiques au brief.** `node --check` : **7 blocs, 7 OK**. Empreintes prises
**après** la dernière retouche du HTML (`GAME_NOTES`), fichiers de banc déjà supprimés.

## Montage effectif

⚠ **Je n'ai pas la sauvegarde de fin de partie d'Ethan.** L'état de référence est donc **reproduit
in vivo** via `window.__gameRef` sur une partie neuve : nœuds 38/40/42 (réparations) et 39/41/43
(puzzles) confirmés, `techTree.colliderConfirms = 668919`, `port[6].moteur_quantique = 14714`,
île 6 débloquée, élévateur réparé. Les valeurs numériques attendues par le brief en découlent
directement — et sont **retrouvées au caractère près** (V4, V7, V8).

Banc : Chromium 1194 headless, serveur HTTP **depuis la racine du dépôt**, viewport 880×1750
(420×900 pour les contrôles de mise en page). Copie de banc `BANC_392.html` exposant `setInfo`,
`switchIsland` et `activateOverclock` (portée App, hors de `__ui`) par un **exposeur PARESSEUX**
`window.__H = () => ({…})` — la leçon du lot L2 : un objet dans le corps du composant déclenche une
TDZ et une page blanche que `node --check` ne voit pas. **Les copies de banc ne partent pas dans la
PR**, et leur absence est asservie par un test.

Carburant : réseau tuyau **strictement 4-adjacent** au bloc du Collisionneur, citerne ré-affirmée en
continu (`setInterval` 25 ms) ; `co.powered` figé à `true` par un **getter** (la boucle énergie le
réécrit à chaque tick).

## Validation — 14 assertions sur le patch, 14 PASS, suite rejouée sans flottement

| # | test | résultat | valeurs relevées |
|---|---|---|---|
| V1 | Boot + ouverture de la fiche | **PASS** | 0 `pageerror` · montage `{conf: 668919, pal: 3, effPal: 3, repaired: true}` |
| V2 | Onglets | **PASS** | exactement 3 : `["État","Recherches","Surcadençage"]` |
| V3 | Onglet Recherches — sentinelle | **PASS** | **4 boutons** (et non 5), « Démarrage du Collisionneur » ABSENT, panneau vivant, **0 `pageerror` nouveau** |
| V4 | Prochain point | **PASS** | **« 6,69e5 / 1,28e6 »** · jauge libellée **« 52 % »**, remplissage `width: 52%` |
| V5 | Explications | **PASS** | « …coûte **×2.70** le précédent — **la recherche abaisse ce facteur.** » · effet **« ×2.70 → ×2.69 »** |
| V6 | Onglet Surcadençage | **PASS** | P3 « ✓ actif » **disabled** · P4 « activer… » **actionnable** · P5 « 🔒 palier précédent requis » **disabled** |
| V7 | Mini-écran de confirmation | **PASS** | « irréversible » · **« 16 → 64 /s »** · **« 8,39 GW → 134 GW »** · « ×2 par manche » · **« 4 000 mot.quantique »** · **« 1e5 confirmations ✓ »** · bouton Confirmer actif |
| V8 | Activation | **PASS** | `overclock` **4** · `colliderBoost` **2** · `colliderEffPal` **4** · plafond **134 217 728** · **exactement 4 000** moteurs débités (14 714 → 10 714) |
| V9 | Effet moteur | **PASS** | `he3Need` **64** · `he3Used` **64** · `halt` **null** · `state` `starting` |
| V9b | Onglet État suit le palier effectif | **PASS** | **« P4 (surcadencé) »** et **« 64 /s »** |
| V10 | **Codes intacts** | **PASS** | `collider.palier` vaut **toujours 3** · `colliderPalier` **3** |
| V11 | Pas de retour | **PASS** | P3 « acquis » et P4 « ✓ actif » **tous deux disabled** · `activateOverclock(3)` rend **false** |
| V12 | P5 après P4 | **PASS** | **actionnable** mais affiche **« 🔒 1e6 conf. requises »** (seuil non atteint) |
| V13 | Persistance | **PASS** | `archipel_slot_…` → `collider.overclock` = **4** |

**V8 et V10 sont bien la preuve centrale** : le surcadençage prend effet sur le plafond électrique,
la soif d'He3 et le boost, **sans toucher** le palier qui pilote l'émission des codes.
**V3 est la sentinelle** annoncée : elle rend l'onglet et compte les boutons — c'est le seul test qui
pourrait attraper un `d.base.toFixed()` non gardé, que `node --check` laisse passer.

### Contre-épreuve — mêmes gestes, mêmes assertions, sur la BASE 391 : 9/9

| | base 391 | patch 392 |
|---|---|---|
| onglets dans la fiche | **aucun** | 3 |
| boutons de recherche | **5**, « Démarrage du Collisionneur » **présent** | **4**, retiré |
| « Prochain point » + jauge | **absents** | présents, 52 % |
| explications | **absentes** | présentes |
| cartes P4 / P5 | **absentes** | présentes |
| `COLLIDER_POWER[4]`, `COLLIDER_HE3[4]`, `colliderEffPal`, `colliderBoost` | **tous `undefined`** | définis |
| `he3Need` | **16** (P3 nominal) | 64 après activation |
| `collider.overclock` | **champ inexistant** | 4, sérialisé |
| ligne Confirmations | **« 6,69e5 / 10 000 »** — le non-sens | **« 6,69e5 »** |

Le montage est **identique des deux côtés** ; neuf verdicts s'inversent. Le lot est falsifiable de
bout en bout.

### Contrôles finaux — 4/4

| test | résultat | mesure |
|---|---|---|
| Boot du **fichier LIVRÉ** (pas le banc) | **PASS** | build **392** / Alpha 15.9 / SAVE **31**, canvas **100 %**, 0 `pageerror`, 0 `tickError` |
| Poignée de banc absente du livrable | **PASS** | `typeof window.__H` = `undefined` |
| Ligne Confirmations, avant/après | **PASS** | base « 6,69e5 / 10 000 » → livré « 6,69e5 » |
| Barre d'onglets **à 420 px** | **PASS** | les 3 onglets à `[22→121]`, `[125→245]`, `[249→398]` dans un panneau `[0→420]` — **aucun débordement, aucun libellé tronqué** |

Le seul bruit console est le **404 préexistant** du serveur de test (ressource PWA absente).

## Écarts par rapport au brief, et leurs raisons

1. **Montage reproduit au lieu de la sauvegarde d'Ethan** (cf. ci-dessus). Les valeurs de référence du
   brief sont retrouvées à l'identique, donc la propriété testée est la même ; mais je ne peux pas
   affirmer avoir rejoué SA partie.
2. **Aucun autre écart.** Les 12 ancres sont appliquées verbatim.

## ⚠ DÉFAUT VISUEL TROUVÉ DANS LE PATCH — mesuré, SIGNALÉ, NON corrigé

L'onglet Surcadençage contient **deux** libellés `.ip-fluxpri-lbl` qui passent à la ligne. **L'un pose
`lineHeight`, l'autre non** — et la classe n'en définit aucun dans la feuille de style :

| libellé | `line-height` calculé | lignes | hauteur | rendu |
|---|---|---|---|---|
| intro « Surcadencer : la machine boit… » | **`normal` = 9 px** pour une police de **9 px** | 3 | 27 px | **les lignes se chevauchent** |
| avertissement « ⚠ Règles inchangées… » | `1.5` = 13,5 px (inline) | 5 | 68 px | correct |

Interligne nul ⇒ les jambages d'une ligne mordent sur la suivante. **Visible à l'écran**, capture à
l'appui. Les deux usages historiques de cette classe (« Répartition du débit », « Ordre de service »)
sont des libellés **courts qui ne passent jamais à la ligne** : le patch est le premier à la faire
déborder, ce qui explique que le défaut n'existait pas avant.

**Remède : un seul mot**, `lineHeight: 1.5` dans le `style` de ce `div` — exactement ce que son frère
deux lignes plus bas fait déjà. **Je n'y ai pas touché** : le brief place la mise en page dans les
points « à signaler, pas à corriger », et modifier le patch romprait la conformité de hash qui est le
contrôle central de la méthode. À ton arbitrage.

## Points signalés, NON corrigés (conformément au brief)

- **33 nouveaux libellés non traduits** (comptés, pas estimés : diff des chaînes `I18N.t` entre base et
  livré) → repli français dans les 4 autres langues. **C'est bien le plus gros apport de chaînes de
  tous les lots livrés jusqu'ici** ; il rejoint le lot i18n ouvert de l'audit 381.
- **P5 est visible mais hors de portée** sur l'état de référence : 1e6 confirmations requises pour
  668 919 acquises (**66,9 %**). Mesuré, conforme au calibrage voulu.
- Le boost multiplie `co.dcReward` ; les **pénalités ne sont pas multipliées** — surcadencer accélère
  le gain, pas la punition. Choix assumé du brief, vérifié dans le code (une seule ligne touchée,
  celle du `gain`).
- **Aucun garde-fou nouveau** : un déficit d'électricité ou d'He3 arrête la machine et reperd les
  10 min de démarrage, comme avant. La fiche le dit, elle ne l'empêche pas.

## ⚠ MA FAUTE DU LOT PRÉCÉDENT, RÉPARÉE ICI

Mon `git add -A` du correctif L6b a embarqué **`blk1.js` … `blk7.js`** — les 7 blocs `<script>` que
mon outil d'extraction écrit dans le répertoire courant, soit **3,1 Mo de fichiers parasites**, commités
puis mergés sur `main` par la PR #374. Ils sont **supprimés dans ce lot**.

Mon test « le banc n'a pas fui dans la livraison » ne vérifiait que l'absence de `window.__H` **dans le
fichier de jeu** — il ne pouvait pas voir des fichiers voisins ajoutés à l'index. **Règle ajoutée :
lister `git status` avant de commiter et ne jamais utiliser `git add -A` après un banc**, mais des
chemins explicites.

## Pièges de banc payés en séance

- **`innerText` des fiches est en MAJUSCULES** (CSS `text-transform`, piège déjà au mémo en 14.61).
  Six de mes assertions ont échoué à tort à la première passe alors que le contenu était juste — V7
  le montrait de façon flagrante : ses six contrôles de contenu passaient, seul le bouton « Confirmer »
  n'était pas trouvé. **Tout motif de texte doit être insensible à la casse.**
- **`adjacentNetworksFootprint` est en 4 DIRECTIONS** : un tuyau posé en **diagonale** du bloc n'est pas
  vu, `colliderDrawHe3` échoue et la machine tombe en arrêt total He3 — on croit alors le patch inerte
  alors que `he3Need` valait déjà 64. Poser le tuyau strictement en vis-à-vis d'une face du bloc.
- **`typeof` sur un symbole non déclaré rend `"undefined"` sans lever** : une contre-épreuve qui attend
  une `ReferenceError` échoue sur une base où le symbole est bien absent.
- **Normaliser les espaces avec `\s`**, qui couvre U+202F / U+00A0 / U+2009 en JavaScript : une classe
  de caractères écrite à la main ne survit pas forcément à l'écriture du fichier de test, et
  « 10 000 » (espace fine) ne matche alors jamais.
- **Forger l'état ouvre de NOUVELLES astuces** dont le popup recouvre le panneau : purger **après** la
  forge, ou couper `g.ui.tipsEnabled` à la source avant toute capture.
- Reconfirmé : **`useGhostGuard` avale le premier clic** après l'ouverture de la fiche → cliquer deux fois.
