<template>
  <el-select v-model="selectValue" ref="mainSelectRef" @blur="handleBlur" v-bind="props.elProps || {}" @visible-change="visibleChange">
    <el-option v-for="item in options" :key="item.value" :value="item.value">
      {{ item.label }}
    </el-option>
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

const selectValue = ref<string | number | boolean>('')
// this component should get options and return param
const emit = defineEmits(['blur', 'cancel'])
const handleBlur = () => {
  const paramLabel = props.options.find(e => e.value === selectValue.value)?.label
  emit('blur', paramLabel, selectValue.value)
}
const visibleChange = () => {
    // when you choose nothing, emit cancel, so you can trigger panel again
  if (!selectValue.value) {
    emit('cancel')
  }
}
const mainSelectRef = ref(null)
watch(() => props.focus, () => {
    if (props.focus) {
      nextTick(() => {
        mainSelectRef.value?.toggleMenu()
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
</style>
