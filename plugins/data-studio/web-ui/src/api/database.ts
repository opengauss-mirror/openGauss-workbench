import request from './request';

// getDatabaseList
export function getDatabaseList(uuid) {
  return request({
    url: `/dataStudio/web/v1/metaData/databaseList/${uuid}`,
    method: 'get',
  });
}

// create database
export function createDatabase(data) {
  return request({
    url: '/dataStudio/web/v1/database/create',
    method: 'post',
    data,
  });
}

// delete database
export function deleteDatabase(data) {
  return request({
    url: '/dataStudio/web/v1/metaData/database/delete',
    method: 'delete',
    data,
  });
}

// get database attribute
export function getDatabaseAttribute(data) {
  return request({
    url: '/dataStudio/web/v1/database/attribute',
    method: 'get',
    params: data,
  });
}

// open database connection
export function openDatabaseConnection(data) {
  return request({
    url: '/dataStudio/web/v1/database/connection',
    method: 'post',
    data,
  });
}
