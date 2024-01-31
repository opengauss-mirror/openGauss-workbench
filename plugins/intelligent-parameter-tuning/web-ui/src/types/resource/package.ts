export enum PackageType {
  OPENGAUSS = 'openGauss',
  ZOOKEEPER = 'zookeeper',
  SHARDING_PROXY = 'shardingProxy',
  OPENLOOKENG = 'openLooKeng',
  DISTRIBUTE_DEPLOY = 'dad'
}

export enum PackageState {
  OK,
  ERROR
}

export interface UploadInfo {
  realPath: string,
  realName: string,
  name: string,
  file: File
}

export enum DefaultOpenGauss {
  VERSION = '5.0.0'
}
