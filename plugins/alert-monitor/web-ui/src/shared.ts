export const uuid = () => Math.random().toString(36).slice(2);

export const toFixed = (n: number | string, r: number = 2) => (Number.parseInt(`${Number.parseFloat(`${n}`) * (10 ** r)}`) / (10 ** r)).toFixed(r);

export const downloadCsvFile = (filename: string, title: string, data: Record<any, string | number>[]) => {
    let str = `${title}\n`;
    data.forEach(d => {
        for (const k in d) {
            str += `${d[k]}\t,`
        }
        str += `\n`
    })
    const link = document.createElement("a")
    link.href = 'data:text/csv;charset=utf-8,\ufeff' + encodeURIComponent(str)
    link.download = filename
    link.click()
    link.remove()
}
