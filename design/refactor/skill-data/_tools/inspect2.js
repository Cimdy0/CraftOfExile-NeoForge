const fs = require('fs');
const f = process.argv[2] || 'design/refactor/skill-data/crawled/raw/probe/us__Solar_Orb.html';
const h = fs.readFileSync(f, 'utf8');

function dump(label, start, len) {
  console.log('\n========== ' + label + ' @' + start + ' ==========');
  console.log(h.slice(start, start + len).replace(/\s+/g, ' '));
}
dump('STAT BLOCK (Requires)', 26600, 2200);
dump('QUALITY', 29950, 1400);
dump('LEVEL TABLE HEAD', 38091, 1500);
dump('LEVEL TABLE ROW2', 41000, 900);
dump('METADATA', 45355, 900);
