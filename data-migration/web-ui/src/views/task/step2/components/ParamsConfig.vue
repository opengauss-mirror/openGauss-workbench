<template>
  <a-drawer
    v-model:visible="visible"
    width="600px"
    unmount-on-close
  >
    <template #title>
      <div class="title-con">
        <span class="params-title">{{ props.mode === 1 ? '默认参数配置（全局配置）' : '子任务参数配置'}}</span>
        <span v-if="props.mode === 1" class="params-info"><b>{{ props.countUseDefault }}</b>个子任务使用了此配置</span>
      </div>
    </template>
    <div class="config-con">
      <div v-if="props.mode !== 1" class="diy-info-con">
        <a-descriptions :data="descData" layout="inline-horizontal" :column="2" table-layout="fixed" bordered />
      </div>
      <div class="basic-config-con">
        <div class="basic-title">基础参数</div>
        <div class="basic-params-table">
          <a-table :data="basicData" :hoverable="false" :bordered="true" :pagination="false" :row-class="basicRowClass">
            <template #columns>
              <a-table-column title="参数名称" data-index="paramKey" :width="180" ellipsis tooltip></a-table-column>
              <a-table-column title="值" data-index="paramValue">
                <template #cell="{ record }">
                  <a-input v-model="record.paramValue" />
                </template>
              </a-table-column>
              <a-table-column title="备注说明" data-index="paramDesc" :width="200" ellipsis tooltip></a-table-column>
            </template>
          </a-table>
        </div>
      </div>
      <div class="super-config-con">
        <a-collapse :default-active-key="[1]" accordion :bordered="false" expand-icon-position="right" :show-expand-icon="true">
          <a-collapse-item header="高级参数" key="1" :style="customStyle">
            <div class="super-params-table">
              <a-table :data="moreData" :hoverable="false" :bordered="true" :pagination="false" :row-class="moreRowClass">
                <template #columns>
                  <a-table-column title="参数名称" data-index="paramKey" :width="180" ellipsis tooltip></a-table-column>
                  <a-table-column title="值" data-index="paramValue">
                    <template #cell="{ record }">
                      <a-input v-model="record.paramValue" />
                    </template>
                  </a-table-column>
                  <a-table-column title="备注说明" data-index="paramDesc" :width="200" ellipsis tooltip></a-table-column>
                </template>
              </a-table>
            </div>
          </a-collapse-item>
        </a-collapse>
      </div>
    </div>
    <template #footer>
      <div class="footer-con">
        <a-button v-if="props.mode !== 1 && (basicEditData.length || moreEditData.length)" type="outline" style="margin-right: 10px;" @click="resetDefault">恢复默认值</a-button>
        <a-button type="primary" @click="saveParams()">保存</a-button>
      </div>
    </template>
  </a-drawer>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { defaultParams } from '@/api/task'

const props = defineProps({
  open: Boolean,
  mode: [String, Number],
  globalParams: Object,
  countUseDefault: [String, Number],
  taskInfo: Object
})

const emits = defineEmits(['update:open', 'syncGlobalParams', 'syncTaskParams'])

const visible = ref(false)

const customStyle = ref({
  borderRadius: '6px',
  marginBottom: '18px',
  border: 'none',
  overflow: 'hidden'
})

const descData = ref([])

const defaultData = reactive({
  basic: [],
  more: []
})
const basicData = ref([])
const moreData = ref([])
const basicEditData = ref([])
const moreEditData = ref([])
const taskParams = reactive({
  basic: [],
  more: []
})

const basicRowClass = (row) => {
  if (basicEditData.value.some(item => item.id === row.raw.id)) {
    return 'row-changed'
  } else {
    return ''
  }
}

const moreRowClass = (row) => {
  if (moreEditData.value.some(item => item.id === row.raw.id)) {
    return 'row-changed'
  } else {
    return ''
  }
}

watch(visible, (v) => {
  emits('update:open', v)
})

