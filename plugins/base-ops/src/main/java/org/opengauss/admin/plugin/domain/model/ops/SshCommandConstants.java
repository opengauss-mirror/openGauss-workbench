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
 * @since 2022/8/10 14:51
 **/
public interface SshCommandConstants {
    /**
     * default bashrc
     */
    String DEFAULT_ENV_BASHRC = "~/.bashrc";

    /**
     * free hard disk space dir
     */
    String DIR_FREE_HARD_DISK = "df -BG {0} | awk -F ' ' '{print $4}' | sort";
    /**
     * install dependencies
     */
    String DEPENDENCY = "yum list installed | egrep 'libaio-devel|flex|bison|ncurses-devel|glibc-devel|patch|redhat" +
            "-lsb-core|readline-devel|lsscsi'";
    /**
     * monitor dependencies
     */
    String MONITOR_DEPENDENCY = "yum list installed | egrep 'coreutils|procps-ng|grep'";

    /**
     * Install user
     */
    String OMM_USER = "cat /etc/passwd | awk -F \":\" \"{print $1}\"|grep omm | wc -l";
    /**
     * Create OMM user
     */
    String CREATE_OMM_USER = "useradd omm";

    /**
     * Modify OMM password
     */
    String CHANGE_OMM_PASSWORD = "passwd omm";

    /**
     * limit check
     */
    String LIMITS_CHECK = " cat /etc/security/limits.conf | grep 1048576 ";

    /**
     * limit
     */
    String LIMITS = "echo -e \"* hard nofile 1048576\\n* soft nofile 1048576\\n* hard nproc 1048576\\n* soft nproc 1048576\" >> /etc/security/limits.conf";

    /**
     * check hostname
     */
    String HOSTNAME = "cat /etc/hostname | sed 's/\\\"//g'";
    /**
     * decompress
     */
    String DECOMPRESS = "tar {0} {1} -C {2}";
    /**
     * SEMMNI
     */
    String SEM = "echo 'kernel.sem = 250 6400000 1000 25600' | sudo tee -a /etc/sysctl.conf && sysctl -p";
    /**
     * SEM MNI
     */
    String SEM_VALUE = "250 6400000 1000 25600";
    /**
     * check sem
     */
    String CHECK_SEM = "sysctl kernel.sem | awk '{for(i=3;i<=NF;i++) printf $i (i<NF?\" \":\"\\n\")}'";
    /**
     * Minimalist single node installation
     */
    String MINIMAL_LIST_SINGLE_INSTALL = "sh {0}install.sh  -w {1} -p {2} &&source ~/.bashrc";
    /**
     * Minimalist cluster installation
     */
    String MINIMAL_LIST_CLUSTER_INSTALL = "sh {0}install.sh  -w {1} -p {2} --multinode &&source ~/.bashrc";
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

    /**
     * grant permission
     */
    String CHMOD_DATA_PATH = "chmod 700 -R {0}";
    /**
     * grant permission
     */
    String CHOWN = "chown {0} -R {1}";
    /**
     * grant permission
     */
    String CHOWN_USER_GROUP = "chown {0}:{1} -R {2}";
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
    String LITE_MASTER_INSTALL = "echo {0} | sh {1}/install.sh --mode primary -D {2} -R {3} -C \"replconninfo1=''localhost={4} localport={5} remotehost={6} remoteport={7}''\" -C port={8}  --start";
    /**
     * Lite install from node
     */
    String LITE_SLAVE_INSTALL = "echo {0} | sh {1}/install.sh --mode standby -D {2} -R {3} -C \"replconninfo1=''localhost={4} localport={5} remotehost={6} remoteport={7}''\" -C port={8}  --start";
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
    String HBA = "gs_guc set -D {0} -h \"host all gaussdb 0.0.0.0/0 sha256\" "
            + "&& gs_guc set -D {0} -h \"host all gaussdb ::/0 sha256\"";
    /**
     * Login to openGauss
     */
    String LOGIN = "gsql -d postgres -p {0}";
    /**
     * network usage
     */
    String NET = "cat /proc/net/dev";
    /**
     * Delete file
     */
    String DEL_FILE = "rm -rf {0}";
    /**
     * CentOs install dependencies
     */
    String INSTALL_DEPENDENCY = "yum install -y wget net-tools python3 bzip2 expect libaio-devel flex bison "
            + "ncurses-devel glibc-devel patch redhat-lsb readline-devel coreutils procps-ng openssh-clients unzip "
            + "lsof grep tar";
    /**
     * OPENEULER_X86 install dependencies
     */
    String INSTALL_DEPENDENCY_OPENEULER_X86 = "yum install -y wget net-tools python3 bzip2 expect libaio-devel flex "
            + "bison ncurses-devel glibc-devel patch readline-devel libnsl coreutils procps-ng openssh-clients unzip "
            + "lsof grep tar";
    /**
     * OPENEULER_ARCH64 install dependencies
     */
    String INSTALL_DEPENDENCY_OPENEULER_ARCH64 = "yum install -y wget net-tools python3 bzip2 expect libaio-devel "
            + "flex bison ncurses-devel glibc-devel patch readline-devel coreutils procps-ng openssh-clients unzip "
            + "lsof grep tar";

    /**
     * upadmin command for lun path
     */
    String UPADMIN = "upadmin show vlun | awk 'NR>2 {print $2, $4, $6, $3}'";

    /**
     * upadmin_plus command for lun path
     */
    String UPADMIN_PLUS = "upadmin_plus show vlun | awk 'NR>2 {print $2, $4, $6, $3}'";

    /**
     * lsscsi command for lun path
     */
    String LS_SCSI = "lsscsi -i -s | awk '{print $6, $7, $8}'";

    /**
     * command to find wwn by lsscsi
     */
    String FIND_DISK_BY_SCSI = "lsscsi -i -s | grep %s | awk '{print $6}'";

    /**
     * command to find wwn by upadmin
     */
    String FIND_DISK_BY_UPADMIN = "upadmin show vlun | grep %s | awk '{print $2}'";

    /**
     * command to find wwn by upadmin_plus
     */
    String FIND_DISK_BY_UPADMIN_PLUS = "upadmin_plus show vlun | grep %s | awk '{print $2}'";
    /**
     * Import cluster command
     */
    String CHECK_PERMISSION = "ls -l --time-style=long-iso -d {0}";
    /**
     * command to get openGauss OM package
     */
    String GET_OM_PACKAGE = "ls {0} | grep -i 'om.*\\.tar\\.gz'";

    /**
     * create dir and check write permission
     */
    String CREATE_DIR_AND_CHECK_WRITE_PERM = "if [ ! -d \"{0}\" ]; then mkdir -p \"{0}\"; fi\n"
            + "if [ -w \"{0}\" ]; then echo \"had write permission\"; else echo \"{1}\"; fi";

    /**
     * command to get user home
     */
    String GET_INSTALL_USER_HOME = "getent passwd {0} | cut -d: -f6";
}
