/* ════════════════════════════════════════════════════════════════
   Archipel Industry — Service Worker
   Fichier RÉEL servi en same-origin : contrairement au SW inline en
   blob: (refusé par tous les navigateurs), celui-ci s'enregistre
   vraiment. C'est lui qui garantit le mode hors ligne sur iOS.

   Le jeu est un HTML unique auto-suffisant (polices en base64,
   React inliné, zéro CDN) → le précache tient en 2 entrées.

   ⚠ BUMPER `CACHE` À CHAQUE MISE À JOUR DU JEU (voir GAME_BUILD).
     Sinon les joueurs gardent l'ancienne version en cache.
   ════════════════════════════════════════════════════════════════ */

// ⚠ Ligne réécrite AUTOMATIQUEMENT par la CI (étape « Sync PWA ») à partir de
//   GAME_BUILD. Ne rien ajouter après le `;` : l'ancre sed/grep exige une fin de
//   ligne immédiate.
var CACHE = 'archipel-242';

var SHELL = './';                     // page de navigation (index.html)

var PRECACHE = [
  './',
  './index.html',
  './manifest.json',
  './icon-192.png',
  './icon-512.png'
];

// ── Install : précache le shell, puis prend la main immédiatement ──
self.addEventListener('install', function (e) {
  e.waitUntil(
    caches.open(CACHE)
      .then(function (c) {
        // addAll est atomique : si UNE entrée échoue, tout échoue.
        // On tolère les manquants pour ne jamais bloquer l'install.
        return Promise.all(PRECACHE.map(function (u) {
          return c.add(new Request(u, { cache: 'reload' })).catch(function () {});
        }));
      })
      .then(function () { return self.skipWaiting(); })
  );
});

// ── Activate : purge les anciens caches (bump de version) ──
self.addEventListener('activate', function (e) {
  e.waitUntil(
    caches.keys()
      .then(function (keys) {
        return Promise.all(keys.filter(function (k) {
          return k !== CACHE;
        }).map(function (k) {
          return caches.delete(k);
        }));
      })
      .then(function () { return self.clients.claim(); })
  );
});

// ── Fetch ──
// Navigation  → network-first, repli cache (permet de voir une MAJ en ligne,
//               tout en restant jouable en mode avion).
// Le reste    → cache-first + revalidation en arrière-plan (stale-while-revalidate).
self.addEventListener('fetch', function (e) {
  var req = e.request;
  if (req.method !== 'GET') return;

  // On ignore tout ce qui n'est pas same-origin (ex. version.json sur GitHub,
  // téléchargement d'APK) : ça doit toujours passer par le réseau.
  var url;
  try { url = new URL(req.url); } catch (_) { return; }
  if (url.origin !== self.location.origin) return;

  if (req.mode === 'navigate') {
    e.respondWith(
      fetch(req)
        .then(function (r) {
          if (r && r.ok) {
            var copy = r.clone();
            caches.open(CACHE).then(function (c) { c.put(SHELL, copy); });
          }
          return r;
        })
        .catch(function () {
          return caches.open(CACHE).then(function (c) {
            return c.match(SHELL) || c.match('./index.html');
          });
        })
    );
    return;
  }

  e.respondWith(
    caches.open(CACHE).then(function (c) {
      return c.match(req).then(function (cached) {
        var net = fetch(req).then(function (r) {
          if (r && r.ok) c.put(req, r.clone());
          return r;
        }).catch(function () { return cached; });
        return cached || net;
      });
    })
  );
});

// ── Permet à la page de forcer l'activation d'un SW en attente ──
self.addEventListener('message', function (e) {
  if (e.data === 'SKIP_WAITING') self.skipWaiting();
});
