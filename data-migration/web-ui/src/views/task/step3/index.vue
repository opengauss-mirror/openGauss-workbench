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
      <div class="select-tips">
        <span class="tips-item">迁移子任务：<b>{{ props.subTaskConfig.length }}</b>个</span>
        <span class="tips-item">已选择机器：<b>{{ selectedKeys.length }}</b>台</span>
      </div>
      <a-table :loading="loading" row-key="hostId" :data="tableData" :row-selection="rowSelection" v-model:selectedKeys="selectedKeys" :bordered="false" :stripe="!currentTheme" :hoverable="!currentTheme" :pagination="pagination" @page-change="pageChange" @selection-change="selectionChange">
        <template #columns>
          <a-table-column title="物理机IP" data-index="publicIp"></a-table-column>
          <a-table-column title="物理机名称+OS" data-index="hostname"></a-table-column>
          <a-table-column title="配置信息">
            <template #cell="{ record }">
              {{ record.os ? '系统：' + record.os + ',' : '' }}
              {{ record.os ? 'CPU架构：' + record.cpuArch : '' }}
            </template>
          </a-table-column>
          <a-table-column title="最大子任务数" data-index="d"></a-table-column>
          <a-table-column title="正在执行子任务数" data-index="tasks" align="center">
            <template #cell="{ record }">
              {{ record.tasks.length }}
            </template>
          </a-table-column>
          <a-table-column title="正在执行的子任务" data-index="tasks">
            <template #cell="{ record }">
              {{ record.tasks.map(item => `#${item.id}`).join(', ')}}
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, toRaw } from 'vue'
import { hostsData } from '@/api/task'
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

const getHostsData = () => {
  loading.value = true
  hostsData().then(res => {
    loading.value = false
    tableData.value = res.data
    originData.value = JSON.parse(JSON.stringify(res.data))
    pagination.total = res.data.length
  }).catch(() => {
    loading.value = false
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
    .select-tips {
      display: flex;
      margin-bottom: 10px;
      .tips-item {
        color: var(--color-text-1);
        margin-right: 10px;
      }
    }
  }
}
</style>
