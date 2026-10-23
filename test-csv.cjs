const fs=require('fs'),vm=require('vm'),assert=require('assert/strict');
const ctx=vm.createContext({document:{querySelector:()=>({value:'2026-10'})},window:{},setTimeout,clearTimeout});
vm.runInContext(fs.readFileSync(__dirname+'/web/app.js','utf8').split("$('#entryForm').onsubmit=")[0],ctx);
for(const prefix of ['=','+','-','@','\t','\r']){
 const value=prefix+'SUM(1,2)';
 assert.equal(ctx.csvCell(value),'"\''+value+'"');
}
assert.equal(ctx.csvCell('ordinary text'),'"ordinary text"');
assert.equal(ctx.csvCell('quote " and, comma\nnext line'),'"quote "" and, comma\nnext line"');
assert.equal(ctx.csvCell(null),'""');
assert.equal(ctx.csvCell(123),'"123"');
console.log('10 CSV export checks passed');
