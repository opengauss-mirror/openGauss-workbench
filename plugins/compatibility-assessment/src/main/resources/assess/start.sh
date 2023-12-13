#!/bin/bash
# Copyright (c): 2023-2023, Huawei Tech. Co., Ltd.

VERSION=5.1.0
dataSource=""
confFile=""
reportFile=""
jarName="compatibility-assessment-${VERSION}.jar"

function print_help()
{
    echo "Usage: $0 [OPTION]
    -h|--help                         show help information
    -d|--dataSource                 this values of paramenter is collect or file
    -c|--confFile              the path of configure file
    -o|--output                    output file, must be .html, default is report.html.
    "
}

function startJar() {
    cmd="java -jar"
    if [ "${dataSource}"X = "file"X ]; then
        cmd="${cmd} -Djavax.xml.accessExternalDTD=all ${jarName}"
    fi
    cmd="${cmd} ${jarName}  -d ${dataSource}"

    if [ "${confFile}"X != X ]; then
        cmd="${cmd} -c ${confFile}"
    fi

    if [ "${reportFile}"X != X ]; then
        cmd="${cmd} -o ${reportFile}"
    fi
    
    echo ${cmd}
    $cmd
}


function main() {
    while [ $# -gt 0 ]; do
        case "$1" in
            -h|--help)
                print_help
                exit 1
                ;;
            -d|--dataSource)
                if [ "$2"X = X ]; then
                    echo "no given correct dataSource information"
                    exit 1
                fi
                dataSource=$2
                shift 2
                ;;
            -c|--confFile)
                if [ "$2"X = X ]; then
                    echo "no given configure file"
                    exit 1
                fi
                confFile=$2
                shift 2
                ;;
            -o|--output)
                if [ "$2"X = X ]; then
                    echo "no given output file"
                    exit 1
                fi
                reportFile=$2
                shift 2
                ;;
            *)
                echo "Internal Error: option processing error: $1" 1>&2
                echo "please input right paramtenter, the following command may help you"
                echo "./start.sh --help or ./start.sh -h"
                exit 1
        esac
    done

    if [ "${dataSource}"X = X ]; then
        echo "Error: optional -d or --dataSource must specified."
        exit 1
    fi

    startJar

}

main $@