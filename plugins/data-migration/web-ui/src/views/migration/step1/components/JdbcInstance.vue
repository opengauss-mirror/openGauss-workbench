<template>
  <el-form :model="form" ref="formRef" auto-label-width :rules="formRules" label-width="110px">
    <el-row style="justify-content: space-between">
      <el-form-item prop="ip" :label="$t('components.JdbcInstance.5q0a9843uf40')" validate-trigger="change">
        <el-select v-model="form.ip" :placeholder="$t('components.JdbcInstance.5q0a8km729o0')"
                   allow-create filterable style="width:441px">
          <el-option v-for="item in props.hostList" :key="item.value" :label="item.label" :value="item.value"/>
        </el-select>
      </el-form-item>
      <el-form-item prop="port" label="/" validate-trigger="blur">
        <el-input-number v-model="form.port" :placeholder="$t('components.JdbcInstance.5q0a8km73bo0')" :min="0"
                         :max="65535" controls-position="right" style="width:178px" />
      </el-form-item>
    </el-row>
    <el-form-item prop="username" :label="$t('components.JdbcInstance.5q0a8km73f00')" validate-trigger="blur">
      <el-input v-model="form.username" :placeholder="$t('components.JdbcInstance.5q0a8km73hs0')"
                :disabled="props.jdbcType === JDBCType.Milvus || props.jdbcType === JDBCType.Elasticsearch" style="width:441px" />
    </el-form-item>
    <el-form-item prop="password" :label="$t('components.JdbcInstance.5q0a8km73ko0')" validate-trigger="blur">
      <div class="password-container">
        <div class="input-wrapper">
          <el-input v-model="form.password" :placeholder="$t('components.JdbcInstance.5q0a8km73n80')"
            allow-clear show-password class="password-input"
            :disabled="props.jdbcType === JDBCType.Milvus || props.jdbcType === JDBCType.Elasticsearch"
          />
        </div>
        <div class="actions-row">
          <el-button type="primary" link @click="handelTest" class="test-btn">
            {{ $t('components.AddJdbc.5q0a7i43ap40') }}
          </el-button>
          <div class="status-container">
            <el-text truncated type="info" v-if="form.status === -1">
              {{ $t('components.AddJdbc.5q0a7i439cg0') }}
            </el-text>
            <el-text truncated type="success" v-else-if="form.status === 1">
              {{ $t('components.AddJdbc.5q0a7i43aeg0') }}
            </el-text>
            <el-text truncated type="danger" v-else>
              {{ $t('components.AddJdbc.5q0a7i43ajk0') }}
            </el-text>
          </div>
        </div>
      </div>
    </el-form-item>
    <el-form-item :label="$t('components.JdbcInstance.5q0a8km73q00')"
                  v-if="props.jdbcType !== JDBCType.Milvus && props.jdbcType !== JDBCType.Elasticsearch">
      <el-table class="full-w" :data="form.props" size="mini" :pagination="false">
        <el-table-column :label="$t('components.JdbcInstance.5q0a8km74h00')" prop="name">
          <template #default="scope">
            <el-input size="mini" v-model="scope.row.name"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('components.JdbcInstance.5q0a8km74kk0')" prop="value">
          <template #default="scope">
            <el-input size="mini" v-model="scope.row.value"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('components.JdbcInstance.5q0a8km74n80')" prop="value">
          <template #default="scope">
            <el-popconfirm v-if="scope.$index > 0"
                           :confirm-button-text="$t('components.AddJdbc.5q0a7i43as00')"
                           :cancel-button-text="$t('components.AddJdbc.5q0a7i43amo0')"
                           :icon="InfoFilled" icon-color="#626AEF"
                           :title="$t('components.JdbcInstance.delePropertiesMsg')"
                           @confirm="handleUrlDel(scope.$index)">
              <template #reference>
                <el-button
                  size="small"
                  type="danger"
                >
                  <el-icon>
                    <Delete/>
                  </el-icon>
                </el-button>
              </template>
            </el-popconfirm>
            <a-link v-if="scope.row > 0" @click="handleUrlDel(scope.row)">
              {{ $t('components.JdbcInstance.5q0a8km73xc0') }}
            </a-link>
          </template>
        </el-table-column>
      </el-table>
      <el-button type="info" link class="mr-s" @click="handleUrlAdd(rowIndexLen)">
        <el-icon>
          <CirclePlus/>
        </el-icon>
        {{ $t('components.JdbcInstance.addPropertiesMsg') }}
      </el-button>
    </el-form-item>
    <el-form-item prop="url" :label="$t('components.JdbcInstance.5q0a8km74cw0')" validate-trigger="blur">
      <div class="flex-row" style="width: 100%;">
        {{ jdbcUrl }}
      </div>
    </el-form-item>
  </el-form>
</template>

<script setup>
import {ref, computed, watch} from 'vue'
import {jdbcNodePing} from '@/api/task'
import {useI18n} from 'vue-i18n'
import {Delete, CirclePlus, InfoFilled} from '@element-plus/icons-vue'
import {encryptPassword} from "@/utils/jsencrypt"
import { JDBCType } from "@/types/jdbc";

const {t} = useI18n()

const props = defineProps({
  formData: {
    type: Object,
    required: true
  },
  hostList: {
    type: Object,
    required: true
  },
  jdbcType: {
    type: String,
    required: true
  }
})
const emits = defineEmits([`update:formData`])
const form = computed({
  get: () => props.formData,
  set: (val) => {
    emits(`update:formData`, val)
  }
})

