<template>
  <div class="home-container">
    <div class="main-con">
      <div class="title-con">
        <div class="title">创建数据迁移任务</div>
        <div class="task-name-con">
          <span class="task-name"><i>*</i>任务名称</span>
          <a-input v-model.trim="taskName" placeholder="请输入任务名称" style="width: 250px;" />
        </div>
      </div>
      <a-divider />
      <div class="task-steps-con">
        <a-steps :current="currentStep">
          <a-step description="1个子任务是1个源库到1个目的库的迁移过程">选择迁移源库和目的库</a-step>
          <a-step description="确定每一个迁移过程的执行参数">配置迁移过程参数</a-step>
          <a-step description="选择合适的机器执行迁移过程">分配执行机资源</a-step>
        </a-steps>
      </div>
      <!-- step1 -->
      <step1 v-if="currentStep === 1" ref="stepOneComp" :sub-task-config="subTaskConfig" @syncConfig="syncSubTask" />
      <!-- step2 -->
      <step2 v-if="currentStep === 2" :sub-task-config="subTaskConfig" :global-params="globalParamsObject" @syncConfig="syncSubTask" @syncGlobalParams="syncGlobalParams" />
      <!-- step3 -->
      <step3 v-if="currentStep === 3" :sub-task-config="subTaskConfig" :host-data="selectedHosts" @syncHost="syncHost" />
    </div>
    <div class="submit-con">
      <a-button v-if="currentStep === 2 || currentStep === 3" type="outline" class="btn-item" @click="onPrev">上一步</a-button>
      <a-button v-if="currentStep === 1 || currentStep === 2" type="primary" class="btn-item" @click="onNext">下一步</a-button>
      <a-button v-if="currentStep === 3" type="primary" class="btn-item" @click="saveConfig">完成</a-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import Step1 from './step1'
import Step2 from './step2'
import Step3 from './step3'
import { migrationSave, migrationUpdate } from '@/api/task'
import { taskEditInfo } from '@/api/detail'
import dayjs from 'dayjs'

const currentStep = ref(1)
const taskId = ref()
const taskName = ref('')
const subTaskConfig = ref([])
const globalParamsObject = reactive({
  basic: [],
  more: []
})
const selectedHosts = ref([])
const stepOneComp = ref(null)

// prev step
const onPrev = () => {
  currentStep.value = Math.max(1, currentStep.value - 1)
}

// next step
const onNext = () => {
  if (currentStep.value === 1 && !subTaskConfig.value.length) {
    Message.error('Please add subtasks')
    return
  }
  if (subTaskConfig.value.length && subTaskConfig.value.some(item => !item.mode)) {
    Message.error('Please select a migration mode for subtasks')
    return
  }
  currentStep.value = Math.min(3, currentStep.value + 1)
}

// sync task config
const syncSubTask = configData => {
  console.log(configData)
  subTaskConfig.value = configData
}

// sync global params
const syncGlobalParams = params => {
  globalParamsObject.basic = params.basic
  globalParamsObject.more = params.more
}

// sync host data
const syncHost = hosts => {
  selectedHosts.value = hosts
}

