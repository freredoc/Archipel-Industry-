# Rapport — 4 retours d'Ethan sur la sortie du tutoriel

Pas de brief : quatre retours envoyés avec deux captures d'écran, après le lot C (build 400).

| | avant | après |
|---|---|---|
| `GAME_BUILD` | 401 | **402** |
| `GAME_VERSION` | `Alpha 16.8` | **`Alpha 16.9`** |
| `SAVE_VERSION` | 31 | **31 — INCHANGÉ** |

Base : `main` à jour (401 / Alpha 16.8). **9 paires, toutes à `count == 1`**, round-trip verbatim,
`node --check` **7/7 sur les deux éditions**, delta **+7 584 car.**

⚠ **Numéro de build vérifié contre TOUTES les branches distantes** avant livraison (une collision
de build 400 a déjà eu lieu entre deux sessions parallèles) : le maximum est **401** — sur `main`
et sur `claude/carte-archipel-wmyxbs` —, donc **402 est libre**. Re-contrôlé juste avant le push.

---

## Retour 1 — « lingot et ciment l'un par dessus l'autre sur mobile »

**Cause.** `.tuto-count` est un flex **en ligne** et ses items sont `flex-shrink:0` — à dessein :
ils doivent survivre à l'ellipse du goal. Sous 480 px, `.tuto-main{flex-wrap:wrap}` (lot 3A) fait
bien passer le bloc de compteurs sur sa propre ligne, mais **les deux items y restent côte à
côte** : l'ensemble déborde et le second est coupé en plein milieu — exactement ta capture.

**Correctif.** Sous 480 px, `.tuto-count` passe en **colonne** (`flex-direction:column;
align-items:flex-start`). `margin-left:auto` est neutralisé, sinon la colonne resterait collée à
droite alors que la ligne wrappée commence à gauche. Les empiler est la seule issue : on ne peut
pas les rétrécir sans casser la raison d'être du `flex-shrink:0`.

**Mesuré en jeu (viewport 420 px)** : les deux items sont empilés, aucun ne dépasse le bord de la
bannière, aucun texte tronqué. Sur la base : non empilés, **débordants**.

---

## Retour 2 — « un dernier tuto nous dit d'aller sur l'île 2 alors qu'on l'a déjà visitée »

**Ce n'est pas le tutoriel, c'est le GUIDE** (bannière « Objectif », pas « Tuto ») — objectif
`go_ile2`, dont le `done` vaut :

```js
done: g => g.currentIsland === 2 || !!(g.guide && g.guide.seenIsland2)
```

**Cause racine.** Dans `checkGuide`, la pose de `seenIsland2` était **derrière** le `return` de la
garde « tutoriel actif » :

```js
if (g.tutorial && g.tutorial.active) { … return; }   // <- sortie
if (g.currentIsland === 2 && !g.guide.seenIsland2) { … }   // <- jamais atteint pendant le tuto
```

Or le chapitre île 2 fait précisément **passer le joueur sur l'île 2** — c'est la condition de
franchissement de l'étape 12. Le drapeau ne pouvait donc **jamais** être posé pendant le tutoriel.
À la fin, le joueur est revenu sur l'île 1 pour améliorer le transit : `currentIsland === 1`,
`seenIsland2 === false`, l'objectif s'arme et lui dit d'aller visiter une île dont il revient.

**Correctif.** La ligne remonte **avant** la garde. Ce drapeau n'est qu'un **constat d'état** (« le
joueur a vu l'île 2 »), pas un objectif : le gater sur le tutoriel le rendait faux au pire moment.
Une ligne déplacée, aucune logique nouvelle.

**Mesuré en jeu** : `seenIsland2 === true` à la fin du parcours, **aucune bannière d'objectif**.
Sur la base : `seenIsland2 === false` et la bannière `"Objectif — Une deuxième île est accessible.
Va la visiter."` — ta capture, reproduite au mot près.

---

## Retour 3 — « et après plus rien, alors qu'on devrait avoir un message de fin »

**Le message existait**, mais sous forme de **toast** : 1,8 s d'affichage, et la bannière du
tutoriel s'efface au même instant. Le joueur perd son fil conducteur d'un coup, sans rien pour
prendre le relais — et sur la base, ce vide était en plus comblé par l'objectif parasite du
retour 2.

