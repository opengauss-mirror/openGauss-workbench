import request from './request';
import requestBlob from './requestBlob';

// dropFunctionSP
export function dropFunctionSP(data) {
  return request({
    url: '/dataStudio/web/v1/functionSP',
    method: 'delete',
    data,
  });
}

export function dropPackage(data) {
  return requestBlob({
    url: '/dataStudio/web/v1/drop/package',
    method: 'delete',
    data,
  });
}

export function getCoverageRateList(data) {
  return request({
    url: '/dataStudio/web/v1/coverageRate/query',
    method: 'post',
    data,
  });
}

export function deleteCoverageRate(data) {
  return request({
    url: '/dataStudio/web/v1/coverageRate/delete',
    method: 'post',
    data,
  });
}

export function exportCoverageRate(data) {
  return requestBlob({
    url: '/dataStudio/web/v1/coverageRate/export',
    method: 'post',
    data,
  });
}

// export ddl of function
export function exportFunctionDdl(data) {
  return requestBlob({
    url: '/dataStudio/web/v1/export/function/ddl',
    method: 'post',
    data,
  });
}
