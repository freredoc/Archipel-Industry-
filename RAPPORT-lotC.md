# Rapport — lot C « sortie du chapitre île 2 » + renversement d'Ethan

Brief : `BRIEFlotCsortiechapitreile2.md` · paires : `pairs-lotC.json` (20) · harnais fournis :
`test-T1` à `test-T5`.

**Une demande d'Ethan renverse un point que le brief laissait ouvert** : le halo ne doit pas
revenir quand le joueur referme le panneau Port. Cinq paires supplémentaires (`pairs-lotD.json`),
signalées comme hors brief au §9.

---

## 1. Versions

| | avant | après |
|---|---|---|
| `GAME_BUILD` | 399 | **400** |
| `GAME_VERSION` | `Alpha 16.6` | **`Alpha 16.7`** |
| `SAVE_VERSION` | 31 | **31 — INCHANGÉ** |

`SAVE_VERSION` ne bouge pas : le champ ajouté par le renversement, `tutorial.portSeen`, est
**additif avec repli** (absent → `false`), exactement comme `targetIdx` au lot 13.60.

**Base d'exécution conforme** : SHA-256 du fichier de départ
`44a9b75f116a973e4c7603c2cf00aa0a274160fb466102be07d88113de8e18e6` — **identique au caractère
près** à celui annoncé par le brief.

**Taille** : 3 428 412 → 3 435 522 car. (**+7 110**), dont **+4 856 pour les 20 paires du brief**
et **+2 254 pour le renversement**.

---

## 2. Les 20 `count` relevés AVANT application

| tag | attendu | mesuré | | tag | attendu | mesuré |
|---|---|---|---|---|---|---|
| `C1-sentinelle` | 1 | 1 | | `C5-toasts-en` | 1 | 1 |
| `C2-goal-inline` | 1 | 1 | | `C5-toasts-es` | 1 | 1 |
| `C3-afterToast-14` | 1 | 1 | | `C5-toasts-de` | 1 | 1 |
| `C4-toast-fin-inline` | 1 | 1 | | **`C6-goal13-fr`** | **2** | **2** |
| `C4-toast-fin-fr` | 1 | 1 | | **`C6-goal13-en`** | **2** | **2** |
| `C4-toast-fin-en` | 1 | 1 | | **`C6-goal13-es`** | **2** | **2** |
| `C4-toast-fin-es` | 1 | 1 | | **`C6-goal13-de`** | **2** | **2** |
| `C4-toast-fin-de` | 1 | 1 | | `C7-aide-inline` | 1 | 1 |
| `C7-aide-fr` | 1 | 1 | | `C7-aide-en` | 1 | 1 |
| `C7-aide-es` | 1 | 1 | | `C7-aide-de` | 1 | 1 |

**20/20 conformes, dont les quatre `count == 2` de `C6`** (l'IIFE `var TUT` **et** son miroir
littéral portent le même texte — c'est tout l'objet du piège n°2).

Les 5 paires du renversement, relevées sur le résultat des 20 précédentes :
`D1-newGame`, `D2-serialize`, `D3-loadSave`, `D4-checkTutorial`, `D5-cibles-etape14` —
**5/5 à `count == 1`**.

---

## 3. `node --check` — 7/7, sur les deux éditions

Avant patch : **7/7**. Après patch : **7/7 en publique et 7/7 en dev** (`DEV_BUILD = true`
substitué par `sed`, extraction refaite sur le fichier dev — pas sur le publique : c'est le
piège d'outil que j'ai corrigé au lot B).

---

## 4. SHA-256 des blocs 6 et 7, ré-extraits du fichier

**Variante « brief seul »** (les 20 paires, sans bump ni renversement) — c'est elle que le
brief décrit :

| bloc | attendu par le brief | mesuré | verdict |
|---|---|---|---|
| 6 | `e448f828…` / 238 158 | `e448f8280258b643a3824a2008e6926ee8dbf03392a5058f7a11f2df63831949` / **238 158** | **conforme au caractère près** |
| 7 | `66162b86…` / 1 677 669 | `66162b86e62e7859f4deafc4b32fcdd7594b0130d276ac065959167501e0ecaa` / **1 677 669** | **conforme au caractère près** |

