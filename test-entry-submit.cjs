const fs=require('fs'),vm=require('vm'),assert=require('assert/strict');
const app=fs.readFileSync(__dirname+'/web/app.js','utf8');
const handler=app.slice(app.indexOf("$('#entryForm').onsubmit="),app.indexOf("$('#deleteEntry').onclick="));
async function submit({draftId='',editing=null,fail=false,hooks=true}={}){
 const events=[],form={},error={textContent:''};
 const ctx=vm.createContext({editing,selectedCategory:'food',entryType:'expense',book:'daily',
  state:{entries:editing?[{id:editing,book:'daily'}]:[]},uid:()=> 'manual-id',
  cents:v=>Math.round(Number(v)*100),toast:()=>events.push('toast'),
  $:s=>s==='#entryForm'?form:s==='#entryError'?error:{close:()=>events.push('close')},
  window:hooks?{autoDraftEntryId:()=>draftId,autoDraftEntryMeta:()=>draftId?{autoSource:'synthetic source'}:{},autoDraftSaved:()=>events.push('ack')}:{}
 });
 ctx.clone=()=>JSON.parse(JSON.stringify(ctx.state));
 ctx.persist=async data=>{events.push('persist');if(fail)throw Error('synthetic write failure');ctx.state=data;events.push('committed')};
 vm.runInContext(handler,ctx);
 const field=value=>({value});
 await form.onsubmit({preventDefault(){},target:{elements:{amount:field('12.34'),date:field('2026-10-24'),
  note:field('synthetic'),projectGroup:field(''),projectId:field(''),account:field('cash')}}});
 return {events,error,state:ctx.state};
}
(async()=>{
 let result=await submit();assert.equal(result.state.entries[0].id,'manual-id');
 assert.equal(Object.hasOwn(result.state.entries[0],'autoSource'),false);
 result=await submit({hooks:false});assert.equal(result.state.entries[0].id,'manual-id');
 assert.deepEqual(result.events,['persist','committed','close','toast']);
 result=await submit({draftId:'auto-synthetic'});assert.equal(result.state.entries[0].id,'auto-synthetic');
 assert.equal(result.state.entries[0].autoSource,'synthetic source');
 assert.deepEqual(result.events,['persist','committed','ack','close','toast']);
 result=await submit({draftId:'auto-synthetic',fail:true});assert.deepEqual(result.events,['persist']);
 assert.equal(result.state.entries.length,0);assert.match(result.error.textContent,/write failure/);
 result=await submit({draftId:'auto-synthetic',editing:'existing'});assert.equal(result.state.entries.length,1);
 assert.equal(result.state.entries[0].id,'existing');
 const backup=vm.createContext({document:{querySelector:()=>({value:'2026-10'})},window:{},setTimeout,clearTimeout});
 for(const file of ['categories.js','projects.js'])vm.runInContext(fs.readFileSync(__dirname+'/web/'+file,'utf8'),backup);
 vm.runInContext(app.split("$('#entryForm').onsubmit=")[0],backup);
 vm.runInContext("state=initial();state.entries=[{id:'auto-synthetic',type:'expense',amount:1234,category:'e0',account:'cash',book:'daily',date:'2026-10-24',note:'edited note',autoSource:'synthetic source'}];globalThis.roundtrip=validState(JSON.parse(JSON.stringify(state)))",backup);
 assert.equal(backup.roundtrip.entries[0].autoSource,'synthetic source');
 for(const value of [123,{},'x'.repeat(41)]){
  backup.value=value;assert.throws(()=>vm.runInContext('state.entries[0].autoSource=value;validState(state)',backup));
 }
 vm.runInContext("delete state.entries[0].autoSource;validState(state);state.entries[0].autoSource='';validState(state)",backup);
 console.log('Entry save keeps manual/edit IDs and acknowledges drafts only after commit');
})().catch(error=>{console.error(error);process.exitCode=1});
