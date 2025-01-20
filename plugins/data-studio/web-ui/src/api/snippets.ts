import request from './request';

export function createSqlCodeApi(data) {
  return request({
    url: '/dataStudio/web/v1/sqlCode/create',
    method: 'post',
    data,
  });
}

export function getSqlCodeListApi(data) {
  return request({
    url: '/dataStudio/web/v1/sqlCode/list',
    method: 'get',
    params: data,
  });
}

export function getSqlCodeApi(data) {
  return request({
    url: '/dataStudio/web/v1/sqlCode/get',
    method: 'get',
    params: data,
  });
}

export function updateSqlCodeApi(data) {
  return request({
    url: '/dataStudio/web/v1/sqlCode/update',
    method: 'post',
    data,
  });
}

export function deleteSqlCodeApi(data) {
  return request({
    url: '/dataStudio/web/v1/sqlCode/delete',
    method: 'delete',
    params: data,
  });
}
