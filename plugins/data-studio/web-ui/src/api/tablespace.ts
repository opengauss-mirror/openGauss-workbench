import request from './request';

export const getTablespaceListApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/metaData/tablespace/list',
    method: 'get',
    params: data,
  });
};

export const createTablespaceApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/tablespace/create',
    method: 'post',
    data,
  });
};

export const getTablespacePreviewDdlApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/tablespace/create/ddl',
    method: 'post',
    data,
  });
};

export const dropTablespaceApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/tablespace/drop',
    method: 'delete',
    data,
  });
};

export const getTablespaceAttributeApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/tablespace/attribute',
    method: 'get',
    params: data,
  });
};

export const updateTablespaceApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/tablespace/update',
    method: 'post',
    data,
  });
};

export const updateTablespacePreviewDdlApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/tablespace/update/ddl',
    method: 'post',
    data,
  });
};
