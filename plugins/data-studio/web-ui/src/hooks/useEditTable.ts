import { reactive, toRefs } from 'vue';
export const useEditTable = (
  options = {
    editingSuffix: '',
    editedSuffix: '',
    rowStatusKey: '',
  },
) => {
  const state = reactive({
    globalEditing: false,
    editingElement: null,
    editingData: {
      row: {},
      column: {} as any,
    },
  });
  const cellClassFn = ({ row, column }) => {
    if (options.rowStatusKey && row[options.rowStatusKey] == 'add') return 'add';
    if (options.rowStatusKey && row[options.rowStatusKey] == 'delete') return 'delete';
    if (row[column.property + options.editedSuffix]) return 'edited';
  };
  const clickOutsideTable = () => {
    if (state.globalEditing) {
      state.globalEditing = false;
      state.editingElement = null;
      state.editingData.row[state.editingData.column.property + options.editingSuffix] = false;
    }
  };
  const handleTableClick = (event) => {
    if (
      state.globalEditing &&
      state.editingElement &&
      !state.editingElement.contains(event.target)
    ) {
      clickOutsideTable();
    }
  };
  return {
    ...toRefs(state),
    cellClassFn,
    clickOutsideTable,
    handleTableClick,
  };
};
