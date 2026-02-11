#!/bin/bash

# Copyright (c) Huawei Technologies Co., Ltd. 2026-2026. All rights reserved.
# Description: DataKit Installation Script

datakit_home_dir=$(cd "$(dirname "$0")" && pwd)

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

    rm -rf files ssl logs
    mkdir files ssl logs
    echo "Create required directories successfully"
}

check_java_version() {
    echo "Checking Java version..."
    local java_version
    local version_num

    if ! java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}'); then
        echo "Error: Java is not installed or not in PATH"
        exit 1
    fi

    if [[ "$java_version" =~ ^1\. ]]; then
        version_num=$(echo "$java_version" | cut -d. -f2)
    else
        version_num=$(echo "$java_version" | cut -d. -f1)
    fi

    if [ "$version_num" -lt 17 ]; then
        echo "Error: Java 17 or later is required (found Java version: $java_version)"
        exit 1
    fi

    echo "Check Java version is ${java_version} "
}

generate_ssl_key() {
    echo "Generating the SSL key..."

    keytool -genkey -noprompt -dname "CN=opengauss, OU=opengauss, O=opengauss, L=Beijing, S=Beijing, C=CN" \
    -alias opengauss -storetype PKCS12 -keyalg RSA -keysize 4096 \
    -keystore ${datakit_home_dir}/ssl/keystore.p12 \
    -validity 365 -storepass ${store_password} \

    if [ $? -ne 0 ]; then
        echo "Error: Generated the SSL key failed"
        exit 1
    fi

    echo "Generate the SSL key successfully"
}

change_config_file() {
    echo "Configuring the application-temp.yml..."

    sed -i "/key-store-password:/s#.*#    key-store-password: '${store_password}'#" ${config_file_path}
    sed -i "/defaultStoragePath:/s#.*#  defaultStoragePath: ${datakit_home_dir}/files#" ${config_file_path}
    sed -i "/key-store:/s#.*#    key-store: ${datakit_home_dir}/ssl/keystore.p12#" ${config_file_path}
    sed -i "/    path:/s#.*#    path: ${datakit_home_dir}/logs/#" ${config_file_path}
    sed -i "/  config: /s#.*#  config: ${datakit_home_dir}/config/log4j2.xml#" ${config_file_path}

    echo "Configuring the application-temp.yml successfully"
}

main() {
    cd ${datakit_home_dir}
    check_jar
    create_required_dirs
    check_java_version
    generate_ssl_key
    change_config_file

    echo "Datakit has been installed successfully. "
    echo "Please go to the '${datakit_home_dir}' directory to manually start the DataKit server."
    echo "The command is as follows: "
    echo "    sh run.sh start --aes-key ******"
    echo "    sh run.sh status"
    echo "    sh run.sh stop"
    echo "    sh run.sh restart --aes-key ******"
}

main