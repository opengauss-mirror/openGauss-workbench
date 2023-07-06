<template>
    <el-dialog :width="400" v-model="visible" :close-on-click-modal="false" custom-class="del-dialog" center draggable
        @close="close">
        <template #header></template>
        <div class="del-text">{{ t('app.deleteTip0') }}</div>
        <div class="content-text">{{ props.content }}</div>
        <template #footer>
            <el-button style="padding: 5px 20px;" type="primary" @click="confirm">{{ t('app.confirm') }}</el-button>
            <el-button style="padding: 5px 20px;" @click="() => visible = false">{{ t('app.cancel') }}</el-button>
        </template>
    </el-dialog>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
const { t } = useI18n();
const props = withDefaults(
    defineProps<{
        content: string
        id: string
        resolve: () => void
    }>(),
    {}
);
const visible = ref(true)
const close = () => document.getElementById(props.id)?.remove()
const confirm = () => {
    props.resolve()
    visible.value = false
}
</script>

<style>
.del-dialog .el-dialog__header {
    display: none;
}
</style>
<style scoped>
.del-text {
    text-align: center;
    font-weight: bold;
    font-size: 16px;
    margin-bottom: 20px;
}

.content-text {
    text-align: center;
}
</style>
