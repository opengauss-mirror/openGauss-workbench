/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 * -------------------------------------------------------------------------
 *
 * SshCommandConstants.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-common/src/main/java/org/opengauss/admin/common/constant/ops/SshCommandConstants.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.common.constant.ops;

/**
 * @author lhf
 * @date 2022/8/10 14:51
 **/
public interface SshCommandConstants {
    /**
     * query operating system
     */
    String OS = "cat /etc/os-release | grep ID= | head -n 1 | awk -F '=' '{print $2}' | sed 's/\\\"//g'";
    /**
     * Query the operating system version
     */
    String OS_VERSION = "cat /etc/os-release | grep VERSION_ID= | head -n 1|awk -F '=' '{print $2}' | sed 's/\\\"//g'";

    /**
     * get memory info
     */
    String MEMORY = "free -g |head -n 2| tail -n 1  | tr -s \" \"";

    /**
     * CPU usage
     */
    String CPU_USING
        = "awk 'NR==1 {u=$2; n=$3; s=$4; i=$5; w=$6; x=$7; y=$8; z=$9} END {total=u+n+s+i+w+x+y+z; idle=i; printf \"%"
        + ".1f\\n\", 100 - (idle*100/total)}' /proc/stat";
    /**
     * rely
     */
    String DEPENDENCY
        = "yum list installed | egrep 'libaio-devel|flex|bison|ncurses-devel|glibc-devel|patch|readline-devel'";

    /**
     * base dependencies
     */
    String BASE_DEPENDENCY = "yum list installed | egrep 'coreutils|procps-ng|openssh-clients|unzip|lsof|grep|tar'";
    /**
     * base dependencies
     */
    String OPENEULER_BASE_DEPENDENCY = "yum list installed | egrep 'coreutils|procps-ng|openssh|unzip|lsof|grep|tar'";

    /**
     * firewall
     */
    String FIREWALL = "systemctl status firewalld | head -n 3 | tail -n 1 | awk '{print $2}'";

    /**
     * Install user
     */
    String OMM_USER = "cat /etc/passwd | awk -F \":\" \"{print $1}\"|grep omm | wc -l";

    /**
     * Modify user password
     */
    String CHANGE_OMM_PASSWORD_TEMPLATE = "LC_ALL=C passwd {0}";
    /**
     * Modify OMM password
     */
    String CHANGE_OMM_PASSWORD = "passwd omm";

    String LIMITS_CHECK = " cat /etc/security/limits.conf | grep 1048576 ";

    /**
     * linux limits config
     */
    String LIMITS =
        "echo -e \"* hard nofile 1048576\\n* soft nproc 1048576\\n* hard nproc 1048576\\n* soft nproc 1048576\" >> "
            + "/etc/security/limits.conf";

