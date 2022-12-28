package org.opengauss.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.opengauss.admin.system.domain.SysWhiteList;
import org.opengauss.admin.system.mapper.SysWhiteListMapper;
import org.opengauss.admin.system.service.ISysWhiteListService;
import org.opengauss.admin.system.service.impl.SysWhiteListServiceImpl;
import org.junit.*;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @className: SysWhileListTest
 * @description: SysWhiteListTest
 * @author: xielibo
 * @date: 2022-10-16 13:24
 **/
@RunWith(SpringRunner.class)
public class SysWhiteListTest{

    @InjectMocks
    private ISysWhiteListService sysWhiteListService = new SysWhiteListServiceImpl();

    @Mock
    private SysWhiteListMapper sysWhiteListMapper;

    private List<SysWhiteList> mockSysWhiteLists(){
        SysWhiteList whiteList = new SysWhiteList();
        whiteList.setIpList("192.168.0.21,192.168.0.22");
        whiteList.setTitle("test");
        return List.of(whiteList);
    }

    @BeforeClass
    public static void before(){
        MockitoAnnotations.initMocks(SysWhiteListTest.class);
        System.out.println("start white list test........");
    }
    @AfterClass
    public static void after(){
        System.out.println("end white list test........");
    }

    @Test
    public void testPageSysWhiteList(){
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
    public void testListSysWhiteList(){
        Mockito.doReturn(mockSysWhiteLists()).when(sysWhiteListMapper)
                .selectWhiteListList(ArgumentMatchers.any(SysWhiteList.class));
        List<SysWhiteList> list = sysWhiteListService.selectListAll(new SysWhiteList());
        Assertions.assertNotNull(list);
        Assertions.assertTrue(list.size() > 0);
        Assertions.assertArrayEquals(mockSysWhiteLists().toArray(),list.toArray());
    }

}
