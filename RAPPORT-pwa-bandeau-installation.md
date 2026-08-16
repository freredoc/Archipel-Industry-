# RAPPORT — LOT PWA-1 : bandeau d'installation (web / PWA uniquement)

Brief : `BRIEFlotpwabandeauinstallation.md` · patcheur `patch_pwa_banner.py` · banc `bench_pwa_banner.js`

---

## 1. Version livrée

| | |
|---|---|
| `GAME_BUILD` | **426** |
| `GAME_VERSION` | **'Alpha 19.3'** |
| `SAVE_VERSION` | **31, INCHANGÉ** |
| `GAME_NOTES` | `Version web : une invitation a installer le jeu sur l'ecran d'accueil apparait apres le tutoriel. Rien ne change dans l'application Android.` |

Le brief ne proposait aucun numéro. **425 est le maximum relevé sur les 64 branches distantes**
(`for b in $(git branch -r); do git show "$b:…html" | grep -m1 GAME_BUILD; done`) — pas seulement sur
`main`. 426 était donc libre, re-vérifié juste avant le push.

Une ligne a été ajoutée au bloc de commentaire cumulatif au-dessus de `const GAME_BUILD` (24 lignes,
aucune ligne antérieure effacée).

`SAVE_VERSION` n'est pas concerné : le refus du bandeau vit dans `localStorage` sous
`archipel_pwa_hide`. C'est une **préférence d'appareil**, pas un état de partie — aucun champ ajouté à
la sauvegarde, aucune migration.

---

## 2. Empreintes

```
base    3 713 985 o   sha256 054a5c1a2c1f394accd3681d01d2a121cb895a80cf1371e9c32683b2e786734b
patché  3 722 084 o   sha256 0f163c4264fe55ea80a759a6b219bfd32b1d9960d48b1aeaf02efadfac667890   (patcheur seul)
livré   3 725 662 o   sha256 6eeaad9072172b800fc39e7343d79207ca0035d31dc09b92ac498d0fe9ae97b0
delta   +11 677 o
```

- **La base sur `main` correspond EXACTEMENT à celle déclarée par le brief** (octets et SHA-256) — les
  six ancres se sont donc appliquées sans adaptation.
- **Le fichier « patcheur seul » est conforme au brief au caractère près** (3 722 084 o / `0f163c42…`,
  delta +8 099 o). C'est la vérification qui compte : elle prouve que le patch livré est bien celui
  qui a été pré-compilé et validé.
- Les **+3 578 o** restants sont le bump (bloc de commentaire cumulatif, `GAME_NOTES`) et le
  correctif d'inset du §6.

**Aller-retour** : rejeu du patcheur sur la base propre → **identique octet pour octet** (`cmp`).
**Idempotence** : rejeu sur le fichier patché → `DEJA PATCHE — aucune modification`.

---

## 3. Ancres appliquées

Le patcheur s'arrête (`SystemExit`) si une ancre n'est pas à `count == 1`. Il n'a pas levé.
Contrôle indépendant du contenu **après** application :

| # | Ancre | Contenu vérifié | count |
|---|---|---|---|
| 1 | `.toolbar-wrap{…}` (CSS) | `.pwa-bar{flex-shrink:0;display:flex;` | 1 |
| 2 | `const SUPPORT_URL = …;` | `const PWA_DISMISS_KEY = 'archipel_pwa_hide';` | 1 |
| 2 | " | `const PWA_ELIGIBLE = (() => {` | 1 |
| 2 | " | `window.addEventListener('beforeinstallprompt'` | 1 |
| 3 | `// Hook de test (inoffensif)…` | `function PWAInstallBanner({ onDismiss }) {` | 1 |
| 4 | `const [tutorialStep, …] = useState(-1);` | `const [pwaHidden, setPwaHidden] = useState(false);` | 1 |
| 4 | " | `const dismissPwa = () => {` | 1 |
| 5 | fin du rendu racine | `React.createElement(PWAInstallBanner, { onDismiss: dismissPwa })` | 1 |
| 6 | IIFE i18n 14.18 | `/* PWA-1 — bandeau d'installation */` | 1 |
| 6 | " | `/* 14.18 */` **conservée** | 1 |

