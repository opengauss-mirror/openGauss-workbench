<script setup lang="ts">
import { RouteRecordRaw } from 'vue-router';
import _ from 'lodash-es';
import { routes } from '../router/index';

const props = withDefaults(defineProps<{
    collapse: boolean
}>(), {})

const route = useRoute();

type appDataType = {
    curRoutes: Array<RouteRecordRaw>
}
const appData = reactive<appDataType>({
    curRoutes: []
});

const deatRoute = (r: Array<RouteRecordRaw>) => {
    return r.filter(item => !item.meta?.hidden).map(m => {
        if (Array.isArray(m.children)) {
            m.children = deatRoute(m.children);
        }
        return m;
    });
}

onMounted(() => {
    appData.curRoutes = deatRoute(_.cloneDeep(routes));
});
</script>

<template>
<div class="logo" v-if="!props.collapse">{{ $t('app.name') }}</div>
<el-menu
    class="vem-menu"
    :default-active="route.path"
    router
    :collapse="props.collapse"
    :collapse-transition="false"
    :style="{width: collapse ? 'auto' : '200px'}"
>
    <div v-for="route in appData.curRoutes" :key="route.path">
        <el-sub-menu v-if="route.children && route.children.length > 0" :index="route.path">
            <template #title>
                <svg-icon
                    :name="route.meta?.icon!"
                    style="margin-right: 6px"
                />
                <span>{{ $t(route.meta?.title!) }}</span>
            </template>
            <el-menu-item v-for="sub in route.children" :key="sub.path" :index="sub.path">
                {{ $t(sub.meta?.title!) }}
            </el-menu-item>
        </el-sub-menu>
        <el-menu-item v-else :index="route.path">
            <svg-icon
                :name="route.meta?.icon!"
                style="margin-right: 6px"
            />
            <template #title>{{ $t(route.meta?.title!) }}</template>
        </el-menu-item>
    </div>
</el-menu>
</template>

<style scoped lang="scss">
.logo {
    width: auto;
    height: 60px;
    // color: $og-text-color;
    line-height: 60px;
    text-align: center;
    font-size: 18px;
    font-weight: bold;
    background-color: var(--el-menu-bg-color);
}
.vem-menu {
    overflow-y: auto;
    overflow-x: hidden;
    height: 100%;
    border-right: 0;
}
.vem-menu.el-menu--collapse .el-sub-menu__title {
    span {
        display: none;
    }
    .el-sub-menu__icon-arrow {
        display: none!important;
    }
}
</style>
