package mx.com.gunix.framework.ui.vaadin.view;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.spring.security.VaadinSecurity;

import com.vaadin.data.Container.Indexed;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ProgressBarRenderer;
import com.vaadin.ui.renderers.TextRenderer;
import com.vaadin.ui.themes.ValoTheme;

import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.service.UsuarioService;
import mx.com.gunix.framework.ui.vaadin.component.Header;

public class MainViewLayout extends VerticalLayout{
	@Autowired
	@Lazy
	ApplicationContext applicationContext;

	@Autowired
	private VaadinSecurity security;
	
	@Autowired
	UsuarioService usuarioSrv;

	private static final long serialVersionUID = 1;
	private TabSheet aplicacionesTab;
	private Button userIdLabel;
	private Button downloads;
	private Grid downloadsManager;
	private Map<String, DownloadProps> registeredDownloads = new HashMap<String, DownloadProps>();
	String userId;
	private VerticalLayout userDetailsLayout;
	private FormLayout cambioContraseñaLyt;
	private PasswordField nuevaContraseña;
	private PasswordField confirmacionNuevaContraseña;
	private PasswordField contraseñaActual;
	Window userDetailsWindow;
	Button cambiarContraseña;

	@PostConstruct
	private void postConstruct() {
		setSizeUndefined();
		setWidth("100.0%");
		setSpacing(false);
		setMargin(true);
		addStyleName("MainViewLayout");
		Usuario u = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		StringBuilder userIdStrBldr = new StringBuilder(u.getIdUsuario());
		if (u.getDatosUsuario() != null) {
			userIdStrBldr
				.append(" - ")
				.append(u.getDatosUsuario().getApPaterno())
				.append(" ")
				.append(u.getDatosUsuario().getApMaterno())
				.append(u.getDatosUsuario().getApMaterno() != null && !"".equals(u.getDatosUsuario().getApMaterno().trim()) ? " " : "")
				.append(u.getDatosUsuario().getNombre());
		}
		userId = userIdStrBldr.toString();

		HorizontalLayout hl = new HorizontalLayout();
		hl.addStyleName("padding-top-header");
		hl.setMargin(false);
		hl.setSpacing(true);
		hl.setWidth("100%");

		Image logoInstitucional = new Image(null, new ThemeResource("img/logo-institucional.png"));
		hl.addComponent(logoInstitucional);
		hl.setComponentAlignment(logoInstitucional, Alignment.MIDDLE_LEFT);
		hl.setExpandRatio(logoInstitucional, 0.25f);

		HorizontalLayout appInfo = new HorizontalLayout();
		appInfo.setMargin(false);
		appInfo.setSpacing(false);
		appInfo.setSizeUndefined();
		hl.addComponent(appInfo);
		hl.setComponentAlignment(appInfo, Alignment.MIDDLE_CENTER);
		hl.setExpandRatio(appInfo, 0.50f);
		
		final VerticalLayout downloadsUserIdLayt = new VerticalLayout();
		downloadsUserIdLayt.setSizeFull();
		downloadsUserIdLayt.setSpacing(false);
		downloadsUserIdLayt.setMargin(false);
		
		userIdLabel = new Button(userId);
		userIdLabel.addStyleName(ValoTheme.BUTTON_LINK);
		userIdLabel.addClickListener(clickEvnt->{
			userDetailsWindow = new Window();
			userDetailsWindow.setModal(true);
			userDetailsWindow.setResizable(false);
			userDetailsWindow.setClosable(true);
			userDetailsWindow.setWidth("200px");
			userDetailsWindow.setHeight("100px");
			userDetailsWindow.setContent(userDetailsLayout);
			userDetailsWindow.addCloseListener(closeEvnt->{
				nuevaContraseña.setValue(null);
				confirmacionNuevaContraseña.setValue(null);
				contraseñaActual.setValue(null);
				cambioContraseñaLyt.setVisible(false);
				cambiarContraseña.setVisible(true);
			});
			UI.getCurrent().addWindow(userDetailsWindow);
		});
		
		downloadsUserIdLayt.addComponent(userIdLabel);
		downloadsUserIdLayt.setComponentAlignment(userIdLabel, Alignment.MIDDLE_RIGHT);		
				
		downloads = new Button("Descargas...");
		downloads.addStyleName(ValoTheme.BUTTON_LINK);
		downloads.addStyleName("descargas-button");
		downloads.addClickListener(clickEvnt -> {
			showDownloads();
		});
		downloads.setVisible(false);
		
		downloadsUserIdLayt.addComponent(downloads);
		downloadsUserIdLayt.setComponentAlignment(downloads, Alignment.BOTTOM_RIGHT);
		
		hl.addComponent(downloadsUserIdLayt);
		hl.setComponentAlignment(downloadsUserIdLayt, Alignment.MIDDLE_RIGHT);
		hl.setExpandRatio(downloadsUserIdLayt, 0.25f);

		addComponent(hl);
		setExpandRatio(hl, 0.0f);

		renderApps(u.getAplicaciones(), appInfo);
		
		downloadsManager = new Grid();
		downloadsManager.addColumn("Archivo", String.class).setRenderer(new TextRenderer());
		downloadsManager.addColumn("Progreso", Double.class).setRenderer(new ProgressBarRenderer());
		downloadsManager.addColumn("", String.class).setRenderer(new ButtonRenderer(clickEvnt -> {
			DownloadProps downloadProps = null;
			for (String buttonID : registeredDownloads.keySet()) {
				if (clickEvnt.getItemId().equals((downloadProps = registeredDownloads.get(buttonID)).id)) {
					if (downloadProps.progreso >= 1.0) {
						Page.getCurrent().getJavaScript().execute(new StringBuilder("document.getElementById('").append(buttonID).append("').click();").toString());
					} else {
						Notification.show("El archivo sigue en proceso de construcción", Type.WARNING_MESSAGE);
					}
					break;
				}
			}
		}, "Generando Archivo..."));
		downloadsManager.setHeightMode(HeightMode.ROW);
		downloadsManager.setWidth("750px");
		downloadsManager.setHeightByRows(5.0);
		
		userDetailsLayout = new VerticalLayout();
		Button logoutButton = new Button("Cerrar Sesión");
		logoutButton.addClickListener(clckEvnt -> {
			security.logout();
		});
		userDetailsLayout.addComponent(logoutButton);
		userDetailsLayout.setExpandRatio(logoutButton, 0.0f);
		userDetailsLayout.setComponentAlignment(logoutButton, Alignment.MIDDLE_CENTER);
			
		cambiarContraseña = new Button("Cambiar Contraseña");
		cambiarContraseña.addClickListener(clcEvnt->{
			cambioContraseñaLyt.setVisible(true);
			userDetailsWindow.setWidth("500px");
			userDetailsWindow.setHeight("300px");
			userDetailsWindow.center();
			userDetailsWindow.markAsDirtyRecursive();
			cambiarContraseña.setVisible(false);
		});
		
		userDetailsLayout.addComponent(cambiarContraseña);
		userDetailsLayout.setComponentAlignment(cambiarContraseña, Alignment.MIDDLE_CENTER);
		
		cambioContraseñaLyt = new FormLayout();
		cambioContraseñaLyt.setSizeFull();
		cambioContraseñaLyt.setVisible(false);
		
		nuevaContraseña = new PasswordField("Nueva Contraseña");
		nuevaContraseña.setNullRepresentation("");
		nuevaContraseña.setRequired(true);
		cambioContraseñaLyt.addComponent(nuevaContraseña);
		confirmacionNuevaContraseña = new PasswordField("Confirmación de Nueva Contraseña");
		confirmacionNuevaContraseña.setNullRepresentation("");
		confirmacionNuevaContraseña.setRequired(true);
		cambioContraseñaLyt.addComponent(confirmacionNuevaContraseña);
		contraseñaActual = new PasswordField("Contraseña Actual");
		contraseñaActual.setNullRepresentation("");
		contraseñaActual.setRequired(true);
		cambioContraseñaLyt.addComponent(contraseñaActual);
		
		Button cambiarContraseñaButton = new Button("Aceptar");
		cambiarContraseñaButton.addClickListener(clicEvnt -> {
			if (nuevaContraseña.getValue() != null && confirmacionNuevaContraseña.getValue() != null && contraseñaActual.getValue() != null &&
					!"".equals(nuevaContraseña.getValue()) && !"".equals(confirmacionNuevaContraseña.getValue()) && !"".equals(contraseñaActual.getValue())) {
				if (nuevaContraseña.getValue().equals(confirmacionNuevaContraseña.getValue())) {
					u.setPassword(nuevaContraseña.getValue());
					try {
						usuarioSrv.updatePassword(u, contraseñaActual.getValue());
						userDetailsLayout.addComponent(new Label("Cambio de contraseña realizado con éxito, de clic en el botón Continuar para cerrar la sesión actual e ingresar nuevamente con la nueva contraseña"));
						Button continuarButton = new Button("Continuar", clickEvnt -> {
							security.logout();
						});
						userDetailsLayout.addComponent(continuarButton);
						userDetailsLayout.setComponentAlignment(continuarButton, Alignment.MIDDLE_CENTER);
						userDetailsWindow.setClosable(false);
						cambioContraseñaLyt.setVisible(false);
						userDetailsWindow.setWidth("500px");
						userDetailsWindow.setHeight("200px");
						userDetailsWindow.center();
						userDetailsWindow.markAsDirtyRecursive();
					} catch (Exception e) {
						Notification.show(ExceptionUtils.getRootCause(e).getMessage(), Type.ERROR_MESSAGE);
					}
				} else {
					Notification.show("La nueva contraseña y la confirmación deben ser iguales", Type.ERROR_MESSAGE);
				}
			} else {
				Notification.show("La nueva contraseña, la confirmación y la contraseña actual son requeridas", Type.ERROR_MESSAGE);
			}
		});
		cambioContraseñaLyt.addComponent(cambiarContraseñaButton);

		userDetailsLayout.addComponent(cambioContraseñaLyt);
		userDetailsLayout.setExpandRatio(cambioContraseñaLyt, 1.0f);
		userDetailsLayout.setComponentAlignment(cambioContraseñaLyt, Alignment.MIDDLE_CENTER);
		
		userDetailsLayout.setMargin(true);
		userDetailsLayout.setSizeFull();		
	}

