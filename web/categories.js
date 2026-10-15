'use strict';
const expandedCategoryGroups={expense:false,income:false};
const commonCategoryIds=new Set(['e0','e1','e2','e3','e7','e8','e9','e10','e12','e13','e15','e19','e21','e22','e23','e31','i0','i1','i3','i5','i6','i7']);
const originalCategoryIds=new Set([...Array.from({length:34},(_,i)=>'e'+i),...Array.from({length:10},(_,i)=>'i'+i)]);
function visibleCategories(type,selected=''){
 return state.categories.filter(c=>c.type===type&&(expandedCategoryGroups[type]||commonCategoryIds.has(c.id)||!originalCategoryIds.has(c.id)||c.id===selected));
}
function categoryMoreControl(type,picker=false){
 const hidden=state.categories.filter(c=>c.type===type&&!commonCategoryIds.has(c.id)&&originalCategoryIds.has(c.id)).length;
 if(!hidden)return '';
 return `<button type="button" class="${picker?'category-more':'category-more-link ghost'}" data-category-toggle="${type}" aria-expanded="${expandedCategoryGroups[type]}">${expandedCategoryGroups[type]?'收起分类':'更多分类（'+hidden+'）'}</button>`;
}
function bindCategoryToggles(picker=false){
 const root=picker?$('#categoryGrid'):$('#content');
 root.querySelectorAll('[data-category-toggle]').forEach(b=>b.onclick=()=>{const type=b.dataset.categoryToggle;expandedCategoryGroups[type]=!expandedCategoryGroups[type];if(picker)categoryButtons();else render()});
}
