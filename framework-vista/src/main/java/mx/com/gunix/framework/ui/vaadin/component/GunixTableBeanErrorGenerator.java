package mx.com.gunix.framework.ui.vaadin.component;

import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;

import com.vaadin.ui.AbstractSelect.ItemDescriptionGenerator;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.CellStyleGenerator;

public class GunixTableBeanErrorGenerator<S extends Serializable> implements CellStyleGenerator, ItemDescriptionGenerator {
	private static final long serialVersionUID = 1L;
	private final Map<S, String> beanErrors = new IdentityHashMap<S, String>();

	@SuppressWarnings("unchecked")
	@Override
	public String generateDescription(Component source, Object itemId, Object propertyId) {
		S bean = (S) itemId;
		String errorMessage = null;
		if (propertyId == null && (errorMessage = beanErrors.get(bean)) != null) {
			return errorMessage;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getStyle(Table source, Object itemId, Object propertyId) {
		S bean = (S) itemId;

		if (propertyId == null && beanErrors.get(bean) != null) {
			return "bean-error";
		}
		return null;
	}

	public void addBeanError(S leafBean, String message) {
		beanErrors.put(leafBean, message);
	}

}
