const fs=require('fs'),vm=require('vm'),assert=require('assert/strict');
const source=fs.readFileSync(__dirname+'/web/app.js','utf8').split("$('#entryForm').onsubmit=")[0];
const context=vm.createContext({console,document:{querySelector:()=>({value:'2026-10'})},window:{},setTimeout,clearTimeout});
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
test('default classification has 34 expense and 10 income categories',()=>{assert.equal(run("initial().categories.filter(c=>c.type==='expense').length"),34);assert.equal(run("initial().categories.filter(c=>c.type==='income').length"),10)});
console.log(`${count} accounting checks passed`);
