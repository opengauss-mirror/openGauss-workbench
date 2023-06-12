import request from './request';
import requestBlob from './requestBlob';

// createView
export function createView(data) {
  return request({
    url: '/dataStudio/web/v1/views',
    method: 'post',
    data,
  });
}

// createViewDdl
export function createViewDdl(data) {
  return request({
    url: '/dataStudio/web/v1/views/action?action=createViewDdl',
    method: 'post',
    data,
  });
}

// dropView
export function dropView(data) {
  return request({
    url: '/dataStudio/web/v1/views',
    method: 'delete',
    data,
  });
}

// getViewDdls
export function getViewDdls(data) {
  return request({
    url: '/dataStudio/web/v1/viewDdls',
    method: 'post',
    data,
  });
}

// getViewDatas
export function getViewDatas(data) {
  return request({
    url: '/dataStudio/web/v1/viewDatas',
    method: 'get',
    params: data,
  });
}

// export ddl of view
export function exportViewDdl(data) {
  return requestBlob({
    url: '/dataStudio/web/v1/export/view/ddl',
    method: 'post',
    data,
  });
}
