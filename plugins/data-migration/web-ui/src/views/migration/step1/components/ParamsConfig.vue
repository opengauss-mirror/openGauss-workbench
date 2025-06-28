<template>
  <el-drawer v-model="visible" :size="'50%'" :destroy-on-close="true">
    <template #header>
      <div class="title-con">
      <span class="params-title">{{
          props.mode === 1
            ? $t('components.ParamsConfig.5q0aazspots0')
            : $t('components.ParamsConfig.5q0aazsppew0')
        }}</span>
      </div>
    </template>
    <div class="config-con">
      <div class="basic-config-con">
        <div class="basic-title">
          {{ $t('components.ParamsConfig.5q0aazsppm00') }}
        </div>
        <div class="basic-params-table">
          <el-form :model="form" ref="basicFormRef" class="page-input-size">
            <el-table :data="form.basicData" :hover="false" :border="true" :row-class-name="basicRowClass">
              <el-table-column :label="$t('components.ParamsConfig.5q0aazsppog0')" prop="paramKey"
                               show-overflow-tooltip/>
              <el-table-column :label="$t('components.ParamsConfig.5q0aazsppr80')">
                <template #default="{ row, $index }">
                  <el-form-item :prop="`basicData.${$index}.paramValue`" :rules="getValidator(row, props.mode===1)">
                    <el-select v-if="row.paramType === PORTAL_PARAM_TYPE.SELECT" v-model="row.paramValue">
                      <el-option v-for="item in JSON.parse(row.paramRules)" :key="item" :value="item" :label="item"/>
                    </el-select>
                    <el-input-number v-else-if="row.paramType === PORTAL_PARAM_TYPE.NUMBER" v-model="row.paramValue"
                                     :min="JSON.parse(row.paramRules)[0]" :max="JSON.parse(row.paramRules)[1]"
                                     controls-position="right"/>
                    <el-switch v-else-if="row.paramType === PORTAL_PARAM_TYPE.BOOLEAN" v-model="row.paramValue"
                               active-value="true" inactive-value="false"/>
                    <el-input v-else v-model.trim="row.paramValue" :input-style="{ textAlign: 'center' }" />
                  </el-form-item>
                </template>
              </el-table-column>
              <el-table-column :label="$t('components.ParamsConfig.defaultValue')" prop="defaultParamValue"
                               show-overflow-tooltip/>
              <el-table-column :label="$t('components.ParamsConfig.5q0aazspptw0')" prop="paramDesc"
                               show-overflow-tooltip/>
            </el-table>
          </el-form>
        </div>
      </div>
      <div class="super-config-con" v-if="defaultData.more.length > 0">
        <el-collapse v-model="activeCollapse" accordion name="1">
          <el-collapse-item :title="$t('components.ParamsConfig.5q0aazsppwg0')" name="1" :style="customStyle">
            <div class="super-params-table">
              <el-form :model="form" ref="moreFormRef" class="page-input-size">
                <el-table :data="form.moreData" :hover="false" border :row-class-name="moreRowClass">
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppog0')" prop="paramKey"
                                   show-overflow-tooltip/>
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazsppr80')">
                    <template #default="{ row, $index }">
                      <el-form-item :prop="`moreData.${$index}.paramValue`" :rules="getValidator(row)">
                        <el-select v-if="row.paramType === PORTAL_PARAM_TYPE.SELECT" v-model="row.paramValue">
                          <el-option v-for="item in JSON.parse(row.paramRules)" :key="item" :value="item"
                                     :label="item"/>
                        </el-select>
                        <el-input-number
                          v-else-if="row.paramType === PORTAL_PARAM_TYPE.NUMBER ||row.paramType === PORTAL_PARAM_TYPE.OBJECT_ARRAY"
                          v-model="row.paramValue"
                          :min="JSON.parse(row.paramRules)[0]" :max="JSON.parse(row.paramRules)[1]"
                          @change="moreValueChangeAdapter($event, row, $index)"
                          controls-position="right"
                        />
                        <el-switch v-else-if="row.paramType === PORTAL_PARAM_TYPE.BOOLEAN" v-model="row.paramValue"
                                   active-value="true" inactive-value="false"/>
                        <el-input v-else v-model.trim="row.paramValue"/>
                      </el-form-item>
                    </template>
                  </el-table-column>
                  <el-table-column :label="$t('components.ParamsConfig.defaultValue')" prop="defaultParamValue"
                                   show-overflow-tooltip/>
                  <el-table-column :label="$t('components.ParamsConfig.5q0aazspptw0')" prop="paramDesc"
                                   show-overflow-tooltip/>
                </el-table>
              </el-form>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
    <template #footer>
      <div class="footer-con">
        <el-button v-if="props.mode === 2 && (basicEditData.length || moreEditData.length)" type="default"
                   style="margin-right: 10px" @click="resetDefault">{{ $t('components.ParamsConfig.5q0aazsppz40') }}
        </el-button>
        <el-button type="primary" @click="saveParams()">{{ $t('components.ParamsConfig.5q0aazspq4g0') }}</el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import {ref, reactive, watch, onMounted, toRaw, computed} from 'vue'
