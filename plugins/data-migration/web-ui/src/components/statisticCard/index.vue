<template>
    <div class="main-card center">
        <div>
            <div class="count center">{{ props.count ?? '--' }}</div>
            <div class="center" :class="props.type">{{ props.description || '--' }}</div>
        </div>
    </div>
</template>
<script setup lang="ts">
type infoType = 'default' | 'primary' | 'success' | 'danger' | 'wait'
const props = defineProps(({
    count: {
        type: [Number, String],
        required: true,
    },
    description: {
        type: String,
        required: true,
    },
    type: {
        type: String as () => infoType,
        require: false,
        default: 'default'
    }
}))

</script>
<style lang="less" scoped>
.primary {
    color: var(--o-color-primary);
}

.success {
    color: var(--o-color-success)
}

.danger {
    color: var(--o-color-danger)
}

.wait {
    color: #ACACAD
}

.center {
    display: flex;
    justify-content: center;
    align-items: center;
}

.main-card {
    background-color: var(--o-bg-color-light2);
    color: var(--o-text-color-primary);
    border-radius: 8px;
    width: 182px;
    height: 80px;
    text-align: center;

    .count {
        font-size: 24px;
        margin-bottom: 4px;

    }

}</style>
