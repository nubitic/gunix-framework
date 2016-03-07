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
	private GunixFileBuffer receiver;
	private String acceptFiler;
	private int maxFileSize;
	private static Field uploadField;
	private static Field html5FileInputSettingsField;
	private static Field receiverField;
	static {
		try {
			uploadField = UploadField.class.getDeclaredField("upload");
			uploadField.setAccessible(true);

			html5FileInputSettingsField = UploadField.class.getDeclaredField("html5FileInputSettings");
			html5FileInputSettingsField.setAccessible(true);

			receiverField = UploadField.class.getDeclaredField("receiver");
			receiverField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

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

	@Override
	public void clear() {
		Upload upload = new Upload(null, receiver);
        upload.setImmediate(true);
        upload.addStartedListener(this);
        upload.addFinishedListener(this);
        upload.addProgressListener(this);
        upload.setButtonCaption(getButtonCaption());
        
        try {
        	Upload currUpload = (Upload) uploadField.get(this);
        	getRootLayout().replaceComponent(currUpload, upload);
			uploadField.set(this, upload);
			html5FileInputSettingsField.set(this, null);
			setAcceptFilter(acceptFiler);
			if (maxFileSize > 0) {
				setMaxFileSize(maxFileSize);
			}
			super.clear();
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
        
		markAsDirty();
	}

	@Override
	public void setAcceptFilter(String acceptString) {
		super.setAcceptFilter(acceptString);
		this.acceptFiler=acceptString;
	}

	@Override
	public void setMaxFileSize(int maxFileSize) {
		super.setMaxFileSize(maxFileSize);
		this.maxFileSize=maxFileSize;
	}

	public GunixUploadField(StorageMode mode) {
		super(mode);
		init();
	}

	private void init() {
		try {
			receiver = new GunixFileBuffer(this);
			receiverField.set(this, receiver);

			Upload upload = (Upload) uploadField.get(this);
			upload.setReceiver(receiver);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		setButtonCaption("Cargar");
		setValidationVisible(true);
		setFileDeletesAllowed(false);
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
