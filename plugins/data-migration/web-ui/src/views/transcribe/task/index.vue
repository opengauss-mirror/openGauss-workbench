<template>
  <div class="common-layout">
    <div class="background-main">
      <el-container>
        <el-page-header :icon="le" class="backgroundform" @click="backToIndex">
          <template #content>
            <span class="text-large font-600 mr-3"> {{ $t('transcribe.index.createtask') }} </span>
          </template>
        </el-page-header>
        <el-header style="height: 0"/>
        <el-container>
          <el-container>
            <el-main>
              <el-card class="backgroundcard">
                <el-form :model="taskBasicInfo" :rules="taskBasicRules" validateTrigger="onBlur" labelAlign="left"
                         label-width="300px" ref="taskNameFormRef">
                  <h3>{{ $t('transcribe.create.taskconfig') }}</h3>
                  <el-form-item :label="t('transcribe.index.taskname')" prop="taskName">
                    <el-input v-model="taskBasicInfo.taskName" style="width: 30vw"></el-input>
                  </el-form-item>
                  <el-form-item :label="t('transcribe.create.version')" prop="taskVersion">
                    <el-select v-model="taskBasicInfo.taskVersion" :loading="loadingSource" style="width: 30vw"
                               :teleported="false">
                      <el-option v-for="item in taskVersionOptions" :key="item.key" :label="item.value"
                                 :value="item.value"/>
                    </el-select>
                  </el-form-item>
                </el-form>
              </el-card>
              <el-card class="backgroundcard">
                <el-form :model="taskBasicInfo" :rules="taskBasicRules" validateTrigger="onBlur" labelAlign="left"
                         label-width="300px" ref="taskDataFormRef">
                  <h3>{{ $t('transcribe.create.dataconfig') }}</h3>
                  <h4>{{ $t('transcribe.create.sourceconfig') }}</h4>
                  <el-form-item :label="t('transcribe.create.dbtype')" label-position="right" prop="sourceDbType">
                    <el-radio-group v-model="taskBasicInfo.sourceDbType">
                      <el-radio-button value="MySQL">MySQL</el-radio-button>
                      <el-radio-button value="PostgreSQL" disabled>PostgreSQL</el-radio-button>
                      <el-radio-button value="openGauss" disabled>openGauss</el-radio-button>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item :label="t('transcribe.create.tasktype')" label-position="right" prop="taskType">
                    <el-radio-group v-model="taskBasicInfo.taskType" @change="changeReplayVersion">
                      <el-radio-button value="transcribe">{{ $t('transcribe.index.transcribe') }}</el-radio-button>
                      <el-radio-button value="replay">{{ $t('transcribe.index.replay') }}</el-radio-button>
                      <el-radio-button value="transcribe_replay">{{
                          $t('transcribe.index.transcribeandreplay')
                        }}
                      </el-radio-button>
                    </el-radio-group>
                  </el-form-item>
                  <el-form-item v-if="replayTaskVersion" :label="t('transcribe.create.recordtaskname')"
                                prop="replayTaskId">
                    <el-select v-model="taskBasicInfo.replayTaskId" :placeholder="t('transcribe.create.selectaskname')"
                               :teleported="false" filterable style="width: 30vw" @change="backTaskBasicinfo">
                      <el-option v-for="item in replayTaskOptions" :key="item.key" :label="item.label"
                                 :value="item.value" :data-extra="item.extra"/>
                    </el-select>
                    <div class="refresh-con">
                      <el-icon>
                        <IconRefresh @click="getreplayTaskOptions"/>
                      </el-icon>
                    </div>
                  </el-form-item>
                  <el-form-item :label="t('transcribe.create.sourceip')" prop="sourceIp">
                    <el-select v-model="taskBasicInfo.sourceIp" :placeholder="t('transcribe.create.sourceip')"
                               filterable :teleported="false" :loading="loadingSource" style="width: 30vw"
                               @change="getSourceClusterDB" @blur="validateSourcehostip"
                               :disabled="replayTaskVersion && taskBasicInfo.replayTaskId != ''">
                      <el-option v-for="item in sourceClusterOptions" :key="item.key" :label="item.value"
                                 :value="item.value"/>
                    </el-select>
                    <div class="refresh-con">
                      <el-icon>
                        <IconRefresh @click="frechSourceCluster"/>
                      </el-icon>
                      <el-link @click="handleAddSql(taskBasicInfo.sourceDbType.toUpperCase())">
                        {{ $t('transcribe.create.newsource') }}
                      </el-link>
                    </div>
                  </el-form-item>
                  <div v-if="addHostVisible === true" style="margin-left: 170px; margin-bottom: 30px">
                    <span>
                      {{ $t('transcribe.create.addsourcemsg') }}
                      <el-button text @click="handleAddHost">{{
                          $t('transcribe.create.createserver')
                        }}</el-button>
                    </span>
                  </div>
                  <el-form-item :label="t('transcribe.create.serveruser')" prop="sourceHostUser" :error="sourceDbError">
                    <el-select v-model="taskBasicInfo.sourceHostUser" :placeholder="t('transcribe.create.pendinguser')"
                               filterable
                               :teleported="false" style="width: 30vw"
                               :disabled="replayTaskVersion && taskBasicInfo.replayTaskId != ''">
                      <el-option v-for="item in sourcehostUserList" :key="item.key" :label="item.value"
                                 :value="item.value"/>
                    </el-select>
                    <div class="refresh-con">
                      <el-icon>
                        <IconRefresh @click="getHostUserPage('source')"/>
                      </el-icon>
                      <el-link @click="handleAddUser('source')">{{ $t('transcribe.create.adduser') }}</el-link>
                    </div>
                  </el-form-item>
                  <div
                    v-if="sourcehostUserList.length === 0 && taskBasicInfo.sourceClusterId !== ''
                    && !(replayTaskVersion && taskBasicInfo.replayTaskId != '') && taskBasicInfo.sourceHostUser === ''"
                    style="margin-left: 170px; margin-bottom: 30px">
                    <span>
                      {{ $t('transcribe.create.addusermsg') }}
                      <el-link @click="handleAddUser('source')">{{ $t('transcribe.create.adduser') }}</el-link>
                    </span>
                  </div>
                  <el-form-item :label="t('transcribe.create.sourcepath')" prop="sourceInstallPath"
                                :error="sourceDbError">
                    <el-input v-model="taskBasicInfo.sourceInstallPath" style="width: 30vw"
                              :disabled="replayTaskVersion && taskBasicInfo.replayTaskId != ''"/>
                  </el-form-item>
                  <h4>{{ $t('transcribe.create.targetconfig') }}</h4>
                  <el-form-item :label="t('transcribe.create.targetip')" prop="targetIp">
                    <el-select v-model="taskBasicInfo.targetIp" :placeholder="t('transcribe.create.targetip')"
                               filterable
                               :loading="loadingTarget" @change="getTargetClusterDB" style="width: 30vw"
                               :teleported="false"
                               :disabled="replayTaskVersion && taskBasicInfo.replayTaskId != ''">
                      <el-option v-for="item in targetClusterOptions" :key="item.key" :label="item.value"
                                 :value="item.value"/>
                    </el-select>
                    <div class="refresh-con">
                      <el-icon>
                        <IconRefresh @click="getTargetClustersData"/>
                      </el-icon>
                      <el-link @click="handleAddSql('OPENGAUSS')">{{ $t('transcribe.create.newsource') }}</el-link>
                    </div>
                  </el-form-item>
                  <el-form-item :label="t('transcribe.create.serveruser')" prop="targetHostUser">
                    <el-select v-model="taskBasicInfo.targetHostUser" :placeholder="t('transcribe.create.pendinguser')"
                               filterable
                               :teleported="false"
                               style="width: 30vw" :disabled="replayTaskVersion && taskBasicInfo.replayTaskId != ''">
                      <el-option v-for="item in targethostUserList" :key="item.key" :label="item.value"
                                 :value="item.value"/>
                    </el-select>
                    <div class="refresh-con">
                      <el-icon>
                        <IconRefresh @click="getHostUserPage('target')"/>
                      </el-icon>
                      <el-link @click="handleAddUser('target')">{{ $t('transcribe.create.adduser') }}</el-link>
                    </div>
                  </el-form-item>
                  <div
                    v-if="targethostUserList.length === 0 && taskBasicInfo.targetClusterId !== ''
                    && !(replayTaskVersion && taskBasicInfo.replayTaskId != '') && taskBasicInfo.targetHostUser === ''"
                    style="margin-left: 170px; margin-bottom: 30px">
                    <span>
                      {{ $t('transcribe.create.addusermsg') }}
                      <el-link @click="handleAddUser('target')">{{ $t('transcribe.create.adduser') }}</el-link>
                    </span>
                  </div>
                  <el-form-item :label="t('transcribe.create.targetinstall')" prop="targetInstallPath">
                    <el-input v-model="taskBasicInfo.targetInstallPath" style="width: 30vw"
                              :disabled="replayTaskVersion && taskBasicInfo.replayTaskId != ''"></el-input>
                  </el-form-item>
                  <h4>{{ $t('transcribe.create.dbrelationship') }}</h4>
                  <el-form-item v-for="(item, index) in taskBasicInfo.settings" :key="index"
                                :label="t('transcribe.create.relation')">
                    <el-form-item :label="t('transcribe.create.sourcedb')" :prop="'settings[' + index + '].sourceDB'"
                                  :rules="[{ required: true, message: t('transcribe.create.required'), trigger: ['blur', 'change'] }]"
                                  label-width="10vw" style="margin-right: 30px" :error="removeDbError">
                      <el-select v-model.trim="item.sourceDB" :placeholder="t('transcribe.create.sourcedb')" filterable
                                 style="width: 15vw"
                                 :disabled="replayTaskVersion && taskBasicInfo.replayTaskId != ''" :teleported="false"
                                 :rules="[{ required: true, message: t('transcribe.create.required'), trigger: ['blur', 'change'] }]">
                        <el-option v-for="option in sourceDBOptions" :key="option.key" :label="option.value"
                                   :value="option.value"/>
                      </el-select>
                    </el-form-item>
                    <el-form-item :label="t('transcribe.create.targetdb')" :prop="'settings[' + index + '].targetDB'"
                                  :rules="[{ required: true, message: t('transcribe.create.required'), trigger: ['blur', 'change'] }]"
                                  label-width="10vw" style="margin-right: 30px">
                      <el-select v-model="item.targetDB" :placeholder="t('transcribe.create.targetdb')" filterable
                                 style="width: 15vw"
                                 :disabled="replayTaskVersion && taskBasicInfo.replayTaskId != ''" :teleported="false"
                                 :rules="[{ required: true, message: t('transcribe.create.required'), trigger: ['blur', 'change'] }]">
                        <el-option v-for="option in targetDBOptions" :key="option.key" :label="option.value"
                                   :value="option.value"/>
                      </el-select>
                    </el-form-item>
                    <el-form-item>
                      <el-link v-if="!(replayTaskVersion && taskBasicInfo.replayTaskId != '')"
                               @click.prevent="removeSetting(index)" @blur="validateRemoveDb">
                        {{ $t('transcribe.index.delete') }}
                      </el-link>
                    </el-form-item>
                  </el-form-item>
                  <el-form-item label=" ">
                    <el-button v-if="!(replayTaskVersion && taskBasicInfo.replayTaskId != '')" @click="addSetting()">
                      {{ $t('transcribe.create.addsettings') }}
                    </el-button>
                  </el-form-item>
                </el-form>
              </el-card>
              <el-card class="backgroundcard">
                <el-form :model="taskAdvancedInfo" :rules="taskAdvancedRules" validateTrigger="onBlur" labelAlign="left"
                         label-width="300px" ref="taskAdvancedFormRef">
                  <h4>{{ $t('transcribe.create.advconfig') }}</h4>
                  <el-form-item :label="t('transcribe.create.paraconfig')" label-position="right"
                                prop="isDefaultRecordConfig">
                    <el-radio-group v-model="taskAdvancedInfo.isDefaultRecordConfig"
                                    @change="handleParamsConfig('once')">
                      <el-radio-button value=true>{{ $t('transcribe.create.defaultpara') }}</el-radio-button>
                      <el-radio-button value=false>{{ $t('transcribe.create.custompara') }}</el-radio-button>
                    </el-radio-group>
                    <div v-if="taskAdvancedInfo.isDefaultRecordConfig === 'false'">
                      <div v-if="choosePlaybackMethod === true">
                        <span>{{ $t('transcribe.create.customparayet') }}</span>
                      </div>
                      <div v-else>
                        <span>{{ $t('transcribe.create.customparaalready') }} <el-link type="primary"
                                                                                       @click="handleParamsconfigAgain">{{
                            $t('transcribe.create.modify')
                          }}</el-link>
                          <el-link type="error" @click="defaultParamsConfig">{{
                              $t('transcribe.create.reset')
                            }}</el-link></span>
                      </div>
                    </div>
                  </el-form-item>
                  <div v-if="!replayTaskVersion">
                    <el-form-item :label="t('transcribe.create.transcribemode')" label-position="right"
                                  prop="transcribemode">
                      <el-select v-model="taskAdvancedInfo.transcribemode" @change="changeTranscribeMode(val)"
                                 :teleported="false" filterable style="width: 30vw">
                        <el-option label="tcpdump" value="tcpdump"/>
                        <el-option label="attach" value="attach"/>
                        <el-option label="general" value="general"/>
                      </el-select>
                      <el-tooltip class="item" effect="light"
                                  :content="t('transcribe.create.transcribemodemsg')"
                                  :teleported="false" placement="right">
                        <i class="el-icon icon">
                          <el-icon>
                            <IconHelpCircle/>
                          </el-icon>
                        </i>
                      </el-tooltip>
                    </el-form-item>
                    <div v-if="taskAdvancedInfo.transcribemode === 'tcpdump' && taskAdvancedInfo.isDefaultRecordConfig">
                      <el-form-item :label="t('transcribe.create.interface')" label-position="right"
                                    prop="tcpdump.network.interface"
                                    :rules="[{ required: true, message: t('transcribe.create.interfacemsg'), trigger: ['blur', 'change'] }]">
                        <el-input v-model="taskAdvancedInfo['tcpdump.network.interface']"
                                  @change="changeTcpdumpInterface"
                                  style="width: 30vw"></el-input>
                        <el-tooltip class="item" effect="light" :content="t('transcribe.create.interface')"
                                    placement="right"
                                    :teleported="false">
                          <i class="el-icon icon">
                            <el-icon>
                              <IconHelpCircle/>
                            </el-icon>
                          </i>
                        </el-tooltip>
                      </el-form-item>
                      <el-form-item :label="t('transcribe.create.recordingduration')" label-position="right"
                                    prop="tcpdump.capture.duration"
                                    :rules="[{ required: true, message: t('transcribe.create.recordingdurationmsg'), trigger: ['blur', 'change'] }]">
                        <el-input-number v-model="taskAdvancedInfo['tcpdump.capture.duration']"
                                         @change="changeTcpdumpDuration" style="width: 30vw" :min="0" :max="2147483647"
                                         :step="1"
                                         :precision="0" :controls="false">
                          <template #suffix>
                            <span>{{ $t('transcribe.create.min') }}</span>
                          </template>
                        </el-input-number>
                        <el-tooltip class="item" effect="light" :content="t('transcribe.create.recordingdurationcon')"
                                    placement="right"
                                    :teleported="false">
                          <i class="el-icon icon">
                            <el-icon>
                              <IconHelpCircle/>
                            </el-icon>
                          </i>
                        </el-tooltip>
                      </el-form-item>
                    </div>
                    <div
                      v-else-if="taskAdvancedInfo.transcribemode === 'attach' && taskAdvancedInfo.isDefaultRecordConfig">
                      <el-form-item label="pid" label-position="right" prop="attach.process.pid"
                                    :rules="[{ required: true, message: t('transcribe.create.pidmsg'), trigger: ['blur', 'change'] },
                                    { type: 'number', min: 0, max: 4294967295, message: t('transcribe.create.pidrange'), trigger: ['blur', 'change'] }]">
                        <el-input-number v-model="taskAdvancedInfo['attach.process.pid']"
                                         @change="changeAttachPid" style="width: 30vw" :step="1" :precision="0"
                                         :controls="false"
                                         :min="0">
                        </el-input-number>
                        <el-tooltip class="item" effect="light" :content="t('transcribe.create.pidcon')"
                                    placement="right"
                                    :teleported="false">
                          <i class="el-icon icon">
                            <el-icon>
                              <IconHelpCircle/>
                            </el-icon>
                          </i>
                        </el-tooltip>
                      </el-form-item>
                      <el-form-item :label="t('transcribe.create.recordingduration')" label-position="right"
                                    prop="attach.capture.duration"
                                    :rules="[{ required: true, message: t('transcribe.create.recordingdurationmsg'), trigger: ['blur', 'change'] },]">
                        <el-input-number v-model="taskAdvancedInfo['attach.capture.duration']"
                                         @change="changeAttachDuration" style="width: 30vw" :step="1" :precision="0"
                                         :controls="false"
                                         :min="0">
                          <template #suffix>
                            <span>{{ $t('transcribe.create.min') }}</span>
                          </template>
                        </el-input-number>
                        <el-tooltip class="item" effect="light" :content="t('transcribe.create.recordingdurationcon')"
                                    placement="right"
                                    :teleported="false">
                          <i class="el-icon icon">
                            <el-icon>
                              <IconHelpCircle/>
                            </el-icon>
                          </i>
                        </el-tooltip>
                      </el-form-item>
                      <el-form-item :label="t('transcribe.create.replaytime')" label-position="right"
                                    prop="replay.max.time"
                                    :rules="[{ required: true, message: t('transcribe.create.replaytimemsg'), trigger: ['blur', 'change'] }]">
                        <el-input-number v-model="taskAdvancedInfo['replay.max.time']" style="width: 30vw"
                                         :min="0" :max="2147483647" :step="1" :precision="0" :controls="false">
                          <template #suffix>
                            <span>{{ $t('transcribe.create.min') }}</span>
                          </template>
                        </el-input-number>
                        <el-tooltip class="item" effect="light" placement="right" :teleported="false"
                                    :content="t('transcribe.create.replaytimecon')">
                          <i class="el-icon icon">
                            <el-icon>
                              <IconHelpCircle/>
                            </el-icon>
                          </i>
                        </el-tooltip>
                      </el-form-item>
                    </div>
                    <div v-else>
                      <el-form-item :label="t('transcribe.create.replaytime')" label-position="right"
                                    prop="replay.max.time"
                                    :rules="[{ required: true, message: t('transcribe.create.replaytimemsg'), trigger: ['blur', 'change'] }]">
                        <el-input-number v-model="taskAdvancedInfo['replay.max.time']" style="width: 30vw"
                                         :min="0" :max="2147483647" :step="1" :precision="0" :controls="false">
                          <template #suffix>
                            <span>{{ $t('transcribe.create.min') }}</span>
                          </template>
                        </el-input-number>
                        <el-tooltip class="item" effect="light" placement="right" :teleported="false"
                                    :content="t('transcribe.create.replaytimecon')">
                          <i class="el-icon icon">
                            <el-icon>
                              <IconHelpCircle/>
                            </el-icon>
                          </i>
                        </el-tooltip>
                      </el-form-item>
                    </div>
                  </div>
                  <div v-else>
                    <el-form-item :label="t('transcribe.create.replaytime')" label-position="right"
                                  prop="replay.max.time"
                                  :rules="[{ required: true, message: t('transcribe.create.replaytimemsg'), trigger: ['blur', 'change'] }]">
                      <el-input-number v-model="taskAdvancedInfo['replay.max.time']" style="width: 30vw"
                                       :min="0" :max="2147483647" :step="1" :precision="0" :controls="false">
                        <template #suffix>
                          <span>{{ $t('transcribe.create.min') }}</span>
                        </template>
                      </el-input-number>
                      <el-tooltip class="item" effect="light" placement="right" :teleported="false"
                                  content="{{ $t('transcribe.create.replaytimecon') }}">
                        <i class="el-icon icon">
                          <el-icon>
                            <IconHelpCircle/>
                          </el-icon>
                        </i>
                      </el-tooltip>
                    </el-form-item>
                  </div>
                </el-form>
              </el-card>
            </el-main>
            <el-footer>
              <div class="footer-con" style="">
                <el-button @click="backToIndex">{{ $t('transcribe.create.cancel') }}</el-button>
                <el-button @click="saveParams">{{ $t('transcribe.create.submit') }}</el-button>
              </div>
            </el-footer>
          </el-container>
        </el-container>
      </el-container>
    </div>
    <div>
      <params-config ref="addPlaybackRef" @submit="playbackTnfoSave"/>
      <add-jdbc ref="addJdbcRef" @finish="finishAddJdbc"/>
      <add-host ref="addHostRef" @finish="labelClose"/>
      <add-host-user ref="addUserRef" @finish="getHostUserPage"/>
    </div>
  </div>
