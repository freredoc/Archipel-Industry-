# RAPPORT — LOT DISCORD

Brief : `BRIEFlotdiscord.md` · patcher fourni pré-exécuté : `patch_discord.py`
Branche : `claude/discord-vapsvx` · base : `main` @ `eda73b0`

---

## 1. Version réellement produite

| | |
|---|---|
| `GAME_BUILD` | **431** (430 → 431) |
| `GAME_VERSION` | **Alpha 19.8** (19.7 → 19.8) |
| `SAVE_VERSION` | **31 — INCHANGÉ** (aucun champ de partie) |
| `GAME_NOTES` | réécrit (UTF-8 littéral, aucun `"`, aucun `\u`) |

⚠ **Contrôle de collision de numéro fait sur les 67 branches distantes**, pas seulement sur
`main` : maximum relevé = **430** (`main`, `move-reports-folder`, `move-sprites-i18n-folders`,
`petit-bug-fix`) → **431 libre**. Re-vérifié juste avant le push.

## 2. Ancres appliquées — 6/6 à `count == 1`

| Ancre | Longueur | count |
|---|---|---|
| `P1/SUPPORT_URL` | 49 | 1 |
| `P2/renderOptions` | 112 | 1 |
| `P3/i18n-fr` | 55 | 1 |
| `P3/i18n-en` | 47 | 1 |
| `P3/i18n-es` | 52 | 1 |
| `P3/i18n-de` | 49 | 1 |

Passage à blanc effectué **avant** d'écrire dans le dépôt (sortie en scratchpad).
**Idempotence-hostile vérifiée** : rejeu du patcher sur le fichier patché →
`ANCRE P2/renderOptions : 0 occurrences (attendu 1)`, sortie en erreur, aucun doublement.

Le mot `discord` était **absent de la base** (0 occurrence, insensible à la casse) → aucune
collision avec un `grep` de CI.

## 3. SHA-256 des blocs — ré-extraits du fichier livré

**Base d'entrée EXACTE** : 3 759 910 o, 7 blocs `<script>` réels — conforme au brief.

### Avant bump (comparaison au tableau §4 du brief)

**Cas rare : les 7 SHA de bloc ET le SHA du fichier complet sont CONFORMES au brief**
(`0063b4cb5b5c2674dfd8dfb03537ab9c98e4f9413c798234376c922b053e3dc5`, **+2 306 o au byte
près**) → le patch appliqué ici est **byte-identique** à celui du rédacteur.

### Après bump (fichier livré)

| Bloc | Octets | SHA-256 | État |
|---|---|---|---|
| 1 | 418 | `a50c1c4e7f4a304c650c0cfa7e06c4ffdfb0d87510d83738114d4dd5a1641628` | identique |
| 2 | 4 397 | `8fbb22187703339c146b2f82badd8701d199128cd3d9dd8673d213c8616ca541` | identique |
| 3 | 10 751 | `d949f1c3687aedadcedac85261865f29b17cd273997e7f6b2bfc53b2f9d4c4dd` | identique |
| 4 | 131 835 | `35f4f974f4b2bcd44da73963347f8952e341f83909e4498227d4e26b98f66f0d` | identique |
| 5 | 1 113 969 | `1be53ce44e7be14fb81bd92e6a338cba274304f38c6077061fd3e33232cc2651` | identique |
| 6 | 437 335 | `c8d52d2d9dc19b5dbded5e6960cc30d80c0e27ea1a3a2f8740456001190ea265` | **modifié** (+563 = i18n) — **conforme au brief** |
| 7 | 1 793 440 | `a4663fee6ac9a3f2b54c26b7d1009bba34edc4241c3114b410a89e682d814414` | **modifié** — **diverge du brief, ATTENDU** |

⚠ **Le bloc 7 diverge du tableau §4 et c'est normal** : `GAME_BUILD`, `GAME_VERSION` et
`GAME_NOTES` y sont déclarés (lignes 10111-10121), et le commentaire cumulatif du lot y est
ajouté. Son SHA avant bump valait bien `51570f8e…`, celui du brief. Le SHA du fichier complet
livré est `0ff5f5d37a8a13aafcde916225fc6a2665a4d34f4b7835b899ab9a4ca3f1cc23`.

