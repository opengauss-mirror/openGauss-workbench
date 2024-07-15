import org.junit.jupiter.api.Test;
import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.common.utils.http.HttpUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpUtilTest {
    @Test
    void checkBaiduUrl() {
        AjaxResult baiduResult = HttpUtils.checkUrl("https://www.baidu.com");
        System.out.println(baiduResult);
        assertTrue(baiduResult.isOk());
    }

    @Test
    void checkOpenGaussDownUrl() {
        AjaxResult openGaussDownUrlResult = HttpUtils.checkUrl("https://opengauss.obs.cn-south-1.myhuaweicloud.com/5.0.2/x86/openGauss-5.0.2-CentOS-64bit-all.tar.gz");
        System.out.println(openGaussDownUrlResult);
        assertTrue(openGaussDownUrlResult.isOk());
    }

    @Test
    void checkOpenGaussDownErrorUrl() {
        AjaxResult openGaussDownUrlResult2 = HttpUtils.checkUrl("https://opengauss.obs.cn-south-1.myhuaweicloud.com/7.0.5/x86/openGauss-5.0.2-CentOS-64bit-all.tar.gz");
        System.out.println(openGaussDownUrlResult2);
        assertFalse(openGaussDownUrlResult2.isOk());
    }

    @Test
    void checkOpenGaussHome() {
        AjaxResult openGaussDownUrlResult2 = HttpUtils.checkUrl("https://opengauss.org/zh/");
        System.out.println(openGaussDownUrlResult2);
        assertTrue(openGaussDownUrlResult2.isOk());
    }
}
