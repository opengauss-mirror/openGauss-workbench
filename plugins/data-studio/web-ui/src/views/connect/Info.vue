<template>
  <div>
    <el-dialog
      v-model="visible"
      :title="$t('connection.props')"
      width="600px"
      @open="handleOpen"
      @close="myEmit('update:modelValue', false)"
    >
      <el-table :data="gridData" stripe border>
        <el-table-column property="attr" :label="$t('connection.attribute')" />
        <el-table-column property="value" :label="$t('connection.value')" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
  import { getDatabaseAttr } from '@/api/connect';
  import { useI18n } from 'vue-i18n';
  import { useUserStore } from '@/store/modules/user';

  const { t } = useI18n();
  const UserStore = useUserStore();
  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      connectInfo: {
        id: string | number;
      };
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

  const gridData = ref([]);
  const info_remote = {
    name: t('connection.attribute_table.name'),
    host: t('connection.attribute_table.host'),
    port: t('connection.attribute_table.port'),
    userName: t('connection.attribute_table.username'),
    ip: t('connection.attribute_table.ip'),
    type: t('connection.attribute_table.type'),
  };

  const handleOpen = async () => {
    gridData.value = [];
    const data: any = await getDatabaseAttr({
      id: props.connectInfo.id,
      webUser: UserStore.userId,
    });
    data.host = data.ip;
    Object.keys(info_remote).forEach((item) => {
      gridData.value.push({ attr: info_remote[item], value: data[item] || '-' });
    });
  };
</script>

<style scoped lang="scss">
  :deep(.el-dialog__body) {
    padding-top: 0;
  }
</style>