## 4. Delta d'octets

| | |
|---|---|
| Patch seul (avant bump) | 3 759 910 → 3 762 216 = **+2 306 o EXACT** (valeur du brief) |
| Fichier livré (avec bump + commentaire cumulatif) | 3 759 910 → **3 765 541** = **+5 631 o** |

Les 3 325 octets supplémentaires sont le bloc de commentaire cumulatif du lot (exigé par les
conventions du projet), le bump et le nouveau `GAME_NOTES`.

## 5. Tests T1 → T6

Setup commun : serveur **HTTP** local (`http://127.0.0.1:8099`), jamais `file://` ;
`localStorage.clear()` + `archipel_lang` posés dans `addInitScript` ; astuces fermées par
**clic sur `.tip-ok`** (jamais `.remove()`) ; bouton Options cliqué en boucle jusqu'à
**assertion de l'état atteint** (`.slot-list` présent) plutôt qu'un nombre de clics fixe —
`useGhostGuard` avale le premier. Viewport 420×900, DPR 3.
⚠ `sw.js` est **servi avec le bon MIME** par le serveur de test → le faux positif
« unsupported MIME type » signalé au §5 du brief ne s'est pas produit.

| # | Résultat | Setup effectif et mesures |
|---|---|---|
| **T1** | **PASS 7/7 ×3** | Extraction **séquentielle** des blocs (repart après chaque `</script>` → immunisée aux `<script>` en commentaire/chaîne), avec **refus de conclure si le compte ≠ 7**. `node --check` joué sur les **3 variantes CI** : `game-public` 7/7, `game-dev` 7/7, `game-store` 7/7 — **après** l'écriture de mes commentaires. |
| **T2** | **PASS 6/6** | Boot headless, 3 s après `networkidle`. `bodyLen` = **3 477 617** (> 2 000 000), **1 `<canvas>`**, `DISCORD_URL === 'https://discord.gg/kdFKzVymdt'` lu **par identifiant nu**, `SUPPORT_URL` toujours renseigné, **0 `pageerror`**. `GAME_BUILD` 431 / `GAME_VERSION` Alpha 19.8 relevés en jeu. **Contrôle ajouté** : `typeof window.DISCORD_URL === 'undefined'` → la portée lexicale est confirmée, pas supposée. |
| **T3** | **PASS 10/10** | `eval` du **bloc 6 seul** sous Node (`vm`) avec stub `localStorage`/`navigator`. Assertions par **`hasOwnProperty` + valeur non vide**, jamais `t(k) !== k`. 8/8 entrées présentes : fr « Rejoindre le Discord » · en « Join the Discord » · es « Unirse al Discord » · de « Discord beitreten » (+ les 4 descriptions). Témoins de non-régression : `de/"Vérifier les mises à jour"` → `"Nach Updates suchen"`, `es/" · build "` → `" · build "`. |
| **T4** | **PASS ×4 langues** | Options ouvert pour de vrai en fr/en/es/de. Par langue : `href` **exact**, `target="_blank"`, `rel="noopener"`, libellé traduit (`💬 Rejoindre le Discord` / `Join the Discord` / `Unirse al Discord` / `Discord beitreten`), description traduite dans le `.opt-desc` du **même `.slot-row`**, **ordre DOM `discord-kofi`** (Discord AVANT soutien), largeur **335 px = 335 px** (égale au bouton de soutien), 0 `pageerror`. Capture du panneau contrôlée. |
| **T5** | **PASS 4/4** | Variante servie avec `const DISCORD_URL = '';` : `DISCORD_URL` vide, **0** `a[href*="discord.gg"]`, **et le bouton de soutien TOUJOURS présent (1)** → le contrôle ne verdit pas à vide. Le retrait par CI est donc prouvé fonctionnel sans toucher au jeu. |
| **T6** | **KO comme attendu** | Base **non patchée** (`git show main:…`) servie et Options ouvert : **0** lien Discord, `DISCORD_URL` lève une **`ReferenceError`**, bouton de soutien présent (1) → la suite T4 y échouerait, elle mesure donc quelque chose. |

