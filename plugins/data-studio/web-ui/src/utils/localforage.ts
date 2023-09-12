import localforage from 'localforage';
// import { connectListPersist } from '@/config';
localforage.config({
  name: 'dataStudio',
});

const sidebarForage = localforage.createInstance({
  name: 'dataStudio',
  storeName: 'sidebar',
});

export { sidebarForage };
