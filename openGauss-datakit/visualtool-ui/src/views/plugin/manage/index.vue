<template>
  <div class="app-container" id="pluginManage">
    <div class="main-bd">
      <a-spin :loading="doLoading" style="display: block;">
        <div class="search-con">
          <a-form :model="form" layout="inline">
            <a-form-item field="keyWords" style="margin-left: -17px;">
              <a-input v-model="form.keyWords" allow-clear :placeholder="$t('manage.index.5m5v55wx37o0')" style="width: 200px;" @change="getList"></a-input>
            </a-form-item>
            <a-form-item field="status" :label="$t('manage.index.5m5v55wxj5s0')">
              <a-select v-model="form.status" :placeholder="$t('manage.index.5m5v55wxjf40')" allow-clear style="width: 200px;" @change="getList">
                <a-option :value="2">{{$t('manage.index.5m5v55wxjjs0')}}</a-option>
                <a-option :value="1">{{$t('manage.index.5m5v55wxjo00')}}</a-option>
              </a-select>
            </a-form-item>
            <a-form-item>
              <a-button type="outline" @click="getList">
                <template #icon>
                  <icon-search />
                </template>
                <template #default>{{$t('manage.index.5m5v55wxjsg0')}}</template>
              </a-button>
              <a-button style="margin-left: 10px;" @click="resetQuery">
                <template #icon>
                  <icon-sync />
                </template>
                <template #default>{{$t('manage.index.5m5v55wxjwg0')}}</template>
              </a-button>
            </a-form-item>
          </a-form>
          <div class="btn-con">
            <a-button type="primary" @click="handleUpload">
              <template #icon>
                <icon-plus />
              </template>
              <template #default>{{$t('manage.index.5m5v55wxk140')}}</template>
            </a-button>
          </div>
        </div>
        <div v-if="!loading && pluginList.length" class="list-con">
          <div v-for="item in pluginList" :key="item.id" class="list-item">
            <div class="item-info">
              <div class="plugin-logo">
                <img :src="item.logoPath" alt="">
              </div>
              <div class="plugin-info">
                <div class="plugin-name-con">
                  <div class="name" :title="item.pluginId">{{item.pluginId}}</div>
                  <div class="version" :title="item.pluginVersion">{{item.pluginVersion}}</div>
                  <div class="author" v-if="item.pluginProvider" :title="item.pluginProvider">{{item.pluginProvider}}</div>
                </div>
                <div class="plugin-desc" :title="currentLocale === 'zh-CN' ? item.pluginDesc : item.pluginDescEn">{{ currentLocale === 'zh-CN' ? item.pluginDesc : item.pluginDescEn }}</div>
              </div>
            </div>
            <div class="item-btn-con">
              <div class="btn-left">
                <a-popconfirm :content="$t('manage.index.else3', {id: item.pluginId})" @ok="handleUninstall(item)">
                  <a-button type="outline" size="mini">
                    <template #icon>
                      <icon-delete />
                    </template>
                    <template #default>{{$t('manage.index.5m5v55wxkrw0')}}</template>
                  </a-button>
                </a-popconfirm>

                <a-button
                  v-if="item.extendInfo && item.extendInfo.pluginLicenseType"
                  type="dashed"
                  shape="round"
                  size="mini"
                  status="success"
                  @click="handleAuthDetail(item, item.extendInfo)"
                >
                  <template #icon>
                    <icon-info-circle />
                  </template>
                  <template #default>
                    {{ $t(`manage.index.${item.extendInfo.pluginLicenseType}`) }}
                  </template>
                </a-button>

                <template v-if="item.extendInfo && item.extendInfo.authAddress">
                  <a-link
                    v-if="isAbsolutePath(item.extendInfo.authAddress)"
                    :href="item.extendInfo.authAddress"
                    target="_blank"
                    class="update-auth-link"
                  >
                    <template #icon>
                      <icon-link />
                    </template>
                    {{ $t('manage.index.update_authorization') }}
                  </a-link>
                  <router-link v-else class="update-auth-link" :to="`/static-plugin/${item.pluginId}${item.extendInfo.authAddress}`">
                    {{$t('manage.index.update_authorization')}}
                  </router-link>
                </template>
              </div>
              <div class="btn-right">
                <a-switch type="line" v-model="item.pluginStatus" :checked-value="1" :unchecked-value="2" @change="switchChange(item)"></a-switch>
                <span class="txt">{{item.pluginStatus === 1 ? $t('manage.index.5n6f62aai3k0') : $t('manage.index.5n6f62aajx80')}}</span>
              </div>
            </div>
          </div>
        </div>
        <div v-if="!pluginList.length" class="loading-con">
          <a-spin v-if="loading" :size="32" />
          <a-empty v-else />
        </div>
      </a-spin>
    </div>

    <!-- plugin auth detail -->
    <a-modal
      :title="$t('manage.index.authorization_detail')"
      v-model:visible="authDetail.open"
      :footer="false"
      @cancel="handleAuthDetail(null)"
      width="600px"
      title-align="start"
    >
      <div v-if="authDetail.info && authDetail.extendInfo" class="auth-detail">
        <div class="auth-detail__item">
          <label class="auth-detail__label">{{ $t('manage.index.plugin_name') }}：</label>
          <div class="auth-detail__value">
            <a-space size="mini">
              <img :src="authDetail.info.logoPath" :alt="authDetail.info.pluginId" class="auth-detail__logo">
              <span>{{ authDetail.info.pluginId }}</span>
            </a-space>
          </div>
        </div>
        <div class="auth-detail__item">
          <label class="auth-detail__label">{{ $t('manage.index.authorization') }}：</label>
          <div class="auth-detail__value">
            <a-button
              type="dashed"
              shape="round"
              size="mini"
              status="success"
              class="auth-detail__auth-type"
            >
              {{ $t(`manage.index.${authDetail.extendInfo.pluginLicenseType}`) }}
            </a-button>
          </div>
        </div>
        <div class="auth-detail__item" v-if="authDetail.extendInfo.pluginActivationTime">
          <label class="auth-detail__label">{{ $t('manage.index.activation_time') }}：</label>
          <div class="auth-detail__value">
            {{ dayjs(authDetail.extendInfo.pluginActivationTime).format('YYYY-MM-DD HH:mm:ss') }}
          </div>
        </div>
        <div class="auth-detail__item" v-if="authDetail.extendInfo.pluginExpirationTime">
          <label class="auth-detail__label">{{ $t('manage.index.expires') }}：</label>
          <div class="auth-detail__value">
            {{ dayjs(authDetail.extendInfo.pluginExpirationTime).format('YYYY-MM-DD HH:mm:ss') }}
          </div>
        </div>
        <div class="auth-detail__item full">
          <label class="auth-detail__label">{{ $t('manage.index.plugin_description') }}：</label>
          <div class="auth-detail__value">{{ authDetail.info.pluginDesc }}</div>
        </div>
        <div class="auth-detail__item full" v-if="authDetail.extendInfo.pluginDevelopmentCompany">
          <label class="auth-detail__label">{{ $t('manage.index.company') }}：</label>
          <div class="auth-detail__value">{{ authDetail.extendInfo.pluginDevelopmentCompany }}</div>
        </div>
        <div class="auth-detail__item" v-if="authDetail.extendInfo.phoneNumber">
          <label class="auth-detail__label">{{ $t('manage.index.phone') }}：</label>
          <div class="auth-detail__value">{{ authDetail.extendInfo.phoneNumber }}</div>
        </div>
        <div class="auth-detail__item" v-if="authDetail.extendInfo.email">
          <label class="auth-detail__label">{{ $t('manage.index.email') }}：</label>
          <div class="auth-detail__value">{{ authDetail.extendInfo.email }}</div>
        </div>
        <div class="auth-detail__item full" v-if="authDetail.extendInfo.companyAddress">
          <label class="auth-detail__label">{{ $t('manage.index.address') }}：</label>
          <div class="auth-detail__value">{{ authDetail.extendInfo.companyAddress }}</div>
        </div>
        <div class="auth-detail__item" v-if="authDetail.extendInfo.userGuide">
          <label class="auth-detail__label">{{ $t('manage.index.manual') }}：</label>
          <div class="auth-detail__value">
            <a-link :href="authDetail.extendInfo.userGuide" target="_blank">
              {{ authDetail.extendInfo.userGuide }}
            </a-link>
          </div>
        </div>
        <div class="auth-detail__item" v-if="authDetail.extendInfo.demoAddress">
          <label class="auth-detail__label">{{ $t('manage.index.demo') }}：</label>
          <div class="auth-detail__value">
            <a-link :href="authDetail.extendInfo.demoAddress" target="_blank">
              {{ authDetail.extendInfo.demoAddress }}
            </a-link>
          </div>
        </div>
      </div>
    </a-modal>
    <plugin-install v-model:visible="uploadModal" :wsId="wsBusinessId" @downloadStart="downloadStart" />
    <download-notification ref="downloadNotRef" :percentage="currPercent" :msg="$t('manage.index.onlineDownload')" :iconClass="'rar-file'"
                           :fileName="uploadName"></download-notification>
    <plugin-config v-model:open="configVisible" :plugin-id="configPluginId" :configAttrs="configAttrs" :configData="configData" />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, watch } from 'vue'
