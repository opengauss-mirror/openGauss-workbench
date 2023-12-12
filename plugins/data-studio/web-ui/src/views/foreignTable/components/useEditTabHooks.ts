import { reactive, toRefs } from 'vue';
import { ElMessage } from 'element-plus';
import { useI18n } from 'vue-i18n';
export const useTableDataHooks = (
  options = {
    idKey: '',
    rowStatusKey: '',
    editingSuffix: '',
    editedSuffix: '',
    barStatus: {},
  },
  getRowInfo,
  getTableRowIndex,
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
  function generateRandomNumber() {
    return Math.floor(Math.random() * (9999 - 1000 + 1)) + 1000;
  }
  const handleCopyLine = () => {
    const { data, idKey, rowStatusKey, selectionRows } = getRowInfo();
    if (selectionRows.length) {
      selectionRows.forEach((item) => {
        const rowIndex = getTableRowIndex(data, item, idKey);
        data.splice(rowIndex + 1, 0, {
          ...item,
          [idKey]: Date.now() * 1000 + generateRandomNumber(),
          [rowStatusKey]: 'add',
        });
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
  const handlePage = (pageNum) => {
    state.page.pageNum = Number(pageNum || 1);
    getData('DataTab');
  };
  const changePageSize = (pageSize) => {
    state.page.pageSize = pageSize;
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
    changePageSize,
  };
};
