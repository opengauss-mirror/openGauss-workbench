<template>
  <div class="smart-label-icon" :style="containerStyle">
    <el-icon :size="size" :color="computedIconColor">
      <component :is="iconComponent" />
    </el-icon>
    <div
      class="centered-label"
      :style="computedLabelStyle"
    >
      {{ labelText }}
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Document } from '@element-plus/icons-vue'

const props = defineProps({
  iconComponent: {
    type: [Object, String],
    default: Document
  },
  size: {
    type: [Number, String],
    default: 32
  },
  labelText: {
    type: String,
    default: 'ZIP'
  },
  type: {
    type: String,
    default: 'primary',
    validator: (value) => [
      'primary', 'default', 'success',
      'warning', 'danger', 'info'
    ].includes(value)
  },
  iconColor: {
    type: String,
    default: null
  },
  labelColor: {
    type: String,
    default: null
  },
  labelTextColor: {
    type: String,
    default: null
  },
  labelRadius: {
    type: String,
    default: '4px'
  },
  labelPadding: {
    type: String,
    default: '2px 8px'
  }
})

const colorMap = {
  primary: {
    icon: 'var(--el-color-primary)',
    label: 'var(--el-color-primary)',
    text: 'var(--el-color-white)'
  },
  default: {
    icon: 'var(--el-text-color-regular)',
    label: 'var(--el-text-color-secondary)',
    text: 'var(--el-color-white)'
  },
  success: {
    icon: 'var(--el-color-success)',
    label: 'var(--el-color-success)',
    text: 'var(--el-color-white)'
  },
  warning: {
    icon: 'var(--el-color-warning)',
    label: 'var(--el-color-warning)',
    text: 'var(--el-color-white)'
  },
  danger: {
    icon: 'var(--el-color-danger)',
    label: 'var(--el-color-danger)',
    text: 'var(--el-color-white)'
  },
  info: {
    icon: 'var(--el-color-info)',
    label: 'var(--el-color-info)',
    text: 'var(--el-color-white)'
  }
}

const computedIconColor = computed(() => {
  return props.iconColor || colorMap[props.type].icon
})

const computedLabelStyle = computed(() => {
  const baseColors = colorMap[props.type]
  return {
    backgroundColor: props.labelColor || baseColors.label,
    color: props.labelTextColor || baseColors.text,
    borderRadius: props.labelRadius,
    padding: props.labelPadding,
    position: 'absolute',
    top: '50%',
    left: '50%',
    transform: 'translate(-50%, -30%)',
    fontWeight: 'bold',
    boxShadow: '0 1px 3px rgba(0,0,0,0.2)',
    textTransform: 'uppercase',
    letterSpacing: '0.5px',
    whiteSpace: 'nowrap',
    lineHeight: '1',
    fontSize: `calc(${typeof props.size === 'number' ? props.size : parseInt(props.size)}px * 0.35)`,
    zIndex: 1
  }
})

const containerStyle = computed(() => ({
  fontSize: typeof props.size === 'number' ? `${props.size}px` : props.size,
  color: computedIconColor.value,
  position: 'relative',
  display: 'inline-flex',
  alignItems: 'center',
  justifyContent: 'center',
  width: '1em',
  height: '1em'
}))
</script>

<style scoped>
.smart-label-icon {
  display: inline-block;
  vertical-align: middle;
}

.centered-label {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto,
  Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue',
  sans-serif;
  pointer-events: none;
}
</style>
