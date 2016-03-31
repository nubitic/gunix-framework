package mx.com.gunix.ui.vaadin.view.adminapp.usuario;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.security.domain.Ambito;
import mx.com.gunix.framework.security.domain.Ambito.Permiso;
import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.Rol;
import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.ui.vaadin.component.GunixMTable;
import mx.com.gunix.framework.ui.vaadin.component.GunixTableFieldFactory;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;
import mx.com.gunix.framework.ui.vaadin.view.AbstractGunixView;
import mx.com.gunix.framework.ui.vaadin.view.SecuredView;

import org.josso.selfservices.password.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.data.util.converter.StringToBooleanConverter;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;


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
	private Accordion appAmbitosAcc;	
	private Panel datosPanel;
	private Panel appRolesPanel;
	private Panel appAmbitosPanel;
	private GridLayout datosGridL;
	private List<Aplicacion> aplicaciones;
	private List<Aplicacion> appSelect;

	
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
		
		if ((esConsulta || esModificacion) ) {
			if(((List<Usuario>) $("resultado")) != null){
				deepCopy( ((List<Usuario>) $("resultado")).get(0));
			}else if(esModificacion){
				deepCopy((Usuario) $("usuario"));
			}
		}
		
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

		}
		
		
		guardarButton.addClickListener(event ->{
				guardarButton.setEnabled(false);
				try {
					if(!esConsulta){
						commit();
						usuario = getBean();
					    commitUsuario();
					}
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
		
		if (esConsulta || esModificacion) {
			idUsuario.setEnabled(false);
			estatus.setValue(getEstatus());
			if (esModificacion) {
				cancelarButton.setEnabled(true);
				cancelarButton.setVisible(true);
				cancelarButton.setDisableOnClick(true);
				estatus.setEnabled(true);
			}else{
				datosPanel.setEnabled(false);
				//appRolesPanel.setEnabled(false);
				estatus.setEnabled(false);
			}
		}else{
			estatus.setValue(1);
			estatus.setEnabled(false);
		}
		
		guardarButton.setEnabled(true);
		guardarButton.setDisableOnClick(true);

		
	}

	@Override
	protected List<Variable<?>> getVariablesTarea() {
		List<Variable<?>> vars = new ArrayList<Variable<?>>();
		Variable<Usuario> usuarioVar = new Variable<Usuario>();
		
		if(esConsulta||esModificacion){
			Variable<ArrayList<Usuario>> resultadoVar = new Variable<ArrayList<Usuario>>();
			resultadoVar.setNombre("resultado");
			resultadoVar.setValor(null);
			vars.add(resultadoVar);
			
			if (esModificacion) {
				Variable<String> accionVar = new Variable<String>();
				accionVar.setNombre("acción");
				accionVar.setValor(cancelar ? "Cancelar" : "Guardar");
				vars.add(accionVar);
			}
		}
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
		estatus.setItemCaption(0, "Eliminado");
		estatus.addItem(1);
		estatus.setItemCaption(1,"Activo");
		estatus.addItem(2);
		estatus.setItemCaption(2,"Bloqueado");
		estatus.setWidth("-1px");
		estatus.setHeight("-1px");
		flyt.addComponent(estatus);
		
		// datos personales
		datosPanel = buildDatosPanel();
		flyt.addComponent(datosPanel);
		
		appRolesPanel = buildAppRolesPanel();
		flyt.addComponent(appRolesPanel);
		
		appAmbitosPanel = buildAppAmbitosPanel();
		flyt.addComponent(appAmbitosPanel);
	}
	
	
	private Panel buildAppAmbitosPanel() {
		appAmbitosPanel = new Panel("Ambito Aplicativo");
		appAmbitosPanel.setIcon(new ThemeResource("img/1459221873_control_panel_access.png"));
		appAmbitosPanel.setImmediate(false);
		appAmbitosPanel.setSizeFull();
		appAmbitosPanel.setHeight("-1px");

		VerticalLayout vl = new VerticalLayout();
		vl.setMargin(true);
		appAmbitosAcc = new Accordion();
		appAmbitosAcc.setImmediate(true);
		appAmbitosAcc.setSizeFull();
		appAmbitosAcc.setHeight("-1px");

		for (Aplicacion app : usuario.getAplicaciones()) {
			if (app.getAmbito() != null && !app.getAmbito().isEmpty()) {
				appAmbitosAcc.addTab(buildAmbitosPanel(app), app.getDescripcion(), null);
				appAmbitosAcc.setId("ambitos_" + app.getIdAplicacion());
			}
		}
		
		vl.addComponent(appAmbitosAcc);
		appAmbitosPanel.setContent(vl);

		return appAmbitosPanel;
	}
	
	private Component buildAmbitosPanel(Aplicacion app) {
		Panel ambitosPanel = new Panel();
		ambitosPanel.setImmediate(false);
		ambitosPanel.setSizeFull();
		ambitosPanel.setHeight("-1px");
		VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);

		ComboBox ambitos = new ComboBox("Ambito");
		ambitos.setImmediate(true);
		
		if (app.getAmbito() != null) {
			app.getAmbito().forEach(ambito -> {
				ambitos.addItem(ambito.getClase());
				ambitos.setItemCaption(ambito.getClase(), ambito.getDescripcion());
			});
		}
		
		GunixMTable<Permiso> permisos = new GunixMTable<Permiso>(Permiso.class);		
		permisos.setTableFieldFactory(new GunixTableFieldFactory());
		permisos.setVisibleColumns("aclType", "lectura", "modificacion", "eliminacion");
		permisos.setColumnHeaders(new String[] { "Elemento", "¿Puede ver?", "¿Puede modificar?", "¿Puede eliminar?" });
		permisos.addGeneratedColumn("aclType", (source, itemId, columnId) -> {
			return new StringBuilder(((Permiso) itemId).getAclType().getClaveNegocio()).append(" - ").append(((Permiso) itemId).getAclType().getDescripcion()).toString();
		});
		
		permisos.setConverter("lectura", new StringToBooleanConverter("Si", "No"));
		permisos.setConverter("modificacion", new StringToBooleanConverter("Si", "No"));
		permisos.setConverter("eliminacion", new StringToBooleanConverter("Si", "No"));
		permisos.setEditable(!esConsulta);
		permisos.setImmediate(false);
		permisos.setHeight("255px");
		permisos.setWidth("100%");
		permisos.setVisible(false);
		
		ambitos.addValueChangeListener(vlChnEvnt -> {
			for (Ambito ambito : app.getAmbito()) {
				if (ambito.getClase().equals(ambitos.getValue())) {
					ambito.getPermisos().forEach(permiso -> {
						permisos.getContainerDataSource().addItem(permiso);
					});
					break;
				}
			}
			permisos.setVisible(true);
		});
		
		layout.addComponent(ambitos);
		layout.addComponent(permisos);
		ambitosPanel.setContent(layout);

		return ambitosPanel;
	}

	@SuppressWarnings({ "unchecked" })
	private Panel buildAppRolesPanel(){
		List<Rol> rolSelect =null;
		appRolesPanel = new Panel("Roles");
		appRolesPanel.setIcon(new ThemeResource("img/1440815816_To_do_list.png"));
		appRolesPanel.setImmediate(false);
		appRolesPanel.setSizeFull();
		appRolesPanel.setHeight("-1px");
		
		VerticalLayout vl = new VerticalLayout();
		vl.setMargin(true);
		appRolesAcc = new Accordion();
		appRolesAcc.setImmediate(true);
		appRolesAcc.setSizeFull();
		appRolesAcc.setHeight("-1px");
		
		aplicaciones = (List<Aplicacion>) $("aplicaciones");
		if (esConsulta || esModificacion) {
			appSelect = usuario.getAplicaciones();
		}
		
		for(Aplicacion app : aplicaciones){
			if(appSelect!=null){
				rolSelect = buscaApp(app.getIdAplicacion());
			} 
			appRolesAcc.addTab(buildRolesPanel(app,rolSelect), app.getDescripcion(), null);
			appRolesAcc.setId(app.getIdAplicacion());
		}
		vl.addComponent(appRolesAcc);
		appRolesPanel.setContent(vl);
		
		return appRolesPanel;
	}

	private Panel buildRolesPanel(Aplicacion app,List<Rol> rolSelect){
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
			if(rolSelect != null){
				if(buscaRol(rolSelect,rol.getIdRol())){
					roles.select(rol.getIdRol());
				}
			}


		});
		
		if(esConsulta){
			rolesPanel.setEnabled(false);
		}
				
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
		usuario.setActivo(false);
		usuario.setEliminado(false);
		usuario.setBloqueado(false);
		if(!esModificacion){
			usuario.setPassword(pg.generateClearPassword());
			usuario.setActivo(true);
		}else{
			if(estatus.getValue().equals(1)){
				usuario.setActivo(true);
			}else if(estatus.getValue().equals(2)){
				usuario.setBloqueado(true);
			}else {
				usuario.setEliminado(true);	
			}			
		}	
		usuario.setAplicaciones(getAplicacionesRolesAmbitos());
	
	}
	
	private List<Aplicacion> getAplicacionesRolesAmbitos(){
		Iterator<Component> i = appRolesAcc.iterator();
		List<Aplicacion> appSelect = new ArrayList<Aplicacion>();
		while (i.hasNext()) {
		    Panel panel = (Panel) i.next();
		    OptionGroup roles = (OptionGroup)panel.getContent();
		    			
		    if(roles != null){
				@SuppressWarnings("unchecked")
				Collection<String> rSelect = (Collection<String>) roles.getValue();
		    	
			    if(rSelect!=null && !rSelect.isEmpty()){
			    	Aplicacion app = new Aplicacion();
			    	List<Rol> rolesSelec = new ArrayList<Rol>();
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
		
		appSelect.forEach(app->{
			usuario.getAplicaciones()
				.stream()
				.filter(aplicacion -> (aplicacion.getIdAplicacion().equals(app.getIdAplicacion())))
				.findFirst()
				.ifPresent(aplicacion->{
					app.setAmbito(aplicacion.getAmbito());
				});
		});
		return appSelect;

	}
	
	private void deepCopy(Usuario usuarioVar) {
		usuario =  getBean();
		usuario.setIdUsuario(usuarioVar.getIdUsuario());
		if(usuarioVar.getDatosUsuario()!=null){
			usuario.setDatosUsuario(usuarioVar.getDatosUsuario());
		}	
		usuario.setPassword(usuarioVar.getPassword());
		usuario.setActivo(usuarioVar.isActivo());
		usuario.setBloqueado(usuarioVar.isBloqueado());
		usuario.setEliminado(usuarioVar.isEliminado());
		usuario.setAplicaciones(usuarioVar.getAplicaciones());
	}
	
	private List<Rol> buscaApp(String idAplicacion){
		for(Aplicacion app:appSelect){
			if(app.getIdAplicacion().equals(idAplicacion)){
				return app.getRoles();
			}
		}		
		return null;
	}
	
	private Boolean buscaRol(List<Rol> rolSelect,String idRol){
		for(Rol rol:rolSelect){
			if(rol.getIdRol().equals(idRol)){
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}
	
	private Object getEstatus(){
		if(usuario.isActivo()){
			return 1;
		}else if(usuario.isBloqueado()){
			return 2;
		}else {
			return 0;	
		}
	}

}
