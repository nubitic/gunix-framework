package mx.com.gunix.framework.ui.vaadin.component;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import mx.com.gunix.framework.ui.vaadin.VaadinUtils;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.vaadin.viritin.LazyList.CountProvider;
import org.vaadin.viritin.LazyList.PagingProvider;
import org.vaadin.viritin.ListContainer;
import org.vaadin.viritin.SortableLazyList.SortablePagingProvider;
import org.vaadin.viritin.fields.MTable;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.TransactionalPropertyWrapper;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Field;

public class GunixMTable<T extends Serializable> extends MTable<T> {
	private static final long serialVersionUID = 1L;

	public GunixMTable() {
		super();
		init();
	}

	public GunixMTable(Class<T> type) {
		super(type);
		init();
	}

	public GunixMTable(Collection<T> beans) {
		super(beans);
		init();
	}

	public GunixMTable(PagingProvider<T> pageProvider, CountProvider countProvider, int pageSize) {
		super(pageProvider, countProvider, pageSize);
		init();
	}

	public GunixMTable(PagingProvider<T> pageProvider, CountProvider countProvider) {
		super(pageProvider, countProvider);
		init();
	}

	public GunixMTable(SortablePagingProvider<T> pageProvider, CountProvider countProvider, int pageSize) {
		super(pageProvider, countProvider, pageSize);
		init();
	}

	public GunixMTable(SortablePagingProvider<T> pageProvider, CountProvider countProvider) {
		super(pageProvider, countProvider);
		init();
	}

	@SafeVarargs
	public GunixMTable(T... beans) {
		super(beans);
		init();
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
					};
				}

			};
		}

	}
}
