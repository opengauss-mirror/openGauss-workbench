<template>
    <el-date-picker v-model="selectValue" type="daterange" ref="mainDateRangeRef" @change="handleBlur"
        v-bind="props.elProps || {}" @visible-change="visibleChange" placement="bottom-start">
    </el-date-picker>
</template>
  
<script lang="ts" setup>
import { nextTick, ref, watch } from 'vue'
import dayjs from 'dayjs'

const props = defineProps({
    elProps: {
        type: Object
    },
    focus: {
        type: Boolean,
        required: true
    }
})

const selectValue = ref<string[] | undefined>([])
// this component should get options and return param
const emit = defineEmits(['blur', 'cancel'])
const handleBlur = () => {
    const paramLabel = selectValue.value.map(v => dayjs(v).format('YYYY-MM-DD')).join(' - ')
    mainDateRangeRef.value?.blur()
    emit('blur', paramLabel, selectValue.value)
}
const visibleChange = () => {
    // when you choose nothing, emit cancel, so you can trigger panel again
    if (!selectValue.value || !selectValue.value.length) {
        emit('cancel')
    }
}
const mainDateRangeRef = ref(null)
watch(() => props.focus, () => {
    if (props.focus) {
        nextTick(() => {
            mainDateRangeRef.value?.focus()
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