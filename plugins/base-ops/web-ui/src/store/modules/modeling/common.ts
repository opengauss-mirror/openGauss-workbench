import { KeyValue } from '@/api/modeling/request'
import { defineStore } from 'pinia'

export const useModelCommonStore = defineStore(`modeling-common`, {
  state: () => {
    return {
      currentSelectData: {},
      selectNode: null as KeyValue | null,
      showConfig: false,
      isRegisterNodes: [] as Array<string>,
      rules: {} as KeyValue,
      nodeEventData: null as any,
      i18n: null as any
    }
  },
  getters: {
    getI18n: state => state.i18n,
    getCurrentSelectData: state => state.currentSelectData,
    getSelectNode: state => state.selectNode,
    getRules: state => state.rules
  },
  actions: {
    setI18n (i18n: any) {
      this.$patch(state => {
        state.i18n = i18n
      })
    },
    // nodeEvent
    nodeEvent (data: any) {
      this.nodeEventData = data
    },
    // add register node
    setIsResigterNodes (node: string) {
      this.$patch(state => {
        state.isRegisterNodes.push(node)
      })
    },
    // modify seleced node
    setSelectNode (nodeInfo: KeyValue | null, showConfig: boolean) {
      this.$patch(state => {
        state.showConfig = showConfig
        state.selectNode = nodeInfo
      })
    },
    // 
    setSelectData () {
      const data = [
        { id: 5, name: 'hs.article', fields: [
          { id: 1, name: 'article.type' },
          { id: 2, name: 'article.title' },
          { id: 3, name: 'article.content' }
        ] },
        { id: 1, name: 'hs.user_test', fields: [
          { id: 1, name: 'user.name' },
          { id: 2, name: 'user.sex' },
          { id: 3, name: 'user.age' }
        ] }
      ]
      this.currentSelectData = data
    },
    // 
    setRule (key: string, rule: any) {
      this.$patch(state => {
        state.rules[key] = rule
      })
    }
  }
})
