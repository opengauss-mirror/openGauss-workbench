"use strict";(self["webpackChunkweb_ui"]=self["webpackChunkweb_ui"]||[]).push([[7379],{39518:function(e,t,o){o.d(t,{$4:function(){return B},Ac:function(){return c},BL:function(){return k},C4:function(){return O},DF:function(){return x},GD:function(){return R},IW:function(){return z},K4:function(){return L},Kf:function(){return $},LR:function(){return l},Lf:function(){return s},Ox:function(){return b},PK:function(){return u},R1:function(){return I},Sv:function(){return M},T2:function(){return m},T4:function(){return A},TL:function(){return y},TY:function(){return F},Uw:function(){return P},We:function(){return _},YQ:function(){return C},Ye:function(){return v},ZA:function(){return H},_V:function(){return f},c0:function(){return a},gi:function(){return V},h:function(){return w},jQ:function(){return W},jV:function(){return T},nT:function(){return g},sT:function(){return N},tA:function(){return i},ur:function(){return E},w3:function(){return d},xj:function(){return p},y6:function(){return S},zs:function(){return D},zx:function(){return h}});var r=o(9669),n=o.n(r);const s=()=>n().get("encryption/getKey"),l=e=>n().post("opsCluster/download",e||{}),a=e=>n().get(`opsCluster/env/${e}`),c=e=>n().post("opsCluster/install",e),i=e=>n().post("opsCluster/import",e),u=e=>n().post("opsCluster/quickInstall",e),p=e=>n().get("opsCluster/check",{params:e}),d=e=>n().post("opsCluster/uninstall",e),m=e=>n().get("opsCluster/hasName",{params:e}),f=()=>n().get("opsCluster/listCluster"),v=e=>n().post("opsCluster/ssh",e),g=e=>n().get("opsCluster/monitor",{params:e}),k=e=>n().post("opsCluster/start",e),N=e=>n().post("opsCluster/stop",e),E=e=>n().post("opsCluster/restart",e),y=e=>n().get("opsCluster/listHost",{params:e}),w=e=>n().get(e?`opsCluster/listInstallPackage?path=${e}`:"opsCluster/listInstallPackage"),h=()=>n().get("host/listAll"),C=e=>n().get(`hostUser/listAllWithoutRoot/${e}`),B=()=>n().get("az/listAll"),x=e=>n().get("opsCluster/logPath",{params:e}),V=e=>n().get("opsCluster/ls",{params:e}),P=e=>n().get("opsCluster/slowSql",{params:e}),R=e=>n().get("opsCluster/auditLog",{params:e}),M=e=>n().get("opsCluster/download",{params:e}),S=e=>n().get("wdr/list",{params:e}),I=e=>n().post("wdr/generate",e),D=e=>n()["delete"]("wdr/del/"+e),b=e=>n().get("wdr/createSnapshot",{params:e}),L=e=>n().get("wdr/downloadWdr",{params:e}),O=e=>n().get("wdr/listSnapshot",{params:e}),z=e=>n().post("backup/backup",e),A=e=>n().get("backup/page",{params:e}),$=(e,t)=>n().post("backup/recover/"+e,t),T=e=>n()["delete"]("backup/del/"+e),_=e=>n().get("installPackageManager/page",{params:e}),F=e=>n()["delete"]("installPackageManager/"+e),H=e=>n().post("installPackageManager/save",e),W=(e,t)=>n().put(`installPackageManager/update/${e}`,t)},83744:function(e,t){t.Z=(e,t)=>{const o=e.__vccOpts||e;for(const[r,n]of t)o[r]=n;return o}},97379:function(e,t,o){o.r(t),o.d(t,{default:function(){return _}});o(57658);var r=o(70821),n=o(39518),s=o(40225),l=o(98535),a=o(15784),c=o(57330);const i={class:"env-monitor-c"},u={class:"flex-col"},p={class:"flex-col-start"},d={class:"flex-row"},m={class:"env-item-c flex-between full-w mb"},f={class:"flex-row"},v={class:"flex-row"},g={key:0},k={class:"flex-row mb"},N={class:"ft-xlg"},E={class:"hardware-env"},y={key:0,class:"flex-col"},w={style:{width:"250px"},class:"flex-row"},h={class:"mr"},C={style:{"min-width":"500px"},class:"flex-row-start"},B={class:"mr"},x={class:"flex-row mb"},V={class:"ft-xlg"},P={class:"hardware-env"},R={key:0,class:"flex-col"},M={style:{width:"250px"},class:"flex-row"},S={class:"mr"},I={style:{"min-width":"500px"},class:"flex-row-start"},D={class:"mr"},b={key:1},L={key:1},O={key:2};var z;(function(e){e["NORMAL"]="NORMAL",e["WARMING"]="WARMING",e["ERROR"]="ERROR",e["INFO"]="INFO"})(z||(z={}));var A=(0,r.defineComponent)({setup(e,{expose:t}){const{t:o}=(0,c.QT)(),A=(0,s.BH)(),$=(0,r.inject)("loading"),T=(0,r.reactive)({nodeData:[]}),_=(0,r.ref)(null);(0,r.onMounted)((()=>{$.toLoading(),T.nodeData=[],F(),T.nodeData.forEach((e=>{e.loading=!0,(0,n.c0)(e.hostId).then((t=>{200===Number(t.code)&&(e.result=200,e.hardwareEnv.envProperties=t.data.hardwareEnv.envProperties,e.softwareEnv.envProperties=t.data.softwareEnv.envProperties,G(e))})).catch((t=>{e.result=500,e.errMsg=t})).finally((()=>{e.loading=!1}))})),_.value=setInterval((()=>{const e=T.nodeData.filter((e=>200!==e.result&&500!==e.result));0===e.length&&(clearInterval(_.value),$.cancelLoading())}),100)}));const F=()=>{T.nodeData=[],A.getEnterpriseConfig.nodeConfigList.forEach((e=>{const t=T.nodeData.find((t=>t.privateIp===e.privateIp));if(!t){const t=H();t.hostId=e.hostId,t.privateIp=e.privateIp,t.publicIp=e.publicIp,t.clusterRole=e.clusterRole,T.nodeData.push(t)}}))},H=()=>({result:-1,errMsg:"",noPassNum:0,noPassNumHard:0,noPassNumSoft:0,isShow:!0,loading:!1,hostId:"",clusterRole:"",privateIp:"",publicIp:"",hardwareEnv:{envProperties:[]},softwareEnv:{envProperties:[]}}),W=e=>{$.toLoading(),e.loading=!0,(0,n.c0)(e.hostId).then((t=>{200===Number(t.code)?(e.result=200,e.hardwareEnv.envProperties=t.data.hardwareEnv.envProperties,e.softwareEnv.envProperties=t.data.softwareEnv.envProperties,G(e)):(e.result=500,e.errMsg=t.msg)})).catch((t=>{e.result=500,e.errMsg=t})).finally((()=>{e.loading=!1,$.cancelLoading()}))},G=e=>{e.hardwareEnv.envProperties.forEach((t=>{t.status===z.ERROR&&(e.noPassNum=e.noPassNum+1,e.noPassNumHard=e.noPassNumHard+1)})),e.softwareEnv.envProperties.forEach((t=>{t.status===z.ERROR&&(e.noPassNum=e.noPassNum+1,e.noPassNumHard=e.noPassNumHard+1)}))},j=e=>{switch(e.status){case z.ERROR:return"red";case z.INFO:return"gray";case z.NORMAL:return"green";default:return"orange"}},K=e=>{switch(e){case l._.MASTER:return o("enterprise.EnvMonitor.5mpm5p9xfzo0");case l._.SLAVE:return o("enterprise.EnvMonitor.5mpm5p9xg3o0");default:return o("enterprise.EnvMonitor.5mpm5p9xg700")}},Z=()=>{const e=T.nodeData.filter((e=>200!==e.result));let t=!0;return e.length>0&&(t=!1,a.Z.warning("If the host fails to be detected, configure the host and re-detect the host for installation")),t};return t({beforeConfirm:Z}),(e,t)=>{const o=(0,r.resolveComponent)("a-badge"),n=(0,r.resolveComponent)("a-alert"),s=(0,r.resolveComponent)("a-tag"),l=(0,r.resolveComponent)("icon-refresh"),a=(0,r.resolveComponent)("a-button"),c=(0,r.resolveComponent)("icon-down"),A=(0,r.resolveComponent)("icon-up"),$=(0,r.resolveComponent)("icon-exclamation-circle-fill"),_=(0,r.resolveComponent)("icon-check-circle-fill"),F=(0,r.resolveComponent)("icon-close-circle-fill"),H=(0,r.resolveComponent)("icon-info-circle-fill"),G=(0,r.resolveComponent)("a-spin"),Z=(0,r.resolveComponent)("a-divider");return(0,r.openBlock)(),(0,r.createElementBlock)("div",i,[(0,r.createElementVNode)("div",u,[((0,r.openBlock)(!0),(0,r.createElementBlock)(r.Fragment,null,(0,r.renderList)((0,r.unref)(T).nodeData,((t,i)=>((0,r.openBlock)(),(0,r.createElementBlock)("div",{class:"mb",style:{width:"50%"},key:i},[(0,r.createVNode)(G,{class:"full-w",loading:t.loading,tip:e.$t("enterprise.EnvMonitor.5mpm5p9xees0")},{default:(0,r.withCtx)((()=>[(0,r.createElementVNode)("div",p,[-1!==t.result&&t.noPassNum>0?((0,r.openBlock)(),(0,r.createBlock)(n,{key:0,class:"mb",style:{width:"fit-content"},type:"error"},{default:(0,r.withCtx)((()=>[(0,r.createElementVNode)("div",d,[(0,r.createTextVNode)((0,r.toDisplayString)(e.$t("enterprise.EnvMonitor.5mpm5p9xf4w0"))+" ",1),(0,r.createVNode)(o,{count:t.noPassNum},null,8,["count"]),(0,r.createTextVNode)((0,r.toDisplayString)(e.$t("enterprise.EnvMonitor.else1")),1)])])),_:2},1024)):(0,r.createCommentVNode)("",!0),(0,r.createElementVNode)("div",m,[(0,r.createElementVNode)("div",f,[(0,r.createVNode)(s,{class:"mr",color:"#86909C"},{default:(0,r.withCtx)((()=>[(0,r.createTextVNode)((0,r.toDisplayString)(K(t.clusterRole)),1)])),_:2},1024),(0,r.createTextVNode)(" "+(0,r.toDisplayString)(e.$t("enterprise.EnvMonitor.else2"))+": "+(0,r.toDisplayString)(t.privateIp)+"("+(0,r.toDisplayString)(t.publicIp)+") ",1)]),(0,r.createElementVNode)("div",v,[(0,r.createVNode)(a,{class:"mr",type:"text",long:"",onClick:e=>W(t)},{icon:(0,r.withCtx)((()=>[(0,r.createVNode)(l)])),default:(0,r.withCtx)((()=>[(0,r.createTextVNode)(" "+(0,r.toDisplayString)(e.$t("enterprise.EnvMonitor.5mpm5p9xfb80")),1)])),_:2},1032,["onClick"]),t.isShow?((0,r.openBlock)(),(0,r.createBlock)(A,{key:1,style:{cursor:"pointer"},onClick:e=>t.isShow=!1},null,8,["onClick"])):((0,r.openBlock)(),(0,r.createBlock)(c,{key:0,style:{cursor:"pointer"},onClick:e=>t.isShow=!0},null,8,["onClick"]))])]),(0,r.withDirectives)((0,r.createElementVNode)("div",null,[200===t.result?((0,r.openBlock)(),(0,r.createElementBlock)("div",g,[(0,r.createElementVNode)("div",k,[t.noPassNumHard>0?((0,r.openBlock)(),(0,r.createBlock)($,{key:0,class:"mr",style:{color:"orange"},size:30})):((0,r.openBlock)(),(0,r.createBlock)(_,{key:1,style:{color:"green"},class:"mr",size:30})),(0,r.createElementVNode)("div",N,(0,r.toDisplayString)(e.$t("enterprise.EnvMonitor.5mpm5p9xff40")),1)]),(0,r.createElementVNode)("div",E,[t.hardwareEnv.envProperties.length?((0,r.openBlock)(),(0,r.createElementBlock)("div",y,[((0,r.openBlock)(!0),(0,r.createElementBlock)(r.Fragment,null,(0,r.renderList)(t.hardwareEnv.envProperties,((e,t)=>((0,r.openBlock)(),(0,r.createElementBlock)("div",{class:"flex-row mb",key:t},[(0,r.createElementVNode)("div",w,[(0,r.createElementVNode)("div",h,(0,r.toDisplayString)(e.name)+":",1),(0,r.createElementVNode)("div",null,(0,r.toDisplayString)(e.value),1)]),(0,r.createElementVNode)("div",C,[(0,r.createElementVNode)("div",B,[e.status===z.ERROR?((0,r.openBlock)(),(0,r.createBlock)(F,{key:0,style:{color:"red"},size:20})):(0,r.createCommentVNode)("",!0),e.status===z.NORMAL?((0,r.openBlock)(),(0,r.createBlock)(_,{key:1,style:{color:"green"},size:20})):(0,r.createCommentVNode)("",!0),e.status===z.WARMING?((0,r.openBlock)(),(0,r.createBlock)($,{key:2,style:{color:"orange"},size:20})):(0,r.createCommentVNode)("",!0),e.status===z.INFO?((0,r.openBlock)(),(0,r.createBlock)(H,{key:3,style:{color:"gray"},size:20})):(0,r.createCommentVNode)("",!0)]),(0,r.createElementVNode)("div",{style:(0,r.normalizeStyle)("color: "+j(e))},(0,r.toDisplayString)(e.statusMessage),5)])])))),128))])):(0,r.createCommentVNode)("",!0)]),(0,r.createElementVNode)("div",x,[t.noPassNumSoft>0?((0,r.openBlock)(),(0,r.createBlock)($,{key:0,class:"mr",style:{color:"orange"},size:30})):((0,r.openBlock)(),(0,r.createBlock)(_,{key:1,style:{color:"green"},class:"mr",size:30})),(0,r.createElementVNode)("div",V,(0,r.toDisplayString)(e.$t("enterprise.EnvMonitor.5mpm5p9xfk80")),1)]),(0,r.createElementVNode)("div",P,[t.softwareEnv.envProperties.length?((0,r.openBlock)(),(0,r.createElementBlock)("div",R,[((0,r.openBlock)(!0),(0,r.createElementBlock)(r.Fragment,null,(0,r.renderList)(t.softwareEnv.envProperties,((e,t)=>((0,r.openBlock)(),(0,r.createElementBlock)("div",{class:"flex-row mb",key:t},[(0,r.createElementVNode)("div",M,[(0,r.createElementVNode)("div",S,(0,r.toDisplayString)(e.name)+":",1),(0,r.createElementVNode)("div",null,(0,r.toDisplayString)(e.value),1)]),(0,r.createElementVNode)("div",I,[(0,r.createElementVNode)("div",D,[e.status===z.ERROR?((0,r.openBlock)(),(0,r.createBlock)(F,{key:0,style:{color:"red"},size:20})):(0,r.createCommentVNode)("",!0),e.status===z.NORMAL?((0,r.openBlock)(),(0,r.createBlock)(_,{key:1,style:{color:"green"},size:20})):(0,r.createCommentVNode)("",!0),e.status===z.WARMING?((0,r.openBlock)(),(0,r.createBlock)($,{key:2,style:{color:"orange"},size:20})):(0,r.createCommentVNode)("",!0),e.status===z.INFO?((0,r.openBlock)(),(0,r.createBlock)(H,{key:3,style:{color:"gray"},size:20})):(0,r.createCommentVNode)("",!0)]),(0,r.createElementVNode)("div",{style:(0,r.normalizeStyle)("max-width: 300px; color: "+j(e))},(0,r.toDisplayString)(e.statusMessage),5)])])))),128))])):(0,r.createCommentVNode)("",!0),500===t.result?((0,r.openBlock)(),(0,r.createElementBlock)("div",b,[(0,r.createVNode)(s,{color:"red"},{default:(0,r.withCtx)((()=>[(0,r.createTextVNode)((0,r.toDisplayString)(e.$t("enterprise.EnvMonitor.5mpm5p9xfo40")),1)])),_:1})])):(0,r.createCommentVNode)("",!0)])])):-1!==t.result&&200!==t.result?((0,r.openBlock)(),(0,r.createElementBlock)("div",L,(0,r.toDisplayString)(e.$t("enterprise.EnvMonitor.5mpm5p9xfrs0"))+" "+(0,r.toDisplayString)(t.errMsg),1)):((0,r.openBlock)(),(0,r.createElementBlock)("div",O,(0,r.toDisplayString)(e.$t("enterprise.EnvMonitor.5mpm5p9xfv80")),1))],512),[[r.vShow,t.isShow]])])])),_:2},1032,["loading","tip"]),(0,r.createVNode)(Z)])))),128))])])}}}),$=o(83744);const T=(0,$.Z)(A,[["__scopeId","data-v-efd21192"]]);var _=T}}]);
//# sourceMappingURL=7379.75068fb7.js.map