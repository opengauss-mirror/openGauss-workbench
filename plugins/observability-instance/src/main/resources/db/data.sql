
insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(1,'DB','recovery_max_workers','','','','','1','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(2,'DB','recovery_parse_workers','','','','','1','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(3,'DB','recovery_redo_workers','','','','','1','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(4,'DB','recovery_min_apply_delay','','','','','1','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(5,'DB','wal_level','','','','','hot_standby','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(6,'DB','synchronous_commit','','','','','on','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(7,'DB','wal_buffers','','','','12800-128000','2048','','','actualValue>=12800 && actualValue<=128000');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(8,'DB','wal_writer_delay','','','','','200','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(9,'DB','commit_delay','','','','','0','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(10,'DB','commit_siblings','','','','','5','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(11,'DB','wal_flush_timeout','','','','','2','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(12,'DB','recovery_time_target','','','','','0','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(13,'DB','wal_flush_delay','','','','','1','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(14,'OS','net.ipv4.tcp_max_tw_buckets','','','','10000','180000','','','actualValue<=10000');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(15,'OS','net.ipv4.tcp_tw_reuse','','','','1','0','','','actualValue==1');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(16,'OS','net.ipv4.tcp_tw_recycle','','','','1','0','','','actualValue==1');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(17,'DB','max_process_memory','','','','2*1024*1024～INT_MAX','12582912','','','actualValue>=2*1024*1024');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(18,'DB','work_mem','','','','','','','','actualValue>=64 && actualValue<=2147483647');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(19,'DB','pagewriter_sleep','','','','0～3600000','2000','','','actualValue>0 && actualValue<3600000');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(20,'OS','net.ipv4.ip_local_port_range','','','','26000-65535','32768-61000','','','actualValue=="26000 65535"');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(21,'OS','net.ipv4.tcp_keepalive_time','','','','30','7200','','','actualValue==30');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(22,'OS','net.ipv4.tcp_keepalive_probes','','','','9','9','','','actualValue==9');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(23,'OS','net.ipv4.tcp_keepalive_intvl','','','','30','75','','','actualValue==30');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(24,'OS','net.ipv4.tcp_retries1','','','','5','3','','','actualValue==5');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(25,'OS','net.ipv4.tcp_syn_retries','','','','5','5','','','actualValue==5');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(26,'OS','net.ipv4.tcp_synack_retries','','','','5','5','','','actualValue==5');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(27,'OS','net.ipv4.tcp_retries2','','','','12','15','','','actualValue==12');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(28,'OS','vm.overcommit_memory','','','','0','0','','','actualValue==0');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(29,'OS','net.ipv4.tcp_rmem','','','','8192 250000 16777216','4096 87380 174760（4k）','','','actualValue=="8192 250000 16777216"');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(30,'OS','net.ipv4.tcp_wmem','','','','8192 250000 16777216','4096 16384 131072（4k）','','','actualValue=="8192 250000 16777216"');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(31,'OS','net.core.wmem_max','','','','21299200','129024','','','actualValue==21299200');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(32,'OS','net.core.rmem_max','','','','21299200','129024','','','actualValue==21299200');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(33,'OS','net.core.wmem_default','','','','21299200','129024','','','actualValue==21299200');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(34,'OS','net.core.rmem_default','','','','21299200','129024','','','actualValue==21299200');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(35,'OS','kernel.sem','','','','250 6400000 1000 25600','250 32000 32 128','','','actualValue=="250 6400000 1000 25600"');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(36,'OS','vm.min_free_kbytes','','','','5%','724','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(37,'OS','net.core.somaxconn','','','','65535','128','','','actualValue==65535');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(38,'OS','net.ipv4.tcp_syncookies','','','','1','0','','','actualValue==1');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(39,'OS','net.core.netdev_max_backlog','','','','65535','1000','','','actualValue==65535');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(40,'OS','net.ipv4.tcp_max_syn_backlog','','','','65535','1024','','','actualValue==65535');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(41,'OS','net.ipv4.tcp_fin_timeout','','','','60','60','','','actualValue==60');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(42,'OS','kernel.shmall','','','','1152921504606840000','2097152','','','actualValue==1152921504606840000');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(43,'OS','kernel.shmmax','','','','18446744073709500000','33554432','','','actualValue==18446744073709500000');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(44,'OS','net.ipv4.tcp_sack','','','','1','1','','','actualValue==1');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(45,'OS','net.ipv4.tcp_timestamps','','','','1','1','','','actualValue==1');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(46,'OS','vm.extfrag_threshold','','','','500','500','','','actualValue==500');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(47,'OS','vm.overcommit_ratio','','','','90','50','%','','actualValue==90');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(48,'OS','MTU','network','','','8192','1500','','','actualValue==8192');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(49,'DB','bgwriter_delay','','','','','2','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(50,'DB','pagewriter_thread_num','','','','','','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(51,'DB','max_io_capacity','','','','','512000','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(52,'DB','log_min_duration_statement','','','','','1800000','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(53,'DB','log_duration','','','','','on','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(54,'DB','track_stmt_stat_level','','','','','OFF,L0','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(55,'DB','track_stmt_retention_time','','','','','3600,604800','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(56,'DB','enable_thread_pool','','','','','off','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(57,'DB','thread_pool_attr','','','','','16, 2, (nobind)','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(58,'DB','log_statement','','','','','none','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(59,'DB','log_error_verbosity','','','','','default','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(60,'DB','log_min_messages','','','','','warning','','','');

insert into param_info(id,param_type,param_name,parameter_category,value_range,param_detail,suggest_value,default_value,unit,suggest_explain,diagnosis_rule)
values(61,'DB','log_min_error_statement','','','','','error','','','');

