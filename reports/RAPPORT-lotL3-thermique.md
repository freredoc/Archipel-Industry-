# RAPPORT — Lot L3 (thermique) + arbitrage joueur sur la démolition

Brief : `BRIEFlotL3thermique` · patcheur `patch_L3.py` (pré-compilé, fourni)
Branche : `claude/temps-souterrain-display-uoonrz`, **repartie de `main`** (la PR #371 a été mergée).

## Version produite

| | |
|---|---|
| `GAME_BUILD` | 388 → **389** |
| `GAME_VERSION` | Alpha 15.5 → **Alpha 15.6** |
| `SAVE_VERSION` | **31, INCHANGÉ** — aucune migration |
| Taille | 3 382 524 → **3 391 803** o (**+9 279**) |
| SHA-256 livré | `d5b0029bcd1c0a64e4c108e4a85f7f02fef1f336170b1f33ad94831c49d62187` |

Le brief annonçait +4 279 o pour le patch seul ; s'y ajoutent le commentaire cumulatif, `GAME_NOTES`
et l'arbitrage joueur (constante partagée + remboursement amputé + toast).

## Sortie du patcheur

Base vérifiée : `102805bb…` = build 388, **aucun avertissement**.

```
OK - 6 ancres appliquees
SHA-256 fichier patche : d72038bb0f03cdea1e8c3e71f40352542cc32c7db02969183bb1bd60a2cb12bc
```

**Conforme au caractère près** à l'attendu du brief, 6 ancres à `count == 1`.

## SHA-256 des 7 blocs, ré-extraits APRÈS la toute dernière modification du HTML

| bloc | octets | sha256 |
|---|---|---|
| 1 | 418 | `a50c1c4e7f4a304c650c0cfa7e06c4ffdfb0d87510d83738114d4dd5a1641628` |
| 2 | 4 397 | `8fbb22187703339c146b2f82badd8701d199128cd3d9dd8673d213c8616ca541` |
| 3 | 10 751 | `d949f1c3687aedadcedac85261865f29b17cd273997e7f6b2bfc53b2f9d4c4dd` |
| 4 | 131 835 | `35f4f974f4b2bcd44da73963347f8952e341f83909e4498227d4e26b98f66f0d` |
| 5 | 1 112 066 | `6066e8c1aeb44929ec5e92d946a936e70451d0c76d7f1375fae084200c308720` |
| 6 | 239 836 | `f6cdea55af87528fdb0faa6d73d1cf2367219509802364d6d0226252832c73d9` |
| 7 | 1 639 350 | `4b4c16d8736506565962fa3fc10d60c55232025ea62c70023dd37c66ee38897f` |

**Blocs 1 à 6 byte-identiques au brief.** L'écart du bloc 7 (`cfe36f49…` au brief) porte le bump, le
commentaire cumulatif, `GAME_NOTES` et l'arbitrage joueur. `node --check` : **7/7 OK**.

## Arbitrage joueur (hors brief) — le point que le brief laissait ouvert

> « pour la démolition réparation : après 5 min le bâtiment devient démolissable, mais avec 20 % réduit. »

Le brief fermait l'avantage de **temps** de la démolition et notait explicitement que l'avantage de
**prix** restait entier : « démolir puis reposer reste économiquement meilleur que réparer à 20 % ».
C'est désormais fermé. Démolir un bâtiment **endommagé** ne rend plus que **80 %** du cumul investi.

- La retenue vaut **exactement le prix d'une réparation** : même constante, même arrondi **supérieur**
  (`v − ceil(v × 0,2)`), donc réparer et démolir-reposer coûtent la même chose. Le choix redevient un
  choix au lieu d'être dominé.
- ⚠ **Le taux de 20 % était codé en dur à DEUX endroits** (aperçu de la fiche l. 20264, paiement de
  `tryHeatRepair` l. 27652) **sans rien pour les tenir ensemble**. La démolition en aurait fait un
  **troisième**, promis à diverger au premier rééquilibrage. J'ai introduit **`HEAT_REPAIR_FRAC`** et
  basculé les trois lecteurs dessus — un seul chiffre désormais.
- ⚠ La retenue **enveloppe toutes les voies de remboursement, jonction comprise**. Une jonction n'a
  pas de `heatCap` donc ne peut pas surchauffer aujourd'hui, mais un helper limité à la branche
  « bâtiment normal » rouvrirait la faille en silence le jour où une infra deviendrait sujette au trip.
- Le **toast nomme la cause** (« remboursé à 80 % (surchauffe) ») : sans cela, le joueur voit revenir
  moins de matière que d'habitude et lit un bug là où il y a une règle.

## Validation — 21 assertions, 21 PASS

Banc : Chromium 1194 headless, serveur HTTP depuis la racine du dépôt, viewport 420×900.
`processHeat`, `rebuildNetworks` et `heatCapOf` étant déjà exposés par `window.__heat`, **le test
thermique n'a demandé aucune copie de banc** ; seuls `tryDemolish` / `setAntBoost` en exigeaient une
(`BANC_L3.html`, exposeur **paresseux**, **supprimée avant le commit** — et son absence du livrable
est asservie par un test).

Faute de la sauvegarde de fin de partie d'Ethan, le montage thermique a été **construit en séance** :
1 tour aéroréfrigérante + 5 tuiles de conduit + 3 sources aux tampons très différents (antenne
865 074 MJ, cryostat 122,88 MJ, machine-outil 7,68 MJ), **toutes posées à 50 % de leur plafond**,
antenne placée en **tête du balayage** pour reproduire la priorité « coin haut-gauche ». L'eau de la
tour vient de la citerne d'un réseau tuyau isolé (évite de tracer jusqu'au port).

