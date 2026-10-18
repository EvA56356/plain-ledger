'use strict';
function dailyTotals(entries){
  const days={};
  for(const e of entries){const d=days[e.date]??={income:0,expense:0,count:0};d[e.type]+=e.amount;d.count++}
  return days;
}
function monthGrid(month){
  const first=new Date(month+'-01T12:00:00Z');
  if(!Number.isFinite(first.getTime()))return [];
  const year=first.getUTCFullYear(),m=first.getUTCMonth();
  const last=new Date(Date.UTC(year,m+1,0,12));
  const count=Math.ceil((first.getUTCDay()+last.getUTCDate())/7)*7;
  return Array.from({length:count},(_,i)=>{
    const d=new Date(first);d.setUTCDate(1-first.getUTCDay()+i);
    const date=d.toISOString().slice(0,10);
    return {date,day:d.getUTCDate(),current:date.slice(0,7)===month};
  });
}
function calendar(){
  const month=$('#month').value,es=monthEntries(),days=dailyTotals(es),sum=totals(es);
  const cells=monthGrid(month).map(d=>{
    const v=days[d.date],net=v?v.income-v.expense:0,selected=d.date===selectedDay,isToday=d.date===today();
    const description=d.current?`${d.date}${v?`，收入 ${money(v.income)} 元，支出 ${money(v.expense)} 元，净收支 ${money(net)} 元`:'，无记录'}`:d.date;
    return `<button type="button" class="calendar-day ${d.current?'':'outside'} ${v?'has-records':''} ${selected?'selected':''} ${isToday?'is-today':''}" ${d.current?`data-cal-day="${d.date}"`:'disabled'} aria-label="${esc(description)}" aria-pressed="${selected}" title="${esc(description)}"><span class="day-number">${String(d.day).padStart(2,'0')}${isToday?'<i>今</i>':''}</span><span class="day-amount ${net<0?'negative':'positive'}"><span class="day-amount-value">${v?(net>0?'＋':net<0?'−':'')+money(Math.abs(net)).replace(/,/g,''):''}</span></span></button>`;
  }).join('');
  return `<section class="calendar-panel" aria-label="月度收支日历"><div class="calendar-top"><div><span class="eyebrow">YOUR DAYS, AT A GLANCE</span><h2><button class="calendar-month-title" data-month-picker aria-label="选择年月">${Number(month.slice(0,4))}年 ${Number(month.slice(5))}月 <span aria-hidden="true">⌄</span></button></h2></div><div class="calendar-controls"><button type="button" class="calendar-today" data-cal-today>今天</button><button type="button" class="calendar-arrow" data-cal-move="-1" aria-label="上个月">‹</button><button type="button" class="calendar-arrow" data-cal-move="1" aria-label="下个月">›</button></div></div><div class="calendar-weekdays" aria-hidden="true">${['日','一','二','三','四','五','六'].map(w=>`<span>${w}</span>`).join('')}</div><div class="calendar-days">${cells}</div><div class="calendar-footer"><span>本月收入 <strong class="income">¥ ${money(sum.income)}</strong></span><span>本月支出 <strong class="expense">¥ ${money(sum.expense)}</strong></span></div><div class="calendar-caption"><span>日期下方为当天净收支（收入 − 支出）</span><span>点击日期查看明细</span></div></section>`;
}
function moveCalendarMonth(delta){
  const [y,m]=$('#month').value.split('-').map(Number),d=new Date(Date.UTC(y,m-1+delta,1,12));
  const month=d.toISOString().slice(0,7);
  if(!/^\d{4}-\d{2}$/.test(month))return;
  $('#month').value=month;selectedDay=month===today().slice(0,7)?today():null;render();
}
function fitCalendarAmounts(){
  document.querySelectorAll('.day-amount-value').forEach(el=>{
    el.style.fontSize='';el.style.transform='';
    const available=el.parentElement.clientWidth,natural=el.scrollWidth;
    if(available>0&&natural>available){
      const size=parseFloat(getComputedStyle(el).fontSize);
      el.style.fontSize=Math.max(8,Math.floor(size*available/natural))+'px';
      const width=el.scrollWidth;
      if(width>available)el.style.transform='scaleX('+available/width+')';
    }
  });
}
let calendarAmountObserver;
function bindCalendar(){
  fitCalendarAmounts();
  if(typeof ResizeObserver!=='undefined'){
    if(calendarAmountObserver)calendarAmountObserver.disconnect();
    const panel=document.querySelector('.calendar-panel');
    if(panel){calendarAmountObserver=new ResizeObserver(fitCalendarAmounts);calendarAmountObserver.observe(panel)}
  }

  $$('[data-month-picker]').forEach(b=>b.onclick=openMonthPicker);
  $$('[data-cal-day]').forEach(b=>b.onclick=()=>{selectedDay=b.dataset.calDay;render()});
  $$('[data-cal-move]').forEach(b=>b.onclick=()=>moveCalendarMonth(Number(b.dataset.calMove)));
  const reset=$('[data-cal-all]');if(reset)reset.onclick=()=>{selectedDay=null;render()};
  const now=$('[data-cal-today]');if(now)now.onclick=()=>{$('#month').value=today().slice(0,7);selectedDay=today();render()};
  $$('[data-book-use]').forEach(b=>b.onclick=()=>{book=b.dataset.bookUse;page='ledger';render()});
}
function openMonthPicker(){
  const [year,month]=$('#month').value.split('-').map(Number);
  manage('选择年月',`<div class="formrow"><label>年份<select name="year">${Array.from({length:201},(_,i)=>1900+i).map(y=>`<option value="${y}" ${y===year?'selected':''}>${y}年</option>`).join('')}</select></label><label>月份<select name="month">${Array.from({length:12},(_,i)=>`<option value="${i+1}" ${i+1===month?'selected':''}>${i+1}月</option>`).join('')}</select></label></div>`,async f=>{
    const y=Number(f.get('year')),m=Number(f.get('month'));
    if(!Number.isInteger(y)||y<1900||y>2100||!Number.isInteger(m)||m<1||m>12)throw Error('请选择有效的年份和月份');
    $('#month').value=String(y).padStart(4,'0')+'-'+String(m).padStart(2,'0');
    selectedDay=$('#month').value===today().slice(0,7)?today():null;render();
  });
}
