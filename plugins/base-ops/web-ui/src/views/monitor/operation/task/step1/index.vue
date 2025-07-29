<template>
  <div class="container" id="taskStep1">
    <div class="main">
      <div class="left">
        <div ref="module1" class="module" id="server">
          <div class="item">
            <h2>{{ $t('operation.task.step1.index.serverConfig') }}</h2>
          </div>
          <a-form :model="data" :rules="serverRules" validateTrigger="onBlur" labelAlign="left" ref="serverFormRef">
            <div v-if="changeHostipFlag">
              <a-form-item :label="$t('operation.task.step1.index.IP')" field="hostIp" validate-trigger="blur">
                <a-popover position="right" trigger="click">
                  <a-select field="hostIp" v-model="ipPublicPrivate" allow-search show-search :options="hostIpList"
                    @change="fetchUserList" :placeholder="$t('operation.task.step1.index.IPPlaceholder')" style="width: 30%; height: 32px">
                  </a-select>
                  <template #content>
                    <p>{{ $t('operation.task.step1.index.IPContent') }}</p>
                  </template>
                </a-popover>
              </a-form-item>
              <a-form-item :label="$t('operation.task.step1.index.installUser')" field="hostUser" validate-trigger="blur">
                <a-popover position="right" trigger="click">
                  <a-select field="hostUserList" v-model="data.hostUser" :options="hostUserList" optionFilterProp="label"
                    allow-search show-search allow-clear @change="getHostId" :placeholder="$t('operation.task.step1.index.installUserPlaceholder')"
                    style="width: 30%; height: 32px">
                  </a-select>
                  <template #content>
                    <p>{{ $t('operation.task.step1.index.userContent') }}</p>
                  </template>
                </a-popover>
              </a-form-item>
            </div>
            <div v-else>
              <a-form-item :label="$t('operation.task.step1.index.IP')" field="hostIp" validate-trigger="blur">
                <a-select field="hostIp" v-model="ipPublicPrivate" allow-search show-search :options="hostIpList"
                  @change="fetchUserList" :placeholder="$t('operation.task.step1.index.IPPlaceholder')" style="width: 30%; height: 32px">
                </a-select>
              </a-form-item>
              <a-form-item :label="$t('operation.task.step1.index.installUser')" field="hostUser" validate-trigger="blur">
                <a-select field="hostUserList" v-model="data.hostUser" :options="hostUserList" optionFilterProp="label"
                  allow-search show-search allow-clear @change="getHostId" :placeholder="$t('operation.task.step1.index.installUserPlaceholder')"
                  style="width: 30%; height: 32px">
                </a-select>
              </a-form-item>
            </div>

          </a-form>
        </div>
        <div ref="module2" class="module" id="package">
          <div class="item">
            <h2>{{ $t('operation.task.step1.index.packageConfig') }}</h2>
          </div>
          <a-form :model="data" :rules="serverRules" labelAlign="left" ref="packageFormRef">
            <a-form-item :label="$t('operation.task.step1.index.system')">
              <div v-if="data.os === OS.OPEN_EULER">
                <a-button type="primary" disabled>openEuler</a-button>
                <a-button disabled>centOs</a-button>
              </div>
              <div v-else>
                <a-button disabled>openEuler</a-button>
                <a-button type="primary" disabled>centOs</a-button>
              </div>
            </a-form-item>
            <a-form-item :label="$t('operation.task.step1.index.cpuArch')">
              <div v-if="data.cpuArch === CpuArch.X86_64">
                <a-button type="primary" disabled>{{ CpuArch.X86_64 }}</a-button>
                <a-button disabled>{{ CpuArch.AARCH64 }}</a-button>
              </div>
              <div v-else>
                <a-button disabled>{{ CpuArch.X86_64 }}</a-button>
                <a-button type="primary" disabled>{{ CpuArch.AARCH64 }}</a-button>
              </div>
            </a-form-item>
            <a-form-item :label="$t('operation.task.step1.index.version')">
              <div class="flex-row-center panel-body">
                <a-button class="squarebutton mr-xlg"
                  :class="'card-item-c ' + (currVersion === OpenGaussVersionEnum.MINIMAL_LIST ? 'center-item-active' : '')"
                  @click="chooseVer(OpenGaussVersionEnum.MINIMAL_LIST)">
                  <svg-icon icon-class="people-safe" class="icon-size mb-0"></svg-icon>
                  <div class="label-color ft-lg mb-s" style="font-weight: bold;">{{ $t('operation.task.step1.index.MINIMAL_LIST') }}</div>
                  <div class="label-color remark" style="font-size: 11px;">
                    <a-typography-text>
                      {{ $t('operation.task.step1.index.MINIMAL_LISTContent') }}
                    </a-typography-text>
                   </div>
                </a-button>
                <a-button class="squarebutton mr-xlg"
                  :class="'card-item-c ' + (currVersion === OpenGaussVersionEnum.LITE ? 'center-item-active' : '')"
                  @click="chooseVer(OpenGaussVersionEnum.LITE)">
                  <svg-icon icon-class="people-safe" class="icon-size mb-0"></svg-icon>
                  <div class="label-color ft-lg mb-s" style="font-weight: bold;">{{ $t('operation.task.step1.index.LITE') }}</div>
                  <p class="label-color remark" style="font-size: 11px;">
                    <a-typography-text>
                      {{ $t('operation.task.step1.index.LITEContent') }}
                    </a-typography-text>
                  </p>
                </a-button>
                <a-button class="squarebutton mr-xlg"
                  :class="'card-item-c ' + (currVersion === OpenGaussVersionEnum.ENTERPRISE ? 'center-item-active' : '')"
                  @click="chooseVer(OpenGaussVersionEnum.ENTERPRISE)">
                  <svg-icon icon-class="people-safe" class="icon-size mb-0"></svg-icon>
                  <div class="label-color ft-lg mb-s" style="font-weight: bold;">{{ $t('operation.task.step1.index.ENTERPRISE') }}</div>
                  <div class="label-color remark" style="font-size: 11px;">
                    <a-typography-text>
                      {{ $t('operation.task.step1.index.ENTERPRISEContent') }}
                    </a-typography-text>
                  </div>
                </a-button>
              </div>
            </a-form-item>
            <a-form-item :label="$t('operation.task.step1.index.versionNum')" field="packageVersionNum">
              <a-select field="packageVersionNum" v-model="data.packageVersionNum" allow-create allow-clear allow-search
                show-search :placeholder="$t('operation.task.step1.index.versionPlaceholder')" :options="versionNumList" :inputmode="true" @change="checkContains"
                style="width: 30%; height: 32px">
              </a-select><br>
            </a-form-item>
            <a-form-item :label="$t('operation.task.step1.index.selectedPackage')">
              <div v-if="!packageSerchResult">
                <p>{{ $t('operation.task.step1.index.morePackage') }}</p>
                <a-button class="primary" type="text" @click="addPackInstall('create', 1)">{{ $t('operation.task.step1.index.upload') }}</a-button>
                <a-button class="primary" type="text" @click="addPackInstall('create', 0)">{{ $t('operation.task.step1.index.download') }}</a-button>
              </div>
              <div v-else>
                <p>{{ data.packageName }}</p>
                <a-button class="primary" type="text" @click="openPackManage()">{{ $t('operation.task.step1.index.packageManagement') }}</a-button>
                <a-button class="primary" type="text" @click="addPackInstall('create', 1)">{{ $t('operation.task.step1.index.upload') }}</a-button>
                <a-button class="primary" type="text" @click="addPackInstall('create', 0)">{{ $t('operation.task.step1.index.download') }}</a-button>
              </div>
            </a-form-item>
          </a-form>
        </div>
        <div ref="module3" class="module" id="cluster">
          <div class="item">
            <h2>{{ $t('operation.task.step1.index.clusterConfig') }}</h2>
          </div>
          <a-form :model="data" :rules="serverRules" labelAlign="left" ref="clusterFormRef">
            <a-form-item :label="$t('operation.task.step1.index.clusterName')" field="clusterName" validate-trigger="blur">
              <a-input v-model="data.clusterName" :placeholder="$t('operation.task.step1.index.clusterNamePlaceholder')" @change="getClusterName" :max-length="255"
                style="width: 30%; height: 32px" />
            </a-form-item>
            <a-form-item :label="$t('operation.task.step1.index.installPath')" field="installPath" validate-trigger="blur">
              <a-input v-model="data.installPath" :placeholder="$t('operation.task.step1.index.installPathPlaceholder')" @change="getInstallPath" :max-length="255"
                style="width: 30%; height: 32px" />
              <template #extra>
                <span class="form-helper-text">{{ $t('operation.task.step1.index.installPathContent') }}</span>
              </template>
            </a-form-item>
            <a-form-item :label="$t('operation.task.step1.index.installPackagePath')" field="installPackagePath">
              <a-input v-model="data.installPackagePath" :placeholder="$t('operation.task.step1.index.installPackagePathplaceholder')" :max-length="255"
                style="width: 30%; height: 32px" />
              <template #extra>
                <span class="form-helper-text">{{ $t('operation.task.step1.index.installPackagePathcontent') }}</span>
              </template>
            </a-form-item>
            <a-form-item :label="$t('operation.task.step1.index.port')" field="port">
              <a-input-number id="inputNumber" v-model="data.port" :min="1024" :max="65535" @change="getPort"
                style="width: 10%; height: 32px" :step="10" />
            </a-form-item>
            <a-form-item :label="$t('operation.task.step1.index.dbPwd')" field="databasePassword">
              <a-input-password v-model="data.databasePassword" :placeholder="$t('operation.task.step1.index.placeholder')" :max-length="255"
                style="width: 30%; height: 32px" />
              <template #extra>
                <span class="form-helper-text">{{ $t('operation.task.step1.index.dbPwdContent') }}</span>
              </template>
            </a-form-item>
            <a-form-item :label="$t('operation.task.step1.index.deployType')">
              <a-radio-group v-model="data.deployType" @change="clusterModeChange" type="button">
                <a-radio value="SINGLE_NODE" :disabled="flagCM.valueOf()">{{ $t('operation.task.step1.index.SINGLE_NODE') }}</a-radio>
                <a-radio value="CLUSTER">{{ $t('operation.task.step1.index.CLUSTER') }}</a-radio>
              </a-radio-group>
            </a-form-item>
            <a-form-item v-if="data.packageVersion === OpenGaussVersionEnum.ENTERPRISE && data.deployType === 'CLUSTER'"
              :label="$t('operation.task.step1.index.checkflagCM')">
              <a-switch v-model="flagCM" @change="checkflagCM" />
            </a-form-item>
            <a-form-item :label="$t('operation.task.step1.index.checkflagEnvSeqar')" v-if="data.packageVersion !== OpenGaussVersionEnum.MINIMAL_LIST">
              <a-switch v-model="flagEnvSeqar" @change="checkflagEnvSeqar" />
            </a-form-item>
            <a-form-item v-if="flagEnvSeqar && data.packageVersion !== OpenGaussVersionEnum.MINIMAL_LIST" :label="$t('operation.task.step1.envPath')"
              field="envPath">
              <a-input v-model="data.envPath" :placeholder="$t('operation.task.step1.index.envPathPlaceholder')" :max-length="255"
                style="width: 30%; height: 32px" />
            </a-form-item>
            <div class="item" v-if="data.packageVersion === OpenGaussVersionEnum.ENTERPRISE">
              <a-collapse :default-active-key="['0']">
                <a-collapse-item :key='1' :header="$t('operation.task.step1.index.proConfig')">
                  <a-form-item :label="$t('operation.task.step1.index.logPath')" field="logPath">
                    <a-input v-model="data.logPath" :max-length="255" autofocus :placeholder="$t('operation.task.step1.index.logPathPlaceholder')" />
                  </a-form-item>
                  <a-form-item :label="$t('operation.task.step1.index.tmpPath')" field="tmpPath">
                    <a-input v-model="data.tmpPath" autofocus :placeholder="$t('operation.task.step1.index.tmpPathPlaceholder')" style="width: 30%; height: 32px" />
                  </a-form-item>
                  <a-form-item :label="$t('operation.task.step1.index.omToolsPath')" field="omToolsPath">
                    <a-input v-model="data.omToolsPath" :max-length="255" autofocus :placeholder="$t('operation.task.step1.index.omToolsPathPlaceholder')"
                      style="width: 30%; height: 32px" />
                  </a-form-item>
                  <a-form-item :label="$t('operation.task.step1.index.corePath')" field="corePath">
                    <a-input v-model="data.corePath" autofocus :max-length="255" :placeholder="$t('operation.task.step1.index.corePathPlaceholder')"
                      style="width: 30%; height: 32px" />
                  </a-form-item>
                </a-collapse-item>
              </a-collapse>
            </div>
          </a-form>
        </div>
        <div ref="module4" class="module" id="clusternode">
          <div class="item">
            <h2>{{ $t('operation.task.step1.index.nodeConfig') }}</h2>
          </div>
          <a-button type="text" icon="plus" @click="addColumn"
            v-if="clusterNodesLimit > data.clusterNodes.length">{{ $t('operation.task.step1.index.addColumn') }}</a-button>
          <a-table :data="data.clusterNodes" style="margin-top: 20px; width: 100%" :pagination="false" column-resizable
            :bordered="{ cell: true }">
            <template #columns>
              <a-table-column :title="$t('operation.task.step1.index.IP')" data-index="displayHostIp" fixed="left">
                <template #cell="{ record }">
                  <div class="flex-row-start" v-if="record.editing && record.nodeType !== 'MASTER'">
                    <a-select :options="hostIpSame" @change="e => checkSameUser(e, record.order)"
                      :default-value="record.displayHostIp" />
                  </div>
                  <div v-else>
                    <p>{{ record.displayHostIp }}</p>
                  </div>
                </template>
              </a-table-column>
              <a-table-column :title="$t('operation.task.step1.index.installUser')" data-index="hostUser"></a-table-column>
              <a-table-column :title="$t('operation.task.step1.index.clusterType')" data-index="nodeType">
                <template #cell="{ record }">
                  <div class="flex-row-start" v-if="record.nodeType === 'MASTER'">
                    <p>{{ $t('operation.task.step1.index.clusterMaster') }}</p>
                  </div>
                  <div v-else>
                    <p>{{ $t('operation.task.step1.index.clusterOthers') }}</p>
                  </div>
                </template>
              </a-table-column>
              <a-table-column :title="$t('operation.task.step1.index.dataPath')" data-index="dataPath"
                v-if="data.packageVersion !== OpenGaussVersionEnum.MINIMAL_LIST">
                <template #cell="{ record }">
                  <div class="flex-row-start" v-if="record.editing">
                    <a-input :default-value="record.dataPath" @change="e => checkClusterPathDB(e, record.order)"
                      :max-length="255" />
                  </div>
                  <div v-else>
                    <p>{{ record.dataPath }}</p>
                  </div>
                </template>
              </a-table-column>
              <a-table-column :title="$t('operation.task.step1.index.azOwner')" data-index="azOwner"
                v-if="data.packageVersion === OpenGaussVersionEnum.ENTERPRISE">
                <template #cell="{ record }">
                  <div class="flex-row-start" v-if="record.editing">
                    <a-select :options="azOwnerList" @change="e => changeAZOwner(e, record.order)"
                      :default-value="record.azOwner" />
                  </div>
                  <div v-else>
                    <p>{{ record.azOwner }}</p>
                  </div>
                </template>
              </a-table-column>
              <a-table-column :title="$t('operation.task.step1.index.azPriority')" data-index="azPriority"
                v-if="data.packageVersion === OpenGaussVersionEnum.ENTERPRISE">
                <template #cell="{ record }">
                  <div class="flex-row-start" v-if="record.editing">
                    <a-input-number @change="e => checkAzPriority(e, record.order)" :min="1" :max="10"
                      :default-value="record.azPriority" />
                  </div>
                  <div v-else>
                    <p>{{ record.azPriority }}</p>
                  </div>
                </template>
              </a-table-column>
              <a-table-column :title="$t('operation.task.step1.index.isCMMaster')" data-index="isCMMaster"
                v-if="data.packageVersion === OpenGaussVersionEnum.ENTERPRISE && data.enableCmTool">
                <template #cell="{ record }">
                  <div class="flex-row-start" v-if="record.editing">
                    <a-popconfirm :content="$t('operation.task.step1.index.cmMasterContent')" type="warning"
                      :ok-text="$t('operation.task.step1.index.confirm')" :cancel-text="$t('operation.task.step1.index.cancel')">
                      <a-select :options="optionsCMMaster" @change="e => checkCMMaster(e, record.order)"
                        :default-value="record.isCMMaster ? $t('operation.task.step1.index.yes') : $t('operation.task.step1.index.no')" />
                    </a-popconfirm>
                  </div>
                  <div v-else>
                    <p v-if="record.isCMMaster">{{ $t('operation.task.step1.index.yes') }}</p>
                    <p v-else>{{ $t('operation.task.step1.index.no') }}</p>
                  </div>
                </template>
              </a-table-column>
              <a-table-column :title="$t('operation.task.step1.index.cmDataPath')" data-index="cmDataPath"
                v-if="data.packageVersion === OpenGaussVersionEnum.ENTERPRISE && data.enableCmTool">
                <template #cell="{ record }">
                  <div class="flex-row-start" v-if="record.editing">
                    <a-input @change="e => checkClusterPathCM(e, record.order)" :default-value="record.cmDataPath"
                      :max-length="255" />
                  </div>
                  <div v-else>
                    <p>{{ record.cmDataPath }}</p>
                  </div>
                </template>
              </a-table-column>
              <a-table-column :title="$t('operation.task.step1.index.cmPort')" data-index="cmPort"
                v-if="data.packageVersion === OpenGaussVersionEnum.ENTERPRISE && data.enableCmTool">
                <template #cell="{ record }">
                  <div class="flex-row-start" v-if="record.editing">
                    <a-popconfirm :content="$t('operation.task.step1.index.cmPortContent')" type="warning" :ok-text="$t('operation.task.step1.index.confirm')"
                      :cancel-text="$t('operation.task.step1.index.cancel')">
                      <a-input-number @change="e => checkCmPort(e, record.order)" :min="1024" :max="65529"
                        :default-value="record.cmPort" />
                    </a-popconfirm>
                  </div>
                  <div v-else>
                    <p>{{ record.cmPort }}</p>
                  </div>
                </template>
              </a-table-column>
              <a-table-column :title="$t('operation.task.step1.index.option')" data-index="operation"
                v-if="data.packageVersion !== OpenGaussVersionEnum.MINIMAL_LIST">
                <template #cell="{ record }">
                  <div class="flex-row-start" v-if="record.editing === false">
                    <a-link class="mr" @click="editCluster(record)">{{ $t('operation.task.step1.index.edit') }}</a-link>
                    <a-popconfirm :content="$t('operation.task.step1.index.deleteContent')" type="warning" :ok-text="$t('operation.task.step1.index.confirm')" :cancel-text="$t('operation.task.step1.index.cancel')"
                      @ok="deleteRows(record)">
                      <a-link status="danger" v-if="record.nodeType !== 'MASTER'">{{ $t('operation.task.step1.index.delete') }}</a-link>
                    </a-popconfirm>
                  </div>
                  <div class="flex-row-start" v-else>
                    <a-link class="mr" @click="saveCluster(record)">{{ $t('operation.task.step1.index.save') }}</a-link>
                    <a-link class="mr" @click="cancelEdit(record)">{{ $t('operation.task.step1.index.cancel') }}</a-link>
                  </div>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </div>
      </div>
      <div class="right">
        <div>
          <a-affix :offsetTop="80">
            <a-anchor :change-hash="false">
              <a-anchor-link href="#server">{{ $t('operation.task.step1.index.serverConfig') }}</a-anchor-link>
              <a-anchor-link href="#package">{{ $t('operation.task.step1.index.packageConfig') }}</a-anchor-link>
              <a-anchor-link href="#cluster">{{ $t('operation.task.step1.index.clusterConfig') }}</a-anchor-link>
              <a-anchor-link href="#clusternode">{{ $t('operation.task.step1.index.nodeConfig') }}</a-anchor-link>
            </a-anchor>
          </a-affix>
        </div>
      </div>
    </div>
    <download-notification ref="downloadNotRef" :percentage="currPercent" :msg="$t('operation.task.step1.index.notificationText')"
                           :iconClass="'rar-file'" :fileName="uploadName"></download-notification>
    <addPack ref="addPackRef" @close="addPackClose()" @submit="addPackSubmit()" @downloadStart="addPackStart"></addPack>
    <packManage ref="packManageRef" :packageIDSelected="packageIDSelected" @finish="packManageClose()"
      @ok="packManageSubmit()" @packageIDSelected="syncSubTask"></packManage>
  </div>
