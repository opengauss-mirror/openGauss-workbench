
<template>
  <a-tooltip :content="props.content" :disabled="isShowTooltip">
    <span @mouseover="onMouseOver">
      <slot ></slot>
    </span>
    <span class="get-width" ref="contentRef">{{ props.text }}</span>
  </a-tooltip>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { Tooltip as ATooltip, TriggerProps } from '@arco-design/web-vue'
interface propsType extends TriggerProps {
  content: string,
  text: string,
  otherWidth?: number,
  fontSize?: number
}
const props = defineProps<propsType>()
const contentRef = ref<any>()
const isShowTooltip = ref<boolean>(false)
const onMouseOver = () => {
  let parentWidth = contentRef.value.parentNode.offsetWidth
  let contentWidth = contentRef.value.offsetWidth + ((props.otherWidth || props.otherWidth === 0) ? props.otherWidth : 24)
  if (contentWidth > parentWidth) {
    isShowTooltip.value = false
  } else {
    isShowTooltip.value = true
  }
}
</script>
<style scoped lang="less">
  .get-width {
    position: absolute;
    left: 0;
    top: 0;
    opacity: 0;
    z-index: -9;
  }
</style>
