# RAPPORT — Pastilles compactes + Options en pastille

Retour d'Ethan sur capture d'un téléphone réel, sans brief : « réduis la taille des bords, ça fait
trop moche, le sprite unique il faut qu'il soit plus comprimé […] et pareil pour l'option, du coup tu
prends le gros sprite pour les options et tu réduis le bouton, là c'est vraiment grossier ».

Base : `origin/main` @ **build 420 / Alpha 18.7**. Livré en **build 421 / Alpha 18.8**.
`SAVE_VERSION` reste **31**. Nom de rapport vérifié libre avant écriture.

---

## 1. Diagnostic — deux défauts, dont un que la capture ne nomme pas

Mesuré sur le 420, pas estimé :

| élément | géométrie | détail |
|---|---|---|
| pastille (Aide, Carte) | **66 × 54 px** pour un sprite de 32 | padding 5/11 + bordure 6 → **34 px de chrome horizontal** |
| bouton Options (titré) | **126,8 × 38 px** | |

Le premier défaut est celui qu'Ethan décrit : une pastille consacre plus de la moitié de sa largeur à
son cadre.

**Le second n'est pas nommé dans le retour mais c'est probablement lui qui fait le plus « grossier » :
le bouton Options ne fait que 38 px de haut à côté de pastilles à 54.** La rangée n'est pas alignée —
un bouton bas et large collé à deux carrés hauts. C'est aussi ce qui explique les +16 px de HUD du
build 420 : la rangée prend la hauteur du plus haut.

La demande d'Ethan (« le gros sprite pour Options aussi ») règle les deux d'un coup : trois pastilles
identiques, alignées, et compactes.

---

## 2. Correctif

Les **trois** boutons passent en pastille sous 480 px, à **48 × 48** (bordure 4, padding 4).

```
.hud .hud-main .options-btn{border-width:4px;border-image-width:4px;padding:4px;}
```

⚠ **Spécificité — la règle DOIT être en (0,3,0).** La règle de thème est
`body:not(.theme-inox) .options-btn` = **(0,2,1)**, et elle est **plus bas dans la feuille** : ni
(0,2,0) ni une égalité ne suffiraient. D'où `.hud .hud-main`, aligné sur les sélecteurs des paliers.

⚠ **On ne touche QUE `border-width` et `border-image-width`, jamais la shorthand `border-image`.** La
redéclarer imposerait de répéter la source pour **chaque thème ET chaque état** (normal / survol /
actif). La largeur de rendu du slice suffit ; la source et le slice restent ceux du thème.

**Le palier 2 à 383 px est supprimé** : il n'existait que pour garder « Options » écrit à 384 px, un
calibrage que le retour d'Ethan annule. 383 et 384 sont désormais identiques.

⚠ **Le bloc Lot C (paysage court) rend le cadre plein ET le libellé.** Il refuse déjà les pastilles,
donc le bouton doit y reprendre sa géométrie normale — sans ces deux lignes, un téléphone en paysage
sous 480 px aurait des boutons de **32 px**, sous le plancher tactile, et perdrait le mot « Options »
là où la largeur est abondante. Même spécificité (0,3,0) que la règle de compaction, et ce bloc est
plus bas dans la feuille : c'est l'ordre qui arbitre, comme prévu.

### ⚠ Un piège payé, attrapé par la mesure

Supprimer le palier 2 retire aussi la bascule 16 → 32 d'**Options**, qui n'était déclarée **que là**
(le palier 1 ne visait que Carte et Aide). Première passe : Options sortait à **32 × 32** — libellé
masqué mais icône restée en 16. Rien ne l'aurait signalé à l'œil sur une capture réduite ; c'est la
mesure de la largeur du bouton qui l'a montré.

---

## 3. Résultat mesuré

| | base 420 | patch 421 |
|---|---|---|
| pastille | 66 × 54 | **48 × 48** |
| Options à 384 px | 126,8 × 38 (titré) | **48 × 48** (pastille) |
| `.island-tabs` à 384 px | 53,3 | **166** (×3,1) |
| `.island-tabs` à 360 px | 90 | **124** |
| `.hud` compact | 124 | **118** |
| `--hud-h` compact | 173 | **167** |

À 360 px, cinq îles sont visibles là où la base en montrait deux.

### Trois variantes comparées à la capture

| variante | bordure / padding | taille | verdict |
|---|---|---|---|
| **A — retenue** | 4 / 4 | **48 × 48** | le sprite garde une marge dans son cadre, le biseau garde sa profondeur |
| B | 4 / 2 | 44 × 44 | **écartée** : sprite collé au cadre, lit « bon marché » |
| C | 3 / 3 | 44 × 44 | **écartée** : idem, et le biseau aminci aplatit le bouton |

