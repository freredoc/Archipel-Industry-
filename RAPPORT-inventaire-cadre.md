# RAPPORT — Lot « cadre 9-slice de la barre d'inventaire »

Brief : `BRIEFinventairecadre.md`. Modèle : **Opus 5** (plancher du brief), effort élevé.
Lot **CSS pur** : aucun bloc `<script>` touché, aucun sprite importé, aucune donnée de partie.

Source re-fetchée en tête de session depuis `origin/main` @ `e62b0ee` — **build 417 / Alpha 18.4**,
`3 686 292` octets, SHA-256 `15652fb6c0ba2ad401980e9bd9b9ad5b373dc55361963dc1649b3cfcbec0b011`.
Conforme au §3 du brief à l'octet près. La branche de session a été réinitialisée depuis `main`
(travail précédent entièrement mergé).

Nom de rapport **vérifié libre** avant écriture (le brief le demandait, `RAPPORT-lotA.md` ayant déjà
mordu) : aucun `RAPPORT-inventaire-*` dans l'arborescence ni dans tout l'historique git.

---

## 1. Version produite

| | Avant | Après |
|---|---|---|
| `GAME_BUILD` | 417 | **418** |
| `GAME_VERSION` | `Alpha 18.4` | **`Alpha 18.5`** |
| `SAVE_VERSION` | 31 | **31 (inchangé)** |

`git log --all` vérifié : aucun build ≥ 418 ni étiquette ≥ 18.5. Commentaire de version **cumulatif**
(la ligne « 18.5 » s'ajoute, rien n'est effacé). `GAME_NOTES` en texte joueur, **0 guillemet droit**
hors délimiteurs (compté mécaniquement à l'écriture).

---

## 2. Ancres appliquées — `count` vérifié

| ancre | cible | `count` |
|---|---|---|
| A1 | commentaire + règle `.inventory` | **1** |
| A2 | `body.theme-inox .inventory` | **1** |
| A3 | bloc Lot C, après `.tool-group-label{display:none;}` | **1** |

Le patcheur abandonne sur tout `count != 1` ; il ne s'est pas arrêté, aucune ancre n'a été retapée.

**Idempotence vérifiée** (§3 du brief) : rejeu sur le fichier déjà patché → les 3 ancres sortent
« déjà appliqué », **delta +0**, `cmp` byte-identique.

---

## 3. T1 — Blocs `<script>` intacts *(le contrôle central d'un lot CSS pur)*

Les 7 blocs du fichier patché, comparés **bit à bit** à ceux de la base 417 :

| # | octets | SHA-256 | vs base |
|---|---|---|---|
| 1 | 416 | `50efceadfef7efeb1cda224e8ae0f653cc925441676a17f249c6388fd1e4ab9b` | **=** |
| 2 | 4 395 | `6820628a9539b3b7425faf5ff3988a756f2b6ca02a40fcc003e6395736145a2f` | **=** |
| 3 | 10 750 | `efe1e3ea573b9ea4190a747ed911f79eff89ed85df8a7654c94ef3a2239831fc` | **=** |
| 4 | 131 834 | `583039674ce895e6d81c67428b2bd975054c5c8cf7df204481a4385b7f115038` | **=** |
| 5 | 1 113 967 | `bebed304106c1f83e9eab46c8bb9082eccac36183af243649d1dcb5baad34cdc` | **=** |
| 6 | 430 158 | `3c6fdacf9a3098c18262a09a174cd9998babefee683b9e0b2cf915e4891abc72` | **=** |
| 7 | 1 734 252 | `920d8371e3e58e1100a386eabafdba7e161e3ab594f5d7440bc5628b64f8e28a` | **=** |

**T1 PASS — les 7 sont identiques à la base**, `node --check` 7/7. Le patch n'a pas mordu hors du CSS.

**Après le bump**, seul le bloc 7 change (il porte `GAME_BUILD`/`GAME_VERSION`/`GAME_NOTES`) :
1 734 252 → 1 736 458 o, SHA `af831bc96658974efd3d2351c3805120c3ac4ae30419327ee2f453486b79fdf6`.
Les blocs 1–6 restent ceux de la base, re-vérifiés après bump. `node --check` 7/7.

SHA-256 du fichier **patché avant bump** : `de63214a0c1fe73d8f6b049c71c7dcd37f927346f4e74e94145fbfa6b7c04a9e`
— identique au §3 du brief.
SHA-256 du fichier **final (après bump)** : `523706fa39fccf3ebfc129d40b2f9c9c21d47c718908316ac7d6366fa7e5709b`

---

## 4. Delta d'octets

Mesuré en `len(contenu.encode('utf-8'))` des deux côtés :

| | octets |
|---|---|
| Base 417 | 3 686 292 |
| Patché, avant bump | 3 688 271 → **+1 979** (identique au brief) |
| Final 418 | 3 690 477 → **+4 185** au total (dont +2 206 de commentaire de version et `GAME_NOTES`) |

---

## 5. Tests — montage effectif et résultat

Harnais Playwright/Chromium, **un contexte navigateur NEUF par scénario** (localStorage isolé) — le
brief signale que deux fichiers servis depuis `file://` le partagent, et que l'état d'ouverture de
l'inventaire est persisté. Popup d'accueil écarté par `.tip-ok`. Déploiement/repli par **vrai clic
souris** (`page.mouse.click` aux coordonnées mesurées, jamais `el.click()`), avec **re-clic tant que
la classe n'a pas changé** — `useGhostGuard` avale le premier.

### ⚠ Écart au §8 du brief : l'inventaire démarre DÉPLOYÉ, pas replié

Le §8 décrit T2 comme « mesurer replié, puis déployer par clic ». **Mesuré : une partie neuve boote
inventaire OUVERT** (`class="inventory open"`) — c'est le comportement posé au build 364 (lot 3B,
« inventaire déplié à la création »), antérieur au brief. Ma première passe a donc rendu 2 KO — et
c'est **l'assertion de classe exigée par le brief qui les a produits**, sur sa propre prémisse. Le
montage a été inversé : on mesure l'état de boot (déployé), **puis on REPLIE** par un vrai clic pour
obtenir les 45 px. Toutes les valeurs attendues sont ensuite retrouvées.

