import { ElLoading, ElMessage } from 'element-plus';
import type { Column } from 'element-plus';
import { i18n } from '@/i18n/index';

const t = i18n.global.t;
/**
 * Parse the time to string
 * @param {(Object|string|number)} time
 * @param {string} cFormat
 * @returns {string | null}
 */
export function parseTime(time, cFormat) {
  if (arguments.length === 0 || !time) {
    return null;
  }
  const format = cFormat || '{y}-{m}-{d} {h}:{i}:{s}';
  let date;
  if (typeof time === 'object') {
    date = time;
  } else {
    if (typeof time === 'string') {
      if (/^[0-9]+$/.test(time)) {
        // support "1548221490638"
        time = parseInt(time);
      } else {
        // support safari
        // https://stackoverflow.com/questions/4310953/invalid-date-in-safari
        time = time.replace(new RegExp(/-/gm), '/');
      }
    }

    if (typeof time === 'number' && time.toString().length === 10) {
      time = time * 1000;
    }
    date = new Date(time);
  }
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay(),
  };
  const time_str = format.replace(/{([ymdhisa])+}/g, (result, key) => {
    const value = formatObj[key];
    // Note: getDay() returns 0 on Sunday
    if (key === 'a') {
      return [
        t('week.Sunday'),
        t('week.Monday'),
        t('week.Tuesday'),
        t('week.Wednesday'),
        t('week.Thursday'),
        t('week.Friday'),
        t('week.Saturday'),
      ][value];
    }
    return value.toString().padStart(2, '0');
  });
  return time_str;
}

/**
 * @param {string} url
 * @returns {Object}
 */
export function getQueryObject(url) {
  url = url == null ? window.location.href : url;
  const search = url.substring(url.lastIndexOf('?') + 1);
  const obj = {};
  const reg = /([^?&=]+)=([^?&=]*)/g;
  search.replace(reg, (rs, $1, $2) => {
    const name = decodeURIComponent($1);
    let val = decodeURIComponent($2);
    val = String(val);
    obj[name] = val;
    return rs;
  });
  return obj;
}

/**
 * @param {string} input value
 * @returns {number} output value
 */
export function byteLength(str) {
  // returns the byte length of an utf8 string
  let s = str.length;
  for (let i = str.length - 1; i >= 0; i--) {
    const code = str.charCodeAt(i);
    if (code > 0x7f && code <= 0x7ff) s++;
    else if (code > 0x7ff && code <= 0xffff) s += 2;
    if (code >= 0xdc00 && code <= 0xdfff) i--;
  }
  return s;
}

/**
 * @param {Array} actual
 * @returns {Array}
 */
export function cleanArray(actual) {
  const newArray = [];
  for (let i = 0; i < actual.length; i++) {
    if (actual[i]) {
      newArray.push(actual[i]);
    }
  }
  return newArray;
}

/**
 * @param {Object} json
 * @returns {Array}
 */
export function param(json) {
  if (!json) return '';
  return cleanArray(
    Object.keys(json).map((key) => {
      if (json[key] === undefined) return '';
      return encodeURIComponent(key) + '=' + encodeURIComponent(json[key]);
    }),
  ).join('&');
}

/**
 * @param {string} url
 * @returns {Object}
 */
export function param2Obj(url) {
  const search = decodeURIComponent(url.split('?')[1]).replace(/\+/g, ' ');
  if (!search) {
    return {};
  }
  const obj = {};
  const searchArr = search.split('&');
  searchArr.forEach((v) => {
    const index = v.indexOf('=');
    if (index !== -1) {
      const name = v.substring(0, index);
      const val = v.substring(index + 1, v.length);
      obj[name] = val;
    }
  });
  return obj;
}

/**
 * @param {string} val
 * @returns {string}
 */
export function html2Text(val) {
  const div = document.createElement('div');
  div.innerHTML = val;
  return div.textContent || div.innerText;
}

/**
 * Merges two objects, giving the last one precedence
 * @param {Object} target
 * @param {(Object|Array)} source
 * @returns {Object}
 */
export function objectMerge(target, source) {
  if (typeof target !== 'object') {
    target = {};
  }
  if (Array.isArray(source)) {
    return source.slice();
  }
  Object.keys(source).forEach((property) => {
    const sourceProperty = source[property];
    if (typeof sourceProperty === 'object') {
      target[property] = objectMerge(target[property], sourceProperty);
    } else {
      target[property] = sourceProperty;
    }
  });
  return target;
}

