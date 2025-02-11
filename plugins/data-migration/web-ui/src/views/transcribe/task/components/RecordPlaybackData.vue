<template>
  <a-drawer v-model:visible="visible" width="50vw" unmount-on-close style="margin-left: calc(100% - 100vw)">
    <template #title>
      <div class="title-con">
        <span class="params-title">录制回放参数配置</span>
      </div>
    </template>
    <el-tabs v-model="activeStep" class="demo-tabs" :before-leave="beforeTabLeave">
      <el-tab-pane v-if="taskTypeNum != 2" label="录制解析" :name="0"/>
      <el-tab-pane v-if="taskTypeNum != 1" label="存储方式" :name="1"/>
      <el-tab-pane v-if="taskTypeNum != 1" label="回放" :name="2"/>
    </el-tabs>
    <p></p>
    <div class="config-con" id="mainForm">
      <div class="basic-config-con" v-if="activeStep < 2">
        <div class="basic-params-table">
          <el-form :model="formModeSep" class="o-form-has-require" label-position="left" style="text-align: left;">
            <el-table border :data="formData.tableData.filter((v) => v.index === activeStep)"
                      :columns="formData.columns"
                      :row-style="{height:'70px' }" :cell-style="{ padding:'0px' }" :hoverable="false"
                      :bordered="true" :pagination="false" style="margin-right:50px"
            >
              <el-table-column label="参数名称" prop="name">
                <template #default="{ row }">
                  <span>{{ row.name }}</span>
                </template>
              </el-table-column>
              <el-table-column label="默认值" aria-label="left">
                <template #default="{ row }">
                  <el-form-item :prop="row.name" style="width:100%">
                    <div v-if="activeStep === 0">
                      {{ retPlaybackData["sql.transcribe.mode"] }}
                    </div>
                    <div v-else>
                      <el-select v-model="formData.mode" placeholder="请选择" controls-position="right"
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
              <el-table-column label="参数说明" prop="intro">
                <template #default="{ row }">
                  <el-tooltip class="item" effect="dark" :content="row.intro" placement="top">
                    <span class="single-line-text" :title="row.intro">{{ row.intro }}</span>
                  </el-tooltip>
                </template>
              </el-table-column>
            </el-table>
          </el-form>
        </div>
      </div>
      <div class="basic-config-con">
        <div class="basic-title">{{ formData.mode }}配置</div>
        <div class="basic-params-table">
          <el-form :model="formDataModel" :rules="formRules" ref="formRef" class="o-form-has-require, table-container"
                   label-position="left"
          >
            <el-table border :data="formTableData" :columns="formData.columns" :row-style="{height:'70px' }"
                      :cell-style="{ padding:'0px' }" :hoverable="false" :bordered="true" :pagination="false"
                      ref="tableRef" style="margin-right:50px"
            >
              <el-table-column label="参数名称" prop="name">
                <template #default="{ row }">
                  <span>{{ row.name }}</span>
                </template>
              </el-table-column>
              <el-table-column label="默认值" style="text-align: left;">
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
                      <el-select v-model="formDataModel[row.name]" placeholder="请选择" controls-position="right"
                                 append-to="#mainForm" style="text-align: left; width: 90%" value-key="nodeId"
                                 @change="dbEntity => selectChange(row.name, dbEntity)"
                                 :disabled="(retPlaybackData['sql.transcribe.mode'] !== 'tcpdump' && row.name === 'sql.replay.strategy')
                          || (retPlaybackData['sql.transcribe.mode'] !== 'tcpdump' && row.name === 'compare.select.result')">
                        <el-option v-for="(option, i) in row.options" :key="i" :label="option.label"
                                   :value="option.value"/>
                      </el-select>
                    </template>
                    <template v-else-if="row.type === 'date'">
                      <el-date-picker v-model="formDataModel[row.name]" type="date" style="text-align: left; width: 90%"
                                      placeholder="选择日期"/>
                    </template>
                    <template v-else>
                      <el-input v-model="formDataModel[row.name]" :placeholder="'请输入' + row.name"
                                style="text-align: left;width:90%"/>
                    </template>
                  </el-form-item>
                </template>
              </el-table-column>
              <el-table-column label="参数说明" prop="intro">
                <template #default="{ row }">
                  <el-tooltip class="item" effect="dark" :content="row.intro" placement="top">
                    <span class="single-line-text" :title="row.intro">{{ row.intro }}</span>
                  </el-tooltip>
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
          恢复当前页默认值
        </el-button>
        <el-button @click="saveParams">保存</el-button>
      </div>
    </template>
  </a-drawer>
</template>

<script setup>
import {ref, reactive} from 'vue'
import {useI18n} from 'vue-i18n'
import {targetClusters, targetClusterDbsData} from '@/api/task'

