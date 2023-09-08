import request from './request';

export function querySqlHistory(data) {
  return request({
    url: '/dataStudio/web/v1/sqlHistory/query',
    method: 'post',
    data,
  });
}

export function updateSqlHistory(data) {
  return request({
    url: '/dataStudio/web/v1/sqlHistory/update',
    method: 'post',
    data,
  });
}

export function deleteSqlHistory(data) {
  return request({
    url: '/dataStudio/web/v1/sqlHistory/delete',
    method: 'post',
    data,
  });
}
