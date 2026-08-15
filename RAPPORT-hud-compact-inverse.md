# RAPPORT — Inversion de la compaction HUD + pastilles 32 px

Brief `BRIEFhudcompactinverse.md`, patcheur `patch_hud_compact.py` + 4 sprites.

Base : **build 419 / Alpha 18.6** (`origin/main` @ `be661cb`, 3 694 607 o, SHA-256 `e8878d8b…`) —
**conforme au §3 du brief au caractère près**, le rebasage sur la 419 est exact.
Livré en **build 420 / Alpha 18.7**. `SAVE_VERSION` reste **31**.

---

## 1. Livraison en deux temps — le pack d'art manquait au premier envoi

Le premier envoi ne contenait que le `.md` et le `.py` ; le dossier `sprites/` était absent. Cherché
avant de conclure : ni dans les pièces jointes, ni dans les **7 zips du dépôt** — les trois `_32`
n'existaient nulle part, et le seul `ui_carte.png` trouvé était **byte-identique** à celui déjà en jeu,
donc l'ancien art, pas le remplacement.

**Le coût de passer outre a été mesuré, pas supposé** : les six ancres de code appliquées seules, sondées
à 390 px, donnent

| bouton | base 419 | code seul, sans art |
|---|---|---|
| Options | 50 px, icône visible | 127 px, icône 16 — conforme |
| **Aide** | 50 px, icône visible | **34 px, AUCUNE icône** |
| **Carte** | 130 px, icône visible | **34 px, AUCUNE icône** |

Le lot a donc été **suspendu** plutôt que livré dégradé, et **aucun art de substitution n'a été
fabriqué** : le §7 T6 exige la concordance exacte (193 / 732 / 732 / 732 px opaques), qu'un dessin
régénéré ne produirait jamais — et le précédent du build 14.94 montre le vrai danger, où appliquer le
mauvais asset a réécrit le défaut sur lui-même en silence.

Les quatre PNG sont arrivés au second envoi. Contrôlés **avant** application :

| clé | dim | alphas | opaques | couleurs | verdict |
|---|---|---|---|---|---|
| `ui_carte` | 16×16 | 0/255 | 193 (att. 193) | 5 | OK |
| `ui_carte_32` | 32×32 | 0/255 | 732 (att. 732) | 6 | OK |
| `ui_info_32` | 32×32 | 0/255 | 732 (att. 732) | 5 | OK |
| `ui_configurer_32` | 32×32 | 0/255 | 732 (att. 732) | 5 | OK |

Deux contrôles de plus, qui prouvent que c'est le **bon** art et pas une resucée : les trois 32 partagent
un masque **identique au pixel** de 732 (et non 4 × 193 = 772 → disque retracé nativement, pas upscalé),
et le 16 conserve exactement le masque de disque de l'existant tout en changeant **73 pixels de couleur**
— le glyphe est réellement refait.

---

## 2. Application

Patcheur exécuté **verbatim**, aucune ancre retapée.

| ancre | count | |
|---|---|---|
| A1 css compaction | 1 | ok |
| A2 css `ico-lg` | 1 | ok |
| A3 css Lot C | 1 | ok |
| A4 jsx Options | 1 | ok |
| A5 jsx Aide | 1 | ok |
| A6 jsx Carte | 1 | ok |
| S1 `ui_carte` remplacé | — | ok |
| S2 trois pastilles 32 insérées | — | ok |

**SHA-256 avant bump : `9906836ec3f225d660ae5e0521b9d184941be9c2e6eb2247fb5efe1d9757c83a`, +4 747 o —
identique au §3 au caractère près.** Idempotence vérifiée dans les deux sens : rejeu sur base propre →
octet pour octet le même fichier ; rejeu sur le fichier déjà patché → **delta +0**.

### ⚠ Les SHA de bloc du §3 supposent une convention de découpe

Mon extracteur sortait les 7 blocs **exactement 2 octets plus courts** que les chiffres du §3 — un
décalage systématique et uniforme, donc une convention de bord, pas un octet de contenu (piège
documenté au build 14.99). Prouvé plutôt qu'affirmé : en conservant le saut de ligne après `>` et celui
avant `</script>`, **les 7 blocs concordent en taille ET en SHA-256** avec le §3.

SHA-256 **re-extraits après bump** (convention qui garde les sauts de bord) :

