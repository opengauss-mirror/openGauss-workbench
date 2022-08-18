<template>
  <div class="panel-c">
    <div class="panel-header">
      <div class="mb ft-xlg">
        {{ $t('components.OfflineInstall.5mpn1nway8c0') }} {{ data.currentVersion }}
        {{ $t('components.OfflineInstall.5mpn1nwaywo0') }}
      </div>
      <div class="mb">
        {{ $t('components.OfflineInstall.5mpn1nwaz280') }} {{ (data.path && data.fileName) ? data.path + data.fileName :
            'no choose'
        }}
      </div>
      <a-link @click="showChangePath">{{ $t('components.OfflineInstall.5mpn1nwaz600') }}</a-link>
    </div>
    <div class="panel-body">
      <div class="flex-col">
        <div v-for="(item, index) in data.files" :key="index">
          <div :class="'install-package-card mb ' + (data.fileName === item.name ? 'center-item-active' : '')"
            @click="choosePackge(item)">
            <svg-icon icon-class="ops-offline-install" class="icon-size-s mr"></svg-icon>
            <div class="ft-main">{{ item.name }}</div>
          </div>
        </div>
      </div>
    </div>
    <a-modal :mask-closable="false" :visible="pathData.show" :title="pathData.title" :modal-style="{ width: '450px' }"
      @ok="dialogSubmit" @cancel="dialogClose">
      <a-form :model="pathData.form" ref="formRef" auto-label-width :rules="pathData.rules">
        <a-form-item field="path" :label="$t('components.OfflineInstall.5mpn1nwaz980')" validate-trigger="blur"
          :rules="[{ required: true, message: $t('components.OfflineInstall.5mpn1nwazks0') }]">
          <a-input v-model="pathData.form.path" :placeholder="$t('components.OfflineInstall.5mpn1nwazd40')"></a-input>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { listInstallPackage } from '@/api/ops'
import { KeyValue } from '@/types/global'
import { useOpsStore } from '@/store'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { Message } from '@arco-design/web-vue'
import { OpenGaussVersionEnum } from '@/types/ops/install'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
const installStore = useOpsStore()

const data = reactive<KeyValue>({
  path: '',
  currentVersion: '',
  fileName: '',
  files: []
})

const pathData = reactive({
  show: false,
  title: t('components.OfflineInstall.5mpn1nwazgw0'),
  form: {
    path: ''
  },
  rules: {
    path: [{ required: true, 'validate-trigger': 'blur', message: t('components.OfflineInstall.5mpn1nwazks0') }]
  }
})

onMounted(() => {
  if (storeData.value && storeData.value.openGaussVersion) {
    if (storeData.value.openGaussVersion === OpenGaussVersionEnum.MINIMAL_LIST) {
      data.currentVersion = t('components.OfflineInstall.5mpn1nwazok0')
    } else if (storeData.value.openGaussVersion === OpenGaussVersionEnum.LITE) {
      data.currentVersion = t('components.OfflineInstall.5mpn1nwazs80')
    } else {
      data.currentVersion = t('components.OfflineInstall.5mpn1nwazvg0')
    }
  }
  if (storeData.value && storeData.value.packagePath) {
    data.path = storeData.value.packagePath
    getAllPackage(data.path)
  } else {
    getAllPackage()
  }
})

const getAllPackage = (path?: string) => new Promise(resolve => {
  listInstallPackage(path).then((res: KeyValue) => {
    if (Number(res.code) === 200) {
      data.files = []
      data.path = res.data.path
      if (!data.path.endsWith('/')) {
        data.path += '/'
      }
      data.files = res.data.files
      if (storeData.value && storeData.value.packageName) {
        data.fileName = storeData.value.packageName
      } else {
        if (data.files.length) {
          data.fileName = res.data.files[0].name
          setPathToStore()
        }
      }
      resolve(true)
    } else resolve(false)
  })

})
const choosePackge = (row: KeyValue) => {
  data.fileName = row.name
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
      listInstallPackage(pathData.form.path).then((res: KeyValue) => {
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
    installStore.setInstallContext({
      packagePath: data.path,
      packageName: data.fileName,
      installPackagePath: data.path + data.fileName
    })
  }
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
  box-shadow: 0px 4px 20px 0px rgba(255, 255, 255, 0.5);
  transition: all 0.3s ease-in-out;

  &:hover {
    box-shadow: 0px 4px 20px 0px rgba(153, 153, 153, 0.6);
    cursor: pointer;
  }
}
</style>
