package com.nctigba.observability.instance.entity;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * @author lhf
 * @date 2022/10/13 15:03
 **/
@Data
@TableName("ops_wdr")
public class OpsWdrEntity {
	@TableId
	private String wdrId;
	private WdrScopeEnum scope;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date reportAt;
	private WdrTypeEnum reportType;
	private String reportName;
	private String reportPath;
	private String clusterId;
	private String nodeId;
	private String hostId;
	private String userId;
	private String startSnapshotId;
	private String endSnapshotId;

	/**
	 * search value
	 */
	@TableField(exist = false)
	private String searchValue;

	/**
	 * creator
	 */
	@TableField(fill = FieldFill.INSERT)
	private String createBy;

	/**
	 * create time
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@TableField(fill = FieldFill.INSERT)
	private Date createTime;

	/**
	 * updater
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private String updateBy;

	/**
	 * update time
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date updateTime;

	/**
	 * remark
	 */
	private String remark;

	@TableField(exist = false)
	private Map<String, Object> params;

	public Map<String, Object> getParams() {
		if (params == null) {
			params = new HashMap<>(4);
		}
		return params;
	}

	public enum WdrScopeEnum {
		CLUSTER,
		NODE;
	}

	public enum WdrTypeEnum {
		DETAIL,
		SUMMARY,
		ALL;
	}
}