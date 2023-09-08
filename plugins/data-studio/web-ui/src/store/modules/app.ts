import { defineStore } from 'pinia';
import { storePersist } from '@/config';

interface LastestConnectDatabase {
  rootId: string;
  connectInfoName: string;
  name: string;
  uuid: string;
  connectTime: null | number;
}
interface CurrentTerminalInfo {
  label?: string;
  rootId: string;
  connectInfoName: string;
  dbname: string;
  uuid: string;
}

export const useAppStore = defineStore({
  id: 'appState',
  state: () => ({
    // currentConnectNode: {}, //current choose's node
    connectListMap: [],
    currentTerminalInfo: {} as CurrentTerminalInfo, // the info of open new terminal
    isLoadEditor: false,
    language: 'zh-CN',
    isReloadRouter: true,
    isMainViewMounted: false,
    isOpenSqlAssistant: false,
  }),
  getters: {
    //current choose's connectInfo
    // currentConnectInfo(state: any) {
    //   return state.currentConnectNode.connectInfo;
    // },
    connectedDatabase(state) {
      return state.connectListMap.reduce((prev, cur) => {
        return prev.concat(cur.connectedDatabase);
      }, []);
    },
    lastestConnectDatabase(): LastestConnectDatabase {
      return this.connectedDatabase.sort((a, b) => b.connectTime - a.connectTime)[0] || {};
    },
  },
  actions: {
    // updateCurrentNode(node) {
    //   this.currentConnectNode = node;
    // },
    updataLoadEditor(value: boolean) {
      this.isLoadEditor = value;
    },
    updateCurrentTerminalInfo() {
      const availDbConnectCount = this.connectedDatabase.length;
      if (availDbConnectCount) {
        if (
          this.connectedDatabase.findIndex(
            (item) => item.uuid === this.currentTerminalInfo.uuid,
          ) === -1
        ) {
          this.currentTerminalInfo = {
            rootId: this.lastestConnectDatabase.rootId,
            connectInfoName: this.lastestConnectDatabase.connectInfoName,
            dbname: this.lastestConnectDatabase.name,
            uuid: this.lastestConnectDatabase.uuid,
          };
        }
      } else {
        this.currentTerminalInfo = {};
      }
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
    // paths: ['currentConnectNode', 'connectListMap', 'language'],
    paths: ['connectListMap', 'language'],
  },
});
