package mx.com.gunix.framework.config.aspects;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.ControlFlow;
import org.springframework.core.ControlFlowFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

import mx.com.gunix.framework.security.PersistentTokenBasedRememberMeServices;
import mx.com.gunix.framework.security.josso.JOSSOAuthenticationProvider;
import mx.com.gunix.framework.service.UsuarioService;

@Aspect
@Component
public class HessianServiceAdvice {
	private static final Logger log = Logger.getLogger(HessianServiceAdvice.class);
	
	@Around("execution(* net.bytebuddy.renamed..*.*(..))")
	public Object aroundAnyMethodInsideAClassGeneratedByByteBuddy(ProceedingJoinPoint pjp){
		try {
			Object[] orgArgs = pjp.getArgs();
			Object[] newArgs = new Object[orgArgs.length+1];
			System.arraycopy(orgArgs, 0, newArgs, 0, orgArgs.length);
			
			ControlFlow controlFlow = null;
			
			Object principal = (SecurityContextHolder.getContext().getAuthentication()==null && 
								((pjp.getSignature().getDeclaringType().equals(UsuarioService.class) &&				// Excepciones para las que se acepta autenticación null
							     (pjp.getSignature().getName().equals("getAnonymous") || 							// UsuarioService.getAnonymous(),
							      (pjp.getSignature().getName().equals("getUsuario") && 							// UsuarioService.getUsuario() siempre y cuando provenga de:
							       ((controlFlow= ControlFlowFactory.createControlFlow()).under(PersistentTokenBasedRememberMeServices.class, "processAutoLoginCookie") || // PersistentTokenBasedRememberMeServices.processAutoLoginCookie ó 
							    		   									  controlFlow.under(UsernamePasswordAuthenticationFilter.class, "attemptAuthentication") 	|| // UsernamePasswordAuthenticationFilter.attemptAuthentication ó 
							    		   									  controlFlow.under(JOSSOAuthenticationProvider.class, "authenticate")))))			|| // JOSSOAuthenticationProvider.authenticate,
							    (pjp.getSignature().getDeclaringType().equals(PersistentTokenRepository.class) &&	// ó 
							     (pjp.getSignature().getName().equals("getTokenForSeries") || 						// PersistentTokenRepository.getTokenForSeries() ó
							      pjp.getSignature().getName().equals("updateToken") || 							// PersistentTokenRepository.getTokenForSeries() ó
							      pjp.getSignature().getName().equals("removeUserTokens")) 							// PersistentTokenRepository.removeUserTokens
							    )))?																				
								null
								:SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			newArgs[newArgs.length-1]=(principal instanceof UserDetails)?principal:null;
			
			Class<?>[] interfaces = ((Advised)pjp.getThis()).getProxiedInterfaces();
			Class<?> generatedInterface = null;
			for(Class<?> interCara:interfaces){
				if(interCara.getName().startsWith("net.bytebuddy.renamed")){
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
			
			if (log.isDebugEnabled()) {
				if (newArgs[newArgs.length - 1] != null) {
					log.debug("Invocando " + generatedMethod.toGenericString() + " con: " + Arrays.toString(newArgs));
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
