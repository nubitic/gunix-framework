package mx.com.gunix.ui.vaadin.view.adminapp.usuario;

import java.util.ArrayList;
import java.util.List;

import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.security.domain.Usuario;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;
import mx.com.gunix.framework.ui.vaadin.view.AbstractGunixView;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.RowHeaderMode;
import com.vaadin.ui.TextField;

@GunixVaadinView
public class UsuarioSearchView extends AbstractGunixView<Usuario> {

	private static final long serialVersionUID = 1L;
	private TextField idUsuario;
	private Button buscaButton;
	private Table resultadosTable;
	private Boolean esMaestro;
	
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doEnter(ViewChangeEvent event) {
		buscaButton.setEnabled(true);

		List<Usuario> usuarios = (List<Usuario>) $("resultado");
		resultadosTable.removeAllItems();
		
		if (usuarios != null && !usuarios.isEmpty()) {
			resultadosTable.setVisible(true);
			usuarios.forEach(usuario -> {
				resultadosTable.addItem(new Object[] {
						usuario.getIdUsuario(),			
						usuario.getDatosUsuario()!=null?usuario.getDatosUsuario().getNombre().concat(" ").concat(usuario.getDatosUsuario().getApPaterno().concat(" ").concat(usuario.getDatosUsuario().getApMaterno())):" ",
						usuario.isActivo()==true?"ACTIVO":(usuario.isBloqueado()==true?"BLOQUEADO":(usuario.isEliminado()==true?"ELIMINADO":"NO DEFINIDO"))}, usuario);
				//resultadosTable.setItemIcon(usuario, new ThemeResource("img/1452591654_marty-mcfly.png"));
			});
		} else {
			if (esMaestro != null) {
				resultadosTable.setVisible(false);
				appendNotification(Type.HUMANIZED_MESSAGE, "No se encontraron aplicaciones con los filtros indicados");
			}
		}
		
	}

	@Override
	protected void doConstruct() {
		flyt.setIcon(new ThemeResource("img/1440816106_window.png"));
		flyt.setCaption(new StringBuilder($("operación").toString()).append(" de Usuarios").toString());
		
		idUsuario = new TextField();
		idUsuario.setCaption("Id Usuario");
		idUsuario.setImmediate(false);
		idUsuario.setRequired(false);
		idUsuario.setNullRepresentation("");
		idUsuario.setInvalidCommitted(true);
		idUsuario.setWidth("-1px");
		idUsuario.setHeight("-1px");
		flyt.addComponent(idUsuario);
		
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
		resultadosTable.setWidth("70.0%");
		resultadosTable.setHeight("-1px");
		resultadosTable.setInvalidAllowed(false);
		resultadosTable.setVisible(false);
		resultadosTable.setSelectable(true);
		resultadosTable.setMultiSelect(false);
		
		resultadosTable.addContainerProperty("idUsuario", String.class, "", "Id Usuario", null, null);
		resultadosTable.addContainerProperty("nombre", String.class, "", "Nombre", null, null);
		resultadosTable.addContainerProperty("estatus", String.class, "", "Estatus", null, null);
		
		resultadosTable.setColumnExpandRatio("idUsuario", 0.5f);
		resultadosTable.setColumnExpandRatio("nombre", 1f);
		resultadosTable.setColumnExpandRatio("estatus", 1f);
		resultadosTable.setRowHeaderMode(RowHeaderMode.ICON_ONLY);
		resultadosTable.setPageLength(5);
		
		resultadosTable.addItemClickListener(itemClickEvnt -> {
			esMaestro = Boolean.FALSE;
			resultadosTable.setValue(itemClickEvnt.getItemId());
			completaTarea();
		});

		flyt.addComponent(resultadosTable);
		camposVaciosSonValidos();
		initBean((Usuario)$("usuario"));
	}

	@Override
	protected List<Variable<?>> getVariablesTarea() {
		List<Variable<?>> vars = new ArrayList<Variable<?>>();

		Variable<Boolean> esMaestroVar = new Variable<Boolean>();
		esMaestroVar.setValor(esMaestro);
		esMaestroVar.setNombre("esMaestro");
		vars.add(esMaestroVar);

		Variable<Usuario> appBusquedaVar = new Variable<Usuario>();
		appBusquedaVar.setValor(esMaestro ? getBean() : (Usuario) resultadosTable.getValue());
		appBusquedaVar.setNombre("usuario");

		vars.add(appBusquedaVar);
		return vars;
	}

	@Override
	protected String getComentarioTarea() {
		// TODO Auto-generated method stub
		return null;
	}

}
