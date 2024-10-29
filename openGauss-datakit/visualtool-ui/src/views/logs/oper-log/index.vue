<template>
  <div class="app-container" id="operateLog">
    <div class="main-bd">
      <div class="search-con">
        <a-form :model="form" layout="inline">
          <a-form-item field="title" :label="$t('oper-log.index.5mjaax4r6ko0')">
            <a-input v-model="form.title" allow-clear :placeholder="$t('oper-log.index.5mjaax4r7fs0')" style="width: 200px;" @change="getList(true)"></a-input>
          </a-form-item>
          <a-form-item field="operName" :label="$t('oper-log.index.5mjaax4r7mc0')">
            <a-input v-model="form.operName" allow-clear :placeholder="$t('oper-log.index.5mjaax4r7qc0')" style="width: 210px;" @change="getList(true)"></a-input>
          </a-form-item>
          <a-form-item field="businessType" >
            <a-select v-model="form.businessType" allow-clear :placeholder="$t('oper-log.index.5mjaax4r7to0')" style="width: 180px;" @change="getList(true)">
              <a-option v-for="(label, key) in BusinessTypeMap" :key="key" :value="key">{{label}}</a-option>
            </a-select>
          </a-form-item>
          <a-form-item field="status" >
            <a-select v-model="form.status" allow-clear :placeholder="$t('oper-log.index.5mjaax4r7x80')" style="width: 180px;" @change="getList(true)">
              <a-option value="0">{{$t('oper-log.index.5mjaax4r80g0')}}</a-option>
              <a-option value="1">{{$t('oper-log.index.5mjaax4r83o0')}}</a-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-button type="outline" @click="getList(true)">
              <template #icon>
                <icon-search />
              </template>
              <template #default>{{$t('oper-log.index.5mjaax4r89o0')}}</template>
            </a-button>
            <a-button style="margin-left: 10px;" @click="resetQuery">
              <template #icon>
                <icon-sync />
              </template>
              <template #default>{{$t('oper-log.index.5mjaax4r8d80')}}</template>
            </a-button>
          </a-form-item>
        </a-form>
      </div>
      <div class="table-con">
        <a-table :data="data" :bordered="false" stripe :pagination="pagination" @page-change="pageChange" @page-size-change="pageSizeChange">
          <template #columns>
            <a-table-column :title="$t('oper-log.index.5mjaax4r6ko0')" data-index="title"></a-table-column>
            <a-table-column :title="$t('oper-log.index.5mjaax4r7to0')" data-index="businessType" align="center">
              <template #cell="{record}">
                <a-tag bordered>{{ BusinessTypeMap[record.businessType] }}</a-tag>
              </template>
            </a-table-column>
            <a-table-column :title="$t('oper-log.index.5mjaax4r8jo0')" data-index="requestMethod" align="center"></a-table-column>
            <a-table-column :title="$t('oper-log.index.5mjaax4r7mc0')" data-index="operName" align="center"></a-table-column>
            <a-table-column :title="$t('oper-log.index.5mjaax4r7x80')" data-index="status" align="center">
              <template #cell="{record}">
                <a-tag bordered :color="record.status === OperStatus.SUCCESS ? 'green': 'red'">{{ OperStatusMap[record.status] }}</a-tag>
              </template>
            </a-table-column>
            <a-table-column :title="$t('oper-log.index.5mjaax4r8mo0')" data-index="operTime" align="center"></a-table-column>

            <a-table-column :title="$t('oper-log.index.5mjaax4r8pw0')" align="center">
              <template  #cell="{record}" >
                <a-button size="mini" type="text" @click="showDetail(record)">
                  <template #icon>
                    <icon-eye />
                  </template>
                  <template #default>{{$t('oper-log.index.5mjaax4r8sw0')}}</template>
              </a-button>
            </template>
          </a-table-column>
          </template>
        </a-table>
      </div>
      <a-modal v-model:visible="detailVisible" :width="860">
        <a-form  auto-label-width :model="detail" label-align="left" size="medium">
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item :label="$t('oper-log.index.5mjaax4r8vw0')">{{ detail.title }} / {{ BusinessTypeMap[detail.businessType]  }}</a-form-item>
            <a-form-item
              :label="$t('oper-log.index.5mjaax4r8z40')"
            >{{ detail.operName }} / {{ detail.operIp }} / {{ detail.operLocation }}</a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="$t('oper-log.index.5mjaax4r92g0')">{{ detail.operUrl }}</a-form-item>
            <a-form-item :label="$t('oper-log.index.5mjaax4r9co0')">{{ detail.requestMethod }}</a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label-col-flex="60px" :label="$t('oper-log.index.5mjaax4r9hk0')">{{ detail.method }}</a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label-col-flex="60px"  :label="$t('oper-log.index.5mjaax4r9m00')">
              <div style="word-break: break-all">{{ detail.operParam }}</div>
            </a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item label-col-flex="60px"  :label="$t('oper-log.index.5mjaax4r9ow0')">
              <div style="word-break: break-all">{{ detail.jsonResult }}</div>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="$t('oper-log.index.5mjaax4r9sw0')">
              <div>
                {{OperStatusMap[detail.status]}}
              </div>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="$t('oper-log.index.5mjaax4r9vo0')">{{ detail.operTime }}</a-form-item>
          </a-col>
          <a-col :span="24">
            <a-form-item :label="$t('oper-log.index.5mjaax4r9y00')" v-if="form.status === 1">{{ detail.errorMsg }}</a-form-item>
          </a-col>
        </a-row>
      </a-form>
      </a-modal>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref, reactive, computed } from '@vue/runtime-core'
