"use strict";(self["webpackChunkweb_ui"]=self["webpackChunkweb_ui"]||[]).push([[4746],{83744:function(e,t){t.Z=(e,t)=>{const o=e.__vccOpts||e;for(const[i,n]of t)o[i]=n;return o}},34746:function(e,t,o){o.r(t),o.d(t,{default:function(){return l}});var i=o(70821);const n={class:"d-drag-content"};var a=(0,i.defineComponent)({props:{width:{type:String,required:!1},height:{type:String,required:!1},visible:{type:Boolean,required:!1},title:{type:String,required:!1},modalClass:{type:String,required:!1},renderToBody:{type:Boolean,required:!1}},setup(e,{expose:t}){const o=e,a=(0,i.ref)(),r=(0,i.ref)();(0,i.onMounted)((()=>{r.value?.addEventListener("selectstart",(e=>{e.preventDefault()}))})),(0,i.watch)((0,i.computed)((()=>o.visible)),(e=>{e&&(s.value=!1,d.right=30,d.top=30)}));const d=(0,i.reactive)({right:30,top:30}),l=(0,i.computed)((()=>({width:o.width?o.width:"300px",height:o.height?o.height:"auto",right:d.right+"px",top:d.top+"px"}))),s=(0,i.ref)(!1),u=()=>{s.value=!s.value},c=()=>{s.value&&(s.value=!1)};let v=!1,p=0,m=0,h=0,g=0;const f=e=>{let t=a.value?.parentElement;h=t?t.offsetWidth:0,g=t?t.offsetHeight:0,v=!0,p=e.pageX,m=e.pageY;let o=e||window.event;o.preventDefault();let i=document.getElementById("app");i?.addEventListener("mousemove",w),i?.addEventListener("mouseup",y)},w=e=>{if(v){let{pageX:t,pageY:o}=e,i=t-p,n=o-m,r=d.right-i,l=d.top+n;r>0&&(!(h>0)||r+(a.value?a.value.offsetWidth:0)<h)&&(d.right=r,p=t),l>0&&(!(g>0)||l+(a.value?a.value.offsetHeight:0)<g)&&(d.top=l,m=o)}},y=()=>{v=!1;let e=document.getElementById("app");e?.removeEventListener("mousemove",w),e?.removeEventListener("mouseup",y)};return t({noMinimize:c}),(t,o)=>{const d=(0,i.resolveComponent)("icon-expand"),c=(0,i.resolveComponent)("icon-minus");return(0,i.openBlock)(),(0,i.createBlock)(i.Transition,{mode:"out-in",name:"d-drag-modal-animation"},{default:(0,i.withCtx)((()=>[(0,i.withDirectives)((0,i.createElementVNode)("div",{ref:(e,t)=>{t["dDragModalRef"]=e,a.value=e},class:(0,i.normalizeClass)(["drag-model-container",[e.modalClass?e.modalClass:"",s.value?"d-drag-modal-is-minimize":""]]),style:(0,i.normalizeStyle)((0,i.unref)(l))},[(0,i.createElementVNode)("div",{class:"d-drag-modal-minimize",onClick:u,onMousedown:f},[(0,i.createVNode)(d)],32),(0,i.createElementVNode)("div",{class:"d-drag-modal-body",style:(0,i.normalizeStyle)({width:e.width?e.width:"300px",height:e.height?e.height:"auto"})},[(0,i.createElementVNode)("div",{class:"d-drag-context",onMousedown:f},[(0,i.createElementVNode)("div",{class:"title",ref:(e,t)=>{t["dDragContextTextRef"]=e,r.value=e}},(0,i.toDisplayString)(e.title),513),(0,i.createElementVNode)("div",{class:"close",onClick:u},[(0,i.createVNode)(c)])],32),(0,i.createElementVNode)("div",n,[(0,i.renderSlot)(t.$slots,"default")])],4)],6),[[i.vShow,!!e.visible&&e.visible]])])),_:3})}}}),r=o(83744);const d=(0,r.Z)(a,[["__scopeId","data-v-2f983906"]]);var l=d}}]);
//# sourceMappingURL=4746.1848c391.js.map