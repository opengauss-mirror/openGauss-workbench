<template>
  <a-drawer v-model:visible="visible" width="50vw" unmount-on-close style="margin-left: calc(100% - 100vw)">
    <template #title>
      <div class="title-con">
        <span class="params-title">{{ $t('transcribe.config.title') }}</span>
      </div>
    </template>
    <el-tabs v-model="activeStep" class="demo-tabs" :before-leave="beforeTabLeave">
      <el-tab-pane v-if="taskTypeNum != 2" :label="$t('transcribe.create.transcribemode')" :name="0"/>
      <el-tab-pane v-if="taskTypeNum != 1" :label="$t('transcribe.config.storage')" :name="1"/>
      <el-tab-pane v-if="taskTypeNum != 1" :label="$t('transcribe.config.playback')" :name="2"/>
    </el-tabs>
    <p></p>
    <div class="config-con" id="mainForm">
      <div class="basic-config-con" v-if="activeStep < 2">
        <div class="basic-params-table">
          <el-form :model="formModeSep" class="o-form-has-require" label-position="left" style="text-align: left;">
            <el-table border :data="computedTitleTableData"
                      :columns="formData.columns"
                      :row-style="{height:'70px' }" :cell-style="{ padding:'0px' }" :hoverable="false"
                      :bordered="true" :pagination="false" style="margin-right:50px"
            >
              <el-table-column :label="$t('transcribe.config.parameter')" prop="name">
                <template #default="{ row }">
                  <span>{{ row.name }}</span>
                </template>
              </el-table-column>
              <el-table-column :label="$t('transcribe.config.default')" aria-label="left">
                <template #default="{ row }">
                  <el-form-item :prop="row.name" style="width:100%">
                    <div v-if="activeStep === 0">
                      {{ retPlaybackData["sql.transcribe.mode"] }}
                    </div>
                    <div v-else>
                      <el-select v-model="formData.mode" placeholder="t('transcribe.config.select')" controls-position="right"
                                 style="text-align: left; width: 200%" append-to="#mainForm"
                                 :disabled="retPlaybackData['sql.transcribe.mode'] === 'attach'"
                                 @change="chooseRecoedMode(row)"
                      >
                        <el-option v-for="option in row.options" :key="option.value" :label="option.label"
                                   :value="option.value">
                          {{ option.value }}
                        </el-option>
                      </el-select>
                    </div>
                  </el-form-item>
                </template>
              </el-table-column>
              <el-table-column :label="$t('transcribe.config.parades')" prop="intro">
                <template #default="{ row }">
                    <span class="single-line-text" :title="row.intro">{{ row.intro }}</span>
                </template>
              </el-table-column>
            </el-table>
          </el-form>
        </div>
      </div>
      <div class="basic-config-con">
        <div class="basic-title">{{ formData.mode + t('transcribe.config.config') }}</div>
        <div class="basic-params-table">
          <el-form :model="formDataModel" :rules="formRules" ref="formRef" class="o-form-has-require, table-container"
                   label-position="left"
          >
            <el-table border :data="computedFormTableData" :columns="formData.columns" :row-style="{height:'70px' }"
                      :cell-style="{ padding:'0px' }" :hoverable="false" :bordered="true" :pagination="false"
                      ref="tableRef" style="margin-right:50px"
            >
              <el-table-column :label="t('transcribe.config.parameter')" prop="name">
                <template #default="{ row }">
                  <span>{{ row.name }}</span>
                </template>
              </el-table-column>
              <el-table-column :label="t('transcribe.config.default')" style="text-align: left;">
                <template #default="{ row }">
                  <el-form-item :prop="row.prop" style="width:100%">
                    <template v-if="row.type === 'int'">
                      <el-input-number v-model="formDataModel[row.name]" controls-position="right" style="width: 90%"
                                       :disabled="retPlaybackData['sql.transcribe.mode'] !== 'tcpdump' && row.name === 'sql.replay.parallel.max.pool.size'"/>
                    </template>
                    <template v-else-if="row.type === 'float'">
                      <el-input-number v-model="formDataModel[row.name]" controls-position="right" :precision="2"
                                       :step="0.1" :max="1" style="width: 90%"/>
                    </template>
                    <template v-else-if="row.type === 'select'">
                      <el-select v-model="formDataModel[row.name]" :placeholder="t('transcribe.config.select')" controls-position="right"
                                 append-to="#mainForm" style="text-align: left; width: 90%" value-key="nodeId"
                                 @change="dbEntity => selectChange(row.name, dbEntity)"
                                 :disabled="(retPlaybackData['sql.transcribe.mode'] !== 'tcpdump' && row.name === 'sql.replay.strategy')
                          || (retPlaybackData['sql.transcribe.mode'] !== 'tcpdump' && row.name === 'compare.select.result')">
                        <el-option v-for="(option, i) in row.options" :key="i" :label="option.label"
                                   :value="option.value"/>
                      </el-select>
                    </template>
                    <template v-else-if="row.type === 'date'">
                      <el-date-picker v-model="formDataModel[row.name]" type="datetime" style="text-align: left; width: 90%"
                                      :placeholder="t('transcribe.config.selectdate')" append-to="#mainForm" />
                    </template>
                    <template v-else>
                      <el-input v-model="formDataModel[row.name]" :placeholder="$t('transcribe.config.enter') + row.name"
                                style="text-align: left;width:90%"/>
                    </template>
                  </el-form-item>
                </template>
              </el-table-column>
              <el-table-column :label="$t('transcribe.config.parades')" prop="intro">
                <template #default="{ row }">
                    <span class="single-line-text" :title="row.intro">{{ row.intro }}</span>
                </template>
              </el-table-column>
            </el-table>
          </el-form>
        </div>
      </div>
    </div>
    <template #footer>
      <div class="footer-con">
        <el-button
          type="outline"
          style="margin-right: 10px"
          @click="resetDefault(1)"
        >
          {{ $t('transcribe.config.restore') }}
        </el-button>
        <el-button @click="saveParams">{{ $t('transcribe.config.save') }}</el-button>
      </div>
    </template>
  </a-drawer>
