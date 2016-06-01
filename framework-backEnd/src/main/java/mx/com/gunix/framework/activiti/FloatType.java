package mx.com.gunix.framework.activiti;

import org.activiti.engine.impl.variable.ValueFields;
import org.activiti.engine.impl.variable.VariableType;

public class FloatType implements VariableType {
	public String getTypeName() {
		return "float";
	}

	public boolean isCachable() {
		return true;
	}

	@Override
	public Object getValue(ValueFields valueFields) {
		Double d = valueFields.getDoubleValue();
		return d != null ? d.floatValue() : null;
	}

	@Override
	public void setValue(Object value, ValueFields valueFields) {
		valueFields.setDoubleValue(value != null ? ((Float) value).doubleValue() : null);
	}

	@Override
	public boolean isAbleToStore(Object value) {
		if (value == null) {
			return false;
		}
		return Float.class.isAssignableFrom(value.getClass());
	}

}
