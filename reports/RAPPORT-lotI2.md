# RAPPORT — lot I2 (chantier i18n) : couche CONTENU, bâtiments + ressources

**Livré en `GAME_BUILD = 405` / `GAME_VERSION = 'Alpha 17.2'`.** `SAVE_VERSION` INCHANGÉ (31).

| | |
|---|---|
| Base | build **404 / Alpha 17.1** (le lot I1 de cette même branche) ; base du chantier : 403, SHA `981f1f95…` |
| Fichier livré | SHA-256 `937f380ad2f234f0d6eb66d9f499ebbf2595cb5f002066208e3088e07fada951` |
| Delta | **+15 090 o** pour le patch I2 seul · **+22 494 o** pour la branche entière (I1 + I2 + bumps) |
| Couverture | `bld` **53/114 → 114/114** · `res` **32/48 → 48/48** (en, es, de) |
| Effet de bord réparé | labels de tuile vides : **64 → 5** |
| Lignes de rendu touchées | **aucune** |

---

## 1. Ce que le lot corrige — et il ne fait pas que traduire

Le défaut annoncé : **61 bâtiments sur 114** et **16 ressources sur 48** sans entrée `bld`/`res`,
donc `name` et `label` en français dans les 4 langues. Presque tout le mid/late-game.

**Un second défaut, non annoncé, a été trouvé en cours de route — et il touche AUSSI le français.**
`applyToData` écrit :

```js
var lb = self.bld(bid, 'label'); if (lb != null) refs.BUILDINGS[bid].label = lb;
```

et `bld()` rend la **chaîne vide** quand l'id n'a d'entrée dans **aucune** langue (fr compris, qui
sert de repli). La garde est `!= null`, pas `nonEmpty` : `''` passe. **Le label source de ces
61 bâtiments était donc écrasé par `''` à chaque chargement**, et le code court n'était dessiné sur
aucune de leurs tuiles — `GEO`, `DATA`, `CRYO`, `MOT2`, `TUN4`… **en français aussi**. Le `name`,
lui, est protégé par une garde `nonEmpty` et n'a jamais été perdu.

Mesuré sur le build patché contre la base : **64 labels vides avant, 5 après**. Les 5 restants sont
`road`, `pipe`, `wire`, `conduit`, `logic_wire` — des infra qui n'ont jamais eu de code court.
Le site de dessin (`if (b.label && tile >= 30)`) confirme qu'un label vide ne dessine rien.

**C'est la raison pour laquelle le lot écrit aussi une entrée `fr`** — contrairement à I1, où `fr`
n'a rien à faire. Mais **le `label` SEUL, jamais le `name`** : le nom français vit déjà dans
`BUILDINGS`, `applyToData` ne l'écrase pas quand la clé manque, et le dupliquer créerait deux copies
crédibles qui peuvent diverger — le piège exact du lot A′ (14.97). Vérifié en jeu : en `fr`,
`geothermie` = « Centrale Géothermique » (nom source intact) + label `GEO` (restauré).

## 2. Méthode

IIFE d'augmentation en fin de bloc 6, comme les ~40 existantes. **Ids, noms fr et labels source
extraits du RUNTIME** (Chromium + délimitation du littéral `BUILDINGS` par comptage d'accolades
conscient des chaînes ET des commentaires — piège 14.91), jamais retapés depuis l'inventaire.

⚠ **Structure imbriquée, pas plate.** Les IIFE existantes n'écrivent que `L.ui`, un dictionnaire
plat. Celle-ci vise `L.bld` (`{name,label}`) et `L.res` : la fusion se fait **par id**, avec la garde
`if(!L.bld[k])` — jamais de remplacement en bloc d'une entrée existante.

⚠ **Le label est PARTAGÉ par en/es/de, seul le `fr` diffère.** C'est la convention des 53 entrées
déjà livrées (`IRON`, `STEEL`, `CEM` identiques dans les trois) : elle est suivie, pas réinventée.
D'où `four_arc_fer` → fr `ARC.F` / en-es-de `ARC.I`, `cimenterie_v2` → fr `CIM2` / autres `CEM2`.

⚠ **`TOWR` et non `COOL`** pour la tour aéroréfrigérante : `refroidisseur` occupe déjà `COOL`, deux
tuiles au même code seraient illisibles.

