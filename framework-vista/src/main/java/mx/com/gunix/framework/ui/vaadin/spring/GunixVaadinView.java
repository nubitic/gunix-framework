package mx.com.gunix.framework.ui.vaadin.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.vaadin.spring.annotation.VaadinComponent;
import org.vaadin.spring.navigator.annotation.VaadinViewScope;

import com.vaadin.ui.UI;

@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Documented
@VaadinComponent
@VaadinViewScope
public @interface GunixVaadinView {
	String INDEX = "index";
	String NORMAL = "normal";

	Class<? extends UI>[] ui() default {};

	String tipo() default NORMAL;
}
