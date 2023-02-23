<template>
  <a-drawer
    v-model:visible="visible"
    width="50%"
    :footer="false"
  >
    <template #title>
      <div class="title-con">
        <div class="tab-con">
          <span class="tab-item" :class="tabActive === 1 && 'tab-item-active'" @click="tabChange(1)">子任务详情（ID：{{ props.subTaskId }}，{{ subTaskInfo.migrationModelId === 1 ? '离线模式' : '在线模式' }}）</span>
          <span class="tab-item" :class="tabActive === 2 && 'tab-item-active'" @click="tabChange(2)">日志</span>
        </div>
        <span class="task-status">
          状态：
          <b>{{ execSubStatusMap(subTaskInfo.execStatus) }}</b>
        </span>
      </div>
    </template>
    <div class="task-detail-con">
      <div class="task-desc-con">
        <a-descriptions :data="descData" layout="inline-horizontal" :column="2" table-layout="fixed" bordered />
      </div>
      <div v-if="tabActive === 1 && subTaskInfo.migrationModelId === 1" class="progress-con">
        <span class="progress-info">进度</span>
        <a-progress size="large" :percent="subTaskInfo.execStatus === 100 ? 1 : (subTaskInfo.migrationProcess || 0)" />
      </div>
      <div v-if="tabActive === 1 && subTaskInfo.migrationModelId === 1" class="table-con">
        <a-table :data="tableData" :bordered="false" stripe :pagination="false" :default-expanded-keys="['table']">
          <template #columns>
            <a-table-column :title="`源库：${subTaskInfo.sourceDb}`" data-index="name"></a-table-column>
            <a-table-column :title="`目的库：${subTaskInfo.targetDb}`" data-index="name"></a-table-column>
            <a-table-column data-index="status" align="center">
              <template #title>
                <span>迁移状态</span>
                <a-popover content-class="pop-con">
                  <span style="margin-left: 5px;cursor: pointer;"><icon-filter /></span>
                  <template #content>
                    <div class="filter-con">
                      <span>仅显示错误数据：</span>
                      <a-switch v-model="onlyError" size="small" @change="filterTableData(1)" />
                    </div>
                  </template>
                </a-popover>
              </template>
              <template #cell="{ record }">
                <a-progress v-if="record.status === 1 || record.status === 2" :percent="record.percent" />
                <icon-check-circle-fill v-if="record.status === 3 || record.status === 4 || record.status === 5" size="16" style="color: #00B429;" />
                <a-popover title="错误详情" position="tr">
                  <icon-close-circle-fill v-if="record.status === 6" size="16" style="color: #FF7D01;" />
                  <template #content>
                    <p>{{ record.msg }}</p>
                  </template>
                </a-popover>
              </template>
            </a-table-column>
            <a-table-column data-index="status" align="center">
              <template #title>
                <span>迁移校验</span>
                <a-popover content-class="pop-con">
                  <span style="margin-left: 5px;cursor: pointer;"><icon-filter /></span>
                  <template #content>
                    <div class="filter-con">
                      <span>仅显示错误数据：</span>
                      <a-switch v-model="onlyCheckError" size="small" @change="filterTableData(2)" />
                    </div>
                  </template>
                </a-popover>
              </template>
              <template #cell="{ record }">
                <a-progress v-if="record.status === 4" :percent="record.percent" />
                <icon-check-circle-fill v-if="record.status === 5" size="16" style="color: #00B429;" />
                <a-popover title="错误详情" position="tr">
                  <icon-close-circle-fill v-if="record.status === 6" size="16" style="color: #FF7D01;" />
                  <template #content>
                    <p>{{ record.msg }}</p>
                  </template>
                </a-popover>
              </template>
            </a-table-column>
          </template>
        </a-table>
      </div>
      <div v-if="tabActive === 1 && subTaskInfo.migrationModelId === 2" class="record-con">
        <div class="record-title">在线迁移过程记录</div>
        <div class="record-list">
          <a-steps :default-current="4" type="dot" direction="vertical">
            <a-step>
              <div class="record-item-hd">
                <span class="hd-info">迁移过程开始（自动）</span>
                <span class="hd-time">by admin, 2023-1-23 10:10:00</span>
              </div>
              <a-card hoverable>
                <div class="record-item-con">
                  <div class="record-item">
                    <span class="info">全量迁移：开始</span>
                    <span class="time">2023-1-23 10:10:00</span>
                  </div>
                  <div class="record-item">
                    <span class="info">全量迁移：进行中 <i @click="globalVisible = !globalVisible">查看</i></span>
                    <span class="time">2023-1-23 10:10:00</span>
                  </div>
                  <div v-if="globalVisible" class="table-con">
                    <a-table :data="tableData" :bordered="false" stripe :pagination="false">
                      <template #columns>
                        <a-table-column title="源库：user-profile-abc" data-index="a"></a-table-column>
                        <a-table-column title="目的库：user-profile-storage-abc" data-index="b"></a-table-column>
                        <a-table-column title="迁移状态" data-index="c" align="center">
                          <template #cell="{ record }">
                            <icon-check-circle-fill v-if="record.c === 'ok'" size="16" style="color: #00B429;" />
                            <a-popover title="错误详情" position="tr">
                              <icon-close-circle-fill v-if="record.c !== 'ok'" size="16" style="color: #FF7D01;" />
                              <template #content>
                                <p>Here is the text content</p>
                                <p>Here is the text content</p>
                              </template>
                            </a-popover>
                          </template>
                        </a-table-column>
                      </template>
                    </a-table>
                  </div>
                  <div class="record-item">
                    <span class="info">全量迁移：已完成</span>
                    <span class="time">2023-1-23 10:10:00</span>
                  </div>
                  <div class="record-item">
                    <span class="info">增量迁移：开始</span>
                    <span class="time">2023-1-23 10:10:00</span>
                  </div>
                  <div class="record-item">
                    <span class="info">增量迁移：进行中</span>
                    <span class="time">2023-1-23 10:10:00</span>
                  </div>
                </div>
              </a-card>
            </a-step>
            <a-step>
              <div class="record-item-hd">
                <span class="hd-info">停止增量（人工操作）</span>
                <span class="hd-time">by admin, 2023-1-23 10:10:00</span>
              </div>
              <a-card hoverable>
                <div class="record-item-con">
                  <div class="record-item">
                    <span class="info">增量迁移：已停止</span>
                    <span class="time">2023-1-23 10:10:00</span>
                  </div>
                </div>
              </a-card>
            </a-step>
            <a-step>
              <div class="record-item-hd">
                <span class="hd-info">启动反向（人工操作）</span>
                <span class="hd-time">by admin, 2023-1-23 10:10:00</span>
              </div>
              <a-card hoverable>
                <div class="record-item-con">
                  <div class="record-item">
                    <span class="info">反向迁移：开始</span>
                    <span class="time">2023-1-23 10:10:00</span>
                  </div>
                </div>
              </a-card>
            </a-step>
          </a-steps>
        </div>
      </div>
      <div v-if="tabActive === 2" class="log-con">
        <div class="log-detail-info" v-html="taskLog"></div>
      </div>
    </div>
  </a-drawer>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { subTaskDetail } from '@/api/detail'
