<template>
  <transition mode="out-in" name="d-config-pancel-animation">
    <a-resize-box
      v-show="visible ? visible : false"
      :directions="['left']"
      v-model:width="width"
      class="config-panel-container"
      :style="configPancelStyle"
      @moving-end="movingEnd"
    >
      <div class="config-panel-button">
        <a-button type="primary" @click="saveAndClose">{{ $t('modeling.dy_common.saveAndClose') }}</a-button>
      </div>
      <div class="config-panel-content" :class="rightSidebarClass">
        <div v-if="iData.configComponent && iData.cellData">
          <component ref="comRef" :is="iData.configComponent" />
        </div>
        <div v-else>
          {{$t('modeling.config-panel.index.5m7adga7to40')}}
        </div>
      </div>
    </a-resize-box>
  </transition>
</template>
<script setup lang="ts">
import { computed, defineAsyncComponent, markRaw, nextTick, reactive, ref } from 'vue'
import { useModelCommonStore } from '@/store/modules/modeling/common'
import { KeyValue } from '@/api/modeling/request'
const mCStore = useModelCommonStore()
const visible = ref<boolean>(false)
let comArr: KeyValue = {
  'DataSource': markRaw(defineAsyncComponent(() => import(`./components/special/DataSource.vue`))),
  'InsertOperator': markRaw(defineAsyncComponent(() => import(`./components/base/InsertOperator.vue`))),
  'QueryOperator': markRaw(defineAsyncComponent(() => import(`./components/base/QueryOperator.vue`))),
  'UpdateOperator': markRaw(defineAsyncComponent(() => import(`./components/base/UpdateOperator.vue`))),
  'DeleteOperator': markRaw(defineAsyncComponent(() => import(`./components/base/DeleteOperator.vue`))),
  'BridgingOperator': markRaw(defineAsyncComponent(() => import(`./components/combination/BridgingOperator.vue`))),
  'ConditionOperator': markRaw(defineAsyncComponent(() => import(`./components/combination/ConditionOperator.vue`))),
  'SortOperator': markRaw(defineAsyncComponent(() => import(`./components/combination/SortOperator.vue`))),
  'RestrictionOperator': markRaw(defineAsyncComponent(() => import(`./components/combination/RestrictionOperator.vue`))),
  'DictionaryOperator': markRaw(defineAsyncComponent(() => import(`./components/combination/DictionaryOperator.vue`))),
  'MapOperator': markRaw(defineAsyncComponent(() => import(`./components/combination/MappingOperator.vue`))),
  'GroupOperator': markRaw(defineAsyncComponent(() => import(`./components/combination/GroupOperator.vue`))),
  'PolymerizationOperator': markRaw(defineAsyncComponent(() => import(`./components/combination/PolymerizationOperator.vue`)))
}
interface RSIDataProps { cellData: KeyValue, configComponent: any }
const width = ref<number>(0)
const movingEnd = () => {
  localStorage.setItem('ModelingConfigPancelWidth', String(width.value))
}
const iData: RSIDataProps = reactive({
  cellData: {}, configComponent: null
})
const configPancelStyle = computed(() => {
  return {
    transform: visible.value ? 'translateX(0)' : 'translateX(12px)'
  }
})
const saveAndClose = () => {
  mCStore.setSelectNode(null, false)
}
const comRef = ref<any>(null)
const init = () => {
  mCStore.$onAction(({ name, after }) => {
    after(() => {
      if (name === 'setSelectNode') {
        let cell = mCStore.selectNode
        let showConfig = mCStore.showConfig
        if (showConfig && cell) {
          iData.cellData = cell.data
          if (cell.data && cell.data.configName && Object.keys(comArr).includes(cell.data.configName)) {
            iData.configComponent = comArr[cell.data.configName]
            visible.value = true
            nextTick(() => {
              const w = localStorage.getItem('ModelingConfigPancelWidth')
              width.value = w ? Number(w) : 500
            })
            nextTick(() => {
              const initCom = () => {
                if (comRef.value) {
                  comRef.value.init(cell)
                } else setTimeout(initCom, 100)
              }
              initCom()
            })
          } else {
            visible.value = false
            setTimeout(() => {
              iData.cellData = {}
              iData.configComponent = null
            }, 500)
          }
        } else {
          visible.value = false
          setTimeout(() => {
            iData.cellData = {}
            iData.configComponent = null
          }, 500)
        }
      }
    })
  })
}
const closeConfigPanel = () => {
  visible.value = false
}
const rightSidebarClass = computed(() => {
  return {
    open: iData.cellData && iData.configComponent
  }
})
defineExpose({ init, closeConfigPanel })
</script>
<style scoped lang="less">
  .config-panel-container {
    min-width: 300px;
    width: 0;
    height: 100%;
    position: absolute;
    z-index: 11;
    right: 0;
    top: 0;
    transition: opacity .3s, transform .3s;
    .config-panel-button {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      box-sizing: border-box;
      width: 100%;
      padding: 0 30px;
      background-color: var(--color-bg-2);
      border-bottom: 1px solid var(--color-border-3);
      > * {
        width: 100%;
      }
    }
    .config-panel-content {
      height: calc(100% - 60px);
      width: 100%;
      min-width: 300px;
      background-color: var(--color-bg-2);
      color: #000;
      overflow: auto;
    }
  }
  .d-config-pancel-animation-enter-active {
    transition: opacity .3s, transform .3s;
  }
  .d-config-pancel-animation-leave-active {
    transition: opacity .3s, transform .3s;
  }
  .d-config-pancel-animation-enter-from,
  .d-config-pancel-animation-leave-to {
    transform: translateX(-20px);
    opacity: 0;
  }
</style>
