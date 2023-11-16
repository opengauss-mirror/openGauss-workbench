<template>
  <div class="panel-c panel-overflow">
    <a-space direction="vertical" fill>
      <div class="label-color mb ft-xlg">
        {{ $t('components.OnlineInstall.else1') }}:
        {{ data.selectedPackageName ? data.selectedPackageName : '--' }}
      </div>
      <div class="label-color mb">
        <a-spin :loading="data.uploadPathLoading">
          {{ $t('components.OnlineInstall.5mpn3mp10hw0') }}:
          {{ data.targetPath }}
        </a-spin>
      </div>

      <a-spin
        class="full-w"
        :loading="data.loading"
        :tip="$t('components.OnlineInstall.else2')"
      >
        <div class="flex-col panel-body">
          <div v-if="data.packageList.length">
            <div v-for="item in data.packageList" :key="item.packageId">
              <div
                v-if="item.hasDownload"
                :class="
                  'label-color install-package-card mb ' +
                  (data.selectedPackageId === item.packageId
                    ? 'center-item-active'
                    : '')
                "
                @click="setPackageName(item)"
              >
                <div class="center-item flex-row">
                  <svg-icon
                    icon-class="ops-online-install"
                    class="icon-size mr-lg"
                  ></svg-icon>
                  <div class="label-color ft-main mr-xlg" style="flex: 1">
                    <div class="mb-s">
                      {{ item.name }}
                      <icon-launch
                        style="cursor: pointer"
                        v-if="item.name"
                        @click="handleGoToPkgList(item.pkgManagedName)"
                      />
                    </div>
                    <div>
                      <a-tag class="mr-s">{{ item.os }}</a-tag>
                      <a-tag class="mr-s">{{ item.cpuArch }}</a-tag>
                      <a-tag>{{ item.packageVersionNum }}</a-tag>
                    </div>
                  </div>
                  <a-tag color="green">{{
                    $t('components.OnlineInstall.5mpn3mp117k0')
                  }}</a-tag>
                </div>
              </div>
              <div class="mb" v-else>
                <div class="center-item flex-row">
                  <svg-icon
                    icon-class="ops-online-install"
                    class="icon-size mr-lg"
                  ></svg-icon>
                  <div class="label-color ft-main mr-xlg" style="flex: 1">
                    <div class="mb-s" style="font-weight: bold">
                      {{
                        `${t('components.OfflineInstall.5mpn1nwazvg3')}${
                          item.name
                        }`
                      }}
                      <icon-launch
                        style="cursor: pointer"
                        v-if="item.name"
                        @click="handleGoToPkgList(item.name)"
                      />
                    </div>
                    <div>
                      <a-tag class="mr-s">{{ item.os }}</a-tag>
                      <a-tag class="mr-s">{{ item.cpuArch }}</a-tag>
                      <a-tag>{{ item.packageVersionNum }}</a-tag>
                    </div>
                  </div>
                  <a-button @click="downloadPackage(item)">{{
                    $t('components.OnlineInstall.5mpn3mp111s0')
                  }}</a-button>
                </div>
              </div>
            </div>
          </div>
          <div
            class="flex-col"
            v-if="data.packageList.length === 0 && !data.loading"
          >
            <svg-icon icon-class="ops-empty" class="icon-size mb-lg"></svg-icon>
            <div class="empty-content mb">
              {{ $t('components.OnlineInstall.else3') }}
            </div>
            <a-button type="outline" size="large" @click="handleAddPackage">{{
              $t('components.OnlineInstall.else4')
            }}</a-button>
          </div>
        </div>
      </a-spin>
    </a-space>
    <a-modal
      :mask-closable="false"
      :esc-to-close="false"
      v-model:visible="processVisible"
      :ok-text="$t('components.OnlineInstall.5mpn3mp11cg0')"
      @ok="handleOk"
    >
      <template #title>
        {{ $t('components.OnlineInstall.5mpn3mp11g00') }}
      </template>
      <a-progress size="large" :percent="currPercent" />
    </a-modal>
    <add-package-dlg
      ref="addPackageRef"
      @finish="getPackageList"
    ></add-package-dlg>
  </div>
</template>

<script lang="ts" setup>
import AddPackageDlg from '@/views/monitor/packageManage/AddPackageDlg.vue'
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { download } from '@/api/ops'
import Socket from '@/utils/websocket'
import { KeyValue } from '@/types/global'
import { useOpsStore } from '@/store'
import { packageListAll, getSysUploadPath } from '@/api/ops'
import { useI18n } from 'vue-i18n'
import { Message } from '@arco-design/web-vue'
import { useRouter } from 'vue-router'

const installStore = useOpsStore()
const { t } = useI18n()
const router = useRouter()

const data = reactive<KeyValue>({
  loading: false,
  targetPath: '',
  packageList: [],
  selectedPackageId: '',
  selectedPackageName: '',
  uploadPathLoading: false
})

