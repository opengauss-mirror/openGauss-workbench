"use strict";(self["webpackChunkweb_ui"]=self["webpackChunkweb_ui"]||[]).push([[490,9451],{59451:function(e,t,r){r.r(t);var o=r(47327);class n{constructor(){(0,o.Z)(this,"_keyStr","ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=")}encode(e){let t,r,o,n,i,a,c,l="",d=0;e=this._utf8_encode(e);while(d<e.length)t=e.charCodeAt(d++),r=e.charCodeAt(d++),o=e.charCodeAt(d++),n=t>>2,i=(3&t)<<4|r>>4,a=(15&r)<<2|o>>6,c=63&o,isNaN(r)?a=c=64:isNaN(o)&&(c=64),l=l+this._keyStr.charAt(n)+this._keyStr.charAt(i)+this._keyStr.charAt(a)+this._keyStr.charAt(c);return l}decode(e){let t,r,o,n,i,a,c,l="",d=0;e=e.replace(/[^A-Za-z0-9]/g,"");while(d<e.length)n=this._keyStr.indexOf(e.charAt(d++)),i=this._keyStr.indexOf(e.charAt(d++)),a=this._keyStr.indexOf(e.charAt(d++)),c=this._keyStr.indexOf(e.charAt(d++)),t=n<<2|i>>4,r=(15&i)<<4|a>>2,o=(3&a)<<6|c,l+=String.fromCharCode(t),64!=a&&(l+=String.fromCharCode(r)),64!=c&&(l+=String.fromCharCode(o));return l=this._utf8_decode(l),l}_utf8_encode(e){e=e.replace(/\r\n/g,"\n");let t="";for(let r=0;r<e.length;r++){const o=e.charCodeAt(r);o<128?t+=String.fromCharCode(o):o>127&&o<2048?(t+=String.fromCharCode(o>>6|192),t+=String.fromCharCode(63&o|128)):(t+=String.fromCharCode(o>>12|224),t+=String.fromCharCode(o>>6&63|128),t+=String.fromCharCode(63&o|128))}return t}_utf8_decode(e){let t="",r=0,o=0,n=0,i=0;while(r<e.length)o=e.charCodeAt(r),o<128?(t+=String.fromCharCode(o),r++):o>191&&o<224?(n=e.charCodeAt(r+1),t+=String.fromCharCode((31&o)<<6|63&n),r+=2):(n=e.charCodeAt(r+1),i=e.charCodeAt(r+2),t+=String.fromCharCode((15&o)<<12|(63&n)<<6|63&i),r+=3);return t}}t["default"]=n},83744:function(e,t){t.Z=(e,t)=>{const r=e.__vccOpts||e;for(const[o,n]of t)r[o]=n;return r}},10490:function(e,t,r){r.r(t),r.d(t,{default:function(){return u}});var o=r(70821),n=r(15784),i=r(59451),a=r(57330),c=(0,o.defineComponent)({setup(e,{expose:t}){const{t:r}=(0,a.QT)(),c=new i["default"],l=(0,o.ref)(!1),d=(0,o.ref)(""),u=e=>{let t=window.location.href.split("modeling/dataflow")[0];t.includes("#/")&&(t=window.location.href.split("#/")[0]),d.value=`${t}modeling/visualization/report/share/${c.encode(e.id+"|d1a2ay1k0|")}`,l.value=!0},h=()=>{l.value=!1},f=()=>{const e=d.value,t=document.createElement("input");t.value=e,document.body.appendChild(t),t.select(),document.execCommand("copy"),document.body.removeChild(t),n.Z.success({content:r("modeling.components.Share.5m7ipfzhxd40")})},s=()=>{window.open(d.value,"_blank")};return t({open:u}),(e,t)=>{const r=(0,o.resolveComponent)("a-modal");return(0,o.openBlock)(),(0,o.createBlock)(r,{class:"share-container",visible:l.value,title:e.$t("modeling.components.Share.5m7ipfzhwh40"),"ok-text":e.$t("modeling.components.Share.5m7ipfzhx540"),"cancel-text":e.$t("modeling.components.Share.5m7ipfzhx9g0"),onCancel:h,onOk:f,width:"700px"},{default:(0,o.withCtx)((()=>[(0,o.createElementVNode)("div",{class:"share-content",onClick:s},(0,o.toDisplayString)(d.value),1)])),_:1},8,["visible","title","ok-text","cancel-text"])}}}),l=r(83744);const d=(0,l.Z)(c,[["__scopeId","data-v-6604c63e"]]);var u=d},47327:function(e,t,r){function o(e){return o="function"==typeof Symbol&&"symbol"==typeof Symbol.iterator?function(e){return typeof e}:function(e){return e&&"function"==typeof Symbol&&e.constructor===Symbol&&e!==Symbol.prototype?"symbol":typeof e},o(e)}function n(e,t){if("object"!==o(e)||null===e)return e;var r=e[Symbol.toPrimitive];if(void 0!==r){var n=r.call(e,t||"default");if("object"!==o(n))return n;throw new TypeError("@@toPrimitive must return a primitive value.")}return("string"===t?String:Number)(e)}function i(e){var t=n(e,"string");return"symbol"===o(t)?t:String(t)}function a(e,t,r){return t=i(t),t in e?Object.defineProperty(e,t,{value:r,enumerable:!0,configurable:!0,writable:!0}):e[t]=r,e}r.d(t,{Z:function(){return a}})}}]);
//# sourceMappingURL=490.0dd2a0f8.js.map