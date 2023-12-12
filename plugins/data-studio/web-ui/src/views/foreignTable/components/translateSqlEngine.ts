import { SetFilterTableDataRow } from '../types';

const sqlSyntaxMap = {
  '=': '=',
  '!=': '<>',
  '<': '<',
  '<=': '<=',
  '>': '>',
  '>=': '>=',
  contains: 'LIKE',
  containsCaseInsensitive: 'ILIKE',
  doesNotContain: 'NOT LIKE',
  doesNotContainCaseInsensitive: 'NOT ILIKE',
  beginsWith: 'LIKE',
  doesNotBeginWith: 'NOT LIKE',
  endsWith: 'LIKE',
  doesNotEndWith: 'NOT LIKE',
  isNull: 'IS NULL',
  isNotNull: 'IS NOT NULL',
  isEmpty: `=''`,
  isNotEmpty: `<>''`,
  isBetween: 'BETWEEN',
  isNotBetween: 'NOT BETWEEN',
  isInList: 'IN',
  isNotInList: 'NOT IN',
};

function fn(list: SetFilterTableDataRow[]) {
  const availableList = list
    .filter((item) => item.check)
    .filter((item) => !(item.type == 'normal' && !item.key));
  return availableList.map((item) => {
    const logic = item.logic ? ` ${item.logic}` : '';
    let rowStr = '';
    if (item.type == 'leftBracket') {
      rowStr = '(';
    }
    if (item.type == 'rightBracket') {
      rowStr = ')';
    }
    if (item.type == 'normal') {
      if (['=', '!=', '<', '<=', '>', '>='].includes(item.connector)) {
        rowStr = `${item.key} ${sqlSyntaxMap[item.connector]} ${item.value}`;
      }
      if (
        [
          'contains',
          'containsCaseInsensitive',
          'doesNotContain',
          'doesNotContainCaseInsensitive',
        ].includes(item.connector)
      ) {
        rowStr = `${item.key} ${sqlSyntaxMap[item.connector]} '%${item.value}%'`;
      }
      if (['beginsWith', 'doesNotBeginWith'].includes(item.connector)) {
        rowStr = `${item.key} ${sqlSyntaxMap[item.connector]} '${item.value}%'`;
      }
      if (['endsWith', 'doesNotEndWith'].includes(item.connector)) {
        rowStr = `${item.key} ${sqlSyntaxMap[item.connector]} '%${item.value}'`;
      }
      if (['isNull', 'isNotNull', 'isEmpty', 'isNotEmpty'].includes(item.connector)) {
        rowStr = `${item.key} ${sqlSyntaxMap[item.connector]}`;
      }
      if (['isBetween', 'isNotBetween'].includes(item.connector)) {
        rowStr = `${item.key} ${sqlSyntaxMap[item.connector]} ${item.value} AND ${item.value2}`;
      }
      if (['isInList', 'isNotInList'].includes(item.connector)) {
        rowStr = `${item.key} ${sqlSyntaxMap[item.connector]} (${item.value})`;
      }
    }
    return rowStr + logic;
  });
}

export default fn;
