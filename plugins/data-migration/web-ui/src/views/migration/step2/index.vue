<template>
  <div class="warn-con">
    <el-alert>{{ $t('step3.index.5q093f8y7g00') }}</el-alert>
  </div>
  <div class="warn-padding" />
  <div class="step3-container" id="migrationStep3">
    <div class="select-info-con">
      <div class="select-tips">
        <el-button type="primary" @click="handleBatchInstall" class="mr-s">{{ $t('step2.index.bulkInstall') }}</el-button>
        <el-popconfirm
          :title="$t('step3.index.5q096184bp84')"
          :confirm-button-text="$t('step3.index.5q096184bp85')"
          :cancel-button-text="$t('step3.index.5q096184bp86')"
          @confirm="handleBatchRemove"
        >
          <template #reference>
            <el-button type="danger" class="mr-s" :loading="removeLoading">
              {{ $t('step3.index.5q096184bp87') }}
            </el-button>
          </template>
        </el-popconfirm>
        <el-button @click="handleUploadPkg" class="mr-s">{{
            $t('step3.index.5q096184bp88')
          }}</el-button>
        <el-spin v-if="loading"></el-spin>
        <span class="tips-item"> {{ $t('step2.index.machineSelect') }} <b>
          <span class="tag-color">
            <slot> {{ selectedKeys.length }} </slot>
          </span>
        </b></span>
      </div>
      <div class="refresh-con">
        <el-link @click="refreshData" :icon="Refresh">
          <span class="btn-txt">{{ $t('step3.index.5q093f8y99s0') }}</span>
        </el-link>
        <el-link @click="refreshSearchInfo" :icon="Filter" type="primary">
          <span class="btn-txt">{{ $t('step2.index.advancedFilter') }}</span>
        </el-link>
      </div>
    </div>
    <div class="search-con" v-if="searchAreaVisible">
      <el-form :model="form" inline>
        <el-row class="form-margin">
          <el-form-item prop="ip" style="margin-left: -17px" :label="$t('step2.index.physicalIP')">
            <el-input v-model.trim="form.ip" clearable :placeholder="$t('step3.index.5q093f8y8b40')" style="width: 160px" />
          </el-form-item>
          <el-form-item prop="hostname" style="margin-left: -17px" :label="$t('step2.index.hostName')">
            <el-input v-model.trim="form.hostname" clearable :placeholder="$t('step3.index.5q093f8y8fs0')" style="width: 160px" />
          </el-form-item>
          <el-form-item prop="cpu" style="margin-left: -17px" :label="$t('step2.index.CPUcores')">
            <el-input v-model.number="form.cpu" maxlength="5" clearable :placeholder="$t('step3.index.5q093f8y8j40')" style="width: 160px" />
          </el-form-item>
          <el-form-item prop="memory" style="margin-left: -17px" :label="$t('step2.index.remainMemory')">
            <el-input v-model.number="form.memory" maxlength="10" clearable :placeholder="$t('step3.index.5q093f8y8lw0')" style="width: 160px" />
          </el-form-item>
          <el-form-item prop="disk" style="margin-left: -17px" :label="$t('step2.index.remainHarddiskCapacity')">
            <el-input v-model.number="form.disk" maxlength="10" clearable :placeholder="$t('step3.index.5q093f8y8p40')" style="width: 160px" />
          </el-form-item>
        </el-row>
        <el-row class="form-margin, flex-end">
          <el-form-item style="margin-left: -17px">
            <el-button type="primary" @click="getFilterData" >
              {{ $t('step3.index.5q093f8y8ss0') }}
            </el-button>
            <el-button style="margin-left: 10px" @click="resetQuery">
              {{ $t('step3.index.5q093f8y8vk0') }}
            </el-button>
          </el-form-item>
        </el-row>
      </el-form>
    </div>
    <div class="table-con">
      <el-table
        ref="tableRef"
        row-key="hostId"
        :data="tableData"
        v-loading="tableLoading"
        @selection-change="handleSelectionChange"
        style="width: 100%"
        border
        stripe
      >
        <el-table-column type="selection" width="55" fixed="left"  reserve-selection />
        <el-table-column
          :label="$t('step2.index.physicalIP')"
          prop="hostInfo.publicIp"
          fixed="left"
          :min-width="150"
        />
        <el-table-column
          :label="$t('step2.index.installStatus')"
          prop="installPortalStatus"
          align="left"
          :min-width="150"
          fixed="left"
          :filters="[
            { text: $t('step2.index.installYet'), value: PORTAL_INSTALL_STATUS.NOT_INSTALL },
            { text: $t('step2.index.installAlready'), value: PORTAL_INSTALL_STATUS.INSTALLED },
            { text: $t('step2.index.installFailed'), value: PORTAL_INSTALL_STATUS.FAILED },
            { text: $t('step2.index.installIng'), value: PORTAL_INSTALL_STATUS.INSTALLING },
          ]"
          :filter-method="filterTag"
          filter-placement="bottom-end"
        >
          <template #default="{ row }">
            <el-row style="align-items: center">
              <span class="status-dot"
                    :class="{ 'error-dot': row.installPortalStatus === PORTAL_INSTALL_STATUS.FAILED,
                  'success-dot': row.installPortalStatus === PORTAL_INSTALL_STATUS.INSTALLED,
                  'info-dot': row.installPortalStatus === PORTAL_INSTALL_STATUS.NOT_INSTALL ||
                  row.installPortalStatus === PORTAL_INSTALL_STATUS.INSTALLING}">
              </span>
              <span v-if="row.installPortalStatus === PORTAL_INSTALL_STATUS.NOT_INSTALL">
                {{ $t('step2.index.installYet') }}
              </span>
              <span v-else-if="row.installPortalStatus === PORTAL_INSTALL_STATUS.INSTALLED">
                {{ $t('step2.index.installAlready') }}
              </span>
              <span v-else-if="row.installPortalStatus === PORTAL_INSTALL_STATUS.FAILED">
                {{ $t('step2.index.installFailed') }}
              </span>
              <span v-else-if="row.installPortalStatus === PORTAL_INSTALL_STATUS.INSTALLING">
                {{ $t('step2.index.installing') }}
              </span>
            </el-row>
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('step2.index.hostNameOs')"
          prop="hostInfo.hostname"
          :min-width="100"
          show-overflow-tooltip
        />
        <el-table-column
          :label="$t('step2.index.system')"
          prop="hostInfo.hostInfo.os"
          :min-width="150"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{row.hostInfo.os}}
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('step2.index.architecture')"
          prop="hostInfo.hostInfo.cpuArch"
          :min-width="120"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{row.hostInfo.cpuArch}}
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('step2.index.CPUcores')"
          :min-width="120"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{row.baseInfos[0]}}
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('step2.index.remainMemory')"
          :min-width="120"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{row.baseInfos[1]}}M
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('step2.index.remainHarddiskCapacity')"
          :min-width="120"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            {{row.baseInfos[2]}}G
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('step3.index.5q093f8ya2c0')"
          prop="tasks.length"
          align="left"
          :min-width="150"
        />
        <el-table-column
          :label="$t('step3.index.5q093f8ya4w0')"
          prop="tasks"
          width="150"
        >
          <template #default="{ row }">
            {{ row.tasks.map((item) => `#${item.id}`).join(', ') }}
          </template>
        </el-table-column>
        <el-table-column
          :label="$t('step3.index.5q093f8y9p40')"
          prop="installPortalStatus"
          align="left"
          :min-width="300"
        >
          <template #default="{ row }">
            <span v-if="row.installPortalStatus !== PORTAL_INSTALL_STATUS.NOT_INSTALL">
              {{ statusMap(row.installPortalStatus) }}
            </span>

            <el-popover
              v-if="row.installPortalStatus === PORTAL_INSTALL_STATUS.INSTALLED"
              placement="top-start"
              width="300"
              trigger="hover"
            >
              <template #reference>
                <el-icon :size="15" class="tips"><InfoFilled /></el-icon>
              </template>
              <div>
                <p>
                  {{ $t('step3.index.5q094h74md80') }}:
                  {{ row.installInfo.runUser || '-' }}
                </p>
                <p>
                  {{ $t('step3.index.5q094raosqg0') }}:
                  {{ row.installInfo.installPath || '-' }}
                </p>
                <p v-if="row.installInfo.installType === INSTALL_TYPE.OFFLINE">
                  {{ $t('step3.index.5q097pi0m541') }}:
                  {{ row.installInfo.pkgName || '-' }}
                </p>
              </div>
            </el-popover>

            <template v-if="row.installPortalStatus === PORTAL_INSTALL_STATUS.INSTALLED">
              <el-button size="small" type="danger" text @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                {{ $t('step3.index.5q093f8y9zg1') }}
              </el-button>
              <el-button size="small" type="primary" text @click="handleToolsParams(row)">
                <el-icon><Edit /></el-icon>
                {{ $t('components.ToolsParamsConfig.5q0toolspar0') }}
              </el-button>
            </template>

            <el-button
              v-if="row.installPortalStatus === PORTAL_INSTALL_STATUS.NOT_INSTALL"
              size="small"
              type="primary"
              text
              @click="handleInstall(row)"
            >
              <el-icon><VideoPlay /></el-icon>
              <span class="startInstall">
                {{ $t('step3.index.5q093f8y9rs0') }}
              </span>
            </el-button>

            <template v-else-if="row.installPortalStatus === PORTAL_INSTALL_STATUS.FAILED">
              <el-button size="small" type="primary" text @click="handleDownloadLog(row)">
                <el-icon><Download /></el-icon>
                {{ $t('step3.index.5q093f8y9us0') }}
              </el-button>
              <el-button size="small" type="primary" text @click="handleReInstall(row)">
                <el-icon><VideoPlay /></el-icon>
                {{ $t('step3.index.5q093f8y9zg0') }}
              </el-button>
              <el-button size="small" type="danger" text @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>
                {{ $t('step3.index.5q093f8y9zg2') }}
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
<!--      <div class="pagination-con">-->
<!--        <el-pagination-->
<!--          v-model:current-page="pagination.current"-->
<!--          v-model:page-size="pagination.pageSize"-->
<!--          :page-sizes="[10, 20, 50, 100]"-->
<!--          :background="true"-->
<!--          layout="total, sizes, prev, pager, next, jumper"-->
<!--          :total="pagination.total"-->
<!--          @size-change="handleSizeChange"-->
<!--          @current-change="handleCurrentChange"-->
<!--        />-->
<!--      </div>-->
    </div>
    <portal-install
      v-model:open="portalDlg.visible"
      :mode="portalDlg.installMode"
      :host-id="portalDlg.curHostId"
      :install-info="portalDlg.installInfo"
      @startInstall="refreshInstallStatus"
      @pkgDeleted="handlePkgDelete"
    />
    <upload-portal-dlg v-model:open="uploadDlg.visible" />
    <batch-install-dlg
      @startInstall="refreshInstallStatus"
      v-model:open="installBatchDlg.visible"
      :host-list="installBatchDlg.hostList"
    />
    <tools-params-config
      v-model:open="toolsParamsDlg.visible"
      :host-id="toolsParamsDlg.curHostId"
    />
  </div>