import { useAppStore, useTabBarStore } from '@/store'
import { Message } from '@arco-design/web-vue'
import { getToken } from '@/utils/auth'
import { list, start, stop, uninstall, extendInfoList } from '@/api/plugin'
import { getUserInfo } from '@/api/user'
import PluginConfig from './components/PluginConfig.vue'
import PluginInstall from './components/PluginInstall.vue'
import { destroyPluginApp } from '@/utils/pluginApp'
import useLocale from '@/hooks/locale'
import dayjs from 'dayjs'
import downloadNotification from '@/components/downloadNotification'
import showMessage from "@/hooks/showMessage";

const appStore = useAppStore()
const tabBarStore = useTabBarStore()
const { currentLocale } = useLocale()
const loading = ref<boolean>(true)
const doLoading = ref<boolean>(false)

const form = reactive({
  keyWords: undefined,
  status: undefined
})

const upload = reactive({
  open: false,
})

const authDetail = reactive<any>({
  open: false,
  info: null,
  extendInfo: null
})

const pluginList = ref<any[]>([])
const configPluginId = ref<string | number>('')
const configVisible = ref<boolean>(false)
const configAttrs = ref<any[]>([])
const configData = ref<any>({})

const getList = () => {
  const params = {
    pluginId: form.keyWords,
    pluginStatus: form.status
  }

  loading.value = true
  Promise.all([
    list(params),
    extendInfoList(params)
  ])
    .then(([listRes, extendInfoRes]: any[]) => {
      const nextPluginList = (listRes.rows ?? []).map((item: any) => Object.assign(item, {
        extendInfo: extendInfoRes.rows.find((info: any) => info.pluginId === item.pluginId) ?? null
      }))

      pluginList.value = nextPluginList
    })
    .finally(() => {
      loading.value = false
      doLoading.value = false
    })
}