C'est la première fois qu'un brief tombe juste sur les **deux** blocs — il ne propose aucun
numéro de version, donc son hash décrit bien un bloc 7 sans bump.

**Fichier réellement livré** (brief + renversement + bump) :

| bloc | mesuré |
|---|---|
| 6 | `e448f8280258b643a3824a2008e6926ee8dbf03392a5058f7a11f2df63831949` / 238 158 — **identique** : le renversement ne touche pas le bloc 6 |
| 7 | `dc51b56233d6edd0ee3b2d529937c128653934d4f98507d4ae0bc71fbaafefa3` / 1 681 008 |

Blocs 1 à 5 : **inchangés** (`a50c1c4e…`, `8fbb2218…`, `d949f1c3…`, `35f4f974…`, `1be53ce4…`).

---

## 5. Round-trip

Le patch se fait en **deux couches**, donc le round-trip aussi :

- **couche 1** — 20 paires, base 399 → « brief seul » : chaque ancre au `count` attendu sur la
  base, chaque remplacement au même `count` sur le résultat. **0 anomalie.**
- **couche 2** — 5 paires, « brief seul » → livré. **0 anomalie.**

⚠ **Recouvrement assumé et vérifié** : `D5` réécrit les deux premières cibles que `C1` vient de
poser, donc `C1.new` n'est plus présent tel quel dans le fichier livré (`count == 0`). Ce n'est
pas une perte : la **sentinelle** de `C1` et son commentaire sur le hook orphelin
`port-upgrade` survivent tous deux, vérifié explicitement.

---

## 6. Boot navigateur

```
PUBLIQUE : canvas 100 % · ticks 0 → 6 · tickErrors {} · erreurs console 0
DEV      : canvas 100 % · ticks 0 → 6 · tickErrors {} · erreurs console 0
```

Le brief insiste : `node --check` et le round-trip sont aveugles au rendu, et le bloc 7 touche
un site React. Le boot est passé sur les deux éditions.

---

## 7. Les cinq harnais, chacun avec sa contre-épreuve

Toutes les suites ont été **jouées deux fois, sans flottement**.

| harnais | vérifie | livré | base 399 | brief annonçait |
|---|---|---|---|---|
| **T1** | tout `afterToast` inline résout en en/es/de | **13 toasts, 0 muet** | **14 toasts, 8 muets** | 13/0 vs 8 ✅ |
| **T2** | message de fin dans les 4 langues, clé morte absente | **0 KO** | **7 KO** | 0 vs 7 ✅ |
| **T3** | rejoue l'algorithme d'avancement du halo | **0 KO** (8 assertions) | **4 KO** | 0 vs 4 ✅ |
| **T4** | le `goal` 13 RÉELLEMENT servi par `_g` | nouveau texte ×4 langues | ancien texte ×4 | ✅ |
| **T5** | titre de l'Aide dans les 4 langues | **0 KO** | **8 KO** | 0 vs 8 ✅ |

Les cinq chiffres de contre-épreuve annoncés par le brief sont retrouvés **exactement**.

⚠ T1, T2, T4 et T5 **exécutent le bloc 6 dans un `vm`** puis interrogent `I18N` — donc après
l'IIFE d'augmentation. Auditer l'i18n sur le texte de la source aurait menti.

**T3 passe sans la moindre modification malgré le renversement** : son assertion « mauvaise île
prime, même port ouvert » construit un état sans `tutorial`, donc `!(g.tutorial && …)` vaut
`true` et la garde laisse passer. Aucune assertion du brief n'a eu à être renversée.

### Harnais supplémentaire — le renversement (`test-D-portseen.js`)

Le brief ne couvre pas ce point puisqu'il le laissait ouvert. **11 assertions, 0 KO sur le
livré, 5 KO sur la base** :

