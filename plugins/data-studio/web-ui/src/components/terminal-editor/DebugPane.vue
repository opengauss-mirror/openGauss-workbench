<template>
  <div class="DebugPane">
    <div class="pane">
      <div class="title-wrapper">
        <div class="title">
          <el-icon><CaretBottom /></el-icon>
          {{ $t('debugPane.callStack') }}
        </div>
      </div>
      <el-table :data="props.stackList" border max-height="200">
        <el-table-column
          prop="frameno"
          align="center"
          :label="$t('debugPane.callHierarchy')"
          width="70"
        />
        <el-table-column
          prop="funcname"
          align="center"
          :label="$t('debugPane.function')"
          min-width="180"
          show-overflow-tooltip
        />
        <el-table-column
          prop="lineno"
          align="center"
          :label="$t('debugPane.currentLineNumber')"
          width="65"
        />
      </el-table>
    </div>
    <div class="pane">
      <div class="title-wrapper">
        <div class="title left">
          <el-icon><CaretBottom /></el-icon>
          {{ $t('debugPane.breakpoint') }}
        </div>
        <div class="right">
          <el-button plain @click="handleChangeStatus(true)">
            {{ $t('button.enabled') }}
          </el-button>
          <el-button type="primary" @click="handleChangeStatus(false)">
            {{ $t('button.disabled') }}
          </el-button>
        </div>
      </div>
      <el-table
        ref="multipleTableRef"
        :data="props.breakPointList"
        border
        max-height="250"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" align="center" width="35" />
        <el-table-column
          prop="lineno"
          align="center"
          :label="$t('debugPane.lineNumber')"
          width="65"
        />
        <el-table-column
          prop="query"
          align="center"
          :label="$t('debugPane.sentence')"
          min-width="100"
          show-overflow-tooltip
        />
        <el-table-column prop="enable" align="center" :label="$t('button.status')" width="65">
          <template #default="scope">{{
            scope.row.enable ? $t('button.enabled') : $t('button.disabled')
          }}</template>
        </el-table-column>
      </el-table>
    </div>
    <div class="pane">
      <div class="title-wrapper">
        <div class="title">
          <el-icon><CaretBottom /></el-icon>
          {{ $t('debugPane.variable') }}
        </div>
      </div>
      <el-table ref="singleTableRef" :data="variableList" border max-height="290">
        <el-table-column
          prop="varname"
          align="center"
          :label="$t('debugPane.variable')"
          :min-width="100"
          show-overflow-tooltip
        >
          <template #header>
            <div v-if="showVariableInput" class="flex-header">
              <div style="word-break: keep-all; margin-right: 5px">
                {{ $t('debugPane.variable') }}
              </div>
              <div class="flex-header">
                <el-icon @click="showVariableInput = false" class="icon-pointer"
                  ><Search
                /></el-icon>
                <el-input
                  class="border-bottom-input"
                  v-model="filterVariableText"
                  :placeholder="$t('debugPane.placeholder.variable')"
                  clearable
                />
              </div>
            </div>
            <div v-else class="flex-header">
              <div style="width: 12px"></div>
              <span>{{ $t('debugPane.variable') }}</span>
              <el-icon @click="showVariableInput = true" class="icon-pointer">
                <Search />
              </el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="value"
          align="center"
          :label="$t('debugPane.value')"
          min-width="100"
          show-overflow-tooltip
        />
        <el-table-column
          prop="vartype"
          align="center"
          :label="$t('debugPane.dataType')"
          width="70"
        />
        <el-table-column
          prop="paramType"
          align="center"
          :label="$t('debugPane.paramType')"
          width="70"
        />
      </el-table>
    </div>
  </div>
</template>
<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';
  import { ElTable } from 'element-plus';
  import { Search } from '@element-plus/icons-vue';

  interface Props {
    stackList: any[];
    breakPointList: any[];
    variableList: any[];
    variableHighLight?: number;
  }
  const props = withDefaults(defineProps<Props>(), {
    stackList: () => [],
    breakPointList: () => [],
    variableList: () => [],
  });
  interface ChangeBreakPoint {
    type: boolean;
    line: number[];
  }
  const emit = defineEmits<{
    (e: 'changeBreakPoint', value: ChangeBreakPoint): void;
  }>();

  const multipleTableRef = ref<InstanceType<typeof ElTable>>();
  interface Table2 {
    id: number;
    lineno: string;
    query: string;
    enable: boolean;
  }
  const multipleSelection = ref<Table2[]>([]);
  const handleSelectionChange = (val: Table2[]) => {
    multipleSelection.value = val;
  };

  // enable / disabled
  const handleChangeStatus = (status: boolean) => {
    const result: ChangeBreakPoint = {
      type: status,
      line: [],
    };
    multipleSelection.value.forEach((item) => {
      if (item.enable !== status) {
        item.enable = status;
        result.line.push(parseInt(item.lineno));
      }
    });
    emit('changeBreakPoint', result);
    multipleTableRef.value!.clearSelection();
  };

  // table3 hightline
  const showVariableInput = ref(false);
  const filterVariableText = ref('');
  const variableList = computed(() => {
    return filterVariableText.value
      ? props.variableList.filter((item) => item.varname.indexOf(filterVariableText.value) > -1)
      : props.variableList;
  });
  const singleTableRef = ref<InstanceType<typeof ElTable>>();
  watch(
    () => props.variableList,
    () => {
      filterVariableText.value == '';
    },
  );
</script>
<style lang="scss" scoped>
  .filter-input {
    margin-bottom: 5px;
  }
  .flex-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .icon-pointer {
    cursor: pointer;
    :hover {
      color: var(--normal-color);
    }
  }
  .border-bottom-input {
    box-shadow: none;
    height: auto;
    :deep(.el-input__wrapper) {
      box-shadow: none;
      .el-input__inner {
        box-shadow: 0 1px 0 0 var(--el-input-border-color);
      }
    }
  }
  :deep(.el-table) {
    .el-table__body tr:hover > td.el-table__cell {
      background: none;
    }
    .el-table__body tr.current-row > td.el-table__cell {
      background-color: rgba(255, 255, 0, 0.7);
    }
    .el-table__cell {
      padding: 2px 0;
    }
    .cell {
      padding: 0 3px;
      line-height: 20px;
    }
    .el-table-column--selection > .cell {
      height: 20px;
    }
  }
  .DebugPane {
    width: 370px;
    flex-shrink: 0;
    padding-left: 15px;
  }
  .pane {
    &:not(:last-child) {
      margin-bottom: 15px;
    }
    .title-wrapper {
      display: flex;
      justify-content: space-between;
      margin-bottom: 5px;
    }
    .title {
      font-weight: bold;
      .el-icon {
        margin-right: 5px;
        font-size: 14px;
      }
    }
  }
</style>
