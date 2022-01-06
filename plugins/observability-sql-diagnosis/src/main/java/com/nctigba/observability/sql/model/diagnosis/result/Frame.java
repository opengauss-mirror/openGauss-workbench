package com.nctigba.observability.sql.model.diagnosis.result;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class Frame {
	FrameType type;
	Object data;
	Map<bearing, Frame> child;

	public Frame(FrameType type) {
		this.type = type;
	}

	public Frame addChild(bearing s, Frame f) {
		if (child == null) {
			type = FrameType.Frame;
			child = new HashMap<>();
			for (bearing b : bearing.values()) {
				child.put(b, new Frame());
			}
		}
		child.put(s, f);
		return this;
	}

	public enum bearing {
		top, center, bottom
	}
}