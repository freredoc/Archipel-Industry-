# RAPPORT — LOT A : dimensionnement UI (anti-débordement / anti-étirement / safe-area)

Brief exécuté : `BRIEFLOTA.md`. Base retéléchargée en tête de session (pas de copie mémorisée) :
`main` @ `8b6cff5`, `Archipel_industry_alpha-7.html` conforme au build annoncé par le brief.

⚠ **Nom de fichier écarté du brief** : le brief demande `RAPPORT-lotA.md`, mais ce nom est déjà pris
par un fichier **suivi dans git**, sans rapport (le « Lot A » du build 379 — retrait du halo permanent
sur l'onglet Améliorer). L'écraser aurait détruit ce dossier historique distinct. Le dépôt contient
déjà plusieurs `RAPPORT-lot-*.md` à noms descriptifs (`RAPPORT-lot-ui-port.md`,
`RAPPORT-lot-gisements.md`…) : ce rapport suit la même convention sous
**`RAPPORT-lotA-ui-dimensionnement.md`**.

## 1. Version produite

| | Avant | Après |
|---|---|---|
| `GAME_BUILD` | 383 | **384** |
| `GAME_VERSION` | `Alpha 15.0` | **`Alpha 15.1`** |
| `SAVE_VERSION` | 31 | **31 (inchangé)** |

Le brief ne fixait pas de numéro (« bumper GAME_VERSION ET GAME_BUILD ensemble, en choisissant le
numéro disponible au moment de l'exécution »). `git log --all` a été passé au crible avant de choisir
(aucun Lot B/C, aucun build > 383 déjà mergé) : 384 / Alpha 15.1 suit la numérotation séquentielle
observée dans tout l'historique du projet. `GAME_NOTES` a été réécrit (texte joueur, sans `"`,
guillemets français `«»` uniquement, UTF-8 littéral) : la note de la 15.0 (nettoyage de champs morts,
sans rapport) a été remplacée par une description du Lot A.

## 2. Les 6 anchors — count constaté

Le patcher (`patch_lotA.py`) lève un `SystemExit` sur tout anchor dont le `count()` n'est pas
exactement 1, avant toute substitution. Les 6 se sont résolues sans interruption :

| Anchor | Site | `count()` |
|---|---|---|
| A1a | `.island-tabs` scrollable + plancher onglet | **1** |
| A1b | `.hud-main` compressible | **1** |
| A2a | `.hud` wrap + safe-area | **1** |
| A2b | media 760 nettoyée | **1** |
| A3 | `.tab-btn` max-width | **1** |
| A4 | `.toolbar-wrap` safe-area | **1** |

Aucun anchor n'a dû être ré-extrait : la base 383 correspondait exactement à celle sur laquelle le
brief avait été pré-compilé.

## 3. SHA-256 réextraits (fichier final, dans le dépôt)

7 blocs `<script>` (comptage : balises en début de ligne), **7/7 `node --check` PASS** :

| # | octets | SHA-256 |
|---|---|---|
| 1 | 416 | `50efceadfef7efeb1cda224e8ae0f653cc925441676a17f249c6388fd1e4ab9b` |
| 2 | 4 395 | `6820628a9539b3b7425faf5ff3988a756f2b6ca02a40fcc003e6395736145a2f` |
| 3 | 10 750 | `efe1e3ea573b9ea4190a747ed911f79eff89ed85df8a7654c94ef3a2239831fc` |
| 4 | 131 834 | `583039674ce895e6d81c67428b2bd975054c5c8cf7df204481a4385b7f115038` |
| 5 | 1 112 064 | `3b3948d44d1d5de971e33da7a8ac7e0f0638efa61821908c5133cdd75f7a42e9` |
| 6 | 239 834 | `24521d5b971c8e2da112ebf771154fd981ec8d19a3ec3feba21792fce97a3e06` |
| 7 | 1 607 313 | `89c3f20b3e6c6a6ec87eda3d56fc10cd0ad358ccc381b4655bf3e1bd3d259868` |

SHA-256 fichier complet (final, avec version bumpée) :
`ad2f92d74ef15ad02f2a56c18ec3c5e103555223c186f801ac1999ab906d9b31`

**Confirmation blocs 1–6 inchangés :** ces 6 hachages sont **identiques, caractère pour caractère**,
au tableau du §3 du brief. Vérification faite en deux temps : (a) immédiatement après le patch CSS,
**avant** tout bump de version — les 7 blocs **ET** le SHA-256 du fichier complet
(`f6b965c34aaa37e19907352d6c918c8513a02fc0da2b8c36d6d4a1e413ce34d3`) correspondaient alors
**exactement** aux 7 lignes du tableau du brief (bloc 7 inclus, puisque rien n'avait encore été
touché) ; (b) après le bump, seul le **bloc 7** change (il porte `GAME_BUILD`/`GAME_VERSION`/
`GAME_NOTES`) — les blocs 1–6 restent identiques au tableau ci-dessus. Aucun patch parasite.

## 4. Delta d'octets

Mesuré en `len(contenu.encode('utf-8'))` des deux côtés (pas de comptage en caractères) :

- **Patch CSS seul** (avant bump) : 3 351 944 → 3 352 819 octets = **+875 o**, conforme au chiffre
  annoncé par le brief (§2 : « Delta total : +875 octets ») et à la sortie du patcher lui-même
  (`delta octets : +875`).
- **Delta final** (CSS + bloc de changelog + GAME_BUILD/VERSION/NOTES) : 3 351 944 → 3 355 591 octets
  = **+3 647 o**, dont +875 o pour le patch et +2 772 o pour le commentaire de version (convention du
  projet : chaque bump documente le changement dans un bloc de commentaire au-dessus de
  `const GAME_BUILD`) + la réécriture de `GAME_NOTES`.

## 5. Résultats de la suite de validation (10 viewports, `.tip-ok` cliqué)

### Run 1 — base 383, état neuf (`verify2.js <fichier> 0`)
```
KO   320x568          spill=hud-grp(R383)+island-tabs(R383)+2 onglets hors cadre
KO   360x780-S25FE    spill=hud-grp(R383)+island-tabs(R383)+2 onglets hors cadre
PASS 393x852
PASS 412x915
PASS 780x360-paysage
PASS 768x1024-iPadP
PASS 1024x768-iPadL
KO   1280x800         actionBtnMax=245 (étiré)
KO   1366x768         actionBtnMax=262 (étiré)
KO   1920x1080        actionBtnMax=373 (étiré)
>>> 5 ECHEC(S)
```
**5 KO / 10**, exactement le chiffre annoncé par le brief (§5, tableau « avant »).

### Run 2 — base 383, 12 îles simulées (`verify2.js <fichier> 12`)
```
KO   320x568 · 360x780-S25FE · 393x852 · 412x915   (spill hud-grp/island-tabs, R551)
PASS 780x360-paysage · 768x1024-iPadP · 1024x768-iPadL
KO   1280x800 · 1366x768 · 1920x1080               (étirement)
>>> 7 ECHEC(S)
```
**7 KO / 10**, exactement le chiffre annoncé par le brief.

### Run 3 — fichier final (patché + bumpé build 384), état neuf (`verify2.js <fichier> 0`)
```
PASS sur les 10 viewports (ileTabW=26 partout, actionBtnMax plafonné à 200 sur 1280/1366/1920)
>>> 10/10 PASS
```

### Run 4 — fichier final (patché + bumpé build 384), 12 îles simulées (`verify2.js <fichier> 12`)
```
PASS sur les 10 viewports (ileTabW=26 partout, actionBtnMax plafonné à 200 sur 1280/1366/1920)
>>> 10/10 PASS
```

**Setup falsifiable respecté** : les runs à N=12 injectent des onglets d'île clonés dans le DOM
(`.island-tab` dupliqué jusqu'à atteindre 12) — aucun déblocage de partie n'a été nécessaire ; les
runs à N=0 mesurent l'état neuf du jeu tel qu'il boote. Le détecteur de débordement ignore tout
élément dont un ancêtre défile horizontalement (sans quoi les onglets légitimement hors-cadre d'une
bande scrollable compteraient comme des débordements et feraient échouer le test à tort).

La suite **inchangée** passe donc bien de KO à PASS comme demandé, base et fichier patché comparés
dans la même session, KO constatés avant patch puis PASS après.

### Contrôle de non-régression — hauteur du HUD

Mesure directe de `getBoundingClientRect().height` sur `.hud`, base vs fichier final :

| Viewport | Avant | Après | Écart |
|---|---|---|---|
| 320×568 | 108 | 108 | — |
| 360×780-S25FE | 108 | 108 | — |
| 393×852 | 108 | 108 | — |
| 412×915 | 108 | 108 | — |
| 780×360-paysage | **106** | **108** | +2 (accepté) |
| 768×1024-iPadP | **106** | **108** | +2 (accepté) |
| 1024×768-iPadL | 66 | 66 | — |
| 1280×800 | 66 | 66 | — |
| 1366×768 | 66 | 66 | — |
| 1920×1080 | 66 | 66 | — |

Exactement le seul écart accepté par le brief (106→108 px, le HUD wrappe au lieu de couper un bouton).
Hauteur de scène correspondante (état neuf) : 780×360-paysage 124→122 ; 768×1024-iPadP 788→786 (même
delta de 2 px). Tout le reste est **strictement inchangé**.

### Boot
Les 4 runs ci-dessus chargent réellement la page dans Chromium headless (pas de simple
`node --check`) : aucune `pageerror`, canvas présent et non vide, `.tip-ok` cliqué avec succès sur
chaque viewport pour écarter le popup d'accueil.

## 6. Écarts au brief et raisons

1. **Nom de fichier du rapport** (voir en-tête) : `RAPPORT-lotA.md` était déjà pris par un fichier
   suivi dans git, sans rapport avec ce lot → livré sous `RAPPORT-lotA-ui-dimensionnement.md`.
2. **Numérotation de version** : le brief laissait le choix ; 384 / Alpha 15.1 retenu par continuité
   avec la séquence de l'historique (aucun autre lot n'avait déjà consommé ce numéro).
3. **Mode de livraison (PR)** : le brief affirme « PR impossible : l'App GitHub n'est pas connectée
   pour l'org » et demande de livrer une branche + une URL de comparaison, à charge pour Ethan
   d'ouvrir la PR manuellement. **Dans cette session, l'intégration GitHub est bien connectée et
   scopée sur ce dépôt** (outils MCP GitHub disponibles), et la consigne standing du projet est que
   Claude ouvre lui-même la PR une fois le contenu prêt. Écart assumé : une PR sera ouverte
   directement depuis cette session au lieu de s'arrêter à une branche + URL de comparaison — cela
   ne change rien au contenu livré, seulement à la mécanique de remise.

Aucun autre écart : les 6 sites, les anchors, les SHA-256 et les deltas mesurés correspondent tous
exactement aux valeurs annoncées par le brief — un patch réellement pré-compilé et vérifié en amont.

## 7. Reliquats ouverts

- **`env(safe-area-inset-*)`** (A2a, A4) : non testable en environnement headless (toujours 0).
  Posé pour les builds store (Android APK / PWA) ; **à vérifier sur un appareil réel à encoche** —
  non validé dans cette session, comme prévenu par le brief.
- **Paysage mobile** : reste à ~100-122 px de hauteur de scène (780×360). Explicitement hors
  périmètre — c'est le **Lot C**.
- **Corps de texte fixe à 12.5 px** (`html{font-size:12.5px}`) : hors périmètre — **Lot B**
  (`clamp()` sur la racine), non touché ici comme demandé.
- **Plancher tactile de 26 px** : sous le seuil recommandé (~44 px) mais volontairement conservé
  pour ne pas modifier le design actuel — à revoir au Lot B, pas augmenté unilatéralement ici.
- **`index.html` / `version.json` / `sw.js`** : non modifiés dans cette branche (convention du
  projet : ils sont régénérés par la CI depuis `Archipel_industry_alpha-7.html` après un merge sur
  `main`, étapes « Sync PWA » / « Sync version.json »). Rien à faire de plus côté session.
