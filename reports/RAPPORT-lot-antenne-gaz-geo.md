# RAPPORT — Lot « Antenne graduée, Gaz ×4, Géothermie V2, 0 kW »

**Brief** : `BRIEFlotantennegazgeo.md` (4 chantiers) · pack `geothermiev2.zip`
**Livré** : `GAME_BUILD = 378` · `GAME_VERSION = 'Alpha 14.95'` · **`SAVE_VERSION` INCHANGÉ (31)**
**Base** : Alpha 14.94 / build 377 / **3 322 621 o** — exacte, conforme au brief.
**Taille** : 3 322 621 → **3 333 643 o**, delta **+11 022 o** (dont ~3,2 Ko pour les 2 PNG).

Ordre d'exécution : 1 → 2 → 3 → 4, comme recommandé.

---

## 0 — Contrôles d'intégrité

| | |
|---|---|
| Ancres | **11**, toutes à `count == 1` **avant** écriture |
| `node --check` | **7 blocs / 7 OK**, éditions publique **et** dev |
| SHA-256 des 2 PNG re-décodés **du fichier patché** | `1c4e9c1b…` · `8dc2c43b…` — **conformes au pack** |
| `SAVE_VERSION` | **31**, inchangé |

SHA-256 des 7 blocs `<script>` après patch (les blocs 5 et 7 sont ceux qui changent) :

```
bloc 1 :     418 o  a50c1c4e7f4a304c650c0cfa7e06c4ff
bloc 2 :    4397 o  8fbb22187703339c146b2f82badd8701
bloc 3 :   10751 o  d949f1c3687aedadcedac85261865f29
bloc 4 :  131835 o  35f4f974f4b2bcd44da73963347f8952
bloc 5 : 1112066 o  6066e8c1aeb44929ec5e92d946a936e7   ← sprites Géothermie V2
bloc 6 :  234216 o  82895501545968e7097909c406a0e65f
bloc 7 : 1591914 o  938943be06fd3d6f9e48d1aa83bd9b45   ← les 4 chantiers
```

**Boot des 2 éditions** : canvas **100 %** peint (1107/1107 et 1053/1053), **0 `tickError`**,
**0 erreur console**, `build 378 · Alpha 14.95`.

**Non-régression** : suites des lots 14.89 / 14.90 / 14.91 rejouées sur ce build —
**103 PASS / 0 KO** (10+6+7+7+2+11+3+8+4 = 58, plus 23+17+5 = 45).
⚠ Deux scripts de la boîte à outils ne tournent plus, **ce ne sont pas des régressions** :
`rl/tstatic.js` lit un fichier de travail `RES_TIER.txt` qui n'existe plus, et `tboot.js` sonde le
canvas trop tôt. Un contrôle de boot neuf les remplace (ci-dessus).

---

## 1 — CHANTIER 1 : la ligne « Élec. » disparaît des non-consommateurs

