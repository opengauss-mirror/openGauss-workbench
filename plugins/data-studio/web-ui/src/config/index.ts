export const PRIMARY_COLOR = '#E41D1D';

export const storePersist = {
  visitedViews: {
    key: 'DS_tagViewState_new',
    storage: window.globalThis.sessionStorage,
  },
  appState: {
    key: 'DS_appState',
    storage: localStorage,
  },
};

export const connectMenuPersist = {
  key: 'DS_ConnectMenu',
  storage: sessionStorage,
};