import {defaultParams} from '@/api/task'
import {mergeObjectArray} from '@/utils'
import {useI18n} from 'vue-i18n'
import {PORTAL_PARAM_TYPE} from '@/utils/constants'
import {getValidator} from './validators'
const activeCollapse = ref('0')
const {t} = useI18n()

const props = defineProps({
  open: Boolean,
  mode: [String, Number],
  globalParams: Object,
  taskInfo: Object
})

const emits = defineEmits(['update:open', 'syncGlobalParams', 'syncTaskParams'])

const visible = ref(false)
const basicFormRef = ref()
const moreFormRef = ref()
const customStyle = ref({
  borderRadius: '6px',
  marginBottom: '18px',
  border: 'none',
  overflow: 'hidden'
})

const defaultData = reactive({
  basic: [],
  more: []
})
const form = reactive({
  basicData: [],
  moreData: []
})
const basicEditData = ref([])
const moreEditData = ref([])
const taskParams = reactive({
  basic: [],
  more: []
})

const basicRowClass = (row) => {
  const tempRow = toRaw(row)
  if (basicEditData.value.some((item) => item.paramKey === row.row.paramKey)) {
    return 'row-changed'
  } else {
    return ''
  }
}

const moreRowClass = ({row, rowIndex}) => {
  const paramKey = toRaw(row).paramKey
  if (moreEditData.value.some((item) => item.paramKey === paramKey)) {
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
      activeCollapse.value = '0'
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
      if (fItem.paramValue != item.paramValue) {
        flag = true
      } else {
        const gItem = props.globalParams.basic.find(
          (gItem) => gItem.paramKey === item.paramKey
        )
        if (props.mode === 2 && gItem && gItem.paramValue != item.paramValue) {
          flag = true
        }
      }
      return flag
    })
  },
  {deep: true}
)

watch(
  () => form.moreData,
  (v) => {
    moreEditData.value = v.filter((item) => {
      let flag = false
      const fItem = defaultData.more.find(
        (vItem) => vItem.paramKey === item.paramKey
      )
      if (!fItem || fItem.paramValue != item.paramValue) {
        flag = true
      } else {
        const gItem = props.globalParams.more.find(
          (gItem) => gItem.paramKey === item.paramKey
        )
        if (props.mode === 2 && gItem && gItem.paramValue != item.paramValue) {
          flag = true
        }
      }
      return flag
    })
  },
  {deep: true}
)

const moreValueChangeAdapter = (val, row, rowIndex) => {
  row.paramValue = val
  moreValueChange(row, rowIndex)
}

