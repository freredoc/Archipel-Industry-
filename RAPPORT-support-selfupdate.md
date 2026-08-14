# RAPPORT — Lien de soutien (Ko-fi) + garde `SELF_UPDATE`

Brief : `BRIEFsupportselfupdate.md` · patcheur fourni : `patch_support_selfupdate.py`

## Version produite

| | |
|---|---|
| `GAME_BUILD` | **417** (416 → 417) |
| `GAME_VERSION` | **Alpha 18.4** (Alpha 18.3 → Alpha 18.4) |
| `SAVE_VERSION` | **31, INCHANGÉ** — aucun champ persisté, aucune migration |

Numéro choisi après relevé de `GAME_BUILD` sur **toutes** les branches distantes
(`origin/main` = 416, `origin/claude/playstore-preparation-g0w8vb` = 416) → **417 libre**.

`GAME_NOTES` (UTF-8 littéral, aucun guillemet droit) :

> Une section « Soutenir le projet » facultative apparait desormais en bas des Options, juste
> au-dessus de la ligne Version. C'est un simple lien externe, sans aucune contrepartie en jeu :
> rien ne change dans la partie.

## Base

Source live re-fetchée avant exécution, **conforme au brief au caractère près** :
`3 682 587 o`, `sha256 = 7ba46cf4d9963c22d1d01a381eccefb6f8fd3859f603adaa94645b05436e321a`.

## Les 5 ancres, count vérifié AVANT application

| # | Ancre | count | Action | Vérif après |
|---|---|---|---|---|
| 1 | `const SUPPORT_URL = '';` + son commentaire | **1** | commentaire réécrit (délimité par parcours de lignes, jamais retapé) + valeur `https://ko-fi.com/freredoc` | −220 / +683 car. |
| 2 | `const VERSION_URL = '…/version.json';` | **1** | `const SELF_UPDATE = true;` + commentaire insérés juste après | +1 086 car. |
| 3 | `useEffect(() => {\n    let cancelled = false;\n    fetch(VERSION_URL` | **1** | `if (!SELF_UPDATE) return;` en tête d'effet | +33 car. |
| 4 | `React.createElement("button", {\n    className: "opt-upd-btn",` | **1** | préfixé de `SELF_UPDATE && ` | +15 car. |
| 5 | `updStatus())));` | **1** | préfixé de `SELF_UPDATE && ` | +15 car. |

**Delta du patch seul : +1 609 caractères** — la valeur du brief au caractère près
(fichier intermédiaire `3 684 236 o`, `sha256 = 60b7820463aaa8be310d665133f1e9883cb29d0ad4b52b28b72e539b4192df6f`,
identique au SHA pré-compilé du brief).

**Idempotence vérifiée** : le patcheur rejoué sur le fichier LIVRÉ affiche « Déjà patché — aucune
modification » et produit un fichier au SHA identique.

**Diff HTML : 8 lignes retirées seulement** (les 2 lignes de version, `GAME_NOTES`, les 2 lignes de
commentaire + la ligne `const SUPPORT_URL`, les 2 lignes JSX des sites 4 et 5). Le commentaire
historique **« 14.54 » qui mentionne `SUPPORT_URL` n'a PAS été réécrit** (vérifié : intact).
Le bloc de commentaire de version est **cumulatif** — la ligne 18.4 est ajoutée, aucune ligne
antérieure effacée.

`SELF_UPDATE` : **4 occurrences du jeton** (1 déclaration + 3 gardes), toutes dans le bloc 7 et
toutes **après** la déclaration → aucune zone morte temporelle.

## SHA-256 re-extraits des blocs (jamais recopiés du brief)

Extraction par `(?m)^<script` (et non `<script[^>]*>`, qui matcherait les occurrences textuelles
dans les chaînes et les commentaires).

### Variante HORS MAGASIN — fichier livré
`3 686 292 o` · `sha256 = 15652fb6c0ba2ad401980e9bd9b9ad5b373dc55361963dc1649b3cfcbec0b011`
Delta vs base 416 : **+3 705 o** (dont +1 649 o pour le patch, le reste = bump, commentaire de
version cumulatif et `GAME_NOTES`).

