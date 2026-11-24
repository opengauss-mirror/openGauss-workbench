/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

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
