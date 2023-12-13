import { KeyValue } from '@/api/modeling/request'

export enum OpenGaussVersionEnum {
  ENTERPRISE = 'ENTERPRISE',
  MINIMAL_LIST = 'MINIMAL_LIST',
  LITE = 'LITE',
  OPENlOOKENG = 'OPENlOOKENG'
}

export enum InstallModeEnum {
  ON_LINE = 'ON_LINE',
  OFF_LINE = 'OFF_LINE'
}

export enum DeployTypeEnum {
  CLUSTER = 'CLUSTER',
  SINGLE_NODE = 'SINGLE_NODE'
}

export enum ClusterRoleEnum {
  MASTER = 'MASTER',
  SLAVE = 'SLAVE',
  CASCADE = 'CASCADE',
  PENDING = 'PENDING',
  UNKNOWN = 'UNKNOWN',
  DOWN = 'DOWN',
  ABNORMAL = 'ABNORMAL',
  STOPED = 'STOPED'
}

export enum CMStateEnum {
  Primary = 'Primary',
  Standby = 'Standby',
  Down = 'Down'
}
export enum WsConnectTypeEnum {
  DOWNLOAD_INSTALL_PACKAGE = 'DOWNLOAD_INSTALL_PACKAGE',
  SSH = 'SSH',
  COMMAND_EXEC = 'COMMAND_EXEC'
}

export enum DatabaseKernelArch {
  MASTER_SLAVE = 'MASTER_SLAVE',
  SHARING_STORAGE = 'SHARING_STORAGE'
}

export enum ConnectTypeEnum {
  TCP = 'TCP',
  RDMA = 'RDMA'
}

export interface InstallContext {
  // version
  openGaussVersion?: OpenGaussVersionEnum;
  // openGauss version number
  openGaussVersionNum?: string;
  // install way
  installMode?: InstallModeEnum;
  // path
  installPackagePath?: string;
  // deploy
  deployType: DeployTypeEnum;

  clusterId: string;

  clusterName: string;
}

export interface MiniNodeConfig {
  clusterRole: string;
  hostId: string;
  rootPassword: string;
  privateIp: string;
  publicIp: string;
  installUserId: string;
  installUserName: string;
  installPath: string;
  dataPath: string;
  isInstallDemoDatabase: boolean;
}

export interface MinimalistInstallConfig {
  clusterName: string;
  port: number;
  installPackagePath: string;
  databaseUsername: string;
  databasePassword: string;
  databasePasswordOriginal: string;
  nodeConfigList: MiniNodeConfig[]
}

export interface LiteNodeConfig {
  clusterRole: string;
  hostId: string;
  rootPassword: string;
  privateIp: string;
  publicIp: string;
  installUserId: string;
  installUserName: string;
  installPath: string;
  dataPath: string;
  port: number;
  databasePassword: string;
  isEnvSeparate: boolean,
  envPath: string;
}

export interface LiteInstallConfig {
  clusterName: string;
  installPackagePath: string;
  port: number;
  databaseUsername: string;
  databasePassword: string;
  nodeConfigList: LiteNodeConfig[]
}

export interface EnterpriseInstallNodeConfig {
  clusterRole: string;
  hostId: string;
  rootPassword: string;
  privateIp: string;
  publicIp: string;
  hostname: string;
  installUserId: string;
  installUsername: string;
  azId: string;
  azName: string;
  isCMMaster: boolean;
  cmDataPath: string;
  cmPort: number;
  dataPath: string;
  xlogPath: string;
  azPriority: number;
}

export interface SharingStorageInstallConfig {
  dssHome: string;
  dssVgName: string;
  dssDataLunPath: string;
  xlogVgName: string;
  xlogLunPath: string;
  cmSharingLunPath: string;
  cmVotingLunPath: string;
  interconnectType: ConnectTypeEnum;
  rdmaConfig: string;
}

export interface EnterpriseInstallConfig { // eslint-disable-line
  installPath: string;
  installPackagePath: string;
  logPath: string;
  tmpPath: string;
  omToolsPath: string;
  corePath: string;
  port: number;
  enableDCF: boolean;
  databaseUsername: string;
  databasePassword: string;
  isInstallCM: boolean,
  azId: string;
  azName: string;
  databaseKernelArch: DatabaseKernelArch;
  sharingStorageInstallConfig: SharingStorageInstallConfig;
  nodeConfigList: EnterpriseInstallNodeConfig[];
}

export interface OpenLookengInstallConfig {
  name: '',
  dadInstallHostId: '',
  dadInstallUsername: '',
  dadTarId: '',
  dadInstallPath: '/data/install',
  dadInstallPassword: '',
  dadRootPassword: '',
  dadPort: 8080,
  ssInstallHostId: '',
  ssInstallUsername: '',
  ssInstallPassword: '',
  ssRootPassword: '',
  ssInstallPath: '/data/install',
  ssUploadPath: '/data/tar',
  ssTarId: '',
  ssPort: 1234,
  zkTarId: '',
  zkPort: 2181,
  dsConfig: Array<Array<string>>,
  tableName: '',
  columns: '',
  olkInstallHostId: '',
  olkInstallUsername: '',
  olkInstallPassword: '',
  olkRootPassword: '',
  olkInstallPath: '/data/install',
  olkUploadPath: '/data/tar',
  olkTarId: '',
  olkPort: 2345,
  remark: '',
  dadNeedEncrypt: false,
  ssNeedEncrypt: false,
  olkNeedEncrypt: false,
  ruleYaml: '',
  olkParamConfig: OlkParamConfig
}
export interface OlkParamConfig {
  xmx: '16G',
  maxMemory: '50GB',
  maxTotalMemory: '50GB',
  maxMemoryPer: '10GB',
  maxTotalMemoryPer: '10GB'
}

export interface ShardingDsConfig {
  host: '',
  port: '',
  dbName: '',
  username: ''
  password: ''
}

export interface downloadPackage {
  resourceUrl: string,
  targetPath: string,
  fileName: string,
  connectType: string | 'DOWNLOAD_INSTALL_PACKAGE',
  businessId: string | 'downloadPackage'
}

export enum WsConnectType {
  DOWNLOAD_INSTALL_PACKAGE = 'DOWNLOAD_INSTALL_PACKAGE',
  SSH = 'SSH',
  COMMAND_EXEC = 'COMMAND_EXEC'
}

export interface SSHBody {
  hostId: string,
  wsConnectType: WsConnectType,
  businessId: string
}
