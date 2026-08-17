# Internationalisation d'Archipel Industry — guide d'intégration

Kit préparé à partir de `Archipel_industry_alpha-7.html` (GitHub, build 108 / Alpha 10.83 / SAVE_VERSION 13).
Le jeu n'a pas été modifié : ce dossier ne contient que les fichiers à **intégrer**.

**Langues fournies COMPLÈTES : Français · English · Español · Deutsch.**
Par défaut, le jeu prend la **langue du système/téléphone** ; choix mémorisé.

---

## 1. Contenu du kit

| Fichier | Rôle |
|---|---|
| `archipel_i18n.js` | Moteur i18n autonome + **les 4 langues complètes**. À coller dans un `<script>` avant le jeu. |
| `locales/fr.json` | Français (source de vérité). |
| `locales/en.json` | Anglais complet. |
| `locales/es.json` | Espagnol complet. |
| `locales/de.json` | Allemand complet. |
| `ui_strings_reference.tsv` | Les 404 libellés d'UI + leur **n° de ligne** dans le jeu (pour la phase 2). |

**Couverture, par langue** : 32 ressources · 56 bâtiments (nom) · 27 nœuds de recherche · 7 étapes de tutoriel · 17 astuces (titre + 41 paragraphes) · 404 libellés d'UI. Tout est traduit dans les 4 langues (vérifié à 100 %).

---

## 2. Architecture : deux couches

**Couche CONTENU (données)** — textes rangés dans des structures à clé stable :
`RES_SHORT`, `BUILDINGS` (`.name`/`.label`), `TECH_NODES` (`.name`), `TUTORIAL_STEPS` (`.goal`), `GAME_TIPS` (`.title`/`.body`).
→ Réécrite **en place** par `I18N.applyToData(...)`. **Aucune ligne de rendu à toucher.** C'est le *drop-in*.

**Couche UI (interface)** — libellés écrits en dur dans les `React.createElement(...)`.
→ Chaque libellé est enveloppé dans `I18N.t('texte français')`. Mécanique (~404 sites).

Repli systématique sur le **français** si une clé manque. On peut donc livrer la phase 1 seule : tout le contenu passe dans la langue choisie, l'UI reste en français tant qu'elle n'est pas câblée.

---

## 3. Langue par défaut = langue du système

À l'initialisation, `archipel_i18n.js` choisit, dans l'ordre :
1. le dernier choix de l'utilisateur (`localStorage['archipel_lang']`) ;
2. sinon la **langue du système/navigateur** (`navigator.languages`, ex. `de-AT` → `de`, `es-419` → `es`) si elle fait partie des 4 ;
3. sinon **anglais** (défaut international) si la langue système n'est pas couverte.

Rien à coder pour ce comportement : il est intégré au module.

---

## 4. Phase 1 — couche contenu (intégration minimale)

### 4.1 Charger le module
Coller le contenu de `archipel_i18n.js` dans un `<script>…</script>` **avant** le `<script>` principal du jeu (ligne ~1621), pour rester en fichier unique hors-ligne. (En dev : `<script src="archipel_i18n.js"></script>`.)

### 4.2 Brancher l'application (1 ligne)
Les structures sont des `const` de la portée du script principal : appeler `applyToData` **depuis cette portée**, après la définition des données (après `GAME_TIPS`, ligne ~6230, ou juste avant `ReactDOM…render`) :

```js
if (window.I18N) I18N.applyToData({ BUILDINGS, RES_SHORT, TECH_NODES, TUTORIAL_STEPS, GAME_TIPS });
```

### 4.3 Sélecteur de langue (panneau Options)
Le plus simple, cohérent avec le changement de slot existant (rechargement) :

```js
React.createElement('select', {
  value: I18N.get(),
  onChange: function (e) { I18N.set(e.target.value); location.reload(); }
}, I18N.available().map(function (code) {
  return React.createElement('option', { key: code, value: code }, I18N.names[code]);
}))
```

> Variante sans rechargement : `I18N.set(code)`, re-`applyToData({…})`, puis re-render (UI + canvas). Le rechargement reste le plus sûr.

### 4.4 Sauvegardes
La langue est une **préférence d'interface**, persistée par le module dans `localStorage`. **Ne pas** l'inscrire dans les sauvegardes, **ne pas** incrémenter `SAVE_VERSION`. Aucun impact sur la rétrocompatibilité.

---

## 5. ⚠️ Point de vigilance : libellés qui servent de clé logique

Les libellés de `TOOLBAR_GROUPS[].label` sont **comparés dans le code** ; les traduire casse la logique :

```js
// ligne ~3561
const NETWORK_GROUPS = TOOLBAR_GROUPS.filter(g => g.label === 'Infrastructure' || g.label === 'Jonctions');
// ligne ~3562
const BUILD_GROUPS   = TOOLBAR_GROUPS.filter(g => g.label !== 'Infrastructure' && g.label !== 'Jonctions');
```

