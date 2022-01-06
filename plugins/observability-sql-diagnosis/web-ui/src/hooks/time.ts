import { ComputedGetter, Ref } from "vue"

export const useIntervalTime = (cb: Function, timeFn: ComputedGetter<number> | Ref<number>) => {
    const timer = ref<number>()
    timer.value = setInterval(cb, typeof timeFn === 'function' ? timeFn() : timeFn.value)
    watch(timeFn, t => {
        if (timer.value) {
            clearInterval(timer.value)
            timer.value = setInterval(cb, t)
        }
    })
    onBeforeUnmount(() => {
        if (timer.value) {
            clearInterval(timer.value)
        }
    })
}
