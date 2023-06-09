<template>
  <a-drawer v-model:visible="visible" width="50vw" unmount-on-close>
    <template #title>
      <div class="title-con">
        <span class="params-title">{{
          props.mode === 1
            ? $t('components.ParamsConfig.5q0aazspots0')
            : $t('components.ParamsConfig.5q0aazsppew0')
        }}</span>
        <span v-if="props.mode === 1" class="params-info">{{
          $t('components.ParamsConfig.5q0aazsppjc0', {
            num: props.countUseDefault,
          })
        }}</span>
      </div>
    </template>
    <div class="config-con">
      <div v-if="props.mode !== 1" class="diy-info-con">
        <a-descriptions
          :data="descData"
          layout="inline-horizontal"
          :column="2"
          table-layout="fixed"
          bordered
        />
      </div>
      <div class="basic-config-con">
        <div class="basic-title">
          {{ $t('components.ParamsConfig.5q0aazsppm00') }}
        </div>
        <div class="basic-params-table">
          <a-form :model="form" ref="basicFormRef">
            <a-table
              :data="form.basicData"
              :hoverable="false"
              :bordered="true"
              :pagination="false"
              :row-class="basicRowClass"
            >
              <template #columns>
                <a-table-column
                  :title="$t('components.ParamsConfig.5q0aazsppog0')"
                  data-index="paramKey"
                  :width="180"
                  ellipsis
                  tooltip
                ></a-table-column>
                <a-table-column
                  :title="$t('components.ParamsConfig.5q0aazsppr80')"
                >
                  <template #cell="{ record, rowIndex }">
                    <a-form-item
                      hide-asterisk
                      :field="`basicData.${rowIndex}.paramValue`"
                      :rules="getValidator(record)"
                    >
                      <a-select
                        v-if="record.paramType === PORTAL_PARAM_TYPE.SELECT"
                        v-model="record.paramValue"
                      >
                        <a-option
                          v-for="item in JSON.parse(record.paramRules)"
                          :key="item"
                          :value="item"
                          :label="item"
                        ></a-option>
                      </a-select>
                      <!-- cannot use v-model.number in a-input-number -->
                      <!-- waiting arco design solve this issue -->
                      <a-input-number
                        v-else-if="
                          record.paramType === PORTAL_PARAM_TYPE.NUMBER
                        "
                        :model-value="Number(record.paramValue)"
                        @change="(val) => (record.paramValue = val)"
                        :min="JSON.parse(record.paramRules)[0]"
                        :max="JSON.parse(record.paramRules)[1]"
                      />
                      <a-switch
                        v-else-if="
                          record.paramType === PORTAL_PARAM_TYPE.BOOLEAN
                        "
                        v-model="record.paramValue"
                        checked-value="true"
                        unchecked-value="false"
                      />
                      <a-input v-else v-model.trim="record.paramValue" />
                    </a-form-item>
                  </template>
                </a-table-column>
                <a-table-column
                  :title="$t('components.ParamsConfig.5q0aazspptw0')"
                  data-index="paramDesc"
                  :width="200"
                  ellipsis
                  tooltip
                ></a-table-column>
              </template>
            </a-table>
          </a-form>
        </div>
      </div>
      <div class="super-config-con">
        <a-collapse
          :default-active-key="[1]"
          accordion
          :bordered="false"
          expand-icon-position="right"
          :show-expand-icon="true"
        >
          <a-collapse-item
            :header="$t('components.ParamsConfig.5q0aazsppwg0')"
            key="1"
            :style="customStyle"
          >
            <div class="super-params-table">
              <a-form :model="form" ref="moreFormRef">
                <a-table
                  :data="form.moreData"
                  :hoverable="false"
                  :bordered="true"
                  :pagination="false"
                  :row-class="moreRowClass"
                >
                  <template #columns>
                    <a-table-column
                      :title="$t('components.ParamsConfig.5q0aazsppog0')"
                      data-index="paramKey"
                      :width="180"
                      ellipsis
                      tooltip
                    ></a-table-column>
                    <a-table-column
                      :title="$t('components.ParamsConfig.5q0aazsppr80')"
                    >
                      <template #cell="{ record, rowIndex }">
                        <a-form-item
                          hide-asterisk
                          :field="`moreData.${rowIndex}.paramValue`"
                          :rules="getValidator(record)"
                        >
                          <a-select
                            v-if="record.paramType === PORTAL_PARAM_TYPE.SELECT"
                            v-model="record.paramValue"
                          >
                            <a-option
                              v-for="item in JSON.parse(record.paramRules)"
                              :key="item"
                              :value="item"
                              :label="item"
                            ></a-option>
                          </a-select>
                          <a-input-number
                            v-else-if="
                              record.paramType === PORTAL_PARAM_TYPE.NUMBER ||
                              record.paramType ===
                                PORTAL_PARAM_TYPE.OBJECT_ARRAY
                            "
                            :model-value="Number(record.paramValue)"
                            @change="
                              moreValueChangeAdapter($event, record, rowIndex)
                            "
                            :min="JSON.parse(record.paramRules)[0]"
                            :max="JSON.parse(record.paramRules)[1]"
                          />
                          <a-switch
                            v-else-if="
                              record.paramType === PORTAL_PARAM_TYPE.BOOLEAN
                            "
                            v-model="record.paramValue"
                            checked-value="true"
                            unchecked-value="false"
                          />
                          <a-input v-else v-model.trim="record.paramValue" />
                        </a-form-item>
                      </template>
                    </a-table-column>
                    <a-table-column
                      :title="$t('components.ParamsConfig.5q0aazspptw0')"
                      data-index="paramDesc"
                      :width="200"
                      ellipsis
                      tooltip
                    ></a-table-column>
                  </template>
                </a-table>
              </a-form>
            </div>
          </a-collapse-item>
        </a-collapse>
      </div>
    </div>
    <template #footer>
      <div class="footer-con">
        <a-button
          v-if="
            props.mode === 1 && (basicEditData.length || moreEditData.length)
          "
          type="outline"
          style="margin-right: 10px"
          @click="resetDefault"
          >{{ $t('components.ParamsConfig.5q0aazsppz40') }}</a-button
        >
        <a-button
          v-if="
            props.mode === 2 && (basicEditData.length || moreEditData.length)
          "
          type="outline"
          style="margin-right: 10px"
          @click="resetDefault"
          >{{ $t('components.ParamsConfig.5q0aazspq200') }}</a-button
        >
        <a-button type="primary" @click="saveParams()">{{
          $t('components.ParamsConfig.5q0aazspq4g0')
        }}</a-button>
      </div>
    </template>
  </a-drawer>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { defaultParams } from '@/api/task'
