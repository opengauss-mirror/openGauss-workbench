"use strict";(self["webpackChunkweb_ui"]=self["webpackChunkweb_ui"]||[]).push([[5361,7869,2847],{83744:function(e,t){t.Z=(e,t)=>{const l=e.__vccOpts||e;for(const[n,o]of t)l[n]=o;return l}},27869:function(e,t,l){l.r(t),l.d(t,{default:function(){return I}});l(57658);var n=l(70821),o=l(882),i=l(58478),a=l(72453);const d=e=>((0,n.pushScopeId)("data-v-065398a0"),e=e(),(0,n.popScopeId)(),e),c={class:"edit"},s={class:"header"},r={class:"table"},m={class:"d-row"},u=d((()=>(0,n.createElementVNode)("div",{class:"d-col"},null,-1))),p={class:"d-col"},f={class:"d-col"},v={class:"d-col"},g={class:"d-col"},h={class:"d-col"},y={class:"d-col"},N={class:"d-col"},V={class:"d-col"},k=["onClick"],x={class:"d-col"},w={class:"fill"},E={class:"d-col"},S=["onUpdate:modelValue"],b={class:"d-col"},D=["onUpdate:modelValue"],C=["value"],$={class:"d-col"},_=["onUpdate:modelValue"],Z={class:"d-col"},B=["onUpdate:modelValue"],F={class:"d-col"},U={class:"d-col"},T={class:"d-col"},O=["onUpdate:modelValue"],J={class:"d-col"},M=["onUpdate:modelValue"];var j=(0,n.defineComponent)({emits:["finish"],setup(e,{expose:t,emit:l}){const d=(0,n.inject)("getGraph"),j=(0,n.reactive)({show:!1,loading:!1,list:[],types:["bigint","binary","bit","blob","char","date","datetime","decimal","double","enum","float","geometry","getmetrycollection","int","integer","json","linestring","longblob","longtext","mediumblob","mediumint","mediumtext","multilinestring","multipoint","multipolygon","numeric","point","polygon","real","set","smallint","text","time","timestamp","tinyblob","tinyint","tinytext","varbinary","varchar"]}),z=e=>{j.show=!0,j.list=JSON.parse(JSON.stringify(e))},R=()=>{if(j.show=!1,d){let e=d();e.resetSelection()}},I=()=>{j.loading=!0,l("finish","fields",j.list),R(),j.loading=!1},P=e=>{let t={name:"",type:"varchar",length:255,decimal:"",isNull:!1,virtual:!1,key:"",note:"",select:!1},l=j.list.findIndex((e=>e.select));if("add"===e)j.list.push(JSON.parse(JSON.stringify(t)));else if("insert"===e)-1!==l&&j.list.splice(l+1,0,JSON.parse(JSON.stringify(t)));else if("delete"===e)-1!==l&&j.list.splice(l,1);else if("up"===e){if(-1!==l&&0!==l){let e=j.list[l];j.list[l]=j.list[l-1],j.list[l-1]=e}}else if("down"===e&&-1!==l&&l!==j.list.length-1){let e=j.list[l];j.list[l]=j.list[l+1],j.list[l+1]=e}},L=e=>{j.list.forEach((e=>e.select=!1)),e.select=!0};return t({open:z}),(e,t)=>((0,n.openBlock)(),(0,n.createBlock)((0,n.unref)(o.Z),{class:"fields-container",visible:(0,n.unref)(j).show,"onUpdate:visible":t[6]||(t[6]=e=>(0,n.unref)(j).show=e),"confirm-loading":(0,n.unref)(j).loading,style:{"min-width":"1080px"},onCancel:R,onOk:I},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",c,[(0,n.createElementVNode)("div",s,[(0,n.createVNode)((0,n.unref)(i.Z),{type:"primary",class:"mr-xs",onClick:t[0]||(t[0]=e=>P("add"))},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("modeling.components.Fields.5mpu393tc580")),1)])),_:1}),(0,n.createVNode)((0,n.unref)(i.Z),{type:"primary",class:"mr-xs",onClick:t[1]||(t[1]=e=>P("insert"))},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("modeling.components.Fields.5mpu393tcr00")),1)])),_:1}),(0,n.createVNode)((0,n.unref)(i.Z),{type:"primary",class:"mr-xs",onClick:t[2]||(t[2]=e=>P("delete"))},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("modeling.components.Fields.5mpu393td080")),1)])),_:1}),(0,n.createVNode)((0,n.unref)(i.Z),{type:"primary",class:"mr-xs",onClick:t[3]||(t[3]=e=>P("key"))},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("modeling.components.Fields.5mpu393td3o0")),1)])),_:1}),(0,n.createVNode)((0,n.unref)(i.Z),{type:"primary",class:"mr-xs",onClick:t[4]||(t[4]=e=>P("up"))},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("modeling.components.Fields.5mpu393tdcs0")),1)])),_:1}),(0,n.createVNode)((0,n.unref)(i.Z),{type:"primary",class:"mr-xs",onClick:t[5]||(t[5]=e=>P("down"))},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("modeling.components.Fields.5mpu393tdfo0")),1)])),_:1})]),(0,n.createElementVNode)("div",r,[(0,n.createElementVNode)("div",m,[u,(0,n.createElementVNode)("div",p,(0,n.toDisplayString)(e.$t("modeling.components.Fields.5mpu393tdi40")),1),(0,n.createElementVNode)("div",f,(0,n.toDisplayString)(e.$t("modeling.components.Fields.5mpu393tdk80")),1),(0,n.createElementVNode)("div",v,(0,n.toDisplayString)(e.$t("modeling.components.Fields.5mpu393tdpo0")),1),(0,n.createElementVNode)("div",g,(0,n.toDisplayString)(e.$t("modeling.components.Fields.5mpu393tdsk0")),1),(0,n.createElementVNode)("div",h,(0,n.toDisplayString)(e.$t("modeling.components.Fields.5mpu393tdus0")),1),(0,n.createElementVNode)("div",y,(0,n.toDisplayString)(e.$t("modeling.components.Fields.5mpu393tdyc0")),1),(0,n.createElementVNode)("div",N,(0,n.toDisplayString)(e.$t("modeling.components.Fields.5mpu393td3o0")),1),(0,n.createElementVNode)("div",V,(0,n.toDisplayString)(e.$t("modeling.components.Fields.5mpu393te100")),1)]),((0,n.openBlock)(!0),(0,n.createElementBlock)(n.Fragment,null,(0,n.renderList)((0,n.unref)(j).list,((e,t)=>((0,n.openBlock)(),(0,n.createElementBlock)("div",{class:"d-row d-table-content",key:`item${t}`,onClick:t=>L(e)},[(0,n.createElementVNode)("div",x,[(0,n.withDirectives)((0,n.createElementVNode)("div",w,null,512),[[n.vShow,e.select]])]),(0,n.createElementVNode)("div",E,[(0,n.withDirectives)((0,n.createElementVNode)("input",{"onUpdate:modelValue":t=>e.name=t},null,8,S),[[n.vModelText,e.name]])]),(0,n.createElementVNode)("div",b,[(0,n.withDirectives)((0,n.createElementVNode)("select",{"onUpdate:modelValue":t=>e.type=t},[((0,n.openBlock)(!0),(0,n.createElementBlock)(n.Fragment,null,(0,n.renderList)((0,n.unref)(j).types,((e,t)=>((0,n.openBlock)(),(0,n.createElementBlock)("option",{key:`type${t}`,value:e},(0,n.toDisplayString)(e),9,C)))),128))],8,D),[[n.vModelSelect,e.type]])]),(0,n.createElementVNode)("div",$,[(0,n.withDirectives)((0,n.createElementVNode)("input",{"onUpdate:modelValue":t=>e.length=t,type:"number"},null,8,_),[[n.vModelText,e.length]])]),(0,n.createElementVNode)("div",Z,[(0,n.withDirectives)((0,n.createElementVNode)("input",{"onUpdate:modelValue":t=>e.decimal=t,type:"number"},null,8,B),[[n.vModelText,e.decimal]])]),(0,n.createElementVNode)("div",F,[(0,n.createVNode)((0,n.unref)(a.Z),{checked:e.isNull,"onUpdate:checked":t=>e.isNull=t},null,8,["checked","onUpdate:checked"])]),(0,n.createElementVNode)("div",U,[(0,n.createVNode)((0,n.unref)(a.Z),{checked:e.virtual,"onUpdate:checked":t=>e.virtual=t},null,8,["checked","onUpdate:checked"])]),(0,n.createElementVNode)("div",T,[(0,n.withDirectives)((0,n.createElementVNode)("input",{"onUpdate:modelValue":t=>e.key=t},null,8,O),[[n.vModelText,e.key]])]),(0,n.createElementVNode)("div",J,[(0,n.withDirectives)((0,n.createElementVNode)("input",{"onUpdate:modelValue":t=>e.note=t},null,8,M),[[n.vModelText,e.note]])])],8,k)))),128))])])])),_:1},8,["visible","confirm-loading"]))}}),z=l(83744);const R=(0,z.Z)(j,[["__scopeId","data-v-065398a0"]]);var I=R},82847:function(e,t,l){l.r(t),l.d(t,{default:function(){return m}});var n=l(70821),o=l(882),i=l(2811),a=l(38655),d=l(18933);const c={class:"cu-dialog"};var s=(0,n.defineComponent)({emits:["finish"],setup(e,{expose:t,emit:l}){const s=(0,n.reactive)({show:!1,loading:!1,placeholder:"",formData:{name:""},formModal:{name:""}}),r=e=>{s.show=!0,s.placeholder=e},m=(0,n.ref)(null),u=()=>{s.show=!1,(0,n.nextTick)((()=>{s.formData=JSON.parse(JSON.stringify(s.formModal)),m.value?.clearValidate(),m.value?.resetFields()}))},p=()=>{m.value?.validate().then((()=>{s.loading=!0,l("finish","rename",s.formData.name),s.loading=!1,u()})).catch()};return t({open:r}),(e,t)=>((0,n.openBlock)(),(0,n.createBlock)((0,n.unref)(o.Z),{class:"rename-container",visible:(0,n.unref)(s).show,"onUpdate:visible":t[1]||(t[1]=e=>(0,n.unref)(s).show=e),title:e.$t("modeling.components.Rename.5mpu3fm9iw00"),"ok-text":e.$t("modeling.components.Rename.5mpu3fm9jbw0"),"confirm-loading":(0,n.unref)(s).loading,"cancel-text":e.$t("modeling.components.Rename.5mpu3fm9jf40"),onOk:p,onCancel:u},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",c,[(0,n.createVNode)((0,n.unref)(i.Z),{model:(0,n.unref)(s).formData,ref:(e,t)=>{t["formRef"]=e,m.value=e},"label-col":{style:{width:"90px"}}},{default:(0,n.withCtx)((()=>[(0,n.createVNode)((0,n.unref)(a.Z),{name:"name",label:e.$t("modeling.components.Rename.5mpu3fm9jhc0"),rules:{required:!0,message:"please enter table name"}},{default:(0,n.withCtx)((()=>[(0,n.createVNode)((0,n.unref)(d.ZP),{"max-length":140,"show-word-limit":"",value:(0,n.unref)(s).formData.name,"onUpdate:value":t[0]||(t[0]=e=>(0,n.unref)(s).formData.name=e),placeholder:(0,n.unref)(s).placeholder},null,8,["value","placeholder"])])),_:1},8,["label"])])),_:1},8,["model"])])])),_:1},8,["visible","title","ok-text","confirm-loading","cancel-text"]))}});const r=s;var m=r},65361:function(e,t,l){l.r(t),l.d(t,{default:function(){return h}});var n=l(70821),o=l(27869),i=l(82212),a=l(72175),d=l(82847);const c={class:"header"},s={class:"name"},r={class:"fields"},m={class:"f-name"},u={class:"f-type"},p={key:0,class:"f-length"};var f=(0,n.defineComponent)({setup(e){const t=(0,n.inject)("getNode"),l=(0,n.inject)("getGraph"),f=(0,n.reactive)({name:this.$t("modeling.table.index.5mpu30zss2o0"),fields:[],antvSelected:!1});(0,n.onMounted)((()=>{if(t){let e=t();e.on("change:data",(({current:e})=>{f.antvSelected=e.antvSelected}))}}));const v=(0,n.ref)(null),g=(0,n.ref)(null),h=e=>{"fields"===e?v.value?.open(f.fields):"rename"===e&&g.value?.open(f.name)},y=(0,n.ref)(null),N=(e,l)=>{if("fields"===e){if(f.fields=JSON.parse(JSON.stringify(l)),t){let e=t();e.setData({fields:f.fields}),(0,n.nextTick)((()=>{e.setProp("size",{width:y.value?.offsetWidth,height:y.value?.offsetHeight})}))}}else if("rename"===e&&t){let e=t();f.name=l,e.setData({name:l})}},V=e=>{if(l&&t){let n=l(),o=t();"delete"===e?n.removeNode(o.id):"copy"===e?(n.copy([o]),n.paste()):"cut"===e&&n.cut([o])}};return(e,t)=>((0,n.openBlock)(),(0,n.createBlock)((0,n.unref)(i.ZP),{trigger:"contextMenu",alignPoint:"",style:{display:"block"}},{content:(0,n.withCtx)((()=>[(0,n.createVNode)((0,n.unref)(a.Z),{key:"1",onClick:t[1]||(t[1]=e=>V("cut"))},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("modeling.table.index.5mpu30zsqys0")),1)])),_:1}),(0,n.createVNode)((0,n.unref)(a.Z),{key:"2",onClick:t[2]||(t[2]=e=>V("copy"))},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("modeling.table.index.5mpu30zsrj00")),1)])),_:1}),(0,n.createVNode)((0,n.unref)(a.Z),{key:"3",onClick:t[3]||(t[3]=e=>V("delete"))},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("modeling.table.index.5mpu30zsroc0")),1)])),_:1}),(0,n.createVNode)((0,n.unref)(a.Z),{key:"3",onClick:t[4]||(t[4]=e=>h("rename"))},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("modeling.table.index.5mpu30zsrrw0")),1)])),_:1}),(0,n.createVNode)((0,n.unref)(a.Z),{key:"4"},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("modeling.table.index.5mpu30zsrvc0")),1)])),_:1})])),default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",{class:(0,n.normalizeClass)(["d-antv-node table-node-container",{"d-antv-node-selected":(0,n.unref)(f).antvSelected}]),ref:(e,t)=>{t["tableNodeRef"]=e,y.value=e},onDblclick:t[0]||(t[0]=e=>h("fields"))},[(0,n.createElementVNode)("div",c,[(0,n.createElementVNode)("div",s,(0,n.toDisplayString)((0,n.unref)(f).name),1)]),(0,n.withDirectives)((0,n.createElementVNode)("div",r,[((0,n.openBlock)(!0),(0,n.createElementBlock)(n.Fragment,null,(0,n.renderList)((0,n.unref)(f).fields,((e,t)=>((0,n.openBlock)(),(0,n.createElementBlock)("div",{class:"field",key:`field${t}`},[(0,n.createElementVNode)("div",m,(0,n.toDisplayString)(e.name)+"：",1),(0,n.createElementVNode)("div",u,(0,n.toDisplayString)(e.type),1),e.length||0===e.length?((0,n.openBlock)(),(0,n.createElementBlock)("div",p,"（"+(0,n.toDisplayString)(e.length)+"）",1)):(0,n.createCommentVNode)("",!0)])))),128))],512),[[n.vShow,0!==(0,n.unref)(f).fields.length]]),(0,n.createVNode)(o["default"],{ref:(e,t)=>{t["fieldsRef"]=e,v.value=e},onFinish:N},null,512),(0,n.createVNode)(d["default"],{ref:(e,t)=>{t["renameRef"]=e,g.value=e},onFinish:N},null,512)],34)])),_:1}))}}),v=l(83744);const g=(0,v.Z)(f,[["__scopeId","data-v-c4c50c72"]]);var h=g}}]);
//# sourceMappingURL=5361.ebf338ae.js.map