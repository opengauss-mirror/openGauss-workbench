#!/bin/bash
#处理参数，规范化参数
ARGS=`getopt -o h --long eshost:,nodeid:,clusterid:,opengausslog:,opengaussslowlog:,opengausserrorlog:,gsCtlLogPath:,gsGucLogPath:,gsOmLogPath:,gsInstallLogPath:,gsLocalLogPath:,cmLogPath:, -- "$@"`
if [ $? != 0 ];then
        echo "Terminating..."
        exit 1
fi
#重新排列参数顺序
eval set -- "${ARGS}"
#通过shift和while循环处理参数
while :
do
    case $1 in
        --eshost)
            eshost=$2
            shift
            ;;
        --clusterid)
            clusterid=$2
            shift
            ;;
        --nodeid)
            nodeid=$2
            shift
            ;;
        --opengausslog)
            opengausslog=$2
            shift
            ;;
        --opengaussslowlog)
            opengaussslowlog=$2
            shift
            ;;
        --opengausserrorlog)
            opengausserrorlog=$2
            shift
            ;;
        --gsCtlLogPath)
            sed -i 's#/var/log/omm/omm/bin/gs_ctl#'$2'#g' module/opengauss/gsctl/manifest.yml
            shift
            ;;
        --gsGucLogPath)
            sed -i 's#/var/log/omm/omm/bin/gs_guc#'$2'#g' module/opengauss/gsguc/manifest.yml
            shift
            ;;
        --gsOmLogPath)
            sed -i 's#/var/log/omm/omm/om#'$2'#g' module/opengauss/gsom/manifest.yml
		shift
            ;;
        --gsInstallLogPath)
		sed -i 's#/var/log/omm/omm/om#'$2'#g' module/opengauss/gsinstall/manifest.yml
            shift
            ;;
        --gsLocalLogPath)
            sed -i 's#/var/log/omm/omm/om#'$2'#g' module/opengauss/gslocal/manifest.yml
            shift
            ;;
        --cmLogPath)
            sed -i 's#/opt/openGauss/log/omm/omm#'$2'#g' module/opengauss/**/manifest.yml
            shift
            ;;
	--)
            shift
            break
            ;;
    esac
shift
done

echo -------filebeat.yml-----
sed -i 's/127.0.0.1:9200/'${eshost}'/g' filebeat.yml
sed -i 's/ogbrench2/'${nodeid}'/g' filebeat.yml
sed -i 's/ogbrench2/'${nodeid}'/g' module/opengauss/log/ingest/pipeline.yml
echo -------manifest.yml path-----
sed -i 's#/data/opengauss/install/data/datanode1/pg_log#'${opengausslog}'#g' module/opengauss/log/manifest.yml
sed -i 's#/data/opengauss/install/data/datanode1/pg_log#'${opengaussslowlog}'#g' module/opengauss/slowlog/manifest.yml
sed -i 's#/data/opengauss/install/data/datanode1/pg_log#'${opengausserrorlog}'#g' module/opengauss/errorlog/manifest.yml
sed -i 's#/data/opengauss/install/data/datanode1/pg_log#'${opengausslog}'#g' module/opengauss/swithover/manifest.yml
sed -i 's#/data/opengauss/install/data/datanode1/pg_log#'${opengausslog}'#g' module/opengauss/failover/manifest.yml


sed -i 's#/var/log/omm/omm/om#'${opengaussgsinstalllog}'#g' module/opengauss/gsinstall/manifest.yml

echo -------manifest.yml node-----
sed -i 's/pipeline.yml/pipeline-'${nodeid}'.yml/g' module/opengauss/**/manifest.yml
sed -i 's/pipeline-log.yml/pipeline-log-'${nodeid}'.yml/g' module/opengauss/**/manifest.yml
sed -i 's/pipeline-csv.yml/pipeline-csv-'${nodeid}'.yml/g' module/opengauss/**/manifest.yml


echo -------manifest.yml node-----
sed -i 's/pipeline.yml/pipeline-'${nodeid}'.yml/g' module/system/errorlog/manifest.yml
sed -i 's/pipeline.yml/pipeline-'${nodeid}'.yml/g' module/system/syslog/manifest.yml
echo -------pipeline.yml----------
sed -i 's/pipeline-csv/pipeline-csv-'${nodeid}'/g' module/opengauss/**/ingest/pipeline.yml
sed -i 's/pipeline-log/pipeline-log-'${nodeid}'/g' module/opengauss/**/ingest/pipeline.yml

sed -i 's/ogbrench2/'${nodeid}'/g' module/opengauss/**/ingest/pipeline.yml
sed -i 's/gba-cluster/'${clusterid}'/g' module/opengauss/**/ingest/pipeline.yml

sed -i 's/ogbrench2/'${nodeid}'/g' module/system/**/ingest/pipeline.yml
sed -i 's/gba-cluster/'${clusterid}'/g' module/system/**/ingest/pipeline.yml


