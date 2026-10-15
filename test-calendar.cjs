const fs=require('fs'),vm=require('vm'),assert=require('assert/strict');
const scope=vm.createContext({console});
vm.runInContext(fs.readFileSync(__dirname+'/web/calendar.js','utf8'),scope);
const run=s=>vm.runInContext(s,scope);
let count=0;
function test(name,f){f();count++;console.log('PASS '+name)}
test('October 2026 weekday layout and adjacent month cells',()=>{
 assert.equal(run("monthGrid('2026-10').length"),35);
 assert.equal(run("monthGrid('2026-10')[0].date"),'2026-09-27');
 assert.equal(run("monthGrid('2026-10')[4].date"),'2026-10-01');
 assert.equal(run("monthGrid('2026-10').filter(d=>d.current).length"),31);
});
test('leap February has 29 real days',()=>{assert.equal(run("monthGrid('2024-02').filter(d=>d.current).length"),29)});
test('non-leap February has 28 days',()=>{assert.equal(run("monthGrid('2025-02').filter(d=>d.current).length"),28)});
test('month starting Sunday does not add an extra empty week',()=>{assert.equal(run("monthGrid('2026-03')[0].date"),'2026-03-01')});
test('six-week month retains all days',()=>{assert.equal(run("monthGrid('2026-08').length"),42);assert.equal(run("monthGrid('2026-08').filter(d=>d.current).length"),31)});
test('daily net groups income and expenses without floating point loss',()=>{
 run("var totals=dailyTotals([{date:'2026-10-15',type:'expense',amount:1234},{date:'2026-10-15',type:'income',amount:2000},{date:'2026-10-16',type:'expense',amount:10},{date:'2026-10-16',type:'expense',amount:20}])");
 assert.equal(run("totals['2026-10-15'].income-totals['2026-10-15'].expense"),766);
 assert.equal(run("totals['2026-10-15'].count"),2);
 assert.equal(run("totals['2026-10-16'].expense"),30);
 assert.equal(run("totals['2026-10-14']"),undefined);
});
console.log(`${count} calendar checks passed`);