---

## 4. Coût et non-régressions

**Ce lot est CSS pur** : avant bump, **aucun** des 7 blocs `<script>` ne change (le CSS vit dans
`<style>`). Après bump, seul le bloc 7 bouge — il porte `GAME_BUILD` / `GAME_VERSION` / `GAME_NOTES`.
`node --check` **7/7 PASS**.

| | |
|---|---|
| Base 420 | 3 701 828 o |
| Final 421 | 3 705 262 o → **+3 434 o** (dont **+1 475 le CSS**, le reste = commentaire de version et notes) |
| SHA-256 final | `4f54053eb62c98e306fe77d72ccff210af10c622af30d72734658268e6b37465` |
| Bloc 7 | 1 746 346 o · `e593950260f0eed349cef03f22d947fd626925cf959ab13364f11ca8a79eb155` |

| suite | résultat |
|---|---|
| **T1** régimes de largeur (recalibré, fr + de) | **64/64 PASS** — 18 KO sur la base 420, donc falsifiable |
| **T2 / T2bis / T3 / T5 / T6** | **40/40 PASS** |
| **T4** + position des boutons flottants | **20/20 PASS** |
| **Lot A** `verify3.js` | **10/10 PASS** |
| **Lot B** `scaleverify.js` | **12/12 PASS** |
| **Lot C** `landv.js` | **8 formats paysage strictement identiques** ; 2 portraits **+6 px de scène** |
| **Build 418** cadre d'inventaire | **TOUT PASS** après 3 renversements documentés |
| Erreurs JS / console | **aucune**, tous formats, fr et de |

Les boutons flottants du build 419 restent à **10 px CSS sous le haut de la scène** à ×1 / ×1,5 / ×2,
en mode plein comme en compact : l'ancrage dynamique absorbe le HUD qui rétrécit sans une ligne de code,
comme il avait absorbé son agrandissement au 420.

Le **paysage court est intact** : `.hud` à 99 px, bouton Carte à 74 px, icônes 16, bordure 6, padding
5/11 et **libellé Options rendu** — les deux lignes de restitution font leur travail.

### Trois renversements d'assertion de la suite du build 418, voulus

Tous du même **+6 px**, à 390 × 844 (mode compact) : scène repliée 526 → **532**, `.hud` 124 → **118**,
scène déployée 575 → **581**. La scène **regagne** ce que le 420 lui avait pris, pastilles comprises.
La suite reste **falsifiable** : rejouée sur la base 420, elle rend exactement les 3 KO inverses.

---

## 5. Décisions prises seul (à contredire d'un mot)

1. **48 px et pas 44.** Les deux variantes à 44 ont été rendues et comparées : le sprite y touche
   presque le cadre. La demande était « plus comprimé », pas « au ras ».
2. **Palier 2 supprimé plutôt que déplacé.** Sa seule raison d'être était de garder « Options » écrit à
   384 px ; Ethan demande explicitement l'inverse.
3. **Le bloc Lot C restitue le cadre plein ET le libellé** plutôt que de laisser les pastilles
   compactes s'appliquer en paysage court, où elles donneraient des boutons de 32 px.
4. **`.research-btn` (PORT / RECHERCHE) n'est pas touché** — la demande porte sur la rangée du haut.

---

## 6. Points laissés en suspens

- Le plancher tactile passe de 66 à **48 px** sur les trois pastilles : toujours au-dessus des 44 px
  usuels, mais c'est un resserrement — à dire si ça devient inconfortable au doigt.
- `uiIcon` retombe sur la **chaîne vide** quand une clé de sprite manque (mode de défaillance
  silencieux documenté au build 420) : inchangé, couvert par le T6.
- Reliquats connus : `env(safe-area-inset-*)` non validé sur appareil à encoche, `--cadre-bouton`
  toujours orphelin, calque d'inventaire déployé sans plafond de hauteur.
- `index.html` / `version.json` / `sw.js` : régénérés par la CI au merge.

---

## 7. Livraison

| | |
|---|---|
| Branche | `claude/new-session-8itu4m` |
| Base | `origin/main` (build 420) |
| Version livrée | **build 421 / Alpha 18.8**, `SAVE_VERSION` 31 |
| Contrôle anti-collision | max distant relevé avant push |
| Merge | **non effectué** — il appartient à Ethan |
