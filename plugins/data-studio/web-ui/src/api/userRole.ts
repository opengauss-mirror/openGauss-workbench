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

export const getResource = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/metaData/resource`,
    method: 'get',
    params: data,
  });
};

export const getUserRoleList = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/metaData/user`,
    method: 'get',
    params: data,
  });
};

export const previewUserRole = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/user/create/preview`,
    method: 'post',
    data,
  });
};

export const createUserRole = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/user/create`,
    method: 'post',
    data,
  });
};

export const dropUser = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/user/drop`,
    method: 'delete',
    data,
  });
};

export const getUserRoleInfo = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/user/attribute`,
    method: 'get',
    params: data,
  });
};

export const getUserRoleDdl = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/user/ddl`,
    method: 'get',
    params: data,
  });
};

export const updateUserRoleInfo = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/user/attribute/update`,
    method: 'post',
    data,
  });
};

export const updateUserPassword = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/user/password/update`,
    method: 'post',
    data,
  });
};
