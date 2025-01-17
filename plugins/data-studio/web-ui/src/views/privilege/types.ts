export type OperateType = 'schema' | 'table' | 'function' | 'sequence' | 'view';
export type GrantOrRevoke = 'GRANT' | 'REVOKE';
export type SelectedObjectList = Array<{ showName: string; name: string; schema?: string }>;
export interface SaveParams {
  uuid: string;
  grantOrRevoke: string;
  type: string;
  obj: { name: string; schema?: string }[];
  user: string[];
  privilegeOption: { privilege: string; checkPrivilege: boolean; checkOption: boolean }[];
}