| bloc | octets | sha256 |
|---|---|---|
| 1 | 418 | `a50c1c4e7f4a304c650c0cfa7e06c4ffdfb0d87510d83738114d4dd5a1641628` |
| 2 | 4 397 | `8fbb22187703339c146b2f82badd8701d199128cd3d9dd8673d213c8616ca541` |
| 3 | 10 751 | `d949f1c3687aedadcedac85261865f29b17cd273997e7f6b2bfc53b2f9d4c4dd` |
| 4 | 131 835 | `35f4f974f4b2bcd44da73963347f8952e341f83909e4498227d4e26b98f66f0d` |
| 5 | 1 113 969 | `1be53ce44e7be14fb81bd92e6a338cba274304f38c6077061fd3e33232cc2651` |
| 6 | 430 160 | `268c19eb9a15b15e0ede8b61f6e173223c50d7657d3094397150be485b01b6fb` |
| **7** | **1 744 387** | **`3f221ffea0648a503faabef326c5f880ce1d9cde385367885492008ac3c7d96b`** |

**Blocs 1 à 6 identiques à la base 419** (leurs SHA sont ceux du §3, le bump ne les touche pas) ; seul le
bloc 7 bouge. `node --check` **7/7 PASS**.

| | |
|---|---|
| Base 419 | 3 694 607 o |
| Final 420 | 3 701 828 o → **+7 221 o** (dont +4 747 le patch, +2 474 le commentaire de version et `GAME_NOTES`) |
| SHA-256 final | `fab57653aaaf2c3a7194e812d689e743e8aeab584c505e01a29ebb2134b36c76` |

---

## 3. Tests du §7

### T1 — Régimes de largeur — **56/56 PASS**

Les sept lignes du tableau du brief reproduites **exactement**, fr et de :

| largeur | langue | largeurs mesurées | Options | Carte | `.island-tabs` | `.hud` |
|---|---|---|---|---|---|---|
| 520 | fr | 126,8 / 50 / 130,3 | oui | oui | 141 | 108 |
| 479 | fr | 126,8 / 66 / 66 | oui | non | 148,3 | 124 |
| 384 | fr | 126,8 / 66 / 66 | oui | non | 53,3 | 124 |
| 384 | de | 137 / 66 / 66 | oui | non | **43** | 124 |
| 470 × 400 | fr | 126,8 / 50 / 74 | oui | non | 147,3 | 99 |
| 383 | fr | 66 / 66 / 66 | **non** | non | 113 | 124 |
| 340 | de | 66 / 66 / 66 | non | non | 70 | 124 |

Montage falsifiant écarté à chaque cas : `--ui-scale` asserté à **1** avant toute mesure (au-delà des
bornes du Lot B toutes les valeurs seraient multipliées), et `archipel_lang` posée **avant** le
chargement. Le joint de 4 px du build 419 vaut 4 px dans les sept régimes.

### T2 — Le Lot C reprend la main en paysage court — **PASS**

À **470 × 400** (les deux conditions) : icônes **16 px**, les trois `.ico-lg` à `display:none`,
`.map-repair-flag` revenu en `position:static`, bouton Carte à **74 px**, `.hud` à **99 px**.

Les deux montages qui rendraient ce test nul ont été joués **exprès** pour vérifier qu'ils sont bien nuls :
à **740 × 400** le palier 1 ne s'applique pas (largeur > 479, icônes 16) ; à **470 × 600** c'est le Lot C
qui est inactif (pastilles 32 actives, badge en `absolute`).

C'est le seul test qui vérifie la correction de spécificité : les règles du Lot C sont en (0,3,0) pour
battre celles des paliers — à spécificité inférieure le bloc serait ignoré **malgré** sa position en fin
de feuille, l'ordre n'arbitrant qu'à spécificité égale.

### T2bis — Interaction avec le build 419 — **PASS, y compris la partie que le brief n'avait pas pu jouer**

`--hud-h` passe de **157 px** (mode plein) à **173 px** (mode compact), +16 exactement, et le joint reste
à 4 px dans les deux régimes.

Le brief signalait n'avoir **pas pu** mesurer la position réelle des deux boutons flottants (absents du
DOM en partie neuve). Rejoué avec le montage de partie avancée du build 419 — île 7 débloquée et calque
logique accordé via `techTree.grantedBuildings`, **sans confirmer l'arbre** (confirmer tout ouvre un
popup dont le `.research-backdrop` vole les clics). Les deux boutons sont **assertés présents avant toute
mesure**, sinon le test conclurait « pas de chevauchement » alors qu'il n'y aurait rien à chevaucher :

