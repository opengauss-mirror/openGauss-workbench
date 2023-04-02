
<template>
  <div class="scatter-config-container">
    <div class="mb-s">
      <div class="ch-title">{{$t('modeling.components.ScatterConfig.5m7iiczn8k80')}}</div>
    </div>
    <div class="mb">
      <a-row align="center" class="mb-s">
        <a-col class="mr-xs" :span="9">
          <div style="display: flex">
            <span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>
            <a-select v-model="config.indicator.field" :placeholder="$t('modeling.components.LineConfig.5mpu292dkhc0')">
              <overflow-tooltip :text="item.label" v-for="(item, key) in numberOption" :key="key" :content="item.label">
                <a-option :value="item.value"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
              </overflow-tooltip>
            </a-select>
          </div>
        </a-col>
        <a-col class="mr-xs" :span="6">
          <div style="display: flex">
            <span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>
            <a-select v-model="config.indicator.type" :placeholder="$t('modeling.components.LineConfig.5mpu292dky40')">
              <a-option v-for="(item, key) in indicatorTypeSum" :key="`a${key}`" :value="item.value">{{ item.label }}</a-option>
            </a-select>
          </div>
        </a-col>
        <a-col class="mr-xs" :span="8">
          <a-input :max-length="140" model="config.indicator.unit">
            <template #prepend>{{$t('modeling.components.ScatterConfig.5m7iiczn92w0')}}</template>
          </a-input>
        </a-col>
      </a-row>
    </div>
    <div class="mb-s">
      <div class="ch-title">{{$t('modeling.components.ScatterConfig.5m7iiczn97w0')}}</div>
    </div>
    <div class="mb">
      <a-row align="center" class="mb-s">
        <a-col :span="5"><span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>{{$t('modeling.components.ScatterConfig.5m7iiczn9b80')}}</a-col>
        <a-col class="mr-xs" :span="18">
          <a-select v-model="config.location.field" :placeholder="$t('modeling.components.ScatterConfig.5m7iiczn9b80')">
            <overflow-tooltip :text="item.label" v-for="(item, key) in stringOption" :key="key" :content="item.label">
              <a-option :value="item.value"><icon-tag class="mr-s" v-if="item.isCustom" />{{ item.label }}</a-option>
            </overflow-tooltip>
          </a-select>
        </a-col>
      </a-row>
    </div>
    <div>
      <div class="mb-s flex-row">
        <div class="ch-title">{{$t('modeling.components.ScatterConfig.5m7iiczn9u40')}}</div>
        <a-button style="margin-left: auto;" type="primary" size="mini" @click="openUpload">{{$t('modeling.components.ScatterConfig.5m7iiczn9wk0')}}</a-button>
      </div>
      <a-row align="center" class="mb-s">
        <a-col :span="5"><span style="color: #f53f3f; font-weight: bold; margin-right: 5px;">*</span>{{$t('modeling.components.ScatterConfig.5m7iiczn9zg0')}}</a-col>
        <a-col :span="17">
          <a-select v-model="config.location.commonValue" :loading="dOptions.mapJson.loading" :placeholder="$t('modeling.components.ScatterConfig.5mjcl4pctfw0')">
            <a-option v-for="(item, key) in dOptions.mapJson.list" :key="`option${key}`" :value="item.id" :label="item.registerName" class="scatter-config-container-a-option-dy">
              <div class="flex-row" style="width: 100%;">
                <div>{{ item.registerName }}</div>
                <a-popconfirm :content="$t('modeling.components.ScatterConfig.5m7iiczna1w0')" type="warning" :ok-text="$t('modeling.components.ScatterConfig.5m7iiczna4o0')" :cancel-text="$t('modeling.components.ScatterConfig.5m7iiczna740')" @ok="deleteMapJson(item)">
                  <a-button type="primary" @click.stop="() => {}" size="mini" style="margin-left: auto;">{{$t('modeling.components.ScatterConfig.5m7iiczna9k0')}}</a-button>
                </a-popconfirm>
              </div>
            </a-option>
          </a-select>
        </a-col>
      </a-row>
      <a-row align="center" class="mb-s">
        <a-col :span="5">{{$t('modeling.components.ScatterConfig.5m7iicznac00')}}</a-col>
        <a-col :span="17">
          <a-select v-model="config.location.diyValue">
            <a-option :value="'LLA'">{{$t('modeling.components.ScatterConfig.5m7iicznaec0')}}</a-option>
          </a-select>
        </a-col>
      </a-row>
      <div class="mb-s flex-row">
        <div class="ch-title">{{$t('modeling.dy_common.geoMapLabelColor0')}}</div>
      </div>
      <a-row align="center" class="mb-s color-config">
        <a-col :span="5">{{$t('modeling.dy_common.geoMapLabelColor1')}}</a-col>
        <a-col :span="17">
          <input v-model="config.colorConfig[0]" type="color" style="width: 100%" />
        </a-col>
      </a-row>
      <a-row align="center" class="mb-s color-config">
        <a-col :span="5">{{$t('modeling.dy_common.geoMapLabelColor2')}}</a-col>
        <a-col :span="17">
          <input v-model="config.colorConfig[1]" type="color" style="width: 100%" />
        </a-col>
      </a-row>
      <a-row align="center" class="mb-s color-config">
        <a-col :span="5">{{$t('modeling.dy_common.geoMapLabelColor3')}}</a-col>
        <a-col :span="17">
          <input v-model="config.colorConfig[2]" type="color" style="width: 100%" />
        </a-col>
      </a-row>
      <div class="mb-s flex-row">
        <div class="ch-title">{{$t('modeling.dy_common.visualEdit.mapTitle1')}}</div>
      </div>
      <a-row align="center" class="mb-s">
        <a-col :span="5">{{$t('modeling.components.ScatterConfig.5m7iiczn9dw0')}}</a-col>
        <a-col :span="8" class="mr-xs">
          <a-input-number read-only :model-value="config.center[0]"></a-input-number>
        </a-col>
        <a-col :span="8">
          <a-input-number read-only :model-value="config.center[1]"></a-input-number>
        </a-col>
      </a-row>
      <a-row align="center" class="mb-s">
        <a-col :span="5">{{$t('modeling.components.ScatterConfig.5m7iiczn9m40')}}</a-col>
        <a-col :span="8" class="mr-xs">
          <a-input-number read-only :model-value="config.zoom" ></a-input-number>
        </a-col>
      </a-row>
    </div>
    <a-modal
      :title="$t('modeling.components.ScatterConfig.5m7iicznags0')"
      :ok-text="$t('modeling.components.ScatterConfig.5m7iicznak00')"
      :ok-loading="upload.loading"
      :visible="upload.visible"
      @cancel="closeUpload"
      @ok="isUpload"
    >
      <a-form :model="upload.data" :rules="upload.rules" ref="formRef" :label-col-props="{ span: 4, offset: 0 }">
        <a-form-item :label="$t('modeling.components.ScatterConfig.5m7iicznam80')" field="name">
          <a-input :max-length="140" show-word-limit  v-model="upload.data.name" :placeholder="$t('modeling.components.ScatterConfig.5m7iicznaok0')"></a-input>
        </a-form-item>
        <a-form-item :label="$t('modeling.components.ScatterConfig.5m7iicznaqs0')" field="file" class="scatter-config-container-d-upload">
          <a-upload
            v-model:file-list="upload.data.file"
            draggable
            action="/"
            :auto-upload="false"
            ref="uploadRef"
            accept=".json"
            :multiple="false"
            :show-retry-button="false"
            :limit="1"
            @change="onFileChange"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script setup lang="ts">