**2 ancres**, `count == 1` chacune, **insertion pure** dans une chaîne `&&` existante — aucune
parenthèse déplacée (le brief prévenait qu'un `)` manquant sur ce chemin donne une page blanche) :

| Site | Variable d'id en portée | Ajout |
|---|---|---|
| 1a `InfoPanel` | `info.id` (`const b = BUILDINGS[info.id]`) | `&& isEnergyConsumer(info.id)` |
| 1b `UpgradePanel` | `bld.id` (`const b = BUILDINGS[bld.id]`) | `isEnergyConsumer(bld.id) &&` |

**LE CRITÈRE COMPTE, ET LA MESURE LE PROUVE.** Avec `b.power === 0` seul, **36 bâtiments**
auraient été masqués à tort — ils consomment par sigmoïde ou `randomP` : Fabrique d'Ordinateur
Quantique, Usine Moteur Quantique, Presse UHP, les 4 fours à arc, les mines V3/V4, Centrale
d'Enrichissement… `isEnergyConsumer` en masque **36 autres**, tous réellement à conso nulle.

| # | Attendu | Mesuré | |
|---|---|---|---|
| 1.1 | Géothermie : aucune ligne Élec. | absente | **PASS** |
| 1.2 | Éolienne · Centrale à Gaz · Centrale Charbon | absente sur les 3 | **PASS** |
| 1.3 | Fabrique d'Ordi Quantique (`power: 0` + sigmoïde) | **présente** — « 1,02 MW→8,1 MW (amplitude) » | **PASS** |
| 1.3b | Usine Moteur Quantique · Presse UHP | présentes | **PASS** |
| 1.4 | `randomP` (four à arc fer / cuivre) | présentes | **PASS** |
| 1.4b | conso fixe (aciérie · data center · mine V2) | présentes | **PASS** |
| 1.5 | Aciérie boostée par antenne | présente, **« boosté ×1→×1,4 · 128 kW→179 kW »** | **PASS** |
| 1.6 | Aperçu d'amélioration, mêmes bâtiments | même comportement (2 lignes vs 4) | **PASS** |
| **1.7** | **Rejouer 1.1 sur la base 377** | **« ÉLEC. 0 kW » sur les 4 → le test ÉCHOUE bien** | **PASS** |

**18 PASS / 0 KO**, 0 erreur console.

⚠ **Effet non listé par le brief, mesuré et assumé** : les **mines V1** (`mine_fer`, `carriere`…)
perdent aussi leur ligne. Vérifié : `meanPower` et `nominalPower` valent **0** — elles ne
consomment rien, **même boostées par une antenne** (le boost multiplie un nominal nul). La ligne
n'affichait donc que du bruit, exactement comme pour la géothermie.

---

## 2 — CHANTIER 2 : Centrale à Gaz ×4

Bloc `centrale_gaz` délimité par **comptage d'accolades conscient des chaînes ET des commentaires**
(les commentaires français du bloc contiennent `l'oxygène`, `d'échappement`) ; les 4 substitutions
faites **dans le bloc**, jamais sur le fichier entier — `vent:` a 8 occurrences globales.

| Champ | 377 | 378 |
|---|---|---|
| `inputs.methane` | 8 | **32** |
| `inputs.oxygene` | 64 | **256** |
| `outputs.energie_kw` | 2048 | **8192** |
| `vent` | 64 | **128** (×2, décision d'Ethan) |

| # | Attendu | Mesuré (moteur réel, île 7) | |
|---|---|---|---|
| 2.0 | def conforme | 32 · 256 · 8192 · 128 | **PASS** |
| 2.1 | 32 méthane/s et 256 oxygène/s | **exactement 32 et 256** | **PASS** |
| 2.2 | `gaz_echappement` à 128/s | **128** | **PASS** |
| 2.2b | jamais accumulé | `port[6].gaz_echappement` **absent** après 6 s | **PASS** |
| 2.3 | île 6 (surface) : aucun gaz | flux 0, port 0 | **PASS** |
| 2.5 | Nv.2 : tout ×2 | 64 · 512 · **256** — ratio préservé | **PASS** |
| 2.6 | Séparateur d'Air suffit ? | chiffré ci-dessous | **PASS** |

**8 PASS / 0 KO**, 0 erreur console.

### ⚠ 2.4 — LE BRIEF SE TROMPE : LA CENTRALE À GAZ N'ÉMET AUCUNE CHALEUR

Le brief pose « la chaleur suit la consommation (`HEAT_PER_MW`) : quadrupler l'oxygène quadruple la
chaleur émise. Mesurer, et le consigner. » **Mesuré : `heatEmit` est nul, avant comme après.**

Deux raisons, indépendantes :

1. `centrale_gaz` **n'a pas de champ `heatCap`** — or tout le bloc chaleur du tick est gaté dessus.
2. La liste des bâtiments dont la chaleur est indexée sur la conso est **explicite et fermée** :
   `machine_outil`, `presse_uhp`, `usine_moteur_quantique`, `centrale_enrichissement_v2`,
   `usine_moteur_nuc_v2`. La centrale à gaz n'y figure pas. Et sa conso est de toute façon nulle
   (`power: 0` — c'est un **producteur**).

Le ×4 ne change donc **rien** au refroidissement. Si l'intention était que cette centrale chauffe,
c'est un chantier à part (ajouter `heatCap` + l'inscrire dans la liste), pas un effet de bord de
celui-ci. **Non fait** : hors périmètre, et l'inventer aurait changé l'équilibrage sans mandat.

### 2.6 — L'oxygène est-il fournissable ? Réponse chiffrée

Le **Séparateur d'Air V1 sort 512 oxygène/s dès son niveau de base** — soit **2 centrales à gaz**
par séparateur, sans aucune amélioration. La logistique tient donc largement.

⚠ **Mais pas sur l'île 7** : `separateur_air` porte `forbiddenIslands: [7]`. Une centrale
souterraine doit faire **descendre** son oxygène depuis le port de l'île 6 par l'élévateur (ce que
le commentaire de la def documentait déjà). À 256 O₂/s, cela consomme une part notable du débit de
la cage — c'est le vrai coût logistique du ×4, et il est dans l'élévateur, pas dans le séparateur.

⚠ Le commentaire du tick disait « `gaz_echappement` = l'oxygène consommé » : **devenu faux** (vent
×2, oxygène ×4). Réécrit aux deux endroits plutôt que laissé à mentir.

---

## 3 — CHANTIER 3 : Centrale Géothermique V2

### Le pack

`geothermiev2.zip` **vérifié conforme** — contrairement au zip du lot précédent, celui-ci contient
bien le correctif : SHA-256 `1c4e9c1b…` (317 o) et `8dc2c43b…` (402 o), identiques au brief.
Contrôles refaits **indépendamment du LISEZ-MOI** : **587 pixels opaques de part et d'autre**,
**0 écart d'alpha sur 1024**, **365 pixels de couleur changés**, **frame 0 == statique au pixel
près (0 px)**, transparence en **palette (`tRNS`)** → octets inlinés tels quels, jamais ré-encodés.

### Les décisions demandées par le brief

**`entry: 10` / `cap: 9`.** `geothermie` est un V1 **sans palier intermédiaire**, exactement comme
`antenne` et `centrale_nucleaire` — toutes deux `cap: 9 / entry: 10`, toutes deux en fin d'arbre.
Un `cap: 19` supposerait une chaîne V1→V2→V3 qui n'existe pas ici.

**La def reprend la V1 à l'identique** (`terrains`, `exclusiveIsland: 7`, `power: 0`,
`outputs.energie_kw: 512`, `cost: {}`), et **c'est la convention des paliers de PRODUCTEURS, pas un
oubli** : `centrale_charbon_v2` (128 → 128) et `centrale_diesel_v2` (512 → 512) gardent leur
`energie_kw`, comme `antenne_v2` garde ses 1024 kW. Ce que le palier apporte, c'est de **lever le
cap** : la V1 plafonne au Nv.10 (262 144 kW), la V2 reprend à u10 et double sans limite
(Nv.11 = 524 288 kW = ×2 le plafond V1 — continuité exacte de la chaîne). La géothermie n'ayant
**aucun intrant**, l'allègement de recette qui fait la valeur des autres V2 n'existe pas : le
déplafonnement **est** le palier.

