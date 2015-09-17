package mx.com.gunix.framework.ui.vaadin.component;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import org.vaadin.easyuploads.GunixFileBuffer;
import org.vaadin.easyuploads.UploadField;

import com.vaadin.data.Property;
import com.vaadin.ui.Upload;

public class GunixUploadField extends UploadField {
	private static final long serialVersionUID = 1L;

	public GunixUploadField() {
		super();
		init();
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
		Transactional propertyProxy = (Transactional) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { Transactional.class, ValueChangeNotifier.class, ReadOnlyStatusChangeNotifier.class }, (proxy, method, args) -> {
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

}
