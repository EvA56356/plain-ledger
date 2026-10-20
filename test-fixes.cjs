const fs=require('fs'),vm=require('vm'),assert=require('assert/strict');
const ctx=vm.createContext({console,document:{querySelector:()=>({value:'2026-10'})},window:{},setTimeout,clearTimeout});
for(const f of ['categories.js','projects.js'])vm.runInContext(fs.readFileSync(__dirname+'/web/'+f,'utf8'),ctx);
vm.runInContext(fs.readFileSync(__dirname+'/web/app.js','utf8').split("$('#entryForm').onsubmit=")[0],ctx);
const run=s=>vm.runInContext(s,ctx);let count=0;
function test(n,f){f();count++;console.log('PASS '+n)}
test('delete root and all descendants without changing ledger amounts',()=>{
 run("state=ensureSubcategories(initial());state.entries=[{id:'1',type:'expense',amount:12345,category:state.categories.find(c=>c.parentId==='e15').id,book:'daily',account:'cash',date:'2026-10-20',note:'kept'},{id:'2',type:'expense',amount:500,category:'e15',book:'daily',account:'cash',date:'2026-10-20',note:'kept root'}];state=deleteCategoryData(state,'e15')");
 assert.ok(run('validState(state)'));assert.equal(run("state.categories.some(c=>c.id==='e15'||c.parentId==='e15')"),false);assert.equal(run('totals(state.entries).expense'),12845);assert.equal(run('state.entries.length'),2);assert.equal(run("category(state.entries[0].category).name"),'其它');
});
test('deleting only root creates safe fallback and retains income/expense type',()=>{
 run("state=initial();state.categories=[{id:'root',name:'工资',type:'income',icon:''}];state.entries=[{id:'1',type:'income',category:'root',amount:100,book:'daily',account:'cash',date:'2026-10-20',note:''}];state=deleteCategoryData(state,'root')");assert.ok(run('validState(state)'));assert.equal(run('state.categories.length'),1);assert.equal(run('state.categories[0].type'),'income');assert.equal(run('totals(state.entries).income'),100);
});
test('deleting child leaves siblings and root intact',()=>{run("state=ensureSubcategories(initial());const child=state.categories.find(c=>c.parentId==='e15');globalThis.removed=child.id;globalThis.before=state.categories.length;state=deleteCategoryData(state,child.id)");assert.ok(run('validState(state)'));assert.equal(run('state.categories.length'),run('before-1'));assert.equal(run("state.categories.some(c=>c.id==='e15')"),true)});
test('project header displays projects, not book names, and filters hidden projects',()=>{run("state=initial();state.projects=[{id:'g',name:'旅游',book:'daily'},{id:'p',name:'大连',parentId:'g',book:'daily'},{id:'h',name:'隐藏项目',parentId:'g',book:'daily',dropdownVisible:false}]");const html=run('projectOptions()');assert.match(html,/旅游/);assert.match(html,/大连/);assert.doesNotMatch(html,/日常账本|隐藏项目/)});
const app=fs.readFileSync(__dirname+'/web/app.js','utf8'),native=fs.readFileSync(__dirname+'/android/src/cn/plainledger/MainActivity.java','utf8');
test('native back delegates hierarchy and exits without exit dialog',()=>{assert.match(native,/window.handleAppBack/);assert.match(native,/if\(!"true"\.equals\(r\)\)finish\(\)/);assert.doesNotMatch(native,/退出简账？/);assert.match(app,/page==='visibility'\)\{page='settings'/)});
test('confirmation is promoted into modal top layer on every use',()=>{assert.match(app,/if\(d.open\)d.close\(\);document.body.appendChild\(d\)/);assert.match(app,/d.oncancel=\(\)=>resolve\(false\);d.showModal\(\)/)});
console.log(count+' regression checks passed');
