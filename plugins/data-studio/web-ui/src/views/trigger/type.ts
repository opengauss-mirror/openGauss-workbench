export interface Generateform {
  name: string;
  status: boolean;
  type: 'table' | 'view';
  tableName: string;
  time: string;
  frequency: string;
  function: string;
  event: string[];
  condition: string;
  columnList: string[];
  description: string;
}