| Test | Montage | Résultat |
|---|---|---|
| **T2** 390×844, fr | classe assertée avant **chaque** lecture de hauteur | **PASS** — `border-top-width: 6px`, `border-image-source` non nul, `background-image` non nul, **139 px déployée**, **45 px repliée**, cadre conservé aux deux états, scène repliée **546 px** |
| **T3** 740×400 | montage vérifié d'abord : `orientation:landscape` **ET** `max-height:520px` tous deux vrais (sinon test à vide) | **PASS** — `border-top-width: 3px`, calque **133 px**, `border-image-width: 3px` **avec `slice: 8`** (le slice est bien redéclaré, pas seulement la largeur), HUD 99 px |
| **T4** thème inox | `classList.add('theme-inox')` puis relecture | **PASS** — `border-image-source` **change**, fond **change**, hauteur **inchangée** (139 → 139) : même géométrie |
| **T5** invariants | mesurés dans la même passe que T2 | **PASS** — HUD **108 px** (portrait) et **99 px** (paysage court), cadre du HUD **inchangé**, scène **591 px** inventaire déployé, `.inventory` **pleine largeur** (390) |
| **T6** visuel | rendu réel capturé aux 4 états (portrait replié/déployé, inox, paysage court), base **et** patché | **PASS** — voir §6 |
| — | erreurs JS / console, base et patché | **aucune** |

### Contre-épreuve — la suite est falsifiable

La **même suite, inchangée**, jouée sur la base 417 non patchée : **11 KO**.

| mesure | base 417 | patché 418 | brief |
|---|---|---|---|
| bordure portrait | 1 px | **6 px** | 6 px |
| `border-image-source` | `none` | **non nul** | non nul |
| barre repliée | 35 px | **45 px** | 45 px |
| calque déployé | 129 px | **139 px** | 139 px |
| scène, barre repliée | 556 px | **546 px** | 546 px |
| paysage court, bordure | 1 px | **3 px** | 3 px |
| paysage court, calque | 129 px | **133 px** | 133 px |
| inox : le cadre change ? | **non** (identique) | **oui** | oui |

Toutes les valeurs de base retrouvées correspondent au tableau du §3 du brief. **Les 4 invariants T5
passent des DEUX côtés** — ce sont donc de vrais invariants, pas des assertions rendues vraies par le
patch.

---

