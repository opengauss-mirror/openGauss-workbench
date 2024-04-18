<template>
  <div class="data-asider">
    <div class="top">
      <div>{{ $t('table.sort.title') + $t('common.colon') }}</div>
      <el-table class="compact-table" :data="tableData" ref="sortTableRef">
        <el-table-column type="selection" width="30" />
        <el-table-column prop="name" :label="t('table.column.title')" min-width="80" />
        <el-table-column :label="t('table.sort.title')" width="65">
          <template #default="{ row }">
            <el-dropdown trigger="click">
              <span class="dropdown-link">
                {{ row.multipleOrder || '-' }}
                <el-icon class="el-icon--right"><arrow-down /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="item in sortList"
                    :key="item"
                    @click="chooseMenu(row, item)"
                  >
                    {{ item || '-' }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div class="bottom">
      <el-button type="primary" @click="handleApply">
        {{ $t('button.apply') }}
      </el-button>
      <el-button @click="handleClose">{{ $t('button.hide') }}</el-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { useI18n } from 'vue-i18n';
  import { ElTable } from 'element-plus';
  import type { DataTabColumn, DataTabSortColumn } from '../types';

  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      columns: DataTabColumn[];
    }>(),
    {
      modelValue: false,
      columns: () => [],
    },
  );
  const myEmit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
    (event: 'confirm', data: any): void;
  }>();
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });

  const { t } = useI18n();
  const sortTableRef = ref<InstanceType<typeof ElTable>>();

  const sortList = ref([null, 'ASC', 'DESC']);
  const tableData = ref<DataTabSortColumn[]>([]);

  const initSort = () => {
    tableData.value = props.columns.map((item) => {
      return {
        name: item.name,
        multipleOrder: item.multipleOrder,
      };
    });
    nextTick(() => {
      tableData.value.forEach((row) => {
        sortTableRef.value!.toggleRowSelection(row, !!row.multipleOrder);
      });
    });
  };
  const chooseMenu = (row: DataTabSortColumn, sortType) => {
    row.multipleOrder = sortType;
  };
  const handleApply = () => {
    const selectionRows = sortTableRef.value.getSelectionRows();
    const list = tableData.value.filter((row) => {
      return selectionRows.findIndex((r) => r.name == row.name) > -1;
    });
    const data = JSON.parse(JSON.stringify(list));
    myEmit('confirm', data);
  };
  const handleClose = () => {
    visible.value = false;
  };
  onMounted(() => {
    initSort();
  });
  defineExpose({
    initSort,
  });
</script>

<style lang="scss" scoped>
  .data-asider {
    width: 340px;
    height: 100%;
    padding: 0 10px;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    border-right: 1px solid var(--el-border-color-lighter);
    .top {
      flex: 1;
      flex-basis: auto;
      overflow: auto;
    }
    .bottom {
      padding: 10px 0;
      text-align: center;
    }
  }
  .compact-table {
    font-size: 12px;
    :deep(.el-table__cell) {
      padding: 3px 0;
    }
    :deep(.cell) {
      padding: 0 8px;
    }
  }
  .dropdown-link {
    cursor: pointer;
    font-size: 12px;
    display: flex;
    align-items: center;
  }
  :deep(.el-dropdown) {
    line-height: inherit;
  }
</style>
