package org.opengauss.admin.plugin.service.ops;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.opengauss.admin.plugin.domain.entity.ops.OpsWdrEntity;
import org.opengauss.admin.plugin.domain.model.ops.WdrGeneratorBody;
import org.opengauss.admin.plugin.enums.ops.WdrScopeEnum;
import org.opengauss.admin.plugin.enums.ops.WdrTypeEnum;
import org.opengauss.admin.plugin.vo.ops.DwrSnapshotVO;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * @author lhf
 * @date 2022/10/13 15:13
 **/
public interface IOpsWdrService extends IService<OpsWdrEntity> {
    List<OpsWdrEntity> listWdr(String clusterId, WdrScopeEnum wdrScope, WdrTypeEnum wdrType, String hostId, Date start, Date end);

    void generate(WdrGeneratorBody wdrGeneratorBody);

    List<DwrSnapshotVO> listSnapshot(Page page, String clusterId, String hostId);

    void createSnapshot(String clusterId, String hostId);

    void del(String id);

    void downloadWdr(String wdrId, HttpServletResponse response);
}