/**
 * @param {HTMLElement} element
 * @param {string} className
 */
export function toggleClass(element, className) {
  if (!element || !className) {
    return;
  }
  let classString = element.className;
  const nameIndex = classString.indexOf(className);
  if (nameIndex === -1) {
    classString += '' + className;
  } else {
    classString =
      classString.substr(0, nameIndex) + classString.substr(nameIndex + className.length);
  }
  element.className = classString;
}

/**
 * @param {string} type
 * @returns {Date}
 */
export function getTime(type) {
  if (type === 'start') {
    return new Date().getTime() - 3600 * 1000 * 24 * 90;
  } else {
    return new Date(new Date().toDateString());
  }
}

/**
 * @param {Function} func
 * @param {number} wait
 * @param {boolean} immediate
 * @return {*}
 */
export function debounce(func, wait, immediate) {
  let timeout, args, that, timestamp, result;

  const later = function () {
    const last = +new Date() - timestamp;

    // The last time the wrapped function was called last is less than the set time interval: wait
    if (last < wait && last > 0) {
      timeout = setTimeout(later, wait - last);
    } else {
      timeout = null;
      // if immediate===true，Already called at the beginning
      if (!immediate) {
        result = func.apply(that, args);
        if (!timeout) that = args = null;
      }
    }
  };

  return function (...args) {
    that = this;
    timestamp = +new Date();
    const callNow = immediate && !timeout;
    // if no timeout, reset timeout
    if (!timeout) timeout = setTimeout(later, wait);
    if (callNow) {
      result = func.apply(that, args);
      that  = null;
    }

    return result;
  };
}

/**
 * This is just a simple version of deep copy
 * Has a lot of edge cases bug
 * If you want to use a perfect deep copy, use lodash's _.cloneDeep
 * @param {Object} source
 * @returns {Object}
 */
export function deepClone(source) {
  if (!source && typeof source !== 'object') {
    throw new Error('deepClone: error arguments');
  }
  const targetObj = source.constructor === Array ? [] : {};
  Object.keys(source).forEach((keys) => {
    if (source[keys] && typeof source[keys] === 'object') {
      targetObj[keys] = deepClone(source[keys]);
    } else {
      targetObj[keys] = source[keys];
    }
  });
  return targetObj;
}

/**
 * @param {Array} arr
 * @returns {Array}
 */
export function uniqueArr(arr) {
  return Array.from(new Set(arr));
}

/**
 * @returns {string}
 */
export function createUniqueString() {
  const timestamp = +new Date() + '';
  const randomNum = parseInt((1 + Math.random()) * 65536 + '') + '';
  return (+(randomNum + timestamp)).toString(32);
}

/**
 * Check if an element has a class
 * @param {HTMLElement} elm
 * @param {string} cls
 * @returns {boolean}
 */
export function hasClass(ele, cls) {
  return !!ele.className.match(new RegExp('(\\s|^)' + cls + '(\\s|$)'));
}

/**
 * Add class to element
 * @param {HTMLElement} elm
 * @param {string} cls
 */
export function addClass(ele, cls) {
  if (!hasClass(ele, cls)) ele.className += ' ' + cls;
}

/**
 * Remove class from element
 * @param {HTMLElement} elm
 * @param {string} cls
 */
export function removeClass(ele, cls) {
  if (hasClass(ele, cls)) {
    const reg = new RegExp('(\\s|^)' + cls + '(\\s|$)');
    ele.className = ele.className.replace(reg, ' ');
  }
}

