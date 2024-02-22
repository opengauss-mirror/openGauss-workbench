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

// getSchemaObjects. query the one type of [ table, foreignTable, trigger, fun_pro, sequence, view, synonym ]
export function getSchemaObjects(data) {
  return request({
    url: '/dataStudio/web/v1/metaData/schemaObjects',
    method: 'get',
    params: data,
  });
}

// get count of schemaObjects. query all count of [ table, foreignTable, trigger, fun_pro, sequence, view, synonym ], such as {foreignTable: 0, function :0, sequence: 0, synonym: 0, table: 0, trigger: 0, view: 0}
export function getSchemaObjectCount(data) {
  return request({
    url: '/dataStudio/web/v1/metaData/schemaObjectCount',
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
