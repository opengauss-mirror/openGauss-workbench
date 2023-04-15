
<template>
  <a-modal
    class="visual-edit-container"
    :visible="visible"
    :closable="false"
    :footer="false"
    body-style="padding: 0; width: 100%; height: 900px;"
    :modal-style="{ minWidth: '1480px', height: '900px' }"
    unmount-on-close
  >
    <a-spin style="width: 100%; height: 100%;" :loading="dData.loading">
      <div class="container modeling-dialogs-visual-edit-index">
        <div class="main">
          <div class="chart">
            <div class="visual-menu" :class="{ 'hide-visual-menu': !menu.visible }">
              <div class="vm-header">{{$t('modeling.visual-edit.index.5m7ihlv0mmo0')}}</div>
              <div class="vm-content">
                <a-dropdown trigger="contextMenu" v-for="(menuItem, menuKey) in menu.list" :key="`menuItem${menuKey}`">
                  <a-button
                    class="mb-s"
                    :type="menuKey === configKey ? 'outline' : 'dashed'"
                    @click="configChange(menuItem, menuKey)"
                  >
                    {{ menuItem.name ? menuItem.name : $t('modeling.visual-edit.index.5m7ihlv0mig0') }}
                  </a-button>
                  <template #content>
                    <a-doption @click="deleteConfig(menuItem)">{{$t('modeling.visual-edit.index.5m7ihlv0mpc0')}}</a-doption>
                  </template>
                </a-dropdown>
                <a-button type="primary" @click="operateMenu('add')"><template #icon><icon-plus /></template>{{$t('modeling.visual-edit.index.5m7ihlv0ms00')}}</a-button>
              </div>
              <div class="vm-footer">
                <a-button type="primary" @click="openSnapList">{{$t('modeling.dy_common.visualEdit.snapshotBtnText')}}</a-button>
              </div>
            </div>
            <div class="chart-container" :style="{ marginLeft: !menu.visible ? '45px' : '0' }">
              <div id="chartDiv" ref="chartRef" />
            </div>
            <div class="vm-toggle" @click="toggleMenu" :class="{ 'hide-vm-toggle': !menu.visible }">
              <icon-menu-fold v-show="menu.visible" />
              <icon-menu-unfold v-show="!menu.visible" />
            </div>
          </div>
        </div>
        <div class="config">
          <div class="config-main">
                <div class="config-content common">
                  <Common ref="commonRef" />
                  <a-form-item required :label="$t('modeling.visual-edit.index.5m7ihlv0mxo0')" :rules="[]">
                    <a-input :max-length="140" show-word-limit  v-model="config.title" :placeholder="$t('modeling.visual-edit.index.5m7ihlv0n0c0')"></a-input>
                  </a-form-item>
                </div>
                <div class="config-content common">
                  <a-form-item required :label="$t('modeling.visual-edit.index.5m7ihlv0n2s0')" :rules="[]">
                    <a-select v-model="config.chartType" :placeholder="$t('modeling.visual-edit.index.5m7ihlv0n5c0')" @change="(e: any) => isSelectChartType(e)">
                      <a-option v-for="(item, key) in chartTypes" :key="`chartType${key}`" :label="item.label" :value="item.value" />
                    </a-select>
                  </a-form-item>
                  <a-form-item required :label="$t('modeling.dy_common.visualEdit.showNum')" :rules="[]">
                    <a-radio-group v-model="config.showNumber">
                      <a-radio :value="1">{{$t('modeling.dy_common.visualEdit.showNumYes')}}</a-radio>
                      <a-radio :value="2">{{$t('modeling.dy_common.visualEdit.showNumNo')}}</a-radio>
                    </a-radio-group>
                  </a-form-item>
                </div>
                <div class="config-content" v-if="config.chartType">
                  <a-spin :loading="config.loading" style="width: 100%; height: 100%">
                    <component
                      :is="isComponent"
                      ref="configRef"
                      :numberOption="dData.number"
                      :stringOption="dData.string"
                      :datetimeOption="dData.datetime"
                      :allOption="dData.all"
                      @openCDD="openCDD"
                    />
                  </a-spin>
                </div>
          </div>
          <div class="config-footer">
            <a-button type="primary" @click="close">{{$t('modeling.visual-edit.index.5m7ihlv0lrk0')}}</a-button>
            <a-button type="primary" @click="previewChart">{{$t('modeling.visual-edit.index.5m7ihlv0n7s0')}}</a-button>
            <a-button type="primary" @click="saveConfig">{{$t('modeling.visual-edit.index.5m7ihlv0na80')}}</a-button>
            <a-button type="primary" :loading="snapshot.loading" v-show="snapshot.option" @click="saveSnapshot">{{$t('modeling.visual-edit.index.5m7ihlv0ndo0')}}</a-button>
          </div>
        </div>
        <a-modal :visible="chartType.visible" :title="$t('modeling.visual-edit.index.5m7ihlv0ng80')" :footer="false" width="690px" @cancel="closeSelectType">
          <div class="visual-edit-select-chart-type-container">
            <div v-for="(item, key) in chartTypes" :key="`chartType${key}`" @click="isSelectChartType(item.value, true)">
              <img v-if="item.value === 'bar'" src="@/assets/images/modeling/chart/bar.png" alt="">
              <img v-else-if="item.value === 'line'" src="@/assets/images/modeling/chart/line.png" alt="">
              <img v-else-if="item.value === 'pie'" src="@/assets/images/modeling/chart/pie.png" alt="">
              <img v-else-if="item.value === 'graph'" src="@/assets/images/modeling/chart/graph.png" alt="">
              <img v-else-if="item.value === 'heatmap'" src="@/assets/images/modeling/chart/heatmap.png" alt="">
              <img v-else-if="item.value === 'mix'" src="@/assets/images/modeling/chart/mix.png" alt="">
              <img v-else-if="item.value === 'scatter'" src="@/assets/images/modeling/chart/scatter.png" alt="">
              <div class="type-name">{{ item.label }}</div>
            </div>
          </div>
        </a-modal>
        <CustomDimensionDialog
          :stringOption="dData.string"
          @setCustomList="(data: any) => (dData.string = setCustomList(dData.string, data))"
          ref="custormDimensionDialogRef"
        />
      </div>
    </a-spin>
  </a-modal>
