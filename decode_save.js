// Décodeur d'export `ARCHv1:` — réplique EXACTE de decodeSave/_lzwDecode du jeu (13.74).
const fs=require('fs');
function lzwDecode(codes){
  if(!codes.length) return Buffer.alloc(0);
  const dict=[]; for(let i=0;i<256;i++) dict.push(String.fromCharCode(i));
  let next=256, w=String.fromCharCode(codes[0]), out=w;
  for(let i=1;i<codes.length;i++){ const k=codes[i]; let e;
    if(k<dict.length) e=dict[k]; else if(k===dict.length) e=w+w.charAt(0); else throw new Error('lzw');
    out+=e; if(next<0x10000){dict.push(w+e.charAt(0)); next++;} w=e; }
  const b=Buffer.alloc(out.length); for(let i=0;i<out.length;i++) b[i]=out.charCodeAt(i)&0xFF;
  return b;
}
function decodeSave(text){
  const t=String(text).trim();
  if(t.indexOf('ARCHv1:')!==0) return t;
  const packed=Buffer.from(t.slice(7),'base64');
  const codes=new Array(packed.length>>1);
  for(let i=0;i<codes.length;i++) codes[i]=(packed[i*2]<<8)|packed[i*2+1];
  return lzwDecode(codes).toString('utf8');
}
module.exports={decodeSave};
if(require.main===module){
  const json=decodeSave(fs.readFileSync(process.argv[2],'utf8'));
  fs.writeFileSync(process.argv[3],json);
  const d=JSON.parse(json);
  console.log('version:',d.version,' mode:',d.mode,' playTicks:',d.playTicks);
  console.log('elevatorRepaired:',d.elevatorRepaired,' elevatorLevel:',d.elevatorLevel);
  console.log('îles:',(d.islands||[]).map(i=>i.id).join(','));
  console.log('octets JSON:',json.length);
}
