package org.opengauss.admin.service;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.opengauss.admin.common.core.domain.entity.SysMenu;
import org.opengauss.admin.common.core.domain.entity.SysRole;
import org.opengauss.admin.common.core.vo.RouterVo;
import org.opengauss.admin.common.enums.SysLanguage;
import org.opengauss.admin.common.enums.SysMenuRouteOpenPosition;
import org.opengauss.admin.system.mapper.SysMenuMapper;
import org.opengauss.admin.system.mapper.SysRoleMapper;
import org.opengauss.admin.system.service.ISysMenuService;
import org.opengauss.admin.system.service.impl.SysMenuServiceImpl;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @className: SysMenuTest
 * @description: SysMenuTest
 * @author: xielibo
 * @date: 2022-10-16 13:24
 **/
@RunWith(SpringRunner.class)
public class SysMenuTest {

    @InjectMocks
    private ISysMenuService sysMenuService = new SysMenuServiceImpl();

    @Mock
    private SysMenuMapper menuMapper;

    @Mock
    private SysRoleMapper roleMapper;

    private List<SysMenu> mockSysMenus(){
        List<SysMenu> menus = new ArrayList<>();
        menus.add(mockSysMenu());
        SysMenu menu = new SysMenu();
        menu.setMenuId(1001);
        menu.setMenuName("log");
        menu.setParentId(1);
        menu.setPath("index");
        menus.add(menu);
        return menus;
    }
    private SysMenu mockSysMenu(){
        SysMenu menu = new SysMenu();
        menu.setMenuId(1);
        menu.setMenuName("monitor");
        menu.setParentId(0);
        menu.setPath("index");
        return menu;
    }

    @BeforeClass
    public static void before(){
        MockitoAnnotations.initMocks(SysMenuTest.class);
        System.out.println("start system user test........");
    }
    @AfterClass
    public static void after(){
        System.out.println("end system user test........");
    }

    @Test
    public void testSelectMenuList(){
        List<SysMenu> menus = mockSysMenus();
        Mockito.doReturn(menus).when(menuMapper).selectList(ArgumentMatchers.any());
        List<SysMenu> sysMenus = sysMenuService.selectMenuList(new SysMenu(), 1);
        Assertions.assertSame(menus, sysMenus);

        Mockito.doReturn(menus).when(menuMapper).selectMenuListByUserId(ArgumentMatchers.any());
        sysMenus = sysMenuService.selectMenuList(new SysMenu(), 10);
        Assertions.assertSame(menus, sysMenus);
    }

    @Test
    public void testSelectSpecialRouteList(){
        List<SysMenu> menus = mockSysMenus();
        Mockito.doReturn(menus).when(menuMapper).selectList(ArgumentMatchers.any());
        List<SysMenu> sysMenus = sysMenuService.selectSpecialRouteList(SysMenuRouteOpenPosition.INDEX_INSTANCE_DATA.getCode());
        Assertions.assertSame(menus, sysMenus);
    }

    @Test
    public void testExistsMenu() {
        List<SysMenu> menus = mockSysMenus();
        Mockito.doReturn(menus).when(menuMapper).selectList(ArgumentMatchers.any());
        SysMenu menu = sysMenuService.existsMenu(0,"test","index","tool");
        Assertions.assertNotNull(menu);

        menus = new ArrayList<>();
        Mockito.doReturn(menus).when(menuMapper).selectList(ArgumentMatchers.any());
        menu = sysMenuService.existsMenu(0,"test","index","tool");
        Assertions.assertNull(menu);
    }

    @Test
    public void testSelectMenuPermsByUserId(){
        String[] permArr = new String[]{"plugins","users"};
        List<String> perms = Arrays.stream(permArr).collect(Collectors.toList());
        Mockito.doReturn(perms).when(menuMapper).selectMenuPermsByUserId(ArgumentMatchers.any());
        Set<String> strings = sysMenuService.selectMenuPermsByUserId(1);
        Assertions.assertTrue(strings.size() > 0);
    }

    @Test
    public void testSelectMenuTreeByUserId(){
        List<SysMenu> menus = mockSysMenus();
        Mockito.doReturn(menus).when(menuMapper).selectList(ArgumentMatchers.any());
        List<SysMenu> sysMenus = sysMenuService.selectMenuTreeByUserId(1);
        Assertions.assertTrue(sysMenus.size() > 0);
        sysMenus = sysMenuService.selectMenuTreeByUserId(100);
        Assertions.assertTrue(sysMenus.size() == 0);
    }

    @Test
    public void testSelectMenuListByRoleId(){
        List<Integer> menuIds = new ArrayList<>();
        menuIds.add(1);
        menuIds.add(1001);
        SysRole role = new SysRole();
        role.setMenuCheckStrictly(true);
        Mockito.doReturn(role).when(roleMapper).selectRole(ArgumentMatchers.any());
        Mockito.doReturn(menuIds).when(menuMapper).selectMenuListByRoleId(1,role.isMenuCheckStrictly());

        List<Integer> integers = sysMenuService.selectMenuListByRoleId(1);
        Assertions.assertSame(menuIds,integers);
    }

    @Test
    public void testBuildMenus(){
        List<RouterVo> routerVos = sysMenuService.buildMenus(mockSysMenus(), SysLanguage.ZH.getCode());
        Assertions.assertTrue(routerVos.size() > 0);

    }

    @Test
    public void testBuildMenuTree(){
        List<SysMenu> sysMenus = sysMenuService.buildMenuTree(mockSysMenus());
        Assertions.assertTrue(sysMenus.size() > 0);
    }

}
