<template>
  <div class="list-container common-layout" id="migrationList">
    <div class="btn-con">
      <el-button type="primary" @click="createTask">
        <template #icon>
          <icon-plus />
        </template>
        <template #default>{{ $t('list.index.5q08sf2diwc0') }}</template>
      </el-button>
      <el-button status="success" @click="getList()">
        <template #icon>
          <icon-sync />
        </template>
        <template #default>{{ $t('list.index.5q08sf2dj240') }}</template>
      </el-button>
      <!-- In the case of batch deletion -->
      <a-popconfirm :content="$t('transcribe.index.bulkdelete')" type="warning" :ok-text="$t('list.index.confirm')"
        :cancel-text="$t('list.index.cancel')" @ok="deleteSelectedTask()" class="aPopConfirmStyle">
        <el-button status="warning">
          <template #icon>
            <icon-delete />
          </template>
          <template #default>{{ $t('list.index.5q08sf2dj5g0') }}</template>
        </el-button>
      </a-popconfirm>
      <div class="switchUpdate">
        <div class="switchDesc">
          {{ switchRefreshText }}
        </div>
        <el-switch v-model="listRefresh" :active-value="true" :inactive-value="false" size="small"></el-switch>
      </div>
    </div>
    <div class="search-con">
      <fusion-search :labelOptions="labelOptions" @click-search="clickSearch"></fusion-search>
    </div>
    <div class="table-con openDesignTableArea">
      <el-table ref="listTable" :loading="loading" row-key="id" :data="tableData" :expand-row-keys="expandRowIdList"
        @selection-change="handleSelectChange" :hoverable="!currentTheme" :row-class-name="handleRowClass"
        @expand-change="handleExpand">
        <template #empty>
          <div class="o-table-empty mt24">
            <el-icon class="o-empty-icon">
              <IconEmpty />
            </el-icon>
            <div class="o-empty-label">
              {{ $t('third.index.noData') }}
            </div>
          </div>
        </template>
        <el-table-column type="expand" width="32">
          <template #default="record">
            <DetailTable v-if="record.row.expanded" :key="record.row?.id" :taskId="record.row?.id"
              :listRefresh="listRefresh" @openTmerminal="openTmerminal"></DetailTable>
          </template>
        </el-table-column>
        <el-table-column type="selection" :selectable="selectable" reverse-selection width="32"></el-table-column>
        <el-table-column :label="$t('list.index.5q08sf2dj8k0')" prop="taskName">
          <template #default="record">
              <el-button size="small" v-if="record.row.execStatus === 0" text @click="goConfig(record.row)">
                <TextTooltip class="textDetail" :content="record.row.taskName">
                  {{ record.row.taskName }}
                </TextTooltip>
              </el-button>
              <TextTooltip v-else class="textDetail" :content="record.row.taskName">
                {{ record.row.taskName }}
              </TextTooltip>
          </template>
        </el-table-column>
        <el-table-column :label="$t('list.index.5q08sf2djbk0')" prop="createUser"></el-table-column>
        <el-table-column :label="$t('list.index.5q08sf2djew0')" prop="execStatus">
          <template #default="record">
            <el-tag :type="statusColor[record.row.execStatus]" width="150"
              style="width: 88px; border-radius: 2px; font-size: 12px;">
              {{ execStatusMap(record.row.execStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="$t('list.index.5q08sf2djks0')" prop="execProgress">
          <template #default="record">
            <el-progress :percentage="record.row.execStatus === 2 ? 100 : (record.row.execProgress * 100 || 0)"
              :stroke-width="8" :min-width="400" />
          </template>
        </el-table-column>
        <el-table-column :label="$t('list.index.5q08sf2djoo0')">
          <template #default="record">
            {{ calcTime(record.row) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('list.index.5q08sf2djt00')" prop="createTime">
          <template #default="record">
            {{ record.row.createTime || '--' }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('list.index.5q08sf2dhng0')" prop="execTime">
          <template #default="record">
            {{ record.row.execTime || '--' }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('list.index.5q08sf2djvk0')" prop="finishTime">
          <template #default="record">
            {{ record.row.finishTime || '--' }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('list.index.5q08sf2djyc0')" fixed="right">
          <template #default="record">
            <el-button v-if="record.row.execStatus === 1 || record.row.execStatus === 3000" size="small" text
              @click="stopTask(record.row)">
              <template #default>{{ $t('list.index.5q08sf2dk3c0') }}</template>
            </el-button>
            <el-button v-if="record.row.execStatus === 0" :loading="record.row.startLoading" size="small" text
              @click="startTask(record.row)">
              <template #default>{{ $t('list.index.5q08sf2dk5s0') }}</template>
            </el-button>
            <a-popconfirm
              v-if="record.row.execStatus === 0 || record.row.execStatus === 2 || record.row.execStatus === 3 || record.row.execStatus === 4"
              :content="$t('list.index.5q08sf2dk800')" type="warning" :ok-text="$t('list.index.confirm')"
              :cancel-text="$t('list.index.cancel')" @ok="deleteTheTask(record.row)" class="aPopConfirmStyle">
              <el-button status="danger" text>{{ $t('list.index.5q08sf2dka40') }}</el-button>
            </a-popconfirm>
            <a-popconfirm
              v-if="record.row.execStatus === 2 || record.row.execStatus === 3 || record.row.execStatus === 4"
              :content="$t('list.index.resetWarning')" type="warning" :ok-text="$t('list.index.confirm')"
              :cancel-text="$t('list.index.cancel')" @ok="resetTask(record.row)" class="aPopConfirmStyle">
              <el-button status="danger" text>{{ $t('list.index.5q08sf2diqs0') }}</el-button>
            </a-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:currentPage="pagination.pageNum" v-model:page-size="pagination.pageSize"
        :page-sizes="[5, 10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" @current-change="currentChange"
        @size-change="sizeChange" :total="total"></el-pagination>
    </div>
    <div class="terminaStyle">
      <mac-terminal v-model:open="terminalVisible" :host="macHost" />
    </div>

  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed, watch, provide, nextTick } from 'vue';
import showMessage from '@/utils/showMessage';
import FusionSearch from '@/components/fusion-search';
import { searchType } from '@/types/searchType';
import { IconEmpty } from '@computing/opendesign-icons';
import { list, start, stop, reset, deleteTask, userList } from '@/api/list';
import dayjs from 'dayjs';
import useTheme from '@/hooks/theme';
import { useI18n } from 'vue-i18n';
import { KeyValue, PageType } from '@/types/global';
import DetailTable from '../detail/components/detailTable.vue'
import MacTerminal from '../detail/components/MacTerminal.vue'
import TextTooltip from "@/components/textTooltip/index.vue";
const { t } = useI18n();
const { currentTheme } = useTheme();

const expandRowIdList = ref<number[]>([]);
const maxExpandRow = ref(3);

const loading = ref(true);
interface userOptionType {
  label: string,
  value: string
}
const userData = ref<userOptionType[]>([]);
const listTable = ref({});
const listRefresh = ref(true);
const terminalVisible = ref(false)
const macHost = ref({})
provide('listRefresh', listRefresh);
const total = ref(0);
const pagination = reactive({
  pageNum: 1,
  pageSize: 10
});
const layout = 'total, sizes, prev, pager, next, jumper';

interface rowsType {
  createTime: string,
  createUser: string,
  currentTime: string,
  execProgress: string | null,
  execStatus: number,
  execTime: string | null,
  finishTime: string | null,
  id: number,
  taskName: string,
  startLoading?: boolean,
  expanded?: boolean,
  [key: string]: any,
}
const tableData = ref<rowsType[]>([]);
const selectedKeys = ref([]);
const rowSelection = reactive({
  type: 'checkbox',
  showCheckedAll: true,
  onlyCurrent: false
});
const switchRefreshText = computed(() => {
  return listRefresh.value ? t('components.SubTaskDetail.autoRefresh') : t('components.SubTaskDetail.stopRefresh')
})
// status map
const execStatusMap = (status: number): any => {
  const maps = {
    0: t('list.index.5q08sf2dha80'),
    1: t('list.index.5q08sf2dhek0'),
    2: t('list.index.5q08sf2dhj00'),
    3: t('list.index.taskSuccess'),
    4: t('list.index.taskFail'),
    3000: t('list.index.5q08sf2dha81')
  };
  return maps[status as keyof typeof maps];
};
const statusColor = {
  0: 'info',
  1: 'primary',
  2: 'warning',
  3: 'success',
  4: 'danger',
  3000: 'danger'
};

const labelOptions = computed(() => {
  return {
    taskName: {
      label: t('list.index.5q08sf2dj8k0'),
      value: 'taskName',
      placeholder: t('list.index.5q08sf2dez80'),
      selectType: searchType.INPUT
    },
    createUser: {
      label: t('list.index.5q08sf2djbk0'),
      value: 'createUser',
      placeholder: t('list.index.5q08sf2dgxo0'),
      selectType: searchType.SELECT,
      options: userData.value
    },
    execStatus: {
      label: t('list.index.5q08sf2djew0'),
      value: 'execStatus',
      placeholder: t('list.index.5q08sf2dh5g0'),
      selectType: searchType.SELECT,
      options: [{
        value: 0,
        label: t('list.index.5q08sf2dha80'),
      },
      {
        value: 1,
        label: t('list.index.5q08sf2dhek0'),
      },
      {
        value: 2,
        label: t('list.index.5q08sf2dhj00'),
      },
      {
        value: 3,
        label: t('list.index.taskSuccess'),
      },
      {
        value: 4,
        label: t('list.index.taskFail'),
      },
      {
        value: 3000,
        label: t('list.index.5q08sf2dha81')
      }]
    },
    execTime: {
      label: t('list.index.executeTimeRange'),
      value: 'execTime',
      placeholder: t('list.index.executeTimeRangePlaceHolder'),
      selectType: searchType.DATERANGE
    },
    finishTime: {
      label: t('list.index.completeTimeRange'),
      value: 'finishTime',
      placeholder: t('list.index.completeTimeRangePlaceHolder'),
      selectType: searchType.DATERANGE
    }
  };
});

const tableTimer = ref<any>((null))
const subTableTimer = ref<any>(null);

// Open terminal
const openTmerminal = (row: any) => {
  terminalVisible.value = true
  macHost.value = row
}

const filter = reactive<any>({
  pageNum: 1,
  pageSize: 10,
  taskName: undefined,
  createUser: undefined,
  execStatus: undefined,
  execStartTime: undefined,
  execEndTime: undefined,
  finishStartTime: undefined,
  finishEndTime: undefined,

})

const clickSearch = (params: any) => {
  const { taskName, createUser, execStatus, execTime, finishTime } = params;
  filter.taskName = taskName;
  filter.createUser = createUser;
  filter.execStatus = execStatus;
  filter.execStartTime = execTime ? dayjs(params.execTime[0]).format('YYYY-MM-DD HH:mm:ss') : undefined;
  filter.execEndTime = execTime ? `${dayjs(params.execTime[1]).format('YYYY-MM-DD')} 23:59:59` : undefined;
  filter.finishStartTime = finishTime ? dayjs(params.finishTime[0]).format('YYYY-MM-DD HH:mm:ss') : undefined;
  filter.finishEndTime = finishTime ? `${dayjs(params.finishTime[1]).format('YYYY-MM-DD')} 23:59:59` : undefined;
  getList();
}

const currentChange = (e: number) => {
  filter.pageNum = e;
  getList();
}

const sizeChange = (e: number) => {
  filter.pageSize = e;
  getList();
}

// get task list data
const getList = (loopQuery?: string) => {
  loading.value = true;
  if (loopQuery === 'loopQuery' && !listRefresh.value) {
    return;
  }
  const expandList = [...expandRowIdList.value];
  const currentSelectedIdList = [...selectedKeys.value];
  list({
    ...filter
  }).then((res: any) => {
    loading.value = false;
    total.value = res.total;
    tableData.value = res.rows.map(item => {
      // Here the resumption of the expansion
      if (expandList?.indexOf(item.id) > -1) {
        return { ...item, disabled: item.execStatus === 1, expanded: true, startLoading: false };
      } else {
        return { ...item, disabled: item.execStatus === 1, expanded: false, startLoading: false };
      }
    })
  }).catch(() => {
    loading.value = false;
  }).finally(() => {
    // Leave the interface selected
    tableData.value.forEach((row) => {
      const isSelected = currentSelectedIdList.includes(row.id)
      listTable.value?.toggleRowSelection(row, isSelected)
    })
  })
};

const selectable = (row: rowsType) => {
  return row.execStatus !== 1;
};

const calcTime = (row: rowsType) => {
  if (row.execStatus === 1 || row.execStatus === 2 || row.execStatus === 3 || row.execStatus === 4) {
    const seconds = row.finishTime ? dayjs(row.finishTime).diff(dayjs(row.execTime), 'seconds')
      : dayjs(row.currentTime).diff(dayjs(row.execTime), 'seconds')
    if (seconds < 0) {
      return '--'
    }
    const hour = Math.floor(seconds / 3600)
    const minute = Math.floor((seconds - hour * 3600) / 60)
    return `${hour ? hour + t('list.index.5q08sf2dkc80') : ''} ${minute ? minute + t('list.index.5q08sf2dkek0') : ''}`
  } else {
    return '--';
  }
};

// jump to the page of task-config
const createTask = () => {
  window.$wujie?.props.methods.jump({
    name: `Static-pluginData-migrationTaskConfig`
  });
};

// start task
const startTask = async (row: rowsType) => {
  try {
    row.startLoading = true;
    await start(row.id);
    showMessage('success', t('list.index.startSuccess'));
    let timer = setTimeout(() => {
      row.startLoading = false;
      clearTimeout(timer)
    }, 1000)
  } catch (e) {
    row.startLoading = false;
  }
  getList();
};

// stop task
const stopTask = async (row: rowsType) => {
  await stop(row.id);
  showMessage('success', t('list.index.stopSuccess'));
  getList();
};
// reset task
const resetTask = async (row: rowsType) => {
  try {
    await reset(row.id);
    // Close the subtable
    closeSubTable(row.id)
    showMessage('success', t('list.index.resetSuccess'));
    getList();
  } catch (err) {
    console.error(err, 'err')
  }
};

// remove a task
const deleteTheTask = async (row: rowsType) => {
  await deleteTask(row.id);
  window.$wujie?.bus.$emit('opengauss-close-special-tab', {
    fullPath: `/static-plugin/data-migration/taskDetail?id=${row.id}`
  });
  window.$wujie?.bus.$emit('opengauss-close-special-tab', {
    fullPath: `/static-plugin/data-migration/taskConfig?id=${row.id}`
  });
  selectedKeys.value = [];
  showMessage('success', t('list.index.delSuccess'));
  if (tableData.value.length === 1 && pagination.pageNum !== 1) {
    pagination.pageNum--;
  }
  getList();
};

const handleRowClass = (({ row, index }: any) => {
  let className = '';
  if (row?.execStatus === 0) {
    className += 'hide-expand'
  }
  return className;
})

const handleSelectChange = (selected: any) => {
  selectedKeys.value = selected.map(e => {
    return e.id
  });
};

// Expand the table children
const handleExpand = (row: rowsType, expandRows: any[]) => {
  // Start by emptying the timer
  subTableTimer.value && clearTimeout(subTableTimer.value)
  // Determine whether it has been expanded
  const idx = expandRowIdList.value.indexOf(row.id)
  // If it has already been expanded
  if (idx > -1) {
    expandRowIdList.value.splice(idx, 1);
    // Traversing the current line is closed
    tableData.value.forEach(r => {
      if (r.id === row.id) {
        r.expanded = false;
      }
    })
  } else {
    // If the current column is not expanded, check whether the maximum number of expansions has been reached
    if (expandRowIdList.value.length === maxExpandRow.value) {
      // When the number of expansions reaches the maximum value, click Expand again, you need to delete and close the first expanded column, and save the currently clicked column and expand it
      const closeRowId = expandRowIdList.value[0];
      // Delete the first expanded column of the store
      expandRowIdList.value.splice(0, 1);
      // Add the ID of the current column where the current column is stored
      expandRowIdList.value.push(row.id);
      // Iterate through collapses the first expanded column, and expands the current column
      tableData.value.forEach(r => {
        // The first closing
        if (r.id === closeRowId) {
          r.expanded = false;
        }
        if (r.id === row.id) {
          r.expanded = true;
        }
      })
    } else {
      // Traversing through the current line opens
      tableData.value.forEach(r => {
        if (r.id === row.id) {
          r.expanded = true;
        }
      })
      expandRowIdList.value.push(row.id);
    }
  }
}

// Close the row based on the id
const closeSubTable = (id: number) => {
  // Determine whether to expand
  const idx = expandRowIdList.value.findIndex(item => item === id);
  if (idx > -1) {
    // If it is expanded, it is closed and relined, and the expanded column and column are deleted
    expandRowIdList.value.splice(idx, 1)
    tableData.value.forEach(row => {
      if (row.id === id) {
        row.expanded = false;
      }
    })
  }
}

const deleteSelectedTask = () => {
  if (selectedKeys.value.length) {
    deleteMore();
  } else {
    showMessage('warning', t('list.index.delWarningText'));
  }
}

// remove more tasks
const deleteMore = async () => {
  await deleteTask(selectedKeys.value.join(','));
  selectedKeys.value.forEach(item => {
    window.$wujie?.bus.$emit('opengauss-close-special-tab', {
      fullPath: `/static-plugin/data-migration/taskDetail?id=${item}`
    });
    window.$wujie?.bus.$emit('opengauss-close-special-tab', {
      fullPath: `/static-plugin/data-migration/taskConfig?id=${item}`
    });
  });
  if (tableData.value.length === selectedKeys.value.length && pagination.pageNum !== 1) {
    pagination.pageNum--;
  }
  selectedKeys.value = [];
  showMessage('success', t('list.index.delSuccess'));
  getList();
};

// get user data
const getUserList = () => {
  userList().then((res: KeyValue) => {
    userData.value.length = 0;
    res.data?.forEach((e: string) => { userData.value.push({ label: e, value: e }); });
  });
};

// jump to the page of task-detail
const goConfig = (row: rowsType) => {
  window.$wujie?.props.methods.jump({
    name: `Static-pluginData-migrationTaskConfig`,
    query: {
      id: row.id
    }
  });
};

watch(() => listRefresh.value, () => {
  if (listRefresh.value) {
    interListTable();
  } else {
    clearListInterval();
  }
})

const interListTable = () => {
  clearListInterval();
  tableTimer.value = setInterval(() => {
    getList('loopQuery');
  }, 6000)
}

const clearListInterval = () => {
  tableTimer.value && clearInterval(tableTimer.value);
  tableTimer.value = null;
}

onMounted(() => {
  getList();
  getUserList();
  window.$wujie?.bus.$on('data-migration-update', () => {
    getList();
  });
  interListTable()
});
</script>

<style lang="less" scoped>
.list-container {
  min-height: calc(100vh - 114px);
  overflow: auto;
  overflow: auto;
  display: flex;
  flex-direction: column;
  padding: 24px 28px 20px;

  .btn-con {
    position: relative;
    min-width: 1000px;

    .switchUpdate {
      position: absolute;
      width: fit-content;
      right: 0;
      top: 0;
      display: flex;
      gap: 8px;
      z-index: 11;
      height: 32px;
      display: flex;
      align-items: center;

      .switchDesc {
        color: var(--o-text-color-primary);
      }
    }
  }

  .search-con {
    margin: 16px 0px;
  }

  ::v-deep(.table-con) {
    display: flex;
    flex-grow: 1;
    flex-direction: column;
    justify-content: space-between;
    height: 100%;

    .el-table__body-wrapper {
      overflow-x: hidden;
    }

    .tableTdTaskName {
      width: 100%;

      span {
        display: block;
        width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        text-align: left;
      }
    }

    td .cell {
      max-width: 100% !important;

      .textDetail {
        cursor: pointer;
        max-width: 100% !important;
      }
    }
  }
}

::v-deep(.hide-expand) {
  .el-table__expand-column {
    .cell {
      display: none;
    }
  }
}
</style>
