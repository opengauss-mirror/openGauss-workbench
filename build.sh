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
declare mvn_target="clean package"
declare mvn_input_args=''
declare mvn_prod='prod'
#############################################################################
function print_help()
{
    echo "Usage: $0 [OPTION]
    -h|--help              show help information
    -t|--target            set the mvn build target, default is clean package
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
                echo "no given correct mvn target, such as: clean package"
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
pom_version=`awk '/<admin.version>[^<]+<\/admin.version>/{gsub(/<admin.version>|<\/admin.version>/,"",$1);print $1;exit;}' ${root_path}/pom.xml`
mkdir -p output 
rm -rf output/*
mkdir -p output/$plugin_output
mkdir -p output/$plugin_doc_output

function prepare_java_env()
{
    echo "We no longer provide java, please makesure java(1.11*) already in PATH!"
    JAVA_VERSION=`java -version 2>&1 | awk -F '"' '/version/ {print $2}'`
    echo java version is $JAVA_VERSION
    if [ x"$JAVA_VERSION" \< x"11" ]; then
        echo "java version is not meeting requirements!"
        exit 1
    fi
}

function prepare_maven_env()
{
    echo "We no longer provide mvn, please makesure mvn(3.6.0+) already in PATH!"
    MAVEN_VERSION=`mvn -v 2>&1 | awk '/Apache Maven / {print $3}'`
    echo maven version is $MAVEN_VERSION
    if [ x"$MAVEN_VERSION" \< x"3.6" ]; then
          echo "maven version is not meeting requirements!"
          exit 1
    fi
}

function prepare_npm_env()
{
    echo "We no longer provide node/npm, please makesure npm(8.11.0+) already in PATH!"
    NPM_VERSION=`npm -v 2>&1 | awk -F '"' '// {print $1}'`
    echo npm version is $NPM_VERSION
    if [ x"$NPM_VERSION" \< x"8.11" ]; then
          echo "npm version is not meeting requirements!"
          exit 1
    fi
}

function prepare_env()
{
    prepare_java_env
    prepare_maven_env
    prepare_npm_env
}

function get_git_log(){
    cd $output_path
    package_time=`date '+%Y-%m-%d %H:%M:%S'`
    echo "--------------------------------get_git_log---------------------------------"
    echo "build time: "$package_time
    echo "build time: "$package_time >> build_commit_id.log
    echo "git branch: "$(git rev-parse --abbrev-ref HEAD)
    echo "git branch: "$(git rev-parse --abbrev-ref HEAD) >> build_commit_id.log
    echo "last commit:"
    echo "last commit:" >> build_commit_id.log
    echo "$(git log -1)"
    echo "$(git log -1)" >> build_commit_id.log
    echo "--------------------------------get_git_log finished---------------------------------"
}

function build_pkg() {
    cd $root_path
    echo "build dir:${root_path} ,to run cmd: mvn ${mvn_target} ${mvn_args}"
    mvn ${mvn_target} ${mvn_args}
    if [ $? -ne 0 ]; then
      echo "Build datakit failed..."
      exit 1
    fi
    cp ./${build_main_pkg}/visualtool-api/target/openGauss-datakit-*.jar ${output_path}/
    cp ./run.sh ${output_path}/
    cp ./${build_main_pkg}/README.md ${output_path}/${plugin_doc_output}/datakit-README.md
    cp ./${build_main_pkg}/config/application-temp.yml ${output_path}/
}

function copy_plugin_pkg() {
  local plugin_path=$root_path/plugins
  cd $root_path
  sub_plugin_paths=`ls -D ./plugins`
  for plugin_name in ${sub_plugin_paths[*]}
  do
      {
        local build_path=$plugin_path/${plugin_name}
        cd ${build_path}
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
get_git_log
build_pkg
copy_plugin_pkg
cd $output_path
tar -zcf Datakit-${pom_version}.tar.gz ./*
