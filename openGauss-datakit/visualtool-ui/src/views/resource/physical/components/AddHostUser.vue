<template>

    <el-dialog v-model="userData.show" :title="userData.title" class="w550 addUserDia openDesignDialog" @cancel="close"
        :z-index="1000">
        <el-form :model="userData.formData" class="addUserFormStyle" ref="formRef" :label-width="84"
            :label-col="{ style: { width: '90px' } }" auto-label-width>
            <el-form-item :label="$t('components.AddHostUser.5mphzt9pc0g0')">
                {{ userData.formData.privateIp }}({{ userData.formData.publicIp }})
            </el-form-item>
            <el-form-item prop="username" :label="$t('components.AddHostUser.5mphzt9pdn80')" validate-trigger="blur"
                :rules="[{ required: true, trriger: 'blur', message: t('components.AddHostUser.5mphzt9pdw00') }]">
                <el-input v-model="userData.formData.username" :placeholder="$t('components.AddHostUser.5mphzt9pdw00')"
                    v-if="userData.type === 'create'">
                </el-input>
                <span v-else>
                    {{ userData.formData.username }}
                </span>
            </el-form-item>
            <el-form-item prop="password" :label="$t('components.AddHostUser.5mphzt9pe3s0')" validate-trigger="blur"
                :rules="[{ required: true, trriger: 'blur', message: t('components.AddHostUser.5mphzt9pec40') }]">
                <el-input type="password" v-model="userData.formData.password"
                    :placeholder="$t('components.AddHostUser.5mphzt9pec40')" allow-clear show-password/>
            </el-form-item>
        </el-form>
        <template #footer>
            <div class="flex-between">
                <div class="flex-row">
                    <div class="label-color mr" v-if="status !== hostStatusEnum.unTest">{{
                        $t('components.AddHost.currentStatus')
                    }}
                    </div>
                    <el-tag v-if="status === hostStatusEnum.success" color="green">{{
                        $t('components.AddHost.5mphy3snvg80')
                    }}</el-tag>
                    <el-tag v-if="status === hostStatusEnum.fail" color="red">{{
                        $t('components.AddHost.5mphy3snwq40')
                    }}</el-tag>
                </div>
                <div>
                    <el-button @click="close" class="mr">{{ $t('components.AddHost.5mphy3snwxs0') }}</el-button>
                    <el-button :loading="testLoading" class="mr" @click="handleTestHost">{{
                        $t('components.AddHost.5mphy3snx3o0')
                    }}</el-button>
                    <el-popconfirm :title="$t('components.AddHostUser.else2')" type="warning"
                        popper-class="o-popper-confirm" :confirm-button-text="$t('components.Package.5mtcyb0rty17')"
                        :cancel-button-text="$t('components.Package.5mtcyb0rty18')" @confirm="handleConfirm">
                        <template #reference>
                            <el-button :loading="submitLoading" type="primary">{{
                                $t('components.AddHost.5mphy3snx7c0') }}</el-button>
                        </template>
                    </el-popconfirm>
                </div>
            </div>
        </template>
    </el-dialog>
</template>
<script lang="ts" setup>
import { KeyValue } from '@/types/global'
import { FormInstance } from '@arco-design/web-vue/es/form'
import { reactive, ref } from 'vue'
import { addHostUser, editHostUser, hostPing } from '@/api/ops'
import showMessage from '@/utils/showMessage'
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
    formRef.value?.validate(async valid => {
        if (!valid) return;
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
    })
}

const submitLoading = ref<boolean>(false)
const formRef = ref<null | FormInstance>(null)

const close = () => {
    userData.show = false
    formRef.value?.resetFields()
}

const handleConfirm = async () => {
    formRef.value?.validate(async valid => {
        if (!valid) return;
        submitLoading.value = true
        const userPwd = await encryptPassword(userData.formData.password)
        const param = Object.assign({}, userData.formData)
        param.password = userPwd
        if (userData.formData.id) {
            // edit
            editHostUser(userData.formData.id, param).then(() => {
                showMessage('success', t('components.AddHostUser.modifySuccess'))
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
                showMessage('success', t('components.AddHostUser.createSuccess'))
                emits(`finish`)
                close()
            }).finally(() => {
                submitLoading.value = false
            })
        }
    })


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

<style lang="less">
.addUserDia {
    .addUserFormStyle {
        .el-form-item.is-required {
            margin-left: -8px;

            .el-input__inner {
                font-size: 14px;
            }
        }
    }
}
</style>
