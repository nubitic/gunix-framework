package mx.com.gunix.ui.vaadin.view.adminapp.aplicacion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.security.domain.Ambito;
import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.Funcion;
import mx.com.gunix.framework.security.domain.Modulo;
import mx.com.gunix.framework.security.domain.Parametro;
import mx.com.gunix.framework.security.domain.Rol;
import mx.com.gunix.framework.ui.vaadin.component.GunixMTable;
import mx.com.gunix.framework.ui.vaadin.component.GunixTableFieldFactory;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;
import mx.com.gunix.framework.ui.vaadin.view.AbstractGunixView;
import mx.com.gunix.framework.ui.vaadin.view.SecuredView;
import mx.com.gunix.ui.vaadin.view.adminapp.aplicacion.components.FuncionesTab;
import mx.com.gunix.ui.vaadin.view.adminapp.aplicacion.components.ModuloForm;
import mx.com.gunix.ui.vaadin.view.adminapp.aplicacion.components.RolForm;

import org.vaadin.viritin.ListContainer;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.UserError;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@GunixVaadinView
public class AplicacionView extends AbstractGunixView<Aplicacion> implements SecuredView {
	private static final long serialVersionUID = 1L;
	
	private Button guardarButton;
	private Button cancelarButton;
	private HorizontalLayout botonera;
	private Accordion modulosRolesAmbitoAcc;
	private Panel rolesPanel;
	private VerticalLayout verticalLayout_3;
	private TabSheet rolesTabS;
	private Panel modulosPanel;
	private VerticalLayout verticalLayout_2;
	private TabSheet modulosTabS;
	private Panel ambitoPanel;
	private VerticalLayout verticalLayout_4;
	private GunixMTable<Ambito> ambitosTable;
	private TextField icono;
	private TextField descripcion;
	private TextField idAplicacion;
	private Tab modulosAddTab;
	private Tab rolesAddTab;
	private Aplicacion aplicacion;
	private Boolean cancelar = Boolean.FALSE;

	private Boolean esConsulta;
	private Boolean esModificacion;

	private Ambito ultimoAmbitoAgregadoAMano;

