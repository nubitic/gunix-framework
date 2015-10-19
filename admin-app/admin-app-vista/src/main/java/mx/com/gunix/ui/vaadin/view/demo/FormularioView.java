package mx.com.gunix.ui.vaadin.view.demo;

import java.util.ArrayList;
import java.util.List;

import mx.com.gunix.domain.demo.Formulario;
import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;
import mx.com.gunix.framework.ui.vaadin.view.AbstractGunixView;

import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;

@GunixVaadinView
public class FormularioView extends AbstractGunixView<Formulario>{
	private static final long serialVersionUID = 1L;
	private TextField nombre;
	private TextField apellidoPaterno;
	private TextField apellidoMaterno;
	private TextField sexo;
	private Button enviarButton;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doEnter(ViewChangeEvent event) {
		List<String> errores = (List<String>) $("errores");
		enviarButton.setEnabled(true);
		if(errores!=null&&!errores.isEmpty()){
			Notification.show("Existen errores en el formulario: "+errores, Type.ERROR_MESSAGE);
		}
	}

	@Override
	protected void doConstruct() {		
		flyt.setCaption($("operación").toString());
		
		nombre = new TextField("Nombre");
		nombre.setNullRepresentation("");
		nombre.setInvalidCommitted(false);

		flyt.addComponent(nombre);

		apellidoPaterno = new TextField("Apellido Paterno");
		apellidoPaterno.setNullRepresentation("");
		apellidoPaterno.setInvalidCommitted(false);

		flyt.addComponent(apellidoPaterno);

		apellidoMaterno = new TextField("Apellido Materno");
		apellidoMaterno.setNullRepresentation("");
		apellidoMaterno.setInvalidCommitted(false);

		flyt.addComponent(apellidoMaterno);

		sexo = new TextField("Sexo");
		sexo.setNullRepresentation("");
		sexo.setInvalidCommitted(false);

		flyt.addComponent(sexo);
		
		enviarButton = new Button("Enviar...");
		enviarButton.setDisableOnClick(true);
		enviarButton.addClickListener(event -> {
			try {
				commit();
				completaTarea();
			} catch (CommitException ce) {
				Notification.show("Existen errores en el formulario", Type.ERROR_MESSAGE);
				enviarButton.setEnabled(true);
			}
		});
		flyt.addComponent(enviarButton);
		flyt.setComponentAlignment(enviarButton, Alignment.BOTTOM_RIGHT);
	}

	@Override
	protected List<Variable<?>> getVariablesTarea() {
		List<Variable<?>> vars = new ArrayList<Variable<?>>();
		Variable<Formulario> clienteVar = new Variable<Formulario>();
		clienteVar.setValor(getBean());
		clienteVar.setNombre("form");
		vars.add(clienteVar);
		return vars;
	}

	@Override
	protected String getComentarioTarea() {
		return null;
	}

}
