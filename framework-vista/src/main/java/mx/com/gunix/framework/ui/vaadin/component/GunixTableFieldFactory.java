package mx.com.gunix.framework.ui.vaadin.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.vaadin.data.Container;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;

public class GunixTableFieldFactory extends DefaultFieldFactory {
	private static final long serialVersionUID = 1L;
	private Map<String,GunixFieldPropertyRel> previouslyCreatedFieldsMap = new HashMap<String,GunixFieldPropertyRel>();
	
	public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
		String fieldId = generateId(uiContext, container, itemId, propertyId);
		GunixFieldPropertyRel gfpr = previouslyCreatedFieldsMap.get(fieldId);
		if (gfpr == null) {
			Field<?> field = null;
			field = super.createField(container, itemId, propertyId, uiContext);
			field.setInvalidAllowed(false);
			if (field instanceof AbstractTextField) {
				((AbstractTextField) field).setNullRepresentation("");
			}
			gfpr = new GunixFieldPropertyRel();
			gfpr.setField(field);
			gfpr.setPropertyId(propertyId);
			previouslyCreatedFieldsMap.put(fieldId, gfpr);
		}
		return gfpr.getField();
	}
	
	private String generateId(Component uiContext, Container container, Object itemId, Object propertyId){
		return new StringBuilder(String.valueOf(System.identityHashCode(uiContext)))
					.append("-")
					.append(String.valueOf(System.identityHashCode(container)))
					.append("-")
					.append(String.valueOf(System.identityHashCode(itemId)))
					.append("-")
					.append(propertyId!=null?String.valueOf(System.identityHashCode(propertyId)):"")
					.toString();
	}
	
	public List<GunixFieldPropertyRel> getFieldsBy(Component uiContext, Container container, Object itemId) {
		String fieldIds = generateId(uiContext, container, itemId, null);
		List<GunixFieldPropertyRel> campos = previouslyCreatedFieldsMap.entrySet()
														   .stream()
														   .filter(entry -> entry.getKey().startsWith(fieldIds))
														   .map(entry -> {
															   return entry.getValue();
															   })
														   .collect(Collectors.toCollection(() -> {
															   return new ArrayList<GunixFieldPropertyRel>();
															   }));
		return campos;
	}
	
	public static class GunixFieldPropertyRel implements Serializable {
		private static final long serialVersionUID = 1L;

		private Field<?> field;
		private Object propertyId;

		public Field<?> getField() {
			return field;
		}

		public void setField(Field<?> field) {
			this.field = field;
		}

		public Object getPropertyId() {
			return propertyId;
		}

		public void setPropertyId(Object propertyId) {
			this.propertyId = propertyId;
		}

	}
}
