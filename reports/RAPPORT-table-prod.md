# RAPPORT — Lot TABLE-PROD

Le tableau du panneau Production redevient lisible en portrait téléphone.
Base : `main` @ `01e85ba` (build 431 / Alpha 19.8). Branche : `claude/code-audit-qbbdio`.

## 1. Version produite

| | |
|---|---|
| `GAME_BUILD` | **432** (431 → 432) |
| `GAME_VERSION` | **Alpha 19.9** |
| `SAVE_VERSION` | **31 — INCHANGÉ** (aucun champ de partie, correctif 100 % CSS) |

Numéro choisi après relevé de `GAME_BUILD` sur **toutes les branches distantes** (règle
anti-collision) : maximum = 431 (`main`, `claude/discord-vapsvx`, `claude/code-audit-qbbdio`),
donc 432 libre. Entrée « 19.9 — LOT TABLE-PROD » ajoutée **en fin** du bloc de commentaire
cumulatif, juste avant la constante de build, sans effacer les lignes précédentes.

`GAME_NOTES` réécrit en **ASCII pur, sans guillemet droit** (la CI extrait par `[^"]*` et
tronquerait silencieusement).

## 2. Ancre appliquée

Ancre unique du §5, **jamais retapée** — le patcher joint la porte en littéral Python :

```
.prod-empty{color:var(--ink-dim);font-style:italic;font-size:.78rem;padding:8px 0;}\n
```

`count == 1` vérifié **avant** application (le patcher sort en code 1 sinon). Bloc inséré
juste après, dans la section « Panneau Production » — **pas en fin de feuille** (invariant
Lot B / Lot C, cf. §7 des écarts).

**Idempotence vérifiée** : patcher rejoué → « DEJA APPLIQUE (sentinelle presente) », SHA du
fichier strictement identique.

## 3. Empreintes — ré-extraites du fichier, jamais transcrites

**Avant bump** (le patch seul) — comparaison directe avec le §7 du brief :

| Contrôle | Attendu (brief) | Mesuré | |
|---|---|---|---|
| SHA-256 du fichier patché | `311e4035…de65d` | `311e4035f1d27db940640997d6a3818c5a6cb6691582bd954f0e7117535de65d` | ✅ **conforme** |
| SHA-256 du bloc `<style>` | `683c103b…aabe` | `683c103be13c3bf2ca53b6c274d99d3f587ef39c503a8215543e512ccf57aabe` | ✅ **conforme** |
| Delta octets | +2604 | **+2604** | ✅ exact |
| Blocs `<script>` (`^<script`) | 7 | 7 | ✅ |
| Accolades du bloc `<style>` | 961 / 961 (955 + 6) | **961 / 961** | ✅ |

> **Cas rare** : les DEUX SHA-256 du brief concordent avant bump → le patch appliqué ici est
> **byte-identique** à celui du rédacteur. Rien n'a été recopié à la main.

**Après bump** (avant la rectification du §6.8) :

| | |
|---|---|
| SHA-256 du fichier | `3d154cba26812ea5842a7a0786da4b8d1c24ba16a283471d8a598da3f12973cd` |
| SHA-256 du bloc `<style>` | `683c103be13c3bf2ca53b6c274d99d3f587ef39c503a8215543e512ccf57aabe` — **inchangé** |

> Le SHA du bloc `<style>` **survit au bump** : les constantes de version vivent dans le bloc 7.
> C'est la preuve que le bump n'a pas touché une ligne de CSS.