## 6. T6 — contrôle visuel

Captures réelles (recadrées sur HUD + barre d'inventaire), avant/après, aux quatre états :

- **Portrait déployé** — la barre passe d'un aplat sombre bordé de deux filets à une zone **encadrée
  et texturée**, nettement lisible comme un bloc distinct sous le HUD.
- **Portrait replié** — le cadre tient sur la barre seule (45 px) sans écraser les deux boutons
  INVENTAIRE / Production.
- **Thème inox** — cadre **différent** de celui du HUD et fond en tôle larmée : la barre se distingue
  de `--inox-panneau` qui habille HUD et panneaux, ce qui est l'intention du lot.
- **Paysage court (740×400)** — cadre visiblement **plus fin** (3 px), discret, sans manger la scène.

Le cadre est bien posé **sans `fill`** : la texture reste visible sous le sprite dans les quatre cas.
Aucun étirement aberrant du sprite (le slice de 8 px est respecté aux deux largeurs de bordure).

---

## 7. Décision du §4 du brief — **conservée**

L'amincissement à 3 px en paysage court est **gardé**. Le brief laissait le choix de le retirer d'un
mot. Motif : il ramène le surcoût de 10 à 4 px là où la scène ne fait que ~200 px et où le calque
déployé n'a **toujours aucun plafond** — sans lui, ce lot aggraverait 129 → 139 px sur l'écran le
plus contraint. Vérifié que `border-image` y est bien **redéclaré avec son slice** et pas seulement
`border-width` : `border-image-width: 3px` **et** `border-image-slice: 8` (T3). Sans cela le sprite
serait étiré pour 6 px de rendu dans une boîte de 3.

---

## 8. Écarts au brief et raisons

1. **T2 : l'état de boot est DÉPLOYÉ, pas replié** (§5 ci-dessus). Le montage du brief a été inversé
   pour mesurer réellement les deux états. Ce n'est pas un défaut du patch — les deux hauteurs
   attendues (45 / 139) sont retrouvées.
2. **`GAME_NOTES` écrit AVEC accents**, alors que les **dix derniers builds (407 → 417) sont sans
   accents**. Motif : j'ai vérifié en build 385 que les accents traversent correctement la CI
   jusqu'à `version.json` (aucun `\uXXXX`, aucun caractère de remplacement), et cette note est lue
   par le joueur sous « Mise à jour disponible » — « recoit enfin le meme habillage » s'y lirait
   comme une faute. **Écart signalé pour arbitrage** : si la série sans accents est délibérée (et non
   une habitude propagée de session en session), la note se normalise en une ligne.
3. **PR ouverte depuis la session** : le MCP GitHub, déconnecté lors des lots précédents, est
   revenu — le repli « branche + URL » du §10 n'a donc pas eu à s'appliquer. **Aucun merge**, comme
   exigé.

Aucun autre écart : les 3 ancres, le SHA avant bump, le delta de +1 979 o, l'idempotence et les
huit mesures du tableau de contre-épreuve correspondent tous aux valeurs pré-calculées du brief.

---

## 9. Points en suspens

- **Le calque d'inventaire déployé reste sans plafond de hauteur.** Le lot en atténue l'effet en
  paysage court (3 px) mais ne le traite pas. `vh`/`dvh` restent inutilisables dans le harnais en
  paysage émulé (`30dvh` calcule 234 px sur un viewport de 360 de haut) — reliquat inchangé du
  chantier dimensionnement.
- **`--cadre-bouton` reste orphelin** (0 usage) après ce lot. À garder en tête avant d'importer un
  nouveau sprite de cadre : il y a déjà un cadre inutilisé en magasin.
- **Bordure fixée à 6 px en portrait.** L'alternative à 5 px n'aurait rendu que 2 px et affinait les
  équerres du sprite ; non retenue faute de gain (décision du brief, non rediscutée).
- **`env(safe-area-inset-*)`** toujours non testable en headless — inchangé par ce lot.
- **`index.html` / `version.json` / `sw.js`** : régénérés par la CI au merge sur `main`, rien à faire
  côté session.

---

## 10. Livraison

| | |
|---|---|
| Branche | `claude/new-session-8itu4m` |
| Base | `origin/main` @ `e62b0ee` (build 417) |
| Merge | **non effectué** — réservé à Ethan (§10 du brief) |
