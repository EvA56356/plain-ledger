'use strict';
(() => {
 let sequence=0,opened=null,queued=false;
 function close(focus=false){
  if(!opened)return;
  const {trigger,menu}=opened;opened=null;menu.remove();trigger.setAttribute('aria-expanded','false');trigger.classList.remove('is-open');
  if(focus&&trigger.isConnected)trigger.focus();
 }
 window.closePlainDropdown=()=>{if(!opened)return false;close(true);return true};
 function labelFor(select){
  if(select.getAttribute('aria-label'))return select.getAttribute('aria-label');
  const label=select.labels?.[0];
  if(label){const copy=label.cloneNode(true);copy.querySelectorAll('select,button,.plain-select').forEach(n=>n.remove());return copy.textContent.trim()||'请选择'}
  return '请选择';
 }
 function refresh(select){
  if(!select.parentElement?.classList.contains('plain-select')){
   const wrapper=document.createElement('span');wrapper.className='plain-select';
   if(select.id==='mobileBookSelect')wrapper.classList.add('plain-select-mobile');
   if(select.id==='bookSelect')wrapper.classList.add('plain-select-sidebar');
   select.before(wrapper);wrapper.appendChild(select);
   const trigger=document.createElement('button');trigger.type='button';trigger.className='plain-select-trigger';
   trigger.setAttribute('role','combobox');trigger.setAttribute('aria-haspopup','listbox');trigger.setAttribute('aria-expanded','false');trigger.setAttribute('aria-label',labelFor(select));
   const text=document.createElement('span');text.className='plain-select-value';
   const arrow=document.createElement('span');arrow.className='plain-select-chevron';arrow.setAttribute('aria-hidden','true');
   trigger.append(text,arrow);wrapper.appendChild(trigger);
   if(!select.id)select.id='plain-native-'+(++sequence);
   trigger.id=select.id+'-trigger';select.classList.add('plain-native-select');select.setAttribute('aria-hidden','true');select.tabIndex=-1;
   trigger.onclick=()=>{if(opened?.select===select)close(true);else open(select,trigger)};
   trigger.onkeydown=e=>{if(['ArrowDown','ArrowUp','Enter',' '].includes(e.key)){e.preventDefault();open(select,trigger)}};
   select.addEventListener('change',()=>refresh(select));
  }
  const trigger=select.parentElement.querySelector('.plain-select-trigger');trigger.setAttribute('aria-label',labelFor(select));
  trigger.querySelector('.plain-select-value').textContent=select.selectedOptions[0]?.textContent||'请选择';trigger.disabled=select.disabled;
 }
 function open(select,trigger){
  if(select.disabled)return;close();
  const menu=document.createElement('div');menu.className='plain-select-menu';menu.id=select.id+'-menu';menu.setAttribute('role','listbox');menu.setAttribute('aria-label',trigger.getAttribute('aria-label'));
  const options=Array.from(select.options).filter(o=>!o.hidden);
  options.forEach(option=>{
   const item=document.createElement('button');item.type='button';item.className='plain-select-option';item.setAttribute('role','option');item.setAttribute('aria-selected',String(option.selected));item.disabled=option.disabled;item.tabIndex=-1;
   const name=document.createElement('span');name.textContent=option.textContent;
   const check=document.createElement('span');check.className='plain-select-check';check.setAttribute('aria-hidden','true');check.textContent=option.selected?'✓':'';item.append(name,check);
   item.onclick=()=>{const value=option.value;close();select.value=value;select.dispatchEvent(new Event('change',{bubbles:true}));const next=document.getElementById(select.id+'-trigger');if(next)next.focus()};
   menu.appendChild(item);
  });
  const layer=trigger.closest('dialog')||document.body;layer.appendChild(menu);
  opened={select,trigger,menu};trigger.classList.add('is-open');trigger.setAttribute('aria-expanded','true');trigger.setAttribute('aria-controls',menu.id);
  const r=trigger.getBoundingClientRect(),vw=document.documentElement.clientWidth,vh=window.innerHeight;
  const width=Math.min(Math.max(r.width,164),vw-24);menu.style.width=width+'px';menu.style.left=Math.max(12,Math.min(r.left,vw-width-12))+'px';
  const desired=Math.min(options.length*44+16,290),below=vh-r.bottom-16,above=r.top-16;
  if(below>=Math.min(desired,170)||below>=above){menu.style.top=r.bottom+7+'px';menu.style.maxHeight=Math.max(90,Math.min(desired,below))+'px'}
  else{const height=Math.max(90,Math.min(desired,above));menu.style.top=Math.max(8,r.top-height-7)+'px';menu.style.maxHeight=height+'px'}
  const chosen=menu.querySelector('[aria-selected="true"]:not(:disabled)')||menu.querySelector('button:not(:disabled)');chosen?.focus({preventScroll:true});chosen?.scrollIntoView({block:'nearest'});
  menu.onkeydown=e=>{
   const items=Array.from(menu.querySelectorAll('button:not(:disabled)')),i=items.indexOf(document.activeElement);
   if(['ArrowDown','ArrowUp','Home','End'].includes(e.key)){e.preventDefault();const next=e.key==='Home'?0:e.key==='End'?items.length-1:(i+(e.key==='ArrowDown'?1:-1)+items.length)%items.length;items[next]?.focus()}
   if(e.key==='Escape'){e.preventDefault();close(true)}if(e.key==='Tab')close();
  };
 }
 function enhance(){document.querySelectorAll('select').forEach(refresh)}
 function schedule(){if(queued)return;queued=true;queueMicrotask(()=>{queued=false;enhance()})}
 new MutationObserver(changes=>{if(changes.some(m=>m.target.tagName==='SELECT'||m.target.closest?.('select')||Array.from(m.addedNodes).some(n=>n.nodeType===1&&(n.tagName==='SELECT'||n.querySelector('select')))))schedule();if(opened&&!opened.trigger.isConnected)close()}).observe(document.documentElement,{childList:true,subtree:true});
 document.addEventListener('pointerdown',e=>{if(opened&&!opened.menu.contains(e.target)&&!opened.trigger.contains(e.target))close()});
 document.addEventListener('scroll',e=>{if(opened&&e.target!==opened.menu&&!opened.menu.contains(e.target))close()},{capture:true,passive:true});
 document.addEventListener('close',()=>close(),true);window.addEventListener('resize',()=>close());
 if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',enhance);else enhance();
})();
