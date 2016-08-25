package mx.com.gunix.framework.ui.vaadin.component;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

import mx.com.gunix.framework.domain.validation.GunixValidationGroups.BeanValidations;
import mx.com.gunix.framework.ui.vaadin.VaadinUtils;

import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.NestingBeanItem;
import com.vaadin.data.validator.BeanValidator;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;

public class GunixBeanFieldGroup<BT> extends BeanFieldGroup<BT> {
	public interface OnBeanValidationErrorCallback<BT> {
		void callback(ConstraintViolation<BT> cv);
	}

	private static final long serialVersionUID = 1L;
	private static ValidatorFactory factory;
	private transient javax.validation.Validator javaxBeanValidator;
	private Class<BT> beanType;
	private final Map<Field<?>, BeanValidator> defaultValidators;
	private boolean requiredEnabled = true;

	public GunixBeanFieldGroup(Class<BT> beanType) {
		super(beanType);
		this.beanType = beanType;
		this.defaultValidators = new HashMap<Field<?>, BeanValidator>();
		setBuffered(false);
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
		commit(null, null);
	}

	public void commit(OnBeanValidationErrorCallback<BT> onBVECallback) throws CommitException {
		commit(onBVECallback, null);
	}

	public void commit(Class<?>... groups) throws CommitException {
		commit(null, groups);
	}

	public void commit(OnBeanValidationErrorCallback<BT> onBVECallback, Class<?>... groups) throws CommitException {
		Map<Field<?>, InvalidValueException> invalidValueExceptions = new HashMap<Field<?>, InvalidValueException>();
		defaultValidators.keySet().forEach(field -> {
			GunixViewErrorHandler errorHandler = GunixViewErrorHandler.getCurrent();
			if (errorHandler.isInvalidValueComponent(field)) {
				InvalidValueException ive = new InvalidValueException(((AbstractComponent) field).getComponentError().getFormattedHtmlMessage());
				invalidValueExceptions.put(field, new InvalidValueException(ive.getMessage(), new InvalidValueException[] { ive }));
			} else {
				try {
					field.commit();
				} catch (InvalidValueException e) {
					invalidValueExceptions.put(field, e);
				}
			}
		});
		if (!invalidValueExceptions.isEmpty()) {
			throw new CommitException("Commit Failed!", this, new FieldGroupInvalidValueException(invalidValueExceptions));
		}
		Set<ConstraintViolation<BT>> errores = getJavaxBeanValidator().validate(getItemDataSource().getBean(), groups == null ? new Class<?>[] { BeanValidations.class } : groups);
		if (errores != null && !errores.isEmpty()) {
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
	public void unbind(Field<?> field) throws BindException {
		super.unbind(field);

		BeanValidator removed = defaultValidators.remove(field);
		if (removed != null) {
			field.removeValidator(removed);
		}
	}

	@Override
	protected void configureField(Field<?> field) {
		field.setBuffered(isBuffered());
		field.setErrorHandler(GunixViewErrorHandler.getCurrent());

		if (field instanceof AbstractField) {
			((AbstractField<?>) field).setConversionError(VaadinUtils.getConversionError(field.getPropertyDataSource().getType()));
		}
		if (field instanceof DateField && "Date format not recognized".equals(((DateField) field).getParseErrorMessage())) {
			((DateField) field).setParseErrorMessage("Fecha Inválida");
		}
		// Add Bean validators if there are annotations
		if (isBeanValidationImplementationAvailable() && !defaultValidators.containsKey(field)) {
			GunixBeanValidator validator = new GunixBeanValidator(beanType, getPropertyId(field).toString());
			field.addValidator(validator);
			if (field.getLocale() != null) {
				validator.setLocale(field.getLocale());
			}
			defaultValidators.put(field, validator);
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
	public boolean isRequiredEnabled() {
		return requiredEnabled;
	}

	public void setRequiredEnabled(boolean requiredEnabled) {
		this.requiredEnabled = requiredEnabled;
	}
	private class GunixBeanValidator extends BeanValidator {
		private static final long serialVersionUID = 1L;
		private String propertyName;
		private Locale locale;

		public GunixBeanValidator(Class<BT> beanClass, String propertyName) {
			super(beanType, propertyName);
			this.propertyName = propertyName;
			locale = Locale.getDefault();
		}

		@Override
		public void validate(Object value) throws InvalidValueException {
			Set<ConstraintViolation<BT>> violations = getJavaxBeanValidator().validateValue(beanType, propertyName, value);
			if (violations.size() > 0) {
				List<InvalidValueException> causes = new ArrayList<InvalidValueException>();
				for (ConstraintViolation<BT> v : violations) {
					if (!requiredEnabled && v.getConstraintDescriptor().getAnnotation() != null) {
						Annotation annotation = v.getConstraintDescriptor().getAnnotation();
						if (annotation.annotationType() == NotNull.class || annotation.annotationType() == Size.class || annotation.annotationType() == NotBlank.class) {
							continue;
						}
					}

					final ConstraintViolation<?> violation = (ConstraintViolation<?>) v;
					String msg = getJavaxBeanValidatorFactory().getMessageInterpolator().interpolate(violation.getMessageTemplate(), new SimpleContext(value, violation.getConstraintDescriptor()), locale);
					causes.add(new InvalidValueException(msg));
				}
				if (!causes.isEmpty()) {
					throw new InvalidValueException(null, causes.toArray(new InvalidValueException[] {}));
				}
			}
		}
	}
}
