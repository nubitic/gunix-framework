package mx.com.gunix.ui.vaadin.view.demo;

import java.util.ArrayList;
import java.util.List;

import mx.com.gunix.domain.demo.Formulario;
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
public class FormularioView extends AbstractGunixView{
	private static final long serialVersionUID = 1L;
	private TextField nombre;
	private TextField apellidoPaterno;
	private TextField apellidoMaterno;
	private TextField sexo;
	private BeanFieldGroup<Formulario> fieldGroup;
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
		FormLayout flyt = new FormLayout();
		
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
		
		fieldGroup = new BeanFieldGroup<Formulario>(Formulario.class);
		fieldGroup.setItemDataSource(new Formulario());
		fieldGroup.bindMemberFields(this);

		enviarButton = new Button("Enviar...");
		enviarButton.setDisableOnClick(true);
		enviarButton.addClickListener(event -> {
			try {
				fieldGroup.commit();
				completaTarea();
			} catch (CommitException ce) {
				Notification.show("Existen errores en el formulario", Type.ERROR_MESSAGE);
				enviarButton.setEnabled(true);
			}
		});
		flyt.addComponent(enviarButton);
		flyt.setComponentAlignment(enviarButton, Alignment.BOTTOM_RIGHT);

		addComponent(flyt);
	}

	@Override
	protected List<Variable<?>> getVariablesTarea() {
		List<Variable<?>> vars = new ArrayList<Variable<?>>();
		Variable<Formulario> clienteVar = new Variable<Formulario>();
		clienteVar.setValor(fieldGroup.getItemDataSource().getBean());
		clienteVar.setNombre("form");
		vars.add(clienteVar);
		return vars;
	}

	@Override
	protected String getComentarioTarea() {
		return null;
	}

}
