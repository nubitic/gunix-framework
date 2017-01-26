package org.activiti.engine.impl;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.activiti.engine.impl.variable.VariableTypes;

public class GunixVariableProcessInstanceQuery extends ProcessInstanceQueryImpl {
	private static final long serialVersionUID = 1L;
	List<QueryVariableValue> variables = new ArrayList<QueryVariableValue>();

	public GunixVariableProcessInstanceQuery(CommandExecutor commandExecutor) {
		super(commandExecutor);
	}

	public List<QueryVariableValue> getVariables() {
		return variables;
	}

	public void setVariables(List<QueryVariableValue> variables) {
		this.variables = variables;
	}

	public GunixVariableProcessInstanceQuery variableNameLikeValueEquals(String variableName, Object variableValue) {
		variables.add(new QueryVariableValue(variableName, variableValue, QueryOperator.EQUALS, true));
		return this;
	}

	public void inicializaVariables() {
		ensureVariablesInitialized();
		if (!variables.isEmpty()) {
			VariableTypes variableTypes = Context.getProcessEngineConfiguration().getVariableTypes();
			for (QueryVariableValue queryVariableValue : variables) {
				queryVariableValue.initialize(variableTypes);
			}
		}
	}

	public GunixVariableProcessInstanceQuery variableNameLikeValueLike(String varName, String varValue) {
		variables.add(new QueryVariableValue(varName, varValue, QueryOperator.LIKE, true));
		return this;
	}
}
