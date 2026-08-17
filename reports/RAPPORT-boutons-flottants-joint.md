# RAPPORT — Boutons flottants ancrés + joint HUD / barre d'inventaire

Demande d'Ethan sur capture d'écran, sans brief : « descendre les boutons souterrain et bouton
logique » et « rajouter une belle séparation entre la fenêtre inventaire et la fenêtre tout en haut,
parce que ça fait un peu bizarre — ou juste un truc très fin ».

Base : `origin/main` @ `e62b0ee` → **build 418 / Alpha 18.5**. Livré en **build 419 / Alpha 18.6**.
`SAVE_VERSION` reste **31**. Nom de rapport vérifié libre avant écriture.

---

## 1. Diagnostic — mesuré, pas supposé

### (1) Les boutons ne « débordaient » pas : ils **chevauchaient**

`.logiclayer-btn` et `.underground-btn` étaient en `position:fixed; top:150px` — une **constante**
qui ne suit pas la hauteur réelle du chrome. Mesures sur la base 418, inventaire **replié** (l'état
de la capture) :

| viewport | échelle | chrome (hud-stack) | `top` des boutons | écart |
|---|---|---|---|---|
| 390×844 portrait | ×1 | 153 px | 150 px | **−3 px → CHEVAUCHEMENT** |
| 360×780 portrait | ×1 | 153 px | 150 px | **−3 px → CHEVAUCHEMENT** |
| 740×400 paysage | ×1 | 138 px | 150 px | +12 px |
| 1920×1080 | ×1,5 | 167 px | 225 px | +58 px |
| 2560×1440 | ×2 | 222 px | 300 px | +78 px |

**Cause racine : c'est mon build 418 qui a déclenché le symptôme.** Le cadre 9-slice donné à la barre
d'inventaire l'a fait passer de 35 à 45 px → le chrome de 143 à 153 px, transformant l'ancien écart
de +7 px en **−3 px**. Le `top:150px` avait d'ailleurs **déjà** dû être retouché une fois (92 → 150
au build 13.90) : une constante figée face à un chrome variable est un défaut récurrent, pas un
accident.

Deux conséquences supplémentaires que la capture ne montre pas :
- **`top` est multiplié par `zoom:var(--ui-scale)`** (Lot B) → 150 × 1,5 = 225, × 2 = 300. Les
  boutons **dérivent loin du chrome** sur grand écran (+58, +78 px) au lieu de le suivre.
- À 150 px, les boutons passent **par-dessus le bandeau tutoriel** (qui commence à 153 px en
  portrait) sur tous les formats.

### (2) Les deux cadres 9-slice se **touchaient**

Depuis le build 418, `.hud` (cadre à rivets) et `.inventory` (cadre métal) sont deux blocs encadrés
**directement accolés** : bordure contre bordure, coins jaunes contre coins jaunes. Capture à l'appui,
ça se lit comme une **double bordure accidentelle** — exactement le « ça fait bizarre » signalé.

---

## 2. Correctifs

### (1) Les boutons s'ancrent **sous le chrome**, au lieu d'un `top` figé

```
:root{--float-top:calc(var(--hud-h, 143px) + var(--tuto-h, 0px) + 10px);}
.underground-btn{... top:var(--float-top); ...}
.logiclayer-btn  {... top:var(--float-top); ...}
```

`--hud-h` est **publiée par `Hud`** (hauteur mesurée du `.hud-stack`, `useLayoutEffect` +
`ResizeObserver`), sur le **modèle exact de `--tuto-h`** que `TutorialBanner` publie depuis le
build 13.85 — le patron existait déjà dans le fichier, je l'ai suivi plutôt que d'en inventer un.

⚠ **`offsetHeight` et non `getBoundingClientRect()`** : mesuré, à ×1,5 le premier rend **111** et le
second **167**. `offsetHeight` donne la hauteur **non zoomée**, qui est précisément l'unité qu'attend
un `top` posé sur un bouton lui-même zoomé. Prendre le rect aurait doublement appliqué le zoom.

Les deux replis (`--hud-h, 143px` / `--tuto-h, 0px`) reproduisent l'ancienne géométrie si le JS ne
publiait rien : aucun écran ne casse.

**Résultat mesuré — les boutons sont à 10 px CSS sous le haut de la scène partout :**

| viewport | échelle | haut de scène | `top` des boutons | écart |
|---|---|---|---|---|
| 390×844 | ×1 | 223 | 233 | **10 px** |
| 360×780 | ×1 | 223 | 233 | **10 px** |
| 740×400 paysage | ×1 | 169 | 179 | **10 px** |
| 1920×1080 | ×1,5 | 224 | 239 | 15 écran = **10 CSS** |
| 2560×1440 | ×2 | 300 | 320 | 20 écran = **10 CSS** |

Fini le chevauchement, fini la dérive au zoom, et les boutons ne passent plus sur le bandeau tuto.

### (2) Un **joint** entre le HUD et la barre d'inventaire

