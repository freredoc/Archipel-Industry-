# RAPPORT — Lot SILENCIEUX

Quatre défaillances qui échouaient **sans bruit** : A1 (Android) + M1 / M2 / M4 (CI).
Base : `main` @ `7385eb0` (build 432 / Alpha 19.9). Branche : `claude/code-audit-qbbdio`.

## 1. Version produite

| | |
|---|---|
| `GAME_BUILD` | **433** (432 → 433) |
| `GAME_VERSION` | **Alpha 20.0** |
| `SAVE_VERSION` | **31 — INCHANGÉ** |

Numéro relevé sur **toutes les branches distantes** : maximum 432, donc 433 libre.
Étiquette : convention x.9 → (x+1).0 du projet (précédent vérifié : 15.9 → 16.0 au build 393).

**Aucune ligne de jeu ne change** : le fichier de jeu ne porte que son bump et l'entrée du bloc
cumulatif. Le bump est pourtant **fonctionnel, pas conventionnel** — A1 modifie le comportement de
l'APK, et sans nouveau numéro `version.json` reste identique : l'auto-mise à jour ne proposerait
jamais l'APK corrigé aux éditions publique et dev. `GAME_NOTES` reste honnête (« Maintenance
interne… Aucun changement visible en jeu. »).

## 2. Ancres appliquées — comptes vérifiés avant application

| Point | Ancre | Attendu | Mesuré |
|---|---|---|---|
| **A1** | corps complet de `openExternally`, Javadoc incluse | 1 | **1** |
| **M1** | `  cancel-in-progress: true` | 1 | **1** |
| **M2** | 6 formes distinctes | 3 / 3 / 2 / 1 / 1 / 1 | **conformes** (11 lignes) |
| **M4** | `NOTES="${NOTES:-$(git log -1 --pretty=%s)}"` | 1 | **1** |

