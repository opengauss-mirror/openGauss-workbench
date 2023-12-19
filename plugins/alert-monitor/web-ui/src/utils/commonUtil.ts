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
  }
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