</template>

<script setup>
import {computed, watch, ref, toRaw} from "vue"
import {
  getHostInfo, hostListAll, hostUsers, sourceClusterDbsData, sourceClusters, targetClusterDbsData, targetClusters,
  transcribeReplaydownloadAndConfig, transcribeReplayList, transcribeReplaySave, transcribeReplaytoolsVersion
}
  from "@/api/playback"
import ParamsConfig from './components/RecordPlaybackData.vue'
import AddJdbc from './components/AddJdbc.vue'
import AddHost from './components/AddHost.vue'
import AddHostUser from './components/AddHostUser.vue'
import {IconHelpCircle} from "@computing/opendesign-icons"
import showMessage from "@/utils/showMessage"
import {useI18n} from 'vue-i18n'

const {t} = useI18n()
const addPlaybackRef = ref()
const editPlayBackId = ref('')
const handlePlayBack = () => {
  let taskType = 0
  if (taskBasicInfo.value.taskType === 'transcribe') {
    taskType = 1
  } else if (taskBasicInfo.value.taskType === 'replay') {
    taskType = 2
  } else {
    taskType = 0
  }
  const taskVersion = !(taskBasicInfo.value.taskVersion === '6.0.0')
  addPlaybackRef.value?.open(taskRetInfo.value, taskType, taskVersion)
}
const sourceHostInfo = ref({
  hostId: '',
  privateIp: '',
  publicIp: '',
  port: 0
})
const targetHostInfo = ref({
  hostId: '',
  privateIp: '',
  publicIp: '',
  port: 0
})

