<template>
  <div class="panel-c">
    <div class="panel-header">
      <div class="label-color mb ft-xlg">
        {{ $t('components.OfflineInstall.5mpn1nway8c0') }} {{ currentVersion }}
        {{ $t('components.OfflineInstall.5mpn1nwaywo0') }}
      </div>
      <div class="label-color mb">
        {{ $t('components.OfflineInstall.5mpn1nwaz280') }} {{ (data.path && data.fileName) ? data.path + data.fileName :
          'no choose'
        }}
      </div>
      <a-link class="mb-s" @click="showChangePath">{{ $t('components.OfflineInstall.5mpn1nwaz600') }}</a-link>
    </div>
    <a-spin class="flex-row-center" width="50%" :loading="data.getArchLoading"
      :tip="$t('components.OfflineInstall.else3')">
      <div class="panel-body">
        <div class="flex-col">
          <div v-for="(item, index) in data.files" :key="index">
            <div
              :class="'label-color install-package-card mb ' + (data.fileName === item.name ? 'center-item-active' : '')"
              @click="choosePackge(item)">
              <svg-icon icon-class="ops-offline-install" class="icon-size-s mr"></svg-icon>
              <div class="ft-main">{{ item.name }}</div>
            </div>
          </div>
        </div>
      </div>
    </a-spin>
    <a-modal :mask-closable="false" :visible="pathData.show" :title="pathData.title" :modal-style="{ width: '450px' }"
      @ok="dialogSubmit" @cancel="dialogClose">
      <a-form class="mb" :model="pathData.form" ref="formRef" auto-label-width :rules="formRules">
        <a-form-item field="path" :label="$t('components.OfflineInstall.5mpn1nwaz980')" validate-trigger="blur">
          <a-input v-model="pathData.form.path" :placeholder="$t('components.OfflineInstall.5mpn1nwazd40')"></a-input>
        </a-form-item>
      </a-form>
      <div class="label-color">
        {{ $t('components.OfflineInstall.else1') }}
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, onMounted, reactive, ref, inject } from 'vue'
import { listInstallPackage, getPackageCpuArch } from '@/api/ops'
import { KeyValue } from '@/types/global'
import { useOpsStore } from '@/store'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { Message } from '@arco-design/web-vue'
import { OpenGaussVersionEnum } from '@/types/ops/install'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const installStore = useOpsStore()

const loadingFunc = inject<any>('loading')

const data = reactive<KeyValue>({
  path: '',
  fileName: '',
  files: [],
  openGaussVersionNum: '',
  getArchLoading: false
})

const pathData = reactive({
  show: false,
  title: t('components.OfflineInstall.5mpn1nwazgw0'),
  form: {
    path: ''
  }
})

const formRules = computed(() => {
  return {
    path: [
      { required: true, 'validate-trigger': 'blur', message: t('components.OfflineInstall.5mpn1nwazks0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            const reg = /^\/([\u4E00-\u9FA5A-Za-z0-9_]+\/?)+$/
            const re = new RegExp(reg)
            if (re.test(value)) {
              resolve(true)
            } else {
              cb(t('components.OfflineInstall.else2'))
              resolve(false)
            }
          })
        }
      }
    ]
  }
})

const currentVersion = computed(() => {
  if (storeData.value && storeData.value.openGaussVersion) {
    if (storeData.value.openGaussVersion === OpenGaussVersionEnum.MINIMAL_LIST) {
      return t('components.OfflineInstall.5mpn1nwazok0')
    } else if (storeData.value.openGaussVersion === OpenGaussVersionEnum.LITE) {
      return t('components.OfflineInstall.5mpn1nwazs80')
    } else {
      return t('components.OfflineInstall.5mpn1nwazvg0')
    }
  }
})

onMounted(() => {
  if (storeData.value && storeData.value.packagePath) {
    data.path = storeData.value.packagePath
  } else {
    data.path = '/ops/files/'
  }
  getAllPackage()
})

const getAllPackage = () => new Promise(resolve => {
  const param = {
    path: data.path,
    version: storeData.value.openGaussVersion
  }
  listInstallPackage(param).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.files = []
      data.path = res.data.path
      if (!data.path.endsWith('/')) {
        data.path += '/'
      }
      data.files = res.data.files
      if (storeData.value && storeData.value.packageName) {
        data.fileName = storeData.value.packageName
        data.openGaussVersionNum = storeData.value.openGaussVersionNum
      } else {
        if (data.files.length) {
          data.fileName = res.data.files[0].name
          data.openGaussVersionNum = res.data.files[0].openGaussVersionNum
          setPathToStore()
        }
      }
      resolve(true)
    } else resolve(false)
  })

})
const choosePackge = (row: KeyValue) => {
  data.fileName = row.name
  data.openGaussVersionNum = row.openGaussVersionNum
  setPathToStore()
}

const formRef = ref<null | FormInstance>(null)
const showChangePath = () => {
  pathData.form.path = ''
  nextTick(() => {
    formRef.value?.clearValidate()
    formRef.value?.resetFields()
  })
  pathData.show = true
  pathData.title = t('components.OfflineInstall.5mpn1nwazgw0')
}

const dialogSubmit = () => {
  formRef.value?.validate().then(result => {
    if (!result) {
      const param = {
        path: pathData.form.path,
        version: storeData.value.openGaussVersion
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
            pathData.show = false
          } else {
            Message.warning('No files in the directory were detected')
          }
        } else {
          Message.error(`Failed to obtain the installation package in the directory: ${res.msg}`)
        }
      })
    }
  })

}

const setPathToStore = () => {
  if (data.fileName && data.path) {
    loadingFunc.toLoading()
    data.getArchLoading = true
    const param = {
      installPackagePath: data.path + data.fileName,
      version: storeData.value.openGaussVersion
    }
    getPackageCpuArch(param).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        let cpuArchStr = res.msg
        if (cpuArchStr.includes('-')) {
          cpuArchStr = cpuArchStr.replace('-', '_')
        }
        const temp = getInstallOs() + '_' + cpuArchStr.toLocaleUpperCase()
        installStore.setInstallContext({
          packagePath: data.path,
          packageName: data.fileName,
          installPackagePath: data.path + data.fileName,
          openGaussVersionNum: data.openGaussVersionNum,
          installOs: temp
        })
        console.log('show cpuArch', installStore.getInstallConfig)
      }
    }).finally(() => {
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

const getInstallOs = () => {
  let os = ''
  const fileName = data.fileName.toLocaleUpperCase()
  if (fileName.includes(osEnum.CENTOS)) {
    os = osEnum.CENTOS
  } else if (fileName.includes(osEnum.OPENEULER)) {
    os = osEnum.OPENEULER
  }
  return os
}

const dialogClose = () => {
  pathData.show = false
}
const storeData = computed(() => installStore.getInstallConfig)

</script>

<style lang="less" scoped>
@import url('~@/assets/style/ops/ops.less');

.install-package-card {
  width: 250px;
  height: 100px;
  padding: 24px;
  border-radius: 8px;
  border: 1px solid #C9CDD4;
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
