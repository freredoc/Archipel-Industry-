# RAPPORT — Lot L8 : découvrabilité de la libération d'île

## Version produite

| | valeur |
|---|---|
| `GAME_BUILD` | 393 → **394** |
| `GAME_VERSION` | Alpha 16.0 → **Alpha 16.1** |
| `SAVE_VERSION` | **31, inchangé** — aucun champ ajouté |
| Base | build 393, SHA-256 `cbebae793bb81fe97f67e18b7a4c49d18e2338bdfe5ba9153e6dd09943880f19` (**conforme au brief**) |
| Fichier livré | SHA-256 `148eee3530a2f892c22eb11fe4372f2b37e3e35658f00a9af39fee271206ba38`, **3 427 717 o** |

## Sortie du patcheur

```
OK - 5 ancres appliquees
SHA-256 fichier patche : 28f2bc755b66a07eeed68c6fb0caee6d3394ec6086ec821fa7f1bd31746cc892
```

**Conforme au caractère près** : le SHA attendu par le brief (`28f2bc75…`) est retrouvé à l'identique,
et la taille intermédiaire tombe sur **3 424 661 o**, soit le **+3 370** annoncé. Les 5 ancres sont
sorties à `count == 1` sans adaptation.

**Contrôle de conformité fait AVANT le bump**, seul moment où il a un sens : les **7 blocs `<script>`
étaient tous identiques au brief**, y compris le bloc 6 inchangé (`f6cdea55…`) et le seul bloc qui
bouge, le 7 (`d5d8b90f…`, 1 672 702 o).

## SHA-256 des 7 blocs, ré-extraits APRÈS la toute dernière modification

Extraction par `(?m)^<script` — balise en **début de ligne**. Un compteur naïf en rend 11 : quatre
occurrences sont textuelles (une chaîne du UMD React, trois commentaires), invisibles à une
extraction séquentielle.

| bloc | octets | sha256 |
|---|---|---|
| 1 | 418 | `a50c1c4e7f4a304c650c0cfa7e06c4ffdfb0d87510d83738114d4dd5a1641628` |
| 2 | 4 397 | `8fbb22187703339c146b2f82badd8701d199128cd3d9dd8673d213c8616ca541` |
| 3 | 10 751 | `d949f1c3687aedadcedac85261865f29b17cd273997e7f6b2bfc53b2f9d4c4dd` |
| 4 | 131 835 | `35f4f974f4b2bcd44da73963347f8952e341f83909e4498227d4e26b98f66f0d` |
| 5 | 1 111 572 | `eb79498a11d3c700bca3e23a24b4e5312302e19340c2e9cf32ef0e94a70624e2` |
| 6 | 239 836 | `f6cdea55af87528fdb0faa6d73d1cf2367219509802364d6d0226252832c73d9` |
| **7** | **1 675 758** | **`f18a52c4dd04ab2e40cdf9f442eb006b1ac4ff294a3b0b324ae787978fc9d6a5`** |

`node --check` : **7/7 OK**. Ces empreintes sont mesurées **après** la rectification de commentaire
décrite plus bas — c'est le fichier réellement livré, pas un état intermédiaire.

## Delta d'octets

