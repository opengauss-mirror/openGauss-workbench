<template>
  <div class="text-tooltip">
    <el-tooltip
      class="item"
      effect="dark"
      :disabled="isShowTooltip"
      :content="content"
      :placement="placement"
    >
      <p class="over-flow" :class="className" @mouseover="onMouseOver(refName)">
        <span :ref="refName">{{content || '--'}}</span>
      </p>
    </el-tooltip>
  </div>
</template>
<script>
export default {
  name: 'textTooltip',
  props: {
    // Displayed text content
    content: {
      type: [String, Number],
      default: () => {
        return ''
      }
    },
    // Location
    placement: {
      type: String,
      default: () => {
        return 'top'
      }
    },
    // The style of the outer frame, the class name passed in can set the display width.
    className: {
      type: String,
      default: () => {
        return '';
      }
    },
    // For page text identification (if the same component is called multiple times on a page, this parameter cannot be repeated.
    refName: {
      type: String,
      default: () => {
        return ''
      }
    }
  },
  data() {
    return {
      isShowTooltip: true,
    }
  },
  methods: {
    onMouseOver(str) {
      let parentWidth = this.$refs[str].parentNode.offsetWidth;
      let contentWidth = this.$refs[str].offsetWidth;
      // If the subset is broader, enable the tooltip function.
      if (contentWidth > parentWidth) {
        this.isShowTooltip = false
      } else {
        // Otherwise, turn off the tooltip.
        this.isShowTooltip = true
      }
    }
  }
}
</script>

<style lang="less" scoped>
.over-flow {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
p {
  margin: 0;
}

</style>
