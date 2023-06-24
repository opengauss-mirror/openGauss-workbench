<script setup lang="ts">
import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'
import { useI18n } from 'vue-i18n'
const { t } = useI18n()

dayjs.extend(utc)
dayjs.extend(timezone)

interface ShortcutsConfigObj {
    text: string
    value: number
}

const props = withDefaults(
    defineProps<{
        modelValue: any | any[]
        type: any
        showShortcuts?: boolean
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

const getShortcutsConfig = () => {
    const { showShortcuts, type } = props
    let shortcutsArr: any[] = []
    const shortcutsConfig = [
        { text: t('app.lastOneHour'), value: 1 },
        { text: t('app.lastThreeHour'), value: 3 },
        { text: t('app.lastSixHour'), value: 6 },
    ]
    if ((type === 'datetimerange' || type === 'daterange') && showShortcuts) {
        shortcutsConfig.forEach((item: ShortcutsConfigObj) => {
            shortcutsArr.push({
                text: item.text,
                value: () => {
                    const end = new Date()
                    const start = new Date()
                    start.setTime(start.getTime() - 3600 * 1000 * item.value)
                    return [start, end]
                },
            })
        })
    }
    return shortcutsArr
}

const onDatePackerVisible = (v: boolean) => {
    if (!v) {
        const docu = document.getElementsByClassName('el-range-input')
        if (docu.length > 0) {
            for (let index = 0; index < docu.length; index++) {
                const element = docu[index]
                // @ts-ignore
                element.blur()
            }
        }
    }
}

const shortcuts = getShortcutsConfig()

const dateValue = computed({
    get: () => {
        return props.modelValue
    },
    set: (val) => {
        let value
        if (val && props.valueFormatToISO) {
            value = Array.isArray(val)
                ? val.map((item) => dayjs(item).tz(dayjs.tz.guess()).format())
                : dayjs(val).tz(dayjs.tz.guess()).format()
        } else if (val && props.valueFormatToUTC) {
            value = Array.isArray(val) ? val.map((item) => dayjs(item).utc().format()) : dayjs(val).utc().format()
        } else if (val && !props.valueFormatToISO && !props.valueFormatToUTC) {
            let fmt: string
            switch (props.type) {
                case 'date':
                case 'daterange':
                    fmt = 'YYYY-MM-DD'
                    break
                case 'datetime':
                case 'datetimerange':
                    fmt = 'YYYY-MM-DD HH:mm:ss'
                    break
                case 'year':
                    fmt = 'YYYY'
                    break
                case 'month':
                    fmt = 'YYYY-MM'
                    break
                default:
                    fmt = 'YYYY-MM-DD'
                    break
            }
            value = Array.isArray(val) ? val.map((item) => dayjs(item).format(fmt)) : dayjs(val).format(fmt)
        } else {
            value = Array.isArray(props.modelValue) ? [] : null
        }
        myEmit('update:modelValue', value)
        myEmit('change', value)
    },
})
</script>

<template>
    <el-date-picker
        ref="datePickerRef"
        @visible-change="onDatePackerVisible"
        popper-class="myDatePicker"
        unlink-panels
        v-bind="$attrs"
        v-model="dateValue"
        :type="type"
        :shortcuts="shortcuts"
        :start-placeholder="$t('app.startTime')"
        :end-placeholder="$t('app.endTime')"
    />
</template>