export function getColor() {
  let str = '#';
  const arr = ['1', '2', '3', '4', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'];
  for (let i = 0; i < 6; i++) {
    const num = parseInt('' + Math.random() * 16);
    str += arr[num];
  }
  return str;
}

export const isArray = function (value) {
  return objToString.call(value) === '[object Array]';
};
const funProto = Function.prototype;
const objProto = Object.prototype;

const getPrototypeOf = Object.getPrototypeOf;

const objToString = objProto.toString;
const hasOwnProperty = objProto.hasOwnProperty;
const funToString = funProto.toString;

export const isString = function (value) {
  return objToString.call(value) === '[object String]';
};
// isPlainObject: Pure objects refer to objects declared through {} or new Object()
export const isPlainObject = function (value) {
  if (!value || objToString.call(value) !== '[object Object]') {
    return false;
  }

  const prototype = getPrototypeOf(value);

  if (prototype === null) {
    return true;
  }

  const constructor = hasOwnProperty.call(prototype, 'constructor') && prototype.constructor;

  return (
    typeof constructor === 'function' && funToString.call(constructor) === funToString.call(Object)
  );
};

// // deepObjClone: array or json object，return vice
export const deepObjClone = function (obj) {
  const weakMap = new WeakMap();
  function clone(obj) {
    if (obj == null) {
      return obj;
    }
    if (obj instanceof Date) {
      return new Date(obj);
    }
    if (obj instanceof RegExp) {
      return new RegExp(obj);
    }
    if (typeof obj !== 'object') return obj;

    if (weakMap.get(obj)) {
      return weakMap.get(obj);
    }
    const copy = new obj.constructor();
    weakMap.set(obj, copy);
    for (const key in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, key)) {
        const value = obj[key];
        copy[key] = clone(value);
      }
    }
    return copy;
  }
  return clone(obj);
};

/**
 * color: hexToRgb
 * @param str color string
 * @returns Processed color value
 */
export function hexToRgb(str: any) {
  let hexs: any = '';
  const reg = /^#?[0-9A-Fa-f]{6}$/;
  if (!reg.test(str)) return ElMessage.warning(t('utils.hexError'));
  str = str.replace('#', '');
  hexs = str.match(/../g);
  for (let i = 0; i < 3; i++) hexs[i] = parseInt(hexs[i], 16);
  return hexs;
}

/**
 * color: rgbToHex
 * @param r red
 * @param g green
 * @param b blue
 * @returns Processed color value
 */
export function rgbToHex(r: any, g: any, b: any) {
  const reg = /^\d{1,3}$/;
  if (!reg.test(r) || !reg.test(g) || !reg.test(b))
    return ElMessage.warning(t('utils.colorError', 'rgb'));
  const hexs = [r.toString(16), g.toString(16), b.toString(16)];
  for (let i = 0; i < 3; i++) if (hexs[i].length == 1) hexs[i] = `0${hexs[i]}`;
  return `#${hexs.join('')}`;
}

/**
 * Deepen Color Value
 * @param color color string
 * @param level deepening-level: 0~1
 * @returns Processed color value
 */
export function getDarkColor(color: string, level: number) {
  const reg = /^#?[0-9A-Fa-f]{6}$/;
  if (!reg.test(color)) return ElMessage.warning(t('utils.colorError', 'hex'));
  const rgb = hexToRgb(color);
  for (let i = 0; i < 3; i++) rgb[i] = Math.floor(rgb[i] * (1 - level));
  return rgbToHex(rgb[0], rgb[1], rgb[2]);
}

/**
 * Lighten Color Value
 * @param color color string
 * @param level deepening-level: 0~1
 * @returns Processed color value
 */
export function getLightColor(color: string, level: number) {
  const reg = /^#?[0-9A-Fa-f]{6}$/;
  if (!reg.test(color)) return ElMessage.warning(t('utils.colorError', 'hex'));
  const rgb = hexToRgb(color);
  for (let i = 0; i < 3; i++) rgb[i] = Math.floor((255 - rgb[i]) * level + rgb[i]);
  return rgbToHex(rgb[0], rgb[1], rgb[2]);
}

export const uuid = () => Math.random().toString(36).slice(2);

export const loadingInstance = () => {
  const loading = ElLoading.service({
    lock: true,
    text: t('common.loadData'),
    background: 'rgba(255, 255, 255, 0.7)',
  });
  return loading;
};

/**
 * dateFormat
 * @param date
 * @param fmt format, example: 'yyyy-MM-dd hh:mm:ss'
 * @returns Processed date
 */
export const dateFormat = (date, fmt) => {
  date = new Date(date);
  const o = {
    'M+': date.getMonth() + 1, // month
    'd+': date.getDate(), // day
    'h+': date.getHours(), // hour
    'm+': date.getMinutes(), // minute
    's+': date.getSeconds(), // second
    'q+': Math.floor((date.getMonth() + 3) / 3), // quarter
    S: date.getMilliseconds(), // Milliseconds
    w: date.getDay(), // week
    T: 'T',
  };
  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (date.getFullYear() + '').substr(4 - RegExp.$1.length));
  }
  for (const k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      fmt = fmt.replace(
        RegExp.$1,
        RegExp.$1.length === 1 ? o[k] : ('00' + o[k]).substr(('' + o[k]).length),
      );
    }
  }
  return fmt;
};

