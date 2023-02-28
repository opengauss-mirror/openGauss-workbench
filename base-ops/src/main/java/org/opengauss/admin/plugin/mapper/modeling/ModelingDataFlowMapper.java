package org.opengauss.admin.plugin.mapper.modeling;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.opengauss.admin.plugin.domain.entity.modeling.ModelingDataFlowEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author LZW
* @description modeling_data_flow
* @createDate 2022-08-10 11:41:13
* @Entity org.opengauss.admin.plugin.domain.modeling.dataflow.system.ModelingDataFlow
*/
@Mapper
public interface ModelingDataFlowMapper extends BaseMapper<ModelingDataFlowEntity> {
    @Select("SELECT * FROM modeling_data_flow WHERE name = #{name}")
    List<ModelingDataFlowEntity> findByName(String name);
}