</template>

<script setup>
import {inject, nextTick, onBeforeUnmount, onMounted, reactive, ref, toRaw, watch} from 'vue'
import {deletePortal, downloadEnvLog, hostsData} from '@/api/task'
import dayjs from 'dayjs'
import PortalInstall from './components/PortalInstall.vue'
import uploadPortalDlg from './components/uploadPortalDlg.vue'
import batchInstallDlg from './components/batchAddPortal.vue'
import ToolsParamsConfig from './components/ToolsParamsConfig.vue'
import {useI18n} from 'vue-i18n'
import {INSTALL_TYPE, PORTAL_INSTALL_STATUS} from '@/utils/constants'
import {Message} from '@arco-design/web-vue'
import {Delete, Download, Filter, InfoFilled, Refresh, VideoPlay} from '@element-plus/icons-vue'
import showMessage from "@/utils/showMessage";


const { t } = useI18n()

// const refreshPageData = () => {
//   pagination.current = 1
//   getFilterData()
// }

// const handleSizeChange = (val) => {
//   pagination.pageSize = val
//   getFilterData()
// }
//
// const handleCurrentChange = (val) => {
//   pagination.current = val
//   getFilterData()
// }


const changeSubmitLoading = inject('changeSubmitLoading')

const props = defineProps({
  taskBasicInfo: {
    type: Array,
    default: () => []
  },
  subTaskConfig: {
    type: Array,
    default: () => []
  },
  hostData: {
    type: Array,
    default: () => []
  }
})

