# RAPPORT — lot I5d (chantier i18n) : 27 clés `ui` — collisionneur / paliers / recherche

**Livré en `GAME_BUILD = 411` / `GAME_VERSION = 'Alpha 17.8'`.** `SAVE_VERSION` INCHANGÉ (31).

| | |
|---|---|
| Base | **410** (sous-lot précédent, même branche) |
| Delta | **+8,726 o** pour le patch seul |
| Clés | **27 × 3 langues = 81 chaînes**, dont **5 fragments** de concaténation |
| Code de rendu touché | **aucun** — une IIFE en fin de bloc 6 |


## Inventaire re-dérivé indépendamment — conforme

**972 clés uniques, 395 absentes des 3 tables** sur la base 407 : les chiffres du brief, retrouvés
au nombre près. Après retrait des 6 typographiques : **389 clés réelles**, dont **81 fragments**.
**0 clé partiellement présente.** L'affectation figée se croise parfaitement
(**56 / 23 / 37 / 27 / 246 = 389**, **0 entrée d'inventaire sans clé extraite**, **0 clé non
affectée**). Aucun classement n'a été recalculé, comme le demande le brief.

## Méthode (commune aux 5 sous-lots)

Extraction **AST** des premiers arguments **littéraux** de `I18N.t` dans le bloc 7, croisée avec les
tables **relevées au runtime**. Le patcheur ne reçoit **jamais une clé écrite à la main** : il prend
la liste extraite et les traductions **par index**. C'est le risque n°1 du brief — une apostrophe
droite au lieu d'une typographique ne correspondrait à rien, `I18N.t` retomberait sur le français
et **rien ne le signalerait**. Il refuse d'écrire si une traduction est vide ou si une interpolation
`{var}` a été altérée.

IIFE en fin de bloc 6, fusion **par clé avec garde** `if(!L.ui[k])` → rejouable. Non-ASCII en
`\uXXXX`. Aucune ligne de rendu touchée.

⚠ **Une espace de bord fait partie de la clé et du rendu — mais la traduction peut légitimement ne
pas avoir la même** : l'espace avant « : » est une convention **française**. Chaque fragment a été
traduit en **relisant son site d'appel**, jamais isolément.

## Tests

**T1 — ré-extraire les appels, pas lire la table. PASS.** Le bloc 7 **patché** est re-parsé, les
**972** clés littérales re-collectées, puis `hasOwnProperty` vérifié en en/es/de : **27 / 27 clés du
sous-lot toujours appelées**, **0 manquante**, **730 clés préexistantes inchangées**. Une clé mal
recopiée ressortirait « toujours manquante » — c'est le but ; un test qui parcourrait la table
ajoutée ne pourrait pas échouer.

⚠ L'assertion porte sur la **présence**, jamais sur `t(k) !== k` : « Export », « Import »,
« Data Center », « Plutonium », « Auto », « Volume » se traduisent **à l'identique**.

**T2 — rendu écran (de). PASS.** 6 surfaces ouvertes, dont la **fiche à onglets du Collisionneur** (le Collisionneur est un **terrain**, pas un bâtiment : la tuile est visée directement) : **aucune des 26 clés détectables du lot n'apparaît en français**, et « Bestätigungen » / « Collider ausschalten » y sont relevées. (La 27ᵉ clé, « Data Center », se traduit à l'identique.)**

**T3 — fragments, phrase complète. PASS (4 phrases).** Chaque phrase est **assemblée
exactement comme au site d'appel** (numéro de ligne relevé dans le bloc 7) avec le **vrai `I18N.t`**
et la vraie table allemande, puis contrôlée : aucun fragment resté français, **aucun mot collé**,
**aucune double espace**, ordre des mots correct.

**T4 — non-régression. PASS.** `node --check` **7/7** · **table `ui` française identique base 407 ↔
patché 412 (404 clés)** — le chantier n'ajoute que en/es/de, l'affichage français ne peut pas
bouger · **+389 clés / 0 altérée** dans chacune des 3 tables au terme des 5 sous-lots · **0
`pageerror`**.


**Aucun fragment de ce sous-lot n'a exigé un gabarit `{var}`** : tous se reconstruisent correctement
dans les trois langues sans réordonner (réécriture qui serait de toute façon hors périmètre).

## Contrôles finaux (état au terme des 5 sous-lots)

| contrôle | résultat |
|---|---|
| `node --check` 7 blocs | **7/7** |
| `GAME_NOTES` | 378 car., **0 guillemet droit**, **0 séquence `\u`/`\x`** |
| `SAVE_VERSION` | **31, inchangé** |
| Collision de build | max **407** sur toutes les branches distantes → 408-412 libres |
| Blocs 1-5 inchangés | `a50c1c4e…` · `8fbb2218…` · `d949f1c3…` · `35f4f974…` · `1be53ce4…` |
| Blocs modifiés | 6 `333f75ef…` (les 5 IIFE) · 7 `5d31df09…` (commentaires + `GAME_NOTES`) |
| Fichier livré | SHA-256 `ed0618fad78ef1f62f6b640ff79a026a15ac0bc31def36f46c2f0c4f2de78c51` |

## Écart de branche

Le brief demande `claude/i18n-lotI5{x}` et **une PR par sous-lot** ; la consigne d'Ethan (« lot 4,
puis 5, pas en même temps, mais sur la même branche ») impose la branche désignée
**`claude/chantier-l18n-s04ef0`** et donc **une PR unique** (#389). Le brief le prévoit : « Si Ethan
préfère une PR unique, regrouper — mais **valider par thème** ». La validation est bien faite
**sous-lot par sous-lot**, avec bump, tests et rapport propres à chacun.
