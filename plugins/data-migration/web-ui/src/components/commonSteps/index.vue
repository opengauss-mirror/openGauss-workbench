<template>
  <div>
    <el-steps>
      <el-step v-for="(item, index) in props.steps" :key="item.title" :description="item.description" :title="item.title">
        <template #icon v-if="item.status === 'wait'">
          <div class="circle"></div>
        </template>
        <template #icon v-if="item.status === 'process'">
          <div class="doubleCircle outer">
            <div class="doubleCircle inner"></div>
          </div>
        </template>
        <template #icon v-if="item.status === 'finish'">
          <svg-icon icon-class="circleCheck" class="checkCircle"></svg-icon>
        </template>
      </el-step>
    </el-steps>
  </div>
</template>
  
<script setup>
const props = defineProps(['steps'])
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

.checkCircle {
  background-color: #f4a7a7;
  color: rgb(var(--primary-6));
  font-size: 20px;
  border-radius: 52%;
}</style>