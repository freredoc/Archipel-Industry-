# RAPPORT — L'île 6 n'a plus d'onglet tant qu'elle est verrouillée

Retour d'Ethan sur capture : « on voit l'île 6 dans le slide, alors qu'elle doit apparaître seulement
lorsque l'on fait la réparation du port île 5 ».

Base : `origin/main` @ **build 421 / Alpha 18.8**. Livré en **build 422 / Alpha 18.9**.
`SAVE_VERSION` reste **31**. Nom de rapport vérifié libre avant écriture.

---

## 1. La règle existait déjà — elle n'était appliquée qu'à moitié

`ARCHI_CACHEE = { 6: true }` est dans le fichier depuis le build 14.42, avec sa justification écrite
juste au-dessus :

> « Une silhouette grisée annoncerait son existence — alors que les îles 2..5 sont grisées justement
> pour que le joueur voie où il va. »

Mais elle n'était consultée que par **`ArchipelMap`** (la carte de l'archipel du panneau Port). Le
sélecteur d'île, lui, ne filtrait que l'île 7 :

```js
const ids = ISLAND_TERRAINS.map(d => d.id).filter(id => id !== 7 || (unlocked && unlocked[7]) || dev);
```

Les deux surfaces se contredisaient donc : la carte cachait l'île 6, les onglets la montraient dès la
partie neuve.

## 2. ⚠ Défaut PRÉEXISTANT, pas une régression du build 18.8

Mesuré des deux côtés, partie neuve, 384 px :

| | onglets dans le DOM | bande | contenu | îles visibles |
|---|---|---|---|---|
| **base 420** | **6** | 53,3 px | 166 px | 1 · 2 — les îles 3 à 6 **hors champ** |
| **421** | **6** | 166 px | 166 px | 1 · 2 · 3 · 4 · 5 · **6** |

**L'onglet de l'île 6 a toujours été dans le DOM.** Sur le 420 il était simplement hors champ dans une
bande trop étroite — et **atteignable en faisant défiler**. Le lot 18.8, qui a rendu sa largeur au
sélecteur en compactant les boutons, l'a **révélé sans le créer**.

## 3. Correctif

```js
const ids = ISLAND_TERRAINS.map(d => d.id).filter(id =>
  (id !== 7 || (unlocked && unlocked[7]) || dev)
  && (!ARCHI_CACHEE[id] || (unlocked && unlocked[id]) || dev));
```

Une seule ancre, `count == 1`. La règle du sélecteur devient celle de la carte.

⚠ **Aucun verrouillage possible** : dès que le nœud 28 pose `islandUnlocked[6]`, l'onglet reparaît
(testé). Et l'île 6 s'ouvre en touchant sa cible **sur la carte**, jamais par cet onglet — le chemin
d'accès ne dépend pas de lui.

⚠ **`ARCHI_CACHEE` est déclarée plus bas dans le même bloc** que `IslandSelector` : pas de zone morte
temporelle, la lecture ayant lieu au **rendu** et non à l'évaluation du module. Vérifié au boot,
0 erreur.

---

## 4. Tests

| test | résultat |
|---|---|
| partie neuve → **5 onglets**, aucun hors champ | **PASS** |
| île 6 débloquée → **6 onglets** (pas de verrouillage) | **PASS** |
| îles 2 à 5 **toujours grisées et visibles** | **PASS** |
| 0 erreur JS / console (pas de TDZ) | **PASS** |
| **édition DEV**, vrai clic sur le toggle « Mode développeur » → **7 onglets** | **PASS** |
| **falsifiabilité** : la même suite sur le 421 non corrigé | **2 KO** |

⚠ **Le contournement `dev` n'est pas testable en édition publique** : `dev` arrive par une **prop
React**, pas par le champ `g.ui.dev` — l'écrire ne met pas le state à jour (piège 14.83) —, et
`toggleDev` refuse quand `DEV_BUILD` est faux. Le test est donc joué en **édition dev** (`sed` sur
`DEV_BUILD`), par un vrai clic. Deux pièges payés au passage : le toggle est en **bas d'une liste
défilante** (piège 14.55 — `scrollIntoView` obligatoire) et une recherche **par texte** tombe sur un
autre bouton — le sélecteur sûr est **`.opt-toggle.dev`**.

