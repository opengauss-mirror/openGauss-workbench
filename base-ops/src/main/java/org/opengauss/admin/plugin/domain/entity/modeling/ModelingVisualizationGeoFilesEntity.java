package org.opengauss.admin.plugin.domain.entity.modeling;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.opengauss.admin.plugin.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author LZW
 * @TableName modeling_visualization_geo_files
 * @date 2022/10/29 22:38
 */
@Data
@EqualsAndHashCode(callSuper=true)
@TableName(value ="modeling_visualization_geo_files")
public class ModelingVisualizationGeoFilesEntity extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * File name
     */
    private String name;

    /**
     * http url if exist
     */
    private String filePath;

    /**
     * geo full info json
     */
    private String geoJson;

    /**
     * register map name
     */
    private String registerName;

    /**
     * belongs to one dataflow
     */
    private Long dataFlowId;

    private Object sortId;

}