    /**
     * hostname
     */
    String HOSTNAME = "hostname";
    /**
     * decompress
     */
    String DECOMPRESS = "tar {0} {1} -C {2}";
    /**
     * SEMMNI
     */
    String SEM = "sysctl -w kernel.sem=\"250 85000 250 330\"";
    /**
     * Minimalist single node installation
     */
    String MINIMAL_LIST_SINGLE_INSTALL = "sh {0}/install.sh  -w {1} -p {2} &&source ~/.bashrc";
    /**
     * Minimalist cluster installation
     */
    String MINIMAL_LIST_CLUSTER_INSTALL = "sh {0}install.sh  -w {1} -p {2} --multinode &&source ~/.bashrc";
    /**
     * Minimalist Single Node Uninstall
     */
    String MINIMAL_LIST_SINGLE_UNINSTALL = "gs_ctl stop -D {0}/single_node";
    /**
     * Minimalist single node restart
     */
    String MINIMAL_LIST_SINGLE_RESTART = "gs_ctl restart -D {0}/single_node";
    /**
     * Minimalist single node startup
     */
    String MINIMAL_LIST_SINGLE_START = "gs_ctl start -D {0}/single_node";
    /**
     * Minimalist single node stop
     */
    String MINIMAL_LIST_SINGLE_STOP = "gs_ctl stop -D {0}/single_node";
    /**
     * Minimalist master node uninstall
     */
    String MINIMAL_LIST_MASTER_UNINSTALL = "gs_ctl stop -D {0}/master";
    /**
     * minimalist uninstall from node
     */
    String MINIMAL_LIST_SLAVE_UNINSTALL = "gs_ctl stop -D {0}/slave";
    /**
     * Minimalist master node restart
     */
    String MINIMAL_LIST_MASTER_RESTART = "gs_ctl restart -D {0}/master";
    /**
     * Minimalist masternode startup
     */
    String MINIMAL_LIST_MASTER_START = "gs_ctl start -D {0}/master";
    /**
     * Minimalist masternode stopped
     */
    String MINIMAL_LIST_MASTER_STOP = "gs_ctl stop -D {0}/master";
    /**
     * Minimalist restart from node
     */
    String MINIMAL_LIST_SLAVE_RESTART = "gs_ctl restart -D {0}/slave";
    /**
     * The minimalist version starts from node
     */
    String MINIMAL_LIST_SLAVE_START = "gs_ctl start -D {0}/slave";
    /**
     * minimalist stop from node
     */
    String MINIMAL_LIST_SLAVE_STOP = "gs_ctl stop -D {0}/slave";
    /**
     * Lite reboot
     */
    String LITE_RESTART = "gs_ctl restart -D {0}";
    /**
     * Lite launch
     */
    String LITE_START = "gs_ctl start -D {0}";
    /**
     * Lite version discontinued
     */
    String LITE_STOP = "gs_ctl stop -D {0}";
    /**
     * Create a directory
     */
    String MK_DIR = "mkdir -p {0}";
    /**
     * Remote copy from current host to host
     */
    String SCP = "scp {0} root@{1}:{2}";
    /**
     * move files
     */
    String MV = "mv -rf {0} ./old/";
    /**
     * grant permission
     */
    String CHMOD = "chmod 755 -R {0}";

    String CHMOD_DATA_PATH = "chmod 700 -R {0}";
    String CHOWN = "chown {0} -R {1}";
    /**
     * switch user
     */
    String SU = "su {0}";
    /**
     * Lite single node installation
     */
    String LITE_SINGLE_INSTALL = "echo {0} | sh {1}/install.sh --mode single -D {2} -R {3}  --start";
    /**
     * Lite masternode installation
     */
    String LITE_MASTER_INSTALL =
        "echo {0} | sh {1}/install.sh --mode primary -D {2} -R {3} -C \"replconninfo1=''localhost={4} localport={5}"
            + " remotehost={6} remoteport={7}''\"  --start";
    /**
     * Lite install from node
     */
    String LITE_SLAVE_INSTALL =
        "echo {0} | sh {1}/install.sh --mode standby -D {2} -R {3} -C \"replconninfo1=''localhost={4} localport={5}"
            + " remotehost={6} remoteport={7}''\"  --start";
    /**
     * light version uninstall
     */
    String LITE_UNINSTALL = "sh {0}/uninstall.sh --delete-data";
    /**
     * Create a file
     */
    String FILE_CREATE = "echo ''{0}'' > {1}";
    /**
     * Enterprise Edition preInstall
     */
    String GS_PREINSTALL_INTERACTIVE = "cd {0} && ./gs_preinstall -U {1} -G {2} -X {3} --non-interactive";
    String GS_PREINSTALL = "cd {0} && ./gs_preinstall -U {1} -G {2} -X {3}";
    /**
     * Enterprise Edition Installation
     */
    String ENTERPRISE_INSTALL = "cd ~ && gs_install -X {0}";
    /**
     * Modify the listening address
     */
    String LISTENER = "gs_guc set -D {0} -c \" listen_addresses = ''*''\"";
    /**
     * Modify hba.conf
     */
    String HBA = "gs_guc set -D {0} -h \"host all all 0.0.0.0/0 sha256\" "
            + "&& gs_guc set -D {0} -h \"host all all ::/0 sha256\"";
    /**
     * Login to openGauss
     */
    String LOGIN = "gsql -d postgres -p {0}";
    /**
     * network usage
     */
    String NET = "cat /proc/net/dev";
    String DEL_FILE = "rm -rf {0}";

