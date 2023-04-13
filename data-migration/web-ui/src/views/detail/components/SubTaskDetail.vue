<template>
  <a-drawer
    v-model:visible="visible"
    width="60%"
    :footer="false"
    :unmount-on-close="true"
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
      <a-spin v-if="loading" :loading="loading" tip="数据加载中">
        <div class="loading-con"></div>
      </a-spin>
      <div v-if="descData.length" class="task-desc-con">
        <a-descriptions :data="descData" layout="inline-horizontal" :column="5" bordered />
      </div>
      <div v-if="tabActive === 1 && subTaskInfo.migrationModelId === 1" class="progress-con">
        <span class="progress-info">进度</span>
        <a-progress :status="subTaskInfo.execStatus === 500 ? 'danger' : 'success'" size="large" :percent="subTaskInfo.execStatus === 100 ? 1 : (subTaskInfo.migrationProcess || 0)" />
      </div>
      <div v-if="tabActive === 1 && subTaskInfo.migrationModelId === 1" class="table-con">
        <big-data-list :full-data="fullData" :sub-task-info="subTaskInfo" :record-counts="recordCounts" />
      </div>
      <div v-if="tabActive === 1 && subTaskInfo.migrationModelId === 2" class="record-con">
        <div class="record-title">在线迁移过程记录</div>
        <div class="record-list">
          <a-steps :default-current="Object.keys(statusRecords).length + 1" type="dot" direction="vertical">
            <a-step v-for="(value, key) in statusRecords" :key="key">
              <div class="record-item-hd">
                <span class="hd-info">{{ recordsMap(key) }}</span>
                <span class="hd-time">by {{ value[0].operateUser || '-' }}, {{ value[0].operateTime || '-' }}</span>
              </div>
              <a-card hoverable>
                <div class="record-item-con">
                  <div v-for="item in value" :key="item.id" class="record-item">
                    <div class="record-item-info">
                      <span class="info">
                        {{ execSubStatusMap(item.statusId) }}
                        <i v-if="item.statusId === 2" @click="globalVisible = !globalVisible">{{ globalVisible ? '收起' : '查看' }}</i>
                        <i v-if="item.statusId === 8" @click="increaseVisible = !increaseVisible">{{ increaseVisible ? '收起' : '查看' }}</i>
                      </span>
                      <span class="time">{{ item.createTime }}</span>
                    </div>
                    <div v-if="item.statusId === 2 && globalVisible" class="table-con">
                      <big-data-list :full-data="fullData" :sub-task-info="subTaskInfo" :record-counts="recordCounts" />
                    </div>
                    <div v-if="item.statusId === 8 && increaseVisible" class="list-con">
                      <div class="list-item-con">
                        <div class="list-title">
                          <div class="list-title-l">
                            <icon-info-circle size="15" />
                            <span>累计增量迁移对象数：{{ increaseData.count }} 条</span>
                          </div>
                          <div class="list-title-r">
                            <span>{{ increaseData.createTime }}</span>
                          </div>
                        </div>
                        <div class="list-info-em">
                          <span>增量抽取速度：{{ increaseData.sourceSpeed || 0 }} 条/s</span>
                        </div>
                        <div class="list-info-em">
                          <span>增量写入速度：{{ increaseData.sinkspeed || 0 }} 条/s</span>
                        </div>
                        <div class="list-info">
                          <span>剩余待写入数据：{{ increaseData.rest }} 条</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </a-card>
            </a-step>
          </a-steps>
        </div>
      </div>
      <div v-if="tabActive === 2 && !loading" class="log-con">
        <a-list>
          <a-list-item v-for="item in logData" :key="item.url">
            <div class="log-detail-info">
              <span>{{ item.name }}</span>
              <span class="desc">{{ logsMap(item.name) }}</span>
            </div>
            <template #actions>
              <a-button type="text" size="mini" @click="handleDownloadLog(item.url)">
                <template #icon>
                  <icon-download />
                </template>
                下载日志
              </a-button>
            </template>
          </a-list-item>
        </a-list>
      </div>
    </div>
  </a-drawer>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { subTaskDetail, downloadLog } from '@/api/detail'
import BigDataList from './BigDataList.vue'
import dayjs from 'dayjs'

const props = defineProps({
  open: Boolean,
  taskInfo: Object,
  subTaskId: [String, Number],
  tab: [String, Number]
})

