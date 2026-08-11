
(function(){
  // ── Police désormais EMBARQUÉE en @font-face base64 → aucune dépendance réseau. ──
  var FONTS_CSS  = '';

  // ── Génère une icône PWA (motif île + usine) sans fichier externe ──
  function makeIcon(size){
    var c = document.createElement('canvas'); c.width = c.height = size;
    var x = c.getContext('2d'); var u = size/16; // unité de grille
    x.fillStyle = '#0d0d1a'; x.fillRect(0,0,size,size);
    x.fillStyle = '#3A6A5A'; x.fillRect(2*u,3*u,12*u,10*u);        // côte
    x.fillStyle = '#4A7A3A'; x.fillRect(3*u,4*u,10*u,8*u);         // terre
    x.fillStyle = '#7A6040'; x.fillRect(5*u,5*u,3*u,3*u);          // gisement
    x.fillStyle = '#f0d060';                                       // « usine »
    x.fillRect(9*u,7*u,4*u,5*u); x.fillRect(8*u,9*u,2*u,3*u);
    x.fillStyle = '#FF5722'; x.fillRect(10*u,5*u,1.4*u,2.4*u);     // cheminée
    return c.toDataURL('image/png');
  }

  // ── Manifest minimal injecté via blob URL (supporté par les navigateurs) ──
  try{
    var icon192 = makeIcon(192), icon512 = makeIcon(512);
    var manifest = {
      name:'Archipel Industry', short_name:'Archipel', start_url:'.',
      display:'standalone', orientation:'any',
      background_color:'#0d0d1a', theme_color:'#0d0d1a',
      icons:[
        { src:icon192, sizes:'192x192', type:'image/png' },
        { src:icon512, sizes:'512x512', type:'image/png' }
      ]
    };
    var mBlob = new Blob([JSON.stringify(manifest)], {type:'application/manifest+json'});
    var link = document.createElement('link');
    link.rel = 'manifest'; link.href = URL.createObjectURL(mBlob);
    if (!document.querySelector('link[rel="manifest"]')) document.head.appendChild(link);
  }catch(e){ /* manifest non critique pour le fonctionnement du jeu */ }

  // ── Service Worker inline (cache shell pour le mode hors ligne) ──
  // IMPORTANT / LIMITATION CONNUE :
  //   Chrome et Firefox REFUSENT d'enregistrer un Service Worker dont le
  //   script provient d'une URL blob: ou data: (le script doit être
  //   same-origin et servi avec un type JS). L'enregistrement ci-dessous
  //   échouera donc silencieusement sur ces navigateurs — c'est attendu.
  //   Pour un offline 100% garanti : héberger le jeu en HTTPS avec un vrai
  //   fichier sw.js (ou inliner React/Babel/polices dans le HTML).
  //   La sauvegarde localStorage reste persistante hors-ligne dans tous les cas.
  var SW_SRC = [
    "var CACHE='archipel-shell-v1';",
    "var PRECACHE=" + JSON.stringify([FONTS_CSS].filter(Boolean)) + ";",
    "self.addEventListener('install',function(e){",
    "  e.waitUntil(caches.open(CACHE).then(function(c){return c.addAll(PRECACHE).catch(function(){});}).then(function(){return self.skipWaiting();}));",
    "});",
    "self.addEventListener('activate',function(e){",
    "  e.waitUntil((async function(){var ks=await caches.keys();await Promise.all(ks.filter(function(k){return k!==CACHE;}).map(function(k){return caches.delete(k);}));await self.clients.claim();})());",
    "});",
    "self.addEventListener('fetch',function(e){",
    "  var req=e.request; if(req.method!=='GET') return;",
    "  e.respondWith((async function(){",
    "    var cache=await caches.open(CACHE);",
    "    var cached=await cache.match(req);",
    "    if(cached){ fetch(req).then(function(r){ if(r&&r.ok) cache.put(req,r.clone()); }).catch(function(){}); return cached; }",
    "    try{ var r=await fetch(req); if(r&&(r.ok||r.type==='opaque')) cache.put(req,r.clone()); return r; }",
    "    catch(err){ if(req.mode==='navigate'){ var shell=await cache.match(self.registration.scope); if(shell) return shell; } throw err; }",
    "  })());",
    "});"
  ].join('\n');

  // Garde : pas de SW en file:// (et jamais d'erreur visible)
  if ('serviceWorker' in navigator && location.protocol.indexOf('http') === 0){
    try{
      var swUrl = './sw.js';
      navigator.serviceWorker.register(swUrl).then(function(){
        // Réchauffe le cache du HTML une fois le SW actif (pour l'offline au 2e chargement)
        return navigator.serviceWorker.ready;
      }).then(function(){
        try{ fetch(location.href, {cache:'reload'}).catch(function(){}); }catch(_){}
      }).catch(function(){ /* blob SW bloqué : voir la note ci-dessus */ });
    }catch(e){ /* SW indisponible : on continue sans, le jeu reste jouable */ }
  }
})();
