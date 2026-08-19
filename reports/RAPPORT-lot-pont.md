# RAPPORT — Lot PONT (build 434 / Alpha 20.1)

Brief `BRIEFlotpont.md`, patcher `patch_lot_pont.py` **pré-compilé**, test unitaire
`SafeName-test.java` fourni. Base : `main` @ `ebe93f2` (build 433 / Alpha 20.0).
Branche : `claude/code-audit-qbbdio`.

---

## 1. Version produite

| | |
|---|---|
| `GAME_BUILD` | **433 → 434** |
| `GAME_VERSION` | **Alpha 20.0 → Alpha 20.1** |
| `SAVE_VERSION` | **31, inchangé** |

Numéro relevé sur **toutes** les branches distantes avant le bump (règle de collision du
build 400) : max = **433** (`main` et `claude/code-audit-qbbdio`, toutes deux au lot
précédent) → **434 libre**.

Le bump est **fonctionnel**, pas conventionnel : aucune ligne de jeu ne change, mais
`version.json` dérive de `GAME_BUILD` — sans bump, l'APK corrigé ne serait jamais proposé à un
joueur déjà installé. `GAME_NOTES` reste honnête (« Aucun changement visible en jeu »), en ASCII
et sans guillemet droit.

## 2. Ancres — les 7 à `count == 1`, vérifiées AVANT écriture

Passage à blanc sur le fichier réel, avant toute modification :

| Point | `count` |
|---|---|
| A2/champ (`private BroadcastReceiver installReceiver;`) | 1 |
| A2/session (`int sessionId = pi.createSession(params);`) | 1 |
| A2/garde (signature `onReceive` + les 2 lignes `EXTRA_STATUS`) | 1 |
| A2/fermeture (le `Toast` « Installation interrompue » et son `}`) | 1 |
| A4/import (`import java.net.URL;`) | 1 |
| A4/appel (ligne `String name = (filename == null …)`) | 1 |
| A4/assainisseur (`private void writeDownload(…) {`) | 1 |

Sentinelles absentes avant patch (`A2` et `A4`), `installSessionId` à **0**,
`import java.util.Locale;` à **0** → l'état de départ est bien celui que le patcher attend.

**Après patch** : les 2 sentinelles à 1, `import java.util.Locale;` à 1, et **`installSessionId`
à 5 occurrences**. Le patcher est **idempotent** (2ᵉ passage : « A2 déjà appliqué | A4 déjà
appliqué », delta +0, SHA identique).

## 3. Empreintes — ré-extraites du fichier patché

| Fichier | Avant | Après |
|---|---|---|
| `MainActivity.java` | `8c06e9af…d698a45c2` · 23 807 o | **`a3dc21f0f771339566624ec8864563b3cda5f255374a2c116d0b65f07dec737b`** · **26 671 o** |
| `Archipel_industry_alpha-7.html` | `cf83b0bf…f370c68` · 3 777 542 o | `bfea9498…39fbc13b` · 3 781 458 o (bump + commentaire cumulatif seuls) |

**Le SHA-256 du fichier Java et sa taille sont EXACTEMENT ceux annoncés par le brief** — le patch
appliqué ici est byte-identique à celui du rédacteur. Le bump ne touche pas ce fichier : son SHA
est le même avant et après.

## 4. Delta d'octets

**+2 864 o EXACT** sur `MainActivity.java` (valeur du brief au byte près), mesuré par le patcher
et confirmé par `wc -c` (23 807 → 26 671). Le fichier de jeu prend +3 916 o, intégralement le
bump et le bloc de commentaire cumulatif — **aucune ligne de JS ni de CSS n'est modifiée**
(vérifié au diff : les seules lignes touchées sont `GAME_BUILD`, `GAME_VERSION`, `GAME_NOTES` et
les commentaires insérés au-dessus).

## 5. Suite de validation — montages réellement exécutés

