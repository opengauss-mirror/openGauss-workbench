<template>
  <div>
    <el-steps v-bind="$attrs">
      <el-step v-for="(item, index) in props.steps" :key="item.title" :description="item.description" :title="item.title"
        :status="getStatus(item.stepIndex)" @click="clickNode(item.stepIndex)"
        :class="{ pointer: props.active && item.stepIndex <= props.current }">
        <!-- enlarge double circle, clicked node or current node   -->
        <template #icon v-if="props.active ? item.stepIndex === props.active : item.stepIndex === props.current">
          <div class="doubleCircle outer">
            <div class="doubleCircle inner"></div>
          </div>
          <div class="currentMark">{{ t('detail.index.currentPage') }}</div>
        </template>
        <!-- gray circle -->
        <template #icon v-else-if="item.stepIndex > props.current">
          <div class="circle"></div>
        </template>
        <template #icon v-else-if="item.stepIndex === 2 && props.subTaskDbType?.toUpperCase() === 'POSTGRESQL'">
          <div class="lockCircle">
            <svg-icon icon-class="circleCheck"></svg-icon>
          </div>
        </template>
        <!-- checked node  -->
        <template #icon v-else>
          <div class="checkCircle">
            <svg-icon icon-class="circleCheck"></svg-icon>
          </div>
        </template>
      </el-step>
    </el-steps>
  </div>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const props = defineProps({
  active: {
    type: Number,
    required: false
  },
  steps: {
    type: Object,
    required: true,
  },
  current: {
    type: Number,
    required: true,
  },
  subTaskDbType: {
    type: String,
  }
})
const getStatus = (index) => {
  if (index > props.current) {
    return 'wait'
  } else if (index < props.current) {
    return 'finish'
  } else {
    return 'process'
  }
}

const emits = defineEmits(['update:active'])
const clickNode = (index) => {
  if (index === 2 && props.subTaskDbType.toUpperCase() === 'POSTGRESQL') {
    return
  }
  if (index > props.current) {
    return
  }
  emits('update:active', index)
}
</script>

<style scoped lang="less">
:deep(.el-steps--horizontal:not(.o-cluster) .el-step__title) {
  background-color: var(--o-bg-color-light2);
  font-size: 14px;
}

:deep(.el-steps--horizontal:not(.o-cluster) .el-step__description) {
  background-color: inherit;
}

:deep(.el-steps--horizontal) {
  background-color: inherit;
}

.pointer {
  cursor: pointer;
}

.circle {
  width: 20px;
  height: 20px;
  border: 1px solid #8d98aa;
  border-radius: 50%;
}

.doubleCircle {
  position: absolute;
  border-radius: 50%;
}

.outer {
  width: 24px;
  height: 24px;
  border: 2px solid var(--o-color-primary)
}

.inner {
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 12px;
  height: 12px;
  border: 1px solid var(--o-color-primary)
}

:deep(.el-steps--horizontal:not(.o-cluster) .el-step__head.is-process .el-step__icon) {
  transform: scale(1)
}

:deep(.el-steps--horizontal:not(.o-cluster) .el-step__line) {
  background: var(--o-border-color-lighter);
}

.currentMark {
  position: absolute;
  left: 26px;
  top: -18px;
  width: 64px;
  height: 16px;
  background-color: transparent;
  color: var(--o-color-primary);
}

.lockCircle {
  position: relative;
  display: inline-block;
  box-sizing: border-box;
  cursor: not-allowed;
  &:before {
    content: '';
    position: absolute;
    margin: 2px;
    width: 80%;
    height: 80%;
    background-color: var(--o-border-color-base);
    border-radius: 50%;
    z-index: -1;
  }

  svg {
    color: var(--o-border-color-lighter);
    font-size: 20px;
    border-radius: 50%;
  }
}

.checkCircle {
  position: relative;
  display: inline-block;
  box-sizing: border-box;
  &:before {
    content: '';
    position: absolute;
    margin: 2px;
    width: 80%;
    height: 80%;
    background-color: var(--o-color-primary-fourth);
    border-radius: 50%;
    z-index: -1;
  }

  svg {
    color: var(--o-color-primary);
    font-size: 20px;
    border-radius: 50%;
  }
}
</style>
