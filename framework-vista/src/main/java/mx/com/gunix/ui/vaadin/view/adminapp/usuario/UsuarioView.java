package mx.com.gunix.ui.vaadin.view.adminapp.usuario;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;





import org.josso.selfservices.password.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Notification.Type;

import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.DatosUsuario;
import mx.com.gunix.framework.security.domain.Rol;
import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;
import mx.com.gunix.framework.ui.vaadin.view.AbstractGunixView;
import mx.com.gunix.framework.ui.vaadin.view.SecuredView;


@GunixVaadinView
public class UsuarioView extends AbstractGunixView<Usuario> implements SecuredView {

	@Autowired
	private PasswordGenerator pg;
	
	private static final long serialVersionUID = 1L;
	
	private TextField idUsuario;
	private ComboBox estatus;
	private Button guardarButton;
	private Button cancelarButton;
	private Accordion appRolesAcc;
	private Panel datosPanel;
	private Panel appRolesPanel;
	private GridLayout datosGridL;
	private List<Aplicacion> aplicaciones;

	
	//datos personales
	@PropertyId("datosUsuario.curp")
	private TextField curp;
	@PropertyId("datosUsuario.rfc")
	private TextField rfc;
	@PropertyId("datosUsuario.nombre")
	private TextField nombre;
	@PropertyId("datosUsuario.apPaterno")
	private TextField apPaterno;
	@PropertyId("datosUsuario.apMaterno")
	private TextField apMaterno;
	@PropertyId("datosUsuario.correoElectronico")
	private TextField correoElectronico;
	@PropertyId("datosUsuario.telefono")
	private TextField telefono;
	
	private Usuario usuario;
	
	private Boolean esConsulta;
	private Boolean esModificacion;
	private Boolean cancelar = Boolean.FALSE;

