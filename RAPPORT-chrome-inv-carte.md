# RAPPORT — LOT CHROME : barre d'inventaire + carte pré-île 6

## Version produite

| | |
|---|---|
| Base | build **428** / `Alpha 19.5` — 3 735 145 o |
| Livré | build **429** / `Alpha 19.6` |
| `SAVE_VERSION` | **31, INCHANGÉ** — aucun champ de partie touché |
| Branche | `claude/petit-bug-fix-3h59fa` (partie exactement de `main`, `52abb45`) |

Le brief ne proposait aucun numéro. **429 est libre** : relevé fait sur **toutes** les branches
distantes (`origin/main` et `origin/claude/petit-bug-fix-3h59fa` sont toutes deux à 428), contrôle
imposé par le mémo depuis la collision du build 400.

`GAME_NOTES` réécrit en UTF-8 littéral, **sans guillemet droit ni apostrophe droite** (vérifié par
programme) ; le `grep -oP 'const GAME_NOTES = "\K[^"]*'` de la CI l'extrait en entier.

---

## Ancres — 8/8 à `count == 1`

Patcheur `patch_chrome.py` **rejoué tel quel** sur une base fraîche, aucune ligne retapée.

| # | Nom | Zone | Compte |
|---|---|---|---|
| 1 | `A1-bar` | `InvBar`, retour anticipé de la barre repliée | 1 |
| 2 | `A2-panneau` | `InvBar`, en-tête du panneau ouvert | 1 |
| 3 | `A3-fragment-close` | `InvBar`, fermeture du Fragment | 1 |
| 4 | `A4-css-margin` | feuille de style, règle `.inventory.open` | 1 |
| 5 | `M1-table` | `const ARCHI_POS = …` | 1 |
| 6 | `M2-select` | `ArchipelMap`, après `const visible = …` | 1 |
| 7 | `M3-liens` | lecture des coordonnées des liaisons | 1 |
| 8 | `M4-iles` | lecture des coordonnées des nœuds | 1 |

**Delta : 3 735 145 → 3 738 579 o = +3 434 exactement**, la valeur annoncée au byte près.

---

## SHA-256 — ré-extraits du fichier patché

Blocs comptés par `(?m)^<script` : **7** (le naïf `<script[^>]*>` en trouve 11 — chaînes et
commentaires). Extracteur écrit dans `/tmp`, prenant son chemin en argument et **échouant
bruyamment** s'il ne le trouve pas (leçon 14.98).

### Avant bump — conformité au brief : **8/8**

Fichier complet : `4f248981a1c585d5537c872b4b72ca2fae3270e726a13069d2526db01de89563` — **identique
au brief**.

| Bloc | État | SHA-256 | Conforme au brief |
|---|---|---|---|
| b01 | identique | `a50c1c4e7f4a304c650c0cfa7e06c4ffdfb0d87510d83738114d4dd5a1641628` | ✔ |
| b02 | identique | `8fbb22187703339c146b2f82badd8701d199128cd3d9dd8673d213c8616ca541` | ✔ |
| b03 | identique | `d949f1c3687aedadcedac85261865f29b17cd273997e7f6b2bfc53b2f9d4c4dd` | ✔ |
| b04 | identique | `35f4f974f4b2bcd44da73963347f8952e341f83909e4498227d4e26b98f66f0d` | ✔ |
| b05 | identique | `1be53ce44e7be14fb81bd92e6a338cba274304f38c6077061fd3e33232cc2651` | ✔ |
| b06 | identique | `8a382825620df9d1dbe47c7605b3a1a2e38a3a3b8a7852e75a0649c686881052` | ✔ |
| b07 | **MODIFIÉ** | `f1fb1caa68ad9e6aef348c1c631a4be75424eafa700ae0dcca973aa7ba0114e3` | ✔ |

La modification CSS (A4) vit dans le bloc `<style>` : elle n'apparaît dans aucun SHA de bloc
`<script>`, seulement dans celui du fichier complet — conforme à ce qu'annonce le brief.

