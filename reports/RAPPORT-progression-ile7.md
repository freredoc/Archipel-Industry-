# RAPPORT — Lot « progression île 7 »

**Livré en `GAME_BUILD = 443` / `GAME_VERSION = 'Alpha 21.0'`.**
`SAVE_VERSION` **INCHANGÉ (31)** — voir §3, c'est vérifié au fichier et par un round-trip réel.

Base : build 442 / Alpha 20.9 · `src/index.src.html` `61414b7e50a0e453…` · artefact `024efc7d…`
(= `.build-stamp` de `main`). **Les trois empreintes de départ concordent avec le brief.**

---

## 0. Ce qu'il faut lire en premier

Le brief demande **Opus 5, effort élevé, plancher ferme** — c'est le modèle qui a exécuté. Aucune
réserve de modèle sur ce lot, contrairement au L3.

Trois choses méritent l'attention avant le détail :

1. **Le patcheur est byte-identique à celui du rédacteur.** Reconstruit sans le bump, l'artefact
   sort à `a265f2f1ac1cc67619427250efebcc01ac8ae7f50eee1faeb90f4e4b73a22802` — **exactement** le
   SHA annoncé par le brief. Le lot livré diffère seulement du bump de version, comme prévu (§1).
2. **Une conséquence de second ordre que le brief ne mentionne pas, mesurée et NON corrigée** :
   à 0 kW, scierie et filerie cessent aussi de **PONTER** le câble. Le brief annonce la perte du
   raccord visuel et du panneau Énergie ; il ne dit pas qu'un câble qui **traversait** ces
   bâtiments est désormais **coupé en deux**. Détail et mesure au §4.
3. **Trois défauts de mon banc, pas du patch**, trouvés et corrigés avant conclusion (§6.6). Le
   plus instructif : mon témoin de non-régression était faux — `presse_uhp` a `power: 0` **par
   construction** (elle a une sigmoïde). Il est remplacé par un **balayage exhaustif** des 112
   bâtiments `build`, comparé à la base : bien plus fort qu'un témoin choisi à la main.

---

## 1. Empreintes, tailles, blocs

| Grandeur | Valeur |
|---|---|
| `src/index.src.html` avant | 2 560 692 o |
| `src/index.src.html` après patcheur seul | **2 562 841 o** (**+2 149**) — *valeur du brief, au byte près* |
| `src/index.src.html` livré (patch + bump + bloc de version) | 2 566 690 o (+3 849 pour le bump) |
| Artefact avant | 3 989 439 o |
| Artefact livré | **3 995 437 o** (**+5 998**) |
| Blocs `^<script` | **7** (le build sort en erreur si ≠ 7) |
| SHA-256 de l'artefact livré, **ré-extrait du fichier produit** | `afdfce0c5febf798a7e4a040bc379c9cb136174b618b882e86a0fa4600203793` |
| `.build-stamp` | identique à la ligne ci-dessus |

### Écart de SHA avec le brief — attendu, et vérifié plutôt que supposé

Le brief annonce `a265f2f1ac1cc676…`. L'artefact livré porte `afdfce0c…`. C'est **normal** : le
brief pré-compile **avant** le choix du numéro de version, alors que la règle du lot impose de
bumper `GAME_BUILD` **et** `GAME_VERSION`, ce qui change le bloc 7 (bump + `GAME_NOTES` + bloc de
commentaire cumulatif).

Ce n'est pas une excuse, c'est une hypothèse testable — et elle a été testée. J'ai reconstruit la
variante **« patch seul, sans bump »** depuis `HEAD` :

```
SHA patch-seul  : a265f2f1ac1cc67619427250efebcc01ac8ae7f50eee1faeb90f4e4b73a22802
SHA du brief    : a265f2f1ac1cc67619427250efebcc01ac8ae7f50eee1faeb90f4e4b73a22802
=> CONFORME : patch byte-identique a celui du redacteur
```

et `src` patch-seul = 2 562 841 o, la taille du brief. **Le patch appliqué est celui du rédacteur,
au bit près.** L'état livré a ensuite été restauré et re-vérifié par SHA.

