# RAPPORT — LOT 1 « PANNEAUX UI »
## base 381 / Alpha 14.98 → **Alpha 14.99 / build 382**

## 0. Métadonnées

| | |
|---|---|
| Modèle réellement utilisé | **Claude Fable 5** (le brief demandait Opus 5 — la session tournait sur Fable 5 ; signalé au §5, sans écart de méthode : le patch est appliqué verbatim, jamais retapé) |
| SHA-256 d'entrée | `7fda80ee2455658a29f086bdb3616a8a4fd4c00ff51d1bf5c1bc3d031dfda4ec` (= base 381 auditée, re-vérifié avant patch, arbre git propre) |
| SHA-256 de sortie **avant bump** | `c423068ec3d1ca133b5de9cac7142d9cf15fc1383d3f724c544cea39157b7312` — **EXACTEMENT la valeur attendue au §4 du brief** |
| Numéros retenus | `GAME_BUILD = 382`, `GAME_VERSION = 'Alpha 14.99'` (premiers disponibles après 381/14.98) |
| SHA-256 du fichier livré (après bump) | `8972492af09143fa29ef0d4cd1251ecb7a91a839b9cffbb980185e5229e85ef5` · 3 351 999 octets |
| `SAVE_VERSION` | **31, inchangé** (aucune donnée de partie touchée) |

## 1. Application du patch

Le patcher du §3 a été **extrait à l'octet près du fichier du brief** (bloc ```python découpé par `awk` entre les clôtures — jamais retapé) et exécuté tel quel sur la base :

```
C1-helper    count=1
C2-fiche     count=1
C3-up-cur    count=1
C3-up-nxt    count=1
C4-grid      count=1
C4-res       count=1
C5-geo       count=1
C5-nuc       count=1
patch applique, 3351281 octets
```

**8/8 ancres uniques · 3 351 281 octets = la valeur annoncée · SHA de sortie conforme au caractère près. Aucune divergence, aucune réparation manuelle.**

## 2. Vérifications

**Blocs `<script>` (avant bump)** — extraits du fichier **non filtré**, extracteur conservant le texte collé à la balise ouvrante :

| Bloc | Caractères | SHA-256 | Verdict |
|---|---|---|---|
| 1 | 411 | `50efcead…` | conforme |
| 2 | 4 339 | `6820628a…` | conforme |
| 3 | 10 750 | `efe1e3ea…` | conforme (inchangé vs base) |
| 4 | 131 834 | `58303967…` | conforme (inchangé vs base) |
| 5 | 1 111 780 | `3b3948d4…` | conforme |
| 6 | 232 098 | `24521d5b…` | conforme |
| 7 | 1 580 386 | `0f6b6aca…` | conforme (**le seul modifié**, comme annoncé) |

⚠ Note de méthode : mon extracteur inclut le `\n` qui suit `<script …>` quand la balise n'a rien après le `>` → blocs 1/2/5/6/7 sortis à **+1 caractère** avec d'autres SHA au premier passage. Après normalisation (retrait de ce seul `\n` de tête), **7/7 identiques aux empreintes du brief** — les blocs 3/4 (ceux au `/**` collé à la balise, précisément le piège signalé au §4 étape 3) concordaient « bruts ». Le SHA du **fichier entier**, lui, concordait dès le premier coup : la divergence était une convention d'extraction, pas un octet de contenu.

**`node --check`** : 7/7 OK **avant** bump ET 7/7 OK **après** bump.

**Bump** : fait après les étapes 2-3, conformément à la note du brief — commentaire de version (7 lignes, style du fichier) + `GAME_BUILD/GAME_VERSION` + `GAME_NOTES` réécrit (voir §5). `GAME_NOTES` contrôlé : **0 guillemet droit interne, 0 séquence `\u`** (UTF-8 littéral, guillemets français — contraintes CI 14.96).

**Boot réel (étape 4)** — Chromium headless, serveur lancé depuis le dépôt, fichier bumpé : splash retiré, **canvas peint à 100 %** (échantillonnage alpha), horloge qui avance (`playTicks` 1 → 3), **0 `pageerror`**. La zone morte temporelle redoutée n'existe pas : `basePowerRange` est déclaré ~l.14552, après `BUILDINGS`/`ARC_DEF`, et n'est appelé qu'au rendu — **constaté au boot, pas déduit**.

