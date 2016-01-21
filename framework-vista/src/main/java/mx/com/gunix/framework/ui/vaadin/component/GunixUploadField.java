package mx.com.gunix.framework.ui.vaadin.component;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Arrays;

import org.vaadin.easyuploads.GunixFileBuffer;
import org.vaadin.easyuploads.UploadField;

import com.vaadin.data.Property;
import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.UserError;
import com.vaadin.ui.Upload;

public class GunixUploadField extends UploadField {
	private static final long serialVersionUID = 1L;
	private boolean readOnly;

	public GunixUploadField() {
		super();
		init();
	}

	@Override
	public void validate() throws InvalidValueException {
		try {
			setComponentError(null);
			super.validate();
		} catch (InvalidValueException reThrow) {
			if (reThrow instanceof EmptyValueException) {
				setComponentError(new UserError((null == getRequiredError() || "".equals(getRequiredError())) ? "no puede ser null" : getRequiredError()));
			} else {
				if (reThrow.getCauses() != null) {
					StringBuilder errorString = new StringBuilder();
					Arrays.stream(reThrow.getCauses()).forEach(ive -> {
						errorString.append(ive.getMessage()).append("\n");
					});
					setComponentError(new UserError(errorString.toString()));
				}
			}
			throw reThrow;
		}
	}

	public GunixUploadField(StorageMode mode) {
		super(mode);
		init();
	}

	private void init() {
		try {
			Field receiverField = UploadField.class.getDeclaredField("receiver");
			receiverField.setAccessible(true);
			GunixFileBuffer receiver = new GunixFileBuffer(this);
			receiverField.set(this, receiver);
			Field uploadField = UploadField.class.getDeclaredField("upload");
			uploadField.setAccessible(true);
			Upload upload = (Upload) uploadField.get(this);
			upload.setReceiver(receiver);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		setButtonCaption("Cargar");
		setValidationVisible(true);
	}

	@Override
	protected String getDeleteCaption() {
		return "Eliminar";
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setPropertyDataSource(Property newDataSource) {
		Transactional propertyProxy = (Transactional) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { Transactional.class, ValueChangeNotifier.class,
				ReadOnlyStatusChangeNotifier.class }, (proxy, method, args) -> {
			Object result = null;
			if (method.getName().equals("getType")) {
				result = File.class;
			} else {
				result = method.invoke(newDataSource, args);
			}
			return result;
		});
		super.setPropertyDataSource(propertyProxy);
	}

	@Override
	protected void updateDisplay() {
		if (readOnly) {
			getRootLayout().removeAllComponents();
			updateDisplayComponent();
		} else {
			super.updateDisplay();
		}
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;

		updateDisplay();

		super.setReadOnly(readOnly);
	}

	@Override
	protected String getDisplayDetails() {
		StringBuilder sb = new StringBuilder();

		sb.append("<em>");
		Object value = getValue();
		String string = value == null ? null : value.toString();
		if (string.length() > 200) {
			string = string.substring(0, 199) + "...";
		}
		sb.append(string);
		sb.append("</em>");
		return sb.toString();
	}
}