const formData = reactive({
  steps: [{type: Object,}],
  current: [{type: Number,}],
  columns: [
    {key: 'name', title: '参数名称', dataIndex: 'name'},
    {key: 'default', title: '默认值', dataIndex: 'default'},
    {key: 'intro', title: '输入框', dataIndex: 'intro'},
  ],
  tableData: [
    {
      index: 0, name: 'sql.transcribe.mode', default: 'tcpdump', value: '', prop: 'sql.transcribe.mode', type: 'select',
      intro: '可选tcpdump/attach/general，分别表示流量采集，动态插桩与查询系统表的录制方式',
      options: [
        {index: 0, label: 'tcpdump', value: 'tcpdump'},
        {index: 1, label: 'attach', value: 'attach'},
        {index: 2, label: 'general', value: 'general'},]
    },
    {
      index: 1, name: 'sql.storage.mode', default: 'json', value: '', prop: 'sql.storage.mode', type: 'select',
      intro: 'sql存储方式，可选json或db',
      options: [
        {index: 0, label: 'json', value: 'json'},
        {index: 1, label: 'db', value: 'db'},]
    },
  ],
  tableDataTcp: [
    {
      index: 4, name: 'tcpdump.file.name', default: 'tcpdump-file', value: '', prop: 'tcpdump.file.name', type: 'input',
      intro: '网络文件包名'
    },
    {
      index: 5, name: 'tcpdump.file.size', default: 10, value: '', prop: 'tcpdump.file.size', type: 'int',
      intro: '单个网络文件包大小限制，单位: MB'
    },
    {
      index: 8, name: 'queue.size.limit', default: 10000, value: '', prop: 'queue.size.limit', type: 'int',
      intro: '解析时限定每次读取的最大报文条数'
    },
    {
      index: 9, name: 'packet.batch.size', default: 10000, value: '', prop: 'packet.batch.size', type: 'int',
      intro: '解析时每次提交sql所处理的报文条数'
    },
    {
      index: 11, name: 'remote.retry.count', default: 1, value: '', prop: 'remote.retry.count', type: 'int',
      intro: '发送失败重试次数，项远程主机发送文件允许的发送失败次数，超过该次数则停止录制'
    },
    {
      index: 13, name: 'should.check.system', default: true, value: '', prop: 'should.check.system', type: 'select',
      intro: '是否检查系统资源利用率',
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
    {
      index: 14, name: 'max.cpu.threshold', default: 0.85, value: '', prop: 'max.cpu.threshold', type: 'float',
      intro: '系统CPU使用率阈值，取值在0~1之间，当系统CPU使用率超过该值时，工具会停止录制'
    },
    {
      index: 15, name: 'max.memory.threshold', default: 0.85, value: '', prop: 'max.memory.threshold', type: 'float',
      intro: ' 系统内存使用率阈值，取值在0~1之间，当系统内存使用率超过该值时，工具会停止录制'
    },
    {
      index: 16, name: 'max.disk.threshold', default: 0.85, value: '', prop: 'max.disk.threshold', type: 'float',
      intro: '磁盘使用率阈值，取值在0~1之间，当存储文件的磁盘占用率超过该值时，工具会停止录制'
    },
    {
      index: 17, name: 'result.file.size', default: 10, value: '', prop: 'result.file.size', type: 'int',
      intro: 'select语句查询结果保存文件大小，单位: MB'
    },
    {
      index: 18, name: 'parse.select.result', default: false, value: '', prop: 'parse.select.result', type: 'select',
      intro: '是否解析select语句查询结果，该功能用于对比录制端和回放端的查询结果',
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
  ],
  tableDataAtt: [
    {
      index: 7, name: 'max.cpu.threshold', default: 0.85, value: '', prop: 'max.cpu.threshold', type: 'float',
      intro: '系统CPU使用率阈值，取值在0~1之间，当系统CPU使用率超过该值时，工具会停止录制'
    },
    {
      index: 8, name: 'max.memory.threshold', default: 0.85, value: '', prop: 'max.memory.threshold', type: 'float',
      intro: ' 系统内存使用率阈值，取值在0~1之间，当系统内存使用率超过该值时，工具会停止录制'
    },
    {
      index: 9, name: 'max.disk.threshold', default: 0.85, value: '', prop: 'max.disk.threshold', type: 'float',
      intro: '磁盘使用率阈值，取值在0~1之间，当存储文件的磁盘占用率超过该值时，工具会停止录制'
    },
    {
      index: 12, name: 'remote.retry.count', default: 1, value: '', prop: 'remote.retry.count', type: 'int',
      intro: '发送失败重试次数，项远程主机发送文件允许的发送失败次数，超过该次数则停止录制'
    },
    {
      index: 14, name: 'should.check.system', default: true, value: '', prop: 'should.check.system', type: 'select',
      intro: '是否检查系统资源利用率',
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
    {
      index: 17, name: 'result.file.size', default: 10, value: '', prop: 'result.file.size', type: 'int',
      intro: 'select语句查询结果保存文件大小，单位: MB'
    },
    {
      index: 18, name: 'parse.select.result', default: false, value: '', prop: 'parse.select.result', type: 'select',
      intro: '是否解析select语句查询结果，该功能用于对比录制端和回放端的查询结果',
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
  ],
  tableDataGen: [
    {
      index: 0, name: 'general.sql.batch', default: 1000, value: '', prop: 'general.sql.batch', type: 'int',
      intro: '每次查询的数据条数'
    },
    {
      index: 1,
      name: 'general.start.time',
      default: new Date('1970-01-01').toISOString().replace('T', ' ').substring(0, 19),
      value: '',
      prop: 'general.start.time',
      type: 'date',
      intro: ' general log采集sql工具开始的时间'
    },
    {
      index: 17, name: 'result.file.size', default: 10, value: '', prop: 'result.file.size', type: 'int',
      intro: 'select语句查询结果保存文件大小，单位: MB'
    },
    {
      index: 18, name: 'parse.select.result', default: false, value: '', prop: 'parse.select.result', type: 'select',
      intro: '是否解析select语句查询结果，该功能用于对比录制端和回放端的查询结果',
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
  ],
  tableDataJson: [
    {
      index: 1, name: 'sql.file.name', default: 'sql-file', value: '', prop: 'sql.file.name', type: 'input',
      intro: 'sql文件名'
    },
    {
      index: 2, name: 'sql.file.size', default: 10, value: '', prop: 'sql.file.size', type: 'int',
      intro: 'sql文件大小限制，单位: MB'
    },
    {
      index: 4, name: 'result.file.name', default: 'select-result', value: '', prop: 'result.file.name', type: 'input',
      intro: 'select语句查询结果保存文件名称'
    },
    {
      index: 5, name: 'parse.max.time', default: 0, value: '', prop: 'parse.max.time', type: 'int',
      intro: '解析进程的总执行时间，从进程启动开始计算，为0表示进程一直持续直到收到结束标识，单位: 分钟'
    },
  ],
  tableDataDb: [
    {
      index: 0, name: 'sql.database.db', default: '', value: '', prop: 'sql.database.db', type: 'select',
      options: [],
      intro: 'sql存储库实例'
    },
    {
      index: 1, name: 'sql.database.name', default: '', value: '', prop: 'sql.database.name', type: 'select',
      intro: 'sql存储库名称',
      options: []
    },
    {
      index: 2, name: 'sql.table.name', default: 'sql_table', value: '', prop: 'sql.table.name', type: 'input',
      intro: '存储sql的表名称'
    },
    {
      index: 3, name: 'sql.table.drop', default: 'false', value: '', prop: 'sql.table.drop', type: 'select',
      intro: '存储sql的表名称弱于数据库中已有表名一致，是否删除已有表',
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
    {
      index: 4, name: 'result.file.name', default: 'select-result', value: '', prop: 'result.file.name', type: 'select',
      intro: 'select语句查询结果保存文件名称'
    },
    {
      index: 5, name: 'parse.max.time', default: 0, value: '', prop: 'parse.max.time', type: 'int',
      intro: '解析进程的总执行时间，从进程启动开始计算，为0表示进程一直持续直到收到结束标识，单位: 分钟'
    },
  ],
  tableDataPlayback: [
    {
      index: 0, name: 'sql.replay.strategy', default: 'parallel', value: '', prop: 'sql.replay.strategy',
      type: 'select', intro: '回放策略 串行-serial 并行-parallel',
      options: [
        {index: 0, label: 'parallel', value: 'parallel'},
        {index: 1, label: 'serial', value: 'serial'},]
    },
    {
      index: 1, name: 'sql.replay.multiple', default: 1, value: '', prop: 'sql.replay.multiple', type: 'int',
      intro: 'N倍压测倍数',
    },
    {
      index: 2, name: 'sql.replay.only.query', default: false, value: '', prop: 'sql.replay.only.query', type: 'select',
      intro: '是否只回放查询语句',
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
    {
      index: 3, name: 'sql.replay.parallel.max.pool.size', default: 5, value: '',
      prop: 'sql.replay.parallel.max.pool.size', type: 'int', intro: '最大并行的session数',
    },
    {
      index: 4, name: 'sql.replay.slow.sql.rule', default: '2', value: '', prop: 'sql.replay.slow.sql.rule',
      type: 'input', intro: '慢SQL判定规则',
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
      intro: '慢SQL判定规则规则1(与MySQL时间差距，单位：微秒)',
    },
    {
      index: 6, name: 'sql.replay.slow.sql.duration.threshold', default: 1000, value: '',
      prop: 'sql.replay.slow.sql.duration.threshold', type: 'int', intro: '慢SQL判定规则2:(openGauss执行耗时)',
    },
    {
      index: 8, name: 'sql.replay.slow.top.number', default: 5, value: '', prop: 'sql.replay.slow.top.number',
      type: 'int', intro: '慢SQL打印TOPN',
    },
    {
      index: 8, name: 'sql.replay.draw.interval', default: 100, value: '', prop: 'sql.replay.draw.interval',
      type: 'int', intro: 'MySQL和openGauss执行时间对比图采样间隔',
    },
    {
      index: 10, name: 'sql.replay.session.white.list', default: '[]', value: '', prop: 'sql.replay.session.white.list',
      type: 'input', intro: '回放session白名单， 格式: 192.168.0.1，192.168.0.1:8888，' +
        '[2407:c080:1200:22a0:a09f:d625:2787:800b]或者[2407:c080:1200:22a0:a09f:d625:2787:800b]:54068， session之间用\';\'分隔',
    },
    {
      index: 11, name: 'sql.replay.session.black.list', default: '[]', value: '', prop: 'sql.replay.session.black.list',
      type: 'input', intro: '# 回放session黑名单， 格式: 192.168.0.1，192.168.0.1:8888，' +
        '[2407:c080:1200:22a0:a09f:d625:2787:800b]或者[2407:c080:1200:22a0:a09f:d625:2787:800b]:54068， session之间用\';\'分隔',
    },
    {
      index: 12, name: 'replay.max.time', default: 0, value: '', prop: 'replay.max.time', type: 'int',
      intro: '回放进程的总执行时间，从进程启动开始计算，为0表示进程一直持续直到收到结束标识，单位: 分钟',
    },
    {
      index: 13, name: 'source.time.interval.replay', default: false, value: '', prop: 'source.time.interval.replay',
      type: 'select', intro: '是否启用回放时间间隔和源端一致的功能，不启用则是连续快速回放模式',
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
    {
      index: 14, name: 'compare.select.result', default: false, value: '', prop: 'compare.select.result',
      type: 'select', intro: '是否将回放端和录制端的select查询结果对比',
      options: [
        {index: 0, label: 'true', value: true},
        {index: 1, label: 'false', value: false},]
    },
  ],
  mode: 'tcpdump',
})

const isVersionNeed = ref(false)
const formModeSep = ref('tcpdump')
const {t} = useI18n()
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
const formRulesTcp = reactive({
  'tcpdump.file.name': [
    {required: true, message: '没有填写网络文件包名', trigger: ['blur', 'change']},
    {max: 255, message: '网络文件包名不能超过 255 个字符', trigger: ['blur', 'change']},
  ],
  'tcpdump.file.size': [
    {required: true, message: '没有填写单个网络文件包大小限制', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: '单个网络文件包大小必须是数字', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: maxIntValue, message: '数字必须在 0 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
  'queue.size.limit': [
    {required: true, message: '没有填写解析时限定每次读取的最大报文条数', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: '每次读取的最大报文条数必须是数字', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: maxIntValue, message: '数字必须在 0 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
  'packet.batch.size': [
    {required: true, message: '没有填写解析时每次提交sql所处理的报文条数', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: '所处理的报文条数必须是数字', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: maxIntValue, message: '数字必须在 0 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
  'max.cpu.threshold': [
    {required: true, message: '没有填写系统CPU使用率阈值', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: 1, message: '超过系统CPU使用率阈值', trigger: ['blur', 'change']},
  ],
  'max.memory.threshold': [
    {required: true, message: '没有填写系统内存使用率阈值', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: 1, message: '超过系统内存使用率阈值', trigger: ['blur', 'change']},
  ],
  'max.disk.threshold': [
    {required: true, message: '没有填写磁盘使用率阈值', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: 1, message: '超过磁盘使用率阈值', trigger: ['blur', 'change']},
  ],
  'result.file.size': [
    {required: isVersionNeed, message: '没有填写select语句查询结果保存文件大小', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: 'select语句查询结果保存文件大小必须是数字', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: maxIntValue, message: '数字必须在 0 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
  'parse.select.result': [
    {required: isVersionNeed, message: '没有填写是否解析select语句查询结果', trigger: ['blur', 'change']},
  ],
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
const formRulesAtt = reactive({
  'max.cpu.threshold': [
    {required: true, message: '没有填写系统CPU使用率阈值', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: 1, message: '超过系统CPU使用率阈值', trigger: ['blur', 'change']},
  ],
  'max.memory.threshold': [
    {required: true, message: '没有填写系统内存使用率阈值', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: 1, message: '超过系统内存使用率阈值', trigger: ['blur', 'change']},
  ],
  'max.disk.threshold': [
    {required: true, message: '没有填写磁盘使用率阈值', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: 1, message: '超过磁盘使用率阈值', trigger: ['blur', 'change']},
  ],
  'remote.retry.count': [
    {required: true, message: '没有填写发送失败重试次数', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: '发送失败重试次数必须是数字', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: maxIntValue, message: '数字必须在 0 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
  'result.file.size': [
    {required: isVersionNeed, message: '没有填写select语句查询结果保存文件大小', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: 'select语句查询结果保存文件大小必须是数字', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: maxIntValue, message: '数字必须在 0 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
  'parse.select.result': [
    {required: isVersionNeed, message: '没有填写是否解析select语句查询结果', trigger: ['blur', 'change']},
  ],
})

const formRefGen = ref(null)
const formDataGen = ref({
  'general.sql.batch': '',
  'general.start.time': '',
  'result.file.size': '',
  'parse.select.result': '',
})
const formRulesGen = reactive({
  'general.sql.batch': [
    {required: true, message: '没有每次查询的数据条数', trigger: ['blur', 'change']},
    {type: 'number', min: '1', message: '数据条数不能少于 1 条', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: '每次查询的数据条数必须是数字', trigger: ['blur', 'change']},
    {type: 'number', max: maxIntValue, message: '数字必须在 1 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
  'general.start.time': [
    {required: true, message: '没有填写采集sql工具开始的时间', trigger: ['blur', 'change']},
    {
      validator: (rule, value, callback) => {
        const minDate = new Date('1970-01-01')
        if (value && new Date(value) < minDate) {
          callback(new Error('日期必须在1970年1月1日之后'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  'result.file.size': [
    {required: isVersionNeed, message: '没有填写select语句查询结果保存文件大小', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: 'select语句查询结果保存文件大小必须是数字', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: maxIntValue, message: '数字必须在 0 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
  'parse.select.result': [
    {required: isVersionNeed, message: '没有填写是否解析select语句查询结果', trigger: ['blur', 'change']},
  ],
})

const formRefJson = ref(null)
const formDataJson = ref({
  'sql.file.name': '',
  'sql.file.size': '',
  'result.file.name': '',
  'parse.max.time': 0,
})

const formRulesJson = reactive({
  'sql.file.name': [
    {required: true, message: '没有填写sql文件名', trigger: ['blur', 'change']},
    {max: 255, message: '网络文件包名不能超过 255 个字符', trigger: ['blur', 'change']},
  ],
  'sql.file.size': [
    {required: true, message: '没有填写sql文件包大小限制', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: '单个网络文件包大小必须是数字', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: maxIntValue, message: '数字必须在 0 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
  'parse.max.time': [
    {required: true, message: '没有填写解析进程的总执行时间', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: '解析进程的总执行时间必须是数字', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: maxIntValue, message: '数字必须在 0 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
  'result.file.name': [
    {required: isVersionNeed, message: '没有填写select语句查询结果保存文件名称', trigger: ['blur', 'change']},
  ],
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
const formRulesDb = reactive({
  'sql.database.db': [
    {required: true, message: '没有选择sql存储库实例', trigger: ['blur', 'change']},
  ],
  'sql.database.name': [
    {required: true, message: '没有填写sql存储库名称', trigger: ['blur', 'change']},
    {max: 255, message: '存储库名称不能超过 255 个字符', trigger: ['blur', 'change']},
  ],
  'sql.table.name': [
    {required: true, message: '没有填写存储aql表的名称', trigger: ['blur', 'change']},
    {max: 255, message: '网络文件包名不能超过 255 个字符', trigger: ['blur', 'change']},
  ],
  'sql.table.drop': [
    {required: true, message: '没有填写若名称相同是否删除已有表', trigger: ['blur', 'change']},
  ],
  'result.file.name': [
    {required: isVersionNeed, message: '没有填写select语句查询结果保存文件名称', trigger: ['blur', 'change']},
    {max: 255, message: '保存文件名称不能超过 255 个字符', trigger: ['blur', 'change']},
  ],
  'parse.max.time': [
    {required: true, message: '没有填写解析进程的总执行时间', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: '解析进程的总执行时间必须是数字', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: maxIntValue, message: '数字必须在 0 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
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

const formRulesPlayback = reactive({
  'sql.replay.strategy': [
    {required: true, message: '没有填写回放策略', trigger: ['blur', 'change']},
  ],
  'sql.replay.multiple': [
    {required: true, message: '没有填写压测倍数', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: '压测倍数必须为数字', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: maxIntValue, message: '数字必须在 0 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
  'sql.replay.only.query': [
    {required: true, message: '没有填写是否只回放查询语句', trigger: ['blur', 'change']},
  ],
  'sql.replay.parallel.max.pool.size': [
    {required: true, message: '没有填写最大并行的session数', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: '最大并行的session数必须为数字', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: maxIntValue, message: '数字必须在 0 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
  'sql.replay.slow.sql.rule': [
    {required: true, message: '没有填写慢SQL判定规则', trigger: ['blur', 'change']},
  ],
  'sql.replay.slow.time.difference.threshold': [
    {required: true, message: '没有填写慢SQL判定规则规则1(与MySQL时间差距，单位：微秒)', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: '与MySQL时间差距必须为数字', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: maxIntValue, message: '数字必须在 0 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
  'sql.replay.slow.sql.duration.threshold': [
    {required: true, message: '没有填写慢SQL判定规则2(openGauss执行耗时)', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: 'openGauss执行耗时必须为数字', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: maxIntValue, message: '数字必须在 0 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
  'sql.replay.slow.top.number': [
    {required: true, message: '没有填写慢SQL打印TOPN', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: '慢SQL打印TOPN必须为数字', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: maxIntValue, message: '数字必须在 0 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
  'sql.replay.session.white.list': [
    {required: true, message: '没有填写回放session白名单', trigger: ['blur', 'change']},
    {max: 255, message: '保存文件名称不能超过 255 个字符', trigger: ['blur', 'change']},
  ],
  'sql.replay.session.black.list': [
    {required: true, message: '没有填写回放session黑名单', trigger: ['blur', 'change']},
    {max: 255, message: '保存文件名称不能超过 255 个字符', trigger: ['blur', 'change']},
  ],
  'sql.replay.draw.interval': [
    {required: true, message: '没有填写对比图采样间隔', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: '慢SQL打印TOPN必须为数字', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: maxIntValue, message: '数字必须在 0 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
  'replay.max.time': [
    {required: isVersionNeed, message: '没有填写回放进程的总执行时间', trigger: ['blur', 'change']},
    {pattern: /^[0-9]*$/, message: '回放进程的总执行时间必须为数字', trigger: ['blur', 'change']},
    {type: 'number', min: 0, max: maxIntValue, message: '数字必须在 0 到 2147483647 之间', trigger: ['blur', 'change']}
  ],
  'source.time.interval.replay': [
    {required: isVersionNeed, message: '没有填写是否启用回放时间间隔和源端一致的功能', trigger: ['blur', 'change']},
  ],
  'compare.select.result': [
    {required: isVersionNeed, message: '没有填写是否将回放端和录制端的select查询结果对比', trigger: ['blur', 'change']},
  ],
})

const formRef = ref(null)
const formRules = reactive([])
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
      Object.keys(formRules).forEach(key => delete formRules[key])
      Object.assign(formRules, formRulesTcp)
      break;
    case 'attach':
      formRef.value = formRefAtt.value
      formDataModel.value = formDataAtt.value
      formTableData.splice(0, formTableData.length, ...formData.tableDataAtt)
      formData.tableDataAtt.forEach((item) => {
        formDataModel.value[item.prop] = item.default
        item.value = item.default
      })
      Object.keys(formRules).forEach(key => delete formRules[key])
      Object.assign(formRules, formRulesAtt)
      break;

    case 'general':
      formRef.value = formRefGen.value
      formDataModel.value = formDataGen.value
      formTableData.splice(0, formTableData.length, ...formData.tableDataGen)
      formData.tableDataGen.forEach((item) => {
        formDataModel.value[item.prop] = item.default
        item.value = item.default
      })
      Object.keys(formRules).forEach(key => delete formRules[key])
      Object.assign(formRules, formRulesGen)
      break;

    case 'json':
      formRef.value = formRefJson.value
      formDataModel.value = formDataJson.value
      formTableData.splice(0, formTableData.length, ...formData.tableDataJson)
      formData.tableDataJson.forEach((item) => {
        formDataModel.value[item.prop] = item.default
        item.value = item.default
      })
      Object.keys(formRules).forEach(key => delete formRules[key])
      Object.assign(formRules, formRulesJson)
      break;

    case 'db':
      formRef.value = formRefDb.value
      formDataModel.value = formDataDb.value
      formTableData.splice(0, formTableData.length, ...formData.tableDataDb)
      formData.tableDataDb.forEach((item) => {
        formDataModel.value[item.prop] = item.default
        item.value = item.default
      })
      Object.keys(formRules).forEach(key => delete formRules[key])
      Object.assign(formRules, formRulesDb)
      break;

    default:
      formRef.value = formRefPlayback.value
      formDataModel.value = formDataPlayback.value
      formTableData.splice(0, formTableData.length, ...formData.tableDataPlayback)
      formData.tableDataPlayback.forEach((item) => {
        formDataModel.value[item.prop] = item.default
        item.value = item.default
      })
      Object.keys(formRules).forEach(key => delete formRules[key])
      Object.assign(formRules, formRulesPlayback)
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
      Object.keys(formRules).forEach(key => delete formRules[key])
      Object.assign(formRules, formRulesTcp)
      break;

    case 'attach':
      formRef.value = formRefAtt.value
      formDataModel.value = retPlaybackData.value.pagefir
      formTableData.splice(0, formTableData.length, ...formData.tableDataAtt)
      Object.keys(formRules).forEach(key => delete formRules[key])
      Object.assign(formRules, formRulesAtt)
      break;

    case 'general':
      formRef.value = formRefGen.value
      formDataModel.value = retPlaybackData.value.pagefir
      formTableData.splice(0, formTableData.length, ...formData.tableDataGen)
      Object.keys(formRules).forEach(key => delete formRules[key])
      Object.assign(formRules, formRulesGen)
      break;

    case 'json':
      formRef.value = formRefJson.value
      formDataModel.value = retPlaybackData.value.pagesec
      formTableData.splice(0, formTableData.length, ...formData.tableDataJson)
      Object.keys(formRules).forEach(key => delete formRules[key])
      Object.assign(formRules, formRulesJson)
      break;

    case 'db':
      formRef.value = formRefDb.value
      formDataModel.value = retPlaybackData.value.pagesec
      formTableData.splice(0, formTableData.length, ...formData.tableDataDb)
      Object.keys(formRules).forEach(key => delete formRules[key])
      Object.assign(formRules, formRulesDb)
      break;

    default:
      formRef.value = formRefPlayback.value
      formDataModel.value = retPlaybackData.value.pagethi
      formTableData.splice(0, formTableData.length, ...formData.tableDataPlayback)
      Object.keys(formRules).forEach(key => delete formRules[key])
      Object.assign(formRules, formRulesPlayback)
      break;
  }
  modeLoading.value = false
}

const taskTypeNum = ref(0)

const isVersionSet = ref(["result.file.size", "parse.select.result", "result.file.name", "replay.max.time",
  "source.time.interval.replay", "compare.select.result", "parse.max.time"])
const deleteVersionOption = () => {
  if (isVersionNeed.value === false) {
    isVersionSet.value.forEach(item => {
      if (item === "result.file.size" || item === "parse.select.result") {
        delete retPlaybackData.value.pagefir[item]
        formData.tableDataTcp = formData.tableDataTcp.filter(key => key.name !== item)
        formData.tableDataAtt = formData.tableDataAtt.filter(key => key.name !== item)
        formData.tableDataGen = formData.tableDataGen.filter(key => key.name !== item)
        delete formDataTcp.value[item]
        delete formDataAtt.value[item]
        delete formDataGen.value[item]
      } else if (item === "result.file.name") {
        delete retPlaybackData.value.pagesec[item]
        formData.tableDataJson = formData.tableDataJson.filter(key => key.name !== item)
        formData.tableDataDb = formData.tableDataDb.filter(key => key.name !== item)
        delete formDataJson.value[item]
        delete formDataDb.value[item]
      } else {
        delete retPlaybackData.value.pagethi[item]
        formData.tableDataPlayback = formData.tableDataPlayback.filter(key => key.name !== item)
        delete formDataPlayback.value[item]
      }
    })
  } else {
    const indexExists = formData.tableDataTcp.some(item => item.index === 17)
    if (!indexExists) {
      formData.tableDataTcp.push(
        {
          index: 17, name: 'result.file.size', default: 10, value: '', prop: 'result.file.size', type: 'int',
          intro: 'select语句查询结果保存文件大小，单位: MB'
        },
        {
          index: 18, name: 'parse.select.result', default: false, value: '', prop: 'parse.select.result',
          type: 'select', intro: '是否解析select语句查询结果，该功能用于对比录制端和回放端的查询结果',
          options: [
            {index: 0, label: 'true', value: true},
            {index: 1, label: 'false', value: false},]
        },
      )
      formData.tableDataAtt.push(
        {
          index: 17, name: 'result.file.size', default: 10, value: '', prop: 'result.file.size', type: 'int',
          intro: 'select语句查询结果保存文件大小，单位: MB'
        },
        {
          index: 18, name: 'parse.select.result', default: false, value: '', prop: 'parse.select.result',
          type: 'select', intro: '是否解析select语句查询结果，该功能用于对比录制端和回放端的查询结果',
          options: [
            {index: 0, label: 'true', value: true},
            {index: 1, label: 'false', value: false},]
        },
      )
      formData.tableDataGen.push(
        {
          index: 17, name: 'result.file.size', default: 10, value: '', prop: 'result.file.size', type: 'int',
          intro: 'select语句查询结果保存文件大小，单位: MB'
        },
        {
          index: 18, name: 'parse.select.result', default: false, value: '', prop: 'parse.select.result',
          type: 'select', intro: '是否解析select语句查询结果，该功能用于对比录制端和回放端的查询结果',
          options: [
            {index: 0, label: 'true', value: true},
            {index: 1, label: 'false', value: false},]
        },
      )
      formData.tableDataJson.push(
        {
          index: 4, name: 'result.file.name', default: 'select-result', value: '', prop: 'result.file.name',
          type: 'input', intro: 'select语句查询结果保存文件名称'
        },
        {
          index: 5, name: 'parse.max.time', default: 0, value: '', prop: 'parse.max.time', type: 'int',
          intro: '解析进程的总执行时间，从进程启动开始计算，为0表示进程一直持续直到收到结束标识，单位: 分钟'
        },
      )
      formData.tableDataDb.push(
        {
          index: 4, name: 'result.file.name', default: 'select-result', value: '', prop: 'result.file.name',
          type: 'input', intro: 'select语句查询结果保存文件名称'
        },
        {
          index: 5, name: 'parse.max.time', default: 0, value: '', prop: 'parse.max.time', type: 'int',
          intro: '解析进程的总执行时间，从进程启动开始计算，为0表示进程一直持续直到收到结束标识，单位: 分钟'
        },
      )
      if (taskRetInfo["sql.transcribe.mode"] === 'tcpdump') {
        formData.tableDataPlayback.push(
          {
            index: 12, name: 'replay.max.time', default: 0, value: '', prop: 'replay.max.time', type: 'int',
            intro: '回放进程的总执行时间，从进程启动开始计算，为0表示进程一直持续直到收到结束标识，单位: 分钟',
          })
      }
      formData.tableDataPlayback.push(
        {
          index: 13,
          name: 'source.time.interval.replay',
          default: false,
          value: '',
          prop: 'source.time.interval.replay',
          type: 'select',
          intro: '是否启用回放时间间隔和源端一致的功能，不启用则是连续快速回放模式',
          options: [
            {index: 0, label: 'true', value: true},
            {index: 1, label: 'false', value: false},]
        },
        {
          index: 14, name: 'compare.select.result', default: false, value: '', prop: 'compare.select.result',
          type: 'select', intro: '是否将回放端和录制端的select查询结果对比',
          options: [
            {index: 0, label: 'true', value: true},
            {index: 1, label: 'false', value: false},]
        },
      )
      isVersionSet.value.forEach(item => {
        if (item === "result.file.size" || item === "parse.select.result") {
          if (retPlaybackData.value.pagefir[item] === undefined) {
            let tempdefault = formData.tableDataTcp.find(key => key.name === item)
            retPlaybackData.value.pagefir[item] = tempdefault ? tempdefault.default : null
            formDataTcp.value[item] = tempdefault ? tempdefault.default : null
            formDataAtt.value[item] = tempdefault ? tempdefault.default : null
            formDataGen.value[item] = tempdefault ? tempdefault.default : null
          } else {
            formDataTcp.value[item] = null
            formDataAtt.value[item] = null
            formDataGen.value[item] = null
          }
        } else if (item === "result.file.name") {
          if (retPlaybackData.value.pagefir[item] === undefined) {
            let tempdefault = formData.tableDataJson.find(key => key.name === item)
            retPlaybackData.value.pagesec[item] = tempdefault ? tempdefault.default : null
            formDataJson.value[item] = tempdefault ? tempdefault.default : null
            formDataDb.value[item] = tempdefault ? tempdefault.default : null
          } else {
            formDataJson.value[item] = null
            formDataDb.value[item] = null
          }
        } else {
          if (retPlaybackData.value.pagefir[item] === undefined) {
            let tempdefault = formData.tableDataPlayback.find(key => key.name === item)
            retPlaybackData.value.pagethi[item] = tempdefault ? tempdefault.default : null
            formDataPlayback.value[item] = tempdefault ? tempdefault.default : null
          } else {
            formDataPlayback.value[item] = null
          }
        }
      })
    }
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
  await deleteVersionOption()
  if (taskType === 0) {
    activeStep.value = 0
    formData.mode = retPlaybackData.value['sql.transcribe.mode']
    editChooseMode(retPlaybackData.value['sql.transcribe.mode'])
  } else if (taskType === 1) {
    activeStep.value = 0
    formData.mode = retPlaybackData.value['sql.transcribe.mode']
    editChooseMode(retPlaybackData.value['sql.transcribe.mode'])
  } else {
    activeStep.value = 1
    formData.mode = retPlaybackData.value['sql.storage.mode']
    editChooseMode(retPlaybackData.value['sql.storage.mode'])
  }
  visible.value = true
  const res = await targetClusters();
  let clusterOptions = formData.tableDataDb.find(e => e.name === 'sql.database.db').options;
  clusterOptions.length = 0;
  const tempOptions = res.data.targetClusters.map(e => {
    return {
      label: `${e.clusterNodes[0]?.privateIp}:${e.clusterNodes[0]?.dbPort}`,
      value: e.clusterNodes[0]
    }
  })
  clusterOptions.push(...tempOptions)
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