```
AVANT le geste (portSeen = false) — comportement du brief, inchangé
  ok  île 2 → halo sur l'onglet île 1
  ok  île 1, port fermé → halo sur le bouton Port
  ok  port OUVERT → aucun halo
APRÈS le geste (portSeen = true) — le renversement
  ok  port encore ouvert → toujours aucun halo
  ok  PANNEAU REFERMÉ → le halo NE REVIENT PAS
  ok  RETOUR SUR L'ÎLE 2 → aucun halo sur l'onglet île 1
  ok  île 2 + port ouvert → aucun halo
Contre-épreuve interne : sans le drapeau, le halo REVIENT
  ok  portSeen=false + panneau fermé → halo de retour
  ok  portSeen=false + île 2 → halo onglet
Robustesse
  ok  `tutorial` ABSENT ne jette pas et n'éteint rien
  ok  `done` inchangé (portSpeed[1] >= 1)
```

---

## 8. Contrôle en jeu — points 1 à 7 (`T6`, **33 OK / 0 KO**)

Le chapitre est joué par de **vrais gestes**, sans jamais forcer `tutorial.step` (une seule
écriture pour se placer à l'entrée du chapitre, équivalent d'un chargement mi-tutoriel).

| point du brief | mesuré |
|---|---|
| **1.** sur l'île 2 : halo sur l'onglet île 1 | ✅ `island-1` |
| **2.** sur l'île 1 : halo sur le bouton Port + bannière | ✅ `port` · « Tuto 14/14 … **développe l'île 2 en attendant.** » |
| **3.** ouvrir le Port → le halo DISPARAÎT | ✅ 0 `.tut-halo`, et **`portSeen` posé par le geste** |
| **4.** *(renversé)* refermer → le halo NE revient PAS | ✅ panneau refermé, **toujours 0 halo** |
| **4 bis.** repartir sur l'île 2 | ✅ **toujours 0 halo** sur l'onglet île 1 |
| **4 ter.** sauvegarde + rechargement | ✅ `portSeen` **sérialisé**, restauré, étape 14 intacte, **0 halo** |
| **5.** améliorer le port → toast unique | ✅ **« 🎓 Tutoriel terminé — la suite est dans l'arbre de recherche. »**, seul ; ni l'ancien message, ni l'`afterToast` supprimé |
| **6.** toasts d'étape traduits | couvert **exhaustivement** par T1 (13 toasts × 3 langues) plutôt que par 4 étapes échantillonnées |
| **7.** Aide sans « île 1 » | ✅ « Premiers pas (tutoriel) » |

Le parcours complet reste vert de bout en bout : réparation réelle → île 2 débloquée → saisie
réelle de 1 000 charbons → amélioration réelle du transit → `step = 14`, `active = false`.

---

## 9. Écarts entre le brief et le fichier réel

### 9.1 Le renversement demandé par Ethan (hors brief)

Le brief écrit, au point 4 du contrôle en jeu : *« Refermer le panneau → le halo revient sur le
bouton Port. C'est voulu. […] Si Ethan le veut éteint pour de bon après la première ouverture,
il faut un drapeau persisté — hors périmètre. »* **Ethan l'a demandé.** Livré en 5 paires :