---

## 4. `node --check`

**7 blocs `<script>`, 7 OK**, rejoué **après** le bump et **après** le correctif d'inset.

Et sur les **trois variantes que la CI dérive** (simulation locale des `sed`/`grep` d'`android.yml`,
lignes 74-110) : `game-public.html` **7/7**, `game-dev.html` **7/7**, `game-store.html` **7/7**.

---

## 5. Banc — 11 tests

### 5.1 Écart de pilote (assumé, motivé)

Le banc du brief utilise `puppeteer-core` + `@sparticuz/chromium`. **Ces deux paquets sont absents de
l'image** ; ce qui est disponible est `playwright-core` + le Chromium de l'image
(`/opt/pw-browsers/chromium-1194/chrome-linux/chrome`), et `playwright install` est proscrit par les
conventions du projet (la révision attendue par le paquet n'est pas celle de l'image).

Le banc a donc été **porté sur playwright-core, PILOTE SEUL** : `launch({executablePath})`,
`newContext({userAgent, viewport, isMobile, hasTouch})` au lieu de `setUserAgent`/`setViewport`,
`addInitScript` au lieu de `evaluateOnNewDocument`, `waitUntil:'networkidle'` au lieu de
`'networkidle0'`. **Les 11 montages, leurs mutations et leurs assertions sont repris à l'identique**,
`page.mouse.click` compris.

⚠ **Une conséquence à connaître** : playwright isole `localStorage` **par contexte**, là où puppeteer
le partageait entre pages de même origine. La purge par `addInitScript` et le drapeau `keepStore` du
banc d'origine sont **conservés tels quels** — ils restent nécessaires pour T6/T7, qui rechargent la
page dans le MÊME contexte (la sauvegarde doit y survivre) ; ailleurs l'isolation les rend seulement
redondants. Aucun test n'y gagne ni n'y perd sa signification.

