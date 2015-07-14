/**
 * Aquí se deben depositar las interfaces java que indican como mapear los métodos
 * de negocio hacia la Base de Datos
 *
 * Ejemplo:
 *
 * <pre>
 * <code>
 * package mx.com.gunix.domain.persistence;
 * 
 * import mx.com.gunix.domain.Cliente;
 * 
 * import org.apache.ibatis.annotations.Insert;
 * import org.apache.ibatis.annotations.Options;
 * import org.apache.ibatis.annotations.Result;
 * import org.apache.ibatis.annotations.Results;
 * import org.apache.ibatis.annotations.Select;
 * 
 * public interface ClienteMapper {
 * 	{@literal @}Insert("insert into CLIENTE(nombre) values(#{nombre})")
 * 	{@literal @}Options(useGeneratedKeys=true, keyColumn="id_cliente")
 * 	public void guarda(Cliente cliente);
 * 	
 * 	{@literal @}Select("Select * from CLIENTE where nombre=#{nombre}")
 * 	{@literal @}Results({{@literal @}Result(id=true,column="id",property="id"),
 * 			  {@literal @}Result(column="nombre",property="nombre")})
 * 	public Cliente getClienteByNombre(String nombre);
 * }
 * </code>
 * </pre>
 * @since 1.0
 * @see <a href="https://mybatis.github.io/mybatis-3/java-api.html#Mapper_Annotations">MyBatis Java API - Mapper Annotations</a>
 */
package mx.com.gunix.domain.persistence;
