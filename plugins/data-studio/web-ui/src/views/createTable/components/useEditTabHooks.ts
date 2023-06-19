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
    currentRow: null,
  });
  const handleChangeValue = (row, column) => {
    row[column.property + editedSuffix] = true;
    if (!row[rowStatusKey]) row[rowStatusKey] = 'edit';
  };
  const handleCellDataChange = () => {
    if (!state.edited) state.edited = true;
  };
  const handleCurrentChange = (row) => {
    state.currentRow = row;
  };
  const getRowInfo = (data) => {
    const rowIndex = data.findIndex(
      (item) => state.currentRow && item[idKey] == state.currentRow[idKey],
    );
    return { rowIndex };
  };
  const handleAddLine = (data, defaultValue = {}) => {
    const { rowIndex } = getRowInfo(data);
    data.splice(rowIndex + 1, 0, {
      [idKey]: Date.now(),
      [rowStatusKey]: 'add',
      ...defaultValue,
    });
    handleCellDataChange();
  };
  const handleRemoveLine = (data) => {
    const { rowIndex } = getRowInfo(data);
    if (state.currentRow) {
      data.splice(rowIndex, 1);
      handleCellDataChange();
    } else {
      ElMessage.info(t('message.selectedData'));
    }
  };
  return {
    ...toRefs(state),
    handleChangeValue,
    handleCurrentChange,
    handleAddLine,
    handleRemoveLine,
  };
};
