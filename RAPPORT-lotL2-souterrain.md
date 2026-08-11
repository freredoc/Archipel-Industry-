# RAPPORT — Lot L2 (souterrain : foreuses, socle rocheux, chantiers)

Brief : `BRIEFlotL2souterrain` · patcheur `patch_L2.py` (pré-compilé, fourni)
Branche : `claude/temps-souterrain-display-uoonrz` — **PR #371, NON mergée** (le merge appartient à Ethan).

## Version produite

| | |
|---|---|
| `GAME_BUILD` | 387 → **388** |
| `GAME_VERSION` | Alpha 15.4 → **Alpha 15.5** |
| `SAVE_VERSION` | **31, INCHANGÉ** — aucune migration (l'état du socle se *déduit* de la distance) |
| Taille | 3 371 093 → **3 382 524** o (**+11 431**) |
| SHA-256 livré | `102805bbdd2129ff84085d846bfccb1d0700317b4ca05af5bb69d8bde706d1ce` |

Le brief annonçait +7 142 o pour le patch seul. S'y ajoutent le bloc de commentaire cumulatif
(convention projet) et la réécriture de `GAME_NOTES` pour couvrir les deux lots — voir l'écart 1.

## Sortie du patcheur

**Aucun avertissement de base** : le fichier de la branche portait exactement `7f615b11…`.

```
OK - 11 ancres appliquees
SHA-256 fichier patche : e9858d53f8c7772f139e804c62d138965e6384efb9e13d3703df19af7342bee7
```

**Conforme au caractère près** à l'attendu du brief (`e9858d53…`), les 11 ancres à `count == 1`.

Contrôle intermédiaire : **avant le bump**, les 7 blocs `<script>` étaient **tous identiques aux SHA
du brief, bloc 7 compris** (`1bb1c26b…`) — preuve directe que le patch est appliqué à l'identique.

## SHA-256 des 7 blocs, ré-extraits APRÈS la toute dernière modification du HTML

| bloc | octets | sha256 |
|---|---|---|
| 1 | 418 | `a50c1c4e7f4a304c650c0cfa7e06c4ffdfb0d87510d83738114d4dd5a1641628` |
| 2 | 4 397 | `8fbb22187703339c146b2f82badd8701d199128cd3d9dd8673d213c8616ca541` |
| 3 | 10 751 | `d949f1c3687aedadcedac85261865f29b17cd273997e7f6b2bfc53b2f9d4c4dd` |
| 4 | 131 835 | `35f4f974f4b2bcd44da73963347f8952e341f83909e4498227d4e26b98f66f0d` |
| 5 | 1 112 066 | `6066e8c1aeb44929ec5e92d946a936e70451d0c76d7f1375fae084200c308720` |
| 6 | 239 836 | `f6cdea55af87528fdb0faa6d73d1cf2367219509802364d6d0226252832c73d9` |
| 7 | 1 630 071 | `fbebca77b41b9fe1358562aae453cc86f37871d7900cbf76e4274bd33cc7343c` |

**Blocs 1 à 6 byte-identiques au brief** ; l'écart du bloc 7 (`1bb1c26b…` au brief) est celui du
bump, du commentaire cumulatif et de `GAME_NOTES` — que le brief laisse au moment de l'exécution.
Ces empreintes sont mesurées **après** la dernière retouche du HTML (leçon du rapport précédent, qui
portait une empreinte périmée de 217 octets pour l'avoir prise avant une retouche de commentaire).

`node --check` : **7 blocs, 7 OK**.

## Validation — 30 assertions, 30 PASS

Banc : Chromium 1194 headless, serveur HTTP **depuis la racine du dépôt**, viewport 420×900.
Copie de banc `BANC_L2.html` exposant `window.__H` — **supprimée avant le commit**, et son absence
du fichier livré est **asservie par un test** (voir tableau).

Faute de disposer de la sauvegarde de fin de partie d'Ethan, l'île 7 a été ouverte en séance
(`elevatorRepaired`, `islandUnlocked[7]`) et les foreuses **forgées** sur des tuiles ouvertes — le
brief prévient qu'une partie avancée n'en contient aucune et que sans cela les tests 4 à 7 passent à
vide.

### Suite du brief

| # | test | résultat | mesure |
|---|---|---|---|
| V1 | Boot | **PASS** | 0 `pageerror`, aucune page blanche |
| V2 | Géométrie du socle | **PASS** | grille **21 × 25**, élévateur **(10, 11)**, **80** tuiles au cercle 10, **164** tuiles de socle |
| V3 | Bornes du socle | **PASS** | 9 cases à l'est → `false` · 10 cases → `true` |
| V4 | Sens déduit du geste | **PASS** | `tryDrill` → `true`, `drillDir` = **2** (ouest), `buildingSpriteKey('foreuse',0,2)` = **`bat_foreuse_o`** |
| V5 | Verrou du sens | **PASS** | `setDrillDir` → `false`, `drillDir` inchangé (2 → 2) |
| V6 | Réservation du mur | **PASS** | 2ᵉ foreuse de l'autre côté → `false`, aucun `drilling` posé |
| V7 | Socle inforable | **PASS** | `tryDrill` sur socle → `false` (foreuse adjacente présente, donc le refus vient bien du socle) |
| V8 | Exploit fermé | **PASS** | `construction {rem:1000, up:9}` → `tryDowngrade` = **false**, niveau **8 → 8** |
| V9 | Contre-épreuve de V8 | **PASS** | sans `construction` → **true**, niveau **8 → 7** |
| V10 | Pose sur gisement | **PASS** | `['land','resource','coast']` (`coast` ajouté par le moteur au démarrage) |
| V11 | Assets | **PASS** | `tile_i7_socle` et `ui_pause` présents |

### Vérifications ajoutées (hors brief)

| test | résultat | mesure |
|---|---|---|
| Boot du **vrai fichier** de la PR (et non du banc) | **PASS** | build **388** / Alpha 15.5 / SAVE 31, canvas **100 %**, 0 `pageerror` |
| Le banc n'a pas fui dans la livraison | **PASS** | `window.__H` **absent** du fichier livré |
| Socle **réellement dessiné** sur la carte | **PASS** | espion `drawImage` : `tile_i7_socle` dessiné, roche ordinaire toujours dessinée à côté |
| Borne exacte du cercle 10 | **PASS** | tuile de layer **10** → socle ; layer **9** → forable |
| Contraste du socle | **PASS** | luminance roche **31,7** → socle **14,0** (**−55,9 %**), exactement les valeurs annoncées ; frontière franche à la capture |
| Pastille de pause dessinée | **PASS** | `ui_pause` dessinée quand `rate = 0`, **et le glyphe texte « ⏸ » a disparu** |

**V4 et le couple V8/V9 sont les preuves les plus solides**, comme l'annonçait le brief : V4 fait
passer une clé de sprite de `bat_foreuse` à `bat_foreuse_o` sans toucher au moteur de rendu, et V9
prouve que la garde de V8 vise bien *les travaux* et non le geste lui-même.

## Écarts par rapport au brief, et leurs raisons

1. **Base non mergée, PR unique au lieu de deux.** Le brief exige la base 387 **mergée sur `main`** et
   sa propre PR. Or `main` est **toujours en build 386** (`3d9ce553…`) : le lot L1+L4 n'est pas mergé.
   - La **base technique était pourtant exacte** : le fichier de la branche de travail portait
     `7f615b11…`, le SHA de base attendu, et le patcheur n'a émis **aucun avertissement**.
   - La consigne de session interdit de pousser sur une autre branche que
     `claude/temps-souterrain-display-uoonrz` sans autorisation explicite. Le lot est donc **empilé
     sur cette branche, en commit distinct**, et la **PR #371 porte désormais les deux lots**.
   - Conséquence pour Ethan : merger #371 livre L1+L4 **et** L2 en une fois (build 388). S'il veut
     les séparer, dites-le — la scission est triviale, les deux commits sont indépendants.
2. **`GAME_NOTES` réécrit pour couvrir les deux lots** : le joueur qui met à jour depuis 386 reçoit
   L1+L4 **et** L2 ; des notes ne parlant que du hors-ligne auraient été trompeuses.
3. **Rien d'autre.** Les 11 ancres appliquées verbatim, aucune adaptation.

## Points signalés, NON corrigés (conformément au brief)

- Les quatre nouveaux messages (verrou du sens, mur réservé, socle, travaux en cours) ne sont pas
  traduits : repli français dans les 4 autres langues → lot i18n ouvert de l'audit 381.
- La fiche d'une foreuse propose toujours ses boutons N/E/S/O quand un mur de socle est visé ; le
  refus vient au démarrage, par un toast. Griser le bouton relève d'un lot d'interface.
- `isBedrock` ne teste ni l'île ni le terrain : **ses deux appelants le font** (rendu dans la branche
  `water` avec `isl === 7`, `tryDrill` après le contrôle d'île). Ne pas « durcir » la fonction sans
  vérifier ces deux sites.
- **Équilibrage à remonter** : le socle laisse **361 tuiles jouables** (cercles 0 à 9) ; la
  sauvegarde de référence d'Ethan en a déjà ouvert 183, soit **plus de la moitié**. Rien à migrer,
  mais la marge restante est courte.

## Piège de banc payé en séance (nouveau, à ne pas repayer)

Poser les poignées de test **dans le corps du composant** sous forme d'objet
(`window.__H = { tryDrill, curTiles, … }`) déclenche une **zone morte temporelle** : `curTiles` est
déclarée plus bas en `const` → `ReferenceError: Cannot access 'curTiles' before initialization` →
**page blanche**, que `node --check` ne voit pas. C'est exactement pourquoi `window.__ui` est écrit
comme une **fonction** (`() => ({...})`) : une fléchée ne capture rien à la définition. Le banc a été
corrigé sur ce modèle. Le fichier livré n'a jamais porté cette ligne.

Autres pièges reconfirmés : lancer le serveur HTTP **depuis le dépôt** (sinon 404 pris pour une page
blanche) ; fermer les astuces par `.tip-ok` ; replier l'inventaire, qui recouvre le haut du canvas.
