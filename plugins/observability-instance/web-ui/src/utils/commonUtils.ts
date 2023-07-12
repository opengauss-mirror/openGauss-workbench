export const parseSql = (sql: string) => {
  if (!sql) {
    return sql
  }
  const regex = new RegExp(/[';]|(\$\d+)/g)
  let matchList = sql.matchAll(regex)
  if (!matchList) {
    return sql;
  }
  let endIndex = -1
  let cur = 0;
  let apostrophe = 0
  let subSqlArr = []
  let sqlParams = []
  let paramsMap = {}
  let str = ""
  for (let match of matchList) {
    let word = match[0]
    let index = match.index as number
    str += sql.substring(cur, index)
    cur = index + word.length
    if (word === "'") {
      apostrophe = apostrophe === 1 ? apostrophe - 1 : apostrophe + 1;
      str += word
    } else if (word === ";") {
      str += word
      if (apostrophe === 0) {
        subSqlArr.push(str)
        endIndex = cur
        break
      }
    } else {
      if (apostrophe === 0) {
        subSqlArr.push(str)
        sqlParams.push(word)
        str = ""
        continue
      }
      str += word
    }
  }
  if (endIndex === -1) {
    return sql
  }
  let paramsStr = sql.substring(endIndex, sql.length);
  if (!paramsStr || !(paramsStr.trim())) {
    return sql;
  }
  if (!paramsStr.trim().startsWith("parameters:")) {
    return sql;
  }
  paramsStr = paramsStr.trim().substring("parameters:".length);

  let paramStrArr = paramsStr.split(",");
  for (let paramStr of paramStrArr) {
    if (paramStr) {
      let paramArr = paramStr.split("=") || [];
      if (paramArr.length !== 2) {
        continue
      }
      paramsMap[paramArr[0].trim()] = paramArr[1].trim()
    }
  }
  let i = 0
  let j = 0
  let result = ""
  while (i < subSqlArr.length) {
    result += subSqlArr[i]
    if (i === (subSqlArr.length - 1)) {
      break;
    }
    result += paramsMap[sqlParams[j]]
    i++;
    j++;
  }
  return result;
}