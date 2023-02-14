import request from './request';

// heartbeat
export function heartbeat() {
  return request({
    url: '/dataStudio/web/v1/functionality/heartbeat',
    method: 'get',
  });
}

// createConnect
export function createConnect(data) {
  return request({
    url: '/dataStudio/web/v1/connections',
    method: 'post',
    data,
  });
}

// updateConnect
export function updateConnect(data) {
  return request({
    url: '/dataStudio/web/v1/connections',
    method: 'put',
    data,
  });
}

// get connection info
export function getDatabaseAttr(id) {
  return request({
    url: `/dataStudio/web/v1/connections/${id}/attribute`,
    method: 'get',
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
