type EditColumnType = 'ColumnsTab' | 'ConstraintTab' | 'IndexesTab';
const getColumn = (type: EditColumnType) => {
  const columnMap = {
    ColumnsTab: [
      {
        label: 'table.column.columnName',
        name: 'columnName',
        prop: 'columnName',
        isI18nLabel: true,
        element: 'input',
      },
      {
        label: 'table.column.dataType',
        name: 'dataType',
        prop: 'dataType',
        isI18nLabel: true,
        element: 'select',
        options: [],
      },
      {
        label: 'table.column.canBeNotNull',
        name: 'canBeNull',
        prop: 'canBeNull',
        isI18nLabel: true,
        element: 'checkbox',
      },
      {
        label: 'table.column.defaultValue',
        name: 'defaultValue',
        prop: 'defaultValue',
        isI18nLabel: true,
        element: 'input',
      },
      {
        label: 'table.column.isUnique',
        name: 'isUnique',
        prop: 'isUnique',
        isI18nLabel: true,
        element: 'checkbox',
      },
      {
        label: 'table.column.precisionSize',
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
        label: 'table.column.range',
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
    ],
    ConstraintTab: [
      {
        label: 'table.constraint.constrainName',
        name: 'constrainName',
        prop: 'constrainName',
        isI18nLabel: true,
        element: 'input',
      },
      {
        label: 'table.constraint.columnName',
        name: 'columnName',
        prop: 'columnName',
        isI18nLabel: true,
        element: 'select',
        attributes: {
          multiple: true,
          collapseTags: true,
        },
        options: [],
      },
      {
        label: 'table.constraint.constrainType',
        name: 'constrainType',
        prop: 'constrainType',
        isI18nLabel: true,
        element: 'cascader',
        options: [
          {
            value: 'c',
            label: 'CHECK',
            disabled: false,
            leaf: true,
          },
          {
            value: 'u',
            label: 'UNIQUE',
            disabled: false,
            leaf: true,
          },
          {
            value: 'p',
            label: 'PRIMARY KEY',
            disabled: false,
            leaf: true,
          },
          {
            value: 's',
            label: 'PARTIAL CLUSTER KEY',
            disabled: false,
            leaf: true,
          },
          {
            value: 'f',
            label: 'FOREIGN KEY',
            disabled: false,
            leaf: false,
          },
        ],
      },
      {
        label: 'table.constraint.expression',
        name: 'expression',
        prop: 'expression',
        isI18nLabel: true,
        element: 'input',
      },
      {
        label: 'table.constraint.isDeffered',
        name: 'isDeffered',
        prop: 'isDeffered',
        isI18nLabel: true,
        element: 'checkbox',
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
    ],
    IndexesTab: [
      {
        label: 'table.indexes.indexName',
        name: 'indexName',
        prop: 'indexName',
        isI18nLabel: true,
        element: 'input',
      },
      {
        label: 'table.indexes.isUnique',
        name: 'isUnique',
        prop: 'isUnique',
        isI18nLabel: true,
        element: 'checkbox',
        attributes: {},
      },
      {
        label: 'table.indexes.accessMethod',
        name: 'accessMethod',
        prop: 'accessMethod',
        isI18nLabel: true,
        element: 'select',
        attributes: {},
        options: [
          {
            value: 'btree',
            label: 'btree',
          },
          {
            value: 'cbtree',
            label: 'cbtree',
          },
          {
            value: 'cgin',
            label: 'cgin',
          },
          {
            value: 'gin',
            label: 'gin',
          },
          {
            value: 'gist',
            label: 'gist',
          },
          {
            value: 'hash',
            label: 'hash',
          },
          {
            value: 'psort',
            label: 'psort',
          },
          {
            value: 'spgist',
            label: 'spgist',
          },
          {
            value: 'ubtree',
            label: 'ubtree',
          },
        ],
      },
      {
        label: 'table.indexes.columnName',
        name: 'columnName',
        prop: 'columnName',
        isI18nLabel: true,
        element: 'select',
        attributes: {
          multiple: true,
        },
        options: [],
      },
      {
        label: 'table.indexes.expression',
        name: 'expression',
        prop: 'expression',
        isI18nLabel: true,
        element: 'input',
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
    ],
  };
  return columnMap[type];
};
export { getColumn };
export type { EditColumnType };
