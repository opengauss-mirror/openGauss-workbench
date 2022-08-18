<template>
  <div class="home-container">
    <div class="tips-con">
      <a-collapse :default-active-key="[]">
        <template #expand-icon>
          <icon-exclamation-circle-fill size="16" />
        </template>
        <a-collapse-item :header="$t('home.index.5mq3g65qkhw0')" key="1">
          <div class="tips"><span class="num">1. </span><span class="desc">{{$t('home.index.5mq3g65qla00')}}</span></div>
          <div class="tips"><span class="num">2. </span><span class="desc">{{$t('home.index.5mq3g65qlew0')}}</span></div>
          <div class="code" v-html="code"></div>
          <div class="tips"><span class="num">3. </span><span class="desc">{{$t('home.index.5mq3d6o5yvc0')}}</span></div>
          <div class="tips"><span class="num">4. </span><span class="desc">{{$t('home.index.5mq3d6o5yzo0')}}</span></div>
        </a-collapse-item>
      </a-collapse>
    </div>
    <div class="form-con">
      <div class="form-left">
        <a-card :style="{ width: '400px', height: '400px'}" :title="$t('home.index.5mq3hrx8s6o0')" bordered>
          <a-form ref="formLeftRef" :model="formLeft" auto-label-width>
            <a-form-item field="host" :label="$t('home.index.5mq3d6o5z6g0')" :rules="[{
              required: true,
              message: $t('home.index.5mq3d6o5zb00')
            }]">
              <a-input v-model="formLeft.host" :placeholder="$t('home.index.5mq3d6o5zjk0')" />
            </a-form-item>
            <a-form-item field="port" :label="$t('home.index.5mq3d6o608g0')" :rules="[{
              required: true,
              message: $t('home.index.5mq3d6o60gk0')
            }]">
              <a-input v-model="formLeft.port" :placeholder="$t('home.index.5mq3d6o60mc0')" />
            </a-form-item>
            <a-form-item field="user" :label="$t('home.index.5mq3d6o60po0')" :rules="[{
              required: true,
              message: $t('home.index.5mq3d6o60t00')
            }]">
              <a-input v-model="formLeft.user" :placeholder="$t('home.index.5mq3d6o60vw0')" />
            </a-form-item>
            <a-form-item field="password" :label="$t('home.index.5mq3d6o612g0')" :rules="[{
              required: true,
              message: $t('home.index.5mq3d6o615g0')
            }]">
              <a-input-password v-model="formLeft.password" :placeholder="$t('home.index.5mq3d6o62lc0')" />
            </a-form-item>
            <a-form-item field="schema" label="Database" :rules="[{
              required: true,
              message: $t('home.index.5mq3h1j5ees0')
            }]">
              <a-input v-model="formLeft.schema" :placeholder="$t('home.index.5mq3d6o66ys0')" />
            </a-form-item>
          </a-form>
        </a-card>
      </div>
      <div class="form-center">
        <icon-arrow-right size="40" />
      </div>
      <div class="form-right">
        <a-card :style="{ width: '400px', height: '400px' }" :title="$t('home.index.5mq3d6o677g0')" bordered>
          <a-form ref="formRightRef" :model="formRight" auto-label-width>
            <a-form-item field="host" :label="$t('home.index.5mq3d6o5z6g0')" :rules="[{
              required: true,
              message: $t('home.index.5mq3d6o5zb00')
            }]">
              <a-input v-model="formRight.host" :placeholder="$t('home.index.5mq3d6o5zjk0')" />
            </a-form-item>
            <a-form-item field="port" :label="$t('home.index.5mq3d6o608g0')" :rules="[{
              required: true,
              message: $t('home.index.5mq3d6o60gk0')
            }]">
              <a-input v-model="formRight.port" :placeholder="$t('home.index.5mq3d6o60mc0')" />
            </a-form-item>
            <a-form-item field="user" :label="$t('home.index.5mq3d6o60po0')" :rules="[{
              required: true,
              message: $t('home.index.5mq3d6o60t00')
            }]">
              <a-input v-model="formRight.user" :placeholder="$t('home.index.5mq3d6o60vw0')" />
            </a-form-item>
            <a-form-item field="password" :label="$t('home.index.5mq3d6o612g0')" :rules="[{
              required: true,
              message: $t('home.index.5mq3d6o615g0')
            }]">
              <a-input-password v-model="formRight.password" :placeholder="$t('home.index.5mq3d6o62lc0')" />
            </a-form-item>
            <a-form-item field="database" label="Database" :rules="[{
              required: true,
              message: $t('home.index.5mq3ijy1z1s0')
            }]">
              <a-input v-model="formRight.database" :placeholder="$t('home.index.5mq3d6o66ys0')" />
            </a-form-item>
            <a-form-item field="schema" label="Schema" :rules="[{
              required: true,
              message: $t('home.index.5mq3ijy1zw40')
            }]">
              <a-input v-model="formRight.schema" :placeholder="$t('home.index.5mq3d6o67ac0')" />
            </a-form-item>
          </a-form>
        </a-card>
      </div>
    </div>
    <div class="btn-con">
      <a-button type="primary" :loading="loading" @click="startSync">{{ loading ? $t('home.index.5mq3d6o67dc0') : $t('home.index.5mq3d6o67fo0') }}</a-button>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted } from 'vue'