const emits = defineEmits(['update:open'])

const loading = ref(false)
const visible = ref(false)
const tabActive = ref(1)
const globalVisible = ref(false)
const increaseVisible = ref(false)
const subTaskInfo = ref({})
const descData = ref([])
const increaseData = ref({})

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

// record map
const recordsMap = (key) => {
  const maps = {
    1: '启动迁移',
    2: '停止增量',
    3: '启动反向',
    100: '结束迁移'
  }
  return maps[key]
}

// log map
const logsMap = (key) => {
  const maps = {
    'sink.log': '数据校验sink端日志',
    'source.log': '数据校验source端日志',
    'check.log': '数据校验check端日志',
    'full_migration.log': '全量迁移日志',
    'server.log': 'zookeeper+kafka日志',
    'schema-registry.log': 'schema-registry日志',
    'connect.log': '增量迁移日志',
    'connect_source.log': '增量迁移source端日志',
    'connect_sink.log': '增量迁移sink端日志',
    'reverse_connect.log': '反向迁移日志',
    'reverse_connect_source.log': '反向迁移source端日志',
    'reverse_connect_sink.log': '反向迁移sink端日志',
    'error.log': '执行时错误日志'
  }
  return `(${maps[key]})` || ''
}

watch(visible, (v) => {
  emits('update:open', v)
})

watch(() => props.open, (v) => {
  if (v) {
    tabActive.value = props.tab
    globalVisible.value = false
    increaseVisible.value = false
    increaseData.value = {}
    logData.value = []
    getSubTaskDetail()
  } else {
    globalVisible.value = false
    increaseVisible.value = false
    increaseData.value = {}
    subTaskInfo.value = {}
    descData.value = []
    logData.value = []
    loading.value = false
  }
  visible.value = v
})

const tabChange = tab => {
  tabActive.value = tab
}

const fullData = ref()
const recordCounts = ref()
const statusRecords = ref({})
const logData = ref([])

const handleDownloadLog = (url) => {
  downloadLog(subTaskInfo.value.id, { filePath: url }).then(res => {
    if (res) {
      const blob = new Blob([res], {
        type: 'text/plain'
      })
      const a = document.createElement('a')
      const URL = window.URL || window.webkitURL
      const herf = URL.createObjectURL(blob)
      a.href = herf
      a.download = `#${subTaskInfo.value.id}_${url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'))}_${dayjs().format('YYYYMMDDHHmmss')}.log`
      document.body.appendChild(a)
      a.click()
      document.body.removeChild(a)
      window.URL.revokeObjectURL(herf)
    }
  })
}

