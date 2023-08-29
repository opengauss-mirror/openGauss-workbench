import { KeyValue } from '@/types/global'
import i18n from '@/locale/index'
export const indicatorType = [
  { value: 'sum', label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1cb00') },
  { value: 'avg', label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1d3k0') },
  { value: 'max', label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1d7c0') },
  { value: 'min', label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1d9s0') },
  { value: 'mid', label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1dcg0') }
]
export const indicatorTypeSum = [
  { value: 'sum', label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1cb00') }
]
export const chartTypes = [
  { value: 'bar', label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1df40') },
  { value: 'line', label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1dhs0') },
  { value: 'mix', label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1dk40') },
  { value: 'pie', label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1dmk0') },
  { value: 'heatmap', label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1dqg0') },
  { value: 'scatter', label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1dso0') },
  { value: 'graph', label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1dvc0') }
]
let queryData: KeyValue[] = []
let cacheQueryData: any = []
let cacheQueryDataKeys: string[] = []
export const getQueryDataByKey = (key: string, page: number, size: number) => {
  let pageSize = 10
  if (size) pageSize = size
  let arr = []
  if (cacheQueryDataKeys.includes(key)) {
    arr = JSON.parse(JSON.stringify(cacheQueryData[key]))
  } else {
    arr = Array.from(new Set(queryData.map((item: KeyValue) => item[key])))
            .filter((item: KeyValue) => item && item.trim())
            .map((item: KeyValue) => ({ label: item, value: item }))
    cacheQueryData[key] = JSON.parse(JSON.stringify(arr))
    cacheQueryDataKeys.push(key)
  }
  return arr.splice((page - 1) * pageSize, pageSize)
}
export const setQueryData = (data: KeyValue[]) => {
  if (data && Array.isArray(data)) {
    queryData = data
    cacheQueryData = []
    cacheQueryDataKeys = []
  }
}
export const compareWays = [
  { value: 1, label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1dxs0') },
  { value: 2, label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1e080') }
]
export const particles = [
  { value: 1, label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1e2g0') },
  { value: 2, label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1e500') },
  { value: 3, label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1e7g0') },
  { value: 4, label: i18n.global.t('modeling.hooks.visual-edit.5m83coh1e9o0') }
]
