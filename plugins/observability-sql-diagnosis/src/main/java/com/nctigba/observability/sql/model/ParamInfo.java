/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.observability.sql.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.LinkedList;
import java.util.List;

/**
 * ParamInfo
 *
 * @author luomeng
 * @since 2023/9/26
 */
@Data
@Accessors(chain = true)
@TableName(value = "param_info", autoResultMap = true)
@NoArgsConstructor
public class ParamInfo {
    @TableId(type = IdType.AUTO)
    Integer id;
    @TableField("paramType")
    ParamType paramType;
    @TableField("paramName")
    String paramName;
    String paramDetail;
    @TableField("suggestValue")
    String suggestValue;
    @TableField("defaultValue")
    String defaultValue;
    String unit;
    String suggestExplain;
    @TableField("diagnosisRule")
    String diagnosisRule;

    /**
     * ParamType
     *
     * @author luomeng
     * @since 2023/9/26
     */
    public enum ParamType {
        OS,
        DB
    }

    /**
     * Construction method
     *
     * @param id param info
     * @param paramType param info
     * @param paramName param info
     * @param paramDetail param info
     * @param suggestValue param info
     * @param defaultValue param info
     * @param unit param info
     * @param suggestExplain param info
     * @param diagnosisRule param info
     */
    public ParamInfo(Integer id, ParamType paramType, String paramName, String paramDetail, String suggestValue,
            String defaultValue, String unit, String suggestExplain, String diagnosisRule) {
        this.id = id;
        this.paramType = paramType;
        this.paramName = paramName;
        this.paramDetail = paramDetail;
        this.suggestValue = suggestValue;
        this.defaultValue = defaultValue;
        this.unit = unit;
        this.suggestExplain = suggestExplain;
        this.diagnosisRule = diagnosisRule;
    }

    /**
     * toString method
     *
     * @return String
     */
    public String toString() {
        return String.format("%d,'%s','%s','%s','%s','%s','%s','%s','%s'", id, paramType, paramName, paramDetail,
                suggestValue, defaultValue, unit, suggestExplain, diagnosisRule);
    }

