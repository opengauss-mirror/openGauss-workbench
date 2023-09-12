import { defineStore } from 'pinia';

import { userPersist } from '@/config';
export const useUserStore = defineStore({
  id: 'userState',
  state: () => ({
    userId: '',
  }),
  getters: {},
  actions: {},
  persist: {
    key: userPersist.key,
    storage: userPersist.storage,
  },
});