// save task config
const saveConfig = () => {
  if (!taskName.value) {
    Message.error('Task name cannot be empty')
    return
  }
  if (!selectedHosts.value.length) {
    Message.error('Please select a migration machine')
    return
  }

  const params = {
    taskName: taskName.value,
    globalParams: [...globalParamsObject.basic.map(item => ({ paramKey: item.paramKey, paramValue: item.paramValue, paramDesc: item.paramDesc })), ...globalParamsObject.more.map(item => ({ paramKey: item.paramKey, paramValue: item.paramValue, paramDesc: item.paramDesc }))],
    hostIds: selectedHosts.value,
    tasks: subTaskConfig.value.map(item => {
      return {
        migrationModelId: item.mode,
        sourceDb: item.sourceDBName,
        sourceDbHost: item.sourceNodeInfo.host,
        sourceDbPass: item.sourceNodeInfo.password,
        sourceDbPort: item.sourceNodeInfo.port,
        sourceDbUser: item.sourceNodeInfo.username,
        sourceNodeId: item.sourceNodeInfo.nodeId,
        targetDb: item.targetDBName,
        targetDbHost: item.targetNodeInfo.host,
        targetDbPass: item.targetNodeInfo.password,
        targetDbPort: item.targetNodeInfo.port,
        targetDbUser: item.targetNodeInfo.username,
        targetDbVersion: item.targetNodeInfo.versionNum,
        targetNodeId: item.targetNodeInfo.nodeId,
        taskParams: [...item.taskParamsObject.basic.map(item => ({ paramKey: item.paramKey, paramValue: item.paramValue, paramDesc: item.paramDesc })), ...item.taskParamsObject.more.map(item => ({ paramKey: item.paramKey, paramValue: item.paramValue, paramDesc: item.paramDesc }))]
      }
    })
  }

  if (taskId.value) {
    params['taskId'] = taskId.value
    migrationUpdate(params).then(() => {
      Message.success('Update success')
      window.$wujie?.bus.$emit('data-migration-update')
      window.$wujie?.bus.$emit('opengauss-close-tab', {
        name: 'Static-pluginData-migrationTaskConfig',
        fullPath: '/static-plugin/data-migration/taskConfig'
      })
      window.$wujie?.props.methods.jump({
        name: `Static-pluginData-migrationIndex`
      })
    })
  } else {
    migrationSave(params).then(() => {
      Message.success('Save success')
      window.$wujie?.bus.$emit('data-migration-update')
      window.$wujie?.bus.$emit('opengauss-close-tab', {
        name: 'Static-pluginData-migrationTaskConfig',
        fullPath: '/static-plugin/data-migration/taskConfig'
      })
      window.$wujie?.props.methods.jump({
        name: `Static-pluginData-migrationIndex`
      })
    })
  }
}

// get task detail
const getTaskDetail = id => {
  taskEditInfo(id).then(res => {
    const data = res.data
    taskId.value = data.taskId
    taskName.value = data.taskName
    selectedHosts.value = data.hostIds
    globalParamsObject.basic = data.globalParams
    globalParamsObject.more = []
    subTaskConfig.value = data.tasks.map(item => {
      return {
        mode: item.migrationModelId,
        configType: !item.taskParams.length ? 1 : 2,
        sourceDBName: item.sourceDb,
        sourceNodeName: item.sourceDbHost,
        sourceNodeInfo: {
          host: item.sourceDbHost,
          password: item.sourceDbPass,
          port: item.sourceDbPort,
          username: item.sourceDbUser,
          nodeId: item.sourceNodeId
        },
        targetDBName: item.targetDb,
        targetNodeName: item.targetDbHost,
        targetNodeInfo: {
          host: item.targetDbHost,
          password: item.targetDbPass,
          port: item.targetDbPort,
          username: item.targetDbUser,
          versionNum: item.targetDbVersion,
          nodeId: item.targetNodeId
        },
        taskParamsObject: {
          basic: item.taskParams,
          more: []
        }
      }
    })
    stepOneComp.value.init(subTaskConfig.value)
  })
}

onMounted(() => {
  const id = window.$wujie?.props.data.id
  if (id) {
    getTaskDetail(id)
  } else {
    taskName.value = `Task_${dayjs().format('YYYYMMDDHHmm')}_${Math.random().toString(36).substring(2, 8)}`
  }
})
</script>

<style lang="less" scoped>
.home-container {
  position: relative;
  height: calc(100vh - 123px);
  overflow: hidden;
  .main-con {
    height: calc(100vh - 123px);
    padding-bottom: 40px;
    overflow-y: auto;
  }
  .title-con {
    padding: 20px 20px 0;
    display: flex;
    align-items: center;
    .title {
      font-size: 20px;
      color: var(--color-text-1);
    }
    .task-name-con {
      margin-left: 40px;
      display: flex;
      justify-content: center;
      align-items: center;
      .task-name {
        color: var(--color-text-1);
        white-space: nowrap;
        margin-right: 10px;
        display: flex;
        align-items: center;
        i {
          font-size: 16px;
          margin-right: 3px;
          font-style: normal;
          color: rgb(var(--danger-6));
        }
      }
    }
  }
  .task-steps-con {
    width: 80%;
    margin: 30px auto;
  }
  .submit-con {
    position: absolute;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 10;
    background: var(--color-bg-2);
    display: flex;
    justify-content: center;
    .btn-item {
      margin: 10px;
    }
  }
}
</style>
