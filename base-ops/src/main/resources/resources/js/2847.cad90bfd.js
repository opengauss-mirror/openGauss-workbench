"use strict";(self["webpackChunkweb_ui"]=self["webpackChunkweb_ui"]||[]).push([[2847],{82847:function(e,a,n){n.r(a),n.d(a,{default:function(){return u}});var l=n(70821),t=n(882),o=n(2811),r=n(38655),i=n(18933);const m={class:"cu-dialog"};var c=(0,l.defineComponent)({emits:["finish"],setup(e,{expose:a,emit:n}){const c=(0,l.reactive)({show:!1,loading:!1,placeholder:"",formData:{name:""},formModal:{name:""}}),f=e=>{c.show=!0,c.placeholder=e},u=(0,l.ref)(null),d=()=>{c.show=!1,(0,l.nextTick)((()=>{c.formData=JSON.parse(JSON.stringify(c.formModal)),u.value?.clearValidate(),u.value?.resetFields()}))},s=()=>{u.value?.validate().then((()=>{c.loading=!0,n("finish","rename",c.formData.name),c.loading=!1,d()})).catch()};return a({open:f}),(e,a)=>((0,l.openBlock)(),(0,l.createBlock)((0,l.unref)(t.Z),{class:"rename-container",visible:(0,l.unref)(c).show,"onUpdate:visible":a[1]||(a[1]=e=>(0,l.unref)(c).show=e),title:e.$t("modeling.components.Rename.5mpu3fm9iw00"),"ok-text":e.$t("modeling.components.Rename.5mpu3fm9jbw0"),"confirm-loading":(0,l.unref)(c).loading,"cancel-text":e.$t("modeling.components.Rename.5mpu3fm9jf40"),onOk:s,onCancel:d},{default:(0,l.withCtx)((()=>[(0,l.createElementVNode)("div",m,[(0,l.createVNode)((0,l.unref)(o.Z),{model:(0,l.unref)(c).formData,ref:(e,a)=>{a["formRef"]=e,u.value=e},"label-col":{style:{width:"90px"}}},{default:(0,l.withCtx)((()=>[(0,l.createVNode)((0,l.unref)(r.Z),{name:"name",label:e.$t("modeling.components.Rename.5mpu3fm9jhc0"),rules:{required:!0,message:"please enter table name"}},{default:(0,l.withCtx)((()=>[(0,l.createVNode)((0,l.unref)(i.ZP),{"max-length":140,"show-word-limit":"",value:(0,l.unref)(c).formData.name,"onUpdate:value":a[0]||(a[0]=e=>(0,l.unref)(c).formData.name=e),placeholder:(0,l.unref)(c).placeholder},null,8,["value","placeholder"])])),_:1},8,["label"])])),_:1},8,["model"])])])),_:1},8,["visible","title","ok-text","confirm-loading","cancel-text"]))}});const f=c;var u=f}}]);
//# sourceMappingURL=2847.cad90bfd.js.map