### Après bump + commentaire cumulatif

`GAME_BUILD` vit dans b07, le bloc que le patch modifie : **b07 et le fichier complet divergent
nécessairement**, c'est attendu et documenté par le brief. b01→b06 restent identiques au byte près.

| Bloc | SHA-256 |
|---|---|
| b01→b06 | inchangés (voir tableau ci-dessus) |
| b07 | `7a9a2db68b82c13de7cb71b5261ca9ee4a05484e922f06e5c22f864f5a1ebd9e` |
| fichier complet | `a074675709ca180877ba5e66260aae516166663b0ef0931b49ea9062d33a982f` |

**Taille finale : 3 744 974 o** (+9 829 sur la base). Le CODE seul pèse **+3 434** ; le reste est le
commentaire cumulatif de version (le lot est très commenté, conformément aux conventions du projet)
et `GAME_NOTES`.

**Ordre respecté** : patcheur → vérification des SHA → **ensuite seulement** le bump.

---

## `node --check`

**7/7 sur les 3 variantes CI**, simulées par les `sed` exacts d'`android.yml` :

| Variante | Blocs OK |
|---|---|
| `game-public.html` | 7/7 |
| `game-dev.html` (`DEV_BUILD = true`) | 7/7 |
| `game-store.html` (`SELF_UPDATE = false`, `SUPPORT_URL = ''`) | 7/7 |

Invariants CI rejoués : `ko-fi` = **1** dans la publique, **0** dans le magasin ;
`DEV_BUILD = false` bien conservé dans la publique ; `BUILD` extrait par la CI = **429**.

> Rappel : un `node --check` vert ne prouve que la syntaxe. Le boot réel est fait plus bas (T8).

---

## Suite de validation

**Setup réellement utilisé** — Chromium 1194 piloté par **playwright-core**, servi en HTTP sur
`127.0.0.1:8099` **depuis la racine du dépôt** (sinon 404 et fausse page blanche), `localStorage`
purgé par `addInitScript`, locale forcée `fr`, attente de 6 s après `networkidle`, astuces fermées
par `.tip-ok` (**jamais** `.remove()`). Toutes les suites ont été **rejouées deux fois sans
flottement**.

| Test | Verdict | Mesure |
|---|---|---|
| T1 — invariance du chrome | **PASS** | les 5 grandeurs identiques aux 3 relevés |
| T2 — contre-test base 428 | **PASS** (échoue comme prévu) | écart de **49 px exactement** |
| T3 — aucun bouton dupliqué | **PASS** | 1 `.inv-label-btn`, 1 `.inv-prod-btn` |
| T4 — liseré | **PASS pour la part mesurable** ; coup d'œil appareil **NON COUVERT** | voir ci-dessous |
| T5 — carte pleine largeur, sans débordement | **PASS** | 412 px et 320 px, 0 débordement |
| T6 — branche île 6 intacte, `arch-sout` | **PASS** | 6 nœuds, 1 `arch-sout` à 81 %, 0 erreur |
| T7 — bascule sur `visible(6)` | **PASS** | table pleine, 5 sprites, 1 cible |
| T8 — non-régression au boot | **PASS** | 0 `pageerror`, 0 `tickError`, canvas 100 % |

### T1 — invariance du chrome au basculement *(cœur du lot)*

Viewport 412×915. L'inventaire étant **déplié à la création** (13.84), les trois relevés sont
« initial ouvert → replié → rouvert ».

| Relevé | `hudH` | `--hud-h` | `tutoTop` | `canvas.height` | `canvas.top` |
|---|---|---|---|---|---|
| initial (ouvert) | 167 | 167px | 167 | 603 | 233 |
| replié | 167 | 167px | 167 | 603 | 233 |
| rouvert | 167 | 167px | 167 | 603 | 233 |

**Invariance 5/5.** Ce sont exactement les valeurs annoncées par le brief.

### T2 — CONTRE-TEST sur la base 428 non patchée

Même protocole, `_base428.html`. Les valeurs bougent, et de **49 px exactement** dans les deux sens :

