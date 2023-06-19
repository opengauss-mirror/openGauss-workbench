import request from './request';
import requestBlob from './requestBlob';

export function getSchemaUsers(data) {
  return request({
    url: `/dataStudio/web/v1/schema/query/users`,
    method: 'post',
    data,
  });
}

export function createSchema(data) {
  return request({
    url: `/dataStudio/web/v1/schema/create`,
    method: 'post',
    data,
  });
}

export function getSchema(data) {
  return request({
    url: `/dataStudio/web/v1/schema/query`,
    method: 'post',
    data,
  });
}

export function updateSchema(data) {
  return request({
    url: `/dataStudio/web/v1/schema/update`,
    method: 'post',
    data,
  });
}

export function deleteSchema(data) {
  return request({
    url: `/dataStudio/web/v1/schema/delete`,
    method: 'post',
    data,
  });
}

// export ddl of schema
export function exportSchemaDdl(data) {
  return requestBlob({
    url: `/dataStudio/web/v1/export/schema/ddl`,
    method: 'post',
    data,
  });
}
