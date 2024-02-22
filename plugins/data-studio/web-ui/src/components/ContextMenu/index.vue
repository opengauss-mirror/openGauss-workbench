<template>
  <transition name="context-menu">
    <div class="context-menu" v-show="show" :style="style" @mousedown.stop @contextmenu.prevent>
      <slot>
        <ul v-if="props.list.length">
          <li v-for="(item, index) in props.list" :key="index" @click="handleMenuItemClick(item)">
            {{ item.label }}
          </li>
        </ul>
      </slot>
    </div>
  </transition>
</template>
<script lang="ts" setup name="ContextMenu">
  import { getCurrentInstance } from 'vue';

  interface Menu {
    label: string;
    click: () => any;
  }
  const { proxy } = getCurrentInstance();
  const style = ref({});
  const props = withDefaults(
    defineProps<{
      offset: { top: number; left: number };
      show: boolean;
      list?: Menu[];
    }>(),
    {
      offset: () => ({
        left: 0,
        top: 0,
      }),
      show: false,
      list: () => [],
    },
  );
  const emit = defineEmits<{
    (e: 'update:show', value: boolean): void;
  }>();

  watch(
    () => props.show,
    (value) => {
      value && nextTick(setPosition);
    },
  );

  const handleMenuItemClick = (menu: Menu) => {
    menu.click();
    clickDocumentHandler();
  };
  const clickDocumentHandler = () => {
    props.show && emit('update:show', false);
  };
  const setPosition = () => {
    let docHeight = document.documentElement.clientHeight;
    let docWidth = document.documentElement.clientWidth;
    let menuHeight = proxy.$el.getBoundingClientRect().height;
    let menuWidth = proxy.$el.getBoundingClientRect().width;
    // Increase the spacing between clicks and menus, which is more aesthetically pleasing
    const gap = 3;
    let topover = props.offset.top + menuHeight + gap >= docHeight ? menuHeight + gap : -gap;
    let leftover = props.offset.left + menuWidth + gap >= docWidth ? menuWidth + gap : -gap;
    style.value = {
      left: `${props.offset.left - leftover}px`,
      top: `${props.offset.top - topover}px`,
    };
  };

  onMounted(() => {
    document.body.appendChild(proxy.$el);
    document.addEventListener('mousedown', clickDocumentHandler);
    window.addEventListener('resize', clickDocumentHandler);
  });
  onBeforeUnmount(() => {
    let popperElm = proxy.$el;
    if (popperElm && popperElm.parentNode === document.body) {
      document.body.removeChild(popperElm);
    }
    document.removeEventListener('mousedown', clickDocumentHandler);
    window.removeEventListener('resize', clickDocumentHandler);
  });
</script>
<style lang="scss" scoped>
  .context-menu {
    z-index: 2300;
    display: block;
    position: fixed;
    color: initial;
    box-shadow: 0px 0px 12px rgba(0, 0, 0, 0.12);
    &-enter,
    &-leave-to {
      opacity: 0;
    }
    &-enter-active,
    &-leave-active {
      transition: opacity 0.15s;
    }
    ul {
      padding: 3px 0;
      margin: 0;
      list-style-type: none;
      border-radius: 4px;
      background-color: var(--el-bg-color-overlay);
    }
    li {
      padding: 2px 12px;
      line-height: 20px;
      display: flex;
      align-items: center;
      white-space: nowrap;
      list-style: none;
      margin: 0;
      cursor: pointer;
      outline: 0;
      &.disabled {
        color: var(--color-text-4);
        cursor: not-allowed;
      }
      &:not(.disabled):hover {
        background-color: var(--color-fill-2);
      }
    }
  }
</style>
