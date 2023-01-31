
<template>
  <a-modal
    class="cu-report-drag-container"
    :visible="visible"
    :title="$t('modeling.components.CUReportDrag.5m7ipr5lr5s0')"
    :ok-text="$t('modeling.components.CUReportDrag.5m7ipr5lrw40')"
    @cancel="close"
    @ok="saveReport"
    fullscreen
  >
    <div class="cu-report">
      <div class="draw" id="content">
        <grid-layout ref="gridlayoutRef" v-model:layout="layout"
          :col-num="colNum"
          :row-height="10"
          :is-draggable="true"
          :is-resizable="true"
          :vertical-compact="true"
          :use-css-transforms="true"
        >
          <grid-item :key="item.i" v-for="(item, key) in layout"
            :ref="(el: any) => gridItemRefs(el, key)"
            :x="item.x"
            :y="item.y"
            :w="item.w"
            :h="item.h"
            :i="item.i"
            style="border: 1px solid blue;"
          >
            <div class="grid-item-content">
              <img :src="item.imgBase64" />
            </div>
          </grid-item>
        </grid-layout>
      </div>
      <div class="config">
        <div class="config-item base">
          <div class="ci-title">{{$t('modeling.components.CUReportDrag.5m7ipr5ls0w0')}}</div>
          <div class="config-row">
            <div class="text">{{$t('modeling.components.CUReportDrag.5m7ipr5ls3k0')}}</div>
            <div class="value"><a-input :max-length="140" show-word-limit  v-model="config.name" :placeholder="$t('modeling.components.CUReportDrag.5m7ipr5ls800')" /></div>
          </div>
          <div class="config-row">
            <div class="text">{{$t('modeling.components.CUReportDrag.5m7ipr5lsao0')}}</div>
            <div class="value">
              <a-select v-model="config.type" :placeholder="$t('modeling.components.CUReportDrag.5m7ipr5lsd40')">
                <a-option>{{$t('modeling.components.CUReportDrag.5m7ipr5lsfc0')}}</a-option>
              </a-select>
            </div>
          </div>
        </div>
        <div class="config-item snapshot-list">
          <div class="ci-title">{{$t('modeling.components.CUReportDrag.5m7ipr5lsho0')}}</div>
          <div class="snapshot-list-content">
            <div>{{ layout }}</div>
            <a-tooltip v-for="(item, key) in snapshotList" :key="`s${key}`" :content="item.name">
              <div class="snapshot-item">
                <img
                  :src="item.imgBase64"
                  draggable="true" unselectable="on"
                  @drag="e => drag(e, key)"
                  @dragend="e => dragEnd(e, key)"
                />
              </div>
            </a-tooltip>
          </div>
        </div>
      </div>
    </div>
  </a-modal>
