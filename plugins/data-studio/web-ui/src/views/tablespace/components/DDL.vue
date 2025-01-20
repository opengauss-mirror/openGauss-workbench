<template>
  <div class="ddl-wrapper" v-loading="props.loading">
    <AceEditor ref="editorRef" type="form" readOnly />
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
  .ddl-wrapper {
    flex: 1;
  }
</style>