Contrôles post-patch : `grep -oP "const GAME_` non ancré → **0** ; motifs ancrés `^const` →
**11** ; forme `\\K` divergente de l'étape Sync PWA → **0** (normalisée) ; `import
android.widget.Toast;` → 1. **Idempotence** : patcher rejoué → « A1 deja applique | M1/M2/M4 deja
appliques », SHA identiques.

## 3. Empreintes — ré-extraites des fichiers, jamais transcrites

**Avant bump** — comparaison directe avec le §7 du brief :

| Fichier | Attendu (brief) | Mesuré | Delta |
|---|---|---|---|
| `MainActivity.java` | `8c06e9af…45c2` | `8c06e9af190edcfb9b016749a5f668ae91d0db235d0d717d39f3974d698a45c2` ✅ | **+711 o** (exact) |
| `.github/workflows/android.yml` | `b0afe004…a9ba` | `b0afe0044d71b88586bfdc0f40adb17a1c989055dc11e96ffb6c39c8611aa9ba` ✅ | **+1187 o** (exact) |

> Les **deux** SHA-256 concordent → patches **byte-identiques** à ceux du rédacteur.

**Après bump** : les deux SHA ci-dessus sont **inchangés** (le bump ne touche que le fichier de
jeu, ce qui le confirme). Fichier de jeu : `cf83b0bfe90e6844dd0fce5790bdc66d32f77448b328b9c0aa434da01f370c68`,
3 777 542 o, accolades `<style>` **961/961** (inchangées), blocs `^<script>` **7**.

## 4. Suite de validation

Montages réellement exécutés. Les trois premiers ne valent que par leur contre-épreuve.

### T1 — ancrage falsifiable · **PASS**

Copie du fichier de jeu **réel** (build 432) avec `const GAME_BUILD = 999;` inséré **en
commentaire, juste avant** la vraie constante, puis les deux motifs lancés dessus :

| Motif | Extrait |
|---|---|
| non ancré (celui d'avant le patch) | **999** ← aurait été publié en silence |
| ancré `^const` (celui du patch) | **432** ✅ |

Le piège est donc **réel** et l'ancrage le ferme.

### T2 — garde `GAME_NOTES`, trois cas · **PASS**

Garde du workflow rejouée **telle quelle**, `if` de présence inclus :

| Fichier forgé | Extraction | Garde |
|---|---|---|
| note saine | `note saine sans guillemet.` | **passe** |
| note avec `"guillemet"` droit | `une note avec un ` ← **tronquée** | **BLOQUE (exit 1)** |
| constante absente | *(vide)* | **non applicable** — repli sur le sujet de commit intact |

Le troisième cas justifie l'enveloppe `if` : une garde inconditionnelle casserait le repli
documenté pour un fichier de jeu ancien.

### T3 · **PASS** — `grep -c 'grep -oP "const GAME_'` sur le workflow patché = **0**.

### T4 · **PASS** — `yaml.safe_load` OK, `jobs: ['build']`, 17 étapes,
`concurrency.cancel-in-progress` résolu à `${{ github.ref != 'refs/heads/main' }}`,
`group` à `android-apk-${{ github.ref }}`.

### T5 — compilation Android réelle des 3 paquets

**Pas de SDK Android dans l'environnement** (`sdkmanager` absent) : la compilation ne peut pas
être faite localement. Deux choses ont été faites à la place :

1. **Contrôle syntaxique `javac`, avec contre-épreuve** (`gradle` et `javac` sont présents) : le
   fichier patché ne produit **0 erreur de syntaxe** (uniquement des `package android.* does not
   exist` / `cannot find symbol`, attendus sans SDK) ; le **même fichier saboté** d'une accolade
   produit `error: illegal start of expression`. La méthode détecte donc bien ce qu'elle prétend
   détecter. Également : délimiteurs équilibrés hors chaînes et commentaires (parenthèses 0,
   crochets 0, accolades 0, état final `code`).
2. **`workflow_dispatch` lancé sur la branche** — protocole du projet (runs 550-553) : les deux
   étapes à effet de bord sont gatées sur `refs/heads/main`, donc **rien n'est publié**. Résultat
   consigné au §6.

### T6 / T7 — **NON EXÉCUTÉS, ne pas porter au vert**

Les deux exigent un **appareil**. T7 est le seul test qui distingue « le lien s'ouvre » (vrai
avant comme après le patch) de « le repli dans la WebView a disparu » : il faut **désactiver le
navigateur système**, rouvrir le lien Discord des Options, et constater un **toast sans
navigation**. Une inspection de code n'est pas une exécution — c'est dit franchement ici.

### T8 — gardes CI rejouées **après** rédaction des commentaires du lot · **PASS**

| Garde | Attendu | Mesuré |
|---|---|---|
| `ko-fi` (publique / magasin après sed) | 1 / 0 | **1 / 0** |
| `const SELF_UPDATE = true;` (publique / magasin) | 1 / 0 | **1 / 0** |
| `^const DEV_BUILD = false;$` | 1 | **1** |
| `^const SUPPORT_URL = 'https://ko-fi…';$` | 1 | **1** |

Extractions par les **motifs ancrés du lot** : `GAME_BUILD → 433`, `GAME_VERSION → Alpha 20.0`,
`GAME_NOTES → …` complet ; garde M4 sur le fichier réel : **passe**. Chacun des trois motifs
`^const …` sort à **1 occurrence**.

### Contrôles complémentaires

- **`node --check` 7/7 sur les 3 variantes CI** (`game-public` / `game-dev` / `game-store`,
  produites par les vrais `sed` du workflow).
- **Boot réel** du jeu bumpé (Chromium, HTTP réel) : build 433, canvas peint, `tickErrors` vide,
  **0 `pageerror`** (seule « erreur » : le 404 favicon du serveur de test, hors jeu).

## 5. Écarts au brief

1. **Aucun écart de contenu.** Patcher exécuté **tel quel**, les deux SHA-256 concordent, deltas
   exacts. Rien n'a été adapté ni recopié à la main.
2. **Étiquette de version** : le brief n'en propose aucune ; **Alpha 20.0** retenue par la
   convention x.9 → (x+1).0 du projet, et **433** après relevé sur toutes les branches.
3. **T5 dédoublé** (ci-dessus) faute de SDK local : contrôle syntaxique falsifiable *plus*
   compilation réelle déportée en CI. Le brief demandait la compilation seule.
4. **T6/T7 non exécutés** et **non déclarés PASS**, conformément à l'instruction du brief.

## 6. Résultat de la compilation CI (T5) — **PASS**

**Run 566** (`workflow_dispatch` sur la branche, head `2f1c515`) : **succès complet**, 2 min 24 s.

| Étape | Conclusion |
|---|---|
| Prepare game files (public + dev + magasin) | **success** — les gardes CI passent en conditions réelles, y compris les motifs nouvellement ancrés |
| Build PUBLIC APK | **success** |
| Build DEV APK | **success** |
| Build STORE bundle + APK de contrôle | **success** |
| Assert store package (paquet, targetSdk, permissions, debuggable) | **success** |
| Assert appIds (2 éditions installables côte à côte) | **success** |
| Show signing certificate (clé stable) | **success** |
| Upload APK artifacts / store artifacts | **success** |
| **Publish to « apk-latest » release** | **SKIPPED** |
| **Sync version.json** | **SKIPPED** |

Les **trois paquets compilent** avec le Java patché — c'est le seul risque levable sans appareil, et
il est levé. Les **deux étapes à effet de bord sont bien sautées** (gate `refs/heads/main`) :
**rien n'a été publié**, la release et le fichier de version en ligne sont intacts.

> Le `cancel-in-progress` modifié n'a pas empêché ce run de tourner : sur une branche l'expression
> vaut `true`, comportement inchangé — c'est sur `main` seulement qu'il devient `false`.

## 7. Points ouverts

1. **T7 reste à faire sur appareil**, avant la prochaine soumission magasin. Rappel du brief :
   le `.aab` déjà soumis au test fermé est le **430**, antérieur au lien Discord — le facteur
   aggravant n'existe qu'à partir du prochain paquet.
2. **M1 n'a pas de test** et n'en aura pas : le reproduire demanderait deux merges à quelques
   secondes d'intervalle sur `main`, avec une fenêtre de quelques secondes à toucher, et un échec
   détruirait précisément la release qu'on protège. La revue de l'expression et T4 en tiennent lieu.
3. **M3 écarté délibérément** (compteurs `grep -c` non ancrés) : sa direction d'échec est
   l'inverse des autres — un motif surveillé qui apparaît ailleurs fait échouer **bruyamment** le
   build. C'est une gêne, pas un risque.
4. **Test fermé Play Store** : ce lot change la coquille Android, donc le `.aab` produit par la CI
   **changera**. Il ne doit **pas** être soumis pendant la fenêtre de 14 jours.
5. Restent ouverts de l'audit 431 : A2 (receiver pré-33), A4 (assainissement de `filename`),
   J1/J2 (code mort dont `makeIcon`), README android et `_config.yml` périmés, `node_modules`
   commité.
