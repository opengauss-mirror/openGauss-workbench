export type TabName = 'DDL' | 'Column' | 'Constraint' | 'Indexes' | 'Data';
export type EditTableTabName = 'Column' | 'Constraint' | 'Indexes' | 'Data';
export interface EditTableColumn {
  label: string;
  name: string;
  isI18nLabel?: boolean;
  type: 'input' | 'inputNumber' | 'select' | 'checkbox' | 'cascader';
  show?: boolean;
  attributes?: Record<string, any>;
  multipleOrder?: null | 'ASC' | 'DESC';
  options?: any[];
}

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
  PKCreate: boolean;
}

export interface DataTabColumn extends EditTableColumn {
  systemTypeNum: string;
  systemTypeName: number;
}

export interface DataTabSortColumn {
  name: string;
  multipleOrder: null | 'ASC' | 'DESC';
}

export interface SetFilterTableDataRow1 {
  id: number;
  level: number;
  type: 'normal';
  key: string;
  keySystemTypeNum: string;
  keySystemDataType: number;
  connector: string;
  value: string;
  value2: string;
  logic: 'and' | 'or' | '';
  check: boolean;
}
export interface SetFilterTableDataRow2 {
  id: number;
  level: number;
  type: 'leftBracket' | 'rightBracket';
  logic?: 'and' | 'or' | '';
  check: boolean;
}
export type SetFilterTableDataRow = SetFilterTableDataRow1 | SetFilterTableDataRow2;
