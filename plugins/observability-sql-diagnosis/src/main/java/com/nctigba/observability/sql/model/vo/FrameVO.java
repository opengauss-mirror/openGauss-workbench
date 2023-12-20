/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  FrameVO.java
 *
 *  IDENTIFICATION
 *  plugins/observability-sql-diagnosis/src/main/java/com/nctigba/observability/sql/model/vo/FrameVO.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.sql.model.vo;

import java.util.HashMap;
import java.util.Map;

import com.nctigba.observability.sql.enums.FrameTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class FrameVO {
	FrameTypeEnum type;
	Object data;
	Map<bearing, FrameVO> child;

	public FrameVO(FrameTypeEnum type) {
		this.type = type;
	}

	public FrameVO addChild(bearing s, FrameVO f) {
		if (child == null) {
			type = FrameTypeEnum.Frame;
			child = new HashMap<>();
			for (bearing b : bearing.values()) {
				child.put(b, new FrameVO());
			}
		}
		child.put(s, f);
		return this;
	}

	public enum bearing {
		top, center, bottom
	}
}