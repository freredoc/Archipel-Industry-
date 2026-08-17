# RAPPORT — Lot « Halos d'antenne après déficit »

**Brief** : `BRIEFlothalosantenne.md`
**Livré** : `GAME_BUILD = 375` · `GAME_VERSION = 'Alpha 14.92'` · **`SAVE_VERSION` INCHANGÉ (31)**

---

## 0 — Deux corrections au brief, avant tout le reste

### 0a — Base effective : **374**, pas 372

Le brief déclare « Alpha 14.89 / GAME_BUILD 372 / 3 303 182 octets ». La branche était à **374**
(Alpha 14.91, 3 316 626 o) : les lots « Gisements » et « Recherche par livraison » ont été mergés
entre la rédaction du brief et son exécution.

Les 4 ancres du §2 ont été **re-vérifiées sur la base réelle** — toutes à `count == 1`, et
`for (const def of ISLAND_TERRAINS) {` bien à `count == 9` comme le brief l'annonce. Aucune
adaptation nécessaire.

### 0b — Il y a **7** blocs `<script>`, pas 11

Le §4 de la méthode avertit : « La base 372 en compte **11**, tous porteurs de JS — pas 7 comme dans
les lots antérieurs. » **C'est faux, et la façon dont c'est faux mérite d'être notée**, parce que
c'est la même famille d'erreur que le piège des apostrophes du lot précédent.

Une regex `<script\b[^>]*>` lancée seule rend bien 11 correspondances. Mais **4 d'entre elles ne sont
pas des balises** :

| Ligne | Contexte |
|---|---|
| 1661 | `a.innerHTML="<script>\x3c/script>"` — une **chaîne** du UMD React |
| 2206 | `* MEME bloc <script> : ANIM_INDEX est construit…` — un **commentaire** |
| 2769 (×2) | `* <script> AVANT le <script> principal du jeu.` — un **commentaire** |

Une extraction **séquentielle** (repartir après chaque `</script>`) ne peut pas les voir : elles sont
à l'intérieur de blocs déjà consommés. Comptés ainsi : **7 blocs**, de 418 o à 1 579 031 o, chacun
terminé par un vrai `</script>`. `node --check` a donc tourné sur **7 blocs, 7 OK**, éditions publique
et dev.

---

## 1 — Investigation (§6) : **H1 confirmée, H2 écartée**

Protocole imposé, exécuté **avant tout patch**, sur la base 374.

