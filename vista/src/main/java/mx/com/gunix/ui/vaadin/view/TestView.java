package mx.com.gunix.ui.vaadin.view;

import java.util.ArrayList;
import java.util.List;

import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.processes.domain.Variable.Scope;
import mx.com.gunix.framework.ui.vaadin.view.AbstractGunixView;
import mx.com.gunix.ui.vaadin.view.component.TestComponent;

import org.vaadin.spring.navigator.annotation.VaadinView;

import com.vaadin.data.Item;
import com.vaadin.ui.Alignment;

@VaadinView(name = "mx.com.gunix.ui.vaadin.view.TestView")
public class TestView extends AbstractGunixView {
	private static final long serialVersionUID = 1L;

	TestComponent tc;

	@Override
	protected void doConstruct() {
		tc = getBeanComponent(TestComponent.class);
		tc.getCompleteTaskButton().addClickListener(event -> {
			completaTarea();
		});
		addComponent(tc);
		setComponentAlignment(tc, Alignment.MIDDLE_CENTER);
	}

	@Override
	protected List<Variable> getVariablesTarea() {
		List<Variable> vars = new ArrayList<Variable>();
		tc.getDummyTableVars().getItemIds().stream().forEach(itemId -> {
			Item it = tc.getDummyTableVars().getItem(itemId);
			Variable v = new Variable();
			v.setScope(Scope.TAREA);
			v.setNombre((String) it.getItemProperty("Variable").getValue());
			v.setValor((String) it.getItemProperty("Valor").getValue());
			vars.add(v);
		});
		return vars;
	}

	@Override
	protected String getComentarioTarea() {
		return "no comment";
	}

}
