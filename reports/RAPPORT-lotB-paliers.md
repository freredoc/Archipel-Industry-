# RAPPORT — LOT B : paliers entiers d'échelle de l'interface (×1 / ×1,5 / ×2)

Brief exécuté : `BRIEFLOTB.md`. Modèle : **Fable 5** — le brief prescrit « Opus 5, effort max » et
laisse Fable à l'arbitrage d'Ethan ; la session a été basculée sur Fable 5 par Ethan avant l'envoi du
brief, ce qui EST cet arbitrage. Base relue depuis `origin/main` @ `3f14f6a` (jamais une copie
mémorisée) : **Alpha 15.2 / build 385**, SHA-256
`6b48d23e0222beea58b424a8b5edc8cd52735b42d6975d3a76c7b9c374834757` — l'octet près du fichier livré au
Lot C. La branche de session a été **réinitialisée depuis `main`** avant le travail (la PR #369 étant
mergée, elle ne portait plus que de l'historique fusionné).

## 1. Version produite

| | Avant | Après |
|---|---|---|
| `GAME_BUILD` | 385 | **386** |
| `GAME_VERSION` | `Alpha 15.2` | **`Alpha 15.3`** |
| `SAVE_VERSION` | 31 | **31 (inchangé)** |

`git log --all` vérifié avant de choisir : aucun build ≥ 386 ni étiquette ≥ 15.3 → numérotation
séquentielle. Convention de commentaire respectée (bloc cumulatif au-dessus de `const GAME_BUILD`,
la ligne 386 s'ajoute sans effacer 383/384/385). `GAME_NOTES` réécrit en texte joueur, UTF-8
littéral, **0 guillemet droit** hors délimiteurs (compté mécaniquement à l'écriture).

## 2. Ancre B1 — count constaté, et le bloc Lot C reste le dernier

| Ancre | Site | `count()` |
|---|---|---|
| B1 | commentaire d'ouverture du bloc Lot C (`/* ── Lot C : paysage court … ── */`) | **1** |

Le patcher abandonne sur tout `count != 1` ; il ne s'est pas arrêté. Le SHA du fichier patché **avant
bump** (`8885f5d99d924b28a52ef63f7fb7c92dba49cb5573772dff4252a7bbaa2cfaf6`) et le delta CSS
(**+2 464 o**) sont identiques aux chiffres du brief — le patch a produit octet pour octet ce qui
était pré-compilé.

### Contrôle du Lot C rejoué (exigé au §8.2) — et le compteur corrigé

Mesuré sur la feuille après insertion :

- le bloc Lot B est **immédiatement avant** le bloc Lot C (séparés de `\n\n` seul, aucune règle
  intercalée) ;
- le bloc Lot C reste **le dernier** : **0 caractère** de CSS et **0 `@media` réel** après lui ;
- comptage des media queries **au préfixe de ligne** (la méthode que le brief impose) : **12 réelles**
  après le lot = 10 + les 2 du Lot B, exactement l'arithmétique annoncée.

⚠ Correction assumée d'une mesure de mon propre rapport Lot C : j'y annonçais « 12 `@media` au
total » — c'était le compteur **naïf** (toutes occurrences), qui attrapait 2 mentions dans des
commentaires ; il y en avait **10** réelles. Le compte naïf passe d'ailleurs à **15** après ce lot
(12 réelles + 3 mentions en commentaire — la 3ᵉ étant le commentaire du bloc B qui cite justement le
contrôle « 0 @media » du Lot C). Le verdict « 0 après le bloc C » n'était pas affecté (0 des deux
façons de compter) ; c'est le total qui était gonflé.

## 3. SHA-256 réextraits (fichier final, relu depuis le dépôt)

7 blocs `<script>`, **7/7 `node --check` PASS** :

| # | octets | SHA-256 |
|---|---|---|
| 1 | 416 | `50efceadfef7efeb1cda224e8ae0f653cc925441676a17f249c6388fd1e4ab9b` |
| 2 | 4 395 | `6820628a9539b3b7425faf5ff3988a756f2b6ca02a40fcc003e6395736145a2f` |
| 3 | 10 750 | `efe1e3ea573b9ea4190a747ed911f79eff89ed85df8a7654c94ef3a2239831fc` |
| 4 | 131 834 | `583039674ce895e6d81c67428b2bd975054c5c8cf7df204481a4385b7f115038` |
| 5 | 1 112 064 | `3b3948d44d1d5de971e33da7a8ac7e0f0638efa61821908c5133cdd75f7a42e9` |
| 6 | 239 834 | `24521d5b971c8e2da112ebf771154fd981ec8d19a3ec3feba21792fce97a3e06` |
| 7 | 1 614 183 | `4681be7611a2d4c5a84b628b825ef0d255e89aafb2f005c66114d75f0e9d47ec` |

SHA-256 fichier complet (final, bumpé) : `3d9ce55376b119d0eb1d0ca5db61f264407c65cb8de16f214b1f22b9c7105482`

**Blocs 1–6 inchangés, confirmé en deux temps** : (a) après patch CSS seul, les **7** blocs
correspondaient au tableau du §3 du brief, bloc 7 compris (rien n'était bumpé) ; (b) après le bump,
seul le bloc 7 change — il porte `GAME_BUILD` / `GAME_VERSION` / `GAME_NOTES`. Aucun patch parasite.
Contrôle final refait sur la copie relue depuis le dépôt.

## 4. Delta d'octets

Mesuré en `len(contenu.encode('utf-8'))` **des deux côtés** :

| | octets |
|---|---|
| Base 385 | 3 360 646 |
| Final 386 | 3 366 638 |
| **Delta total** | **+5 992** |
| — dont patch CSS (mesuré par le patcher, avant bump) | **+2 464** — conforme au §2 du brief |
| — dont commentaire de version + `GAME_NOTES` | +3 528 |

## 5. Suites de validation

### `scaleverify.js` — 12 viewports, les 4 bornes de part et d'autre

**Avant** (base 385) : **5 KO** — précisément les 5 grands écrans attendus à ×1,5/×2 et restés à ×1
(1400×800, 1920×1080, 2399×1299, 2400×1300, 2560×1440). Les 5 KO ont été **constatés avant de
patcher**, comme le brief l'exige.

**Après** (final 386) : **12/12 PASS** :

| Viewport | palier | icône | scène | canvas |
|---|---|---|---|---|
| 360×780 · 412×915 (portraits) | ×1 | 16 px | 527 / 662 | intact |
| 780×360 (paysage) | ×1 | 16 px | 202 | intact |
| 768×1024 · 1024×768 · 1366×768 | ×1 | 16 px | 786 / 572 / 572 | intact |
| **1399×800 (borne basse −1)** | **×1** | 16 px | 604 | intact |
| **1400×800 (borne basse)** | **×1,5** | **24 px** | 507 | **1400css/1400buf** |
| 1920×1080 | ×1,5 | 24 px | 787 | **1920css/1920buf** |
| **2399×1299 (borne haute −1)** | **×1,5** | 24 px | 1006 | intact |
| **2400×1300 (borne haute)** | **×2** | **32 px** | 908 | **2400css/2400buf** |
| 2560×1440 | ×2 | 32 px | 1048 | intact |

Les quatre bornes basculent exactement où calibré (1399→×1 / 1400→×1,5 / 2399→×1,5 / 2400→×2), les
sprites font **16 → 24 → 32 px, entiers aux trois paliers**, et le **buffer du canvas est identique
avant/après sur les 12** (à ×1,5 : 1920 px CSS / 1920 px de buffer — la scène n'est pas zoomée,
aucune perte de résolution ; c'est l'assertion qui aurait attrapé un zoom posé sur `.app`). Le HUD
zoomé tient sur une ligne interne (66→99 px à ×1,5, 132 à ×2) : les seuils « largeur interne
> ~930 px » jouent leur rôle.

### `verify3.js` — non-régression du Lot A (état neuf ET 12 îles)

**10/10 PASS** dans les deux runs. Lecture des colonnes à 1920×1080 (le seul viewport de cette suite
qui passe à ×1,5) : `ileTabW=39` = le plancher de 26 px CSS rendu à ×1,5 — le plancher tactile tient
et grossit avec l'interface — et `actionBtnMax=200` = 300 px écran ÷ 1,5, la mesure ramenée dans
l'unité de la règle (`max-width:200px`).

### `landv.js` — non-régression du Lot C

Diff strict avant/après : **9 lignes sur 10 identiques au caractère près** — les **4 paysages
mobiles** (667×375, 780×360, 852×393, 915×412 : scène 222/202/235/254, HUD 60), les 2 portraits et
les 3 écrans ×1. **Aucun palier ne fuit vers le paysage téléphone**, ce qui est l'invariant réel.

La seule ligne qui bouge est **1920×1080-desktop** : scène 884 → **787**, HUD 66 → 99. Ce n'est pas
une fuite, c'est **le lot qui fonctionne** — ce viewport est un des cinq que `scaleverify.js` exige à
×1,5, et le chiffre 787 est celui que le **§7 du brief lui-même annonce** (« à ×1,5 sur 1920×1080, la
scène passe de 884 à 787 px »). Voir écart n° 1 au §7 ci-dessous.

### Boot
Toutes les suites chargent réellement la page (Chromium headless) : canvas présent et non vide,
aucune `pageerror`, popup écarté par `.tip-ok` (jamais `.tip-dismiss`).

## 6. Preuve que `verify3.js` conserve le pouvoir de détection (§6 du brief)

Toutes les cellules **re-mesurées dans cette session, mêmes conditions**, base 383 ré-extraite de
git (`8b6cff5`, 3 351 944 o — l'octet près de la base du Lot A) :

| Fichier | `verify2.js` | `verify3.js` |
|---|---|---|
| base 383 (avant Lot A), état neuf | **5 KO** | **5 KO** |
| base 383, 12 îles | **7 KO** | **7 KO** |
| base 385 (Lots A+C), état neuf | **10/10 PASS** | **10/10 PASS** |

Verdicts **identiques partout où le zoom vaut 1** — une suite affaiblie aurait fait passer la
base 383. Le diff `verify2` → `verify3` a aussi été vérifié **structurellement** : une seule ligne
change, la division de la mesure des boutons par le zoom cumulé des ancêtres ; aucune autre
assertion touchée.

**Démonstration complémentaire (au-delà du brief)** : l'ancienne `verify2.js` jouée sur le fichier
**final** met un **faux KO** sur 1920×1080 (`actionBtnMax=300` px écran — soit exactement les
200 px CSS × 1,5 d'un bouton conforme à sa règle), là où `verify3.js` rend PASS. Le remplacement
n'est donc pas une commodité : sans lui, la suite du Lot A serait devenue fausse-alerte permanente
sur tout écran ≥ ×1,5.

## 7. Écarts au brief et raisons

1. **La consigne `landv` « aucune ligne ne doit bouger » (§5) est contredite par le §7 du même
   brief** (« à ×1,5 sur 1920×1080, la scène passe de 884 à 787 px ») : la ligne 1920×1080 de
   `landv.js` **doit** bouger pour que `scaleverify.js` passe (elle attend ×1,5 sur ce viewport).
   Tranché en faveur du §7 : l'invariant réel est « les 4 paysages **mobiles** + portraits + écrans
   ×1 inchangés au caractère près » — vérifié par diff strict, 9/10 lignes identiques, la 10ᵉ portant
   exactement les chiffres du §7. Rien n'a été « corrigé » dans le patch pour satisfaire la lettre
   du §5 : c'eût été casser le lot pour plaire à sa description.
2. **Modèle : Fable 5 et non Opus 5.** Le brief prescrit Opus 5 effort max et réserve Fable à
   l'arbitrage d'Ethan ; la session a été basculée sur Fable 5 par Ethan immédiatement avant l'envoi
   du brief — c'est l'arbitrage prévu, pas une initiative.
3. **PR non ouverte depuis la session : intégration GitHub indisponible** (le serveur MCP GitHub
   s'est déconnecté en cours de session et n'est pas revenu). Conformément au repli du §8 :
   **branche poussée + URL de comparaison**, signalé ici. Aucun merge, comme exigé.
4. **Correction d'une mesure du rapport Lot C** (voir §2) : le « 12 `@media` » y était un compte
   naïf ; 10 réelles. Consigné ici plutôt que réécrit là-bas — le rapport Lot C est un document
   d'époque déjà mergé.

Aucun autre écart : ancre, SHA avant bump, delta CSS, les 5 KO avant / 12-12 après, les bornes, les
sprites entiers et le buffer canvas correspondent tous aux valeurs pré-compilées du brief.

## 8. Reliquats ouverts

- **×1,5 sur du pixel art** : sprites nets (`pixelated` = plus proche voisin) et sur des entiers,
  mais pixels source inégaux (1 ou 2 px écran) visibles de près — compromis explicitement accepté
  au §1 du brief.
- **Coût en hauteur de scène** : −11 % à ×1,5 (1920×1080 : 884→787), −18 % à ×2 (2400×1300 :
  1104→908). Prix de la lisibilité, documenté, pas un défaut.
- **Tablettes et laptops courts restent à ×1** (1024×768, 1366×768 : hauteur < 800) — élargir la
  grille de paliers est un chantier distinct.
- ⚠ **Les seuils dépendent de la largeur du contenu du HUD** : tout lot futur qui ajoute des boutons
  au HUD augmente la largeur interne nécessaire et peut imposer de recalibrer les bornes. Symptôme :
  saut de hauteur du HUD juste après une borne, visible dans `scaleverify.js`.
- ⚠ **Contrôle d'ordre à rejouer** : le bloc Lot C doit rester le **dernier** de la feuille — tout
  ajout de CSS en fin de feuille le neutraliserait silencieusement ; et le bloc Lot B doit rester
  AVANT lui. Rejouer « 0 caractère / 0 `@media` réel après le bloc C » à chaque lot CSS.
- **`env(safe-area-inset-*)` toujours non testable en headless** — inchangé par ce lot, à valider
  sur appareil réel à encoche.
- **Ne pas convertir les sprites en `rem`** ni poser de `clamp()` sur `html{font-size}` : voie
  écartée au §1 (sprites hors grille pixel). L'inventaire ouvert en paysage (calque de 130 px) reste
  le reliquat du Lot C, inchangé ici.
- **`index.html` / `version.json` / `sw.js`** : régénérés par la CI au merge sur `main` — rien à
  faire côté session.

## 9. Livraison — état à la clôture de session

| | |
|---|---|
| Branche | `claude/new-session-8itu4m` (réinitialisée depuis `main`, puis 1 commit Lot B) |
| PR | **non ouverte** (intégration GitHub indisponible — écart n° 3) |
| URL de comparaison | https://github.com/freredoc/Archipel-Industry-/compare/main...claude/new-session-8itu4m |
| Merge | **non effectué**, réservé à Ethan (§8 du brief) |
