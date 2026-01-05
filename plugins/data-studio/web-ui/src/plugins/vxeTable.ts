import '@/styles/vxe-table-reset.scss';
import { App } from 'vue';

import { Column, Table } from 'vxe-table';

function useTable(app: App) {
  app.use(Column).use(Table);
}

export default useTable;
