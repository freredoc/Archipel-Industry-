#!/usr/bin/env node
/* =============================================================================
 * Lot S — SPLITTER A USAGE UNIQUE.
 *
 * Sort du monolithe les lignes d'assignation pure de data-URL et produit :
 *   src/sprites-inline.js   les lignes extraites (+ les commentaires qui les documentent)
 *   src/index.src.html      le residuel, avec le marqueur <!--@@SPRITES_INLINE@@-->
 *
 * Il est COMMITE pour la tracabilite : c'est lui qui repond a la question
 * « d'ou vient exactement le decoupage ? ». Il n'est PAS rejoue au quotidien
 * (le build normal, c'est tools/build.js).
 *
 * Usage : node tools/split-once.js [chemin-du-monolithe]
 * ========================================================================== */
'use strict';
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const SRC = process.argv[2] || path.join(ROOT, 'Archipel_industry_alpha-7.html');
const OUT_SRC = path.join(ROOT, 'src', 'index.src.html');
const OUT_SPR = path.join(ROOT, 'src', 'sprites-inline.js');
const MARKER = '<!--@@SPRITES_INLINE@@-->';

/* --- Regle de deplacement (brief §4), mecanique, sans jugement ------------- */
const RE_ASSIGN = /^\s*window\.__(SPRITE|ANIM)_DATA__(\[|=\{)/;
const isMovedLine = l => RE_ASSIGN.test(l) && l.includes('data:image/');

/* Une ligne « commentish » : vide, // , ou une ligne de bloc /* ... *\/.
 * On ne l'evalue qu'en REMONTANT depuis une ligne deplacee, donc jamais au
 * milieu d'une chaine : le fichier n'a aucune ligne de code commencant par * . */
function commentish(l) {
  const t = l.trim();
  return t === '' || t.startsWith('//') || t.startsWith('/*') || t.startsWith('*');
}

const raw = fs.readFileSync(SRC, 'utf8');
const lines = raw.split('\n');
const n = lines.length;
const moved = new Array(n).fill(false);
for (let i = 0; i < n; i++) moved[i] = isMovedLine(lines[i]);
const movedCount = moved.filter(Boolean).length;

/* --- Liste de RETENTION (brief §4) : ces commentaires restent dans le residuel
 * parce qu'ils documentent du code qui, lui, ne bouge pas. ------------------ */
const retained = new Array(n).fill(false);
const retainedBlocks = [];
function retain(from, to, why) {
  for (let i = from; i <= to; i++) {
    if (!commentish(lines[i])) throw new Error('RETENTION sur une ligne non-commentaire ' + (i + 1) + ' : ' + lines[i].slice(0, 80));
    retained[i] = true;
  }
  retainedBlocks.push({ from: from + 1, to: to + 1, why, text: lines[from].slice(0, 78) });
}
const iAnimData = lines.findIndex(l => /^const ANIM_DATA = typeof window/.test(l));
if (iAnimData < 0) throw new Error('ancre `const ANIM_DATA` introuvable');
retain(iAnimData - 3, iAnimData - 1, "commentaire de `const ANIM_DATA`");

const iTdz = lines.findIndex(l => /^\/\/ ⚠ 14\.95 — CET APPEL DOIT RESTER APRÈS/.test(l));
if (iTdz < 0) throw new Error('ancre ⚠ 14.95 introuvable');
retain(iTdz, iTdz + 2, "⚠ 14.95 — zone morte temporelle d'ANIM_META");

const titles = [];
for (let i = 0; i < n; i++) if (/^\/\/ --- (ANIM_META|spritesheets)/.test(lines[i])) titles.push(i);
if (titles.length !== 5) throw new Error('attendu 5 commentaires titrant les Object.assign(ANIM_META), vu ' + titles.length);
titles.forEach(i => retain(i, i, 'titre au-dessus d\'un Object.assign(ANIM_META, …) / de spritesheets'));

/* --- Attachement des commentaires (brief §4) : un bloc contigu de lignes
 * blanches/commentaires precedant immediatement une serie de lignes deplacees
 * voyage avec elles, SAUF s'il est retenu ci-dessus. ------------------------ */
const taken = moved.slice();
for (let i = 0; i < n; i++) {
  if (!moved[i]) continue;
  if (i > 0 && moved[i - 1]) continue;          // pas un debut de serie
  for (let j = i - 1; j >= 0; j--) {
    if (taken[j] || retained[j] || !commentish(lines[j])) break;
    taken[j] = true;
  }
}
const attachedCount = taken.filter(Boolean).length - movedCount;

/* --- Lignes de renvoi (brief §4) : une seule ligne, sous chaque commentaire
 * RETENU dont les donnees sont parties. Table EXPLICITE : les deux titres
 * `// --- ANIM_META : …` n'en recoivent pas, leurs donnees (le litteral
 * ANIM_META) restent juste en dessous. ------------------------------------- */
const RENVOI = '// (data-URL déplacées dans src/sprites-inline.js — cf. tools/build.js)';
const renvoiAfter = new Set();
const renvoiLog = [];
function renvoi(idx, why) {
  renvoiAfter.add(idx);
  renvoiLog.push({ line: idx + 1, after: lines[idx].slice(0, 72), why });
}
renvoi(iAnimData, 'les sheets decrites juste au-dessus sont parties');
renvoi(iTdz + 2, 'le pack de sheets qui suivait le ⚠ est parti');
titles.filter(i => /^\/\/ --- spritesheets/.test(lines[i])).forEach(i => renvoi(i, 'les spritesheets titrees ici sont parties'));

/* --- Ecriture --------------------------------------------------------------*/
const HEADER_SPR = [
  '// ===========================================================================',
  '// FICHIER SOURCE — injecte tel quel dans le monolithe par tools/build.js, a la',
  '// place du marqueur @@SPRITES_INLINE@@ du bloc <script> n°5.',
  '// Contenu : les seules lignes d\'assignation de data-URL de window.__SPRITE_DATA__',
  '// et window.__ANIM_DATA__, dans leur ordre d\'origine (le DERNIER override gagne),',
  '// avec les commentaires qui les documentent. Aucune logique de jeu ici.',
  '// ==========================================================================='
];

const spritesOut = [];
const residualOut = [];
let markerPlaced = false;
for (let i = 0; i < n; i++) {
  if (taken[i]) {
    if (!markerPlaced) { residualOut.push(MARKER); markerPlaced = true; }
    spritesOut.push(lines[i]);
  } else {
    residualOut.push(lines[i]);
    if (renvoiAfter.has(i)) residualOut.push(RENVOI);
  }
}
if (!markerPlaced) throw new Error('aucune ligne extraite');

fs.mkdirSync(path.join(ROOT, 'src'), { recursive: true });
fs.writeFileSync(OUT_SPR, HEADER_SPR.join('\n') + '\n' + spritesOut.join('\n') + '\n', 'utf8');
fs.writeFileSync(OUT_SRC, residualOut.join('\n'), 'utf8');

/* --- Compte rendu ----------------------------------------------------------*/
const firstTaken = taken.indexOf(true);
console.log('source            : ' + path.relative(ROOT, SRC));
console.log('lignes source     : ' + (n - 1) + ' (+ fin de fichier)');
console.log('lignes DEPLACEES  : ' + movedCount + '  (attendu 1292)');
console.log('  bloc 5 / bloc 7 : ' + moved.slice(0, 3075).filter(Boolean).length + ' / ' + moved.slice(3075).filter(Boolean).length);
console.log('commentaires attaches : ' + attachedCount);
console.log('marqueur pose ligne   : ' + (firstTaken + 1) + ' (1re ligne extraite)');
console.log('blocs de commentaires RETENUS : ' + retainedBlocks.length);
retainedBlocks.forEach(b => console.log('   L' + b.from + (b.to !== b.from ? '-' + b.to : '') + '  ' + b.why));
console.log('lignes de RENVOI ajoutees : ' + renvoiLog.length);
renvoiLog.forEach(r => console.log('   apres L' + r.line + '  « ' + r.after + ' »'));
console.log('ecrit : src/sprites-inline.js  ' + fs.statSync(OUT_SPR).size + ' o, ' + (spritesOut.length + HEADER_SPR.length) + ' lignes');
console.log('ecrit : src/index.src.html     ' + fs.statSync(OUT_SRC).size + ' o, ' + residualOut.length + ' lignes');
