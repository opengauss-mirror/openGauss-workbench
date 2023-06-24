///
/// Copyright (c) 2023 Huawei Technologies Co.,Ltd.
///

import { getCurrentInstance } from 'vue'

export default function useGetGlobalProperties() {
    const {
        emit,
        appContext: {
            app: {
                config: { globalProperties },
            },
        },
    } = getCurrentInstance()

    return { ...globalProperties }
}