const addUserRef = ref(null)
const handleAddUser = (type) => {
  if (type === 'source') {
    addUserRef.value?.open(type, sourceHostInfo.value)
  } else {
    addUserRef.value?.open(type, targetHostInfo.value)
  }
}
const getHostUserPage = (type) => {
  if (type === 'source') {
    getUserList(sourceHostInfo.value.hostId, type)
  } else {
    getUserList(targetHostInfo.value.hostId, type)
  }
}

const addHostVisible = ref(false)
const addHostRef = ref()
const handleAddHost = () => {
  addHostRef.value?.open()
}

const backToIndex = () => {
  init()
  window.$wujie?.bus.$emit('opengauss-close-tab', {
    name: 'Static-pluginData-migrationCreatetranscribetask',
    fullPath: '/static-plugin/data-migration//createtranscribetask'
  })
  window.$wujie?.props.methods.jump({
    name: `Static-pluginData-migrationTranscribe`
  })
}

const finishAddJdbc = (type) => {
  if (type === 'MYSQL') {
    getSourceClustersData()
  } else {
    getTargetClustersData()
  }
}

const sourceDbError = ref('')
const validateSourcehostip = () => {
  if (addHostVisible.value === true) {
    sourceDbError.value = t('transcribe.create.withoutserver')
  } else {
    sourceDbError.value = ''
  }
}