</template>

<script setup lang="ts">
import { computed, defineProps, onMounted, PropType, reactive, ref, toRaw, watch } from 'vue'
import { CpuArch, OS } from "@/types/os";
import { OpenGaussVersionEnum } from "@/types/ops/install"
import message from "@arco-design/web-vue/es/message"
import { Message } from "@arco-design/web-vue"
import { KeyValue } from "@antv/x6/lib/types"
import {
  checkVersionNumber,
  checkPkg,
  getHostInfo,
  getHostIp,
  getHostUser,
  getPackageList,
  getVersionNum,
  packageOnlineUpdate,
  checkPortExist, checkClusternameExist, checkClusterExist,
  batchClusterNodes, fetchAZList, updateClustertaskNode, createClustertaskNode, deleteClustertaskNode,
} from '@/api/ops'
import AddPack from "./AddPackage.vue"
import PackManage from "./PackageManage.vue"
import Socket from "@/utils/websocket"
import { useRoute } from "vue-router";
import downloadNotification from '@/components/downloadNotification'
import { useI18n } from 'vue-i18n'
import { encryptPassword } from "@/utils/jsencrypt";
const { t } = useI18n()
const module1 = ref(null)
const module2 = ref(null)
const module3 = ref(null)
const module4 = ref(null)
const packageIDSelected = ref()
const data = reactive({
  hostIp: '',
  hostId: "",
  hostUser: "",
  hostUserId: '',
  os: '',
  osVersion: '',
  cpuArch: '',
  basePath: '',
  packageVersion: OpenGaussVersionEnum.MINIMAL_LIST,
  packageVersionNum: '',
  packageId: '',
  packageName: '',
  clusterId: '',
  clusterName: '',
  databasePassword: "",
  port: 5432,
  clusterNodes: [],
  deployType: "SINGLE_NODE",
  installPath: "",
  installPackagePath: "",
  logPath: "",
  tmpPath: "",
  omToolsPath: "",
  corePath: "",
  envPath: "",
  enableCmTool: false,
  enableGenerateEnvironmentVariableFile: false,
  xmlConfigPath: "",
})

