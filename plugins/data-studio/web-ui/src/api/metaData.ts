import request from './request';

// getDatabaseList
export function getDatabaseList({ id }) {
  return request({
    url: `/dataStudio/web/v1/metaData/${id}/dataList`,
    method: 'get',
  });
}

// getSchemaList: Object Owner
export function getSchemaList(data) {
  return request({
    url: '/dataStudio/web/v1/metaData/schemaList',
    method: 'get',
    params: data,
  });
}

// getObjectList: Object list. Sequence -> tableName
export function getObjectList(data) {
  return request({
    url: '/dataStudio/web/v1/metaData/objectList',
    method: 'get',
    params: data,
  });
}

// getSchemaObjectList.includes: [foreignTable, fun_pro, sequence, synonym, table, trigger...]
export function getSchemaObjectList(data) {
  return request({
    url: '/dataStudio/web/v1/metaData/schemaObjectList',
    method: 'get',
    params: data,
  });
}

// getSchemaObjects. query the one type of [foreignTable, fun_pro, sequence, synonym, table, trigger...]
export function getSchemaObjects(data) {
  return request({
    url: '/dataStudio/web/v1/metaData/schemaObjects',
    method: 'get',
    params: data,
  });
}

// get tablespace list
export function getTablespaceList(data) {
  return request({
    url: `/dataStudio/web/v1/metaData/tablespace`,
    method: 'get',
    params: data,
  });
}

// get dataType list
export function getDataTypeList(data) {
  return request({
    url: `/dataStudio/web/v1/metaData/typeList`,
    method: 'get',
    params: data,
  });
}
