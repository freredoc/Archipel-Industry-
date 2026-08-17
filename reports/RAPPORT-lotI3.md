# RAPPORT — lot I3 (chantier i18n) : enveloppement des littéraux jamais passés à `I18N.t`

**Livré en `GAME_BUILD = 406` / `GAME_VERSION = 'Alpha 17.3'`.** `SAVE_VERSION` INCHANGÉ (31).

| | |
|---|---|
| Base | **405 / Alpha 17.2**, SHA `937f380a…` (état de `main` après merge de la PR #387) — conforme au brief |
| Fichier livré | SHA-256 `28d83a3833f9142effe35f3cf49100f50d3f19dd89c0e7ead143de0e4330fe99` |
| Delta | **+687 o** pour le patch seul · **+3 073 o** avec bump, `GAME_NOTES` et commentaire |
| Sites enveloppés | **74 / 74** (55 littéraux + **19 fragments de gabarit**) |
| Traductions ajoutées | **aucune** — 40 chaînes gagnent leur traduction (clé déjà en table), 34 attendent I5 |

---

## 1. Ce que le lot fait, et le seul critère qui le juge

74 chaînes françaises écrites **en dur dans des fonctions de rendu**, donc intraduisibles quoi qu'on
ajoute aux tables. Le lot les enveloppe dans `I18N.t(...)` — **il n'ajoute aucune traduction**. Le
critère est donc unique et vérifiable : **l'écran affiche-t-il exactement la même chose en
français ?** (§T0 ci-dessous : oui, sur 20 captures, au caractère près.)

## 2. Méthode — patch par OFFSETS AST, pas par correspondance de texte

Toucher 74 sites de rendu React par `replace` textuel, c'est le terrain des régressions L6/L8. Le
patch travaille donc sur l'**AST** (acorn) du bloc 7 : chaque site est identifié par ses offsets, et
surtout par son **rôle syntaxique** remonté depuis la chaîne des parents — argument de rendu, valeur
de prop (et **laquelle**), opérande de `===`, clé d'objet, nom de balise, fragment de gabarit.

Deux transformations :

1. **`Literal`** : `"texte"` → `I18N.t("texte")`. On enveloppe la **tranche source brute** : guillemets
   et échappements d'origine sont préservés à l'identique, aucun risque de ré-échappement (le fichier
   mêle UTF-8 littéral et `\xNN`).
2. **`TemplateElement`** : `` `❌ ${b.name} : terrain non autorisé` `` →
   `` `❌ ${b.name}${I18N.t(" : terrain non autorisé")}` ``.

⚠ **LES 19 CHAÎNES DE `App` NE SONT PAS DES `Literal`.** Les toasts sont écrits en **gabarits**
(backticks) : leurs fragments sont des `TemplateElement`, et **la méthode du brief
(`"x"` → `I18N.t("x")`) ne les atteint pas** — mon premier appariement les a d'ailleurs toutes
rendues « introuvables ». Le brief nomme pourtant l'une d'elles (`' démoli — remboursé à '`) comme un
fragment à envelopper : l'intention est claire. Elles deviennent donc une interpolation, ce qui
**préserve la structure du gabarit** et **n'en fusionne aucun en clé unique** (explicitement hors
périmètre).

⚠ **Les fragments gardent leurs espaces** — `" démoli — remboursé à "`, `" : terrain non autorisé"` :
les sortir de l'appel changerait la clé.

Après application, le bloc 7 est **re-parsé par acorn** (un `)` manquant se voit là, pas au boot),
puis re-analysé : les 74 cibles sont bien devenues des premiers arguments de `I18N.t` (**candidats
hors `I18N.t` : 904 → 830, écart exactement 74, 0 cible restante**).

## 3. Sites écartés — et pourquoi c'est le cœur du lot

⚠ Envelopper une chaîne **comparée** casse la comparaison dès qu'une autre langue est active, et
**le symptôme n'apparaît jamais en français**. Deux garde-fous :

