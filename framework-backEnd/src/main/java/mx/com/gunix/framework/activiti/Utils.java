package mx.com.gunix.framework.activiti;

import java.io.ObjectStreamException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.springframework.beans.BeanUtils;

public class Utils {
	private static final String NESTED_REFERENCE = "\"#\"";

	private static final String EMPTY_COLLECTION_NESTED_REFERENCE = NESTED_REFERENCE + Iterable.class + "|1";
	private static final String EMPTY_MAP_NESTED_REFERENCE = NESTED_REFERENCE + Map.class + "|0";

	@SuppressWarnings({ "rawtypes" })
	public static Object fromMap(String varName, TreeMap<String, Object> stringifiedObject, ClassLoader classLoader) {
		final Object ans;
		if (!stringifiedObject.isEmpty()) {
			try {
				PropertyUtilsBean pub = new PropertyUtilsBean();
				Map<String, Object> iterableMapTypes = new TreeMap<String, Object>();
				Pattern iterablePattern = Pattern.compile("\\[\\d+\\]");
				Pattern mapPattern = Pattern.compile("\\(.+\\)");
				final boolean isNotObject;
				final boolean isItereable;

				Optional classObj = Optional.ofNullable(stringifiedObject.get(varName + ".class"));
				String classStr = classObj.isPresent() ? classObj.get().toString() : null;

				if (classStr != null) {
					isNotObject = false;
					isItereable = false;
					Class<?> rootClass = classLoader.loadClass(classStr);
					ans = rootClass.newInstance();
				} else {
					isNotObject = true;
					String firstKey = stringifiedObject.firstKey();
					if (iterablePattern.matcher(firstKey).find()) {
						isItereable = true;
						ans = new GunixArrayList();
						doMatcher(firstKey, iterablePattern.matcher(firstKey), ans, iterableMapTypes);
					} else {
						if (mapPattern.matcher(firstKey).find()) {
							isItereable = false;
							ans = new HashMap();
							doMatcher(firstKey, mapPattern.matcher(firstKey), ans, iterableMapTypes);
						} else {
							throw new IllegalArgumentException("Tipo de objeto no soportado para la variable " + varName);
						}
					}
				}

				stringifiedObject.keySet().stream().filter(key -> iterablePattern.matcher(key).find() || mapPattern.matcher(key).find()).forEach(key -> {
					doMatcher(key, iterablePattern.matcher(key), new GunixArrayList(), iterableMapTypes);
					doMatcher(key, mapPattern.matcher(key), new HashMap(), iterableMapTypes);
				});

				Map<String, Object> childTypes = new TreeMap<String, Object>();

				String varClass = varName + ".class";

				stringifiedObject.keySet().stream().filter(key -> (!key.equals(varClass)) && key.endsWith(".class")).forEach(key -> {
					try {
						Class<?> childClass = classLoader.loadClass(stringifiedObject.get(key).toString());
						String newKey = key.substring(0, key.length() - 6);
						childTypes.put(newKey, childClass.newInstance());
					} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
						throw new RuntimeException(e);
					}
				});

				stringifiedObject.putAll(iterableMapTypes);
				stringifiedObject.putAll(childTypes);

				Map<String, String> nestedReferences = new TreeMap<String, String>();

				stringifiedObject.forEach((key, value) -> {
					try {
						if (!key.endsWith(".class") && !key.equals(varName)) {
							if ((value instanceof String) && value.toString().startsWith(NESTED_REFERENCE)) {
								nestedReferences.put(key, (String) value);
							} else {
								String prop = isNotObject ? key.substring(isItereable ? key.indexOf("[") : key.indexOf("(")) : key.substring(varName.length() + 1);
								if (value instanceof Double) {
									Class<?> propType = pub.getPropertyType(ans, prop);
									if ((propType.isPrimitive() && propType.getName().equals("float")) || Float.class.isAssignableFrom(propType)) {
										value = ((Double) value).floatValue();
									}
								} else {
									if (value instanceof Long) {
										Class<?> propType = pub.getPropertyType(ans, prop);
										if ((propType.isPrimitive() && propType.getName().equals("boolean")) || Boolean.class.isAssignableFrom(propType)) {
											value = ((Long)value == 1);
										}
									}
								}

								pub.setNestedProperty(ans, prop, value);
							}
						}
					} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				});

				childTypes.put("", ans);

				int controlCount = 0;
				int tope50Ciclos = nestedReferences.size()*50;
				do {
					String assignedPropertyPath = null;
					for(String propertyPath : nestedReferences.keySet()){
						String nestedReferenceHash = nestedReferences.get(propertyPath);
						Boolean assigned = Boolean.FALSE;
						if (!(assigned = doAssignNestedReference(pub, ans, isNotObject, isItereable, varName, childTypes, propertyPath, nestedReferenceHash))) {
							assigned = doAssignNestedReference(pub, ans, isNotObject, isItereable, varName, iterableMapTypes, propertyPath, nestedReferenceHash);
						}
						if(assigned) {
							assignedPropertyPath = propertyPath;
							break;
						}
					}
					if(assignedPropertyPath!=null) {
						nestedReferences.remove(assignedPropertyPath);
					}
					controlCount++;
					if (!nestedReferences.isEmpty() && (controlCount > tope50Ciclos)) {
						throw new IllegalStateException("No fue posible resolver todas las dependencias anidadas dentro de 50 ciclos");
					}
				} while (!nestedReferences.isEmpty());

			} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
				throw new RuntimeException(e);
			}
		} else {
			ans = null;
		}
		return ans;
	}

	private static boolean doAssignNestedReference(PropertyUtilsBean pub, Object ans, boolean isNotObject, boolean isItereable, String varName, Map<String, Object> childTypes, String propertyPath, String nestedReferenceHash) {
		Boolean[] boolAns = new Boolean[] { Boolean.FALSE };
		String actualPropertyPath = isNotObject ? propertyPath.substring(isItereable ? propertyPath.indexOf("[") : propertyPath.indexOf("(")) : propertyPath.substring(varName.length() + 1);
		try {
			if (pub.getNestedProperty(ans, actualPropertyPath) != null) {
				boolAns[0] = Boolean.TRUE;
			} else {
				if (EMPTY_COLLECTION_NESTED_REFERENCE.equals(nestedReferenceHash)) {
					pub.setNestedProperty(ans, actualPropertyPath, new GunixArrayList());
					boolAns[0] = Boolean.TRUE;
				} else {
					if (EMPTY_MAP_NESTED_REFERENCE.equals(nestedReferenceHash)) {
						pub.setNestedProperty(ans, actualPropertyPath, new HashMap<Object,Object>());
						boolAns[0] = Boolean.TRUE;
					} else {
						childTypes.forEach((childKey, childValue) -> {
							Class<?> clazz = childValue.getClass();
							String classHash = getClassHash(clazz, childValue);
							try {
								if (nestedReferenceHash.equals(classHash)) {
									pub.setNestedProperty(ans, actualPropertyPath, childValue);
									boolAns[0] = Boolean.TRUE;
									return;
								}
							} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
								throw new RuntimeException(e);
							}
						});
					}
				}
			}
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		return boolAns[0];
	}

	private static void doMatcher(String key, Matcher matcher, Object object, Map<String, Object> subMap) {
		while (matcher.find()) {
			String subKey = key.substring(0, matcher.start());
			if (!subMap.containsKey(subKey)) {
				subMap.put(subKey, object);
			}
		}
	}

	public static Map<String, Object> toMap(String varName, Object object) {
		Map<String, Object> stringifiedObject = new TreeMap<String, Object>();
		List<String> processedObjectsHashes = new ArrayList<String>();
		doAppend(object, stringifiedObject, varName, processedObjectsHashes);
		return stringifiedObject;
	}

	@SuppressWarnings("rawtypes")
	private static void doAppend(Object nestedObject, Map<String, Object> stringifiedObject, CharSequence currFieldStrBldr, List<String> processedObjectsHashes) {
		if (nestedObject != null) {
			Class<?> clazz = nestedObject.getClass();
			if (BeanUtils.isSimpleProperty(clazz)) {
				doPut(stringifiedObject, currFieldStrBldr, clazz, nestedObject);
			} else {
				String classHash = getClassHash(clazz, nestedObject);
				if (!processedObjectsHashes.contains(classHash)) {
					processedObjectsHashes.add(classHash);
					if (Iterable.class.isAssignableFrom(clazz)) {
						doIterable(((Iterable) nestedObject).iterator(), currFieldStrBldr, stringifiedObject, processedObjectsHashes);
					} else {
						if (Map.class.isAssignableFrom(clazz)) {
							doMap((Map) nestedObject, currFieldStrBldr, stringifiedObject, processedObjectsHashes);
						} else {
							BeanMap bm = new BeanMap(nestedObject);
							bm.keyIterator().forEachRemaining(fieldName -> {
								doAppend(bm.get(fieldName), stringifiedObject, new StringBuilder(currFieldStrBldr).append(".").append(fieldName), processedObjectsHashes);
							});
						}
					}
				} else {
					doPut(stringifiedObject, currFieldStrBldr, clazz, classHash);
				}
			}
		}
	}

	private static String getClassHash(Class<?> clazz, Object nestedObject) {
		return new StringBuilder(NESTED_REFERENCE).append(Iterable.class.isAssignableFrom(clazz) ? Iterable.class : Map.class.isAssignableFrom(clazz) ? Map.class : clazz).append("|")
				.append(Objects.hashCode(nestedObject)).toString();
	}

	@SuppressWarnings("rawtypes")
	private static void doMap(Map nestedMap, CharSequence currFieldStrBldr, Map<String, Object> stringifiedObject, List<String> processedObjectsHashes) {
		for (Object key : nestedMap.keySet()) {
			doPutOrAppendForIterableOrMapElement(nestedMap.get(key), new StringBuilder(currFieldStrBldr).append("(").append(key).append(")"), key.toString(), stringifiedObject, processedObjectsHashes);
		}
	}

	@SuppressWarnings("rawtypes")
	private static void doIterable(Iterator noIt, CharSequence currFieldStrBldr, Map<String, Object> stringifiedObject, List<String> processedObjectsHashes) {
		int i = 0;
		while (noIt.hasNext()) {
			doPutOrAppendForIterableOrMapElement(noIt.next(), new StringBuilder(currFieldStrBldr).append("[").append(i).append("]"), String.valueOf(i), stringifiedObject, processedObjectsHashes);
			i++;
		}
	}

	private static void doPutOrAppendForIterableOrMapElement(Object itObj, CharSequence indexedElement, String elementIndex, Map<String, Object> stringifiedObject, List<String> processedObjectsHashes) {
		if (itObj != null) {
			Class<?> clazzItObj = itObj.getClass();
			if (BeanUtils.isSimpleProperty(clazzItObj)) {
				doPut(stringifiedObject, indexedElement, clazzItObj, itObj);
			} else {
				doAppend(itObj, stringifiedObject, indexedElement, processedObjectsHashes);
			}
		}
	}

	private static void doPut(Map<String, Object> stringifiedObject, CharSequence currFieldStrBldr, Class<?> clazz, Object nestedObject) {
		stringifiedObject.put(currFieldStrBldr.toString(), clazz.equals(Class.class) ? ((Class<?>) nestedObject).getName() : nestedObject);
	}

	static class GunixArrayList extends ArrayList<Object> {
		private static final long serialVersionUID = 1L;

		@Override
		public Object set(int index, Object element) {
			while (index >= size()) {
				add(null);
			}
			return super.set(index, element);
		}

		@Override
		public Object get(int index) {
			Object obj = null;
			try {
				obj = super.get(index);
			} catch (IndexOutOfBoundsException ignorar) {
			}

			return obj;
		}

		private Object writeReplace() throws ObjectStreamException {
			return new ArrayList<Object>(this);
		}
	}
}