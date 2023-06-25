import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import timezone from 'dayjs/plugin/timezone'
import moment from 'moment'
dayjs.extend(utc)
dayjs.extend(timezone)

export function utcTimeFormat(date: string | Date, formatter: string) {
    return dayjs.utc(date).local().format(formatter)
}

export function dateToUTCString(date: Date) {
    return dayjs(date).utc().format()
}

export function dateAddHour(date: Date, num: number) {
    return new Date(date.getTime() + num * 60 * 60 * 1000)
}

export function formatDateStartAndEnd(dateStart: any, dateEnd: any) {
    const startMoment = moment(dateStart)
    const startYear = startMoment.format('YYYY')
    const startMonth = startMoment.format('MM')
    const startDay = startMoment.format('DD')
    const endMoment = moment(dateEnd)
    const endYear = endMoment.format('YYYY')
    const endMonth = endMoment.format('MM')
    const endDay = endMoment.format('DD')
    const nowMoment = moment(new Date())
    const nowYear = nowMoment.format('YYYY')

    const yearStart = startYear === nowYear ? '' : 'startYear'
    const monthStart = startMonth
    const dayStart = startDay
    const yearEnd = endYear === startYear ? yearStart : endYear
    const monthEnd = !yearEnd && startMonth === endMonth && startDay === endDay ? '' : endMonth
    const dayEnd = !yearEnd && startMonth === endMonth && startDay === endDay ? '' : endDay

    return (
        yearStart +
        (yearStart ? '-' : '') +
        monthStart +
        (monthStart ? '-' : '') +
        dayStart +
        (dayStart ? ' ' : '') +
        startMoment.format('HH:mm:ss') +
        '~' +
        yearEnd +
        (yearEnd ? '-' : '') +
        monthEnd +
        (monthEnd ? '-' : '') +
        (dayEnd ? ' ' : '') +
        endMoment.format('HH:mm:ss')
    )
}
