
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
              table: '', fields: [],
              rule: (data: KeyValue) => Boolean(data.table) && data.fields.filter((item: KeyValue) => !item.value).length === 0
            },
            ports: edge1.ports
          }, {
            width: 180, height: 35, shape: 'vue-shape',
            data: {
              type: 'BaseNode', cells_type: 'update', text: i18n.global.t('modeling.hooks.index.5m83asjg7f40'), configName: 'UpdateOperator', icon: 'modeling-gengxin',
              table: '', list: [],
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
              table: '', list1: [],
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
              or: [],
              rule: (data: KeyValue) => data.or.filter((orItem: KeyValue) => {
                return orItem.filter((andItem: KeyValue) => !(andItem.field && andItem.condition && andItem.value)).length !== 0
              }).length === 0
            },
            ports: edge1.ports
          },
          {
            width: 180, height: 35, shape: 'vue-shape',
            data: {
              type: 'BaseNode', cells_type: 'join', text: i18n.global.t('modeling.hooks.index.5m83asjg7zs0'), configName: 'BridgingOperator', icon: 'modeling-qiaojie',
              table: '', connectType: '', condition: [],
              rule: (data: KeyValue) => data.table && data.connectType && data.condition.filter((orItem: KeyValue) => {
                return orItem.filter((andItem: KeyValue) => !(andItem.field && andItem.condition && andItem.value)).length !== 0
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
              groups: []
            },
            ports: edge1.ports
          }, {
            width: 180, height: 35, shape: 'vue-shape',
            data: {
              type: 'BaseNode', cells_type: 'polymerization', text: i18n.global.t('modeling.hooks.index.5m83asjg8ak0'), icon: 'modeling-juhe',
              configName: 'PolymerizationOperator',
              polymerization: []
            },
            ports: edge1.ports
          }, {
            width: 180, height: 35, shape: 'vue-shape',
            data: {
              type: 'BaseNode', cells_type: 'mapping', text: i18n.global.t('modeling.hooks.index.5m83asjg8f40'), configName: 'MapOperator', icon: 'modeling-yingshe',
              mappings: [],
              rule: (data: KeyValue) => data.mappings.filter((item: KeyValue) => (!item.field || !item.condition || !item.value)).length === 0
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
