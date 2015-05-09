package mx.com.gunix.ui.vaadin.view;

import java.util.ArrayList;
import java.util.List;

import mx.com.gunix.domain.Cliente;
import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;
import mx.com.gunix.framework.ui.vaadin.view.AbstractGunixView;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;

@GunixVaadinView
public class ClientesView extends AbstractGunixView {
	private static final long serialVersionUID = 1L;
	private TextField nombre;
	private BeanFieldGroup<Cliente> fieldGroup;

	@Override
	protected void doConstruct() {
		FormLayout flyt = new FormLayout();
		
		flyt.setCaption(getVariable("operación")+ " de Cliente");
		
		nombre = new TextField("Nombre");
		nombre.setNullRepresentation("");
		nombre.setNullSettingAllowed(false);
		nombre.setRequired(true);
		nombre.setRequiredError("El nombre es requerido");
		nombre.setInvalidCommitted(false);

		flyt.addComponent(nombre);

		fieldGroup = new BeanFieldGroup<Cliente>(Cliente.class);
		fieldGroup.setItemDataSource(new Cliente());
		fieldGroup.bindMemberFields(this);

		Button enviarButton = new Button("Enviar...");
		enviarButton.addClickListener(event -> {
			try {
				fieldGroup.commit();
				completaTarea();
			} catch (CommitException ce) {
				Notification.show("Existen errores en el formulario", Type.ERROR_MESSAGE);
			}
		});
		flyt.addComponent(enviarButton);
		flyt.setComponentAlignment(enviarButton, Alignment.BOTTOM_RIGHT);

		addComponent(flyt);
	}

	@Override
	protected List<Variable<?>> getVariablesTarea() {
		List<Variable<?>> vars = new ArrayList<Variable<?>>();
		Variable<Cliente> clienteVar = new Variable<Cliente>();
		clienteVar.setValor(fieldGroup.getItemDataSource().getBean());
		clienteVar.setNombre("cliente");
		vars.add(clienteVar);
		return vars;
	}

	@Override
	protected String getComentarioTarea() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doEnter(ViewChangeEvent event) {
		List<String> errores = (List<String>) getVariable("errores");
		if(errores!=null&&!errores.isEmpty()){
			Notification.show("Existen errores en el formulario: "+errores, Type.ERROR_MESSAGE);
		}
	}

}
