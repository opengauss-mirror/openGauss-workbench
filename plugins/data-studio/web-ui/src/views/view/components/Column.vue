<template>
  <AdvancedTable
    :data="props.data"
    :loading="props.loading"
    :row-key="props.rowKey"
    :columns="columns"
    :menuList="['advancedCopy']"
  >
    <el-table-column prop="name" :label="$t('table.column.columnName')" align="center" />
    <el-table-column prop="type" :label="$t('table.column.dataType')" align="center" />
    <el-table-column prop="notnull" :label="$t('table.column.canBeNotNull')" align="center" />
  </AdvancedTable>
</template>

<script lang="ts" setup>
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      data: any[];
      rowKey: string;
      loading?: boolean;
    }>(),
    {
      data: () => [],
      rowKey: '_id',
      loading: false,
    },
  );
  const columns = computed(() => {
    return [
      {
        label: t('table.column.columnName'),
        prop: 'name',
      },
      {
        label: t('table.column.dataType'),
        prop: 'type',
      },
      {
        label: t('table.column.canBeNotNull'),
        prop: 'notnull',
      },
    ];
  });
</script>
