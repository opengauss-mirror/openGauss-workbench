import request from './request';

export const getTableDdl = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/tableDdls`,
    method: 'get',
    params: data,
  });
};

export const getTableColumn = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/tableColumns`,
    method: 'get',
    params: data,
  });
};

export const getTableConstraint = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/tableConstraints`,
    method: 'get',
    params: data,
  });
};

export const getTableIndex = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/tableIndexs`,
    method: 'get',
    params: data,
  });
};

export const getTableData = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/tableDatas`,
    method: 'get',
    params: data,
  });
};
