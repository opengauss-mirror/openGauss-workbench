import request from './request';

export function getPrivilegeSqlApi(data) {
  return request({
    url: '/dataStudio/web/v1/privilege/sql',
    method: 'post',
    data,
  });
}

export function setPrivilegeApi(data) {
  return request({
    url: '/dataStudio/web/v1/privilege/set',
    method: 'post',
    data,
  });
}

export function getPrivilegehistoryApi(data) {
  return request({
    url: '/dataStudio/web/v1/privilege/history',
    method: 'get',
    params: data,
  });
}