Forfait : `{ moteur_quantique: 10, element_moteur_nuc: 1000, cable_supraconducteur: 800 }` — les
3 clés vérifiées dans `RES_TIER` avant écriture.

| # | Attendu | Mesuré | |
|---|---|---|---|
| 3.a | `TIER_NEXT` cap 9 · `TIER_STEP` entry 10 · `TIER_PREV` dérivé | conformes | **PASS** |
| 3.b | forfait exact, 3 clés | conforme | **PASS** |
| 3.c | `cost: {}` · terrains et île repris · sortie 512 | conformes | **PASS** |
| 3.1 | nœud 43 non confirmé → verrouillé | bouton 🔒 « Collisionneur P3 », **2 clics ne densifient rien** | **PASS** |
| 3.2 | nœud 43 confirmé → proposé | « ✦ Densifier · Centrale Géothermique V2 » | **PASS** |
| 3.3 | sans les ressources → refusé | bâtiment intact, **port strictement inchangé** | **PASS** |
| 3.4a | débit exact du forfait | **10 / 1000 / 800**, l'acier témoin à **0** | **PASS** |
| 3.4b | densification île 7 **ÉTALÉE** | travaux `{to:'geothermie_v2', up:10, rem:1554}` | **PASS** |
| 3.4c | travaux aboutis | `geothermie_v2` u10 | **PASS** |
| **3.5** | **nœud 43 : `auto`, AUCUN `delivery`, gratuit** | **conforme** | **PASS** |
| 3.5b | nœud 43 débloque `geothermie_v2` | présent dans `unlocks.buildings` | **PASS** |
| 3.6 | art dédié réellement dessiné | **`bat_geothermie_v2`, jamais l'art V1** | **PASS** |
| 3.6b | `ANIM_META` 4 frames 32×32 fps 8 | conforme (fps repris de `bat_geothermie`) | **PASS** |
| 3.7 | SHA-256 re-décodés du fichier patché | **identiques au tableau** | **PASS** |
| 3.8 | fumée transparente | **587 opaques / 1024, alpha binaire, 32×32** | **PASS** |
| 3.9 | modes de `TECH_NODES` inchangés | `{start:1, delivery:39, auto:3}`, 43 nœuds | **PASS** |

