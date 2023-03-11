import { KeyValue } from '@/types/global'
import { PackageState, PackageType } from '@/types/resource/package'

export const mockHostListAll = () => {
  return new Promise<KeyValue>((resolve, reject) => {
    setTimeout(() => resolve(
        {
          'msg': 'success',
          'code': 200,
          'data': [{
            'createBy': 'admin',
            'createTime': '2023-02-24 19:53:20',
            'updateBy': 'admin',
            'updateTime': '2023-02-24 19:53:20',
            'remark': null,
            'params': {},
            'hostId': '1629087075347202049',
            'hostname': 'instance-me8utibj',
            'privateIp': '192.168.48.4',
            'publicIp': '120.48.145.206',
            'port': 22,
            'azId': null,
            'os': 'centos',
            'cpuArch': 'x86_64'
          }, {
            'createBy': 'admin',
            'createTime': '2023-02-24 19:54:04',
            'updateBy': 'admin',
            'updateTime': '2023-02-24 19:54:04',
            'remark': null,
            'params': {},
            'hostId': '1629087262375411714',
            'hostname': 'instance-xik6j867',
            'privateIp': '192.168.48.5',
            'publicIp': '120.48.139.202',
            'port': 22,
            'azId': null,
            'os': 'centos',
            'cpuArch': 'x86_64'
          }, {
            'createBy': 'admin',
            'createTime': '2023-02-24 21:59:12',
            'updateBy': 'admin',
            'updateTime': '2023-02-24 21:59:12',
            'remark': null,
            'params': {},
            'hostId': '1629118750030561281',
            'hostname': 'VM-8-3-centos',
            'privateIp': '10.0.8.3',
            'publicIp': '82.157.245.128',
            'port': 22,
            'azId': null,
            'os': 'centos',
            'cpuArch': 'x86_64'
          }, {
            'createBy': 'admin',
            'createTime': '2023-02-24 22:08:57',
            'updateBy': 'admin',
            'updateTime': '2023-02-24 22:08:57',
            'remark': null,
            'params': {},
            'hostId': '1629121203744538626',
            'hostname': 'instance-ryd4ib1a',
            'privateIp': '192.168.48.6',
            'publicIp': '106.12.153.142',
            'port': 22,
            'azId': null,
            'os': 'centos',
            'cpuArch': 'x86_64'
          }]
        }
      ),
      1200)
  })
}

// write host id for user list
export const mockUserListAll = (data: KeyValue) => {
  return new Promise<KeyValue>((resolve, reject) => {
    setTimeout(() => resolve({
      'msg': 'success',
      'code': 200,
      'data': [{
        'createBy': 'admin',
        'createTime': '2023-02-24 19:55:14',
        'updateBy': 'admin',
        'updateTime': '2023-02-24 19:55:14',
        'remark': null,
        'params': {},
        'hostUserId': '1629087554428993538',
        'username': 'omm',
        'password': 'XCvpnQ4onbdWsdnXaSyV7v1i7UOCKDCpjmK1d9IpWM7uye0arSimqaxKhJYlepopxpZ7wKKUQUMfvIhDWEivAg==',
        'hostId': '1629087075347202049'
      }, {
        'createBy': 'admin',
        'createTime': '2023-02-24 22:09:19',
        'updateBy': 'admin',
        'updateTime': '2023-02-24 22:09:19',
        'remark': null,
        'params': {},
        'hostUserId': '1629121295088091137',
        'username': 'cm',
        'password': 'RWwKBO8sIrZQr390GSurg7RI7e4PqioO9N44bZ8bIW7mOM6a6mrT7OW8Xz5K0uPvtnsxIgZDIAR1Qr+7R66dvA==',
        'hostId': '1629087075347202049'
      }]
    }), 1000)
  })
}

export const mockTarListAll = () => {
  return new Promise<KeyValue>((resolve, reject) => {
    setTimeout(() => resolve({
      'msg': 'success',
      'code': 200,
      'data': [{
        id: 2938,
        name: 'hetu-server-1.9.0-SNAPSHOT.jar',
        type: PackageType.OPENLOOKENG,
        version: '1.9.0-snapshot',
        path: '/work/tar/hetu-server-1.9.0-SNAPSHOT.jar',
        uploader: 'admin',
        uploadTime: '',
        remark: '这是一个olk的测试',
        state: PackageState.OK
      }, {
        id: 1022,
        name: 'apache-zookeeper-3.8.0-bin.tar.gz',
        type: PackageType.ZOOKEEPER,
        version: '3.8.0',
        path: '/work/tar/apache-zookeeper-3.8.0-bin.tar.gz',
        uploader: 'admin',
        uploadDate: '',
        remark: '这是一个zk的测试',
        state: PackageState.OK
      }, {
        id: 4596,
        name: 'apache-shardingsphere-5.3.0-shardingsphere-proxy-bin.tar.gz',
        type: PackageType.SHARDING_PROXY,
        version: '5.3.0',
        path: '/work/tar/apache-shardingsphere-5.3.0-shardingsphere-proxy-bin.tar.gz',
        uploader: 'admin',
        uploadDate: '',
        remark: '这是一个sharding的测试',
        state: PackageState.OK
      }]
    }))
  })
}

export const mockPathEmpty = (hostId: string, pathParam: KeyValue) => {
  return new Promise<KeyValue>((resolve, reject) => {
    setTimeout(() => resolve({
      'msg': 'success',
      'code': 200,
      'data': true
    }), 2000)
  })
}

export const mockPortUsed = (hostId: string, pathParam: KeyValue) => {
  return new Promise<KeyValue>((resolve, reject) => {
    setTimeout(() => resolve({
      'msg': 'success',
      'code': 200,
      'data': true
    }), 2000)
  })
}

export const mockEncryptPassword = async (pwd: string) => {
  return new Promise<string>((resolve, reject) => {
    resolve(pwd + 'mock')
  })
}

export const mockHostPingById = (hostId: string, data: KeyValue) => {
  return new Promise<KeyValue>((resolve, reject) => {
    resolve({
      'msg': 'success',
      'code': 200,
      'data': true
    })
  })
}

export const mockGenerateYaml = () => {
  return new Promise<KeyValue>((resolve, reject) => {
    resolve({
      'msg': 'success',
      'code': 200,
      'data': 'yaml内容正文'
    })
  })
}
