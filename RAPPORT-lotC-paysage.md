# RAPPORT — LOT C : paysage mobile (récupération de hauteur de scène)

Brief exécuté : `BRIEFLOTC.md`. Modèle : **Opus 5**, conformément au §« MODÈLE ET EFFORT ».
Base retéléchargée en tête de session (jamais une copie mémorisée) : `origin/main` @ `af47af1`,
`Archipel_industry_alpha-7.html` en **Alpha 15.1 / build 384**, SHA-256
`ad2f92d74ef15ad02f2a56c18ec3c5e103555223c186f801ac1999ab906d9b31` — soit exactement le fichier
produit par le Lot A, la CI ayant déjà resynchronisé `version.json` / PWA entre-temps.

## 1. Version produite

| | Avant | Après |
|---|---|---|
| `GAME_BUILD` | 384 | **385** |
| `GAME_VERSION` | `Alpha 15.1` | **`Alpha 15.2`** |
| `SAVE_VERSION` | 31 | **31 (inchangé)** |

Le brief ne fixait pas de numéro. `git log --all` vérifié avant de choisir : aucun build ≥ 385 ni
étiquette ≥ 15.2 dans l'historique → 385 / Alpha 15.2, dans la continuité de la séquence.

Convention de commentaire respectée : le bloc cumulatif au-dessus de `const GAME_BUILD` reçoit sa
ligne « build 385 / 15.2 » **sans effacer** les précédentes (vérifiée sur les builds 383 et 384).
`GAME_NOTES` réécrit en texte joueur, UTF-8 littéral, guillemets français uniquement, **aucun `"`**
(le `[^"]*` de la CI le tronquerait).

## 2. Ancre C1 — count constaté

| Ancre | Site | `count()` |
|---|---|---|
| C1 | bloc `@media (max-width:420px)` « Very narrow phones », fin de feuille | **1** |

Le patcher abandonne sur tout `count != 1` avant substitution ; il ne s'est pas arrêté. Aucune ancre
n'a dû être ré-extraite : la base 384 correspondait exactement à celle sur laquelle le brief avait été
pré-compilé (SHA-256 du fichier patché **avant bump** = `1de2887aac14328796f8e4f547be5088c8a15d8b045400898abf0ade9c35ed3e`,
identique au chiffre annoncé au §3 du brief — le patch a donc produit octet pour octet ce qui était prévu).

### Contrôle ajouté : la position en fin de feuille est **vérifiée**, pas supposée

Le brief insiste : la position du bloc *est* le mécanisme (spécificité identique aux règles de base,
c'est l'ordre qui arbitre). Plutôt que de m'en tenir à l'affirmation, j'ai mesuré la feuille après
insertion :

- **0 caractère** de CSS après le bloc Lot C, jusqu'au `</style>` ;
- **0 `@media`** après lui (sur **12** au total dans la feuille).

Le bloc est donc structurellement le dernier — la cascade ne peut pas être défaite par une règle
ultérieure. C'est le contrôle qu'il faudra **rejouer si un lot futur ajoute du CSS** : tout ajout en
fin de feuille passerait devant lui.

## 3. SHA-256 réextraits (fichier final, depuis le dépôt)

7 blocs `<script>`, **7/7 `node --check` PASS** :

| # | octets | SHA-256 |
|---|---|---|
| 1 | 416 | `50efceadfef7efeb1cda224e8ae0f653cc925441676a17f249c6388fd1e4ab9b` |
| 2 | 4 395 | `6820628a9539b3b7425faf5ff3988a756f2b6ca02a40fcc003e6395736145a2f` |
| 3 | 10 750 | `efe1e3ea573b9ea4190a747ed911f79eff89ed85df8a7654c94ef3a2239831fc` |
| 4 | 131 834 | `583039674ce895e6d81c67428b2bd975054c5c8cf7df204481a4385b7f115038` |
| 5 | 1 112 064 | `3b3948d44d1d5de971e33da7a8ac7e0f0638efa61821908c5133cdd75f7a42e9` |
| 6 | 239 834 | `24521d5b971c8e2da112ebf771154fd981ec8d19a3ec3feba21792fce97a3e06` |
| 7 | 1 610 655 | `e7114e057bf1d89c4655924b52844d9e41d6454117faf92deffd577ffcc441fe` |

SHA-256 fichier complet (final, bumpé) : `6b48d23e0222beea58b424a8b5edc8cd52735b42d6975d3a76c7b9c374834757`

