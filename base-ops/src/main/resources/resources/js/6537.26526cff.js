(self["webpackChunkweb_ui"]=self["webpackChunkweb_ui"]||[]).push([[6537],{33781:function(e,t,o){"use strict";o.d(t,{Z:function(){return r}});var s=o(47327);class n{constructor(){(0,s.Z)(this,"heartTimeOut",void 0),(0,s.Z)(this,"ServerHeartTimeOut",void 0),(0,s.Z)(this,"timeout",5e3)}reset(){clearTimeout(this.heartTimeOut),clearTimeout(this.ServerHeartTimeOut)}start(e){this.heartTimeOut=setTimeout((t=>{e(t),this.ServerHeartTimeOut=setTimeout((t=>{e(t),this.reset(),this.start(e)}),this.timeout)}),this.timeout)}}class r extends n{constructor(e){super(),(0,s.Z)(this,"ws",void 0),(0,s.Z)(this,"reconnectTimer",0),(0,s.Z)(this,"reconnectCount",10),(0,s.Z)(this,"options",{url:null,heartTime:5e3,heartMsg:"ping",isReconnect:!0,isRestory:!1,reconnectTime:5e3,reconnectCount:5,openCb:e=>{console.log("connect success callback::::",e)},closeCb:e=>{console.log("close callback::::",e)},messageCb:e=>{console.log("message callback::::",e)},errorCb:e=>{console.log("error callback::::",e)}}),Object.assign(this.options,e),this.create()}create(){if(!("WebSocket"in window))throw new Error("The current browser does not support it and cannot be used");if(!this.options.url)throw new Error("The address does not exist, and the channel cannot be established");let e;console.log("get locaion host: ",window.location.host),e=`ws://${window.location.host}/ws/base-ops/${this.options.url}`,this.ws=new WebSocket(e),this.onopen(this.options.openCb),this.onclose(this.options.closeCb),this.onmessage(this.options.messageCb)}onopen(e){this.ws.onopen=t=>{"function"===typeof e?e(t):"function"===typeof this.options.openCb&&this.options.openCb(t)}}onclose(e){this.ws.onclose=t=>{"function"===typeof e?e(t):"function"===typeof this.options.closeCb&&this.options.closeCb(t)}}onerror(e){this.ws.onerror=t=>{"function"===typeof e?e(t):"function"===typeof this.options.errorCb&&this.options.errorCb(t)}}onmessage(e){this.ws.onmessage=t=>{const o=t.data;"function"===typeof e&&e(o)}}send(e){console.log("doNothing",e)}destroy(){super.reset(),clearTimeout(this.reconnectTimer),this.options.isRestory=!0,this.ws.close()}}},26537:function(e,t,o){"use strict";o.r(t),o.d(t,{default:function(){return Z}});o(57658);var s=o(70821),n=o(39518),r=o(15784),i=o(98535),l=o(33781),a=o(12320),c=o(3334),d=o(12617),u=o(79134),p=o(57330);const m=e=>((0,s.pushScopeId)("data-v-25fd8dbe"),e=e(),(0,s.popScopeId)(),e),f={class:"simple-install-c"},h={key:0,class:"install-content"},g={class:"ft-b ft-m mb"},v={key:1,class:"install-panel"},b={key:0,class:"flex-col full-h"},y={class:"install-doing mb"},w={class:"ft-m ft-b mb-lg"},I={class:"flex-col-start full-w"},x={class:"progress-c mb"},_={class:"flex-row full-w full-h"},N={class:"flex-col-start mr",style:{width:"50%"}},E=m((()=>(0,s.createElementVNode)("div",{id:"xtermLog",class:"xterm"},null,-1))),C={key:0,class:"flex-col-start",style:{width:"50%"}},V={class:"flex-between full-w mb"},S={class:"flex-row"},k={class:"mr-s"},T=m((()=>(0,s.createElementVNode)("div",{id:"xterm",class:"xterm"},null,-1))),P={key:1,class:"flex-col full-w full-h"},D={class:"mb-lg"},O={class:"install-connect-c flex-col mb-xlg"},L={class:"ft-b mb"},$={class:"mb"},A=m((()=>(0,s.createElementVNode)("span",{class:"content"},"gaussdb",-1))),B=m((()=>(0,s.createElementVNode)("span",{class:"content"},"1qaz2wsx#EDC",-1))),M={class:"flex-row"};var j=(0,s.defineComponent)({setup(e){const{t:t}=(0,p.QT)(),o=(0,s.reactive)({state:-1,form:{hostId:"",rootPassword:"",rootPasswordEncrypt:""},installProgress:0,privateIp:"",installUserId:"",installUsername:"",hostLoading:!1,hostObj:{},hostList:[],rules:{hostId:[{required:!0,"validate-trigger":"change",message:t("simpleInstall.index.5mpn813gwbk0")}],rootPassword:[{required:!0,"validate-trigger":"blur",message:t("simpleInstall.index.5mpn813gupc0")}]}}),m=(0,s.ref)(),j=(0,s.ref)(),U=(0,s.ref)(),F=(0,s.ref)();(0,s.onMounted)((()=>{Y()})),(0,s.onBeforeUnmount)((()=>{m.value?.destroy(),j.value?.destroy(),U.value?.dispose(),F.value?.dispose()}));const Z=(0,s.ref)(),R=async()=>{const e=await(Z.value?.validate());if(!e){const e=await(0,u.v)(o.form.rootPassword);o.form.rootPasswordEncrypt=e,o.state=0,H()}},z=()=>{j.value&&j.value?.destroy(),U.value&&U.value.dispose(),H()},H=()=>{const e=W();let t=!1;const s=(new Date).getTime(),n=new l.Z({url:`simple_installLog_${s}`});m.value=n,n.onopen((()=>{G(n,`simple_installLog_${s}`),X(e,n.ws)})),n.onmessage((s=>{if("START"===s&&(t=!0),"DOWNLOAD_FINISH"===s&&(t=!1),t){const t=Number(s);isNaN(t)?e.writeln(s):(e.write("[2K\r"),1===t?e.writeln((100*t).toFixed(0)+"%"):e.write((100*t).toFixed(0)+"%"))}else e.writeln(s);if(Q(s),s.indexOf("FINAL_EXECUTE_EXIT_CODE")>-1){o.installProgress=1;const e=Number(s.split(":")[1]);0===e?o.state=1:(o.state=2,F.value&&F.value.dispose(),q()),n.destroy()}}))},q=()=>{const e=W(),t=(new Date).getTime(),s=new l.Z({url:`simple_terminal_${t}`});j.value=s,s.onopen((()=>{const r={hostId:o.form.hostId,rootPassword:o.form.rootPasswordEncrypt,wsConnectType:i.Ie.SSH,businessId:`simple_terminal_${t}`};(0,n.Ye)(r),K(e,s.ws)}))},G=async(e,t)=>{const s={installContext:{openGaussVersion:i.e0.MINIMAL_LIST,openGaussVersionNum:"3.0.0",installMode:i.k8.OFF_LINE,installPackagePath:"/ops/files",deployType:i.K0.SINGLE_NODE,clusterId:"MINI_"+(new Date).getTime(),clusterName:"",minimalistInstallConfig:{port:Number(5432),databaseUsername:"",databasePassword:"1qaz2wsx#EDC",nodeConfigList:[{clusterRole:i._.MASTER,hostId:o.form.hostId,rootPassword:o.form.rootPasswordEncrypt,installUserId:o.installUserId,installPath:"/opt/openGauss",dataPath:"/opt/openGauss/data",isInstallDemoDatabase:!0}]}},wsConnectType:i.Fl.COMMAND_EXEC,businessId:t};(0,n.PK)(s).then((t=>{200!==Number(t.code)&&e.destroy()})).catch((()=>{e.destroy()}))},W=()=>new a.Terminal({fontSize:14,rows:40,cols:100,cursorBlink:!0,convertEol:!0,disableStdin:!1,cursorStyle:"underline",theme:{background:"black"}}),X=(e,t)=>{if(t){const t=new d.FitAddon;e.loadAddon(t),e.open(document.getElementById("xtermLog")),t.fit(),e.clear(),e.focus(),U.value=e}},K=(e,t)=>{if(t){const o=new c.AttachAddon(t),s=new d.FitAddon;e.loadAddon(o),e.loadAddon(s),e.open(document.getElementById("xterm")),s.fit(),e.clear(),e.focus(),e.write("\r\n[33m$[0m "),F.value=e}},Q=e=>{"START"===e?o.installProgress=.05:"BEFORE INSTALL"===e?o.installProgress=.15:"INSTALL"===e?o.installProgress=.3:"SAVE CONTEXT"===e?o.installProgress=.6:"CREATE REMOTE USER"===e&&(o.installProgress=.8)},Y=()=>{o.hostLoading=!0,(0,n.zx)().then((e=>{200===Number(e.code)?(o.hostList=[],o.hostList=e.data,e.data.forEach((e=>{o.hostObj[e.hostId]=e})),o.form.hostId=o.hostList[0].hostId,o.privateIp=o.hostList[0].privateIp,J()):r.Z.error("Failed to obtain the host list data")})).finally((()=>{o.hostLoading=!1}))},J=()=>{o.form.hostId&&(o.hostObj[o.form.hostId]&&(o.privateIp=o.hostObj[o.form.hostId].privateIp),(0,n.YQ)(o.form.hostId).then((e=>{200===Number(e.code)?e.data.length?(o.installUserId=e.data[0].hostUserId,o.installUsername=e.data[0].username):(o.installUserId="",o.installUsername=""):r.Z.error("Failed to obtain user data from the host")})))},ee=()=>{window.$wujie?.props.methods.jump({name:"Dashboard"})};return(e,n)=>{const r=(0,s.resolveComponent)("a-divider"),i=(0,s.resolveComponent)("svg-icon"),l=(0,s.resolveComponent)("a-option"),a=(0,s.resolveComponent)("a-select"),c=(0,s.resolveComponent)("a-form-item"),d=(0,s.resolveComponent)("a-input-password"),u=(0,s.resolveComponent)("a-form"),p=(0,s.resolveComponent)("a-button"),m=(0,s.resolveComponent)("a-progress"),j=(0,s.resolveComponent)("a-alert");return(0,s.openBlock)(),(0,s.createElementBlock)("div",f,[(0,s.createElementVNode)("div",null,(0,s.toDisplayString)(e.$t("simpleInstall.index.5mpn813gtvs0")),1),(0,s.createVNode)(r,{class:"mb-lg"}),-1===(0,s.unref)(o).state?((0,s.openBlock)(),(0,s.createElementBlock)("div",h,[(0,s.createVNode)(i,{"icon-class":"ops-mini-version",class:"icon-size mb"}),(0,s.createElementVNode)("div",g,"openGauss "+(0,s.toDisplayString)(e.$t("simpleInstall.index.else1")),1),(0,s.createVNode)(u,{class:"mb",model:(0,s.unref)(o).form,rules:(0,s.unref)(o).rules,style:{width:"400px"},ref:(e,t)=>{t["formRef"]=e,Z.value=e},"auto-label-width":""},{default:(0,s.withCtx)((()=>[(0,s.createVNode)(c,{field:"hostId",label:e.$t("simpleInstall.index.5mpn813guf00")},{default:(0,s.withCtx)((()=>[(0,s.createVNode)(a,{loading:(0,s.unref)(o).hostLoading,modelValue:(0,s.unref)(o).form.hostId,"onUpdate:modelValue":n[0]||(n[0]=e=>(0,s.unref)(o).form.hostId=e),placeholder:e.$t("simpleInstall.index.5mpn813gukw0"),onChange:J},{default:(0,s.withCtx)((()=>[((0,s.openBlock)(!0),(0,s.createElementBlock)(s.Fragment,null,(0,s.renderList)((0,s.unref)(o).hostList,(e=>((0,s.openBlock)(),(0,s.createBlock)(l,{key:e.hostId,value:e.hostId},{default:(0,s.withCtx)((()=>[(0,s.createTextVNode)((0,s.toDisplayString)(e.privateIp+"("+(e.publicIp?e.publicIp:"--")+")"),1)])),_:2},1032,["value"])))),128))])),_:1},8,["loading","modelValue","placeholder"])])),_:1},8,["label"]),(0,s.createVNode)(c,{field:"rootPassword",label:e.$t("simpleInstall.index.else2"),"validate-trigger":"blur",rules:[{required:!0,message:(0,s.unref)(t)("simpleInstall.index.5mpn813gupc0")}]},{default:(0,s.withCtx)((()=>[(0,s.createVNode)(d,{modelValue:(0,s.unref)(o).form.rootPassword,"onUpdate:modelValue":n[1]||(n[1]=e=>(0,s.unref)(o).form.rootPassword=e),placeholder:e.$t("simpleInstall.index.5mpn813gupc0"),"allow-clear":""},null,8,["modelValue","placeholder"])])),_:1},8,["label","rules"])])),_:1},8,["model","rules"]),(0,s.createVNode)(p,{type:"primary",size:"large",onClick:R},{default:(0,s.withCtx)((()=>[(0,s.createTextVNode)((0,s.toDisplayString)(e.$t("simpleInstall.index.5mpn813gut00")),1)])),_:1})])):(0,s.createCommentVNode)("",!0),-1!==(0,s.unref)(o).state?((0,s.openBlock)(),(0,s.createElementBlock)("div",v,[1!==(0,s.unref)(o).state?((0,s.openBlock)(),(0,s.createElementBlock)("div",b,[(0,s.createElementVNode)("div",y,[(0,s.createElementVNode)("div",w,(0,s.toDisplayString)(e.$t("simpleInstall.index.5mpn813guxc0")),1),(0,s.createElementVNode)("div",I,[(0,s.createElementVNode)("div",x,[(0,s.createVNode)(m,{color:1===(0,s.unref)(o).state?"green":"red","stroke-width":12,percent:(0,s.unref)(o).installProgress},null,8,["color","percent"])]),(0,s.createElementVNode)("div",null,(0,s.toDisplayString)(e.$t("simpleInstall.index.5mpn813gv880"))+" "+(0,s.toDisplayString)(e.$t("simpleInstall.index.5mpn813gvc40"))+" "+(0,s.toDisplayString)((0,s.unref)(o).privateIp),1)])]),(0,s.createElementVNode)("div",_,[(0,s.createElementVNode)("div",N,[2===(0,s.unref)(o).state?((0,s.openBlock)(),(0,s.createBlock)(j,{key:0,class:"mb-s",style:{padding:"10px 12px",width:"fit-content"},type:"error"},{default:(0,s.withCtx)((()=>[(0,s.createTextVNode)((0,s.toDisplayString)(e.$t("simpleInstall.index.5mpn813gvfw0")),1)])),_:1})):(0,s.createCommentVNode)("",!0),E]),2===(0,s.unref)(o).state?((0,s.openBlock)(),(0,s.createElementBlock)("div",C,[(0,s.createElementVNode)("div",V,[(0,s.createElementVNode)("div",S,[(0,s.createElementVNode)("div",k,(0,s.toDisplayString)(e.$t("simpleInstall.index.5mpn813gvjk0")),1),(0,s.createElementVNode)("div",null,(0,s.toDisplayString)((0,s.unref)(o).privateIp),1)]),(0,s.createVNode)(p,{type:"primary",onClick:z},{default:(0,s.withCtx)((()=>[(0,s.createTextVNode)((0,s.toDisplayString)(e.$t("simpleInstall.index.5mpn813gvmw0")),1)])),_:1})]),T])):(0,s.createCommentVNode)("",!0)])])):((0,s.openBlock)(),(0,s.createElementBlock)("div",P,[(0,s.createVNode)(i,{class:"succ-icon-size mb","icon-class":"ops-install-success"}),(0,s.createElementVNode)("div",D,(0,s.toDisplayString)(e.$t("simpleInstall.index.5mpn813gvqo0")),1),(0,s.createElementVNode)("div",O,[(0,s.createElementVNode)("div",L,(0,s.toDisplayString)(e.$t("simpleInstall.index.5mpn813gvu40")),1),(0,s.createElementVNode)("div",$,[(0,s.createTextVNode)((0,s.toDisplayString)(e.$t("simpleInstall.index.5mpn813gvxg0"))+" ",1),A]),(0,s.createElementVNode)("div",null,[(0,s.createTextVNode)((0,s.toDisplayString)(e.$t("simpleInstall.index.5mpn813gw100"))+" ",1),B])]),(0,s.createElementVNode)("div",M,[(0,s.createVNode)(p,{type:"outline",class:"mr",onClick:ee},{default:(0,s.withCtx)((()=>[(0,s.createTextVNode)((0,s.toDisplayString)(e.$t("simpleInstall.index.5mpn813gw4c0")),1)])),_:1}),(0,s.createVNode)(p,{type:"primary",onClick:n[2]||(n[2]=t=>e.$router.push({name:"DailyOps"}))},{default:(0,s.withCtx)((()=>[(0,s.createTextVNode)((0,s.toDisplayString)(e.$t("simpleInstall.index.5mpn813gw7w0")),1)])),_:1})])]))])):(0,s.createCommentVNode)("",!0)])}}}),U=o(83744);const F=(0,U.Z)(j,[["__scopeId","data-v-25fd8dbe"]]);var Z=F},3334:function(e){!function(t,o){e.exports=o()}(window,(function(){return function(e){var t={};function o(s){if(t[s])return t[s].exports;var n=t[s]={i:s,l:!1,exports:{}};return e[s].call(n.exports,n,n.exports,o),n.l=!0,n.exports}return o.m=e,o.c=t,o.d=function(e,t,s){o.o(e,t)||Object.defineProperty(e,t,{enumerable:!0,get:s})},o.r=function(e){"undefined"!=typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(e,"__esModule",{value:!0})},o.t=function(e,t){if(1&t&&(e=o(e)),8&t)return e;if(4&t&&"object"==typeof e&&e&&e.__esModule)return e;var s=Object.create(null);if(o.r(s),Object.defineProperty(s,"default",{enumerable:!0,value:e}),2&t&&"string"!=typeof e)for(var n in e)o.d(s,n,function(t){return e[t]}.bind(null,n));return s},o.n=function(e){var t=e&&e.__esModule?function(){return e.default}:function(){return e};return o.d(t,"a",t),t},o.o=function(e,t){return Object.prototype.hasOwnProperty.call(e,t)},o.p="",o(o.s=0)}([function(e,t,o){"use strict";Object.defineProperty(t,"__esModule",{value:!0}),t.AttachAddon=void 0;var s=function(){function e(e,t){this._disposables=[],this._socket=e,this._socket.binaryType="arraybuffer",this._bidirectional=!t||!1!==t.bidirectional}return e.prototype.activate=function(e){var t=this;this._disposables.push(n(this._socket,"message",(function(t){var o=t.data;e.write("string"==typeof o?o:new Uint8Array(o))}))),this._bidirectional&&(this._disposables.push(e.onData((function(e){return t._sendData(e)}))),this._disposables.push(e.onBinary((function(e){return t._sendBinary(e)})))),this._disposables.push(n(this._socket,"close",(function(){return t.dispose()}))),this._disposables.push(n(this._socket,"error",(function(){return t.dispose()})))},e.prototype.dispose=function(){this._disposables.forEach((function(e){return e.dispose()}))},e.prototype._sendData=function(e){1===this._socket.readyState&&this._socket.send(e)},e.prototype._sendBinary=function(e){if(1===this._socket.readyState){for(var t=new Uint8Array(e.length),o=0;o<e.length;++o)t[o]=255&e.charCodeAt(o);this._socket.send(t)}},e}();function n(e,t,o){return e.addEventListener(t,o),{dispose:function(){o&&e.removeEventListener(t,o)}}}t.AttachAddon=s}])}))},12617:function(e){!function(t,o){e.exports=o()}(self,(function(){return(()=>{"use strict";var e={775:(e,t)=>{Object.defineProperty(t,"__esModule",{value:!0}),t.FitAddon=void 0;var o=function(){function e(){}return e.prototype.activate=function(e){this._terminal=e},e.prototype.dispose=function(){},e.prototype.fit=function(){var e=this.proposeDimensions();if(e&&this._terminal){var t=this._terminal._core;this._terminal.rows===e.rows&&this._terminal.cols===e.cols||(t._renderService.clear(),this._terminal.resize(e.cols,e.rows))}},e.prototype.proposeDimensions=function(){if(this._terminal&&this._terminal.element&&this._terminal.element.parentElement){var e=this._terminal._core;if(0!==e._renderService.dimensions.actualCellWidth&&0!==e._renderService.dimensions.actualCellHeight){var t=window.getComputedStyle(this._terminal.element.parentElement),o=parseInt(t.getPropertyValue("height")),s=Math.max(0,parseInt(t.getPropertyValue("width"))),n=window.getComputedStyle(this._terminal.element),r=o-(parseInt(n.getPropertyValue("padding-top"))+parseInt(n.getPropertyValue("padding-bottom"))),i=s-(parseInt(n.getPropertyValue("padding-right"))+parseInt(n.getPropertyValue("padding-left")))-e.viewport.scrollBarWidth;return{cols:Math.max(2,Math.floor(i/e._renderService.dimensions.actualCellWidth)),rows:Math.max(1,Math.floor(r/e._renderService.dimensions.actualCellHeight))}}}},e}();t.FitAddon=o}},t={};return function o(s){if(t[s])return t[s].exports;var n=t[s]={exports:{}};return e[s](n,n.exports,o),n.exports}(775)})()}))},47327:function(e,t,o){"use strict";function s(e){return s="function"==typeof Symbol&&"symbol"==typeof Symbol.iterator?function(e){return typeof e}:function(e){return e&&"function"==typeof Symbol&&e.constructor===Symbol&&e!==Symbol.prototype?"symbol":typeof e},s(e)}function n(e,t){if("object"!==s(e)||null===e)return e;var o=e[Symbol.toPrimitive];if(void 0!==o){var n=o.call(e,t||"default");if("object"!==s(n))return n;throw new TypeError("@@toPrimitive must return a primitive value.")}return("string"===t?String:Number)(e)}function r(e){var t=n(e,"string");return"symbol"===s(t)?t:String(t)}function i(e,t,o){return t=r(t),t in e?Object.defineProperty(e,t,{value:o,enumerable:!0,configurable:!0,writable:!0}):e[t]=o,e}o.d(t,{Z:function(){return i}})}}]);
//# sourceMappingURL=6537.26526cff.js.map