const resetQuery = () => {
  form.keyWords = undefined
  form.status = undefined
  getList()
}
const uploadModal = ref<boolean>(false)
const handleUpload = () => {
  webSocketOpen()
  uploadModal.value = true
  upload.open = true
}

watch(
  () => uploadModal.value,
  (oldVal, newVal) => {
    if (newVal && !oldVal) {
      appStore.fetchServerMenuConfig()
      getList()
    }
  }
)
const switchChange = async (row: any) => {
  const pluginId = row.pluginId
  if (row.pluginStatus !== 2) {
    try {
      doLoading.value = true
      await start(pluginId)
      doLoading.value = false
      Message.success('Start success')
    } catch (e) {
      doLoading.value = false
    }
  } else {
    try {
      doLoading.value = true
      await stop(pluginId)
      doLoading.value = false
      Message.success('Stop success')
      const tagList: any[] = [...tabBarStore.tagList]
      tagList.forEach((item: any) => {
        if (~item.fullPath.indexOf(`/${pluginId}/`)) {
          tabBarStore.deleteTags(item)
          destroyPluginApp(item.fullPath)
        }
      })
    } catch (e) {
      doLoading.value = false
    }
  }
  appStore.fetchServerMenuConfig()
  getList()
}

const handleUninstall = async (row: any) => {
  const pluginId = row.pluginId
  try {
    doLoading.value = true
    await uninstall(pluginId)
    doLoading.value = false
    Message.success('Uninstall success')
    const tagList: any[] = [...tabBarStore.tagList]
    tagList.forEach((item: any) => {
      if (~item.fullPath.indexOf(`/${pluginId}/`)) {
        tabBarStore.deleteTags(item)
        destroyPluginApp(item.fullPath)
      }
    })
  } catch (e) {
    doLoading.value = false
  }
  appStore.fetchServerMenuConfig()
  getList()
}

