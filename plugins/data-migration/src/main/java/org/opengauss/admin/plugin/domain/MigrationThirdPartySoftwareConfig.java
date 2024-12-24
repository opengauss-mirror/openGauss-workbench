/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.admin.plugin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MigrationThirdPartySoftwareConfig
 *
 * @author: www
 * @date: 2023/11/28 15:14
 * @description: msg
 * @since: 1.1
 * @version: 1.1
 */
@Data
@Builder
@TableName("tb_migration_third_party_software_config")
@NoArgsConstructor
@AllArgsConstructor
public class MigrationThirdPartySoftwareConfig {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "zk_port")
    private String zookeeperPort;

    @TableField(value = "kafka_port")
    private String kafkaPort;

    @TableField(value = "schema_registry_port")
    private String schemaRegistryPort;

    @TableField(value = "zk_ip")
    private String zkIp;

    @TableField(value = "kafka_ip")
    private String kafkaIp;

    @TableField(value = "schema_registry_ip")
    private String schemaRegistryIp;

    @TableField(value = "install_dir")
    private String installDir;

    // 逗号隔开的字符串
    @TableField(value = "bind_portal_host")
    private String bindPortalHost;

    @TableField(value = "host")
    private String host;

    @TableField(exist = false)
    private Integer thirdPartySoftwareConfigType;

    /**
     * 替换~ 为目录结构
     *
     * @author: www
     * @date: 2023/11/28 15:14
     * @description: msg
     * @since: 1.1
     * @version: 1.1
     * @param userName userName
     */
    public void replacePathHome(String userName) {
        if (this.installDir.contains("~")) {
            this.installDir = this.installDir.replace("~", "/home/" + userName);
        }
    }

    /**
     * checks if the kafkaIp or kafkaPort is null.
     *
     * @return true/false
     */
    public boolean isEmpty() {
        return this.kafkaIp == null || this.kafkaPort == null;
    }
}
