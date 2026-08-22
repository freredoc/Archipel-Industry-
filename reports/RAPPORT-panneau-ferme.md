# RAPPORT — Lot « panneau ferme »

**Livré : `GAME_BUILD = 446`, `GAME_VERSION = 'Alpha 21.3'`, `SAVE_VERSION` INCHANGÉ.**
Base : build 445 / Alpha 21.2, artefact `d5ee2f39…` (= `.build-stamp` de `main`).
Cible : `src/index.src.html`, puis `node tools/build.js`.

---

## 1. Empreintes

| | `src/index.src.html` | artefact | SHA-256 de l'artefact |
|---|---|---|---|
| base 445 | 2 578 744 | 4 047 440 | `d5ee2f39a5689d039c36812aecd51699fe4f3a0c882a588e89a631d83352ec14` |
| **patch SEUL** | **2 580 840** (**+2 096**) | **4 049 536** | **`779248fd4dd18a6425bf56f299ab519ede755ebc64315a7da6e7e70a8723509b`** |
| livré (446) | 2 586 177 (+7 433) | 4 054 873 | `adaeb46a702a4b31492b2846b4a757d541f1648fee35e56b91fb99ab577f11ea` |

**Le patch seul est CONFORME AU BRIEF, à l'octet et au SHA.** Contrôle rejoué à l'instant sur une
copie fraîche de `origin/main` : `+2 096`, artefact 4 049 536 o, SHA `779248fd…` — les trois valeurs
annoncées. Le fichier livré diffère parce qu'il porte en plus le bump, le commentaire cumulatif,
`GAME_NOTES`, les 9 entrées i18n et l'alignement du nom inline (§5).

Répartition du delta livré : +2 096 le patcheur · +491 i18n et nom inline · +4 846 bloc de version.

## 2. Les 10 ancres

Passage à blanc sur `origin/main` AVANT toute écriture — **10/10 à `count == 1`**, aucune adaptation :

| Ancre | Objet | `count` |
|---|---|---|
| U1 | `kids.push(… "ip-nuc-irr-note" …)` — le pavé gris | 1 |
| U2 | `FARM_CULT_KEYS.filter(k => farmCultUnlocked(game, k)).map(…)` | 1 |
| U3a | `drawTutorialHalo(ctx, ox, oy, tile, r0, r1, c0, c1);` — site d'appel | 1 |
| U3b | `function drawTutorialHalo(…) {` — site de déclaration | 1 |
| U4a | `const FARM_CULT_NODE = { … hevea: 49, legum: 49 };` | 1 |
| U4b | bloc des nœuds 48 et 49 de `TECH_NODES` | 1 |
| U4c | table `tech` **fr** | 1 |
| U4d | table `tech` **en** | 1 |
| U4e | table `tech` **es** | 1 |
| U4f | table `tech` **de** | 1 |

Contrôle d'aller-retour après écriture : `FARM_CULT_KEYS.filter` = 0, `hevea: 49` = 0, `id: 49,` = 0.

Le piège annoncé par le brief est confirmé : les tables de noms de nœuds sont en **UTF-8 littéral**
(`"Charbonnière"`), pas en `\uXXXX`. Les ancres du patcheur sont les formes littérales, extraites de
la source — retapées avec des échappements elles seraient sorties à `count == 0`.

## 3. Contrôles avant / après écriture

- **7 blocs `^<script`**, `node --check` **7/7 OK** sur les **3 variantes CI** : `game-public.html`,
  `game-dev.html` (`DEV_BUILD` basculé par `sed`), `game-store.html` (`SUPPORT_URL` vidé,
  `SELF_UPDATE = false`).
- **Gardes de comptage de la CI rejouées APRÈS avoir écrit mes propres commentaires** (c'est le lot
  429 qui a imposé cet ordre — un nom en commentaire libre avait cassé `main`) :
  `ko-fi` publique **1** / magasin **0** · `const SELF_UPDATE = true;` magasin **0** ·
  `DEV_BUILD = false` publique **1** · `DEV_BUILD = true` dev **1** · emoji usine **5**.
- **`GAME_NOTES`** : une seule ligne, **398 caractères**, **0 `\u`**, **0 guillemet droit**, extrait
  simulé par le `grep -oP` de la CI → chaîne rendue correctement accentuée.
- **`GAME_BUILD` relevé sur TOUTES les branches distantes** avant de choisir le numéro : max = 445
  (`main` et `claude/file-7-a52mbd`) → **446 libre**.

## 4. Suite de validation — témoin 445 vs patch 446

**35 assertions, 0 KO**, deux suites, **rejouées 2 fois sans flottement**. Chaque valeur est donnée
témoin → patché. Aucune erreur console ni `pageerror` sur les 6 contextes (2 de la 1ʳᵉ suite, 4 de
la 2ᵈᵉ, dont un par langue).

### T1 — la chaîne (6 PASS)

