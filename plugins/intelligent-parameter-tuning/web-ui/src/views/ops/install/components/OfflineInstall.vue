<template>
  <div class="panel-c panel-overflow" style="padding:0 20%" id="tuningOffline">
    <a-form
    ref="formSysbenchRef"
    :model="formSysbench"
    auto-label-width
    :rules="formRules"
  > 
    <p style="color: var(--color-text-1);font-size: 20px;font-weight: bold;">{{$t('install.Offline.5mpn60ejri11')}}</p>
    <a-form-item
      field="clusterName"
      :label="$t('install.Offline.5mpn60ejri12')"
    >
      <a-select
        v-model="formSysbench.clusterName"
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
      v-model="formSysbench.db"
      :placeholder="$t('install.Offline.7mpn60ejri13')"
      @change="selectdbChange"
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
    <a-select
      v-model="formSysbench.schemaName"
      :placeholder="$t('install.Offline.7mpn60ejri14')"
      @change=""
    >
      <a-option
        v-for="option in schemaNameList"
        :label="option.label"
        :value="option.value"
        :key="option.value"
      ></a-option>
    </a-select>
    </a-form-item>
    <a-form-item
      field="osUser"
      :label="$t('install.Offline.5mpn60ejri15')"
    >
      <a-input
        v-model="formSysbench.osUser"
        :placeholder="$t('install.Offline.7mpn60ejri15')"
      />
    </a-form-item>
    <a-form-item field="hasPressureTab" :label="$t('install.Offline.5mpn60ejri17')" >
      <a-radio-group v-model="formSysbench.hasPressureTab">
        <a-radio value="1">{{$t('install.Offline.5mpn60fjra17')}}</a-radio>
        <a-radio value="0">{{$t('install.Offline.5mpn60fjrb17')}}</a-radio>
      </a-radio-group>
    </a-form-item>
    <a-form-item
    field="tables"
    :label="$t('install.Offline.5mpn60ejri18')"
  >
    <a-input
      v-model="formSysbench.tables"
      :placeholder="$t('install.Offline.7mpn60ejri18')"
    />
    </a-form-item>
    <a-form-item
    field="tableSize"
    :label="$t('install.Offline.5mpn60ejri19')"
  >
    <a-input
      v-model="formSysbench.tableSize"
      :placeholder="$t('install.Offline.7mpn60ejri19')"
    />
    </a-form-item>
    <p style="color: var(--color-text-1);font-size: 20px;font-weight: bold;">{{$t('install.Offline.5mpn60ejri20')}}</p>
    <a-form-item
    field="mode"
    :label="$t('install.Offline.5mpn60ejri21')"
  >
    <a-select
      v-model="formSysbench.mode"
      :placeholder="$t('install.Offline.7mpn60ejri21')"
      @change=""
    >
      <a-option
        v-for="option in modeList"
        :label="option.label"
        :value="option.value"
        :key="option.value"
      ></a-option>
    </a-select>
    </a-form-item>
    <a-form-item
    v-if="storeData.installMode === 'OFF_LINE'"
    field="samplingNumber"
    :label="$t('install.Offline.5mpn60ejri22')"
  >
    <a-input
      v-model="formSysbench.samplingNumber"
      :placeholder="$t('install.Offline.7mpn60ejri22')"
    />
    </a-form-item>
    <a-form-item
    field="iteration"
    :label="$t('install.Offline.5mpn60ejri23')"
  >
    <a-input
      v-model="formSysbench.iteration"
      :placeholder="$t('install.Offline.7mpn60ejri23')"
    />
    </a-form-item>
    <a-form-item
    field="threads"
    :label="$t('install.Offline.5mpn60ejri24')"
  >
    <a-input
      v-model="formSysbench.threads"
      :placeholder="$t('install.Offline.7mpn60ejri24')"
    />
    </a-form-item>
    <a-form-item
    field="runningTime"
    :label="$t('install.Offline.5mpn60ejri25')"
  >
    <template #label>
      <span>{{t('install.Offline.5mpn60ejri25')}}</span>
      <a-tooltip
      :content-style="{ color: '#1d212a' }"
      background-color="#fff7e8"
      :content="$t('install.Online.8mpn82ejri24')"
      position="right"
    >
      <icon-question-circle-fill
        :style="{ 'margin-left': '5px', color: '#ff7d00', 'font-size': '16px' }"
      />
    </a-tooltip>
    </template>
    <a-input
      v-model="formSysbench.runningTime"
      :placeholder="$t('install.Offline.7mpn60ejri25')"
    />
    </a-form-item>
  </a-form>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref, inject, PropType } from 'vue'
