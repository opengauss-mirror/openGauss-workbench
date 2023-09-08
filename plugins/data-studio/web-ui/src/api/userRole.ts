import request from './request';

export const getResource = async (uuid) => {
  return await request({
    url: `/dataStudio/web/v1/metaData/resource/${uuid}`,
    method: 'get',
  });
};

export const getUserRoleList = async (uuid) => {
  return await request({
    url: `/dataStudio/web/v1/metaData/user/${uuid}`,
    method: 'get',
  });
};

export const previewUserRole = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/user/creat/preview`,
    method: 'post',
    data,
  });
};

export const createUserRole = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/user/creat`,
    method: 'post',
    data,
  });
};

export const dropUser = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/user/drop`,
    method: 'delete',
    data,
  });
};
