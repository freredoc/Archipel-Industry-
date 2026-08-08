# Lots A′ et B — tutoriel : resynchro i18n puis chapitre île 2

Deux commits, à merger ensemble. Branche : `claude/tuto-v2-fix-0q23n4`.

| | build | version | `SAVE_VERSION` |
|---|---|---|---|
| avant | 379 | Alpha 14.96 | 31 |
| après | **381** | **Alpha 14.98** | **31 — inchangé** |

---

## Commit 1 — Lot A′ : resynchronisation de la table i18n du tutoriel (380 / 14.97)

Les 4 tables `LOCALES.<lang>.tutorial` du grand littéral passent de 7 à 10 entrées.

⚠ **La prémisse du brief est fausse : le défaut décrit n'est pas atteignable en jeu.** Le
littéral à 7 entrées est **mort** — une IIFE d'augmentation (13.60), dans le même bloc 6, fait
`L.tutorial = m.tutorial` (remplacement en bloc, inconditionnel) avec les 10 entrées correctes
dans les 4 langues. Trois preuves sur la base non patchée : bloc 6 tronqué avant l'IIFE → 7
entrées périmées / bloc complet → 10 justes ; rejeu du vrai `_g` → `fr/en/es/de = 10/10`,
0 null sur 40 ; **bannière relevée en jeu** → l'étape 2 affiche déjà le bon texte.

Le texte du brief n'a donc **pas** été appliqué tel quel : il diverge de la source vivante en
4 points (`en[5]`, `es[4]`, `es[5]`, `de[5]`). Le fichier aurait porté deux copies crédibles
qui se contredisent — exactement le piège que le brief dénonce. **Les 4 tables littérales sont
régénérées depuis l'augmentation**, jamais retapées → une seule vérité.

**C'est un no-op pour le joueur, et c'est prouvé** : bannière relevée en navigateur,
4 langues × 10 étapes avant et après → **40/40 identiques**. Ce que le lot apporte :
le prochain lecteur de `LOCALES.fr.tutorial` ne lira plus l'inverse de la vérité, et l'IIFE
étant gardée (`if(!window.I18N||!I18N.locales) return;`), un refactor qui cesserait d'exposer
`I18N.locales` ferait retomber le jeu **en silence** sur le littéral — mesuré : ce repli donne
un texte faux sur 379 et le texte juste sur 380.

---

## Commit 2 — Lot B : chapitre île 2 du tutoriel (381 / 14.98)

Le tutoriel ne s'arrête plus au bout de l'île 1. Un **chapitre de 4 étapes** (indices 10 à 13)
enchaîne sur la réparation de la liaison maritime, le passage sur l'île 2, la première demande
d'import au port et l'amélioration du transit.

**23 paires** appliquées, toutes à `count == 1` sur la base ; round-trip **23/23 verbatim** ;
**étapes 0 à 9 octet à octet identiques**.

### Ce que ça change

- **Le tutoriel devient multi-îles** : champ `island` sur une étape + helper `tutStepIsland`,
  qui remplace le `!== 1` codé en dur dans **5 sites** (plan de pose, verrou, halo, filtre du
  menu, onglets) — sans lui, tout le guidage s'éteignait dès qu'on quitte l'île 1.
- **Le menu Bâtiment se débride à l'étape 9**, sinon il serait resté filtré aux 6 bâtiments
  révélés pendant tout le chapitre.
- **« Cible ⇒ Réserve » démarre à OUI en nouvelle partie** — sans ça, ce que le joueur importe
  repart aussitôt et l'étape 12 n'enseigne rien. Additif : une save existante garde son
  réglage, une save terminée ne rejoue pas le chapitre.
- Astuce `tut_transit` + les 4 goals posés **dans l'IIFE d'augmentation ET dans le miroir
  littéral**, avec un commentaire « ⚠ MIROIR, PAS SOURCE » au-dessus de `var LOCALES` : la
  leçon du lot A′ rendue permanente dans le fichier.

### Deux pièges silencieux, tous deux réels

- `NumField` **avale toute prop hors de sa destructuration** — un `data-tut` posé à l'appel
  serait parti à la poubelle sans erreur. `dataTut` ajouté et propagé sur l'`input`.
- **TDZ** : `panelsRef.current.portOpen = portOpen;` doit rester après son `useState`. Le
  remonter donne une **page blanche que `node --check` ne voit pas** — seul un boot l'attrape.

---

## Validation

- `node --check` **7/7 sur les deux éditions** (publique et dev).
- **T1** (i18n au runtime, après l'IIFE) : **0 KO** — 14 goals non-nuls × 4 langues,
  `tut_transit` traduit en 3 paragraphes. **Contre-épreuve sur la base : 20 KO** → le test est
  falsifiable.
- **T2** (`TUTORIAL_STEPS` hors navigateur) : **22 OK / 0 KO**, bornes exactes vérifiées
  (999 refusé / 1000 accepté ; `portSpeed[2]` ne valide pas l'étape 13).
- **T3** (chapitre joué par de **vrais gestes**) : **22 OK / 0 KO** — réparation réelle → île 2
  débloquée → saisie réelle de 1000 → amélioration réelle du transit → `step=14`,
  `active=false`, tutoriel terminé.
- **T4** (non-régression sur saves réelles) : **7 OK / 0 KO** sur 4 scénarios.
- **Non-régression** : lot A (halo / saturation) **15 OK**, halo tutoriel **8 OK**.
- **Boot des 2 éditions** : canvas 100 %, 0 `tickError`, 0 erreur console.

Suites **rejouées deux fois sans flottement**. Détail complet dans `RAPPORT-lotB.md` et
`RAPPORT-lotA-prime.md`.

### Écarts assumés

- **Hash du bloc 7 ≠ brief**, structurel : ce bloc porte `GAME_BUILD`/`GAME_VERSION`/
  `GAME_NOTES`, or le brief est pré-compilé avant le choix du numéro. Contrôle fait sur la
  variante **patch seul** : bloc 6 `f6cdea55…`/232 100 et bloc 7 `8bf4fb5f…`/**1 578 266** —
  **conformes au caractère près**.
- **2 assertions de non-régression mises à jour** (renversements voulus) : elles assertaient
  `Tuto 5/10` et `Tuto 6/10`, or la trame compte désormais 14 étapes. Elles portent maintenant
  sur le numéro d'étape et jamais sur le total figé.

🤖 Generated with [Claude Code](https://claude.com/claude-code)

https://claude.ai/code/session_01F8a9jVd7hBN12F4tTXRup9
