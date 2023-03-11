package org.opengauss.admin.plugin.enums.ops;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * installation package type
 *
 * @author wangyl
 * @date 2023/3/9 17:08
 **/
@Getter
@AllArgsConstructor
public enum PackageTypeEnum {
    OPENGAUSS("openGauss"),
    ZOOKEEPER("zookeeper"),
    SHARDING_PROXY("shardingProxy"),
    OPENLOOKENG("openLooKeng"),
    DISTRIBUTE_DEPLOY("dad");

    private String packageType;
}
