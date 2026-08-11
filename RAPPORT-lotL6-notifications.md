# RAPPORT — Lot L6 (notifications)

Brief : `BRIEFlotL6notifications` · patcheur `patch_L6.py` (pré-compilé, fourni)
Branche : `claude/temps-souterrain-display-uoonrz`, **repartie de `main`** (la PR #372 a été mergée).

## Version produite

| | |
|---|---|
| `GAME_BUILD` | 389 → **390** |
| `GAME_VERSION` | Alpha 15.6 → **Alpha 15.7** |
| `SAVE_VERSION` | **31, INCHANGÉ** — aucune migration |
| Taille | 3 391 803 → **3 399 561** o (**+7 758**) |
| SHA-256 livré | `09437fa52c332faf1362c66db6bd067d20869780e665b68a95b6ff2769281d81` |

Le brief annonçait +4 587 o pour le patch seul ; s'y ajoutent le commentaire cumulatif et `GAME_NOTES`.

## Sortie du patcheur

Base vérifiée `d5b0029b…` = build 389, **aucun avertissement**.

```
OK - 11 ancres appliquees
SHA-256 fichier patche : 1fe5ec0796dd3955176d03dc9b3f18ffd8e8ad2f6f29d4ec87b8544cfc62a9db
```

**Conforme au caractère près.** Contrôle intermédiaire : **avant le bump, les 7 blocs étaient tous
identiques aux SHA du brief, bloc 7 compris** (`58c5c419…`) — patch appliqué à l'identique.

## SHA-256 des 7 blocs, ré-extraits APRÈS la toute dernière modification du HTML

| bloc | octets | sha256 |
|---|---|---|
| 1 | 418 | `a50c1c4e7f4a304c650c0cfa7e06c4ffdfb0d87510d83738114d4dd5a1641628` |
| 2 | 4 397 | `8fbb22187703339c146b2f82badd8701d199128cd3d9dd8673d213c8616ca541` |
| 3 | 10 751 | `d949f1c3687aedadcedac85261865f29b17cd273997e7f6b2bfc53b2f9d4c4dd` |
| 4 | 131 835 | `35f4f974f4b2bcd44da73963347f8952e341f83909e4498227d4e26b98f66f0d` |
| 5 | 1 112 066 | `6066e8c1aeb44929ec5e92d946a936e70451d0c76d7f1375fae084200c308720` |
| 6 | 239 836 | `f6cdea55af87528fdb0faa6d73d1cf2367219509802364d6d0226252832c73d9` |
| 7 | 1 647 108 | `1385cc2f13d0ec25df286c8cdcdaa0e48973d782ec5c084e3c6bcd9de4b48663` |

**Blocs 1 à 6 byte-identiques au brief** ; l'écart du bloc 7 porte le bump, le commentaire cumulatif
et `GAME_NOTES`. `node --check` : **7/7 OK**. Empreintes prises **après** la dernière retouche du HTML.

## §4 — la rectification me concerne, et le brief a raison

Le §4 corrige une affirmation **que j'ai écrite moi-même** dans le commentaire du build 389 : « la
retenue vaut exactement le prix de la réparation ». **Vérifié dans le code, c'est faux :**

- `buildingTotalCost(bld)` = `b.cost` de l'**id courant** + `cumulativeUpgradeCost` de ses montées.
  Sur un bâtiment de palier, `b.cost` est **vide**.
- `cumulativeInvested(id, upgrade)` = **toute la chaîne depuis la V1**, forfaits de palier compris.

Les deux coïncident **uniquement** sur un bâtiment sans palier. Mon test du build 389 portait sur une
**cimenterie de base à l'upgrade 0** — un cas où ils sont égaux par construction : il ne pouvait pas
révéler la divergence, et j'ai généralisé à tort une mesure juste mais non représentative. Le brief
chiffre l'écart sur la partie de référence : `centrale_enrichissement_v2` Nv.16, **3 312 069 acier**
perdus à la démolition contre **0 acier** facturé à la réparation.

**Aucun changement de code** : l'écart est connu et assumé (il pousse vers la réparation sur les gros
paliers) ; seul le commentaire est rectifié pour qu'un futur lecteur ne le « corrige » pas.

## Validation — 19 assertions sur le patch, 19 PASS, plus la contre-épreuve

Banc : Chromium 1194 headless, serveur depuis la racine du dépôt, viewport 420×900. Deux copies de
banc (`BANC_L6.html` pour le patch, `BANC_389.html` pour la base) exposant `window.__H` en **exposeur
paresseux** — **supprimées avant le commit**, et leur absence du livrable est asservie par un test.

⚠ Le brief prévient que le test de la pastille **passe à vide** sur une save telle quelle. Le montage
force donc l'état exact : nœud **non-livraison** (`mode !== 'delivery'`, sinon `hasPendingResearch`
exige aussi `deliveryReady`), `status = 'condition_ok'`, `notified = true`, les deux files vidées ;
on vérifie que `hasPendingResearch` est **vrai** et la pastille **absente**, puis on appelle
`finishCatchUp`.

