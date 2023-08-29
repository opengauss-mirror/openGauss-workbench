<template>
  <div class="app-container">
    <div class="main-bd">
      <div class="title-con">
        <div class="title">{{ taskName }}</div>
        <div class="sync-btn">
          <a-button type="text" @click="getDetail">
            <template #icon>
              <icon-sync />
            </template>
          </a-button>
        </div>
      </div>
      <div class="desc-con">
        <a-descriptions :data="basicData" title="基础信息" layout="vertical" :column="4" table-layout="fixed" bordered />
      </div>
      <div class="desc-con">
        <a-descriptions :data="infoData" title="环境信息" layout="inline-vertical" :column="2" table-layout="fixed" bordered />
      </div>
      <div class="log-con">
        <div class="log-title-con">
          <div class="log-title">任务日志</div>
        </div>
        <div v-for="(item, index) in subTaskData" :key="item.mainTaskId" class="log-desc-con">
          <a-descriptions :title="`任务${index + 1}`" layout="vertical" :column="4" table-layout="fixed" bordered>
            <a-descriptions-item label="ip地址">
              <div class="info-link">
                <span>{{ item.runHost }}</span>
                <a-link><icon-link /></a-link>
              </div>
            </a-descriptions-item>
            <a-descriptions-item label="物理机名称">测试机</a-descriptions-item>
            <a-descriptions-item label="cpu">8核</a-descriptions-item>
            <a-descriptions-item label="内存">16G</a-descriptions-item>
          </a-descriptions>
          <div class="log-detail-con">
            <div class="log-steps">
              <a-steps>
                <a-step :description="`${item.sourceResourceName}-${item.sourceDb}`">
                  export from source
                  <template #icon>
                    <icon-arrow-up />
                  </template>
                </a-step>
                <a-step :description="`${item.targetResourceName}-${item.targetDb}`">
                  import to target
                  <template #icon>
                    <icon-arrow-down />
                  </template>
                </a-step>
              </a-steps>
            </div>
            <div class="log-detail-info" v-html="item.taskLog"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { useRoute } from 'vue-router'
  import { taskById } from '@/api/task'

  const route = useRoute()

  const taskName = ref<string>('')

  const basicData = ref<any[]>([])

  const infoData = ref<any[]>([
    {
      label: 'JDK版本',
      value: '11.0.251'
    },
    {
      label: 'portal版本',
      value: '1.0.0'
    }
  ])

  const subTaskData = ref<any[]>([])

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

  const getDetail = () => {
    const id: any = route.query.id
    taskById(id).then((res: any) => {
      const mainTask: any = res.data.mainTask || {}
      taskName.value = mainTask.taskName
      basicData.value = [
        {
          label: '创建时间',
          value: mainTask.createTime
        },
        {
          label: '完成时间',
          value: mainTask.finishTime
        },
        {
          label: '任务状态',
          value: execStatusMap(mainTask.execStatus)
        },
        {
          label: '任务类型',
          value: taskTypeMap(mainTask.taskType)
        }
      ]
      subTaskData.value = res.data.subTask
    })
  }

  onMounted(() => {
    getDetail()
  })
</script>

<style lang="less" scoped>
.app-container {
  .main-bd {
    position: relative;
    .title-con {
      display: flex;
      align-items: center;
      padding: 10px 20px;
      .title {
        font-size: 20px;
      }
      .sync-btn {
        margin-left: 5px;
      }
    }
    .desc-con {
      padding: 10px 20px;
    }
    .log-con {
      padding: 10px 20px;
      .log-title-con {
        padding: 20px 0 10px;
        .log-title {
          font-size: 18px;
        }
      }
      .log-desc-con {
        padding-bottom: 30px;
        margin-bottom: 20px;
        border-bottom: 1px solid #eee;
        .info-link {
          display: flex;
          align-items: center;
        }
        .log-detail-con {
          .log-steps {
            width: 500px;
            margin: 30px auto;
          }
          .log-detail-info {
            white-space: pre-wrap;
            background: #e2e2e2;
            border-radius: 4px;
            line-height: 20px;
            padding: 10px;
            max-height: 400px;
            overflow-y: auto;
          }
        }
      }
    }
  }
}
</style>
