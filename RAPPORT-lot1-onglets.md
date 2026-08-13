# RAPPORT — LOT 1 : bande d'onglets d'île (sprites du souterrain + affordance de défilement)

**Livré : `GAME_BUILD = 395`, `GAME_VERSION = 'Alpha 16.2'`, `SAVE_VERSION = 31` (INCHANGÉ).**

Base d'exécution : build **394 / Alpha 16.1**, SHA-256 `148eee3530a2f892c22eb11fe4372f2b37e3e35658f00a9af39fee271206ba38`,
3 427 717 o — **identique au caractère près** à la source de référence annoncée par le brief. Aucune
adaptation d'ancre n'a été nécessaire.

---

## 1. Application du patch

Les 3 pièces jointes ont été **hachées avant usage** et concordent avec la table du brief :

| pièce | taille | SHA-256 | verdict |
|---|---|---|---|
| `patch_lot1.py` | 7 711 o | `704d76dad10f12d2406030484a5738febd21a27877575829611e4b35db2063d0` | conforme |
| `art/onglet-souterrain-64.png` | 878 o | `6b8935d8deba78398335a5a4ce52b6bb1c1eb0937c3f44dc9380e76ac1b7d279` | conforme |
| `art/onglet-souterrain-gris-64.png` | 826 o | `02b42c5626fc758ccf258c16c45bd3963723e974b01f118f82af4b21040d38e0` | conforme |

Sortie du patcher, les 4 lignes `OK` :

```
OK 1 sprites : ile_7 + ile_7_gris injectes
OK 2 CSS : wrapper + voiles + chevrons
OK 3 repli : numero interne -> S
OK 4 composant : wrapper + refs + voiles + chevrons
delta octets : +6045
```

**Delta constaté : +6 045 o** (3 427 717 → 3 433 762) — **exactement la valeur annoncée**, au byte près.
Après bump de version : 3 436 454 o (+2 692 o de commentaire cumulatif et de `GAME_NOTES`).

**Idempotence vérifiée, pas supposée** : seconde passe du patcher → les 4 gardes se déclenchent
(`deja present` ×4) et le **delta est de +0 octet**.

---

## 2. Contrôles statiques

- **`node --check` : 7/7 OK**, avant ET après le bump de version.
  Extraction par `(?m)^<script` puis balayage jusqu'au `</script>` correspondant. ⚠ Le piège annoncé
  par le brief est **reproduit** : un scanner naïf `<script\b[^>]*>` rend **11** correspondances sur ce
  fichier (chaînes et commentaires) contre **7** blocs réels. Un rapport qui annonce « 11 blocs »
  décrit son scanner, pas le fichier.
- **Parenthèses du site de rendu React** (`IslandSelector` entier) : **67 ouvrantes / 67 fermantes**,
  accolades 23/23 — la valeur du brief est retrouvée à l'identique.
- **Ordre de la feuille de style préservé** : le bloc du lot 1 s'insère ligne **80** (juste après
  `.island-tab-ico`), le bloc du lot C reste à la ligne **1446**, donc **toujours le dernier**.
  Contrainte des builds 384-386 respectée.
- **Octets des PNG, pas ré-encodage** : les data-URL ont été **re-décodées depuis le fichier patché**
  et comparées aux fichiers source → **identiques octet à octet** (878 o / 826 o, SHA `6b8935d8…` /
  `02b42c56…`). Les deux PNG sont en **colortype 6 (RGBA)** : la transparence est dans le canal alpha,
  aucun `tRNS` de palette à préserver — mais le contrôle vaut d'être fait, il est le seul à distinguer
  un inlining d'un ré-encodage.

### SHA-256 des 7 blocs, re-extraits APRÈS la toute dernière modification

Fichier entier : **`1d0d0cd864f800970547caec974e31a525170f5cba36872215c6f98773360177`** — 3 436 454 o.

| bloc | SHA-256 (16 car.) | taille |
|---|---|---|
| blk1 | `a50c1c4e7f4a304c` | 418 |
| blk2 | `8fbb22187703339c` | 4 397 |
| blk3 | `d949f1c3687aedad` | 10 751 |
| blk4 | `35f4f974f4b2bcd4` | 131 835 |
| blk5 | `1be53ce44e7be14f` | 1 113 969 |
| blk6 | `f6cdea55af87528f` | 239 836 |
| blk7 | `a922319b45eac9f9` | 1 680 770 |

