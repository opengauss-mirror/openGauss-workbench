#!/bin/bash
###
# Copyright (c) 2023 Huawei Technologies Co., Ltd.
# openGauss-workbench is licensed under Mulan PSL v2.
# You can use this software according to the terms and conditions of the Mulan PSL v2.
# You may obtain a copy of Mulan PSL v2 at:
#          http://license.coscl.org.cn/MulanPSL2
# THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
# EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
# MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
# See the Mulan PSL v2 for more details.
###

# Description: DataKit Installation Script
#
# Usage:
#   sh install.sh                      Normal user installation (weak isolation mode)
#   sudo sh install.sh                 Privileged installation (RECOMMENDED for production):
#                                      install dir owned by root (service account read-only),
#                                      data dir owned by the run account (defaults to the
#                                      invoking user before sudo)
#
#   NOTE: running the installer directly as root (root SSH login or 'su - root')
#         is NOT allowed. Always use 'sudo sh install.sh' from a regular account.
# Environment variables (all optional, defaults fit most cases):
#   DATAKIT_RUN_USER       dedicated run account for privileged install
#                          (default: the invoking user before sudo, i.e. ${SUDO_USER};
#                          fallback 'datakit' when absent, e.g. 'sudo su -')
#   DATAKIT_DATA_ROOT      data ROOT directory for privileged install
#                          (default: ${install_dir}/data).
#                          note: files/logs/ssl subdirectories are created UNDER this root,
#                          the actual upload directory is ${DATAKIT_DATA_ROOT}/files.
#                          production deployments should point this to a dedicated mount
#                          point (e.g. /srv/datakit) mounted with noexec,nosuid

if [ "$#" -gt 0 ]; then
    echo "Unknown option: $1"
    exit 1
fi

datakit_home_dir=$(cd "$(dirname "$0")" && pwd)

PRIVILEGED_INSTALL=0
if [ "$(id -u)" -eq 0 ]; then
    # euid 0 is expected here ONLY via sudo, which always sets SUDO_USER.
    # Root direct login (root SSH / 'su - root') has no SUDO_USER and is rejected:
    # audit trails require knowing which regular account performed the install.
    if [ -z "${SUDO_USER}" ] || [ "${SUDO_USER}" = "root" ]; then
        echo "Error: installing directly as root is not allowed."
        echo "Log in with a regular user and run: sudo sh install.sh"
        exit 1
    fi
    PRIVILEGED_INSTALL=1
    echo "Sudo detected: using privileged installation (standard mode)."
    echo "  data root : ${DATAKIT_DATA_ROOT:-${datakit_home_dir}/data}"
    echo "  run user  : ${DATAKIT_RUN_USER:-${SUDO_USER:-datakit}}"
fi

# Restrict permissions of all files and directories created by this script
umask 027

datakit_jar="openGauss-datakit-*.jar"

config_file_path="${datakit_home_dir}/config/application-temp.yml"

store_password=$(openssl rand -hex 16)

check_jar() {
    echo "Checking the DataKit jar..."
    local jar_file=$(ls -t ${datakit_jar} 2>/dev/null | head -n 1)

    echo "Check the DataKit jar is ${jar_file}"
    if [ -z "$jar_file" ] || [ ! -f "$jar_file" ]; then
        echo "Error: No matching JAR file found: '${datakit_jar}'. Please check the installation package"
        exit 1
    fi

    echo "Check the DataKit jar successfully"
}

create_required_dirs() {
    echo "Creating required directories..."

    if [ ! -w "${datakit_home_dir}" ]; then
        echo "Error: You do not have write permission on directory '${datakit_home_dir}'."
        exit 1
    fi

    # the installation package ships without these directories; create them
    # without deleting anything, so re-installs never touch existing data
    mkdir -p files ssl logs
    echo "Create required directories successfully"
}

