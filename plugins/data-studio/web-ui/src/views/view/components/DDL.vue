<template>
  <div class="ace-wrapper" v-loading="props.loading">
    <AceEditor ref="editorRef" style="margin: 4px 0; border: 1px solid #ddd" />
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
</script>

<style>
  .ace-wrapper {
    flex: 1;
    padding: 10px;
    height: 100%;
  }
</style>
