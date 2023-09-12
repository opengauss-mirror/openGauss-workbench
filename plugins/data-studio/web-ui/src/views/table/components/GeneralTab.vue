<template>
  <el-table :data="props.data" v-loading="props.loading" border>
    <el-table-column property="attr" :label="$t('common.attribute')" align="center" />
    <el-table-column prop="value" :label="$t('common.value')" align="center" />
  </el-table>
</template>

<script lang="ts" setup>
  const props = withDefaults(
    defineProps<{
      columns: string[];
      data: any[];
      loading?: boolean;
    }>(),
    {
      columns: () => [],
      data: () => [],
      loading: false,
    },
  );
</script>
