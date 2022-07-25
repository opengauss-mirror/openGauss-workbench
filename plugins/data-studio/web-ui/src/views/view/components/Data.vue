<template>
  <div style="height: 100%">
    <el-auto-resizer>
      <template #default="{ height, width }">
        <el-table-v2
          stripe
          :columns="props.data?.columns || []"
          :data="props.data?.data || []"
          :width="width"
          :height="height"
          fixed
          :header-height="30"
          :row-height="30"
        >
          <template #overlay v-if="props.loading">
            <div
              class="el-loading-mask"
              style="display: flex; align-items: center; justify-content: center"
            >
              <el-icon class="is-loading" color="var(--el-color-primary)" :size="26">
                <loading-icon />
              </el-icon>
            </div>
          </template>
        </el-table-v2>
      </template>
    </el-auto-resizer>
  </div>
</template>

<script lang="ts" setup>
  import { Loading as LoadingIcon } from '@element-plus/icons-vue';
  const props = withDefaults(
    defineProps<{
      data: any;
      loading?: boolean;
    }>(),
    {
      data: [],
      loading: false,
    },
  );
</script>

<style lang="scss" scoped>
  .el-table-v2 {
    font-size: 12px;
  }
  :deep(.el-table-v2__overlay) {
    z-index: 9;
  }
</style>
