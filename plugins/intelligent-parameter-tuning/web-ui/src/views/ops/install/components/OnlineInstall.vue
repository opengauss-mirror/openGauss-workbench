<template>
  <div class="panel-c panel-overflow" style="padding:0 20%">
    <a-form
    ref="formDwgRef"
    :model="formDwg"
    auto-label-width
    :rules="formRules"
  > 
    <p style="font-size: 20px;font-weight: bold;">{{$t('install.Offline.5mpn60ejri11')}}</p>
    <a-form-item
      field="clusterName"
      :label="$t('install.Offline.5mpn60ejri12')"
    >
      <a-select
        v-model="formDwg.clusterName"
        :placeholder="$t('install.Offline.7mpn60ejri12')"
        @change="selectChange"
      >
        <a-option
          v-for="option in selectParams"
          :label="option.label"
          :value="option.value"
          :key="option.value"
        ></a-option>
      </a-select>
    </a-form-item>
    <a-form-item
    field="db"
    :label="$t('install.Offline.5mpn60ejri13')"
  >
    <a-select
      v-model="formDwg.db"
      :placeholder="$t('install.Offline.7mpn60ejri13')"
      @change=""
    >
      <a-option
        v-for="option in dbList"
        :label="option.label"
        :value="option.value"
        :key="option.value"
      ></a-option>
    </a-select>
    </a-form-item>
    <a-form-item
    field="schemaName"
    :label="$t('install.Offline.5mpn60ejri14')"
  >
    <a-input
      v-model="formDwg.schemaName"
      :placeholder="$t('install.Offline.7mpn60ejri14')"
    />
    </a-form-item>
    <a-form-item
      field="opengaussNodePath"
      :label="$t('install.Offline.5mpn60ejri15')"
    >
      <a-input
        v-model="formDwg.opengaussNodePath"
        :placeholder="$t('install.Offline.7mpn60ejri15')"
      />
    </a-form-item>
    <a-form-item
      field="ommPassword"
      :label="$t('install.Offline.5mpn60ejri16')"
      :validate-trigger="blur"
    >
      <a-input-password
        v-model="formDwg.ommPassword"
        :placeholder="$t('install.Offline.7mpn60ejri16')"
      />
    </a-form-item>
    <p style="font-size: 20px;font-weight: bold;">{{$t('install.Offline.5mpn60ejri20')}}</p>
    <a-form-item
    v-if="storeData.installMode === 'OFF_LINE'"
    field="samplingNumber"
    :label="$t('install.Offline.5mpn60ejri22')"
  >
    <a-input
      v-model="formDwg.samplingNumber"
      :placeholder="$t('install.Offline.7mpn60ejri22')"
    />
    </a-form-item>
    <a-form-item
    field="iteration"
    :label="$t('install.Offline.5mpn60ejri23')"
  >
    <a-input
      v-model="formDwg.iteration"
      :placeholder="$t('install.Offline.7mpn60ejri23')"
    />
    </a-form-item>
    <a-form-item
    field="threads"
    :label="$t('install.Offline.5mpn60ejri24')"
  >
    <a-input
      v-model="formDwg.threads"
      :placeholder="$t('install.Offline.7mpn60ejri24')"
    />
    </a-form-item>
    <a-form-item
    field="runningTime"
    :label="$t('install.Offline.5mpn60ejri25')"
  >
    <a-input
      v-model="formDwg.runningTime"
      :placeholder="$t('install.Offline.7mpn60ejri25')"
    />
    </a-form-item>
    <p style="font-size: 20px;font-weight: bold;">{{$t('install.Online.8mpn80ejri01')}}</p>
    <a-form-item field="customLoad" :label="$t('install.Online.8mpn80ejri02')" >
      <template #label>
        <span>{{t('install.Online.8mpn80ejri02')}}</span>
        <a-tooltip
        :content-style="{ color: '#1d212a' }"
        background-color="#fff7e8"
        :content="$t('install.Online.8mpn81ejri02')"
        position="right"
      >
        <icon-question-circle-fill
          :style="{ 'margin-left': '5px', color: '#ff7d00', 'font-size': '16px' }"
        />
      </a-tooltip>
      </template>
      <a-upload
      v-model="formDwg.customLoad"
      :file-list="defaultFiles"
      name="file"
      action="/"
      :limit="5"
      @change="handleUpload"
      @before-upload="beforeUpload"
      :show-retry-button="false"
      :auto-upload="false"
      @before-remove="deletedata"
    />
    </a-form-item>
    <a-form-item field="isCustomPayloads" :label="$t('install.Online.8mpn80ejri03')" >
      <a-radio-group v-model="formDwg.isCustomPayloads">
        <a-radio value="1">{{$t('install.Offline.5mpn60fjra17')}}</a-radio>
        <a-radio value="0">{{$t('install.Offline.5mpn60fjrb17')}}</a-radio>
      </a-radio-group>
    </a-form-item>
    <a-form-item
    v-if="!Number(formDwg.isCustomPayloads)"
    field="sqlNum"
    :label="$t('install.Online.8mpn80ejri04')"
  >
  <template #label>
    <span>{{t('install.Online.8mpn80ejri04')}}</span>
    <a-tooltip
    :content-style="{ color: '#1d212a' }"
    background-color="#fff7e8"
    :content="$t('install.Online.8mpn82ejri23')"
    position="right"
  >
    <icon-question-circle-fill
      :style="{ 'margin-left': '5px', color: '#ff7d00', 'font-size': '16px' }"
    />
  </a-tooltip>
  </template>
    <a-input
      v-model="formDwg.sqlNum"
      :placeholder="$t('install.Online.8mpn81ejri04')"
    />
    </a-form-item>
    <a-form-item
    v-if="!Number(formDwg.isCustomPayloads)"
    field="averageTableNum"
    :label="$t('install.Online.8mpn80ejri06')"
  >
  <template #label>
    <span>{{t('install.Online.8mpn80ejri06')}}</span>
    <a-tooltip
    :content-style="{ color: '#1d212a' }"
    background-color="#fff7e8"
    :content="$t('install.Online.8mpn82ejri23')"
    position="right"
  >
    <icon-question-circle-fill
      :style="{ 'margin-left': '5px', color: '#ff7d00', 'font-size': '16px' }"
    />
  </a-tooltip>
  </template>
    <a-input
      v-model="formDwg.averageTableNum"
      :placeholder="$t('install.Online.8mpn80ejri06')"
    />
    </a-form-item>
    <a-form-item
    v-if="!Number(formDwg.isCustomPayloads)"
    field="readWriteRatio"
    :label="$t('install.Online.8mpn80ejri05')"
  >
  <template #label>
    <span>{{t('install.Online.8mpn80ejri05')}}</span>
    <a-tooltip
    :content-style="{ color: '#1d212a' }"
    background-color="#fff7e8"
    :content="$t('install.Online.8mpn82ejri16')"
    position="right"
  >
    <icon-question-circle-fill
      :style="{ 'margin-left': '5px', color: '#ff7d00', 'font-size': '16px' }"
    />
  </a-tooltip>
  </template>
    <a-input
      v-model="formDwg.readWriteRatio"
      :placeholder="$t('install.Online.8mpn81ejri05')"
    />
    </a-form-item>
    <a-form-item
    v-if="!Number(formDwg.isCustomPayloads)"
    field="queryComparisonOperatorRatio"
    :label="$t('install.Online.8mpn80ejri07')"
  >
  <template #label>
    <span>{{t('install.Online.8mpn80ejri07')}}</span>
    <a-tooltip
    :content-style="{ color: '#1d212a' }"
    background-color="#fff7e8"
    :content="$t('install.Online.8mpn82ejri17')"
    position="right"
  >
    <icon-question-circle-fill
      :style="{ 'margin-left': '5px', color: '#ff7d00', 'font-size': '16px' }"
    />
  </a-tooltip>
  </template>
    <a-input
      v-model="formDwg.queryComparisonOperatorRatio"
      :placeholder="$t('install.Online.8mpn81ejri07')"
    />
    </a-form-item>
    <a-form-item
    v-if="!Number(formDwg.isCustomPayloads)"
    field="queryLogicPredicateNum"
    :label="$t('install.Online.8mpn80ejri08')"
  >
  <template #label>
    <span>{{t('install.Online.8mpn80ejri08')}}</span>
    <a-tooltip
    :content-style="{ color: '#1d212a' }"
    background-color="#fff7e8"
    :content="$t('install.Online.8mpn82ejri18')"
    position="right"
  >
    <icon-question-circle-fill
      :style="{ 'margin-left': '5px', color: '#ff7d00', 'font-size': '16px' }"
    />
  </a-tooltip>
  </template>
    <a-input
      v-model="formDwg.queryLogicPredicateNum"
      :placeholder="$t('install.Online.8mpn81ejri08')"
    />
    </a-form-item>
    <a-form-item
    v-if="!Number(formDwg.isCustomPayloads)"
    field="averageAggregationOperatorNum"
    :label="$t('install.Online.8mpn80ejri09')"
  >
  <template #label>
    <span>{{t('install.Online.8mpn80ejri09')}}</span>
    <a-tooltip
    :content-style="{ color: '#1d212a' }"
    background-color="#fff7e8"
    :content="$t('install.Online.8mpn82ejri19')"
    position="right"
  >
    <icon-question-circle-fill
      :style="{ 'margin-left': '5px', color: '#ff7d00', 'font-size': '16px' }"
    />
  </a-tooltip>
  </template>
    <a-input
      v-model="formDwg.averageAggregationOperatorNum"
      :placeholder="$t('install.Online.8mpn81ejri09')"
    />
    </a-form-item>
    <a-form-item
    v-if="!Number(formDwg.isCustomPayloads)"
    field="averageQueryColomnNum"
    :label="$t('install.Online.8mpn80ejri10')"
  >
  <template #label>
    <span>{{t('install.Online.8mpn80ejri10')}}</span>
    <a-tooltip
    :content-style="{ color: '#1d212a' }"
    background-color="#fff7e8"
    :content="$t('install.Online.8mpn82ejri20')"
    position="right"
  >
    <icon-question-circle-fill
      :style="{ 'margin-left': '5px', color: '#ff7d00', 'font-size': '16px' }"
    />
  </a-tooltip>
  </template>
    <a-input
      v-model="formDwg.averageQueryColomnNum"
      :placeholder="$t('install.Online.8mpn81ejri10')"
    />
    </a-form-item>
    <a-form-item
    v-if="!Number(formDwg.isCustomPayloads)"
    field="groupByRatioIfReadSql"
    :label="$t('install.Online.8mpn80ejri11')"
  >
  <template #label>
    <span>{{t('install.Online.8mpn80ejri11')}}</span>
    <a-tooltip
    :content-style="{ color: '#1d212a' }"
    background-color="#fff7e8"
    :content="$t('install.Online.8mpn82ejri21')"
    position="right"
  >
    <icon-question-circle-fill
      :style="{ 'margin-left': '5px', color: '#ff7d00', 'font-size': '16px' }"
    />
  </a-tooltip>
  </template>
    <a-input
      v-model="formDwg.groupByRatioIfReadSql"
      :placeholder="$t('install.Online.8mpn81ejri11')"
    />
    </a-form-item>
    <a-form-item
    v-if="!Number(formDwg.isCustomPayloads)"
    field="orderByDescOrAscIfGrouped"
    :label="$t('install.Online.8mpn80ejri12')"
  >
  <template #label>
    <span>{{t('install.Online.8mpn80ejri12')}}</span>
    <a-tooltip
    :content-style="{ color: '#1d212a' }"
    background-color="#fff7e8"
    :content="$t('install.Online.8mpn82ejri22')"
    position="right"
  >
    <icon-question-circle-fill
      :style="{ 'margin-left': '5px', color: '#ff7d00', 'font-size': '16px' }"
    />
  </a-tooltip>
  </template>
    <a-input
      v-model="formDwg.orderByDescOrAscIfGrouped"
      :placeholder="$t('install.Online.8mpn81ejri12')"
    />
    </a-form-item>
  </a-form>
  </div>