| viewport | échelle | mode | logique | souterrain |
|---|---|---|---|---|
| 390 × 844 | ×1 | **compact** | 10,0 px CSS | 10,0 px CSS |
| 520 × 844 | ×1 | plein | 10,0 px CSS | 10,0 px CSS |
| 1500 × 900 | ×1,5 | plein | 10,0 px CSS (15 écran) | 10,0 px CSS |
| 2500 × 1400 | ×2 | plein | 10,0 px CSS (20 écran) | 10,0 px CSS |

L'ancrage dynamique du 419 absorbe donc les +16 px du mode compact **sans une ligne de code**.

### T3 — Invariants d'immobilité au-dessus de 480 px — **PASS**

Largeurs **126,8 / 50 / 130,3** (fr) et **137 / 50 / 130,3** (de) inchangées, `.hud` à 108 px, `--hud-h`
à 157 px, `.map-repair-flag` en `static`, les trois `.ico-lg` à `display:none`, `.ui-ico` rendues à
16 px, joint à 4 px.

### T4 — Boot et interaction réelle — **PASS**

Boot fr et de : **0 erreur JS, 0 erreur console**. À 390 px, clic **réel** (`page.mouse.click`) sur la
pastille Carte → la carte de l'archipel s'ouvre ; sur le bouton Options → le panneau s'ouvre. Dans les
deux langues.

⚠ **Le brief impose « `useGhostGuard` avale le premier clic → cliquer deux fois ». C'est faux ici** — le
2ᵉ clic REFERME ce que le 1ᵉʳ vient d'ouvrir (clic hors panneau), et l'on mesure alors un panneau fermé
en croyant mesurer un bouton mort. C'est le même piège déjà consigné au build 418. Le harnais clique
**une fois puis asserte l'état atteint**, et boucle si le garde a réellement avalé le clic.

### T5 — Halo du tutoriel — **PASS**

`data-tut="repair"` toujours porté par `.map-btn`, vérifié **au runtime** sur le DOM rendu. Aucune ancre
du patch ne touche cet attribut.

### T6 — Sprites au runtime — **PASS 4/4**

Mesurés **dans le jeu** (décodage réel puis lecture des pixels au canvas), et non par comptage textuel :
`SPRITE_DATA` a deux formes de déclaration et un comptage par regex sur le seul littéral sous-compte
massivement.

`ui_carte` 16×16 / 193 opaques · les trois `_32` 32×32 / 732 opaques · alpha binaire 0-255 sur les quatre.

---

## 4. Non-régressions

| suite | résultat |
|---|---|
| **Lot A** — `verify3.js` (10 viewports) | **10/10 PASS** |
| **Lot B** — `scaleverify.js` (12 viewports, 4 bornes) | **12/12 PASS** |
| **Lot C** — `landv.js` (10 formats) | **8 formats paysage strictement identiques** ; 2 portraits étroits à −16 px (voulu) |
| **Build 418** — cadre d'inventaire | **TOUT PASS** après 3 renversements documentés |
| **Build 419** — joint 4 px | vérifié dans **tous** les régimes, compact et inox compris |
| Erreurs JS / console | **aucune**, tous formats, fr et de |

**Le Lot C fait exactement son office** : les 8 formats paysage ne bougent pas d'un pixel — c'est
précisément ce que son bloc protège en refusant les pastilles. Les deux formats portrait étroits
paient le coût annoncé :

| format | scène base 419 | scène 420 | écart |
|---|---|---|---|
| 360 × 780 portrait | 527 | 511 | **−16 px** |
| 412 × 915 portrait | 662 | 646 | **−16 px** |

**Le Lot B ne demande aucune recalibration** (invariant §8.5) : ses quatre bornes (1399/1400,
2399/2400) passent, et pour cause — elles sont toutes à ≥ 1400 px de large, où les règles de compaction
(< 480 px) ne s'appliquent jamais.

### Trois renversements d'assertion de la suite du build 418, voulus et documentés

Tous les trois valent exactement **±16 px**, à 390 × 844, c'est-à-dire en mode compact :

| assertion | avant | après |
|---|---|---|
| scène repliée | 542 | **526** |
| `.hud` portrait | 108 | **124** |
| scène inventaire déployé | 591 | **575** |