- **par rôle** : aucun des 74 sites n'est une clé d'objet, un opérande de comparaison, une
  indexation ni une prop interdite (`className`/`key`/`id`/`type`/`color`…). Les 11 sites de type
  « prop » sont des `title`/`label`/`safety` — des libellés rendus, vérifiés un par un ;
- **par valeur (T3)** : balayage des 904 littéraux du bloc 7 — **aucune** des 74 chaînes ne figure
  ailleurs en `===`, `switch` ou indexation. **PASS.**

**Un scan large indépendant trouve 134 autres littéraux français dans ces mêmes fonctions de rendu.
Ils sont volontairement écartés**, et c'est ce qui valide l'inventaire : ce sont des emoji (`🔥`,
`🗺`), des ids de porteur (`'port'`), des **classes CSS** (`'theme-inox'`) et des **valeurs
comparées** (`'bleu'`, valeur de thème). Les envelopper casserait le jeu en anglais sans qu'on le
voie jamais en français.

⚠ **SEUL VRAI OUBLI REPÉRÉ, NON CORRIGÉ** (hors inventaire, donc hors périmètre) : `InfoPanel`
L17633, `starting: 'Calibrage…'` — ses **trois sœurs du même objet** (`off`, `running`, `stopping`)
sont enveloppées, lui non. À prendre au lot I5. Je ne l'ai pas ajouté de moi-même : élargir
unilatéralement un patch de 74 sites de rendu est exactement le risque que le brief signale.

## 4. Tests

**T0 — identité française (invariant central). PASS.** Le **même scénario scripté** joué sur la base
405 et sur le build patché, en `fr` ; `textContent` normalisé de chaque panneau, de ses attributs
`title`, et des toasts. **20 captures comparées, 0 écart** — pas même une espace.

**T1 — rendu effectif. PASS, 11/11 composants exigés réellement ouverts** (+ `MapPanel` en prime) :

| ouvert à l'écran | comment |
|---|---|
| `Hud` | permanent |
| `InfoPanel` | **tap canvas réel** sur une aciérie posée |
| `NetworkPanel` | **tap canvas réel** sur une route |
| `UpgradePanel` | outil Améliorer + tap canvas réel |
| `PortPanel` | bouton PORT |
| `ResearchPanel` | bouton RECHERCHE |
| `EnergyPanel` | pastille ⚡ |
| `MapPanel` | bouton Carte |
| `RepairModal` | Carte → **clic sur le nœud d'île** |
| `OptionsModal` / `SlotPanel` | ⚙ → « Emplacements de sauvegarde » |
| `OfflineModal` | save **antidatée de 2 h** + rechargement réel |

**0 `pageerror`** sur l'ensemble des runs. Le bruit console (404 `sw.js`, `ERR_CONNECTION_RESET` sur
`version.json`) est **préexistant** et filtré explicitement.

**T2 — gain visible (de). PASS.** Au moins une chaîne par composant passe à l'allemand :

- `RepairModal` : « **Reparieren — Insel 2** » (était « Réparer — Île 2 »)
- `OfflineModal` : « … **— nichts zu produzieren** » (était « — rien à produire »)
- `EnergyPanel` : « **⚡ Strom fließt durch Kabelnetze…** »
- `Hud` (titres), `PortPanel`, `ResearchPanel`, `SlotPanel`, `MapPanel`, `InfoPanel`,
  `NetworkPanel`, `UpgradePanel` : idem.

Découpage annoncé par le brief **vérifié en table** : les **40** marquées « déjà en table » ont bien
leur clé en en/es/de (**40/40**) ; les **34** autres ne l'ont **pas** (**0/34**) — elles sont
enveloppées et attendent I5. C'est visible à l'écran et c'est **normal** : en allemand, le toast rend
« **Stahlwerk** : terrain non autorisé » (nom traduit par I2, fragment en attente de sa clé), et
`OptionsModal` affiche encore « Fond des panneaux ».

