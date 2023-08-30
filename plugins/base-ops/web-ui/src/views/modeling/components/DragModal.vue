
<template>
  <transition mode="out-in" name="d-drag-modal-animation">
    <div
      v-show="visible ? visible : false"
      ref="dDragModalRef"
      class="drag-model-container"
      :class="[modalClass ? modalClass : '', minimize ? 'd-drag-modal-is-minimize' : '']"
      :style="dDragModalStyle"
    >
      <div
          class="d-drag-modal-minimize"
          @click="toggleMinimize"
          @mousedown="dragMouseDown"
      >
        <icon-expand />
      </div>
      <div
        class="d-drag-modal-body"
        :style="{ width: width ? width : '300px', height: height ? height : 'auto' }"
      >
        <div
          class="d-drag-context"
          @mousedown="dragMouseDown"
        >
          <div class="title" ref="dDragContextTextRef">{{ title }}</div>
          <div class="close" @click="toggleMinimize"><icon-minus /></div>
        </div>
        <div class="d-drag-content">
          <slot></slot>
        </div>
      </div>
    </div>
  </transition>
</template>
<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
const props = defineProps<{
  width?: string,
  height?: string,
  visible?: boolean,
  title?: string,
  modalClass?: string,
  renderToBody?: boolean
}>()
const dDragModalRef = ref<HTMLElement>()
const dDragContextTextRef = ref<HTMLElement>()
onMounted(() => {
  dDragContextTextRef.value?.addEventListener('selectstart', (e: Event) => {
    e.preventDefault()
  })
})
watch(computed(() => props.visible), (value) => {
  if (value) {
    minimize.value = false
    modalInfo.right = 30
    modalInfo.top = 30
  }
})
const modalInfo = reactive({ right: 30, top: 30 })
const dDragModalStyle = computed(() => {
  return {
    width: props.width ? props.width : '300px',
    height: props.height ? props.height : 'auto',
    right: modalInfo.right + 'px',
    top: modalInfo.top + 'px'
  }
})
const minimize = ref<boolean>(false)
const toggleMinimize = () => {
  minimize.value = !minimize.value
}
const noMinimize = () => {
  if (minimize.value) minimize.value = false
}
let draging = false
let x = 0, y = 0, maxX = 0, maxY = 0
const dragMouseDown = (e: MouseEvent) => {
  let parnetDom = dDragModalRef.value?.parentElement
  maxX = parnetDom ? parnetDom.offsetWidth : 0
  maxY = parnetDom ? parnetDom.offsetHeight : 0
  draging = true
  x = e.pageX
  y = e.pageY
  let et = e || window.event
  et.preventDefault()
  let app: HTMLElement | null = document.getElementById('app')
  app?.addEventListener('mousemove', dragMouseMove)
  app?.addEventListener('mouseup', dragMouseUp)
}
const dragMouseMove = (e: MouseEvent) => {
  if (draging) {
    let { pageX, pageY } = e
    let diffX = pageX - x
    let diffY = pageY - y
    let nowX = modalInfo.right - diffX
    let nowY = modalInfo.top + diffY
    if (nowX > 0 && (maxX > 0 ? (nowX + (dDragModalRef.value ? dDragModalRef.value.offsetWidth : 0)) < maxX : true)) {
      modalInfo.right = nowX
      x = pageX
    }
    if (nowY > 0 && (maxY > 0 ? (nowY + (dDragModalRef.value ? dDragModalRef.value.offsetHeight : 0)) < maxY : true)) {
      modalInfo.top = nowY
      y = pageY
    }
  }
}
const dragMouseUp = () => {
  draging = false
  let app: HTMLElement | null = document.getElementById('app')
  app?.removeEventListener('mousemove', dragMouseMove)
  app?.removeEventListener('mouseup', dragMouseUp)
}
defineExpose({ noMinimize })
</script>
<style scoped lang="less">
  .drag-model-container {
    position: absolute;
    right: 30px;
    top: 30px;
    z-index: 999;
    min-height: 460px;
    background-color: #fff;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    background: rgb(255, 255, 255);
    box-shadow: 0px 3px 26px rgba(135, 118, 159, 0.28);
    border-radius: 10px;
    .d-drag-modal-minimize {
      position: absolute;
      right: 0;
      top: 0;
      opacity: 0;
      width: 22px;
      height: 22px;
      line-height: 22px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      cursor: pointer;
      margin-left: auto;
      background-color: #fff;
      transition: background-color .3s;
      z-index: -1;
      > svg {
        font-size: 12px;
        transform: translateY(1px);
      }
      &:hover {
        background-color: #dbdbdb;
      }
    }
    .d-drag-modal-body {
      min-height: 460px;
      max-height: 700px;
      opacity: 1;
      transition: opacity .3s;
      overflow: auto;
      .d-drag-context {
        height: 36px;
        line-height: 36px;
        cursor: move;
        display: flex;
        align-items: center;
        box-sizing: border-box;
        padding: 0 10px;
        border-bottom: 1px solid #e2e2e2;
        .title {
          font-weight: bold;
          -webkit-touch-callout:none;
          -webkit-user-select:none;
          -khtml-user-select:none;
          -moz-user-select:none;
          -ms-user-select:none;
          user-select:none;
        }
        .close {
          width: 22px;
          height: 22px;
          line-height: 22px;
          display: flex;
          align-items: center;
          justify-content: center;
          border-radius: 50%;
          cursor: pointer;
          margin-left: auto;
          background-color: transparent;
          transition: background-color .3s;
          > svg {
            font-size: 12px;
            transform: translateY(1px);
          }
          &:hover {
            background-color: #dbdbdb;
          }
        }
      }
      .d-drag-content {
        height: calc(100% - 36px);
        max-height: calc(100% - 36px);
        overflow: auto;
        box-sizing: border-box;
        padding: 0 0 15px 0;
      }
    }
  }
  .d-drag-modal-is-minimize {
    min-height: 22px !important;
    width: 22px !important;
    height: 22px !important;
    .d-drag-modal-body {
      opacity: 0;
    }
    .d-drag-modal-minimize {
      z-index: 1;
      opacity: 1;
    }
  }
  .d-drag-modal-animation-enter-active {
    transition: all 0.3s;
  }
  .d-drag-modal-animation-leave-active {
    transition: all 0.3s;
  }
  .d-drag-modal-animation-enter-from,
  .d-drag-modal-animation-leave-to {
    transform: translateY(-20px);
    opacity: 0;
  }
</style>
