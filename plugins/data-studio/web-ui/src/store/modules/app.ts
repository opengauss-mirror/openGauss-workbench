import { defineStore } from 'pinia';
import { storePersist } from '@/config';

export const useAppStore = defineStore({
  id: 'appState',
  state: () => ({
    currentConnectNode: {}, //current choose's node
    lastestConnectDatabase: {
      name: '',
      uuid: '',
    },
    connectListMap: {},
    isLoadEditor: false,
    language: 'zh-Cn',
    isReloadRouter: true,
    isMainViewMounted: false,
  }),
  getters: {
    //current choose's connectInfo
    currentConnectInfo(state) {
      return state.currentConnectNode.connectInfo;
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
    paths: ['currentConnectNode', 'lastestConnectDatabase', 'connectListMap', 'language'],
  },
});