import {
  listInstallPackage,
  getPackageCpuArch,
  getSysUploadPath
} from '@/api/ops'
import { KeyValue } from '@/types/global'
import { useOpsStore } from '@/store'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { Message } from '@arco-design/web-vue'
import { trainingModel } from '@/api/ops' // eslint-disable-line
import { OpenGaussVersionEnum } from '@/types/ops/install'
import TarUploadModal from './TarUploadModal.vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

const { t } = useI18n()
const installStore = useOpsStore()
const router = useRouter()
const selectParams = inject('selectParams');
const loadingFunc = inject<any>('loading')
const changeTabsFunc = inject<any>('changeTabs')

const tarUploadModal = ref<null | InstanceType<typeof TarUploadModal>>(null)

const data = reactive<KeyValue>({
  fileName: '',
  files: [],
  openGaussVersionNum: '',
  getArchLoading: false
})
const formSysbenchRef = ref();
const formSysbench = reactive({
  clusterName: "",
  db: "",
  schemaName: "",
  osUser: "",
  hasPressureTab: "1",
  tables: "20",
  tableSize: "100000",
  mode: "oltp_read_only",
  samplingNumber: "10",
  iteration: "2",
  threads: "10",
  runningTime: "5",
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
    osUser: [
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
    hasPressureTab: [
      { required: true, message: t("install.Offline.6mpn60ejri18") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Offline.6mpn60ejri18"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    tables: [
      { required: true, message: t("install.Offline.6mpn60ejri18") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Offline.6mpn60ejri18"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    tableSize: [
      { required: true, message: t("install.Offline.6mpn60ejri19") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Offline.6mpn60ejri19"));
              resolve(false);
            } else {
              resolve(true);
            }
          });
        },
      },
    ],
    mode: [
      { required: true, message: t("install.Offline.6mpn60ejri21") },
      {
        validator: (value, cb) => {
          return new Promise((resolve) => {
            if (!value.trim()) {
              cb(t("install.Offline.6mpn60ejri21"));
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
    ],};
});
const modeList = computed(() => {
  return [{
    label: 'oltp_read_only',
    value: 'oltp_read_only'
  }, {
    label: 'oltp_read_write',
    value: 'oltp_read_write'
  }]
})
onMounted(() => {
  if(storeData.value.isCopy){
    Object.assign(formSysbench, storeData.value.copyData)
    emits('selectdbList', formSysbench.clusterName)
    emits('selectschemaNameList', formSysbench.db, formSysbench.clusterName)
  }
})
const props = defineProps({
    dbList: {
      type: Array as PropType<string[]>,
      default () {
        return []
      }
    },
    schemaNameList: {
      type: Array as PropType<string[]>,
      default () {
        return []
      }
    }
})
const emits = defineEmits(['selectdbList','selectschemaNameList'])
const selectChange = (e) => {
  emits('selectdbList', e);
}
const selectdbChange = (e) => {
  emits('selectschemaNameList', e, formSysbench.clusterName);
}
const getFormData = (object) => {
    const formData = new FormData()
    Object.keys(object).forEach(key => {
        const value = object[key]
        if (Array.isArray(value)) {
            value.forEach((subValue, i) =>
                formData.append(key + `[${i}]`, subValue)
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
      ...{
        clusterName: formSysbench.clusterName,
        db: formSysbench.db,
        schemaName: formSysbench.schemaName,
        osUser: formSysbench.osUser,
        hasPressureTab: formSysbench.hasPressureTab,
        tables: formSysbench.tables,
        tableSize: formSysbench.tableSize,
        mode: formSysbench.mode,
        samplingNumber: formSysbench.samplingNumber,
        iteration: formSysbench.iteration,
        threads: formSysbench.threads,
        runningTime: formSysbench.runningTime,
        }
    } 
  formSysbenchRef.value?.validate((valid) => {
        if (!valid) { // 表单校验通过
          trainingModel(getFormData(param)).then((res: KeyValue) => {
            if (Number(res.code) === 200) {
              changeTabsFunc.handleSwitchTab("2")
              Message.success(res.msg);
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
</style>