const addSetting = () => {
  taskBasicInfo.value.settings.push({sourceDB: '', targetDB: '', key: ''})
}
const removeDbError = ref('')
const validateRemoveDb = () => {
  if (taskBasicInfo.value.settings.length === 1) {
    removeDbError.value = t('transcribe.create.withoutsettings')
  } else {
    removeDbError.value = ''
  }
}
const removeSetting = (index) => {
  if (taskBasicInfo.value.settings.length > 1) {
    taskBasicInfo.value.settings.splice(index, 1)
  } else {
    showMessage('error', t('transcribe.create.withoutsettings'))
  }
}

const choosePlaybackMethod = ref(true)
const defaultParamsConfig = async () => {
  choosePlaybackMethod.value = false
  taskAdvancedInfo.value.isDefaultRecordConfig = true
  taskBasicInfo.value.isDefaultRecordConfig = taskAdvancedInfo.value.isDefaultRecordConfig
  await changeTranscribeMode(taskRetInfo.value["sql.transcribe.mode"])
  taskRetInfo.value["sql.storage.mode"] = 'json'
  taskRetInfo.value.pagesec = {...defaultDataJson.value}
  taskRetInfo.value.pagethi = {...defaultDataPlayback.value}
  await inittaskPlayBack()
}

const handleParamsConfig = async (type) => {
  taskBasicInfo.value.isDefaultRecordConfig = taskAdvancedInfo.value.isDefaultRecordConfig
  let validRes = true
  await taskAdvancedFormRef.value.validate((valid, errors) => {
    if (valid) {
      validRes = validRes && true
    } else {
      validRes = validRes && false
    }
  })
  await taskNameFormRef.value.validate((valid, errors) => {
    if (valid) {
      validRes = validRes && true
    } else {
      validRes = validRes && false
    }
  })
  await taskDataFormRef.value.validate((valid, errors) => {
    if (valid) {
      validRes = validRes && true
    } else {
      validRes = validRes && false
    }
  })
  if (taskBasicInfo.value.isDefaultRecordConfig === 'false' && validRes === true) {
    if (type === 'once') {
      await changeTranscribeMode(taskRetInfo.value["sql.transcribe.mode"])
      await inittaskPlayBack()
    }
    taskBasicInfo.value.isDefaultRecordConfig = false
    handlePlayBack()
  } else {
    if (taskBasicInfo.value.isDefaultRecordConfig === 'false' || taskBasicInfo.value.isDefaultRecordConfig === false) {
      showMessage('error', t('transcribe.create.withoutsave'))
    }
    taskBasicInfo.value.isDefaultRecordConfig = true
    taskAdvancedInfo.value.isDefaultRecordConfig = true
    defaultParamsConfig()
  }
}

const handleParamsconfigAgain = () => {
  taskAdvancedInfo.value.isDefaultRecordConfig = 'false'
  taskBasicInfo.value.isDefaultRecordConfig = taskAdvancedInfo.value.isDefaultRecordConfig
  handleParamsConfig('again')
}

const playbackTnfoSave = (recordPlaybackData) => {
  for (let recordPlaybackDataKey in recordPlaybackData) {
    if (toRaw(recordPlaybackData[recordPlaybackDataKey]).length != 0) {
      if (recordPlaybackDataKey === 'sql.storage.mode' || recordPlaybackDataKey === 'sql.transcribe.mode') {
        taskRetInfo.value[recordPlaybackDataKey] = recordPlaybackData[recordPlaybackDataKey]
      } else {
        for (let recordPlaybackDatumKey in recordPlaybackData[recordPlaybackDataKey]) {
          taskRetInfo.value[recordPlaybackDataKey][recordPlaybackDatumKey] =
            recordPlaybackData[recordPlaybackDataKey][recordPlaybackDatumKey]
        }
      }
    }
  }
  editPlayBackId.value = ''
  choosePlaybackMethod.value = false
}

const changeTranscribeMode = (val) => {
  taskBasicInfo.value.transcribemode = taskAdvancedInfo.value.transcribemode
  taskRetInfo.value["sql.transcribe.mode"] = taskAdvancedInfo.value.transcribemode
  if (taskAdvancedInfo.value.transcribemode === 'tcpdump') {
    taskRetInfo.value.pagefir = {...defaultDataTcp.value}
    taskRetInfo.value.pagefir['tcpdump.database.ip'] = sourceHostInfo.value.publicIp
    taskRetInfo.value.pagefir['tcpdump.database.port'] = sourceDbPort.value
    taskRetInfo.value.pagefir['remote.receiver.name'] = taskBasicInfo.value.targetHostUser
    taskRetInfo.value.pagefir['remote.node.ip'] = targetHostInfo.value.publicIp
    taskRetInfo.value.pagefir['tcpdump.network.interface'] = taskAdvancedInfo.value['tcpdump.network.interface']
    taskRetInfo.value.pagefir['tcpdump.capture.duration'] = taskAdvancedInfo.value['tcpdump.capture.duration']
  } else if (taskAdvancedInfo.value.transcribemode === 'attach') {
    taskRetInfo.value.pagefir = {...defaultDataAtt.value}
    taskRetInfo.value.pagefir['remote.receiver.name'] = taskBasicInfo.value.targetHostUser
    taskRetInfo.value.pagefir['remote.node.ip'] = targetHostInfo.value.publicIp
    taskRetInfo.value.pagefir['remote.node.port'] = targetHostInfo.value.port
    taskRetInfo.value.pagefir['attach.process.pid'] = taskAdvancedInfo.value['attach.process.pid']
    taskRetInfo.value.pagefir['attach.target.schema'] = taskAdvancedInfo.value['attach.target.schema']
    taskRetInfo.value.pagefir['attach.capture.duration'] = taskAdvancedInfo.value['attach.capture.duration']
    taskRetInfo.value.pagethi['sql.replay.strategy'] = 'serial'
    taskRetInfo.value.pagethi['sql.replay.parallel.max.pool.size'] = 1
    taskRetInfo.value.pagefir['attach.target.schema'] = taskBasicInfo.value.settings[0].sourceDB
  } else {
    taskRetInfo.value.pagefir = {...defaultDataGen.value}
    taskRetInfo.value.pagefir['general.database.ip'] = sourceHostInfo.value.publicIp
    taskRetInfo.value.pagefir['general.database.port'] = sourceDbPort.value
    taskRetInfo.value.pagefir['general.database.username'] = sourceDBUserPwd.get(sourceHostInfo.value.publicIp).split(',')[0]
    taskRetInfo.value.pagefir['general.database.password'] = sourceDBUserPwd.get(sourceHostInfo.value.publicIp).split(',')[1]
    taskRetInfo.value.pagethi['sql.replay.strategy'] = 'serial'
    taskRetInfo.value.pagethi['sql.replay.parallel.max.pool.size'] = 1
  }
}