**Confirmation blocs 1–6 inchangés :** les 6 hachages sont **identiques, caractère pour caractère**,
au tableau du §3 du brief — et donc aussi à ceux du build 384, le patch ne touchant que le `<style>`.
Vérification en deux temps : (a) après patch CSS **seul**, les **7** blocs correspondaient encore au
tableau du brief (bloc 7 inclus, rien n'ayant été bumpé) ; (b) après le bump, **seul le bloc 7**
change — il porte `GAME_BUILD` / `GAME_VERSION` / `GAME_NOTES`. Aucun patch parasite.
Contrôle final refait sur la copie **relue depuis le dépôt**, pas sur le fichier de travail.

## 4. Delta d'octets

Mesuré en `len(contenu.encode('utf-8'))` **des deux côtés** (jamais en caractères — l'écart serait de
~+33 ko sur ce fichier) :

| | octets |
|---|---|
| Base 384 | 3 355 591 |
| Final 385 | 3 360 646 |
| **Delta total** | **+5 055** |
| — dont patch CSS (mesuré par le patcher, avant bump) | **+1 713** — conforme au §2 du brief |
| — dont bloc de commentaire de version + `GAME_NOTES` | +3 342 |

## 5. Suite de validation

### `landv.js` — 8 formats paysage + 2 portraits témoins

| Viewport | HUD av→ap | tuto av→ap | toolbar av→ap | **SCÈNE av→ap** | gain |
|---|---|---|---|---|---|
| 667×375 (SE) | 108 → **60** | 35 → 27 | 83 → 66 | **149 (40 %) → 222 (59 %)** | **+49 %** |
| 780×360 (S25 FE) | 108 → **60** | 35 → 27 | 95 → 71 | **122 (34 %) → 202 (56 %)** | **+66 %** |
| 852×393 (iPhone 15) | 108 → **60** | 35 → 27 | 95 → 71 | **155 (39 %) → 235 (60 %)** | **+52 %** |
| 915×412 (Pixel 8) | 66 → **60** | 35 → 27 | 95 → 71 | **216 (52 %) → 254 (62 %)** | **+18 %** |
| 1024×600 (tab. petite) | 66 → 66 | 35 → 35 | 95 → 95 | **404 (67 %) → 404** | inchangé |
| 1024×768 (iPad L) | 66 → 66 | 35 → 35 | 95 → 95 | **572 (74 %) → 572** | inchangé |
| 1366×768 (laptop) | 66 → 66 | 35 → 35 | 95 → 95 | **572 (74 %) → 572** | inchangé |
| 1920×1080 (desktop) | 66 → 66 | 35 → 35 | 95 → 95 | **884 (82 %) → 884** | inchangé |
| 360×780 (portrait) | 108 → 108 | 66 → 66 | 79 → 79 | **527 (68 %) → 527** | inchangé |
| 412×915 (portrait) | 108 → 108 | 66 → 66 | 79 → 79 | **662 (72 %) → 662** | inchangé |

Les gains correspondent au tableau §5 du brief **au pixel près** (222 / 202 / 235 / 254), et le HUD
tombe bien à **60 px sur les quatre formats paysage** — confirmant que les deux causes identifiées au
§1 (`.hud-brand` visible entre 780 et 852 px, `.hud-side{width:100%}`) portaient bien le coût.

**Contrôle décisif — les 6 lignes qui doivent être inchangées le sont**, colonne par colonne (HUD,
tuto, toolbar, inventaire, scène, largeur d'onglet) : 4 desktop/tablette + 2 portraits, aucun écart.
Preuve que la borne `max-height:520px` joue et que le bloc n'a pas fui hors de sa media query.

⚠ **Seul écart non listé par le brief, assumé** : en paysage, `btnMax` bouge de **+1 px**
(780×360 : 145→146 · 852×393 : 159→160 · 915×412 : 172→173 ; 667×375 inchangé à 125). C'est la
conséquence directe et attendue du `padding` des boutons d'action passé à `4px 8px` dans le bloc ;
la contrainte du Lot A (≤ 200 px) reste largement tenue. Aucune autre colonne ne bouge.

### `verify2.js` — non-régression du Lot A, suite **inchangée**

La suite fournie est **byte-identique** à celle du Lot A (vérifié par `diff`) : ce n'est pas une
version relâchée pour l'occasion.

- `verify2.js <fichier> 0` (état neuf) → **10/10 PASS**
- `verify2.js <fichier> 12` (12 îles simulées) → **10/10 PASS**

Sur les 10 viewports : aucun débordement hors conteneur défilant, pas de scroll document, canvas
présent, aucune `pageerror`, plancher d'onglet à 26 px, bouton d'action ≤ 200 px. Le Lot C touche des
propriétés voisines de celles du Lot A sur `.hud` et `.tabs-row` — cette suite est ce qui prouve
qu'il ne les a pas défaites.

**Gain supplémentaire non listé par le brief** : dans le cas dégradé « paysage **et** 12 îles »
(780×360), la scène passe de **100 px** (mesure Lot A sur le build 384) à **163 px**. Le compactage
profite donc aussi au scénario que le Lot A avait laissé le plus étroit.

### Boot
Les 4 runs chargent réellement la page dans Chromium headless (pas un simple contrôle syntaxique) :
canvas présent et non vide, aucune `pageerror`, popup d'accueil écarté par **`.tip-ok`** — jamais
`.tip-dismiss`, qui désactiverait les astuces et fausserait toutes les mesures suivantes.

## 6. Écarts au brief et raisons

1. **La PR n'est pas une PR neuve : le Lot C rejoint la PR #369, déjà ouverte.** La session est
   épinglée à la branche `claude/new-session-8itu4m`, et la PR **#369** (complément documentaire du
   rapport du Lot A, `+24` lignes de `.md`) y est **encore ouverte, non mergée**. Pousser le Lot C
   sur cette branche l'ajoute donc mécaniquement à #369. Je n'ai pas réinitialisé la branche : cela
   aurait vidé #369 de son contenu. **Conséquence pour la relecture** : #369 contient désormais
   *deux* changements — le complément du rapport Lot A, puis le Lot C. Son titre et sa description
   ne décrivent plus que le premier ; à mettre à jour, ou à fermer au profit d'une PR neuve.
2. **Impossible d'ouvrir la PR depuis la session.** L'intégration GitHub s'est **déconnectée en
   cours de session** (les outils `mcp__github__*` ne sont plus disponibles). Le §8 du brief demande
   d'ouvrir la PR sans merger ; je livre donc **branche poussée + URL de comparaison**, l'ouverture
   ou la mise à jour de la PR revenant à Ethan. Cela rejoint d'ailleurs le repli que le brief lui-même
   prévoyait.
3. **Aucun merge effectué**, conformément au §8 : le merge sur `main` appartient à Ethan seul, c'est
   lui qui relit avant que la CI ne republie APK, `index.html` et `version.json`.

Aucun autre écart : l'ancre, le SHA du fichier patché avant bump, le delta CSS et les quatre gains de
scène correspondent tous exactement aux valeurs annoncées — patch réellement pré-compilé et vérifié.

## 7. Reliquats ouverts

- **L'inventaire ouvert reste un calque de 130 px** — mesuré identique avant/après sur les 10
  formats. Avant le lot il couvrait la scène entière en paysage (130 px de calque sur 122 px de
  scène) ; après, il en laisse ~72 px visibles grâce aux gains ci-dessus. Le plafonner proprement
  exige une unité de hauteur fiable : **reliquat assumé, hors de ce lot**.
- ⚠ **`vh` / `dvh` sont inutilisables dans ce harnais en paysage émulé** — mesuré : `30dvh` calcule
  **234 px sur un viewport de 360 px de haut**. Une contrainte `max-height` sur `.inventory` avait
  été écrite puis **retirée du lot** pour cette raison : elle n'était pas vérifiable. **Ne pas la
  réintroduire « parce que ça a l'air juste »** — sans unité de hauteur fiable, on ne saurait pas la
  tester.
- **`env(safe-area-inset-top)`** du HUD en paysage : **non testable en headless** (toujours 0). Posé
  pour les builds store ; **à confirmer sur appareil réel à encoche** — non validé ici.
- **Corps de texte encore à 12.5 px fixes** (`html{font-size:12.5px}`) : **Lot B**. Non touché —
  ~1 100 valeurs `px` fixes ne suivraient pas la racine.
- **Plancher tactile des onglets d'îles à 26 px**, sous les ~44 px recommandés : à rediscuter au
  **Lot B**, pas augmenté unilatéralement ici.
- **La toolbar n'est pas passée en barre latérale** et la mise en page paysage n'est pas réorganisée :
  le lot se limite à récupérer de la hauteur par compactage, comme demandé.
- **Contrôle à rejouer** : tout lot futur ajoutant du CSS **en fin de feuille** passerait devant le
  bloc paysage et le neutraliserait. Le contrôle « 0 caractère / 0 `@media` après le bloc Lot C »
  (§2) est le test à refaire dans ce cas.
- **`index.html` / `version.json` / `sw.js`** : non modifiés ici — la CI les régénère depuis
  `Archipel_industry_alpha-7.html` après un merge sur `main` (« Sync PWA » / « Sync version.json »).