const moreValueChange = (row, rowIndex) => {
  if (row.paramType === PORTAL_PARAM_TYPE.OBJECT_ARRAY) {
    const subData = JSON.parse(row.paramExtends)
    if (row.paramValue && row.paramValue !== '0') {
      const subKeyPrefixs = subData.map((sub) => sub.subKeyPrefix)
      form.moreData = form.moreData.filter(
        (item) =>
          item.paramType === PORTAL_PARAM_TYPE.OBJECT_ARRAY ||
          !subKeyPrefixs.some((prefix) => item.paramKey.includes(prefix))
      )
      const addNum = +row.paramValue
      const arr = []
      for (let i = 0; i < addNum; i++) {
        for (let j = 0; j < subData.length; j++) {
          const dotPos = subData[j]['subKeyPrefix'].lastIndexOf(".")
          let parentKey;
          if (dotPos > -1) {
            parentKey = subData[j]['subKeyPrefix'].substring(0, dotPos)
          }
          if (subData[j]['subKeyPrefix'].indexOf('override') > -1) parentKey = "type_override"
          arr.push({
            paramKey: subData[j]['subKeyPrefix'] + `${i + 1}`,
            parentKey,
            childIndex: i + 1,
            paramValue: subData[j]['paramValue'],
            paramDesc: subData[j]['desc'],
            paramType: subData[j]['paramType'],
            paramRules: subData[j]['paramRules']
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

const getDefaultParams = () => {
  defaultParams(props.taskInfo.sourceDbType).then((res) => {
    fillDefaultData(res.data)
    let basicRowCount = props.taskInfo.sourceDbType == 'MYSQL'? 13: 3
    defaultData.basic = res.data.slice(0, basicRowCount)
    defaultData.more = res.data.slice(basicRowCount)
    const data = JSON.parse(JSON.stringify(res.data))
    form.basicData = data.slice(0, basicRowCount)
    form.moreData = data.slice(basicRowCount)

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
          return findItem ? {...item, ...findItem} : item
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
      fillDefaultData(taskParams.basic)
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
        moreEditData.value = taskParams.more.filter(
          v => {
            let flag = true
            if (v.parentKey) {
              const value = taskParams.more.find(e => e.paramKey === v.parentKey).paramValue
              flag = v.childIndex <= taskParams.more.find(e => e.paramKey === v.parentKey).paramValue
            }
            return flag
          }
        )
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
          return findItem ? {...item, ...findItem} : item
        })
        form.moreData = form.moreData.filter(
          v => {
            let flag = true
            if (v.parentKey) {
              flag = v.childIndex <= form.moreData.find(e => e.paramKey === v.parentKey).paramValue
            }
            return flag
          }
        )
      }
    }
  })
}

const fillDefaultData = (data) => {
  const defaultBasicDataMap = new Map(
    defaultData.basic.map(item => [item.paramKey, item])
  );
  data.forEach((item) => {
    if (
      item.paramType === PORTAL_PARAM_TYPE.VAR &&
      item.paramKey === 'opengauss.database.schema'
    ) {
      item.paramValue = props.taskInfo?.sourceDBName
      item.defaultParamValue = props.taskInfo?.sourceDBName
    }
    if (
      item.paramType === PORTAL_PARAM_TYPE.VAR &&
      item.paramKey === 'schema.mappings'
    ) {
      const result = Object.entries(props.taskInfo?.sourceSchema)
        .filter(([_, value]) => value != null)
        .map(([_, value]) => `${value}:${value}`)
        .join(',')
      item.paramValue = result
      item.defaultParamValue = result
    }
    if (item && item.defaultParamValue === undefined && defaultBasicDataMap.get(item.paramKey)) {
      item.defaultParamValue = defaultBasicDataMap.get(item.paramKey).defaultParamValue
    }
  })
}

const resetDefault = () => {
  basicEditData.value = []
  moreEditData.value = []
  form.basicData = JSON.parse(JSON.stringify(defaultData.basic))
  form.moreData = JSON.parse(JSON.stringify(defaultData.more))
}

const saveParams = async () => {
  try {
    await basicFormRef.value.validate()
    await moreFormRef.value.validate()
    if (props.mode === 1) {
      emits('syncGlobalParams', {basic: basicEditData.value, more: moreEditData.value});
    } else {
      emits('syncTaskParams', {basic: basicEditData.value, more: moreEditData.value});
    }
    visible.value = false;
  } catch (error) {
    console.log('验证失败:', error);
  }
};

onMounted(() => {
  visible.value = props.open
})
</script>

<style lang="less" scoped>
.el-drawer.custom-drawer {
  overflow-x: hidden !important;
}

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

.page-input-size {
  .el-form-item .el-input,
  .el-form-item .el-select,
  .el-form-item .el-input-number {
    width: 259px;
  }
}

</style>
