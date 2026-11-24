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

export const apiGetClusterAndDeviceManagerList = () => {
  return axios.get("/disasterCluster/listClusterAndDeviceManager");
};

export const apiInstallDtCluster = (data: any) => {
  return axios.post("/disasterCluster/install", data);
};

export const apiGetDtCluster = () => {
  return axios.get("/disasterCluster/listCluster");
};

export const apiSwitchover = (data: any) => {
  return axios.post("/disasterCluster/switchover", data);
};

export const apiRleaseDtCluster = (data: any) => {
  return axios.post("/disasterCluster/delete", data);
};

export const apiMonitorDtCluster = (clusterId: string, businessId: string) => {
  return axios.get(`/disasterCluster/monitor?clusterId=${clusterId}&businessId=${businessId}`);
};

export const apiGetHosts = (primaryClusterName: string, standbyClusterName: string) => {
  return axios.get(
    `/disasterCluster/getHosts?primaryClusterName=${primaryClusterName}&standbyClusterName=${standbyClusterName}`
  );
};

export const openSSH = (data: any) => {
  return axios.post(`opsCluster/ssh`, data);
};
