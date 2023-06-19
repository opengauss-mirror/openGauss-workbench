import { ElMessage, ElMessageBox } from 'element-plus';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import EventBus, { EventTypeName } from '@/utils/event-bus';
import { setTableReindex, setTableTruncate, setTableVacuum, dropTable } from '@/api/table';
import type { NodeData } from '../Sidebar/types';

export const useSidebarContext = (
  options = {
    currentContextNodeData: {} as NodeData,
    hideTreeContext: undefined as undefined | (() => void),
  },
) => {
  const { t } = useI18n();
  const router = useRouter();
  const getTableCommonParams = () => {
    const { uuid, schemaName: schema, oid, name } = options.currentContextNodeData;
    return {
      uuid,
      schema,
      oid,
      tableName: name,
    };
  };

  const handleReindex = () => {
    if (options.hideTreeContext) options.hideTreeContext();
    const params = getTableCommonParams();
    ElMessageBox.confirm(
      t('message.reindexTable', { name: `${params.schema}.${params.tableName}` }),
    ).then(async () => {
      await setTableReindex(params);
      ElMessage.success(t('message.success'));
    });
  };
  const handleTruncate = () => {
    if (options.hideTreeContext) options.hideTreeContext();
    const params = getTableCommonParams();
    ElMessageBox.confirm(
      t('message.truncateTable', { name: `${params.schema}.${params.tableName}` }),
    ).then(async () => {
      await setTableTruncate(params);
      ElMessage.success(t('message.success'));
    });
  };

  const handleVacuum = () => {
    if (options.hideTreeContext) options.hideTreeContext();
    const params = getTableCommonParams();
    ElMessageBox.confirm(
      t('message.vacuumTable', { name: `${params.schema}.${params.tableName}` }),
    ).then(async () => {
      await setTableVacuum(params);
      ElMessage.success(t('message.success'));
    });
  };

  const handleDropTable = () => {
    if (options.hideTreeContext) options.hideTreeContext();
    const params = getTableCommonParams();
    ElMessageBox.confirm(
      t('message.dropTable', { name: `${params.schema}.${params.tableName}` }),
    ).then(async () => {
      await dropTable(params);
      ElMessage.success(t('message.success'));
      EventBus.notify(EventTypeName.REFRESH_ASIDER, 'mode', {
        rootId: options.currentContextNodeData.rootId,
        databaseId: options.currentContextNodeData.databaseId,
        schemaId: options.currentContextNodeData.schemaId,
      });
    });
  };

  const handleRelatedSequence = () => {
    if (options.hideTreeContext) options.hideTreeContext();
    const params = getTableCommonParams();
    router.push({
      path: `/tableRelatedSequence/${options.currentContextNodeData.id}`,
      query: {
        title: `${params.schema}.${params.tableName}.RelatedSequence-options.${options.currentContextNodeData.databaseName}@${options.currentContextNodeData.connectInfo.name}`,
        fileName: `${params.tableName}.RelatedSequence`,
        ...params,
        rootId: options.currentContextNodeData.rootId,
        dbname: options.currentContextNodeData.databaseName,
      },
    });
  };

  return {
    handleReindex,
    handleTruncate,
    handleVacuum,
    handleDropTable,
    handleRelatedSequence,
  };
};
