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

// dropFunctionSP
export function dropFunctionSP(data) {
  return request({
    url: '/dataStudio/web/v1/functionSP',
    method: 'delete',
    data,
  });
}

export function dropPackage(data) {
  return requestBlob({
    url: '/dataStudio/web/v1/drop/package',
    method: 'delete',
    data,
  });
}

export function getCoverageRateList(data) {
  return request({
    url: '/dataStudio/web/v1/coverageRate/query',
    method: 'post',
    data,
  });
}

export function deleteCoverageRate(data) {
  return request({
    url: '/dataStudio/web/v1/coverageRate/delete',
    method: 'post',
    data,
  });
}

export function exportCoverageRate(data) {
  return requestBlob({
    url: '/dataStudio/web/v1/coverageRate/export',
    method: 'post',
    data,
  });
}

// export ddl of function
export function exportFunctionDdl(data) {
  return requestBlob({
    url: '/dataStudio/web/v1/export/function/ddl',
    method: 'post',
    data,
  });
}
