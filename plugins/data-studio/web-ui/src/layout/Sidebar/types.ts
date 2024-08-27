export enum NodeEnum {
  ROOT = 'root',
  DATABASECOLLECT = 'databaseCollect',
  USERROLECOLLECT = 'userRoleCollect',
  TABLESPACECOLLECT = 'tablespaceCollect',
  JOB = 'job',
  DATABASE = 'database',
  USER = 'user',
  ROLE = 'role',
  TABLESPACE = 'tablespace',
  PUBLIC = 'public',
  PERSON = 'person',
  TABLECOLLECT = 'tableCollect',
  VIEWCOLLECT = 'viewCollect',
  SYNONYMCOLLECT = 'synonymCollect',
  SEQUENCECOLLECT = 'sequenceCollect',
  TABLE = 'table',
  TERMINAL = 'terminal',
  PACKAGE = 'package',
  VIEW = 'view',
  SYNONYM = 'synonym',
  SEQUENCE = 'sequence',
}

type NodeType = `${NodeEnum}` | '';

export interface Tree {
  id: string;
  oid?: string;
  rootId: string;
  parentId: string;
  label: string;
  name: string;
  isLeaf?: boolean;
  children?: Tree[];
  type?: NodeType;
  isConnect?: boolean;
  connectTime?: null | number;
  uuid?: string;
  schemaName?: string;
  parttype?: 'n' | 'y' | ''; // only in table
  tableName?: string; // only in trigger
  isTableTrigger?: boolean; // only in trigger
  isPackage?: boolean;
  enabled?: boolean;
  connectInfo: ConnectInfo;
}
export interface FetchNode {
  name: string;
  oid: string;
  parttype?: 'n' | 'y' | '';
  tableName?: string;
  isTableTrigger?: boolean;
  isPackage?: boolean;
  enabled?: boolean;
  children?: FetchNode;
}
export interface ConnectInfo {
  name: string;
  id: string;
  ip: string;
  port: string;
  userName: string;
  uuid?: string;
  type?: 'openGauss';
  driver?: '';
  password?: string;
  webUser: string;
}

export interface NodeData {
  id: string;
  type: NodeType;
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
  databaseName: string;
  schemaId: string;
  schema: string;
  nodeId: string;
}

export type BatchExportType =
  | 'schemaDDL'
  | 'schemaDDLData'
  | 'tableDDL'
  | 'tableDDLData'
  | 'functionDDL'
  | 'viewDDL'
  | 'sequenceDDL'
  | 'sequenceDDLData';
