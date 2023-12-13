import localforage from 'localforage';
localforage.config({
  name: 'dataStudio',
});

const sidebarForage = localforage.createInstance({
  name: 'dataStudio',
  storeName: 'sidebar',
});

export { sidebarForage };
