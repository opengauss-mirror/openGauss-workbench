/**
 Copyright  ruoyi.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.opengauss.admin.common.core.page;

import org.opengauss.admin.common.utils.ServletUtils;

/**
 * table support
 *
 * @author xielibo
 */
public class TableSupport {
    /**
     * pageNum
     */
    public static final String PAGE_NUM = "pageNum";

    /**
     * pageSize
     */
    public static final String PAGE_SIZE = "pageSize";

    /**
     * orderByColumn
     */
    public static final String ORDER_BY_COLUMN = "orderByColumn";

    /**
     * order type
     */
    public static final String IS_ASC = "isAsc";

    /**
     * reasonable
     */
    public static final String REASONABLE = "reasonable";

    public static PageDomain getPageDomain() {
        PageDomain pageDomain = new PageDomain();
        pageDomain.setPageNum(ServletUtils.getParameterToInt(PAGE_NUM));
        pageDomain.setPageSize(ServletUtils.getParameterToInt(PAGE_SIZE));
        pageDomain.setOrderByColumn(ServletUtils.getParameter(ORDER_BY_COLUMN));
        pageDomain.setIsAsc(ServletUtils.getParameter(IS_ASC));
        pageDomain.setReasonable(ServletUtils.getParameterToBool(REASONABLE));
        return pageDomain;
    }

    public static PageDomain buildPageRequest() {
        return getPageDomain();
    }
}