	@SuppressWarnings("unchecked")
	@Override
	protected void doConstruct() {	
		esConsulta = "Consulta".equals($("operación"));
		esModificacion = "Modificación".equals($("operación"));
		
		flyt.setIcon(new ThemeResource("img/1440816106_window.png"));
		flyt.setCaption(new StringBuilder($("operación").toString()).append(" de Usuarios").toString());		
		buildMainLayout();
		
		// guardarButton
		guardarButton = new Button();
		guardarButton.setImmediate(true);
		guardarButton.setWidth("-1px");
		guardarButton.setHeight("-1px");
		guardarButton.setCaption(esConsulta == true?"Regresar":"Guardar");
		guardarButton.setDisableOnClick(true);		
		flyt.addComponent(guardarButton);
		
		if (esModificacion) {
			cancelarButton = new Button();
			cancelarButton.setImmediate(true);
			cancelarButton.setWidth("-1px");
			cancelarButton.setHeight("-1px");
			cancelarButton.setCaption("Cancelar");
			
			cancelarButton.addClickListener(evnt->{
				cancelar=Boolean.TRUE;
				completaTarea();
			});
			
			flyt.addComponent(cancelarButton);
			
			if (esConsulta || esModificacion) {
				deepCopy(((List<Usuario>) $("usuario")).get(0), usuario);
			}
		}
		
		guardarButton.addClickListener(event ->{
				guardarButton.setEnabled(false);
				try {
					commit();
					usuario = getBean();
				    commitUsuario();
				    completaTarea();
				} catch (CommitException ce) {			
					appendNotification(Type.ERROR_MESSAGE, "Existen errores en el formulario");
					guardarButton.setEnabled(true);
					guardarButton.setDisableOnClick(true);				
					ce.printStackTrace();
				}
			
		});
				
		setSizeFull();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	protected void doEnter(ViewChangeEvent event) {
		List<String> errores = (List<String>) $("errores");
		if (errores != null && !errores.isEmpty()) {
			Notification.show("Existen errores en el formulario: " + errores, Type.ERROR_MESSAGE);
		}
		guardarButton.setEnabled(true);
		guardarButton.setDisableOnClick(true);

		if (cancelarButton != null) {
			cancelarButton.setEnabled(false);
			cancelarButton.setVisible(false);
		}
		
		if (esConsulta) {
		 flyt.setReadOnly(true);
		}
		
	}

	@Override
	protected List<Variable<?>> getVariablesTarea() {
		List<Variable<?>> vars = new ArrayList<Variable<?>>();
		Variable<Usuario> usuarioVar = new Variable<Usuario>();
		
		usuarioVar.setValor(usuario);
		usuarioVar.setNombre("usuario");
		vars.add(usuarioVar);
		return vars;
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
		
		//estatus
		estatus = new ComboBox();
		estatus.setNullSelectionAllowed(false);
		estatus.setCaption("Estatus");	
		estatus.addItem(0);
		estatus.setItemCaption(0, "Inactivo");
		estatus.addItem(1);
		estatus.setItemCaption(1,"Activo");
		estatus.setWidth("-1px");
		estatus.setHeight("-1px");
		estatus.setValue(1);
		estatus.setEnabled(false);
		flyt.addComponent(estatus);
		
		// datos personales
		datosPanel = buildDatosPanel();
		flyt.addComponent(datosPanel);
		
		appRolesPanel = buildAppRolesPanel();
		flyt.addComponent(appRolesPanel);
	}
	
	
	@SuppressWarnings({ "unchecked" })
	private Panel buildAppRolesPanel(){
		appRolesPanel = new Panel("Roles");
		appRolesPanel.setIcon(new ThemeResource("img/1440815816_To_do_list.png"));
		appRolesPanel.setImmediate(false);
		appRolesPanel.setSizeFull();
		appRolesPanel.setHeight("-1px");
		
		appRolesAcc = new Accordion();
		appRolesAcc.setImmediate(true);
		appRolesAcc.setSizeFull();
		appRolesAcc.setHeight("-1px");
		
		aplicaciones = (List<Aplicacion>) $("aplicaciones");
		
		for(Aplicacion app : aplicaciones){
			appRolesAcc.addTab(buildRolesPanel(app), app.getDescripcion(), null);
			appRolesAcc.setId(app.getIdAplicacion());
		}
		
		appRolesPanel.setContent(appRolesAcc);
		
		return appRolesPanel;
	}

	private Panel buildRolesPanel(Aplicacion app){
		Panel rolesPanel = new Panel();
		rolesPanel.setImmediate(false);
		rolesPanel.setSizeFull();
		rolesPanel.setHeight("-1px");

		OptionGroup roles = new OptionGroup(app.getIdAplicacion());
		roles.addStyleName("horizontal");
		roles.setMultiSelect(true);
		
		app.getRoles().forEach(rol -> {
			roles.addItem(rol.getIdRol());
			roles.setItemCaption(rol.getIdRol(), rol.getDescripcion());
		});
				
		rolesPanel.setContent(roles);
		return rolesPanel;
	}
	
	private Panel buildDatosPanel() {
		// common part: create layout
		datosPanel = new Panel("Datos Personales");
		datosPanel.setIcon(new ThemeResource("img/1452591654_marty-mcfly.png"));
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
		
		correoElectronico = new TextField();
		correoElectronico.setCaption("CORREO ELECTRÓNICO");
		correoElectronico.setImmediate(false);
		correoElectronico.setNullRepresentation("");
		correoElectronico.setInvalidCommitted(false);
		correoElectronico.setWidth("-1px");
		correoElectronico.setHeight("-1px");
		
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
		datosGridL.addComponent(correoElectronico,0,2);
		datosGridL.addComponent(telefono,1,2);
		
		return datosGridL;
	}
	

	private void commitUsuario() {
		usuario.setPassword(pg.generateClearPassword());
		usuario.setActivo(true);		
		usuario.setAplicaciones(getAplicacionesRoles());
	
	}
	
	private List<Aplicacion> getAplicacionesRoles(){
		Iterator<Component> i = appRolesAcc.iterator();
		List<Aplicacion> appSelect = new ArrayList<Aplicacion>();
		List<Rol> rolesSelec = new ArrayList<Rol>();
		while (i.hasNext()) {
		    Panel panel = (Panel) i.next();
		    OptionGroup roles = (OptionGroup)panel.getContent();
		    			
		    if(roles != null){
				@SuppressWarnings("unchecked")
				Collection<String> rSelect = (Collection<String>) roles.getValue();
		    	
			    if(rSelect!=null && !rSelect.isEmpty()){
			    	Aplicacion app = new Aplicacion();
			    	app.setIdAplicacion(roles.getCaption());	
	  
	                for (String r : rSelect) {
	                	Rol rolSelect = new Rol();
	                	rolSelect.setIdRol(r);
	                	rolSelect.setDescripcion(roles.getItemCaption(r));
	                	rolesSelec.add(rolSelect);
	                }
	                app.setRoles(rolesSelec);	    	
			    	appSelect.add(app);
			    }
		    }
		}
		
		return appSelect;

	}
	
	private Usuario deepCopy(Usuario usuario, Usuario usuarioBean) {
		usuarioBean.setIdUsuario(usuario.getIdUsuario());
		usuarioBean.setDatosUsuario(usuario.getDatosUsuario());
		//usuarioBean.setPassword(usuario.getPassword());
		usuarioBean.setActivo(usuario.isActivo());
		usuarioBean.setBloqueado(usuario.isBloqueado());
		usuarioBean.setEliminado(usuario.isEliminado());
		usuarioBean.setAplicaciones(usuario.getAplicaciones());
		return usuarioBean;
	}

}
