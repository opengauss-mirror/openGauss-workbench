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

export function createSqlCodeApi(data) {
  return request({
    url: '/dataStudio/web/v1/sqlCode/create',
    method: 'post',
    data,
  });
}

export function getSqlCodeListApi(data) {
  return request({
    url: '/dataStudio/web/v1/sqlCode/list',
    method: 'get',
    params: data,
  });
}

export function getSqlCodeApi(data) {
  return request({
    url: '/dataStudio/web/v1/sqlCode/get',
    method: 'get',
    params: data,
  });
}

export function updateSqlCodeApi(data) {
  return request({
    url: '/dataStudio/web/v1/sqlCode/update',
    method: 'post',
    data,
  });
}

export function deleteSqlCodeApi(data) {
  return request({
    url: '/dataStudio/web/v1/sqlCode/delete',
    method: 'delete',
    params: data,
  });
}