</template>
<script setup lang="ts">
import { reactive, ref, nextTick, defineAsyncComponent, computed, onMounted, h } from 'vue'
import { IconArrowLeft } from '@arco-design/web-vue/es/icon'
import Common from './components/Common.vue'
import BarConfig from './components/BarConfig.vue'
import { initChart, renderChart, clearChart, showLoading, hideLoading, getBase64 } from './hooks/chart'
import { Message, Notification } from '@arco-design/web-vue'
import { KeyValue } from '@antv/x6/lib/types'
import { Cell, Graph } from '@antv/x6'
import { jsonFormat } from '../../../utils/operateJson'
import { modelingGetResultFieldsByOperator, modelingVPAdd, modelingVPEdit, modelingVPDelete, modelingVPGenerateChart, modelingVPGetListByOperatorId, modelingVSAdd, modelingVCGetListByOperatorId } from '@/api/modeling'
import { chartTypes, setQueryData } from './hooks/options'
import { useRoute } from 'vue-router'
import { EChartsOption } from 'echarts'
import CustomDimensionDialog from './components/CustomDimensionDialog.vue'
import { useI18n } from 'vue-i18n'
const emits = defineEmits(['operate'])
const { t } = useI18n()
let graph: Graph
let cell: Cell
const visible = ref<boolean>(false)
const dData = reactive({
  activeKey: '1', title: t('modeling.visual-edit.index.5m7ihlv0niw0'), loading: false,
  number: [] as KeyValue[], string: [] as KeyValue[], datetime: [] as KeyValue[], all: [] as KeyValue[]
})
const coms: { [key: string]: any } = {
  'bar': defineAsyncComponent(() => import(`./components/BarConfig.vue`)),
  'line': defineAsyncComponent(() => import(`./components/LineConfig.vue`)),
  'mix': defineAsyncComponent(() => import(`./components/BarLineConfig.vue`)),
  'pie': defineAsyncComponent(() => import(`./components/PieConfig.vue`)),
  'graph': defineAsyncComponent(() => import(`./components/GraphConfig.vue`)),
  'heatmap': defineAsyncComponent(() => import(`./components/HeatmapConfig.vue`)),
  'scatter': defineAsyncComponent(() => import(`./components/ScatterConfig.vue`))
}
const isComponent = computed(() => {
  return coms[config.chartType]
})
const chartEvent = (type: string, data: KeyValue) => {
  if (type === 'georoam' && config.chartType === 'scatter') {
    if (configRef.value && configRef.value.setCenterZoom) {
      configRef.value?.setCenterZoom(data)
    }
  }
}
const open = (pGraph: Graph, data?: any) => {
  visible.value = true
  graph = pGraph
  dData.loading = true
  setTimeout(() => {
    if (dData.loading) dData.loading = false
  }, 5 * 1000)
  nextTick(() => {
    let f = () => {
      let dom = document.getElementById(`chartDiv`)
        if (dom && dom.offsetWidth > 0) initChart(dom, chartEvent)
        else setTimeout(f, 100)
      }
    f()
  })
  clearConfig('')
  clearChart()
  cell = data
  getMenuList().then(() => {
    if (menu.list.length > 0) {
      initConfig(menu.list[0], 0)
    }
  })
  getFieldsList()
}
const close = () => {
  visible.value = false
}
const getFieldsList = () => {
  let sendData = { ...jsonFormat(graph), id: cell.id, dataFlowId: window.$wujie?.props.data.id }
  modelingGetResultFieldsByOperator(sendData).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      if (res.query_data.length > 0) {
        let string = [] as KeyValue[]
        let number = [] as KeyValue[]
        let datetime = [] as KeyValue[]
        let all = [] as KeyValue[]
        let data = res.query_data[0]
        for (let i in res.dimension_options) {
          let key = res.dimension_options[i]
          if (typeof data[key] === 'number') number.push({ value: key, label: key })
          else if (
            (isNaN(data[key]) && !isNaN(Date.parse(data[key]))) ||
            key === 'date' || key === 'year' || key === 'datetime'
          ) {
            datetime.push({ value: key, label: key })
          } else string.push({ value: key, label: key })
          all.push({ value: key, label: key })
        }
        modelingVCGetListByOperatorId(cell.id).then((res2: KeyValue) => {
          dData.string = setCustomList(string, (res2 && res2.data && Array.isArray(res2.data)) ? res2.data : [])
          dData.number = number
          dData.datetime = datetime
          dData.all = all
          setQueryData(res.query_data)
        }).catch(() => {
          dData.string = setCustomList(string, [])
          dData.number = number
          dData.all = all
          setQueryData(res.query_data)
        })
      }
    }
  })
}
const setCustomList = (list: KeyValue[], data: KeyValue[]) => {
  let stringList = list.filter((item: KeyValue) => !item.isCustom)
  let dataT = data.map((item: KeyValue) => ({ value: `custom|${item.id}`, label: item.name, isCustom: true }))
  return [...stringList, ...dataT]
}
let configKey = ref<number | string>('')
const config = reactive({
  title: '', chartType: '', showNumber: 2, loading: false
})
let timer: any = null
const useConfigComponentInit = (callback: any) => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
  const max = 20
  let polling = 0
  timer = setInterval(() => {
    if (configRef.value || polling++ > max) {
      clearInterval(timer)
      timer = null
      if (configRef.value) callback()
      config.loading = false
    }
  }, 100)
}
const clearConfig = (chartType: string, isNew?: boolean) => {
  config.chartType = ''
  config.showNumber = 2
  if (chartType) config.chartType = chartType
  if (isNew) config.title = ''
  nextTick(() => {
    useConfigComponentInit(() => {
      configRef.value?.init()
      config.loading = false
    })
  })
  snapshot.option = null
}
const configChange = (data: KeyValue, key: number) => {
  initConfig(data, key)
  clearChart()
}
const initConfig = (data: KeyValue, key: number) => {
  config.loading = true
  config.title = data.config.title ? data.config.title : ''
  config.showNumber = data.config.showNumber ? data.config.showNumber : ''
  config.chartType = data.config.chartType ? data.config.chartType : ''
  configKey.value = key
  nextTick(() => {
    useConfigComponentInit(() => {
      configRef.value?.init(data.config)
      config.loading = false
    })
  })
  snapshot.option = null
}
const route = useRoute()
const saveConfig = () => {
  let configData: KeyValue = {
    ... config,
    dataFlowId: window.$wujie?.props.data.id,
    operatorId: cell.id
  }
  if (configRef.value?.config) configData = { ...configData, ...configRef.value.config }
  let sendData: KeyValue = {
    name: configData.title,
    type: configData.chartType,
    showNumber: configData.showNumber,
    dataFlowId: window.$wujie?.props.data.id,
    operatorId: cell.id,
    paramsJson: JSON.stringify(configData)
  }
  if (!sendData.name) {
    Message.error({ content: t('modeling.visual-edit.index.5mpsromgvig0') })
    return
  } else if (!sendData.type) {
    Message.error({ content: t('modeling.visual-edit.index.5mpsromgvxg0') })
    return
  }
  dData.loading = true
  const resultCallback = (res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({ content: t('modeling.visual-edit.index.5m7ihlv0nl00') })
      getMenuList().then(() => {
        let index: number = ((configKey.value || configKey.value === 0) ? configKey.value : (menu.list.length - 1)) as number
        initConfig(menu.list[index], index)
      })
    } else {
      dData.loading = false
    }
  }
  if (configKey.value || configKey.value === 0) {
    sendData.id = menu.list[configKey.value as number].id
    modelingVPEdit(sendData).then((res: KeyValue) => resultCallback(res))
  } else {
    modelingVPAdd(sendData).then((res: KeyValue) => resultCallback(res))
  }
}
const snapshotLoading = ref<boolean>(false)
const snapshot = reactive({
  loading: false, option: null as EChartsOption | null
})
const saveSnapshot = () => {
  snapshotLoading.value = true
  let sendData = {
    name: config.title ? config.title : t('modeling.visual-edit.index.5m7ihlv0mig0'),
    imgBase64: getBase64(),
    dataFlowId: window.$wujie?.props.data.id,
    chartDataJson: typeof snapshot.option === 'string' ? snapshot.option : JSON.stringify(snapshot.option)
  }
  modelingVSAdd(sendData).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Notification.success({
        title: t('modeling.visual-edit.index.5m7ihlv0nnw0'),
        content: t('modeling.dy_common.visualEdit.snapshotSuccessNotice'),
        duration: 5 * 1000,
        closable: true
      })
    }
  })
}
const menu = reactive({
  visible: true, list: [] as { title: string, [key: string]: any }[]
})
const getMenuList = () => new Promise(resolve => {
  dData.loading = true
  modelingVPGetListByOperatorId(cell.id as string).then((res: KeyValue) => {
    dData.loading = false
    if (Number(res.code) === 200 && res.data) {
      res.data.forEach((item: KeyValue) => item.config = JSON.parse(item.paramsJson))
      menu.list = res.data ? res.data : []
      resolve(menu.list)
    }
  })
})
const toggleMenu = () => {
  menu.visible = !menu.visible
}
let iAddConfig = false
const operateMenu = (type: string) => {
  if (type === 'add') {
    iAddConfig = true
    openSelectType()
  }
}
const deleteConfig = (item: KeyValue) => {
  dData.loading = true
  modelingVPDelete(item.id).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({ content: t('modeling.visual-edit.index.5m7ihlv0nqc0') })
      getMenuList().then(() => {
        if (menu.list.length > 0) {
          initConfig(menu.list[0], 0)
        } else {
          iAddConfig = true
          openSelectType()
        }
      })
    } else dData.loading = false
  }).catch(() => { dData.loading = false })
}
const chartType = reactive({
  visible: false, other: {}
})
const openSelectType = () => chartType.visible = true
const closeSelectType = () => chartType.visible = false
const isSelectChartType = (chartType: string, isNew?: boolean) => {
  nextTick(() => {
    clearConfig(chartType, isNew)
    closeSelectType()
    clearChart()
    if (iAddConfig) {
      iAddConfig = false
      configKey.value = ''
    }
  })
}
const custormDimensionDialogRef = ref<InstanceType<typeof CustomDimensionDialog>>()
const openCDD = () => {
  custormDimensionDialogRef.value?.open(cell.id)
}
const configRef = ref<any>()
const previewChart = () => {
  if (configRef.value && configRef.value.validate()) {
    let configData: KeyValue = {
      paramsData: {
        ... config,
        operatorId: cell.id
      },
      dataFlowId: window.$wujie?.props.data.id,
      operatorsData: { ...jsonFormat(graph) }
    }
    if (configRef.value?.config) configData = {
      paramsData: {
        ... config,
        operatorId: cell.id,
        ...configRef.value.config
      },
      dataFlowId: window.$wujie?.props.data.id,
      operatorsData: { ...jsonFormat(graph) }
    }
    showLoading()
    modelingVPGenerateChart(configData).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        snapshot.option = res.chartData

        if (res.chartData && res.chartData.geo && res.chartData.geo[0] && res.chartData.geo[0].center && res.chartData.geo[0].center[0] && res.chartData.geo[0].center[1] && res.chartData.geo[0].zoom) {
        const center = res.chartData.geo[0].center
        center[0] = center[0].toFixed(5)
        center[1] = center[1].toFixed(5)
        const zoom = res.chartData.geo[0].zoom.toFixed(5)
        configRef.value?.setCenterZoom({ center, zoom })
      }

        console.log(res)
        renderChart(res.chartData, (config.chartType === 'scatter' && res.mapData && res.mapData.name && res.mapData.data) ? res.mapData : null)
      }
      hideLoading()
    }).catch(() => {
      hideLoading()
    })
  }
}