const formRules = computed(() => {
  return {
    ip: [
      {required: true, message: t('components.JdbcInstance.5q0a8km74po0')},
      {
        validator: (rule, value) => {
          return new Promise((resolve, reject) => {
            const reg = /^(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|[0-9])\.((1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|\d)\.){2}(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|\d)$/
            if (reg.test(value)) {
              resolve()
            } else {
              reject(new Error(t('components.JdbcInstance.5q0a8km74s40')))
            }
          })
        }
      }
    ],
    port: [
      {required: true, message: t('components.JdbcInstance.5q0a8km73bo0')}
    ],
    username: [
      {required: props.jdbcType !== JDBCType.Milvus && props.jdbcType !== JDBCType.Elasticsearch, message: t('components.JdbcInstance.5q0a8km73hs0')},
      {
        validator: (rule, value) => {
          return new Promise((resolve, reject) => {
            if (!value && props.jdbcType !== JDBCType.Milvus && props.jdbcType !== JDBCType.Elasticsearch) {
              reject(new Error(t('components.JdbcInstance.5q0a8km74xk0')))
            } else {
              resolve()
            }
          })
        }
      }
    ],
    password: [
      {required: props.jdbcType !== JDBCType.Milvus && props.jdbcType !== JDBCType.Elasticsearch, message: t('components.JdbcInstance.5q0a8km75000')},
      {
        validator: (rule, value) => {
          return new Promise((resolve, reject) => {
            if (!value && props.jdbcType !== JDBCType.Milvus && props.jdbcType !== JDBCType.Elasticsearch) {
              reject(new Error(t('components.JdbcInstance.5q0a8km74xk0')))
            } else {
              resolve()
            }
          })
        }
      }
    ]
  }
})

const handleUrlAdd = (index) => {
  form.value.props?.splice(index + 1, 0, {
    name: '',
    value: ''
  })
}

const rowIndexLen = computed(() => {
  return form.value.props?.length
})

const handleUrlDel = (index) => {
  form.value.props?.splice(index, 1)
}

const handelTest = async () => {
  let result = {
    id: form.value.id,
    res: false
  }
  let encryptPwd = ''
  if (form.value.password && form.value.password !== '') {
    encryptPwd = await encryptPassword(form.value.password)
  }

  const param = {
    dbType: props.jdbcType,
    username: form.value.username,
    password: encryptPwd,
    url: jdbcUrl.value
  }
  try {
    const res = await jdbcNodePing(param)
    if (Number(res.code) === 200 && res.data) {
      result.res = true
      form.value.status = 1
    } else {
      form.value.status = 0
    }
  } catch (err) {
    form.value.status = 0
  }
  return result
}

const jdbcUrl = computed(() => {
  const queryParams = form.value.props
    ?.filter(item => item.name?.trim() && item.value?.trim())
    .map(item => `${item.name}=${item.value}`)
    .join('&') || ''

  const config = JDBCType.getConfig(props.jdbcType)
  const host = form.value.ip || '{IP}'
  const port = form.value.port || '{port}'

  let baseUrl = ''

  if (config.urlPattern) {
    let defaultDb = 'postgres'
    if (props.jdbcType === JDBCType.MySQL) {
      defaultDb = 'mysql'
    }
    baseUrl = config.urlPattern
      .replace('{host}', host)
      .replace('{port}', port.toString())
      .replace('{defaultDb}', defaultDb)

    return queryParams ? `${baseUrl}?${queryParams}` : baseUrl
  } else {
    baseUrl = `http://${host}:${port}`
    return baseUrl
  }
})


watch(jdbcUrl, (val) => {
  form.value.url = val
})
watch(() => props.jdbcType, (val) => {
  form.value.port = JDBCType.getDefaultPort(val)
})

const formRef = ref(null)

const formValidate = async () => {
  try {
    const p = await formRef.value?.validate();
    return {
      id: form.value.id,
      res: true
    };
  } catch (error) {
    console.log('Error:', error);
    return {
      id: form.value.id,
      res: false,
      errors: error
    };
  }
};

defineExpose({
  handelTest,
  formValidate
})
</script>

<style scoped>
.text-row {
  display: flex;
  align-items: center;
  gap: 24px;
}
.text-item {
  flex: 1;
  height: 60px;
  padding: 12px 15px;
  background: white;
  border: 1px solid #d0e3ff;
  border-radius: 6px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
  overflow: hidden;
  position: relative;
  line-height: 1.4;
}

.text-item .content {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  height: 100%;
}

.text-item .full-content {
  display: none;
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  padding: 12px 15px;
  background: white;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  z-index: 10;
}

.text-item:hover .content {
  display: none;
}

.text-item:hover .full-content {
  display: block;
}

.password-container {
  width: 100%;
}

.input-wrapper {
  display: inline-flex;
  vertical-align: middle;
  margin-right: 16px;
}

.password-input {
  width: 441px;
}

.actions-row {
  display: inline-flex;
  align-items: center;
  gap: 16px;
  vertical-align: middle;
}

.test-btn {
  white-space: nowrap;
}

.status-container {
  display: inline-block;
  min-width: 150px;
}

@media (max-width: 1100px) {
  .input-wrapper {
    display: block;
    margin-right: 0;
    margin-bottom: 8px;
  }
  .password-input {
    width: 100%;
    max-width: 441px;
  }
  .actions-row {
    display: flex;
    align-items: center;
    gap: 16px;
  }
}

@media (max-width: 600px) {
  .actions-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .status-container {
    width: 100%;
  }
}

</style>
