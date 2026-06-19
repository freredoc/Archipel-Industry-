// Archipel Industry — Données terrain des 5 îles
// Extraites pixel par pixel depuis les cartes du map editor
// W=eau  T=terre constructible  M=ressource minière  O=obstacle

const ISLAND_TERRAINS = [

  // Ile 1 — 12 cols x 12 rows
  { id:1, cols:12, rows:12, grid:[
    "WWWWWWWWWWWW",
    "WWWWWTTTWWWW",
    "WWWTTTTTTTWW",
    "WWTTTTMMTTTW",
    "WWTTMMTTTTTW",
    "WWTTTTTTTOTW",
    "WWTOTTTTTTTW",
    "WWTTTTOTTTTW",
    "WWTTTTMMTTWW",
    "WWWTTTTMMTWW",
    "WWWWTTTTTWWW",
    "WWWWWWWWWWWW",
  ]},

  // Ile 2 — 18 cols x 9 rows
  { id:2, cols:18, rows:9, grid:[
    "WWWWWWWWWWWWWWWWWW",
    "WWWWWTTTTTTTTTWWWW",
    "WWWTTTTTTTTTMTTTWW",
    "WWWTTMMMTTOTTMTTWW",
    "WWTTOTMTTTMMTMOTWW",
    "WWTTTTTTTTTTTTTTWW",
    "WWTTTTTTTTTTTTWWWW",
    "WWWWTTTWWWWTTTWWWW",
    "WWWWWWWWWWWWWWWWWW",
  ]},

  // Ile 3 — 14 cols x 14 rows
  { id:3, cols:14, rows:14, grid:[
    "WWWWWWWWWWWWWW",
    "WWTTTTTTWWWWWW",
    "WTTTTMMTTWWWWW",
    "WTTTTOTTTTWWWW",
    "WWTTTTTTTTTWWW",
    "WWWWTTTTTTTTWW",
    "WWWWWWTTOTTTWW",
    "WWWWWWTTTTMTWW",
    "WWWWWWTTOTMTWW",
    "WWWWWWTTTTTTWW",
    "WWWWWWWTTTTTWW",
    "WWWWWWWTTTTTWW",
    "WWWWWWWWTTWWWW",
    "WWWWWWWWWWWWWW",
  ]},

  // Ile 4 — 16 cols x 16 rows
  { id:4, cols:16, rows:16, grid:[
    "WWWWWWWWWWWWWWWW",
    "WWWWWTWTTTTTTWWW",
    "WWWWWTTTOMMTTWWW",
    "WWWWTTTTTTTTTWWW",
    "WWWTTTTTTTTWWWWW",
    "WWWTTOTTWWWWWWWW",
    "WWTTMTTTTWWWWWWW",
    "WWWTMTTTTWWWWWWW",
    "WWWTTMTTTWWWWWWW",
    "WWWTTTTOTTTTTTWW",
    "WWWWTTTTTTTTTTWW",
    "WWWWWTTTTTMMMTWW",
    "WWWWWWWWWWTTTWWW",
    "WWWWWWWWWWWWWWWW",
    "WWWWWWWWWWWWWWWW",
    "WWWWWWWWWWWWWWWW",
  ]},

  // Ile 5 — 16 cols x 16 rows
  { id:5, cols:16, rows:16, grid:[
    "WWWWWWWWWWWWWWWW",
    "WWTTTTWWWWWWWWWW",
    "WTOMTTTTWWWWWWWW",
    "WTTMMMTTWWWWWWWW",
    "WTTTTOTTWWWWWWWW",
    "WTTOTTTTWWWWWWWW",
    "WTTTTTTTWWTWWWWW",
    "WTTTTTTTTTTTWWWW",
    "WWTTTWTTTTTTTWWW",
    "WWWWWWTTOTTTTWWW",
    "WWWWWWTTMMMTTWWW",
    "WWWWWWTTTTOTTWWW",
    "WWWWWWWTTTTTWWWW",
    "WWWWWWWWTTWWWWWW",
    "WWWWWWWWTWWWWWWW",
    "WWWWWWWWWWWWWWWW",
  ]},

];

function charToTerrain(c) {
  return ({ W:'water', T:'land', M:'resource', O:'obstacle' })[c] ?? 'water';
}

function buildIslandTiles(def) {
  return Array.from({ length: def.rows }, (_, row) =>
    Array.from({ length: def.cols }, (_, col) => ({
      terrain: charToTerrain((def.grid[row] || '')[col]),
      building: null,
      networkId: null,
    }))
  );
}
