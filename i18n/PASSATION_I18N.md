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

## CHECKPOINT 2 — FAIT ✅ (couche UI câblée)

Les libellés d'interface en dur sont enveloppés dans `I18N.t('texte fr')` (gettext, clé = texte fr).

**Méthode** : transform automatisé conservateur (`scratchpad/i18n_wrap.js` + `i18n_wrap2.js`), 2 passes :
1. Littéraux **UTF-8** entre guillemets simples/doubles (~362 enveloppes).
2. Formes **`\xNN`** produites par Babel pour les accents (~79 enveloppes ; ex. `"B\xE2timent"`).

Restreint au `<script>` du jeu. Positions **sûres** uniquement : on saute si la chaîne est précédée de
`className/key/id/label/name/type/color/res/kind/mode/tab/icon :`, d'une comparaison `==`/`!=`, de
`case`, d'un `I18N.t(` déjà présent, ou suivie de `:` (clé d'objet). Tokens de longueur < 3 ou sans
lettre ignorés (évite `/s`, `·`…). Total ≈ **440 enveloppes / ~351 clés distinctes** sur 403.

**Vérifié** : `node --check` (6 blocs, 0 échec) ; smoke Chromium :
- `de` → onglets d'action **Gebäude/Netz/Kopieren/Abreißen/Verbessern**, barre du haut **Optionen/HAFEN/
  FORSCHUNG/INVENTAR/Produktion**, Options **Sprache/Stufen anzeigen/Effizienz anzeigen (%)**, bandeau
  **Auswahlmodus…** ; **0 erreur console**.
- `fr` → tout en français (repli sur la clé) ; **0 erreur**.

### Reliquat (reste en français hors-fr — repli)
- **Toasts en littéraux-gabarits** (backticks `` ` ``, ex. `` `✓ ${b.name}` ``) : non matchés par le wrap
  par guillemets. À envelopper à la main si voulu (souvent contenu dynamique + fragment fr).
- Quelques libellés **ajoutés après build 108** absents du TSV (ex. « Fond des panneaux », libellés
  des sous-catégories récentes déjà gérés via `I18N.t(g.label)`). À ajouter aux locales au besoin.
- Codes tuile (`label` FER/CHAR…) volontairement non traduits (mnémoniques).

## Reliquats / décisions
- Nouveaux bâtiments postérieurs au build 108 (ex. `mine_uranium_v3`) absents des locales →
  `applyToData` les laisse en français (repli, via garde `nonEmpty`). À ajouter aux locales plus tard.
- Mes catégories diffèrent du prompt d'origine (Traitement scindé en Fer-acier/Cuivre/Électronique/
  Plastique et chimie ; pas de catégorie « Or »). Les traductions ont été fournies en conséquence.
- L'augmentation des libellés de catégories vit dans le HTML (IIFE après le module) — si on re-inline
  `archipel_i18n.js` un jour, réappliquer cette IIFE (ou fusionner les clés dans `LOCALES`).
