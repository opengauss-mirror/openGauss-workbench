<template>
  <div class="table-container">
    <Toolbar :status="barStatus" @addLine="toAddLine" @removeLine="handleRemoveLine(data)" />
    <EditTable2
      :data="props.data"
      :columns="tableColumns"
      :idKey="idKey"
      :rowStatusKey="rowStatusKey"
      :editingSuffix="editingSuffix"
      :editedSuffix="editedSuffix"
      v-model:globalEditing="globalEditing"
      @currentChange="handleCurrentChange"
    >
      <template v-for="item in tableColumns" :key="item.prop" v-slot:[item.prop]="scope">
        <el-input
          v-if="globalEditing && scope.row[item.prop + editingSuffix] && item.element == 'input'"
          v-model="scope.row[item.prop]"
          v-bind="item.attribute"
          @change="handleChangeValue(scope.row, scope.column)"
        />
        <el-input-number
          v-else-if="
            globalEditing && scope.row[item.prop + editingSuffix] && item.element == 'inputNumber'
          "
          v-model="scope.row[item.prop]"
          v-bind="item.attribute"
          :controls="false"
          @change="handleChangeValue(scope.row, scope.column)"
          style="width: 100%"
        />
        <el-select
          v-else-if="
            globalEditing && scope.row[item.prop + editingSuffix] && item.prop == 'columnName'
          "
          v-model="scope.row[item.prop]"
          v-bind="item.attribute"
          @change="handleChangeValue(scope.row, scope.column)"
        >
          <el-option
            v-for="op in props.columns"
            :key="op.value"
            :label="op.label"
            :value="op.value"
          />
        </el-select>
        <el-select
          v-else-if="
            globalEditing && scope.row[item.prop + editingSuffix] && item.element == 'select'
          "
          v-model="scope.row[item.prop]"
          v-bind="item.attribute"
          @change="handleChangeValue(scope.row, scope.column)"
        >
          <el-option
            v-for="op in item.options"
            :key="op.value"
            :label="op.label"
            :value="op.value"
          />
        </el-select>
        <el-checkbox
          v-else-if="item.element == 'checkbox'"
          v-model="scope.row[item.prop]"
          @change="handleChangeValue(scope.row, scope.column)"
        />
        <span v-else>{{ formatTextCell(scope.row[item.prop], item.element) }}</span>
      </template>
    </EditTable2>
  </div>
</template>

<script lang="ts" setup>
  import EditTable2 from './EditTable2.vue';
  import Toolbar from './Toolbar.vue';
  import { useEditTabHooks } from './useEditTabHooks';
  import type { CommonParams, TableColumn, AvailColumnList } from '../types';

  const props = withDefaults(
    defineProps<{
      data: any[];
      commonParams: CommonParams;
      columns: AvailColumnList[];
    }>(),
    {
      data: () => [],
      columns: () => [],
    },
  );

  const emit = defineEmits<{
    (e: 'update:data', value: any[]): void;
  }>();

  const idKey = ref('_id');
  const rowStatusKey = ref('_rowStatus');
  const editingSuffix = ref('_isEditing');
  const editedSuffix = ref('_edited');
  const globalEditing = ref(false);
  const barStatus = reactive({
    addLine: true,
    removeLine: true,
  });

  const data = computed({
    get: () => props.data,
    set: (val) => emit('update:data', val),
  });
  const tableColumns = ref<TableColumn[]>([
    {
      label: 'table.indexes.indexName',
      prop: 'indexName',
      slot: true,
      isI18n: true,
      element: 'input',
    },
    {
      label: 'table.indexes.isUnique',
      prop: 'isUnique',
      slot: true,
      isI18n: true,
      element: 'checkbox',
    },
    {
      label: 'table.indexes.accessMethod',
      prop: 'accessMethod',
      slot: true,
      isI18n: true,
      element: 'select',
      options: [
        {
          value: 'btree',
          label: 'btree',
        },
        {
          value: 'cbtree',
          label: 'cbtree',
        },
        {
          value: 'cgin',
          label: 'cgin',
        },
        {
          value: 'gin',
          label: 'gin',
        },
        {
          value: 'gist',
          label: 'gist',
        },
        {
          value: 'hash',
          label: 'hash',
        },
        {
          value: 'psort',
          label: 'psort',
        },
        {
          value: 'spgist',
          label: 'spgist',
        },
        {
          value: 'ubtree',
          label: 'ubtree',
        },
      ],
    },
    {
      label: 'table.indexes.columnName',
      prop: 'columnName',
      slot: true,
      isI18n: true,
      element: 'select',
      attribute: {
        multiple: true,
      },
      options: [],
    },
    {
      label: 'table.indexes.expression',
      prop: 'expression',
      slot: true,
      isI18n: true,
      element: 'input',
    },
    {
      label: 'table.description',
      prop: 'description',
      slot: true,
      isI18n: true,
      element: 'input',
      attribute: {
        maxlength: 5000,
      },
    },
  ]);
  const formatTextCell = (value: string[] | string, type) => {
    if (['select'].includes(type) && Array.isArray(value)) return value.join(',');
    return value;
  };
  const toAddLine = () => {
    handleAddLine(data.value, {
      isUnique: true,
    });
  };
  const { handleChangeValue, handleCurrentChange, handleAddLine, handleRemoveLine } =
    useEditTabHooks({
      idKey: idKey.value,
      rowStatusKey: rowStatusKey.value,
      editingSuffix: editingSuffix.value,
      editedSuffix: editedSuffix.value,
    });
</script>

<style lang="scss" scoped>
  .table-container {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
</style>