**17 PASS / 0 KO**, 0 erreur console.

⚠ **CONSTAT NON PRÉVU PAR LE BRIEF** : sur l'**île 7**, une densification n'est pas immédiate —
`scheduleUnderWork` l'**étale dans le temps** (lot 14.03), le forfait étant débité tout de suite et
la transformation attendant que la matière descende par l'élévateur. Le test 3.4 a d'abord semblé
échouer pour cette raison ; il est désormais formulé en trois temps (débit · travaux programmés ·
travaux aboutis), et il **exige un vrai lien route ET tuyau port↔élévateur en surface**, sans quoi
rien n'aboutit jamais.

---

## 4 — CHANTIER 4 : graduer le boost de l'antenne

### Où `k` est appliqué

**Au seul endroit où le facteur de zone est posé**, dans la pré-passe d'antenne :
`const boost = antZoneFactor(bl)` (au lieu de `Math.pow(2, upgrade+1)`). Ce site est **en amont de
`buffSet` ET de `debuffSet`** — donc de tout l'aval, sans une ligne de plus : tick, fiche,
overlay de halo, badge `×` sur la tuile, bornes min→max du panneau Énergie, chaleur d'antenne.

### §4.6 — Décision : le curseur s'applique aux DEUX modes

Un curseur qui ne vaudrait que pour la vitesse **mentirait**. La facture électrique est la **même
dans les deux modes** — le tick applique `antElecBoost(fac)` sans regarder le mode. Un joueur qui
bride son antenne puis bascule en productivité retrouverait la facture pleine, sans le moindre
signal. `k` multiplie donc `f` avant tout, `buffSet` comme `debuffSet`. **Mesuré (4.9)** : en
productivité à 50 %, facteur 1024 et **zone toujours à 24 tuiles**.

### §4.4 — Le piège du plancher : traité, et pas en silence

Les 3 formules contiennent `(f > 1 ? f : 2)` : sous f = 2 elles **remontent** à 2. Pire, tous les
gardes `> 1` de l'aval liraient la case comme **non boostée** — la zone disparaîtrait de l'écran.

**Traitement retenu, en deux parties :**
1. **Moteur** : `antZoneFactor` borne le facteur effectif à **2** (la valeur d'une antenne Nv.1 à
   100 %, soit le boost minimal que le jeu sait représenter). Garde-fou indispensable : une antenne
   réglée bas puis **rétrogradée** repasserait autrement sous le plancher.
2. **Interface** : `antBoostMin(bld)` calcule le premier cran **réellement atteignable** à ce
   niveau d'antenne, et le bouton « − » est **grisé** en dessous, avec le motif en infobulle.

