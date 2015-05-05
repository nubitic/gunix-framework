package mx.com.gunix.ui.vaadin.view;

import java.util.ArrayList;
import java.util.List;

import mx.com.gunix.domain.Cliente;
import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;
import mx.com.gunix.framework.ui.vaadin.view.AbstractGunixView;
import mx.com.gunix.ui.vaadin.view.component.ClienteForm;

import com.vaadin.ui.Alignment;

@GunixVaadinView
public class ClientesView extends AbstractGunixView {
	private static final long serialVersionUID = 1L;

	ClienteForm cf;

	@Override
	protected void doConstruct() {
		cf = getBeanComponent(ClienteForm.class);
		cf.getCompleteTaskButton().addClickListener(event -> {
			completaTarea();
		});
		addComponent(cf);
		setComponentAlignment(cf, Alignment.MIDDLE_CENTER);
	}

	@Override
	protected List<Variable<?>> getVariablesTarea() {
		List<Variable<?>> vars = new ArrayList<Variable<?>>();
		Variable<Cliente> clienteVar = new Variable<Cliente>();
		Cliente cliente = new Cliente();
		cliente.setNombre("Mawaps");
		cliente.setId(1L);
		clienteVar.setValor(cliente);
		clienteVar.setNombre("cliente");
		vars.add(clienteVar);
		return vars;
	}

	@Override
	protected String getComentarioTarea() {
		return null;
	}

}
