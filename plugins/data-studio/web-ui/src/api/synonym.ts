import request from './request';

// createSynonym
export function createSynonym(data) {
  return request({
    url: '/dataStudio/web/v1/synonyms',
    method: 'post',
    data,
  });
}

// createSynonymDdl
export function createSynonymDdl(data) {
  return request({
    url: '/dataStudio/web/v1/synonyms/action?action=createSynonymDdl',
    method: 'post',
    data,
  });
}

// dropSynonym
export function dropSynonym(data) {
  return request({
    url: '/dataStudio/web/v1/synonyms',
    method: 'delete',
    data,
  });
}

// getSynonyms
export function getSynonyms(data) {
  return request({
    url: '/dataStudio/web/v1/synonyms',
    method: 'get',
    params: data,
  });
}
