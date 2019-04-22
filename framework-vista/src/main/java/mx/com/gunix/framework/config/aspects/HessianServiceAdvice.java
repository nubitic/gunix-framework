package mx.com.gunix.framework.config.aspects;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.apache.commons.lang.SerializationUtils;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.ControlFlow;
import org.springframework.core.ControlFlowFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLLogoutFilter;
import org.springframework.security.saml.SAMLLogoutProcessingFilter;
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
			Signature pjpSignature = null;
			
			
			Object principal = (SecurityContextHolder.getContext().getAuthentication()==null && 
								(((pjpSignature = pjp.getSignature()).getDeclaringType().equals(UsuarioService.class) &&	// Excepciones para las que se acepta autenticación null
							     (pjpSignature.getName().equals("getAnonymous") || 											// UsuarioService.getAnonymous(),
							       ((pjpSignature.getName().equals("getUsuario") || 										// UsuarioService.getUsuario(),
							    	 pjpSignature.getName().equals("guardaSAMLSSOAuth") ||									// UsuarioService.guardaSAMLSSOAuth(), 
							    	 pjpSignature.getName().equals("getSAMLLocalSessions") || 								// UsuarioService.getSAMLLocalSessions(), 
							    	 pjpSignature.getName().equals("deleteSAMLLocalSessions")) && 							// UsuarioService.deleteSAMLLocalSessions() siempre y cuando provenga de:
							        ((controlFlow= ControlFlowFactory.createControlFlow()).under(PersistentTokenBasedRememberMeServices.class, "processAutoLoginCookie")	|| // PersistentTokenBasedRememberMeServices.processAutoLoginCookie ó 
							    	    									   controlFlow.under(UsernamePasswordAuthenticationFilter.class, "attemptAuthentication") 		|| // UsernamePasswordAuthenticationFilter.attemptAuthentication ó 
							    		   									   controlFlow.under(JOSSOAuthenticationProvider.class, "authenticate")							|| // JOSSOAuthenticationProvider.authenticate ó
							    		   									   controlFlow.under(SAMLLogoutProcessingFilter.class, "processLogout")							|| // SAMLLogoutProcessingFilter.processLogout ó
							    		   									   controlFlow.under(SAMLLogoutFilter.class, "processLogout")									|| // SAMLLogoutFilter.processLogout ó
							    		   									   controlFlow.under(SAMLAuthenticationProvider.class, "getUserDetails")))))					|| // SAMLAuthenticationProvider.getUserDetails
							    (pjpSignature.getDeclaringType().equals(PersistentTokenRepository.class) &&	// ó 
							     (pjpSignature.getName().equals("getTokenForSeries") || 					// PersistentTokenRepository.getTokenForSeries() ó
							      pjpSignature.getName().equals("updateToken") || 							// PersistentTokenRepository.getTokenForSeries() ó
							      pjpSignature.getName().equals("removeUserTokens")) 						// PersistentTokenRepository.removeUserTokens
							    )))?																				
								null
								:SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			newArgs[newArgs.length-1]=(principal instanceof UserDetails)?genAuth((Serializable) principal):null;		
			
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

	private Object genAuth(Serializable principal) {
		if (principal instanceof mx.com.gunix.framework.security.UserDetails && ((mx.com.gunix.framework.security.UserDetails) principal).getSelectedAuthority() != null) {
			mx.com.gunix.framework.security.UserDetails gxud = (mx.com.gunix.framework.security.UserDetails) SerializationUtils.clone(principal);
			gxud.setAuthorities(gxud.getAuthorities()
										.stream()
										.filter(ga -> ga.getAuthority().equals(gxud.getSelectedAuthority()))
										.collect(Collectors.toList()));
			principal = gxud;
		}
		return principal;
	}

	private boolean compatibles(Class<?>[] argTypes, Class<?>[] mArgTypes) {
		Class<?>[] mTArgTypes = new Class<?>[argTypes.length];
		System.arraycopy(mArgTypes, 0, mTArgTypes, 0, argTypes.length);
		return Arrays.deepEquals(argTypes, mTArgTypes);
	}
}
