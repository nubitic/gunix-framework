package mx.com.gunix.framework.ui.vaadin.component;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;

public class GunixTableFieldFactory extends DefaultFieldFactory {
	private static final long serialVersionUID = 1L;
	private Map<String,Field<?>> previouslyCreatedFieldsMap = new HashMap<String,Field<?>>(); 
	
	@Override
	public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
		
		String fieldId = new StringBuilder(String.valueOf(System.identityHashCode(container)))
				.append("-")
				.append(String.valueOf(System.identityHashCode(itemId)))
				.append("-")
				.append(String.valueOf(System.identityHashCode(propertyId)))
				.append("-")
				.append(String.valueOf(System.identityHashCode(uiContext)))
				.toString();
		Field<?> field = previouslyCreatedFieldsMap.get(fieldId);
		if(field==null) {
			field = super.createField(container, itemId, propertyId, uiContext);
			previouslyCreatedFieldsMap.put(fieldId, field);
		}
		return field;
	}

}
