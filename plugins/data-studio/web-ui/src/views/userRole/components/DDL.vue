<template>
  <div class="monaco-wrapper" v-loading="props.loading">
    <AceEditor ref="editorRef" readOnly style="margin: 4px 0; border: 1px solid #ddd" />
  </div>
</template>

<script lang="ts" setup>
  import AceEditor from '@/components/AceEditor.vue';

  const props = withDefaults(
    defineProps<{
      data: any;
      loading?: boolean;
    }>(),
    {
      data: ``,
      loading: false,
    },
  );

  const editorRef = ref();
  watch(
    () => props.data,
    (value) => {
      editorRef.value.setValue(value);
    },
  );

  onMounted(() => {
    editorRef.value.setValue(props.data);
  });
</script>

<style lang="scss" scoped>
  .monaco-wrapper {
    flex: 1;
    :deep(.ace-editor) {
      margin: 0 !important;
    }
  }
</style>
