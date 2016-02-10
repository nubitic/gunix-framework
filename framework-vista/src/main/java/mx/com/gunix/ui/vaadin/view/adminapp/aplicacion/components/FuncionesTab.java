package mx.com.gunix.ui.vaadin.view.adminapp.aplicacion.components;

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
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class FuncionesTab extends CustomComponent {
	private static final ThreadLocal<Boolean> rolSelectChkBxMap = new ThreadLocal<Boolean>();
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
	private static final String DESC_MODULO = "Módulo ";

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the visual editor.
	 */
	public FuncionesTab(boolean soloLectura, boolean esParaRoles) {
		buildMainLayout();
		setCompositionRoot(mainLayout);

		funcionesTreeTable.setTableFieldFactory(new GunixTableFieldFactory());
		funcionesTreeTable.addContainerProperty("idFuncion", String.class, "", "Id Función", null, null);
		if (esParaRoles && !soloLectura) {
			funcionesTreeTable.addContainerProperty("selectFuncion", CheckBox.class, Boolean.FALSE, "", null, null);
			funcionesTreeTable.setColumnExpandRatio("selectFuncion", 0.0f);
		}
		funcionesTreeTable.addContainerProperty("titulo", String.class, "", "Título", null, null);
		funcionesTreeTable.addContainerProperty("descripcion", String.class, "", "Descripción", null, null);
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
			if (esParaRoles) {
				funcionesTreeTable.setMultiSelect(true);
				funcionesTreeTable.setCellStyleGenerator((table, itemId, propertyId) -> {
					String style = null;
					if (propertyId == null) {
						// Styling for row
						String idFuncion = (String) table.getItem(itemId).getItemProperty("idFuncion").getValue();
						if (idFuncion.startsWith(DESC_MODULO)) {
							style = "admin-app-highlight-text-bold";
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

		this.esSoloLectura = soloLectura;
		this.esParaRoles = esParaRoles;
	}

	private void toggleRutaFuncion(Set<Funcion> currVals, Funcion funcion, Boolean selected) {
		if (funcion.getHijas() != null) {
			funcion.getHijas().forEach(funcionHija -> {
				toggleRutaFuncion(currVals, funcionHija, selected);
			});
		}
		HierarchicalContainer container = (HierarchicalContainer) funcionesTreeTable.getContainerDataSource();
		((CheckBox) container.getItem(funcion).getItemProperty("selectFuncion").getValue()).setValue(selected);
		if (selected) {
			currVals.add(funcion);
			agregaPadre(funcion, currVals);
		} else {
			currVals.remove(funcion);
		}
	}

	private void agregaPadre(Funcion funcionSel, Set<Funcion> newValue) {
		Funcion funcionPadre = (Funcion) funcionesTreeTable.getParent(funcionSel);
		if (funcionPadre != null) {
			agregaPadre(funcionPadre, newValue);
		}
		HierarchicalContainer container = (HierarchicalContainer) funcionesTreeTable.getContainerDataSource();
		((CheckBox) container.getItem(funcionSel).getItemProperty("selectFuncion").getValue()).setValue(Boolean.TRUE);
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

	@SuppressWarnings("unchecked")
	private void addFunciones(Funcion funcionPadre, List<Funcion> funciones, List<Funcion> funcionesSel, Set<Funcion> selItems) {
		if (funcionPadre != null
				&& ((!esParaRoles && !esSoloLectura && !funcionesTreeTable.getItemIds().stream().filter(bI -> ((BeanItem<Funcion>) bI).getBean().equals(funcionPadre)).findFirst().isPresent()) || ( !(!esParaRoles && !esSoloLectura) && funcionesTreeTable.getItem(funcionPadre) == null))) {
			
			boolean isSelected = isSelected(funcionPadre, funcionesSel);
			if (!esParaRoles && !esSoloLectura) {
				initFuncion(funcionPadre, null);
			} else {
				initFuncion((esParaRoles && !esSoloLectura) ?
							new Object[] { funcionPadre.getIdFuncion(), buildCheckBox(funcionPadre, isSelected), funcionPadre.getTitulo(), funcionPadre.getDescripcion(), funcionPadre.getProcessKey() }
							:new Object[] { funcionPadre.getIdFuncion(), funcionPadre.getTitulo(), funcionPadre.getDescripcion(), funcionPadre.getProcessKey() }
							, funcionPadre);
			}
			if (isSelected) {
				selItems.add(funcionPadre);
			}
		}

		for (Funcion funcionHija : funciones) {
			if (!esParaRoles && !esSoloLectura) {
				initFuncion(funcionHija, null);
			} else {
				boolean isSelected=isSelected(funcionHija, funcionesSel);
				Object[] cells = !esParaRoles ? 
							new Object[] { funcionHija.getIdFuncion(), funcionHija.getTitulo(), funcionHija.getDescripcion(), funcionHija.getProcessKey(), (funcionHija.getHijas() == null || funcionHija.getHijas().isEmpty()) ? 
									buildParametrosButton(funcionHija, esSoloLectura) 
									: null}
							: (esParaRoles && !esSoloLectura) ?
									new Object[] { funcionHija.getIdFuncion(), buildCheckBox(funcionHija, isSelected), funcionHija.getTitulo(), funcionHija.getDescripcion(), funcionHija.getProcessKey() }
									: new Object[] { funcionHija.getIdFuncion(),funcionHija.getTitulo(), funcionHija.getDescripcion(), funcionHija.getProcessKey() };

				initFuncion(cells, funcionHija);
				
				if (funcionPadre != null) {
					funcionesTreeTable.setParent(funcionHija, funcionPadre);
				}
				if (isSelected) {
					selItems.add(funcionHija);
				}
			}
			if (funcionHija.getHijas() != null && !funcionHija.getHijas().isEmpty()) {
				addFunciones(funcionHija, funcionHija.getHijas(), funcionesSel, selItems);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private CheckBox buildCheckBox(Funcion funcion, boolean isSelected) {
		CheckBox chbox = new CheckBox();
		chbox.setValue(isSelected);
		chbox.setImmediate(true);
		chbox.setData(funcion);
		chbox.addValueChangeListener(vchEvLs -> {
			if (rolSelectChkBxMap.get() == null || !rolSelectChkBxMap.get()) {
				rolSelectChkBxMap.set(Boolean.TRUE);
				Set<Funcion> selItems = new LinkedHashSet<Funcion>((Set<Funcion>) funcionesTreeTable.getValue());
				toggleRutaFuncion(selItems, (Funcion) chbox.getData(), chbox.getValue());
				funcionesTreeTable.setValue(selItems);
				funcionesTreeTable.getParent().markAsDirtyRecursive();
				rolSelectChkBxMap.set(Boolean.FALSE);
			}
		});
		return chbox;
	}

	private void initFuncion(Object[] cells, Funcion funcion) {
		funcionesTreeTable.addItem(cells, funcion);
		funcionesTreeTable.setCollapsed(funcion, false);
		funcionesTreeTable.setChildrenAllowed(funcion, true);
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
						if (isSelected(funcionPadre, f.getHijas())) {
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

		BeanItem<Funcion> beanItem = initFuncion(f, event);

		Property<String> idFuncionProp = container.getContainerProperty(beanItem, "idFuncion");
		idFuncionProp.setValue(f.getIdFuncion());
		if (f.getPadre() == null) {
			modulo.getFunciones().add(f);
		}
		agregarFuncion.setEnabled(true);
	}

	@SuppressWarnings("unchecked")
	private BeanItem<Funcion> initFuncion(Funcion f, ClickEvent event) {
		HierarchicalContainer container = (HierarchicalContainer) funcionesTreeTable.getContainerDataSource();
		BeanItem<Funcion> beanItem = new BeanItem<Funcion>(f);
		GunixBeanFieldGroup<Funcion> bfgf = new GunixBeanFieldGroup<Funcion>(Funcion.class);
		bfgf.setItemDataSource(beanItem);
		Button button = new Button("Agregar", newFuncButtonEvent -> {
			doAgregarFuncion(newFuncButtonEvent);
		});

		button.setData(bfgf);

		funcionesTreeTable.addItem(beanItem);
		container.getItem(beanItem).getItemProperty("agregarButton").setValue(button);
		container.getItem(beanItem).getItemProperty("parametrosButton").setValue(buildParametrosButton(f, false));

		if (event != null && (button = (Button) event.getComponent()).getParent() == funcionesTreeTable) {
			Funcion padre = ((GunixBeanFieldGroup<Funcion>) button.getData()).getItemDataSource().getBean();
			beanItem.getBean().setPadre(padre);
			if (padre.getHijas() == null) {
				padre.setHijas(new ArrayList<Funcion>());
			}
			padre.getHijas().add(beanItem.getBean());

			funcionesTreeTable.setParent(beanItem, ((GunixBeanFieldGroup<Funcion>) button.getData()).getItemDataSource());
		} else {
			container.getItem(beanItem).getItemProperty("idFuncion").setValue(f.getIdFuncion() == null ? "" : f.getIdFuncion());
			container.getItem(beanItem).getItemProperty("descripcion").setValue(f.getDescripcion() == null ? "" : f.getDescripcion());
			if (f.getHijas() == null || f.getHijas().isEmpty()) {
				container.getItem(beanItem).getItemProperty("processKey").setValue(f.getProcessKey() == null ? "" : f.getProcessKey());
			}
			container.getItem(beanItem).getItemProperty("titulo").setValue(f.getTitulo() == null ? "" : f.getTitulo());
			if (f.getPadre() != null) {
				funcionesTreeTable.setParent(beanItem, funcionesTreeTable.getItemIds().stream().filter(bI -> ((BeanItem<Funcion>) bI).getBean().equals(f.getPadre())).findFirst().get());
			}
		}

		funcionesTreeTable.setCollapsed(beanItem, false);
		funcionesTreeTable.setChildrenAllowed(beanItem, true);

		return beanItem;
	}

	private Button buildParametrosButton(Funcion f, boolean esSoloLectura) {
		Button button = new Button("Parámetros", newFuncButtonEvent -> {
			final Window window = new Window("Parámetros");
			window.setModal(true);
			window.setClosable(false);
			window.setResizable(false);
			window.setWidth("410px");
			window.setHeight("390px");
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
			throw new CommitException("El módulo " + modulo.getIdModulo() + " tiene errores");
		} else {
			funcionesTreeTable.setComponentError(null);
			moduloBFG.commit(cv -> {
				UserError ue = new UserError(cv.getMessage());
				if ("funciones".equals(cv.getPropertyPath().toString())) {
					funcionesTreeTable.setComponentError(ue);
				}
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
		mainLayout.setWidth("100.0%");
		mainLayout.setHeight("100.0%");
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");

		// agregarFuncion
		agregarFuncion = new Button();
		agregarFuncion.setCaption("Agregar Menú");
		agregarFuncion.setImmediate(true);
		agregarFuncion.setWidth("-1px");
		agregarFuncion.setHeight("-1px");
		mainLayout.addComponent(agregarFuncion);

		// funcionesTreeTable
		funcionesTreeTable = new TreeTable();
		funcionesTreeTable.setImmediate(false);
		funcionesTreeTable.setWidth("100%");
		funcionesTreeTable.setHeight("255px");
		funcionesTreeTable.setInvalidAllowed(false);
		mainLayout.addComponent(funcionesTreeTable);

		return mainLayout;
	}

}
