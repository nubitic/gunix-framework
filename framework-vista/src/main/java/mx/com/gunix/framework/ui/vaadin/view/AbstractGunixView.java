package mx.com.gunix.framework.ui.vaadin.view;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.processes.domain.Tarea;
import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.service.ActivitiService;
import mx.com.gunix.framework.service.GetterService;
import mx.com.gunix.framework.service.TokenService;
import mx.com.gunix.framework.ui.GunixVariableGetter;
import mx.com.gunix.framework.ui.vaadin.component.GunixBeanFieldGroup;
import mx.com.gunix.framework.ui.vaadin.component.GunixBeanFieldGroup.OnBeanValidationErrorCallback;
import mx.com.gunix.framework.ui.vaadin.component.GunixTableHelper;
import mx.com.gunix.framework.ui.vaadin.component.GunixUploadField;
import mx.com.gunix.framework.ui.vaadin.component.GunixViewErrorHandler;
import mx.com.gunix.framework.ui.vaadin.component.Header.TareaActualNavigator;
import mx.com.gunix.framework.util.ActivitiGunixFile;
import mx.com.gunix.framework.util.GunixFile;
import mx.com.gunix.framework.util.GunixLogger;

public abstract class AbstractGunixView<S extends Serializable> extends VerticalLayout implements View {
	private static final ThreadLocal<Map<Notification.Type, Set<String>>> notificacionesMap = new ThreadLocal<Map<Notification.Type, Set<String>>>();
	private Class<S> clazz;
	private static final long serialVersionUID = 1L;
	private static GunixLogger log;
	
	TareaActualNavigator taNav;
	private Tarea tarea;
	
	@Autowired
	GunixVariableGetter vg;
	
	@Autowired
	@Lazy
	ApplicationContext applicationContext;

	@Autowired
	@Lazy
	ActivitiService as;
	
	@Autowired
	@Lazy
	GetterService gs;
	
	@Autowired
	@Lazy
	TokenService ts;
	
	@Autowired
	MessageSource ms;
	
	public static void appendNotification(Notification.Type type, String notificacion) {
		Map<Notification.Type, Set<String>> notificaciones = notificacionesMap.get();
		if (notificaciones == null) {
			notificaciones = new HashMap<Notification.Type, Set<String>>();
			notificacionesMap.set(notificaciones);
		}
		
		Set<String> notificacionesPuntuales = notificaciones.get(type);
		if(notificacionesPuntuales == null) {
			notificacionesPuntuales = new LinkedHashSet<String>();
			notificaciones.put(type, notificacionesPuntuales);
		}
		
		notificacionesPuntuales.add(notificacion);
		UI.getCurrent().markAsDirty();
	}
	
	public static void doNotification() {
		Map<Notification.Type, Set<String>> notificaciones = notificacionesMap.get();
		if (notificaciones != null && !notificaciones.isEmpty()) {
			notificaciones.keySet().forEach(type -> {
				Set<String> notificacionesPuntuales = notificaciones.get(type);
				if (notificacionesPuntuales != null && !notificacionesPuntuales.isEmpty()) {
					StringBuilder notificationMessage = new StringBuilder();
					notificacionesPuntuales.forEach(notificacion -> {
						notificationMessage.append(notificacion).append("\n\r");
					});
					try {
						Notification.show(notificationMessage.toString(), type);
					} catch (IllegalStateException ignorar) {

					}
				}
			});
		}
		notificacionesMap.set(null);
	}
	
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private GunixBeanFieldGroup<S> fieldGroup;

	protected FormLayout flyt = new FormLayout();

	private void postInitFieldGroup() {
		if (fieldGroup != null) {
			fieldGroup.bindMemberFields(this);
		}
	}

	@PostConstruct
	private void postConstruct() {
		setWidth("100%");
		setHeight("100%");
		setSpacing(false);
		setMargin(true);
		setErrorHandler(GunixViewErrorHandler.getCurrent());
		setId(new StringBuilder(getClass().getName()).append(":").append(hashCode()).toString());
		taNav = (TareaActualNavigator) UI.getCurrent().getNavigator();
		tarea = taNav.getTareaActual();
		vg.setInstancia(tarea.getInstancia());
		preInitFieldGroup();
		doConstruct();
		addComponent(flyt);
		postInitFieldGroup();
	}