const changeTcpdumpInterface = (input) => {
  taskRetInfo.value.pagefir['tcpdump.network.interface'] = input
}
const changeTcpdumpDuration = (input) => {
  taskRetInfo.value.pagefir['tcpdump.capture.duration'] = input
}
const changeAttachDuration = (input) => {
  taskRetInfo.value.pagefir['attach.capture.duration'] = input
}
const changeAttachPid = (input) => {
  taskRetInfo.value.pagefir['attach.process.pid'] = input
}

const addJdbcRef = ref(null)

const handleAddSql = (dbType) => {
  addJdbcRef.value?.open(dbType)
}

const taskBasicInfo = ref({
  taskName: '',
  taskVersion: '',
  taskType: 'transcribe_replay',
  replayTaskId: '',

  sourceDbType: 'MySQL',
  sourceIp: '',
  sourceInstallPath: '',
  sourceHostUser: '',
  sourceClusterId: '',

  targetIp: '',
  targetHostUser: '',
  targetInstallPath: '',

  targetDBuser: '',
  targetDBpwd: '',
  targetClusterId: '',
  isDefaultRecordConfig: true,
  dbMap: [],
  transcribemode: 'tcpdump',

  settings: [{sourceDB: '', targetDB: '', key: ''}],
})

const taskAdvancedInfo = ref({
  isDefaultRecordConfig: true,
  transcribemode: 'tcpdump',
  'tcpdump.network.interface': 'eth0',
  'tcpdump.capture.duration': 1,
  'attach.process.pid': 1,
  'attach.target.schema': '',
  'attach.capture.duration': 1,
  'replay.max.time': 5,
  settings: [{sourceDB: '', targetDB: '', key: ''}],
})

const taskRetInfo = ref({
  'sql.transcribe.mode': 'tcpdump',
  pagefir: {},
  'sql.storage.mode': 'json',
  pagesec: {},
  pagethi: {}
})

const defaultDataTcp = ref({
  'tcpdump.network.interface': 'eth0',
  'tcpdump.capture.duration': 1,
  'tcpdump.file.name': 'tcpdump-file',
  'tcpdump.file.size': 10,
  'tcpdump.database.ip': '',
  'tcpdump.database.port': 10,
  'queue.size.limit': 10000,
  'packet.batch.size': 10000,
  'should.send.file': true,
  'should.check.system': false,
  'max.cpu.threshold': 0.85,
  'max.memory.threshold': 0.85,
  'max.disk.threshold': 0.85,
  'remote.receiver.name': '',
  'remote.node.ip': '',
  'remote.node.port': 22,
  'remote.retry.count': 1,
  'result.file.size': 10,
  'parse.select.result': false,
})

const defaultDataAtt = ref({
  'attach.process.pid': 1,
  'attach.target.schema': '',
  'attach.capture.duration': 1,
  'should.send.file': true,
  'should.check.system': false,
  'max.cpu.threshold': 0.85,
  'max.memory.threshold': 0.85,
  'max.disk.threshold': 0.85,
  'remote.receiver.name': '',
  'remote.node.ip': '',
  'remote.node.port': 22,
  'remote.retry.count': 1,
  'result.file.size': 10,
  'parse.select.result': false,
})

const defaultDataGen = ref({
  'general.database.ip': '',
  'general.database.port': 0,
  'general.database.username': '',
  'general.database.password': '',
  'general.sql.batch': 1000,
  'general.start.time': new Date('1970-01-01'),
  'result.file.size': 10,
  'parse.select.result': false,
})

const defaultDataJson = ref({
  'sql.file.name': 'sql-file',
  'sql.file.size': 10,
  'result.file.name': 'select-result',
  'parse.max.time': 0,
  'file.count.limit': 100
})

const defaultDataPlayback = ref({
  'sql.replay.strategy': 'parallel',
  'sql.replay.multiple': 1,
  'sql.replay.only.query': false,
  'sql.replay.parallel.max.pool.size': 5,
  'sql.replay.slow.sql.rule': '2',
  'sql.replay.slow.time.difference.threshold': 1000,
  'sql.replay.slow.sql.duration.threshold': 1000,
  'sql.replay.slow.top.number': 5,
  'sql.replay.session.white.list': '[]',
  'sql.replay.session.black.list': '[]',
  'sql.replay.database.ip': '',
  'sql.replay.database.port': '',
  'sql.replay.database.schema.map': '',
  'sql.replay.database.username': '',
  'sql.replay.database.password': '',
  'sql.replay.draw.interval': 100,
  'replay.max.time': 0,
  'source.time.interval.replay': false,
  'compare.select.result': false,
})

const replayTaskVersion = ref(false)

const taskBasicRules = computed(() => {
  return {
    taskName: [
      {required: true, trigger: ['blur', 'change'], message: t('transcribe.create.withouttaskname')},
      {max: 255, message: t('transcribe.create.tasknamemsg'), trigger: ['blur', 'change']}
    ],
    taskType: [
      {required: true, trigger: ['blur', 'change'], message: t('transcribe.create.withoutrecordingtype')}
    ],
    taskVersion: [
      {required: true, trigger: ['blur', 'change'], message: t('transcribe.create.withoutversion')},
    ],
    replayTaskId: [
      {
        required: replayTaskVersion.value,
        trigger: ['blur', 'change'],
        message: t('transcribe.create.withoutreplaytaskname')
      }
    ],
    sourceIp: [
      {required: true, trigger: ['blur', 'change'], message: t('transcribe.create.withoutsourceip')}
    ],
    sourceHostUser: [
      {required: true, trigger: ['blur', 'change'], message: t('transcribe.create.withoutsourceuser')},
    ],
    sourceInstallPath: [
      {required: true, trigger: ['blur', 'change'], message: t('transcribe.create.withoutsourcepath')},
      {
        pattern: /^([\/~])(?!\/)(?!.*\/\/).*$/,
        message: t('transcribe.create.formaterror'),
        trigger: ['blur', 'change']
      },
      {max: 255, message: t('transcribe.create.sourcepathmsg'), trigger: ['blur', 'change']},
    ],
    targetIp: [
      {required: true, trigger: ['blur', 'change'], message: t('transcribe.create.withouytargetip')},
    ],
    targetHostUser: [
      {required: true, trigger: ['blur', 'change'], message: t('transcribe.create.withouytargetuser')},
    ],
    targetInstallPath: [
      {required: true, trigger: ['blur', 'change'], message: t('transcribe.create.withouytargetpath')},
      {
        pattern: /^([\/~])(?!\/)(?!.*\/\/).*$/,
        message: t('transcribe.create.formaterror'),
        trigger: ['blur', 'change']
      },
      {max: 255, message: t('transcribe.create.targetpathmsg'), trigger: ['blur', 'change']},
    ],
  }
})

const taskAdvancedRules = computed(() => {
  return {
    transcribemode: [
      {required: true, trigger: ['blur', 'change'], message: t('transcribe.create.withoutreplaytype')},
    ],
  }
})

const taskAdvancedFormRef = ref(null)
const taskNameFormRef = ref(null)
const taskDataFormRef = ref(null)
const sourceClusterOptions = ref([])
const taskVersionOptions = ref([])
const sourceDBOptions = ref([])
const sourceClusterInfo = ref({})

