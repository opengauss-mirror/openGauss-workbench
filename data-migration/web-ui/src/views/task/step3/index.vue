<template>
  <div class="step3-container">
    <div class="warn-con">
      <a-alert>提示：DataKit本着资源利用最大化原则尽量让子任务并行执行，为每个子任务创建一个数据迁移代理（一台机器可以创建多个迁移代理），在资源不足时则等待资源顺序执行。</a-alert>
    </div>
    <div class="search-con">
      <a-form :model="form" layout="inline">
        <a-form-item field="ip" style="margin-left: -17px;">
          <a-input v-model.trim="form.ip" allow-clear placeholder="请输入物理机IP" style="width: 160px;" @change="getFilterData"></a-input>
        </a-form-item>
        <a-form-item field="hostname" style="margin-left: -17px;">
          <a-input v-model.trim="form.hostname" allow-clear placeholder="请输入物理机名称" style="width: 160px;" @change="getFilterData"></a-input>
        </a-form-item>
        <a-form-item field="a" style="margin-left: -17px;">
          <a-input v-model.trim="form.a" allow-clear placeholder="请输入cpu核心数" style="width: 160px;" @change="getFilterData"></a-input>
        </a-form-item>
        <a-form-item field="b" style="margin-left: -17px;">
          <a-input v-model.trim="form.b" allow-clear placeholder="请输入内存大小" style="width: 160px;" @change="getFilterData"></a-input>
        </a-form-item>
        <a-form-item field="c" style="margin-left: -17px;">
          <a-input v-model.trim="form.c" allow-clear placeholder="请输入硬盘空闲大小" style="width: 160px;" @change="getFilterData"></a-input>
        </a-form-item>
        <a-form-item style="margin-left: -17px;">
          <a-button type="outline" @click="getFilterData">
            <template #icon>
              <icon-search />
            </template>
            <template #default>查询</template>
          </a-button>
          <a-button style="margin-left: 10px;" @click="resetQuery">
            <template #icon>
              <icon-sync />
            </template>
            <template #default>重置</template>
          </a-button>
        </a-form-item>
      </a-form>
    </div>
    <div class="table-con">
      <div class="select-info-con">
        <div class="select-tips">
          <span class="tips-item">迁移子任务：<b>{{ props.subTaskConfig.length }}</b>个</span>
          <span class="tips-item">已选择机器：<b>{{ selectedKeys.length }}</b>台</span>
        </div>
        <div class="refresh-con">
          <a-link @click="getHostsData">
            <icon-refresh />
            <span class="btn-txt">刷新</span>
          </a-link>
        </div>
      </div>
      <a-table row-key="hostId" :data="tableData" :row-selection="rowSelection" v-model:selectedKeys="selectedKeys" :bordered="false" :stripe="!currentTheme" :hoverable="!currentTheme" :pagination="pagination" @page-change="pageChange" @selection-change="selectionChange">
        <template #columns>
          <a-table-column title="物理机IP" data-index="publicIp" fixed="left" :width="150"></a-table-column>
          <a-table-column title="物理机名称+OS" data-index="hostname" :width="200" ellipsis tooltip></a-table-column>
          <a-table-column title="配置信息" :width="300" ellipsis tooltip>
            <template #cell="{ record }">
              {{ record.os ? '系统：' + record.os + ',' : '' }}
              {{ record.os ? 'CPU架构：' + record.cpuArch : '' }}
            </template>
          </a-table-column>
          <a-table-column title="是否已安装portal" data-index="installPortalStatus" align="center" :width="150">
            <template #cell="{ record }">
              <span v-if="record.installPortalStatus !== 0">{{ statusMap(record.installPortalStatus) }}</span>
              <a-button
                v-if="record.installPortalStatus === 0"
                size="mini"
                type="text"
                @click="handleInstall(record)"
              >
                <template #icon>
                  <icon-play-arrow />
                </template>
                <template #default>开始安装</template>
              </a-button>
              <a-button
                v-if="record.installPortalStatus === 10"
                size="mini"
                type="text"
                @click="handleDownloadLog(record)"
              >
                <template #icon>
                  <icon-download />
                </template>
                <template #default>日志</template>
              </a-button>
            </template>
          </a-table-column>
          <a-table-column title="正在执行子任务数" data-index="tasks" align="center" :width="150">
            <template #cell="{ record }">
              {{ record.tasks.length }}
            </template>
          </a-table-column>
          <a-table-column title="正在执行的子任务" data-index="tasks" :width="150">
            <template #cell="{ record }">
              {{ record.tasks.map(item => `#${item.id}`).join(', ')}}
            </template>
          </a-table-column>
          <a-table-column title="最大子任务数" data-index="d" :width="150"></a-table-column>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, toRaw, onBeforeUnmount } from 'vue'
import { hostsData, downloadEnvLog, installPortal } from '@/api/task'
import useTheme from '@/hooks/theme'

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
  a: undefined,
  b: undefined,
  c: undefined
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

const statusMap = (status) => {
  const maps = {
    0: '未安装',
    1: '安装中',
    2: '已安装',
    10: '安装失败'
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
    return flag
  })
  pagination.total = tableData.value.length
}

const selectionChange = (rowKey) => {
  emits('syncHost', rowKey)
}

let timer = null

// start install
const handleInstall = row => {
  installPortal(row.hostId).then(() => {
    getHostsData()
  })
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
      a.download = row.hostname
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(herf)
    }
  })
}

const getHostsData = () => {
  timer && clearTimeout(timer)
  loading.value = true
  hostsData().then(res => {
    loading.value = false
    tableData.value = res.data.map(item => ({ ...item, disabled: item.installPortalStatus !== 2 }))
    originData.value = JSON.parse(JSON.stringify(res.data))
    pagination.total = res.data.length
    timer = setTimeout(() => {
      getHostsData()
    }, 5000)
  }).catch(() => {
    loading.value = false
    timer && clearTimeout(timer)
    timer = null
  })
}

const resetQuery = () => {
  form.ip = undefined
  form.hostname = undefined
  form.a = undefined
  form.b = undefined
  form.c = undefined
  getFilterData()
}

onMounted(() => {
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
  }
}
</style>
