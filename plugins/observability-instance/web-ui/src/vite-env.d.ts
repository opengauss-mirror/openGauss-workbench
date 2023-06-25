///
/// Copyright (c) 2023 Huawei Technologies Co.,Ltd.
///

/// <reference types="vite/client" />

declare module "*.vue" {
    import type { DefineComponent } from "vue";
    const component: DefineComponent<{}, {}, any>;
    export default component;
}
declare module 'jsencrypt'
