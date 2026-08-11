
// Filet de sécurité : si le jeu n'a pas pris la main au bout de 12 s (cas extrême), on masque
// quand même le splash pour ne pas rester bloqué dessus. Le masquage normal vient de draw().
setTimeout(function(){var s=document.getElementById('splash');if(s&&!window.__splashGone){window.__splashGone=true;s.classList.add('hide');setTimeout(function(){if(s.parentNode)s.parentNode.removeChild(s);},600);}},12000);