## 3. Suite de validation — T1..T7

Tous les tests par **gestes réels** (souris brute aux coordonnées, appuis longs 700 ms, taps canvas), sur le fichier livré (« APRÈS ») **et** sur la base 381 servie en parallèle (« AVANT », contrôles négatifs). **Suite rejouée 2 fois — résultats strictement identiques, zéro flottement.**

| # | Verdict | APRÈS (observé) | AVANT (contrôle négatif observé) |
|---|---|---|---|
| **T1** Presse UHP, fiche détaillée | **PASS** | ligne « Conso. élec. » présente : `128 kW – 1,02 MW` (bornes brutes runtime `{min:128, max:1024}`) | `LIGNE ABSENTE` ✓ |
| **T2** Four à Arc Fer, fiche détaillée | **PASS** | fourchette **bornée par les modes ARC** : bornes brutes runtime `{min:0.25, max:10}` — surtout PAS le `randomP` 0,5–1,5 ✓. Affiché : `0 kW – 10 kW` (voir note a) | `LIGNE ABSENTE` ✓ |
| **T3** Aciérie V1, fiche détaillée | **PASS** | **une seule** valeur : `128 kW` | `128 kW` — identique ✓ |
| **T4** Presse UHP Nv.1 posée, panneau d'amélioration | **PASS** | « Élec. `1,02 MW → 2,05 MW` » (= 1024×mult → 1024×nextMult), tête « Presse Ultra Haute Pression », ouvert par vrai tap canvas | « Élec. `0 kW → 0 kW` » ✓ — exactement le défaut (a) du §1 du brief |
| **T5** Production, 360 & 390 px, noms longs | **PASS** | **360 px** : `scrollWidth === clientWidth === 322`, colonne « Net /s » entière (bord droit 329 ≤ panneau 341), nom tronqué (admis) · **390 px** : `351 === 351`, Net /s à 358 ≤ 370 | 360 px : `441 > 322` et Net /s à 440 (99 px hors panneau) · 390 px : `441 > 351`, Net /s à 441 ✓ |
| **T6** Barre d'outils | **PASS** | Géothermie V2 **présente dans « Énergie »**, Usine Moteur Nucléaire V2 **présente dans « Nucléaire »**, et leurs **fiches détaillées s'ouvrent** (titres « Centrale Géothermique V2 » / « Usine Moteur Nucléaire V2 ») | absentes des deux groupes ✓ |
| **T7** Round-trip | **PASS** | les 8 paires ré-appliquées à une copie vierge de la base 381 → **identité octet pour octet** avec le fichier livré avant bump (`cmp` silencieux) | — |

Notes d'observation (pas des échecs) :
- **(a)** Pour le four à arc, la borne basse réelle 0,25 kW s'affiche « 0 kW » : `fmtPower` arrondit les kW à l'entier sous 1 000. Les bornes calculées sont justes (`basePowerRange('four_arc_fer')` → `{0.25, 10}`, `four_arc_cuivre` → `{0.25, 4}`) ; seul le FORMATAGE écrase la décimale. Piste pour un lot ultérieur : une marche « W » ou une décimale sous 1 kW dans `fmtPower` — hors périmètre ici (interdit §6 : pas de nouveau libellé, et `fmtPower` est partagé).
- Contrôle de non-régression au passage : `basePowerRange('eolienne')` → `{0, 0}` (producteur pur → ligne masquée, comme avant) et id inconnu → `{0, 0}` sans lever.

## 4. Mesure T5 — colonne « Ressource » à 360 px

Avec `minmax(0,1fr)` en place et une ressource au nom long (« all.tungst. ») :

| Viewport | Largeur restante `.pc-res` | Lisible |
|---|---|---|
| **360 px** | **18 px** | l'icône 16 px + le gap consomment tout : **≈ 0 caractère de texte visible** |
| 390 px | 47 px | ≈ 3-4 caractères |

