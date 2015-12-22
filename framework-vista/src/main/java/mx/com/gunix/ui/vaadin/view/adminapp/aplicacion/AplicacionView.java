package mx.com.gunix.ui.vaadin.view.adminapp.aplicacion;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.security.domain.Funcion;
import mx.com.gunix.framework.security.domain.Modulo;
import mx.com.gunix.framework.security.domain.Rol;
import mx.com.gunix.framework.ui.GunixFile;
import mx.com.gunix.framework.ui.vaadin.component.GunixUploadField;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;
import mx.com.gunix.framework.ui.vaadin.view.AbstractGunixView;
import mx.com.gunix.framework.ui.vaadin.view.SecuredView;
import mx.com.gunix.ui.vaadin.view.adminapp.aplicacion.components.FuncionesTab;
import mx.com.gunix.ui.vaadin.view.adminapp.aplicacion.components.ModuloForm;
import mx.com.gunix.ui.vaadin.view.adminapp.aplicacion.components.RolForm;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.UserError;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@GunixVaadinView
public class AplicacionView extends AbstractGunixView<AplicacionView.AplicacionViewBean> implements SecuredView {
	public static class AplicacionViewBean extends Aplicacion {

		@Override
		protected int doHashCode() {
			return 31;
		}

		private static final long serialVersionUID = 1L;
		private GunixFile iconoFile;

		public GunixFile getIconoFile() {
			return iconoFile;
		}

		public void setIconoFile(GunixFile iconoFile) {
			setIcono(iconoFile.getFileName());
			this.iconoFile = iconoFile;
		}
	}

	private static final long serialVersionUID = 1L;

	private static final String MODULOS_ROLES_WIDTH = "1000px";

	private Button guardarButton;
	private Button cancelarButton;
	private Accordion modulosRolesAcc;
	private Panel rolesPanel;
	private VerticalLayout verticalLayout_3;
	private TabSheet rolesTabS;
	private Panel modulosPanel;
	private VerticalLayout verticalLayout_2;
	private TabSheet modulosTabS;
	private GunixUploadField iconoFile;
	private TextField descripcion;
	private TextField idAplicacion;
	private Tab modulosAddTab;
	private Tab rolesAddTab;
	private AplicacionViewBean aplicacion;
	private Boolean cancelar = Boolean.FALSE;

	private Boolean esConsulta;
	private Boolean esModificacion;

	@SuppressWarnings("unchecked")
	@Override
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

