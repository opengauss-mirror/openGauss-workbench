const columnsTabColumns = [
  {
    label: 'foreignTable.columns.foreignColumnName',
    name: 'columnName',
    prop: 'columnName',
    isI18nLabel: true,
    element: 'input',
  },
  {
    label: 'foreignTable.columns.dataType',
    name: 'dataType',
    prop: 'dataType',
    isI18nLabel: true,
    element: 'select',
    options: [],
  },
  {
    label: 'foreignTable.columns.canBeNull',
    name: 'canBeNull',
    prop: 'canBeNull',
    isI18nLabel: true,
    element: 'checkbox',
  },
  {
    label: 'foreignTable.columns.defaultValue',
    name: 'defaultValue',
    prop: 'defaultValue',
    isI18nLabel: true,
    element: 'input',
  },
  {
    label: 'foreignTable.columns.isUnique',
    name: 'isUnique',
    prop: 'isUnique',
    isI18nLabel: true,
    element: 'checkbox',
  },
  {
    label: 'foreignTable.columns.precisionSize',
    name: 'precisionSize',
    prop: 'precisionSize',
    isI18nLabel: true,
    element: 'inputNumber',
    attributes: {
      min: 0,
      step: 0,
      precision: 0,
    },
  },
  {
    label: 'foreignTable.columns.range',
    name: 'range',
    prop: 'range',
    isI18nLabel: true,
    element: 'inputNumber',
    attributes: {
      min: 0,
      step: 0,
      precision: 0,
    },
  },
  {
    label: 'common.description',
    name: 'description',
    prop: 'description',
    isI18nLabel: true,
    element: 'input',
    attributes: {
      maxlength: 5000,
    },
  },
];
const getColumnsTabColumns = () => {
  return columnsTabColumns;
};
export { getColumnsTabColumns };
