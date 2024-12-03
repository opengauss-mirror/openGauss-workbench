/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2024-2024. All rights reserved.
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
 * ConstantEnum.java
 *
 * IDENTIFICATION
 * plugins/container-management-plugin/src/main/java/org/opengauss/admin/container/config/ConstantEnum.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.container.config;

/**
 * 常量枚举
 *
 * @since 2024-08-29
 */
public enum ConstantEnum {
    REDIS_KEY_K8S_CLUSTER("ogsp:k8s:cluster:list"),
    REDIS_KEY_CMDB_SYSTEM("ogsp:cmdb:system:list"),
    OPENGAUSS_OPERATOR_TYPE_KEY("opengauss-operator-type"),
    OPENGAUSS_OPERATOR_TYPE_PRD("prd"),
    OPENGAUSS_OPERATOR_TYPE_TEST("test"),
    OPENGAUSS_NAMESPACE("opengauss"),
    OPENGAUSS_OPERATOR_LABEL_KEY("opengauss-operator-name"),
    OPENGAUSS_CMDB_SYSTEM_ID_LABEL_KEY("cmdb-system-id"),
    OPENGAUSS_NAME_LABEL_KEY("cmdb.cmos.cn/ogcName"),
    OPENGAUSS_CRD_KIND("OpenGaussCluster"),
    OPENGAUSS_CRD_GROUP("gaussdb.gaussdb.middleware.cmos.cn"),
    OPENGAUSS_CRD_SLASH("/"),
    OPENGAUSS_CRD_VERSION("v1"),
    OPENGAUSS_CRD_PLURAL("opengaussclusters"),
    OPENGAUSS_CRD_SINGULAR("opengausscluster"),
    NETWORK_MACVLAN("underlayMacvlan"),
    DISKUNIT("Gi"),
    STORAGE_CLASS_NAME_LOCAL_STORAGE("local-storage"),
    STORAGE_TYPE("local"),
    OPENGAUSS_ROOT_PASSWORD_PRD("p#5IGsvb*0Nh"),
    OPERATOR_NUM_ADD("+"),
    OPERATOR_NUM_REDUCE("-"),
    X86_IMAGE_PREFIX("k8s-deploy/"),
    ARM_IMAGE_PREFIX("k8s-deploy-arm/"),
    OPENGAUSS_IMAGE("opengauss-container:"),
    BACKUP_IMAGE("opengauss-backup:"),
    EXPORTER_IMAGE("opengauss-exporter:"),
    CLEANUP_IMAGE("opengauss-cleanup:"),
    OPENGAUSS_OPERATOR_NAMESPACE("kube-system");
    private String value;

    ConstantEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}


