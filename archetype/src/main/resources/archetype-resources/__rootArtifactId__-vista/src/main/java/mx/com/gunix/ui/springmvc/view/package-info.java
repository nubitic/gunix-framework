/**
 * Aquí se deben depositar las clases java que representan las vistas
 * del sistema
 *
 * Ejemplo:
 *
 * <pre>
 * <code>
 *package mx.com.gunix.ui.springmvc.view;
 *
 *import java.util.ArrayList;
 *import java.util.List;
 *
 *import javax.servlet.http.HttpServletRequest;
 *
 *import mx.com.gunix.domain.Cliente;
 *import mx.com.gunix.framework.processes.domain.Variable;
 *import mx.com.gunix.framework.ui.springmvc.AbstractGunixController;
 *import mx.com.gunix.framework.ui.springmvc.spring.GunixSpringMVCView;
 *
 *import org.springframework.ui.Model;
 *
 *{@literal @}GunixSpringMVCView("cliente")
 *public class ClientesView extends AbstractGunixController<Cliente> {
 *	{@literal @}Override
 *	protected String doConstruct(Model uiModel) {
 *		return "view.ClientesView";
 *	}
 *
 *	{@literal @}Override
 *	protected List<Variable<?>> getVariablesTarea(HttpServletRequest request) {
 *		List<Variable<?>> vars = new ArrayList<Variable<?>>();
 *		Variable<Cliente> clienteVar = new Variable<Cliente>();
 *		clienteVar.setValor(getBean());
 *		clienteVar.setNombre("cliente");
 *		vars.add(clienteVar);
 *		return vars;
 *	}
 *
 *	{@literal @}Override
 *	protected String getComentarioTarea(HttpServletRequest request) {
 *		return null;
 *	}
 *
 *	{@literal @}Override
 *	protected String doEnter(HttpServletRequest request) {
 *		return null;
 *	}
 *}
 * </code>
 * </pre>
 * @since 1.0
 * @see <a href="http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html#mvc-controller">}> Implementing Controllers</a>
 * @see <a href="http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html#mvc-view-resolvers-tbl">}> View resolvers</a>
 */
package mx.com.gunix.ui.springmvc.view;
