
<template>
  <a-modal
  class="visual-list-container"
    :visible="visible"
    :title="$t('modeling.visual-list.index.5m7iox8oj6k0')"
    :footer="false"
    @cancel="close"
    body-style="padding: 0; width: 100%;"
    :modal-style="{ minWidth: '1480px' }"
  >
    <div style="padding: 20px; box-sizing: border-box;">
      <a-tabs default-active-key="1" v-model="tabActive">
        <a-tab-pane key="1" :title="$t('modeling.visual-list.index.5m7iox8ok400')">
          <div class="visual-display-list">
            <a-dropdown
              trigger="contextMenu" alignPoint :style="{display:'block'}"
              v-for="(item, key) in history.list" :key="`vditem${key}`"
            >
              <div class="vd-item" @click="openHistory(item)">
                <div class="cover">
                  <img :src="item.imgBase64" />
                </div>
                <div class="name">{{ item.name }}</div>
              </div>
              <template #content>
                <a-doption @click="deleteSnapshot(item)">{{$t('modeling.visual-list.index.5m7iox8oka00')}}</a-doption>
              </template>
            </a-dropdown>
          </div>
        </a-tab-pane>
        <a-tab-pane key="2" :title="$t('modeling.visual-list.index.5m7iox8okdw0')">
          <div class="pane-2">
            <div class="flex-row mb">
              <a-input-search :max-length="140"  v-model="reportFilter.name" :style="{ width:'250px' }" :placeholder="$t('modeling.visual-list.index.5m7iox8okgs0')" allow-clear search-button
                @search="getReportList" @clear="getReportList" />
              <div style="margin-left: auto;">
                <a-button type="primary" @click="openReport('create')">
                  <template #icon><icon-plus /></template>
                  {{$t('modeling.visual-list.index.5m7iox8okmg0')}}
                </a-button>
              </div>
            </div>
            <div class="visual-display-list report-list">
              <a-table
                class="d-a-table-row"
                :data="report.list"
                :columns="columns"
                :bordered="false"
                :pagination="report.page"
                :loading="report.loading"
                rowKey="id"
                @page-change="currentPage"
              >
                <template #operation="{ record }">
                  <a-button size="mini" type="text" @click="openReport('update', record)">
                    <template #icon><icon-edit /></template>
                    <template #default>{{$t('modeling.visual-list.index.5m7iox8okpk0')}}</template>
                  </a-button>
                  <a-button size="mini" type="text" @click="openShare(record)">
                    <template #icon><icon-share-alt /></template>
                    <template #default>{{$t('modeling.visual-list.index.5m7iox8oksk0')}}</template>
                  </a-button>
                  <a-popconfirm :content="$t('modeling.dy_common.deleteNotice')" type="warning" :ok-text="$t('modeling.dataflow.index.5m77w0y5g400')" :cancel-text="$t('modeling.dataflow.index.5m77w0y5g9k0')" @ok="deleteReport(record)">
                    <a-button size="mini" type="text" status="danger">
                      <template #icon><icon-delete /></template>
                      <template #default>{{$t('modeling.dy_common.deleteText')}}</template>
                    </a-button>
                  </a-popconfirm>
                </template>
              </a-table>
            </div>
          </div>
        </a-tab-pane>
      </a-tabs>
    </div>
  </a-modal>
  <c-u-report :snapshot-list="history.list" ref="cuReportRef" @success="getReportList" />
  <c-u-snapshot ref="cuSnapshotRef" />
  <Share ref="shareRef" />
