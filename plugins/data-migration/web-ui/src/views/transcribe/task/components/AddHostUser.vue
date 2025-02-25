<template>
    <a-modal :mask-closable="false" :esc-to-close="false" :visible="userData.show" :title="userData.title"
        :modal-style="{ width: '550px' }" @cancel="close">
        <template #footer>
            <div class="flex-between">
                <div class="flex-row">
                    <div class="label-color mr" v-if="status !== hostStatusEnum.unTest">{{
                        $t('detail.index.5q09asiwjvg0')
                    }}
                    </div>
                    <a-tag v-if="status === hostStatusEnum.success" color="green">{{
                        $t('transcribe.create.addHost.5mphy3snvg80')
                    }}</a-tag>
                    <a-tag v-if="status === hostStatusEnum.fail" color="red">{{
                        $t('transcribe.create.addHost.5mphy3snwq40')
                    }}</a-tag>
                </div>
                <div>
                    <a-button @click="close" class="mr">{{ $t('transcribe.create.addHost.5mphy3snwxs0') }}</a-button>
                    <a-button :loading="testLoading" class="mr" @click="handleTestHost">{{
                        $t('transcribe.create.addHost.5mphy3snx3o0')
                    }}</a-button>
                    <a-button @click="handleBeforeOk" :loading="submitLoading" type="primary">{{
                        $t('transcribe.create.addHost.5mphy3snx7c0') }}</a-button>
                </div>
            </div>
        </template>
        <a-form :model="userData.formData" ref="formRef" :label-col="{ style: { width: '90px' } }" auto-label-width>
            <a-form-item :label="$t('transcribe.create.addHost.host')">
                {{ userData.formData.privateIp }}({{ userData.formData.publicIp }})
            </a-form-item>
            <a-form-item field="username" :label="$t('transcribe.create.addHost.username')" validate-trigger="blur"
                :rules="[{ required: true, message: t('transcribe.create.addHost.usernamePlaceholder') }]">
                <a-input v-model="userData.formData.username"
                    :placeholder="$t('transcribe.create.addHost.usernamePlaceholder')"></a-input>
            </a-form-item>
            <a-form-item field="password" :label="$t('transcribe.create.addHost.5mphy3sny4w0')" validate-trigger="blur"
                :rules="[{ required: true, message: t('transcribe.create.addHost.5mphy3snyao0') }]">
                <a-input-password v-model="userData.formData.password"
                    :placeholder="$t('transcribe.create.addHost.5mphy3snyao0')" allow-clear />
            </a-form-item>
        </a-form>
    </a-modal>
    <a-modal width="auto" v-model:visible="userData.confirmVisible" @ok="handleConfirm" @cancel="handleCancel">
        <template #title>
            {{ $t('transcribe.create.addHost.confirm') }}
        </template>
        <div>{{ $t('transcribe.create.addHost.pwdsavmsg') }}</div>
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
    title: t('transcribe.create.adduser'),
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
  userData.title = t('transcribe.create.adduser')
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
