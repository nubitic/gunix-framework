package mx.com.gunix.ui.vaadin.view.aplicacion.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mx.com.gunix.framework.security.domain.Funcion;
import mx.com.gunix.framework.security.domain.Modulo;
import mx.com.gunix.framework.ui.vaadin.component.GunixBeanFieldGroup;
import mx.com.gunix.framework.ui.vaadin.component.GunixTableFieldFactory;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.Property;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.UserError;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class FuncionesTab extends CustomComponent {

	@AutoGenerated
	private VerticalLayout mainLayout;
	@AutoGenerated
	private TreeTable funcionesTreeTable;
	@AutoGenerated
	private Button agregarFuncion;
	private Modulo modulo;
	private Boolean esSoloLectura;
	private Boolean esParaRoles;

	private GunixBeanFieldGroup<Modulo> moduloBFG;

	private int newFuncionCount = 0;

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */
	private static final long serialVersionUID = 1L;
	private static final String DESC_MODULO = "M�dulo ";

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	@SuppressWarnings("unchecked")
	public FuncionesTab(boolean soloLectura, boolean esParaRoles) {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		funcionesTreeTable.setTableFieldFactory(new GunixTableFieldFactory());
		funcionesTreeTable.addContainerProperty("idFuncion", String.class, "", "Id Funci�n", null, null);
		funcionesTreeTable.addContainerProperty("titulo", String.class, "", "T�tulo", null, null);
		funcionesTreeTable.addContainerProperty("descripcion", String.class, "", "Descripci�n", null, null);
		funcionesTreeTable.addContainerProperty("processKey", String.class, "", "Proceso", null, null);

		if (!soloLectura && !esParaRoles) {
			funcionesTreeTable.addContainerProperty("agregarButton", Button.class, null, "", null, null);
			funcionesTreeTable.setColumnExpandRatio("agregarButton", 0.5f);
			funcionesTreeTable.setEditable(true);
			agregarFuncion.setDisableOnClick(true);

			agregarFuncion.addClickListener(event -> {
				doAgregarFuncion(event);
			});
		} else {
			if (esParaRoles && !soloLectura) {
				funcionesTreeTable.setSelectable(true);
			} else {
				if (esParaRoles && soloLectura) {
					funcionesTreeTable.setSelectable(false);
				}
			}
			if (esParaRoles) {
				funcionesTreeTable.setMultiSelect(true);
				funcionesTreeTable.addValueChangeListener(evnt -> {
					Set<Funcion> newValue = new LinkedHashSet<Funcion>();
					completaRutaFuncion(newValue, (Set<Funcion>) funcionesTreeTable.getValue());
					funcionesTreeTable.setValue(newValue);
				});
				funcionesTreeTable.setCellStyleGenerator((table, itemId, propertyId) -> {
					String style = null;
					if (propertyId == null) {
						// Styling for row
						String idFuncion = (String) table.getItem(itemId).getItemProperty("idFuncion").getValue();
						if (idFuncion.startsWith(DESC_MODULO)) {
							style = "highlight-text-bold";
						}
					}
					return style;
				});
			}
			agregarFuncion.setVisible(false);
		}

		if (!esParaRoles) {
			funcionesTreeTable.addContainerProperty("parametrosButton", Button.class, "", "", null, null);
			funcionesTreeTable.setColumnExpandRatio("parametrosButton", 0.75f);
		}

		funcionesTreeTable.setColumnExpandRatio("idFuncion", 1.5f);
		funcionesTreeTable.setColumnExpandRatio("titulo", 1.083f);
		funcionesTreeTable.setColumnExpandRatio("descripcion", 1.083f);
		funcionesTreeTable.setColumnExpandRatio("processKey", 1.083f);
		funcionesTreeTable.setPageLength(10);

		this.esSoloLectura = soloLectura;
		this.esParaRoles = esParaRoles;
	}

	private void completaRutaFuncion(Set<Funcion> newValue, Set<Funcion> value) {
		for (Funcion funcionSel : value) {
			agregaPadre(funcionSel, newValue);
		}
	}

	private void agregaPadre(Funcion funcionSel, Set<Funcion> newValue) {
		Funcion funcionPadre = (Funcion) funcionesTreeTable.getParent(funcionSel);
		if (funcionPadre != null) {
			agregaPadre(funcionPadre, newValue);
		}
		newValue.add(funcionSel);
	}

	public void removeAllItems() {
		funcionesTreeTable.removeAllItems();
	}

	@SuppressWarnings("unchecked")
	public void addOrUpdateFuncionesModulo(Modulo m, Modulo mSel) {
		Funcion moduloFuncion = new Funcion();
		moduloFuncion.setIdFuncion(DESC_MODULO + m.getIdModulo());
		moduloFuncion.setTitulo(m.getDescripcion());
		moduloFuncion.setModulo(m);
		Set<Funcion> selItems = new LinkedHashSet<Funcion>((Set<Funcion>) funcionesTreeTable.getValue());
		if (mSel != null) {
			selItems.add(moduloFuncion);
		}
		addFunciones(moduloFuncion, m.getFunciones(), mSel != null ? mSel.getFunciones() : null, selItems);
		funcionesTreeTable.setValue(selItems);
	}

	public void addFunciones(List<Funcion> funciones) {
		if (funciones != null) {
			addFunciones(null, funciones, null, null);
		}
	}

	private void addFunciones(Funcion funcionPadre, List<Funcion> funciones, List<Funcion> funcionesSel, Set<Funcion> selItems) {
		if (funcionPadre != null && funcionesTreeTable.getItem(funcionPadre) == null) {
			funcionesTreeTable.addItem(new Object[] { funcionPadre.getIdFuncion(), funcionPadre.getTitulo(), funcionPadre.getDescripcion(), funcionPadre.getProcessKey() }, funcionPadre);
			funcionesTreeTable.setCollapsed(funcionPadre, false);
			funcionesTreeTable.setChildrenAllowed(funcionPadre, true);
			if (isSelected(funcionPadre, funcionesSel)) {
				selItems.add(funcionPadre);
			}
		}

		for (Funcion funcionHija : funciones) {
			Object[] cells = !esParaRoles ? new Object[] { funcionHija.getIdFuncion(), funcionHija.getTitulo(), funcionHija.getDescripcion(), funcionHija.getProcessKey(),
					(funcionHija.getHijas() == null || funcionHija.getHijas().isEmpty()) ? buildParametrosButton(funcionHija, esSoloLectura) : null } : new Object[] { funcionHija.getIdFuncion(),
					funcionHija.getTitulo(), funcionHija.getDescripcion(), funcionHija.getProcessKey() };

			funcionesTreeTable.addItem(cells, funcionHija);
			funcionesTreeTable.setCollapsed(funcionHija, false);
			funcionesTreeTable.setChildrenAllowed(funcionHija, true);
			if (funcionPadre != null) {
				funcionesTreeTable.setParent(funcionHija, funcionPadre);
			}
			if (isSelected(funcionHija, funcionesSel)) {
				selItems.add(funcionHija);
			}
			if (funcionHija.getHijas() != null && !funcionHija.getHijas().isEmpty()) {
				addFunciones(funcionHija, funcionHija.getHijas(), funcionesSel, selItems);
			}
		}
	}

	private boolean isSelected(Funcion funcionPadre, List<Funcion> funcionesSel) {
		boolean isSelected = false;
		if (funcionesSel != null) {
			for (Funcion f : funcionesSel) {
				if (f.equals(funcionPadre)) {
					isSelected = true;
					break;
				} else {
					if (f.getHijas() != null && !f.getHijas().isEmpty()) {
						if(isSelected(funcionPadre, f.getHijas())) {
							isSelected = true;
							break;		
						}
					}
				}
			}
		}
		return isSelected;
	}

	@SuppressWarnings("unchecked")
	private void doAgregarFuncion(ClickEvent event) {
		if (modulo.getFunciones() == null) {
			modulo.setFunciones(new ArrayList<Funcion>());
		}
		HierarchicalContainer container = (HierarchicalContainer) funcionesTreeTable.getContainerDataSource();

		Funcion f = new Funcion();
		f.setModulo(modulo);
		f.setIdFuncion(new StringBuilder("funcion").append(newFuncionCount++).toString());
		BeanItem<Funcion> beanItem = new BeanItem<Funcion>(f);
		GunixBeanFieldGroup<Funcion> bfgf = new GunixBeanFieldGroup<Funcion>(Funcion.class);
		bfgf.setItemDataSource(beanItem);
		Button button = new Button("Agregar", newFuncButtonEvent -> {
			doAgregarFuncion(newFuncButtonEvent);
		});

		button.setData(bfgf);

		funcionesTreeTable.addItem(beanItem);
		container.getItem(beanItem).getItemProperty("agregarButton").setValue(button);

		if ((button = (Button) event.getComponent()).getParent() == funcionesTreeTable) {
			Funcion padre = ((GunixBeanFieldGroup<Funcion>) button.getData()).getItemDataSource().getBean();
			beanItem.getBean().setPadre(padre);
			if (padre.getHijas() == null) {
				padre.setHijas(new ArrayList<Funcion>());
			}
			padre.getHijas().add(beanItem.getBean());

			funcionesTreeTable.setParent(beanItem, ((GunixBeanFieldGroup<Funcion>) button.getData()).getItemDataSource());
		}

		container.getItem(beanItem).getItemProperty("parametrosButton").setValue(buildParametrosButton(f, false));

		funcionesTreeTable.setCollapsed(beanItem, false);
		funcionesTreeTable.setChildrenAllowed(beanItem, true);

		Property<String> idFuncionProp = container.getContainerProperty(beanItem, "idFuncion");
		idFuncionProp.setValue(f.getIdFuncion());
		if (f.getPadre() == null) {
			modulo.getFunciones().add(f);
		}
		agregarFuncion.setEnabled(true);
	}

	private Button buildParametrosButton(Funcion f, boolean esSoloLectura) {
		Button button = new Button("Par�metros", newFuncButtonEvent -> {
			final Window window = new Window("Par�metros");
			window.setWidth("25%");
			window.setModal(true);
			window.setClosable(false);
			window.setResizable(false);
			window.setWidth("490px");
			window.setHeight("470px");
			window.center();
			ParametrosForm pf = new ParametrosForm(window, esSoloLectura);
			if (f.getParametros() != null && !f.getParametros().isEmpty()) {
				pf.setParametros(f.getParametros());
			}
			window.setContent(pf);
			UI.getCurrent().addWindow(window);

			window.addCloseListener(event -> {
				f.setParametros(pf.getParametros());
			});
		});

		return button;
	}

	@SuppressWarnings("unchecked")
	public List<Modulo> getFuncionesSeleccionadas() {
		List<Modulo> funcionesSel = new ArrayList<Modulo>();
		Set<Funcion> selectedIds = (Set<Funcion>) funcionesTreeTable.getValue();
		int descModuloLength = DESC_MODULO.length();
		for (Funcion selectedFuncion : selectedIds) {
			if (funcionesTreeTable.getParent(selectedFuncion) == null) {
				Modulo m = new Modulo();
				m.setIdModulo(selectedFuncion.getIdFuncion().substring(descModuloLength));
				m.setAplicacion(selectedFuncion.getModulo().getAplicacion());
				funcionesSel.add(m);
			} else {
				if (selectedFuncion.getPadre() == null) {
					agregaFuncionRootAModulo((Funcion) funcionesTreeTable.getParent(selectedFuncion), selectedFuncion, funcionesSel);
				} else {
					agregaFuncionAFuncionRoot(selectedFuncion, funcionesSel);
				}
			}
		}
		return funcionesSel;
	}

	private void agregaFuncionAFuncionRoot(Funcion selectedFuncion, List<Modulo> modulos) {
		Funcion funPadre = (Funcion) funcionesTreeTable.getParent(selectedFuncion);
		for (Modulo m : modulos) {
			doAgregaFuncionAFuncionRoot(funPadre, selectedFuncion, m.getFunciones());
		}
	}

	private boolean doAgregaFuncionAFuncionRoot(Funcion funPadre, Funcion selectedFuncion, List<Funcion> funciones) {
		boolean found = false;
		if (funciones != null) {
			for (Funcion funcion : funciones) {
				if (funcion.equals(funPadre)) {
					Funcion clonFuncionRoot = new Funcion();
					clonFuncionRoot.setModulo(funPadre.getModulo());
					clonFuncionRoot.setIdFuncion(selectedFuncion.getIdFuncion());
					if (funcion.getHijas() == null) {
						funcion.setHijas(new ArrayList<Funcion>());
					}
					clonFuncionRoot.setPadre(funcion);
					funcion.getHijas().add(clonFuncionRoot);
					found = true;
					break;
				}

				if (!found) {
					if ((found = doAgregaFuncionAFuncionRoot(funPadre, selectedFuncion, funcion.getHijas()))) {
						break;
					}
				}
			}
		}
		return found;
	}

	private void agregaFuncionRootAModulo(Funcion moduloFuncion, Funcion selectedFuncion, List<Modulo> modulos) {
		String idModuloClean = moduloFuncion.getIdFuncion().substring(DESC_MODULO.length());
		for (Modulo m : modulos) {
			if (m.getIdModulo().equals(idModuloClean)) {
				Funcion clonFuncionRoot = new Funcion();
				clonFuncionRoot.setModulo(m);
				clonFuncionRoot.setIdFuncion(selectedFuncion.getIdFuncion());
				clonFuncionRoot.setHijas(new ArrayList<Funcion>());
				if (m.getFunciones() == null) {
					m.setFunciones(new ArrayList<Funcion>());
				}
				m.getFunciones().add(clonFuncionRoot);
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	private GunixBeanFieldGroup<Funcion> getBeanFieldGroupByFuncion(Funcion funcion) {
		GunixBeanFieldGroup<Funcion> bfg = null;
		HierarchicalContainer container = (HierarchicalContainer) funcionesTreeTable.getContainerDataSource();

		for (Object beanItemF : container.getItemIds()) {
			BeanItem<Funcion> beanItem = (BeanItem<Funcion>) beanItemF;
			if (beanItem.getBean() == funcion) {
				bfg = (GunixBeanFieldGroup<Funcion>) ((Button) container.getItem(beanItem).getItemProperty("agregarButton").getValue()).getData();
				break;
			}
		}
		return bfg;
	}

	public void setModulo(Modulo modulo) {
		moduloBFG = new GunixBeanFieldGroup<Modulo>(Modulo.class);
		moduloBFG.setItemDataSource(new BeanItem<Modulo>(modulo));
		this.modulo = modulo;
	}

	public Modulo getModulo() {
		return this.modulo;
	}

	public void commit() throws CommitException {
		funcionesTreeTable.setComponentError(null);
		HierarchicalContainer container = (HierarchicalContainer) funcionesTreeTable.getContainerDataSource();
		if (!validaFunciones(container, modulo.getFunciones())) {
			throw new CommitException("El m�dulo " + modulo.getIdModulo() + " tiene errores");
		} else {
			moduloBFG.commit(ibve -> {
			});
		}
	}

	private boolean validaFunciones(HierarchicalContainer container, List<Funcion> funciones) {
		boolean sinErrores = true;
		if (funciones != null) {
			for (Funcion funcion : funciones) {
				sinErrores = sinErrores & validaFuncion(container, funcion);
				if (funcion.getHijas() != null && !funcion.getHijas().isEmpty()) {
					sinErrores = sinErrores & validaFunciones(container, funcion.getHijas());
				}
			}
		}
		return sinErrores;
	}

	@SuppressWarnings("unchecked")
	private boolean validaFuncion(HierarchicalContainer container, Funcion funcion) {
		boolean sinErrores = true;
		GunixBeanFieldGroup<Funcion> bfgf = getBeanFieldGroupByFuncion(funcion);
		Iterator<Component> componentIterator = funcionesTreeTable.iterator();

		if (componentIterator != null) {
			Property<String> idFuncionProp = container.getContainerProperty(bfgf.getItemDataSource(), "idFuncion");
			Property<String> tituloProp = container.getContainerProperty(bfgf.getItemDataSource(), "titulo");
			Property<String> descripcionProp = container.getContainerProperty(bfgf.getItemDataSource(), "descripcion");
			Property<String> processKeyProp = container.getContainerProperty(bfgf.getItemDataSource(), "processKey");
			int components = 0;
			Map<Field<String>, Property<?>> prevPropDS = new HashMap<Field<String>, Property<?>>();
			AbstractComponent processKeyField = null;
			while (componentIterator.hasNext()) {
				Component c = componentIterator.next();
				if (c instanceof Field) {
					Field<String> field = (Field<String>) c;
					if (field.getPropertyDataSource().equals(idFuncionProp)) {
						prevPropDS.put(field, field.getPropertyDataSource());
						((AbstractComponent) field).setComponentError(null);
						bfgf.bind(field, "idFuncion");
						field.setValue(idFuncionProp.getValue());
						components++;
					} else {
						if (field.getPropertyDataSource().equals(tituloProp)) {
							prevPropDS.put(field, field.getPropertyDataSource());
							((AbstractComponent) field).setComponentError(null);
							bfgf.bind(field, "titulo");
							field.setValue(tituloProp.getValue());
							components++;
						} else {
							if (field.getPropertyDataSource().equals(descripcionProp)) {
								prevPropDS.put(field, field.getPropertyDataSource());
								((AbstractComponent) field).setComponentError(null);
								bfgf.bind(field, "descripcion");
								field.setValue(descripcionProp.getValue());
								components++;
							} else {
								if (field.getPropertyDataSource().equals(processKeyProp)) {
									processKeyField = (AbstractComponent) field;
									prevPropDS.put(field, field.getPropertyDataSource());
									((AbstractComponent) field).setComponentError(null);
									bfgf.bind(field, "processKey");
									field.setValue(processKeyProp.getValue());
									components++;
								}
							}
						}
					}
				}
				if (components == 4) {
					break;
				}
			}

			try {
				final AbstractComponent processKeyFieldLamba = processKeyField;
				bfgf.commit(ibve -> {
					processKeyFieldLamba.setComponentError(new UserError(ibve.getMessage()));
				});
				sinErrores = true;
			} catch (CommitException e) {
				sinErrores = false;
				for (Field<?> f : e.getInvalidFields().keySet()) {
					((AbstractComponent) f).setComponentError(new UserError(e.getInvalidFields().get(f).getCauses()[0].getMessage()));
				}
			} finally {
				for (Field<?> f : prevPropDS.keySet()) {
					bfgf.unbind(f);
					f.setPropertyDataSource(prevPropDS.get(f));
					f.setBuffered(false);
				}
			}
		}
		return sinErrores;
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");

		// agregarFuncion
		agregarFuncion = new Button();
		agregarFuncion.setCaption("Agregar Men�");
		agregarFuncion.setImmediate(true);
		agregarFuncion.setWidth("-1px");
		agregarFuncion.setHeight("-1px");
		mainLayout.addComponent(agregarFuncion);

		// funcionesTreeTable
		funcionesTreeTable = new TreeTable();
		funcionesTreeTable.setImmediate(false);
		funcionesTreeTable.setWidth("100%");
		funcionesTreeTable.setInvalidAllowed(false);
		mainLayout.addComponent(funcionesTreeTable);

		return mainLayout;
	}

}
