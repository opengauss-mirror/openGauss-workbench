<template>
  <a-form :model="form" ref="formRef" auto-label-width :rules="formRules">
    <a-form-item field="ip" :label="$t('database.JdbcInstance.5oxhtcbo8ac0')" validate-trigger="change">
      <a-select v-model.trim="form.ip" :placeholder="$t('database.JdbcInstance.5oxhtcbo9fg0')" allow-create>
        <a-option v-for="item in props.hostList" :key="item.value" :label="item.label" :value="item.value"></a-option>
      </a-select>
    </a-form-item>
    <a-form-item field="port" :label="$t('database.JdbcInstance.5oxhtcbo9ps0')" validate-trigger="blur">
      <a-input-number v-model="form.port" :placeholder="$t('database.JdbcInstance.5oxhtcbo9xc0')" :min="0" :max="65535"/>
    </a-form-item>
    <a-form-item field="username" :label="$t('database.JdbcInstance.5oxhtcboa240')" validate-trigger="blur">
      <a-input v-model.trim="form.username" :placeholder="$t('database.JdbcInstance.5oxhtcboa7c0')" />
    </a-form-item>
    <a-form-item field="password" :label="$t('database.JdbcInstance.5oxhtcboac00')" validate-trigger="blur">
      <a-input-password v-model.trim="form.password" :placeholder="$t('database.JdbcInstance.5oxhtcboags0')"
        allow-clear />
    </a-form-item>
    <a-form-item :label="$t('database.JdbcInstance.5oxhtcboap00')">
      <a-table class="full-w" :data="form.props" :columns="columns" size="mini" :pagination="false">
        <template #name="{ rowIndex }">
          <a-input size="mini" v-model.trim="form.props[rowIndex].name" />
        </template>
        <template #value="{ rowIndex }">
          <a-input size="mini" v-model.trim="form.props[rowIndex].value" />
        </template>
        <template #operation="{ rowIndex }">
          <div class="flex-row-start">
            <a-link class="mr-s" @click="handleUrlAdd(rowIndex)">{{ $t('database.JdbcInstance.5oxhtcboatc0') }}</a-link>
            <a-link v-if="rowIndex > 0" @click="handleUrlDel(rowIndex)">{{ $t('database.JdbcInstance.5oxhtcboaxc0')
            }}</a-link>
          </div>
        </template>
      </a-table>
    </a-form-item>
    <a-form-item field="url" :label="$t('database.JdbcInstance.5oxhtcbob1s0')" validate-trigger="blur">
      <div class="flex-row" style="width: 100%;">
        {{ jdbcUrl }}
      </div>
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
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

const columns = computed(() => [
  { title: t('database.JdbcInstance.5oxhtcbob640'), dataIndex: 'name', slotName: 'name' },
  { title: t('database.JdbcInstance.5oxhtcbobac0'), dataIndex: 'value', slotName: 'value' },
  { title: t('database.JdbcInstance.5oxhtcbobe80'), slotName: 'operation', width: 130 }
])

import { IpRegex } from '@/types/global'
import {encryptPassword} from "@/utils/jsencrypt";

const formRules = computed(() => {
  return {
    ip: [
      { required: true, message: t('database.JdbcInstance.5oxhtcbobhs0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (IpRegex.ipv4Reg.test(value) || IpRegex.ipv6Reg.test(value)) {
              resolve(true)
            } else {
              cb(t('database.JdbcInstance.5oxhtcboblw0'))
              resolve(false)
            }
          })
        }
      }
    ],
    port: [
      { required: true, message: t('database.JdbcInstance.5oxhtcbo9xc0') }
    ],
    username: [
      { required: true, message: t('database.JdbcInstance.5oxhtcboa7c0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('database.JdbcInstance.5oxhtcbobtc0'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    password: [
      { required: true, message: t('database.JdbcInstance.5oxhtcboc2c0') },
      {
        validator: (value: any, cb: any) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('database.JdbcInstance.5oxhtcbobtc0'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
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
  const encryptPwd = await encryptPassword(form.value.password)
  const param = {
    username: form.value.username,
    password: encryptPwd,
    url: jdbcUrl.value
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
  let urlSuffix = ''
  if (form.value.props?.length) {
    form.value.props?.forEach((item: KeyValue, index: number) => {
      if (item.name.trim() && item.value.trim()) {
        if (index === 0) {
          urlSuffix = urlSuffix + item.name + '=' + item.value
        } else if (index > 0 && index < form.value.props?.length)
          urlSuffix = urlSuffix + '&' + item.name + '=' + item.value
      }
    })
  }
  let urlPrefix = ''
  if (props.jdbcType === 'MYSQL') {
    urlPrefix = `jdbc:mysql://${form.value.ip ? form.value.ip : '{IP}'}:${form.value.port ? form.value.port : '{port}'}`
    if (urlSuffix) {
      return urlPrefix + '?' + urlSuffix
    } else {
      return urlPrefix
    }
  } else if (props.jdbcType === 'OPENGAUSS') {
    urlPrefix = `jdbc:opengauss://${form.value.ip ? form.value.ip : '{IP}'}:${form.value.port ? form.value.port : '{port}'}/postgres`
    if (urlSuffix) {
      return urlPrefix + '?' + urlSuffix
    } else {
      return urlPrefix
    }
  } else if (props.jdbcType === 'POSTGRESQL') {
    urlPrefix = `jdbc:postgresql://${form.value.ip ? form.value.ip : '{IP}'}:${form.value.port ? form.value.port : '{port}'}/postgres`
    if (urlSuffix) {
      return urlPrefix + '?' + urlSuffix
    } else {
      return urlPrefix
    }
  }
  return urlPrefix
})

watch(jdbcUrl, (val) => {
  form.value.url = val
})
watch(() => props.jdbcType, (val) => {
  if (val === 'MYSQL') {
    form.value.port = 3306
  } else if (val === 'OPENGAUSS') {
    form.value.port = 5432
  } else if (val === 'POSTGRESQL') {
    form.value.port = 5432
  }
})

const formRef = ref<null | FormInstance>(null)

const formValidate = async (): Promise<KeyValue> => {
  const validRes = await formRef.value?.validate()
  const result = {
    id: form.value.id,
    res: !validRes
  }
  return result
}

defineExpose({
  handelTest,
  formValidate
})

</script>