const openGaussVersion = ref()
const currVersion = ref(OpenGaussVersionEnum.MINIMAL_LIST)
const flagCM = ref(false)
const flagEnvSeqar = ref(false)

const tempDataPath = new FormData
const hostUserId = new FormData
const ipPublicPrivate = ref('')
const hostUserList = ref([])
const fetchUserList = (value: any) => {
  data.hostUser = ''
  ipPublicPrivate.value = value
  let tempHostIp = value.split('(').filter(part => part !== '')
  data.hostIp = tempHostIp.length > 0 ? tempHostIp[0] : ''
  masterHostIp.value = data.hostIp
  data.hostId = hostIpId.get(data.hostIp)
  if (data.clusterNodes.length > 0) {
    data.clusterNodes.forEach(item => {
      if (item.nodeType === 'MASTER') {
        item.displayHostIp = ipPublicPrivate.value
        item.hostId = data.hostId
        item.hostIp = data.hostIp
      }
    })
  }
  getHostUser(data.hostId).then((res) => {
    hostUserList.value = []
    res.data.forEach(item => {
      if (item.username !== 'root') {
        hostUserList.value.push(item.username)
        hostUserId.set(item.username, item.hostUserId)
      }
    })
  }).catch((error) => {
    console.log(error)
  }).finally(() => {
    fetchOsSystem(true)
  })
}
const hostUserOrder = ref(0)
const getHostId = (inputvalue: string) => {
  data.hostUser = inputvalue
  data.hostUserId = hostUserId.get(inputvalue)
  let tempPath = '/opt/' + data.hostUser + '/openGauss/tmp'
  tempDataPath.append(OpenGaussVersionEnum.MINIMAL_LIST, tempPath)
  tempPath = '/opt/' + data.hostUser + '/openGauss/tmp'
  tempDataPath.append(OpenGaussVersionEnum.LITE, tempPath)
  tempPath = '/opt/' + data.hostUser + '/openGauss/tmp'
  tempDataPath.append(OpenGaussVersionEnum.ENTERPRISE, tempPath)
  data.installPath = '/opt/' + data.hostUser + '/openGauss/install/app'
  data.installPackagePath = '/opt/' + data.hostUser + '/software/openGauss'
  hostUserOrder.value = hostUserList.value.indexOf(inputvalue)
  data.port = 5432 + hostUserOrder.value * 10
  if (changeHostipFlag) {
    data.clusterNodes.forEach((item) => {
      item.hostUserId = ''
      item.hostUser = data.hostUser
      item.editing = true
    })
  } else {
    initMasterNode()
  }
  if (data.packageName !== '') {
    data.packageName = ''
    fetchPackageList()
  }
}

const hostIpSame = ref([])
const fetchOsSystem = (isChange: boolean) => {
  getHostInfo(data.hostIp).then((res) => {
    if (res.code === 200) {
      data.os = res.data.os
      data.cpuArch = res.data.cpuArch
      data.osVersion = res.data.osVersion
    }
    if (!res.data.os || !res.data.cpuArch) {
      Message.error(t('operation.task.step1.index.serverDataError'))
    } else {
      fetchSameOs(isChange)
    }
  }).catch((error) => {
    console.error(error)
  })
}
const fetchSameOs = (isChange: boolean) => {
  const param = {
    os: data.os,
    osVersion: data.osVersion,
    cpuArch: data.cpuArch
  }
  getHostIp(param).then((res) => {
    if (Number(res.code) === 200) {
      hostIpSame.value = []
      res.data.forEach(item => { hostIpSame.value.push(item.displayIp) })
    } else {
      console.error({
        content: '获取ip失败'
      })
    }
  }).catch((error) => {
    console.error(error)
  }).finally(() => {
    if (changeHostipFlag && isChange) {
      data.clusterNodes.forEach((item) => {
        if (item.nodeType === 'MASTER') {
          item.hostIp = data.hostIp
          item.hostId = data.hostId
        } else if (hostIpSame.value === '' || !hostIpSame.value.includes(item.hostIp) || item.hostIp === data.hostIp) {
          item.hostIp = ''
          item.hostId = ''
        }
        item.editing = true
        tempEditArr.value[item.order] = item
        editFlagGroup.value[item.order] = {
          hostIp: false,
          cmDataPath: true,
          dataPath: true,
          CMMaster: false,
          CmPort: false,
          order: item.order
        }
      })
    }
  })
}

const chooseVer = (versionType: any) => {
  data.packageVersion = versionType
  currVersion.value = versionType
  if (versionType === OpenGaussVersionEnum.ENTERPRISE && data.hostUser !== null && data.hostUser !== '') {
    data.logPath = '/opt/' + data.hostUser + '/openGauss/log/omm'
    data.tmpPath = '/opt/' + data.hostUser + '/openGauss/tmp'
    data.omToolsPath = '/opt/' + data.hostUser + '/openGauss/install/om'
    data.corePath = '/opt/' + data.hostUser + '/openGauss/corefile'
  } else if (versionType === OpenGaussVersionEnum.ENTERPRISE && data.hostUser === '') {
    data.logPath = '/opt/openGauss/log/omm'
    data.tmpPath = '/opt/openGauss/tmp'
    data.omToolsPath = '/opt/openGauss/install/om'
    data.corePath = '/opt/openGauss/corefile'
  } else if (versionType === OpenGaussVersionEnum.MINIMAL_LIST) {
    data.envPath = '';
    flagEnvSeqar.value = false
    data.enableGenerateEnvironmentVariableFile = false
  }

  if (data.packageVersionNum && data.packageVersionNum !== '') {
    fetchPackageList()
    initMasterNode()
  }
}