const emits = defineEmits(['syncHost'])

const loading = ref(true)
const tableLoading = ref(false)
const removeLoading = ref(false)
const form = reactive({
  ip: undefined,
  hostname: undefined,
  cpu: undefined,
  memory: undefined,
  disk: undefined
})

const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 20,
  showPageSize: true
})
const tableData = ref([])
const originData = ref([])

const selectedKeys = ref([])
const portalDlg = reactive({
  visible: false,
  curHostId: '',
  installMode: 'install',
  installInfo: {}
})

const toolsParamsDlg = reactive({
  visible: false,
  curHostId: '',
})

const uploadDlg = reactive({
  visible: false
})

const installBatchDlg = reactive({
  visible: false,
  hostList: []
})

const statusMap = (status) => {
  const maps = {
    0: t('step3.index.5q093f8yae80'),
    1: t('step3.index.5q093f8yagw0'),
    2: t('step3.index.5q093f8yajg0'),
    10: t('step3.index.5q093f8yals0')
  }
  return maps[status]
}

const pageChange = (current) => {
  pagination.current = current
}

const getFilterData = () => {
  const rawOriginData = toRaw(originData.value);
  tableData.value = rawOriginData.filter(item => {
    const {
      hostInfo = {},
      baseInfos = []
    } = item;
    let flag = true;
    if (form.ip) {
      const ipMatch = hostInfo.name?.includes?.(form.ip) ?? false;
      if (!ipMatch) flag = false;
    }
    if (form.hostname) {
      const hostnameMatch = hostInfo.hostname?.includes?.(form.hostname) ?? false;
      if (!hostnameMatch) flag = false;
    }
    if (form.cpu) {
      const cpuMatch = baseInfos[0] === form.cpu;
      if (!cpuMatch) flag = false;
    }
    if (form.memory) {
      const memoryValue = parseFloat(baseInfos[1]) || 0;
      if (memoryValue < form.memory) flag = false;
    }
    if (form.disk) {
      const diskValue = parseFloat(baseInfos[2]) || 0;
      if (diskValue < form.disk) flag = false;
    }

    return flag;
  });
  pagination.total = tableData.value.length;
  if (!form.ip && !form.hostname && !form.cpu && !form.memory && !form.disk) {
    getHostsData()
  }
}
const tableRef = ref(null)

