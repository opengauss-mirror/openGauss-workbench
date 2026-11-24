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

export const getServerListApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/foreignTable/query/server',
    method: 'post',
    data,
  });
};

export const createServerListApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/foreignTable/createServer',
    method: 'post',
    data,
  });
};

export const testForeignTableApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/foreignTable/test',
    method: 'post',
    data,
  });
};

export const deleteForeignServerApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/foreignServer/delete',
    method: 'post',
    data,
  });
};

export const createForeignTableApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/foreignTable/create',
    method: 'post',
    data,
  });
};

export const dropForeignTableApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/foreignTable/delete',
    method: 'post',
    data,
  });
};

export const getForeignTableDdlApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/foreignTable/ddl',
    method: 'post',
    data,
  });
};

export const getForeignTableAttributeApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/foreignTable/attribute',
    method: 'post',
    data,
  });
};

export const updateForeignTableApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/foreignTable/edit',
    method: 'post',
    data,
  });
};
