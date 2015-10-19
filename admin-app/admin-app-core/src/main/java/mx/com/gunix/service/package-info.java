/**
 * Aquí se deben depositar las interfaces java que indican que funcionalidad
 * de negocio se puede ejecutar en la aplicación.
 *
 * Ejemplo:
 *
 * <pre>
 * <code>
 * package mx.com.gunix.service;
 *
 * import mx.com.gunix.domain.Cliente;
 *
 * import org.springframework.security.access.annotation.Secured;
 * import org.springframework.security.access.vote.AuthenticatedVoter;
 *
 * {@literal @}Secured(AuthenticatedVoter.IS_AUTHENTICATED_REMEMBERED)
 * public interface ClienteService {
 *  	public void guarda(Cliente cliente);
 *  	public Boolean isValid(Cliente cliente);
 * }
 * </code>
 * </pre>
 * @since 1.0
 * @see <a href="http://docs.spring.io/spring-security/site/docs/4.0.1.RELEASE/reference/htmlsingle/#jc-method">Spring Security - Method Security</a>
 * @see <a href="http://docs.spring.io/spring-security/site/docs/4.0.1.RELEASE/reference/htmlsingle/#authorization">Spring Security - Authorization</a>
 */
package mx.com.gunix.service;
