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

Nécessite le **SDK Android** (platform 34, build-tools 34.0.0) et un **JDK 17**.

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
| Package        | `fr.archipel.industry`          |
| minSdk         | 26 (Android 8.0)                |
| targetSdk / compileSdk | 34                      |
| AGP / Gradle   | 8.5.2 / 8.7                     |
| Permissions    | aucune (hors-ligne)             |
| Orientation    | portrait                        |

L'APK publié est un build **debug** (signé avec le keystore de debug), suffisant pour
une installation perso par sideload. Pour une distribution sur le Play Store, il faudrait
un build **release** signé avec votre propre keystore.
