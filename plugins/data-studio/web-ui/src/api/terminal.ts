import requestBlob from './requestBlob';

// export the data of terminal result
export function exportResultApi(data) {
  return requestBlob({
    url: `/dataStudio/web/v1/export/result`,
    method: 'post',
    responseType: 'blob',
    data,
  });
}