const clusterNodesLimit = ref(0)
const clusterModeChange = (input) => {
  if (input === "SINGLE_NODE") {
    clusterNodesLimit.value = 0
    data.deployType = "SINGLE_NODE"
  } else {
    data.deployType = "CLUSTER"
    if (data.packageVersion === OpenGaussVersionEnum.LITE) {
      clusterNodesLimit.value = 2
    } else if (data.packageVersion === OpenGaussVersionEnum.MINIMAL_LIST) {
      clusterNodesLimit.value = 1
    } else {
      clusterNodesLimit.value = 9
    }
  }
}

const contains = ref(true)
const checkContains = (inputValue: any) => {
  data.packageVersionNum = inputValue
  if (inputValue && inputValue !== '') {
    contains.value = versionNumList.value.some(item => item === versionNumList)
    if (contains.value === false) {
      const params = {
        os: data.os,
        osVersion: data.osVersion,
        cpuArch: data.cpuArch,
        openGaussVersion: data.packageVersion,
        openGaussVersionNum: data.packageVersionNum
      }
      checkVersionNumber(params).then((res: any) => {
        if (res.code !== 200) {
          Message.error({
            content: data.packageVersionNum + t('operation.task.step1.index.packageExistError')
          })
          data.packageVersionNum = ''
        }
      }).catch((error: any) => {
        Message.error({
          content: '版本号' + data.packageVersionNum + t('operation.task.step1.index.packageExistError') + error
        })
        data.packageVersionNum = versionNumList.value[0]
      })
    }
    fetchPackageList()
    initMasterNode()
  }
}

const clusterOrder = ref(0)
const initMasterNode = () => {
  if (data.packageVersionNum === '' || data.hostUser === '') {
    data.hostUser === '' ? Message.error(t('operation.task.step1.index.userNullError')) : ''
  } else {
    let tempCMPath = data.packageVersion === OpenGaussVersionEnum.ENTERPRISE && data.enableCmTool === true ? '/opt/' + data.hostUser + '/openGauss/data/cmserver' : ""
    let tempCMPort = data.packageVersion === OpenGaussVersionEnum.ENTERPRISE && data.enableCmTool === true ? 15300 : 1
    let checkMasterPort = true
    if (tempCMPort === 15300) {
      checkPortExist(data.hostId, tempCMPort, data.clusterId).then((res) => {
        if (Number(res.code) === 200) {
          if (res.data === "NO_USED") {
            checkMasterPort = false
          }
        }
      }).catch((error) => {
        console.error(error)
      }).finally(() => {
        if (!data.clusterId || data.clusterId === '') {
          data.clusterNodes.length = 0
          const newData = {
            "order": 1,
            "clusterNodeId": '',
            "clusterId": data.clusterId ? data.clusterId : '',
            "hostId": data.hostId,
            "hostIp": data.hostIp,
            "hostUser": data.hostUser,
            "hostUserId": data.hostUserId,
            "nodeType": "MASTER",
            "dataPath": data.hostUser ? '/opt/' + data.hostUser + '/openGauss/data/dn' : '/opt/openGauss/data/dn',
            "azOwner": data.packageVersion === OpenGaussVersionEnum.ENTERPRISE ? azOwnerList.value[0] : '',
            "azPriority": data.packageVersion === OpenGaussVersionEnum.ENTERPRISE ? '1' : '',
            "isCMMaster": data.enableCmTool,
            "cmDataPath": tempCMPath,
            "cmPort": tempCMPort,
            "editing": checkMasterPort,
            "displayHostIp": ipPublicPrivate.value
          }
          if (newData.editing) {
            tempEditArr.value[1] = JSON.parse(JSON.stringify(newData))
          }
          data.clusterNodes.push(newData)
          masterHostIp.value = data.hostIp
          clusterOrder.value = 1
        } else {
          data.clusterNodes[0].hostIp = data.hostIp
          data.clusterNodes[0].hostId = data.hostId
          data.clusterNodes[0].hostUser = data.hostUser
          data.clusterNodes[0].hostUserId = data.hostUserId
          masterHostIp.value = data.hostIp
        }
      })
    } else {
      if (!data.clusterId || data.clusterId === '') {
        data.clusterNodes.length = 0
        const newData = {
          "order": 1,
          "clusterNodeId": '',
          "clusterId": data.clusterId ? data.clusterId : '',
          "hostId": data.hostId,
          "hostIp": data.hostIp,
          "hostUser": data.hostUser,
          "hostUserId": data.hostUserId,
          "nodeType": "MASTER",
          "dataPath": data.hostUser ? '/opt/' + data.hostUser + '/openGauss/data/dn' : '/opt/openGauss/data/dn',
          "azOwner": data.packageVersion === OpenGaussVersionEnum.ENTERPRISE ? azOwnerList.value[0] : '',
          "azPriority": data.packageVersion === OpenGaussVersionEnum.ENTERPRISE ? '1' : '',
          "isCMMaster": data.enableCmTool,
          "cmDataPath": tempCMPath,
          "cmPort": tempCMPort,
          "editing": false,
          "displayHostIp": ipPublicPrivate.value
        }
        data.clusterNodes.push(newData)
        masterHostIp.value = data.hostIp
        clusterOrder.value = 1
      } else {
        data.clusterNodes[0].hostIp = data.hostIp
        data.clusterNodes[0].hostId = data.hostId
        data.clusterNodes[0].hostUser = data.hostUser
        data.clusterNodes[0].hostUserId = data.hostUserId
        masterHostIp.value = data.hostIp
      }
    }
  }
}

const packageSerchResult = ref(false)
const fetchPackageList = () => {
  if (data.cpuArch && data.os && data.packageVersion && data.packageVersionNum) {
    getPackageList({
      name: '',
      os: data.os,
      osVersion: data.osVersion,
      cpuArch: data.cpuArch,
      openGaussVersion: data.packageVersion,
      openGaussVersionNum: data.packageVersionNum,
    }).then((res) => {
      if (res.length === 0) {
        packageSerchResult.value = false
      } else {
        packageSerchResult.value = true
        data.packageId = res[0].packageId
        data.packageName = res[0].name
        checkPackageExist()
      }
    }).catch((error) => {
      console.error(error)
    })
  }
}
const downloadNotRef = ref(null)
const timer = ref<any>(null)
const lastProcess = ref(0);
const nextProcess = ref(0);
const uploadName = ref('')
const downloadWs = ref<Socket<any, any> | undefined>()
const processVisible = ref(false)
const percentLoading = ref(false)
const currPercent = ref<number>(0)
const wsBusinessId = ref('')