---

## 3. Suite de validation exécutée

Chromium headless (`/opt/pw-browsers/chromium-1194`), page servie **depuis la racine du dépôt**,
locale forcée `fr`, attente de la **suppression** de `#splash` (et non du seul drapeau), purge des
astuces par `.tip-ok` (jamais le premier bouton, qui est `.tip-dismiss`).

| test | verdict | montage et valeurs mesurées |
|---|---|---|
| **T1** pré-compilation | **PASS** | 7/7 `node --check`, scanner ancré ; scanner naïf = 11 (piège reproduit) |
| **T2** sprites chargés | **PASS** | 390×780. `SPRITE_DATA['ile_7']` **1 194 car.**, `['ile_7_gris']` **1 126 car.**, tous deux préfixés `data:image/png;base64,`, **distincts**, et **décodés en `Image` : 64×64 tous les deux** |
| **T3** voile ancré au wrapper | **PASS** | 390×780. Au repos, bord droit voile/bande **370 / 370** (écart 0). Après `scrollLeft = scrollWidth` (+400 ms), bord gauche **216,75 / 216,75** (écart 0). Coïncidence dans les DEUX états |
| **T4** état des chevrons | **PASS** | 390×780. Repos : droit **présent**, gauche **absent**. Fin de course : gauche **présent**, droit **absent** |
| **T5** onglet souterrain | **PASS** | 430×780, `islandUnlocked[7]=true` + `ui.dev=true` sur `__gameRef.current`, rendu forcé par clic, +900 ms. **7 onglets** ; dernier = `<img class="island-tab-ico">`, `data-tut="island-7"`, `title="Île 6 S"`, **26 px**, `textContent` **vide**, `scrollWidth` **194** |
| **T6** le chevron défile | **PASS** | 360×780. **Clic PHYSIQUE** Playwright (`elementFromPoint` = `island-chev r`, cible réellement atteignable). `scrollLeft` **0 → 43 = scrollWidth − clientWidth** (166 − 123) |

