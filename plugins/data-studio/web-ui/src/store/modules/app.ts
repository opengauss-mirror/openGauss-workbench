import { defineStore } from 'pinia';
import { storePersist } from '@/config';

/* interface ConnectListMap {
  id: string;
  connectInfo: {
    dataName: string;
    id: string;
    name: string;
    ip: string;
    port: string;
    type: string;
    userName: string;
    isRememberPassword: 'y' | 'n';
    [prop: string]: any;
  };
  connectedDatabase: {
    connectInfoName: string;
    isConnect: boolean;
    connectTime: number;
    name: string;
    rootId: string;
    uuid: string;
  }[];
} */

export interface ConnectedDatabase {
  connectInfoName: string;
  connectTime: number;
  isConnect: boolean;
  name: string;
  rootId: string;
  uuid: string;
}
export interface LastestConnectDatabase {
  rootId: string;
  connectInfoName: string;
  name: string;
  uuid: string;
  connectTime: null | number;
}
export interface CurrentTerminalInfo {
  label?: string;
  rootId: string;
  connectInfoName: string;
  dbname: string;
  uuid: string;
}

export const useAppStore = defineStore({
  id: 'appState',
  state: () => ({
    connectListMap: [],
    historyConnectedDatabase: [] as ConnectedDatabase[], // Include existing connections and disconnected connections
    currentTerminalInfo: {} as CurrentTerminalInfo, // the info of open new terminal
    isLoadEditor: false,
    language: 'zh-CN',
    isReloadRouter: true,
    isMainViewMounted: false,
    isOpenSqlAssistant: false,
  }),
  getters: {
    connectedDatabase(state): ConnectedDatabase[] {
      return state.connectListMap.reduce((prev, cur) => {
        return prev.concat(cur.connectedDatabase.filter((item) => item.isConnect));
      }, []);
    },
    lastestConnectDatabase(): LastestConnectDatabase {
      return this.connectedDatabase.sort((a, b) => b.connectTime - a.connectTime)[0] || {};
    },
  },
  actions: {
    getConnectionOneAvailableUuid(rootId) {
      const selectConnection = this.connectListMap.find((listItem) => listItem.id === rootId);
      return selectConnection?.connectedDatabase[0]?.uuid;
    },
    getConnectInfoByRootId(rootId) {
      const selectConnection = this.connectListMap.find((listItem) => listItem.id === rootId);
      return selectConnection?.connectInfo;
    },
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
    updateHistoryConnectedDatabase() {
      this.historyConnectedDatabase = this.connectListMap.reduce((prev, cur) => {
        return prev.concat(cur.connectedDatabase);
      }, []);
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
    paths: ['connectListMap', 'historyConnectedDatabase', 'language'],
  },
});
