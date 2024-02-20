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
