'use strict';
const expandedCategoryGroups={expense:false,income:false};
const commonCategoryIds=new Set(['e0','e1','e2','e3','e7','e8','e9','e10','e12','e13','e15','e19','e21','e22','e23','e31','i0','i1','i3','i5','i6','i7']);
const originalCategoryIds=new Set([...Array.from({length:34},(_,i)=>'e'+i),...Array.from({length:10},(_,i)=>'i'+i)]);
function visibleCategories(type,selected=''){
 return state.categories.filter(c=>c.type===type&&!c.parentId&&(expandedCategoryGroups[type]||commonCategoryIds.has(c.id)||!originalCategoryIds.has(c.id)||c.id===selected));
}
function categoryMoreControl(type,picker=false){
 const hidden=state.categories.filter(c=>c.type===type&&!c.parentId&&!commonCategoryIds.has(c.id)&&originalCategoryIds.has(c.id)).length;
 if(!hidden)return '';
 return `<button type="button" class="${picker?'category-more':'category-more-link ghost'}" data-category-toggle="${type}" aria-expanded="${expandedCategoryGroups[type]}">${expandedCategoryGroups[type]?'收起分类':'更多分类（'+hidden+'）'}</button>`;
}
function bindCategoryToggles(picker=false){
 const root=picker?$('#categoryGrid'):$('#content');
 root.querySelectorAll('[data-category-toggle]').forEach(b=>b.onclick=()=>{const type=b.dataset.categoryToggle;expandedCategoryGroups[type]=!expandedCategoryGroups[type];if(picker)categoryButtons();else render()});
}

