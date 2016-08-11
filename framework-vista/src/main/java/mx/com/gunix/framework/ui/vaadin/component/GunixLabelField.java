package mx.com.gunix.framework.ui.vaadin.component;

import java.util.Collection;
import java.util.Locale;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.converter.ConverterUtil;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;

public class GunixLabelField extends Label implements Field<String> {
	private static final long serialVersionUID = 1L;
	private TextField innerField;

	@Override
	public void focus() {
		super.focus();
	}

	@Override
	public boolean isInvalidCommitted() {
		return innerField.isInvalidCommitted();
	}

	@Override
	public void setInvalidCommitted(boolean isCommitted) {
		innerField.setInvalidCommitted(isCommitted);
	}

	@Override
	public void commit() throws SourceException, InvalidValueException {
		innerField.commit();
	}

	@Override
	public void discard() throws SourceException {
		innerField.discard();
	}

	@Override
	public void setBuffered(boolean buffered) {
		innerField.setBuffered(buffered);
	}

	@Override
	public boolean isBuffered() {
		return innerField.isBuffered();
	}

	@Override
	public boolean isModified() {
		return innerField.isModified();
	}

	@Override
	public void addValidator(Validator validator) {
		innerField.addValidator(validator);
	}

	@Override
	public void removeValidator(Validator validator) {
		innerField.removeValidator(validator);
	}

	@Override
	public void removeAllValidators() {
		innerField.removeAllValidators();
	}

	@Override
	public Collection<Validator> getValidators() {
		return innerField.getValidators();
	}

	@Override
	public boolean isValid() {
		return innerField.isValid();
	}

	@Override
	public void validate() throws InvalidValueException {
		innerField.validate();
	}

	@Override
	public boolean isInvalidAllowed() {
		return innerField.isInvalidAllowed();
	}

	@Override
	public void setInvalidAllowed(boolean invalidValueAllowed) throws UnsupportedOperationException {
		innerField.setInvalidAllowed(invalidValueAllowed);
	}

	@Override
	public int getTabIndex() {
		return innerField.getTabIndex();
	}

	@Override
	public void setTabIndex(int tabIndex) {
		innerField.setTabIndex(tabIndex);
	}

	@Override
	public boolean isRequired() {
		return innerField.isRequired();
	}

	@Override
	public void setRequired(boolean required) {
		innerField.setRequired(required);
	}

	@Override
	public void setRequiredError(String requiredMessage) {
		innerField.setRequiredError(requiredMessage);
	}

	@Override
	public String getRequiredError() {
		return innerField.getRequiredError();
	}

	@Override
	public boolean isEmpty() {
		return innerField.isEmpty();
	}

	@Override
	public void clear() {
		innerField.clear();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void setValue(String newStringValue) {
		try {
			super.setValue(newStringValue);
		} catch (IllegalStateException ignorar) {
			Property prop = getPropertyDataSource();
			if (prop != null) {
				getPropertyDataSource().setValue(prop.getType().equals(String.class) ? 
													newStringValue 
												: getConverter() == null ? 
														ConverterUtil.getConverter(String.class, prop.getType(), null).convertToModel(newStringValue, prop.getType(), Locale.getDefault()) 
													: getConverter().convertToModel(newStringValue, prop.getType(), Locale.getDefault()));
			}
		}
		if (innerField == null) {
			innerField = new TextField();
		}
		innerField.setValue(newStringValue);
		markAsDirty();
	}

	@Override
	public String getValue() {
		// TODO Auto-generated method stub
		return super.getValue();
	}

}
