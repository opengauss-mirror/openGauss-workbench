package com.nctigba.observability.sql.model.diagnosis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nctigba.observability.sql.model.diagnosis.grab.GrabType;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@TableName(value = "diagnosis_resource", autoResultMap = true)
@Accessors(chain = true)
@NoArgsConstructor
public class Resource {
	@TableId(type = IdType.AUTO)
	Integer id;
	Integer taskid;
	@TableField("grabType")
	GrabType grabType;
	byte[] f;

	public void to(OutputStream os) throws IOException {
		IOUtils.copy(new ByteArrayInputStream(f), os);
	}

	public Resource(Task task, GrabType grabType) {
		super();
		this.taskid = task.getId();
		this.grabType = grabType;
	}
}