	@SuppressWarnings("unchecked")
	@Override
	protected void doEnter(ViewChangeEvent event) {
		List<String> errores = (List<String>) $("errores");
		if (errores != null && !errores.isEmpty()) {
			appendNotification(Type.ERROR_MESSAGE, "Existen errores en el formulario: " + errores);
		}
		guardarButton.setEnabled(true);
		guardarButton.setDisableOnClick(true);

		if (cancelarButton != null) {
			cancelarButton.setEnabled(false);
			cancelarButton.setVisible(false);
		}

		if (esConsulta || esModificacion) {
			aplicacion.getModulos().forEach(modulo -> {
				addModuloTab(modulo);
			});
			aplicacion.getRoles().forEach(rol -> {
				addRolTab(rol);
			});
			if (aplicacion.getAmbito() != null) {
				aplicacion.getAmbito().forEach(ambito -> {
					ambitosTable.removeAllItems();
					((ListContainer<Ambito>) ambitosTable.getContainerDataSource()).addAll(aplicacion.getAmbito());
				});
			}

			idAplicacion.setReadOnly(true);

			if (esModificacion) {
				cancelarButton.setEnabled(true);
				cancelarButton.setVisible(true);
				cancelarButton.setDisableOnClick(true);
			} else {
				descripcion.setReadOnly(true);
				icono.setReadOnly(true);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doConstruct() {
		esConsulta = "Consulta".equals($("operación"));
		esModificacion = "Modificación".equals($("operación"));

		flyt.setIcon(new ThemeResource("img/1440816106_window.png"));
		flyt.setCaption(new StringBuilder($("operación").toString()).append(" de Aplicaciones").toString());

		buildMainLayout();
		modulosRolesAmbitoAcc.getTab(0).setIcon(new ThemeResource("img/1440815258_blockdevice.png"));
		modulosRolesAmbitoAcc.getTab(1).setIcon(new ThemeResource("img/1440815816_To_do_list.png"));
		modulosRolesAmbitoAcc.getTab(2).setIcon(new ThemeResource("img/1459221873_control_panel_access.png"));
		
		if (!esConsulta) {
			modulosAddTab = modulosTabS.addTab(new Panel());
			modulosAddTab.setCaption("+");
			modulosAddTab.setClosable(false);
			modulosTabS.addFocusListener(event -> {
				doModulosAddTab();
			});
			rolesAddTab = rolesTabS.addTab(new Panel());
			rolesAddTab.setCaption("+");
			rolesAddTab.setClosable(false);
			rolesTabS.addFocusListener(event -> {
				doRolesAddTab();
			});
			modulosRolesAmbitoAcc.addSelectedTabChangeListener(event -> {
				if (modulosRolesAmbitoAcc.getSelectedTab() == rolesPanel) {
					if (commitModulos()) {
						updateRoles();
					}
				} else {
					commitRoles();
				}
			});
			guardarButton.setCaption("Guardar");
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
				botonera.addComponent(cancelarButton);
				botonera.setComponentAlignment(cancelarButton, Alignment.MIDDLE_RIGHT);
				botonera.setExpandRatio(cancelarButton, 0.0f);
			}
		} else {
			guardarButton.setCaption("Regresar");
		}

		guardarButton.setDisableOnClick(true);
		guardarButton.addClickListener(event -> {
			try {
				if (!esConsulta) {
					commitModulos();
					commitRoles();
					updateRoles();
					commitAmbito();
					modulosRolesAmbitoAcc.getTab(0).setComponentError(null);
					modulosRolesAmbitoAcc.getTab(1).setComponentError(null);
					modulosRolesAmbitoAcc.getTab(2).setComponentError(null);
					modulosRolesAmbitoAcc.setComponentError(null);
					commit(cv -> {
						UserError ue = new UserError(cv.getMessage());
						if ("modulos".equals(cv.getPropertyPath().toString())) {
							modulosRolesAmbitoAcc.getTab(0).setComponentError(ue);
							modulosRolesAmbitoAcc.setComponentError(ue);
						} else {
							if ("roles".equals(cv.getPropertyPath().toString()) || cv.getPropertyPath().toString().matches("roles\\[\\d+\\]\\.modulos")) {
								modulosRolesAmbitoAcc.getTab(1).setComponentError(ue);
								modulosRolesAmbitoAcc.setComponentError(ue);
							}else{
								if ("ambitos".equals(cv.getPropertyPath().toString())) {
									modulosRolesAmbitoAcc.getTab(2).setComponentError(ue);
									modulosRolesAmbitoAcc.setComponentError(ue);
								}	
							}
						}
					});
				}
				completaTarea();
			} catch (CommitException ce) {
				appendNotification(Type.ERROR_MESSAGE, "Existen errores en el formulario");
				guardarButton.setEnabled(true);
				guardarButton.setDisableOnClick(true);
			}
		});

		aplicacion = getBean();
		if (esConsulta || esModificacion) {
			deepCopy(((List<Aplicacion>) $("resultado")).get(0), aplicacion);
		}
		setSizeFull();
	}

	@SuppressWarnings("unchecked")
	private void commitAmbito() {
		if (ambitosTable != null) {
			aplicacion.setAmbito((List<Ambito>) ambitosTable.getItemIds());
		}
	}

	private void updateRoles() {
		Iterator<Component> rolesTabsIt = rolesTabS.iterator();
		while (rolesTabsIt.hasNext()) {
			Component comp = rolesTabsIt.next();
			if (comp instanceof FuncionesTab) {
				FuncionesTab fnesTabComp = (FuncionesTab) comp;
				Rol rol = getRol(fnesTabComp);
				fnesTabComp.removeAllItems();
				for (Modulo m : aplicacion.getModulos()) {
					fnesTabComp.addOrUpdateFuncionesModulo(m, rol != null && rol.getModulos() != null ? getModulo(m, rol) : null);
				}
			}
		}
	}

	private Modulo getModulo(Modulo m, Rol rol) {
		Modulo mRol = null;
		for (Modulo mS : rol.getModulos()) {
			if (mS.equals(m)) {
				mRol = mS;
				break;
			}
		}
		return mRol;
	}

	private void commitRoles() {
		Iterator<Component> rolesTabsIt = rolesTabS.iterator();
		while (rolesTabsIt.hasNext()) {
			Component comp = rolesTabsIt.next();
			if (comp instanceof FuncionesTab) {
				FuncionesTab fnesTabComp = (FuncionesTab) comp;
				Rol selRol = getRol(fnesTabComp);
				selRol.setModulos(fnesTabComp.getFuncionesSeleccionadas());
			}
		}
	}

	private Rol getRol(FuncionesTab fnesTabComp) {
		Rol rolRet = null;
		String idRol = rolesTabS.getTab(fnesTabComp).getCaption();
		for (Rol rol : aplicacion.getRoles()) {
			if (rol.getIdRol().equals(idRol)) {
				rolRet = rol;
				break;
			}
		}
		return rolRet;
	}

	private boolean commitModulos() {
		boolean sinError = true;
		Iterator<Component> fnesTabIt = modulosTabS.iterator();
		while (fnesTabIt.hasNext()) {
			Component comp = fnesTabIt.next();
			if (comp instanceof FuncionesTab) {
				FuncionesTab fnesTabComp = (FuncionesTab) comp;
				try {
					fnesTabComp.commit();
				} catch (CommitException e) {
					sinError = false;
					modulosRolesAmbitoAcc.setSelectedTab(modulosPanel);
					modulosTabS.setSelectedTab(fnesTabComp);
				}
			}
		}
		return sinError;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected List<Variable<?>> getVariablesTarea() {
		List<Variable<?>> vars = new ArrayList<Variable<?>>();

		if (esModificacion && !cancelar) {
			Variable<Aplicacion> aplicacionActualizadaVar = new Variable<Aplicacion>();
			aplicacionActualizadaVar.setValor(esConsulta ? null : deepCopy(aplicacion, new Aplicacion()));
			aplicacionActualizadaVar.setNombre("aplicacionActualizada");
			vars.add(aplicacionActualizadaVar);

			Variable<Aplicacion> aplicacionVar = new Variable<Aplicacion>();
			aplicacionVar.setValor(((List<Aplicacion>) $("resultado")).get(0));
			aplicacionVar.setNombre("aplicacion");
			vars.add(aplicacionVar);
		} else {
			Variable<Aplicacion> aplicacionVar = new Variable<Aplicacion>();
			aplicacionVar.setNombre("aplicacion");
			if (esConsulta || (esModificacion && cancelar)) {
				aplicacionVar.setValor(null);
			} else {
				aplicacionVar.setValor(deepCopy(aplicacion, new Aplicacion()));
			}
			vars.add(aplicacionVar);
		}

		if (esConsulta || esModificacion) {
			if (esConsulta || (esModificacion && cancelar)) {
				Variable<ArrayList<Aplicacion>> resultadoVar = new Variable<ArrayList<Aplicacion>>();
				resultadoVar.setValor(null);
				resultadoVar.setNombre("resultado");
				vars.add(resultadoVar);
			}
			if (esModificacion) {
				Variable<String> accionVar = new Variable<String>();
				accionVar.setNombre("acción");
				accionVar.setValor(cancelar ? "Cancelar" : "Guardar");
				vars.add(accionVar);
			}
		}
		return vars;
	}

	private Aplicacion deepCopy(Aplicacion aplicacion, Aplicacion appBean) {
		appBean.setDescripcion(aplicacion.getDescripcion());
		appBean.setIcono(aplicacion.getIcono());
		appBean.setIdAplicacion(aplicacion.getIdAplicacion());
		appBean.setId(aplicacion.getId());
		appBean.setIcono(aplicacion.getIcono());

		List<Modulo> modulos = new ArrayList<Modulo>();
		appBean.setModulos(modulos);
		aplicacion.getModulos().forEach(modulo -> {
			modulos.add(cloneModulo(appBean, modulo));
		});
		List<Rol> roles = new ArrayList<Rol>();
		appBean.setRoles(roles);
		aplicacion.getRoles().forEach(rol -> {
			Rol r = new Rol();
			r.setAplicacion(appBean);
			r.setDescripcion(rol.getDescripcion());
			r.setIdRol(rol.getIdRol());
			List<Modulo> modulosRol = new ArrayList<Modulo>();
			r.setModulos(modulosRol);
			rol.getModulos().forEach(modulo -> {
				modulosRol.add(cloneModulo(appBean, modulo));
			});
			roles.add(r);
		});
		List<Ambito> ambitos = new ArrayList<Ambito>();
		appBean.setAmbito(ambitos);
		if (aplicacion.getAmbito() != null) {
			aplicacion.getAmbito().forEach(ambito -> {
				ambitos.add(cloneAmbito(appBean, ambito));
			});
		}
		return appBean;
	}

	private Ambito cloneAmbito(Aplicacion appBean, Ambito ambito) {
		Ambito a = new Ambito();
		a.setAplicacion(appBean);
		a.setClase(ambito.getClase());
		a.setDescripcion(ambito.getDescripcion());
		a.setGetAllUri(ambito.getGetAllUri());
		return a;
	}

	private Modulo cloneModulo(Aplicacion appBean, Modulo modulo) {
		Modulo m = new Modulo();
		m.setAplicacion(appBean);
		m.setDescripcion(modulo.getDescripcion());
		m.setIcono(modulo.getIcono());
		m.setIdModulo(modulo.getIdModulo());
		m.setFunciones(new ArrayList<Funcion>());
		copyFunciones(m, m.getFunciones(), modulo.getFunciones());
		return m;
	}

	private void copyFunciones(Modulo modulo, List<Funcion> funcionesDest, List<Funcion> funcionesOrigen) {
		funcionesOrigen.forEach(funcion -> {
			Funcion funcionC = new Funcion();
			funcionC.setDescripcion(funcion.getDescripcion());
			funcionC.setIdFuncion(funcion.getIdFuncion());
			funcionC.setModulo(modulo);
			funcionC.setOrden(funcion.getOrden());
			funcionC.setProcessKey(funcion.getProcessKey());
			funcionC.setViewEngine(funcion.getViewEngine());
			funcionC.setTitulo(funcion.getTitulo());
			if (funcion.getHijas() != null) {
				funcionC.setHijas(new ArrayList<Funcion>());
				copyFunciones(modulo, funcionC.getHijas(), funcion.getHijas());
				funcionC.getHijas().forEach(funcionCHija -> {
					funcionCHija.setPadre(funcionC);
				});
			}
			funcionC.setParametros(copyParametros(funcion.getParametros()));
			funcionesDest.add(funcionC);
		});
	}

	private List<Parametro> copyParametros(List<Parametro> parametros) {
		List<Parametro> clonedParametros = new ArrayList<Parametro>();
		if (parametros != null) {
			parametros.forEach(parametro -> {
				Parametro param = new Parametro();
				param.setNombre(parametro.getNombre());
				param.setValor(parametro.getValor());
				clonedParametros.add(param);
			});
		}
		return clonedParametros;
	}

	@Override
	protected String getComentarioTarea() {
		// TODO Auto-generated method stub
		return null;
	}

	/* **** CONSTRUCCION LAYOUT **** */

	private void doModulosAddTab() {
		if (modulosTabS.getSelectedTab() == modulosAddTab.getComponent()) {
			showModuloPopup(null, false);
		}
	}

	private void doRolesAddTab() {
		if (rolesTabS.getSelectedTab() == rolesAddTab.getComponent() && aplicacion.getModulos() != null && !aplicacion.getModulos().isEmpty()) {
			showRolPopup(null);
		} else {
			idAplicacion.focus();
		}
	}

	private boolean moduloExiste(Modulo m) {
		boolean existe = false;
		if (aplicacion.getModulos() != null) {
			for (Modulo mod : aplicacion.getModulos()) {
				if (mod.getIdModulo().equals(m.getIdModulo())) {
					existe = true;
					break;
				}
			}
		}
		return existe;
	}

	private void showModuloPopup(Modulo mod, boolean soloLectura) {
		final Window window = new Window("Nuevo Módulo");
		window.setModal(true);
		window.setClosable(false);
		window.setResizable(false);
		window.setWidth("350px");
		window.setHeight("290px");
		window.center();
		ModuloForm mf = new ModuloForm(window, soloLectura);
		if (mod != null) {
			mf.setModulo(mod);
		}
		window.setContent(mf);
		UI.getCurrent().addWindow(window);

		window.addCloseListener(event -> {
			Modulo m = null;
			if ((m = ((ModuloForm) window.getContent()).getModulo()) != null) {
				if (moduloExiste(m)) {
					showModuloPopup(m, false);
					appendNotification(Type.ERROR_MESSAGE, "Ya existe un módulo con el id " + m.getIdModulo());
				} else {
					addModuloTab(m);
				}
			} else {
				idAplicacion.focus();
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void addModuloTab(Modulo m) {
		FuncionesTab ft = new FuncionesTab(esConsulta, false);
		ft.setModulo(m);
		Tab newTab = modulosTabS.addTab(ft);
		newTab.setClosable(false);
		newTab.setCaption(m.getIdModulo());
		if (!esConsulta) {
			modulosTabS.setSelectedTab(newTab);
			if (aplicacion.getModulos() == null) {
				aplicacion.setModulos(new ArrayList<Modulo>());
			}
			
			if (esModificacion && aplicacion.getModulos().indexOf(m) < 0) { //Solo si es modificación nos interesa verificar si el modulo ya existe antes de agregarlo
				aplicacion.getModulos().add(m);
			} else {
				if(!esModificacion) {
					aplicacion.getModulos().add(m);	
				}
			}
			
			m.setAplicacion(aplicacion);
			modulosTabS.setTabPosition(newTab, modulosTabS.getTabPosition(modulosAddTab));
			Iterator<FocusListener> fEvListeners = (Iterator<FocusListener>) modulosTabS.getListeners(FocusEvent.class).iterator();
			if (fEvListeners.hasNext()) {
				modulosTabS.removeFocusListener(fEvListeners.next());
				modulosTabS.addSelectedTabChangeListener(sTabChEvent -> {
					doModulosAddTab();
				});
			}
		}

		if (esConsulta || esModificacion) {
			ft.addFunciones(m.getFunciones());
		}
	}

	private void showRolPopup(Rol rol) {
		final Window window = new Window("Nuevo Rol");
		window.setModal(true);
		window.setClosable(false);
		window.setResizable(false);
		window.setWidth("350px");
		window.setHeight("230px");
		window.center();
		RolForm rf = new RolForm(window);

		if (rol != null) {
			rf.setRol(rol);
		}

		window.setContent(rf);
		UI.getCurrent().addWindow(window);

		window.addCloseListener(event -> {
			Rol r = null;
			if ((r = rf.getRol()) != null) {
				if (aplicacion.getRoles() == null) {
					aplicacion.setRoles(new ArrayList<Rol>());
				}

				if (rolExiste(r)) {
					showRolPopup(r);
					appendNotification(Type.ERROR_MESSAGE, "Ya existe un rol con el id " + r.getIdRol());
				} else {
					r.setAplicacion(aplicacion);
					aplicacion.getRoles().add(r);

					addRolTab(r);
				}
			} else {
				idAplicacion.focus();
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void addRolTab(Rol r) {
		FuncionesTab ft = new FuncionesTab(esConsulta, true);

		for (Modulo m : aplicacion.getModulos()) {
			Modulo mRol = null;
			ft.addOrUpdateFuncionesModulo(m, 
							((esConsulta || esModificacion) && r.getModulos()!=null) && (mRol = r.getModulos().stream().filter(moduloRol -> (moduloRol.getIdModulo().equals(m.getIdModulo()))).findFirst().orElse(null)) != null ? 
									mRol : 
							null);
		}

		Tab newTab = rolesTabS.addTab(ft);
		newTab.setClosable(false);
		newTab.setCaption(r.getIdRol());

		if (!esConsulta) {
			rolesTabS.setSelectedTab(newTab);
			rolesTabS.setTabPosition(newTab, rolesTabS.getTabPosition(rolesAddTab));

			Iterator<FocusListener> fEvListeners = (Iterator<FocusListener>) rolesTabS.getListeners(FocusEvent.class).iterator();
			if (fEvListeners.hasNext()) {
				rolesTabS.removeFocusListener(fEvListeners.next());
				rolesTabS.addSelectedTabChangeListener(sTabChEvent -> {
					doRolesAddTab();
				});
			}
		}
	}

	private boolean rolExiste(Rol r) {
		boolean existe = false;
		for (Rol rol : aplicacion.getRoles()) {
			if (rol.getIdRol().equals(r.getIdRol())) {
				existe = true;
				break;
			}
		}
		return existe;
	}

	private void buildMainLayout() {
		flyt.setImmediate(false);
		flyt.setWidth("100%");
		flyt.setHeight("100%");
		flyt.setMargin(false);
		flyt.setSpacing(true);

		// siglas
		idAplicacion = new TextField();
		idAplicacion.setCaption("Id Aplicacion");
		idAplicacion.setImmediate(false);
		idAplicacion.setNullRepresentation("");
		idAplicacion.setInvalidCommitted(false);
		idAplicacion.setWidth("-1px");
		idAplicacion.setHeight("-1px");
		flyt.addComponent(idAplicacion);

		// descripcion
		descripcion = new TextField();
		descripcion.setCaption("Descripción");
		descripcion.setImmediate(false);
		descripcion.setWidth("-1px");
		descripcion.setHeight("-1px");
		descripcion.setNullRepresentation("");
		descripcion.setInvalidCommitted(false);
		flyt.addComponent(descripcion);

		// icono
		icono = new TextField();
		icono.setCaption("Icono");
		icono.setImmediate(false);
		icono.setWidth("-1px");
		icono.setHeight("-1px");
		icono.setNullRepresentation("");
		icono.setInvalidCommitted(false);
		flyt.addComponent(icono);

		// modulosRolesAcc
		modulosRolesAmbitoAcc = buildModulosRolesAcc();
		flyt.addComponent(modulosRolesAmbitoAcc);

		// botones
		botonera = new HorizontalLayout();
		botonera.setWidth("100%");
		botonera.setSpacing(true);
		botonera.setMargin(false);
		flyt.addComponent(botonera);
		
		Label fill = new Label();
		botonera.addComponent(fill);
		botonera.setComponentAlignment(fill, Alignment.MIDDLE_LEFT);
		botonera.setExpandRatio(fill, 1.0f);
		
		guardarButton = new Button();
		guardarButton.setImmediate(true);
		guardarButton.setWidth("-1px");
		guardarButton.setHeight("-1px");
		botonera.addComponent(guardarButton);
		botonera.setComponentAlignment(guardarButton, Alignment.MIDDLE_RIGHT);
		botonera.setExpandRatio(guardarButton, 0.0f);
	}

	private Accordion buildModulosRolesAcc() {
		// common part: create layout
		modulosRolesAmbitoAcc = new Accordion();
		modulosRolesAmbitoAcc.setImmediate(true);
		modulosRolesAmbitoAcc.setSizeFull();
		modulosRolesAmbitoAcc.setHeight("-1px");
		// modulosPanel
		modulosPanel = buildModulosPanel();
		modulosRolesAmbitoAcc.addTab(modulosPanel, "Módulos", null);

		// rolesPanel
		rolesPanel = buildRolesPanel();
		modulosRolesAmbitoAcc.addTab(rolesPanel, "Roles", null);

		// ambitoPanel
		ambitoPanel = buildAmbitoPanel();
		modulosRolesAmbitoAcc.addTab(ambitoPanel, "Ambitos", null);
		return modulosRolesAmbitoAcc;
	}

	private Panel buildAmbitoPanel() {
		// common part: create layout
		ambitoPanel = new Panel();
		ambitoPanel.setImmediate(false);
		ambitoPanel.setSizeFull();
		// verticalLayout_4
		verticalLayout_4 = buildVerticalLayout_4();
		ambitoPanel.setContent(verticalLayout_4);

		return ambitoPanel;
	}

	private VerticalLayout buildVerticalLayout_4() {
		// common part: create layout
		verticalLayout_4 = new VerticalLayout();
		verticalLayout_4.setImmediate(false);
		verticalLayout_4.setMargin(true);
		verticalLayout_4.setSpacing(true);

		// Ambito Table
		ambitosTable = new GunixMTable<Ambito>(Ambito.class);
		ambitosTable.setTableFieldFactory(new GunixTableFieldFactory());
		ambitosTable.setVisibleColumns("clase", "descripcion","getAllUri");
		ambitosTable.setColumnHeaders(new String[]{"Clase", "Descripción","Get All URI"});
		ambitosTable.setEditable(!esConsulta);
		ambitosTable.setImmediate(false);
		ambitosTable.setHeight("255px");
		ambitosTable.setWidth("100%");
		verticalLayout_4.addComponent(ambitosTable);
		
		if(!esConsulta){
			Button agregar = new Button("Agregar");
			agregar.addClickListener(clickEvent -> {
				if (ultimoAmbitoAgregadoAManoIsValid()) {
					Ambito nuevoAmbito = new Ambito();
					nuevoAmbito.setAplicacion(aplicacion);
					ultimoAmbitoAgregadoAMano = nuevoAmbito;
					ambitosTable.getContainerDataSource().addItem(nuevoAmbito);
				}
			});
			verticalLayout_4.addComponent(agregar);	
		}

		return verticalLayout_4;
	}

	private boolean ultimoAmbitoAgregadoAManoIsValid() {
		boolean ans = true;
		if (ultimoAmbitoAgregadoAMano != null) {
			ans = isValido(ambitosTable, ultimoAmbitoAgregadoAMano, Ambito.class);
		}
		return ans;
	}

	private Panel buildModulosPanel() {
		// common part: create layout
		modulosPanel = new Panel();
		modulosPanel.setImmediate(false);
		modulosPanel.setSizeFull();
		modulosPanel.setHeight("-1px");
		// verticalLayout_2
		verticalLayout_2 = buildVerticalLayout_2();
		modulosPanel.setContent(verticalLayout_2);

		return modulosPanel;
	}

	private VerticalLayout buildVerticalLayout_2() {
		// common part: create layout
		verticalLayout_2 = new VerticalLayout();
		verticalLayout_2.setImmediate(false);
		verticalLayout_2.setSizeFull();
		verticalLayout_2.setMargin(false);
		verticalLayout_2.setSpacing(false);

		// modulosTabS
		modulosTabS = new TabSheet();
		modulosTabS.setImmediate(false);
		modulosTabS.setSizeFull();
		verticalLayout_2.addComponent(modulosTabS);

		return verticalLayout_2;
	}

	private Panel buildRolesPanel() {
		// common part: create layout
		rolesPanel = new Panel();
		rolesPanel.setImmediate(false);
		rolesPanel.setSizeFull();
		rolesPanel.setHeight("-1px");
		// verticalLayout_3
		verticalLayout_3 = buildVerticalLayout_3();
		rolesPanel.setContent(verticalLayout_3);

		return rolesPanel;
	}

	private VerticalLayout buildVerticalLayout_3() {
		// common part: create layout
		verticalLayout_3 = new VerticalLayout();
		verticalLayout_3.setImmediate(false);
		verticalLayout_3.setWidth("100.0%");
		verticalLayout_3.setHeight("100.0%");
		verticalLayout_3.setMargin(false);
		
		// rolesTabS
		rolesTabS = new TabSheet();
		rolesTabS.setImmediate(false);
		rolesTabS.setWidth("100.0%");
		rolesTabS.setHeight("100.0%");
		verticalLayout_3.addComponent(rolesTabS);

		return verticalLayout_3;
	}
}