const loadingSource = ref(false)

const getTaskversionData = () => {
  loadingSource.value = true
  transcribeReplaytoolsVersion().then(res => {
    if (Number(res.code) === 200) {
      taskVersionOptions.value = []
      res.data.forEach(item => {
        if (item !== '6.0.0') {
          taskVersionOptions.value.push({key: item, value: item})
        }
      })
    }
    loadingSource.value = false
  }).catch(error => {
    loadingSource.value = false
    console.log(error)
  })
}

const replayTaskOptions = ref()
const getreplayTaskOptions = () => {
  const filter = ({
    pageNum: 1,
    pageSize: 0
  })
  transcribeReplayList({
    ...filter
  }).then(res => {
    if (Number(res.code) === 200) {
      filter.pageSize = res.total
      transcribeReplayList({
        ...filter
      }).then(res => {
        if (Number(res.code) === 200) {
          replayTaskOptions.value = []
          res.rows.forEach(item => {
            if (item.taskType !== 'replay') {
              replayTaskOptions.value.push({
                key: item.id,
                value: item.id.toString(),
                label: item.taskName,
                extra: toRaw(item)
              })
            }
          })
        }
      }).catch(error => {
        console.log(error)
      })
    }
  }).catch(error => {
    console.log(error)
  })
}

const changeReplayVersion = (value) => {
  if (value === 'replay') {
    replayTaskVersion.value = true
  } else {
    replayTaskVersion.value = false
    getSourceClusterDB()
    getTargetClusterDB()
  }
}

const backTaskBasicinfo = (value) => {
  const selectedOption = toRaw(replayTaskOptions.value.find(option => option.value === value))
  getHostInfo(value).then(res => {
    if (Number(res.code) === 200) {
      taskBasicInfo.value.taskVersion = selectedOption.extra.toolVersion
      taskBasicInfo.value.sourceIp = res.data[0].ip
      taskBasicInfo.value.sourceHostUser = res.data[0].userName
      taskBasicInfo.value.sourceClusterId = selectedOption.extra.sourceNodeId
      taskBasicInfo.value.sourceDbType = res.data[0].dbType
      sourceClusterOptions.value.forEach(item => {
        if (item.value.includes(res.data[0].ip)) {
          taskBasicInfo.value.sourceIp = item.value
        }
      })
      let tempValue = '/' + value
      taskBasicInfo.value.sourceInstallPath = selectedOption.extra.sourceInstallPath.split(tempValue)[0]
      targetClusterOptions.value.forEach(item => {
        if (item.value.includes(res.data[1].ip)) {
          taskBasicInfo.value.targetIp = item.value
        }
      })
      taskBasicInfo.value.targetHostUser = res.data[1].userName
      taskBasicInfo.value.targetClusterId = selectedOption.extra.targetNodeId
      taskBasicInfo.value.targetInstallPath = selectedOption.extra.targetInstallPath.split(tempValue)[0]
      taskBasicInfo.value.settings = []
      let tempKey = 0
      selectedOption.extra.dbMap.forEach(item => {
        let sourceDb = item.split(':')[0]
        let targetDb = item.split(':')[1]
        taskBasicInfo.value.settings.push({sourceDB: sourceDb, targetDB: targetDb, key: tempKey})
        tempKey = tempKey + 1
      })
      let lastColonIndex = taskBasicInfo.value.sourceIp.lastIndexOf(':')
      let tempIp = ''
      if (lastColonIndex !== -1) {
        tempIp = taskBasicInfo.value.sourceIp.slice(0, lastColonIndex)
        sourceHostInfo.value.hostId = hostIpId.get([tempIp]).split(',')[0]
        sourceHostInfo.value.publicIp = hostIpId.get([tempIp]).split(',')[1]
        sourceHostInfo.value.privateIp = hostIpId.get([tempIp]).split(',')[2]
        sourceHostInfo.value.port = hostIpId.get([tempIp]).split(',')[3]
      }
      lastColonIndex = taskBasicInfo.value.targetIp.lastIndexOf(':')
      if (lastColonIndex !== -1) {
        tempIp = taskBasicInfo.value.targetIp.slice(0, lastColonIndex)
        targetHostInfo.value.hostId = hostIpId.get([tempIp]).split(',')[0]
        targetHostInfo.value.publicIp = hostIpId.get([tempIp]).split(',')[1]
        targetHostInfo.value.privateIp = hostIpId.get([tempIp]).split(',')[2]
        targetHostInfo.value.port = hostIpId.get([tempIp]).split(',')[3]
      }
      taskBasicInfo.value.targetDBuser = targetDBUserPwd.get(targetHostInfo.value.publicIp).split(',')[0]
      taskBasicInfo.value.targetDBpwd = targetDBUserPwd.get(targetHostInfo.value.publicIp).split(',')[1]
    }
  })
}
const sourceDBUserPwd = new FormData
const getSourceClustersData = () => {
  loadingSource.value = true
  sourceClusters().then(res => {
    if (Number(res.code) === 200) {
      sourceClusterOptions.value = []
      res.data.sourceClusters.forEach(item => {
        if (item.dbType.toLowerCase() === taskBasicInfo.value.sourceDbType.toLowerCase()) {
          let tempname = item.nodes[0].ip + ':' + item.nodes[0].port
          sourceClusterOptions.value.push({key: tempname, value: tempname})
          sourceClusterInfo.value[tempname] = [item.nodes[0].username, item.nodes[0].password, item.nodes[0].clusterNodeId]
          sourceDBUserPwd.has(item.nodes[0].ip) ?
            sourceDBUserPwd.set(item.nodes[0].ip, [item.nodes[0].username, item.nodes[0].password]) :
            sourceDBUserPwd.append(item.nodes[0].ip, [item.nodes[0].username, item.nodes[0].password])
        }
      })
    }
    loadingSource.value = false
  }).catch(error => {
    loadingSource.value = false
    console.log(error)
  })
}

const targetClusterOptions = ref([])
const targetDBOptions = ref([])
const targetClusterInfo = ref({})
const loadingTarget = ref(false)

const targetDBUserPwd = new FormData
const getTargetClustersData = () => {
  loadingTarget.value = true
  targetClusters().then(res => {
    if (Number(res.code) === 200) {
      targetClusterOptions.value = []
      res.data.targetClusters.forEach(item => {
        let temp = item.clusterNodes[0]
        let tempname = temp.publicIp + ':' + temp.dbPort
        targetClusterOptions.value.push({key: tempname, value: tempname})
        delete temp["isSystemAdmin"]
        targetClusterInfo.value[tempname] = temp
        targetDBUserPwd.has(temp.publicIp) ? targetDBUserPwd.set(temp.publicIp, [temp.dbUser, temp.dbUserPassword]) :
          targetDBUserPwd.append(temp.publicIp, [temp.dbUser, temp.dbUserPassword])
      })
    }
    loadingTarget.value = false
  }).catch(error => {
    loadingTarget.value = false
    console.log(error)
  })
}
const hostIpId = new FormData()
const targethostUserList = ref([])
const targethostUserPwd = new FormData()
const sourcehostUserList = ref([])
const sourcehostUserPwd = new FormData()
const getUserList = (hostId, type) => {
  hostUsers(hostId).then((res) => {
    if (Number(res.code) === 200) {
      if (type === 'target') {
        targethostUserList.value = []
      } else {
        sourcehostUserList.value = []
      }
      res.rows.forEach(item => {
        if (item.sudo === true) {
          if (type === 'target') {
            targethostUserList.value.push({key: item.username, value: item.username})
            targethostUserPwd.has(item.username) ? targethostUserPwd.set(item.username, item.password)
              : targethostUserPwd.append(item.username, item.password)
          } else {
            sourcehostUserList.value.push({key: item.username, value: item.username})
            sourcehostUserPwd.has(item.username) ? sourcehostUserPwd.set(item.username, item.password)
              : sourcehostUserPwd.append(item.username, item.password)
          }

        }
      })
    }
  }).catch((error) => {
    console.log(error)
  })
}

