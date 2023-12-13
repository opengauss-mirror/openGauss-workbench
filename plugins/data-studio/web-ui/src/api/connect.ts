import request from './request';
import requestSystem from './requestSystem';

export function getSystemUserProfile() {
  return requestSystem({
    url: '/system/user/profile',
    method: 'get',
  });
}

export function heartbeat() {
  return request({
    url: '/dataStudio/web/v1/functionality/heartbeat',
    method: 'get',
  });
}

export function createConnect(data) {
  return request({
    url: '/dataStudio/web/v1/connections',
    method: 'post',
    data,
  });
}

export function updateConnect(data) {
  return request({
    url: '/dataStudio/web/v1/connections',
    method: 'put',
    data,
  });
}

// get connection info
export function getDatabaseAttr(data) {
  return request({
    url: `/dataStudio/web/v1/connections/attribute`,
    method: 'get',
    params: data,
  });
}

// get connection list: platform
export const getAllCluster = async () => {
  return await request({
    url: `/dataStudio/web/v1/clusters`,
    method: 'get',
  });
};

// get connection list: ourself
export const getDataLinkList = async (webUser: string) => {
  return request({
    url: `/dataStudio/web/v1/connections?webUser=${webUser}`,
    method: 'get',
  });
};

// delete connection list: ourself
export const deleteDataLinkList = async (id: string) => {
  return request({
    url: `/dataStudio/web/v1/connections/${id}`,
    method: 'delete',
  });
};

export const closeConnections = async (data) => {
  return request({
    url: `/dataStudio/web/v1/connections/close`,
    method: 'delete',
    params: data,
  });
};

export const reconnection = async (data) => {
  return request({
    url: '/dataStudio/web/v1/reconnection',
    method: 'post',
    data,
  });
};

export const getConnectionTime = async (data) => {
  return request({
    url: '/dataStudio/web/v1/connection/getTime',
    method: 'get',
    params: data,
  });
};

// HTTP request for connection failure and no password
export const reLogin = async (data) => {
  return request({
    url: '/dataStudio/web/v1/login',
    method: 'post',
    data,
  });
};
