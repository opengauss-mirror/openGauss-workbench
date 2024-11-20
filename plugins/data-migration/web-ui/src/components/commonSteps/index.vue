<template>
  <div>
    <el-steps v-bind="$attrs">
      <el-step v-for="(item, index) in props.steps" :key="item.title" :description="item.description" :title="item.title"
        :status="getStatus(item.stepIndex)">
        <template #icon v-if="item.stepIndex > props.current">
          <div class="circle"></div>
        </template>
        <template #icon v-if="item.stepIndex === props.current">
          <div class="doubleCircle outer">
            <div class="doubleCircle inner"></div>
          </div>
        </template>
        <template #icon v-if="item.stepIndex < props.current">
          <div class="checkCircle">
            <svg-icon icon-class="circleCheck"></svg-icon>
          </div>
        </template>
      </el-step>
    </el-steps>
  </div>
</template>

<script setup>
const props = defineProps({
  steps: {
    type: Object,
    required: true,
  },
  current: {
    type: Number,
    required: true,
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
</script>
  
<style scoped lang="less">
:deep(.el-steps--horizontal:not(.o-cluster) .el-step__title) {
  background-color: var(--color-bg-1);
  font-size: 14px;
}

:deep(.el-steps--horizontal:not(.o-cluster) .el-step__description) {
  background-color: inherit;
}

:deep(.el-steps--horizontal) {
  background-color: inherit;
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
  border: 2px solid rgb(var(--primary-6))
}

.inner {
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 12px;
  height: 12px;
  border: 1px solid rgb(var(--primary-6))
}

:deep(.el-steps--horizontal:not(.o-cluster) .el-step__head.is-process .el-step__icon) {
  transform: scale(1)
}

:deep(.el-steps--horizontal:not(.o-cluster) .el-step__title.is-process) {
  color: rgb(var(--primary-6));
}

:deep(.el-steps--horizontal:not(.o-cluster) .el-step__line) {
  background: var(--o-border-color-lighter);
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
    background-color: #f4a7a7;
    border-radius: 50%;
    z-index: -1;
  }

  svg {
    color: rgb(var(--primary-6));
    font-size: 20px;
    border-radius: 50%;
  }
}</style>