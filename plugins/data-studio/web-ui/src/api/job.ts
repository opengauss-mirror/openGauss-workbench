import request from './request';

export const createJobApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/job/create',
    method: 'post',
    data,
  });
};

export const queryAJobApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/job/query',
    method: 'post',
    data,
  });
};

export const queryJobListApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/job/queryList',
    method: 'post',
    data,
  });
};

export const editJobApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/job/edit',
    method: 'post',
    data,
  });
};

export const deleteJobApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/job/delete',
    method: 'post',
    data,
  });
};

export const setEnableJobApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/job/enable',
    method: 'post',
    data,
  });
};

export const setDisableJobApi = async (data) => {
  return await request({
    url: '/dataStudio/web/v1/job/disable',
    method: 'post',
    data,
  });
};
