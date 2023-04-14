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
 * SysWhiteListTest.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/test/java/org/opengauss/admin/service/SysWhiteListTest.java
 *
 * -------------------------------------------------------------------------
 */


package org.opengauss.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.opengauss.admin.system.domain.SysWhiteList;
import org.opengauss.admin.system.mapper.SysWhiteListMapper;
import org.opengauss.admin.system.service.ISysWhiteListService;
import org.opengauss.admin.system.service.impl.SysWhiteListServiceImpl;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @className: SysWhileListTest
 * @description: SysWhiteListTest
 * @author: xielibo
 * @date: 2022-10-16 13:24
 **/
@RunWith(SpringRunner.class)
@Slf4j
public class SysWhiteListTest {

    @InjectMocks
    private ISysWhiteListService sysWhiteListService = new SysWhiteListServiceImpl();

    @Mock
    private SysWhiteListMapper sysWhiteListMapper;

    private List<SysWhiteList> mockSysWhiteLists() {
        SysWhiteList whiteList = new SysWhiteList();
        whiteList.setIpList("192.168.0.21,192.168.0.22");
        whiteList.setTitle("test");
        return List.of(whiteList);
    }

    @BeforeClass
    public static void before() {
        MockitoAnnotations.initMocks(SysWhiteListTest.class);
        log.info("start white list test........");
    }

    @AfterClass
    public static void after() {
        log.info("end white list test........");
    }

    @Test
    public void testPageSysWhiteList() {
        IPage<SysWhiteList> page = new Page<>();
        page.setRecords(mockSysWhiteLists());
        page.setPages(1);
        page.setCurrent(1);
        page.setTotal(1);
        Mockito.doReturn(page).when(sysWhiteListMapper)
                .selectWhiteListPage(ArgumentMatchers.any(Page.class), ArgumentMatchers.any(SysWhiteList.class));
        IPage pageResult = sysWhiteListService.selectList(new Page<>(), new SysWhiteList());
        Assertions.assertNotNull(pageResult.getRecords());
        Assertions.assertSame(page, pageResult);
    }

    @Test
    public void testListSysWhiteList() {
        Mockito.doReturn(mockSysWhiteLists()).when(sysWhiteListMapper)
                .selectWhiteListList(ArgumentMatchers.any(SysWhiteList.class));
        List<SysWhiteList> list = sysWhiteListService.selectListAll(new SysWhiteList());
        Assertions.assertNotNull(list);
        Assertions.assertTrue(list.size() > 0);
        Assertions.assertArrayEquals(mockSysWhiteLists().toArray(), list.toArray());
    }

}
