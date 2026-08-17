# RAPPORT — P4 : variante B retenue, A supprimée, barre d'état visible en jeu

**`GAME_BUILD = 425`, `GAME_VERSION = 'Alpha 19.2'`.** `SAVE_VERSION` **INCHANGÉ (31)**.
**Aucun changement de jeu** : ce lot ne touche que `android/` et la CI ; le HTML ne bouge que par
son bump et son commentaire de version.

Base : build 424 / Alpha 19.1 (lot P3). Trois commits, dans l'ordre du brief.

---

## 1. Ce que le test de P3 a tranché

Mesuré par Ethan sur **Galaxy S25 FE, navigation à 3 boutons**, avec `sys t0 b135` et `cut t82`
des deux côtés :

| | rembourrage natif | hauteur racine | hauteur WebView |
|---|---|---|---|
| **A** | `pad t0 b135` | 2340 | 2205 |
| **B** | `pad t0 b0` | 2340 | **2340** |

En **B**, la WebView occupe tout l'écran, **aucun pixel natif n'est rembourré**, et la barre
`ACTIONS` est pourtant dégagée. **Une WebView Android renseigne donc bien
`safe-area-inset-bottom` pour la barre de NAVIGATION**, et pas seulement pour l'encoche — c'était
l'inconnue explicite du brief P3, elle est levée. C'est le CSS `env(safe-area-inset-*)` du lot A
qui opère, seul.

