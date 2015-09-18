package mx.com.gunix.framework.activiti;

import org.activiti.engine.impl.variable.DoubleType;
import org.activiti.engine.impl.variable.ValueFields;

public class FloatType extends DoubleType {

	@Override
	public Object getValue(ValueFields valueFields) {
		return ((Double) super.getValue(valueFields)).floatValue();
	}

	@Override
	public void setValue(Object value, ValueFields valueFields) {
		super.setValue(((Float) value).doubleValue(), valueFields);
	}

	@Override
	public boolean isAbleToStore(Object value) {
		if (value == null) {
			return false;
		}
		return Float.class.isAssignableFrom(value.getClass());
	}

}