## 5. Non-régressions

| suite | résultat |
|---|---|
| **T1** régimes de largeur (recalibré) | **64/64 PASS** — 6 KO sur le 421, donc falsifiable |
| **T2 / T2bis / T3 / T5 / T6** | **40/40 PASS** |
| **T4** + position des boutons flottants | **20/20 PASS** |
| **Lot A** `verify3.js` | **10/10 PASS** |
| **Lot B** `scaleverify.js` | **12/12 PASS** |
| **Build 418** cadre d'inventaire | **TOUT PASS** |
| Erreurs JS / console | **aucune** |

**T1 recalibré** : `.island-tabs` perd un onglet (26 px + 2 de gap) partout où la bande était
dimensionnée par son **contenu** — 166 → **138**, 147,3 → 138, 141 → 138. À 340 px et à 520 px en
allemand elle est bornée par le viewport ou déjà plus étroite : **inchangée**.

### ⚠ Effet de bord mesuré, favorable, mais dépendant de la langue

Retirer un onglet fait parfois basculer le HUD de **deux rangées à une** sur tablette :

| viewport | langue | 421 | corrigé, île 6 verrouillée | corrigé, île 6 ouverte |
|---|---|---|---|---|
| 1024 × 768 | en | 108 | **66** (+42 px de scène) | 108 |
| 1024 × 768 | fr | 108 | 108 | 108 |
| 1024 × 600 | en | 108 | **66** (+42) | 108 |
| 768 × 1024 | fr | 130 | **88** (+42) | 130 |

Le HUD remonte quand l'île 6 s'ouvre. **Ce n'est pas un mécanisme neuf** : le HUD se replie déjà selon
la largeur de son contenu, et l'île 7 ajoute un onglet exactement de la même façon. Sur la plus grande
partie d'une partie, la tablette y gagne 42 px de scène ; au déblocage de l'île 6, le HUD reprend sa
seconde rangée — un pas unique, à un moment où le joueur voit de toute façon son archipel changer.
**Signalé, pas corrigé.**

⚠ Le harnais `landv.js` monte `isMobile` sous 1100 px et **ne force pas la langue** (il tourne donc en
anglais) : c'est ce qui explique qu'il ait signalé ces deux lignes alors qu'un relevé en français ne
montrait rien. Deux mesures contradictoires, une seule cause — le montage.

## 6. Coût

| | |
|---|---|
| Base 421 | 3 705 262 o |
| Final 422 | 3 708 031 o → **+2 769 o** (dont ~1 000 de code, le reste = commentaire de version et notes) |
| SHA-256 | `a873437f025f81ae6c11bbdaed4d66318d4d8c1927d6a93c9bbae965ca2c9dfd` |
| Blocs | seul le **bloc 7** change, `node --check` **7/7 PASS** |

## 7. Points laissés en suspens

- Le **basculement de rangée du HUD** sur tablette (ci-dessus) : signalé, non traité.
- **`ARCHI_CACHEE` reste consultée à deux endroits** (carte et onglets) plutôt que derrière un helper
  unique — c'est ce qui a permis la divergence. Un `ileVisible(game, id)` partagé fermerait la classe
  entière de défaut ; ce serait un lot de refonte, pas ce correctif.
- Reliquats connus inchangés : plancher tactile à 48 px, `env(safe-area-inset-*)` non validé sur
  encoche, `--cadre-bouton` orphelin, calque d'inventaire déployé sans plafond.
- `index.html` / `version.json` / `sw.js` : régénérés par la CI au merge.

## 8. Livraison

| | |
|---|---|
| Branche | `claude/new-session-8itu4m` |
| Base | `origin/main` (build 421) |
| Version livrée | **build 422 / Alpha 18.9**, `SAVE_VERSION` 31 |
| Merge | **non effectué** — il appartient à Ethan |
