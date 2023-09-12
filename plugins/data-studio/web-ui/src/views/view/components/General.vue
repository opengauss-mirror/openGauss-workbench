<template>
  <el-table :data="props.data" v-loading="props.loading" border>
    <el-table-column type="index" width="100" :label="$t('common.serialNumber')" align="center" />
    <el-table-column
      property="attr"
      :min-width="200"
      :label="$t('common.attribute')"
      align="center"
    />
    <el-table-column prop="value" :min-width="300" :label="$t('common.value')" align="center" />
  </el-table>
</template>

<script lang="ts" setup>
  const props = withDefaults(
    defineProps<{
      data: any[];
      loading?: boolean;
    }>(),
    {
      data: () => [],
      loading: false,
    },
  );
</script>
