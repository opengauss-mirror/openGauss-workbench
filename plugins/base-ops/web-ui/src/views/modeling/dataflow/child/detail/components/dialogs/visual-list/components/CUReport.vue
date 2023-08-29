
<template>
  <a-modal
    class="cu-report-container"
    :visible="visible"
    :title="$t('modeling.components.CUReport.5m7iq3dyy200')"
    :ok-text="$t('modeling.components.CUReport.5m7iq3dyzf80')"
    @cancel="close"
    @ok="saveReport"
    body-style="padding: 0; width: 100%;"
    :modal-style="{ minWidth: '1480px' }"
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
          <grid-item :key="item.i" v-for="(item) in layout"
            :x="item.x"
            :y="item.y"
            :w="item.w"
            :h="item.h"
            :i="item.i"
            style="border: 1px solid blue;"
          >
            <a-dropdown trigger="contextMenu" alignPoint :style="{ display: 'block' }">
              <div class="grid-item-content">
                <img :src="item.imgBase64" />
              </div>
              <template #content>
                <a-doption @click="removeItem(item.i)">{{$t('modeling.components.CUReport.5m7iq3dyzls0')}}</a-doption>
              </template>
            </a-dropdown>
          </grid-item>
        </grid-layout>
      </div>
      <div class="config">
        <div class="config-item base">
          <div class="ci-title">{{$t('modeling.components.CUReport.5m7iq3dyzps0')}}</div>
          <div class="config-row">
            <div class="text">{{$t('modeling.components.CUReport.5m7iq3dyzsg0')}}</div>
            <div class="value"><a-input :max-length="140" show-word-limit  v-model="config.name" :placeholder="$t('modeling.components.CUReport.5m7iq3dyzvw0')" /></div>
          </div>
          <div class="config-row">
            <div class="text">{{$t('modeling.components.CUReport.5m7iq3dyzyo0')}}</div>
            <div class="value">
              <a-select v-model="config.type" :placeholder="$t('modeling.components.CUReport.5m7iq3dz0140')">
                <a-option :value="1">{{$t('modeling.components.CUReport.5m7iq3dz0400')}}</a-option>
              </a-select>
            </div>
          </div>
        </div>
        <div class="config-item snapshot-list">
          <div class="ci-title">{{$t('modeling.components.CUReport.5m7iq3dz0780')}}</div>
          <div class="snapshot-list-content">
            <a-tooltip v-for="(item, key) in snapshotListFilter" :key="`s${key}`" :content="item.name">
              <div class="snapshot-item">
                <img
                  :src="item.imgBase64"
                  draggable="true" unselectable="on"
                  @click="addItem(item)"
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
import { computed, reactive, ref } from 'vue'
import { KeyValue } from '@antv/x6/lib/types'
import { Message } from '@arco-design/web-vue'
import { useRoute } from 'vue-router'
import { modelingVRAdd, modelingVRUpdate } from '@/api/modeling'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const route = useRoute()
const props = defineProps<{ snapshotList: KeyValue[] }>()
const emits = defineEmits(['success'])
const snapshotListFilter = computed(() => {
  return props.snapshotList.filter((item: KeyValue) => {
    return layout.value.findIndex((item2: ItemType) => Number(item2.i) === Number(item.id)) === -1
  })
})
const visible = ref<boolean>(false)
const open = (type: string, data?: any) => {
  visible.value = true
  if (type === 'create') {
    layout.value = []
    config.name = ''
    config.type = ''
    config.id = ''
  } else if (type === 'update') {
    layout.value = data && data.paramsJson ? JSON.parse(data.paramsJson) : []
    config.name = data.name
    config.type = data.type
    config.id = data.id
  }
}
const close = () => {
  visible.value = false
}
const config = reactive({
  name: '', type: '', id: ''
})
interface ItemType { x: number, y: number, w: number, h: number, i: number | string, imgBase64: string, chartDataJson: any }
const colNum = 120
const width = 20
const height = 10
const layout = ref<Array<ItemType>>([
])
const gridlayoutRef = ref<any>()
const addItem = (item: KeyValue) => {
  layout.value.push({
    x: (layout.value.length * width) % (colNum || 12),
    y: layout.value.length + (colNum || 12),
    w: width,
    h: height,
    i: item.id,
    imgBase64: item.imgBase64,
    chartDataJson: item.chartDataJson ? item.chartDataJson : {}
  })
  gridlayoutRef.value.layoutUpdate()
}
const removeItem = (val: any) => {
  const index = layout.value.map(item => item.i).indexOf(val)
  layout.value.splice(index, 1)
  gridlayoutRef.value.layoutUpdate()
}
const saveReport = () => {
  if (!config.name) {
    Message.error({ content: t('modeling.components.CUReport.5m7iq3dyzvw0') })
    return
  }
  if (!config.type) {
    Message.error({ content: t('modeling.components.CUReport.5m7iq3dz0140') })
    return
  }
  let sendData: KeyValue = {
    'name': config.name,
    'dataFlowId': window.$wujie?.props.data.id,
    'paramsJson': JSON.stringify(layout.value),
    'type': config.type
  }
  const resultFunc = (res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({ content: t('modeling.components.CUReport.5m7iq3dz09k0') })
      close()
      emits('success')
    }
  }
  if (config.id) {
    sendData.id = config.id
    modelingVRUpdate(sendData).then((res: KeyValue) => resultFunc(res))
  } else {
    modelingVRAdd(sendData).then((res: KeyValue) => resultFunc(res))
  }
}
defineExpose({ open })
</script>
<style scoped lang="less">
  .cu-report-container {
    .cu-report {
      height: calc(100vh - 24px - 24px - 48px - 65px - 50px);
      width: 100%;
      display: flex;
      min-width: 1024px;
      overflow: auto;
      box-sizing: border-box;
      padding: 20px;
      .draw {
        height: 100%;
        flex: 1;
        border: 1px solid var(--color-border-3);
        padding-right: 20px;
        margin-right: 20px;
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
        height: calc(100vh - 24px - 24px - 48px - 65px - 10px - 50px - 40px);
        display: flex;
        flex-direction: column;
        .config-item {
          box-shadow: 0px 2px 15px rgba(159, 118, 118, 0.21);;
          border-radius: 2px;
          .ci-title {
            color: rgb(var(--primary-6));
            font-weight: bold;
            font-size: 16px;
            border-bottom: 1px solid var(--color-border-3);
            box-sizing: border-box;
            padding: 10px 0;
          }
        }
        .base {
          margin-bottom: 20px;
          height: 150px;
          box-sizing: border-box;
          padding: 0 10px;
          .config-row {
            border-bottom: 1px solid var(--color-border-3);
            box-sizing: border-box;
            padding: 10px 0;
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
            overflow-x: hidden;
            .snapshot-item {
              margin: 0 auto;
              width: 200px;
              height: 200px;
              border: 1px solid var(--color-border-3);
              margin-bottom: 20px;
              border-radius: 4px;
              box-sizing: border-box;
              padding: 10px;
              > img {
                width: 100%;
                height: 100%;
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
