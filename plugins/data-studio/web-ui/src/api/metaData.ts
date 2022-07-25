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

// getSchemaObjectList
export function getSchemaObjectList(data) {
  return request({
    url: '/dataStudio/web/v1/metaData/schemaObjectList',
    method: 'get',
    params: data,
  });
}
