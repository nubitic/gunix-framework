package mx.com.gunix.framework.security.domain.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import mx.com.gunix.framework.security.domain.Funcion;

public class ValidaFuncionValidator implements ConstraintValidator<ValidaFuncion, Funcion> {

	@Override
	public void initialize(ValidaFuncion constraintAnnotation) {
	}

	@Override
	public boolean isValid(Funcion funcion, ConstraintValidatorContext context) {
		boolean isValid = true;
		if (funcion.getHijas() != null && !funcion.getHijas().isEmpty() && (funcion.getProcessKey() != null && !"".equals(funcion.getProcessKey()))) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate("{mx.com.gunix.framework.security.domain.funcion.constraints.padre}").addConstraintViolation();
			isValid = false;
		} else {
			if ((funcion.getHijas() == null || funcion.getHijas().isEmpty()) && (funcion.getProcessKey() == null || "".equals(funcion.getProcessKey()))) {
				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate("{mx.com.gunix.framework.security.domain.funcion.constraints.hija}").addConstraintViolation();
				isValid = false;
			}
		}
		return isValid;
	}

}
