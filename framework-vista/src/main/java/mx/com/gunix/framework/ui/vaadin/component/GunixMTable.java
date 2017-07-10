package mx.com.gunix.framework.ui.vaadin.component;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.vaadin.viritin.ListContainer;
import org.vaadin.viritin.fields.MTable;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.TransactionalPropertyWrapper;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Field;

import mx.com.gunix.framework.ui.vaadin.VaadinUtils;
import mx.com.gunix.framework.ui.vaadin.component.GunixTableFieldFactory.FieldBuilder;

public class GunixMTable<T extends Serializable> extends MTable<T> {
	private static final long serialVersionUID = 1L;
	
	private Class<T> clazz;

	public GunixMTable(Class<T> type) {
		super(type);
		clazz = type;
		init();
	}

	public GunixMTable(Collection<T> beans) {
		super(beans);
		initClazzFromCollection(beans);
		init();
	}

	@SafeVarargs
	public GunixMTable(T... beans) {
		super(beans);
		initClazzFromCollection(Arrays.asList(beans));
		init();
	}
	
	@SuppressWarnings("unchecked")
	private void initClazzFromCollection(Collection<T> beans) {
		if (beans != null && beans.isEmpty()) {
			T bean = beans.stream().filter(b -> true).findFirst().orElse(null);
			if (bean != null) {
				clazz = (Class<T>) bean.getClass();
			}
		}
	}
	
	private void init(){
		setTableFieldFactory(new GunixTableFieldFactory());
	}
	
	@Override
	protected ListContainer<T> createContainer(Class<T> type) {
		return new GunixListContainer(type);
	}

	@Override
	protected ListContainer<T> createContainer(Collection<T> beans) {
		return new GunixListContainer(beans);
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void bindPropertyToField(Object rowId, Object colId, Property property, Field field) {
		super.bindPropertyToField(rowId, colId, property, field);
		if (field instanceof AbstractField) {
			((AbstractField<?>) field).setConversionError(VaadinUtils.getConversionError(property.getType()));
		}
	}
	
	public void addCustomFieldBuilder(String propertyId, FieldBuilder builder){
		((GunixTableFieldFactory)getTableFieldFactory()).addCustomFieldBuilder(propertyId, builder);
	}

	@Override
	public boolean removeAllItems() {
		boolean ans = super.removeAllItems();
		if(ans){
			((GunixTableFieldFactory)getTableFieldFactory()).clearPreviouslyCreatedFieldsMap();
		}
		return ans;
	}

	@Override
	public boolean removeItem(Object itemId) {
		boolean ans = super.removeItem(itemId);
		if(ans){
			((GunixTableFieldFactory)getTableFieldFactory()).removeItemId(itemId);
		}
		return ans;
	}

	class GunixListContainer extends ListContainer<T> {
		private static final long serialVersionUID = 1L;

		public GunixListContainer(Class<T> type, Collection<T> backingList) {
			super(type, backingList);
		}

		public GunixListContainer(Class<T> type) {
			super(type);
		}

		public GunixListContainer(Collection<T> backingList) {
			super(backingList);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Item getItem(Object itemId) {
			if (itemId == null) {
				return null;
			}
			return new DynaBeanItem<T>((T) itemId) {
				private static final long serialVersionUID = 1L;

				@Override
				public Property<Object> getItemProperty(Object id) {
					return new TransactionalPropertyWrapper<Object>(super.getItemProperty(id)) {
						private static final long serialVersionUID = 1L;

						@Override
						public boolean isReadOnly() {
							boolean ans = false;
							try {
								ans = super.isReadOnly();
							} catch (NullPointerException npe) {
								try {
									ans = PropertyUtils.getWriteMethod(PropertyUtils.getPropertyDescriptor(itemId, id.toString())) == null;
								} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
									throw new RuntimeException(e);
								}
							}
							return ans;
						}

						@Override
						public Object getValue() {
							try {
								return super.getValue();
							} catch (RuntimeException ignorar) {
								if (ExceptionUtils.getRootCause(ignorar) instanceof NestedNullException) {
									return null;
								} else {
									throw ignorar;
								}
							}
						}

						@Override
						public void setValue(Object newValue) throws com.vaadin.data.Property.ReadOnlyException {
							Object oldVal = getValue();
							super.setValue(newValue);
							if (oldVal != null ? !oldVal.equals(newValue) : oldVal != newValue) {
								fireValueChange();
							}
						}
					};
				}

			};
		}

	}

	public void setReadOnlyProperties(String... propiedadesRO) {
		((GunixTableFieldFactory)getTableFieldFactory()).setReadOnlyProperties(propiedadesRO);
	}
	
	public void replaceField(Object itemId, Object propertyId, Field<?> newField) {
		((GunixTableFieldFactory) getTableFieldFactory()).replaceField(newField, this, getContainerDataSource(), itemId, propertyId);
		markAsDirtyRecursive();
	}
	
	public void replaceField(Field<?> oldField, Field<?> newField) {
		((GunixTableFieldFactory) getTableFieldFactory()).replaceField(oldField, newField, this, getContainerDataSource());
		markAsDirtyRecursive();
	}
	
	public void setClaseBeans(Class<T> clazz) {
		this.clazz = clazz;
	}
	
	protected List<T> getBeans() {
		return GunixTableHelper.getBeans(this);
	}

	protected boolean isValido(T bean) {
		assertClazzNotNull();
		return GunixTableHelper.isValido(this, bean, clazz);
	}

	protected boolean isValida(boolean vacioEsError) {
		assertClazzNotNull();
		return GunixTableHelper.isValida(this, vacioEsError, clazz);
	}
	
	private void assertClazzNotNull() {
		if(clazz == null){
			throw new IllegalStateException("Se intento validar la tabla sin haber agregado elementos a validar o indicar el tipo (clase) de los elementos a contener en la tabla");
		}
	}
}
