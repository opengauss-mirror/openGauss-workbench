<template>
  <div class="step3-container">
    <div class="warn-con">
      <a-alert>{{ $t('step3.index.5q093f8y7g00') }}</a-alert>
    </div>
    <div class="search-con">
      <a-form :model="form" layout="inline">
        <a-form-item field="ip" style="margin-left: -17px;">
          <a-input v-model.trim="form.ip" allow-clear :placeholder="$t('step3.index.5q093f8y8b40')"
            style="width: 160px;"></a-input>
        </a-form-item>
        <a-form-item field="hostname" style="margin-left: -17px;">
          <a-input v-model.trim="form.hostname" allow-clear :placeholder="$t('step3.index.5q093f8y8fs0')"
            style="width: 160px;"></a-input>
        </a-form-item>
        <a-form-item field="cpu" style="margin-left: -17px;">
          <a-input v-model.number="form.cpu" max-length="5" allow-clear :placeholder="$t('step3.index.5q093f8y8j40')"
            style="width: 160px;"></a-input>
        </a-form-item>
        <a-form-item field="memory" style="margin-left: -17px;">
          <a-input v-model.number="form.memory" max-length="10" allow-clear :placeholder="$t('step3.index.5q093f8y8lw0')"
            style="width: 160px;"></a-input>
        </a-form-item>
        <a-form-item field="disk" style="margin-left: -17px;">
          <a-input v-model.number="form.disk" max-length="10" allow-clear :placeholder="$t('step3.index.5q093f8y8p40')"
            style="width: 160px;"></a-input>
        </a-form-item>
        <a-form-item style="margin-left: -17px;">
          <a-button type="outline" @click="getFilterData">
            <template #icon>
              <icon-search />
            </template>
            <template #default>{{ $t('step3.index.5q093f8y8ss0') }}</template>
          </a-button>
          <a-button style="margin-left: 10px;" @click="resetQuery">
            <template #icon>
              <icon-sync />
            </template>
            <template #default>{{ $t('step3.index.5q093f8y8vk0') }}</template>
          </a-button>
        </a-form-item>
      </a-form>
    </div>
    <div class="table-con">
      <div class="select-info-con">
        <div class="select-tips">
          <span class="tips-item">{{ $t('step3.index.5q093f8y8y80') }}<b>{{ props.subTaskConfig.length
          }}</b>{{ $t('step3.index.5q093f8y91s0') }}</span>
          <span class="tips-item">{{ $t('step3.index.5q093f8y94c0') }}<b>{{ selectedKeys.length
          }}</b>{{ $t('step3.index.5q093f8y9740') }}</span>
        </div>
        <div class="refresh-con">
          <a-link @click="refreshData">
            <icon-refresh />
            <span class="btn-txt">{{ $t('step3.index.5q093f8y99s0') }}</span>
          </a-link>
        </div>
      </div>
      <a-table :loading="loading" row-key="hostId" :data="tableData" :row-selection="rowSelection"
        v-model:selectedKeys="selectedKeys" :bordered="false" :stripe="!currentTheme" :hoverable="!currentTheme"
        :pagination="pagination" @page-change="pageChange" @selection-change="selectionChange">
        <template #columns>
          <a-table-column :title="$t('step3.index.5q093f8y9ck0')" data-index="publicIp" fixed="left"
            :width="150"></a-table-column>
          <a-table-column :title="$t('step3.index.5q093f8y9fg0')" data-index="hostname" :width="200" ellipsis
            tooltip></a-table-column>
          <a-table-column :title="$t('step3.index.5q093f8y9i40')" :width="300" ellipsis tooltip>
            <template #cell="{ record }">
              {{ record.os ? $t('step3.index.5q093f8y9m80') + record.os : '' }}
              {{ record.os ? $t('step3.index.5q096184bp80') + record.cpuArch : '' }}
              {{ record.baseInfos ? $t('step3.index.5q097pi0m540', {
                a: record.baseInfos[0], b: record.baseInfos[1], c:
                  record.baseInfos[2]
              }) : '' }}
            </template>
          </a-table-column>
          <a-table-column :title="$t('step3.index.5q093f8y9p40')" data-index="installPortalStatus" align="center"
            :width="300">
            <template #cell="{ record }">
              <span v-if="record.installPortalStatus !== 0">{{ statusMap(record.installPortalStatus) }}</span>
              <a-popover v-if="record.installPortalStatus === 2">
                <span class="tips"><icon-info-circle size="15" /></span>
                <template #content>
                  <p>{{ $t('step3.index.5q094h74md80') }}: {{ record.installUser || '-' }}</p>
                  <p>{{ $t('step3.index.5q094raosqg0') }}: {{ record.installPath || '-' }}</p>
                </template>
              </a-popover>
              <a-popconfirm v-if="record.installPortalStatus === 2" :content="$t('step3.index.5q093f8y9zg3')" type="warning" :ok-text="$t('step3.index.5q093f8y9zg4')" :cancel-text="$t('step3.index.5q093f8y9zg5')" @ok="handleDelete(record)">
                <a-button size="mini" type="text">
                  <template #icon>
                    <icon-delete />
                  </template>
                  <template #default>{{ $t('step3.index.5q093f8y9zg1') }}</template>
                </a-button>
              </a-popconfirm>
              <a-button v-if="record.installPortalStatus === 0" size="mini" type="text" @click="handleInstall(record)">
                <template #icon>
                  <icon-play-arrow />
                </template>
                <template #default>{{ $t('step3.index.5q093f8y9rs0') }}</template>
              </a-button>
              <a-button v-if="record.installPortalStatus === 10" size="mini" type="text"
                @click="handleDownloadLog(record)">
                <template #icon>
                  <icon-download />
                </template>
                <template #default>{{ $t('step3.index.5q093f8y9us0') }}</template>
              </a-button>
              <a-button v-if="record.installPortalStatus === 10" size="mini" type="text" @click="handleReInstall(record)">
                <template #icon>
                  <icon-play-arrow />
                </template>
                <template #default>{{ $t('step3.index.5q093f8y9zg0') }}</template>
              </a-button>
              <a-button v-if="record.installPortalStatus === 10" size="mini" type="text" @click="handleDelete(record)">
                <template #icon>
                  <icon-delete />
                </template>
                <template #default>{{ $t('step3.index.5q093f8y9zg2') }}</template>
              </a-button>
            </template>
          </a-table-column>
          <a-table-column :title="$t('step3.index.5q093f8ya2c0')" data-index="tasks" align="center" :width="150">
            <template #cell="{ record }">
              {{ record.tasks.length }}
            </template>
          </a-table-column>
          <a-table-column :title="$t('step3.index.5q093f8ya4w0')" data-index="tasks" :width="150">
            <template #cell="{ record }">
              {{ record.tasks.map(item => `#${item.id}`).join(', ') }}
            </template>
          </a-table-column>
          <a-table-column :title="$t('step3.index.5q093f8yabs0')" data-index="d" :width="150"></a-table-column>
        </template>
      </a-table>
    </div>
    <!-- portal install -->
    <portal-install v-model:open="portalDlg.visible" :mode="portalDlg.installMode" :user-id="portalDlg.userId"
      :host-id="portalDlg.curHostId" :install-path="portalDlg.installPath" @startInstall="refreshData" />
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, toRaw, onBeforeUnmount } from 'vue'
import { hostsData, downloadEnvLog, deletePortal } from '@/api/task'
import useTheme from '@/hooks/theme'
import dayjs from 'dayjs'
import PortalInstall from '../components/PortalInstall.vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const { currentTheme } = useTheme()

