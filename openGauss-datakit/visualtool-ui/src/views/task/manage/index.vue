<template>
  <div class="app-container">
    <div class="main-bd">
      <div class="search-con">
        <a-form :model="form" layout="inline">
          <a-form-item field="taskName" style="margin-left: -17px;">
            <a-input v-model="form.taskName" allow-clear placeholder="请输入任务名称" style="width: 200px;" @change="getList"></a-input>
          </a-form-item>
          <a-form-item field="taskType" style="margin-left: -17px;">
            <a-select v-model="form.taskType" placeholder="请选择任务类型" allow-clear style="width: 150px;" @change="getList">
              <a-option :value="1">数据迁移</a-option>
            </a-select>
          </a-form-item>
          <a-form-item field="execStatus" style="margin-left: -17px;">
            <a-select v-model="form.execStatus" placeholder="请选择状态" allow-clear style="width: 150px;" @change="getList">
              <a-option :value="0">未执行</a-option>
              <a-option :value="1">执行中</a-option>
              <a-option :value="2">已完成</a-option>
              <a-option :value="3">执行失败</a-option>
            </a-select>
          </a-form-item>
          <a-form-item field="execStartTime" style="margin-left: -17px;">
            <a-date-picker v-model="form.execStartTime" placeholder="请选择执行时间" style="width: 150px;" @change="getList" />
          </a-form-item>
          <a-form-item field="execEndTime" style="margin-left: -17px;">
            <a-date-picker v-model="form.execEndTime" placeholder="请选择完成时间" style="width: 150px;" @change="getList" />
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
            <template #default>创建新任务</template>
          </a-button>
          <a-button status="success" @click="getList">
            <template #icon>
              <icon-sync />
            </template>
            <template #default>刷新</template>
          </a-button>
          <a-button status="warning">
            <template #icon>
              <icon-delete />
            </template>
            <template #default>批量删除</template>
          </a-button>
        </a-space>
      </div>
      <div class="table-con">
        <a-table row-key="id" :data="tableData" :row-selection="rowSelection" :bordered="false" stripe :pagination="pagination" @page-change="pageChange">
          <template #columns>
            <a-table-column title="任务名称" data-index="taskName" :width="150" fixed="left"></a-table-column>
            <a-table-column title="任务类型" data-index="taskType" :width="120">
              <template #cell="{ record }">
                {{ taskTypeMap(record.taskType) }}
              </template>
            </a-table-column>
            <a-table-column title="执行状态" data-index="execStatus" :width="120">
              <template #cell="{ record }">
                {{ execStatusMap(record.execStatus) }}
              </template>
            </a-table-column>
            <a-table-column title="任务参数" data-index="execParams" :width="200" ellipsis tooltip></a-table-column>
            <a-table-column title="进度" data-index="execProgress" :width="150">
              <template #cell="{ record }">
                <a-progress :percent="record.execProgress || 0" />
              </template>
            </a-table-column>
            <a-table-column title="任务报告" :width="100"></a-table-column>
            <a-table-column title="任务创建时间" data-index="createTime" :width="200"></a-table-column>
            <a-table-column title="任务执行时间" data-index="execTime" :width="200"></a-table-column>
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
                  size="mini"
                  type="text"
                  @click="stopTask(record)"
                >
                  <template #icon>
                    <icon-pause />
                  </template>
                  <template #default>停止</template>
                </a-button>
                <a-button
                  size="mini"
                  type="text"
                  @click="startTask(record)"
                >
                  <template #icon>
                    <icon-play-arrow />
                  </template>
                  <template #default>启动</template>
                </a-button>
                <a-popconfirm content="你确认删除此任务吗？" @ok="deleteTheTask(record)">
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
  </div>
</template>

<script setup lang="ts">
  import { reactive, ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { Message } from '@arco-design/web-vue'
  import { list, start, stop, deleteTask } from '@/api/task'

  const router = useRouter()
  const loading = ref<boolean>(true)

  const form = reactive({
    taskName: undefined,
    taskType: undefined,
    execStatus: undefined,
    execStartTime: undefined,
    execEndTime: undefined
  })

  const queryParams = reactive({
    pageNum: 1,
    pageSize: 10
  })
  const pagination = reactive({
    total: 0,
    current: 1,
    pageSize: 10
  })
  const tableData = ref<any[]>([])
  const rowSelection = reactive<any>({
    type: 'checkbox',
    showCheckedAll: true,
    onlyCurrent: false
  })

  const taskTypeMap = (type: any) => {
    const maps: any = {
      1: '数据迁移'
    }
    return maps[type]
  }

  const execStatusMap = (status: any) => {
    const maps: any = {
      0: '未执行',
      1: '执行中',
      2: '已完成',
      3: '执行失败'
    }
    return maps[status]
  }

  const getList = () => {
    list({
      taskName: form.taskName,
      taskType: form.taskType,
      execStatus: form.execStatus,
      execStartTime: form.execStartTime,
      execEndTime: form.execEndTime
    }).then((res: any) => {
      loading.value = false
      tableData.value = res.data
    })
  }

  const resetQuery = () => {
    form.taskName = undefined
    form.taskType = undefined
    form.execStatus = undefined
    form.execStartTime = undefined
    form.execEndTime = undefined
    getList()
  }

  const pageChange = (current: number) => {
    queryParams.pageNum = current
    pagination.current = current
    getList()
  }

  const createTask = () => {
    router.push({
      path: '/static-plugin/data-migration/index'
    })
  }

  const startTask = async (row: any) => {
    await start(row.id)
    Message.success('Start success')
    getList()
  }

  const stopTask = async (row: any) => {
    await stop(row.id)
    Message.success('Stop success')
    getList()
  }

  const deleteTheTask = async (row: any) => {
    await deleteTask(row.id)
    Message.success('Delete success')
    getList()
  }

  const goDetail = (row: any) => {
    router.push({
      name: 'TaskDetail',
      query: {
        id: row.id
      }
    })
  }

  onMounted(() => {
    getList()
  })
</script>

<style lang="less" scoped>
.app-container {
  .main-bd {
    position: relative;
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
      .cell-con {
        display: flex;
        align-items: center;
        justify-content: center;
        .txt {
          margin-left: 5px;
        }
      }
    }
  }
}
</style>