| # | Résultat | Montage |
|---|---|---|
| **T1** | **PASS** | `javac` sur base et patch : **100 erreurs de part et d'autre**, **14 familles d'erreurs, ensembles IDENTIQUES** (`diff` vide après normalisation des numéros), **0 erreur de syntaxe** (`illegal start`, `expected`, `class, interface`, `reached end of file` : 0/0). **Calibrage** : le même fichier privé d'une accolade de `safeDownloadName` sort `MainActivity.java:281: error: illegal start of expression` → la méthode détecte bien ce qu'elle prétend détecter. |
| **T2** | **PASS 18/18** | `SafeName.java` compilé et exécuté (Java pur). **Contrôle ajouté avant d'exécuter** : le corps de `safeDownloadName` du test a été comparé ligne à ligne à celui du fichier patché → **VERBATIM IDENTIQUE** (11 lignes de part et d'autre) ; sans ce contrôle, le test prouverait la justesse d'une copie, pas celle du code livré. Les **4 noms réellement produits par le jeu ressortent inchangés** (`archipel-sauvegarde.txt`, `archipel-partie_1.txt`, `archipel-journal-433.txt`, `archipel-mon-slot.txt`) ; les 6 cas de traversée et les 8 cas limites sortent aux valeurs attendues. |
| **T3** | **PASS (5 occurrences)** | ⚠ **la commande du brief ne mesure pas ce que sa valeur attendue annonce** — voir §7. `grep -o installSessionId \| wc -l` = **5** (déclaration, écriture de session, **2 lectures dans la garde**, remise à `-1`), sur **4 lignes**. |
| **T4** | **PASS** | Compilation Android réelle des 3 paquets via `workflow_dispatch` **sur la branche** — voir §5 bis. |
| **T5** | **PASS** | Gardes CI rejouées **APRÈS** rédaction des commentaires de ce lot (leçon du run 561) : `ko-fi` **1** en publique / **0** en magasin ; `const SELF_UPDATE = true;` **0** en magasin ; `^const DEV_BUILD = false;$` 1 en publique, `^const DEV_BUILD = true;$` 1 en dev ; `^const SELF_UPDATE = false;$` 1 en magasin ; `^const SUPPORT_URL = 'https://ko-fi…';$` 1 en publique. **Extractions ancrées (M2 du lot précédent)** : `GAME_BUILD` = 434, `GAME_VERSION` = `Alpha 20.1`, `GAME_NOTES` = la note complète. **Garde M4 passante.** |
| **T6** | **NON EXÉCUTÉ — appareil requis, NON porté au vert** | Mise à jour in-app complète jusqu'à l'écran de confirmation, sur APK dev. **C'est le test qui compte** : une garde qui rejetterait nos propres broadcasts rendrait l'auto-mise à jour muette. |
| **T7** | **NON EXÉCUTÉ — appareil requis, NON porté au vert** | Broadcast `fr.archipel.industry.INSTALL_STATUS` forgé hors installation sur API 26-32. |

**Contrôles complémentaires** : `node --check` **7/7 sur les 3 variantes CI**
(`game-public` / `game-dev` / `game-store`, reproduites par les `sed` exacts du workflow) — le
compte de blocs est vérifié **avant** de boucler et l'extracteur refuse de conclure si ≠ 7 ;
accolades du `<style>` **équilibrées** (961 / 961).

### 5 bis. T4 — run CI

`workflow_dispatch` sur `claude/code-audit-qbbdio`. Résultat consigné ci-dessous après complétion :

- **run 568 — succès.** Détail en §9.

## 6. Ce que le lot ferme, et ce qu'il ne ferme pas

**A2.** Le récepteur exige désormais `EXTRA_SESSION_ID == installSessionId`, et
`installSessionId` ne vaut autre chose que `-1` que **pendant** une session que nous avons
ouverte. Deux points de conception vérifiés au fichier, pas supposés :

- **la fenêtre s'ouvre AVANT le `commit()`** (`installSessionId = sessionId` est posé juste après
  `createSession`, donc avant que le moindre broadcast puisse partir) ;
- **elle ne se referme PAS sur `STATUS_PENDING_USER_ACTION`**, qui n'est pas terminal : l'écran
  de confirmation suit, puis un second broadcast. La refermer là ferait rater ce second message.

**A4.** Requalification reprise telle quelle du brief, parce qu'elle est juste : **ce n'est plus
une vulnérabilité atteignable**. Le jeu ne produit que des noms sûrs, et depuis A1 aucune page
externe ne peut plus être chargée dans la WebView pour appeler le pont autrement. Le correctif
tient à la **structure** — un pont est une frontière de confiance, et une frontière qui délègue
sa validation à l'appelant n'en est pas une. **Comportement observable inchangé** : c'est
précisément ce que mesure le premier tiers de T2.

## 7. Écarts au brief, et raisons