**Correctif** — séparer une clé stable de l'étiquette affichée :
1. Ajouter une `key` à chaque groupe : `{ key:'infra', label:'Infrastructure', ids:[…] }`, puis `junction`, `extraction`, `energy`, `processing`, `nuclear`.
2. Filtrer sur la clé : `g.key === 'infra' || g.key === 'junction'` (et l'inverse pour `BUILD_GROUPS`).
3. Afficher l'étiquette via `I18N.t(g.label)`. Les 6 libellés de groupe sont déjà fournis dans les 4 langues, prêts à l'emploi :

| fr | en | es | de |
|---|---|---|---|
| Infrastructure | Infrastructure | Infraestructura | Infrastruktur |
| Jonctions | Junctions | Uniones | Kreuzungen |
| Extraction | Extraction | Extracción | Förderung |
| Énergie | Energy | Energía | Energie |
| Traitement | Processing | Procesamiento | Verarbeitung |
| Nucléaire | Nuclear | Nuclear | Nuklear |

(Ces 6 chaînes sont à ajouter au `ui` de chaque langue, ou à traduire à la main au rendu — elles ne passent pas par `applyToData` car elles vivent dans `TOOLBAR_GROUPS`, pas dans la couche contenu.)

> Règle générale : avant de traduire une chaîne, vérifier qu'elle n'est pas comparée ailleurs (`=== '…'`, `switch`, clé d'objet). Toute chaîne « clé » doit être découplée de son affichage.

---

## 6. Phase 2 — couche UI (404 libellés, déjà traduits dans les 4 langues)

Les traductions existent ; il reste le **câblage** : envelopper chaque libellé français en dur dans `I18N.t(...)`. `ui_strings_reference.tsv` liste les 404 chaînes avec leur n° de ligne.

```js
React.createElement('button', { title: 'Fermer' }, …)
// devient
React.createElement('button', { title: I18N.t('Fermer') }, …)
```

**Concaténations** (`'Coût : ' + cost`, `'→ Île ' + n`) : soit traduire le fragment (`I18N.t('Coût : ') + cost`), soit regrouper avec variable (`I18N.t('Réparer l\'île {n}', { n: id })` — renommer alors la clé dans les 4 `.json`). La clé est le **texte français** (modèle gettext) : si tu modifies un libellé fr, mets à jour la clé dans chaque langue.

Procéder panneau par panneau (n° de ligne groupés : Recherche ~6717, HUD ~6838, Port ~8103, Énergie ~8336, Options ~9326, toasts ~10500–12900). Le jeu reste jouable à chaque étape (le non-câblé reste en français).

---

## 7. Compléter / ajouter une langue

- Éditer `locales/<code>.json`. Toute valeur vide `""` retombe sur le français.
- `tips` : garder les balises `<b>…</b>` et le **même nombre de paragraphes** dans `body` (repli paragraphe par paragraphe).
- Réinjecter le JSON dans l'objet `LOCALES` en tête de `archipel_i18n.js` (le fichier hors-ligne embarque les langues ; les `.json` servent à la traduction/revue).
- Nouvelle langue : ajouter une entrée dans `LOCALES` **et** dans `LANG_NAMES` (en tête du module). Elle devient automatiquement sélectionnable et détectable par la langue système.

---

## 8. Décisions / points à valider

- **Codes tuile (`label`)** : pour en/es/de, ils réutilisent un **mnémonique commun** (`IRON`, `COAL`, `WIND`…), lisible dans toutes les langues et sûr côté largeur 32 px ; le fr garde ses codes d'origine (`FER`, `CHAR`…). On peut les localiser par langue dans les `.json` si tu préfères — c'est juste plus risqué visuellement.
- **Câblage de la couche UI** dans le jeu (404 sites + correctif §5) = une passe d'édition du fichier de jeu : non faite ici (règle « modifier le jeu sur demande »). À déclencher quand tu veux, ou à confier à Opus. C'est le seul reste.
- **Marque** : « Archipel Industry », « ARCHIPEL / INDUSTRY » non traduits.
- **`index.html`** : `<html lang="fr">` peut être ajusté dynamiquement si besoin.
- **Relecture** : les traductions es/de sont de première main — à faire relire idéalement par un locuteur natif avant publication.

---

## 9. Glossaire (cohérence terminologique)

| fr | en | es | de |
|---|---|---|---|
| île | island | isla | Insel |
| Réseau | Network | Red | Netz |
| Route / Tuyau / Câble | Road / Pipe / Cable | Carretera / Tubería / Cable | Straße / Rohr / Kabel |
| débit | throughput | caudal | Durchsatz |
| intrants | inputs | insumos | Eingänge |
| Améliorer / Démolir | Upgrade / Demolish | Mejorar / Demoler | Verbessern / Abreißen |
| Réparer / remblai | Repair / land reclamation | Reparar / relleno | Reparieren / Aufschüttung |
| accumulateur | accumulator | acumulador | Akkumulator |
| Centrale | Power Plant | Central | Kraftwerk |
| Recherche / Astuce | Research / Tip | Investigación / Consejo | Forschung / Tipp |
| déficit / lot | deficit / batch | déficit / lote | Defizit / Los |
