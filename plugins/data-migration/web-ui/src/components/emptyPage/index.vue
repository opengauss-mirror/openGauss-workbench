<template >
    <div class="o-table-empty">
        <el-icon class="o-empty-icon mt24">
            <IconEmpty />
        </el-icon>
        <div class="o-empty-label">
          {{ t('components.SubTaskDetail.noData') }}
        </div>
    </div>
</template>
<script setup lang="ts">
import { IconEmpty } from '@computing/opendesign-icons';
import { useI18n } from "vue-i18n";
const { t } = useI18n();
</script>
<style lang="less" scoped>

</style>
