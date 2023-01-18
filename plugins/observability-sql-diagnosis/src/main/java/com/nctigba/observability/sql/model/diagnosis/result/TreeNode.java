package com.nctigba.observability.sql.model.diagnosis.result;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.nctigba.observability.sql.model.diagnosis.result.TaskResult.ResultState;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class TreeNode {
	private String title;
	private ResultType type;
	private List<TreeNode> child;
	private ResultState state;
	private Boolean hidden;

	public TreeNode setState(ResultState resultState) {
		if (this.state == ResultState.Suggestions)
			return this;
		this.state = resultState;
		return this;
	}

	public Boolean getHidden() {
		if (this.hidden != null)
			return this.hidden;
		if (this.state == ResultState.Suggestions)
			return this.hidden = false;
		if (CollectionUtils.isEmpty(child) && state == ResultState.NoAdvice)
			return this.hidden = true;
		if (!CollectionUtils.isEmpty(child))
			for (TreeNode treeNode : child)
				if (!treeNode.getHidden())
					return this.hidden = false;
		if (type == ResultType.TaskInfo)
			return this.hidden = false;
		return this.hidden = true;
	}

	public TreeNode(ResultType type) {
		this(type.getText(), type);
	}

	public TreeNode(String title, ResultType type) {
		this.title = title;
		this.type = type;
	}

	public TreeNode appendChild(TreeNode n) {
		if (child == null)
			child = new ArrayList<>();
		child.add(n);
		return this;
	}
}