**État LIVRÉ** (après rectification d'une coquille de commentaire, §6.8) :

| | |
|---|---|
| SHA-256 du fichier | `25aa5afd0346d9b4887f224beae1f6a12d80d35333a9451dcd91f8cfcdc307f5` |
| SHA-256 du bloc `<style>` | `e72b8b3e4291e3b95642996f4375a623109331f12ef2950569e6e2e38484ffc2` |
| Taille | 3 772 618 o |
| Delta total vs `main` | **+7077 o** (2604 le patch + le commentaire cumulatif + le bump + 90 la rectification) |
| Accolades `<style>` · blocs `^<script>` | 961 / 961 · 7 |

## 4. Contrôles de syntaxe et gardes CI

- **`node --check` : 7/7 sur les TROIS variantes CI** — `game-public.html`, `game-dev.html`
  (sed `DEV_BUILD`), `game-store.html` (sed `SELF_UPDATE` + `SUPPORT_URL`). Extracteur
  séquentiel qui **compte les blocs avant de boucler** et refuse de conclure si ≠ 7.
- **Accolades CSS** : 961 / 961.
- **Gardes de la CI rejouées APRÈS écriture de mes commentaires** (leçon du run 561) :

| Garde | Attendu | Mesuré |
|---|---|---|
| `grep -c 'ko-fi'` (publique) | 1 | **1** |
| `grep -c 'ko-fi'` (magasin, après sed) | 0 | **0** |
| `grep -c 'const SELF_UPDATE = true;'` (publique / magasin) | 1 / 0 | **1 / 0** |
| `^const DEV_BUILD = false;$` | 1 | **1** |
| Gardes d'entrée et de sortie des 2 sed magasin | passent | **passent** |

- **Extractions de la CI** (qui prennent la **première** occurrence, motif non ancré — piste M2
  de l'audit 431) : `GAME_BUILD → 432`, `GAME_VERSION → Alpha 19.9`, `GAME_NOTES → …` complet et
  non tronqué. Les 3 motifs sortent à **1 occurrence** : mon commentaire cumulatif n'en introduit
  aucun en texte libre.

## 5. Suite de validation T1-T5

**Montage réellement exécuté** : Chromium 1194 headless, fichier servi en **HTTP réel**
(`http://127.0.0.1:8931`, jamais `file://`), DPR 3, locale forcée `fr`, `localStorage` purgé via
`addInitScript` **avant** tout script du jeu, astuces fermées par `.tip-ok` (jamais `.remove()`),
tutoriel passé, panneau ouvert en cliquant `.inv-prod-btn` **en boucle jusqu'à ce que
`.prod-panel` existe** (`useGhostGuard`). Mesures par `getComputedStyle` + `getBoundingClientRect`.

### Géométrie (mesurée sur `.prod-row.prod-head`, présente même tableau vide)

| Viewport | `grid-template-areas` | Colonnes résolues | Hauteur | Verdict |
|---|---|---|---|---|
| **360** | `"res res res" "prd cns net"` | **96,80 / 96,80 / 96,80** | 30 px | **T1 PASS** |
| **520** | `"res res res" "prd cns net"` | 146,92 / 146,94 / 146,94 | 30 px | **T2 PASS** |
| **521** | `none` | **169,73 / 88 / 88 / 92** | 16 px | **T3 PASS** |
| **800** | `none` | **240 / 88 / 88 / 92** | 16 px | **T4 PASS** |

- **T1** : colonne du nom = **298,39 px** (contre 18,39 px avant patch) ; les trois nombres à
  **96,80 px**, soit **plus** que les 88/92 px d'origine → aucune troncature de nombre n'est
  introduite (nombre le plus large mesuré : 78 px ; en-tête `CONSO /S` : 68 px).
- **T3** — *le test qui compte* : place réelle pour le texte du nom = 169,73 − 16 (icône) − 6
  (gouttière flex) = **147,73 px ≥ 126,75 px** (plus long libellé). Le seuil ne laisse donc
  **aucune bande de largeurs repassée en ligne unique avec un nom qui ne tient pas**.
- **T4** : à 521 **et** à 800 px, `areas`, colonnes et hauteur sont **strictement identiques à la
  base non patchée** (comparaison directe base ↔ patch) → zéro régression au-dessus du seuil.
- **T5** : **0 `pageerror`** sur les 4 viewports, base comme patch.

### Troncature réelle — contre-épreuve (hors brief, ajoutée)

Lignes de données forgées en réassignant **`islandFlowAgg` sur `window`** (déclaration de
fonction d'un script classique) — écrire dans `game.netFlow` aurait été écrasé au tick suivant.
5 ressources dont le plus long libellé. Troncature mesurée par `scrollWidth > clientWidth`
**et** `scrollHeight > clientHeight` (`.pc-res` est en `overflow:hidden` : le texte peut aussi
se couper en hauteur).

| | 360 px | 520 px | 521 px |
|---|---|---|---|
| **Base (non patchée)** | colonne nom **18,39 px** — **5/5 libellés TRONQUÉS** (`mot.quantique` : 149 px de contenu pour 18 px visibles) | 168,80 px, 0 tronqué | 169,73 px, 0 tronqué |
| **Après patch** | 298,39 px — **0 tronqué** | 448,80 px — 0 tronqué | 169,73 px — 0 tronqué |

Le bug d'origine est donc **reproduit** sur la base et **fermé** sur le patch : la suite mesure
bien quelque chose.

**Contrôles complémentaires** (12 cellules par viewport, en-tête incluse) : **0 cellule
tronquée**, `.prod-body` sans débordement horizontal (`scrollWidth == clientWidth`), panneau
entièrement dans le viewport. Capture 360 px contrôlée à l'œil : `mot.quantique`, `si.raffiné`,
`ciment irr.` s'affichent en entier, les trois nombres alignés dessous.

## 6. Écarts au brief et précisions

1. **Aucun écart de contenu.** Le patcher a été exécuté **tel quel**, les deux SHA-256 du §7
   concordent avant bump. Rien n'a été adapté.
2. **Numéro de version** : le brief n'en propose aucun ; 432 / Alpha 19.9 retenus après relevé
   sur toutes les branches distantes.
3. **Piège de banc découvert (à consigner pour la prochaine reproduction)** : mesurer la largeur
   d'un libellé via le sélecteur `.prod-row .pc-res` vise l'**en-tête** (`.68rem`) et rend
   **110,5 px**, pas les 126,75 px du brief. Le rapport des deux vaut exactement 0,78 / 0,68 : le
   brief est juste, c'est la mesure qui doit être faite dans la police d'une **vraie ligne de
   données**. Même écart sur le nombre large (68 px en en-tête ↔ **78 px** en ligne). Après
   correction du sélecteur, les deux valeurs du brief sont retrouvées **au centième**.
4. **Hauteur de ligne** : le brief annonce « 16 → 30 px », exact pour l'**en-tête**. Les lignes de
   **données** (qui portent l'icône 16 px) passent de **23 à 38 px**. Le coût de défilement est
   donc un peu supérieur à l'annonce ; `.prod-body` (`overflow-y:auto`) et `.prod-panel`
   (`max-height:84vh`) l'absorbent, mesuré sans débordement.
5. **Marge du seuil, mesurée et assumée** : entre ~489 et 520 px, la ligne unique tenait **déjà**
   sur la base (mesuré à 520 px : colonne nom 168,80 px, 0 troncature). Les deux lignes y sont
   donc un **faux positif volontaire**. Le brief le calibre ainsi pour absorber la barre de
   défilement classique (~8,6 px) d'un navigateur de bureau, absente du banc headless où elle est
   en surimpression. **Seuil laissé à 520** : l'abaisser sans re-mesurer **avec** une barre
   classique rouvrirait le risque que T3 est là pour garder.
6. **Contrôles ajoutés hors brief** : troncature réelle (largeur *et* hauteur), débordement
   horizontal, contre-épreuve complète sur la base non patchée, gardes CI rejouées après
   rédaction des commentaires.
7. **Coquille du patcher rectifiée (seul écart au « tel quel »)** : le commentaire inséré par
   `patch_prod_table.py` annonce « les trois nombres en dessous a 1fr = **93,46** px chacun ».
   La valeur est fausse — la mesure donne **96,80 px**, et le §3 du brief écrit lui-même 96,8.
   Le chiffre a été corrigé **dans le commentaire seul** (mention de la coquille conservée sur
   place) ; **aucune ligne de CSS n'est touchée**, la géométrie re-mesurée après rectification est
   strictement identique. Ordre des opérations : patch appliqué **tel quel** et conformité des
   deux SHA-256 du brief **prouvée d'abord** (§3), rectification **ensuite**, d'où les deux jeux
   d'empreintes. Motif : un commentaire faux gravé dans le mono-fichier est précisément la dette
   que l'audit du build 431 vient de relever ailleurs (`makeIcon`) — la laisser passer ici aurait
   été incohérent.
8. **Vérification faite avant d'appliquer** (elle aurait rendu le bloc entièrement inerte si elle
   avait échoué) : `grid-area` n'agit que sur les enfants **directs** d'une grille — les 4
   cellules (`resCell` + 3 `span`) sont bien enfants directs de `.prod-row` au JSX. Et aucune
   autre règle du fichier ne cible `.prod-row` (seules l. 244-252 ; l. 882-883 ne visent que
   `.prod-tab`), ce qui valide le placement hors de la fin de feuille.

## 7. Hors périmètre — non traité (conforme au §9)

- **Formats mixtes dans une même colonne** (`8504,32` · `3,5e5` · `+1 024`) : gêne le balayage
  vertical, visible sur la capture. Cosmétique à côté de la troncature ; toucher `fmtRateSci` /
  `NUM_THRESHOLD` élargirait le diff bien au-delà d'un bloc CSS. **À traiter séparément.**
- `RES_SHORT`, `ProductionPanel`, `SAVE_VERSION` : intacts. Aucune ligne de JS modifiée.

## 8. Points ouverts

1. **Rendu sur appareil non couvert.** Le banc est headless : le signalement venait d'un Galaxy
   S25 FE en portrait. La géométrie et l'absence de troncature sont prouvées à 360 px CSS, mais
   la lisibilité perçue (hauteur de ligne doublée, longueur du défilement avec ~23 ressources
   actives) mérite un coup d'œil sur l'APK dev.
2. **Bande 489-520 px** : deux lignes là où une suffirait sur un appareil sans barre de
   défilement classique (cf. écart 5). Réglable en une valeur si Ethan préfère la densité, mais
   à ne faire qu'avec une mesure **avec** barre.
3. Le lot est **web uniquement** : aucun `.aab` à soumettre, la fenêtre de test fermé Play Store
   n'est pas concernée. Le merge republiera APK, `index.html` et `version.json`.
