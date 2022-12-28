
<template>
  <a-modal
    class="run-container"
    :visible="dData.show"
    :title="dData.title"
    :footer="false"
    @cancel="close"
    :modal-style="{ width: '800px' }"
  >
    <a-spin :loading="dData.loading" :tip="dData.message" style="width: 100%;">
      <a-tabs :default-active-key="0">
        <a-tab-pane :key="sqlKey" :title="item.name" v-for="(item, sqlKey) in dData.sql">
            <div class="run-info modeling-data-flow-detail-run-dialog-info">
              <div class="info-1">{{ $t('modeling.dy_common.sqlyunxing') }}</div>
              <div class="sql" v-show="dData.sql" ref="copyRef">
                <highlightjs
                  autodetect
                  language="pgSQL"
                  :code="item.sql"
                />
              </div>
              <div class="info-1" v-show="dData.isRun">{{$t('modeling.dialogs.Run.5m7iqlci0400')}}</div>
              <div class="content" v-show="dData.isRun">
                <table v-if="dData.data.length > 0 && Array.isArray(dData.data[sqlKey].runData)" border="1" cellspacing="0" cellpadding="0">
                  <thead>
                    <tr class="header">
                      <td v-for="(row, rowKey) in dData.data[sqlKey].runData[0]" :key="`rowKey1thead${rowKey}`">{{ rowKey }}</td>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(row, rowKey) in dData.data[sqlKey].runData" :key="`rowKey1${rowKey}`">
                      <td
                        class="col"
                        v-for="(colValue, colKey, colIndex) in row"
                        :key="`${rowKey}col${colIndex}body`"
                      >
                        {{ colValue }}
                      </td>
                    </tr>
                  </tbody>
                </table>
                <div v-else class="runsql-other"><span>{{ dData.data[sqlKey] ? dData.data[sqlKey].runData : '' }}</span></div>
              </div>
            </div>
        </a-tab-pane>
      </a-tabs>
      <div class="empty" v-if="dData.sql.length === 0">{{ $t('modeling.dy_common.noResult') }}</div>
    </a-spin>
  </a-modal>
</template>
<script setup lang="ts">
import 'highlight.js/styles/atom-one-dark.css'
import { getSQL, runSql } from '@/api/modeling'
import { useDataFlowStore } from '@/store/modules/modeling/data-flow'
import { Graph } from '@antv/x6'
import { KeyValue } from '@antv/x6/lib/types'
import {
  Modal as AModal,
  Spin as ASpin
} from '@arco-design/web-vue'
import { reactive } from 'vue'
import { checkData } from '../../utils/operateJson'
import 'highlight.js/lib/common'
import hljsVuePlugin from '@highlightjs/vue-plugin'
import hljs from 'highlight.js/lib/core'
import pgsql from 'highlight.js/lib/languages/pgsql'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
const route = useRoute()
const { t } = useI18n()
hljs.registerLanguage('pgsql', pgsql)
const highlightjs = hljsVuePlugin.component
const dFStore = useDataFlowStore()
const dData = reactive({
  show: false,
  title: t('modeling.dialogs.Run.5m7iqlci0rk0'),
  message: t('modeling.dialogs.Run.5m7iqlci0vk0'),
  loading: false,
  sql: [] as any,
  isRun: false,
  jsonData: {} as KeyValue,
  data: [] as Array<KeyValue>
})
const open = (graph: Graph, data: any, type: string) => {
  if (dFStore.getFlowDataInfo) {
    dData.show = true
    dData.isRun = false
    dData.jsonData = data
    dData.loading = true
    dData.title = t('modeling.dialogs.Run.5m7iqlci0y80')
    if (type === `run`) {
      dData.title = t('modeling.dialogs.Run.5m7iqlci10s0')
      getSQL({ ...data, dataFlowId: window.$wujie?.props.data.id }).then((res: KeyValue) => {
        let arr: KeyValue[] = []
        for (let i in res.data) arr.push({ name: i, sql: res.data[i] })
        dData.sql = arr
        if (checkData(dData.jsonData) && dFStore.getFlowDataInfo) {
          runSql({ ...dData.jsonData, dataFlowId: window.$wujie?.props.data.id }).then((res: KeyValue) => {
            dData.loading = false
            dData.isRun = true
            if (res && Number(res.code) === 200) {
              let arr: KeyValue[] = []
              for (let i in res.data) {
                arr.push({ name: i, runData: res.data[i] })
              }
              dData.data = arr
            }
          })
        }
      })
    } else {
      getSQL({ ...data, dataFlowId: window.$wujie?.props.data.id }).then((res: KeyValue) => {
        dData.loading = false
        let arr: KeyValue[] = []
        for (let i in res.data) arr.push({ name: i, sql: res.data[i] })
        dData.sql = arr
      })
    }
  }
}
const close = () => {
  dData.show = false
}
defineExpose({ open })
</script>
<style scoped lang="less">
.run-container {
  .run-info {
    .info-1 {
      margin-bottom: 5px;
    }
    .content {
      background-color: #fff;
      width: 100%;
      min-height: 200px;
      max-height: 500px;
      overflow: auto;
      box-sizing: border-box;
      .table-info {
        padding: 2px 5px;
        display: flex;
        border-left: 1px solid #000;
        border-top: 1px solid #000;
        background-color: #00ffff;
        flex-wrap: wrap;
        > div {
          width: 33.3%;
        }
        .full {
          width: 100%;
        }
      }
      .header {
        font-weight: bold;
        background-color: rgba(202, 202, 202, 0.1);
      }
      table {
        color: #000;
        border-collapse: collapse;
        border-color:rgba(0, 0, 0, 0.2);
        td {
          padding: 3px 4px;
        }
        tbody {
          tr {
            td {
              word-wrap: break-word;
              word-break: break-all;
            }
          }
        }
      }
      .runsql-other {
        background-color: #292c34;
        overflow: auto;
        padding: 14px;
        box-sizing: border-box;
        color: #fff;
        word-wrap: break-word !important;
        white-space: pre !important;
        word-break: break-all !important;
      }
    }
  }
}
.empty {
  text-align: center;
  font-weight: bold;
  font-size: 22px;
}
</style>
<style lang="less">
.modeling-data-flow-detail-run-dialog-info {
  * {
    white-space: pre !important;
  }
  code {
    max-height: 200px;
    overflow: auto;
  }
  .hljs-keyword {
    display: block;
  }
  .hljs-title {
    display: block;
  }
}
</style>
