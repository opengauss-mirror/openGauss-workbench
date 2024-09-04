CREATE TABLE IF NOT EXISTS his_diagnosis_threshold_info (
    id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    cluster_id TEXT,
    node_id TEXT,
    threshold_type TEXT,
    threshold CLOB,
    threshold_name TEXT,
    threshold_value TEXT,
    threshold_unit TEXT,
    threshold_detail TEXT,
    sort_no VARCHAR,
    diagnosis_type TEXT,
    is_deleted INTEGER,
    create_time DATETIME,
    update_time DATETIME
);

insert into his_diagnosis_threshold_info(
    id,threshold_type,threshold,threshold_name,threshold_value,threshold_unit,threshold_detail,sort_no,diagnosis_type)
values(1,'CPU','cpuUsageRate','{{i18n,history.threshold.cpuUsageRate.title}}','50','%',
       '{{i18n,history.threshold.cpuUsageRate.detail}}','1','history');
insert into his_diagnosis_threshold_info(
    id,threshold_type,threshold,threshold_name,threshold_value,threshold_unit,threshold_detail,sort_no,diagnosis_type)
values(2,'CPU','dbCpuUsageRate','{{i18n,history.threshold.dbCpuUsageRate.title}}','50','%',
       '{{i18n,history.threshold.dbCpuUsageRate.detail}}','2','history');
insert into his_diagnosis_threshold_info(
    id,threshold_type,threshold,threshold_name,threshold_value,threshold_unit,threshold_detail,sort_no,diagnosis_type)
values(3,'CPU','proCpuUsageRate','{{i18n,history.threshold.proCpuUsageRate.title}}','50','%',
       '{{i18n,history.threshold.proCpuUsageRate.detail}}','3','history');
insert into his_diagnosis_threshold_info(
    id,threshold_type,threshold,threshold_name,threshold_value,threshold_unit,threshold_detail,sort_no,diagnosis_type)
values(4,'CPU','activityNum','{{i18n,history.threshold.activityNum.title}}','10','pcs',
       '{{i18n,history.threshold.activityNum.detail}}','4','history');
insert into his_diagnosis_threshold_info(
    id,threshold_type,threshold,threshold_name,threshold_value,threshold_unit,threshold_detail,sort_no,diagnosis_type)
values(5,'CPU','threadPoolUsageRate','{{i18n,history.threshold.threadPoolUsageRate.title}}','30','%',
       '{{i18n,history.threshold.threadPoolUsageRate.detail}}','5','history');
insert into his_diagnosis_threshold_info(
    id,threshold_type,threshold,threshold_name,threshold_value,threshold_unit,threshold_detail,sort_no,diagnosis_type)
values(6,'CPU','connectionNum','{{i18n,history.threshold.connectionNum.title}}','10','pcs',
       '{{i18n,history.threshold.connectionNum.detail}}','6','history');
insert into his_diagnosis_threshold_info(
    id,threshold_type,threshold,threshold_name,threshold_value,threshold_unit,threshold_detail,sort_no,diagnosis_type)
values(7,'CPU','duration','{{i18n,history.threshold.duration.title}}','60','s',
       '{{i18n,history.threshold.duration.detail}}','7','history');
insert into his_diagnosis_threshold_info(
    id,threshold_type,threshold,threshold_name,threshold_value,threshold_unit,threshold_detail,sort_no,diagnosis_type)
values(8,'CPU','sqlNum','{{i18n,history.threshold.sqlNum.title}}','1','pcs',
       '{{i18n,history.threshold.sqlNum.detail}}','8','history');
insert into his_diagnosis_threshold_info(
    id,threshold_type,threshold,threshold_name,threshold_value,threshold_unit,threshold_detail,sort_no,diagnosis_type)
values(9,'CPU','waitEventNum','{{i18n,history.threshold.waitEventNum.title}}','10','pcs',
       '{{i18n,history.threshold.waitEventNum.detail}}','9','history');
insert into his_diagnosis_threshold_info(
    id,threshold_type,threshold,threshold_name,threshold_value,threshold_unit,threshold_detail,sort_no,diagnosis_type)
values(12,'CPU','swapIn','{{i18n,history.threshold.swapIn.title}}','0','page',
       '{{i18n,history.threshold.swapIn.detail}}','11','history');
insert into his_diagnosis_threshold_info(
    id,threshold_type,threshold,threshold_name,threshold_value,threshold_unit,threshold_detail,sort_no,diagnosis_type)
values(13,'CPU','swapOut','{{i18n,history.threshold.swapOut.title}}','0','page',
       '{{i18n,history.threshold.swapOut.detail}}','12','history');
insert into his_diagnosis_threshold_info(
    id,threshold_type,threshold,threshold_name,threshold_value,threshold_unit,threshold_detail,sort_no,diagnosis_type)
values(14,'EXPLAIN','randomPageCost','{{i18n,sql.threshold.randomPageCost.title}}','1.5','',
       '{{i18n,sql.threshold.randomPageCost.detail}}','2','sql');