	protected void renderApps(List<Aplicacion> apps, HorizontalLayout appInfo) {
		if (apps.size() > 1) {
			aplicacionesTab = new TabSheet();
			aplicacionesTab.setId("gx_aplicaciones_tabsheet");
			aplicacionesTab.setImmediate(true);

			apps.stream().forEach(aplicacion -> {
				Header h = applicationContext.getBean(Header.class);
				h.renderHeader(aplicacion);
				h.setId("gx_"+aplicacion.getIdAplicacion()+"_header");
				Tab appTab = aplicacionesTab.addTab(h, aplicacion.getDescripcion());
				appTab.setId("gx_" + aplicacion.getIdAplicacion() + "_tab");
				aplicacionesTab.addSelectedTabChangeListener(selectedTab -> {
					Header iH = (Header) selectedTab.getTabSheet().getSelectedTab();
					updateAppInfo(iH, appInfo);
					UI.getCurrent().setNavigator(iH.getNavigator());
				});
			});

			addComponent(aplicacionesTab);

			Header iH = (Header) aplicacionesTab.getSelectedTab();
			updateAppInfo(iH, appInfo);
			UI.getCurrent().setNavigator(iH.getNavigator());
			setExpandRatio(aplicacionesTab, 1.0f);
		} else {
			if (!apps.isEmpty()) {
				Header h = applicationContext.getBean(Header.class);
				Aplicacion app = apps.get(0);
				h.renderHeader(app);
				addComponent(h);
				UI.getCurrent().setNavigator(h.getNavigator());
				setExpandRatio(h, 1.0f);
				setSpacing(true);
				updateAppInfo(h, appInfo);
			}
		}
	}

