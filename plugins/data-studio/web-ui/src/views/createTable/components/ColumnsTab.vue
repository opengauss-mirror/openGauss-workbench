<template>
  <div class="table-container">
    <Toolbar :status="barStatus" @addLine="toAddLine" @removeLine="handleRemoveLine(data)" />
    <EditTable
      :data="props.data"
      :columns="tableColumns"
      :idKey="idKey"
      :rowStatusKey="rowStatusKey"
      :editingSuffix="editingSuffix"
      :editedSuffix="editedSuffix"
      :menuList="['advancedCopy']"
      v-model:globalEditing="globalEditing"
      @selectionChange="handleSelectionChange"
    >
      <template v-for="item in tableColumns" :key="item.prop" v-slot:[item.prop]="scope">
        <el-input
          v-if="globalEditing && scope.row[item.prop + editingSuffix] && item.element == 'input'"
          v-model="scope.row[item.prop]"
          v-bind="item.attributes"
          @change="handleChangeValue(scope.row, scope.column)"
        />
        <el-input-number
          v-else-if="
            globalEditing && scope.row[item.prop + editingSuffix] && item.element == 'inputNumber'
          "
          v-model="scope.row[item.prop]"
          v-bind="item.attributes"
          :controls="false"
          @change="handleChangeValue(scope.row, scope.column)"
          style="width: 100%"
        />
        <el-select
          v-else-if="
            globalEditing && scope.row[item.prop + editingSuffix] && item.prop == 'dataType'
          "
          v-model="scope.row[item.prop]"
          v-bind="item.attributes"
          @change="handleChangeValue(scope.row, scope.column)"
        >
          <el-option
            v-for="op in dataTypeList"
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
          v-bind="item.attributes"
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
    </EditTable>
  </div>
</template>

<script lang="ts" setup>
  import EditTable from './EditTable2.vue';
  import Toolbar from './Toolbar.vue';
  import { getDataTypeList } from '@/api/metaData';
  import { useEditTabHooks } from './useEditTabHooks';
  import type { CommonParams, TableColumn } from '../types';
  const props = withDefaults(
    defineProps<{
      data: any[];
      commonParams: CommonParams;
    }>(),
    {
      data: () => [],
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

  const dataTypeList = ref([]);
  const data = computed({
    get: () => props.data,
    set: (val) => emit('update:data', val),
  });
  const tableColumns = ref<TableColumn[]>([
    {
      label: 'table.column.columnName',
      prop: 'columnName',
      slot: true,
      isI18nLabel: true,
      element: 'input',
    },
    {
      label: 'table.column.dataType',
      prop: 'dataType',
      slot: true,
      isI18nLabel: true,
      element: 'select',
      options: [],
    },
    {
      label: 'table.column.canBeNotNull',
      prop: 'canBeNull',
      slot: true,
      isI18nLabel: true,
      element: 'checkbox',
    },
    {
      label: 'table.column.defaultValue',
      prop: 'defaultValue',
      slot: true,
      isI18nLabel: true,
      element: 'input',
    },
    {
      label: 'table.column.isUnique',
      prop: 'isUnique',
      slot: true,
      isI18nLabel: true,
      element: 'checkbox',
    },
    {
      label: 'table.column.precisionSize',
      prop: 'precisionSize',
      slot: true,
      isI18nLabel: true,
      element: 'inputNumber',
      attributes: {
        min: 0,
        step: 0,
        precision: 0,
      },
    },
    {
      label: 'table.column.range',
      prop: 'range',
      slot: true,
      isI18nLabel: true,
      element: 'inputNumber',
      attributes: {
        min: 0,
        step: 0,
        precision: 0,
      },
    },
    {
      label: 'common.description',
      prop: 'description',
      slot: true,
      isI18nLabel: true,
      element: 'input',
      attributes: {
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
      columnName: '',
      dataType: dataTypeList.value[0]?.value,
      canBeNull: false,
      defaultValue: null,
      isUnique: false,
      precisionSize: null,
      range: null,
      description: null,
    });
  };
  const fetchDataTypeList = async () => {
    const res = (await getDataTypeList({ uuid: props.commonParams.uuid })) as unknown as string[];
    dataTypeList.value = res.map((item) => ({ value: item, label: item }));
  };
  const { handleChangeValue, handleSelectionChange, handleAddLine, handleRemoveLine } =
    useEditTabHooks({
      idKey: idKey.value,
      rowStatusKey: rowStatusKey.value,
      editingSuffix: editingSuffix.value,
      editedSuffix: editedSuffix.value,
    });
  onMounted(() => {
    nextTick(fetchDataTypeList);
  });
</script>

<style lang="scss" scoped>
  .table-container {
    flex: 1;
    display: flex;
    flex-direction: column;
  }
</style>