echo -------pipeline-nodeid-------
mv module/opengauss/errorlog/ingest/pipeline.yml module/opengauss/errorlog/ingest/pipeline-${nodeid}.yml
mv module/opengauss/errorlog/ingest/pipeline-csv.yml module/opengauss/errorlog/ingest/pipeline-csv-${nodeid}.yml
mv module/opengauss/errorlog/ingest/pipeline-errorlog.yml module/opengauss/errorlog/ingest/pipeline-errorlog-${nodeid}.yml
mv module/opengauss/log/ingest/pipeline.yml module/opengauss/log/ingest/pipeline-${nodeid}.yml
mv module/opengauss/log/ingest/pipeline-csv.yml module/opengauss/log/ingest/pipeline-csv-${nodeid}.yml
mv module/opengauss/log/ingest/pipeline-log.yml module/opengauss/log/ingest/pipeline-log-${nodeid}.yml
mv module/opengauss/slowlog/ingest/pipeline.yml module/opengauss/slowlog/ingest/pipeline-${nodeid}.yml
mv module/opengauss/slowlog/ingest/pipeline-csv.yml module/opengauss/slowlog/ingest/pipeline-csv-${nodeid}.yml
mv module/opengauss/slowlog/ingest/pipeline-slowlog.yml module/opengauss/slowlog/ingest/pipeline-slowlog-${nodeid}.yml
mv module/system/errorlog/ingest/pipeline.yml module/system/errorlog/ingest/pipeline-${nodeid}.yml
mv module/system/syslog/ingest/pipeline.yml module/system/syslog/ingest/pipeline-${nodeid}.yml
mv module/opengauss/gsctl/ingest/pipeline.yml module/opengauss/gsctl/ingest/pipeline-${nodeid}.yml
mv module/opengauss/gsguc/ingest/pipeline.yml module/opengauss/gsguc/ingest/pipeline-${nodeid}.yml
mv module/opengauss/gsom/ingest/pipeline.yml module/opengauss/gsom/ingest/pipeline-${nodeid}.yml
mv module/opengauss/gsinstall/ingest/pipeline.yml module/opengauss/gsinstall/ingest/pipeline-${nodeid}.yml
mv module/opengauss/gslocal/ingest/pipeline.yml module/opengauss/gslocal/ingest/pipeline-${nodeid}.yml

mv module/opengauss/cmagent/ingest/pipeline.yml module/opengauss/cmagent/ingest/pipeline-${nodeid}.yml
mv module/opengauss/cmagent/ingest/pipeline-csv.yml module/opengauss/cmagent/ingest/pipeline-csv-${nodeid}.yml
mv module/opengauss/cmagent/ingest/pipeline-log.yml module/opengauss/cmagent/ingest/pipeline-log-${nodeid}.yml

mv module/opengauss/cmserver/ingest/pipeline.yml module/opengauss/cmserver/ingest/pipeline-${nodeid}.yml
mv module/opengauss/cmserver/ingest/pipeline-csv.yml module/opengauss/cmserver/ingest/pipeline-csv-${nodeid}.yml
mv module/opengauss/cmserver/ingest/pipeline-log.yml module/opengauss/cmserver/ingest/pipeline-log-${nodeid}.yml

mv module/opengauss/dcc/ingest/pipeline.yml module/opengauss/dcc/ingest/pipeline-${nodeid}.yml
mv module/opengauss/dcc/ingest/pipeline-csv.yml module/opengauss/dcc/ingest/pipeline-csv-${nodeid}.yml
mv module/opengauss/dcc/ingest/pipeline-log.yml module/opengauss/dcc/ingest/pipeline-log-${nodeid}.yml

mv module/opengauss/keyevent/ingest/pipeline.yml module/opengauss/keyevent/ingest/pipeline-${nodeid}.yml
mv module/opengauss/keyevent/ingest/pipeline-csv.yml module/opengauss/keyevent/ingest/pipeline-csv-${nodeid}.yml
mv module/opengauss/keyevent/ingest/pipeline-log.yml module/opengauss/keyevent/ingest/pipeline-log-${nodeid}.yml

mv module/opengauss/systemcall/ingest/pipeline.yml module/opengauss/systemcall/ingest/pipeline-${nodeid}.yml
mv module/opengauss/systemcall/ingest/pipeline-csv.yml module/opengauss/systemcall/ingest/pipeline-csv-${nodeid}.yml
mv module/opengauss/systemcall/ingest/pipeline-log.yml module/opengauss/systemcall/ingest/pipeline-log-${nodeid}.yml

mv module/opengauss/swithover/ingest/pipeline.yml module/opengauss/swithover/ingest/pipeline-${nodeid}.yml
mv module/opengauss/swithover/ingest/pipeline-csv.yml module/opengauss/swithover/ingest/pipeline-csv-${nodeid}.yml
mv module/opengauss/swithover/ingest/pipeline-log.yml module/opengauss/swithover/ingest/pipeline-log-${nodeid}.yml

mv module/opengauss/failover/ingest/pipeline.yml module/opengauss/failover/ingest/pipeline-${nodeid}.yml
mv module/opengauss/failover/ingest/pipeline-csv.yml module/opengauss/failover/ingest/pipeline-csv-${nodeid}.yml
mv module/opengauss/failover/ingest/pipeline-log.yml module/opengauss/failover/ingest/pipeline-log-${nodeid}.yml

echo -------chmod-------------------
chmod -R go-w modules.d
chmod -R go-w module