const openSnapList = () => {
  emits('operate', 'visual')
}
defineExpose({ open })
</script>
<style scoped lang="less">
  .visual-edit-container{
    .container {
      width: 100%;
      height: 100%;
      display: flex;
      .main {
        padding: 20px;
        flex: 1;
        display: flex;
        flex-direction: column;
        .header {
          display: flex;
          align-items: center;
          border: 1px solid var(--color-border-3);
          border-radius: 1px;
          margin-bottom: 20px;
          height: 40px;
          line-height: 40px;
          .back {
            display: flex;
            align-items: center;
            padding: 0 15px;
            cursor: pointer;
            transition: all .3s;
            &:hover {
              background-color:rgb(247, 249, 255)f;
            }
            > svg {
              margin-right: 12px;
            }
          }
          .title {
            display: flex;
            align-items: center;
            padding-left: 10px;
          }
        }
        .chart {
          flex: 1;
          display: flex;
          position: relative;
          height: 100%;
          .vm-toggle {
            z-index: 1;
            position: absolute;
            padding: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 20px;
            left: 1px;
            top: 1px;
            cursor: pointer;
            box-shadow: 0 1px 4px var(--color-fill-4);
            transition: all .3s;
          }
          .hide-vm-toggle {
            left: 1px;
          }
          .visual-menu {
            width: 160px;
            height: 100%;
            text-align: center;
            transition: all .3s;
            overflow: hidden;
            border: 1px solid var(--color-border-3);
            display: flex;
            flex-direction: column;
            &:hover {
              .vm-toggle {
                opacity: 1;
              }
            }
            .vm-header {
              font-weight: bold;
              text-align: start;
              padding: 10px;
              padding-left: 50px;
              margin-bottom: 10px;
              border-bottom: 1px solid var(--color-border-3);
            }
            .vm-content {
              width: 100%;
              box-sizing: border-box;
              padding: 10px;
              flex: 1;
              overflow: auto;
              .visual-menu-item {
                font-size: 14px;
              }
              > * {
                width: 100%;
              }
            }
            > * {
              width: 160px;
            }
            .vm-footer {
              border-top: 1px solid var(--color-border-3);
              padding: 10px 0;
              margin-top: auto;
            }
          }
          .hide-visual-menu {
            width: 0;
            border-color: transparent;
            margin-right: 0;
          }
          .chart-container {
            flex: 1;
            height: 100%;
            > div {
              width: 100%;
              height: 100%;
            }
          }
        }
      }
      .config {
        min-width: 420px;
        width: 420px;
        height: 100%;
        display: flex;
        flex-direction: column;
        border-left: 1px solid var(--color-border-3);
        .config-main {
          flex: 1;
          overflow: auto;
        }
        .config-content {
          padding: 20px 12px 0px 12px;
        }
        .common {
          border-bottom: 1px solid var(--color-border-3);
        }
        .config-footer {
          border-top: 1px solid var(--color-border-3);
          margin-top: auto;
          padding: 10px 0;
          display: flex;
          justify-content: space-around;
        }
      }
    }
  }
  .visual-edit-select-chart-type-container {
    display: flex;
    flex-wrap: wrap;
    > div {
      width: 200px;
      margin-bottom: 10px;
      cursor: pointer;
      transition: all .3s;
      box-sizing: border-box;
      padding: 10px;
      margin-right: 20px;
      border: 1px solid transparent;
      background-color: var(--color-fill-1);
      border-radius: 10px;
      &:nth-of-type(3n) {
        margin-right: 0;
      }
      img {
        width: 100%;
        height: 120px;
        margin-bottom: 10px;
      }
      .type-name {
        color: var(--color-neutral-10);
        text-align: center;
        width: 100%;
      }
      &:hover {
        border: 1px solid rgb(var(--primary-6));
        box-shadow: 0px 1px 15px rgba(var(--primary-6), 0.29);
      }
    }
  }
</style>
<style lang="less">
.modeling-dialogs-visual-edit-index {
  .vm-content {
    button {
      padding: 4px 0;
      height: auto !important;
      white-space: normal !important;
      word-wrap: break-word !important;
      width: 100% !important;
    }
  }
}
</style>