</template>

<script lang="ts" setup>
import { computed, onBeforeMount, onMounted, reactive, ref, inject } from 'vue'
import {
  listInstallPackage,
  getPackageCpuArch,
  getSysUploadPath
} from '@/api/ops'
import { KeyValue } from '@/types/global'
import { useOpsStore } from '@/store'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { Message } from '@arco-design/web-vue'
import { trainingModel, getFiles } from '@/api/ops' // eslint-disable-line
import { OpenGaussVersionEnum } from '@/types/ops/install'
import TarUploadModal from './TarUploadModal.vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

const { t } = useI18n()
const installStore = useOpsStore()
const router = useRouter()
const selectParams = inject('selectParams', []);
const loadingFunc = inject<any>('loading')
const changeTabsFunc = inject<any>('changeTabs')

const tarUploadModal = ref<null | InstanceType<typeof TarUploadModal>>(null)

const data = reactive<KeyValue>({
  fileName: '',
  files: [],
  openGaussVersionNum: '',
  getArchLoading: false
})

const clusterNameList = ref([]);
const defaultFiles = ref([]);
const formDwgRef = ref();
const formDwg = reactive({
  clusterName: "",
  db: "",
  schemaName: "public",
  opengaussNodePath: "/opt/module/opengauss/data",
  ommPassword: "",
  samplingNumber: "10",
  iteration: "2",
  threads: "10",
  runningTime: "5",
  customLoad: [],
  isCustomPayloads:"0",
  averageTableNum:'[1,0]',
  sqlNum:"5000",
  readWriteRatio:"0.9",
  queryComparisonOperatorRatio:"[0, 0, 1, 0]",
  queryLogicPredicateNum:"[1, 0, 0]",
  averageAggregationOperatorNum:"[1, 0, 0]",
  averageQueryColomnNum:"[0, 1, 0]",
  groupByRatioIfReadSql:"[1, 0]",
  orderByDescOrAscIfGrouped:"[1, 0]"
});
const formRules = computed(() => {
  return {
    clusterName: [
      { required: true, message: t("install.Offline.6mpn60ejri12") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Offline.6mpn60ejri12"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    db: [
      { required: true, message: t("install.Offline.6mpn60ejri13") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Offline.6mpn60ejri13"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    schemaName: [
      { required: true, message: t("install.Offline.6mpn60ejri14") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Offline.6mpn60ejri14"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    opengaussNodePath: [
      { required: true, message: t("install.Offline.6mpn60ejri15") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Offline.6mpn60ejri15"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    ommPassword: [
      { required: true, message: t("install.Offline.6mpn60ejri16") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Offline.6mpn60ejri16"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    samplingNumber: [
      { required: true, message: t("install.Offline.6mpn60ejri22") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Offline.6mpn60ejri22"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    iteration: [
      { required: true, message: t("install.Offline.6mpn60ejri23") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Offline.6mpn60ejri23"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    threads: [
      { required: true, message: t("install.Offline.6mpn60ejri24") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Offline.6mpn60ejri24"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    runningTime: [
      { required: true, message: t("install.Offline.6mpn60ejri25") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Offline.6mpn60ejri25"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    averageTableNum: [
      { required: true, message: t("install.Online.8mpn82ejri06") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Online.8mpn82ejri06"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    sqlNum: [
      { required: true, message: t("install.Online.8mpn82ejri04") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Online.8mpn82ejri04"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    readWriteRatio: [
      { required: true, message: t("install.Online.8mpn82ejri05") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Online.8mpn82ejri05"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    queryComparisonOperatorRatio: [
      { required: true, message: t("install.Online.8mpn82ejri07") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Online.8mpn82ejri07"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    queryLogicPredicateNum: [
      { required: true, message: t("install.Online.8mpn82ejri08") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Online.8mpn82ejri08"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    averageAggregationOperatorNum: [
      { required: true, message: t("install.Online.8mpn82ejri09") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Online.8mpn82ejri09"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    averageQueryColomnNum: [
      { required: true, message: t("install.Online.8mpn82ejri10") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Online.8mpn82ejri10"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    groupByRatioIfReadSql: [
      { required: true, message: t("install.Online.8mpn82ejri11") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Online.8mpn82ejri11"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    orderByDescOrAscIfGrouped: [
      { required: true, message: t("install.Online.8mpn82ejri12") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Online.8mpn82ejri12"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],};
});
const props = defineProps({
    dbList: {
      type: Array as PropType<string[]>,
      default () {
        return []
      }
    }
})
const emits = defineEmits(['selectdbList'])
const selectChange = (e) => {
  emits('selectdbList', e)
}
const handleUpload = (fileList) => {
  formDwg.customLoad = []
  fileList.forEach(item => {
    formDwg.customLoad.push(item.file)
  })
}
const beforeUpload = (file) => {
  debugger
  let name = ["sql"];
  let fileName = file.name.split(".");
  let fileExt = fileName[fileName.length - 1];
  let isTypeOk = name.indexOf(fileExt) >= 0;
  let isSize = 1024 * 1024 * 500;
  if (!isTypeOk) {
    Message.error(t('install.Online.8mpn82ejri13'));
  } else if (file.size > isSize) {
    Message.error(t('install.Online.8mpn82ejri14'));
  } else {
    return new Promise((resolve, reject) => {
    resolve(true);
  })
  }
};

const currentVersion = computed(() => {
  if (storeData.value && storeData.value.installMode) {
    if (
      storeData.value.installMode === InstallModeEnum.OFF_LINE
    ) {
      return t('components.OfflineInstall.5mpn1nwazok0')
    } else if (storeData.value.installMode === InstallModeEnum.ON_LINE) {
      return t('components.OfflineInstall.5mpn1nwazs80')
    } else {
      return t('components.OfflineInstall.5mpn1nwazvg0')
    }
  } else {
    return ''
  }
})
const getFile = (e) => {
  getFiles({trainingId:e}).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      res.obj.forEach(item => {
        const blob = new Blob([''], { type: item.file.type });
        const file = new File([blob], item.name, { type: item.file.type });
        item.file = file;
        defaultFiles.value.push(item)
        formDwg.customLoad.push(file)
      });
    }
  }).finally(() => {
  })
}
onMounted(() => {
  if(storeData.value.isCopy){
    Object.assign(formDwg, storeData.value.copyData)
    emits('selectdbList', formDwg.clusterName)
    getFile(formDwg.trainingId)
  }
})
const getFormData = (object) => {
    const formData = new FormData()
    Object.keys(object).forEach(key => {
        const value = object[key]
        if (Array.isArray(value)) {
            value.forEach((subValue, i) =>{
              if(key === 'customLoad'){
              formData.append('customLoad', subValue)
            } else{
              formData.append(key + `[${i}]`, subValue)
            }
            }
            )
        } else {
            formData.append(key, object[key])
        }
    })
    return formData
}
const submit = () => {
  let param = {
      online: storeData.value.installMode === 'ON_LINE' ? 'True' : 'False',
      benchmark: storeData.value.benchMark === 'DWG' ? 'dwg' : 'sysbench',
      clusterName: formDwg.clusterName,
      db: formDwg.db,
      schemaName: formDwg.schemaName,
      opengaussNodePath: formDwg.opengaussNodePath,
      ommPassword: formDwg.ommPassword,
      samplingNumber: formDwg.samplingNumber,
      iteration: formDwg.iteration,
      threads: formDwg.threads,
      runningTime: formDwg.runningTime,
      isCustomPayloads:formDwg.isCustomPayloads,
      customLoad:formDwg.customLoad,
      ...formDwg.isCustomPayloads === '0' ? {
        averageTableNum:formDwg.averageTableNum,
        sqlNum:formDwg.sqlNum,
        readWriteRatio:formDwg.readWriteRatio,
        queryComparisonOperatorRatio:formDwg.queryComparisonOperatorRatio,
        queryLogicPredicateNum:formDwg.queryLogicPredicateNum,
        averageAggregationOperatorNum:formDwg.averageAggregationOperatorNum,
        averageQueryColomnNum:formDwg.averageQueryColomnNum,
        groupByRatioIfReadSql:formDwg.groupByRatioIfReadSql,
        orderByDescOrAscIfGrouped:formDwg.orderByDescOrAscIfGrouped
      } : ''
    } 
  const data = getFormData(param)
  formDwgRef.value?.validate((valid) => {
        if (!valid) {
          trainingModel(data).then((res: KeyValue) => {
            if (Number(res.code) === 200) {
              Message.success(res.msg);
              changeTabsFunc.handleSwitchTab("2")
            } else {
              Message.error(
                "Sync error, please check whether the configuration is correct"
              );
            }
          }).catch((error) => {
            Message.error(error);
          }).finally(() => {
          })
        } else {}
      });
}
const choosePackge = (row: KeyValue) => {
  data.fileName = row.name
  data.id = row.id
  data.openGaussVersionNum = row.openGaussVersionNum
  setPathToStore()
}

const formRef = ref<null | FormInstance>(null)
const showUploadModal = () => {
  tarUploadModal.value?.open()
}

const dialogSubmit = () => {
  const param = {
    version: storeData.value.installMode
  }
  listInstallPackage(param).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      if (res.data.files.length) {
        data.files = []
        data.files = res.data.files
        data.path = res.data.path
        if (!data.path.endsWith('/')) {
          data.path += '/'
        }
        if (res.data.files.length) {
          data.fileName = res.data.files[0].name
          data.openGaussVersionNum = res.data.files[0].openGaussVersionNum
          setPathToStore()
        }
      } else {
        Message.warning('No files in the directory were detected')
      }
    } else {
      Message.error(
        `Failed to obtain the installation package in the directory: ${res.msg}`
      )
    }
  })
}

const setPathToStore = () => {
  if (!data.fileName || !data.path) {
    return
  }
  const fileInfo = data.files.find((item: KeyValue) => data.id === item.id)
  if (fileInfo.pkgManagedName) {
    let cpuArchStr = fileInfo.cpuArch
    if (cpuArchStr.includes('-')) {
      cpuArchStr = cpuArchStr.replace('-', '_')
    }
    const temp =
      getInstallOs(fileInfo.os) + '_' + cpuArchStr.toLocaleUpperCase()
    installStore.setInstallContext({
      packagePath: data.path,
      packageName: data.fileName,
      installPackagePath: fileInfo.path,
      openGaussVersionNum: fileInfo.openGaussVersionNum,
      installOs: temp
    })
  } else {
    loadingFunc.toLoading()
    data.getArchLoading = true
    const param = {
      installPackagePath: fileInfo.path,
      version: storeData.value.installMode
    }
    getPackageCpuArch(param)
      .then((res: KeyValue) => {
        if (Number(res.code) === 200) {
          let cpuArchStr = res.msg
          if (cpuArchStr.includes('-')) {
            cpuArchStr = cpuArchStr.replace('-', '_')
          }
          const temp =
            getInstallOs(data.fileName) + '_' + cpuArchStr.toLocaleUpperCase()
          installStore.setInstallContext({
            packagePath: data.path,
            packageName: data.fileName,
            installPackagePath: fileInfo.path,
            openGaussVersionNum: data.openGaussVersionNum,
            installOs: temp
          })
          const file = data.files.find((item: KeyValue) => item.id === data.id)
          if (file) {
            file.os = getInstallOs(data.fileName)
            file.cpuArch = cpuArchStr
          }
        }
      })
      .finally(() => {
        loadingFunc.cancelLoading()
        data.getArchLoading = false
      })
  }
}

enum osEnum {
  CENTOS = 'CENTOS',
  OPENEULER = 'OPENEULER'
}

enum cpuArchEnum {
  X86_64 = 'X86_64',
  AARCH64 = 'AARCH64'
}

const getInstallOs = (name: string) => {
  let os = ''
  const fileName = name.toLocaleUpperCase()
  if (fileName.includes(osEnum.CENTOS)) {
    os = osEnum.CENTOS
  } else if (fileName.includes(osEnum.OPENEULER)) {
    os = osEnum.OPENEULER
  }
  return os
}

const handleGoToPkgList = (name: string) => {
  router.push({ name: 'PackageManage', query: { name: name } })
}

defineExpose({
  submit
})
const storeData = computed(() => installStore.getInstallConfig)
</script>

<style lang="less" scoped>
@import url('~@/assets/style/ops/ops.less');

.panel-overflow {
  overflow: auto;
}

.install-package-card {
  height: 150px;
  padding: 24px;
  width: 1000px;
  border-radius: 8px;
  border: 1px solid #c9cdd4;
  display: flex;
  justify-content: flex-start;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease-in-out;

  &:hover {
    box-shadow: 0px 4px 20px 0px rgba(153, 153, 153, 0.6);
    cursor: pointer;
  }
}
 :deep(.arco-upload-progress) {
    display: none ;
  }
</style>