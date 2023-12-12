<template>
  <div class="ddl-wrapper" v-loading="props.loading">
    <AceEditor ref="editorRef" />
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
  .ddl-wrapper {
    flex: 1;
    height: 100%;
  }
</style>
