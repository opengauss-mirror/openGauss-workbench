export type TabName = 'DDL' | 'Column' | 'Constraint' | 'Indexes' | 'Data';
export type EditTableTabName = 'Column' | 'Constraint' | 'Indexes' | 'Data';
export interface BarStatus {
  [key: string]: boolean;
}
export interface TableDataHooksOptions {
  idKey: string;
  rowStatusKey: string;
  editingSuffix: string;
  editedSuffix: string;
  barStatus: BarStatus;
}
export interface RowInfo {
  data: any[];
  currentRow: any;
  idKey: string;
  rowStatusKey: string;
  rowIndex: number;
}

export interface EditDataResponse {
  msg: string;
  pkcreate: boolean;
}
