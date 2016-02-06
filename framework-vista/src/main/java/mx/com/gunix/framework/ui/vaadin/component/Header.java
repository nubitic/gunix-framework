package mx.com.gunix.framework.ui.vaadin.component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.processes.domain.Tarea;
import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.Funcion;
import mx.com.gunix.framework.security.domain.Modulo;
import mx.com.gunix.framework.security.domain.Rol;
import mx.com.gunix.framework.service.ActivitiService;
import mx.com.gunix.framework.ui.vaadin.spring.SpringViewProvider;
import mx.com.gunix.framework.ui.vaadin.view.DefaultProcessEndView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.lexaden.breadcrumb.Breadcrumb;
import com.vaadin.annotations.AutoGenerated;
import com.vaadin.navigator.NavigationStateManager;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.ClassResource;
import com.vaadin.server.Extension;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field.ValueChangeEvent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.SingleComponentContainer;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Component
@Scope("prototype")
public class Header extends CustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout mainLayout;

	@AutoGenerated
	private Panel panelContenido;

	@AutoGenerated
	private VerticalLayout verticalLayout_2;

	@AutoGenerated
	private GridLayout modulosLayout;

	@AutoGenerated
	private HorizontalLayout breadCrumbLayout;

	@AutoGenerated
	private Panel userDetailsPanel;

	@AutoGenerated
	private HorizontalLayout horizontalLayout_2;

	@AutoGenerated
	private MenuBar menuBar;

	@AutoGenerated
	private ComboBox rolCBox;

	private Breadcrumb breadCrumb;
	
	private Button downloadInvisibleButton;
	
	public class TareaActualNavigator extends Navigator {
		private static final long serialVersionUID = 1L;
		private Tarea tareaActual;

		private TareaActualNavigator(UI ui, SingleComponentContainer container) {
			super(ui, container);
		}

		public void setTareaActual(Tarea tareaActual) {
			this.tareaActual = tareaActual;
		}

		@Override
		protected NavigationStateManager getStateManager() {
			return null;
		}

		public Tarea getTareaActual() {
			return tareaActual;
		}
	}

	@Autowired
	@Lazy
	ActivitiService as;

	@Autowired
	SpringViewProvider svp;

	TareaActualNavigator navigator;

	final private int MODULOS_POR_FILA = 3;

	private Aplicacion aplicacion;
	private static final long serialVersionUID = 1L;

	public static final String DESCARGA_ARCHIVO = "da:";
	private static final String DESCARGA_ARCHIVO_BUTTON_ID = "DownloadButtonId";
	private static final String JAVASCRIPT_DESCARGA_ARCHIVO = "document.getElementById('"+DESCARGA_ARCHIVO_BUTTON_ID+"').click();";

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	public Header() {
		buildMainLayout();
		menuBar.setCaption("");
		rolCBox.setInputPrompt("Seleccione un Rol");
		setCompositionRoot(mainLayout);
		navigator = new TareaActualNavigator(UI.getCurrent(), panelContenido);
		breadCrumbLayout.setSpacing(false);

		setSizeFull();
	}

	private void initBreadCrumb() {
		if (breadCrumb != null) {
			breadCrumbLayout.removeComponent(breadCrumb);
		}
		breadCrumb = new Breadcrumb();
		breadCrumb.setShowAnimationSpeed(Breadcrumb.AnimSpeed.SLOW);
		breadCrumb.setHideAnimationSpeed(Breadcrumb.AnimSpeed.SLOW);
		breadCrumb.setUseDefaultClickBehaviour(false);
		Button homeButton = new Button();
		homeButton.addClickListener(clevnt -> {
			rolCBox.valueChange(new ValueChangeEvent(rolCBox));
		});
		breadCrumb.addLink(homeButton);
		breadCrumb.setLinkEnabled(true, 0);
		breadCrumbLayout.setVisible(false);
		breadCrumbLayout.addComponent(breadCrumb);
		breadCrumbLayout.setComponentAlignment(breadCrumb, Alignment.MIDDLE_LEFT);
	}

	public Navigator getNavigator() {
		return navigator;
	}

	public void renderHeader(Aplicacion aplicacion) {
		this.aplicacion = aplicacion;
		setId(new StringBuilder(getClass().getName()).append(":").append(aplicacion.getIdAplicacion()).toString());
		navigator.addProvider(svp);

		mainLayout.setExpandRatio(userDetailsPanel, 0.0f);
		mainLayout.setExpandRatio(modulosLayout, 1.0f);
		horizontalLayout_2.setComponentAlignment(menuBar, Alignment.MIDDLE_LEFT);

		aplicacion.getRoles().stream().forEach(rol -> {
			rolCBox.addItem(rol.getIdRol());
			rolCBox.setItemCaption(rol.getIdRol(), rol.getDescripcion());
		});

		rolCBox.addValueChangeListener(vchlnrEv -> {
			modulosLayout.removeAllComponents();
			modulosLayout.setVisible(true);
			panelContenido.setVisible(false);
			panelContenido.setEnabled(false);
			menuBar.removeItems();
			menuBar.setEnabled(false);
			menuBar.setCaption("");
			initBreadCrumb();
			modulosLayout.setRows(3);// Rows Iniciales
			modulosLayout.setColumns((MODULOS_POR_FILA * 2) + 1);

			Optional<Rol> rolSelOpt = aplicacion.getRoles().stream().filter(rol -> rol.getIdRol().equals(rolCBox.getValue())).findFirst();

			rolSelOpt.ifPresent(rolSel -> {
				boolean isOneModulo = rolSel.getModulos().size() == 1;
				if(isOneModulo) {
					seleccionaModulo(rolSel.getModulos().get(0));
				} else {
					int filasModulos = ((filasModulos = rolSel.getModulos().size()) % MODULOS_POR_FILA == 0) ? filasModulos / MODULOS_POR_FILA : (filasModulos / MODULOS_POR_FILA) + 1;
					if (filasModulos > 1) {
						modulosLayout.setRows(1 + (filasModulos * 2));
					}

					int modulosProcesados = 0;
					int rowIncr = 1;
					for (int row = 0; row < filasModulos; row++) {
						if (row % 2 != 0) {
							modulosLayout.setRowExpandRatio(row, 1);
						} else {
							modulosLayout.setRowExpandRatio(row, 2);
						}
						int colIncr = isOneModulo ? 3 : 1;
						for (int col = 0; col < MODULOS_POR_FILA; col++) {
							Modulo modulo = rolSel.getModulos().get(modulosProcesados);
							Image button = new Image(modulo.getDescripcion(), new ThemeResource("img/" + modulo.getIcono()));
							button.addStyleName("moduleImageButton");
							button.addClickListener(clickEvnt -> {
								seleccionaModulo(modulo);
							});
							modulosLayout.addComponent(button, col + colIncr, row + rowIncr);

							modulosLayout.setComponentAlignment(button, Alignment.TOP_CENTER);
							modulosProcesados++;
							colIncr++;
							if (modulosProcesados == rolSel.getModulos().size()) {
								break;
							}
						}
						rowIncr++;
					}
				}
			});
		});

	}

	private void seleccionaModulo(Modulo modulo) {
		menuBar.removeItems();
		menuBar.setCaption("");
		menuBar.setEnabled(true);
		modulosLayout.setVisible(false);
		modulo.getFunciones().stream().forEach(funcion -> {
			Optional<List<Funcion>> optHijas = Optional.ofNullable(funcion.getHijas());
			MenuItem padre = null;
			if (optHijas.isPresent()) {
				padre = menuBar.addItem(funcion.getTitulo(), null);
			} else {
				padre = menuBar.addItem(funcion.getTitulo(), selectedItem -> {
					Notification.show(funcion.getDescripcion());
				});
			}

			padre.setEnabled(true);
			recorreFuncionesHijas(padre, optHijas);
		});
		menuBar.setCaption(modulo.getDescripcion());
	}

	private void recorreFuncionesHijas(MenuItem padre, Optional<List<Funcion>> optHijas) {
		optHijas.ifPresent(hijas -> {
			hijas.stream().forEach(
					funcion -> {
						Optional<List<Funcion>> optHijas2 = Optional.ofNullable(funcion.getHijas());
						MenuItem nvoPadre = null;
						if (optHijas2.isPresent()) {
							nvoPadre = padre.addItem(funcion.getTitulo(), null);
						} else {
							nvoPadre = padre.addItem(funcion.getTitulo(), selectedItem -> {
								LocalDateTime now = LocalDateTime.now();
								DayOfWeek today = now.getDayOfWeek();
								switch (funcion.getHorario()) {
								case LV24:
									switch (today) {
									case SUNDAY:
									case SATURDAY:
										Notification.show("Funcionalidad Cerrada", "La funcionalidad seleccionada se encuentra disponible sólo de lunes a viernes", Type.ERROR_MESSAGE);
										break;
									default:
										doFuncion(funcion);
										break;
									}
									break;
								case LV9_18:
									switch (today) {
									case SUNDAY:
									case SATURDAY:
										Notification.show("Funcionalidad Cerrada", "La funcionalidad seleccionada se encuentra disponible sólo de lunes a viernes en el Horario de 09:00 a 18:00", Type.ERROR_MESSAGE);
										break;
									default:
										int hora = now.getHour();
										if (hora >= 9 && hora <= 18) {
											doFuncion(funcion);
										} else {
											Notification.show("Funcionalidad Cerrada", "La funcionalidad seleccionada se encuentra disponible sólo de lunes a viernes en el Horario de 09:00 a 18:00", Type.ERROR_MESSAGE);
										}
										break;
									}
									break;
								case PERSONALIZADO:
									break;
								default:
									doFuncion(funcion);
								}
							});
						}
						nvoPadre.setEnabled(true);
						recorreFuncionesHijas(nvoPadre, optHijas2);
					});
		});
	}

	private void doFuncion(Funcion funcion) {
		if (funcion.getProcessKey().startsWith(DESCARGA_ARCHIVO)) {
			FileDownloader fileDownloader = new FileDownloader(new ClassResource(funcion.getProcessKey().split(":")[1]));
			if (downloadInvisibleButton.getExtensions() != null) {
				Extension ext = null;
				Iterator<Extension> extIt = downloadInvisibleButton.getExtensions().iterator();
				while (extIt.hasNext()) {
					ext = extIt.next();
					break;
				}
				if (ext != null) {
					downloadInvisibleButton.removeExtension(ext);
				}
			}
			fileDownloader.extend(downloadInvisibleButton);
			Page.getCurrent().getJavaScript().execute(JAVASCRIPT_DESCARGA_ARCHIVO);
		} else {
			Instancia instancia = as.iniciaProceso(funcion.getProcessKey(), Variable.fromParametros(funcion.getParametros()), "");
			try {
				navigator.setTareaActual(instancia.getTareaActual());

				if (instancia.getTareaActual() == null || instancia.getTareaActual().getVista().equals(Tarea.DEFAULT_END_TASK_VIEW)) {
					navigator.navigateTo(DefaultProcessEndView.class.getName());
				} else {
					navigator.navigateTo(instancia.getTareaActual().getVista());
				}
				modulosLayout.setVisible(false);
				panelContenido.setVisible(true);
				panelContenido.setEnabled(true);
				initBreadCrumb();
				updateBreadcrumb(funcion);
				breadCrumbLayout.setVisible(true);
			} finally {
				navigator.setTareaActual(null);
			}
		}
	}

	private void updateBreadcrumb(Funcion funcion) {
		if (funcion.getPadre() != null) {
			updateBreadcrumb(funcion.getPadre());
		}
		Button button = new Button(funcion.getTitulo());
		breadCrumb.addLink(button);
		breadCrumb.setLinkEnabled(false, breadCrumb.getIndexOfLink(button));
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);

		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");

		// userDetailsPanel
		userDetailsPanel = buildUserDetailsPanel();
		mainLayout.addComponent(userDetailsPanel);
		mainLayout.setComponentAlignment(userDetailsPanel, new Alignment(6));

		// breadCrumbLayout
		breadCrumbLayout = new HorizontalLayout();
		breadCrumbLayout.setImmediate(false);
		breadCrumbLayout.setWidth("-1px");
		breadCrumbLayout.setHeight("-1px");
		breadCrumbLayout.setMargin(false);
		mainLayout.addComponent(breadCrumbLayout);

		// modulosLayout
		modulosLayout = new GridLayout();
		modulosLayout.setImmediate(false);
		modulosLayout.setWidth("550px");
		modulosLayout.setHeight("-1px");
		modulosLayout.setMargin(false);
		modulosLayout.setSpacing(true);
		mainLayout.addComponent(modulosLayout);
		mainLayout.setExpandRatio(modulosLayout, 20.0f);
		mainLayout.setComponentAlignment(modulosLayout, new Alignment(48));

		// panelContenido
		panelContenido = buildPanelContenido();
		mainLayout.addComponent(panelContenido);
		mainLayout.setComponentAlignment(panelContenido, new Alignment(48));

		downloadInvisibleButton = new Button();
		downloadInvisibleButton.setId(DESCARGA_ARCHIVO_BUTTON_ID);
		downloadInvisibleButton.addStyleName("invisibleDownloadButton");
		mainLayout.addComponent(downloadInvisibleButton);
		
		return mainLayout;
	}

	@AutoGenerated
	private Panel buildUserDetailsPanel() {
		// common part: create layout
		userDetailsPanel = new Panel();
		userDetailsPanel.setImmediate(false);
		userDetailsPanel.setWidth("100.0%");
		userDetailsPanel.setHeight("90px");

		// horizontalLayout_2
		horizontalLayout_2 = buildHorizontalLayout_2();
		userDetailsPanel.setContent(horizontalLayout_2);

		return userDetailsPanel;
	}

	@AutoGenerated
	private HorizontalLayout buildHorizontalLayout_2() {
		// common part: create layout
		horizontalLayout_2 = new HorizontalLayout();
		horizontalLayout_2.setImmediate(false);
		horizontalLayout_2.setWidth("100.0%");
		horizontalLayout_2.setHeight("100.0%");
		horizontalLayout_2.setMargin(true);
		horizontalLayout_2.setSpacing(true);

		// rolCBox
		rolCBox = new ComboBox();
		rolCBox.setCaption("Rol");
		rolCBox.setImmediate(true);
		rolCBox.setWidth("100.0%");
		rolCBox.setHeight("-1px");
		horizontalLayout_2.addComponent(rolCBox);
		horizontalLayout_2.setExpandRatio(rolCBox, 1.0f);
		horizontalLayout_2.setComponentAlignment(rolCBox, Alignment.MIDDLE_LEFT);

		// menuBar
		menuBar = new MenuBar();
		menuBar.setEnabled(false);
		menuBar.setImmediate(false);
		menuBar.setWidth("100.0%");
		menuBar.setHeight("-1px");
		horizontalLayout_2.addComponent(menuBar);
		horizontalLayout_2.setExpandRatio(menuBar, 3.0f);

		return horizontalLayout_2;
	}

	@AutoGenerated
	private Panel buildPanelContenido() {
		// common part: create layout
		panelContenido = new Panel();
		panelContenido.setEnabled(false);
		panelContenido.setImmediate(false);
		panelContenido.setVisible(false);
		panelContenido.setWidth("100.0%");
		panelContenido.setHeight("100.0%");

		// verticalLayout_2
		verticalLayout_2 = new VerticalLayout();
		verticalLayout_2.setEnabled(false);
		verticalLayout_2.setImmediate(false);
		verticalLayout_2.setVisible(false);
		verticalLayout_2.setWidth("100.0%");
		verticalLayout_2.setHeight("100.0%");
		verticalLayout_2.setMargin(false);
		panelContenido.setContent(verticalLayout_2);

		return panelContenido;
	}

	public Aplicacion getAplicacion() {
		return aplicacion;
	}
}
