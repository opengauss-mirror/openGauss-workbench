import request from './request';
import requestBlob from './requestBlob';

export function createView(data) {
  return request({
    url: '/dataStudio/web/v1/views',
    method: 'post',
    data,
  });
}

export function createViewDdl(data) {
  return request({
    url: '/dataStudio/web/v1/views/action?action=createViewDdl',
    method: 'post',
    data,
  });
}

export function dropView(data) {
  return request({
    url: '/dataStudio/web/v1/views',
    method: 'delete',
    data,
  });
}

export function getViewDdls(data) {
  return request({
    url: '/dataStudio/web/v1/viewDdls',
    method: 'post',
    data,
  });
}

export function getViewDatas(data) {
  return request({
    url: '/dataStudio/web/v1/viewDatas',
    method: 'get',
    params: data,
  });
}

export function getViewInfo(data) {
  return request({
    url: '/dataStudio/web/v1/view/query',
    method: 'post',
    data,
  });
}

export function getViewAttribute(data) {
  return request({
    url: '/dataStudio/web/v1/view/attribute',
    method: 'post',
    data,
  });
}

export function getViewColumn(data) {
  return request({
    url: '/dataStudio/web/v1/view/column',
    method: 'post',
    data,
  });
}

export function setEditView(data) {
  return request({
    url: '/dataStudio/web/v1/view/edit',
    method: 'post',
    data,
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
