<template>
    <a-modal :mask-closable="false" :esc-to-close="false" :visible="userData.show" :title="userData.title"
        :modal-style="{ width: '550px' }" @cancel="close">
        <template #footer>
            <div class="flex-between">
                <div class="flex-row">
                    <div class="label-color mr" v-if="status !== hostStatusEnum.unTest">{{
                        $t('当前状态')
                    }}
                    </div>
                    <a-tag v-if="status === hostStatusEnum.success" color="green">{{
                        $t('可用')
                    }}</a-tag>
                    <a-tag v-if="status === hostStatusEnum.fail" color="red">{{
                        $t('不可用')
                    }}</a-tag>
                </div>
                <div>
                    <a-button @click="close" class="mr">{{ $t('取消') }}</a-button>
                    <a-button :loading="testLoading" class="mr" @click="handleTestHost">{{
                        $t('连通性测试')
                    }}</a-button>
                    <a-button @click="handleBeforeOk" :loading="submitLoading" type="primary">{{
                        $t('确定') }}</a-button>
                </div>
            </div>
        </template>
        <a-form :model="userData.formData" ref="formRef" :label-col="{ style: { width: '90px' } }" auto-label-width>
            <a-form-item :label="$t('主机信息')">
                {{ userData.formData.privateIp }}({{ userData.formData.publicIp }})
            </a-form-item>
            <a-form-item field="username" :label="$t('用户名')" validate-trigger="blur"
                :rules="[{ required: true, message: t('请输入用户名') }]">
                <a-input v-model="userData.formData.username"
                    :placeholder="$t('请输入用户名')"></a-input>
            </a-form-item>
            <a-form-item field="password" :label="$t('密码')" validate-trigger="blur"
                :rules="[{ required: true, message: t('请输入密码') }]">
                <a-input-password v-model="userData.formData.password"
                    :placeholder="$t('请输入密码')" allow-clear />
            </a-form-item>
        </a-form>
    </a-modal>
    <a-modal width="auto" v-model:visible="userData.confirmVisible" @ok="handleConfirm" @cancel="handleCancel">
        <template #title>
            {{ $t('确认') }}
        </template>
        <div>{{ $t('系统将会保存该用户的密码，是否继续保存？') }}</div>
    </a-modal>
</template>
<script setup>
import { reactive, ref } from 'vue'
import { addHostUser, editHostUser, hostPing } from '@/api/playback'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
import { encryptPassword } from '@/utils/jsencrypt'
const { t } = useI18n()

const hostStatusEnum  = {
    unTest: -1,
    success: 1,
    fail: 0
}

const userData = reactive({
    show: false,
    title: t('新增用户'),
    isNeedPwd: true,
    formData: {
        id: '',
        hostId: '',
        privateIp: '',
        publicIp: '',
        port: '',
        username: '',
        password: ''
    },
    confirmVisible: false
})

const emits = defineEmits([`finish`])

const status = ref(hostStatusEnum.unTest)
const testLoading = ref(false)
const handleTestHost = () => {
    formRef.value?.validate().then(async result => {
        if (!result) {
            testLoading.value = true
            const { password, publicIp, privateIp, port, username } = userData.formData
            const encryptPwd = await encryptPassword(password)
            const param = {
                privateIp,
                publicIp,
                port,
                password: encryptPwd,
                username
            }
            hostPing(param).then((res) => {
                if (Number(res.code) === 200) {
                    status.value = hostStatusEnum.success
                } else {
                    status.value = hostStatusEnum.fail
                }
            }).catch(() => {
                status.value = hostStatusEnum.fail
            }).finally(() => {
                testLoading.value = false
            })
        }
    })
}

const submitLoading = ref(false)
const formRef = ref(null)
const handleBeforeOk = () => {
    formRef.value?.validate().then(async result => {
        if (!result) {
            userData.confirmVisible = true
        }
    })
}

const close = () => {
    userData.show = false
    userData.confirmVisible = false
}

const userBelongingIp = ref('')
const handleConfirm = async () => {
    submitLoading.value = true
    const userPwd = await encryptPassword(userData.formData.password)
    const param = Object.assign({}, userData.formData)
    param.password = userPwd
    if (userData.formData.id) {
        // edit
        editHostUser(userData.formData.id, param).then(() => {
            Message.success({ content: `Modified success` })
            emits(`finish`, userBelongingIp.value)
            close()
        }).finally(() => {
            submitLoading.value = false
        })
    } else {
        const param = {
            hostId: userData.formData.hostId,
            username: userData.formData.username,
            password: userPwd
        }
        addHostUser(param).then((res) => {
          if (Number(res.code) === 200) {
            Message.success({ content: `Create success` })
            emits(`finish`, userBelongingIp.value)
            close()
          }
        }).catch((error) => {
          Message.error({ content: `Create fail` + error })
          emits(`finish`, userBelongingIp.value)
          close()
        }).finally(() => {
            submitLoading.value = false
        })
    }
}

const handleCancel = () => {
    userData.confirmVisible = false
}

const open = (type, hostData) => {
  status.value = hostStatusEnum.unTest
  userBelongingIp.value = type
  userData.show = true
  userData.isNeedPwd = !hostData.isRemember
  const { hostId, privateIp, publicIp, port } = hostData
  userData.title = t('新增用户')
  Object.assign(userData.formData, {
    id: '',
    hostId,
    privateIp,
    publicIp,
    port,
    username: '',
    password: ''
  })
}

defineExpose({
    open
})

</script>

<style scoped></style>