</template>

<script setup>
import {ref, reactive, computed} from 'vue'
import {useI18n} from 'vue-i18n'
import {targetClusters, targetClusterDbsData} from '@/api/task'

const { t } = useI18n()
const formData = reactive({
  steps: [{type: Object,}],
  current: [{type: Number,}],
  columns: [
    {key: 'name', title: t('transcribe.config.parameter'), dataIndex: 'name'},
    {key: 'default', title: t('transcribe.config.default'), dataIndex: 'default'},
    {key: 'intro', title: t('transcribe.config.inputbox'), dataIndex: 'intro'},
  ],
  tableData: [
    {
      index: 0, name: 'sql.transcribe.mode', default: 'tcpdump', value: '', prop: 'sql.transcribe.mode', type: 'select',
      intro: t('transcribe.create.transcribemodemsg'),
      options: [
        {index: 0, label: 'tcpdump', value: 'tcpdump'},
        {index: 1, label: 'attach', value: 'attach'},
        {index: 2, label: 'general', value: 'general'},]
    },
    {
      index: 1, name: 'sql.storage.mode', default: 'json', value: '', prop: 'sql.storage.mode', type: 'select',
      intro: t('transcribe.config.sqlintro'),
      options: [
        {index: 0, label: 'json', value: 'json'},
        {index: 1, label: 'db', value: 'db'},]
    },
  ],
  tableDataTcp: [
    {
      index: 4, name: 'tcpdump.file.name', default: 'tcpdump-file', value: '', prop: 'tcpdump.file.name', type: 'input',
      intro: t('transcribe.config.tcp.filenameintro')
    },
    {
      index: 5, name: 'tcpdump.file.size', default: 10, value: '', prop: 'tcpdump.file.size', type: 'int',
      intro: t('transcribe.config.tcp.filesizeintro')
    },
    {
      index: 8, name: 'queue.size.limit', default: 10000, value: '', prop: 'queue.size.limit', type: 'int',
      intro: t('transcribe.config.tcp.sizelimitintro')
    },
    {
      index: 9, name: 'packet.batch.size', default: 10000, value: '', prop: 'packet.batch.size', type: 'int',
      intro: t('transcribe.config.tcp.batchsizeintro')
    },
    {
      index: 11, name: 'remote.retry.count', default: 1, value: '', prop: 'remote.retry.count', type: 'int',
      intro: t('transcribe.config.tcp.retryxounintro')
    },
    {
      index: 13, name: 'should.check.system', default: true, value: '', prop: 'should.check.system', type: 'select',
      intro: t('transcribe.config.tcp.checksysintro'),
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
    {
      index: 14, name: 'max.cpu.threshold', default: 0.85, value: '', prop: 'max.cpu.threshold', type: 'float',
      intro: t('transcribe.config.tcp.maxcpuintro')
    },
    {
      index: 15, name: 'max.memory.threshold', default: 0.85, value: '', prop: 'max.memory.threshold', type: 'float',
      intro: t('transcribe.config.tcp.maxmemory')
    },
    {
      index: 16, name: 'max.disk.threshold', default: 0.85, value: '', prop: 'max.disk.threshold', type: 'float',
      intro: t('transcribe.config.tcp.diskintro')
    },
    {
      index: 17, name: 'result.file.size', default: 10, value: '', prop: 'result.file.size', type: 'int',
      intro: t('transcribe.config.tcp.selefilesizeintro')
    },
    {
      index: 18, name: 'parse.select.result', default: false, value: '', prop: 'parse.select.result', type: 'select',
      intro: t('transcribe.config.tcp.selecresintro'),
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
  ],
  tableDataAtt: [
    {
      index: 7, name: 'max.cpu.threshold', default: 0.85, value: '', prop: 'max.cpu.threshold', type: 'float',
      intro: t('transcribe.config.tcp.maxcpuintro')
    },
    {
      index: 8, name: 'max.memory.threshold', default: 0.85, value: '', prop: 'max.memory.threshold', type: 'float',
      intro: t('transcribe.config.tcp.maxmemory')
    },
    {
      index: 9, name: 'max.disk.threshold', default: 0.85, value: '', prop: 'max.disk.threshold', type: 'float',
      intro: t('transcribe.config.tcp.diskintro')
    },
    {
      index: 12, name: 'remote.retry.count', default: 1, value: '', prop: 'remote.retry.count', type: 'int',
      intro: t('transcribe.config.tcp.retryxounintro')
    },
    {
      index: 14, name: 'should.check.system', default: true, value: '', prop: 'should.check.system', type: 'select',
      intro: t('transcribe.config.tcp.checksysintro'),
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
    {
      index: 17, name: 'result.file.size', default: 10, value: '', prop: 'result.file.size', type: 'int',
      intro: t('transcribe.config.tcp.selefilesizeintro')
    },
    {
      index: 18, name: 'parse.select.result', default: false, value: '', prop: 'parse.select.result', type: 'select',
      intro: t('transcribe.config.tcp.selecresintro'),
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
  ],
  tableDataGen: [
    {
      index: 0, name: 'general.sql.batch', default: 1000, value: '', prop: 'general.sql.batch', type: 'int',
      intro: t('transcribe.config.gen.sqlbatchintro')
    },
    {
      index: 1,
      name: 'general.start.time',
      default: new Date('1970-01-01'),
      value: '',
      prop: 'general.start.time',
      type: 'date',
      intro: t('transcribe.config.gen.sqlstartintro')
    },
    {
      index: 17, name: 'result.file.size', default: 10, value: '', prop: 'result.file.size', type: 'int',
      intro: t('transcribe.config.tcp.selefilesizeintro')
    },
    {
      index: 18, name: 'parse.select.result', default: false, value: '', prop: 'parse.select.result', type: 'select',
      intro: t('transcribe.config.tcp.selecresintro'),
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
  ],
  tableDataJson: [
    {
      index: 1, name: 'sql.file.name', default: 'sql-file', value: '', prop: 'sql.file.name', type: 'input',
      intro: t('transcribe.config.json.filenameintro')
    },
    {
      index: 2, name: 'sql.file.size', default: 10, value: '', prop: 'sql.file.size', type: 'int',
      intro: t('transcribe.config.json.filesizeintro')
    },
    {
      index: 3, name: 'file.count.limit', default: 100, value: '', prop: 'file.count.limit', type: 'int',
      intro: t('transcribe.config.json.filecountintro')
    },
    {
      index: 4, name: 'result.file.name', default: 'select-result', value: '', prop: 'result.file.name', type: 'input',
      intro: t('transcribe.config.json.resnameintro')
    },
    {
      index: 5, name: 'parse.max.time', default: 0, value: '', prop: 'parse.max.time', type: 'int',
      intro: t('transcribe.config.json.parsetimeintro')
    },
  ],
  tableDataDb: [
    {
      index: 0, name: 'sql.database.db', default: '', value: '', prop: 'sql.database.db', type: 'select',
      options: [],
      intro: t('transcribe.config.db.dbintro')
    },
    {
      index: 1, name: 'sql.database.name', default: '', value: '', prop: 'sql.database.name', type: 'select',
      intro: t('transcribe.config.db.dbnameintro'),
      options: []
    },
    {
      index: 2, name: 'sql.table.name', default: 'sql_table', value: '', prop: 'sql.table.name', type: 'input',
      intro: t('transcribe.config.db.tablenameintro')
    },
    {
      index: 3, name: 'sql.table.drop', default: 'false', value: '', prop: 'sql.table.drop', type: 'select',
      intro: t('transcribe.config.db.tabledropintro'),
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
    {
      index: 4, name: 'result.file.name', default: 'select-result', value: '', prop: 'result.file.name', type: 'select',
      intro: t('transcribe.config.json.resnameintro')
    },
    {
      index: 5, name: 'parse.max.time', default: 0, value: '', prop: 'parse.max.time', type: 'int',
      intro: t('transcribe.config.json.parsetimeintro')
    },
  ],
  tableDataPlayback: [
    {
      index: 0, name: 'sql.replay.strategy', default: 'parallel', value: '', prop: 'sql.replay.strategy',
      type: 'select', intro: t('transcribe.config.back.parallelintro'),
      options: [
        {index: 0, label: 'parallel', value: 'parallel'},
        {index: 1, label: 'serial', value: 'serial'},]
    },
    {
      index: 1, name: 'sql.replay.multiple', default: 1, value: '', prop: 'sql.replay.multiple', type: 'int',
      intro: t('transcribe.config.back.mulintro'),
    },
    {
      index: 2, name: 'sql.replay.only.query', default: false, value: '', prop: 'sql.replay.only.query', type: 'select',
      intro: t('transcribe.config.back.replayonluintro'),
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
    {
      index: 3, name: 'sql.replay.parallel.max.pool.size', default: 5, value: '',
      prop: 'sql.replay.parallel.max.pool.size', type: 'int', intro: t('transcribe.config.back.sessionintro'),
    },
    {
      index: 4, name: 'sql.replay.slow.sql.rule', default: '2', value: '', prop: 'sql.replay.slow.sql.rule',
      type: 'input', intro: t('transcribe.config.back.sqlruleintro'),
      options: [
        {index: 0, label: '1', value: '1'},
        {index: 1, label: '2', value: '2'},]
    },
    {
      index: 5,
      name: 'sql.replay.slow.time.difference.threshold',
      default: 1000,
      value: '',
      prop: 'sql.replay.slow.time.difference.threshold',
      type: 'int',
      intro: t('transcribe.config.back.sqlrule1intro'),
    },
    {
      index: 6, name: 'sql.replay.slow.sql.duration.threshold', default: 1000, value: '',
      prop: 'sql.replay.slow.sql.duration.threshold', type: 'int', intro: t('transcribe.config.back.sqlrule2intro'),
    },
    {
      index: 7, name: 'sql.replay.slow.top.number', default: 5, value: '', prop: 'sql.replay.slow.top.number',
      type: 'int', intro: t('transcribe.config.back.topnintro'),
    },
    {
      index: 8, name: 'sql.replay.draw.interval', default: 100, value: '', prop: 'sql.replay.draw.interval',
      type: 'int', intro: t('transcribe.config.back.intervalintro'),
    },
    {
      index: 10, name: 'sql.replay.session.white.list', default: '[]', value: '', prop: 'sql.replay.session.white.list',
      type: 'input', intro: t('transcribe.config.back.whitelistintro'),
    },
    {
      index: 11, name: 'sql.replay.session.black.list', default: '[]', value: '', prop: 'sql.replay.session.black.list',
      type: 'input', intro: t('transcribe.config.back.blacklistintro'),
    },
    {
      index: 12, name: 'replay.max.time', default: 0, value: '', prop: 'replay.max.time', type: 'int',
      intro: t('transcribe.create.replaytimecon'),
    },
    {
      index: 13, name: 'source.time.interval.replay', default: false, value: '', prop: 'source.time.interval.replay',
      type: 'select', intro: t('transcribe.config.back.intervalreplayintro'),
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
    {
      index: 14, name: 'compare.select.result', default: false, value: '', prop: 'compare.select.result',
      type: 'select', intro: t('transcribe.config.back.selectresultintro'),
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
  ],
  mode: 'tcpdump',
})

const generateRandomString =(index) => {
  const formattedIndex = index.toString().padStart(2, '0')
  switch (formData.mode) {
    case 'tcpdump':
      return `transcribe.config.tcp.5qtkk97a2e${formattedIndex}`
    case 'attach':
      return `transcribe.config.att.5qtkk97a2e${formattedIndex}`
    case 'general':
      return `transcribe.config.gen.5qtkk97a2e${formattedIndex}`
    case 'json':
      return `transcribe.config.json.5qtkk97a2e${formattedIndex}`
    case 'db':
      return `transcribe.config.db.5qtkk97a2e${formattedIndex}`
    default:
      return `transcribe.config.back.5qtkk97a2e${formattedIndex}`
  }
}
const computedFormTableData = computed(() => {
  return formTableData.map(item => {
    const key = generateRandomString(item.index)
    const introText =t(key)
    return {
      ...item,
      intro: introText || item.intro
    }
  })
})

const computedTitleTableData = computed(() => {
  const filteredData = formData.tableData.filter((v) => v.index === activeStep.value)
  return filteredData.map(item => {
    let introText = ''
    if (item.index === 0 ) {
      introText = t('transcribe.create.transcribemodemsg')
    } else {
      introText = t('transcribe.config.sqlintro')
    }
    return {
      ...item,
      intro: introText || item.intro
    }
  })
})

const isVersionNeed = ref(false)
const formModeSep = ref('tcpdump')
const maxIntValue = 2147483647

const formRefTcp = ref(null)

const formDataTcp = ref({
  'tcpdump.file.name': 'tcpdump-file',
  'tcpdump.file.size': 10,
  'tcpdump.database.port': 10,
  'queue.size.limit': 10000,
  'packet.batch.size': 10000,
  'should.send.file': true,
  'should.check.system': true,
  'max.cpu.threshold': 0.85,
  'max.memory.threshold': 0.85,
  'max.disk.threshold': 0.85,
  'remote.retry.count': 1,
})
const formRulesTcp =computed(()=>{
  return {
    'tcpdump.file.name': [
      {required: true, message: t('transcribe.config.tcp.withoutfilename'), trigger: ['blur', 'change']},
      {max: 255, message: t('transcribe.config.tcp.filenamemsg'), trigger: ['blur', 'change']},
    ],
    'tcpdump.file.size': [
      {required: true, message: t('transcribe.config.tcp.withoutfilesize'), trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.tcp.filesizemsg'), trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message: t('transcribe.config.tcp.filesizelimit'), trigger: ['blur', 'change']}
    ],
    'queue.size.limit': [
      {required: true, message: t('transcribe.config.tcp.withoutsizelimit'), trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.tcp.sizelimitmsg'), trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message:  t('transcribe.config.tcp.filesizelimit') , trigger: ['blur', 'change']}
    ],
    'packet.batch.size': [
      {required: true, message: t('transcribe.config.tcp.withoutbatchsize'), trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.tcp.batchsizemsg'), trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message: t('transcribe.config.tcp.filesizelimit'), trigger: ['blur', 'change']}
    ],
    'max.cpu.threshold': [
      {required: true, message: t('transcribe.config.tcp.withoutcpu'), trigger: ['blur', 'change']},
      {type: 'number', min: 0.01, max: 1, message: t('transcribe.config.tcp.cpumsg'), trigger: ['blur', 'change']},
    ],
    'max.memory.threshold': [
      {required: true, message: t('transcribe.config.tcp.withoutmemory'), trigger: ['blur', 'change']},
      {type: 'number', min: 0.01, max: 1, message: t('transcribe.config.tcp.memorymsg'), trigger: ['blur', 'change']},
    ],
    'max.disk.threshold': [
      {required: true, message: t('transcribe.config.tcp.withoutdisk'), trigger: ['blur', 'change']},
      {type: 'number', min: 0.01, max: 1, message: t('transcribe.config.tcp.diskmsg'), trigger: ['blur', 'change']},
    ],
    'result.file.size': [
      {required: isVersionNeed, message: t('transcribe.config.tcp.withoutresfilesize'), trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.tcp.resfilesizemsg'), trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message: t('transcribe.config.tcp.filesizelimit'), trigger: ['blur', 'change']}
    ],
    'parse.select.result': [
      {required: isVersionNeed, message: t('transcribe.config.tcp.withoutseleres'), trigger: ['blur', 'change']},
    ],
  }

})

const formRefAtt = ref(null)
const formDataAtt = ref({
  'attach.process.pid': 1,
  'attach.capture.duration': 1,
  'should.send.file': true,
  'should.check.system': true,
  'max.cpu.threshold': 0.85,
  'max.memory.threshold': 0.85,
  'max.disk.threshold': 0.85,
  'remote.retry.count': 1,
})
const formRulesAtt = computed(() => {
  return {
    'max.cpu.threshold': [
      {required: true, message:  t('transcribe.config.tcp.withoutcpu') , trigger: ['blur', 'change']},
      {type: 'number', min: 0.01, max: 1, message:  t('transcribe.config.tcp.cpumsg') , trigger: ['blur', 'change']},
    ],
    'max.memory.threshold': [
      {required: true, message:  t('transcribe.config.tcp.withoutmemory') , trigger: ['blur', 'change']},
      {type: 'number', min: 0.01, max: 1, message:  t('transcribe.config.tcp.memorymsg') , trigger: ['blur', 'change']},
    ],
    'max.disk.threshold': [
      {required: true, message: t('transcribe.config.tcp.withoutdisk') , trigger: ['blur', 'change']},
      {type: 'number', min: 0.01, max: 1, message: t('transcribe.config.tcp.diskmsg') , trigger: ['blur', 'change']},
    ],
    'remote.retry.count': [
      {required: true, message: t('transcribe.config.att.withoutretrycount') , trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.att.retrycountmsg') , trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message: t('transcribe.config.tcp.filesizelimit') , trigger: ['blur', 'change']}
    ],
    'result.file.size': [
      {required: isVersionNeed, message: t('transcribe.config.tcp.withoutresfilesize') , trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.tcp.resfilesizemsg') , trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message: t('transcribe.config.tcp.filesizelimit') , trigger: ['blur', 'change']}
    ],
    'parse.select.result': [
      {required: isVersionNeed, message: t('transcribe.config.tcp.withoutseleres') , trigger: ['blur', 'change']},
    ],
  }
})

const formRefGen = ref(null)
const formDataGen = ref({
  'general.sql.batch': '',
  'general.start.time': '',
  'result.file.size': '',
  'parse.select.result': '',
})
const formRulesGen = computed(() => {
  return {
    'general.sql.batch': [
      {required: true, message: t('transcribe.config.gen.withoutSqlBatch') , trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.gen.sqlBatchMsg'), trigger: ['blur', 'change']},
      {type: 'number', min: '1', max: maxIntValue, message: t('transcribe.config.gen.sqlBatchLimit'), trigger: ['blur', 'change']}
    ],
    'general.start.time': [
      {required: true, message: t('transcribe.config.gen.withoutStartTime'), trigger: ['blur', 'change']},
      {
        validator: (rule, value, callback) => {
          const minDate = new Date('1970-01-01')
          if (value && new Date(value) < minDate) {
            callback(new Error(t('transcribe.config.gen.startTimeLimit')))
          } else {
            callback()
          }
        },
        trigger: 'change'
      }
    ],
    'result.file.size': [
      {required: isVersionNeed, message: t('transcribe.config.tcp.withoutresfilesize'), trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.tcp.resfilesizemsg'), trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message: t('transcribe.config.tcp.filesizelimit'), trigger: ['blur', 'change']}
    ],
    'parse.select.result': [
      {required: isVersionNeed, message: t('transcribe.config.tcp.withoutseleres'), trigger: ['blur', 'change']},
    ],
  }
})

const formRefJson = ref(null)
const formDataJson = ref({
  'sql.file.name': '',
  'sql.file.size': '',
  'result.file.name': '',
  'parse.max.time': 0,
  'file.count.limit': 100
})

const formRulesJson = computed(() => {
  return {
    'sql.file.name': [
      {required: true, message: t('transcribe.config.json.withoutfilename'), trigger: ['blur', 'change']},
      {max: 255, message: t('transcribe.config.tcp.filenamemsg'), trigger: ['blur', 'change']},
    ],
    'sql.file.size': [
      {required: true, message: t('transcribe.config.json.withoutfilesize'), trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.tcp.filesizemsg'), trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message: t('transcribe.config.tcp.filesizelimit'), trigger: ['blur', 'change']}
    ],
    'parse.max.time': [
      {required: true, message: t('transcribe.config.json.withoutparsetime'), trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.json.parsetimemsg'), trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message: t('transcribe.config.tcp.filesizelimit'), trigger: ['blur', 'change']}
    ],
    'result.file.name': [
      {required: isVersionNeed, message: t('transcribe.config.json.withoutresfilename'), trigger: ['blur', 'change']},
      {max: 255, message: t('transcribe.config.db.resfilemsg'), trigger: ['blur', 'change']},
    ],
    'file.count.limit': [
      {required: true, message: t('transcribe.config.json.withoutfilecount'), trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.json.filecountmsg'), trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message: t('transcribe.config.tcp.filesizelimit'), trigger: ['blur', 'change']}
    ],
  }
})

const formRefDb = ref(null)
const formDataDb = ref({
  'sql.database.db': '',
  'sql.database.ip': '',
  'sql.database.port': '',
  'sql.database.username': '',
  'sql.database.password': '',
  'sql.database.name': '',
  'sql.table.name': '',
  'sql.table.drop': '',
  'result.file.name': '',
  'parse.max.time': 0,
})
const formRulesDb = computed(() => {
  return {
    'sql.database.db': [
      {required: true, message: t('transcribe.config.db.withoutdb'), trigger: ['blur', 'change']},
    ],
    'sql.database.name': [
      {required: true, message: t('transcribe.config.db.withoutdbname'), trigger: ['blur', 'change']},
      {max: 255, message: t('transcribe.config.db.dbnamemsg'), trigger: ['blur', 'change']},
    ],
    'sql.table.name': [
      {required: true, message: t('transcribe.config.db.withoutsqlname'), trigger: ['blur', 'change']},
      {max: 255, message: t('transcribe.config.db.sqltablemsg'), trigger: ['blur', 'change']},
    ],
    'sql.table.drop': [
      {required: true, message: t('transcribe.config.db.withouttabledrop'), trigger: ['blur', 'change']},
    ],
    'result.file.name': [
      {required: isVersionNeed, message: t('transcribe.config.json.withoutresfilename'), trigger: ['blur', 'change']},
      {max: 255, message: t('transcribe.config.db.resfilemsg'), trigger: ['blur', 'change']},
    ],
    'parse.max.time': [
      {required: true, message: t('transcribe.config.json.withoutparsetime'), trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.json.parsetimemsg'), trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message: t('transcribe.config.tcp.filesizelimit'), trigger: ['blur', 'change']}
    ],
  }
})

const formRefPlayback = ref(null)
const formDataPlayback = ref({
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
  'source.time.interval.replay': false,
  'compare.select.result': false,
})

const formRulesPlayback = computed (() => {
  return {
    'sql.replay.strategy': [
      {required: true, message: t('transcribe.config.back.withoutstrategy'), trigger: ['blur', 'change']},
    ],
    'sql.replay.multiple': [
      {required: true, message: t('transcribe.config.back.withoutmultiple'), trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.back.mulmsg'), trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message: t('transcribe.config.tcp.filesizelimit'), trigger: ['blur', 'change']}
    ],
    'sql.replay.only.query': [
      {required: true, message: t('transcribe.config.back.withoutreplayonly'), trigger: ['blur', 'change']},
    ],
    'sql.replay.parallel.max.pool.size': [
      {required: true, message: t('transcribe.config.back.withoutsession'), trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.back.sessionmsg'), trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message: t('transcribe.config.tcp.filesizelimit'), trigger: ['blur', 'change']}
    ],
    'sql.replay.slow.sql.rule': [
      {required: true, message: t('transcribe.config.back.withoutsqlrule'), trigger: ['blur', 'change']},
    ],
    'sql.replay.slow.time.difference.threshold': [
      {required: true, message: t('transcribe.config.back.withoutsql1rule'), trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.back.sqlrule1msg'), trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message: t('transcribe.config.tcp.filesizelimit'), trigger: ['blur', 'change']}
    ],
    'sql.replay.slow.sql.duration.threshold': [
      {required: true, message: t('transcribe.config.back.withoutsqlrule2'), trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.back.sqlrule2msg'), trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message: t('transcribe.config.tcp.filesizelimit'), trigger: ['blur', 'change']}
    ],
    'sql.replay.slow.top.number': [
      {required: true, message: t('transcribe.config.back.withouttopn'), trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.back.topnmsg'), trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message: t('transcribe.config.tcp.filesizelimit'), trigger: ['blur', 'change']}
    ],
    'sql.replay.session.white.list': [
      {required: true, message: t('transcribe.config.back.withoutwhitelist'), trigger: ['blur', 'change']},
      {max: 255, message: t('transcribe.config.db.resfilemsg'), trigger: ['blur', 'change']},
    ],
    'sql.replay.session.black.list': [
      {required: true, message: t('transcribe.config.back.withoutblacklist'), trigger: ['blur', 'change']},
      {max: 255, message: t('transcribe.config.db.resfilemsg'), trigger: ['blur', 'change']},
    ],
    'sql.replay.draw.interval': [
      {required: true, message: t('transcribe.config.back.withoutdraw'), trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.back.drawmsg'), trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message: t('transcribe.config.tcp.filesizelimit'), trigger: ['blur', 'change']}
    ],
    'replay.max.time': [
      {required: isVersionNeed, message: t('transcribe.config.back.withoutreplaytime'), trigger: ['blur', 'change']},
      {pattern: /^[0-9]*$/, message: t('transcribe.config.back.replaytimemsg'), trigger: ['blur', 'change']},
      {type: 'number', min: 0, max: maxIntValue, message: t('transcribe.config.tcp.filesizelimit'), trigger: ['blur', 'change']}
    ],
    'source.time.interval.replay': [
      {required: isVersionNeed, message: t('transcribe.config.back.withoutsourcetime'), trigger: ['blur', 'change']},
    ],
    'compare.select.result': [
      {required: isVersionNeed, message: t('transcribe.config.back.withoutcompare'), trigger: ['blur', 'change']},
    ],
  }
})

const formRef = ref(null)
const formRules = computed(()=> {
  switch (formData.mode) {
    case 'tcpdump':
      return formRulesTcp
    case 'attach':
      return formRulesAtt
    case 'general':
      return formRulesGen
    case 'json':
      return formRulesJson
    case 'db':
      return  formRulesDb
    default:
      return formRulesPlayback
  }
})
const formDataModel = ref()
const formTableData = reactive([])

const chooseRecoedMode = () => {
  initChooseMode(formData.mode)
}

const emits = defineEmits([`submit`])

const visible = ref(false)

const retPlaybackData = ref({
  'sql.transcribe.mode': 'tcpdump',
  pagefir: [],
  'sql.storage.mode': 'json',
  pagesec: [],
  pagethi: []
})
const activeStep = ref(0)

const validCurrentPage = async (currentPage) => {
  return new Promise(resolve => {
    formRef.value.validate((valid) => {
      if (valid) {
        if (currentPage === 0) {
          retPlaybackData.value["sql.transcribe.mode"] = formData.mode
          retPlaybackData.value.pagefir = formDataModel.value
        } else if (currentPage === 1) {
          retPlaybackData.value["sql.storage.mode"] = formData.mode
          retPlaybackData.value.pagesec = formDataModel.value
        } else {
          retPlaybackData.value.pagethi = formDataModel.value
        }
        resolve()
        return true
      } else {
        return false
      }
    })
  })
}

const beforeTabLeave = async (activeName, oldActivename) => {
  let res = await validCurrentPage(oldActivename)
  if (res !== false) {
    switch (activeName) {
      case 0:
        formModeSep.value = retPlaybackData.value["sql.transcribe.mode"]
        formData.mode = retPlaybackData.value["sql.transcribe.mode"]
        formData.tableData[0].value = retPlaybackData.value["sql.transcribe.mode"]
        retPlaybackData.value.pagefir.length === 0 ? initChooseMode(formData.mode) : editChooseMode(formData.mode)
        break
      case 1:
        formModeSep.value = retPlaybackData.value["sql.storage.mode"]
        formData.tableData[1].value = retPlaybackData.value["sql.storage.mode"]
        formData.mode = retPlaybackData.value["sql.storage.mode"]
        retPlaybackData.value.pagesec.length === 0 ? initChooseMode(formData.mode) : editChooseMode(formData.mode)
        break
      case 2:
        formModeSep.value = '回放'
        formData.mode = '回放'
        retPlaybackData.value.pagethi.length === 0 ? initChooseMode(formData.mode) : editChooseMode(formData.mode)
        break
    }

  } else {
    return false
  }
}

const resetDefault = (changeType) => {
  if (changeType === 1) {
    initChooseMode(formData.mode)
  }
}

const saveParams = async () => {
  let res = await (validCurrentPage(activeStep.value))
  if (res !== false) {
    emits('submit', retPlaybackData.value)
    visible.value = false
  }
}

const clearAllErrors = () => {
  if (formRules.value) {
    Object.keys(formRules.value).forEach(field => {
      formRules.value[field] = null
    })
  }

}

const tableRef = ref()
const modeLoading = ref(false)
const initChooseMode = (mode) => {
  modeLoading.value = true
  clearAllErrors()
  formDataModel.value = []
  switch (mode) {
    case 'tcpdump':
      formRef.value = formRefTcp.value
      formDataModel.value = formDataTcp.value
      formTableData.splice(0, formTableData.length, ...formData.tableDataTcp)
      formData.tableDataTcp.forEach((item) => {
        formDataModel.value[item.prop] = item.default
        item.value = item.default
      })
      break;
    case 'attach':
      formRef.value = formRefAtt.value
      formDataModel.value = formDataAtt.value
      formTableData.splice(0, formTableData.length, ...formData.tableDataAtt)
      formData.tableDataAtt.forEach((item) => {
        formDataModel.value[item.prop] = item.default
        item.value = item.default
      })
      break;

    case 'general':
      formRef.value = formRefGen.value
      formDataModel.value = formDataGen.value
      formTableData.splice(0, formTableData.length, ...formData.tableDataGen)
      formData.tableDataGen.forEach((item) => {
        formDataModel.value[item.prop] = item.default
        item.value = item.default
      })
      break;

    case 'json':
      formRef.value = formRefJson.value
      formDataModel.value = formDataJson.value
      formTableData.splice(0, formTableData.length, ...formData.tableDataJson)
      formData.tableDataJson.forEach((item) => {
        formDataModel.value[item.prop] = item.default
        item.value = item.default
      })
      break;

    case 'db':
      formRef.value = formRefDb.value
      formDataModel.value = formDataDb.value
      formTableData.splice(0, formTableData.length, ...formData.tableDataDb)
      formData.tableDataDb.forEach((item) => {
        formDataModel.value[item.prop] = item.default
        item.value = item.default
      })
      break;

    default:
      formRef.value = formRefPlayback.value
      formDataModel.value = formDataPlayback.value
      formTableData.splice(0, formTableData.length, ...formData.tableDataPlayback)
      formData.tableDataPlayback.forEach((item) => {
        formDataModel.value[item.prop] = item.default
        item.value = item.default
      })
      break;
  }
  modeLoading.value = false
}

const editChooseMode = (mode) => {
  modeLoading.value = true
  formDataModel.value = {}
  switch (mode) {
    case 'tcpdump':
      formRef.value = formRefTcp.value
      formDataModel.value = retPlaybackData.value.pagefir
      formTableData.splice(0, formTableData.length, ...formData.tableDataTcp)
      break;

    case 'attach':
      formRef.value = formRefAtt.value
      formDataModel.value = retPlaybackData.value.pagefir
      formTableData.splice(0, formTableData.length, ...formData.tableDataAtt)
      break;

    case 'general':
      formRef.value = formRefGen.value
      formDataModel.value = retPlaybackData.value.pagefir
      formTableData.splice(0, formTableData.length, ...formData.tableDataGen)
      break;

    case 'json':
      formRef.value = formRefJson.value
      formDataModel.value = retPlaybackData.value.pagesec
      formTableData.splice(0, formTableData.length, ...formData.tableDataJson)
      break;

    case 'db':
      formRef.value = formRefDb.value
      formDataModel.value = retPlaybackData.value.pagesec
      formTableData.splice(0, formTableData.length, ...formData.tableDataDb)
      break;

    default:
      formRef.value = formRefPlayback.value
      formDataModel.value = retPlaybackData.value.pagethi
      formTableData.splice(0, formTableData.length, ...formData.tableDataPlayback)
      break;
  }
  modeLoading.value = false
}

const taskTypeNum = ref(0)

const deleteVersionOption = (taskType) => {
  if (taskType === 2) {
    formData.tableDataJson = formData.tableDataJson.filter(key => key.name !== "parse.max.time")
    formData.tableDataDb = formData.tableDataDb.filter(key => key.name !== "parse.max.time")
  } else {
    const indexExists = formData.tableDataJson.some(item => item.index === 5)
    if (!indexExists) {
      formData.tableDataJson.push(
        {
          index: 5, name: 'parse.max.time', default: 0, value: '', prop: 'parse.max.time', type: 'int',
          intro: t('transcribe.config.json.parsetimeintro')
        },
      )
      formData.tableDataDb.push(
        {
          index: 5, name: 'parse.max.time', default: 0, value: '', prop: 'parse.max.time', type: 'int',
          intro: t('transcribe.config.json.parsetimeintro')
        },
      )
      if (retPlaybackData.value.pagesec["parse.max.time"] === undefined) {
        let tempdefault = formData.tableDataJson.find(key => key.name === "parse.max.time")
        retPlaybackData.value.pagesec["parse.max.time"] = tempdefault ? tempdefault.default : null
        formDataJson.value["parse.max.time"] = tempdefault ? tempdefault.default : null
        formDataDb.value["parse.max.time"] = tempdefault ? tempdefault.default : null
      } else {
        formDataJson.value["parse.max.time"] = retPlaybackData.value.pagesec["parse.max.time"]
        formDataDb.value["parse.max.time"] = retPlaybackData.value.pagesec["parse.max.time"]
      }
    }
  }
  const indexExists = formData.tableDataPlayback.some(item => item.index === 12)
  if (retPlaybackData.value["sql.transcribe.mode"] === 'tcpdump') {
    if (!indexExists) {
      formData.tableDataPlayback.push(
        {
          index: 12, name: 'replay.max.time', default: 0, value: '', prop: 'replay.max.time', type: 'int',
          intro: t('transcribe.create.replaytimecon'),
        })
    }
  } else {
    formData.tableDataPlayback = formData.tableDataPlayback.filter(key => key.name !== "replay.max.time")
  }
}

const open = async (taskRetInfo, taskType, taskVersion) => {
  isVersionNeed.value = taskVersion
  taskTypeNum.value = taskType
  retPlaybackData.value.pagefir = taskRetInfo.pagefir
  retPlaybackData.value.pagesec = taskRetInfo.pagesec
  retPlaybackData.value.pagethi = taskRetInfo.pagethi
  retPlaybackData.value["sql.transcribe.mode"] = taskRetInfo["sql.transcribe.mode"]
  retPlaybackData.value["sql.storage.mode"] = taskRetInfo["sql.storage.mode"]
  await deleteVersionOption(taskType)
  try {
    const res = await targetClusters()
    let clusterOptions = formData.tableDataDb.find(e => e.name === 'sql.database.db').options;
    clusterOptions.length = 0;
    const tempOptions = res.data.targetClusters.map(e => {
      return {
        label: `${e.clusterNodes[0]?.privateIp}:${e.clusterNodes[0]?.dbPort}`,
        value: e.clusterNodes[0]
      }
    })
    clusterOptions.push(...tempOptions)
  } catch (error) {
    console.error( error);
  } finally {
    if (taskType === 0 || taskType === 1) {
      activeStep.value = 0
      formData.mode = retPlaybackData.value['sql.transcribe.mode']
      editChooseMode(retPlaybackData.value['sql.transcribe.mode'])
    } else {
      activeStep.value = 1
      formData.mode = retPlaybackData.value['sql.storage.mode']
      editChooseMode(retPlaybackData.value['sql.storage.mode'])
    }
    visible.value = true
  }
}

const selectChange = async (name, dbEntity) => {
  if (name === 'sql.database.db') {
    const {dbUser, dbPort, privateIp, dbUserPassword} = dbEntity;
    formDataModel.value['sql.database.ip'] = privateIp;
    formDataModel.value['sql.database.port'] = dbPort;
    formDataModel.value['sql.database.username'] = dbUser;
    formDataModel.value['sql.database.password'] = dbUserPassword;

    let databaseOptions = formData.tableDataDb.find(e => e.name === 'sql.database.name').options;
    databaseOptions.length = 0;
    const res = await targetClusterDbsData(dbEntity);
    const tempOptions = res.data.map(e => {
      return {
        label: e.dbName,
        value: e.dbName
      }
    });
    databaseOptions.push(...tempOptions);
  }
}
defineExpose({
  open
})

</script>

<style lang="less" scoped>
@import '@/assets/style/openGlobal.less';

.title-con {
  width: 540px;
  display: flex;
  justify-content: space-between;
  align-items: center;

  .params-info {
    font-size: 14px;
    font-weight: normal;

    b {
      color: rgb(var(--primary-6));
    }
  }
}

.config-con {
  .diy-info-con {
    margin-bottom: 10px;
  }

  .basic-config-con {
    .basic-title {
      padding-left: 13px;
      margin-bottom: 10px;
    }

    :deep(.row-changed) {
      .arco-table-td {
        background: var(--color-neutral-3);
      }
    }
  }

  .super-config-con {
    margin-top: 10px;

    :deep(.arco-collapse-item-content) {
      padding-left: 0;
      padding-right: 0;
      background-color: transparent;

      .arco-collapse-item-content-box {
        padding: 0;
      }
    }

    :deep(.row-changed) {
      .arco-table-td {
        background: var(--color-neutral-3);
      }
    }
  }
}

:deep(.arco-form-item) {
  margin-bottom: 0;
}

:deep(.arco-col-5) {
  flex: none;
  width: auto;
}

.single-line-text {
  width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

:deep(.el-input-number .el-input__inner) {
  text-align: left;
}

.table-container {
  transition: height 0.3s ease;
}

</style>