const sourceDbPort = computed(() => {
  const lastColonIndex = taskBasicInfo.value.sourceIp.lastIndexOf(':')
  if (lastColonIndex !== -1) {
    return Number(taskBasicInfo.value.sourceIp.slice(lastColonIndex + 1))
  } else {
    return 0
  }
})
const getSourceClusterDB = () => {
  sourceDBOptions.value = []
  const tempformdata = new FormData()
  const lastColonIndex = taskBasicInfo.value.sourceIp.lastIndexOf(':')
  let tempIp = ''
  if (lastColonIndex !== -1) {
    tempIp = taskBasicInfo.value.sourceIp.slice(0, lastColonIndex)
  } else {
    addHostVisible.value = true
  }
  if (!hostIpId.get([tempIp])) {
    addHostVisible.value = true
  } else {
    addHostVisible.value = false
    validateSourcehostip()
    sourceHostInfo.value.hostId = hostIpId.get([tempIp]).split(',')[0]
    sourceHostInfo.value.publicIp = hostIpId.get([tempIp]).split(',')[1]
    sourceHostInfo.value.privateIp = hostIpId.get([tempIp]).split(',')[2]
    sourceHostInfo.value.port = hostIpId.get([tempIp]).split(',')[3]
    getUserList(sourceHostInfo.value.hostId, 'source')
    tempformdata.append('url', `jdbc:${taskBasicInfo.value.sourceDbType.toLowerCase()}://${taskBasicInfo.value.sourceIp}`)
    tempformdata.append('username', sourceClusterInfo.value[taskBasicInfo.value.sourceIp][0])
    tempformdata.append('password', sourceClusterInfo.value[taskBasicInfo.value.sourceIp][1])
    taskBasicInfo.value.sourceClusterId = sourceClusterInfo.value[taskBasicInfo.value.sourceIp][2]
    sourceClusterDbsData(tempformdata).then(res => {
      if (Number(res.code) === 200) {
        sourceDBOptions.value = []
        res.data.forEach(item => {
          sourceDBOptions.value.push({key: item, value: item})
        })
      }
    }).catch(error => {
      console.log('get source db error:', error)
    })
  }
}

const targetDbPort = computed(() => {
  const lastColonIndex = taskBasicInfo.value.targetIp.lastIndexOf(':')
  if (lastColonIndex !== -1) {
    return Number(taskBasicInfo.value.targetIp.slice(lastColonIndex + 1))
  } else {
    return 0
  }
})
const getTargetClusterDB = () => {
  targetDBOptions.value = []
  const lastColonIndex = taskBasicInfo.value.targetIp.lastIndexOf(':')
  let tempIp = ''
  if (lastColonIndex !== -1) {
    tempIp = taskBasicInfo.value.targetIp.slice(0, lastColonIndex)
  }
  targetHostInfo.value.hostId = hostIpId.get([tempIp]).split(',')[0]
  targetHostInfo.value.publicIp = hostIpId.get([tempIp]).split(',')[1]
  targetHostInfo.value.privateIp = hostIpId.get([tempIp]).split(',')[2]
  targetHostInfo.value.port = hostIpId.get([tempIp]).split(',')[3]
  taskBasicInfo.value.targetClusterId = targetClusterInfo.value[taskBasicInfo.value.targetIp].nodeId
  getUserList(targetHostInfo.value.hostId, 'target')
  targetClusterDbsData(targetClusterInfo.value[taskBasicInfo.value.targetIp]).then(res => {
    if (Number(res.code) === 200) {
      res.data.forEach(item => {
        targetDBOptions.value.push({key: item.dbName, value: item.dbName, select: item.isSelect})
      })
      taskBasicInfo.value.targetDBuser = targetDBUserPwd.get(tempIp).split(',')[0]
      taskBasicInfo.value.targetDBpwd = targetDBUserPwd.get(tempIp).split(',')[1]
    }
  }).catch(error => {
    console.log('get source db error:', error)
  })
}

const getHostIp = () => {
  hostIpId.forEach(item => hostIpId.delete(item))
  hostListAll().then((res) => {
    if (Number(res.code) === 200) {
      res.data.forEach(item => {
        hostIpId.append(item.publicIp, [item.hostId, item.publicIp, item.privateIp, item.port])
      })
    }
  }).catch(error => {
    console.log('get ip error:', error)
  }) .finally(() => {
    if (addHostVisible.value === true) {
      getSourceClusterDB()
    }
  })
}

const frechSourceCluster = () => {
  if (addHostVisible.value === true) {
    getHostIp()
  } else {
    getSourceClustersData()
  }
}

const labelClose = () => {
  getHostIp()
}

const inittaskRetInfo = () => {
  taskRetInfo.value["sql.storage.mode"] = 'json'
  taskRetInfo.value.pagefir = {...defaultDataTcp.value}
  taskRetInfo.value["sql.transcribe.mode"] = 'tcpdump'
  taskRetInfo.value.pagesec = {...defaultDataJson.value}
  taskRetInfo.value.pagethi = {...defaultDataPlayback.value}
}

const inittaskPlayBack = () => {
  taskRetInfo.value.pagethi['sql.replay.database.ip'] = targetHostInfo.value.publicIp
  taskRetInfo.value.pagethi['sql.replay.database.port'] = targetDbPort.value
  taskRetInfo.value.pagethi['sql.replay.database.schema.map'] = []
  taskBasicInfo.value.settings.forEach((item) => {
    taskRetInfo.value.pagethi['sql.replay.database.schema.map'].push(item.sourceDB + ':' + item.targetDB)
  })
  taskRetInfo.value.pagethi['sql.replay.database.username'] = taskBasicInfo.value.targetDBuser
  taskRetInfo.value.pagethi['sql.replay.database.password'] = taskBasicInfo.value.targetDBpwd
}

