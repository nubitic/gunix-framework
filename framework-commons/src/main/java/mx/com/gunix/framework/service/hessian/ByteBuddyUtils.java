package mx.com.gunix.framework.service.hessian;

import java.lang.reflect.Method;
import java.util.Arrays;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.TypeManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;

import org.springframework.security.core.userdetails.UserDetails;

public final class ByteBuddyUtils {
	public static Class<?> appendUsuario(ClassLoader classLoader, String beanClassName) {
		try {
			Class<?> serviceInterface = classLoader.loadClass(beanClassName);
			
			DynamicType.Builder<?> builder = new ByteBuddy()
													.makeInterface(serviceInterface)
													.modifiers(Visibility.PUBLIC, TypeManifestation.INTERFACE);

			for (Method m : serviceInterface.getMethods()) {
				Class<?>[] args = new Class<?>[m.getParameterCount() + 1];
				System.arraycopy(m.getParameterTypes(), 0, args, 0, m.getParameterCount());
				args[args.length - 1] = UserDetails.class;
				builder = builder.defineMethod(m.getName(), m.getReturnType(), Visibility.PUBLIC).withParameters(Arrays.asList(args)).withoutCode();
			}
			
			return builder.make()
				.load(classLoader, ClassLoadingStrategy.Default.INJECTION)
				.getLoaded();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} 
	}
}
