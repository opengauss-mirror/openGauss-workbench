const getTagMultiTitleArr = (tag) => {
  const { connectInfoName, dbname, schema } = tag.query;
  const connectionObj = {
    key: 'connection.connection',
    value: connectInfoName,
  };
  const dbObj = {
    key: 'database.database',
    value: dbname,
  };
  const schemaObj = {
    key: 'mode.schema',
    value: schema,
  };
  let result = [];
  if (['home', 'createTerminal'].includes(tag.name)) {
    result = [
      {
        key: 'windows.terminal',
        value: decodeURIComponent(tag.showName),
      },
      connectionObj,
      dbObj,
    ];
  } else if (['editUserRole'].includes(tag.name)) {
    result = [
      {
        key: 'userRole.name',
        value: tag.query.fileName,
      },
      connectionObj,
    ];
  } else if (['editTablespace'].includes(tag.name)) {
    result = [
      {
        key: 'tablespace.name',
        value: tag.query.fileName,
      },
      connectionObj,
    ];
  } else if (['jobs'].includes(tag.name)) {
    result = [
      {
        key: 'job.name',
        value: tag.query.fileName,
      },
      connectionObj,
    ];
  } else if (['table'].includes(tag.name)) {
    result = [
      {
        key: tag.query.parttype == 'y' ? 'database.partition_table' : 'database.regular_table',
        value: tag.query.fileName,
      },
      schemaObj,
      dbObj,
      connectionObj,
    ];
  } else if (['foreignTable'].includes(tag.name)) {
    result = [
      {
        key: 'database.foreign_table',
        value: tag.query.fileName,
      },
      schemaObj,
      dbObj,
      connectionObj,
    ];
  } else if (['trigger'].includes(tag.name)) {
    result = [
      {
        key: 'database.trigger',
        value: tag.query.fileName,
      },
      schemaObj,
      dbObj,
      connectionObj,
    ];
  } else if (['debug', 'debugChild'].includes(tag.name)) {
    result = [
      {
        key: 'database.function_process',
        value: tag.query.fileName,
      },
      schemaObj,
      dbObj,
      connectionObj,
    ];
  } else if (['view'].includes(tag.name)) {
    result = [
      {
        key: 'database.view',
        value: tag.query.fileName,
      },
      schemaObj,
      dbObj,
      connectionObj,
    ];
  } else if (['synonym'].includes(tag.name)) {
    result = [
      {
        key: 'database.synonym',
        value: tag.query.fileName,
      },
      schemaObj,
      dbObj,
      connectionObj,
    ];
  } else if (['sequence'].includes(tag.name)) {
    result = [
      {
        key: 'database.sequence',
        value: tag.query.fileName,
      },
      schemaObj,
      dbObj,
      connectionObj,
    ];
  }
  result = result.filter((item) => item.value);
  return result;
};
export default getTagMultiTitleArr;