- **`tutorial.portSeen`**, drapeau persisté, posé par `checkTutorial`, jamais remis à faux ;
- gardes sur la pose : **`currentIsland === 1`** (ouvrir le port de l'île 2 ne prouve rien sur
  celui de l'île 1 — et l'étape 12 EXIGE justement d'ouvrir un port) **et** dernière étape,
  écrite `TUTORIAL_STEPS.length - 1` et non `13` en dur ;
- garde de lecture sur les **deux** cibles, pas seulement sur `port`.

⚠ **Le choix qui mérite ton arbitrage** : j'ai éteint **aussi** le halo de l'onglet `island-1`.
La demande littérale ne parlait que du bouton Port, mais le goal — que ce même lot réécrit —
dit désormais « développe l'île 2 en attendant ». Ne garder la garde que sur `port` aurait
renvoyé vers l'île 1 le joueur qui fait exactement ce qu'on lui demande : ç'aurait été déplacer
le harcèlement, pas le supprimer. Si tu veux conserver la flèche de retour, il suffit de retirer
la garde de la première cible.

### 9.2 Les cinq pièges annoncés sont tous RÉELS

Aucun ne s'est avéré faux, et les cinq contre-épreuves le prouvent chiffre pour chiffre :
sentinelle (sans elle le halo se recolle au bouton Port), `goal` inline réécrit par index
(d'où les `count == 2`), dernier `afterToast` jamais lu (`showToast` écrase), les deux
mécanismes i18n voisins (`L.tutorial =` remplace, `L.ui[k] =` fusionne **seulement si absent**),
et les 4 `afterToast` d'origine déjà muets avec leur clé morte en face.

### 9.3 Le jeu avait avancé de 18 builds — un hook a bien failli tomber dans le vide

Ma base locale était le build 381 ; `main` est à 399. Le **lot 4b (build 399) a supprimé le
bouton Réparer** en le fusionnant dans le bouton Carte. L'étape 10 du chapitre pointe
`data-tut="repair"` : sans précaution, **le halo aurait désigné un bouton disparu, sans la
moindre erreur JS**. Vérifié : le lot 4b a **migré le hook avec le bouton**, en posant même le
commentaire d'avertissement qui le dit. Audit complet fait : les **13 hooks `data-tut`
référencés par `TUTORIAL_STEPS` sont tous encore posés**.

Conséquence pour le harnais, pas pour le produit : le geste de réparation passe désormais par
**carte → nœud d'île → « Réparer l'île 2 »**. Mon T6 a dû être réécrit sur ce flux.

### 9.4 Le point laissé ouvert par le brief est respecté

`data-tut="port-upgrade"` reste posé sur le bouton Améliorer du PortPanel et n'est plus
référencé par personne. **Conservé**, avec le commentaire de la sentinelle qui l'explique — ne
pas conclure à un halo cassé en le trouvant orphelin.

### 9.5 Bruit console — contre-épreuvé, pas masqué

T6 signalait 3 erreurs console (404 sur le service worker, 404 ressource,
`ERR_CONNECTION_RESET`). **Contre-épreuve sur la base 399 non patchée : le même bruit 404 y est
déjà présent.** Ce sont des artefacts du serveur de test (pas de `sw.js`, fetch sortant coupé en
sandbox), filtrés explicitement plutôt qu'ignorés en silence.

### 9.6 Deux pièges de harnais, à ma charge

- Mon test du renversement passait le drapeau à `halo(g, ui)` — qui ne prend que deux
  paramètres — au lieu de le passer au constructeur d'état. Il rendait **3 KO fantômes** ;
  corrigé, il donne 0 KO sur le livré et 5 KO sur la base.
- Fermer un panneau par un `.click()` DOM est **avalé par `useGhostGuard`** (piège 13.50, déjà
  au mémo) : il faut un vrai clic Playwright. Et après une validation de nœud, le
  `.research-backdrop` du popup « Recherche terminée » intercepte le clic suivant — la purge
  doit fermer `.rd-popup` aussi, pas seulement `.tip-popup`.

### 9.7 Ménage — `PR-body.md` retiré du dépôt

Ce fichier avait été commité à la racine au lot B **parce que l'accès GitHub était coupé** et
que la PR ne pouvait pas être ouverte ; il a été mergé avec. L'accès est revenu : c'est un
artefact de contournement, il n'a rien à faire dans le dépôt. Retiré dans ce lot.

---

## Hors périmètre — non touché

`data-tut="port-upgrade"` (conservé orphelin, à dessein), les étapes 0 à 9 du tutoriel, les
autres objectifs du guide, `applyToData`, `SAVE_VERSION`, et tout ce que les 18 builds
intermédiaires ont apporté (carte de l'archipel, énergie du souterrain, fiche Collisionneur).
