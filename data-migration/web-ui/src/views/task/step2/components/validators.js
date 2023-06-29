import i18n from '@/locale/index'
import { PORTAL_PARAM_TYPE } from '@/utils/constants'

export function getValidator(record) {
    let validator = []
    if (record.paramType === PORTAL_PARAM_TYPE.NUMBER)
        validator.push({
            required: true,
            message: i18n.global.t('components.ParamsConfig.5q0aazspqfs1') + record.paramKey,
        })
    if (record.paramType === PORTAL_PARAM_TYPE.REGEX) {
        validator.push({ trigger: 'blur', validator: regexValidator })
    }
    if ((record.paramType === PORTAL_PARAM_TYPE.STRING || record.paramType === PORTAL_PARAM_TYPE.VAR) && record.paramRules) {
        const rulesArr = JSON.parse(record.paramRules)
        if (Array.isArray(rulesArr)) {
            const isDoubleArray = rulesArr.some(item => Array.isArray(item))
            if (isDoubleArray) {
                validator.push({
                    trigger: 'blur',
                    validator: rangeValidator(rulesArr),
                })
            } else {
                validator.push({
                    trigger: 'blur',
                    validator: stringValidator(record.paramRules),
                })
            }
        }
    }
    return validator
}

const regexValidator = (value, cb) => {
    if (
        !value ||
        value?.length <= 0 ||
        /^\/((\\\/|[^\/])+)\/([gim]{0,3})$/.test(value)
    ) {
        cb()
    } else {
        cb(i18n.global.t('components.ParamsConfig.5q0aazspqfs2'))
    }
}

const stringValidator = (paramRules) => {
    const ruleArr = JSON.parse(paramRules)
    return (value, cb) => {
        if (!value || value.length < ruleArr[0]) {
            cb(i18n.global.t('components.ParamsConfig.5q0aazspqfs3', { count: ruleArr[0] }))
        } else if (value.length > ruleArr[1]) {
            cb(i18n.global.t('components.ParamsConfig.5q0aazspqfs4', { count: ruleArr[1] }))
        } else {
            cb()
        }
    }
}

const rangeValidator = (ruleArr) => {
    return (value, cb) => {
        if (!value) {
            cb()
            return
        }
        const valueArr = value.split(',')
        if (
            Array.isArray(valueArr) &&
            valueArr.length === 2 &&
            !Number.isNaN(Number(valueArr[0])) &&
            !Number.isNaN(Number(valueArr[1]))
        ) {
            const offset = Number(valueArr[0])
            const count = Number(valueArr[1])
            if (offset < ruleArr[0][0] || offset > ruleArr[0][1]) {
                cb(
                    i18n.global.t('components.ParamsConfig.5q0aazspqfs6', {
                        min: ruleArr[0][0],
                        max: ruleArr[0][1],
                    })
                )
                return
            }

            if (count < ruleArr[1][0] || count > ruleArr[1][1]) {
                cb(
                    i18n.global.t('components.ParamsConfig.5q0aazspqfs7', {
                        min: ruleArr[1][0],
                        max: ruleArr[1][1],
                    })
                )
                return
            }

            cb()
        } else {
            cb(i18n.global.t('components.ParamsConfig.5q0aazspqfs5'))
        }
    }
}