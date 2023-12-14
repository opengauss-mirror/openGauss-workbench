<template>
  <a-modal
    :mask-closable="false"
    :esc-to-close="false"
    :visible="data.show"
    :title="data.title"
    :ok-loading="data.loading"
    :modal-style="{ width: '650px' }"
    @ok="handleOk"
    @cancel="close"
    :okText="$t('wdr.GenerateWdrDlg.5mpm0eufx3g0')"
  >
    <a-form
      :model="data.formData"
      ref="formRef"
      :label-col="{ style: { width: '100px' } }"
      :rules="data.rules"
      auto-label-width
    >
      <a-form-item
        field="clusterId"
        :label="$t('wdr.GenerateWdrDlg.5mpm0eufxww0')"
        validate-trigger="change"
        :rules="[{ required: true, message: t('wdr.GenerateWdrDlg.5mpm0eufy340') }]"
      >
        <a-select
          v-model="data.formData.clusterId"
          :placeholder="$t('wdr.GenerateWdrDlg.5mpm0eufy340')"
          @change="getHostList"
        >
          <a-option
            v-for="(item, index) of data.clusterList"
            :key="index"
            :value="item.value"
            :label="item.label"
          />
        </a-select>
      </a-form-item>
      <a-form-item
        field="hostId"
        :label="$t('wdr.GenerateWdrDlg.5mpm0eufy7k0')"
        validate-trigger="change"
        :rules="[{ required: true, message: t('wdr.GenerateWdrDlg.5mpm0eufyb40') }]"
      >
        <a-select
          v-model="data.formData.hostId"
          :placeholder="$t('wdr.GenerateWdrDlg.5mpm0eufyb40')"
          @change="getSnapshotList"
        >
          <a-option
            v-for="(item, index) of data.hostList"
            :key="index"
            :value="item.value"
            :label="item.label"
          />
        </a-select>
      </a-form-item>
      <a-form-item
        field="scope"
        :label="$t('wdr.GenerateWdrDlg.5mpm0eufyew0')"
        validate-trigger="change"
      >
        <a-select
          v-model="data.formData.scope"
          :placeholder="$t('wdr.GenerateWdrDlg.5mpm0eufyig0')"
        >
          <a-option
            v-for="(item, index) of data.wdrScopeList"
            :key="index"
            :value="item.value"
            :label="item.label"
          />
        </a-select>
      </a-form-item>
      <a-form-item
        field="type"
        :label="$t('wdr.GenerateWdrDlg.5mpm0eufyo80')"
        validate-trigger="change"
      >
        <a-select
          v-model="data.formData.type"
          :placeholder="$t('wdr.GenerateWdrDlg.5mpm0eufyz00')"
        >
          <a-option
            v-for="(item, index) of data.wdrTypeList"
            :key="index"
            :value="item.value"
            :label="item.label"
          />
        </a-select>
      </a-form-item>
      <a-form-item
        field="startId"
        :label="$t('wdr.GenerateWdrDlg.5mpm0eufzbk0')"
      >
        <a-select
          :loading="data.getSnapshotLoading"
          v-model="data.formData.startId"
          :placeholder="$t('wdr.GenerateWdrDlg.5mpm0eufzg00')"
        >
          <a-option
            v-for="(item, index) of data.snapshotList"
            :key="index"
            :value="item.value"
            :label="item.label"
          />
        </a-select>
      </a-form-item>
      <a-form-item
        field="endId"
        :label="$t('wdr.GenerateWdrDlg.5mpm0eufzk80')"
      >
        <a-select
          :loading="data.getSnapshotLoading"
          v-model="data.formData.endId"
          :placeholder="$t('wdr.GenerateWdrDlg.5mpm0eufzo40')"
        >
          <a-option
            v-for="(item, index) of data.snapshotList"
            :key="index"
            :value="item.value"
            :label="item.label"
          />
        </a-select>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">

import { nextTick, reactive, ref, resolve } from 'vue'
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { clusterList, getHostByClusterId, wdrGenerate, listSnapshot } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

