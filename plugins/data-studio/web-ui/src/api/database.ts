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
    url: '/dataStudio/web/v1/metaData/database/create',
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

// open database connection
export function openDatabaseConnection(data) {
  return request({
    url: '/dataStudio/web/v1/metaData/database/connection',
    method: 'post',
    data,
  });
}
