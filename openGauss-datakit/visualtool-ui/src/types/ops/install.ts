export enum OpenGaussVersionEnum {
  ENTERPRISE = 'ENTERPRISE',
  MINIMAL_LIST = 'MINIMAL_LIST',
  LITE = 'LITE'
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
  CASCADE = 'CASCADE'
}

export enum WsConnectTypeEnum {
  DOWNLOAD_INSTALL_PACKAGE = 'DOWNLOAD_INSTALL_PACKAGE',
  SSH = 'SSH',
  COMMAND_EXEC = 'COMMAND_EXEC'
}

// Install step object
export interface InstallContext {
  // selected version
  openGaussVersion?: OpenGaussVersionEnum;
  // openGauss version number
  openGaussVersionNum?: string;
  // The chosen installation method
  installMode?: InstallModeEnum;
  // During offline installation, the selected installation package path
  installPackagePath?: string;
  // Deployment method
  deployType: DeployTypeEnum;

  clusterId: string;

  clusterName: string;
}

// Minimalist object
export interface MiniNodeConfig {
  clusterRole: string;
  hostId: string;
  privateIp: string;
  publicIp: string;
  installUserId: string;
  installPath: string;
  dataPath: string;
  isInstallDemoDatabase: boolean;
}

export interface MinimalistInstallConfig {
  clusterName: string;
  port: number;
  databaseUsername: string;
  databasePassword: string;
  nodeConfigList: MiniNodeConfig[]
}

// lightweight object
export interface LiteNodeConfig {
  clusterRole: string;
  hostId: string;
  privateIp: string;
  publicIp: string;
  installUserId: string;
  installPath: string;
  dataPath: string;
  port: number;
  databasePassword: string;
}

export interface LiteInstallConfig {
  clusterName: string;
  port: number;
  databaseUsername: string;
  databasePassword: string;
  nodeConfigList: LiteNodeConfig[]
}

export interface EnterpriseInstallNodeConfig {
  clusterRole: string;
  hostId: string;
  privateIp: string;
  publicIp: string;
  hostname: string;
  installUserId: string;
  installUsername: string;
  azId: string;
  azName: string;
  isInstallCM: boolean;
  isCMMaster: boolean;
  cmDataPath: string;
  cmPort: number;
  dataPath: string;
  xlogPath: string;
}

// Enterprise Edition Object
export interface EnterpriseInstallConfig { // eslint-disable-line
  installPath: string;
  logPath: string;
  tmpPath: string;
  omToolsPath: string;
  corePath: string;
  port: number;
  enableDCF: boolean;
  databaseUsername: string;
  databasePassword: string;
  azId: string;
  azName: string;
  nodeConfigList: EnterpriseInstallNodeConfig[];
}

// Download the package object
// Installation package download
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
