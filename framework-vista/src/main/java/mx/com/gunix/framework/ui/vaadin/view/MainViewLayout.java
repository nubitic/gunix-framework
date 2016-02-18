package mx.com.gunix.framework.ui.vaadin.view;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.ui.vaadin.component.Header;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.spring.security.VaadinSecurity;

import com.vaadin.data.Container.Indexed;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.ui.Alignment;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Window;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ProgressBarRenderer;
import com.vaadin.ui.renderers.TextRenderer;
import com.vaadin.ui.themes.ValoTheme;

public class MainViewLayout extends VerticalLayout{
	@Autowired
	@Lazy
	ApplicationContext applicationContext;

	@Autowired
	private VaadinSecurity security;

	private static final long serialVersionUID = 1;
	private TabSheet aplicacionesTab;
	private PopupView userIdLabel;
	private Button downloads;
	private Grid downloadsManager;
	private Map<String, DownloadProps> registeredDownloads = new HashMap<String, DownloadProps>();
	String userId;
	private VerticalLayout userDetailsLayout;

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
				.append("<br/>")
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
		
		userIdLabel = new PopupView(new PopupView.Content() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getMinimizedValueAsHTML() {
				return userId;
			}

			@Override
			public Component getPopupComponent() {
				return userDetailsLayout;
			}
		});
		userIdLabel.addStyleName("user-id-label");
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

		if (u.getAplicaciones().size() > 1) {
			aplicacionesTab = new TabSheet();
			aplicacionesTab.setImmediate(true);

			u.getAplicaciones().stream().forEach(aplicacion -> {
				Header h = applicationContext.getBean(Header.class);
				h.renderHeader(aplicacion);
				aplicacionesTab.addTab(h, aplicacion.getDescripcion());
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
			if (!u.getAplicaciones().isEmpty()) {
				Header h = applicationContext.getBean(Header.class);
				Aplicacion app = u.getAplicaciones().get(0);
				h.renderHeader(app);
				addComponent(h);
				UI.getCurrent().setNavigator(h.getNavigator());
				setExpandRatio(h, 1.0f);
				setSpacing(true);
				updateAppInfo(h, appInfo);
			}
		}
		
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
	}

	private void updateAppInfo(Header iH, HorizontalLayout appInfo) {
		Page.getCurrent().setTitle(iH.getAplicacion().getDescripcion());
		appInfo.removeAllComponents();
		appInfo.addComponent(new Image(null, new ThemeResource("img/" + iH.getAplicacion().getIcono())));
		Label appTitle = new Label(iH.getAplicacion().getDescripcion());
		appTitle.addStyleName("app-title");
		appInfo.addComponent(appTitle);
		appInfo.setComponentAlignment(appTitle, Alignment.MIDDLE_LEFT);
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
