<template>
  <el-form :model="form" ref="formRef" :rules="formRules" label-width="auto">
    <el-row :gutter="16">
      <el-col :span="16">
        <el-form-item prop="ip" :label="$t('database.JdbcInstance.5oxhtcbo8ac0')" class="uniform-width">
          <el-select v-model.trim="form.ip" :placeholder="$t('database.JdbcInstance.5oxhtcbo9fg0')"
                     allow-create filterable style="width:441px">
            <el-option v-for="item in props.hostList" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item prop="port" label="/" label-width="2">
          <el-input-number v-model="form.port" :placeholder="$t('database.JdbcInstance.5oxhtcbo9xc0')"
                           :min="0" :max="65535" controls-position="right"
                           style="width:178px"
          />
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item prop="username" :label="$t('database.JdbcInstance.5oxhtcboa240')" class="uniform-width">
      <el-input v-model.trim="form.username" :placeholder="$t('database.JdbcInstance.5oxhtcboa7c0')"
                :disabled="props.jdbcType === JDBCType.Milvus || props.jdbcType === JDBCType.Elasticsearch" />
    </el-form-item>
    <el-row :gutter="16">
      <el-col :span="16">
        <el-form-item prop="password" :label="$t('database.JdbcInstance.5oxhtcboac00')" class="uniform-width">
          <el-input v-model.trim="form.password" :placeholder="$t('database.JdbcInstance.5oxhtcboags0')" show-password
                    allow-clear :disabled="props.jdbcType === JDBCType.Milvus || props.jdbcType === JDBCType.Elasticsearch" />
        </el-form-item>
      </el-col>
      <el-col :span="3">
        <el-link type="primary" @click="handleCurTest">  {{ $t('database.AddJdbc.5oxhkhimx5c0')}}</el-link>
      </el-col>
      <el-col :span="5">
        <span class="status-container">
          <span class="status-dot"
                :class="{
                  'error-dot': form.status === jdbcStatusEnum.fail,
                  'success-dot': form.status === jdbcStatusEnum.success,
                  'info-dot': form.status === undefined || form.status === jdbcStatusEnum.unTest
                }" />
          {{
            form.status === undefined || form.status === jdbcStatusEnum.unTest
              ? $t('database.AddJdbc.else1')
              : form.status === jdbcStatusEnum.success
                ? $t('database.AddJdbc.5oxhkhiks5k0')
                : $t('database.AddJdbc.5oxhkhimwfg0')
          }}
        </span>
      </el-col>
    </el-row>
    <div v-if="props.jdbcType !== JDBCType.Milvus && props.jdbcType !== JDBCType.Elasticsearch">
      <el-form-item :label="$t('database.JdbcInstance.5oxhtcboap00')" class="uniform-width">
        <el-table class="full-w" :data="form.props" size="small" :pagination="false">
          <el-table-column :label="t('database.JdbcInstance.5oxhtcboap00')" prop="name">
            <template #default="{ $index }">
              <el-input size="small" v-model.trim="form.props[$index].name" />
            </template>
          </el-table-column>
          <el-table-column :label="t('database.JdbcInstance.5oxhtcboap00')" prop="value">
            <template #default="{ $index }">
              <el-input size="small" v-model.trim="form.props[$index].value" />
            </template>
          </el-table-column>
          <el-table-column :label="t('database.JdbcInstance.5oxhtcboap00')" width="130">
            <template #default="{ $index }">
              <div class="flex-row-start">
                <el-link class="mr-s" @click="handleUrlAdd($index)">{{ $t('database.JdbcInstance.5oxhtcboatc0') }}</el-link>
                <el-link v-if="$index > 0" @click="handleUrlDel($index)">{{ $t('database.JdbcInstance.5oxhtcboaxc0') }}</el-link>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-form-item>
    </div>
    <el-form-item prop="url" :label="$t('database.JdbcInstance.5oxhtcbob1s0')" class="uniform-width" style="margin-bottom: 0">
      <div class="flex-row" style="width: 100%;">
        {{ jdbcUrl }}
      </div>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { ElForm } from 'element-plus'
import { PropType, ref, computed, watch } from 'vue'
import { jdbcNodePing } from '@/api/ops'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()
enum jdbcStatusEnum {
  unTest = -1,
  success = 1,
  fail = 0
}

