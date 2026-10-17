const fs=require('fs'),vm=require('vm'),assert=require('assert/strict');
const ctx=vm.createContext({});vm.runInContext(fs.readFileSync(__dirname+'/web/reports.js','utf8'),ctx);
const e=(date,amount,book='a',type='expense')=>({date,amount,book,type,category:'food',account:'cash'});
const model=ctx.reportModel([e('2026-10-01',1234),e('2026-10-31',200),e('2026-09-30',100),e('2026-10-01',999,'b'),e('2026-10-02',5000,'a','income')],'2026-10-01','2026-10-31','a');
assert.equal(model.sums.expense,1434);assert.equal(model.sums.income,5000);assert.equal(model.before.expense,100);assert.equal(model.days,31);assert.equal(Object.keys(model.daily).length,31);assert.equal(model.categories.food,1434);
assert.equal(ctx.reportModel([e('2026-10-01',100,'a'),e('2026-10-01',200,'b')],'2026-10-01','2026-10-01',null).sums.expense,300);
assert.equal(ctx.reportModel([],'2024-02-01','2024-02-29',null).days,29);
assert.throws(()=>ctx.reportModel([],'2026-10-02','2026-10-01',null));assert.throws(()=>ctx.reportModel([],'2020-01-01','2026-01-01',null));
console.log('10 reporting assertions passed');
