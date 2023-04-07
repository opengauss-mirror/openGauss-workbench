<template>
  <div class="header-container label-color">
    <div class="toggle-antv-left label-color" @click="clickBtn('antvLeft')">
      <icon-menu-fold v-show="antvLeftShow" />
      <icon-menu-unfold v-show="!antvLeftShow" />
      <div class="text">{{$t('modeling.components.Header.5m78qbczrv40')}}</div>
    </div>
    <a-tooltip :content="$t('modeling.components.Header.5m78qbczsmk0')">
      <div class="d-button" @click="clickBtn('save')">
        <icon-save />
      </div>
    </a-tooltip>
    <a-tooltip :content="$t('modeling.components.Header.5m78qbczsqw0')">
      <div class="d-button" @click="clickBtn('delete')">
        <icon-delete />
      </div>
    </a-tooltip>
    <a-tooltip :content="$t('modeling.dy_common.header.gridText')">
      <div class="d-button" @click="clickBtn('toggleGridType')">
        <icon-mosaic />
      </div>
    </a-tooltip>
    <a-tooltip :content="$t('modeling.components.Header.5m78qbczsto0')">
      <div class="d-button" @click="clickBtn('revoke')">
        <icon-undo />
      </div>
    </a-tooltip>
    <a-tooltip :content="$t('modeling.components.Header.5m78qbczswg0')">
      <div class="d-button" @click="clickBtn('recover')">
        <icon-refresh />
      </div>
    </a-tooltip>
    <a-tooltip :content="$t('modeling.components.Header.5m78qbczszg0')">
      <div class="d-button" @click="clickBtn('enlarge')">
        <icon-zoom-in />
      </div>
    </a-tooltip>
    <a-tooltip :content="$t('modeling.components.Header.5m78qbczt2k0')">
      <div class="d-button" @click="clickBtn('narrow')">
        <icon-zoom-out />
      </div>
    </a-tooltip>
    <a-tooltip :content="$t('modeling.components.Header.5m78qbczt4w0')">
      <div class="d-button" @click="clickBtn('center')">
        <icon-align-center />
      </div>
    </a-tooltip>
    <a-tooltip :content="$t('modeling.components.Header.5m78qbczt7g0')">
      <div class="d-button" @click="clickBtn('import')">
        <icon-import />
      </div>
    </a-tooltip>
    <a-tooltip :content="$t('modeling.components.Header.5m78qbczt9s0')">
      <a-dropdown>
        <div class="d-button">
          <icon-export />
        </div>
        <template #content>
          <a-doption @click="clickBtn('export', 'json')">{{$t('modeling.components.Header.5m78qbcztdc0')}}</a-doption>
          <a-doption @click="clickBtn('export', 'svg')">{{$t('modeling.components.Header.5m78qbcztg00')}}</a-doption>
          <a-doption @click="clickBtn('export', 'jpeg')">{{$t('modeling.components.Header.5m78qbcztio0')}}</a-doption>
          <a-doption @click="clickBtn('export', 'png')">{{$t('modeling.components.Header.5m78qbcztlk0')}}</a-doption>
        </template>
      </a-dropdown>
    </a-tooltip>
    <a-tooltip :content="$t('modeling.components.Header.5m78qbcztok0')">
    <div class="d-button" @click="clickBtn('code')">
      <icon-code-square />
    </div>
    </a-tooltip>
    <a-tooltip :content="$t('modeling.components.Header.5m78qbcztqs0')">
    <div class="d-button" @click="clickBtn('run')">
      <icon-play-circle />
    </div>
    </a-tooltip>
    <a-tooltip :content="$t('modeling.components.Header.5m78qbcztws0')">
      <div class="d-button" @click="clickBtn('visual')">
        <icon-bar-chart />
      </div>
    </a-tooltip>
    <input
      type="file"
      id="files"
      ref="refFile"
      style="display: none"
      v-on:change="fileLoad"
    />
  </div>