const data: {
  show: boolean,
  title: string,
  loading: boolean,
  formData: KeyValue,
  clusterListLoading: boolean,
  clusterList: KeyValue[],
  hostListLoading: boolean,
  hostList: KeyValue[],
  wdrScopeList: KeyValue[],
  wdrTypeList: KeyValue[],
  getSnapshotLoading: boolean,
  snapshotList: KeyValue[],
  rules: KeyValue
} = reactive({
  show: false,
  title: t('wdr.GenerateWdrDlg.5mpm0eufzrs0'),
  loading: false,
  formData: {
    clusterId: '',
    hostId: '',
    scope: 'CLUSTER',
    type: 'DETAIL',
    startId: '',
    endId: ''
  },
  clusterListLoading: false,
  clusterList: [],
  hostListLoading: false,
  hostList: [],
  wdrScopeList: [],
  getSnapshotLoading: false,
  snapshotList: [],
  wdrTypeList: [],
  rules: {
    clusterId: [{ required: true, 'validate-trigger': 'blur', message: t('wdr.GenerateWdrDlg.5mpm0eufy340') }],
    startId: [
      { required: true, 'validate-trigger': 'change', message: t('wdr.GenerateWdrDlg.5mpm0eufzg00') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (value >= data.formData.endId) {
              cb(t('wdr.GenerateWdrDlg.else1'))
              resolve(false)
            }
            formRef.value?.clearValidate('endId')
            resolve(true)
          })
        }
      }
    ],
    endId: [
      { required: true, 'validate-trigger': 'change', message: t('wdr.GenerateWdrDlg.5mpm0eufzo40') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (value <= data.formData.startId) {
              cb(t('wdr.GenerateWdrDlg.else2'))
              resolve(false)
            }
            formRef.value?.clearValidate('startId')
            resolve(true)
          })
        }
      }
    ]
  }
})

const close = () => {
  data.show = false
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
    data.clusterList = []
  })
}

const emits = defineEmits([`finish`])

const formRef = ref<null | FormInstance>(null)
const handleOk = () => {
  formRef.value?.validate().then(result => {
    if (!result) {
      data.loading = true
      wdrGenerate(data.formData).then((res: KeyValue) => {
        if (Number(res.code) === 200) {
          emits(`finish`)
          close()
        } else {
          Message.error('Error generating WDR report, cause of error: ' + res.msg)
        }
      }).catch(() => {
        Message.error('Error generating WDR report')
      }).finally(() => {
        data.loading = false
      })
    }
  })

}

const getClusterList = () => new Promise(resolve => {
  data.clusterListLoading = true
  clusterList().then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      resolve(true)
      res.data.forEach((item: KeyValue) => {
        data.clusterList.push({
          label: item.clusterId,
          value: item.clusterId
        })
      })
      data.formData.clusterId = data.clusterList[0].value
      getHostList()
    } else resolve(false)
  }).finally(() => {
    data.clusterListLoading = false
  })
})

const getHostList = () => {
  if (data.formData.clusterId) {
    const param = {
      clusterId: data.formData.clusterId
    }
    data.hostListLoading = true
    getHostByClusterId(param).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        data.hostList = []
        res.data.forEach((item: KeyValue) => {
          data.hostList.push({
            label: `${item.privateIp}(${item.hostname})`,
            value: item.hostId
          })
        })
        data.formData.hostId = data.hostList[0].value
        getSnapshotList()
      } else {
        Message.error('Failed to obtain host user information')
      }
    }).finally(() => {
      data.hostListLoading = false
    })
  }
}

const getSnapshotList = () => {
  if (data.formData.clusterId && data.formData.hostId) {
    const param = {
      clusterId: data.formData.clusterId,
      hostId: data.formData.hostId
    }
    data.getSnapshotLoading = true
    listSnapshot(param).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        data.snapshotList = []
        res.data.records.forEach((item: KeyValue) => {
          data.snapshotList.push({
            label: item.snapshot_id,
            value: item.snapshot_id
          })
        })
        if (data.snapshotList.length) {
          data.formData.startId = data.snapshotList[0].value
          data.formData.endId = data.snapshotList[data.snapshotList.length - 1].value
        }
      } else {
        Message.error('Failed to obtain host information')
      }
    }).finally(() => {
      data.getSnapshotLoading = false
    })
  }
}

const open = () => {
  data.show = true
  data.title = t('wdr.GenerateWdrDlg.5mpm0eufzrs0')
  data.wdrScopeList = [
    { label: t('wdr.GenerateWdrDlg.5mpm0eufzv40'), value: 'CLUSTER' },
    { label: t('wdr.GenerateWdrDlg.5mpm0eufzyg0'), value: 'NODE' }
  ]
  data.wdrTypeList = [
    { label: t('wdr.GenerateWdrDlg.5mpm0eug0300'), value: 'DETAIL' },
    { label: t('wdr.GenerateWdrDlg.5mpm0eug0880'), value: 'SUMMARY' },
    { label: t('wdr.GenerateWdrDlg.5mpm0eug0bc0'), value: 'ALL' }
  ]
  getClusterList()
}

defineExpose({
  open
})

</script>

<style lang="less" scoped></style>
