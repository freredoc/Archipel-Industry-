# RAPPORT — Correctif : la carte de l'archipel était écrasée à 2 px

**Livré : `GAME_BUILD = 401`, `GAME_VERSION = 'Alpha 16.8'`, `SAVE_VERSION = 31` (INCHANGÉ).**

⚠ **Renuméroté de 400 / 16.7 vers 401 / 16.8 après coup** : pendant que cette PR attendait, une
autre session a mergé le **lot C** (PR #383) qui avait pris le **même** numéro 400 / 16.7, déjà
publié par la CI. Deux livraisons sous un même `GAME_BUILD` empêchent la notification de mise à
jour de partir (incident déjà survenu aux builds 298/299). `main` a donc été fusionné dans cette
branche, le conflit résolu — **les deux blocs de commentaire cumulatif sont conservés**, celui du
lot C d'abord — et la suite complète rejouée sur le fichier fusionné.

Base d'écriture : build **399 / Alpha 16.6** (`44a9b75f…`) ; base de livraison après fusion :
**400 / Alpha 16.7** (`main`, 3 473 237 o). Le correctif lui-même pèse **+441 o** ; le reste du
delta est le commentaire cumulatif et `GAME_NOTES`. Fichier final
`543a45fd17d867a9fe07462eefc0f6d169b63ee83eaa5e81a7515264de305690`, **3 475 670 o**.

---

## 1. Le symptôme

Signalé par le joueur, capture à l'appui : le panneau **Carte de l'archipel** n'affiche que
« Flux entre îles » et sa liste. **La carte a disparu.**

## 2. La cause, mesurée sur sa sauvegarde

`.rp-list` — le conteneur défilant du panneau — est en `display:flex; flex-direction:column`.
`.arch-map` y entrait avec le **`flex-shrink:1` par défaut**. Or **tous** les enfants de la carte
(vignettes, liaisons, brume) sont en `position:absolute` : sa hauteur **min-content vaut ZÉRO**. Le
flex l'écrase donc intégralement avant de laisser le conteneur défiler.

Relevé sur sa partie (7 îles, 10 cartes de flux, 430 px de large) :

```
scrollHeight 2005 px   pour   626 px disponibles
.arch-map  ->  368 × 2 px          (au lieu de 368 × 486)
flex-shrink : 1
```

⚠ **`aspect-ratio` ne protège pas** : il fixe une taille *préférée*, que `flex-shrink` a le droit de
réduire. C'est le piège exact de ce défaut.

**Pourquoi ça ne se voyait qu'en fin de partie** : sur une partie neuve la liste de flux est vide ou
courte, il n'y a aucune pression sur le flex, la carte s'affiche à 486 px. Le défaut n'apparaît qu'une
fois les liaisons actives assez nombreuses.

## 3. Le correctif

Un mot : **`flex-shrink:0`** sur `.arch-map`, avec le commentaire qui explique pourquoi il est
obligatoire. **Aucun JS touché**, aucune autre règle CSS modifiée. Accolades du bloc `<style>`
re-comptées : **932 / 932, équilibrées**.

Re-mesuré sur **la même sauvegarde** : **368 × 486 px**, les **6 vignettes visibles**, et le panneau
défile normalement (`scrollHeight` 2 005 > 626 — c'est bien le conteneur qui défile, plus la carte qui
se sacrifie).

---

## 4. Pourquoi mes 71 assertions du lot 4b ne l'ont pas vu

**C'est le point le plus important de ce rapport.** Toutes mes assertions testaient la **présence** de
`.arch-map` dans le DOM — jamais sa **hauteur**. Or un conteneur écrasé à 2 px :

- est toujours présent (`document.querySelector('.arch-map')` le trouve) ;
- garde tous ses nœuds absolus dans le DOM ;
- répond encore correctement à `elementFromPoint` et aux clics de mes tests.

**Faux vert complet.** Et comme mes montages partaient de parties neuves, la liste de flux était
toujours courte : la pression sur le flex n'existait pas au banc.

**Règle à retenir** : tout bloc à `aspect-ratio` placé **dans un flex** doit être asserté par
`getBoundingClientRect`, jamais par sa seule existence.

## 5. Validation

**Nouveau test T8**, en trois volets, sur la **sauvegarde réelle du joueur** :

| # | verdict | mesure |
|---|---|---|
| **T8a** | **PASS** | Partie neuve : carte présente **et haute de 486 px** |
| **T8b** | **PASS** | Sa sauvegarde : 7 îles chargées, la liste **déborde** (`scrollHeight` 2 005 > 626), `flex-shrink` = **0**, **carte à 486 px**, **6 vignettes visibles** |
| **T8c** | **PASS** | **Contre-épreuve** : `flex-shrink:1` réinjecté en CSS → la carte **retombe à 2 px**. Sans elle, T8b ne prouverait rien |

**Non-régression** : les suites du lot 4b rejouées entières **sur le fichier fusionné avec le lot
C** — **T1+T2 tout passe**, **T3-T7 tout passe**, **i18n 0 libellé non traduit** (4 langues).
`node --check` **7/7**, accolades CSS **932 / 932**. **0 `pageerror`**, console propre hors le 404
préexistant du serveur de test. Cohabitation des deux lots vérifiée : le `flex-shrink:0` est là,
et le lot C aussi (**11 occurrences de `portSeen`**).

### SHA-256 des 7 blocs, re-extraits APRÈS la fusion et le bump

| bloc | SHA-256 (16 car.) | taille |
|---|---|---|
| blk1 | `a50c1c4e7f4a304c` | 418 |
| blk2 | `8fbb22187703339c` | 4 397 |
| blk3 | `d949f1c3687aedad` | 10 751 |
| blk4 | `35f4f974f4b2bcd4` | 131 835 |
| blk5 | `1be53ce44e7be14f` | 1 113 969 |
| blk6 | `e448f8280258b643` | 246 101 |
| blk7 | `22c46e137ea22539` | 1 709 693 |

(Les blocs 6 et 7 portent aussi le lot C, arrivé par la fusion.)

`GAME_NOTES` : **370 caractères** extraits par la regex de la CI, accents littéraux, **0 séquence
`\u`**, aucun guillemet droit.

---

## 6. Points en suspens

- **Le défaut date du lot 2** (build 396), quand la carte a quitté l'onglet « Transit archipel » du
  Port pour son propre panneau — c'est là qu'elle est entrée dans `.rp-list`. Il est resté invisible
  quatre builds parce qu'aucun banc ne partait d'une partie avancée.
- **Aucun autre bloc à `aspect-ratio` dans un flex** n'a été trouvé dans la feuille de style : la
  règle ne concerne aujourd'hui que `.arch-map`. À re-vérifier si un futur lot en ajoute un.
- **Piège de banc consigné** : rejouer la sauvegarde d'un joueur exige de la réinjecter dans un
  `addInitScript` (les trois clés de slot), sinon le flush `pagehide` l'écrase à la navigation et
  l'on mesure une partie neuve en croyant mesurer la sienne — c'est arrivé à la première passe.

## Vocabulaire

Le lieu d'id 7 est appelé **le souterrain** (affiché « Île 6 S ») ; l'id n'apparaît que pour parler
de code.