**Correctif.** Nouvelle astuce **`tut_fin`** (« À toi de jouer »), ouverte par `showTip` à la fin
du tutoriel. C'est le **même canal que le « pourquoi » de chaque étape** — le joueur le connaît
déjà — et surtout il **attend un « Compris »** au lieu de disparaître. Le toast est conservé : les
deux sont complémentaires (l'un confirme le geste, l'autre explique la suite).

Trois points au contenu : la boucle acquise (extraire → acheminer → transformer → monter en
cadence), la suite qui se choisit dans l'arbre de recherche, et le fait que le guide continuera de
signaler les blocages (avec la mention qu'il est désactivable dans les Options).

⚠ **`showTip` MET EN FILE** si un popup est déjà ouvert (lot 3A) : il ne peut donc pas écraser le
« pourquoi » de la dernière étape — c'est précisément le piège qui avait rendu muet le dernier
`afterToast` au lot C.

**i18n** : les 4 langues, **3 paragraphes chacune** (la fusion d'`applyToData` se fait par index —
une langue à 2 paragraphes en laisserait un en français). Vérifié au runtime après l'IIFE
d'augmentation : `fr/en/es/de` → 3 § chacune, **0 paragraphe resté en français**.

**Mesuré en jeu** : popup ouvert à la fin, bouton « Compris » présent, texte conforme, et l'astuce
est **marquée vue à la fermeture** → consultable ensuite dans l'Aide. Sur la base : aucun popup.

---

## Retour 4 — « uniquement des nombres entiers dans le compteur »

**Cause.** Le compteur affichait `fmtRateSci(c[1])`, et `fmtRate` ne pose le séparateur de
milliers **que si la valeur est entière** — d'où l'incohérence visible sur ta capture :
« 5275,96 » (sans séparateur) face à « 10 000 » (avec).

**Correctif.** `Math.floor` à l'affichage. **Arrondi vers le bas, jamais au plus proche** : montrer
« 10 000/10 000 » pendant que l'étape refuse encore serait un mensonge — c'est la convention déjà
retenue pour `tutFlowOf` (13.80). Effet de bord bienvenu : la valeur redevenant entière, elle
récupère le séparateur et s'aligne enfin sur le max.

⚠ Le test `done` (`c[1] >= c[2]`, qui met le compteur au vert) reste sur les **valeurs brutes** :
le vert suit l'étape, pas l'arrondi.

**Mesuré en jeu**, avec un stock volontairement fractionnaire (5275,96 et 812,4) — sans quoi le
test serait vacueux, un port neuf étant à 0 : **« Lingots au port 5 275/10 000 »** et **« Ciments
au port 812/10 000 »**. Sur la base : « 5275,96/10 000 » et « 812,4/10 000 ».

---

## Validation

- `node --check` **7/7 en publique et 7/7 en dev**.
- **Round-trip** : 9 paires, ancre à `count == 1` sur la base, remplacement à `count == 1` sur le
  livré. **0 anomalie.**
- **Test en jeu (`T7`), 42 OK / 0 KO** — le chapitre est joué par de **vrais gestes** jusqu'à la
  fin du tutoriel, puis les 4 retours sont mesurés.
- **Contre-épreuve sur la base 401 : 9 KO**, un par symptôme. Le test est falsifiable.
- Suites **rejouées deux fois sans flottement**.
- **Non-régression du lot C** : extinction du halo (`portSeen`) 0 KO ; les harnais T1 (13 toasts,
  0 muet), T2, T3 et T5 tous à 0 KO.
- **Boot des deux éditions** : canvas 100 %, 0 `tickError`, console propre.

---

## Écart à signaler — une erreur de ma part, rattrapée par le round-trip

Mon premier script d'application n'a posé que l'astuce et ses traductions : **les trois correctifs
principaux (CSS, `seenIsland2`, popup) n'étaient pas dans le fichier**, alors que `node --check`
passait et que l'i18n de la nouvelle astuce était correcte. C'est le **round-trip** qui l'a
attrapé, en montrant 3 remplacements à `count == 0`. Sans lui, j'aurais livré un lot qui ne
corrigeait rien de ce qui avait été demandé. Contrôle refait après application complète : 9/9.

## Hors périmètre — non touché

`GUIDE_OBJECTIVES` et le reste des objectifs (seule la **pose** de `seenIsland2` change de place,
pas la définition de `go_ile2`), les étapes du tutoriel, `applyToData`, `SAVE_VERSION`, et le toast
de fin (conservé à côté du nouveau popup).