/**
 * format el-table-v2 data
 * @param columns example: ['col1', 'col2', 'col3']
 * @param data example: [[1,2,3],[4,5,6]] => 2row,3col
 * @param config example: { showIndex: true, indexName: 'No.', }
 * @returns Processed: columns,data
 * columns: [{title: 'col1', dataKey: 'col1', width: 150}...]
 * data: [{col1: 1, col1: 2, col1: 3}]
 */
interface FormatTableV2Options {
  showIndex?: boolean;
  indexName?: string;
  columnWidth?: number;
}
export const formatTableV2Data = (
  columns: string[],
  data: (string | number)[][],
  config?: FormatTableV2Options,
) => {
  const defaultConfig = {
    showIndex: false,
    indexName: '',
  };
  config = { ...defaultConfig, ...config };
  let columnWidth;
  if (config.columnWidth) {
    columnWidth = config.columnWidth;
  } else if (columns.length == 1) {
    columnWidth = 300;
  } else if (columns.length == 2) {
    columnWidth = 230;
  } else if (columns.length == 3) {
    columnWidth = 180;
  } else {
    columnWidth = 150;
  }
  const myColumns: Column<any>[] = columns.map((col) => {
    return {
      title: col,
      key: col,
      dataKey: col,
      width: columnWidth,
    };
  });
  const myData = data.map((rowData) => {
    const obj = {};
    myColumns.forEach((col, colIndex) => {
      obj[col.dataKey] = rowData[colIndex];
    });
    return obj;
  });
  if (config.showIndex) {
    myColumns.unshift({
      title: config.indexName || '',
      key: 'index',
      width: 50,
      cellRenderer: ({ rowIndex }) => `${rowIndex + 1}`,
    });
  }
  return {
    columns: myColumns,
    data: myData,
  };
};

/**
 * format el-table data
 * @param columns example: ['col1', 'col2', 'col3']
 * @param data example: [[1,2,3],[4,5,6]] => 2row,3col
 * @returns Processed data：[{col1: 1, col2: 2}...]
 */
export const formatTableData = (columns: string[], data: (string | number)[][]) => {
  return data.map((rowData, rowIndex) => {
    const obj = {
      id: rowIndex + 1,
    };
    columns.forEach((col, colIndex) => {
      obj[col] = rowData[colIndex];
    });
    return obj;
  });
};

export const formatEditTableData = (config: {
  columns: any[];
  data: (string | number)[][];
  idKey?: string;
  rowStatusKey?: string;
  editingSuffix?: string;
  editedSuffix?: string;
}) => {
  const options = {
    editingSuffix: '_isEditing',
    editedSuffix: '_edited',
    ...config,
  };
  return options.data.map((rowData, rowIndex) => {
    const obj = {};
    options.columns.forEach((col, colIndex) => {
      obj[col.name] = rowData[colIndex];
      obj[col.name + options.editingSuffix] = false;
      obj[col.name + options.editedSuffix] = false;
      if (options.idKey) obj[options.idKey] = rowIndex;
      if (options.rowStatusKey) obj[options.rowStatusKey] = '';
    });
    return obj;
  });
};

export const getFlexColumnWidth = (
  data: Record<string, any>[],
  key: string,
  isCalculateCharWidth?: boolean,
) => {
  const minWidth = 90;
  const maxWidth = 400;
  const maxCharLength = 50 * 3;
  key = key + '';
  if (!data || !data.length || data.length === 0 || data === undefined) {
    return minWidth;
  }
  if (!key || !key.length || key.length === 0 || key === undefined || typeof key == 'object') {
    return minWidth;
  }
  let index = 0;
  for (let i = 0; i < data.length; i++) {
    if ([null, undefined, ''].includes(data[i][key])) {
      continue;
    }
    const now_temp = data[i][key] + '';
    const max_temp = data[index][key] + '';
    if (now_temp.length > max_temp.length) {
      index = i;
    }
    if ((data[index][key] + '').length >= maxCharLength) {
      return maxWidth;
    }
  }
  let maxColumnContent = data[index][key] ?? '';
  maxColumnContent += '';
  let flexWidth = 0;
  if (isCalculateCharWidth) {
    for (const char of maxColumnContent) {
      if ((char >= 'A' && char <= 'Z') || (char >= 'a' && char <= 'z')) {
        flexWidth += 8;
      } else if (char >= '\u4e00' && char <= '\u9fa5') {
        flexWidth += 15;
      } else {
        flexWidth += 8;
      }
    }
  } else {
    flexWidth = maxColumnContent.length * 8;
  }
  if (flexWidth <= minWidth) {
    flexWidth = minWidth;
  } else if (flexWidth <= 200) {
    flexWidth = 100;
  } else if (flexWidth <= 300) {
    flexWidth = 200;
  } else if (flexWidth <= 400) {
    flexWidth = 300;
  } else {
    flexWidth = maxWidth;
  }
  return flexWidth;
};

