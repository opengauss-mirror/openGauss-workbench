<template>
  <div class="db-dialog">
    <el-dialog
      v-model="visible"
      :title="$t('database.property')"
      :width="500"
      align-center
      :close-on-click-modal="false"
      @opened="handleOpen"
      @close="handleClose"
    >
      <div class="dialog_body">
        <el-table :data="data" v-loading="loading" width="100%" border>
          <el-table-column prop="attr" :label="$t('database.attribute')" align="center" />
          <el-table-column prop="value" :label="$t('database.value')" align="center" />
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>
<script lang="ts" setup>
  import { computed, ref } from 'vue';
  import { useI18n } from 'vue-i18n';
  import { getDatabaseAttribute } from '@/api/database';

  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      uuid: string;
      databaseName: string;
    }>(),
    {
      modelValue: false,
    },
  );
  const myEmit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
  }>();
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });

  const { t } = useI18n();
  const loading = ref(false);
  const data = ref([]);

  const infoObj = {
    oid: 'OID',
    datname: t('database.info.name'),
    encoding: t('database.info.encoding'),
    datallowconn: t('database.info.datallowconn'),
    datconnlimit: t('database.info.datconnlimit'),
    dattablespace: t('database.info.dattablespace'),
    datcollate: t('database.info.datcollate'),
    datctype: t('database.info.datctype'),
  };
  const handleOpen = async () => {
    data.value = [];
    try {
      loading.value = true;
      const res: any = await getDatabaseAttribute({
        uuid: props.uuid,
        databaseName: props.databaseName,
      });
      data.value = res.column.map((col, index) => {
        return {
          attr: infoObj[col],
          value: res.result[0]?.[index] || '-',
        };
      });
    } finally {
      loading.value = false;
    }
  };
  const handleClose = () => {
    myEmit('update:modelValue', false);
  };
</script>

<style lang="scss" scoped>
  .db-dialog {
    :deep(.el-dialog__body) {
      padding-top: 5px;
      padding-bottom: 20px;
    }
    :deep(.el-select) {
      width: 100%;
    }
  }
</style>
