# RAPPORT — lot I4 (chantier i18n) : les 27 astuces `GAME_TIPS` sans traduction

**Livré en `GAME_BUILD = 407` / `GAME_VERSION = 'Alpha 17.4'`.** `SAVE_VERSION` INCHANGÉ (31).

| | |
|---|---|
| Base | **406 / Alpha 17.3**, SHA-256 `28d83a38…` (état de `main` après merge de la PR #388) — conforme au brief (« 402 ou postérieur ») |
| Fichier livré | SHA-256 `78ce12fb4747413ca2c28d019025cf6b7e7779d477c88a2670f965e9c70a458a` |
| Delta | **+57 228 o** pour le patch seul · **+59 287 o** avec bump, commentaire et `GAME_NOTES` |
| Volume | **27 astuces × 4 langues** = **108 titres + 316 paragraphes** |
| Code de rendu touché | **aucun** — une IIFE en fin de bloc 6, rien d'autre |

---

## 1. Le point qui change tout : **quatre** langues, pas trois

Le brief et l'inventaire annoncent « 27 titres + 79 paragraphes, × 3 langues = 318 chaînes ».
**C'est structurellement insuffisant, et le défaut serait silencieux.** `I18N.tip(id)` part du corps
**français DE LA TABLE**, pas du corps inline de `GAME_TIPS` :

```js
var fb = (F.tips[id] && F.tips[id].body) || [];   // F = LOCALES.fr
body: fb.map(function (s, i) { return nonEmpty(lb[i]) ? lb[i] : s; })
```

Or **aucune** des 27 astuces n'a d'entrée `fr` (la table fr n'en comptait que 27 : les 25 déjà
traduites + les 2 orphelines). Sans entrée fr, `fb = []` → `body` sort **vide** → `applyToData`,
gardé par `if (tr.body && tr.body.length)`, **ne réécrit rien** : le texte serait resté **français
dans les trois langues**, seul le titre passant.

**Mesuré avant d'écrire une seule traduction** (`i4_probe.js`, injection en page sur la base 406) :

| montage | `tip()` rend |
|---|---|
| `de` seul (ce que décrit le brief) | titre `DE-TITRE`, **corps `[]`** |
| `de` + `fr` | titre `DE-TITRE`, corps `["DE-P1","DE-P2"]` |
| `de` amputé d'un § | corps `["DE-P1", "FR-P2"]` — le piège d'index du brief, reproduit |

Livré : **27 × 4 langues**. L'entrée `fr` est le **texte source relevé au runtime** au caractère
près → **no-op strict à l'écran en français** (§T4).

## 2. Méthode

Patcheur Python (`patch_lotI4.py`, non commité comme les précédents) : une IIFE en fin de bloc 6,
fusion **par id AVEC GARDE** `if(!L.tips[t])` → **rejouable**, conforme à la convention majoritaire
(l'IIFE « TUTORIEL V2 » remplace sans garde ; on ne l'imite pas sur ce point, comme le demande le
brief). Non-ASCII en `\uXXXX`. Textes fr **extraits du runtime** (`GAME_TIPS` après `applyToData`),
jamais recopiés de l'inventaire.

**Contrôles bloquants AVANT écriture** (le patcheur refuse d'écrire sinon) : langue manquante,
titre vide, nombre de § ≠ français, balise `<b>`/`<i>` déséquilibrée.

**L'inventaire du brief a été re-dérivé indépendamment depuis le runtime** et retrouvé **à
l'identique** : 52 astuces, **27 sans traduction**, **79 paragraphes**, les 27 comptes de § conformes
un à un, 0 astuce partiellement traduite, 0 écart de § préexistant, et les 2 orphelines
(`non_stockable`, `upgrade_vs_v2`) présentes dans les 4 tables.

⚠ **Le piège emoji du brief ne s'applique pas ici, vérifié** : l'icône vit dans `t.icon`, pas dans
le texte — **0 chaîne à emoji de tête** parmi les 27 titres et 79 §. Les `<b>`/`<i>` (seules balises
présentes) sont reportés sur les mots équivalents.

## 3. Paragraphes par astuce et par langue

| id | fr | en | es | de | titre allemand |
|---|---|---|---|---|---|
| `antenne_modes` | 2 | 2 | 2 | 2 | Antenne: Tempo oder Ausbeute |
| `centrale_charbon` | 2 | 2 | 2 | 2 | Das Kohlekraftwerk |
| `centrale_diesel` | 2 | 2 | 2 | 2 | Das Dieselkraftwerk |
| `chaleur_nuc` | 2 | 2 | 2 | 2 | Wärme & Kühlung |
| `collider_arret` | 5 | 5 | 5 | 5 | Der Collider hat sich selbst abgeschaltet |
| `collider_cmp1` | 5 | 5 | 5 | 5 | Zwei Bits vergleichen |
| `collider_cmp2` | 5 | 5 | 5 | 5 | NAND & NOR: der Komparator in 3 Gattern |
| `collider_cmp3` | 5 | 5 | 5 | 5 | Natives XNOR — und die Leptonen |
| `collider_penalite` | 5 | 5 | 5 | 5 | Der Collider ist ausgegangen |
| `construire_mer` | 2 | 2 | 2 | 2 | Auf dem Meer bauen |
| `copier` | 2 | 2 | 2 | 2 | Ein Gebäude kopieren |
| `densifier` | 3 | 3 | 3 | 3 | Ausbauen & verdichten |
| `eolienne` | 2 | 2 | 2 | 2 | Das Windrad |
| `foreuse` | 3 | 3 | 3 | 3 | Der Bohrer und die Helium-3-Taschen |
| `four_arc_fer` | 3 | 3 | 3 | 3 | Die Lichtbogenöfen |
| `liaisons_port` | 2 | 2 | 2 | 2 | Hafenverbindungen |
| `nuc_mix` | 3 | 3 | 3 | 3 | Die bestrahlte Produktion aufteilen |
| `plutonium` | 2 | 2 | 2 | 2 | Plutonium & Motorenwerk |
| `port` | 2 | 2 | 2 | 2 | Der Hafen |
| `priorite` | 2 | 2 | 2 | 2 | Flusspriorität |
| `puits_piege` | 2 | 2 | 2 | 2 | Ölquelle: die Kosten steigen |
| `reserves` | 3 | 3 | 3 | 3 | Reserven & Transit |
| `traverser` | 3 | 3 | 3 | 3 | Ein Gebäude durchqueren |
| `traverser_tuyau` | 3 | 3 | 3 | 3 | Durchqueren: Leitungen auch |
| `tut_copier` | 3 | 3 | 3 | 3 | Kopieren statt suchen |
| `tut_debit` | 3 | 3 | 3 | 3 | Die Taktzahl steigern |
| `tut_marge` | 3 | 3 | 3 | 3 | Vorsprung aufbauen |
| **total** | **79** | **79** | **79** | **79** | **27 titres** |

## 4. Tests

**T1 — parité des paragraphes. PASS.** Sur les **52 astuces × 4 langues** :
`locales[lg].tips[id].body.length === locales.fr.tips[id].body.length` → **0 écart sur 52×3**,
**0 titre vide**, et **52/52 astuces couvertes en fr** (table fr : 27 → 54). C'est le test qui
attrape le piège d'index.

**T2 — application effective. PASS.** La vraie porte est `applyToData` : jeu chargé en `en`, `es`
puis `de`, `GAME_TIPS` relu → **27/27 titres ET corps identiques à la table, dans les 3 langues**.
Une entrée sous un id mal orthographié laisserait l'astuce française sans erreur : seul ce test le
voit. **25 astuces préexistantes intactes** dans les 3 langues.

**T3 — rendu écran (de). 18 PASS / 1 KO.** Par les **vrais chemins d'ouverture**, jamais par un
appel forcé :

| surface | astuce | comment | résultat |
|---|---|---|---|
| popup | `tut_marge`, `tut_debit` | `checkTips` re-propose le `why` de l'étape courante non vue | titre allemand ✓ |
| popup | `collider_penalite`, `collider_arret` | drapeau de notification consommé par la boucle de frame | titre allemand ✓ |
| **Aide** | `collider_cmp1` (5 §) | accordéon, vrai clic sur la tête de carte | **5 § allemands, 13 `<b>` interprétés** ✓ |
| **Aide** | `tut_marge`, `reserves` (3 §) | idem | 3 § allemands, `<b>` interprétés ✓ |

Icône rendue en **sprite** (`<img class="ui-ico">`) sur 3 des 4 popups. **Le KO est
`tut_debit` (📈)** — voir §5.3, défaut d'art préexistant et **délibéré**.

**T4 — non-régression. PASS.** `node --check` **7/7** · **identité française base 406 ↔ patché 407**
sur `GAME_TIPS`, `BUILDINGS`, `RES_SHORT`, `TECH_NODES`, `TUTORIAL_STEPS` : **tout identique** →
l'ajout des entrées `fr` est bien un no-op strict · **0 `pageerror`** sur tous les runs.

## 5. Écarts et constats — à lire

### 5.1 ⚠ Le POPUP n'affiche pas `body` — et `short` n'est traduit NULLE PART

**Défaut PRÉEXISTANT, mesuré, hors inventaire, NON corrigé.** Depuis 13.79 §D, `TipPopup` rend
**`tip.short`** (UNE phrase) quand il existe — et **les 52 astuces en ont un** (relevé au runtime :
`GAME_TIPS.filter(t => t.short).length === 52`). Or `short` n'est traduit par aucun mécanisme :
`I18N.tip()` ne rend que `{title, body}` et `applyToData` n'écrit que `t.title`/`t.body`.

Conséquence, **avant comme après ce lot, pour les 52 astuces** : le popup affiche un **titre
traduit et une phrase française**. C'est ce qu'on voit en T3a — « TippVorsprung aufbauen » avec un
corps d'une seule phrase en français.

**Le test T3 du brief (« ≥ 3 popups … les 5 § présents ») ne peut donc pas passer sur le popup** :
cette surface ne montre jamais 5 §. La surface qui rend `body` — donc **tout ce que ce lot
traduit** — est **le panneau Aide** (accordéon, 13.44), où les 5 § allemands et les `<b>` sont
vérifiés (T3b).

**Non corrigé délibérément** : combler ce trou demande de **modifier le moteur i18n**
(`I18N.tip()` + `applyToData` pour porter `short`) et d'écrire 52 × 3 chaînes de plus — un lot à
part entière, hors du périmètre annoncé (« 27 titres + 79 paragraphes »). **À arbitrer.**

### 5.2 Écart de volume assumé

Livré **27 × 4 = 108 titres et 316 paragraphes** au lieu des « 318 chaînes sur 3 langues » du
brief — les 27 titres + 79 § français en plus sont la **condition de fonctionnement** (§1), pas un
élargissement de périmètre : ils reproduisent le texte source au caractère près.

### 5.3 Trois icônes d'astuce restent en emoji brut — voulu

`tut_fin` 🎓, `tut_debit` 📈, `liaisons_port` 🔗 n'ont **pas** de sprite dans `UI_ICON_BY_EMOJI`
(58 entrées). **Contre-épreuve : rigoureusement identique sur la base 406 et sur le patché** — ce
lot n'y touche pas. Et ce n'est pas un oubli : le mémo **14.74** note que 🎓 et 🔗 ont été
**écartés en séance d'art** après plusieurs tentatives (le diplôme se lisait « casque », les anneaux
« lunettes »). Rien à corriger ici.

### 5.4 Divers

- **Branche** : le brief demande `claude/i18n-lotI4` ; la consigne de session impose la branche
  désignée **`claude/chantier-l18n-s04ef0`**. C'est elle qui est utilisée — et le lot I5 y sera
  poussé aussi, comme demandé.
- Les 2 entrées orphelines (`non_stockable`, `upgrade_vs_v2`) sont **laissées** : elles partent avec
  I6, pas ici.
- **Pièges de banc rencontrés** (à ne pas redécouvrir) : (a) fermer un popup exige un **vrai clic
  Playwright** — un `.click()` DOM est avalé par `useGhostGuard` (13.50), et l'on croit le popup
  bloqué ; (b) `showTip` **met en file** (13.80) : sans purge préalable, le popup visé attend
  derrière « bienvenue » et le test conclut à tort qu'il ne s'ouvre pas ; (c) le drapeau
  `colliderPenaltyNotify` est **consommé AVANT** la garde `!activeTipRef.current` → un popup ouvert
  fait **perdre** la notification ; (d) **`tut_copier` n'est le `why` d'aucune étape** depuis le lot
  14.86 (Copier est sorti de la trame) — les `tut_*` atteignables en popup sont `tut_marge` (étape 5)
  et `tut_debit` (étape 9).

## 6. Contrôles finaux

| contrôle | résultat |
|---|---|
| `node --check` 7 blocs | **7/7** |
| `GAME_NOTES` | 381 car., **0 guillemet droit**, **0 séquence `\u`/`\x`** |
| `SAVE_VERSION` | **31, inchangé** |
| Collision de build | max **406** sur toutes les branches distantes → **407 libre** |
| Blocs 1-5 inchangés | `a50c1c4e…` · `8fbb2218…` · `d949f1c3…` · `35f4f974…` · `1be53ce4…` (identiques à I3) |
| Blocs modifiés | 6 `58eaba1c…` (IIFE) · 7 `2e2e3455…` (commentaire de version + `GAME_NOTES`) |

## 7. Suite du chantier

**I5** enchaîne sur la **même branche** (consigne d'Ethan), après ce lot. ⚠ Son inventaire doit être
**régénéré sur la base 407** : le lot I3 a créé **34 clés `ui` à fournir**, et l'oubli signalé au
rapport I3 (`InfoPanel` `starting: 'Calibrage…'`) est à reprendre là. Reste **I6** en dernier
(purge des 2 orphelines + commentaires + convention IIFE).
