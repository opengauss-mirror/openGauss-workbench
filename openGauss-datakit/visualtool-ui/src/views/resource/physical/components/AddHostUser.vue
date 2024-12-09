<template>
    <a-modal :mask-closable="false" :esc-to-close="false" :visible="userData.show" :title="userData.title" @cancel="close"
        :modal-style="{ width: '550px' }">
        <template #footer>
            <div class="flex-between">
                <div class="flex-row">
                    <div class="label-color mr" v-if="status !== hostStatusEnum.unTest">{{
                        $t('components.AddHost.currentStatus')
                    }}
                    </div>
                    <a-tag v-if="status === hostStatusEnum.success" color="green">{{
                        $t('components.AddHost.5mphy3snvg80')
                    }}</a-tag>
                    <a-tag v-if="status === hostStatusEnum.fail" color="red">{{
                        $t('components.AddHost.5mphy3snwq40')
                    }}</a-tag>
                </div>
                <div>
                    <a-button @click="close" class="mr">{{ $t('components.AddHost.5mphy3snwxs0') }}</a-button>
                    <a-button :loading="testLoading" class="mr" @click="handleTestHost">{{
                        $t('components.AddHost.5mphy3snx3o0')
                    }}</a-button>
                    <a-button @click="handleBeforeOk" :loading="submitLoading" type="primary">{{
                        $t('components.AddHost.5mphy3snx7c0') }}</a-button>
                </div>
            </div>
        </template>
        <a-form :model="userData.formData" ref="formRef" :label-col="{ style: { width: '90px' } }" auto-label-width>
            <a-form-item :label="$t('components.AddHostUser.5mphzt9pc0g0')">
                {{ userData.formData.privateIp }}({{ userData.formData.publicIp }})
            </a-form-item>
            <a-form-item field="username" :label="$t('components.AddHostUser.5mphzt9pdn80')" validate-trigger="blur"
                :rules="[{ required: true, message: t('components.AddHostUser.5mphzt9pdw00') }]">
                <a-input v-model="userData.formData.username"
                    :placeholder="$t('components.AddHostUser.5mphzt9pdw00')" v-if="userData.type === 'create'">
                </a-input>
                <span v-else>
                    {{ userData.formData.username }}
                </span>
            </a-form-item>
            <a-form-item field="password" :label="$t('components.AddHostUser.5mphzt9pe3s0')" validate-trigger="blur"
                :rules="[{ required: true, message: t('components.AddHostUser.5mphzt9pec40') }]">
                <a-input-password v-model="userData.formData.password"
                    :placeholder="$t('components.AddHostUser.5mphzt9pec40')" allow-clear />
            </a-form-item>
        </a-form>
    </a-modal>
    <a-modal width="auto" v-model:visible="userData.confirmVisible" @ok="handleConfirm" @cancel="handleCancel">
        <template #title>
            {{ $t('components.AddHostUser.else1') }}
        </template>
        <div>{{ $t('components.AddHostUser.else2') }}</div>
    </a-modal>
</template>
<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { reactive, ref } from 'vue'
import { addHostUser, editHostUser, hostPing } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
import { encryptPassword } from '@/utils/jsencrypt'
const { t } = useI18n()

enum hostStatusEnum {
    unTest = -1,
    success = 1,
    fail = 0
}

const userData = reactive({
    show: false,
    title: t('components.AddHostUser.5mphzt9peog0'),
    type: 'create',
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
            hostPing(param).then((res: KeyValue) => {
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

const submitLoading = ref<boolean>(false)
const formRef = ref<null | FormInstance>(null)
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

const handleConfirm = async () => {
    submitLoading.value = true
    const userPwd = await encryptPassword(userData.formData.password)
    const param = Object.assign({}, userData.formData)
    param.password = userPwd
    if (userData.formData.id) {
        // edit
        editHostUser(userData.formData.id, param).then(() => {
            Message.success({ content: `Modified success` })
            emits(`finish`)
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
        addHostUser(param).then(() => {
            Message.success({ content: `Create success` })
            emits(`finish`)
            close()
        }).finally(() => {
            submitLoading.value = false
        })
    }
}

const handleCancel = () => {
    userData.confirmVisible = false
}

const open = (type: string, hostData: KeyValue, data?: KeyValue) => {
    userData.isNeedPwd = !hostData.isRemember
    const { hostId, privateIp, publicIp, port } = hostData
    userData.type = type
    if (type === 'create') {
        userData.title = t('components.AddHostUser.5mphzt9peog0')
        Object.assign(userData.formData, {
            id: '',
            hostId,
            privateIp,
            publicIp,
            port,
            username: '',
            password: ''
        })
    } else {
        userData.title = t('components.AddHostUser.updateUser')
        Object.assign(userData.formData, {
            id: data?.hostUserId,
            hostId,
            privateIp,
            publicIp,
            port,
            username: data?.username,
            password: ''
        })
    }
    status.value = hostStatusEnum.unTest
    userData.show = true
}

defineExpose({
    open
})

</script>

<style scoped></style>
