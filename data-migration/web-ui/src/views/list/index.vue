<template>
  <div class="list-container">
    <div class="search-con">
      <a-form :model="form" layout="inline">
        <a-form-item field="taskName" style="margin-left: -17px;">
          <a-input v-model="form.taskName" allow-clear placeholder="请输入任务名称" style="width: 200px;" @change="getList"></a-input>
        </a-form-item>
        <a-form-item field="createUser" style="margin-left: -17px;">
          <a-select v-model="form.createUser" placeholder="请选择创建人" allow-clear style="width: 150px;" @change="getList">
            <a-option v-for="item in userData" :key="item" :value="item">{{ item }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="execStatus" style="margin-left: -17px;">
          <a-select v-model="form.execStatus" placeholder="请选择状态" allow-clear style="width: 150px;" @change="getList">
            <a-option :value="0">未启动</a-option>
            <a-option :value="1">迁移中</a-option>
            <a-option :value="2">已完成</a-option>
          </a-select>
        </a-form-item>
        <a-form-item field="execTime" style="margin-left: -17px;">
          <a-range-picker v-model="form.execTime" :placeholder="['执行开始时间', '执行结束时间']" style="width: 250px;" @change="getList" />
        </a-form-item>
        <a-form-item field="finishTime" style="margin-left: -17px;">
          <a-range-picker v-model="form.finishTime" :placeholder="['完成开始时间', '完成结束时间']" style="width: 250px;" @change="getList" />
        </a-form-item>
        <a-form-item>
          <a-button type="outline" @click="getList">
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
    <div class="btn-con">
      <a-space>
        <a-button type="primary" @click="createTask">
          <template #icon>
            <icon-plus />
          </template>
          <template #default>创建数据迁移任务</template>
        </a-button>
        <a-button status="success" @click="getList">
          <template #icon>
            <icon-sync />
          </template>
          <template #default>刷新</template>
        </a-button>
        <a-button status="warning" @click="deleteMore">
          <template #icon>
            <icon-delete />
          </template>
          <template #default>批量删除</template>
        </a-button>
      </a-space>
    </div>
    <div class="table-con">
      <a-table :loading="loading" row-key="id" :data="tableData" :row-selection="rowSelection" v-model:selectedKeys="selectedKeys" :bordered="false" :stripe="!currentTheme" :hoverable="!currentTheme" :pagination="pagination" @page-change="pageChange">
        <template #columns>
          <a-table-column title="任务名称" data-index="taskName" :width="220" fixed="left" ellipsis tooltip></a-table-column>
          <a-table-column title="创建人" data-index="createUser" :width="100"></a-table-column>
          <a-table-column title="执行状态" data-index="execStatus" :width="120">
            <template #cell="{ record }">
              {{ execStatusMap(record.execStatus) }}
            </template>
          </a-table-column>
          <a-table-column title="进度" data-index="execProgress" :width="150">
            <template #cell="{ record }">
              <a-progress :percent="record.execStatus === 2 ? 1 : (+record.execProgress || 0)" />
            </template>
          </a-table-column>
          <a-table-column title="已执行时长" :width="150">
            <template #cell="{ record }">
              {{ calcTime(record) }}
            </template>
          </a-table-column>
          <a-table-column title="任务创建时间" data-index="createTime" :width="200"></a-table-column>
          <a-table-column title="执行开始时间" data-index="execTime" :width="200"></a-table-column>
          <a-table-column title="任务完成时间" data-index="finishTime" :width="200"></a-table-column>
          <a-table-column title="操作" align="center" :width="300" fixed="right">
            <template #cell="{ record }">
              <a-button
                size="mini"
                type="text"
                @click="goDetail(record)"
              >
                <template #icon>
                  <icon-edit />
                </template>
                <template #default>详情</template>
              </a-button>
              <a-button
                v-if="record.execStatus === 1"
                size="mini"
                type="text"
                @click="stopTask(record)"
              >
                <template #icon>
                  <icon-pause />
                </template>
                <template #default>结束迁移</template>
              </a-button>
              <a-button
                v-if="record.execStatus === 0"
                :loading="record.startLoading"
                size="mini"
                type="text"
                @click="startTask(record)"
              >
                <template #icon>
                  <icon-play-arrow />
                </template>
                <template #default>启动</template>
              </a-button>
              <a-popconfirm v-if="record.execStatus === 0 || record.execStatus === 2" content="你确认删除此任务吗？" @ok="deleteTheTask(record)">
                <a-button
                  size="mini"
                  type="text"
                >
                  <template #icon>
                    <icon-delete />
                  </template>
                  <template #default>删除</template>
                </a-button>
              </a-popconfirm>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { list, start, stop, deleteTask, userList } from '@/api/list'
import dayjs from 'dayjs'
import useTheme from '@/hooks/theme'

const { currentTheme } = useTheme()

const loading = ref(true)

const form = reactive({
  taskName: undefined,
  createUser: undefined,
  execStatus: undefined,
  execTime: undefined,
  finishTime: undefined
})

const userData = ref([])

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10
})

