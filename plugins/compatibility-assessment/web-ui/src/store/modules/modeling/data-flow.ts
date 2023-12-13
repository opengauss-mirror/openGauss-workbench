import { getTableFields, getTableList } from '@/api/modeling'
import { defineStore } from 'pinia'
import { Message } from '@arco-design/web-vue'
import { KeyValue } from '@/api/modeling/request'
import { Graph } from '@antv/x6'

interface stateType {
  graph: any,
  dataFlowInfo: KeyValue,
  rawData: KeyValue,
  useData: KeyValue,
  useDatabase: string,
  useTable: string[],
  fieldsAlias: { database: string, table: string, name: string }[]
}
const defineState: stateType = {
  graph: null,
  dataFlowInfo: {},
  rawData: {},
  useData: {},
  useDatabase: ``,
  useTable: [],
  fieldsAlias: []
}

export const useDataFlowStore = defineStore(`dataFlow`, {
  state () {
    return JSON.parse(JSON.stringify(defineState))
  },
  getters: {
    getTableSelectList: (state: stateType) => {
      if (state.useDatabase) {
        return state.useData[state.useDatabase]
      } else return []
    },
    getFieldsSelectList: (state: stateType) => {
      if (state.useDatabase && state.useTable.length > 0) {
        const database = state.useData[state.useDatabase]
        const tableList: { group: string, fields: KeyValue[] }[] = []
        state.useTable.forEach((item: string) => {
          const arr = { group: item, fields: [] } as any
          const index = database.findIndex((item2: KeyValue) => item2.tablename === item)
          if (index !== -1) {
            arr.fields = database[index].fields
          }
          if (arr.fields && Array.isArray(arr.fields)) {
            state.fieldsAlias.forEach(item => {
              if (item.table === arr.group) {
                arr.fields.push({
                  name: item.name
                })
              }
            })
          }
          tableList.push(arr)
        })
        console.log(tableList)
        console.log(state.fieldsAlias)
        return tableList
      } else return []
    },
    getFlowDataInfo: (state: stateType) => state.dataFlowInfo,
    getFieldsByTable: (state: stateType) => (tableName: string | number) => {
      if (state.useDatabase) {
        const database = state.useData[state.useDatabase]
        if (tableName === 0 || tableName === '0') {
          const index = database.findIndex((item2: KeyValue) => item2.tablename === state.useTable[0])
          if (index === -1) return []
          else return database[index].fields
        } else {
          const index = database.findIndex((item2: KeyValue) => item2.tablename === tableName)
          if (index === -1) return []
          else return database[index].fields
        }
      } else return []
    },
    getUseTable: (state: stateType) => state.useTable,
    getUseDatabase: (state: stateType) => state.useDatabase
  },
  actions: {
    clearFieldsAlias () {
      this.$patch((state: stateType) => {
        state.fieldsAlias = []
      })
    },
    setFieldsAlias (field: { database: string, table: string, name: string, value: string }) {
      this.$patch((state: stateType) => {
        const i = state.fieldsAlias.findIndex(item => (item.database === field.database && item.table === field.table && item.name === field.value))
        if (i === -1) {
          state.fieldsAlias.push({
            database: field.database,
            table: field.table,
            name: field.value + '[as]'
          })
        }
      })
    },
    setGraph (graph: Graph) {
      this.$patch((state: stateType) => {
        state.graph = graph
      })
    },
    initState () {
      return new Promise((resolve) => {
        this.$patch((state: stateType) => {
          state.useData = []
          state.useDatabase = ``
          state.useTable = []
          state.rawData = []
          state.dataFlowInfo = []
          resolve(true)
        })
      })
    },
    clearUse () {
      return new Promise((resolve) => {
        this.$patch((state: stateType) => {
          state.useDatabase = ``
          state.useTable = []
          resolve(true)
        })
      })
    },
    setFlowDataInfo (info: KeyValue) {
      this.$patch((state: stateType) => state.dataFlowInfo = info)
    },
    setDatabaseInfo (dbName: string, clusterNodeId: string, schema: string) {
      return new Promise((resolve, reject) => {
        this.$patch((state: stateType) => {
          state.useDatabase = `${dbName}|,|${clusterNodeId}|,|${schema}`
          state.useTable = []
          if (!Object.keys(state.rawData).includes(`${dbName}|,|${clusterNodeId}|,|${schema}`)) {
            getTableList(dbName, clusterNodeId, schema).then((res: KeyValue) => {
              if (Number(res.code) === 200) {
                const arr = res.data.map((item: KeyValue) => ({ ...item, tables: {}}))
                state.rawData[`${dbName}|,|${clusterNodeId}|,|${schema}`] = arr
                state.useData[`${dbName}|,|${clusterNodeId}|,|${schema}`] = arr
                resolve(true)
              } else {
                Message.error(`database not found`)
                reject(true)
              }
            }).catch(() => {
              Message.error(`database not found`)
              reject(true)
            })
          } else {
            resolve(true)
          }
        })
      })
    },
    setDatabaseTableInfo (tableName: string, params?: KeyValue) {
      return new Promise(resolve => {
        this.$patch((state: stateType) => {
          if (tableName) {
            if (params && (params.tableKey || params.tableKey === 0)) state.useTable[params.tableKey] = tableName
            else {
              if (!(params && params.noUse)) state.useTable.push(tableName)
            }

            const index = state.rawData[state.useDatabase].findIndex((item: KeyValue) => item.tablename === tableName)
            if (!Object.keys(state.rawData[state.useDatabase][index].tables).includes(tableName)) {
              getTableFields(`${state.useDatabase.split('|,|').join('/')}/${tableName}`).then((res: KeyValue) => {
                resolve(true)
                state.rawData[state.useDatabase][index].fields = res.data
                state.useData[state.useDatabase][index].fields = res.data
              })
            } else resolve(false)
          } else resolve(false)
        })
      })
    },
    removeDatabaseTable (tableName: string) {
      return new Promise(resolve => {
        this.$patch((state: stateType) => {
          const index = state.useTable.indexOf(tableName)
          if (index !== -1) {
            state.useTable.splice(index, 1)
            resolve(true)
          } else resolve(false)
        })
      })
    }
  }
})