## 2. Les 14 ancres

Passage à blanc **avant** toute écriture (compteur seul, aucune substitution), puis application
réelle. Les deux passes donnent le même résultat.

| Ancre | `count` | Ancre | `count` |
|---|---|---|---|
| P1a — scierie `power: 64` → 0 | **1** | P3c — filtre du sélecteur de culture | **1** |
| P1b — filerie `power: 256` → 0 | **1** | P4a — nœud 45 → Scierie / 100 bois | **1** |
| P1c — `FARM_KW_PER_TILE` 2 → 0 | **1** | P4b — unlocks du 45 → `['scierie']` | **1** |
| P2a — `FARM_ZONE_BASE` 4 → 1 | **1** | P4c — nœud 46 → Agriculture / 100 planches | **1** |
| P2b — `FARM_ZONE_PER_LVL` 2 → 1 | **1** | P4d — nœud 47 + création des 48 et 49 | **1** |
| P3a — `FARM_CULT_NODE` + `farmCultUnlocked` | **1** | P5a — table `tech` des nœuds 45..49 ×4 langues | **1** |
| P3b — filtre de `unlockedResourceSet` | **1** | P5b — purge de l'ancienne entrée « Agriculture » | **1** |

**14/14 à `count == 1`.** Aucune ancre retapée. Le patcheur n'écrit rien si une seule échoue.

## 3. Contrôles avant application

Ce que j'ai vérifié **au fichier** avant de lancer le patcheur, parce que ce sont les points où un
lot de ce genre casse :

- **`isNodeConfirmed` existe** (l. 12821) et c'est un `const`. `farmCultUnlocked` est posé
  l. ~11356 et le lit — **1 466 lignes plus haut que sa déclaration**. C'est la zone morte
  temporelle du 14.95, celle qui donne une page blanche que `node --check` ne voit pas. **Sans
  danger ici, et pour une raison précise** : `farmCultUnlocked` est une *déclaration de fonction*
  (hoistée), son corps n'est évalué qu'à l'appel, et ses deux appelants
  (`unlockedResourceSet` l. 17891, sélecteur l. 22768) sont des chemins de **rendu**. J'ai vérifié
  qu'**aucun appelant de `unlockedResourceSet` ne tourne au chargement du module** (les 7 sites
  sont dans le rendu ou la boucle de jeu). ⚠ Ne jamais transformer ce prédicat en `const` ni
  l'appeler au chargement.
- **Aucun code ne lit `TECH_NODES.length`** (grep vide) → ajouter deux nœuds ne casse aucune
  hypothèse de longueur.
- **`loadSave` reconstruit `techTree.nodes`** par `TECH_NODES.map` avec `savedStatus[n.id]`
  (défaut `locked`), `savedStatus` étant un simple map par id (l. 27134-27135). **C'est ce qui rend
  l'ajout de deux nœuds gratuit** et justifie que `SAVE_VERSION` ne bouge pas. Prouvé par T2.
- **Le masquage de l'arbre de recherche** (`masqueSeuil`, l. ~23236) plafonne à 27 ou 28 selon les
  nœuds 25/28. Les nœuds 48-49 arrivent bien après 43 : au moment où le joueur les atteint, le
  masquage est levé depuis longtemps. Aucun risque de masquer un nœud atteignable.
- **`FARM_ART_KEYS` est une IIFE qui tourne au chargement** et dérive ses 19 clés de
  `FARM_CULT_KEYS`. C'est exactement pourquoi la table ne doit pas être filtrée. Vérifié : elle ne
  l'est pas, et l'art reste à 19 (T3).

## 4. La conséquence de second ordre — signalée, mesurée, NON corrigée

Le brief documente honnêtement la conséquence du 0 kW : `isEnergyConsumer` vaut
`(power||0)>0 || sigmoid || randomP`, donc scierie et filerie quittent le panneau Énergie et le
câble ne dessine plus de branche vers elles.

