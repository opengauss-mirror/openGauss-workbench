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

export const createJobApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/job/create',
    method: 'post',
    data,
  });
};

export const queryAJobApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/job/query',
    method: 'post',
    data,
  });
};

export const queryJobListApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/job/queryList',
    method: 'post',
    data,
  });
};

export const editJobApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/job/edit',
    method: 'post',
    data,
  });
};

export const deleteJobApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/job/delete',
    method: 'post',
    data,
  });
};

export const setEnableJobApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/job/enable',
    method: 'post',
    data,
  });
};

export const setDisableJobApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/job/disable',
    method: 'post',
    data,
  });
};
