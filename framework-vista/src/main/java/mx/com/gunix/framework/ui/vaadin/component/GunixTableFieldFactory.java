package mx.com.gunix.framework.ui.vaadin.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import mx.com.gunix.framework.util.CustomLabelEnum;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.Converter.ConversionException;
import com.vaadin.server.ErrorMessage;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;

public class GunixTableFieldFactory extends DefaultFieldFactory {
	private static final long serialVersionUID = 1L;
	private Map<String,GunixFieldPropertyRel> previouslyCreatedFieldsMap = new HashMap<String,GunixFieldPropertyRel>();
	private Map<String, FieldBuilder> customFieldBuilder = new HashMap<String, FieldBuilder>();
	
	@SuppressWarnings("unchecked")
	public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
		String fieldId = generateId(uiContext, container, itemId, propertyId);
		GunixFieldPropertyRel gfpr = previouslyCreatedFieldsMap.get(fieldId);
		if (gfpr == null) {
			Field<?> field = null;
			Class<?> fieldType = container.getContainerProperty(itemId, propertyId).getType();
			FieldBuilder fb = customFieldBuilder.get(propertyId);
			if(fb==null){
				if (Date.class.isAssignableFrom(fieldType)) {
					final DateField df = new DateField() {
						private static final long serialVersionUID = 1L;
	
						@Override
						protected Date handleUnparsableDateString(String dateString) throws ConversionException {
							try {
								return super.handleUnparsableDateString(dateString);
							} catch (Converter.ConversionException reThrow) {
								getErrorHandler().error(new ConnectorErrorEvent(this ,new Validator.InvalidValueException(reThrow.getMessage())));
								throw reThrow;
							}
						}
	
						@Override
						public ErrorMessage getErrorMessage() {
							if (!(GunixViewErrorHandler.getCurrent().isInvalidValueComponent(this))) {
								return super.getErrorMessage();
							} else {
								return getComponentError();
							}
						}
					};
					df.setResolution(Resolution.DAY);
					df.setParseErrorMessage("Fecha inválida");
					field = df;
				} else {
					if (fieldType.isEnum()) {
						ComboBox cbField = new ComboBox();
						cbField.addContainerProperty("caption", String.class, "");
						cbField.setItemCaptionPropertyId("caption");
						boolean isCustomLabelEnum = CustomLabelEnum.class.isAssignableFrom(fieldType); 
						Arrays.asList(fieldType.getEnumConstants()).forEach(enumConstant -> {
							Item it = cbField.addItem(enumConstant);
							String label = null;
							if (isCustomLabelEnum) {
								label = ((CustomLabelEnum) enumConstant).getLabel();
							} else {
								label = enumConstant.toString();
							}
							it.getItemProperty("caption").setValue(label);
						});
						field = cbField;
					} else {
						if (String.class.isAssignableFrom(fieldType)) {
							field = new GunixLabelField();
						} else {
							field = super.createField(container, itemId, propertyId, uiContext);
						}
					}
				}
			} else {
				field = fb.build(new SiblingFieldGetter(container, itemId, uiContext));
			}
			field.setInvalidAllowed(false);
			if (field instanceof AbstractTextField) {
				((AbstractTextField) field).setNullRepresentation("");
			}
			field.setErrorHandler(GunixViewErrorHandler.getCurrent());
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
	
	public void addCustomFieldBuilder(String propertyId, FieldBuilder builder){
		customFieldBuilder.put(propertyId, builder);
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
	
	public interface FieldBuilder extends Serializable{
		Field<?> build(SiblingFieldGetter sfg);
	}
	
	public final class SiblingFieldGetter {
		private Container container;
		private Object itemId;
		private Component uiContext;

		public SiblingFieldGetter(Container container, Object itemId, Component uiContext) {
			this.container = container;
			this.itemId = itemId;
			this.uiContext = uiContext;
		}

		public Field<?> getFieldBy(String propertyId) {
			Field<?> field = null;
			GunixFieldPropertyRel gfpr = previouslyCreatedFieldsMap.get(generateId(uiContext, container, itemId, propertyId));
			if (gfpr != null) {
				field = gfpr.getField();
			}
			return field;
		}
	}
}
