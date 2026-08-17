# Rapport — lot B « chapitre île 2 »

Brief : `BRIEFlotBchapitreile2.md` · paires : `pairslotB.json` (23) · tests fournis :
`testT1runtimei18n.js`, `testT2steps.js`.

---

## 1. Versions

| | avant | après |
|---|---|---|
| `GAME_BUILD` | 380 | **381** |
| `GAME_VERSION` | `Alpha 14.97` | **`Alpha 14.98`** |
| `SAVE_VERSION` | 31 | **31 — INCHANGÉ** |

`SAVE_VERSION` n'a pas bougé : le seul champ ajouté au modèle de partie est
`tradeLinkReserve` en **nouvelle partie uniquement** (`newGame`), et il était déjà
sérialisé/restauré depuis le lot 13.16. Une save antérieure ne le porte pas et n'est pas
migrée — voir T4, scénario 2.

**Taille** : 3 336 909 → 3 349 788 o (**+12 879**), dont **+12 161 pour les 23 paires**
(exactement le patch) ; les 718 o restants sont le bump, les 4 lignes de commentaire de
version et le nouveau `GAME_NOTES`.

---

## 2. Les 23 `count` relevés AVANT application

Ancre comptée sur la base 380, remplacement compté sur le fichier livré.

| # | tag | count(ancre)/base | count(remplacement)/381 |
|---|---|---|---|
| 1 | `E1a-helper` | 1 | 1 |
| 2 | `E1b-planAllows` | 1 | 1 |
| 3 | `E1c-halo` | 1 | 1 |
| 4 | `E1d-revealed` | 1 | 1 |
| 5 | `E1e-tutStep` | 1 | 1 |
| 6 | `E1f-tabAllowed-comment` | 1 | 1 |
| 7 | `E2a-panelsRef-init` | 1 | 1 |
| 8 | `E2b-panelsRef-portOpen` | 1 | 1 |
| 9 | `E3-NumField` | 1 | 1 |
| 10 | `E4a-cible-charbon` | 1 | 1 |
| 11 | `E4b-port-upgrade` | 1 | 1 |
| 12 | `E9-linkReserve` | 1 | 1 |
| 13 | `E10-miroir` | 1 | 1 |
| 14 | `E6-steps` | 1 | 1 |
| 15 | `E7-tip` | 1 | 1 |
| 16 | `E8-TUT-fr` | 1 | 1 |
| 17 | `E8-TUT-en` | 1 | 1 |
| 18 | `E8-TUT-es` | 1 | 1 |
| 19 | `E8-TUT-de` | 1 | 1 |
| 20 | `E8bis-miroir-fr` | 1 | 1 |
| 21 | `E8bis-miroir-en` | 1 | 1 |
| 22 | `E8bis-miroir-es` | 1 | 1 |
| 23 | `E8bis-miroir-de` | 1 | 1 |

**23/23 à `count == 1`, aucune adaptation.** Le brief était pré-compilé sur la bonne base.

---

## 3. `node --check` — 7/7, sur les DEUX éditions

7 blocs `<script>` retenus (règle du mémo : ne compter que les balises dont le **préfixe de
ligne est vide** — les 4 correspondances textuelles restantes sont une chaîne du UMD React et
3 occurrences en commentaire).

```
ÉDITION PUBLIQUE (DEV_BUILD = false)   pub b1..b7  OK   (7/7)
ÉDITION DEV      (DEV_BUILD = true)    dev b1..b7  OK   (7/7)
```

L'édition dev est bien une **autre** source : son bloc 7 fait 1 578 960 car.
(`34b5f0f2…`) contre 1 578 961 (`fe1c003e…`) en publique — l'écart d'un caractère est
exactement `false` → `true`.

⚠ **Écart de MON harnais, signalé** : `extract.py` portait le chemin du jeu **en dur** et
ignorait son argument. Mon premier « node --check dev » a donc relu le fichier publique et
était **creux** ; le contre-test T1 « sur la base » l'était aussi. Corrigé (`sys.argv`),
les deux contrôles refaits pour de vrai — c'est cette version-là qui est rapportée ci-dessus
et au §7.

---

## 4. SHA-256 des blocs 6 et 7, ré-extraits du fichier

**Variante « patch SEUL »** (base + les 23 paires, sans bump ni commentaires de version) —
c'est elle que le brief décrit :

