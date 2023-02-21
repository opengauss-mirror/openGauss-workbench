package com.nctigba.observability.sql.model.diagnosis.result;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public enum FrameType {
	NONE,
	Frame,
	Flamefigure,
	LineChart,
	DataTree,
	Text,
	Paragraph,
	UL,
	Pie,
	HeatMap,
	Table,
	Suggestion,
	Explain,
	Param;
}