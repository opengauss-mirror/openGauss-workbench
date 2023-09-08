export interface Tree {
  id: string;
  rootId: string;
  parentId: string;
  label: string;
  name: string;
  isLeaf?: boolean;
  children?: Tree[];
  type?: string;
  isConnect?: boolean;
  connectTime?: null | number;
  uuid?: string;
  connectInfo: ConnectInfo;
}
export interface FetchNode {
  name: string;
  oid: string;
  parttype?: 'n' | 'y' | '';
  isPackage?: boolean;
  children?: FetchNode;
}
export interface ConnectInfo {
  name: string;
  id: string;
  ip: string;
  port: string;
  userName: string;
  uuid?: string;
  type?: string;
  driver?: '';
  password?: string;
  webUser: string;
}

export interface NodeData {
  id: string;
  type: string;
  connectInfo: ConnectInfo;
  rootId: string;
  parentId: string | null;
  uuid: string;
  databaseId: string;
  databaseName: string;
  schemaId: string;
  schemaName: string;
  oid: string;
  label: string;
  name: string;
  children?: NodeData[];
  [key: string]: any;
}

export interface RefreshOptions {
  connectInfo: ConnectInfo | undefined;
  rootId: string;
  parentId: string;
  uuid: string;
  databaseId: string;
  schemaId: string;
  nodeId: string;
}
