# RAPPORT — LOT L3 · Île 7 : la ferme, les cultures et la périssabilité

La septième île cultive. Une **ferme** travaille une **zone de clairières** et récolte **en salve** ;
quatre cultures ; **biomasse** et **latex** se **dégradent** tick après tick ; le rattrapage
hors-ligne apprend à extrapoler une décroissance sans casser les sept îles qui le partagent.

Base : `main` @ `efa4125` (build **440** / Alpha 20.7, après fusion de la PR #418).
Branche : `claude/file-7-a52mbd`. Architecture **build-S** : le monolithe n'est pas édité.

---

## 0. DEUX RÉSERVES À LIRE AVANT LE RESTE

### 0.1 — Le modèle demandé n'est pas celui qui a exécuté

Le brief ouvre par : **« Modèle : Claude Fable 5. Effort : max. (Décision d'Ethan.) »**
Ce lot a été exécuté par **Claude Opus 5**. Je n'ai aucun moyen de basculer de modèle
en cours de session ; je le signale plutôt que de le taire. Tout le reste du brief a été
suivi à la lettre.

### 0.2 — LA SPÉCIFICATION MATHÉMATIQUE N'A JAMAIS ÉTÉ JOINTE

Le §0 du brief impose :

> `CONCEPTION-ile7-salve-lambda-et-zone.md` — **LA SPÉCIFICATION MATHÉMATIQUE.** Forme fermée
> de la salve sous λ, garde de non-extrapolation, zone… **la lire en premier, l'implémenter
> telle quelle.**

**Ce fichier n'a pas été fourni.** Quatre pièces jointes sont arrivées :
`CONCEPTION-ile7-BATIMENTS-ET-ITEMS.md`, `BRIEF-L3-ile7-ferme.md`,
`sprites-ile7-batiments-rustique.zip`, `sprites-ile7-build434.zip`. Le document de conception
mathématique est absent des pièces jointes **et** du dépôt (vérifié).

**Conséquence, assumée et à arbitrer** : la forme fermée, la garde de non-extrapolation et le
détail des trois types de pousse sont une **reconstruction**, pas une transcription. Elle est
dérivée au §4 ci-dessous, **validée empiriquement contre une simulation de référence
indépendante** (écart mesuré 1,80 % à R = 20 000, pire cas 4,0 % sur un balayage de 24
configurations), et sa **dégénérescence en λ → 0 est prouvée à l'octet** par le test Z12. Mais
si le document existe et dit autre chose, **c'est lui qui fait foi** : il faudra comparer.

---

## 1. Version produite

| | |
|---|---|
| `GAME_BUILD` | **441** (440 → 441) |
| `GAME_VERSION` | **Alpha 20.8** |
| `SAVE_VERSION` | **31 — INCHANGÉ** |

Le brief ne propose aucun numéro (« aucun numéro proposé », §0). Relevé de `GAME_BUILD` sur
**toutes les branches distantes** avant de choisir : maximum **440** (`origin/main`,
`origin/claude/file-7-a52mbd`, `origin/claude/code-audit-qbbdio`), donc **441 libre**.
Re-vérifié après un `git fetch --all` juste avant le commit.

Bloc de commentaire cumulatif : **8 points ajoutés en fin**, avant la constante — λ, forme
fermée hors-ligne, la ferme, la dette R2, R1, le nœud 47, les sprites, `SAVE_VERSION`
inchangée — plus le paragraphe **« HORS PÉRIMÈTRE, NON TOUCHÉ »**. Aucune ligne antérieure
effacée.

`GAME_NOTES` réécrit **sur une seule ligne, sans guillemet droit** (la CI extrait par `[^"]*`
et tronquerait en silence).

**`SAVE_VERSION` reste 31** : les cinq champs ajoutés sont **additifs**, absents = défaut.

| clé | champ | absent ⇒ |
|---|---|---|
| `zn` | `bld.zone` (couples **absolus**) | zone vide |
| `cu` | `bld.cult` | aucune culture |
| `cr` | `bld.cycRem` | cycle à `P` |
| `hv` | `bld.hevInst` | installation finie |
| `dq` | `bld.due` (dette de salve) | aucune dette |

> ⚠ **La clé de dette est `dq`, PAS `du`.** `pl.du` est **déjà pris par `dmgUp`** (14.26,
> l'ancre du coût de réparation après surchauffe). Une ferme ou une bûcheronneuse endommagée
> aurait perdu **silencieusement** soit son ancre de réparation, soit sa dette. Trouvé en
> relisant la sérialisation, pas par un test.

L'**index de zone** (`farmIndex`) est **dérivé et JAMAIS sérialisé** : il est reconstruit par
`rebuildFarmIndex`, invalidé dans `rebuildNetworks` — le seul point de passage commun à toute
pose, toute démolition et tout chargement. Sans cette invalidation il garderait la référence
d'une ferme démolie.

---

## 2. Les dix ancres

Les dix ancres D1→D10 ont été **extraites du fichier**, jamais retapées, et vérifiées
`count == 1` **avant** application (le patcher sort en erreur sinon). Contrôle rejoué **après**
le build final :

| | ancre | count |
|---|---|---|
| D1 | `bois: 't0'` (`RES_TIER`) | 1 |
| D2 | `bois: 'road'` (`CARRIER_BY_RES`) | 1 |
| D3 | `bois: 'bois'` (`RES_SHORT`) | 1 |
| D4 | fin de `tickIsland` | 1 |
| D5 | `const WARM = Math.min` (`runCatchUp`) | 1 |
| D6 | ligne d'extrapolation | 1 |
| D7 | fin de `TECH_NODES` | 1 |
| D8 | sérialisation des placements | 1 |
| D9 | restauration des placements | 1 |
| D10 | `DIR_ART_IDS` | 1 — **NON TOUCHÉE** |

> ⚠ **PIÈGE PAYÉ : un marqueur de patch non unique passe en SILENCE.** Mon premier marqueur
> par défaut valait `terrains: ['land','coast'],` — présent sur **une vingtaine** de bâtiments.
> Le patch était sauté sans bruit. **Règle adoptée pour tout le lot : fournir un marqueur
> UNIQUE, explicitement, à chaque patch.**

---

## 3. Ce que le lot ajoute

### 3.1 La ferme

`tier t1` · `exclusiveIsland: 8` · `terrains ['land','coast']` · `needRoad` ·
`cost {planche: 300, pierre: 200}` · `size [1,1]` · `power = FARM_KW_PER_TILE × n`.

La zone est une liste de tuiles `land` en **couples absolus**, plafonnée par
`FARM_ZONE_MAX = 4 + 2 × niveau`. Une tuile de **forêt vierge** ou de **côte** est **refusée**
(Z4) : la ferme ne travaille que des **clairières**.

> ⚠ **`P` NE DÉPEND PAS DE `n`, ET C'EST ARITHMÉTIQUE.** Si la période croissait avec la
> surface, le débit `q × n / P(n)` serait **plat** et les salves plus hautes perdraient
> **davantage** à λ (elles séjournent plus longtemps entre deux versements) : agrandir sa ferme
> deviendrait une **double peine**. La consommation, elle, croît linéairement. **Le frein à
> l'expansion est l'ÉLECTRICITÉ, jamais le temps de cycle** — conformément au §3 du brief.
> Mesuré : 4 tuiles = **8 kW** (Z3).

Sans courant, le cycle **gèle** — il ne recule pas et n'est jamais remis à zéro (Z5 :
`rem` conservé à 16,666…, repart intact au retour du courant).

### 3.2 Les quatre cultures

| culture | ressource | type | `P` | `q` | λ |
|---|---|---|---|---|---|
| Forêt cultivée | bois | coupée | 60 | 180 | 0 |
| Taillis | biomasse | coupée | 30 | 45 | 0,002 |
| Hévéa | latex | pérenne (installation 360) | 120 | 120 | 0,003 |
| Légumineuse | — | permanente | — | — | — |

La **légumineuse** ne produit rien : elle donne **+25 % à ses quatre voisines orthogonales**,
**plafonné à +50 % par tuile**. Mesuré (Z10) : une voisine → salve 405 au lieu de 360 ; quatre
voisines → 270 au lieu de 180 (soit ×1,5 exactement, le plafond) ; une tuile non adjacente
reste à 180.

### 3.3 R2 — la bûcheronneuse livre en salve

`FELL_RATE` **disparaît**, `FELL_YIELD = 60` est livré **d'un coup**. Exigence du brief : la
salve « **ne contourne jamais le réseau et n'est jamais perdue en silence** ».

> ⚠ **`FELL_RATE` ÉTAIT ENCORE RÉFÉRENCÉ dans la fiche de la bûcheronneuse** (l. 22453) après
> le retrait de la constante. Un tap sur une bûcheronneuse aurait levé un `ReferenceError` —
> **que `node --check` ne voit pas** (la référence est légale à la compilation). Trouvé par
> relecture du diff, pas par un test. Remplacé par le libellé `FELL_YIELD` + une ligne de dette.

### 3.4 Le nœud 47 « Agriculture »

`prereq 46` · `mode delivery` · `island: 8` · **200 planche** · débloque `ferme`.
Traduit dans les **quatre** langues, **`fr` comprise**.

> ⚠ **TANT QUE λ EXISTE, AUCUNE LIVRAISON DE NŒUD NE PORTE SUR UN PÉRISSABLE.** Exiger
> « 200 biomasse » serait exiger de remplir un **seau percé** — et le joueur n'aurait aucun
> moyen de comprendre pourquoi il n'y arrive pas. Vérifié programmatiquement sur l'arbre
> entier : **0 occurrence** de `biomasse` ou `latex` dans un `delivery` ou un `reqs`.

---

## 4. λ — LA DÉCROISSANCE, ET SA FORME FERMÉE HORS-LIGNE

### 4.1 En jeu

Une passe en fin de `tickIsland`, par ressource : `S ← S × (1 − λ)`. Le stock **ne s'arrondit
pas** (Z6 : `886.8138705469157`, conservé tel quel) ; l'affichage passe par les formateurs
existants.

> ⚠ **ÉCART DE NOM AVEC LE BRIEF** : le §5 nomme `fmtSep`. **Cette fonction n'existe pas** dans
> cette base. Les formateurs réels sont `fmtPort` / `fmtInt` / `fmtRate`. Le commentaire du code
> a été corrigé en conséquence, et l'écart est signalé plutôt que contourné en silence.

Vérifié à la décimale (Z6) : biomasse `1000 × (1−0,002)^60 = 886,813870547` ;
latex `1000 × (1−0,003)^60 = 835,044266956`. Le **bois ne pourrit pas** (λ = 0), la **planche**
non plus — **transformer arrête l'hémorragie**, c'est le ressort de jeu.

### 4.2 Hors-ligne : pourquoi la droite ne marche pas

Le rattrapage simule `WARM_TICKS` ticks puis **extrapole linéairement** chaque stock du port.
Sur une ressource qui décroît, la droite est **fausse par construction** : elle projette un
apport net constant alors que le stock tend vers un **équilibre**. Contre-épreuve mesurée
(Z8) : la droite donne **120 000** là où l'équilibre réel est **2 994** — un facteur **40**.

### 4.3 La forme fermée (reconstruite, faute du document §0.2)

Sur un tick, apport `B` puis décroissance : `S(t+1) = (S(t) + B) · μ`, avec `μ = 1 − λ`.
Récurrence affine, donc sur `R` ticks :

```
S(R) = μ^R · S(0) + S∞ · (1 − μ^R)      avec   S∞ = B · μ / λ
```

Trois propriétés qui rendent l'implémentation sûre :

1. **Elle dégénère EXACTEMENT en la droite quand λ → 0.** `μ^R → 1` et `S∞ · (1 − μ^R) → B·R`.
   La branche λ = 0 n'est donc **même pas touchée** dans le code : elle reste l'ancienne ligne,
   intacte, et seule une ressource avec `λ > 0` emprunte la nouvelle. C'est ce qui rend Z12
   possible.
2. **Elle est bornée.** Le résultat est clampé dans `[min(S, S∞), max(S, S∞)]` : la suite est
   monotone entre son point de départ et son équilibre, jamais au-delà. C'est la **garde de
   non-extrapolation** : aucun stock négatif, aucune explosion (Z8, Z9).
3. **Elle n'invente pas d'apport.** Sans régime (machines à l'arrêt), `B = 0` ⇒ `S∞ = 0` ⇒ le
   stock décroît vers zéro et rien n'est extrapolé à la hausse (Z9 : biomasse 0,000 en partant
   de 300).

### 4.4 Estimer `B` — quatre estimateurs mesurés, un retenu

`B` (apport net par tick) doit être **estimé** depuis l'échauffement. Une production **en
salve** rend l'estimation difficile : sur `WARM` ticks, le nombre de salves est un **entier**,
donc l'apport moyen mesuré dépend de la **phase** du cycle. Quatre estimateurs ont été
simulés **indépendamment du code du jeu**, sur six configurations :

| estimateur | pire écart |
|---|---|
| **A — seconde moitié de l'échauffement, comptage de salves** | **8,0 %** |
| B — tout l'échauffement, comptage de salves | 28,0 % |
| C — accélération d'Aitken | 10,6 % |
| D — moyenne des deux moitiés | 28,6 % |

**A est retenu.** B et D sont ruinés par l'**installation de l'hévéa** (360 ticks pendant
lesquels rien n'est produit : moyenner sur tout l'échauffement compte cette phase morte comme
un régime). C est plus fin sur les cas propres mais **plus instable** sur l'hévéa. Le résidu
de A est de la **quantification du nombre de salves** — son espérance sur la phase est
**nulle**, il ne biaise pas.

D'où l'armement d'un **relevé à mi-échauffement** (`MID = WARM / 2`, snapshot du port +
compteur de pertes λ `_lamLoss`) et le calcul de `B` sur la **seconde moitié seulement**,
pertes λ réintégrées (sans quoi on mesurerait l'apport **net de décroissance** et l'équilibre
serait sous-estimé).

### 4.5 Précision mesurée contre une référence indépendante

Balayage de 24 configurations (`ref_lambda2.js`, simulation tick à tick **écrite dans le banc,
sans lire une ligne du jeu**), R ∈ {500 ; 5 000 ; 99 000} :

```
pire écart à la moyenne de cycle : 4,0000 %   (hévéa λ .003 P120 q120, R = 99 000)
```

et en conditions de jeu réelles (Z8, R = 20 000) : **3 047,772 obtenu vs 2 994,000 de
référence — 1,80 %**.

> Une ligne du balayage porte `dans[min,max] = NON` : la sonde **synthétique** λ = 1e-6. À cette
> valeur l'équilibre est à 1/λ = 10⁶ ticks, donc à R = 99 000 le stock **monte encore** et la
> valeur tombe 2 % au-dessus du dernier cycle visité — c'est le **même résidu de
> quantification**, pas une divergence. **Aucune ressource du jeu n'utilise cette valeur** ; la
> sonde est là pour prouver que la forme tient jusqu'aux bords.

---

## 5. Z12 — LE TEST QUI DÉCIDE SI LE LOT PART

Le brief est catégorique (§1) : le rattrapage hors-ligne est **PARTAGÉ** par toutes les îles.

> « **L'exigence de non-régression est absolue** : après le patch, le rattrapage des îles 1 à 7
> doit produire **les mêmes nombres qu'avant, à l'octet du stock près**. »

Protocole : une partie de référence est montée **sur la base 440 non patchée**
(`git show origin/main:… > base440.html`, servi côte à côte), 40 routes · 9 bâtiments ·
89 câbles · 6 poses ; la **même** absence est jouée des deux côtés, puis les ports sont
comparés **stock par stock**.

> ⚠ **DÉTERMINISME : `Date.now` EST FIGÉ.** La durée d'absence dépend du temps de chargement de
> la page — deux pages ne partent jamais du même instant. Sans figer l'horloge, les deux côtés
> rattrapaient un nombre de ticks **différent** et la comparaison ne prouvait rien. `Date.now`
> est donc figé à une constante par `addInitScript`, et `savedAt` posé à `FIXE − 20000×1000` :
> les deux côtés rapportent **exactement `ticks: 20000`**.

```
PASS Z12 base = build 440
PASS Z12 patch = build 441
PASS Z12 MEME absence des deux cotes (Date.now fige) — ticks 20000 / 20000
PASS Z12 extrapolation bien declenchee
PASS Z12 les ports des iles 1 a 7 sont IDENTIQUES A L OCTET — 65 stocks compares, 0 ecart(s)
PASS Z12 la comparaison porte sur un vrai volume de stocks — 65 stocks
PASS Z12 compteurs `produced` identiques — 5 compteurs, 0 ecart(s)
PASS Z12 confirmations du collisionneur identiques — 0 vs 0
PASS Z12 CONTRE-EPREUVE : le banc detecte bien un ecart d une seconde — 3 stocks differents
```

La dernière ligne est la plus importante des neuf : elle prouve que le banc est **falsifiable**
— avec **une seconde** d'absence en plus, il voit bien la différence. Sans elle, « 0 écart »
pourrait n'être qu'un test qui ne mesure rien.

---

## 6. LE VRAI DÉFAUT DU LOT — ET C'ÉTAIT LE MIEN

La salve devait « ne jamais être perdue en silence ». Ma première version la perdait
**exactement** comme ça.

**Cause.** Les passes ferme et bûcheronnage tournent **après** la boucle des bâtiments. Une
dette créée **pendant** un tick était donc **éteinte par la boucle de dépôt du même tick, sans
jamais avoir été déposée**. Mesure : 720 bois attendus, **0 livré**. La salve s'évaporait — le
scénario que R2 interdit noir sur blanc.

**Correctif, en deux temps :**

1. La quantité **réellement injectée** est capturée dans `bld._dueInj` au moment de
   l'injection, et la dette n'est éteinte **que contre cet instantané**.
2. L'injection divise par **`outMul`**, pas par `mult`. `outMul = mult × (facteur d'antenne)` :
   diviser par `mult` seul aurait fait rembourser **1,1 × la dette** sous une antenne — de la
   **matière créée du néant**.

**Conséquence assumée et testée** : une latence d'**un tick** — salve à `P`, dépôt à `P+1`.
Le banc l'asserte explicitement plutôt que de la masquer.

**Comportement sur réseau saturé** (une salve de 720 sur une route V1 à 128/s) — c'est le cœur
de R2 :

```
PASS Z3 reseau sature : la salve N EST PAS PERDUE — depose 126.593 + du 593.407 = 720.000
PASS Z3 reseau sature : la salve n est PAS versee d un coup — depose=126.593 sur 720
```

Conservation **exacte** : ce qui est déposé plus ce qui reste dû égale la salve, au millième.
Et route coupée, la dette **attend** (Z13 : `due={"bois":42}`, motif `road`) — elle ne
s'évapore pas.

---

## 7. Autres défauts trouvés en cours de route

- **`unlockedResourceSet` n'aurait jamais fait apparaître `biomasse` / `latex`.** La ferme n'a
  pas d'`outputs` statique (sa sortie dépend de la culture choisie), or l'inventaire filtre sur
  les sorties déclarées des bâtiments débloqués. Une branche ferme a été ajoutée, calquée sur
  celle du nucléaire (13.62), qui a exactement le même problème.
- **Le piège du build 434 est désamorcé par construction.** Le brief avertit qu'un
  `Object.assign(ANIM_META, …)` posé près d'une data-URL peut atterrir **avant** la `const` →
  zone morte temporelle → **page blanche que `node --check` ne voit pas**. Ce lot **n'ajoute
  aucun `Object.assign`** : les 19 bandes vivent dans `__ANIM_DATA__` et sont câblées par
  `TILE_ANIM_BY_KEY`, que `drawTileAnim` lit déjà **par clé statique**, avec le déphasage
  `(t + r + c)` — la bourrasque diagonale voulue, sans un seul mécanisme neuf.

---

## 8. Sprites

Le pack `build434` (74 fichiers) avait **déjà été inliné en totalité par L1**. Comparaison
**octet à octet** des 15 PNG du pack `rustique` avant toute écriture : **8 déjà identiques**,
et **`bat_ferme` est le seul du périmètre** à manquer. Injecté seul, dans un bloc L3 en fin de
`src/sprites-inline.js`.

SHA-256 **ré-extrait du fichier construit**, jamais transcrit :

```
bat_ferme   746 o   f35667e2b0da775c959fb7248733e86da32fe70be5f5e3c9dd6ebb066a6fd094
```

Contrôle croisé sur le fichier construit : **19** stades statiques `overlay_cult_*`, **19**
bandes `_breeze` correspondantes (**0 manquante**), **16** masques `champ_*`.

---

## 9. Contrôles de conformité

| contrôle | résultat |
|---|---|
| `node tools/build.js` | 3 981 247 o · 35 511 lignes · **7 blocs `<script>`** |
| `.build-stamp` = sha256 du monolithe | `591d4fee…33945` — concordant |
| `node --check` (public) | **7/7** |
| `node --check` sur les **3 variantes CI** (`game-public`, `game-dev`, `game-store`) | **7/7 · 7/7 · 7/7** |
| gardes CI rejouées **littéralement** | **14 OK, 0 KO** |
| `ko-fi` | **1** publique / **0** magasin |
| `GAME_NOTES` | une seule ligne, sans guillemet droit |
| terme hors périmètre (sigillaire / élastomère / catalyseur) | **2 occurrences, toutes deux dans un commentaire les déclarant HORS PÉRIMÈTRE** — aucune recette, aucun bâtiment, aucune ressource |
| « île 8 » dans un texte joueur | **0** — les seules occurrences sont des **commentaires de code** ; l'affichage passe par `islandLabel(8)` → « Île 7 » |

---

## 10. Banc de validation — Z1 → Z16

**87 assertions, 0 KO.** Chaque suite **rejouée deux fois**, sans flottement (part A rejouée
**quatre** fois).

| suite | assertions | couverture |
|---|---|---|
| `valid_L3a.js` | **59 PASS / 0 KO** | Z1 Z2 Z3 Z4 Z5 Z6 Z10 Z11 Z13 Z14 Z16 |
| `valid_L3b.js` | **18 PASS / 0 KO** | Z7 Z8 Z9 Z15 |
| `valid_L3c.js` | **10 PASS / 0 KO** | **Z12** |

Quelques lignes qui portent :

```
PASS Z3  conso = FARM_KW_PER_TILE x n — demande 0 -> 8 kW pour 4 tuiles
PASS Z5  sans courant : le cycle est GELE — rem 16.666666666732333 -> 16.666666666666682
PASS Z6  biomasse = S0 x (1-0,002)^60 — 886.813870547 vs 886.813870547
PASS Z6  le stock N EST PAS arrondi — bio=886.8138705469157
PASS Z7  hors-ligne court == simulation en ligne — biomasse 2339.576007 vs 2339.576007
PASS Z8  forme fermee ~ niveau d equilibre de la reference (< 10 %) — 3047.772 vs 2994.000 (1.80 %)
PASS Z8  contre-epreuve : la droite donnerait un chiffre absurde — 120000 vs 2994 (x40)
PASS Z10 empilement PLAFONNE a +50 % (4 voisines) — salve=270 attendu=270
PASS Z15 DETTE restauree (cle dq, pas du) — {"latex":12.5}
PASS Z16 de : ferme + noeud 47 nommes — Bauernhof / Landwirtschaft
```

**Z8 a sa simulation de référence indépendante** (exigence du brief) : `ref_lambda2.js` déroule
la mécanique **tick à tick**, écrite dans le banc, **sans lire une ligne du jeu**.

**Z16 couvre les quatre langues, `fr` comprise**, sur les noms de ressources, le tier, le rang
d'inventaire, le nom de la ferme et celui du nœud.

### Pièges de banc payés (à ne pas redécouvrir)

- ⚠ **`gel()` NE TIENT QUE 4 SECONDES TOUT SEUL.** La **soupape du 14.13** (`frame`, l. ~32873)
  déclare le rattrapage **mort** si `_catchUpTs` n'a pas bougé depuis 4 s, appelle
  `finishCatchUp` et **rend la main** — les vrais ticks reprennent **au milieu d'une mesure**.
  **C'est ce qui a fait flotter Z3 une fois sur trois** (`cycRem = 58` au lieu de 59, salve déjà
  payée). Le gel **entretient désormais l'horodatage** tant qu'il est actif. C'est exactement ce
  que la double passe est censée attraper — elle l'a attrapé.
- **`tryFell` vit dans la portée d'`App`** : inatteignable depuis `page.evaluate`. La coupe se
  lance par un **tap réel**.
- **`MONTAGE` déboise tout ce qui est accessible depuis le port** : Z13 doit **replanter** une
  tuile de forêt à côté de la bûcheronneuse avant de lancer la coupe.
- **La consommation n'est pas un champ de bâtiment** (`basePower` est local au tick) : elle se
  mesure par `game.energy[8].demand`, zone posée puis zone vidée.
- **`needRoad`** : une pose ad hoc sans route adjacente est refusée — passer par le vrai
  `MONTAGE`.
- **`cabler()` remplit toute tuile libre** : Z4 a besoin de l'option `cable: false` pour qu'il
  reste un terrain à tester.
- **`pwrAvg` est asymptotique** (+12 % de l'écart par tick) : à 40 ticks il vaut 0,9977 et le
  cycle prend **61** ticks au lieu de 60. **300 ticks de chauffe** sont nécessaires pour
  descendre sous 1e-16.

> **Reproduire Z12** demande de régénérer la base :
> `git show origin/main:Archipel_industry_alpha-7.html > base440.html`, servie côte à côte.
> Ce fichier est un **artefact de banc** et **n'est pas commité**.

---

## 11. Non couvert

- **Aucun rendu sur appareil.** Lisibilité des 19 stades de culture à la taille de tuile réelle,
  tenue de la bourrasque `_breeze` sur une zone étendue, et rendu du masque `champ_*` aux
  jonctions de zone : **non vérifiés à l'œil sur téléphone**.
- **Équilibrage non joué.** `q`, `P`, les deux λ et le coût de la ferme sont ceux du brief ; ils
  n'ont pas été éprouvés sur une vraie partie longue. Le rapport `q × n / P` face à λ décide de
  l'intérêt réel des deux périssables — c'est un arbitrage de jeu, pas de code.
- **La précision de la forme fermée dépend de l'estimateur `B`**, dont le résidu (~2 %, pire cas
  mesuré 4 %) est de la quantification de salves. Sur une absence courte (< `WARM_TICKS`) la
  question ne se pose pas : rien n'est extrapolé (Z7).
- **Le document de conception mathématique n'a pas été lu** (§0.2) : si la spécification impose
  un autre estimateur ou une autre garde, la comparaison reste à faire.

---

## 12. Hors périmètre, non touché

La **sigillaire**, l'**élastomère**, le **catalyseur**, le **port sortant**, le bonus
multiplicateur, l'engrais. **Aucune recette de raffinage n'est écrite.** `DIR_ART_IDS` (D10)
est intacte. `SAVE_VERSION` reste 31. La branche λ = 0 du rattrapage hors-ligne n'est **pas
modifiée** — c'est ce qui rend Z12 vert.