**Ce que le brief ne dit pas** : la branche `wire` de `buildingConnectsCarrier` porte la **même
condition**, et `'wire'` appartient à **`BRIDGE_CARRIERS`** (l. 3979). Ces deux bâtiments cessent
donc aussi de **PONTER** le câble — un câble posé *à travers* une scierie est désormais **coupé en
deux réseaux distincts**.

C'est la même classe de défaut que la fonderie d'or perdant son acide (14.19) et la centrifugeuse
(13.25), toutes deux consignées au mémo. Mesuré aux deux bouts :

```
T4 SECOND ORDRE MESURE : la scierie ne PONTE plus le cable — patch=false base=true
T4 SECOND ORDRE MESURE : la filerie non plus            — patch=false base=true
T4 TEMOIN : machine_outil PONTE toujours                — patch=true  base=true
```

**Pourquoi je ne le corrige pas** : le 0 kW est une décision explicite d'Ethan, déclarée
temporaire, et le corriger reviendrait à toucher la mécanique de pontage — hors périmètre. **Sans
effet avant le nœud 48** (l'île n'a aucune source de courant, donc aucun câble, avant le foyer) ;
visible seulement après. **Il disparaît de lui-même** quand les vraies valeurs seront rétablies.

## 5. Ce que le lot débloque

La chaîne livrait le **charbon avant la scierie**. Le nœud qui exige 100 planches ne pouvait donc
être payé qu'après la scierie, elle-même débloquée plus tard — et la scierie tirait 64 kW alors que
le foyer à charbon est la **seule** source de courant de l'île. **La progression se bloquait sur
elle-même.** Nouvel ordre, vérifié en jeu (T1) :

| Nœud | Nom (fr) | Livraison | Débloque |
|---|---|---|---|
| 45 | Scierie | 100 bois | `scierie` |
| 46 | Agriculture | 100 planches | `ferme` |
| 47 | Charbonnière | 100 planches | `charbonniere` |
| 48 | Foyer à Charbon | 100 charbon | `foyer_charbon` |
| 49 | Filerie de Carbone | 200 charbon | `filerie_carbone` |

Chaînage `prereq` 44→45→46→47→48→49, `mode: 'delivery'`, `island: 8` sur les cinq.

## 6. Validation — 63 PASS / 0 KO, deux passes identiques

Pilote : `playwright-core` + Chromium 1194, servi en **HTTP depuis la racine du dépôt**, jamais
`file://`. La base 442 est servie **en parallèle** pour les contre-épreuves.

### 6.1 `node --check` — 7 blocs × 3 variantes CI

```
  public : 7 blocs extraits, node --check 7/7 OK
  dev    : 7 blocs extraits, node --check 7/7 OK
  store  : 7 blocs extraits, node --check 7/7 OK
```

Extraction **séquentielle** (on repart après chaque `</script>`), dossier de sortie **purgé** et
**compté avant de boucler** : c'est le piège du 19.7, où une boucle annonce « 7/7 » sur un dossier
qui n'en contient qu'un. Le banc refuse de conclure si le compte ≠ 7.

Gardes CI rejouées à la main : `DEV_BUILD` true/false selon la variante, `SELF_UPDATE = true` en
publique, `ko-fi` **= 1** en publique et **= 0** en magasin, `const SELF_UPDATE = true;` **= 0** en
magasin. Toutes conformes.

### 6.2 T1 — la chaîne, en français **et en anglais** — PASS (24 assertions)

Les cinq nœuds sortent avec le bon nom, le bon `prereq`, la bonne livraison, le bon déblocage et
`island: 8`, **dans les deux langues**. La lecture se fait sur la donnée **vive**, après
`I18N.applyToData` — c'est-à-dire ce que le joueur voit, pas le littéral inline.

L'anglais est la langue qui avait révélé le défaut au rédacteur ; il est vert :
`Sawmill / Agriculture / Charcoal Kiln / Coal Hearth / Carbon Spinnery`.

**Contre-épreuve sur la base 442** — c'est elle qui rend T1 falsifiable :