**La mesure tombe très en dessous du seuil de ~40 px évoqué par le brief** : à 360 px la colonne « Ressource » se réduit à l'icône seule. Le débordement est bien supprimé par construction (aucun `scrollWidth` excédentaire, « Net /s » entière), mais c'est l'argument chiffré attendu pour **redimensionner les colonnes numériques** (88/88/92 px + gaps = 280 px de fixe) dans le chantier « dimensions selon les écrans ».

## 5. Écarts au brief

1. **Modèle** : exécuté sur Fable 5 et non Opus 5 (configuration de session). Aucune décision « créative » n'a été prise en compensation : patch appliqué verbatim, SHA vérifiés à chaque étape.
2. **`GAME_NOTES` réécrit + commentaire de version ajouté** : non demandés par le §« Versionnage », mais ce sont les conventions du projet (chaque lot livré les fait ; `GAME_NOTES` alimente le bandeau « Mise à jour disponible » via la CI). Contraintes 14.96 respectées (UTF-8 littéral, 0 `"` interne, 0 `\u`).
3. **SHA de blocs** : concordance établie après normalisation du `\n` de tête (convention d'extracteur, §2) — le fichier entier concordait sans normalisation.
4. **Harnais de test uniquement** (aucun octet du jeu concerné) : astuces ET guide désactivés (`ui.tipsEnabled=false`, `guide.enabled=false`), tutoriel passé par le vrai bouton, nœuds de recherche forcés `confirmed` pour peupler la barre d'outils, presse forgée sur l'île 1 pour T4, `netFlow` ré-affirmé toutes les 25 ms pour peupler le panneau Production (patron 14.62-e).
5. Rien d'autre : aucune ancre retapée, aucune réparation manuelle, `SAVE_VERSION`/`nominalPower`/`minPower`/`isEnergyConsumer`/`pwrAvg`/`ARC_DEF` intouchés, aucune clé i18n créée, pas de `.nojekyll`, pas de PR.

**Pièges de banc d'essai rencontrés (à verser au mémo)** :
- **La fiche détaillée n'a PAS de croix `.slot-close`** : elle se ferme par le bouton **« Fermer »** en bas du panneau (ou clic backdrop). Toute purge qui cherche une croix échoue en silence et le backdrop avale ensuite TOUS les gestes — c'est ce qui a donné 4 passes de suite entièrement fausses avant diagnostic.
- `useGhostGuard` (13.50) confirme : un `dispatchEvent('pointerdown')` synthétique n'arme PAS le garde — il faut un **vrai clic souris dans le panneau** avant le clic de fermeture.
- **La liste du menu Bâtiment peut défiler entre la mesure et l'appui** (restauration de scroll 13.37) : viser une carte exige scroll → attente 350 ms → **re-mesure sans scroller** → contrôle `elementFromPoint` — sinon l'appui long ouvre la fiche d'une AUTRE carte (constaté : visé Aciérie, ouvert Pompe Eau).
- **Le guide dynamique ouvre ses popups `why` même astuces désactivées** (`checkGuide` → `showTip` sans consulter `tipsEnabled`) : le neutraliser via `guide.enabled=false` dans tout harnais.
- L'inventaire déplié d'office recouvre le canvas (piège 14.85a re-confirmé) : le replier avant tout tap.

## 6. Hypothèses [NON VÉRIFIÉ]

- **[NON VÉRIFIÉ]** Le rendu de la fourchette n'a été exercé que sur 3 fiches (presse/arc/aciérie) + 2 fiches T6 ; les 33 autres bâtiments à conso variable passent par le même helper (bornes runtime vérifiées par échantillon), mais leurs fiches n'ont pas été ouvertes une à une.
- **[NON VÉRIFIÉ]** Aucune save réelle de joueur n'a été rejouée sur ce build (aucun champ de save n'est touché ; le round-trip de save du lot précédent couvrait la base — non refait ici).
- **[NON VÉRIFIÉ]** Le comportement de `.prod-row` avec 20+ lignes réelles de flux (le banc en forge 3) — la règle CSS étant par-ligne, la mesure sur 3 lignes couvre le mécanisme, pas la volumétrie.