| bloc | attendu par le brief | mesuré | verdict |
|---|---|---|---|
| 6 | `f6cdea55…` / 232 100 | `f6cdea55af87528fdb0faa6d73d1cf2367219509802364d6d0226252832c73d9` / **232 100** | **conforme au caractère près** |
| 7 | `8bf4fb5f…` / 1 578 266 | `8bf4fb5f0acde7d63093b532aa9dbf4d5081772a0b666591b9c220ac355bbb58` / **1 578 266** | **conforme au caractère près** |

**Fichier réellement livré** (avec le bump) :

| bloc | mesuré |
|---|---|
| 6 | `f6cdea55af87528fdb0faa6d73d1cf2367219509802364d6d0226252832c73d9` / 232 100 — **identique** (le bloc 6 ne porte pas la version) |
| 7 | `fe1c003eca29d1d9995345f916caa01c3728180cfbd44d407ae4190c368464b8` / 1 578 961 |

⚠ **Écart de bloc 7 ASSUMÉ, et c'est structurel** : le bloc 7 porte
`GAME_BUILD`/`GAME_VERSION`/`GAME_NOTES`, or le brief est pré-compilé **avant** que le numéro
de version soit choisi — son hash ne peut décrire que la variante patch-seul. Les 695
caractères d'écart sont le bump + les 4 lignes de commentaire + le nouveau `GAME_NOTES`.
Même situation qu'aux lots A et A′.

Pour mémoire, la base 380 : bloc 6 `bb270eed…` / 227 358, bloc 7 `d1719184…` / 1 571 233.

---

## 5. Round-trip

Chaque remplacement re-cherché **verbatim** dans le fichier livré : **23/23 à `count == 1`,
0 anomalie d'échappement**. Aucun bloc n'a été retapé : les 23 chaînes sortent du JSON.

**Étapes 0 à 9 : octet à octet identiques.** Délimitation de `TUTORIAL_STEPS` par comptage de
crochets **conscient des chaînes ET des commentaires** (le fichier contient des apostrophes
françaises dans les commentaires — piège documenté au lot 14.91), puis découpe en spans
d'objets de profondeur 1 : base = 10 étapes, patch = 14, **aucune des 10 premières modifiée**.

Les 4 nouvelles : étape 10 (1 308 car., `67cb7851…`), 11 (258, `97da9525…`),
12 (823, `f20bcaca…`), 13 (881, `47742a83…`).

---

## 6. Boot navigateur — le piège n°2 est bien réel, et il est désamorcé

Le brief prévient qu'une zone morte temporelle (TDZ) sur `panelsRef.current.portOpen` donne
une **page blanche que `node --check` ne voit pas**. Vérifié au fichier :

```
23720:  const [portOpen, setPortOpen] = useState(false);
23724:  panelsRef.current.portOpen = portOpen;
```

L'assignation est bien **après** le `useState` — le piège est réel (l'inverse aurait levé une
`ReferenceError` au premier rendu) et il est évité.

**Boot des 2 éditions** :

```
PUBLIQUE : canvas 100 % · ticks 0 -> 6 · tickErrors {} · erreurs console 0
DEV      : canvas 100 % · ticks 0 -> 6 · tickErrors {} · erreurs console 0
```

Le **piège n°3** (`NumField` avale toute prop hors de sa destructuration) est lui aussi réel :
la signature ne listait que `{value, onCommit, title, className}`. `dataTut` y a été ajouté
**et** propagé sur l'`input` (`'data-tut': dataTut`). Prouvé au runtime par T3 : le champ
cible du charbon porte le hook, **et lui seul** (`n = 1`).

---

## 7. Les 4 tests

Toutes les suites ont été **jouées deux fois, sans flottement**.

### T1 — i18n au RUNTIME (fourni par le brief)

On **exécute** le bloc 6 dans un `vm` et on interroge `I18N` **après** l'IIFE d'augmentation.
C'est exactement le contrôle qui manquait au lot A′.

```
fr | entrées tutorial = 14   tut_transit : titre="Le transit se paie"  body=3 §
en | entrées tutorial = 14   tut_transit : titre="Transit has a price" body=3 §
es | entrées tutorial = 14   tut_transit : titre="El tránsito se paga" body=3 §
de | entrées tutorial = 14   tut_transit : titre="Transit kostet"      body=3 §
RUNTIME I18N : 0 KO
```

Les 14 goals sortent non-nuls dans les 4 langues (aucun ne retombe sur le goal inline), et
aucun paragraphe de `tut_transit` n'est resté en français après la fusion par index.

