/**
 * Aquí se deben depositar las clases simples de java (Java Beans)
 * sobre los que se soporta el Negocio de la Aplicación.
 *
 * Ejemplo:
 *
 * <pre>
 * <code>
 * package mx.com.gunix.domain;
 * 
 * import java.io.Serializable;
 * 
 * import javax.validation.constraints.NotNull;
 * 
 * public class Cliente implements Serializable {
 * 	private static final long serialVersionUID = 1L;
 * 
 * 	{@literal @}NotNull
 * 	private String nombre;
 * 	private Long id;
 * 
 * 	public String getNombre() {
 * 		return nombre;
 * 	}
 * 
 * 	public void setNombre(String nombre) {
 * 		this.nombre = nombre;
 * 	}
 * 
 * 	public Long getId() {
 * 		return id;
 * 	}
 * 
 * 	public void setId(Long id) {
 * 		this.id = id;
 * 	}
 * 
 * 	{@literal @}Override
 * 	public int hashCode() {
 * 		final int prime = 31;
 * 		int result = 1;
 * 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 * 		result = prime * result + ((nombre == null) ? 0 : nombre.hashCode());
 * 		return result;
 * 	}
 * 
 * 	{@literal @}Override
 * 	public boolean equals(Object obj) {
 * 		if (this == obj)
 * 			return true;
 * 		if (obj == null)
 * 			return false;
 * 		if (getClass() != obj.getClass())
 * 			return false;
 * 		Cliente other = (Cliente) obj;
 * 		if (id == null) {
 * 			if (other.id != null)
 * 				return false;
 * 		} else if (!id.equals(other.id))
 * 			return false;
 * 		if (nombre == null) {
 * 			if (other.nombre != null)
 * 				return false;
 * 		} else if (!nombre.equals(other.nombre))
 * 			return false;
 * 		return true;
 * 	}
 * 
 * 	{@literal @}Override
 * 	public String toString() {
 * 		return "Cliente [nombre=" + nombre + ", id=" + id + "]";
 * 	}
 * 
 * }
 * </code>
 * </pre>
 * @since 1.0
 * @see <a href="http://docs.oracle.com/javase/tutorial/javabeans/">The Java(TM) Tutorials - Trail: JavaBeans(TM)</a>
 * @see <a href="https://docs.oracle.com/javaee/6/tutorial/doc/gircz.html">The Java EE 6 Tutorial - Using Bean Validation</a>
 */
package mx.com.gunix.domain;