```
bloc 1        418 o  a50c1c4e7f4a304c650c0cfa7e06c4ffdfb0d87510d83738114d4dd5a1641628
bloc 2       4397 o  8fbb22187703339c146b2f82badd8701d199128cd3d9dd8673d213c8616ca541
bloc 3      10751 o  d949f1c3687aedadcedac85261865f29b17cd273997e7f6b2bfc53b2f9d4c4dd
bloc 4     131835 o  35f4f974f4b2bcd44da73963347f8952e341f83909e4498227d4e26b98f66f0d
bloc 5    1113969 o  1be53ce44e7be14fb81bd92e6a338cba274304f38c6077061fd3e33232cc2651
bloc 6     430160 o  268c19eb9a15b15e0ede8b61f6e173223c50d7657d3094397150be485b01b6fb
bloc 7    1734254 o  4a4d273de4a3b3d8fffe846bd04c2a0347dac85fd2c8a1cd51d46ff356416dce
```

### Variante MAGASIN — produite par les 2 `sed`
`3 686 267 o` (**delta −25 o**) · `sha256 = 2656862df9ce082c82d51575c24b9bb983b781c39ec12c6da06ae8b1990e363e`

Les blocs 1 à 6 sont **identiques** à la variante hors magasin. Seul le bloc 7 change :

```
bloc 7    1734229 o  4c42214be8724c7f706c20af2d8522ed5d28d3eece023801c6afaeaeb6c35774
```

## Tests

Chromium 1194 (`/opt/pw-browsers/chromium-1194/chrome-linux/chrome`) via Playwright, viewport
420 × 900, DPR 2, locale `fr-FR`. `localStorage` vidé par `addInitScript` **avant** navigation.
Clics par `page.mouse.click()` à de vraies coordonnées, jamais `el.click()`. Overlays fermés par
`.tip-ok`, jamais `remove()`. **Suite rejouée 2 fois sans flottement : 12 PASS / 0 KO.**

| Test | Verdict | Montage réellement exécuté |
|---|---|---|
| **T0** panneau Options réellement ouvert | **PASS** | assertion ajoutée hors brief — sans elle, T1b/T2-magasin/T3 seraient passés **à vide** (cf. écart n° 2) |
| **T1** section de soutien rendue, hors magasin | **PASS** | `.slot-list` défilée en butée, `getAttribute('href')` du `a.slot-new.opt-fullbtn` lu → **exactement `https://ko-fi.com/freredoc`**. L'attribut est LU, pas seulement le nœud compté |
| **T1b** contre-mesure magasin | **PASS** | 0 nœud `a.slot-new.opt-fullbtn` sur `store.html`, même harnais |
| **T2** bouton de MAJ suit la garde | **PASS** | `.opt-upd-btn` compté sur les **deux** fichiers avec le même harnais : **1** hors magasin, **0** en magasin |
| **T3** pas de trou de mise en page | **PASS** | `getBoundingClientRect` : `.opt-version-row` = **32 px** en magasin (> 0), ≥ son `.opt-lbl` (21 px), et ≤ la même ligne hors magasin (39 px). Défilement mis **en butée** (`scrollTop 767 / max 767`) puis `last.bottom 810 ≤ list.bottom 820` |
| **T4** aucun appel réseau au lancement, magasin | **PASS** | `page.on('request')`, 5 s d'observation : **0** requête `version.json` en magasin. **CONTRE-MESURE JOUÉE** : **1** requête hors magasin sur le même harnais → le test est falsifiable |
| **T5** gardes d'artefact | **PASS** | `grep -c "ko-fi"` = **0** · `grep -c "const SELF_UPDATE = true;"` = **0** · `grep -c "const SELF_UPDATE = false;"` = **1**. Contrôle miroir hors magasin : `ko-fi` = **1** |
| **T6** compilation des 2 variantes | **PASS** | `node --check` **7/7 sur la variante hors magasin ET 7/7 sur la variante magasin** ; les **deux** bootées en vrai navigateur avec ouverture du panneau Options (captures contrôlées) |

Captures : panneau Options hors magasin (« Archipel Industry est gratuit et le restera. » +
« ❤ Soutenir le projet » + « Version Alpha 18.4 · build 417 » + « Vérifier les mises à jour ») et
panneau Options magasin (ni section de soutien, ni bouton de MAJ, ligne Version intacte sur une
seule ligne).

## CI — workflow TROUVÉ et MODIFIÉ

Le workflow qui substitue déjà `DEV_BUILD` est **`.github/workflows/android.yml`**, étape
**« Prepare game files »** (renommée `… (public + dev + magasin)`). Les deux `sed` du brief y ont
été ajoutés, au même endroit, avec :