Résultat : à Nv.10 (f = 2048) les 10 crans sont ouverts ; à Nv.2 (f = 4) le premier cran est
**60 %** ; à Nv.1 (f = 2) **aucun** cran n'est atteignable — le « − » est grisé d'emblée, ce qui est
exact : une antenne Nv.1 est déjà au minimum. **Le curseur ne bouge jamais dans le vide.**

### §4.5 — Persistance

Champ `bld.antBoost`, **inscrit des deux côtés de la liste blanche** (`pl.ab` / `p.ab`) sur le
modèle de `pl.dd`. Additif : absent = 100 %. **`SAVE_VERSION` inchangé.**

| # | Attendu | Mesuré | |
|---|---|---|---|
| 4.1 | 100 % identique à la base | f = 2048 · vitesse **×103,4** · conso **×205,8** | **PASS** |
| 4.2 | 10 % → ×11,2 / ×21,5 | **×11,24 / ×21,48** | **PASS** |
| 4.3 | 20 % → ×21,5 / ×42,0 | **×21,48 / ×42,0** | **PASS** |
| 4.3b | 50 % → ×52,2 / ×103,4 | **×52,2 / ×103,4** | **PASS** |
| **4.4** | **le nombre de bâtiments touchés ne change pas** | **24 tuiles et 2 bâtiments à 100 % COMME à 10 %** | **PASS** |
| 4.4b | l'intensité, elle, baisse | 2048 → **204,8** → 409,6 | **PASS** |
| 4.5 | Nv.1 : comportement explicite | `antBoostMin` = 100 %, « − » grisé | **PASS** |
| 4.5b | Nv.2 : premier cran = 60 % | conforme | **PASS** |
| 4.6a | réglage sérialisé | `"ab":30` présent dans la save | **PASS** |
| 4.6 | 30 % → sauver → recharger | **toujours 30 %** | **PASS** |
| **4.7** | **save antérieure → 100 %** | **save RÉELLE créée sur 377, rechargée en 378 : f = 16 = 2^(3+1), stocks et niveaux intacts, 0 erreur** | **PASS** |
| 4.8 | deux îles, réglages indépendants | 50 % / 30 % | **PASS** |
| 4.9 | productivité à 50 % | facteur 1024, zone inchangée | **PASS** |
| 4.10 | chaleur cohérente | `heatEmitMax` **52,43 → 5,24 = ÷10 exact** | **PASS** |
| 4.11 | badge × et overlay = valeur effective | `antennaBuff` du voisin = **204,8** | **PASS** |
| **4.12** | **rejouer 4.2 sur la base 377** | **helpers absents, ×103,4 imposé → le test ÉCHOUE bien** | **PASS** |
| 4.ui1-4 | curseur réel dans la fiche | 100 % au défaut, « + » grisé au max ; 9 clics « − » → 10 %, « − » grisé, effet affiché « ×11,24 · conso ×1→×21,48 » ; ligne « Zone 5×5 · inchangée par le curseur » | **PASS** |

**16 PASS + 5 PASS (UI) / 0 KO**, 0 erreur console.

⚠ **Aucun équilibrage ne dépend de la chaleur d'antenne** : elle est indexée sur la conso
supplémentaire des voisins, baisser le boost la baisse d'autant (÷10 mesuré). Aucun seuil de trip
n'est franchi dans l'autre sens — le curseur ne peut que **réduire** la chaleur.

---

## 5 — Écarts au brief et justifications

