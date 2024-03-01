import '@/styles/vxe-table-reset.scss';
import { App } from 'vue';

import { Column, Table, Tooltip } from 'vxe-table';

function useTable(app: App) {
  app.use(Column).use(Table).use(Tooltip);
}

export default useTable;