import { mergeObjectArray } from '@/utils'
import { useI18n } from 'vue-i18n'
import { PORTAL_PARAM_TYPE } from '@/utils/constants'
import { getValidator } from './validators'

const { t } = useI18n()

const props = defineProps({
  open: Boolean,
  mode: [String, Number],
  globalParams: Object,
  countUseDefault: [String, Number],
  taskInfo: Object,
})

const emits = defineEmits(['update:open', 'syncGlobalParams', 'syncTaskParams'])

const visible = ref(false)
const basicFormRef = ref()
const moreFormRef = ref()
const customStyle = ref({
  borderRadius: '6px',
  marginBottom: '18px',
  border: 'none',
  overflow: 'hidden',
})

const descData = ref([])

const defaultData = reactive({
  basic: [],
  more: [],
})
const form = reactive({
  basicData: [],
  moreData: [],
})
const basicEditData = ref([])
const moreEditData = ref([])
const taskParams = reactive({
  basic: [],
  more: [],
})

const basicRowClass = (row) => {
  if (basicEditData.value.some((item) => item.paramKey === row.raw.paramKey)) {
    return 'row-changed'
  } else {
    return ''
  }
}

const moreRowClass = (row) => {
  if (moreEditData.value.some((item) => item.paramKey === row.raw.paramKey)) {
    return 'row-changed'
  } else {
    return ''
  }
}

watch(visible, (v) => {
  emits('update:open', v)
})

watch(
  () => props.open,
  (v) => {
    if (v) {
      form.basicData = []
      form.moreData = []
      if (props.mode === 2) {
        descData.value = [
          {
            label: t('components.ParamsConfig.5q0aazspq7k0'),
            value: props.taskInfo.sourceNodeName,
          },
          {
            label: t('components.ParamsConfig.5q0aazspqac0'),
            value: props.taskInfo.targetNodeName,
          },
          {
            label: t('components.ParamsConfig.5q0aazspqd00'),
            value: props.taskInfo.sourceDBName,
          },
          {
            label: t('components.ParamsConfig.5q0aazspqfs0'),
            value: props.taskInfo.targetDBName,
          },
        ]
      }
      getDefaultParams()
    }
    visible.value = v
  }
)

