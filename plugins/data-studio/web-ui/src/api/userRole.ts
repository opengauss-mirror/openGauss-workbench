import request from './request';

export const getResource = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/metaData/resource`,
    method: 'get',
    params: data,
  });
};

export const getUserRoleList = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/metaData/user`,
    method: 'get',
    params: data,
  });
};

export const previewUserRole = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/user/create/preview`,
    method: 'post',
    data,
  });
};

export const createUserRole = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/user/create`,
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

export const getUserRoleInfo = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/user/attribute`,
    method: 'get',
    params: data,
  });
};

export const getUserRoleDdl = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/user/ddl`,
    method: 'get',
    params: data,
  });
};

export const updateUserRoleInfo = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/user/attribute/update`,
    method: 'post',
    data,
  });
};

export const updateUserPassword = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/user/password/update`,
    method: 'post',
    data,
  });
};