| | témoin | patché |
|---|---|---|
| dernier id de `TECH_NODES` | 49 | **48** |
| `TECH_NODES.length` | 49 | **48** |
| `g.techTree.nodes.length` | 49 | **48** |
| nœud 47 → | `["charbonniere"]` | `["charbonniere"]` (inchangé) |
| nœud 48 → | `["foyer_charbon"]` | **`["foyer_charbon","filerie_carbone"]`** |
| nœud 49 existe | oui | **non** |

### T2 — les cultures suivent (3 PASS)

`FARM_CULT_NODE` : `{foret:null, taillis:48, hevea:49, legum:49}` →
**`{foret:null, taillis:48, hevea:48, legum:48}`**. **Aucune valeur ne reste à 49** — c'est le test
qui attrape le couplage oublié, et il est vert.

### T3 — les noms, dans les quatre langues (3 PASS)

`I18N.locales[c].tech['48']` :
`["Foyer à Charbon","Coal Hearth","Hogar de Carbón","Kohlefeuer"]` →
**`["Foyer et Filerie","Hearth and Spinnery","Hogar e Hilandería","Feuer und Spinnerei"]`**.
`tech['49']` : les quatre anciens libellés → **`[null,null,null,null]`**, absent partout.
Le `name` lu au runtime suit bien la table de la locale courante (`applyToData` réécrit par id).

### T-bn — `BUILDING_NODE` est DÉRIVÉ (2 PASS)

`foyer_charbon` → 48 → 48 · **`filerie_carbone` → 49 → 48**, sans une ligne de câblage : la table
est construite par balayage des `unlocks`. Une partie qui avait déjà confirmé le 48 garde donc la
filerie **avec** lui.

### T4 — le pavé (2 PASS, contrôle sur la SOURCE)

