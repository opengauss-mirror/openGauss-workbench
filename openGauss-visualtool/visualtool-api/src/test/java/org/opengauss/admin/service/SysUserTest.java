package org.opengauss.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.common.core.domain.entity.SysRole;
import org.opengauss.admin.common.core.domain.entity.SysUser;
import org.opengauss.admin.system.domain.SysWhiteList;
import org.opengauss.admin.system.mapper.SysRoleMapper;
import org.opengauss.admin.system.mapper.SysUserMapper;
import org.opengauss.admin.system.mapper.SysUserRoleMapper;
import org.opengauss.admin.system.service.ISysUserService;
import org.opengauss.admin.system.service.impl.SysUserServiceImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @className: SysUserTest
 * @description: SysUserTest
 * @author: xielibo
 * @date: 2022-10-16 13:24
 **/
@RunWith(SpringRunner.class)
public class SysUserTest {

    @InjectMocks
    private ISysUserService sysUserService = new SysUserServiceImpl();

    @Mock
    private SysUserMapper sysUserMapper;
    @Mock
    private SysRoleMapper roleMapper;
    @Mock
    private SysUserRoleMapper userRoleMapper;

    private List<SysUser> mockSysUsers(){
        return List.of(mockSysUser());
    }
    private SysUser mockSysUser(){
        SysUser user = new SysUser();
        user.setUserId(100);
        user.setUserName("xielibo");
        user.setNickName("xielibo");
        user.setPassword("123123");
        user.setPhonenumber("13866886688");
        user.setEmail("xielibo22@163.com");
        user.setStatus("1");
        return user;
    }
    private List<SysRole> mockSysRoles(){
        SysRole role = new SysRole();
        role.setRoleId(1);
        role.setRoleName("admin");
        return List.of(role);
    }

    @BeforeClass
    public static void before(){
        MockitoAnnotations.initMocks(SysUserTest.class);
        System.out.println("start system user test........");
    }
    @AfterClass
    public static void after(){
        System.out.println("end system user test........");
    }

    @Test
    public void testPageSysUser(){
        IPage<SysUser> page = new Page<>();
        page.setRecords(mockSysUsers());
        page.setPages(1);
        page.setCurrent(1);
        page.setTotal(1);
        Mockito.doReturn(page).when(sysUserMapper).selectUserList(ArgumentMatchers.any(Page.class), ArgumentMatchers.any(SysUser.class));
        IPage pageResult = sysUserService.selectUserList(new SysUser(),new Page<>());
        Assertions.assertNotNull(pageResult.getRecords());
        Assertions.assertSame(page, pageResult);
    }

    @Test
    public void testSelectUserByUserName() {
        SysUser checkUser = mockSysUser();
        Mockito.doReturn(mockSysUser()).when(sysUserMapper).selectUserVo(ArgumentMatchers.any());
        SysUser user = sysUserService.selectUserByUserName("xielibo");
        Assertions.assertNotNull(user);
        Assertions.assertEquals(checkUser, user);
    }

    @Test
    public void testListSysUser(){
        SysUser checkUser = mockSysUser();
        Mockito.doReturn(mockSysUser()).when(sysUserMapper).selectUserVo(ArgumentMatchers.any());
        SysUser user = sysUserService.selectUserById(100);
        Assertions.assertNotNull(user);
        Assertions.assertEquals(checkUser, user);
    }

    @Test
    public void testSelectUserRoleGroup(){
        String group = "admin";
        Mockito.doReturn(mockSysRoles()).when(roleMapper).selectRoles(ArgumentMatchers.any());
        String userRoleGroup = sysUserService.selectUserRoleGroup(ArgumentMatchers.anyString());
        Assertions.assertEquals(group, userRoleGroup);
    }

    @Test
    public void testCheckUserNameUnique(){
        String validUnique = "0";
        Mockito.doReturn(0L).when(sysUserMapper).selectCount(ArgumentMatchers.any());
        String isUnique = sysUserService.checkUserNameUnique("xielibo001");
        Assertions.assertEquals(validUnique, isUnique);

        String validNotUnique = "1";
        Mockito.doReturn(1L).when(sysUserMapper).selectCount(ArgumentMatchers.any());
        isUnique = sysUserService.checkUserNameUnique("xielibo");
        Assertions.assertEquals(validNotUnique, isUnique);
    }

    @Test
    public void testCheckPhoneUnique(){
        SysUser user = new SysUser();
        user.setPhonenumber("13866886688");

        String validUnique = "1";
        Mockito.doReturn(mockSysUser()).when(sysUserMapper).selectOne(ArgumentMatchers.any());
        String isUnique = sysUserService.checkPhoneUnique(user);
        Assertions.assertEquals(validUnique, isUnique);

        String validNotUnique = "0";
        user.setUserId(100);
        Mockito.doReturn(mockSysUser()).when(sysUserMapper).selectOne(ArgumentMatchers.any());
        isUnique = sysUserService.checkPhoneUnique(user);
        Assertions.assertEquals(validNotUnique, isUnique);
    }

    @Test
    public void testCheckEmailUnique(){
        SysUser user = new SysUser();
        user.setEmail("xielibo22@163.com");

        String validUnique = "1";
        Mockito.doReturn(mockSysUser()).when(sysUserMapper).selectOne(ArgumentMatchers.any());
        String isUnique = sysUserService.checkEmailUnique(user);
        Assertions.assertEquals(validUnique, isUnique);

        String validNotUnique = "0";
        user.setUserId(100);
        Mockito.doReturn(mockSysUser()).when(sysUserMapper).selectOne(ArgumentMatchers.any());
        isUnique = sysUserService.checkEmailUnique(user);
        Assertions.assertEquals(validNotUnique, isUnique);
    }

    @Test
    public void testInsertUser(){
        int validRow = 1;
        Mockito.doReturn(validRow).when(sysUserMapper).insert(ArgumentMatchers.any());
        Mockito.doReturn(1).when(userRoleMapper).insert(ArgumentMatchers.any());
        int row = sysUserService.insertUser(mockSysUser());
        Assertions.assertEquals(validRow, row);
    }


    @Test
    public void testUpdateUserStatus(){
        int validRow = 1;
        Mockito.doReturn(validRow).when(sysUserMapper).updateById(ArgumentMatchers.any());
        int row = sysUserService.updateUser(mockSysUser());
        Assertions.assertEquals(validRow, row);
    }

    @Test
    public void testUpdateUserProfile(){
        int validRow = 1;
        Mockito.doReturn(validRow).when(sysUserMapper).updateById(ArgumentMatchers.any());
        int row = sysUserService.updateUserProfile(mockSysUser());
        Assertions.assertEquals(validRow, row);
    }

    @Test
    public void testDeleteUserById(){
        int expected = 0;
        Mockito.doReturn(expected).when(sysUserMapper).deleteById(ArgumentMatchers.any());
        Mockito.doReturn(1).when(userRoleMapper).delete(ArgumentMatchers.any());
        int row = sysUserService.deleteUserById(ArgumentMatchers.any());
        Assertions.assertEquals(expected, row);
    }


}