	@SuppressWarnings("unchecked")
	private void preInitFieldGroup() {
		Type genSuperType = getClass().getGenericSuperclass();
		if (genSuperType instanceof ParameterizedType) {
			Type[] typeArguments = ((ParameterizedType) genSuperType).getActualTypeArguments();
			if (typeArguments.length == 1) {
				if(typeArguments[0] instanceof Class) {
					clazz = ((Class<S>) typeArguments[0]);
					fieldGroup = new GunixBeanFieldGroup<S>(clazz);
					try {
						fieldGroup.setItemDataSource(clazz.newInstance());
					} catch (InstantiationException | IllegalAccessException e) {
						throw new IllegalArgumentException("No fue posible inicializar el ItemDataSource para el fieldGroup con una nueva instancia de " + clazz.getName(), e);
					}	
				}
			}
		}
	}
	
	protected final void camposVaciosSonValidos() {
		if (fieldGroup != null) {
			fieldGroup.setRequiredEnabled(false);
		}
	}
	
	protected void initBean(S fuente) {
		if (fuente != null) {
			BeanUtils.copyProperties(fuente, getBean());
		}
	}
	
	protected final void completaTarea() {
		tarea.setVariables(getVariablesTarea());
		tarea.setComentario(getComentarioTarea());
		
		try {
			Instancia instancia = as.completaTarea(tarea);
			String taskView = null;
			Tarea tareaAct = instancia.getTareaActual();
			if (tareaAct == null || (taskView = tareaAct.getVista()).equals(Tarea.DEFAULT_END_TASK_VIEW)) {
				taskView = DefaultProcessEndView.class.getName();
			}

			try {
				taNav.setTareaActual(instancia.getTareaActual());
				taNav.navigateTo(taskView);
			} finally {
				taNav.setTareaActual(null);
			}
		} catch (Throwable exp) {
			String mensajeError = exp.getMessage();
			if (mensajeError != null && mensajeError.indexOf("GunixHessianServiceException") != -1) {
				Notification.show("Se presentó un error al procesar su solicitud: " + mensajeError.split("GunixHessianServiceException")[1] ,Notification.Type.ERROR_MESSAGE);
			}
			throw exp;
		}
	}

	protected final <T extends Component> T getBeanComponent(Class<T> beanClass) {
		return applicationContext.getBean(beanClass);
	}

	protected final Serializable $(String nombreVariable) {
		return vg.get(nombreVariable);
	}

	protected final S getBean() {
		if (fieldGroup == null) {
			throw new IllegalStateException("No se puede obtener el Bean dado que no se indicó el tipo específico para la clase genérica AbstractGunixView");
		}
		return fieldGroup.getItemDataSource().getBean();
	}

	protected final void commit() throws CommitException {
		validaFieldGroup();
		fieldGroup.commit();
	}

	protected final void commit(Class<?>... groups) throws CommitException {
		validaFieldGroup();
		fieldGroup.commit(groups);
	}
	
	protected final void commit(OnBeanValidationErrorCallback<S> onBVECallback) throws CommitException {
		validaFieldGroup();
		fieldGroup.commit(onBVECallback);
	}

	protected final void commit(OnBeanValidationErrorCallback<S> onBVECallback, Class<?>... groups) throws CommitException {
		validaFieldGroup();
		fieldGroup.commit(onBVECallback, groups);
	}

	private void validaFieldGroup() {
		if (fieldGroup == null) {
			throw new IllegalStateException("No se puede dar commit dado que no se indicó el tipo específico para la clase genérica AbstractGunixView");
		}
	}

	@Override
	public void enter(ViewChangeEvent event) {
		tarea = taNav.getTareaActual();
		doEnter(event);
		Responsive.makeResponsive(this);
		UI.getCurrent().markAsDirtyRecursive();
	}
	