**B retenue (décision d'Ethan).** Un seul mécanisme pour l'APK, le web et la PWA ; la dette
« deux chemins de rembourrage » est **annulée**.

---

## 2. Commit 1 — retenir B, supprimer A

Le rembourrage natif est retiré **entièrement** : pas de drapeau, pas de branche morte, pas de code
commenté. La bascule `-PinsetMode` disparaît avec lui, et la CI ne produit plus qu'**un seul** APK
de test.

⚠ **CE QUI RESTE, ET QU'IL NE FAUT PAS RETIRER EN CROYANT SIMPLIFIER** :
`setDecorFitsSystemWindows(false)` est le **prérequis de B**. Sans lui la WebView n'est pas disposée
sous les barres, elle reçoit des insets nuls, `env(safe-area-inset-*)` y vaut 0 et **plus rien n'est
rembourré, sur les trois paquets à la fois**. C'est écrit en tête de `setUpInsets()`.

`appCategory="game"` et le verrou d'orientation portrait sont **conservés** (pas de paysage).

Le relevé de diagnostic est **conservé jusqu'à la fin du commit 2**, comme demandé, et **étendu** :
il publie désormais `env(safe-area-inset-*)` **tel que la WebView le calcule**, sans quoi la mesure
exigée au §3 serait impossible. Le relevé natif dit ce que la *fenêtre* reçoit ; il ne dit pas ce
que la *page* en fait.

⚠ **DEUX UNITÉS, à ne pas confondre** : `env()` rend des **px CSS**, les insets natifs des **px
physiques**. À `devicePixelRatio = 3`, un inset natif de 82 px se lit ~27,3 px CSS. Le relevé publie
donc les deux lignes (`sa css` puis `sa phy` = css × dpr) pour que la comparaison avec `sys` / `cut`
ait un sens.

---

## 3. Commit 2 — barre d'état visible en jeu

On ne masque plus la barre de statut (`hide(statusBars())` supprimé, ainsi que
`BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE` et le chemin `setSystemUiVisibility`). On force en plus ses
**icônes claires** : le fond du jeu est sombre (`#0E1726`) et certains constructeurs posent le
drapeau « fond clair » par défaut, ce qui donnerait des icônes noires illisibles.

**Aucun rembourrage natif n'est rétabli.** La fenêtre reste edge-to-edge, la barre se dessine
par-dessus le fond, et c'est toujours le CSS qui réserve la bande haute.

### ⚠ Vérifié dans la feuille de style, pas supposé — aucune ligne de HTML/CSS n'a eu à changer

Le brief demandait de vérifier plutôt que de supposer. Relevé sur le fichier :

| Ligne | Règle | Rôle |
|---|---|---|
| 50 | `.hud{padding-top:max(6px,env(safe-area-inset-top))}` | **réserve la bande haute** — suit l'inset tout seul |
| 48-49 | `.hud{padding-left/right:max(12px,env(safe-area-inset-left/right))}` | côtés |
| 1263-64 | `.toolbar-wrap{padding-bottom/left/right:env(safe-area-inset-*)}` | **c'est elle qui dégage la barre ACTIONS** |
| 1592 | `.hud{padding-top:max(3px,env(safe-area-inset-top))}` (paysage court) | idem, variante paysage |

`.hud` est le premier élément du document et le HUD est au-dessus du canvas : la barre d'état se
superpose donc au fond du HUD, pas au contenu du jeu. `viewport-fit=cover` est déjà dans le
`<meta name="viewport">` (ligne 5).

### `safe-area-inset-top` avant / après — ⚠ ÉCART AU BRIEF

Le brief demande la valeur **mesurée** avant et après. Voici où j'en suis, sans arrondir les angles :

| | valeur | statut |
|---|---|---|
| **avant** (plein écran) | **82 px physiques** (`cut t82`, `sys t0`) | **déduit** d'une mesure réelle — le relevé P3 donne les insets natifs reçus par la WebView, dont la seule composante haute est l'encoche |
| **après** (barre visible) | `sys t` = hauteur de la barre d'état, `cut t82` inchangé | **À MESURER — le relevé de l'APK de test le donne, ligne `sa phy t`** |

**Je ne peux pas produire la valeur « après » ici** : aucun appareil ni émulateur côté
développement, et les insets valent toujours 0 en headless. Ce que j'ai fait à la place : ajouter au
relevé la ligne `sa css` / `sa phy`, pour que la mesure revienne **dans le même aller-retour** que
les tests T4-T6. Ethan lit `sa phy t` sur l'APK de test et la valeur est acquise.

**Attendu, à confirmer** : la barre d'état d'un S25 FE occupe approximativement la bande de
l'encoche, donc `safe-area-inset-top` devrait rester **du même ordre** (~82-135 px physiques) — le
vide noir contournant l'objectif devient une barre d'état utile. **Si `sa phy t` dépasse nettement
82, c'est de la hauteur de jeu perdue**, et c'est à Ethan d'arbitrer.

Le bas n'est pas touché.

---

## 4. Commit 3 — retrait de la surimpression de diagnostic

Disparaissent ensemble : l'afficheur, la sonde CSS, le journal `ArchipelInsets`, le drapeau
`-PinsetDiag` et son `buildConfigField`, l'étape de CI produisant l'APK de test.

Et avec eux le **dernier reste du gestionnaire natif** : le listener ne servait plus qu'au relevé
(il n'appliquait rien et ne consommait rien), la racine `FrameLayout` n'existait que pour porter
l'afficheur. La WebView redevient la vue de contenu directe. Il ne reste de `setUpInsets()` que
`setDecorFitsSystemWindows(false)`.

`onConfigurationChanged` demande désormais les insets **à la WebView elle-même** : sans cet appel
elle garderait un `env(safe-area-inset-*)` périmé après un changement de configuration, l'activité
déclarant `configChanges` et n'étant donc pas recréée.

---

## 5. Tests

### T1 — construction des trois paquets · **PASS**

Run CI **553** (`workflow_dispatch` depuis la branche, commit 3). Les trois paquets construits, les
**deux étapes à effet de bord SAUTÉES** (gate `refs/heads/main`) : **rien n'a été publié**.

Run **552** (commits 1+2) également vert : c'est lui qui a produit l'**APK de test**.

### T2 — assertions du lot P2 non retouchées, toujours vertes · **PASS**

Aucune de ces étapes n'a été modifiée par ce lot ; elles passent au run 553 :

| Assertion | Attendu |
|---|---|
| asset extrait **du `.aab`** | `SELF_UPDATE = false`, `ko-fi` = **0** |
| contre-mesure sur l'asset de l'**APK publique** | `SELF_UPDATE = true`, `ko-fi` = **1** |
| paquet magasin | **0** `uses-permission`, non debuggable, `targetSdk 36` |
| contre-mesure permissions | l'APK publique en déclare bien **2** |
| `Assert appIds` | les deux éditions installables côte à côte |

### T3 — plus aucune trace du gestionnaire d'insets natif · **PASS**

`grep` sur `MainActivity.java` au commit 3 :

| Motif | Occurrences |
|---|---|
| `setOnApplyWindowInsetsListener` | **0** |
| `onApplyWindowInsets` | **0** |
| `setPadding` | **0** *(1 mention, dans un commentaire d'avertissement)* |
| `WindowInsets.CONSUMED` | **0** |
| `Type.systemBars` / `Type.navigationBars` / `Type.displayCutout` | **0 / 0 / 0** |
| `android.graphics.Insets` (import) | **0** |
| `INSET_MODE` / `INSET_DIAG` (Java, Gradle, CI) | **0** |

⚠ Une occurrence de `WindowInsets.Type.statusBars()` subsiste : c'est le **`show()`** qui rend la
barre d'état visible (commit 2), **pas un chemin de rembourrage**. La distinction est faite ici pour
qu'elle ne soit pas lue comme un résidu.

Contrôle annexe : plus aucun import inutilisé dans le fichier.

### Sur appareil — **NON EXÉCUTÉS, en attente d'Ethan**

Aucun appareil ici. **Ne jamais les porter au vert sans appareil.**

Artefact **`ArchipelIndustry-P4-DIAG`** du **run 552** → `ArchipelIndustryDev-P4.apk`, libellé
**« Archipel P4 »**. ⚠ Il garde l'**appId dev** : il remplace l'app dev installée **en conservant la
sauvegarde**.

| # | Test | Mode | Attendu | Résultat |
|---|---|---|---|---|
| **T4** | barre `ACTIONS` entièrement visible, cinq libellés lisibles, aucun bouton sous les touches système | **3 boutons** | non-régression du correctif P3 | ☐ **OUVERT** |
| **T4′** | idem — **contre-mesure** | **gestuelle** | idem | ☐ **OUVERT** |
| **T5** | barre d'état visible en jeu (heure, batterie, notifications) ; HUD ni recouvert par elle, ni par le trou de caméra | 3 boutons | | ☐ **OUVERT** |
| **T6** | **celui qui peut faire annuler le commit 2** — partir d'un point dans les ~100 premiers pixels de la **zone de jeu** et glisser vers le bas, **dix fois** ; compter les ouvertures du volet de notifications | 3 boutons | **PASS si le volet ne s'ouvre jamais** | ☐ **OUVERT** |

⚠ **T4 en gestuelle seule ne vaut rien** : l'inset bas y est quasi nul, une variante cassée y paraît
saine. C'est le mode **3 boutons** qui fait foi.

⚠ **T6 se fausse trivialement** : un glissement parti du **milieu** de l'écran n'ouvre jamais le
volet et validerait à vide. **Le geste doit partir du haut de la zone de jeu.**

⚠ **L'afficheur masque le haut du HUD** — donc précisément la zone que T5 juge. Il **s'efface d'un
appui** : lire les chiffres, capturer, puis le faire disparaître avant de juger T5 et T6.

**Relevé à rapporter** (une capture avec l'afficheur visible suffit) :

| Ligne du relevé | Valeur |
|---|---|
| `sa phy t` — **c'est le chiffre attendu au §3** | |
| `sa phy b` | |
| `sa css t` et `dpr` | |
| `sys t` / `sys b` | |
| `cut t` | |
| T6 : nombre d'ouvertures du volet sur 10 glissements | |

**Si T6 échoue**, le commit 2 se retire seul par `revert` et le commit 1 reste — c'est la raison de
la séparation des commits.

---

## 6. Articulation avec la PR #398

**Ce lot est ajouté à la PR #398, sur la même branche `claude/playstore-preparation-g0w8vb`.**
Choix, et non contrainte subie :

- la PR **portait** le défaut d'inset (le rembourrage natif inopérant du P1) ; l'y corriger **le
  retire** au lieu de laisser une PR bancale ouverte en parallèle d'une seconde qui la répare ;
- P1 (API 36), P2 (variante magasin), P3 (diagnostic) et P4 (correctif) forment **une seule chaîne
  de dépendances** : P4 ne se merge pas sans P1, et P1 ne devrait pas se merger sans P4 ;
- la branche est celle qu'impose la consigne de session ; une seconde PR depuis la même branche
  n'existerait pas techniquement.

**#398 devient donc la PR du lot Play complet**, de l'API 36 au correctif d'insets. Son titre et son
corps sont mis à jour. **Elle n'est pas mergée** — c'est la décision d'Ethan.

---

## 7. Écarts au brief, et points en suspens

1. **`safe-area-inset-top` « avant » est DÉDUIT, pas mesuré** (§3). Aucun appareil ici. Il découle
   directement d'une mesure réelle (`cut t82`, `sys t0` du relevé P3), mais c'est une déduction et
   elle est signalée comme telle. La valeur « après » revient avec les tests.
2. **Le relevé de diagnostic a été ÉTENDU** au lieu d'être seulement conservé : sans la sonde
   `env(safe-area-inset-*)`, la mesure exigée au §3 du brief était impossible à prendre, le relevé
   natif ne disant rien de ce que la page fait des insets.
3. **T1/T2/T3 sont vérifiés au commit 3** (branche head) ; l'**APK de test vient du run 552**
   (commits 1+2), puisque le commit 3 retire précisément l'afficheur dont Ethan a besoin. Les deux
   builds ne diffèrent que par ce relevé.
4. **Rien de ce lot n'a été compilé localement** (aucun SDK Android) — c'est la CI qui compile, et
   elle passe. ⚠ **Un APK qui compile, s'assemble et se signe peut parfaitement mal se comporter à
   l'écran** : le run 550 en est l'exemple. Seuls T4-T6 tranchent.
5. **Hors périmètre, non traité** : dépôt sur la Play Console, Play App Signing, piste de test
   fermé, fiche magasin ; **T4 du lot P2** (boot du paquet magasin sans permission) reste lui aussi
   non exécuté, faute d'appareil ; App Store.