const processVisible = ref<boolean>(false)

const currPercent = ref<number>(0)
const percentLoading = ref(false)

const downloadWs = ref<Socket<any, any> | undefined>()

onMounted(() => {
  getUploadPath()
  getPackageList()
})

const getUploadPath = () => {
  data.uploadPathLoading = true
  getSysUploadPath()
    .then((res: KeyValue) => {
      console.log('show system upload path', res)
      if (Number(res.code) === 200) {
        data.targetPath = res.data
      }
    })
    .finally(() => {
      data.uploadPathLoading = false
    })
}

onBeforeUnmount(() => {
  downloadWs.value?.destroy()
})

const getPackageList = () => {
  if (installStore.getInstallConfig.openGaussVersion) {
    // get package list
    data.loading = true
    const param = {
      packageVersion: installStore.getInstallConfig.openGaussVersion
    }
    packageListAll(param)
      .then((res: KeyValue) => {
        if (Number(res.code) === 200) {
          console.log('get package list', res)
          data.packageList = []
          res.data.forEach((item: KeyValue) => {
            if (item.name) {
              // if has download set true
              if (installStore.getHasDownload.indexOf(item.name) > -1) {
                item.hasDownload = true
              }
              data.packageList.push(item)
            }
          })
          if (installStore.getInstallConfig.packagePath) {
            // find one
            const hasDownloadPackage = res.data.find((item: any) => {
              return item.packageId === installStore.getInstallConfig.packageId
            })
            if (hasDownloadPackage) {
              setPackageName(hasDownloadPackage)
              hasDownloadPackage.hasDownload = true
            }
          }
          console.log('get item name')
        }
      })
      .finally(() => {
        data.loading = false
        console.log('get item name111')
      }).catch(e => {
        console.log('catch error:', e)
      })
      console.log('finish')
  }
}

const setPackageName = (packageData: KeyValue) => {
  data.selectedPackageId = packageData.packageId
  data.selectedPackageName = `${packageData.os}-${packageData.cpuArch}-${packageData.packageVersionNum}-${packageData.packageVersion}`
  const fileName =
    packageData.packageUrl.split('/')[
      packageData.packageUrl.split('/').length - 1
    ]
  installStore.setInstallContext({
    packageId: packageData.packageId,
    packagePath: data.targetPath,
    packageName: fileName,
    installPackagePath: data.targetPath + packageData.name + '/' + fileName,
    openGaussVersionNum: packageData.packageVersionNum,
    installOs: (packageData.os + '_' + packageData.cpuArch).toLocaleUpperCase()
  })
}

const downloadPackage = (packageData: KeyValue) => {
  currPercent.value = 0
  // get url last fileName
  const fileName =
    packageData.packageUrl.split('/')[
      packageData.packageUrl.split('/').length - 1
    ]
  const socketKey = new Date().getTime()
  const websocket = new Socket({ url: `downloadPackage_${socketKey}` })
  downloadWs.value = websocket
  websocket.onopen(() => {
    const param = {
      resourceUrl: packageData.packageUrl,
      targetPath: data.targetPath + packageData.name,
      fileName: fileName,
      connectType: 'DOWNLOAD_INSTALL_PACKAGE',
      businessId: `downloadPackage_${socketKey}`
    }
    download(param).then(() => {
      processVisible.value = true
      percentLoading.value = true
    })
  })
  websocket.onmessage((messageData: any) => {
    if (!isNaN(Number(messageData))) {
      currPercent.value = Number(messageData)
      if (Number(messageData) === 1) {
        percentLoading.value = false
        packageData.hasDownload = true
        installStore.addDownloadInstallPackage(packageData.name)
        setPackageName(packageData)
        websocket.destroy()
      }
    }
  })
}

const addPackageRef = ref<null | InstanceType<typeof AddPackageDlg>>(null)
const handleAddPackage = () => {
  addPackageRef.value?.open(
    'create',
    undefined,
    installStore.getInstallConfig.openGaussVersion
  )
}

const handleOk = () => {
  processVisible.value = false
}

const getVersionName = (version: string) => {
  switch (version) {
    case 'MINIMAL_LIST':
      return t('operation.DailyOps.5mplp1xc46o0')
    case 'LITE':
      return t('operation.DailyOps.5mplp1xc4b40')
    default:
      return t('operation.DailyOps.5mplp1xc4fg0')
  }
}

const handleGoToPkgList = (name: string) => {
  router.push({ name: 'PackageManage', query: { name: name }, params: { backUrl: '/ops/install' } })
}

const storeData = computed(() => installStore.getInstallConfig)
</script>

<style lang="less" scoped>
@import url('~@/assets/style/ops/ops.less');
.center-item {
  width: 1000px;
  &:hover {
    box-shadow: none;
  }
}

.empty-content {
  font-weight: bold;
  color: var(--color-neutral-4);
}

.panel-overflow {
  overflow: auto;
}

.install-package-card {
  height: 100px;
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