	protected Tarea getTarea() {
		return tarea;
	}
	
	protected List<S> getBeans(Table tabla) {
		return GunixTableHelper.getBeans(tabla);
	}

	protected <R extends Serializable> boolean isValido(Table tabla, R bean, Class<R> clase) {
		return GunixTableHelper.isValido(tabla, bean, clase);
	}

	protected boolean isValido(Table tabla, S bean) {
		return GunixTableHelper.isValido(tabla, bean, clazz);
	}

	protected boolean isValida(Table tabla, boolean vacioEsError) {
		return GunixTableHelper.isValida(tabla, vacioEsError, clazz);
	}
	
	protected Variable<ActivitiGunixFile> buildGunixFileVariable(String nombreVariable, GunixUploadField uploadField) {
		Variable<ActivitiGunixFile> fileVar = new Variable<ActivitiGunixFile>();
		fileVar.setNombre(nombreVariable);
		GunixFile gf = (GunixFile) uploadField.getValue();
		fileVar.setValor(new ActivitiGunixFile(nombreVariable, gf.getFileName(), gf.getMimeType(), gf.getFile()));
		return fileVar;
	}
	
	protected Serializable get(String uri, Object... args) {
		Object[] argsWithPIDDIF = null;
		if (args != null) {
			argsWithPIDDIF = new Object[args.length + 3];
			System.arraycopy(args, 0, argsWithPIDDIF, 1, args.length);
			argsWithPIDDIF[0] = GetterService.INCLUDES_PID_PDID;
			argsWithPIDDIF[args.length + 1] = getTarea().getInstancia().getId();
			argsWithPIDDIF[args.length + 2] = getTarea().getInstancia().getProcessDefinitionId();
		} else {
			argsWithPIDDIF = new Object[3];
			argsWithPIDDIF[0] = GetterService.INCLUDES_PID_PDID;
			argsWithPIDDIF[1] = getTarea().getInstancia().getId();
			argsWithPIDDIF[2] = getTarea().getInstancia().getProcessDefinitionId();
		}

		return gs.get(uri, argsWithPIDDIF);
	}
	
	private void aseguraLogInicializado(){
		if (log == null) {
			log = GunixLogger.getLogger(getClass());
		}
	}
	
	protected void logD(Supplier<String> mensajeSupplier) {
		aseguraLogInicializado();
		log.log(Level.DEBUG, mensajeSupplier, null);
	}

	protected void logE(Supplier<Throwable> throwableSupplier) {
		aseguraLogInicializado();
		log.log(Level.ERROR, null, throwableSupplier);
	}

	protected void logE(Supplier<String> mensajeSupplier, Supplier<Throwable> throwableSupplier) {
		aseguraLogInicializado();
		log.log(Level.ERROR, mensajeSupplier, throwableSupplier);
	}
	
	protected void log(Level nivel, Supplier<String> mensajeSupplier, Supplier<Throwable> throwableSupplier) {
		aseguraLogInicializado();
		log.log(nivel, mensajeSupplier, throwableSupplier);
	}
	
	protected void bind(String propertyId, Field<?> field) {
		fieldGroup.bind(field, propertyId);
	}
	
	protected void registraTareaBG(String idTarea){
		ts.registraToken(idTarea);
	}
	
	protected void terminaTareaBG(String idTarea){
		ts.liberaToken(idTarea);
	}
	
	protected Boolean tareaBGTerminada(String idTarea){
		return ts.tokenActivo(idTarea);
	}
	
	protected void eliminaTareaBG(String idTarea){
		ts.eliminaToken(idTarea);
	}
	
	protected String gMssg(String mKey, String defaultMessage, Object... mArgs) {
		return mx.com.gunix.framework.util.Utils.procesaMensaje(ms, getClass(), mKey, defaultMessage, mArgs);
	}

	protected abstract void doEnter(ViewChangeEvent event);

	protected abstract void doConstruct();

	protected abstract List<Variable<?>> getVariablesTarea();

	protected abstract String getComentarioTarea();
}