	private void updateAppInfo(Header iH, HorizontalLayout appInfo) {
		if (appInfo != null) {
			Page.getCurrent().setTitle(iH.getAplicacion().getDescripcion());
			JavaScript.getCurrent().execute(" window.parent.document.title = window.parent.document.getElementById('gnxVdnIF').contentDocument.title;");
			appInfo.removeAllComponents();
			appInfo.addComponent(new Image(null, new ThemeResource("img/" + iH.getAplicacion().getIcono())));
			Label appTitle = new Label(iH.getAplicacion().getDescripcion());
			appTitle.addStyleName("app-title");
			appInfo.addComponent(appTitle);
			appInfo.setComponentAlignment(appTitle, Alignment.MIDDLE_LEFT);
		}
	}

	public void enter(ViewChangeEvent event) {
		Responsive.makeResponsive(this);
	}

	public static void registraExportacion(Button botonDescarga, String nombreArchivo, Integer size) {
		MainViewLayout mvl = (MainViewLayout) UI.getCurrent().getContent();
		mvl.addComponent(botonDescarga);
		DownloadProps downloadProps = new DownloadProps();
		downloadProps.id = mvl.downloadsManager.addRow(new Object[] { nombreArchivo, 0.0, null });
		downloadProps.size = size;
		downloadProps.downloadButton = botonDescarga;
		mvl.registeredDownloads.put(botonDescarga.getId(), downloadProps);
		mvl.downloads.setVisible(true);
		mvl.showDownloads();
	}

