package mx.com.gunix.framework.ui.vaadin.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.server.UserError;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.Table;

import mx.com.gunix.framework.ui.vaadin.component.GunixTableFieldFactory.GunixFieldPropertyRel;

public abstract class GunixTableHelper {
	
	@SuppressWarnings("unchecked")
	public static <S extends Serializable> List<S> getBeans(Table tabla) {
		List<S> beans = new ArrayList<S>();
		if (tabla.getContainerDataSource() == null) {
			throw new IllegalArgumentException("Se requiere que el ContainerDataSource sea diferente a null");
		}
		tabla.getContainerDataSource().getItemIds().forEach(beanId -> {
			beans.add((S) beanId);
		});
		return beans;
	}
	
	public static <R extends Serializable> boolean isValido(Table tabla, R bean, Class<R> clase) {
		return isValid(tabla, false, bean, clase);
	}
	
	@SuppressWarnings("unchecked")
	private static <R extends Serializable> boolean isValid(Table tabla, boolean vacioEsError, R bean, Class<R> clase){
		Map<String, Boolean> hayErroresHolder = new HashMap<String, Boolean>();
		hayErroresHolder.put("hayErrores", false);
		Container container = tabla.getContainerDataSource();
		if (bean == null && vacioEsError && container.size() == 0) {
			hayErroresHolder.put("hayErrores", true);
			tabla.setComponentError(new UserError("La tabla debe contener al menos un registro"));
		}
		if (bean != null && !container.getItemIds().contains(bean)) {
			throw new IllegalArgumentException("El bean especificado no se encuentra en la tabla");
		}
		tabla.setComponentError(null);
		GunixTableBeanErrorGenerator<R> bErrGen = initTableBeanErrorGenerator(tabla);
		bErrGen.clearBeanErrors();
		Map<Field<?>, Property<?>> prevPropDS = new HashMap<Field<?>, Property<?>>();
		GunixBeanFieldGroup<R> bfgf = new GunixBeanFieldGroup<R>(clase);
		GunixTableFieldFactory grff = (GunixTableFieldFactory) tabla.getTableFieldFactory();
		if (bean == null) {
			container.getItemIds().forEach(beanId -> {
				validaBean(bfgf, (R) beanId, grff, tabla, container, prevPropDS, hayErroresHolder);
			});
		} else {
			validaBean(bfgf, bean, grff, tabla, container, prevPropDS, hayErroresHolder);
		}
		if (hayErroresHolder.get("hayErrores") && tabla.getComponentError() == null) {
			tabla.setComponentError(new UserError("La tabla tiene errores"));
		}
		tabla.markAsDirtyRecursive();
		return !hayErroresHolder.get("hayErrores");
	}
	
	private static <R extends Serializable> void validaBean(GunixBeanFieldGroup<R> bfgf, R beanId, GunixTableFieldFactory grff, Table tabla, Container container, Map<Field<?>, Property<?>> prevPropDS, Map<String, Boolean> hayErroresHolder) {
		bfgf.setItemDataSource(beanId);
		GunixTableBeanErrorGenerator<R> bErrGen = initTableBeanErrorGenerator(tabla);
		List<GunixFieldPropertyRel> fieldProps = grff.getFieldsBy(tabla, container, beanId);
		fieldProps.forEach(fieldProp -> {
			if (!(GunixViewErrorHandler.getCurrent().isInvalidValueComponent(fieldProp.getField()))) {
				prevPropDS.put(fieldProp.getField(), fieldProp.getField().getPropertyDataSource());
				((AbstractComponent) fieldProp.getField()).setComponentError(null);
				bfgf.bind(fieldProp.getField(), fieldProp.getPropertyId());
			} else {
				hayErroresHolder.put("hayErrores", true);
			}
		});
		try {
			bfgf.commit(ibve -> {
				bErrGen.addBeanError((R) ibve.getRootBean(), ibve.getMessage());
			});
		} catch (CommitException e) {
			hayErroresHolder.put("hayErrores", true);
			for (Field<?> f : e.getInvalidFields().keySet()) {
				((AbstractComponent) f).setComponentError(new UserError(e.getInvalidFields().get(f).getCauses()[0].getMessage()));
			}
		} finally {
			for (Field<?> f : prevPropDS.keySet()) {
				bfgf.unbind(f);
				f.setPropertyDataSource(prevPropDS.get(f));
				f.setBuffered(false);
				f.setInvalidAllowed(false);
			}
			prevPropDS.clear();
		}
	}

	@SuppressWarnings("unchecked")
	private static <R extends Serializable> GunixTableBeanErrorGenerator<R> initTableBeanErrorGenerator(Table tabla) {
		GunixTableBeanErrorGenerator<R> gtbeg = null;
		if (tabla.getItemDescriptionGenerator() == null && tabla.getCellStyleGenerator() == null) {
			gtbeg = new GunixTableBeanErrorGenerator<R>();
			tabla.setItemDescriptionGenerator(gtbeg);
			tabla.setCellStyleGenerator(gtbeg);
		} else {
			if (tabla.getItemDescriptionGenerator() instanceof GunixTableBeanErrorGenerator) {
				gtbeg = (GunixTableBeanErrorGenerator<R>) tabla.getItemDescriptionGenerator();
			}
		}
		return gtbeg;
	}

	public static <S extends Serializable> boolean isValida(Table tabla, boolean vacioEsError, Class<S> clazz) {
		return isValid(tabla, vacioEsError, null, clazz);
	}
	
}
