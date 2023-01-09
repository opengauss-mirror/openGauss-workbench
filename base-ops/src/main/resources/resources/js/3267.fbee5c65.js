"use strict";(self["webpackChunkweb_ui"]=self["webpackChunkweb_ui"]||[]).push([[3267,733],{89965:function(e,t,a){a.d(t,{AH:function(){return g},Bd:function(){return p},C7:function(){return T},DG:function(){return L},Du:function(){return I},H7:function(){return Z},Iz:function(){return _},J6:function(){return F},Jh:function(){return b},NQ:function(){return E},QS:function(){return S},Qt:function(){return C},R7:function(){return x},Rq:function(){return c},SB:function(){return m},SW:function(){return D},UL:function(){return f},Up:function(){return w},WK:function(){return y},Yn:function(){return z},gp:function(){return d},j9:function(){return K},nB:function(){return k},qR:function(){return V},qo:function(){return v},sZ:function(){return B},v$:function(){return u},vs:function(){return i},w0:function(){return Q},wB:function(){return O},x9:function(){return J},xl:function(){return N},yA:function(){return s},yf:function(){return $}});var n=a(15784),l=a(9669),o=a.n(l),r=a(90989);const i=(e,t,a,n)=>o().get(`/modeling/dataflow/dataSourceDb/getTablesBySchema/${e}/${t}/${a}`,n||{}),s=(e,t)=>o().get(`/modeling/dataflow/dataSourceDb/getFieldsByTable/${e}`,t||{}),d=e=>o().get("/modeling/dataflow/list",{params:e}),u=e=>o().post("/modeling/dataflow/add",e||{}),c=e=>o()["delete"](`/modeling/dataflow/delete/${e}`),f=e=>o().put("/modeling/dataflow/edit",e||{}),p=e=>o().get(`/modeling/dataflow/getById/${e}`),m=(e,t)=>o().get(`/modeling/dataflow/dataSourceDb/getSchemaByClusterNodeId/${e}/${t}`,e||{}),g=e=>o().post("/modeling/dataflow/getSql",e||{}),b=e=>o().post("/modeling/dataflow/runSql",e||{});let h=!0;const w=(e,t,a)=>new Promise((l=>{h&&(h=!1,o().put("/modeling/dataflow/edit",{id:e,operatorContent:JSON.stringify(t)}).then((e=>{l(!0),h=!0,200===Number(e.code)&&(a||n.Z.success({content:r.Z.global.t("modeling.dy_common.saveSuccess")}))})).catch((()=>{l(!1),a||n.Z.error({content:r.Z.global.t("modeling.dy_common.saveFailed")}),h=!0})))})),v=e=>o().get("/modeling/dataflow/dataSourceDb/list",e||{}),y=e=>o().post("/modeling/dataflow/getResultFieldsByOperator",e||{}),D=e=>o().post("/modeling/dataflow/visualization/params/add",e||{}),$=e=>o().put("/modeling/dataflow/visualization/params/edit",e||{}),x=e=>o()["delete"](`/modeling/dataflow/visualization/params/delete/${e}`),N=e=>o().post("/modeling/dataflow/visualization/params/generateChart",e||{}),k=(e,t)=>o().get(`/modeling/dataflow/visualization/params/getListByOperatorId/${e}`,t||{}),V=e=>o().post("/modeling/dataflow/visualization/snapshot/add",e||{}),S=(e,t)=>o().get(`/modeling/dataflow/visualization/snapshot/getListByDataFlowId/${e}`,t||{}),C=e=>o()["delete"](`/modeling/dataflow/visualization/snapshot/delete/${e}`),B=(e,t)=>o().get(`/modeling/dataflow/visualization/report/getListByDataFlowId/${e}`,{params:t||{}}),O=e=>o().post("/modeling/dataflow/visualization/report/add",e||{}),T=e=>o().put("/modeling/dataflow/visualization/report/update",e||{}),_=e=>o()["delete"](`/modeling/dataflow/visualization/report/delete/${e}`),I=e=>o().get(`/modeling/dataflow/visualization/report/share/getByReportId/${e}`),z=(e,t)=>o().get(`/modeling/dataflow/visualization/customDimension/getListByOperatorId/${e}`,{params:t||{}}),F=e=>o().post("/modeling/dataflow/visualization/customDimension/add",e||{}),E=e=>o().put("/modeling/dataflow/visualization/customDimension/edit",e||{}),Z=e=>o()["delete"](`/modeling/dataflow/visualization/customDimension/delete/${e}`),J=(e,t)=>o().get(`/modeling/visualization/report/share/getMapGeo/${e}`,t||{}),L=e=>o().post("/modeling/dataflow/visualization/params/uploadGeo",e||{}),Q=(e,t)=>o().get(`/modeling/dataflow/visualization/params/getGeo/${e}`,{params:t||{}}),K=e=>o()["delete"](`/modeling/dataflow/visualization/params/deleteGeo/${e}`)},8603:function(e,t,a){a.d(t,{M:function(){return i}});a(57658);var n=a(89965),l=a(95684),o=a(15784);const r={graph:null,dataFlowInfo:{},rawData:{},useData:{},useDatabase:"",useTable:[]},i=(0,l.Q_)("dataFlow",{state(){return JSON.parse(JSON.stringify(r))},getters:{getTableSelectList:e=>e.useDatabase?e.useData[e.useDatabase]:[],getFieldsSelectList:e=>{if(e.useDatabase&&e.useTable.length>0){const t=e.useData[e.useDatabase],a=[];return e.useTable.forEach((e=>{const n={group:e,fields:[]},l=t.findIndex((t=>t.tablename===e));-1!==l&&(n.fields=t[l].fields),a.push(n)})),a}return[]},getFlowDataInfo:e=>e.dataFlowInfo,getFieldsByTable:e=>t=>{if(e.useDatabase){const a=e.useData[e.useDatabase];if(0===t||"0"===t){const t=a.findIndex((t=>t.tablename===e.useTable[0]));return-1===t?[]:a[t].fields}{const e=a.findIndex((e=>e.tablename===t));return-1===e?[]:a[e].fields}}return[]},getUseTable:e=>e.useTable,getUseDatabase:e=>e.useDatabase},actions:{setGraph(e){this.$patch((t=>{t.graph=e}))},initState(){return new Promise((e=>{this.$patch((t=>{t.useData=[],t.useDatabase="",t.useTable=[],t.rawData=[],t.dataFlowInfo=[],e(!0)}))}))},clearUse(){return new Promise((e=>{this.$patch((t=>{t.useDatabase="",t.useTable=[],e(!0)}))}))},setFlowDataInfo(e){this.$patch((t=>t.dataFlowInfo=e))},setDatabaseInfo(e,t,a){return new Promise(((l,r)=>{this.$patch((i=>{i.useDatabase=`${e}|,|${t}|,|${a}`,i.useTable=[],Object.keys(i.rawData).includes(`${e}|,|${t}|,|${a}`)?l(!0):(0,n.vs)(e,t,a).then((n=>{if(200===Number(n.code)){const o=n.data.map((e=>({...e,tables:{}})));i.rawData[`${e}|,|${t}|,|${a}`]=o,i.useData[`${e}|,|${t}|,|${a}`]=o,l(!0)}else o.Z.error("database not found"),r(!0)})).catch((()=>{o.Z.error("database not found"),r(!0)}))}))}))},setDatabaseTableInfo(e,t){return new Promise((a=>{this.$patch((l=>{if(e){t&&(t.tableKey||0===t.tableKey)?l.useTable[t.tableKey]=e:t&&t.noUse||l.useTable.push(e);const o=l.rawData[l.useDatabase].findIndex((t=>t.tablename===e));Object.keys(l.rawData[l.useDatabase][o].tables).includes(e)?a(!1):(0,n.yA)(`${l.useDatabase.split("|,|").join("/")}/${e}`).then((e=>{a(!0),l.rawData[l.useDatabase][o].fields=e.data,l.useData[l.useDatabase][o].fields=e.data}))}else a(!1)}))}))},removeDatabaseTable(e){return new Promise((t=>{this.$patch((a=>{const n=a.useTable.indexOf(e);-1!==n?(a.useTable.splice(n,1),t(!0)):t(!1)}))}))}}})},733:function(e,t,a){a.r(t),a.d(t,{checkDisabled:function(){return n},saveData:function(){return l}});const n=(e,t,a)=>{if(!e||0===e.length)return!1;if("string"===typeof e[0]||"number"===typeof e[0])return-1!==e.indexOf(t);{const n=e;return-1!==n.findIndex((e=>e[a||"value"]===t))}},l=(e,t,a)=>{if(console.log(e,t,a),a){const n=a.getData();a?.setData({...n,[e]:JSON.parse(JSON.stringify(t))},{overwrite:!0})}}},63736:function(e,t,a){a.r(t),a.d(t,{default:function(){return s}});var n=a(70821),l=a(29067),o=(0,n.defineComponent)({props:{content:{type:String,required:!0},text:{type:String,required:!0},otherWidth:{type:Number,required:!1},fontSize:{type:Number,required:!1}},setup(e){const t=e,a=(0,n.ref)(),o=(0,n.ref)(!1),r=()=>{let e=a.value.parentNode.offsetWidth,n=a.value.offsetWidth+(t.otherWidth||0===t.otherWidth?t.otherWidth:24);o.value=!(n>e)};return(e,i)=>((0,n.openBlock)(),(0,n.createBlock)((0,n.unref)(l.Z),{content:t.content,disabled:o.value},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("span",{onMouseover:r},[(0,n.renderSlot)(e.$slots,"default")],32),(0,n.createElementVNode)("span",{class:"get-width",ref:(e,t)=>{t["contentRef"]=e,a.value=e}},(0,n.toDisplayString)(t.text),513)])),_:3},8,["content","disabled"]))}}),r=a(83744);const i=(0,r.Z)(o,[["__scopeId","data-v-6f1614ab"]]);var s=i},73267:function(e,t,a){a.r(t),a.d(t,{default:function(){return k}});a(57658);var n=a(70821),l=a(63736),o=a(96336),r=a(73019),i=a(62e3),s=a(38655),d=a(51121),u=a(2768),c=a(8603),f=a(733);const p={class:"tab-content d-a-form"},m={class:"select-comp-container"},g={class:"d-form-item-label mb-s"},b={class:"label-text"},h={class:"select-field-container"},w=["onClick"],v={class:"warning"},y={class:"title"},D={class:"content"};var $=(0,n.defineComponent)({setup(e,{expose:t}){const a=o.ZP.Option,$=o.ZP.OptGroup,x=(0,c.M)(),N=(0,n.computed)((()=>x.getUseTable));(0,n.watch)((()=>x.useTable),(()=>{I.fields.forEach((e=>e.value=null)),Z.fields.forEach((e=>e.key=null))}),{deep:!0});let k=null;const V=(0,n.ref)(),S=e=>{k=e;let{data:t}=e;I.fields=t.fields?t.fields:[],I.table=t.table,E.source=t.source?t.source:[],Z.alias=t.outputAlias?t.outputAlias:"",Z.fields=t.outputFields?t.outputFields:[],L.oldValue=t.table?t.table:""};let C=!1;const B=e=>{if("4"===e){if(C)return;(0,n.nextTick)((()=>{V.value&&k?.data.visualConfig&&V.value.init(k?.data.visualConfig)}))}},O=(0,n.computed)((()=>x.getTableSelectList)),T=(0,n.reactive)({activeKey:"1"}),_=(e,t)=>{"table"===t&&Q()},I=(0,n.reactive)({table:void 0,tableInfo:{},fields:[]}),z=(0,n.computed)((()=>x.getFieldsSelectList)),F=(e,t)=>{"add"===e?I.fields.push({value:void 0}):"delete"===e&&I.fields.splice(t,1),J("fields",I.fields)},E=(0,n.reactive)({source:[]}),Z=(0,n.reactive)({fields:[],alias:""}),J=(e,t)=>{if(k){let a=k.getData();k?.setData({...a,[e]:JSON.parse(JSON.stringify(t))},{overwrite:!0})}},L=(0,n.reactive)({show:!1,loading:!1,oldValue:""}),Q=()=>{L.oldValue?L.show=!0:U()},K=e=>{L.show=!1,e||(I.table=L.oldValue)},U=()=>{L.loading=!0,x.setDatabaseTableInfo(I.table,{tableKey:0}).then((()=>{L.loading=!1,L.oldValue=JSON.parse(JSON.stringify(I.table)),K(!0),J("table",I.table)})).catch((()=>{L.loading=!1,L.oldValue=JSON.parse(JSON.stringify(I.table)),K(!0),J("table",I.table)}))};return t({init:S}),(e,t)=>{const c=(0,n.resolveComponent)("icon-exclamation-circle-fill"),x=(0,n.resolveComponent)("a-modal");return(0,n.openBlock)(),(0,n.createElementBlock)(n.Fragment,null,[(0,n.createVNode)((0,n.unref)(r.Z),{class:"d-a-tabs",activeKey:(0,n.unref)(T).activeKey,"onUpdate:activeKey":t[4]||(t[4]=e=>(0,n.unref)(T).activeKey=e),centered:"",size:"mini",tabBarGutter:40,tabBarStyle:{color:"#fff"},onChange:B},{default:(0,n.withCtx)((()=>[(0,n.createVNode)((0,n.unref)(i.Z),{key:"1",title:e.$t("modeling.base.QueryOperator.5m7hhed6am80")},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",p,[(0,n.createVNode)((0,n.unref)(s.Z),{label:e.$t("modeling.base.QueryOperator.5m7hhed94ik0"),labelCol:{span:6,offset:0},labelAlign:"left",colon:!1},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",m,[(0,n.createVNode)((0,n.unref)(o.ZP),{"popup-container":".select-comp-container",modelValue:(0,n.unref)(I).table,"onUpdate:modelValue":t[0]||(t[0]=e=>(0,n.unref)(I).table=e),placeholder:e.$t("modeling.base.QueryOperator.5m7hhed94qc0"),allowSearch:"","trigger-props":{contentClass:"d-a-select-dropdown"},onChange:t[1]||(t[1]=e=>_(e,"table"))},{label:(0,n.withCtx)((({data:e})=>[(0,n.createVNode)(l["default"],{text:e?.label,content:e?.label,"other-width":0},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e?.label),1)])),_:2},1032,["text","content"])])),default:(0,n.withCtx)((()=>[((0,n.openBlock)(!0),(0,n.createElementBlock)(n.Fragment,null,(0,n.renderList)((0,n.unref)(O),((e,t)=>((0,n.openBlock)(),(0,n.createBlock)(l["default"],{text:e.tablename,key:t,content:e.tablename},{default:(0,n.withCtx)((()=>[(0,n.createVNode)((0,n.unref)(a),{value:e.tablename,disabled:(0,n.unref)(f.checkDisabled)((0,n.unref)(N),e.tablename)},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.tablename),1)])),_:2},1032,["value","disabled"])])),_:2},1032,["text","content"])))),128))])),_:1},8,["modelValue","placeholder"])])])),_:1},8,["label"]),(0,n.createElementVNode)("div",g,[(0,n.createElementVNode)("div",b,(0,n.toDisplayString)(e.$t("modeling.base.QueryOperator.5m7hhed94tg0")),1),(0,n.createElementVNode)("div",{class:"d-control-add",onClick:t[2]||(t[2]=e=>F("add"))},"+")]),((0,n.openBlock)(!0),(0,n.createElementBlock)(n.Fragment,null,(0,n.renderList)((0,n.unref)(I).fields,((r,i)=>((0,n.openBlock)(),(0,n.createBlock)((0,n.unref)(d.Z),{align:"center",key:`configFields${i}`,class:"mb-s"},{default:(0,n.withCtx)((()=>[(0,n.createVNode)((0,n.unref)(u.Z),{span:21,class:"mr-s"},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",h,[(0,n.createVNode)((0,n.unref)(o.ZP),{"popup-container":".select-field-container",modelValue:r.value,"onUpdate:modelValue":e=>r.value=e,placeholder:e.$t("modeling.base.QueryOperator.5m7hhed94w80"),"trigger-props":{contentClass:"d-a-select-dropdown"},allowSearch:"",style:{width:"100%"},onChange:t[3]||(t[3]=e=>J("fields",(0,n.unref)(I).fields))},{label:(0,n.withCtx)((({data:e})=>[(0,n.createVNode)(l["default"],{text:e?.label,content:e?.label,"other-width":0},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e?.label),1)])),_:2},1032,["text","content"])])),default:(0,n.withCtx)((()=>[((0,n.openBlock)(!0),(0,n.createElementBlock)(n.Fragment,null,(0,n.renderList)((0,n.unref)(z),((e,t)=>((0,n.openBlock)(),(0,n.createBlock)((0,n.unref)($),{key:`fieldsGroup${t}`,label:e.group},{label:(0,n.withCtx)((()=>[(0,n.createVNode)(l["default"],{text:e.group,content:e.group},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.group),1)])),_:2},1032,["text","content"])])),default:(0,n.withCtx)((()=>[((0,n.openBlock)(!0),(0,n.createElementBlock)(n.Fragment,null,(0,n.renderList)(e.fields,((t,o)=>((0,n.openBlock)(),(0,n.createBlock)(l["default"],{text:t.name,key:`field${o}`,content:`${e.group} . ${t.name}`},{default:(0,n.withCtx)((()=>[(0,n.createVNode)((0,n.unref)(a),{value:`${e.group}.${t.name}`,disabled:(0,n.unref)(f.checkDisabled)((0,n.unref)(I).fields,`${e.group}.${t.name}`,"value")},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(t.name),1)])),_:2},1032,["value","disabled"])])),_:2},1032,["text","content"])))),128))])),_:2},1032,["label"])))),128))])),_:2},1032,["modelValue","onUpdate:modelValue","placeholder"])])])),_:2},1024),(0,n.createVNode)((0,n.unref)(u.Z),{span:2},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",{class:"d-control-remove",onClick:e=>F("delete",i)},"-",8,w)])),_:2},1024)])),_:2},1024)))),128))])])),_:1},8,["title"])])),_:1},8,["activeKey"]),(0,n.createVNode)(x,{visible:(0,n.unref)(L).show,"ok-loading":(0,n.unref)(L).loading,closable:!1,simple:"","ok-text":e.$t("modeling.base.QueryOperator.5m7hr5m93c80"),"cancel-text":e.$t("modeling.base.QueryOperator.5m7hr5m943c0"),onOk:U,onCancel:K},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",v,[(0,n.createElementVNode)("div",y,[(0,n.createVNode)(c,{color:"#ff7d00"}),(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("modeling.base.QueryOperator.5m7hr5m948k0")),1)]),(0,n.createElementVNode)("div",D,(0,n.toDisplayString)(e.$t("modeling.base.QueryOperator.5m7hr5m94c00")),1)])])),_:1},8,["visible","ok-loading","ok-text","cancel-text"])],64)}}}),x=a(83744);const N=(0,x.Z)($,[["__scopeId","data-v-d7344768"]]);var k=N}}]);
//# sourceMappingURL=3267.fbee5c65.js.map