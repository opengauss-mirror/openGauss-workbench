import request from './request';
import requestBlob from './requestBlob';

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

export const updateTableColumn = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/tableColumns/edit`,
    method: 'post',
    data,
  });
};

export const getTableConstraint = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/tableConstraints`,
    method: 'get',
    params: data,
  });
};

export const updateTableConstraint = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/constraint`,
    method: 'post',
    data,
  });
};

export const setConstraintPk = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/constraint/pk`,
    method: 'post',
    data,
  });
};

export const getTableIndex = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/tableIndexs`,
    method: 'get',
    params: data,
  });
};

export const updateTableIndex = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/index`,
    method: 'post',
    data,
  });
};

export const getTableData = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/tableDatas`,
    method: 'post',
    data,
  });
};

export const updateTableData = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/tableDatas/edit`,
    method: 'post',
    data,
  });
};

export const getCreateTableDdl = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/table/ddl`,
    method: 'post',
    data,
  });
};

export const createTable = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/table`,
    method: 'post',
    data,
  });
};

export const setTableAnalyze = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/table/analyze`,
    method: 'post',
    data,
  });
};

export const setTableReindex = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/table/reindex`,
    method: 'post',
    data,
  });
};

export const setTableTruncate = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/table/truncate`,
    method: 'post',
    data,
  });
};

export const setTableVacuum = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/table/vacuum`,
    method: 'post',
    data,
  });
};

export const dropTable = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/table/drop`,
    method: 'delete',
    data,
  });
};

export const tableRelatedSequence = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/table/sequence`,
    method: 'post',
    data,
  });
};

export const getTableAttribute = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/table/attribute`,
    method: 'post',
    data,
  });
};

export const renameTable = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/table/rename`,
    method: 'post',
    data,
  });
};

export const setTableComment = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/table/comment`,
    method: 'post',
    data,
  });
};

export const setTablespace = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/table/retablespace`,
    method: 'post',
    data,
  });
};

export const setTableSchema = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/table/reschema`,
    method: 'post',
    data,
  });
};

export const getTablePartition = async (data) => {
  return await request({
    url: `/dataStudio/web/v1/table/attribute/partition`,
    method: 'get',
    params: data,
  });
};

export const importTableData = async (data) => {
  return await requestBlob({
    url: `/dataStudio/web/v1/import/table/data`,
    method: 'post',
    data,
  });
};

// export [data, ddl] of table
export const exportTableDdl = async (data) => {
  return await requestBlob({
    url: `/dataStudio/web/v1/export/table/ddl`,
    method: 'post',
    responseType: 'blob',
    data,
  });
};

// export data of table
export const exportTableData = async (data) => {
  return await requestBlob({
    url: `/dataStudio/web/v1/export/table/data`,
    method: 'post',
    responseType: 'blob',
    data,
  });
};

// export data of table
export const exportTableFilterData = async (data) => {
  return await requestBlob({
    url: `/dataStudio/web/v1/table/exportData`,
    method: 'post',
    responseType: 'blob',
    data,
  });
};

export const closeTableDatas = async (winId) => {
  return await request({
    url: `/dataStudio/web/v1/tableDatas/close/${winId}`,
    method: 'get',
  });
};
