<template>
  <div class="package-list">
    <div class="flex-between flex-supplement mb">
      <div class="flex-row">
        <div class="flex-row mr">
          <div class="label-color top-label mr-s">
            {{ $t('taskList.TaskList.7mpn70ejri01') }}
          </div>
          <a-select
            v-model="filter.clusterName"
            :placeholder="$t('taskList.TaskList.7mpn80ejri01')"
            style="width: 210px;"
          >
            <a-option
              v-for="(item, index) in selectParams.clusterNameList"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </a-select>
        </div>
        <div class="flex-row mr">
          <div class="label-color top-label mr-s">
            {{ $t('taskList.TaskList.7mpn70ejri02') }}
          </div>
          <a-select
            v-model="filter.db"
            :placeholder="$t('taskList.TaskList.7mpn80ejri02')"
            style="width: 210px;"
          >
            <a-option
              v-for="(item, index) in selectParams.dbList"
              :key="index"
              :label="item.label"
              :value="item.value"
            />
          </a-select>
        </div>
        <div class="flex-row mr">
          <div class="label-color top-label mr-s">
            {{ $t('taskList.TaskList.7mpn70ejri03') }}
          </div>
          <a-range-picker
          show-time
          :allow-clear="false"
          :time-picker-props="{ defaultValue: ['00:00:00', '23:59:59'] }"
          format="YYYY-MM-DD HH:mm:ss"
          @ok="dateOnOk"
        />
        </div>
        <div class="flex-row">
          <a-button type="primary" class="" @click="isFilter">
            <template #icon>
              <icon-search />
            </template>
            {{ $t('taskList.TaskList.7mpn70ejri04') }}
          </a-button>
          <a-button type="primary" style="margin-left: 12px;" @click="deleteParam" >
            <template #icon>
              <icon-delete />
            </template>
            {{ $t('taskList.TaskList.7mpn70ejri20') }}
          </a-button>
        </div>
      </div>
    </div>
    <a-table
      class="d-a-table-row"
      :data="list.data"
      :columns="columns"
      :pagination="list.page"
      @page-change="currentPage"
      :loading="list.loading"
      :row-selection="list.rowSelection"
      v-model:selectedKeys="list.selectedRowKeys"
      row-key="parameterId"
    >
      <template #operation="{ record }">
        <div class="flex-row-start">
          <a-link class="" @click="handleParameters(record)">{{
            $t('paramsRecommendation.paramsDetails.9mpn60ejri05')
          }}</a-link>
        </div>
      </template>
    </a-table>
    <view-parameters
      ref="viewParametersRef"
    ></view-parameters>
  </div>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { Message, Modal } from '@arco-design/web-vue'
import { useOpsStore } from '@/store'
import { computed, onMounted, reactive, ref, inject } from 'vue'
import { getParamList, getParamClusterName, getParamDatabase, deleteParamList } from '@/api/ops' // eslint-disable-line
import ViewParameters from './ViewParametersDlg.vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const filter = reactive({
  clusterName: '',
  db: '',
  timeInterval: [],
  pageNum: 1,
  pageSize: 10
})
const opsStore = useOpsStore()
const selectParams = reactive({
  clusterNameList: [],
  dbList: []
});
const columns = computed(() => [
  {
    title: t('taskList.TaskList.7mpn70ejri01'),
    dataIndex: 'clusterName',
    width: 180,
    ellipsis: true,
    tooltip: true
  },
  { title: t('taskList.TaskList.7mpn70ejri02'), dataIndex: 'db' },
  { title: t('taskList.TaskList.7mpn70ejri07'), dataIndex: 'startTime',width: 200 },
  {
    title: t('paramsRecommendation.paramsDetails.9mpn60ejri01'),
    dataIndex: 'initialTps',
  },
  {
    title: t('paramsRecommendation.paramsDetails.9mpn60ejri02'),
    dataIndex: 'optimalTps',
  },
  {
    title: t('paramsRecommendation.paramsDetails.9mpn60ejri03'),
    dataIndex: 'performanceImprovement',
  },
  {
    title: t('paramsRecommendation.paramsDetails.9mpn60ejri04'),
    slotName: 'operation',
  }
])

const list = reactive<KeyValue>({
  data: [],
  page: {
    total: 0,
    pageSize: 10,
    'show-total': true
  },
  selectedRowKeys:[],
  rowSelection: {
    type: 'checkbox',
    showCheckedAll: true,
    onlyCurrent: false
  },
  loading: false
})

onMounted(async () => {
  await getAllClusterName()
  await getAlldbName()
  getListData()
})
const getAllClusterName = () => {
  getParamClusterName().then((res: KeyValue) => {
    let clusterNameList = []
    if (Number(res.code) === 200) {
      res.obj.forEach((item: KeyValue) => {
        const temp = {
          label: item,
          value: item
        }
        clusterNameList.push(temp)
      })
      selectParams.clusterNameList = clusterNameList
    }
  }).finally(() => {
  })
}
const deleteParam = () => {
  if(list.selectedRowKeys.length === 0){
    return 
  } else {
    Modal.confirm({
        content: t("taskList.TaskList.7mpn80ejri03"),
        bodyStyle:"text-align:center",
        width:"260px",
        onOk: () => {
          deleteParamList(list.selectedRowKeys).then((res: KeyValue) => {
            let dbList = []
            if (Number(res.code) === 200) {
              Message.success(res.msg);
              getListData()
            }
          }).finally(() => {
          })
        },
        onCancel() {},
      });
  }
}
const getAlldbName = () => {
  getParamDatabase().then((res: KeyValue) => {
    let dbList = []
    if (Number(res.code) === 200) {
      res.obj.forEach((item: KeyValue) => {
        const temp = {
          label: item,
          value: item
        }
        dbList.push(temp)
      })
      selectParams.dbList = dbList
    }
  }).finally(() => {
  })
}
const dateOnOk = (date: any) => {
  filter.timeInterval = date
};
const getListData = () => {
  list.loading = true
  getParamList(filter)
    .then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        list.data = res.obj
        list.page.total = res.total
      }
    })
    .finally(() => {
      list.loading = false
    })
}

const currentPage = (e: number) => {
  filter.pageNum = e
  getListData()
}

const isFilter = () => {
  filter.pageNum = 1
  getListData()
}

const viewParametersRef = ref<null | InstanceType<typeof ViewParameters>>(null)
const handleParameters = (data?: KeyValue) => {
  viewParametersRef.value?.open(data)
}
const installStore = computed(() => opsStore.getInstallConfig)
</script>
<style lang="less" scoped>
.package-list {
  padding: 4px 20px 20px 20px;
  border-radius: 8px;
  .flex-supplement {
    flex-direction: row-reverse;
  }
  .top-label {
    white-space: nowrap;
  }

  .select-w {
    width: 200px;
  }
}
</style>
