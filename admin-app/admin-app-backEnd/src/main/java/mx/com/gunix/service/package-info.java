/**
 * Aquí se deben depositar las implementaciones de las interfaces java que
 * indican que funcionalidad de negocio se puede ejecutar en la aplicación.
 *
 * Ejemplo:
 *
 * <pre>
 * <code>
 * package mx.com.gunix.service;
 * 
 * import java.util.ArrayList;
 * import java.util.List;
 * 
 * import mx.com.gunix.domain.Cliente;
 * import mx.com.gunix.domain.persistence.ClienteMapper;
 * import mx.com.gunix.framework.service.GunixActivitServiceSupport;
 * 
 * import org.springframework.beans.factory.annotation.Autowired;
 * import org.springframework.stereotype.Service;
 * import org.springframework.transaction.annotation.Transactional;
 * 
 * {@literal @}Service("clienteService")
 * {@literal @}Transactional(rollbackFor = Exception.class)
 * public class ClienteServiceImpl extends GunixActivitServiceSupport implements ClienteService{
 * 	{@literal @}Autowired
 * 	ClienteMapper cm;
 * 
 * 	{@literal @}Override
 * 	public void guarda(Cliente cliente) {
 * 		cm.guarda(cliente);
 * 		actualizaVariable(cliente);
 * 	}
 * 
 * 	{@literal @}Override
 * 	public Boolean isValid(Cliente cliente) {
 * 		Cliente clienteBD = cm.getClienteByNombre(cliente.getNombre());
 * 		Boolean ans=Boolean.TRUE;
 * 		if(clienteBD!=null){
 * 			List<String> errores = new ArrayList<String>();
 * 			errores.add(new StringBuilder("El cliente: ").append(cliente.getNombre()).append(" ya existe en la Base de Datos").toString());
 * 			agregaVariable("errores", errores);
 * 			ans = Boolean.FALSE;
 * 		}
 * 		return ans;
 * 	}
 * }
 * </code>
 * </pre>
 * @since 1.0
 * @see <a href="http://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#beans-autowired-annotation">Spring Framework - @Autowired</a>
 * @see <a href="https://mybatis.github.io/spring/mappers.html">mybatis-spring - Injecting Mappers</a>
 * @see <a href="http://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#beans-classpath-scanning">Spring Framework - Classpath scanning and managed components</a>
 * @see <a href="http://docs.spring.io/spring/docs/current/spring-framework-reference/htmlsingle/#transaction-declarative-annotations">Spring Framework - Using @Transactional</a>
 */
package mx.com.gunix.service;