const isAbsolutePath = (url: string): boolean => url.startsWith('http')

const handleAuthDetail = (info: any, extendInfo: any = null): void => {
  authDetail.info = info
  authDetail.extendInfo = extendInfo
  authDetail.open = Boolean(info)
}
const wsBusinessId = ref('')
const currPercent = ref<number>(0)
const percentLoading = ref<boolean>(false)
const processVisible = ref<boolean>(false)

watch(currPercent, (newValue) => {
  if (newValue === 100) {
    processVisible.value = false
    closeNotification()
    appStore.fetchServerMenuConfig()
    getList()
  }
})
const notificationVisible = ref<boolean>(false)
const closeNotification = () => {
  notificationVisible.value = false
  uploadName.value = ''
}

const uploadName = ref('')
const downloadStart = (name: string) => {
  if (name === 'END') {
    getList()
    appStore.fetchServerMenuConfig()
  } else {
    uploadName.value = name
    notificationVisible.value = true
  }
}

const timer = ref<any>(null)
const lastProcess = ref(0);
const nextProcess = ref(0);
const webSocketOpen = () => {
  currPercent.value = 0
  const socketKey = new Date().getTime()
  const wsPrefix = window.location.protocol.includes('https') ? 'wss' : 'ws'
  const socketUrl = `${wsPrefix}://${window.location.host}/websocket/1/downloadPlugin_${socketKey}`
  const websocket = new WebSocket(socketUrl)
  wsBusinessId.value = `downloadPlugin_${socketKey}`
  websocket.onopen = function (event) {
    wsBusinessId.value = `downloadPlugin_${socketKey}`
  }
  websocket.onmessage = function (event) {
    processVisible.value = true
    const messageData = event.data
    downloadNotRef.value?.createOrUpdateNotification(wsBusinessId, messageData, uploadName.value)
    if (messageData === 'File download Failed') {
      showMessage('error', t('components.Package.5mtcyb0rty52'))
      websocket.close()
      downloadNotRef.value?.closeNotifiCation(wsBusinessId);
      clearInterval(timer.value)
    } else {
      if (!isNaN(Number(messageData))) {
        const percent = Number(messageData)
        nextProcess.value = percent;
        clearInterval(timer.value);
        timer.value = setInterval(() => {
          if (nextProcess.value === 1) {
            lastProcess.value = 0;
            nextProcess.value = 0;
          } else if (nextProcess.value.toString() === lastProcess.value.toString()) {
            let warnningMsg = setTimeout(() => {
              showMessage('error', 'websocket长时间无响应，将关闭下载进度悬浮框')
              downloadNotRef.value?.closeNotifiCation(wsBusinessId);
              clearTimeout(warnningMsg)
            }, 3000)
            websocket.close();
            clearInterval(timer.value);
          } else {
            lastProcess.value = nextProcess.value
          }
        }, 10000)
        if (percent === 1) {
          clearInterval(timer.value);
          percentLoading.value = false
          lastProcess.value = 0;
          nextProcess.value = 0;
          websocket.close()
          downloadNotRef.value?.closeNotifiCation(wsBusinessId);
          doLoading.value = true
          downloadStart('END')
        }
      } else if (messageData === 'DOWNLOAD_FINISH') {
        percentLoading.value = false
        websocket.close()
        downloadNotRef.value?.closeNotifiCation(wsBusinessId);
        lastProcess.value = 0;
        nextProcess.value = 0;
        clearInterval(timer.value)
        doLoading.value = true
        downloadStart('END')
      } else {
        console.error('WebSocket error', messageData)
        websocket.close()
        downloadNotRef.value?.closeNotifiCation(wsBusinessId);
        lastProcess.value = 0;
        nextProcess.value = 0;
        clearInterval(timer.value)
      }
    }
  }
  websocket.onclose = function () {
    console.log('WebSocket Close')
  }
  websocket.onerror = function (error) {
    console.error('WebSocket error:', error)
  }
}

const downloadNotRef = ref(null)

const init =() => {
  getList()
}
onMounted(() => {
  init()
})
</script>

