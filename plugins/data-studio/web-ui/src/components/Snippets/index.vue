<template>
  <div class="snippets-wrapper">
    <el-drawer
      v-model="showDrawer"
      size="40%"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="isInList"
      :before-close="handleClose"
      @opened="handleOpened"
      class="my-snippets-drawer"
    >
      <template #header>
        <h4>
          <el-button v-if="isInList" type="primary" @click="handleCreate">创建</el-button>
          <el-button v-else @click="toggleSnippetsList">返回</el-button>
        </h4>
      </template>
      <SnippetsList v-if="isInList" ref="snippetsListRef" />
      <CreateSnippets v-else :type="createType" :codeId="codeId" :code="fastCode" />
    </el-drawer>
  </div>
</template>

<script lang="ts" setup>
  import SnippetsList from './SnippetsList.vue';
  import CreateSnippets from './CreateSnippets.vue';

  const props = withDefaults(
    defineProps<{
      modelValue: boolean;
    }>(),
    {
      modelValue: false,
    },
  );
  const myEmit = defineEmits<{
    (event: 'update:modelValue', text: boolean): void;
  }>();
  const showDrawer = computed({
    get: () => props.modelValue,
    set: (val) => myEmit('update:modelValue', val),
  });
  const snippetsListRef = ref<InstanceType<typeof SnippetsList>>();
  const isInList = ref(true);
  const createType = ref<'create' | 'edit'>('create');
  const codeId = ref('');
  const fastCode = ref('');

  const toggleSnippetsList = ({ id }: { id?: string } = {}) => {
    if (id) {
      codeId.value = id;
      createType.value = 'edit';
    } else {
      createType.value = 'create';
    }
    isInList.value = !isInList.value;
    if (isInList.value) {
      codeId.value = '';
      fastCode.value = '';
    }
    nextTick(() => {
      isInList.value && snippetsListRef.value.handleQuery();
    });
  };
  provide('toggleSnippetsList', toggleSnippetsList);
  const handleCreate = () => {
    createType.value = 'create';
    isInList.value = false;
  };
  const handleFastCreate = (text) => {
    createType.value = 'create';
    fastCode.value = text;
    isInList.value = false;
  };
  const handleOpened = () => {
    !fastCode.value && snippetsListRef.value.handleQuery();
  };
  const handleClose = (done: () => void) => {
    snippetsListRef.value.resetQueryPage();
    done();
  };

  defineExpose({
    handleFastCreate,
  });
</script>
