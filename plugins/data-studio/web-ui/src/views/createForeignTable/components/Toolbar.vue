<template>
  <div class="table-toolbar">
    <div class="toolbar-left">
      <template v-for="(item, key) in commonButton" :key="key">
        <div class="tool-item-wrapper" v-if="item.show">
          <div
            v-if="item.type == 'button'"
            :class="['tool-item', { disabled: !item.enabled }]"
            @click="item.enabled && item.on()"
          >
            <span
              :class="['iconfont', item.icon]"
              :style="{
                color: item.enabled ? item.color : item.disabledColor,
              }"
            ></span>
            <span class="tool-name">{{ item.name }}</span>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup>
  const props = withDefaults(
    defineProps<{
      status: Record<string, boolean>;
      isGlobalEnable?: boolean;
      pageNum?: number;
      pageSize?: number;
      pageTotal?: number;
    }>(),
    {
      isGlobalEnable: true,
    },
  );
  const emit = defineEmits<{
    (e: 'addLine'): void;
    (e: 'copyLine'): void;
    (e: 'removeLine'): void;
  }>();

  const commonButton = computed(() => {
    return [
      {
        name: '',
        type: 'button',
        show: true,
        enabled: props.status.addLine,
        on: () => emit('addLine'),
        icon: 'icon-jia1',
        color: '#4fa643',
        disabledColor: '#4fa64340',
      },
      {
        name: '',
        type: 'button',
        show: false,
        enabled: props.status.copyLine,
        on: () => emit('copyLine'),
        icon: 'icon-copy-add',
        color: '#1afa29',
        disabledColor: '#1afa2940',
      },
      {
        name: '',
        type: 'button',
        show: true,
        enabled: props.status.removeLine,
        on: () => emit('removeLine'),
        icon: 'icon-jian1',
        color: '#d81e06',
        disabledColor: '#d81e0640',
      },
    ];
  });
</script>

<style lang="scss" scoped>
  @import url('@/styles/table-toolbar.scss');
</style>