**Section D — les 3 entrées mortes ne sont pas réécrites, elles sont RECYCLÉES puis purgées.**
`four_arc_acier`/`_cable`/`_piece` sont des ids disparus à l'unification des fours à arc (13.22)
mais **leurs traductions existaient**. Elles ont été reprises sur les ids vivants
`four_arc_fer`/`_cuivre`/`_tungstene` en corrigeant le métal (Steel→Iron, etc.), puis les entrées
mortes sont supprimées dans les **4** langues. Vérifié après patch : **0 entrée orpheline**, en `en`
comme en `fr`.

IIFE **100 % ASCII** (non-ASCII en `\uXXXX`) : le fichier mêle UTF-8 littéral et `\xNN`.
Idempotente (marqueur `lot I2 : couche CONTENU`), et la purge des morts est un `delete`, donc un
no-op au second passage.

## 3. Tests — 5/5 PASS

**T1 — couverture.** `Object.keys(BUILDINGS).every(b => I18N.locales[lg].bld[b])` et l'équivalent
`RES_SHORT`/`.res`, au runtime :

| | en | es | de |
|---|---|---|---|
| `bld` | **114/114** | **114/114** | **114/114** |
| `res` | **48/48** | **48/48** | **48/48** |

Falsifiable : la base rend **61 manquants** par langue (mesuré, contre-épreuve jouée sur le build
404 servi en parallèle). Orphelines restantes : **0**.

**T2 — application effective (la vraie porte, `applyToData`).** T1 seul ne verrait pas une entrée
écrite sous un id inexistant. On lit donc `BUILDINGS[id].name` **après** `applyToData`, dans les
4 langues :

| id | de | es | en | fr (source) |
|---|---|---|---|---|
| `porte_and` | UND-Gatter | Puerta Y | AND Gate | Porte ET |
| `mine_tungstene_v4` | Wolframmine V4 | Mina de Wolframio V4 | Tungsten Mine V4 | Mine Tungstène V4 |
| `geothermie` | Geothermiekraftwerk | Central Geotérmica | Geothermal Plant | Centrale Géothermique |
| `four_arc_fer` | Lichtbogenofen Eisen | Horno de Arco Hierro | Iron Arc Furnace | Four à Arc Fer |
| `cimenterie_v2` | Zementwerk V2 | Cementera V2 | Cement Plant V2 | Cimenterie V2 |

Ressources idem (`tungstene` → Wolfram / wolframio / tungsten ; `helium_liquide` → Fl. Helium /
He líquido / liquid He). Accents corrects, **aucun `\xNN` ni `\uXXXX` visible**. **0 `pageerror`.**

**T3 — rendu écran.** Menu Bâtiment en allemand, tutoriel passé par le **vrai bouton**, arbre
débloqué, overlays purgés : **93 vignettes, 0 nom resté français**, dont Geothermiekraftwerk,
Wolframmine V1/V4, Lichtbogenofen Eisen/Kupfer/Wolfram, Kühlturm, Zementwerk V2, Bohrer, Kryostat,
Quantenstabilisator, Untertage-Extraktor, Kryo-Zerleger, Ultrahochdruckpresse, Quantenrechnerfabrik,
Quantenmotorenwerk, Werkzeugmaschine, Luftzerleger V1/V2, Gaskraftwerk, Data Center.

**Les 9 blocs logiques ne sont PAS dans ce menu** — ils vivent dans celui de la **couche logique**
(13.91). Vérifié séparément après bascule par le vrai bouton :
`Sensor · Aktor · UND-Gatter · ODER-Gatter · NICHT-Gatter · NAND-Gatter · NOR-Gatter · XOR-Gatter ·
XNOR-Gatter`.

**T4 — le code court tient-il sur la tuile ?** Mesuré au canvas avec la **vraie police du dessin**
(`700 round(tile*0.26)px "Barlow Condensed"`, seuil de dessin `tile >= 30`) :

| | nb | largeur max / tuile |
|---|---|---|
| 50 labels **déjà livrés** | 50 | **1,333** (`STONE`) |
| 59 labels **du lot** | 59 | **1,333** (`C.CO2`) |

