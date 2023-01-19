<template>
  <div class="panel-c panel-overflow">
    <a-space direction="vertical" fill>
      <div class="mb ft-xlg">
        {{ $t('components.OnlineInstall.else1') }}: {{ currInstallPackage? currInstallPackage: '--' }}
      </div>
      <div class="mb">
        {{ $t('components.OnlineInstall.5mpn3mp10hw0') }}: {{ data.targetPath }}
      </div>

      <a-spin class="full-w" :loading="data.loading" tip="正在获取安装包">
        <div class="flex-col panel-body">
          <div v-if="data.packageList.length">
            <div v-for="item in data.packageList" :key="item.packageId">
              <div class="center-item flex-between flex-row mb">
                <svg-icon icon-class="ops-online-install" class="icon-size mr-lg"></svg-icon>
                <div class="ft-main mr-xlg">{{ item.os }}-{{ item.cpuArch }}-{{ item.packageVersion }}-{{
                  item.packageVersionNum
                }}
                </div>
                <a-button @click="downloadPackage(item)" v-if="!item.hasDownload">{{
                  $t('components.OnlineInstall.5mpn3mp111s0')
                }}</a-button>
                <a-tag color="green" v-else>{{ $t('components.OnlineInstall.5mpn3mp117k0') }}</a-tag>
              </div>
            </div>
          </div>
          <div class="flex-col" v-if="data.packageList.length === 0 && !data.loading">
            <svg-icon icon-class="ops-empty" class="icon-size mb-lg"></svg-icon>
            <div class="empty-content mb">暂无安装包信息</div>
            <a-button type="outline" size="large" @click="handleAddPackage">添加安装包</a-button>
          </div>
        </div>
      </a-spin>
    </a-space>
    <a-modal :mask-closable="false" :esc-to-close="false" v-model:visible="processVisible"
      :ok-text="$t('components.OnlineInstall.5mpn3mp11cg0')" @ok="handleOk">
      <template #title>
        {{ $t('components.OnlineInstall.5mpn3mp11g00') }}
      </template>
      <a-progress size="large" :percent="currPercent" />
    </a-modal>
    <add-package-dlg ref="addPackageRef" @finish="getPackageList"></add-package-dlg>
  </div>
</template>

<script lang="ts" setup>
import AddPackageDlg from '@/views/monitor/packageManage/AddPackageDlg.vue'
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { download } from '@/api/ops'
import Socket from '@/utils/websocket'
import { KeyValue } from '@/types/global'
import { useOpsStore } from '@/store'
import { packageListAll } from '@/api/ops'

const installStore = useOpsStore()

const data = reactive<KeyValue>({
  loading: false,
  targetPath: '/ops/files/',
  packageList: []
})

const currInstallPackage = ref('')

const processVisible = ref<boolean>(false)

const currPercent = ref<number>(0)
const percentLoading = ref(false)

const downloadWs = ref<Socket<any, any> | undefined>()

onMounted(() => {
  getPackageList()
})

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
    packageListAll(param).then((res: KeyValue) => {
      if (Number(res.code) === 200) {
        console.log('get package list', res)
        data.packageList = res.data
        if (installStore.getInstallConfig.packagePath) {
          // find one
          const hasDownloadPackage = res.data.find((item: any) => {
            return item.packageId === installStore.getInstallConfig.packageId
          })
          if (hasDownloadPackage) {
            hasDownloadPackage.hasDownload = true
          }
        }
      }
    }).finally(() => {
      data.loading = false
    })
  }
}

const downloadPackage = (packageData: KeyValue) => {
  currPercent.value = 0
  currInstallPackage.value = `${packageData.os}-${packageData.cpuArch}-${packageData.packageVersionNum}-${packageData.packageVersion}`
  // get url last fileName
  const fileName = packageData.packageUrl.split('/')[packageData.packageUrl.split('/').length - 1]
  const socketKey = new Date().getTime()
  const websocket = new Socket({ url: `downloadPackage_${socketKey}` })
  downloadWs.value = websocket
  websocket.onopen(() => {
    const param = {
      resourceUrl: packageData.packageUrl,
      targetPath: data.targetPath,
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
        installStore.setInstallContext({
          packageId: packageData.packageId,
          packagePath: data.targetPath,
          packageName: fileName,
          installPackagePath: data.targetPath + fileName
        })
        websocket.destroy()
      }
    }
  })
}

const addPackageRef = ref<null | InstanceType<typeof AddPackageDlg>>(null)
const handleAddPackage = () => {
  addPackageRef.value?.open('create', undefined, installStore.getInstallConfig.openGaussVersion)
}

const handleOk = () => {
  processVisible.value = false
}

const storeData = computed(() => installStore.getInstallConfig)

</script>

<style lang="less" scoped>
@import url('~@/assets/style/ops/ops.less');

.empty-content {
  font-weight: bold;
  color: var(--color-neutral-4);
}

.panel-overflow {
  overflow: auto;
}
</style>