**Suites rejouées 2 fois, sans flottement** (2ᵉ passe : 30 PASS / 0 KO sur T2/T4 fr+de/T5).

⚠ **Piège de banc payé, et il était déjà au mémo** : ma 1ʳᵉ formulation de T6 assertait une
`ReferenceError` via **`typeof DISCORD_URL`** — or `typeof` sur un symbole non déclaré rend
`"undefined"` **sans lever**, donc l'assertion échouait là où le symbole est bien absent.
Reformulée en **identifiant nu** dans un `try`. Second piège, propre à ce lot : les IIFE
d'augmentation i18n lisent `I18N` en **identifiant nu (global)** alors que le kit pose
`window.I18N` → dans un contexte `vm` il faut poser **`ctx.window = ctx`** (en navigateur
`window === globalThis`), sinon T3 lève `ReferenceError: I18N is not defined`.

## 6. Vérification sur APK dev

**NON EFFECTUÉE** — aucun appareil dans cet environnement. Ce que la lecture du code confirme :
`MainActivity.openExternally()` route bien `http`/`https` vers `ACTION_VIEW`. Ce qui reste à
confirmer par Ethan, **une fois**, sur le S25 FE : que taper le bouton ouvre réellement Discord
(application ou navigateur). Le brief le classe non bloquant pour la PR, et il a raison — mais
c'est un chemin qui n'a **peut-être jamais été emprunté en vrai**, le lien de soutien étant
aujourd'hui le seul lien externe du jeu **et** vidé en variante magasin.

## 7. Écarts au brief

1. **Aucun écart de contenu.** Le patcher a été appliqué verbatim ; les 6 ancres sont sorties à
   `count == 1` sans adaptation, et le résultat est byte-identique à celui du rédacteur.
2. **Numérotation** : le brief n'en propose pas (§7). Retenu **431 / Alpha 19.8**, après relevé
   sur les 67 branches distantes.
3. **T6 reformulé** (`typeof` → identifiant nu) : correction d'une erreur d'assertion de ma part,
   pas d'un écart au brief — le critère du brief (`found: false`) était déjà satisfait.
4. **T4 étendu de 2 contrôles** non demandés : `typeof window.DISCORD_URL` (T2) et présence du
   bouton de soutien dans T5/T6. Les deux rendent les contre-tests non vacuous.

## 8. Points en suspens

- **Ouverture du lien sur appareil** (§6) — à taper une fois dans l'APK dev.
- **Invitation permanente** : le lien `https://discord.gg/kdFKzVymdt` doit être une invitation
  **sans expiration, à usages illimités, émise depuis un salon qui ne sera pas supprimé**. Une
  invitation Discord **meurt avec son salon d'origine**, et un lien mort dans un build hors ligne
  n'est plus corrigeable chez le joueur. Non vérifiable depuis ici — à confirmer par Ethan.
- **`android.yml` volontairement non touché** (§6 du brief) : 0 collision de `grep`, et écrire des
  lignes de CI inertes dans un pipeline qui marche ajoute du risque sans contrepartie. Les
  invariants ont tout de même été **rejoués localement** en simulant les `sed` de la CI :
  `ko-fi` = **1** en publique / **0** en magasin, `SELF_UPDATE = true` = 0 en magasin, `DEV_BUILD`
  correct sur les 2 variantes, et `discord.gg` = 1 dans les deux paquets (voulu).
- **⚠ Rappel de la leçon du run 561 (build 429)** : ne **jamais** écrire le nom du service de
  soutien en clair dans un commentaire — la CI compte `grep -c '<nom>'` avec un motif **non ancré**
  et exige 1 en publique / 0 en magasin. Mes commentaires de ce lot ne le mentionnent nulle part,
  et le compte a été **re-vérifié après leur écriture**, pas avant.
- **Aucun `.aab` ne doit partir au Play Console** (§6) : la fenêtre de 14 jours du test fermé court.
  Ce lot part sur le web et l'APK dev.
- **Panneau Aide & astuces non touché** (hors périmètre, §2 du brief).
