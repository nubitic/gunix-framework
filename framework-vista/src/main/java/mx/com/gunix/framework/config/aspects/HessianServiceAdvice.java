package mx.com.gunix.framework.config.aspects;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class HessianServiceAdvice {
	@Around("execution(* net.bytebuddy.generated.*.*(..))")
	public Object aroundPublicMethodInsideAClassMarkedWithAtHessian(ProceedingJoinPoint pjp){
		try {
			Object[] orgArgs = pjp.getArgs();
			Object[] newArgs = new Object[orgArgs.length+1];
			System.arraycopy(orgArgs, 0, newArgs, 0, orgArgs.length);
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			newArgs[newArgs.length-1]=(principal instanceof UserDetails)?principal:null;
			
			Class<?>[] interfaces = ((Advised)pjp.getThis()).getProxiedInterfaces();
			Class<?> generatedInterface = null;
			for(Class<?> interCara:interfaces){
				if(interCara.getName().startsWith("net.bytebuddy.generated")){
					generatedInterface=interCara;
					break;
				}
			}

			Method[] metodos = generatedInterface.getMethods();
			Method generatedMethod=null;
			MethodSignature ms = (MethodSignature)pjp.getSignature();
			String name=ms.getName();
			Class<?>[] argTypes=ms.getParameterTypes();
			Class<?> returnType=ms.getReturnType();
			for(Method m:metodos){
				Class<?>[] mArgTypes = m.getParameterTypes();
				if(name.equals(m.getName()) && 
						returnType.equals(m.getReturnType()) && 
						argTypes.length==(mArgTypes.length-1) &&
						mArgTypes[mArgTypes.length-1]==UserDetails.class &&
						compatibles(argTypes,mArgTypes)
						){
					generatedMethod=m;
					break;
				}
			}
			
			return AopUtils.invokeJoinpointUsingReflection(pjp.getTarget(), generatedMethod, newArgs);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}		
	}

	private boolean compatibles(Class<?>[] argTypes, Class<?>[] mArgTypes) {
		Class<?>[] mTArgTypes = new Class<?>[argTypes.length];
		System.arraycopy(mArgTypes, 0, mTArgTypes, 0, argTypes.length);
		return Arrays.deepEquals(argTypes, mTArgTypes);
	}
}