| | inventaire ouvert | inventaire replié | écart |
|---|---|---|---|
| `.hud-stack` offsetHeight | 118 | 167 | +49 |
| `--hud-h` | 118px | 167px | +49 |
| `.tuto-banner` top | 118 | 167 | +49 |
| `canvas.height` | 652 | 603 | −49 |
| `canvas` top | 184 | 233 | +49 |

Conforme au tableau du brief à la valeur près. **Sans ce contre-test, T1 serait un vert à vide** :
c'est lui qui prouve que le harnais mesure quelque chose.

### T3 — aucun bouton dupliqué

Inventaire **ouvert** : `.inv-label-btn` = **1**, `.inv-prod-btn` = **1**.

Mesure ajoutée hors brief, qui rend le test parlant : `document.querySelectorAll('.inventory')`
donne **2** sur le patch (la barre **et** le calque) contre **1** sur la base — la preuve directe
que le calque s'ajoute à la barre au lieu de la remplacer, sans dupliquer les boutons.

### T4 — le liseré

La part mesurable est **couverte et falsifiable**. Sonde par `elementFromPoint` dans les 4 px
au-dessus de la barre d'inventaire, inventaire **ouvert** :

| | patch 429 | base 428 |
|---|---|---|
| barre dans le flux | **oui** | non |
| élément vu au-dessus de la barre | `.hud-stack` (fond `rgba(126,138,162,.26)`) | **`CANVAS`** |
| `margin-top` du panneau ouvert | **0px** (effet de A4) | 4px |

Autrement dit : sur la base, les 4 px du liseré tombent sur le canvas — le liseré **disparaît**,
exactement le défaut signalé ; sur le patch il reste dans `.hud-stack`. Captures d'écran comparées
(inventaire ouvert) : sur la base le bandeau du tutoriel remonte se coller au HUD ; sur le patch la
barre reste en place et le bandeau ne bouge pas.

> **NON COUVERT** : le coup d'œil sur appareil (S25 FE, navigation à 3 boutons). Aucun banc ne le
> remplace, et c'est le dernier mot sur ce point.
>
> **Écart de lecture à connaître** : le gap entre la barre et le calque n'est pas nul, il vaut la
> hauteur du bandeau du tutoriel — `.inventory.open` est ancré en
> `top: calc(100% + var(--tuto-h))`, comportement voulu depuis 13.84 (l'inventaire ouvert
> recouvrait le bandeau). Le `margin-top:0` d'A4 supprime bien les 4 px parasites, il ne supprime
> pas cet ancrage.

### T5 — la carte remplit la largeur, sans déborder

Nouvelle partie, carte ouverte par `.map-btn`. Positions rendues : **20 / 80 / 46 / 20 / 77 %** aux
deux viewports (table `ARCHI_POS_5`).

| viewport | W de `.arch-map` | marge gauche | marge droite | occupation | débordements |
|---|---|---|---|---|---|
| 412 px | 351,3 px | 18,8 px | 18,9 px | **89,3 %** | **0** |
| 320 px | 264,8 px | 1,5 px | 1,6 px | **98,8 %** | **0** |

Assertion `left >= 0 && right <= W` vérifiée sur les 5 sprites, aux deux viewports. Le test à
320 px n'était pas optionnel : c'est là que se joue le vrai risque du volet 2 (sprite de 104 px
**fixes**, positions en pourcentage).

*Écart mineur au brief : il annonçait 2 px / 2 px à 320 px, la mesure donne 1,5 / 1,6 px — arrondi,
même conclusion (0 débordement).*

### T6 — la branche île 6 est intacte, et `arch-sout` ne casse pas

`window.ArchipelMap` rendu **hors du jeu** dans un conteneur de 351 px
(`ArchipelMap` est une déclaration de fonction d'un script classique, donc bien une propriété de
`window`), avec `game = {islandUnlocked:{2..6}, currentIsland:1, elevatorRepaired:true}` et
`repairInfo = null`.

