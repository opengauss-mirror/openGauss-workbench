package com.nctigba.observability.sql.config;

import com.nctigba.observability.sql.constants.CommonConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.sqlite.JDBC;
import org.sqlite.SQLiteDataSource;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

@Component
@Slf4j
public class ParamInfoInitConfig {
    @Value("${sqlitePath:data/paramDiagnosisInfo.db}")
    private String path;
    @Value("${sqliteinit:false}")
    private boolean refresh;
    boolean needInit = false;

    private String[] initSqls = {
            "CREATE TABLE param_info (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, paramType TEXT,\n" +
                    " paramName TEXT, paramDetail TEXT, suggestValue TEXT, defaultValue TEXT,\n" +
                    " unit TEXT, suggestExplain TEXT, diagnosisRule TEXT);",
            "CREATE TABLE param_value_info (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, sid INTEGER,\n" +
                    " instance TEXT, actualValue TEXT);",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.tcp_max_tw_buckets\",\"表示同时保持TIME_WAIT状态的TCP/IP连接最大数量。如果超过所配置的取值，TIME_WAIT将立刻被释放并打印警告信息\",\"10000\",\"180000\",\"数目\",\"系统在同时所处理的最大 timewait sockets 数目。如果超过此数的话﹐time-wait socket 会被立即砍除并且显示警告信息。之所以要设定这个限制﹐纯粹为了抵御那些简单的 DoS 攻击﹐不过﹐如果网络条件需要比默认值更多﹐则可以提高它(或许还要增加内存)。(事实上做NAT的时候最好可以适当地增加该值)\",\"actualValue>10000000\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.tcp_tw_reuse\",\"允许将TIME-WAIT状态的sockets重新用于新的TCP连接\",\"1\",\"0\",\"布尔值\",\"表示是否允许重新应用处于TIME-  WAIT状态的socket用于新的TCP连接(这个对快速重启动某些服务,而启动后提示端口已经被使用的情形非常有帮助)\",\"actualValue==1\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.tcp_tw_recycle\",\"表示开启TCP连接中TIME-WAIT状态sockets的快速回收\",\"1\",\"0\",\"布尔值\",\"打开快速 TIME-WAIT sockets 回收。除非得到技术专家的建议或要求﹐请不要随意修改这个值。(做NAT的时候，建议打开它)\",\"actualValue==1\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"DB\",\"max_process_memory\",\"设置一个数据库节点可用的最大物理内存\",\"2*1024*1024～INT_MAX\",\"12582912\",\"字节\",\"数据库节点上该数值需要根据系统物理内存及单节点部署主数据库节点个数决定。建议计算公式如下：(物理内存大小 - vm.min_free_kbytes) \\* 0.7 / (1 + 主节点个数)。该系数的目的是尽可能保证系统的可靠性，不会因数据库内存膨胀导致节点OOM。这个公式中提到vm.min_free_kbytes，其含义是预留操作系统内存供内核使用，通常用作操作系统内核中通信收发内存分配，至少为5%内存。即，max_process_memory = 物理内存 * 0.665 / (1 + 主节点个数)\",\"actualValue>=2*1024*1024\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"DB\",\"work_mem\",\"判断执行作业可下盘算子是否已使用内存量触发下盘点\",\"\",\"\",\"数目\",\"参数设置通常是一个权衡，即要保证并发的吞吐量，又要保证单查询作业的性能，故需要根据实际执行情况（结合Explain Performance输出）进行调优\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"DB\",\"pagewriter_sleep\",\"设置用于增量检查点打开后，pagewrite线程每隔pagewriter_sleep的时间刷一批脏页下盘。当脏页占据shared_buffers的比例达到dirty_page_percent_max时，每批页面数量以设定的max_io_capacity计算出的值刷页，其余情况每批页面数量按比例相对减少\",\"0～3600000\",\"2000\",\"ms\",\"\",\"actualValue>0 && actualValue<3600000\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.ip_local_port_range\",\"物理机可用临时端口范围\",\"26000-65535\",\"32768-61000\",\"数目\",\"表示用于向外连接的端口范围，默认比较小，这个范围同样会间接用于NAT表规模\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.tcp_keepalive_time\",\"表示当keepalive启用的时候，TCP发送keepalive消息的频度\",\"30\",\"7200\",\"秒\",\"TCP发送keepalive探测消息的间隔时间（秒），用于确认TCP连接是否有效。防止两边建立连接但不发送数据的攻击\",\"actualValue==30\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.tcp_keepalive_probes\",\"在认定连接失效之前，发送TCP的keepalive探测包数量。这个值乘以tcp_keepalive_intvl之后决定了一个连接发送了keepalive之后可以有多少时间没有回应\",\"9\",\"9\",\"秒\",\"TCP发送keepalive探测消息的间隔时间（秒），用于确认TCP连接是否有效\",\"actualValue==9\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.tcp_keepalive_intvl\",\"当探测没有确认时，重新发送探测的频度\",\"30\",\"75\",\"秒\",\"探测消息未获得响应时，重发该消息的间隔时间（秒）。默认值为75秒。 (对于普通应用来说,这个值有一些偏大,可以根据需要改小.特别是web类服务器需要改小该值,15是个比较合适的值)\",\"actualValue<30\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.tcp_retries1\",\"在连接建立过程中TCP协议最大重试次数\",\"5\",\"3\",\"次数\",\"放弃回应一个TCP连接请求前﹐需要进行多少次重试。RFC规定最低的数值是3\",\"actualValue>5\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.tcp_syn_retries\",\"TCP协议SYN报文最大重试次数\",\"5\",\"5\",\"次数\",\"对于一个新建连接，内核要发送多少个 SYN 连接请求才决定放弃。不应该大于255，默认值是5，对应于180秒左右时间。。(对于大负载而物理通信良好的网络而言,这个值偏高,可修改为2.这个值仅仅是针对对外的连接,对进来的连接,是由tcp_retries1决定的)\",\"actualValue<=5\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.tcp_synack_retries\",\"TCP协议SYN应答报文最大重试次数\",\"5\",\"5\",\"次数\",\"对于远端的连接请求SYN，内核会发送SYN ＋ ACK数据报，以确认收到上一个 SYN连接请求包。这是所谓的三次握手( threeway handshake)机制的第二个步骤。这里决定内核在放弃连接之前所送出的 SYN+ACK 数目。不应该大于255，默认值是5，对应于180秒左右时间\",\"actualValue==5\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.tcp_retries2\",\"控制内核向已经建立连接的远程主机重新发送数据的次数\",\"12\",\"15\",\"次数\",\"在丢弃激活(已建立通讯状况)的TCP连接之前﹐需要进行多少次重试。默认值为15，根据RTO的值来决定，相当于13-30分钟(RFC1122规定，必须大于100秒).(这个值根据目前的网络设置,可以适当地改小,我的网络内修改为了5)\",\"actualValue<15\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"vm.overcommit_memory\",\"控制在做内存分配的时候，内核的检查方式\",\"0\",\"0\",\"方式\",\"vm.overcommit_memory文件指定了内核针对内存分配的策略，其值可以是0、1、2\n" +
                    "0： (默认)表示内核将检查是否有足够的可用内存供应用进程使用；如果有足够的可用内存，内存申请允许；否则，内存申请失败，并把错误返回给应用进程。0 即是启发式的overcommitting handle,会尽量减少swap的使用,root可以分配比一般用户略多的内存\n" +
                    "1： 表示内核允许分配所有的物理内存，而不管当前的内存状态如何，允许超过CommitLimit，直至内存用完为止。在数据库服务器上不建议设置为1，从而尽量避免使用swap.\n" +
                    "2： 表示不允许超过CommitLimit值\",\"actualValue==0\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.tcp_rmem\",\"TCP协议接收端缓冲区的可用内存大小\",\"8192 250000 16777216\",\"4096 87380 174760（4k）\",\"字节\",\"接收缓存设置同tcp_wmem\",\"actualValue>=4096 87380 174760\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.tcp_wmem\",\"TCP协议发送端缓冲区的可用内存大小\",\"8192 250000 16777216\",\"4096 16384 131072（4k）\",\"字节\",\"发送缓存设置min：为TCP socket预留用于发送缓冲的内存最小值。每个tcp socket都可以在建议以后都可以使用它。默认值为4096(4K)。default：为TCP socket预留用于发送缓冲的内存数量，默认情况下该值会影响其它协议使用的net.core.wmem_default 值，一般要低于net.core.wmem_default的值。默认值为16384(16K)。max: 用于TCP socket发送缓冲的内存最大值。该值不会影响net.core.wmem_max，\"静态\"选择参数SO_SNDBUF则不受该值影响。默认值为131072(128K)。（对于服务器而言，增加这个参数的值对于发送数据很有帮助,在我的网络环境中,修改为了51200 131072 204800）\",\"actualValue>=4096 87380 174760\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.core.wmem_max\",\"socket发送端缓冲区大小的最大值\",\"21299200\",\"129024\",\"字节\",\"最大的TCP数据发送缓冲\",\"actualValue==21299200\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.core.rmem_max\",\"socket接收端缓冲区大小的最大值\",\"21299200\",\"129024\",\"字节\",\"最大的TCP数据接收缓冲\",\"actualValue==21299200\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.core.wmem_default\",\"socket发送端缓冲区大小的默认值\",\"21299200\",\"129024\",\"字节\",\"默认的发送窗口大小\",\"actualValue==21299200\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.core.rmem_default\",\"socket接收端缓冲区大小的默认值\",\"21299200\",\"129024\",\"字节\",\"默认的接收窗口大小\",\"actualValue==21299200\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"kernel.sem\",\"内核信号量参数设置大小\",\"250 6400000 1000 25600\",\"250 32000 32 128\",\"字节\",\"\",\"actualValue==250 6400000 1000 25600\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"vm.min_free_kbytes\",\"保证物理内存有足够空闲空间，防止突发性换页\",\"系统总内存的5%\",\"724\",\"字节\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.core.somaxconn\",\"定义了系统中每一个端口最大的监听队列的长度\",\"65535\",\"128\",\"个数\",\"用来限制监听(LISTEN)队列最大数据包的数量，超过这个数量就会导致链接超时或者触发重传机制。web应用中listen函数的backlog默认会给我们内核参数的net.core.somaxconn限制到128，而nginx定义的NGX_LISTEN_BACKLOG默认为511，所以有必要调整这个值。对繁忙的服务器,增加该值有助于网络性能\",\"actualValue==65535\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.tcp_syncookies\",\"当出现SYN等待队列溢出时，启用cookies来处理，可防范少量SYN攻击\",\"1\",\"0\",\"布尔值\",\"只有在内核编译时选择了CONFIG_SYNCOOKIES时才会发生作用。当出现syn等候队列出现溢出时象对方发送syncookies。目的是为了防止syn flood攻击\",\"actualValue==1\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.core.netdev_max_backlog\",\"在每个网络接口接收数据包的速率比内核处理这些包的速率快时，允许送到队列的数据包的最大数目\",\"65535\",\"1000\",\"个数\",\"\",\"actualValue==65535\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.tcp_max_syn_backlog\",\"记录的那些尚未收到客户端确认信息的连接请求的最大值\",\"65535\",\"1024\",\"个数\",\"对于那些依然还未获得客户端确认的连接请求﹐需要保存在队列中最大数目。对于超过 128Mb 内存的系统﹐默认值是 1024 ﹐低于 128Mb 的则为 128。如果服务器经常出现过载﹐可以尝试增加这个数字。警告﹗假如您将此值设为大于 1024﹐最好修改include/net/tcp.h里面的TCP_SYNQ_HSIZE﹐以保持TCP_SYNQ_HSIZE*16(SYN Flood攻击利用TCP协议散布握手的缺陷，伪造虚假源IP地址发送大量TCP-SYN半打开连接到目标系统，最终导致目标系统Socket队列资源耗尽而无法接受新的连接。为了应付这种攻击，现代Unix系统中普遍采用多连接队列处理的方式来缓冲(而不是解决)这种攻击，是用一个基本队列处理正常的完全连接应用(Connect()和Accept() )，是用另一个队列单独存放半打开连接。这种双队列处理方式和其他一些系统内核措施(例如Syn-Cookies/Caches)联合应用时，能够比较有效的缓解小规模的SYN Flood攻击(事实证明)\",\"actualValue==65535\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.tcp_fin_timeout\",\"系统默认的超时时间\",\"60\",\"60\",\"秒\",\"对于本端断开的socket连接，TCP保持在FIN-WAIT-2状态的时间。对方可能会断开连接或一直不结束连接或不可预料的进程死亡\",\"actualValue==60\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"kernel.shmall\",\"内核可用的共享内存总量\",\"1152921504606840000\",\"2097152\",\"字节\",\"\",\"actualValue==1152921504606840000\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"kernel.shmmax\",\"内核参数定义单个共享内存段的最大值\",\"18446744073709500000\",\"33554432\",\"字节\",\"\",\"actualValue==18446744073709500000\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.tcp_sack\",\"启用有选择的应答，通过有选择地应答乱序接受到的报文来提高性能，让发送者只发送丢失的报文段（对于广域网来说）这个选项应该启用，但是会增加对CPU的占用\",\"1\",\"1\",\"布尔值\",\"使用 Selective ACK﹐它可以用来查找特定的遗失的数据报--- 因此有助于快速恢复状态。该文件表示是否启用有选择的应答（Selective Acknowledgment），这可以通过有选择地应答乱序接收到的报文来提高性能（这样可以让发送者只发送丢失的报文段）。(对于广域网通信来说这个选项应该启用，但是这会增加对 CPU 的占用\",\"actualValue==1\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"net.ipv4.tcp_timestamps\",\"TCP时间戳（会在TCP包头增加12节），以一种比重发超时更精确的方式（参考RFC 1323）来启用对RTT的计算，启用可以实现更好的性能\",\"1\",\"1\",\"布尔值\",\"Timestamps 用在其它一些东西中﹐可以防范那些伪造的sequence 号码。一条1G的宽带线路或许会重遇到带 out-of-line数值的旧sequence 号码(假如它是由于上次产生的)。Timestamp 会让它知道这是个 '旧封包'。(该文件表示是否启用以一种比超时重发更精确的方法（RFC 1323）来启用对 RTT 的计算；为了实现更好的性能应该启用这个选项。)\",\"actualValue==1\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"vm.extfrag_threshold\",\"系统内存不够用时，linux会为当前系统内存碎片情况打分，如果超过vm.extfrag_threshold的值，kswapd就会触发memory compaction。所以这个值设置的接近1000，说明系统在内存碎片的处理倾向于把旧的页换出，以符合申请的需要，而设置接近0，表示系统在内存碎片的处理倾向做memory compaction\",\"500\",\"500\",\"分值\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"vm.overcommit_ratio\",\"系统使用绝不过量使用内存的算法时，系统整个内存地址空间不得超过swap+RAM值的此参数百分比，当vm.overcommit_memory=2时此参数生效\",\"90\",\"50\",\"%\",\"这个参数值只有在vm.overcommit_memory=2的情况下，这个参数才会生效\",\"actualValue>50\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"OS\",\"MTU\",\"节点网卡最大传输单元。OS默认值为1500，调整为8192可以提升SCTP协议数据收发的性能\",\"8192\",\"1500\",\"字节\",\"\",\"actualValue==8129\");",

            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"DB\",\"bgwriter_delay\",\"设置后端写进程写“脏”共享缓冲区之间的时间间隔\",\"10~10000\",\"2\",\"毫秒\",\"在数据写压力比较大的场景中可以尝试减小该值以降低checkpoint的压力\",\"actualValue>=10 && actualValue<=10000\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"DB\",\"bgwriter_thread_num\",\"设置后端写进程数量\",\"\",\"\",\"个数\",\"\",\"\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"DB\",\"max_io_capacity\",\"设置后端写进程批量刷页每秒的IO上限，需要根据具体业务场景和机器磁盘IO能力进行设置\",\"30720~10485760\",\"512000\",\"KB\",\"\",\"actualValue>=30720 && actualValue<=10485760\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"DB\",\"log_min_duration_statement\",\"当某条语句的持续时间大于或者等于特定的毫秒数时，log_min_duration_statement参数用于控制记录每条完成语句的持续时间\",\"-1 ~ INT_MAX\",\"1800000\",\"毫秒\",\"设置为250，所有运行时间不短于250ms的SQL语句都会被记录。\n" +
                    "设置为0，输出所有语句的持续时间。\n" +
                    "设置为-1，关闭此功能\",\"actualValue>=-1\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"DB\",\"log_duration\",\"控制记录每个已完成SQL语句的执行时间。对使用扩展查询协议的客户端、会记录语法分析、绑定和执行每一步所花费的时间\",\"off\",\"on\",\"布尔值\",\"设置为off，该选项与log_min_duration_statement的不同之处在于log_min_duration_statement强制记录查询文本。\n" +
                    "设置为on并且log_min_duration_statement大于零，记录所有持续时间，但是仅记录超过阈值的语句。这可用于在高负载情况下搜集统计信息\",\"actualValue=='off'\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"DB\",\"track_stmt_stat_level\",\"控制语句执行跟踪的级别,参数第一部分为非OFF情况下，会记录所有SQL，第一部分为OFF，第二部分为非OFF情况下，仅记录慢SQL\",\"OFF,L0\",\"OFF,L0\",\"字符型\",\"\",\"actualValue=='OFF,L0'\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"DB\",\"track_stmt_retention_time\",\"组合参数，控制全量/慢SQL记录的保留时间。以60秒为周期读取该参数，并执行清理超过保留时间的记录，仅sysadmin用户可以访问\",\"3600,604800\",\"3600,604800\",\"字符型\",\"\",\"actualValue=='3600,604800'\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"DB\",\"enable_thread_pool\",\"控制是否使用线程池功能\",\"off\",\"off\",\"布尔值\",\"on表示开启线程池功能。\n" +
                    "off表示不开启线程池功能\",\"actualValue=='off'\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"DB\",\"thread_pool_attr\",\"用于控制线程池功能的详细属性，该参数仅在enable_thread_pool打开后生效。\",\"长度大于0\",\"16, 2, (nobind)\",\"字符型\",\"\",\"actualValue>0\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"DB\",\"log_statement\",\"控制记录SQL语句。对于使用扩展查询协议的客户端，记录接收到执行消息的事件和绑定参数的值\",\"none\",\"none\",\"枚举型\",\"none表示不记录语句。\n" +
                    "ddl表示记录所有的数据定义语句，比如CREATE、ALTER和DROP语句。\n" +
                    "mod表示记录所有DDL语句，还包括数据修改语句INSERT、UPDATE、DELETE、TRUNCATE和COPY FROM 。\n" +
                    "all表示记录所有语句，PREPARE、EXECUTE和EXPLAIN ANALYZE语句也同样被记录\",\"actualValue=='none'\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"DB\",\"log_error_verbosity\",\"控制服务器日志中每条记录的消息写入的详细度\",\"default\",\"default\",\"枚举型\",\"terse代表输出不包括DETAIL、HINT、QUERY及CONTEXT错误信息的记录。\n" +
                    "verbose代表输出包括SQLSTATE错误代码、源代码文件名、函数名及产生错误所在的行号。\n" +
                    "default代表输出包括DETAIL、HINT、QUERY及CONTEXT错误信息的记录，不包括SQLSTATE错误代码 、源代码文件名、函数名及产生错误所在的行号\",\"actualValue=='default'\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"DB\",\"log_min_messages\",\"控制写到服务器日志文件中的消息级别。每个级别都包含排在它后面的所有级别中的信息。级别越低，服务器运行日志中记录的消息就越少\",\"warning\",\"warning\",\"枚举型\",\"有效值有debug、debug5、debug4、debug3、debug2、debug1、info、log、notice、warning、error、fatal、panic。\",\"actualValue=='warning'\");",
            CommonConstants.INSERT_INTO_PARAM_INFO +
                    " values(\"DB\",\"log_min_error_statement\",\"控制在服务器日志中记录错误的SQL语句。\",\"error\",\"error\",\"枚举型\",\"有效值有debug、debug5、debug4、debug3、debug2、debug1、info、log、notice、warning、error、fatal、panic。\",\"actualValue=='error'\");"};


    @PostConstruct
    public void init() throws IOException {
        File f = new File(path);
        log.info("sqlite:" + f.getCanonicalPath());
        if (!f.exists()) {
            needInit = true;
            var parent = f.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            boolean b=f.createNewFile();
            if(!b){
                log.error("ParamInfoInitConfig init createNewFile fail");
            }
        } else if (refresh) {
            needInit = true;
            Files.delete(f.toPath());
            boolean b=f.createNewFile();
            if(!b){
                log.error("ParamInfoInitConfig init createNewFile fail");
            }
        }
        if (needInit) {
            var sqLiteDataSource = new SQLiteDataSource();
            sqLiteDataSource.setUrl(JDBC.PREFIX + f.getCanonicalPath());
            try (var conn = sqLiteDataSource.getConnection();) {
                for (String sql : initSqls)
                    try {
                        conn.createStatement().execute(sql);
                    } catch (SQLException e) {
                        log.info(e.getMessage());
                    }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
