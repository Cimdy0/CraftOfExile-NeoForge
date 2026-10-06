// 详情页结构勘察：不臆造选择器，先看真实 HTML
const fs = require('fs');
const path = require('path');

const file = process.argv[2] || 'design/refactor/skill-data/crawled/raw/probe/us__Solar_Orb.html';
const h = fs.readFileSync(file, 'utf8');
console.log('FILE', file, 'LEN', h.length);

const markers = ['Level Effect', 'levelTbl', 'ModsView', 'new ModsView', 'data-tag',
  'Quality', 'Recommended', 'Requires', 'Cast Time', 'Critical Strike', 'Mana',
  'Spirit', 'Reservation', 'Radius', 'Duration', 'id="'];
for (const m of markers) console.log('  marker', JSON.stringify(m).padEnd(22), h.indexOf(m));

console.log('\n--- <table> occurrences ---');
let idx = 0, n = 0;
while ((idx = h.indexOf('<table', idx + 1)) > 0 && n < 6) {
  console.log('@' + idx, JSON.stringify(h.slice(idx, idx + 400)));
  n++;
}

console.log('\n--- <table class=...> class list ---');
const cls = new Set();
const re = /<table[^>]*class="([^"]*)"/g;
let m;
while ((m = re.exec(h))) cls.add(m[1]);
console.log([...cls].join('\n'));
