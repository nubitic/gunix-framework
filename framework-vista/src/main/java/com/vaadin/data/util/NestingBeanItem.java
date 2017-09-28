/**
 * https://github.com/tlaukkan/vaadin-lazyquerycontainer
 */
package com.vaadin.data.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ReadOnlyException;

import mx.com.gunix.framework.domain.Transient;

@SuppressWarnings("serial")
public class NestingBeanItem<BT> extends BeanItem<BT> {
	private static Boolean isGroovyPresent;
	private static Class<?> groovyMetaClass;
	private static Field methodPropertyGetMethodField;
	
	static{
		try {
			methodPropertyGetMethodField = MethodProperty.class.getDeclaredField("getMethod");
			methodPropertyGetMethodField.setAccessible(true);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	public NestingBeanItem(final BT nestedObject, Class<BT> beanClass, boolean bindTopLevelPropsOnly) {
		super(nestedObject, beanClass);
		if (!bindTopLevelPropsOnly) {
			exploreProperties(getItemPropertyIds());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void exploreProperties(Collection<?> propertyIds) {
		Set<String> removedProperties = new HashSet<String>();
		Set<String> nestedProperties = new HashSet<String>();
		for (final Object propertyId : propertyIds) {
			Property p = getItemProperty(propertyId);
			Method getMethod = null;
			if (p instanceof MethodProperty) {
				try {
					getMethod = (Method) methodPropertyGetMethodField.get(p);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			} else {
				if (p instanceof NestedMethodProperty) {
					List<Method> gets = ((NestedMethodProperty)p).getGetMethods();
					if (gets != null && !gets.isEmpty()) {
						getMethod = gets.get(gets.size() - 1);
					} else {
						throw new IllegalStateException("No fue posible obtener un método get para la propiedad: " + propertyId);
					}
				}
			}
			Class<?> propertyType = p.getType();
			if (!getMethod.isAnnotationPresent(Transient.class) && !Modifier.isAbstract(propertyType.getModifiers()) && !BeanUtils.isSimpleProperty(propertyType) && !Iterable.class.isAssignableFrom(propertyType) && !Map.class.isAssignableFrom(propertyType) && !isGroovyMetaClass(propertyType)) {
				try {
					if (p.getValue() == null) {
						propertyType.getConstructor((Class<?>[]) null);
						p.setValue(propertyType.newInstance());
					}
					// Enumerate all sub-properties
					Map<String, ?> pds = (Map<String, ?>) getPropertyDescriptors(propertyType);
					pds.keySet().forEach(key -> {
						if (((VaadinPropertyDescriptor) pds.get(key)).getPropertyType().equals(propertyType)) {
							return;
						}
						String qualifiedPropertyId = propertyId + "." + key;
						nestedProperties.add(qualifiedPropertyId);
					});
					removedProperties.add((String) propertyId);
				} catch (ReadOnlyException | InstantiationException | IllegalAccessException | SecurityException e) {
					throw new RuntimeException("No fue posible crear una nueva instancia para inicializar la propiedad \"" + propertyId+"\" de la clase "+ getBean().getClass(), e);
				} catch (NoSuchMethodException noProcesar) {

				}
			}
		}

		nestedProperties.forEach(property -> {
			addNestedProperty(property);
		});

		removedProperties.forEach(property -> {
			removeItemProperty(property);
		});

		if (!nestedProperties.isEmpty()) {
			exploreProperties(nestedProperties);
		}
	}

	private boolean isGroovyMetaClass(Class<?> propertyType) {
		if (isGroovyPresent == null && propertyType != null) {
			try {
				groovyMetaClass = Class.forName("groovy.lang.MetaClass");
				isGroovyPresent = Boolean.TRUE;
			} catch (ClassNotFoundException ignorar) {
				isGroovyPresent = Boolean.FALSE;
			}
		}

		if (groovyMetaClass != null) {
			if (groovyMetaClass.isAssignableFrom(propertyType)) {
				return true;
			}
		}

		return false;
	}
}