**T3 — aucune comparaison cassée. PASS** (§3).

**T4 — non-régression. PASS.** `node --check` **7/7** (avant, après patch, après bump) · re-parse
acorn du bloc 7 · **round-trip byte-identique** des 7 blocs · boot sans erreur.

## 5. Écarts et points à connaître

1. **Aucun patcheur n'était joint** (le brief le dit « déjà exécuté »). Écrit ici, en JS + acorn,
   pour pouvoir raisonner sur les rôles syntaxiques. Non commité (aucun `patch_*` n'est versionné
   dans ce dépôt) ; le résultat est vérifiable par T0/T3/T4.
2. **Branche** : le brief demande `claude/i18n-lotI3` ; la consigne de session impose la branche
   désignée `claude/chantier-l18n-s04ef0`. C'est elle qui est utilisée.
3. **Les 19 fragments de gabarit** sortent du cadre littéral du brief (`"x"` → `I18N.t("x")`) : leur
   traitement est décrit au §2. Si cette forme ne convient pas, c'est le seul point à revoir — les
   55 autres sites sont des enveloppements stricts.
4. **`Calibrage…` laissé de côté** (§3), à reprendre en I5.
5. Une fois I5 livré, **34 chaînes de plus se traduiront sans retoucher une ligne de rendu** : c'est
   tout l'intérêt d'avoir séparé les deux lots.

### Pièges de banc rencontrés (coûteux, à ne pas redécouvrir)

- **L'inventaire ouvert recouvre le canvas** (14.89) : une fois le port rempli il est haut et **avale
  tous les taps de tuile**. Le replier avant de taper — c'est ce qui faisait échouer `InfoPanel`,
  `NetworkPanel` et `UpgradePanel` alors que le tap fonctionnait en isolation.
- **Confirmer des nœuds ouvre « Recherche terminée »**, dont le backdrop couvre le canvas : purger
  **après** la forge, pas avant.
- **`EnergyPanel`, `PortPanel` et la carte partagent les classes `research-panel port-panel`** : un
  sélecteur `:not(.port-panel)` ne voit jamais l'`EnergyPanel`.
- **`RepairModal` n'a plus de bouton « Réparer »** : la réparation a été fusionnée dans la **carte**
  au lot 4b, et le modal s'ouvre **au clic sur le nœud d'île**. Surtout, `repairInfo` ne vise que
  `currentIsland + 1` **et seulement si elle est encore VERROUILLÉE** : déverrouiller l'île 2 pour
  garnir les autres panneaux rend le modal **inatteignable**.
- **Les familles de panneaux doivent être testées dans des runs SÉPARÉS** : un panneau laisse un
  backdrop qui avale l'interaction suivante.

## 6. Contrôles finaux

| contrôle | résultat |
|---|---|
| `node --check` 7 blocs | **7/7** |
| Re-parse acorn du bloc 7 patché | **OK** |
| `GAME_NOTES` | 461 car., **0 guillemet droit**, **0 séquence `\u`/`\x`** |
| `SAVE_VERSION` | **31, inchangé** |
| Collision de build | max **405** sur toutes les branches distantes → **406 libre** |
| SHA-256 des 7 blocs | 1 `a50c1c4e…` · 2 `8fbb2218…` · 3 `d949f1c3…` · 4 `35f4f974…` · 5 `1be53ce4…` · 6 `3e31f1a5…` · 7 `970c3491…` |

## 7. Suite du chantier

**I5 doit suivre I3** (`00ORDRE.md`) : ce lot crée **34 clés à fournir**. Restent aussi **I4**
(astuces, indépendant) et **I6** en dernier (purge + convention IIFE). ⚠ L'inventaire d'I5 devra être
**régénéré sur la base 406** pour intégrer ces 34 clés et `Calibrage…`.
