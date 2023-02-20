package org.opengauss.admin.plugin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.admin.plugin.domain.MigrationTaskModel;
import org.opengauss.admin.plugin.mapper.MigrationTaskModelMapper;
import org.opengauss.admin.plugin.service.MigrationTaskModelService;
import org.springframework.stereotype.Service;

/**
 * @author xielibo
 * @date 2023/01/14 09:01
 **/
@Service
@Slf4j
public class MigrationTaskModelServiceImpl extends ServiceImpl<MigrationTaskModelMapper, MigrationTaskModel> implements MigrationTaskModelService {


}