const saveParams = async () => {
  taskBasicInfo.value.dbMap = []
  taskBasicInfo.value.settings.forEach((item) => {
    taskBasicInfo.value.dbMap.push(item.sourceDB + ':' + item.targetDB)
  })
  if (taskRetInfo.value['sql.transcribe.mode'] === 'attach') {
    taskRetInfo.value.pagefir['attach.target.schema'] = taskBasicInfo.value.settings[0].sourceDB
  }
  if (taskBasicInfo.value.isDefaultRecordConfig === true || taskRetInfo.value.pagethi.length === 0) {
    changeTranscribeMode(taskRetInfo.value['sql.transcribe.mode'])
    await inittaskPlayBack()
  }
  if (taskBasicInfo.value.isDefaultRecordConfig === true && taskRetInfo.value['sql.transcribe.mode'] === 'tcpdump'
    && !replayTaskVersion.value) {
    taskRetInfo.value.pagethi['replay.max.time'] = 0
  } else if (taskAdvancedInfo.value["replay.max.time"] !== 0 &&
    (taskRetInfo.value['sql.transcribe.mode'] !== 'tcpdump' || replayTaskVersion.value)) {
    taskRetInfo.value.pagethi['replay.max.time'] = taskAdvancedInfo.value["replay.max.time"]
  }

  const retMessageMap = {}
  retMessageMap['sql.transcribe.mode'] = taskRetInfo.value['sql.transcribe.mode']
  for (let pagefirKey in taskRetInfo.value.pagefir) {
    retMessageMap[pagefirKey] = taskRetInfo.value.pagefir[pagefirKey]
  }
  retMessageMap['sql.storage.mode'] = taskRetInfo.value['sql.storage.mode']
  for (let pagesecKey in taskRetInfo.value.pagesec) {
    retMessageMap[pagesecKey] = taskRetInfo.value.pagesec[pagesecKey]
  }
  for (let pagethiKey in taskRetInfo.value.pagethi) {
    retMessageMap[pagethiKey] = taskRetInfo.value.pagethi[pagethiKey]
  }
  retMessageMap['tcpdump.file.id'] = taskBasicInfo.value.replayTaskId
  if (retMessageMap['general.start.time']) {
    const localTimeForm = retMessageMap['general.start.time'].toLocaleString().replace('T', ' ').replace(/\//g, '-').substring(0, 19)
    const formattedDate = localTimeForm.replace(
      /(\d{4})-(\d{1,2})-(\d{1,2}) (\d{2}:\d{2}:\d{2})/,
      (match, year, month, day, time) => {
        const formattedMonth = month.padStart(2, '0')
        const formattedDay = day.padStart(2, '0')
        return `${year}-${formattedMonth}-${formattedDay} ${time}`
      })
    retMessageMap['general.start.time'] = formattedDate
  }
  let validRes = true
  await taskAdvancedFormRef.value.validate((valid, errors) => {
    if (valid) {
      validRes = validRes && true
    } else {
      validRes = validRes && false
    }
  })
  await taskNameFormRef.value.validate((valid, errors) => {
    if (valid) {
      validRes = validRes && true
    } else {
      validRes = validRes && false
    }
  })
  await taskDataFormRef.value.validate((valid, errors) => {
    if (valid) {
      validRes = validRes && true
    } else {
      validRes = validRes && false
    }
  })
  if (validRes === true) {
    transcribeReplaySave({
      "taskName": taskBasicInfo.value.taskName,
      "sourceDbType": taskBasicInfo.value.sourceDbType,
      "sourceIp": sourceHostInfo.value.publicIp,
      "sourcePort": sourceDbPort.value,
      "sourceInstallPath": taskBasicInfo.value.sourceInstallPath,
      "targetIp": targetHostInfo.value.publicIp,
      "targetPort": targetDbPort.value,
      "targetInstallPath": taskBasicInfo.value.targetInstallPath,
      "sourceUser": taskBasicInfo.value.sourceHostUser,
      "targetUser": taskBasicInfo.value.targetHostUser,
      "dbMap": taskBasicInfo.value.dbMap,
      "toolVersion": taskBasicInfo.value.taskVersion,
      "taskType": taskBasicInfo.value.taskType,
      "sourceNodeId": taskBasicInfo.value.sourceClusterId,
      "targetNodeId": taskBasicInfo.value.targetClusterId,
    }).then(res => {
      if (Number(res.code) === 200) {
        let taskid = res.data
        transcribeReplaydownloadAndConfig(taskid, retMessageMap).then((res) => {
          if (Number(res.code) === 200) {
            showMessage('success', t('transcribe.create.savesuc'))
            backToIndex()
          }
        }).catch(error => {
          console.log('get ip error:', error)
        })
      }
    }).catch(error => {
      console.log('get ip error:', error)
    })
  } else {
    showMessage('error', t('transcribe.create.saveerr'))
  }
}

const inittaskAdvancedInfo = () => {
  taskAdvancedInfo.value.isDefaultRecordConfig = true
  taskAdvancedInfo.value.transcribemode = 'tcpdump'
  taskAdvancedInfo.value["tcpdump.network.interface"] = 'eth0'
  taskAdvancedInfo.value["tcpdump.capture.duration"] = 1
  taskAdvancedInfo.value["attach.process.pid"] = 1
  taskAdvancedInfo.value["attach.target.schema"] = ''
  taskAdvancedInfo.value["attach.capture.duration"] = 1
  taskAdvancedInfo.value.settings = [{sourceDB: '', targetDB: '', key: ''}]
}

const inittaskBasicInfo = () => {
  taskBasicInfo.value.taskName = ''
  taskBasicInfo.value.taskVersion = ''
  taskBasicInfo.value.taskType = 'transcribe_replay'
  taskBasicInfo.value.replayTaskId = ''
  taskBasicInfo.value.sourceDbType = 'MySQL'
  taskBasicInfo.value.sourceIp = ''
  taskBasicInfo.value.sourceInstallPath = ''
  taskBasicInfo.value.sourceHostUser = ''
  taskBasicInfo.value.sourceClusterId = ''
  taskBasicInfo.value.targetIp = ''
  taskBasicInfo.value.targetHostUser = ''
  taskBasicInfo.value.targetInstallPath = ''
  taskBasicInfo.value.targetDBuser = ''
  taskBasicInfo.value.targetDBpwd = ''
  taskBasicInfo.value.targetClusterId = ''
  taskBasicInfo.value.isDefaultRecordConfig = true
  taskBasicInfo.value.dbMap = []
  taskBasicInfo.value.transcribemode = 'tcpdump'
  taskBasicInfo.value.settings = [{sourceDB: '', targetDB: '', key: ''}]

}
watch(() => taskBasicInfo.value.sourceIp, (newValue, oldValue) => {
  if (oldValue !== '' && newValue !== oldValue) {
    taskBasicInfo.value.sourceHostUser = ''
    taskBasicInfo.value.settings.length = 1
    taskBasicInfo.value.settings[0].sourceDB = ''
  }
});
watch(() => taskBasicInfo.value.targetIp, (newValue, oldValue) => {
  if (oldValue !== '' && newValue !== oldValue) {
    taskBasicInfo.value.targetHostUser = ''
    taskBasicInfo.value.settings.length = 1
    taskBasicInfo.value.settings[0].targetDB = ''
  }
})

const init = () => {
  sourceDBOptions.value = []
  targetDBOptions.value = []
  sourcehostUserList.value = []
  targethostUserList.value = []
  sourceDBOptions.value = []
  targetDBOptions.value = []
  getSourceClustersData()
  getTargetClustersData()
  getTaskversionData()
  getHostIp()
  getreplayTaskOptions()
  inittaskRetInfo()
  inittaskAdvancedInfo()
  inittaskBasicInfo()
  changeTranscribeMode(taskRetInfo.value['sql.transcribe.mode'])
  choosePlaybackMethod.value = false
  replayTaskVersion.value = false
  if (taskNameFormRef.value) {
    taskNameFormRef.value.resetFields()
  }
  if (taskDataFormRef.value) {
    taskDataFormRef.value.resetFields()
  }
  if (taskAdvancedFormRef.value) {
    taskAdvancedFormRef.value.resetFields()
  }
}

init()
defineExpose({
  init
})

</script>

<style scoped lang="less">
@import '@/assets/style/openGlobal.less';

.background-main {
  min-height: 100vh;
  height: auto;
  width: 100vw;
  background-color: #F4F6FA;
  background-size: 100% 100%;
  position: absolute;

}

.backgroundform {
  background-color: white;
  padding-top: 10px;
  padding-bottom: 10px;
  padding-right: 20px;
  padding-left: 20px;
  margin-top: 20px;
}

.backgroundcard {
  :deep(.el-input-number .el-input__inner) {
    text-align: start;
  }

  background-color: white;
  margin-bottom: 20px;
  box-shadow: 0px 0px 0px;
  border: 0;
}

:deep( .el-select .el-select__selected-item) {
  line-height: 70px;
}

</style>

