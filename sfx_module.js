/* ============================================================================
   ARCHIPEL INDUSTRY — MODULE AUDIO (SFX)
   Synthèse procédurale Web Audio. Aucun fichier, aucun CDN, offline-first.
   À inliner tel quel dans le <script> principal du jeu (une seule instance).

   API publique :
     SFX.play(name)                 -> joue un son one-shot
     SFX.playThrottled(name, ms)    -> joue avec cooldown (alertes ; défaut 1500ms)
     SFX.unlock()                   -> crée/reprend l'AudioContext (à appeler au 1er geste, mobile)
     SFX.setEnabled(bool)           -> mute global on/off
     SFX.isEnabled()                -> état mute
     SFX.setVolume(0..1)            -> volume master
     SFX.getVolume()                -> volume actuel
     SFX.ALERTS                     -> liste des sons d'alerte (à jouer via playThrottled)
     SFX.names                      -> liste de tous les sons disponibles
   ============================================================================ */
const SFX = (() => {
  let ctx = null, master = null, volume = 0.55, noiseBuf = null;
  let enabled = true;            // mute global (persisté dans la save)
  const _last = {};             // horodatage du dernier déclenchement par son (cooldown)
  // Sons d'alerte : à jouer via SFX.playThrottled(...) pour éviter la répétition au tick.
  const ALERTS = ['powerAlert','fuelLow','stockFull','noInput','importBlocked'];

  function ensure(){
    if(!ctx){
      ctx = new (window.AudioContext || window.webkitAudioContext)();
      master = ctx.createGain(); master.gain.value = volume; master.connect(ctx.destination);
    }
    if(ctx.state === 'suspended') ctx.resume();
    return ctx;
  }
  function setVolume(v){ volume = v; if(master) master.gain.value = v; }

  function noiseBuffer(){
    if(!noiseBuf){
      const len = ctx.sampleRate;
      noiseBuf = ctx.createBuffer(1, len, ctx.sampleRate);
      const d = noiseBuf.getChannelData(0);
      for(let i=0;i<len;i++) d[i] = Math.random()*2 - 1;
    }
    return noiseBuf;
  }

  // Oscillateur + enveloppe percussive
  function tone({type='sine', freq=440, freqEnd=null, t0=0, attack=0.005, decay=0.08, peak=0.5}){
    const o = ctx.createOscillator(), g = ctx.createGain();
    o.type = type; o.frequency.setValueAtTime(freq, t0);
    if(freqEnd) o.frequency.exponentialRampToValueAtTime(Math.max(1,freqEnd), t0 + attack + decay);
    g.gain.setValueAtTime(0.0001, t0);
    g.gain.linearRampToValueAtTime(peak, t0 + attack);
    g.gain.exponentialRampToValueAtTime(0.0001, t0 + attack + decay);
    o.connect(g); g.connect(master);
    o.start(t0); o.stop(t0 + attack + decay + 0.02);
  }

  // Salve de bruit filtrée
  function noise({t0=0, dur=0.1, peak=0.4, type='lowpass', freq=1000, q=1}){
    const src = ctx.createBufferSource(); src.buffer = noiseBuffer();
    const f = ctx.createBiquadFilter(); f.type = type; f.frequency.value = freq; f.Q.value = q;
    const g = ctx.createGain();
    g.gain.setValueAtTime(0.0001, t0);
    g.gain.linearRampToValueAtTime(peak, t0 + 0.004);
    g.gain.exponentialRampToValueAtTime(0.0001, t0 + dur);
    src.connect(f); f.connect(g); g.connect(master);
    src.start(t0); src.stop(t0 + dur + 0.02);
  }

  // Woosh : bruit avec filtre qui balaye (transitions, carte)
  function woosh({t0=0, dur=0.2, f1=300, f2=1600, peak=0.22, type='bandpass', q=0.8}){
    const src = ctx.createBufferSource(); src.buffer = noiseBuffer(); src.loop = true;
    const f = ctx.createBiquadFilter(); f.type = type; f.Q.value = q;
    f.frequency.setValueAtTime(f1, t0); f.frequency.exponentialRampToValueAtTime(Math.max(1,f2), t0+dur);
    const g = ctx.createGain();
    g.gain.setValueAtTime(0.0001, t0); g.gain.linearRampToValueAtTime(peak, t0+dur*0.3);
    g.gain.exponentialRampToValueAtTime(0.0001, t0+dur);
    src.connect(f); f.connect(g); g.connect(master);
    src.start(t0); src.stop(t0+dur+0.02);
  }

  /* ---------------- LES SONS ---------------- */
  const S = {
    /* --- Interface --- */
    click(){ const t=ctx.currentTime;
      tone({type:'square', freq:1100, freqEnd:900, t0:t, attack:0.001, decay:0.035, peak:0.22}); },
    clickAlt(){ const t=ctx.currentTime;
      tone({type:'triangle', freq:520, freqEnd:380, t0:t, attack:0.001, decay:0.05, peak:0.22}); },
    panelOpen(){ const t=ctx.currentTime;
      tone({type:'triangle', freq:380, freqEnd:880, t0:t, attack:0.005, decay:0.085, peak:0.3}); },
    panelClose(){ const t=ctx.currentTime;
      tone({type:'triangle', freq:880, freqEnd:360, t0:t, attack:0.005, decay:0.085, peak:0.3}); },
    tabSwitch(){ const t=ctx.currentTime;
      tone({type:'triangle', freq:720, freqEnd:760, t0:t, attack:0.002, decay:0.045, peak:0.2}); },
    tabHover(){ const t=ctx.currentTime;
      tone({type:'sine', freq:1400, t0:t, attack:0.001, decay:0.018, peak:0.08}); },
    selectBuilding(){ const t=ctx.currentTime;
      tone({type:'triangle', freq:560, t0:t, attack:0.002, decay:0.04, peak:0.18});
      noise({t0:t, dur:0.02, peak:0.1, type:'highpass', freq:3000}); },
    deselect(){ const t=ctx.currentTime;
      tone({type:'triangle', freq:480, freqEnd:300, t0:t, attack:0.001, decay:0.05, peak:0.18}); },
    toggleOn(){ const t=ctx.currentTime;
      tone({type:'square', freq:760, freqEnd:1020, t0:t, attack:0.002, decay:0.06, peak:0.16}); },
    toggleOff(){ const t=ctx.currentTime;
      tone({type:'square', freq:620, freqEnd:420, t0:t, attack:0.002, decay:0.06, peak:0.16}); },
    notify(){ const t=ctx.currentTime;
      tone({type:'sine', freq:680, freqEnd:880, t0:t, attack:0.003, decay:0.08, peak:0.22}); },
    mapOpen(){ const t=ctx.currentTime;
      woosh({t0:t, dur:0.22, f1:260, f2:1600, peak:0.22});
      tone({type:'sine', freq:520, t0:t+0.08, attack:0.01, decay:0.12, peak:0.12}); },
    islandTransition(){ const t=ctx.currentTime;
      woosh({t0:t, dur:0.3, f1:200, f2:1400, peak:0.24});
      tone({type:'triangle', freq:392, t0:t+0.12, attack:0.02, decay:0.25, peak:0.18});
      tone({type:'sine',     freq:588, t0:t+0.12, attack:0.02, decay:0.25, peak:0.1}); },

    /* --- Pose & Construction --- */
    place(){ const t=ctx.currentTime;
      tone({type:'sine',     freq:200, freqEnd:90,  t0:t, attack:0.002, decay:0.12, peak:0.6});
      tone({type:'triangle', freq:430, freqEnd:300, t0:t, attack:0.001, decay:0.05, peak:0.22});
      noise({t0:t, dur:0.03, peak:0.28, type:'lowpass', freq:2600}); },
    placeHeavy(){ const t=ctx.currentTime;
      tone({type:'sine',     freq:150, freqEnd:60,  t0:t, attack:0.003, decay:0.2, peak:0.7});
      tone({type:'triangle', freq:320, freqEnd:220, t0:t, attack:0.001, decay:0.07, peak:0.2});
      noise({t0:t, dur:0.05, peak:0.3, type:'lowpass', freq:2000});
      noise({t0:t+0.01, dur:0.12, peak:0.18, type:'lowpass', freq:700, q:0.6}); },
    invalid(){ const t=ctx.currentTime;
      tone({type:'square', freq:150, t0:t, attack:0.003, decay:0.16, peak:0.26});
      tone({type:'square', freq:139, t0:t, attack:0.003, decay:0.16, peak:0.22}); },
    demolish(){ const t=ctx.currentTime;
      noise({t0:t, dur:0.16, peak:0.5, type:'lowpass', freq:1100, q:0.7});
      tone({type:'sine', freq:120, freqEnd:55, t0:t, attack:0.002, decay:0.14, peak:0.4}); },
    upgrade(){ const t=ctx.currentTime; const n=[523,659,784];
      n.forEach((f,i)=> tone({type:'triangle', freq:f, t0:t+i*0.06, attack:0.003, decay:0.1, peak:0.3})); },
    // baisse de niveau bâtiment : arpège DESCENDANT, plus mat (régression volontaire, pas une erreur)
    downgrade(){ const t=ctx.currentTime; const n=[659,523,392];
      n.forEach((f,i)=> tone({type:'triangle', freq:f, t0:t+i*0.06, attack:0.003, decay:0.1, peak:0.24})); },
    // réserve : broum bam calé sur la voix (~0.84s), mis de côté au profit du thock
    placeC(){ const t=ctx.currentTime;
      tone({type:'sine',     freq:160, freqEnd:62,  t0:t, attack:0.002, decay:0.24, peak:0.7});
      tone({type:'triangle', freq:220, freqEnd:110, t0:t, attack:0.001, decay:0.08, peak:0.25});
      noise({t0:t, dur:0.18, peak:0.3, type:'lowpass', freq:320, q:0.7});
      noise({t0:t, dur:0.03, peak:0.32, type:'lowpass', freq:2200});
      const tb=t+0.55;
      tone({type:'sine', freq:200, freqEnd:58, t0:tb, attack:0.001, decay:0.26, peak:0.8});
      noise({t0:tb, dur:0.04, peak:0.45, type:'lowpass', freq:2200});
      tone({type:'sine', freq:90,  freqEnd:45, t0:tb, attack:0.002, decay:0.28, peak:0.34}); },

    /* --- Réseaux --- */
    road(){ const t=ctx.currentTime;
      noise({t0:t, dur:0.05, peak:0.32, type:'bandpass', freq:1400, q:1.2});
      tone({type:'triangle', freq:260, t0:t, attack:0.001, decay:0.03, peak:0.14}); },
    cable(){ const t=ctx.currentTime;
      noise({t0:t, dur:0.04, peak:0.24, type:'bandpass', freq:1200, q:1});
      for(let i=0;i<4;i++) noise({t0:t+0.01+i*0.014, dur:0.012, peak:0.16, type:'highpass', freq:3600}); },
    pipe(){ const t=ctx.currentTime;
      tone({type:'sine', freq:300, freqEnd:175, t0:t, attack:0.002, decay:0.09, peak:0.4});
      noise({t0:t, dur:0.05, peak:0.1, type:'bandpass', freq:500, q:3}); },
    junction(){ const t=ctx.currentTime;
      tone({type:'triangle', freq:600, t0:t,      attack:0.001, decay:0.025, peak:0.18});
      tone({type:'triangle', freq:760, t0:t+0.05, attack:0.001, decay:0.025, peak:0.18}); },
    networkSplit(){ const t=ctx.currentTime;
      tone({type:'triangle', freq:560, freqEnd:360, t0:t, attack:0.002, decay:0.14, peak:0.2}); },
    upgradeNetwork(){ const t=ctx.currentTime;
      [440,587].forEach((f,i)=> tone({type:'triangle', freq:f, t0:t+i*0.06, attack:0.003, decay:0.09, peak:0.22})); },
    downgradeNetwork(){ const t=ctx.currentTime;
      [587,440].forEach((f,i)=> tone({type:'triangle', freq:f, t0:t+i*0.06, attack:0.003, decay:0.09, peak:0.18})); },

    /* --- Progression --- */
    nodeReady(){ const t=ctx.currentTime;
      tone({type:'sine', freq:659, t0:t,      attack:0.01, decay:0.18, peak:0.18});
      tone({type:'sine', freq:988, t0:t+0.02, attack:0.01, decay:0.16, peak:0.1}); },
    unlock(){ const t=ctx.currentTime; const n=[392,523,659,784];
      n.forEach((f,i)=>{
        tone({type:'square', freq:f, t0:t+i*0.07, attack:0.004, decay:0.12, peak:0.16});
        tone({type:'sine',   freq:f, t0:t+i*0.07, attack:0.004, decay:0.14, peak:0.22}); });
      tone({type:'sine', freq:1047, t0:t+0.29, attack:0.01, decay:0.26, peak:0.24}); },
    delivery(){ const t=ctx.currentTime;
      tone({type:'sine', freq:880,  t0:t,       attack:0.002, decay:0.18, peak:0.22});
      tone({type:'sine', freq:1320, t0:t+0.005, attack:0.002, decay:0.22, peak:0.12}); },
    buildingUnlock(){ const t=ctx.currentTime;
      [587,784].forEach((f,i)=>{
        tone({type:'triangle', freq:f,   t0:t+i*0.08, attack:0.003, decay:0.14, peak:0.2});
        tone({type:'sine',     freq:f*2, t0:t+i*0.08, attack:0.003, decay:0.1,  peak:0.08}); }); },
    boatUnlock(){ const t=ctx.currentTime;
      [523,659,880].forEach((f,i)=> tone({type:'sine', freq:f, t0:t+i*0.07, attack:0.004, decay:0.2, peak:0.18})); },
    prodMilestone(){ const t=ctx.currentTime;
      tone({type:'sine', freq:784, freqEnd:1046, t0:t, attack:0.003, decay:0.1, peak:0.2}); },
    // DÉBLOCAGE ÎLE ★ — version longue & gratifiante (~1.5s) : arpège + suspension -> résolution majeure ample
    islandUnlock(){ const t=ctx.currentTime;
      [261.63,329.63,392.00,523.25,659.25].forEach((f,i)=>
        tone({type:'triangle', freq:f, t0:t+i*0.08, attack:0.004, decay:0.18, peak:0.2}));
      const t1=t+0.42;                              // accord suspendu (sous-dominante)
      [349.23,440,523.25].forEach(f=> tone({type:'triangle', freq:f, t0:t1, attack:0.012, decay:0.4, peak:0.14}));
      const t2=t+0.72;                              // résolution majeure tenue
      [392,523.25,659.25,783.99].forEach(f=>{
        tone({type:'sine',     freq:f, t0:t2, attack:0.012, decay:0.75, peak:0.2});
        tone({type:'triangle', freq:f, t0:t2, attack:0.012, decay:0.6,  peak:0.11}); });
      tone({type:'sine', freq:130.81, t0:t2, attack:0.008, decay:0.8, peak:0.42});   // basse
      tone({type:'sine', freq:196,    t0:t2, attack:0.008, decay:0.5, peak:0.16});
      [1046.50,1318.51,1567.98,2093].forEach((f,i)=>                                  // sparkle prolongé
        tone({type:'sine', freq:f, t0:t2+0.06+i*0.06, attack:0.002, decay:0.28, peak:0.11})); },
    // ENDGAME / ÎLE 6 ★★ — le plus solennel (~1.4s)
    endgameUnlock(){ const t=ctx.currentTime;
      [196,293.66,392].forEach(f=> tone({type:'triangle', freq:f, t0:t, attack:0.02, decay:0.5, peak:0.16}));
      const tc=t+0.45;
      [261.63,392,523.25,659.25].forEach(f=>{
        tone({type:'sine',     freq:f, t0:tc, attack:0.015, decay:0.8, peak:0.18});
        tone({type:'triangle', freq:f, t0:tc, attack:0.015, decay:0.6, peak:0.1}); });
      tone({type:'sine', freq:130.81, t0:tc, attack:0.01, decay:0.85, peak:0.45});
      tone({type:'sine', freq:65.41,  t0:tc, attack:0.01, decay:0.7,  peak:0.3});
      [1046,1318,1568,2093].forEach((f,i)=>
        tone({type:'sine', freq:f, t0:tc+0.08+i*0.06, attack:0.002, decay:0.25, peak:0.1})); },

    /* --- Alertes --- */
    powerAlert(){ const t=ctx.currentTime;
      const beep=(t0)=>{
        tone({type:'square', freq:233, freqEnd:185, t0:t0, attack:0.004, decay:0.14, peak:0.2});
        tone({type:'sine',   freq:116, freqEnd:92,  t0:t0, attack:0.004, decay:0.14, peak:0.18}); };
      beep(t); beep(t+0.2); },
    stockFull(){ const t=ctx.currentTime;
      tone({type:'triangle', freq:330, t0:t,      attack:0.004, decay:0.12, peak:0.18});
      tone({type:'triangle', freq:330, t0:t+0.16, attack:0.004, decay:0.12, peak:0.16}); },
    noInput(){ const t=ctx.currentTime;
      tone({type:'triangle', freq:440, freqEnd:392, t0:t, attack:0.002, decay:0.07, peak:0.16}); },
    fuelLow(){ const t=ctx.currentTime;
      const beep=(t0)=>{ tone({type:'square', freq:180, freqEnd:150, t0:t0, attack:0.004, decay:0.16, peak:0.2});
                         tone({type:'sine', freq:90, t0:t0, attack:0.004, decay:0.16, peak:0.16}); };
      beep(t); beep(t+0.22); beep(t+0.44); },
    importBlocked(){ const t=ctx.currentTime;
      tone({type:'square', freq:400, freqEnd:240, t0:t, attack:0.003, decay:0.16, peak:0.18}); },
    normalRestored(){ const t=ctx.currentTime;
      tone({type:'sine', freq:523, freqEnd:659, t0:t, attack:0.01, decay:0.22, peak:0.16}); },
    // explosion d'un bâtiment en surchauffe : déflagration grave + crack + débris + vapeur (~0.6s)
    overheatExplosion(){ const t=ctx.currentTime;
      tone({type:'sine',     freq:140, freqEnd:34, t0:t, attack:0.001, decay:0.5,  peak:0.7});   // onde de choc
      tone({type:'triangle', freq:95,  freqEnd:30, t0:t, attack:0.001, decay:0.42, peak:0.28});
      noise({t0:t, dur:0.09, peak:0.55, type:'lowpass', freq:2400});                              // crack initial
      noise({t0:t, dur:0.6,  peak:0.4,  type:'lowpass', freq:900, q:0.6});                        // grondement / débris
      noise({t0:t+0.06, dur:0.55, peak:0.15, type:'highpass', freq:3800}); },                     // sifflement de vapeur

    /* --- Système & slots --- */
    save(){ const t=ctx.currentTime;
      tone({type:'sine', freq:660, t0:t,      attack:0.004, decay:0.07, peak:0.24});
      tone({type:'sine', freq:990, t0:t+0.08, attack:0.004, decay:0.09, peak:0.27}); },
    load(){ const t=ctx.currentTime;
      tone({type:'sine', freq:440, freqEnd:660, t0:t, attack:0.006, decay:0.16, peak:0.2}); },
    slotCreate(){ const t=ctx.currentTime;
      tone({type:'sine', freq:600, freqEnd:840, t0:t, attack:0.003, decay:0.09, peak:0.2}); },
    slotDelete(){ const t=ctx.currentTime;
      tone({type:'triangle', freq:380, freqEnd:200, t0:t, attack:0.002, decay:0.14, peak:0.2});
      noise({t0:t, dur:0.06, peak:0.15, type:'lowpass', freq:1200}); },
    errorGeneric(){ const t=ctx.currentTime;
      tone({type:'square', freq:160, t0:t, attack:0.003, decay:0.12, peak:0.22});
      tone({type:'square', freq:151, t0:t, attack:0.003, decay:0.12, peak:0.18}); },
    titleSting(){ const t=ctx.currentTime;
      [392,523,659].forEach((f,i)=> tone({type:'triangle', freq:f, t0:t+i*0.1, attack:0.006, decay:0.3, peak:0.18}));
      tone({type:'sine', freq:1046, t0:t+0.32, attack:0.01, decay:0.4, peak:0.14}); },
  };

  // Joue immédiatement (respecte le mute). Crée/reprend le contexte si besoin.
  function play(name){
    if(!enabled) return;
    ensure();
    if(S[name]) { try { S[name](); } catch(e){ /* audio indispo : on n'interrompt jamais le jeu */ } }
  }
  // Joue avec cooldown : ignore l'appel si le même son a sonné il y a moins de minMs.
  function playThrottled(name, minMs=1500){
    const now = (typeof performance!=='undefined' ? performance.now() : Date.now());
    if(_last[name] && (now - _last[name]) < minMs) return;
    _last[name] = now;
    play(name);
  }
  function setEnabled(v){ enabled = !!v; }
  function isEnabled(){ return enabled; }
  function getVolume(){ return volume; }
  function unlock(){ try { ensure(); } catch(e){} }   // à appeler au 1er geste utilisateur (mobile)

  return { play, playThrottled, unlock, setEnabled, isEnabled, setVolume, getVolume,
           ALERTS, names:Object.keys(S) };
})();
