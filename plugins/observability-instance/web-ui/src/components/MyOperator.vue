<template>
    <div class="operator">
        <div class="left"><slot name="left" /></div>
        <div class="right"><slot name="right" /></div>
    </div>
</template>

<style scoped lang="scss">
    .operator {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
        .left {
            display: flex;
        }
        .right {
            display: flex;
            justify-content: flex-end;
            gap: 12px;
        }
    }
</style>
