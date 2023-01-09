"use strict";(self["webpackChunkweb_ui"]=self["webpackChunkweb_ui"]||[]).push([[5505,325,9674,7271,8208],{61362:function(e,t,o){o.r(t),o.d(t,{default:function(){return p}});var n=o(47327),a=o(43610),l=o(8603),i=o(64253),r=o(28208);const d=(0,l.M)(),s=(0,a.m)(),c={attrs:{line:{stroke:"#222c36",strokeWidth:1,targetMarker:{name:"classic",size:8}}},zIndex:0,connector:{name:"rounded"},router:{name:"manhattan"}};class p{static init(e,t,o,n){return o&&o.offsetWidth&&o.offsetHeight,this.option.container=document.getElementById(e.containerId),this.option.connecting&&(this.option.connecting.validateEdge=({edge:e})=>(n&&n.edgeConnect&&n.edgeConnect(),(0,r.lineConnect)(this.graph.getCellById(e.source.cell),this.graph.getCellById(e.target.cell),this.graph))),this.graph=new i.kJ(e.canvasOption?{...this.option,...e.canvasOption}:this.option),this.initEvent(e,t,n),this.graph}static initEvent(e,t,o){const{graph:n}=this,a=document.getElementById(e.containerId),l=({cell:e})=>{const t=a.querySelectorAll(".x6-port-body");e.data&&e.data.disabled?this.togglePorts(t,!1):this.togglePorts(t,!0)};n.on("node:mouseenter",i.Iw.debounce((({node:e})=>{if(!e.data.portsDisabled){e.setData({showDisabledCheckbox:!0});const t=a.querySelectorAll(".x6-port-body");e.data.disabled||this.togglePorts(t,!0),e.on("change:data",l)}})),500),n.on("node:mouseleave",(({node:e})=>{if(!e.data.portsDisabled){e.setData({showDisabledCheckbox:!1});const t=a.querySelectorAll(".x6-port-body");this.togglePorts(t,!1),e.off("change:data",l)}})),n.on("node:collapse",(({node:e,e:t})=>{t.stopPropagation(),e.toggleCollapse();const o=e.isCollapsed(),n=e.getDescendants();n.forEach((e=>o?e.hide():e.show))})),n.on("cell:selected",(({cell:e})=>{e.setData({antvSelected:!0})})),n.on("cell:unselected",(({cell:e})=>{e.setData({antvSelected:!1})})),n.on("blank:click",(()=>{s.setSelectNode(null,!1)})),n.on("edge:mouseenter",(({edge:e})=>{(!e.data||!e.data.operate||e.data.operate&&Array.isArray(e.data.operate)&&e.data.operate.includes("showChangeArrow"))&&e.addTools(["source-arrowhead","target-arrowhead"])})),n.on("edge:mouseleave",(({edge:e})=>{(!e.data||!e.data.operate||e.data.operate&&Array.isArray(e.data.operate)&&e.data.operate.includes("showChangeArrow"))&&e.removeTools()})),n.bindKey("delete",(()=>{const e=n.getSelectedCells();if(e.length){const t=e.filter((e=>!e.data.operate||e.data.operate.includes("delete")));n.removeCells(t)}})),n.bindKey(["ctrl+s"],(()=>{const e=n.toJSON();e.cells.forEach((e=>{e.data&&e.data.cells_type?e.cells_type=e.data.cells_type:"edge"===e.shape&&(e.cells_type="line")}))})),n.on("cell:removed",(e=>{e.cell&&"join"===e.cell.cells_type&&d.removeDatabaseTable(e.cell.table),o&&o.deleteCell&&o.deleteCell()}))}static togglePorts(e,t){for(let o=0,n=e.length;o<n;o+=1)e[o].style.visibility=t?"visible":"hidden"}static createEdge(e,t,o){this.graph.addEdge({shape:"edge",source:e,target:t,...c,data:o||{}})}}(0,n.Z)(p,"graph",void 0),(0,n.Z)(p,"option",{width:1920,height:1080,autoResize:!0,background:{color:"#fff"},grid:{size:10,visible:!0,type:"doubleMesh",args:[{color:"#e3e3e3",thickness:1},{color:"#e3e3e3",thickness:1,factor:4}]},interacting:function(e){const t=e.cell.getData();return!t||!t.disableMove},scroller:{enabled:!0,pageVisible:!1,pageBreak:!0,pannable:!1},mousewheel:{enabled:!0,modifiers:["ctrl","meta"],minScale:.5,maxScale:2},selecting:{enabled:!0,multiple:!0,rubberband:!0,movable:!0},connecting:{anchor:"center",connectionPoint:"anchor",allowBlank:!1,allowMulti:!0,highlight:!0,snap:{radius:30},createEdge(){return new i.bn.kS(c)},validateConnection:({sourceView:e,targetView:t,sourceMagnet:o,targetMagnet:n})=>e!==t&&(!!o&&!!n)},highlighting:{magnetAvailable:{name:"stroke",args:{padding:4,attrs:{strokeWidth:4,stroke:"rgba(200,200,200)"}}}},snapline:!0,history:!0,clipboard:!0,keyboard:!0,embedding:{enabled:!0,findParent({node:e}){const t=e.getBBox();return this.getNodes().filter((e=>{const o=e.getData();if(o&&o.parent){const o=e.getBBox();return t.isIntersectWithRect(o)}return!1}))}}}),(0,n.Z)(p,"renderInitJson",(()=>{const e=d.getFlowDataInfo,t=JSON.parse(e.operatorContent);t&&t.cells&&t.cells.length>0&&(t.cells.forEach((e=>{e.cells_type in s.getRules&&"function"===typeof s.getRules[e.cells_type]&&(e.data.rule=s.getRules[e.cells_type])})),p.graph.fromJSON(t))})),(0,n.Z)(p,"createNode",((e,t,o,n)=>{if(t){let a=null;return e.stencil.nodes.forEach((e=>{e.child.forEach((e=>{e.data.configName===t&&(a=p.graph.createNode({shape:"BaseNode",x:(p.option.width?p.option.width:1920)/2+(o||0),y:(p.option.height?p.option.height/2:1080)/2+60+(n||0),width:220,height:60,ports:e.ports?e.ports:{},data:e.data?e.data:{}}))}))})),a&&p.graph.addNode(a),a}}))},39674:function(e,t,o){o.r(t),o.d(t,{options:function(){return l}});var n=o(27271),a=o(90989);const l=()=>({containerId:"database_container_id",stencil:{nodes:[{name:a.Z.global.t("modeling.hooks.index.5m83asj8uws0"),icon:"icon-layers",child:[{width:180,height:35,shape:"vue-shape",data:{type:"BaseNode",cells_type:"dataSource",text:a.Z.global.t("modeling.hooks.index.5m83asj8vok0"),configName:"DataSource",icon:"modeling-dataSource",source:"",rule:()=>!0},ports:n.edge1.ports}]},{name:a.Z.global.t("modeling.hooks.index.5m83asj8vsw0"),icon:"icon-common",child:[{width:180,height:35,shape:"vue-shape",data:{type:"BaseNode",cells_type:"query",text:a.Z.global.t("modeling.hooks.index.5m83asjg7480"),configName:"QueryOperator",icon:"modeling-chaxun",table:"",fields:[],rule:e=>Boolean(e.table)&&0===e.fields.filter((e=>!e.value)).length},ports:n.edge1.ports},{width:180,height:35,shape:"vue-shape",data:{type:"BaseNode",cells_type:"update",text:a.Z.global.t("modeling.hooks.index.5m83asjg7f40"),configName:"UpdateOperator",icon:"modeling-gengxin",table:"",list:[],rule:e=>{let t=!0;return e.table||(t=!1),e.list.forEach((e=>{e.field&&e.value&&(e.valueType!==a.Z.global.t("modeling.hooks.index.5m83asjg7j80")||e.fieldType!==a.Z.global.t("modeling.hooks.index.5m83asjg7m80")||e.splitValue)||(t=!1)})),t}},ports:n.edge1.ports},{width:180,height:35,shape:"vue-shape",data:{type:"BaseNode",cells_type:"delete",text:a.Z.global.t("modeling.hooks.index.5m83asjg7oo0"),configName:"DeleteOperator",icon:"modeling-shanchu",table:"",fields:[],rule:e=>Boolean(e.table)&&0===e.fields.filter((e=>!e)).length},ports:n.edge1.ports},{width:180,height:35,shape:"vue-shape",data:{type:"BaseNode",cells_type:"insert",text:a.Z.global.t("modeling.hooks.index.5m83asjg7rk0"),configName:"InsertOperator",icon:"modeling-charu",table:"",list1:[],rule:e=>Boolean(e.table)&&0===e.list1.filter((e=>0!==e.filter((e=>!e.field||!e.value)).length)).length},ports:n.edge1.ports}]},{name:a.Z.global.t("modeling.hooks.index.5m83asjg7u80"),icon:"icon-apps",child:[{width:180,height:35,shape:"vue-shape",data:{type:"BaseNode",cells_type:"condition",text:a.Z.global.t("modeling.hooks.index.5m83asjg7x40"),configName:"ConditionOperator",icon:"modeling-tiaojian",or:[],rule:e=>0===e.or.filter((e=>0!==e.filter((e=>!(e.field&&e.condition&&e.value))).length)).length},ports:n.edge1.ports},{width:180,height:35,shape:"vue-shape",data:{type:"BaseNode",cells_type:"join",text:a.Z.global.t("modeling.hooks.index.5m83asjg7zs0"),configName:"BridgingOperator",icon:"modeling-qiaojie",table:"",connectType:"",condition:[],rule:e=>e.table&&e.connectType&&0===e.condition.filter((e=>0!==e.filter((e=>!(e.field&&e.condition&&e.value))).length)).length},ports:n.edge1.ports},{width:180,height:35,shape:"vue-shape",data:{type:"BaseNode",cells_type:"sort",text:a.Z.global.t("modeling.hooks.index.5m83asjg8280"),configName:"SortOperator",icon:"modeling-paixu",sorts:[],rule:e=>0===e.sorts.filter((e=>!e.field||!e.value)).length},ports:n.edge1.ports},{width:180,height:35,shape:"vue-shape",data:{type:"BaseNode",cells_type:"limit",text:a.Z.global.t("modeling.hooks.index.5m83asjg84w0"),configName:"RestrictionOperator",icon:"modeling-xiantiao",restriction:{skip:null,limitCount:null}},ports:n.edge1.ports},{width:180,height:35,shape:"vue-shape",data:{type:"BaseNode",cells_type:"group",text:a.Z.global.t("modeling.hooks.index.5m83asjg87c0"),configName:"GroupOperator",icon:"modeling-fenzu",groups:[]},ports:n.edge1.ports},{width:180,height:35,shape:"vue-shape",data:{type:"BaseNode",cells_type:"polymerization",text:a.Z.global.t("modeling.hooks.index.5m83asjg8ak0"),icon:"modeling-juhe",configName:"PolymerizationOperator",polymerization:[]},ports:n.edge1.ports},{width:180,height:35,shape:"vue-shape",data:{type:"BaseNode",cells_type:"mapping",text:a.Z.global.t("modeling.hooks.index.5m83asjg8f40"),configName:"MapOperator",icon:"modeling-yingshe",mappings:[],rule:e=>0===e.mappings.filter((e=>!e.field||!e.condition||!e.value)).length},ports:n.edge1.ports},{width:180,height:35,shape:"vue-shape",data:{type:"BaseNode",cells_type:"dictionary",text:a.Z.global.t("modeling.hooks.index.5m83asjg8i00"),configName:"DictionaryOperator",icon:"modeling-zidian",table:"",field:"",matchField:"",rigidField:"",rule:e=>e.table&&e.field&&e.matchField&&e.rigidField},ports:n.edge1.ports}]}]},canvasOption:{selecting:{multiple:!0,enabled:!0,rubberband:!0,movable:!0}}})},27271:function(e,t,o){o.r(t),o.d(t,{edge1:function(){return n}});const n={ports:{groups:{top:{position:"top",attrs:{circle:{r:5,magnet:!0,stroke:"#5F95FF",strokeWidth:1,fill:"#fff",style:{visibility:"hidden"}}}},bottom:{position:"bottom",attrs:{circle:{r:5,magnet:!0,stroke:"#5F95FF",strokeWidth:1,fill:"#fff",style:{visibility:"hidden"}}}},left:{position:"left",attrs:{circle:{r:5,magnet:!0,stroke:"#5F95FF",strokeWidth:1,fill:"#fff",style:{visibility:"hidden"}}}},right:{position:"right",attrs:{circle:{r:5,magnet:!0,stroke:"#5F95FF",strokeWidth:1,fill:"#fff",style:{visibility:"hidden"}}}}},items:[{group:"top",id:"port1"},{group:"bottom",id:"port2"},{group:"left",id:"port3"},{group:"right",id:"port4"}]}}},28208:function(e,t,o){o.r(t),o.d(t,{lineConnect:function(){return i}});var n=o(15784),a=o(90989);const l=["query","update","delete","insert"],i=(e,t,o)=>{const i=e=>{n.Z.warning({content:e,duration:5e3})};if("dataSource"===t.data.cells_type)return i(a.Z.global.t("modeling.utils.index.5m835tc0m9o0")),!1;if("dataSource"===e.data.cells_type&&!l.includes(t.data.cells_type))return i(a.Z.global.t("modeling.utils.index.5m835tc0n8k0")),!1;if(l.includes(e.data.cells_type)&&l.includes(t.data.cells_type))return i(a.Z.global.t("modeling.utils.index.5m835tc0ndo0")),!1;const r=o.getEdges();if("dataSource"!==e.data.cells_type){const t=r.filter((t=>t.source.cell===e.id));if(t.length>1)return i(a.Z.global.t("modeling.utils.index.5m835tc0nh00")),!1}const d=r.filter((e=>e.target.cell===t.id));return!(d.length>1)||(i(a.Z.global.t("modeling.utils.index.5m835tc0njw0")),!1)}},53803:function(e,t,o){o.r(t),o.d(t,{default:function(){return f}});var n=o(70821),a=o(89965),l=o(64253),i=o(42119),r=o(15784),d=o(68957),s=o(57330);const c={class:"header-container"},p={class:"text"},g={class:"d-button"};var m=(0,n.defineComponent)({emits:["operate"],setup(e,{expose:t,emit:o}){const{t:m}=(0,s.QT)(),u=((0,i.yj)(),(0,n.ref)(!0)),h=(0,n.reactive)({graph:null,options:null}),f=(e,t)=>{h.graph=e,h.options=t},v=(e,t)=>{if("antvLeft"===e)u.value=!u.value,o("operate",e);else if("revoke"===e)h.graph?.undo();else if("recover"===e)h.graph?.redo();else if("enlarge"===e)h.graph?.zoomTo(h.graph?.zoom()+.15,{maxScale:2,minScale:.5});else if("narrow"===e)h.graph?.zoomTo(h.graph?.zoom()-.15,{maxScale:2,minScale:.5});else if("center"===e)h.graph?.center();else if("delete"===e){const e=h.graph?.getSelectedCells();if(e&&e.length){const t=e.filter((e=>!e.data.operate||e.data.operate.includes("delete")));h.graph?.removeCells(t)}}else if("save"===e){if(h.graph){let e=(0,d.jsonFormat)(h.graph);(0,a.Up)(String(window.$wujie?.props.data.id),e)}}else if("export"===e)if("json"===t){let e=h.graph&&(0,d.jsonFormat)(h.graph);if(e){const t=m("modeling.components.Header.5m78qbcztzk0")+`${(new Date).toISOString()}.json`,o="object"===typeof e?JSON.stringify(e,void 0,2):e;if(o){const e=new Blob([o],{type:"text/json"}),n=document.createElement("a");n.setAttribute("style","display: none"),n.download=t,n.href=window.URL.createObjectURL(e),n.dataset.downloadurl=["text/json",n.download,n.href].join(":"),document.body.appendChild(n),n.click(),document.body.removeChild(n)}}}else"svg"===t?h.graph?.toSVG((e=>{l.Vb.downloadDataUri(l.Vb.svgToDataUrl(e),m("modeling.components.Header.5m78qbczu200")+"{(new Date()).toISOString()}.svg")}),{serializeImages:!0,stylesheet:"\n          .base-node-container {\n            width: 100%;\n            height: 100%;\n            background: #ffffff;\n            box-shadow: 0px 2px 8px rgb(165 165 165 / 33%);\n            border-radius: 4px;\n            transition: opacity 0.3s;\n            display: flex;\n            align-items: center;\n            box-sizing: border-box;\n            padding: 0 10px;\n            position: relative;\n          }\n          .base-node-icon { display: none !important; }\n        "}):"png"!==t&&"jpeg"!==t||h.graph?.toPNG((e=>{l.Vb.downloadDataUri(e,m("modeling.components.Header.5m78qbczu500")+`${(new Date).toISOString()}.${t}`)}),{serializeImages:!0,stylesheet:"\n          .base-node-container {\n            width: 100%;\n            height: 100%;\n            background: #ffffff;\n            box-shadow: 0px 2px 8px rgb(165 165 165 / 33%);\n            border-radius: 4px;\n            transition: opacity 0.3s;\n            display: flex;\n            align-items: center;\n            box-sizing: border-box;\n            padding: 0 10px;\n            position: relative;\n          }\n          .base-node-icon { display: none !important; }\n        ",padding:{top:20,right:20,bottom:20,left:20}});else if("import"===e)b.value.click();else if("code"===e){let t=h.graph&&(0,d.jsonFormat)(h.graph);t&&o("operate",e,t)}else if("run"===e){let t=h.graph&&(0,d.jsonFormat)(h.graph),n=(0,d.checkData)(t);t&&n&&o("operate",e,t)}else"visual"===e&&o("operate",e)},b=(0,n.ref)(null),y=()=>{const e=b.value.files[0],t=new FileReader;t.onloadend=()=>{if(t.result)try{const e=JSON.parse(t.result);h.graph?.fromJSON(e)}catch(e){r.Z.error({content:m("modeling.components.Header.5m78qbczu8c0")})}finally{b.value.value=""}},t.readAsText(e)};return t({init:f}),(e,t)=>{const o=(0,n.resolveComponent)("icon-menu-fold"),a=(0,n.resolveComponent)("icon-menu-unfold"),l=(0,n.resolveComponent)("icon-save"),i=(0,n.resolveComponent)("a-tooltip"),r=(0,n.resolveComponent)("icon-delete"),d=(0,n.resolveComponent)("icon-undo"),s=(0,n.resolveComponent)("icon-refresh"),m=(0,n.resolveComponent)("icon-zoom-in"),h=(0,n.resolveComponent)("icon-zoom-out"),f=(0,n.resolveComponent)("icon-align-center"),w=(0,n.resolveComponent)("icon-import"),C=(0,n.resolveComponent)("icon-export"),k=(0,n.resolveComponent)("a-doption"),N=(0,n.resolveComponent)("a-dropdown"),x=(0,n.resolveComponent)("icon-code-square"),V=(0,n.resolveComponent)("icon-play-circle"),_=(0,n.resolveComponent)("icon-bar-chart");return(0,n.openBlock)(),(0,n.createElementBlock)("div",c,[(0,n.createElementVNode)("div",{class:"toggle-antv-left",onClick:t[0]||(t[0]=e=>v("antvLeft"))},[(0,n.withDirectives)((0,n.createVNode)(o,null,null,512),[[n.vShow,u.value]]),(0,n.withDirectives)((0,n.createVNode)(a,null,null,512),[[n.vShow,!u.value]]),(0,n.createElementVNode)("div",p,(0,n.toDisplayString)(e.$t("modeling.components.Header.5m78qbczrv40")),1)]),(0,n.createVNode)(i,{content:e.$t("modeling.components.Header.5m78qbczsmk0")},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",{class:"d-button",onClick:t[1]||(t[1]=e=>v("save"))},[(0,n.createVNode)(l)])])),_:1},8,["content"]),(0,n.createVNode)(i,{content:e.$t("modeling.components.Header.5m78qbczsqw0")},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",{class:"d-button",onClick:t[2]||(t[2]=e=>v("delete"))},[(0,n.createVNode)(r)])])),_:1},8,["content"]),(0,n.createVNode)(i,{content:e.$t("modeling.components.Header.5m78qbczsto0")},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",{class:"d-button",onClick:t[3]||(t[3]=e=>v("revoke"))},[(0,n.createVNode)(d)])])),_:1},8,["content"]),(0,n.createVNode)(i,{content:e.$t("modeling.components.Header.5m78qbczswg0")},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",{class:"d-button",onClick:t[4]||(t[4]=e=>v("recover"))},[(0,n.createVNode)(s)])])),_:1},8,["content"]),(0,n.createVNode)(i,{content:e.$t("modeling.components.Header.5m78qbczszg0")},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",{class:"d-button",onClick:t[5]||(t[5]=e=>v("enlarge"))},[(0,n.createVNode)(m)])])),_:1},8,["content"]),(0,n.createVNode)(i,{content:e.$t("modeling.components.Header.5m78qbczt2k0")},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",{class:"d-button",onClick:t[6]||(t[6]=e=>v("narrow"))},[(0,n.createVNode)(h)])])),_:1},8,["content"]),(0,n.createVNode)(i,{content:e.$t("modeling.components.Header.5m78qbczt4w0")},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",{class:"d-button",onClick:t[7]||(t[7]=e=>v("center"))},[(0,n.createVNode)(f)])])),_:1},8,["content"]),(0,n.createVNode)(i,{content:e.$t("modeling.components.Header.5m78qbczt7g0")},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",{class:"d-button",onClick:t[8]||(t[8]=e=>v("import"))},[(0,n.createVNode)(w)])])),_:1},8,["content"]),(0,n.createVNode)(i,{content:e.$t("modeling.components.Header.5m78qbczt9s0")},{default:(0,n.withCtx)((()=>[(0,n.createVNode)(N,null,{content:(0,n.withCtx)((()=>[(0,n.createVNode)(k,{onClick:t[9]||(t[9]=e=>v("export","json"))},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("modeling.components.Header.5m78qbcztdc0")),1)])),_:1}),(0,n.createVNode)(k,{onClick:t[10]||(t[10]=e=>v("export","svg"))},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("modeling.components.Header.5m78qbcztg00")),1)])),_:1}),(0,n.createVNode)(k,{onClick:t[11]||(t[11]=e=>v("export","jpeg"))},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("modeling.components.Header.5m78qbcztio0")),1)])),_:1}),(0,n.createVNode)(k,{onClick:t[12]||(t[12]=e=>v("export","png"))},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("modeling.components.Header.5m78qbcztlk0")),1)])),_:1})])),default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",g,[(0,n.createVNode)(C)])])),_:1})])),_:1},8,["content"]),(0,n.createVNode)(i,{content:e.$t("modeling.components.Header.5m78qbcztok0")},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",{class:"d-button",onClick:t[13]||(t[13]=e=>v("code"))},[(0,n.createVNode)(x)])])),_:1},8,["content"]),(0,n.createVNode)(i,{content:e.$t("modeling.components.Header.5m78qbcztqs0")},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",{class:"d-button",onClick:t[14]||(t[14]=e=>v("run"))},[(0,n.createVNode)(V)])])),_:1},8,["content"]),(0,n.createVNode)(i,{content:e.$t("modeling.components.Header.5m78qbcztws0")},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",{class:"d-button",onClick:t[15]||(t[15]=e=>v("visual"))},[(0,n.createVNode)(_)])])),_:1},8,["content"]),(0,n.createElementVNode)("input",{type:"file",id:"files",ref:(e,t)=>{t["refFile"]=e,b.value=e},style:{display:"none"},onChange:y},null,544)])}}}),u=o(83744);const h=(0,u.Z)(m,[["__scopeId","data-v-021b3bba"]]);var f=h},17867:function(e,t,o){o.r(t),o.d(t,{default:function(){return p}});var n=o(70821),a=o(43610);const l={key:0},i={key:1},r={class:"config-panel-bottom"};var d=(0,n.defineComponent)({setup(e,{expose:t}){const d=(0,a.m)(),s=(0,n.ref)(!1);let c={DataSource:(0,n.markRaw)((0,n.defineAsyncComponent)((()=>Promise.all([o.e(8794),o.e(6145)]).then(o.bind(o,78794))))),InsertOperator:(0,n.markRaw)((0,n.defineAsyncComponent)((()=>Promise.all([o.e(9032),o.e(1063)]).then(o.bind(o,89032))))),QueryOperator:(0,n.markRaw)((0,n.defineAsyncComponent)((()=>Promise.all([o.e(3267),o.e(372)]).then(o.bind(o,73267))))),UpdateOperator:(0,n.markRaw)((0,n.defineAsyncComponent)((()=>Promise.all([o.e(6754),o.e(2819)]).then(o.bind(o,96754))))),DeleteOperator:(0,n.markRaw)((0,n.defineAsyncComponent)((()=>Promise.all([o.e(6713),o.e(6024)]).then(o.bind(o,96713))))),BridgingOperator:(0,n.markRaw)((0,n.defineAsyncComponent)((()=>Promise.all([o.e(1792),o.e(1015)]).then(o.bind(o,31792))))),ConditionOperator:(0,n.markRaw)((0,n.defineAsyncComponent)((()=>Promise.all([o.e(6302),o.e(8515)]).then(o.bind(o,76302))))),SortOperator:(0,n.markRaw)((0,n.defineAsyncComponent)((()=>Promise.all([o.e(895),o.e(7700)]).then(o.bind(o,40895))))),RestrictionOperator:(0,n.markRaw)((0,n.defineAsyncComponent)((()=>o.e(1313).then(o.bind(o,41701))))),DictionaryOperator:(0,n.markRaw)((0,n.defineAsyncComponent)((()=>Promise.all([o.e(6911),o.e(6719)]).then(o.bind(o,26911))))),MapOperator:(0,n.markRaw)((0,n.defineAsyncComponent)((()=>Promise.all([o.e(500),o.e(5760)]).then(o.bind(o,50500))))),GroupOperator:(0,n.markRaw)((0,n.defineAsyncComponent)((()=>Promise.all([o.e(8183),o.e(5717)]).then(o.bind(o,28183))))),PolymerizationOperator:(0,n.markRaw)((0,n.defineAsyncComponent)((()=>Promise.all([o.e(13),o.e(5646)]).then(o.bind(o,90013)))))};const p=(0,n.ref)(0),g=()=>{localStorage.setItem("ModelingConfigPancelWidth",String(p.value))},m=(0,n.reactive)({cellData:{},configComponent:null}),u=(0,n.computed)((()=>({transform:s.value?"translateX(0)":"translateX(12px)"}))),h=()=>{d.setSelectNode(null,!1)},f=(0,n.ref)(null),v=()=>{d.$onAction((({name:e,after:t})=>{t((()=>{if("setSelectNode"===e){let e=d.selectNode,t=d.showConfig;t&&e?(m.cellData=e.data,e.data&&e.data.configName&&Object.keys(c).includes(e.data.configName)?(m.configComponent=c[e.data.configName],s.value=!0,(0,n.nextTick)((()=>{const e=localStorage.getItem("ModelingConfigPancelWidth");p.value=e?Number(e):500})),(0,n.nextTick)((()=>{const t=()=>{f.value?f.value.init(e):setTimeout(t,100)};t()}))):(s.value=!1,setTimeout((()=>{m.cellData={},m.configComponent=null}),500))):(s.value=!1,setTimeout((()=>{m.cellData={},m.configComponent=null}),500))}}))}))},b=()=>{s.value=!1},y=(0,n.computed)((()=>({open:m.cellData&&m.configComponent})));return t({init:v,closeConfigPanel:b}),(e,t)=>{const o=(0,n.resolveComponent)("a-button"),a=(0,n.resolveComponent)("a-resize-box");return(0,n.openBlock)(),(0,n.createBlock)(n.Transition,{mode:"out-in",name:"d-config-pancel-animation"},{default:(0,n.withCtx)((()=>[(0,n.withDirectives)((0,n.createVNode)(a,{directions:["left"],width:p.value,"onUpdate:width":t[0]||(t[0]=e=>p.value=e),class:"config-panel-container",style:(0,n.normalizeStyle)((0,n.unref)(u)),onMovingEnd:g},{default:(0,n.withCtx)((()=>[(0,n.createElementVNode)("div",{class:(0,n.normalizeClass)(["config-panel-content",(0,n.unref)(y)])},[(0,n.unref)(m).configComponent&&(0,n.unref)(m).cellData?((0,n.openBlock)(),(0,n.createElementBlock)("div",l,[((0,n.openBlock)(),(0,n.createBlock)((0,n.resolveDynamicComponent)((0,n.unref)(m).configComponent),{ref:(e,t)=>{t["comRef"]=e,f.value=e}},null,512))])):((0,n.openBlock)(),(0,n.createElementBlock)("div",i,(0,n.toDisplayString)(e.$t("modeling.config-panel.index.5m7adga7to40")),1))],2),(0,n.createElementVNode)("div",r,[(0,n.createVNode)(o,{type:"primary",onClick:h},{default:(0,n.withCtx)((()=>[(0,n.createTextVNode)((0,n.toDisplayString)(e.$t("modeling.dy_common.saveAndClose")),1)])),_:1})])])),_:1},8,["width","style"]),[[n.vShow,!!s.value&&s.value]])])),_:1})}}}),s=o(83744);const c=(0,s.Z)(d,[["__scopeId","data-v-42c1e284"]]);var p=c},4064:function(e,t,o){o.r(t),o.d(t,{default:function(){return E}});var n=o(70821),a=o(76574),l=o(17867),i=o(61362),r=o(53803),d=o(26017),s=o(26878),c=o(39883),p=o(97021),g=o(42119),m=o(8603),u=o(39674),h=o(43610),f=o(68957),v=o(89965),b=o(57330);const y={class:"app-container"},w={class:"main-bd"},C={class:"process data-flow"},k={key:0,class:"antv-panel d-antv-component-panel"},N={class:"antv-header"},x={class:"antv-main"},V=["id"];var _=(0,n.defineComponent)({setup(e){const{t:t}=(0,b.QT)(),o=(0,g.yj)(),_=(0,m.M)(),S=(0,h.m)();S.setI18n(t);const z=(0,g.tv)();let E,D=null;const j=(0,n.reactive)({tip:t("modeling.dy_common.AutomaticallySaved"),visible:!1}),B=()=>{E&&(clearInterval(E),E=null)},O=()=>{if(j.tip=t("modeling.dy_common.AutomaticallySaving"),j.visible=!0,D){let e=(0,f.jsonFormat)(D);(0,v.Up)(String(window.$wujie?.props.data.id),e,!0).then((()=>{j.tip=t("modeling.dy_common.AutomaticallySaved"),setTimeout((()=>{j.visible=!1}),2e3)}))}},I=()=>{E=setInterval(O,1e4)},Z=(0,n.ref)(),A=(0,n.ref)(!1),R=(0,n.ref)(!1),q=(0,n.ref)(null),P=(0,n.ref)(null),$=(0,n.ref)(null),F=(0,n.ref)(),T=async()=>{let e=(0,u.options)();e&&e.containerId?(A.value=!0,(0,v.Bd)(window.$wujie?.props.data.id).then((t=>{if(200===Number(t.code)&&t.data){const a=t.data;_.setFlowDataInfo(a),_.setDatabaseInfo(a.dbName,a.clusterNodeId,a.schema).then((()=>{R.value=!0,(0,n.nextTick)((async()=>{D=i["default"].init(e,o,F.value,{edgeConnect:()=>{K()},edgeDelete:()=>{K()},deleteCell:()=>{K()}}),_.setGraph(D),D&&q.value?.init(D,e),P.value?.init(),D&&$.value?.init(D,e),i["default"].renderInitJson(),A.value=!1,I()}))})).catch((e=>{console.log(e),z.back()}))}else z.back()})).catch((()=>z.back()))):console.error({content:t("modeling.detail.index.5m7apwiz8b00")})},H=e=>{"s"===e.key&&(navigator.platform.match("Mac")?e.metaKey:e.ctrlKey)&&e.preventDefault()};(0,n.onMounted)((()=>{_.initState(),T(),document.addEventListener("keydown",H,!1)})),(0,n.onUnmounted)((()=>{document.removeEventListener("keydown",H),B()}));const M=(0,n.ref)(),U=(0,n.ref)(),W=(0,n.ref)();S.$onAction((({name:e,args:t})=>{"nodeEvent"===e&&t&&t[0]&&t[0].event&&"operate"===t[0].event&&t[0].type&&J(t[0].type,t[0].data?t[0].data:null)}));const J=(e,t)=>{if("run"===e)D&&M.value?.open(D,t,e);else if("code"===e)D&&M.value?.open(D,t,e);else if("antvLeft"===e)q.value?.toggleMenu();else if("close-config-panel"===e)P.value?.closeConfigPanel();else if("visual"===e)U.value?.open(t),J("close-config-panel");else if("visual-edit"===e&&D){let e=(0,f.jsonFormat)(D),o=(0,f.checkData)(e);e&&o&&W.value?.open(D,t)}};(0,n.onUnmounted)((()=>{_.initState()}));const L=(0,n.ref)(!1),G=()=>{L.value=!0},K=()=>{P.value?.closeConfigPanel()};return(e,t)=>((0,n.openBlock)(),(0,n.createElementBlock)("div",y,[(0,n.createElementVNode)("div",w,[(0,n.createElementVNode)("div",C,[(0,n.createVNode)((0,n.unref)(p.Z),{style:{width:"100%",height:"100%"},loading:A.value,tip:e.$t("modeling.detail.index.5m7apwiz75o0")},{default:(0,n.withCtx)((()=>[R.value?((0,n.openBlock)(),(0,n.createElementBlock)("div",k,[(0,n.createElementVNode)("div",N,[(0,n.createVNode)(r["default"],{ref:(e,t)=>{t["headerRef"]=e,$.value=e},onOperate:J},null,512),(0,n.createElementVNode)("div",{class:(0,n.normalizeClass)(["auto-save-tip",{"auto-save-tip-hidden":!(0,n.unref)(j).visible}])},(0,n.toDisplayString)((0,n.unref)(j).tip),3)]),(0,n.createElementVNode)("div",x,[(0,n.createVNode)(a["default"],{ref:(e,t)=>{t["leftSidebarRef"]=e,q.value=e}},null,512),(0,n.createElementVNode)("div",{class:"antv-main-center",ref:(e,t)=>{t["antvMainCenterRef"]=e,F.value=e}},[(0,n.createElementVNode)("div",{id:Z.value?Z.value.containerId:"database_container_id",class:"antv-container"},null,8,V)],512),(0,n.createVNode)(l["default"],{ref:(e,t)=>{t["configPanelRef"]=e,P.value=e},onConfigOpen:G},null,512)]),(0,n.createVNode)(d["default"],{ref:(e,t)=>{t["runRef"]=e,M.value=e}},null,512),(0,n.createVNode)(c["default"],{ref:(e,t)=>{t["visualEditRef"]=e,W.value=e},onOperate:J},null,512),(0,n.createVNode)(s["default"],{ref:(e,t)=>{t["visualListRef"]=e,U.value=e}},null,512)])):(0,n.createCommentVNode)("",!0)])),_:1},8,["loading","tip"])])])]))}}),S=o(83744);const z=(0,S.Z)(_,[["__scopeId","data-v-20e4e59e"]]);var E=z}}]);
//# sourceMappingURL=5505.48d8cb00.js.map