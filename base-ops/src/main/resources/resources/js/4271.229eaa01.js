"use strict";(self["webpackChunkweb_ui"]=self["webpackChunkweb_ui"]||[]).push([[4271,2467],{17702:function(e,t,o){o.r(t),o.d(t,{default:function(){return x}});o(57658);var n=o(70821),l=o(39518),s=o(40225),a=o(57330);const c=e=>((0,n.pushScopeId)("data-v-3392051b"),e=e(),(0,n.popScopeId)(),e),i={class:"exe-install-c"},r={key:0,class:"flex-col full-w full-h"},m={class:"mb-xlg"},p={class:"install-connect-c flex-col mb-lg"},d={class:"ft-b mb"},u={class:"mb"},v=c((()=>(0,n.createElementVNode)("span",{class:"content"},"gaussdb",-1))),g={class:"content"},N={class:"flex-row"},f={key:1,class:"flex-col full-w full-h ft-lg"},E={class:"flex-row"};var y;(function(e){e[e["UN_INSTALL"]=Number(-1)]="UN_INSTALL",e[e["SUCESS"]=Number(1)]="SUCESS",e[e["FAIL"]=Number(0)]="FAIL"})(y||(y={}));var C=(0,n.defineComponent)({setup(e,{expose:t}){const{t:o}=(0,a.QT)(),c=(0,s.BH)(),C=(0,n.ref)(y.UN_INSTALL),V=(0,n.ref)(!1),S=(0,n.ref)(""),x=(0,n.inject)("loading");(0,n.onMounted)((()=>{x.setNextBtnShow(!1),I()}));const I=()=>{V.value=!0;const e=Object.assign({},c.getInstallConfig);e.minimalistInstallConfig=c.getMiniConfig,e.liteInstallConfig=c.getLiteConfig,e.enterpriseInstallConfig=c.getEnterpriseConfig,x.toLoading(),(0,l.tA)(e).then((e=>{200===Number(e.code)?(x.setBackBtnShow(!1),x.setNextBtnShow(!1),C.value=y.SUCESS):(V.value=!1,x.cancelLoading(),C.value=y.FAIL,S.value=e.msg)})).catch((e=>{V.value=!1,x.cancelLoading(),C.value=y.FAIL,console.log(o("components.ExeImport.5mpmzg3ztak0"),e),S.value=e.toString()})).finally((()=>{x.cancelLoading(),V.value=!1}))},z=async()=>!0;return t({beforeConfirm:z}),(e,t)=>{const o=(0,n.resolveComponent)("svg-icon"),l=(0,n.resolveComponent)("a-button"),s=(0,n.resolveComponent)("icon-close-circle-fill"),a=(0,n.resolveComponent)("a-spin");return(0,n.openBlock)(),(0,n.createElementBlock)("div",i,[C.value===y.SUCESS?((0,n.openBlock)(),(0,n.createElementBlock)("div",r,[(0,n.createVNode)(o,{"icon-class":"ops-install-success",class:"icon-size mb"}),(0,n.createElementVNode)("div",m,(0,n.toDisplayString)(e.$t("components.ExeImport.5mpmzg3zqu80")),1),(0,n.createElementVNode)("div",p,[(0,n.createElementVNode)("div",d,(0,n.toDisplayString)(e.$t("components.ExeImport.5mpmzg3zrrw0")),1),(0,n.createElementVNode)("div",u,[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("components.ExeImport.5mpmzg3zs0o0"))+": ",1),v]),(0,n.createElementVNode)("div",null,[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("components.ExeImport.5mpmzg3zs6w0"))+": ",1),(0,n.createElementVNode)("span",g,(0,n.toDisplayString)((0,n.unref)(c).getMiniConfig.databasePassword),1)])]),(0,n.createElementVNode)("div",N,[(0,n.createVNode)(l,{type:"outline",class:"mr",onClick:t[0]||(t[0]=t=>e.$router.push({name:"Dashboard"}))},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("components.ExeImport.5mpmzg3zseo0")),1)])),_:1}),(0,n.createVNode)(l,{type:"primary",onClick:t[1]||(t[1]=t=>e.$router.push({name:"DailyOps"}))},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("components.ExeImport.5mpmzg3zskw0")),1)])),_:1})])])):(0,n.createCommentVNode)("",!0),C.value===y.FAIL?((0,n.openBlock)(),(0,n.createElementBlock)("div",f,[(0,n.createElementVNode)("div",E,[(0,n.createVNode)(s,{class:"mr",style:{color:"red",width:"24px",height:"24px"}}),(0,n.createTextVNode)(" "+(0,n.toDisplayString)(e.$t("components.ExeImport.5mpmzg3zsu80"))+" "+(0,n.toDisplayString)(S.value),1)])])):(0,n.createCommentVNode)("",!0),V.value?((0,n.openBlock)(),(0,n.createBlock)(a,{key:2,class:"flex-col full-w full-h",loading:V.value,tip:e.$t("components.ExeImport.5mpmzg3zt0g0")},null,8,["loading","tip"])):(0,n.createCommentVNode)("",!0)])}}}),V=o(83744);const S=(0,V.Z)(C,[["__scopeId","data-v-3392051b"]]);var x=S},92971:function(e,t,o){o.r(t),o.d(t,{default:function(){return E}});var n=o(70821),l=o(98535),s=o(40225);const a={class:"panel-c"},c={class:"flex-row-center panel-body"},i={class:"flex-row mb"},r={class:"instance-c"},m={class:"ft-main"},p={class:"flex-row mb"},d={class:"instance-type-c mr"},u={class:"instance-type-c"},v={class:"ft-main"};var g=(0,n.defineComponent)({setup(e){const t=(0,n.ref)(l.K0.SINGLE_NODE),o=e=>{t.value=e},g=(0,s.BH)();(0,n.onMounted)((()=>{N.value&&(t.value=N.value)})),(0,n.onBeforeUnmount)((()=>{g.setInstallContext({deployType:t.value})}));const N=(0,n.computed)((()=>g.getInstallConfig.deployType));return(e,s)=>{const g=(0,n.resolveComponent)("svg-icon"),N=(0,n.resolveComponent)("a-tag");return(0,n.openBlock)(),(0,n.createElementBlock)("div",a,[(0,n.createElementVNode)("div",c,[(0,n.createElementVNode)("div",{class:(0,n.normalizeClass)("deploy-size card-item-c mr-xlg "+("SINGLE_NODE"===t.value?"center-item-active":"center-item")),onClick:s[0]||(s[0]=e=>o((0,n.unref)(l.K0).SINGLE_NODE))},[(0,n.createElementVNode)("div",i,[(0,n.createVNode)(g,{"icon-class":"ops-host",class:"icon-size-s mr"}),(0,n.createElementVNode)("div",r,[(0,n.createVNode)(g,{"icon-class":"ops-instance",class:"icon-size-s"})])]),(0,n.createElementVNode)("div",m,(0,n.toDisplayString)(e.$t("simple.DeployWay.5mpmphlozp00")),1)],2),(0,n.createElementVNode)("div",{class:(0,n.normalizeClass)("deploy-cluster-size card-item-c "+("CLUSTER"===t.value?"center-item-active":"center-item")),onClick:s[1]||(s[1]=e=>o((0,n.unref)(l.K0).CLUSTER))},[(0,n.createElementVNode)("div",p,[(0,n.createVNode)(g,{"icon-class":"ops-host",class:"icon-size-s mr"}),(0,n.createElementVNode)("div",d,[(0,n.createVNode)(N,{class:"type"},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("simple.DeployWay.5mpmphlp0eo0")),1)])),_:1}),(0,n.createVNode)(g,{"icon-class":"ops-instance",class:"icon-size-s"})]),(0,n.createElementVNode)("div",u,[(0,n.createVNode)(N,{class:"type"},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("simple.DeployWay.5mpmphlp0k00")),1)])),_:1}),(0,n.createVNode)(g,{"icon-class":"ops-instance",class:"icon-size-s"})])]),(0,n.createElementVNode)("div",v,(0,n.toDisplayString)(e.$t("simple.DeployWay.5mpmphlp0nw0")),1)],2)])])}}}),N=o(83744);const f=(0,N.Z)(g,[["__scopeId","data-v-14879bf5"]]);var E=f}}]);
//# sourceMappingURL=4271.229eaa01.js.map