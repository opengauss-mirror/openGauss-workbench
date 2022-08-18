package org.opengauss.admin.common.core.vo;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

/**
 * @className: MenuVo
 * @description: Menu value object
 * @author: xielibo
 * @date: 2022-09-24 6:02 PM
 **/
@Builder
@Data
public class MenuVo {

    /**
     * menu ID
     */
    private Integer menuId;

    /**
     * menu Name
     */
    private String menuName;

    /**
     * parent ID
     */
    private Integer parentId;

    @Tolerate
    public MenuVo(){}

}
