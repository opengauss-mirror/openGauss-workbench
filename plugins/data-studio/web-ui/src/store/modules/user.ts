import { defineStore } from 'pinia';

import { userPersist } from '@/config';
export const useUserStore = defineStore({
  id: 'userState',
  state: () => ({
    userId: '',
    globalPageSize: 500,
  }),
  getters: {},
  actions: {},
  persist: {
    key: userPersist.key,
    storage: userPersist.storage,
  },
});
