import { i18n } from '@/i18n/index';
const t = i18n.global.t;
type EditColumnType = 'ColumnTab' | 'ConstraintTab' | 'IndexesTab';
const getColumn = (type: EditColumnType) => {
  const columnMap = {
    ColumnTab: [
      {
        label: 'table.column.columnName',
        name: 'columnName',
        isI18nLabel: true,
        type: 'input',
      },
      {
        label: 'table.column.dataType',
        name: 'dataType',
        isI18nLabel: true,
        type: 'select',
        options: [],
      },
      {
        label: 'table.column.canBeNull',
        name: 'canBeNull',
        isI18nLabel: true,
        type: 'checkbox',
      },
      {
        label: 'table.column.defaultValue',
        name: 'defaultValue',
        isI18nLabel: true,
        type: 'input',
      },
      {
        label: 'table.column.isUnique',
        name: 'isUnique',
        isI18nLabel: true,
        type: 'checkbox',
      },
      {
        label: 'table.column.precisionSize',
        name: 'precisionSize',
        isI18nLabel: true,
        type: 'inputNumber',
        attributes: {
          min: 0,
          step: 0,
          precision: 0,
        },
      },
      {
        label: 'table.column.range',
        name: 'range',
        isI18nLabel: true,
        type: 'inputNumber',
        attributes: {
          min: 0,
          step: 0,
          precision: 0,
        },
      },
      {
        label: 'table.description',
        name: 'description',
        isI18nLabel: true,
        type: 'input',
        attributes: {
          maxlength: 5000,
        },
      },
    ],
    ConstraintTab: [
      {
        label: 'table.constraint.constrainName',
        name: 'constrainName',
        isI18nLabel: true,
        type: 'input',
      },
      {
        label: 'table.constraint.columnName',
        name: 'columnName',
        isI18nLabel: true,
        type: 'select',
        attributes: {
          multiple: true,
          collapseTags: true,
        },
        options: [],
      },
      {
        label: 'table.constraint.constrainType',
        name: 'constrainType',
        isI18nLabel: true,
        type: 'cascader',
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
        isI18nLabel: true,
        type: 'input',
      },
      {
        label: 'table.constraint.isDeffered',
        name: 'isDeffered',
        isI18nLabel: true,
        type: 'checkbox',
      },
      {
        label: 'table.description',
        name: 'description',
        isI18nLabel: true,
        type: 'input',
        attributes: {
          maxlength: 5000,
        },
      },
    ],
    IndexesTab: [
      {
        label: 'table.indexes.indexName',
        name: 'indexName',
        isI18nLabel: true,
        type: 'input',
      },
      {
        label: 'table.indexes.isUnique',
        name: 'isUnique',
        isI18nLabel: true,
        type: 'checkbox',
        attributes: {},
      },
      {
        label: 'table.indexes.accessMethod',
        name: 'accessMethod',
        isI18nLabel: true,
        type: 'select',
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
        isI18nLabel: true,
        type: 'select',
        attributes: {
          multiple: true,
        },
        options: [],
      },
      {
        label: 'table.indexes.expression',
        name: 'expression',
        isI18nLabel: true,
        type: 'input',
      },
      {
        label: 'table.description',
        name: 'description',
        isI18nLabel: true,
        type: 'input',
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
