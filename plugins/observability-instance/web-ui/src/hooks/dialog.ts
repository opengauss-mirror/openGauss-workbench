// Copyright (c) 2023 Huawei Technologies Co., Ltd.
// openGauss-workbench is licensed under Mulan PSL v2.
// You can use this software according to the terms and conditions of the Mulan PSL v2.
// You may obtain a copy of Mulan PSL v2 at:
//          http://license.coscl.org.cn/MulanPSL2
// THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
// EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
// MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
// See the Mulan PSL v2 for more details.

import { uuid } from "../shared";
import DelModalVue from "./DelModal.vue";

/**
 * delete comfirm
 * @param content delete tip
 * @returns void
 */
export const useDelConfirm = (content: string) => {
    return new Promise((resolve) => {
        const el = document.createElement('div');
        el.id = 'del_' + uuid()
        document.body.appendChild(el)
        createApp(DelModalVue, { id: el.id, resolve, content }).mount(`#${el.id}`)
    })
}