const subcategoryPresets={
 '餐饮':['早餐','午餐','晚餐','饮品'], '购物':['衣物','日用品','家电','其他'],
 '交通':['公交地铁','打车','火车飞机','停车'], '旅行':['交通','住宿','餐饮','购物','门票'], '旅游':['交通','住宿','餐饮','购物','门票'],
 '住房':['房租','水电燃气','物业','其他'], '医疗':['药品','门诊','检查','其他'],
 '娱乐':['电影','游戏','演出','其他'], '学习':['课程','资料','考试','其他'], '宠物':['食品','用品','医疗','其他'],
 '工资':['基本工资','绩效','补贴'], '理财':['利息','分红','投资收益']
};
function seedCategoryChildren(d,parent){
 const names=subcategoryPresets[parent.name]||(parent.type==='income'?['固定收入','临时收入','其他']:['日常','用品','其他']);
 names.forEach((name,i)=>{let id='sub:'+parent.id+':'+i;while(d.categories.some(c=>c.id===id))id+='x';d.categories.push({id,name,type:parent.type,parentId:parent.id,icon:glyphs[name]||parent.icon||'◇'})});
}
function ensureSubcategories(d){if(d.categoryHierarchyVersion===1)return d;for(const c of [...d.categories].filter(c=>!c.parentId))if(!d.categories.some(x=>x.parentId===c.id))seedCategoryChildren(d,c);d.categoryHierarchyVersion=1;return d}
function validateCategoryHierarchy(d){const cats=new Map(d.categories.map(c=>[c.id,c]));for(const c of d.categories){if(c.parentId!==undefined&&typeof c.parentId!=='string')throw Error('上级分类编号无效');if(c.parentId){const p=cats.get(c.parentId);if(!p||p.parentId||p.id===c.id||p.type!==c.type)throw Error('分类仅支持同收支类型的两级结构')}}}
function categoryDisplay(id){const c=state.categories.find(c=>c.id===id);if(!c)return {name:'未知分类',icon:'◇'};const p=state.categories.find(p=>p.id===c.parentId);return p?{...c,name:p.name+' / '+c.name}:c}
function categoryManagement(){return `<section class="section category-manager"><div class="toolbar category-manager-heading"><h2>分类管理</h2><button data-category-new>＋ 添加分类</button></div><p>选择一级分类查看二级分类，可添加、改名；已有账单的分类不能删除。</p>${['expense','income'].map(type=>`<h3>${type==='expense'?'支出分类':'收入分类'}</h3>${visibleCategories(type).map(c=>`<details class="category-tree"><summary>${esc(c.name)}<small>${state.categories.filter(x=>x.parentId===c.id).length} 个二级分类</small></summary><div class="category-tree-actions"><button data-category-edit="${esc(c.id)}">编辑一级分类</button><button data-category-child="${esc(c.id)}">＋ 添加二级分类</button></div><div class="chips">${state.categories.filter(x=>x.parentId===c.id).map(x=>`<button class="chip" data-category-edit="${esc(x.id)}">${esc(x.name)}</button>`).join('')||'<span class="muted">暂无二级分类</span>'}</div></details>`).join('')}${categoryMoreControl(type)}`).join('')}</section>`}
function hierarchicalCategoryButtons(){
 const chosen=state.categories.find(c=>c.id===selectedCategory&&c.type===entryType);
 const parent=chosen?.parentId?state.categories.find(c=>c.id===chosen.parentId):chosen;
 const cs=visibleCategories(entryType,parent?.id||'');
 if(!parent||!cs.some(c=>c.id===parent.id))selectedCategory=cs[0]?.id||'';
 const selected=state.categories.find(c=>c.id===selectedCategory),root=selected?.parentId?state.categories.find(c=>c.id===selected.parentId):selected;
 $('#categoryGrid').innerHTML=cs.map(c=>`<button type="button" data-cat-parent="${esc(c.id)}" class="${root?.id===c.id?'active':''}">${esc(c.name)}</button>`).join('')+categoryMoreControl(entryType,true)+(root?`<div class="subcategory-picker"><strong>${esc(root.name)} · 二级分类</strong><div class="subcategory-options"><button type="button" data-cat="${esc(root.id)}" class="${selectedCategory===root.id?'active':''}">未细分</button>${state.categories.filter(c=>c.parentId===root.id).map(c=>`<button type="button" data-cat="${esc(c.id)}" class="${selectedCategory===c.id?'active':''}">${esc(c.name)}</button>`).join('')}</div></div>`:'');
 bindCategoryToggles(true);$$('[data-cat-parent]').forEach(b=>b.onclick=()=>{selectedCategory=b.dataset.catParent;categoryButtons()});$$('[data-cat]').forEach(b=>b.onclick=()=>{selectedCategory=b.dataset.cat;categoryButtons()});$$('[data-type]').forEach(b=>b.classList.toggle('active',b.dataset.type===entryType));
}
function editHierarchicalCategory(id,parentId=''){
 const c=state.categories.find(x=>x.id===id),parent=state.categories.find(x=>x.id===(c?.parentId||parentId));
 manage(c?'编辑'+(c.parentId?'二级':'一级')+'分类':parent?'添加二级分类':'添加分类',field('分类名称','name',c?.name||'','required maxlength="20"')+`<label>收支类型<select name="type" ${c||parent?'disabled':''}><option value="expense" ${(c?.type||parent?.type||'expense')==='expense'?'selected':''}>支出</option><option value="income" ${(c?.type||parent?.type)==='income'?'selected':''}>收入</option></select></label>`+(c?`<p>${parent?'上级分类：'+esc(parent.name):'一级分类'}</p>`:`<label>上级分类<select name="parent"><option value="">无（创建一级分类）</option>${state.categories.filter(x=>!x.parentId).map(x=>`<option value="${esc(x.id)}" ${parentId===x.id?'selected':''}>${x.type==='expense'?'支出':'收入'} · ${esc(x.name)}</option>`).join('')}</select></label>`)+(c?'<button type="button" class="danger" id="removeCat">删除分类</button>':''),async f=>{
  const name=nameOf(f.get('name')),p=c?.parentId||(!c?f.get('parent'):'')||'',up=state.categories.find(x=>x.id===p),type=c?.type||up?.type||f.get('type'),d=clone();
  if(p&&(!up||up.parentId))throw Error('请选择有效的一级分类');
  if(d.categories.some(x=>x.name===name&&x.type===type&&(x.parentId||'')===p&&x.id!==id))throw Error('同一层级的分类名称已存在');
  if(c)d.categories.find(x=>x.id===id).name=name;else{const n={id:uid(),name,type,icon:glyphs[name]||'◇',...(p?{parentId:p}:{})};d.categories.push(n);if(!p)seedCategoryChildren(d,n)}
  await persist(d);toast('分类已保存');
 });
 if(c)$('#removeCat').onclick=async()=>{await removeEntity('categories',id);if(!state.categories.some(x=>x.id===id))$('#manageDialog').close()};
 if(!c){const type=$('#manageForm').elements.type,select=$('#manageForm').elements.parent;const update=()=>{for(const opt of select.options){const item=state.categories.find(x=>x.id===opt.value);opt.hidden=!!item&&item.type!==type.value}const p=state.categories.find(x=>x.id===select.value);if(p&&p.type!==type.value)select.value=''};type.onchange=update;update();}
}
