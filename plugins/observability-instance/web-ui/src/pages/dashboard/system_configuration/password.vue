<template>
    <div class="dialog">
        <el-dialog width="400px" :title="$t('configParam.rootPWDTitle')" v-model="visible" :close-on-click-modal="false" draggable @close="closeDialog">
            <div class="dialog-content">
                <el-form :model="formData" :rules="connectionFormRules" ref="connectionFormRef">
                    <el-form-item :label="t('configParam.rootPWD')" prop="rootPassword">
                        <el-input v-model="formData.rootPassword" show-password style="width: 200px; margin: 0 4px" />
                    </el-form-item>
                </el-form>
            </div>

            <template #footer>
                <el-button style="padding: 5px 20px" type="primary" @click="handleconfirmModel">{{ $t("app.confirm") }}</el-button>
                <el-button style="padding: 5px 20px" @click="handleCancelModel">{{ $t("app.cancel") }}</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script lang="ts" setup>
import { cloneDeep } from "lodash-es";
import { FormRules, FormInstance } from "element-plus";
import { useI18n } from "vue-i18n";
import { encryptPassword } from "../../../utils/jsencrypt";
const { t } = useI18n();

const visible = ref(false);
const props = withDefaults(
    defineProps<{
        show: boolean;
    }>(),
    {}
);
watch(
    () => props.show,
    (newValue) => {
        visible.value = newValue;
    },
    { immediate: true }
);

// form data
const initFormData = {
    rootPassword: "",
};
const formData = reactive(cloneDeep(initFormData));

// build
const emit = defineEmits(["changeModal", "confirm"]);
async function handleconfirmModel() {
    let result = await connectionFormRef.value?.validate();
    if (!result) return;
    const encryptPwd = await encryptPassword(formData.rootPassword);
    emit("confirm", encryptPwd);
    visible.value = false;
    emit("changeModal", visible.value);
}
const connectionFormRef = ref<FormInstance>();
const connectionFormRules = reactive<FormRules>({
    rootPassword: [{ required: true, message: t("configParam.rootPWDTitle"), trigger: "blur" }],
});

const handleCancelModel = () => {
    visible.value = false;
    emit("changeModal", visible.value);
};
const closeDialog = () => {
    visible.value = false;
    emit("changeModal", visible.value);
};
</script>
<style lang="scss" scoped>
@import "../../../assets/style/style1.scss";
</style>