watch(() => tableData.value, (newData) => {
  nextTick(() => {
    // 确保表格渲染完成
    setTimeout(() => {
      newData.forEach(row => {
        const isSelected = selectedKeys.value.includes(row.hostId)
        tableRef.value?.toggleRowSelection(row, isSelected)
      })
    }, 150)
  })
}, { deep: true })
const handleSelectionChange = (selection) => {
  selectedKeys.value = selection.map(row => row.hostId)
  emits('syncHost', selection)
}
const selectionChange = () => {
  nextTick(() => {
    const rows = []
    selectedKeys.value.map((item) => rows.push(item))
    emits('syncHost', rows)
  })
}

const findHostsFromTableByStatus = (keys, status) => {
  if (keys.length > 0) {
    const selectData = tableData.value.filter(
      (item) => keys.indexOf(item.hostId) > -1
    )
    if (selectData.length > 0) {
      const hostList = selectData.filter(
        (item) => item.installPortalStatus === status
      )
      return hostList
    }
  } else {
    return []
  }
}

let timer = null

// start install
const handleToolsParams = (row) => {
  toolsParamsDlg.curHostId = row.hostInfo.hostId
  toolsParamsDlg.visible = true
}

// start install
const handleInstall = (row) => {
  portalDlg.curHostId = row.hostInfo.hostId
  portalDlg.installMode = 'install'
  portalDlg.visible = true
  portalDlg.installInfo = row.installInfo
}

