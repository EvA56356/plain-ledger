const fs=require('fs'),vm=require('vm'),assert=require('assert/strict');
const source=fs.readFileSync(__dirname+'/web/app.js','utf8').split("$('#entryForm').onsubmit=")[0];
const context=vm.createContext({console,document:{querySelector:()=>({value:'2026-10'})},window:{},setTimeout,clearTimeout});
vm.runInContext(fs.readFileSync(__dirname+'/web/categories.js','utf8'),context);
vm.runInContext(fs.readFileSync(__dirname+'/web/projects.js','utf8'),context);
vm.runInContext(source,context);
let count=0;
function test(name,fn){fn();count++;console.log('PASS '+name)}
const run=s=>vm.runInContext(s,context);
test('money uses exact integer cents',()=>{assert.equal(run("cents('0.10')+cents('0.20')"),30);assert.equal(run("cents('123.45')"),12345)});
test('reject zero, negative, excess decimals, overflow',()=>{for(const n of ['0','-1','0.001','1000000000','NaN','1e3'])assert.throws(()=>run(`cents('${n}')`))});
run("state=initial(); book='daily'; state.entries=[{id:'t1',type:'expense',amount:3280,category:'e0',account:'cash',book:'daily',date:'2026-10-15',note:'test'},{id:'t2',type:'income',amount:10000,category:'i0',account:'cash',book:'daily',date:'2026-10-15',note:'test'}]");
test('account balance and monthly totals',()=>{assert.equal(run("balance('cash')"),6720);assert.equal(run('totals(monthEntries()).expense'),3280);assert.equal(run('totals(monthEntries()).income'),10000)});
test('transfer conserves total assets and does not inflate income',()=>{run("state.transfers=[{id:'x',from:'cash',to:'bank',amount:2000,date:'2026-10-15'}]");assert.equal(run("balance('cash')"),4720);assert.equal(run("balance('bank')"),2000);assert.equal(run("state.accounts.reduce((n,a)=>n+balance(a.id),0)"),6720);assert.equal(run('totals(monthEntries()).income'),10000)});
test('backup validates references and types',()=>{assert.ok(run('validState(state)'));assert.throws(()=>run("validState({...state,entries:[{...state.entries[0],account:'missing'}]})"));assert.throws(()=>run("validState({...state,entries:[{...state.entries[0],category:'i0'}]})"))});
test('backup rejects invalid date and transfer',()=>{assert.throws(()=>run("validState({...state,entries:[{...state.entries[0],date:'2026-02-30'}]})"));assert.throws(()=>run("validState({...state,transfers:[{id:'x',from:'cash',to:'cash',amount:1,date:'2026-10-15'}]})"))});
test('book and month isolation',()=>{run("state.entries.push({...state.entries[0],id:'other',book:'other',amount:999});state.entries.push({...state.entries[0],id:'past',date:'2026-09-01',amount:999})");assert.equal(run('totals(monthEntries()).expense'),3280)});
test('default classification has 34 expense and 10 income categories',()=>{assert.equal(run("initial().categories.filter(c=>c.type==='expense'&&!c.parentId).length"),34);assert.equal(run("initial().categories.filter(c=>c.type==='income'&&!c.parentId).length"),10)});
test('compact category view preserves all existing categories',()=>{assert.equal(run("state=initial(); visibleCategories('expense').length"),16);assert.equal(run("visibleCategories('income').length"),6);assert.equal(run('state.categories.filter(c=>!c.parentId).length'),44)});
test('editing an uncommon category keeps its current selection visible',()=>{assert.equal(run("visibleCategories('expense','e4').some(c=>c.id==='e4')"),true)});
test('custom categories remain visible in compact view',()=>{run("state.categories.push({id:'custom',name:'custom',type:'expense',icon:'◇'})");assert.equal(run("visibleCategories('expense').some(c=>c.id==='custom')"),true)});
test('expanded view exposes all categories without deleting or changing data',()=>{assert.equal(run("expandedCategoryGroups.expense=true; visibleCategories('expense').length"),35);assert.equal(run('state.categories.filter(c=>!c.parentId).length'),45)});

 test('every root has defaults and travel includes food and shopping',()=>{run('state=initial()');assert.ok(run('state.categories.filter(c=>!c.parentId).every(p=>state.categories.some(c=>c.parentId===p.id))'));assert.ok(run("['餐饮','购物'].every(n=>state.categories.some(c=>c.parentId==='e15'&&c.name===n))"))});
 test('migration preserves entries and is idempotent',()=>{assert.ok(run("(()=>{const d=baseInitial();d.entries=[{id:'legacy',category:'e0',amount:123}];const before=JSON.stringify(d.entries);ensureSubcategories(d);const n=d.categories.length;ensureSubcategories(d);return before===JSON.stringify(d.entries)&&n===d.categories.length})()"))});
 test('custom child validates, round trips and shows its full path',()=>{run("state=initial();state.categories.push({id:'custom-child',name:'纪念品',type:'expense',parentId:'e15'});state.entries=[{id:'child-entry',type:'expense',amount:123,category:'custom-child',account:'cash',book:'daily',date:'2026-10-18',note:''}]");assert.ok(run('validState(JSON.parse(JSON.stringify(state)))'));assert.equal(run("category('custom-child').name"),'旅行 / 纪念品');assert.equal(run('totals(state.entries).expense'),123)});
 test('reject orphan, cycle, third level and mismatched parent type',()=>{for(const parentId of ['missing','custom-child','i0'])assert.throws(()=>run(`validState({...state,categories:[...state.categories,{id:'bad',name:'bad',type:'expense',parentId:'${parentId}'}]})`));assert.throws(()=>run("validState({...state,categories:[...state.categories,{id:'self',name:'self',type:'expense',parentId:'self'}]})"))});
 console.log(`${count} accounting checks passed`);
