const fs=require('fs'),vm=require('vm'),assert=require('assert/strict');
const source=fs.readFileSync(__dirname+'/web/auto-bill.js','utf8');
const make=(id,extra={})=>({id,amount:1234,source:'支付宝',created:Date.now(),...extra});
function harness(initial=[]){
 const h={drafts:initial,foreground:true,editing:false,acknowledged:[],completed:[],opened:0,failRender:false,failFill:false,failAck:false,throwAck:false,failFinish:false};
 const els={};for(const key of ['#month','#entryTitle','#entryError'])els[key]={value:'',textContent:''};
 const fields={};for(const key of ['amount','date','note','account'])fields[key]={value:''};els['#entryForm']={elements:fields};
 const listeners={};els['#entryDialog']={get open(){return h.editing},addEventListener(name,fn){listeners[name]=fn},close(){h.editing=false;listeners.close?.()}};
 const android={takeAutoDraft:()=>JSON.stringify(h.drafts[0]||null),isAppForeground:()=>h.foreground,
  ackAutoDraft(id){if(h.throwAck)throw Error('native commit failed');if(h.failAck)return false;if(!h.drafts.some(d=>d.id===id))return false;h.acknowledged.push(id);return true},
  finishAutoDraft(id,saved){if(h.failFinish)return false;h.completed.push({id,saved});h.drafts=h.drafts.filter(d=>d.id!==id);return true},
  autoBillStatus:()=>JSON.stringify({enabled:true,systemEnabled:true,connected:true})};
 const context=vm.createContext({console,JSON,Date,Number,String,Android:android,window:{Android:android,addEventListener(){}},
  document:{visibilityState:'visible',querySelector:()=>h.editing?{}:null,addEventListener(){}},setInterval(){},
  state:{entries:[],accounts:[{id:'alipay',name:'支付宝'}],categories:[{id:'child-food',name:'餐饮',type:'expense',parentId:'travel'},{id:'food',name:'餐饮',type:'expense'},{id:'other',name:'其它',type:'expense'}]},
  page:'settings',selectedDay:'',selectedCategory:'',today:()=> '2026-10-20',$:s=>els[s]||null,esc:s=>String(s),
  render:()=>{if(h.failRender)throw Error('render failed')},openEntry:()=>{h.opened++;h.editing=true},categoryButtons(){if(h.failFill)throw Error('fill failed')},toast(){}});
 h.run=s=>vm.runInContext(s,context);h.receive=()=>h.run('receiveAutoDraft()');h.fields=fields;h.els=els;h.close=()=>els['#entryDialog'].close();
 h.save=()=>{h.run('state.entries.push({id:window.autoDraftEntryId()});window.autoDraftSaved()');h.close()};
 h.newPage=()=>harness(h.drafts);
 vm.runInContext(source,context);return h;
}
{
 const h=harness([make('first')]);h.foreground=false;h.receive();assert.equal(h.opened,0);
 h.foreground=true;h.editing=true;h.receive();assert.equal(h.acknowledged.length,0);
 h.editing=false;h.failRender=true;h.receive();assert.equal(h.acknowledged.length,0);assert.equal(h.drafts[0].id,'first');
 h.failRender=false;h.receive();assert.equal(h.fields.amount.value,'12.34');assert.equal(h.fields.account.value,'alipay');assert.equal(h.run('selectedCategory'),'food');
 assert.equal(h.drafts[0].id,'first','Showing the editor must not consume a payment');
 assert.equal(h.run('window.autoDraftEntryId()'),'auto-first');h.receive();assert.equal(h.opened,1);
 const restarted=h.newPage();restarted.receive();assert.equal(restarted.fields.amount.value,'12.34','Unsaved payment survives process recreation');
 h.save();assert.deepEqual(h.completed,[{id:'first',saved:true}]);assert.equal(h.drafts.length,0);
}
{
 const h=harness([make('test',{test:true})]);h.receive();assert.match(h.els['#entryTitle'].textContent,/虚构金额/);assert.match(h.fields.note.value,/测试/);
 h.close();assert.deepEqual(h.completed,[{id:'test',saved:false}]);
}
{
 const h=harness([make('waiting',{created:Date.now()-360000})]);h.receive();assert.match(h.els['#entryError'].textContent,/超过 5 分钟/);assert.equal(h.drafts.length,1);
 h.close();assert.equal(h.drafts.length,0);
}
{
 const h=harness([make('queue-first'),make('queue-second',{amount:990})]);h.receive();h.fields.amount.value='77.77';h.receive();
 assert.equal(h.fields.amount.value,'77.77','A second payment cannot overwrite an existing edit');assert.equal(h.opened,1);
 h.save();h.receive();assert.equal(h.fields.amount.value,'9.90');assert.equal(h.drafts[0].id,'queue-second');
 h.close();assert.deepEqual(h.completed,[{id:'queue-first',saved:true},{id:'queue-second',saved:false}]);
}
for(const failure of ['failAck','throwAck']){
 const h=harness([make(failure)]);h[failure]=true;h.receive();const before=h.opened;assert.equal(h.editing,true);assert.equal(h.drafts.length,1);
 h.fields.amount.value='44.44';h.receive();h[failure]=false;h.receive();assert.equal(h.opened,before);assert.equal(h.fields.amount.value,'44.44');assert.equal(h.drafts.length,1);
 h.close();assert.equal(h.drafts.length,0,'Explicit cancel completes even if initial display receipt failed');
}
{
 const h=harness([make('save-receipt'),make('next',{amount:765})]);h.receive();h.failFinish=true;h.save();
 h.receive();assert.equal(h.opened,1);assert.equal(h.drafts.length,2,'A completion failure preserves the head and blocks replacement');
 h.failFinish=false;h.receive();assert.equal(h.fields.amount.value,'7.65');assert.equal(h.drafts[0].id,'next');
}
{
 const h=harness([make('committed-before-crash')]);h.run("state.entries.push({id:'auto-committed-before-crash'})");h.receive();
 assert.equal(h.opened,0,'An already committed stable entry must not open or append again');assert.deepEqual(h.completed,[{id:'committed-before-crash',saved:true}]);
}
{
 const h=harness([make('fill-retry')]);h.failFill=true;h.receive();assert.equal(h.editing,false);assert.equal(h.drafts.length,1);assert.equal(h.completed.length,0);
 h.failFill=false;h.receive();assert.equal(h.editing,true);assert.equal(h.drafts.length,1);
}
{
 const created=new Date(2026,9,20,12).getTime(),h=harness([make('another-real-payment',{created})]);
 h.run("state.entries.push({id:'auto-earlier-payment',autoSource:'支付宝',amount:1234,date:'2026-10-20'})");h.receive();
 assert.match(h.els['#entryError'].textContent,/可能重复/);assert.equal(h.run('window.autoDraftEntryMeta().autoSource'),'支付宝');
 h.save();assert.equal(h.drafts.length,0,'A duplicate warning must still allow a distinct real payment to be saved');
}
{
 const created=new Date(2026,9,20,12).getTime(),h=harness([make('different-source-or-date',{created})]);
 h.run("state.entries.push({id:'auto-other-source',autoSource:'微信',amount:1234,date:'2026-10-20'},{id:'auto-other-date',autoSource:'支付宝',amount:1234,date:'2026-10-19'})");h.receive();
 assert.doesNotMatch(h.els['#entryError'].textContent,/可能重复/);assert.equal(h.drafts.length,1);
}
{
 const h=harness([make('original-date',{created:new Date(2026,9,19,23,59).getTime()})]);h.receive();assert.equal(h.fields.date.value,'2026-10-19');assert.equal(h.els['#month'].value,'2026-10');
 assert.match(h.run('autoBillStatusText({enabled:false,systemEnabled:true,connected:true})'),/应用内识别已暂停/);
 assert.match(h.run('autoBillStatusText({enabled:true,systemEnabled:true,connected:false})'),/服务未连接/);
}
console.log('Automatic draft delivery, persistent review lifecycle, stable save recovery, ordered queue, edit preservation, acknowledgement/completion retries, cancellation, dates and diagnostics checks passed');