**Montage** : antenne V1 + éolienne sur un réseau câble passé en `unlimited` (⚠ sans cela le débit
V1 plafonne la composante à 512 kW et l'antenne, qui tire 1 024 kW, n'est **jamais** servie — on
mesurerait le plafond du réseau et non l'hystérésis). Un espion `drawImage` compte les dessins réels
de `fx_boost`, en plus de l'état moteur `game.antennaBuff[isl]`.

| Étape | pwrAvg | antPowered | zone | halos dessinés |
|---|---|---|---|---|
| 1 — référence | 1,00000 | `true` | 8 | oui |
| 2 — déficit (t+2) | 0,77440 | `false` | 0 | non |
| 2 — creux (t+7) | 0,46440 | `false` | 0 | non |
| 3 — reprise, t+51 | 0,99921 | `true` | 8 | **oui** |

- **Seuil réellement franchi** : `pwrAvg` mesuré à **0,4644**, bien sous 0,90 — pas de faux négatif.
- **Extinction : 1 tick** après le franchissement (2 ticks bout à bout, avec le décalage d'un tick
  documenté de la pré-passe, qui lit l'état du tick précédent).
- **Rallumage : 51 ticks.**
- **Les halos réapparaissent au MÊME tick que `antPowered`** (51 et 51).

**Conclusion : H1 seule.** L'écart entre « `antPowered` rallumé » et « halos réapparus » est **nul** →
`_animPlayed` n'ajoute aucun délai, **H2 n'est pas confirmée** et n'est donc pas patchée. H3 restait
écartée par le fait 2c du brief (`tickIsland` tourne sur toutes les îles) — confirmé : `pwrAvg`
progresse hors écran.

**Pourquoi « changer d'île » semblait corriger** : c'est bien la corrélation annoncée par le brief.
Les 51 ticks s'écoulent pendant l'aller-retour, et le joueur attribue le retour au changement d'île.

⚠ **Le 51 mesuré est un plancher, pas les 55 théoriques.** Le calcul du brief (`⌈ln(0,001)/ln(0,88)⌉`
= 55) suppose `pwrAvg` retombé à 0 ; ici le déficit n'a duré que 6 s et `pwrAvg` s'est arrêté à
0,4644. Le calcul est juste, la mesure le confirme **dans le bon sens**. Un déficit plus long donne
bien 55.

Mesure **reproduite deux fois** (périodes de référence de 12 s puis 35 s) : **51 ticks** les deux fois.

---

## 2 — ⚠ Le correctif recommandé par le §3, appliqué tel quel, **échoue au test 7.4**

C'est le point central de ce lot.

Le §3 recommande : `antPowered` s'allume après **3 ticks consécutifs** servis, s'éteint au premier
tick coupé. Appliqué littéralement (patch A), mesuré :

| | base 374 | patch A (§3 littéral) |
|---|---|---|
| **7.3** rallumage après déficit | 51 ticks | **5 ticks** ✓ |
| **7.4** réseau juste à la limite | 4 bascules / 75 s | **37 bascules / 75 s** ✗ |

Trace de la zone sous patch A : `1000100010001000…` — **cycle de 4 ticks, clignotement à 0,25 Hz**.
C'est exactement le mécanisme que le commentaire d'origine décrit : *« couper la zone baisse la
demande, ce qui rallume la zone, ce qui la remonte »*. Trois ticks pour rallumer et un pour éteindre
ne font que **ralentir** ce cycle d'un facteur 2 ; ils ne le suppriment pas.

Or le §3 exige explicitement de **conserver l'intention anti-clignotement**. Le respecter à la lettre
l'aurait détruite.

### Montage du test 7.4

Il fallait un montage où la **zone elle-même** fait basculer le réseau en déficit :

- 2 **Séparateurs d'Air** dans la zone — 1 024 kW **fixes**, **aucun intrant**, sorties tuyau. Ce sont
  les seuls consommateurs qui soient **éligibles au boost** (il faut des `outputs`, cf. 14.63) tout en
  n'ayant besoin d'aucune matière première : la conso boostée oscille bien ×1 → ×1,2.
- production calibrée **entre** la demande non boostée (3 072 kW) et le pic boosté (3 482 kW) →
  3 136 kW ;
- **antenne DERNIÈRE dans l'ordre de priorité énergie** : c'est le pire cas, celui où c'est bien
  l'antenne que `cutToFit` coupe.

⚠ Sans ce dernier réglage, le déficit coupe un **voisin** au lieu de l'antenne, la boucle de
rétroaction n'existe pas, et le test passe à tort : mesuré, **0 coupure / 75** avec l'ordre par
défaut. Le clignotement se cherche, il ne s'observe pas par hasard.

⚠ **L'hystérésis d'origine ne supprime pas le clignotement non plus** : la base fait **4 bascules /
75 s**. Elle le rend rarissime en gardant la zone éteinte 73 ticks sur 75. Le critère « aucun
clignotement » du brief est donc inatteignable au sens littéral ; le critère retenu est **« pas pire
que la base »**.

---

## 3 — Le correctif livré

Deux barèmes de rallumage, et le rapide n'est accordé qu'à une zone qui a **tenu**.

```js
const ANT_POWER_TICKS = 3;   // rallumage normal
const ANT_POWER_SLOW = 45;   // rallumage quand la zone s'est fait couper aussitôt allumée
const ANT_POWER_HOLD = 20;   // ticks pendant lesquels la zone doit avoir TENU pour le barème rapide
```

```js
const antNeed = bl.antNeed || ANT_POWER_TICKS;
const antServed = !(bl.active === false && bl.discReason === 'power');
if (antServed) bl.antTicks = (bl.antTicks || 0) + 1;
else {
  if (bl.antPowered) bl.antNeed = (bl.antTicks || 0) - antNeed >= ANT_POWER_HOLD ? ANT_POWER_TICKS : ANT_POWER_SLOW;
  bl.antTicks = 0;
}
bl.antPowered = bl.antTicks >= (bl.antNeed || ANT_POWER_TICKS);
```

**Ce qui sépare les deux situations est observable, et c'est ce qui rend le correctif possible :**

- un **déficit extérieur** coupe une antenne dont la zone tournait depuis longtemps → `antTicks`
  largement au-dessus du seuil → barème **rapide** ;
- la **boucle de clignotement** coupe l'antenne **aussitôt** après l'allumage de sa zone (c'est
  précisément cette zone qui remonte la demande) → zone tenue 1 tick → barème **lent**.

⚠ `ANT_POWER_HOLD` doit rester nettement au-dessus de la durée de maintien du cycle marginal
(1 tick mesuré), et `ANT_POWER_SLOW` nettement en dessous : sinon le cycle atteindrait le palier de
stabilité et retomberait sur le barème rapide.

⚠ **LE TEST DE SERVICE PORTE SUR LE MOTIF ÉLECTRIQUE, PAS SUR `active` TOUT COURT — et c'est le
test 7.5 qui l'a imposé.** Ma première formulation était `antServed = bl.active !== false`. Elle
échoue en silence : une antenne **ENDOMMAGÉE** (surchauffe) a elle aussi `active === false`, avec le
motif `'heat'` — or la règle de la surchauffe veut que son **debuff de productivité PERSISTE 5 min**
(c'est la pénalité), ce que le code dit deux lignes plus bas (`speedOn = … && !bl.damaged` mais
`prodOn = antMode === 'prod'`, sans garde). Lue comme « non servie », l'antenne sortait par le
`continue` **avant** ce bloc → **la pénalité de surchauffe était purement et simplement annulée**.
Mesuré en comparaison base ↔ patch : base `0/8` sur 10 ticks, première formulation `0/8` puis
`0/0` — la zone de productivité disparaissait. L'ancien barème sur `pwrAvg` ne voyait pas ce cas
(un bâtiment endommagé est sauté par la boucle, `pwrAvg` reste figé à 1). La condition retenue
(`active === false ET discReason === 'power'`) reproduit donc la base **sur tout motif d'arrêt non
électrique** (`heat`, `elevbusy`, …) et ne réagit qu'à la coupure de courant, seule visée par ce lot.
Après correction : base et patch donnent **la même série, `0/8` sur les 10 ticks**.

⚠ **`pwrAvg` n'est pas touché**, ni son coefficient 0,12 — le brief l'exclut explicitement, et il
pilote le badge de déficit et le régime affiché de **tous** les bâtiments du jeu. Contrôle statique :

| | base 374 | build 375 |
|---|---|---|
| ligne `cc.bld.pwrAvg = …* 0.12;` | 1 | **1** |
| occurrences de `* 0.12` | 3 | **3** |
| lectures `bl.pwrAvg` par l'antenne | 2 | **0** |

L'antenne ne lit plus `pwrAvg` **du tout**.

⚠ `antTicks` et `antNeed` sont **transitoires**, comme `antPowered` : la sérialisation des placements
est une liste blanche. Vérifié sur une save réelle (test 7.6).

### Deux formulations écartées, mesurées

| Variante | 7.3 | 7.4 | Verdict |
|---|---|---|---|
| A — 3 ticks secs (§3 littéral) | 5 ticks | **37 bascules** | rejetée : réintroduit le clignotement |
| B — rapide après 90 ticks de service **absolu** | **46 ticks** | — | rejetée : une antenne qui ne tourne que depuis 12 s n'y a pas droit, le symptôme d'origine revient |
| **C — rapide si la zone a TENU ≥ 20 ticks** | **4 ticks** | **2 bascules** | **retenue** |

La variante B illustre pourquoi le seuil devait porter sur **la durée de maintien de la zone** et non
sur une stabilité absolue.

---

## 4 — Tests

| # | Montage | Attendu | Mesuré | |
|---|---|---|---|---|
| 7.1 | Antenne alimentée, aucun déficit | halos présents | `antPowered` vrai, zone 8 tuiles | **PASS** |
| 7.2 | Déficit franc, `pwrAvg` mesuré < 0,90 | éteints en 1 tick | éteints en **2 ticks** (1 + le décalage documenté), `pwrAvg` 0,4644 | **PASS** |
| **7.3** | Plein service rétabli, **sans changer d'île** | ≤ 5 ticks | **4 ticks** (contre 51) | **PASS** |
| **7.4** | Réseau à la limite, antenne dernière en priorité | pas de clignotement | **2 bascules / 75 s**, contre **4** sur la base | **PASS** |
| **7.5** | Antenne endommagée (surchauffe) | vitesse éteinte, productivité inchangée | mode vitesse **8/0 → 0/0** · mode prod **0/8 tenu sur 10 ticks** · **série identique base ↔ patch** dans les deux modes | **PASS** |
| 7.6 | Save halos allumés, rechargement | ≤ 5 ticks, aucun champ nouveau | halos revenus en **3 ticks** ; `antTicks`/`antNeed`/`antPowered` **absents de la save** | **PASS** |
| 7.7 | Badges de déficit des autres bâtiments | identiques | série `pwrAvg` d'un consommateur sur une île **sans antenne**, tick par tick : **0 écart sur 36 ticks** | **PASS** |
| 7.8 | Île sans antenne, 20 s | aucun redessin supplémentaire | **594 (base) → 595 (patch)** — le canvas se redessine ~30 fois/s de toute façon (animations d'ambiance), seule la comparaison a un sens | **PASS** |
| **7.9** | Rejouer 7.3 sur la **base** | doit **échouer** | **51 ticks** (deux exécutions indépendantes) | **PASS** (échoue bien) |

**7.6 est un gain non listé par le brief** : sur la base, `pwrAvg` est `null` au chargement →
`antServed = 0` → il fallait ~55 ticks pour retrouver les halos **à chaque lancement de partie**.
Désormais 3.

### Non-régression

Suites des lots antérieurs rejouées **sur ce build** :

| Lot | Suites | Résultat |
|---|---|---|
| 14.89 « UI & Port » | `t1_port` `t23` `t4` `t35` `t6` `t7` `ttuto` | inclus dans les **58 PASS / 0 KO** |
| 14.90 « Gisements » | `tg` `tg2` | idem |
| 14.91 « Recherche par livraison » | `tstatic` `tA` `tB` `tD` | voir ci-dessous |

**Boot des deux éditions** : canvas **100 %** peint (2 802 / 2 802 et 2 672 / 2 672), **0 `tickError`**,
**0 erreur console**, `build 375 · Alpha 14.92`, `DEV_BUILD` correctement basculé côté dev.

---

## 5 — Contrôles d'intégrité

| | |
|---|---|
| Ancres | **2**, toutes deux à `count == 1` avant écriture ; l'ancre du cœur **inclut le commentaire anti-clignotement**, comme l'exige le §3 |
| Round-trip | 2/2 **verbatim** dans le fichier compilé |
| `node --check` | **7 blocs / 7 OK**, éditions publique **et** dev |
| Taille (`os.path.getsize`) | 3 316 626 → **3 319 817 o**, delta **+3 191 o** |
| `SAVE_VERSION` | **31**, inchangé — aucun champ persisté |

---

## 6 — Écarts au brief

| # | Écart | Justification |
|---|---|---|
| 1 | Base **374**, pas 372 | La branche avait avancé ; les 4 ancres re-vérifiées, aucune adaptation. |
| 2 | **7 blocs `<script>`**, pas 11 | Démontré : 4 des 11 correspondances sont dans une chaîne du UMD React ou dans des commentaires. |
| 3 | **Deux barèmes de rallumage** au lieu du seul plancher de 3 ticks | Le plancher seul réintroduit le clignotement, mesuré : 37 bascules / 75 s contre 4 sur la base. Le §3 exige de conserver l'intention anti-clignotement ; l'appliquer à la lettre l'aurait détruite. |
| 4 | **H2 non patchée** | Non confirmée : les halos réapparaissent au même tick que `antPowered` (51 et 51). Le §4 ne demande le correctif que si H2 est confirmée. |
| 5 | Critère de 7.4 : **« pas pire que la base »** et non « aucun clignotement » | La base clignote déjà (4 bascules / 75 s) ; le critère littéral est inatteignable. |
| 6 | 7.5 et 7.8 formulés en **comparaison base ↔ patch**, pas en absolu | « inchangé » et « aucun redessin supplémentaire » n'ont de sens que par rapport à une référence : le canvas se redessine ~30 fois/s de toute façon. C'est cette formulation qui a révélé la régression du §3 bis — un seuil absolu l'aurait laissée passer. |
| 7 | Correctif hors périmètre : les **6 commentaires `14.9x`** laissés par le lot précédent sont datés **14.91** | Placeholders non substitués, déjà sur `main`. Corrigés au passage, sans effet fonctionnel. |

---

## 7 — Hors périmètre, non touché

`pwrAvg` et son coefficient 0,12 (exclu explicitement par le §3) · le mode productivité de l'antenne
(hors ce que 7.5 vérifie) · `_animPlayed` et l'armement de `g.dirty` (H2 non confirmée) · les barèmes
`antSpeedMul` / `antElecBoost` / `antProdEffect` · le rayon d'influence · `SAVE_VERSION`.
