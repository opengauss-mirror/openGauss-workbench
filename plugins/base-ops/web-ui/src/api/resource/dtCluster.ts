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
