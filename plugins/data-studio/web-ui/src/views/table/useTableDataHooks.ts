import { reactive, toRefs } from 'vue';
import { ElMessage } from 'element-plus';
import { useI18n } from 'vue-i18n';
import { TableDataHooksOptions, RowInfo } from './types';
export const useTableDataHooks = (
  options: TableDataHooksOptions = {
    idKey: '',
    rowStatusKey: '',
    editingSuffix: '',
    editedSuffix: '',
    barStatus: {},
  },
  getRowInfo,
  handleCellDataChange,
  getData,
) => {
  const { t } = useI18n();
  const state = reactive({
    globalEditing: false,
    editingElement: null,
    editingData: {
      row: {},
      column: {} as any,
    },
    page: {
      pageNum: 1,
      pageSize: 100,
      pageTotal: 0,
      winId: '',
    },
  });
  const handleCopyLine = () => {
    const { data, idKey, rowStatusKey, currentRow, rowIndex } = getRowInfo() as RowInfo;
    if (currentRow) {
      data.splice(rowIndex + 1, 0, {
        ...currentRow,
        [idKey]: Date.now(),
        [rowStatusKey]: 'add',
      });
      handleCellDataChange();
    } else {
      ElMessage.info(t('message.selectedData'));
    }
  };
  const handleFirstPage = () => {
    state.page.pageNum = 1;
    getData('DataTab');
  };
  const handlePreviousPage = () => {
    state.page.pageNum--;
    getData('DataTab');
  };
  const handleNextPage = () => {
    state.page.pageNum++;
    getData('DataTab');
  };
  const handleLastPage = () => {
    state.page.pageNum = state.page.pageTotal;
    getData('DataTab');
  };
  const handlePage = (value) => {
    state.page.pageNum = Number(value || 1);
    getData('DataTab');
  };
  const handlePageSize = (value) => {
    state.page.pageSize = value;
    state.page.pageNum = 1;
    getData('DataTab');
  };
  return {
    ...toRefs(state),
    handleCopyLine,
    handlePreviousPage,
    handleNextPage,
    handleLastPage,
    handleFirstPage,
    handlePage,
    handlePageSize,
  };
};
