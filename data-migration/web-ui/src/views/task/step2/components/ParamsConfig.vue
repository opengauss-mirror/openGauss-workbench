<template>
  <a-drawer
    v-model:visible="visible"
    width="600px"
    unmount-on-close
  >
    <template #title>
      <div class="title-con">
        <span class="params-title">{{ props.mode === 1 ? $t('components.ParamsConfig.5q0aazspots0') : $t('components.ParamsConfig.5q0aazsppew0')}}</span>
        <span v-if="props.mode === 1" class="params-info">{{$t('components.ParamsConfig.5q0aazsppjc0', { num: props.countUseDefault })}}</span>
      </div>
    </template>
    <div class="config-con">
      <div v-if="props.mode !== 1" class="diy-info-con">
        <a-descriptions :data="descData" layout="inline-horizontal" :column="2" table-layout="fixed" bordered />
      </div>
      <div class="basic-config-con">
        <div class="basic-title">{{$t('components.ParamsConfig.5q0aazsppm00')}}</div>
        <div class="basic-params-table">
          <a-table :data="basicData" :hoverable="false" :bordered="true" :pagination="false" :row-class="basicRowClass">
            <template #columns>
              <a-table-column :title="$t('components.ParamsConfig.5q0aazsppog0')" data-index="paramKey" :width="180" ellipsis tooltip></a-table-column>
              <a-table-column :title="$t('components.ParamsConfig.5q0aazsppr80')" data-index="paramValue">
                <template #cell="{ record }">
                  <a-input v-model.trim="record.paramValue" />
                </template>
              </a-table-column>
              <a-table-column :title="$t('components.ParamsConfig.5q0aazspptw0')" data-index="paramDesc" :width="200" ellipsis tooltip></a-table-column>
            </template>
          </a-table>
        </div>
      </div>
      <div class="super-config-con">
        <a-collapse :default-active-key="[1]" accordion :bordered="false" expand-icon-position="right" :show-expand-icon="true">
          <a-collapse-item :header="$t('components.ParamsConfig.5q0aazsppwg0')" key="1" :style="customStyle">
            <div class="super-params-table">
              <a-table :data="moreData" :hoverable="false" :bordered="true" :pagination="false" :row-class="moreRowClass">
                <template #columns>
                  <a-table-column :title="$t('components.ParamsConfig.5q0aazsppog0')" data-index="paramKey" :width="180" ellipsis tooltip></a-table-column>
                  <a-table-column :title="$t('components.ParamsConfig.5q0aazsppr80')" data-index="paramValue">
                    <template #cell="{ record, rowIndex }">
                      <a-input v-model.trim="record.paramValue" @change="moreValueChange(record, rowIndex)" />
                    </template>
                  </a-table-column>
                  <a-table-column :title="$t('components.ParamsConfig.5q0aazspptw0')" data-index="paramDesc" :width="200" ellipsis tooltip></a-table-column>
                </template>
              </a-table>
            </div>
          </a-collapse-item>
        </a-collapse>
      </div>
    </div>
    <template #footer>
      <div class="footer-con">
        <a-button v-if="props.mode === 1 && (basicEditData.length || moreEditData.length)" type="outline" style="margin-right: 10px;" @click="resetDefault">{{$t('components.ParamsConfig.5q0aazsppz40')}}</a-button>
        <a-button v-if="props.mode === 2 && (basicEditData.length || moreEditData.length)" type="outline" style="margin-right: 10px;" @click="resetDefault">{{$t('components.ParamsConfig.5q0aazspq200')}}</a-button>
        <a-button type="primary" @click="saveParams()">{{$t('components.ParamsConfig.5q0aazspq4g0')}}</a-button>
      </div>
    </template>
  </a-drawer>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { defaultParams } from '@/api/task'
import { mergeObjectArray } from '@/utils'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

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
  if (basicEditData.value.some(item => item.paramKey === row.raw.paramKey)) {
    return 'row-changed'
  } else {
    return ''
  }
}

const moreRowClass = (row) => {
  if (moreEditData.value.some(item => item.paramKey === row.raw.paramKey)) {
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
          label: t('components.ParamsConfig.5q0aazspq7k0'),
          value: props.taskInfo.sourceNodeName
        },
        {
          label: t('components.ParamsConfig.5q0aazspqac0'),
          value: props.taskInfo.targetNodeName
        },
        {
          label: t('components.ParamsConfig.5q0aazspqd00'),
          value: props.taskInfo.sourceDBName
        },
        {
          label: t('components.ParamsConfig.5q0aazspqfs0'),
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
    if (!fItem || fItem.paramValue !== item.paramValue) {
      flag = true
    }
    return flag
  })
}, { deep: true })

