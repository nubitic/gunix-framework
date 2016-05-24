package mx.com.gunix.framework.config.aspects;

import static mx.com.gunix.framework.service.ActivitiService.VOLATIL;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.activiti.engine.EngineServices;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import mx.com.gunix.framework.activiti.GunixObjectVariableType;

@Aspect
@Component
public class ActivitiDelegateExecutionAroundAspect {
	@Around("execution(* mx.com.gunix.framework.service.GunixActivitServiceSupport+.*(..,org.activiti.engine.delegate.DelegateExecution,..))")
	public Object aroundAnyMethodInsideAClassThatExtendsGunixActivitServiceSupportAndHaveOneDelegateExecutionParameter(ProceedingJoinPoint pjp) throws Throwable {
		Object[] orgArgs = pjp.getArgs();
		Object[] newArgs = new Object[orgArgs.length];
		System.arraycopy(orgArgs, 0, newArgs, 0, orgArgs.length);

		for (int i = 0; i < newArgs.length; i++) {
			if (newArgs[i] instanceof DelegateExecution) {
				DelegateExecution deO = (DelegateExecution) newArgs[i];
				newArgs[i] = new DelegateExecution() {
					private DelegateExecution de = deO;

					@Override
					public Map<String, Object> getVariables() {
						return de.getVariables();
					}

					@Override
					public Map<String, Object> getVariables(Collection<String> variableNames) {
						return de.getVariables(variableNames);
					}

					@Override
					public Map<String, Object> getVariables(Collection<String> variableNames, boolean fetchAllVariables) {
						return de.getVariables(variableNames, fetchAllVariables);
					}

					@Override
					public Map<String, Object> getVariablesLocal() {
						return de.getVariablesLocal();
					}

					@Override
					public Map<String, Object> getVariablesLocal(Collection<String> variableNames) {
						return de.getVariablesLocal(variableNames);
					}

					@Override
					public Map<String, Object> getVariablesLocal(Collection<String> variableNames, boolean fetchAllVariables) {
						return de.getVariables(variableNames, fetchAllVariables);
					}

					@Override
					public Object getVariable(String variableName) {
						return de.getVariable(variableName);
					}

					@Override
					public Object getVariable(String variableName, boolean fetchAllVariables) {
						return de.getVariable(variableName, fetchAllVariables);
					}

					@Override
					public Object getVariableLocal(String variableName) {
						return de.getVariableLocal(variableName);
					}

					@Override
					public Object getVariableLocal(String variableName, boolean fetchAllVariables) {
						return de.getVariableLocal(variableName, fetchAllVariables);
					}

					@Override
					public <T> T getVariable(String variableName, Class<T> variableClass) {
						return de.getVariable(variableName, variableClass);
					}

					@Override
					public <T> T getVariableLocal(String variableName, Class<T> variableClass) {
						return de.getVariableLocal(variableName, variableClass);
					}

					@Override
					public Set<String> getVariableNames() {
						return de.getVariableNames();
					}

					@Override
					public Set<String> getVariableNamesLocal() {
						return de.getVariableNamesLocal();
					}

					@Override
					public void setVariable(String variableName, Object value) {
						withPresuntoVolatil(valueSer -> {
							de.setVariable(variableName, valueSer);
							return null;
						} , (Serializable) value);
					}

					@Override
					public void setVariable(String variableName, Object value, boolean fetchAllVariables) {
						withPresuntoVolatil(valueSer -> {
							de.setVariable(variableName, valueSer, fetchAllVariables);
							return null;
						} , (Serializable) value);
					}

					@Override
					public Object setVariableLocal(String variableName, Object value) {
						return withPresuntoVolatil(valueSer -> {
							return de.setVariableLocal(variableName, valueSer);
						} , (Serializable) value);
					}

					@Override
					public Object setVariableLocal(String variableName, Object value, boolean fetchAllVariables) {
						return withPresuntoVolatil(valueSer -> {
							return de.setVariableLocal(variableName, valueSer, fetchAllVariables);
						} , (Serializable) value);
					}

					@Override
					public void setVariables(Map<String, ? extends Object> variables) {
						if (variables != null && !variables.isEmpty()) {
							variables.keySet().forEach(varName -> {
								withPresuntoVolatil(valueSer -> {
									de.setVariable(varName, valueSer);
									return null;
								} , (Serializable) variables.get(varName));
							});
						}
					}

					@Override
					public void setVariablesLocal(Map<String, ? extends Object> variables) {
						if (variables != null && !variables.isEmpty()) {
							variables.keySet().forEach(varName -> {
								withPresuntoVolatil(valueSer -> {
									de.setVariableLocal(varName, valueSer);
									return null;
								} , (Serializable) variables.get(varName));
							});
						}
					}

					@Override
					public void createVariableLocal(String variableName, Object value) {
						withPresuntoVolatil(valueSer -> {
							de.createVariableLocal(variableName, valueSer);
							return null;
						} , (Serializable) value);
					}

					@Override
					public boolean hasVariables() {
						return de.hasVariables();
					}

					@Override
					public boolean hasVariablesLocal() {
						return de.hasVariablesLocal();
					}

					@Override
					public boolean hasVariable(String variableName) {
						return de.hasVariable(variableName);
					}

					@Override
					public boolean hasVariableLocal(String variableName) {
						return de.hasVariableLocal(variableName);
					}

					@Override
					public void removeVariable(String variableName) {
						de.removeVariable(variableName);
					}

					@Override
					public void removeVariableLocal(String variableName) {
						de.removeVariableLocal(variableName);
					}

					@Override
					public void removeVariables(Collection<String> variableNames) {
						de.removeVariables(variableNames);
					}

					@Override
					public void removeVariablesLocal(Collection<String> variableNames) {
						de.removeVariablesLocal(variableNames);
					}

					@Override
					public void removeVariables() {
						de.removeVariables();
					}

					@Override
					public void removeVariablesLocal() {
						de.removeVariablesLocal();
					}

					@Override
					public String getId() {
						return de.getId();
					}

					@Override
					public String getProcessInstanceId() {
						return de.getProcessInstanceId();
					}

					@Override
					public String getEventName() {
						return de.getEventName();
					}

					@SuppressWarnings("deprecation")
					@Override
					public String getBusinessKey() {
						return de.getBusinessKey();
					}

					@Override
					public String getProcessBusinessKey() {
						return de.getProcessBusinessKey();
					}

					@Override
					public String getProcessDefinitionId() {
						return de.getProcessDefinitionId();
					}

					@Override
					public String getParentId() {
						return de.getParentId();
					}

					@Override
					public String getSuperExecutionId() {
						return de.getSuperExecutionId();
					}

					@Override
					public String getCurrentActivityId() {
						return de.getCurrentActivityId();
					}

					@Override
					public String getCurrentActivityName() {
						return de.getCurrentActivityName();
					}

					@Override
					public String getTenantId() {
						return de.getTenantId();
					}

					@Override
					public EngineServices getEngineServices() {
						return de.getEngineServices();
					}

					private <S extends Serializable> Object withPresuntoVolatil(Function<S, Object> function, S varValue) {
						if (VOLATIL.equals(((ProcessDefinitionEntity) ((ExecutionEntity) de).getProcessDefinition()).getCategory())) {
							return function.apply(varValue);
						} else {
							GunixObjectVariableType.setCurrentVar(de.getProcessInstanceId(), varValue);
							try {
								return function.apply(varValue);
							} finally {
								GunixObjectVariableType.removeCurrentVar();
							}
						}
					}
				};
				break;
			}
		}

		return pjp.proceed(newArgs);
	}
}
