import { defineStore } from 'pinia';
import { storePersist } from '@/config';

export const useAppStore = defineStore({
  id: 'appState',
  state: () => ({
    currentDBNode: {}, //current choose's node
    isLoadEditor: false,
    language: 'zh-Cn',
    isReloadRouter: true,
  }),
  getters: {
    //current choose's connectInfo
    currentConnectInfo(state) {
      return state.currentDBNode.connectInfo;
    },
  },
  actions: {
    updateCurrentNode(node) {
      this.currentDBNode = node;
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
  },
  persist: {
    key: storePersist.appState.key,
    storage: storePersist.appState.storage,
    paths: ['language'],
  },
});
