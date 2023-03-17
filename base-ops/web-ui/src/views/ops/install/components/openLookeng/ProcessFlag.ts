export enum ProcessFlag {
  START_PROCESS = 'Start OpenLooKeng Deploy Process, This may take 5~10 minutes...',
  START_UPLOAD = 'Start Uploading All Packages...',
  START_UPLOAD_SHARDING_PROXY = 'Start Uploading ShardingProxy Package To %s, By User %s',
  START_UPLOAD_ZOOKEEPER = 'Start Uploading Zookeeper Package To %s, By User %s',
  START_UPLOAD_OLK = 'Start Uploading OpenLooKeng Package To %s, By User %s',
  START_UPLOAD_DAD = 'Start Uploading Distributed Deployment Component To %s, By User %s',
  START_UPLOAD_RULE_YAML = 'Start Uploading rule.yaml To %s',
  END_UPLOAD_RULE_YAML = 'Start Uploading rule.yaml To %s Complete',
  END_UPLOAD_SHARDING_PROXY = 'Upload ShardingProxy Package To %s Complete',
  END_UPLOAD_ZOOKEEPER = 'Upload Zookeeper Package To %s Complete',
  END_UPLOAD_OLK = 'Upload OpenLooKeng Package To %s Complete',
  END_UPLOAD_DAD = 'Upload Distributed Deployment Component Package To %s Complete',
  END_UPLOAD = 'Uploading Packages Complete',
  RUN_DAD_SERVICE = 'Running Deployment Service',
  RUN_DAD_SERVICE_COMPLETE = 'Running Deployment Service Complete'
}
