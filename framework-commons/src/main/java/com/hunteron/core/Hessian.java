package com.hunteron.core;
/**
 * hessian
 * 
 * @author rocca.peng@hunteron.com
 * @Description
 * @Date 2015
 */
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Indica si una determinada clase se debe identificar como un Servicio Gunix (Soportado en el back con Hessian)*/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Hessian {
	 String value(); //Para indicar la URI del servicio
	 Context host() default Context.HOST; //Indica el HOST en donde vive el servicio
}