const props = defineProps({
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
  pageSize: 10
})
const tableData = ref([])
const originData = ref([])

const selectedKeys = ref([])
const rowSelection = reactive({
  type: 'checkbox',
  showCheckedAll: true,
  onlyCurrent: false
})

const portalDlg = reactive({
  visible: false,
  curHostId: '',
  installMode: 'install',
  installUserId: ''
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

// data filter
const getFilterData = () => {
  pagination.current = 1
  tableData.value = originData.value.filter(item => {
    let flag = true
    if (form.ip && !~item.publicIp.indexOf(form.ip)) {
      flag = false
    }
    if (form.hostname && !~item.hostname.indexOf(form.hostname)) {
      flag = false
    }
    if (form.cpu && item.baseInfos && item.baseInfos[0] !== form.cpu) {
      flag = false
    }
    if (form.memory && item.baseInfos && item.baseInfos[1] < form.memory) {
      flag = false
    }
    if (form.disk && item.baseInfos && item.baseInfos[2] < form.disk) {
      flag = false
    }
    return flag
  })
  pagination.total = tableData.value.length

  if (!form.ip && !form.hostname && !form.cpu && !form.memory && !form.disk) {
    getHostsData()
  }
}

const selectionChange = (rowKey) => {
  emits('syncHost', rowKey)
}

let timer = null

// start install
const handleInstall = row => {
  portalDlg.curHostId = row.hostId
  portalDlg.installMode = 'install'
  portalDlg.visible = true
}

// retry install
const handleReInstall = row => {
  portalDlg.curHostId = row.hostId
  portalDlg.installMode = 'reinstall'
  portalDlg.visible = true
  portalDlg.userId = row.installUserId
  portalDlg.installPath = row.installPath
}

// download log
const handleDownloadLog = row => {
  downloadEnvLog(row.hostId).then(res => {
    if (res) {
      const blob = new Blob([res], {
        type: 'text/plain'
      })
      const a = document.createElement('a')
      const URL = window.URL || window.webkitURL
      const herf = URL.createObjectURL(blob)
      a.href = herf
      a.download = `${row.hostname}_${dayjs().format('YYYYMMDDHHmmss')}.log`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(herf)
    }
  })
}

const refreshData = () => {
  loading.value = true
  getHostsData()
}

const getHostsData = () => {
  timer && clearTimeout(timer)
  hostsData().then(res => {
    loading.value = false
    if (form.ip || form.hostname || form.cpu || form.memory || form.disk) {
      timer && clearTimeout(timer)
      timer = null
    } else {
      tableData.value = res.data.map(item => ({ ...item, disabled: item.installPortalStatus !== 2 }))
      originData.value = JSON.parse(JSON.stringify(res.data))
      pagination.total = res.data.length
      timer = setTimeout(() => {
        getHostsData()
      }, 5000)
    }
  }).catch(() => {
    loading.value = false
    timer && clearTimeout(timer)
    timer = null
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

const handleDelete = (record) => {
  loading.value = true
  deletePortal(record.hostId).then(res => {
    loading.value = false
    getHostsData()
  }).catch(() => {
    loading.value = false
  })
}

onMounted(() => {
  loading.value = true
  getHostsData()
  selectedKeys.value = toRaw(props.hostData)
})

onBeforeUnmount(() => {
  timer && clearTimeout(timer)
})
</script>

<style lang="less" scoped>
.step3-container {
  .warn-con {
    width: 70%;
    margin: 0 auto;
  }

  .search-con {
    margin-top: 20px;
    padding: 0 20px;
  }

  .table-con {
    margin-top: 20px;
    padding: 0 20px 30px;

    .select-info-con {
      display: flex;
      justify-content: space-between;
      margin-bottom: 5px;

      .select-tips {
        display: flex;

        .tips-item {
          color: var(--color-text-1);
          margin-right: 10px;
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

    .tips {
      cursor: pointer;
      margin-left: 3px;
      color: var(--color-text-2);
    }
  }
}
</style>