```
PASS T1 contre-epreuve base442 : n45 porte l'ANCIEN nom — "Fire and Charcoal"
PASS T1 contre-epreuve base442 : aucun noeud 48 — absent
```

### 6.3 T2 — aucune migration — PASS (6 assertions)

Montage : partie réelle, sauvegarde forcée **par le vrai chemin** (`saveTimer` armé +
`visibilityState` redéfini à `'hidden'` — `flushSave` est une closure, dispatcher l'événement seul
ne suffit pas), puis `techTree.nodes` **tronqué à 43 entrées** dans le JSON du slot, réinjection
des trois clés de slot via `addInitScript`, rechargement réel.

```
PASS T2 montage : save reelle tronquee a 43 entrees — 49 -> 43 entrees, version 31
PASS T2 nodes.length === 49 — 49
PASS T2 noeud 48 locked · PASS T2 noeud 49 locked
PASS T2 aucune erreur au chargement (hors bruit preexistant)
PASS T2 SAVE_VERSION inchange (31) — 31
```

### 6.4 T3 — les cultures — PASS (9 assertions)

```
PASS FARM_ART_KEYS.length === 19 (art COMPLET) · PASS FARM_CULT_KEYS.length === 4 (table NON filtree)
PASS avant 48 -> ["foret"] · PASS apres 48 -> ["foret","taillis"] · PASS apres 49 -> les quatre
PASS remise a locked -> ["foret"]
```

**Ajout hors brief** : le brief ne teste que le **sélecteur**. Or le patch pose le même verrou sur
l'**inventaire** (`unlockedResourceSet`) — un site qui, non gardé, annoncerait `biomasse` et
`latex` avant qu'on puisse les semer. Testé aussi :

```
PASS T3 montage inventaire : ferme debloquee — true
PASS T3 inventaire : biomasse/latex ABSENTS avant 48-49 — biomasse=false latex=false
PASS T3 inventaire : biomasse/latex PRESENTS apres 48-49 — biomasse=true latex=true
```

Statuts remis à `locked` en fin de test, comme le brief l'exige.

### 6.5 T4 — 0 kW et zone — PASS (19 assertions)

Les cinq valeurs du brief sont conformes (`scierie 0`, `filerie 0`, `FARM_KW_PER_TILE 0`,
`farmZoneMax(0) = 1`, `farmZoneMax(3) = 4`), contre-épreuvées sur la base (`64 / 256 / 2 / 4 / 10`).

**Le témoin du brief est remplacé par un balayage exhaustif** (voir §6.6) :

```
PASS T4 meme nombre de batiments `build` que la base — 112 vs 112
PASS T4 le 0 kW n'a touche QUE scierie et filerie (balayage exhaustif)
        — ajoutes=[filerie_carbone,scierie] retires=[]
```

Sur **112** bâtiments `build`, l'ensemble de ceux à `power` nul gagne exactement deux entrées et
n'en perd aucune. Le continent ne perd rien.

### 6.6 T5 — non-régression moteur — PASS

1 500 ticks (`onTick` **et** `tickShips` — le vrai couple de la boucle de jeu) sur une partie
menée jusqu'à l'île 8 par le vrai chemin : **aucune exception**, `tickErrors` vide, aucune erreur
console. 1 055 ms.

### 6.7 Mes trois défauts de banc, et ce qu'ils apprennent

Les trois premiers KO venaient de mon banc. Je les consigne parce que deux d'entre eux sont des
pièges reproductibles :

1. **Ordre de lecture.** Je lisais `isBuildingUnlocked(g,'ferme')` **après** avoir remis tous les
   nœuds à `locked` : l'assertion mesurait la remise à zéro, pas le montage.
2. **Témoin de non-régression faux.** J'avais pris `presse_uhp.power > 0` comme témoin « le
   continent ne perd rien ». Or `presse_uhp` a `power: 0` **par construction** — sa conso est une
   **sigmoïde** (128→1024, cf. 13.79). Le témoin échouait sur une valeur parfaitement saine.
   **Un témoin choisi à la main peut être faux ; un balayage exhaustif, non.** Remplacé par la
   comparaison ensembliste des 112 défs `build`.
