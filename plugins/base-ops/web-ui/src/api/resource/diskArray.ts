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

import axios from "axios";

export const apiGetDiskArrayList = () => {
  return axios.get("/deviceManager/list");
};

export const apiAddDiskArray = (data: any) => {
  return axios.post("/deviceManager/add", data);
};

export const apiModifyDiskArray = (data: any) => {
  return axios.post("/deviceManager/modify", data);
};

export const apiDeleteDiskArray = (id: string) => {
  return axios.post(`/deviceManager/delete/${id}`);
};

export const apiCheckConnectivity = (data: any) => {
  return axios.post("/deviceManager/connect", data);
};

export const apiCheckName = (data: any) => {
  return axios.get(`/deviceManager/hasName?name=${data}`);
};
