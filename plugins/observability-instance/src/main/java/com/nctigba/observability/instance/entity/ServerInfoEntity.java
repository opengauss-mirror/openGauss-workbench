package com.nctigba.observability.instance.entity;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *    ServerInfo
 * </p>
 *
 * @author liudm@vastdata.com.cn
 * @since 2022-09-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("observability.server_info")
public class ServerInfoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String name;

    private String os;

    private String ip;

    private Integer port;

    private String userName;

    private String userPassword;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