    /**
     * cpu arch
     */
    String CPU_ARCH = "LC_ALL=C lscpu | grep Architecture: | head -n 1 | awk -F ':' '{print $2}'";
    /**
     * check os user exist
     */
    String CHECK_OS_USER_EXIST = "export PATH=$PATH:/sbin:/usr/sbin &&  id -u %s";
    /**
     * message constants no such user
     */
    String NO_SUCH_USER = "no such user";
    /**
     * create os user
     */
    String CREATE_OS_USER = "useradd %s";

    /**
     * check os user sudo no password permission
     */
    String CREATE_OS_USER_SUDO = "sudo -n true && echo \"%s\" || echo \"%s\"";

    /**
     * check os disk info
     */
    String DISK_TOTAL_MONITOR = "df -T --total | tail -n +2| tr -s \" \" | grep total";

    /**
     * check os net card name
     */
    String NET_CARD_NAME = "export PATH=$PATH:/sbin:/usr/sbin && ip -o addr show | grep %s | awk '{print $2}'";
    /**
     * check os net card rx tx
     */
    String NET_MONITOR = "export PATH=$PATH:/sbin:/usr/sbin  net_card_name=\"%s\"\n"
        + "    rx_net1=$(ip -s link show $net_card_name | awk '/RX:/{getline; print $1}')\n"
        + "    tx_net1=$(ip -s link show $net_card_name | awk '/TX:/{getline; print $1}')\n" + "    sleep 1\n"
        + "    rx_net2=$(ip -s link show $net_card_name | awk '/RX:/{getline; print $1}')\n"
        + "    tx_net2=$(ip -s link show $net_card_name | awk '/TX:/{getline; print $1}')\n"
        + "    rx_net=$((rx_net2 - rx_net1))\n" + "    tx_net=$((tx_net2 - tx_net1))\n"
        + "    echo \"$rx_net|$tx_net\"";
    /**
     * check os port conflict
     */
    String CHECK_OS_PORT_CONFLICT = "export PATH=$PATH:/sbin:/usr/sbin &&  ss -tuln | grep -E \":%d\\s+\" | wc -l";
    /**
     * check os openGauss Version
     */
    String OPENGAUSS_MAIN_VERSION_NUM = "gsql -V| awk '{print $3}'";
    /**
     * check os user java version
     */
    String JAVA_VERSION = "java -version";
    /**
     * base host info: contains cpu core number, remaining memory, available disk space
     */
    String HOST_BASE_INFO = "cat /proc/cpuinfo |grep \"processor\"|wc -l && grep MemFree /proc/meminfo "
        + "| awk '{val=$2/1024}END{print val}' && df -Th | egrep -v \"(tmpfs|sr0)\" | tail -n +2| "
        + "tr -s \" \" | cut -d \" \" -f5 | tr -d \"G\" | awk '{sum+=$1}END{print sum}'";
    /**
     * check path is empty
     */
    String CHECK_PATH_EMPTY = "if [ $(ls -A %s | wc -l) -eq 0 ]; then\n echo dir_empty\n else\n echo dir_no_empty\n fi";
    /**
     * check file is exist
     */
    String CHECK_FILE_EXIST = "if [ -e %s ]; then  echo file_exist; else   echo file_not_exist; fi";
    /**
     * check result
     * file_exist
     */
    String CHECK_RESULT_FILE_EXIST = "file_exist";
    /**
     * check result
     * dir_empty:
     */
    String CHECK_RESULT_PATH_EMPTY = "dir_empty";

    /**
     * get host cpu info
     */
    String CPU = "lscpu | grep \"CPU(s):\\|Architecture\\|MHz\"";
    /**
     * check openGauss version
     * ENTERPRISE:
     */
    String CHECK_ENTERPRISE_VERSION = "source %s;gs_om -t view&&[ -f %s ]";
    /**
     * check openGauss version
     * LITE:
     */
    String CHECK_LITE_VERSION = "source %s;gsql -V|grep -i \"openGauss-lite\"&&[ -f %s ]";
    /**
     * check openGauss version
     * MINILIST:
     */
    String CHECK_MINILIST_VERSION = "source %s;gsql -V&&[ -f %s ]";

    /**
     * check environment is online by  curl -I https://opengauss.org/zh/
     */
    String ONLINE = "curl -I https://opengauss.org/zh/";
}
