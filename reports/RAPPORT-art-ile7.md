# RAPPORT — Lot « art île 7 »

**Livré en `GAME_BUILD = 444` / `GAME_VERSION = 'Alpha 21.1'`.** `SAVE_VERSION` **INCHANGÉ (31)** —
le lot n'ajoute aucun champ de partie.

Base : build 443 / Alpha 21.0, artefact `afdfce0c…` (= `.build-stamp` de `main`), `src/sprites-inline.js`
1 428 698 o, `src/index.src.html` 2 566 690 o. **Les quatre empreintes de départ concordent avec le brief.**

---

## 0. Ce qu'il faut lire en premier

Le brief demande **Opus 5, effort élevé** — c'est le modèle qui a exécuté.

1. **Le patcheur est byte-identique à celui du rédacteur.** Reconstruit sans le bump ni l'ajout,
   l'artefact sort à `f8c545ecb2d748878d66e0a9815104349de7e70dc0a0ecaa8cf9356e7c1197c0` — **exactement**
   le SHA du brief, et à 4 033 846 octets, sa taille exacte (§1).
2. **Une 26ᵉ image a été ajoutée au périmètre, sur demande d'Ethan : la bûcheronneuse neutre.** Le
   brief la classait hors périmètre avec un motif explicite — « **Le pack ne fournit pas ce PNG** ».
   Ce motif est tombé : le pack livré le contient. Détail, mécanisme et mesure au §4.
3. **Un constat que le brief ne mentionne pas** : sur 443 le foyer à charbon n'était pas sans
   sprite, il **portait l'art de la centrale à charbon**. Le lot L4 avait pré-branché une liste de
   candidats pour ce jour-là. Mesuré au §5.4.
4. **Une assertion fausse dans mon banc**, corrigée avant conclusion — c'est justement elle qui a
   mis le point 3 au jour (§5.6).

---

## 1. Empreintes, tailles, blocs

| Grandeur | Valeur |
|---|---|
| `src/sprites-inline.js` avant | 1 428 698 o |
| … après patcheur seul | **1 466 679 o** (**+37 981**) — *valeur du brief* |
| … livré (+ bûcheronneuse neutre) | 1 468 647 o (+1 968) |
| `src/index.src.html` avant | 2 566 690 o |
| … après patcheur seul | **2 567 118 o** (**+428**) — *valeur du brief* |
| … livré (+ bloc de version) | 2 570 416 o (+3 298) |
| Artefact avant | 3 995 437 o |
| … après patcheur seul | **4 033 846 o** — *taille du brief, au byte près* |
| Artefact livré | **4 039 112 o** (**+43 675** au total) |
| Blocs `^<script` | **7** (le build sort en erreur si ≠ 7) |
| SHA-256 de l'artefact livré, **ré-extrait du fichier produit** | `4ee2746ccd422cf0b414bcb1875dda7b03453eab1d23bbfddb4d42a8905fb516` |
| `.build-stamp` | identique à la ligne ci-dessus |

### Écart de SHA avec le brief — attendu, et vérifié plutôt que supposé

Le brief annonce `f8c545ec…`. L'artefact livré porte `4ee2746c…` : il contient en plus le bump de
version (obligatoire) et la 26ᵉ image (demandée). J'ai donc reconstruit la variante **« patcheur
seul »** avant d'ajouter quoi que ce soit :

```
SHA patch-seul : f8c545ecb2d748878d66e0a9815104349de7e70dc0a0ecaa8cf9356e7c1197c0
SHA du brief   : f8c545ecb2d748878d66e0a9815104349de7e70dc0a0ecaa8cf9356e7c1197c0
=> CONFORME : patch byte-identique a celui du redacteur
artefact : 4033846 o (attendu 4033846)
```

**Le patch appliqué est celui du rédacteur, au bit près**, et les deux sources sortent aux tailles
annoncées. Tout écart restant est imputable au bump et à l'ajout, pas à l'application.

## 2. Les 2 ancres

