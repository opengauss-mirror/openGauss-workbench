<template>
    <a-modal :mask-closable="false" :esc-to-close="false" :ok-loading="submitLoading" :visible="userData.show"
        :title="userData.title" :modal-style="{ width: '550px' }" @ok="handleBeforeOk" @cancel="close">
        <a-form :model="userData.formData" ref="formRef" :label-col="{ style: { width: '90px' } }" auto-label-width>
            <a-form-item :label="$t('components.AddHostUser.5mphzt9pc0g0')">
                {{ userData.formData.privateIp }}({{ userData.formData.publicIp }})
            </a-form-item>
            <a-form-item v-if="userData.isNeedPwd" field="rootPassword" :label="$t('components.AddHostUser.rootPassword')"
                validate-trigger="blur" :rules="[{ required: true, message: $t('components.AddHostUser.5mphzt9pdak0') }]">
                <a-input-password v-model="userData.formData.rootPassword"
                    :placeholder="$t('components.AddHostUser.5mphzt9pdak0')" allow-clear />
            </a-form-item>
            <a-form-item field="username" :label="$t('components.AddHostUser.5mphzt9pdn80')" validate-trigger="blur"
                :rules="[{ required: true, message: t('components.AddHostUser.5mphzt9pdw00') }]">
                <a-input v-model="userData.formData.username"
                    :placeholder="$t('components.AddHostUser.5mphzt9pdw00')"></a-input>
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
import { addHostUser, editHostUser } from '@/api/ops'
import { Message } from '@arco-design/web-vue'
import { useI18n } from 'vue-i18n'
import { encryptPassword } from '@/utils/jsencrypt'
const { t } = useI18n()
const userData = reactive({
    show: false,
    title: t('components.AddHostUser.5mphzt9peog0'),
    isNeedPwd: true,
    formData: {
        id: '',
        hostId: '',
        privateIp: '',
        publicIp: '',
        rootPassword: '',
        username: '',
        password: ''
    },
    confirmVisible: false
})

const emits = defineEmits([`finish`])

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
    const rootPwd = await encryptPassword(userData.formData.rootPassword)
    const userPwd = await encryptPassword(userData.formData.password)
    const param = Object.assign({}, userData.formData)
    param.rootPassword = rootPwd
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
            rootPassword: rootPwd,
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
    userData.show = true
    userData.isNeedPwd = !hostData.isRemember
    if (type === 'create') {
        userData.title = t('components.AddHostUser.5mphzt9peog0')
        Object.assign(userData.formData, {
            id: '',
            hostId: hostData.hostId,
            privateIp: hostData.privateIp,
            publicIp: hostData.publicIp,
            rootPassword: '',
            username: '',
            password: ''
        })
    } else {
        userData.title = t('components.AddHostUser.updateUser')
        Object.assign(userData.formData, {
            id: data?.hostUserId,
            hostId: hostData.hostId,
            privateIp: hostData.privateIp,
            publicIp: hostData.publicIp,
            rootPassword: '',
            username: data?.username,
            password: ''
        })
    }
}

defineExpose({
    open
})

</script>