    /**
     * Get all param info
     *
     * @return list
     */
    public static List<ParamInfo> getStaticInfos() {
        return new LinkedList<>() {
            {
                add(new ParamInfo(1, ParamType.OS, "net.ipv4.tcp_max_tw_buckets",
                        "表示同时保持TIME_WAIT状态的TCP/IP连接最大数量。如果超过所配置的取值，"
                                + "TIME_WAIT将立刻被释放并打印警告信息",
                        "10000", "180000", "数目",
                        "系统在同时所处理的最大 timewait sockets 数目。如果超过此数的话﹐time-wait socket "
                                + "会被立即砍除并且显示警告信息。之所以要设定这个限制﹐纯粹为了抵御那些简单的 DoS 攻击﹐不过﹐"
                                + "如果网络条件需要比默认值更多﹐则可以提高它(或许还要增加内存)。"
                                + "(事实上做NAT的时候最好可以适当地增加该值)",
                        "actualValue>=10000"));
                add(new ParamInfo(2, ParamType.OS, "net.ipv4.tcp_tw_reuse",
                        "允许将TIME-WAIT状态的sockets重新用于新的TCP连接", "1",
                        "0", "布尔值", "表示是否允许重新应用处于TIME-  "
                        + "WAIT状态的socket用于新的TCP连接(这个对快速重启动某些服务,"
                        + "而启动后提示端口已经被使用的情形非常有帮助)", "actualValue==1"));
                add(new ParamInfo(3, ParamType.OS, "net.ipv4.tcp_tw_recycle",
                        "表示开启TCP连接中TIME-WAIT状态sockets的快速回收", "1",
                        "0", "布尔值", "打开快速 TIME-WAIT sockets 回收。"
                        + "除非得到技术专家的建议或要求﹐请不要随意修改这个值。"
                        + "(做NAT的时候，建议打开它)", "actualValue==1"));
                add(new ParamInfo(4, ParamType.DB, "max_process_memory",
                        "设置一个数据库节点可用的最大物理内存", "5578424",
                        "12582912", "字节",
                        "数据库节点上该数值需要根据系统物理内存及单节点部署主数据库节点个数决定。"
                                + "建议计算公式如下：(物理内存大小 - vm.min_free_kbytes) * 0.7 / (1 + 主节点个数)。"
                                + "该系数的目的是尽可能保证系统的可靠性，不会因数据库内存膨胀导致节点OOM。"
                                + "这个公式中提到vm.min_free_kbytes，"
                                + "其含义是预留操作系统内存供内核使用，通常用作操作系统内核中通信收发内存分配，至少为5%内存。"
                                + "即，max_process_memory = 物理内存 * 0.665 / (1 + 主节点个数)",
                        "actualValue==5578424"));
                add(new ParamInfo(5, ParamType.OS, "net.ipv4.tcp_keepalive_time",
                        "表示当keepalive启用的时候，"
                                + "TCP发送keepalive消息的频度", "30", "7200", "秒",
                        "TCP发送keepalive探测消息的间隔时间（秒），"
                                + "用于确认TCP连接是否有效。防止两边建立连接但不发送数据的攻击", "actualValue==30"));
                add(new ParamInfo(6, ParamType.OS, "net.ipv4.tcp_keepalive_probes",
                        "在认定连接失效之前，发送TCP的keepalive探测包数量。"
                                + "这个值乘以tcp_keepalive_intvl之后决定了一个连接发送了keepalive之后可以有多少时间没有回应",
                        "9", "9",
                        "秒", "TCP发送keepalive探测消息的间隔时间（秒），用于确认TCP连接是否有效",
                        "actualValue==9"));
                add(new ParamInfo(7, ParamType.OS, "net.ipv4.tcp_keepalive_intvl",
                        "当探测没有确认时，重新发送探测的频度", "30", "75",
                        "秒", "探测消息未获得响应时，重发该消息的间隔时间（秒）。默认值为75秒。"
                        + " (对于普通应用来说,这个值有一些偏大,"
                        + "可以根据需要改小.特别是web类服务器需要改小该值,15是个比较合适的值)", "actualValue<=30"));
                add(new ParamInfo(8, ParamType.OS, "net.ipv4.tcp_retries1",
                        "在连接建立过程中TCP协议最大重试次数", "5", "3", "次数",
                        "放弃回应一个TCP连接请求前﹐需要进行多少次重试。RFC规定最低的数值是3",
                        "actualValue>5"));
                add(new ParamInfo(9, ParamType.OS, "net.ipv4.tcp_syn_retries",
                        "TCP协议SYN报文最大重试次数", "5", "5", "次数",
                        "对于一个新建连接，内核要发送多少个 SYN 连接请求才决定放弃。"
                                + "不应该大于255，默认值是5，对应于180秒左右时间。。"
                                + "(对于大负载而物理通信良好的网络而言,这个值偏高,可修改为2.这个值仅仅是针对对外的连接,对进来的连接,"
                                + "是由tcp_retries1决定的)", "actualValue<=5"));
                add(new ParamInfo(10, ParamType.OS, "net.ipv4.tcp_synack_retries",
                        "TCP协议SYN应答报文最大重试次数", "5", "5", "次数",
                        "对于远端的连接请求SYN，内核会发送SYN ＋ ACK数据报，以确认收到上一个 "
                                + "SYN连接请求包。这是所谓的三次握手( "
                                + "threeway handshake)机制的第二个步骤。这里决定内核在放弃连接之前所送出的 SYN+ACK 数目。"
                                + "不应该大于255，"
                                + "默认值是5，对应于180秒左右时间", "actualValue==5"));
                add(new ParamInfo(11, ParamType.OS, "net.ipv4.tcp_retries2",
                        "控制内核向已经建立连接的远程主机重新发送数据的次数", "12",
                        "15", "次数", "在丢弃激活(已建立通讯状况)的TCP连接之前﹐"
                        + "需要进行多少次重试。默认值为15，根据RTO的值来决定，"
                        + "相当于13-30分钟(RFC1122规定，必须大于100秒).(这个值根据目前的网络设置,可以适当地改小,我的网络内修改为了5)",
                        "actualValue<=15"));
                add(new ParamInfo(12, ParamType.OS, "vm.overcommit_memory",
                        "控制在做内存分配的时候，内核的检查方式", "0", "0", "方式",
                        "vm.overcommit_memory文件指定了内核针对内存分配的策略，其值可以是0、1、2n"
                                + "0： (默认)表示内核将检查是否有足够的可用内存供应用进程使用；如果有足够的可用内存，内存申请允许；"
                                + "否则，内存申请失败，"
                                + "并把错误返回给应用进程。0 即是启发式的overcommitting handle,会尽量减少swap的使用,"
                                + "root可以分配比一般用户略多的内存n"
                                + "1： 表示内核允许分配所有的物理内存，而不管当前的内存状态如何，"
                                + "允许超过CommitLimit，直至内存用完为止。"
                                + "在数据库服务器上不建议设置为1，从而尽量避免使用swap.n" +
                                "2： 表示不允许超过CommitLimit值", "actualValue==0"));
                add(new ParamInfo(13, ParamType.OS, "net.ipv4.tcp_rmem",
                        "TCP协议接收端缓冲区的可用内存大小", "8192 250000 16777216",
                        "4096 87380 174760（4k）", "字节", "接收缓存设置同tcp_wmem",
                        "actualValue>=409687380174760"));
                add(new ParamInfo(14, ParamType.OS, "net.ipv4.tcp_wmem",
                        "TCP协议发送端缓冲区的可用内存大小", "8192 250000 16777216",
                        "4096 16384 131072（4k）", "字节",
                        "发送缓存设置min：为TCP socket预留用于发送缓冲的内存最小值。"
                                + "每个tcp socket都可以在建议以后都可以使用它。默认值为4096(4K)。default：为TCP socket"
                                + "预留用于发送缓冲的内存数量，默认情况下该值会影响其它协议使用的net.core.wmem_default 值，"
                                + "一般要低于net.core.wmem_default的值。默认值为16384(16K)。max: 用于TCP "
                                + "socket发送缓冲的内存最大值。"
                                + "该值不会影响net.core.wmem_max，\"静态\"选择参数SO_SNDBUF则不受该值影响。"
                                + "默认值为131072(128K)。"
                                + "（对于服务器而言，增加这个参数的值对于发送数据很有帮助,在我的网络环境中,"
                                + "修改为了51200 131072 204800）",
                        "actualValue>=409687380174760"));
                add(new ParamInfo(15, ParamType.OS, "net.core.wmem_max",
                        "socket发送端缓冲区大小的最大值", "21299200", "129024",
                        "字节", "最大的TCP数据发送缓冲", "actualValue==21299200"));
                add(new ParamInfo(16, ParamType.OS, "net.core.rmem_max",
                        "socket接收端缓冲区大小的最大值", "21299200", "129024",
                        "字节", "最大的TCP数据接收缓冲", "actualValue==21299200"));
                add(new ParamInfo(17, ParamType.OS, "net.core.wmem_default",
                        "socket发送端缓冲区大小的默认值", "21299200", "129024",
                        "字节", "默认的发送窗口大小", "actualValue==21299200"));
                add(new ParamInfo(18, ParamType.OS, "net.core.rmem_default",
                        "socket接收端缓冲区大小的默认值", "21299200", "129024",
                        "字节", "默认的接收窗口大小", "actualValue==21299200"));
                add(new ParamInfo(19, ParamType.OS, "net.core.somaxconn",
                        "定义了系统中每一个端口最大的监听队列的长度", "65535", "128",
                        "个数", "用来限制监听(LISTEN)队列最大数据包的数量，"
                        + "超过这个数量就会导致链接超时或者触发重传机制。"
                        + "web应用中listen函数的backlog默认会给我们内核参数的net.core.somaxconn限制到128，"
                        + "而nginx定义的NGX_LISTEN_BACKLOG默认为511，所以有必要调整这个值。对繁忙的服务器,增加该值有助于网络性能",
                        "actualValue==65535"));
                add(new ParamInfo(20, ParamType.OS, "net.ipv4.tcp_syncookies",
                        "当出现SYN等待队列溢出时，启用cookies来处理，"
                                + "可防范少量SYN攻击", "1", "0", "布尔值",
                        "只有在内核编译时选择了CONFIG_SYNCOOKIES时才会发生作用。"
                                + "当出现syn等候队列出现溢出时象对方发送syncookies。目的是为了防止syn flood攻击",
                        "actualValue==1"));
                add(new ParamInfo(21, ParamType.OS, "net.ipv4.tcp_max_syn_backlog",
                        "记录的那些尚未收到客户端确认信息的连接请求的最大值",
                        "65535", "1024", "个数",
                        "对于那些依然还未获得客户端确认的连接请求﹐需要保存在队列中最大数目。"
                                + "对于超过 128Mb 内存的系统﹐默认值是 1024 ﹐低于 128Mb 的则为 128。如果服务器经常出现过载﹐"
                                + "可以尝试增加这个数字。"
                                + "警告﹗假如您将此值设为大于 1024﹐最好修改include/net/tcp.h里面的TCP_SYNQ_HSIZE﹐"
                                + "以保持TCP_SYNQ_HSIZE*16(SYN Flood攻击利用TCP协议散布握手的缺陷，"
                                + "伪造虚假源IP地址发送大量TCP-SYN"
                                + "半打开连接到目标系统，最终导致目标系统Socket队列资源耗尽而无法接受新的连接。为了应付这种攻击，"
                                + "现代Unix系统中普遍采用多连接队列处理的方式来缓冲(而不是解决)这种攻击，"
                                + "是用一个基本队列处理正常的完全连接应"
                                + "用(Connect()和Accept() )，是用另一个队列单独存放半打开连接。"
                                + "这种双队列处理方式和其他一些系统内核措施"
                                + "(例如Syn-Cookies/Caches)联合应用时，能够比较有效的缓解小规模的SYN Flood攻击(事实证明)",
                        "actualValue==65535"));
                add(new ParamInfo(22, ParamType.OS, "net.ipv4.tcp_fin_timeout",
                        "系统默认的超时时间",
                        "60", "60", "秒",
                        "对于本端断开的socket连接，TCP保持在FIN-WAIT-2状态的时间。对方可能会断开连接或一直不结束连接"
                                + "或不可预料的进程死亡", "actualValue==60"));
                add(new ParamInfo(23, ParamType.OS, "net.ipv4.tcp_sack",
                        "启用有选择的应答，通过有选择地应答乱序接受到的报文来提高性能，"
                                + "让发送者只发送丢失的报文段（对于广域网来说）这个选项应该启用，但是会增加对CPU的占用",
                        "1", "1", "布尔值",
                        "使用 Selective ACK﹐它可以用来查找特定的遗失的数据报--- 因此有助于快速恢复状态。"
                                + "该文件表示是否启用有选择的应答"
                                + "（Selective Acknowledgment），这可以通过有选择地应答乱序接收到的报文来提高性能"
                                + "（这样可以让发送者只发送丢失的报文段）。(对于广域网通信来说这个选项应该启用，"
                                + "但是这会增加对 CPU 的占用",
                        "actualValue==1"));
                add(new ParamInfo(24, ParamType.OS, "net.ipv4.tcp_timestamps",
                        "TCP时间戳（会在TCP包头增加12节），以一种比重发超时更精确的方式"
                                + "（参考RFC 1323）来启用对RTT的计算，启用可以实现更好的性能",
                        "1", "1", "布尔值",
                        "Timestamps 用在其它一些东西中﹐可以防范那些伪造的sequence 号码。"
                                + "一条1G的宽带线路或许会重遇到带 "
                                + "out-of-line数值的旧sequence 号码(假如它是由于上次产生的)。Timestamp 会让它知道这是个 '旧封包'。"
                                + "(该文件表示是否启用以一种比超时重发更精确的方法（RFC 1323）来启用对 RTT 的计算；"
                                + "为了实现更好的性能应该启用这个选项。)", "actualValue==1"));
                add(new ParamInfo(25, ParamType.OS, "vm.overcommit_ratio",
                        "系统使用绝不过量使用内存的算法时，"
                                + "系统整个内存地址空间不得超过swap+RAM值的此参数百分比，当vm.overcommit_memory=2时此参数生效",
                        "90",
                        "50", "%", "这个参数值只有在vm.overcommit_memory=2的情况下，"
                        + "这个参数才会生效",
                        "actualValue>50"));
                add(new ParamInfo(26, ParamType.OS, "MTU",
                        "节点网卡最大传输单元。OS默认值为1500，调整为8192可以提升SCTP协议数据收发的性能",
                        "8192", "1500", "字节", "",
                        "actualValue==8129"));
            }
        };
    }
}