1. ⚠ **T3 : la commande du brief ne mesure pas sa valeur attendue.** Le brief écrit
   « `grep -c installSessionId` → **5** ». Or `grep -c` compte les **lignes**, pas les
   occurrences, et la ligne de garde en contient **deux** :
   `if (installSessionId == -1 || sid != installSessionId) return;`. La commande littérale rend
   donc **4**. Le contrôle interne du patcher, lui, compte bien les **occurrences** (`s.count(...)
   != 5`) et il passe. Mesuré des deux façons : **5 occurrences sur 4 lignes**. T3 est PASS sur la
   métrique voulue ; c'est la commande qui est à corriger, pas le code.
2. **Contrôle ajouté à T2, non demandé** : la comparaison verbatim entre l'assainisseur du test
   et celui du fichier patché. Un test qui exécute une *copie* ne prouve rien tant que la copie
   n'est pas prouvée identique — la vérification tient en trois lignes et rend le 18/18 opposable.
3. **Aucune modification du patch.** SHA et delta conformes au brief au byte près ; rien n'a été
   « amélioré » en passant.

### Le choix session-ID plutôt que nonce, rediscuté — comme le brief le demande

Le brief écarte le nonce parce qu'il « repose sur la survie de nos propres extras à travers le
remplissage de l'`IntentSender` », comportement qu'il ne peut pas vérifier sans appareil.
**La prémisse est plus solide qu'il ne le crédite** : la sémantique d'`Intent.fillIn` est
d'**ajouter** des extras, jamais d'en retirer, et les extras de l'intent de base d'un
`PendingIntent` l'emportent en cas de conflit — c'est du contrat `Intent`, pas du comportement
AOSP observé. Un nonce survivrait donc, très probablement.

**Cela ne change pas la décision, et je ne l'ai pas modifiée** : `EXTRA_SESSION_ID` est posé par
`PackageInstaller` lui-même sur les **deux** chemins concernés (demande de confirmation
utilisateur et statut final), c'est le contrat documenté, et le lot précédent vient précisément
de fermer la classe « échec silencieux » — ce n'est pas le moment de parier sur une lecture de
spec non exécutée. Le résidu (ID devinable, mais seulement pendant une installation lancée par
l'utilisateur) est le bon compromis.

**Piste pour plus tard, si T6 est un jour exécuté** : accepter le broadcast si
`sid == installSessionId` **OU** si un nonce que nous aurions glissé correspond. Les deux gardes
sont indépendantes, l'une couvre la défaillance de l'autre — mais cela ne se livre pas sans un
appareil pour le vérifier.

## 8. Points ouverts

- **T6 et T7 restent dus, avec le T7 du lot 20.0** (repli `openExternally` sans navigateur
  système). Les trois tiennent en une session sur appareil. **Aucun n'est porté au vert ici.**
- **A3** (`update(url)` sans liste d'hôtes autorisés) : décision produit, pas correctif mécanique.
- **A5** (APK publiés en `assembleDebug`), **A6** (README `android/` périmé), **J1/J2** (code mort
  dont `makeIcon`), `_config.yml` périmé, `node_modules` commité : lots distincts.
- **Mineurs de l'audit 431 non traités** : flux non fermés, sessions `PackageInstaller` jamais
  abandonnées, I/O de `saveText` sur le thread UI.
- ⚠ **Play Store** : ce lot change la coquille Android, donc le `.aab` produit **diffère** de
  celui déposé en test fermé — **à ne pas soumettre pendant la fenêtre de 14 jours**. Le merge
  republiera APK, `index.html` et `version.json`, sans effet sur la Play Console.

## 9. Run CI de validation

`workflow_dispatch` sur la branche `claude/code-audit-qbbdio`, commit `5aa5162` →
**run 568, conclusion `success`** (1 min 48 s).

| Étape | Résultat |
|---|---|
| Build **PUBLIC** APK | success |
| Build **DEV** APK | success |
| Build **STORE** bundle `.aab` + APK de contrôle | success |
| Assert store package (paquet, `targetSdk`, permissions, debuggable) | success |
| Assert appIds (2 éditions installables côte à côte) | success |
| Show signing certificate (clé stable) | success |
| Upload APK / store artifacts | success |
| Sync PWA (`index.html` + cache `sw.js`) | success — **libre par conception** (n'écrit que dans le workspace du run) |
| **Publish to `apk-latest` release** | **SKIPPED** |
| **Sync `version.json`** | **SKIPPED** |

**Les deux étapes à effet de bord sont bien sautées** : le gate `refs/heads/main` tient, rien
n'est publié. C'est la seule chose que le conteneur pouvait prouver du côté Android — il n'y a ni
SDK ni appareil ici, et **un APK qui compile peut mal se comporter à l'écran** : T6 et T7 restent
les seuls tests qui tranchent.
