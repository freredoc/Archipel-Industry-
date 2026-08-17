# RAPPORT — Lot 4a : le moteur de paiement des réparations de port

**Livré : `GAME_BUILD = 398`, `GAME_VERSION = 'Alpha 16.5'`, `SAVE_VERSION = 31` (INCHANGÉ, vérifié).**

Base d'exécution : build **397 / Alpha 16.4**, SHA-256
`69b909a48c10b4aeb63bb348157fe6ab896f51ed058ddb76d7d5429260aaf025`, 3 446 584 o — **identique** à la
base annoncée par le brief. Branche repartie de `main` après le merge du lot 3 (PR #380).

---

## 1. Application du patch

`patch-lot4a.py`, **5 ancres sur 5**, chacune à `count == 1` :

```
A helper deliveryIsland OK      D ResearchPanel    OK
B techDeliver           OK      E RepairModal      OK
C deliveryReady         OK
Ecrit : 3446584 -> 3448659 octets
```

**Delta : +2 075 o**, et le fichier patché **avant bump** porte
**`a7ed675434695f0a2f99e65d76684e4203f4ed86f3e9005e01bd71511ab056e6`** — **identique au binaire près
au repère du brief** (3 448 659 o attendus, 3 448 659 o obtenus).

**Idempotence** : seconde passe → « Aucun changement (patch déjà appliqué) », 5 sentinelles
détectées, 0 octet écrit.

Taille finale après bump : **3 451 366 o** (+2 707 o de commentaire cumulatif et de `GAME_NOTES`).

---

## 2. Vérifications faites avant d'appliquer

Deux affirmations du brief ont été **contrôlées à la source** plutôt que crues :

| affirmation | vérification |
|---|---|
| `ISLAND_ACCESS_NODE` est déclaré **151 lignes après** `techDeliver` | **exact** : `techDeliver` l. 17320, `const ISLAND_ACCESS_NODE` l. 17471 → **+151**. Le choix « boucle au runtime, pas de `const` dérivée » est donc justifié : une table dérivée posée près de `techDeliver` lirait `ISLAND_ACCESS_NODE` avant sa déclaration → zone morte temporelle, page blanche invisible à `node --check`. |
| les 5 nœuds d'accès n'ont **pas** de champ `island` racine | **exact, mais vérifié au RUNTIME** — voir l'avertissement ci-dessous |

⚠ **PIÈGE ÉVITÉ, à consigner** : mon premier relevé statique par expression régulière annonçait
`island: 3` pour le nœud 8 et `island: 6` pour le nœud 31 — **ce sont des `island` de `reqs`**, pas
du nœud. Les deux champs portent le même nom dans deux portées distinctes (piège déjà documenté au
build 14.91). Relevé **au runtime**, seule source fiable :

```
ISLAND_ACCESS_NODE = {"2":2,"3":8,"4":14,"5":21,"6":28}
nœud  2 : island=ABSENT · paie l'île 1 · delivery={"lingot_fer":10000,"ciment":10000}
nœud  8 : island=ABSENT · paie l'île 2 · delivery={"acier":50000,"cable":50000}
nœud 31 : island=ABSENT · paie l'île COURANTE (hors table)
nœud 35 : island=7      · paie l'île du champ
```

---

## 3. Contrôles statiques

- **`node --check` : 7/7**, avant ET après le bump. Scanner naïf = 11 correspondances, scanner
  ancré `(?m)^<script` = **7** blocs réels.
- **`SAVE_VERSION` toujours à 31.**
- **Aucun libellé nouveau** : le diff ne contient **0** ajout de `I18N.t(` — l'audit i18n 381
  n'augmente pas d'une ligne, comme le brief l'annonçait.
- `GAME_NOTES` : **636 caractères** extraits par la regex de la CI, chaîne complète, accents
  littéraux, **0 séquence `\u`**, aucun guillemet droit.

### SHA-256 des 7 blocs, re-extraits APRÈS la toute dernière modification

Fichier entier : **`3042e46ceaf0c8c93447e6295ade09d74c014546be4247654dfce3fce9cbf671`** — 3 451 366 o.

| bloc | SHA-256 (16 car.) | taille |
|---|---|---|
| blk1 | `a50c1c4e7f4a304c` | 418 |
| blk2 | `8fbb22187703339c` | 4 397 |
| blk3 | `d949f1c3687aedad` | 10 751 |
| blk4 | `35f4f974f4b2bcd4` | 131 835 |
| blk5 | `1be53ce44e7be14f` | 1 113 969 |
| blk6 | `188ef1b636036af0` | 240 694 |
| blk7 | `c644dd0d5d0b7480` | 1 692 679 |

---

## 4. Banc de logique

`test-lot4a.js` était joint, **mais pas le `harness_core.js` qu'il exige**. Je l'ai donc **construit**
: extraction de `ISLAND_ACCESS_NODE`, `portPool`, `deliveryIsland`, `techDeliver` et `deliveryReady`
**verbatim depuis le fichier patché** (jamais réimplémentés), par un scanner d'accolades conscient
des chaînes **et des commentaires** — le fichier contient des apostrophes françaises en commentaire,
qui font dérailler un compteur naïf (piège du build 14.91).

Résultat : **22 assertions, TOUT PASSE**, et **rejoué après le bump** : toujours vert.

---

## 5. Le boot navigateur — ce que le brief n'avait pas pu faire

Le brief signale que le boot réel n'a pas pu être exécuté en amont (Chromium bloqué par le proxy) et
le déclare **obligatoire**. Il a été fait : Chromium headless, 430 × 820, locale `fr`, page servie
depuis la racine du dépôt, astuces fermées **par `.tip-ok`** et jamais par `remove()`, clics par
**vraie souris** (`locator.click()`), jamais `el.click()` ni appel direct.

| # | verdict | montage effectif et valeurs mesurées |
|---|---|---|
| **T1** | **PASS** | Joueur **île 1**, nœud 8 forcé `condition_ok`, port île 1 **vidé**, port île 2 = coût exact (50 000 acier + 50 000 câble). Bouton **« Livrer » actif**, **0 pastille rouge sur 2**. Après clic : nœud **`confirmed`**, port île 2 → `{acier: 0, cable: 0}`, **port île 1 intact `{}`** |
| **T2** | **PASS** | L'inverse : port île 1 **plein**, port île 2 **vide**. Bouton **grisé**, **2 pastilles rouges sur 2** affichant **« acier 0/50 000 »** et **« câble 0/50 000 »** — l'écran lit bien le port PAYEUR. Nœud resté `condition_ok`, **port île 1 rigoureusement intact**, `deliveryReady = false` |
| **T3** | **PASS** | Nœud 2 (accès île 2) depuis l'**île 1** : l'île payeuse **est** l'île courante → **comportement strictement inchangé**. Bouton actif, nœud `confirmed`, port île 1 débité, île 2 débloquée |
| **T4** | **PASS** | Joueur **au souterrain**, élévateur réparé, nœud 31 forcé, port île 6 au coût exact. Livraison acceptée, débit sur `game.port[6]`, et **le port interne du souterrain n'est JAMAIS créé** — `portPool` fait son office, non-régression confirmée |
| **T5** | **PASS** | Nœud 35 (`island: 7`) depuis l'**île 1**, les deux ports garnis. Bouton **grisé**, mention **« À livrer depuis l'Île 6 S »** conservée, `deliveryReady = false`. **La garde de présence n'a pas bougé** |
| **T6** | **PASS** | Nœud 8 forcé, joueur **île 1**, les deux ports vides → `deliveryReady = false`, **pastille éteinte**. Le port de l'île 2 est garni **sans que le joueur bouge** → `deliveryReady = true`, **pastille allumée**. C'est le changement voulu |
| **T7** | **PASS** | Modale Réparer ouverte depuis l'île 1 (nœud 2 prêt, port île 1 à 12 345 de chaque). La source affichée **est la même référence** que `game.port[1]` (`===` vrai) — la modale ne lit plus `game.port[…]` figé, donc **stock affiché et stock débité ne peuvent plus diverger** |

**0 `pageerror`** sur l'ensemble. Seule erreur console : le **404 unique** du serveur de test, bruit
préexistant documenté depuis le build 14.47.

**T2 est la contre-épreuve qui donne sa valeur à T1** : même nœud, mêmes montants, ports intervertis
→ verdict opposé sur le bouton, sur les pastilles ET sur le moteur.

---

## 6. Écarts par rapport au brief, et trois pièges de banc

1. **`harness_core.js` manquant** (§4) : construit par extraction, pas réimplémenté.
2. **Branche `claude/carte-archipel-wmyxbs`** (consigne de session), et non une branche dédiée.
3. **Numéro choisi à l'exécution** : 398 / Alpha 16.5, le dépôt étant à 397.

Trois pièges rencontrés, **tous dans mon harnais, aucun défaut produit** :

- **`[data-tut="confirm"]` existe sur PLUSIEURS lignes** du panneau (le nœud 2 est `condition_ok`
  dès le premier tick). Un `.first()` cliquait le mauvais nœud et rendait un « bouton désactivé »
  parfaitement trompeur. Corrigé en ciblant la ligne par `.rp-name`, qui porte `"<id>. <nom>"`.
- **Le bouton Recherche est légitimement VERROUILLÉ quand le port payeur est vide.**
  `researchPanelUnlocked` s'appuie sur le même `deliveryReady` : montage T2 initial → panneau
  injoignable. **Ce n'est pas un défaut**, c'est la cohérence du gate ; il a fallu débloquer le
  panneau autrement.
- **`researchPanelUnlocked` exclut EXPLICITEMENT le nœud 1** (`n.id !== 1`, nœud de départ toujours
  confirmé) : confirmer le nœud 1 pour ouvrir le panneau ne marche pas. Il faut un nœud d'id ≠ 1.

---

## 7. Points en suspens

- **Conséquence pour le joueur, écrite dans `GAME_NOTES`** : les ressources d'une réparation doivent
  désormais être **acheminées par bateau** jusqu'au port de l'île précédente. Les avoir ailleurs ne
  suffit plus. En contrepartie, il n'a plus besoin d'être sur place.
- **`RepairModal` est corrigé mais son correctif reste inobservable aujourd'hui** : `repairInfo`
  n'est construit que pour `currentIsland + 1`, donc l'île payeuse **est** l'île courante quand la
  modale s'ouvre. T7 le vérifie par l'**identité de référence**, seule propriété observable à ce
  stade. Le correctif prendra son sens quand le lot 4b ouvrira cette action depuis la carte.
- **Le libellé « quel port paie » n'existe pas** : rien à l'écran ne dit au joueur que c'est le port
  de l'île précédente qui finance. Le brief le laisse **délibérément au lot 4b**, et je ne l'ai pas
  anticipé. C'est aujourd'hui le seul point où le joueur peut être surpris sans explication.
- **`I18N.t("Faire défiler")` du lot 1** reste le seul libellé français résiduel de la refonte Carte.
- **Nom du rapport vérifié libre avant écriture** (leçon du build 15.1).

---

## Vocabulaire

Conformément au brief, **« île 7 » n'apparaît nulle part** dans ce rapport pour désigner le lieu : il
s'appelle **le souterrain**, affiché « Île 6 S ». L'id 7 n'est employé que pour parler de code
(`island: 7`, `game.port[7]`, `islandUnlocked[7]`), où il est correct.