const moreValueChange = (row, rowIndex) => {
  if (row.paramType === 9) {
    const subData = JSON.parse(row.paramExtends)
    if (row.paramValue && row.paramValue !== '0') {
      // delete
      const subKeyPrefixs = subData.map(sub => sub.subKeyPrefix)
      moreData.value = moreData.value.filter(item => item.paramType === 9 || !subKeyPrefixs.some(prefix => item.paramKey.includes(prefix)))
      // add
      const addNum = +row.paramValue
      const arr = []
      for (let i = 0; i < addNum; i++) {
        for (let j = 0; j < subData.length; j++) {
          arr.push({
            paramKey: subData[j]['subKeyPrefix'] + `${i + 1}`,
            paramValue: subData[j]['defaultValue'],
            paramDesc: subData[j]['desc'],
            paramType: subData[j]['dataType']
          })
        }
      }
      moreData.value.splice(rowIndex + 1, 0, ...arr)
    } else {
      const subKeyPrefixs = subData.map(sub => sub.subKeyPrefix)
      moreData.value = moreData.value.filter(item => item.paramType === 9 || !subKeyPrefixs.some(prefix => item.paramKey.includes(prefix)))
    }
  }
}

const getDefaultParams = () => {
  defaultParams().then(res => {
    defaultData.basic = res.data.slice(0, 12)
    defaultData.more = res.data.slice(12)
    const data = JSON.parse(JSON.stringify(res.data))
    basicData.value = data.slice(0, 12)
    moreData.value = data.slice(12)

    if (props.mode === 1) {
      if (props.globalParams.basic.length) {
        basicEditData.value = props.globalParams.basic
        basicData.value = basicData.value.map(item => {
          const findItem = basicEditData.value.find(fItem => fItem.paramKey === item.paramKey)
          return findItem || item
        })
      }
      if (props.globalParams.more.length) {
        moreEditData.value = props.globalParams.more
        const moreDataTmp = []
        moreData.value.forEach(item => {
          moreDataTmp.push(item)
          if (item.paramType === 9) {
            const parentKey = item.paramKey
            props.globalParams.more.forEach(param => {
              const childPrefix = param.paramKey.replace(/\.[^/.]+$/, '')
              if (parentKey === childPrefix) {
                moreDataTmp.push(param)
              }
            })
          }
        })
        moreData.value = moreDataTmp.map(item => {
          const findItem = moreEditData.value.find(fItem => fItem.paramKey === item.paramKey)
          return findItem ? ({ ...item, ...findItem }) : item
        })
      }
    } else {
      taskParams.basic = mergeObjectArray(props.globalParams.basic, props.taskInfo.taskParamsObject.basic, 'paramKey')
      taskParams.more = mergeObjectArray(props.globalParams.more, props.taskInfo.taskParamsObject.more, 'paramKey')
      if (taskParams.basic.length) {
        basicEditData.value = taskParams.basic
        basicData.value = basicData.value.map(item => {
          const findItem = basicEditData.value.find(fItem => fItem.paramKey === item.paramKey)
          return findItem || item
        })
      }
      if (taskParams.more.length) {
        moreEditData.value = taskParams.more
        const moreDataTmp = []
        moreData.value.forEach(item => {
          moreDataTmp.push(item)
          if (item.paramType === 9) {
            const parentKey = item.paramKey
            taskParams.more.forEach(param => {
              const childPrefix = param.paramKey.replace(/\.[^/.]+$/, '')
              if (parentKey === childPrefix) {
                moreDataTmp.push(param)
              }
            })
          }
        })
        moreData.value = moreDataTmp.map(item => {
          const findItem = moreEditData.value.find(fItem => fItem.paramKey === item.paramKey)
          return findItem ? ({ ...item, ...findItem }) : item
        })
      }
    }
  })
}

const resetDefault = () => {
  if (props.mode === 1) {
    basicEditData.value = []
    moreEditData.value = []
    basicData.value = JSON.parse(JSON.stringify(defaultData.basic))
    moreData.value = JSON.parse(JSON.stringify(defaultData.more))
  } else {
    basicEditData.value = mergeObjectArray([], props.globalParams.basic, 'paramKey')
    moreEditData.value = mergeObjectArray([], props.globalParams.more, 'paramKey')
    basicData.value = mergeObjectArray(defaultData.basic, props.globalParams.basic, 'paramKey')
    if (props.globalParams.more.length) {
      moreData.value = mergeObjectArray(defaultData.more, [], 'paramKey')
      const moreDataTmp = []
      moreData.value.forEach(item => {
        moreDataTmp.push(item)
        if (item.paramType === 9) {
          const parentKey = item.paramKey
          props.globalParams.more.forEach(param => {
            const childPrefix = param.paramKey.replace(/\.[^/.]+$/, '')
            if (parentKey === childPrefix) {
              moreDataTmp.push(param)
            }
          })
        }
      })
      moreData.value = moreDataTmp.map(item => {
        const findItem = moreEditData.value.find(fItem => fItem.paramKey === item.paramKey)
        return findItem ? ({ ...item, ...findItem }) : item
      })
    } else {
      moreData.value = mergeObjectArray(defaultData.more, props.globalParams.more, 'paramKey')
    }
  }
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
        background: var(--color-neutral-3);
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
        background: var(--color-neutral-3);
      }
    }
  }
}
</style>