- **grep de garde À L'ENTRÉE** sur les deux lignes exactes (échec dur si une ligne `const` a été
  renommée ou reformatée) ;
- **grep de garde À LA SORTIE** : `ko-fi` = 0 et `const SELF_UPDATE = true;` = 0 dans la variante ;
- **contre-garde** (ajout hors brief) : `game-public.html` doit CONSERVER `SELF_UPDATE = true` et
  son unique occurrence `ko-fi` — sans elle, une inversion de `sed` priverait silencieusement la
  PWA et les APK sideloadées de leur auto-updater et de leur lien de soutien.

L'étape a été **simulée en local** sur le fichier réellement livré : elle reproduit
`game-store.html` **au SHA près** (`2656862d…`) du fichier passé aux tests. YAML relu par
`yaml.safe_load` : 15 étapes, structure valide.

⚠ **`game-store.html` n'alimente AUCUN APK de ce workflow** : un paquet magasin exige une coquille
native distincte (sans `REQUEST_INSTALL_PACKAGES` ni pont `ArchipelNative.update`), qui n'existe
pas et qui est **hors périmètre**. Il est donc téléversé dans un artefact **dédié**
`ArchipelIndustry-STORE-html` (étape séparée, pour ne pas polluer l'artefact APK).

## Écarts par rapport au brief, avec leurs raisons

1. **Le brief impose « `useGhostGuard(1)` avale le premier clic → cliquer deux fois ». C'est FAUX
   pour le bouton Options**, et c'est le piège le plus coûteux du lot : il OUVRE le panneau au 1ᵉʳ
   clic, et le 2ᵉ clic tombe **hors du panneau** et le REFERME. Résultat mesuré à la 1ʳᵉ passe :
   panneau jamais ouvert, `optOpen: false`, et **T1b / T2-magasin / T3 passaient tous les trois à
   VIDE** (tout à 0 des deux côtés). La règle des 2 clics vaut pour les boutons **internes** d'un
   panneau, pas pour un bouton du HUD qui ouvre ce panneau. Remède retenu : **un seul clic**, puis
   **assertion de l'état atteint** (`.slot-list` présent) avec re-tentative — et **T0 ajouté** pour
   qu'un panneau non ouvert échoue bruyamment au lieu de verdir à vide.
2. **T0 ajouté hors brief** (cf. ci-dessus).
3. **T3 : formulation corrigée après un faux KO.** « Le dernier enfant est atteignable en
   défilement » mesuré par `offsetTop + offsetHeight` sortait `1497 > 1460`… **des deux côtés**, avec
   un écart CONSTANT de 37 px : `offsetTop` est relatif à l'`offsetParent`, qui est `.slot-panel` et
   non `.slot-list`. Mesure refaite par `getBoundingClientRect` **après mise en butée du
   défilement** : `scrollTop == maxScroll` et `last.bottom <= list.bottom`.
4. **Contre-garde CI ajoutée** sur `game-public.html` (cf. section CI).
5. **Artefact de la variante magasin téléversé** (étape `Upload store HTML artifact`) : produire un
   fichier que rien ne consomme et jeter ne rendrait aucun service ; l'étape est explicitement
   commentée comme non branchée sur un APK.
6. **`RAPPORT-support-selfupdate.md`** : nom vérifié libre avant écriture (convention 15.1).

## Points laissés en suspens

- **Coquille native magasin** : c'est elle qui est réellement jugée par Play — la permission
  `REQUEST_INSTALL_PACKAGES` du manifeste et le pont `ArchipelNative.update` doivent être ABSENTS.
  La garde `SELF_UPDATE` n'est que la **deuxième ligne de défense**. Non traité (hors périmètre).
- **`targetSdk` doit atteindre 36** — échéance d'août 2026, donc **échue**. Le projet compile
  aujourd'hui contre `android-34`. **À traiter en priorité** ; non traité ici (hors périmètre).
- **App Store** : risque de rejet 4.2 « fonctionnalité minimale » pour un jeu emballé en WebView, et
  persistance de `localStorage` en WKWebView (purge possible → sauvegardes perdues). À instruire
  avant tout empaquetage iOS.
- **`index.html` / `version.json` / `sw.js`** ne sont pas édités à la main : la CI les régénère
  depuis l'édition publique après un build sur `main`.