import { reactive, ref } from 'vue'
import { KeyValue } from '@antv/x6/lib/types'
import { indicatorType, indicatorTypeSum } from '../hooks/options'
import OverflowTooltip from '../../../../../../../components/OverflowTooltip.vue'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { useRoute } from 'vue-router'
import { UploadInstance } from '@arco-design/web-vue/es/upload'
import { Message, Notification } from '@arco-design/web-vue'
import { modelingDVdeleteGeo, modelingDVGetGeo, uploadGeo } from '@/api/modeling'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const route = useRoute()
interface TypeIndicator { field: string, type: string, unit: string }
interface TypeLocationConfig { field: string, type: string | number, commonValue: string, diyValue: string | number, area: number }
defineProps<{
  stringOption: KeyValue[],
  numberOption: KeyValue[],
  datetimeOption: KeyValue[]
}>()
const dOptions = reactive({
  mapJson: { list: [] as KeyValue[], loading: false }
})
const config = reactive({
  indicator: { field: '', type: '', unit: '' } as TypeIndicator,
  location: { field: '', type: '', commonValue: '', diyValue: 1, area: 0 } as TypeLocationConfig,
  colorConfig: ['#FF4800', '#FFFE00', '#89CEF5'],
  center: [undefined, undefined] as Array<undefined | number>,
  zoom: 0 as number | undefined
})
const init = (data?: KeyValue) => {
  getMapJsonList()
  if (data) {
    config.center = data.center ? data.center : [undefined, undefined]
    config.zoom = data.zoom ? data.zoom : 0
    config.indicator = data.indicator ? data.indicator : { field: '', type: '', unit: '' }
    config.location = data.location ? data.location : { field: '', type: '', commonValue: '', diyValue: 1, area: 0 }
    config.colorConfig = data.colorConfig ? data.colorConfig : ['#FF4800', '#FFFE00', '#89CEF5']
  } else {
    config.center = [undefined, undefined]
    config.zoom = 0
    config.indicator = { field: '', type: '', unit: '' }
    config.location = { field: '', type: '', commonValue: '', diyValue: 'LLA', area: 0 }
    config.colorConfig = ['#FF4800', '#FFFE00', '#89CEF5']
  }
}
const noticeArr: string[] = []
const validate = () => {
  let flag = true
  let message = ''
  if (!config.indicator || !config.indicator.field) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.components.ScatterConfig.5mjcl4pctqg0')
  }
  if (!config.indicator || !config.indicator.type) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.components.ScatterConfig.5mjcl4pcu280')
  }
  if (!config.location || !config.location.field) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.components.ScatterConfig.5mjcl4pcu7c0')
  }
  if (!config.center || !config.location.commonValue) {
    flag = false
    message += (message ? '；\n ' : '') + t('modeling.components.ScatterConfig.5mjcl4pcuk00')
  }
  if (!flag) {
    if (noticeArr.includes(message)) return flag
    let isDelete = false
    Notification.error({
      title: t('modeling.components.BarConfig.5m7insim3vc0'),
      content: message,
      closable: true,
      duration: 5 * 1000,
      onClose: () => {
        let index = noticeArr.indexOf(message)
        if (index != -1) noticeArr.splice(index, 1)
        isDelete = true
      }
    })
    noticeArr.push(message)
    setTimeout(() => {
      if (!isDelete) {
        let index = noticeArr.indexOf(message)
        if (index != -1) noticeArr.splice(index, 1)
      }
    }, 5 * 1000)
  }
  return flag
}
const getMapJsonList = () => {
  dOptions.mapJson.loading = true
  modelingDVGetGeo(Number(window.$wujie?.props.data.id)).then((res: KeyValue) => {
    dOptions.mapJson.loading = false
    if (Number(res.code) === 200) {
      dOptions.mapJson.list = res.geoFiles
    }
  })
}
const deleteMapJson = (item: KeyValue) => {
  modelingDVdeleteGeo(item.id).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      Message.success({ content: t('modeling.components.ScatterConfig.5m7iicznask0') })
      getMapJsonList()
    }
  })
}
const upload = reactive({
  visible: false, data: { file: undefined, name: '' } as KeyValue,
  loading: false,
  rules: {
    name: [{ required: true, message: t('modeling.components.ScatterConfig.5m7iicznaok0') }],
    file: [{ required: true, message: t('modeling.components.ScatterConfig.5m7iicznaug0') }]
  }
})
const uploadRef = ref<UploadInstance>()
const openUpload = () => {
  upload.data.name = ''
  upload.data.file = undefined
  upload.visible = true
}
const closeUpload = () => {
  upload.visible = false
}
const formRef = ref<FormInstance>()
const onFileChange = (fileList: Array<{ file: File }>) => {
  if (fileList.length > 0) {
    if (fileList[0].file.type.includes('json')) {
      upload.data.file = fileList
    } else {
      Message.error({ content: t('modeling.components.ScatterConfig.5m7iicznawk0') })
      upload.data.file = []
    }
  }
}
const isUpload = () => {
  formRef.value?.validate((err: any) => {
    if (!err) {
      upload.loading = true
      let sendData = new FormData()
      sendData.append('id', String(window.$wujie?.props.data.id))
      sendData.append('name', upload.data.name)
      sendData.append('file', upload.data.file[0].file)
      uploadGeo(sendData).then((res: KeyValue) => {
        upload.loading = false
        if (Number(res.code) === 200) {
          Message.success({ content: t('modeling.components.ScatterConfig.5m7iicznayc0') })
          closeUpload()
          getMapJsonList()
        }
      }).catch(() => { upload.loading = false })
    }
  })
}
const setCenterZoom = (data: { zoom: number, center: number[] }) => {
  if (data.center && data.center[0]) config.center[0] = Number(data.center[0])
  if (data.center && data.center[1]) config.center[1] = Number(data.center[1])
  if (data.zoom) config.zoom = Number(data.zoom)
}
defineExpose({ config, validate, init, setCenterZoom })
</script>
<style scoped lang="less">
  .scatter-config-container {
    .content-header{
      display: flex;
      align-items: center;
      .ch-title {
        margin-right: 8px;
        color: rgb(var(--primary-6));
        font-size: 16px;
        font-weight: bold;
      }
      > * {
        &:last-child {
          margin-left: auto;
        }
      }
    }
    .ch-title {
      margin-right: 8px;
      color: rgb(var(--primary-6));
      font-size: 16px;
      font-weight: bold;
    }
    .d-title-1 {
      color: rgb(var(--primary-6));
      font-size: 16px;
      font-weight: bold;
    }
  }
</style>
<style lang="less">
  .d-data-flow-radio {
    .arco-radio-label {
      width: 100% !important;
    }
  }
  .scatter-config-container-a-option-dy {
    .arco-select-option-content {
      width: 100%;
    }
  }
  .scatter-config-container-d-upload {
    .arco-upload-progress {
      display: none !important;
    }
  }
</style>
