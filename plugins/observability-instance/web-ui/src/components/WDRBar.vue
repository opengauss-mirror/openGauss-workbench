<template>
    <MyDatePicker v-model="dateValue" type="datetimerange" style="width: 315px; visibility: hidden" />

    <div class="refresh-bar2-container">
        <div class="refresh-bar2-filter">
            <div style="width: 600px"></div>
            <MyDatePicker
                v-model="dateValue"
                :start-placeholder="$t('app.startDate')"
                :end-placeholder="$t('app.endDate')"
                type="datetimerange"
                style="width: 400px; pointer-events: all"
            />
        </div>
    </div>
</template>

<script lang="ts" setup>
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'

dayjs.extend(utc)
dayjs.extend(timezone)

const props = withDefaults(
    defineProps<{
        modelValue: any | any[]
        type: any
        showShortcuts?: boolean
        disabled?: boolean
        valueFormatToISO?: boolean // Whether to process as ISO8601 format string '2020-04-02T08:02:17-05:00'
        valueFormatToUTC?: boolean // Whether to process as UTC Universal Time
    }>(),
    {
        showShortcuts: true,
        valueFormatToISO: false,
        valueFormatToUTC: false,
    }
)

const myEmit = defineEmits<{
    (event: 'change', value: any | any[]): void
    (event: 'update:modelValue', value: any | any[]): void
}>()
const dateValue = computed({
    get: () => {
        return props.modelValue
    },
    set: (val) => {
        myEmit('update:modelValue', val)
        myEmit('change', val)
    },
})
</script>

<style lang="scss" scoped>
.refresh-bar2 {
    &-container {
        height: 500px;
        position: absolute;
        right: 0px;
        top: 0px;
        display: flex;
        justify-content: end;
        overflow: hidden;
        padding: 0px !important;
        pointer-events: none;
        .myDatePicker {
            pointer-events: all !important;
        }
    }

    &-filter {
        z-index: -1;
        width: 800px;
        z-index: 10;
        display: flex;
        align-items: flex-start;
        height: 30px;
        padding: 0px !important;
    }
}
</style>