3. **Contre-épreuve non déterministe.** J'exigeais que la base 442 émette *aussi* un 404 pour
   prouver que le bruit est préexistant. Chromium met le favicon en cache **par origine** : le 404
   n'apparaît que dans le premier contexte de la session, donc l'assertion dépendait de l'ordre des
   tests. Remplacée par deux propriétés déterministes : **le filtre ne matche aucun message d'erreur
   réel** (trois témoins : `TypeError`, `ReferenceError`, `reading 'kind'`), et le compte filtré
   vaut 0 des deux côtés. Un filtre de bruit doit être prouvé incapable de masquer un vrai défaut.

## 7. Écarts et incidents

| # | Écart | Raison |
|---|---|---|
| 1 | SHA de l'artefact ≠ `a265f2f1…` du brief | Le brief pré-compile avant le choix du numéro. **Vérifié** en reconstruisant la variante patch-seul : byte-identique (§1). |
| 2 | Conséquence de pontage non mentionnée par le brief | Mesurée et signalée, **non corrigée** : elle découle de P1, décision explicite et temporaire (§4). |
| 3 | T3 étendu à l'inventaire | Le patch garde **deux** sites ; le brief n'en teste qu'un. |
| 4 | Témoin T4 remplacé par un balayage exhaustif | Le témoin du brief (`machine_outil`) est conservé et vert ; celui que j'avais ajouté était faux (§6.6). |
| 5 | T5 exécute `onTick` **et** `tickShips` | La vraie boucle appelle les deux ; n'appeler qu'`onTick` teste une demi-boucle. |
| 6 | **Incident de banc : `tools/split-once.js`** | J'ai chaîné cet outil sans l'inspecter pour extraire les blocs. C'est un **outil de migration à usage unique qui RÉÉCRIT `src/index.src.html` et `src/sprites-inline.js`** — il a écrasé les deux. Restaurés (sprites depuis `HEAD`, source depuis ma copie), reconstruits, et **l'artefact est ressorti au SHA attendu `afdfce0c…`** : aucun impact sur le livrable, prouvé par empreinte. Extraction refaite avec un extracteur dédié. **À retenir : ne jamais lancer `tools/split-once.js`, il n'est pas un extracteur.** |

## 8. Points ouverts — à équilibrer plus tard, pas dans ce lot

Repris du brief, plus ce que le lot a fait apparaître :

- **Les nœuds 46 et 47 demandent tous deux 100 planches.** Chiffre de remplissage, posé pour que la
  chaîne tourne. Ethan a dit « à refaire et équilibrer ensuite ».
- **La ferme coûte 300 planches** alors que le nœud qui la débloque n'en exige que 100.
- **Le 0 kW est temporaire.** Rétablir scierie 64, filerie 256, `FARM_KW_PER_TILE` 2. À ce
  moment-là **seulement**, l'ordre charbon-avant-scierie redeviendrait obligatoire — et la
  conséquence de pontage du §4 disparaîtra d'elle-même.
- **La légumineuse est ignorée, par décision d'Ethan.** À une clairière au niveau 0 elle n'a aucune
  voisine 4-adjacente : son bonus de +25 % par voisine ne mord pas avant le niveau 3.
- **Aucun effet rétroactif du verrou de culture** (rappel) : une ferme portant déjà
  `cult: 'hevea'` continue de tourner. Le prédicat garde l'**ouverture** d'une culture, pas
  l'exécution d'un cycle en cours. C'est voulu ; à trancher si le playtest le juge incohérent.
- **Non couvert ici** : aucun test sur appareil, et aucune partie jouée de bout en bout sur la
  nouvelle chaîne 45→49 (T1 lit la table, il ne franchit pas les cinq livraisons). Le brief ne le
  demandait pas ; c'est le playtest qui tranchera l'ordre et les quantités.
