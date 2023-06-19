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

export interface CommonParams {
  schema: string;
  uuid: string;
}

export interface TableColumn {
  label: string;
  name?: string;
  prop: string;
  slot?: boolean;
  isI18n?: boolean;
  element: 'input' | 'inputNumber' | 'select' | 'checkbox' | 'cascader';
  attribute?: Record<string, any>;
  options?: any[];
}

export interface ColumnTabInterface {
  columnName: string;
  dataType: string;
  canBeNull: boolean;
  defaultValue: boolean;
  isUnique: boolean;
  precisionSize: number;
  range: number;
  description: string;
  [key: string]: any;
}

export interface AvailColumnList {
  label: string;
  value: string;
  dataType: string;
}