</template>
<script setup lang="ts">
import { Cell } from '@antv/x6'
import { KeyValue } from '@antv/x6/lib/types'
import { computed, reactive, ref } from 'vue'
import CUReport from './components/CUReport.vue'
import CUSnapshot from './components/CUSnapshot.vue'
import { modelingVRDelete, modelingVRGetListByDataFlowId, modelingVSDelete, modelingVSGetListByDataFlowId } from '@/api/modeling'
import { useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import Share from './components/ShareDialog.vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const route = useRoute()
const visible = ref<boolean>(false)
const list = ref<KeyValue[]>([])
const tabActive = ref<string>('1')
const open = (cell: Cell) => {
  visible.value = true
  if (cell) {
  } else list.value = []
  getHistoryList()
  getReportList()
}
const close = () => {
  visible.value = false
}
const history = reactive({
  list: [] as KeyValue[], loading: false
})
const getHistoryList = () => {
  history.loading = true
  modelingVSGetListByDataFlowId(Number(window.$wujie?.props.data.id)).then((res: KeyValue) => {
    history.loading = false
    if (Number(res.code) === 200) {
      history.list = res.data
    }
  })
}
const cuSnapshotRef = ref<InstanceType<typeof CUSnapshot>>()
const openHistory = (data: any) => {
  cuSnapshotRef.value?.open(JSON.parse(data.chartDataJson))
}
const deleteSnapshot = (item: KeyValue) => {
  modelingVSDelete(item.id).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({ content: t('modeling.visual-list.index.5m7iox8okyc0') })
      getHistoryList()
    }
  })
}
const columns = computed(() => [
  { title: t('modeling.visual-list.index.5m7iox8ol0w0'), dataIndex: 'name' },
  { title: t('modeling.visual-list.index.5m7iox8ol3k0'), dataIndex: 'typeName' },
  { title: t('modeling.visual-list.index.5m7iox8ol680'), dataIndex: 'createTime' },
  { title: t('modeling.visual-list.index.5m7iox8ol8o0'), dataIndex: 'updateTime' },
  { title: t('modeling.visual-list.index.5m7iox8olb00'), slotName: 'operation', width: 180 }
])
const reportFilter = reactive({ name: '' })
const report = reactive({
  list: [] as KeyValue[], loading: false, selectedRowKeys: [],
  page: { pageNum: 1, current: 1, pageSize: 10, total: 10, size: 10 } as KeyValue
})
const currentPage = (e: any) => {
  report.page.pageNum = e
  report.page.current = e
  getReportList()
}
const getReportList = () => {
  report.loading = true
  let params = { ...report.page } as KeyValue
  if (reportFilter.name) params.name = reportFilter.name
  modelingVRGetListByDataFlowId(Number(window.$wujie?.props.data.id), params).then((res: KeyValue) => {
    report.loading = false
    if (Number(res.code) === 200) {
      res.data.forEach((item: KeyValue) => item.typeName = item.type === 1 ? t('modeling.visual-list.index.5m7iox8oldc0') : '')
      report.list = res.data
      report.page.total = (res.data && res.data.total) ? res.data.total : 10
    }
  })
}
const deleteReport = (item: KeyValue) => {
  modelingVRDelete(item.id).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({ content: t('modeling.visual-list.index.5m7iox8okyc0') })
      getReportList()
    }
  })
}
const cuReportRef = ref<InstanceType<typeof CUReport>>()
const openReport = (type: string, data?: any) => {
  cuReportRef.value?.open(type, data)
}
const shareRef = ref<InstanceType<typeof Share>>()
const openShare = (data: any) => {
  shareRef.value?.open(data)
}
defineExpose({ open })
</script>
<style scoped lang="less">
  .visual-list-container {
    .visual-display-list {
      width: 100%;
      height: 700px;
      display: flex;
      flex-wrap: wrap;
      overflow: auto;
      .vd-item {
        box-sizing: border-box;
        width: 300px;
        height: 275px;
        background: var(--color-fill-1);
        border: 1px solid #dadce7;
        border-color: var(--color-border-4);
        border-radius: 10px;
        box-sizing: border-box;
        padding: 10px;
        margin-right: 30px;
        margin-bottom: 30px;
        cursor: pointer;
        transition: all .3s;
        &:hover {
          border: 1px solid rgb(var(--primary-6));
          box-shadow: 0px 1px 15px rgba(var(--primary-6), 0.29);;
          border-radius: 10px;
        }
        .cover {
          width: 100%;
          height: 210px;
          display: flex;
          align-items: center;
          justify-content: center;
          border-color: var(--color-border-3);
          border-radius: 11px;
          margin-bottom: 15px;
          img {
            width: 100%;
            height: 100%;
            object-fit: contain;
          }
        }
        .name {
          margin-bottom: 8px;
        }
        .attrs {
          color: rgb(134, 144, 156);
          font-size: 12px;
        }
      }
      .vd-add {
        position: relative;
        .card-1 {
          position: absolute;
          width: 117px;
          height: 170px;
          left: 42%;
          top: 40%;
          background: linear-gradient(rgba(var(--primary-6), .4) 0%,rgba(var(--primary-6), 1) 100%);
          border-radius: 6px;
          transform-origin: left bottom;
          transform: rotate(15deg) translate(-50%, -50%);
          opacity: 0.2;
        }
        .card-2 {
          position: absolute;
          width: 117px;
          height: 160px;
          left: 50%;
          top: 45%;
          background: linear-gradient(rgba(var(--primary-6), .3) 0%,rgba(var(--primary-6), 1) 100%);
          border-radius: 6px;
          transform: translate(-50%, -50%);
          display: flex;
          align-items: center;
          justify-content: center;
          > svg {
            font-size: 50px;
            color: #fff;
          }
        }
        .text {
          position: absolute;
          left: 50%;
          bottom: 10%;
          transform: translateX(-50%);
          font-size: 20px;
        }
      }
    }
    .pane-2 {
      height: 700px;
    }
    .report-list {
      display: block;
      overflow: auto;
    }
    .d-links {
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      width: 110px;
      height: 22px;
      .d-link {
        color: #2d69ed;
        font-size: 14px;
        display: flex;
        align-items: center;
        position: relative;
        cursor: pointer;
        margin-right: 11px;
        margin-bottom: 0;
        &::after {
          display: none;
          content: "";
          position: absolute;
          right: -6px;
          top: 50%;
          transform: translateY(-50%);
          width: 1px;
          height: 14px;
          background-color: #d4d4d4;
        }
        &:nth-child(2n) {
          margin-right: 0;
          &::after {
            display: none;
          }
        }
        &::before {
          content: "";
          position: absolute;
          bottom: 0;
          left: 0;
          width: 100%;
          height: 1px;
          background-color: rgb(var(--primary-6));
          opacity: 0;
        }
        &:hover {
          &::before {
            opacity: 1;
          }
        }
        &:last-child {
          &::before {
            background-color: rgb(var(--primary-6));
          }
        }
      }
    }
  }
</style>
