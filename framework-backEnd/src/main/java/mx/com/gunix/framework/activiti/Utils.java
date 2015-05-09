package mx.com.gunix.framework.activiti;

import java.io.ObjectStreamException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.springframework.beans.BeanUtils;

public class Utils {
	@SuppressWarnings({"rawtypes" })
	public static Object fromMap(String varName, TreeMap<String, Object> stringifiedObject, ClassLoader classLoader) {
		final Object ans;
		if(!stringifiedObject.isEmpty()){
			try{
				PropertyUtilsBean pub = new PropertyUtilsBean();
				Map<String,Object> iterableMapTypes = new TreeMap<String,Object>();
				Pattern iterablePattern = Pattern.compile("\\[\\d+\\]");
				Pattern mapPattern = Pattern.compile("\\(.+\\)");
				final boolean isNotObject;
				final boolean isItereable;
	
				
				Optional classObj = Optional.ofNullable(stringifiedObject.get(varName + ".class"));
				String classStr = classObj.isPresent()?classObj.get().toString():null; 
				
				if(classStr!=null){
					isNotObject=false;
					isItereable=false;
					Class<?> rootClass = classLoader.loadClass(classStr);
					ans = rootClass.newInstance();	
				}else{
					isNotObject=true;
					String firstKey = stringifiedObject.firstKey();
					if(iterablePattern.matcher(firstKey).find()){
						isItereable=true;
						ans = new GunixArrayList();
						doMatcher(firstKey, iterablePattern.matcher(firstKey), ans,iterableMapTypes);
					}else{
						if(mapPattern.matcher(firstKey).find()){
							isItereable=false;
							ans = new HashMap();
							doMatcher(firstKey, mapPattern.matcher(firstKey), ans,iterableMapTypes);
						}else{
							throw new IllegalArgumentException("Tipo de objeto no soportado para la variable "+varName);
						}
					}
				}
				
				stringifiedObject
							.keySet()
							.stream()
							.filter(key -> iterablePattern.matcher(key).find() || mapPattern.matcher(key).find() )
							.forEach(key -> {
											doMatcher(key, iterablePattern.matcher(key), new GunixArrayList(),iterableMapTypes);
											doMatcher(key, mapPattern.matcher(key), new HashMap(),iterableMapTypes);
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
						if (!key.endsWith(".class") && !key.equals(varName)) {
							pub.setNestedProperty(ans, isNotObject?key.substring(isItereable?key.indexOf("["):key.indexOf("(")):key.substring(varName.length() + 1), value);
						}
					} catch (NoSuchMethodException e) {
						throw new RuntimeException(e);
					} catch(InvocationTargetException e){
						throw new RuntimeException(e);
					} catch(IllegalAccessException e){
						throw new RuntimeException(e);
					}
				});
			}catch(ClassNotFoundException e){
				throw new RuntimeException(e);
			}catch(IllegalAccessException e){
				throw new RuntimeException(e);
			}catch(InstantiationException e){
				throw new RuntimeException(e);
			}
		}else{
			ans=null;
		}
		return ans;
	}
	
	private static void doMatcher(String key, Matcher matcher, Object object, Map<String,Object> subMap){
		while(matcher.find()){
			String subKey = key.substring(0,matcher.start());
			if(!subMap.containsKey(subKey)){
				subMap.put(subKey, object);
			}
		}
	}

	public static Map<String, Object> toMap(String varName, Object object){
		Map<String, Object> stringifiedObject = new TreeMap<String, Object>();
		doAppend(object,stringifiedObject,varName);
		return stringifiedObject;
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
						BeanMap bm = new BeanMap(nestedObject);
						bm.keyIterator().forEachRemaining(fieldName -> {
							doAppend(bm.get(fieldName), stringifiedObject, new StringBuilder(currFieldStrBldr).append(".").append(fieldName));
						});
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
				doAppend(itObj,stringifiedObject, indexedElement);
			}
		}
	}

	private static void doPut(Map<String, Object> stringifiedObject, CharSequence currFieldStrBldr, Class<?> clazz, Object nestedObject) {
		stringifiedObject.put(currFieldStrBldr.toString(), clazz.equals(Class.class) ? ((Class<?>) nestedObject).getName() : nestedObject);
	}
	
	static class GunixArrayList extends ArrayList<Object>{
		private static final long serialVersionUID = 1L;

		@Override
		public Object set(int index, Object element) {
			while(index>=size()){
				add(null);
			}
			return super.set(index, element);
		}
		private Object writeReplace() throws ObjectStreamException {
			return new ArrayList<Object>(this);
		}
	}
}