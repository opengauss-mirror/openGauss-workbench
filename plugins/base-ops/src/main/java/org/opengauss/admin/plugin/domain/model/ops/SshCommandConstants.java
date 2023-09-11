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
 * base-ops/src/main/java/org/opengauss/admin/plugin/domain/model/ops/SshCommandConstants.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin.domain.model.ops;

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
     * free memory space
     */
    String FREE_MEMORY = "free -g |head -n 2| tail -n 1 | awk '{print $4}'";
    /**
     * memory usage
     */
    String MEMORY_USING = "head -2 /proc/meminfo | awk 'NR==1{t=$2}NR==2{f=$2;print(t-f)*100/t}'";
    /**
     * memory size
     */
    String MEMORY_TOTAL = "cat /proc/meminfo | grep MemTotal | awk -F ' ' '{print $2}'";
    /**
     * Number of CPU cores
     */
    String CPU_CORE_NUM = "cat /proc/cpuinfo | grep 'processor' |wc -l";
    /**
     * CPU frequency
     */
    String CPU_FREQUENCY = "cat /proc/cpuinfo | grep 'model name' |awk -F ':' '{print $2}'| awk -F ' ' '{print $7}' | sort | head -n 1";
    /**
     * CPU usage
     */
    String CPU_USING = "top -b -n2 -p 1 | fgrep \"Cpu(s)\" | tail -1 | awk -F'id,' -v prefix=\"$prefix\" '{ split($1, vs, \",\"); v=vs[length(vs)]; sub(\"%\",\"\", v); printf \"%s%.1f\\n\", prefix, 100 - v }'";
    /**
     * free hard disk space
     */
    String FREE_HARD_DISK = "df -BG | awk -F ' ' '{print $4}' | sort";
    /**
     * rely
     */
    String DEPENDENCY = "yum list installed | egrep 'libaio-devel|flex|bison|ncurses-devel|glibc-devel|patch|redhat-lsb-core|readline-devel'";
    /**
     * firewall
     */
    String FIREWALL = "systemctl status firewalld | head -n 3 | tail -n 1 | awk '{print $2}'";

    /**
     * Install user
     */
    String OMM_USER = "cat /etc/passwd | awk -F \":\" \"{print $1}\"|grep omm | wc -l";
    /**
     * Create OMM user
     */
    String CREATE_OMM_USER = "useradd omm";

    String CHANGE_OMM_PASSWORD_TEMPLATE = "passwd {0}";
    /**
     * Modify OMM password
     */
    String CHANGE_OMM_PASSWORD = "passwd omm";

    String LIMITS_CHECK = "cat /etc/security/limits.conf | grep 1048576";

    String LIMITS = "echo -e \"* hard nofile 1048576\\n* soft nofile 1048576\\n* hard nproc 1048576\\n* soft nproc 1048576\" >> /etc/security/limits.conf";

    String HOSTNAME = "cat /etc/hostname | sed 's/\\\"//g'";
    /**
     * decompress
     */
    String DECOMPRESS = "tar {0} {1} -C {2}";
    /**
     * SEMMNI
     */
    String SEM = "sysctl -w kernel.sem=\"50100 128256000 50100 2560\"";
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
    String LITE_SINGLE_INSTALL = "echo {0} | sh {1}/install.sh --mode single -D {2} -R {3}  --start -C port={4}";
    /**
     * Lite masternode installation
     */
    String LITE_MASTER_INSTALL = "echo {0} | sh {1}/install.sh --mode primary -D {2} -R {3} -C \"replconninfo1=''localhost={4} localport={5} remotehost={6} remoteport={7}''\"  --start";
    /**
     * Lite install from node
     */
    String LITE_SLAVE_INSTALL = "echo {0} | sh {1}/install.sh --mode standby -D {2} -R {3} -C \"replconninfo1=''localhost={4} localport={5} remotehost={6} remoteport={7}''\"  --start";
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
    String GS_PREINSTALL_INTERACTIVE = "cd {0} && ./gs_preinstall -U {1} -G {2} -X {3}";
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
    String HBA = "gs_guc set -D {0} -h \"host all gaussdb 0.0.0.0/0 sha256\"";
    /**
     * Login to openGauss
     */
    String LOGIN = "gsql -d postgres -p {0}";
    /**
     * network usage
     */
    String NET = "cat /proc/net/dev";
    String DEL_FILE = "rm -rf {0}";
    String INSTALL_DEPENDENCY = "yum install -y wget net-tools python3 bzip2 expect libaio-devel flex bison ncurses-devel glibc-devel patch redhat-lsb readline-devel";

    String INSTALL_DEPENDENCY_OPENEULER_X86 = "yum install -y wget net-tools python3 bzip2 expect libaio-devel flex bison ncurses-devel glibc-devel patch readline-devel libnsl tar";

    String INSTALL_DEPENDENCY_OPENEULER_ARCH64 = "yum install -y wget net-tools python3 bzip2 expect libaio-devel flex bison ncurses-devel glibc-devel patch readline-devel tar";

}
