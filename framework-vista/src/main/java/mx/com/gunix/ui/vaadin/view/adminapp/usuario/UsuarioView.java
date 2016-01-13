package mx.com.gunix.ui.vaadin.view.adminapp.usuario;

import java.util.List;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.ui.vaadin.component.GunixUploadField;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;
import mx.com.gunix.framework.ui.vaadin.view.AbstractGunixView;
import mx.com.gunix.framework.ui.vaadin.view.SecuredView;
import mx.com.gunix.ui.vaadin.view.adminapp.aplicacion.AplicacionView.AplicacionViewBean;


@GunixVaadinView
public class UsuarioView extends AbstractGunixView<UsuarioView.UsuarioViewBean> implements SecuredView {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TextField idUsuario;
	private ComboBox estatus;
	private Button guardarButton;
	private Accordion datosRolesAcc;
	private Accordion appRolesAcc;
	private Panel datosPanel;
	private Panel rolesPanel;
	private GridLayout datosGridL;
	private List<Aplicacion> aplicaciones;

	
	//datos personales
	private TextField curp;
	private TextField rfc;
	private TextField nombre;
	private TextField apPaterno;
	private TextField apMaterno;
	private TextField correoE;
	private TextField telefono;

	public static class UsuarioViewBean extends Usuario {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
	}
	
	
	@Override
	protected void doEnter(ViewChangeEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doConstruct() {
		//esConsulta = "Consulta".equals($("operación"));
		//esModificacion = "Modificación".equals($("operación"));
		
		flyt.setIcon(new ThemeResource("img/1440816106_window.png"));
		flyt.setCaption(new StringBuilder($("operación").toString()).append(" de Usuarios").toString());
		
		buildMainLayout();
		datosRolesAcc.getTab(0).setIcon(new ThemeResource("img/1452591654_marty-mcfly.png"));
		datosRolesAcc.getTab(1).setIcon(new ThemeResource("img/1440815816_To_do_list.png"));
		
	}

	@Override
	protected List<Variable<?>> getVariablesTarea() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getComentarioTarea() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void buildMainLayout() {
		flyt.setImmediate(false);
		flyt.setWidth("100%");
		flyt.setHeight("100%");
		flyt.setMargin(false);
		flyt.setSpacing(true);

		// usuario
		idUsuario = new TextField();
		idUsuario.setCaption("Id Usuario");
		idUsuario.setImmediate(false);
		idUsuario.setNullRepresentation("");
		idUsuario.setInvalidCommitted(false);
		idUsuario.setWidth("-1px");
		idUsuario.setHeight("-1px");
		flyt.addComponent(idUsuario);
		
		estatus = new ComboBox();
		estatus.setCaption("Estatus");
		
		estatus.addItem(1);
		estatus.setItemCaption(1,"Activo");
		estatus.addItem(0);
		estatus.setItemCaption(0,"Inactivo");

		estatus.select(1);
		estatus.setEnabled(false);
		estatus.setWidth("-1px");
		estatus.setHeight("-1px");
		flyt.addComponent(estatus);
		
		// datosRolesAcc
		datosRolesAcc = buildDatosRolesAcc();
		flyt.addComponent(datosRolesAcc);

		// guardarButton
		guardarButton = new Button();
		guardarButton.setImmediate(true);
		guardarButton.setWidth("-1px");
		guardarButton.setHeight("-1px");
		flyt.addComponent(guardarButton);
		
		guardarButton.setCaption("Guardar");
	}
	
	private Accordion buildDatosRolesAcc() {
		// common part: create layout
		datosRolesAcc = new Accordion();
		datosRolesAcc.setImmediate(true);
		datosRolesAcc.setSizeFull();
		datosRolesAcc.setHeight("-1px");
		// datosPanel
		datosPanel = buildDatosPanel();
		datosRolesAcc.addTab(datosPanel, "Datos Personales", null);

		// rolesPanel
		rolesPanel = buildRolesPanel();
		datosRolesAcc.addTab(rolesPanel, "Roles", null);

		return datosRolesAcc;
	}
	
	@SuppressWarnings("unchecked")
	private Accordion buildAppRolesAcc(){
		appRolesAcc = new Accordion();
		appRolesAcc.setImmediate(true);
		appRolesAcc.setSizeFull();
		appRolesAcc.setHeight("-1px");
		
		aplicaciones = (List<Aplicacion>) $("aplicaciones");
		
		for(Aplicacion app : aplicaciones){
			appRolesAcc.addTab(new Panel(), app.getDescripcion(), null);
		}
		return appRolesAcc;
	}
	private Panel buildDatosPanel() {
		// common part: create layout
		datosPanel = new Panel();
		datosPanel.setImmediate(false);
		datosPanel.setSizeFull();
		datosPanel.setHeight("-1px");
		//datosGridL
		datosGridL = buildDatosGridL();
		datosPanel.setContent(datosGridL);

		return datosPanel;
	}
	
	private GridLayout buildDatosGridL() {
		datosGridL = new GridLayout(3,3);
		datosGridL.setImmediate(false);
		datosGridL.setSizeFull();
		datosGridL.setMargin(true);
		datosGridL.setSpacing(true);
		
		//datos personales
		curp = new TextField();
		curp.setCaption("CURP");
		curp.setImmediate(false);
		curp.setNullRepresentation("");
		curp.setInvalidCommitted(false);
		curp.setWidth("-1px");
		curp.setHeight("-1px");
		
		rfc = new TextField();
		rfc.setCaption("RFC");
		rfc.setImmediate(false);
		rfc.setNullRepresentation("");
		rfc.setInvalidCommitted(false);
		rfc.setWidth("-1px");
		rfc.setHeight("-1px");
		
		nombre = new TextField();
		nombre.setCaption("NOMBRE(S)");
		nombre.setImmediate(false);
		nombre.setNullRepresentation("");
		nombre.setInvalidCommitted(false);
		nombre.setWidth("-1px");
		nombre.setHeight("-1px");
		
		apPaterno = new TextField();
		apPaterno.setCaption("APELLIDO PATERNO");
		apPaterno.setImmediate(false);
		apPaterno.setNullRepresentation("");
		apPaterno.setInvalidCommitted(false);
		apPaterno.setWidth("-1px");
		apPaterno.setHeight("-1px");
		
		apMaterno = new TextField();
		apMaterno.setCaption("APELLIDO MATERNO");
		apMaterno.setImmediate(false);
		apMaterno.setNullRepresentation("");
		apMaterno.setInvalidCommitted(false);
		apMaterno.setWidth("-1px");
		apMaterno.setHeight("-1px");
		
		correoE = new TextField();
		correoE.setCaption("CORREO ELECTRÓNICO");
		correoE.setImmediate(false);
		correoE.setNullRepresentation("");
		correoE.setInvalidCommitted(false);
		correoE.setWidth("-1px");
		correoE.setHeight("-1px");
		
		telefono = new TextField();
		telefono.setCaption("TELEFONO");
		telefono.setImmediate(false);
		telefono.setNullRepresentation("");
		telefono.setInvalidCommitted(false);
		telefono.setWidth("-1px");
		telefono.setHeight("-1px");

		datosGridL.addComponent(curp);
		datosGridL.addComponent(rfc);
		datosGridL.addComponent(nombre,0,1);
		datosGridL.addComponent(apPaterno,1,1);
		datosGridL.addComponent(apMaterno,2,1);
		datosGridL.addComponent(correoE,0,2);
		datosGridL.addComponent(telefono,1,2);
		
		return datosGridL;
	}

	private Panel buildRolesPanel() {
		rolesPanel = new Panel();
		rolesPanel.setImmediate(false);
		rolesPanel.setSizeFull();
		rolesPanel.setHeight("-1px");

		appRolesAcc = buildAppRolesAcc();
		rolesPanel.setContent(appRolesAcc);

		return rolesPanel;
	}

}
