#!/bin/bash

# Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
# Description: DataKit Uninstall Script, support uninstall installed tools.

# ============================================
# Module: Public Functions
# ============================================

function echo_info() {
    echo -e "\033[32m[INFO]\033[0m $1"
}

function echo_error() {
    echo -e "\033[31m[ERROR]\033[0m $1" >&2
}

function echo_warn() {
    echo -e "\033[33m[WARN]\033[0m $1"
}

function is_file_exists() {
    local file_path="${1}"
    
    if [[ ! -f "$file_path" ]]; then
        return 1
    fi

    return 0
}

function can_file_read() {
    local file_path="${1}"
    
    if [[ ! -r "$file_path" ]]; then
        return 1
    fi

    return 0
}

# ============================================
# Module: Datakit
# ============================================

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" &> /dev/null && pwd)
DATAKIT_JAR=""

function check_datakit_jar() {
    echo_info "Check Datakit jar file..."

    local jar_pattern="openGauss-datakit-*.jar"
    DATAKIT_JAR=$(ls -t ${SCRIPT_DIR}/${jar_pattern} 2>/dev/null | head -n 1)
    
    if [ -z "${DATAKIT_JAR}" ] || [ ! -f "${DATAKIT_JAR}" ]; then
        echo_error "Datakit jar file not found. Cannot continue uninstallation. Please check the installation directory."
        exit 1
    fi

    echo_info "Datakit jar file: ${DATAKIT_JAR}"
}

function stop_datakit_process() {
    echo_info "Stop Datakit process..."
    local datakit_pid=$(ps -ef | grep "${DATAKIT_JAR}" | grep -v grep | awk '{print $2}')

    if [ -z "${datakit_pid}" ]; then
        echo_warn "Datakit process not found."
    else
        echo_info "Stop Datakit process with PID: ${datakit_pid}..."
        kill -9 "${datakit_pid}" >/dev/null 2>&1
        echo_info "Datakit process stopped."
    fi
}

function clear_datakit_install_dir() {
    echo_info "Clear Datakit install directory..."
    echo_info "Datakit install directory: ${SCRIPT_DIR}"

    cd "${SCRIPT_DIR}"
    if [[ $? -ne 0 ]]; then
        echo_error "Failed to switch to Datakit install directory. Cannot continue uninstallation."
        exit 1
    fi

    rm -rf "${SCRIPT_DIR}"
    if [[ $? -ne 0 ]]; then
        echo_error "Failed to clear Datakit install directory."
        exit 1
    else
        echo_info "Datakit install directory cleared."
    fi
}

function datakit_uninstall() {
    echo_info "Start Datakit uninstall"
    check_datakit_jar
    stop_datakit_process
    clear_datakit_install_dir
    echo_info "Datakit uninstalled successfully."
}

# ============================================
# Module: Migration Portal
# ============================================

MIGRATION_PORTAL_TOOL_NAME="migration-portal"

declare -A MIGRATION_PORTAL_ID_MAP=()
declare -A MIGRATION_PORTAL_IP_MAP=()
declare -A MIGRATION_PORTAL_PORT_MAP=()
declare -A MIGRATION_PORTAL_USER_MAP=()
declare -A MIGRATION_PORTAL_INSTALL_DIR_MAP=()
declare -A MIGRATION_PORTAL_PORTAL_TYPE_MAP=()

