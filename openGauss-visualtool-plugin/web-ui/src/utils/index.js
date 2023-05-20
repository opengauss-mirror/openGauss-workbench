export function mergeObjectArray (arr1, arr2, idProp) {
  const arr1Temp = JSON.parse(JSON.stringify(arr1))
  const arr2Temp = JSON.parse(JSON.stringify(arr2))
  const mergedArr = arr1Temp.slice()

  arr2Temp.forEach(obj => {
    const index = mergedArr.findIndex(item => item[idProp] === obj[idProp])
    if (index !== -1) {
      mergedArr[index] = obj
    } else {
      mergedArr.push(obj)
    }
  })

  return mergedArr
}
