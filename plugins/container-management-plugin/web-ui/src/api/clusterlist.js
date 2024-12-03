import axios from './interceptor'
// 集群列表
export function cluterListData(params = {}) {
  return axios.get('/opengauss/cluster/list', { params })
}
// 系统列表
export function systemListData() {
  return axios.get('/cmdb/system/list')
}
// 域名列表
export function domainListData() {
  return axios.get('/k8s/cluster/list')
}
// 镜像列表
export function imageListData(params = {}) {
  return axios.get('/image/list',
    {
      params: {
        ...params,
        pageNum: 1,
        pageSize: 20,
        type: 'opengauss',
        enable: true,
        // os: 'openEuler',
        // architecture: 'arm',
        // version: '5.0.0'
      }
    })
}
// 创建集群
export function newCluster(body = {}) {
  return axios.post('/opengauss/cluster/deploy', body)
}
// 删除集群
export function deleteCluster(id) {
  return axios.delete('/opengauss/cluster/release/' + id)
}
// 集群详情
export function cluterDetails(id) {
  return axios.get('/opengauss/cluster/' + id);
}
// 集群详情Pod列表
export function clusterPodList(id) {
  return axios.get('/opengauss/cluster/pod/list/' + id);
}
// 重启集群pod
export function clusterPodRestart(id) {
  return axios.post(`/opengauss/cluster/${id}/restart`)
}
// 删除集群pod
export function clusterPodDelete(id, podname) {
  return axios.delete(`/opengauss/cluster/release/${id}/pod/${podname}`)
}
// 设为主节点
export function setTheMasterNode(body = {}) {
  return axios.post(`/opengauss/cluster/switchover/${body.id}`, body)
}
// 集群详情编辑
export function editCluster(body = {}) {
  return axios.put('/opengauss/cluster/' + body.id, body);
}
// 集群添加4a
export function addCluster4A(id) {
  return axios.put(`/opengauss/cluster/platform/4a/${id}`);
}
// 集群添加监控
export function addClusterMonitor(id) {
  return axios.put(`/opengauss/cluster/platform/monitor/${id}`);
}
// 集群删除监控
export function deleteClusterMonitor(id) {
  return axios.delete(`/opengauss/cluster/platform/monitor/${id}`);
}