**0 label du lot plus large que le pire déjà livré** → PASS.
⚠ **Honnêteté du chiffre** : 46 labels du lot dépassent 1 tuile — **mais 20 des labels déjà livrés
aussi** (`STONE`, `WATER`, `STEEL`, `C.DSL`, `OFF.W`…). Le léger débord est une caractéristique
**préexistante** du repli vectoriel, pas quelque chose que ce lot introduit ; et ce chemin de dessin
n'est emprunté que lorsque le sprite manque. Le lot n'aggrave rien.

**T5 — non-régression.** Comparaison exhaustive base ↔ livré sur les 114 bâtiments et 48 ressources :

- **exactement 61 bâtiments** changent de `(name,label)` — 0 changement nul, 0 en trop ;
- **exactement 16 ressources** changent ;
- **les 53 bâtiments déjà traduits : 0 modifié** ;
- `node --check` **7/7** (avant, après patch, après bump) · boot **0 `pageerror`** ;
- **idempotence** : 2ᵉ exécution des patcheurs I1 **et** I2 → « DEJA APPLIQUE », fichier inchangé ;
- **round-trip** : ré-extraction des 7 blocs identique ;
- **I1 toujours vert sur le build final** : `.tuto-count` = `Minen 0/1` en de (base : `Mines 0/1`).

## 4. Écarts assumés et points laissés ouverts

1. **Aucun patcheur n'était joint** à ce lot (le brief le dit « déjà exécuté »). Il a donc été
   **écrit ici** à partir des registres runtime. Conformément à la convention du dépôt — aucun
   `patch_*.py` n'y est versionné — il n'est **pas commité** ; il reste dans le scratchpad de
   session. Le résultat est de toute façon vérifiable par les tests T1/T2/T5, et l'IIFE est
   idempotente.
2. **`antenne` / `antenne_v2` gardent un nom FRANÇAIS en allemand** (« Antenne Amplificatrice »).
   C'est une **incohérence préexistante de la base** : le palier s'aligne sur sa sœur plutôt que de
   trancher unilatéralement. À arbitrer (corriger imposerait de réécrire une entrée existante).
3. **« Data Center » laissé tel quel en es/de.** La couche `ui` le traite déjà comme un nom propre
   (« Data Center PAUSIERT », traduit au lot I1) : le traduire ici *aggraverait* l'incohérence
   signalée dans le rapport I1 au lieu de la réduire. Une harmonisation « Data Center » vs
   « Rechenzentrum » / « Centro de Datos » est un arbitrage de terminologie, pas un lot de
   traduction — il touche de l'existant.
4. **Branche.** Le brief demande `claude/i18n-lotI2`. La consigne de session impose la branche
   désignée `claude/chantier-l18n-s04ef0` et interdit toute autre branche sans autorisation : I1 et
   I2 arrivent donc en deux commits sur **une seule branche**, et non en une PR par lot.
5. **Numéros de build.** I1 = 404, I2 = 405, sur une PR unique. Collision revérifiée sur **toutes
   les branches distantes** avant push : max 403 → 404 et 405 libres.
6. **Qualité des traductions** : elles suivent le registre des 53 entrées existantes (noms complets
   par langue, abréviations courtes pour `RES_SHORT` sur le patron `ling.fer`/`iron ing.`/`Eisenb.`).
   Elles n'ont pas été relues par un locuteur natif — à signaler si un terme choque en jeu.

## 5. Contrôles finaux

| contrôle | résultat |
|---|---|
| `node --check` 7 blocs | **7/7** |
| Balises `^<script` | **7** |
| `GAME_NOTES` | 484 car., **0 guillemet droit**, **0 séquence `\u`/`\x`** |
| `SAVE_VERSION` | **31, inchangé** — aucun champ de partie touché |
| SHA-256 des 7 blocs | 1 `a50c1c4e…` · 2 `8fbb2218…` · 3 `d949f1c3…` · 4 `35f4f974…` · 5 `1be53ce4…` · 6 `3e31f1a5…` · 7 `ceb0f540…` |

## 6. Suite du chantier

Restent, dans l'ordre imposé par `00ORDRE.md` : **I3** (enveloppement de 74 littéraux bruts) puis
**I5** (358 clés `ui`, après I3), **I4** (astuces, indépendant), et **I6** en dernier (purge +
convention IIFE). ⚠ **I5 après I3** : I3 crée des clés neuves, et si I3 n'est pas mergé au démarrage
de I5, l'inventaire I5 doit être régénéré.