check_java_version() {
    echo "Checking Java version..."
    local java_version
    local version_num

    if ! command -v java > /dev/null 2>&1; then
        echo "Error: java not found in PATH."
        echo "Hint: Java may be installed for your user but stripped from the sudo PATH."
        echo "      Run as root with the user PATH preserved: sudo env PATH=\"\$PATH\" sh install.sh"
        echo "      Or install Java 17+ system-wide (e.g. 'yum install java-17-openjdk')."
        exit 1
    fi

    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    if [ -z "${java_version}" ]; then
        echo "Error: unable to determine Java version from 'java -version' output."
        exit 1
    fi

    if [[ "$java_version" =~ ^1\. ]]; then
        version_num=$(echo "$java_version" | cut -d. -f2)
    else
        version_num=$(echo "$java_version" | cut -d. -f1)
    fi

    if ! [[ "$version_num" =~ ^[0-9]+$ ]]; then
        echo "Error: unable to parse Java version number from '${java_version}'."
        exit 1
    fi

    if [ "$version_num" -lt 17 ]; then
        echo "Error: Java 17 or later is required (found Java version: $java_version)"
        exit 1
    fi

    echo "Check Java version is ${java_version} "
}

generate_ssl_key() {
    local keystore_file="$1"
    echo "Generating the SSL key..."

    if ! command -v keytool > /dev/null 2>&1; then
        echo "Error: keytool not found in PATH (it is part of the JDK)."
        echo "Hint: a JRE-only installation does not ship keytool; install a full JDK 17+,"
        echo "      or run as root with the user PATH preserved: sudo env PATH=\"\$PATH\" sh install.sh"
        exit 1
    fi

    # pass the store password via environment variable (JDK 9+),
    # so it never shows up in the process list ('ps -ef')
    export KEYTOOL_STORE_PASS="${store_password}"
    keytool -genkey -noprompt -dname "CN=opengauss, OU=opengauss, O=opengauss, L=Beijing, S=Beijing, C=CN" \
    -alias opengauss -storetype PKCS12 -keyalg RSA -keysize 4096 \
    -keystore ${keystore_file} \
    -validity 365 -storepass:env KEYTOOL_STORE_PASS

    if [ $? -ne 0 ]; then
        echo "Error: Generated the SSL key failed"
        exit 1
    fi

    echo "Generate the SSL key successfully"
}

change_config_file() {
    local files_dir="$1" keystore_file="$2" logs_dir="$3"
    echo "Configuring the application-temp.yml..."

    sed -i "/key-store-password:/s#.*#    key-store-password: '${store_password}'#" ${config_file_path}
    sed -i "/defaultStoragePath:/s#.*#  defaultStoragePath: ${files_dir}#" ${config_file_path}
    sed -i "/key-store:/s#.*#    key-store: ${keystore_file}#" ${config_file_path}
    sed -i "/    path:/s#.*#    path: ${logs_dir}/#" ${config_file_path}
    sed -i "/  config: /s#.*#  config: ${datakit_home_dir}/config/log4j2.xml#" ${config_file_path}
    sed -i "/    report-dir:/s#.*#    report-dir: ${logs_dir}/handle#" ${config_file_path}

    echo "Configuring the application-temp.yml successfully"
}

main() {
    cd ${datakit_home_dir}
    check_jar
    create_required_dirs
    check_java_version
    generate_ssl_key "${datakit_home_dir}/ssl/keystore.p12"
    change_config_file "${datakit_home_dir}/files" "${datakit_home_dir}/ssl/keystore.p12" "${datakit_home_dir}/logs"
    tighten_permissions

    echo "Datakit has been installed successfully. "
    echo "Please go to the '${datakit_home_dir}' directory to manually start the DataKit server."
    echo "The command is as follows: "
    echo "    sh run.sh start --aes-key ******"
    echo "    sh run.sh status"
    echo "    sh run.sh stop"
    echo "    sh run.sh restart --aes-key ******"
}

# Restrict permissions of sensitive files after a normal installation:
# config and keystore are owner-only; ssl/logs/files directories are group-restricted
tighten_permissions() {
    echo "Tightening file permissions..."
    chmod 600 "${config_file_path}" "${datakit_home_dir}/ssl/keystore.p12"
    chmod 750 "${datakit_home_dir}/ssl" "${datakit_home_dir}/logs" "${datakit_home_dir}/files"
    echo "Tighten file permissions successfully"
}

