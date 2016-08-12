package mx.com.gunix.framework.service;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ReflectionUtils;

import com.thoughtworks.xstream.core.util.Primitives;

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

		Object[] argsWithOutPIDDIF = args;
		if (args != null && args.length > 0 && args[0].equals(GetterService.INCLUDES_PID_PDID)) {
			String[] pidPdidArr = new String[2];
			if (args.length > 3) {
				argsWithOutPIDDIF = new Object[args.length - 3];
				System.arraycopy(args, 1, argsWithOutPIDDIF, 0, args.length - 3);
				pidPdidArr[GetterService.PROCESS_INSTANCEID] = (String) args[args.length - 2];
				pidPdidArr[GetterService.PROCESS_DEFINITIONID] = (String) args[args.length - 1];
			} else {
				argsWithOutPIDDIF = null;
				pidPdidArr[GetterService.PROCESS_INSTANCEID] = (String) args[1];
				pidPdidArr[GetterService.PROCESS_DEFINITIONID] = (String) args[2];
			}
			GetterService.pidPdid.set(pidPdidArr);
		}
		if (applicationContext.containsBean(serviceName)) {
			Object service = getTargetObject(applicationContext.getBean(serviceName));
			Class<?>[] argTypes = null;
			Method m = findMethod(service.getClass(), methodName, argsWithOutPIDDIF!=null?argTypes = Arrays
																		.asList(argsWithOutPIDDIF)
																		.stream()
																		.map(s -> {
																				return s != null ? s.getClass() : null;
																			})
																		.collect(Collectors.toList())
																		.toArray(new Class<?>[] {}):new Class<?>[] {});
			if (m == null) {
				throw new IllegalArgumentException("No se encontró el método " + methodName + " con argumentos: " + (argTypes != null ? Arrays.toString(argTypes) : "SIN ARGUMENTOS"));
			}
			return (Serializable) ReflectionUtils.invokeMethod(m, service, argsWithOutPIDDIF);
		} else {
			throw new IllegalArgumentException("No se encontró el servicio " + serviceName);
		}
	}
	
	private Object getTargetObject(Object proxy) {
		if (AopUtils.isJdkDynamicProxy(proxy)) {
			try {
				return ((Advised) proxy).getTargetSource().getTarget();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			return proxy; // expected to be cglib proxy then, which is simply a specialized class
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
				if (fuente[i] !=null && !(destino[i].isAssignableFrom(fuente[i]) || checkPrimitiveWrappers(destino[i],fuente[i]) || checkPrimitiveWrappers(fuente[i], destino[i]) )) {
					return false;
				}
			}
			return true;
		}
	}

	private boolean checkPrimitiveWrappers(Class<?> destino, Class<?> fuente) {
		boolean ans = false;
		if (destino.isPrimitive() && Primitives.unbox(fuente) == destino) {
			ans = true;
		}
		return ans;
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
