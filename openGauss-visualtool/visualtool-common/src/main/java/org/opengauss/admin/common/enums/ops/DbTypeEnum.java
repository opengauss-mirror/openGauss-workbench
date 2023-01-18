package org.opengauss.admin.common.enums.ops;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lhf
 * @date 2023/1/13 11:00
 **/
@AllArgsConstructor
@Getter
public enum DbTypeEnum {
    MYSQL("com.mysql.cj.jdbc.Driver");

    private String driverClass;

    public static DbTypeEnum typeOf(String dbType) {
        if (StrUtil.isEmpty(dbType)) {
            return null;
        }

        for (DbTypeEnum enumConstant : DbTypeEnum.class.getEnumConstants()) {
            if (enumConstant.name().equalsIgnoreCase(dbType)) {
                return enumConstant;
            }
        }
        return null;
    }
}
