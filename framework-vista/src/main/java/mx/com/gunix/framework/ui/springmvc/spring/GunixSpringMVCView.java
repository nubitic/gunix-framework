package mx.com.gunix.framework.ui.springmvc.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Documented
@Controller
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public @interface GunixSpringMVCView {
	String value() default "na";
}