| # | Écart | Justification |
|---|---|---|
| 1 | **2.4 : la centrale à gaz n'émet aucune chaleur** | Mesuré. Elle n'a pas de `heatCap` et n'est pas dans la liste fermée des émetteurs indexés sur la conso ; sa conso est nulle (`power: 0`, producteur). Le ×4 est donc sans effet sur le refroidissement. Lui en donner serait un autre chantier. |
| 2 | **`k` appliqué à `const boost` et non à la ligne `buffSet[bk] = boost`** | Le site du brief ne couvre que la vitesse. Une ligne plus haut, `buffSet` **et** `debuffSet` en héritent — conforme à la décision du §4.6 (les deux modes), et il n'y a qu'un seul point à maintenir. |
| 3 | **Le plancher `(f > 1 ? f : 2)` est CONSERVÉ** | Le brief laissait le choix. Le retirer ne suffirait pas : les gardes `> 1` de l'aval (fiche, badge, overlay, bornes élec.) feraient disparaître la zone de l'écran. On borne donc le facteur effectif à 2 **et** l'interface grise les crans inatteignables — option (a) du brief, sans toucher aux formules ni à leurs 15 appelants. |
| 4 | **`geothermie_v2` n'est PAS ajouté à `TOOLBAR_GROUPS`** | Le brief dit « pas un bâtiment posable séparément » ; suivi à la lettre. ⚠ **À signaler** : c'est un écart à la convention dominante du projet — **15 des 16 paliers y figurent** (`antenne_v2`, `mine_fer_v4`, `centrale_nucleaire_v2`…), la seule exception étant `usine_moteur_nuc_v2`, que le mémo signale déjà comme une anomalie (sa fiche détaillée devient inatteignable). Conséquence identique ici. Une ligne dans le groupe `energy` la fermerait — décision d'Ethan, non prise ici. |
| 5 | **3.4 reformulé en trois temps** | Sur l'île 7 la densification est **étalée** (`scheduleUnderWork`, lot 14.03) : le forfait part tout de suite, la transformation attend l'élévateur. Asserter la transformation immédiate donnait un faux KO. |
| 6 | 4.2/4.3 : le jeu affiche **×21,48** là où la table du brief écrit ×21,5 | Même valeur, deux décimales au lieu d'un arrondi. |

---

## 6 — Pièges rencontrés (à ne pas redécouvrir)

1. ⚠ **ZONE MORTE TEMPORELLE, retombé dessus (piège 14.76)** : `Object.assign(ANIM_META, …)` posé
   à côté de la data-URL de la feuille se retrouvait **avant `const ANIM_META`** → `ReferenceError`
   au chargement → **page blanche**. `node --check` **ne le voit pas** (ce n'est pas une erreur de
   syntaxe) : seul un boot l'attrape. L'appel est déplacé après la déclaration, avec un
   avertissement en commentaire.
2. ⚠ **La pré-passe d'antenne exige un CÂBLE adjacent ET une alimentation réelle** : sans les deux,
   `game.antennaBuff` reste vide et l'on mesure une zone éteinte en croyant mesurer un curseur
   inerte. Le montage de test utilise des **Séparateurs d'Air** — seuls consommateurs à la fois
   éligibles au boost et **sans aucune matière première** — plus des accumulateurs rechargés en
   continu (à ×205 le boost les vide en quelques ticks).
3. ⚠ **Le gate des halos (14.92) impose ANT_POWER_TICKS ticks servis** avant d'ouvrir la zone :
   une sonde qui attend 1,2 s (~1 tick) lit une zone pas encore allumée.
4. ⚠ **`tryDensify` et `selectTool` ne sont PAS exposés par `__ui()`** (portée App) : il faut passer
   par le bouton « ✦ Densifier » de la fiche (2 temps) et par l'onglet `.tab-upg`.
5. ⚠ Le bouton « ✦ Densifier » verrouillé est **grisé mais CLIQUABLE par design** (il ouvre le popup
   « Recherche requise ») : asserter `disabled === true` est un faux KO — le critère falsifiable est
   « la classe `locked` est là **et** deux clics ne densifient rien ».
6. ⚠ Le `label` d'un bâtiment est **vidé au runtime** par `I18N.applyToData` : ne pas asserter dessus.

---

## 7 — Hors périmètre, non touché

`antRadius` et tout le code de zone (c'est la prémisse du chantier 4) · les formules de la
productivité hors la décision du §4.6 · les coûts d'accès aux îles et les confirmations 39/41/43
(le nœud 43 reste `auto` et gratuit, vérifié) · `pwrAvg` et son coefficient · `TOOLBAR_GROUPS` ·
`SAVE_VERSION` · le pense-bête boucle ambiante en écran verrouillé (testable seulement sur APK).