</template>
<script setup lang="ts">
import { nextTick, reactive, ref } from 'vue'
import { KeyValue } from '@antv/x6/lib/types'
import { Message } from '@arco-design/web-vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const route = useRoute()
const props = defineProps<{ snapshotList: KeyValue[] }>()
const visible = ref<boolean>(false)
const open = (type: string, data?: any) => {
  visible.value = true
  if (type === 'create') {
    layout.value = []
    config.name = ''
    config.type = ''
    config.id = ''
  } else if (type === 'update') {
    layout.value = JSON.parse(data.paramsJson)
    config.name = data.name
    config.type = data.type
    config.id = data.id
  }
  nextTick(() => {
    document.addEventListener('dragover', function (e) {
      mouseXY.x = e.clientX
      mouseXY.y = e.clientY
    }, false)
  })
}
const close = () => {
  document.removeEventListener('dragover', function (e) {
    mouseXY.x = e.clientX
    mouseXY.y = e.clientY
  }, false)
  visible.value = false
}
const config = reactive({
  name: '', type: '', id: ''
})
interface ItemType { x: number, y: number, w: number, h: number, i: number | string, imgBase64: string }
const colNum = 120
let mouseXY: any = { x: null, y: null }
let dragPos: ItemType = { x: 0, y: 0, w: 20, h: 10, i: -1, imgBase64: '' }
const layout = ref<Array<ItemType>>([
])
const gridlayoutRef = ref<any>()
let gridItemRef: any = []
const gridItemRefs = (el: HTMLElement, key: number) => {
  if (key === 0) gridItemRef = []
  gridItemRef.push(el)
}
const drag = (e: DragEvent, key: number) => {
  let drawDom = document.getElementById('content')
  if (drawDom && gridlayoutRef.value) {
    let parentRect = drawDom.getBoundingClientRect()
    let mouseInGrid = false
    if (((mouseXY.x > parentRect.left) && (mouseXY.x < parentRect.right)) && ((mouseXY.y > parentRect.top) && (mouseXY.y < parentRect.bottom))) {
      mouseInGrid = true
    }
    if (mouseInGrid === true && (layout.value.findIndex(item => item.i === 'drop')) === -1) {
      layout.value.push({
        x: (layout.value.length * 2) % (colNum || 12),
        y: layout.value.length + (colNum || 12),
        w: 20,
        h: 10,
        i: 'drop',
        imgBase64: props.snapshotList[key].imgBase64
      })
    }
    let index = layout.value.findIndex(item => item.i === 'drop')
    if (index !== -1) {
      try {
        gridItemRef[layout.value.length - 1].$refs.item.style.display = 'none'
      } catch {
        console.error(`gridItemRef[layout.value.length].$refs.item.style.display = 'none'`)
      }
      let el = gridItemRef[index]
      if (el) {
        el.dragging = { 'top': mouseXY.y - parentRect.top, 'left': mouseXY.x - parentRect.left }
        let new_pos = el.calcXY(mouseXY.y - parentRect.top, mouseXY.x - parentRect.left)
        if (mouseInGrid === true) {
          gridlayoutRef.value.dragEvent('dragstart', 'drop', new_pos.x, new_pos.y, 1, 1)
          dragPos.i = String(index)
          dragPos.x = layout.value[index].x
          dragPos.y = layout.value[index].y
        }
        if (mouseInGrid === false) {
          gridlayoutRef.value.dragEvent('dragend', 'drop', new_pos.x, new_pos.y, 1, 1)
          layout.value = layout.value.filter(obj => obj.i !== 'drop')
        }
      }
    }
  }
}
const dragEnd = (e: DragEvent, key: number) => {
  let drawDom = document.getElementById('content')
  if (drawDom && gridlayoutRef.value) {
    let parentRect = drawDom.getBoundingClientRect()
    let mouseInGrid = false
    if (((mouseXY.x > parentRect.left) && (mouseXY.x < parentRect.right)) && ((mouseXY.y > parentRect.top) && (mouseXY.y < parentRect.bottom))) {
      mouseInGrid = true
    }
    if (mouseInGrid === true) {
      gridlayoutRef.value.dragEvent('dragend', 'drop', dragPos.x, dragPos.y, 1, 1)
      layout.value = layout.value.filter(obj => obj.i !== 'drop')

      layout.value.push({
        x: dragPos.x,
        y: dragPos.y,
        w: 20,
        h: 10,
        i: dragPos.i,
        imgBase64: props.snapshotList[key].imgBase64
      })
      gridlayoutRef.value.dragEvent('dragend', dragPos.i, dragPos.x, dragPos.y, 1, 1)
      try {
        gridItemRef[layout.value.length].$refs.item.style.display = 'block'
      } catch {
        console.error('gridItemRef[layout.value.length].$refs.item.style.display = `block`')
      }
    }
  }
}
const saveReport = () => {
  if (!config.name) {
    Message.error({ content: t('modeling.components.CUReportDrag.5m7ipr5ls800') })
    return
  }
  if (!config.type) {
    Message.error({ content: t('modeling.components.CUReportDrag.5m7ipr5lsd40') })
    return
  }
  let sendData = {
    'name': config.name,
    'url': null,
    'intro': 'abc',
    'dataFlowId': window.$wujie?.props.data.id,
    'paramsJson': '',
    'type': config.type,
    'resourceUrl': null
  }
}
defineExpose({ open })
</script>
<style scoped lang="less">
  .cu-report-drag-container {
    .cu-report {
      height: calc(100vh - 24px - 24px - 48px - 65px);
      width: 100%;
      display: flex;
      min-width: 1024px;
      overflow: auto;
      .draw {
        height: 100%;
        flex: 1;
        border: 1px solid #DADCE7;
        padding-right: 20px;
        margin-right: 20px;
        background-color: #f7f8fa;
        overflow: auto;
        .grid-item-content {
          width: 100%;
          height: 100%;
          border: 1px solid rgb(var(--primary-6));
          border-radius: 2px;
          > img {
            width: 100%;
            height: 100%;
            object-fit: contain;
          }
        }
      }
      .config {
        width: 260px;
        margin-right: 10px;
        box-sizing: border-box;
        padding-top: 10px;
        height: calc(100vh - 24px - 24px - 48px - 10px);
        display: flex;
        flex-direction: column;
        .config-item {
          box-shadow: 0px 2px 15px rgba(159, 118, 118, 0.21);;
          border-radius: 2px;
          .ci-title {
            color: rgb(var(--primary-6));
            font-weight: bold;
            font-size: 16px;
            border-bottom: 1px solid #dededf;
            box-sizing: border-box;
            padding: 10px;
          }
        }
        .base {
          margin-bottom: 20px;
          height: 150px;
          .config-row {
            border-bottom: 1px solid #dededf;
            box-sizing: border-box;
            padding: 10px;
            display: flex;
            align-items: center;
            .text {
              width: 70px;
            }
            .value {
              flex: 1;
            }
            &:last-child {
              border-bottom: none;
            }
          }
        }
        .snapshot-list {
          height: 0;
          flex: 1;
          box-sizing: border-box;
          padding: 10px;
          display: flex;
          flex-direction: column;
          .ci-title {
            padding: 0;
            padding-bottom: 10px;
            margin-bottom: 10px;
          }
          .snapshot-list-content {
            flex: 1;
            overflow: auto;
            .snapshot-item {
              border: 1px solid #dededf;
              margin-bottom: 20px;
              border-radius: 4px;
              box-sizing: border-box;
              padding: 10px;
              > img {
                width: 218px;
                height: 218px;
                cursor: pointer;
              }
              &:last-child {
                margin-bottom: 0;
              }
            }
          }
        }
      }
    }
  }
</style>
<style lang="less">
  .cu-report {
    .vue-grid-item {
      border: none !important;
    }
  }
</style>
