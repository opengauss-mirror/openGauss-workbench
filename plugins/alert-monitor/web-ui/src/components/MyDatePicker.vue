<template>
    <el-date-picker v-bind="$attrs" v-model="dateValue" :type="type" :shortcuts="shortcuts"
        :start-placeholder="startPlaceholder" :end-placeholder="endPlaceholder" />
</template>

<script setup lang="ts">
import dayjs from 'dayjs';
import utc from 'dayjs/plugin/utc';
import timezone from 'dayjs/plugin/timezone';

dayjs.extend(utc);
dayjs.extend(timezone);

interface ShortcutsConfigObj {
    text: string;
    value: number;
}

const props = withDefaults(
    defineProps<{
        modelValue: any | any[];
        type: any;
        showShortcuts?: boolean,
        startPlaceholder?: string;
        endPlaceholder?: string;
        valueFormatToISO?: boolean;
        shortcutsConfig?: any[];
    }>(),
    {
        startPlaceholder: '',
        endPlaceholder: '',
        showShortcuts: true,
        valueFormatToISO: false,
        shortcutsConfig: () => []
    }
);
const myEmit = defineEmits<{
    (event: 'change', value: any | any[]): void;
    (event: 'update:modelValue', value: any | any[]): void;
}>();

const getShortcutsConfig = () => {
    const { showShortcuts, type, shortcutsConfig } = props;
    let shortcutsArr: any[] = [];
    if ((type === 'datetimerange' || type === 'daterange') && showShortcuts) {
        shortcutsConfig.forEach((item: ShortcutsConfigObj) => {
            shortcutsArr.push({
                text: item.text,
                value: (() => {
                    const end = new Date();
                    const start = new Date();
                    start.setTime(start.getTime() - 3600 * 1000 * item.value);
                    return [start, end];
                })()
            })
        })
    }
    return shortcutsArr;
}
const shortcuts = getShortcutsConfig();

const dateValue = computed({
    get: () => props.modelValue,
    set: val => {
        let value;
        if (val && props.valueFormatToISO) {
            value = Array.isArray(val) ? val.map(item => dayjs(item).tz(dayjs.tz.guess()).format()) : dayjs(val).tz(dayjs.tz.guess()).format();
        } else if (val && !props.valueFormatToISO) {
            let fmt: string;
            switch (props.type) {
                case 'date':
                case 'daterange':
                    fmt = 'YYYY-MM-DD'
                    break;
                case 'datetime':
                case 'datetimerange':
                    fmt = 'YYYY-MM-DD HH:mm:ss';
                    break;
                case 'year':
                    fmt = 'YYYY';
                    break;
                case 'month':
                    fmt = 'YYYY-MM';
                    break;
                default:
                    fmt = 'YYYY-MM-DD';
                    break;
            }
            value = Array.isArray(val) ? val.map(item => dayjs(item).format(fmt)) : dayjs(val).format(fmt);
        } else {
            value = Array.isArray(val) ? [] : null;
        }
        myEmit('update:modelValue', value);
        myEmit('change', value);
    }
})
</script>