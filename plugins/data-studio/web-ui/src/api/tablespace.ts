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

import request from './request';

export const getTablespaceListApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/metaData/tablespace/list',
    method: 'get',
    params: data,
  });
};

export const createTablespaceApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/tablespace/create',
    method: 'post',
    data,
  });
};

export const getTablespacePreviewDdlApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/tablespace/create/ddl',
    method: 'post',
    data,
  });
};

export const dropTablespaceApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/tablespace/drop',
    method: 'delete',
    data,
  });
};

export const getTablespaceAttributeApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/tablespace/attribute',
    method: 'get',
    params: data,
  });
};

export const updateTablespaceApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/tablespace/update',
    method: 'post',
    data,
  });
};

export const updateTablespacePreviewDdlApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/tablespace/update/ddl',
    method: 'post',
    data,
  });
};
