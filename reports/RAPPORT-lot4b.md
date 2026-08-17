# RAPPORT — Lot 4b : la réparation d'une liaison passe sur la carte (variante A)

**Livré : `GAME_BUILD = 399`, `GAME_VERSION = 'Alpha 16.6'`, `SAVE_VERSION = 31` (INCHANGÉ, vérifié).**

Base d'exécution : build **398 / Alpha 16.5**, SHA-256
`3042e46ceaf0c8c93447e6295ade09d74c014546be4247654dfce3fce9cbf671`, 3 451 366 o — la base
annoncée par le brief. Branche `claude/carte-archipel-wmyxbs`, repartie de `main` après le merge du
lot 4a (PR #381).

⚠ **Le brief ne livrait pas de patcheur** (contrairement au lot 4a) : les 17 ancres du chantier
principal ont été extraites du fichier et le patcheur écrit ici.

---

## 1. Application

Trois patcheurs, tous **idempotents** (2ᵉ passe = « aucun changement », 0 octet écrit), tous à
`count == 1` sur chaque ancre, tous avec **refus d'écriture atomique** si une ancre manque.

| patcheur | ancres | delta |
|---|---|---|
| `patch_lot4b.py` — les six chantiers du brief | **17 / 17** | +7 372 o |
| `patch_lot4b_ile6.py` — **correctif du blocage île 6** (§4) | 2 / 2 | +878 o |
| `patch_lot4b_i18n.py` — i18n + libellé de guide devenu faux | 2 / 2 | +1 630 o |
| bump 399 / 16.6 + commentaire cumulatif + `GAME_NOTES` | — | +3 455 o |

**Total : 3 451 366 → 3 464 701 o (+13 335 o).** Fichier final
**`44a9b75f116a973e4c7603c2cf00aa0a274160fb466102be07d88113de8e18e6`**.

⚠ **Un défaut de mon patcheur i18n, attrapé par sa propre idempotence** : la sentinelle portait
`touche l'île` alors que le fichier écrit `touche l\'île` (apostrophe échappée) → la 2ᵉ passe n'a pas
reconnu son travail et a **réécrit le bloc i18n une seconde fois**. Le fichier a été **remis à HEAD et
les trois patcheurs rejoués dans l'ordre** ; contrôlé après coup : `grep -c "16.6 lot 4b"` = **1**.

---

## 2. Le défaut bloquant que le brief n'avait pas vu

**L'île 6 était devenue INOUVRABLE.** `ARCHI_CACHEE = { 6: true }` masque *entièrement* l'île 6 sur la
carte tant qu'elle est verrouillée — c'est la surprise du jeu, et c'était sans conséquence tant que la
réparation vivait dans un bouton du HUD. Ce lot déplace l'action **sur la carte**. Mesuré au banc,
joueur sur l'île 5, nœud 28 prêt, port garni :

```
badge HUD  : 1
notif-dot  : 1
title HUD  : « Réparer le port pour débloquer l'île 6 »
carte      : 5 vignettes (îles 1-5), 4 liaisons
             target : 0     liaison 5-6 : ABSENTE
```

Le jeu **désigne l'action, allume sa pastille, mène le joueur à la carte — et il n'y a rien à
toucher.** Blocage dur de progression, qu'aucun `node --check` ni aucun boot ne signale.

**Correctif (2 ancres)** : `visible()` laisse toujours passer la cible de réparation ; et tant que
cette cible est `ARCHI_CACHEE` **et** verrouillée, elle est rendue **sans son sprite d'île** —
repérage pointillé + badge 🛠 + libellé seuls. Le sprite `carte_ile_6`, l'animation `arch-emerge`
**et la brume** ne se jouent qu'à l'ouverture : le joueur sait *où* agir sans qu'on lui montre *quoi*
l'attend. Vérifié dans les deux sens en T7.

C'est un **écart assumé au brief** : il n'anticipait pas `ARCHI_CACHEE`. Livrer le lot sans ce
correctif aurait rendu le jeu infinissable.

---

## 3. Contrôles statiques

- **`node --check` : 7/7**, avant et après le bump. Scanner naïf = 11 correspondances, scanner ancré
  `(?m)^<script` = **7** blocs réels.
- **`SAVE_VERSION` toujours à 31** — aucun champ persisté ajouté ; la détection de révélation
  s'appuie sur une **ref d'instantané**, pas sur un drapeau de sauvegarde.
- Équilibre des délimiteurs re-vérifié sur les trois composants touchés : `ArchipelMap` 80/80,
  `MapPanel` 71/71, `RepairModal` 34/34.
- **`data-tut="repair"` : exactement 1 occurrence**, sur le bouton Carte.
- `GAME_NOTES` : **600 caractères** extraits par la regex de la CI, chaîne complète, accents
  littéraux, **0 séquence `\u`**, **aucun guillemet droit**.

### SHA-256 des 7 blocs, re-extraits APRÈS le bump

| bloc | SHA-256 (16 car.) | taille |
|---|---|---|
| blk1 | `a50c1c4e7f4a304c` | 418 |
| blk2 | `8fbb22187703339c` | 4 397 |
| blk3 | `d949f1c3687aedad` | 10 751 |
| blk4 | `35f4f974f4b2bcd4` | 131 835 |
| blk5 | `1be53ce44e7be14f` | 1 113 969 |
| blk6 | `4529778de067622d` | 242 166 |
| blk7 | `8dfd4a8640b5c708` | 1 703 100 |

---

## 4. Suite de validation — Chromium, souris réelle

430 × 820, DPR 2, locale `fr`, page servie **depuis la racine du dépôt**. **Aucun `el.click()`, aucun
appel direct de fonction** : uniquement `mouse.move/down/up` aux coordonnées réelles. Astuces fermées
**par `.tip-ok`**, panneaux fermés **par un vrai clic sur leur backdrop** — jamais `remove()`.

| # | verdict | montage effectif et valeurs mesurées |
|---|---|---|
| **T1** | **PASS** (21) | Île 1, nœud 2 `condition_ok`, port île 1 au coût exact. Carte → l'île 2 est un **`<button>` `.target`** avec badge 🛠, liaison 1-2 **`.repairable`** et **non `on`**. Clic → **la modale s'ouvre et `.arch-map` est TOUJOURS dans le DOM**. « Payé par le port de Île 1 » affiché. Réparer → île 2 débloquée, port île 1 vidé, modale fermée, **carte toujours visible**, révélation jouée (1 vignette + 2 liaisons), **0 brume** |
| **T2** | **PASS** (10) | Contre-épreuve : même montage, **port vidé**. Pastille du bouton Carte **éteinte**, badge 🛠 conservé, cible toujours cliquable, bouton **`disabled`** libellé **« Stock insuffisant »**, pastilles rouges **« ling.fer 0/10 000 »** et **« ciment 0/10 000 »**, **aucun débit**, île 2 non débloquée |
| **T3** | **PASS** (8) | `repairInfo.locked` : île 2 → 3, nœud 8 non atteint, **port 2 PLEIN** (seul le gate doit compter). Cible rendue en **`div`**, badge conservé, titre « Réparation Île 3 — recherche d'accès non atteinte » sur la vignette **et** sur le bouton Carte, pastille éteinte. **Clic réel sur la cible → aucune modale** |
| **T4a** | **PASS** (4) | Étape de tutoriel « répare la liaison » (bandeau « Tuto 11/14 » relevé). `elementFromPoint` au centre du halo → **`[data-tut="repair"]`**, écart au centre du bouton **0,00 px** |
| **T4b** | **PASS** (4) | Objectif de guide `go_reparer`, tutoriel quitté **par le vrai bouton « Passer »**. Bandeau « Objectif », `elementFromPoint` → **bouton Carte**, écart **< 0,001 px** |
| **T5** | **PASS** (9) | Nœud 8 : bouton **« Voir sur la carte »**, **`data-tut="confirm"` conservé** → clic : la Carte s'ouvre, **Recherche se ferme**, la cible île 3 est actionnable. Contre-épreuve nœud **35** (`island: 7`, joueur au souterrain) : garde **« Livrer »**, **livre réellement** (`confirmed`) et débite le port payeur |
| **T6** | **PASS** (4) | Sur la même carte que T3 : les autres verrouillées sont **toutes des `div`**, **aucun badge**, **aucun `title`**, et il n'y a **qu'une seule** liaison `.repairable` |
| **T7** | **PASS** (11) | Île 6, première ouverture. **Avant** : cible présente **malgré `ARCHI_CACHEE`**, cliquable, liaison 5-6 pointillée, **aucun sprite d'île dévoilé**, badge 🛠, **0 brume**. **Après** : île 6 débloquée, carte toujours visible, **brume = 1**, révélation jouée, **le sprite n'apparaît qu'à l'ouverture**, `archiVu6` posé |

**Total : 71 assertions, 0 KO.** **0 `pageerror`**, **0 erreur console** hors le 404 unique du serveur
de test (bruit préexistant documenté depuis 14.47).

**T2 donne sa valeur à T1** et **T3 à T1** : même nœud ou même geste, un seul paramètre inversé,
verdict opposé sur le rendu *et* sur le moteur.

### i18n — 4 langues, au runtime

`I18N.t` interrogé en fr / en / es / de sur les 9 libellés de la refonte Carte :
**0 libellé non traduit**. Exemples relevés : `« Voir sur la carte »` → *See on the map* / *Ver en el
mapa* / *Auf der Karte ansehen* ; `« Payé par le port de »` → *Paid from the port of* / *Pagado por el
puerto de* / *Bezahlt vom Hafen von*.

⚠ **Mon premier audit i18n était FAUX** : il cherchait `"clef":"` alors que les tables écrivent
`"clef": "trad"` **avec une espace** → il déclarait non traduits *tous* les libellés, y compris
« Carte », « Carte de l'archipel » et « Aller à » qui le sont depuis les lots 2 et 3. Refait avec une
regex tolérante : **4 libellés** manquaient réellement, ajoutés.

⚠ **Le brief demande « les cinq tables »** : il n'y en a que **quatre** (fr / en / es / de), et le
français est la **clé** elle-même — trois tables à remplir. Écart de comptage du brief, sans effet.

---

## 5. Décisions et écarts par rapport au brief

1. **Correctif île 6** (§2) — écart majeur, non prévu, sans lequel le jeu se bloque.
2. **Le libellé du guide `go_reparer` disait « Ouvre Réparer »**, un bouton que ce lot supprime. Le
   halo, lui, pointait juste (`data-tut` a migré). Une phrase qui envoie chercher un bouton disparu
   est un défaut à part entière : réécrite en « Ouvre la Carte : touche l'île à ouvrir pour voir ce
   qu'il faut livrer. », traduite dans les 3 tables. Le brief dit « rien d'autre à ajuster côté tuto » —
   il parlait des **prédicats** (`done` est un état, il ne peut pas bloquer), pas du texte.
3. **`repairBtn` conservé à `null`** au lieu d'être supprimé : la constante est consommée à **deux**
   endroits (barre d'inventaire repliée *et* ouverte), un `null` n'y rend rien, et le diff reste
   minimal et réversible.
4. **Branche `claude/carte-archipel-wmyxbs`** (consigne de session), pas une branche dédiée.
5. **Numéro choisi à l'exécution** : 399 / Alpha 16.6, le dépôt étant à 398.

### Cinq pièges de banc, tous dans le harnais, aucun défaut produit

- **Re-cliquer aveuglément « pour contourner `useGhostGuard` » REFERME ce qu'on vient d'ouvrir** : le
  second clic aux mêmes coordonnées tape le backdrop du panneau fraîchement monté. Le harnais clique
  désormais **jusqu'à ce qu'une condition soit vraie**, jamais un nombre fixe de fois.
- **Le nœud 2 ne peut PAS servir à observer `locked`** : ses `reqs` sont vides, `evaluateTechTree` le
  remet en `condition_ok` au tick suivant. T3 passe donc par le nœud 8.
- **Un backdrop encore monté fait échouer `elementFromPoint`** : T4 renvoyait `research-backdrop`
  alors que le halo était **exactement** sur le bouton (écart 0,00 px). Faux KO ; fermer tous les
  panneaux avant de sonder.
- **Écrire `g.tutorial.active = false` ne sort pas du tutoriel** : la barre lit le state React
  `tutorialStep` (piège 14.83). Sortir par le vrai bouton « Passer ».
- **`hasText` teste tout le `textContent`** : un `/^35\./` ne s'ancre jamais sur une ligne de nœud.
  Relever l'index en JS via `.rp-name`, puis viser par `.nth()`.
- (bis) **`.research-btn` matche AUSSI le bouton Port** → viser par `data-tut`.

---

## 6. Points en suspens

- **La carte devient le seul chemin de réparation.** Un joueur habitué au bouton 🛠 de la barre
  d'inventaire ne le retrouvera pas ; le badge et la pastille ont migré sur le bouton Carte, et les
  deux halos (tutoriel + guide) l'y désignent. Rien ne subsiste de l'ancien emplacement.
- **La cible « mystère » de l'île 6 affiche son libellé « Île 6 ».** L'existence de l'île n'est donc
  pas cachée à ce stade — mais elle ne l'était déjà plus : le nœud « Accès Île 6 » et le titre du
  bouton la nomment tous deux. Seule son **apparence** reste la surprise, et elle est préservée.
- **La liaison `repairable` n'est pas animée** : le pointillé est statique (CSS du brief, appliqué tel
  quel). Si l'attention doit être plus forte, une animation de `background-position` suffirait.
- **`I18N.t("Faire défiler")` du lot 1** reste le seul libellé français résiduel de la refonte Carte.
- **Nom du rapport vérifié libre avant écriture** (leçon du build 15.1).

---

## Vocabulaire

Conformément au brief, **« île 7 » n'apparaît nulle part** dans ce rapport pour désigner le lieu : il
s'appelle **le souterrain**, affiché « Île 6 S ». L'id 7 n'est employé que pour parler de code
(`island: 7`, `game.port[7]`), où il est correct.
