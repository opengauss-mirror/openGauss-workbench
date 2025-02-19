<template>
  <el-select v-model="selectValue" multiple collapse ref="mainMultiRef" v-bind="props.elProps || {}" @blur="handleBlur"
             @visible-change="handleVisibleChange" collapse-tags automatic-dropdown value-key="value">
    <el-option v-for="item in options" :key="item.value" :value="item" :disabled="item.disabled">
      {{ item.label }}
    </el-option>
    <template #footer>
      <div class="footerBtn">
        <el-button @click="confirm" type="primary" size="small">{{ $t('components.FusionSearch.confirm') }}</el-button>
        <el-button size="small" @click="cancel">{{ $t('components.FusionSearch.cancel') }}</el-button>
      </div>
    </template>
  </el-select>
</template>

<script lang="ts" setup>
import { nextTick, ref, PropType, watch } from 'vue'

interface option {
  label: string,
  value: any,
  disabled?: boolean
}
const props = defineProps({
  options: {
    type: Array as PropType<option[]>,
    required: true
  },
  elProps: {
    type: Object
  },
  focus: {
    type: Boolean,
    required: true
  }
})

const selectValue = ref([])
// this component should get options and return param
const emit = defineEmits(['blur', 'cancel'])

const confirm = () => {
  const labelList = selectValue.value?.map(item => item.label)?.join(',')
  const valueList = selectValue.value?.map(item => item.value)
  emit('blur', labelList, valueList)
}

const cancel = () => {
  mainMultiRef.value?.toggleMenu()
  emit('cancel')
}
const handleBlur = () => {
  emit('cancel')
}

const handleVisibleChange = () => {
  // the drop menu would hide after first click, so we should control its visible by focus method
  nextTick(() => {
    mainMultiRef.value?.focus()
  })
}

const mainMultiRef = ref(null)
watch(() => props.focus, () => {
    if (props.focus) {
      nextTick(() => {
        mainMultiRef.value?.toggleMenu()
      })
    }

  },
  { immediate: true }
)
</script>

<style scoped lang="less">
:deep(.el-select) {
  clip-path: inset(100%);
}

.footerBtn {
  display: flex;
  justify-content: center;
}
</style>