import dayjs from 'dayjs'

const props = defineProps({
  open: Boolean,
  taskInfo: Object,
  subTaskId: [String, Number],
  tab: [String, Number]
})

const emits = defineEmits(['update:open'])

const visible = ref(false)
const tabActive = ref(1)
const taskLog = ref('暂无数据')
const globalVisible = ref(false)
const subTaskInfo = ref({})
const descData = ref([])

// sub task status map
const execSubStatusMap = (status) => {
  const maps = {
    0: '未启动',
    1: '全量迁移开始',
    2: '全量迁移进行中',
    3: '全量迁移完成',
    4: '全量校验开始',
    5: '全量校验中',
    6: '全量检验完成',
    7: '增量迁移开始',
    8: '增量迁移进行中',
    9: '增量迁移已停止',
    10: '反向迁移开始',
    11: '反向迁移进行中',
    12: '反向迁移已停止',
    100: '迁移完成',
    500: '迁移失败',
    1000: '等待资源中'
  }
  return maps[status]
}

watch(visible, (v) => {
  emits('update:open', v)
})

watch(() => props.open, (v) => {
  if (v) {
    tabActive.value = props.tab
    getSubTaskDetail()
  }
  visible.value = v
})

const tabChange = tab => {
  tabActive.value = tab
}

const tableData = ref([])
const originData = ref([])
const onlyError = ref(false)
const onlyCheckError = ref(false)