</template>
<script setup lang="ts">
import { KeyValue } from '@/api/modeling/request'
import { saveJsonData } from '@/api/modeling'
import { Cell, DataUri, Graph } from '@antv/x6'
import { reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { PropsOptions } from '../types'
import message from '@arco-design/web-vue/es/message'
import { checkData, jsonFormat } from '../utils/operateJson'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const route = useRoute()
interface HeaderIData { graph: Graph | null, options: PropsOptions | null }
const emits = defineEmits([`operate`])
const antvLeftShow = ref<boolean>(true)
const iData: HeaderIData = reactive({
  graph: null, options: null
})
const init = (graph: Graph, options: PropsOptions) => {
  iData.graph = graph
  iData.options = options
}
const clickBtn = (type: string, other?: any) => {
  if (type === 'antvLeft') {
    antvLeftShow.value = !antvLeftShow.value
    emits(`operate`, type)
  } else if (type === 'revoke') iData.graph?.undo()
  else if (type === 'recover') iData.graph?.redo()
  else if (type === 'enlarge') iData.graph?.zoomTo(iData.graph?.zoom() + 0.15, { maxScale: 2, minScale: 0.5 })
  else if (type === 'narrow') iData.graph?.zoomTo(iData.graph?.zoom() - 0.15, { maxScale: 2, minScale: 0.5 })
  else if (type === 'center') iData.graph?.center()
  else if (type === 'delete') {
    const cells: Cell[] | undefined = iData.graph?.getSelectedCells()
    if (cells && cells.length) {
      const deleteArr = cells.filter((item: Cell) => (!item.data.operate || item.data.operate.includes('delete')))
      iData.graph?.removeCells(deleteArr)
    }
  } else if (type === 'save') {
    if (iData.graph) {
      let jsonData = jsonFormat(iData.graph) as KeyValue
      saveJsonData(String(window.$wujie?.props.data.id), jsonData)
    }
  } else if (type === 'export') {
    if (other === 'json') {
      let jsonData = iData.graph && jsonFormat(iData.graph)
      if (jsonData) {
        const filename = t('modeling.components.Header.5m78qbcztzk0') + `${(new Date()).toISOString()}.json`
        const data = typeof jsonData === 'object' ? JSON.stringify(jsonData, undefined, 2) : jsonData
        if (data) {
          const blob = new Blob([data], { type: 'text/json' })
          const link = document.createElement('a')
          link.setAttribute('style', 'display: none')
          link.download = filename
          link.href = window.URL.createObjectURL(blob)
          link.dataset.downloadurl = ['text/json', link.download, link.href].join(':')
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
        }
      }
    } else if (other === 'svg') {
      iData.graph?.toSVG((dataUri: string) => {
        DataUri.downloadDataUri(DataUri.svgToDataUrl(dataUri), t('modeling.components.Header.5m78qbczu200') + `{(new Date()).toISOString()}.svg`)
      }, {
        serializeImages: true,
        stylesheet: `
          .base-node-container {
            width: 100%;
            height: 100%;
            background: #ffffff;
            box-shadow: 0px 2px 8px rgb(165 165 165 / 33%);
            border-radius: 4px;
            transition: opacity 0.3s;
            display: flex;
            align-items: center;
            box-sizing: border-box;
            padding: 0 10px;
            position: relative;
          }
          .base-node-icon { display: none !important; }
        `
      })
    } else if (other === 'png' || other === 'jpeg') {
      iData.graph?.toPNG((dataUri: string) => {
        DataUri.downloadDataUri(dataUri, t('modeling.components.Header.5m78qbczu500') + `${(new Date()).toISOString()}.${other}`)
      }, {
        serializeImages: true,
        stylesheet: `
          .base-node-container {
            width: 100%;
            height: 100%;
            background: #ffffff;
            box-shadow: 0px 2px 8px rgb(165 165 165 / 33%);
            border-radius: 4px;
            transition: opacity 0.3s;
            display: flex;
            align-items: center;
            box-sizing: border-box;
            padding: 0 10px;
            position: relative;
          }
          .base-node-icon { display: none !important; }
        `,
        padding: { top: 20, right: 20, bottom: 20, left: 20 }
      })
    }
  } else if (type === 'import') refFile.value.click()
  else if (type === 'code') {
    let jsonData = iData.graph && jsonFormat(iData.graph)
    if (jsonData) {
      emits(`operate`, type, jsonData)
    }
  } else if (type === 'run') {
    let jsonData = iData.graph && jsonFormat(iData.graph)
    let check = checkData(jsonData)
    if (jsonData && check) {
      emits(`operate`, type, jsonData)
    }
  } else if (type === 'visual') {
    emits(`operate`, type)
  } else if (type === 'toggleGridType') {
    emits(`operate`, type)
  }
}
const refFile: any = ref(null)
const fileLoad = () => {
  const selectedFile = refFile.value.files[0]
  const reader = new FileReader()
  reader.onloadend = () => {
    if (!reader.result) return
    try {
      const data = JSON.parse(reader.result as string)
      iData.graph?.fromJSON(data)
    } catch (error) {
      message.error({ content: t('modeling.components.Header.5m78qbczu8c0') })
    } finally {
      refFile.value.value = ''
    }
  }
  reader.readAsText(selectedFile)
}

defineExpose({ init })
</script>
<style scoped lang="less">
  .header-container {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    box-sizing: border-box;
    background-origin: padding-box;
    border-bottom: 1px solid var(--color-border-2);
    .toggle-antv-left {
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--color-neutral-1);
      margin-right: 10px;
      height: 100%;
      padding: 0 16px;
      cursor: pointer;
      transition: background .3s;
      > svg {
        font-size: 18px;
        margin-right: 10px;
      }
      &:hover {
        background: var(--color-neutral-3);
      }
    }
    .d-button {
      width: 32px;
      height: 32px;
      display: flex;
      text-align: center;
      justify-content: center;
      align-items: center;
      border-radius: var(--border-radius-small);
      margin-right: 10px;
      cursor: pointer;
      border: 1px solid rgba(0, 0, 0, 0);
      transition: all .3s;
      > svg {
        font-size: 16px;
      }
      &:hover {
        background: var(--color-neutral-3);
      }
      &:last-child {
        margin-right: 0;
      }
    }
  }
</style>