const getSubTaskDetail = () => {
  loading.value = true
  subTaskDetail(props.subTaskId).then(res => {
    loading.value = false
    subTaskInfo.value = res.data.task
    const seconds = subTaskInfo.value.finishTime ? dayjs(subTaskInfo.value.finishTime).diff(dayjs(subTaskInfo.value.execTime), 'seconds') : dayjs().diff(dayjs(subTaskInfo.value.execTime), 'seconds')
    const hour = parseInt(seconds / 3600)
    const minute = parseInt((seconds - hour * 3600) / 60)

    const offlineDesc = [
      {
        label: '属于任务：',
        value: props.taskInfo.taskName,
        span: 3
      },
      {
        label: '创建时间：',
        value: subTaskInfo.value.createTime,
        span: 2
      },
      {
        label: '源库名：',
        value: subTaskInfo.value.sourceDb,
        span: 3
      },
      {
        label: '目的库名：',
        value: subTaskInfo.value.targetDb,
        span: 2
      },
      {
        label: '执行开始时间：',
        value: subTaskInfo.value.execTime,
        span: 3
      },
      {
        label: '已执行：',
        value: `${hour ? hour + '小时' : ''} ${minute ? minute + '分钟' : ''}`,
        span: 2
      },
      {
        label: '迁移情况：',
        value: `总迁移对象数量：${(res.data.totalWaitCount || 0) + (res.data.totalRunningCount || 0) + (res.data.totalFinishCount || 0) + (res.data.totalErrorCount || 0)}，未开始：${res.data.totalWaitCount || 0}，迁移中：${res.data.totalRunningCount || 0}，成功：${res.data.totalFinishCount || 0}，失败：${res.data.totalErrorCount || 0}`,
        span: 5
      }
    ]

    const onlineDesc = [
      {
        label: '属于任务：',
        value: props.taskInfo.taskName,
        span: 3
      },
      {
        label: '创建时间：',
        value: subTaskInfo.value.createTime,
        span: 2
      },
      {
        label: '全量迁移：',
        value: res.data.fullProcess ? `总迁移对象：${(res.data.totalWaitCount || 0) + (res.data.totalRunningCount || 0) + (res.data.totalFinishCount || 0) + (res.data.totalErrorCount || 0)}，未开始：${res.data.totalWaitCount || 0}，迁移中：${res.data.totalRunningCount || 0}，成功：${res.data.totalFinishCount || 0}，失败：${res.data.totalErrorCount || 0}` : '未启动',
        span: 3
      },
      {
        label: '执行时间：',
        value: subTaskInfo.value.execTime,
        span: 2
      },
      {
        label: '增量迁移：',
        value: res.data.incrementalProcess ? `总迁移对象：${0}，未开始：${0}，迁移中：${0}，成功：${0}，失败：${0}` : '未启动',
        span: 3
      },
      {
        label: '源库至目的库：',
        value: `${subTaskInfo.value.sourceDb} 至 ${subTaskInfo.value.targetDb}`,
        span: 2
      },
      {
        label: '反向迁移：',
        value: res.data.reverseProcess ? `总迁移对象：${0}，未开始：${0}，迁移中：${0}，成功：${0}，失败：${0}` : '未启动',
        span: 3
      },
      {
        label: '已执行：',
        value: `${hour ? hour + '小时' : ''} ${minute ? minute + '分钟' : ''}`,
        span: 2
      }
    ]

    descData.value = res.data.task.migrationModelId === 1 ? offlineDesc : onlineDesc

    recordCounts.value = {
      table: res.data.tableCounts,
      view: res.data.viewCounts,
      function: res.data.funcCounts,
      trigger: res.data.triggerCounts,
      procedure: res.data.produceCounts
    }

    // 全量表格数据
    const fullProcessDetail = res.data.fullProcess?.execResultDetail ? JSON.parse(res.data.fullProcess?.execResultDetail) : null
    fullData.value = fullProcessDetail || null

    // 过程记录
    if (subTaskInfo.value.migrationModelId === 2) {
      statusRecords.value = res.data.statusRecords
    }

    // 增量迁移详情
    const increaseProcessDetail = res.data.incrementalProcess?.execResultDetail ? JSON.parse(res.data.incrementalProcess?.execResultDetail) : null
    if (increaseProcessDetail) {
      increaseData.value = {
        createTime: res.data.incrementalProcess.createTime,
        ...increaseProcessDetail
      }
    }

    // log
    logData.value = res.data.logs.map(item => {
      const name = item.substring(item.lastIndexOf('/') + 1)
      return {
        name,
        url: item
      }
    })
  }).catch(() => {
    loading.value = false
  })
}

onMounted(() => {
  visible.value = props.open
})
</script>

<style lang="less" scoped>
.title-con {
  width: calc(60vw - 60px);
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
  .loading-con {
    width: 60vw;
    height: 200px;
  }
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
  .table-con {
    .data-count {
      cursor: pointer;
      color: var(--color-text-3);
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
          .record-item-info {
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
          .list-con {
            border: 1px solid var(--color-border-1);
            border-radius: 3px;
            padding: 5px 10px;
            .list-item-con {
              .list-title {
                display: flex;
                align-items: center;
                justify-content: space-between;
                .list-title-l {
                  .arco-icon {
                    color: rgb(var(--primary-6));
                  }
                  span {
                    font-size: 14px;
                    margin-left: 3px;
                    color: var(--color-text-2);
                  }
                }
                .list-title-r {
                  span {
                    font-size: 12px;
                    color: var(--color-text-3);
                  }
                }
              }
              .list-info-em {
                margin-left: 18px;
                span {
                  font-size: 12px;
                  color: rgb(var(--warning-6));
                }
              }
              .list-info {
                margin-left: 18px;
                span {
                  font-size: 12px;
                  color: var(--color-text-2);
                }
              }
            }
          }
        }
      }
    }
  }
  .log-con {
    margin-top: 10px;
    .log-detail-info {
      white-space: pre-wrap;
      .desc {
        margin-left: 5px;
        color: var(--color-text-2);
      }
    }
  }
}
</style>
