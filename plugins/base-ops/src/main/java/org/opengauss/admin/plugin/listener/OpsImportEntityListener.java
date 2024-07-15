package org.opengauss.admin.plugin.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import org.opengauss.admin.plugin.domain.entity.ops.OpsImportEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpsImportEntityListener extends AnalysisEventListener<OpsImportEntity> {
    private static final Logger logger = LoggerFactory.getLogger(OpsImportEntityListener.class);

    @Override
    public void invoke(OpsImportEntity user, AnalysisContext analysisContext) {
        logger.info("Process data rows: {}", user.toString());
        int currentRowNum = analysisContext.readRowHolder().getRowIndex() + 1;
        logger.info("Current processing line number: {}", currentRowNum);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        logger.info("All data processing completed");
        int totalRows = analysisContext.getTotalCount();
        logger.info("Total number of processed lines: {}", totalRows);
    }
}