const handleReInstall = (row) => {
  portalDlg.curHostId = row.hostInfo.hostId
  portalDlg.installMode = 'reinstall'
  portalDlg.visible = true
  portalDlg.installInfo = row.installInfo
}

const handleDownloadLog = (row) => {
  downloadEnvLog(row.hostInfo.hostId).then((res) => {
    if (res) {
      const blob = new Blob([res], {
        type: 'text/plain'
      })
      const a = document.createElement('a')
      const URL = window.URL || window.webkitURL
      const herf = URL.createObjectURL(blob)
      a.href = herf
      a.download = `${row.hostInfo.hostname}_${dayjs().format(
        'YYYYMMDDHHmmss'
      )}.log`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(herf)
    }
  })
}

const refreshData = (hostIdList) => {
  getHostsData()
}
const searchAreaVisible = ref(false)
const refreshSearchInfo = (hostIdList) => {
  searchAreaVisible.value = !searchAreaVisible.value
  resetQuery()
}

const getHostsData = () => {
  timer && clearTimeout(timer)
  loading.value = true
  hostsData()
    .then((res) => {
      loading.value = false
      if (form.ip || form.hostname || form.cpu || form.memory || form.disk) {
        timer && clearTimeout(timer)
        timer = null
      } else {
        tableData.value = res.data.map((item) => ({
          ...item,
          hostId: item.hostInfo.hostId
        }))
        originData.value = JSON.parse(JSON.stringify(tableData.value))
        pagination.total = res.data.length
        selectionChange()
        timer = setTimeout(() => {
          getHostsData()
        }, 6000)
      }
    })
    .catch(() => {
      loading.value = false
      timer && clearTimeout(timer)
      timer = null
    }) .finally(() =>{
      console.log(selectedKeys.value)
    tableData.value.forEach((row) =>{
      const isSelected = selectedKeys.value.includes(row.hostId)
      tableRef.value?.toggleRowSelection(row, isSelected)
    })
  })
}

const resetQuery = () => {
  form.ip = undefined
  form.hostname = undefined
  form.cpu = undefined
  form.memory = undefined
  form.disk = undefined
  getFilterData()
}

const filterTag = (value, row) => {
  return row.installPortalStatus === value
}

const handlePkgDelete = () => {
  portalDlg.installInfo.pkgUploadPath = {}
}

const handleDelete = (record) => {
  tableLoading.value = true
  changeSubmitLoading(true)
  deletePortal(record.hostId)
    .then(() => {
      const deletedRow = tableData.value.find(
        (item) => item.hostId === record.hostId
      )
      deletedRow.installPortalStatus = PORTAL_INSTALL_STATUS.NOT_INSTALL
      nextTick(() => {
        selectionChange()
        tableLoading.value = false
        changeSubmitLoading(false)
      })
    })
    .catch(() => {
      tableLoading.value = false
      changeSubmitLoading(false)
    })
}

const handleUploadPkg = () => {
  uploadDlg.visible = true
}

const handleBatchInstall = () => {
  console.log('handleBatchInstall')
  const notInstallHosts = findHostsFromTableByStatus(
    selectedKeys.value,
    PORTAL_INSTALL_STATUS.NOT_INSTALL
  )
  if (notInstallHosts.length < selectedKeys.value.length) {
    showMessage('error',t('step2.index.bulkInstallErrmsg'))
    return
  }
  console.log(notInstallHosts)
  if (notInstallHosts.length <= 0) {
    showMessage('error',t('step2.index.bulkInstallErrmsg'))
    return
  }
  const notInstallHostInfo = []
  notInstallHosts.map((item) => notInstallHostInfo.push(item.hostInfo))
  installBatchDlg.hostList = notInstallHostInfo
  installBatchDlg.visible = true
}