**Contre-test — le test est FALSIFIABLE.** La même suite rejouée sur le bloc 6 de la base 380 :

```
fr/en/es/de | entrées tutorial = 10
   KO <lg> 10..13 -> null (retomberait sur le goal inline)   [16 KO]
   tut_transit : ABSENT                                       [4 KO]
   -> 20 KO, puis un throw sur I.locales.fr.tips.tut_transit
```

### T2 — `TUTORIAL_STEPS` hors navigateur (fourni par le brief)

**22 assertions, 0 KO.** 14 étapes ; les 10 d'origine ne portent pas `island` ; îles du
chapitre = 1, 1, 2, 1 ; `tutStepIsland` défaute à 1 et lit le champ. Bornes exactes vérifiées :
l'étape 12 refuse 999 et accepte **1000**, et la cible de l'île 1 ne compte pas ; l'étape 13
refuse `portSpeed[2]` et accepte `portSpeed[1]`. Les `when` des cibles lisent `ui.portOpen`
sans jeter, et le halo de l'étape 13 avance bien onglet île → bouton Port → bouton Améliorer.

### T3 — le chapitre joué par de VRAIS gestes (22 OK / 0 KO)

Pas à pas, en navigateur, sur une partie réelle :

1. étape 9 → 10 : le tutoriel **entre** dans le chapitre au lieu de se terminer (`step=10`, `active=true`) ;
2. bannière **« Tuto 11/14 »** ;
3. halo sur le bouton **Réparer** ;
4. deux compteurs « Lingots au port 0/10000 » et « Ciments au port 0/10000 », lus au port de l'île 1 ;
5. ils montent bien à 10 000/10 000 ;
6. étape franchie **par la réparation réelle** (clic sur « Réparer l'île 2 ») → `step=11`, île 2 réellement débloquée ;
7. halo sur l'onglet **île 2** ; passage sur l'île 2 → `step=12` ;
8. halo sur le bouton **Port** (panneau fermé), puis **déplacé sur le champ cible** une fois le panneau ouvert ;
9. le champ cible du charbon porte le hook **et lui seul** (`n=1`) ;
10. étape franchie **par la saisie réelle de 1000** → `step=13` ; cible charbon île 2 = 1000 **et la réserve a suivi** (1000) grâce à « Cible ⇒ Réserve » ;
11. halo sur l'onglet **île 1**, puis sur le bouton **Port**, puis sur le bouton **Améliorer** du transit ;
12. amélioration réelle → `portSpeed[1] = 1` → **`step=14`, `active=false` : tutoriel terminé** ;
13. piège n°4 : `mine_cuivre` est bien présente au menu Bâtiment pendant le chapitre (6 bâtiments au menu) ;
14. console propre — 0 erreur.

⚠ **Piège de harnais rencontré** : le clic final était avalé par `useGhostGuard` (13.50) tant
qu'aucun `pointerdown` interne n'avait eu lieu — un `el.click()` synthétique passe à la
trappe. Il faut un **vrai clic Playwright** (`scrollIntoViewIfNeeded()` puis `.click()`).

### T4 — non-régression sur saves RÉELLES (7 OK / 0 KO)

| scénario | attendu | mesuré |
|---|---|---|
| partie **neuve** | « Cible ⇒ Réserve » à OUI sur les 7 îles | `{1..7: true}` ✅ |
| save **existante sans le champ** | reste à NON, aucune bascule silencieuse | `{}` ✅ |
| save **existante avec un NON explicite** | conservé tel quel | `{2: true}` — le NON de l'île 1 tient ✅ |
| save **terminée** (`active:false, step:10`) | le chapitre n'est **pas** rejoué | `active=false`, aucune bannière `Tuto n/14` ✅ |

Sur la save terminée, la bannière affichée est celle du **guide dynamique**
(« Objectif — Ouvre Réparer… ») : c'est le relais normal après le tutoriel, pas une reprise.

**Le lot A tient** : `sel: '.tab-upg'` rend **2**, et la délimitation par comptage de crochets
confirme que les **deux sont dans `TUTORIAL_STEPS`** (étapes d'index 4 et 5) et **zéro dans
`GUIDE_OBJECTIVES`** — la seule occurrence de `.tab-upg` dans le bloc du guide est le
commentaire du lot A qui explique le retrait.

**Suites antérieures rejouées sur 381** :

- lot A (halo / saturation) : **15 OK / 0 KO** — demande relevée 144,00 u/s, `netFactor` 0,889, bannière présente et **0 halo**, contre-test Nv.2 → Nv.1 concluant ;
- non-régression tutoriel (halo sur `.tab-upg` aux étapes 5 et 6) : **8 OK / 0 KO**.

⚠ **2 assertions mises à jour — renversements VOULUS, pas des régressions** : la suite de
non-régression assertait `Tuto 5/10` et `Tuto 6/10`, or la trame compte désormais **14**
étapes. Le comportement testé (halo DOM sur `.tab-upg`) est inchangé et vert ; l'assertion
porte maintenant sur le **numéro** d'étape et jamais sur le total figé, pour que la prochaine
retouche de trame ne donne pas un faux KO.

---

## 8. Écarts entre le brief et le fichier réel

1. **Bloc 7 — hash ≠ brief, structurel.** Le bloc 7 porte la version ; le brief est
   pré-compilé avant que le numéro soit choisi. Contrôle fait sur la variante patch-seul :
   `8bf4fb5f…` / 1 578 266, **conforme au caractère près**.

2. **Les 4 pièges annoncés sont tous RÉELS** — aucun ne s'est avéré faux :
   - n°1, la source i18n vivante est l'**IIFE d'augmentation**, pas le grand littéral : les
     entrées ont été posées dans l'IIFE (`E8-TUT-*`) **et** dans le miroir littéral
     (`E8bis-miroir-*`), avec le commentaire « ⚠ MIROIR, PAS SOURCE » posé au-dessus de
     `var LOCALES` (`E10-miroir`) pour que le prochain lecteur ne s'y trompe pas — c'est
     exactement le piège qui avait fait dérailler le lot A′ ;
   - n°2, TDZ sur `panelsRef.current.portOpen` : vérifié au fichier (l. 23720 puis 23724) et
     par le boot ;
   - n°3, `NumField` avale les props inconnues : sa destructuration ne listait bien que
     4 clés ; `dataTut` ajouté et propagé, prouvé au runtime ;
   - n°4, le filtre du menu Bâtiment : sans `game.tutorial.step < 9` dans `tutorialRevealed`,
     le menu serait resté filtré pendant le chapitre. Vérifié en jeu (6 bâtiments au menu).

3. **Écart de mon harnais, à ma charge** : `extract.py` avait le chemin du jeu en dur et
   ignorait son argument → mon premier contrôle « node --check dev » et le contre-test T1
   « sur la base » relisaient tous deux le fichier publique et étaient **creux**. Corrigé,
   les deux refaits ; c'est ce qui a permis d'obtenir les 20 KO de falsification du §7.
   **Leçon** : un outil de harnais qui prend un chemin doit échouer bruyamment s'il ne
   l'utilise pas.

4. **Deux pièges de harnais T4, déjà au mémo, retombés dessus** :
   (a) `addInitScript` **rejoue à chaque navigation, reload compris** → un `localStorage.clear()`
   nu efface la save forgée au rechargement et le test repart sur une partie **neuve**
   (14.59) ; (b) rejouer une save exige les **trois** clés de slot — `archipel_slot_<id>`,
   `archipel_slots` et `archipel_active` (14.52) — le seul slot ne suffit pas. Première passe :
   3 OK / 3 KO **de harnais**, aucun défaut produit. Corrigé par un drapeau `sessionStorage`
   et la capture/réinjection de tout le `localStorage`.

5. **Effet de bord favorable, non listé par le brief** : `GAME_NOTES` a été réécrit en
   **UTF-8 littéral** (et non en `\uXXXX` hérités de Babel). L'étape CI « Sync version.json »
   fait `grep -oP 'const GAME_NOTES = "\K[^"]*'` puis `jq --arg notes` : avec l'ancien style,
   `jq` échappait le backslash et le joueur lisait `Ã©` / `Â«` sous « Mise à jour disponible ».
   Vérifié : la chaîne livrée ne contient **aucun `\u`**, et pas un seul guillemet droit (le
   `[^"]*` la tronquerait) — guillemets français uniquement.

---

## Hors périmètre — non touché

`GUIDE_OBJECTIVES` et le lot A, les étapes 0 à 9 de `TUTORIAL_STEPS` (octet à octet),
`SAVE_VERSION`, `applyToData`, `checkTutorial`, `tabAllowed` (seul son commentaire est
amendé), les sous-clés `res`/`bld`/`tech`/`ui` des `LOCALES`, la mécanique du transit et de
« Cible ⇒ Réserve » elle-même (seule sa **valeur par défaut en nouvelle partie** change).