Le banc sert bien **en HTTP sur `127.0.0.1:8731`**, jamais en `file://` (le piège documenté par le
brief : `PWA_ELIGIBLE` exige `http(s)`, un banc en `file://` rendrait six tests non falsifiables), et
lit `PWA_ELIGIBLE` **par identifiant nu** (c'est un `const` de portée lexicale, absent de `window`).

### 5.2 Résultats — 11 / 11 PASS

| Test | Montage réel | Mesure | Verdict |
|---|---|---|---|
| T1 | démarrage UA Android, écoute `pageerror` | `errs=0 eligible=true` | PASS |
| T2 | web nu / `window.ArchipelNative` injecté par `addInitScript` | `web=true apk=false` | PASS |
| T3 | `matchMedia('…standalone…')` stubé **avant** chargement | `eligible=false` | PASS |
| T3b | `navigator.standalone` redéfini sur UA iPhone | `eligible=false` | PASS |
| T4 | UA Android + `Instagram 300.0` | `eligible=false` | PASS |
| T4b | UA bureau X11 | `eligible=false` | PASS |
| T5 | éligible **ET** tutoriel actif | `eligible=true tutorial.active=true pwa-bar=0` | PASS |
| T6 | **positif** : `tutorial.step=999` → sondage → rechargement | `tutorial.active=false pwa-bar=1 err=0 txt="Install the game from your browser menu: …"` | PASS |
| T7 | **clic réel** `page.mouse.click` aux coordonnées de `.pwa-bar-x` | `avant=1 apres=0 localStorage=1 err=0` | PASS |
| T8 | `archipel_pwa_hide='1'` posé avant chargement | `eligible=false` | PASS |
| T9 | présence des clés dans `I18N.locales.{en,es,de}.ui` | `{"en":true,"es":true,"de":true}` | PASS |

**Suite rejouée 2 fois sans flottement** (11/11 les deux fois), plus une 3ᵉ fois après le correctif
d'inset du §6.

⚠ Le texte relevé par T6 est **en anglais** : le navigateur de test est en locale EN, `I18N.t` rend
donc l'anglais. Ce n'est pas un défaut — c'est au contraire la preuve que le chemin i18n fonctionne.
Et T9 asserte la **présence de la clé**, jamais `t(k) !== k` : « Installer » se traduit légitimement
par lui-même en français.

### 5.3 Contre-test — 0 / 11 sur la base non patchée

```
0/11 PASS   (T1..T9 : eligible=ABSENT, pwa-bar=0, i18n {en:false,es:false,de:false})
```

La suite échoue **intégralement** sans le patch : elle mesure donc bien quelque chose.

---

## 6. ÉCART AU BRIEF — correctif d'inset bas, MESURÉ

**C'est le point le plus important de ce rapport.**

Le brief classe le rendu sur appareil en « non couvert », en supposant le bandeau posé **au-dessus**
de la barre d'outils (le commentaire du patcheur l'écrit : *« il se pose AU-DESSUS de la barre
d'outils »*, et le §7 parle du *« rendu du bandeau au-dessus de `.toolbar-wrap` »*).

**Mesuré au DOM : c'est faux.** Le montage du §5 place le bandeau en **DERNIER enfant de `.app`** :

```
.app > [hud-stack, tuto-banner, tut-halo, stage sel, toolbar-wrap, pwa-bar]
                                                       index 4       index 5
.pwa-bar dans .toolbar-wrap : false
```

Le bandeau est donc l'élément **le plus bas de l'écran** — et son CSS ne portait
`env(safe-area-inset-bottom)` **nulle part** (`padding-bottom` calculé : `10px` ; une seule occurrence
de `env(safe-area-inset-bottom)` dans tout le fichier, celle de `.toolbar-wrap`).

**Conséquence chiffrée**, simulée en remplaçant `env(safe-area-inset-bottom)` par **45 px CSS**
(= les 135 px physiques relevés par Ethan sur le S25 FE en 3 boutons, ÷ dpr 3) :

| | avant correctif | après correctif |
|---|---|---|
| barre d'outils | 717 → 841 (**124 px**, gonflée par un inset devenu inutile) | 717 → **796 (79 px, sa taille naturelle)** |
| bandeau | 841 → 915 | 796 → 915 |
| bas de la zone utile | 870 | 870 |
| **hauteur du bandeau sous la barre de navigation** | **45 px sur 74, soit 61 %** | 45 px, mais **de rembourrage** |
| **centre de la croix de fermeture** | **y = 879 → 9 px SOUS la barre de nav, INTOUCHABLE** | **y = 834, bas à 849 → entièrement au-dessus des 870** |

C'est **exactement le défaut que les lots P3 et P4 viennent de fermer**, réintroduit sur le bandeau.
Le livrer en l'état aurait donné un bandeau dont on ne peut pas se débarrasser sur l'appareil même
qui a servi à valider P4.

**Correctif appliqué, CSS seul, dans l'esprit de P4 (« c'est le CSS qui réserve la bande, et lui
seul ») :**

```css
.pwa-bar{ … padding-bottom:calc(10px + env(safe-area-inset-bottom)); }
.toolbar-wrap:has(+ .pwa-bar){padding-bottom:0;}
```

La seconde règle est indispensable : sans elle la bande est réservée **deux fois** et le trou de 45 px
se contente de remonter d'un cran (c'est le `124 px` de la colonne « avant »).

⚠ **Limite assumée de `:has()`** (Chrome 105+ / Safari 15.4+) : un navigateur plus ancien retombe sur
un espace mort de la hauteur de l'inset entre la barre d'outils et le bandeau. **Dégradation
cosmétique, jamais fonctionnelle** — le bandeau et sa croix restent atteignables dans tous les cas.
Le bandeau ne s'affiche de toute façon que dans un navigateur mobile récent (il faut
`beforeinstallprompt` ou l'« Ajouter à l'écran d'accueil » d'iOS).

