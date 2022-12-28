import { KeyValue } from '@/api/modeling/request'
import { Cell } from '@antv/x6'
export const checkDisabled = (checkArr: Array<KeyValue | number | string>, checkValue: number | string, comparedKey?: string) : boolean => {
  if (!checkArr || checkArr.length === 0) return false
  if (typeof checkArr[0] === 'string' || typeof checkArr[0] === 'number') {
    return checkArr.indexOf(checkValue) !== -1
  } else {
    const arr = checkArr as Array<KeyValue>
    return arr.findIndex((field: KeyValue) => field[comparedKey ? comparedKey : 'value'] === checkValue) !== -1
  }
}
/** When filling in the content, save it in the data of the node itself */
export const saveData = (key: string, value: any, cell: Cell) => {
  console.log(key, value, cell)
  if (cell) {
    const cellData = cell.getData()
    cell?.setData({ ...cellData, [key]: JSON.parse(JSON.stringify(value)) }, { overwrite: true })
  }
}
