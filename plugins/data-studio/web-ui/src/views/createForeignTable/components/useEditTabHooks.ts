import { reactive, toRefs } from 'vue';
import { ElMessage } from 'element-plus';
import { useI18n } from 'vue-i18n';
export const useEditTabHooks = (
  options = {
    idKey: '',
    rowStatusKey: '',
    editingSuffix: '',
    editedSuffix: '',
  },
) => {
  const { t } = useI18n();
  const { idKey, rowStatusKey, editedSuffix } = options;
  const state = reactive({
    edited: false,
    selectionRows: [],
  });
  const handleChangeValue = (row, column) => {
    row[column.property + editedSuffix] = true;
    if (!row[rowStatusKey]) row[rowStatusKey] = 'edit';
  };
  const handleCellDataChange = () => {
    if (!state.edited) state.edited = true;
  };
  const handleSelectionChange = (row) => {
    state.selectionRows = row;
  };

  function generateRandomNumber() {
    return Math.floor(Math.random() * (9999 - 1000 + 1)) + 1000;
  }
  const getTableRowIndex = (tableData, targetRow, targetIdKey) => {
    return tableData.findIndex((item) => targetRow && item[targetIdKey] == targetRow[targetIdKey]);
  };

  const handleAddLine = (data, defaultValue = {}) => {
    if (state.selectionRows?.length) {
      state.selectionRows.forEach((item) => {
        const rowIndex = getTableRowIndex(data, item, idKey);
        data.splice(rowIndex + 1, 0, {
          [idKey]: Date.now() * 1000 + generateRandomNumber(),
          [rowStatusKey]: 'add',
          ...defaultValue,
        });
      });
    } else {
      data.splice(0, 0, {
        [idKey]: Date.now() * 1000 + generateRandomNumber(),
        [rowStatusKey]: 'add',
        ...defaultValue,
      });
    }
    handleCellDataChange();
  };

  const handleRemoveLine = (data) => {
    if (state.selectionRows?.length) {
      state.selectionRows.forEach((item) => {
        const rowIndex = getTableRowIndex(data, item, idKey);
        data.splice(rowIndex, 1);
      });
      handleCellDataChange();
    } else {
      ElMessage.info(t('message.selectedData'));
    }
  };

  return {
    ...toRefs(state),
    handleChangeValue,
    handleSelectionChange,
    handleAddLine,
    handleRemoveLine,
  };
};
