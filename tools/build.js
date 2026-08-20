#!/usr/bin/env node
/* =============================================================================
 * Lot S — RECONSTRUCTION DU MONOLITHE.
 *
 *   src/index.src.html  +  src/sprites-inline.js   ->   Archipel_industry_alpha-7.html
 *
 * Le fichier genere reste commite a la racine (modele A) : l'URL raw, la CI
 * `android.yml` et l'exclusion Pages en dependent. Il ne doit JAMAIS etre edite
 * a la main — c'est tout l'objet du garde-fou ci-dessous.
 *
 * Node pur, ZERO dependance npm.
 *
 * Usage : node tools/build.js [--force]
 *   --force  outrepasse le garde-fou de sha256 (premier build, ou remplacement
 *            assume d'une modification faite a la main dans le fichier genere).
 * ========================================================================== */
'use strict';
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

const ROOT = path.resolve(__dirname, '..');
const F_SRC = path.join(ROOT, 'src', 'index.src.html');
const F_SPR = path.join(ROOT, 'src', 'sprites-inline.js');
const F_OUT = path.join(ROOT, 'Archipel_industry_alpha-7.html');
const F_STAMP = path.join(ROOT, '.build-stamp');
const MARKER = '<!--@@SPRITES_INLINE@@-->';
const BANNER = '<!-- FICHIER GENERE PAR tools/build.js - NE PAS EDITER. Sources : src/ -->';

const FORCE = process.argv.includes('--force');
const sha = buf => crypto.createHash('sha256').update(buf).digest('hex');
function die(msg) { console.error('\n[build] ERREUR — ' + msg + '\n'); process.exit(1); }

/* --- 1. Lecture (utf8 brut, aucune normalisation : GAME_NOTES est du texte
 *        joueur en UTF-8 litteral, il ne doit etre ni re-encode ni normalise) */
for (const f of [F_SRC, F_SPR]) if (!fs.existsSync(f)) die('source manquante : ' + path.relative(ROOT, f));
const src = fs.readFileSync(F_SRC, 'utf8');
const spr = fs.readFileSync(F_SPR, 'utf8');

/* --- 2. Marqueur present exactement une fois, SEUL SUR SA LIGNE -------------
 * Le compte porte sur les lignes EGALES au marqueur, pas sur les occurrences de
 * la chaine : le marqueur est cite dans les commentaires de version, et une
 * citation ne doit ni faire echouer le build ni servir de point d'injection. */
const occ = src.split('\n').filter(l => l === MARKER).length;
if (occ !== 1) die('le marqueur ' + MARKER + ' apparait ' + occ + ' fois seul sur sa ligne dans src/index.src.html (attendu : 1).');
const parts = src.split(MARKER + '\n');
if (parts.length !== 2) die('le marqueur ' + MARKER + ' n\'est pas seul sur sa ligne dans src/index.src.html.');

/* --- 3. GARDE-FOU : le fichier genere n'a pas ete edite a la main ----------- */
if (fs.existsSync(F_OUT)) {
  const cur = sha(fs.readFileSync(F_OUT));
  const stamp = fs.existsSync(F_STAMP) ? fs.readFileSync(F_STAMP, 'utf8').trim().split(/\s+/)[0] : null;
  if (stamp === null) {
    if (!FORCE) die('aucun .build-stamp : impossible de verifier que ' + path.basename(F_OUT) + '\n'
      + '  n\'a pas ete modifie a la main. Premier build ? relancez avec --force.');
    console.log('[build] .build-stamp absent — poursuite demandee par --force.');
  } else if (stamp !== cur) {
    if (!FORCE) die('le fichier genere a ete modifie a la main ; vos modifications seraient perdues.\n'
      + '  attendu (.build-stamp) : ' + stamp + '\n'
      + '  sur le disque          : ' + cur + '\n'
      + '  -> reportez la correction dans src/, puis relancez.\n'
      + '  -> ou, si la modification est a jeter : node tools/build.js --force');
    console.log('[build] ecart de sha256 IGNORE (--force) : la version disque est ecrasee.');
  }
}

/* --- 4. Injection ---------------------------------------------------------- */
let out = parts[0] + spr + parts[1];

/* --- 5. Banniere en toute premiere ligne (avant <!DOCTYPE) ------------------ */
if (!out.startsWith(BANNER)) out = BANNER + '\n' + out;

fs.writeFileSync(F_OUT, out, 'utf8');

/* --- 6. Empreinte + compte rendu ------------------------------------------- */
const bytes = Buffer.byteLength(out, 'utf8');
const digest = sha(fs.readFileSync(F_OUT));
fs.writeFileSync(F_STAMP, digest + '\n', 'utf8');

const injected = spr.replace(/\n$/, '').split('\n').length;
const blocks = (out.match(/^<script/gm) || []).length;
console.log('[build] ecrit ' + path.basename(F_OUT) + ' : ' + bytes + ' o, ' + (out.split('\n').length - 1) + ' lignes');
console.log('[build] lignes injectees depuis src/sprites-inline.js : ' + injected);
console.log('[build] blocs <script> detectes (^<script) : ' + blocks + (blocks === 7 ? ' — OK' : ' — ATTENDU 7 !'));
console.log('[build] sha256 : ' + digest + '  -> .build-stamp');
if (blocks !== 7) process.exit(1);
