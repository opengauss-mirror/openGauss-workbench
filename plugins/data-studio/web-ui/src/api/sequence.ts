import request from './request';

// getColumnList
export function getColumnList(data) {
  return request({
    url: '/dataStudio/web/v1/metaData/columnList',
    method: 'get',
    params: data,
  });
}

// createSequence
export function createSequence(data) {
  return request({
    url: '/dataStudio/web/v1/sequences',
    method: 'post',
    data,
  });
}

// createSequenceDdl
export function createSequenceDdl(data) {
  return request({
    url: '/dataStudio/web/v1/sequences/action?action=createSequenceDdl',
    method: 'post',
    data,
  });
}

// dropSequence
export function dropSequence(data) {
  return request({
    url: '/dataStudio/web/v1/sequences',
    method: 'delete',
    data,
  });
}

// getSequenceDdls
export function getSequenceDdls(data) {
  return request({
    url: '/dataStudio/web/v1/sequenceDdls',
    method: 'post',
    data,
  });
}