Tout le reste de la suite passe sans retouche (bordure 6 px, hauteurs 45 / 139 / 133, paysage court à
3 px, thème inox). La suite mise à jour **reste falsifiable** : rejouée sur la base 419, elle rend
exactement les 3 KO inverses.

---

## 5. Deux pièges de harnais, coûteux, à ne pas redécouvrir

Ma première passe des tests T2/T3 a rendu **9 KO**. Aucun n'était un défaut ; les deux causes valent
d'être consignées.

**(a) Le bouton Carte contient TROIS `img.ui-ico`, pas deux.** La troisième est le badge 🛠, qui ne porte
ni `ico-sm` ni `ico-lg`. Une sonde qui liste `img.ui-ico` lit donc « 16,16 » là où l'icône du bouton vaut
bien 16, et cinq assertions tombent à tort. Filtrer sur `img.ico-sm, img.ico-lg` — les deux seules
images que le lot pilote.

**(b) `--hud-h` ne vaut la hauteur du chrome qu'inventaire REPLIÉ.** L'inventaire démarre **déployé**
(build 364) et il est alors `position:absolute` : le `.hud-stack` ne mesure plus que le `.hud`, et
`--hud-h` sort à 108/124 au lieu de 157/173. Les chiffres du brief sont ceux de l'état **replié** — et
c'est le bon état, puisque les deux boutons flottants n'existent que là (`!invOpen`). Le harnais doit
replier par un **vrai clic** avant de lire.

---

## 6. Contrôle visuel

Captures avant/après du `.hud-stack` à 340 / 384 (de) / 390 / 520 px.

À **340 px**, la base laissait deux icônes 16 flotter dans des boutons largement vides pendant que
« Carte » monopolisait la largeur, et le sélecteur d'île tombait à **une seule île**. Le patch rend trois
pastilles uniformes et **deux îles** visibles. L'inversion se justifie d'elle-même à l'image : le libellé
qu'on conservait était le moins utile des trois.

À **390 px** (palier 1), Options garde son libellé avec son icône 16 tandis qu'Aide et Carte passent en
pastilles 32. L'asymétrie est voulue — un bouton titré n'a pas besoin d'une grande icône, un bouton
réduit à son icône si.

Au-dessus de 480 px, rien ne change ; le nouveau glyphe 16 de `ui_carte` s'accorde en teinte au 32.

---

## 7. Écarts au brief

**Aucun écart de code** : le patcheur a été appliqué verbatim et le SHA d'avant bump concorde au
caractère près.

Deux écarts de **rapport**, tous deux instruits ci-dessus : la convention de découpe des blocs (§2), et
la consigne « cliquer deux fois » du T4, qui est fausse pour ces boutons (§3, T4).

---

## 8. Points laissés en suspens

- **Mode de défaillance silencieux à connaître** : `uiIcon` retombe sur son repli quand une clé de sprite
  manque, et ce repli est ici la **chaîne vide** — le `.ico-lg` n'est même pas créé dans le DOM pendant
  que le CSS masque le 16. Un seul des trois `_32` absent donnerait un bouton **vide**, sans la moindre
  erreur : ni `node --check` ni un boot ne le verraient. C'est ce que la mesure du §1 a montré, et c'est
  la raison d'être du T6.
- §11 du brief, inchangés : le glyphe 16 n'a que **deux volets** là où le 32 en a trois (sept variantes à
  trois volets essayées, illisibles sur les ~11 × 11 px utiles — écart de dessin assumé) ; le placement du
  🛠 en bas-droite reste dans les bornes du bouton.
- Reliquats du chantier dimensionnement : plancher tactile à **26 px** (sous les ~44 px recommandés),
  `env(safe-area-inset-*)` jamais validé sur appareil à encoche, tablettes et laptops courts toujours à ×1.
- **`--cadre-bouton` reste orphelin** (0 usage), signalé depuis le build 418.
- Le **calque d'inventaire déployé n'a toujours aucun plafond de hauteur**.
- `index.html` / `version.json` / `sw.js` : régénérés par la CI au merge.

---

## 9. Livraison

| | |
|---|---|
| Branche | `claude/new-session-8itu4m` |
| Base | `origin/main` @ `be661cb` (build 419) |
| Version livrée | **build 420 / Alpha 18.7**, `SAVE_VERSION` 31 |
| Contrôle anti-collision | max distant relevé sur **toutes** les branches avant push |
| Merge | **non effectué** — il appartient à Ethan |