		if (esConsulta || esModificacion) {
			aplicacion.getModulos().forEach(modulo -> {
				addModuloTab(modulo);
			});
			aplicacion.getRoles().forEach(rol -> {
				addRolTab(rol);
			});

			idAplicacion.setReadOnly(true);

			if (esModificacion) {
				cancelarButton.setEnabled(true);
				cancelarButton.setVisible(true);
				cancelarButton.setDisableOnClick(true);
			} else {
				descripcion.setReadOnly(true);
				iconoFile.setReadOnly(true);
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
		modulosRolesAcc.getTab(0).setIcon(new ThemeResource("img/1440815258_blockdevice.png"));
		modulosRolesAcc.getTab(1).setIcon(new ThemeResource("img/1440815816_To_do_list.png"));

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
			modulosRolesAcc.addSelectedTabChangeListener(event -> {
				if (modulosRolesAcc.getSelectedTab() == rolesPanel) {
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
				
				flyt.addComponent(cancelarButton);
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
					commit(ibve -> {
						flyt.setComponentError(new UserError(ibve.getMessage()));
					});
					GunixFile iconoF = (GunixFile) iconoFile.getValue();
					if ((esModificacion && iconoF != null && iconoF.getFile() != null) || !esModificacion) {
						Files.copy(iconoF.getFile().toPath(), new File(VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/VAADIN/themes/gunix/img/" + aplicacion.getIcono()).toPath(),
								REPLACE_EXISTING);
					}
				}
				completaTarea();
			} catch (CommitException ce) {
				Notification.show("Existen errores en el formulario", Type.ERROR_MESSAGE);
				guardarButton.setEnabled(true);
				guardarButton.setDisableOnClick(true);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});

		aplicacion = getBean();
		if (esConsulta || esModificacion) {
			deepCopy(((List<Aplicacion>) $("resultado")).get(0), aplicacion);
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
					Notification.show("Existen errores en el formulario:" + e.getMessage(), Type.ERROR_MESSAGE);
					modulosRolesAcc.setSelectedTab(modulosPanel);
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

		if (appBean instanceof AplicacionViewBean) {
			if (aplicacion instanceof AplicacionViewBean) {
				((AplicacionViewBean) appBean).setIconoFile(((AplicacionViewBean) aplicacion).getIconoFile());
			} else {
				GunixFile gf = new GunixFile();
				gf.setFileName(aplicacion.getIcono());
				((AplicacionViewBean) appBean).setIconoFile(gf);
			}
		}

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
		return appBean;
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
			funcionC.setParametros(funcion.getParametros());
			funcionC.setProcessKey(funcion.getProcessKey());
			funcionC.setTitulo(funcion.getTitulo());
			if (funcion.getHijas() != null) {
				funcionC.setHijas(new ArrayList<Funcion>());
				copyFunciones(modulo, funcionC.getHijas(), funcion.getHijas());
				funcionC.getHijas().forEach(funcionCHija -> {
					funcionCHija.setPadre(funcionC);
				});
			}
			funcionesDest.add(funcionC);
		});
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
		window.setWidth("25%");
		window.setModal(true);
		window.setClosable(false);
		window.setResizable(false);
		window.setWidth("420px");
		window.setHeight("350px");
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
					Notification.show("Ya existe un módulo con el id " + m.getIdModulo(), Type.ERROR_MESSAGE);
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
		window.setWidth("25%");
		window.setModal(true);
		window.setClosable(false);
		window.setResizable(false);
		window.setWidth("420px");
		window.setHeight("300px");
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
					Notification.show("Ya existe un rol con el id " + r.getIdRol(), Type.ERROR_MESSAGE);
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
		iconoFile = new GunixUploadField();
		iconoFile.setCaption("Icono");
		iconoFile.setImmediate(false);
		iconoFile.setWidth("-1px");
		iconoFile.setHeight("-1px");
		iconoFile.setAcceptFilter(".jpe,.jpg,.jpeg,.gif,.png");
		iconoFile.setRequired(true);
		flyt.addComponent(iconoFile);

		// modulosRolesAcc
		modulosRolesAcc = buildModulosRolesAcc();
		flyt.addComponent(modulosRolesAcc);

		// guardarButton
		guardarButton = new Button();
		guardarButton.setImmediate(true);
		guardarButton.setWidth("-1px");
		guardarButton.setHeight("-1px");
		flyt.addComponent(guardarButton);
	}

	private Accordion buildModulosRolesAcc() {
		// common part: create layout
		modulosRolesAcc = new Accordion();
		modulosRolesAcc.setImmediate(true);
		modulosRolesAcc.setWidth("-1px");
		modulosRolesAcc.setHeight("-1px");

		// modulosPanel
		modulosPanel = buildModulosPanel();
		modulosRolesAcc.addTab(modulosPanel, "Módulos", null);

		// rolesPanel
		rolesPanel = buildRolesPanel();
		modulosRolesAcc.addTab(rolesPanel, "Roles", null);

		return modulosRolesAcc;
	}

	private Panel buildModulosPanel() {
		// common part: create layout
		modulosPanel = new Panel();
		modulosPanel.setImmediate(false);
		modulosPanel.setWidth(MODULOS_ROLES_WIDTH);
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
		verticalLayout_2.setWidth("100.0%");
		verticalLayout_2.setHeight("100.0%");
		verticalLayout_2.setMargin(false);

		// modulosTabS
		modulosTabS = new TabSheet();
		modulosTabS.setImmediate(false);
		modulosTabS.setWidth("100.0%");
		modulosTabS.setHeight("100.0%");
		verticalLayout_2.addComponent(modulosTabS);

		return verticalLayout_2;
	}

	private Panel buildRolesPanel() {
		// common part: create layout
		rolesPanel = new Panel();
		rolesPanel.setImmediate(false);
		rolesPanel.setWidth(MODULOS_ROLES_WIDTH);
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