import { listOperLog } from '@/api/operLog'
import { OperStatus, BusinessType } from '@/types/operlog'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

 const OperStatusMap = computed(() => {
  return {
    [OperStatus.SUCCESS]: t('oper-log.index.5mjaax4r80g0'),
  [OperStatus.ERROR]: t('oper-log.index.5mjaax4r83o0')
  } as {[key: number]: string}
})

 const BusinessTypeMap = computed(() => {
  return {
  [BusinessType.OTHER]: t('oper-log.index.5mjaax4ra0g0'),
  [BusinessType.INSERT]: t('oper-log.index.5mjaax4ra3w0'),
  [BusinessType.UPDATE]: t('oper-log.index.5mjaax4ra6c0'),
  [BusinessType.DELETE]: t('oper-log.index.5mjaax4ra8w0'),
  [BusinessType.GRANT]: t('oper-log.index.5mjaax4rab80'),
  [BusinessType.START]: t('oper-log.index.5mjaax4rads0'),
  [BusinessType.STOP]: t('oper-log.index.5mjaax4rag80'),
  [BusinessType.INSTALL]: t('oper-log.index.5mjaax4raio0'),
  [BusinessType.UNINSTALL]: t('oper-log.index.5mjaax4ral00')
} as {[key: number]: string}
 })

const data = ref([])
const detailVisible = ref(false)
const detail = ref<any>({})
const pagination = reactive({
  total: 0,
  current: 1,
  'page-size': 10,
  'show-total': true,
  'show-page-size': true
})
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const form = reactive({
  title: '',
  operName: '',
  businessType: undefined,
  status: undefined
})

const resetQuery = () => {
  queryParams.pageNum = 1
  pagination.current = 1
  form.title = ''
  form.operName = ''
  form.businessType = undefined
  form.status = undefined
  getList()
}

const pageChange = (current: number) => {
  queryParams.pageNum = current
  pagination.current = current
  getList()
}

const pageSizeChange = (current: number) => {
  queryParams.pageNum = 1
  queryParams.pageSize = current
  pagination.current = 1
  pagination['page-size'] = current
  getList()
}

const showDetail = (record: any) => {
  detailVisible.value = true
  detail.value = record
}

const getList = (fresh?: any) => {
  if (fresh) {
    queryParams.pageNum = 1
    pagination.current = 1
  }
  listOperLog({
    ...queryParams,
    ...form
  }).then((res: any) => {
    data.value = res.rows
    pagination.total = res.total
  })
}

onMounted(() => {
  getList()
})
</script>

<style lang="less" scoped>
.app-container {
  .main-bd {
    .search-con {
      padding: 16px 0 8px;
      margin: 0 20px;
      display: flex;
      justify-content: space-between;
      border-bottom: 1px solid var(--color-border-2);
    }
    .table-con {
      margin-top: 30px;
      padding: 0 20px 30px;
    }
  }
}
</style>
