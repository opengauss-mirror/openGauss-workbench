<template>
  <div class="card" style="overflow: visible" ref="card">
    <div class="card-header">
      <div class="icon"></div>
      <div class="title">
        {{ props.title }}
        <ShowInfo :info="info" v-if="showBtns || showQuestion" />
      </div>
      <div class="buttons">
        <slot name="headerExtend"></slot>
        <div class="card-links">
          <el-link :underline="false" @click="expand" v-if="showBtns || showExpand">
            <svg-icon name="Vector" />
          </el-link>
          <el-link :underline="false" @click="() => myEmit('download', props.title)" v-if="showBtns || showDownload">
            <svg-icon name="download" />
          </el-link>
        </div>
      </div>
    </div>
    <div
      class="card-body"
      :class="{ 'is-collapse': isCollapse }"
      ref="bodyRef"
      :style="{
        resize: props.resize ? 'vertical' : 'none',
      }"
      style="overflow: visible"
    >
      <div style="position: relative; height: 100%; overflow: visible">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useDebounceFn } from '@vueuse/core'
import { useWidthOverflow } from '@/hooks/dom'
import ShowInfo from "@/components/ShowInfo.vue";

const props = withDefaults(
  defineProps<{
    title: string;
    legend?: { color: string; name: string }[];
    bodyPadding?: boolean;
    height?: string | number;
    collapse?: boolean;
    skipBodyHeight?: boolean // do not set body height;
    // enable resize bodyHeight
    resize?: boolean;
    maxBodyHeight?: string;
    overflowHidden?: boolean;
    withTable?: boolean;
    showBtns: boolean;
    showQuestion: boolean;
    showExpand: boolean;
    showDownload: boolean;
    info: {title: string, option: any[]};
  }>(),
  {
    bodyPadding: true,
    maxBodyHeight: 'unset',
    overflowHidden: true,
    withTable: false,
    showBtns: false,
    showQuestion: false,
    showExpand: false,
    showDownload: false,
    info: () => { return { title: '', option: [] } },
  }
)
const hidden = ref(props.overflowHidden ? 'hidden' : 'scroll')
const padding = ref(props.bodyPadding ? '24px 16px' : '0px')
const withTableBorderRadius = ref(props.withTable ? '2px 2px 0 0' : '2px')
const bodyRef = ref<HTMLDivElement>()
const bodyHeight = ref(`calc(100% - ${props.bodyPadding ? 90 : 42}px)`)
const minBodyHeight = ref(bodyHeight.value)
// unset transition animation when resize body height
onMounted(() => {
  nextTick(() => {
    if (!props.skipBodyHeight && bodyRef && bodyRef.value) {
      bodyHeight.value = `${bodyRef.value.offsetHeight + (props.bodyPadding && !props.collapse ? 48 : 0)}px`
    }
    if (bodyRef && bodyRef.value && props.resize && window && 'ResizeObserver' in window) {
      const resizeObserver = new ResizeObserver((entries) => {
        for (const entry of entries) {
          if (!collapsing.value) {
            bodyHeight.value = `${entry.contentRect.height}px`
            if (minBodyHeight.value.startsWith('calc')) {
              minBodyHeight.value = bodyHeight.value
            }
            myEmit('resize-body-height', entry.contentRect.height)
          }
        }
      })
      resizeObserver.observe(bodyRef.value)
    }
  })
})
const myEmit = defineEmits<{
  (event: 'resize-body-height', height: number): void;
  (event: 'download', title: string): void;
  (event: 'expand'): void;
}>()
// ctl height
const height = computed(() => {
  if (!props.height) {
    return '100%'
  }
  return ['%', 'px'].includes(`${props.height}`) ? props.height : `${props.height}px`
})
// ctl collapse
const isCollapse = ref(false)
// skip ResizeObserver event when collapsing
const collapsing = ref(false)
const resetCollapsing = useDebounceFn(() => {
  collapsing.value = false
  if (bodyRef && bodyRef.value) {
    bodyRef.value.style.transition = 'unset'
    bodyRef.value.style.minHeight = isCollapse.value ? '0px' : minBodyHeight.value
  }
}, 500)
const toggleCollapse = () => {
  collapsing.value = true
  if (bodyRef && bodyRef.value) {
    bodyRef.value.style.transition = 'height 0.5s cubic-bezier(0.23, 1, 0.32, 1)'
    bodyRef.value.style.minHeight = !isCollapse.value ? '0px' : minBodyHeight.value
  }
  isCollapse.value = !isCollapse.value
  resetCollapsing()
}
const extraRef = ref()
const dropdownRef = ref()
const { trigger } = useWidthOverflow(extraRef, dropdownRef)
defineExpose({
  trigger,
})