| Ancre | Cible | `count` |
|---|---|---|
| A1 — les 25 images, après la **dernière ligne d'alias** vers l'île 5 | `src/sprites-inline.js` | **1** |
| A2 — les 4 entrées `TILE_ANIM_BY_KEY` | `src/index.src.html` | **1** |
| B1 — *(ajout)* la bûcheronneuse neutre, avant le bloc des bandes | `src/sprites-inline.js` | **1** |

**3/3 à `count == 1`.** Le patcheur du rédacteur n'écrit rien si une ancre échoue ; mon patcheur
d'ajout est en plus **idempotent** (rejoué : « DEJA POSEE, rien a faire »).

## 3. Contrôle du pack avant application

Les 25 images embarquées en base64 dans `patch_art.py` ont été comparées **octet à octet** au dossier
`retenu/` du ZIP :

```
embarquees dans le patcheur : 25
presentes dans retenu/      : 26
identiques octet a octet : 25 / 25   ecarts : 0   absentes du pack : aucune
DANS LE PACK MAIS PAS DANS LE PATCHEUR : ['bat_bucheronneuse']
```

Ce contrôle n'est pas de la politesse : au lot 14.94, un ZIP joint contenait **l'ancien art**, et
seul le contrôle d'empreinte l'avait attrapé. Ici tout concorde, et l'écart est exactement l'image
qu'Ethan signale.

⚠ **Non injecté, conformément au brief** : les variantes `basalte` / `calcaire` / `gres_rouge` des
falaises. Seul `retenu/` l'est, comme son nom l'indique.

## 4. L'ajout : la bûcheronneuse neutre

### Pourquoi il entre au périmètre

Le brief l'exclut, mais avec un motif **factuel et vérifiable**, pas un arbitrage :

> « **Le pack ne fournit pas ce PNG** — le défaut reste entier après ce lot. »

Le pack livré contient `retenu/batiments/bat_bucheronneuse.png` (32×32, 651 o). Le motif de
l'exclusion est tombé et Ethan le signale : l'image entre. **Rien d'autre du hors-périmètre n'est
touché** (panneau de la ferme, champs contigus, carrière hors côte, coupe à 100, fusion 48/49,
variantes de falaise : tous intacts).

### Le défaut, établi au fichier

`buildingSpriteKey(id, upgrade, dir)` procède en trois temps : `DIR_ART_IDS` **si et seulement si
`dir != null`**, puis `BLD_SPRITE_OVERRIDE`, puis les candidats `['bat_' + id, id, id + '_v1']`.

`DIR_ART_IDS.bucheronneuse = 'bat_bucheronneuse'` produit donc `bat_bucheronneuse_<n|s|o|e>` — mais
**seulement quand une direction est fournie**. Deux conséquences, toutes deux visibles :

