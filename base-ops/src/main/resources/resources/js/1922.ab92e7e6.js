"use strict";(self["webpackChunkweb_ui"]=self["webpackChunkweb_ui"]||[]).push([[1922,686],{50686:function(e,t,n){n.r(t),n.d(t,{oneCheck:function(){return l}});const l={cluster:["CheckClusterState","CheckDBParams","CheckDebugSwitch","CheckDirPermissions","CheckEnvProfile","CheckReadonlyMode","CheckDilateSysTab","CheckProStartTime","CheckMpprcFile"],db:["CheckCurConnCount","CheckCursorNum","CheckTableSpace","CheckSysadminUser","CheckHashIndex","CheckPgxcRedistb","CheckNodeGroupName","CheckTDDate"],os:["CheckEncoding","CheckFirewall","CheckKernelVer","CheckMaxHandle","CheckNTPD","CheckOSVer","CheckSysParams","CheckTHP","CheckTimeZone","CheckCPU","CheckSshdService","CheckSshdConfig","CheckCrondService","CheckStack","CheckSysPortRange","CheckMemInfo","CheckHyperThread","CheckMaxProcMemory","CheckBootItems","CheckKeyProAdj","CheckFilehandle","CheckDropCache"],device:["CheckBlockdev","CheckDiskFormat","CheckSpaceUsage","CheckInodeUsage","CheckSwapMemory","CheckLogicalBlock","CheckIOrequestqueue","CheckMaxAsyIOrequests","CheckIOConfigure"],network:["CheckMTU","CheckPing","CheckRXTX","CheckNetWorkDrop","CheckMultiQueue","CheckRouting","CheckNICModel"]}},81922:function(e,t,n){n.r(t),n.d(t,{default:function(){return W}});n(57658);var l=n(70821),o=n(39518),i=n(50686),c=n(15784),a=n(98535),s=n(57330);const r={class:"one-check-c"},m={class:"flex-between mb"},p={class:"flex-row"},d={class:"mr",style:{"white-space":"nowrap"}},h={key:1},u={class:"check-item-c",style:{height:"365px"}},k={class:"ft-lg ft-b mb"},C={class:"check-item-c",style:{height:"365px"}},g={class:"ft-lg ft-b mb"},N={class:"check-item-c",style:{height:"680px"}},V={class:"ft-lg ft-b mb"},y={class:"check-item-c",style:{height:"350px","margin-bottom":"20px"}},D={class:"ft-lg ft-b mb"},E={class:"check-item-c",style:{height:"310px"}},S={class:"ft-lg ft-b mb"},f={class:"flex-row mb"},O={class:"mr"},j={class:"mr-s"},q=(0,l.createTextVNode)(),$={class:"flex-row"},v={class:"mr-s"},w=(0,l.createTextVNode)(),x={class:"ft-lg mr-s"},b={class:"flex-row"},I={class:"mr-s"},T={class:"ft-lg",style:{color:"red"}},_={class:"flex-row"},R={class:"mr-s"},L={class:"ft-lg",style:{color:"red"}},B={class:"flex-row"},P={class:"mr-s"},M={class:"ft-lg",style:{color:"orange"}},U={class:"flex-row"},F={class:"mr-s"},G={class:"ft-lg",style:{color:"green"}};var H=(0,l.defineComponent)({setup(e,{expose:t}){const{t:n}=(0,s.QT)(),H=(0,l.reactive)({loading:!1,checkVisible:!1,checkLoading:!1,checkResult:!1,clusterId:"",isChoose:!0,clusterList:[],clusterListLoading:!1,finishTime:"",checkNum:0,errorData:[],warningData:[],passData:[],ngData:[]}),K=(0,l.computed)((()=>[{title:n("components.OneCheck.5mpiji1qpcc0"),dataIndex:"type",slotName:"type",width:100},{title:n("components.OneCheck.5mpiji1qpns0"),dataIndex:"name",tooltip:!0,width:150},{title:n("components.OneCheck.5mpiji1qpvg0"),dataIndex:"msg",ellipsis:!0,tooltip:!0}])),A=()=>new Promise((e=>{H.clusterListLoading=!0,(0,o._V)().then((t=>{200===Number(t.code)?(e(!0),t.data.forEach((e=>{e.version===a.e0.ENTERPRISE&&H.clusterList.push({label:e.clusterId,value:e.clusterId})})),H.clusterList.length&&(H.clusterId=H.clusterList[0].value)):e(!1)})).finally((()=>{H.clusterListLoading=!1}))})),W=()=>{if(H.clusterList.length||!H.isChoose){if(H.clusterId){H.loading=!0;const e={clusterId:H.clusterId};(0,o.xj)(e).then((e=>{200===Number(e.code)&&(H.warningData=e.data.result.WARNING,H.passData=e.data.result.OK,H.errorData=e.data.result.ERROR,H.ngData=e.data.result.NG,H.finishTime=z(),H.checkNum=e.data.result.WARNING.length+e.data.result.OK.length+e.data.result.ERROR.length+e.data.result.NG.length,H.checkResult=!0)})).finally((()=>{H.loading=!1}))}}else c.Z.warning("please choose cluster")},z=()=>{const e=new Date;return e.getFullYear()+"-"+e.getMonth()+"-"+e.getDate()+" "+e.getHours()+":"+e.getMinutes()+":"+e.getSeconds()},Z=e=>{const t=e.replace(".","");return i.oneCheck.cluster.includes(t)?"CLUSTER":i.oneCheck.db.includes(t)?"DB":i.oneCheck.network.includes(t)?"NETWORK":i.oneCheck.os.includes(t)?"OS":i.oneCheck.device.includes(t)?"DEVICE":void 0},Q=()=>{H.clusterId="",H.checkVisible=!1},X=e=>{e?(H.clusterId=e,H.isChoose=!1):(H.isChoose=!0,A()),H.checkVisible=!0};return t({open:X}),(e,t)=>{const n=(0,l.resolveComponent)("a-option"),o=(0,l.resolveComponent)("a-select"),i=(0,l.resolveComponent)("a-button"),c=(0,l.resolveComponent)("a-col"),a=(0,l.resolveComponent)("a-row"),s=(0,l.resolveComponent)("svg-icon"),A=(0,l.resolveComponent)("a-table"),z=(0,l.resolveComponent)("a-tab-pane"),X=(0,l.resolveComponent)("a-tabs"),Y=(0,l.resolveComponent)("a-spin"),J=(0,l.resolveComponent)("a-drawer");return(0,l.openBlock)(),(0,l.createBlock)(J,{width:800,"mask-closable":!1,footer:!1,visible:(0,l.unref)(H).checkVisible,onCancel:Q,unmountOnClose:""},{title:(0,l.withCtx)((()=>[(0,l.createTextVNode)((0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1q2ng0")),1)])),default:(0,l.withCtx)((()=>[(0,l.createElementVNode)("div",r,[(0,l.createElementVNode)("div",m,[(0,l.createElementVNode)("div",p,[(0,l.createElementVNode)("div",d,(0,l.toDisplayString)(e.$t("components.OneCheck.clusterName"))+":",1),(0,l.unref)(H).isChoose?((0,l.openBlock)(),(0,l.createBlock)(o,{key:0,style:{width:"200px"},loading:(0,l.unref)(H).clusterListLoading,modelValue:(0,l.unref)(H).clusterId,"onUpdate:modelValue":t[0]||(t[0]=e=>(0,l.unref)(H).clusterId=e),placeholder:e.$t("components.OneCheck.5mpiji1q3wo0")},{default:(0,l.withCtx)((()=>[((0,l.openBlock)(!0),(0,l.createElementBlock)(l.Fragment,null,(0,l.renderList)((0,l.unref)(H).clusterList,((e,t)=>((0,l.openBlock)(),(0,l.createBlock)(n,{key:t,label:e.label,value:e.value},null,8,["label","value"])))),128))])),_:1},8,["loading","modelValue","placeholder"])):((0,l.openBlock)(),(0,l.createElementBlock)("div",h,(0,l.toDisplayString)((0,l.unref)(H).clusterId),1))]),(0,l.createVNode)(i,{type:"primary",loading:(0,l.unref)(H).checkLoading,disabled:(0,l.unref)(H).clusterListLoading,onClick:W},{default:(0,l.withCtx)((()=>[(0,l.createTextVNode)((0,l.toDisplayString)((0,l.unref)(H).checkResult?e.$t("components.OneCheck.5mpiji1q4a00"):e.$t("components.OneCheck.5mpiji1q4k40")),1)])),_:1},8,["loading","disabled"])]),(0,l.createVNode)(Y,{class:"full-w",loading:(0,l.unref)(H).loading,tip:e.$t("components.OneCheck.5mpiji1q4tg0")},{default:(0,l.withCtx)((()=>[(0,l.withDirectives)((0,l.createElementVNode)("div",null,[(0,l.createVNode)(a,{gutter:24,class:"mb"},{default:(0,l.withCtx)((()=>[(0,l.createVNode)(c,{span:12},{default:(0,l.withCtx)((()=>[(0,l.createElementVNode)("div",u,[(0,l.createElementVNode)("div",k,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1q57w0")),1),(0,l.createElementVNode)("ul",null,[(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1q5r40")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1q5zw0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1q6b00")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1q8q80")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1q96c0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1q9r00")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qa080")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qa940")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qahc0")),1)])])])),_:1}),(0,l.createVNode)(c,{span:12},{default:(0,l.withCtx)((()=>[(0,l.createElementVNode)("div",C,[(0,l.createElementVNode)("div",g,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qas40")),1),(0,l.createElementVNode)("ul",null,[(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qbv80")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qc780")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qcjs0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qczo0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qdeo0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.else1")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qdlk0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.else2")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.else3")),1)])])])),_:1})])),_:1}),(0,l.createVNode)(a,{gutter:24,class:"mb"},{default:(0,l.withCtx)((()=>[(0,l.createVNode)(c,{span:12},{default:(0,l.withCtx)((()=>[(0,l.createElementVNode)("div",N,[(0,l.createElementVNode)("div",V,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qds80")),1),(0,l.createElementVNode)("ul",null,[(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qdy40")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qe8s0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qehc0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qeoo0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qf280")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qf9k0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qfgo0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qfnc0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qfx00")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qg400")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qga80")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qgl40")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qgt80")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qh2k0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qh8s0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qhfs0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qhn00")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qhto0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qi2s0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qif40")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qioc0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qizk0")),1)])])])),_:1}),(0,l.createVNode)(c,{span:12},{default:(0,l.withCtx)((()=>[(0,l.createElementVNode)("div",y,[(0,l.createElementVNode)("div",D,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qjag0")),1),(0,l.createElementVNode)("ul",null,[(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qjh80")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qkn00")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qkyk0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1ql580")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qldc0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qlmc0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qltc0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qm0s0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qmbw0")),1)])]),(0,l.createElementVNode)("div",E,[(0,l.createElementVNode)("div",S,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qmj40")),1),(0,l.createElementVNode)("ul",null,[(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qmqg0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qn1c0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qn8g0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qnf00")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qnks0")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qnr00")),1),(0,l.createElementVNode)("li",null,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qnxs0")),1)])])])),_:1})])),_:1})],512),[[l.vShow,!(0,l.unref)(H).checkResult]]),(0,l.withDirectives)((0,l.createElementVNode)("div",null,[(0,l.createElementVNode)("div",f,[(0,l.createElementVNode)("div",O,[(0,l.createElementVNode)("span",j,(0,l.toDisplayString)(e.$t("components.OneCheck.else4"))+":",1),q,(0,l.createElementVNode)("span",null,(0,l.toDisplayString)((0,l.unref)(H).finishTime?(0,l.unref)(H).finishTime:"--"),1)]),(0,l.createElementVNode)("div",$,[(0,l.createElementVNode)("span",v,(0,l.toDisplayString)(e.$t("components.OneCheck.else5"))+":",1),w,(0,l.createElementVNode)("span",x,(0,l.toDisplayString)((0,l.unref)(H).checkNum),1),(0,l.createTextVNode)((0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qo440")),1)])]),(0,l.createVNode)(X,null,{default:(0,l.withCtx)((()=>[(0,l.createVNode)(z,{key:"ng"},{title:(0,l.withCtx)((()=>[(0,l.createElementVNode)("div",b,[(0,l.createVNode)(s,{"icon-class":"ops-error",class:"icon-s mr-s"}),(0,l.createElementVNode)("div",I,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qoe00")),1),(0,l.createElementVNode)("div",T,(0,l.toDisplayString)((0,l.unref)(H).ngData.length),1)])])),default:(0,l.withCtx)((()=>[(0,l.createVNode)(A,{data:(0,l.unref)(H).ngData,columns:(0,l.unref)(K)},{type:(0,l.withCtx)((({record:e})=>[(0,l.createTextVNode)((0,l.toDisplayString)(Z(e.name)),1)])),_:1},8,["data","columns"])])),_:1}),(0,l.createVNode)(z,{key:"error"},{title:(0,l.withCtx)((()=>[(0,l.createElementVNode)("div",_,[(0,l.createVNode)(s,{"icon-class":"ops-error",class:"icon-s mr-s"}),(0,l.createElementVNode)("div",R,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qom40")),1),(0,l.createElementVNode)("div",L,(0,l.toDisplayString)((0,l.unref)(H).errorData.length),1)])])),default:(0,l.withCtx)((()=>[(0,l.createVNode)(A,{data:(0,l.unref)(H).errorData,columns:(0,l.unref)(K)},{type:(0,l.withCtx)((({record:e})=>[(0,l.createTextVNode)((0,l.toDisplayString)(Z(e.name)),1)])),_:1},8,["data","columns"])])),_:1}),(0,l.createVNode)(z,{key:"warning"},{title:(0,l.withCtx)((()=>[(0,l.createElementVNode)("div",B,[(0,l.createVNode)(s,{"icon-class":"ops-warning",class:"icon-s mr-s"}),(0,l.createElementVNode)("div",P,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qosc0")),1),(0,l.createElementVNode)("div",M,(0,l.toDisplayString)((0,l.unref)(H).warningData.length),1)])])),default:(0,l.withCtx)((()=>[(0,l.createVNode)(A,{data:(0,l.unref)(H).warningData,columns:(0,l.unref)(K)},{type:(0,l.withCtx)((({record:e})=>[(0,l.createTextVNode)((0,l.toDisplayString)(Z(e.name)),1)])),_:1},8,["data","columns"])])),_:1}),(0,l.createVNode)(z,{key:"pass"},{title:(0,l.withCtx)((()=>[(0,l.createElementVNode)("div",U,[(0,l.createVNode)(s,{"icon-class":"ops-pass",class:"icon-s mr-s"}),(0,l.createElementVNode)("div",F,(0,l.toDisplayString)(e.$t("components.OneCheck.5mpiji1qoyw0")),1),(0,l.createElementVNode)("div",G,(0,l.toDisplayString)((0,l.unref)(H).passData.length),1)])])),default:(0,l.withCtx)((()=>[(0,l.createVNode)(A,{data:(0,l.unref)(H).passData,columns:(0,l.unref)(K)},{type:(0,l.withCtx)((({record:e})=>[(0,l.createTextVNode)((0,l.toDisplayString)(Z(e.name)),1)])),_:1},8,["data","columns"])])),_:1})])),_:1})],512),[[l.vShow,(0,l.unref)(H).checkResult]])])),_:1},8,["loading","tip"])])])),_:1},8,["visible"])}}}),K=n(83744);const A=(0,K.Z)(H,[["__scopeId","data-v-1d9c358c"]]);var W=A}}]);
//# sourceMappingURL=1922.ab92e7e6.js.map