package mx.com.gunix.framework.service;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;

@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
public class GetterServiceImpl implements GetterService, ApplicationContextAware {
	private static final Map<Class<?>, Method[]> declaredMethodsCache = new ConcurrentReferenceHashMap<Class<?>, Method[]>(256);
	private static final Method[] NO_METHODS = {};
	private ApplicationContext applicationContext;

	@Override
	public Serializable get(String uri, Object... args) {
		String[] serviceNameAndMethod = uri.replaceAll("(^/|/$)", "").split("/");
		if (serviceNameAndMethod.length < 2) {
			throw new IllegalArgumentException("La uri debe contener el nombre del servicio y método en el formato: nombreServicio/nombreMetodo");
		}

		String serviceName = serviceNameAndMethod[0];
		String methodName = serviceNameAndMethod[1];

		if (applicationContext.containsBean(serviceName)) {
			Object service = applicationContext.getBean(serviceName);
			Class<?>[] argTypes = null;
			Method m = findMethod(service.getClass(), methodName, args!=null?argTypes = Arrays
																		.asList(args)
																		.stream()
																		.map(s -> {
																			return s.getClass();
																			})
																		.collect(Collectors.toList())
																		.toArray(new Class<?>[] {}):new Class<?>[] {});
			if (m == null) {
				throw new IllegalArgumentException("No se encontró el método " + methodName + " y argumentos: " + (argTypes != null ? Arrays.toString(argTypes) : "SIN ARGUMENTOS"));
			}
			return (Serializable) ReflectionUtils.invokeMethod(m, service, args);
		} else {
			throw new IllegalArgumentException("No se encontró el servicio " + serviceName);
		}
	}

	private Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
		Assert.notNull(clazz, "Class must not be null");
		Assert.notNull(name, "Method name must not be null");
		Class<?> searchType = clazz;
		while (searchType != null) {
			Method[] methods = (searchType.isInterface() ? searchType.getMethods() : getDeclaredMethods(searchType));
			for (Method method : methods) {
				if (name.equals(method.getName()) && (paramTypes == null || areCompatible(method.getParameterTypes(),paramTypes))) {
					return method;
				}
			}
			searchType = searchType.getSuperclass();
		}
		return null;
	}

	private boolean areCompatible(Class<?>[] destino, Class<?>[] fuente) {
		if (destino.length != fuente.length) {
			return false;
		} else {
			for (int i = 0; i < destino.length; i++) {
				if (!destino[i].isAssignableFrom(fuente[i])) {
					return false;
				}
			}
			return true;
		}
	}

	private Method[] getDeclaredMethods(Class<?> clazz) {
		Method[] result = declaredMethodsCache.get(clazz);
		if (result == null) {
			Method[] declaredMethods = clazz.getDeclaredMethods();
			List<Method> defaultMethods = findConcreteMethodsOnInterfaces(clazz);
			if (defaultMethods != null) {
				result = new Method[declaredMethods.length + defaultMethods.size()];
				System.arraycopy(declaredMethods, 0, result, 0, declaredMethods.length);
				int index = declaredMethods.length;
				for (Method defaultMethod : defaultMethods) {
					result[index] = defaultMethod;
					index++;
				}
			}
			else {
				result = declaredMethods;
			}
			declaredMethodsCache.put(clazz, (result.length == 0 ? NO_METHODS : result));
		}
		return result;
	}

	private List<Method> findConcreteMethodsOnInterfaces(Class<?> clazz) {
		List<Method> result = null;
		for (Class<?> ifc : clazz.getInterfaces()) {
			for (Method ifcMethod : ifc.getMethods()) {
				if (!Modifier.isAbstract(ifcMethod.getModifiers())) {
					if (result == null) {
						result = new LinkedList<Method>();
					}
					result.add(ifcMethod);
				}
			}
		}
		return result;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
