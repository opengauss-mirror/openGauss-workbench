export function getDefaultEcharOption() {
    const theme = localStorage.getItem('theme')
    let themeColor = {
        nodes: {
            font: {
                color: 'black',
            },
            color: {
                border: '#424242',
                background: '#424242',
                hover: {
                    border: '#fff',
                    background: '#424242',
                },
                highlight: {
                    border: '#fff',
                    background: '#424242',
                },
            },
        },
        edges: {
            color: {
                color: '#7d7d7d',
                highlight: '#424242',
                hover: 'green',
                inherit: 'from',
                opacity: 0.5,
            },
        },
    }
    if (theme === 'dark') {
        themeColor = {
            nodes: {
                font: {
                    color: 'white',
                },
                color: {
                    border: '#424242',
                    background: '#424242',
                    hover: {
                        border: '#fff',
                        background: '#424242',
                    },
                    highlight: {
                        border: '#fff',
                        background: '#424242',
                    },
                },
            },
            edges: {
                color: {
                    color: '#7d7d7d',
                    highlight: '#fff',
                    hover: 'green',
                    inherit: 'from',
                    opacity: 0.5,
                },
            },
        }
    }
    return themeColor
}

const isArr = (origin: any): boolean => {
    let str = '[object Array]'
    return Object.prototype.toString.call(origin) === str
}

const deepClone = <T>(origin: T, target?: Record<string, any> | T): T => {
    let tar = target || {}

    for (const key in origin) {
        if (Object.prototype.hasOwnProperty.call(origin, key)) {
            if (typeof origin[key] === 'object' && origin[key] !== null) {
                tar[key] = isArr(origin[key]) ? [] : {}
                deepClone(origin[key], tar[key])
            } else {
                tar[key] = origin[key]
            }
        }
    }

    return tar as T
}