const filterTableData = (type) => {
  const oData = JSON.parse(JSON.stringify(originData.value)) || []
  if (type === 1 && onlyError.value) {
    tableData.value = oData.map(item => {
      return {
        ...item,
        children: item.children.filter(child => child.status === 6)
      }
    })
  } else if (type === 2 && onlyCheckError.value) {
    tableData.value = oData.map(item => {
      return {
        ...item,
        children: item.children.filter(child => child.status === 6)
      }
    })
  } else {
    onlyError.value = false
    onlyCheckError.value = false
    tableData.value = oData
  }
}

const getSubTaskDetail = () => {
  subTaskDetail(props.subTaskId).then(res => {
    subTaskInfo.value = res.data.task
    const seconds = subTaskInfo.value.finishTime ? dayjs(subTaskInfo.value.finishTime).diff(dayjs(subTaskInfo.value.execTime), 'seconds') : dayjs().diff(dayjs(subTaskInfo.value.execTime), 'seconds')
    const hour = parseInt(seconds / 3600)
    const minute = parseInt((seconds - hour * 3600) / 60)
    descData.value = [
      {
        label: '属于任务：',
        value: props.taskInfo.taskName
      },
      {
        label: '创建时间：',
        value: subTaskInfo.value.createTime
      },
      {
        label: '源库名：',
        value: subTaskInfo.value.sourceDb
      },
      {
        label: '目的库名：',
        value: subTaskInfo.value.targetDb
      },
      {
        label: '执行开始时间：',
        value: subTaskInfo.value.execTime
      },
      {
        label: '已执行：',
        value: `${hour ? hour + '小时' : ''} ${minute ? minute + '分钟' : ''}`
      }
    ]

    const fullProcessDetail = JSON.parse(res.data.fullProcess.execResultDetail)
    const dealData = ['table', 'view', 'function', 'trigger', 'procedure'].map(item => {
      const nameMap = {
        'table': '表',
        'view': '视图',
        'function': '函数',
        'trigger': '触发器',
        'procedure': '存储过程'
      }

      return {
        key: item,
        name: nameMap[item],
        status: '',
        msg: '',
        children: fullProcessDetail[item].map(child => {
          return {
            key: Math.random(),
            ...child
          }
        })
      }
    })
    originData.value = JSON.parse(JSON.stringify(dealData))
    tableData.value = dealData
  })
}

onMounted(() => {
  visible.value = props.open
})
</script>

<style lang="less" scoped>
.title-con {
  width: calc(50vw - 60px);
  display: flex;
  justify-content: space-between;
  align-items: center;
  .tab-con {
    .tab-item {
      cursor: pointer;
      color: var(--color-text-3);
      font-weight: 400;
      font-size: 15px;
      margin-right: 10px;
      transition: all 0.1s linear;
      &.tab-item-active {
        color: var(--color-text-1);
        font-weight: 500;
        font-size: 16px;
      }
      &:hover {
        color: var(--color-text-1);
      }
    }
  }
  .task-status {
    font-size: 14px;
    font-weight: normal;
    b {
      color: rgb(var(--primary-6))
    }
  }
}
.task-detail-con {
  .progress-con {
    margin-top: 15px;
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    .progress-info {
      white-space: nowrap;
      margin-right: 10px;
    }
  }
  .record-con {
    margin-top: 20px;
    .record-title {
      font-weight: bold;
      margin-bottom: 10px;
    }
    .record-list {
      :deep(.arco-steps-item) {
        &:last-child {
          margin-right: 16px;
        }
        .arco-steps-item-content {
          width: 100%;
          .arco-steps-item-title {
            width: 100%;
            padding-right: 8px;
          }
        }
      }
      .record-item-hd {
        display: flex;
        justify-content: space-between;
        .hd-info {
          font-size: 14px;
        }
        .hd-time {
          font-size: 12px;
          color: var(--color-text-3);
        }
      }
      .record-item-con {
        .record-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          .info {
            font-size: 13px;
            i {
              cursor: pointer;
              font-style: normal;
              color: rgb(var(--primary-6));
            }
          }
          .time {
            font-size: 12px;
            color: var(--color-text-3);
          }
        }
      }
    }
  }
  .log-con {
    margin-top: 10px;
    .log-detail-info {
      white-space: pre-wrap;
      background: #e2e2e2;
      border-radius: 4px;
      line-height: 20px;
      padding: 10px;
      max-height: calc(100vh - 200px);
      overflow-y: auto;
    }
  }
}
</style>
