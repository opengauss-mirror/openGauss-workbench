
import { useModelCommonStore } from '@/store/modules/modeling/common'
import { Cell, Graph } from '@antv/x6'
import { KeyValue } from '@antv/x6/lib/types'
import { Notification } from '@arco-design/web-vue'
import i18n from '@/locale/index'
const mCStore = useModelCommonStore()

const noticeArr: string[] = []
export const checkData = (data: any): boolean => {
  let check = true
  let message = ``
  if (data && data.cells && Array.isArray(data.cells) && data.cells.length > 0) {
    data.cells.forEach((item: Cell) => {
      if (item.shape !== 'edge') {
        const storeSaveRule = mCStore.getRules[item.data.cells_type]
        if (item.data) {
          if (item.data.rule && typeof item.data.rule === 'function') {
            if (!item.data.rule(item.data)) {
              check = false
              message += `[${item.data.text}]` + i18n.global.t('modeling.utils.index.5m78yfizcxc0')
            }
          } else if (storeSaveRule && typeof storeSaveRule === 'function') {
            if (!storeSaveRule(item.data)) {
              check = false
              message += `[${item.data.text}]` + i18n.global.t('modeling.utils.index.5m78yfizcxc0')
            }
          }
        }
      }
    })
  }

  if (!check && message) {
    if (noticeArr.includes(message)) return check
    let isDelete = false
    Notification.error({
      position: 'bottomRight',
      content: message,
      closable: true,
      duration: 10 * 1000,
      onClose: () => {
        const index = noticeArr.indexOf(message)
        if (index != -1) noticeArr.splice(index, 1)
        isDelete = true
      }
    })
    noticeArr.push(message)
    setTimeout(() => {
      if (!isDelete) {
        const index = noticeArr.indexOf(message)
        if (index != -1) noticeArr.splice(index, 1)
      }
    }, 10 * 1000)
  }

  return check
}
export const jsonFormat = (graph: Graph) => {
  if (graph) {
    const jsonData: { cells: Array<KeyValue> } = graph.toJSON()
    jsonData.cells.forEach((item: KeyValue) => {
      if (item.data && item.data.cells_type) item.cells_type = item.data.cells_type
      else if (item.shape === 'edge') item.cells_type = 'line'
    })
    return jsonData
  } else return ''
}
