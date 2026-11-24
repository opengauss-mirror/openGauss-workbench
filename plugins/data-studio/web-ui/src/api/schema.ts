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
import requestBlob from './requestBlob';

export function getSchemaUsers(data) {
  return request({
    url: `/dataStudio/web/v1/schema/query/users`,
    method: 'post',
    data,
  });
}

export function createSchema(data) {
  return request({
    url: `/dataStudio/web/v1/schema/create`,
    method: 'post',
    data,
  });
}

export function getSchema(data) {
  return request({
    url: `/dataStudio/web/v1/schema/query`,
    method: 'post',
    data,
  });
}

export function updateSchema(data) {
  return request({
    url: `/dataStudio/web/v1/schema/update`,
    method: 'post',
    data,
  });
}

export function deleteSchema(data) {
  return request({
    url: `/dataStudio/web/v1/schema/delete`,
    method: 'post',
    data,
  });
}

// export ddl of schema
export function exportSchemaDdl(data) {
  return requestBlob({
    url: `/dataStudio/web/v1/export/schema/ddl`,
    method: 'post',
    data,
  });
}
