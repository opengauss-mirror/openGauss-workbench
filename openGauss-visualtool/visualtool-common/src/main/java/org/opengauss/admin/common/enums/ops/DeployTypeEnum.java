package org.opengauss.admin.common.enums.ops;

/**
 * deploy type
 *
 * @author lhf
 * @date 2022/8/6 17:08
 **/
public enum DeployTypeEnum {
    /**
     * cluster
     */
    CLUSTER,
    /**
     * single node
     */
    SINGLE_NODE;

    public static DeployTypeEnum nameOf(String deployType) {
        DeployTypeEnum[] enumConstants = DeployTypeEnum.class.getEnumConstants();
        for (DeployTypeEnum enumConstant : enumConstants) {
            if (enumConstant.name().equalsIgnoreCase(deployType)) {
                return enumConstant;
            }
        }
        return null;
    }
}
