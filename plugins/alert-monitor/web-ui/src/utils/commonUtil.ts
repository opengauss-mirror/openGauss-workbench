/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

import { i18n } from '@/i18n'

export function parseContent(content: string, param: any) {
  if (!content || !param) {
    return content
  }
  const paramOpen = "${";
  const paramClose = "}";
  let start = content.indexOf(paramOpen);
  if (start === -1) {
    return content;
  }
  let result = ''
  let offset = 0
  while (start > -1) {
    result += content.slice(offset, start)
    offset = start + paramOpen.length
    let end = content.indexOf(paramClose, offset)
    if (end === -1) {
      return content
    }
    let paramName = content.slice(offset, end)
    let paramVal = param[paramName] || ''
    result += paramVal
    offset = end + paramClose.length
    start = content.indexOf(paramOpen, offset)
  }
  result += content.slice(offset, content.length)

  return result
}

const param = {
  nodeName: {
    name: i18n.global.t('contentParam.nodeName'),
    preVal: 'test/127.0.0.1:5432(MASTER)'
  },
  hostname: {
    name: i18n.global.t('contentParam.hostname'),
    preVal: 'localhost'
  },
  hostIp: {
    name: i18n.global.t('contentParam.hostIp'),
    preVal: '127.0.0.1'
  },
  port: {
    name: i18n.global.t('contentParam.port'),
    preVal: '5432'
  },
  alertTime: {
    name: i18n.global.t('contentParam.alertTime'),
    preVal: '2023-01-01 00:00:00'
  },
  alertStatus: {
    name: i18n.global.t('contentParam.alertStatus'),
    preVal: 'alerting'
  },
  level: {
    name: i18n.global.t('contentParam.level'),
    preVal: 'serious'
  },
  limitValue: {
    name: i18n.global.t('contentParam.limitValue'),
    preVal: '95%'
  },
  ruleName: {
    name: i18n.global.t('contentParam.ruleName'),
    preVal: 'CPUUsage'
  },
  duration: {
    name: i18n.global.t('contentParam.duration'),
    preVal: '1m'
  },
}

export function getAlertContentParam() {
  return Object.assign(param, {
    value: {
      name: i18n.global.t('contentParam.curVal'),
      preVal: '96'
    }
  })
}

export function getNotifyContentParam() {
  return Object.assign(param, {
    content: {
      name: i18n.global.t('contentParam.content'),
      preVal: 'The CPU usage rate is over 95%.'
    }
  })
}