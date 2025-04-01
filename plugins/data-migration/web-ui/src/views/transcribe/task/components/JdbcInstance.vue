<template>
  <a-form :model="form" ref="formRef" auto-label-width :rules="formRules">
    <a-form-item field="ip" :label="$t('components.JdbcInstance.5q0a9843uf40')" validate-trigger="change">
      <a-select v-model="form.ip" :placeholder="$t('components.JdbcInstance.5q0a8km729o0')" allow-create>
        <a-option v-for="item in props.hostList" :key="item.value" :label="item.label" :value="item.value"></a-option>
      </a-select>
    </a-form-item>
    <a-form-item field="port" :label="$t('components.JdbcInstance.5q0a8km736s0')" validate-trigger="blur">
      <a-input-number v-model="form.port" :placeholder="$t('components.JdbcInstance.5q0a8km73bo0')" :min="0"
                      :max="65535"/>
    </a-form-item>
    <a-form-item field="username" :label="$t('components.JdbcInstance.5q0a8km73f00')" validate-trigger="blur">
      <a-input v-model="form.username" :placeholder="$t('components.JdbcInstance.5q0a8km73hs0')"/>
    </a-form-item>
    <a-form-item field="password" :label="$t('components.JdbcInstance.5q0a8km73ko0')" validate-trigger="blur">
      <a-input-password v-model="form.password" :placeholder="$t('components.JdbcInstance.5q0a8km73n80')" allow-clear/>
    </a-form-item>
    <a-form-item :label="$t('components.JdbcInstance.5q0a8km73q00')">
      <a-table class="full-w" :data="form.props" :columns="columns" size="mini" :pagination="false">
        <template #name="{ rowIndex }">
          <a-input size="mini" v-model="form.props[rowIndex].name"/>
        </template>
        <template #value="{ rowIndex }">
          <a-input size="mini" v-model="form.props[rowIndex].value"/>
        </template>
        <template #operation="{ rowIndex }">
          <div class="flex-row-start">
            <a-link class="mr-s" @click="handleUrlAdd(rowIndex)">{{ $t('components.JdbcInstance.5q0a8km73uc0') }}
            </a-link>
            <a-link v-if="rowIndex > 0" @click="handleUrlDel(rowIndex)">
              {{ $t('components.JdbcInstance.5q0a8km73xc0') }}
            </a-link>
          </div>
        </template>
      </a-table>
    </a-form-item>
    <a-form-item field="url" :label="$t('components.JdbcInstance.5q0a8km74cw0')" validate-trigger="blur">
      <div class="flex-row" style="width: 100%;">
        {{ jdbcUrl }}
      </div>
    </a-form-item>
  </a-form>
</template>

<script setup>
import {ref, computed, watch} from 'vue'
import {jdbcNodePing} from '@/api/task'
import {useI18n} from 'vue-i18n'

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

const columns = computed(() => [
  {title: t('components.JdbcInstance.5q0a8km74h00'), dataIndex: 'name', slotName: 'name'},
  {title: t('components.JdbcInstance.5q0a8km74kk0'), dataIndex: 'value', slotName: 'value'},
  {title: t('components.JdbcInstance.5q0a8km74n80'), slotName: 'operation', width: 130}
])

const formRules = computed(() => {
  return {
    ip: [
      {required: true, message: t('components.JdbcInstance.5q0a8km74po0')},
      {
        validator: (value, cb) => {
          return new Promise(resolve => {
            const reg = /^(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|[0-9])\.((1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|\d)\.){2}(1\d{2}|2[0-4]\d|25[0-5]|[1-9]\d|\d)$/
            const re = new RegExp(reg)
            if (re.test(value)) {
              resolve(true)
            } else {
              cb(t('components.JdbcInstance.5q0a8km74s40'))
              resolve(false)
            }
          })
        }
      }
    ],
    port: [
      {required: true, message: t('components.JdbcInstance.5q0a8km73bo0')}
    ],
    username: [
      {required: true, message: t('components.JdbcInstance.5q0a8km73hs0')},
      {
        validator: (value, cb) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('components.JdbcInstance.5q0a8km74xk0'))
              resolve(false)
            } else {
              resolve(true)
            }
          })
        }
      }
    ],
    password: [
      {required: true, message: t('components.JdbcInstance.5q0a8km75000')},
      {
        validator: (value, cb) => {
          return new Promise(resolve => {
            if (!value.trim()) {
              cb(t('components.JdbcInstance.5q0a8km74xk0'))
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

const handleUrlAdd = (index) => {
  form.value.props?.splice(index + 1, 0, {
    name: '',
    value: ''
  })
}

const handleUrlDel = (index) => {
  form.value.props?.splice(index, 1)
}

const handelTest = async () => {
  let result = {
    id: form.value.id,
    res: false
  }
  const param = {
    username: form.value.username,
    password: form.value.password,
    url: jdbcUrl.value
  }
  try {
    const res = await jdbcNodePing(param)
    if (Number(res.code) === 200 && res.data) {
      // ok
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
  let urlSuffix = ''
  if (form.value.props?.length) {
    form.value.props?.forEach((item, index) => {
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
    urlPrefix = `jdbc:mysql://${form.value.ip || '{IP}'}:${form.value.port || '{port}'}`
    if (urlSuffix) {
      return urlPrefix + '?' + urlSuffix
    } else {
      return urlPrefix
    }
  } else if (props.jdbcType === 'OPENGAUSS') {
    urlPrefix = `jdbc:opengauss://${form.value.ip || '{IP}'}:${form.value.port || '{port}'}/postgres`
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
  }
})

const formRef = ref(null)

const formValidate = async () => {
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
