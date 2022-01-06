import { useEventListener } from "@vueuse/core";
import { Ref } from "vue";

/**
 * is dom element overflow
 * @param targetRef Ref<dom>
 * @param dropdownRef element-plus dropdownRef
 * @returns is overflow
 */
export const useWidthOverflow = (targetRef: Ref<HTMLElement>, dropdownRef: Ref<any>) => {
    const overflow = ref(false)
    const isFinish = ref(false)
    const stopWatch = watch(targetRef, t => {
        if (t) {
            overflow.value = t.scrollWidth > t.offsetWidth
            isFinish.value = true
            useEventListener(t, 'mouseenter', (e: MouseEvent) => {
                if (dropdownRef && dropdownRef.value) {
                    const l = t.getBoundingClientRect().left - document.querySelector('#app')!.getBoundingClientRect().left
                    dropdownRef.value.$el.style.left = e.pageX - l + 'px'
                    if (overflow.value) {
                        dropdownRef.value.handleOpen()
                    }
                }
            })
            useEventListener(t, 'mouseleave', (e: MouseEvent) => {
                if (dropdownRef && dropdownRef.value) {
                    dropdownRef.value.handleClose()
                }
            })
            stopWatch()
        }
    }, { immediate: true })
    const trigger = () => {
        isFinish.value = false
        overflow.value = false
        if (targetRef && targetRef.value) {
            targetRef.value.style.justifyContent = 'flex-start';
            overflow.value = targetRef.value.scrollWidth > targetRef.value.offsetWidth
            isFinish.value = true
        }
    }
    useEventListener(window, 'resize', trigger)
    return { overflow, isFinish, trigger }
}