`.inventory{margin-top:4px}` **et** le fond du parent `.hud-stack` peint d'un gris translucide
(`rgba(126,138,162,.26)`). La gouttière laissant voir le fond du parent, elle devient un **joint clair
et délibéré** au lieu d'un trou noir — « un truc très fin », comme demandé.

**Quatre variantes essayées et comparées à la capture** (le choix est visuel, il ne se déduit pas) :

| variante | rendu | verdict |
|---|---|---|
| A — écart 4 px seul | gouttière noire | correct mais lit comme un simple trou |
| B — écart 5 px + liseré interne | ligne claire **dans** la barre | **écartée** : refait une double bordure, le défaut qu'on corrige |
| C — écart 8 px | large bande sombre | **écartée** : lit comme de l'espace mort, et coûte 8 px |
| **D — écart 4 px + gouttière peinte** | **joint clair fin entre deux cadres** | **retenue** |

Vérifiée aux quatre états : portrait replié, portrait déployé, **thème inox** (le joint tient sur la
tôle larmée) et **paysage court** (où la bordure est à 3 px, le joint reste proportionné).

---

## 3. Coût et non-régressions

**⚠ Ce lot n'est PAS CSS pur** (contrairement au build 418) : le bloc `<script>` qui porte `Hud`
change, puisqu'il publie `--hud-h`. Vérifié : **blocs 1 à 6 identiques à la base**, seul le **bloc 7**
bouge, `node --check` **7/7 PASS**.

| | |
|---|---|
| Base 418 | 3 690 477 o |
| Final 419 | 3 694 607 o → **+4 130 o** |
| SHA-256 final | `e8878d8b0cb0fec9d0ef6fc4a27ce65d162cb8a58f708cb97e7bd1956e4ed807` |
| Bloc 7 | 1 739 863 o · `f20a0784abcce3b77dee0b8d2a1d6f6d5fc3a507d3c82533dcb81a8003d40e3d` |

**Coût en scène : +4 px, barre repliée uniquement** (390×844 : 546 → **542 px**). **Aucun coût barre
déployée** — le calque est en superposition, il ne pousse rien (scène toujours 591 px).

### Suites rejouées

| suite | résultat |
|---|---|
| **Lot A** — `verify3.js` état neuf | **10/10 PASS** |
| **Lot A** — `verify3.js` 12 îles | **10/10 PASS** |
| **Lot B** — `scaleverify.js` (12 viewports, 4 bornes) | **12/12 PASS** |
| **Lot C** — `landv.js` (10 formats) | **strictement identique**, ligne par ligne |
| **Build 418** — suite du cadre d'inventaire | **TOUT PASS** après un renversement (ci-dessous) |
| Erreurs JS / console | **aucune**, sur tous les formats |

`landv.js` est identique parce qu'il mesure inventaire **ouvert** : le joint ne coûte alors rien.

**Un renversement d'assertion, voulu et documenté** : la suite du build 418 assertait « scène repliée
= 546 ». Elle vaut désormais **542** — exactement les 4 px du joint, rien d'autre ne bouge (bordure
6 px, hauteurs 45/139/133, thème inox, invariants HUD tous inchangés). L'assertion a été mise à jour
à 542 ; la suite reste **falsifiable** (rejouée sur le build 418, elle signale bien l'écart inverse).

---

## 4. Décisions prises seul (à contredire d'un mot)

1. **Ancrage dynamique plutôt qu'un nouveau `top` figé.** Un simple `top:165px` aurait réglé la
   capture d'Ethan et rien d'autre : le défaut serait revenu au prochain changement de chrome (il
   était déjà revenu deux fois) et la dérive au zoom (+58/+78 px) serait restée. Le coût est d'avoir
   touché le JS.
2. **Joint à 4 px et pas davantage.** 6 et 8 px ont été essayés puis écartés à la capture.
3. **Gouttière peinte plutôt que trou noir** : c'est ce qui fait la différence entre « il manque
   quelque chose » et « c'est un joint ».
4. **Aucun plafond ajouté au calque d'inventaire déployé** — reliquat connu, hors sujet ici.

---

## 5. Points en suspens

- Le **calque d'inventaire déployé n'a toujours aucun plafond de hauteur** (`vh`/`dvh` inutilisables
  dans le harnais en paysage émulé). Inchangé.
- **`--cadre-bouton` reste orphelin** (0 usage), comme signalé au build 418.
- **`env(safe-area-inset-*)`** toujours non testable en headless.
- Les boutons restent **masqués quand l'inventaire est ouvert** (`!invOpen`, comportement d'origine) :
  l'ancrage ne s'applique donc qu'à l'état replié, le seul où ils sont visibles.
- `index.html` / `version.json` / `sw.js` : régénérés par la CI au merge.

---

## 6. Livraison

| | |
|---|---|
| Branche | `claude/new-session-8itu4m` |
| Base | `origin/main` @ `e62b0ee` (build 418) |
| Merge | **non effectué** — Ethan relit d'abord, comme demandé |
