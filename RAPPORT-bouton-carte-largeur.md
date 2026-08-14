# RAPPORT — Le bouton Carte changeait de largeur pendant le tutoriel

**Livré : `GAME_BUILD = 403`, `GAME_VERSION = 'Alpha 17.0'`, `SAVE_VERSION = 31` (INCHANGÉ).**

Base : build **402 / Alpha 16.9** (`main`). Numéro re-vérifié **juste avant le bump** — leçon du
build 401, où une autre session avait pris le même numéro pendant l'attente.
Fichier final `981f1f95a92cb13ea009d54ea02333803650997a4cf8cb56647fbb23d0fd1eab`.

---

## 1. Le symptôme

Signalé sur **Galaxy S25 FE** : « le bouton carte se contracte ou redevient normal pendant le tuto —
il ne doit pas bouger ».

## 2. La cause racine

Le bouton Carte portait la classe **`help-btn`**, que j'avais reprise du bouton Aide au lot 2 pour
récupérer sa géométrie (`margin-left:4px; padding:5px 9px`). Or `help-btn` n'est pas un jeu de
paddings, c'est un **nom de rôle : « icône seule »**. Sa règle :

```css
.help-btn > span:last-child{display:none;}   /* « Aide en icône seule (« ? ») » */
```

masque le **dernier `<span>`** du bouton. Sur le bouton Carte, lequel c'est **dépend de l'état de
jeu** — d'où trois largeurs, mesurées à 411 px CSS (le viewport du S25 FE) :

| état | dernier span | effet | largeur |
|---|---|---|---|
| aucune île suivante (dernière île) | le **libellé** | « Carte » **disparaît** | **50 px** |
| réparation possible mais non payable | le **badge 🛠** | badge **invisible** | **106,3 px** |
| réparation payable | la pastille (`absolute`) | badge rendu | **130,3 px** |

Pendant le tutoriel, le stock du port franchit sans cesse les **10 000 lingots + 10 000 ciments**
exigés par la réparation de la liaison : `canRepair` bascule à chaque passage, et le bouton bat
entre **106,3 et 130,3 px**. C'est exactement ce que le joueur voit.

⚠ **Deux défauts que celui-ci cachait, découverts en le corrigeant :**
1. **Le badge 🛠 fusionné au lot 4b n'était visible que lorsque la réparation était déjà payable** —
   c'est-à-dire au seul moment où il n'apprend plus rien. Le reste du temps il était purement
   invisible, alors que c'est lui qui doit signaler qu'une liaison attend.
2. **Sur la dernière île, le bouton perdait son libellé** et se réduisait à son icône.

## 3. Le correctif

1. **Classe dédiée `.map-btn`** — même géométrie que `.help-btn`, sans la règle « icône seule ». Le
   bouton Carte n'aurait jamais dû hériter d'un rôle qui n'est pas le sien.
2. **L'emplacement du badge est toujours rendu**, `visibility:hidden` quand il n'y a rien à réparer.
   ⚠ Un `min-width` en pixels ne tiendrait pas : le libellé fait *Carte* / *Map* / *Mapa* / *Karte*,
   de largeurs différentes.
3. **`.map-btn` est exempté de la compaction `@media (max-width:479px)`**, au même titre que
   `.help-btn`. Le commentaire de cette règle dit qu'elle existe **précisément pour laisser la place
   au bouton Carte** — lui retirer son libellé la retournerait contre son propre but. Il l'était
   déjà, mais **par accident**, via `help-btn`.

**Aucun changement de comportement**, aucune donnée touchée. `SAVE_VERSION` inchangé.

---

## 4. Validation

**Nouveau test T9**, quatre volets :

| # | verdict | mesure |
|---|---|---|
| **T9a** | **PASS** | 411 px (S25 FE), **7 états de jeu** (stock 0 / 9 999 / 10 000 / un seul au seuil / 12 000 / île 2 ouverte / dernière île) → **une seule largeur : 130,3 px**, libellé « Carte » **toujours présent** |
| **T9b** | **PASS** | Écran large (900 px) : largeur constante, libellé présent |
| **T9c** | **PASS** | **4 langues** : constante dans chacune — fr 130,3 · **en 109,8** · **es 120** · de 130,3 (les largeurs diffèrent d'une langue à l'autre, ce qui est normal ; c'est la **constance dans une langue** qui compte) |
| **T9d** | **PASS** | **Contre-épreuve sur le build NON patché** (`origin/main` servi en parallèle) → **50 / 106,3 / 130,3 px** et libellé vide sur deux états. Sans elle, « une seule largeur » ne prouverait pas que la mesure sait en voir plusieurs |

**Non-régression** : **T8** (hauteur de la carte + sa contre-épreuve), **T1+T2**, **T3-T7**, **i18n
4 langues** — toutes rejouées, toutes vertes. `node --check` **7/7**, accolades CSS **936 / 936**.

### SHA-256 des 7 blocs, re-extraits APRÈS le bump

| bloc | SHA-256 (16 car.) | taille |
|---|---|---|
| blk1 | `a50c1c4e7f4a304c` | 418 |
| blk2 | `8fbb22187703339c` | 4 397 |
| blk3 | `d949f1c3687aedad` | 10 751 |
| blk4 | `35f4f974f4b2bcd4` | 131 835 |
| blk5 | `1be53ce44e7be14f` | 1 113 969 |
| blk6 | `f1e06b99972b7c94` | 248 524 |
| blk7 | `7dc919046600433c` | 1 716 268 |

`GAME_NOTES` : **429 caractères** extraits par la regex de la CI, accents littéraux, **0 séquence
`\u`**, aucun guillemet droit.

---

## 5. La leçon, et pourquoi le lot 4b ne l'a pas vue

C'est **la même leçon qu'au build 401** (la carte écrasée à 2 px), sous une autre forme : mes
assertions vérifiaient qu'un élément **existait**, jamais **ce qu'il mesurait**. Ici le bouton était
bien là, cliquable, le halo se posait dessus au pixel près — tous mes tests passaient — pendant que
sa largeur changeait sous le doigt du joueur et que le badge que je venais d'y fusionner restait
invisible la plupart du temps.

**Règle ajoutée dans le fichier** : une classe reprise « pour le style » emporte **aussi son
comportement**. `help-btn` était un **nom de rôle**, pas un jeu de paddings. Avant de recopier une
classe d'UI, lister les règles qui la ciblent — au besoin en demandant au navigateur lesquelles
matchent réellement (`el.matches(rule.selectorText)` sur `document.styleSheets`), qui est ce qui a
permis de trouver la cause ici après trois hypothèses fausses.

## 6. Points en suspens

- **Le bouton Options reste en icône seule sous 479 px** : c'est le comportement voulu par la règle
  de compaction, inchangé.
- **La largeur diffère d'une langue à l'autre** (109,8 à 130,3 px). C'est normal et sans effet : la
  bande du HUD ne déborde dans aucune des quatre langues au viewport testé.
- **Aucun autre bouton ne porte `help-btn` par emprunt** : vérifié, seul le bouton Aide le garde,
  et c'est son rôle.
