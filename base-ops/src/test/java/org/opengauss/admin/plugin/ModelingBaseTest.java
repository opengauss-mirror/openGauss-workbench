/*
 * Copyright (c) 2022 Huawei Technologies Co.,Ltd.
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
 * ModelingBaseTest.java
 *
 * IDENTIFICATION
 * base-ops/src/test/java/org/opengauss/admin/plugin/ModelingBaseTest.java
 *
 * -------------------------------------------------------------------------
 */

package org.opengauss.admin.plugin;

import org.junit.After;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

public class ModelingBaseTest {

    @Before
    public void before(){
        MockitoAnnotations.openMocks(this);
        System.out.println("start test:" + this.getClass());
    }

    @After
    public void after(){
        System.out.println("start end:" + this.getClass());
    }

}
