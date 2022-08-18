package org.opengauss.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.opengauss.admin.common.core.domain.entity.SysRole;
import org.opengauss.admin.common.core.domain.entity.SysUser;
import org.opengauss.admin.common.enums.BusinessType;
import org.opengauss.admin.common.enums.OperatorType;
import org.opengauss.admin.system.domain.SysOperLog;
import org.opengauss.admin.system.mapper.SysOperLogMapper;
import org.opengauss.admin.system.mapper.SysRoleMapper;
import org.opengauss.admin.system.mapper.SysUserMapper;
import org.opengauss.admin.system.mapper.SysUserRoleMapper;
import org.opengauss.admin.system.service.ISysOperLogService;
import org.opengauss.admin.system.service.ISysUserService;
import org.opengauss.admin.system.service.impl.SysOperLogServiceImpl;
import org.opengauss.admin.system.service.impl.SysUserServiceImpl;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @className: SysOperlogTest
 * @description: SysOperlogTest
 * @author: xielibo
 * @date: 2022-10-16 13:24
 **/
@RunWith(SpringRunner.class)
public class SysOperlogTest {

    @InjectMocks
    private ISysOperLogService sysOperLogService = new SysOperLogServiceImpl();

    @Mock
    private SysOperLogMapper operLogMapper;

    private List<SysOperLog> mockSysOperLogs(){
        return List.of(mockSysOperLog());
    }
    private SysOperLog mockSysOperLog(){
        SysOperLog operLog = new SysOperLog();
        operLog.setTitle("update");
        operLog.setMethod("post");
        operLog.setRequestMethod("put");
        return operLog;
    }

    @BeforeClass
    public static void before(){
        MockitoAnnotations.initMocks(SysOperlogTest.class);
        System.out.println("start system user test........");
    }
    @AfterClass
    public static void after(){
        System.out.println("end system user test........");
    }

    @Test
    public void testInsertOperlog(){
        int validRow = 1;
        Mockito.doReturn(validRow).when(operLogMapper).insert(ArgumentMatchers.any());
        sysOperLogService.insertOperlog(mockSysOperLog());
    }

    @Test
    public void testSelectOperLogList(){
        IPage<SysOperLog> page = new Page<>();
        page.setRecords(mockSysOperLogs());
        page.setPages(1);
        page.setCurrent(1);
        page.setTotal(1);
        Mockito.doReturn(page).when(operLogMapper).selectPage(ArgumentMatchers.any(), ArgumentMatchers.any());
        IPage pageResult = sysOperLogService.selectOperLogList(new Page<>(),new SysOperLog());
        Assertions.assertNotNull(pageResult.getRecords());
        Assertions.assertSame(page, pageResult);
    }

    @Test
    public void testSelectOperLogById() {
        SysOperLog operLog = mockSysOperLog();
        Mockito.doReturn(operLog).when(operLogMapper).selectById(ArgumentMatchers.any());
        SysOperLog log = sysOperLogService.selectOperLogById("1");
        Assertions.assertNotNull(log);
        Assertions.assertEquals(operLog, log);
    }

    @Test
    public void testDeleteOperLogByIds(){
        int valid = 1;
        Mockito.doReturn(valid).when(operLogMapper).deleteBatchIds(ArgumentMatchers.any());
        int row = sysOperLogService.deleteOperLogByIds(new String[]{"1","2"});
        Assertions.assertEquals(valid,row);
    }

}