| # | test | résultat | valeurs relevées |
|---|---|---|---|
| V1 | Boot | **PASS** | 0 `pageerror` |
| V1b | Rattrapage 24 h | **PASS** | `{ticks: 86401, warm: 1000, simulated: 1000, approx: true, capped: false}` |
| V2 | Confirmations au récap | **PASS** | « 🔬 Collisionneur : **691** confirmation(s) pendant l'absence. » |
| V2b | Nombre cohérent | **PASS** | 8 injectées pendant l'échauffon → **691** au total (683 extrapolées) |
| V3 | Montage : nœud en attente, pastille éteinte **avant** | **PASS** | `{nodeId: 1, pendingBefore: true, badgeBefore: false}` |
| V3b | **Pastille rallumée après `finishCatchUp`** | **PASS** | `.research-btn.pending` **présente** |
| V5 | Porte fautive détectée | **PASS** | `activeLogicAlerts` → **1** entrée, position **exacte** (10,13), `faces: 1` |
| V6 | Compteur du HUD | **PASS** | `alertCount` = stock 0 + énergie 0 + **logique 1**, badge « 1 » |
| V7 | Ligne d'alerte + « Y aller » | **PASS** | titre « Aller à cette porte » |
| V7b | **Calque logique allumé** | **PASS** | `ui.logicLayer` **true** après le clic |
| V8 | Absence de 4 min | **PASS** | **aucun récap**, 0 `.offline-note` (seuil de 300 s inchangé) |
| — | Boot du **vrai fichier** | **PASS** | build **390** / Alpha 15.7 / SAVE 31, canvas **100 %**, 0 `pageerror` |
| — | Poignée de banc absente du livrable | **PASS** | `window.__H` **undefined** |
| — | `activeLogicAlerts` présente dans le livrable | **PASS** | `function` |

### V4 — la contre-épreuve, le même montage sur le build 389 non patché

| | build 389 | build 390 |
|---|---|---|
| montage (nœud en attente, pastille éteinte avant) | **identique** | **identique** |
| pastille après `finishCatchUp` | **éteinte** (`false`) | **allumée** (`true`) |
| `activeLogicAlerts` | **FONCTION ABSENTE** | 1 entrée localisée |
| bouton d'alerte du HUD | absent | présent, badge « 1 » |
| ligne des confirmations au récap | **absente** | présente |

**Cinq verdicts opposés pour un montage identique** : le lot est falsifiable de bout en bout, et le
bug de la pastille est reproduit avant d'être fermé.

## Écarts par rapport au brief, et leurs raisons

1. **V5 mesurée sur une porte forgée en séance, pas sur celle d'Ethan.** Le brief attend
   `{isl: 6, r: 17, c: 13, faces: 1}` — la porte que la sauvegarde de référence contient. Je n'ai pas
   cette sauvegarde : j'ai forgé une porte fautive sur l'île courante et vérifié que `activeLogicAlerts`
   rend **sa position exacte**, ce qui teste la même propriété (les coordonnées ne sont plus jetées).
2. **Aucun autre écart.** Les 11 ancres appliquées verbatim, y compris la rectification du §4.

## Points signalés, NON corrigés

- Les quatre nouveaux libellés (ligne des confirmations, ligne d'alerte, titre du bouton) ne sont pas
  traduits : repli français → lot i18n de l'audit 381.
- Le **toast du démarrage subsiste tel quel** à côté de la nouvelle ligne d'alerte. Sa formulation
  « voir leur fiche » devient redondante maintenant que la liste localise les portes — **signalé sans
  y toucher**, comme demandé.
- Les lignes d'alerte logique réutilisent `alert-row` / `alert-go` **sans nouvelle règle CSS** ;
  l'affichage tient à 420 px sur le banc.
- `activeLogicAlerts` **balaie toutes les îles à chaque calcul du HUD**. Coût non mesurable sur mes
  parties de test (aucune dégradation d'images observée, 0 `tickError`) ; **aucune mémoïsation
  ajoutée** faute de profilage — à rapporter si un point chaud apparaît, plutôt que d'optimiser à
  l'aveugle.
- L'écart réparation / démolition sur les bâtiments de palier (§4) reste **assumé**, code inchangé.

## Piège de banc payé en séance

**`processLogic` recalcule `gateWiredInh` à chaque tick**, exactement comme `logicOff` et
`conduitLoad` : un état forgé à la main est effacé au tick suivant, et le test lit alors zéro alerte
en croyant à un défaut du patch. Il faut **ré-affirmer l'état en continu** (`setInterval` ~25 ms),
patron déjà documenté au mémo pour `co.powered` et `conduitLoad`. Deuxième piège reconfirmé : le HUD
ne se re-rend qu'au **bump du tick** — attendre l'apparition de `.inv-alert-btn` avant de conclure
qu'une ligne d'alerte est absente.