function load_migration_portal_install_info() {
    local csv_file="${1}"
    echo_info "Start loading $MIGRATION_PORTAL_TOOL_NAME install info..."
    
    if ! is_file_exists "$csv_file"; then
        echo_warn "$MIGRATION_PORTAL_TOOL_NAME install info file not found."
        return 0
    fi
    
    if ! can_file_read "$csv_file"; then
        echo_error "$MIGRATION_PORTAL_TOOL_NAME install info file is not readable. File path: $csv_file. Skip $MIGRATION_PORTAL_TOOL_NAME uninstall."
        return 1
    fi
    
    MIGRATION_PORTAL_ID_MAP=()
    MIGRATION_PORTAL_IP_MAP=()
    MIGRATION_PORTAL_PORT_MAP=()
    MIGRATION_PORTAL_USER_MAP=()
    MIGRATION_PORTAL_INSTALL_DIR_MAP=()
    MIGRATION_PORTAL_PORTAL_TYPE_MAP=()

    local lines
    mapfile -t lines < "$csv_file"
    for ((i=1; i<${#lines[@]}; i++)); do
        IFS=',' read -r id ip port user install_dir portal_type <<< "${lines[$i]}"
        
        if [ -z "$id" ] || [ -z "$ip" ] || [ -z "$port" ] || [ -z "$user" ] || [ -z "$install_dir" ] || [ -z "$portal_type" ]; then
            echo_warn "Storage file for installed $MIGRATION_PORTAL_TOOL_NAME is not formatted correctly, line $i is skipped." 
            continue
        fi
        
        MIGRATION_PORTAL_ID_MAP["$id"]="$id"
        MIGRATION_PORTAL_IP_MAP["$id"]="$ip"
        MIGRATION_PORTAL_PORT_MAP["$id"]="$port"
        MIGRATION_PORTAL_USER_MAP["$id"]="$user"
        MIGRATION_PORTAL_INSTALL_DIR_MAP["$id"]="$install_dir"
        MIGRATION_PORTAL_PORTAL_TYPE_MAP["$id"]="$portal_type"

        echo_info "Loaded $MIGRATION_PORTAL_TOOL_NAME install info, IP: $ip, Port: $port, User: $user, InstallDir: $install_dir"
    done
    
    echo_info "Loaded $MIGRATION_PORTAL_TOOL_NAME install info successfully, total ${#MIGRATION_PORTAL_ID_MAP[@]} records."
    return 0
}

function uninstall_migration_portal() {
    echo_info "Start uninstall $MIGRATION_PORTAL_TOOL_NAME..."

    local -A uninstall_failed_ids=()
    local -A uninstall_skipped_ids=()

    for id in "${!MIGRATION_PORTAL_ID_MAP[@]}"; do
        local ip="${MIGRATION_PORTAL_IP_MAP[$id]}"
        local port="${MIGRATION_PORTAL_PORT_MAP[$id]}"
        local user="${MIGRATION_PORTAL_USER_MAP[$id]}"
        local install_dir="${MIGRATION_PORTAL_INSTALL_DIR_MAP[$id]}"
        local portal_type="${MIGRATION_PORTAL_PORTAL_TYPE_MAP[$id]}"
        local host_key="${ip},${port},${user}"
        local host_info="$ip:$port($user)"

        if [[ -n "${SKIP_HOST_MAP[$host_key]}" ]]; then
            echo_warn "Host $host_info is a remote machine, password not configured. Skip this $MIGRATION_PORTAL_TOOL_NAME uninstall."
            uninstall_skipped_ids["$id"]="$id"
            continue
        fi

        # local host migration-portal tool uninstall
        if [[ -n "${LOCAL_HOST_MAP[$host_key]}" ]]; then
            echo_info "Host $host_info is a local machine, start local uninstalling $MIGRATION_PORTAL_TOOL_NAME..."

            if [[ "$portal_type" == "MYSQL_ONLY" ]]; then
                local uninstall_script="$install_dir/portal/uninstall.sh"
                uninstall_script=$(readlink -f "$uninstall_script")

                if [[ ! -f "$uninstall_script" ]]; then
                    echo_error "Host $host_info $MIGRATION_PORTAL_TOOL_NAME uninstall script not found. Cannot continue uninstall."
                    uninstall_failed_ids["$id"]="$id"
                    continue
                fi

                echo y | sh "$uninstall_script" --remove-package
                
                if [[ $? -ne 0 ]]; then
                    echo_error "Host $host_info $MIGRATION_PORTAL_TOOL_NAME uninstall failed."
                    uninstall_failed_ids["$id"]="$id"
                else
                    echo_info "Host $host_info $MIGRATION_PORTAL_TOOL_NAME uninstalled successfully."
                fi
            elif [[ "$portal_type" == "MULTI_DB" ]]; then
                local uninstall_script="$install_dir/portal/bin/uninstall"
                uninstall_script=$(readlink -f "$uninstall_script")

                if [[ ! -f "$uninstall_script" ]]; then
                    echo_error "Host $host_info $MIGRATION_PORTAL_TOOL_NAME uninstall script not found. Cannot continue uninstall."
                    uninstall_failed_ids["$id"]="$id"
                    continue
                fi

                echo y | "$uninstall_script" --remove-package
                
                if [[ $? -ne 0 ]]; then
                    echo_error "Host $host_info $MIGRATION_PORTAL_TOOL_NAME uninstall failed."
                    uninstall_failed_ids["$id"]="$id"
                else
                    echo_info "Host $host_info $MIGRATION_PORTAL_TOOL_NAME uninstalled successfully."
                fi
            else
                echo_error "Host $host_info $MIGRATION_PORTAL_TOOL_NAME type not recognized, type: $portal_type. Cannot continue uninstall."
                uninstall_failed_ids["$id"]="$id"
            fi
            continue
        fi

        # remote host migration-portal tool uninstall
        echo_info "Host $host_info is a remote machine, start remote uninstalling $MIGRATION_PORTAL_TOOL_NAME..."
        SSHPASS="${REMOTE_HOST_PASSWORD_MAP[$host_key]}" sshpass -e ssh -p "$port" "$user"@"$ip" -o ConnectTimeout=5 -o StrictHostKeyChecking=no -q -T <<EOF
cd "$install_dir"

if [[ "$portal_type" == "MYSQL_ONLY" ]]; then
    uninstall_script="./portal/uninstall.sh"

    if [[ ! -f "\$uninstall_script" ]]; then
        echo -e "\033[31m[ERROR]\033[0m Host $host_info $MIGRATION_PORTAL_TOOL_NAME uninstall script not found. Cannot continue uninstall." >&2
        exit 1
    fi

    echo y | sh "\$uninstall_script" --remove-package
elif [[ "$portal_type" == "MULTI_DB" ]]; then
    uninstall_script="./portal/bin/uninstall"

    if [[ ! -f "\$uninstall_script" ]]; then
        echo -e "\033[31m[ERROR]\033[0m Host $host_info $MIGRATION_PORTAL_TOOL_NAME uninstall script not found. Cannot continue uninstall." >&2
        exit 1
    fi
    
    echo y | "\$uninstall_script" --remove-package
else
    echo -e "\033[31m[ERROR]\033[0m Host $host_info $MIGRATION_PORTAL_TOOL_NAME type not recognized, type: $portal_type. Cannot continue uninstall." >&2
    exit 1
fi
EOF
        if [[ $? -ne 0 ]]; then
            echo_error "Host $host_info $MIGRATION_PORTAL_TOOL_NAME uninstall failed."
            uninstall_failed_ids["$id"]="$id"
        else
            echo_info "Host $host_info $MIGRATION_PORTAL_TOOL_NAME uninstalled successfully."
        fi
    done
    
    echo_info "All $MIGRATION_PORTAL_TOOL_NAME uninstalled."
    if [[ ${#uninstall_skipped_ids[@]} -ne 0 ]]; then
        echo_info "Skipped uninstalling $MIGRATION_PORTAL_TOOL_NAME on the following hosts: "
        for id in "${!uninstall_skipped_ids[@]}"; do
            echo_info "IP: ${MIGRATION_PORTAL_IP_MAP[$id]}, Port: ${MIGRATION_PORTAL_PORT_MAP[$id]}, User: ${MIGRATION_PORTAL_USER_MAP[$id]}, InstallDir: ${MIGRATION_PORTAL_INSTALL_DIR_MAP[$id]}"
        done
    else
        echo_info "No skipped uninstalling $MIGRATION_PORTAL_TOOL_NAME."
    fi
    
    if [[ ${#uninstall_failed_ids[@]} -ne 0 ]]; then
        echo_info "Failed uninstalling $MIGRATION_PORTAL_TOOL_NAME on the following hosts: "
        for id in "${!uninstall_failed_ids[@]}"; do
            echo_info "IP: ${MIGRATION_PORTAL_IP_MAP[$id]}, Port: ${MIGRATION_PORTAL_PORT_MAP[$id]}, User: ${MIGRATION_PORTAL_USER_MAP[$id]}, InstallDir: ${MIGRATION_PORTAL_INSTALL_DIR_MAP[$id]}"
        done
    else
        echo_info "No failed uninstalling $MIGRATION_PORTAL_TOOL_NAME."
    fi
}

# ============================================
# Module: Prometheus
# ============================================

PROMETHEUS_TOOL_NAME="prometheus"

declare -A PROMETHEUS_ID_MAP=()
declare -A PROMETHEUS_IP_MAP=()
declare -A PROMETHEUS_PORT_MAP=()
declare -A PROMETHEUS_USER_MAP=()
declare -A PROMETHEUS_INSTALL_DIR_MAP=()
declare -A PROMETHEUS_SERVER_PORT_MAP=()

function load_prometheus_install_info() {
    local csv_file="${1}"
    echo_info "Start loading $PROMETHEUS_TOOL_NAME install info..."

    if ! is_file_exists "$csv_file"; then
        echo_warn "$PROMETHEUS_TOOL_NAME install info file not found."
        return 0
    fi
    
    if ! can_file_read "$csv_file"; then
        echo_error "$PROMETHEUS_TOOL_NAME install info file is not readable. File path: $csv_file. Skip $PROMETHEUS_TOOL_NAME uninstall."
        return 1
    fi
    
    PROMETHEUS_ID_MAP=()
    PROMETHEUS_IP_MAP=()
    PROMETHEUS_PORT_MAP=()
    PROMETHEUS_USER_MAP=()
    PROMETHEUS_INSTALL_DIR_MAP=()
    PROMETHEUS_SERVER_PORT_MAP=()

    local lines
    mapfile -t lines < "$csv_file"
    for ((i=1; i<${#lines[@]}; i++)); do
        IFS=',' read -r id ip port user install_dir server_port <<< "${lines[$i]}"
        
        if [ -z "$id" ] || [ -z "$ip" ] || [ -z "$port" ] || [ -z "$user" ] || [ -z "$install_dir" ] || [ -z "$server_port" ]; then
            echo_warn "Storage file for installed $PROMETHEUS_TOOL_NAME is not formatted correctly, line $i is skipped." 
            continue
        fi
        
        PROMETHEUS_ID_MAP["$id"]="$id"
        PROMETHEUS_IP_MAP["$id"]="$ip"
        PROMETHEUS_PORT_MAP["$id"]="$port"
        PROMETHEUS_USER_MAP["$id"]="$user"
        PROMETHEUS_INSTALL_DIR_MAP["$id"]="$install_dir"
        PROMETHEUS_SERVER_PORT_MAP["$id"]="$server_port"
        
        echo_info "Loaded $PROMETHEUS_TOOL_NAME install info, IP: $ip, Port: $port, User: $user, InstallDir: $install_dir, ServerPort: $server_port"
    done
    
    echo_info "Loaded $PROMETHEUS_TOOL_NAME install info successfully, total ${#PROMETHEUS_ID_MAP[@]} records."
    return 0
}

function uninstall_prometheus() {
    echo_info "Start uninstall $PROMETHEUS_TOOL_NAME..."

    local -A uninstall_failed_ids=()
    local -A uninstall_skipped_ids=()

    for id in "${!PROMETHEUS_ID_MAP[@]}"; do
        local ip="${PROMETHEUS_IP_MAP[$id]}"
        local port="${PROMETHEUS_PORT_MAP[$id]}"
        local user="${PROMETHEUS_USER_MAP[$id]}"
        local install_dir="${PROMETHEUS_INSTALL_DIR_MAP[$id]}"
        local server_port="${PROMETHEUS_SERVER_PORT_MAP[$id]}"
        local host_key="${ip},${port},${user}"
        local host_info="$ip:$port($user)"
        
        if [[ -n "${SKIP_HOST_MAP[$host_key]}" ]]; then
            echo_warn "Host $host_info is a remote machine, password not configured. Skip this $PROMETHEUS_TOOL_NAME uninstall."
            uninstall_skipped_ids["$id"]="$id"
            continue
        fi
        
        # local host prometheus tool uninstall
        if [[ -n "${LOCAL_HOST_MAP[$host_key]}" ]]; then
            echo_info "Host $host_info is a local machine, start local uninstalling $PROMETHEUS_TOOL_NAME..."
            
            echo_info "Stop $PROMETHEUS_TOOL_NAME process on host $host_info..."
            local pid=$(ps ux | grep -- "[\.]/prometheus --config\.file=prometheus\.yml --web\.enable-lifecycle --web\.listen-address=:$server_port --storage\.tsdb\.retention\.time=" | awk '{print $2}')
            
            if [[ -n "$pid" ]]; then
                kill -9 "$pid" 2>/dev/null
                echo_info "Host $host_info $PROMETHEUS_TOOL_NAME process $pid stopped."
            else
                echo_info "Host $host_info $PROMETHEUS_TOOL_NAME not running."
            fi
            
            echo_info "Cleaning $PROMETHEUS_TOOL_NAME install directory on host $host_info..."
            rm -rf "$install_dir"
            
            if [[ $? -ne 0 ]]; then
                echo_error "Host $host_info $PROMETHEUS_TOOL_NAME install directory cleanup failed. Uninstall failed."
                uninstall_failed_ids["$id"]="$id"
            else
                echo_info "Host $host_info $PROMETHEUS_TOOL_NAME install directory cleaned successfully. Uninstall succeeded."
            fi
            continue
        fi
        
        # remote host prometheus tool uninstall
        echo_info "Host $host_info is a remote machine, start remote uninstalling $PROMETHEUS_TOOL_NAME..."
        SSHPASS="${REMOTE_HOST_PASSWORD_MAP[$host_key]}" sshpass -e ssh -p "$port" "$user"@"$ip" -o ConnectTimeout=5 -o StrictHostKeyChecking=no -q -T <<EOF
echo -e "\033[32m[INFO]\033[0m Stop $PROMETHEUS_TOOL_NAME process on host $host_info..."
pid=\$(ps ux | grep -- "[\.]/prometheus --config\.file=prometheus\.yml --web\.enable-lifecycle --web\.listen-address=:$server_port --storage\.tsdb\.retention\.time=" | awk '{print \$2}')

if [[ -n "\$pid" ]]; then
    kill -9 "\$pid"
    echo -e "\033[32m[INFO]\033[0m Host $host_info $PROMETHEUS_TOOL_NAME process \$pid stopped."
else
    echo -e "\033[33m[WARN]\033[0m Host $host_info $PROMETHEUS_TOOL_NAME not running."
fi

echo -e "\033[32m[INFO]\033[0m Cleaning $PROMETHEUS_TOOL_NAME install directory on host $host_info..."
rm -rf "$(dirname "$install_dir")"

if [[ \$? -ne 0 ]]; then
    echo -e "\033[31m[ERROR]\033[0m Host $host_info $PROMETHEUS_TOOL_NAME install directory cleanup failed."
    exit 1
else
    echo -e "\033[32m[INFO]\033[0m Host $host_info $PROMETHEUS_TOOL_NAME install directory cleaned successfully."
fi
EOF
        if [[ $? -ne 0 ]]; then
            echo_error "Host $host_info $PROMETHEUS_TOOL_NAME uninstall failed."
            uninstall_failed_ids["$id"]="$id"
        else
            echo_info "Host $host_info $PROMETHEUS_TOOL_NAME uninstalled successfully."
        fi
    done
    
    echo_info "All $PROMETHEUS_TOOL_NAME uninstalled."
    if [[ ${#uninstall_skipped_ids[@]} -ne 0 ]]; then
        echo_info "Skipped uninstalling $PROMETHEUS_TOOL_NAME on the following hosts: "
        for id in "${!uninstall_skipped_ids[@]}"; do
            echo_info "IP: ${PROMETHEUS_IP_MAP[$id]}, Port: ${PROMETHEUS_PORT_MAP[$id]}, User: ${PROMETHEUS_USER_MAP[$id]}, InstallDir: ${PROMETHEUS_INSTALL_DIR_MAP[$id]}, ServerPort: ${PROMETHEUS_SERVER_PORT_MAP[$id]}"
        done
    else
        echo_info "No skipped uninstalling $PROMETHEUS_TOOL_NAME."
    fi
    
    if [[ ${#uninstall_failed_ids[@]} -ne 0 ]]; then
        echo_info "Failed uninstalling $PROMETHEUS_TOOL_NAME on the following hosts: "
        for id in "${!uninstall_failed_ids[@]}"; do
            echo_info "IP: ${PROMETHEUS_IP_MAP[$id]}, Port: ${PROMETHEUS_PORT_MAP[$id]}, User: ${PROMETHEUS_USER_MAP[$id]}, InstallDir: ${PROMETHEUS_INSTALL_DIR_MAP[$id]}, ServerPort: ${PROMETHEUS_SERVER_PORT_MAP[$id]}"
        done
    else
        echo_info "No failed uninstalling $PROMETHEUS_TOOL_NAME."
    fi
}

# ============================================
# Module: Instance-exporter
# ============================================

INSTANCE_EXPORTER_TOOL_NAME="instance-exporter"

declare -A INSTANCE_EXPORTER_ID_MAP=()
declare -A INSTANCE_EXPORTER_IP_MAP=()
declare -A INSTANCE_EXPORTER_PORT_MAP=()
declare -A INSTANCE_EXPORTER_USER_MAP=()
declare -A INSTANCE_EXPORTER_INSTALL_DIR_MAP=()
declare -A INSTANCE_EXPORTER_SERVER_PORT_MAP=()

function load_instance_exporter_install_info() {
    local csv_file="${1}"
    echo_info "Start loading $INSTANCE_EXPORTER_TOOL_NAME install info..."

    if ! is_file_exists "$csv_file"; then
        echo_warn "$INSTANCE_EXPORTER_TOOL_NAME install info file not found."
        return 0
    fi
    
    if ! can_file_read "$csv_file"; then
        echo_error "$INSTANCE_EXPORTER_TOOL_NAME install info file is not readable. File path: $csv_file. Skip $INSTANCE_EXPORTER_TOOL_NAME uninstall."
        return 1
    fi
    
    INSTANCE_EXPORTER_ID_MAP=()
    INSTANCE_EXPORTER_IP_MAP=()
    INSTANCE_EXPORTER_PORT_MAP=()
    INSTANCE_EXPORTER_USER_MAP=()
    INSTANCE_EXPORTER_INSTALL_DIR_MAP=()
    INSTANCE_EXPORTER_SERVER_PORT_MAP=()

    local lines
    mapfile -t lines < "$csv_file"
    for ((i=1; i<${#lines[@]}; i++)); do
        IFS=',' read -r id ip port user install_dir server_port <<< "${lines[$i]}"
        
        if [ -z "$id" ] || [ -z "$ip" ] || [ -z "$port" ] || [ -z "$user" ] || [ -z "$install_dir" ] || [ -z "$server_port" ]; then
            echo_warn "Storage file for installed $INSTANCE_EXPORTER_TOOL_NAME is not formatted correctly, line $i is skipped." 
            continue
        fi
        
        INSTANCE_EXPORTER_ID_MAP["$id"]="$id"
        INSTANCE_EXPORTER_IP_MAP["$id"]="$ip"
        INSTANCE_EXPORTER_PORT_MAP["$id"]="$port"
        INSTANCE_EXPORTER_USER_MAP["$id"]="$user"
        INSTANCE_EXPORTER_INSTALL_DIR_MAP["$id"]="$install_dir"
        INSTANCE_EXPORTER_SERVER_PORT_MAP["$id"]="$server_port"
        
        echo_info "Loaded $INSTANCE_EXPORTER_TOOL_NAME install info, IP: $ip, Port: $port, User: $user, InstallDir: $install_dir, ServerPort: $server_port"
    done
    
    echo_info "Loaded $INSTANCE_EXPORTER_TOOL_NAME install info successfully, total ${#INSTANCE_EXPORTER_ID_MAP[@]} records."
    return 0
}

function uninstall_instance_exporter() {
    echo_info "Start uninstall $INSTANCE_EXPORTER_TOOL_NAME..."

    local -A uninstall_failed_ids=()
    local -A uninstall_skipped_ids=()

    for id in "${!INSTANCE_EXPORTER_ID_MAP[@]}"; do
        local ip="${INSTANCE_EXPORTER_IP_MAP[$id]}"
        local port="${INSTANCE_EXPORTER_PORT_MAP[$id]}"
        local user="${INSTANCE_EXPORTER_USER_MAP[$id]}"
        local install_dir="${INSTANCE_EXPORTER_INSTALL_DIR_MAP[$id]}"
        local server_port="${INSTANCE_EXPORTER_SERVER_PORT_MAP[$id]}"
        local host_key="${ip},${port},${user}"
        local host_info="$ip:$port($user)"
        
        if [[ -n "${SKIP_HOST_MAP[$host_key]}" ]]; then
            echo_warn "Host $host_info is a remote machine, password not configured. Skip this $INSTANCE_EXPORTER_TOOL_NAME uninstall."
            uninstall_skipped_ids["$id"]="$id"
            continue
        fi
        
        # local host instance-exporter tool uninstall
        if [[ -n "${LOCAL_HOST_MAP[$host_key]}" ]]; then
            echo_info "Host $host_info is a local machine, start local uninstalling $INSTANCE_EXPORTER_TOOL_NAME..."
            
            echo_info "Stop $INSTANCE_EXPORTER_TOOL_NAME process on host $host_info..."
            local pid=$(ps ux | grep -- "[j]ava -jar instance-exporter\.jar --server\.port=$server_port" | awk '{print $2}')
            
            if [[ -n "$pid" ]]; then
                kill -9 "$pid"
                echo_info "Host $host_info $INSTANCE_EXPORTER_TOOL_NAME process $pid stopped."
            else
                echo_info "Host $host_info $INSTANCE_EXPORTER_TOOL_NAME not running."
            fi
            
            echo_info "Cleaning $INSTANCE_EXPORTER_TOOL_NAME install directory on host $host_info..."
            rm -rf "$install_dir"
            
            if [[ $? -ne 0 ]]; then
                echo_error "Host $host_info $INSTANCE_EXPORTER_TOOL_NAME install directory cleanup failed. Uninstall failed."
                uninstall_failed_ids["$id"]="$id"
            else
                echo_info "Host $host_info $INSTANCE_EXPORTER_TOOL_NAME install directory cleaned successfully. Uninstall succeeded."
            fi
            continue
        fi
        
        # remote host instance-exporter tool uninstall
        echo_info "Host $host_info is a remote machine, start remote uninstalling $INSTANCE_EXPORTER_TOOL_NAME..."
        SSHPASS="${REMOTE_HOST_PASSWORD_MAP[$host_key]}" sshpass -e ssh -p "$port" "$user"@"$ip" -o ConnectTimeout=5 -o StrictHostKeyChecking=no -q -T <<EOF
echo -e "\033[32m[INFO]\033[0m Stop $INSTANCE_EXPORTER_TOOL_NAME process on host $host_info..."
pid=\$(ps ux | grep -- "[j]ava -jar instance-exporter\.jar --server\.port=$server_port" | awk '{print \$2}')

if [[ -n "\$pid" ]]; then
    kill -9 "\$pid"
    echo -e "\033[32m[INFO]\033[0m Host $host_info $INSTANCE_EXPORTER_TOOL_NAME process \$pid stopped."
else
    echo -e "\033[33m[WARN]\033[0m Host $host_info $INSTANCE_EXPORTER_TOOL_NAME not running."
fi

echo -e "\033[32m[INFO]\033[0m Cleaning $INSTANCE_EXPORTER_TOOL_NAME install directory on host $host_info..."
rm -rf "$install_dir"

if [[ \$? -ne 0 ]]; then
    echo -e "\033[31m[ERROR]\033[0m Host $host_info $INSTANCE_EXPORTER_TOOL_NAME install directory cleanup failed."
    exit 1
else
    echo -e "\033[32m[INFO]\033[0m Host $host_info $INSTANCE_EXPORTER_TOOL_NAME install directory cleaned successfully."
fi
EOF
        if [[ $? -ne 0 ]]; then
            echo_error "Host $host_info $INSTANCE_EXPORTER_TOOL_NAME uninstall failed."
            uninstall_failed_ids["$id"]="$id"
        else
            echo_info "Host $host_info $INSTANCE_EXPORTER_TOOL_NAME uninstalled successfully."
        fi
    done

    echo_info "All $INSTANCE_EXPORTER_TOOL_NAME uninstalled."
    if [[ ${#uninstall_skipped_ids[@]} -ne 0 ]]; then
        echo_info "Skipped uninstalling $INSTANCE_EXPORTER_TOOL_NAME on the following hosts: "
        for id in "${!uninstall_skipped_ids[@]}"; do
            echo_info "IP: ${INSTANCE_EXPORTER_IP_MAP[$id]}, Port: ${INSTANCE_EXPORTER_PORT_MAP[$id]}, User: ${INSTANCE_EXPORTER_USER_MAP[$id]}, InstallDir: ${INSTANCE_EXPORTER_INSTALL_DIR_MAP[$id]}, ServerPort: ${INSTANCE_EXPORTER_SERVER_PORT_MAP[$id]}"
        done
    else
        echo_info "No skipped uninstalling $INSTANCE_EXPORTER_TOOL_NAME."
    fi
    
    if [[ ${#uninstall_failed_ids[@]} -ne 0 ]]; then
        echo_info "Failed uninstalling $INSTANCE_EXPORTER_TOOL_NAME on the following hosts: "
        for id in "${!uninstall_failed_ids[@]}"; do
            echo_info "IP: ${INSTANCE_EXPORTER_IP_MAP[$id]}, Port: ${INSTANCE_EXPORTER_PORT_MAP[$id]}, User: ${INSTANCE_EXPORTER_USER_MAP[$id]}, InstallDir: ${INSTANCE_EXPORTER_INSTALL_DIR_MAP[$id]}, ServerPort: ${INSTANCE_EXPORTER_SERVER_PORT_MAP[$id]}"
        done
    else
        echo_info "No failed uninstalling $INSTANCE_EXPORTER_TOOL_NAME."
    fi
}

# ============================================
# Module: Third-party Tools
# ============================================

declare -A THIRD_PARTY_TOOLS_MAP=()

function load_third_party_tools() {
    echo_info "Start loading installed tools..."
    
    local third_party_tools_csv="data/third-party-tools/third-party-tools.csv"
    if ! is_file_exists "$third_party_tools_csv"; then
        echo_warn "No installed tools found."
        return 0
    fi
    
    if ! can_file_read "$third_party_tools_csv"; then
        echo_error "Storage file for installed tools is not readable, file path: $third_party_tools_csv. Cannot load installed tools."
        return 1
    fi
    
    THIRD_PARTY_TOOLS_MAP=()
    
    local lines
    mapfile -t lines < "$third_party_tools_csv"
    for ((i=1; i<${#lines[@]}; i++)); do
        IFS=',' read -r tool_name install_info_csv_file_path <<< "${lines[$i]}"
        
        if [ -z "$tool_name" ] || [ -z "$install_info_csv_file_path" ]; then
            echo_warn "Storage file for installed tools is not formatted correctly, line $i is skipped." 
            continue
        fi
        
        THIRD_PARTY_TOOLS_MAP["$tool_name"]="$install_info_csv_file_path"
        echo_info "Loaded tool: $tool_name, install info file: $install_info_csv_file_path"
    done
    
    echo_info "Loaded ${#THIRD_PARTY_TOOLS_MAP[@]} tools."
    return 0
}

function load_all_third_party_tools_install_info() {
    echo_info "Start loading installed tools install info..."
    
    if [ ${#THIRD_PARTY_TOOLS_MAP[@]} -eq 0 ]; then
        return 1
    fi

    local migration_portal_install_info_csv="${THIRD_PARTY_TOOLS_MAP[$MIGRATION_PORTAL_TOOL_NAME]}"
    if [[ -n "$migration_portal_install_info_csv" ]]; then
        load_migration_portal_install_info "$migration_portal_install_info_csv"
    fi
    
    local prometheus_install_info_csv="${THIRD_PARTY_TOOLS_MAP[$PROMETHEUS_TOOL_NAME]}"
    if [[ -n "$prometheus_install_info_csv" ]]; then
        load_prometheus_install_info "$prometheus_install_info_csv"
    fi
    
    local instance_exporter_install_info_csv="${THIRD_PARTY_TOOLS_MAP[$INSTANCE_EXPORTER_TOOL_NAME]}"
    if [[ -n "$instance_exporter_install_info_csv" ]]; then
        load_instance_exporter_install_info "$instance_exporter_install_info_csv"
    fi
    
    echo_info "All tools install info loaded."
    return 0
}

function uninstall_third_party_tools() {
    load_third_party_tools || return 1
    load_all_third_party_tools_install_info || return 1

    unite_all_unique_ip_port_user
    generate_local_host_map
    prepare_host_credentials

    if [[ -n "${THIRD_PARTY_TOOLS_MAP[$MIGRATION_PORTAL_TOOL_NAME]}" ]]; then
        uninstall_migration_portal
    fi
    
    if [[ -n "${THIRD_PARTY_TOOLS_MAP[$PROMETHEUS_TOOL_NAME]}" ]]; then
        uninstall_prometheus
    fi
    
    if [[ -n "${THIRD_PARTY_TOOLS_MAP[$INSTANCE_EXPORTER_TOOL_NAME]}" ]]; then
        uninstall_instance_exporter
    fi

    REMOTE_HOST_PASSWORD_MAP=()
}

# ============================================
# Module: Prepare Host Credentials
# ============================================

declare -A ALL_UNIQUE_HOST_MAP=()
declare -A SKIP_HOST_MAP=()
declare -A LOCAL_HOST_MAP=()
declare -A REMOTE_HOST_PASSWORD_MAP=()

function unite_all_unique_ip_port_user() {
    local -A all_host_map=()

    for id in "${!MIGRATION_PORTAL_ID_MAP[@]}"; do
        local key="${MIGRATION_PORTAL_IP_MAP[$id]},${MIGRATION_PORTAL_PORT_MAP[$id]},${MIGRATION_PORTAL_USER_MAP[$id]}"
        ALL_UNIQUE_HOST_MAP["$key"]=1
    done
    
    for id in "${!PROMETHEUS_ID_MAP[@]}"; do
        local key="${PROMETHEUS_IP_MAP[$id]},${PROMETHEUS_PORT_MAP[$id]},${PROMETHEUS_USER_MAP[$id]}"
        ALL_UNIQUE_HOST_MAP["$key"]=1
    done
    
    for id in "${!INSTANCE_EXPORTER_ID_MAP[@]}"; do
        local key="${INSTANCE_EXPORTER_IP_MAP[$id]},${INSTANCE_EXPORTER_PORT_MAP[$id]},${INSTANCE_EXPORTER_USER_MAP[$id]}"
        ALL_UNIQUE_HOST_MAP["$key"]=1
    done
    
    echo_info "All installed tools hosts:"
    for item in "${!ALL_UNIQUE_HOST_MAP[@]}"; do
        echo_info "$item"
    done
}

function generate_local_host_map() {
    local local_ips=$(ip -4 addr show | grep -oP '(?<=inet\s)\d+(\.\d+){3}')
    local local_port=22
    local current_user=$(whoami)

    for ip in $local_ips; do
        LOCAL_HOST_MAP["$ip,$local_port,$current_user"]=0
    done
}

function prepare_host_credentials() {
    echo_info "Start preparing host credentials..."

    for host_key in "${!ALL_UNIQUE_HOST_MAP[@]}"; do
        local ip=$(echo "$host_key" | cut -d',' -f1)
        local port=$(echo "$host_key" | cut -d',' -f2)
        local user=$(echo "$host_key" | cut -d',' -f3)
        local host_info="$ip:$port($user)"

        if [[ -n "${LOCAL_HOST_MAP["$host_key"]}" ]]; then
            LOCAL_HOST_MAP["$host_key"]=1
            echo_info "Host $host_info is local machine, not need to input password."
            continue
        fi

        local password=""
        local connect_success=0

        while [[ $connect_success -eq 0 ]]; do
            echo_info "Host $host_info is remote machine, need to input password."
            read -p "Please input password for remote host $host_info: " -s password
            echo ""

            echo_info "Testing connection to remote host $host_info..."
            if SSHPASS="$password" sshpass -e ssh -p "$port" "$user"@"$ip" -o ConnectTimeout=5 -o StrictHostKeyChecking=no -q -T "/bin/true"; then
                connect_success=1
                REMOTE_HOST_PASSWORD_MAP["$host_key"]="$password"
                echo_info "Host $host_info connection test succeeded."
            else
                echo_error "Host $host_info connection test failed, please check the password."
                
                local retry_choice=""
                while [[ "$retry_choice" != "y" && "$retry_choice" != "n" ]]; do
                    read -p "Do you want to retry input password for host $host_info (y/n): " retry_choice
                    case "$retry_choice" in
                        n|N)
                            SKIP_HOST_MAP["$host_key"]=1
                            echo_warn "Cancel input password for host $host_info, skip this host tools uninstall."
                            connect_success=1
                            break
                            ;;
                        y|Y)
                            password=""
                            break
                            ;;
                        *)
                            echo_error "Invalid input, please input y or n."
                            ;;
                    esac
                done
            fi
        done
    done
    return 0
}

# ============================================
# Module: Main
# ============================================

IS_ALL_UNINSTALL=true

function usage() {
    cat <<EOF
Datakit Uninstall Script

Usage:
  $0               Uninstall DataKit and all installed tools
  $0 -s|--self     Uninstall only DataKit
  $0 -a|--all      Uninstall DataKit and all installed tools
  $0 -h|--help     Show this help message

Tips:
  1. When uninstalling tools on a remote machine, you need to provide the password for each machine.
EOF
}

function parse_args() {
    if [[ $# -eq 0 ]]; then
        IS_ALL_UNINSTALL=true
        return 0
    fi

    if [[ $# -gt 1 ]]; then
        echo_error "Invalid argument: Only one option is allowed."
        usage
        exit 1
    fi

    case "$1" in
        -s|--self)
            IS_ALL_UNINSTALL=false
            ;;
        -a|--all)
            IS_ALL_UNINSTALL=true
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        *)
            echo_error "Invalid argument: $1"
            usage
            exit 1
            ;;
    esac
}

function main() {
    parse_args "$@"

    if [[ $IS_ALL_UNINSTALL == true ]]; then
        uninstall_third_party_tools
        datakit_uninstall
    else
        datakit_uninstall
    fi
}

main "$@"