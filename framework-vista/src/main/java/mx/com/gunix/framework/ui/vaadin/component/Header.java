package mx.com.gunix.framework.ui.vaadin.component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.lexaden.breadcrumb.Breadcrumb;
import com.vaadin.annotations.AutoGenerated;
import com.vaadin.navigator.NavigationStateManager;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.ClassResource;
import com.vaadin.server.Extension;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.BrowserFrame;
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

import mx.com.gunix.framework.processes.domain.Instancia;
import mx.com.gunix.framework.processes.domain.Tarea;
import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.security.UserDetails;
import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.Funcion;
import mx.com.gunix.framework.security.domain.Funcion.ViewEngine;
import mx.com.gunix.framework.security.domain.Modulo;
import mx.com.gunix.framework.security.domain.Rol;
import mx.com.gunix.framework.service.ActivitiService;
import mx.com.gunix.framework.ui.vaadin.spring.SpringViewProvider;
import mx.com.gunix.framework.ui.vaadin.view.DefaultProcessEndView;

@Component
@Scope("prototype")
public class Header extends CustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	protected VerticalLayout mainLayout;

	@AutoGenerated
	protected Panel panelContenido;

	@AutoGenerated
	private VerticalLayout verticalLayout_2;

	@AutoGenerated
	protected GridLayout modulosLayout;

	@AutoGenerated
	protected HorizontalLayout breadCrumbLayout;

	@AutoGenerated
	protected Panel userDetailsPanel;

	@AutoGenerated
	protected HorizontalLayout rolMenuLayout;

	@AutoGenerated
	protected MenuBar menuBar;

	@AutoGenerated
	protected ComboBox rolCBox;

	protected Breadcrumb breadCrumb;
	
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
		menuBar.setId("gx_menuBar");
		rolCBox.setInputPrompt("Seleccione un Rol");
		rolCBox.setId("gx_rol_cbx");
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
		rolMenuLayout.setComponentAlignment(menuBar, Alignment.MIDDLE_LEFT);

		aplicacion.getRoles().stream().forEach(rol -> {
			rolCBox.addItem(rol.getIdRol());
			rolCBox.setItemCaption(rol.getIdRol(), rol.getDescripcion());
		});

		rolCBox.addValueChangeListener(vchlnrEv -> {
			panelContenido.setVisible(false);
			panelContenido.setEnabled(false);
			menuBar.removeItems();
			menuBar.setEnabled(false);
			menuBar.setCaption("");
			initBreadCrumb();
			Optional<Rol> rolSelOpt = aplicacion.getRoles().stream().filter(rol -> rol.getIdRol().equals(rolCBox.getValue())).findFirst();

			rolSelOpt.ifPresent(rolSel -> {
				doRenderModulos(rolSel.getModulos(), rolSel);
				((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).setSelectedAuthority(aplicacion.getIdAplicacion() + "_" + rolSel.getIdRol());
			});
		});
	}

	protected void doRenderModulos(List<Modulo> modulos, Rol rolSel) {
		modulosLayout.removeAllComponents();
		modulosLayout.setVisible(true);
		modulosLayout.setRows(3);// Rows Iniciales
		modulosLayout.setColumns((MODULOS_POR_FILA * 2) + 1);
		boolean isOneModulo = modulos.size() == 1;
		if(isOneModulo) {
			seleccionaModulo(rolSel, modulos.get(0));
		} else {
			int filasModulos = ((filasModulos = modulos.size()) % MODULOS_POR_FILA == 0) ? filasModulos / MODULOS_POR_FILA : (filasModulos / MODULOS_POR_FILA) + 1;
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
					Modulo modulo = modulos.get(modulosProcesados);
					Image button = new Image(modulo.getDescripcion(), new ThemeResource("img/" + modulo.getIcono()));
					button.addStyleName("moduleImageButton");
					button.addClickListener(clickEvnt -> {
						seleccionaModulo(rolSel, modulo);
					});
					modulosLayout.addComponent(button, col + colIncr, row + rowIncr);

					modulosLayout.setComponentAlignment(button, Alignment.TOP_CENTER);
					modulosProcesados++;
					colIncr++;
					if (modulosProcesados == modulos.size()) {
						break;
					}
				}
				rowIncr++;
			}
		}
	}

	protected void seleccionaModulo(Rol rol, Modulo modulo) {
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
			padre.setStyleName("gx_" + rol.getAplicacion().getIdAplicacion() + "_" + rol.getIdRol() + "_" + funcion.getIdFuncion());
			padre.setEnabled(true);
			recorreFuncionesHijas(rol, padre, optHijas);
		});
		menuBar.setCaption(modulo.getDescripcion());
	}

	private void recorreFuncionesHijas(Rol rol, MenuItem padre, Optional<List<Funcion>> optHijas) {
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
										doFuncion(rol, funcion);
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
											doFuncion(rol, funcion);
										} else {
											Notification.show("Funcionalidad Cerrada", "La funcionalidad seleccionada se encuentra disponible sólo de lunes a viernes en el Horario de 09:00 a 18:00", Type.ERROR_MESSAGE);
										}
										break;
									}
									break;
								case PERSONALIZADO:
									break;
								default:
									doFuncion(rol, funcion);
								}
							});
						}
						nvoPadre.setEnabled(true);
						nvoPadre.setStyleName("gx_" + rol.getAplicacion().getIdAplicacion() + "_" + rol.getIdRol() + "_" + funcion.getIdFuncion());
						recorreFuncionesHijas(rol, nvoPadre, optHijas2);
					});
		});
	}

	private void doFuncion(Rol rol, Funcion funcion) {
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
			try {
				modulosLayout.setVisible(false);
				panelContenido.setVisible(true);
				panelContenido.setEnabled(true);
				if (funcion.getViewEngine() == ViewEngine.SPRINGMVC) {
					BrowserFrame bf = new BrowserFrame("gunix", new ExternalResource(doGetURL(rol,funcion)));
					bf.setSizeFull();
					bf.setHeight((UI.getCurrent().getPage().getBrowserWindowHeight() - 240) + "px");
					panelContenido.setContent(bf);
				} else {
					Instancia instancia = as.iniciaProceso(funcion.getProcessKey(), Variable.fromParametros(funcion.getParametros()), "");
					navigator.setTareaActual(instancia.getTareaActual());

					if (instancia.getTareaActual() == null || instancia.getTareaActual().getVista().equals(Tarea.DEFAULT_END_TASK_VIEW)) {
						navigator.navigateTo(DefaultProcessEndView.class.getName());
					} else {
						navigator.navigateTo(instancia.getTareaActual().getVista());
					}
				}
				initBreadCrumb();
				updateBreadcrumb(funcion);
				breadCrumbLayout.setVisible(true);
			} finally {
				navigator.setTareaActual(null);
			}
		}
	}

	protected String doGetURL(Rol rol, Funcion funcion) {
		return "../startProcess?idAplicacion=" + rol.getAplicacion().getIdAplicacion() + "&idRol=" + rol.getIdRol() + "&idModulo=" + funcion.getModulo().getIdModulo() + "&idFuncion=" + funcion.getIdFuncion();
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
		rolMenuLayout = buildHorizontalLayout_2();
		userDetailsPanel.setContent(rolMenuLayout);

		return userDetailsPanel;
	}

	@AutoGenerated
	private HorizontalLayout buildHorizontalLayout_2() {
		// common part: create layout
		rolMenuLayout = new HorizontalLayout();
		rolMenuLayout.setImmediate(false);
		rolMenuLayout.setWidth("100.0%");
		rolMenuLayout.setHeight("100.0%");
		rolMenuLayout.setMargin(true);
		rolMenuLayout.setSpacing(true);

		// rolCBox
		rolCBox = new ComboBox();
		rolCBox.setCaption("Rol");
		rolCBox.setImmediate(true);
		rolCBox.setWidth("100.0%");
		rolCBox.setHeight("-1px");
		rolMenuLayout.addComponent(rolCBox);
		rolMenuLayout.setExpandRatio(rolCBox, 1.0f);
		rolMenuLayout.setComponentAlignment(rolCBox, Alignment.MIDDLE_LEFT);

		// menuBar
		menuBar = new MenuBar();
		menuBar.setEnabled(false);
		menuBar.setImmediate(false);
		menuBar.setWidth("100.0%");
		menuBar.setHeight("-1px");
		rolMenuLayout.addComponent(menuBar);
		rolMenuLayout.setExpandRatio(menuBar, 3.0f);

		return rolMenuLayout;
	}

	@AutoGenerated
	private Panel buildPanelContenido() {
		// common part: create layout
		panelContenido = new Panel();
		panelContenido.setEnabled(false);
		panelContenido.setImmediate(false);
		panelContenido.setVisible(false);
		panelContenido.setWidth((UI.getCurrent().getPage().getBrowserWindowWidth()-55)+"px");
		UI.getCurrent().getPage().addBrowserWindowResizeListener(evnt -> {
			panelContenido.setWidth((evnt.getWidth() - 55) + "px");
		});
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