watch(
  () => form.basicData,
  (v) => {
    basicEditData.value = v.filter((item) => {
      let flag = false
      const fItem = defaultData.basic.find(
        (vItem) => vItem.paramKey === item.paramKey
      )
      // basic data value will change from string to number, so we use != here
      if (fItem.paramValue != item.paramValue) {
        flag = true
      }
      return flag
    })
  },
  { deep: true }
)

watch(
  () => form.moreData,
  (v) => {
    moreEditData.value = v.filter((item) => {
      let flag = false
      const fItem = defaultData.more.find(
        (vItem) => vItem.paramKey === item.paramKey
      )
      // more data value will change from string to number, so we use != here
      if (!fItem || fItem.paramValue != item.paramValue) {
        flag = true
      }
      return flag
    })
  },
  { deep: true }
)

const moreValueChangeAdapter = (val, row, rowIndex) => {
  row.paramValue = val
  moreValueChange(row, rowIndex)
}

const moreValueChange = (row, rowIndex) => {
  if (row.paramType === PORTAL_PARAM_TYPE.OBJECT_ARRAY) {
    const subData = JSON.parse(row.paramExtends)
    if (row.paramValue && row.paramValue !== '0') {
      // delete
      const subKeyPrefixs = subData.map((sub) => sub.subKeyPrefix)
      form.moreData = form.moreData.filter(
        (item) =>
          item.paramType === PORTAL_PARAM_TYPE.OBJECT_ARRAY ||
          !subKeyPrefixs.some((prefix) => item.paramKey.includes(prefix))
      )
      // add
      const addNum = +row.paramValue
      const arr = []
      for (let i = 0; i < addNum; i++) {
        for (let j = 0; j < subData.length; j++) {
          arr.push({
            paramKey: subData[j]['subKeyPrefix'] + `${i + 1}`,
            paramValue: subData[j]['paramValue'],
            paramDesc: subData[j]['desc'],
            paramType: subData[j]['paramType'],
            paramRules: subData[j]['paramRules'],
          })
        }
      }
      form.moreData.splice(rowIndex + 1, 0, ...arr)
    } else {
      const subKeyPrefixs = subData.map((sub) => sub.subKeyPrefix)
      form.moreData = form.moreData.filter(
        (item) =>
          item.paramType === PORTAL_PARAM_TYPE.OBJECT_ARRAY ||
          !subKeyPrefixs.some((prefix) => item.paramKey.includes(prefix))
      )
    }
  }
}

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
  defaultParams().then((res) => {
    defaultData.basic = res.data.slice(0, 12)
    defaultData.more = res.data.slice(12)
    const data = JSON.parse(JSON.stringify(res.data))
    form.basicData = data.slice(0, 12)
    form.moreData = data.slice(12)

    if (props.mode === 1) {
      if (props.globalParams.basic.length) {
        basicEditData.value = props.globalParams.basic
        form.basicData = form.basicData.map((item) => {
          const findItem = basicEditData.value.find(
            (fItem) => fItem.paramKey === item.paramKey
          )
          return findItem || item
        })
      }
      if (props.globalParams.more.length) {
        moreEditData.value = props.globalParams.more
        const moreDataTmp = []
        form.moreData.forEach((item) => {
          moreDataTmp.push(item)
          if (item.paramType === PORTAL_PARAM_TYPE.OBJECT_ARRAY) {
            const parentKey = item.paramKey
            props.globalParams.more.forEach((param) => {
              const childPrefix = param.paramKey.replace(/\.[^/.]+$/, '')
              if (parentKey === childPrefix && parentKey !== 'type_override') {
                moreDataTmp.push(param)
              } else if (
                parentKey === 'type_override' &&
                childPrefix.indexOf('override') > -1 &&
                childPrefix !== 'type_override'
              ) {
                moreDataTmp.push(param)
              }
            })
          }
        })
        form.moreData = moreDataTmp.map((item) => {
          const findItem = moreEditData.value.find(
            (fItem) => fItem.paramKey === item.paramKey
          )
          return findItem ? { ...item, ...findItem } : item
        })
      }
    } else {
      taskParams.basic = mergeObjectArray(
        props.globalParams.basic,
        props.taskInfo.taskParamsObject.basic,
        'paramKey'
      )
      taskParams.more = mergeObjectArray(
        props.globalParams.more,
        props.taskInfo.taskParamsObject.more,
        'paramKey'
      )
      if (taskParams.basic.length) {
        basicEditData.value = taskParams.basic
        form.basicData = form.basicData.map((item) => {
          const findItem = basicEditData.value.find(
            (fItem) => fItem.paramKey === item.paramKey
          )
          return findItem || item
        })
      }
      if (taskParams.more.length) {
        moreEditData.value = taskParams.more
        const moreDataTmp = []
        form.moreData.forEach((item) => {
          moreDataTmp.push(item)
          if (item.paramType === PORTAL_PARAM_TYPE.OBJECT_ARRAY) {
            const parentKey = item.paramKey
            taskParams.more.forEach((param) => {
              const childPrefix = param.paramKey.replace(/\.[^/.]+$/, '')
              if (parentKey === childPrefix && parentKey !== 'type_override') {
                moreDataTmp.push(param)
              } else if (
                parentKey === 'type_override' &&
                childPrefix.indexOf('override') > -1 &&
                childPrefix !== 'type_override'
              ) {
                moreDataTmp.push(param)
              }
            })
          }
        })
        form.moreData = moreDataTmp.map((item) => {
          const findItem = moreEditData.value.find(
            (fItem) => fItem.paramKey === item.paramKey
          )
          return findItem ? { ...item, ...findItem } : item
        })
      }
    }
  })
}

