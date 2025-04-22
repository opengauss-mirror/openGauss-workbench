<template>
  <div class="panel-c panel-overflow" id="offlineInstall">
    <div class="panel-header">
      <div class="label-color mb ft-xlg">
        {{ $t('components.OfflineInstall.5mpn1nway8c0') }} {{ currentVersion }}
        {{ $t('components.OfflineInstall.5mpn1nwaywo0') }}
      </div>
      <div class="label-color mb">
        {{ $t('components.OfflineInstall.5mpn1nwaz280') }}
        {{
          data.path && data.fileName ? data.path : 'no choose'
        }}
      </div>
      <a-link class="mb-s" @click="showUploadModal">{{
        $t('components.OfflineInstall.5mpn1nwaz600')
      }}</a-link>
    </div>
    <a-spin
      class="flex-row-center"
      :loading="data.getArchLoading"
      :tip="$t('components.OfflineInstall.else3')"
    >
      <div class="panel-body">
        <div class="flex-col">
          <div v-for="(item, index) in data.files" :key="index">
            <div
              :class="
                'label-color install-package-card mb ' +
                (data.id === item.id ? 'center-item-active' : '')
              "
              @click="choosePackge(item)"
            >
              <svg-icon
                icon-class="ops-offline-install"
                class="icon-size-s mr"
              ></svg-icon>
              <div class="label-color ft-main mr-xlg" style="flex: 1">
                <div
                  v-if="item.pkgManagedName"
                  class="mb-s"
                  style="font-weight: bold"
                >
                  {{
                    `${t('components.OfflineInstall.5mpn1nwazvg3')}${
                      item.pkgManagedName
                    }`
                  }}
                  <icon-launch
                    v-if="item.pkgManagedName"
                    @click="handleGoToPkgList(item.pkgManagedName)"
                  />
                </div>
                <div class="mb-s">
                  {{
                    `${t('components.OfflineInstall.5mpn1nwazvg2')}${item.path}`
                  }}
                </div>
                <div>
                  <a-tag v-if="item.os" class="mr-s">{{ item.os }}</a-tag>
                  <a-tag v-if="item.cpuArch" class="mr-s">{{
                    item.cpuArch
                  }}</a-tag>
                  <a-tag>{{ item.openGaussVersionNum }}</a-tag>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </a-spin>
    <tar-upload-modal
      ref="tarUploadModal"
      @finish="dialogSubmit"
    ></tar-upload-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref, inject } from 'vue'
import {
  listInstallPackage,
  getPackageCpuArch,
  getSysUploadPath
} from '@/api/ops'
import { KeyValue } from '@/types/global'
import { useOpsStore } from '@/store'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { Message } from '@arco-design/web-vue'
import { OpenGaussVersionEnum } from '@/types/ops/install'
import TarUploadModal from './TarUploadModal.vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'

const { t } = useI18n()
const installStore = useOpsStore()
const router = useRouter()

const loadingFunc = inject<any>('loading')

const tarUploadModal = ref<null | InstanceType<typeof TarUploadModal>>(null)

const data = reactive<KeyValue>({
  fileName: '',
  files: [],
  openGaussVersionNum: '',
  getArchLoading: false
})

const currentVersion = computed(() => {
  if (storeData.value && storeData.value.openGaussVersion) {
    if (
      storeData.value.openGaussVersion === OpenGaussVersionEnum.MINIMAL_LIST
    ) {
      return t('components.OfflineInstall.5mpn1nwazok0')
    } else if (storeData.value.openGaussVersion === OpenGaussVersionEnum.LITE) {
      return t('components.OfflineInstall.5mpn1nwazs80')
    } else {
      return t('components.OfflineInstall.5mpn1nwazvg0')
    }
  } else {
    return ''
  }
})

onMounted(() => {
  getAllPackage()
})

const getAllPackage = () => {
  const param = {
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
        data.id = storeData.value.id
        data.openGaussVersionNum = storeData.value.openGaussVersionNum
      } else {
        if (data.files.length) {
          data.fileName = res.data.files[0].name
          data.openGaussVersionNum = res.data.files[0].openGaussVersionNum
          data.id = res.data.files[0].id
          setPathToStore()
        }
      }
    }
  })
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
  data.path = fileInfo.path
  if (fileInfo.pkgManagedName) {
    let cpuArchStr = fileInfo.cpuArch
    if (cpuArchStr.includes('-')) {
      cpuArchStr = cpuArchStr.replace('-', '_')
    }
    const temp =
      fileInfo.os.toLocaleUpperCase() + '_' + cpuArchStr.toLocaleUpperCase()
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
      version: storeData.value.openGaussVersion
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
  router.push({ name: 'PackageManage', query: { name: name }, params: { backUrl: '/ops/install' } })
}

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