| # | test | résultat | valeurs relevées |
|---|---|---|---|
| V1 | Boot | **PASS** | 0 `pageerror`, aucune page blanche |
| V2 | Les 3 sources sont servies | **PASS** | antenne **3,911** · machine **2,529** · cryostat **3,801** MJ |
| V2c | Budget de la tour intégralement distribué | **PASS** | total **10,240 MJ** = 10 ticks × 1,024 MJ/s — **bornes conservées** |
| V3 | La grosse source reste servie | **PASS** | 3,911 MJ (elle n'est pas affamée à son tour) |
| V5 | **Contre-épreuve sur le build 388** | **PASS** | voir ci-dessous |
| V6 | Granularité | **PASS** | `ANT_BOOST_STEP` = **1** |
| V7 | Arrondi fin | **PASS** | `antBoost = 57` → `antBoostPct` = **57** (et non 60) |
| V8 | Plancher exact | **PASS** | Nv2 **51**, Nv3 **26**, Nv4 **13**, Nv10 **1** |
| V8b | Antenne Nv1 | **PASS** | `antBoostMin` = **100** — aucun cran ouvert, correct |
| V9 | Démolition à chaud | **PASS** | `dmgTimer = 10` → `tryDemolish` = **false**, bâtiment **toujours là** |
| V10 | Démolition après attente | **PASS** | `dmgTimer = 300` → **true**, bâtiment **disparu** |
| — | **Arbitrage joueur : 80 % exact** | **PASS** | pierre **48/60**, minerai de fer **16/20** — exactement `v − ceil(0,2 v)` |
| — | `HEAT_REPAIR_FRAC` partagée | **PASS** | 0,2, trois lecteurs |
| — | Boot du **vrai fichier** de la PR | **PASS** | build **389** / Alpha 15.6 / SAVE 31, canvas **100 %**, 0 `pageerror` |
| — | Poignée de banc absente du livrable | **PASS** | `window.__H` **undefined** |
| — | Quatre boutons de réglage | **PASS** | `−− − + ++`, titres « Baisser de 10 % / 1 % », « Monter de 1 % / 10 % », `+`/`++` grisés à 100 % |
| — | Clic **réel** sur `−` | **PASS** | 100 → **99** |

### V5 — la contre-épreuve, mesurée sur le même montage

| | antenne | machine-outil | cryostat | total |
|---|---|---|---|---|
| **build 388** | **10,240 MJ** | **0,000** | **0,000** | 10,240 |
| **build 389** | 3,911 | **2,529** | **3,801** | 10,240 |

Sur 388, **l'antenne prend 100 % du budget de la tour et les deux autres ne reçoivent rien** — la
priorité « coin haut-gauche » du brief est reproduite exactement. Sur 389 les trois sont servies, et
le **total évacué est identique au millième** : le patch ne crée ni ne détruit de refroidissement, il
ne change que le partage.

Planchers d'antenne, même comparaison : **388** → Nv2 60, Nv3 30, Nv4 20, Nv10 10 (arrondis à la
dizaine) ; **389** → **51, 26, 13, 1**. Le joueur perdait jusqu'à 9 points de réglage.

