<template>
  <div class="fusionSelect" v-click-outside="handleClickOutside">
    <div id="mainContent" :class="{ focus: (focus || !outClick) }">
      <!-- display the tags and the outer input -->
      <el-tooltip v-for="(val, key) in selectedData" :key="val.keyValue" :content="`${val.keyLabel}：${val.paramLabel}`"
        placement="top">
        <el-tag class="tag" closable @close="closeTag(key)" :style="`max-width: ${tagMaxWidth}px;`">{{
          `${val.keyLabel}：${val.paramLabel}` }}</el-tag>
      </el-tooltip>
      <span class="prefixTip" v-if="currentKeyLabel">{{ `${currentKeyLabel}：` }}</span>
      <div class="selectInputBind">
        <!-- input and the hiding control value area -->
        <div id="mainInput">
          <el-input :maxlength="maxLen"
            :placeholder="isLabelSelect ? $t('components.FusionSearch.addFilters') : labelOptions[currentKeyValue]?.placeholder"
            @click="inputFocus" v-model="selectValue" @search="clickSearch" @keyup.enter="clickSearch">
            <template #suffix>
              <template v-if="clearAllVisible">
                <el-icon :size="16">
                  <IconXSolid @click="clearAll" />
                </el-icon>
                <el-divider direction="vertical"></el-divider>
              </template>
              <el-icon :size="16">
                <IconSearch @click="clickSearch" />
              </el-icon>
            </template>
          </el-input>
        </div>
        <div class="mainSelect" id="mainSelect">
          <el-select v-model="selectValue" ref="mainSelectRef" @blur="handleBlur" v-if="isLabelSelect">
            <el-option v-for="item in labelOptions" :key="item.value" :value="item.value">{{ item.label }}</el-option>
          </el-select>
          <!-- this area could be select、multiple select、time、date select -->
          <template v-else>
            <component :is="componentIds[componentId]" :options="options" @blur="getParam" :focus="focus"
              @cancel="handleCancel"></component>
          </template>
        </div>
      </div>
    </div>
    <div class="inputMaxWarning" v-if="componentId === searchType.INPUT && selectValue.length >= Number(maxLen)">
      {{ t('components.FusionSearch.inputMaxWarnText') }}
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, nextTick, computed } from 'vue'
import fusionSelect from './fusionSelect.vue'
import fusionMulti from './fusionMulti.vue'
import fusionDateRange from './fusionDateRange.vue'
import { IconSearch, IconXSolid } from '@computing/opendesign-icons'
import { ClickOutside as vClickOutside } from 'element-plus'
import { searchType } from '@/types/searchType'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const props = defineProps({
  labelOptions: {
    type: Object,
    required: true
  },
  tagMaxWidth: {
    type: [String, Number],
    require: false,
    default: 350
  }
})
const labelOptions = computed(() => props.labelOptions)
const componentIds = {
  SELECT: fusionSelect,
  MULTIPLESELECT: fusionMulti,
  DATERANGE: fusionDateRange,
}
interface selectParams {
  keyLabel: string,
  keyValue: string,
  paramLabel: string,
  paramValue: any,
}

type dynamicParams = {
  [key: string]: selectParams
}

const componentId = ref('')
const isLabelSelect = ref(true)
const selectValue = ref('')
const currentKeyLabel = ref('') // Chinese editing key name
const currentKeyValue = ref('') // English editing key name
// this variable maintain params, display tags and the return params comes form it.
const selectedData = ref<dynamicParams>({})
const maxLen = ref('50')
const clearAllVisible = computed(() => {
  return currentKeyValue.value || Object.keys(selectedData.value).length
})

const closeTag = (key: string) => {
  delete selectedData.value[key]
  // research after delete
  clickSearch()
}

const clearAll = () => {
  selectedData.value = {}
  currentKeyLabel.value = ''
  currentKeyValue.value = ''
  focus.value = false
  isLabelSelect.value = true
  // research after delete
  clickSearch()
}

