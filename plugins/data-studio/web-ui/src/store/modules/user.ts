import { defineStore } from 'pinia';

export const useUserStore = defineStore({
  id: 'userState',
  state: () => ({
    userId: '',
  }),
  getters: {},
  actions: {},
  persist: {
    key: 'DS_userState',
    storage: localStorage,
  },
});
