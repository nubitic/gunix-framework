package mx.com.gunix.ui.vaadin.view.adminapp.aplicacion;

import java.util.ArrayList;
import java.util.List;

import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.security.domain.Aplicacion;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;
import mx.com.gunix.framework.ui.vaadin.view.AbstractGunixView;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.TextField;

@GunixVaadinView
public class AplicacionSearchView extends AbstractGunixView<Aplicacion> {
	private static final long serialVersionUID = 1L;

	private TextField descripcion;
	private TextField idAplicacion;
	private Button buscaButton;
	private Table resultadosTable;

	private Boolean esMaestro;

	@SuppressWarnings("unchecked")
	@Override
	protected void doEnter(ViewChangeEvent event) {
		buscaButton.setEnabled(true);

		List<Aplicacion> aplicaciones = (List<Aplicacion>) $("resultado");
		resultadosTable.removeAllItems();

		if (aplicaciones != null && !aplicaciones.isEmpty()) {
			resultadosTable.setVisible(true);
			aplicaciones.forEach(aplicacion -> {
				resultadosTable.addItem(new Object[] { aplicacion.getIdAplicacion(), aplicacion.getDescripcion() }, aplicacion);
				resultadosTable.setItemIcon(aplicacion, new ThemeResource("img/" + aplicacion.getIcono()));
			});
		} else {
			if (esMaestro != null) {
				resultadosTable.setVisible(false);
				Notification.show("No se encontraron aplicaciones con los filtros indicados", Type.HUMANIZED_MESSAGE);
			}
		}
	}

	@Override
	protected void doConstruct() {

		flyt.setIcon(new ThemeResource("img/1440816106_window.png"));
		flyt.setCaption(new StringBuilder($("operación").toString()).append(" de Aplicaciones").toString());

		idAplicacion = new TextField();
		idAplicacion.setCaption("Id Aplicacion");
		idAplicacion.setImmediate(false);
		idAplicacion.setRequired(false);
		idAplicacion.setNullRepresentation("");
		idAplicacion.setInvalidCommitted(true);
		idAplicacion.setWidth("-1px");
		idAplicacion.setHeight("-1px");
		flyt.addComponent(idAplicacion);

		descripcion = new TextField();
		descripcion.setCaption("Descripción");
		descripcion.setImmediate(false);
		descripcion.setRequired(false);
		descripcion.setNullRepresentation("");
		descripcion.setInvalidCommitted(true);
		descripcion.setWidth("-1px");
		descripcion.setHeight("-1px");
		flyt.addComponent(descripcion);

		buscaButton = new Button();
		buscaButton.setCaption("Buscar...");
		buscaButton.setImmediate(true);
		buscaButton.setWidth("-1px");
		buscaButton.setHeight("-1px");
		buscaButton.setDisableOnClick(true);
		buscaButton.addClickListener(clickEvnt -> {
			esMaestro = Boolean.TRUE;
			try {
				commit();
			} catch (CommitException ignorar) {
			}
			completaTarea();
		});
		flyt.addComponent(buscaButton);

		resultadosTable = new Table();
		resultadosTable.setImmediate(false);
		resultadosTable.setWidth("50.0%");
		resultadosTable.setHeight("-1px");
		resultadosTable.setInvalidAllowed(false);
		resultadosTable.setVisible(false);
		resultadosTable.setSelectable(true);
		resultadosTable.setMultiSelect(false);

		resultadosTable.addContainerProperty("idAplicacion", String.class, "", "Id Aplicación", null, null);
		resultadosTable.addContainerProperty("descripcion", String.class, "", "Descripción", null, null);

		resultadosTable.setColumnExpandRatio("idAplicacion", 0.5f);
		resultadosTable.setColumnExpandRatio("descripcion", 1f);
		resultadosTable.setRowHeaderMode(RowHeaderMode.ICON_ONLY);
		resultadosTable.setPageLength(5);

		resultadosTable.addItemClickListener(itemClickEvnt -> {
			esMaestro = Boolean.FALSE;
			resultadosTable.setValue(itemClickEvnt.getItemId());
			completaTarea();
		});

		flyt.addComponent(resultadosTable);
	}

	@Override
	protected List<Variable<?>> getVariablesTarea() {
		List<Variable<?>> vars = new ArrayList<Variable<?>>();

		Variable<Boolean> esMaestroVar = new Variable<Boolean>();
		esMaestroVar.setValor(esMaestro);
		esMaestroVar.setNombre("esMaestro");
		vars.add(esMaestroVar);

		Variable<Aplicacion> appBusquedaVar = new Variable<Aplicacion>();
		appBusquedaVar.setValor(esMaestro ? getBean() : (Aplicacion) resultadosTable.getValue());
		appBusquedaVar.setNombre("aplicacion");

		vars.add(appBusquedaVar);
		return vars;
	}

	@Override
	protected String getComentarioTarea() {
		// TODO Auto-generated method stub
		return null;
	}

}