# Privileged installation (standard mode, R11 defense in depth):
#   - install dir (jar/lib/config/scripts): owned by root, service account read-only
#   - data dir (files/logs/ssl): owned by the dedicated run account
# A file-write vulnerability in the service can therefore no longer overwrite
# the application itself to persist a backdoor.
root_install() {
    if [ "$(id -u)" -ne 0 ]; then
        echo "Error: privileged installation must be executed as root (use sudo)"
        exit 1
    fi

    local run_user="${DATAKIT_RUN_USER:-${SUDO_USER:-datakit}}"
    local data_root="${DATAKIT_DATA_ROOT:-${datakit_home_dir}/data}"

    # dedicated run account
    if ! id -u "${run_user}" >/dev/null 2>&1; then
        echo "Creating dedicated run account '${run_user}'..."
        useradd -M -s /bin/bash "${run_user}"
    fi

    cd "${datakit_home_dir}"
    check_jar
    check_java_version

    # data directories live on the (ideally dedicated) data mount point;
    # created without deleting anything, so re-installs never touch existing data
    mkdir -p "${data_root}/files" "${data_root}/logs" "${data_root}/ssl"

    generate_ssl_key "${data_root}/ssl/keystore.p12"

    change_config_file "${data_root}/files" "${data_root}/ssl/keystore.p12" "${data_root}/logs"

    # per-installation runtime environment consumed by run.sh
    # DATAKIT_USER_HOME redirects the JVM 'user.home' to the data root, so the
    # AES keystore (~/.secure/datakit.jceks) lands in run-account-owned storage
    # even when the account home dir does not exist (useradd -M)
    cat > "${datakit_home_dir}/config/run-env.sh" <<EOF
export DATAKIT_RUN_USER="${run_user}"
export DATAKIT_USER_HOME="${data_root}"
export DATAKIT_LOG_DIR="${data_root}/logs"
export DATAKIT_PID_FILE="${data_root}/datakit.pid"
EOF

    echo "Setting install directory ownership (root:root, service account read-only)..."
    chown -R root:root "${datakit_home_dir}"
    find "${datakit_home_dir}" -name "*.jar" -exec chmod 644 {} +
    chmod 755 "${datakit_home_dir}" "${datakit_home_dir}/run.sh" "${datakit_home_dir}/install.sh"
    # config dir: traverse restricted to the run account group
    chown root:"${run_user}" "${datakit_home_dir}/config"
    chmod 750 "${datakit_home_dir}/config"
    # application-temp.yml and run-env.sh contain secrets: root-owned, group-readable by the run account only
    chown root:"${run_user}" "${config_file_path}" "${datakit_home_dir}/config/run-env.sh"
    chmod 640 "${config_file_path}" "${datakit_home_dir}/config/run-env.sh"
    chmod 644 "${datakit_home_dir}/config/log4j2.xml"

    echo "Setting data directory ownership (${run_user}:${run_user})..."
    chown -R "${run_user}:${run_user}" "${data_root}"
    chmod 750 "${data_root}" "${data_root}/files" "${data_root}/logs"
    chmod 700 "${data_root}/ssl"
    chmod 600 "${data_root}/ssl/keystore.p12"

    echo "Privileged installation completed."
    echo ""
    echo "Security notes:"
    echo "  1. Mount the data partition with 'noexec,nosuid' options (recommended), e.g. in /etc/fstab:"
    echo "       <device>  ${data_root}  <fstype>  defaults,noexec,nosuid  0 0"
    echo "  2. Start the service with:"
    echo "       su ${run_user} -c 'sh ${datakit_home_dir}/run.sh start --aes-key ******'"
    echo "  3. run.sh enforces the dedicated account via DATAKIT_RUN_USER (see config/run-env.sh)."
}

if [ "${PRIVILEGED_INSTALL}" -eq 1 ]; then
    root_install
else
    main
fi
