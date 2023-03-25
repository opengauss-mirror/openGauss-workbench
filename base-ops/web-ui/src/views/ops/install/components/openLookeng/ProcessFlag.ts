export enum ProcessFlag {
  END_UPLOAD = 'Uploading Packages Successfully Completed',
  END_INSTALL_SHARDING_PROXY = 'Install ShardingProxy On .+ Complete',
  END_INSTALL_ZOOKEEPER = 'Install Zookeeper On .+ Complete',
  END_INSTALL_OLK = 'Install OpenLooKeng On .+ Complete',
  END_INSTALL = 'Install All Packages Complete',
  END_SERVICE = 'Start All Service Complete',
  START_DESTROY = "Start Destroy OpenLooKeng Service...",
  END_DESTROY_IN_ERROR = "Destroy OpenLooKeng Service Process Abort With Error:",
  END_DESTROY = "OpenLooKeng Deploy Process Successfully Completed",
  START_SERVICE = "Start OpenLooKeng Service...",
  END_START_SERVICE_IN_ERROR = "Start OpenLooKeng Service Process Abort With Error:",
  END_START_SERVICE = "OpenLooKeng Service Successfully Started",
  START_STOP_SERVICE = "Stopping OpenLooKeng Service...",
  END_STOP_SERVICE_IN_ERROR = "Stop OpenLooKeng Service Process Abort With Error:",
  END_STOP_SERVICE = "OpenLooKeng Service Successfully Stopped"
}