const webSocketOpen = () => {
  currPercent.value = 0
  const socketKey = new Date().getTime()
  const wsPrefix = window.location.protocol.includes('https') ? 'wss' : 'ws'
  const socketUrl = `${wsPrefix}://${window.location.host}/ws/base-ops/downloadPackage_${socketKey}`
  const websocket = new WebSocket(socketUrl)
  wsBusinessId.value = `downloadPackage_${socketKey}`
  websocket.onopen = function (event) {
    wsBusinessId.value = `downloadPackage_${socketKey}`
  }
  downloadWs.value = websocket
  websocket.onmessage = function (event) {
    processVisible.value = true
    const messageData = event.data
    downloadNotRef.value?.createOrUpdateNotification(wsBusinessId, messageData, uploadName.value)
    if (messageData === 'File download Failed') {
      Message.error({
        content: t('components.Package.5mtcyb0rty52')
      })
      handleOk()
      websocket.close()
      downloadNotRef.value?.closeNotifiCation(wsBusinessId);
      clearInterval(timer.value)
    } else {
      if (!isNaN(Number(messageData))) {
        const percent = Number(messageData)
        currPercent.value = percent
        nextProcess.value = percent
        clearInterval(timer.value)
        timer.value = setInterval(() => {
          if (nextProcess.value === 1) {
            lastProcess.value = 0;
            nextProcess.value = 0;
          } else if (nextProcess.value.toString() === lastProcess.value.toString()) {
            let warnningMsg = setTimeout(() => {
              Message.error(t('operation.task.step1.index.websocketErrorMsg'))
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
          lastProcess.value = 0;
          nextProcess.value = 0;
          percentLoading.value = false
          websocket.close()
          downloadNotRef.value?.closeNotifiCation(wsBusinessId);
          clearInterval(timer.value);
          handleOk()
        }
      } else if (messageData === 'DOWNLOAD_FINISH') {
        percentLoading.value = false
        websocket.close()
        downloadNotRef.value?.closeNotifiCation(wsBusinessId);
        clearInterval(timer.value);
        lastProcess.value = 0;
        nextProcess.value = 0;
        handleOk()
      } else {
        websocket.close()
        downloadNotRef.value?.closeNotifiCation(wsBusinessId);
        clearInterval(timer.value);
        lastProcess.value = 0;
        nextProcess.value = 0;
        console.error('WebSocket error')
      }
    }
  }
  websocket.onerror = function (error) {
    console.error('WebSocket error:', error);
  }
}

const downloadPackage = () => {
  packageOnlineUpdate(data.packageId, wsBusinessId.value).then(() => {
    websocket.onmessage = function (event) {
      const messageData = event.data
      if (!isNaN(Number(messageData))) {
        const percent = Number(messageData)
        currPercent.value = percent
        if (percent === 100) {
          percentLoading.value = false
          downloadWs.value?.destroy()
        }
      }
    }
    simulateDownload()
    processVisible.value = true
    percentLoading.value = true
  }).catch((error) => {
    console.error(error)
  })
}
const simulateDownload = async () => {
  try {
    while (currPercent.value < 100) {
      await new Promise(resolve => setTimeout(resolve, 1000))
    }
    downloadWs.value?.destroy()
    fetchPackageList()
  } catch (error) {
    console.error('Download failed:', error)
  }
}
const handleOk = () => {
  processVisible.value = false
  uploadName.value = ''
  fetchPackageList()
}
const addPackStart =(name) => {
  uploadName.value = name
}
watch(currPercent, (newValue) => {
  if (newValue === 1) {
    processVisible.value = false
    handleOk()
  }
})


const addPackRef = ref<null | InstanceType<typeof AddPack>>(null)
const addPackInstall = (type: string, uploadFlag: number) => {
  let tempuploadFlag: string
  wsBusinessId.value = "";
  if (uploadFlag === 0) {
    webSocketOpen()
  }
  const searchPackageOption = {
    name: '',
    os: data.os,
    cpuArch: data.cpuArch,
    packageVersion: data.packageVersion,
    packageVersionNum: data.packageVersionNum,
    osVersion: data.osVersion
  }
  tempuploadFlag = wsBusinessId.value ? wsBusinessId.value : '1'
  addPackRef.value?.open(type, searchPackageOption, tempuploadFlag)
}

const addPackClose = () => {
  if (wsBusinessId && wsBusinessId.value && wsBusinessId.value != '') {
    downloadWs.value?.destroy()
  }
  fetchPackageList()
}

const addPackSubmit = () => {
  processVisible.value = true
  fetchPackageList()
  if (wsBusinessId && wsBusinessId.value && wsBusinessId.value != '') {
    downloadPackage()
  }
}

const packManageRef = ref<null | InstanceType<typeof PackManage>>(null)
const openPackManage = () => {
  const searchPackageOption = {
    os: data.os,
    osVersion: data.osVersion,
    cpuArch: data.cpuArch,
    packageVersion: data.packageVersion,
    packageVersionNum: data.packageVersionNum,
  }
  packManageRef.value?.open(searchPackageOption, data.packageId)
}
const packManageClose = () => {
  console.log('packManageClose')
}

const syncSubTask = configData => {
  packageIDSelected.value = configData
}

const packManageSubmit = () => {
  data.packageId = toRaw(packageIDSelected.value)[0]
  checkPackageExist()
  getPackageList({
    name: '',
    os: data.os,
    osVersion: data.osVersion,
    cpuArch: data.cpuArch,
    openGaussVersion: data.packageVersion,
    openGaussVersionNum: data.packageVersionNum,
  }).then((res) => {
    res.forEach((item) => {
      if (item.packageId === data.packageId) {
        data.packageName = item.name
      }
    })
  }).catch((error) => {
    console.error(error)
  })
}
const checkPackageExist = async () => {
  try {
    const res = await checkPkg(data.packageId)
    if (!res.data) {
      Message.error(t('operation.task.step1.index.packageCheckError'))
    }
  } catch (error) {
    Message.error($t('operation.task.step1.index.packageCheckError'))
  }
}

const checkflagCM = (input) => {
  flagCM.value = input
  data.enableCmTool = input
}
const checkflagEnvSeqar = (input) => {
  flagEnvSeqar.value = input
  data.enableGenerateEnvironmentVariableFile = input
  data.envPath = generateEnvironmentPath()
}

const generateEnvironmentPath = () => {
  let temppath = '/home/' + data.hostUser
  temppath = temppath + '/cluster_' + getCurrentDateTime() + '.bashrc'
  return temppath
}

const getCurrentDateTime = () => {
  const now = new Date()
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  return `${year}${month}${day}_${hours}${minutes}${seconds}`
}

const clusterNodes = reactive([])

const editFlagGroup = ref({})
const masterHostIp = ref('')
const addColumn = () => {
  clusterOrder.value = clusterOrder.value + 1
  let tempCMPath = data.packageVersion === OpenGaussVersionEnum.ENTERPRISE && data.enableCmTool === true ? '/opt/' + data.hostUser + '/openGauss/data/cmserver' : ""
  let tempCMPort = data.packageVersion === OpenGaussVersionEnum.ENTERPRISE && data.enableCmTool === true ? data.clusterNodes[0].cmPort : 1
  const newData = {
    "order": clusterOrder.value,
    "clusterNodeId": '',
    "clusterId": data.clusterId ? data.clusterId : '',
    "hostId": '',
    "hostUser": data.hostUser,
    "hostUserId": data.hostUserId,
    "hostIp": '',
    "nodeType": "SLAVE",
    "dataPath": data.hostUser ? '/opt/' + data.hostUser + '/openGauss/data/dn' : '/opt/openGauss/data/dn',
    "azOwner": data.packageVersion === OpenGaussVersionEnum.ENTERPRISE ? azOwnerList.value[0] : '',
    "azPriority": data.packageVersion === OpenGaussVersionEnum.ENTERPRISE ? '1' : '',
    "isCMMaster": false,
    "cmDataPath": tempCMPath,
    "cmPort": tempCMPort,
    "editing": data.packageVersion !== OpenGaussVersionEnum.MINIMAL_LIST,
    "displayHostIp": ''
  }
  if (newData.editing) {
    tempEditArr.value[clusterOrder.value] = JSON.parse(JSON.stringify(newData))
    editFlagGroup.value[clusterOrder.value] = {
      hostIp: false,
      cmDataPath: true,
      dataPath: true,
      CMMaster: false,
      CmPort: false,
      order: clusterOrder.value
    }
  }
  data.clusterNodes.push(newData)
}

const tempEditArr = ref({})
const editCluster = (record: any) => {
  data.clusterNodes.forEach((item) => {
    if (item.order === record.order) {
      item.editing = true
      tempEditArr.value[record.order] = JSON.parse(JSON.stringify(item))
      editFlagGroup.value[record.order] = {
        hostIp: false,
        cmDataPath: true,
        dataPath: true,
        CMMaster: false,
        CmPort: false,
        order: record.order
      }
    }
  })
}

const checkSameUser = (inputValue: string, order: number) => {
  let editFlag = editFlagGroup.value[order]
  let tempFlag = false
  editFlag.hostIp = false
  let tempEditCluster = tempEditArr.value[order]
  let tempHostIp = inputValue.split('(').filter(part => part !== '')
  tempEditCluster.hostIp = tempHostIp.length > 0 ? tempHostIp[0] : ''
  tempEditCluster.displayHostIp = inputValue
  let tempHostId = hostIpId.get(tempEditCluster.hostIp)
  if (!inputValue || inputValue === '') {
    Message.error(t('operation.task.step1.index.ipNullError'))
  } else {
    if (tempEditCluster.hostIp === masterHostIp.value && tempEditCluster.nodeType !== "MASTER") {
      Message.error(t('operation.task.step1.index.ipSameError'))
    } else {
      getHostUser(tempHostId).then((res) => {
        res.data.forEach(item => {
          if (item.username === data.hostUser) {
            tempFlag = true
            tempEditCluster.hostId = item.hostId
            editFlag.hostIp = true
            tempEditCluster.hostUserId = item.hostUserId
          }
        })
        if (!tempFlag) {
          Message.error(t('operation.task.step1.index.ipUserError'))
          tempEditCluster.hostId = item.hostId
        }
        tempEditArr.value[order] = tempEditCluster
      }).catch((error) => {
        console.error(error)
      })
    }
  }
}

const checkClusterPathDB = (inputValue: string, order: number) => {
  let editFlag = editFlagGroup.value[order]
  tempEditArr.value[order].dataPath = inputValue
  editFlag.dataPath = checkDataPath(inputValue)
}

const checkCMMaster = (inputValue: string, order: number) => {
  tempEditArr.value[order].isCMMaster = inputValue === '是'
}

const checkClusterPathCM = (inputValue: string, order: number) => {
  let editFlag = editFlagGroup.value[order]
  tempEditArr.value[order].cmDataPath = inputValue
  if (checkDataPath(inputValue)) {
    editFlag.cmDataPath = true
  } else {
    editFlag.cmDataPath = false
  }
}
const checkAzPriority = (inputValue: string, order: number) => {
  tempEditArr.value[order].azPriority = inputValue
}

const changeAZOwner = (inputValue: string, order: number) => {
  tempEditArr.value[order].azOwner = inputValue
}

const checkCmPort = (inputValue: string, order: number) => {
  let editFlag = editFlagGroup.value[order]
  let tempEditCluster = tempEditArr.value[order]
  tempEditArr.value[order].cmPort = inputValue
  if (tempEditCluster.hostIp !== '') {
    tempEditCluster.hostId = hostIpId.get(tempEditCluster.hostIp)
    checkPortExist(tempEditCluster.hostId, tempEditCluster.cmPort, data.clusterId).then((res) => {
      if (res.data === "NO_USED") {
        editFlag.CmPort = true
      } else {
        editFlag.CmPort = false
        Message.error(getIpById(tempEditCluster.hostId) + t('operation.task.step1.index.ipNoticeError') + tempEditCluster.cmPort + t('operation.task.step1.index.portOccupied'))
      }
    }).catch((error) => {
      editFlag.CmPort = false
      console.error(error)
    })
  }
}

const deleteRows = (record: any) => {
  if (record.clusterId && record.clusterId !== '' && record.clusterNodeId && record.clusterNodeId !== '') {
    deleteClustertaskNode(record.clusterId, record.clusterNodeId, {
      "clusterNodeId": record.clusterNodeId,
      "clusterId": record.clusterId,
      "hostId": record.hostId,
      "hostUserId": record.hostUserId,
      "nodeType": record.nodeType,
      "dataPath": record.dataPath,
      "azOwner": record.azOwner,
      "azPriority": record.azPriority,
      "isCMMaster": record.isCMMaster,
      "cmDataPath": record.cmDataPath,
      "cmPort": record.cmPort
    }).then((res) => {
      if (Number(res.code) !== 200) {
        Message.error(res.data)
        record.editing = true
      } else {
        data.clusterNodes = data.clusterNodes.filter(item => item.order !== record.order)
      }
    }).catch((error) => {
      console.error(error)
    })
  } else {
    data.clusterNodes = data.clusterNodes.filter(item => item.order !== record.order)
  }
}

const getIpById = (inputvalue: string) => {
  for (let [key, formValue] of hostIpId.entries()) {
    if (formValue === inputvalue) {
      return key
    }
  }
  return inputvalue
};

const saveCluster = async (record: any) => {
  let editFlag = editFlagGroup.value[record.order]
  let tempEditCluster = tempEditArr.value[record.order]
  if (record.order === 1) {
    editFlag.hostIp = true
  }
  let checkHostIpPromise = new Promise((resolve) => {
    if (record.order !== 1) {
      if (!tempEditCluster.hostIp || tempEditCluster.hostIp === '') {
        Message.error(t('operation.task.step1.index.ipNullError'))
      } else {
        if (tempEditCluster.hostIp === masterHostIp.value && tempEditCluster.nodeType !== "MASTER") {
          Message.error(t('operation.task.step1.index.ipSameError'))
        } else {
          let tempHostId = hostIpId.get(tempEditCluster.hostIp)
          let tempFlag = false
          getHostUser(tempHostId).then((res) => {
            res.data.forEach(item => {
              if (item.username === data.hostUser) {
                tempFlag = true
                tempEditCluster.hostIp = getIpById(item.hostId)
                tempEditCluster.hostId = item.hostId
                tempEditCluster.hostUserId = item.hostUserId
                editFlag.hostIp = true
              }
            })
            if (!tempFlag) {
              Message.error(t('operation.task.step1.index.ipUserError'))
            }
          }).catch((error) => {
            console.error(error)
          }).finally(() => {
            resolve(editFlag.hostIp)
          })
        }
      }
    } else {
      resolve(editFlag.hostIp)
    }
  })
  let checkCmPortPromise = new Promise((resolve) => {
    if (!editFlag.CmPort && tempEditCluster.cmPort && tempEditCluster.hostIp && data.enableCmTool) {
      checkPortExist(hostIpId.get(tempEditCluster.hostIp), tempEditCluster.cmPort, data.clusterId).then((res) => {
        if (res.data === "NO_USED") {
          editFlag.CmPort = true
        } else {
          editFlag.CmPort = false
          Message.error(getIpById(tempEditCluster.hostId) + t('operation.task.step1.index.ipNoticeError') + tempEditCluster.cmPort + t('operation.task.step1.index.portOccupied'))
        }
      }).catch((error) => {
        editFlag.CmPort = false
        console.error(error)
      }).finally(() => {
        resolve(editFlag.CmPort)
      })
      checkCmPort(tempEditCluster.cmPort, record.order)
    } else {
      editFlag.CmPort = true
      resolve(editFlag.CmPort)
    }
  })
  let checkCmMasterPromise = new Promise((resolve) => {
    if (!tempEditCluster.isCMMaster && data.enableCmTool) {
      editFlag.CMMaster = false
      data.clusterNodes.forEach((item) => {
        if (item.isCMMaster && item.order !== record.order) {
          editFlag.CMMaster = true
        }
      })
      if (!editFlag.CMMaster && !data.enableCmTool) {
        Message.error(t('operation.task.step1.index.cmMasterError'))
      }
    } else {
      editFlag.CMMaster = true
    }
    resolve(editFlag.CMMaster)
  })
  Promise.all([checkHostIpPromise, checkCmPortPromise, checkCmMasterPromise]).then(() => {
    if (data.enableCmTool) {
      if (editFlag.hostIp && editFlag.dataPath && editFlag.cmDataPath && editFlag.CMMaster && editFlag.CmPort) {
        data.clusterNodes.forEach((item) => {
          //a record without hostId should not be checked
          if (item.order !== record.order && item.hostId) {
            let tempPort = tempEditCluster.cmPort
            checkPortExist(item.hostId, tempPort, data.clusterId).then((res) => {
              if (res.data !== "NO_USED") {
                Message.error(getIpById(item.hostIp) + t('operation.task.step1.index.ipNoticeError') + tempPort + t('operation.task.step1.index.portOccupied'))
                editFlag.CmPort = false
              }
            }).catch((error) => {
              editFlag.CmPort = false
              console.error(error)
              editFlag.CmPort = false
            })
          }
        })
      }
    }
    if (editFlag.hostIp && editFlag.dataPath && editFlag.cmDataPath && editFlag.CMMaster && editFlag.CmPort) {
      data.clusterNodes.forEach((item) => {
        if (item.order === record.order) {
          item.editing = false
          item.azOwner = tempEditCluster.azOwner
          item.azPriority = tempEditCluster.azPriority
          item.cmPort = tempEditCluster.cmPort
          item.cmDataPath = tempEditCluster.cmDataPath
          item.dataPath = tempEditCluster.dataPath
          item.hostIp = tempEditCluster.hostIp
          item.hostId = tempEditCluster.hostId
          item.hostUserId = tempEditCluster.hostUserId
          item.isCMMaster = tempEditCluster.isCMMaster
          item.displayHostIp = tempEditCluster.displayHostIp
          if (item.order === 1) {
            masterHostIp.value = tempEditCluster.hostIp
          }
        } else {
          item.cmPort = tempEditCluster.cmPort
          if (tempEditCluster.isCMMaster === true) {
            item.isCMMaster = false
          }
        }
      })
      delete tempEditArr.value[record.order]
      delete editFlagGroup.value[record.order]

      if (data.clusterId !== '' && data.clusterId) {
        if (record.clusterNodeId && record.clusterNodeId !== '') {
          updateClustertaskNode({
            "clusterNodeId": record.clusterNodeId,
            "clusterId": data.clusterId,
            "hostId": (!record.hostId && record.nodeType === 'MASTER') ? data.hostId : record.hostId,
            "hostUserId": (!record.hostUserId && record.nodeType === 'MASTER') ? data.hostUserId : record.hostUserId,
            "nodeType": record.nodeType,
            "dataPath": !record.dataPath ? "/opt/" + data.hostUser + "/openGauss/data/dn" : record.dataPath,
            "azOwner": record.azOwner,
            "azPriority": record.azPriority,
            "isCMMaster": record.isCMMaster,
            "cmDataPath": record.cmDataPath,
            "cmPort": record.cmPort
          }).then((res) => {
            if (Number(res.code) !== 200) {
              Message.error(res.data)
              record.editing = true
            } else if (record.order === 1) {
              masterHostIp.value = data.hostIp
              record.hostIp = data.hostIp
              record.hostUser = data.hostUser
              record.hostId = data.hostId
              record.hostUserId = data.hostUserId
            } else {
              record.hostUser = data.hostUser
            }
            if (!record.dataPath) {
              record.dataPath = "/opt/" + data.hostUser + "/openGauss/data/dn"
            }
          }).catch((error) => {
            console.error(error)
          })
        } else {
          createClustertaskNode({
            "clusterNodeId": '',
            "clusterId": data.clusterId,
            "hostId": (!record.hostId && record.nodeType === 'MASTER') ? data.hostId : record.hostId,
            "hostUserId": (!record.hostUserId && record.nodeType === 'MASTER') ? data.hostUserId : record.hostUserId,
            "nodeType": record.nodeType,
            "dataPath": record.dataPath,
            "azOwner": record.azOwner,
            "azPriority": record.azPriority,
            "isCMMaster": record.isCMMaster,
            "cmDataPath": record.cmDataPath,
            "cmPort": record.cmPort
          }).then((res) => {
            if (Number(res.code) !== 200) {
              console.error(res)
              Message.error(res.data)
              record.editing = true
            }
          }).catch((error) => {
            console.error(error)
          })
        }
      }
    } else {
      Message.error(t('operation.task.step1.index.saveError'))
    }
  }).catch((error) => {
    console.error((error))
  })
}

const cancelEdit = (record) => {
  data.clusterNodes.forEach((item) => {
    if (item.order === record.order) {
      item.editing = false
    }
  })
  data.clusterNodes = data.clusterNodes.filter(item => !(item.hostIp === "" && item.order === record.order))
  delete tempEditArr.value[record.order]
  delete editFlagGroup.value[record.order]
}

const checkDataPath = (dataPath: string) => {
  const regex = /^\/(?:[^\u4E00-\u9FFF\/\n]+\/)*[^\u4E00-\u9FFF\/\n]+$/
  return regex.test(dataPath)
}

const getClusterName = (inputValue: any) => {
  checkClusternameExist(inputValue, data.clusterId).then((res) => {
    if (res.data === false) {
      data.clusterName = inputValue
    } else {
      Message.error(t('operation.task.step1.index.sameNameError'))
    }
  }).catch((error) => {
    console.error(error)
  })
}
const getInstallPath = (inputValue: any) => {
  data.installPath = inputValue
  let tempInstallPath = inputValue.split('/').filter(part => part !== '')
  let joinedString: string
  joinedString = '/' + tempInstallPath[0] + '/'
  if (data.packageVersion === OpenGaussVersionEnum.ENTERPRISE) {
    data.logPath = joinedString + data.hostUser + '/openGauss/log/omm'
    data.tmpPath = joinedString + data.hostUser + '/openGauss/tmp'
    data.omToolsPath = joinedString + data.hostUser + '/openGauss/install/om'
  }
}

const getPort = (inputvalue: number) => {
  checkPortExist(data.hostId, inputvalue, data.clusterId).then((res) => {
    if (res.data === "NO_USED") {
      data.port = inputvalue
    } else {
      Message.error(data.hostIp + t('operation.task.step1.index.ipNoticeError') + inputvalue + t('operation.task.step1.index.portOccupied'))
    }
  }).catch((error) => {
    console.error(error)
  })
}

const validateHostUserCheck = (inputvalue: string, callback: any) => {
  return new Promise((resolve, reject) => {
    if (data.clusterId === '') {
      checkClusterExist(data.hostIp, inputvalue).then((res) => {
        if (res.data === false) {
          resolve(false)
          callback('该ip该用户已安装集群。请重新选择')
        } else {
          data.hostUser = inputvalue
          data.hostUserId = hostUserId.get(inputvalue)
          resolve(true)
        }
      }).catch((error) => {
        console.error(error)
      })
    }
  })
}

const validatePortCheck = (inputvalue: string, callback: any) => {
  return new Promise((resolve, reject) => {
    if (data.hostId === '' || inputvalue === '') {
      resolve(new Error('NO HostIp'))
    } else {
      checkPortExist(data.hostId, inputvalue, data.clusterId).then((res) => {
        if (res.data === "NO_USED") {
          data.port = inputvalue
          resolve(true)
        } else if (data.clusterId === '') {
          resolve(new Error('Port already in use'))
          callback(data.hostIp + 'IP 下' + inputvalue + '端口被占用，无法保存')
        }
      }).catch((error) => {
        console.error(error)
      })
    }
  })
}

const validateClusternameExist = (inputvalue: string, callback: any) => {
  return new Promise((resolve, reject) => {
    checkClusternameExist(inputvalue, data.clusterId).then((res) => {
      if (res.data === false) {
        data.clusterName = inputvalue
        resolve(true)
      } else if (data.clusterId === '') {
        resolve(false)
        callback('该标识已经存在，请重新命名')
      }
    }).catch((error) => {
      console.error(error)
    })
  })
}

const serverRules = reactive(
  {
    hostIp: [
      { required: true, 'validate-trigger': 'blur', message: ('主机ip不可为空') }
    ],
    hostUser: [
      { required: true, 'validate-trigger': 'blur', message: ('安装用户不可为空') },
      { validator: validateHostUserCheck }
    ],
    packageVersionNum: [
      { required: true, 'validate-trigger': 'blur', message: ('版本号不可为空') }
    ],
    clusterName: [
      { required: true, 'validate-trigger': 'blur', message: ('集群标识不可为空') },
      { validator: validateClusternameExist }
    ],
    databasePassword: [
      { required: true, 'validate-trigger': 'blur', message: ('数据库密码不可为空') },
      {
        validator: (value, callback) => {
          return new Promise(resolve => {
            const minLength = 8;
            const hasUpperCase = /[A-Z]/.test(value);
            const hasLowerCase = /[a-z]/.test(value);
            const hasDigit = /\d/.test(value);
            const hasSpecialChar = /[^A-Za-z0-9]/.test(value);

            const categories = [hasUpperCase, hasLowerCase, hasDigit, hasSpecialChar].filter(Boolean).length;

            if (value.length < minLength) {
              callback('请输入至少八个字符长度的密码')
              resolve(false)
            }
            if (categories < 4) {
              callback('请输入至少包含大写字母(A-Z)、小写字母(a-z)、数字(0-9)、非字母数字字符的密码')
              resolve(false)
            }
            resolve(true)
          })
        }
      }
    ],
    port: [
      { required: true, 'validate-trigger': 'blur', message: ('端口号不可为空') },
      { validator: validatePortCheck }
    ],
    installPath: [
      { required: true, 'validate-trigger': 'blur', message: ('安装目录不可为空') },
      {
        validator: (value: any, callback: any) => {
          return new Promise(resolve => {
            const regex = /^\/(?:[^\u4E00-\u9FFF\/\n]+\/)*[^\u4E00-\u9FFF\/\n]+$/
            if (!value.trim()) {
              callback('不可以填写空格')
              resolve(false)
            } else if (!regex.test(value)) {
              callback('路径不符合规则')
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    installPackagePath: [
      { required: true, 'validate-trigger': 'blur', message: ('installPackagePath不可为空') },
      {
        validator: (value: any, callback: any) => {
          return new Promise(resolve => {
            const regex = /^\/(?:[^\u4E00-\u9FFF\/\n]+\/)*[^\u4E00-\u9FFF\/\n]+$/
            if (!value.trim()) {
              callback('不可以填写空格')
              resolve(false)
            } else if (!regex.test(value)) {
              callback('路径不符合规则')
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    logPath: [
      { required: true, 'validate-trigger': 'blur', message: ('日志目录不可为空') },
      {
        validator: (value: any, callback: any) => {
          return new Promise(resolve => {
            const regex = /^\/(?:[^\u4E00-\u9FFF\/\n]+\/)*[^\u4E00-\u9FFF\/\n]+$/
            if (!value.trim()) {
              callback('不可以填写空格')
              resolve(false)
            } else if (!regex.test(value)) {
              callback('路径不符合规则')
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    tmpPath: [
      { required: true, 'validate-trigger': 'blur', message: ('临时目录不可为空') },
      {
        validator: (value: any, callback: any) => {
          return new Promise(resolve => {
            const regex = /^\/(?:[^\u4E00-\u9FFF\/\n]+\/)*[^\u4E00-\u9FFF\/\n]+$/
            if (!value.trim()) {
              callback('不可以填写空格')
              resolve(false)
            } else if (!regex.test(value)) {
              callback('路径不符合规则')
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    omToolsPath: [
      { required: true, 'validate-trigger': 'blur', message: ('omToolsPath不可为空') },
      {
        validator: (value: any, callback: any) => {
          return new Promise(resolve => {
            const regex = /^\/(?:[^\u4E00-\u9FFF\/\n]+\/)*[^\u4E00-\u9FFF\/\n]+$/
            if (!value.trim()) {
              callback('不可以填写空格')
              resolve(false)
            } else if (!regex.test(value)) {
              callback('路径不符合规则')
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    corePath: [
      { required: true, 'validate-trigger': 'blur', message: ('corePath不可为空') },
      {
        validator: (value: any, callback: any) => {
          return new Promise(resolve => {
            const regex = /^\/(?:[^\u4E00-\u9FFF\/\n]+\/)*[^\u4E00-\u9FFF\/\n]+$/
            if (!value.trim()) {
              callback('不可以填写空格')
              resolve(false)
            } else if (!regex.test(value)) {
              callback('路径不符合规则')
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    envPath: [{ required: true, 'validate-trigger': 'blur', message: ('envPath不可为空') },
    { pattern: /^\/(?:[^\u4E00-\u9FFF\/\n]+\/)*[^\u4E00-\u9FFF\/\n]+$/, message: '安装目录格式不正确', trigger: ['blur', 'change'] }
    ],

  }
)

const serverFormRef = ref(null)
const packageFormRef = ref(null)
const clusterFormRef = ref(null)

const serverValidateField = async (fieldName: string) => {
  try {
    await serverFormRef.value.validateField(fieldName)
  } catch (err) {
    console.error(`${fieldName} validation failed:`, err)
  }
}
const packageValidateField = async (fieldName: string) => {
  try {
    await packageFormRef.value.validateField(fieldName)
  } catch (err) {
    console.error(`${fieldName} validation failed:`, err)
  }
}
const clusterValidateField = async (fieldName: string) => {
  try {
    await clusterFormRef.value.validateField(fieldName)
  } catch (err) {
    console.error(`${fieldName} validation failed:`, err)
  }
}
const validateAllFields = async () => {
  try {
    const fields = ['hostUser', 'hostIp']
    for (const field of fields) {
      await serverValidateField(field)
    }
    const fields2 = ['packageName', 'packageVersionNum']
    for (const field of fields2) {
      await packageValidateField(field)
    }
    const fields3 = ['clusterName', 'installPath', 'omToolsPath', 'tmpPath', 'logPath', 'installPackagePath', 'envPath', 'port', 'databasePassword']
    for (const field of fields3) {
      await clusterValidateField(field)
    }
    return true
  } catch {
    return false
  }
}
defineExpose({ validateAllFields })

const emits = defineEmits(['syncConfig'])
watch(data, (val) => {
  emits('syncConfig', val)
}, { deep: true })


// init
const azOwnerList = ref([])
const fetchAzOwner = () => {
  fetchAZList().then((res) => {
    azOwnerList.value = []
    res.data.forEach(item => { azOwnerList.value.push(item.name) })
  }).catch((error) => {
    console.error(error)
  })
}

const hostIpList = ref([])
const hostIpId = new FormData
const fetchHostIp = (os?: string, osVersion?: string, cpuArch?: string) => {
  const param = {
    os: os ? os : '',
    osVersion: osVersion ? osVersion : '',
    cpuArch: cpuArch ? cpuArch : ''
  }
  getHostIp(param).then((res) => {
    if (Number(res.code) === 200) {
      hostIpList.value = []
      res.data.forEach((item: any) => { hostIpList.value.push(item.displayIp) })
      res.data.forEach((item: any) => { hostIpId.append(item.publicIp, item.hostId) })
    } else {
      console.error({
        content: '获取ip失败'
      })
    }
  }).catch((error) => {
    console.error(error)
  })
}

const versionNumList = ref([])
const fetchVersionNum = () => {
  getVersionNum().then((res: KeyValue) => {
    versionNumList.value = []
    if (Number(res.code) === 200) {
      res.data.forEach((item: any) => { versionNumList.value.push(item) })
    } else {
      console.error({
        content: '获取版本号失败'
      })
    }
  }).catch((error: any) => {
    console.error(error)
  })
}

const optionsCMMaster = ref([])
const props = defineProps({
  clusterId: Object,
  createClusterId: Object,
  clusterNodeList: Object
})

const init = () => {
  fetchVersionNum()
  fetchHostIp()
  fetchAzOwner()
  optionsCMMaster.value = []
  optionsCMMaster.value.push('是')
  optionsCMMaster.value.push('否')
  if (props.clusterId && props.clusterId != '') {
    data.clusterNodes = []
    batchClusterNodes(props.clusterId).then(async (res) => {
      if (res.code === 200) {
        clusterOrder.value = 0
        res.data.clusterNodes.forEach((item) => {
          clusterOrder.value = clusterOrder.value + 1
          const newData = {
            "clusterId": item.clusterId,
            "clusterNodeId": item.clusterNodeId,
            "order": clusterOrder.value,
            "hostId": item.hostId,
            "hostIp": item.hostIp,
            "hostUserId": item.hostUserId,
            "hostUser": res.data.hostUsername,
            "nodeType": item.nodeType,
            "dataPath": item.dataPath,
            "azOwner": item.azOwner,
            "azPriority": item.azPriority,
            "isCMMaster": item.isCmMaster,
            "cmDataPath": item.cmDataPath,
            "cmPort": item.cmPort,
            "editing": (!item.hostId && res.data.version !== OpenGaussVersionEnum.MINIMAL_LIST),
            "displayHostIp": item.displayHostIp
          }
          const containsElement = computed(() => {
            return data.clusterNodes.some(obj => obj.clusterNodeId === newData.clusterNodeId)
          })
          if (!containsElement.value) {
            if (newData.nodeType === "MASTER") {
              data.clusterNodes.unshift(newData)
            } else {
              data.clusterNodes.push(newData)
            }
          }
        })
        Message.warning(t('operation.task.step1.index.passwdWarning'))
        currVersion.value = res.data.version
        data.clusterId = props.clusterId
        data.os = res.data.os
        data.cpuArch = res.data.cpuArch
        data.packageVersion = res.data.version
        data.packageVersionNum = res.data.versionNum
        data.packageName = res.data.packageName
        data.packageId = res.data.packageId
        data.clusterName = res.data.clusterName
        data.databasePassword = ''
        data.port = Number(res.data.databasePort)
        data.installPackagePath = res.data.installPackagePath
        data.installPath = res.data.installPath
        data.logPath = res.data.logPath
        data.tmpPath = res.data.tmpPath
        data.omToolsPath = res.data.omToolsPath
        data.corePath = res.data.corePath
        data.envPath = res.data.envPath
        data.enableCmTool = res.data.enableCmTool
        data.enableGenerateEnvironmentVariableFile = res.data.enableGenerateEnvironmentVariableFile
        data.xmlConfigPath = res.data.xmlConfigPath
        data.deployType = res.data.deployType
        data.hostIp = res.data.hostIp
        data.hostUser = res.data.hostUsername
        data.hostUserId = res.data.hostUserId
        data.hostId = hostIpId.get(data.hostIp)
        masterHostIp.value = data.hostIp // for validate
        flagCM.value = data.enableCmTool
        flagEnvSeqar.value = data.enableGenerateEnvironmentVariableFile
        packageSerchResult.value = true
        ipPublicPrivate.value = res.data.displayHostIp
        if (data.hostId && data.hostId !== null) {
          getHostUser(data.hostId).then((res) => {
            hostUserList.value = []
            res.data.forEach(item => {
              if (item.sudo === false) {
                hostUserList.value.push(item.username)
                hostUserId.set(item.username, item.hostUserId)
              }
            })
          }).catch((error) => {
            console.log(error)
          }).finally(() => {
            fetchOsSystem(false)
          })
        }
        if (data.deployType) {
          clusterModeChange(data.deployType)
        }
      }
    }).catch((error) => {
      console.error(error)
    })
  } else {
    currVersion.value = OpenGaussVersionEnum.MINIMAL_LIST
    data.port = 5432
    data.installPath = ''
    data.installPackagePath = ''
    data.deployType = "SINGLE_NODE"
    data.os = ''
    data.cpuArch = ''
    data.packageVersion = OpenGaussVersionEnum.MINIMAL_LIST
    data.packageVersionNum = ''
    data.packageName = ''
    data.packageId = ''
    data.clusterId = ''
    data.clusterName = ''
    data.databasePassword = ''
    data.logPath = ''
    data.tmpPath = ''
    data.omToolsPath = ''
    data.corePath = ''
    data.envPath = ''
    data.enableCmTool = false
    data.enableGenerateEnvironmentVariableFile = false
    data.xmlConfigPath = ''
    data.hostIp = ''
    data.hostId = ''
    data.hostUser = ''
    data.hostUserId = ''
    data.clusterNodes = []
    ipPublicPrivate.value = ''
    masterHostIp.value = ''
    flagCM.value = false
    flagEnvSeqar.value = false
    packageSerchResult.value = false
  }
}

const tempClusterId = ref('')
const route = useRoute();

watch(() => route.fullPath, () => {
  init()
})
onMounted(() => {
  init()
})
watch(() => props.clusterId, (newVal) => {
  init()
}, { immediate: true })
watch(() => props.createClusterId, (newVal) => {
  data.clusterId = newVal
}, { immediate: true })
watch(() => props.clusterNodeList, (newVal) => {
  data.clusterNodes.forEach(item => {
    if (newVal.has(item.hostId)) {
      item.clusterNodeId = newVal.get(item.hostId)
    }
  })
}, { deep: true, immediate: true })
const changeHostipFlag = computed(() => {
  return data.hostUser !== '' && data.hostIp !== '' && data.clusterNodes.length > 1 && data.packageVersion === OpenGaussVersionEnum.ENTERPRISE
})

</script>

<style scoped lang="scss">
.container {
  width: 100%;
  height: 100%;
  margin: 0 auto;
  padding: 20px;
}

.main {
  display: flex;

  .left {
    flex: 1;

    .module {
      height: auto;
      margin-bottom: 20px;
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      justify-content: flex-start;
      padding: 20px;

      h2 {
        color: var(--color-neutral-10);
      }

      p {
        color: var(--color-neutral-10);
      }

      .item {
        width: 100%;
        margin-bottom: 20px;
      }

      .h2 {
        font-size: medium;
        font-weight: bold;
      }

      .select {
        margin-left: 10px;
        min-width: 150px;
      }

      .squarebutton {
        width: 200px;
        height: 200px;
        position: relative;
        overflow: hidden;
        text-align: left;
        padding: 20px;
        display: inline-block;
        flex-direction: column;
        align-items: center;
        justify-content: flex-start;
        word-wrap: break-word;
        word-break: break-all;
      }

      .squarebutton:hover {
        background-color: lightgray;
      }

      .squarebutton .label-color {
        word-wrap: break-word;
        word-break: break-word;
      }

      .backselect {
        background-color: lightgray;
      }

      .icon-size {
        width: 30px;
        height: 30px;
      }
    }
  }

  .right {
    width: 200px;
    display: flex;
    flex-direction: column;
    padding-left: 40px;
  }
}

button {
  margin-bottom: 10px;
}

.steps-content {
  margin-top: 16px;
  border: 1px dashed #e9e9e9;
  border-radius: 6px;
  background-color: #fafafa;
  min-height: 200px;
  text-align: center;
  padding-top: 80px;
}

.steps-action {
  margin-top: 24px;
}

[data-theme='dark'] .steps-content {
  background-color: #2f2f2f;
  border: 1px dashed #404040;
}

:deep(.arco-btn-secondary.arco-btn-disabled, .arco-btn-secondary[type="button"].arco-btn-disabled, .arco-btn-secondary[type="submit"].arco-btn-disabled) {
  color: var(--color-text-2);
}

:deep(.arco-btn-primary, .arco-btn-primary[type="button"], .arco-btn-primary[type="submit"]:hover) {
  background-color: rgb(var(--primary-5));
}

.center-item-active {
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  border-color: rgb(var(--primary-6)) !important;
  box-shadow: 0px 4px 20px 0px rgba(var(--primary-6), 0.4);
}

:deep(.arco-col-5) {
  flex: none;
  width: 12%;
}

:deep(.arco-radio-button.arco-radio-disabled) {
  color: var(--color-text-1);
}

.form-helper-text {
  color: #888;
  /* 灰色 */
  font-size: 12px;
}
</style>
