package mx.com.gunix.framework.ui.vaadin.component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import mx.com.gunix.framework.domain.validation.GunixValidationGroups.BeanValidations;

import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.NestingBeanItem;
import com.vaadin.ui.Field;

public class GunixBeanFieldGroup<BT> extends BeanFieldGroup<BT> {

	public interface OnBeanValidationErrorCallback<BT> {
		void callback(ConstraintViolation<BT> cv);
	}

	private static final long serialVersionUID = 1L;
	private static ValidatorFactory factory;
	private transient javax.validation.Validator javaxBeanValidator;
	private Class<BT> beanType;

	public GunixBeanFieldGroup(Class<BT> beanType) {
		super(beanType);
		this.beanType = beanType;
		if (!isBeanValidationImplementationAvailable()) {
			throw new IllegalStateException("No se encontró una implementación de JSR-303 bean validation");
		}
	}

	private static ValidatorFactory getJavaxBeanValidatorFactory() {
		if (factory == null) {
			factory = Validation.buildDefaultValidatorFactory();
		}
		return factory;
	}

	private javax.validation.Validator getJavaxBeanValidator() {
		if (javaxBeanValidator == null) {
			javaxBeanValidator = getJavaxBeanValidatorFactory().getValidator();
		}
		return javaxBeanValidator;
	}

	@Override
	public void commit() throws CommitException {
		commit(null);
	}

	public void commit(OnBeanValidationErrorCallback<BT> onBVECallback) throws CommitException {
		super.commit();
		Set<ConstraintViolation<BT>> errores = getJavaxBeanValidator().validate(getItemDataSource().getBean(), BeanValidations.class);
		if (errores != null && !errores.isEmpty()) {
			Map<Field<?>, InvalidValueException> invalidValueExceptions = new HashMap<Field<?>, InvalidValueException>();
			if (onBVECallback == null && getFields().iterator().hasNext()) {
				for (ConstraintViolation<BT> cv : errores) {
					Field<?> field = getField(cv.getPropertyPath().toString());
					if (field != null) {
						InvalidValueException ive = new InvalidValueException(cv.getMessage());
						invalidValueExceptions.put(field, new InvalidValueException(cv.getMessage(), new InvalidValueException[] { ive }));
					}
				}
				if (!invalidValueExceptions.isEmpty()) {
					throw new CommitException("Commit Failed!", this, new FieldGroupInvalidValueException(invalidValueExceptions));
				}
			} else {
				if (onBVECallback != null) {
					for (ConstraintViolation<BT> cv : errores) {
						onBVECallback.callback(cv);
					}
					throw new CommitException();
				} else {
					throw new IllegalStateException("No se asoció (bind) ningún campo al grupo ni tampoco se indicó un callback para atender los errores en la validación");
				}
			}
		}
	}

	@Override
	public void setItemDataSource(BT bean) {
		if (bean == null) {
			setItemDataSource((Item) null);
		} else {
			setItemDataSource(new NestingBeanItem<BT>(bean, beanType));
		}
	}
}
