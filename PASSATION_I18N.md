# Passation — Intégration i18n (Archipel Industry)

Build 125 / Alpha 11.00. Branche `claude/bug-fix-improvement-1ly46s`.

## CHECKPOINT 1 — FAIT ✅ (couche CONTENU + langue système + sélecteur)

Ce qui est câblé dans `Archipel_industry_alpha-7.html` :

1. **Module embarqué** : `archipel_i18n.js` (137 Ko, 4 langues fr/en/es/de) inliné dans un
   nouveau `<script>` placé **avant** le `<script>` principal du jeu. Aucun `</script>` littéral
   (vérifié). `window.I18N` défini au chargement. Langue par défaut = **langue système**
   (`navigator.languages`, repli `en`), choix mémorisé dans `localStorage['archipel_lang']`.
2. **Bloc d'augmentation** (juste après le module) : ajoute aux 4 langues les **9 libellés de
   catégories** du menu Bâtiment + `Langue` / `Langue du jeu (système par défaut)` (clés absentes
   du module d'origine). Les `.json` racine (`fr/en/es/de.json`) ont reçu les mêmes clés (référence).
3. **Couche CONTENU (drop-in)** : `if (window.I18N) I18N.applyToData({ BUILDINGS, RES_SHORT,
   TECH_NODES, TUTORIAL_STEPS, GAME_TIPS });` appelé **après `GAME_TIPS`**, avant tout rendu →
   réécrit en place noms de ressources/bâtiments/recherches + tutoriel + astuces. Repli fr auto.
4. **`TOOLBAR_GROUPS`** : ajout d'une **`key` stable** par groupe (`infra/junction/extraction/
   energy/steel/copper/electronics/chemistry/nuclear`). Les filtres `NETWORK_GROUPS`/`BUILD_GROUPS`
   comparent désormais `g.key` (plus `g.label`). Le libellé de groupe est rendu via `I18N.t(g.label)`.
5. **Sélecteur de langue** dans les Options (sous « Fond des panneaux ») : `<select>` (noms
   `I18N.names`), `onChange` → `I18N.set(code)` + `location.reload()`.

### Validation
- `node --check` : 6 blocs `<script>` JS OK ; CSS équilibré.
- Smoke Chromium (3 locales) :
  - `de-DE` → lang `de`, `mine_fer`=**Eisenmine V1**, `four_fer`=**Eisenofen V1**, `t('Fermer')`=**Schließen**, `t('Fer-acier')`=**Eisen & Stahl** ; 0 erreur console.
  - `en-US` → `en`, **Iron Mine V1**, `Close`, **Iron & Steel**.
  - `fr-FR` → `fr`, valeurs françaises.
  - Rendu du menu Bâtiment en `de` : noms ET ressources (Stein/Eisenerz) ET catégories (Förderung, Eisen & Stahl) traduits.
- `SAVE_VERSION` **inchangé** (13). Aucune recette / équilibrage / mécanique modifiés. Hors-ligne (file://) OK.

## CHECKPOINT 2 — RESTE À FAIRE (couche UI, ~404 libellés)

Le **chrome UI** (boutons, titres de panneaux, toasts : Options, PORT, RECHERCHE, INVENTAIRE,
Production, Bâtiment/Réseau/Copier/Démolir/Améliorer, « Menu construction… », « Tutoriel passé… »,
etc.) reste **en français**. Câblage prévu : envelopper chaque littéral fr de `ui_strings_reference.tsv`
dans `I18N.t('…')` (clé = texte fr exact, espaces compris), panneau par panneau, avec `node --check`
+ smoke après chacun. ⚠️ Ne jamais envelopper une chaîne servant de `className`/clé/comparaison.

## Reliquats / décisions
- Nouveaux bâtiments postérieurs au build 108 (ex. `mine_uranium_v3`) absents des locales →
  `applyToData` les laisse en français (repli, via garde `nonEmpty`). À ajouter aux locales plus tard.
- Mes catégories diffèrent du prompt d'origine (Traitement scindé en Fer-acier/Cuivre/Électronique/
  Plastique et chimie ; pas de catégorie « Or »). Les traductions ont été fournies en conséquence.
- L'augmentation des libellés de catégories vit dans le HTML (IIFE après le module) — si on re-inline
  `archipel_i18n.js` un jour, réappliquer cette IIFE (ou fusionner les clés dans `LOCALES`).
