"use strict";(self["webpackChunkweb_ui"]=self["webpackChunkweb_ui"]||[]).push([[3238],{39518:function(e,t,r){r.d(t,{$4:function(){return I},Ac:function(){return s},BL:function(){return w},C4:function(){return E},DF:function(){return V},GD:function(){return B},IW:function(){return S},K4:function(){return T},Kf:function(){return U},LR:function(){return o},Lf:function(){return n},Ox:function(){return N},PK:function(){return i},R1:function(){return $},Sv:function(){return G},T2:function(){return f},T4:function(){return z},TL:function(){return v},TY:function(){return R},Uw:function(){return W},We:function(){return F},YQ:function(){return C},Ye:function(){return g},ZA:function(){return P},_V:function(){return m},c0:function(){return u},gi:function(){return y},h:function(){return L},jQ:function(){return q},jV:function(){return A},nT:function(){return h},sT:function(){return b},tA:function(){return d},ur:function(){return k},w3:function(){return p},xj:function(){return c},y6:function(){return x},zs:function(){return _},zx:function(){return D}});var l=r(9669),a=r.n(l);const n=()=>a().get("encryption/getKey"),o=e=>a().post("opsCluster/download",e||{}),u=e=>a().get(`opsCluster/env/${e}`),s=e=>a().post("opsCluster/install",e),d=e=>a().post("opsCluster/import",e),i=e=>a().post("opsCluster/quickInstall",e),c=e=>a().get("opsCluster/check",{params:e}),p=e=>a().post("opsCluster/uninstall",e),f=e=>a().get("opsCluster/hasName",{params:e}),m=()=>a().get("opsCluster/listCluster"),g=e=>a().post("opsCluster/ssh",e),h=e=>a().get("opsCluster/monitor",{params:e}),w=e=>a().post("opsCluster/start",e),b=e=>a().post("opsCluster/stop",e),k=e=>a().post("opsCluster/restart",e),v=e=>a().get("opsCluster/listHost",{params:e}),L=e=>a().get(e?`opsCluster/listInstallPackage?path=${e}`:"opsCluster/listInstallPackage"),D=()=>a().get("host/listAll"),C=e=>a().get(`hostUser/listAllWithoutRoot/${e}`),I=()=>a().get("az/listAll"),V=e=>a().get("opsCluster/logPath",{params:e}),y=e=>a().get("opsCluster/ls",{params:e}),W=e=>a().get("opsCluster/slowSql",{params:e}),B=e=>a().get("opsCluster/auditLog",{params:e}),G=e=>a().get("opsCluster/download",{params:e}),x=e=>a().get("wdr/list",{params:e}),$=e=>a().post("wdr/generate",e),_=e=>a()["delete"]("wdr/del/"+e),N=e=>a().get("wdr/createSnapshot",{params:e}),T=e=>a().get("wdr/downloadWdr",{params:e}),E=e=>a().get("wdr/listSnapshot",{params:e}),S=e=>a().post("backup/backup",e),z=e=>a().get("backup/page",{params:e}),U=(e,t)=>a().post("backup/recover/"+e,t),A=e=>a()["delete"]("backup/del/"+e),F=e=>a().get("installPackageManager/page",{params:e}),R=e=>a()["delete"]("installPackageManager/"+e),P=e=>a().post("installPackageManager/save",e),q=(e,t)=>a().put(`installPackageManager/update/${e}`,t)},33238:function(e,t,r){r.r(t),r.d(t,{default:function(){return d}});r(57658);var l=r(70821),a=r(39518),n=r(15784),o=r(57330),u=(0,l.defineComponent)({emits:["finish"],setup(e,{expose:t,emit:r}){const{t:u}=(0,o.QT)(),s=(0,l.reactive)({show:!1,title:u("wdr.GenerateWdrDlg.5mpm0eufzrs0"),loading:!1,formData:{clusterId:"",hostId:"",scope:"CLUSTER",type:"DETAIL",startId:"",endId:""},clusterListLoading:!1,clusterList:[],hostListLoading:!1,hostList:[],wdrScopeList:[],getSnapshotLoading:!1,snapshotList:[],wdrTypeList:[],rules:{clusterId:[{required:!0,"validate-trigger":"blur",message:u("wdr.GenerateWdrDlg.5mpm0eufy340")}]}}),d=()=>{s.show=!1,(0,l.nextTick)((()=>{i.value?.clearValidate(),i.value?.resetFields(),s.clusterList=[]}))},i=(0,l.ref)(null),c=()=>{i.value?.validate().then((e=>{e||(s.loading=!0,(0,a.R1)(s.formData).then((e=>{200===Number(e.code)?(r("finish"),d()):n.Z.error("Error generating WDR report, cause of error: "+e.msg)})).catch((()=>{n.Z.error("Error generating WDR report")})).finally((()=>{s.loading=!1})))}))},p=()=>new Promise((e=>{s.clusterListLoading=!0,(0,a._V)().then((t=>{200===Number(t.code)?(e(!0),t.data.forEach((e=>{s.clusterList.push({label:e.clusterId,value:e.clusterId})})),s.formData.clusterId=s.clusterList[0].value,f()):e(!1)})).finally((()=>{s.clusterListLoading=!1}))})),f=()=>{if(s.formData.clusterId){const e={clusterId:s.formData.clusterId};s.hostListLoading=!0,(0,a.TL)(e).then((e=>{200===Number(e.code)?(s.hostList=[],e.data.forEach((e=>{s.hostList.push({label:`${e.privateIp}(${e.hostname})`,value:e.hostId})})),s.formData.hostId=s.hostList[0].value,m()):n.Z.error("Failed to obtain host user information")})).finally((()=>{s.hostListLoading=!1}))}},m=()=>{if(s.formData.clusterId&&s.formData.hostId){const e={clusterId:s.formData.clusterId,hostId:s.formData.hostId};s.getSnapshotLoading=!0,(0,a.C4)(e).then((e=>{200===Number(e.code)?(s.snapshotList=[],e.data.forEach((e=>{s.snapshotList.push({label:e.snapshot_id,value:e.snapshot_id})})),s.formData.startId=s.snapshotList[0].value,s.formData.endId=s.snapshotList[s.snapshotList.length-1].value):n.Z.error("Failed to obtain host information")})).finally((()=>{s.getSnapshotLoading=!1}))}},g=()=>{s.show=!0,s.title=u("wdr.GenerateWdrDlg.5mpm0eufzrs0"),s.wdrScopeList=[{label:u("wdr.GenerateWdrDlg.5mpm0eufzv40"),value:"CLUSTER"},{label:u("wdr.GenerateWdrDlg.5mpm0eufzyg0"),value:"NODE"}],s.wdrTypeList=[{label:u("wdr.GenerateWdrDlg.5mpm0eug0300"),value:"DETAIL"},{label:u("wdr.GenerateWdrDlg.5mpm0eug0880"),value:"SUMMARY"},{label:u("wdr.GenerateWdrDlg.5mpm0eug0bc0"),value:"ALL"}],p()};return t({open:g}),(e,t)=>{const r=(0,l.resolveComponent)("a-option"),a=(0,l.resolveComponent)("a-select"),n=(0,l.resolveComponent)("a-form-item"),o=(0,l.resolveComponent)("a-form"),p=(0,l.resolveComponent)("a-modal");return(0,l.openBlock)(),(0,l.createBlock)(p,{"mask-closable":!1,"esc-to-close":!1,visible:(0,l.unref)(s).show,title:(0,l.unref)(s).title,"ok-loading":(0,l.unref)(s).loading,"modal-style":{width:"450px"},onOk:c,onCancel:d,okText:e.$t("wdr.GenerateWdrDlg.5mpm0eufx3g0")},{default:(0,l.withCtx)((()=>[(0,l.createVNode)(o,{model:(0,l.unref)(s).formData,ref:(e,t)=>{t["formRef"]=e,i.value=e},"label-col":{style:{width:"100px"}},rules:(0,l.unref)(s).rules},{default:(0,l.withCtx)((()=>[(0,l.createVNode)(n,{field:"clusterId",label:e.$t("wdr.GenerateWdrDlg.5mpm0eufxww0"),"validate-trigger":"change",rules:[{required:!0,message:(0,l.unref)(u)("wdr.GenerateWdrDlg.5mpm0eufy340")}]},{default:(0,l.withCtx)((()=>[(0,l.createVNode)(a,{modelValue:(0,l.unref)(s).formData.clusterId,"onUpdate:modelValue":t[0]||(t[0]=e=>(0,l.unref)(s).formData.clusterId=e),placeholder:e.$t("wdr.GenerateWdrDlg.5mpm0eufy340"),onChange:f},{default:(0,l.withCtx)((()=>[((0,l.openBlock)(!0),(0,l.createElementBlock)(l.Fragment,null,(0,l.renderList)((0,l.unref)(s).clusterList,((e,t)=>((0,l.openBlock)(),(0,l.createBlock)(r,{key:t,value:e.value,label:e.label},null,8,["value","label"])))),128))])),_:1},8,["modelValue","placeholder"])])),_:1},8,["label","rules"]),(0,l.createVNode)(n,{field:"hostId",label:e.$t("wdr.GenerateWdrDlg.5mpm0eufy7k0"),"validate-trigger":"change",rules:[{required:!0,message:(0,l.unref)(u)("wdr.GenerateWdrDlg.5mpm0eufyb40")}]},{default:(0,l.withCtx)((()=>[(0,l.createVNode)(a,{modelValue:(0,l.unref)(s).formData.hostId,"onUpdate:modelValue":t[1]||(t[1]=e=>(0,l.unref)(s).formData.hostId=e),placeholder:e.$t("wdr.GenerateWdrDlg.5mpm0eufyb40"),onChange:m},{default:(0,l.withCtx)((()=>[((0,l.openBlock)(!0),(0,l.createElementBlock)(l.Fragment,null,(0,l.renderList)((0,l.unref)(s).hostList,((e,t)=>((0,l.openBlock)(),(0,l.createBlock)(r,{key:t,value:e.value,label:e.label},null,8,["value","label"])))),128))])),_:1},8,["modelValue","placeholder"])])),_:1},8,["label","rules"]),(0,l.createVNode)(n,{field:"scope",label:e.$t("wdr.GenerateWdrDlg.5mpm0eufyew0"),"validate-trigger":"change"},{default:(0,l.withCtx)((()=>[(0,l.createVNode)(a,{modelValue:(0,l.unref)(s).formData.scope,"onUpdate:modelValue":t[2]||(t[2]=e=>(0,l.unref)(s).formData.scope=e),placeholder:e.$t("wdr.GenerateWdrDlg.5mpm0eufyig0")},{default:(0,l.withCtx)((()=>[((0,l.openBlock)(!0),(0,l.createElementBlock)(l.Fragment,null,(0,l.renderList)((0,l.unref)(s).wdrScopeList,((e,t)=>((0,l.openBlock)(),(0,l.createBlock)(r,{key:t,value:e.value,label:e.label},null,8,["value","label"])))),128))])),_:1},8,["modelValue","placeholder"])])),_:1},8,["label"]),(0,l.createVNode)(n,{field:"type",label:e.$t("wdr.GenerateWdrDlg.5mpm0eufyo80"),"validate-trigger":"change"},{default:(0,l.withCtx)((()=>[(0,l.createVNode)(a,{modelValue:(0,l.unref)(s).formData.type,"onUpdate:modelValue":t[3]||(t[3]=e=>(0,l.unref)(s).formData.type=e),placeholder:e.$t("wdr.GenerateWdrDlg.5mpm0eufyz00")},{default:(0,l.withCtx)((()=>[((0,l.openBlock)(!0),(0,l.createElementBlock)(l.Fragment,null,(0,l.renderList)((0,l.unref)(s).wdrTypeList,((e,t)=>((0,l.openBlock)(),(0,l.createBlock)(r,{key:t,value:e.value,label:e.label},null,8,["value","label"])))),128))])),_:1},8,["modelValue","placeholder"])])),_:1},8,["label"]),(0,l.createVNode)(n,{field:"startId",label:e.$t("wdr.GenerateWdrDlg.5mpm0eufzbk0"),"validate-trigger":"change",rules:[{required:!0,message:(0,l.unref)(u)("wdr.GenerateWdrDlg.5mpm0eufzg00")}]},{default:(0,l.withCtx)((()=>[(0,l.createVNode)(a,{loading:(0,l.unref)(s).getSnapshotLoading,modelValue:(0,l.unref)(s).formData.startId,"onUpdate:modelValue":t[4]||(t[4]=e=>(0,l.unref)(s).formData.startId=e),placeholder:e.$t("wdr.GenerateWdrDlg.5mpm0eufzg00")},{default:(0,l.withCtx)((()=>[((0,l.openBlock)(!0),(0,l.createElementBlock)(l.Fragment,null,(0,l.renderList)((0,l.unref)(s).snapshotList,((e,t)=>((0,l.openBlock)(),(0,l.createBlock)(r,{key:t,value:e.value,label:e.label},null,8,["value","label"])))),128))])),_:1},8,["loading","modelValue","placeholder"])])),_:1},8,["label","rules"]),(0,l.createVNode)(n,{field:"endId",label:e.$t("wdr.GenerateWdrDlg.5mpm0eufzk80"),"validate-trigger":"change",rules:[{required:!0,message:(0,l.unref)(u)("wdr.GenerateWdrDlg.5mpm0eufzo40")}]},{default:(0,l.withCtx)((()=>[(0,l.createVNode)(a,{loading:(0,l.unref)(s).getSnapshotLoading,modelValue:(0,l.unref)(s).formData.endId,"onUpdate:modelValue":t[5]||(t[5]=e=>(0,l.unref)(s).formData.endId=e),placeholder:e.$t("wdr.GenerateWdrDlg.5mpm0eufzo40")},{default:(0,l.withCtx)((()=>[((0,l.openBlock)(!0),(0,l.createElementBlock)(l.Fragment,null,(0,l.renderList)((0,l.unref)(s).snapshotList,((e,t)=>((0,l.openBlock)(),(0,l.createBlock)(r,{key:t,value:e.value,label:e.label},null,8,["value","label"])))),128))])),_:1},8,["loading","modelValue","placeholder"])])),_:1},8,["label","rules"])])),_:1},8,["model","rules"])])),_:1},8,["visible","title","ok-loading","okText"])}}});const s=u;var d=s}}]);
//# sourceMappingURL=3238.23e44bd4.js.map