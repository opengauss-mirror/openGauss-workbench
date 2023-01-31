import request from './request';

// dropFunctionSP
export function dropFunctionSP(data) {
  return request({
    url: '/dataStudio/web/v1/functionSP',
    method: 'delete',
    data,
  });
}
