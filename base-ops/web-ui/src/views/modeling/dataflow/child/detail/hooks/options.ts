
import { KeyValue } from '@antv/x6/lib/types'
import { PropsOptions } from '../types'
import { edge1 } from '../utils/edgeConfig'
import i18n from '@/locale/index'
export const options = () => ({
  containerId: 'database_container_id',
  stencil: {
    nodes: [
      {
        name: i18n.global.t('modeling.hooks.index.5m83asj8uws0'), icon: 'icon-layers', child: [
          {
            width: 180, height: 35, shape: 'vue-shape',
            data: {
              type: 'BaseNode', cells_type: 'dataSource', text: i18n.global.t('modeling.hooks.index.5m83asj8vok0'), configName: 'DataSource', icon: 'modeling-dataSource',
              source: '',
              rule: () => true
            },
            ports: edge1.ports
          }
        ]
      },
      {
        name: i18n.global.t('modeling.hooks.index.5m83asj8vsw0'), icon: 'icon-common', child: [
          {
            width: 180, height: 35, shape: 'vue-shape',
            data: {
              type: 'BaseNode', cells_type: 'query', text: i18n.global.t('modeling.hooks.index.5m83asjg7480'), configName: 'QueryOperator', icon: 'modeling-chaxun',
              table: '', fields: [{field:undefined}],
              rule: (data: KeyValue) => Boolean(data.table) && data.fields.filter((item: KeyValue) => !item.value).length === 0
            },
            ports: edge1.ports
          }, {
            width: 180, height: 35, shape: 'vue-shape',
            data: {
              type: 'BaseNode', cells_type: 'update', text: i18n.global.t('modeling.hooks.index.5m83asjg7f40'), configName: 'UpdateOperator', icon: 'modeling-gengxin',
              table: '', list: [{ field: undefined, valueType: 'customSql', value: '', fieldType: 'originalValue', splitValue: undefined }],
              rule: (data: KeyValue) => {
                let flag = true
                if (!data.table) flag = false
                data.list.forEach((item: { field: string, value: string, valueType: string, fieldType: string, splitValue: string }) => {
                  if (!item.field || !item.value) flag = false
                  else if (item.valueType === i18n.global.t('modeling.hooks.index.5m83asjg7j80') && item.fieldType === i18n.global.t('modeling.hooks.index.5m83asjg7m80') && !item.splitValue) flag = false
                })
                return flag
              }
            },
            ports: edge1.ports
          }, {
            width: 180, height: 35, shape: 'vue-shape',
            data: {
              type: 'BaseNode', cells_type: 'delete', text: i18n.global.t('modeling.hooks.index.5m83asjg7oo0'), configName: 'DeleteOperator', icon: 'modeling-shanchu',
              table: '', fields: [],
              rule: (data: KeyValue) => Boolean(data.table) && data.fields.filter((item: KeyValue) => !item).length === 0
            },
            ports: edge1.ports
          }, {
            width: 180, height: 35, shape: 'vue-shape',
            data: {
              type: 'BaseNode', cells_type: 'insert', text: i18n.global.t('modeling.hooks.index.5m83asjg7rk0'), configName: 'InsertOperator', icon: 'modeling-charu',
              table: '', list1: [[{ field: '', value: '' }]],
              rule: (data: KeyValue) => {
                return Boolean(data.table) &&
                data.list1.filter((item: any) => {
                  return (item.filter((item2: KeyValue) => {
                    return !item2.field || !item2.value
                  }).length !== 0)
                }).length === 0
              }
            },
            ports: edge1.ports
          }
        ]
      },
      {
        name: i18n.global.t('modeling.hooks.index.5m83asjg7u80'), icon: 'icon-apps', child: [
          {
            width: 180, height: 35, shape: 'vue-shape',
            data: {
              type: 'BaseNode', cells_type: 'condition', text: i18n.global.t('modeling.hooks.index.5m83asjg7x40'), configName: 'ConditionOperator', icon: 'modeling-tiaojian',
              or: [[{ field: null, condition: null, value: '' }]],
              rule: (data: KeyValue) => data.or.filter((orItem: KeyValue) => {
                return orItem.filter((andItem: KeyValue) => !(andItem.field && (
                    (andItem.condition === 'isNull' || andItem.condition === 'notNull') ? true : (andItem.value ? true : false)
                  ))).length !== 0
                }).length === 0
            },
            ports: edge1.ports
          },
          {
            width: 180, height: 35, shape: 'vue-shape',
            data: {
              type: 'BaseNode', cells_type: 'join', text: i18n.global.t('modeling.hooks.index.5m83asjg7zs0'), configName: 'BridgingOperator', icon: 'modeling-qiaojie',
              table: '', connectType: '', condition: [[{ field: null, condition: null, value: '', valueType: 'string' }]],
              rule: (data: KeyValue) => data.table && data.connectType && data.condition.filter((orItem: KeyValue) => {
                return orItem.filter((andItem: KeyValue) => !(andItem.field && (
                  (andItem.condition === 'isNull' || andItem.condition === 'notNull') ? true : (andItem.value ? true : false)
                ))).length !== 0
              }).length === 0
            },
            ports: edge1.ports
          },
          {
            width: 180, height: 35, shape: 'vue-shape',
            data: {
              type: 'BaseNode', cells_type: 'sort', text: i18n.global.t('modeling.hooks.index.5m83asjg8280'), configName: 'SortOperator', icon: 'modeling-paixu',
              sorts: [],
              rule: (data: KeyValue) => data.sorts.filter((item: KeyValue) => (!item.field || !item.value)).length === 0
            },
            ports: edge1.ports
          }, {
            width: 180, height: 35, shape: 'vue-shape',
            data: {
              type: 'BaseNode', cells_type: 'limit', text: i18n.global.t('modeling.hooks.index.5m83asjg84w0'), configName: 'RestrictionOperator', icon: 'modeling-xiantiao',
              restriction: { skip: null, limitCount: null }
            },
            ports: edge1.ports
          },
          {
            width: 180, height: 35, shape: 'vue-shape',
            data: {
              type: 'BaseNode', cells_type: 'group', text: i18n.global.t('modeling.hooks.index.5m83asjg87c0'), configName: 'GroupOperator', icon: 'modeling-fenzu',
              groups: [{ fields: null }]
            },
            ports: edge1.ports
          }, {
            width: 180, height: 35, shape: 'vue-shape',
            data: {
              type: 'BaseNode', cells_type: 'polymerization', text: i18n.global.t('modeling.hooks.index.5m83asjg8ak0'), icon: 'modeling-juhe',
              configName: 'PolymerizationOperator',
              polymerization: [{ field: null, way: null, alias: '' }],
              rule: (data: KeyValue) =>
              data.polymerization.filter((item: KeyValue, key: number) => (
                !item.field || !item.way || !item.alias
                || data.polymerization.findIndex((item2: KeyValue, key2: number) => (key !== key2 && item.alias === item2.alias)) !== -1
              )).length === 0
            },
            ports: edge1.ports
          }, {
            width: 180, height: 35, shape: 'vue-shape',
            data: {
              type: 'BaseNode', cells_type: 'mapping', text: i18n.global.t('modeling.hooks.index.5m83asjg8f40'), configName: 'MapOperator', icon: 'modeling-yingshe',
              mappings: [{ field: null, condition: null, value: '' }],
              rule: (data: KeyValue) =>
                data.mappings.filter((item: KeyValue, key: number) => (
                  !item.field || !item.condition || !item.value
                  || data.mappings.findIndex((item2: KeyValue, key2: number) => (key !== key2 && item.value === item2.value)) !== -1
                )).length === 0
            },
            ports: edge1.ports
          }, {
            width: 180, height: 35, shape: 'vue-shape',
            data: {
              type: 'BaseNode', cells_type: 'dictionary', text: i18n.global.t('modeling.hooks.index.5m83asjg8i00'), configName: 'DictionaryOperator', icon: 'modeling-zidian',
              table: '', field: '', matchField: '', rigidField: '',
              rule: (data: KeyValue) => data.table && data.field && data.matchField && data.rigidField
            },
            ports: edge1.ports
          }
        ]
      }
    ]
  },
  canvasOption: {
    selecting: { multiple: true, enabled: true, rubberband: true, movable: true }
  }
} as PropsOptions)
