

export function parseContent(content: string, param: any) {
    if (!content || !param) {
        return content
    }
    const paramOpen = "${";
    const paramClose = "}";
    let start = content.indexOf(paramOpen);
    if (start === -1) {
        return content;
    }
    let result = ''
    let offset = 0
    while (start > -1) {
        result += content.slice(offset, start)
        offset = start + paramOpen.length
        let end = content.indexOf(paramClose, offset)
        if (end === -1) {
            return content
        }
        let paramName = content.slice(offset, end)
        let paramVal = param[paramName] || ''
        result += paramVal
        offset = end + paramClose.length
        start = content.indexOf(paramOpen, offset)
    }
    result += content.slice(offset, content.length)

    return result
}