const handleBatchRemove = () => {
  const failedPortal = findHostsFromTableByStatus(
    selectedKeys.value,
    PORTAL_INSTALL_STATUS.FAILED
  )
  const installedPortal = findHostsFromTableByStatus(
    selectedKeys.value,
    PORTAL_INSTALL_STATUS.INSTALLED
  )
  const targetPortalList = [...failedPortal, ...installedPortal]
  if (targetPortalList.length <= 0) {
    Message.info(t('step3.index.5q096184bp82'))
    return
  }
  const removeReqList = []
  const removeHostIdList = []
  targetPortalList.map((item) => {
    removeHostIdList.push(item.hostId)
    removeReqList.push(deletePortal(item.hostId))
  })
  removeLoading.value = true
  tableLoading.value = true
  changeSubmitLoading(true)
  Promise.all(removeReqList)
    .then((res) => {
      const deletedRows = tableData.value.filter(
        (item) => removeHostIdList.indexOf(item.hostId) > -1
      )
      deletedRows.forEach(
        (item) => (item.installPortalStatus = PORTAL_INSTALL_STATUS.NOT_INSTALL)
      )
      nextTick(() => {
        selectionChange()
        removeLoading.value = false
        tableLoading.value = false
        changeSubmitLoading(false)
      })
    })
    .catch(() => {
      removeLoading.value = false
      tableLoading.value = false
      changeSubmitLoading(false)
    })
}

const refreshInstallStatus = (hostIdList) => {
  const installRows = tableData.value.filter(
    (item) => hostIdList.indexOf(item.hostId) > -1
  )
  installRows.forEach(
    (item) => (item.installPortalStatus = PORTAL_INSTALL_STATUS.INSTALLING)
  )
  selectionChange()
}

onMounted(() => {
  selectedKeys.value = toRaw(props.taskBasicInfo.selectedHosts)
  console.log(selectedKeys.value, props.taskBasicInfo.selectedHosts)
  getHostsData()
  searchAreaVisible.value = false
})

onBeforeUnmount(() => {
  timer && clearTimeout(timer)
})
</script>

<style lang="less" scoped>
.step3-container {
  .startInstall {
    padding-right: 3px;
  }

  .warn-con {
    width: 70%;
    margin: 0 auto;
  }

  .search-con {
    margin: 24px;
    padding: 0 20px;
    background-color: var(--el-fill-color-light);
  }

  .table-con {
    margin-top: 20px;
    padding: 0 20px 30px;


    .tips {
      cursor: pointer;
      margin-left: 3px;
      color: var(--o-text-color-primary);
    }
  }
  .pagination-con {
    position: absolute;  /* 固定定位 */
    left: 5px;       /* 距离左边距 */
    bottom: 5px;     /* 距离底部距离 */
    z-index: 1000;    /* 确保在顶层 */
    padding: 10px;    /* 可选内边距 */
  }
}

.warn-padding {
  background-color: var(--el-fill-color-light);
  padding: 10px;
}

.select-info-con {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  flex-wrap: nowrap;

  .select-tips {
    display: flex;
    align-items: center;

    .tips-item {
      color: var(--color-text-1);
      margin-right: 15px;
      gap: 5px;
    }
  }

  .refresh-con {
    display: flex;
    align-items: center;

    .btn-txt {
      margin-left: 3px;
    }
  }
}

.form-margin {
  margin-left: 32px;
  margin-right: 32px
}
.flex-end {
  display: flex;
  justify-content: end;
}
.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin: 16px;
}

.tag-color {
  display: inline-flex;
  align-items: center;
  padding: 0 5px;
  height: 16px;
  background-color: var(--o-fill-color-light);
  color: black;
  border-radius: 9999px;
  font-size: 14px;
}

.info-dot {
  background-color: var(--o-color-info);
}

.error-dot {
  background-color: var(--o-color-danger);
}

.success-dot {
  background-color: var(--o-color-success);
}

</style>

