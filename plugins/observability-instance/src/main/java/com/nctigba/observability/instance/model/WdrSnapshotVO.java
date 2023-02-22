package com.nctigba.observability.instance.model;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName(value = "snapshot.snapshot", autoResultMap = true)
public class WdrSnapshotVO {
	@TableId("snapshot_id")
	private Integer snapshotId;
	@TableField("start_ts")
	private Date startTs;
	@TableField("end_ts")
	private Date endTs;
}