**0 `pageerror`** sur l'ensemble de la suite. Une seule erreur console, un **404 `Failed to load
resource`** du serveur de test — bruit **PRÉEXISTANT** documenté depuis le build 14.47, sans rapport
avec le lot.

### Table des 8 largeurs — reproduite à l'identique

| largeur | `scrollWidth` | `clientWidth` | déborde | chevron D | chevron G | erreurs |
|---|---|---|---|---|---|---|
| 320 | 166 | 83 | oui | oui | non | 0 |
| 360 | 166 | 123 | oui | oui | non | 0 |
| 390 | 166 | 153 | oui | oui | non | 0 |
| 430 | 166 | 166 | non | non | non | 0 |
| 560 | 166 | 166 | non | non | non | 0 |
| 768 | 171 | 171 | non | non | non | 0 |
| 1024 | 171 | 171 | non | non | non | 0 |
| 1440 | 171 | 171 | non | non | non | 0 |

**Les trois nombres se calculent et sont retrouvés** : `6×26 + 5×2 = 166` (gap 2 px sous 560 px),
`6×26 + 5×3 = 171` (au-delà), `7×26 + 6×2 = 194` avec le souterrain. Le plancher tactile de 26 px
tient donc sur toute la plage.

### Contre-épreuve sur la BASE 394 — le test est falsifiable

Même montage, même viewport (390×780), les deux fichiers servis côte à côte :

| | base 394 | patch 395 |
|---|---|---|
| dernier onglet : `<img>` | **non** | oui |
| dernier onglet : `textContent` | **`"7"`** | `""` |
| `SPRITE_DATA['ile_7']` | **absent** | présent |
| `.island-tabs-wrap` | **absent** | présent |
| voile droit / chevron droit | **absents** | présents |
| `scrollWidth` | 194 | 194 |

**Cinq verdicts opposés pour un montage identique.** La base affiche littéralement **`7`** sur
l'onglet du souterrain — le défaut décrit par le brief est **reproduit avant d'être corrigé**, il
n'était pas supposé.

---

## 4. Écarts par rapport au brief

1. **Branche.** Le brief demande `claude/lot1-onglets` ; la consigne de session impose
   `claude/carte-archipel-wmyxbs` et interdit toute autre branche sans autorisation explicite. La
   consigne de session l'emporte. **Livré sur `claude/carte-archipel-wmyxbs`.**
2. **Numéro de version.** Le brief n'en propose aucun ; le dépôt était bien à 394 / Alpha 16.1 comme
   il l'anticipait (vérifié, non supposé) → **395 / Alpha 16.2**.
3. **Une assertion de ma première passe de T2 était fausse, pas le produit.** J'avais comparé le
   préfixe des data-URL à `data:image/png;base64` (21 caractères) tout en en tranchant 22 — la
   virgule finale faisait échouer l'égalité alors que les deux sprites étaient parfaitement chargés.
   Corrigée et renforcée (décodage réel en `Image`, dimensions vérifiées), le test passe. Signalé
   parce qu'un « KO » de harnais lu trop vite se serait transformé en défaut imaginaire.

Aucun autre écart : les 4 ancres sont sorties uniques, aucune n'a dû être re-dérivée.

---

## 5. Point ouvert du brief, instruit et tranché

**⚠ Le backdrop à 320 px n'est PAS un comportement propre à 320 px — et ce n'est pas un défaut.**

Le brief signalait n'avoir pas pu valider le clic *physique* à 320 px, `document.elementFromPoint`
renvoyant `.research-backdrop`, et demandait d'établir **pourquoi un panneau couvre l'écran au boot à
cette largeur**. Mesuré **sans aucune purge d'overlay**, aux deux largeurs :

```
SANS PURGE 320px : backdrop=true tip=true overlays=["research-backdrop","tip-popup"] → chevron non atteignable
SANS PURGE 390px : backdrop=true tip=true overlays=["research-backdrop","tip-popup"] → chevron non atteignable
```

C'est **l'astuce de bienvenue** (`.tip-popup` et son `.research-backdrop`) qui s'ouvre au boot d'une
partie neuve, **à toutes les largeurs**. Aucun panneau ne s'ouvre « tout seul à 320 px » : le
comportement est **indépendant de la largeur et indépendant du lot** (c'est le piège de harnais
documenté depuis le build 13.50 — « le backdrop d'un popup avale le clic »). Une fois l'astuce fermée
par `.tip-ok`, geste que le joueur fait de toute façon, le chevron est atteignable : **prouvé par un
clic physique réel à 360 px** (T6), qui déplace effectivement la bande.

**Conclusion : rien à corriger ici**, et rien à corriger ailleurs — le brief demandait de le noter
sans le traiter si le comportement s'avérait indépendant du lot. C'est le cas.

---

## 6. Points en suspens

- **`I18N.t("Faire défiler")` n'existe dans aucune table `LOCALES`** — le libellé sortira en français
  dans les 4 langues. **Assumé explicitement, non ajouté** : la chaîne ne porte que `title` et
  `aria-label`, aucun texte à l'écran, et l'ajouter aux 4 tables pour deux attributs élargirait le
  périmètre d'un lot qui doit rester une bande d'onglets. Elle **rejoint le lot i18n de l'audit 381**
  (321/858 libellés non traduits) et est consignée comme telle dans le commentaire de version, pour
  qu'elle ne se perde pas.
- **Thème inox** : voiles et ombre portée des chevrons passent tous par `var(--panel)`, que le thème
  redéfinit — **aucune couleur en dur n'a été introduite**, le thème suit sans retouche.
- **Hors périmètre, non anticipé** (lots 2 à 4 de la refonte Carte) : bouton dédié, navigation,
  réparation unifiée. Rien n'en a été amorcé ici.
- Rappel de méthode conservé : `I18N.applyToData` **fusionne les tableaux `body` par index** — sans
  objet dans ce lot (aucun texte de données touché), mais toute retouche de texte ailleurs impose de
  vérifier qui d'autre écrit le champ.
- **Nom du rapport vérifié libre avant écriture** (leçon du build 15.1) : `RAPPORT-LOT1-UI.md` existe
  déjà et décrit le lot « panneaux UI » du build 382 — sans rapport. Ce rapport est donc
  `RAPPORT-lot1-onglets.md`.