## Écarts par rapport au brief, et leurs raisons

1. **Arbitrage joueur ajouté** (remboursement 80 %, constante `HEAT_REPAIR_FRAC`, toast) — demandé
   en séance, et c'est précisément le point que le brief listait comme laissé ouvert.
2. ⚠ **Le coût pour la grosse source n'est PAS de −0,2 %, il est de −61,8 % SUR MON MONTAGE.** Le
   brief annonce −0,2 % (V3) et −0,09 % (V4) sur la sauvegarde d'Ethan, que je n'ai pas. L'écart
   s'explique et **ne remet pas le patch en cause** : sur la save réelle, les petites sources ont des
   tampons minuscules, sont vidées en quelques ticks et **rendent le reliquat** à l'antenne — d'où un
   coût quasi nul. Dans mon montage minimal, le cryostat a 61 MJ à évacuer pour une tour qui n'en
   fournit que 1,024 par tick : il consomme durablement sa part, et **à taux de remplissage égal le
   prorata donne des parts égales**. C'est le comportement attendu de la règle, pas un défaut — mais
   **le coût réel pour une grosse source dépend fortement du montage**, et sur un réseau
   sous-refroidi il peut être élevé. À surveiller au playtest.
3. **V2/V4 reformulés.** Le brief attend « cryostat et machine-outil à un taux de 0 » ; avec une seule
   tour, vider 61 MJ en 10 ticks est hors de portée — l'attente supposait le refroidissement abondant
   de la save de référence. J'ai testé la propriété réelle (les trois sources sont servies + le budget
   est intégralement distribué + contre-épreuve sur 388), ce qui est **plus falsifiable** que le seuil
   d'origine. Mes deux premières assertions étaient mal calibrées ; elles ont été corrigées après
   mesure, pas contournées.
4. **V8 précisé** : le brief attend 7 sur l'antenne améliorée d'Ethan. Sur une antenne **Nv1**,
   `antBoostMin` vaut **100** et c'est correct (le mémo 14.95 : « Nv.1 → aucun cran »). J'ai donc
   mesuré le plancher à plusieurs niveaux et comparé 388/389 — ce qui prouve mieux la finesse.

## Points signalés, NON corrigés

- **La démolition reste globalement plus avantageuse en temps de jeu au souterrain** : en surface la
  repose est instantanée, sous terre elle repasse par l'élévateur. L'écart s'y referme de lui-même.
- Les libellés nouveaux (attente de démolition, remboursement à 80 %, titres ±1 / ±10) **ne sont pas
  traduits** : repli français → lot i18n de l'audit 381.
- Les boutons `−−` et `++` réutilisent la classe `ip-nuc-pm` **sans nouvelle règle CSS** ; la rangée
  tient à 420 px sur le banc. Si elle débordait sur plus étroit, **le signaler sans y toucher** : la
  feuille de style est sous invariants (le bloc du lot C doit rester dernier).
- Le prorata s'applique **par réseau de conduits** ; deux réseaux distincts restent indépendants.
  C'est la topologie, pas un défaut.
- `HEAT_REPAIR_FRAC` gouverne maintenant réparation **et** retenue de démolition : la rééquilibrer
  déplace les deux ensemble. C'est voulu.

## Pièges de banc payés en séance

- **`page.evaluate` avec une chaîne l'évalue comme une EXPRESSION** : passer `"async (N) => {…}"`
  renvoie la fonction sans jamais l'appeler (résultat `undefined`). Il faut l'invoquer :
  `` p.evaluate(`(${SRC})(10)`) ``.
- **Forger un bâtiment ouvre une astuce**, dont le `.tip-illu-canvas` recouvre la carte et **avale le
  tap** (piège 14.85) : purger **après** la forge, et vérifier que `elementFromPoint` rend bien
  **le canvas du jeu** avant de cliquer. C'est ce qui faisait échouer la vérification des 4 boutons.
- Poignées de test : **toujours un exposeur paresseux** (`() => ({…})`), jamais un objet — un objet
  capture `curTiles` avant sa déclaration et donne une page blanche par zone morte temporelle (leçon
  du lot L2, reconduite ici sans incident).