<style lang="less" scoped>
.app-container {
  .main-bd {
    position: relative;
    .loading-con {
      position: absolute;
      left: 0;
      top: 0;
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      pointer-events: none;
    }
    .search-con {
      padding: 16px 20px;
      display: flex;
      justify-content: space-between;
    }
    .list-con {
      padding-left: 20px;
      padding-bottom: 20px;
      display: grid;
      grid-column-gap: 20px;
      grid-row-gap: 20px;
      grid-template-columns: repeat(auto-fill, 400px);
      justify-content: center;
      margin: auto;
      max-width: calc(400px * 3 + 20 * 3);
      .list-item {
        background: var(--color-bg-3);
        box-shadow: 0px 0px 8px 0px rgba(201,205,212,0.5);
        border-radius: 2px;
        padding: 16px;
        width: 100%;
        height: 190px;
        grid-column-start: auto;
        grid-row-start: auto;
        position: relative;
        .item-info {
          display: flex;
          align-items: flex-start;
          .plugin-logo {
            margin-right: 10px;
            width: 50px;
            height: 50px;
            padding: 5px;
            border: 1px solid transparent;
            display: flex;
            justify-content: center;
            align-items: center;
            img {
              width: 40px;
              height: 40px;
              border-radius: 50%;
            }
          }
          .plugin-info {
            .plugin-name-con {
              display: flex;
              align-items: center;
              width: 100%;
              margin-bottom: 8px;
              .name {
                font-size: 16px;
                color: var(--color-text-1);
              }
              .version {
                margin-top: 3px;
                margin-left: 5px;
                margin-right: 10px;
                font-size: 12px;
                color: rgb(var(--primary-6));
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }
              .author {
                border: 1px solid var(--color-text-2);
                height: 18px;
                line-height: 16px;
                padding: 0 5px;
                font-size: 12px;
                color: var(--color-text-2);
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              }
            }
            .plugin-desc {
              font-size: 14px;
              color: var(--color-text-2);
              line-height: 20px;
              overflow: hidden;
              text-overflow: ellipsis;
              display: -webkit-box;
              -webkit-line-clamp: 4;
              -webkit-box-orient: vertical;
            }
          }
        }
        .item-btn-con {
          position: absolute;
          left: 0;
          bottom: 16px;
          display: flex;
          justify-content: space-between;
          align-items: center;
          width: 100%;
          padding: 0 20px;
          .btn-left {
            display: flex;
            align-items: center;
            gap: 12px;

            :deep(.arco-btn.arco-btn-size-mini) {
              display: inline-flex;
              align-items: center;
              line-height: 24px;
            }

            :deep(.update-auth-link) {
              font-size: 12px;
            }
          }
          .btn-right {
            display: flex;
            align-items: center;
            .txt {
              color: var(--color-text-2);
              font-size: 14px;
              margin-left: 5px;
            }
          }
        }
      }
    }
  }
}

.upload-info {
  background: var(--color-fill-2);
  border: 1px dotted var(--color-fill-4);
  height: 180px;
  width: 100%;
  border-radius: 2px;
  text-align: center;
  .upload-icon {
    margin-top: 44px;
    margin-bottom: 16px;
  }
  .tips-1 {
    font-size: 16px;
    color: var(--color-text-1);
    margin-bottom: 12px;
    .highlight {
      color: rgb(var(--primary-6));
    }
  }
  .tips-2 {
    font-size: 14px;
    color: var(--color-text-1);
  }
}
.modal-footer {
  text-align: center;
}

.auth-detail {
  display: flex;
  flex-wrap: wrap;
  flex-direction: row;
  align-content: start;
  gap: 8px 20px;
  min-height: 200px;
  font-size: 12px;
  line-height: 22px;

  &__item {
    display: flex;
    min-width: calc(50% - 10px);
    width: calc(50% - 10px);
  }
  &__item.full {
    min-width: 100%;
    width: 100%;
  }

  &__label {
    width: 80px;
    font-weight: 500;
    text-align: right;
  }

  &__value {
    display: flex;
    align-items: center;
    flex: 1;
  }

  &__logo {
    width: 20px;
    height: 20px;
  }

  &__auth-type {
    padding-top: 2px;
    height: 22px;
    cursor: default;
  }
}
</style>