Le brief met en garde : ne pas le chercher dans `document.body.innerHTML` (les blocs `<script>` y
sont, la chaîne s'y trouve de toute façon via les tables de langue, et le test répondrait `true` des
deux côtés). Compté dans le fichier : `ip-nuc-irr-note` **8 → 7** (source ET artefact).
La clé i18n du paragraphe reste dans les 4 tables, **orpheline et sans effet** : 4 → 3 occurrences
(seul le site d'appel disparaît).

⚠ **Ce contrôle a d'abord rendu 8 → 8, et la faute était dans MON commentaire** : j'y avais écrit le
nom de la classe en toutes lettres, ce qui annulait exactement la baisse mesurée. C'est le piège du
lot ICON-1 (un emoji faisant passer un compte de 5 à 6) et du build 429 (un nom de service en
commentaire cassant une garde de CI), retombé une troisième fois. Commentaire reformulé sans le
jeton, et une ligne d'avertissement posée à cet endroit pour le prochain lecteur.

### U1/U2/U3 — la fiche rendue par un TAP CANVAS RÉEL (19 PASS)

Ferme posée par le vrai chemin (`tryPlace`) sur l'île 8, reliée au port par une route, fiche ouverte
par un vrai tap.

| | témoin | patché |
|---|---|---|
| pavé `ip-nuc-irr-note` dans la fiche | 1 | **0** |
| boutons de culture | **1** (filtré) | **4** |
| verrouillées | — | `disabled = true`, `opacity = 0.45` |
| débloquée | active | active, `opacity = 1` |
| détail sans survol | non | **« Managed Forest \| 180 wood · 60 s »** |
| infobulle des verrouillées | — | « Unlocked later in Research » |
| débordement à 420 px | non | **non** (boutons 176 × 30 px) |

- **Clic RÉEL sur une culture VERROUILLÉE : sans effet** (`cult` reste `null`) — le bouton est
  réellement inerte, pas seulement grisé.
- **Clic RÉEL sur la culture DÉBLOQUÉE : elle se sélectionne** (`cult = 'foret'`,
  `on = [true,false,false,false]`).
- **i18n, les trois langues** : `["Managed Forest | 180 wood · 60 s", "Coppice | locked", …]` ·
  `["Nutzwald | 180 Holz · 60 s", "Niederwald | gesperrt", …]` ·
  `["Forêt cultivée | 180 bois · 60 s", "Taillis | verrouillée", …]`.

### U3 — le halo, prouvé par sa PULSATION (3 PASS)

Clic réel sur « ✎ Modifier la zone », puis sonde de pixels sur le bord de la tuile ferme.

⚠ **Un test « il y a du vert » ne prouve RIEN sur l'île 8 : elle EST verte** (forêt, herbe). Mesuré :
22 px verts sur 22 en mode zone, mais **encore 19 sur 22 une fois le mode quitté**. Le discriminant
retenu est donc la **pulsation** — le trait respire (alpha 0,45 → 0,80 à ~0,19 s), l'herbe non.
14 relevés du canal vert, espacés de 90 ms :

| | min | max | **amplitude** |
|---|---|---|---|
| mode zone **ACTIF** | 151,6 | 193,7 | **42,1** |
| mode zone **QUITTÉ** | 146,5 | 146,5 | **0,0** |

La série hors mode est **rigoureusement constante**. Le contour existe, il pulse, et il s'éteint
quand on sort de l'édition.

⚠ La contre-épreuve a d'abord rendu « 0 échantillon sur 0 » — donc un vert à vide : la fiche qui se
rouvre retaille la scène et `clampPan` recale la caméra, ma sonde tombait hors du canvas. Recentrage
ajouté **et** assertion `echantillons > 0` exigée, sinon le test se validait tout seul.

## 5. Écarts au brief (2, tous deux assumés et mesurés)

**(a) i18n — les 3 chaînes neuves du détail n'avaient AUCUNE clé.** `« verrouillée »`,
`« aucune récolte »` et l'infobulle `« Débloquée plus loin dans la Recherche »` sortaient à
**0 occurrence** dans les tables. Mesuré en locale EN avant correctif, le bouton rendait
**« Coppice | verrouillée »** — du français à l'intérieur d'un bouton anglais. Les **3 × 3**
traductions sont posées dans le bloc L3, à côté des toasts de zone du lot précédent, à la même
convention `\uXXXX`. Sans elles, le lot aurait remplacé une infobulle invisible sur téléphone par un
libellé visible **et faux** dans trois langues sur quatre — soit l'inverse de son objet.

**(b) Le nom inline du nœud 48 est aligné.** `I18N.applyToData` réécrit `name` par id, donc l'inline
est mort — mais laisser `name: 'Foyer à Charbon'` face à une table qui dit « Foyer et Filerie »
fabriquerait une copie crédible qui ment, exactement le piège du lot A′ (14.97), où un littéral
périmé avait conduit à un diagnostic faux. Une ligne, aucun effet au runtime (vérifié : le nom
affiché suit la table dans les quatre langues).

## 6. ⚠ Conséquence d'équilibrage NON ÉNONCÉE PAR LE BRIEF, mesurée, **non corrigée**

La fusion fait disparaître le `delivery` du nœud 49 (**200 charbon**). Le coût **total** pour ouvrir
le foyer **et** la filerie tombe donc de **300 à 100 charbon**, en **une seule** livraison au lieu de
deux — mesuré : `charbonTotal` 300 → 100.

Le brief décrit U4 comme une fusion de nœuds, jamais comme une re-tarification, et son propre
paragraphe « points ouverts » suit les coûts de livraison (« les nœuds 46 et 47 demandent toujours
100 planches chacun ») sans mentionner celui-ci. Ce n'est pas un défaut de patch : c'est un
arbitrage de progression, qui **appartient à Ethan**. Le remonter à 300 sur ma seule initiative
rendrait la branche silencieusement plus dure que ce que l'auteur du brief a écrit. Signalé ici et
dans le commentaire de version, non modifié.

## 7. ⚠ Contrôle sur appareil — NON EXÉCUTÉ, à faire

Le brief classe ce contrôle « non automatisable ». Ce qui **a** pu être mesuré au banc l'a été
(§ U1/U2/U3 ci-dessus : les quatre boutons, l'estompage, l'inertie réelle au clic, le détail sans
survol, les trois langues, la pulsation du contour). **Ce qui reste à juger à l'œil, sur téléphone,
et que je ne déclare PAS PASS :**

- la **lisibilité** du détail sur deux lignes à la densité réelle de l'appareil (mesuré 176 × 30 px
  CSS à 420 px de large — le banc dit qu'il tient, pas qu'il se lit) ;
- le **rendu du contour pulsé** sur une zone de plusieurs champs collés (le banc a mesuré une zone
  **vide**, donc seul le pointillé de la ferme ; la règle « une arête n'est tracée que si elle n'a
  pas de voisine dans la zone » n'a pas été jugée à l'œil) ;
- la **tenue de l'allemand**, la langue la plus longue, dans un bouton de 176 px.

## 8. Points ouverts

- Le libellé « + N en salve toutes les P s » en tête de fiche **somme encore toute la zone**, alors
  que les champs sont décalés depuis 21.2. Il reste juste au total par tournée, mais il ne décrit
  plus un événement unique. (Point du brief, non traité.)
- Le **0 kW** de la ferme reste temporaire ; les nœuds **46 et 47** demandent toujours 100 planches
  chacun. (Point du brief, non traité.)
- Le coût total de la branche foyer + filerie, **300 → 100 charbon** (§6). À trancher au playtest.
- La clé i18n du pavé retiré reste dans les quatre tables, orpheline. La retirer serait du risque
  pour zéro gain (précédent 13.83) — laissée en place, volontairement.

## 9. Hors périmètre, non touché

`SAVE_VERSION`, la mécanique de la ferme (cycles par champ, contiguïté, `farmBurst`), la feuille de
style (**aucune règle CSS ajoutée** — la feuille a un invariant d'ordre, bloc du lot C dernier), les
nœuds 45 à 47, `farmCultUnlocked`, `drawTutorialHalo`, la CI.
