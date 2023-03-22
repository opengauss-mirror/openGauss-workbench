import { defineStore } from 'pinia';
import { storePersist } from '@/config';

interface LastestConnectDatabase {
  rootId: string;
  name: string;
  uuid: string;
  connectTime: null | number;
}

export const useAppStore = defineStore({
  id: 'appState',
  state: () => ({
    currentConnectNode: {}, //current choose's node
    connectListMap: [],
    connectListState: [],
    isLoadEditor: false,
    language: 'zh-Cn',
    isReloadRouter: true,
    isMainViewMounted: false,
  }),
  getters: {
    //current choose's connectInfo
    currentConnectInfo(state: any) {
      return state.currentConnectNode.connectInfo;
    },
    connectedDatabase(state) {
      return state.connectListMap.reduce((prev, cur) => {
        return prev.concat(cur.connectedDatabase);
      }, []);
    },
    lastestConnectDatabase(): LastestConnectDatabase {
      return (
        this.connectedDatabase.sort((a, b) => b.connectTime - a.connectTime)[0] || {
          rootId: '',
          name: '',
          uuid: '',
          connectTime: null,
        }
      );
    },
  },
  actions: {
    updateCurrentNode(node) {
      this.currentConnectNode = node;
    },
    updataLoadEditor(value: boolean) {
      this.isLoadEditor = value;
    },
    setLanguage(value) {
      this.language = value;
      this.isReloadRouter = false;
      setTimeout(() => {
        this.isReloadRouter = true;
      }, 300);
    },
    updateAppMounted(value: boolean) {
      this.isMainViewMounted = value;
    },
  },
  persist: {
    key: storePersist.appState.key,
    storage: storePersist.appState.storage,
    paths: ['currentConnectNode', 'connectListMap', 'language'],
  },
});
