<script lang="ts" name="EllipsisWithElTooltip">
  import { ref, onMounted, nextTick, defineComponent, h } from 'vue';
  import type { ElTooltipProps } from 'element-plus';
  import { ElTooltip } from 'element-plus';

  type ToolTipProps = Partial<
    Pick<
      ElTooltipProps,
      | 'effect'
      | 'enterable'
      | 'hideAfter'
      | 'offset'
      | 'placement'
      | 'popperClass'
      | 'popperOptions'
      | 'showAfter'
      | 'showArrow'
      | 'trigger'
    >
  >;
  export default defineComponent({
    name: 'EllipsisWithElTooltip',
    props: {
      toolTipProps: {
        type: Object as () => ToolTipProps,
        default: () => ({}),
      },
    },
    setup(props, { slots }) {
      const textToolTipRef = ref<HTMLElement>();
      const showToolTip = ref(false);
      const textContentRef = ref(null);

      const updateOverflow = () => {
        if (textContentRef.value) {
          const el = textContentRef.value as HTMLElement;
          showToolTip.value = el?.scrollWidth > el?.clientWidth;
        } else {
          showToolTip.value = false;
        }
      };

      onMounted(() => {
        nextTick(() => {
          updateOverflow();
          new ResizeObserver(updateOverflow).observe(textToolTipRef.value as HTMLElement);
        });
      });
      return () =>
        h('div', { ref: textToolTipRef, class: 'text-tooltip' }, [
          h(
            ElTooltip,
            {
              ...props.toolTipProps,
              disabled: !showToolTip.value,
              effect: 'light',
            },
            {
              default: () =>
                h(
                  'div',
                  { ref: textContentRef, class: 'ellipsis-content' },
                  { default: slots.default },
                ),
              content: slots.content ?? slots.default,
            },
          ),
        ]);
    },
  });
</script>

<style scoped>
  .text-tooltip {
    width: 100%;
  }

  .text-tooltip .ellipsis-content {
    width: 100%;
    overflow: hidden;
    overflow-wrap: break-word;
    text-overflow: ellipsis;
    white-space: nowrap;
    word-break: break-all;
  }
</style>
