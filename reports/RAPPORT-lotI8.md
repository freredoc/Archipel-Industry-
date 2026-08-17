# Lot I8 — finition i18n : les 5 derniers textes (CLÔTURE du chantier)

**Base** 415 / Alpha 18.2 · **Livré** `GAME_BUILD = 416`, `GAME_VERSION = 'Alpha 18.3'`
`SAVE_VERSION` **inchangé** · **delta +2 032 octets** (3 680 555 → 3 682 587)
SHA-256 `7ba46cf4d9963c22d1d01a381eccefb6f8fd3859f603adaa94645b05436e321a`
`node --check` **7/7**, 7 balises `<script>`.

⚠ **Base réelle = la branche, pas `main`.** Le brief annonce « build 415 (`main` après la
PR #391) », mais **#391 n'est pas mergée** : `main` est encore en 414. Le lot est donc
empilé sur la branche désignée, dont la tête portait bien 415 — la base annoncée est
exacte, seule sa localisation diffère. La PR #391 porte désormais les deux lots.
**Numéro de build relevé sur TOUTES les branches distantes** : max 415 → **416 libre**.

---

## 1. Ce qui est fait

| # | texte | site | défaut | correctif |
|---|---|---|---|---|
| 1 | `kW soutenus` | `label:` d'un `req` de recherche (`case 'energy'`) | **seul des 8 labels de `reqs` non enveloppé** | enveloppé + traduit ×3 |
| 2 | `Surchauffe` | `sensorModesFor`, branches `heatCap` **et** `conduit` | déjà routé par `m.label`, **absent des 3 tables** | traduit ×3 |
| 3 | `❔ Aide & astuces` | titre de panneau (`slot-title`) | rendu brut | enveloppé + traduit ×3 |
| 4 | `— vide —` | `span.inv-empty` | rendu brut | enveloppé + traduit ×3 |
| 5 | `⚠ MODE DEV — construction gratuite` | `div.dev-banner` | rendu brut | enveloppé + traduit ×3 |

Les traductions posées :

| clé | en | es | de |
|---|---|---|---|
| `kW soutenus` | sustained kW | kW sostenidos | dauerhafte kW |
| `Surchauffe` | Overheating | Sobrecalentamiento | Überhitzung |
| `❔ Aide & astuces` | ❔ Help & tips | ❔ Ayuda y consejos | ❔ Hilfe & Tipps |
| `— vide —` | — empty — | — vacío — | — leer — |
| `⚠ MODE DEV — construction gratuite` | ⚠ DEV MODE — free construction | ⚠ MODO DEV — construcción gratis | ⚠ DEV-MODUS — kostenloser Bau |

**Aucune entrée `fr`** : `I18N.t` retombe sur la clé, qui EST le texte français. En ajouter
une serait un doublon inerte — c'est la convention de tous les lots précédents.

Vocabulaire aligné sur l'existant plutôt qu'inventé : `Überhitzung` est déjà le terme des
14 clés de surchauffe du fichier ; le bandeau dev reprend mot pour mot son **toast jumeau**
`🛠 MODE DEV activé — construction gratuite`, déjà traduit (`DEV MODE — free construction`
/ `MODO DEV — construcción gratis` / `DEV-MODUS — kostenloser Bau`) ; l'emoji reste **dans
la clé**, comme dans ce jumeau.

**Une seule précision au brief** : `Surchauffe` est rendu par **deux** branches de
`sensorModesFor` (support `heatCap` **et** support `conduit`), pas seulement la première.
Une seule traduction couvre les deux ; le montage de T3 exerce la branche `heatCap`, celle
que le brief désigne.

---

## 2. Méthode

4 enveloppements par ancres **vérifiées uniques avant remplacement** (le script échoue si la
base a bougé), espaces et structure préservés, aucune concaténation fusionnée. Les trois
littéraux à emoji sont écrits `\uXXXX` dans le fichier : **on les laisse tels quels** — la
clé au runtime est la chaîne DÉCODÉE, identique dans les deux écritures, et retaper l'emoji
aurait introduit une divergence d'encodage pour rien.

`Surchauffe` n'a **rien à envelopper** : il passait déjà par `I18N.t(m.label)`. Il ne lui
manquait que la traduction — d'où son absence de la liste des enveloppements, et un
`assert` qui vérifie que ses 2 branches sont intactes.

Traductions par IIFE d'augmentation en fin de bloc 6, **fusion gardée** `if(!L.ui[k])`,
non-ASCII en `\uXXXX`.

---

## 3. Validation

### T1 — couverture CLOSE · **PASS**

Ré-extraction AST des littéraux passés à `I18N.t` (commentaires exclus par construction) +
les 7 labels de `sensorModesFor` énumérés statiquement.

- **979 clés littérales** (975 + les 4 enveloppements), **6 absentes des tables** :
  `"Nv. "`, `"max"`, `" min · "`, `" kWh / kW"`, `" → "`, `"∞ "`.
  Ce sont **exactement les 6 fragments typographiques** que le brief annonce comme état de
  clôture — aucune autre.
- **Les 8 voies dynamiques : 8 PASS, 0 défaut** (86 libellés). `sensorModesFor` passe au
  vert avec `Surchauffe`.

⚠ Le « KO `progress` — `mine_fer` ×2 » signalé au lot I6 **était un artefact de mon
extracteur**, pas un libellé manquant : sous une propriété `progress:`, il ramassait aussi
les tableaux d'ARGUMENTS nichés, tel le `['mine_fer','carriere']` passé à
`tutUpgradedCount` (2 sites, l. 12083 et 12101). L'extracteur exige désormais un vrai
triplet — **exactement 3 éléments, le 2ᵉ n'étant jamais une chaîne** — et le faux positif
disparaît. Le routage dynamique est donc **intégralement couvert**.

### T2 — identité française · **PASS**

Les 5 surfaces capturées en `fr` sur la base 415 **et** sur le build 416 :
**0 différence sur 5**. `KW Soutenus 0 / 128`, `Machine-Outil … Déficit électrique`,
`❔ Aide & astuces`, `— vide —`, `⚠ MODE DEV — construction gratuite`.

### T3 — rendu écran en allemand · **PASS, 5 sites sur 5**

Éditions **DEV** servies (`DEV_BUILD = true`, posé par le même `sed` que la CI) : c'est le
seul moyen d'atteindre le bandeau du mode développeur, dont le toggle sort tôt quand le
drapeau est faux.

| site | montage effectif | base 415 (de) | build 416 (de) |
|---|---|---|---|
| objectif de recherche | nœuds < 12 confirmés → le nœud 12 « Circuit V1 » reste `available`, ses `reqs` s'affichent | `KW Soutenus 0 / 128` | **`Dauerhafte KW 0 / 128`** |
| capteur, branche `heatCap` | `machine_outil` (`heatCap: 10`) + capteur en surcouche, couche logique, tap réel | `… Stromdefizit Eingangsdefizit **Surchauffe**` | `… Stromdefizit Eingangsdefizit **Überhitzung**` |
| panneau Aide | clic réel sur `.help-btn` | `❔ Aide & astuces` | **`❔ Hilfe & Tipps`** |
| inventaire vide | port vidé **et** arbre reverrouillé | `— vide —` | **`— leer —`** |
| bandeau MODE DEV | Options → clic réel sur `.opt-toggle.dev` | `⚠ MODE DEV — construction gratuite` | **`⚠ DEV-MODUS — kostenloser Bau`** |

**La contre-épreuve est le test qui compte** : sur la base 415, les 5 sites rendent le
FRANÇAIS au milieu d'une interface allemande — c'est le défaut, mot pour mot. Sur 416 ils
sont traduits. Sans elle, cinq captures allemandes ne prouveraient rien.

### T4 — non-régression · **PASS**

- `node --check` **7/7**, 7 balises `<script>`.
- **Boot fr/en/es/de** : 0 `pageerror`, 0 erreur console, 0 `tickError`, horloge qui avance,
  canvas peint, **0 `undefined` dans le texte de la page**. Tables `ui` en/es/de **1 037 →
  1 042** = exactement les 5 clés ajoutées ; `fr` inchangée à 362 (aucune entrée fr, voulu).
  ⚠ Le 404 de `sw.js` (absent du serveur de test) est **préexistant** : filtré explicitement
  et contre-épreuvé sur la base, jamais ignoré en aveugle.
- **Round-trip** : les 4 remplacements présents **1 fois chacun** dans le fichier livré,
  **0 forme nue restante**, IIFE présente 1 fois, 2 branches `surchauffe` intactes.

---

## 4. Pièges de harnais rencontrés

⚠ **Confirmer tout l'arbre efface la ligne qu'on vient chercher.** Les `reqs` d'un nœud ne
sont rendus que s'il est `available`/`condition_ok` (`showReqs`) : pour voir « kW soutenus »
il faut confirmer les nœuds d'id **< 12** et laisser le 12 ouvert.

⚠ **Mais le capteur exige l'inverse** : le bouton de couche logique n'est rendu que si
`logic_wire` est débloqué (nœud 32). Sans confirmer tout l'arbre, le tap tombe sur la fiche
du BÂTIMENT et l'on croit à tort que le panneau du capteur ne s'affiche pas. Les deux
étapes sont donc **ordonnées** : recherche d'abord, arbre complet ensuite.

⚠ **Vider le port ne vide pas l'inventaire** : il liste toute ressource DÉBLOQUÉE, même à 0
(10.55). `unlockedBuildingSet` ne dérive que des nœuds confirmés et de `grantedBuildings` →
il faut **reverrouiller l'arbre**. D'où le placement de cette étape **en dernier** : elle
défait tout ce dont les précédentes avaient besoin.

⚠ `innerText` des panneaux est en **MAJUSCULES** (CSS `text-transform`) : « Dauerhafte KW »
à l'écran est bien la clé `dauerhafte kW`.

---

## 5. État de clôture du chantier i18n

Après ce lot, **tout ce que `I18N.t` peut atteindre est traduit dans les 3 langues**, à
l'exception de **6 fragments typographiques** — `Nv. `, `max`, ` min · `, ` kWh / kW`,
` → `, `∞ ` — dont la traduction n'aurait aucun sens.

Restent **hors périmètre**, signalés au lot I6 et volontairement laissés : `Jonction
logique` (doublon d'une entrée `bld` traduite), `Partie 1` et `Import ` (noms
d'emplacement de sauvegarde, des données), `/s` (suffixe d'unité identique dans les 4
langues).

**Non touché** : `applyToData`, `I18N.t` et sa chaîne de repli, les tables `bld`/`res`/
`tech`/`tutorial`/`tips`, la logique de jeu, `SAVE_VERSION`.
