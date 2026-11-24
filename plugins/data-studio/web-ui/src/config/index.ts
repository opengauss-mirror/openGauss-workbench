/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

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

export const hasConnectListPersist = {
  key: 'DS_hasConnectList',
  storage: sessionStorage,
};

export const loadVersionTimePersist = {
  key: 'DS_version',
  storage: sessionStorage,
};

export const prevTokenPersist = {
  key: 'DS_prevToken',
  storage: localStorage,
};

export const userPersist = {
  key: 'DS_userState',
  storage: localStorage,
};

export const wsHeartbeatTime = 1000 * 60 * 5;
export const httpHeartbeatTime = 1000 * 60 * 5;