const props = defineProps({
  formData: {
    type: Object as PropType<KeyValue>,
    required: true
  },
  hostList: {
    type: Object as PropType<KeyValue>,
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

import { IpRegex } from '@/types/global'
import {encryptPassword} from "@/utils/jsencrypt";
import {JDBCType} from "@/types/jdbc";

const formRules = computed(() => {
  return {
    ip: [
      { required: true, message: t('database.JdbcInstance.5oxhtcbobhs0'), trigger: 'change' },
      {
        validator: (rule: any, value: any, callback: any) => {
          if (IpRegex.ipv4Reg.test(value) || IpRegex.ipv6Reg.test(value)) {
            callback()
          } else {
            callback(new Error(t('database.JdbcInstance.5oxhtcboblw0')))
          }
        },
        trigger: 'change'
      }
    ],
    port: [
      { required: true, message: t('database.JdbcInstance.5oxhtcbo9xc0'), trigger: 'blur' }
    ],
    username: [
      { required: props.jdbcType !== JDBCType.Milvus && props.jdbcType !== JDBCType.Elasticsearch, message: t('database.JdbcInstance.5oxhtcboa7c0'), trigger: 'blur' },
      {
        validator: (rule: any, value: any, callback: any) => {
          if (props.jdbcType !== JDBCType.Milvus && props.jdbcType !== JDBCType.Elasticsearch && !value.trim() ) {
            callback(new Error(t('database.JdbcInstance.5oxhtcbobtc0')))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ],
    password: [
      { required: props.jdbcType !== JDBCType.Milvus && props.jdbcType !== JDBCType.Elasticsearch, message: t('database.JdbcInstance.5oxhtcboc2c0'), trigger: 'blur' },
      {
        validator: (rule: any, value: any, callback: any) => {
          if (!value.trim() && props.jdbcType !== JDBCType.Milvus && props.jdbcType !== JDBCType.Elasticsearch) {
            callback(new Error(t('database.JdbcInstance.5oxhtcbobtc0')))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
  }
})

const handleUrlAdd = (index: number) => {
  form.value.props?.splice(index + 1, 0, {
    name: '',
    value: ''
  })
}

const handleUrlDel = (index: number) => {
  form.value.props?.splice(index, 1)
}

const handelTest = async (): Promise<KeyValue> => {
  let result = {
    id: form.value.id,
    res: false
  }
  let encryptPwd = ''
  if (form.value.password && form.value.password !== '') {
    encryptPwd = await encryptPassword(form.value.password)
  }
  const param = {
    username: form.value.username,
    password: encryptPwd,
    url: jdbcUrl.value,
    dbType: props.jdbcType
  }
  try {
    const res: KeyValue = await jdbcNodePing(param)
    if (Number(res.code) === 200 && res.data) {
      // ok
      result.res = true
      form.value.status = jdbcStatusEnum.success
    } else {
      form.value.status = jdbcStatusEnum.fail
    }
  } catch (err) {
    form.value.status = jdbcStatusEnum.fail
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

const handleCurTest = async () => {
  const validRes = await formValidate()
  if (validRes.id === form.value.id && validRes.res) {
    await handelTest()
  }
}

const formRef = ref<null | InstanceType<typeof ElForm>>(null)

const formValidate = async (): Promise<KeyValue> => {
  if (!formRef.value) return { id: form.value.id, res: false }

  try {
    await formRef.value.validate()
    return { id: form.value.id, res: true }
  } catch (error) {
    return { id: form.value.id, res: false }
  }
}

defineExpose({
  handelTest,
  formValidate
})

</script>

<style scoped>
.uniform-width {
  width: 100%;
}

:deep(.el-form-item__content) {
  width: 320px;
}

:deep(.el-input),
:deep(.el-input-number),
:deep(.el-select) {
  max-width: 441px;

}

:deep(.el-input-number .el-input) {
  max-width: 441px;
}

:deep(.el-input-number .el-input__inner) {
  text-align: left;
}

.status-container {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  height: 24px;
  line-height: 1;
}


.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin: 16px;
}

.info-dot {
  background-color: var(--o-color-info-secondary);
}

.error-dot {
  background-color: var(--o-color-danger);
}

.success-dot {
  background-color: var(--o-color-success);
}
</style>
