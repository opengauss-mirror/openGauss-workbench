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
 * SysRoleTest.java
 *
 * IDENTIFICATION
 * openGauss-visualtool/visualtool-api/src/test/java/org/opengauss/admin/service/SysRoleTest.java
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
import org.opengauss.admin.common.core.domain.entity.SysRole;
import org.opengauss.admin.system.mapper.SysRoleMapper;
import org.opengauss.admin.system.mapper.SysRoleMenuMapper;
import org.opengauss.admin.system.mapper.SysUserRoleMapper;
import org.opengauss.admin.system.service.ISysRoleService;
import org.opengauss.admin.system.service.impl.SysRoleServiceImpl;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Set;

/**
 * @className: SysRoleTest
 * @description: SysRoleTest
 * @author: xielibo
 * @date: 2022-10-16 13:24
 **/
@RunWith(SpringRunner.class)
@Slf4j
public class SysRoleTest {

    @InjectMocks
    private ISysRoleService sysRoleService = new SysRoleServiceImpl();

    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysUserRoleMapper userRoleMapper;
    @Mock
    private SysRoleMenuMapper roleMenuMapper;

    private List<SysRole> mockSysRoles() {
        return List.of(mockSysRole());
    }

    private SysRole mockSysRole() {
        SysRole role = new SysRole();
        role.setRoleName("test");
        role.setRoleId(100);
        role.setStatus("1");
        role.setRoleKey("test");
        return role;
    }

    @BeforeClass
    public static void before() {
        MockitoAnnotations.initMocks(SysRoleTest.class);
        log.info("start system role test........");
    }

    @AfterClass
    public static void after() {
        log.info("end system role test........");
    }

    @Test
    public void testSelectRoleList() {
        IPage<SysRole> page = new Page<>();
        page.setRecords(mockSysRoles());
        page.setPages(1);
        page.setCurrent(1);
        page.setTotal(1);
        Mockito.doReturn(page).when(roleMapper).selectRoleList(ArgumentMatchers.any(Page.class), ArgumentMatchers.any(SysRole.class));

        IPage pageResult = sysRoleService.selectRoleList(new SysRole(), new Page<>());
        Assertions.assertNotNull(pageResult.getRecords());
        Assertions.assertSame(page, pageResult);
    }

    @Test
    public void testSelectRolesByUserId() {
        Mockito.doReturn(mockSysRoles()).when(roleMapper).selectRoleList(ArgumentMatchers.any(SysRole.class));
        Mockito.doReturn(mockSysRoles()).when(roleMapper).selectRolePermissionByUserId(ArgumentMatchers.any());
        List<SysRole> roles = sysRoleService.selectRolesByUserId(100);
        roles.forEach(e -> {
            Assertions.assertEquals(true, e.isFlag());
        });
    }

    @Test
    public void testSelectRolePermissionByUserId() {
        Mockito.doReturn(mockSysRoles()).when(roleMapper).selectRoles(ArgumentMatchers.any());
        Set<String> pres = sysRoleService.selectRolePermissionByUserId(100);
        Assertions.assertTrue(pres.contains("test"));
    }


    @Test
    public void testCheckRoleNameUnique() {
        SysRole role = new SysRole();
        role.setRoleName("test");
        role.setRoleId(100);
        String validUnique = "0";
        Mockito.doReturn(mockSysRole()).when(roleMapper).selectRole(ArgumentMatchers.any());
        String isUnique = sysRoleService.checkRoleNameUnique(role);
        Assertions.assertEquals(validUnique, isUnique);

        role.setRoleName("test3");
        role.setRoleId(1);
        String validNotUnique = "1";
        Mockito.doReturn(mockSysRole()).when(roleMapper).selectRole(ArgumentMatchers.any());
        isUnique = sysRoleService.checkRoleNameUnique(role);
        Assertions.assertEquals(validNotUnique, isUnique);
    }

    @Test
    public void testInsertRole() {
        SysRole role = mockSysRole();
        role.setMenuIds(new Integer[]{1, 4, 3});
        int validRow = 1;
        Mockito.doReturn(validRow).when(roleMapper).insert(ArgumentMatchers.any());
        Mockito.doReturn(1).when(roleMenuMapper).batchRoleMenu(ArgumentMatchers.any());
        int row = sysRoleService.insertRole(role);
        Assertions.assertEquals(validRow, row);
    }


    @Test
    public void testUpdateRole() {
        SysRole role = mockSysRole();
        role.setMenuIds(new Integer[]{1, 4, 3});
        int validRow = 1;
        Mockito.doReturn(validRow).when(roleMapper).updateById(ArgumentMatchers.any());
        Mockito.doReturn(1).when(roleMenuMapper).delete(ArgumentMatchers.any());
        Mockito.doReturn(1).when(roleMenuMapper).batchRoleMenu(ArgumentMatchers.any());
        int row = sysRoleService.updateRole(role);
        Assertions.assertEquals(validRow, row);
    }

    @Test
    public void testDeleteRoleByIds() {
        int validRow = 1;
        Mockito.doReturn(1).when(roleMenuMapper).delete(ArgumentMatchers.any());
        Mockito.doReturn(1).when(roleMapper).deleteBatchIds(ArgumentMatchers.any());
        int row = sysRoleService.deleteRoleByIds(new Integer[]{2, 3});
        Assertions.assertEquals(validRow, row);
    }


}