Les deux commentaires expliquant *pourquoi* ces lignes existent sont posés dans le CSS, pour qu'un
futur lot ne les retire pas en croyant simplifier.

---

## 7. Invariants CI — aucune ligne de CI ne bouge

Vérifiés sur le fichier **livré**, puis en **rejouant localement les `sed`/`grep` d'`android.yml`** :

| Contrôle | Attendu | Mesuré |
|---|---|---|
| `^const SELF_UPDATE = true;$` | 1 | 1 |
| `^const SUPPORT_URL = 'https://ko-fi.com/freredoc';$` | 1 | 1 |
| `^const DEV_BUILD = false;$` | 1 | 1 |
| occurrences `ko-fi` (la CI en exige exactement 1) | 1 | 1 |
| garde d'entrée `game-dev` / `game-public` | OK | OK |
| gardes d'entrée `game-store` (SELF_UPDATE, SUPPORT_URL) | OK | OK |
| `ko-fi` dans `game-store.html` | 0 | 0 |
| `const SELF_UPDATE = true;` dans `game-store.html` | 0 | 0 |
| contre-garde : `game-public.html` conserve `SELF_UPDATE`/`ko-fi` | 1 / 1 | 1 / 1 |

Le nouveau code se situe **après** la ligne `SUPPORT_URL`, que le `sed` du magasin réécrit seule : la
dérivation magasin reste intacte, et la variante magasin compile (7/7).

⚠ Le bandeau **existe** dans les trois paquets Android (le code n'est pas retiré), mais il y est
**inatteignable** : `window.ArchipelNative` y est injecté inconditionnellement par
`addJavascriptInterface`, donc `PWA_ELIGIBLE` vaut `false`. C'est l'arbitrage déjà inscrit ailleurs
dans le projet — *un verrou plutôt qu'une ablation, donc un seul chemin de code*. T2 le prouve dans
les deux sens.

---

## 8. Ce qui reste NON couvert

- **Aucun test sur appareil réel.** Le §6 réduit fortement le risque (les chiffres sont ceux du relevé
  P3 d'Ethan), mais il reste une **simulation** : `env(safe-area-inset-bottom)` y est remplacé par une
  constante, la vraie valeur vient de la WebView. À confirmer sur le S25 FE — et **en 3 boutons**, pas
  en gestuelle : en gestuelle l'inset bas est quasi nul et un bandeau mal rembourré y paraîtrait sain.
- **Le vrai `beforeinstallprompt` n'est jamais émis en headless.** Le chemin `canPrompt === true` (le
  bouton natif « Installer » d'Android) n'est donc **pas couvert par la suite** ; seuls les chemins iOS
  et générique le sont. Mesuré : `hasBtn:false`, `hasX:true` — l'absence dégrade proprement vers le
  texte générique, ce qui est le comportement voulu.
- **Le texte français est la source**, les trois autres langues suivent par les tables ; elles n'ont
  pas été relues par un locuteur.
- **T4 / T5 / T6 du lot P4** (barre ACTIONS dégagée, barre d'état visible, volet de notifications) et
  **T4 du lot P2** (démarrage du paquet magasin) restent eux aussi à exécuter sur appareil — ils sont
  antérieurs à ce lot et ne sont pas rouverts par lui.

---

## 9. Livraison

- Branche `claude/playstore-preparation-g0w8vb`, **repartie de `origin/main`** (la PR #398 étant
  fusionnée, l'historique déjà mergé n'a pas été empilé).
- **PR ouverte, NON fusionnée** — le merge appartient à Ethan : c'est lui qui déclenche `android.yml`,
  donc la republication de l'APK, d'`index.html` et de `version.json`.
