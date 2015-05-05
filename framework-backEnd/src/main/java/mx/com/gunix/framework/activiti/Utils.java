package mx.com.gunix.framework.activiti;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.springframework.beans.BeanUtils;

public class Utils {
	@SuppressWarnings({ "serial", "rawtypes" })
	public static Object fromMap(String varName, Map<String, Object> stringifiedObject, ClassLoader classLoader) {
		PropertyUtilsBean pub = new PropertyUtilsBean();
		try{
			Class<?> rootClass = classLoader.loadClass(stringifiedObject.get(varName + ".class").toString());
			final Object ans = rootClass.newInstance();
	
			Map<String,Object> iterableMapTypes = new TreeMap<String,Object>();
			Pattern iterablePattern = Pattern.compile("\\[\\d+\\]");
			Pattern mapPattern = Pattern.compile("\\(.+\\)");
			stringifiedObject
						.keySet()
						.stream()
						.filter(key -> iterablePattern.matcher(key).find() || mapPattern.matcher(key).find() )
						.forEach(key -> {
								
									Matcher ipm = iterablePattern.matcher(key);
									Matcher mpm = mapPattern.matcher(key);
									
									while(ipm.find()){
										String subKey = key.substring(0,ipm.start());
										if(!iterableMapTypes.containsKey(subKey)){
											iterableMapTypes.put(subKey, new ArrayList<Object>(){
	
												@Override
												public Object set(int index, Object element) {
													while(index>=size()){
														add(null);
													}
													return super.set(index, element);
												}
												
											});
										}
									}
									
									while(mpm.find()){
										String subKey = key.substring(0,mpm.start());
										if(!iterableMapTypes.containsKey(subKey)){
											iterableMapTypes.put(subKey, new HashMap());
										}
									}
								
							});
			
			Map<String,Object> childTypes = new TreeMap<String,Object>();
			
			stringifiedObject
						.keySet()
						.stream()
						.filter(key -> (!key.equals(varName + ".class")) && key.endsWith(".class"))
						.forEach(key -> {
								try{
									Class<?> childClass = classLoader.loadClass(stringifiedObject.get(key).toString());
									String newKey = key.substring(0, key.length() - ".class".length());
									childTypes.put(newKey, childClass.newInstance());
								}catch(ClassNotFoundException e){
									throw new RuntimeException(e);
								}catch(IllegalAccessException e){
									throw new RuntimeException(e);
								}catch(InstantiationException e){
									throw new RuntimeException(e);
								}
							});
			
			stringifiedObject.putAll(iterableMapTypes);
			stringifiedObject.putAll(childTypes);
			
			stringifiedObject.forEach((key, value) -> {
				try {
					if (!key.endsWith(".class")) {
						pub.setNestedProperty(ans, key.substring(varName.length() + 1), value);
					}
				} catch (NoSuchMethodException e) {
					throw new RuntimeException(e);
				} catch(InvocationTargetException e){
					throw new RuntimeException(e);
				} catch(IllegalAccessException e){
					throw new RuntimeException(e);
				}
			});
	
			return ans;
		}catch(ClassNotFoundException e){
			throw new RuntimeException(e);
		}catch(IllegalAccessException e){
			throw new RuntimeException(e);
		}catch(InstantiationException e){
			throw new RuntimeException(e);
		}
	}
	
	public static Map<String, Object> toMap(String varName, Object object){
		Map<String, Object> stringifiedObject = new TreeMap<String, Object>();
		append(stringifiedObject,varName,object);
		return stringifiedObject;
	}
	
	private static void append(Map<String, Object> stringifiedObject, CharSequence currFieldStrBldr, Object object) {
		BeanMap bm = new BeanMap(object);
		bm.keyIterator().forEachRemaining(fieldName -> {
			doAppend(bm.get(fieldName), stringifiedObject, new StringBuilder(currFieldStrBldr).append(".").append(fieldName));
		});
	}

	@SuppressWarnings("rawtypes")
	private static void doAppend(Object nestedObject, Map<String, Object> stringifiedObject, CharSequence currFieldStrBldr) {
		if (nestedObject != null) {
			Class<?> clazz = nestedObject.getClass();

			if (BeanUtils.isSimpleProperty(clazz)) {
				doPut(stringifiedObject, currFieldStrBldr, clazz, nestedObject);
			} else {
				if (Iterable.class.isAssignableFrom(clazz)) {
					doIterable(((Iterable) nestedObject).iterator(), currFieldStrBldr, stringifiedObject);
				} else {
					if (Map.class.isAssignableFrom(clazz)) {
						doMap((Map) nestedObject, currFieldStrBldr, stringifiedObject);
					} else {
						append(stringifiedObject, new StringBuilder(currFieldStrBldr), nestedObject);
					}
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private static void doMap(Map nestedMap, CharSequence currFieldStrBldr, Map<String, Object> stringifiedObject) {
		for (Object key : nestedMap.keySet()) {
			doPutOrAppendForIterableOrMapElement(nestedMap.get(key), new StringBuilder(currFieldStrBldr).append("(").append(key).append(")"), key.toString(), stringifiedObject);
		}
	}

	@SuppressWarnings("rawtypes")
	private static void doIterable(Iterator noIt, CharSequence currFieldStrBldr, Map<String, Object> stringifiedObject) {
		int i = 0;
		while (noIt.hasNext()) {
			doPutOrAppendForIterableOrMapElement(noIt.next(), new StringBuilder(currFieldStrBldr).append("[").append(i).append("]"), String.valueOf(i), stringifiedObject);
			i++;
		}
	}

	private static void doPutOrAppendForIterableOrMapElement(Object itObj, CharSequence indexedElement, String elementIndex, Map<String, Object> stringifiedObject) {
		if (itObj != null) {
			Class<?> clazzItObj = itObj.getClass();
			if (BeanUtils.isSimpleProperty(clazzItObj)) {
				doPut(stringifiedObject, indexedElement, clazzItObj, itObj);
			} else {
				append(stringifiedObject, indexedElement, itObj);
			}
		}
	}

	private static void doPut(Map<String, Object> stringifiedObject, CharSequence currFieldStrBldr, Class<?> clazz, Object nestedObject) {
		stringifiedObject.put(currFieldStrBldr.toString(), clazz.equals(Class.class) ? ((Class<?>) nestedObject).getName() : nestedObject);
	}
}