	@SuppressWarnings("unchecked")
	public static void actualizaProgresoExportacion(String downloadButtonId, Integer procesados) {
		MainViewLayout mvl = (MainViewLayout) UI.getCurrent().getContent();
		DownloadProps downloadProps = mvl.registeredDownloads.get(downloadButtonId);

		if (downloadProps != null) {
			Double progreso = procesados.doubleValue() / downloadProps.size.doubleValue();
			downloadProps.progreso = progreso;
			if (progreso >= 1.0 || procesados % 5000 == 0) {
				UI.getCurrent().access(() -> {
					Indexed descargas = mvl.downloadsManager.getContainerDataSource();
					if (descargas.getItem(downloadProps.id) != null) {
						if (progreso >= 1.0) {
							String fileName = (String) descargas.getItem(downloadProps.id).getItemProperty("Archivo").getValue();
							mvl.downloadsManager.getContainerDataSource().getItem(downloadProps.id).getItemProperty("").setValue("Descargar");
							mvl.downloadsManager.getContainerDataSource().getItem(downloadProps.id).getItemProperty("Progreso").setValue(progreso);
							Notification.show("Exportación de archivo " + fileName + " TERMINADA", Notification.Type.TRAY_NOTIFICATION);
					} else {
						mvl.downloadsManager.getContainerDataSource().getItem(downloadProps.id).getItemProperty("Progreso").setValue(progreso);
					}
				}
			}	);
			}
		}
	}
	
	private void showDownloads() {
		Window window = new Window("Descargas");
		window.setModal(true);
		window.setClosable(true);
		window.setResizable(false);
		window.setSizeUndefined();
		window.center();
		window.setContent(downloadsManager);
		UI.getCurrent().addWindow(window);
	}
	
	public static class DownloadProps implements Serializable{
		private static final long serialVersionUID = 1L;
		Object id;
		Integer size;
		Button downloadButton;
		Double progreso;
	}
}