watch(() => props.open, (v) => {
  if (v) {
    basicData.value = []
    moreData.value = []
    if (props.mode === 2) {
      descData.value = [
        {
          label: '源实例名：',
          value: props.taskInfo.sourceNodeName
        },
        {
          label: '目的实例名：',
          value: props.taskInfo.targetNodeName
        },
        {
          label: '源库名：',
          value: props.taskInfo.sourceDBName
        },
        {
          label: '目的库名：',
          value: props.taskInfo.targetDBName
        }
      ]
    }
    getDefaultParams()
  }
  visible.value = v
})

watch(basicData, (v) => {
  basicEditData.value = v.filter(item => {
    let flag = false
    const fItem = defaultData.basic.find(vItem => vItem.paramKey === item.paramKey)
    if (fItem.paramValue !== item.paramValue) {
      flag = true
    }
    return flag
  })
}, { deep: true })

watch(moreData, (v) => {
  moreEditData.value = v.filter(item => {
    let flag = false
    const fItem = defaultData.more.find(vItem => vItem.paramKey === item.paramKey)
    if (fItem.paramValue !== item.paramValue) {
      flag = true
    }
    return flag
  })
}, { deep: true })

const getDefaultParams = () => {
  defaultParams().then(res => {
    defaultData.basic = res.data.slice(0, 10)
    defaultData.more = res.data.slice(10)
    const data = JSON.parse(JSON.stringify(res.data))
    basicData.value = data.slice(0, 10)
    moreData.value = data.slice(10)

    if (props.mode === 1) {
      if (props.globalParams.basic.length) {
        basicEditData.value = props.globalParams.basic
        basicData.value = basicData.value.map(item => {
          const findItem = basicEditData.value.find(fItem => fItem.id === item.id)
          return findItem || item
        })
      }
      if (props.globalParams.more.length) {
        moreEditData.value = props.globalParams.more
        moreData.value = moreData.value.map(item => {
          const findItem = moreEditData.value.find(fItem => fItem.id === item.id)
          return findItem || item
        })
      }
    } else {
      taskParams.basic = props.taskInfo.taskParamsObject.basic
      taskParams.more = props.taskInfo.taskParamsObject.more
      if (taskParams.basic.length) {
        basicEditData.value = taskParams.basic
        basicData.value = basicData.value.map(item => {
          const findItem = basicEditData.value.find(fItem => fItem.id === item.id)
          return findItem || item
        })
      }
      if (taskParams.more.length) {
        moreEditData.value = taskParams.more
        moreData.value = moreData.value.map(item => {
          const findItem = moreEditData.value.find(fItem => fItem.id === item.id)
          return findItem || item
        })
      }
    }
  })
}

const resetDefault = () => {
  basicData.value = JSON.parse(JSON.stringify(defaultData.basic))
  moreData.value = JSON.parse(JSON.stringify(defaultData.more))
  basicEditData.value = []
  moreEditData.value = []
}

const saveParams = () => {
  if (props.mode === 1) {
    emits('syncGlobalParams', {
      basic: basicEditData.value,
      more: moreEditData.value
    })
  } else {
    emits('syncTaskParams', {
      basic: basicEditData.value,
      more: moreEditData.value
    })
  }
  visible.value = false
}

onMounted(() => {
  visible.value = props.open
})
</script>

<style lang="less" scoped>
.title-con {
  width: 540px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  .params-info {
    font-size: 14px;
    font-weight: normal;
    b {
      color: rgb(var(--primary-6))
    }
  }
}
.config-con {
  .diy-info-con {
    margin-bottom: 10px;
  }
  .basic-config-con {
    .basic-title {
      padding-left: 13px;
      margin-bottom: 10px;
    }
    :deep(.row-changed) {
      .arco-table-td {
        background: var(--color-primary-light-1);
      }
    }
  }
  .super-config-con {
    margin-top: 10px;
    :deep(.arco-collapse-item-content) {
      padding-left: 0;
      padding-right: 0;
      background-color: transparent;
      .arco-collapse-item-content-box {
        padding: 0;
      }
    }
    :deep(.row-changed) {
      .arco-table-td {
        background: var(--color-primary-light-1);
      }
    }
  }
}
</style>
