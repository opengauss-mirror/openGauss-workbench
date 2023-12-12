export type TabName = 'DDL' | 'ForeignInfoTab' | 'ColumnsTab' | 'DataTab';
export type EditTableTabName = 'ColumnsTab' | 'DataTab';
export interface Generateform {
  foreignTable: string;
  exists: boolean;
  datasourceType: string;
  foreignServer: string;
  foreignDatabase: string;
  schema: string;
  remoteSchema: string;
  remoteDatabase: string;
  remoteTable: string;
  remoteFilePath: string;
  fileType: string;
  quote: string;
  escape: string;
  replaceNull: string;
  encoding: string;
  includeHeader: boolean;
  delimiter: string;
  delimiterOther: string;
  unmatchedEmptyString: boolean;
  description: string;
}

// export interface TableColumn {
//   label: string;
//   name?: string;
//   prop: string;
//   slot?: boolean;
//   isI18nLabel?: boolean;
//   element: 'input' | 'inputNumber' | 'select' | 'checkbox';
//   attribute?: Record<string, any>;
//   options?: any[];
// }

export interface EditTableColumn {
  label: string;
  name?: string;
  prop: string;
  slot?: boolean;
  isI18nLabel?: boolean;
  element: string;
  show?: boolean;
  attributes?: Record<string, any>;
  multipleOrder?: null | 'ASC' | 'DESC';
  options?: any[];
}

export interface DataTabColumn extends EditTableColumn {
  systemTypeNum: string;
  systemTypeName: number;
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
  selectionRows: any[];
  idKey: string;
  rowStatusKey: string;
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
