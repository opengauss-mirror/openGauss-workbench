import request from './request';

export const queryTriggerFunctionApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/trigger/queryFunction',
    method: 'post',
    data,
  });
};

export const createFunctionTriggerApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/trigger/createFunction',
    method: 'post',
    data,
  });
};

export const getTriggerDdlPreviewApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/trigger/ddlPreview',
    method: 'post',
    data,
  });
};

export const createTriggerApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/trigger/create',
    method: 'post',
    data,
  });
};

export const updateTriggerEnableApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/trigger/enable',
    method: 'post',
    data,
  });
};

export const updateTriggerDisableApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/trigger/disable',
    method: 'post',
    data,
  });
};

export const deleteTriggerApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/trigger/delete',
    method: 'post',
    data,
  });
};

export const renameTriggerApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/trigger/rename',
    method: 'post',
    data,
  });
};

export const editTriggerApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/trigger/edit',
    method: 'post',
    data,
  });
};

export const showDdlTriggerApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/trigger/showDdl',
    method: 'post',
    data,
  });
};

export const queryTriggerApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/trigger/query',
    method: 'post',
    data,
  });
};
