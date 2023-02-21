export const PRIMARY_COLOR = '#E41D1D';

export const storePersist = {
  visitedViews: {
    key: 'DS_tagViewState',
    storage: sessionStorage,
  },
  appState: {
    key: 'DS_appState',
    storage: sessionStorage,
  },
};

export const connectListPersist = {
  key: 'DS_connectList',
  storage: sessionStorage,
};