| étape | taille | delta |
|---|---|---|
| base 393 | 3 421 291 | — |
| patch seul | 3 424 661 | **+3 370** (l'attendu exact) |
| + bump, commentaire cumulatif, `GAME_NOTES`, rectification | 3 427 717 | +3 056 |
| **total** | | **+6 426** |

## Validation — 8/8, contre-épreuve 4/4, contrôles finaux 5/5

**Montage.** Chromium 1194, `--no-sandbox --disable-setuid-sandbox --disable-dev-shm-usage`,
viewport 880×1600, serveur HTTP lancé depuis la racine du dépôt, langue forcée à `fr`. Copie de banc
`BANC_394.html` = fichier livré + poignée **paresseuse** `window.__H = () => ({ popup:
setResearchPopup, skipGuide })` injectée dans le corps de `frame` (une fonction, jamais un objet
littéral : la leçon du lot L2 sur la zone morte temporelle). **Cette copie n'est pas dans la PR** —
contrôle F2 ci-dessous. `freedByNode`, `freedScopeLabel`, `applyUnlocks`, `TECH_BY_ID`, `BUILDINGS`
et `islandLabel` sont des déclarations de module : atteignables par leur **nom nu** dans
`page.evaluate`, aucune poignée nécessaire.

⚠ **Écart de montage assumé** : le brief travaille sur la sauvegarde de fin de partie d'Ethan, que
je n'ai pas. La suite part d'une **partie neuve** dont je force l'état — tutoriel passé par le VRAI
bouton `.tuto-skip` (la Toolbar lit le *state* React, pas le champ de jeu), puis les 43 nœuds
confirmés par `applyUnlocks`. C'est plus exigeant que la save d'Ethan sur un point : les trois
bâtiments partent **non vus**, donc V4 pose explicitement les trois clés à `true` avant de mesurer.

| # | test | montage effectif | valeurs relevées | verdict |
|---|---|---|---|---|
| **V1** | Boot | banc 394, 6 s de jeu | build **394** · `Alpha 16.1` · SAVE **31** · canvas **100 %** · splash retiré · **0 `pageerror`** | PASS |
| **V2** | `freedByNode` | appel direct | 39 → **`['four_arc_tungstene']`** · 41 → **`['machine_outil']`** · 43 → **`['separateur_cryogenique']`** · 37 → **`[]`** | PASS |
| **V3** | `freedScopeLabel` | appel direct | four **« toutes les îles »** · machine-outil **« toutes les îles »** · cryo **« Île 6 · Île 6 S »** · `islandLabel(7)` = « Île 6 S » | PASS |
| **V4** | Réarmement **ciblé** | 3 clés à `true`, puis `applyUnlocks(g, TECH_BY_ID[39])` | avant `{four:true, machine:true, cryo:true}` → après **`{four:false, machine:true, cryo:true}`** | PASS |
| **V5** | 3ᵉ cas | puis `applyUnlocks(g, TECH_BY_ID[43])` | **`{four:false, machine:true, cryo:false}`** | PASS |
| **V6** | Fiche détaillée | menu Bâtiment ouvert, vignette retrouvée **par son nom affiché** (93 `.tool-btn`), `scrollIntoView`, **appui long réel de 700 ms** | fiche ouverte, **« CONSTRUCTIBLE toutes les îles — libéré par « Collisionneur P1 » »**, **aucune ligne « EXCLUSIF »**, **0 `pageerror`** | PASS |
| **V7** | Popup de recherche | `setResearchPopup({id:39, name, unlocks})` par la poignée | **2** lignes `.rd-unlocks` : « Débloque : Fonderie Or V2 · Raffineur Si V2 · Fab. Processeur V2 » **et** « 🔓 Libère : Four à Arc Tungstène → toutes les îles » | PASS |
| **V8** | Non-régression | même geste sur le nœud 37 | **1** seule ligne (« Débloque : Refroidisseur »), **aucune** ligne « Libère », `freedByNode(37).length === 0` | PASS |

**Suite rejouée 2 fois : 8/8 les deux fois, sans flottement.**

**V4 est la preuve de précision du lot** : le même appel réarme **exactement un** bâtiment et laisse
les deux autres intacts. Un réarmement global aurait passé un test « le badge revient » tout en
inondant le menu de pastilles sans rapport.

**V6 est la sentinelle.** C'est le seul test qui exerce le composant à l'écran. `node --check` et le
boot passaient sur la version du patch qui lisait `detailId` au lieu de `id` — la fiche, elle,
plantait à l'ouverture. J'ai vérifié la signature au fichier avant de tester :
`function BuildingDetailModal({ id, cryoOk, islandFreed, onClose })`, et le site d'appel passe bien
`id: detailId`. Le patch livré lit `id` : correct.

### Contre-épreuve — les mêmes gestes sur la BASE 393 : 4 verdicts opposés

| # | mesure sur la base non patchée |
|---|---|
| C1 | `typeof freedByNode` et `typeof freedScopeLabel` = **`"undefined"`** (build 393 confirmé) |
| C2 | `applyUnlocks(39)` **puis** `(43)` : les **trois** badges restent `true` — aucun réarmement |
| C3 | fiche du Four **libéré** : **ni « Exclusif » ni « Constructible »** — c'est le **silence**, le défaut même du lot |
| C4 | popup du nœud 39 : **une seule** ligne `.rd-unlocks`, aucune ligne « Libère » |

Contre-épreuve **rejouée 2 fois : 4/4**. C3 est celui qui compte : il montre que sur la base le
joueur n'obtient **rien du tout** là où le patch répond.

⚠ **Le `typeof` de C1 est volontaire et n'est pas un raccourci** : `typeof` sur un symbole non
déclaré rend `"undefined"` **sans lever**. Une contre-épreuve qui attendrait une `ReferenceError`
échouerait précisément là où le symbole est bien absent.

### Contrôles finaux — 5/5

| # | contrôle | relevé |
|---|---|---|
| **F1** | Boot du **fichier livré** (pas le banc), viewport 420 px | build 394 · `Alpha 16.1` · SAVE 31 · canvas **100 %** · `playTicks` 6 → 9 (l'horloge avance) · `tickErrors` nul · **0 `pageerror`** |
| **F2** | Poignées de banc absentes du livrable | `typeof window.__G` et `typeof window.__H` = **`undefined`** |
| **F3** | Débordement de la ligne « Libère » à **360 px** | popup 331 px dans un viewport de 360 ; les 2 lignes font 285 px, `scrollWidth == width`, dépassement **−23 px** (donc à l'intérieur). **Aucun débordement** |
| **F4** | Le cadenas 🔓 | `UI_ICON_BY_EMOJI['🔓']` = **`deverrouille`** : le sprite existe, `iconLabel` rend une vraie image et non l'emoji |
| **F5** | Libellé du séparateur au nœud 43 | « Séparateur Cryogénique → **Île 6 · Île 6 S** », et **jamais** « toutes les îles » |

## Écart par rapport au brief, assumé et signalé

**Un commentaire rendu faux par le patch a été rectifié** (hors des 5 ancres, donc après le contrôle
de conformité). Au-dessus de la branche « Exclusif » de `BuildingDetailModal`, le commentaire du
build 14.4x affirmait :

> une fois le nœud de libération confirmé, la ligne « Exclusif » **DISPARAÎT** (le bâtiment se
> construit **partout**)

Les **deux** moitiés sont devenues fausses le jour du patch : la ligne ne disparaît plus (c'est
précisément l'objet du lot) et « partout » ne vaut pas pour le Séparateur Cryogénique, qui garde son
`forbiddenIslands`. Laissé tel quel, ce commentaire aurait menti sur le code écrit **deux lignes plus
bas** — exactement le piège que le lot 14.77 documente. Il est réécrit, avec mention explicite de la
rectification et de ce que le texte d'origine décrivait correctement à son build. C'est la seule
modification hors patcheur et hors versionnage.

## Points signalés, non corrigés

- **Quatre libellés ne sont pas traduits** — « Libère », « Constructible », « toutes les îles » et la
  flèche de portée : repli français dans les 4 autres langues. Ils rejoignent le lot i18n de
  l'audit 381.
- **La ligne « Libère » réutilise la classe `rd-unlocks`** sans nouvelle règle CSS, comme le veut le
  brief. **Mesuré à 360 px** (F3) : aucun débordement, ni du popup ni du viewport. La feuille de
  style n'est pas touchée.
- **Le réarmement du badge n'est pas rétroactif.** Il s'applique au moment de la confirmation : une
  partie où le nœud est **déjà** validé ne verra jamais la pastille. C'est voulu — le signal
  accompagne l'événement. Les deux autres signaux, eux, fonctionnent quel que soit l'historique : le
  popup à la validation, et surtout la ligne de la fiche, qui est **la seule à répondre encore trois
  jours plus tard**. C'est la raison d'être de la redondance des trois signaux.
- **`freedByNode` parcourt `BUILDINGS` à chaque appel.** Appelée au clic et à la confirmation, jamais
  dans le tick. **Aucun profilage n'a été fait** et aucune mémoïsation n'a été ajoutée : la rapporter
  plutôt que d'optimiser sans mesure.
- **La liste dérive de `exclusiveUntilNode`, jamais d'une table en dur** (contrôle inclus dans V2 :
  les seuls porteurs du champ sont bien les trois attendus). Tout bâtiment futur qui portera ce champ
  sera couvert sans une ligne de code de plus.

## Pièges de banc payés

- **Confirmer 43 nœuds remplit `researchNotify`** : la boucle de frame en dépile un par frame et
  ouvre à chaque fois un `.rd-popup` dont le `.research-backdrop` **recouvre tout le HUD**. Fermer à
  la main ne suffit pas, la file se recharge — il faut la **vider**.
- **Débloquer tout l'arbre remplit la file d'astuces** (13.80). Couper `tipsEnabled` n'empêche que
  les **nouvelles** ; celle déjà ouverte reste, et sa fermeture **dépile la suivante**. Son backdrop
  avale l'appui long, et l'on conclut à tort que la fiche ne s'ouvre pas — c'est exactement le faux
  KO que j'ai eu sur V6 au premier passage. Remède : boucler jusqu'à disparition **réelle**, par vrai
  clic sur `.tip-ok` (jamais `.tip-dismiss`, qui désactive les astuces).
- **Le popup de recherche se ferme par le SETTER, pas par un clic sur le backdrop** : `useGhostGuard`
  avale le clic tant qu'aucun `pointerdown` interne n'a eu lieu.
- **Le geste a dû être rendu RETENTANT** : une astuce peut se dépiler entre la purge et l'appui long.
  Le banc vérifie désormais par `elementFromPoint` que le point visé appartient bien à un `.tool-btn`
  avant de presser, et réessaie jusqu'à trois fois. Sans cela la contre-épreuve C3 a flotté une fois
  sur deux — **un test instable ne prouve rien**, et il aurait tout aussi bien pu masquer un vrai
  défaut.

## Livraison

- **PR ouverte, NON mergée** — le merge sur `main` appartient à Ethan.
- Aucun artefact de banc dans le dépôt : les copies `BANC_*.html` et les blocs extraits vivent dans
  `/tmp`, et le commit stage les fichiers **par chemin explicite**.
