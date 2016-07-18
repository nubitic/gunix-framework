/**
 * https://github.com/tlaukkan/vaadin-lazyquerycontainer
 */
package com.vaadin.data.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanUtils;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ReadOnlyException;

@SuppressWarnings("serial")
public class NestingBeanItem<BT> extends BeanItem<BT> {
	private static Boolean isGroovyPresent;
	private static Class<?> groovyMetaClass;
	
	public NestingBeanItem(final BT nestedObject, Class<BT> beanClass) {
		super(nestedObject, beanClass);
		exploreProperties(getItemPropertyIds());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void exploreProperties(Collection<?> propertyIds) {
		Set<String> removedProperties = new HashSet<String>();
		Set<String> nestedProperties = new HashSet<String>();
		for (final Object propertyId : propertyIds) {
			Property p = getItemProperty(propertyId);
			Class<?> propertyType = p.getType();
			if (!BeanUtils.isSimpleProperty(propertyType) && !Iterable.class.isAssignableFrom(propertyType) && !Map.class.isAssignableFrom(propertyType) && !isGroovyMetaClass(propertyType)) {
				try {
					if (p.getValue() == null) {
						propertyType.getConstructor((Class<?>[]) null);
						p.setValue(propertyType.newInstance());
					}
					// Enumerate all sub-properties
					Map<String, ?> pds = getPropertyDescriptors(propertyType);
					pds.keySet().forEach(key -> {
						String qualifiedPropertyId = propertyId + "." + key;
						nestedProperties.add(qualifiedPropertyId);
					});
					removedProperties.add((String) propertyId);
				} catch (ReadOnlyException | InstantiationException | IllegalAccessException | SecurityException e) {
					throw new RuntimeException(e);
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