export const dispatchEventStorage = () => {
  const setItemSign = localStorage.setItem;
  localStorage.setItem = function (...arg: string[]) {
    const setEvent = new Event('setItemEvent') as any;
    setEvent.key = arg[0];
    setEvent.value = arg[1];
    setItemSign.apply(this, arg);
    window.dispatchEvent(setEvent);
  };
};

export const manualStringify = (value) => {
  let cache = [];
  const str = JSON.stringify(value, function (key, value) {
    if (typeof value === 'object' && value !== null) {
      if (cache.indexOf(value) !== -1) {
        return;
      }
      // 收集所有的值
      cache.push(value);
    }
    return value;
  });
  cache = null;
  return str;
};

/**
 * Update only the first letter of the first word in the sentence to uppercase
 * @example 'hello world' => 'Hello world'
 */
export const upperSentenceFirstLetter = (str: string): string => {
  return str.slice(0, 1).toUpperCase() + str.slice(1).toLowerCase();
};

/**
 * Capitalize the first letter of each word
 * @example 'hello world' => 'Hello World'
 */
export const upperWordFirstLetter = (str: string): string => {
  const strArr = str.split(' ');
  for (let i = 0; i < strArr.length; i++) {
    strArr[i] = strArr[i].substring(0, 1).toUpperCase() + strArr[i].toLowerCase().substring(1);
  }
  return strArr.join(' ');
};

export const isBodyElement = (node: Element): boolean => {
  return node && node.nodeType == 1 && node.tagName.toLowerCase() == 'body';
};

export const findParentElement = (el: Element, parentClassName: string): undefined | Element => {
  if (!el) return;
  if (isBodyElement(el)) return;
  if (el.classList.contains(parentClassName)) return el;
  return findParentElement(el.parentElement, parentClassName);
};

/**
 * @param str string
 * @returns string
 * @example 'abcDef' => 'Abc Def'
 * @example ' abc Def' => 'Abc Def'
 */
export const toSpacePascalCase = (str: string): string => {
  const toUppercase = (str: string) => str.toUpperCase();
  return str
    .replace(/\B[A-Z]/g, ' $&')
    .trim()
    .replace(/^./, toUppercase)
    .replace(/\b\w/g, toUppercase);
};

export const downloadHtml = (htmlContent: BlobPart, fileName?: string): void => {
  const blob = new Blob([htmlContent], { type: 'text/html' });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName || 'download.html';
  link.click();
  URL.revokeObjectURL(url);
};

export const simpleDownloadUrl = async (url: string, fileName?: string): Promise<void> => {
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName || '';
  link.click();
};

export const downLoadURL = (url, fileName) => {
  const elink = document.createElement('a');
  elink.download = fileName;
  elink.style.display = 'none';
  if (url instanceof Blob) {
    elink.href = URL.createObjectURL(url);
  } else {
    elink.href = url;
  }
  document.body.appendChild(elink);
  elink.click();
  URL.revokeObjectURL(elink.href);
  document.body.removeChild(elink);
};

export const downLoadURLBlob = async (url: string, fileName?: string): Promise<void> => {
  if (!fileName) {
    const arr = url.split('?')[0].split('/');
    fileName = arr[arr.length - 1];
  }
  const resp = await fetch(url);
  if (!resp) return;
  const eleA = document.createElement('a');
  const blob = await resp.blob();
  if (!blob) return;
  eleA.href = URL.createObjectURL(blob);
  eleA.download = fileName;
  eleA.click();
  URL.revokeObjectURL(eleA.href);
};

export const downLoadMyBlobType = async (fileName: string, data: any): Promise<void> => {
  const blob = new Blob([data]);
  const tag = document.createElement('a');
  tag.href = window.URL.createObjectURL(blob);
  tag.download = fileName;
  tag.click();
  URL.revokeObjectURL(tag.href);
};
