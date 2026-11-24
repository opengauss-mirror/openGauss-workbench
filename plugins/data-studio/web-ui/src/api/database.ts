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

// getDatabaseList
export function getDatabaseList(uuid) {
  return request({
    url: `/dataStudio/web/v1/metaData/databaseList`,
    method: 'get',
    params: { uuid },
  });
}

// create database
export function createDatabase(data) {
  return request({
    url: '/dataStudio/web/v1/database/create',
    method: 'post',
    data,
  });
}

// rename database
export function renameDatabase(data) {
  return request({
    url: '/dataStudio/web/v1/database/rename',
    method: 'post',
    data,
  });
}

// update database
export function updateDatabase(data) {
  return request({
    url: '/dataStudio/web/v1/database/attribute/update',
    method: 'get',
    params: data,
  });
}

// delete database
export function deleteDatabase(data) {
  return request({
    url: '/dataStudio/web/v1/database/delete',
    method: 'delete',
    data,
  });
}

// get database attribute
export function getDatabaseAttribute(data) {
  return request({
    url: '/dataStudio/web/v1/database/attribute',
    method: 'get',
    params: data,
  });
}

// open database connection
export function openDatabaseConnection(data) {
  return request({
    url: '/dataStudio/web/v1/database/connection',
    method: 'post',
    data,
  });
}
