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
