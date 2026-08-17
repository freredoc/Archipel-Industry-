# RAPPORT — lot I5a (chantier i18n) : 56 clés `ui` — port / livraison / transit / énergie

**Livré en `GAME_BUILD = 408` / `GAME_VERSION = 'Alpha 17.5'`.** `SAVE_VERSION` INCHANGÉ (31).

| | |
|---|---|
| Base | **407 / Alpha 17.4** (lot I4, sur la même branche) |
| Fichier livré | SHA-256 `877928be310209d3c78c029ec3a1d0e9100a709b5ffe094f8410afe27fbeac28` |
| Delta | **+26 844 o** pour le patch seul · **+27 929 o** avec bump, commentaire et `GAME_NOTES` |
| Clés | **56 × 3 langues = 168 chaînes**, dont **13 fragments** de concaténation |
| Code de rendu touché | **aucun** — une IIFE en fin de bloc 6 |

## 1. Inventaire re-dérivé indépendamment — conforme

Extraction AST des premiers arguments **littéraux** de `I18N.t` dans le bloc 7, croisée avec les
tables **relevées au runtime** : **972 clés uniques, 395 absentes des 3 tables** — les chiffres du
brief, retrouvés au nombre près sur la base 407. Après retrait des 6 typographiques : **389 clés
réelles**, dont **81 fragments** (86 clés à espace de bord − 5 typographiques). **0 clé
partiellement présente** (aucune n'est traduite dans une langue et pas dans une autre).

L'affectation figée de l'inventaire a été **appliquée telle quelle** et se croise parfaitement :
**56 / 23 / 37 / 27 / 246 = 389**, **0 entrée d'inventaire sans clé extraite**, **0 clé manquante non
affectée**. Aucun classement n'a été recalculé, comme le demande le brief.

## 2. Méthode — les clés ne sont **jamais** retapées

C'est le risque n°1 du brief : une apostrophe droite au lieu d'une typographique ne correspond à
rien, `I18N.t` retombe sur le français et **rien ne le signale**. Le patcheur ne reçoit donc jamais
une clé écrite à la main : il prend la liste **extraite par AST** (`i5_lots.json`) et les traductions
**par index**. Il refuse d'écrire si une traduction est vide ou si une interpolation `{var}` a été
altérée.

IIFE en fin de bloc 6, fusion **par clé avec garde** `if(!L.ui[k])` → rejouable. Non-ASCII en
`\uXXXX`.

## 3. Fragments — la phrase finale fait foi, pas la clé

⚠ **Une espace de bord fait partie de la clé et du rendu — mais la traduction peut légitimement ne
pas avoir la même.** L'espace avant « : » est une **convention française** : `" : une seule par
île"` devient `": only one per island"` / `": nur eines pro Insel"`. Chaque fragment a été traduit
en **relisant son site d'appel**, jamais isolément.

Exemple mesuré (T3) : `"Transit avec l'île "` et `"Transit avec les îles "` sont choisis par un
ternaire selon le nombre de liaisons, puis concaténés à la liste des îles → rendu allemand
**« ↔ Transit mit Insel 2 »** (1 liaison, île 1) et **« ↔ Transit mit den Inseln 2, 4 »**
(2 liaisons, île 3). C'est exactement le cas où une traduction isolée aurait produit une phrase
fausse en allemand.

**Aucun fragment de ce sous-lot n'a exigé un gabarit `{var}`** : tous se reconstruisent correctement
dans les trois langues sans réordonner (réécriture qui serait de toute façon hors périmètre).

## 4. Tests

**T1 — ré-extraire les appels, pas lire la table. PASS.** Le bloc 7 **patché** est re-parsé, les
972 clés littérales re-collectées, puis `hasOwnProperty` vérifié sur les tables runtime :

- **56 / 56 clés du sous-lot toujours appelées** (aucune n'a disparu du code) ;
- **0 clé manquante en en/es/de** — une clé mal recopiée ressortirait « toujours manquante » ;
- **730 clés préexistantes inchangées**, 0 altérée ;
- reste **339** clés sans traduction dans le fichier = 395 − 56, cohérent.

⚠ L'assertion porte sur la **présence**, jamais sur `t(k) !== k` : « Export », « Import » se
traduisent **à l'identique** en allemand — un test d'inégalité les déclarerait faussement KO (piège
rencontré et corrigé pendant le banc).

**T2 — rendu écran (de). PASS.** 5 panneaux réellement ouverts (`PortPanel`, `ResearchPanel`,
`EnergyPanel`, `MapPanel`, `OptionsModal`) : **aucune des 54 clés détectables du lot n'apparaît en
français** à l'écran. (Les 2 clés non détectables sont « Export » et « Import », dont la traduction
allemande est identique.)

**T3 — fragments, phrase complète. PASS.** Phrases capturées **rendues**, pas reconstruites :
mots non collés, espaces corrects, ordre allemand correct (voir §3). Un toast réel a également été
capté : **« Antenne Amplificatrice: nur eines pro Insel »** — l'espace avant le « : » disparaît bien
en allemand.

**T4 — non-régression. PASS.** `node --check` **7/7** · **table `ui` française identique base ↔
patché (404 clés)** — le lot n'ajoute que en/es/de, l'affichage français ne peut pas bouger ·
**+56 clés / 0 altérée** dans chacune des 3 tables · **0 `pageerror`** sur tous les runs.

## 5. Écarts

1. **Branche et PR** : le brief demande `claude/i18n-lotI5a` et une PR par sous-lot ; la consigne
   d'Ethan (« lot 4, puis 5, pas en même temps, mais sur la même branche ») impose la branche
   désignée **`claude/chantier-l18n-s04ef0`** et donc **une PR unique** (#389) — le brief le prévoit
   (« Si Ethan préfère une PR unique, regrouper — mais valider par thème »). **La validation est bien
   faite par thème**, sous-lot par sous-lot, avec bump et rapport propres.
2. **Pièges de banc** : (a) fermer un panneau par un clic DOM sur `.research-backdrop` est **avalé
   par `useGhostGuard`** — il faut un vrai clic sur le `×` (`.rp-close`/`.slot-close`), sinon le
   panneau suivant ne s'ouvre jamais et l'on croit le test cassé ; (b) l'assertion `t(k) !== k` est
   un faux ami (§T1).

## 6. Contrôles finaux

| contrôle | résultat |
|---|---|
| `node --check` 7 blocs | **7/7** |
| `GAME_NOTES` | 370 car., **0 guillemet droit**, **0 séquence `\u`/`\x`** |
| `SAVE_VERSION` | **31, inchangé** |
| Collision de build | max **407** sur toutes les branches distantes → **408 libre** |
| Blocs 1-5 inchangés | `a50c1c4e…` · `8fbb2218…` · `d949f1c3…` · `35f4f974…` · `1be53ce4…` |
| Blocs modifiés | 6 `7ea7bb87…` (IIFE) · 7 `9b332c25…` (commentaire + `GAME_NOTES`) |

## 7. Suite

**I5b** (23 clés, souterrain / forage / élévateur) enchaîne sur la même branche, puis I5c, I5d, I5e.