1. **Les cinq sites qui appellent `buildingSpriteKey` SANS direction** (vignette du menu, fiches,
   panneau d'amélioration) tombaient sur les candidats, dont le premier — `bat_bucheronneuse` —
   n'existait pas : la fonction rendait **`null`**.
2. **`fellDir` n'est posé qu'au PREMIER abattage** (`mach.fellDir = fellDir`, et l'appelant du dessin
   fait `drillDir != null ? drillDir : fellDir`). Une bûcheronneuse **posée mais jamais utilisée**
   recevait donc `undefined` et se dessinait en **rectangle de couleur**.

### Pourquoi aucun code n'est nécessaire

Déposer l'image sous la clé `bat_bucheronneuse` fait mouche sur `cands[0]` — exactement le mécanisme
de clé dérivée que le brief invoque pour 10 de ses 25 images. **Zéro ligne de code.**

⚠ **Et le format est celui que le code attend déjà.** Le débord « 1 case ½ » de `drawBuilding` ne
mord que sur les quatre clés `<base>_n` / `_s` / `_o` / `_e` ; la neutre en 32×32 n'entre dans aucun
cas et reste cadrée 1×1. Le commentaire du débord anticipait précisément ce repli :

> « les clés `bat_foreuse_<dir>` absentes du pack font retomber buildingSpriteKey sur `bat_foreuse`
> (32×32), **qui n'entre dans aucun des cas ci-dessous** »

Vérifié à l'exécution, pas déduit (T8).

## 5. Validation — 28 PASS / 0 KO, deux passes identiques

Pilote : `playwright-core` + Chromium 1194, servi en **HTTP depuis la racine du dépôt**, jamais
`file://`.

⚠ **Exigence du brief respectée : chaque test est joué DEUX FOIS**, sur le build 443 (témoin) et sur
le build patché, avec la **même sonde**. Les assertions portent sur la **transition**, jamais sur la
seule valeur finale — « un test qui passe déjà sur 443 ne prouve rien ».

### 5.1 `node --check` — 7 blocs × 3 variantes CI

```
  public : 7 blocs extraits, node --check 7/7 OK
  dev    : 7 blocs extraits, node --check 7/7 OK
  store  : 7 blocs extraits, node --check 7/7 OK
```

Extraction **séquentielle**, dossier de sortie **purgé** et **compté avant de boucler** — le banc
refuse de conclure si le compte ≠ 7 (piège 19.7 : une boucle qui annonce « 7/7 » sur un dossier n'en
contenant qu'un).

Gardes de comptage CI rejouées **après** rédaction des commentaires : `ko-fi` **1** en publique / **0**
en magasin, `const SELF_UPDATE = true;` **0** en magasin, `DEV_BUILD` correct des deux côtés.
`GAME_NOTES` s'extrait entier (348 car., non tronqué), `build=444 version=Alpha 21.1`.

### 5.2 T1 à T4 — les valeurs du brief, retrouvées

| Test | Témoin 443 → patché 444 |
|---|---|
| T1 total statiques | **0 → 10** (4 triangles · 5 icônes · foyer) |
| T2 présence des 4 bandes | **0 → 4** |
| T2 dimensions **réelles** | `["128x32","128x32","128x32","128x32"]` |
| T3 `itemSpriteKey('bois')` | `null` → `item_bois` |
| T3 `itemSpriteKey('latex')` | `null` → `item_latex` |
| T4 `TILE_ANIM_BY_KEY` île 8 | `[]` → les 4, chacune 4 frames de 32×32 |

T2 mesure les dimensions **en décodant chaque data-URL dans une `Image()`** : une bande mal découpée
passerait la présence et raterait cette mesure.

### 5.3 T5 — les falaises ne sont plus des alias

```
PASS T5 i8_falaise_s === i5_falaise_s : true -> false
PASS T5 data-URL de i8_falaise_s = 406 car. (PNG de 286 o) — 406 -> 406
PASS T5 les 11 falaises sont de VRAIS PNG (0 alias restant) — 0 -> 11 / 11
```

⚠ **Testé à l'exécution, jamais par `grep`** : la ligne d'alias **reste présente** dans le fichier —
c'est l'assignation suivante qui gagne, et **c'est l'ordre qui fait le remplacement**. Un `grep`
conclurait exactement le contraire de la vérité. J'ai étendu le test du brief aux **11** falaises,
pas seulement à `_s`.

### 5.4 T8 — la bûcheronneuse, et un constat sur le foyer

```
PASS T8 bat_bucheronneuse : absente -> presente — false -> true
PASS T8 SANS direction : null -> bat_bucheronneuse — null -> bat_bucheronneuse
PASS T8 NON-REGRESSION : avec direction, toujours l'art directionnel — bat_bucheronneuse_n -> bat_bucheronneuse_n
PASS T8 NON-REGRESSION : les 4 directionnelles intactes — 4 -> 4
PASS T8 le foyer quitte l'art EMPRUNTE a la centrale a charbon — bat_centrale_charbon -> bat_foyer_charbon
PASS T8 sprite neutre decode en 32x32 — 32x32
PASS T8 il n'entre PAS dans le debord « 1 case 1/2 » — DIR_ART_IDS.bucheronneuse=bat_bucheronneuse debord=false
```

**Constat non mentionné par le brief** : sur 443, `buildingSpriteKey('foyer_charbon')` ne rendait pas
`null` — il rendait **`bat_centrale_charbon`**. Le lot L4 avait pré-branché une **liste de candidats**
`['bat_foyer_charbon', 'bat_centrale_charbon']` (mécanisme 14.32) en écrivant noir sur blanc :

> « le jour où `bat_foyer_charbon` est déposé dans le pack, il est pris automatiquement, sans
> retoucher au code. En attendant, le foyer emprunte l'art de la centrale à charbon — REPLI ASSUMÉ »

Le foyer **portait donc l'art d'un autre bâtiment**, ce qui est une confusion visuelle silencieuse,
pas une absence. Ce lot lui rend le sien, et la mécanique pré-branchée a fonctionné exactement comme
annoncée.

### 5.5 T6 et T7 — pollution et non-régression

```
PASS T6 aucune bande dans ANIM_META, avant NI apres — [] / []
PASS T7 i5_falaise_s INTACT (source des anciens alias) — 406 car. -> 406 car.
PASS T7 aucune erreur reelle au boot, sur les DEUX builds — temoin=0 patche=0
PASS T7bis sur l'ile 8, 400 ticks sans exception — ile 8
PASS T7bis aucune ile en erreur de tick — 0
```

T7 compare `i5_falaise_s` **en longueur et en contenu** : le test échouerait si le remplacement avait
mordu sur la source des anciens alias. T7bis va au-delà du brief — il atteint réellement l'île 8 par
le vrai chemin et y fait tourner 400 ticks (`onTick` **et** `tickShips`).

Le filtre de bruit réseau (404 du serveur de banc) est **prouvé incapable de masquer un vrai
défaut** : trois messages témoins (`TypeError`, `ReferenceError`) le traversent.

### 5.6 Une assertion fausse de mon banc

Le premier passage donnait **28 PASS / 1 KO**. Le KO était **mon attente**, pas le code : j'avais
écrit `foyer_charbon → null` sur le témoin, en supposant qu'un bâtiment sans art rend `null`. La
mesure disait `bat_centrale_charbon`. C'est en cherchant pourquoi que j'ai trouvé la liste de
candidats du lot L4 — donc l'assertion fausse a produit le meilleur constat du lot (§5.4).

J'ai aussi retiré au passage une assertion **vacueuse** que j'avais laissée dans la première version
(`A.dimBuch === undefined || true`, toujours vraie). Une assertion qui ne peut pas échouer n'est pas
un test ; la vraie mesure des 32×32 est faite plus bas, en décodant l'image.

## 6. Écarts

| # | Écart | Raison |
|---|---|---|
| 1 | SHA de l'artefact ≠ `f8c545ec…` du brief | Bump obligatoire + 26ᵉ image. **Vérifié** : la variante patch-seul est byte-identique (§1). |
| 2 | **26 images au lieu de 25** | Ajout demandé par Ethan ; le motif d'exclusion du brief (« le pack ne fournit pas ce PNG ») est factuellement tombé (§4). |
| 3 | T5 étendu aux 11 falaises | Le brief ne teste que `_s`. |
| 4 | T7bis ajouté | Le brief demande « boot sur une partie réelle » ; j'atteins l'île 8 et fais tourner le tick complet. |
| 5 | Constat sur le foyer à charbon | Non mentionné par le brief, trouvé par une assertion fausse (§5.4, §5.6). |

## 7. Points ouverts

- **Contrôle visuel sur appareil, à faire par Ethan** — c'est la seule chose que ce banc ne peut pas
  faire. Arriver sur l'île et vérifier que l'herbe, la mer, le sable et **la canopée** scintillent,
  que les triangles de transition terre → sable apparaissent aux angles du littoral, que le foyer à
  charbon a bien **son** sprite (et non plus celui de la centrale), et que la bûcheronneuse posée
  mais jamais utilisée n'est plus un rectangle de couleur.
- **Les variantes `basalte` / `calcaire` / `gres_rouge`** des falaises restent dans le pack, non
  injectées : `retenu/` est l'arbitrage d'Ethan. Les rebasculer ne demanderait aucun code, seulement
  de changer le dossier source.
- **Hors périmètre, non touché** (lot suivant) : le panneau de la ferme, les champs contigus et
  indépendants, la carrière hors côte, la coupe à 100, la fusion des nœuds 48/49.
- **Toujours ouvert depuis le lot précédent** : le 0 kW de l'île est temporaire ; à son
  rétablissement, la conséquence de pontage du câble (scierie et filerie cessant de ponter)
  disparaîtra d'elle-même.