const resetDefault = () => {
  if (props.mode === 1) {
    basicEditData.value = []
    moreEditData.value = []
    form.basicData = JSON.parse(JSON.stringify(defaultData.basic))
    form.moreData = JSON.parse(JSON.stringify(defaultData.more))
  } else {
    basicEditData.value = mergeObjectArray(
      [],
      props.globalParams.basic,
      'paramKey'
    )
    moreEditData.value = mergeObjectArray(
      [],
      props.globalParams.more,
      'paramKey'
    )
    form.basicData = mergeObjectArray(
      defaultData.basic,
      props.globalParams.basic,
      'paramKey'
    )
    if (props.globalParams.more.length) {
      form.moreData = mergeObjectArray(defaultData.more, [], 'paramKey')
      const moreDataTmp = []
      form.moreData.forEach((item) => {
        moreDataTmp.push(item)
        if (item.paramType === PORTAL_PARAM_TYPE.OBJECT_ARRAY) {
          const parentKey = item.paramKey
          props.globalParams.more.forEach((param) => {
            const childPrefix = param.paramKey.replace(/\.[^/.]+$/, '')
            if (parentKey === childPrefix && parentKey !== 'type_override') {
              moreDataTmp.push(param)
            } else if (
              parentKey === 'type_override' &&
              childPrefix.indexOf('override') > -1 &&
              childPrefix !== 'type_override'
            ) {
              moreDataTmp.push(param)
            }
          })
        }
      })
      form.moreData = moreDataTmp.map((item) => {
        const findItem = moreEditData.value.find(
          (fItem) => fItem.paramKey === item.paramKey
        )
        return findItem ? { ...item, ...findItem } : item
      })
    } else {
      form.moreData = mergeObjectArray(
        defaultData.more,
        props.globalParams.more,
        'paramKey'
      )
    }
  }
}

const saveParams = () => {
  basicFormRef.value.validate((basicValid) => {
    if (basicValid) {
      return
    }
    moreFormRef.value.validate((moreValid) => {
      if (moreValid) {
        return
      }
      if (props.mode === 1) {
        emits('syncGlobalParams', {
          basic: basicEditData.value,
          more: moreEditData.value,
        })
      } else {
        emits('syncTaskParams', {
          basic: basicEditData.value,
          more: moreEditData.value,
        })
      }
      visible.value = false
    })
  })
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
      color: rgb(var(--primary-6));
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
:deep(.arco-form-item) {
  margin-bottom: 0;
}
:deep(.arco-col-5) {
  flex: none;
  width: auto;
}
</style>
