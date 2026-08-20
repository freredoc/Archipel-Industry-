# Archipel Industry — application Android

Coquille **WebView** minimale qui embarque le jeu (`Archipel_industry_alpha-7.html`)
comme asset local. L'app fonctionne **100 % hors-ligne** : tout le code (React, logique,
styles) est déjà inline dans le HTML, aucune connexion réseau n'est requise. Les
sauvegardes (localStorage) persistent normalement.

## Récupérer l'APK (le plus simple)

L'APK est construit automatiquement par GitHub Actions et publié dans la release
**`apk-latest`** :

➡️ Onglet **Releases** du dépôt → `apk-latest` → télécharger **`ArchipelIndustry.apk`**
depuis le téléphone, l'ouvrir, autoriser l'installation, lancer le jeu.

Pour (re)lancer une construction manuellement : onglet **Actions** → *Build Android APK*
→ **Run workflow**. Chaque modification du HTML poussée sur `main` régénère l'APK.

## Construire en local (facultatif)

Nécessite le **SDK Android** (platform 36, build-tools 36.0.0) et un **JDK 17**
— ce sont les versions que la CI installe (`android.yml`, étape SDK).

```bash
# depuis la racine du dépôt : copier le jeu dans les assets de l'app
cp Archipel_industry_alpha-7.html android/app/src/main/assets/index.html

cd android
./gradlew assembleDebug
# APK généré : app/build/outputs/apk/debug/app-debug.apk
```

> L'asset `app/src/main/assets/index.html` est ignoré par git (source unique = le HTML
> à la racine). Le workflow CI et la commande ci-dessus le recopient automatiquement.

## Détails techniques

| Élément        | Valeur                          |
|----------------|---------------------------------|
| Paquets        | `fr.archipel.industry` (publique), `.dev`, `.store` |
| minSdk         | 26 (Android 8.0)                |
| targetSdk / compileSdk | 36 (Android 16)         |
| AGP / Gradle   | 8.13.0 / 8.13                   |
| Permissions (publique, dev) | INTERNET + REQUEST_INSTALL_PACKAGES |
| Permissions (magasin)       | **aucune** — assertion CI bloquante |
| Orientation    | portrait                        |

## Version & mises à jour

Le jeu affiche sa version dans **Options** (ex. « Alpha 7 · build 7 ») avec un bouton
**Vérifier les mises à jour**. Ce bouton lit `version.json` à la racine du dépôt
(`raw.githubusercontent.com/.../main/version.json`) et compare le champ `build` à celui
embarqué dans le jeu (`GAME_BUILD`). Si la version en ligne est plus récente, un lien de
téléchargement de l'APK s'affiche (ouvert dans le navigateur système).

`version.json` est **régénéré automatiquement par la CI** à partir de `GAME_BUILD` /
`GAME_VERSION` du HTML — il suffit donc, pour publier une mise à jour, d'incrémenter
`GAME_BUILD` (et `GAME_VERSION`) dans `Archipel_industry_alpha-7.html` et de pousser sur
`main`. La CI reconstruit l'APK, met à jour la release `apk-latest`, et synchronise
`version.json`.

Les APK publiés dans `apk-latest` (publique et dev) sont des builds **debug** (`assembleDebug`),
suffisants pour une installation par sideload. ⚠ Point connu et NON tranché (A5 de l'audit du
build 431) : un build debug porte `debuggable=true`, qui est donc diffusé. La variante
**magasin** est distincte et n'est pas concernée : elle sort en `bundleRelease` (`.aab`,
applicationId `fr.archipel.industry.store`) et passe par Play App Signing.
