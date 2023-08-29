#!/bin/bash
#############################################################################
# Copyright (c) 2023 Huawei Technologies Co.,Ltd.
#
# openGauss is licensed under Mulan PSL v2.
# You can use this software according to the terms
# and conditions of the Mulan PSL v2.
# You may obtain a copy of Mulan PSL v2 at:
#
#   http://license.coscl.org.cn/MulanPSL2
#
# THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
# EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
# MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
# See the Mulan PSL v2 for more details.
# ----------------------------------------------------------------------------
# Description  : shell script for build datakit and plugins.
#############################################################################
declare mvn_target="clean install package"
declare mvn_input_args=''
declare mvn_prod='prod'
#############################################################################
function print_help()
{
    echo "Usage: $0 [OPTION]
    -h|--help              show help information
    -t|--target            set the mvn build target, default is clean install package
    -a|--args              set the mvn build args additions, default is empty
    -p|--profile           the profile, default is prod
example:
    sh build.sh  // build in default
    sh build.sh -a \"-Dbuild.frontend.skip=true -Dweb.build.skip=true\"  // build skip frontend
    sh build.sh -t \"clean\" -a \"-Dbuild.frontend.skip=true -Dweb.build.skip=true\"  // build only clean and skip frontend
    "
}

while [ $# -gt 0 ]; do
    case "$1" in
        -h|--help)
            print_help
            exit 1
            ;;
        -t|--target)
            if [ "$2"X = X ]; then
                echo "no given correct mvn target, such as: clean install package"
                exit 1
            fi
            mvn_target=$2
            shift 2
            ;;
        -a|--args)
            if [ "$2"X = X ]; then
                echo "no given correct mvn args, such as: -Dbuild.frontend.skip=true -Dweb.build.skip=true"
                exit 1
            fi
            mvn_input_args=$2
            shift 2
            ;;
        -p|--profile)
            if [ "$2"X = X ]; then
                echo "no given mvn profile, such as release or dev"
                exit 1
            fi
            mvn_prod=$2
            shift 2
            ;;
         *)
            echo "Internal Error: option processing error: $1" 1>&2
            echo "please input right paramtenter, the following command may help you"
            echo "./build.sh --help or ./build.sh -h"
            exit 1
    esac
done
root_path=`pwd`
output_path=$root_path/output
mvn_init_args="-Dmaven.test.skip=true"
mvn_args="$mvn_init_args -U -P $mvn_prod $mvn_input_args"
build_main_pkg=openGauss-datakit
plugin_output=visualtool-plugin
plugin_doc_output=doc

echo "we got build cmd: mvn $mvn_target $mvn_args"

export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF8"
pom_version=`awk '/<version>[^<]+<\/version>/{gsub(/<version>|<\/version>/,"",$1);print $1;exit;}' ${root_path}/$build_main_pkg/pom.xml`
mkdir -p output 
rm -rf output/*
mkdir -p output/$plugin_output
mkdir -p output/$plugin_doc_output

function prepare_java_env()
{
    echo "We no longer provide java, please makesure java(1.11*) already in PATH!"
    JAVA_VERSION=`java -version 2>&1 | awk -F '"' '/version/ {print $2}'`
    echo java version is $JAVA_VERSION
}

function prepare_maven_env()
{
    echo "We no longer provide mvn, please makesure mvn(3.8.0+) already in PATH!"
    MAVEN_VERSION=`mvn -v 2>&1 | awk '/Apache Maven / {print $3}'`
    echo maven version is $MAVEN_VERSION
}

function prepare_npm_env()
{
    echo "We no longer provide node/npm, please makesure npm(9.8.0+) already in PATH!"
    NPM_VERSION=`npm -v 2>&1 | awk -F '"' '// {print $1}'`
    echo npm version is $NPM_VERSION
}

function prepare_env()
{
    prepare_java_env
    prepare_maven_env
    prepare_npm_env
}

function build_main_pkg() {
    cd $root_path/$build_main_pkg
    echo "build dir:${root_path}/${build_main_pkg} ,to run cmd: mvn ${mvn_target} ${mvn_args}"
    mvn ${mvn_target} ${mvn_args}
    if [ $? -ne 0 ]; then
      echo "Build datakit failed..."
      exit 1
    fi
    cp ./visualtool-api/target/openGauss-datakit-*.jar ${output_path}/
    cp ./README.md ${output_path}/${plugin_doc_output}/datakit-README.md
    cp ./config/application-temp.yml ${output_path}/
}

function build_plugin() {
  local plugin_path=$root_path/plugins
  cd $root_path
  sub_plugin_paths=`ls -D ./plugins`
  for plugin_name in ${sub_plugin_paths[*]}
  do
      {
        local build_path=$plugin_path/${plugin_name}
        if [ ! -d $build_path ]; then
            echo "NOT dir: ${build_path} ,skip now."
            continue
        fi
        echo "build dir:${build_path} ,to run cmd: mvn ${mvn_target} ${mvn_args}"
        cd ${build_path}
        mvn ${mvn_target} ${mvn_args}
        if [ $? -ne 0 ]; then
            echo "build ${plugin_name} failed!!!"
            exit 1;
        fi
        echo "build ${plugin_name} success!!!"
        if [ "$plugin_name" == "openGauss-tools-monitor" ] ; then
            cp ./**/target/*repackage.jar ${output_path}/$plugin_output/ 2>/dev/null
        else
            cp ./**target/*repackage.jar ${output_path}/$plugin_output/ 2>/dev/null
        fi
        echo "copy ${plugin_name} success!!!"
        if [ -f ./readme.md ] ; then
            cp ./readme.md ${output_path}/${plugin_doc_output}/${plugin_name}-README.md
        fi
        if [ -f ./README.md ] ; then
            cp ./README.md ${output_path}/${plugin_doc_output}/${plugin_name}-README.md
        fi
      }&
  done
  wait
}

prepare_env
build_main_pkg
build_plugin
cd $output_path
tar -zcf Datakit-${pom_version}.tar.gz ./*