const tableData = ref([])
const selectedKeys = ref([])
const rowSelection = reactive({
  type: 'checkbox',
  showCheckedAll: true,
  onlyCurrent: false
})

// status map
const execStatusMap = (status) => {
  const maps = {
    0: '未启动',
    1: '迁移中',
    2: '已完成'
  }
  return maps[status]
}

// get task list data
const getList = () => {
  loading.value = true
  list({
    taskName: form.taskName,
    createUser: form.createUser,
    execStatus: form.execStatus,
    execStartTime: form.execTime ? form.execTime[0] + ' 00:00:00' : undefined,
    execEndTime: form.execTime ? form.execTime[1] + ' 23:59:59' : undefined,
    finishStartTime: form.finishTime ? form.finishTime[0] + ' 00:00:00' : undefined,
    finishEndTime: form.finishTime ? form.finishTime[1] + ' 23:59:59' : undefined,
    ...queryParams
  }).then(res => {
    loading.value = false
    tableData.value = res.rows.map(item => ({ ...item, disabled: item.execStatus === 1 }))
    pagination.total = res.total
  }).catch(() => {
    loading.value = false
  })
}

const resetQuery = () => {
  form.taskName = undefined
  form.createUser = undefined
  form.execStatus = undefined
  form.execTime = undefined
  form.finishTime = undefined
  getList()
}

const pageChange = current => {
  queryParams.pageNum = current
  pagination.current = current
  getList()
}

const calcTime = row => {
  if (row.execStatus === 1 || row.execStatus === 2) {
    const seconds = row.finishTime ? dayjs(row.finishTime).diff(dayjs(row.execTime), 'seconds') : dayjs().diff(dayjs(row.execTime), 'seconds')
    const hour = parseInt(seconds / 3600)
    const minute = parseInt((seconds - hour * 3600) / 60)
    return `${hour ? hour + '小时' : ''} ${minute ? minute + '分钟' : ''}`
  } else {
    return ''
  }
}

// jump to the page of task-config
const createTask = () => {
  window.$wujie?.props.methods.jump({
    name: `Static-pluginData-migrationTaskConfig`
  })
}

// start task
const startTask = async row => {
  try {
    row.startLoading = true
    await start(row.id)
    Message.success('Start success')
    row.startLoading = false
  } catch (e) {
    row.startLoading = false
  }
  getList()
}

// stop task
const stopTask = async row => {
  await stop(row.id)
  Message.success('Stop success')
  getList()
}

// remove a task
const deleteTheTask = async row => {
  await deleteTask(row.id)
  Message.success('Delete success')
  getList()
}

// remove more tasks
const deleteMore = async () => {
  if (!selectedKeys.value.length) {
    Message.error('Please select at least one piece of data')
    return
  }
  await deleteTask(selectedKeys.value.join(','))
  Message.success('Delete success')
  getList()
}

// get user data
const getUserList = () => {
  userList().then(res => {
    userData.value = res.data
  })
}

// jump to the page of task-detail
const goDetail = row => {
  if (row.execStatus === 0) {
    window.$wujie?.props.methods.jump({
      name: `Static-pluginData-migrationTaskConfig`,
      query: {
        id: row.id
      }
    })
  } else {
    window.$wujie?.props.methods.jump({
      name: `Static-pluginData-migrationTaskDetail`,
      query: {
        id: row.id
      }
    })
  }
}

onMounted(() => {
  getList()
  getUserList()
  window.$wujie?.bus.$on('data-migration-update', () => {
    getList()
  })
})
</script>

<style lang="less" scoped>
.list-container {
  position: relative;
  min-height: calc(100vh - 114px);
  .search-con {
    padding: 16px 20px 10px;
    display: flex;
    justify-content: space-between;
  }
  .btn-con {
    padding: 0 20px;
  }
  .table-con {
    margin-top: 20px;
    padding: 0 20px 30px;
  }
}
</style>