Résultat : **6** `.arch-node` aux positions d'origine **20 / 54.5 / 35 / 20 / 53 / 81 %**, **1**
`.arch-sout` à **81 %**, **0 `pageerror`**. Router `arch-sout` sur `POS` lèverait ici un
`TypeError` sur `POS[6][0]` — c'est ce que ce test verrouille.

### T7 — le basculement suit `visible(6)`, pas `ouverte(6)`

Même harnais, 5 îles ouvertes, `elevatorRepaired:false`,
`repairInfo = {island:6, locked:true, node:28, cost:{}}`.

Résultat : **table PLEINE** (20 / 54.5 / 35 / 20 / 53 / 81 %), **5** `.arch-ile` seulement (l'île 6
est en mode « mystère », sans sprite), **1** `.arch-node.target`.

**Contre-test ajouté** (hors brief, pour rendre T7 falsifiable) : même montage avec 5 îles et
**aucune** cible de réparation → positions **20 / 80 / 46 / 20 / 77 %**. La bascule suit donc bien
`visible(6)` et pas autre chose.

### T8 — non-régression au boot

20 s de jeu réel : `playTicks` 21 → 41, `tickErrors` **vide**, canvas peint à **100 %**,
**0 `pageerror`**.

Deux erreurs console subsistent — un `404` et un `ERR_CONNECTION_RESET` (le `fetch` sortant vers
`version.json`, bloqué en bac à sable). **Contre-épreuvées sur la base 428 : identiques.** Ce n'est
pas une régression, c'est le bruit connu du banc.

---

## Écarts par rapport au brief

1. **Banc porté sur playwright-core** — `puppeteer-core` et `@sparticuz/chromium` sont **absents de
   l'image** ; on dispose de `playwright-core` + Chromium 1194, et `playwright install` est proscrit
   (le paquet attend une autre révision). Le pilote seul change ; les montages et les assertions
   sont repris à l'identique. Même arbitrage qu'au lot PWA-1.
2. **Le bouton INVENTAIRE n'a PAS été cliqué deux fois.** Le brief prescrit deux clics
   (`useGhostGuard`) ; c'est faux **sur un interrupteur** — deux clics le basculent deux fois et le
   ramènent à son état de départ, exactement le piège déjà payé au lot SHOT-1. Le harnais amorce le
   garde par un `pointerdown` dans le HUD, clique **une** fois, puis **asserte l'état atteint** et
   réessaie s'il a été avalé. Robuste dans les deux hypothèses.
3. **T4 n'est couvert qu'à moitié** (voir plus haut) : la part mesurable est vérifiée et
   contre-épreuvée, le rendu sur appareil ne l'est pas.
4. **Marges T5 à 320 px** : 1,5 / 1,6 px mesurés contre 2 / 2 annoncés — arrondi, conclusion
   inchangée (0 débordement).
5. **Trois mesures ajoutées** hors brief, toutes falsifiantes : le compte de `.inventory` en T3, la
   sonde `elementFromPoint` de T4, et le contre-test de bascule de T7.

---

## Points restés en suspens

- **T4 sur appareil** : à confirmer d'un coup d'œil sur le S25 FE — le liseré doit rester visible
  inventaire ouvert, et le panneau se coller sous le bandeau sans bande de canvas parasite.
- **`makeIcon` reste du code mort** (signalé au lot ICON-1, non traité ici, hors périmètre).
- **Aucune transition d'élargissement de la carte**, volontairement : le panneau Carte n'est pas
  monté à l'instant où `visible(6)` bascule, et le renvoi des nœuds d'accès (lot 4b) le monte déjà
  en disposition large — une transition ne jouerait jamais et ne serait que du risque.
- **Hors périmètre, non touché** : `ARCHI_POS` (table pleine), `ARCHI_CACHEE`, `arch-sout`,
  `TutorialBanner`, le `ResizeObserver` de `.hud-stack`, `SAVE_VERSION`, la CI.
- **PR ouverte, non mergée** — le merge sur `main` appartient à Ethan : il déclenche `android.yml`,
  qui republie l'APK, `index.html` et `version.json`.
