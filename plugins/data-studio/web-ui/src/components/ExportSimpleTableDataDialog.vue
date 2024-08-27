<template>
  <el-dialog
    class="common-dialog-wrapper"
    v-model="visible"
    :title="title"
    width="500px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @closed="resetForm"
  >
    <div>
      {{ $t('table.export.chooseFormat') }}
      <el-radio-group v-model="fileType">
        <el-radio v-for="item in fileTypeList" :key="item" :value="item">{{ item }}</el-radio>
      </el-radio-group>
    </div>
    <template #footer>
      <el-button @click="cancel">{{ $t('button.cancel') }}</el-button>
      <el-button type="primary" @click="confirm">{{ $t('button.confirm') }}</el-button>
    </template>
  </el-dialog>
</template>

<script lang="ts" setup>
  import { loadingInstance, downLoadMyBlobType } from '@/utils';
  import { useI18n } from 'vue-i18n';

  const { t } = useI18n();
  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
      title?: string;
      type: 'current' | 'all';
      platform?: Platform;
      params: Record<string, any>;
      api: (...arg) => Promise<any>;
    }>(),
    {
      modelValue: false,
      platform: 'openGauss',
      params: () => ({}),
    },
  );
  const myEmit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void;
    (e: 'confirm'): void;
  }>();
  const visible = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });
  const title = computed(() => props.title || t('table.export.dialogTitle'))

  const loading = ref(null);
  const fileTypeList = ref(
    props.platform == 'openGauss' ? ['Excel(xlsx)', 'Excel(xls)'] : ['Text', 'CSV'],
  );
  const fileType = ref(fileTypeList.value[0]);

  const resetForm = () => {
    fileType.value = fileTypeList.value[0];
  };
  const confirm = async () => {
    visible.value = false;
    const params = {
      ...props.params,
      fileType: fileType.value,
    };
    try {
      loading.value = loadingInstance();
      const res = await props.api(params);
      downLoadMyBlobType(res.name, res.data);
    } finally {
      loading.value.close();
    }
  };
  const cancel = () => {
    visible.value = false;
    resetForm();
  };
</script>