const card = ref()
const isExpand = ref<boolean>(false)
const expand = () => {
  let ref = card.value
  if (!isExpand.value) {
    ref.style.position = 'fixed'
    ref.style.top = 0
    ref.style.right = 0
    ref.style.left = 0
    ref.style.bottom = 0
    ref.style.zIndex = 9999
    ref.style.height = '100vh'
  } else {
    ref.style.position = 'relative'
    ref.style.top = 'auto'
    ref.style.right = 'auto'
    ref.style.left = 'auto'
    ref.style.bottom = 'auto'
    ref.style.zIndex = 'auto'
    ref.style.height = height.value
  }
  isExpand.value = !isExpand.value
}
</script>

<style scoped lang="scss">
.card {
  box-sizing: border-box;
  height: v-bind(height);
  border: 1px solid var(--el-og-border-color);
  border-radius: v-bind(withTableBorderRadius);
  overflow: v-bind(hidden);
  .card-header {
    display: flex;
    overflow: hidden;
    justify-content: space-between;
    padding: 0 15px;
    align-items: center;
    min-width: 0;
    height: 40px;
    border-bottom: 1px solid var(--el-og-border-color);
    background-color: var(--background-color-1);
    .icon {
      width: 4px;
      height: 14px;
      background: var(--primary-6);
      border-radius: 1px;
      margin-right: 4px;
    }
    .title {
      font-weight: 500;
      font-size: 14px;
      line-height: 30px;
      flex-shrink: 0;
      > svg {
        position: relative;
        top: -2px;
        margin-right: 12px;
        transition: transform var(--el-transition-duration);
        &.is-collapse {
          transform: rotate(-90deg);
        }
      }
      .question {
        margin: 0px 4px;
        top: 40%
      }
    }
    .tips {
      margin-left: 6px;
      padding-top: 2px;
    }
    .buttons {
      position: relative;
      display: flex;
      justify-content: flex-end;
      margin-left: 16px;
      overflow: hidden;
      flex: 1;
      min-width: 0;
      align-items: center;
      font-size: 12px;
      height: 40px;
      > div {
        display: flex;
        flex-shrink: 0;
        margin-right: 8px;
        min-width: 0;
        > span {
          margin-left: 4px;
        }
        &:last-of-type {
          margin-right: 0;
        }
      }
    }
    &--extra {
      position: relative;
      display: flex;
      margin-left: 16px;
      overflow: hidden;
      flex: 1;
      min-width: 0;
      align-items: center;
      font-size: 12px;
      height: 40px;
      > div {
        display: flex;
        flex-shrink: 0;
        margin-right: 12px;
        min-width: 0;
        > span {
          margin-left: 4px;
        }
        &:last-of-type {
          margin-right: 0;
        }
      }
    }
  }
  &-body {
    position: relative;
    box-sizing: border-box;
    height: v-bind(bodyHeight);
    min-height: v-bind(minBodyHeight);
    max-height: v-bind(maxBodyHeight);
    overflow: v-bind(hidden);
    background-color: var(--el-bg-color-og);
    border-radius: 0 0 8px 8px;
    will-change: height;
    > div {
      box-sizing: border-box;
      padding: v-bind(padding);
      height: v-bind(bodyHeight);
    }
    &.is-collapse {
      height: 0 !important;
    }
  }
}
.hover {
  display: flex;
  align-items: center;
  > span {
    margin-left: 4px;
  }
}
.rect {
  width: 16px;
  height: 16px;
  flex-shrink: 0;
}
</style>