const options = ref([])
const handleBlur = () => {
  if (isLabelSelect.value) {
    componentId.value = labelOptions.value[selectValue.value]?.selectType
    options.value = labelOptions.value[selectValue.value]?.options
    isLabelSelect.value = false
    currentKeyValue.value = selectValue.value
    currentKeyLabel.value = labelOptions.value[selectValue.value].label
    selectValue.value = ''
    // The input field needs to set the maximum input length
    if (componentId.value === searchType.INPUT) {
      maxLen.value = labelOptions.value[currentKeyValue.value]?.maxlength || '50'
    }
  }
}

const outClick = ref(true)
const handleClickOutside = () => {
  outClick.value = true
}

const handleCancel = () => {
  // reset focus so that next time the inner watcher will be trigger
  focus.value = false
}
const getParam = (paramLabel: string, paramValue: any): void => {
  if (isLabelSelect.value) {
    // if the user is choosing label, the input value should be considered as invalid input and discarded.
    return
  }
  focus.value = false
  isLabelSelect.value = true
  selectedData.value[currentKeyValue.value] = { keyLabel: currentKeyLabel.value, keyValue: currentKeyValue.value, paramLabel, paramValue }
  currentKeyValue.value = ''
  currentKeyLabel.value = ''
}
const emit = defineEmits(['clickSearch'])

const clickSearch = () => {
  if (componentId.value === searchType.INPUT && selectValue.value) {
    // handle no option situation, get params from input
    getParam(selectValue.value, selectValue.value)
  }
  selectValue.value = ''
  const outputData = Object.keys(selectedData.value).reduce((acc, key) => {
    acc[key] = selectedData.value[key]?.paramValue
    return acc
  }, {})
  outClick.value = true
  emit('clickSearch', outputData)
}

const focus = ref(false) // control the child component getting the focus
const mainSelectRef = ref(null)
const inputFocus = (e) => {
  nextTick(() => {
    e.stopPropagation()
    const mainContent = document.querySelector('#mainContent')
    // focus, so that you can input and display the drop menu
    mainContent?.focus()
    mainSelectRef.value?.toggleMenu()
    focus.value = true
    outClick.value = false
  })
}
</script>

<style scoped lang="less">
.fusionSelect {
  position: relative;
  border-radius: var(--o-border-radius-small);

  .focus {
    box-shadow: 0 0 0 1px inset var(--o-color-primary) !important;
    background-color: #fdfeff !important;
  }

  #mainContent {
    box-shadow: 0 0 0 1px inset var(--o-border-color-lighter);
    position: relative;
    display: flex;
    flex-wrap: wrap;
    gap: 8px;

    &:hover {
      box-shadow: 0 0 0 1px inset var(--o-color-primary);
      background-color: #fdfeff;
    }

    .prefixTip {
      font-size: 12px;
      padding: 8px 0px;

      &:first-of-type {
        padding-left: 16px;
      }
    }

    .tag {
      margin: 4px 0px;

      &:first-of-type {
        margin-left: 16px;
      }

    }

    .selectInputBind {
      flex: 1;
      position: relative;

      :deep(.el-input__wrapper) {
        background-color: var(--o-input-bg-color);
        box-shadow: none;
        border: none;
        padding-left: 0px;
        box-sizing: border-box;
      }

      &:first-child {
        :deep(.el-input__wrapper) {
          padding-left: 16px;
        }
      }

      :deep(.el-icon) svg {
        color: var(--o-text-color-tertiary);
        cursor: pointer;

        &:hover {
          color: var(--o-color-primary-secondary)
        }

        &:active {
          color: var(--o-color-primary)
        }

      }

      .mainSelect {
        position: absolute;
        top: 0;
        flex: 1;
        z-index: -999;

        :deep(.el-select) {
          clip-path: inset(100%);
        }
      }
    }
  }
}
</style>