import { syncConfig, syncStatus } from '@/api/plugin'
import { Message } from '@arco-design/web-vue'

let timer = null

const formLeft = reactive({
  host: '',
  port: '',
  user: '',
  password: '',
  schema: ''
})

const formRight = reactive({
  host: '',
  port: '',
  user: '',
  password: '',
  database: '',
  schema: ''
})

const oldStatus = ref(0)
const loading = ref(false)
const formLeftRef = ref()
const formRightRef = ref()
const code = ref(`  <span style="color: #9d0006;">[mysqld]</span>
  <span style="color: #b57614;">binlog_format</span> = <span style="color: #797403;">ROW</span>
  <span style="color: #b57614;">log_bin</span> = <span style="color: #797403;">mysql-bin</span>
  <span style="color: #b57614;">server_id</span> = <span style="color: #8f3f71;">1</span>
  <span style="color: #b57614;">binlog_row_image</span> = <span style="color: #797403;">FULL</span>
  <span style="color: #b57614;">enforce_gtid_consistency</span> = <span style="color: #797403;">ON</span>
  <span style="color: #b57614;">gtid_mode</span> = <span style="color: #797403;">ON</span>`)

// start sync
const startSync = () => {
  formLeftRef.value?.validate(validLeft => {
    if (!validLeft) {
      formRightRef.value?.validate(validRight => {
        if (!validRight) {
          const params = {
            mysqlHost: formLeft.host,
            mysqlPort: formLeft.port,
            mysqlUser: formLeft.user,
            mysqlPass: formLeft.password,
            mysqlSchema: formLeft.schema,
            ogHost: formRight.host,
            ogPort: formRight.port,
            ogUser: formRight.user,
            ogPass: formRight.password,
            ogDatabase: formRight.database,
            ogSchema: formRight.schema
          }
          loading.value = true
          syncConfig(params).then((res) => {
            if (res.data) {
              oldStatus.value = 1
              clearTimer()
              getSyncStatus()
            } else {
              loading.value = false
              Message.error('Sync error, please check whether the configuration is correct')
            }
          }).catch(() => {
            loading.value = false
            clearTimer()
          })
        }
      })
    }
  })
}

// polling status
const getSyncStatus = () => {
  syncStatus().then(res => {
    const data = res.data
    if (data.config) {
      if (Object.keys(data.config).length) {
        // data backfill
        const config = data.config || {}
        formLeft.host = config.mysqlHost
        formLeft.port = config.mysqlPort
        formLeft.user = config.mysqlUser
        formLeft.password = config.mysqlPass
        formLeft.schema = config.mysqlSchema
        formRight.host = config.ogHost
        formRight.port = config.ogPort
        formRight.user = config.ogUser
        formRight.password = config.ogPass
        formRight.database = config.ogDatabase
        formRight.schema = config.ogSchema
      }
      oldStatus.value = 1
      timer = setTimeout(() => {
        getSyncStatus()
      }, 5000)
    } else {
      if (data.config === false) {
        Message.error('Sync error, please check whether the configuration is correct')
      }
    }

    if (oldStatus.value === 1 && data.status === 0) {
      oldStatus.value = data.status
      Message.success('Sync success')
      clearTimer()
    }

    if (data.status === 1) {
      oldStatus.value = data.status
      loading.value = true
    } else {
      loading.value = false
    }
  })
}

// clear timer
const clearTimer = () => {
  timer && clearTimeout(timer)
}

onMounted(() => {
  getSyncStatus()
})

onUnmounted(() => {
  clearTimer()
})
</script>

<style lang="less" scoped>
.home-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  .tips-con {
    width: 920px;
    background: rgb(255, 247, 232);
    margin-bottom: 20px;
    font-size: 14px;
    :deep(.arco-collapse) {
      background-color: rgb(255, 247, 232);
      border: 1px solid transparent;
      .arco-collapse-item-header {
        background-color: rgb(255, 247, 232);
        color: rgb(29, 33, 41);
      }
      .arco-collapse-item-content {
        padding-left: 16px;
        padding-right: 16px;
        background: rgb(255, 247, 232);
      }
      .arco-icon-hover {
        color: rgb(255,125,0);
      }
    }
    .tips {
      line-height: 20px;
      color: rgb(29, 33, 41);
      margin-bottom: 5px;
      display: flex;
      .num {
        width: 20px;
        flex: 0 0 auto;
      }
    }
    .code {
      margin-top: 5px;
      margin-bottom: 5px;
      margin-left: 20px;
      white-space: pre-wrap;
      background: #fcf1d6;
      border-radius: 4px;
      line-height: 20px;
      padding: 10px;
    }
  }
  .form-con {
    display: flex;
    justify-content: center;
    align-items: center;
    .form-center {
      margin-left: 40px;
      margin-right: 40px;
      color: #999;
    }
  }
  .btn-con {
    margin-top: 30px;
  }
}
</style>
