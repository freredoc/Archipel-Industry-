# RAPPORT — P3 : la barre ACTIONS passe sous la barre de navigation

Base : build 423 / Alpha 19.0 (lot P1+P2, PR #398). Livré : **build 424 / Alpha 19.1**.
`SAVE_VERSION` **INCHANGÉ (31)**. **Aucun changement de jeu** : ce lot ne touche que `android/`
et la CI ; le HTML ne bouge que par son bump de version.

---

## 1. Étape 1 — MESURER : ce que je peux dire, et ce que je ne peux pas

### ⚠ Les valeurs d'insets ne sont PAS dans ce rapport, et c'est un écart au brief

Le brief exige : « Écrire dans le RAPPORT les valeurs relevées. Un correctif sans ces chiffres
n'est pas recevable. » **Je ne peux pas les produire** : il n'y a aucun appareil ni émulateur
côté développement, et les insets valent toujours 0 dans un environnement headless. Inventer un
tableau de valeurs plausibles serait pire que de ne rien écrire.

Ce qui est livré à la place, et qui respecte l'intention de l'étape 1 **sans coûter un
aller-retour supplémentaire** à Ethan : **le relevé est embarqué dans les APK de test**. Les deux
variantes affichent à l'écran, et journalisent (`adb logcat -s ArchipelInsets`), à chaque passe
d'insets :

```
P3 <A|B> pass=<n> api=<sdk> consumed=<true|false>
sys  t.. b.. l.. r..        (WindowInsets.Type.systemBars())
nav  t.. b.. l.. r..        (WindowInsets.Type.navigationBars())
cut  t.. b.. l.. r..        (WindowInsets.Type.displayCutout())
old  t.. b.. l.. r..        (getSystemWindowInset*, chemin hérité)
pad  t.. b.. l.. r..        (rembourrage RÉELLEMENT appliqué, après application)
+2.5s pad b.. root <h> web <h> dpi <d>
```

Tout est en **pixels**, comme demandé. Le relevé est posé en haut à gauche (la barre ACTIONS
sous test est en bas, il ne la masque pas) ; **un appui le fait disparaître** pour inspecter
l'écran sans gêne. Il n'existe que dans les APK de test (`-PinsetDiag=true`) : les trois paquets
normaux — publique, dev, magasin — n'en embarquent rien.

**La dernière ligne (`+2.5 s`) est là pour une raison précise** : elle relit le rembourrage bien
après la passe de layout initiale. S'il a été écrasé entre-temps, les deux `pad` diffèrent —
c'est la 4ᵉ cause du brief, et elle ne se voit pas autrement.

### Cause identifiée parmi les quatre : **2 / 3 — les insets sont consommés avant nous**

Faute de mesure, le diagnostic est **déduit**, et il repose sur un point du constat d'Ethan qui
tranche à lui seul :

- le gestionnaire du lot P2 rembourre les **quatre** côtés depuis `systemBars() | displayCutout()` ;
- sur un écran à **perforation** (Galaxy S25 FE), `displayCutout().top` est **non nul** ;
- donc, si le gestionnaire recevait des valeurs réelles, une **bande noire apparaîtrait en haut** ;
- Ethan n'en voit **aucune** — le jeu est en plein écran, exactement comme avant —, **et** le bas
  n'est pas rembourré non plus.

**Ni le haut ni le bas ne bougent.** Ce n'est donc pas « le mauvais côté est traité » (cause 1),
c'est que **rien n'est appliqué** : le gestionnaire reçoit des zéros, ou ne se déclenche pas.
Cela désigne les causes 2 et 3 — une vue parente a consommé les insets avant lui —, ce qui est
cohérent avec deux faits du code du lot P2 :

1. `Window.setDecorFitsSystemWindows(false)` **n'était jamais appelé**. Le `DecorView` traite
   alors les insets « à l'ancienne » et les rend **consommés** à ses enfants.
2. Le gestionnaire était posé sur la **WebView**, c'est-à-dire tout en bas de la hiérarchie —
   la position la plus exposée à une consommation en amont.

Le champ **`consumed=`** du relevé confirmera ou infirmera cette lecture en une seconde : `true`
sur la première passe = un parent a bien consommé.

*(La cause 1 n'est pas totalement exclue : elle deviendrait la bonne si le relevé montrait des
insets réels mais un `pad b0`. Le relevé départage.)*

---

## 2. Étape 2 — les deux variantes

Les deux partagent trois changements de structure, qui sont le correctif probable :

| | lot P2 (KO) | lot P3 |
|---|---|---|
| `setDecorFitsSystemWindows(false)` | jamais appelé | **appelé explicitement** (API 30+) |
| vue portant le gestionnaire | la WebView | une **racine** `FrameLayout` que l'on contrôle, insérée entre `android.R.id.content` et la WebView |
| changement de configuration | rien | `onConfigurationChanged` → `requestApplyInsets()` |

`setDecorFitsSystemWindows(false)` est aussi le **prérequis explicite de la variante B** : sans
lui la WebView n'est pas disposée sous les barres et renseignerait 0, donc B échouerait par
construction — le brief le dit, et c'est vérifié dans le code.

### Variante A — `ArchipelIndustryDev-A.apk`, libellé « Archipel P3-A »

Rembourrage **natif** : le padding est posé sur la racine, puis les insets sont **consommés**
(`WindowInsets.CONSUMED`). La WebView est alors entièrement dans la zone sûre.

- gauche, droite, bas : `systemBars() | displayCutout()` ;
- **haut : `systemBars()` seul — ÉCART VOULU AU BRIEF**, voir §4 ;
- conséquence assumée : `env(safe-area-inset-*)` vaut **0 dans l'APK**, le rembourrage CSS du
  lot A y devient inerte. Il reste actif en web/PWA. **Les deux chemins ne se cumulent jamais.**

### Variante B — `ArchipelIndustryDev-B.apk`, libellé « Archipel P3-B »

**Rien de natif** : aucun padding, aucune consommation. Le gestionnaire ne fait qu'**observer**
(il alimente le relevé) et rend les insets intacts, donc `ViewGroup.dispatchApplyWindowInsets`
les transmet à la WebView, qui doit alors renseigner `env(safe-area-inset-*)` — et le CSS du lot
A fait le travail, comme en web et en PWA.

**B est préférable si elle tient** : un seul mécanisme pour les trois canaux. Avec A, deux
mécanismes produisent le même rembourrage sur deux canaux, et le jour où le HUD gagne un élément,
ajuster le CSS ne suffira plus — l'APK ne suivra pas.

**Mais B est incertaine** : rien ne garantit qu'une WebView Android renseigne
`safe-area-inset-bottom` pour la **barre de navigation** (le cas documenté est l'**encoche**).
C'est exactement ce que le test tranche. Aucune des deux n'est présumée gagnante.

### Comment elles arrivent à Ethan

Artefact **`ArchipelIndustry-P3-INSETS`** du `workflow_dispatch` lancé depuis la branche (les
deux étapes à effet de bord restent gatées sur `main` : rien n'est publié).

⚠ **Les deux gardent l'appId DEV** (`fr.archipel.industry`) : elles **remplacent l'app dev
installée en conservant la sauvegarde**, et elles **se remplacent l'une l'autre** — elles ne
s'installent pas côte à côte. C'est le libellé qui dit laquelle est en place : **« Archipel
P3-A »** ou **« Archipel P3-B »**. Une assertion de CI vérifie que les deux APK portent bien des
libellés différents (deux builds successifs dans le même répertoire de sortie : une erreur de
copie donnerait deux fois la même variante, et le test ne prouverait rien), avec pour
contre-mesure que les paquets normaux, eux, n'ont **pas** de libellé P3.

---

## 3. Étape 3 — tests, À FAIRE PAR ETHAN (laissés OUVERTS, aucun appareil ici)

**Aucun de ces points ne peut être porté au vert sans appareil.** Ils sont écrits pour être
remplis tels quels.

Appareil de référence : **Samsung Galaxy S25 FE**.

| # | Test | Mode de navigation | Attendu | Résultat |
|---|---|---|---|---|
| **T-A** | Installer `ArchipelIndustryDev-A.apk`, ouvrir une partie | **3 boutons** | barre `ACTIONS` entièrement visible, les cinq libellés lisibles, aucun bouton sous les touches système | ☐ **OUVERT** |
| **T-A′** | idem | **gestuelle** | idem | ☐ **OUVERT** |
| **T-B** | Installer `ArchipelIndustryDev-B.apk`, ouvrir une partie | **3 boutons** | idem T-A | ☐ **OUVERT** |
| **T-B′** | idem | **gestuelle** | idem | ☐ **OUVERT** |

⚠ **La contre-mesure gestuelle est obligatoire.** En navigation gestuelle l'inset bas est quasi
nul : une variante cassée peut y **paraître saine**. **Un PASS obtenu uniquement en gestuelle est
un résultat nul.** C'est le mode **3 boutons** qui fait foi.

**Relevés à rapporter** (ils remplacent le §1 manquant — c'est la mesure de l'étape 1, prise sur
appareil) :

| | variante A | variante B |
|---|---|---|
| `consumed=` (1ʳᵉ passe) | | |
| `nav b` en **3 boutons** (px) | | |
| `nav b` en **gestuelle** (px) | | |
| `cut t` (px) | | |
| `pad b` appliqué (px) | | |
| `pad b` au relevé **+2,5 s** (px) | | |

Une capture d'écran de chaque variante avec le relevé visible suffit à remplir tout le tableau.

---

## 4. Écart voulu au brief — le HAUT, et lui seul

Le brief demande, pour la variante A, le rembourrage des **quatre côtés** depuis
`systemBars() | displayCutout()`. **Appliqué à la lettre, le côté haut prendrait la perforation
du S25 FE et ferait apparaître une bande noire** là où le jeu s'affiche aujourd'hui en plein
écran — or le même brief met le haut **hors périmètre** et rapporte qu'Ethan juge le comportement
actuel **voulu** (« la barre d'état n'est pas visible en jeu et ne l'a jamais été »). Suivre la
lettre du brief aurait donc régressé un point qu'il déclare satisfaisant.

**Livré** : le haut vient de `systemBars()` seul — il vaut 0 tant que la barre de statut est
masquée, donc le plein écran est préservé à l'identique. Gauche, droite et bas prennent bien
l'union avec l'encoche, comme demandé (utile en paysage, même si l'orientation reste verrouillée).

**Réversible en un mot** : `t = safe.top` au lieu de `t = bars.top` dans `setUpInsets()`.

Le verrou d'orientation par `android:appCategory="game"` est **conservé** : pas de paysage, et
il n'y a pas été retouché.

---

## 5. Étape 4 — après le verdict (à faire, pas fait)

- La variante retenue est portée sur les **trois paquets** (dev, publique, magasin).
- **La perdante est SUPPRIMÉE**, pas laissée derrière un drapeau. Disparaissent avec elle :
  `-PinsetMode`, `BuildConfig.INSET_MODE`, `-PinsetDiag`, `BuildConfig.INSET_DIAG`, l'afficheur
  de diagnostic et l'étape de CI des APK de test.
- **Si B gagne** : retirer le gestionnaire natif du lot P2/P3 et noter la dette « deux mécanismes
  de rembourrage » comme **annulée**.
- **Si A gagne** : inscrire explicitement dans le commentaire du gestionnaire que le rembourrage
  de l'APK **ne vient PAS du CSS**, et que toute retouche du HUD doit être vérifiée **sur les deux
  chemins** (APK d'un côté, web/PWA de l'autre).

Les commits sont **séparés par variante** : la perdante se retire d'un `revert` propre.

---

## 6. Ce qui a été vérifié ici, et comment

Aucun SDK Android côté développement : **rien de ce lot n'a été compilé ni exécuté**. Le banc
d'essai est le `workflow_dispatch` depuis la branche.

| Contrôle | Résultat |
|---|---|
| `node --check` sur les 7 blocs `<script>` du HTML | **7/7 OK** |
| Équilibrage des accolades/parenthèses de `MainActivity.java` (analyse consciente des chaînes et des commentaires) | **0 anomalie** |
| Aucun résidu de `applyInsetPadding` (méthode remplacée) | **0 occurrence** |
| YAML du workflow analysable, 20 étapes, les 2 étapes à effet de bord toujours gatées `refs/heads/main` | **conforme** |
| Extraction CI de `GAME_BUILD` / `GAME_VERSION` / `GAME_NOTES` par les regex **du workflow** | **424 · Alpha 19.1 · note lisible** |
| Gardes CI du lot 18.4 toujours satisfaites (`DEV_BUILD`, `SELF_UPDATE`, `SUPPORT_URL`, 1 seule occurrence de `ko-fi`) | **1 / 1 / 1 / 1** |
| `GAME_NOTES` sans guillemet droit ni séquence `\u` | **conforme** |
| `GAME_BUILD` libre relevé sur **toutes** les branches distantes (max = 423) | **424 libre** |

⚠ **Ce que ces contrôles ne prouvent pas** : ni que le Java compile (aucun SDK), ni que le
correctif fonctionne. Le premier point tombe au `workflow_dispatch`, le second sur l'appareil
d'Ethan. **Ne pas lire le tableau ci-dessus comme une validation de V3.**

---

## 7. Reste ouvert

- **V3** (edge-to-edge / orientation, lot P2) : toujours **KO**, c'est l'objet de ce lot — il ne
  repassera au vert qu'après T-A ou T-B sur appareil.
- **T4** (boot du paquet magasin : aucune permission, pas de section de soutien, pas de bouton de
  mise à jour, `logcat` sans `SecurityException`) : toujours **non exécuté**, il attend le même
  appareil.
- **Hors périmètre, non traité** : dépôt sur la Play Console, Play App Signing, piste de test
  fermé, fiche magasin ; `version.json` et `index.html` (la PWA reste l'édition publique) ;
  App Store.
